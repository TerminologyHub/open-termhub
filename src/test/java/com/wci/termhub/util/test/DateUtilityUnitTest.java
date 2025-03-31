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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.util.DateUtility;

/**
 * Unit tests for {@link DateUtility}.
 */
public class DateUtilityUnitTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(DateUtilityUnitTest.class);

  /**
   * Test time zone offset label.
   *
   * @throws Exception the exception
   */
  @Test
  public void testTimeZoneOffsetLabel() throws Exception {
    // Test with known time zones
    ZoneId edtZone = ZoneId.of("America/New_York");
    ZoneId pdtZone = ZoneId.of("America/Los_Angeles");
    ZoneId parisZone = ZoneId.of("Europe/Paris");

    ZonedDateTime now = ZonedDateTime.now();

    // Calculate expected offsets dynamically
    String expectedEDTOffset = now.withZoneSameInstant(edtZone).getOffset().getId();
    String expectedPDTOffset = now.withZoneSameInstant(pdtZone).getOffset().getId();
    String expectedParisOffset = now.withZoneSameInstant(parisZone).getOffset().getId();

    // Test with known time zones
    assertEquals(expectedEDTOffset, DateUtility.getTimeZoneOffsetLabel("EDT", new Date()));
    assertEquals(expectedPDTOffset, DateUtility.getTimeZoneOffsetLabel("PDT", new Date()));
    assertEquals("Z", DateUtility.getTimeZoneOffsetLabel("UTC", new Date()));
    assertEquals("Z", DateUtility.getTimeZoneOffsetLabel(null, new Date()));

    // Test with ZoneId format
    assertEquals(expectedParisOffset, DateUtility.getTimeZoneOffsetLabel("Europe/Paris", new Date()));

    // Test with America/Los_Angeles
    ZoneId laZone = ZoneId.of("America/Los_Angeles");
    String expectedLAOffset = now.withZoneSameInstant(laZone).getOffset().getId();
    assertEquals(expectedLAOffset, DateUtility.getTimeZoneOffsetLabel("America/Los_Angeles", new Date()));
  }

  /**
   * Test time zone offset.
   *
   * @throws Exception the exception
   */
  @Test
  public void testTimeZoneOffset() throws Exception {
    // Test with known time zones
    // -4 hours in milliseconds
    assertEquals(-14400000, DateUtility.getTimeZoneOffset("EDT", new Date()));
    // -7 hours in milliseconds
    assertEquals(-25200000, DateUtility.getTimeZoneOffset("PDT", new Date()));
    // UTC = 0 offset
    assertEquals(0, DateUtility.getTimeZoneOffset("UTC", new Date()));
    // null defaults to UTC
    assertEquals(0, DateUtility.getTimeZoneOffset(null, new Date()));

    // Test with ZoneId format
    // Positive offset
    assertTrue(DateUtility.getTimeZoneOffset("Europe/Paris", new Date()) > 0);
    // Negative offset

    assertTrue(DateUtility.getTimeZoneOffset("America/Los_Angeles", new Date()) < 0);
  }

  /**
   * Test get time zone.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTimeZone() throws Exception {
    final long currentTime = System.currentTimeMillis();

    // Test UTC
    LOG.info("TimeZone {}", DateUtility.getTimeZone(currentTime, 0));
    assertEquals("UTC", DateUtility.getTimeZone(currentTime, 0));

    // Test EDT (-4 hours = -14400 seconds)
    assertEquals("-04:00", DateUtility.getTimeZone(currentTime, -14400));

    // Test PDT (-7 hours = -25200 seconds)
    assertEquals("-07:00", DateUtility.getTimeZone(currentTime, -25200));
  }

  /**
   * Test valid date.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidDate() throws Exception {
    final Date now = new Date();

    // Test null date
    assertFalse(DateUtility.isValidDate(now, null));

    // Test future date
    final Calendar future = Calendar.getInstance();
    future.add(Calendar.DAY_OF_MONTH, 1);
    assertFalse(DateUtility.isValidDate(now, future.getTime()));

    // Test past date before 1970
    final Calendar past = Calendar.getInstance();
    past.set(1969, 11, 31);
    // assertFalse(DateUtility.isValidDate(now, past.getTime()));

    // Test today's date
    assertFalse(DateUtility.isValidDate(now, now));

    // Test valid date (yesterday)
    final Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DAY_OF_MONTH, -1);
    assertTrue(DateUtility.isValidDate(now, yesterday.getTime()));
  }

  /**
   * Test date formatting.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDateFormatting() throws Exception {
    final Date testDate = new Date();

    // Test various date formats
    assertNotNull(DateUtility.RFC_3339.format(testDate));
    assertNotNull(DateUtility.DATE_YYYYMMDD.format(testDate));
    assertNotNull(DateUtility.UTC_YYYYMMDD.format(testDate));
    assertNotNull(DateUtility.DATE_YYYY_MM_DD.format(testDate));
    assertNotNull(DateUtility.DATE_YYYY_MM_DD_DASH.format(testDate));
    assertNotNull(DateUtility.DATE_YYYY_MM_DD_SLASH.format(testDate));
    assertNotNull(DateUtility.DATE_DD_MM_YYYY.format(testDate));
    assertNotNull(DateUtility.DATE_MM_DD_YYYY.format(testDate));
    assertNotNull(DateUtility.DATE_MMDDYYYY.format(testDate));
    assertNotNull(DateUtility.DATE_YYYY.format(testDate));
  }

  /**
   * Test start and end of day.
   *
   * @throws Exception the exception
   */
  @Test
  public void testStartAndEndOfDay() throws Exception {
    final String dateStr = "20240101"; // January 1, 2024
    final String timeZone = "UTC";

    // Test start of day
    final Date startOfDay = DateUtility.getStartOfDay(dateStr, timeZone);
    final Calendar startCal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
    startCal.setTime(startOfDay);
    assertEquals(0, startCal.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, startCal.get(Calendar.MINUTE));
    assertEquals(0, startCal.get(Calendar.SECOND));
    assertEquals(0, startCal.get(Calendar.MILLISECOND));

    // Test end of day
    final Date endOfDay = DateUtility.getEndOfDay(dateStr, timeZone);
    final Calendar endCal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
    endCal.setTime(endOfDay);
    assertEquals(23, endCal.get(Calendar.HOUR_OF_DAY));
    assertEquals(59, endCal.get(Calendar.MINUTE));
    assertEquals(59, endCal.get(Calendar.SECOND));
    assertEquals(999, endCal.get(Calendar.MILLISECOND));
  }

  /**
   * Test get date with pattern.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDateWithPattern() throws Exception {
    final String dateStr = "2024-01-01";
    final String pattern = "yyyy-MM-dd";
    final String timeZone = "UTC";

    final Date date = DateUtility.getDate(dateStr, pattern, timeZone);
    assertNotNull(date);

    final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
    cal.setTime(date);
    assertEquals(2024, cal.get(Calendar.YEAR));
    assertEquals(0, cal.get(Calendar.MONTH)); // January is 0
    assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
  }

  /**
   * Test get new date.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetNewDate() throws Exception {
    final OffsetDateTime date = DateUtility.getNewDate("UTC");
    assertNotNull(date);
    // assertEquals("UTC", date.getOffset().getId());

    // Test with different time zone
    final OffsetDateTime dateEDT = DateUtility.getNewDate("America/New_York");
    assertNotNull(dateEDT);
    // EDT should have negative offset
    assertTrue(dateEDT.getOffset().getTotalSeconds() < 0);
  }
}
