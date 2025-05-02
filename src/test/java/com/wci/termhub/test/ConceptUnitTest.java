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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.Application;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * The Class ConceptUnitTest.
 */
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(OrderAnnotation.class)
public class ConceptUnitTest extends BaseUnitTest {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(ConceptUnitTest.class);

  /** The concept json. */
  private static final String CONCEPT_JSON = """
                {
        "_index": "snomedctus-nlm-20230901-concept-v1",
        "_type": "_doc",
        "_id": "b2a2dec8-5456-4ee4-abe7-c0c900f5c14f",
        "_score": 1,
        "_source": {
            "_class": "com.wci.termhub.model.Concept",
            "normName": "sprain",
            "stemName": "sprain",
            "terms": [
                {
                    "name": "Sprain",
                    "normName": "sprain",
                    "stemName": "sprain",
                    "wordCt": 1,
                    "length": 6,
                    "terminology": "SNOMEDCT_US",
                    "version": "20230901",
                    "publisher": "NLM",
                    "componentId": "2756010",
                    "code": "1000004",
                    "conceptId": "1000004",
                    "localeMap": {
                        "en_GB": true,
                        "en": true
                    },
                    "type": "900000000000013009",
                    "attributes": {
                        "caseSignificanceId": "900000000000448009",
                        "900000000000490003": "900000000000495008",
                        "moduleId": "900000000000207008"
                    },
                    "modified": "1501484400000",
                    "created": "1501484400000",
                    "modifiedBy": "loader",
                    "local": false,
                    "active": true,
                    "id": "ced133b3-9e9b-4e2e-9b62-394fc9a2541e"
                },
                {
                    "name": "Sprain, NOS",
                    "normName": "sprain nos",
                    "stemName": "sprain nos",
                    "wordCt": 2,
                    "length": 11,
                    "terminology": "SNOMEDCT_US",
                    "version": "20230901",
                    "publisher": "NLM",
                    "componentId": "2757018",
                    "code": "1000004",
                    "conceptId": "1000004",
                    "localeMap": {
                        "en": false
                    },
                    "type": "900000000000013009",
                    "attributes": {
                        "caseSignificanceId": "900000000000020002",
                        "900000000000490003": "900000000000495008",
                        "moduleId": "900000000000207008"
                    },
                    "modified": "1012464000000",
                    "created": "1012464000000",
                    "modifiedBy": "loader",
                    "local": false,
                    "active": false,
                    "id": "b54ae81f-0d27-46e8-bcf4-008035b650c2"
                },
                {
                    "name": "Joint injury, NOS",
                    "normName": "joint injury nos",
                    "stemName": "joint injuri nos",
                    "wordCt": 3,
                    "length": 17,
                    "terminology": "SNOMEDCT_US",
                    "version": "20230901",
                    "publisher": "NLM",
                    "componentId": "2758011",
                    "code": "1000004",
                    "conceptId": "1000004",
                    "localeMap": {
                        "en": false
                    },
                    "type": "900000000000013009",
                    "attributes": {
                        "caseSignificanceId": "900000000000020002",
                        "900000000000490003": "900000000000495008",
                        "moduleId": "900000000000207008"
                    },
                    "modified": "1012464000000",
                    "created": "1012464000000",
                    "modifiedBy": "loader",
                    "local": false,
                    "active": false,
                    "id": "c2e4e7c8-0ea8-4d2f-aebd-cb045cdbcd1a"
                },
                {
                    "name": "Joint injury",
                    "normName": "joint injury",
                    "stemName": "joint injuri",
                    "wordCt": 2,
                    "length": 12,
                    "terminology": "SNOMEDCT_US",
                    "version": "20230901",
                    "publisher": "NLM",
                    "componentId": "2759015",
                    "code": "1000004",
                    "conceptId": "1000004",
                    "localeMap": {
                        "en_GB": false,
                        "en": false
                    },
                    "type": "900000000000013009",
                    "attributes": {
                        "caseSignificanceId": "900000000000448009",
                        "900000000000490003": "900000000000495008",
                        "moduleId": "900000000000207008"
                    },
                    "modified": "1501484400000",
                    "created": "1501484400000",
                    "modifiedBy": "loader",
                    "local": false,
                    "active": true,
                    "id": "3cd4e81b-9ea4-49cd-b9b6-7e7e93a9f245"
                },
                {
                    "name": "Sprain (morphologic abnormality)",
                    "normName": "sprain morphologic abnormality",
                    "stemName": "sprain morpholog abnorm",
                    "wordCt": 3,
                    "length": 32,
                    "terminology": "SNOMEDCT_US",
                    "version": "20230901",
                    "publisher": "NLM",
                    "componentId": "513500012",
                    "code": "1000004",
                    "conceptId": "1000004",
                    "localeMap": {
                        "en_GB": true,
                        "en": true
                    },
                    "type": "900000000000003001",
                    "attributes": {
                        "caseSignificanceId": "900000000000448009",
                        "900000000000490003": "900000000000495008",
                        "moduleId": "900000000000207008"
                    },
                    "modified": "1501484400000",
                    "created": "1501484400000",
                    "modifiedBy": "loader",
                    "local": false,
                    "active": true,
                    "id": "7dd7b3e5-f363-4ae1-9227-ba00a5a17829"
                }
            ],
            "indexTerms": [],
            "definitions": [],
            "axioms": [],
            "ecl": [
                "900000000000527005=384709000",
                "900000000000527005=*"
            ],
            "attributes": {
                "definitionStatusId": "900000000000074008",
                "900000000000489007": "900000000000482003",
                "moduleId": "900000000000207008"
            },
            "highlights": {},
            "semanticTypes": [],
            "labels": [],
            "children": [],
            "parents": [],
            "descendants": [],
            "ancestors": [],
            "relationships": [],
            "inverseRelationships": [],
            "treePositions": [],
            "name": "Sprain",
            "code": "1000004",
            "terminology": "SNOMEDCT_US",
            "version": "20230901",
            "publisher": "NLM",
            "leaf": true,
            "defined": false,
            "modified": "1044000000000",
            "created": "1044000000000",
            "modifiedBy": "loader",
            "local": false,
            "active": false,
            "id": "b2a2dec8-5456-4ee4-abe7-c0c900f5c14f"
        }
      }
                """;

