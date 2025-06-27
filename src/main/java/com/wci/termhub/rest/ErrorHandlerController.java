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

import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Handler for errors when accessing API thru browser. This handles the /error
 * path.
 */
@Controller
@Hidden
@RequestMapping("/error")
public class ErrorHandlerController implements ErrorController {

  /** Logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(ErrorHandlerController.class);

  /** The error attributes. */
  private final ErrorAttributes errorAttributes;

  /**
   * Basic error controller.
   *
   * @param errorAttributes the error attributes
   */
  public ErrorHandlerController(final ErrorAttributes errorAttributes) {
    this.errorAttributes = errorAttributes;
  }

  /**
   * Handle error.
   *
   * @param request the request
   * @return the string
   */
  @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
  @ResponseBody
  public String handleErrorHtml(final HttpServletRequest request) {
    final Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
    final Map<String, Object> body = getErrorAttributes(request, false);
    String ppBody = null;
    try {
      ppBody = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(body);
    } catch (final Exception e) {
      ppBody = body.toString().replaceAll("<", "&lt;");
    }

    return String.format("<html><body><h2>Error Page</h2><div>Something unexpected went wrong, "
        + "Please contact <a href=\"mailto:support-termhub@westcoastinformatics.com\">"
        + "support-termhub@westcoastinformatics.com</a>."
        + "</div><div>Status code: <b>%s</b></div>"
        + "<div>Message: <pre>%s</pre></div><body></html>", statusCode, ppBody);
  }

  /**
   * Handle error json.
   *
   * @param request the request
   * @return the response entity
   */
  @RequestMapping()
  @ResponseBody
  public ResponseEntity<Map<String, Object>> handleErrorJson(final HttpServletRequest request) {
    final HttpStatus status = getStatus(request);
    if (status == HttpStatus.NO_CONTENT) {
      return new ResponseEntity<>(status);
    }
    final Map<String, Object> body = getErrorAttributes(request, false);
    return new ResponseEntity<>(body, status);
  }

  /**
   * Returns the status.
   *
   * @param request the request
   * @return the status
   */
  protected HttpStatus getStatus(final HttpServletRequest request) {
    final Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
    if (statusCode == null) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    try {
      return HttpStatus.valueOf(statusCode);
    } catch (final Exception ex) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }

  /**
   * Returns the error attributes.
   *
   * @param request the request
   * @param includeStackTrace the include stack trace
   * @return the error attributes
   */
  protected Map<String, Object> getErrorAttributes(final HttpServletRequest request,
    final boolean includeStackTrace) {
    final WebRequest webRequest = new ServletWebRequest(request);
    final Map<String, Object> body = errorAttributes.getErrorAttributes(webRequest,
        ErrorAttributeOptions.of(Include.STACK_TRACE));
    if (body.containsKey("message")) {
      try {
        final String message = body.get("message").toString();
        final StringBuilder sb = new StringBuilder();
        for (final String line : message.split("\\n")) {
          sb.append(StringEscapeUtils.escapeHtml4(line));
          sb.append("\n");
        }
        // remove the trailing \n
        body.put("message", sb.toString().replaceFirst("\\n$", ""));
      } catch (final Exception e) {
        body.put("message", body.get("message").toString().replaceAll("<", "&lt;"));
      }
    }
    return body;
  }

  /**
   * Gets the error path.
   *
   * @return the error path
   */
  public String getErrorPath() {
    return "/error";
  }

}
