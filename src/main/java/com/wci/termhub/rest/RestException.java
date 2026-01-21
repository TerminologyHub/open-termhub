/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.rest;

import com.wci.termhub.model.Error;

/**
 * Wrapper REST exception so we can properly format all exception responses to REST calls.
 */
public class RestException extends RuntimeException {

  /** The error. */
  private Error error;

  /**
   * Instantiates an empty {@link RestException}.
   */
  public RestException() {
    // n/a
  }

  /**
   * Instantiates a {@link RestException} from the specified parameters.
   *
   * @param local the local
   * @param status the status
   * @param error the error
   * @param message the message
   */
  public RestException(final boolean local, final int status, final String error,
      final String message) {
    this.error = new Error(local, status, error, message);
  }

  /**
   * Instantiates a {@link RestException} from the specified parameters.
   *
   * @param error the error
   */
  public RestException(final Error error) {
    this.error = error;
  }

  /**
   * Returns the error.
   *
   * @return the error
   */
  public Error getError() {
    return error;
  }

  /**
   * Sets the error.
   *
   * @param error the error
   */
  public void setError(final Error error) {
    this.error = error;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((error == null) ? 0 : error.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }
    final RestException other = (RestException) obj;
    if (error == null) {
      if (other.error != null) {
        return false;
      }
    } else if (!error.equals(other.error)) {
      return false;
    }
    return true;
  }

}
