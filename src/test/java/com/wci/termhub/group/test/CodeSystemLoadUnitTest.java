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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractTerminologyTest;

/**
 * Test class for loading FHIR Code System files.
 */
public class CodeSystemLoadUnitTest extends AbstractTerminologyTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CodeSystemLoadUnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Test verify data loading.
   *
   * @throws Exception the exception
   */
  @Test
  public void testVerifyDataLoading() throws Exception {
    // Verify terminologies are loaded
    final SearchParameters params = new SearchParameters();
    params.setQuery("*:*");
    params.setLimit(100);

    final ResultList<Terminology> terminologies = searchService.find(params, Terminology.class);
    LOGGER.info("Found {} terminologies", terminologies.getItems().size());

    assertFalse(terminologies.getItems().isEmpty(), "Should have found terminologies");

    // Log details of each terminology
    for (final Terminology term : terminologies.getItems()) {

      if ("SANDBOX".equals(term.getPublisher()) || "SNOMEDCT_US".equals(term.getAbbreviation())) {
        continue;
      }
      LOGGER.info("Found terminology: {} ({}), version: {}, publisher: {}", term.getName(),
          term.getAbbreviation(), term.getVersion(), term.getPublisher());

      // Verify concepts are loaded for this terminology
      final SearchParameters conceptParams = new SearchParameters();
      conceptParams.setQuery("terminology:" + term.getAbbreviation());
      conceptParams.setLimit(1);

      final ResultList<Concept> concepts = searchService.find(conceptParams, Concept.class);
      LOGGER.info("Found {} concepts for terminology {}", concepts.getTotal(),
          term.getAbbreviation());
      assertTrue(concepts.getTotal() == 0,
          "Should not have found concepts for terminology " + term.getAbbreviation());
    }
  }
}
