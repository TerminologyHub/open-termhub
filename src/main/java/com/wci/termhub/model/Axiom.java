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

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents an OWL/RDF axiom for the concept.
 */
@Schema(description = "Represents an OWL/RDF axiom for the concept")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Axiom extends AbstractHasModified
    implements TerminologyComponent, Copyable<Axiom>, Comparable<Axiom> {

  /** The value (not indexed). */
  @Field(type = FieldType.Object, enabled = false)
  private String value;

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
   * Instantiates an empty {@link Axiom}.
   */
  public Axiom() {
    // n/a
  }

  /**
   * Instantiates a {@link Axiom} from the specified parameters.
   *
   * @param other the other
   */
  public Axiom(final Axiom other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  @Override
  public void populateFrom(final Axiom other) {
    super.populateFrom(other);
    value = other.getValue();
    terminology = other.getTerminology();
    version = other.getVersion();
    publisher = other.getPublisher();
  }

  /* see superclass */
  @Override
  public void patchFrom(final Axiom other) {
    super.patchFrom(other);
    if (other.getValue() != null) {
      value = other.getValue();
    }
    if (other.getTerminology() != null) {
      terminology = other.getTerminology();
    }
    if (other.getVersion() != null) {
      version = other.getVersion();
    }
    if (other.getPublisher() != null) {
      publisher = other.getPublisher();
    }
  }

  /**
   * Compare to.
   *
   * @param other the other
   * @return the int
   */
  @Override
  public int compareTo(final Axiom other) {
    return getValue().compareTo(other.getValue());
  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  @Schema(description = "Axiom expressed in OwL Manchester syntax "
      + "(https://www.w3.org/TR/owl2-manchester-syntax/)")
  public String getValue() {
    return value;
  }

  /**
   * Sets the value.
   *
   * @param value the new value
   */
  public void setValue(final String value) {
    this.value = value;
  }

  /**
   * Returns the terminology.
   *
   * @return the terminology
   */
  @Override
  public String getTerminology() {
    return terminology;
  }

  /**
   * Sets the terminology.
   *
   * @param terminology the terminology
   */
  @Override
  public void setTerminology(final String terminology) {
    this.terminology = terminology;
  }

  /**
   * Returns the version.
   *
   * @return the version
   */
  @Override
  public String getVersion() {
    return version;
  }

  /**
   * Sets the version.
   *
   * @param version the version
   */
  @Override
  public void setVersion(final String version) {
    this.version = version;
  }

  /**
   * Returns the publisher.
   *
   * @return the publisher
   */
  @Override
  public String getPublisher() {
    return publisher;
  }

  /**
   * Sets the publisher.
   *
   * @param publisher the publisher
   */
  @Override
  public void setPublisher(final String publisher) {
    this.publisher = publisher;
  }

}
