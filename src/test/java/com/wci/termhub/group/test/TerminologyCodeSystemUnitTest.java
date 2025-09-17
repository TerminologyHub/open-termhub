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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractTerminologyTest;
import com.wci.termhub.util.StringUtility;

/**
 * Test class for terminology code system operations.
 */
public class TerminologyCodeSystemUnitTest extends AbstractTerminologyTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TerminologyCodeSystemUnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Test finding all terminologies.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindAllTerminologies() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setQuery("*:*");
    params.setLimit(100);

    final ResultList<Terminology> results = searchService.find(params, Terminology.class);

    LOGGER.info("Found {} terminologies", results.getItems().size());

    // Verify we have loaded at least the 4 terminologies from our Code System
    // files
    assertTrue(results.getItems().size() >= 4, "Expected at least 4 terminologies");

    // Log the terminologies found
    for (final Terminology term : results.getItems()) {
      LOGGER.info("Found terminology: {}, publisher: {}, version: {}", term.getAbbreviation(),
          term.getPublisher(), term.getVersion());
    }
  }

  /**
   * Test finding terminology by abbreviation.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindTerminologyByAbbreviation() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setQuery("abbreviation:SNOMEDCT_US");

    final ResultList<Terminology> results = searchService.find(params, Terminology.class);

    assertFalse(results.getItems().isEmpty(), "Should find at least one terminology");
    assertEquals("SNOMEDCT_US", results.getItems().get(0).getAbbreviation());
    assertEquals("SANDBOX", results.getItems().get(0).getPublisher());
  }

  /**
   * Test finding terminology by publisher.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindTerminologyByPublisher() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setQuery("publisher:SANDBOX");

    final ResultList<Terminology> results = searchService.find(params, Terminology.class);

    assertFalse(results.getItems().isEmpty(), "Should find at least one terminology");
    for (final Terminology term : results.getItems()) {
      assertEquals("SANDBOX", term.getPublisher());
    }
  }

  /**
   * Test finding terminology by version.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindTerminologyByVersion() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setQuery("version:" + StringUtility.escapeQuery("20240301"));

    final ResultList<Terminology> results = searchService.find(params, Terminology.class);

    assertFalse(results.getItems().isEmpty(), "Should find at least one terminology");
    for (final Terminology term : results.getItems()) {
      assertTrue(term.getVersion().contains("20240301"));
    }
  }

  /**
   * Test checking URI value of terminology.
   *
   * @throws Exception the exception
   */
  @Test
  public void testTerminologyUri() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setQuery("abbreviation:ICD10CM");

    final ResultList<Terminology> results = searchService.find(params, Terminology.class);

    assertFalse(results.getItems().isEmpty(), "Should find at least one terminology");
    assertNotNull(results.getItems().get(0).getUri(), "URI should not be null");
    LOGGER.info("ICD-10-CM URI: {}", results.getItems().get(0).getUri());
  }

}
