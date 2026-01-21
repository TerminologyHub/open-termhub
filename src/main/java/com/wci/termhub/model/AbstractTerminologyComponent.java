/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Represents an abstract implementation of TerminologyComponent.
 */
public abstract class AbstractTerminologyComponent extends AbstractHasModified
    implements TerminologyComponent {
  /** The terminology. */
  @Field(type = FieldType.Keyword)
  private String terminology;

  /** The version. */
  @Field(type = FieldType.Keyword)
  private String version;

  /** The publisher. */
  @Field(type = FieldType.Keyword)
  private String publisher;

  /**
   * Instantiates an empty {@link AbstractTerminologyComponent}.
   */
  public AbstractTerminologyComponent() {
    // n/a
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  public void populateFrom(final TerminologyComponent other) {
    super.populateFrom(other);
    terminology = other.getTerminology();
    version = other.getVersion();
    publisher = other.getPublisher();
  }

  /**
   * Patch from.
   *
   * @param other the other
   */
  public void patchFrom(final TerminologyComponent other) {
    super.patchFrom(other);
    if (other.getTerminology() != null) {
      terminology = other.getTerminology();
    }
    if (other.getVersion() != null) {
      version = other.getVersion();
    }
    if (other.getPublisher() != null) {
      version = other.getPublisher();
    }
  }

  /* see superclass */
  @Override
  public String getTerminology() {
    return terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(final String terminology) {
    this.terminology = terminology;
  }

  /* see superclass */
  @Override
  public String getVersion() {
    return version;
  }

  /* see superclass */
  @Override
  public void setVersion(final String version) {
    this.version = version;
  }

  /* see superclass */
  @Override
  public String getPublisher() {
    return publisher;
  }

  /* see superclass */
  @Override
  public void setPublisher(final String publisher) {
    this.publisher = publisher;
  }

}
