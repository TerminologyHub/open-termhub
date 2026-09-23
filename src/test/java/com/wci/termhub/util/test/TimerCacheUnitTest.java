/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.wci.termhub.util.PropertyUtility;
import com.wci.termhub.util.TimerCache;

/**
 * Unit tests for {@link TimerCache}.
 */
public class TimerCacheUnitTest {

  /**
   * Reset read-only so expiry tests are not order-dependent.
   */
  @AfterEach
  public void resetReadOnly() {
    PropertyUtility.setProperty("read.only", "false");
  }

  /**
   * Put and get round-trip.
   */
  @Test
  public void testPutGet() {
    final TimerCache<String> cache = new TimerCache<>(10, 60_000);
    cache.put("a", "one");
    assertEquals("one", cache.get("a"));
  }

  /**
   * Null put removes; does not cache failure/null.
   */
  @Test
  public void testPutNullRemoves() {
    final TimerCache<String> cache = new TimerCache<>(10, 60_000);
    cache.put("a", "one");
    cache.put("a", null);
    assertNull(cache.get("a"));
  }

  /**
   * Timeout of 0 never expires.
   *
   * @throws Exception the exception
   */
  @Test
  public void testZeroTimeoutNeverExpires() throws Exception {
    final TimerCache<String> cache = new TimerCache<>(10, 0);
    cache.put("a", "one");
    Thread.sleep(5);
    assertEquals("one", cache.get("a"));
  }

  /**
   * Read-only mode does not expire entries.
   *
   * @throws Exception the exception
   */
  @Test
  public void testReadOnlyNeverExpires() throws Exception {
    PropertyUtility.setProperty("read.only", "true");
    final TimerCache<String> cache = new TimerCache<>(10, 1);
    cache.put("a", "one");
    Thread.sleep(5);
    assertEquals("one", cache.get("a"));
  }

  /**
   * Expired entry is a miss and is cleaned.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExpiredIsMiss() throws Exception {
    final TimerCache<String> cache = new TimerCache<>(10, 1);
    cache.put("a", "one");
    Thread.sleep(5);
    assertNull(cache.get("a"));
    // second get still safe (no sticky poison)
    assertNull(cache.get("a"));
  }

  /**
   * Desynced timeMap must not NPE; subsequent gets stay miss (no sticky 500).
   *
   * @throws Exception the exception
   */
  @Test
  public void testDesyncedTimeMapFailOpen() throws Exception {
    final TimerCache<String> cache = new TimerCache<>(10, 60_000);
    cache.put("poison", "value");
    // Simulate race: map has key, timeMap does not
    final Field timeMapField = TimerCache.class.getDeclaredField("timeMap");
    timeMapField.setAccessible(true);
    @SuppressWarnings("unchecked")
    final Map<String, Long> timeMap = (Map<String, Long>) timeMapField.get(cache);
    timeMap.remove("poison");

    assertNull(cache.get("poison"));
    assertNull(cache.get("poison"));
    cache.put("poison", "recovered");
    assertEquals("recovered", cache.get("poison"));
  }

  /**
   * Concurrent get/put must not throw.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConcurrentAccessNoThrow() throws Exception {
    final TimerCache<String> cache = new TimerCache<>(100, 60_000);
    final int threads = 16;
    final int ops = 500;
    final ExecutorService pool = Executors.newFixedThreadPool(threads);
    final CountDownLatch start = new CountDownLatch(1);
    final AtomicInteger errors = new AtomicInteger();
    final List<Future<?>> futures = new ArrayList<>();
    for (int t = 0; t < threads; t++) {
      final int threadId = t;
      futures.add(pool.submit(() -> {
        try {
          start.await();
          for (int i = 0; i < ops; i++) {
            final String key = "k" + ((threadId + i) % 50);
            cache.put(key, "v" + i);
            cache.get(key);
            cache.get("missing-" + i);
          }
        } catch (final Exception e) {
          errors.incrementAndGet();
        }
      }));
    }
    start.countDown();
    for (final Future<?> f : futures) {
      f.get(30, TimeUnit.SECONDS);
    }
    pool.shutdownNow();
    assertEquals(0, errors.get());
    assertDoesNotThrow(cache::check);
  }
}
