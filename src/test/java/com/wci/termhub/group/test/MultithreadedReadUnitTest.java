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

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractTerminologyServerTest;
import com.wci.termhub.util.StringUtility;

/**
 * The Class MultithreadedReadUnitTest.
 */
public class MultithreadedReadUnitTest extends AbstractTerminologyServerTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(MultithreadedReadUnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The Constant SEARCH_PARAMETERS. */
  private static final SearchParameters SEARCH_PARAMETERS = new SearchParameters(10, 0);

  /**
   * Test.
   *
   * @throws Exception the exception
   */
  @Test
  public void test() throws Exception {

    final AtomicInteger matchCount = new AtomicInteger(0);
    final AtomicInteger missCount = new AtomicInteger(0);

    // get all the concept codes from the database
    final ResultList<Concept> allConcepts =
        searchService.findAll(new SearchParameters(5000, 0), Concept.class);

    LOGGER.info("Found {} concepts", allConcepts.getItems().size());
    assertFalse(allConcepts.getItems().isEmpty());

    final String queryTemplate = "terminology:%s AND version:%s AND code:%s";

    // final ExecutorService executor = new ThreadPoolExecutor(1, 1, 0L,
    // TimeUnit.MILLISECONDS,
    // new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.CallerRunsPolicy());
    //
    for (final Concept concept : allConcepts.getItems()) {
      // executor.submit(new Runnable() {
      // @Override
      // public void run() {

      try {
        SEARCH_PARAMETERS.setQuery(
            String.format(queryTemplate, StringUtility.escapeQuery(concept.getTerminology()),
                StringUtility.escapeQuery(concept.getVersion()),
                StringUtility.escapeQuery(concept.getCode())));
        LOGGER.info("Query is {}", SEARCH_PARAMETERS.getQuery());

        final ResultList<Concept> searchResults =
            searchService.find(SEARCH_PARAMETERS, Concept.class);
        final int totalFound = searchResults.getItems().size();

        if (totalFound == 1 && searchResults.getItems().get(0).equals(concept)) {
          matchCount.incrementAndGet();
        } else {
          final Concept foundConcept = searchResults.getItems().get(0);
          LOGGER.info("Miss - concept {}:{}:{} does not match {}:{}:{}", concept.getTerminology(),
              concept.getVersion(), concept.getCode(), foundConcept.getTerminology(),
              foundConcept.getVersion(), foundConcept.getCode());
          missCount.incrementAndGet();
        }

      } catch (final Exception e) {
        LOGGER.error("Miss - error while fetching concept for {}", SEARCH_PARAMETERS.getQuery(), e);
        missCount.incrementAndGet();
      }
    }
    // });
    // }
    //
    // executor.shutdown();
    // executor.awaitTermination(10, TimeUnit.SECONDS);

    LOGGER.info("Match count is {}", matchCount.get());
    LOGGER.info("Miss count is {}", missCount.get());

    // count match or miss matches
    assertTrue(matchCount.get() > 0);
    assertEquals(0, missCount.get());

  }

}
