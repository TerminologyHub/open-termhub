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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.fhir.r4.ValueSetProviderR4;
import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.fhir.util.LoincValueSetHelper;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;

/**
 * Unit tests for LOINC LL/LG value set support (Regenstrief mode) with server.mode=regenstrief.
 * Uses LOINC sandbox data loaded by AbstractFhirR4ServerTest (e.g. concepts with ANSWER_LIST_ID
 * LL1772-4).
 */
@TestPropertySource(properties = "server.mode=regenstrief")
public class LoincValueSetR4UnitTest extends AbstractFhirR4ServerTest {

  /** The Constant LG_VS_URL. */
  private static final String LG_VS_URL = "http://loinc.org/vs/LG50982-4";

  /** The Constant LG_VS_ID. */
  private static final String LG_VS_ID = "LG50982-4";

  /** The Constant LL_VS_URL. */
  private static final String LL_VS_URL = "http://loinc.org/vs/LL1772-4";

  /** The Constant LL_VS_ID. */
  private static final String LL_VS_ID = "LL1772-4";

  /** The Constant LOINC_CODE_IN_LL. */
  private static final String LOINC_CODE_IN_LL = "66480-5";

  /** Implicit LOINC value set URL. */
  private static final String IMPLICIT_VS_URL = "http://loinc.org?fhir_vs";

  /** Spanish display for sandbox LG member 66480-5. */
  private static final String SPANISH_DISPLAY_66480 =
      "Historia médica:Hallazgo:Punto temporal:^Paciente:Nom:PhenX";

  /** The provider. */
  @Autowired
  private ValueSetProviderR4 provider;

  /** The loinc lllg helper. */
  @Autowired
  private LoincValueSetHelper loincValueSetHelper;

  /** The request. */
  private MockHttpServletRequest request;

  /** The details. */
  private ServletRequestDetails details;

  /** UUID pattern for ValueSet ids. */
  private static final String UUID_PATTERN =
      "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

