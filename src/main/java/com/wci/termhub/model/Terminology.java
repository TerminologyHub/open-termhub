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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Represents a terminology abbreviation and associated metadata.
 */
@Entity
@Table(name = "terminology")
@Schema(description = "Represents a terminology abbreviation and associated metadata")
@Document(indexName = "terminology-v1")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Terminology extends TerminologyRef implements HasAttributes {

	/**
	 * Attribute keys that can be associated with a terminology (and their
	 * descriptions) rendered as terminology attributes when building a data set.
	 */
	public enum Attributes {

		/** Indicates ECL is supported for the terminology. */
		ecl("ecl"),

		/** Indicates autocomplete is supported. */
		autocomplete("autocomplete"),

		/** Indicates tree positions are computed. */
		treePositions("tree-positions"),
		/** Indicates the terminology has a hierarchy. */
		hierarchical("hierarchical"),

		/** The show sty. */
		showSty("show-sty"),
		/**
		 * The hierarchy sort style. TODO: rather than this ConceptTreePosition shoudl
		 * have a sort field.
		 */
		hierarchySortStyle("hierarchy-sort-style"),
		/** The polyhierarchy. */
		polyhierarchy("polyhierarchy"),
		/** The has relationship directionality. */
		hasRelationshipDirectionality("has-relationship-directionality"),
		/** The unidirectional rels. */
		unidirectionalRels("unidirectional-rels"),
		/** The description logic based. */
		descriptionLogicBased("description-logic-based"),
		/** The description logic profile. */
		descriptionLogicProfile("description-logic-profile"),

		// Editing features
		/** The namespace id. */
		namespaceId("namespace-id"),

		/** The id strategy. */
		idStrategy("id-strategy"),

		/** The diagram. */
		diagram("diagram"),

		/** The metathesaurus. */
		metathesaurus("metathesaurus");

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

	// Superclass tracks abbr/version/latest/publisher

	/** The release date formatted as yyyy-MM-dd. */
	@Transient
	@Field(type = FieldType.Keyword)
	private String releaseDate;

	/** The terminology family. */
	@Transient
	@Field(type = FieldType.Keyword)
	private String family;

	/** The uri (for downloading terminology artifacts). */
	@Transient
	private String uri;

	/** The index name. */
	@Transient
	@Field(type = FieldType.Keyword)
	private String indexName;

	/** The attributes. */
	@Transient
	@Field(type = FieldType.Object)
	private Map<String, String> attributes;

	/** The root codes. */
	@Transient
	@Field(type = FieldType.Keyword)
	private List<String> roots;

	// NOTE: the following 3 "ct" variables should eventually be removed
	// Once all terminologies have been reloaded to simply use "statistics"
	/** The concept count. */
	@Transient
	@Field(type = FieldType.Long)
	private Long conceptCt;

	/** The relationship ct. */
	@Transient
	@Field(type = FieldType.Long)
	private Long relationshipCt;

	/** The tree position ct. */
	@Transient
	@Field(type = FieldType.Long)
	private Long treePositionCt;

	/** The statistics. */
	@Transient
	@Field(type = FieldType.Object, enabled = false)
	private Map<String, Integer> statistics;

	/**
	 * Instantiates an empty {@link Terminology}.
	 */
	public Terminology() {
		// n/a
	}

	/**
	 * Instantiates a {@link Terminology} from the specified parameters.
	 *
	 * @param other the other
	 */
	public Terminology(final Terminology other) {
		populateFrom(other);
	}

	/**
	 * Populate from.
	 *
	 * @param ref the ref
	 */
	@Override
	public void populateFrom(final TerminologyRef ref) {
		super.populateFrom(ref);
		if (ref instanceof Terminology) {
			final Terminology other = (Terminology) ref;
			releaseDate = other.getReleaseDate();
			uri = other.getUri();
			family = other.getFamily();
			indexName = other.getIndexName();
			attributes = new HashMap<>(other.getAttributes());
			roots = new ArrayList<>(other.getRoots());
			conceptCt = other.getConceptCt();
			relationshipCt = other.getRelationshipCt();
			treePositionCt = other.getTreePositionCt();
			statistics = new HashMap<>(other.getStatistics());
			roots = new ArrayList<>(other.getRoots());
			uri = other.getUri();
		}

	}

	/**
	 * Patch from.
	 *
	 * @param ref the ref
	 */
	@Override
	public void patchFrom(final TerminologyRef ref) {
		super.patchFrom(ref);
		if (ref instanceof Terminology) {
			final Terminology other = (Terminology) ref;

			if (other.getReleaseDate() != null) {
				releaseDate = other.getReleaseDate();
			}
			if (other.getFamily() != null) {
				family = other.getFamily();
			}
			if (other.getUri() != null) {
				uri = other.getUri();
			}
			if (other.getIndexName() != null) {
				indexName = other.getIndexName();
			}
			if (other.getConceptCt() != null) {
				conceptCt = other.getConceptCt();
			}
			if (other.getRelationshipCt() != null) {
				relationshipCt = other.getRelationshipCt();
			}
			if (other.getTreePositionCt() != null) {
				treePositionCt = other.getTreePositionCt();
			}
			if (!other.getAttributes().isEmpty()) {
				getAttributes().putAll(other.getAttributes());
				// Copy for fresh data structure (no proxies)
				attributes = new HashMap<>(attributes);
			}
			if (!other.getRoots().isEmpty()) {
				getRoots().addAll(other.getRoots());
				// Copy for fresh data structure (no proxies)
				roots = new ArrayList<>(roots);
			}

			if (!other.getStatistics().isEmpty()) {
				getStatistics().putAll(other.getStatistics());
				statistics = new HashMap<>(statistics);
			}

		}
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
		final String k1 = getAbbreviation() + (getAbbreviation().equals("RXNORM") && getVersion() != null
				? getVersion().replaceFirst("(....)(....)", "$2$1")
				: getVersion());
		final String k2 = other.getAbbreviation() + (other.getAbbreviation().equals("RXNORM") && getVersion() != null
				? other.getVersion().replaceFirst("(....)(....)", "$2$1")
				: other.getVersion());

		return k1.compareTo(k2);
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
	 * Here, we want to search a variety of different ways. Including: 1. Search by
	 * whether an attribute exists. 2. Search by whether an attribute has a
	 * particular value.
	 *
	 * @return the attributes
	 */
	@Override
	@Schema(description = "Key/value pairs associated with this object")
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
	 * Gets the roots.
	 *
	 * @return the roots
	 */
	@Schema(description = "Root codes in the hierarchy")
	public List<String> getRoots() {
		if (roots == null) {
			roots = new ArrayList<>();
		}
		return roots;
	}

	/**
	 * Sets the roots.
	 *
	 * @param roots the new roots
	 */
	public void setRoots(final List<String> roots) {
		this.roots = roots;
	}

	// /**
	// * Unmarshall.
	// *
	// * @throws Exception the exception
	// */
	// /* see superclass */
	// @Override
	// @PostLoad
	// public void unmarshall() throws Exception {
	// final Terminology resource = ModelUtility.fromJson(getData(),
	// this.getClass());
	// if (resource != null) {
	// populateFrom(resource);
	// }
	// }

	/**
	 * Gets the family.
	 *
	 * @return the family
	 */
	@Schema(description = "Family of related terminologies this one belongs to")
	public String getFamily() {
		return family;
	}

	/**
	 * Sets the family.
	 *
	 * @param family the family
	 */
	public void setFamily(final String family) {
		this.family = family;
	}

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	@Schema(description = "Uri for downloading the terminology")
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the uri.
	 *
	 * @param uri the new uri
	 */
	public void setUri(final String uri) {
		this.uri = uri;
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
	 * Gets the concept ct.
	 *
	 * @return the concept ct
	 */
	@Schema(description = "Count of number of concepts in the terminology")
	public Long getConceptCt() {
		return conceptCt;
	}

	/**
	 * Sets the concept ct.
	 *
	 * @param conceptCt the new concept ct
	 */
	public void setConceptCt(final Long conceptCt) {
		this.conceptCt = conceptCt;
	}

	/**
	 * Gets the relationship ct.
	 *
	 * @return the relationship ct
	 */
	@Schema(description = "Count of number of concept relationships in the terminology")
	public Long getRelationshipCt() {
		return relationshipCt;
	}

	/**
	 * Sets the relationship ct.
	 *
	 * @param relationshipCt the new relationship ct
	 */
	public void setRelationshipCt(final Long relationshipCt) {
		this.relationshipCt = relationshipCt;
	}

	/**
	 * Gets the tree position ct.
	 *
	 * @return the tree position ct
	 */
	@Schema(description = "Count of number of concept tree positions in the terminology")
	public Long getTreePositionCt() {
		return treePositionCt;
	}

	/**
	 * Sets the tree position ct.
	 *
	 * @param treePositionCt the new tree position ct
	 */
	public void setTreePositionCt(final Long treePositionCt) {
		this.treePositionCt = treePositionCt;
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

}
