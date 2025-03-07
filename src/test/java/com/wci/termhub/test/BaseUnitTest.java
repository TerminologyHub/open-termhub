/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.test;

import java.util.Collection;

import com.wci.termhub.util.PropertyUtility;

public abstract class BaseUnitTest {

  /** The Constant INDEX_DIRECTORY. */
  protected static final String INDEX_DIRECTORY =
      PropertyUtility.getProperties().getProperty("lucene.index.directory");

  /**
   * Gets the size.
   *
   * @param <T> the generic type
   * @param iterable the iterable
   * @return the size
   */
  @SuppressWarnings("unused")
  protected static <T> int getSize(final Iterable<T> iterable) {
    if (iterable instanceof Collection) {
      return ((Collection<T>) iterable).size();
    } else {
      int size = 0;
      for (final T item : iterable) {
        size++;
      }
      return size;
    }
  }
}
