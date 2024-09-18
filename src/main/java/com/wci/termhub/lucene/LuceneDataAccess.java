package com.wci.termhub.lucene;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.util.FileUtility;
import com.wci.termhub.util.IndexUtility;
import com.wci.termhub.util.ModelUtility;

/**
 * The Class LuceneDao1.
 *
 * @param <T> the generic type
 */
@Component
public class LuceneDataAccess<T> {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(LuceneDataAccess.class);

	/** The Constant INDEX_DIRECTORY. */
	private String indexRootDirectory;

	/**
	 * Instantiates a new lucene data access.
	 */
	public LuceneDataAccess() {
		// n/a
		indexRootDirectory = "C:\\tmp\\index";
	}

	/**
	 * Creates the index based on the class name.
	 *
	 * @param clazz the clazz
	 * @throws Exception the exception
	 */
	public void createIndex(final Class<? extends T> clazz) throws Exception {

		final String indexDirectory = clazz.getCanonicalName();
		final Path indexPath = Paths.get(indexRootDirectory, indexDirectory);
		logger.info("Index path: {}, directory: {}", indexRootDirectory, indexDirectory);
		FSDirectory.open(indexPath);
	}

	/**
	 * Delete index.
	 *
	 * @param clazz the clazz
	 * @throws Exception the exception
	 */
	public void deleteIndex(final Class<? extends T> clazz) throws Exception {

		final String indexDirectory = clazz.getCanonicalName();
		final Path indexPath = Paths.get(indexRootDirectory, indexDirectory);
		logger.info("Deleting index {} from {}", indexDirectory, indexRootDirectory);
		FileUtility.deleteDirectoryAndAllFiles(indexPath);
	}

	/**
	 * Adds the batch.
	 *
	 * @param entities the entities
	 * @throws IOException            Signals that an I/O exception has occurred.
	 * @throws IllegalAccessException the illegal access exception
	 */
	public void add(final List<T> entities) throws IOException, IllegalAccessException {

		final IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

		final String indexDirectory = entities.get(0).getClass().getCanonicalName();
		try (final FSDirectory fsDirectory = FSDirectory.open(Paths.get(indexRootDirectory, indexDirectory));
				final IndexWriter writer = new IndexWriter(fsDirectory, config);) {

			for (final T entity : entities) {
				final Document document = getDocument(entity);
				writer.addDocument(document);
			}

			writer.commit();
			writer.close();
		}
	}

	/**
	 * Adds the entity to the index specified by the entity class name.
	 *
	 * @param entity the entity
	 * @throws IOException            Signals that an I/O exception has occurred.
	 * @throws IllegalAccessException the illegal access exception
	 */
	public void add(final T entity) throws IOException, IllegalAccessException {

		add(List.of(entity));
	}

	/**
	 * Gets the document.
	 *
	 * @param entity the entity
	 * @return the document
	 * @throws IOException            Signals that an I/O exception has occurred.
	 * @throws IllegalAccessException the illegal access exception
	 */
	private Document getDocument(final T entity) throws IOException, IllegalAccessException {

		final Document document = new Document();
		document.add(new StoredField("entity", ModelUtility.toJson(entity)));
		Class<?> currentClass = entity.getClass();

		while (currentClass != null) {
			logger.debug("Add: Current class: {}", currentClass.getName());

			for (final java.lang.reflect.Field field : currentClass.getDeclaredFields()) {

				field.setAccessible(true);
				final Object fieldValue = field.get(entity);
				if (fieldValue == null) {
					continue;
				}

				final Field annotation = field.getAnnotation(Field.class);
				logger.debug("Field: {}, value: {}, annotation: {}", field.getName(), fieldValue, annotation);

				if (annotation != null) {

					final FieldType fieldType = annotation.type();

					// if not collection of objects, add the field to the document
					if (fieldType != FieldType.Object) {

						logger.debug("Add: field instance of NOT Object OR Collection");
						final List<IndexableField> indexableFieldsList = IndexUtility.getIndexableFields(entity, field,
								null);
						for (final IndexableField indexableField : indexableFieldsList) {
							document.add(indexableField);
						}

					} else if (fieldType == FieldType.Object && fieldValue instanceof Collection) {

						logger.debug("Add: object field instance of Collection");
						final Collection<?> collection = (Collection<?>) fieldValue;
						final List<IndexableField> indexableFieldsList = IndexUtility.getIndexableFields(collection,
								field);
						for (final IndexableField indexableField : indexableFieldsList) {
							document.add(indexableField);
						}

					} else if (fieldType == FieldType.Object && fieldValue instanceof BaseModel) {

						logger.debug("Add: object field instance of BaseModel");
						final Object refEntity = fieldValue;
						for (final java.lang.reflect.Field subClassField : refEntity.getClass().getDeclaredFields()) {

							subClassField.setAccessible(true);
							final List<IndexableField> indexableFieldsList = IndexUtility.getIndexableFields(refEntity,
									subClassField, field.getName());
							for (final IndexableField indexableField : indexableFieldsList) {
								document.add(indexableField);
							}

						}
					}

				} else {

					logger.debug("Add: object field instance of MultiField");
					final List<IndexableField> indexableFieldsList = IndexUtility.getIndexableFields(entity, field,
							null);
					for (final IndexableField indexableField : indexableFieldsList) {
						document.add(indexableField);
					}

				}
			}
			currentClass = currentClass.getSuperclass();
		}

		logger.debug("Adding document: {}", document);
		return document;
	}

