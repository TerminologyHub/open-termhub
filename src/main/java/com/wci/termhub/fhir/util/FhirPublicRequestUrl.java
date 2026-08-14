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
 * Builds public FHIR request URLs for Bundle {@code fullUrl} and paging links
 * when the server is behind a reverse proxy (e.g.
 * {@code https://fhir.example.org}).
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
   * Returns the configured public FHIR server base, or null if unset.
   *
   * @return the base without a trailing slash, or null
   */
  public static String configuredServerBase() {
    final String value = PropertyUtility.getProperty(PROXY_URL_PROPERTY);
    if (value == null || value.isBlank()) {
      return null;
    }
    return stripTrailingSlash(value.trim());
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
   * Rewrites {@code url} to the public FHIR base when configured or when
   * forwarded headers are present.
   *
   * @param request the request
   * @param url the internal request URL (may include query)
   * @return the public URL
   */
  public static String toPublic(final HttpServletRequest request, final String url) {
    if (url == null || url.isEmpty()) {
      return url;
    }
    final String publicOrigin = publicOrigin(request);
    if (publicOrigin != null) {
      return replaceOrigin(url, publicOrigin);
    }
    return url;
  }

  /**
   * Returns the HAPI server base. Honors {@code X-Forwarded-*} so Swagger/OpenAPI
   * stay on the host that served the page. Does not use {@code proxy.url.base}
   * (that applies only to Bundle {@code fullUrl} and paging links).
   *
   * @param request the request
   * @param fallback internal server base from the incoming request
   * @return the public server base
   */
  public static String publicServerBase(final HttpServletRequest request, final String fallback) {
    if (fallback == null || fallback.isEmpty()) {
      return fallback;
    }
    final String forwardedOrigin = forwardedOrigin(request);
    if (forwardedOrigin != null) {
      return replaceOrigin(fallback, forwardedOrigin);
    }
    return fallback;
  }

  /**
   * {@code X-Forwarded-*} origin when the request is proxied, else
   * {@code proxy.url.base}. Origin always includes a scheme.
   *
   * @param request the request
   * @return {@code scheme://host[:port]} or null
   */
  private static String publicOrigin(final HttpServletRequest request) {
    final String forwardedOrigin = forwardedOrigin(request);
    if (forwardedOrigin != null) {
      return forwardedOrigin;
    }
    return configuredOrigin(request);
  }

  /**
   * {@code proxy.url.base} as {@code scheme://host[:port]}. A host-only value
   * gets the request scheme.
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
   * Origin from {@code X-Forwarded-Proto} and {@code X-Forwarded-Host}, or
   * null.
   *
   * @param request the request
   * @return {@code scheme://host[:port]} or null
   */
  private static String forwardedOrigin(final HttpServletRequest request) {
    final String host = firstForwarded(request, "X-Forwarded-Host");
    if (host == null) {
      return null;
    }
    final String protoHeader = firstForwarded(request, "X-Forwarded-Proto");
    final String scheme = protoHeader != null ? protoHeader : request.getScheme();
    String hostPart = host;
    final String port = firstForwarded(request, "X-Forwarded-Port");
    if (port != null && hostPart.indexOf(':') < 0 && !isDefaultPort(scheme, port)) {
      hostPart = hostPart + ":" + port;
    }
    return scheme + "://" + hostPart;
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
   * First value of a possibly comma-separated forwarded header.
   *
   * @param request the request
   * @param name the header name
   * @return the value or null
   */
  private static String firstForwarded(final HttpServletRequest request, final String name) {
    final String raw = request.getHeader(name);
    if (raw == null || raw.isBlank()) {
      return null;
    }
    final int comma = raw.indexOf(',');
    final String value = (comma < 0 ? raw : raw.substring(0, comma)).trim();
    return value.isEmpty() ? null : value;
  }

  /**
   * Whether {@code port} is the default for {@code scheme}.
   *
   * @param scheme the scheme
   * @param port the port
   * @return true if default
   */
  private static boolean isDefaultPort(final String scheme, final String port) {
    return ("https".equalsIgnoreCase(scheme) && "443".equals(port))
        || ("http".equalsIgnoreCase(scheme) && "80".equals(port));
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
