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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.wci.termhub.fhir.r4.QuestionnaireProviderR4;
import com.wci.termhub.fhir.r4.ValueSetProviderR4;
import com.wci.termhub.fhir.r5.QuestionnaireProviderR5;
import com.wci.termhub.fhir.r5.ValueSetProviderR5;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * Warms FHIR search caches after startup so the first client is not the one that pays for the
 * Lucene listing load. Invokes the same provider methods as unfiltered and version-filtered search.
 */
@Service
@Profile("!test")
public class FhirSearchCacheWarmer {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(FhirSearchCacheWarmer.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The R4 value set provider. */
  @Autowired
  private ValueSetProviderR4 valueSetProviderR4;

  /** The R5 value set provider. */
  @Autowired
  private ValueSetProviderR5 valueSetProviderR5;

  /** The R4 questionnaire provider. */
  @Autowired
  private QuestionnaireProviderR4 questionnaireProviderR4;

  /** The R5 questionnaire provider. */
  @Autowired
  private QuestionnaireProviderR5 questionnaireProviderR5;

  /** The LOINC helper. */
  @Autowired
  private LoincValueSetHelper loincValueSetHelper;

  /**
   * Starts FHIR search cache warmup after the application is ready.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    final Thread thread = new Thread(this::warmCaches, "fhir-search-cache-warmup");
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Loads ValueSet and Questionnaire search shells for unfiltered listing and the latest LOINC
   * version.
   */
  public void warmCaches() {
    logger.info("FHIR search cache warmup started");
    final long start = System.currentTimeMillis();
    warmValueSets();
    warmQuestionnaires();
    logger.info("FHIR search cache warmup completed in {} ms", System.currentTimeMillis() - start);
  }

  /**
   * Warms R4 and R5 ValueSet search. No-op unless Regenstrief mode.
   */
  public void warmValueSets() {
    if (!loincValueSetHelper.isEnabled()) {
      logger.info("ValueSet R4/R5 cache warmup skipped - not regenstrief mode");
      return;
    }
    warm("ValueSet", "R4", null, () -> valueSetProviderR4.findPossibleValueSets(false, null, null,
        null));
    warm("ValueSet", "R5", null, () -> valueSetProviderR5.findPossibleValueSets(false, null, null,
        null));
    final Terminology latest = loincValueSetHelper.findLoincTerminology(searchService);
    final String version = versionOf(latest);
    if (version == null) {
      return;
    }
    warm("ValueSet", "R4", version, () -> valueSetProviderR4.findPossibleValueSets(false, null,
        null, new org.hl7.fhir.r4.model.StringType(version)));
    warm("ValueSet", "R5", version, () -> valueSetProviderR5.findPossibleValueSets(false, null,
        null, new org.hl7.fhir.r5.model.StringType(version)));
  }

  /**
   * Warms R4 and R5 Questionnaire search. No-op when LOINC is not loaded.
   */
  public void warmQuestionnaires() {
    final Terminology latest = loincValueSetHelper.findLoincTerminology(searchService);
    if (latest == null) {
      logger.info("Questionnaire R4/R5 cache warmup skipped - LOINC not loaded");
      return;
    }
    warm("Questionnaire", "R4", null,
        () -> questionnaireProviderR4.findPossibleQuestionnaires(false, null, null, null));
    warm("Questionnaire", "R5", null,
        () -> questionnaireProviderR5.findPossibleQuestionnaires(false, null, null, null));
    final String version = versionOf(latest);
    if (version == null) {
      return;
    }
    warm("Questionnaire", "R4", version,
        () -> questionnaireProviderR4.findPossibleQuestionnaires(false, null, null,
            new org.hl7.fhir.r4.model.StringType(version)));
    warm("Questionnaire", "R5", version,
        () -> questionnaireProviderR5.findPossibleQuestionnaires(false, null, null,
            new org.hl7.fhir.r5.model.StringType(version)));
  }

  /**
   * Runs one warmup call and logs start/complete/fail with FHIR version and optional content
   * version.
   *
   * @param resource ValueSet or Questionnaire
   * @param fhirVersion R4 or R5
   * @param version LOINC version, or null for unfiltered
   * @param task the provider call
   */
  private void warm(final String resource, final String fhirVersion, final String version,
    final WarmTask task) {
    final String label = label(resource, fhirVersion, version);
    logger.info("{} started", label);
    final long start = System.currentTimeMillis();
    try {
      task.run();
      logger.info("{} completed in {} ms", label, System.currentTimeMillis() - start);
    } catch (final Exception e) {
      logger.warn("{} failed after {} ms", label, System.currentTimeMillis() - start, e);
    }
  }

  /**
   * Builds a consistent warmup log prefix.
   *
   * @param resource ValueSet or Questionnaire
   * @param fhirVersion R4 or R5
   * @param version LOINC version, or null
   * @return log label
   */
  private static String label(final String resource, final String fhirVersion,
    final String version) {
    if (version == null) {
      return resource + " " + fhirVersion + " cache warmup";
    }
    return resource + " " + fhirVersion + " cache warmup version=" + version;
  }

  /**
   * Returns the terminology version, or null if missing.
   *
   * @param terminology the terminology
   * @return version or null
   */
  private static String versionOf(final Terminology terminology) {
    if (terminology == null || terminology.getVersion() == null
        || terminology.getVersion().isEmpty()) {
      return null;
    }
    return terminology.getVersion();
  }

  /**
   * Warmup work that may throw.
   */
  @FunctionalInterface
  private interface WarmTask {
    /**
     * Run.
     *
     * @throws Exception the exception
     */
    void run() throws Exception;
  }

}
