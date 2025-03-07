/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model;

import java.util.Properties;

/**
 * Generically represents something configurable.
 */
public interface Configurable {

  /**
   * Returns the handler key.
   *
   * @return the handler key
   */
  public String getHandlerKey();

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName();

  /**
   * Sets the properties.
   *
   * @param p the properties
   * @throws Exception the exception
   */
  public void setProperties(Properties p) throws Exception;

}
