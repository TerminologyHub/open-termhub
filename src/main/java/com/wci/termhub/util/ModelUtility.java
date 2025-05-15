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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tika.utils.ExceptionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wci.termhub.model.HasId;
import com.wci.termhub.service.RootService;

import jakarta.persistence.metamodel.EntityType;

/**
 * Utility for interacting with domain objects.
 *
 * @param <T> the
 */
@Component
public final class ModelUtility<T> {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(ModelUtility.class);

  /** The Constant DEFAULT. */
  public static final String DEFAULT = "DEFAULT";

  /** The mapper. */
  private static ObjectMapper mapper = new ObjectMapper();

  /** The sort field cache. */
  private static Map<Class<?>, Map<String, String>> sortFieldCache = new HashMap<>();

  /**
   * Instantiates an empty {@link ModelUtility}.
   *
   * @param mapper the mapper
   */
  @Autowired
  private ModelUtility(final ObjectMapper mapper) {
    ModelUtility.mapper = mapper;
  }

  /**
   * Both or neither null.
   *
   * @param a the a
   * @param b the b
   * @return true, if successful
   */
  public static boolean bothOrNeitherNull(final Object a, final Object b) {
    return (a == null && b == null) || (a != null && b != null);
  }

  /**
   * Returns the name from class by stripping package and putting spaces where CamelCase is used.
   *
   * @param clazz the clazz
   * @return the name from class
   */
  public static String getNameFromClass(final Class<?> clazz) {
    return clazz.getName().substring(clazz.getName().lastIndexOf('.') + 1)
        .replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
            "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
  }

  /**
   * Returns the min date.
   *
   * @param dates the dates
   * @return the min date
   */
  public static Date getMinDate(final Date... dates) {
    final Set<Date> set = new HashSet<>();
    for (final Date date : dates) {
      if (date != null) {
        set.add(date);
      }
    }
    return Collections.min(set);
  }

  /**
   * Returns the max date.
   *
   * @param dates the dates
   * @return the max date
   */
  public static Date getMaxDate(final Date... dates) {
    final Set<Date> set = new HashSet<>();
    for (final Date date : dates) {
      if (date != null) {
        set.add(date);
      }
    }
    return Collections.max(set);
  }

  /**
   * Compare null safe.
   *
   * @param a the a
   * @param b the b
   * @return true, if successful
   */
  public static boolean equalsNullSafe(final Object a, final Object b) {
    if ((a == null && b != null) || (a != null && b == null)) {
      return false;
    }
    return (a == null && b == null) || (a != null && a.equals(b));
  }

  /**
   * Equals null match.
   *
   * @param a the a
   * @param b the b
   * @return true, if successful
   */
  public static boolean equalsNullMatch(final Object a, final Object b) {
    if ((a == null && b != null) || (a != null && b == null)) {
      return true;
    }
    return (a == null && b == null) || (a != null && a.equals(b));
  }

  /**
   * check if given string is uuid.
   *
   * @param id the potential id
   * @return true, if true
   */
  public static boolean isID(final String id) {
    return id.matches("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");
  }

  /**
   * First not null.
   *
   * @param <T> the
   * @param values the values
   * @return the t
   */
  @SuppressWarnings("unchecked")
  public static <T> T firstNotNull(final T... values) {
    for (final T t : values) {
      if (t != null) {
        return t;
      }
    }
    return null;
  }

  /**
   * Returns the graph for json.
   *
   * @param <T> the generic type
   * @param json the json
   * @param graphClass the graph class
   * @return the graph for json
   * @throws Exception the exception
   */
  public static <T> T toJson(final String json, final Class<T> graphClass) throws Exception {
    if (StringUtility.isEmpty(json)) {
      return null;
    }
    return mapper.readValue(json, graphClass);

  }

  /**
   * Returns the graph for json node.
   *
   * @param <T> the
   * @param json the json
   * @param graphClass the graph class
   * @return the graph for json node
   * @throws Exception the exception
   */
  public static <T> T fromJson(final String json, final Class<T> graphClass) throws Exception {
    if (StringUtility.isEmpty(json)) {
      return null;
    }
    return mapper.treeToValue(mapper.readTree(json), graphClass);
  }

  /**
   * Returns the graph for json. sample usage:
   *
   * @param <T> the
   * @param json the json
   * @param typeRef the type ref
   * @return the graph for json
   * @throws Exception the exception
   */
  public static <T> T fromJson(final String json, final TypeReference<T> typeRef) throws Exception {
    if (StringUtility.isEmpty(json)) {
      return null;
    }
    final InputStream in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    return mapper.readValue(in, typeRef);
  }

  /**
   * Returns the json for graph.
   *
   * @param object the object
   * @return the json for graph
   */
  public static String toJson(final Object object) {
    return toJson(object, true);
  }

