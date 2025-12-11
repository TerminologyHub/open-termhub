/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wci.termhub.service.RootServiceRestImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MediaType;

/**
 * Reference implementation of {@link FhirServiceRest}.
 * https://hl7.org/fhir/R4/terminology-service.html https://hl7.org/fhir/R4/search.html
 */
@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON)
public class FhirServiceRestImpl extends RootServiceRestImpl {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(FhirServiceRestImpl.class);

  /** The request. */
  @SuppressWarnings("unused")
  @Autowired(required = false)
  private HttpServletRequest request;

  /**
   * Instantiates an empty {@link FhirServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public FhirServiceRestImpl() throws Exception {
    // n/a
  }

  /**
   * Instantiates a {@link FhirServiceRestImpl} from the specified parameters. For testing.
   *
   * @param request the request
   * @throws Exception the exception
   */
  public FhirServiceRestImpl(final HttpServletRequest request) throws Exception {
    logger.info("FhirServiceRestImpl constructor {}", request);
    this.request = request;
  }

}
