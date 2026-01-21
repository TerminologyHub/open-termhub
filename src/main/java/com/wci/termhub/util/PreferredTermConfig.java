/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

/**
 * The Enum PreferredTermConfig.
 */
public enum PreferredTermConfig {

  /** The snomedct. */
  SNOMEDCT("SNOMEDCT", "900000000000548007", "PT"),

  /** The snomedct us. */
  SNOMEDCT_US("SNOMEDCT_US", "900000000000548007", "PT"),

  /** The lnc. */
  LNC("LNC", "LPDN", "LOINC Parts Display Name"),

  /** The loinc. */
  LOINC("LOINC", "LPDN", "LOINC Parts Display Name"),

  /** The icd10cm. */
  ICD10CM("ICD10CM", "HT", "Hierarchical Term"),

  /** The rxnorm. */
  RXNORM("RXNORM", "PT", "Preferred Term");

  /** The abbreviation. */
  private final String abbreviation;

  /** The preferred term code. */
  private final String preferredTermCode;

  /** The preferred term description. */
  private final String preferredTermDescription;

  /**
   * Instantiates a new preferred term config.
   *
   * @param abbreviation the abbreviation
   * @param preferredTermCode the preferred term code
   * @param preferredTermDescription the preferred term description
   */
  PreferredTermConfig(final String abbreviation, final String preferredTermCode,
      final String preferredTermDescription) {
    this.abbreviation = abbreviation;
    this.preferredTermCode = preferredTermCode;
    this.preferredTermDescription = preferredTermDescription;
  }

  /**
   * Gets the abbreviation.
   *
   * @return the abbreviation
   */
  public String getAbbreviation() {
    return abbreviation;
  }

  /**
   * Gets the preferred term code.
   *
   * @return the preferred term code
   */
  public String getPreferredTermCode() {
    return preferredTermCode;
  }

  /**
   * Gets the preferred term description.
   *
   * @return the preferred term description
   */
  public String getPreferredTermDescription() {
    return preferredTermDescription;
  }

  /**
   * Finds a terminology config by abbreviation.
   * 
   * @param abbreviation the terminology abbreviation to find
   * @return the matching config or null if not found
   */
  public static PreferredTermConfig findByAbbreviation(final String abbreviation) {
    if (abbreviation == null) {
      return null;
    }

    for (final PreferredTermConfig config : values()) {
      if (abbreviation.contains(config.abbreviation)) {
        return config;
      }
    }
    return null;
  }

  /**
   * Checks if a terminology abbreviation is supported.
   * 
   * @param abbreviation the terminology abbreviation to check
   * @return true if supported, false otherwise
   */
  public static boolean isSupported(final String abbreviation) {
    return findByAbbreviation(abbreviation) != null;
  }

}
