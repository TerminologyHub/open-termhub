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

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.Application;
import com.wci.termhub.model.HasId;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.ModelUtility;

/**
 * The Class CleanupUnitTest.
 */
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class CleanupUnitTest {

  /** The Constant LOG. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CleanupUnitTest.class);

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

    LOGGER.info("Cleaning up indexes");
    final List<Class<? extends HasId>> indexedObjects = ModelUtility.getIndexedObjects();
    for (final Class<? extends HasId> clazz : indexedObjects) {
      searchService.deleteIndex(clazz);
    }
  }
}
