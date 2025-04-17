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
import java.util.Arrays;
import java.util.HashMap;
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
  private static final Logger LOG = LoggerFactory.getLogger(CodeSystemLoaderUtil.class);

  /** The terminologies cache. */
  private static TimerCache<Concept> conceptCache = new TimerCache<>(1000, 10000);

  /** The key delimiter. */
  private static final String KEY_DELIMITER = "|";

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
      final List<Metadata> metadataList = createMetadata(root);
      if (metadataList != null && !metadataList.isEmpty()) {
        for (final Metadata metadata : metadataList) {
          service.add(Metadata.class, metadata);
        }
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

        if ("118946009".equals(concept.getCode())) {
          LOG.info("concept: {}", concept);
        }

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

            if (relationshipBatch.size() == batchSize) {
              service.addBulk(ConceptRelationship.class, new ArrayList<>(relationshipBatch));
              relationshipBatch.clear();
              LOG.info("indexCodeSystem: processed relationships count: {}", relationshipCount);
            }
            concept.getEclClauses().add(propertyNode.path("code").asText() + "="
                + propertyNode.path("valueCoding").get("code").asText());
            relationshipCount++;
          }

          if ("relationship".equals(propertyType)) {
            final JsonNode extensionNode = propertyNode.path("extension");
            for (final JsonNode extension : extensionNode) {
              concept.getEclClauses().add(extension.path("valueCoding").get("code").asText() + "="
                  + propertyNode.path("valueCoding").get("code").asText());
            }
          }

        }

        if ("118946009".equals(concept.getCode())) {
          LOG.info("concept after relationships: {}", concept);
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

        LOG.info("indexCodeSystem: concept: {}", concept);

        // Add concept to batch
        conceptBatch.add(concept);
        final String conceptKey = getConceptCacheKey(concept);
        conceptCache.put(conceptKey, concept);
        if (conceptBatch.size() == batchSize) {
          service.addBulk(Concept.class, new ArrayList<>(conceptBatch));
          SystemReportUtility.logMemory();
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

    } catch (

    final Exception e) {
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
    final String version = root.path("version").asText().replaceFirst("^.*?version/", "");
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
    final String version = root.path("version").asText().replaceFirst("^.*?version/", "");
    terminology.setVersion(version);
    // Extract YYYY-MM-DD
    terminology.setReleaseDate(root.path("date").asText().substring(0, 10));
    terminology.setUri(root.path("url").asText());
    // NOT IN JSON
    terminology.setFamily(root.path("family").asText());
    terminology.setConceptCt(root.path("count").asLong());

    // Set terminology attributes
    final Map<String, String> attributes = new HashMap<>();
    // attributes.put(Terminology.Attributes.hierarchical.property(), "true");
    // attributes.put(Terminology.Attributes.descriptionLogicBased.property(),
    // Boolean.toString(root.path("compositional").asBoolean()));

    final JsonNode properties = root.path("property");
    for (final JsonNode property : properties) {
      final String uri = property.path("uri").asText();
      final String[] uriParts = FieldedStringTokenizer.split(uri, "/");
      final String modelType = uriParts[uriParts.length - 2];
      final String attributeName = uriParts[uriParts.length - 1];
      if (uriParts.length < 3
          || !("terminology".equals(modelType) && "attributes".equals(attributeName))) {
        continue;
      }

      attributes.put(property.path("code").asText(), property.path("description").asText());

    }

    terminology.setAttributes(attributes);

    LOG.info("CodeSystemLoaderUtil: terminology: {}", terminology);

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
    final String version = root.path("version").asText().replaceFirst("^.*?version/", "");

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

      final Metadata metadata = new Metadata();
      metadata.setId(UUID.randomUUID().toString());
      metadata.setCode(code);
      metadata.setName(description);
      metadata.setActive(true);
      metadata.setPublisher(publisher);
      metadata.setTerminology(terminology);
      metadata.setVersion(version);
      final String fieldType = uriParts[uriParts.length - 1];
      metadata.setModel(MetaModel.Model.valueOf(modelType));
      metadata.setField(MetaModel.Field.valueOf(fieldType));

      metadataList.add(metadata);
    }

    // LOG.info("CodeSystemLoaderUtil: metadata:");
    // for (final Metadata metadata : metadataList) {
    // System.out.println(ModelUtility.toJson(metadata, false));
    // }

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
    if (List.of("SNOMEDCT", "SNOMEDCT_US").contains(terminology.getAbbreviation())) {

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
        term.setType(designation.path("use").path("code").asText());
        term.setTerminology(terminology.getAbbreviation());
        term.setVersion(terminology.getVersion());
        term.setPublisher(terminology.getPublisher());
        term.setCode(concept.getCode());
        term.setConceptId(concept.getCode());
        term.setType(designation.get("use").get("code").asText()); // designation.use.code
        // missing caseSignificanceId and moduleId for designations
        // term.getAttributes().put("caseSignificanceId",
        // designation.get("caseSignificanceId").asText());
        // term.getAttributes().put("moduleId",
        // designation.get("moduleId").asText());

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

    } else if ("LNC".equals(terminology.getAbbreviation())) {
      // Handle LOINC specific concept attributes
      final JsonNode designations = conceptNode.path("designation");
      for (final JsonNode designation : designations) {
        final Term term = new Term();
        term.setId(UUID.randomUUID().toString());
        term.setName(designation.path("value").asText());
        term.setType(designation.path("use").path("code").asText());
        term.setTerminology(terminology.getAbbreviation());
        term.setVersion(terminology.getVersion());
        term.setPublisher(terminology.getPublisher());
        term.setCode(concept.getCode());
        term.setConceptId(concept.getCode());

        // Set language/locale
        final String language = designation.path("language").asText("en");
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
        term.setType(designation.path("use").path("code").asText());
        term.setTerminology(terminology.getAbbreviation());
        term.setVersion(terminology.getVersion());
        term.setPublisher(terminology.getPublisher());
        term.setCode(concept.getCode());
        term.setConceptId(concept.getCode());

        // Set language/locale
        final String language = designation.path("language").asText("en");
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
    if ("SNOMEDCT".equals(terminology.getAbbreviation())) {
      // Process SNOMED CT style extensions
      final JsonNode extensions = relationshipNode.path("extension");
      if (!extensions.isMissingNode()) {
        for (final JsonNode extension : extensions) {
          final String url = extension.path("url").asText();
          if (url.endsWith("/additionalType")) {
            final JsonNode valueCoding = extension.path("valueCoding");
            additionalType = valueCoding.path("code").asText();
          } else if (url.endsWith("/group")) {
            group = extension.path("valueString").asText();
          }
        }
      }

      // Set relationship type based on code
      final String code = relationshipNode.path("code").asText();
      if ("parent".equals(code)) {
        type = "parent";
        // If no additionalType found in extensions, default to "is-a"
        if (additionalType == null) {
          additionalType = "116680003"; // SNOMED CT "Is a" relationship type
        }
      } else {
        type = "relationship";
      }
    } else if ("LNC".equals(terminology.getAbbreviation())) {
      // Handle LOINC relationships
      final String code = relationshipNode.path("code").asText();
      if ("parent".equals(code)) {
        type = "parent";
        additionalType = "is-a";
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
    } else if ("ICD10CM".equals(terminology.getAbbreviation())) {
      // Handle ICD-10-CM relationships
      final String code = relationshipNode.path("code").asText();
      if ("parent".equals(code)) {
        type = "parent";
        additionalType = "is-a";
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
    relationship.setHierarchical("parent".equals(type) || "is-a".equals(additionalType));
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
}
