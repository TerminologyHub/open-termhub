/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.rest.r5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.wci.termhub.fhir.r5.CodeSystemProviderR5;
import com.wci.termhub.fhir.r5.ConceptMapProviderR5;
import com.wci.termhub.fhir.r5.FHIRTerminologyCapabilitiesProviderR5;
import com.wci.termhub.fhir.r5.ValueSetProviderR5;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.LenientErrorHandler;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.RestfulServer;
import jakarta.servlet.ServletException;

/**
 * The Hapi servlet itself.
 */
public class HapiR5RestfulServlet extends RestfulServer {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -6078932462068766663L;

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(HapiR5RestfulServlet.class);

  /**
   * The initialize method is automatically called when the servlet is starting up, so it can be
   * used to configure the servlet to define resource providers, or set up configuration,
   * interceptors, etc.
   *
   * @throws ServletException the servlet exception
   */
  @Override
  protected void initialize() throws ServletException {

    setDefaultResponseEncoding(EncodingEnum.JSON);

    final FhirContext fhirContext = FhirContext.forR5();
    final LenientErrorHandler delegateHandler = new LenientErrorHandler();
    fhirContext.setParserErrorHandler(new StrictErrorHandler() {
      @Override
      public void unknownAttribute(final IParseLocation theLocation,
        final String theAttributeName) {
        delegateHandler.unknownAttribute(theLocation, theAttributeName);
      }

      @Override
      public void unknownElement(final IParseLocation theLocation, final String theElementName) {
        delegateHandler.unknownElement(theLocation, theElementName);
      }

      @Override
      public void unknownReference(final IParseLocation theLocation, final String theReference) {
        delegateHandler.unknownReference(theLocation, theReference);
      }
    });
    setFhirContext(fhirContext);

    /*
     * The servlet defines any number of resource providers, and configures itself to use them by
     * calling setResourceProviders()
     */
    final WebApplicationContext applicationContext =
        WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

    if (applicationContext == null) {
      throw new RuntimeException("Unexpected null application exception");
    }
    setResourceProviders(applicationContext.getBean(CodeSystemProviderR5.class),
        applicationContext.getBean(ValueSetProviderR5.class),
        applicationContext.getBean(ConceptMapProviderR5.class));
    // , applicationContext.getBean(TerminologyUploadProviderR5.class));

    setServerConformanceProvider(new FHIRTerminologyCapabilitiesProviderR5(this));

    // Register interceptors
    registerInterceptor(new TermhubOpenApiInterceptorR5());

    logger.info("FHIR Resource providers and interceptors registered");
  }
}
