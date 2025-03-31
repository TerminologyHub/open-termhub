/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import com.wci.termhub.app.Direction;
import com.wci.termhub.model.BaseModel;
import com.wci.termhub.model.SearchParameters;

/**
 * Performs utility functions relating to Lucene indexes.
 */
public final class IndexUtility {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(IndexUtility.class);

  /**
   * Instantiates a new index utility.
   */
  private IndexUtility() {
    // private constructor to prevent instantiation
  }

  /** The default sort fields cache. */
  private static Map<String, Sort> defaultSortFieldsCache = new HashMap<>();

  /**
   * Gets the indexable fields.
   *
   * @param collection the collection
   * @param field the field
   * @return the indexable fields
   * @throws IllegalAccessException the illegal access exception
   */
  public static List<IndexableField> getIndexableFields(final Collection<?> collection,
    final java.lang.reflect.Field field) throws IllegalAccessException {

    final List<IndexableField> indexableFields = new ArrayList<>();

    logger.debug("Add: object field instance of Collection");

    for (final Object item : collection) {
      if (item instanceof String) {
        indexableFields.add(new StringField(field.getName(), (String) item,
            org.apache.lucene.document.Field.Store.NO));

      } else if (item instanceof Integer) {
        indexableFields.add(new NumericDocValuesField(field.getName(), (Integer) item));
        indexableFields.add(new StoredField(field.getName(), (Integer) item));

      } else if (item instanceof BaseModel) {

        // get all the fields of the object
        // Concatenate similar fields from the object into a single field
        logger.debug("Add: item: {}", item);
        logger.debug("Add: field: {}", field);
        logger.debug("Add: item class: {}", item.getClass().getSimpleName());

        // loop through all the fields of the object
        Class<?> innerClass = item.getClass();
        while (innerClass != null) {

          logger.debug("Add: Inner class: {}", innerClass.getName());
          for (final java.lang.reflect.Field subField : innerClass.getDeclaredFields()) {

            logger.debug("Add: Inner class sub field: {}, type: {}", subField.getName(),
                subField.getType());

            final List<IndexableField> indexableFieldsList = IndexUtility.getIndexableFields(item,
                subField, field.getName(), true);
            indexableFields.addAll(indexableFieldsList);
          }

          innerClass = innerClass.getSuperclass();
        }
      }
    }
    return indexableFields;
  }

  /**
   * Gets the indexable fields.
   *
   * @param obj the obj
   * @param field the field
   * @param indexNamePrefix the index name prefix
   * @return the indexable fields
   * @throws IllegalAccessException the illegal access exception
   */
  public static List<IndexableField> getIndexableFields(final Object obj,
    final java.lang.reflect.Field field, final String indexNamePrefix, boolean isCollection)
    throws IllegalAccessException {

    logger.debug("indexableFields: field: {}, indexNamePrefix: {}", field.getName(),
        indexNamePrefix);

    final List<IndexableField> indexableFields = new ArrayList<>();
    field.setAccessible(true);
    final Object fieldValue = field.get(obj);

    if (fieldValue != null) {

      org.springframework.data.elasticsearch.annotations.Field annotation =
          field.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);

      if (annotation != null) {

        final String indexName = ((StringUtils.isNotEmpty(indexNamePrefix)
            ? indexNamePrefix + "." + field.getName() : field.getName()));

        final String stringValue = fieldValue.toString();
        final FieldType fieldType = annotation.type();
        switch (fieldType) {
          case Text:
            indexableFields.add(
                new TextField(indexName, stringValue, org.apache.lucene.document.Field.Store.NO));
            if(isCollection){
              indexableFields.add(new SortedSetDocValuesField(indexName, new BytesRef(stringValue)));
            } else {
              indexableFields.add(new SortedDocValuesField(indexName, new BytesRef(stringValue)));
            }
            break;
          case Keyword:
            indexableFields.add(
                new StringField(indexName, stringValue, org.apache.lucene.document.Field.Store.NO));
            if(isCollection){
              indexableFields.add(new SortedSetDocValuesField(indexName, new BytesRef(stringValue)));
            } else {
              indexableFields.add(new SortedDocValuesField(indexName, new BytesRef(stringValue)));
            }
            break;
          case Date:
            final long dateValue = ((java.util.Date) fieldValue).getTime();
            if(isCollection){
             indexableFields.add(new SortedNumericDocValuesField(indexName, dateValue));
            } else {
            indexableFields.add(new NumericDocValuesField(indexName, dateValue));
            }
            indexableFields.add(new StoredField(indexName, dateValue));
            break;
          case Long:
            if(isCollection){
              indexableFields.add(new SortedNumericDocValuesField(indexName, Long.parseLong(stringValue)));
            } else {
              indexableFields.add(new NumericDocValuesField(indexName, Long.parseLong(stringValue)));
            }

            indexableFields.add(new StoredField(indexName, Long.parseLong(stringValue)));
            break;
          case Integer:
            if(isCollection){
             indexableFields.add(new SortedNumericDocValuesField(indexName, Integer.parseInt(stringValue)));
            } else {
              indexableFields
                      .add(new NumericDocValuesField(indexName, Integer.parseInt(stringValue)));
            }
            indexableFields.add(new StoredField(indexName, Integer.parseInt(stringValue)));
            break;
          case Float:
            if (isCollection) {
              indexableFields.add(new SortedNumericDocValuesField(indexName,
                  Float.floatToRawIntBits(Float.parseFloat(stringValue))));
            } else {
              indexableFields.add(new NumericDocValuesField(indexName,
                  Float.floatToRawIntBits(Float.parseFloat(stringValue))));
            }
            indexableFields.add(new StoredField(indexName, Float.parseFloat(stringValue)));
            break;
          case Double:
            if (isCollection) {
              indexableFields.add(new SortedNumericDocValuesField(indexName,
                  Double.doubleToRawLongBits(Double.parseDouble(stringValue))));
            } else {
            indexableFields.add(new NumericDocValuesField(indexName,
                Double.doubleToRawLongBits(Double.parseDouble(stringValue))));
            }
            indexableFields.add(new StoredField(indexName, Double.parseDouble(stringValue)));
            break;
          case Boolean:
            indexableFields.add(
                new StringField(indexName, stringValue, org.apache.lucene.document.Field.Store.NO));
            break;
          default:
            if (fieldType == FieldType.Object && fieldValue instanceof Collection) {
              final Collection<?> collection = (Collection<?>) fieldValue;
              for (final Object item : collection) {
                if (item instanceof String) {
                  indexableFields.add(new StringField(indexName, (String) item,
                      org.apache.lucene.document.Field.Store.NO));
                } else if (item instanceof Integer) {
                  indexableFields.add(new NumericDocValuesField(indexName, (Integer) item));
                  indexableFields.add(new StoredField(indexName, (Integer) item));
                }
              }
            }
        }
      }

      final MultiField multiFieldAnnotation = field.getAnnotation(MultiField.class);

      if (multiFieldAnnotation != null && fieldValue != null) {

        final String indexName = ((StringUtils.isNotEmpty(indexNamePrefix)
            ? indexNamePrefix + "." + field.getName() : field.getName()));

        for (final InnerField innerFieldAnnotation : multiFieldAnnotation.otherFields()) {

          final FieldType fieldType = innerFieldAnnotation.type();

          switch (fieldType) {
            case Text:
              indexableFields.add(new TextField(indexName, fieldValue.toString(),
                  org.apache.lucene.document.Field.Store.NO));
              break;
            case Keyword:
              indexableFields.add(new TextField(indexName, fieldValue.toString(),
                  org.apache.lucene.document.Field.Store.NO));
              if(isCollection){
                indexableFields
                        .add(new SortedSetDocValuesField(indexName, new BytesRef(fieldValue.toString())));
              } else{
                indexableFields
                        .add(new SortedDocValuesField(indexName, new BytesRef(fieldValue.toString())));
              }
              break;
            default:
              logger.debug("MultiField field not found Adding default field: {}", fieldType);
          }
        }
      }

    }

