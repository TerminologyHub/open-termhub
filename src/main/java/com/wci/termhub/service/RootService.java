/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.service;

import java.util.List;

import com.wci.termhub.model.HasId;
import com.wci.termhub.model.HasModified;

import jakarta.persistence.EntityManager;

/**
 * Top level root services.
 */
public interface RootService extends AutoCloseable {

  /** The logging object ct threshold. */
  public static final int LOG_CT = 5000;

  /** The commit count. */
  public static final int COMMIT_CT = 2000;

  /**
   * Checks if is modified flag.
   *
   * @return true, if is modified flag
   */
  public boolean isModifiedFlag();

  /**
   * Sets the modified flag.
   *
   * @param modifiedFlag the modified flag
   */
  public void setModifiedFlag(boolean modifiedFlag);

  /**
   * Returns the modified by.
   *
   * @return the modified by
   */
  public String getModifiedBy();

  /**
   * Sets the modified by.
   *
   * @param modifiedBy the modified by
   */
  public void setModifiedBy(String modifiedBy);

  /**
   * Lock Hibernate object.
   *
   * @param object the object
   * @throws Exception the exception
   */
  public void lockObject(Object object) throws Exception;

  /**
   * Unlock Hibernate object.
   *
   * @param object the object
   */
  public void unlockObject(Object object);

  /**
   * Is object locked.
   *
   * @param object the object
   * @return true, if is object locked
   * @throws Exception the exception
   */
  public boolean isObjectLocked(Object object) throws Exception;

  /**
   * New instance of the object type - this binds a service implementation to an object impl.
   *
   * @param <T> the
   * @param clazz the clazz
   * @return the t
   * @throws Exception the exception
   */
  public <T> T newInstance(Class<T> clazz) throws Exception;

  /**
   * Returns the type.
   *
   * @param <T> the type
   * @param <S> the sub type
   * @param clazz the clazz
   * @return the type
   * @throws Exception the exception
   */
  public <T extends HasModified, S extends T> Class<S> getType(Class<T> clazz) throws Exception;

  /**
   * Copy instance.
   *
   * @param <T> the
   * @param clazz the clazz
   * @param object the object
   * @return the t
   * @throws Exception the exception
   */
  public <T extends HasId> T copyInstance(Class<T> clazz, T object) throws Exception;

  /**
   * Returns the model object.
   *
   * @param <T> the
   * @param id the id
   * @param clazz the clazz
   * @return the model object
   * @throws Exception the exception
   */
  public <T extends HasId> T get(String id, Class<T> clazz) throws Exception;

  // /**
  // * Find model objects. This uses elasticsearch to identify ids and then
  // * looks everything up by id.
  // *
  // * @param <T> the
  // * @param params the params
  // * @param type the type
  // * @param handler the handler
  // * @return the list
  // * @throws Exception the exception
  // */
  // public <T extends HasId> ResultList<T> find(SearchParameters params,
  // Class<T> type,
  // String handler) throws Exception;
  //
  // /**
  // * Find single.
  // *
  // * @param <T> the
  // * @param params the params
  // * @param type the type
  // * @param handler the handler
  // * @return the t
  // * @throws Exception the exception
  // */
  // public <T extends HasId> T findSingle(SearchParameters params, Class<T>
  // type, String handler)
  // throws Exception;

  /**
   * Find single.
   *
   * @param <T> the
   * @param field the field
   * @param value the value
   * @param type the type
   * @return the t
   * @throws Exception the exception
   */
  public <T extends HasId> T findSingle(String field, String value, Class<T> type) throws Exception;

  /**
   * Find.
   *
   * @param <T> the
   * @param field the field
   * @param value the value
   * @param type the type
   * @return the list
   * @throws Exception the exception
   */
  public <T extends HasId> List<T> find(String field, String value, Class<T> type) throws Exception;

  /**
   * Find ids.
   *
   * @param <T> the
   * @param field the field
   * @param value the value
   * @param type the type
   * @return the list
   * @throws Exception the exception
   */
  public <T extends HasId> List<String> findIds(String field, String value, Class<T> type)
    throws Exception;

  //
  // /**
  // * Find ids.
  // *
  // * @param <T> the
  // * @param params the params
  // * @param type the type
  // * @param handler the handler
  // * @return the result list
  // * @throws Exception the exception
  // */
  // public <T extends HasId> ResultList<String> findIds(SearchParameters
  // params, Class<T> type,
  // String handler) throws Exception;

  /**
   * Adds the model object.
   *
   * @param <T> the
   * @param object the model object
   * @return the t
   * @throws Exception the exception
   */
  public <T extends HasModified> T add(T object) throws Exception;

  /**
   * Adds the object.
   *
   * @param <T> the
   * @param object the object
   * @return the t
   * @throws Exception the exception
   */
  public <T extends Object> T addObject(final T object) throws Exception;

  /**
   * Update model object.
   *
   * @param <T> the
   * @param object the model object
   * @return the t
   * @throws Exception the exception
   */
  public <T extends HasModified> T update(T object) throws Exception;

  /**
   * Update object.
   *
   * @param <T> the
   * @param object the object
   * @throws Exception the exception
   */
  public <T extends Object> void updateObject(T object) throws Exception;

  /**
   * Removes the model object.
   *
   * @param <T> the
   * @param object the model object
   * @throws Exception the exception
   */
  public <T extends HasModified> void remove(T object) throws Exception;

  /**
   * Removes the object.
   *
   * @param <T> the
   * @param object the object
   * @throws Exception the exception
   */
  public <T extends Object> void removeObject(T object) throws Exception;

  /**
   * Returns the entity manager.
   *
   * @return the entity manager
   * @throws Exception the exception
   */
  public EntityManager getEntityManager() throws Exception;

  /**
   * Gets the transaction per operation.
   *
   * @return the transaction per operation
   * @throws Exception the exception
   */
  public boolean getTransactionPerOperation() throws Exception;

  /**
   * Sets the transaction per operation.
   *
   * @param transactionPerOperation the new transaction per operation
   * @throws Exception the exception
   */
  public void setTransactionPerOperation(boolean transactionPerOperation) throws Exception;

  /**
   * Commit.
   *
   * @throws Exception the exception
   */
  public void commit() throws Exception;

  /**
   * Commit and wait for indexes.
   *
   * @throws Exception the exception
   */
  public void commitAndWait() throws Exception;

  /**
   * Rollback.
   *
   * @throws Exception the exception
   */
  public void rollback() throws Exception;

  /**
   * Begin transaction.
   *
   * @throws Exception the exception
   */
  public void beginTransaction() throws Exception;

  /**
   * Closes the manager.
   *
   * @throws Exception the exception
   */
  @Override
  public void close() throws Exception;

  /**
   * Clears the manager.
   *
   * @throws Exception the exception
   */
  public void clear() throws Exception;

  /**
   * Commit clear begin transaction.
   *
   * @throws Exception the exception
   */
  public void commitClearBegin() throws Exception;

  /**
   * Log and commit.
   *
   * @param objectCt the object ct
   * @param logCt the log ct
   * @param commitCt the commit ct
   * @throws Exception the exception
   */
  public void logAndCommit(int objectCt, int logCt, int commitCt) throws Exception;

  /**
   * Reindex all instances of class.
   *
   * @param <T> the
   * @param clazz the clazz
   * @param force the force
   * @return the long
   * @throws Exception the exception
   */
  public <T extends HasId> long reindex(Class<T> clazz, boolean force) throws Exception;

}
