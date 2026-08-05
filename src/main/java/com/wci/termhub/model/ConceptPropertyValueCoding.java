/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One FHIR CodeSystem concept {@code property} with {@code valueCoding} (code + optional
 * display). LOINC (and others) may repeat the same property {@code code}; this type preserves order
 * and duplicates without encoding them into {@link Concept#getAttributes()} keys.
 */
@JsonInclude(Include.NON_EMPTY)
@Schema(description = "FHIR valueCoding for a CodeSystem concept property")
public class ConceptPropertyValueCoding {

  /** FHIR property code (JSON {@code code}). */
  @Field(type = FieldType.Keyword)
  private String propertyCode;

  /** valueCoding.code. */
  @Field(type = FieldType.Keyword)
  private String valueCode;

  /** valueCoding.display, when present. */
  @Field(type = FieldType.Text)
  private String valueDisplay;

  /**
   * LOINC AnswerListLinkType extension (EXAMPLE, NORMATIVE, …) when present on the source property.
   */
  @Field(type = FieldType.Keyword)
  private String answerListLinkType;

  /**
   * LOINC ApplicableContext extension (e.g. panel code) when present on the source property.
   */
  @Field(type = FieldType.Keyword)
  private String applicableContext;

  /**
   * Instantiates an empty {@link ConceptPropertyValueCoding}.
   */
  public ConceptPropertyValueCoding() {
    // n/a
  }

  /**
   * @param propertyCode FHIR property code
   * @param valueCode valueCoding code
   * @param valueDisplay valueCoding display or null
   */
  public ConceptPropertyValueCoding(final String propertyCode, final String valueCode,
    final String valueDisplay) {
    this.propertyCode = propertyCode;
    this.valueCode = valueCode;
    this.valueDisplay = valueDisplay;
  }

  public String getPropertyCode() {
    return propertyCode;
  }

  public void setPropertyCode(final String propertyCode) {
    this.propertyCode = propertyCode;
  }

  public String getValueCode() {
    return valueCode;
  }

  public void setValueCode(final String valueCode) {
    this.valueCode = valueCode;
  }

  public String getValueDisplay() {
    return valueDisplay;
  }

  public void setValueDisplay(final String valueDisplay) {
    this.valueDisplay = valueDisplay;
  }

  public String getAnswerListLinkType() {
    return answerListLinkType;
  }

  public void setAnswerListLinkType(final String answerListLinkType) {
    this.answerListLinkType = answerListLinkType;
  }

  public String getApplicableContext() {
    return applicableContext;
  }

  public void setApplicableContext(final String applicableContext) {
    this.applicableContext = applicableContext;
  }
}
