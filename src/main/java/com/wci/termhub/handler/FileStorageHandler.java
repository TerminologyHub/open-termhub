/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.handler;

import java.io.InputStream;
import java.util.List;

import com.wci.termhub.model.Configurable;
import com.wci.termhub.model.FileInfo;

/**
 * Represents a handler for file storage.
 */
public interface FileStorageHandler extends Configurable {

  /**
   * Returns the top uri.
   *
   * @return the top uri
   * @throws Exception the exception
   */
  public String getTopUri() throws Exception;

  /**
   * List.
   *
   * @return the list
   * @throws Exception the exception
   */
  public List<String> list() throws Exception;

  /**
   * List.
   *
   * @param prefix the prefix
   * @return the list
   * @throws Exception the exception
   */
  public List<String> list(final String prefix) throws Exception;

  /**
   * Accepts.
   *
   * @param uri the uri
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean accepts(String uri) throws Exception;

  /**
   * Exists.
   *
   * @param uri the uri
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean exists(final String uri) throws Exception;

  /**
   * Put file.
   *
   * @param uri the uri
   * @param is the is
   * @return the file info
   * @throws Exception the exception
   */
  public FileInfo putFile(String uri, InputStream is) throws Exception;

  /**
   * Put and encrypt file.
   *
   * @param uri the uri
   * @param is the is
   * @param key the key
   * @param salt the salt
   * @return the file info
   * @throws Exception the exception
   */
  public FileInfo putAndEncryptFile(String uri, InputStream is, String key, String salt)
    throws Exception;

  /**
   * Returns the and decrypt file.
   *
   * @param uri the uri
   * @param key the key
   * @param salt the salt
   * @return the and decrypt file
   * @throws Exception the exception
   */
  public InputStream getAndDecryptFile(String uri, String key, String salt) throws Exception;

  /**
   * Returns an input stream for a large file. In the background, this may get
   * downloaded to a local directory first. *
   * 
   * @param uri the uri
   * @return the file
   * @throws Exception the exception
   */
  public InputStream getLargeFile(String uri) throws Exception;

  /**
   * Returns an input stream for the file uri.
   *
   * @param uri the uri
   * @return the file
   * @throws Exception the exception
   */
  public InputStream getFile(String uri) throws Exception;

  /**
   * Delete file.
   *
   * @param uri the uri
   * @throws Exception the exception
   */
  public void deleteFile(final String uri) throws Exception;

  /**
   * Find handler.
   *
   * @param handlers the handlers
   * @param uri the uri
   * @return the file storage handler
   * @throws Exception the exception
   */
  public static FileStorageHandler findHandler(final List<FileStorageHandler> handlers,
    final String uri) throws Exception {
    for (final FileStorageHandler handler : handlers) {
      if (handler.accepts(uri)) {
        return handler;
      }
    }
    throw new Exception("Unable to find file storage handler for - " + uri);
  }

}
