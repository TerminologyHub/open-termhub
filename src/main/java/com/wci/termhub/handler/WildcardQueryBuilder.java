/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.handler;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.util.StringUtility;

/**
 * Default implementation a query builder for use by the browser. Make improvements to what's typed
 * in to produce ideal browser results.
 */
@Component
@Order(3)
public class WildcardQueryBuilder implements QueryBuilder {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(WildcardQueryBuilder.class);

  /**
   * Instantiates a handler.
   */
  public WildcardQueryBuilder() {
    // n/a
  }

  /* see superclass */
  @Override
  public boolean accepts(final String handler) {
    return "wildcard".equals(handler);
  }

  /* see superclass */
  @Override
  public String buildQuery(final SearchParameters params) {
    final StringBuilder sb = new StringBuilder();

    sb.append(buildQuery(params.getQuery()));
    if (params.getActive() != null && params.getActive()) {
      sb.append(" AND active:" + params.getActive());
    } else {
      sb.append(" AND (active:true^25 OR active:false)");
    }
    if (params.getLeaf() != null) {
      sb.append(" AND leaf:" + params.getLeaf());
    }
    return sb.toString();
  }

  /* see superclass */
  @Override
  public String buildEscapedQuery(final SearchParameters params) {
    final StringBuilder sb = new StringBuilder();
    // Same thing but with an escaped query
    sb.append(buildEscapedQuery(params.getQuery()));
    if (params.getActive() != null && params.getActive()) {
      sb.append(" AND active:" + params.getActive());
    } else {
      sb.append(" AND (active:true^25 OR active:false)");
    }

    if (params.getLeaf() != null) {
      sb.append(" AND leaf:" + params.getLeaf());
    }
    return sb.toString();
  }

  /* see superclass */
  @Override
  public String buildEscapedQuery(final String query) {
    return (StringUtility.isEmpty(query) || query.equals("*")) ? "*"
        : StringUtility.escapeQuery(query);
  }

  /* see superclass */
  @Override
  public String buildQuery(final String query) {
    final StringBuilder sb = new StringBuilder();
    // Use * for empty query
    if (StringUtility.isEmpty(query) || query.equals("*")) {
      sb.append("*");
    }
    // Fielded queries should be left alone
    else if (StringUtility.isFieldedQuery(query)) {
      sb.append(query);
    }
    // Otherwise, split on whitespace, escape each word and add a *
    else {
      sb.append(String.join(" AND ", Arrays.asList(query.split(" ")).stream()
          .map(w -> StringUtility.escapeQuery(w) + "*").collect(Collectors.toList())));
    }
    return sb.toString();

  }

  /**
   * Checks if is code.
   *
   * @param query the query
   * @return true, if is code
   */
  public boolean isCode(final String query) {

    //
    return
    // 43189234, C48123, XE8CT, PA007
    query.matches("[\\dA-Z]+")
        // 10234-1, A01.5XXA, I51.2, 1C61.3Y
        || query.matches("[\\dA-Z]+[\\-.][\\dA-Z]+")
        // HGNC:12345
        || query.matches("[A-Z]+\\:\\d+");

  }

  /**
   * Checks if is lowercase code.
   *
   * @param query the query
   * @return true, if is lowercase code
   */
  public boolean isLowercaseCode(final String query) {

    return
    // 43189234, c48123, xe8ct, pa007
    query.matches("[\\da-z]+")
        // 10234-1, a01.5xxa, i51.2, 1cC61.3y
        || query.matches("[\\da-z]+[\\-.][\\da-z]+")
        // hgnc:12345
        || query.matches("[a-z]+\\:\\d+");

  }
}
