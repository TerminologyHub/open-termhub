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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.util.ModelUtility;

import jakarta.persistence.MappedSuperclass;

/**
 * Base model for all classes.
 */
@MappedSuperclass
public abstract class BaseModel {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(BaseModel.class);

  /**
   * Instantiates an empty {@link BaseModel}.
   */
  protected BaseModel() {
    // n/a
  }

  /* see superclass */
  @Override
  public String toString() {
    try {
      return ModelUtility.toJson(this);
    } catch (final Exception e) {
      logger.error("Unexpected error serializing object", e);
      throw new RuntimeException(e);
    }
  }
}
