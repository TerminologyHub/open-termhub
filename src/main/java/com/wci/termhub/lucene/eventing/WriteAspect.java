/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.lucene.eventing;

import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.lucene.LuceneDataAccess;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Aspect
@Component
public class WriteAspect {

    private final AtomicBoolean writeInProgress = new AtomicBoolean(false);

    @Around("@annotation(ca.uhn.fhir.rest.annotation.Create) || @annotation(ca.uhn.fhir.rest.annotation.Delete)")
    public Object aroundWrite(ProceedingJoinPoint pjp) throws Throwable {
        if (!writeInProgress.compareAndSet(false, true)) {
            throw FhirUtilityR4.exception("A write operation is already in progress",
                    OperationOutcome.IssueType.CONFLICT, HttpServletResponse.SC_CONFLICT);
        }
        try {
            Object result = pjp.proceed();
            LuceneDataAccess.clearReaders();
            return result;
        } finally {
            writeInProgress.set(false);
        }
    }
}
