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
 * Represents an item for the "resources" sidebar.
 */
@Entity
@Table(name = "resource_info")
@Schema(description = "Represents information about a resource like a video, web page, or other information")
@Document(indexName = "resource-info-v1")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceInfo extends AbstractHasModified implements Copyable<ResourceInfo> {

	/** The type. */
	@Column(nullable = false, length = 256)
	@Field(type = FieldType.Keyword)
	private String type;

	/** The icon: video, link, email, "". */
	@Transient
	@Field(type = FieldType.Keyword)
	private String icon;

	/** The title. */
	@Transient
	@MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
			@InnerField(suffix = "keyword", type = FieldType.Keyword) })
	private String title;

	/** The link. */
	@Transient
	@Field(type = FieldType.Keyword)
	private String link;

	/** The description. */
	@Transient
	@Field(type = FieldType.Text)
	private String description;

	/**
	 * Instantiates an empty {@link ResourceInfo}.
	 */
	public ResourceInfo() {
		// n/a
	}

	/**
	 * Instantiates a {@link ResourceInfo} from the specified parameters.
	 *
	 * @param other the other
	 */
	public ResourceInfo(final ResourceInfo other) {
		populateFrom(other);
	}

	/**
	 * Populate from.
	 *
	 * @param other the other
	 */
	@Override
	public void populateFrom(final ResourceInfo other) {
		super.populateFrom(other);
		type = other.getType();
		icon = other.getIcon();
		title = other.getTitle();
		link = other.getLink();
		description = other.getDescription();
	}

	/**
	 * Patch from.
	 *
	 * @param other the other
	 */
	@Override
	public void patchFrom(final ResourceInfo other) {
		super.patchFrom(other);
		if (other.getType() != null) {
			type = other.getType();
		}
		if (other.getIcon() != null) {
			icon = other.getIcon();
		}
		if (other.getTitle() != null) {
			title = other.getTitle();
		}
		if (other.getLink() != null) {
			link = other.getLink();
		}
		if (other.getDescription() != null) {
			description = other.getDescription();
		}

	}

//	/* see superclass */
//	@Override
//	@PostLoad
//	public void unmarshall() throws Exception {
//		final ResourceInfo resource = ModelUtility.fromJson(getData(), this.getClass());
//		if (resource != null) {
//			populateFrom(resource);
//		}
//	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the icon.
	 *
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * Sets the icon.
	 *
	 * @param icon the new icon
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets the link.
	 *
	 * @return the link
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Sets the link.
	 *
	 * @param link the new link
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
