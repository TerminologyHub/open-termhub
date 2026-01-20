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

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a member of a subset, refset, or value set in a terminology.
 */
@Schema(description = "Represents a member of a subset, refset, or value set in a terminology")
@Document(indexName = "subset-member-v1")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubsetMember extends AbstractTerminologyComponent
    implements HasAttributes, Comparable<SubsetMember>, Copyable<SubsetMember> {

  /** The name. */
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword)
  })
  private String name;

  /** The code. */
  @Field(type = FieldType.Keyword)
  private String code;

  /** The subset this belongs to. */
  @Field(type = FieldType.Object)
  private SubsetRef subset;

  /** The code active. */
  @Field(type = FieldType.Boolean)
  private Boolean codeActive;

  /** The code exists. */
  @Field(type = FieldType.Boolean)
  private Boolean codeExists;

  /** The attributes. */
  @Field(type = FieldType.Object)
  private Map<String, String> attributes;

  /**
   * Instantiates an empty {@link SubsetMember}.
   */
  public SubsetMember() {
    // n/a
  }

  /**
   * Instantiates a {@link SubsetMember} from the specified parameters.
   *
   * @param other the other
   */
  public SubsetMember(final SubsetMember other) {
    populateFrom(other);
  }

  /* see superclass */
  @Override
  public void populateFrom(final SubsetMember other) {
    super.populateFrom(other);

    attributes = new HashMap<>(other.getAttributes());
    code = other.getCode();
    codeActive = other.getCodeActive();
    codeExists = other.getCodeExists();
    name = other.getName();
    subset = other.getSubset();

  }

  /* see superclass */
  @Override
  public void patchFrom(final SubsetMember other) {
    super.patchFrom(other);
    if (!other.getAttributes().isEmpty()) {
      getAttributes().putAll(other.getAttributes());
    }
    if (other.getCode() != null) {
      code = other.getCode();
    }
    if (other.getCodeActive() != null) {
      codeActive = other.getCodeActive();
    }
    if (other.getCodeExists() != null) {
      codeExists = other.getCodeExists();
    }
    if (other.getCode() != null) {
      code = other.getCode();
    }
    if (other.getName() != null) {
      name = other.getName();
    }
    if (other.getSubset() != null) {
      subset = other.getSubset();
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
  public int compareTo(final SubsetMember other) {
    return getCode().compareTo(other.getCode());
  }

  /* see superclass */
  @Override
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  /* see superclass */
  @Override
  public void setAttributes(final Map<String, String> attributes) {
    this.attributes = attributes;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @Schema(description = "Prefererd name of the member code")
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Gets the code.
   *
   * @return the code
   */
  @Schema(description = "Member code in the subset")
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
   * Gets the subset .
   *
   * @return the subset
   */
  @Schema(description = "Reference to the subset this member is part of")
  public SubsetRef getSubset() {
    return subset;
  }

  /**
   * Sets the subset.
   *
   * @param subset the new subset
   */
  public void setSubset(final SubsetRef subset) {
    this.subset = subset;
  }

  /**
   * Gets the code active.
   *
   * @return the code active
   */
  @Schema(description = "Indicates whether the member code is active or not")
  public Boolean getCodeActive() {
    return codeActive;
  }

  /**
   * Sets the code active.
   *
   * @param codeActive the new code active
   */
  public void setCodeActive(final Boolean codeActive) {
    this.codeActive = codeActive;
  }

  /**
   * Gets the code exists.
   *
   * @return the code exists
   */
  @Schema(description = "Indicates whether the member code exists or not")
  public Boolean getCodeExists() {
    return codeExists;
  }

  /**
   * Sets the code exists.
   *
   * @param codeExists the new code exists
   */
  public void setCodeExists(final Boolean codeExists) {
    this.codeExists = codeExists;
  }

}
