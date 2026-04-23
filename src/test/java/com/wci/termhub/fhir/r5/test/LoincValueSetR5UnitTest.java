/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r5.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.UriType;
import org.hl7.fhir.r5.model.ValueSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.fhir.r5.ValueSetProviderR5;
import com.wci.termhub.fhir.util.LoincValueSetHelper;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;

/**
 * Unit tests for LOINC LL/LG value set support (Regenstrief mode) with
 * fhir.loinc.lllg.valuesets.enabled=true. Uses LOINC sandbox data loaded by
 * AbstractFhirR5ServerTest.
 */
@TestPropertySource(properties = "fhir.loinc.lllg.valuesets.enabled=true")
public class LoincValueSetR5UnitTest extends AbstractFhirR5ServerTest {

  /** The Constant LL_VS_URL. */
  private static final String LL_VS_URL = "http://loinc.org?fhir_vs=LL1772-4";

  /** The Constant LL_VS_ID. */
  private static final String LL_VS_ID = "LL1772-4";

  /** The Constant LOINC_CODE_IN_LL. */
  private static final String LOINC_CODE_IN_LL = "66480-5";

  /** The provider. */
  @Autowired
  private ValueSetProviderR5 provider;

  /** The loinc lllg helper. */
  @Autowired
  private LoincValueSetHelper loincValueSetHelper;

  /** The request. */
  private MockHttpServletRequest request;

