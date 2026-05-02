/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r4.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Validates
 * {@link ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor}
 * behavior: browser-style {@code Accept} yields HTML instead of prompting for
 * an XML download, while generic or programmatic clients receive FHIR JSON or
 * XML according to negotiation.
 */
@AutoConfigureMockMvc
public class ResponseHighlighterFhirR4RestTest extends AbstractFhirR4ServerTest {

  /**
   * Chrome-like {@code Accept} from a typical desktop browser navigation
   * request.
   */
  private static final String CHROME_LIKE_ACCEPT =
      "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8";

  /** The port. */
  @LocalServerPort
  private int port;

  /** REST client. */
  @Autowired
  private TestRestTemplate restTemplate;

  /**
   * Browser navigation style request should produce HTML highlighter output
   * (text/html).
   */
  @Test
  public void browserLikeAcceptReturnsHtmlHighlighterMetadata() {

    final HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.ACCEPT, CHROME_LIKE_ACCEPT);
    final ResponseEntity<String> response = restTemplate.exchange(metadataUrl(), HttpMethod.GET,
        new HttpEntity<>(headers), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getHeaders().getContentType());

    final String contentTypeHeader =
        response.getHeaders().getContentType().toString().toLowerCase();
    assertTrue(contentTypeHeader.contains("text/html"),
        "unexpected Content-Type: " + contentTypeHeader);

    final String body = response.getBody();
    assertNotNull(body);
    final String trimmed = body.trim().toLowerCase();
    assertTrue(trimmed.startsWith("<!DOCTYPE html") || trimmed.startsWith("<html"),
        "expected wrapped HTML document from ResponseHighlighterInterceptor");
  }

  /**
   * Typical API client ({@code Accept: *\/*}) should receive FHIR JSON, not
   * negotiated XML from browser {@code application/xml}.
   */
  @Test
  public void wildcardAcceptReturnsFhirJsonMetadata() {

    final HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
    final ResponseEntity<String> response = restTemplate.exchange(metadataUrl(), HttpMethod.GET,
        new HttpEntity<>(headers), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    assertNotNull(response.getHeaders().getContentType(), "missing Content-Type");
    final String contentTypeHeader =
        response.getHeaders().getContentType().toString().toLowerCase();
    assertTrue(contentTypeHeader.contains("fhir+json"),
        "unexpected Content-Type: " + contentTypeHeader);

    final String body = response.getBody();
    assertNotNull(body);
    assertTrue(body.stripLeading().startsWith("{"), "expected JSON CapabilityStatement wrapper");
  }

  /**
   * Explicit FHIR/XML preference without HTML should negotiate XML raw body
   * (Highlighter inactive).
   */
  @Test
  public void explicitXmlAcceptReturnsFhirXmlMetadata() {

    final HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.ACCEPT, "application/xml");
    final ResponseEntity<String> response = restTemplate.exchange(metadataUrl(), HttpMethod.GET,
        new HttpEntity<>(headers), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    assertNotNull(response.getHeaders().getContentType());
    final String contentTypeHeader =
        response.getHeaders().getContentType().toString().toLowerCase();
    assertTrue(
        contentTypeHeader.contains("fhir+xml") || contentTypeHeader.contains("application/xml"),
        "unexpected Content-Type: " + contentTypeHeader);

    final String body = response.getBody();
    assertNotNull(body);
    final String trimmed = body.stripLeading();
    assertTrue(trimmed.startsWith("<"));
  }

  /**
   * Metadata url.
   *
   * @return the string
   */
  private String metadataUrl() {
    return "http://localhost:" + port + "/fhir/r4/metadata";
  }
}
