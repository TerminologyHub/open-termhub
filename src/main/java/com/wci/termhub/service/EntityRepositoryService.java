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
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Query;

import com.wci.termhub.model.HasId;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;

/**
 * The Interface TService.
 */
public interface EntityRepositoryService {

  /**
   * Creates the index.
   *
   * @param clazz the clazz
   * @throws Exception the exception
   */
  public void createIndex(final Class<? extends HasId> clazz) throws Exception;

  /**
   * Delete index.
   *
   * @param clazz the clazz
   * @throws Exception the exception
   */
  public void deleteIndex(final Class<? extends HasId> clazz) throws Exception;

  /**
   * Adds the entity.
   *
   * @param clazz the clazz
   * @param entity the entity
   * @throws Exception the exception
   */
  public void add(final Class<? extends HasId> clazz, final HasId entity) throws Exception;

  /**
   * Adds the.
   *
   * @param clazz the clazz
   * @param entity the entity
   * @throws Exception the exception
   */
  public void addBulk(final Class<? extends HasId> clazz, final List<HasId> entity)
    throws Exception;

  /**
   * Update the entity.
   *
   * @param clazz the clazz
   * @param id the id
   * @param entity the entity
   * @throws Exception the exception
   */
  public void update(final Class<? extends HasId> clazz, final String id, final HasId entity)
    throws Exception;

  /**
   * Update the entity.
   *
   * @param clazz the clazz
   * @param entities the entities
   * @throws Exception the exception
   */
  public void updateBulk(final Class<? extends HasId> clazz, final Map<String, HasId> entities)
    throws Exception;

  /**
   * Find by id.
   *
   * @param <T> the generic type
   * @param id the id
   * @param clazz the clazz
   * @return the optional
   * @throws Exception the exception
   */
  public <T extends HasId> T get(final String id, final Class<T> clazz) throws Exception;

  /**
   * Find ids.
   *
   * @param <T> the generic type
   * @param params the params
   * @param clazz the clazz
   * @return the result list
   * @throws Exception the exception
   */
  public <T extends HasId> ResultList<String> findIds(final SearchParameters params,
    final Class<T> clazz) throws Exception;

  /**
   * Find all.
   *
   * @param <T> the generic type
   * @param searchParameters the search parameters
   * @param clazz the clazz
   * @return the iterable
   * @throws Exception the exception
   */
  public <T extends HasId> ResultList<T> findAll(final SearchParameters searchParameters,
    final Class<T> clazz) throws Exception;

  /**
   * Delete.
   *
   * @param id the id
   * @param clazz the clazz
   * @throws Exception the exception
   */
  public void remove(final String id, final Class<? extends HasId> clazz) throws Exception;

  /**
   * Find.
   *
   * @param <T> the generic type
   * @param params the params
   * @param clazz the clazz
   * @return the result list
   * @throws Exception the exception
   */
  public <T extends HasId> ResultList<T> find(final SearchParameters params, final Class<T> clazz)
    throws Exception;

  /**
   * Find.
   *
   * @param <T> the generic type
   * @param params the params
   * @param clazz the clazz
   * @param handler the handler
   * @return the result list
   * @throws Exception the exception
   */
  public <T extends HasId> ResultList<T> find(final SearchParameters params, final Class<T> clazz,
    final String handler) throws Exception;

  /**
   * Find fields.
   *
   * @param <T> the generic type
   * @param params the params
   * @param fields the fields
   * @param clazz the clazz
   * @return the result list
   * @throws Exception the exception
   */
  public <T extends HasId> ResultList<T> findFields(final SearchParameters params,
    final List<String> fields, final Class<T> clazz) throws Exception;

  /**
   * Find fields.
   *
   * @param <T> the generic type
   * @param params the params
   * @param fields the fields
   * @param clazz the clazz
   * @param terminologies the terminologies
   * @return the result list
   * @throws Exception the exception
   */
  public <T extends HasId> ResultList<T> findFields(final SearchParameters params,
    final List<String> fields, final Class<T> clazz, final Set<String> terminologies)
    throws Exception;

  /**
   * Find all with fields.
   *
   * @param <T> the generic type
   * @param query the query
   * @param fields the fields
   * @param clazz the clazz
   * @return the list
   * @throws Exception the exception
   */
  public <T extends HasId> List<T> findAllWithFields(final Query query, final List<String> fields,
    final Class<T> clazz) throws Exception;

  /**
   * Find all with fields. For each batch of results, send to the callback
   * handler.
   *
   * @param <T> the generic type
   * @param query the query
   * @param fields the fields
   * @param clazz the clazz
   * @param handler the handler
   * @return the list
   * @throws Exception the exception
   */
  public <T extends HasId> List<T> findAllWithFields(final Query query, final List<String> fields,
    final Class<T> clazz, final FindCallbackHandler<T> handler) throws Exception;

  /**
   * Find all.
   *
   * @param <T> the generic type
   * @param query the query
   * @param luceneQuery the lucene query
   * @param clazz the clazz
   * @return the list
   * @throws Exception the exception
   */
  public <T extends HasId> List<T> findAll(final String query, final Query luceneQuery,
    final Class<T> clazz) throws Exception;

  /**
   * Find all.
   *
   * @param <T> the generic type
   * @param query the query
   * @param luceneQuery the lucene query
   * @param clazz the clazz
   * @param handler the handler
   * @return the list
   * @throws Exception the exception
   */
  public <T extends HasId> List<T> findAll(final String query, final Query luceneQuery,
    final Class<T> clazz, final FindCallbackHandler<T> handler) throws Exception;

  /**
   * Find all ids.
   *
   * @param <T> the generic type
   * @param query the query
   * @param clazz the clazz
   * @return the list
   * @throws Exception the exception
   */
  public <T extends HasId> List<String> findAllIds(final Query query, final Class<T> clazz)
    throws Exception;

  /**
   * Find single.
   *
   * @param <T> the generic type
   * @param params the params
   * @param clazz the clazz
   * @return the t
   * @throws Exception the exception
   */
  public <T extends HasId> T findSingle(final SearchParameters params, final Class<T> clazz)
    throws Exception;
}
