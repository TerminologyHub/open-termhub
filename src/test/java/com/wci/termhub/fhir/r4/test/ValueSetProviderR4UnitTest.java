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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;
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

import com.wci.termhub.fhir.r4.ValueSetProviderR4;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;

/**
 * The Class ValueSetProviderR4UnitTest.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test-r4")
public class ValueSetProviderR4UnitTest {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ValueSetProviderR4UnitTest.class);

  /** The provider. */
  @Autowired
  private ValueSetProviderR4 provider;

  /** The request. */
  private MockHttpServletRequest request;

  /** The details. */
  private ServletRequestDetails details;

  /** The Constant TEST_VALUESET_URL. */
  private static final String TEST_VALUESET_URL =
      "http://snomed.info/sct?fhir_vs=900000000000012004";

  /**
   * Setup.
   */
  @BeforeEach
  public void setup() {
    request = new MockHttpServletRequest();
    details = new ServletRequestDetails();
    details.setServletRequest(request);
  }

  /**
   * Test find value sets.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindValueSets() throws Exception {
    final Bundle bundle = provider.findValueSets(request, details, null, null, null, null, null,
        null, null, null, null, null, null, null);
    assertNotNull(bundle);
    bundle.getEntry().forEach(entry -> {
      if (entry.getResource() instanceof ValueSet) {
        final ValueSet vs = (ValueSet) entry.getResource();
        LOGGER.info("FOUND ValueSet: id={}, url={}, name={}, version={}",
            vs.getIdElement().getIdPart(), vs.getUrl(), vs.getName(), vs.getVersion());
      }
    });

    assertTrue(bundle.getEntry().stream().anyMatch(e -> e.getResource() instanceof ValueSet));
  }

  /**
   * Test find value set by url.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindValueSetByUrl() throws Exception {
    final UriParam url = new UriParam(TEST_VALUESET_URL);
    final Bundle bundle = provider.findValueSets(request, details, null, null, null, null, null,
        null, null, null, url, null, null, null);
    assertNotNull(bundle);
    assertTrue(bundle.getEntry().stream().anyMatch(e -> e.getResource() instanceof ValueSet
        && TEST_VALUESET_URL.equals(((ValueSet) e.getResource()).getUrl())));
  }

  /**
   * Test find value set by name.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindValueSetByName() throws Exception {
    final StringParam name = new StringParam("SNOMEDCT model concepts");
    final Bundle bundle = provider.findValueSets(request, details, null, null, null, null, null,
        name, null, null, null, null, null, null);
    assertNotNull(bundle);
    assertTrue(bundle.getEntry().stream().anyMatch(e -> e.getResource() instanceof ValueSet
        && "SNOMEDCT model concepts".equals(((ValueSet) e.getResource()).getName())));
  }

  /**
   * Test find value set by version.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindValueSetByVersion() throws Exception {
    final StringParam version = new StringParam("20240301");
    final Bundle bundle = provider.findValueSets(request, details, null, null, null, null, null,
        null, null, null, null, version, null, null);
    assertNotNull(bundle);
    assertTrue(bundle.getEntry().stream().anyMatch(e -> e.getResource() instanceof ValueSet
        && "20240301".equals(((ValueSet) e.getResource()).getVersion())));
  }

  /**
   * Test get value set by id.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetValueSetById() throws Exception {
    // Get all ValueSets and find the one with the test URL
    final Bundle bundle = provider.findValueSets(request, details, null, null, null, null, null,
        null, null, null, null, null, null, null);
    assertNotNull(bundle);
    final ValueSet found = bundle.getEntry().stream()
        .filter(e -> e.getResource() instanceof ValueSet).map(e -> (ValueSet) e.getResource())
        .filter(vs -> TEST_VALUESET_URL.equals(vs.getUrl())).findFirst()
        .orElseThrow(() -> new AssertionError("Test ValueSet not found by URL"));
    final String id = found.getIdElement().getIdPart();
    final Bundle vsBundle = provider.findValueSets(request, details, new TokenParam(id), null, null,
        null, null, null, null, null, null, null, null, null);
    assertNotNull(vsBundle);
    assertTrue(vsBundle.getEntry().stream()
        .anyMatch(e -> e.getResource() instanceof ValueSet && id.equals(e.getResource().getId())));
  }

}
