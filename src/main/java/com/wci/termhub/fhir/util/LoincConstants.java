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

/**
 * Shared LOINC identifiers used across FHIR providers and utilities.
 */
public final class LoincConstants {

  /** LOINC canonical URI. */
  public static final String LOINC_URI = "http://loinc.org";

  /** LOINC system abbreviation. */
  public static final String LOINC_SYSTEM = "LOINC";

  /** Alternate LOINC system abbreviation (e.g. sandbox). */
  public static final String LOINC_SYSTEM_ALT = "LNC";

  /** LOINC publisher as stored in loaded terminology data. */
  public static final String LOINC_PUBLISHER = "Regenstrief Institute, Inc.";

  /** Alternate LOINC publisher string seen in some indexes. */
  public static final String LOINC_PUBLISHER_ALT = "Regenstrief Institute";

  /** LOINC survey instruments concept code. */
  public static final String LOINC_SURVEY_INSTRUMENTS_CODE = "LP29696-9";

  /** URL prefix for LOINC value sets (query form). */
  public static final String LOINC_VS_URL_PREFIX = "http://loinc.org?fhir_vs";

  /** Path prefix for LOINC value set URLs (e.g. http://loinc.org/vs/LG51018-6-2.72). */
  public static final String LOINC_VS_PATH_PREFIX = "http://loinc.org/vs/";

  /** Path prefix for LOINC value set URLs (HTTPS). */
  public static final String LOINC_VS_PATH_PREFIX_HTTPS = "https://loinc.org/vs/";

  /** Concept attribute for answer list ID (LOINC). */
  public static final String ATTR_ANSWER_LIST_ID = "ANSWER_LIST_ID";

  /** LOINC short common name concept attribute. */
  public static final String ATTR_SHORT_COMMON_NAME = "SHORT_COMMON_NAME";

  /** LOINC short common name term/designation type. */
  public static final String TERM_TYPE_SHORT_COMMON_NAME = "SHORT_COMMON_NAME";

  /** LOINC short name concept attribute (official LOINC column). */
  public static final String ATTR_SHORTNAME = "SHORTNAME";

  /** LOINC short name term/designation type. */
  public static final String TERM_TYPE_SHORTNAME = "SHORTNAME";

  /** Panel membership relationship (full LOINC loads). */
  public static final String LOINC_REL_PANEL_MEMBER = "member";

  /** Panel membership relationship (sandbox / legacy). */
  public static final String LOINC_REL_HAS_MEMBER = "has_member";

  /** Member-edge form link id (parent relationship extension). */
  public static final String ATTR_REL_ID = "ID";

  /** Member-edge form prefix (parent relationship extension). */
  public static final String ATTR_OBSERVATION_ID_IN_FORM = "ObservationIdInForm";

  /** Member-edge form display text (parent relationship extension). */
  public static final String ATTR_DISPLAY_NAME_FOR_FORM = "DisplayNameForForm";

  /** Member-edge sequence number on indexed relationships. */
  public static final String ATTR_SEQ_NO = "SEQ_NO";

  /** LOINC PROPERTY concept attribute / property code. */
  public static final String ATTR_PROPERTY = "PROPERTY";

  /** External copyright notice on a LOINC concept. */
  public static final String ATTR_EXTERNAL_COPYRIGHT_NOTICE = "EXTERNAL_COPYRIGHT_NOTICE";

  private LoincConstants() {
    // utility class
  }
}
