/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.util;

import com.wci.termhub.util.PropertyUtility;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Rewrites Bundle {@code fullUrl} and paging links to the public origin from
 * {@code proxy.url.base} ({@code PROXY_URL}) when set.
 */
public final class FhirPublicRequestUrl {

  /** Public FHIR server base, e.g. {@code https://fhir.example.org}. */
  public static final String PROXY_URL_PROPERTY = "proxy.url.base";

  /**
   * Instantiates a new {@link FhirPublicRequestUrl}.
   */
  private FhirPublicRequestUrl() {
    // n/a
  }

  /**
   * Returns the configured public FHIR origin (scheme + host[:port]), or null
   * if unset. Path components in {@code PROXY_URL} are ignored.
   *
   * @return the origin without a trailing slash, or null
   */
  public static String configuredServerBase() {
    final String value = PropertyUtility.getProperty(PROXY_URL_PROPERTY);
    if (value == null || value.isBlank()) {
      return null;
    }
    return toOrigin(value.trim());
  }

  /**
   * Returns the public form of the current request URL, including query string.
   *
   * @param request the request
   * @return the public request URL
   */
  public static String forRequest(final HttpServletRequest request) {
    final String original = request.getRequestURL().toString();
    final String query = request.getQueryString();
    final String withQuery = query == null ? original : original + "?" + query;
    return toPublic(request, withQuery);
  }

  /**
   * Returns the public request path (no query, no trailing slash) used as the
   * {@code fullUrl} prefix.
   *
   * @param request the request
   * @return the public path
   */
  public static String forRequestPath(final HttpServletRequest request) {
    return stripTrailingSlash(toPublic(request, request.getRequestURL().toString()));
  }

  /**
   * Rewrites {@code url} to the configured public origin when {@code PROXY_URL}
   * is set; otherwise returns {@code url} unchanged.
   *
   * @param request the request (used for scheme when PROXY_URL is host-only)
   * @param url the internal request URL (may include query)
   * @return the public URL
   */
  public static String toPublic(final HttpServletRequest request, final String url) {
    if (url == null || url.isEmpty()) {
      return url;
    }
    final String publicOrigin = configuredOrigin(request);
    if (publicOrigin != null) {
      return replaceOrigin(url, publicOrigin);
    }
    return url;
  }

  /**
   * {@code proxy.url.base} as {@code scheme://host[:port]}. A host-only value
   * gets the request scheme. Path after the host is dropped.
   *
   * @param request the request
   * @return the origin or null
   */
  private static String configuredOrigin(final HttpServletRequest request) {
    final String configured = configuredServerBase();
    if (configured == null) {
      return null;
    }
    if (configured.contains("://")) {
      return configured;
    }
    return request.getScheme() + "://" + configured;
  }

  /**
   * Keeps only {@code scheme://host[:port]} (or host[:port] if no scheme).
   *
   * @param value the configured PROXY_URL
   * @return origin without trailing slash
   */
  private static String toOrigin(final String value) {
    final String schemeSep = "://";
    final int schemeIdx = value.indexOf(schemeSep);
    if (schemeIdx < 0) {
      final int slash = value.indexOf('/');
      final String host = slash < 0 ? value : value.substring(0, slash);
      return stripTrailingSlash(host);
    }
    final int pathStart = value.indexOf('/', schemeIdx + schemeSep.length());
    if (pathStart < 0) {
      return stripTrailingSlash(value);
    }
    return value.substring(0, pathStart);
  }

  /**
   * Replaces {@code scheme://host[:port]} of {@code url} with {@code origin}.
   *
   * @param url the URL
   * @param origin the public origin
   * @return the rewritten URL
   */
  private static String replaceOrigin(final String url, final String origin) {
    final String schemeSep = "://";
    final int schemeIdx = url.indexOf(schemeSep);
    if (schemeIdx < 0) {
      return url;
    }
    final int pathStart = url.indexOf('/', schemeIdx + schemeSep.length());
    if (pathStart < 0) {
      return origin;
    }
    return origin + url.substring(pathStart);
  }

  /**
   * Strips a trailing slash.
   *
   * @param value the value
   * @return the value without a trailing slash
   */
  private static String stripTrailingSlash(final String value) {
    if (value != null && value.endsWith("/") && value.length() > 1) {
      return value.substring(0, value.length() - 1);
    }
    return value;
  }
}
