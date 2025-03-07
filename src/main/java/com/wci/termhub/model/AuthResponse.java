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
@Schema(description = "Response to successful authentication")
public class AuthResponse extends BaseModel {

  /** The access token. */
  private String accessToken;

  /** The refresh token. */
  private String refreshToken;

  /** The expires in. */
  private long expiresIn;

  /** The expires on. */
  private long expiresOn;

  /** The token type. */
  private String tokenType;

  /**
   * Instantiates an empty {@link AuthResponse}.
   */
  public AuthResponse() {
    // n/a
  }

  /**
   * Instantiates a {@link AuthResponse} from the specified parameters.
   *
   * @param accessToken the access token
   * @param expiresIn the expires in
   * @param tokenType the token type
   */
  public AuthResponse(final String accessToken, final long expiresIn, final String tokenType) {
    this.accessToken = accessToken;
    this.expiresIn = expiresIn;
    this.tokenType = tokenType;
  }

  /**
   * Instantiates a {@link AuthResponse} from the specified parameters.
   *
   * @param other the other
   */
  public AuthResponse(final AuthResponse other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  public void populateFrom(final AuthResponse other) {
    accessToken = other.getAccessToken();
    refreshToken = other.getRefreshToken();
    expiresIn = other.getExpiresIn();
    expiresOn = other.getExpiresOn();
    tokenType = other.getTokenType();
  }

  /**
   * Returns the access token.
   *
   * @return the access token
   */
  @JsonProperty("access_token")
  @Schema(name = "access_token", required = true)
  public String getAccessToken() {
    return accessToken;
  }

  /**
   * Sets the access token.
   *
   * @param accessToken the access token
   */
  public void setAccessToken(final String accessToken) {
    this.accessToken = accessToken;
  }

  /**
   * Returns the refresh token.
   *
   * @return the refresh token
   */
  @JsonProperty("refresh_token")
  @Schema(name = "refresh_token", required = true)
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

  /**
   * Returns the expires in.
   *
   * @return the expires in
   */
  @JsonProperty("expires_in")
  @Schema(name = "expires_in", required = true)
  public long getExpiresIn() {
    return expiresIn;
  }

  /**
   * Sets the expires in.
   *
   * @param expiresIn the expires in
   */
  public void setExpiresIn(final long expiresIn) {
    this.expiresIn = expiresIn;
  }

  /**
   * Returns the expires on.
   *
   * @return the expires on
   */
  @JsonProperty("expires_on")
  @Schema(name = "expires_on", required = true)
  public long getExpiresOn() {
    return expiresOn;
  }

  /**
   * Sets the expires on.
   *
   * @param expiresOn the expires on
   */
  public void setExpiresOn(final long expiresOn) {
    this.expiresOn = expiresOn;
  }

  /**
   * Returns the token type.
   *
   * @return the token type
   */
  @JsonProperty("token_type")
  @Schema(name = "token_type", required = true)
  public String getTokenType() {
    return tokenType;
  }

  /**
   * Sets the token type.
   *
   * @param tokenType the token type
   */
  public void setTokenType(final String tokenType) {
    this.tokenType = tokenType;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((refreshToken == null) ? 0 : refreshToken.hashCode());
    result = prime * result + ((accessToken == null) ? 0 : accessToken.hashCode());
    result = prime * result + (int) (expiresIn ^ (expiresIn >>> 32));
    result = prime * result + ((tokenType == null) ? 0 : tokenType.hashCode());
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
    final AuthResponse other = (AuthResponse) obj;
    if (accessToken == null) {
      if (other.accessToken != null) {
        return false;
      }
    } else if (!accessToken.equals(other.accessToken)) {
      return false;
    }
    if (refreshToken == null) {
      if (other.refreshToken != null) {
        return false;
      }
    } else if (!refreshToken.equals(other.refreshToken)) {
      return false;
    }
    if (expiresIn != other.expiresIn) {
      return false;
    }
    if (tokenType == null) {
      if (other.tokenType != null) {
        return false;
      }
    } else if (!tokenType.equals(other.tokenType)) {
      return false;
    }
    return true;
  }

}
