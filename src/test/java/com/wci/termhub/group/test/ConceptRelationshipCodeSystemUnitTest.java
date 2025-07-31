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

import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractTerminologyServerTest;

/**
 * Unit tests for concept relationship functionality with FHIR Code System
 * files.
 */
public class ConceptRelationshipCodeSystemUnitTest extends AbstractTerminologyServerTest {

  /** The logger. */
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ConceptRelationshipCodeSystemUnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Test finding concept relationships by terminology.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindRelationshipsByTerminology() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setQuery("terminology: SNOMEDCT_US");
    params.setLimit(10);

    final ResultList<ConceptRelationship> results =
        searchService.find(params, ConceptRelationship.class);

    assertFalse(results.getItems().isEmpty(), "Should find concept relationships");
    LOGGER.info("Found {} relationships (limited to 10)", results.getItems().size());
    LOGGER.info("Total relationships: {}", results.getTotal());

    for (final ConceptRelationship rel : results.getItems()) {
      assertEquals("SNOMEDCT_US", rel.getTerminology());
      LOGGER.info("Relationship: type={}, from={}, to={}", rel.getType(), rel.getFrom().getCode(),
          rel.getTo().getCode());
    }
  }

  /**
   * Test finding IS-A relationships.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindIsaRelationships() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setQuery("terminology:SNOMEDCT_US AND additionalType:ISA");
    params.setLimit(10);

    final ResultList<ConceptRelationship> results =
        searchService.find(params, ConceptRelationship.class);

    assertFalse(results.getItems().isEmpty(), "Should find ISA relationships");
    LOGGER.info("Found {} ISA relationships (limited to 10)", results.getItems().size());

    for (final ConceptRelationship rel : results.getItems()) {
      assertEquals("ISA", rel.getAdditionalType());
      assertEquals("SNOMEDCT_US", rel.getTerminology());

    }

  }

  /**
   * Test finding relationships for a specific concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindRelationshipsForConcept() throws Exception {
    // Use a known SNOMED CT concept code (Diabetes mellitus)
    final String conceptCode = "73211009";

    final SearchParameters params = new SearchParameters();
    params.setQuery("terminology:SNOMEDCT_US AND from.code:" + conceptCode);

    final ResultList<ConceptRelationship> results =
        searchService.find(params, ConceptRelationship.class);

    assertTrue(!results.getItems().isEmpty(),
        "Expected at least one relationship for concept " + conceptCode);

    for (final ConceptRelationship rel : results.getItems()) {
      assertEquals(conceptCode, rel.getFrom().getCode());
      LOGGER.info("Relationship: type={}, from={}, to={}, additionslType={}", rel.getType(),
          rel.getFrom().getCode(), rel.getTo().getCode(), rel.getAdditionalType());
    }
  }

  /**
   * Test finding parent-child relationships for a concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindParentChildRelationships() throws Exception {
    // Use a known SNOMED CT concept code
    final String fromConceptCode = "73211009"; // Diabetes mellitus

    // Find parent concepts (concepts that this concept is a child of)
    final SearchParameters parentsParams = new SearchParameters();
    parentsParams.setQuery(
        "terminology:SNOMEDCT_US AND from.code:" + fromConceptCode + " AND additionalType:ISA");

    final ResultList<ConceptRelationship> parentRels =
        searchService.find(parentsParams, ConceptRelationship.class);

    assertTrue(!parentRels.getItems().isEmpty(),
        "Expected at least one relationship for concept " + fromConceptCode);

    LOGGER.info("Found {} parent relationships for concept {}", parentRels.getItems().size(),
        fromConceptCode);

    for (final ConceptRelationship rel : parentRels.getItems()) {
      assertEquals(fromConceptCode, rel.getFrom().getCode());
      assertEquals("parent", rel.getType());
      LOGGER.info("Parent concept: {}", rel);
    }

    final String toConceptCode = "126877002";
    // Find child concepts (concepts that are children of this concept)
    final SearchParameters childrenParams = new SearchParameters();
    childrenParams.setQuery(
        "terminology:SNOMEDCT_US AND to.code:" + toConceptCode + " AND additionalType:ISA");
    childrenParams.setLimit(10);

    final ResultList<ConceptRelationship> childRels =
        searchService.find(childrenParams, ConceptRelationship.class);

    assertTrue(!childRels.getItems().isEmpty(),
        "Expected at least one relationship for concept " + toConceptCode);

    LOGGER.info("Found {} child relationships for concept {} (limited to 10)",
        childRels.getItems().size(), toConceptCode);

    for (final ConceptRelationship rel : childRels.getItems()) {
      assertEquals(toConceptCode, rel.getTo().getCode());
      assertEquals("parent", rel.getType());
      LOGGER.info("Child concept: {}", rel.getFrom().getCode());
    }
  }
}
