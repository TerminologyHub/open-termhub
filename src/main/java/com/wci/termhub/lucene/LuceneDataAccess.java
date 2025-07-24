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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
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
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.model.BaseModel;
import com.wci.termhub.model.HasId;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.util.IndexUtility;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.PropertyUtility;
import com.wci.termhub.util.ThreadLocalMapper;

/**
 * Lucene data access manager.
 */
@Component
public class LuceneDataAccess {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(LuceneDataAccess.class);

  /** The Constant INDEX_DIRECTORY. */
  private static String indexRootDirectory;

  /**
   * Instantiates a new lucene data access.
   */
  public LuceneDataAccess() {
    // n/a
    indexRootDirectory = PropertyUtility.getProperties().getProperty("lucene.index.directory");
  }

  /**
   * Creates the index.
   *
   * @param clazz the clazz
   * @throws Exception the exception
   */
  public void createIndex(final Class<? extends HasId> clazz) throws Exception {

    // Create only if the index does not exist
    final File indexDir = new File(indexRootDirectory, clazz.getCanonicalName());
    if (indexDir.exists()) {
      LOGGER.debug("Index already exists: {}", indexDir.getAbsolutePath());
      return;
    }

    LOGGER.info("Create Index: {}/{}", indexRootDirectory, clazz.getCanonicalName());

    // Create a new IndexWriter with default config to initialize the index
    try (final FSDirectory directory = FSDirectory.open(indexDir.toPath());
        final StandardAnalyzer analyzer = new StandardAnalyzer();
        final IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
      // Commit to create the initial index structure
      writer.commit();
    }
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
    final File indexDir = new File(indexRootDirectory, indexDirectory);
    LOGGER.info("Deleting index {} from {}", indexDirectory, indexDir.getAbsolutePath());
    if (indexDir.exists()) {
      FileUtils.deleteDirectory(indexDir);
    }
  }

  /**
   * Adds the batch.
   *
   * @param entities the entities
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws IllegalAccessException the illegal access exception
   */
  public void add(final List<? extends HasId> entities) throws IOException, IllegalAccessException {

    try (final StandardAnalyzer analyzer = new StandardAnalyzer()) {
      final IndexWriterConfig config = new IndexWriterConfig(analyzer);

      final String indexDirectory = entities.get(0).getClass().getCanonicalName();
      final File indexDir = new File(indexRootDirectory, indexDirectory);
      if (!indexDir.exists()) {
        indexDir.mkdirs();
      }

      try (final FSDirectory fsDirectory = FSDirectory.open(indexDir.toPath());
          final IndexWriter writer = new IndexWriter(fsDirectory, config);) {

        for (final HasId entity : entities) {
          final Document document = getDocument(entity);
          writer.addDocument(document);
        }

        writer.commit();
      }
    }
  }

