/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r4.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ConceptMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletRequest;

import com.wci.termhub.fhir.r4.ConceptMapProviderR4;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;

/**
 * The Class ConceptMapProviderR4UnitTest.
 */
@AutoConfigureMockMvc
public class ConceptMapProviderR4UnitTest extends AbstractFhirR4ServerTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConceptMapProviderR4UnitTest.class);

  /** The test map. */
  private ConceptMap testMap;

  /** The provider. */
  @Autowired
  private ConceptMapProviderR4 provider;

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
    final Bundle bundle = provider.findConceptMaps(request, details,

        null, // TokenParam id
        null, // DateRangeParam date
        null, // StringParam description
        null, // StringParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // TokenParam sourceCode
        null, // UriParam sourceSystem
        null, // TokenParam targetCode
        null, // UriParam targetSystem
        null, // StringParam title
        null, // StringParam url
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

    // When
    final Bundle bundle = provider.findConceptMaps(request, details,

        null, // TokenParam id
        null, // DateRangeParam date
        null, // StringParam description
        null, // StringParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // TokenParam sourceCode
        null, // UriParam sourceSystem
        null, // TokenParam targetCode
        null, // UriParam targetSystem
        title, // StringParam title
        null, // StringParam url
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
    final Bundle bundle = provider.findConceptMaps(request, details,

        null, // TokenParam id
        null, // DateRangeParam date
        null, // StringParam description
        null, // StringParam identifier
        null, // StringParam name
        publisher, // StringParam publisher
        null, // TokenParam sourceCode
        null, // UriParam sourceSystem
        null, // TokenParam targetCode
        null, // UriParam targetSystem
        null, // StringParam title
        null, // StringParam url
        null, // StringParam version
        null, // NumberParam count
        null); // NumberParam offset

    // Log bundle contents for debugging
    logBundleContents(bundle, "testFindConceptMapsByPublisher");

    // Then
    assertNotNull(bundle, "Bundle should not be null");
    assertTrue(!bundle.getEntry().isEmpty(), "Bundle should have entries");
    assertTrue(
        bundle.getEntry().stream()
            .allMatch(e -> "SANDBOX".equals(((ConceptMap) e.getResource()).getPublisher())),
        "All entries should have matching publisher");
    //
    //
    //
    // // Test finding by publisher
    // final var bundle = provider.findConceptMaps(request, details, null, null,
    // null, null, null,
    // new StringParam("SANDBOX"), null, null, null, null, null, null, null,
    // null, null);
    //
    // assertNotNull(bundle);
    // assertTrue(!bundle.getEntry().isEmpty());
    // assertTrue(bundle.getEntry().stream()
    // .allMatch(e -> "SANDBOX".equals(((ConceptMap)
    // e.getResource()).getPublisher())));
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
    final Bundle bundle = provider.findConceptMaps(request, details,

        null, // TokenParam id
        null, // DateRangeParam date
        null, // StringParam description
        null, // StringParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // TokenParam sourceCode
        null, // UriParam sourceSystem
        null, // TokenParam targetCode
        null, // UriParam targetSystem
        null, // StringParam title
        null, // StringParam url
        version, // StringParam version
        null, // NumberParam count
        null); // NumberParam offset

    // Log bundle contents for debugging
    logBundleContents(bundle, "testFindConceptMapsByVersion");

    // Then
    assertNotNull(bundle, "Bundle should not be null");
    assertTrue(!bundle.getEntry().isEmpty(), "Bundle should have entries");
    assertTrue(
        bundle.getEntry().stream()
            .allMatch(e -> VERSION.equals(((ConceptMap) e.getResource()).getVersion())),
        "All entries should have matching version");
  }

  /**
   * Test find concept maps by source system.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptMapsBySourceSystem() throws Exception {
    // Given
    final UriParam sourceSystem = new UriParam(TEST_MAP_URL);

    // When
    final Bundle bundle = provider.findConceptMaps(request, details,

        null, // TokenParam id
        null, // DateRangeParam date
        null, // StringParam description
        null, // StringParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // TokenParam sourceCode
        sourceSystem, // UriParam sourceSystem
        null, // TokenParam targetCode
        null, // UriParam targetSystem
        null, // StringParam title
        null, // StringParam url
        null, // StringParam version
        null, // NumberParam count
        null); // NumberParam offset

    // Log bundle contents for debugging
    logBundleContents(bundle, "testFindConceptMapsBySourceSystem");

    // Then
    assertNotNull(bundle, "Bundle should not be null");
    assertTrue(!bundle.getEntry().isEmpty(), "Bundle should have entries");
    assertTrue(bundle.getEntry().stream().allMatch(e -> {
      final ConceptMap cm = (ConceptMap) e.getResource();
      return cm.getSourceUriType() != null && cm.getSourceUriType().getValue() != null
          && cm.getSourceUriType().getValue().startsWith(TEST_MAP_URL);
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
    final Bundle bundle = provider.findConceptMaps(request, details,

        null, // TokenParam id
        null, // DateRangeParam date
        null, // StringParam description
        null, // StringParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // TokenParam sourceCode
        null, // UriParam sourceSystem
        null, // TokenParam targetCode
        targetSystem, // UriParam targetSystem
        null, // StringParam title
        null, // StringParam url
        null, // StringParam version
        null, // NumberParam count
        null); // NumberParam offset

    // Log bundle contents for debugging
    logBundleContents(bundle, "testFindConceptMapsByTargetSystem");

    // Then
    assertNotNull(bundle, "Bundle should not be null");
    assertTrue(!bundle.getEntry().isEmpty(), "Bundle should have entries");
    assertTrue(bundle.getEntry().stream().allMatch(e -> {
      final ConceptMap cm = (ConceptMap) e.getResource();
      return cm.getTargetUriType() != null && cm.getTargetUriType().getValue() != null
          && cm.getTargetUriType().getValue().startsWith(TEST_MAP_TARGET);
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
      LOGGER.info("    sourceUriType={}", cm.getSourceUriType());
      if (cm.getSourceUriType() != null) {
        LOGGER.info("    sourceUriType value={}", cm.getSourceUriType().getValue());
      }

      // Log target scope details
      LOGGER.info("  Target Scope:");
      LOGGER.info("    targetUriType={}", cm.getTargetUriType());
      if (cm.getTargetUriType() != null) {
        LOGGER.info("    targetUriType value={}", cm.getTargetUriType().getValue());
      }

      // Log expected values
      LOGGER.info("  Expected Values:");
      LOGGER.info("    TEST_MAP_URL={}", TEST_MAP_URL);
      LOGGER.info("    TEST_MAP_TARGET={}", TEST_MAP_TARGET);
    });
  }
}
