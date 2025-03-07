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

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents an authorization request.
 */
@Schema(description = "Container for authorization information")
public class AuthRequest extends BaseModel {

  /** The grant type. */
  private String grantType;

  /** The username. */
  private String username;

  /** The password. */
  private String password;

  /** The token. */
  private String token;

  /** The refresh token. */
  private String refreshToken;

  /**
   * Instantiates an empty {@link AuthRequest}.
   */
  public AuthRequest() {
    // n/a
  }

  /**
   * Instantiates a {@link AuthRequest} from the specified parameters.
   *
   * @param grantType the grant type
   * @param username the username
   * @param password the password
   */
  public AuthRequest(final String grantType, final String username, final String password) {
    this.grantType = grantType;
    this.username = username;
    this.password = password;
  }

  /**
   * Instantiates a {@link AuthRequest} from the specified parameters.
   *
   * @param grantType the grant type
   * @param refreshToken the refresh token
   */
  public AuthRequest(final String grantType, final String refreshToken) {
    this.grantType = grantType;
    this.refreshToken = refreshToken;
  }

  /**
   * Instantiates a {@link AuthRequest} from the specified parameters.
   *
   * @param other the other
   */
  public AuthRequest(final AuthRequest other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  public void populateFrom(final AuthRequest other) {
    grantType = other.getGrantType();
    username = other.getUsername();
    password = other.getPassword();
    token = other.getToken();
    refreshToken = other.getRefreshToken();
  }

  /**
   * Returns the grant type.
   *
   * @return the grant type
   */
  @JsonProperty("grant_type")
  @Schema(name = "grant_type", required = true, allowableValues = {
      "username_password", "password", "authorization_code"
  })
  public String getGrantType() {
    return grantType;
  }

  /**
   * Sets the grant type.
   *
   * @param grantType the grant type
   */
  public void setGrantType(final String grantType) {
    this.grantType = grantType;
  }

  /**
   * Returns the username.
   *
   * @return the username
   */
  // @Schema(required = true)
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
   * Returns the password.
   *
   * @return the password
   */
  // @Schema(required = true)
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
   * Returns the token.
   *
   * @return the token
   */
  public String getToken() {
    return token;
  }

  /**
   * Sets the token.
   *
   * @param token the token
   */
  public void setToken(final String token) {
    this.token = token;
  }

  /**
   * Returns the refresh token.
   *
   * @return the refresh token
   */
  @JsonProperty("refresh_token")
  @Schema(name = "refresh_token")
  public String getRefreshToken() {
    return refreshToken;
  }

  /**
   * Sets the refresh token.
   *
   * @param refreshToken the refresh token
   */
  public void setRefreshToken(final String refreshToken) {
    this.refreshToken = refreshToken;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((grantType == null) ? 0 : grantType.hashCode());
    result = prime * result + ((password == null) ? 0 : password.hashCode());
    result = prime * result + ((username == null) ? 0 : username.hashCode());
    result = prime * result + ((token == null) ? 0 : token.hashCode());
    result = prime * result + ((refreshToken == null) ? 0 : refreshToken.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }
    final AuthRequest other = (AuthRequest) obj;
    if (grantType == null) {
      if (other.grantType != null) {
        return false;
      }
    } else if (!grantType.equals(other.grantType)) {
      return false;
    }
    if (password == null) {
      if (other.password != null) {
        return false;
      }
    } else if (!password.equals(other.password)) {
      return false;
    }
    if (username == null) {
      if (other.username != null) {
        return false;
      }
    } else if (!username.equals(other.username)) {
      return false;
    }
    if (token == null) {
      if (other.token != null) {
        return false;
      }
    } else if (!token.equals(other.token)) {
      return false;
    }
    if (refreshToken == null) {
      if (other.refreshToken != null) {
        return false;
      }
    } else if (!refreshToken.equals(other.refreshToken)) {
      return false;
    }
    return true;
  }

}
