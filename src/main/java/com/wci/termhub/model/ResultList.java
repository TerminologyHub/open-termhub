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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a list of results. Aligns with "collection".
 *
 * @param <T> the type parameter
 */
public class ResultList<T> extends BaseModel {

  /** The total count. */
  private long total = 0;

  /** The objects. */
  private List<T> items = null;

  /** The parameters. */
  private SearchParameters parameters;

  /**
   * Instantiates an empty {@link ResultList}.
   */
  public ResultList() {
    // n/a
  }

  /**
   * Instantiates a {@link ResultList} from the specified parameters.
   *
   * @param items the items
   */
  public ResultList(final List<T> items) {
    this.items = items;
    if (items != null) {
      this.total = items.size();
    } else {
      this.total = 0;
    }
  }

  /**
   * Instantiates a {@link ResultList} from the specified parameters.
   *
   * @param other the other
   */
  public ResultList(final ResultList<T> other) {
    total = other.getTotal();
    items = new ArrayList<>(other.getItems());
    parameters = other.getParameters();
  }

  /**
   * Sets the items.
   *
   * @param items the items
   */

  public void setItems(final List<T> items) {
    this.items = items;
  }

  /**
   * Returns the but in an XML transient way.
   *
   * @return the objects transient
   */
  @Schema(description = "items of the result list")
  @JsonProperty("items")
  public List<T> getItems() {
    if (items == null) {
      items = new ArrayList<>();
    }
    return items;
  }

  /**
   * Returns the total.
   *
   * @return the total
   */
  @Schema(description = "Total number of results (often this list represents just a single page)")
  public long getTotal() {
    return total;
  }

  /**
   * Sets the total.
   *
   * @param total the total
   */
  public void setTotal(final long total) {
    this.total = total;
  }

  /**
   * Returns the parameters.
   *
   * @return the parameters
   */
  @Schema(description = "Parameters used to achieve this result")
  public SearchParameters getParameters() {
    return parameters;
  }

  /**
   * Sets the parameters.
   *
   * @param parameters the parameters
   */
  public void setParameters(final SearchParameters parameters) {
    this.parameters = parameters;
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((items == null) ? 0 : items.hashCode());
    result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
    result = prime * result + (int) (total ^ (total >>> 32));
    return result;
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  /* see superclass */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }
    @SuppressWarnings("rawtypes")
    final ResultList other = (ResultList) obj;
    if (items == null) {
      if (other.items != null) {
        return false;
      }
    } else if (!items.equals(other.items)) {
      return false;
    }
    if (parameters == null) {
      if (other.parameters != null) {
        return false;
      }
    } else if (!parameters.equals(other.parameters)) {
      return false;
    }
    if (total != other.total) {
      return false;
    }
    return true;
  }
}
