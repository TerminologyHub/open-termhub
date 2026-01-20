/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.ecl;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;

/**
 * The Class ErrorHandler.
 */
public class ErrorHandler implements ANTLRErrorStrategy {

  /** The exceptions. */
  final List<RecognitionException> exceptions = new ArrayList<>();

  /* see superclass */
  @Override
  public void reset(final Parser parser) {
    // n/a
  }

  /* see superclass */
  @Override
  public Token recoverInline(final Parser parser) throws RecognitionException {
    return null;
  }

  /* see superclass */
  @Override
  public void recover(final Parser parser, final RecognitionException e)
    throws RecognitionException {
    // n/a
  }

  /* see superclass */
  @Override
  public void sync(final Parser parser) throws RecognitionException {
    // n/a
  }

  /* see superclass */
  @Override
  public boolean inErrorRecoveryMode(final Parser parser) {
    return false;
  }

  /* see superclass */
  @Override
  public void reportMatch(final Parser parser) {
    // n/a
  }

  /* see superclass */
  @Override
  public void reportError(final Parser parser, final RecognitionException e) {
    exceptions.add(e);
  }
}
