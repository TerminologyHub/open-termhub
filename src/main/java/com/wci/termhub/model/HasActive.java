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

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generically represents something that is active or inactive.
 */
public interface HasActive {

  /**
   * Indicates whether or not active is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Schema(description = "Indicates whether or not the component is active")
  public Boolean getActive();

  /**
   * Sets the active.
   *
   * @param active the active
   */
  public void setActive(final Boolean active);

}
