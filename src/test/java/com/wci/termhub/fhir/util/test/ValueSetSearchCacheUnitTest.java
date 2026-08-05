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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.util.ValueSetSearchCache;

import ca.uhn.fhir.context.FhirVersionEnum;

/**
 * Unit tests for {@link ValueSetSearchCache}.
 */
public class ValueSetSearchCacheUnitTest {

  /**
   * Clear cache after each test.
   */
  @AfterEach
  public void tearDown() {
    ValueSetSearchCache.clear();
  }

  /**
   * R4 and R5 keys are distinct for the same terminology.
   */
  @Test
  public void testBuildKeyIncludesFhirVersionAndMeta() {
    final String r4 = ValueSetSearchCache.buildKey(FhirVersionEnum.R4, "loinc-1", false);
    final String r5 = ValueSetSearchCache.buildKey(FhirVersionEnum.R5, "loinc-1", false);
    final String r4Meta = ValueSetSearchCache.buildKey(FhirVersionEnum.R4, "loinc-1", true);
    assertFalse(r4.equals(r5));
    assertFalse(r4.equals(r4Meta));
  }

  /**
   * Clear removes cached entries.
   *
   * @throws Exception the exception
   */
  @Test
  public void testClear() throws Exception {
    final String key = ValueSetSearchCache.buildKey(FhirVersionEnum.R4, "clear-test", false);
    ValueSetSearchCache.getOrLoadR4(key, () -> {
      final ValueSet vs = new ValueSet();
      vs.setId("LG1");
      return List.of(vs);
    });
    assertTrue(ValueSetSearchCache.containsKey(key));

    ValueSetSearchCache.clear();
    assertFalse(ValueSetSearchCache.containsKey(key));
  }

  /**
   * getOrLoadR4 builds once and serves concurrent waiters from cache / flight.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetOrLoadR4Coalesces() throws Exception {
    final String key = ValueSetSearchCache.buildKey(FhirVersionEnum.R4, "coalesce-test", false);
    final AtomicInteger builds = new AtomicInteger();
    final CountDownLatch started = new CountDownLatch(6);
    final CountDownLatch release = new CountDownLatch(1);
    final ExecutorService pool = Executors.newFixedThreadPool(6);
    try {
      final List<Future<List<ValueSet>>> futures = new ArrayList<>();
      for (int i = 0; i < 6; i++) {
        futures.add(pool.submit(() -> {
          started.countDown();
          started.await(5, TimeUnit.SECONDS);
          return ValueSetSearchCache.getOrLoadR4(key, () -> {
            builds.incrementAndGet();
            release.await(5, TimeUnit.SECONDS);
            final ValueSet vs = new ValueSet();
            vs.setId("LG-coalesce");
            vs.setUrl("http://loinc.org/vs/LG-coalesce");
            return List.of(vs);
          });
        }));
      }
      assertTrue(started.await(5, TimeUnit.SECONDS));
      Thread.sleep(100);
      release.countDown();
      for (final Future<List<ValueSet>> future : futures) {
        final List<ValueSet> list = future.get(5, TimeUnit.SECONDS);
        assertEquals(1, list.size());
        assertEquals("LG-coalesce", list.get(0).getIdElement().getIdPart());
      }
      assertEquals(1, builds.get());
      assertTrue(ValueSetSearchCache.containsKey(key));
    } finally {
      pool.shutdownNow();
    }
  }
}
