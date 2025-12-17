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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.Document;
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
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(LuceneQueryBuilder.class);


  /** The max clause count. */
  public static final int MAX_CLAUSE_COUNT = 32768;

  static {
    IndexSearcher.setMaxClauseCount(MAX_CLAUSE_COUNT);
  }

  /**
   * Instantiates a new lucene query builder.
   */
  private LuceneQueryBuilder() {
    // private constructor to prevent instantiation
  }

  /** The Constant SEARCHABLE_FIELDS_CACHE. */
  private static final Map<Class<?>, String[]> SEARCHABLE_FIELDS_CACHE = new HashMap<>();

  /**
   * Cache of analyzer "types" per class/field to avoid reflection every time.
   */
  private static final Map<Class<?>, Map<String, AnalyzerType>> FIELD_ANALYZER_TYPES_CACHE =
      new HashMap<>();

  /**
   * Parses the query for a specific model class.
   *
   * @param queryText the query text
   * @param modelClass the model class
   * @return the query
   * @throws ParseException the parse exception
   */
  @SuppressWarnings("resource")
  public static Query parse(final String queryText, final Class<?> modelClass)
    throws ParseException {

    // Build analyzers from cached types
    final Map<String, AnalyzerType> analyzerTypes = getFieldAnalyzerTypes(modelClass);
    final Map<String, Analyzer> fieldAnalyzers = new HashMap<>();
    for (final Map.Entry<String, AnalyzerType> entry : analyzerTypes.entrySet()) {
      fieldAnalyzers.put(entry.getKey(), entry.getValue().newInstance());
    }
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
    final Map<String, AnalyzerType> types = getFieldAnalyzerTypes(modelClass);
    final Map<String, Analyzer> analyzers = new HashMap<>();
    for (final Map.Entry<String, AnalyzerType> e : types.entrySet()) {
      analyzers.put(e.getKey(), e.getValue().newInstance());
    }
    return analyzers;
  }

  /**
   * Returns cached analyzer types (not instances) by field for a model class.
   *
   * @param modelClass the model class
   * @return the field analyzer types
   */
  public static Map<String, AnalyzerType> getFieldAnalyzerTypes(final Class<?> modelClass) {
    if (FIELD_ANALYZER_TYPES_CACHE.containsKey(modelClass)) {
      return FIELD_ANALYZER_TYPES_CACHE.get(modelClass);
    }

    final Map<String, AnalyzerType> fieldAnalyzerTypes = new HashMap<>();

    // Recursively collect analyzer types for this model and any nested models
    collectAnalyzerTypesForClass(modelClass, null, fieldAnalyzerTypes);

    FIELD_ANALYZER_TYPES_CACHE.put(modelClass, fieldAnalyzerTypes);
    return fieldAnalyzerTypes;
  }

  /**
   * Recursively collects analyzers for fields on a model class and nested model classes based on
   * ElasticSearch annotations. Uses a dotted prefix for nested objects/collections.
   *
   * @param modelClass the model class
   * @param prefix the field path prefix, or null for top-level
   * @param fieldAnalyzerTypes the field analyzer types
   */
  private static void collectAnalyzerTypesForClass(final Class<?> modelClass, final String prefix,
    final Map<String, AnalyzerType> fieldAnalyzerTypes) {

    final List<Field> allFields = ModelUtility.getAllFields(modelClass);

    for (final Field field : allFields) {
      final String fieldName = field.getName();
      final String fieldPath =
          (prefix == null || prefix.isEmpty()) ? fieldName : prefix + "." + fieldName;

      // Handle @MultiField first (Text + inner keyword/ngram)
      final MultiField multiField = field.getAnnotation(MultiField.class);
      if (multiField != null) {
        // Main field
        final AnalyzerType mainType = getAnalyzerTypeForFieldType(multiField.mainField().type());
        fieldAnalyzerTypes.put(fieldPath, mainType);
        // Inner fields
        for (final InnerField innerField : multiField.otherFields()) {
          final String subFieldPath = fieldPath + "." + innerField.suffix();
          final AnalyzerType subType = ("ngram".equals(innerField.suffix())) ? AnalyzerType.NGRAM
              : getAnalyzerTypeForFieldType(innerField.type());
          fieldAnalyzerTypes.put(subFieldPath, subType);
        }
      }

      // Handle single @Field annotation
      final org.springframework.data.elasticsearch.annotations.Field esField =
          field.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);
      if (esField != null) {
        // Map analyzer for this concrete field
        final AnalyzerType type = getAnalyzerTypeForFieldType(esField.type());
        fieldAnalyzerTypes.put(fieldPath, type);

        // If it's an object, recurse into nested model types
        if (esField.type() == FieldType.Object) {
          // If this is a collection, extract the element type
          if (List.class.isAssignableFrom(field.getType())) {
            try {
              final java.lang.reflect.Type genericType = field.getGenericType();
              if (genericType instanceof java.lang.reflect.ParameterizedType) {
                final java.lang.reflect.ParameterizedType parameterizedType =
                    (java.lang.reflect.ParameterizedType) genericType;
                final java.lang.reflect.Type elementType =
                    parameterizedType.getActualTypeArguments()[0];
                if (elementType instanceof Class) {
                  final Class<?> elementClass = (Class<?>) elementType;
                  if (isDocumentModel(elementClass)) {
                    collectAnalyzerTypesForClass(elementClass, fieldPath, fieldAnalyzerTypes);
                  }
                }
              }
            } catch (final Exception e) {
              // Defensive: ignore bad generic info
            }
          } else {
            final Class<?> nestedClass = field.getType();
            if (isDocumentModel(nestedClass)) {
              collectAnalyzerTypesForClass(nestedClass, fieldPath, fieldAnalyzerTypes);
            }
          }
        }
      }

      // If there are no annotations but it's a String, default to
      // StandardAnalyzer
      if (multiField == null && esField == null && field.getType().equals(String.class)) {
        fieldAnalyzerTypes.put(fieldPath, AnalyzerType.STANDARD);
      }
    }
  }

  /**
   * Determines if a class is a model eligible for nested recursion. Models should be annotated
   * with @Document, but HasId is allowed as a fallback.
   *
   * @param clazz the class
   * @return true, if the class is a model
   */
  private static boolean isDocumentModel(final Class<?> clazz) {
    return clazz.getAnnotation(Document.class) != null || HasId.class.isAssignableFrom(clazz);
  }

  /**
   * Gets the analyzer for a specific field type.
   *
   * @param fieldType the field type
   * @return the analyzer
   */
  private static AnalyzerType getAnalyzerTypeForFieldType(final FieldType fieldType) {
    switch (fieldType) {
      case Keyword:
        return AnalyzerType.KEYWORD;
      default:
        return AnalyzerType.STANDARD;
    }
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
    }).flatMap(f -> {
      // For @MultiField fields, include both the text field and keyword field
      final MultiField multiField = f.getAnnotation(MultiField.class);
      if (multiField != null) {
        return Stream.of(f.getName(), f.getName() + ".ngram");
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
