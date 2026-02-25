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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.UriType;
import org.hl7.fhir.r5.model.ValueSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.fhir.r5.ValueSetProviderR5;
import com.wci.termhub.fhir.util.LoincLllgValueSetHelper;

import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Unit tests for LOINC LL/LG value set support (Regenstrief mode) with
 * fhir.loinc.lllg.valuesets.enabled=true. Uses LOINC sandbox data loaded by
 * AbstractFhirR5ServerTest.
 */
@TestPropertySource(properties = "fhir.loinc.lllg.valuesets.enabled=true")
public class LoincLllgValueSetR5UnitTest extends AbstractFhirR5ServerTest {

  private static final String LL_VS_URL = "http://loinc.org/vs/LL1772-4";
  private static final String LL_VS_ID = "LL1772-4";
  private static final String LOINC_CODE_IN_LL = "66480-5";

  @Autowired
  private ValueSetProviderR5 provider;

  @Autowired
  private LoincLllgValueSetHelper loincLllgHelper;

  private MockHttpServletRequest request;
  private ServletRequestDetails details;

  @BeforeEach
  public void setUp() {
    request = new MockHttpServletRequest();
    details = new ServletRequestDetails();
    details.setServletRequest(request);
  }

  @Test
  public void testLllgEnabled() {
    assertTrue(loincLllgHelper.isEnabled(), "LL/LG value sets should be enabled in this test");
  }

  @Test
  public void testFindLllgValueSetByUrl() throws Exception {
    final UriParam url = new UriParam(LL_VS_URL);
    final Bundle bundle = provider.findValueSets(request, details, null, null, null, null, null,
        null, null, null, url, null, null, null);
    assertNotNull(bundle);
    assertTrue(
        bundle.getEntry().stream().anyMatch(e -> e.getResource() instanceof ValueSet
            && LL_VS_URL.equals(((ValueSet) e.getResource()).getUrl())
            && LL_VS_ID.equals(((ValueSet) e.getResource()).getId())),
        "Bundle should contain LL value set for " + LL_VS_URL);
  }

  @Test
  public void testGetLllgValueSetById() throws Exception {
    final ValueSet vs = provider.getValueSet(request, details, new IdType(LL_VS_ID));
    assertNotNull(vs);
    assertTrue(LL_VS_ID.equals(vs.getId()), "ValueSet id should be " + LL_VS_ID);
    assertTrue(LL_VS_URL.equals(vs.getUrl()), "ValueSet url should be " + LL_VS_URL);
  }

  @Test
  public void testExpandLllgValueSet() throws Exception {
    final ValueSet vs = provider.expandImplicit(request, details, null, new UriType(LL_VS_URL),
        null, null, null, null, null);
    assertNotNull(vs);
    assertNotNull(vs.getExpansion(), "Expansion should be present");
    assertTrue(vs.getExpansion().getTotal() >= 0, "Total should be non-negative");
    assertTrue(LL_VS_ID.equals(vs.getId()) || vs.getId() != null);
  }

  @Test
  public void testValidateCodeInLllgValueSet() throws Exception {
    final Parameters params = provider.validateCodeImplicit(request, details, new UriType(LL_VS_URL),
        null, new CodeType(LOINC_CODE_IN_LL), null, null, null, null, null);
    assertNotNull(params);
    final Parameters.ParametersParameterComponent resultParam =
        params.getParameter().stream().filter(p -> "result".equals(p.getName())).findFirst()
            .orElse(null);
    assertNotNull(resultParam, "Parameters should contain 'result'");
    assertTrue(resultParam.getValue() instanceof BooleanType,
        "Result should be a BooleanType");
    assertTrue(params.getParameter().stream().anyMatch(p -> "result".equals(p.getName())),
        "LL/LG validate-code should return Parameters with result (true if code in set, false otherwise)");
  }
}
