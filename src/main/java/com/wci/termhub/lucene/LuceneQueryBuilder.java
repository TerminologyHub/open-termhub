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
  public static Map<String, Analyzer> getFieldAnalyzers(final Class<?> modelClass) {
    if (FIELD_ANALYZERS_CACHE.containsKey(modelClass)) {
      return FIELD_ANALYZERS_CACHE.get(modelClass);
    }

    final Map<String, Analyzer> fieldAnalyzers = new HashMap<>();

    // Recursively collect analyzers for this model and any nested
    // @Document/HasId
    // models
    collectAnalyzersForClass(modelClass, null, fieldAnalyzers);

    FIELD_ANALYZERS_CACHE.put(modelClass, fieldAnalyzers);
    return fieldAnalyzers;
  }

  /**
   * Recursively collects analyzers for fields on a model class and nested model classes based on
   * ElasticSearch annotations. Uses a dotted prefix for nested objects/collections.
   *
   * @param modelClass the model class
   * @param prefix the field path prefix, or null for top-level
   * @param fieldAnalyzers the output map of field path to analyzer
   */
  @SuppressWarnings("resource")
  private static void collectAnalyzersForClass(final Class<?> modelClass, final String prefix,
    final Map<String, Analyzer> fieldAnalyzers) {

    final List<Field> allFields = ModelUtility.getAllFields(modelClass);

    for (final Field field : allFields) {
      final String fieldName = field.getName();
      final String fieldPath =
          (prefix == null || prefix.isEmpty()) ? fieldName : prefix + "." + fieldName;

      // Handle @MultiField first (Text + inner keyword/ngram)
      final MultiField multiField = field.getAnnotation(MultiField.class);
      if (multiField != null) {
        // Main field
        final Analyzer mainAnalyzer = getAnalyzerForFieldType(multiField.mainField().type());
        if (mainAnalyzer != null) {
          fieldAnalyzers.put(fieldPath, mainAnalyzer);
        }
        // Inner fields
        for (final InnerField innerField : multiField.otherFields()) {
          final String subFieldPath = fieldPath + "." + innerField.suffix();
          final Analyzer subFieldAnalyzer = ("ngram".equals(innerField.suffix()))
              ? getNgramAnalyzer() : getAnalyzerForFieldType(innerField.type());
          if (subFieldAnalyzer != null) {
            fieldAnalyzers.put(subFieldPath, subFieldAnalyzer);
          }
        }
      }

      // Handle single @Field annotation
      final org.springframework.data.elasticsearch.annotations.Field esField =
          field.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);
      if (esField != null) {
        // Map analyzer for this concrete field
        final Analyzer analyzer = getAnalyzerForFieldType(esField.type());
        if (analyzer != null) {
          fieldAnalyzers.put(fieldPath, analyzer);
        }

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
                    collectAnalyzersForClass(elementClass, fieldPath, fieldAnalyzers);
                  }
                }
              }
            } catch (final Exception e) {
              // Defensive: ignore bad generic info
            }
          } else {
            final Class<?> nestedClass = field.getType();
            if (isDocumentModel(nestedClass)) {
              collectAnalyzersForClass(nestedClass, fieldPath, fieldAnalyzers);
            }
          }
        }
      }

      // If there are no annotations but it's a String, default to
      // StandardAnalyzer
      if (multiField == null && esField == null && field.getType().equals(String.class)) {
        fieldAnalyzers.put(fieldPath, new StandardAnalyzer());
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
