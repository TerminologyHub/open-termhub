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
 * The Interface TerminologyComponent.
 */
public interface TerminologyComponent extends HasModified {

  /**
   * Returns the terminology.
   *
   * @return the terminology
   */
  @Schema(description = "Terminology abbreviation, e.g. \"SNOMEDCT\"")
  public String getTerminology();

  /**
   * Sets the terminology.
   *
   * @param terminology the terminology
   */
  public void setTerminology(String terminology);

  /**
   * Returns the version.
   *
   * @return the version
   */
  @Schema(description = "Terminology version, e.g. \"20230901\"")
  public String getVersion();

  /**
   * Sets the version.
   *
   * @param version the version
   */
  public void setVersion(String version);

  /**
   * Returns the publisher.
   *
   * @return the publisher
   */
  @Schema(description = "Terminology publisher, e.g. \"SNOMEDCT\"")
  public String getPublisher();

  /**
   * Sets the publisher.
   *
   * @param publisher the publisher
   */
  public void setPublisher(String publisher);

}
