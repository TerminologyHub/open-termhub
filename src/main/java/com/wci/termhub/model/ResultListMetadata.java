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
 * Represents a list of metadata.
 */
@Schema(description = "Represents a list of metadata objects returned from a find call")
public class ResultListMetadata extends ResultList<Metadata> {

  /**
   * Instantiates an empty {@link ResultListMetadata}.
   */
  public ResultListMetadata() {
    // n/a
  }

  /**
   * Instantiates a {@link ResultListMetadata} from the specified parameters.
   *
   * @param list the list
   */
  public ResultListMetadata(final ResultList<Metadata> list) {
    this.setItems(list.getItems());
    this.setParameters(list.getParameters());
    this.setTotal(list.getTotal());
  }
}
