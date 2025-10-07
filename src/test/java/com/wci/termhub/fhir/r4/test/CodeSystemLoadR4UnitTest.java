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

import org.hl7.fhir.r4.model.CodeSystem;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.CodeSystemLoaderUtil;

/**
 * Test class for loading FHIR R4 Code System files.
 */
public class CodeSystemLoadR4UnitTest extends AbstractFhirR4ServerTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CodeSystemLoadR4UnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** List of FHIR Code System files to load. */
  private static final List<String> CODE_SYSTEM_FILES =
      List.of("CodeSystem-snomedct_us-sandbox-20240301-r4.json",
          "CodeSystem-snomedct-sandbox-20240101-r4.json", "CodeSystem-lnc-sandbox-277-r4.json",
          "CodeSystem-icd10cm-sandbox-2023-r4.json", "CodeSystem-rxnorm-sandbox-04012024-r4.json");

  /**
   * Test reload code system.
   *
   * @throws Exception the exception
   */
  @Test
  public void testReloadCodeSystem() throws Exception {
    // Should throw an exception if the code system is already loaded
    for (final String codeSystemFile : CODE_SYSTEM_FILES) {
      try {
        final Resource resource = new ClassPathResource("data/" + codeSystemFile,
            CodeSystemLoadR4UnitTest.class.getClassLoader());

        assertThrows(Exception.class, () -> {
          LOGGER.info("Attempt reload of code system from classpath resource: data/{}",
              codeSystemFile);
          CodeSystemLoaderUtil.loadCodeSystem(searchService, resource.getFile(), false,
              CodeSystem.class, new DefaultProgressListener());
        });

      } catch (final Exception e) {
        LOGGER.error("Error reloading code system file: {}", codeSystemFile, e);
        throw e;
      }
    }
  }

}
