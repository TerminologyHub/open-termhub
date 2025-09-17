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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
@Order(2)
public class BrowserQueryBuilder implements QueryBuilder {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(BrowserQueryBuilder.class);

  /**
   * Instantiates a handler.
   */
  public BrowserQueryBuilder() {
    // n/a
  }

  /* see superclass */
  @Override
  public boolean accepts(final String handler) {
    return "browser".equals(handler);
  }

  /* see superclass */
  @Override
  public String buildQuery(final SearchParameters params) {
    final StringBuilder sb = new StringBuilder();
    sb.append(buildQuery(params.getQuery()));
    if (params.getActive() != null) {
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
    sb.append(buildEscapedQuery(params.getQuery()));
    if (params.getActive() != null) {
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
    return (StringUtils.isEmpty(query) || query.equals("*")) ? "*"
        : "\"" + StringUtility.escapeQuery(query) + "\"";
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
    // code:9683-4
    // attributes.STATUS:DEPRECATED
    else if (query.matches(".*[a-zA-Z_]+[a-z]\\:.*")
        || query.matches(".*attributes.[a-zA-Z_]+\\:.*")) {
      sb.append(query);
    }
    // Otherwise, build a query in parts
    else {
      final List<String> clauses = new ArrayList<>();
      final String stemQuery = StringUtility.normalizeWithStemming(query);

      // code:15771004 OR name:(15771004) OR terms.name:(15771004)
      if (isCode(query)) {
        clauses.add("code:" + query + "^30");
        clauses.add("identifiers.id:" + query + "^30");
      } else if (isLowercaseCode(query)) {
        // check both uppercase and lowercase
        clauses.add("(code:" + query.toUpperCase() + "^30 OR code:" + query + "^30)");
      }

      // Concept name exact match
      if (stemQuery.isEmpty()) {
        clauses.add("name.keyword:(" + StringUtility.escapeQuery(query) + ")^30");
      } else {
        clauses.add("stemName.keyword:\"" + StringUtility.escapeQuery(stemQuery) + "\"^30");
        clauses.add("terms.stemName.keyword:\"" + StringUtility.escapeQuery(stemQuery) + "\"^25");
      }

      // term name match
      if (!stemQuery.isEmpty()) {
        // Boost for phrase
        clauses.add("terms.stemName:\"" + StringUtility.escapeQuery(stemQuery) + "\"^5");
        // If not a code, also do wildcard searches on each word (boost for AND)
        if (!isCode(query)) {
          final String clause = "normName:("
              + String.join(" AND ",
                  Arrays.asList(StringUtility.normalize(query).split(" ")).stream()
                      .map(s -> StringUtility.escapeQuery(s) + "*").collect(Collectors.toList()))
              + ")^5";
          clauses.add(clause);
          clauses.add("terms." + clause);
        }
        // Other matches, lower quality
        clauses.add("terms.stemName:(" + StringUtility.escapeQuery(stemQuery) + ")^0.5");
      }

      // Favor shorter things
      sb.append(StringUtility.composeQuery("OR", clauses));
      // NOTE: we could boost based on number of words in the query string
      sb.append(" AND (wordCt:1^80 OR wordCt:2^80 OR wordCt:3^70 OR"
          + " wordCt:4^70 OR wordCt:5^25 OR wordCt:6^10 OR wordCt:[7 TO *])");

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
    query.replaceFirst("\\*$", "").matches("[\\dA-Z]+")
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
    query.replaceFirst("\\*$", "").matches("[\\da-z]+")
        // 10234-1, a01.5xxa, i51.2, 1cC61.3y
        || query.matches("[\\da-z]+[\\-.][\\da-z]+")
        // hgnc:12345
        || query.matches("[a-z]+\\:\\d+");

  }
}
