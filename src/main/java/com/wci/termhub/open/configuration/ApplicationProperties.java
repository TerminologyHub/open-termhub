/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.open.configuration;

import java.util.Iterator;
import java.util.Properties;

/**
 * Application properties configuration.
 */

@SuppressWarnings("serial")
public class ApplicationProperties extends Properties {

  /**
   * Returns the prefixed properties.
   *
   * @param prefix the prefix
   * @return the prefixed properties
   * @throws Exception the exception
   */
  public Properties getPrefixedProperties(final String prefix) throws Exception {
    return getPrefixedProperties(prefix, true);
  }

  /**
   * Returns the prefixed properties.
   *
   * @param prefix the prefix
   * @param removePrefix the remove prefix
   * @return the prefixed properties
   * @throws Exception the exception
   */
  public Properties getPrefixedProperties(final String prefix, final boolean removePrefix)
    throws Exception {

    final Properties propertiesSubset = new Properties();
    final Iterator<Object> keys = keySet().iterator();

    // get any properties that start with the prefix
    while (keys.hasNext()) {

      String key = keys.next().toString();
      final String originalKey = key;

      if (key.startsWith(prefix)) {

        if (removePrefix) {
          key = key.replace(prefix, "");
        }

        propertiesSubset.put(key, getProperty(originalKey));
      }
    }

    return propertiesSubset;
  }
}
