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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a piece of metadata related to a terminology. This is for things like term types and
 * relationship types so that descriptions of metadata codes or abbreviations can be separated from
 * the content.
 */
// @Entity
// @Table(name = "metadata")
@Schema(description = "Represents metadata about a terminology component")
@Document(indexName = "metadata-v1")
@JsonInclude(Include.NON_EMPTY)
public class Metadata extends AbstractTerminologyComponent
    implements Copyable<Metadata>, Comparable<Metadata> {

  /** The model. */
  @Field(type = FieldType.Keyword)
  private MetaModel.Model model;

  /** The field. */
  @Field(type = FieldType.Keyword)
  private MetaModel.Field field;

  /** The abbreviation. */
  @Field(type = FieldType.Keyword)
  private String code;

  /** The name. */
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword)
  })
  private String name;

  /** The rank. */
  @Field(type = FieldType.Keyword)
  private Integer rank = 0;

  /** The attributes. */
  @Field(type = FieldType.Object, enabled = false)
  private Map<String, String> attributes;

  /**
   * Instantiates an empty {@link Metadata}.
   */
  public Metadata() {
    // n/a
  }

  /**
   * Instantiates a {@link Metadata} from the specified parameters.
   *
   * @param model the model
   * @param field the field
   */
  public Metadata(final MetaModel.Model model, final MetaModel.Field field) {
    this.model = model;
    this.field = field;
  }

  /**
   * Instantiates a {@link Metadata} from the specified parameters.
   *
   * @param other the other
   */
  public Metadata(final Metadata other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  /* see superclass */
  @Override
  public void populateFrom(final Metadata other) {
    super.populateFrom(other);
    code = other.getCode();
    name = other.getName();
    rank = other.getRank();
    model = other.getModel();
    field = other.getField();
    attributes = new HashMap<>(other.getAttributes());
  }

  /**
   * Patch from.
   *
   * @param other the other
   */
  /* see superclass */
  @Override
  public void patchFrom(final Metadata other) {
    super.patchFrom(other);
    if (other.getCode() != null) {
      code = other.getCode();
    }
    if (other.getName() != null) {
      name = other.getName();
    }
    if (other.getRank() != null) {
      rank = other.getRank();
    }
    if (other.getModel() != null) {
      model = other.getModel();
    }
    if (other.getField() != null) {
      field = other.getField();
    }
    if (!other.getAttributes().isEmpty()) {
      attributes.putAll(other.getAttributes());
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
  public int compareTo(final Metadata other) {
    return getCode().compareTo(other.getCode());
  }

  /**
   * Gets the code.
   *
   * @return the code
   */
  @Schema(description = "Abbreviated code for a metadata item that has a longer name,"
      + " e.g. \"PT\" for \"Preferred term\"")
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
   * Returns the name.
   *
   * @return the name
   */
  @Schema(description = "Longer name of a metadata item")
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the name
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Gets the model.
   *
   * @return the model
   */
  @Schema(description = "Model object that this applies to")
  public MetaModel.Model getModel() {
    return model;
  }

  /**
   * Sets the model.
   *
   * @param model the new model
   */
  public void setModel(final MetaModel.Model model) {
    this.model = model;
  }

  /**
   * Gets the field.
   *
   * @return the field
   */
  @Schema(description = "Field of model object that this applies to")
  public MetaModel.Field getField() {
    return field;
  }

  /**
   * Sets the field.
   *
   * @param field the new field
   */
  public void setField(final MetaModel.Field field) {
    this.field = field;
  }

  /**
   * Returns the rank.
   *
   * @return the rank
   */
  @Schema(description = "Rank of this relative to other similar kinds of metadata "
      + "(primarily used for term type ranking)")
  public Integer getRank() {
    return rank;
  }

  /**
   * Sets the rank.
   *
   * @param rank the rank
   */
  public void setRank(final Integer rank) {
    this.rank = rank;
  }

  /**
   * Returns the attributes.
   *
   * @return the attributes
   */
  @Schema(description = "Attribute key/value pairs associated with the concept")
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  /**
   * Sets the attributes.
   *
   * @param attributes the attributes
   */
  public void setAttributes(final Map<String, String> attributes) {
    this.attributes = attributes;
  }

}
