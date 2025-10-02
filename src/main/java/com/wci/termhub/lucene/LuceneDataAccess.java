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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.model.BaseModel;
import com.wci.termhub.model.HasId;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.open.configuration.ApplicationProperties;
import com.wci.termhub.util.IndexUtility;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.ThreadLocalMapper;

/**
 * Lucene data access manager.
 */
@Component
public class LuceneDataAccess {

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(LuceneDataAccess.class);

  /** The application properties. */
  @Autowired
  private ApplicationProperties applicationProperties;

  /** The Constant readerMap. */
  private static final Map<String, IndexReader> READER_MAP = new HashMap<>();

  /** The Constant writerMap. */
  private static final Map<String, IndexWriter> WRITER_MAP = new HashMap<>();

  /**
   * Instantiates a new lucene data access.
   */
  public LuceneDataAccess() {
    // n/a
  }

  /**
   * Sets the application properties.
   *
   * @param applicationProperties the new application properties
   */
  public void setApplicationProperties(final ApplicationProperties applicationProperties) {
    this.applicationProperties = applicationProperties;
  }

  /**
   * Creates the index.
   *
   * @param clazz the clazz
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public void createIndex(final Class<? extends HasId> clazz) throws Exception {

    // Create only if the index does not exist
    final File indexDir = getIndexDirectory(clazz);
    if (indexDir.exists()) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.debug("Index already exists: {}", indexDir.getAbsolutePath());
      }
      return;
    }
    // Create a new IndexWriter with default config to initialize the index
    final FSDirectory directory = FSDirectory.open(indexDir.toPath());
    final Analyzer perFieldAnalyzer = buildPerFieldAnalyzer(clazz);

    // Create IndexWriter with per-field analyzer
    final IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(perFieldAnalyzer));

    WRITER_MAP.put(clazz.getCanonicalName(), writer);
    // Commit to create the initial index structure
    writer.commit();
    clearReaderForClass(clazz);
  }

  /**
   * Delete index.
   *
   * @param clazz the clazz
   * @throws Exception the exception
   */
  public void deleteIndex(final Class<? extends HasId> clazz) throws Exception {

    final String indexDirectory = clazz.getCanonicalName();
    // Use File for better cross-platform path handling
    final File indexDir = getIndexDirectory(clazz);
    LOGGER.info("Deleting index {} from {}", indexDirectory, indexDir.getAbsolutePath());
    if (indexDir.exists()) {
      clearReaderForClass(clazz);
      // need to close writer first
      clearWriterForClass(clazz);
      FileUtils.deleteDirectory(indexDir);
    }
  }

  /**
   * Adds the batch.
   *
   * @param entities the entities
   * @throws Exception the exception
   */
  public void add(final List<? extends HasId> entities) throws Exception {
    Set<Class<? extends HasId>> entityClasses = new HashSet<>();
    for (final HasId entity : entities) {
      @SuppressWarnings("resource")
      final IndexWriter writer = getIndexWriter(entity.getClass());
      final Document document = getDocument(entity);
      writer.addDocument(document);
      entityClasses.add(entity.getClass());
    }
    // Commit all writers
    for (final Class<? extends HasId> entityClass : entityClasses) {
      @SuppressWarnings("resource")
      final IndexWriter writer = getIndexWriter(entityClass);
      writer.commit();
      clearReaderForClass(entityClass);
    }
  }

  /**
   * Adds the entity to the index specified by the entity class name.
   *
   * @param entity the entity
   * @throws Exception the exception
   */
  public void add(final HasId entity) throws Exception {
    add(List.of(entity));
  }

  /**
   * Gets the document.
   *
   * @param entity the entity
   * @return the document
   * @throws IllegalAccessException the illegal access exception
   */
  private Document getDocument(final HasId entity) throws IllegalAccessException {

    final Document document = new Document();
    document.add(new StoredField("entity", ModelUtility.toJson(entity)));
    Class<?> currentClass = entity.getClass();

    while (currentClass != null) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Add: Current class: {}", currentClass.getName());
      }

      for (final java.lang.reflect.Field field : currentClass.getDeclaredFields()) {

        field.setAccessible(true);
        final Object fieldValue = field.get(entity);
        if (fieldValue == null) {
          continue;
        }

        final Field annotation = field.getAnnotation(Field.class);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("Field: {}, value: {}, annotation: {}", field.getName(), fieldValue,
              annotation);
        }

