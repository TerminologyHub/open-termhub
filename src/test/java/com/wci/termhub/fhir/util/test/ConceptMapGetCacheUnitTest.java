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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hl7.fhir.r4.model.ConceptMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.util.ConceptMapGetCache;

import ca.uhn.fhir.context.FhirVersionEnum;

/**
 * Unit tests for {@link ConceptMapGetCache}.
 */
public class ConceptMapGetCacheUnitTest {

  /**
   * Clear cache after each test.
   */
  @AfterEach
  public void tearDown() {
    ConceptMapGetCache.clear();
  }

  /**
   * Put and get round-trip for R4 ConceptMap GET responses.
   */
  @Test
  public void testPutGetR4() {
    final String key = ConceptMapGetCache.buildKey(FhirVersionEnum.R4, "cm-1");
    assertFalse(ConceptMapGetCache.containsKey(key));

    final ConceptMap conceptMap = new ConceptMap();
    conceptMap.setId("cm-1");
    conceptMap.setUrl("http://example.org/ConceptMap/cm-1");
    ConceptMapGetCache.putR4(key, conceptMap);

    assertTrue(ConceptMapGetCache.containsKey(key));
    final ConceptMap cached = ConceptMapGetCache.getR4(key);
    assertNotNull(cached);
    assertEquals("cm-1", cached.getIdElement().getIdPart());
  }

  /**
   * Clear removes cached entries.
   */
  @Test
  public void testClear() {
    final String key = ConceptMapGetCache.buildKey(FhirVersionEnum.R4, "cm-2");
    final ConceptMap conceptMap = new ConceptMap();
    conceptMap.setId("cm-2");
    ConceptMapGetCache.putR4(key, conceptMap);
    assertTrue(ConceptMapGetCache.containsKey(key));

    ConceptMapGetCache.clear();
    assertFalse(ConceptMapGetCache.containsKey(key));
    assertNull(ConceptMapGetCache.getR4(key));
  }

  /**
   * R4 and R5 keys are distinct for the same id.
   */
  @Test
  public void testBuildKeyIncludesFhirVersion() {
    final String r4 = ConceptMapGetCache.buildKey(FhirVersionEnum.R4, "cm-1");
    final String r5 = ConceptMapGetCache.buildKey(FhirVersionEnum.R5, "cm-1");
    assertFalse(r4.equals(r5));
  }
}
