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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
import com.wci.termhub.model.ConceptTreePosition;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.TerminologyLoaderUtility;

/**
 * The Class LoadUnitTest.
 */
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class LoadUnitTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(LoadUnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Test create index.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public void setup() throws Exception {

    final Map<String, Boolean> terminologies =
        Map.of("icd10cm-nlm-2023", false, "lnc-nlm-277", false, "rxnorm-nlm-02052024", false,
            "snomedct_us-sandbox-20240301", true, "snomedct-sandbox-20240101", true);

    final String rootDir =
        Paths.get(getClass().getClassLoader().getResource("data").toURI()).toString() + "/";

    // delete all indexes for a fresh start
    searchService.deleteIndex(Terminology.class);
    searchService.deleteIndex(Metadata.class);
    searchService.deleteIndex(Concept.class);
    searchService.deleteIndex(Term.class);
    searchService.deleteIndex(ConceptRelationship.class);
    searchService.deleteIndex(ConceptTreePosition.class);

    for (final Map.Entry<String, Boolean> entry : terminologies.entrySet()) {
      TerminologyLoaderUtility.loadTerminology(searchService, rootDir + entry.getKey() + ".json");
      TerminologyLoaderUtility.loadMetadata(searchService,
          rootDir + entry.getKey() + "-metadata.json");
      TerminologyLoaderUtility.loadConcepts(searchService,
          rootDir + entry.getKey() + "-concepts.json");
      TerminologyLoaderUtility.loadConceptRelationships(searchService,
          rootDir + entry.getKey() + "-relationships.json", entry.getValue());

    }
  }

  @Test
  public void test() {
    // Do not remove this test. It forces the setup before other classes.
    assertTrue(true);
  }

}
