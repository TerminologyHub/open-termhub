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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.util.StringUtility;

/**
 * Implementation of a query builder for most tables in the application, including projects,
 * organizations, teams, users, various config tables, etc.
 */
@Component
@Order(4)
public class TermhubQueryBuilder implements QueryBuilder {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(TermhubQueryBuilder.class);

  /**
   * Instantiates a handler.
   */
  public TermhubQueryBuilder() {
    // n/a
  }

  /* see superclass */
  @Override
  public boolean accepts(final String handler) {
    return "termhub".equals(handler);
  }

  /* see superclass */
  @Override
  public String buildQuery(final SearchParameters params) {
    return buildQuery(params.getQuery());
  }

  /* see superclass */
  @Override
  public String buildEscapedQuery(final SearchParameters params) {
    return buildEscapedQuery(params.getQuery());
  }

  /* see superclass */
  @Override
  public String buildEscapedQuery(final String query) {
    // Not sure this is important
    return (StringUtils.isEmpty(query) || query.equals("*")) ? "*"
        : "\"" + StringUtility.escapeQuery(query) + "\"";
  }

  /* see superclass */
  @Override
  public String buildQuery(final String query) {
    // Use * for empty query
    if (StringUtility.isEmpty(query) || query.equals("*")) {
      return "*";
    }

    final Set<String> words = Arrays.asList(query.split(" ")).stream().filter(w -> !w.isEmpty())
        .map(w -> StringUtility.escapeQuery(w) + "*").collect(Collectors.toSet());

    final String wordsQuery =
        (words.size() > 1 ? "(" : "") + String.join(" AND ", words) + (words.size() > 1 ? ")" : "");

    // Search things in uppercase
    final Set<String> ucwords = Arrays.asList(query.split(" ")).stream().filter(w -> !w.isEmpty())
        .map(w -> StringUtility.escapeQuery(w.toUpperCase()) + "*").collect(Collectors.toSet());
    final String ucWordsQuery = (words.size() > 1 ? "(" : "") + String.join(" AND ", ucwords)
        + (words.size() > 1 ? ")" : "");

    if (wordsQuery.equals(ucWordsQuery)) {
      return wordsQuery;
    }
    return StringUtility.composeQuery("OR", wordsQuery, ucWordsQuery);

  }

}
