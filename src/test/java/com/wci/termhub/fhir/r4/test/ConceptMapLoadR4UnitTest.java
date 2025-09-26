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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.hl7.fhir.r4.model.ConceptMap;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.ConceptMapLoaderUtil;

/**
 * Test class for loading FHIR R5 ConceptMap files.
 */
public class ConceptMapLoadR4UnitTest extends AbstractFhirR4ServerTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConceptMapLoadR4UnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** List of FHIR ConceptMap files to load. */
  private static final List<String> CONCEPT_MAP_FILES =
      List.of("ConceptMap-snomedct_us-icd10cm-sandbox-20240301-r4.json");

  /**
   * Test reload of concept map.
   *
   * @throws Exception the exception
   */
  @Test
  public void testReloadConceptMap() throws Exception {
    // Should throw an exception if the code system is already loaded
    for (final String conceptMapFile : CONCEPT_MAP_FILES) {
      try {
        final Resource resource = new ClassPathResource("data/" + conceptMapFile,
            CodeSystemLoadR4UnitTest.class.getClassLoader());

        assertThrows(Exception.class, () -> {
          LOGGER.info("Attempt reload of concept map from classpath resource: data/{}",
              conceptMapFile);
          ConceptMapLoaderUtil.loadConceptMap(searchService, resource.getFile(), ConceptMap.class);
        });

      } catch (final Exception e) {
        LOGGER.error("Error reloading code system file: {}", conceptMapFile, e);
        throw e;
      }
    }
  }

}
