/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r4;

import java.util.Comparator;
import java.util.List;

import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemAnswerOptionComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.model.Concept;

/**
 * Comparator for sorting FHIR Questionnaire answer options by LOINC
 * SequenceNumber. This ensures proper ordering of answer options according to
 * LOINC specifications.
 */
public class AnswerOptionSequenceComparator
    implements Comparator<QuestionnaireItemAnswerOptionComponent> {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(AnswerOptionSequenceComparator.class);

  /**
   * Compares two answer options based on their SequenceNumber attribute. Falls
   * back to LOINC code comparison if SequenceNumber is not available.
   *
   * @param option1 the first answer option
   * @param option2 the second answer option
   * @return negative if option1 < option2, positive if option1 > option2, 0 if
   *         equal
   */
  @Override
  public int compare(final QuestionnaireItemAnswerOptionComponent option1,
    final QuestionnaireItemAnswerOptionComponent option2) {

    // Safety check - ensure we have Concept objects stored in userData
    final Object storedConcept1 = option1.getUserData("concept");
    final Object storedConcept2 = option2.getUserData("concept");

    if (!(storedConcept1 instanceof Concept) || !(storedConcept2 instanceof Concept)) {
      // Log warning and fall back to code comparison
      logger.warn("Expected Concept objects for sorting, got: {} and {}",
          storedConcept1 != null ? storedConcept1.getClass().getSimpleName() : "null",
          storedConcept2 != null ? storedConcept2.getClass().getSimpleName() : "null");

      return compareByCode(option1, option2);
    }

    final Concept concept1 = (Concept) storedConcept1;
    final Concept concept2 = (Concept) storedConcept2;

    // Try to compare by SequenceNumber first
    final int sequenceComparison = compareBySequenceNumber(concept1, concept2);
    if (sequenceComparison != 0) {
      return sequenceComparison;
    }

    // Fallback to LOINC code comparison if SequenceNumber comparison didn't
    // resolve
    return compareByCode(option1, option2);
  }

  /**
   * Compares two concepts by their SequenceNumber attribute.
   *
   * @param concept1 the first concept
   * @param concept2 the second concept
   * @return comparison result, or 0 if SequenceNumber comparison is not
   *         possible
   */
  private int compareBySequenceNumber(final Concept concept1, final Concept concept2) {
    if (concept1.getAttributes() == null || concept2.getAttributes() == null) {
      return 0;
    }

    final String seqNum1 = concept1.getAttributes().get("SequenceNumber");
    final String seqNum2 = concept2.getAttributes().get("SequenceNumber");

    if (seqNum1 == null || seqNum2 == null) {
      return 0;
    }

    try {
      final int num1 = Integer.parseInt(seqNum1);
      final int num2 = Integer.parseInt(seqNum2);
      return Integer.compare(num1, num2);
    } catch (final NumberFormatException e) {
      // Fallback to string comparison if parsing fails
      logger.debug("Failed to parse SequenceNumber as integer, using string comparison: {} vs {}",
          seqNum1, seqNum2);
      return seqNum1.compareTo(seqNum2);
    }
  }

  /**
   * Compares two answer options by their LOINC code.
   *
   * @param option1 the first answer option
   * @param option2 the second answer option
   * @return comparison result
   */
  private int compareByCode(final QuestionnaireItemAnswerOptionComponent option1,
    final QuestionnaireItemAnswerOptionComponent option2) {
    final String code1 = option1.getValueCoding().getCode();
    final String code2 = option2.getValueCoding().getCode();
    return code1.compareTo(code2);
  }

  /**
   * Sorts a list of answer options using this comparator.
   *
   * @param answerOptions the list to sort
   */
  public static void sortAnswerOptions(
    final List<QuestionnaireItemAnswerOptionComponent> answerOptions) {
    if (answerOptions != null && answerOptions.size() > 1) {
      answerOptions.sort(new AnswerOptionSequenceComparator());
    }
  }
}
