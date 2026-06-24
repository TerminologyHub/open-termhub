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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.util.LoincConceptPropertyHelper;
import com.wci.termhub.model.Concept;

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
}
