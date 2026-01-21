/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.integrationtest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractServerTest;

/**
 * Abstract superclass for source code tests.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-syndication-test.properties", properties = {
    "lucene.index.directory=build/index/lucene-syndication"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractSyndicationIntegrationTest extends AbstractServerTest {

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(AbstractSyndicationIntegrationTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The index directory. */
  @Value("${lucene.index.directory}")
  private String indexDirectory;

  /** The setup once. */
  private static boolean setupOnce = false;

  /**
   * Setup once.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public void setupData() throws Exception {
    if (setupOnce) {
      return;
    }
    try {
      clearAndCreateIndexDirectories(searchService, indexDirectory);
    } catch (final Exception e) {
      logger.error("Error setting up data: {}", e.getMessage(), e);
      throw e;
    }
    setupOnce = true;
  }

}
