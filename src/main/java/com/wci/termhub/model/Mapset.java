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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Transient;

/**
 * Represents an explicit collection of mappings from one terminology to
 * another.
 */
@Schema(description = "Represents an explicit collection of mappings from one terminology to another")
@Document(indexName = "mapset-v1")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Mapset extends MapsetRef implements TerminologyComponent, HasAttributes {

	/**
	 * Attribute keys that can be associated with a terminology (and their
	 * descriptions) rendered as terminology attributes when building a data set.
	 */
	public enum Attributes {

		/** The origin terminology. */
		originTerminology("origin-terminology"),

		/** The origin version. */
		originVersion("origin-version"),

		/** The complexity - e.g. "simple" or "complex". */
		complexity("complexity"),

		/** The cardinality - e.g. "1-1", "1-N", "N-1", "N-N". */
		cardinality("cardinality");

		/** The name. */
		private final String property;

		/**
		 * Instantiates a {@link Attributes} from the specified parameters.
		 *
		 * @param property the property
		 */
		private Attributes(final String property) {
			this.property = property;
		}

		/**
		 * Returns the property.
		 *
		 * @return the property
		 */
		public String property() {
			return property;
		}

	}

	/** The terminology. */
	@Field(type = FieldType.Keyword)
	private String terminology;

	/** The index name. */
	@Transient
	@Field(type = FieldType.Keyword)
	private String indexName;

	/** The description. */
	@Field(type = FieldType.Object, enabled = false)
	private String description;

	/** The release date formatted as yyyy-MM-dd. */
	@Transient
	@Field(type = FieldType.Keyword)
	private String releaseDate;

	/** The attributes. */
	@Field(type = FieldType.Object)
	private Map<String, String> attributes;

	/** The statistics, e.g. "mappings", "uniqueFromCodes", "uniqueToCodes". */
	@Field(type = FieldType.Object, enabled = false)
	private Map<String, Integer> statistics;

	/**
	 * Instantiates an empty {@link Mapset}.
	 */
	public Mapset() {
		// n/a
	}

	/**
	 * Instantiates a new mapset.
	 *
	 * @param abbreviation the abbreviation
	 * @param terminology  the terminology
	 * @param publisher    the publisher
	 * @param version      the version
	 * @param releaseDate  the release date
	 * @param latest       the latest
	 */
	public Mapset(final String abbreviation, final String terminology, final String publisher, final String version,
			final String releaseDate, final Boolean latest) {
		setAbbreviation(abbreviation);
		setTerminology(terminology);
		setPublisher(publisher);
		setVersion(version);
		setReleaseDate(releaseDate);
		setLatest(latest);
	}

	/**
	 * Instantiates a {@link Mapset} from the specified parameters.
	 *
	 * @param other the other
	 */
	public Mapset(final Mapset other) {
		populateFrom(other);
	}

	/**
	 * Populate from.
	 *
	 * @param other the other
	 */
	/* see superclass */
	@Override
	public void populateFrom(final TerminologyRef other) {
		super.populateFrom(other);

		if (other instanceof Mapset) {
			final Mapset mapset = (Mapset) other;

			description = mapset.getDescription();
			indexName = mapset.getIndexName();
			releaseDate = mapset.getReleaseDate();

			terminology = mapset.getTerminology();
			attributes = new HashMap<>(mapset.getAttributes());
			statistics = new HashMap<>(mapset.getStatistics());
		}
	}

	/**
	 * Patch from.
	 *
	 * @param other the other
	 */
	/* see superclass */
	@Override
	public void patchFrom(final TerminologyRef other) {
		super.patchFrom(other);
		if (other instanceof Mapset) {
			final Mapset mapset = (Mapset) other;
			if (mapset.getDescription() != null) {
				description = mapset.getDescription();
			}
			if (mapset.getIndexName() != null) {
				indexName = mapset.getIndexName();
			}
			if (mapset.getReleaseDate() != null) {
				releaseDate = mapset.getReleaseDate();
			}
			if (mapset.getTerminology() != null) {
				terminology = mapset.getTerminology();
			}
			if (!mapset.getAttributes().isEmpty()) {
				getAttributes().putAll(mapset.getAttributes());
			}
			if (!mapset.getStatistics().isEmpty()) {
				getStatistics().putAll(mapset.getStatistics());
			}

		}
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	/* see superclass */
	@Override
	@Schema(description = "Unique identifier", required = true, format = "uuid")
	public String getId() {
		return super.getId();
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	@Schema(description = "Description of the mapset")
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Gets the index name.
	 *
	 * @return the index name
	 */
	@Schema(hidden = true)
	public String getIndexName() {
		return indexName;
	}

	/**
	 * Sets the index name.
	 *
	 * @param indexName the new index name
	 */
	public void setIndexName(final String indexName) {
		this.indexName = indexName;
	}

	/**
	 * Returns the release date.
	 *
	 * @return the release date
	 */
	@Schema(description = "YYYY-MM-DD rendering of the release date")
	public String getReleaseDate() {
		return releaseDate;
	}

	/**
	 * Sets the release date.
	 *
	 * @param releaseDate the release date
	 */
	public void setReleaseDate(final String releaseDate) {
		this.releaseDate = releaseDate;
	}

	/**
	 * Gets the attributes.
	 *
	 * @return the attributes
	 */
	/* see superclass */
	@Override
	public Map<String, String> getAttributes() {
		if (attributes == null) {
			attributes = new HashMap<>();
		}
		return attributes;
	}

	/**
	 * Sets the attributes.
	 *
	 * @param attributes the attributes
	 */
	/* see superclass */
	@Override
	public void setAttributes(final Map<String, String> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Gets the statistics.
	 *
	 * @return the statistics
	 */
	public Map<String, Integer> getStatistics() {
		if (statistics == null) {
			statistics = new HashMap<>();
		}
		return statistics;
	}

	/**
	 * Sets the statistics.
	 *
	 * @param statistics the statistics
	 */
	public void setStatistics(final Map<String, Integer> statistics) {
		this.statistics = statistics;
	}

	/**
	 * Gets the terminology.
	 *
	 * @return the terminology
	 */
	/* see superclass */
	@Override
	public String getTerminology() {
		return terminology;
	}

	/**
	 * Sets the terminology.
	 *
	 * @param terminology the new terminology
	 */
	/* see superclass */
	@Override
	public void setTerminology(final String terminology) {
		this.terminology = terminology;
	}

	/**
	 * Minimize.
	 */
	/* see superclass */
	@Override
	public void minimize() {
		super.minimize();
		cleanForApi();
	}

	/**
	 * Clean for api.
	 */
	/* see superclass */
	@Override
	public void cleanForApi() {
		indexName = null;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	/* see superclass */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hash(getId());
		return result;
	}

	/**
	 * Equals.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	/* see superclass */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		final TerminologyRef other = (TerminologyRef) obj;
		return Objects.equals(getId(), other.getId());
	}

	/**
	 * Compare to.
	 *
	 * @param other the other
	 * @return the int
	 */
	/* see superclass */
	@Override
	public int compareTo(final TerminologyRef other) {
		// Sort by abbreviations (ascending)
		// Then by version in correct version order for the abbreviation
		// If RXNORM participates in the map and the version Year isn't 20YY
		final String k1 = getAbbreviation()
				+ (getAbbreviation().contains("RXNORM") && getVersion() != null && !getVersion().startsWith("20")
						? getVersion().replaceFirst("(....)(....)", "$2$1")
						: getVersion());
		final String k2 = other.getAbbreviation()
				+ (other.getAbbreviation().contains("RXNORM-") && getVersion() != null && !getVersion().startsWith("20")
						? other.getVersion().replaceFirst("(....)(....)", "$2$1")
						: other.getVersion());

		return k1.compareTo(k2);
	}
}
