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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.Application;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * The Class CleanupUnitTest.
 */
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class CleanupUnitTest {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CleanupUnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Tear down.
   *
   * @throws Exception the exception
   */
  /**
   * Tear down.
   *
   * @throws Exception the exception
   */
  @AfterAll
  public void tearDown() throws Exception {

    LOG.info("Cleaning up indexes");
    searchService.deleteIndex(Terminology.class);
    searchService.deleteIndex(Metadata.class);
    searchService.deleteIndex(Concept.class);
    searchService.deleteIndex(Term.class);
    searchService.deleteIndex(ConceptRelationship.class);
  }
}