  /**
   * Adds the entity to the index specified by the entity class name.
   *
   * @param entity the entity
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws IllegalAccessException the illegal access exception
   */
  public void add(final HasId entity) throws IOException, IllegalAccessException {

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
      LOGGER.debug("Add: Current class: {}", currentClass.getName());

      for (final java.lang.reflect.Field field : currentClass.getDeclaredFields()) {

        field.setAccessible(true);
        final Object fieldValue = field.get(entity);
        if (fieldValue == null) {
          continue;
        }

        final Field annotation = field.getAnnotation(Field.class);
        LOGGER.debug("Field: {}, value: {}, annotation: {}", field.getName(), fieldValue,
            annotation);

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
              LOGGER.debug("Add: field instance of NOT Object OR Collection");
              final List<IndexableField> indexableFieldsList = IndexUtility
                  .getIndexableFields(entity, field, null, fieldValue instanceof Collection);
              for (final IndexableField indexableField : indexableFieldsList) {
                document.add(indexableField);
              }
            }

          } else if (fieldType == FieldType.Object && fieldValue instanceof Collection) {

            LOGGER.debug("Add: object field instance of Collection");
            final Collection<?> collection = (Collection<?>) fieldValue;
            final List<IndexableField> indexableFieldsList =
                IndexUtility.getIndexableFields(collection, field);
            for (final IndexableField indexableField : indexableFieldsList) {
              document.add(indexableField);
            }

          } else if (fieldType == FieldType.Object && fieldValue instanceof BaseModel) {

            LOGGER.debug("Add: object field instance of BaseModel");
            final Object refEntity = fieldValue;
            for (final java.lang.reflect.Field subClassField : refEntity.getClass()
                .getDeclaredFields()) {

              subClassField.setAccessible(true);
              final List<IndexableField> indexableFieldsList =
                  IndexUtility.getIndexableFields(refEntity, subClassField, field.getName(), false);
              for (final IndexableField indexableField : indexableFieldsList) {
                document.add(indexableField);
              }

            }
          }

        } else {

          LOGGER.debug("Add: object field instance of MultiField");
          final List<IndexableField> indexableFieldsList =
              IndexUtility.getIndexableFields(entity, field, null, false);
          for (final IndexableField indexableField : indexableFieldsList) {
            document.add(indexableField);
          }

        }
      }
      currentClass = currentClass.getSuperclass();
    }

    LOGGER.debug("Adding document: {}", document);
    return document;
  }

  /**
   * Removes the entity from the index specified by the Class name.
   *
   * @param clazz the clazz
   * @param id the id
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws IllegalAccessException the illegal access exception
   */
  public void remove(final Class<? extends HasId> clazz, final String id)
    throws IOException, IllegalAccessException {

    if (id == null) {
      throw new IllegalArgumentException("id cannot be null");
    }

    final String indexDirectory = clazz.getCanonicalName();
    try (final StandardAnalyzer analyzer = new StandardAnalyzer()) {
      final IndexWriterConfig config = new IndexWriterConfig(analyzer);

      LOGGER.debug("Removing id: {} for index:{}", id, indexDirectory);

      final File indexDir = new File(indexRootDirectory, indexDirectory);
      if (!indexDir.exists()) {
        indexDir.mkdirs();
      }

      try (final FSDirectory fsDirectory = FSDirectory.open(indexDir.toPath());
          final IndexWriter writer = new IndexWriter(fsDirectory, config)) {

        final Query query = new TermQuery(new Term("id", id));
        final BooleanQuery booleanQuery =
            new BooleanQuery.Builder().add(query, BooleanClause.Occur.MUST).build();

        writer.deleteDocuments(booleanQuery);
        writer.commit();

      } catch (final Exception e) {
        LOGGER.error("Error: {}", e.getMessage(), e);
        throw e;
      }
    }

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

    if (sp.getQuery() == null) {
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

    return find(clazz, sp, sp.getLuceneQuery() != null ? sp.getLuceneQuery()
        : LuceneQueryBuilder.parse(sp.getQuery(), clazz));
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

    LOGGER.debug("indexRootDirectory is {}; index is {}", indexRootDirectory,
        clazz.getCanonicalName());

    final File indexDir = new File(indexRootDirectory, clazz.getCanonicalName());
    try (final FSDirectory fsDirectory = FSDirectory.open(indexDir.toPath());
        final IndexReader reader = DirectoryReader.open(fsDirectory)) {

      final BooleanQuery queryBuilder =
          new BooleanQuery.Builder().add(phraseQuery, BooleanClause.Occur.SHOULD).build();

      LOGGER.debug("Query: {}", queryBuilder);

      searcher = new IndexSearcher(reader);

      final Sort sort = (searchParameters.getSort() == null || searchParameters.getSort().isEmpty())
          ? IndexUtility.getDefaultSortOrder(clazz)
          : IndexUtility.getSortOrder(searchParameters, clazz);

      LOGGER.debug("Search Parameters ({}): {}", clazz.getCanonicalName(), searchParameters);
      final int start = searchParameters.getOffset();
      final int end = searchParameters.getLimit() + (searchParameters.getOffset());

      LOGGER.debug("Search Parameters: start:{}, end:{}", start, end);

      final TopDocs topDocs = (sort != null) ? searcher.search(queryBuilder, end, sort)
          : searcher.search(queryBuilder, end);
      LOGGER.debug("Query topDocs: {}", topDocs.totalHits.value);

      final ResultList<T> results = new ResultList<>();
      final ObjectMapper mapper = ThreadLocalMapper.get();
      for (int i = start; i < Math.min(topDocs.totalHits.value, end); i++) {

        final ScoreDoc scoreDoc = topDocs.scoreDocs[i];
        LOGGER.debug("Score: {}", scoreDoc.score);
        // fix deprecated:
        // final Document doc = searcher.doc(scoreDoc.doc);
        final Document doc = searcher.storedFields().document(scoreDoc.doc);
        final String jsonEntityString = doc.get("entity");
        final T obj = mapper.readValue(jsonEntityString, clazz);
        LOGGER.debug("search result: {}", obj);
        results.getItems().add(obj);
      }
      results.setTotal(results.getItems().size());
      return results;

    } catch (final Exception e) {
      LOGGER.error("Error: {}", e.getMessage(), e);
      throw e;
    } finally {
      if (searcher != null && searcher.getIndexReader() != null) {
        searcher.getIndexReader().close();
      }
    }
  }
}
