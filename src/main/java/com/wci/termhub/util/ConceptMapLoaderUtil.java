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

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.Mapping;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.MapsetRef;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.TerminologyRef;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * Utility class for loading ConceptMap format JSON files.
 */
public final class ConceptMapLoaderUtil {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConceptMapLoaderUtil.class);

  /**
   * Instantiates a new concept map loader.
   */
  private ConceptMapLoaderUtil() {
    // n/a
  }

  /**
   * Load concept map from a JSON file and save it using the repository service.
   *
   * @param service the repository service to use for saving
   * @param conceptMap the concept map
   * @return the mapset
   * @throws Exception if there is an error reading or processing the file
   */
  public static Mapset loadConceptMap(final EntityRepositoryService service,
    final String conceptMap) throws Exception {

    return indexConceptMap(service, conceptMap, 1000, -1);

  }

  /**
   * Index concept map.
   *
   * @param service the service
   * @param conceptMap the concept map
   * @param batchSize the batch size
   * @param limit the limit
   * @return the mapset
   * @throws Exception the exception
   */
  private static Mapset indexConceptMap(final EntityRepositoryService service,
    final String conceptMap, final int batchSize, final int limit) throws Exception {

    LOGGER.debug("  batch size: {}, limit: {}", batchSize, limit);
    final long startTime = System.currentTimeMillis();

    try {

      // Read the entire file as a JSON object
      final JsonNode root = ThreadLocalMapper.get().readTree(conceptMap);

      Mapset mapset = getMapset(service, root);
      if (mapset != null) {
        throw new Exception("Can not create multiple ConceptMap resources with ConceptMap from "
            + mapset.getFromTerminology() + " to " + mapset.getToTerminology()
            + ", already have one with resource ID: ConceptMap/" + mapset.getId());
      }

      mapset = createMapset(service, root);

      final int mapsetCount = 0;
      final int mappingCount = 0;

      int ct = 0;
      // process concept map array
      final JsonNode groupArray = root.path("group");
      for (final JsonNode groupNode : groupArray) {

        for (final JsonNode elementNode : groupNode.path("element")) {
          final String sourceCode = elementNode.get("code").asText();
          final String sourceName = elementNode.get("display").asText();

          // Set source concept
          final ConceptRef fromConcept = new ConceptRef();
          fromConcept.setCode(sourceCode);
          fromConcept.setName(sourceName);
          fromConcept.setTerminology(mapset.getFromTerminology());
          fromConcept.setPublisher(mapset.getFromPublisher());
          fromConcept.setVersion(mapset.getFromVersion());

          for (final JsonNode targetNode : elementNode.path("target")) {

            // Create mapping
            final Mapping mapping = new Mapping();
            mapping.setId(UUID.randomUUID().toString());
            mapping.setMapset(new MapsetRef(mapset));

            mapping.setFrom(fromConcept);

            // Set target concept
            final ConceptRef toConcept = new ConceptRef();
            if (targetNode.has("code")) {
              toConcept.setCode(targetNode.get("code").asText());
            }
            toConcept.setName(targetNode.get("display").asText());
            toConcept.setTerminology(mapset.getToTerminology());
            toConcept.setPublisher(mapset.getToPublisher());
            toConcept.setVersion(mapset.getToVersion());
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
            if (++ct % 5000 == 0) {
              LOGGER.info("  count: {}", ct);
            }
            // Too much info
            LOGGER.debug("    Created mapping: {}", mapping);
          }
        }
      }

      LOGGER.info("  final counts - mapsets: {}, mappings: {}", mapsetCount, mappingCount);
      LOGGER.info("  duration: {} ms", (System.currentTimeMillis() - startTime));

      return mapset;

    } catch (final Exception e) {
      LOGGER.error("Error loading concept map: {}", conceptMap, e);
      throw e;
    }
  }

  /**
   * Gets the terminology.
   *
   * @param service the service
   * @param root the root
   * @return the terminology
   * @throws Exception the exception
   */
  private static Mapset getMapset(final EntityRepositoryService service, final JsonNode root)
    throws Exception {

    final String abbreviation = root.path("title").asText();
    final String publisher = root.path("publisher").asText();
    final String version = root.path("version").asText();

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
      throw new IllegalArgumentException("Invalid resource type - expected ConceptMap");
    }

    final Mapset mapset = new Mapset();

    // Convert to internal model
    final String id = root.path("id").asText();
    if (isNotBlank(id)) {
      mapset.setId(id);
    } else {
      final String uuid = UUID.randomUUID().toString();
      mapset.setId(uuid);
      LOGGER.warn("Missing ID in root node, generating new UUID for terminology as {}", uuid);
    }

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
    } else if (root.has("group") && (root.get("group").isArray())) {
      fromTerminology = root.path("group").get(0).path("source").asText();
    }
    if (fromTerminology == null) {
      throw new Exception("Unable to determine information about the map source");
    }
    final TerminologyRef fromRef = TerminologyUtility.getTerminology(service, fromTerminology);
    mapset.setFromTerminology(fromRef.getAbbreviation());
    mapset.setFromPublisher(fromRef.getPublisher());
    mapset.setFromVersion(fromRef.getVersion());

    String toTerminology = null;
    if (root.has("targetScopeUri")) {
      toTerminology = root.path("targetScopeUri").asText().replaceFirst("\\?fhir_vs$", "");
    } else if (root.has("group") && (root.get("group").isArray())) {
      toTerminology = root.path("group").get(0).path("target").asText();
    }
    if (toTerminology == null) {
      throw new Exception("Unable to determine information about the map target");
    }
    final TerminologyRef toRef = TerminologyUtility.getTerminology(service, toTerminology);
    mapset.setToTerminology(toRef.getAbbreviation());
    mapset.setToPublisher(toRef.getPublisher());
    mapset.setToVersion(toRef.getVersion());

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
    mapset.getAttributes().put("fhirUri", root.path("url").asText());
    mapset.getAttributes().put("sourceScopeUri", fromTerminology);
    mapset.getAttributes().put("targetScopeUri", toTerminology);

    // LOGGER.info(" terminology URIs: source={}, target={}", mapset.getFromTerminology(),
    // mapset.getToTerminology());

    // LOGGER.info("ConceptMapLoaderUtil: mapset: {}", mapset);
    service.add(Mapset.class, mapset);
    return mapset;
  }
}
