/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.repository;

import java.util.Optional;

import com.wci.termhub.model.BaseModel;
import com.wci.termhub.model.SearchParameters;

/**
 * The Interface EntityRepository.
 *
 * @param <T> the generic type
 * @param <ID> the generic type
 */
public interface EntityRepository<T extends BaseModel, ID> {

  /**
   * Delete index.
   *
   * @param entity the entity
   * @throws Exception the exception
   */
  public void deleteIndex(final T entity) throws Exception;

  /**
   * Creates the index.
   *
   * @param entity the entity
   * @throws Exception the exception
   */
  public void createIndex(final T entity) throws Exception;

  /**
   * Adds the.
   *
   * @param entity the entity
   * @throws Exception the exception
   */
  public void add(final T entity) throws Exception;

  /**
   * Removes the.
   *
   * @param entity the entity
   * @throws Exception the exception
   */
  public void remove(final T entity) throws Exception;

  /**
   * Find.
   *
   * @param searchParameters the search parameters
   * @param clazz the clazz
   * @return the optional
   * @throws Exception the exception
   */
  public Optional<T> find(final SearchParameters searchParameters, final Class<T> clazz)
    throws Exception;
}
