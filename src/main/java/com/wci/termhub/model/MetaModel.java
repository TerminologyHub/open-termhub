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

/**
 * Meta model for terminology objects and attributes. For defining metadata for controlling loader
 * behavior, and other use cases.
 */
public final class MetaModel {

  /**
   * An enumeration of model objects.
   */
  public enum Model {

    /** The concept. */
    concept,
    /** The term . */
    term,
    /** The index terms . */
    indexTerm,
    /** The definition. */
    definition,
    /** The relationship. */
    relationship,
    /** The treeposition. */
    treePosition,
    /** The mapping. */
    mapping,
    /** The subset. */
    subset,
    /** The mapset. */
    mapset,
    /** The semantic type. */
    semanticType,
    /** The subsetMember. */
    subsetMember,
    /** The axiom. */
    axiom,

    /** inactive concepts. */
    inactiveConcept,

    /** The inactive term. */
    inactiveTerm

  }

  /**
   * The Enum Field.
   */
  public enum Field {

    /** The language. */
    language,
    /** The type. */
    type,
    /** The additional type. */
    additionalType,
    /** The (attribute) name. */
    attribute,
    /** The semantic type. */
    semanticType,
    /** The precedence. */
    precedence,
    /** The category (for subsets). */
    category,
    /** The entity type (for subsets). */
    entityType,
    /** The UI label for this element. */
    uiLabel,
    /** The other. */
    other;

  }

  /**
   * The core relationship "type" values to use for non-RRF terminologies.
   */
  public enum RelationshipType {

    /** The child. */
    child("From child to parent"),

    /** The parent. */
    parent("From parent to child"),

    /** The broader. */
    broader("From broader to narrower"),

    /** The narrower. */
    narrower("From narrower to broader"),

    /** The other. */
    other("otherwise related");

    /** The description. */
    private String description;

    /**
     * Instantiates a new relationship type.
     *
     * @param description the description
     */
    private RelationshipType(final String description) {
      this.description = description;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
      return description;
    }
  }

}
