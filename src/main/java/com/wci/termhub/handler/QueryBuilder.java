/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.handler;

import java.util.List;

import com.wci.termhub.model.SearchParameters;

/**
 * Generically represents an algorithm for object searching.
 */
public interface QueryBuilder {

  /**
   * Find builder.
   *
   * @param builders the builders
   * @param handler the handler
   * @return the query builder
   */
  public static QueryBuilder findBuilder(final List<QueryBuilder> builders, final String handler) {
    return builders.stream().filter(b -> b.accepts(handler)).findFirst().orElse(null);
  }

  /**
   * Returns true if the implementation accepts the handler.
   *
   * @param handler the handler
   * @return true, if successful
   */
  public boolean accepts(String handler);

  /**
   * Builds the query from the search params.
   *
   * @param params the params
   * @return the string
   */
  public String buildQuery(SearchParameters params);

  /**
   * Builds the query.
   *
   * @param query the query
   * @return the string
   */
  public String buildQuery(String query);

  /**
   * Builds an "escaped" version of the query from the search params, in case the original search
   * returns nothing.
   *
   * @param params the params
   * @return the string
   */
  public String buildEscapedQuery(SearchParameters params);

  /**
   * Builds the escaped query.
   *
   * @param query the query
   * @return the string
   */
  public String buildEscapedQuery(String query);

}
