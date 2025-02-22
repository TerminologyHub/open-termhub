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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.Definition;
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
  private static final Logger LOG = LoggerFactory.getLogger(CodeSystemLoaderUtil.class);

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
   * @param fullFileName the full file name
   * @throws Exception the exception
   */
  public static void loadCodeSystem(final EntityRepositoryService service,
    final String fullFileName) throws Exception {
    indexCodeSystem(service, fullFileName, 1000, -1);
  }

  /**
   * Index concepts from CodeSystem format JSON.
   *
   * @param service the service
   * @param fullFileName the full file name
   * @param batchSize the batch size
   * @param limit the limit
   * @throws Exception the exception
   */
  public static void indexCodeSystem(final EntityRepositoryService service,
    final String fullFileName, final int batchSize, final int limit) throws Exception {

    LOG.debug("indexCodeSystem: batch size: {} limit: {}", batchSize, limit);
    final long startTime = System.currentTimeMillis();
    final List<Concept> conceptBatch = new ArrayList<>(batchSize);
    final List<ConceptRelationship> relationshipBatch = new ArrayList<>(batchSize);
    final List<Term> termBatch = new ArrayList<>(batchSize);

    try (final BufferedReader br = new BufferedReader(new FileReader(fullFileName))) {

      service.createIndex(Terminology.class);
      service.createIndex(Metadata.class);
      service.createIndex(Concept.class);
      service.createIndex(Term.class);
      service.createIndex(ConceptRelationship.class);

      // Read the entire file as a JSON object
      final JsonNode root = OBJECT_MAPPER.readTree(br);
      final Terminology terminology = getTerminology(service, root);

      // Extract metadata from root
      final Metadata metadata = new Metadata();
      // The Id is the terminology id
      // metadata.setCode(root.path("id").asText());
      metadata.setName(root.path("title").asText());
      metadata.setPublisher(root.path("publisher").asText());
      metadata.setVersion(root.path("version").asText());
      service.add(Metadata.class, metadata);

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

        // Process relationships
        final JsonNode relationships = conceptNode.path("relationship");
        for (final JsonNode relationshipNode : relationships) {
          final ConceptRelationship relationship =
              createRelationship(relationshipNode, concept, terminology);
          relationshipBatch.add(relationship);
          concept.getRelationships().add(relationship);

          if (relationshipBatch.size() == batchSize) {
            service.addBulk(ConceptRelationship.class, new ArrayList<>(relationshipBatch));
            relationshipBatch.clear();
            LOG.info("indexCodeSystem: processed relationships count: {}", relationshipCount);
          }
          relationshipCount++;
        }

        // Process terms
        for (final Term term : concept.getTerms()) {
          termBatch.add(term);
          if (termBatch.size() == batchSize) {
            service.addBulk(Term.class, new ArrayList<>(termBatch));
            termBatch.clear();
            LOG.info("indexCodeSystem: processed terms count: {}", termCount);
          }
          termCount++;
        }

        // Add concept to batch
        conceptBatch.add(concept);
        if (conceptBatch.size() == batchSize) {
          service.addBulk(Concept.class, new ArrayList<>(conceptBatch));
          conceptBatch.clear();
          LOG.info("indexCodeSystem: processed concepts count: {}", conceptCount);
        }
        conceptCount++;
      }

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

      LOG.info("indexCodeSystem: final counts - concepts: {}, relationships: {}, terms: {}",
          conceptCount, relationshipCount, termCount);
      LOG.info("indexCodeSystem: duration: {} ms", (System.currentTimeMillis() - startTime));

    } catch (final Exception e) {
      LOG.error("indexCodeSystem: Error processing file", e);
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
    final String version = root.path("version").asText().replaceFirst("^.*?/version/", "");
    final SearchParameters searchParams = new SearchParameters();
    searchParams.setQuery("abbreviation: " + abbreviation + " publisher: '" + publisher
        + "' and version: '" + version + "'");

    final ResultList<Terminology> terminology = service.find(searchParams, Terminology.class);

    return (terminology.getItems().isEmpty()) ? createTerminology(service, root)
        : terminology.getItems().get(0);

  }

  /**
   * Creates the terminology.
   *
   * @param service the service
   * @param root the root
   * @return the terminology
   * @throws Exception the exception
   */
  private static Terminology createTerminology(final EntityRepositoryService service,
    final JsonNode root) throws Exception {

    // Extract terminology from root
    final Terminology terminology = new Terminology();
    terminology.setId(UUID.randomUUID().toString());
    terminology.setName(root.path("name").asText());
    terminology.setAbbreviation(root.path("title").asText());
    terminology.setPublisher(root.path("publisher").asText());

    // example: "http://snomed.info/sct/731000124108/version/20240301"
    // remove version part from URL
    final String version = root.path("version").asText().replaceFirst("^.*?/version/", "");
    terminology.setVersion(version);
    // Extract YYYY-MM-DD
    terminology.setReleaseDate(root.path("date").asText().substring(0, 10));
    // NOT IN JSON
    terminology.setUri(root.path("url").asText());
    // NOT IN JSON
    terminology.setFamily(root.path("family").asText());
    terminology.setConceptCt(root.path("count").asLong());

    // Set terminology attributes
    final Map<String, String> attributes = new HashMap<>();
    attributes.put(Terminology.Attributes.hierarchical.property(), "true");
    attributes.put(Terminology.Attributes.descriptionLogicBased.property(),
        Boolean.toString(root.path("compositional").asBoolean()));
    // attributes.put(Terminology.Attributes.effectiveTime.property(),
    // root.path("effectiveTime").asText());

    terminology.setAttributes(attributes);

    LOG.info("CodeSystemLoaderUtil: terminology: {}", terminology);

    service.add(Terminology.class, terminology);

    return terminology;
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
      term.setName(designation.path("value").asText());
      term.setType(designation.path("use").path("code").asText());
      term.setTerminology(terminology.getAbbreviation());
      term.setVersion(terminology.getVersion());
      term.setPublisher(terminology.getPublisher());
      term.setCode(concept.getCode());

      // Set language/locale
      final String language = designation.path("language").asText("en");
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

    // Process properties
    final JsonNode properties = conceptNode.path("property");
    final Map<String, String> attributes = new HashMap<>();
    final HashSet<String> semanticTypes = new HashSet<>();

    for (final JsonNode property : properties) {
      final String code = property.path("code").asText();

      // Handle different value types
      String value = null;
      if (property.has("valueString")) {
        value = property.path("valueString").asText();
      } else if (property.has("valueBoolean")) {
        value = property.path("valueBoolean").asText();
      } else if (property.has("valueCoding")) {
        final JsonNode valueCoding = property.path("valueCoding");
        value = valueCoding.path("code").asText() + "|" + valueCoding.path("display").asText();
      } else if (property.has("valueInteger")) {
        value = property.path("valueInteger").asText();
      } else if (property.has("valueDecimal")) {
        value = property.path("valueDecimal").asText();
      }

      if ("semanticType".equals(code)) {
        semanticTypes.add(value);
      } else {
        attributes.put(code, value);
      }
    }

    concept.setAttributes(attributes);
    concept.setSemanticTypes(semanticTypes);

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
    relationship.setTerminology(terminology.getAbbreviation());
    relationship.setVersion(terminology.getVersion());
    relationship.setPublisher(terminology.getPublisher());

    // Set relationship type
    final String type = relationshipNode.path("type").asText();
    relationship.setType(type);
    relationship.setAdditionalType(relationshipNode.path("code").asText());

    // Set source concept reference
    relationship.setFrom(fromConcept);

    // Set target concept reference or value
    final JsonNode target = relationshipNode.path("target");
    if (target.has("concept")) {
      final JsonNode targetConcept = target.path("concept");
      final ConceptRef toRef = new ConceptRef();
      toRef.setCode(targetConcept.path("code").asText());
      toRef.setName(targetConcept.path("display").asText());
      toRef.setTerminology(terminology.getAbbreviation());
      toRef.setVersion(terminology.getVersion());
      toRef.setPublisher(terminology.getPublisher());
      relationship.setTo(toRef);
    } else if (target.has("value")) {
      relationship.setToValue(target.path("value").asText());
    }

    // Set additional attributes
    relationship.setHierarchical("is-a".equals(type) || "parent".equals(type));
    // Set based on relationship type if needed
    relationship.setHistorical(false);
    relationship.setAsserted(true);
    relationship.setDefining(relationshipNode.path("defining").asBoolean(false));

    // Set group if available
    final String group = relationshipNode.path("group").asText(null);
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
}
