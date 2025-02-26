/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for interacting with dates.
 */
public final class DateUtility {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(DateUtility.class);

  /** The zone map. */
  private static Map<String, String> zoneMap = new HashMap<>();
  static {
    zoneMap.put("EDT", "-04:00");
    zoneMap.put("PDT", "-07:00");
  }

  /** The Constant RFC_3339. */
  public static final FastDateFormat RFC_3339 =
      FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssZZZZZ");

  /** The Constant DATE_YYYYMMDD. */
  public static final FastDateFormat DATE_YYYYMMDD = FastDateFormat.getInstance("yyyyMMdd");

  /** The Constant UTC_YYYYMMDD. */
  public static final FastDateFormat UTC_YYYYMMDD =
      FastDateFormat.getInstance("yyyyMMdd", TimeZone.getTimeZone("UTC"));

  /** The Constant DATE_FORMAT2. */
  public static final FastDateFormat DATE_YYYY_MM_DD = FastDateFormat.getInstance("yyyy_MM_dd");

  /** The Constant DATE_YYYY_MM_DD_DASH. */
  public static final FastDateFormat DATE_YYYY_MM_DD_DASH =
      FastDateFormat.getInstance("yyyy-MM-dd");

  /** The Constant DATE_YYYY_MM_DD_SLASH. */
  public static final FastDateFormat DATE_YYYY_MM_DD_SLASH =
      FastDateFormat.getInstance("yyyy/MM/dd");

  /** The Constant UTC_YYYY_MM_DD_DASH. */
  public static final FastDateFormat UTC_YYYY_MM_DD_DASH =
      FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"));

  /** The Constant DATE_DD_MM_YYYY. */
  public static final FastDateFormat DATE_DD_MM_YYYY = FastDateFormat.getInstance("dd_MM_yyyy");

  /** The Constant DATE_MM_DD_YYYY. */
  public static final FastDateFormat DATE_MM_DD_YYYY = FastDateFormat.getInstance("MM_dd_yyyy");

  /** The Constant DATE_MMDDYYYY. */
  public static final FastDateFormat DATE_MMDDYYYY = FastDateFormat.getInstance("MMddyyyy");

  /** The Constant UTC_MMDDYYYY. */
  public static final FastDateFormat UTC_MMDDYYYY =
      FastDateFormat.getInstance("MMddyyyy", TimeZone.getTimeZone("UTC"));

  /** The Constant DATE_FORMAT3. */
  public static final FastDateFormat DATE_YYYY = FastDateFormat.getInstance("yyyy");

  /** The Constant DATE_FORMAT4. */
  public static final FastDateFormat DATE_YYYY_MM_DD_X1 =
      FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

  /** The Constant DATE_YYYY_MM_DD_X1_XXX. */
  public static final FastDateFormat DATE_YYYY_MM_DD_X1_XXX =
      FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ssXXX");

  /** The Constant DATE_FORMAT5. */
  public static final FastDateFormat DATE_YYYY_MM_DD_X2 =
      FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS");

  /** The Constant DATE_YYYY_MM_DD_X2_XXX. */
  public static final FastDateFormat DATE_YYYY_MM_DD_X2_XXX =
      FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  /** The Constant DATE_FORMAT4. */
  public static final FastDateFormat DATE_YYYYMMDDHHMMSS =
      FastDateFormat.getInstance("yyyyMMddHHmmss");

  /** The Constant DATE_YYYYMMDDHHMMSSXXX. */
  public static final FastDateFormat DATE_YYYYMMDDHHMMSSXXX =
      FastDateFormat.getInstance("yyyyMMddHHmmssXXX");

  /** The Constant DATE_YYYYMMDDHHMMSSZZZZZ. */
  public static final FastDateFormat DATE_YYYYMMDDHHMMSSZZZZZ =
      FastDateFormat.getInstance("yyyyMMddHHmmssZZZZZ");

  /** The Constant DAY. */
  public static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

  /**
   * Instantiates an empty {@link DateUtility}.
   */
  private DateUtility() {
    // n/a
  }

  /**
   * Returns the time zone offset.
   *
   * @param tz the tz
   * @param date the date
   * @return the time zone offset
   */
  public static String getTimeZoneOffsetLabel(final String tz, final Date date) {

    // Return UTC for null timezone.
    if (tz == null) {
      return ZoneOffset.UTC.getId();
    }

    final Instant instant = date == null ? Instant.now() : date.toInstant();
    // First try this style
    try {
      return ZoneId.of(tz).getRules().getOffset(instant).getId();
    } catch (final Exception e) {
      // n/a
    }
    try {
      return ZoneOffset.of(tz).getId();
    } catch (final Exception e) {
      // n/a
    }
    try {
      final String x = TimeZone.getTimeZone(tz).toZoneId().getRules().getOffset(instant).getId();
      if ("Z".equals(x)) {
        return ZoneId.of(tz, zoneMap).getRules().getOffset(instant).getId();
      }
      if (x != null) {
        return x;
      }
    } catch (final Exception e) {
      // n/a
    }
    return ZoneOffset.UTC.getId();
  }

