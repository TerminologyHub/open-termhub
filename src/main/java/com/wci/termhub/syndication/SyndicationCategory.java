/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication;

import jakarta.xml.bind.annotation.XmlAttribute;

/**
 * The Class SyndicationCategory.
 */
public class SyndicationCategory {

  /** The term. */
  private String term;

  /** The label. */
  private String label;

  /** The scheme. */
  private String scheme;

  /**
   * Gets the term.
   *
   * @return the term
   */
  @XmlAttribute
  public String getTerm() {
    return term;
  }

  /**
   * Sets the term.
   *
   * @param term the new term
   */
  public void setTerm(final String term) {
    this.term = term;
  }

  /**
   * Gets the label.
   *
   * @return the label
   */
  @XmlAttribute
  public String getLabel() {
    return label;
  }

  /**
   * Sets the label.
   *
   * @param label the new label
   */
  public void setLabel(final String label) {
    this.label = label;
  }

  /**
   * Gets the scheme.
   *
   * @return the scheme
   */
  @XmlAttribute
  public String getScheme() {
    return scheme;
  }

  /**
   * Sets the scheme.
   *
   * @param scheme the new scheme
   */
  public void setScheme(final String scheme) {
    this.scheme = scheme;
  }

}
