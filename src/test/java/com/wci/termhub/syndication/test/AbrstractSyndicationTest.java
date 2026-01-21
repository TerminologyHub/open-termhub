/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.Application;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractServerTest;

/**
 * The Class SyndicationTestRunner.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:application-syndication-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbrstractSyndicationTest extends AbstractServerTest {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(AbrstractSyndicationTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Initialize indexes.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public void initializeIndexes() throws Exception {
    LOGGER.info("Initializing syndication test runner indexes");
    clearAndCreateIndexDirectories(searchService, "build/index/lucene-syndication");
  }

  /**
   * Shutdown.
   */
  @AfterAll
  public void shutdown() {
    LOGGER.info("Completed syndication test runner execution");
  }
}
