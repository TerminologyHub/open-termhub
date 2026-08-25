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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wci.termhub.fhir.rest.r4.HapiR4RestfulServlet;
import com.wci.termhub.fhir.rest.r5.HapiR5RestfulServlet;

import ca.uhn.fhir.rest.api.EncodingEnum;

/**
 * Servlet registration bean.
 */
@Configuration
public class FHIRConfig {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(FHIRConfig.class);

  /** Normalized R4 context path, e.g. {@code /fhir/r4}. */
  private final String r4ContextPath;

  /** Normalized R5 context path, e.g. {@code /fhir/r5}. */
  private final String r5ContextPath;

  /** Servlet mapping for R4, e.g. {@code /fhir/r4/*}. */
  private final String r4Mapping;

  /** Servlet mapping for R5, e.g. {@code /fhir/r5/*}. */
  private final String r5Mapping;

  /**
   * Instantiates a new FHIR config.
   *
   * @param r4ContextPath the R4 context path
   * @param r5ContextPath the R5 context path
   */
  public FHIRConfig(@Value("${fhir.r4.context.path}") final String r4ContextPath,
    @Value("${fhir.r5.context.path}") final String r5ContextPath) {
    requireDistinct(r4ContextPath, r5ContextPath);
    this.r4ContextPath = normalizeContextPath(r4ContextPath);
    this.r5ContextPath = normalizeContextPath(r5ContextPath);
    this.r4Mapping = mappingPattern(this.r4ContextPath);
    this.r5Mapping = mappingPattern(this.r5ContextPath);
    logger.info("FHIR R4 context path: {}", this.r4ContextPath);
    logger.info("FHIR R5 context path: {}", this.r5ContextPath);
  }

  /**
   * Hapi R4.
   *
   * @return the servlet registration bean
   */
  @Bean
  public ServletRegistrationBean<HapiR4RestfulServlet> hapiR4() {
    final HapiR4RestfulServlet hapiServlet = new HapiR4RestfulServlet();

    final ServletRegistrationBean<HapiR4RestfulServlet> servletRegistrationBean =
        new ServletRegistrationBean<>(hapiServlet, r4Mapping);
    hapiServlet.setServerName("Open Termhub R4 FHIR Terminology Server");
    hapiServlet.setServerVersion(getClass().getPackage().getImplementationVersion());
    hapiServlet.setDefaultResponseEncoding(EncodingEnum.JSON);
    // servletRegistrationBean.setOrder(100);

    return servletRegistrationBean;
  }

  /**
   * Hapi R5.
   *
   * @return the servlet registration bean
   */
  @Bean
  public ServletRegistrationBean<HapiR5RestfulServlet> hapiR5() {
    final HapiR5RestfulServlet hapiServlet = new HapiR5RestfulServlet();

    final ServletRegistrationBean<HapiR5RestfulServlet> servletRegistrationBean =
        new ServletRegistrationBean<>(hapiServlet, r5Mapping);
    hapiServlet.setServerName("Open Termhub R5 FHIR Terminology Server");
    hapiServlet.setServerVersion(getClass().getPackage().getImplementationVersion());
    hapiServlet.setDefaultResponseEncoding(EncodingEnum.JSON);

    // servletRegistrationBean.setOrder(100);

    return servletRegistrationBean;
  }

  /**
   * Rewrites terminology Swagger description links to the configured FHIR context paths.
   *
   * @return the open api customizer
   */
  @Bean
  public OpenApiCustomizer fhirSwaggerLinkCustomizer() {
    return openApi -> {
      if (openApi.getInfo() == null) {
        return;
      }
      openApi.getInfo().setDescription(rewriteSwaggerLinks(openApi.getInfo().getDescription(),
          r4ContextPath, r5ContextPath));
    };
  }

  /**
   * Replaces default FHIR Swagger hrefs with the configured context paths.
   *
   * @param description the OpenAPI description
   * @param r4 the R4 context path
   * @param r5 the R5 context path
   * @return the rewritten description
   */
  public static String rewriteSwaggerLinks(final String description, final String r4,
    final String r5) {
    if (description == null) {
      return null;
    }
    return description
        .replace("/fhir/r4/swagger-ui/index.html",
            joinPath(normalizeContextPath(r4), "/swagger-ui/index.html"))
        .replace("/fhir/r5/swagger-ui/index.html",
            joinPath(normalizeContextPath(r5), "/swagger-ui/index.html"));
  }

  /**
   * Joins a base URL or path with a suffix without producing a double slash.
   *
   * @param base the base (may end with {@code /})
   * @param suffix the suffix (should start with {@code /})
   * @return the joined path
   */
  public static String joinPath(final String base, final String suffix) {
    final String path = suffix.startsWith("/") ? suffix : "/" + suffix;
    if (base == null || base.isEmpty() || "/".equals(base)) {
      return path;
    }
    if (base.endsWith("/")) {
      return base.substring(0, base.length() - 1) + path;
    }
    return base + path;
  }

  /**
   * Normalizes a FHIR context path: trim, require non-blank, no wildcards, leading slash, no
   * trailing slash (except {@code /}).
   *
   * @param path the path
   * @return the normalized path
   */
  public static String normalizeContextPath(final String path) {
    if (path == null || path.isBlank()) {
      throw new IllegalStateException("FHIR context path must not be blank");
    }
    String normalized = path.trim();
    if (normalized.indexOf('*') >= 0 || normalized.indexOf('?') >= 0) {
      throw new IllegalStateException(
          "FHIR context path must not contain wildcards: " + normalized);
    }
    if (!normalized.startsWith("/")) {
      normalized = "/" + normalized;
    }
    while (normalized.length() > 1 && normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }

  /**
   * Servlet url pattern for a context path.
   *
   * @param contextPath the context path
   * @return the mapping, e.g. {@code /fhir/r4/*}
   */
  public static String mappingPattern(final String contextPath) {
    final String normalized = normalizeContextPath(contextPath);
    if ("/".equals(normalized)) {
      return "/*";
    }
    return normalized + "/*";
  }

  /**
   * Fails if R4 and R5 context paths are the same after normalization.
   *
   * @param r4 the R4 context path
   * @param r5 the R5 context path
   */
  public static void requireDistinct(final String r4, final String r5) {
    final String n4 = normalizeContextPath(r4);
    final String n5 = normalizeContextPath(r5);
    if (n4.equals(n5)) {
      throw new IllegalStateException(
          "fhir.r4.context.path and fhir.r5.context.path must differ, both are " + n4);
    }
  }

}
