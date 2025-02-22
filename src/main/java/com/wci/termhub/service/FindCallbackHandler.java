/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.service;

import java.util.List;

import com.wci.termhub.model.HasId;

/**
 * Represents a callback for a streaming lookup.
 *
 * @param <T> the generic type
 */
public interface FindCallbackHandler<T extends HasId> {

  /**
   * Callback to handle a block of results.
   *
   * @param list the list
   * @throws Exception the exception
   */
  public void callback(List<T> list) throws Exception;

}