    logger.debug("indexableFields: {}", indexableFields);

    return indexableFields;
  }

  /**
   * Gets the default sort order.
   *
   * @param clazz the clazz
   * @return the default sort order
   */
  public static Sort getDefaultSortOrder(final Class<?> clazz) {

    final String cacheKey = clazz.getClass().getCanonicalName();
    if (defaultSortFieldsCache.containsKey(cacheKey)) {
      return defaultSortFieldsCache.get(cacheKey);
    }

    final List<SortField> sortFields = new ArrayList<>();
    Class<?> currentClass = clazz;
    while (currentClass != null) {

      final java.lang.reflect.Field[] fields = currentClass.getDeclaredFields();
      for (java.lang.reflect.Field field : fields) {

        if (field.isAnnotationPresent(com.wci.termhub.app.Sort.class)) {

          final com.wci.termhub.app.Sort sortAnnotation =
              field.getAnnotation(com.wci.termhub.app.Sort.class);
          final Direction direction = sortAnnotation.direction();
          final SortField sortField =
              new SortField(field.getName(), SortField.Type.STRING, direction == Direction.DESC);
          sortFields.add(sortField);
        }
      }
      currentClass = currentClass.getSuperclass();
    }

    if (sortFields.isEmpty()) {
      sortFields.add(new SortField("code", SortField.Type.STRING));
    }

    final Sort sort = new Sort(sortFields.toArray(new SortField[0]));
    defaultSortFieldsCache.put(cacheKey, sort);
    return sort;
  }

  /**
   * Gets the sort order.
   *
   * @param searchParameters the search parameters
   * @param clazz the clazz
   * @return the sort order
   * @throws NoSuchFieldException the no such field exception
   */
  public static Sort getSortOrder(final SearchParameters searchParameters, final Class<?> clazz)
    throws NoSuchFieldException {

    final List<String> sortFields = searchParameters.getSort();
    final SortField[] sortFieldArray = new SortField[sortFields.size()];

    for (int i = 0; i < sortFields.size(); i++) {

      final String sortField = sortFields.get(i);

      java.lang.reflect.Field field = null;
      Class<?> currentClass = clazz;
      while (currentClass != null && field == null) {
        try {
          field = currentClass.getDeclaredField(sortField);
        } catch (NoSuchFieldException e) {
          currentClass = currentClass.getSuperclass();
        }
      }

      if (field == null) {
        throw new NoSuchFieldException("Field " + sortField + " not found in class "
            + clazz.getName() + " or its superclasses");
      }

      SortField.Type sortType;
      final String fieldType = field.getType().getSimpleName();
      switch (fieldType) {
        case "String":
          sortType = SortField.Type.STRING;
          break;
        case "Integer":
          sortType = SortField.Type.INT;
          break;
        case "Long":
        case "Date":
        case "Instant":
          sortType = SortField.Type.LONG;
          break;
        case "Float":
          sortType = SortField.Type.FLOAT;
          break;
        case "Double":
          sortType = SortField.Type.DOUBLE;
          break;
        default:
          throw new IllegalArgumentException(
              "Unsupported field type for sorting: " + field.getType());
      }

      sortFieldArray[i] = new SortField(sortField, sortType, !searchParameters.getAscending());
    }

    return new Sort(sortFieldArray);
  }
}