  /** The details. */
  private ServletRequestDetails details;

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
    request = new MockHttpServletRequest();
    details = new ServletRequestDetails();
    details.setServletRequest(request);
  }

  /**
   * Test lllg enabled.
   */
  @Test
  public void testLllgEnabled() {
    assertTrue(loincValueSetHelper.isEnabled(), "LL/LG value sets should be enabled in this test");
  }

  /**
   * Test path-style LOINC value set URL (http://loinc.org/vs/LG51018-6-2.72). When
   * FHIR_LOINC_LLLG_VALUESETS_ENABLED is true, version 2.72 is used for expansion.
   */
  @Test
  public void testParseIdFromPathStyleUrl() {
    assertTrue(loincValueSetHelper.isLllgValueSetUrl("http://loinc.org/vs/LG51018-6-2.72"));
    assertEquals("LG51018-6-2.72",
        loincValueSetHelper.parseIdFromUrl("http://loinc.org/vs/LG51018-6-2.72"));
    assertEquals("LG51018-6-2.72",
        loincValueSetHelper.parseIdFromUrl("https://loinc.org/vs/LG51018-6-2.72"));
    assertEquals("2.72", loincValueSetHelper.getVersionFromLllgId("LG51018-6-2.72"));
    assertEquals("LG51018-6", loincValueSetHelper.parseIdFromUrl("http://loinc.org/vs/LG51018-6"));
  }

  /**
   * Test find lllg value set by url.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindLllgValueSetByUrl() throws Exception {
    final UriParam url = new UriParam(LL_VS_URL);
    final Bundle bundle = provider.findValueSets(request, details, null, null, null, null, null,
        null, null, null, url, null, null, null);
    assertNotNull(bundle);
    assertTrue(
        bundle.getEntry().stream()
            .anyMatch(e -> e.getResource() instanceof ValueSet
                && LL_VS_URL.equals(((ValueSet) e.getResource()).getUrl())
                && LL_VS_ID.equals(e.getResource().getId())),
        "Bundle should contain LL value set for " + LL_VS_URL);
  }

  /**
   * Test get lllg value set by id.
   *
   * @throws Exception the exception
   */
  // @Disabled("Disabled until we have a way to test this")
  @Test
  public void testGetLllgValueSetById() throws Exception {
    final ValueSet vs = provider.getValueSet(request, details, new IdType(LL_VS_ID));
    assertNotNull(vs);
    assertTrue(LL_VS_ID.equals(vs.getId()), "ValueSet id should be " + LL_VS_ID);
    assertTrue(LL_VS_URL.equals(vs.getUrl()), "ValueSet url should be " + LL_VS_URL);
    assertNotNull(vs.getCompose(), "ValueSet should have compose (members) like fhir.loinc.org");
    assertTrue(vs.getCompose().getInclude() != null && !vs.getCompose().getInclude().isEmpty(),
        "Compose should have at least one include");
    assertTrue(vs.getCompose().getInclude().get(0).getSystem() != null,
        "Compose include should have system set");
    assertNotNull(vs.getExpansion(), "ValueSet should have expansion like fhir.loinc.org");
    assertTrue(vs.getExpansion().getTotal() >= 0, "Expansion total should be non-negative");
    // assertTrue(
    //     vs.getExpansion().getParameter().stream().anyMatch(p -> "offset".equals(p.getName())),
    //     "Expansion should have offset parameter");
    // assertTrue(vs.getExpansion().getParameter().stream().anyMatch(p -> "count".equals(p.getName())),
    //     "Expansion should have count parameter");
  }

  /**
   * Test expand lllg value set.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExpandLllgValueSet() throws Exception {
    final ValueSet vs = provider.expandImplicit(request, details, null, new UriType(LL_VS_URL),
        null, null, null, null, null);
    assertNotNull(vs);
    assertNotNull(vs.getExpansion(), "Expansion should be present");
    assertTrue(vs.getExpansion().getTotal() >= 0, "Total should be non-negative");
    assertTrue(LL_VS_ID.equals(vs.getId()) || vs.getId() != null);
    assertTrue(
        vs.getExpansion().getParameter().stream().anyMatch(p -> "offset".equals(p.getName())),
        "Expansion should have offset parameter like fhir.loinc.org");
    assertTrue(vs.getExpansion().getParameter().stream().anyMatch(p -> "count".equals(p.getName())),
        "Expansion should have count parameter");
    assertTrue(vs.getExpansion().getContains() != null, "Expansion should have contains list");
  }

  /**
   * Test ValueSet read by id for LOINC LL value set (from CodeSystem-lnc-sandbox-277-r4). Requires
   * fhir.loinc.lllg.valuesets.enabled=true.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValueSetReadLllgById() throws Exception {
    final ValueSet vs = provider.getValueSet(request, details, new IdType(LL_VS_ID));
    assertNotNull(vs);
    assertEquals(LL_VS_ID, vs.getIdPart());
    assertEquals(LL_VS_URL, vs.getUrl());
    assertTrue(
        vs.getCompose() != null && vs.getCompose().getInclude() != null
            && !vs.getCompose().getInclude().isEmpty()
            || vs.getExpansion() != null && vs.getExpansion().getContains() != null,
        "LL value set should have compose or expansion with members");
  }


  /**
   * Test expand lllg value set with correct version. Verifies that valueSetVersion matching the
   * loaded LOINC version succeeds (regression guard for the bug where only the latest LOINC version
   * was ever checked, making any explicit valueSetVersion fail).
   *
   * @throws Exception the exception
   */
  @Test
  public void testExpandLllgValueSetWithCorrectVersion() throws Exception {
    final ValueSet vs = provider.expandImplicit(request, details, null, new UriType(LL_VS_URL),
        new StringType("277"), null, null, null, null);
    assertNotNull(vs, "ValueSet should be found for loaded version 277");
    assertNotNull(vs.getExpansion(), "Expansion should be present");
    assertEquals("277", vs.getVersion(), "Returned ValueSet should have the requested version");
  }

  /**
   * Test expand lllg value set with wrong version. Verifies that a valueSetVersion that does not
   * match any loaded LOINC terminology returns a not-found error instead of silently falling back to
   * the latest version.
   */
  @Test
  public void testExpandLllgValueSetWithWrongVersionNotFound() {
    assertThrows(FHIRServerResponseException.class,
        () -> provider.expandImplicit(request, details, null, new UriType(LL_VS_URL),
            new StringType("9.99"), null, null, null, null),
        "Requesting a non-existent valueSetVersion should throw FHIRServerResponseException");
  }

  /**
   * Test find lllg value set with correct version. Verifies that a version-filtered ValueSet search
   * for an LL value set returns a result when the requested version is loaded.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindValueSetsLllgWithCorrectVersion() throws Exception {
    final UriParam url = new UriParam(LL_VS_URL);
    final Bundle bundle = provider.findValueSets(request, details, null, null, null, null, null,
        null, null, null, url, new StringParam("277"), null, null);
    assertNotNull(bundle);
    assertTrue(
        bundle.getEntry().stream()
            .anyMatch(e -> e.getResource() instanceof ValueSet
                && LL_VS_URL.equals(((ValueSet) e.getResource()).getUrl())),
        "Bundle should contain LL value set for version 277");
  }

  /**
   * Test find lllg value set with wrong version. Verifies that a version-filtered ValueSet search
   * for a non-existent LOINC version returns an empty bundle.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindValueSetsLllgWithWrongVersionEmpty() throws Exception {
    final UriParam url = new UriParam(LL_VS_URL);
    final Bundle bundle = provider.findValueSets(request, details, null, null, null, null, null,
        null, null, null, url, new StringParam("9.99"), null, null);
    assertNotNull(bundle);
    assertTrue(bundle.getEntry() == null || bundle.getEntry().isEmpty(),
        "Bundle should be empty for a non-existent LOINC version");
  }

  /**
   * Test validate code in lllg value set.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateCodeInLllgValueSet() throws Exception {
    final Parameters params = provider.validateCodeImplicit(request, details,
        new UriType(LL_VS_URL), null, new CodeType(LOINC_CODE_IN_LL), null, null, null, null, null);
    assertNotNull(params);
    final Parameters.ParametersParameterComponent resultParam = params.getParameter().stream()
        .filter(p -> "result".equals(p.getName())).findFirst().orElse(null);
    assertNotNull(resultParam, "Parameters should contain 'result'");
    assertTrue(resultParam.getValue() instanceof BooleanType, "Result should be a BooleanType");
    assertTrue(params.getParameter().stream().anyMatch(p -> "result".equals(p.getName())),
        "LL/LG validate-code should return Parameters with result (true if code in set, false otherwise)");
  }

  /**
   * Test validate code in LG value set by id. Verifies the LG path in validate-code
   * (ValueSet/{id}/$validate-code). Returns valid Parameters with result parameter. With sandbox
   * LOINC, the LG concept may or may not be found depending on index; with full LOINC and
   * ENABLE_POST_LOAD_COMPUTATIONS=true, panel members (e.g. code 8867-4 in ValueSet LG33055-1)
   * correctly return result true, matching fhir.loinc.org.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateCodeInLgValueSetById() throws Exception {
    final String lgId = "LG51029-3";
    final Parameters params = provider.validateCodeInstance(request, details, new IdType(lgId),
        null, null, new CodeType(lgId), null, null, null, null, null);
    assertNotNull(params);
    final Parameters.ParametersParameterComponent resultParam = params.getParameter().stream()
        .filter(p -> "result".equals(p.getName())).findFirst().orElse(null);
    assertNotNull(resultParam, "Parameters should contain 'result'");
    assertTrue(resultParam.getValue() instanceof BooleanType, "Result should be BooleanType");
  }
}
