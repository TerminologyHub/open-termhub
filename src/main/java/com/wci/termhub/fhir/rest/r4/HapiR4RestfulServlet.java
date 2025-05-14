/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.rest.r4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.wci.termhub.fhir.r4.CodeSystemProviderR4;
import com.wci.termhub.fhir.r4.ConceptMapProviderR4;
import com.wci.termhub.fhir.r4.FHIRTerminologyCapabilitiesProviderR4;
import com.wci.termhub.fhir.r4.ValueSetProviderR4;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.LenientErrorHandler;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.RestfulServer;
import jakarta.servlet.ServletException;

/**
 * The Hapi servlet itself.
 */
public class HapiR4RestfulServlet extends RestfulServer {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -8760493251815507812L;

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(HapiR4RestfulServlet.class);

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

    final FhirContext fhirContext = FhirContext.forR4();
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

    // Set the server's base path to be just "/" since the servlet mapping
    // handles
    // "/r4"
    setServerAddressStrategy(new ca.uhn.fhir.rest.server.IncomingRequestAddressStrategy());

    /*
     * The servlet defines any number of resource providers, and configures itself to use them by
     * calling setResourceProviders()
     */
    final WebApplicationContext applicationContext =
        WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

    if (applicationContext == null) {
      throw new RuntimeException("Unexpected null application exception");
    }
    setResourceProviders(applicationContext.getBean(CodeSystemProviderR4.class),
        applicationContext.getBean(ValueSetProviderR4.class),
        applicationContext.getBean(ConceptMapProviderR4.class));
    // , applicationContext.getBean(TerminologyUploadProviderR4.class)

    setServerConformanceProvider(new FHIRTerminologyCapabilitiesProviderR4(this));

    // Register interceptors
    registerInterceptor(new TermhubOpenApiInterceptorR4());

    logger.info("FHIR Resource providers and interceptors registered");
  }
}
