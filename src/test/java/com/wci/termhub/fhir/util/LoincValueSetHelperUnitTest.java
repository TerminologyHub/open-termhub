/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.util.LoincValueSetHelper.LllgComposeStructure;
import com.wci.termhub.model.Concept;

/**
 * Unit tests for {@link LoincValueSetHelper} compose partitioning (no index
 * required).
 */
public class LoincValueSetHelperUnitTest {

  /** The helper. */
  private LoincValueSetHelper helper;

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
    helper = new LoincValueSetHelper();
  }

  /**
   * Test is nested lllg value set code.
   */
  @Test
  public void testIsNestedLllgValueSetCode() {
    assertTrue(helper.isNestedLllgValueSetCode("LG33051-0"));
    assertTrue(helper.isNestedLllgValueSetCode("LL1162-8"));
    assertFalse(helper.isNestedLllgValueSetCode("104063-3"));
    assertFalse(helper.isNestedLllgValueSetCode(null));
  }

  /**
   * Test to child value set url.
   */
  @Test
  public void testToChildValueSetUrl() {
    assertEquals("http://loinc.org/vs/LG33052-8", helper.toChildValueSetUrl("LG33052-8"));
  }

  /**
   * Test build lllg compose structure nested only.
   */
  @Test
  public void testBuildLllgComposeStructureNestedOnly() {
    final Concept lg = new Concept();
    lg.setCode("LG33051-0");
    lg.setName("Oxygen saturation|MFr|Pt|Chal:None");
    final LllgComposeStructure structure = helper.buildLllgComposeStructure(List.of(lg));
    assertEquals(1, structure.getNestedValueSetUrls().size());
    assertEquals("http://loinc.org/vs/LG33051-0", structure.getNestedValueSetUrls().get(0));
    assertTrue(structure.getLeafConcepts().isEmpty());
  }

  /**
   * Test build lllg compose structure mixed.
   */
  @Test
  public void testBuildLllgComposeStructureMixed() {
    final Concept lg = new Concept();
    lg.setCode("LG33052-8");
    lg.setName("Body temperature");
    final Concept leaf = new Concept();
    leaf.setCode("8867-4");
    leaf.setName("Heart rate");
    final LllgComposeStructure structure = helper.buildLllgComposeStructure(List.of(lg, leaf));
    assertEquals(1, structure.getNestedValueSetUrls().size());
    assertEquals(1, structure.getLeafConcepts().size());
    assertEquals("8867-4", structure.getLeafConcepts().get(0).getCode());
  }

  /**
   * Test build lllg compose structure leaf only.
   */
  @Test
  public void testBuildLllgComposeStructureLeafOnly() {
    final Concept leaf = new Concept();
    leaf.setCode("104063-3");
    leaf.setName("Body temperature - Groin");
    final LllgComposeStructure structure = helper.buildLllgComposeStructure(List.of(leaf));
    assertTrue(structure.getNestedValueSetUrls().isEmpty());
    assertEquals(1, structure.getLeafConcepts().size());
  }
}
