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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.util.StringUtility;

/**
 * Unit tests for {@link StringUtility}.
 */
public class StringUtilityUnitTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(StringUtilityUnitTest.class);

  /**
   * Test roman numeral conversion.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRomanNumeralConversion() throws Exception {
    // Test valid roman numerals
    assertEquals(1949, StringUtility.toArabic("MCMXLIX"), "1949 conversion failed");
    assertEquals(2023, StringUtility.toArabic("MMXXIII"), "2023 conversion failed");
    assertEquals(4, StringUtility.toArabic("IV"), "4 conversion failed");
    assertEquals(9, StringUtility.toArabic("IX"), "9 conversion failed");

    // Test roman numeral validation
    assertTrue(StringUtility.isRomanNumeral("MCMXLIX"), "Valid roman numeral not recognized");
    assertTrue(StringUtility.isRomanNumeral("IV"), "Valid roman numeral not recognized");
    assertFalse(StringUtility.isRomanNumeral("IIII"), "Invalid roman numeral recognized");
    assertFalse(StringUtility.isRomanNumeral("ABC"), "Invalid roman numeral recognized");

    // Test invalid input
    assertThrows(Exception.class, () -> StringUtility.toArabic("ABC"),
        "Should throw exception for invalid roman numeral");
  }

  /**
   * Test string normalization.
   */
  @Test
  public void testNormalization() {
    assertEquals("hiv infection", StringUtility.normalize("HIV Infection"),
        "Basic normalization failed");
    assertEquals("1 2 hydroxy", StringUtility.normalize("1,2-hydroxy"),
        "Normalization with numbers failed");
    assertEquals("test", StringUtility.normalize("  TEST  "), "Whitespace normalization failed");
    assertEquals("a b c", StringUtility.normalize("a/b\\c"), "Symbol normalization failed");
  }

  /**
   * Test trimming functions.
   */
  @Test
  public void testTrimming() {
    assertEquals("test", StringUtility.trimNonAlpaNumeric("!!!test!!!"),
        "Trim non-alphanumeric failed");
    assertEquals("test", StringUtility.trimNonAlpaNumeric("...test..."),
        "Trim non-alphanumeric failed");
    assertEquals("test123", StringUtility.trimNonAlpaNumeric("@#test123!!"),
        "Trim non-alphanumeric with numbers failed");
  }

  /**
   * Test case formatting.
   */
  @Test
  public void testCaseFormatting() {
    assertEquals("Test", StringUtility.capitalize("test"), "Simple capitalization failed");
    assertEquals("Test string", StringUtility.capitalize("test string"),
        "Capitalization with space failed");

    assertEquals("Test String", StringUtility.capitalizeEachWord("test string"),
        "Word capitalization failed");
    assertEquals("Test STRING Test", StringUtility.capitalizeEachWord("test STRING test"),
        "Mixed case word capitalization failed");

    assertEquals("Test String", StringUtility.unCamelCase("testString"), "Uncamel case failed");
    assertEquals("Test String Test", StringUtility.unCamelCase("testStringTest"),
        "Complex uncamel case failed");

    assertEquals("TestString", StringUtility.camelCase("test string"), "Camel case failed");
    assertEquals("TestStringTest", StringUtility.camelCase("test string test"),
        "Complex camel case failed");
  }

  /**
   * Test string manipulation.
   */
  @Test
  public void testStringManipulation() {
    assertEquals("test...", StringUtility.substr("test123", 4), "Substring with ellipsis failed");
    assertEquals("test123", StringUtility.substr("test123", 7), "Full substring failed");

    assertEquals("teXXXX3", StringUtility.mask("test123", 2, 6), "String masking failed");
    assertEquals("XXXXX", StringUtility.mask("12345", 0, 5), "Complete masking failed");
  }

  /**
   * Test validation functions.
   */
  @Test
  public void testValidation() {
    assertTrue(StringUtility.isUuid("550e8400-e29b-41d4-a716-446655440000"),
        "Valid UUID not recognized");
    assertFalse(StringUtility.isUuid("not-a-uuid"), "Invalid UUID recognized");

    assertTrue(StringUtility.isEmail("test@example.com"), "Valid email not recognized");
    assertFalse(StringUtility.isEmail("not-an-email"), "Invalid email recognized");
  }

  /**
   * Test query composition.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHTMLQueryComposition() throws Exception {
    final Map<String, String> clauses = new HashMap<>();
    clauses.put("field1", "value1");
    clauses.put("field2", "value2");
    final String queryString = StringUtility.composeQueryString(clauses);
    assertNotNull(queryString, "Query string should not be null");
    assertEquals("?field1=value1&field2=value2", queryString, "Query string missing first clause");

    final List<String> clauseList = Arrays.asList("term1", "term2");
    final String query = StringUtility.composeQuery("AND", clauseList);
    assertEquals("term1 AND term2", query, "AND query composition failed");
  }

  /**
   * Test string profiling.
   */
  @Test
  public void testStringProfiling() {
    final Map<String, Integer> profile = StringUtility.getProfile("hello", 2);
    assertNotNull(profile, "Profile should not be null");
    assertEquals(4, profile.size(), "Profile size incorrect");
    assertTrue(profile.containsKey("he"), "Profile missing expected bigram");
    assertTrue(profile.containsKey("el"), "Profile missing expected bigram");
  }

  /**
   * Test string escaping.
   */
  @Test
  public void testStringEscaping() {
    assertEquals("\\\"test\\\"", StringUtility.escapeJson("\"test\""), "JSON escaping failed");
    assertEquals("\\[test]", StringUtility.escapeRegex("[test]"), "Regex escaping failed");
    assertEquals("test\\?", StringUtility.escapeQuery("test?"), "Query escaping failed");
  }

  /**
   * Test UUID finding.
   */
  @Test
  public void testUuidFinding() {
    final String text = "Text with UUID 550e8400-e29b-41d4-a716-446655440000 and more text";
    final Set<String> uuids = StringUtility.findAllUuids(text);
    assertEquals(1, uuids.size(), "Should find one UUID");
    assertTrue(uuids.contains("550e8400-e29b-41d4-a716-446655440000"), "Should find correct UUID");
  }

  /**
   * Test random string generation.
   */
  @Test
  public void testRandomStringGeneration() {
    final String random1 = StringUtility.randomString(10);
    final String random2 = StringUtility.randomString(10);
    assertNotNull(random1, "Random string should not be null");
    assertEquals(10, random1.length(), "Random string length incorrect");
    assertFalse(random1.equals(random2), "Two random strings should not be equal");
  }
}
