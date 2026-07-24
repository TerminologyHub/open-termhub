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

import java.util.Locale;
import java.util.Map;

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
   * @param regenstriefMode true when {@code server.mode=regenstrief}
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

  /**
   * Relationship properties stored on the concept must not be emitted on $lookup;
   * hierarchy and panel membership are resolved from the relationship index.
   *
   * @param propertyCode the FHIR property code
   * @return true to skip emitting this property
   */
  public static boolean suppressRelationshipPropertyOnLookupOutput(final String propertyCode) {
    if (propertyCode == null) {
      return false;
    }
    return "parent".equals(propertyCode) || "child".equals(propertyCode)
        || LoincConstants.LOINC_REL_PANEL_MEMBER.equals(propertyCode)
        || LoincConstants.LOINC_REL_HAS_MEMBER.equals(propertyCode);
  }

  /**
   * LOINC part / answer class codes (LP…).
   *
   * @param value the value
   * @return true, if is loinc part code
   */
  public static boolean isLoincPartCode(final String value) {
    return value != null && value.matches("^LP\\d+-\\d+$");
  }

  /**
   * True for attribute keys that only store display text for a {@code valueCoding} pair and must
   * not be emitted as their own {@code property} in $lookup.
   *
   * @param key the attribute key
   * @return true if internal display-only key
   */
  public static boolean isLoincLookupInternalDisplayKey(final String key) {
    return key != null && key.endsWith("_display");
  }

  /**
   * True when this attribute is the legacy uppercase string duplicate of a lowercase
   * {@code valueCoding} property (same axis: display text vs LP code).
   *
   * @param key the attribute key
   * @param value the attribute value
   * @param concept the concept
   * @return true if superseded by the canonical lowercase valueCoding attribute
   */
  public static boolean isLoincLegacyStringSupersededByValueCoding(final String key,
    final String value, final Concept concept) {
    if (key == null || value == null
        || !LoincConstants.LOINC_UPPERCASE_PROPERTY_KEYS.contains(key)) {
      return false;
    }
    final String canonical = key.toLowerCase(Locale.ROOT);
    final String canonicalVal = concept.getAttributes().get(canonical);
    if (canonicalVal == null || !isLoincPartCode(canonicalVal)) {
      return false;
    }
    return !isLoincPartCode(value);
  }

  /**
   * Resolve loinc property display.
   *
   * @param key the key
   * @param value the value
   * @param codingCode the coding code
   * @param concept the concept
   * @param displayMap the display map
   * @return the string
   */
  public static String resolveLoincPropertyDisplay(final String key, final String value,
    final String codingCode, final Concept concept, final Map<String, String> displayMap) {
    final String fromAttr = concept.getAttributes().get(key + "_display");
    if (fromAttr != null) {
      return fromAttr;
    }
    if (value != null && !isLoincPartCode(value)) {
      return value;
    }
    if (codingCode != null && displayMap != null && displayMap.containsKey(codingCode)) {
      return displayMap.get(codingCode);
    }
    return codingCode != null ? codingCode : value;
  }

  /**
   * Legacy {@code Map} keys used {@code _N} suffixes for duplicate FHIR property codes. Strip that
   * for the $lookup parameter name (indexed documents only; reload uses
   * {@link com.wci.termhub.model.Concept#getFhirPropertyCodings()}).
   *
   * @param attributeKey the attribute key
   * @return FHIR property name
   */
  public static String loincLookupPropertyName(final String attributeKey) {
    if (attributeKey != null && attributeKey.matches(".+_\\d+")) {
      return attributeKey.replaceFirst("_\\d+$", "");
    }
    return attributeKey;
  }
}
