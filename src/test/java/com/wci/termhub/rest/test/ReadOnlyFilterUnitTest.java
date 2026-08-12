/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.rest.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import com.wci.termhub.ReadOnlyMode;
import com.wci.termhub.rest.ReadOnlyFilter;

import jakarta.servlet.FilterChain;

/**
 * Unit tests for {@link ReadOnlyFilter}.
 */
public class ReadOnlyFilterUnitTest {

  /** The filter. */
  private ReadOnlyFilter filter;

  /** The read only mode. */
  private ReadOnlyMode readOnlyMode;

  /** The chain. */
  private FilterChain chain;

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
    filter = new ReadOnlyFilter();
    readOnlyMode = mock(ReadOnlyMode.class);
    chain = mock(FilterChain.class);
    ReflectionTestUtils.setField(filter, "readOnlyMode", readOnlyMode);
  }

  /**
   * Test is mutating post paths.
   */
  @Test
  public void testIsMutatingPost() {
    assertTrue(ReadOnlyFilter.isMutatingPost("/terminology/admin"));
    assertTrue(ReadOnlyFilter.isMutatingPost("/syndicate"));
    assertTrue(ReadOnlyFilter.isMutatingPost("/terminology"));
    assertTrue(ReadOnlyFilter.isMutatingPost("/concept/SNOMEDCT_US/trees"));
    assertTrue(ReadOnlyFilter.isMutatingPost("/fhir/CodeSystem/$load"));
    assertTrue(ReadOnlyFilter.isMutatingPost("/fhir/r4"));
    assertTrue(ReadOnlyFilter.isMutatingPost("/fhir/r5"));
    assertTrue(ReadOnlyFilter.isMutatingPost("/fhir/r4/CodeSystem"));
    assertTrue(ReadOnlyFilter.isMutatingPost("/fhir/r5/ValueSet"));

    assertFalse(ReadOnlyFilter.isMutatingPost("/concept/bulk"));
    assertFalse(ReadOnlyFilter.isMutatingPost("/fhir/r4/CodeSystem/$lookup"));
    assertFalse(ReadOnlyFilter.isMutatingPost("/fhir/r4/ValueSet/$expand"));
    assertFalse(ReadOnlyFilter.isMutatingPost("/terminology/health"));
  }

  /**
   * Passes through when read-only is disabled.
   *
   * @throws Exception the exception
   */
  @Test
  public void testPassThroughWhenDisabled() throws Exception {
    when(readOnlyMode.isEnabled()).thenReturn(false);
    final MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/terminology/abc");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertEquals(200, response.getStatus());
  }

  /**
   * Allows GET when read-only is enabled.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAllowsGetWhenEnabled() throws Exception {
    when(readOnlyMode.isEnabled()).thenReturn(true);
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/terminology");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  /**
   * Rejects DELETE when read-only is enabled.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRejectsDeleteWhenEnabled() throws Exception {
    when(readOnlyMode.isEnabled()).thenReturn(true);
    final MockHttpServletRequest request =
        new MockHttpServletRequest("DELETE", "/terminology/abc123");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    verify(chain, never()).doFilter(request, response);
    assertEquals(403, response.getStatus());
    assertEquals(ReadOnlyFilter.FORBIDDEN_MESSAGE, response.getContentAsString());
  }

  /**
   * Rejects mutating POST when read-only is enabled.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRejectsMutatingPostWhenEnabled() throws Exception {
    when(readOnlyMode.isEnabled()).thenReturn(true);
    final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/syndicate");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    verify(chain, never()).doFilter(request, response);
    assertEquals(403, response.getStatus());
  }

  /**
   * Allows concept bulk POST when read-only is enabled.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAllowsConceptBulkPostWhenEnabled() throws Exception {
    when(readOnlyMode.isEnabled()).thenReturn(true);
    final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/concept/bulk");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  /**
   * Allows FHIR lookup POST when read-only is enabled.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAllowsFhirLookupPostWhenEnabled() throws Exception {
    when(readOnlyMode.isEnabled()).thenReturn(true);
    final MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/fhir/r4/CodeSystem/$lookup");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }
}
