/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.app;

/**
 * FHIR server compatibility mode.
 */
public enum ServerMode {

  /** Default TermHub behavior. */
  DEFAULT,

  /** Regenstrief LOINC FHIR server compatibility (LL/LG value sets, $lookup rules). */
  REGENSTRIEF;

  /**
   * Returns whether this mode enables Regenstrief-compatible LOINC FHIR behavior.
   *
   * @return true for {@link #REGENSTRIEF}
   */
  public boolean isRegenstriefMode() {
    return this == REGENSTRIEF;
  }

  /**
   * Parses {@code server.mode} configuration (e.g. {@code basic}, {@code regenstrief}).
   *
   * @param value property value or null
   * @return the mode, defaulting to {@link #DEFAULT}
   */
  public static ServerMode fromValue(final String value) {
    if (value == null || value.isBlank()) {
      return DEFAULT;
    }
    final String normalized = value.trim();
    for (final ServerMode mode : values()) {
      if (mode.name().equalsIgnoreCase(normalized)) {
        return mode;
      }
    }
    return DEFAULT;
  }
}
