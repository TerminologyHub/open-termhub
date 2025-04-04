/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.app;

/**
 * The Enum Direction.
 */
public enum Direction {

  /** The asc. */
  ASC("asc"),
  /** The desc. */
  DESC("desc");

  /** The text. */
  private final String text;

  /**
   * Instantiates a new direction.
   *
   * @param text the text
   */
  Direction(final String text) {
    this.text = text;
  }

  /**
   * To string.
   *
   * @return the string
   */
  /*
   * (non-Javadoc)
   *
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return text;
  }
}
