/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r5.model.CodeSystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wci.termhub.algo.ProgressListener;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.service.BulkLoaderService;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractServerTest;

/**
 * Integration test for {@link BulkLoaderServiceImpl}.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:application-test.properties", properties = {
    "lucene.index.directory=build/index/lucene-rest"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BulkLoaderServiceImplTest extends AbstractServerTest {

  /** The bulk loader service. */
  // The service under test, injected from the Spring context
  @Autowired
  private BulkLoaderService bulkLoaderService;

  /** The lucene index directory. */
  @Value("${lucene.index.directory}")
  private String luceneIndexDirectory;

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The code system resource. */
  @Value("classpath:data/CodeSystem-icd10cm-sandbox-2023-r4.json")
  private Resource codeSystemResource;

  /** The temp dir. */
  // A temporary directory for creating test files
  @TempDir
  private File tempDir;

  /**
   * Before all.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public void beforeAll() throws Exception {
    clearAndCreateIndexDirectories(searchService, luceneIndexDirectory);
  }

  /**
   * Test the successful execution of the synchronous code system load.
   *
   * @throws Exception the exception
   */
  @Test
  void testDoCodeSystemLoadSuccess() throws Exception {
    // 1. Arrange
    final File codeSystemFile = codeSystemResource.getFile();
    // This file will be deleted by the service, so copy it to a temp location
    final File tempTestFile = new File(tempDir, codeSystemFile.getName());
    updateVersionAndCopy(tempTestFile);

    // 2. Act
    final CodeSystem result = bulkLoaderService.doCodeSystemLoad(tempTestFile);

    // 3. Assert
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("ICD10CM");
  }

  /**
   * Test the successful execution of the asynchronous code system load.
   *
   * @throws Exception the exception
   */
  @Test
  void testStartAsyncCodeSystemLoadSuccess() throws Exception {
    // 1. Arrange
    final String processId = "process-123";
    // 1. Arrange
    final File codeSystemFile = codeSystemResource.getFile();
    // This file will be deleted by the service, so copy it to a temp location
    final File tempTestFile = new File(tempDir, codeSystemFile.getName());
    updateVersionAndCopy(tempTestFile);

    final Map<String, List<String>> processResultMap = new ConcurrentHashMap<>();
    final Map<String, Long> processProgressMap = new ConcurrentHashMap<>();
    final Map<String, Exception> processExceptionMap = new ConcurrentHashMap<>();
    final ProgressListener mockListener = Mockito.mock(ProgressListener.class);

    // 2. Act
    bulkLoaderService.startAsyncCodeSystemLoad(processId, tempTestFile, mockListener,
        processResultMap, processProgressMap, processExceptionMap);

    // 3. Assert
    // Use Awaitility to wait for the async method to complete.
    // We'll wait until the result map contains our processId.
    await().atMost(Duration.ofSeconds(10)).until(() -> processResultMap.containsKey(processId));

    // Verify the result map was updated correctly
    assertThat(processResultMap).containsKey(processId);
    assertTrue(
        processResultMap.get(processId) != null && !processResultMap.get(processId).isEmpty());
    assertThat(processExceptionMap).doesNotContainKey(processId);
  }

  /**
   * Test the failure scenario for the asynchronous code system load.
   *
   * @throws Exception the exception
   */
  @Test
  void testStartAsyncCodeSystemLoadFailure() throws Exception {
    // 1. Arrange0
    final String processId = "process-456";
    final File testFile = new File(tempDir, "invalid-codesystem.json");
    final String invalidJson = "{\"resourceType\": \"CodeSystem\", \"id\": \"invalid-cs\",,}"; // Malformed
    // JSON
    FileUtils.writeStringToFile(testFile, invalidJson, StandardCharsets.UTF_8);

    final Map<String, List<String>> processResultMap = new ConcurrentHashMap<>();
    final Map<String, Long> processProgressMap = new ConcurrentHashMap<>();
    final Map<String, Exception> processExceptionMap = new ConcurrentHashMap<>();
    final ProgressListener mockListener = Mockito.mock(ProgressListener.class);

    // 2. Act
    bulkLoaderService.startAsyncCodeSystemLoad(processId, testFile, mockListener, processResultMap,
        processProgressMap, processExceptionMap);

    // 3. Assert
    // Use Awaitility to wait for the async method to fail and update the exception map.
    await().atMost(Duration.ofSeconds(10)).until(() -> processExceptionMap.containsKey(processId));

    // Verify the exception map was updated correctly
    assertThat(processExceptionMap).containsKey(processId);
    assertThat(processExceptionMap.get(processId)).isInstanceOf(Exception.class);

    // Verify the progress map indicates an error
    assertThat(processProgressMap).containsKey(processId);
    assertThat(processProgressMap.get(processId)).isEqualTo(-1L);

    // Verify the result map was not populated
    assertThat(processResultMap).doesNotContainKey(processId);

    // Verify that the file was NOT deleted in the case of an error
    assertThat(testFile).exists();
  }

  // Add a test that will run an async load and then try a sync load while the async is
  /**
   * Test concurrent async and sync load.
   *
   * @throws Exception the exception
   */
  // still running. Assert that the sync load fails
  @Test
  void testConcurrentAsyncAndSyncLoad() throws Exception {
    // 1. Arrange
    final String processId = "process-789";
    // 1. Arrange
    final File codeSystemFile = codeSystemResource.getFile();
    // This file will be deleted by the service, so copy it to a temp location
    final File tempTestFile1 = new File(tempDir, "async-" + codeSystemFile.getName());
    updateVersionAndCopy(tempTestFile1);
    final File tempTestFile2 = new File(tempDir, "sync-" + codeSystemFile.getName());
    updateVersionAndCopy(tempTestFile2);
    final Map<String, List<String>> processResultMap = new ConcurrentHashMap<>();
    final Map<String, Long> processProgressMap = new ConcurrentHashMap<>();
    final Map<String, Exception> processExceptionMap = new ConcurrentHashMap<>();
    final ProgressListener mockListener = Mockito.mock(ProgressListener.class);
    // Start the async load
    bulkLoaderService.startAsyncCodeSystemLoad(processId, tempTestFile1, mockListener,
        processResultMap, processProgressMap, processExceptionMap);
    // Wait a brief moment to ensure the async load has started
    Thread.sleep(100);
    // 2. Act
    Exception syncLoadException = null;
    try {
      bulkLoaderService.doCodeSystemLoad(tempTestFile2);
    } catch (final FHIRServerResponseException e) {
      syncLoadException = e;
    }
    // 3. Assert
    // Verify the async load eventually completes
    await().atMost(Duration.ofSeconds(10)).until(() -> processResultMap.containsKey(processId));
    assertThat(processResultMap).containsKey(processId);
    // Verify the sync load failed with an exception
    assertNotNull(syncLoadException);
    assertEquals(((FHIRServerResponseException) syncLoadException).getStatusCode(), 409);
  }

  /**
   * Update version and copy.
   *
   * @param tempTestFile the temp test file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void updateVersionAndCopy(final File tempTestFile) throws IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    final String originalJsonContent =
        FileUtils.readFileToString(codeSystemResource.getFile(), StandardCharsets.UTF_8);
    final ObjectNode rootNode = (ObjectNode) objectMapper.readTree(originalJsonContent);
    final String timestamp = String.valueOf(System.currentTimeMillis());
    rootNode.put("version", timestamp);
    FileUtils.writeStringToFile(tempTestFile, rootNode.toString(), StandardCharsets.UTF_8);
  }
}
