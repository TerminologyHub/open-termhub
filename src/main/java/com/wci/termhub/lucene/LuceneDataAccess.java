package com.wci.termhub.lucene;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
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
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
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
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(LuceneDataAccess.class);

	/** The Constant INDEX_DIRECTORY. */
	private String indexRootDirectory;

	/**
	 * Instantiates a new lucene data access.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public LuceneDataAccess() {
		// n/a
		indexRootDirectory = "C:\\tmp\\index"; // PropertyUtility.getProperty("lucuene.index.directory");
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
	 * Adds the entity to the index specified by the entity class name.
	 *
	 * @param entity the entity
	 * @throws IOException            Signals that an I/O exception has occurred.
	 * @throws IllegalAccessException the illegal access exception
	 */
	public void add(final T entity) throws IOException, IllegalAccessException {

		final IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

		final String indexDirectory = entity.getClass().getCanonicalName();
		try (final FSDirectory fsDirectory = FSDirectory.open(Paths.get(indexRootDirectory, indexDirectory));
				final IndexWriter writer = new IndexWriter(fsDirectory, config);) {

			final Document document = new Document();
			document.add(new StoredField("entity", ModelUtility.toJson(entity)));
			Class<?> currentClass = entity.getClass();

			while (currentClass != null) {
				logger.info("Add: Current class: {}", currentClass.getName());

				for (final java.lang.reflect.Field field : currentClass.getDeclaredFields()) {

					field.setAccessible(true);
					final Field annotation = field.getAnnotation(Field.class);
					final Object fieldValue = field.get(entity);

					logger.info("Add: Field: {}, value: {}, annotation: {}", field.getName(), fieldValue, annotation);

					if (annotation != null && fieldValue != null) {

						final FieldType fieldType = annotation.type();
						logger.debug("AddField: {}, value: {}, type: {}", field.getName(), fieldValue, fieldType);

						// if not collection of objects, add the field to the document
						if (fieldType != FieldType.Object) {

							final List<IndexableField> indexableFieldsList = IndexUtility.getIndexableFields(entity,
									field, null);
							for (final IndexableField indexableField : indexableFieldsList) {
								document.add(indexableField);
							}

						} else if (fieldType == FieldType.Object && fieldValue instanceof Collection) {

							logger.info("Add: object field instance of Collection");
							final Collection<?> collection = (Collection<?>) fieldValue;
							for (final Object item : collection) {
								if (item instanceof String) {
									document.add(new StringField(field.getName(), (String) item,
											org.apache.lucene.document.Field.Store.YES));
								} else if (item instanceof Integer) {
									document.add(new NumericDocValuesField(field.getName(), (Integer) item));
									document.add(new StoredField(field.getName(), (Integer) item));
								} else {
									/*
									 * Class<?> currentRefClass = item.getClass(); while (currentRefClass != null) {
									 * logger.info("Add: Current ref class: {}", currentRefClass.getName()); for
									 * (final java.lang.reflect.Field currentRefClassField : currentRefClass
									 * .getDeclaredFields()) {
									 * 
									 * final List<IndexableField> indexableFieldsList =
									 * IndexUtility.getIndexableFields(item, currentRefClassField,
									 * item.getClass().getSimpleName().toLowerCase());
									 * 
									 * for (final IndexableField indexableField : indexableFieldsList) {
									 * document.add(indexableField); } } currentRefClass =
									 * currentRefClass.getSuperclass(); }
									 */
								}
							}
						} else {

							final MultiField multiFieldAnnotation = field.getAnnotation(MultiField.class);

							if (multiFieldAnnotation != null && fieldValue != null) {

								logger.debug("MF Field: {}, value: {}, annotation: {}",
										multiFieldAnnotation.annotationType(), fieldValue, multiFieldAnnotation);

								for (final InnerField innerFieldAnnotation : multiFieldAnnotation.otherFields()) {

									logger.debug("MF innerFieldAnnotation {}", innerFieldAnnotation.toString());
									final FieldType fieldTypeMF = innerFieldAnnotation.type();

									logger.debug("MF Adding multi-field: fieldName: {}, type: {}", field.getName(),
											fieldTypeMF);

									switch (fieldTypeMF) {
									case Text:
										logger.debug("MF Adding text field: {}, value:{}", field.getName(),
												fieldValue.toString());
										document.add(new TextField(field.getName(), fieldValue.toString(),
												org.apache.lucene.document.Field.Store.YES));
										break;
									case Keyword:
										logger.debug("MF Adding keyword field: {}, value:{}", field.getName(),
												fieldValue.toString());
										document.add(new TextField(field.getName(), fieldValue.toString(),
												org.apache.lucene.document.Field.Store.YES));
										document.add(new SortedDocValuesField(field.getName(),
												new BytesRef(fieldValue.toString())));
										break;
									default:
										logger.info("MultiField field not found Adding default field: {}", fieldTypeMF);
									}
								}
							}
						}
					}
				}
				currentClass = currentClass.getSuperclass();
			}

			logger.info("Adding document: {}", document);

			writer.addDocument(document);
			writer.commit();
			writer.close();

			logger.info("");

		} catch (

		final Exception e) {
			logger.error("Error: {}", e.getMessage(), e);
			throw e;
		}
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

		if (searchParameters.getSort() == null || searchParameters.getSort().isEmpty()) {
			searchParameters.setSort(List.of("code"));
		}

		if (searchParameters.getAscending() == null) {
			searchParameters.setAscending(true);
		}

		return find(clazz, searchParameters, LuceneQueryBuilder.parse(clazz, searchParameters.getQuery()));
	}

	/**
	 * Find stored entities by search parameters.
	 *
	 * @param searchParameters the search parameters
	 * @param phraseQuery      the phrase query
	 * @param clazz            the clazz
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
			final Sort sort = IndexUtility.getSortOrder(searchParameters, clazz);

			logger.info("Sort: {}", sort);
			logger.info("Search Parameters: {}", searchParameters);

			final int start = searchParameters.getOffset();
			final int end = searchParameters.getLimit() * (searchParameters.getOffset() + 1);

			final TopDocs topDocs = searcher.search(queryBuilder, end, sort);
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