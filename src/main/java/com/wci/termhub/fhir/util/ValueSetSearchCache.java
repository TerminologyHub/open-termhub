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

import java.util.List;

import com.wci.termhub.util.SingleFlight;
import com.wci.termhub.util.SingleFlight.ThrowingSupplier;
import com.wci.termhub.util.TimerCache;

import ca.uhn.fhir.context.FhirVersionEnum;

/**
 * Interim cache of ValueSet search shells (metadata-only LL/LG resources for
 * unfiltered search). Concurrent builds for the same key are coalesced via
 * {@link SingleFlight}.
 *
 * <p>
 * Stores the shell list in memory. Callers must not mutate returned instances;
 * search paging copies the page into the response Bundle.
 */
public final class ValueSetSearchCache {

  /** Cache TTL: 5 minutes. */
  private static final int CACHE_TTL_MS = 300_000;

  /**
   * Max cached shell lists. Steady-state is typically R4+R5 per LOINC version.
   * 32 matches {@link QuestionnaireSearchCache}.
   */
  private static final int CACHE_SIZE = 32;

  /** Shell lists by cache key. */
  private static TimerCache<List<?>> cache = new TimerCache<>(CACHE_SIZE, CACHE_TTL_MS);

  /** Coalesces concurrent shell-list builds. */
  private static final SingleFlight FLIGHT = new SingleFlight();

  /**
   * Instantiates a {@link ValueSetSearchCache}.
   */
  private ValueSetSearchCache() {
    // n/a
  }

  /**
   * Builds a cache key for ValueSet LL/LG search shells.
   *
   * @param fhirVersion FHIR version
   * @param terminologyKey LOINC terminology id or abbr|publisher|version
   * @param metaFlag whether shells include meta tags
   * @return cache key
   */
  public static String buildKey(final FhirVersionEnum fhirVersion, final String terminologyKey,
    final boolean metaFlag) {
    return fhirVersion.name() + "|vs-shells|" + (terminologyKey == null ? "" : terminologyKey) + "|"
        + metaFlag;
  }

  /**
   * Returns cached R4 ValueSet shells, or builds/stores them.
   *
   * @param key the cache key
   * @param loader builds the shell list on miss
   * @return shell value sets (cached instances; do not mutate)
   * @throws Exception if the loader fails
   */
  public static List<org.hl7.fhir.r4.model.ValueSet> getOrLoadR4(final String key,
    final ThrowingSupplier<List<org.hl7.fhir.r4.model.ValueSet>> loader) throws Exception {
    final List<org.hl7.fhir.r4.model.ValueSet> cached = getR4(key);
    if (cached != null) {
      return cached;
    }
    return FLIGHT.execute(key, () -> {
      final List<org.hl7.fhir.r4.model.ValueSet> again = getR4(key);
      if (again != null) {
        return again;
      }
      final List<org.hl7.fhir.r4.model.ValueSet> built = loader.get();
      putR4(key, built);
      final List<org.hl7.fhir.r4.model.ValueSet> fromCache = getR4(key);
      return fromCache != null ? fromCache : built;
    });
  }

  /**
   * Returns cached R5 ValueSet shells, or builds/stores them.
   *
   * @param key the cache key
   * @param loader builds the shell list on miss
   * @return shell value sets (cached instances; do not mutate)
   * @throws Exception if the loader fails
   */
  public static List<org.hl7.fhir.r5.model.ValueSet> getOrLoadR5(final String key,
    final ThrowingSupplier<List<org.hl7.fhir.r5.model.ValueSet>> loader) throws Exception {
    final List<org.hl7.fhir.r5.model.ValueSet> cached = getR5(key);
    if (cached != null) {
      return cached;
    }
    return FLIGHT.execute(key, () -> {
      final List<org.hl7.fhir.r5.model.ValueSet> again = getR5(key);
      if (again != null) {
        return again;
      }
      final List<org.hl7.fhir.r5.model.ValueSet> built = loader.get();
      putR5(key, built);
      final List<org.hl7.fhir.r5.model.ValueSet> fromCache = getR5(key);
      return fromCache != null ? fromCache : built;
    });
  }

  /**
   * Clears all cached ValueSet search shells.
   */
  public static void clear() {
    FLIGHT.clear();
    cache = new TimerCache<>(CACHE_SIZE, CACHE_TTL_MS);
  }

  /**
   * Contains key.
   *
   * @param key the key
   * @return true if present
   */
  public static boolean containsKey(final String key) {
    return cache.get(key) != null;
  }

  /**
   * Gets the r4.
   *
   * @param key the key
   * @return shells or null on miss
   */
  @SuppressWarnings("unchecked")
  private static List<org.hl7.fhir.r4.model.ValueSet> getR4(final String key) {
    final List<?> cached = cache.get(key);
    if (cached == null) {
      return null;
    }
    return (List<org.hl7.fhir.r4.model.ValueSet>) cached;
  }

  /**
   * Put R 4.
   *
   * @param key the key
   * @param valueSets shells to store
   */
  private static void putR4(final String key,
    final List<org.hl7.fhir.r4.model.ValueSet> valueSets) {
    if (key == null || valueSets == null) {
      return;
    }
    cache.put(key, List.copyOf(valueSets));
  }

  /**
   * Gets the r5.
   *
   * @param key the key
   * @return shells or null on miss
   */
  @SuppressWarnings("unchecked")
  private static List<org.hl7.fhir.r5.model.ValueSet> getR5(final String key) {
    final List<?> cached = cache.get(key);
    if (cached == null) {
      return null;
    }
    return (List<org.hl7.fhir.r5.model.ValueSet>) cached;
  }

  /**
   * Put R 5.
   *
   * @param key the key
   * @param valueSets shells to store
   */
  private static void putR5(final String key,
    final List<org.hl7.fhir.r5.model.ValueSet> valueSets) {
    if (key == null || valueSets == null) {
      return;
    }
    cache.put(key, List.copyOf(valueSets));
  }
}
