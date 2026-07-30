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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for caching objects with a timeout. Thread-safe; get never throws on map/timeMap drift.
 *
 * @param <T> the generic type
 */
public class TimerCache<T> {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(TimerCache.class);

  /** The map. */
  private final Map<String, T> map;

  /** The time map. */
  private final Map<String, Long> timeMap;

  /** The timeout. */
  private final int timeout;

  /** The size. */
  private final int size;

  /**
   * Instantiates a new timer cache.
   *
   * @param size the size
   * @param timeoutMilliseconds the timeout milliseconds
   */
  public TimerCache(final int size, final int timeoutMilliseconds) {
    this.size = size;
    this.timeout = timeoutMilliseconds;
    this.timeMap = new HashMap<>();
    this.map = new LinkedHashMap<String, T>((this.size + 1) * 4 / 3, 0.75f, true) {

      /* see superclass */
      @Override
      protected boolean removeEldestEntry(final Map.Entry<String, T> eldest) {
        final boolean flag = size() > TimerCache.this.size;
        if (flag) {
          timeMap.remove(eldest.getKey());
        }
        return flag;
      }
    };
  }

  /**
   * Returns the cached value, or null on miss/expiry/corrupt entry. Never throws due to map drift.
   *
   * @param key the key
   * @return the value or null
   */
  public synchronized T get(final String key) {
    if (key == null) {
      return null;
    }
    final long now = System.currentTimeMillis();
    final Long cachedAt = timeMap.get(key);
    if (map.containsKey(key) && cachedAt != null && (now - cachedAt.longValue()) < timeout) {
      if (logger.isDebugEnabled()) {
        logger.debug("   CACHE HIT = " + StringUtility.substr(key, 20));
      }
      return map.get(key);
    }
    // Miss, expired, or desynced maps — fail open and clean both sides.
    map.remove(key);
    timeMap.remove(key);
    return null;
  }

  /**
   * Put. Null value removes the key (does not cache null/failures).
   *
   * @param key the key
   * @param value the value
   */
  public synchronized void put(final String key, final T value) {
    if (key == null) {
      return;
    }
    // putting null is tantamount to removing something — do not cache null
    if (value == null) {
      map.remove(key);
      timeMap.remove(key);
      return;
    }
    final long now = System.currentTimeMillis();
    map.put(key, value);
    timeMap.put(key, Long.valueOf(now));
  }

  /**
   * Check internal consistency.
   *
   * @throws Exception the exception
   */
  public synchronized void check() throws Exception {
    if (map.size() != timeMap.size()) {
      throw new Exception("unexpected differences in size = " + map.size() + ", " + timeMap.size());
    }
    final Set<String> keys = new HashSet<>(map.keySet());
    keys.removeAll(timeMap.keySet());
    if (!keys.isEmpty()) {
      throw new Exception("Mismatched keys (1) = " + keys);
    }
    final Set<String> keys2 = new HashSet<>(timeMap.keySet());
    keys2.removeAll(map.keySet());
    if (!keys2.isEmpty()) {
      throw new Exception("Mismatched keys (2) = " + keys2);
    }
  }
}