	/**
	 * Removes the entity from the index specified by the Class name.
	 * 
	 * @param clazz the clazz
	 * @param id    the id
	 * @throws IOException            Signals that an I/O exception has occurred.
	 * @throws IllegalAccessException the illegal access exception
	 */
	public void remove(final Class<? extends T> clazz, final String id) throws IOException, IllegalAccessException {

		if (id == null) {
			throw new IllegalArgumentException("id cannot be null");
		}

		final String indexDirectory = clazz.getCanonicalName();
		final IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

		logger.info("Removing id: {} for index:{}", id, indexDirectory);

		try (final FSDirectory fsDirectory = FSDirectory.open(Paths.get(indexRootDirectory, indexDirectory));
				final IndexWriter writer = new IndexWriter(fsDirectory, config)) {

			final Query query = new TermQuery(new Term("id", id));
			final BooleanQuery booleanQuery = new BooleanQuery.Builder().add(query, BooleanClause.Occur.MUST).build();

			writer.deleteDocuments(booleanQuery);
			writer.commit();
			writer.close();

		} catch (final Exception e) {
			logger.error("Error: {}", e.getMessage(), e);
			throw e;
		}

	}

	/**
	 * Find stored entities by search parameters.
	 *
	 * @param clazz            the clazz
	 * @param searchParameters the search parameters
	 * @return the list
	 * @throws Exception the exception
	 */
	public Iterable<T> find(final Class<? extends T> clazz, SearchParameters searchParameters) throws Exception {

		// default search parameters if not provided
		if (searchParameters == null) {
			searchParameters = new SearchParameters();
		}

		if (searchParameters.getQuery() == null) {
			searchParameters.setQuery("*:*");
		}

		if (searchParameters.getOffset() == null) {
			searchParameters.setOffset(0);
		}

		if (searchParameters.getLimit() == null) {
			searchParameters.setLimit(10);
		}

		if (searchParameters.getActive() == null) {
			searchParameters.setActive(true);
		}

		if (searchParameters.getAscending() == null) {
			searchParameters.setAscending(true);
		}

		return find(clazz, searchParameters, LuceneQueryBuilder.parse(clazz, searchParameters.getQuery()));
	}

	/**
	 * Find stored entities by search parameters.
	 *
	 * @param clazz            the clazz
	 * @param searchParameters the search parameters
	 * @param phraseQuery      the phrase query
	 * @return the list
	 * @throws Exception the exception
	 */
	private Iterable<T> find(final Class<? extends T> clazz, final SearchParameters searchParameters,
			final Query phraseQuery) throws Exception {

		try (final FSDirectory fsDirectory = FSDirectory.open(Paths.get(indexRootDirectory, clazz.getCanonicalName()));
				final IndexReader reader = DirectoryReader.open(fsDirectory)) {

			final BooleanQuery queryBuilder = new BooleanQuery.Builder().add(phraseQuery, BooleanClause.Occur.MUST)
					.build();
			logger.info("Query: {}", queryBuilder);

			final IndexSearcher searcher = new IndexSearcher(reader);

			final Sort sort = (searchParameters.getSort() == null || searchParameters.getSort().isEmpty())
					? IndexUtility.getDefaultSortOrder(clazz)
					: IndexUtility.getSortOrder(searchParameters, clazz);
			logger.info("Sort: {}", sort);

			logger.info("Search Parameters: {}", searchParameters);
			final int start = searchParameters.getOffset();
			final int end = searchParameters.getLimit() + (searchParameters.getOffset());

			logger.info("Search Parameters: start:{}, end:{}", start, end);

			final TopDocs topDocs = (sort != null) ? searcher.search(queryBuilder, end, sort)
					: searcher.search(queryBuilder, end);
			logger.info("Query topDocs: {}", topDocs.totalHits.value);

			final List<T> results = new ArrayList<>();
			final ObjectMapper mapper = new ObjectMapper();
			for (int i = start; i < Math.min(topDocs.totalHits.value, end); i++) {

				final ScoreDoc scoreDoc = topDocs.scoreDocs[i];
				logger.info("Score: {}", scoreDoc.score);
				@SuppressWarnings("deprecation")
				final Document doc = searcher.doc(scoreDoc.doc);
				final String jsonEntityString = doc.get("entity");
				final T obj = mapper.readValue(jsonEntityString, clazz);
				logger.info("search result: {}", obj);
				results.add(obj);
			}
			return results;

		} catch (final Exception e) {
			logger.error("Error: {}", e);
			throw e;
		}
	}

}