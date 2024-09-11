package com.wci.termhub.service.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.stereotype.Service;

import com.wci.termhub.lucene.LuceneDataAccess;
import com.wci.termhub.model.BaseModel;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * The Class EntityServiceImpl.
 *
 * @param <T>  the generic type
 * @param <ID> the generic type
 */
@Service
public class EntityServiceImpl<T extends BaseModel, ID> implements EntityRepositoryService<T, ID> {

	/** The logger. */
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(EntityServiceImpl.class);

	/**
	 * Creates the index.
	 *
	 * @param clazz the clazz
	 * @throws Exception the exception
	 */
	@Override
	public void createIndex(final Class<? extends T> clazz) throws Exception {

		checkIfEntityHasDocumentAnnotation(clazz);
		final LuceneDataAccess<T> luceneData = new LuceneDataAccess<>();
		luceneData.createIndex(clazz);
	}

	/**
	 * Delete index.
	 *
	 * @param clazz the clazz
	 * @throws Exception the exception
	 */
	@Override
	public void deleteIndex(final Class<? extends T> clazz) throws Exception {

		checkIfEntityHasDocumentAnnotation(clazz);
		final LuceneDataAccess<T> luceneData = new LuceneDataAccess<>();
		luceneData.deleteIndex(clazz);
	}

	/**
	 * Adds the.
	 *
	 * @param entity the entity
	 * @param clazz  the clazz
	 * @throws Exception the exception
	 */
	@Override
	public void add(final T entity, final Class<? extends T> clazz) throws Exception {

		checkIfEntityHasDocumentAnnotation(clazz);
		final LuceneDataAccess<T> luceneData = new LuceneDataAccess<>();
		luceneData.add(entity);
	}

	/**
	 * Find all objects from index of this object type.
	 *
	 * @param clazz            the clazz
	 * @param searchParameters the search parameters
	 * @return the iterable
	 * @throws Exception the exception
	 */
	@Override
	public Iterable<T> findAll(final Class<? extends T> clazz, final SearchParameters searchParameters)
			throws Exception {

		checkIfEntityHasDocumentAnnotation(clazz);
		// TODO: implement this
		// final LuceneDao1<T> luceneData = new LuceneDao1<>();
		// luceneData.findAll(clazz, entity);
		return null;
	}

	/**
	 * Find by id.
	 *
	 * @param id    the id
	 * @param clazz the clazz
	 * @return the optional
	 * @throws Exception the exception
	 */
	@Override
	public Optional<T> findById(String id, final Class<? extends T> clazz) throws Exception {
		checkIfEntityHasDocumentAnnotation(clazz);
		// TODO: implement this
		return Optional.empty();
	}

	/**
	 * Find.
	 *
	 * @param clazz            the clazz
	 * @param searchParameters the search parameters
	 * @return the optional
	 * @throws Exception the exception
	 */
	@Override
	public Iterable<T> find(final Class<? extends T> clazz, final SearchParameters searchParameters) throws Exception {
		checkIfEntityHasDocumentAnnotation(clazz);
		final LuceneDataAccess<T> luceneData = new LuceneDataAccess<>();
		return luceneData.find(clazz, searchParameters);
	}

	/**
	 * Delete object from index.
	 *
	 * @param clazz the clazz
	 * @param id    the id
	 * @throws Exception the exception
	 */
	@Override
	public void remove(final Class<? extends T> clazz, final String id) throws Exception {
		checkIfEntityHasDocumentAnnotation(clazz);
		final LuceneDataAccess<T> luceneData = new LuceneDataAccess<>();
		luceneData.remove(clazz, id);
	}

	/**
	 * Check if entity has document annotation.
	 *
	 * @param clazz the clazz
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	private void checkIfEntityHasDocumentAnnotation(final Class<? extends T> clazz) throws IllegalArgumentException {
		// check if the clazz has annotation @Document in the class
		if (!clazz.isAnnotationPresent(Document.class)) {
			throw new IllegalArgumentException("Entity " + clazz.getName() + " should have @Document annotation.");
		}
	}

}
