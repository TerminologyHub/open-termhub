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

import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.util.LoincConceptPropertyHelper;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptPropertyValueCoding;

/**
 * Unit tests for {@link LoincConceptPropertyHelper}.
 */
public class LoincConceptPropertyHelperUnitTest {

  /**
   * Test suppress status on lookup output when both attributes present.
   */
  @Test
  public void testSuppressStatusWhenBothPresentAndRegenstriefOn() {
    final Concept concept = new Concept();
    concept.getAttributes().put("status", "active");
    concept.getAttributes().put("STATUS", "Active");

    assertTrue(
        LoincConceptPropertyHelper.suppressStatusOnLookupOutput("status", concept, true, true));
    assertFalse(
        LoincConceptPropertyHelper.suppressStatusOnLookupOutput("STATUS", concept, true, true));
  }

  /**
   * Test do not suppress status when regenstrief off.
   */
  @Test
  public void testDoNotSuppressStatusWhenRegenstriefOff() {
    final Concept concept = new Concept();
    concept.getAttributes().put("status", "active");
    concept.getAttributes().put("STATUS", "Active");

    assertFalse(
        LoincConceptPropertyHelper.suppressStatusOnLookupOutput("status", concept, false, true));
  }

  /**
   * Test do not suppress status when only lowercase present.
   */
  @Test
  public void testDoNotSuppressStatusWhenOnlyLowercasePresent() {
    final Concept concept = new Concept();
    concept.getAttributes().put("status", "active");

    assertFalse(
        LoincConceptPropertyHelper.suppressStatusOnLookupOutput("status", concept, true, true));
  }

  /**
   * Test is status value code property.
   */
  @Test
  public void testIsStatusValueCodeProperty() {
    assertTrue(LoincConceptPropertyHelper.isStatusValueCodeProperty("status"));
    assertFalse(LoincConceptPropertyHelper.isStatusValueCodeProperty("STATUS"));
  }

  /**
   * Panel membership and hierarchy properties are suppressed on $lookup.
   */
  @Test
  public void testSuppressRelationshipPropertyOnLookupOutput() {
    assertTrue(LoincConceptPropertyHelper.suppressRelationshipPropertyOnLookupOutput("parent"));
    assertTrue(LoincConceptPropertyHelper.suppressRelationshipPropertyOnLookupOutput("child"));
    assertTrue(LoincConceptPropertyHelper.suppressRelationshipPropertyOnLookupOutput("member"));
    assertTrue(LoincConceptPropertyHelper.suppressRelationshipPropertyOnLookupOutput("has_member"));
    assertFalse(LoincConceptPropertyHelper.suppressRelationshipPropertyOnLookupOutput("component"));
    assertFalse(LoincConceptPropertyHelper.suppressRelationshipPropertyOnLookupOutput(null));
  }

  /**
   * LP part codes match LOINC part / answer class pattern.
   */
  @Test
  public void testIsLoincPartCode() {
    assertTrue(LoincConceptPropertyHelper.isLoincPartCode("LP343406-7"));
    assertFalse(LoincConceptPropertyHelper.isLoincPartCode("LA1-0"));
    assertFalse(LoincConceptPropertyHelper.isLoincPartCode(null));
  }

  /**
   * Internal display companion keys must not be emitted as properties.
   */
  @Test
  public void testIsLoincLookupInternalDisplayKey() {
    assertTrue(LoincConceptPropertyHelper.isLoincLookupInternalDisplayKey("component_display"));
    assertFalse(LoincConceptPropertyHelper.isLoincLookupInternalDisplayKey("component"));
    assertFalse(LoincConceptPropertyHelper.isLoincLookupInternalDisplayKey(null));
  }

  /**
   * Legacy uppercase string duplicate is superseded when lowercase LP code exists.
   */
  @Test
  public void testIsLoincLegacyStringSupersededByValueCoding() {
    final Concept concept = new Concept();
    concept.getAttributes().put("component", "LP343406-7");
    concept.getAttributes().put("component_display", "Body weight");

    assertTrue(LoincConceptPropertyHelper.isLoincLegacyStringSupersededByValueCoding("COMPONENT",
        "Body weight", concept));
    assertFalse(LoincConceptPropertyHelper.isLoincLegacyStringSupersededByValueCoding("COMPONENT",
        "LP343406-7", concept));
    assertFalse(LoincConceptPropertyHelper.isLoincLegacyStringSupersededByValueCoding("component",
        "Body weight", concept));
  }

