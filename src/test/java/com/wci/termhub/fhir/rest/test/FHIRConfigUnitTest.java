/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.rest.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.rest.FHIRConfig;

/**
 * Unit tests for {@link FHIRConfig} context path helpers.
 */
public class FHIRConfigUnitTest {

  /**
   * Default paths map to historical servlet patterns.
   */
  @Test
  public void testDefaultMappings() {
    assertEquals("/fhir/r4/*", FHIRConfig.mappingPattern("/fhir/r4"));
    assertEquals("/fhir/r5/*", FHIRConfig.mappingPattern("/fhir/r5"));
    FHIRConfig.requireDistinct("/fhir/r4", "/fhir/r5");
  }

  /**
   * Trailing slashes collide with the same path.
   */
  @Test
  public void testTrailingSlashCollision() {
    final IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> FHIRConfig.requireDistinct("/fhir/r4", "/fhir/r4/"));
    assertEquals("fhir.r4.context.path and fhir.r5.context.path must differ, both are /fhir/r4",
        ex.getMessage());
  }

  /**
   * Identical custom values fail.
   */
  @Test
  public void testIdenticalCustomValues() {
    assertThrows(IllegalStateException.class,
        () -> FHIRConfig.requireDistinct("/terminology", "/terminology"));
  }

  /**
   * Distinct custom values map correctly.
   */
  @Test
  public void testDistinctCustomValues() {
    FHIRConfig.requireDistinct("/r4", "/r5");
    assertEquals("/r4/*", FHIRConfig.mappingPattern("/r4"));
    assertEquals("/r5/*", FHIRConfig.mappingPattern("/r5"));
    assertEquals("/custom/r4/*", FHIRConfig.mappingPattern("custom/r4/"));
  }

  /**
   * Blank path fails rather than mapping {@code /*}.
   */
  @Test
  public void testBlankPath() {
    assertThrows(IllegalStateException.class, () -> FHIRConfig.normalizeContextPath(""));
    assertThrows(IllegalStateException.class, () -> FHIRConfig.normalizeContextPath("   "));
    assertThrows(IllegalStateException.class, () -> FHIRConfig.normalizeContextPath(null));
  }

  /**
   * Wildcard characters in a context path fail.
   */
  @Test
  public void testWildcardPath() {
    assertThrows(IllegalStateException.class, () -> FHIRConfig.normalizeContextPath("/fhir/*"));
    assertThrows(IllegalStateException.class, () -> FHIRConfig.normalizeContextPath("/fhir/r4*"));
    assertThrows(IllegalStateException.class, () -> FHIRConfig.normalizeContextPath("*"));
    assertThrows(IllegalStateException.class, () -> FHIRConfig.normalizeContextPath("/fhir/r?"));
    assertThrows(IllegalStateException.class, () -> FHIRConfig.mappingPattern("/fhir/r4/*"));
  }

  /**
   * Terminology Swagger description hrefs follow configured FHIR paths.
   */
  @Test
  public void testRewriteSwaggerLinks() {
    final String description =
        "<p>Also see <a href=\"/fhir/r4/swagger-ui/index.html\">FHIR R4 API</a></p>"
            + "<p>Also see <a href=\"/fhir/r5/swagger-ui/index.html\">FHIR R5 API</a></p>";
    final String rewritten = FHIRConfig.rewriteSwaggerLinks(description, "/nuno/4", "/nuno/5");
    assertEquals("<p>Also see <a href=\"/nuno/4/swagger-ui/index.html\">FHIR R4 API</a></p>"
        + "<p>Also see <a href=\"/nuno/5/swagger-ui/index.html\">FHIR R5 API</a></p>", rewritten);
    assertEquals(description, FHIRConfig.rewriteSwaggerLinks(description, "/fhir/r4", "/fhir/r5"));
  }

  /**
   * Root context path maps to {@code /*} and does not produce a double slash.
   */
  @Test
  public void testRootContextPath() {
    assertEquals("/*", FHIRConfig.mappingPattern("/"));
    assertEquals("/swagger-ui/index.html",
        FHIRConfig.joinPath("/", "/swagger-ui/index.html"));
    assertEquals("http://localhost:8080/swagger-ui/",
        FHIRConfig.joinPath("http://localhost:8080/", "/swagger-ui/"));
    final String description =
        "<p>Also see <a href=\"/fhir/r4/swagger-ui/index.html\">FHIR R4 API</a></p>"
            + "<p>Also see <a href=\"/fhir/r5/swagger-ui/index.html\">FHIR R5 API</a></p>";
    final String rewritten = FHIRConfig.rewriteSwaggerLinks(description, "/", "/fhir/r5");
    assertEquals("<p>Also see <a href=\"/swagger-ui/index.html\">FHIR R4 API</a></p>"
        + "<p>Also see <a href=\"/fhir/r5/swagger-ui/index.html\">FHIR R5 API</a></p>", rewritten);
  }

}
