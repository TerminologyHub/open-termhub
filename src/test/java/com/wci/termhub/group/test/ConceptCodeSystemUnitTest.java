/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.group.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractTerminologyServerTest;

/**
 * Unit tests for concept search functionality with FHIR Code System files.
 */
public class ConceptCodeSystemUnitTest extends AbstractTerminologyServerTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConceptCodeSystemUnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Test finding concepts by terminology.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptsByTerminology() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setQuery("terminology: SNOMEDCT");
    params.setLimit(10);

    final ResultList<Concept> results = searchService.find(params, Concept.class);

    assertFalse(results.getItems().isEmpty(), "Should find at least one concept");
    LOGGER.info("Found {} concepts (limited to 10)", results.getItems().size());
    LOGGER.info("Total count: {}", results.getTotal());
    assertTrue(results.getTotal() == 10,
        "Should not have more than 10 total concepts. Found: " + results.getTotal());

    for (final Concept concept : results.getItems()) {
      assertEquals("SNOMEDCT", concept.getTerminology());
      LOGGER.info("Concept: code={}, name={}", concept.getCode(), concept.getName());
    }
  }

  /**
   * Test finding concepts by code.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptsByCode() throws Exception {
    // Try to find a specific concept by code from SNOMED CT
    final SearchParameters params = new SearchParameters();
    params.setQuery("code:73211009 AND terminology: SNOMEDCT");

    final ResultList<Concept> results = searchService.find(params, Concept.class);

    assertFalse(results.getItems().isEmpty(), "Should find a concept with code 73211009");
    assertEquals("73211009", results.getItems().get(0).getCode());
    LOGGER.info("Found concept: {}", results.getItems().get(0).getName());
  }

  /**
   * Test finding concepts by name.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptsByName() throws Exception {
    // Search for concepts with "diabetes" in their name
    final SearchParameters params = new SearchParameters();
    params.setQuery("name: diabetes AND terminology: SNOMEDCT");
    params.setLimit(10);

    final ResultList<Concept> results = searchService.find(params, Concept.class);

    results.getItems().stream().forEach(r -> LOGGER.info("found concept: {}", r));

    assertFalse(results.getItems().isEmpty(), "Should find diabetes concepts");
    LOGGER.info("Found {} diabetes concepts (limited to 10)", results.getItems().size());

    for (final Concept concept : results.getItems()) {
      assertTrue(concept.getName().toLowerCase().contains("diabetes"),
          "Concept name should contain 'diabetes'");
      LOGGER.info("Diabetes concept: code={}, name={}", concept.getCode(), concept.getName());
    }
  }

  /**
   * Test finding ICD-10-CM concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindIcd10Concepts() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setQuery("terminology: ICD10CM");
    params.setLimit(10);

    final ResultList<Concept> results = searchService.find(params, Concept.class);

    assertFalse(results.getItems().isEmpty(), "Should find ICD-10-CM concepts");
    LOGGER.info("Found {} ICD-10-CM concepts (limited to 10)", results.getItems().size());

    for (final Concept concept : results.getItems()) {
      assertEquals("ICD10CM", concept.getTerminology());
      LOGGER.info("ICD-10-CM concept: code={}, name={}", concept.getCode(), concept.getName());
    }
  }

  /**
   * Test finding LOINC concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindLoincConcepts() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setQuery("terminology: LNC");
    params.setLimit(10);

    final ResultList<Concept> results = searchService.find(params, Concept.class);

    assertFalse(results.getItems().isEmpty(), "Should find LOINC concepts");
    LOGGER.info("Found {} LOINC concepts (limited to 10)", results.getItems().size());

    for (final Concept concept : results.getItems()) {
      assertEquals("LNC", concept.getTerminology());
      LOGGER.info("LOINC concept: code={}, name={}", concept.getCode(), concept.getName());
    }
  }
}
