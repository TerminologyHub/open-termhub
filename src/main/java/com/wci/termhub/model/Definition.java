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

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a textual definition of a concept.
 */
@Schema(description = "Represents a textual definition of concept in a terminology")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Definition extends AbstractHasModified
		implements TerminologyComponent, HasAttributes, Copyable<Definition>, Comparable<Definition> {

	/** The definition (no need for keyword indexing). */
	@Field(type = FieldType.Text)
	private String definition;

	// /** The word ct. */
	// @JsonProperty(access = Access.READ_ONLY)
	// @Field(type = FieldType.Integer)
	// private Integer wordCt;

	/** The terminology. */
	@Field(type = FieldType.Keyword)
	private String terminology;

	/** The version. */
	@Field(type = FieldType.Keyword)
	private String version;

	/** The publisher. */
	@Field(type = FieldType.Keyword)
	private String publisher;

	/** The component id for this term. */
	@Field(type = FieldType.Keyword)
	private String componentId;

	/** The locale. */
	@Field(type = FieldType.Object)
	private Map<String, Boolean> localeMap;

	/** The attributes. */
	@Field(type = FieldType.Object)
	private Map<String, String> attributes;

	/**
	 * Instantiates an empty {@link Definition}.
	 */
	public Definition() {
		// n/a
	}

	/**
	 * Instantiates a {@link Definition} from the specified parameters.
	 *
	 * @param other the other
	 */
	public Definition(final Definition other) {
		populateFrom(other);
	}

	/**
	 * Populate from.
	 *
	 * @param other the other
	 */
	@Override
	public void populateFrom(final Definition other) {
		super.populateFrom(other);
		definition = other.getDefinition();
		terminology = other.getTerminology();
		version = other.getVersion();
		publisher = other.getPublisher();
		componentId = other.getComponentId();
		localeMap = new HashMap<>(other.getLocaleMap());
		attributes = new HashMap<>(other.getAttributes());
	}

	/* see superclass */
	@Override
	public void patchFrom(final Definition other) {
		super.patchFrom(other);
		if (other.getDefinition() != null) {
			definition = other.getDefinition();
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
		if (other.getComponentId() != null) {
			componentId = other.getComponentId();
		}
		if (!other.getLocaleMap().isEmpty()) {
			localeMap.putAll(other.getLocaleMap());
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
	@Override
	public int compareTo(final Definition other) {
		return getDefinition().compareTo(other.getDefinition());
	}

	/**
	 * Gets the definition.
	 *
	 * @return the definition
	 */
	@Schema(description = "Text of the definition")
	public String getDefinition() {
		return definition;
	}

	/**
	 * Sets the definition.
	 *
	 * @param definition the new definition
	 */
	public void setDefinition(final String definition) {
		this.definition = definition;

		// also compute "wordCt"
		// wordCt = name == null ? 0 : StringUtility.wordind(name).size();
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
	 * Returns the terminology.
	 *
	 * @return the terminology
	 */
	@Override
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
	 * Returns the component id.
	 *
	 * @return the component id
	 */
	@Schema(description = "Identifier for this object in the published source terminology")
	public String getComponentId() {
		return componentId;
	}

	/**
	 * Sets the component id.
	 *
	 * @param componentId the component id
	 */
	public void setComponentId(final String componentId) {
		this.componentId = componentId;
	}

	/**
	 * Returns the locale map.
	 *
	 * @return the locale map
	 */
	@Schema(description = "Map of language (optionally with locale) to true/false indicating "
			+ "whether this definition is the preferred definition in that language or not.  An entry"
			+ "with true indicates that it is preferred in that language. An entry with "
			+ "false indicates that it is valid for that language but not preferred.")
	public Map<String, Boolean> getLocaleMap() {
		if (localeMap == null) {
			this.localeMap = new HashMap<>();
		}
		return localeMap;
	}

	/**
	 * Sets the locale map.
	 *
	 * @param localeMap the locale map
	 */
	public void setLocaleMap(final Map<String, Boolean> localeMap) {
		this.localeMap = localeMap;
	}

	/**
	 * Contains language.
	 *
	 * @param language the language
	 * @return true, if successful
	 */
	public boolean containsLanguage(final String language) {
		return localeMap.entrySet().stream().anyMatch(e -> e.getKey().startsWith(language));
	}

}