  /** The Constant INDEX_NAME. */
  private static final String INDEX_NAME = Concept.class.getCanonicalName();

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The concept. */
  private static Concept concept;

  /**
   * Creates the index.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void createIndex() throws Exception {
    logger.info("Creating index for Concept");
    searchService.createIndex(Concept.class);
    final File indexFile = new File(INDEX_DIRECTORY, INDEX_NAME);
    assertTrue(indexFile.exists());
  }

  /**
   * Test add concept.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(2)
  public void testAddConcept() throws Exception {

    final ObjectMapper objectMapper = new ObjectMapper();
    final JsonNode rootNode = objectMapper.readTree(CONCEPT_JSON);
    final JsonNode conceptNode = rootNode.get("_source");

    if (conceptNode != null) {
      concept = objectMapper.treeToValue(conceptNode, Concept.class);
      logger.info("Concept: {}", concept);
      assertDoesNotThrow(() -> searchService.add(Concept.class, concept));
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
  public void findConceptByCode() throws Exception {

    final SearchParameters searchParameters = new SearchParameters("code:1000004", 100, 0);
    logger.info("Search for : {}", searchParameters.getQuery());

    final ResultList<Concept> foundConceptObjects =
        searchService.find(searchParameters, Concept.class);
    assertEquals(1, foundConceptObjects.getItems().size());

    for (final Object foundConceptObject : foundConceptObjects.getItems()) {
      final Concept foundConcept = (Concept) foundConceptObject;
      logger.info("Concept found: {}", foundConcept);
      assertEquals(concept.toString(), foundConcept.toString());
    }
  }

  /**
   * Find concept by missing code.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(5)
  public void findConceptByMissingCode() throws Exception {

    final SearchParameters searchParameters = new SearchParameters("code:99999", 100, 0);
    logger.info("Search for : {}", searchParameters.getQuery());

    final ResultList<Concept> foundConceptObjects =
        searchService.find(searchParameters, Concept.class);
    assertEquals(0, foundConceptObjects.getItems().size());
  }

  // /**
  // * Find concept by term name.
  // *
  // * @throws Exception the exception
  // */
  // @Test
  // @Order(6)
  // public void findConceptByTermName() throws Exception {
  //
  // final SearchParameters searchParameters = new SearchParameters();
  // searchParameters.setQuery("term.name:\"Joint injury, NOS\"");
  // logger.info("Search for : {}", searchParameters.getQuery());
  //
  // final ResultList<Concept> foundConceptObjects =
  // searchService.find(searchParameters, Concept.class);
  // assertEquals(1, foundConceptObjects.getItems().size());
  //
  // for (final Concept foundConceptObject : foundConceptObjects.getItems()) {
  // logger.info("Concept found: {}", foundConceptObject.toString());
  // assertEquals(concept.toString(), foundConceptObject.toString());
  // }
  // }
  //
  // /**
  // * Find concept by missing term name.
  // *
  // * @throws Exception the exception
  // */
  // @Test
  // @Order(7)
  // public void findConceptByMissingTermName() throws Exception {
  //
  // final SearchParameters searchParameters = new
  // SearchParameters("term.name:\"dummy term name\"", 100, 0);
  // logger.info("Search for : {}", searchParameters.getQuery());
  //
  // final ResultList<Concept> foundConceptObjects =
  // searchService.find(searchParameters, Concept.class);
  // assertEquals(0, foundConceptObjects.getItems().size());
  // }

  // /**
  // * Delete index.
  // *
  // * @throws Exception the exception
  // */
  // @Test
  // @Order(6)
  // public void deleteIndex() throws Exception {
  //
  // logger.info("Deleting index for Concept from {}",
  // Paths.get(INDEX_DIRECTORY, INDEX_NAME).toString());
  // searchService.deleteIndex(Concept.class);
  //
  // // assert directory does not exist
  // assertFalse(Files.exists(Paths.get(INDEX_DIRECTORY, INDEX_NAME)));
  // }

}
