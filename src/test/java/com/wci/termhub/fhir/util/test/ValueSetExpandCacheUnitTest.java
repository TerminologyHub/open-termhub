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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.util.ValueSetExpandCache;

import ca.uhn.fhir.context.FhirVersionEnum;

/**
 * Unit tests for {@link ValueSetExpandCache}.
 */
public class ValueSetExpandCacheUnitTest {

  /**
   * Clear cache after each test.
   */
  @AfterEach
  public void tearDown() {
    ValueSetExpandCache.clear();
  }

  /**
   * Put and get round-trip for R4 expand responses.
   */
  @Test
  public void testPutGetR4() {
    final String key = ValueSetExpandCache.buildKey(FhirVersionEnum.R4, "vs-1", null, "2.81", 0,
        1000, null, false, null);
    assertFalse(ValueSetExpandCache.containsKey(key));

    final ValueSet valueSet = new ValueSet();
    valueSet.setId("vs-1");
    valueSet.setUrl("http://example.org/ValueSet/vs-1");
    ValueSetExpandCache.putR4(key, valueSet);

    assertTrue(ValueSetExpandCache.containsKey(key));
    final ValueSet cached = ValueSetExpandCache.getR4(key);
    assertNotNull(cached);
    assertEquals("vs-1", cached.getIdElement().getIdPart());
  }

  /**
   * Offset and count are part of the key.
   */
  @Test
  public void testBuildKeyIncludesPaging() {
    final String page0 = ValueSetExpandCache.buildKey(FhirVersionEnum.R4, "vs-1", null, "2.81", 0,
        1000, null, false, null);
    final String page1 = ValueSetExpandCache.buildKey(FhirVersionEnum.R4, "vs-1", null, "2.81",
        1000, 1000, null, false, null);
    assertFalse(page0.equals(page1));
  }
}
