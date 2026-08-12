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

import org.hl7.fhir.instance.model.api.IBaseResource;

import com.wci.termhub.util.TimerCache;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.IParser;

/**
 * Caches serialized ValueSet $expand responses for R4 and R5.
 */
public final class ValueSetExpandCache {

  /** Cache TTL: 5 minutes. */
  private static final int CACHE_TTL_MS = 300_000;

  /** Max cached responses. */
  private static final int CACHE_SIZE = 2000;

  /** R4 FHIR context. */
  private static final FhirContext FHIR_R4 = FhirContext.forR4Cached();

  /** R5 FHIR context. */
  private static final FhirContext FHIR_R5 = FhirContext.forR5Cached();

  /** Serialized ValueSet JSON by cache key. */
  private static TimerCache<String> cache = new TimerCache<>(CACHE_SIZE, CACHE_TTL_MS);

  /**
   * Instantiates a {@link ValueSetExpandCache}.
   */
  private ValueSetExpandCache() {
    // n/a
  }

  /**
   * Builds a cache key for a $expand request.
   *
   * @param fhirVersion FHIR version
   * @param id value set id (may be null)
   * @param url value set url (may be null)
   * @param version valueSetVersion (may be null)
   * @param offset expansion offset
   * @param count expansion count
   * @param filter text filter (may be null)
   * @param activeOnly active-only flag
   * @param languages display languages (may be null)
   * @param includeDesignations whether designations are included
   * @return cache key
   */
  public static String buildKey(final FhirVersionEnum fhirVersion, final String id, final String url,
    final String version, final int offset, final int count, final String filter,
    final boolean activeOnly, final Set<String> languages, final boolean includeDesignations) {
    final String langs;
    if (languages == null || languages.isEmpty()) {
      langs = "*";
    } else {
      final List<String> sorted = new ArrayList<>(languages);
      Collections.sort(sorted);
      langs = sorted.stream().collect(Collectors.joining(","));
    }
    return fhirVersion.name() + "|" + nullToEmpty(id) + "|" + nullToEmpty(url) + "|"
        + nullToEmpty(version) + "|" + offset + "|" + count + "|" + nullToEmpty(filter) + "|"
        + activeOnly + "|" + langs + "|" + includeDesignations;
  }

  /**
   * Returns a cached R4 ValueSet, or null on miss.
   *
   * @param key the cache key
   * @return ValueSet or null
   */
  public static org.hl7.fhir.r4.model.ValueSet getR4(final String key) {
    final String json = cache.get(key);
    if (json == null) {
      return null;
    }
    return FHIR_R4.newJsonParser().parseResource(org.hl7.fhir.r4.model.ValueSet.class, json);
  }

  /**
   * Stores an R4 ValueSet expand response.
   *
   * @param key the cache key
   * @param valueSet the value set
   */
  public static void putR4(final String key, final org.hl7.fhir.r4.model.ValueSet valueSet) {
    if (key == null || valueSet == null) {
      return;
    }
    cache.put(key, encode(FHIR_R4, valueSet));
  }

  /**
   * Returns a cached R5 ValueSet, or null on miss.
   *
   * @param key the cache key
   * @return ValueSet or null
   */
  public static org.hl7.fhir.r5.model.ValueSet getR5(final String key) {
    final String json = cache.get(key);
    if (json == null) {
      return null;
    }
    return FHIR_R5.newJsonParser().parseResource(org.hl7.fhir.r5.model.ValueSet.class, json);
  }

  /**
   * Stores an R5 ValueSet expand response.
   *
   * @param key the cache key
   * @param valueSet the value set
   */
  public static void putR5(final String key, final org.hl7.fhir.r5.model.ValueSet valueSet) {
    if (key == null || valueSet == null) {
      return;
    }
    cache.put(key, encode(FHIR_R5, valueSet));
  }

  /**
   * Clears all cached expand responses.
   */
  public static void clear() {
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
   * Null-safe string.
   *
   * @param value the value
   * @return empty string if null
   */
  private static String nullToEmpty(final String value) {
    return value == null ? "" : value;
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
