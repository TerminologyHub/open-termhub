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

import java.util.Date;

/**
 * Generically represents a health check response.
 */
public class HealthCheck extends BaseModel {

  /** The name. */
  private String name;

  /** The timestamp. */
  private Date timestamp;

  /** The status. */
  private boolean status;

  /**
   * Instantiates an empty {@link HealthCheck}.
   */
  public HealthCheck() {
    // n/a
  }

  /**
   * Instantiates a {@link HealthCheck} from the specified parameters.
   *
   * @param name the name
   * @param timestamp the timestamp
   * @param status the status
   */
  public HealthCheck(final String name, final Date timestamp, final boolean status) {
    this.name = name;
    this.timestamp = timestamp;
    this.status = status;
  }

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the name
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Returns the timestamp.
   *
   * @return the timestamp
   */
  public Date getTimestamp() {
    return timestamp;
  }

  /**
   * Sets the timestamp.
   *
   * @param timestamp the timestamp
   */
  public void setTimestamp(final Date timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Indicates whether or not status is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isStatus() {
    return status;
  }

  /**
   * Sets the status.
   *
   * @param status the status
   */
  public void setStatus(final boolean status) {
    this.status = status;
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
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + (status ? 1231 : 1237);
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
    final HealthCheck other = (HealthCheck) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (status != other.status) {
      return false;
    }
    return true;
  }

}
