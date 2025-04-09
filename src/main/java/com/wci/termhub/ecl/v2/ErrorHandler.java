/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.ecl.v2;

import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler implements ANTLRErrorStrategy {
    final List<RecognitionException> exceptions = new ArrayList<>();

    @Override
    public void reset(Parser parser) {

    }

    @Override
    public Token recoverInline(Parser parser) throws RecognitionException {
        return null;
    }

    @Override
    public void recover(Parser parser, RecognitionException e) throws RecognitionException {

    }

    @Override
    public void sync(Parser parser) throws RecognitionException {

    }

    @Override
    public boolean inErrorRecoveryMode(Parser parser) {
        return false;
    }

    @Override
    public void reportMatch(Parser parser) {

    }

    @Override
    public void reportError(Parser parser, RecognitionException e) {
        exceptions.add(e);
    }
}
