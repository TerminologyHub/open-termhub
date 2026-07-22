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

import java.util.Set;

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

  /** LOINC CLASS part code for HEDIS panels (excluded from FHIR Questionnaire). */
  public static final String LOINC_EXCLUDED_PANEL_CLASS_CODE = "LP71800-4";

  /** LOINC CLASS name for HEDIS panels (excluded from FHIR Questionnaire). */
  public static final String LOINC_EXCLUDED_PANEL_CLASS_NAME = "PANEL.HEDIS";

  /** LOINC CLASSTYPE concept attribute (1=Laboratory, 2=Clinical, 3=Claims, 4=Surveys). */
  public static final String ATTR_CLASSTYPE = "CLASSTYPE";

  /** URL prefix for LOINC value sets (query form). */
  public static final String LOINC_VS_URL_PREFIX = "http://loinc.org?fhir_vs";

  /** Path prefix for LOINC value set URLs (e.g. http://loinc.org/vs/LG51018-6-2.72). */
  public static final String LOINC_VS_PATH_PREFIX = "http://loinc.org/vs/";

  /** Path prefix for LOINC value set URLs (HTTPS). */
  public static final String LOINC_VS_PATH_PREFIX_HTTPS = "https://loinc.org/vs/";

  /** Concept attribute for answer list ID (LOINC). */
  public static final String ATTR_ANSWER_LIST_ID = "ANSWER_LIST_ID";

  /** LOINC short name concept attribute (official LOINC column). */
  public static final String ATTR_SHORTNAME = "SHORTNAME";

  /** LOINC survey question text (form label when DisplayNameForForm is absent). */
  public static final String ATTR_SURVEY_QUEST_TEXT = "SURVEY_QUEST_TEXT";

  /** LOINC long common name concept attribute. */
  public static final String ATTR_LONG_COMMON_NAME = "LONG_COMMON_NAME";

  /** LOINC long common name designation type. */
  public static final String TERM_TYPE_LONG_COMMON_NAME = "LONG_COMMON_NAME";

  /** LOINC short name term/designation type. */
  public static final String TERM_TYPE_SHORTNAME = "SHORTNAME";

  /** LOINC component part property code on indexed concepts. */
  public static final String ATTR_COMPONENT = "COMPONENT";

  /** LOINC fully specified name designation type. */
  public static final String TERM_TYPE_FULLY_SPECIFIED_NAME = "FullySpecifiedName";

  /** Panel membership relationship (full LOINC loads). */
  public static final String LOINC_REL_PANEL_MEMBER = "member";

  /** Panel membership relationship (sandbox / legacy). */
  public static final String LOINC_REL_HAS_MEMBER = "has_member";

  /** LoincAnswerListLink row indexed as concept → answer-list association. */
  public static final String LOINC_REL_ANSWER_LIST_LINK = "answer-list";

  /** PanelsAndForms ObservationRequiredInPanel (R/O). */
  public static final String ATTR_OBSERVATION_REQUIRED_IN_PANEL = "ObservationRequiredInPanel";

  /** PanelsAndForms AnswerListIdOverride on a form row. */
  public static final String ATTR_ANSWER_LIST_ID_OVERRIDE = "AnswerListIdOverride";

  /** LL concept flag that its answers are externally defined ({@code Y}/{@code N}). */
  public static final String ATTR_ANSWER_EXT_DEFINED = "AnswerExtDefinedYNListOID";

  /** LoincAnswerListLink AnswerListLinkType (NORMATIVE, EXAMPLE, …). */
  public static final String ATTR_ANSWER_LIST_LINK_TYPE = "AnswerListLinkType";

  /** LoincAnswerListLink ApplicableContext questionnaire LOINC. */
  public static final String ATTR_APPLICABLE_CONTEXT = "ApplicableContext";

  /** Loader key for QuestionCardinality (PanelsAndForms column). */
  public static final String ATTR_QUESTION_CARDINALITY = "QuestionCardinality";

  /** PanelsAndForms AnswerCardinality on a form/member edge (e.g. {@code 0..4}). */
  public static final String ATTR_ANSWER_CARDINALITY = "AnswerCardinality";

  /** Member-edge form link id (parent relationship extension). */
  public static final String ATTR_REL_ID = "ID";

  /** Member-edge form prefix (parent relationship extension). */
  public static final String ATTR_OBSERVATION_ID_IN_FORM = "ObservationIdInForm";

  /** Member-edge form display text (parent relationship extension). */
  public static final String ATTR_DISPLAY_NAME_FOR_FORM = "DisplayNameForForm";

  /** Member-edge sequence number on indexed relationships (LOINC export: SEQUENCE). */
  public static final String ATTR_SEQUENCE = "SEQUENCE";

  /** Parent form row {@code ID} on a panel member edge (PanelsAndForms ParentID). */
  public static final String ATTR_PARENT_FORM_LINK_ID = "ParentFormLinkId";

  /** Panel member cardinality within a form (e.g. {@code 1..7}). */
  public static final String ATTR_CARDINALITY = "Cardinality";

  /** LOINC scale type concept attribute. */
  public static final String ATTR_SCALE_TYP = "SCALE_TYP";

  /** LOINC PROPERTY concept attribute / property code. */
  public static final String ATTR_PROPERTY = "PROPERTY";

  /**
   * Uppercase LOINC property codes that duplicate lowercase {@code valueCoding} axes in the same
   * CodeSystem (legacy string row vs part code row).
   */
  public static final Set<String> LOINC_UPPERCASE_PROPERTY_KEYS =
      Set.of("CLASS", "COMPONENT", "METHOD_TYP", "PROPERTY", "SCALE_TYP", "SYSTEM", "TIME_ASPCT");

  /** FHIR standard concept status property (valueCode active/inactive). */
  public static final String FHIR_STATUS = "status";

  /** LOINC display status property (valueString, e.g. Active). */
  public static final String ATTR_STATUS = "STATUS";

  /** External copyright notice on a LOINC concept. */
  public static final String ATTR_EXTERNAL_COPYRIGHT_NOTICE = "EXTERNAL_COPYRIGHT_NOTICE";

  private LoincConstants() {
    // utility class
  }
}
