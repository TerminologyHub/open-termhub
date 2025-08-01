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

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.Application;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.HasId;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.BaseUnitTest;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.PropertyUtility;

/**
 * Test class for loading FHIR Code System files.
 */
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(OrderAnnotation.class)
@Order(1)
public class CodeSystemLoadUnitTest extends BaseUnitTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CodeSystemLoadUnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** List of FHIR Code System files to load. */
  private static final List<String> CODE_SYSTEM_FILES =
      List.of("CodeSystem-snomedct_us-sandbox-20240301-r5.json",
          "CodeSystem-snomedct-sandbox-20240101-r5.json", "CodeSystem-lnc-sandbox-277-r5.json",
          "CodeSystem-icd10cm-sandbox-2023-r5.json", "CodeSystem-rxnorm-sandbox-04012024-r5.json");

  /** The Constant CONCEPT_MAP_FILES. */
  private static final List<String> CONCEPT_MAP_FILES =
      List.of("ConceptMap-snomedct_us-icd10cm-sandbox-20240301-r5.json");

  /** The Constant VALUE_SET_FILES. */
  private static final List<String> VALUE_SET_FILES =
      List.of("ValueSet-snomedct_us-723264001-sandbox-20240301-r5.json",
          "ValueSet-snomedct_us-core-sandbox-20240301-r5.json",
          "ValueSet-snomedct_us-extension-sandbox-20240301-r5.json",
          "ValueSet-snomedct_us-model-sandbox-20240301-r5.json");

  /**
   * Setup - load the FHIR Code System files.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public void setup() throws Exception {

    // Use File instead of Path to avoid colon issues in directory paths
    final String indexDirPath =
        PropertyUtility.getProperties().getProperty("lucene.index.directory");

    // Delete all indexes for a fresh start
    LOGGER.info("Deleting existing indexes from directory: {}", indexDirPath);
    final File indexDir = new File(indexDirPath);
    LOGGER.info("Does this exist? {}", indexDir.exists());

    if (indexDir.exists()) {
      FileUtils.deleteDirectory(indexDir);
    }

    final List<Class<? extends HasId>> indexedObjects = ModelUtility.getIndexedObjects();
    for (final Class<? extends HasId> clazz : indexedObjects) {
      searchService.deleteIndex(clazz);
      searchService.createIndex(clazz);
    }

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
      LOGGER.info("Found terminology: {} ({}), version: {}, publisher: {}", term.getName(),
          term.getAbbreviation(), term.getVersion(), term.getPublisher());

      // Verify concepts are loaded for this terminology
      final SearchParameters conceptParams = new SearchParameters();
      conceptParams.setQuery("terminology:" + term.getAbbreviation());
      conceptParams.setLimit(1);

      final ResultList<Concept> concepts = searchService.find(conceptParams, Concept.class);
      LOGGER.info("Found {} concepts for terminology {}", concepts.getTotal(),
          term.getAbbreviation());

      assertTrue(concepts.getTotal() > 0,
          "Should have found concepts for terminology " + term.getAbbreviation());
    }
  }
}
