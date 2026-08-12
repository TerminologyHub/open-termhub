/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.lucene.eventing.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.wci.termhub.ReadOnlyMode;
import com.wci.termhub.lucene.eventing.WriteAspect;
import com.wci.termhub.rest.ReadOnlyFilter;

import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;

/**
 * Unit tests for {@link WriteAspect}.
 */
public class WriteAspectUnitTest {

  /** The aspect. */
  private WriteAspect aspect;

  /** The read only mode. */
  private ReadOnlyMode readOnlyMode;

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
    aspect = new WriteAspect();
    readOnlyMode = mock(ReadOnlyMode.class);
    ReflectionTestUtils.setField(aspect, "readOnlyMode", readOnlyMode);
  }

  /**
   * Rejects writes when read-only mode is enabled.
   *
   * @throws Throwable the throwable
   */
  @Test
  public void testRejectsWhenReadOnly() throws Throwable {
    when(readOnlyMode.isEnabled()).thenReturn(true);
    final ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);

    final BaseServerResponseException ex = assertThrows(BaseServerResponseException.class,
        () -> aspect.aroundWrite(pjp));
    assertEquals(403, ex.getStatusCode());
    assertEquals(ReadOnlyFilter.FORBIDDEN_MESSAGE, ex.getMessage());
  }

  /**
   * Proceeds when read-only mode is disabled.
   *
   * @throws Throwable the throwable
   */
  @Test
  public void testProceedsWhenNotReadOnly() throws Throwable {
    when(readOnlyMode.isEnabled()).thenReturn(false);
    final ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    when(pjp.proceed()).thenReturn("ok");

    assertEquals("ok", aspect.aroundWrite(pjp));
  }
}
