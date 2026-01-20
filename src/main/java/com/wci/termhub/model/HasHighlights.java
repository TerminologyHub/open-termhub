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

import java.util.Map;

/**
 * Generically represents something that tracks a collection of highlights.
 */
public interface HasHighlights {

  /**
   * Gets the highlight.
   *
   * @return the highlight
   */
  public Map<String, String> getHighlights();

  /**
   * Sets the highlight.
   *
   * @param highlights the highlights
   */
  public void setHighlights(Map<String, String> highlights);

}
