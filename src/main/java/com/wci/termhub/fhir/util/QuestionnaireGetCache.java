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

import org.hl7.fhir.instance.model.api.IBaseResource;

import com.wci.termhub.util.SingleFlight;
import com.wci.termhub.util.SingleFlight.ThrowingSupplier;
import com.wci.termhub.util.TimerCache;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.IParser;

/**
 * Caches serialized Questionnaire GET responses for R4 and R5. Concurrent misses for the same key
 * are coalesced via {@link SingleFlight}.
 */
public final class QuestionnaireGetCache {

  /** Cache TTL: 5 minutes. */
  private static final int CACHE_TTL_MS = 300_000;

  /** Max cached responses. */
  private static final int CACHE_SIZE = 2000;

  /** R4 FHIR context. */
  private static final FhirContext FHIR_R4 = FhirContext.forR4Cached();

  /** R5 FHIR context. */
  private static final FhirContext FHIR_R5 = FhirContext.forR5Cached();

  /** Serialized Questionnaire JSON by cache key. */
  private static TimerCache<String> cache = new TimerCache<>(CACHE_SIZE, CACHE_TTL_MS);

  /** Coalesces concurrent builds for the same key. */
  private static final SingleFlight FLIGHT = new SingleFlight();

  /**
   * Instantiates a {@link QuestionnaireGetCache}.
   */
  private QuestionnaireGetCache() {
    // n/a
  }

  /**
   * Builds a cache key for a Questionnaire GET by id and LOINC CodeSystem version.
   *
   * @param fhirVersion FHIR version
   * @param loincVersion LOINC release version (e.g. 2.78); null treated as empty
   * @param id questionnaire Concept UUID or LOINC panel concept code
   * @return cache key
   */
  public static String buildKey(final FhirVersionEnum fhirVersion, final String loincVersion,
    final String id) {
    return fhirVersion.name() + "|" + (loincVersion == null ? "" : loincVersion) + "|"
        + (id == null ? "" : id);
  }

  /**
   * Returns a cached R4 Questionnaire, or null on miss.
   *
   * @param key the cache key
   * @return Questionnaire or null
   */
  public static org.hl7.fhir.r4.model.Questionnaire getR4(final String key) {
    final String json = cache.get(key);
    if (json == null) {
      return null;
    }
    return FHIR_R4.newJsonParser().parseResource(org.hl7.fhir.r4.model.Questionnaire.class, json);
  }

  /**
   * Returns a cached R4 Questionnaire, or builds/stores one. Concurrent callers for the same key
   * share a single build.
   *
   * @param key the cache key
   * @param loader builds the Questionnaire on miss
   * @return Questionnaire
   * @throws Exception if the loader fails
   */
  public static org.hl7.fhir.r4.model.Questionnaire getOrLoadR4(final String key,
    final ThrowingSupplier<org.hl7.fhir.r4.model.Questionnaire> loader) throws Exception {
    final org.hl7.fhir.r4.model.Questionnaire cached = getR4(key);
    if (cached != null) {
      return cached;
    }
    return FLIGHT.execute(key, () -> {
      final org.hl7.fhir.r4.model.Questionnaire again = getR4(key);
      if (again != null) {
        return again;
      }
      final org.hl7.fhir.r4.model.Questionnaire built = loader.get();
      putR4(key, built);
      final org.hl7.fhir.r4.model.Questionnaire fromCache = getR4(key);
      return fromCache != null ? fromCache : built;
    });
  }

  /**
   * Stores an R4 Questionnaire GET response.
   *
   * @param key the cache key
   * @param questionnaire the questionnaire
   */
  public static void putR4(final String key,
    final org.hl7.fhir.r4.model.Questionnaire questionnaire) {
    if (key == null || questionnaire == null) {
      return;
    }
    cache.put(key, encode(FHIR_R4, questionnaire));
  }

  /**
   * Returns a cached R5 Questionnaire, or null on miss.
   *
   * @param key the cache key
   * @return Questionnaire or null
   */
  public static org.hl7.fhir.r5.model.Questionnaire getR5(final String key) {
    final String json = cache.get(key);
    if (json == null) {
      return null;
    }
    return FHIR_R5.newJsonParser().parseResource(org.hl7.fhir.r5.model.Questionnaire.class, json);
  }

  /**
   * Returns a cached R5 Questionnaire, or builds/stores one. Concurrent callers for the same key
   * share a single build.
   *
   * @param key the cache key
   * @param loader builds the Questionnaire on miss
   * @return Questionnaire
   * @throws Exception if the loader fails
   */
  public static org.hl7.fhir.r5.model.Questionnaire getOrLoadR5(final String key,
    final ThrowingSupplier<org.hl7.fhir.r5.model.Questionnaire> loader) throws Exception {
    final org.hl7.fhir.r5.model.Questionnaire cached = getR5(key);
    if (cached != null) {
      return cached;
    }
    return FLIGHT.execute(key, () -> {
      final org.hl7.fhir.r5.model.Questionnaire again = getR5(key);
      if (again != null) {
        return again;
      }
      final org.hl7.fhir.r5.model.Questionnaire built = loader.get();
      putR5(key, built);
      final org.hl7.fhir.r5.model.Questionnaire fromCache = getR5(key);
      return fromCache != null ? fromCache : built;
    });
  }

  /**
   * Stores an R5 Questionnaire GET response.
   *
   * @param key the cache key
   * @param questionnaire the questionnaire
   */
  public static void putR5(final String key,
    final org.hl7.fhir.r5.model.Questionnaire questionnaire) {
    if (key == null || questionnaire == null) {
      return;
    }
    cache.put(key, encode(FHIR_R5, questionnaire));
  }

  /**
   * Clears all cached Questionnaire GET responses.
   */
  public static void clear() {
    FLIGHT.clear();
    cache = new TimerCache<>(CACHE_SIZE, CACHE_TTL_MS);
  }

  /**
   * Visible for tests: whether a key is currently cached.
   *
   * @param key the key
   * @return true if present and not expired
   */
  public static boolean containsKey(final String key) {
    return cache.get(key) != null;
  }

  /**
   * Encode resource to JSON.
   *
   * @param ctx the FHIR context
   * @param resource the resource
   * @return JSON
   */
  private static String encode(final FhirContext ctx, final IBaseResource resource) {
    final IParser parser = ctx.newJsonParser();
    return parser.encodeResourceToString(resource);
  }
}
