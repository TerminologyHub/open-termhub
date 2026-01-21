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

import com.wci.termhub.model.Configurable;

/**
 * Represents an algorithm without a service. Implementations must fully configure themselves before
 * the compute call is made.
 */
public interface NoServiceAlgorithm extends ProgressReporter, Configurable {

  /**
   * Check preconditions for action. This will make use of data structures configured in the action.
   *
   * @return true, if successful
   * @throws Exception the exception
   */
  public ValidationResult checkPreconditions() throws Exception;

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  public void compute() throws Exception;

  /**
   * Rests to initial conditions.
   *
   * @throws Exception the exception
   */
  public void reset() throws Exception;

  /**
   * Cancel.
   *
   * @throws Exception the exception
   */
  public void cancel() throws Exception;

  /**
   * Returns the default description, especially for algorithms that are configured via algorighm
   * configs.
   *
   * @return the description
   */
  public String getDescription();

}
