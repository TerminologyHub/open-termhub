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
 * Utility for caching objects with a timeout.
 *
 * @param <T> the generic type
 */
public class TimerCache<T> {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(TimerCache.class);

  /** The map. */
  private Map<String, T> map = null;

  /** The time map. */
  private Map<String, Long> timeMap = null;

  /** The timeout. */
  private int timeout = 300000;

  /** The size. */
  private int size;

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
   * Returns the.
   *
   * @param key the key
   * @return the t
   */
  public T get(final String key) {
    final long now = System.currentTimeMillis();
    if (map.containsKey(key) && (now - timeMap.get(key)) < timeout) {
      logger.debug("   CACHE HIT = " + StringUtility.substr(key, 20));
      return map.get(key);
    } else {
      map.remove(key);
      map.remove(key);
    }
    return null;
  }

  /**
   * Put.
   *
   * @param key the key
   * @param value the value
   */
  public void put(final String key, final T value) {
    // putting null is tantamount to removing something
    if (value == null) {
      map.remove(key);
      timeMap.remove(key);
    }
    final long now = System.currentTimeMillis();
    map.put(key, value);
    timeMap.put(key, now);
  }

  /**
   * Check.
   *
   * @throws Exception the exception
   */
  public void check() throws Exception {
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
    if (!keys.isEmpty()) {
      throw new Exception("Mismatched keys (2) = " + keys);
    }

  }
}
