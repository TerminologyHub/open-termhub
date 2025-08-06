/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r4.test;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractServerTest;
import com.wci.termhub.util.PropertyUtility;

/**
 * Abstract superclass for source code tests.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractFhirR4ServerTest extends AbstractServerTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(AbstractFhirR4ServerTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The index directory. */
  protected static final String INDEX_DIRECTORY = "build/index/lucene-index-r4";

  /** List of FHIR Code System files to load. */
  private static final List<String> CODE_SYSTEM_FILES =
      List.of("CodeSystem-snomedct_us-sandbox-20240301-r4.json",
          "CodeSystem-snomedct-sandbox-20240101-r4.json", "CodeSystem-lnc-sandbox-277-r4.json",
          "CodeSystem-icd10cm-sandbox-2023-r4.json", "CodeSystem-rxnorm-sandbox-04012024-r4.json");

  /** List of FHIR ConceptMap files to load. */
  private static final List<String> CONCEPT_MAP_FILES =
      List.of("ConceptMap-snomedct_us-icd10cm-sandbox-20240301-r4.json");

  /** List of FHIR Code System files to load. */
  private static final List<String> VALUE_SET_FILES =
      List.of("ValueSet-snomedct_us-extension-sandbox-20240301-r4.json");

  /** The setup once. */
  private static boolean setupOnce = false;

  /**
   * Setup once.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public void setupData() throws Exception {
    PropertyUtility.setProperty("lucene.index.directory", INDEX_DIRECTORY);
    if (setupOnce) {
      return;
    }
    clearAndCreateIndexDirectories(searchService, INDEX_DIRECTORY);
    loadCodeSystems(searchService, CODE_SYSTEM_FILES, false);
    loadConceptMaps(searchService, CONCEPT_MAP_FILES);
    loadValueSets(searchService, VALUE_SET_FILES);
    setupOnce = true;
  }

}
