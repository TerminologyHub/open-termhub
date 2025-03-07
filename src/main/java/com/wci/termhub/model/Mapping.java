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

/**
 * Represents a mapping between a code in one terminology and code in another
 * terminology.
 */
@Schema(
    description = "Represents a mapping between a code in one terminology and code in another terminology")
@Document(indexName = "mapping-v1")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Mapping extends AbstractTerminologyComponent
    implements HasAttributes, Comparable<Mapping>, Copyable<Mapping> {

  /** The component id for this term. */
  @Field(type = FieldType.Keyword)
  private String componentId;

  /** The mapset this belongs to. */
  @Field(type = FieldType.Object)
  private MapsetRef mapset;

  /** The from. */
  @Field(type = FieldType.Object)
  private ConceptRef from;

  /** The to. */
  @Field(type = FieldType.Object)
  private ConceptRef to;

  /** The map group. */
  @Field(type = FieldType.Keyword)
  private String group;

  /** The map priority. */
  @Field(type = FieldType.Keyword)
  private String priority;

  /** The rule. */
  @Field(type = FieldType.Keyword)
  private String rule;

  /** The advice. */
  @Field(type = FieldType.Keyword)
  private List<String> advice;

  /**
   * The type (or "category" or "relationship" - e.g. "equivalent", "broader",
   * etc).
   */
  @Field(type = FieldType.Keyword)
  private String type;

  /** The attributes. */
  @Field(type = FieldType.Object)
  private Map<String, String> attributes;

  // /** The workflow status. */
  // private WorkflowStatus workflowStatus = WorkflowStatus.NEW;

  /**
   * Instantiates an empty {@link Mapping}.
   */
  public Mapping() {
    // n/a
  }

  /**
   * Instantiates a {@link Mapping} from the specified parameters.
   *
   * @param other the other
   */
  public Mapping(final Mapping other) {
    populateFrom(other);
  }

  /* see superclass */
  @Override
  public void populateFrom(final Mapping other) {
    super.populateFrom(other);

    attributes = new HashMap<>(other.getAttributes());
    advice = new ArrayList<>(other.getAdvice());
    from = other.getFrom();
    to = other.getTo();
    group = other.getGroup();
    componentId = other.getComponentId();
    mapset = other.getMapset();
    priority = other.getPriority();
    rule = other.getRule();
    type = other.getType();
  }

  /* see superclass */
  @Override
  public void patchFrom(final Mapping other) {
    super.patchFrom(other);
    if (!other.getAttributes().isEmpty()) {
      getAttributes().putAll(other.getAttributes());
    }
    if (!other.getAdvice().isEmpty()) {
      getAdvice().addAll(other.getAdvice());
    }
    if (other.getFrom() != null) {
      from = other.getFrom();
    }
    if (other.getTo() != null) {
      to = other.getTo();
    }
    if (other.getGroup() != null) {
      group = other.getGroup();
    }
    if (other.getComponentId() != null) {
      componentId = other.getComponentId();
    }
    if (other.getMapset() != null) {
      mapset = other.getMapset();
    }
    if (other.getPriority() != null) {
      priority = other.getPriority();
    }
    if (other.getRule() != null) {
      rule = other.getRule();
    }
    if (other.getType() != null) {
      type = other.getType();
    }

  }

  /* see superclass */
  @Override
  public int compareTo(final Mapping other) {
    final String k1 = getFrom().getCode() + getGroup() + getPriority();
    final String k2 = other.getFrom().getCode() + other.getGroup() + other.getPriority();
    return k1.compareTo(k2);
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
   * Gets the mapset.
   *
   * @return the mapset
   */
  public MapsetRef getMapset() {
    return mapset;
  }

  /**
   * Sets the mapset.
   *
   * @param mapset the new mapset
   */
  public void setMapset(final MapsetRef mapset) {
    this.mapset = mapset;
  }

  /**
   * Gets the from.
   *
   * @return the from
   */
  @Schema(description = "Reference to concept this map is \"from\" (the source concept)")
  public ConceptRef getFrom() {
    return from;
  }

  /**
   * Sets the from.
   *
   * @param from the new from
   */
  public void setFrom(final ConceptRef from) {
    this.from = from;
  }

  /**
   * Gets the to.
   *
   * @return the to
   */
  @Schema(description = "Reference to concept this map is \"to\" "
      + "(the destination or target concept)")
  public ConceptRef getTo() {
    return to;
  }

  /**
   * Sets the to.
   *
   * @param to the new to
   */
  public void setTo(final ConceptRef to) {
    this.to = to;
  }

  /**
   * Gets the group.
   *
   * @return the group
   */
  @Schema(description = "Grouping mechanism for a set of maps")
  public String getGroup() {
    return group;
  }

  /**
   * Sets the group.
   *
   * @param group the new group
   */
  public void setGroup(final String group) {
    this.group = group;
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
   * Gets the priority.
   *
   * @return the priority
   */
  @Schema(description = "Priority indicator within a map group")
  public String getPriority() {
    return priority;
  }

  /**
   * Sets the priority.
   *
   * @param priority the new priority
   */
  public void setPriority(final String priority) {
    this.priority = priority;
  }

  /**
   * Gets the rule.
   *
   * @return the rule
   */
  @Schema(description = "Machine-readable rule for when the map should be applied")
  public String getRule() {
    return rule;
  }

  /**
   * Sets the rule.
   *
   * @param rule the new rule
   */
  public void setRule(final String rule) {
    this.rule = rule;
  }

  /**
   * Gets the advice.
   *
   * @return the advice
   */
  @Schema(description = "Human-readable advice for how to use the map")
  public List<String> getAdvice() {
    if (advice == null) {
      advice = new ArrayList<>();
    }
    return advice;
  }

  /**
   * Sets the advice.
   *
   * @param advice the new advice
   */
  public void setAdvice(final List<String> advice) {
    this.advice = advice;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  @Schema(description = "Type of mapping between the \"from\" and \"to\" concepts. "
      + "Often this is something like \"equivalent\", \"broader\", or \"narrower\"")
  public String getType() {
    return type;
  }

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(final String type) {
    this.type = type;
  }

}
