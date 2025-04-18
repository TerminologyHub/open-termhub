/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.test.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.Application;
import com.wci.termhub.loader.ConceptLoader;
import com.wci.termhub.loader.ConceptRelationshipLoader;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.BaseUnitTest;
import com.wci.termhub.util.FileUtility;

/**
 * The Class IntegrationTest.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class IntegrationTest extends BaseUnitTest {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(IntegrationTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Setup.
   */
  // Uncomment the @BeforeAll annotation if you want to rerun without unzipping
  // the files and reindexing the data
  @BeforeAll
  public static void setup() {

    try {

      FileUtility.extractZipFile("src/test/resources/files/snomed-ecl-20230901.zip",
          "./build/data");

      final int batchSize = 3000;
      final int limit = -1; // no limit

      ConceptLoader.index("./build/data/snomedctus-nlm-20230901-concept-v1.json", batchSize, limit);
      ConceptRelationshipLoader.index(
          "./build/data/snomedctus-nlm-20230901-concept-relationship-v1.json", batchSize, limit);

    } catch (final Exception e) {
      logger.error("Error occurred while loading concepts", e);
    }

  }

  /**
   * Test find concept by id.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptById() throws Exception {

    final String id = "e8963408-bd8c-4e8f-a00b-527f1d10257b";
    final Concept concept = searchService.get(id, Concept.class);
    assertNotNull(concept);
    assertEquals(id, concept.getId());

  }

  /**
   * Test find concept relationship by id.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptRelationshipById() throws Exception {

    final String id = "97569a2c-c002-4c63-bab3-c25eb";
    final ConceptRelationship conceptRel = searchService.get(id, ConceptRelationship.class);
    assertNotNull(conceptRel);
    assertEquals(id, conceptRel.getId());

  }

  /**
   * Test find concept by code.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptByCode() throws Exception {

    final String code = "100455008";
    final SearchParameters sp = new SearchParameters("code:" + code, 100, 0);

    final ResultList<Concept> results = searchService.find(sp, Concept.class);
    results.getItems().forEach(concept -> {
      assertEquals(code, concept.getCode());
    });

  }

  /**
   * Test find concept relationship by from code.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptRelationshipByFromCode() throws Exception {

    final String code = "100455008";
    final SearchParameters sp = new SearchParameters("from.code:" + code, 100, 0);

    final ResultList<ConceptRelationship> results =
        searchService.find(sp, ConceptRelationship.class);
    results.getItems().forEach(conceptRel -> {
      assertEquals(code, conceptRel.getFrom().getCode());
    });

  }

  /**
   * Test find concept relationship by to code.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptRelationshipByToCode() throws Exception {

    final String code = "6266001";
    final SearchParameters sp = new SearchParameters("to.code:" + code, 100, 0);

    final ResultList<ConceptRelationship> results =
        searchService.find(sp, ConceptRelationship.class);
    results.getItems().forEach(conceptRel -> {
      assertEquals(code, conceptRel.getTo().getCode());
    });

  }

  /**
   * Test find concept by term.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptByTermName() throws Exception {

    final String term = "heart";
    final SearchParameters sp = new SearchParameters("term.name:" + term, 100, 0);

    final ResultList<Concept> results = searchService.find(sp, Concept.class);

    for (final Concept concept : results.getItems()) {
      logger.info(concept.toString());
    }

    // check that each concept has at least a term has the word 'heart' in it
    results.getItems().forEach(concept -> {
      assertTrue(
          concept.getTerms().stream().anyMatch(t -> t.getName().toLowerCase().contains(term)));
    });

  }

  /**
   * Test find test paging.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindTestPaging() throws Exception {

    final String term = "heart";
    final SearchParameters sp = new SearchParameters("term.name:" + term, 10, 0);
    final List<String> conceptCodes = new ArrayList<>();

    ResultList<Concept> results = searchService.find(sp, Concept.class);
    assertEquals(10, results.getItems().size());
    results.getItems().forEach(concept -> {
      conceptCodes.add(concept.getCode());
    });

    sp.setOffset(10);
    results = searchService.find(sp, Concept.class);
    assertEquals(10, results.getItems().size());
    results.getItems().forEach(concept -> {
      assertTrue(!conceptCodes.contains(concept.getCode()));
    });

    results.getItems().forEach(concept -> {
      conceptCodes.add(concept.getCode());
    });

    sp.setOffset(20);
    results = searchService.find(sp, Concept.class);
    assertEquals(10, results.getItems().size());
    results.getItems().forEach(concept -> {
      assertTrue(!conceptCodes.contains(concept.getCode()));
    });
  }
}