  /**
   * To json.
   *
   * @param object the object
   * @param formatted the formatted
   * @return the string
   */
  public static String toJson(final Object object, final boolean formatted) {
    try {
      if (formatted) {
        return mapper.writeValueAsString(object);
      }
      return mapper.writer().without(SerializationFeature.INDENT_OUTPUT).writeValueAsString(object);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * To json.
   *
   * @param string the string
   * @return the json node
   */
  public static JsonNode toJsonNode(final String string) {
    try {
      return mapper.readTree(string);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Pretty format json.
   *
   * @param input the input
   * @return the string
   * @throws JsonProcessingException the json processing exception
   */
  public static String prettyFormatJson(final Object input) throws JsonProcessingException {
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(input);
  }

  /**
   * Pretty format json.
   *
   * @param input the input
   * @return the string
   * @throws Exception the exception
   */
  public static String prettyFormatJson(final String input) throws Exception {
    return prettyFormatJson(mapper.readTree(input));
  }

  /**
   * Json copy.
   *
   * @param <T> the
   * @param o the o
   * @param graphClass the graph class
   * @return the t
   * @throws Exception the exception
   */
  public static <T> T jsonCopy(final Object o, final Class<T> graphClass) throws Exception {
    final String json = toJson(o);
    return fromJson(json, graphClass);
  }

  /**
   * Reflection sort.
   *
   * @param <T> the
   * @param classes the classes
   * @param clazz the clazz
   * @param sortField the sort field
   * @throws Exception the exception
   */
  public static <T> void reflectionSort(final List<T> classes, final Class<T> clazz,
    final String sortField) throws Exception {

    final Method getMethod =
        clazz.getMethod("get" + sortField.substring(0, 1).toUpperCase() + sortField.substring(1));
    if (getMethod.getReturnType().isAssignableFrom(Comparable.class)) {
      throw new Exception("Referenced sort field is not comparable");
    }
    Collections.sort(classes, new Comparator<T>() {
      @SuppressWarnings({
          "rawtypes", "unchecked"
      })
      @Override
      public int compare(final T o1, final T o2) {
        try {
          final Comparable f1 = (Comparable) getMethod.invoke(o1, new Object[] {});
          final Comparable f2 = (Comparable) getMethod.invoke(o2, new Object[] {});
          return f1.compareTo(f2);
        } catch (final Exception e) {
          // do nothing
        }
        return 0;
      }
    });
  }

  /**
   * Indicates whether or not empty is the case.
   *
   * @param collection the collection
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public static boolean isEmpty(final Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  /**
   * Default if null.
   *
   * @param <T> the generic type
   * @param obj the obj
   * @param ifNull the if null
   * @return the t
   */
  public static <T> T nvl(final T obj, final T ifNull) {
    return obj == null ? ifNull : obj;
  }

  /**
   * As map.
   *
   * @param values the values
   * @return the map
   */
  public static Map<String, String> asMap(final String... values) {
    final Map<String, String> map = new HashMap<>();
    if (values.length % 2 != 0) {
      throw new RuntimeException("Unexpected odd number of parameters");
    }
    for (int i = 0; i < values.length; i += 2) {
      map.put(values[i], values[i + 1]);
    }
    return map;
  }

  /**
   * As list.
   *
   * @param values the values
   * @return the list
   */
  public static List<String> asList(final String[] values) {
    return new ArrayList<String>(Arrays.asList(values));
  }

  /**
   * As list.
   *
   * @param <T> the
   * @param values the values
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> asList(final T... values) {
    final List<T> list = new ArrayList<>(values.length);
    for (final T value : values) {
      if (value != null) {
        list.add(value);
      }
    }
    return list;
  }

  /**
   * As set.
   *
   * @param <T> the
   * @param values the values
   * @return the sets the
   */
  @SuppressWarnings("unchecked")
  public static <T> Set<T> asSet(final T... values) {
    final Set<T> set = new HashSet<>(values.length);
    for (final T value : values) {
      if (value != null) {
        set.add(value);
      }
    }
    return set;
  }

  /**
   * As set.
   *
   * @param values the values
   * @return the list
   * @throws Exception the exception
   */
  public static Set<String> asSet(final String[] values) throws Exception {
    return new HashSet<String>(Arrays.asList(values));
  }

  /**
   * Returns the stack trace.
   *
   * @param t the t
   * @return the stack trace
   */
  public static String getStackTrace(final Throwable t) {
    return getStackTrace(t, false);
  }

  /**
   * Returns the stack trace.
   *
   * @param t the t
   * @param wciFlag the wci flag
   * @return the stack trace
   */
  public static String getStackTrace(final Throwable t, final boolean wciFlag) {
    final String stackTrace = ExceptionUtils.getStackTrace(t);
    if (!wciFlag) {
      return stackTrace;
    }
    final String[] lines = FieldedStringTokenizer.split(stackTrace, "\n");
    final StringBuilder sb = new StringBuilder();
    boolean found = false;
    for (final String line : lines) {
      final boolean wciLine = line.contains("com.wci.");
      if (wciLine) {
        found = true;
      }
      // if found and not a wci line, stop
      if (found && !wciLine) {
        sb.append("\t...");
        break;
      }
      sb.append(line.replaceAll("\r", "")).append("\n");
    }
    return sb.toString();
  }

  /**
   * Gets the sort field.
   *
   * @param <T> the generic type
   * @param clazz the clazz
   * @param field the field
   * @return the sort field
   */
  public static <T> String getSortField(final Class<T> clazz, final String field) {
    // return sortField if in cache
    if (sortFieldCache.containsKey(clazz)) {
      final Map<String, String> fieldToSortFieldMap = sortFieldCache.get(clazz);
      if (fieldToSortFieldMap.containsKey(field)) {
        return fieldToSortFieldMap.get(field);
      }
    }

    // examine field annotations to determine sortField
    String sortField = field;
    try {
      Field candidateField;
      // if collection, call recursively
      if (field.contains(".")) {
        final String firstTerm = field.substring(0, field.indexOf('.'));
        candidateField = getDeclaredField(clazz, firstTerm);

        final Type type = candidateField.getGenericType();
        if (type instanceof ParameterizedType) {
          final ParameterizedType pType = (ParameterizedType) type;
          logger.debug("Raw type: " + pType.getRawType() + " - ");
          logger.debug("Type args: " + pType.getActualTypeArguments()[0]);
          sortField = firstTerm + "." + getSortField((Class<?>) pType.getActualTypeArguments()[0],
              field.substring(field.indexOf('.') + 1));
          cacheSortField(clazz, field, sortField);
          return sortField;
        }
      }

      // not collection, check for annotation suffix and type=Text
      candidateField = getDeclaredField(clazz, field);
      final MultiField multiField = candidateField.getAnnotation(MultiField.class);
      final InnerField[] innerFields = multiField.otherFields();
      final org.springframework.data.elasticsearch.annotations.Field mainField =
          multiField.mainField();

      for (final InnerField inner : innerFields) {
        if (mainField.type().name().equals("Text") && !inner.suffix().isBlank()
            && !inner.suffix().equals("ngram")) {
          sortField = field + "." + inner.suffix();
        }
      }

    } catch (final Exception e) {
      // do nothing
    } finally {
      // cache regardless if sortField was different from field
      cacheSortField(clazz, field, sortField);
    }

    return sortField;
  }

  /**
   * Cache sort field.
   *
   * @param clazz the clazz
   * @param field the field
   * @param sortField the sort field
   */
  private static void cacheSortField(final Class<?> clazz, final String field,
    final String sortField) {
    if (sortFieldCache.containsKey(clazz)) {
      final Map<String, String> fieldToSortFieldMap = sortFieldCache.get(clazz);
      fieldToSortFieldMap.put(field, sortField);
    } else {
      final Map<String, String> fieldToSortFieldMap = new HashMap<>();
      fieldToSortFieldMap.put(field, sortField);
      sortFieldCache.put(clazz, fieldToSortFieldMap);
    }
  }

  /**
   * Gets the declared field. This method is necessary to ensure superclasses are searched for the
   * field in addition to the clazz parameter.
   *
   * @param clazz the clazz
   * @param field the field
   * @return the declared field
   */
  private static Field getDeclaredField(final Class<?> clazz, final String field) {
    final List<Field> fields = new ArrayList<Field>();
    for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
      fields.addAll(Arrays.asList(c.getDeclaredFields()));
    }
    for (final Field f : fields) {
      if (f.getName().equals(field)) {
        return f;
      }
    }
    return null;
  }

  /**
   * Returns the managed objects.
   *
   * @param service the service
   * @param objects the comma separated list of simple object names (null for all managed objects)
   * @return the managed objects
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public static List<Class<? extends HasId>> getManagedObjects(final RootService service,
    final String objects) throws Exception {

    // Turn objects param into a set
    final Set<String> objectsSet = (objects == null || objects.isEmpty() || objects.equals("null"))
        ? new HashSet<>() : Arrays.asList(objects.split(",")).stream().collect(Collectors.toSet());
    logger.info("  objects = " + objects);

    // Find indexed objects
    final Reflections reflections = new Reflections("com.wci.termhub.model");
    final Set<Class<?>> indexedSet = new HashSet<>(reflections
        .getTypesAnnotatedWith(org.springframework.data.elasticsearch.annotations.Document.class));
    logger.info("  indexedSet = " + indexedSet);

    // Find managed classes
    @SuppressWarnings("resource")
    final Set<EntityType<?>> entities = service.getEntityManager().getMetamodel().getEntities();
    final List<Class<?>> classes = entities.stream().map(EntityType::getJavaType)
        .filter(o -> o != null).collect(Collectors.toList());
    logger.info("  classes = " + classes);

    // Ensure all objects to delete are in indexedSet, otherwise fail
    if (!objectsSet.isEmpty()) {

      int ct = 0;
      for (final Class<?> clazz : classes) {
        if (indexedSet.contains(clazz) && objectsSet.contains(clazz.getSimpleName())) {
          ct++;
        }
      }
      if (ct != objectsSet.size()) {
        throw new Exception("Not all specified objects are indexed");
      }
    }

    // Go through entity classes and identify indexed ones and any specific
    // object
    // ones
    return classes.stream().filter(clazz -> {
      if (indexedSet.contains(clazz)) {
        // Verify that its specified or that nothing was specified
        if (objects == null || objects.isEmpty() || objects.equals("null")
            || objectsSet.contains(clazz.getSimpleName())) {
          return true;
        }
      }
      return false;
    }).map(c -> (Class<? extends HasId>) c).collect(Collectors.toList());
  }

  /**
   * Gets the indexed objects.
   *
   * @param objects the objects
   * @return the indexed objects
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public static List<Class<? extends HasId>> getIndexedObjects(final String objects)
    throws Exception {

    // Turn objects param into a set
    final Set<String> objectsSet = (objects == null || objects.isEmpty() || objects.equals("null"))
        ? new HashSet<>() : Arrays.asList(objects.split(",")).stream().collect(Collectors.toSet());
    logger.info("  objects = " + objects);

    // Find indexed objects
    final Reflections reflections = new Reflections("com.wci.termhub.model");
    final Set<Class<?>> indexedSet = new HashSet<>(reflections
        .getTypesAnnotatedWith(org.springframework.data.elasticsearch.annotations.Document.class));
    logger.info("  indexedSet = " + indexedSet);

    // Ensure all objects to delete are in indexedSet, otherwise fail
    if (!objectsSet.isEmpty()) {

      int ct = 0;
      for (final Class<?> clazz : indexedSet) {
        if (objectsSet.contains(clazz.getSimpleName())) {
          ct++;
        }
      }
      if (ct != objectsSet.size()) {
        throw new Exception("Not all specified objects are indexed");
      }
    }

    // Go through entity classes and identify indexed ones and any specific
    // object
    // ones
    return indexedSet.stream()
        .filter(clazz -> objects == null || objects.isEmpty() || objects.equals("null")
            || objectsSet.contains(clazz.getSimpleName()))
        .map(c -> (Class<? extends HasId>) c).collect(Collectors.toList());

  }

  /**
   * Gets the base fields.
   *
   * @param type the type
   * @return the base fields
   */
  public static List<java.lang.reflect.Field> getBaseFields(final Class<?> type) {
    return getAllFields(type).stream().filter(f -> f.getType().isAssignableFrom(String.class)
        || f.getType().isAssignableFrom(Date.class) || f.getType().isAssignableFrom(Boolean.class)
        || f.getType().isAssignableFrom(Integer.class) || f.getType().isAssignableFrom(Long.class)
        || f.getType().isAssignableFrom(Float.class) || f.getType().isAssignableFrom(Double.class))
        .collect(Collectors.toList());
  }

  /**
   * Gets the all fields.
   *
   * @param type the type
   * @return the all fields
   */
  public static List<java.lang.reflect.Field> getAllFields(final Class<?> type) {
    final List<java.lang.reflect.Field> fields = new ArrayList<>();
    if (type.getSuperclass() != null) {
      for (final java.lang.reflect.Field field : type.getDeclaredFields()) {
        fields.add(field);
      }
      fields.addAll(getAllFields(type.getSuperclass()));
      return fields;
    }
    for (final java.lang.reflect.Field field : type.getDeclaredFields()) {
      fields.add(field);
    }
    return fields;
  }

  // /**
  // * Checks if is soft deleted.
  // *
  // * @param obj the obj
  // * @return true, if is soft deleted
  // */
  // public static boolean isSoftDeleted(final HasSoftDelete obj) {
  // return obj.getSoftDeleted() != null && obj.getSoftDeleted();
  // }

  /**
   * Gets the annotation.
   *
   * @param <T> the generic type
   * @param clazz the clazz
   * @param type the type
   * @return the annotation
   */
  @SuppressWarnings("unchecked")
  public static <T> T getAnnotation(final Class<?> clazz, final Class<T> type) {
    for (final Annotation annotation : clazz.getAnnotations()) {
      if (type.isAssignableFrom(annotation.getClass())) {
        return (T) annotation;
      }
    }
    return null;
  }
}
