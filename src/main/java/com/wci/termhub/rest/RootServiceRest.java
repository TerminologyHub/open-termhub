/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.rest;

import org.springframework.http.ResponseEntity;

import com.wci.termhub.model.HealthCheck;

/**
 * Cross-cutting services that should belong to all REST APIs.
 */
public interface RootServiceRest {

  /**
   * Health.
   *
   * @param dependencies the dependencies
   * @return the health check
   * @throws Exception the exception
   */
  public ResponseEntity<HealthCheck> health(final Boolean dependencies) throws Exception;

  /**
   * Hook for admin tasks. STandard tasks include: reindexing, clearing caches,
   * etc.
   *
   * @param task the task
   * @param adminKey the admin key
   * @param background the background
   * @param payload the payload
   * @return the response
   * @throws Exception the exception
   */
  public ResponseEntity<?> admin(String task, String adminKey, Boolean background, String payload)
    throws Exception;

}
