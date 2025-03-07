/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.rest.client;

import com.wci.termhub.model.Configurable;
import com.wci.termhub.model.HealthCheck;

/**
 * Generically represents all REST clients.
 */
public interface RootClient extends Configurable {

  /**
   * Returns the api url.
   *
   * @return the api url
   */
  public String getApiUrl();

  /**
   * Sets the api url.
   *
   * @param apiUrl the api url
   */
  public void setApiUrl(String apiUrl);

  /**
   * Returns the api url internal.
   *
   * @return the api url internal
   */
  public String getApiUrlInternal();

  /**
   * Sets the api url internal.
   *
   * @param apiUrlInternal the api url internal
   */
  public void setApiUrlInternal(String apiUrlInternal);

  /**
   * Indicates whether or not internal is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isInternal();

  /**
   * Sets the internal.
   *
   * @param internal the internal
   */
  public void setInternal(boolean internal);

  /**
   * Admin.
   *
   * @param task the task
   * @param adminKey the admin key
   * @param payload the payload
   * @throws Exception the exception
   */
  public void admin(String task, String adminKey, String payload) throws Exception;

  /**
   * Health.
   *
   * @param dependencies the dependencies
   * @return the health check
   * @throws Exception the exception
   */
  public HealthCheck health(final Boolean dependencies) throws Exception;

}
