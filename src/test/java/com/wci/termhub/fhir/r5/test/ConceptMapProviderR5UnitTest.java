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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.ConceptMap;
import org.hl7.fhir.r5.model.UriType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.fhir.r5.ConceptMapProviderR5;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;

/**
 * The Class ConceptMapProviderR5UnitTest.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test-r5")
public class ConceptMapProviderR5UnitTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConceptMapProviderR5UnitTest.class);

  /** The test map. */
  private ConceptMap testMap;

  /** The provider. */
  @Autowired
  private ConceptMapProviderR5 provider;

  /** The request. */
  private MockHttpServletRequest request;

  /** The details. */
  private ServletRequestDetails details;

  /** The Constant VERSION. */
  private static final String VERSION = "20240301";

  /** The Constant TEST_MAP_URL. */
  private static final String TEST_MAP_URL = "http://snomed.info/sct";

  /** The Constant TEST_MAP_TARGET. */
  private static final String TEST_MAP_TARGET = "http://hl7.org/fhir/sid/icd-10-cm";

  /**
   * Setup class.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public void setupClass() throws Exception {
    // Load test data
    final String testDataPath =
        "src/main/resources/data/ConceptMap-snomedct_us-icd10cm-sandbox-20240301-r5.json";
    LOGGER.info("Loading test data from: {}", testDataPath);

    // Parse JSON to FHIR ConceptMap
    final String json = new String(Files.readAllBytes(Paths.get(testDataPath)));
    final IParser parser = FhirContext.forR5().newJsonParser();
    final ConceptMap conceptMap = parser.parseResource(ConceptMap.class, json);
    LOGGER.info("Parsed ConceptMap: id={}, version={}, url={}", conceptMap.getId(),
        conceptMap.getVersion(), conceptMap.getUrl());

    // Create the ConceptMap
    provider.createConceptMap(null, null, conceptMap);
    LOGGER.info("Created ConceptMap");

    // Debug what's in the repository
    final List<ConceptMap> maps = provider.findCandidates();
    LOGGER.info("All concept maps in repository:");
    for (final ConceptMap cm : maps) {
      LOGGER.info("ConceptMap: id={}, version={}, url={}, title={}", cm.getId(), cm.getVersion(),
          cm.getUrl(), cm.getTitle());
    }

    // Get the loaded mapset and convert to FHIR
    final List<ConceptMap> candidates = provider.findCandidates();
    LOGGER.info("Before filtering - all URLs:");
    candidates.forEach(m -> LOGGER.info("  URL: {}", m.getUrl()));
    testMap = candidates.stream().filter(m -> m.getUrl().contains("6011000124106")).findFirst()
        .orElseThrow(() -> new RuntimeException("Test mapset not found"));
    LOGGER.info("Retrieved test map: id={}, version={}, url={}", testMap.getId(),
        testMap.getVersion(), testMap.getUrl());
  }

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    request = new MockHttpServletRequest();
    details = new ServletRequestDetails();
    details.setServletRequest(request);
  }

  /**
   * Test find concept maps.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptMaps() throws Exception {
    // Test finding all concept maps
    final Bundle bundle = provider.findConceptMaps(request, details, null, // TokenParam
                                                                           // id
        null, // StringParam date
        null, // StringParam description
        null, // StringParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // UriParam sourceSystem
        null, // TokenParam sourceCode
        null, // UriParam sourceScopeUri
        null, // TokenParam targetCode
        null, // UriParam targetSystem
        null, // UriParam targetScopeUri
        null, // StringParam title
        null, // UriType url
        null, // StringParam version
        null, // NumberParam count
        null); // NumberParam offset

    assertNotNull(bundle);
    assertTrue(!bundle.getEntry().isEmpty());

    // Verify we found our test map
    final boolean found = bundle.getEntry().stream()
        .anyMatch(e -> testMap.getUrl().equals(((ConceptMap) e.getResource()).getUrl()));
    assertTrue(found);
  }

  /**
   * Test find concept maps by title.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptMapsByTitle() throws Exception {

    // Given
    final StringParam title = new StringParam("SNOMEDCT_US-ICD10CM");

    // Test finding by title
    final Bundle bundle = provider.findConceptMaps(request, details, null, // TokenParam
                                                                           // id
        null, // StringParam date
        null, // StringParam description
        null, // StringParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // UriParam sourceSystem
        null, // TokenParam sourceCode
        null, // UriParam sourceScopeUri
        null, // TokenParam targetCode
        null, // UriParam targetSystem
        null, // UriParam targetScopeUri
        title, // StringParam title
        null, // UriType url
        null, // StringParam version
        null, // NumberParam count
        null); // NumberParam offset

    // Log bundle contents for debugging
    logBundleContents(bundle, "testFindConceptMapsByTitle");

    assertNotNull(bundle);
    assertEquals(1, bundle.getEntry().size());
    assertEquals(testMap.getUrl(), ((ConceptMap) bundle.getEntry().get(0).getResource()).getUrl());
  }

  /**
   * Test find concept maps by publisher.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptMapsByPublisher() throws Exception {

    // Given
    final StringParam publisher = new StringParam("SANDBOX");

    // When
    final Bundle bundle = provider.findConceptMaps(request, details, null, // TokenParam
                                                                           // id
        null, // StringParam date
        null, // StringParam description
        null, // StringParam identifier
        null, // StringParam name
        publisher, // StringParam publisher
        null, // sourceSystem, // UriParam sourceSystem
        null, // TokenParam sourceCode
        null, // UriParam sourceScopeUri
        null, // TokenParam targetCode
        null, // UriParam targetSystem
        null, // UriParam targetScopeUri
        null, // StringParam title
        null, // UriType url
        null, // StringParam version
        null, // NumberParam count
        null); // NumberParam offset

    // Test finding by publisher
    assertNotNull(bundle);
    assertTrue(!bundle.getEntry().isEmpty());
    assertTrue(bundle.getEntry().stream()
        .allMatch(e -> "SANDBOX".equals(((ConceptMap) e.getResource()).getPublisher())));
  }

  /**
   * Test find concept maps by version.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptMapsByVersion() throws Exception {

    // Given
    final StringParam version = new StringParam(VERSION);

    // When
    final Bundle bundle = provider.findConceptMaps(request, details, null, // TokenParam
                                                                           // id
        null, // StringParam date
        null, // StringParam description
        null, // StringParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // sourceSystem, // UriParam sourceSystem
        null, // TokenParam sourceCode
        null, // UriParam sourceScopeUri
        null, // TokenParam targetCode
        null, // UriParam targetSystem
        null, // UriParam targetScopeUri
        null, // StringParam title
        null, // UriType url
        version, // StringParam version
        null, // NumberParam count
        null); // NumberParam offset

    logBundleContents(bundle, "testFindConceptMapsByVersion");

    assertNotNull(bundle);
    assertTrue(!bundle.getEntry().isEmpty());
    assertTrue(bundle.getEntry().stream()
        .allMatch(e -> VERSION.equals(((ConceptMap) e.getResource()).getVersion())));
  }

  /**
   * Test find concept maps by source system.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptMapsBySourceSystem() throws Exception {

    // Given
    final UriParam sourceScopeUri = new UriParam(TEST_MAP_URL);

    // When
    final Bundle bundle = provider.findConceptMaps(request, details, null, // TokenParam
                                                                           // id
        null, // StringParam date
        null, // StringParam description
        null, // StringParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // sourceSystem, // UriParam sourceSystem
        null, // TokenParam sourceCode
        sourceScopeUri, // UriParam sourceScopeUri
        null, // TokenParam targetCode
        null, // UriParam targetSystem
        null, // UriParam targetScopeUri
        null, // StringParam title
        null, // UriType url
        null, // new StringParam(VERSION), // StringParam version
        null, // NumberParam count
        null); // NumberParam offset

    // Log bundle contents for debugging
    logBundleContents(bundle, "testFindConceptMapsBySourceSystem");

    // Then
    assertNotNull(bundle, "Bundle should not be null");
    assertFalse(bundle.getEntry().isEmpty(), "Bundle should have entries");
    assertTrue(bundle.getEntry().stream().allMatch(e -> {
      final ConceptMap cm = (ConceptMap) e.getResource();
      return cm.getSourceScope() != null && ((UriType) cm.getSourceScope()).getValue() != null
          && ((UriType) cm.getSourceScope()).getValue().contains(TEST_MAP_URL);
    }), "All entries should have matching source scope URI");
  }

  /**
   * Test find concept maps by target system.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptMapsByTargetSystem() throws Exception {

    // Given
    final UriParam targetSystem = new UriParam(TEST_MAP_TARGET);

    // When
    final Bundle bundle = provider.findConceptMaps(request, details, null, // TokenParam
                                                                           // id
        null, // StringParam date
        null, // StringParam description
        null, // StringParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // UriParam sourceSystem
        null, // TokenParam sourceCode
        null, // UriParam sourceScopeUri
        null, // TokenParam targetCode
        targetSystem, // UriParam targetSystem
        null, // UriParam targetScopeUri
        null, // StringParam title
        null, // UriType url
        null, // StringParam version
        null, // NumberParam count
        null); // NumberParam offset

    // Log bundle contents for debugging
    logBundleContents(bundle, "testFindConceptMapsByTargetSystem");

    // Then
    assertNotNull(bundle, "Bundle should not be null");
    // FAILS next line. Do not remove until fixed
    assertFalse(bundle.getEntry().isEmpty(), "Bundle should have entries");
    assertTrue(bundle.getEntry().stream().allMatch(e -> {
      final ConceptMap cm = (ConceptMap) e.getResource();
      return cm.getTargetScope() != null && ((UriType) cm.getTargetScope()).getValue() != null
          && ((UriType) cm.getTargetScope()).getValue().equals(TEST_MAP_TARGET);
    }), "All entries should have matching target scope URI");
  }

  /**
   * Helper method to log bundle contents for debugging.
   *
   * @param bundle the bundle to log
   * @param testName the name of the test being run
   */
  private void logBundleContents(final Bundle bundle, final String testName) {

    if (bundle == null) {
      LOGGER.info("{}: Bundle is null", testName);
      return;
    }

    if (bundle.getEntry().isEmpty()) {
      LOGGER.info("{}: Bundle has no entries", testName);
      return;
    }

    bundle.getEntry().forEach(entry -> {
      final ConceptMap cm = (ConceptMap) entry.getResource();
      LOGGER.info("{} - ConceptMap: id={}, url={}", testName, cm.getId(), cm.getUrl());

      LOGGER.info("  Title: {}", cm.getTitle());
      LOGGER.info("  Publisher: {}", cm.getPublisher());
      LOGGER.info("  Version: {}", cm.getVersion());

      // Log source scope details
      LOGGER.info("  Source Scope:");
      LOGGER.info("    sourceScope={}", cm.getSourceScope());
      LOGGER.info("    sourceScopeUriType={}", cm.getSourceScopeUriType());
      if (cm.getSourceScope() != null) {
        LOGGER.info("    sourceScope value={}", ((UriType) cm.getSourceScope()).getValue());
      }
      if (cm.getSourceScopeUriType() != null) {
        LOGGER.info("    sourceScopeUriType value={}", cm.getSourceScopeUriType().getValue());
      }

      // Log target scope details
      LOGGER.info("  Target Scope:");
      LOGGER.info("    targetScope={}", cm.getTargetScope());
      LOGGER.info("    targetScopeUriType={}", cm.getTargetScopeUriType());
      if (cm.getTargetScope() != null) {
        LOGGER.info("    targetScope value={}", ((UriType) cm.getTargetScope()).getValue());
      }
      if (cm.getTargetScopeUriType() != null) {
        LOGGER.info("    targetScopeUriType value={}", cm.getTargetScopeUriType().getValue());
      }

      // Log expected values
      LOGGER.info("  Expected Values:");
      LOGGER.info("    TEST_MAP_URL={}", TEST_MAP_URL);
      LOGGER.info("    TEST_MAP_TARGET={}", TEST_MAP_TARGET);
    });
  }

  /**
   * Cleanup.
   *
   * @throws Exception the exception
   */
  @AfterAll
  public void cleanup() throws Exception {
    // Clean up Lucene index
    final File indexDir = new File("target/lucene-index-r5");
    if (indexDir.exists()) {
      FileUtils.deleteDirectory(indexDir);
    }
  }
}
