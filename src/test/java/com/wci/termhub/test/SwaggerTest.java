/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.wci.termhub.util.PropertyUtility;

import io.swagger.v3.core.util.Yaml;

/**
 * Test for swagger urls.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class SwaggerTest {

  /** The port. */
  @LocalServerPort
  private int port;

  /** The rest template. */
  @Autowired
  private TestRestTemplate restTemplate;

  /**
   * Swagger paths.
   *
   * @return the stream
   */
  static Stream<String> swaggerPaths() {
    return Stream.of(
        // Native swagger
        "/swagger-ui/index.html", "/swagger-ui/swagger-ui.css", "/swagger-ui/index.css",
        "/swagger-ui/swagger-ui-bundle.js", "/swagger-ui/swagger-ui-standalone-preset.js",
        "/swagger-ui/swagger-initializer.js", "/v3/api-docs/swagger-config", "/v3/api-docs",
        // FHIR R4
        "/fhir/r4/swagger-ui/index.html", "/fhir/r4/swagger-ui/swagger-ui.css",
        "/fhir/r4/swagger-ui/index.css", "/fhir/r4/swagger-ui/termhub.png",
        "/fhir/r4/swagger-ui/swagger-ui-bundle.js", "/fhir/r4/api-docs",
        // FHIR R5
        "/fhir/r5/swagger-ui/index.html", "/fhir/r5/swagger-ui/swagger-ui.css",
        "/fhir/r5/swagger-ui/index.css", "/fhir/r5/swagger-ui/termhub.png",
        "/fhir/r5/swagger-ui/swagger-ui-bundle.js", "/fhir/r5/api-docs");
  }

  /**
   * Test swagger path loads.
   *
   * @param path the path
   */
  @ParameterizedTest
  @MethodSource("swaggerPaths")
  public void testSwaggerPathLoads(final String path) {
    final String url = "http://localhost:" + port + path;
    final ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Path: " + path);
    assertNotNull(response.getBody(), "Body should not be null for: " + path);
    assertFalse(response.getBody().length == 0, "Body should not be empty for: " + path);
  }

  /**
   * In default server mode, the R4/R5 swagger page loads with the expected base URL and the
   * api-docs retrieves as expected (mutating operations present).
   *
   * @throws Exception the exception
   */
  @Test
  public void testDefaultModeSwaggerAndApiDocs() throws Exception {
    final String origMode = PropertyUtility.getServerMode();
    try {
      PropertyUtility.setProperty("server.mode", "default");
      for (final String version : new String[] {"r4", "r5"}) {
        final JsonNode apiDocs = verifySwaggerAndApiDocs(version);
        // default mode is NOT read-only, so mutating operations remain in the docs
        assertTrue(apiDocsHasMethod(apiDocs, "post") || apiDocsHasMethod(apiDocs, "delete"),
            version + " default mode api-docs should expose mutating operations");
      }
    } finally {
      PropertyUtility.setProperty("server.mode", origMode);
    }
  }

  /**
   * In regenstrief server mode, the R4/R5 swagger page loads with the expected base URL and the
   * api-docs retrieves without any POST, PUT, PATCH, or DELETE operations (read-only).
   *
   * @throws Exception the exception
   */
  @Test
  public void testRegenstriefModeApiDocsIsReadOnly() throws Exception {
    final String origMode = PropertyUtility.getServerMode();
    try {
      PropertyUtility.setProperty("server.mode", "regenstrief");
      for (final String version : new String[] {"r4", "r5"}) {
        final JsonNode apiDocs = verifySwaggerAndApiDocs(version);
        // regenstrief mode is read-only, so no mutating operations may appear
        for (final String method : new String[] {"post", "put", "patch", "delete"}) {
          assertFalse(apiDocsHasMethod(apiDocs, method),
              version + " regenstrief mode api-docs must not expose " + method + " operations");
        }
        // ...but read (GET) operations must still be present
        assertTrue(apiDocsHasMethod(apiDocs, "get"),
            version + " regenstrief mode api-docs should still expose GET operations");
      }
    } finally {
      PropertyUtility.setProperty("server.mode", origMode);
    }
  }

  /**
   * Verifies the swagger page loads and references the expected base URL, and that the api-docs
   * retrieves with the expected {@code servers} base URL. Returns the parsed api-docs.
   *
   * @param version the FHIR version path segment (e.g. {@code r4})
   * @return the parsed api-docs document
   * @throws Exception the exception
   */
  private JsonNode verifySwaggerAndApiDocs(final String version) throws Exception {
    final String base = "http://localhost:" + port + "/fhir/" + version;

    // Swagger page loads and points at the expected api-docs URL
    final ResponseEntity<String> page =
        restTemplate.getForEntity(base + "/swagger-ui/index.html", String.class);
    assertEquals(HttpStatus.OK, page.getStatusCode(), version + " swagger page status");
    assertNotNull(page.getBody(), version + " swagger page body");
    assertTrue(page.getBody().contains(base + "/api-docs"),
        version + " swagger page should reference base url " + base + "/api-docs");

    // api-docs retrieves and declares the expected server base URL
    final ResponseEntity<String> docs =
        restTemplate.getForEntity(base + "/api-docs", String.class);
    assertEquals(HttpStatus.OK, docs.getStatusCode(), version + " api-docs status");
    assertNotNull(docs.getBody(), version + " api-docs body");

    final JsonNode apiDocs = Yaml.mapper().readTree(docs.getBody());
    assertEquals(base, apiDocs.at("/servers/0/url").asText(),
        version + " api-docs server base url");
    return apiDocs;
  }

  /**
   * Indicates whether any path in the api-docs declares the given HTTP method.
   *
   * @param apiDocs the parsed api-docs document
   * @param method the lower-case HTTP method (e.g. {@code post})
   * @return true, if any path declares the method
   */
  private static boolean apiDocsHasMethod(final JsonNode apiDocs, final String method) {
    final JsonNode paths = apiDocs.get("paths");
    if (paths == null) {
      return false;
    }
    for (final Iterator<JsonNode> it = paths.elements(); it.hasNext();) {
      if (it.next().has(method)) {
        return true;
      }
    }
    return false;
  }
}
