/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.rest.r5;

import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r5.model.CapabilityStatement;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;

/**
 * Response highlighter interceptor that is safe to use with the terminology
 * metadata endpoint.
 *
 * <p>
 * The stock {@link ResponseHighlighterInterceptor#capabilityStatementGenerated}
 * hook assumes the generated conformance resource is a
 * {@link CapabilityStatement} (which defines a {@code format} element) and walks
 * that element via {@code FhirTerser}. When {@code GET [base]/metadata?mode=terminology}
 * is served we return a {@code TerminologyCapabilities} resource, which has no
 * {@code format} element, so the terser throws
 * {@code HAPI-1700: Unknown child name 'format' in element TerminologyCapabilities}.
 * Here we only delegate to the base hook for {@code CapabilityStatement}.
 * </p>
 */
public class TermhubResponseHighlighterInterceptorR5 extends ResponseHighlighterInterceptor {

  /* see superclass */
  @Hook(Pointcut.SERVER_CAPABILITY_STATEMENT_GENERATED)
  @Override
  public void capabilityStatementGenerated(final RequestDetails theRequestDetails,
    final IBaseConformance theCapabilityStatement) {
    // TerminologyCapabilities (mode=terminology) has no 'format' element - skip
    if (theCapabilityStatement instanceof CapabilityStatement) {
      super.capabilityStatementGenerated(theRequestDetails, theCapabilityStatement);
    }
  }
}
