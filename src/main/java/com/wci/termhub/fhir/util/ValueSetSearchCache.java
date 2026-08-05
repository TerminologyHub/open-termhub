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

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;

import com.wci.termhub.util.SingleFlight;
import com.wci.termhub.util.SingleFlight.ThrowingSupplier;
import com.wci.termhub.util.TimerCache;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.IParser;

/**
 * Interim cache of ValueSet search shells (metadata-only LL/LG resources for
 * unfiltered search). Concurrent builds for the same key are coalesced via
 * {@link SingleFlight}.
 */
public final class ValueSetSearchCache {

  /** Cache TTL: 5 minutes. */
  private static final int CACHE_TTL_MS = 300_000;

  /**
   * Max cached shell lists. Steady-state is typically R4+R5 per LOINC version.
   * 32 matches {@link QuestionnaireSearchCache}.
   */
  private static final int CACHE_SIZE = 32;

  /** R4 FHIR context. */
  private static final FhirContext FHIR_R4 = FhirContext.forR4Cached();

  /** R5 FHIR context. */
  private static final FhirContext FHIR_R5 = FhirContext.forR5Cached();

  /** Serialized shell Bundle JSON by cache key. */
  private static TimerCache<String> cache = new TimerCache<>(CACHE_SIZE, CACHE_TTL_MS);

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
   * @return shell value sets (detached copies from cache JSON)
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
   * @return shell value sets (detached copies from cache JSON)
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
  private static List<org.hl7.fhir.r4.model.ValueSet> getR4(final String key) {
    final String json = cache.get(key);
    if (json == null) {
      return null;
    }
    final Bundle bundle = FHIR_R4.newJsonParser().parseResource(Bundle.class, json);
    final List<org.hl7.fhir.r4.model.ValueSet> list = new ArrayList<>();
    for (final Bundle.BundleEntryComponent entry : bundle.getEntry()) {
      if (entry.getResource() instanceof final org.hl7.fhir.r4.model.ValueSet vs) {
        list.add(vs);
      }
    }
    return list;
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
    final Bundle bundle = new Bundle();
    bundle.setType(BundleType.COLLECTION);
    for (final org.hl7.fhir.r4.model.ValueSet vs : valueSets) {
      bundle.addEntry().setResource(vs);
    }
    cache.put(key, encode(FHIR_R4, bundle));
  }

  /**
   * Gets the r5.
   *
   * @param key the key
   * @return shells or null on miss
   */
  private static List<org.hl7.fhir.r5.model.ValueSet> getR5(final String key) {
    final String json = cache.get(key);
    if (json == null) {
      return null;
    }
    final org.hl7.fhir.r5.model.Bundle bundle =
        FHIR_R5.newJsonParser().parseResource(org.hl7.fhir.r5.model.Bundle.class, json);
    final List<org.hl7.fhir.r5.model.ValueSet> list = new ArrayList<>();
    for (final org.hl7.fhir.r5.model.Bundle.BundleEntryComponent entry : bundle.getEntry()) {
      if (entry.getResource() instanceof final org.hl7.fhir.r5.model.ValueSet vs) {
        list.add(vs);
      }
    }
    return list;
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
    final org.hl7.fhir.r5.model.Bundle bundle = new org.hl7.fhir.r5.model.Bundle();
    bundle.setType(org.hl7.fhir.r5.model.Bundle.BundleType.COLLECTION);
    for (final org.hl7.fhir.r5.model.ValueSet vs : valueSets) {
      bundle.addEntry().setResource(vs);
    }
    cache.put(key, encode(FHIR_R5, bundle));
  }

  /**
   * Encode.
   *
   * @param ctx FHIR context
   * @param resource resource
   * @return JSON
   */
  private static String encode(final FhirContext ctx, final IBaseResource resource) {
    final IParser parser = ctx.newJsonParser();
    return parser.encodeResourceToString(resource);
  }
}
