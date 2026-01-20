/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.lucene.eventing;

import java.util.concurrent.atomic.AtomicBoolean;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.lucene.LuceneDataAccess;

import jakarta.servlet.http.HttpServletResponse;

/**
 * The Class WriteAspect.
 */
@Aspect
@Component
public class WriteAspect {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(WriteAspect.class);

  /** The write in progress. */
  private final AtomicBoolean writeInProgress = new AtomicBoolean(false);

  /**
   * Around write.
   *
   * @param pjp the pjp
   * @return the object
   * @throws Throwable the throwable
   */
  @Around("@annotation(com.wci.termhub.lucene.eventing.Write)")
  public Object aroundWrite(final ProceedingJoinPoint pjp) throws Throwable {
    if (logger.isTraceEnabled()) {
      logger.trace("Write operation started");
    }
    if (!writeInProgress.compareAndSet(false, true)) {
      throw FhirUtilityR4.exception(
          "A write operation is already in progress, please wait until it completes",
          OperationOutcome.IssueType.CONFLICT, HttpServletResponse.SC_CONFLICT);
    }
    try {
      final Object result = pjp.proceed();
      LuceneDataAccess.clearReaders();
      if (logger.isTraceEnabled()) {
        logger.trace("Write operation completed");
      }
      return result;
    } finally {
      writeInProgress.set(false);
    }
  }
}
