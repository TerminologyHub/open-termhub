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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Set up config properties cache.
 */
@Component
public class PropertyUtility {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(PropertyUtility.class);

  /** environment. */
  @Autowired
  private Environment env;

  /** properties. */
  private static Properties properties = new Properties();

  /** The is test mode. */
  private static Boolean isTestMode = null;

  /**
   * initialize the properties.
   */
  @SuppressWarnings("rawtypes")
  @PostConstruct
  private void init() {
    logger.info("Initializing PropertyUtility...");

    final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();

    StreamSupport.stream(sources.spliterator(), false)
        .filter(ps -> ps instanceof EnumerablePropertySource)
        .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames()).flatMap(Arrays::stream)
        .distinct().forEach(prop -> properties.setProperty(prop, env.getProperty(prop)));

    logger.info("Loaded properties: {}", properties);
  }

  /**
   * get all properties.
   *
   * @return the properties
   */
  public static Properties getProperties() {
    return properties;
  }

  /**
   * update active status.
   *
   * @param key the property key
   * @param value The property value
   */
  public static void setProperty(final String key, final String value) {
    properties.put(key, value);
  }

  /**
   * update active status.
   *
   * @param key The key of the property to return
   * @return the value of the requested property or null
   */
  public static String getProperty(final String key) {
    if (properties.containsKey(key)) {
      return properties.getProperty(key);
    }
    return null;
  }

  /**
   * Return properties with the specified prefix.
   *
   * @param prefix the prefix of the properties to return
   * @param removePrefix Should the prefix be removed from the keys of the returned properties
   * @return the properties with the specified prefix
   * @throws Exception the exception
   */
  public static Properties getPrefixedProperties(final String prefix, final boolean removePrefix)
    throws Exception {

    final Properties propertiesSubset = new Properties();
    final Iterator<Object> keys = properties.keySet().iterator();

    // get any properties that start with the prefix
    while (keys.hasNext()) {
      String key = keys.next().toString();
      final String originalKey = key;

      if (key.startsWith(prefix)) {
        if (removePrefix) {
          key = key.replace(prefix, "");
        }
        propertiesSubset.put(key, properties.getProperty(originalKey));
      }
    }

    return propertiesSubset;
  }

  /**
   * Indicates whether or not test mode is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  public static boolean isTestMode() throws Exception {
    if (isTestMode == null) {
      isTestMode = "true".equals(getProperty("test.mode"));
    }
    return isTestMode;
  }

  /**
   * Builds the api url.
   *
   * @param path the path
   * @return the string
   * @throws Exception the exception
   */
  public static String buildApiUrl(final String path) throws Exception {
    final StringBuilder sb = new StringBuilder();
    sb.append(getProperties().getProperty("api.url"));
    if (!sb.toString().endsWith("/") && path != null && !path.startsWith("/")) {
      sb.append("/");
    }
    sb.append(path);
    return sb.toString();
  }

  /**
   * Builds the api url.
   *
   * @param service the service
   * @param path the path
   * @return the string
   * @throws Exception the exception
   */
  public static String buildApiUrl(final String service, final String path) throws Exception {
    final StringBuilder sb = new StringBuilder();
    sb.append(getProperties().getProperty("api.url.termhub-" + service + "-service"));
    if (!sb.toString().endsWith("/") && path != null && !path.startsWith("/")) {
      sb.append("/");
    }
    sb.append(path);
    return sb.toString();
  }
}
