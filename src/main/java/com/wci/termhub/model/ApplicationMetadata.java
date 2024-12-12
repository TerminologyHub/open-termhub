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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents application metadata.
 */
@Schema(description = "Container for overall application metadata.")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationMetadata extends BaseModel {

	/** The publisher. */
	private List<PublisherInfo> publisher;

	/** The plan. */
	private List<PlanInfo> plan;

	/** The resource. */
	private List<ResourceInfo> resource;

	/** The licence types. */
	private List<LicenseInfo> license;

	/** The terminology. */
	private List<TerminologyInfo> terminology;

	/** The family. */
	private List<FamilyInfo> family;

	/** The language. */
	private List<LanguageInfo> language;

	/**
	 * Instantiates an empty {@link ApplicationMetadata}.
	 */
	public ApplicationMetadata() {
		// n/a
	}

	/**
	 * Instantiates a {@link ApplicationMetadata} from the specified parameters.
	 *
	 * @param other the other
	 */
	public ApplicationMetadata(final ApplicationMetadata other) {
		populateFrom(other);
	}

	/**
	 * Populate from.
	 *
	 * @param other the other
	 */
	public void populateFrom(final ApplicationMetadata other) {
		// super.populateFrom(other);
		language = new ArrayList<>(other.getLanguage());
		publisher = new ArrayList<>(other.getPublisher());
		plan = new ArrayList<>(other.getPlan());
		resource = new ArrayList<>(other.getResource());
		license = new ArrayList<>(other.getLicense());
		terminology = new ArrayList<>(other.getTerminology());
		family = new ArrayList<>(other.getFamily());
	}

	/**
	 * Returns the publisher.
	 *
	 * @return the publisher
	 */
	@Schema(description = "List of all available publisher")
	public List<PublisherInfo> getPublisher() {
		if (publisher == null) {
			publisher = new ArrayList<>();
		}
		return publisher;
	}

	/**
	 * Sets the publisher.
	 *
	 * @param publisher the publisher
	 */
	public void setPublisher(final List<PublisherInfo> publisher) {
		this.publisher = publisher;
	}

	/**
	 * Gets the plan.
	 *
	 * @return the plan
	 */
	@Schema(description = "List of all available plans")
	public List<PlanInfo> getPlan() {
		if (plan == null) {
			plan = new ArrayList<>();
		}
		return plan;
	}

	/**
	 * Sets the plan.
	 *
	 * @param plan the new plan
	 */
	public void setPlan(final List<PlanInfo> plan) {
		this.plan = plan;
	}

	/**
	 * Gets the resource.
	 *
	 * @return the resource
	 */
	@Schema(description = "List of all available resources")
	public List<ResourceInfo> getResource() {
		if (resource == null) {
			resource = new ArrayList<>();
		}
		return resource;
	}

	/**
	 * Sets the resource.
	 *
	 * @param resource the new resource
	 */
	public void setResource(final List<ResourceInfo> resource) {
		this.resource = resource;
	}

	/**
	 * Returns the license types.
	 *
	 * @return the license types
	 */
	@Schema(description = "List of all available license types")
	public List<LicenseInfo> getLicense() {
		if (license == null) {
			license = new ArrayList<>();
		}
		return license;
	}

	/**
	 * Sets the license.
	 *
	 * @param license the license
	 */
	public void setLicense(final List<LicenseInfo> license) {
		this.license = license;
	}

	/**
	 * Returns the terminology.
	 *
	 * @return the terminology
	 */
	@Schema(description = "List of all available terminology")
	public List<TerminologyInfo> getTerminology() {
		if (terminology == null) {
			terminology = new ArrayList<>();
		}
		return terminology;
	}

	/**
	 * Sets the terminology.
	 *
	 * @param terminology the terminology to set
	 */
	public void setTerminology(final List<TerminologyInfo> terminology) {
		this.terminology = terminology;
	}

	/**
	 * Returns the family.
	 *
	 * @return the family
	 */
	@Schema(description = "List of all available family")
	public List<FamilyInfo> getFamily() {
		if (family == null) {
			family = new ArrayList<>();
		}
		return family;
	}

	/**
	 * Sets the family.
	 *
	 * @param family the family to set
	 */
	public void setFamily(final List<FamilyInfo> family) {
		this.family = family;
	}

	/**
	 * Returns the language.
	 *
	 * @return the language
	 */
	@Schema(description = "List of all available languages")
	public List<LanguageInfo> getLanguage() {
		if (language == null) {
			language = new ArrayList<>();
		}
		return language;
	}

	/**
	 * Sets the language.
	 *
	 * @param language the language to set
	 */
	public void setLanguage(final List<LanguageInfo> language) {
		this.language = language;
	}

}
