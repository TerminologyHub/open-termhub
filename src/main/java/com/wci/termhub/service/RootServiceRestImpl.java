/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.rest.ExceptionHandler;

/**
 * Top level class for all REST services.
 */
public class RootServiceRestImpl {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(RootServiceRestImpl.class);

  /**
   * Instantiates an empty {@link RootServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public RootServiceRestImpl() throws Exception {
    // n/a
  }

  /**
   * Handle exception.
   *
   * @param e the e
   * @param whatIsHappening the what is happening
   * @throws Exception the exception
   */
  public static void handleException(final Exception e, final String whatIsHappening)
    throws Exception {
    ExceptionHandler.handleException(e, whatIsHappening, "");
  }

}
