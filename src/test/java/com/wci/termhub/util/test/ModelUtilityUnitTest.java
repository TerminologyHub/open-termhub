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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.wci.termhub.util.ModelUtility;

/**
 * Unit test for ModelUtility.
 */
public class ModelUtilityUnitTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtilityUnitTest.class);

  /**
   * Test class for JSON conversion.
   */
  public static class TestClass {

    /** The name. */
    private String name;

    /** The age. */
    private int age;

    /**
     * Instantiates a new test class.
     */
    public TestClass() {
    }

    /**
     * Instantiates a new test class.
     *
     * @param name the name
     * @param age the age
     */
    public TestClass(final String name, final int age) {
      this.name = name;
      this.age = age;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
      return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(final String name) {
      this.name = name;
    }

    /**
     * Gets the age.
     *
     * @return the age
     */
    public int getAge() {
      return age;
    }

    /**
     * Sets the age.
     *
     * @param age the new age
     */
    public void setAge(final int age) {
      this.age = age;
    }
  }

  /**
   * Test null comparison methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNullComparisons() throws Exception {
    LOGGER.info("TEST testNullComparisons");

    // Test bothOrNeitherNull
    assertTrue(ModelUtility.bothOrNeitherNull(null, null));
    assertTrue(ModelUtility.bothOrNeitherNull("test", "other"));
    assertFalse(ModelUtility.bothOrNeitherNull(null, "test"));
    assertFalse(ModelUtility.bothOrNeitherNull("test", null));

    // Test equalsNullSafe
    assertTrue(ModelUtility.equalsNullSafe(null, null));
    assertTrue(ModelUtility.equalsNullSafe("test", "test"));
    assertFalse(ModelUtility.equalsNullSafe(null, "test"));
    assertFalse(ModelUtility.equalsNullSafe("test", null));
    assertFalse(ModelUtility.equalsNullSafe("test", "other"));

    // Test equalsNullMatch
    assertTrue(ModelUtility.equalsNullMatch(null, "test"));
    assertTrue(ModelUtility.equalsNullMatch("test", null));
    assertTrue(ModelUtility.equalsNullMatch(null, null));
    assertTrue(ModelUtility.equalsNullMatch("test", "test"));
    assertFalse(ModelUtility.equalsNullMatch("test", "other"));
  }

  /**
   * Test date utility methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDateUtilities() throws Exception {
    LOGGER.info("TEST testDateUtilities");

    final Calendar cal = Calendar.getInstance();
    final Date date1 = cal.getTime();
    cal.add(Calendar.DAY_OF_MONTH, 1);
    final Date date2 = cal.getTime();
    cal.add(Calendar.DAY_OF_MONTH, 1);
    final Date date3 = cal.getTime();

    // Test getMinDate
    assertEquals(date1, ModelUtility.getMinDate(date1, date2, date3));
    assertEquals(date1, ModelUtility.getMinDate(date3, date1, date2));

    // Test with null dates
    assertEquals(date1, ModelUtility.getMinDate(null, date1, null));

    // Test getMaxDate
    assertEquals(date3, ModelUtility.getMaxDate(date1, date2, date3));
    assertEquals(date3, ModelUtility.getMaxDate(date3, date1, date2));

    // Test with null dates
    assertEquals(date3, ModelUtility.getMaxDate(null, date3, null));
  }

  /**
   * Test string and collection utilities.
   *
   * @throws Exception the exception
   */
  @Test
  public void testStringAndCollectionUtilities() throws Exception {
    LOGGER.info("TEST testStringAndCollectionUtilities");

    // Test getNameFromClass
    assertEquals("Model Utility Unit Test", ModelUtility.getNameFromClass(this.getClass()));

    // Test isID
    assertTrue(ModelUtility.isID("12345678-1234-1234-1234-123456781234"));
    assertFalse(ModelUtility.isID("not-a-uuid"));

    // Test firstNotNull
    assertEquals("first", ModelUtility.firstNotNull(null, "first", "second"));
    assertNull(ModelUtility.firstNotNull(null, null, null));

    // Test isEmpty for collections
    assertTrue(ModelUtility.isEmpty(null));
    assertTrue(ModelUtility.isEmpty(new ArrayList<>()));
    assertFalse(ModelUtility.isEmpty(Arrays.asList("test")));

    // Test nvl (null value logic)
    assertEquals("default", ModelUtility.nvl(null, "default"));
    assertEquals("value", ModelUtility.nvl("value", "default"));
  }

  /**
   * Test collection conversion utilities.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCollectionConversions() throws Exception {
    LOGGER.info("TEST testCollectionConversions");

    // Test asMap
    final Map<String, String> map = ModelUtility.asMap("key1", "value1", "key2", "value2");
    assertEquals(2, map.size());
    assertEquals("value1", map.get("key1"));
    assertEquals("value2", map.get("key2"));

    // Test asList with array
    final String[] array = new String[] {
        "one", "two", "three"
    };
    final List<String> list = ModelUtility.asList(array);
    assertEquals(3, list.size());
    assertTrue(list.contains("one"));
    assertTrue(list.contains("two"));
    assertTrue(list.contains("three"));

    // Test asList with varargs
    final List<String> varargsList = ModelUtility.asList("one", "two", "three");
    assertEquals(3, varargsList.size());
    assertTrue(varargsList.contains("one"));
    assertTrue(varargsList.contains("two"));
    assertTrue(varargsList.contains("three"));

    // Test asSet with array
    final Set<String> set = ModelUtility.asSet(array);
    assertEquals(3, set.size());
    assertTrue(set.contains("one"));
    assertTrue(set.contains("two"));
    assertTrue(set.contains("three"));

    // Test asSet with varargs
    final Set<String> varargSet = ModelUtility.asSet("one", "two", "three");
    assertEquals(3, varargSet.size());
    assertTrue(varargSet.contains("one"));
    assertTrue(varargSet.contains("two"));
    assertTrue(varargSet.contains("three"));
  }

  /**
   * Test JSON utilities.
   *
   * @throws Exception the exception
   */
  @Test
  public void testJsonUtilities() throws Exception {
    LOGGER.info("TEST testJsonUtilities");

    // Test object to JSON
    final TestClass testObj = new TestClass("Test Name", 25);
    final String json = ModelUtility.toJson(testObj);
    assertNotNull(json);
    assertTrue(json.contains("Test Name"));
    assertTrue(json.contains("25"));

    // Test JSON to object
    final TestClass fromJson = ModelUtility.fromJson(json, TestClass.class);
    assertNotNull(fromJson);
    assertEquals("Test Name", fromJson.getName());
    assertEquals(25, fromJson.getAge());

    // Test JSON to JsonNode
    final JsonNode jsonNode = ModelUtility.toJsonNode(json);
    assertNotNull(jsonNode);
    assertEquals("Test Name", jsonNode.get("name").asText());
    assertEquals(25, jsonNode.get("age").asInt());

    // Test pretty format JSON
    final String prettyJson = ModelUtility.prettyFormatJson(testObj);
    assertNotNull(prettyJson);
    assertTrue(prettyJson.contains("Test Name"));
    assertTrue(prettyJson.contains("25"));
    assertTrue(prettyJson.contains("\n")); // Should be formatted with newlines

    // Test JSON copy
    final TestClass copy = ModelUtility.jsonCopy(testObj, TestClass.class);
    assertNotNull(copy);
    assertEquals(testObj.getName(), copy.getName());
    assertEquals(testObj.getAge(), copy.getAge());

    // Test with TypeReference
    final String listJson = "[{\"name\":\"Test1\",\"age\":25},{\"name\":\"Test2\",\"age\":30}]";
    final List<TestClass> list =
        ModelUtility.fromJson(listJson, new TypeReference<List<TestClass>>() {
          // n/a
        });
    assertNotNull(list);
    assertEquals(2, list.size());
    assertEquals("Test1", list.get(0).getName());
    assertEquals(30, list.get(1).getAge());
  }

  /**
   * Test stack trace utilities.
   *
   * @throws Exception the exception
   */
  @Test
  public void testStackTraceUtilities() throws Exception {
    LOGGER.info("TEST testStackTraceUtilities");

    final Exception testException = new Exception("Test Exception");

    // Test getStackTrace without WCI flag
    final String stackTrace = ModelUtility.getStackTrace(testException);
    assertNotNull(stackTrace);
    assertTrue(stackTrace.contains("Test Exception"));

    // Test getStackTrace with WCI flag
    final String wciStackTrace = ModelUtility.getStackTrace(testException, true);
    assertNotNull(wciStackTrace);
    assertTrue(wciStackTrace.contains("Test Exception"));
  }
}
