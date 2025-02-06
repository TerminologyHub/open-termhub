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

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generically represents something that has an id.
 */
public interface HasId {

  /**
   * Returns the id.
   *
   * @return the id
   */
  @Schema(description = "Unique identifier", format = "uuid")
  public String getId();

  /**
   * Sets the id.
   *
   * @param id the id
   */
  public void setId(final String id);

  /**
   * Returns the confidence.
   *
   * @return the confidence
   */
  @Schema(description = "Confidence value (for use with search results)")
  public Double getConfidence();

  /**
   * Sets the confidence.
   *
   * @param confidence the confidence
   */
  public void setConfidence(Double confidence);
}
