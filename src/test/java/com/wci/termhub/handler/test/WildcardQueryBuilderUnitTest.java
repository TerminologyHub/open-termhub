/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.handler.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.wci.termhub.handler.WildcardQueryBuilder;
import com.wci.termhub.model.SearchParameters;

/**
 * Tests for WildcardQueryBuilder behavior for fielded/non-fielded and escaping.
 */
public class WildcardQueryBuilderUnitTest {

  /** The builder. */
  private WildcardQueryBuilder builder;

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
    builder = new WildcardQueryBuilder();
  }

  /**
   * Test build query empty and wildcard.
   */
  @Test
  public void testBuildQueryEmptyAndWildcard() {
    assertEquals("*", builder.buildQuery(""));
    assertEquals("*", builder.buildQuery("*"));

    final SearchParameters p = new SearchParameters();
    p.setQuery("");
    // buildQuery(SearchParameters) appends active preference
    final String q = builder.buildQuery(p);
    assertTrue(q.startsWith("*"));
    assertTrue(q.contains("(active:true^25 OR active:false)"));
  }

  /**
   * Test build query fielded pass through.
   */
  @Test
  public void testBuildQueryFieldedPassThrough() {
    assertEquals("name:foo", builder.buildQuery("name:foo"));
    assertEquals("code:123-4", builder.buildQuery("code:123-4"));

    final SearchParameters p = new SearchParameters();
    p.setQuery("terminology:SNOMEDCT");
    p.setActive(true);
    p.setLeaf(false);
    final String q = builder.buildQuery(p);
    assertEquals("terminology:SNOMEDCT AND active:true AND leaf:false", q);
  }

  /**
   * Test build query non fielded adds wildcards and escapes.
   */
  @Test
  public void testBuildQueryNonFieldedAddsWildcardsAndEscapes() {
    // Words become escaped + * and AND-composed
    assertEquals("heart* AND attack*", builder.buildQuery("heart attack"));
    // Truly non-fielded (no colon): hyphen and parens escaped; dot/comma left; wildcard appended
    assertEquals("a\\-b.c\\(d\\),ef*", builder.buildQuery("a-b.c(d),ef"));
  }

  /**
   * Test build escaped query.
   */
  @Test
  public void testBuildEscapedQuery() {
    // Escaped variant uses * for empty, escapes otherwise
    assertEquals("*", builder.buildEscapedQuery(""));
    assertEquals("*", builder.buildEscapedQuery("*"));
    assertEquals("name\\:foo", builder.buildEscapedQuery("name:foo"));
    assertEquals("a\\-b.c\\(d\\),e\\:f", builder.buildEscapedQuery("a-b.c(d),e:f"));

    final SearchParameters p = new SearchParameters();
    p.setQuery("code:123-4");
    p.setActive(null);
    p.setLeaf(true);
    final String q = builder.buildEscapedQuery(p);
    assertTrue(q.startsWith("code\\:123\\-4"));
    assertTrue(q.contains("(active:true^25 OR active:false)"));
    assertTrue(q.endsWith(" AND leaf:true"));
  }
}
