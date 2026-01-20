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
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Represents a license for access to content.
 */
@Entity
@Table(name = "publisher_info")
@Schema(description = "Represents a content publisher from which data can be obtained")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublisherInfo extends AbstractHasModified
    implements HasLazyInit, Copyable<PublisherInfo> {

  /** The type. e.g. NLM, SNOMED, etc. */
  @Column(nullable = false, length = 256)
  @Field(type = FieldType.Keyword)
  private String type;

  /** The human readable name of the publisher type. */
  @Transient
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword)
  })
  private String name;

  /** The email. */
  @Transient
  @Field(type = FieldType.Keyword)
  private String email;

  /** The uri. e.g. main website */
  @Transient
  @Field(type = FieldType.Keyword)
  private String uri;

  /** The uri. e.g. main website */
  @Transient
  @Field(type = FieldType.Text)
  private String description;

  /**
   * Instantiates an empty {@link PublisherInfo}.
   */
  public PublisherInfo() {
    // n/a
  }

  /**
   * Instantiates a {@link PublisherInfo} from the specified parameters.
   *
   * @param other the other
   */
  public PublisherInfo(final PublisherInfo other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  @Override
  public void populateFrom(final PublisherInfo other) {
    super.populateFrom(other);
    type = other.getType();
    name = other.getName();
    email = other.getEmail();
    uri = other.getUri();
    description = other.getDescription();
  }

  /**
   * Patch from.
   *
   * @param other the other
   */
  @Override
  public void patchFrom(final PublisherInfo other) {
    super.patchFrom(other);
    if (other.getType() != null) {
      type = other.getType();
    }
    if (other.getName() != null) {
      name = other.getName();
    }
    if (other.getEmail() != null) {
      email = other.getEmail();
    }
    if (other.getUri() != null) {
      uri = other.getUri();
    }
    if (other.getDescription() != null) {
      description = other.getDescription();
    }

  }

  // /* see superclass */
  // @Override
  // @PostLoad
  // public void unmarshall() throws Exception {
  // final PublisherInfo resource = ModelUtility.fromJson(getData(),
  // this.getClass());
  // if (resource != null) {
  // populateFrom(resource);
  // }
  // }

  /* see superclass */
  @Override
  public void lazyInit() {
    // n/a
  }

  /**
   * Returns the type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the type.
   *
   * @param type the type
   */
  public void setType(final String type) {
    this.type = type;
  }

  /**
   * Returns the name.
   *
   * @return the name
   */
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
   * Returns the email.
   *
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the email.
   *
   * @param email the email
   */
  public void setEmail(final String email) {
    this.email = email;
  }

  /**
   * Returns the uri.
   *
   * @return the uri
   */
  public String getUri() {
    return uri;
  }

  /**
   * Sets the uri.
   *
   * @param uri the uri
   */
  public void setUri(final String uri) {
    this.uri = uri;
  }

  /**
   * Returns the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description the description
   */
  public void setDescription(final String description) {
    this.description = description;
  }

}
