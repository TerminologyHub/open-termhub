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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.Application;
import com.wci.termhub.model.HasId;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.PropertyUtility;
import com.wci.termhub.util.SubsetLoaderUtil;

/**
 * Test class for loading FHIR R4 Value Set files.
 */
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test-r4.properties")
public class ValueSetLoadR4UnitTest {
  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ValueSetLoadR4UnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** List of FHIR Code System files to load. */
  private static final List<String> VALUE_SET_FILES =
      List.of("ValueSet-snomedct_us-model-nlm-20240301-r4.json");

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

    // Load each code system by reading directly from the classpath
    for (final String valueSetFile : VALUE_SET_FILES) {
      try {
        // Read file from classpath directly using Spring's resource mechanism
        final Resource resource = new ClassPathResource("data/" + valueSetFile,
            ValueSetLoadR4UnitTest.class.getClassLoader());

        if (!resource.exists()) {
          throw new FileNotFoundException("Could not find resource: data/" + valueSetFile);
        }

        final String fileContent =
            FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8);

        LOGGER.info("Loading value sets from classpath resource: data/{}", valueSetFile);
        assertNotNull(SubsetLoaderUtil.loadSubset(searchService, fileContent, false));

      } catch (final Exception e) {
        LOGGER.error("Error loading value set file: {}", valueSetFile, e);
        throw e;
      }
    }

    LOGGER.info("Finished loading value sets");
  }

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

  /**
   * Basic test to ensure setup was successful.
   */
  @Test
  public void testValueSetLoaded() {
    // This test simply verifies that the setup completed without errors
    assertTrue(true, "Value Sets were loaded without errors");
  }

}
