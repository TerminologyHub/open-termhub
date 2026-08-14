/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.fhir.util.FhirPublicRequestUrl;
import com.wci.termhub.util.PropertyUtility;

import ca.uhn.fhir.rest.param.NumberParam;

/**
 * Unit tests for {@link FhirPublicRequestUrl} and Bundle fullUrl/paging links.
 */
public class FhirPublicRequestUrlUnitTest {

  /** The public origin. */
  private static final String PUBLIC_ORIGIN = "https://fhir.example.org";

  /**
   * Restore proxy.url.base after each test.
   */
  @AfterEach
  public void restoreBase() {
    PropertyUtility.setProperty(FhirPublicRequestUrl.PROXY_URL_PROPERTY, "");
  }

  /**
   * Configured proxy.url.base rewrites host/scheme and keeps /fhir/r4.
   */
  @Test
  public void testConfiguredBaseRewritesOrigin() {
    PropertyUtility.setProperty(FhirPublicRequestUrl.PROXY_URL_PROPERTY, PUBLIC_ORIGIN);
    final MockHttpServletRequest request = searchRequest();

    assertEquals(PUBLIC_ORIGIN + "/fhir/r4/ValueSet?_count=1",
        FhirPublicRequestUrl.forRequest(request));
    assertEquals(PUBLIC_ORIGIN + "/fhir/r4/ValueSet",
        FhirPublicRequestUrl.forRequestPath(request));
  }

  /**
   * X-Forwarded-Host rewrites origin and keeps /fhir/r4.
   */
  @Test
  public void testForwardedHostRewritesOrigin() {
    final MockHttpServletRequest request = searchRequest();
    request.addHeader("X-Forwarded-Host", "fhir.example.org");
    request.addHeader("X-Forwarded-Proto", "https");

    assertEquals(PUBLIC_ORIGIN + "/fhir/r4/ValueSet?_count=1",
        FhirPublicRequestUrl.forRequest(request));
  }

  /**
   * No proxy config leaves the incoming request URL unchanged.
   */
  @Test
  public void testPassthroughWithoutProxy() {
    final MockHttpServletRequest request = searchRequest();
    assertEquals("http://localhost:8080/fhir/r4/ValueSet?_count=1",
        FhirPublicRequestUrl.forRequest(request));
  }

  /**
   * makeBundle fullUrl, next, and previous use the configured public origin.
   */
  @Test
  public void testMakeBundleUsesPublicBase() {
    PropertyUtility.setProperty(FhirPublicRequestUrl.PROXY_URL_PROPERTY, PUBLIC_ORIGIN);
    final MockHttpServletRequest request = searchRequest();
    request.setQueryString("_count=1&_offset=1");
    request.setPathInfo("/ValueSet");

    final List<ValueSet> list = List.of(valueSet("vs-0"), valueSet("vs-1"), valueSet("vs-2"));
    final Bundle bundle =
        FhirUtilityR4.makeBundle(request, list, new NumberParam(1), new NumberParam(1));

    assertEquals(PUBLIC_ORIGIN + "/fhir/r4/ValueSet?_count=1&_offset=1",
        bundle.getLink("self").getUrl());
    assertTrue(bundle.getLink("previous").getUrl()
        .startsWith(PUBLIC_ORIGIN + "/fhir/r4/ValueSet"));
    assertTrue(bundle.getLink("next").getUrl().startsWith(PUBLIC_ORIGIN + "/fhir/r4/ValueSet"));
    assertEquals(PUBLIC_ORIGIN + "/fhir/r4/ValueSet/vs-1",
        bundle.getEntry().get(0).getFullUrl());
  }

  /**
   * Host-only proxy.url.base gets the request scheme.
   */
  @Test
  public void testHostOnlyConfiguredBaseGetsScheme() {
    PropertyUtility.setProperty(FhirPublicRequestUrl.PROXY_URL_PROPERTY, "fhir.example.org");
    final MockHttpServletRequest request = searchRequest();
    assertEquals("http://fhir.example.org/fhir/r4/ValueSet?_count=1",
        FhirPublicRequestUrl.forRequest(request));
  }

  /**
   * X-Forwarded-Host wins over proxy.url.base and keeps a non-default port.
   */
  @Test
  public void testForwardedHostWinsOverConfiguredBase() {
    PropertyUtility.setProperty(FhirPublicRequestUrl.PROXY_URL_PROPERTY, PUBLIC_ORIGIN);
    final MockHttpServletRequest request = searchRequest();
    request.addHeader("X-Forwarded-Host", "fhir.example.org:8100");
    request.addHeader("X-Forwarded-Proto", "http");
    assertEquals("http://fhir.example.org:8100/fhir/r4/ValueSet?_count=1",
        FhirPublicRequestUrl.forRequest(request));
  }

  /**
   * HAPI/Swagger server base ignores proxy.url.base and uses X-Forwarded-Host.
   */
  @Test
  public void testPublicServerBaseIgnoresConfiguredUsesForwardedHost() {
    PropertyUtility.setProperty(FhirPublicRequestUrl.PROXY_URL_PROPERTY, PUBLIC_ORIGIN);
    final MockHttpServletRequest request = searchRequest();
    final String fallback = "http://localhost:8080/fhir/r4";

    assertEquals(fallback, FhirPublicRequestUrl.publicServerBase(request, fallback));

    request.addHeader("X-Forwarded-Host", "fhir.example.org");
    request.addHeader("X-Forwarded-Proto", "https");
    assertEquals(PUBLIC_ORIGIN + "/fhir/r4",
        FhirPublicRequestUrl.publicServerBase(request, fallback));
  }

  /**
   * Search request against the R4 ValueSet servlet mapping.
   *
   * @return the request
   */
  private static MockHttpServletRequest searchRequest() {
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/fhir/r4/ValueSet");
    request.setScheme("http");
    request.setServerName("localhost");
    request.setServerPort(8080);
    request.setRequestURI("/fhir/r4/ValueSet");
    request.setServletPath("/fhir/r4");
    request.setPathInfo("/ValueSet");
    request.setQueryString("_count=1");
    return request;
  }

  /**
   * ValueSet with id.
   *
   * @param id the id
   * @return the value set
   */
  private static ValueSet valueSet(final String id) {
    final ValueSet vs = new ValueSet();
    vs.setId(id);
    return vs;
  }
}
