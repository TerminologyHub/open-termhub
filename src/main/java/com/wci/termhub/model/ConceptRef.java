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
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a reference to a concept with minimal information.
 */
@Schema(
    description = "Represents enough information to uniquely reference a concept in a terminology")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConceptRef extends AbstractHasModified
    implements HasName, TerminologyComponent, Copyable<ConceptRef>, Comparable<ConceptRef> {

  /** The name. */
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword)
  })
  private String name;

  /** The code. */
  @Field(type = FieldType.Keyword)
  private String code;

  /** The terminology. */
  @Field(type = FieldType.Keyword)
  private String terminology;

  /** The version. */
  @Field(type = FieldType.Keyword)
  private String version;

  /** The publisher. */
  @Field(type = FieldType.Keyword)
  private String publisher;

  /** The historical rel. */
  @Field(type = FieldType.Keyword)
  private String historical;

  /** The anonymous flag. */
  @Field(type = FieldType.Boolean)
  private Boolean leaf = false;

  /** The defined. */
  @Field(type = FieldType.Boolean)
  private Boolean defined;

  /** The level. */
  @Field(type = FieldType.Integer)
  private Integer level;

  /**
   * Instantiates an empty {@link ConceptRef}.
   */
  public ConceptRef() {
    // n/a
  }

  /**
   * Instantiates a new concept ref.
   *
   * @param code the code
   * @param name the name
   */
  public ConceptRef(final String code, final String name) {
    this.code = code;
    this.name = name;
  }

  /**
   * Instantiates a {@link ConceptRef} from the specified parameters.
   *
   * @param other the other
   */
  public ConceptRef(final ConceptRef other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  /* see superclass */
  @Override
  public void populateFrom(final ConceptRef other) {
    super.populateFrom(other);
    name = other.getName();
    code = other.getCode();
    terminology = other.getTerminology();
    version = other.getVersion();
    publisher = other.getPublisher();
    historical = other.getHistorical();
    leaf = other.getLeaf();
    defined = other.getDefined();
    level = other.getLevel();
  }

  /**
   * Patch from.
   *
   * @param other the other
   */
  /* see superclass */
  @Override
  public void patchFrom(final ConceptRef other) {
    super.patchFrom(other);
    if (other.getName() != null) {
      name = other.getName();
    }
    if (other.getCode() != null) {
      code = other.getCode();
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
    if (other.getHistorical() != null) {
      historical = other.getHistorical();
    }
    if (other.getLeaf() != null) {
      leaf = other.getLeaf();
    }
    if (other.getDefined() != null) {
      defined = other.getDefined();
    }
    if (other.getLevel() != null) {
      level = other.getLevel();
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
  public int compareTo(final ConceptRef other) {
    return getCode().compareTo(other.getCode());
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
   * Gets the name.
   *
   * @return the name
   */
  /* see superclass */
  @Override
  @Schema(description = "Concept name", required = false)
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
   * Returns the terminology.
   *
   * @return the terminology
   */
  @Override
  @Schema(description = "Terminology abbreviation", required = false)
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
  @Schema(description = "Terminology version", required = false)
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
  @Schema(description = "Terminology publisher", required = false)
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

  /**
   * Gets the historical.
   *
   * @return the historical
   */
  @Schema(description = "Historical relationship type (only used for concept descendants)",
      required = false)
  public String getHistorical() {
    return historical;
  }

  /**
   * Sets the historical.
   *
   * @param historical the new historical
   */
  public void setHistorical(final String historical) {
    this.historical = historical;
  }

  /**
   * Returns the code.
   *
   * @return the code
   */
  @Schema(description = "Terminology code, typically representing a unit of meaning")
  public String getCode() {
    return code;
  }

  /**
   * Sets the code.
   *
   * @param code the code
   */
  public void setCode(final String code) {
    this.code = code;
  }

  /**
   * Indicates whether or not leaf is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Schema(description = "Indicates whether or not this concept is a leaf node in its hierarchy")
  public Boolean getLeaf() {
    return leaf;
  }

  /**
   * Sets the leaf.
   *
   * @param leaf the leaf
   */
  public void setLeaf(final Boolean leaf) {
    this.leaf = leaf;
  }

  /**
   * Returns the defined.
   *
   * @return the defined
   */
  @Schema(description = "Indicates whether or not this concept has a "
      + "logical definition with necessary and sufficient conditions")
  public Boolean getDefined() {
    return defined;
  }

  /**
   * Sets the defined.
   *
   * @param defined the defined
   */
  public void setDefined(final Boolean defined) {
    this.defined = defined;
  }

  /**
   * Returns the level.
   *
   * @return the level
   */
  @Schema(description = "Level of depth")
  public Integer getLevel() {
    return level;
  }

  /**
   * Sets the level.
   *
   * @param level the level
   */
  public void setLevel(final Integer level) {
    this.level = level;
  }
}
