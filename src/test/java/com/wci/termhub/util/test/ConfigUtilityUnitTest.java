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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.util.ConfigUtility;

/**
 * Unit tests for {@link ConfigUtility}.
 */
public class ConfigUtilityUnitTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(ConfigUtilityUnitTest.class);

  /** The temp directory. */
  @TempDir
  private File tempDir;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    ConfigUtility.clearConfigProperties();
  }

  /**
   * Test empty checks.
   */
  @Test
  public void testEmptyChecks() {
    assertTrue(ConfigUtility.isEmpty((String) null));
    assertTrue(ConfigUtility.isEmpty(""));
    assertFalse(ConfigUtility.isEmpty("test"));

    assertTrue(ConfigUtility.isEmpty((List<?>) null));
    assertTrue(ConfigUtility.isEmpty(new ArrayList<>()));
    assertFalse(ConfigUtility.isEmpty(Arrays.asList("test")));
  }

  /**
   * Test first not null.
   */
  @Test
  public void testFirstNotNull() {
    assertNull(ConfigUtility.firstNotNull((String) null, null));
    assertEquals("test", ConfigUtility.firstNotNull(null, "test", "test2"));
    assertEquals("test", ConfigUtility.firstNotNull("test", null, "test2"));
  }

  /**
   * Test collection conversions.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCollectionConversions() throws Exception {
    // Test asList
    final String[] strArray = {
        "a", "b", "c"
    };
    final List<String> list = ConfigUtility.asList(strArray);
    assertEquals(3, list.size());
    assertTrue(list.contains("a"));
    assertTrue(list.contains("b"));
    assertTrue(list.contains("c"));

    // Test asSet
    final Set<String> set = ConfigUtility.asSet(strArray);
    assertEquals(3, set.size());
    assertTrue(set.contains("a"));
    assertTrue(set.contains("b"));
    assertTrue(set.contains("c"));

    // Test varargs with null values
    final List<String> listWithNulls = ConfigUtility.asList("a", null, "c");
    assertEquals(2, listWithNulls.size());
    assertFalse(listWithNulls.contains(null));

    final Set<String> setWithNulls = ConfigUtility.asSet("a", null, "c");
    assertEquals(2, setWithNulls.size());
    assertFalse(setWithNulls.contains(null));
  }

  /**
   * Test map operations.
   */
  @Test
  public void testMapOperations() {
    final Map<String, String> map = ConfigUtility.asMap("key1", "value1", "key2", "value2");
    assertEquals(2, map.size());
    assertEquals("value1", map.get("key1"));
    assertEquals("value2", map.get("key2"));

    // Test odd number of parameters
    assertThrows(RuntimeException.class, () -> ConfigUtility.asMap("key1", "value1", "key2"));
  }

  /**
   * Test config properties.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConfigProperties() throws Exception {
    // Create a temporary config file
    final File configFile = new File(tempDir, "config.properties");
    try (FileWriter writer = new FileWriter(configFile)) {
      writer.write("test.property=value\n");
      writer.write("test.mode=true\n");
    }

    // Set the system property to point to our test config
    System.setProperty("run.config", configFile.getAbsolutePath());

    // Test getting properties
    final Properties props = ConfigUtility.getConfigProperties();
    assertNotNull(props);
    assertEquals("value", props.getProperty("test.property"));
    assertEquals("true", props.getProperty("test.mode"));

    // Test clearing properties
    ConfigUtility.clearConfigProperties();
    assertNotNull(ConfigUtility.getConfigProperties());
  }

  /**
   * Test file operations.
   */
  @Test
  public void testFileOperations() {
    assertEquals("txt", ConfigUtility.getFileExtension("test.txt"));
    assertEquals("", ConfigUtility.getFileExtension("test"));
    assertEquals("test", ConfigUtility.getBaseFilename("test.txt"));
    assertEquals("test", ConfigUtility.getBaseFilename("test"));
  }

  /**
   * Test string operations.
   */
  @Test
  public void testStringOperations() {
    assertTrue(ConfigUtility.isPossibleCode("123"));
    assertTrue(ConfigUtility.isPossibleCode("A123"));
    assertFalse(ConfigUtility.isPossibleCode("ABC"));
    assertEquals("test...", ConfigUtility.substr("test123", 4));
    assertEquals("test123", ConfigUtility.substr("test123", 10));
    assertEquals("teXXXXX", ConfigUtility.mask("test123", 2, 7));
  }

  /**
   * Test null comparisons.
   */
  @Test
  public void testNullComparisons() {
    assertTrue(ConfigUtility.equalsNullSafe(null, null));
    assertFalse(ConfigUtility.equalsNullSafe(null, "test"));
    assertFalse(ConfigUtility.equalsNullSafe("test", null));
    assertTrue(ConfigUtility.equalsNullSafe("test", "test"));
    assertFalse(ConfigUtility.equalsNullSafe("test1", "test2"));

    assertTrue(ConfigUtility.equalsNullMatch(null, null));
    assertTrue(ConfigUtility.equalsNullMatch(null, "test"));
    assertTrue(ConfigUtility.equalsNullMatch("test", null));
    assertTrue(ConfigUtility.equalsNullMatch("test", "test"));
    assertFalse(ConfigUtility.equalsNullMatch("test1", "test2"));
  }

  /**
   * Test UUID validation.
   */
  @Test
  public void testUuidValidation() {
    assertTrue(ConfigUtility.isUuid("550e8400-e29b-41d4-a716-446655440000"));
    assertFalse(ConfigUtility.isUuid("not-a-uuid"));
    assertFalse(ConfigUtility.isUuid(null));
  }

  /**
   * Test date validation.
   */
  @Test
  public void testDateValidation() {
    // Create a date for "now" that's not at the start of a day
    final Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 12); // Set to noon
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    final Date now = cal.getTime();

    // Create a date in the past (not today)
    cal.add(Calendar.DAY_OF_MONTH, -5); // 5 days ago
    final Date validPastDate = cal.getTime();

    // Create a future date
    cal.setTime(now);
    cal.add(Calendar.DAY_OF_MONTH, 1); // tomorrow
    final Date future = cal.getTime();

    // Create a date before Unix epoch
    final Date past = new Date(-86400000L); // One day before Unix epoch

    // Create a date today
    cal.setTime(now);
    cal.set(Calendar.HOUR_OF_DAY, 15); // Different hour, same day
    final Date today = cal.getTime();

    assertFalse(ConfigUtility.isValidDate(now, null)); // null date
    assertFalse(ConfigUtility.isValidDate(now, future)); // future date
    assertFalse(ConfigUtility.isValidDate(now, past)); // before 1970
    assertFalse(ConfigUtility.isValidDate(now, today)); // same day
    assertTrue(ConfigUtility.isValidDate(now, validPastDate)); // valid past
                                                               // date
  }

  /**
   * Test class name formatting.
   */
  @Test
  public void testClassNameFormatting() {
    assertEquals("Config Utility Unit Test",
        ConfigUtility.getNameFromClass(ConfigUtilityUnitTest.class));
  }
}
