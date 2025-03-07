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

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a relationship between two concepts.
 */
@Schema(description = "Represents a relationship between two concepts in a terminology")
@Document(indexName = "concept-relationship-v1")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConceptRelationship extends AbstractHasModified implements TerminologyComponent,
    HasAttributes, Copyable<ConceptRelationship>, Comparable<ConceptRelationship> {

  /** The terminology. */
  @Field(type = FieldType.Keyword)
  private String terminology;

  /** The terminology. */
  @Field(type = FieldType.Keyword)
  private String version;

  /** The terminology. */
  @Field(type = FieldType.Keyword)
  private String publisher;

  /** The component id. */
  @Field(type = FieldType.Keyword)
  private String componentId;

  /** The type. */
  @Field(type = FieldType.Keyword)
  private String type;

  /** The additional type. */
  @Field(type = FieldType.Keyword)
  private String additionalType;

  /** The from. */
  @Field(type = FieldType.Object)
  private ConceptRef from;

  /** The to. */
  @Field(type = FieldType.Object)
  private ConceptRef to;

  /** The to value. */
  @Field(type = FieldType.Keyword)
  private String toValue;

  /** The hierarchical. */
  @Field(type = FieldType.Boolean)
  private Boolean hierarchical;

  /** The historical. */
  @Field(type = FieldType.Boolean)
  private Boolean historical;

  /**
   * The asserted flag. Indicates whether this relationship is in the asserted
   * asserted (for terminologies that assert directions)
   */
  @Field(type = FieldType.Boolean)
  private Boolean asserted;

  /**
   * The defining flag. Indicates whether this relationship is part of the
   * logical definition.
   */
  @Field(type = FieldType.Boolean)
  private Boolean defining;

  /** The group. */
  @Field(type = FieldType.Keyword)
  private String group;

  /** The attributes. */
  @Field(type = FieldType.Object)
  private Map<String, String> attributes;

  /**
   * Instantiates an empty {@link ConceptRelationship}.
   */
  public ConceptRelationship() {
    // n/a
  }

  /**
   * Instantiates a {@link ConceptRelationship} from the specified parameters.
   *
   * @param other the other
   */
  public ConceptRelationship(final ConceptRelationship other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  @Override
  public void populateFrom(final ConceptRelationship other) {
    super.populateFrom(other);
    type = other.getType();
    additionalType = other.getAdditionalType();
    terminology = other.getTerminology();
    version = other.getVersion();
    publisher = other.getPublisher();
    componentId = other.getComponentId();
    from = other.getFrom();
    to = other.getTo();
    toValue = other.getToValue();
    group = other.getGroup();
    hierarchical = other.getHierarchical();
    historical = other.getHistorical();
    asserted = other.getAsserted();
    defining = other.getDefining();
    attributes = new HashMap<>(other.getAttributes());
  }

  /* see superclass */
  @Override
  public void patchFrom(final ConceptRelationship other) {
    super.patchFrom(other);
    if (other.getType() != null) {
      type = other.getType();
    }
    if (other.getAdditionalType() != null) {
      additionalType = other.getAdditionalType();
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
    if (other.getFrom() != null) {
      from.patchFrom(other.getFrom());
    }
    if (other.getTo() != null) {
      to.patchFrom(other.getTo());
    }
    if (other.getToValue() != null) {
      toValue = other.getToValue();
    }
    group = other.getGroup();
    if (other.getHierarchical() != null) {
      hierarchical = other.getHierarchical();
    }
    if (other.getHistorical() != null) {
      historical = other.getHistorical();
    }
    asserted = other.getAsserted();
    defining = other.getDefining();

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
  public int compareTo(final ConceptRelationship other) {
    return getType().compareTo(other.getType());
  }

  /**
   * Returns the group.
   *
   * @return the group
   */
  @Schema(description = "Represents a value used to group relationships whose "
      + "semantics are bound together.")
  public String getGroup() {
    return group;
  }

  /**
   * Sets the group.
   *
   * @param group the group
   */
  public void setGroup(final String group) {
    this.group = group;
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

  /* see superclass */
  @Override
  public String getVersion() {
    return version;
  }

  /* see superclass */
  @Override
  public void setVersion(final String version) {
    this.version = version;
  }

  /* see superclass */
  @Override
  public String getPublisher() {
    return publisher;
  }

  /* see superclass */
  @Override
  public void setPublisher(final String publisher) {
    this.publisher = publisher;
  }

  /**
   * Returns the terminology id.
   *
   * @return the terminology id
   */
  @Schema(description = "Identifier for this object in the published source terminology")
  public String getComponentId() {
    return componentId;
  }

  /**
   * Sets the terminology id.
   *
   * @param componentId the component id
   */
  public void setComponentId(final String componentId) {
    this.componentId = componentId;
  }

  /**
   * Returns the type.
   *
   * @return the type
   */
  @Schema(description = "Type of relationship in a broad sense, "
      + "e.g. \"other\", \"broader\", \"narrower\".  Inspired by "
      + "the REL field in the UMLS MRREL.RRF file, this field will "
      + "typically have a Termhub-computed value for the high-level " + "relationship type")
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
   * Returns the additional type.
   *
   * @return the additional type
   */
  @Schema(description = "Type of relationship in a more specific sense, "
      + "e.g. \"member_of\", \"has_finding_site\", \"363698007\".  Typically this field"
      + "will be used for the primary relationship type asserted in the" + "published source data")
  public String getAdditionalType() {
    return additionalType;
  }

  /**
   * Sets the additional type.
   *
   * @param additionalType the additional type
   */
  public void setAdditionalType(final String additionalType) {
    this.additionalType = additionalType;
  }

  /**
   * Returns the from.
   *
   * @return the from
   */
  @Schema(description = "Concept on this side of the relationship")
  public ConceptRef getFrom() {
    return from;
  }

  /**
   * Sets the from.
   *
   * @param from the from
   */
  public void setFrom(final ConceptRef from) {
    this.from = from;
  }

  /**
   * Returns the to.
   *
   * @return the to
   */
  @Schema(description = "Concept on the other side of the relationship "
      + "(either \"to\" or \"toValue\" will be used but not both)")
  public ConceptRef getTo() {
    return to;
  }

  /**
   * Sets the to.
   *
   * @param to the to
   */
  public void setTo(final ConceptRef to) {
    this.to = to;
  }

  /**
   * Returns the to value.
   *
   * @return the to value
   */
  @Schema(description = "Literal value on the other side of the relationship "
      + "(either \"to\" or \"toValue\" will be used but not both)")
  public String getToValue() {
    return toValue;
  }

  /**
   * Sets the to value.
   *
   * @param toValue the to value
   */
  public void setToValue(final String toValue) {
    this.toValue = toValue;
  }

  /**
   * Indicates whether or not hierarchical is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Schema(description = "Indicates whether this relationship represents a "
      + "hierarchical or parent/child type of connection")
  public Boolean getHierarchical() {
    return hierarchical;
  }

  /**
   * Sets the hierarchical.
   *
   * @param hierarchical the hierarchical
   */
  public void setHierarchical(final Boolean hierarchical) {
    this.hierarchical = hierarchical;
  }

  /**
   * Indicates whether or not historical is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Schema(description = "Indicates whether this relationship represents a "
      + "historical type of connection from a retired concept to an active concept")
  public Boolean getHistorical() {
    return historical;
  }

  /**
   * Sets the historical.
   *
   * @param historical the historical
   */
  public void setHistorical(final Boolean historical) {
    this.historical = historical;
  }

  /**
   * Indicates whether or not asserted direction is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Schema(
      description = "Indicates whether or not this relationship is in the direction asserted by the terminology.")
  public Boolean getAsserted() {
    return asserted;
  }

  /**
   * Sets the asserted direction.
   *
   * @param asserted the new asserted
   */
  public void setAsserted(final Boolean asserted) {
    this.asserted = asserted;
  }

  /**
   * Indicates whether or not defining is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Schema(description = "Indicates whether or not this relationship is part of the logical "
      + "definition of the from concept")
  public Boolean getDefining() {
    return defining;
  }

  /**
   * Sets the defining.
   *
   * @param defining the defining
   */
  public void setDefining(final Boolean defining) {
    this.defining = defining;
  }

}
