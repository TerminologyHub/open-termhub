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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.Term;

/**
 * Unit tests for {@link FhirUtility#preferredDisplay} / matchPreferredLanguage.
 */
public class FhirUtilityPreferredDisplayUnitTest {

  /** English long common name (57604-1). */
  private static final String EN_DISPLAY =
      "6-Alpha hydroxytetrahydro-11-Deoxycortisol [Mass/time] in 24 hour Urine --2 days post dose dexamethasone";

  /** Spanish FSN (es-ES), preferred=false on LOINC terms. */
  private static final String ES_FSN =
      "6-Alfa hidroxitetrahidro-11-Deoxicortisol^2D pos dosis de dexametasona:Indice de masa:24 horas:Orina:Qn:";

  /**
   * es-ES FSN is used even when localeMap value is false.
   */
  @Test
  public void testSpanishFsnWhenNotPreferred() {
    final Concept concept = concept57604();
    assertEquals(ES_FSN, FhirUtility.preferredDisplay(concept, languages("es-ES")));
  }

  /**
   * displayLanguage=es matches es-ES.
   */
  @Test
  public void testEsPrefixMatchesEsEs() {
    final Concept concept = concept57604();
    assertEquals(ES_FSN, FhirUtility.preferredDisplay(concept, languages("es")));
  }

  /**
   * No language param keeps concept name.
   */
  @Test
  public void testNullLanguagesUsesConceptName() {
    final Concept concept = concept57604();
    assertEquals(EN_DISPLAY, FhirUtility.preferredDisplay(concept, null));
    assertEquals(EN_DISPLAY, FhirUtility.preferredDisplay(concept, Collections.emptySet()));
  }

  /**
   * Unrelated language falls back to concept name.
   */
  @Test
  public void testMissingLanguageFallsBackToName() {
    final Concept concept = concept57604();
    assertEquals(EN_DISPLAY, FhirUtility.preferredDisplay(concept, languages("de")));
  }

  /**
   * Preferred true wins over a non-preferred same-language term.
   */
  @Test
  public void testPreferredTrueWins() {
    final Concept concept = new Concept();
    concept.setName("concept-name");
    final List<Term> terms = new ArrayList<>();
    terms.add(term("non-pref", "es-ES", false));
    terms.add(term("pref", "es-ES", true));
    concept.setTerms(terms);
    assertEquals("pref", FhirUtility.preferredDisplay(concept, languages("es-ES")));
  }

  /**
   * Null term locale does not match.
   */
  @Test
  public void testNullTermNoMatch() {
    assertNull(FhirUtility.matchPreferredLanguage(null, languages("es")));
  }

  /**
   * Languages helper.
   *
   * @param values the values
   * @return set
   */
  private static Set<String> languages(final String... values) {
    final Set<String> set = new LinkedHashSet<>();
    for (final String value : values) {
      set.add(value);
    }
    return set;
  }

  /**
   * 57604-1 shaped concept: English LCN preferred, Spanish FSN not preferred.
   *
   * @return concept
   */
  private static Concept concept57604() {
    final Concept concept = new Concept();
    concept.setCode("57604-1");
    concept.setName(EN_DISPLAY);
    final List<Term> terms = new ArrayList<>();
    terms.add(term(EN_DISPLAY, "en-US", true));
    terms.add(term(ES_FSN, "es-ES", false));
    concept.setTerms(terms);
    return concept;
  }

  /**
   * Term with a single locale flag.
   *
   * @param name the name
   * @param lang the language tag
   * @param preferred preferred flag
   * @return term
   */
  private static Term term(final String name, final String lang, final boolean preferred) {
    final Term term = new Term();
    term.setName(name);
    term.getLocaleMap().put(lang, preferred);
    return term;
  }
}