        if (annotation != null) {

          final FieldType fieldType = annotation.type();

          // if not collection of objects, add the field to the document
          if (fieldType != FieldType.Object) {
            if (fieldValue instanceof Collection) {
              final Collection<?> collection = (Collection<?>) fieldValue;
              final List<IndexableField> indexableFieldsList =
                  IndexUtility.getIndexableFields(collection, field);
              for (final IndexableField indexableField : indexableFieldsList) {
                document.add(indexableField);
              }
            } else {
              if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Add: field instance of NOT Object OR Collection");
              }
              final List<IndexableField> indexableFieldsList = IndexUtility
                  .getIndexableFields(entity, field, null, fieldValue instanceof Collection);
              for (final IndexableField indexableField : indexableFieldsList) {
                document.add(indexableField);
              }
            }

          } else if (fieldType == FieldType.Object && fieldValue instanceof Collection) {

            if (LOGGER.isTraceEnabled()) {
              LOGGER.trace("Add: object field instance of Collection");
            }
            final Collection<?> collection = (Collection<?>) fieldValue;
            final List<IndexableField> indexableFieldsList =
                IndexUtility.getIndexableFields(collection, field);
            for (final IndexableField indexableField : indexableFieldsList) {
              document.add(indexableField);
            }

          } else if (fieldType == FieldType.Object && fieldValue instanceof BaseModel) {

            if (LOGGER.isTraceEnabled()) {
              LOGGER.trace("Add: object field instance of BaseModel");
            }
            final Object refEntity = fieldValue;
            Class<?> refClass = refEntity.getClass();
            while (refClass != null) {
              for (final java.lang.reflect.Field subClassField : refClass.getDeclaredFields()) {

                subClassField.setAccessible(true);
                final List<IndexableField> indexableFieldsList = IndexUtility
                    .getIndexableFields(refEntity, subClassField, field.getName(), false);
                for (final IndexableField indexableField : indexableFieldsList) {
                  document.add(indexableField);
                }

              }
              refClass = refClass.getSuperclass();
            }
          }

        } else {

          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Add: object field instance of MultiField");
          }
          final List<IndexableField> indexableFieldsList =
              IndexUtility.getIndexableFields(entity, field, null, false);
          for (final IndexableField indexableField : indexableFieldsList) {
            document.add(indexableField);
          }

        }
      }
      currentClass = currentClass.getSuperclass();
    }

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Adding document: {}", document);
    }
    return document;
  }

  /**
   * Update an existing document efficiently by comparing fields and only updating changed ones.
   * This method retrieves the existing entity, compares it with the new one, and updates only the
   * fields that have actually changed, preserving schema consistency.
   *
   * @param clazz the clazz
   * @param id the id
   * @param entity the new entity
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public void update(final Class<? extends HasId> clazz, final String id, final HasId entity)
    throws Exception {

    if (id == null) {
      throw new IllegalArgumentException("id cannot be null");
    }
    if (entity == null) {
      throw new IllegalArgumentException("entity cannot be null");
    }

    final IndexWriter writer = getIndexWriter(clazz);

    // First, retrieve the existing document to get the current entity
    final Document existingDoc = getExistingDocument(writer, id);
    if (existingDoc == null) {
      throw new IllegalStateException(
          "Document with id " + id + " not found in index " + writer.getDirectory());
    }

    // Get the entity field which contains the JSON representation
    final StoredField entityField = (StoredField) existingDoc.getField("entity");
    if (entityField == null) {
      throw new IllegalStateException("Entity field not found in document with id " + id);
    }

    // Parse the existing entity from JSON
    final HasId existingEntity = parseEntityFromDocument(entityField, clazz);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Retrieved existing entity: {}", existingEntity);
      LOGGER.debug("New entity: {}", entity);
    }

    // Compare entities and update only changed fields
    final boolean hasChanges = updateChangedFields(existingEntity, entity);

    if (!hasChanges) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("No changes detected for entity with id: {}", id);
      }
      return;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Changes detected, updating document for entity with id: {}", id);
    }

    // Now delete the old document and add the updated one using the same
    // writer. This ensures schema consistency while being more efficient than
    // full reindexing
    updateDocumentInIndex(writer, id, existingEntity);
    clearReaderForClass(clazz);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Successfully updated document with id: {} for index: {}", id,
          writer.getDirectory());
    }
  }

  /**
   * Update multiple entities efficiently by comparing fields and only updating changed ones. This
   * method processes each entity individually using the optimized update approach.
   *
   * @param clazz the clazz
   * @param entities the map of id to entity
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public void updateBulk(final Class<? extends HasId> clazz, final Map<String, HasId> entities)
    throws Exception {

    if (entities == null || entities.isEmpty()) {
      return;
    }

    final IndexWriter writer = getIndexWriter(clazz);

    for (final Map.Entry<String, HasId> entry : entities.entrySet()) {
      final String id = entry.getKey();
      final HasId entity = entry.getValue();

      if (id == null) {
        LOGGER.warn("Skipping entity with null id in bulk update");
        continue;
      }
      if (entity == null) {
        LOGGER.warn("Skipping null entity with id: {} in bulk update", id);
        continue;
      }

      try {
        // First, retrieve the existing document to get the current entity
        final Document existingDoc = getExistingDocument(writer, id);
        if (existingDoc == null) {
          LOGGER.warn("Document with id {} not found in bulk update, skipping", id);
          continue;
        }

        // Get the entity field which contains the JSON representation
        final StoredField entityField = (StoredField) existingDoc.getField("entity");
        if (entityField == null) {
          LOGGER.warn("Entity field not found in document with id {} in bulk update, skipping", id);
          continue;
        }

        // Parse the existing entity from JSON
        final HasId existingEntity = parseEntityFromDocument(entityField, clazz);

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Processing bulk update for entity with id: {}", id);
        }

        // Compare entities and update only changed fields
        final boolean hasChanges = updateChangedFields(existingEntity, entity);

        if (!hasChanges) {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("No changes detected for entity with id: {} in bulk update", id);
          }
          continue;
        }

        // Delete the old document and add the updated one
        updateDocumentInIndex(writer, id, existingEntity);

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Updated entity with id: {} in bulk update", id);
        }

      } catch (final Exception e) {
        LOGGER.error("Error updating entity with id: {} in bulk update", id, e);
        // Continue with other entities instead of failing the entire batch
      }
    }

    // Commit all changes at once
    writer.commit();
    clearReaderForClass(clazz);
    LOGGER.debug("Successfully completed bulk update for {} entities in index: {}", entities.size(),
        writer.getDirectory());
  }

  /**
   * Compare two entities and update only the fields that have changed. This method uses reflection
   * to compare field values and updates the existing entity with new values from the updated
   * entity.
   *
   * @param existingEntity the existing entity to update
   * @param updatedEntity the entity containing the new values
   * @return true if any fields were changed, false otherwise
   */
  private boolean updateChangedFields(final HasId existingEntity, final HasId updatedEntity) {

    boolean hasChanges = false;
    final Class<?> entityClass = existingEntity.getClass();

    // Ensure both entities are of the same class
    if (!entityClass.equals(updatedEntity.getClass())) {
      throw new IllegalArgumentException("Entity classes must match: " + entityClass.getName()
          + " vs " + updatedEntity.getClass().getName());
    }

    // Iterate through all fields in the class hierarchy
    Class<?> currentClass = entityClass;
    while (currentClass != null) {
      for (final java.lang.reflect.Field field : currentClass.getDeclaredFields()) {
        field.setAccessible(true);

        try {
          final Object existingValue = field.get(existingEntity);
          final Object newValue = field.get(updatedEntity);

          // Skip null values in the updated entity (don't overwrite with null)
          if (newValue == null) {
            continue;
          }

          // Compare values and update if different
          if (!Objects.equals(existingValue, newValue)) {
            if (LOGGER.isDebugEnabled()) {
              LOGGER.debug("Field '{}' changed from '{}' to '{}'", field.getName(), existingValue,
                  newValue);
            }

            field.set(existingEntity, newValue);
            hasChanges = true;
          }

        } catch (final Exception e) {
          LOGGER.warn("Error comparing field '{}' in entity class '{}'", field.getName(),
              currentClass.getName(), e);
          // Continue with other fields instead of failing the entire update
        }
      }
      currentClass = currentClass.getSuperclass();
    }

    return hasChanges;
  }

  /**
   * Removes the entity from the index specified by the Class name.
   *
   * @param clazz the clazz
   * @param id the id
   * @throws Exception the exception
   */
  public void remove(final Class<? extends HasId> clazz, final String id) throws Exception {
    if (id == null) {
      throw new IllegalArgumentException("id cannot be null");
    }
    remove(clazz, List.of(id));
  }

  /**
   * Removes the entity.
   *
   * @param clazz the clazz
   * @param ids the ids
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public void remove(final Class<? extends HasId> clazz, final List<String> ids) throws Exception {

    if (ids == null || ids.isEmpty()) {
      throw new IllegalArgumentException("ids cannot be null");
    }

    final IndexWriter writer = getIndexWriter(clazz);

    for (final String id : ids) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Removing id: {} for index:{}", id, writer.getDirectory());
      }
      deleteDocumentById(writer, id);
    }
    clearReaderForClass(clazz);
    writer.commit();
  }

  /**
   * Find stored entities by search parameters.
   *
   * @param <T> the generic type
   * @param clazz the clazz
   * @param searchParameters the search parameters
   * @return the list
   * @throws Exception the exception
   */
  public <T extends HasId> ResultList<T> find(final Class<T> clazz,
    final SearchParameters searchParameters) throws Exception {

    // default search parameters if not provided
    final SearchParameters sp =
        (searchParameters != null) ? searchParameters : new SearchParameters();

    if (StringUtility.isEmpty(sp.getQuery())) {
      sp.setQuery("*:*");
    }

    if (sp.getOffset() == null) {
      sp.setOffset(0);
    }

    if (sp.getLimit() == null) {
      sp.setLimit(10);
    }

    if (sp.getActive() == null) {
      sp.setActive(true);
    }

    if (sp.getAscending() == null) {
      sp.setAscending(true);
    }
    try {
      final Query query = sp.getLuceneQuery() != null ? sp.getLuceneQuery()
          : LuceneQueryBuilder.parse(sp.getQuery(), clazz);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Executing query: {} for class: {}", query, clazz.getSimpleName());
      }
      return find(clazz, sp, query);
    } catch (ParseException e) {
      throw new Exception("Unable to parse query = " + sp.getQuery(), e);
    }
  }

  /**
   * Find stored entities by search parameters.
   *
   * @param <T> the generic type
   * @param clazz the clazz
   * @param searchParameters the search parameters
   * @param phraseQuery the phrase query
   * @return the list
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public <T extends HasId> ResultList<T> find(final Class<T> clazz,
    final SearchParameters searchParameters, final Query phraseQuery) throws Exception {

    IndexSearcher searcher = null;

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("indexRootDirectory is {}; index is {}", getIndexRootDirectory(),
          clazz.getCanonicalName());
    }

    final IndexReader reader = getIndexReader(clazz);

    final BooleanQuery queryBuilder =
        new BooleanQuery.Builder().add(phraseQuery, BooleanClause.Occur.SHOULD).build();
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("  query = {}", queryBuilder);
    }

    searcher = new IndexSearcher(reader);

    final Sort sort = (searchParameters.getSort() == null || searchParameters.getSort().isEmpty())
        ? IndexUtility.getDefaultSortOrder(clazz)
        : IndexUtility.getSortOrder(searchParameters, clazz);

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Search Parameters ({}): {}", clazz.getCanonicalName(), searchParameters);
    }
    final int start = searchParameters.getOffset();
    final int end = searchParameters.getLimit() + (searchParameters.getOffset());

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Search Parameters: start:{}, end:{}", start, end);
    }

    final TopDocs topDocs = (sort != null) ? searcher.search(queryBuilder, end, sort)
        : searcher.search(queryBuilder, end);
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Query topDocs: {}", topDocs.totalHits.value);
    }

    final ResultList<T> results = new ResultList<>();
    final ObjectMapper mapper = ThreadLocalMapper.get();
    for (int i = start; i < Math.min(topDocs.totalHits.value, end); i++) {

      final ScoreDoc scoreDoc = topDocs.scoreDocs[i];
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Score: {}", scoreDoc.score);
      }
      final Document doc = searcher.storedFields().document(scoreDoc.doc);
      final String jsonEntityString = doc.get("entity");
      final T obj = mapper.readValue(jsonEntityString, clazz);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("search result: {}", obj);
      }
      results.getItems().add(obj);
    }
    results.setTotal(topDocs.totalHits.value);
    return results;
  }

  /**
   * Add a field to an existing document without full reindexing. This method retrieves the existing
   * entity, updates it with the new field value, and then re-indexes it to ensure schema
   * consistency.
   *
   * @param clazz the clazz
   * @param id the id
   * @param entity the entity containing the field value
   * @param fieldName the field name to add
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public void addField(final Class<? extends HasId> clazz, final String id, final HasId entity,
    final String fieldName) throws Exception {

    if (id == null) {
      throw new IllegalArgumentException("id cannot be null");
    }
    if (fieldName == null) {
      throw new IllegalArgumentException("fieldName cannot be null");
    }
    if (entity == null) {
      throw new IllegalArgumentException("entity cannot be null");
    }

    final IndexWriter writer = getIndexWriter(clazz);

    // First, we need to retrieve the existing document to get the entity
    final Document existingDoc = getExistingDocument(writer, id);
    if (existingDoc == null) {
      throw new IllegalStateException(
          "Document with id " + id + " not found in index " + writer.getDirectory());
    }

    // Get the entity field which contains the JSON representation
    final StoredField entityField = (StoredField) existingDoc.getField("entity");
    if (entityField == null) {
      throw new IllegalStateException("Entity field not found in document with id " + id);
    }

    // Parse the existing entity from JSON
    final HasId existingEntity = parseEntityFromDocument(entityField, clazz);

    if (LOGGER.isTraceEnabled()) {
      LOGGER.debug("Retrieved existing entity: {}", existingEntity);
    }

    // Get the field value from the new entity using reflection
    final java.lang.reflect.Field field = getFieldByName(entity.getClass(), fieldName);
    if (field == null) {
      throw new IllegalArgumentException(
          "Field '" + fieldName + "' not found in class " + entity.getClass().getName());
    }

    field.setAccessible(true);
    final Object fieldValue = field.get(entity);
    if (fieldValue == null) {
      LOGGER.warn("Field value is null for field: {} in entity: {}", fieldName, entity);
      return;
    }

    // Set the new field value on the existing entity
    field.set(existingEntity, fieldValue);

    if (LOGGER.isTraceEnabled()) {
      LOGGER.debug("Updated existing entity with new field value: {} = {}", fieldName, fieldValue);
    }

    // Now delete the old document and add the updated one using the same
    // writer. This ensures schema consistency while being more efficient than
    // full reindexing
    updateDocumentInIndex(writer, id, existingEntity);
    clearReaderForClass(clazz);
    LOGGER.debug("Successfully updated field: {} in document with id: {} for index: {}", fieldName,
        id, writer.getDirectory());
  }

  /**
   * Retrieve an existing document from the index.
   *
   * @param writer the index writer
   * @param id the document id
   * @return the existing document or null if not found
   * @throws Exception on error
   */
  private Document getExistingDocument(final IndexWriter writer, final String id) throws Exception {
    try (final DirectoryReader reader = DirectoryReader.open(writer.getDirectory())) {
      final IndexSearcher searcher = new IndexSearcher(reader);
      final Query query = new TermQuery(new Term("id", id));
      final TopDocs topDocs = searcher.search(query, 1);

      if (topDocs.totalHits.value == 0) {
        return null;
      }

      final ScoreDoc scoreDoc = topDocs.scoreDocs[0];
      return searcher.storedFields().document(scoreDoc.doc);
    }
  }

  /**
   * Get field by name from class hierarchy.
   *
   * @param clazz the clazz
   * @param fieldName the field name
   * @return the field or null if not found
   */
  private java.lang.reflect.Field getFieldByName(final Class<?> clazz, final String fieldName) {
    try {
      return clazz.getDeclaredField(fieldName);
    } catch (final NoSuchFieldException e) {
      final Class<?> superClass = clazz.getSuperclass();
      if (superClass != null) {
        return getFieldByName(superClass, fieldName);
      }
    }
    return null;
  }

  /**
   * Get the index directory for a given class.
   *
   * @param clazz the clazz
   * @return the index directory
   */
  private File getIndexDirectory(final Class<? extends HasId> clazz) {
    return new File(getIndexRootDirectory(), clazz.getCanonicalName());
  }

  /**
   * Create an IndexWriter with StandardAnalyzer.
   *
   * @param clazz the clazz
   * @return the index writer
   * @throws Exception the exception
   */
  private Analyzer buildPerFieldAnalyzer(final Class<? extends HasId> clazz) throws Exception {

    final Map<String, Analyzer> fieldAnalyzers = LuceneQueryBuilder.getFieldAnalyzers(clazz);

    fieldAnalyzers.put("name.ngram", createNgramAnalyzer());

    fieldAnalyzers.put("name", new StandardAnalyzer());

    final Analyzer defaultAnalyzer = new StandardAnalyzer();

    return new PerFieldAnalyzerWrapper(defaultAnalyzer, fieldAnalyzers);
  }

  /**
   * Creates the ngram analyzer.
   *
   * @return the analyzer
   */
  private Analyzer createNgramAnalyzer() {
    return new Analyzer() {
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
   * Creates the index writer.
   *
   * @param clazz the clazz
   * @param directory the directory
   * @return the index writer
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  private IndexWriter createIndexWriter(final Class<? extends HasId> clazz,
    final FSDirectory directory) throws Exception {
    final Analyzer analyzer = buildPerFieldAnalyzer(clazz);
    return new IndexWriter(directory, new IndexWriterConfig(analyzer));
  }

  /**
   * Parse entity from document field.
   *
   * @param entityField the entity field
   * @param clazz the clazz
   * @return the parsed entity
   * @throws Exception on error
   */
  private HasId parseEntityFromDocument(final StoredField entityField,
    final Class<? extends HasId> clazz) throws Exception {

    final String entityJson = entityField.stringValue();
    final ObjectMapper mapper = ThreadLocalMapper.get();
    return mapper.readValue(entityJson, mapper.getTypeFactory().constructType(clazz));
  }

  /**
   * Update document in index by deleting old and adding new.
   *
   * @param writer the index writer
   * @param id the document id
   * @param entity the entity to index
   * @throws Exception on error
   */
  private void updateDocumentInIndex(final IndexWriter writer, final String id, final HasId entity)
    throws Exception {

    final Term term = new Term("id", id);
    writer.deleteDocuments(term);
    final Document updatedDoc = getDocument(entity);
    writer.addDocument(updatedDoc);
    writer.commit();
  }

  /**
   * Delete document by id.
   *
   * @param writer the index writer
   * @param id the document id
   * @throws IOException on error
   * @throws IllegalAccessException on error
   */
  private void deleteDocumentById(final IndexWriter writer, final String id)
    throws IOException, IllegalAccessException {

    final Query query = new TermQuery(new Term("id", id));
    final BooleanQuery booleanQuery =
        new BooleanQuery.Builder().add(query, BooleanClause.Occur.MUST).build();
    writer.deleteDocuments(booleanQuery);
  }

  /**
   * Gets the index writer.
   *
   * @param clazz the clazz
   * @return the index writer
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public IndexWriter getIndexWriter(final Class<? extends HasId> clazz) throws Exception {
    String className = clazz.getCanonicalName();
    if (WRITER_MAP.containsKey(className)) {
      final IndexWriter writer = WRITER_MAP.get(className);
      return writer;
    } else {
      final File indexDir = getIndexDirectory(clazz);
      if (!indexDir.exists()) {
        indexDir.mkdirs();
      }
      final FSDirectory directory = FSDirectory.open(indexDir.toPath());
      final IndexWriter writer = createIndexWriter(clazz, directory);
      WRITER_MAP.put(className, writer);
      return writer;
    }
  }

  /**
   * Gets the index reader.
   *
   * @param clazz the clazz
   * @return the index reader
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public IndexReader getIndexReader(final Class<? extends HasId> clazz) throws Exception {
    String canonicalClassName = clazz.getCanonicalName();
    IndexReader reader = READER_MAP.get(canonicalClassName);
    if (reader == null) {
      synchronized (READER_MAP) {
        if (!READER_MAP.containsKey(canonicalClassName)) {
          synchronized (READER_MAP) {
            final File indexDir = getIndexDirectory(clazz);
            final FSDirectory fsDirectory = FSDirectory.open(indexDir.toPath());
            reader = DirectoryReader.open(fsDirectory);
            READER_MAP.put(canonicalClassName, reader);
            return reader;
          }
        } else {
          return READER_MAP.get(canonicalClassName);
        }
      }
    }
    return reader;
  }

  /**
   * Clear readers.
   */
  public static final void clearReaders() {
    READER_MAP.clear();
  }

  /**
   * Clear reader for class.
   *
   * @param clazz the clazz
   */
  @SuppressWarnings("resource")
  public static final void clearReaderForClass(final Class<? extends HasId> clazz) {
    READER_MAP.remove(clazz.getCanonicalName());
  }

  /**
   * Clear writer for class.
   *
   * @param clazz the clazz
   */
  @SuppressWarnings("resource")
  public static final void clearWriterForClass(final Class<? extends HasId> clazz) {
    IndexWriter writer = WRITER_MAP.get(clazz.getCanonicalName());
    if (writer == null) {
      return;
    }
    try {
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    WRITER_MAP.remove(clazz.getCanonicalName());
  }

  /**
   * Gets the index root directory.
   *
   * @return the index root directory
   */
  private String getIndexRootDirectory() {
    return applicationProperties.getProperty("lucene.index.directory");
  }

}
