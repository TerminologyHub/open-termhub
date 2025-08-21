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
import com.wci.termhub.algo.TerminologyCache;
import com.wci.termhub.algo.TreePositionAlgorithm;
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

/**
 * Utility class for loading CodeSystem format JSON files.
 */
public final class CodeSystemLoaderUtil {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CodeSystemLoaderUtil.class);

  /** The Constant BATCH_SIZE. */
  private static final int DEFAULT_BATCH_SIZE = 10000;

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

    return indexCodeSystem(service, content, -1, computeTreePositions);
  }

  /**
   * Index concepts from CodeSystem format JSON.
   *
   * @param service the service
   * @param content the content
   * @param limit the limit
   * @param computeTreePositions whether to compute tree positions
   * @return the string
   * @throws Exception the exception
   */
  private static String indexCodeSystem(final EntityRepositoryService service, final String content,
    final int limit, final boolean computeTreePositions) throws Exception {

    final long startTime = System.currentTimeMillis();
    final List<ConceptRelationship> relationshipBatch = new ArrayList<>(DEFAULT_BATCH_SIZE);
    final List<Term> termBatch = new ArrayList<>(DEFAULT_BATCH_SIZE);
    final String id;
    final Set<Concept> conceptCache = new HashSet<>();
    final TerminologyCache terminologyCache = new TerminologyCache();

    try {

      // Read the entire file as a JSON object
      final JsonNode root = ThreadLocalMapper.get().readTree(content);

      LOGGER.info("Indexing CodeSystem {}: ", root.path("title"));
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("  batch size: {}, limit: {}", DEFAULT_BATCH_SIZE, limit);
      }

      final String abbreviation = root.path("title").asText();
      final String publisher = root.path("publisher").asText();
      String version = root.path("version").asText();
      Terminology terminology = getTerminology(service, abbreviation, publisher, version);

      if (terminology != null) {
        throw new Exception("Can not create multiple CodeSystem resources with CodeSystem.url "
            + terminology.getUri() + " and CodeSystem.version " + terminology.getVersion()
            + ", already have one with resource ID: CodeSystem/" + terminology.getId());
      }

      // Create the terminology
      terminology = createTerminology(service, root, computeTreePositions);
      id = terminology.getId();

      // Extract metadata from root
      final List<Metadata> metadataList = createMetadata(terminology, root);
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

      for (final JsonNode conceptNode : concepts) {
        if (limit != -1 && conceptCount >= limit) {
          break;
        }

        final Concept concept = createConcept(conceptNode, terminology);
        final JsonNode relationships = conceptNode.path("property");
        final List<ConceptRelationship> conceptRelationships =
            createRelationships(relationships, concept, terminology, terminologyCache);
        relationshipBatch.addAll(conceptRelationships);
        relationshipCount++;
        concept.getRelationships().addAll(conceptRelationships);

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

        // Add concept to batch
        terminologyCache.addConcept(concept);
        conceptCache.add(concept);
        conceptCount++;
        if (conceptCache.size() == DEFAULT_BATCH_SIZE) {
          LOGGER.info("  concept count: {}", conceptCount);
        }
      } // end concepts loop

      if (!relationshipBatch.isEmpty()) {
        service.addBulk(ConceptRelationship.class, new ArrayList<>(relationshipBatch));
      }
      if (!termBatch.isEmpty()) {
        service.addBulk(Term.class, new ArrayList<>(termBatch));
      }
      LOGGER.info("  final counts - concepts: {}, relationships: {}, terms: {}, time: {}",
          conceptCount, relationshipCount, termCount, (System.currentTimeMillis() - startTime));

      LOGGER.info("  begin compute for ancestors");
      if (terminology.getAttributes() != null
          && terminology.getAttributes().containsKey(Terminology.Attributes.hierarchical.property())
          && Boolean.parseBoolean(
              terminology.getAttributes().get(Terminology.Attributes.hierarchical.property()))) {
        for (final Concept concept : conceptCache) {
          final Set<String> ancestors = terminologyCache.getAncestors(concept.getCode());
          // get the conceptRef for each ancestor
          if (ancestors != null && !ancestors.isEmpty()) {
            for (final String ancestorCode : ancestors) {
              final ConceptRef ancestorRef =
                  terminologyCache.getOrCreateConceptRef(ancestorCode, concept);
              concept.getAncestors().add(ancestorRef);
            }
          }
        }
      }
      LOGGER.info("  finish compute for ancestors");

      conceptCount = 0;
      final List<Concept> conceptBatch = new ArrayList<>(DEFAULT_BATCH_SIZE);
      for (final Concept concept : conceptCache) {
        conceptBatch.add(concept);
        conceptCount++;
        if (conceptBatch.size() >= DEFAULT_BATCH_SIZE) {
          LOGGER.info("  count: {}", conceptCount);
          service.addBulk(Concept.class, new ArrayList<>(conceptBatch));
          conceptBatch.clear();
        }
      }

      if (!conceptBatch.isEmpty()) {
        LOGGER.info("  count: {}", conceptCount);
        service.addBulk(Concept.class, new ArrayList<>(conceptBatch));
        conceptBatch.clear();
      }

      // compute tree positions if required
      if (computeTreePositions) {
        computeConceptTreePositions(service, terminology);
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
   * @param abbreviation the abbreviation
   * @param publisher the publisher
   * @param version the version
   * @return the terminology
   * @throws Exception the exception
   */
  private static Terminology getTerminology(final EntityRepositoryService service,
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
      throw new IllegalArgumentException("Invalid resource type - expected CodeSystem");
    }

    final Terminology terminology = new Terminology();
    // The HAPI Plan server @Create method blanks the identifier on sending a
    // code system in
    final String id = root.path("id").asText();
    if (isNotBlank(id)) {
      terminology.setId(id);
    } else {
      final String uuid = UUID.randomUUID().toString();
      terminology.setId(uuid);
      LOGGER.warn("Missing ID in root node, generating new UUID for terminology as {}", uuid);
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
    final Map<String, String> attributes = new HashMap<>();
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
          LOGGER.debug("  ADD metadata = {}", metadata.toString());
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
   * @return the concept
   * @throws Exception the exception
   */
  private static Concept createConcept(final JsonNode conceptNode, final Terminology terminology)
    throws Exception {

    final Concept concept = new Concept();

    final String id =
        conceptNode.has("id") ? conceptNode.path("id").asText() : UUID.randomUUID().toString();

    concept.setId(id);
    concept.setActive(true);
    concept.setCode(conceptNode.path("code").asText());
    concept.setTerminology(terminology.getAbbreviation());
    concept.setVersion(terminology.getVersion());
    concept.setPublisher(terminology.getPublisher());
    concept.setLeaf(true); // Default to true unless children found
    concept.setName(conceptNode.path("display").asText());

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

      if ("semanticType".equals(code)) {
        concept.getSemanticTypes().add(value);
      } else if (!"parent".equals(code)) {
        concept.getAttributes().put(code, value);
      }

    }

    return concept;
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
            createRelationship(propertyNode, concept, terminology, terminologyCache, "parent");
        // Track parent-child relationship using thread-safe collections
        final JsonNode valueCoding = propertyNode.path("valueCoding");
        if (!valueCoding.isMissingNode() && valueCoding.has("code")) {
          final String parentCode = valueCoding.path("code").asText();
          terminologyCache.addParChd(parentCode, concept.getCode());
        }

        // Add ECL clause
        if (!valueCoding.isMissingNode() && valueCoding.has("code")) {
          concept.getEclClauses().add(
              propertyNode.path("code").asText() + "=" + valueCoding.path("code").asText());
        }

        relationships.add(relationship);
      }

      if ("relationship".equals(propertyType)) {
        final ConceptRelationship relationship =
            createRelationship(propertyNode, concept, terminology, terminologyCache, "relationship");

        // Process extensions for ECL clauses
        final JsonNode extensionNode = propertyNode.path("extension");
        for (final JsonNode extension : extensionNode) {
          final JsonNode extensionValueCoding = extension.path("valueCoding");
          final JsonNode propertyValueCoding = propertyNode.path("valueCoding");

          if (!extensionValueCoding.isMissingNode() && !propertyValueCoding.isMissingNode()
              && extensionValueCoding.has("code") && propertyValueCoding.has("code")) {
            concept.getEclClauses().add(extensionValueCoding.path("code").asText() + "="
                + propertyValueCoding.path("code").asText());
          }
        }

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
      final Term term = createBaseTerm(terminology, concept, designation);
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
      } else {
        LOGGER.warn("Unsupported terminology: {}. Using basic term processing.",
            terminology.getAbbreviation());
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
   * @return the term
   */
  private static Term createBaseTerm(final Terminology terminology, final Concept concept,
    final JsonNode designation) {

    final Term term = new Term();
    term.setId(UUID.randomUUID().toString());
    term.setActive(true);
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

    final String language = designation.path("language").asText();
    term.getLocaleMap().put(language, false);

    // Set component ID if available
    final String componentId = designation.path("id").asText(null);
    if (componentId != null) {
      term.setComponentId(componentId);
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
   * @param relationshipType the relationship type ("parent" or "relationship")
   * @return the concept relationship
   */
  private static ConceptRelationship createRelationship(final JsonNode relationshipNode,
    final Concept fromConcept, final Terminology terminology,
    final TerminologyCache terminologyCache, final String relationshipType) {

    final ConceptRelationship relationship = new ConceptRelationship();
    relationship.setId(UUID.randomUUID().toString());
    relationship.setActive(true);
    relationship.setTerminology(terminology.getAbbreviation());
    relationship.setVersion(terminology.getVersion());
    relationship.setPublisher(terminology.getPublisher());

    if ("parent".equals(relationshipType)) {
      relationship.setType("Is a");
      relationship.setAdditionalType("116680003");
      relationship.setHierarchical(true);
    } else {
      relationship.setType("other");
      relationship.setAdditionalType("Is a");
      relationship.setHierarchical(false);
    }

    // Process extensions to get additionalType and group
    final JsonNode extensions = relationshipNode.path("extension");
    if (!extensions.isMissingNode()) {
      for (final JsonNode extension : extensions) {
        final String url = extension.path("url").asText();
        if (url.endsWith("/additionalType")) {
          final JsonNode valueCoding = extension.path("valueCoding");
          if (!valueCoding.isMissingNode() && valueCoding.has("code")) {
            relationship.setAdditionalType(valueCoding.path("code").asText());
          }
        } else if (url.endsWith("/group")) {
          relationship.setGroup(extension.path("valueString").asText());
        }
      }
    }

    final ConceptRef fromRef =
        terminologyCache.getOrCreateConceptRef(fromConcept.getCode(), fromConcept);
    relationship.setFrom(fromRef);

    // Set target concept reference from valueCoding
    final JsonNode valueCoding = relationshipNode.path("valueCoding");
    if (!valueCoding.isMissingNode()) {
      final ConceptRef toRef = terminologyCache.getOrCreateConceptRef(
          valueCoding.path("code").asText(), valueCoding.path("display").asText(), terminology);
      relationship.setTo(toRef);
    }

    // Set additional attributes
    relationship.setHistorical(false);
    relationship.setAsserted(true);
    relationship.setDefining(relationshipNode.path("defining").asBoolean(false));

    return relationship;
  }

  /**
   * Compute concept tree positions.
   *
   * @param service the service
   * @param terminology the terminology
   * @throws Exception the exception
   */
  private static void computeConceptTreePositions(final EntityRepositoryService service,
    final Terminology terminology) throws Exception {

    LOGGER.info(
        "  Computing concept tree positions for terminology: {}, publisher: {}, version: {}",
        terminology.getAbbreviation(), terminology.getPublisher(), terminology.getVersion());

    final TreePositionAlgorithm treepos = new TreePositionAlgorithm(service);
    treepos.setTerminology(terminology.getAbbreviation());
    treepos.setPublisher(terminology.getPublisher());
    treepos.setVersion(terminology.getVersion());
    treepos.checkPreconditions();
    treepos.compute();

  }
}
