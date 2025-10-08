/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.handler.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.wci.termhub.handler.BrowserQueryBuilder;

/**
 * Tests for BrowserQueryBuilder composition and escaping.
 */
public class BrowserQueryBuilderUnitTest {

  /** The query builder. */
  private BrowserQueryBuilder queryBuilder;

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
    queryBuilder = new BrowserQueryBuilder();
  }

  @Test
  public void testBuildQueryIncludesStemAndNorm() {
    final String q = queryBuilder.buildQuery("heart-attack (acute), stage-3");
    // Should contain a phrase boosted stemName clause and AND-composed words
    assertTrue(q.contains("terms.stemName:\""));
    assertTrue(q.contains("normName:("));
    // Ensure wildcard expansion occurs in normName clause
    assertTrue(q.contains("*"));
  }

  @Test
  public void testIsCodeDetection() {
    for (final String s : Arrays.asList("43189234", "C48123", "XE8CT", "PA007", "I51.2", "A01.5XXA",
        "10234-1", "HGNC:12345")) {
      assertTrue(queryBuilder.isCode(s), "isCode should return true for " + s);
    }
  }
}
