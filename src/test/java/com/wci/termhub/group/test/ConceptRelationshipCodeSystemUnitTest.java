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
import com.wci.termhub.test.AbstractTerminologyTest;

/**
 * Unit tests for concept relationship functionality with FHIR Code System files.
 */
public class ConceptRelationshipCodeSystemUnitTest extends AbstractTerminologyTest {

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
    params.setQuery("terminology:SNOMEDCT_US AND type:isa");
    params.setLimit(10);

    final ResultList<ConceptRelationship> results =
        searchService.find(params, ConceptRelationship.class);

    results.getItems().forEach(rel -> LOGGER.info("FOUND: {}", rel));

    assertFalse(results.getItems().isEmpty(), "Should find parent relationships");
    LOGGER.info("Found {} ISA relationships (limited to 10)", results.getItems().size());

    for (final ConceptRelationship rel : results.getItems()) {
      assertEquals("isa", rel.getType());
      assertEquals("SNOMEDCT_US", rel.getTerminology());

    }

  }

  @Test
  public void testFindRelationshipsForConcept2() throws Exception {
    // Use a known SNOMED CT concept code (COVID-19)
    final String conceptCode = "840539006";

    final SearchParameters params = new SearchParameters();
    params.setQuery("terminology:SNOMEDCT_US AND from.code:" + conceptCode);

    final ResultList<ConceptRelationship> results =
        searchService.find(params, ConceptRelationship.class);

    assertTrue(!results.getItems().isEmpty(),
        "Expected at least one relationship for concept " + conceptCode);

    results.getItems().forEach(rel -> LOGGER.info("FOUND: {}", rel));

    // Verify that we have the expected number of relationships
    assertEquals(3, results.getItems().size(), "Three relationships expected for concept "
        + conceptCode + " Found: " + results.getItems().size());

    // Check for the three specific relationships
    boolean foundIsaRelationship = false;
    boolean foundPathologicalProcess = false;
    boolean foundCausativeAgent = false;

    for (final ConceptRelationship rel : results.getItems()) {
      assertEquals(conceptCode, rel.getFrom().getCode());
      assertEquals("SNOMEDCT_US", rel.getTerminology());
      assertEquals("20240301", rel.getVersion());
      assertEquals("SANDBOX", rel.getPublisher());

      LOGGER.info("Relationship: type={}, from={}, to={}, additionalType={}", rel.getType(),
          rel.getFrom().getCode(), rel.getTo().getCode(), rel.getAdditionalType());

      // Check for ISA relationship (parent)
      if ("isa".equals(rel.getType()) && "116680003".equals(rel.getAdditionalType())) {
        assertEquals("186747009", rel.getTo().getCode(),
            "ISA relationship should point to Coronavirus infection");
        assertEquals("Coronavirus infection", rel.getTo().getName());
        assertTrue(rel.getHierarchical(), "ISA relationship should be hierarchical");
        foundIsaRelationship = true;
      }

      // Check for Pathological process relationship
      if ("relationship".equals(rel.getType()) && "370135005".equals(rel.getAdditionalType())) {
        assertEquals("441862004", rel.getTo().getCode(),
            "Pathological process relationship should point to Infectious process");
        assertEquals("Infectious process", rel.getTo().getName());
        assertFalse(rel.getHierarchical(), "Non-ISA relationship should not be hierarchical");
        foundPathologicalProcess = true;
      }

      // Check for Causative agent relationship
      if ("relationship".equals(rel.getType()) && "246075003".equals(rel.getAdditionalType())) {
        assertEquals("840533007", rel.getTo().getCode(),
            "Causative agent relationship should point to SARS-CoV-2");
        assertEquals("SARS-CoV-2", rel.getTo().getName());
        assertFalse(rel.getHierarchical(), "Non-ISA relationship should not be hierarchical");
        foundCausativeAgent = true;
      }
    }

    // Verify all three relationships were found
    assertTrue(foundIsaRelationship,
        "Should find ISA relationship (type='isa', additionalType='116680003')");
    assertTrue(foundPathologicalProcess,
        "Should find Pathological process relationship (type='other', additionalType='370135005')");
    assertTrue(foundCausativeAgent,
        "Should find Causative agent relationship (type='other', additionalType='246075003')");
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
    // Diabetes mellitus
    final String fromConceptCode = "73211009";

    // Find parent concepts (concepts that this concept is a child of)
    final SearchParameters parentsParams = new SearchParameters();
    parentsParams
        .setQuery("terminology:SNOMEDCT_US AND from.code:" + fromConceptCode + " AND type:isa");

    final ResultList<ConceptRelationship> parentRels =
        searchService.find(parentsParams, ConceptRelationship.class);

    assertTrue(!parentRels.getItems().isEmpty(),
        "Expected at least one relationship for concept " + fromConceptCode);

    LOGGER.info("Found {} parent relationships for concept {}", parentRels.getItems().size(),
        fromConceptCode);

    for (final ConceptRelationship rel : parentRels.getItems()) {
      assertEquals(fromConceptCode, rel.getFrom().getCode());
      assertEquals("isa", rel.getType());
      LOGGER.info("Parent concept: {}", rel);
    }

    final String toConceptCode = "126877002";
    // Find child concepts (concepts that are children of this concept)
    final SearchParameters childrenParams = new SearchParameters();
    childrenParams
        .setQuery("terminology:SNOMEDCT_US AND to.code:" + toConceptCode + " AND type:isa");
    childrenParams.setLimit(10);

    final ResultList<ConceptRelationship> childRels =
        searchService.find(childrenParams, ConceptRelationship.class);

    assertTrue(!childRels.getItems().isEmpty(),
        "Expected at least one relationship for concept " + toConceptCode);

    LOGGER.info("Found {} child relationships for concept {} (limited to 10)",
        childRels.getItems().size(), toConceptCode);

    for (final ConceptRelationship rel : childRels.getItems()) {
      assertEquals(toConceptCode, rel.getTo().getCode());
      assertEquals("isa", rel.getType());
      LOGGER.info("Child concept: {}", rel.getFrom().getCode());
    }
  }
}
