/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.util.FieldedStringTokenizer;

/**
 * Unit tests for {@link FieldedStringTokenizer}.
 */
public class FieldedStringTokenizerUnitTest {

  /** The logger. */
  private static final Logger LOGGER =
      LoggerFactory.getLogger(FieldedStringTokenizerUnitTest.class);

  /**
   * Test basic split functionality.
   */
  @Test
  public void testBasicSplit() {
    final String input = "a|b|c";
    final String[] expected = {
        "a", "b", "c"
    };
    final String[] result = FieldedStringTokenizer.split(input, "|");
    assertArrayEquals(expected, result, "Basic split should work with single character delimiter");
  }

  /**
   * Test split with empty fields.
   */
  @Test
  public void testSplitWithEmptyFields() {
    final String input = "a||c";
    final String[] expected = {
        "a", "", "c"
    };
    final String[] result = FieldedStringTokenizer.split(input, "|");
    assertArrayEquals(expected, result, "Split should handle empty fields");
  }

  /**
   * Test split with multiple delimiters.
   */
  @Test
  public void testSplitWithMultipleDelimiters() {
    final String input = "a,b;c|d";
    final String[] expected = {
        "a", "b", "c", "d"
    };
    final String[] result = FieldedStringTokenizer.split(input, ",;|");
    assertArrayEquals(expected, result, "Split should work with multiple delimiters");
  }

  /**
   * Test split with empty input.
   */
  @Test
  public void testSplitWithEmptyInput() {
    String[] result = FieldedStringTokenizer.split("", "|");
    assertArrayEquals(new String[0], result, "Empty input should return empty array");

    result = FieldedStringTokenizer.split(null, "|");
    assertArrayEquals(new String[0], result, "Null input should return empty array");
  }

  /**
   * Test split as set.
   */
  @Test
  public void testSplitAsSet() {
    final String input = "a|b|a|c";
    final Set<String> expected = new HashSet<>(Arrays.asList("a", "b", "c"));
    final Set<String> result = FieldedStringTokenizer.splitAsSet(input, "|");
    assertEquals(expected, result, "SplitAsSet should remove duplicates");
  }

  /**
   * Test split with known field count.
   */
  @Test
  public void testSplitWithFieldCount() {
    final String input = "a|b|c|d|e";
    final String[] expected = {
        "a", "b", "c"
    };
    final String[] result = FieldedStringTokenizer.split(input, "|", 3);
    assertArrayEquals(expected, result, "Split should respect field count limit");
  }

  /**
   * Test split with array parameter.
   */
  @Test
  public void testSplitWithArrayParameter() {
    final String input = "a|b|c";
    final String[] tokens = new String[3];
    FieldedStringTokenizer.split(input, "|", 3, tokens);
    assertArrayEquals(new String[] {
        "a", "b", "c"
    }, tokens, "Split should populate provided array");
  }

  /**
   * Test tokenizer enumeration.
   */
  @Test
  public void testTokenizerEnumeration() {
    final FieldedStringTokenizer tokenizer = new FieldedStringTokenizer("a|b|c", "|");
    assertTrue(tokenizer.hasMoreElements(), "Should have more elements initially");
    assertEquals("a", tokenizer.nextElement(), "First element should be 'a'");
    assertTrue(tokenizer.hasMoreTokens(), "Should have more tokens after first");
    assertEquals("b", tokenizer.nextToken(), "Second token should be 'b'");
    assertTrue(tokenizer.hasMoreElements(), "Should have more elements for last token");
    assertEquals("c", tokenizer.nextElement(), "Last element should be 'c'");
    assertFalse(tokenizer.hasMoreTokens(), "Should have no more tokens");
  }

  /**
   * Test join operations.
   */
  @Test
  public void testJoinOperations() {
    // Test joining List
    final List<String> list = Arrays.asList("a", "b", "c");
    final String listResult = FieldedStringTokenizer.join(list, "|");
    assertEquals("a|b|c", listResult, "Join should work with List");

    // Test joining Set
    final Set<String> set = new HashSet<>(Arrays.asList("a", "b", "c"));
    final String setResult = FieldedStringTokenizer.join(set, "|");
    assertTrue(setResult.split("\\|").length == 3, "Join should work with Set");
    assertTrue(setResult.contains("a") && setResult.contains("b") && setResult.contains("c"),
        "Join result should contain all elements");

    // Test joining array
    final String[] array = {
        "1", "2", "3"
    };
    final String arrayResult = FieldedStringTokenizer.join(array, "|");
    assertEquals("1|2|3", arrayResult, "Join should work with array");
  }

  /**
   * Test count tokens.
   */
  @Test
  public void testCountTokens() {
    FieldedStringTokenizer tokenizer = new FieldedStringTokenizer("a|b|c", "|");
    assertEquals(3, tokenizer.countTokens(), "Should count correct number of tokens");

    tokenizer = new FieldedStringTokenizer("", "|");
    assertEquals(0, tokenizer.countTokens(), "Empty string should have zero tokens");
  }
}
