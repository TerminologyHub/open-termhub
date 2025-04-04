/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.algo;

/**
 * Generically something that will report progress to a listener.
 */
public interface ProgressReporter {

  /**
   * Adds a {@link ProgressListener}.
   * 
   * @param l the {@link ProgressListener}
   */
  public void addProgressListener(ProgressListener l);

  /**
   * Removes a {@link ProgressListener}.
   * 
   * @param l the {@link ProgressListener}
   */
  public void removeProgressListener(ProgressListener l);

}
