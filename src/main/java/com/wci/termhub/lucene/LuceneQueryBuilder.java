/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.lucene;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LuceneQueryBuilder.
 */
public final class LuceneQueryBuilder {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(LuceneQueryBuilder.class);

  /**
   * Instantiates a new lucene query builder.
   */
  private LuceneQueryBuilder() {
    // private constructor to prevent instantiation
  }

  /**
   * Parses the.
   *
   * @param queryText the query text
   * @return the query
   * @throws ParseException the parse exception
   */
  public static Query parse(final String queryText) throws ParseException {

    final KeywordAnalyzer analyzer = new KeywordAnalyzer();
    final QueryParser queryParser = new QueryParser("", analyzer);

    final Query query = queryParser.parse(queryText);
    logger.debug("Parsed Query: {}", query.toString());

    return query;
  }

}
