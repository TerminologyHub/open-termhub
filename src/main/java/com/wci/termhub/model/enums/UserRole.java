/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * An enum for application roles.
 */
@Schema(description = "Application role within the system used to control authorization")
public enum UserRole {

  /** The viewer . */
  VIEWER("Viewer"),
  /** The user. */
  USER("User"),
  /** Account role - for password resets and such. */
  ACCOUNT("Account"),
  /** Project role for project api keys. */
  PROJECT("Project"),
  /** The administrator - reserved for WCI admins. */
  ADMIN("Admin");

  /** The value. */
  private String value;

  /**
   * Instantiates a {@link UserRole} from the specified parameters.
   *
   * @param value the value
   */
  private UserRole(final String value) {
    this.value = value;
  }

  /**
   * Returns the value.
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * Checks for privileges of.
   *
   * @param role the role
   * @return true, if successful
   */
  public boolean hasPrivilegesOf(final UserRole role) {
    return ordinal() >= role.ordinal();

  }

}
