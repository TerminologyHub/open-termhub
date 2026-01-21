/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;

/**
 * The Class SyndicationDependency.
 */
public class SyndicationDependency {

  /** The edition dependency. */
  private String editionDependency;

  /** The derivative dependency. */
  private List<String> derivativeDependency;

  /**
   * Gets the edition dependency.
   *
   * @return the edition dependency
   */
  @XmlElement(namespace = "http://terminologyhub.com/syndication/th-extension/1.0.0",
      name = "editionDependency")
  public String getEditionDependency() {
    return editionDependency;
  }

  /**
   * Sets the edition dependency.
   *
   * @param editionDependency the new edition dependency
   */
  public void setEditionDependency(final String editionDependency) {
    this.editionDependency = editionDependency;
  }

  /**
   * Gets the derivative dependency.
   *
   * @return the derivative dependency
   */
  @XmlElement(namespace = "http://terminologyhub.com/syndication/th-extension/1.0.0",
      name = "derivativeDependency")
  public List<String> getDerivativeDependency() {
    return derivativeDependency;
  }

  /**
   * Sets the derivative dependency.
   *
   * @param derivativeDependency the new derivative dependency
   */
  public void setDerivativeDependency(final List<String> derivativeDependency) {
    this.derivativeDependency = derivativeDependency;
  }
}
