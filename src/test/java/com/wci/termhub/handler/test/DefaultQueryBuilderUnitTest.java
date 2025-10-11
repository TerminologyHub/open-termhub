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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.wci.termhub.handler.DefaultQueryBuilder;
import com.wci.termhub.model.SearchParameters;

/**
 * Tests for DefaultQueryBuilder escaping and composition.
 */
public class DefaultQueryBuilderUnitTest {

  /** The query builder. */
  private DefaultQueryBuilder queryBuilder;

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
    queryBuilder = new DefaultQueryBuilder();
  }

  @Test
  public void testBuildQueryWildcardAndFieldedPassThrough() {
    // Wildcards
    assertEquals("*:*", queryBuilder.buildQuery("*"));
    assertEquals("*:*", queryBuilder.buildEscapedQuery("*"));
    // Fielded stays fielded
    assertEquals("name:foo", queryBuilder.buildQuery("name:foo"));
    // Escaped handler maps name field to normName
    assertEquals("normName:foo", queryBuilder.buildEscapedQuery("name:foo"));
  }

  @Test
  public void testBuildEscapedQueryEscapesSpecials() {
    // Should escape hyphen, parens, commas, plus colon; dot may be left per StringUtility
    final String in = "a-b.c(d),e:f";
    final String escaped = queryBuilder.buildEscapedQuery(in);
    // Non-fielded queries are composed as (term) OR (normName:term)
    final String expectedTerm = "a\\-b.c\\(d\\),e\\:f";
    final String expected = "(" + expectedTerm + ") OR (normName:" + expectedTerm + ")";
    assertEquals(expected, escaped);
  }

  @Test
  public void testBuildQueryAddsFlags() {
    final SearchParameters p = new SearchParameters();
    p.setQuery("name:heart");
    p.setActive(true);
    p.setLeaf(false);
    final String q = queryBuilder.buildQuery(p);
    assertEquals("name:heart AND active:true AND leaf:false", q);
  }

  /**
   * Test name field query transformation.
   */
  @Test
  public void testNameFieldQueryTransformation() {
    // Name field queries are left as-is for buildQuery
    final String input = "name:canagliflozin";
    final String expected = "name:canagliflozin";
    final String actual = queryBuilder.buildQuery(input);
    assertEquals(expected, actual,
        "Name field queries should pass through in buildQuery");
  }

  /**
   * Test name field query with spaces.
   */
  @Test
  public void testNameFieldQueryWithSpaces() {
    // Test name field queries with spaces pass through in buildQuery
    final String input = "name:dummyname with space";
    final String expected = "name:dummyname with space";
    final String actual = queryBuilder.buildQuery(input);
    assertEquals(expected, actual, "Name field queries with spaces should pass through");
  }

  /**
   * Test name field query with special characters.
   */
  @Test
  public void testNameFieldQueryWithSpecialCharacters() {
    // Test name field queries with special characters pass through in buildQuery
    final String input = "name:diabetes-mellitus";
    final String expected = "name:diabetes-mellitus";
    final String actual = queryBuilder.buildQuery(input);
    assertEquals(expected, actual,
        "Name field queries with special characters should pass through");
  }

  /**
   * Test non fielded query transformation.
   */
  @Test
  public void testNonFieldedQueryTransformation() {
    // Non-fielded buildQuery should just escape the query (no normName clause)
    final String input = "cancer";
    final String expected = "cancer";
    final String actual = queryBuilder.buildQuery(input);
    assertEquals(expected, actual,
        "Non-fielded buildQuery should not add normName clause");
  }

  /**
   * Test non fielded query with spaces.
   */
  @Test
  public void testNonFieldedQueryWithSpaces() {
    // Non-fielded buildQuery with spaces will escape spaces
    final String input = "diabetes mellitus";
    final String expected = "diabetes\\ mellitus";
    final String actual = queryBuilder.buildQuery(input);
    assertEquals(expected, actual,
        "Non-fielded buildQuery with spaces should escape spaces");
  }

  /**
   * Test other fielded queries pass through.
   */
  @Test
  public void testOtherFieldedQueriesPassThrough() {
    // Test that other fielded queries pass through unchanged
    final String input = "terminology:SNOMEDCT";
    final String actual = queryBuilder.buildQuery(input);
    assertEquals(input, actual, "Other fielded queries should pass through unchanged");
  }

  /**
   * Test code fielded queries pass through.
   */
  @Test
  public void testCodeFieldedQueriesPassThrough() {
    // Test that code fielded queries pass through unchanged
    final String input = "code:12345";
    final String actual = queryBuilder.buildQuery(input);
    assertEquals(input, actual, "Code fielded queries should pass through unchanged");
  }

  /**
   * Test empty query.
   */
  @Test
  public void testEmptyQuery() {
    // Test empty query handling
    final String input = "";
    final String expected = "*:*";
    final String actual = queryBuilder.buildQuery(input);
    assertEquals(expected, actual, "Empty queries should return *:*");
  }

  /**
   * Test wildcard query.
   */
  @Test
  public void testWildcardQuery() {
    // Test wildcard query handling
    final String input = "*";
    final String expected = "*:*";
    final String actual = queryBuilder.buildQuery(input);
    assertEquals(expected, actual, "Wildcard queries should return *:*");
  }

  /**
   * Test build escaped query name field.
   */
  @Test
  public void testBuildEscapedQueryNameField() {
    // Test buildEscapedQuery for name field queries
    final String input = "name:canagliflozin";
    final String expected = "normName:canagliflozin";
    final String actual = queryBuilder.buildEscapedQuery(input);
    assertEquals(expected, actual,
        "buildEscapedQuery should handle name field queries the same as buildQuery");
  }

  /**
   * Test build escaped query non fielded.
   */
  @Test
  public void testBuildEscapedQueryNonFielded() {
    // Test buildEscapedQuery for non-fielded queries
    final String input = "cancer";
    final String expected = "(cancer) OR (normName:cancer)";
    final String actual = queryBuilder.buildEscapedQuery(input);
    assertEquals(expected, actual,
        "buildEscapedQuery should compose non-fielded queries with normName");
  }

  /**
   * Test build escaped query other fielded.
   */
  @Test
  public void testBuildEscapedQueryOtherFielded() {
    // For other fielded queries, buildEscapedQuery composes with normName as well
    final String input = "terminology:SNOMEDCT";
    final String expected = "(terminology\\:SNOMEDCT) OR (normName:terminology\\:SNOMEDCT)";
    final String actual = queryBuilder.buildEscapedQuery(input);
    assertEquals(expected, actual,
        "buildEscapedQuery should escape fielded queries and compose with normName");
  }

  /**
   * Test query builder interface.
   */
  @Test
  public void testQueryBuilderInterface() {
    // Test that DefaultQueryBuilder implements QueryBuilder interface correctly
    assertNotNull(queryBuilder, "DefaultQueryBuilder should be instantiable");

    // Test accepts method
    assertEquals(true, queryBuilder.accepts(null),
        "DefaultQueryBuilder should accept null handler");
    assertEquals(true, queryBuilder.accepts("default"),
        "DefaultQueryBuilder should accept 'default' handler");
    assertEquals(false, queryBuilder.accepts("browser"),
        "DefaultQueryBuilder should not accept 'browser' handler");

    // DefaultQueryBuilder uses @Order(1) annotation for Spring ordering
  }

  /**
   * Test search parameters build query.
   */
  @Test
  public void testSearchParametersBuildQuery() {
    // Test buildQuery with SearchParameters
    final SearchParameters params = new SearchParameters();
    params.setQuery("name:diabetes");

    final String expected = "name:diabetes";
    final String actual = queryBuilder.buildQuery(params);
    assertEquals(expected, actual,
        "buildQuery with SearchParameters should match String version");
  }
}