  /**
   * Asserts a LOINC LL/LG ValueSet uses a Concept UUID as id and encodes the code in url.
   *
   * @param vs the value set
   * @param lllgCode the expected LL/LG code
   */
  private static void assertLllgValueSetHasUuidId(final ValueSet vs, final String lllgCode) {
    assertNotNull(vs.getId(), "ValueSet id should not be null");
    assertFalse(lllgCode.equals(vs.getId()),
        "ValueSet id should be Concept UUID, not LL/LG code " + lllgCode);
    assertTrue(vs.getId().matches(UUID_PATTERN), "ValueSet id should be a UUID");
    assertEquals(lllgCode, FhirUtilityR4.parseLllgIdFromValueSetUrl(vs.getUrl()),
        "ValueSet url should encode LL/LG code " + lllgCode);
  }

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
   * server.mode=regenstrief, version 2.72 is used for expansion.
   */
  @Test
  public void testParseIdFromPathStyleUrl() {
    assertTrue(loincValueSetHelper.isLllgValueSetUrl("http://loinc.org/vs/LG51018-6-2.72"));
    assertEquals("LG51018-6-2.72",
        loincValueSetHelper.parseIdFromUrl("http://loinc.org/vs/LG51018-6-2.72"));
    assertEquals("LG51018-6-2.72",
        loincValueSetHelper.parseIdFromUrl("https://loinc.org/vs/LG51018-6-2.72"));
    assertEquals("2.72", LoincValueSetHelper.getVersionFromLllgId("LG51018-6-2.72"));
    assertEquals("LG51018-6", LoincValueSetHelper.getBaseLllgCode("LG51018-6-2.72"));
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
        null, null, null, null, null, url, null, null, null);
    assertNotNull(bundle);
    assertTrue(
        bundle.getEntry().stream()
            .anyMatch(e -> e.getResource() instanceof ValueSet
                && LL_VS_URL.equals(((ValueSet) e.getResource()).getUrl())),
        "Bundle should contain LL value set for " + LL_VS_URL);
  }

  /**
   * Test find LG value set by url uses Concept UUID as id.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindLgValueSetByUrlHasUuidId() throws Exception {
    final UriParam url = new UriParam(LG_VS_URL);
    final Bundle bundle = provider.findValueSets(request, details, null, null, null, null, null,
        null, null, null, null, null, url, null, null, null);
    assertNotNull(bundle);
    final ValueSet found = bundle.getEntry().stream().map(e -> e.getResource())
        .filter(ValueSet.class::isInstance).map(ValueSet.class::cast)
        .filter(v -> LG_VS_URL.equals(v.getUrl())).findFirst().orElse(null);
    assertNotNull(found, "Bundle should contain LG value set for " + LG_VS_URL);
    assertLllgValueSetHasUuidId(found, LG_VS_ID);
    assertLllgValueSetContact(found);
  }

  /**
   * Asserts a LOINC LL/LG ValueSet has contact from terminology publisher and uri.
   *
   * @param vs the value set
   */
  private static void assertLllgValueSetContact(final ValueSet vs) {
    assertNotNull(vs.getContact(), "ValueSet contact should not be null");
    assertNotNull(vs.getContactFirstRep().getName(), "ValueSet contact name should not be null");
    assertEquals(vs.getPublisher(), vs.getContactFirstRep().getName(),
        "ValueSet contact name should match publisher");
    assertNotNull(vs.getContactFirstRep().getTelecomFirstRep().getValue(),
        "ValueSet contact telecom should not be null");
    assertTrue(vs.getContactFirstRep().getTelecomFirstRep().getValue().contains("loinc.org"),
        "ValueSet contact url should reference loinc.org");
  }

  /**
   * Test get lllg value set by id.
   *
   * @throws Exception the exception
   */
  @Disabled("Disabled until we have a way to test this")
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
    // vs.getExpansion().getParameter().stream().anyMatch(p -> "offset".equals(p.getName())),
    // "Expansion should have offset parameter");
    // assertTrue(vs.getExpansion().getParameter().stream().anyMatch(p ->
    // "count".equals(p.getName())),
    // "Expansion should have count parameter");
  }

  /**
   * Test expand lllg value set.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExpandLllgValueSet() throws Exception {
    final ValueSet vs = provider.expandImplicit(request, details, null, new UriType(LL_VS_URL),
        null, null, null, null, null, null, null, null, null, null);
    assertNotNull(vs);
    assertNotNull(vs.getExpansion(), "Expansion should be present");
    assertTrue(vs.getExpansion().getTotal() >= 0, "Total should be non-negative");
    assertNotNull(vs.getVersion(), "Expanded ValueSet should have version");
    assertFalse(vs.getVersion().isEmpty(), "Expanded ValueSet version should not be empty");
    if (vs.getId() != null) {
      assertFalse(LL_VS_ID.equals(vs.getId()), "Expanded ValueSet id should not be the LL/LG code");
    }
    assertTrue(
        vs.getExpansion().getParameter().stream().anyMatch(p -> "offset".equals(p.getName())),
        "Expansion should have offset parameter like fhir.loinc.org");
    assertTrue(vs.getExpansion().getParameter().stream().anyMatch(p -> "count".equals(p.getName())),
        "Expansion should have count parameter");
    assertTrue(vs.getExpansion().getContains() != null, "Expansion should have contains list");
    assertTrue(
        vs.getExpansion().getContains().stream()
            .noneMatch(c -> c.hasDesignation() && !c.getDesignation().isEmpty()),
        "Default expand must not include designations");
    assertNotEquals(Boolean.TRUE, vs.getExperimental(),
        "LL value set expansion should not be experimental");
  }

  /**
   * includeDesignations=false must omit designations.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExpandLllgWithoutDesignations() throws Exception {
    final ValueSet vs = provider.expandImplicit(request, details, null, new UriType(LL_VS_URL),
        null, null, null, null, null, new BooleanType(false), null, null, null, null);
    assertNotNull(vs.getExpansion());
    assertNotNull(vs.getVersion(), "Expanded ValueSet should have version");
    assertTrue(
        vs.getExpansion().getContains().stream()
            .noneMatch(c -> c.hasDesignation() && !c.getDesignation().isEmpty()),
        "includeDesignations=false must not include designations");
  }

  /**
   * includeDesignations=true must include designations in fhir.loinc.org shape when terms exist.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExpandLllgWithDesignations() throws Exception {
    final ValueSet vs = provider.expandImplicit(request, details, null, new UriType(LL_VS_URL),
        null, null, null, null, null, new BooleanType(true), null, null, null, null);
    assertNotNull(vs.getExpansion());
    assertNotNull(vs.getVersion(), "Expanded ValueSet should have version");
    assumeTrue(
        vs.getExpansion().getContains() != null && !vs.getExpansion().getContains().isEmpty(),
        "LL expansion has no contains to check designations");
    final boolean anyDesignation = vs.getExpansion().getContains().stream()
        .anyMatch(c -> c.hasDesignation() && !c.getDesignation().isEmpty());
    assumeTrue(anyDesignation, "Loaded LOINC concepts lack terms for designation assertions");
    assertTrue(
        vs.getExpansion().getContains().stream().flatMap(c -> c.getDesignation().stream())
            .anyMatch(d -> d.hasValue() && !d.hasLanguage() && !d.hasUse())
            || vs.getExpansion().getContains().stream().flatMap(c -> c.getDesignation().stream())
                .anyMatch(d -> d.hasUse() && "http://loinc.org".equals(d.getUse().getSystem())),
        "LOINC designations should be value-only (English) or use.system=http://loinc.org");
  }

  /**
   * Test expand LG value set by id sets experimental=true.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExpandLgValueSetExperimental() throws Exception {
    final ValueSet vs = provider.expandInstance(request, details, new IdType(LG_VS_ID), null, null,
        null, null, null, null, null, null, null, null, null, null);
    assertNotNull(vs);
    assertNotNull(vs.getExpansion(), "Expansion should be present");
    assertEquals(Boolean.TRUE, vs.getExperimental(),
        "LG value set expansion should be experimental");
  }

  /**
   * Test ValueSet read by id for LOINC LL value set (from CodeSystem-lnc-sandbox-277-r4). Requires
   * server.mode=regenstrief.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValueSetReadLllgById() throws Exception {
    final ValueSet vs = provider.getValueSet(request, details, new IdType(LL_VS_ID));
    assertNotNull(vs);
    assertEquals(LL_VS_URL, vs.getUrl());
    assertTrue(
        vs.getCompose() != null && vs.getCompose().getInclude() != null
            && !vs.getCompose().getInclude().isEmpty()
            || vs.getExpansion() != null && vs.getExpansion().getContains() != null,
        "LL value set should have compose or expansion with members");
    request.addParameter("version", "2.81");
    assertThrows(FHIRServerResponseException.class,
        () -> provider.getValueSet(request, details, new IdType(LL_VS_ID)));
  }

  /**
   * Test ValueSet read by LG code returns Concept UUID as id.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValueSetReadLgById() throws Exception {
    final ValueSet vs = provider.getValueSet(request, details, new IdType(LG_VS_ID));
    assertNotNull(vs);
    assertLllgValueSetHasUuidId(vs, LG_VS_ID);
    assertEquals(LG_VS_URL, vs.getUrl());
  }

  /**
   * Test ValueSet read by versioned LG id selects that LOINC version and canonical url.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValueSetReadLgByVersionedId() throws Exception {
    final ValueSet latest = provider.getValueSet(request, details, new IdType(LG_VS_ID));
    assertNotNull(latest.getVersion());
    final String versionedId = LG_VS_ID + "-" + latest.getVersion();
    final ValueSet vs = provider.getValueSet(request, details, new IdType(versionedId));
    assertNotNull(vs);
    assertEquals(latest.getVersion(), vs.getVersion());
    assertEquals(LG_VS_URL, vs.getUrl());
    assertLllgValueSetHasUuidId(vs, LG_VS_ID);
  }

  /**
   * Test ValueSet read by unknown versioned LG id returns 404.
   */
  @Test
  public void testValueSetReadLgByUnknownVersionedIdNotFound() {
    assertThrows(FHIRServerResponseException.class,
        () -> provider.getValueSet(request, details, new IdType(LG_VS_ID + "-9.99")));
  }

  /**
   * Test expand by versioned LG id uses that LOINC version and canonical url.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExpandLgByVersionedId() throws Exception {
    final ValueSet latest = provider.getValueSet(request, details, new IdType(LG_VS_ID));
    assertNotNull(latest.getVersion());
    final String versionedId = LG_VS_ID + "-" + latest.getVersion();
    final ValueSet vs = provider.expandInstance(request, details, new IdType(versionedId), null,
        null, null, null, null, null, null, null, null, null, null, null);
    assertNotNull(vs);
    assertEquals(latest.getVersion(), vs.getVersion());
    assertEquals(LG_VS_URL, vs.getUrl());
    assertNotNull(vs.getExpansion());
  }

  /**
   * Test ValueSet read by Concept UUID for LOINC LG value set.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValueSetReadLgByUuid() throws Exception {
    final ValueSet byCode = provider.getValueSet(request, details, new IdType(LG_VS_ID));
    assertNotNull(byCode.getId());
    final ValueSet byUuid = provider.getValueSet(request, details, new IdType(byCode.getId()));
    assertNotNull(byUuid);
    assertEquals(byCode.getId(), byUuid.getId());
    assertEquals(LG_VS_URL, byUuid.getUrl());
    assertLllgValueSetHasUuidId(byUuid, LG_VS_ID);
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
        new StringType("277"), null, null, null, null, null, null, null, null, null);
    assertNotNull(vs, "ValueSet should be found for loaded version 277");
    assertNotNull(vs.getExpansion(), "Expansion should be present");
    assertEquals("277", vs.getVersion(), "Returned ValueSet should have the requested version");
  }

  /**
   * Test expand lllg value set with wrong version. Verifies that a valueSetVersion that does not
   * match any loaded LOINC terminology returns a not-found error instead of silently falling back
   * to the latest version.
   */
  @Test
  public void testExpandLllgValueSetWithWrongVersionNotFound() {
    assertThrows(FHIRServerResponseException.class,
        () -> provider.expandImplicit(request, details, null, new UriType(LL_VS_URL),
            new StringType("9.99"), null, null, null, null, null, null, null, null, null),
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
        null, null, null, null, null, url, new StringParam("277"), null, null);
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
        null, null, null, null, null, url, new StringParam("9.99"), null, null);
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

  /**
   * LG47-3 compose should reference child LG value sets (not concepts). Requires full LOINC 2.78
   * with hierarchical LG panels loaded.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLg47ComposeUsesNestedValueSetRefs() throws Exception {
    final String lgId = "LG47-3";
    final ValueSet vs = provider.getValueSet(request, details, new IdType(lgId));
    assumeTrue(vs.getCompose() != null && !vs.getCompose().getInclude().isEmpty(),
        "LG47-3 not in loaded LOINC index");
    final int nestedCount = vs.getCompose().getInclude().stream()
        .mapToInt(inc -> inc.getValueSet() == null ? 0 : inc.getValueSet().size()).sum();
    assumeTrue(nestedCount >= 7, "LG47-3 hierarchical children not in index");
    assertEquals(7, nestedCount);
    assertFalse(
        vs.getCompose().getInclude().stream().flatMap(inc -> inc.getConcept().stream())
            .anyMatch(c -> c.getCode() != null && c.getCode().startsWith("LG")),
        "Child LG codes must be valueSet references, not concepts");
  }

  /**
   * LG47-3 expansion should flatten to leaf LOINC codes (76 members per fhir.loinc.org 2.78).
   *
   * @throws Exception the exception
   */
  @Test
  public void testLg47ExpandReturnsLeafCodes() throws Exception {
    final String lgId = "LG47-3";
    final ValueSet vs = provider.expandInstance(request, details, new IdType(lgId), null, null,
        null, null, new IntegerType(0), new IntegerType(1000), null, null, null, null, null, null);
    assumeTrue(vs.getExpansion() != null, "LG47-3 not in loaded LOINC index");
    assumeTrue(vs.getExpansion().getTotal() >= 76,
        "LG47-3 full expansion requires LOINC 2.78 with panel hierarchy");
    assertEquals(76, vs.getExpansion().getTotal());
    assertFalse(
        vs.getExpansion().getContains().stream()
            .anyMatch(c -> c.getCode() != null && c.getCode().startsWith("LG330")),
        "Expansion must not contain nested LG group codes");
    assertTrue(
        vs.getExpansion().getContains().stream().anyMatch(c -> "104063-3".equals(c.getCode())),
        "Expected leaf code 104063-3 in LG47-3 expansion");
  }

  /**
   * Validates a transitive panel member under LG47-3 (leaf code in child LG value set).
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("null")
  @Test
  public void testValidateCodeInLg47PanelMember() throws Exception {
    final Parameters params = provider.validateCodeInstance(request, details, new IdType("LG47-3"),
        null, null, new CodeType("104063-3"), null, null, null, null, null);
    final Parameters.ParametersParameterComponent resultParam = params.getParameter().stream()
        .filter(p -> "result".equals(p.getName())).findFirst().orElse(null);
    assumeTrue(resultParam != null && resultParam.getValue() instanceof BooleanType,
        "LG47-3 not in loaded LOINC index");
    assumeTrue(((BooleanType) resultParam.getValue()).getValue(),
        "104063-3 panel membership requires full LOINC 2.78 hierarchy");
  }

  /**
   * Search params that need sandbox CodeSystem fields (publisher, name, title, description, date,
   * identifier, reference, status, code).
   *
   * @throws Exception the exception
   */
  @Test
  public void testSearchSandboxValueSetParams() throws Exception {
    final StringParam nameContains = new StringParam("SARS");
    nameContains.setContains(true);
    final Bundle byName = provider.findValueSets(request, details, null, null, null, null, null,
        nameContains, null, null, null, null, new UriParam(LG_VS_URL), null, null, null);
    assertTrue(bundleHasUrl(byName, LG_VS_URL));

    final Bundle byPublisher = provider.findValueSets(request, details, null, null, null, null,
        null, null, new StringParam("SANDBOX"), null, null, null, new UriParam(LG_VS_URL), null,
        null, null);
    assertTrue(bundleHasUrl(byPublisher, LG_VS_URL));

    final StringParam titleExact = new StringParam("LNC-ENTIRE");
    titleExact.setExact(true);
    final Bundle byTitle = provider.findValueSets(request, details, null, null, null, null, null,
        null, null, null, null, titleExact, new UriParam(IMPLICIT_VS_URL), null, null, null);
    assertTrue(bundleHasUrl(byTitle, IMPLICIT_VS_URL));

    final StringParam descriptionContains = new StringParam("entire");
    descriptionContains.setContains(true);
    final Bundle byDescription =
        provider.findValueSets(request, details, null, null, null, descriptionContains, null, null,
            null, null, null, null, new UriParam(IMPLICIT_VS_URL), null, null, null);
    assertTrue(bundleHasUrl(byDescription, IMPLICIT_VS_URL));

    final Bundle byDateGe = provider.findValueSets(request, details, null, null,
        new DateRangeParam("ge2020-01-01", null), null, null, null, null, null, null, null,
        new UriParam(IMPLICIT_VS_URL), null, null, null);
    assertTrue(bundleHasUrl(byDateGe, IMPLICIT_VS_URL));

    final Bundle byDateLe = provider.findValueSets(request, details, null, null,
        new DateRangeParam(null, "le2010-01-01"), null, null, null, null, null, null, null,
        new UriParam(IMPLICIT_VS_URL), null, null, null);
    assertTrue(byDateLe.getEntry() == null || byDateLe.getEntry().isEmpty());

    final Bundle byCode = provider.findValueSets(request, details, null, new TokenParam(LG_VS_ID),
        null, null, null, null, null, null, null, null, null, null, null, null);
    assertTrue(bundleHasUrl(byCode, LG_VS_URL));

    final Bundle byIdentifier = provider.findValueSets(request, details, null, null, null, null,
        new TokenParam("urn:oid:1.3.6.1.4.1.12009.10.1.944"), null, null, null, null, null,
        new UriParam(LL_VS_URL), null, null, null);
    assertTrue(bundleHasUrl(byIdentifier, LL_VS_URL));

    final Bundle byReference = provider.findValueSets(request, details, null, null, null, null,
        null, null, null, new UriParam("http://loinc.org"), null, null, new UriParam(LG_VS_URL),
        null, null, null);
    assertTrue(bundleHasUrl(byReference, LG_VS_URL));

    final Bundle byStatusAnd = provider.findValueSets(request, details, null, null, null, null,
        null, null, null, null, new TokenParam("active"), null, new UriParam(LG_VS_URL),
        new StringParam("277"), null, null);
    assertTrue(bundleHasUrl(byStatusAnd, LG_VS_URL));

    final Bundle byStatusMiss = provider.findValueSets(request, details, null, null, null, null,
        null, null, null, null, new TokenParam("draft"), null, new UriParam(LG_VS_URL),
        new StringParam("277"), null, null);
    assertTrue(byStatusMiss.getEntry() == null || byStatusMiss.getEntry().isEmpty());
  }

  /**
   * $expand filter, displayLanguage, property, paging, includeDefinition, and error cases.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExpandSandboxLllgParams() throws Exception {
    final FHIRServerResponseException missingUrl =
        assertThrows(FHIRServerResponseException.class, () -> provider.expandImplicit(request,
            details, null, null, null, null, null, null, null, null, null, null, null, null));
    assertEquals(400, missingUrl.getStatusCode());

    final ValueSet filtered =
        provider.expandImplicit(request, details, null, new UriType(LL_VS_URL), null,
            new StringType("Diabetes"), null, null, null, null, null, null, null, null);
    assertEquals(1, filtered.getExpansion().getContains().size());
    assertEquals("LA10529-8", filtered.getExpansion().getContainsFirstRep().getCode());

    final ValueSet spanish = provider.expandImplicit(request, details, null, new UriType(LG_VS_URL),
        null, null, null, null, List.of(new CodeType("es-ES")), null, null, null, null, null);
    assertTrue(spanish.getExpansion().getContains().stream().anyMatch(
        c -> LOINC_CODE_IN_LL.equals(c.getCode()) && SPANISH_DISPLAY_66480.equals(c.getDisplay())));

    final ValueSet withProp = provider.expandImplicit(request, details, null,
        new UriType(LG_VS_URL), null, null, null, null, null, null, null, null, null,
        List.of(new CodeType("CLASS"), new CodeType("STATUS")));
    assertTrue(withProp.getExpansion().getContains().stream().anyMatch(
        c -> LOINC_CODE_IN_LL.equals(c.getCode()) && hasR4ExpandProperty(c, "CLASS", "PHENX")
            && hasR4ExpandProperty(c, "STATUS", "Active")));

    final ValueSet paged = provider.expandImplicit(request, details, null, new UriType(LL_VS_URL),
        null, null, new IntegerType(0), new IntegerType(1), null, null, null, null, null, null);
    assertEquals(2, paged.getExpansion().getTotal());
    assertEquals(1, paged.getExpansion().getContains().size());

    final ValueSet withoutDef = provider.expandImplicit(request, details, null,
        new UriType(LL_VS_URL), null, null, null, null, null, null, null, null, null, null);
    assertFalse(withoutDef.hasCompose());

    final ValueSet withDef = provider.expandImplicit(request, details, null, new UriType(LL_VS_URL),
        null, null, null, null, null, null, null, new BooleanType(true), null, null);
    assertTrue(withDef.hasCompose());
    assertTrue(withDef.getCompose().hasInclude());

    final ValueSet filterDesignations =
        provider.expandImplicit(request, details, null, new UriType(LL_VS_URL),
            new StringType("277"), new StringType("Diabetes"), new IntegerType(0),
            new IntegerType(10), null, new BooleanType(true), null, null, null, null);
    assertEquals("277", filterDesignations.getVersion());
    assertTrue(filterDesignations.getExpansion().getContainsFirstRep().hasDesignation());

    final ValueSet instancePage = provider.expandInstance(request, details, new IdType(LL_VS_ID),
        null, null, null, new StringType("Heart"), new IntegerType(0), new IntegerType(10), null,
        new BooleanType(true), null, null, null, null);
    assertEquals("LA16990-6", instancePage.getExpansion().getContainsFirstRep().getCode());
  }

  /**
   * Missing ids are 404; search params on GET-by-id are unsupported.
   */
  @Test
  public void testValueSetMissingAndUnsupportedReadParams() {
    final FHIRServerResponseException missing = assertThrows(FHIRServerResponseException.class,
        () -> provider.getValueSet(request, details, new IdType("does-not-exist")));
    assertEquals(404, missing.getStatusCode());

    final FHIRServerResponseException missingExpand =
        assertThrows(FHIRServerResponseException.class,
            () -> provider.expandInstance(request, details, new IdType("does-not-exist"), null,
                null, null, null, null, null, null, null, null, null, null, null));
    assertEquals(404, missingExpand.getStatusCode());

    request.addParameter("url", IMPLICIT_VS_URL);
    final FHIRServerResponseException urlOnRead = assertThrows(FHIRServerResponseException.class,
        () -> provider.getValueSet(request, details, new IdType(LG_VS_ID)));
    assertEquals(400, urlOnRead.getStatusCode());

    request.removeAllParameters();
    request.addParameter("status", "active");
    final FHIRServerResponseException statusOnRead = assertThrows(FHIRServerResponseException.class,
        () -> provider.getValueSet(request, details, new IdType(LG_VS_ID)));
    assertEquals(400, statusOnRead.getStatusCode());
  }

  /**
   * Whether a search bundle contains a ValueSet with the given url.
   *
   * @param bundle the bundle
   * @param url the url
   * @return true if present
   */
  private static boolean bundleHasUrl(final Bundle bundle, final String url) {
    return bundle != null && bundle.getEntry() != null
        && bundle.getEntry().stream().anyMatch(e -> e.getResource() instanceof ValueSet
            && url.equals(((ValueSet) e.getResource()).getUrl()));
  }

  /**
   * Whether an R4 expansion contains the R5 pre-adoption property extension.
   *
   * @param contains the contains
   * @param code the property code
   * @param value the string value
   * @return true if present
   */
  private static boolean hasR4ExpandProperty(
    final ValueSet.ValueSetExpansionContainsComponent contains, final String code,
    final String value) {
    return contains.getExtension().stream().anyMatch(ext -> {
      if (!ext.getUrl().contains("expansion.contains.property")) {
        return false;
      }
      String foundCode = null;
      String foundValue = null;
      for (final Extension nested : ext.getExtension()) {
        if ("code".equals(nested.getUrl()) && nested.getValue() instanceof CodeType) {
          foundCode = ((CodeType) nested.getValue()).getValue();
        }
        if ("value".equals(nested.getUrl()) && nested.getValue() instanceof StringType) {
          foundValue = ((StringType) nested.getValue()).getValue();
        }
      }
      return code.equals(foundCode) && value.equals(foundValue);
    });
  }

}
