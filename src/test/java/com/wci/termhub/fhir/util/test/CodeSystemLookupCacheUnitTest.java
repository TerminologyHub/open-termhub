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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.util.CodeSystemLookupCache;

/**
 * Unit tests for {@link CodeSystemLookupCache}.
 */
public class CodeSystemLookupCacheUnitTest {

  /**
   * Clear cache after each test.
   */
  @AfterEach
  public void tearDown() {
    CodeSystemLookupCache.clear();
  }

  /**
   * Regenstrief mode is part of the cache key.
   */
  @Test
  public void testBuildKeyIncludesRegenstriefMode() {
    final String off = CodeSystemLookupCache.buildKey("R4", "http://loinc.org", "2.69", "10-9",
        null, false);
    final String on = CodeSystemLookupCache.buildKey("R4", "http://loinc.org", "2.69", "10-9", null,
        true);
    assertNotEquals(off, on);
    assertTrue(off.endsWith("|false"));
    assertTrue(on.endsWith("|true"));
  }

  /**
   * Property set is sorted and joined; null means *.
   */
  @Test
  public void testBuildKeyPropertySet() {
    final String all = CodeSystemLookupCache.buildKey("R4", "http://loinc.org", "2.69", "10-9",
        null, false);
    assertTrue(all.contains("|*|"));

    final String filtered = CodeSystemLookupCache.buildKey("R4", "http://loinc.org", "2.69", "10-9",
        Set.of("child", "parent"), false);
    assertTrue(filtered.contains("|child,parent|"));
  }

  /**
   * Put and get round-trip for R4 Parameters.
   */
  @Test
  public void testPutGetR4() {
    final String key = CodeSystemLookupCache.buildKey("R4", "http://loinc.org", "2.69", "10-9",
        null, false);
    assertFalse(CodeSystemLookupCache.containsKey(key));

    final Parameters parameters = new Parameters();
    parameters.addParameter("code", new StringType("10-9"));
    parameters.addParameter("display", new StringType("Test"));
    CodeSystemLookupCache.putR4(key, parameters);

    assertTrue(CodeSystemLookupCache.containsKey(key));
    final Parameters cached = CodeSystemLookupCache.getR4(key);
    assertNotNull(cached);
    assertEquals("10-9", cached.getParameter("code").getValue().toString());
  }

  /**
   * Different FHIR versions do not share entries.
   */
  @Test
  public void testR4AndR5KeysDiffer() {
    final String r4 = CodeSystemLookupCache.buildKey("R4", "http://loinc.org", "2.69", "10-9", null,
        false);
    final String r5 = CodeSystemLookupCache.buildKey("R5", "http://loinc.org", "2.69", "10-9", null,
        false);
    assertNotEquals(r4, r5);
  }
}
