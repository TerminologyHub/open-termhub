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

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.MultiField;

import com.wci.termhub.model.Concept;
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
    // Check if this is a complex query with OR operators or multiple fielded
    // parts
    final boolean isComplexQuery = queryText != null && (queryText.contains(" OR ")
        || (queryText.contains(" AND ") && queryText.matches(".*\\w+:.*\\w+:.*")));

    if (isComplexQuery) {
      // Handle complex queries with OR or AND operators
      try {
        if (queryText.contains(" OR ")) {
          // Handle OR queries like "(cancer) OR (normName:cancer)"
          final String[] parts = queryText.split(" OR ");
          if (parts.length == 2) {
            final String leftPart = parts[0].trim().replaceAll("^\\(", "").replaceAll("\\)$", "");
            final String rightPart = parts[1].trim().replaceAll("^\\(", "").replaceAll("\\)$", "");

            // Parse each part separately
            final Query leftQuery = parse(leftPart, modelClass);
            final Query rightQuery = parse(rightPart, modelClass);

            // Combine with OR
            final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
            booleanQueryBuilder.add(leftQuery, BooleanClause.Occur.SHOULD);
            booleanQueryBuilder.add(rightQuery, BooleanClause.Occur.SHOULD);

            final Query query = booleanQueryBuilder.build();
            if (logger.isDebugEnabled()) {
              logger.debug("    parsed query (complex OR): {} for class {}", query,
                  modelClass != null ? modelClass.getSimpleName() : "null");
            }
            return query;
          }
        } else if (queryText.contains(" AND ")) {
          // Handle AND queries like "name: diabetes AND terminology: SNOMEDCT"
          // Clean up malformed parentheses and extra characters
          String cleanedQuery = queryText.trim();
          // Remove trailing extra parentheses
          while (cleanedQuery.endsWith(")")) {
            cleanedQuery = cleanedQuery.substring(0, cleanedQuery.length() - 1).trim();
          }
          // Remove leading extra parentheses
          while (cleanedQuery.startsWith("(")) {
            cleanedQuery = cleanedQuery.substring(1).trim();
          }

          final String[] parts = cleanedQuery.split(" AND ");
          if (parts.length >= 2) {
            final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

            for (final String part : parts) {
              final String trimmedPart = part.trim();
              if (!trimmedPart.isEmpty()) {
                // Clean up each part - remove extra parentheses and quotes
                String cleanPart = trimmedPart;
                // Remove surrounding quotes if they exist
                if (cleanPart.startsWith("\"") && cleanPart.endsWith("\"")) {
                  cleanPart = cleanPart.substring(1, cleanPart.length() - 1);
                }
                // Remove extra parentheses
                while (cleanPart.startsWith("(") && cleanPart.endsWith(")")) {
                  cleanPart = cleanPart.substring(1, cleanPart.length() - 1).trim();
                }

                // Parse each part separately
                final Query partQuery = parse(cleanPart, modelClass);
                booleanQueryBuilder.add(partQuery, BooleanClause.Occur.MUST);
              }
            }

            final Query combinedQuery = booleanQueryBuilder.build();
            if (logger.isDebugEnabled()) {
              logger.debug("    parsed query (complex AND): {} for class {}", combinedQuery,
                  modelClass != null ? modelClass.getSimpleName() : "null");
            }
            return combinedQuery;
          }
        }
      } catch (Exception e) {
        // Fall back to simple parsing if complex parsing fails
        if (logger.isDebugEnabled()) {
          logger.debug("    complex query parsing failed, falling back to simple parsing: {}",
              e.getMessage());
        }
      }
    }

    final boolean isFieldedQuery = queryText != null && queryText.matches(".*\\w+:.*");
    if (isFieldedQuery) {
      // Transform fielded queries to use .keyword suffix for exact matching
      String transformedQuery = transformFieldedQuery(queryText, modelClass);
      try (final KeywordAnalyzer analyzer = new KeywordAnalyzer();) {
        // Extract the field name from the transformed query
        final String fieldName = transformedQuery.split(":")[0];
        final String fieldQueryText = transformedQuery.substring(fieldName.length() + 1);
        final QueryParser queryParser = new QueryParser(fieldName, analyzer);
        final Query query = queryParser.parse(fieldQueryText);
        if (logger.isDebugEnabled()) {
          logger.debug("    parsed query: {} (transformed from: {}) for class {}", query, queryText,
              modelClass != null ? modelClass.getSimpleName() : "null");
        }
        return query;
      }
    } else {

      // Handle non-fielded queries by searching across all searchable fields
      final String[] fields = getSearchableFields(modelClass != null ? modelClass : Concept.class);

      // For .keyword fields with regex syntax, we need to use KeywordAnalyzer
      // to avoid tokenizing the regex syntax
      try (final KeywordAnalyzer analyzer = new KeywordAnalyzer();) {
        final MultiFieldQueryParser multiFieldQueryParser =
            new MultiFieldQueryParser(fields, analyzer);

        // For .keyword fields, we need to use regex queries since the data
        // contains full phrases
        // Transform the query to use regex syntax for .keyword fields
        final String regexQuery = transformToWildcardQuery(queryText, fields);
        final Query query = multiFieldQueryParser.parse(regexQuery);
        if (logger.isDebugEnabled()) {
          logger.debug("    parsed query (multi-field): {} for class {}", query,
              modelClass != null ? modelClass.getSimpleName() : "null");
        }
        return query;
      }
    }
  }

  /**
   * Transform non-fielded queries to use proper syntax for .keyword fields.
   * This is needed because .keyword fields contain full phrases, not individual
   * tokens.
   *
   * @param queryText the original query text
   * @param fields the searchable fields
   * @return the transformed query text with proper syntax for .keyword fields
   */
  private static String transformToWildcardQuery(final String queryText, final String[] fields) {
    if (queryText == null || queryText.trim().isEmpty()) {
      return queryText;
    }

    final String trimmedQuery = queryText.trim();
    if ("*:*".equals(trimmedQuery)) {
      return trimmedQuery;
    }

    final StringBuilder wildcardQuery = new StringBuilder();
    for (int i = 0; i < fields.length; i++) {
      if (i > 0) {
        wildcardQuery.append(" ");
      }

      final String field = fields[i];
      if (field.endsWith(".keyword")) {
        // For .keyword fields, use a regex query to match the term anywhere in
        // the field
        // This allows us to match the term anywhere within the phrase
        wildcardQuery.append(field).append(":/.*").append(trimmedQuery).append(".*/");
      } else {
        // For regular fields, use the original query
        wildcardQuery.append(field).append(":").append(trimmedQuery);
      }
    }

    return wildcardQuery.toString();
  }

  /**
   * Transform fielded queries to use .keyword suffix with wildcard matching.
   * This handles the Elasticsearch @MultiField mapping where fields like 'name'
   * are actually indexed as 'name.keyword' for exact matching, but we need
   * wildcard queries for partial matching since the data contains full phrases.
   *
   * @param queryText the original query text
   * @param modelClass the model class
   * @return the transformed query text
   */
  private static String transformFieldedQuery(final String queryText, final Class<?> modelClass) {
    if (queryText == null) {
      return queryText;
    }

    String transformed = queryText;

    // Define field transformation patterns
    final Map<String, FieldTransform> fieldTransforms = Map.of("name",
        new FieldTransform("^name\\s*:\\s*.*", "name", modelClass), "normName",
        new FieldTransform("^\\s*normName\\s*:\\s*[^\\s].*\\s*$", "normName.keyword"), "stemName",
        new FieldTransform("^\\s*stemName\\s*:\\s*[^\\s].*\\s*$", "stemName.keyword"),
        "subset.abbreviation",
        new FieldTransform("^\\s*subset\\.abbreviation\\s*:\\s*.*", "subset.abbreviation"),
        "subset.publisher",
        new FieldTransform("^\\s*subset\\.publisher\\s*:\\s*.*", "subset.publisher"),
        "subset.version", new FieldTransform("^\\s*subset\\.version\\s*:\\s*.*", "subset.version"));

    // Apply transformations
    for (final Map.Entry<String, FieldTransform> entry : fieldTransforms.entrySet()) {
      final FieldTransform transform = entry.getValue();
      if (transformed.matches(transform.pattern) && !transformed.contains(" AND ")
          && !transformed.contains(" OR ")) {
        final String searchTerm = extractSearchTerm(transformed, transform.pattern);
        if (!searchTerm.isEmpty()) {
          transformed = buildRegexQuery(searchTerm, transform, modelClass);
          break;
        }
      }
    }

    return transformed;
  }

  /**
   * Extract search term.
   *
   * @param queryText the query text
   * @param pattern the pattern
   * @return the string
   */
  private static String extractSearchTerm(final String queryText, final String pattern) {
    final String regexPattern = pattern.replace("^\\s*", "").replace("\\s*$", "").replace(".*", "");
    return queryText.replaceFirst(regexPattern, "").trim();
  }

  /**
   * Builds the regex query.
   *
   * @param searchTerm the search term
   * @param transform the transform
   * @param modelClass the model class
   * @return the string
   */
  private static String buildRegexQuery(final String searchTerm, final FieldTransform transform,
    final Class<?> modelClass) {
    final String cleanSearchTerm = searchTerm.replace("\\-", "-").replace("\\:", ":");
    final String fieldName = transform.getFieldName(modelClass);
    return fieldName + ":/.*" + cleanSearchTerm + ".*/";
  }

  /**
   * Check if a model class uses MultiField annotations (like Concept/Term) vs
   * direct keyword fields (like TestDocument).
   *
   * @param modelClass the model class to check
   * @return true if the model uses MultiField annotations, false otherwise
   */
  private static boolean isMultiFieldModel(final Class<?> modelClass) {
    if (modelClass == null) {
      return true; // Default to MultiField behavior for null class
    }

    // Check if any field in the model class has @MultiField annotation
    final List<Field> allFields = ModelUtility.getAllFields(modelClass);
    return allFields.stream().anyMatch(f -> f.getAnnotation(MultiField.class) != null);
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
    }).map(f -> {
      // For @MultiField fields, use the .keyword suffix for exact matching
      final MultiField multiField = f.getAnnotation(MultiField.class);
      if (multiField != null) {
        return f.getName() + ".keyword";
      }
      return f.getName();
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

  /**
   * The Class FieldTransform.
   */
  private static class FieldTransform {

    /** The pattern. */
    private final String pattern;

    /** The field name. */
    private final String fieldName;

    /** The is multi field. */
    private final boolean isMultiField;

    /**
     * Instantiates a new field transform.
     *
     * @param pattern the pattern
     * @param fieldName the field name
     * @param modelClass the model class
     */
    public FieldTransform(final String pattern, final String fieldName, final Class<?> modelClass) {
      this.pattern = pattern;
      this.fieldName = fieldName;
      this.isMultiField = modelClass != null && isMultiFieldModel(modelClass);
    }

    /**
     * Instantiates a new field transform.
     *
     * @param pattern the pattern
     * @param fieldName the field name
     */
    public FieldTransform(final String pattern, final String fieldName) {
      this.pattern = pattern;
      this.fieldName = fieldName;
      this.isMultiField = false;
    }

    /**
     * Gets the field name.
     *
     * @param modelClass the model class
     * @return the field name
     */
    public String getFieldName(final Class<?> modelClass) {
      if ("name".equals(fieldName) && isMultiField) {
        return "name.keyword";
      }
      return fieldName;
    }
  }

}
