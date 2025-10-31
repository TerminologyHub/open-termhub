/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * REST exception handler for certain exception types.
 */
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * Handle rest exceptions.
   *
   * @param ex the ex
   * @param request the request
   * @return the response entity
   */
  @ExceptionHandler({
      RestException.class
  })
  protected ResponseEntity<Object> handleRestException(final RuntimeException ex,
    final WebRequest request) {

    final RestException re = (RestException) ex;
    return handleExceptionInternal(re, re.getError(), new HttpHeaders(),
        HttpStatus.valueOf(re.getError().getStatus()), request);
  }

  /* see superclass - handle 400 */
  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
    final MissingServletRequestParameterException ex, final HttpHeaders headers,
    final HttpStatusCode statusCode, final WebRequest request) {

    final RestException re = new RestException(false, 400, "Bad Request", ex.getMessage());
    return handleExceptionInternal(re, re.getError(), new HttpHeaders(),
        HttpStatus.valueOf(re.getError().getStatus()), request);
  }

  /* see superclass - handle 404 */
  @Override
  protected ResponseEntity<Object> handleNoHandlerFoundException(final NoHandlerFoundException ex,
    final HttpHeaders headers, final HttpStatusCode statusCode, final WebRequest request) {

    final RestException re = new RestException(false, 404, "Not found", "API path does not exist");
    return handleExceptionInternal(re, re.getError(), new HttpHeaders(),
        HttpStatus.valueOf(re.getError().getStatus()), request);
  }

  /* see superclass - handle 406 */
  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
    final HttpMediaTypeNotAcceptableException ex, final HttpHeaders headers,
    final HttpStatusCode statusCode, final WebRequest request) {

    final RestException re = new RestException(false, 406, "Not acceptable", "Bad 'accept' header");
    return handleExceptionInternal(re, re.getError(), new HttpHeaders(),
        HttpStatus.valueOf(re.getError().getStatus()), request);
  }

  /* see superclass - handle 415 */
  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
    final HttpMediaTypeNotSupportedException ex, final HttpHeaders headers,
    final HttpStatusCode statusCode, final WebRequest request) {

    final RestException re =
        new RestException(false, 415, "Unsupported media type", "Bad 'content-type' header");
    return handleExceptionInternal(re, re.getError(), new HttpHeaders(),
        HttpStatus.valueOf(re.getError().getStatus()), request);
  }

}
