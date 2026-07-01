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

import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.InstantType;

import com.wci.termhub.util.DateUtility;

/**
 * Sets FHIR dateTime/instant elements from stored strings so serialization preserves UTC offsets
 * instead of converting through {@code java.util.Date} and the JVM default timezone.
 */
public final class FhirDateTimeUtil {

  private FhirDateTimeUtil() {
    // n/a
  }

  /**
   * Sets an R4 dateTime element from a stored date string.
   *
   * @param element the dateTime element
   * @param dateString the stored date (ISO 8601 or yyyy-MM-dd)
   */
  public static void setR4DateTimeUtc(final DateTimeType element, final String dateString) {
    final String utc = DateUtility.toFhirUtcDateTimeString(dateString);
    if (utc != null) {
      element.setValueAsString(utc);
    }
  }

  /**
   * Sets an R4 instant element from a stored date string.
   *
   * @param element the instant element
   * @param dateString the stored date (ISO 8601 or yyyy-MM-dd)
   */
  public static void setR4InstantUtc(final InstantType element, final String dateString) {
    final String utc = DateUtility.toFhirUtcDateTimeString(dateString);
    if (utc != null) {
      element.setValueAsString(utc);
    }
  }

  /**
   * Sets an R5 dateTime element from a stored date string.
   *
   * @param element the dateTime element
   * @param dateString the stored date (ISO 8601 or yyyy-MM-dd)
   */
  public static void setR5DateTimeUtc(final org.hl7.fhir.r5.model.DateTimeType element,
    final String dateString) {
    final String utc = DateUtility.toFhirUtcDateTimeString(dateString);
    if (utc != null) {
      element.setValueAsString(utc);
    }
  }

  /**
   * Sets an R5 instant element from a stored date string.
   *
   * @param element the instant element
   * @param dateString the stored date (ISO 8601 or yyyy-MM-dd)
   */
  public static void setR5InstantUtc(final org.hl7.fhir.r5.model.InstantType element,
    final String dateString) {
    final String utc = DateUtility.toFhirUtcDateTimeString(dateString);
    if (utc != null) {
      element.setValueAsString(utc);
    }
  }
}
