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
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

/**
 * Represents a mapset reference. This serves as a pointer from a project to an actual mapset. It
 * can also be used to compute the mapset keys used by the model objects.
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MapsetRef extends TerminologyRef {

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

  /** The to publisher. */
  @Field(type = FieldType.Keyword)
  private String toPublisher;

  /** The to terminology. */
  @Field(type = FieldType.Keyword)
  private String toTerminology;

  /** The to version. */
  @Field(type = FieldType.Keyword)
  private String toVersion;

  /**
   * Instantiates an empty {@link MapsetRef}.
   */
  public MapsetRef() {
    // n/a
  }

  /**
   * Instantiates a new mapset ref.
   *
   * @param code the code
   * @param abbreviation the abbreviation
   * @param publisher the publisher
   * @param version the version
   */
  public MapsetRef(final String code, final String abbreviation, final String publisher,
      final String version) {
    setCode(code);
    setAbbreviation(abbreviation);
    setPublisher(publisher);
    setVersion(version);
    setActive(null);
    setLocal(null);
  }

  /**
   * Instantiates a new mapset ref.
   *
   * @param other the other
   */
  public MapsetRef(final MapsetRef other) {
    populateFrom(other);
  }

  /* see superclass */
  @Override
  public void populateFrom(final TerminologyRef other) {
    super.populateFrom(other);

    if (other instanceof MapsetRef) {
      final MapsetRef mapset = (MapsetRef) other;

      code = mapset.getCode();
      toPublisher = mapset.getToPublisher();
      toVersion = mapset.getToVersion();
      toTerminology = mapset.getToTerminology();
      fromTerminology = mapset.getFromTerminology();
      fromPublisher = mapset.getFromPublisher();
      fromVersion = mapset.getFromVersion();

    }
  }

  /* see superclass */
  @Override
  public void patchFrom(final TerminologyRef other) {
    super.patchFrom(other);
    if (other instanceof MapsetRef) {
      final MapsetRef mapset = (MapsetRef) other;
      if (mapset.getCode() != null) {
        code = mapset.getCode();
      }
      if (mapset.getToTerminology() != null) {
        toTerminology = mapset.getToTerminology();
      }
      if (mapset.getToPublisher() != null) {
        toPublisher = mapset.getToPublisher();
      }
      if (mapset.getToVersion() != null) {
        toVersion = mapset.getToVersion();
      }
      if (mapset.getFromTerminology() != null) {
        fromTerminology = mapset.getFromTerminology();
      }
      if (mapset.getFromPublisher() != null) {
        fromPublisher = mapset.getFromPublisher();
      }
      if (mapset.getFromVersion() != null) {
        fromVersion = mapset.getFromVersion();
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
  @Schema(description = "Mapset code")
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
   * Gets the to terminology.
   *
   * @return the to terminology
   */
  @Schema(description = "Terminology abbreviation that maps in this set are mapped to, "
      + "e.g. \"SNOMEDCT\"")
  public String getToTerminology() {
    return toTerminology;
  }

  /**
   * Sets the to terminology.
   *
   * @param toTerminology the new to terminology
   */
  public void setToTerminology(final String toTerminology) {
    this.toTerminology = toTerminology;
  }

  /**
   * Gets the to publisher.
   *
   * @return the to publisher
   */
  @Schema(description = "Publisher that maps in this set are mapped from, " + "e.g. \"SNOMEDCT\"")
  public String getToPublisher() {
    return toPublisher;
  }

  /**
   * Sets the to publisher.
   *
   * @param toPublisher the new to publisher
   */
  public void setToPublisher(final String toPublisher) {
    this.toPublisher = toPublisher;
  }

  /**
   * Gets the to version.
   *
   * @return the to version
   */
  @Schema(description = "Terminology version that maps in this set are mapped to, "
      + "e.g. \"20230901\"")
  public String getToVersion() {
    return toVersion;
  }

  /**
   * Sets the to version.
   *
   * @param toVersion the new to version
   */
  public void setToVersion(final String toVersion) {
    this.toVersion = toVersion;
  }

  /**
   * Gets the from terminology.
   *
   * @return the from terminology
   */
  @Schema(description = "Terminology abbreviation that maps in this set are mapped from, "
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
  @Schema(description = "Terminology version that maps in this set are mapped from, "
      + "e.g. \"20230901\"")
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
