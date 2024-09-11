package com.wci.termhub.repository;

import java.util.Optional;

import com.wci.termhub.model.BaseModel;
import com.wci.termhub.model.SearchParameters;

/**
 * The Interface EntityRepository.
 *
 * @param <T>  the generic type
 * @param <ID> the generic type
 */
public interface EntityRepository<T extends BaseModel, ID> {

	/**
	 * Delete index.
	 *
	 * @param entity the entity
	 */
	public void deleteIndex(final T entity) throws Exception;

	/**
	 * Creates the index.
	 *
	 * @param entity the entity
	 */
	public void createIndex(final T entity) throws Exception;

	/**
	 * Adds the.
	 *
	 * @param entity the entity
	 */
	public void add(final T entity) throws Exception;

	/**
	 * Removes the.
	 *
	 * @param entity the entity
	 */
	public void remove(final T entity) throws Exception;

	/**
	 * Find.
	 *
	 * @param searchParameters the search parameters
	 * @param clazz            the clazz
	 * @return the optional
	 */
	public Optional<T> find(final SearchParameters searchParameters, final Class<T> clazz) throws Exception;

}