/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.ecl.test;

import java.util.List;

/**
 * Represents an ECL result.
 */
public class SnomedEclResults {

  /** The ecl. */
  private String ecl;

  /** The count. */
  private int count;

  /** The concepts. */
  private List<String> concepts;

  /**
   * Gets the ecl.
   *
   * @return the ecl
   */
  public String getEcl() {
    return ecl;
  }

  /**
   * Sets the ecl.
   *
   * @param ecl the new ecl
   */
  public void setEcl(final String ecl) {
    this.ecl = ecl;
  }

  /**
   * Gets the count.
   *
   * @return the count
   */
  public int getCount() {
    return count;
  }

  /**
   * Sets the count.
   *
   * @param count the new count
   */
  public void setCount(final int count) {
    this.count = count;
  }

  /**
   * Gets the concepts.
   *
   * @return the concepts
   */
  public List<String> getConcepts() {
    return concepts;
  }

  /**
   * Sets the concepts.
   *
   * @param concepts the new concepts
   */
  public void setConcepts(final List<String> concepts) {
    this.concepts = concepts;
  }
}
