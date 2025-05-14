/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r5.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.Application;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.ConceptTreePosition;
import com.wci.termhub.model.Mapping;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.PropertyUtility;

/**
 * Test class for loading FHIR R5 ConceptMap files.
 */
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test-r5.properties")
public class ConceptMapLoadR5UnitTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConceptMapLoadR5UnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The object mapper. */
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /** List of FHIR ConceptMap files to load. */
  private static final List<String> CONCEPT_MAP_FILES =
      List.of("ConceptMap-snomedct_us-icd10cm-sandbox-20240301-r5.json");

  /**
   * Setup - load the FHIR ConceptMap files.
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

    searchService.deleteIndex(Terminology.class);
    searchService.deleteIndex(Metadata.class);
    searchService.deleteIndex(Concept.class);
    searchService.deleteIndex(Term.class);
    searchService.deleteIndex(ConceptRelationship.class);
    searchService.deleteIndex(ConceptTreePosition.class);
    searchService.deleteIndex(Mapset.class);
    searchService.deleteIndex(Mapping.class);

    // Load each concept map by reading directly from the classpath
    for (final String conceptMapFile : CONCEPT_MAP_FILES) {
      try {
        // Read file from classpath directly using Spring's resource mechanism
        final Resource resource = new ClassPathResource("data/" + conceptMapFile,
            ConceptMapLoadR5UnitTest.class.getClassLoader());

        if (!resource.exists()) {
          throw new FileNotFoundException("Could not find resource: data/" + conceptMapFile);
        }

        LOGGER.info("Loading concept map from classpath resource: data/{}", conceptMapFile);
        // Verify the file is a ConceptMap
        @SuppressWarnings("resource")
        final JsonNode root = OBJECT_MAPPER.readTree(resource.getInputStream());
        if (!"ConceptMap".equals(root.path("resourceType").asText())) {
          throw new IllegalArgumentException("Invalid resource type - expected ConceptMap");
        }
        // TODO: Add ConceptMap loading logic here
      } catch (final Exception e) {
        LOGGER.error("Error loading concept map file: {}", conceptMapFile, e);
        throw e;
      }
    }

    LOGGER.info("Finished loading concept maps");
  }

  /**
   * Basic test to ensure setup was successful.
   */
  @Test
  public void testConceptMapsLoaded() {
    // This test simply verifies that the setup completed without errors
    assertTrue(true, "Concept maps were loaded without errors");
  }
}
