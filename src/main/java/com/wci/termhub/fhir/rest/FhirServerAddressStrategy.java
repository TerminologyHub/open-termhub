/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.rest;

import com.wci.termhub.fhir.util.FhirPublicRequestUrl;

import ca.uhn.fhir.rest.server.IServerAddressStrategy;
import ca.uhn.fhir.rest.server.IncomingRequestAddressStrategy;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * HAPI server base from {@code X-Forwarded-*} headers, else the incoming
 * request. Does not apply {@code proxy.url.base} (Swagger must stay on the
 * host that served the page).
 */
public final class FhirServerAddressStrategy implements IServerAddressStrategy {

  /** Fallback strategy using the incoming request. */
  private final IncomingRequestAddressStrategy incoming = new IncomingRequestAddressStrategy();

  /**
   * Determine server base.
   *
   * @param theServletContext the servlet context
   * @param theRequest the request
   * @return the server base
   */
  /* see superclass */
  @Override
  public String determineServerBase(final ServletContext theServletContext,
    final HttpServletRequest theRequest) {
    return FhirPublicRequestUrl.publicServerBase(theRequest,
        incoming.determineServerBase(theServletContext, theRequest));
  }
}
