package com.wci.termhub.service;

import java.util.Optional;

import com.wci.termhub.model.BaseModel;
import com.wci.termhub.model.SearchParameters;

/**
 * The Interface TService.
 *
 * @param <T>  the generic type
 * @param <ID> the generic type
 */
public interface EntityRepositoryService<T extends BaseModel, ID> {

	/**
	 * Creates the index.
	 *
	 * @param clazz the clazz
	 * @throws Exception the exception
	 */
	public void createIndex(final Class<? extends T> clazz) throws Exception;

	/**
	 * Delete index.
	 *
	 * @param clazz the clazz
	 * @throws Exception the exception
	 */
	public void deleteIndex(final Class<? extends T> clazz) throws Exception;

	/**
	 * Adds the.
	 *
	 * @param entity the entity
	 * @param clazz  the clazz
	 * @throws Exception the exception
	 */
	public void add(final T entity, final Class<? extends T> clazz) throws Exception;

	/**
	 * Find all.
	 *
	 * @param clazz            the clazz
	 * @param searchParameters the search parameters
	 * @return the iterable
	 * @throws Exception the exception
	 */
	public Iterable<T> findAll(final Class<? extends T> clazz, final SearchParameters searchParameters)
			throws Exception;

	/**
	 * Find by id.
	 *
	 * @param id    the id
	 * @param clazz the clazz
	 * @return the optional
	 * @throws Exception the exception
	 */
	public Optional<T> findById(final String id, final Class<? extends T> clazz) throws Exception;

	/**
	 * Find.
	 *
	 * @param clazz            the clazz
	 * @param searchParameters the search parameters
	 * @return the optional
	 * @throws Exception the exception
	 */
	public Iterable<T> find(final Class<? extends T> clazz, final SearchParameters searchParameters) throws Exception;

	/**
	 * Delete.
	 *
	 * @param clazz the clazz
	 * @param id    the id
	 * @throws Exception the exception
	 */
	public void remove(final Class<? extends T> clazz, final String id) throws Exception;

}