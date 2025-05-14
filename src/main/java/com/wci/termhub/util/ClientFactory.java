/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import java.util.HashMap;
import java.util.Map;

import com.wci.termhub.rest.client.RootClient;

/**
 * A factory for creating clients.
 */
public final class ClientFactory {

  /**
   * Instantiates an empty {@link ClientFactory}.
   */
  private ClientFactory() {
    // n/a
  }

  /** The cache. */
  private static Map<String, RootClient> cache = new HashMap<>();

  /**
   * Returns the.
   *
   * @param <T> the
   * @param service the service
   * @param clazz the clazz
   * @return the t
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public static <T extends RootClient> T get(final String service, final Class<T> clazz)
    throws Exception {
    if (cache.containsKey(service)) {
      return (T) cache.get(service);
    }
    final T client = HandlerUtility.newStandardHandlerInstanceWithConfiguration("client." + service,
        "DEFAULT", clazz);
    cache.put(service, client);
    return client;
  }

  /**
   * Returns the.
   *
   * @param <T> the
   * @param clazz the clazz
   * @return the t
   * @throws Exception the exception
   */
  public static <T extends RootClient> T get(final Class<T> clazz) throws Exception {
    return get(clazz.getSimpleName().replace("Client", "").toLowerCase(), clazz);
  }
}
