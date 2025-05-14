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

import java.util.Objects;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Transient;

/**
 * Represents a terminology reference. This serves as a pointer from a project to an actual
 * terminology. It can also be used to compute the terminology keys used by the model objects.
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TerminologyRef extends AbstractHasModified // extends
    // AbstractHasJsonData
    implements HasName, HasMinimize, Copyable<TerminologyRef>, Comparable<TerminologyRef>
{

  /** The abbreviation. */
  @Transient
  @Field(type = FieldType.Keyword)
  private String abbreviation;

  /** The name. */
  @Transient
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword)
  })
  private String name;

  /** The version. */
  @Transient
  @Field(type = FieldType.Keyword)
  private String version;

  /** The publisher - e.g. "TERMHUB", "NLM", "SNOMED" */
  @Transient
  @Field(type = FieldType.Keyword)
  private String publisher;

  /** The latest flag. */
  @Transient
  @Field(type = FieldType.Boolean)
  private Boolean latest;

  /** The loaded. */
  @Transient
  @Field(type = FieldType.Boolean)
  private Boolean loaded;

  /**
   * Instantiates an empty {@link TerminologyRef}.
   */
  public TerminologyRef() {
    // n/a
  }

  /**
   * Instantiates a {@link TerminologyRef} from the specified parameters.
   *
   * @param abbreviation the abbreviation
   * @param version the version
   * @param latest the latest
   * @param publisher the publisher
   */
  public TerminologyRef(final String abbreviation, final String version, final Boolean latest,
      final String publisher) {
    this.abbreviation = abbreviation;
    this.version = version;
    this.latest = latest;
    this.publisher = publisher;
  }

  /**
   * Instantiates a {@link TerminologyRef} from the specified parameters.
   *
   * @param other the other
   */
  public TerminologyRef(final TerminologyRef other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  @Override
  public void populateFrom(final TerminologyRef other) {
    super.populateFrom(other);
    abbreviation = other.getAbbreviation();
    name = other.getName();
    version = other.getVersion();
    latest = other.getLatest();
    loaded = other.getLoaded();
    publisher = other.getPublisher();
  }

  /**
   * Patch from.
   *
   * @param other the other
   */
  @Override
  public void patchFrom(final TerminologyRef other) {
    super.patchFrom(other);
    if (other.getAbbreviation() != null) {
      abbreviation = other.getAbbreviation();
    }
    if (other.getName() != null) {
      name = other.getName();
    }
    if (other.getVersion() != null) {
      version = other.getVersion();
    }
    if (other.getLatest() != null) {
      latest = other.getLatest();
    }
    if (other.getLoaded() != null) {
      loaded = other.getLoaded();
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
  /* see superclass */
  @Override
  public int compareTo(final TerminologyRef other) {
    return getAbbreviation().compareTo(other.getAbbreviation());
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  /* see superclass */
  @Override
  @Schema(description = "Unique identifier", required = false, format = "uuid")
  public String getId() {
    return super.getId();
  }

  /**
   * Returns the abbreviation.
   *
   * @return the abbreviation
   */
  @Schema(description = "Terminology abbreviation, e.g. \"SNOMEDCT\"")
  public String getAbbreviation() {
    return abbreviation;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  /* see superclass */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  /* see superclass */
  @Override
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Sets the abbreviation.
   *
   * @param abbreviation the abbreviation
   */
  public void setAbbreviation(final String abbreviation) {
    this.abbreviation = abbreviation;
  }

  /**
   * Returns the version.
   *
   * @return the version
   */
  @Schema(description = "Terminology version, e.g. \"20230901\"")
  public String getVersion() {
    return version;
  }

  /**
   * Sets the version.
   *
   * @param version the version
   */
  public void setVersion(final String version) {
    this.version = version;
  }

  /**
   * Returns the latest.
   *
   * @return the latest
   */
  @Schema(description = "Indicates whether this is the latest version of the terminology")
  public Boolean getLatest() {
    return latest;
  }

  /**
   * Sets the latest.
   *
   * @param latest the latest
   */
  public void setLatest(final Boolean latest) {
    this.latest = latest;
  }

  /**
   * Gets the loaded flag.
   *
   * @return the loaded
   */
  @Schema(description = "Indicates whether this is the version of the terminology is loaded")
  public Boolean getLoaded() {
    return loaded;
  }

  /**
   * Sets the loaded.
   *
   * @param loaded the new loaded
   */
  public void setLoaded(final Boolean loaded) {
    this.loaded = loaded;
  }

  /**
   * Returns the publisher.
   *
   * @return the publisher
   */
  @Schema(description = "Terminology publisher, e.g. \"SNOMEDCT\"")
  public String getPublisher() {
    return publisher;
  }

  /**
   * Sets the publisher.
   *
   * @param publisher the publisher
   */
  public void setPublisher(final String publisher) {
    this.publisher = publisher;
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Objects.hash(abbreviation, latest, loaded, name, publisher, version);
    return result;
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  /* see superclass */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj) || (getClass() != obj.getClass())) {
      return false;
    }
    final TerminologyRef other = (TerminologyRef) obj;
    return Objects.equals(abbreviation, other.abbreviation) && Objects.equals(latest, other.latest)
        && Objects.equals(loaded, other.loaded) && Objects.equals(name, other.name)
        && Objects.equals(publisher, other.publisher) && Objects.equals(version, other.version);
  }

  /**
   * Minimize.
   */
  /* see superclass */
  @Override
  public void minimize() {
    // At the end of this, only abbreviation, publisher, and version or latest
    // should be set
    final Boolean latest = getLatest();
    super.minimize();
    setId(null);
    setConfidence(null);
    loaded = null;
    name = null;
    if (latest != null && latest) {
      version = null;
      setLatest(latest);
    }
  }

  /**
   * Clean for api.
   */
  /* see superclass */
  @Override
  public void cleanForApi() {
    //
  }
}
