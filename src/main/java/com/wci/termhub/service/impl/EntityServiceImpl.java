package com.wci.termhub.service.impl;

import java.util.List;
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
	 * Adds the entity.
	 *
	 * @param entity the entity
	 * @param clazz  the clazz
	 * @throws Exception the exception
	 */
	@Override
	public void add(final Class<? extends T> clazz, final T entity) throws Exception {

		checkIfEntityHasDocumentAnnotation(clazz);
		final LuceneDataAccess<T> luceneData = new LuceneDataAccess<>();
		luceneData.add(entity);
	}

	/**
	 * Adds the entity.
	 *
	 * @param entity the entity
	 * @param clazz  the clazz
	 * @throws Exception the exception
	 */
	@Override
	public void add(final Class<? extends T> clazz, final List<T> entity) throws Exception {

		checkIfEntityHasDocumentAnnotation(clazz);
		final LuceneDataAccess<T> luceneData = new LuceneDataAccess<>();
		luceneData.add(entity);
	}

	/**
	 * Update the entity
	 *
	 * @param entity the entity
	 * @param clazz  the clazz
	 * @throws Exception the exception
	 */
	@Override
	public void update(final Class<? extends T> clazz, final String id, final T entity) throws Exception {

		checkIfEntityHasDocumentAnnotation(clazz);
		final LuceneDataAccess<T> luceneData = new LuceneDataAccess<>();
		luceneData.remove(clazz, id);
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

		final LuceneDataAccess<T> luceneData = new LuceneDataAccess<>();
		// searchParameters.setQuery("*:*");
		return luceneData.find(clazz, new SearchParameters());
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
	public Optional<T> findById(final Class<? extends T> clazz, final String id) throws Exception {

		checkIfEntityHasDocumentAnnotation(clazz);
		final LuceneDataAccess<T> luceneData = new LuceneDataAccess<>();
		final SearchParameters searchParameters = new SearchParameters();
		searchParameters.setQuery("id:" + id);
		final Iterable<T> result = luceneData.find(clazz, searchParameters);

		if (result.iterator().hasNext()) {
			return Optional.of(result.iterator().next());
		}

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
