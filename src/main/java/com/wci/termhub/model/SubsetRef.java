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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

/**
 * Represents a subsetset reference. This serves as a pointer from a project to an actual subset. It
 * can also be used to compute the subset keys used by the model objects.
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubsetRef extends TerminologyRef {

  /** The abbreviation. */
  @Field(type = FieldType.Keyword)
  private String code;

  /** The from publisher. */
  @Field(type = FieldType.Keyword)
  private String fromPublisher;

  /** The from terminology. */
  @Field(type = FieldType.Keyword)
  private String fromTerminology;

  /** The from version. */
  @Field(type = FieldType.Keyword)
  private String fromVersion;

  /**
   * Instantiates an empty {@link SubsetRef}.
   */
  public SubsetRef() {
    // n/a
  }

  /**
   * Instantiates a new subset ref.
   *
   * @param code the code
   * @param abbreviation the abbreviation
   * @param publisher the publisher
   * @param version the version
   */
  public SubsetRef(final String code, final String abbreviation, final String publisher,
      final String version) {
    setCode(code);
    setAbbreviation(abbreviation);
    setPublisher(publisher);
    setVersion(version);
    setActive(null);
    setLocal(null);
  }

  /**
   * Instantiates a new subset ref.
   *
   * @param other the other
   */
  public SubsetRef(final SubsetRef other) {
    populateFrom(other);
  }

  /* see superclass */
  @Override
  public void populateFrom(final TerminologyRef other) {
    super.populateFrom(other);

    if (other instanceof SubsetRef) {
      final SubsetRef subset = (SubsetRef) other;
      code = subset.getCode();
      fromTerminology = subset.getFromTerminology();
      fromPublisher = subset.getFromPublisher();
      fromVersion = subset.getFromVersion();
    }
  }

  /* see superclass */
  @Override
  public void patchFrom(final TerminologyRef other) {
    super.patchFrom(other);
    if (other instanceof SubsetRef) {
      final SubsetRef subset = (SubsetRef) other;
      if (subset.getCode() != null) {
        code = subset.getCode();
      }
      if (subset.getFromTerminology() != null) {
        fromTerminology = subset.getFromTerminology();
      }
      if (subset.getFromPublisher() != null) {
        fromPublisher = subset.getFromPublisher();
      }
      if (subset.getFromVersion() != null) {
        fromVersion = subset.getFromVersion();
      }
    }
  }

  /* see superclass */
  @Override
  @Schema(description = "Unique identifier", requiredMode = RequiredMode.NOT_REQUIRED,
      format = "uuid")
  public String getId() {
    return super.getId();
  }

  /**
   * Gets the code.
   *
   * @return the code
   */
  @Schema(description = "Subset code")
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
   * Gets the from terminology.
   *
   * @return the from terminology
   */
  @Schema(description = "Terminology abbreviation that members in this set are from, "
      + "e.g. \"SNOMEDCT\"")
  public String getFromTerminology() {
    return fromTerminology;
  }

  /**
   * Sets the from terminology.
   *
   * @param fromTerminology the new from terminology
   */
  public void setFromTerminology(final String fromTerminology) {
    this.fromTerminology = fromTerminology;
  }

  /**
   * Gets the from publisher.
   *
   * @return the from publisher
   */
  @Schema(description = "Publisher that maps in this set are mapped from, " + "e.g. \"SNOMEDCT\"")
  public String getFromPublisher() {
    return fromPublisher;
  }

  /**
   * Sets the from publisher.
   *
   * @param fromPublisher the new from publisher
   */
  public void setFromPublisher(final String fromPublisher) {
    this.fromPublisher = fromPublisher;
  }

  /**
   * Gets the from version.
   *
   * @return the from version
   */
  @Schema(
      description = "Terminology version that members in this set are from, " + "e.g. \"20230901\"")
  public String getFromVersion() {
    return fromVersion;
  }

  /**
   * Sets the from version.
   *
   * @param fromVersion the new from version
   */
  public void setFromVersion(final String fromVersion) {
    this.fromVersion = fromVersion;
  }
}
