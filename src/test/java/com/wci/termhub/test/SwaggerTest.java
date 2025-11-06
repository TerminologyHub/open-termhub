/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
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

import java.util.stream.Stream;

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
}
