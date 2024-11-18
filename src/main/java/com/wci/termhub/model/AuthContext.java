/*
 * Copyright 2024 West Coast Informatics - All Rights Reserved.
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

/**
 * Generically represents auth information from a JWT.
 */
public class AuthContext extends BaseModel {

	/** The claims. */
	private Map<String, String> claims;

	/** The jwt. */
	private String jwt;

	/** The user id. */
	private String userId;

	/** The project id. */
	private String projectId;

	/** The organization id. */
	private String organizationId;

	/** The correlation id. */
	private String correlationId;

	/** The role. */
	private String role;

	/** The plan - the user's plan. */
	private String plan;

	/** The skip header. */
	private boolean skipHeader;

	/**
	 * Instantiates an empty {@link AuthContext}.
	 */
	public AuthContext() {
		// n/a
	}

	/**
	 * Returns the claims.
	 *
	 * @return the claims
	 */
	public Map<String, String> getClaims() {
		if (claims == null) {
			claims = new HashMap<>();
		}
		return claims;
	}

	/**
	 * Sets the claims.
	 *
	 * @param claims the claims
	 */
	public void setClaims(final Map<String, String> claims) {
		this.claims = claims;
	}

	/**
	 * Returns the jwt.
	 *
	 * @return the jwt
	 */
	public String getJwt() {
		return jwt;
	}

	/**
	 * Sets the jwt.
	 *
	 * @param jwt the jwt
	 */
	public void setJwt(final String jwt) {
		this.jwt = jwt;
	}

	/**
	 * Returns the userid.
	 *
	 * @return the user id
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Sets the user id.
	 *
	 * @param userId the user id
	 */
	public void setUserId(final String userId) {
		this.userId = userId;
	}

	/**
	 * Gets the project id.
	 *
	 * @return the project id
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * Sets the project id.
	 *
	 * @param projectId the new project id
	 */
	public void setProjectId(final String projectId) {
		this.projectId = projectId;
	}

	/**
	 * Gets the organization id.
	 *
	 * @return the organization id
	 */
	public String getOrganizationId() {
		return organizationId;
	}

	/**
	 * Sets the organization id.
	 *
	 * @param organizationId the new organization id
	 */
	public void setOrganizationId(final String organizationId) {
		this.organizationId = organizationId;
	}

	/**
	 * Returns the correlation id.
	 *
	 * @return the correlation id
	 */
	public String getCorrelationId() {
		return correlationId;
	}

	/**
	 * Sets the correlation id.
	 *
	 * @param correlationId the correlation id
	 */
	public void setCorrelationId(final String correlationId) {
		this.correlationId = correlationId;
	}

	/**
	 * Returns the role.
	 *
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Sets the role.
	 *
	 * @param role the role
	 */
	public void setRole(final String role) {
		this.role = role;
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
	 * Checks if is plan expired.
	 *
	 * @return true, if is plan expired
	 */
	public boolean isPlanExpired() {
		return "EXPIRED".equals(plan);
	}

	/**
	 * Indicates whether or not skip header is the case.
	 *
	 * @return <code>true</code> if so, <code>false</code> otherwise
	 */
	public boolean isSkipHeader() {
		return skipHeader;
	}

	/**
	 * Sets the skip header.
	 *
	 * @param skipHeader the skip header
	 */
	public void setSkipHeader(final boolean skipHeader) {
		this.skipHeader = skipHeader;
	}

}
