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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hl7.fhir.r4.model.Questionnaire;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.util.QuestionnaireGetCache;

import ca.uhn.fhir.context.FhirVersionEnum;

/**
 * Unit tests for {@link QuestionnaireGetCache}.
 */
public class QuestionnaireGetCacheUnitTest {

  /**
   * Clear cache after each test.
   */
  @AfterEach
  public void tearDown() {
    QuestionnaireGetCache.clear();
  }

  /**
   * Put and get round-trip for R4 Questionnaire GET responses.
   */
  @Test
  public void testPutGetR4() {
    final String key = QuestionnaireGetCache.buildKey(FhirVersionEnum.R4, "2.78", "12345-6");
    assertFalse(QuestionnaireGetCache.containsKey(key));

    final Questionnaire questionnaire = new Questionnaire();
    questionnaire.setId("12345-6");
    questionnaire.setUrl("http://loinc.org/q/12345-6");
    QuestionnaireGetCache.putR4(key, questionnaire);

    assertTrue(QuestionnaireGetCache.containsKey(key));
    final Questionnaire cached = QuestionnaireGetCache.getR4(key);
    assertNotNull(cached);
    assertEquals("12345-6", cached.getIdElement().getIdPart());
  }

  /**
   * Clear removes cached entries.
   */
  @Test
  public void testClear() {
    final String key = QuestionnaireGetCache.buildKey(FhirVersionEnum.R4, "2.78", "99999-9");
    final Questionnaire questionnaire = new Questionnaire();
    questionnaire.setId("99999-9");
    QuestionnaireGetCache.putR4(key, questionnaire);
    assertTrue(QuestionnaireGetCache.containsKey(key));

    QuestionnaireGetCache.clear();
    assertFalse(QuestionnaireGetCache.containsKey(key));
    assertNull(QuestionnaireGetCache.getR4(key));
  }

  /**
   * R4 and R5 keys are distinct for the same id.
   */
  @Test
  public void testBuildKeyIncludesFhirVersion() {
    final String r4 = QuestionnaireGetCache.buildKey(FhirVersionEnum.R4, "2.78", "12345-6");
    final String r5 = QuestionnaireGetCache.buildKey(FhirVersionEnum.R5, "2.78", "12345-6");
    assertFalse(r4.equals(r5));
  }

  /**
   * LOINC version is part of the cache key.
   */
  @Test
  public void testBuildKeyIncludesLoincVersion() {
    final String v278 = QuestionnaireGetCache.buildKey(FhirVersionEnum.R4, "2.78", "12345-6");
    final String v281 = QuestionnaireGetCache.buildKey(FhirVersionEnum.R4, "2.81", "12345-6");
    assertFalse(v278.equals(v281));
  }

  /**
   * getOrLoadR4 builds once and serves concurrent waiters from cache / flight.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetOrLoadR4Coalesces() throws Exception {
    final String key = QuestionnaireGetCache.buildKey(FhirVersionEnum.R4, "2.78", "55555-5");
    final java.util.concurrent.atomic.AtomicInteger builds =
        new java.util.concurrent.atomic.AtomicInteger();
    final java.util.concurrent.CountDownLatch started =
        new java.util.concurrent.CountDownLatch(6);
    final java.util.concurrent.CountDownLatch release =
        new java.util.concurrent.CountDownLatch(1);
    final java.util.concurrent.ExecutorService pool =
        java.util.concurrent.Executors.newFixedThreadPool(6);
    try {
      final java.util.List<java.util.concurrent.Future<Questionnaire>> futures =
          new java.util.ArrayList<>();
      for (int i = 0; i < 6; i++) {
        futures.add(pool.submit(() -> {
          started.countDown();
          started.await(5, java.util.concurrent.TimeUnit.SECONDS);
          return QuestionnaireGetCache.getOrLoadR4(key, () -> {
            builds.incrementAndGet();
            release.await(5, java.util.concurrent.TimeUnit.SECONDS);
            final Questionnaire questionnaire = new Questionnaire();
            questionnaire.setId("55555-5");
            return questionnaire;
          });
        }));
      }
      assertTrue(started.await(5, java.util.concurrent.TimeUnit.SECONDS));
      Thread.sleep(100);
      release.countDown();
      for (final java.util.concurrent.Future<Questionnaire> future : futures) {
        final Questionnaire q = future.get(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(q);
        assertEquals("55555-5", q.getIdElement().getIdPart());
      }
      assertEquals(1, builds.get());
      assertTrue(QuestionnaireGetCache.containsKey(key));
    } finally {
      pool.shutdownNow();
    }
  }
}
