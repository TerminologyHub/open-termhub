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
 * Represents an exception thrown by canceling an operation.
 */
public class CancelException extends Exception {

  /**
   * Instantiates a {@link CancelException} from the specified parameters.
   *
   * @param message the message
   * @param t the t
   */
  public CancelException(final String message, final Exception t) {
    super(message, t);
  }

  /**
   * Instantiates a {@link CancelException} from the specified parameters.
   *
   * @param message the message
   */
  public CancelException(final String message) {
    super(message);

  }
}
