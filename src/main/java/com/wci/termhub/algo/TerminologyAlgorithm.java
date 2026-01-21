/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.algo;

/**
 * Represents an algorithm for loading a terminology.
 */
public interface TerminologyAlgorithm extends NoServiceAlgorithm {

  /**
   * Sets the terminology.
   *
   * @param terminology the terminology
   */
  public void setTerminology(String terminology);

  /**
   * Sets the version.
   *
   * @param version the version
   */
  public void setVersion(String version);

  /**
   * Sets the publisher.
   *
   * @param publisher the publisher
   */
  public void setPublisher(String publisher);

}
