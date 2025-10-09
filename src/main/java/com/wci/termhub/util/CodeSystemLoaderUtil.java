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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wci.termhub.Application;
import com.wci.termhub.algo.ProgressEvent;
import com.wci.termhub.algo.ProgressListener;
import com.wci.termhub.algo.TerminologyCache;
import com.wci.termhub.algo.TreePositionAlgorithm;
import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.fhir.rest.r5.FhirUtilityR5;
import com.wci.termhub.lucene.LuceneDataAccess;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.MetaModel;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Utility class for loading CodeSystem format JSON files.
 */
public final class CodeSystemLoaderUtil {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CodeSystemLoaderUtil.class);

  /** The id map. Used by QA to map from test files to internal ids. */
  private static Map<String, String> idMap =
      new LinkedHashMap<String, String>(101 * 4 / 3, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<String, String> eldest) {
          return size() > 100;
        }
      };

  /** The Constant BATCH_SIZE. */
  private static final int DEFAULT_BATCH_SIZE = 10000;

  /**
   * Instantiates a new code system loader util.
   */
  private CodeSystemLoaderUtil() {
    // Prevent instantiation
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
   * Load concepts from CodeSystem format JSON.
   *
   * @param <T> the generic type
   * @param service the service
   * @param file the file
   * @param computeTreePositions whether to compute tree positions
   * @param type the type
   * @param listener the listener
   * @return the string
   * @throws Exception the exception
   */
  public static <T> T loadCodeSystem(final EntityRepositoryService service, final File file,
    final boolean computeTreePositions, final Class<T> type, final ProgressListener listener)
    throws Exception {

    return loadCodeSystem(service, file, computeTreePositions, type, listener, null);
  }

  /**
   * Load concepts from CodeSystem format JSON.
   *
   * @param <T>                  the generic type
   * @param service              the service
   * @param file                 the file
   * @param computeTreePositions whether to compute tree positions
   * @param type                 the type
   * @param listener             the listener
   * @param isSyndicated         the is syndicated
   * @return the string
   * @throws Exception the exception
   */
  public static <T> T loadCodeSystem(final EntityRepositoryService service, final File file,
      final boolean computeTreePositions, final Class<T> type, final ProgressListener listener,
      final Boolean isSyndicated) throws Exception {

    Application.logMemory();
    return indexCodeSystem(service, file, computeTreePositions, type, listener, isSyndicated);
  }

  /**
   * Index concepts from CodeSystem format JSON.
   *
   * @param <T> the generic type
   * @param service the service
   * @param file the file
   * @param computeTreePositions whether to compute tree positions
   * @param type the type
   * @param listener the listener
   * @param isSyndicated         the is syndicated
   * @return the string
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private static <T> T indexCodeSystem(final EntityRepositoryService service, final File file,
      final boolean computeTreePositions, final Class<T> type, final ProgressListener listener,
      final Boolean isSyndicated) throws Exception {

    final long startTime = System.currentTimeMillis();
    final List<Concept> conceptBatch = new ArrayList<>(DEFAULT_BATCH_SIZE);
    final List<ConceptRelationship> relationshipBatch = new ArrayList<>(DEFAULT_BATCH_SIZE);
    final List<Term> termBatch = new ArrayList<>(DEFAULT_BATCH_SIZE);
    final TerminologyCache terminologyCache = new TerminologyCache();

    final JsonNode jsonContent = getRootWithoutConcepts(file);

    try {

      // Set listener to 0%
      listener.updateProgress(new ProgressEvent(0));
      final int progressDenominator = computeTreePositions ? 50 : 100;
      final float progressScale = 100.0f * progressDenominator / 100.0f;

      LOGGER.info("Indexing CodeSystem {} = {}", jsonContent.path("title"),
          jsonContent.path("url"));
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("  batch size: {}, limit: {}", DEFAULT_BATCH_SIZE);
      }

      // Basic checks
      // Validate required fields
      if (jsonContent.get("url").isMissingNode()) {
        throw FhirUtilityR4.exception("CodeSystem.url is required", IssueType.INVALID,
            HttpServletResponse.SC_BAD_REQUEST);
      }

      // check for existing
      final String abbreviation = jsonContent.path("title").asText();
      final String publisher = jsonContent.path("publisher").asText();
      final String version = jsonContent.path("version").asText();
      Terminology terminology = findTerminology(service, abbreviation, publisher, version);

      if (terminology != null) {
        throw FhirUtilityR4.exception(
            "Can not create multiple CodeSystem resources the same title, publisher,"
                + " and version. duplicate = " + terminology.getId(),
            IssueType.INVALID, HttpServletResponse.SC_CONFLICT);
      }

      // Create the terminology
      terminology = createTerminology(service, jsonContent, computeTreePositions);

      // Extract metadata from root
      final List<Metadata> metadataList = createMetadata(terminology, jsonContent);
      if (metadataList != null && !metadataList.isEmpty()) {
        for (final Metadata metadata : metadataList) {
          service.add(Metadata.class, metadata);
        }
      }

      if (metadataList == null) {
        throw FhirUtilityR4.exception("Unexpected null metadata list", IssueType.INVALID,
            HttpServletResponse.SC_EXPECTATION_FAILED);
      }

      int conceptCount = 0;
      int relationshipCount = 0;
      int termCount = 0;
      LOGGER.info("  initialize terminology cache");
      try (JsonParser parser = ThreadLocalMapper.get().getFactory().createParser(file)) {

        // You can iterate through the tokens one by one
        while (parser.nextToken() != null) {

          // Get the current token
          final JsonToken token = parser.currentToken();

          // Example: Find the start of a nested JSON array
          if (token == JsonToken.START_ARRAY && "concept".equals(parser.currentName())) {

            // Process each object within the array without loading the whole array into memory
            while (parser.nextToken() != JsonToken.END_ARRAY) {

              // Use ObjectMapper to read the sub-object as a JsonNode
              final JsonNode conceptNode = parser.readValueAsTree();
              conceptCount++;
              cacheConcept(conceptNode, terminologyCache);

            }
          }
        }
      }

      final int totalConceptCount = conceptCount;
      conceptCount = 0;

      LOGGER.info("  index concepts");
      try (JsonParser parser = ThreadLocalMapper.get().getFactory().createParser(file)) {

        // You can iterate through the tokens one by one
        while (parser.nextToken() != null) {

          // Get the current token
          final JsonToken token = parser.currentToken();

          // Example: Find the start of a nested JSON array
          if (token == JsonToken.START_ARRAY && "concept".equals(parser.currentName())) {

            // Process each object within the array without loading the whole array into memory
            while (parser.nextToken() != JsonToken.END_ARRAY) {

              // Use ObjectMapper to read the sub-object as a JsonNode
              final JsonNode conceptNode = parser.readValueAsTree();

              final Concept concept = createConcept(conceptNode, terminology, terminologyCache);
              computeParentsChildren(concept, terminologyCache);
              final JsonNode relationships = conceptNode.path("property");
              final List<ConceptRelationship> conceptRelationships =
                  createRelationships(relationships, concept, terminology, terminologyCache);
              relationshipBatch.addAll(conceptRelationships);
              relationshipCount++;

              // we should not duplicate the data
              // concept.getRelationships().addAll(conceptRelationships);

              if (relationshipBatch.size() == DEFAULT_BATCH_SIZE) {
                service.addBulk(ConceptRelationship.class, new ArrayList<>(relationshipBatch));
                relationshipBatch.clear();
                LOGGER.info("  relationships count: {}", relationshipCount);
              }

              // Process terms
              for (final Term term : concept.getTerms()) {
                termBatch.add(term);
                termCount++;
                if (termBatch.size() == DEFAULT_BATCH_SIZE) {
                  service.addBulk(Term.class, new ArrayList<>(termBatch));
                  termBatch.clear();
                  LOGGER.info("  terms count: {}", termCount);
                }
              }

              // this should never happen
              if ((concept.getCode() == null) || (concept.getName() == null)) {
                throw FhirUtilityR4.exception("Concept code is unexpectedly null = " + concept,
                    IssueType.INVALID, HttpServletResponse.SC_EXPECTATION_FAILED);

              }

              // Add concept to batch
              conceptBatch.add(concept);
              conceptCount++;
              if (conceptBatch.size() == DEFAULT_BATCH_SIZE) {
                service.addBulk(Concept.class, new ArrayList<>(conceptBatch));
                conceptBatch.clear();
                LOGGER.info("  concept count: {}", conceptCount);
                Application.logMemory();

                // Progress update
                listener.updateProgress(new ProgressEvent(
                    (int) ((conceptCount * 1.0 / totalConceptCount) * progressScale)));
              }
            }
          }
        }
      } // end concepts loop

      if (!relationshipBatch.isEmpty()) {
        service.addBulk(ConceptRelationship.class, new ArrayList<>(relationshipBatch));
        LOGGER.info("  relationships count: {}", relationshipCount);
      }
      if (!termBatch.isEmpty()) {
        service.addBulk(Term.class, new ArrayList<>(termBatch));
        LOGGER.info("  terms count: {}", termCount);
      }
      if (!conceptBatch.isEmpty()) {
        service.addBulk(Concept.class, new ArrayList<>(conceptBatch));
        LOGGER.info("  concept count: {}", conceptCount);
      }

      // Progress update
      listener.updateProgress(
          new ProgressEvent((int) ((conceptCount * 1.0 / totalConceptCount) * progressScale)));
      LOGGER.info("  final counts - concepts: {}, relationships: {}, terms: {}, time: {}",
          conceptCount, relationshipCount, termCount, (System.currentTimeMillis() - startTime));

      // Free up some memory
      relationshipBatch.clear();
      termBatch.clear();

      // compute tree positions if required
      if (computeTreePositions) {

        // Set listener to 51%
        listener.updateProgress(new ProgressEvent(51));

        final ProgressListener listener2 = new ProgressListener() {

          /**
           * Update progress.
           *
           * @param event the event
           */
          @Override
          public void updateProgress(final ProgressEvent event) {
            // Take the event progress and rescale it for here
            int progress = 50 + (int) (event.getProgress() * progressScale);
            listener.updateProgress(new ProgressEvent(progress));
          }
        };

        computeConceptTreePositions(service, terminology, listener2);
      }
      LOGGER.info("  duration: {} ms", (System.currentTimeMillis() - startTime));
      Application.logMemory();

      // Get the terminology again because the tree position computer would've changed it
      terminology = service.get(terminology.getId(), Terminology.class, false);
      // Set loaded to true and save it again
      terminology.getAttributes().put("loaded", "true");
      if (isSyndicated != null && isSyndicated) {
        terminology.getAttributes().put("syndicated", "true");
      }
      service.update(Terminology.class, terminology.getId(), terminology);

      // Set listener to 100%
      listener.updateProgress(new ProgressEvent(100));

      // R4
      if (type == org.hl7.fhir.r4.model.CodeSystem.class) {
        return (T) FhirUtilityR4.toR4(terminology);
      }
      // else R5
      return (T) FhirUtilityR5.toR5(terminology);

    } catch (final Exception e) {
      LOGGER.error("Error indexing code system.", e);
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
  private static Terminology findTerminology(final EntityRepositoryService service,
    final String abbreviation, final String publisher, final String version) throws Exception {

    String versionToLookup = version;
    // For SNOMED, set the terminology version to just the base version at the
    // end of the URL
    if (abbreviation.contains("SNOMED") && version.contains("/")) {
      versionToLookup = version.replaceFirst(".*/", "");
    }

    final SearchParameters searchParams = new SearchParameters();
    searchParams.setQuery(
        TerminologyUtility.getTerminologyAbbrQuery(abbreviation, publisher, versionToLookup));
    final ResultList<Terminology> terminology = service.find(searchParams, Terminology.class);

    return (terminology.getItems().isEmpty()) ? null : terminology.getItems().get(0);
  }

  /**
   * Creates the terminology.
   *
   * @param service the service
   * @param root the root
   * @param computeTreePositions the compute tree positions
   * @return the terminology
   * @throws Exception the exception
   */
  private static Terminology createTerminology(final EntityRepositoryService service,
    final JsonNode root, final boolean computeTreePositions) throws Exception {

    // Validate that this is a CodeSystem resource
    if (!root.has("resourceType") || !"CodeSystem".equals(root.get("resourceType").asText())) {
      throw FhirUtilityR4.exception("Invalid resource type - expected CodeSystem",
          IssueType.INVALID, HttpServletResponse.SC_EXPECTATION_FAILED);
    }

    final Terminology terminology = new Terminology();
    final Map<String, String> attributes = new HashMap<>();

    // The HAPI Plan server @Create method blanks the identifier on sending a
    // code system in. Always create a new identifier.
    terminology.setId(UUID.randomUUID().toString());
    attributes.put("loaded", "false");

    // Set "originalId" if provided
    final String originalId = root.path("id").asText();
    if (isNotBlank(originalId)) {
      attributes.put("originalId", originalId);
      idMap.put(originalId, terminology.getId());
    }

    terminology.setActive(true);
    terminology.setUri(root.path("url").asText());
    terminology.setName(root.path("name").asText());
    terminology.setAbbreviation(root.path("title").asText());
    terminology.setPublisher(root.path("publisher").asText());
    terminology.setVersion(root.path("version").asText());
    // For SNOMED, set the terminology version to just the base version at the
    // end of the URL
    if (terminology.getUri().contains("snomed") && terminology.getVersion().contains("/")) {
      terminology.setVersion(terminology.getVersion().replaceFirst(".*/", ""));
    }
    // Store the full date string with timezone information
    final String fullDateString = root.path("date").asText();
    terminology.setReleaseDate(fullDateString);
    terminology.setFamily(terminology.getAbbreviation());
    terminology.setConceptCt(root.path("count").asLong(0));

    // Set terminology attributes
    final JsonNode properties = root.path("property");
    for (final JsonNode property : properties) {
      final String uri = property.path("uri").asText();
      // [ https:, , terminologyhub.com, model, terminology, attributes,
      // autocomplete ]
      if (uri == null || uri.isEmpty() || !uri.contains("terminology/attributes")) {
        continue;
      }
      attributes.put(property.path("code").asText(), property.path("description").asText());
    }
    attributes.put("fhirVersion", root.path("version").asText());
    attributes.put(Terminology.Attributes.treePositions.property(),
        Boolean.toString(computeTreePositions));
    terminology.setAttributes(attributes);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("CodeSystemLoaderUtil: terminology: {}", terminology);
    }
    service.add(Terminology.class, terminology);

    LOGGER.info("Created terminology: {}, publisher: {}, version: {}, id: {}",
        terminology.getAbbreviation(), terminology.getPublisher(), terminology.getVersion(),
        terminology.getId());
    LuceneDataAccess.clearReaderForClass(Terminology.class);
    return terminology;
  }

  /**
   * Creates the metadata.
   *
   * @param terminology the terminology
   * @param root the root
   * @return the metadata
   */
  private static List<Metadata> createMetadata(final Terminology terminology, final JsonNode root) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Create metadata");
    }

    final List<Metadata> metadataList = new ArrayList<>();

    final JsonNode properties = root.path("property");
    for (final JsonNode property : properties) {

      // {"code":"morphologic abnormality",
      // "uri":"https://terminologyhub.com/model/concept/semanticType/
      // MorphologicAbnormality","type":"string"}

      final String uri = property.path("uri").asText();
      final String[] uriParts = FieldedStringTokenizer.split(uri, "/");

      // e.g. modelType is "concept"
      final String modelType = uriParts[uriParts.length - 3];

      // Avoid creating metadata for "terminology" things
      if ("terminology".equals(modelType)) {
        continue;
      }

      final String code = property.path("code").asText();
      final String description = property.path("description").asText();
      final String fieldType = uriParts[uriParts.length - 2];

      try {

        final Metadata metadata = new Metadata();
        metadata.setId(UUID.randomUUID().toString());
        metadata.setCode(code);
        metadata.setName(description);
        metadata.setActive(true);
        metadata.setPublisher(terminology.getPublisher());
        metadata.setTerminology(terminology.getAbbreviation());
        metadata.setVersion(terminology.getVersion());
        metadata.setModel(MetaModel.Model.valueOf(modelType));
        metadata.setField(MetaModel.Field.valueOf(fieldType));
        metadataList.add(metadata);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("    ADD metadata = {}", metadata.toString());
        }

      } catch (final IllegalArgumentException e) {
        LOGGER.warn(
            "Skipping metadata due to invalid model or field type: model={}, field={}, code={}, name={}",
            modelType, fieldType, code, description);
      }
    }

    return metadataList;
  }

  /**
   * Creates the concept.
   *
   * @param conceptNode the concept node
   * @param terminology the terminology
   * @param cache the cache
   * @return the concept
   * @throws Exception the exception
   */
  private static Concept createConcept(final JsonNode conceptNode, final Terminology terminology,
    final TerminologyCache cache) throws Exception {

    final Concept concept = new Concept();

    final String id = UUID.randomUUID().toString();
    if (conceptNode.has("id")) {
      concept.getAttributes().put("originalId", conceptNode.path("id").asText());
    }

    concept.setId(id);
    concept.setCode(conceptNode.path("code").asText());
    concept.setTerminology(terminology.getAbbreviation());
    concept.setVersion(terminology.getVersion());
    concept.setPublisher(terminology.getPublisher());
    concept.setActive(cache.isActive(concept.getCode()));
    concept.setDefined(cache.getDefined(concept.getCode()));
    concept.setLeaf(cache.isLeaf(concept.getCode()));
    concept.setName(cache.getConceptName(concept.getCode()));
    concept.setNormName(StringUtility.normalize(concept.getName()));
    concept.setStemName(StringUtility.normalizeWithStemming(concept.getName()));

    // Set defined status - default to true, will be updated based on
    // definitionStatusId
    concept.setDefined(true);

    // Set concept ID if different from code
    final String conceptId = conceptNode.path("conceptId").asText(null);
    if (conceptId != null && !conceptId.equals(concept.getCode())) {
      concept.getAttributes().put("conceptId", conceptId);
    }

    final List<Term> terms = createTerms(terminology, concept, conceptNode);
    concept.getTerms().addAll(terms);

    // Process common properties
    final JsonNode properties = conceptNode.path("property");
    for (final JsonNode property : properties) {
      final String code = property.path("code").asText();
      String value = null;

      if (property.has("valueString")) {
        value = property.path("valueString").asText();
      } else if (property.has("valueBoolean")) {
        value = property.path("valueBoolean").asText();
      }

      // Redirect semantic type
      if ("semanticType".equals(code)) {
        concept.getSemanticTypes().add(value);
      }
      // redirect active
      else if ("active".equals(code)) {
        // already handled by cacheConcept
        continue;
        // concept.setActive(Boolean.valueOf(value));
      }
      // redirect defined
      else if ("sufficientlyDefined".equals(code)) {
        // already handled by cacheConcept
        continue;
        // concept.setDefined(Boolean.valueOf(value));
      }
      // Hack to handle defined if not set for SNOMED
      else if ("definitionStatusId".equals(code)) {
        // Set defined status based on definitionStatusId
        // 900000000000073002 = Defined (true), 900000000000074008 = Primitive
        // (false)
        concept.setDefined("900000000000073002".equals(value));
        concept.getAttributes().put(code, value);
      }
      // anything other than "parent" is just a property
      else if (!"parent".equals(code)) {
        concept.getAttributes().put(code, value);
      }

    }

    return concept;
  }

  /**
   * Compute parents children.
   *
   * @param concept the concept
   * @param cache the terminology cache
   * @throws Exception the exception
   */
  private static void computeParentsChildren(final Concept concept, final TerminologyCache cache)
    throws Exception {

    if (cache.getParents(concept.getCode()) != null) {
      final Set<String> parents = cache.getParents(concept.getCode());

      for (final String parentCode : parents) {
        final ConceptRef parent = new ConceptRef();
        parent.setCode(parentCode);
        parent.setName(cache.getConceptName(parentCode));
        parent.setPublisher(concept.getPublisher());
        parent.setTerminology(concept.getTerminology());
        parent.setVersion(concept.getVersion());
        parent.setDefined(cache.getDefined(parentCode));
        parent.setLeaf(cache.isLeaf(parentCode));
        concept.getParents().add(parent);
      }
    }
    if (cache.getChildren(concept.getCode()) != null) {

      for (final String childCode : cache.getChildren(concept.getCode())) {
        final ConceptRef child = new ConceptRef();
        child.setCode(childCode);
        child.setName(cache.getConceptName(childCode));
        child.setPublisher(concept.getPublisher());
        child.setTerminology(concept.getTerminology());
        child.setVersion(concept.getVersion());
        child.setDefined(cache.getDefined(childCode));
        child.setLeaf(cache.isLeaf(childCode));
        concept.getChildren().add(child);
      }
    }
    concept.setLeaf(cache.isLeaf(concept.getCode()));

    if (cache.getAncestors(concept.getCode()) != null) {
      for (final Map.Entry<String, Integer> ancestorCode : cache
          .getAncestorsWithDepth(concept.getCode()).entrySet()) {
        final ConceptRef ancestor = new ConceptRef();
        ancestor.setLocal(null);
        ancestor.setCode(ancestorCode.getKey());
        ancestor.setName(cache.getConceptName(ancestorCode.getKey()));
        // TBD: Do these need to be set here?
        // ancestor.setPublisher(concept.getPublisher());
        // ancestor.setTerminology(concept.getTerminology());
        // ancestor.setVersion(concept.getVersion());
        ancestor.setDefined(cache.getDefined(ancestorCode.getKey()));
        ancestor.setLeaf(cache.isLeaf(ancestorCode.getKey()));
        ancestor.setLevel(ancestorCode.getValue());
        concept.getAncestors().add(ancestor);
      }
      // Sort by level+name
      concept.setAncestors(concept.getAncestors().stream().sorted((a, b) -> {
        final int l1 = 100 + a.getLevel();
        final String k1 = l1 + a.getName();
        final int l2 = 100 + b.getLevel();
        final String k2 = l2 + b.getName();
        return k1.compareTo(k2);
      }).collect(Collectors.toList()));

    }

    final Map<String, ConceptRef> descendants = cache.getDescendantsWithDepth(concept.getCode());

    for (final Map.Entry<String, ConceptRef> entry : descendants.entrySet().stream()
        .sorted((a, b) -> {
          final ConceptRef aa = a.getValue();
          final int l1 = 100 + aa.getLevel();
          final String k1 = l1 + aa.getName();
          final ConceptRef bb = b.getValue();
          final int l2 = 100 + bb.getLevel();
          final String k2 = l2 + bb.getName();
          return k1.compareTo(k2);
        }).collect(Collectors.toList())) {
      final ConceptRef descendant = entry.getValue();
      // TBD: Do these need to be set here?
      // descendant.setPublisher(concept.getPublisher());
      // descendant.setTerminology(concept.getTerminology());
      // descendant.setVersion(concept.getVersion());
      concept.getDescendants().add(descendant);
    }
  }

  /**
   * Cache concept.
   *
   * @param conceptNode the concept node
   * @param cache the cache
   * @throws Exception the exception
   */
  private static void cacheConcept(final JsonNode conceptNode, final TerminologyCache cache)
    throws Exception {

    // active/defined/name/par/chd

    final Concept concept = new Concept();
    concept.setCode(conceptNode.path("code").asText());
    concept.setName(conceptNode.path("display").asText());
    // Process common properties
    final JsonNode properties = conceptNode.path("property");
    for (final JsonNode property : properties) {
      final String code = property.path("code").asText();
      String value = null;

      if (property.has("valueString")) {
        value = property.path("valueString").asText();
      } else if (property.has("valueBoolean")) {
        value = property.path("valueBoolean").asText();
      }

      // redirect active
      if ("active".equals(code)) {
        concept.setActive(Boolean.valueOf(value));
      }
      // redirect defined
      else if ("sufficientlyDefined".equals(code)) {
        concept.setDefined(Boolean.valueOf(value));
      }
      // Need hack for defined for now
      else if ("definitionStatusId".equals(code)) {
        // Set defined status based on definitionStatusId
        // 900000000000073002 = Defined (true), 900000000000074008 = Primitive
        // (false)
        concept.setDefined("900000000000073002".equals(value));
        concept.getAttributes().put(code, value);
      }
      // Cache parent/child relationship here
      else if ("parent".equals(code)) {
        if (property.has("valueCoding")) {
          cache.addParChd(property.get("valueCoding").get("code").asText(),
              conceptNode.path("code").asText());
        }
      }
    }
    cache.addConcept(concept);
  }

  /**
   * Creates the relationships.
   *
   * @param relationshipArrayNode the relationship array node
   * @param concept the concept
   * @param terminology the terminology
   * @param terminologyCache the terminology cache
   * @return the list
   * @throws Exception the exception
   */
  private static List<ConceptRelationship> createRelationships(final JsonNode relationshipArrayNode,
    final Concept concept, final Terminology terminology, final TerminologyCache terminologyCache)
    throws Exception {

    final List<ConceptRelationship> relationships = new ArrayList<>();

    // Process relationships
    for (final JsonNode propertyNode : relationshipArrayNode) {

      final String propertyType =
          propertyNode.has("code") ? propertyNode.path("code").asText() : "";

      if (!Arrays.asList("parent", "relationship").contains(propertyType)) {
        continue;
      }

      if ("parent".equals(propertyType)) {
        final ConceptRelationship relationship =
            createRelationship(propertyNode, concept, terminology, terminologyCache);
        // Track parent-child relationship using thread-safe collections
        final JsonNode valueCoding = propertyNode.path("valueCoding");
        if (!valueCoding.isMissingNode() && valueCoding.has("code")) {
          final String parentCode = valueCoding.path("code").asText();
          terminologyCache.addParChd(parentCode, concept.getCode());
        }

        // ECL clauses removed per requirements

        relationships.add(relationship);
      }

      if ("relationship".equals(propertyType)) {
        final ConceptRelationship relationship = createRelationship(propertyNode, concept, terminology,
            terminologyCache);

        // ECL clauses removed per requirements

        relationships.add(relationship);
      }
    }
    return relationships;
  }

  /**
   * Creates the terms.
   *
   * @param terminology the terminology
   * @param concept the concept
   * @param conceptNode the concept node
   * @return the list
   */
  private static List<Term> createTerms(final Terminology terminology, final Concept concept,
    final JsonNode conceptNode) {

    final List<Term> terms = new ArrayList<>();
    final JsonNode designations = conceptNode.path("designation");

    for (final JsonNode designation : designations) {
      final Term term = createBaseTerm(terminology, concept, designation, conceptNode);
      final PreferredTermConfig ptConfig =
          PreferredTermConfig.findByAbbreviation(terminology.getAbbreviation());

      if (ptConfig != null) {
        final String language = designation.path("language").asText();
        final boolean isPriority = ptConfig.getPreferredTermCode().equals(term.getType());
        term.getLocaleMap().put(language, isPriority);

        // Use preferred term designation as concept name
        if (ptConfig.getPreferredTermCode().equals(term.getType())) {
          if (concept.getName() == null || "SNOMEDCT".equals(ptConfig.getAbbreviation())
              || "SNOMEDCT_US".equals(ptConfig.getAbbreviation())) {
            concept.setName(term.getName());
          }
        }
      }

      terms.add(term);
    }

    return terms;
  }

  /**
   * Creates the base term with common properties.
   *
   * @param terminology the terminology
   * @param concept the concept
   * @param designation the designation
   * @param conceptNode the concept node
   * @return the term
   */
  private static Term createBaseTerm(final Terminology terminology, final Concept concept,
    final JsonNode designation, final JsonNode conceptNode) {

    final Term term = new Term();
    term.setId(UUID.randomUUID().toString());
    term.setActive(true);
    term.setName(designation.path("value").asText());
    term.setNormName(StringUtility.normalize(term.getName()));
    term.setStemName(StringUtility.normalizeWithStemming(term.getName()));

    // Safely set term type with null checks
    if (designation.has("use") && designation.path("use").has("code")) {
      term.setType(designation.path("use").path("code").asText());
    } else {
      // Default to PT (Preferred Term) if no type is specified
      term.setType("PT");
      LOGGER.warn("Missing term type for designation, defaulting to PT for concept: {}",
          concept.getCode());
    }

    term.setTerminology(terminology.getAbbreviation());
    term.setVersion(terminology.getVersion());
    term.setPublisher(terminology.getPublisher());
    term.setCode(concept.getCode());
    term.setConceptId(concept.getCode());

    final String language = designation.path("language").asText();
    term.getLocaleMap().put(language, false);

    // Set component ID if available
    final String componentId = designation.path("id").asText(null);
    if (componentId != null) {
      term.setComponentId(componentId);
    }

    // Set term attributes from concept properties
    // Extract caseSignificanceId and moduleId from concept properties
    final JsonNode properties = conceptNode.path("property");
    for (final JsonNode property : properties) {
      final String code = property.path("code").asText();
      if ("caseSignificanceId".equals(code) && property.has("valueString")) {
        term.getAttributes().put("caseSignificanceId", property.path("valueString").asText());
      } else if ("moduleId".equals(code) && property.has("valueString")) {
        term.getAttributes().put("moduleId", property.path("valueString").asText());
      }
    }

    // Set default values if not found in concept properties
    if (!term.getAttributes().containsKey("caseSignificanceId")) {
      term.getAttributes().put("caseSignificanceId", "900000000000448009");
    }
    if (!term.getAttributes().containsKey("moduleId")) {
      term.getAttributes().put("moduleId", "900000000000207008");
    }

    return term;
  }

  /**
   * Creates a relationship (either ISA or other).
   *
   * @param relationshipNode the relationship node
   * @param fromConcept the from concept
   * @param terminology the terminology
   * @param terminologyCache the terminology cache
   * @return the concept relationship
   * @throws Exception the exception
   */
  private static ConceptRelationship createRelationship(final JsonNode relationshipNode,
    final Concept fromConcept, final Terminology terminology,
    final TerminologyCache terminologyCache) throws Exception {

    final ConceptRelationship relationship = new ConceptRelationship();
    relationship.setId(UUID.randomUUID().toString());
    relationship.setActive(true);
    relationship.setTerminology(terminology.getAbbreviation());
    relationship.setVersion(terminology.getVersion());
    relationship.setPublisher(terminology.getPublisher());

    // Get type
    final String relationshipType = relationshipNode.get("code").asText();

    relationship.setType(relationshipType);
    relationship.setHierarchical(false);

    if ("parent".equals(relationshipType)) {
      relationship.setType("isa");
      relationship.setHierarchical(true);
    }

    // Process extensions to get additionalType, group, and historical flag
    boolean isHistorical = false;
    final JsonNode extensions = relationshipNode.path("extension");
    if (!extensions.isMissingNode()) {
      for (final JsonNode extension : extensions) {
        final String url = extension.path("url").asText();
        if (url.endsWith("/additionalType")) {
          final JsonNode valueCoding = extension.path("valueCoding");
          if (!valueCoding.isMissingNode() && valueCoding.has("code")) {
            relationship.setAdditionalType(valueCoding.path("code").asText());
          } else if (!valueCoding.isMissingNode() && valueCoding.has("display")) {
            relationship.setAdditionalType(valueCoding.path("display").asText());
          }
        } else if (url.endsWith("/group")) {
          relationship.setGroup(extension.path("valueString").asText());
        } else if (url.endsWith("/historical")) {
          // Check for historical relationship extension
          final JsonNode valueBoolean = extension.path("valueBoolean");
          if (!valueBoolean.isMissingNode() && valueBoolean.asBoolean()) {
            isHistorical = true;
          }
        }
      }
    }

    final ConceptRef fromRef = terminologyCache.newConceptRef(fromConcept.getCode(),
        terminology.getPublisher(), terminology.getAbbreviation(), terminology.getVersion());
    relationship.setFrom(fromRef);

    // Set target concept reference from valueCoding
    final JsonNode valueCoding = relationshipNode.path("valueCoding");
    if (!valueCoding.isMissingNode()) {
      final ConceptRef toRef = terminologyCache.newConceptRef(valueCoding.path("code").asText(),
          terminology.getPublisher(), terminology.getAbbreviation(), terminology.getVersion());
      relationship.setTo(toRef);
    } else {
      throw FhirUtilityR4.exception("Unexpected 'parent' property without a valueCoding = "
          + fromConcept.getCode() + ", " + relationshipNode, IssueType.INVALID,
          HttpServletResponse.SC_EXPECTATION_FAILED);
    }

    // Set additional attributes
    relationship.setHistorical(isHistorical);
    relationship.setAsserted(true);
    // Set defining to null and comment out current logic per requirements
    relationship.setDefining(null);

    // Call addHistoricalRel if this is a historical relationship
    if (isHistorical && relationship.getTo() != null) {
      try {
        terminologyCache.addHistoricalRel(fromConcept.getCode(), relationship.getTo().getCode(),
            relationship.getType());
      } catch (final Exception e) {
        LOGGER.warn("Failed to add historical relationship: {}", e.getMessage());
      }
    }

    return relationship;
  }

  /**
   * Compute concept tree positions.
   *
   * @param service the service
   * @param terminology the terminology
   * @param listener the listener
   * @throws Exception the exception
   */
  private static void computeConceptTreePositions(final EntityRepositoryService service,
    final Terminology terminology, final ProgressListener listener) throws Exception {

    LOGGER.info(
        "  Computing concept tree positions for terminology: {}, publisher: {}, version: {}",
        terminology.getAbbreviation(), terminology.getPublisher(), terminology.getVersion());

    final TreePositionAlgorithm treepos = new TreePositionAlgorithm(service);
    treepos.addProgressListener(listener);
    treepos.setTerminology(terminology.getAbbreviation());
    treepos.setPublisher(terminology.getPublisher());
    treepos.setVersion(terminology.getVersion());
    treepos.checkPreconditions();
    treepos.compute();

  }

  /**
   * Gets the root without concepts.
   *
   * @param file the file
   * @return the root without concepts
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  private static JsonNode getRootWithoutConcepts(final File file) throws Exception {

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

        if ("concept".equals(fieldName)) {
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
