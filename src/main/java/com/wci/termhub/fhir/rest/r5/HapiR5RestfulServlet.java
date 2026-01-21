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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.wci.termhub.fhir.r5.CodeSystemProviderR5;
import com.wci.termhub.fhir.r5.ConceptMapProviderR5;
import com.wci.termhub.fhir.r5.FHIRMetadataProviderR5;
import com.wci.termhub.fhir.r5.FHIRTerminologyCapabilitiesR5;
import com.wci.termhub.fhir.r5.SystemTransactionProviderR5;
import com.wci.termhub.fhir.r5.ValueSetProviderR5;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.LenientErrorHandler;
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
    fhirContext.setParserErrorHandler(new LenientErrorHandler());
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

    final FHIRTerminologyCapabilitiesR5 terminologyCapabilitiesR5 =
        applicationContext.getBean(FHIRTerminologyCapabilitiesR5.class);
    final FHIRMetadataProviderR5 metadataProvider = new FHIRMetadataProviderR5(this);
    metadataProvider.setTerminologyCapabilitiesR5(terminologyCapabilitiesR5);
    setServerConformanceProvider(metadataProvider);

    // Register interceptors
    registerInterceptor(new TermhubOpenApiInterceptorR5());

    // Register system-level transaction provider
    registerProvider(applicationContext.getBean(SystemTransactionProviderR5.class));

    logger.info("FHIR Resource providers and interceptors registered");
  }
}
