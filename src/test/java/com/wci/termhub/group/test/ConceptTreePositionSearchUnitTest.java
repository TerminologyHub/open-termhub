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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptTreePosition;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractTerminologyTest;
import com.wci.termhub.util.StringUtility;

/**
 * Test class for concept tree position search functionality.
 */
public class ConceptTreePositionSearchUnitTest extends AbstractTerminologyTest {

  /** The logger. */
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ConceptTreePositionSearchUnitTest.class);

  /** The Constant SEARCH_PARAMETERS. */
  private static final SearchParameters SEARCH_PARAMETERS = new SearchParameters(100000, 0);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Test log all concept tree positions.
   *
   * @throws Exception the exception
   */
  // @Test // for debugging
  public void testLogAllConceptTreePositions() throws Exception {

    SEARCH_PARAMETERS.setQuery("*:*");
    LOGGER.info("testLogAllConceptTreePositions 0 Query: {}", SEARCH_PARAMETERS.getQuery());
    final ResultList<ConceptTreePosition> conceptTreePositions0 =
        searchService.find(SEARCH_PARAMETERS, ConceptTreePosition.class);
    for (final ConceptTreePosition ctp : conceptTreePositions0.getItems()) {
      LOGGER.info("US terminology:{}, code:{}, ancestorPath:{} ", ctp.getTerminology(),
          ctp.getConcept().getCode(), ctp.getAncestorPath());
    }

    // List<String> clauseList =
    // Arrays.asList("terminology:SNOMEDCT_US", "publisher:SANDBOX", "version:"
    // +
    // StringUtility.escapeQuery("http://snomed.info/sct/731000124108/version/20240301"));
    // SEARCH_PARAMETERS.setQuery(StringUtility.composeQuery("AND",
    // clauseList));
    // LOGGER.info("testLogAllConceptTreePositions 1 Query: {}",
    // SEARCH_PARAMETERS.getQuery());
    // final ResultList<ConceptTreePosition> conceptTreePositions1 =
    // searchService.find(SEARCH_PARAMETERS, ConceptTreePosition.class);
    // for (final ConceptTreePosition ctp : conceptTreePositions1.getItems()) {
    // LOGGER.info("US terminology:{}, code:{}, ancestorPath:{} ",
    // ctp.getTerminology(),
    // ctp.getConcept().getCode(), ctp.getAncestorPath());
    // }
    //
    // clauseList = Arrays.asList("terminology:SNOMEDCT", "publisher:SANDBOX",
    // "version:"
    // +
    // StringUtility.escapeQuery("http://snomed.info/sct/900000000000207008/version/20240101"));
    // SEARCH_PARAMETERS.setQuery(StringUtility.composeQuery("AND",
    // clauseList));
    // LOGGER.info("testLogAllConceptTreePositions 2 Query: {}",
    // SEARCH_PARAMETERS.getQuery());
    // final ResultList<ConceptTreePosition> conceptTreePositions2 =
    // searchService.find(SEARCH_PARAMETERS, ConceptTreePosition.class);
    // for (final ConceptTreePosition ctp : conceptTreePositions2.getItems()) {
    // LOGGER.info("INT terminology:{}, code:{}, ancestorPath:{} ",
    // ctp.getTerminology(),
    // ctp.getConcept().getCode(), ctp.getAncestorPath());
    // }

  }

  /**
   * Test find concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConcept() throws Exception {

    final TermQueryComposer termQuery =
        new TermQueryComposer("SNOMEDCT_US", "20240301", "52988006", null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery() + " AND publisher:SANDBOX");
    LOGGER.info("testFindConcept Query: {}", SEARCH_PARAMETERS.getQuery());
    final ResultList<Concept> concepts = searchService.find(SEARCH_PARAMETERS, Concept.class);
    assertEquals(1, concepts.getItems().size());
  }

  /**
   * Test find concept tree position for SNOMEDCT_US concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptTreePositions() throws Exception {

    final TermQueryComposer termQuery =
        new TermQueryComposer("SNOMEDCT_US", "20240301", null, null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery() + " AND publisher:SANDBOX");
    LOGGER.info("testFindConceptTreePositions Query: {}", SEARCH_PARAMETERS.getQuery());
    final ResultList<ConceptTreePosition> conceptTreePositions =
        searchService.find(SEARCH_PARAMETERS, ConceptTreePosition.class);
    assertTrue(conceptTreePositions.getItems().size() > 10);
    for (final ConceptTreePosition ctp : conceptTreePositions.getItems()) {
      assertEquals(termQuery.getTerminology(), ctp.getTerminology());
      assertEquals(termQuery.getVersion(), ctp.getVersion());
      assertEquals(termQuery.getVersion(), ctp.getVersion());
      assertEquals("SANDBOX", ctp.getPublisher());

    }

  }

  /**
   * Test find concept tree position for SNOMEDCT_US concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptTreePositionsForCode() throws Exception {

    final TermQueryComposer termQuery =
        new TermQueryComposer("SNOMEDCT_US", "20240301", null, null);
    SEARCH_PARAMETERS
        .setQuery(termQuery.getQuery() + " AND publisher:SANDBOX AND concept.code:52988006");
    LOGGER.info("testFindConceptTreePositionsForCode Query: {}", SEARCH_PARAMETERS.getQuery());
    final ResultList<ConceptTreePosition> conceptTreePositions =
        searchService.find(SEARCH_PARAMETERS, ConceptTreePosition.class);
    assertEquals(1, conceptTreePositions.getItems().size());
    for (final ConceptTreePosition ctp : conceptTreePositions.getItems()) {
      assertEquals(termQuery.getTerminology(), ctp.getTerminology());
      assertEquals(termQuery.getVersion(), ctp.getVersion());
      assertEquals(termQuery.getVersion(), ctp.getVersion());
      assertEquals("SANDBOX", ctp.getPublisher());
      assertEquals("52988006", ctp.getConcept().getCode());
      assertNotNull(ctp.getAncestorPath());
    }

  }

  /**
   * Test find concept tree position for SNOMEDCT_US concept.
   *
   * @throws Exception the exception
   */
  // @Test
  public void testFindConceptTreePositionsWithEmptyAncestor() throws Exception {

    final TermQueryComposer termQuery =
        new TermQueryComposer("SNOMEDCT_US", "20240301", null, null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery() + " publisher:SANDBOX AND concept.code:52988006"
        + " AND ancestorPath:\"\"");
    LOGGER.info("testFindConceptTreePositionsWithEmptyAncestor Query: {}",
        SEARCH_PARAMETERS.getQuery());
    LOGGER.info("testFindConceptTreePositionsWithEmptyAncestor Query: {}",
        SEARCH_PARAMETERS.getQuery());
    final ResultList<ConceptTreePosition> conceptTreePositions =
        searchService.find(SEARCH_PARAMETERS, ConceptTreePosition.class);
    assertEquals(1, conceptTreePositions.getItems().size());
    for (final ConceptTreePosition ctp : conceptTreePositions.getItems()) {
      assertEquals(termQuery.getTerminology(), ctp.getTerminology());
      assertEquals(termQuery.getVersion(), ctp.getVersion());
      assertEquals(termQuery.getVersion(), ctp.getVersion());
      assertEquals("SANDBOX", ctp.getPublisher());
      assertEquals("20240301", ctp.getVersion());
      assertEquals(null, ctp.getAncestorPath());
    }

  }

  /**
   * Test find concept tree position for SNOMEDCT_US concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptTreePositionsWithAncestor() throws Exception {

    final String ancestorPath = "138875005~123037004~118956008~49755003";
    final TermQueryComposer termQuery =
        new TermQueryComposer("SNOMEDCT_US", "20240301", null, null);
    final String ancestorPathEscaped = StringUtility.escapeQuery(ancestorPath);
    SEARCH_PARAMETERS
        .setQuery(termQuery.getQuery() + " AND publisher:SANDBOX AND concept.code:52988006"
            + " AND ancestorPath:" + ancestorPathEscaped);

    LOGGER.info("testFindConceptTreePositionsWithAncestor Query: {}", SEARCH_PARAMETERS.getQuery());
    final ResultList<ConceptTreePosition> conceptTreePositions =
        searchService.find(SEARCH_PARAMETERS, ConceptTreePosition.class);
    assertEquals(1, conceptTreePositions.getItems().size());
    for (final ConceptTreePosition ctp : conceptTreePositions.getItems()) {
      assertEquals("SNOMEDCT_US", ctp.getTerminology());
      assertEquals("SANDBOX", ctp.getPublisher());
      assertEquals("20240301", ctp.getVersion());
      assertEquals(ancestorPath, ctp.getAncestorPath());
    }

  }

}
