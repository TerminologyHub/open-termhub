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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.MultiField;

import com.wci.termhub.model.Concept;
import com.wci.termhub.util.ModelUtility;

/**
 * Lucene query builder.
 */
public final class LuceneQueryBuilder {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(LuceneQueryBuilder.class);

  /**
   * Instantiates a new lucene query builder.
   */
  private LuceneQueryBuilder() {
    // private constructor to prevent instantiation
  }

  /** The Constant SEARCHABLE_FIELDS_CACHE. */
  private static final Map<Class<?>, String[]> SEARCHABLE_FIELDS_CACHE = new HashMap<>();

  /**
   * Parses the query for a specific model class.
   *
   * @param queryText the query text
   * @param modelClass the model class
   * @return the query
   * @throws ParseException the parse exception
   */
  public static Query parse(final String queryText, final Class<?> modelClass)
    throws ParseException {
    logger.debug("  parse query: {} for class {}", queryText,
        modelClass != null ? modelClass.getSimpleName() : "null");
    final boolean isFieldedQuery = queryText != null && queryText.matches(".*\\w+:.*");

    if (isFieldedQuery) {
      final KeywordAnalyzer analyzer = new KeywordAnalyzer();
      final QueryParser queryParser = new QueryParser("", analyzer);
      final Query query = queryParser.parse(queryText);
      logger.debug("  Parsed Query: {}", query);
      return query;
    } else {
      // Handle non-fielded queries by searching across all searchable fields
      final String[] fields = getSearchableFields(modelClass != null ? modelClass : Concept.class);
      final StandardAnalyzer analyzer = new StandardAnalyzer();
      final MultiFieldQueryParser multiFieldQueryParser =
          new MultiFieldQueryParser(fields, analyzer);
      final Query query = multiFieldQueryParser.parse(queryText);
      logger.debug("  Parsed Query (multi-field): {}", query);
      return query;
    }
  }

  /**
   * Returns the list of searchable fields for a given model class. Uses
   * reflection to find String or List<String> fields, or those annotated
   * with @Field(type = FieldType.Text) or MultiField.
   *
   * @param modelClass the model class
   * @return the searchable fields
   */
  private static String[] getSearchableFields(final Class<?> modelClass) {
    if (SEARCHABLE_FIELDS_CACHE.containsKey(modelClass)) {
      return SEARCHABLE_FIELDS_CACHE.get(modelClass);
    }
    final List<Field> allFields = ModelUtility.getAllFields(modelClass);
    final List<String> fieldNames = allFields.stream().filter(f -> {
      // Check for String or List<String>
      final Class<?> type = f.getType();
      final boolean isString = type.equals(String.class);
      final boolean isListString = List.class.isAssignableFrom(type);
      // Check for @Field(type = FieldType.Text) or @MultiField
      final org.springframework.data.elasticsearch.annotations.Field esField =
          f.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);
      final MultiField multiField = f.getAnnotation(MultiField.class);
      final boolean isTextField = esField != null && esField.type() == FieldType.Text;
      final boolean isMultiField = multiField != null;
      return isString || isListString || isTextField || isMultiField;
    }).map(Field::getName).toList();
    final String[] result = fieldNames.toArray(new String[0]);
    SEARCHABLE_FIELDS_CACHE.put(modelClass, result);
    return result;
  }

}
