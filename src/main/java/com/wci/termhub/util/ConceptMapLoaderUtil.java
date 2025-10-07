/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wci.termhub.algo.ProgressEvent;
import com.wci.termhub.algo.ProgressListener;
import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.fhir.rest.r5.FhirUtilityR5;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.Mapping;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.MapsetRef;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.TerminologyRef;
import com.wci.termhub.service.EntityRepositoryService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Utility class for loading ConceptMap format JSON files.
 */
public final class ConceptMapLoaderUtil {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConceptMapLoaderUtil.class);

  /** The id map. Used by QA to map from test files to internal ids. */
  private static Map<String, String> idMap =
      new LinkedHashMap<String, String>(101 * 4 / 3, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<String, String> eldest) {
          return size() > 100;
        }
      };

  /**
   * Instantiates a new concept map loader.
   */
  private ConceptMapLoaderUtil() {
    // n/a
  }

  /**
   * Map original id.
   *
   * @param originalId the original id
   * @return the string
   */
  public static String mapOriginalId(final String originalId) {
    return idMap.get(originalId);
  }

  /**
   * Load concept map.
   *
   * @param <T> the generic type
   * @param service the service
   * @param file the file
   * @param type the type
   * @param listener the listener
   * @return the t
   * @throws Exception the exception
   */
  public static <T> T loadConceptMap(final EntityRepositoryService service, final File file,
    final Class<T> type, final ProgressListener listener) throws Exception {
    return loadConceptMap(service, file, type, listener, null);
  }

  /**
   * Load concept map.
   *
   * @param <T>          the generic type
   * @param service      the service
   * @param file         the file
   * @param type         the type
   * @param listener     the listener
   * @param isSyndicated the is syndicated
   * @return the t
   * @throws Exception the exception
   */
  public static <T> T loadConceptMap(final EntityRepositoryService service, final File file,
      final Class<T> type, final ProgressListener listener, final Boolean isSyndicated)
      throws Exception {
    return indexConceptMap(service, file, type, listener, isSyndicated);
  }

  /**
   * Index concept map.
   *
   * @param <T> the generic type
   * @param service the service
   * @param file the file
   * @param type the type
   * @param listener the listener
   * @param isSyndicated the is syndicated
   * @return the t
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private static <T> T indexConceptMap(final EntityRepositoryService service, final File file,
      final Class<T> type, final ProgressListener listener, final Boolean isSyndicated)
      throws Exception {

    final long startTime = System.currentTimeMillis();

    // Use JSON as a way to handle both R4 and R5
    final JsonNode conceptMap = ThreadLocalMapper.get().readTree(file);

    try {
      // Set listener to 0%
      listener.updateProgress(new ProgressEvent(0));

      LOGGER.info("Indexing ConceptMap {} = {}", conceptMap.path("title"), conceptMap.path("url"));

      // Basic checks
      // Validate required fields
      if (conceptMap.get("url").isMissingNode()) {
        throw FhirUtilityR4.exception("ConceptMap.url is required", IssueType.INVALID,
            HttpServletResponse.SC_BAD_REQUEST);
      }

      // check for existing
      final String abbreviation = conceptMap.path("title").asText();
      final String publisher = conceptMap.path("publisher").asText();
      final String version = conceptMap.path("version").asText();
      Mapset mapset = findMapset(service, abbreviation, publisher, version);
      if (mapset != null) {
        throw FhirUtilityR4.exception(
            "Can not create multiple ConceptMap resources the same title, publisher,"
                + " and version. duplicate = " + mapset.getId(),
            IssueType.INVALID, HttpServletResponse.SC_CONFLICT);
      }
      mapset = createMapset(service, conceptMap);

      // Get "source" - either "sourceUri", or "sourceScopeUri", or group.source
      final String source;
      if (conceptMap.has("sourceUri")) {
        source = conceptMap.get("sourceUri").asText();
      } else if (conceptMap.has("sourceScopeUri")) {
        source = conceptMap.get("sourceScopeUri").asText().replaceFirst("\\?fhir_cm", "");
      } else if (conceptMap.has("group") && !conceptMap.get("group").isEmpty()
          && conceptMap.get("group").get(0).has("source")) {
        source = conceptMap.get("group").get(0).get("source").asText();
      } else {
        throw FhirUtilityR4.exception("ConceptMap.source is required", IssueType.INVALID,
            HttpServletResponse.SC_BAD_REQUEST);
      }
      mapset.getAttributes().put("fhirSourceUri", source);

      // Get "source" - either "sourceUri", or "sourceScopeUri", or group.source
      final String target;
      if (conceptMap.has("targetUri")) {
        target = conceptMap.get("targetUri").asText();
      } else if (conceptMap.has("targetScopeUri")) {
        target = conceptMap.get("targetScopeUri").asText().replaceFirst("\\?fhir_cm", "");
      } else if (conceptMap.has("group") && !conceptMap.get("group").isEmpty()
          && conceptMap.get("group").get(0).has("target")) {
        target = conceptMap.get("group").get(0).get("target").asText();
      } else {
        throw FhirUtilityR4.exception("ConceptMap.target is required", IssueType.INVALID,
            HttpServletResponse.SC_BAD_REQUEST);
      }
      mapset.getAttributes().put("fhirTargetUri", target);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Create ConceptMap with source: {} and target:{}", source, target);
      }

      int mappingCount = 0;

      // process concept map array
      final JsonNode groupArray = conceptMap.path("group");
      int totalCt = groupArray.size();
      for (final JsonNode groupNode : groupArray) {

        for (final JsonNode elementNode : groupNode.path("element")) {
          final String sourceCode = elementNode.get("code").asText();
          final String sourceName = elementNode.get("display").asText();

          // Set source concept
          final Concept existingFromConcept =
              TerminologyUtility.getConcept(service, mapset.getFromTerminology(),
                  mapset.getFromPublisher(), mapset.getFromVersion(), sourceCode);
          ConceptRef fromConcept = null;
          if (existingFromConcept == null) {
            fromConcept = new ConceptRef();
            fromConcept.setCode(sourceCode);
            fromConcept.setName(sourceName);
            fromConcept.setTerminology(mapset.getFromTerminology());
            fromConcept.setPublisher(mapset.getFromPublisher());
            fromConcept.setVersion(mapset.getFromVersion());
          } else {
            fromConcept = new Concept(existingFromConcept);
          }

          for (final JsonNode targetNode : elementNode.path("target")) {

            final String targetCode =
                targetNode.has("code") ? targetNode.get("code").asText() : null;
            final String targetName = targetNode.has("display") ? targetNode.get("display").asText()
                : "Unable to determine name";

            // Create mapping
            final Mapping mapping = new Mapping();
            mapping.setId(UUID.randomUUID().toString());
            mapping.setActive(true);
            mapping.setMapset(new MapsetRef(mapset.getCode(), mapset.getAbbreviation(),
                mapset.getPublisher(), mapset.getVersion()));

            mapping.setFrom(fromConcept);

            // Set target concept
            final Concept existingToConcept = (targetCode == null || targetCode.equals("NOCODE"))
                ? null : TerminologyUtility.getConcept(service, mapset.getToTerminology(),
                    mapset.getToPublisher(), mapset.getToVersion(), targetCode);

            ConceptRef toConcept = null;
            if (existingToConcept == null) {
              toConcept = new ConceptRef();
              toConcept.setCode(targetCode);
              toConcept.setName(
                  targetCode != null && targetCode.equals("NOCODE") ? "No target" : targetName);
              toConcept.setTerminology(mapset.getToTerminology());
              toConcept.setPublisher(mapset.getToPublisher());
              toConcept.setVersion(mapset.getToVersion());
            } else {
              toConcept = new ConceptRef(existingToConcept);
            }
            mapping.setTo(toConcept);

            if (targetNode.has("equivalence")) {
              mapping.setType(targetNode.get("equivalence").asText());
            } else {
              mapping.setType(targetNode.get("relationship").asText());
            }

            // Set mapping attributes from extensions
            for (final JsonNode extension : targetNode.get("extension")) {

              final String url = extension.get("url").asText();
              final String valueString = extension.get("valueString").asText();

              if (url.endsWith("concept-map-group")) {
                mapping.getAttributes().put("group", valueString);
              } else if (url.endsWith("concept-map-rule")) {
                mapping.getAttributes().put("rule", valueString);
              } else if (url.endsWith("concept-map-priority")) {
                mapping.getAttributes().put("priority", valueString);
              } else if (url.endsWith("concept-map-advice")) {
                mapping.getAttributes().put("advice", valueString);
              }
            }

            // Save mapping
            service.add(Mapping.class, mapping);
            if (++mappingCount % 5000 == 0) {
              LOGGER.info("  count: {}", mappingCount);
              listener.updateProgress(new ProgressEvent((int) (mappingCount * 1.0 / totalCt)));
            }
            // Too much info
            if (LOGGER.isDebugEnabled()) {
              LOGGER.debug("    Created mapping: {}", mapping);
            }
          }
        }
      }

      LOGGER.info("  final counts - mapsets: {}, mappings: {}", 1, mappingCount);
      LOGGER.info("  duration: {} ms", (System.currentTimeMillis() - startTime));

      // Get the mapset again because the tree position computer would've changed it
      mapset = service.get(mapset.getId(), Mapset.class);
      // Set loaded to true and save it again
      mapset.getAttributes().put("loaded", "true");
      if (isSyndicated != null && isSyndicated) {
        mapset.getAttributes().put("syndicated", "true");
      }
      service.update(Mapset.class, mapset.getId(), mapset);

      // Set listener to 100%
      listener.updateProgress(new ProgressEvent(100));

      // R4
      if (type == org.hl7.fhir.r4.model.ConceptMap.class) {
        return (T) FhirUtilityR4.toR4(mapset);
      }
      // else R5
      return (T) FhirUtilityR5.toR5(mapset);

    } catch (final Exception e) {
      LOGGER.error("Error indexing concept map", e);
      throw e;
    }
  }

  /**
   * Gets the terminology.
   *
   * @param service the service
   * @param abbreviation the abbreviation
   * @param publisher the publisher
   * @param version the version
   * @return the terminology
   * @throws Exception the exception
   */
  private static Mapset findMapset(final EntityRepositoryService service, final String abbreviation,
    final String publisher, final String version) throws Exception {

    final SearchParameters searchParams = new SearchParameters();
    searchParams
        .setQuery(TerminologyUtility.getTerminologyAbbrQuery(abbreviation, publisher, version));
    final ResultList<Mapset> mapset = service.find(searchParams, Mapset.class);

    return (mapset.getItems().isEmpty()) ? null : mapset.getItems().get(0);
  }

  /**
   * Creates the mapset.
   *
   * @param service the service
   * @param root the root
   * @return the mapset
   * @throws Exception the exception
   */
  private static Mapset createMapset(final EntityRepositoryService service, final JsonNode root)
    throws Exception {

    // Validate that this is a CodeSystem resource
    if (!root.has("resourceType") || !"ConceptMap".equals(root.get("resourceType").asText())) {
      throw FhirUtilityR4.exception("Invalid resource type - expected ConceptMap",
          IssueType.INVALID, HttpServletResponse.SC_EXPECTATION_FAILED);
    }

    final Mapset mapset = new Mapset();
    mapset.getAttributes().put("loaded", "false");
    // Convert to internal model
    // The HAPI Plan server @Create method blanks the identifier on sending a
    // code system in. Always create a new identifier.
    mapset.setId(UUID.randomUUID().toString());
    final String originalId = root.path("id").asText();
    if (isNotBlank(originalId)) {
      mapset.getAttributes().put("originalId", originalId);
      idMap.put(originalId, mapset.getId());
    }
    mapset.setActive(true);
    mapset.setName(root.path("name").asText());
    mapset.setActive(root.path("active").asBoolean(true));
    mapset.setAbbreviation(root.path("title").asText());
    mapset.setDescription(root.path("description").asText());
    mapset.setPublisher(root.path("publisher").asText());
    final String version = root.path("version").asText();
    mapset.setVersion(version);
    mapset.setReleaseDate(root.path("date").asText().substring(0, 10));

    String fromTerminology = null;
    if (root.has("sourceScopeUri")) {
      fromTerminology = root.path("sourceScopeUri").asText().replaceFirst("\\?fhir_vs$", "");
    } else if (root.has("sourceUri")) {
      fromTerminology = root.path("sourceUri").asText();
    } else if (root.has("group") && (root.get("group").isArray())) {
      fromTerminology = root.path("group").get(0).path("source").asText();
    }
    if (fromTerminology == null) {
      throw FhirUtilityR4.exception("Unable to determine information about the map source",
          IssueType.INVALID, HttpServletResponse.SC_EXPECTATION_FAILED);
    }
    final TerminologyRef fromRef = new TerminologyRef();
    fromRef.setUri(fromTerminology);
    fromRef.setPublisher(root.path("publisher").asText());
    fromRef.setVersion(version);

    String toTerminology = null;
    if (root.has("targetScopeUri")) {
      toTerminology = root.path("targetScopeUri").asText().replaceFirst("\\?fhir_vs$", "");
    } else if (root.has("targetUri")) {
      fromTerminology = root.path("targetUri").asText();
    } else if (root.has("group") && (root.get("group").isArray())) {
      toTerminology = root.path("group").get(0).path("target").asText();
    }
    if (toTerminology == null) {
      throw FhirUtilityR4.exception("Unable to determine information about the map target",
          IssueType.INVALID, HttpServletResponse.SC_EXPECTATION_FAILED);
    }
    final TerminologyRef toRef = new TerminologyRef();
    toRef.setUri(toTerminology);
    toRef.setPublisher(root.path("publisher").asText());
    toRef.setVersion(version);

    // Extract abbreviations from title field
    final String title = root.path("title").asText();
    String[] titleParts = title.split("-");
    final String fromAbbreviation = titleParts.length > 0 ? titleParts[0] : fromTerminology;
    final String toAbbreviation = titleParts.length > 1 ? titleParts[1] : toTerminology;

    // Set the abbreviations
    fromRef.setAbbreviation(fromAbbreviation);
    toRef.setAbbreviation(toAbbreviation);

    mapset.setFromTerminology(fromRef.getAbbreviation());
    mapset.setFromPublisher(fromRef.getPublisher());
    mapset.setFromVersion(fromRef.getVersion());
    mapset.getAttributes().put("fhirSourceUri", fromRef.getUri());

    mapset.setToTerminology(toRef.getAbbreviation());
    mapset.setToPublisher(toRef.getPublisher());
    mapset.setToVersion(toRef.getVersion());
    mapset.getAttributes().put("fhirTargetUri", toRef.getUri());

    // Use the identifier as the code, otherwise use the id
    // NOTE: termhub generated files will have a single id
    // with a system matching this value.
    if ("https://terminologyhub.com/model/mapset/code"
        .equals(root.path("identifier").get(0).path("system").asText())) {
      mapset.setCode(root.path("identifier").get(0).path("value").asText());
    }
    if (StringUtility.isEmpty(mapset.getCode())) {
      mapset.setCode(mapset.getId());
    }

    // Store the original URIs in attributes
    mapset.setUri(root.path("url").asText());

    // LOGGER.info(" terminology URIs: source={}, target={}",
    // mapset.getFromTerminology(),
    // mapset.getToTerminology());

    // LOGGER.info("ConceptMapLoaderUtil: mapset: {}", mapset);
    service.add(Mapset.class, mapset);
    return mapset;
  }

  /**
   * Gets the root without groups.
   *
   * @param file the file
   * @return the root without groups
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "resource", "unused"
  })
  private static JsonNode getRootWithoutGroups(final File file) throws Exception {

    // Use try-with-resources to ensure the parser is closed
    try (JsonParser parser = ThreadLocalMapper.get().getFactory().createParser(file)) {

      // The first token should be the start of the root object: {
      if (parser.nextToken() != JsonToken.START_OBJECT) {
        throw new IOException("Expected start of object at the root of the file.");
      }

      // Create a node to build the new JSON tree, skipping the concepts array
      final JsonNode newRoot = ThreadLocalMapper.get().createObjectNode();

      while (parser.nextToken() != JsonToken.END_OBJECT) {
        final String fieldName = parser.currentName();

        // Advance the parser past the field name
        parser.nextToken();

        if ("group".equals(fieldName)) {
          // We found the large array. The next token should be START_ARRAY.
          // The skipChildren() method is the key to low memory usage.
          if (parser.currentToken() == JsonToken.START_ARRAY) {
            parser.skipChildren();
          }
          // Do nothing with the skipped data; it's just gone.
        } else {
          // For all other fields, read the entire value as a JsonNode
          // and add it to our new root object.
          final JsonNode node = parser.readValueAsTree();
          ((ObjectNode) newRoot).set(fieldName, node);
        }
      }

      return newRoot;
    }
  }
}
