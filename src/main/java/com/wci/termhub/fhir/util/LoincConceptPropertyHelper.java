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

import com.wci.termhub.model.Concept;

/**
 * LOINC concept property helpers for loader output and FHIR $lookup.
 */
public final class LoincConceptPropertyHelper {

  /** FHIR standard concept status property code (valueCode). */
  public static final String ATTR_STATUS_CODE = "status";

  /** LOINC display status property code (valueString, e.g. Active). */
  public static final String ATTR_STATUS_DISPLAY = "STATUS";

  /**
   * Instantiates a {@link LoincConceptPropertyHelper}.
   */
  private LoincConceptPropertyHelper() {
    // utility class
  }

  /**
   * When Regenstrief mode is on and both status codes are stored, emit only
   * STATUS on $lookup.
   *
   * @param attributeKey the concept attribute key
   * @param concept the concept
   * @param regenstriefMode true when
   *          {@code fhir.loinc.lllg.valuesets.enabled=true}
   * @param isLoinc true when the code system is LOINC
   * @return true to skip emitting this attribute as a property
   */
  public static boolean suppressStatusOnLookupOutput(final String attributeKey,
    final Concept concept, final boolean regenstriefMode, final boolean isLoinc) {
    return regenstriefMode && isLoinc && ATTR_STATUS_CODE.equals(attributeKey)
        && concept.getAttributes().containsKey(ATTR_STATUS_CODE)
        && concept.getAttributes().containsKey(ATTR_STATUS_DISPLAY);
  }

  /**
   * True when the FHIR property name is the standard lowercase {@code status}
   * (valueCode).
   *
   * @param propertyName the property name
   * @return true for status
   */
  public static boolean isStatusValueCodeProperty(final String propertyName) {
    return ATTR_STATUS_CODE.equals(propertyName);
  }
}
