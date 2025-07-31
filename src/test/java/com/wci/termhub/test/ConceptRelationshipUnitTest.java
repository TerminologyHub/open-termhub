/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.FileUtility;
import com.wci.termhub.util.ThreadLocalMapper;

/**
 * Tests for ConceptRelationship.
 */
@TestMethodOrder(OrderAnnotation.class)
public class ConceptRelationshipUnitTest extends AbstractClassTest {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(ConceptRelationshipUnitTest.class);

  /** The concept relationship json. */
  private static final String CONCEPT_RELATIONSHIP_JSON = """
      {
        "_index": "snomedctus-nlm-20230901-concept-relationship-v1",
        "_type": "_doc",
        "_id": "84234204-cfc9-4258-a07b-9c13cf02b70b",
        "_score": 1,
        "_source": {
            "_class": "com.wci.termhub.model.ConceptRelationship",
            "terminology": "SNOMEDCT_US",
            "version": "20230901",
            "publisher": "NLM",
            "componentId": "88aa17e4-4971-5dfa-9643-1a97ca3539a5",
            "type": "other",
            "additionalType": "900000000000524003",
            "from": {
                "name": "EFA LIQUID",
                "code": "100476003",
                "terminology": "SNOMEDCT_US",
                "version": "20230901",
                "publisher": "NLM",
                "leaf": true,
                "defined": false,
                "local": false,
                "active": false
            },
            "to": {
                "name": "Extension Namespace 1000009",
                "code": "416516009",
                "terminology": "SNOMEDCT_US",
                "version": "20230901",
                "publisher": "NLM",
                "leaf": true,
                "defined": false,
                "local": false,
                "active": true
            },
            "historical": true,
            "asserted": true,
            "attributes": {
                "moduleId": "900000000000207008"
            },
            "modified": "1249023600000",
            "created": "1249023600000",
            "modifiedBy": "loader",
            "local": false,
            "active": true,
            "id": "84234204-cfc9-4258-a07b-9c13cf02b70b"
        }
      }
        """;

  /** The entity service impl. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The concept. */
  private static ConceptRelationship conceptRelationship;

  /** The Constant INDEX_NAME. */
  private static final String INDEX_NAME = ConceptRelationship.class.getCanonicalName();

  /**
   * Creates the index.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void createIndex() throws Exception {

    logger.info("Creating index for Concept Relationship");
    final File indexFile = new File(INDEX_DIRECTORY, INDEX_NAME);
    FileUtility.deleteDirectoryRecursively(indexFile.toPath());
    searchService.createIndex(ConceptRelationship.class);
    assertTrue(indexFile.exists(),
        "Index directory does not exist: " + indexFile.getAbsolutePath());
  }

  /**
   * Test add concept.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(2)
  public void testAddConceptRelationship() throws Exception {

    final ObjectMapper objectMapper = ThreadLocalMapper.get();
    final JsonNode rootNode = objectMapper.readTree(CONCEPT_RELATIONSHIP_JSON);
    final JsonNode conceptRelNode = rootNode.get("_source");

    if (conceptRelNode != null) {
      conceptRelationship = objectMapper.treeToValue(conceptRelNode, ConceptRelationship.class);
      logger.info("Concept Relationship: {}", conceptRelationship);
      assertDoesNotThrow(() -> searchService.add(ConceptRelationship.class, conceptRelationship));
    } else {
      logger.error("No '_source' node found in the provided JSON.");
    }
  }

  /**
   * Find concept by code.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(3)
  public void findConceptRelationshipById() throws Exception {

    final ConceptRelationship foundConceptRelObjects =
        searchService.get("84234204-cfc9-4258-a07b-9c13cf02b70b", ConceptRelationship.class);

    assertNotNull(foundConceptRelObjects);
    assertEquals(conceptRelationship.toString(), foundConceptRelObjects.toString());

  }

  /**
   * Find concept by code.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(4)
  public void findConceptRelationshipByFromCode() throws Exception {

    final SearchParameters searchParameters = new SearchParameters();
    searchParameters.setQuery("from.code:100476003");
    logger.info("Search for : {}", searchParameters.getQuery());

    final ResultList<ConceptRelationship> foundConceptRelObjects =
        searchService.find(searchParameters, ConceptRelationship.class);
    assertEquals(1, foundConceptRelObjects.getItems().size());

    for (final Object foundConceptObject : foundConceptRelObjects.getItems()) {
      final ConceptRelationship foundConceptRel = (ConceptRelationship) foundConceptObject;
      logger.info("Concept Relationship found: {}", foundConceptRel);
      assertEquals(conceptRelationship.toString(), foundConceptRel.toString());
    }
  }

}
