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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseParameters;

import com.wci.termhub.util.TimerCache;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * Caches serialized CodeSystem $lookup Parameters responses for R4 and R5.
 * Key includes FHIR version and regenstriefMode so default and Regenstrief
 * payloads never collide.
 */
public final class CodeSystemLookupCache {

  /** Cache TTL: 5 minutes. */
  private static final int CACHE_TTL_MS = 300_000;

  /** Max cached responses. */
  private static final int CACHE_SIZE = 5000;

  /** R4 FHIR context (lazy). */
  private static final FhirContext FHIR_R4 = FhirContext.forR4Cached();

  /** R5 FHIR context (lazy). */
  private static final FhirContext FHIR_R5 = FhirContext.forR5Cached();

  /** Serialized Parameters JSON by cache key. */
  private static TimerCache<String> cache = new TimerCache<>(CACHE_SIZE, CACHE_TTL_MS);

  /**
   * Instantiates a {@link CodeSystemLookupCache}.
   */
  private CodeSystemLookupCache() {
    // n/a
  }

  /**
   * Builds a cache key for a $lookup request.
   *
   * @param fhirVersion R4 or R5
   * @param system code system URI
   * @param version terminology version
   * @param code the code
   * @param propertySet requested properties, or null for all
   * @param regenstriefMode true when Regenstrief LOINC shaping applies
   * @return cache key
   */
  public static String buildKey(final String fhirVersion, final String system, final String version,
    final String code, final Set<String> propertySet, final boolean regenstriefMode) {
    final String props;
    if (propertySet == null || propertySet.isEmpty()) {
      props = "*";
    } else {
      final List<String> sorted = new ArrayList<>(propertySet);
      Collections.sort(sorted);
      props = sorted.stream().collect(Collectors.joining(","));
    }
    return fhirVersion + "|" + nullToEmpty(system) + "|" + nullToEmpty(version) + "|"
        + nullToEmpty(code) + "|" + props + "|" + regenstriefMode;
  }

  /**
   * Returns a cached R4 Parameters, or null on miss.
   *
   * @param key the cache key
   * @return Parameters or null
   */
  public static org.hl7.fhir.r4.model.Parameters getR4(final String key) {
    final String json = cache.get(key);
    if (json == null) {
      return null;
    }
    return parseR4(json);
  }

  /**
   * Stores an R4 Parameters response.
   *
   * @param key the cache key
   * @param parameters the parameters
   */
  public static void putR4(final String key, final org.hl7.fhir.r4.model.Parameters parameters) {
    if (key == null || parameters == null) {
      return;
    }
    cache.put(key, encode(FHIR_R4, parameters));
  }

  /**
   * Returns a cached R5 Parameters, or null on miss.
   *
   * @param key the cache key
   * @return Parameters or null
   */
  public static org.hl7.fhir.r5.model.Parameters getR5(final String key) {
    final String json = cache.get(key);
    if (json == null) {
      return null;
    }
    return parseR5(json);
  }

  /**
   * Stores an R5 Parameters response.
   *
   * @param key the cache key
   * @param parameters the parameters
   */
  public static void putR5(final String key, final org.hl7.fhir.r5.model.Parameters parameters) {
    if (key == null || parameters == null) {
      return;
    }
    cache.put(key, encode(FHIR_R5, parameters));
  }

  /**
   * Clears all cached lookup responses.
   */
  public static void clear() {
    cache = new TimerCache<>(CACHE_SIZE, CACHE_TTL_MS);
  }

  /**
   * Null-safe string.
   *
   * @param value the value
   * @return empty string if null
   */
  private static String nullToEmpty(final String value) {
    return value == null ? "" : value;
  }

  /**
   * Encode parameters to JSON.
   *
   * @param ctx the FHIR context
   * @param parameters the parameters
   * @return JSON
   */
  private static String encode(final FhirContext ctx, final IBaseParameters parameters) {
    final IParser parser = ctx.newJsonParser();
    return parser.encodeResourceToString(parameters);
  }

  /**
   * Parse R4 Parameters JSON.
   *
   * @param json the JSON
   * @return Parameters
   */
  private static org.hl7.fhir.r4.model.Parameters parseR4(final String json) {
    return FHIR_R4.newJsonParser().parseResource(org.hl7.fhir.r4.model.Parameters.class, json);
  }

  /**
   * Parse R5 Parameters JSON.
   *
   * @param json the JSON
   * @return Parameters
   */
  private static org.hl7.fhir.r5.model.Parameters parseR5(final String json) {
    return FHIR_R5.newJsonParser().parseResource(org.hl7.fhir.r5.model.Parameters.class, json);
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
}
