/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.util.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.wci.termhub.fhir.r4.QuestionnaireProviderR4;
import com.wci.termhub.fhir.r4.ValueSetProviderR4;
import com.wci.termhub.fhir.r5.QuestionnaireProviderR5;
import com.wci.termhub.fhir.r5.ValueSetProviderR5;
import com.wci.termhub.fhir.util.FhirSearchCacheWarmer;
import com.wci.termhub.fhir.util.LoincValueSetHelper;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * Unit tests for {@link FhirSearchCacheWarmer}.
 */
public class FhirSearchCacheWarmerUnitTest {

  /** The warmer. */
  private FhirSearchCacheWarmer warmer;

  /** The mock search service. */
  @Mock
  private EntityRepositoryService searchService;

  /** The mock R4 value set provider. */
  @Mock
  private ValueSetProviderR4 valueSetProviderR4;

  /** The mock R5 value set provider. */
  @Mock
  private ValueSetProviderR5 valueSetProviderR5;

  /** The mock R4 questionnaire provider. */
  @Mock
  private QuestionnaireProviderR4 questionnaireProviderR4;

  /** The mock R5 questionnaire provider. */
  @Mock
  private QuestionnaireProviderR5 questionnaireProviderR5;

  /** The mock LOINC helper. */
  @Mock
  private LoincValueSetHelper loincValueSetHelper;

  /**
   * Sets up mocks.
   */
  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    warmer = new FhirSearchCacheWarmer();
    ReflectionTestUtils.setField(warmer, "searchService", searchService);
    ReflectionTestUtils.setField(warmer, "valueSetProviderR4", valueSetProviderR4);
    ReflectionTestUtils.setField(warmer, "valueSetProviderR5", valueSetProviderR5);
    ReflectionTestUtils.setField(warmer, "questionnaireProviderR4", questionnaireProviderR4);
    ReflectionTestUtils.setField(warmer, "questionnaireProviderR5", questionnaireProviderR5);
    ReflectionTestUtils.setField(warmer, "loincValueSetHelper", loincValueSetHelper);
  }

  /**
   * Regenstrief off skips ValueSet warmup.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSkipValueSetWhenNotRegenstrief() throws Exception {
    when(loincValueSetHelper.isEnabled()).thenReturn(false);
    warmer.warmValueSets();
    verify(valueSetProviderR4, never()).findPossibleValueSets(false, null, null, null);
    verify(valueSetProviderR5, never()).findPossibleValueSets(false, null, null, null);
  }

  /**
   * Warms R4 and R5 ValueSet unfiltered search then latest LOINC version.
   *
   * @throws Exception the exception
   */
  @Test
  public void testWarmValueSetsR4AndR5() throws Exception {
    when(loincValueSetHelper.isEnabled()).thenReturn(true);
    final Terminology latest = new Terminology();
    latest.setVersion("2.78");
    when(loincValueSetHelper.findLoincTerminology(searchService)).thenReturn(latest);
    when(valueSetProviderR4.findPossibleValueSets(eq(false), isNull(), isNull(), any()))
        .thenReturn(List.of());
    when(valueSetProviderR5.findPossibleValueSets(eq(false), isNull(), isNull(), any()))
        .thenReturn(List.of());

    warmer.warmValueSets();

    verify(valueSetProviderR4).findPossibleValueSets(false, null, null, null);
    verify(valueSetProviderR5).findPossibleValueSets(false, null, null, null);
    verify(valueSetProviderR4).findPossibleValueSets(eq(false), isNull(), isNull(),
        any(org.hl7.fhir.r4.model.StringType.class));
    verify(valueSetProviderR5).findPossibleValueSets(eq(false), isNull(), isNull(),
        any(org.hl7.fhir.r5.model.StringType.class));
  }

  /**
   * Questionnaire warmup runs when LOINC is loaded even if not Regenstrief.
   *
   * @throws Exception the exception
   */
  @Test
  public void testWarmQuestionnairesWhenLoincPresent() throws Exception {
    when(loincValueSetHelper.isEnabled()).thenReturn(false);
    final Terminology latest = new Terminology();
    latest.setVersion("2.78");
    when(loincValueSetHelper.findLoincTerminology(searchService)).thenReturn(latest);
    when(questionnaireProviderR4.findPossibleQuestionnaires(eq(false), isNull(), isNull(), any()))
        .thenReturn(List.of());
    when(questionnaireProviderR5.findPossibleQuestionnaires(eq(false), isNull(), isNull(), any()))
        .thenReturn(List.of());

    warmer.warmCaches();

    verify(valueSetProviderR4, never()).findPossibleValueSets(false, null, null, null);
    verify(questionnaireProviderR4).findPossibleQuestionnaires(false, null, null, null);
    verify(questionnaireProviderR5).findPossibleQuestionnaires(false, null, null, null);
    verify(questionnaireProviderR4).findPossibleQuestionnaires(eq(false), isNull(), isNull(),
        any(org.hl7.fhir.r4.model.StringType.class));
    verify(questionnaireProviderR5).findPossibleQuestionnaires(eq(false), isNull(), isNull(),
        any(org.hl7.fhir.r5.model.StringType.class));
  }

  /**
   * Missing LOINC skips Questionnaire warmup.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSkipQuestionnairesWhenNoLoinc() throws Exception {
    when(loincValueSetHelper.findLoincTerminology(searchService)).thenReturn(null);
    warmer.warmQuestionnaires();
    verify(questionnaireProviderR4, never()).findPossibleQuestionnaires(false, null, null, null);
    verify(questionnaireProviderR5, never()).findPossibleQuestionnaires(false, null, null, null);
  }

  /**
   * ValueSet failure does not skip Questionnaire warmup.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValueSetFailureDoesNotSkipQuestionnaire() throws Exception {
    when(loincValueSetHelper.isEnabled()).thenReturn(true);
    final Terminology latest = new Terminology();
    latest.setVersion("2.78");
    when(loincValueSetHelper.findLoincTerminology(searchService)).thenReturn(latest);
    when(valueSetProviderR4.findPossibleValueSets(false, null, null, null))
        .thenThrow(new Exception("lucene down"));
    when(questionnaireProviderR4.findPossibleQuestionnaires(eq(false), isNull(), isNull(), any()))
        .thenReturn(List.of());
    when(questionnaireProviderR5.findPossibleQuestionnaires(eq(false), isNull(), isNull(), any()))
        .thenReturn(List.of());

    assertDoesNotThrow(() -> warmer.warmCaches());
    verify(questionnaireProviderR4).findPossibleQuestionnaires(false, null, null, null);
  }

}
