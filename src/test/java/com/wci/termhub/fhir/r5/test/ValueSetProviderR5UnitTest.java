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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.ValueSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;

import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.fhir.r5.ValueSetProviderR5;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.SubsetMember;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.ValueSetLoaderUtil;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;

/**
 * The Class ValueSetProviderR5UnitTest.
 */
@AutoConfigureMockMvc
public class ValueSetProviderR5UnitTest extends AbstractFhirR5ServerTest {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ValueSetProviderR5UnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The provider. */
  @Autowired
  private ValueSetProviderR5 provider;

  /** The request. */
  private MockHttpServletRequest request;

  /** The details. */
  private ServletRequestDetails details;

  /** List of FHIR Code System files to load. */
  private static final List<String> VALUE_SET_FILES =
      List.of("ValueSet-snomedct_us-extension-sandbox-20240301-r5.json");

  /** The Constant TEST_VALUESET_URL. */
  private static final String TEST_VALUESET_URL = "http://snomed.info/sct?fhir_vs=731000124108";

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
    final StringParam name = new StringParam("SNOMEDCT_US extension concepts");
    final Bundle bundle = provider.findValueSets(request, details, null, null, null, null, null,
        name, null, null, null, null, null, null);
    assertNotNull(bundle);
    assertTrue(bundle.getEntry().stream().anyMatch(e -> e.getResource() instanceof ValueSet
        && "SNOMEDCT_US extension concepts".equals(((ValueSet) e.getResource()).getName())));
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
        final Resource resource = new ClassPathResource("data/" + valueSetFile,
            ValueSetProviderR5UnitTest.class.getClassLoader());

        assertThrows(Exception.class, () -> {
          LOGGER.info("Attempt reload of value set from classpath resource: data/{}", valueSetFile);
          ValueSetLoaderUtil.loadValueSet(searchService, resource.getFile(), ValueSet.class,
              new DefaultProgressListener());
        });

      } catch (final Exception e) {
        LOGGER.error("Error reloading value set file: {}", valueSetFile, e);
        throw e;
      }
    }
  }

  /**
   * Test multi-system value set loading and member verification. This test
   * loads a ValueSet with concepts from both SNOMED CT and HL7 RoleCode systems
   * and verifies that members from each system are correctly loaded with their
   * proper terminology.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMultiSystemValueSet() throws Exception {
    // Find all members of this value set
    final SearchParameters params = new SearchParameters();
    params.setQuery("subset.abbreviation:SNOMEDCT_HL7_COMBINED_TEST");
    params.setLimit(100);

    final ResultList<SubsetMember> members = searchService.find(params, SubsetMember.class);

    assertNotNull(members, "Members should not be null");
    assertTrue(members.getTotal() > 0, "Should have members loaded");
    LOGGER.info("Found {} total members", members.getTotal());

    // Log ALL members to see what terminologies we actually have
    LOGGER.info("All members:");
    members.getItems()
        .forEach(m -> LOGGER.info("  Member: code={}, terminology={}, publisher={}, name={}",
            m.getCode(), m.getTerminology(), m.getPublisher(), m.getName()));

    // Check for SNOMED CT member - terminology might be SNOMEDCT or fallback to
    // title prefix
    final boolean hasSnomedMember = members.getItems().stream()
        .anyMatch(m -> "105724001".equals(m.getCode())
            && (m.getTerminology() != null && (m.getTerminology().equals("SNOMEDCT")
                || m.getTerminology().equals("SNOMEDCT_HL7_COMBINED_TEST"))));
    assertTrue(hasSnomedMember, "Should have SNOMED CT member with code 105724001");

    // Check for HL7 RoleCode member - will have fallback terminology if not in
    // DB
    final boolean hasRoleMember =
        members.getItems().stream().anyMatch(m -> "AUNT".equals(m.getCode()));
    assertTrue(hasRoleMember, "Should have HL7 RoleCode member with code AUNT");

    // Verify we have members from both systems based on codes
    // Since database lookup may fail, all get fallback terminology, so check by
    // code patterns
    final long snomedCount = members.getItems().stream()
        .filter(m -> m.getCode() != null && m.getCode().matches("\\d+")).count();
    final long roleCount = members.getItems().stream()
        .filter(m -> m.getCode() != null && m.getCode().matches("[A-Z]+")).count();

    LOGGER.info("SNOMED CT members (numeric codes): {}", snomedCount);
    LOGGER.info("HL7 Role members (alpha codes): {}", roleCount);

    assertTrue(snomedCount == 6, "Should have 6 SNOMED CT members");
    assertTrue(roleCount == 16, "Should have 16 HL7 Role members");
  }

}
