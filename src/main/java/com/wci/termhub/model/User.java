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

import java.util.Date;

import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.wci.termhub.model.enums.UserRole;
import com.wci.termhub.util.ModelUtility;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Transient;

/**
 * Represents a user within the system.
 */

@Schema(description = "Represents a user allowed to perform actions in the system")
@Document(indexName = "user-v1")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends AbstractHasModified implements HasLazyInit, Copyable<User> {

  /** The username. */
  @Column(nullable = false, length = 256)
  @Field(type = FieldType.Keyword)
  private String username;

  /** The password. */
  @Transient
  @Field(type = FieldType.Object, enabled = false)
  private String password;

  /** Pass this in from JSON but not the other way. */
  @JsonProperty(access = Access.WRITE_ONLY)
  @Transient
  @Field(type = FieldType.Object, enabled = false)
  private String oldPassword;

  /** The uri label. */
  @Column(nullable = false, length = 256)
  @Field(type = FieldType.Keyword)
  private String uriLabel;

  /** The verified. */
  @Transient
  @Field(type = FieldType.Boolean)
  private Boolean verified = false;

  /** The role. */
  @Transient
  @Field(type = FieldType.Keyword)
  private UserRole role = null;

  /** The name. */
  @Transient
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword)
  })
  private String name;

  /** The plan. */
  @Transient
  @Field(type = FieldType.Keyword)
  private String plan = null;

  /** The plan start. */
  @Transient
  @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
  private Date planStart = null;

  /** The plan expiration. */
  @Transient
  @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
  private Date planExpiration = null;

  /**
   * Instantiates an empty {@link User}.
   */
  public User() {
    // n/a
  }

  /**
   * Instantiates a {@link User} from the specified parameters.
   *
   * @param username the username
   * @param password the password
   */
  public User(final String username, final String password) {
    this.username = username;
    this.password = password;
  }

  /**
   * Instantiates a {@link User} from the specified parameters.
   *
   * @param other the other
   */
  public User(final User other) {
    populateFrom(other);
  }

  /* see superclass */
  @Override
  public void populateFrom(final User other) {
    super.populateFrom(other);
    username = other.getUsername();
    name = other.getName();
    plan = other.getPlan();
    planStart = other.getPlanStart();
    planExpiration = other.getPlanExpiration();
    password = other.getPassword();
    oldPassword = other.getOldPassword();
    uriLabel = other.getUriLabel();
    verified = other.getVerified();
    role = other.getRole();
  }

  /* see superclass */
  @Override
  public void patchFrom(final User other) {
    super.patchFrom(other);
    if (other.getUsername() != null) {
      username = other.getUsername();
    }
    if (other.getName() != null) {
      name = other.getName();
    }
    if (other.getPlan() != null) {
      plan = other.getPlan();
    }
    if (other.getPlanExpiration() != null) {
      planExpiration = other.getPlanExpiration();
    }
    if (other.getPlanStart() != null) {
      planStart = other.getPlanStart();
    }
    if (other.getPassword() != null) {
      password = other.getPassword();
    }
    if (other.getUriLabel() != null) {
      uriLabel = other.getUriLabel();
    }
    if (other.getVerified() != null) {
      verified = other.getVerified();
    }

    if (other.getRole() != null) {
      role = other.getRole();
    }
  }

  /**
   * Returns the username.
   *
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the username.
   *
   * @param username the username
   */
  public void setUsername(final String username) {
    this.username = username;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
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
   * Gets the plan.
   *
   * @return the plan
   */
  public String getPlan() {
    return plan;
  }

  /**
   * Sets the plan.
   *
   * @param plan the new plan
   */
  public void setPlan(final String plan) {
    this.plan = plan;
  }

  /**
   * Gets the plan start.
   *
   * @return the plan start
   */
  public Date getPlanStart() {
    return planStart;
  }

  /**
   * Sets the plan start.
   *
   * @param planStart the new plan start
   */
  public void setPlanStart(final Date planStart) {
    this.planStart = planStart;
  }

  /**
   * Gets the plan expiration.
   *
   * @return the plan expiration
   */
  public Date getPlanExpiration() {
    return planExpiration;
  }

  /**
   * Sets the plan expiration.
   *
   * @param planExpiration the new plan expiration
   */
  public void setPlanExpiration(final Date planExpiration) {
    this.planExpiration = planExpiration;
  }

  /**
   * Returns the password.
   *
   * @return the password
   */
  @Schema(description = "User password (removed from payloads returned by the API)")
  public String getPassword() {
    return password;
  }

  /**
   * Sets the password.
   *
   * @param password the password
   */
  public void setPassword(final String password) {
    this.password = password;
  }

  /**
   * Returns the old password.
   *
   * @return the old password
   */
  @Schema(description = "Only used when changing passwords (knowledge of "
      + "previous password is required)")
  public String getOldPassword() {
    return oldPassword;
  }

  /**
   * Sets the old password.
   *
   * @param oldPassword the old password
   */
  public void setOldPassword(final String oldPassword) {
    this.oldPassword = oldPassword;
  }

  /**
   * Returns the uri label.
   *
   * @return the uri label
   */
  public String getUriLabel() {
    return uriLabel;
  }

  /**
   * Sets the uri label.
   *
   * @param uriLabel the uri label
   */
  public void setUriLabel(final String uriLabel) {
    this.uriLabel = uriLabel;
  }

  /**
   * Is verified.
   *
   * @return the boolean
   */
  @Schema(description = "Flag indicating whether the user account has been verified."
      + " If so, the system will send certain email notifications (e.g. usage reports)")
  public Boolean getVerified() {
    return verified;
  }

  /**
   * Sets the verified.
   *
   * @param verified the verified
   */
  public void setVerified(final Boolean verified) {
    this.verified = verified;
  }

  /**
   * Returns the role.
   *
   * @return the role
   */
  public UserRole getRole() {
    return role;
  }

  /**
   * Sets the role.
   *
   * @param role the role
   */
  public void setRole(final UserRole role) {
    this.role = role;
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

  /* see superclass */
  @Override
  public void lazyInit() {
    // n/a
  }

}
