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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.algo.TreePositionAlgorithm;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.Definition;
import com.wci.termhub.model.HasId;
import com.wci.termhub.model.MetaModel;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * Utility class for loading CodeSystem format JSON files.
 */
public final class CodeSystemLoaderUtil {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CodeSystemLoaderUtil.class);

  /** The terminologies cache. */
  private static TimerCache<Concept> conceptCache = new TimerCache<>(1000000, 60 * 60 * 1000);

  /** The key delimiter. */
  private static final String KEY_DELIMITER = "|";

  /** The Constant BATCH_SIZE. */
  private static final int BATCH_SIZE = 1000;

  /** The object mapper. */
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * Instantiates a new code system loader util.
   */
  private CodeSystemLoaderUtil() {
    // Prevent instantiation
  }

  /**
   * Load concepts from CodeSystem format JSON.
   *
   * @param service the service
   * @param content the content
   * @param computeTreePositions whether to compute tree positions
   * @return the string
   * @throws Exception the exception
   */
  public static String loadCodeSystem(final EntityRepositoryService service, final String content,
    final boolean computeTreePositions) throws Exception {

    return indexCodeSystem(service, content, BATCH_SIZE, -1, computeTreePositions);
  }

  /**
   * Index concepts from CodeSystem format JSON.
   *
   * @param service the service
   * @param content the content
   * @param batchSize the batch size
   * @param limit the limit
   * @param computeTreePositions whether to compute tree positions
   * @return the string
   * @throws Exception the exception
   */
  private static String indexCodeSystem(final EntityRepositoryService service, final String content,
    final int batchSize, final int limit, final boolean computeTreePositions) throws Exception {

    LOGGER.debug("  batch size: {}, limit: {}", batchSize, limit);
    final long startTime = System.currentTimeMillis();
    final List<Concept> conceptBatch = new ArrayList<>(batchSize);
    final List<ConceptRelationship> relationshipBatch = new ArrayList<>(batchSize);
    final List<Term> termBatch = new ArrayList<>(batchSize);
    final String id;

    try {

      // Read the entire file as a JSON object
      final JsonNode root = OBJECT_MAPPER.readTree(content);
      Terminology terminology = getTerminology(service, root);

      if (terminology != null) {
        throw new Exception("Can not create multiple CodeSystem resources with CodeSystem.url "
            + terminology.getUri() + " and CodeSystem.version " + terminology.getVersion()
            + ", already have one with resource ID: CodeSystem/" + terminology.getId());
      }

      // Create the terminology
      terminology = createTerminology(service, root, computeTreePositions);
      id = terminology.getId();

      // Extract metadata from root
      final List<Metadata> metadataList = createMetadata(root);
      if (metadataList != null && !metadataList.isEmpty()) {
        for (final Metadata metadata : metadataList) {
          service.add(Metadata.class, metadata);
        }
      }

      if (metadataList == null) {
        throw new Exception("Unexpected null metadata list");
      }

      // Process concepts array
      final JsonNode concepts = root.path("concept");
      int conceptCount = 0;
      int relationshipCount = 0;
      int termCount = 0;

      final Map<String, Set<String>> codeToChildren = new HashMap<>();
      final Map<String, Set<String>> codeToParents = new HashMap<>();

      for (final JsonNode conceptNode : concepts) {
        if (limit != -1 && conceptCount >= limit) {
          break;
        }

        final Concept concept = createConcept(conceptNode, terminology);
        final String conceptCode = concept.getCode();

        // Process relationships
        final JsonNode relationships = conceptNode.path("property");
        for (final JsonNode propertyNode : relationships) {

          final String propertyType =
              propertyNode.has("code") ? propertyNode.path("code").asText() : "";

          if (!Arrays.asList("parent", "relationship").contains(propertyType)) {
            continue;
          }

          if ("parent".equals(propertyType)) {
            final ConceptRelationship relationship =
                createRelationship(propertyNode, concept, terminology);
            relationshipBatch.add(relationship);
            concept.getRelationships().add(relationship);

            // Track parent-child relationship
            final JsonNode valueCoding = propertyNode.path("valueCoding");
            if (!valueCoding.isMissingNode() && valueCoding.has("code")) {
              final String parentCode = valueCoding.path("code").asText();
              codeToChildren.computeIfAbsent(parentCode, k -> new HashSet<>()).add(conceptCode);
              codeToParents.computeIfAbsent(conceptCode, k -> new HashSet<>()).add(parentCode);
            }

            if (relationshipBatch.size() == batchSize) {
              service.addBulk(ConceptRelationship.class, new ArrayList<>(relationshipBatch));
              relationshipBatch.clear();
              LOGGER.info("  processed relationships count: {}", relationshipCount);
            }

            // Safely get the valueCoding and its code
            if (!valueCoding.isMissingNode() && valueCoding.has("code")) {
              concept.getEclClauses().add(
                  propertyNode.path("code").asText() + "=" + valueCoding.path("code").asText());
            } else {
              LOGGER.debug("    Missing valueCoding or code for parent relationship in concept: {}",
                  concept.getCode());
            }
            relationshipCount++;
          }

          if ("relationship".equals(propertyType)) {
            final JsonNode extensionNode = propertyNode.path("extension");
            for (final JsonNode extension : extensionNode) {
              // Add null checks for valueCoding nodes
              final JsonNode extensionValueCoding = extension.path("valueCoding");
              final JsonNode propertyValueCoding = propertyNode.path("valueCoding");

              // Only proceed if both valueCoding nodes are present and have
              // code fields
              if (!extensionValueCoding.isMissingNode() && !propertyValueCoding.isMissingNode()
                  && extensionValueCoding.has("code") && propertyValueCoding.has("code")) {
                concept.getEclClauses().add(extensionValueCoding.path("code").asText() + "="
                    + propertyValueCoding.path("code").asText());
              } else {
                LOGGER.debug(
                    "    Skipping relationship due to missing valueCoding or code for concept: {}",
                    concept.getCode());
              }
            }
          }
        }

        // Process terms
        for (final Term term : concept.getTerms()) {
          termBatch.add(term);
          if (termBatch.size() == batchSize) {
            service.addBulk(Term.class, new ArrayList<>(termBatch));
            termBatch.clear();
            LOGGER.info("  processed terms count: {}", termCount);
          }
          termCount++;
        }

        // Add concept to batch
        conceptBatch.add(concept);
        final String conceptKey = getConceptCacheKey(concept);
        conceptCache.put(conceptKey, concept);
        if (conceptBatch.size() == batchSize) {
          service.addBulk(Concept.class, new ArrayList<>(conceptBatch));
          SystemReportUtility.logMemory();
          conceptBatch.clear();
          LOGGER.info("  processed concepts count: {}", conceptCount);
        }
        conceptCount++;
      } // end concepts loop

      // Add remaining batches
      if (!conceptBatch.isEmpty()) {
        service.addBulk(Concept.class, new ArrayList<>(conceptBatch));
      }
      if (!relationshipBatch.isEmpty()) {
        service.addBulk(ConceptRelationship.class, new ArrayList<>(relationshipBatch));
      }
      if (!termBatch.isEmpty()) {
        service.addBulk(Term.class, new ArrayList<>(termBatch));
      }

      LOGGER.info("  final counts - concepts: {}, relationships: {}, terms: {}", conceptCount,
          relationshipCount, termCount);
      LOGGER.info("  begin compute for ancestors and descendants");

      // Compute ancestors and descendants for each concept
      final Map<String, HasId> batch = new HashMap<>(BATCH_SIZE);
      int computedConceptsCount = 0;

      // Build complete relationship maps
      final Map<String, Set<String>> allAncestors = new HashMap<>();
      final Map<String, Set<String>> allDescendants = new HashMap<>();

      // First pass: compute all ancestor sets in one go
      for (final String conceptCode : codeToParents.keySet()) {
        if (!allAncestors.containsKey(conceptCode)) {
          final Set<String> ancestors = new HashSet<>();
          computeAncestors(conceptCode, codeToParents, ancestors);
          allAncestors.put(conceptCode, ancestors);

          // Add this concept as a descendant to all its ancestors
          for (final String ancestor : ancestors) {
            allDescendants.computeIfAbsent(ancestor, k -> new HashSet<>()).add(conceptCode);
          }
        }
      }

      // Process concepts in batches
      final List<String> conceptCodes = new ArrayList<>(codeToParents.keySet());
      for (int i = 0; i < conceptCodes.size(); i += BATCH_SIZE) {
        final int end = Math.min(i + BATCH_SIZE, conceptCodes.size());
        final List<String> batchCodes = conceptCodes.subList(i, end);

        // Batch lookup concepts from cache
        final Map<String, Concept> cachedConcepts = new HashMap<>();
        for (final String code : batchCodes) {
          final String cacheKey =
              terminology.getPublisher() + KEY_DELIMITER + terminology.getAbbreviation()
                  + KEY_DELIMITER + terminology.getVersion() + KEY_DELIMITER + code;
          final Concept concept = conceptCache.get(cacheKey);
          if (concept != null) {
            cachedConcepts.put(code, concept);
          }
        }

        // Process each concept in the batch
        for (final String code : batchCodes) {
          final Concept concept = cachedConcepts.get(code);
          if (concept != null) {
            // Add direct parents
            final Set<String> parents = codeToParents.getOrDefault(code, new HashSet<>());
            for (final String parentCode : parents) {
              concept.getParents().add(createConceptRef(parentCode, concept));
            }

            // Add direct children
            final Set<String> children = codeToChildren.getOrDefault(code, new HashSet<>());
            for (final String childCode : children) {
              concept.getChildren().add(createConceptRef(childCode, concept));
            }

            // Add ancestors
            final Set<String> ancestors = allAncestors.getOrDefault(code, new HashSet<>());
            for (final String ancestorCode : ancestors) {
              concept.getAncestors().add(createConceptRef(ancestorCode, concept));
            }

            // Add descendants
            final Set<String> descendants = allDescendants.getOrDefault(code, new HashSet<>());
            for (final String descendantCode : descendants) {
              concept.getDescendants().add(createConceptRef(descendantCode, concept));
            }

            batch.put(concept.getId(), concept);
            computedConceptsCount++;
          }
        }

        // Batch update concepts
        if (!batch.isEmpty()) {
          service.updateBulk(Concept.class, batch);
          batch.clear();
          LOGGER.info("  computed concepts count: {}", computedConceptsCount);
        }
      }

      // compute tree positions
      if (computeTreePositions) {
        computeConceptTreePositions(service, metadataList.get(0).getTerminology(),
            metadataList.get(0).getPublisher(), metadataList.get(0).getVersion());
      }

      LOGGER.info("  duration: {} ms", (System.currentTimeMillis() - startTime));

      return id;

    } catch (final Exception e) {
      LOGGER.error("Error indexing code system.", e);
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
  private static Terminology getTerminology(final EntityRepositoryService service,
    final JsonNode root) throws Exception {

    final String abbreviation = root.path("title").asText();
    final String publisher = root.path("publisher").asText();
    final String version = root.path("version").asText();

    final SearchParameters searchParams = new SearchParameters();
    searchParams
        .setQuery(TerminologyUtility.getTerminologyAbbrQuery(abbreviation, publisher, version));
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
      throw new IllegalArgumentException("Invalid resource type - expected CodeSystem");
    }

    final Terminology terminology = new Terminology();
    final String id = root.path("id").asText();
    if (isNotBlank(id)) {
      terminology.setId(id);
    } else {
      final String uuid = UUID.randomUUID().toString();
      terminology.setId(uuid);
      LOGGER.warn("Missing ID in root node, generating new UUID for terminology as {}", uuid);
    }
    terminology.setName(root.path("name").asText());
    terminology.setAbbreviation(root.path("title").asText());
    terminology.setPublisher(root.path("publisher").asText());
    terminology.setVersion(root.path("version").asText());
    terminology.setReleaseDate(root.path("date").asText().substring(0, 10));
    terminology.setUri(root.path("url").asText());
    terminology.setFamily("SNOMED");
    terminology.setConceptCt(root.path("count").asLong(0));

    // Set terminology attributes
    final Map<String, String> attributes = new HashMap<>();
    final JsonNode properties = root.path("property");
    for (final JsonNode property : properties) {
      final String uri = property.path("uri").asText();
      final String[] uriParts = FieldedStringTokenizer.split(uri, "/");
      if (uriParts.length < 3 || !"terminology".equals(uriParts[uriParts.length - 2])
          || !"attributes".equals(uriParts[uriParts.length - 1])) {
        continue;
      }
      attributes.put(property.path("code").asText(), property.path("description").asText());
    }
    attributes.put("fhirVersion", root.path("version").asText());
    attributes.put(Terminology.Attributes.treePositions.property(),
        Boolean.toString(computeTreePositions));
    terminology.setAttributes(attributes);

    LOGGER.info("CodeSystemLoaderUtil: terminology: {}", terminology);
    service.add(Terminology.class, terminology);
    return terminology;
  }

  /**
   * Creates the metadata.
   *
   * @param root the root
   * @return the metadata
   */
  private static List<Metadata> createMetadata(final JsonNode root) {

    final List<Metadata> metadataList = new ArrayList<>();

    final String publisher = root.path("publisher").asText();
    final String terminology = root.path("title").asText();
    final String version = root.path("version").asText();

    final JsonNode properties = root.path("property");
    for (final JsonNode property : properties) {

      final String uri = property.path("uri").asText();
      final String[] uriParts = FieldedStringTokenizer.split(uri, "/");

      final String modelType = uriParts[uriParts.length - 2];

      if (uriParts.length < 3 || "terminology".equals(modelType)) {
        continue;
      }

      final String code = property.path("code").asText();
      final String description = property.path("description").asText();
      final String fieldType = uriParts[uriParts.length - 1];

      // Map 'attributes' model type to 'concept' as a fallback
      String mappedModelType = modelType;
      if ("attributes".equals(modelType)) {
        mappedModelType = "concept";
        LOGGER.info("Mapped 'attributes' model type to 'concept' for metadata: code={}, name={}",
            code, description);
      }

      try {

        final Metadata metadata = new Metadata();
        metadata.setId(UUID.randomUUID().toString());
        metadata.setCode(code);
        metadata.setName(description);
        metadata.setActive(true);
        metadata.setPublisher(publisher);
        metadata.setTerminology(terminology);
        metadata.setVersion(version);

        metadata.setModel(MetaModel.Model.valueOf(mappedModelType));
        metadata.setField(MetaModel.Field.valueOf(fieldType));
        metadataList.add(metadata);

      } catch (final IllegalArgumentException e) {
        LOGGER.warn(
            "Skipping metadata due to invalid model or field type: model={}, field={}, code={}, name={}",
            mappedModelType, fieldType, code, description);
      }
    }

    return metadataList;
  }

  /**
   * Creates the concept.
   *
   * @param conceptNode the concept node
   * @param terminology the terminology
   * @return the concept
   * @throws Exception the exception
   */
  private static Concept createConcept(final JsonNode conceptNode, final Terminology terminology)
    throws Exception {

    final Concept concept = new Concept();

    final String id =
        conceptNode.has("id") ? conceptNode.path("id").asText() : UUID.randomUUID().toString();

    concept.setId(id);
    concept.setCode(conceptNode.path("code").asText());
    concept.setTerminology(terminology.getAbbreviation());
    concept.setVersion(terminology.getVersion());
    concept.setPublisher(terminology.getPublisher());
    concept.setLeaf(true); // Default to true unless children found
    concept.setName(conceptNode.path("display").asText());

    // Handle terminology-specific concept attributes
    if (terminology.getAbbreviation().contains("SNOMEDCT")) {

      if (conceptNode.has("definition")) {
        final List<Definition> definitions =
            createDefinitions(conceptNode.path("definition").asText(), terminology);
        if (definitions != null && !definitions.isEmpty()) {
          concept.getDefinitions().addAll(definitions);
        }
      }

      // Set concept ID if different from code
      final String conceptId = conceptNode.path("conceptId").asText(null);
      if (conceptId != null && !conceptId.equals(concept.getCode())) {
        concept.getAttributes().put("conceptId", conceptId);
      }

      // Process designations (terms)
      final JsonNode designations = conceptNode.path("designation");
      for (final JsonNode designation : designations) {
        final Term term = new Term();
        term.setId(UUID.randomUUID().toString());
        term.setName(designation.path("value").asText());

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

        // Set language/locale
        final String language = designation.path("language").asText();
        term.getLocaleMap().put(language, "HT".equals(term.getType()));

        // Set component ID if available
        final String componentId = designation.path("id").asText(null);
        if (componentId != null) {
          term.setComponentId(componentId);
        }

        concept.getTerms().add(term);

        // Use first HT designation as concept name
        if (concept.getName() == null && "HT".equals(term.getType())) {
          concept.setName(term.getName());
        }
      }

    } else if ("LNC".equals(terminology.getAbbreviation())) {
      // Handle LOINC specific concept attributes
      final JsonNode designations = conceptNode.path("designation");
      for (final JsonNode designation : designations) {
        final Term term = new Term();
        term.setId(UUID.randomUUID().toString());
        term.setName(designation.path("value").asText());

        // Safely set term type with null checks
        if (designation.has("use") && designation.path("use").has("code")) {
          term.setType(designation.path("use").path("code").asText());
        } else {
          // Default to PT (Preferred Term) if no type is specified
          term.setType("PT");
          LOGGER.warn("Missing term type for LOINC designation, defaulting to PT for concept: {}",
              concept.getCode());
        }

        term.setTerminology(terminology.getAbbreviation());
        term.setVersion(terminology.getVersion());
        term.setPublisher(terminology.getPublisher());
        term.setCode(concept.getCode());
        term.setConceptId(concept.getCode());

        // Set language/locale
        final String language = designation.path("language").asText();
        term.getLocaleMap().put(language, "LPDN".equals(term.getType()));

        concept.getTerms().add(term);

        // Use LPDN (LOINC parts display name) as concept name
        if ("LPDN".equals(term.getType())) {
          concept.setName(term.getName());
        }
      }

    } else if ("ICD10CM".equals(terminology.getAbbreviation())) {
      // Handle ICD-10-CM specific concept attributes
      final JsonNode designations = conceptNode.path("designation");
      for (final JsonNode designation : designations) {
        final Term term = new Term();
        term.setId(UUID.randomUUID().toString());
        term.setName(designation.path("value").asText());

        // Safely set term type with null checks
        if (designation.has("use") && designation.path("use").has("code")) {
          term.setType(designation.path("use").path("code").asText());
        } else {
          // Default to PT (Preferred Term) if no type is specified
          term.setType("PT");
          LOGGER.warn(
              "Missing term type for ICD-10-CM designation, defaulting to PT for concept: {}",
              concept.getCode());
        }

        term.setTerminology(terminology.getAbbreviation());
        term.setVersion(terminology.getVersion());
        term.setPublisher(terminology.getPublisher());
        term.setCode(concept.getCode());
        term.setConceptId(concept.getCode());

        // Set language/locale
        final String language = designation.path("language").asText();
        term.getLocaleMap().put(language, "HT".equals(term.getType()));

        concept.getTerms().add(term);

        // Use HT (Hierarchical term) as concept name
        if ("HT".equals(term.getType())) {
          concept.setName(term.getName());
        }
      }

      // Add ICD-10-CM specific attributes
      final JsonNode properties = conceptNode.path("property");
      for (final JsonNode property : properties) {
        final String code = property.path("code").asText();
        String value = null;

        if (property.has("valueString")) {
          value = property.path("valueString").asText();
        } else if (property.has("valueBoolean")) {
          value = property.path("valueBoolean").asText();
        }

        if ("EXCLUDES1".equals(code) || "USE_ADDITIONAL".equals(code) || "ORDER_NO".equals(code)
            || "NOTE".equals(code)) {
          concept.getAttributes().put(code, value);
        } else if ("semanticType".equals(code)) {
          concept.getSemanticTypes().add(value);
        }
      }
    } else if ("RXNORM".equals(terminology.getAbbreviation())) {
      // Handle RXNORM specific concept attributes
      final JsonNode designations = conceptNode.path("designation");
      for (final JsonNode designation : designations) {
        final Term term = new Term();
        term.setId(UUID.randomUUID().toString());
        term.setName(designation.path("value").asText());

        // Safely set term type with null checks
        if (designation.has("use") && designation.path("use").has("code")) {
          term.setType(designation.path("use").path("code").asText());
        } else {
          // Default to PT (Preferred Term) if no type is specified
          term.setType("PT");
          LOGGER.warn("Missing term type for RXNORM designation, defaulting to PT for concept: {}",
              concept.getCode());
        }

        term.setTerminology(terminology.getAbbreviation());
        term.setVersion(terminology.getVersion());
        term.setPublisher(terminology.getPublisher());
        term.setCode(concept.getCode());
        term.setConceptId(concept.getCode());

        // Set language/locale
        final String language = designation.path("language").asText();
        term.getLocaleMap().put(language, "PT".equals(term.getType()));

        concept.getTerms().add(term);

        // Use first PT designation as concept name
        if (concept.getName() == null && "PT".equals(term.getType())) {
          concept.setName(term.getName());
        }
      }
    }

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

      if ("semanticType".equals(code)) {
        concept.getSemanticTypes().add(value);
      } else if (!"parent".equals(code)) {
        concept.getAttributes().put(code, value);
      }

    }

    return concept;
  }

  /**
   * Creates a relationship between concepts.
   *
   * @param relationshipNode the relationship JSON node
   * @param fromConcept the source concept
   * @param terminology the terminology
   * @return the concept relationship
   */
  private static ConceptRelationship createRelationship(final JsonNode relationshipNode,
    final Concept fromConcept, final Terminology terminology) {

    final ConceptRelationship relationship = new ConceptRelationship();
    relationship.setId(UUID.randomUUID().toString());
    relationship.setTerminology(terminology.getAbbreviation());
    relationship.setVersion(terminology.getVersion());
    relationship.setPublisher(terminology.getPublisher());

    // Get relationship type and additional type from extensions
    String type = "other";
    String additionalType = null;
    String group = null;

    // Handle different terminology formats
    if (terminology.getAbbreviation().contains("SNOMEDCT")) {
      // Process SNOMED CT style extensions
      final JsonNode extensions = relationshipNode.path("extension");
      if (!extensions.isMissingNode()) {
        for (final JsonNode extension : extensions) {
          final String url = extension.path("url").asText();
          if (url.endsWith("/additionalType")) {
            final JsonNode valueCoding = extension.path("valueCoding");
            additionalType = valueCoding.path("code").asText();
            LOGGER.debug("Found additionalType: {}", additionalType);
          } else if (url.endsWith("/group")) {
            group = extension.path("valueString").asText();
          }
        }
      }

      // Set relationship type based on code
      final String code = relationshipNode.path("code").asText();
      LOGGER.debug("Processing SNOMED CT relationship with code: {}", code);

      if ("parent".equalsIgnoreCase(code)) {
        type = "parent";
        additionalType = "ISA";
        LOGGER.debug("Set type to ISA for parent relationship");
      } else {
        type = "relationship";
      }

    } else if (terminology.getAbbreviation().contains("LNC")) {
      // Handle LOINC relationships
      final String code = relationshipNode.path("code").asText();
      if ("parent".equalsIgnoreCase(code)) {
        type = "parent";
        additionalType = "ISA";
      } else if ("relationship".equals(code)) {
        type = "relationship";
        // Get additionalType from extension
        final JsonNode extensions = relationshipNode.path("extension");
        if (!extensions.isMissingNode()) {
          for (final JsonNode extension : extensions) {
            final String url = extension.path("url").asText();
            if (url.endsWith("/additionalType")) {
              final JsonNode valueCoding = extension.path("valueCoding");
              additionalType = valueCoding.path("code").asText();
            }
          }
        }
      }
    } else if (terminology.getAbbreviation().contains("ICD10CM")) {
      // Handle ICD-10-CM relationships
      final String code = relationshipNode.path("code").asText();
      if ("parent".equals(code)) {
        type = "parent";
        additionalType = "ISA";
      }
    }

    relationship.setType(type);
    relationship.setAdditionalType(additionalType);

    // Create a ConceptRef for the from concept to avoid circular references
    final ConceptRef fromRef = new ConceptRef();
    fromRef.setId(fromConcept.getId());
    fromRef.setCode(fromConcept.getCode());
    fromRef.setName(fromConcept.getName());
    fromRef.setTerminology(terminology.getAbbreviation());
    fromRef.setVersion(terminology.getVersion());
    fromRef.setPublisher(terminology.getPublisher());
    relationship.setFrom(fromRef);

    // Set target concept reference from valueCoding
    final JsonNode valueCoding = relationshipNode.path("valueCoding");
    if (!valueCoding.isMissingNode()) {
      final ConceptRef toRef = new ConceptRef();
      toRef.setCode(valueCoding.path("code").asText());
      toRef.setName(valueCoding.path("display").asText());
      toRef.setTerminology(terminology.getAbbreviation());
      toRef.setVersion(terminology.getVersion());
      toRef.setPublisher(terminology.getPublisher());
      relationship.setTo(toRef);
    }

    // Set additional attributes
    relationship.setHierarchical("parent".equals(type) || "ISA".equals(additionalType));
    relationship.setHistorical(false);
    relationship.setAsserted(true);
    relationship.setDefining(relationshipNode.path("defining").asBoolean(false));

    // Set group if found
    if (group != null) {
      relationship.setGroup(group);
    }

    return relationship;
  }

  /**
   * Creates the definition.
   *
   * @param definition the definition
   * @param terminology the terminology
   * @return the list
   */
  private static List<Definition> createDefinitions(final String definition,
    final Terminology terminology) {

    final List<Definition> definitions = new ArrayList<>();

    final String[] definitionArray = definition.split("\\n");
    if (definitionArray != null && definitionArray.length > 0) {
      for (int i = 0; i < definitionArray.length; i++) {
        if (isNotBlank(definitionArray[i])) {
          final Definition def = new Definition();
          def.setId(UUID.randomUUID().toString());
          def.setDefinition(definitionArray[i].trim());
          def.setTerminology(terminology.getAbbreviation());
          def.setVersion(terminology.getVersion());
          def.setPublisher(terminology.getPublisher());
          definitions.add(def);
        }
      }
    }
    return definitions;

  }

  /**
   * Gets the concept cache key.
   *
   * @param concept the concept
   * @return the concept cache key
   */
  private static String getConceptCacheKey(final Concept concept) {
    if (concept == null) {
      return null;
    }
    return concept.getPublisher() + KEY_DELIMITER + concept.getTerminology() + KEY_DELIMITER
        + concept.getVersion() + KEY_DELIMITER + concept.getCode();
  }

  /**
   * Compute concept tree positions.
   *
   * @param service the service
   * @param terminologyName the terminology name
   * @param publisher the publisher
   * @param version the version
   * @throws Exception the exception
   */
  private static void computeConceptTreePositions(final EntityRepositoryService service,
    final String terminologyName, final String publisher, final String version) throws Exception {

    LOGGER.info("Computing concept tree positions for terminology: {}, publisher: {}, version: {}",
        terminologyName, publisher, version);

    final TreePositionAlgorithm treepos = new TreePositionAlgorithm(service);
    treepos.setTerminology(terminologyName);
    treepos.setPublisher(publisher);
    treepos.setVersion(version);
    treepos.checkPreconditions();
    treepos.compute();

  }

  /**
   * Creates the concept ref.
   *
   * @param concept the concept
   * @return the concept ref
   */
  private static ConceptRef createConceptRef(final String code, final Concept concept) {
    final ConceptRef ref = new ConceptRef();
    ref.setCode(code);
    ref.setName(concept.getName());
    ref.setTerminology(concept.getTerminology());
    ref.setVersion(concept.getVersion());
    ref.setPublisher(concept.getPublisher());
    return ref;
  }

  /**
   * Compute ancestors recursively.
   *
   * @param conceptCode the concept code
   * @param codeToParents the code to parents map
   * @param ancestors the ancestors set to populate
   */
  private static void computeAncestors(final String conceptCode,
    final Map<String, Set<String>> codeToParents, final Set<String> ancestors) {
    final Set<String> parents = codeToParents.get(conceptCode);
    if (parents != null) {
      for (final String parent : parents) {
        if (ancestors.add(parent)) {
          computeAncestors(parent, codeToParents, ancestors);
        }
      }
    }
  }

  /**
   * Compute descendants recursively.
   *
   * @param conceptCode the concept code
   * @param codeToChildren the code to children map
   * @param descendants the descendants set to populate
   */
  private static void computeDescendants(final String conceptCode,
    final Map<String, Set<String>> codeToChildren, final Set<String> descendants) {
    final Set<String> children = codeToChildren.get(conceptCode);
    if (children != null) {
      for (final String child : children) {
        if (descendants.add(child)) {
          computeDescendants(child, codeToChildren, descendants);
        }
      }
    }
  }

}
