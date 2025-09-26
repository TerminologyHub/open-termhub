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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.ConceptMap;
import org.hl7.fhir.r5.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.lucene.LuceneDataAccess;
import com.wci.termhub.model.HasId;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.CodeSystemLoaderUtil;
import com.wci.termhub.util.ConceptMapLoaderUtil;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.ThreadLocalMapper;
import com.wci.termhub.util.ValueSetLoaderUtil;

/**
 * Abstract superclass for source code tests.
 */
public abstract class AbstractServerTest extends BaseUnitTest {

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(AbstractServerTest.class);

  /**
   * Clear and create index directories.
   *
   * @param searchService the search service
   * @param indexDirectory the index directory
   * @throws Exception the exception
   */
  protected void clearAndCreateIndexDirectories(final EntityRepositoryService searchService,
    final String indexDirectory) throws Exception {

    // Delete all indexes for a fresh start
    logger.info("Deleting existing indexes from directory: {}", indexDirectory);
    final File indexDir = new File(indexDirectory);
    logger.info("Does index {} exist? {}", indexDir.getAbsolutePath(), indexDir.exists());

    if (indexDir.exists()) {
      FileUtils.deleteDirectory(indexDir);
    }

    // Create the directory again
    if (!indexDir.mkdirs()) {
      throw new RuntimeException("Failed to create index directory: " + indexDirectory);
    }

    final List<Class<? extends HasId>> indexedObjects = ModelUtility.getIndexedObjects();
    for (final Class<? extends HasId> clazz : indexedObjects) {
      searchService.deleteIndex(clazz);
      searchService.createIndex(clazz);
    }
    LuceneDataAccess.clearReaders();
  }

  /**
   * Clear all indexes for cleanup.
   *
   * @param searchService the search service
   * @param indexDirectory the index directory
   * @throws Exception the exception
   */
  protected void clearIndexes(final EntityRepositoryService searchService,
    final String indexDirectory) throws Exception {

    logger.info("Clearing indexes from directory: {}", indexDirectory);
    final File indexDir = new File(indexDirectory);

    if (indexDir.exists()) {
      FileUtils.deleteDirectory(indexDir);
    }

    final List<Class<? extends HasId>> indexedObjects = ModelUtility.getIndexedObjects();
    for (final Class<? extends HasId> clazz : indexedObjects) {
      searchService.deleteIndex(clazz);
    }
  }

  /**
   * Load code systems.
   *
   * @param searchService the search service
   * @param codeSystemFilenames the code system filenames
   * @param computeTreePositions the compute tree positions
   * @throws Exception the exception
   */
  protected void loadCodeSystems(final EntityRepositoryService searchService,
    final List<String> codeSystemFilenames, final boolean computeTreePositions) throws Exception {

    // Load each code system by reading directly from the classpath
    for (final String codeSystemFile : codeSystemFilenames) {
      try {
        final ClassPathResource resource = new ClassPathResource("data/" + codeSystemFile);
        if (!resource.exists()) {
          throw new FileNotFoundException("Could not find resource: data/" + codeSystemFile);
        }

        logger.info("Loading code system from classpath resource: data/{}", codeSystemFile);
        CodeSystemLoaderUtil.loadCodeSystem(searchService, resource.getFile(), computeTreePositions,
            CodeSystem.class, new DefaultProgressListener());
      } catch (final Exception e) {
        logger.error("Error loading code system file: {}", codeSystemFile, e);
        throw e;
      }
    }
    LuceneDataAccess.clearReaders();
    logger.info("Finished loading code systems");
  }

  /**
   * Load concept maps.
   *
   * @param searchService the search service
   * @param conceptMapFilenames the concept map filenames
   * @throws Exception the exception
   */
  protected void loadConceptMaps(final EntityRepositoryService searchService,
    final List<String> conceptMapFilenames) throws Exception {

    // Load each concept map by reading directly from the classpath
    for (final String conceptMapFile : conceptMapFilenames) {
      try {
        final ClassPathResource resource = new ClassPathResource("data/" + conceptMapFile);
        if (!resource.exists()) {
          throw new FileNotFoundException("Could not find resource: data/" + conceptMapFile);
        }

        logger.info("Loading concept map from classpath resource: data/{}", conceptMapFile);
        // Verify the file is a ConceptMap
        @SuppressWarnings("resource")
        final JsonNode root = ThreadLocalMapper.get().readTree(resource.getInputStream());
        if (!"ConceptMap".equals(root.path("resourceType").asText())) {
          throw new IllegalArgumentException("Invalid resource type - expected ConceptMap");
        }

        ConceptMapLoaderUtil.loadConceptMap(searchService, resource.getFile(), ConceptMap.class);
      } catch (final Exception e) {
        logger.error("Error loading concept map file: {}", conceptMapFile, e);
        throw e;
      }
    }
    LuceneDataAccess.clearReaders();
    logger.info("Finished loading concept maps");
  }

  /**
   * Load value sets.
   *
   * @param searchService the search service
   * @param valueSetFileNames the value set file names
   * @throws Exception the exception
   */
  protected void loadValueSets(final EntityRepositoryService searchService,
    final List<String> valueSetFileNames) throws Exception {

    // Load each code system by reading directly from the classpath
    for (final String valueSetFile : valueSetFileNames) {
      try {
        final ClassPathResource resource = new ClassPathResource("data/" + valueSetFile);
        if (!resource.exists()) {
          throw new FileNotFoundException("Could not find resource: data/" + valueSetFile);
        }

        logger.info("Loading value sets from classpath resource: data/{}", valueSetFile);
        assertNotNull(
            ValueSetLoaderUtil.loadValueSet(searchService, resource.getFile(), ValueSet.class));

      } catch (final Exception e) {
        logger.error("Error loading value set file: {}", valueSetFile, e);
        throw e;
      }
    }
    LuceneDataAccess.clearReaders();
    logger.info("Finished loading value sets");

  }

}