  /**
   * Resolve display from _display attribute, literal, or display map.
   */
  @Test
  public void testResolveLoincPropertyDisplay() {
    final Concept concept = new Concept();
    concept.getAttributes().put("component_display", "From attribute");

    assertEquals("From attribute",
        LoincConceptPropertyHelper.resolveLoincPropertyDisplay("component", "LP343406-7",
            "LP343406-7", concept, Map.of()));
    assertEquals("Literal text",
        LoincConceptPropertyHelper.resolveLoincPropertyDisplay("component", "Literal text",
            null, new Concept(), Map.of()));
    assertEquals("Mapped display",
        LoincConceptPropertyHelper.resolveLoincPropertyDisplay("component", "LP343406-7",
            "LP343406-7", new Concept(), Map.of("LP343406-7", "Mapped display")));
  }

  /**
   * Legacy indexed keys strip numeric suffix for FHIR property name.
   */
  @Test
  public void testLoincLookupPropertyName() {
    assertEquals("component", LoincConceptPropertyHelper.loincLookupPropertyName("component_2"));
    assertEquals("component", LoincConceptPropertyHelper.loincLookupPropertyName("component"));
    assertEquals(null, LoincConceptPropertyHelper.loincLookupPropertyName(null));
  }

  /**
   * EXAMPLE then NORMATIVE for the same LL collapses to NORMATIVE for $lookup.
   */
  @Test
  public void testSelectFhirPropertyCodingsPrefersNormativeAnswerList() {
    final ConceptPropertyValueCoding example = new ConceptPropertyValueCoding();
    example.setPropertyCode("answer-list");
    example.setValueCode("LL6214-2");
    example.setAnswerListLinkType("EXAMPLE");

    final ConceptPropertyValueCoding normative = new ConceptPropertyValueCoding();
    normative.setPropertyCode("answer-list");
    normative.setValueCode("LL6214-2");
    normative.setAnswerListLinkType("NORMATIVE");
    normative.setApplicableContext("84428-2");

    final List<ConceptPropertyValueCoding> selected =
        LoincConceptPropertyHelper.selectFhirPropertyCodingsForLookup(List.of(example, normative));
    assertEquals(1, selected.size());
    assertEquals("NORMATIVE", selected.get(0).getAnswerListLinkType());
    assertEquals("LL6214-2", selected.get(0).getValueCode());
  }

  /**
   * Without link types, keep the last duplicate (EXAMPLE then NORMATIVE order in LOINC).
   */
  @Test
  public void testSelectFhirPropertyCodingsKeepsLastWhenLinkTypeMissing() {
    final ConceptPropertyValueCoding first = new ConceptPropertyValueCoding();
    first.setPropertyCode("answer-list");
    first.setValueCode("LL6214-2");
    first.setValueDisplay("first");

    final ConceptPropertyValueCoding second = new ConceptPropertyValueCoding();
    second.setPropertyCode("answer-list");
    second.setValueCode("LL6214-2");
    second.setValueDisplay("second");

    final List<ConceptPropertyValueCoding> selected =
        LoincConceptPropertyHelper.selectFhirPropertyCodingsForLookup(List.of(first, second));
    assertEquals(1, selected.size());
    assertEquals("second", selected.get(0).getValueDisplay());
  }

  /**
   * Different LLs are both kept.
   */
  @Test
  public void testSelectFhirPropertyCodingsKeepsDistinctAnswerLists() {
    final ConceptPropertyValueCoding a = new ConceptPropertyValueCoding();
    a.setPropertyCode("answer-list");
    a.setValueCode("LL1-1");
    a.setAnswerListLinkType("NORMATIVE");

    final ConceptPropertyValueCoding b = new ConceptPropertyValueCoding();
    b.setPropertyCode("answer-list");
    b.setValueCode("LL2-2");
    b.setAnswerListLinkType("NORMATIVE");

    final List<ConceptPropertyValueCoding> selected =
        LoincConceptPropertyHelper.selectFhirPropertyCodingsForLookup(List.of(a, b));
    assertEquals(2, selected.size());
  }
}
