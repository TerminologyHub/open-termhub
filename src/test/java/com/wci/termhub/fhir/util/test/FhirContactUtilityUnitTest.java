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

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.wci.termhub.fhir.util.FhirContactUtility;

/**
 * Unit tests for {@link FhirContactUtility}.
 */
public class FhirContactUtilityUnitTest {

  /** LOINC-style contact JSON. */
  private static final String LOINC_CONTACT =
      "[{\"telecom\":[{\"system\":\"url\",\"value\":\"http://loinc.org\"}]}]";

  /**
   * Test parse and equivalence.
   */
  @Test
  public void testParseAndEquivalence() {
    final List<JsonNode> parsed = FhirContactUtility.parseArray(LOINC_CONTACT);
    assertEquals(1, parsed.size());
    assertTrue(FhirContactUtility.isEquivalent(parsed.get(0), parsed.get(0)));
    assertTrue(FhirContactUtility.containsEquivalent(parsed, parsed.get(0)));
  }

  /**
   * Test distinct contacts are not equivalent.
   */
  @Test
  public void testDistinctContacts() {
    final List<JsonNode> parsed = FhirContactUtility.parseArray(LOINC_CONTACT);
    final JsonNode other =
        FhirContactUtility.toContactNode(null, "url", "http://example.org");
    assertFalse(FhirContactUtility.isEquivalent(parsed.get(0), other));
    assertFalse(FhirContactUtility.containsEquivalent(parsed, other));
  }

  /**
   * Test invalid input returns empty list.
   */
  @Test
  public void testInvalidInput() {
    assertTrue(FhirContactUtility.parseArray(null).isEmpty());
    assertTrue(FhirContactUtility.parseArray("").isEmpty());
    assertTrue(FhirContactUtility.parseArray("not-json").isEmpty());
  }
}
