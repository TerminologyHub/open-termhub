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
import com.wci.termhub.util.StringUtility;

/**
 * Base unit test.
 */
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

  /**
   * The Class TermQuery.
   */
  protected class TermQueryComposer {

    /** The Constant serialVersionUID. */
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    /** The terminology. */
    private String terminology;

    /** The version. */
    private String version;

    /** The code. */
    private String code;

    /** The name. */
    private final String name;

    /**
     * Instantiates a new term query.
     *
     * @param terminology the terminology
     * @param version the version
     * @param code the code
     * @param name the name
     */
    public TermQueryComposer(final String terminology, final String version, final String code,
        final String name) {
      this.terminology = terminology;
      this.version = version;
      this.code = code;
      this.name = name;
    }

    /**
     * Gets the terminology.
     *
     * @return the terminology
     */
    public String getTerminology() {
      return terminology;
    }

    /**
     * Sets the terminology.
     *
     * @param terminology the new terminology
     */
    public void setTerminology(final String terminology) {
      this.terminology = terminology;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion() {
      return version;
    }

    /**
     * Sets the version.
     *
     * @param version the new version
     */
    public void setVersion(final String version) {
      this.version = version;
    }

    /**
     * Gets the code.
     *
     * @return the code
     */
    public String getCode() {
      return code;
    }

    /**
     * Sets the code.
     *
     * @param code the new code
     */
    public void setCode(final String code) {
      this.code = code;
    }

    /**
     * Gets the query.
     *
     * @return the query
     */
    public String getQuery() {
      final StringBuilder sb = new StringBuilder();

      sb.append("terminology:").append(terminology);
      if (version != null) {
        sb.append(" AND version:").append(StringUtility.escapeQuery(version));
      }
      if (code != null) {
        sb.append(" AND code:").append(StringUtility.escapeQuery(code));
      }
      if (name != null) {
        sb.append(" AND name:").append(StringUtility.escapeQuery(name));
      }

      return sb.toString();
    }
  }
}
