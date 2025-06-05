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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.Application;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Term;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * The Class TermSearchUnitTest.
 */
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class TermSearchUnitTest extends BaseUnitTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TermSearchUnitTest.class);

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

    final TermQueryComposer termQuery = new TermQueryComposer("ICD10CM", "2023", "C50", null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery());
    final ResultList<Term> terms = searchService.find(SEARCH_PARAMETERS, Term.class);
    assertEquals(4, terms.getItems().size());
    terms.getItems().forEach(term -> {
      assertEquals(termQuery.getCode(), term.getCode());
      assertEquals(termQuery.getTerminology(), term.getTerminology());
      assertEquals(termQuery.getVersion(), term.getVersion());
    });
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
    final ResultList<Term> terms = searchService.find(SEARCH_PARAMETERS, Term.class);
    assertEquals(1, terms.getItems().size());
    terms.getItems().forEach(term -> {
      assertEquals(termQuery.getCode(), term.getCode());
      assertEquals(termQuery.getTerminology(), term.getTerminology());
      assertEquals(termQuery.getVersion(), term.getVersion());
    });
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
    final ResultList<Term> terms = searchService.find(SEARCH_PARAMETERS, Term.class);
    assertEquals(5, terms.getItems().size());
    terms.getItems().forEach(term -> {
      assertEquals(termQuery.getCode(), term.getCode());
      assertEquals(termQuery.getTerminology(), term.getTerminology());
      assertEquals(termQuery.getVersion(), term.getVersion());
    });

  }

  /**
   * Test find concept snomed ct us.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptSnomedCtUs() throws Exception {

    final TermQueryComposer termQuery = new TermQueryComposer("SNOMEDCT_US",
        "http://snomed.info/sct/731000124108/version/20240301", "384719006", null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery());
    final ResultList<Term> terms = searchService.find(SEARCH_PARAMETERS, Term.class);
    assertEquals(2, terms.getItems().size());
    terms.getItems().forEach(term -> {
      assertEquals(termQuery.getCode(), term.getCode());
      assertEquals(termQuery.getTerminology(), term.getTerminology());
      assertEquals(termQuery.getVersion(), term.getVersion());
    });

  }

  /**
   * Test find concept snomed ct.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptSnomedCt() throws Exception {

    final TermQueryComposer termQuery = new TermQueryComposer("SNOMEDCT",
        "http://snomed.info/sct/900000000000207008/version/20240101", "277302009", null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery());
    final ResultList<Term> terms = searchService.find(SEARCH_PARAMETERS, Term.class);
    assertEquals(2, terms.getItems().size());
    terms.getItems().forEach(term -> {
      assertEquals(termQuery.getCode(), term.getCode());
      assertEquals(termQuery.getTerminology(), term.getTerminology());
      assertEquals(termQuery.getVersion(), term.getVersion());
    });

  }

  /**
   * Test find concept by term.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptByTermName() throws Exception {

    final String term = "Procedure on gastrointestinal tract";
    final String terminology = "SNOMEDCT_US";
    final String version = "http://snomed.info/sct/731000124108/version/20240301";
    final TermQueryComposer termQuery = new TermQueryComposer(terminology, version, null, term);
    final SearchParameters sp = new SearchParameters(termQuery.getQuery(), 100, 0);

    final ResultList<Term> results = searchService.find(sp, Term.class);

    for (final Term t : results.getItems()) {
      LOGGER.info("testFindConceptByTermName found {}", t.toString());
    }

    // check that each concept has at least a term has the word 'Procedure on
    // gastrointestinal tract' in it
    results.getItems().forEach(t -> {
      assertTrue(t.getName().toLowerCase().contains(term));
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
    final String terminology = "SNOMEDCT_US";
    final String version = "http://snomed.info/sct/731000124108/version/20240301";
    final TermQueryComposer termQuery = new TermQueryComposer(terminology, version, null, term);

    final SearchParameters sp = new SearchParameters(termQuery.getQuery(), 10, 0);
    final List<String> terms = new ArrayList<>();

    ResultList<Term> results = searchService.find(sp, Term.class);
    assertEquals(10, results.getItems().size());
    results.getItems().forEach(t -> {
      assertEquals(terminology, t.getTerminology());
      assertEquals(version, t.getVersion());
      assertFalse(terms.contains(t.getCode() + "_" + t.getName()));
      terms.add(t.getCode() + "_" + t.getName());
    });

    sp.setOffset(10);
    results = searchService.find(sp, Term.class);
    LOGGER.info("result size:{}", results.getItems().size());
    assertEquals(10, results.getItems().size());
    results.getItems().forEach(t -> {
      LOGGER.info("term:{}", t);
      assertEquals(terminology, t.getTerminology());
      assertEquals(version, t.getVersion());
      assertFalse(terms.contains(t.getCode() + "_" + t.getName()));
      terms.add(t.getCode() + "_" + t.getName());
    });

    results.getItems().forEach(t -> {
      terms.add(t.getName());
    });

    sp.setOffset(20);
    results = searchService.find(sp, Term.class);
    assertEquals(6, results.getItems().size());
    results.getItems().forEach(t -> {
      assertEquals(terminology, t.getTerminology());
      assertEquals(version, t.getVersion());
      assertFalse(terms.contains(t.getCode() + "_" + t.getName()));
      terms.add(t.getCode() + "_" + t.getName());
    });
  }

}
