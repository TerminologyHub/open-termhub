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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.wci.termhub.rest.TerminologyServiceRestImpl;
import com.wci.termhub.syndication.SyndicationJobRunner;
import com.wci.termhub.syndication.SyndicationJobService;
import com.wci.termhub.syndication.SyndicationJobStatus;
import com.wci.termhub.util.PropertyUtility;

/**
 * Unit test for syndication endpoints in {@link TerminologyServiceRestImpl}.
 */
public class TerminologyServiceRestImplSyndicationUnitTest {

  /** The admin key. */
  private static final String ADMIN_KEY = "test-admin";

  /** The authorization header. */
  private static final String AUTHORIZATION = "Bearer " + ADMIN_KEY;

  /** The rest service. */
  private TerminologyServiceRestImpl restService;

  /** The job service. */
  private SyndicationJobService jobService;

  /** The job runner. */
  private SyndicationJobRunner jobRunner;

  /**
   * Sets up the test.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setUp() throws Exception {
    restService = new TerminologyServiceRestImpl();
    jobService = mock(SyndicationJobService.class);
    jobRunner = mock(SyndicationJobRunner.class);

    ReflectionTestUtils.setField(restService, "syndicationJobService", jobService);
    ReflectionTestUtils.setField(restService, "syndicationJobRunner", jobRunner);
    PropertyUtility.setProperty("admin.key", ADMIN_KEY);
  }

  /**
   * Cleans up the test.
   */
  @AfterEach
  public void tearDown() {
    PropertyUtility.setProperty("admin.key", "");
  }

  /**
   * Test invalid admin key.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSyndicateInvalidAdminKey() throws Exception {
    final ResponseEntity<SyndicationJobStatus> response = restService.syndicate("Bearer bad-key");

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(jobRunner, never()).runSyndication(org.mockito.ArgumentMatchers.anyString());
  }

  /**
   * Test syndication not configured.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSyndicateNotConfigured() throws Exception {
    when(jobService.isSyndicationConfigured()).thenReturn(false);

    final ResponseEntity<SyndicationJobStatus> response = restService.syndicate(AUTHORIZATION);

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    verify(jobRunner, never()).runSyndication(org.mockito.ArgumentMatchers.anyString());
  }

  /**
   * Test syndication in progress.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSyndicateInProgress() throws Exception {
    when(jobService.isSyndicationConfigured()).thenReturn(true);
    when(jobService.isSyndicationInProgress()).thenReturn(true);

    final ResponseEntity<SyndicationJobStatus> response = restService.syndicate(AUTHORIZATION);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    verify(jobRunner, never()).runSyndication(org.mockito.ArgumentMatchers.anyString());
  }

  /**
   * Test syndication accepted.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSyndicateAccepted() throws Exception {
    final SyndicationJobStatus status = new SyndicationJobStatus("process-id");
    when(jobService.isSyndicationConfigured()).thenReturn(true);
    when(jobService.isSyndicationInProgress()).thenReturn(false);
    when(jobService.startJob()).thenReturn(status);

    final ResponseEntity<SyndicationJobStatus> response = restService.syndicate(AUTHORIZATION);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("process-id", response.getBody().getProcessId());
    verify(jobRunner).runSyndication("process-id");
  }

  /**
   * Test status not found.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSyndicationStatusNotFound() throws Exception {
    when(jobService.getStatus("missing")).thenReturn(null);

    final ResponseEntity<SyndicationJobStatus> response =
        restService.getSyndicationStatus("missing", AUTHORIZATION);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  /**
   * Test status found.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSyndicationStatusFound() throws Exception {
    final SyndicationJobStatus status = new SyndicationJobStatus("process-id");
    when(jobService.getStatus("process-id")).thenReturn(status);

    final ResponseEntity<SyndicationJobStatus> response =
        restService.getSyndicationStatus("process-id", AUTHORIZATION);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(status, response.getBody());
  }
}
