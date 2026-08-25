/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.rest;

import java.io.IOException;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.wci.termhub.ReadOnlyMode;
import com.wci.termhub.fhir.rest.FHIRConfig;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rejects HTTP APIs that add, alter, or remove content when read-only mode is enabled.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ReadOnlyFilter implements Filter {

  /** Forbidden response message. */
  public static final String FORBIDDEN_MESSAGE =
      "Read-only mode: content mutations are disabled";

  /** Tree compute POST: /concept/{terminology}/trees. */
  private static final Pattern CONCEPT_TREES_POST =
      Pattern.compile("^/concept/[^/]+/trees$");

  /** FHIR R4 context path. Default for unit tests without Spring injection. */
  @Value("${fhir.r4.context.path}")
  private String r4ContextPath = "/fhir/r4";

  /** FHIR R5 context path. Default for unit tests without Spring injection. */
  @Value("${fhir.r5.context.path}")
  private String r5ContextPath = "/fhir/r5";

  /** FHIR create POST: {r4|r5 context path}/{ResourceType}. */
  private Pattern fhirCreatePost;

  /** The read only mode. */
  @Autowired
  private ReadOnlyMode readOnlyMode;

  /**
   * Do filter.
   *
   * @param req the req
   * @param res the res
   * @param chain the chain
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  /* see superclass */
  @Override
  public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
    throws IOException, ServletException {

    if (readOnlyMode == null || !readOnlyMode.isEnabled()) {
      chain.doFilter(req, res);
      return;
    }

    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;
    final String method = request.getMethod();

    if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)
        || "OPTIONS".equalsIgnoreCase(method)) {
      chain.doFilter(req, res);
      return;
    }

    if ("PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)
        || "DELETE".equalsIgnoreCase(method)) {
      reject(response);
      return;
    }

    if ("POST".equalsIgnoreCase(method) && isMutatingPost(normalizePath(request))) {
      reject(response);
      return;
    }

    chain.doFilter(req, res);
  }

  /**
   * Indicates whether the POST path mutates content.
   *
   * @param path the normalized path
   * @return true, if mutating
   */
  public boolean isMutatingPost(final String path) {
    if ("/terminology/admin".equals(path) || path.startsWith("/terminology/admin/")) {
      return true;
    }
    if ("/syndicate".equals(path)) {
      return true;
    }
    if ("/terminology".equals(path)) {
      return true;
    }
    if (CONCEPT_TREES_POST.matcher(path).matches()) {
      return true;
    }
    if (path.contains("/$load")) {
      return true;
    }
    final String r4 = FHIRConfig.normalizeContextPath(r4ContextPath);
    final String r5 = FHIRConfig.normalizeContextPath(r5ContextPath);
    if (r4.equals(path) || r5.equals(path)) {
      return true;
    }
    if (getFhirCreatePost().matcher(path).matches()) {
      return true;
    }
    return false;
  }

  /**
   * FHIR create POST pattern for the configured context paths.
   *
   * @return the pattern
   */
  private Pattern getFhirCreatePost() {
    if (fhirCreatePost == null) {
      final String r4 = FHIRConfig.normalizeContextPath(r4ContextPath);
      final String r5 = FHIRConfig.normalizeContextPath(r5ContextPath);
      fhirCreatePost = Pattern.compile(
          "^(" + Pattern.quote(r4) + "|" + Pattern.quote(r5) + ")/[A-Za-z][A-Za-z0-9]*$");
    }
    return fhirCreatePost;
  }

  /**
   * Normalize request path (strip context path and trailing slash).
   *
   * @param request the request
   * @return the path
   */
  static String normalizePath(final HttpServletRequest request) {
    String path = request.getRequestURI();
    final String contextPath = request.getContextPath();
    if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
      path = path.substring(contextPath.length());
    }
    if (path.isEmpty()) {
      path = "/";
    }
    if (path.length() > 1 && path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    return path;
  }

  /**
   * Reject with 403.
   *
   * @param response the response
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static void reject(final HttpServletResponse response) throws IOException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("text/plain;charset=UTF-8");
    response.getWriter().write(FORBIDDEN_MESSAGE);
  }
}
