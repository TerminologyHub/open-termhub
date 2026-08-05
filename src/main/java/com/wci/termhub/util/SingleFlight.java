/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coalesces concurrent computations for the same key into a single execution
 * (single-flight). Waiters share the winner's result or exception. Used to
 * prevent cache stampedes.
 */
public final class SingleFlight {

  /**
   * Supplier that may throw checked exceptions.
   *
   * @param <T> result type
   */
  @FunctionalInterface
  public interface ThrowingSupplier<T> {

    /**
     * Gets the.
     *
     * @return the value
     * @throws Exception on failure
     */
    T get() throws Exception;
  }

  /** In-flight computations by key. */
  private final ConcurrentHashMap<String, CompletableFuture<?>> flights = new ConcurrentHashMap<>();

  /**
   * Runs {@code supplier} once per key while concurrent callers wait for the
   * same result.
   *
   * @param <T> result type
   * @param key coalescing key
   * @param supplier the computation
   * @return supplier result
   * @throws Exception if the supplier fails (rethrown to all waiters)
   */
  @SuppressWarnings("unchecked")
  public <T> T execute(final String key, final ThrowingSupplier<T> supplier) throws Exception {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(supplier, "supplier");

    final CompletableFuture<T> created = new CompletableFuture<>();
    final CompletableFuture<?> existing = flights.putIfAbsent(key, created);
    if (existing != null) {
      try {
        return (T) existing.join();
      } catch (final CompletionException e) {
        final Throwable cause = e.getCause() != null ? e.getCause() : e;
        if (cause instanceof final Exception ex) {
          throw ex;
        }
        if (cause instanceof final Error err) {
          throw err;
        }
        throw e;
      }
    }

    try {
      final T value = supplier.get();
      created.complete(value);
      return value;
    } catch (final Exception e) {
      created.completeExceptionally(e);
      throw e;
    } catch (final Error e) {
      created.completeExceptionally(e);
      throw e;
    } finally {
      flights.remove(key, created);
    }
  }

  /**
   * Drops in-flight entries (e.g. after cache clear). Running suppliers still
   * complete.
   */
  public void clear() {
    flights.clear();
  }
}
