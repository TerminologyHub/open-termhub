/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model;

import java.util.Properties;

import com.wci.termhub.util.ModelUtility;

/**
 * Abstractly represents something configurable.
 */
public abstract class AbstractConfigurable implements Configurable {

  /**
   * Returns the name.
   *
   * @return the name
   */
  @Override
  public String getName() {
    return ModelUtility.getNameFromClass(getClass());
  }

  /**
   * Sets the properties.
   *
   * @param properties the properties
   * @throws Exception the exception
   */
  @Override
  public void setProperties(final Properties properties) throws Exception {
    // n/a
  }

}
