/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.model.Concept;

/**
 * Unit tests for LOINC status / STATUS handling in {@link FhirUtilityR4}
 * $lookup output.
 */
public class FhirUtilityR4LoincLookupUnitTest {

  /**
   * Loinc concept with status.
   *
   * @return the concept
   */
  private static Concept loincConceptWithStatus() {
    final Concept concept = new Concept();
    concept.setCode("95209-3");
    concept.setName("Test");
    concept.setActive(true);
    concept.getAttributes().put("status", "active");
    concept.getAttributes().put("STATUS", "Active");
    return concept;
  }

  /**
   * Loinc code system.
   *
   * @return the code system
   */
  private static CodeSystem loincCodeSystem() {
    final CodeSystem cs = new CodeSystem();
    cs.setUrl("http://loinc.org");
    cs.setTitle("LOINC");
    cs.setVersion("2.77");
    return cs;
  }

  /**
   * Property codes.
   *
   * @param parameters the parameters
   * @return the sets the
   */
  private static Set<String> propertyCodes(final Parameters parameters) {
    return parameters.getParameter().stream().filter(p -> "property".equals(p.getName()))
        .map(FhirUtilityR4LoincLookupUnitTest::propertyCodeFromParameter)
        .collect(Collectors.toSet());
  }

  /**
   * Property code from parameter.
   *
   * @param propertyParam the property param
   * @return the string
   */
  private static String propertyCodeFromParameter(
    final ParametersParameterComponent propertyParam) {
    return propertyParam.getPart().stream()
        .filter(part -> "code".equals(part.getName()) && part.getValue() instanceof CodeType)
        .map(part -> ((CodeType) part.getValue()).getValue()).findFirst().orElse(null);
  }

  /**
   * Regenstrief mode: emit STATUS only, not lowercase status.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLookupSuppressesStatusWhenRegenstriefOn() throws Exception {
    final Parameters parameters = FhirUtilityR4.toR4(loincCodeSystem(), loincConceptWithStatus(),
        null, Map.of(), List.of(), List.of(), null, null, true);

    final Set<String> codes = propertyCodes(parameters);
    assertFalse(codes.contains("status"));
    assertTrue(codes.contains("STATUS"));
  }

  /**
   * Regenstrief off: both status and STATUS appear.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLookupEmitsBothStatusWhenRegenstriefOff() throws Exception {
    final Parameters parameters = FhirUtilityR4.toR4(loincCodeSystem(), loincConceptWithStatus(),
        null, Map.of(), List.of(), List.of(), null, null, false);

    final Set<String> codes = propertyCodes(parameters);
    assertTrue(codes.contains("status"));
    assertTrue(codes.contains("STATUS"));
  }

  /**
   * LA1-0 style concept after load: Regenstrief mode must emit STATUS and
   * semanticType, not lowercase status.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLookupLa1StyleRegenstriefOn() throws Exception {
    final Concept concept = la1StyleConceptAfterLoad();

    final Parameters parameters = FhirUtilityR4.toR4(loincCodeSystem(), concept, null, Map.of(),
        List.of(), List.of(), null, null, true);

    final Set<String> codes = propertyCodes(parameters);
    assertFalse(codes.contains("status"),
        "Regenstrief mode must not emit lowercase status when STATUS is present");
    assertTrue(codes.contains("STATUS"));
    assertTrue(codes.contains("semanticType"),
        "semanticType must appear in $lookup when present on the concept");
  }

  /**
   * Legacy index shape: semanticType only on semanticTypes, not attributes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLookupEmitsSemanticTypeFromSemanticTypesOnly() throws Exception {
    final Concept concept = new Concept();
    concept.setCode("LA1-0");
    concept.setName("UTD");
    concept.setActive(true);
    concept.getSemanticTypes().add("Group");

    final Parameters parameters = FhirUtilityR4.toR4(loincCodeSystem(), concept, null, Map.of(),
        List.of(), List.of(), null, null, true);

    assertTrue(propertyCodes(parameters).contains("semanticType"));
  }

  /**
   * Regenstrief on with only lowercase status (STATUS missing from store) still
   * emits status — documents loader/index gap until STATUS is loaded.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLookupRegenstriefOnStatusOnlyStillEmitsStatus() throws Exception {
    final Concept concept = new Concept();
    concept.setCode("LA1-0");
    concept.setName("UTD");
    concept.setActive(true);
    concept.getAttributes().put("status", "active");

    final Parameters parameters = FhirUtilityR4.toR4(loincCodeSystem(), concept, null, Map.of(),
        List.of(), List.of(), null, null, true);

    assertTrue(propertyCodes(parameters).contains("status"));
    assertFalse(propertyCodes(parameters).contains("STATUS"));
  }

  /**
   * Concept mirroring CodeSystem input properties for LA1-0 after load.
   *
   * @return the concept
   */
  private static Concept la1StyleConceptAfterLoad() {
    final Concept concept = new Concept();
    concept.setCode("LA1-0");
    concept.setName("UTD");
    concept.setActive(true);
    concept.getAttributes().put("status", "active");
    concept.getAttributes().put("STATUS", "Active");
    concept.getAttributes().put("SequenceNumber", "3");
    concept.getSemanticTypes().add("Group");
    concept.getAttributes().put("semanticType", "Group");
    return concept;
  }

  /**
   * Lowercase status is emitted as valueCode.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLookupStatusUsesValueCode() throws Exception {
    final Concept concept = loincConceptWithStatus();
    concept.getAttributes().remove("STATUS");

    final Parameters parameters = FhirUtilityR4.toR4(loincCodeSystem(), concept, null, Map.of(),
        List.of(), List.of(), null, null, false);

    final ParametersParameterComponent statusProp =
        parameters.getParameter().stream().filter(p -> "property".equals(p.getName()))
            .filter(p -> "status".equals(propertyCodeFromParameter(p))).findFirst().orElseThrow();

    final boolean hasValueCode = statusProp.getPart().stream()
        .anyMatch(part -> "value".equals(part.getName()) && part.getValue() instanceof CodeType);
    assertTrue(hasValueCode);
    assertEquals("active",
        statusProp.getPart().stream()
            .filter(part -> "value".equals(part.getName()) && part.getValue() instanceof CodeType)
            .map(part -> ((CodeType) part.getValue()).getValue()).findFirst().orElse(null));
  }
}
