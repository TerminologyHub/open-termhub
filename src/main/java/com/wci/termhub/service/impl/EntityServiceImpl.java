/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.stereotype.Service;

import com.wci.termhub.handler.QueryBuilder;
import com.wci.termhub.lucene.LuceneDataAccess;
import com.wci.termhub.lucene.LuceneQueryBuilder;
import com.wci.termhub.model.HasId;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.service.FindCallbackHandler;
import com.wci.termhub.util.ModelUtility;

/**
 * The Class EntityServiceImpl.
 */
@Service
public class EntityServiceImpl implements EntityRepositoryService {

  /** The LOG. */
  private static final Logger LOGGER = LoggerFactory.getLogger(EntityServiceImpl.class);

  /** The find all page size. */
  private int findAllPageSize = 10000;

  /** The builders. */
  @Autowired
  private List<QueryBuilder> builders;

  /**
   * Sets the find all page size.
   *
   * @param pageSize the new find all page size
   */
  public void setFindAllPageSize(final int pageSize) {
    findAllPageSize = pageSize;
  }

  /**
   * Creates the index.
   *
   * @param clazz the clazz
   * @throws Exception the exception
   */
  @Override
  public void createIndex(final Class<? extends HasId> clazz) throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    luceneData.createIndex(clazz);
  }

  /**
   * Delete index.
   *
   * @param clazz the clazz
   * @throws Exception the exception
   */
  @Override
  public void deleteIndex(final Class<? extends HasId> clazz) throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    luceneData.deleteIndex(clazz);
  }

  /**
   * Adds the entity.
   *
   * @param clazz the clazz
   * @param entity the entity
   * @throws Exception the exception
   */
  @Override
  public void add(final Class<? extends HasId> clazz, final HasId entity) throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    LOGGER.info("Adding {} entity to index {}", entity.getId(), clazz.getSimpleName());
    luceneData.add(entity);
  }

  /**
   * Adds the entity.
   *
   * @param clazz the clazz
   * @param entity the entity
   * @throws Exception the exception
   */
  @Override
  public void addBulk(final Class<? extends HasId> clazz, final List<HasId> entity)
    throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    LOGGER.info("Adding {} entities to index = {}", clazz.getSimpleName(), entity.size());
    luceneData.add(entity);
  }

  /**
   * Update the entity.
   *
   * @param clazz the clazz
   * @param id the id
   * @param entity the entity
   * @throws Exception the exception
   */
  @Override
  public void update(final Class<? extends HasId> clazz, final String id, final HasId entity)
    throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    LOGGER.info("Update {} entities to index {}", entity, clazz.getSimpleName());
    luceneData.remove(clazz, id);
    luceneData.add(entity);
  }

  /**
   * Find all objects from index of this object type.
   *
   * @param <T> the generic type
   * @param searchParameters the search parameters
   * @param clazz the clazz
   * @return the iterable
   * @throws Exception the exception
   */
  @Override
  public <T extends HasId> ResultList<T> findAll(final SearchParameters searchParameters,
    final Class<T> clazz) throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    // use paging
    return luceneData.find(clazz, searchParameters);
  }

  /**
   * Find by id.
   *
   * @param <T> the generic type
   * @param id the id
   * @param clazz the clazz
   * @return the optional
   * @throws Exception the exception
   */
  @Override
  public <T extends HasId> T get(final String id, final Class<T> clazz) throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    final SearchParameters searchParameters = new SearchParameters();
    searchParameters.setQuery("id:" + id);
    final ResultList<T> results = luceneData.find(clazz, searchParameters);

    if (results.getItems().size() == 1) {
      return results.getItems().get(0);
    }

    return null;
  }

  /**
   * Find ids.
   *
   * @param <T> the generic type
   * @param params the params
   * @param clazz the clazz
   * @return the result list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public <T extends HasId> ResultList<String> findIds(final SearchParameters params,
    final Class<T> clazz) throws Exception {

    final ResultList<T> list = findFields(params, ModelUtility.asList("id"), clazz);
    final ResultList<String> ids = new ResultList<>();
    ids.setTotal(list.getTotal());
    ids.setParameters(params);
    ids.setItems(list.getItems().stream().map(o -> o.getId()).collect(Collectors.toList()));
    return ids;
  }

  /**
   * Delete object from index.
   *
   * @param id the id
   * @param clazz the clazz
   * @throws Exception the exception
   */
  @Override
  public void remove(final String id, final Class<? extends HasId> clazz) throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    luceneData.remove(clazz, id);
  }

  /**
   * Check if entity has document annotation.
   *
   * @param clazz the clazz
   * @throws IllegalArgumentException the illegal argument exception
   */
  private void checkIfEntityHasDocumentAnnotation(final Class<? extends HasId> clazz)
    throws IllegalArgumentException {

    // check if the clazz has annotation @Document in the class
    if (!clazz.isAnnotationPresent(Document.class)) {
      throw new IllegalArgumentException(
          "Entity " + clazz.getName() + " should have @Document annotation.");
    }
  }

  /**
   * Find.
   *
   * @param <T> the generic type
   * @param searchParameters the search parameters
   * @param clazz the clazz
   * @return the result list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public <T extends HasId> ResultList<T> find(final SearchParameters searchParameters,
    final Class<T> clazz) throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    return luceneData.find(clazz, searchParameters);
  }

  /**
   * Find.
   *
   * @param <T> the generic type
   * @param searchParameters the search parameters
   * @param clazz the clazz
   * @param handler the handler
   * @return the result list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public <T extends HasId> ResultList<T> find(final SearchParameters searchParameters,
    final Class<T> clazz, final String handler) throws Exception {

    try {
      if (searchParameters.getLimit() == 0) {
        final ResultList<T> result = new ResultList<>();
        result.setTotal(0);
        return result;
      }

      final int fromIndex = searchParameters.getOffset() % searchParameters.getLimit();
      final int toIndex = fromIndex + searchParameters.getLimit();

      final Query searchQuery = findHelper(searchParameters, clazz, handler, false);

      final LuceneDataAccess luceneData = new LuceneDataAccess();
      final ResultList<T> result = luceneData.find(clazz, searchParameters, searchQuery);

      LOGGER.debug("      result count = {} (total={})", result.getItems().size(),
          result.getTotal());
      if (fromIndex != 0) {
        // Handle non-aligned paging (e.g fromRecord=6, pageSize=10)
        // if fromIndex is beyond the end of results, bail
        if (fromIndex >= result.getItems().size()) {
          result.setItems(new ArrayList<>());
        } else {
          result.setItems(
              result.getItems().subList(fromIndex, Math.min(toIndex, result.getItems().size())));
        }
      }

      return result;

    } catch (final Exception e) {
      throw e;
    }
  }

  /**
   * Find fields.
   *
   * @param <T> the generic type
   * @param searchParameters the search parameters
   * @param fields the fields
   * @param clazz the clazz
   * @return the result list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public <T extends HasId> ResultList<T> findFields(final SearchParameters searchParameters,
    final List<String> fields, final Class<T> clazz) throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    return luceneData.find(clazz, searchParameters);
  }

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
  /* see superclass */
  @Override
  public <T extends HasId> List<T> findAllWithFields(final Query query, final List<String> fields,
    final Class<T> clazz) throws Exception {
    return findAllWithFields(query, fields, clazz, null);
  }

  /**
   * Find all with fields.
   *
   * @param <T> the generic type
   * @param query the query
   * @param fields the fields
   * @param clazz the clazz
   * @param handler the handler
   * @return the list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public <T extends HasId> List<T> findAllWithFields(final Query query, final List<String> fields,
    final Class<T> clazz, final FindCallbackHandler<T> handler) throws Exception {

    // Load blocks of 10k, need to specify sort=id for this to work
    final SearchParameters params = new SearchParameters(query, 0, findAllPageSize, "id", null);

    final List<T> list = new ArrayList<>();
    while (true) {
      final List<T> innerList = findFields(params, fields, clazz).getItems();

      if (innerList.isEmpty()) {
        break;
      }

      if (handler != null) {
        handler.callback(innerList);
      } else {
        list.addAll(innerList);
      }

      if (innerList.size() < params.getLimit()) {
        break;
      }
      params.setOffset(params.getOffset() + params.getLimit());
    }

    return list;
  }

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
  /* see superclass */
  @Override
  public <T extends HasId> List<T> findAll(final String query, final Query luceneQuery,
    final Class<T> clazz) throws Exception {
    return findAll(query, luceneQuery, clazz, null);
  }

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
  /* see superclass */
  @Override
  public <T extends HasId> List<T> findAll(final String query, final Query luceneQuery,
    final Class<T> clazz, final FindCallbackHandler<T> handler) throws Exception {

    // Load blocks of 10k, need to specify sort=id for this to work
    final SearchParameters params =
        (luceneQuery != null) ? new SearchParameters(luceneQuery, 0, findAllPageSize, "id", null)
            : new SearchParameters(query, 0, findAllPageSize, "id", null);

    final List<T> list = new ArrayList<>();

    while (true) {
      final List<T> innerList = find(params, clazz).getItems();

      if (innerList.isEmpty()) {
        break;
      }

      if (handler != null) {
        handler.callback(innerList);
      } else {
        list.addAll(innerList);
      }

      if (innerList.size() < params.getLimit()) {
        break;
      }
      params.setOffset(params.getOffset() + params.getLimit());
    }

    return list;
  }

  /**
   * Find all ids.
   *
   * @param <T> the generic type
   * @param query the query
   * @param clazz the clazz
   * @return the list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public <T extends HasId> List<String> findAllIds(final Query query, final Class<T> clazz)
    throws Exception {

    // Load blocks of 10k, need to specify sort=id for this to work
    final SearchParameters params = new SearchParameters(query, 0, findAllPageSize, "id", null);
    final List<String> list = new ArrayList<>();

    while (true) {
      final List<String> innerList = findIds(params, clazz).getItems();

      if (innerList.isEmpty()) {
        break;
      }

      // Get the id of the last result
      list.addAll(innerList);

      if (innerList.size() < params.getLimit()) {
        break;
      }
      params.setOffset(params.getOffset() + params.getLimit());
    }

    return list;
  }

  /**
   * Find single.
   *
   * @param <T> the generic type
   * @param params the params
   * @param clazz the clazz
   * @return the t
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public <T extends HasId> T findSingle(final SearchParameters params, final Class<T> clazz)
    throws Exception {

    final ResultList<T> list = find(params, clazz, null);

    if (list.getTotal() == 1) {
      return list.getItems().get(0);
    } else if (list.getTotal() > 1) {
      throw new Exception(
          "Unexpectedly found more than one object = " + params + ", " + clazz.getName());
    }
    return null;
  }

  /**
   * Find helper.
   *
   * @param <T> the
   * @param params the params
   * @param clazz the clazz
   * @param handler the handler
   * @param escapeFlag the escape flag
   * @return the native search query builder
   * @throws Exception the exception
   */
  private <T extends HasId> Query findHelper(final SearchParameters params, final Class<T> clazz,
    final String handler, final boolean escapeFlag) throws Exception {

    // Escape the term in case it has special characters
    final String queryString =
        escapeFlag ? QueryBuilder.findBuilder(builders, handler).buildEscapedQuery(params)
            : QueryBuilder.findBuilder(builders, handler).buildQuery(params);
    LOGGER.debug("    query [{}] offset={}, limit={}, {} {}", queryString, params.getOffset(),
        params.getLimit(), clazz.getSimpleName(), handler);
    final Query query = LuceneQueryBuilder.parse(queryString);
    return query;

  }

}
