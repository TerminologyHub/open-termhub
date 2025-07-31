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

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.SubsetLoaderUtil;

/**
 * Test class for loading FHIR R4 Value Set files.
 */
public class ValueSetLoadR4UnitTest extends AbstractFhirR4ServerTest {
  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ValueSetLoadR4UnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** List of FHIR Code System files to load. */
  private static final List<String> VALUE_SET_FILES =
      List.of("ValueSet-snomedct_us-model-nlm-20240301-r4.json");

   /**
   * Test reload value set.
   *
   * @throws Exception the exception
   */
  @Test
  public void testReloadValueSet() throws Exception {
    // Should throw an exception if the code system is already loaded
    for (final String valueSetFile : VALUE_SET_FILES) {
      try {
        // Read file from classpath directly using Spring's resource mechanism
        final Resource resource = new ClassPathResource("data/" + valueSetFile,
            ValueSetLoadR4UnitTest.class.getClassLoader());

        final String fileContent =
            FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8);

        assertThrows(Exception.class, () -> {
          LOGGER.info("Attempt reload of value set from classpath resource: data/{}", valueSetFile);
          SubsetLoaderUtil.loadSubset(searchService, fileContent, false);
        });

      } catch (final Exception e) {
        LOGGER.error("Error reloading value set file: {}", valueSetFile, e);
        throw e;
      }
    }
  }

}
