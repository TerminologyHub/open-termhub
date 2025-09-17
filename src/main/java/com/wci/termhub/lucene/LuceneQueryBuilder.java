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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import com.wci.termhub.model.HasId;
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

  /** The Constant FIELD_ANALYZERS_CACHE. */
  private static final Map<Class<?>, Map<String, Analyzer>> FIELD_ANALYZERS_CACHE = new HashMap<>();

  /** The Constant NESTED_FIELD_SUFFIXES. */
  private static final Set<String> NESTED_FIELD_SUFFIXES =
      Set.of("code", "terminology", "version", "publisher", "abbreviation", "keyword");

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

    // Get field-specific analyzers based on annotations
    final Map<String, Analyzer> fieldAnalyzers = getFieldAnalyzers(modelClass);
    final String[] searchableFields = getSearchableFields(modelClass);

    // Create PerFieldAnalyzerWrapper with field-specific analyzers
    try (final Analyzer defaultAnalyzer = new StandardAnalyzer();) {
      final PerFieldAnalyzerWrapper perFieldAnalyzer =
          new PerFieldAnalyzerWrapper(defaultAnalyzer, fieldAnalyzers);

      // Create query parser with field-specific analyzers
      final MultiFieldQueryParser queryParser =
          new MultiFieldQueryParser(searchableFields, perFieldAnalyzer);
      queryParser.setSplitOnWhitespace(true);
      queryParser.setAllowLeadingWildcard(true);

      return queryParser.parse(queryText);
    }
  }

  /**
   * Gets field-specific analyzers for a given model class based on ElasticSearch annotations.
   *
   * @param modelClass the model class
   * @return the field analyzers
   */
  @SuppressWarnings("resource")
  public static Map<String, Analyzer> getFieldAnalyzers(final Class<?> modelClass) {
    if (FIELD_ANALYZERS_CACHE.containsKey(modelClass)) {
      return FIELD_ANALYZERS_CACHE.get(modelClass);
    }

    final Map<String, Analyzer> fieldAnalyzers = new HashMap<>();
    final List<Field> allFields = ModelUtility.getAllFields(modelClass);

    for (final Field field : allFields) {
      final String fieldName = field.getName();
      final Analyzer analyzer = getAnalyzerForField(field);

      if (analyzer != null) {
        fieldAnalyzers.put(fieldName, analyzer);

        // Handle MultiField annotations
        final MultiField multiField = field.getAnnotation(MultiField.class);
        if (multiField != null) {
          for (final InnerField innerField : multiField.otherFields()) {
            final String subFieldName = fieldName + "." + innerField.suffix();
            final Analyzer subFieldAnalyzer = ("ngram".equals(innerField.suffix()))
                ? getNgramAnalyzer() : getAnalyzerForFieldType(innerField.type());
            if (subFieldAnalyzer != null) {
              fieldAnalyzers.put(subFieldName, subFieldAnalyzer);
            }
          }
        }
      }
    }

    // Handle nested fields (e.g., ancestors.code, terms.name)
    for (final String fieldPath : getNestedFieldPaths(modelClass)) {
      final Analyzer nestedAnalyzer = getAnalyzerForNestedField(fieldPath, modelClass);
      fieldAnalyzers.put(fieldPath, nestedAnalyzer);
      logger.debug("Added analyzer for nested field: {} -> {}", fieldPath,
          nestedAnalyzer.getClass().getSimpleName());
    }

    FIELD_ANALYZERS_CACHE.put(modelClass, fieldAnalyzers);
    return fieldAnalyzers;
  }

  /**
   * The ngram analyzer.
   *
   * @return the ngram analyzer
   */
  private static Analyzer getNgramAnalyzer() {
    return new Analyzer() {
      @SuppressWarnings("resource")
      @Override
      protected TokenStreamComponents createComponents(final String fieldName) {
        final Tokenizer tokenizer = new StandardTokenizer();
        final TokenStream lower = new LowerCaseFilter(tokenizer);
        final TokenStream cleaned =
            new PatternReplaceFilter(lower, Pattern.compile("[^a-z0-9]+"), "", true);
        final TokenStream ngrams = new NGramTokenFilter(cleaned, 3, 20, false);
        return new TokenStreamComponents(tokenizer, ngrams);
      }
    };
  }

  /**
   * Gets nested field paths for the model class.
   *
   * @param modelClass the model class
   * @return the nested field paths
   */
  private static Set<String> getNestedFieldPaths(final Class<?> modelClass) {
    final Set<String> nestedPaths = new HashSet<>();
    final List<Field> allFields = ModelUtility.getAllFields(modelClass);

    if (logger.isDebugEnabled()) {
      logger.debug("Looking for nested fields in class: {}", modelClass.getSimpleName());
    }

    for (final Field field : allFields) {
      final org.springframework.data.elasticsearch.annotations.Field esField =
          field.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);

      if (esField != null && esField.type() == FieldType.Object) {
        if (logger.isDebugEnabled()) {
          logger.debug("Found Object field: {}", field.getName());
        }

        // Check if it's a List of HasId objects
        if (List.class.isAssignableFrom(field.getType())) {
          final java.lang.reflect.ParameterizedType genericType =
              (java.lang.reflect.ParameterizedType) field.getGenericType();
          final Class<?> listElementType = (Class<?>) genericType.getActualTypeArguments()[0];

          if (HasId.class.isAssignableFrom(listElementType)) {
            if (logger.isDebugEnabled()) {
              logger.debug("Field {} is List<{}> implementing HasId, adding nested paths",
                  field.getName(), listElementType.getSimpleName());
            }
            // Add common keyword fields that should be exact matches
            nestedPaths.addAll(getCommonNestedPaths(field));

          }
        }
        // Check if it's a direct HasId object (not a List)
        else if (HasId.class.isAssignableFrom(field.getType())) {
          logger.debug("Field {} implements HasId, adding nested paths", field.getName());
          // Add common keyword fields that should be exact matches
          nestedPaths.addAll(getCommonNestedPaths(field));
        }
      }
    }

    logger.debug("Nested field paths found: {}", nestedPaths);
    return nestedPaths;
  }

  /**
   * Gets the common nested paths.
   *
   * @param field the field
   * @return the common nested paths
   */
  private static Set<String> getCommonNestedPaths(final Field field) {
    return NESTED_FIELD_SUFFIXES.stream().map(suffix -> field.getName() + "." + suffix)
        .collect(Collectors.toSet());
  }

  /**
   * Gets the analyzer for a specific field based on its annotations.
   *
   * @param field the field
   * @return the analyzer
   */
  private static Analyzer getAnalyzerForField(final Field field) {
    final org.springframework.data.elasticsearch.annotations.Field esField =
        field.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);
    if (esField != null) {
      return getAnalyzerForFieldType(esField.type());
    }

    // Check for MultiField annotation
    final MultiField multiField = field.getAnnotation(MultiField.class);
    if (multiField != null) {
      for (final InnerField innerField : multiField.otherFields()) {
        if ("ngram".equals(innerField.suffix())) {
          return getNgramAnalyzer();
        }
      }
      return getAnalyzerForFieldType(multiField.mainField().type());
    }

    // Default for String fields
    if (field.getType().equals(String.class)) {
      return new StandardAnalyzer();
    }

    return null;
  }

  /**
   * Gets the analyzer for a specific field type.
   *
   * @param fieldType the field type
   * @return the analyzer
   */
  private static Analyzer getAnalyzerForFieldType(final FieldType fieldType) {
    switch (fieldType) {
      case Keyword:
        return new KeywordAnalyzer();
      default:
        return new StandardAnalyzer();
    }
  }

  /**
   * Gets the analyzer for a nested field path.
   *
   * @param fieldPath the field path (e.g., "ancestors.code")
   * @param modelClass the model class
   * @return the analyzer
   */
  private static Analyzer getAnalyzerForNestedField(final String fieldPath,
    final Class<?> modelClass) {
    // For nested keyword fields, use KeywordAnalyzer for exact matching
    if (NESTED_FIELD_SUFFIXES.stream().anyMatch(suffix -> fieldPath.endsWith("." + suffix))) {
      return new KeywordAnalyzer();
    }

    // For other nested fields, use the default analyzer
    return new StandardAnalyzer();
  }

  /**
   * Returns the list of searchable fields for a given model class. Uses reflection to find String
   * or List<String> fields, or those annotated with @Field(type = FieldType.Text) or MultiField.
   *
   * @param modelClass the model class
   * @return the searchable fields
   */
  private static String[] getSearchableFields(final Class<?> modelClass) {
    if (SEARCHABLE_FIELDS_CACHE.containsKey(modelClass)) {
      return SEARCHABLE_FIELDS_CACHE.get(modelClass);
    }
    final List<Field> allFields = ModelUtility.getAllFields(modelClass);

    final List<String> fieldNames = new ArrayList<>(allFields.stream().filter(f -> {
      // Check for String or List<String>
      final Class<?> type = f.getType();
      final boolean isString = type.equals(String.class);
      final boolean isListString = List.class.isAssignableFrom(type);
      // Check for @Field(type = FieldType.Text), @Field(type =
      // FieldType.Keyword), or @MultiField
      final org.springframework.data.elasticsearch.annotations.Field esField =
          f.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);
      final MultiField multiField = f.getAnnotation(MultiField.class);
      final boolean isTextField = esField != null && esField.type() == FieldType.Text;
      final boolean isKeywordField = esField != null && esField.type() == FieldType.Keyword;
      final boolean isMultiField = multiField != null;
      return isString || isListString || isTextField || isKeywordField || isMultiField;
    }).<String> flatMap(f -> {
      // For @MultiField fields, include both the text field and keyword field
      final MultiField multiField = f.getAnnotation(MultiField.class);
      if (multiField != null) {
        return Stream.of(f.getName(), f.getName() + ".keyword", f.getName() + ".ngram");
      }
      return Stream.of(f.getName());
    }).toList());

    final List<String> subFieldNames = new ArrayList<>();
    for (final Field f : allFields) {
      final org.springframework.data.elasticsearch.annotations.Field esField =
          f.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);
      if (esField != null && esField.type() == FieldType.Object) {
        // Handle HasId objects
        if (HasId.class.isAssignableFrom(f.getType())) {
          subFieldNames.addAll(Arrays.asList(getSearchableFields(f.getType())).stream()
              .map(s -> f.getName() + "." + s).collect(Collectors.toList()));

        } else if (HasId.class.isAssignableFrom(f.getGenericType().getClass())) {
          subFieldNames.addAll(Arrays.asList(getSearchableFields(f.getGenericType().getClass()))
              .stream().map(s -> f.getName() + "." + s).collect(Collectors.toList()));

        }
        // Handle other fields just as regular fields
        else {
          subFieldNames.add(f.getName());
        }
      }
    }
    fieldNames.addAll(subFieldNames);
    final String[] result = fieldNames.toArray(new String[0]);
    SEARCHABLE_FIELDS_CACHE.put(modelClass, result);
    return result;
  }

}
