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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.wci.termhub.util.SingleFlight;

/**
 * Unit tests for {@link SingleFlight}.
 */
public class SingleFlightUnitTest {

  /**
   * Concurrent callers for the same key run the supplier once.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCoalescesConcurrentCalls() throws Exception {
    final SingleFlight flight = new SingleFlight();
    final AtomicInteger builds = new AtomicInteger();
    final CountDownLatch started = new CountDownLatch(8);
    final CountDownLatch release = new CountDownLatch(1);
    final ExecutorService pool = Executors.newFixedThreadPool(8);
    try {
      final List<Future<String>> futures = new ArrayList<>();
      for (int i = 0; i < 8; i++) {
        futures.add(pool.submit(() -> {
          started.countDown();
          started.await(5, TimeUnit.SECONDS);
          return flight.execute("k1", () -> {
            builds.incrementAndGet();
            release.await(5, TimeUnit.SECONDS);
            return "value";
          });
        }));
      }
      assertEquals(true, started.await(5, TimeUnit.SECONDS));
      // Give workers a moment to enter execute / queue behind the winner.
      Thread.sleep(100);
      release.countDown();
      for (final Future<String> future : futures) {
        assertEquals("value", future.get(5, TimeUnit.SECONDS));
      }
      assertEquals(1, builds.get());
    } finally {
      pool.shutdownNow();
    }
  }

  /**
   * Failures are shared with waiters.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSharesExceptions() throws Exception {
    final SingleFlight flight = new SingleFlight();
    final AtomicInteger builds = new AtomicInteger();
    final CountDownLatch started = new CountDownLatch(4);
    final CountDownLatch release = new CountDownLatch(1);
    final ExecutorService pool = Executors.newFixedThreadPool(4);
    try {
      final List<Future<?>> futures = new ArrayList<>();
      for (int i = 0; i < 4; i++) {
        futures.add(pool.submit(() -> {
          started.countDown();
          started.await(5, TimeUnit.SECONDS);
          flight.execute("k2", () -> {
            builds.incrementAndGet();
            release.await(5, TimeUnit.SECONDS);
            throw new IllegalStateException("boom");
          });
          return null;
        }));
      }
      assertEquals(true, started.await(5, TimeUnit.SECONDS));
      Thread.sleep(100);
      release.countDown();
      for (final Future<?> future : futures) {
        final Exception ex = assertThrows(Exception.class, () -> {
          try {
            future.get(5, TimeUnit.SECONDS);
          } catch (final java.util.concurrent.ExecutionException e) {
            throw (Exception) e.getCause();
          }
        });
        assertEquals("boom", ex.getMessage());
      }
      assertEquals(1, builds.get());
    } finally {
      pool.shutdownNow();
    }
  }
}
