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
import java.util.Map;
import java.util.Set;

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
import com.wci.termhub.model.TerminologyComponent;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.service.FindCallbackHandler;
import com.wci.termhub.util.ModelUtility;

/**
 * Entity service implementation.
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

  /* see superclass */
  @Override
  public void createIndex(final Class<? extends HasId> clazz) throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    luceneData.createIndex(clazz);
  }

  /* see superclass */
  @Override
  public void deleteIndex(final Class<? extends HasId> clazz) throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    luceneData.deleteIndex(clazz);
  }

  /* see superclass */
  @Override
  public void add(final Class<? extends HasId> clazz, final HasId entity) throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    LOGGER.debug("    ADD {} entity to index {}", entity, clazz.getSimpleName());
    luceneData.add(entity);
  }

  /* see superclass */
  @Override
  public void addBulk(final Class<? extends HasId> clazz, final List<HasId> entity)
    throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    LOGGER.debug("    ADD {} entities to index: {}", clazz.getSimpleName(), entity.size());
    luceneData.add(entity);
  }

  /* see superclass */
  @Override
  public void update(final Class<? extends HasId> clazz, final String id, final HasId entity)
    throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    LOGGER.debug("    UPDATE {} entity to index {}", entity, clazz.getSimpleName());
    luceneData.remove(clazz, id);
    luceneData.add(entity);
  }

  /* see superclass */
  @Override
  public void updateBulk(final Class<? extends HasId> clazz, final Map<String, HasId> entities)
    throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();

    for (final String id : entities.keySet()) {
      final HasId entity = entities.get(id);
      if (entity == null) {
        LOGGER.warn("    UPDATE {} entity to index {} - entity is null", id, clazz.getSimpleName());
        continue;
      }
      LOGGER.debug("    UPDATE {} entity to index {}", entity, clazz.getSimpleName());
      luceneData.remove(clazz, id);
      luceneData.add(entity);
    }

  }

  /* see superclass */
  @Override
  public <T extends HasId> ResultList<T> findAll(final SearchParameters searchParameters,
    final Class<T> clazz) throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    // use paging
    return luceneData.find(clazz, searchParameters);
  }

  /* see superclass */
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

  /* see superclass */
  @Override
  public <T extends HasId> ResultList<String> findIds(final SearchParameters params,
    final Class<T> clazz) throws Exception {

    final ResultList<T> list = findFields(params, ModelUtility.asList("id"), clazz, null);
    final ResultList<String> ids = new ResultList<>();
    ids.setTotal(list.getTotal());
    ids.setParameters(params);
    ids.setItems(list.getItems().stream().map(o -> o.getId()).toList());
    return ids;
  }

  /* see superclass */
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

  /* see superclass */
  @Override
  public <T extends HasId> ResultList<T> find(final SearchParameters searchParameters,
    final Class<T> clazz) throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();
    return luceneData.find(clazz, searchParameters);
  }

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

      LOGGER.info("      result count = {} (total={})", result.getItems().size(),
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

  /* see superclass */
  @Override
  public <T extends HasId> ResultList<T> findFields(final SearchParameters searchParameters,
    final List<String> fields, final Class<T> clazz) throws Exception {
    return findFields(searchParameters, fields, clazz, null);
  }

  /* see superclass */
  @Override
  public <T extends HasId> ResultList<T> findFields(final SearchParameters searchParameters,
    final List<String> fields, final Class<T> clazz, final Set<String> terminologies)
    throws Exception {

    checkIfEntityHasDocumentAnnotation(clazz);
    final LuceneDataAccess luceneData = new LuceneDataAccess();

    final ResultList<T> results = luceneData.find(clazz, searchParameters);

    // if class does not extend TerminologyComponent, skip filtering
    if (!TerminologyComponent.class.isAssignableFrom(clazz) || terminologies == null
        || terminologies.isEmpty()) {
      return results;
    }

    // filter results by terminologies
    final ResultList<T> filteredResultList = new ResultList<>();
    filteredResultList.setParameters(searchParameters);

    if (!terminologies.isEmpty()) {
      final List<T> filteredItems = results.getItems().stream()
          .filter(item -> terminologies.contains(((TerminologyComponent) item).getTerminology()))
          .toList();
      filteredResultList.setItems(filteredItems);
      filteredResultList.setTotal(filteredItems.size());
    } else {
      // if no terminologies specified, return all results
      filteredResultList.setItems(results.getItems());
      filteredResultList.setTotal(results.getTotal());
    }

    return filteredResultList;
  }

  /* see superclass */
  @Override
  public <T extends HasId> List<T> findAllWithFields(final Query query, final List<String> fields,
    final Class<T> clazz) throws Exception {
    return findAllWithFields(query, fields, clazz, null);
  }

  /* see superclass */
  @Override
  public <T extends HasId> List<T> findAllWithFields(final Query query, final List<String> fields,
    final Class<T> clazz, final FindCallbackHandler<T> handler) throws Exception {

    // Load blocks of 10k, need to specify sort=id for this to work
    final SearchParameters params = new SearchParameters(query, 0, findAllPageSize, "id", null);

    final List<T> list = new ArrayList<>();
    while (true) {
      final List<T> innerList = findFields(params, fields, clazz, null).getItems();

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

  /* see superclass */
  @Override
  public <T extends HasId> List<T> findAll(final String query, final Query luceneQuery,
    final Class<T> clazz) throws Exception {
    return findAll(query, luceneQuery, clazz, null);
  }

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
    LOGGER.info("    query [{}] offset={}, limit={}, {} {}", queryString, params.getOffset(),
        params.getLimit(), clazz.getSimpleName(), handler);
    final Query query = LuceneQueryBuilder.parse(queryString, clazz);
    return query;

  }

}
