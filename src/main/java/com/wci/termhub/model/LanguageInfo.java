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

import org.springframework.data.elasticsearch.annotations.Document;
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
 * Represents language info.
 */
@Entity
@Table(name = "language_info")
@Schema(description = "Represents language info.")
@Document(indexName = "language-info-v1")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LanguageInfo extends AbstractHasModified implements HasLazyInit, Copyable<LanguageInfo> {

	/** The type. e.g. "en", "en_US", "en_GB" */
	@Column(nullable = false)
	@Field(type = FieldType.Keyword)
	private String type;

	/** The human readable name of the language type. */
	@Transient
	@MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
			@InnerField(suffix = "keyword", type = FieldType.Keyword) })
	private String name;

	/** The iso2Code. */
	@Column(nullable = false)
	@Field(type = FieldType.Keyword)
	private String iso2Code;

	/** The iso3Code. */
	@Transient
	@Field(type = FieldType.Keyword)
	private String iso3Code;

	/** The isoCountryCode. */
	@Transient
	@Field(type = FieldType.Keyword)
	private String isoCountryCode;

	/** The uri pointing to information about the license. */
	@Transient
	@Field(type = FieldType.Keyword)
	private String country;

	/**
	 * Instantiates an empty {@link LanguageInfo}.
	 */
	public LanguageInfo() {
		// n/a
	}

	/**
	 * Instantiates a {@link LanguageInfo} from the specified parameters.
	 *
	 * @param other the other
	 */
	public LanguageInfo(final LanguageInfo other) {
		populateFrom(other);
	}

	/**
	 * Populate from.
	 *
	 * @param other the other
	 */
	@Override
	public void populateFrom(final LanguageInfo other) {
		super.populateFrom(other);
		type = other.getType();
		name = other.getName();
		iso2Code = other.getIso2Code();
		iso3Code = other.getIso3Code();
		isoCountryCode = other.getIsoCountryCode();
		country = other.getCountry();
	}

	/* see superclass */
	@Override
	public void patchFrom(final LanguageInfo other) {
		super.patchFrom(other);
		if (other.getType() != null) {
			type = other.getType();
		}
		if (other.getName() != null) {
			name = other.getName();
		}
		if (other.getIso2Code() != null) {
			iso2Code = other.getIso2Code();
		}
		if (other.getIso3Code() != null) {
			iso3Code = other.getIso3Code();
		}
		if (other.getIsoCountryCode() != null) {
			isoCountryCode = other.getIsoCountryCode();
		}
		if (other.getCountry() != null) {
			country = other.getCountry();
		}
	}

//	/* see superclass */
//	@Override
//	public void unmarshall() throws Exception {
//		final LanguageInfo li = ModelUtility.fromJson(getData(), this.getClass());
//		populateFrom(li);
//	}

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
	 * Returns the iso2Code.
	 *
	 * @return the iso2Code
	 */
	public String getIso2Code() {
		return iso2Code;
	}

	/**
	 * Sets the iso2Code.
	 *
	 * @param iso2Code the iso2Code
	 */
	public void setIso2Code(final String iso2Code) {
		this.iso2Code = iso2Code;
	}

	/**
	 * Returns the iso3Code.
	 *
	 * @return the iso3Code
	 */
	public String getIso3Code() {
		return iso3Code;
	}

	/**
	 * Sets the iso3Code.
	 *
	 * @param iso3Code the iso3Code
	 */
	public void setIso3Code(final String iso3Code) {
		this.iso3Code = iso3Code;
	}

	/**
	 * Returns the isoCountryCode.
	 *
	 * @return the isoCountryCode
	 */
	public String getIsoCountryCode() {
		return isoCountryCode;
	}

	/**
	 * Sets the isoCountryCode.
	 *
	 * @param isoCountryCode the isoCountryCode
	 */
	public void setIsoCountryCode(final String isoCountryCode) {
		this.isoCountryCode = isoCountryCode;
	}

	/**
	 * Returns the info uri.
	 *
	 * @return the info uri
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * Sets the info uri.
	 *
	 * @param country the info uri
	 */
	public void setCountry(final String country) {
		this.country = country;
	}

}