  /**
   * Returns the time zone offset.
   *
   * @param tz the tz
   * @param date the date
   * @return the time zone offset
   */
  public static long getTimeZoneOffset(final String tz, final Date date) {

    if (tz == null) {
      return ZoneOffset.UTC.getTotalSeconds() * 1000;
    }

    final Instant instant = date == null ? Instant.now() : date.toInstant();
    // First try this style
    try {
      return ZoneId.of(tz).getRules().getOffset(instant).getTotalSeconds() * 1000;
    } catch (final Exception e) {
      // n/a
    }
    try {
      return ZoneOffset.of(tz).getTotalSeconds() * 1000;
    } catch (final Exception e) {
      // n/a
    }
    try {
      final String x = TimeZone.getTimeZone(tz).toZoneId().getRules().getOffset(instant).getId();
      if ("Z".equals(x)) {
        return ZoneId.of(tz, zoneMap).getRules().getOffset(instant).getTotalSeconds() * 1000;
      }
      if (x != null) {
        return ZoneOffset.of(x).getTotalSeconds() * 1000;
      }
    } catch (final Exception e) {
      // n/a
    }
    logger.warn("    REVERTING to default time zone UTC = " + tz);
    return ZoneOffset.UTC.getTotalSeconds() * 1000;
  }

  /**
   * Returns the time zone.
   *
   * @param time the time
   * @param secondsOffset the seconds offset
   * @return the time zone
   */
  public static String getTimeZone(final long time, final int secondsOffset) {
    final ZoneId id = Instant.ofEpochMilli(time).atOffset(ZoneOffset.ofTotalSeconds(secondsOffset))
        .toZonedDateTime().getZone();
    return secondsOffset == 0 ? "UTC" : id.getId();
  }

  /**
   * Indicates whether or not valid date is the case.
   *
   * @param now the now
   * @param date the date
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public static boolean isValidDate(final Date now, final Date date) {
    if (date == null) {
      return false;
    }
    // Not in the future
    final boolean future = date.after(now);
    // Not before Jan 1 1970
    final boolean past = date.before(new Date(0));
    // Not within 5 seconds before "now"
    // final boolean nowish = Math.abs(now.getTime() - date.getTime()) < 5000;
    // Not "today"
    final boolean today = DateUtils.truncate(now, Calendar.DAY_OF_MONTH)
        .equals(DateUtils.truncate(date, Calendar.DAY_OF_MONTH));
    return !future && !past && !today;
  }

  /**
   * Returns the date.
   *
   * @param yyyymmdd the yyyymmdd
   * @param timeZone the time zone
   * @return the date
   */
  public static Date getStartOfDay(final String yyyymmdd, final String timeZone) {
    return Date.from(LocalDate.parse(yyyymmdd, DAY).atStartOfDay(ZoneId.of(timeZone)).toInstant());
  }

  /**
   * Returns the end of day.
   *
   * @param yyyymmdd the yyyymmdd
   * @param timeZone the time zone
   * @return the end of day
   */
  public static Date getEndOfDay(final String yyyymmdd, final String timeZone) {
    return Date.from(LocalDate.parse(yyyymmdd, DAY).atTime(LocalTime.MAX)
        .atZone(ZoneId.of(timeZone)).toInstant());
  }

  /**
   * Returns the date.
   *
   * @param dateStr the date str
   * @param pattern the pattern
   * @param timeZone the time zone
   * @return the date
   * @throws Exception the exception
   */
  public static Date getDate(final String dateStr, final String pattern, final String timeZone)
    throws Exception {
    try {
      // Try parsing as LocalDateTime first
      final LocalDateTime ldt = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
      final ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.of(timeZone));
      return Date.from(zdt.toInstant());
    } catch (DateTimeException e) {
      // If that fails, try parsing as LocalDate
      final LocalDate ld = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
      final ZonedDateTime zdt = ld.atStartOfDay(ZoneId.of(timeZone));
      return Date.from(zdt.toInstant());
    }
  }

  /**
   * Returns a new date object at the specified timezone, or UTC.
   *
   *
   * @param timeZone the time zone
   * @return the date object
   * @throws Exception the exception
   */
  public static OffsetDateTime getNewDate(final String timeZone) throws Exception {

    String newTimezone = timeZone;

    if (timeZone == null) {
      newTimezone = "UTC";
    }
    final OffsetDateTime newDate = OffsetDateTime.now(ZoneId.of(newTimezone));
    return newDate;
  }

}
