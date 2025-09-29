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

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * Test class for concept search functionality.
 */
public class ConceptSearchUnitTest extends AbstractTerminologyTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(ConceptSearchUnitTest.class);

  /** The Constant SEARCH_PARAMETERS. */
  private static final SearchParameters SEARCH_PARAMETERS = new SearchParameters(1000, 0);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Test find concept icd 10 cm.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptIcd10cm() throws Exception {

    final TermQueryComposer termQuery = new TermQueryComposer("ICD10CM", "2023", "E10", null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery());
    final ResultList<Concept> concept = searchService.find(SEARCH_PARAMETERS, Concept.class);
    assertEquals(1, concept.getItems().size());
    assertEquals(termQuery.getTerminology(), concept.getItems().get(0).getTerminology());
    assertEquals(termQuery.getVersion(), concept.getItems().get(0).getVersion());
    assertEquals(termQuery.getCode(), concept.getItems().get(0).getCode());
  }

  /**
   * Test find concept loinc.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptLoinc() throws Exception {

    final TermQueryComposer termQuery = new TermQueryComposer("LNC", "277", "LA14283-8", null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery());
    final ResultList<Concept> concept = searchService.find(SEARCH_PARAMETERS, Concept.class);
    assertEquals(1, concept.getItems().size());
    assertEquals(termQuery.getTerminology(), concept.getItems().get(0).getTerminology());
    assertEquals(termQuery.getVersion(), concept.getItems().get(0).getVersion());
    assertEquals(termQuery.getCode(), concept.getItems().get(0).getCode());
  }

  /**
   * Test find concept rx norm.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptRxNorm() throws Exception {

    final TermQueryComposer termQuery = new TermQueryComposer("RXNORM", "04012024", "899989", null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery());
    final ResultList<Concept> concept = searchService.find(SEARCH_PARAMETERS, Concept.class);
    assertEquals(1, concept.getItems().size());
    assertEquals(termQuery.getTerminology(), concept.getItems().get(0).getTerminology());
    assertEquals(termQuery.getVersion(), concept.getItems().get(0).getVersion());
    assertEquals(termQuery.getCode(), concept.getItems().get(0).getCode());
  }

  /**
   * Test find concept snomed ct us.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptSnomedCtUs() throws Exception {

    final TermQueryComposer termQuery =
        new TermQueryComposer("SNOMEDCT_US", "20240301", "384719006", null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery());
    final ResultList<Concept> concept = searchService.find(SEARCH_PARAMETERS, Concept.class);
    assertEquals(1, concept.getItems().size());
    assertEquals(termQuery.getTerminology(), concept.getItems().get(0).getTerminology());
    assertEquals(termQuery.getVersion(), concept.getItems().get(0).getVersion());
    assertEquals(termQuery.getCode(), concept.getItems().get(0).getCode());
  }

  /**
   * Test find concept snomed ct.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptSnomedCt() throws Exception {

    final TermQueryComposer termQuery =
        new TermQueryComposer("SNOMEDCT", "20240101", "277302009", null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery());
    final ResultList<Concept> concept = searchService.find(SEARCH_PARAMETERS, Concept.class);
    assertEquals(1, concept.getItems().size());
    assertEquals(termQuery.getTerminology(), concept.getItems().get(0).getTerminology());
    assertEquals(termQuery.getVersion(), concept.getItems().get(0).getVersion());
    assertEquals(termQuery.getCode(), concept.getItems().get(0).getCode());
  }

}
