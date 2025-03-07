/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model;

import java.util.UUID;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.wci.termhub.util.ModelUtility;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

/**
 * Abstractly represents something that has an id.
 */
@MappedSuperclass
public abstract class AbstractHasIdNoGenerator implements HasId {

  /** The id. */
  @Id
  @Column(updatable = false, nullable = false, length = 64)
  @Field(type = FieldType.Keyword)
  private String id = UUID.randomUUID().toString();

  /** The confidence. */
  @Transient
  private Double confidence;

  /**
   * Instantiates an empty {@link AbstractHasIdNoGenerator}.
   */
  protected AbstractHasIdNoGenerator() {
    // n/a
  }

  /**
   * Instantiates a {@link AbstractHasIdNoGenerator} from the specified
   * parameters.
   *
   * @param other the other
   */
  protected AbstractHasIdNoGenerator(final HasId other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  public void populateFrom(final HasId other) {
    this.id = other.getId();
    this.confidence = other.getConfidence();
  }

  /**
   * Returns the id. NOTE: this causes a hibernate warning, but the background
   * behavior is correct. Moving it or removing it causes either other problems
   * or the field to not be indexed.
   * 
   * @return the id
   */
  @Override
  // @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  // @SortableField
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the id
   */
  @Override
  public void setId(final String id) {
    this.id = id;
  }

  /* see superclass */
  @Override
  public Double getConfidence() {
    return confidence;
  }

  /* see superclass */
  @Override
  public void setConfidence(final Double confidence) {
    this.confidence = confidence;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }
    final AbstractHasIdNoGenerator other = (AbstractHasIdNoGenerator) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    try {
      return ModelUtility.toJson(this);
    } catch (final Exception e) {
      return e.getMessage();
    }
  }
}
