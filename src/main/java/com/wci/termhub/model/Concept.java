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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wci.termhub.util.StringUtility;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.persistence.Transient;

/**
 * Jpa-enabled implementation of {@link Concept}.
 */
@Schema(description = "Represents a concept in a terminology")
@Document(indexName = "concept-v1")
// Needed because of term ngram index (due to embedded indexing)
@Setting(settingPath = "es-config/autocomplete-analyzer.json")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Concept extends ConceptRef implements HasAttributes, HasMinimize, HasHighlights {

  /** The norm name. */
  @JsonProperty(access = Access.READ_ONLY)
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword)
  })
  private String normName;

  /** The stem name. */
  @JsonProperty(access = Access.READ_ONLY)
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword)
  })
  private String stemName;

  /**
   * The terms. (aka atoms, descriptions, names, synonyms, etc). Ignore the ngram indexed field
   * here.
   */
  @Field(type = FieldType.Object, ignoreFields = {
      "name.ngram"
  })
  private List<Term> terms;

  /** The index terms. */
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword)
  })
  private List<String> indexTerms;

  /** The definitions. */
  private List<Definition> definitions;

  /** The axioms. */
  private List<Axiom> axioms;

  /** The ECL index clauses. */
  @Field(name = "ecl", type = FieldType.Keyword)
  private List<String> eclClauses;

  /** The attributes. */
  @Field(type = FieldType.Object)
  private Map<String, String> attributes;

  /** The highlights. */
  @Transient
  @JsonSerialize
  @JsonDeserialize
  private Map<String, String> highlights;

  /** The semantic types. */
  @Field(type = FieldType.Keyword)
  private Set<String> semanticTypes;

  /** labels to support searching. */
  @Transient
  @Field(type = FieldType.Keyword)
  private Set<String> labels;

  /** The children. */
  @Field(type = FieldType.Object)
  private List<ConceptRef> children;

  /** The parents. */
  @Field(type = FieldType.Object)
  private List<ConceptRef> parents;

  /** The descendants. */
  @Field(type = FieldType.Object)
  private List<ConceptRef> descendants;

  /** The ancestors. */
  @Field(type = FieldType.Object)
  private List<ConceptRef> ancestors;

  /** The subsets. */
  @Field(type = FieldType.Object)
  private List<SubsetRef> subsets;

  // The following fields are here for returning a concept object
  // from the API with an include parameter. But the data lives
  // in a different index

  /** The relationships. */
  @Transient
  private List<ConceptRelationship> relationships;

  /** The inverse relationships. */
  @Transient
  private List<ConceptRelationship> inverseRelationships;

  /** The tree positions. */
  @Transient
  private List<ConceptTreePosition> treePositions;

  /**
   * Instantiates an empty {@link Concept}.
   */
  public Concept() {
    // n/a
  }

  /**
   * Instantiates a {@link Concept} from the specified parameters.
   *
   * @param other the other
   */
  public Concept(final Concept other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  /* see superclass */
  @Override
  public void populateFrom(final ConceptRef other) {
    super.populateFrom(other);

    if (other instanceof Concept) {
      final Concept concept = (Concept) other;
      normName = StringUtility.normalize(getName());
      stemName = StringUtility.normalizeWithStemming(getName());
      terms = new ArrayList<>(concept.getTerms());
      indexTerms = new ArrayList<>(concept.getIndexTerms());
      axioms = new ArrayList<>(concept.getAxioms());
      definitions = new ArrayList<>(concept.getDefinitions());
      attributes = new HashMap<>(concept.getAttributes());
      semanticTypes = new HashSet<>(concept.getSemanticTypes());
      labels = new HashSet<>(concept.getLabels());
      parents = new ArrayList<>(concept.getParents());
      children = new ArrayList<>(concept.getChildren());
      descendants = new ArrayList<>(concept.getDescendants());
      ancestors = new ArrayList<>(concept.getAncestors());
      subsets = new ArrayList<>(concept.getSubsets());

      // Additional fields
      relationships = new ArrayList<>(concept.getRelationships());
      inverseRelationships = new ArrayList<>(concept.getInverseRelationships());
      treePositions = new ArrayList<>(concept.getTreePositions());
      eclClauses = new ArrayList<>(concept.getEclClauses());
      highlights = new HashMap<>(concept.getHighlights());
    }
  }

  /**
   * Patch from.
   *
   * @param other the other
   */
  /* see superclass */
  @Override
  public void patchFrom(final ConceptRef other) {
    super.patchFrom(other);
    if (other instanceof Concept) {
      final Concept concept = (Concept) other;
      if (concept.getName() != null) {
        // "name" itself is handled in superclass
        normName = StringUtility.normalize(getName());
        stemName = StringUtility.normalizeWithStemming(getName());
      }
      if (!concept.getTerms().isEmpty()) {
        terms.addAll(concept.getTerms());
      }
      if (!concept.getIndexTerms().isEmpty()) {
        getIndexTerms().addAll(concept.getIndexTerms());
      }
      if (!concept.getAxioms().isEmpty()) {
        getAxioms().addAll(concept.getAxioms());
      }
      if (!concept.getDefinitions().isEmpty()) {
        getDefinitions().addAll(concept.getDefinitions());
      }
      if (!concept.getAttributes().isEmpty()) {
        getAttributes().putAll(concept.getAttributes());
      }
      if (!concept.getSemanticTypes().isEmpty()) {
        getSemanticTypes().addAll(concept.getSemanticTypes());
      }
      if (!concept.getLabels().isEmpty()) {
        getLabels().addAll(concept.getLabels());
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
  public int compareTo(final ConceptRef other) {
    return getCode().compareTo(other.getCode());
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  /* see superclass */
  @Override
  @Schema(description = "Unique identifier", requiredMode = RequiredMode.REQUIRED, format = "uuid")
  public String getId() {
    return super.getId();
  }

  /**
   * Returns the terms.
   *
   * @return the terms
   */
  @Schema(description = "Terms associated with the concept")
  public List<Term> getTerms() {
    if (terms == null) {
      terms = new ArrayList<>();
    }
    return terms;
  }

  /**
   * Sets the terms.
   *
   * @param terms the terms
   */
  public void setTerms(final List<Term> terms) {
    this.terms = terms;
  }

  /**
   * Gets the index terms.
   *
   * @return the index terms
   */
  @Schema(description = "Index terms associated with the concept (these exist for "
      + "searchability but are not strictly content from the publisher)")
  public List<String> getIndexTerms() {
    if (indexTerms == null) {
      indexTerms = new ArrayList<>();
    }
    return indexTerms;
  }

  /**
   * Sets the index terms.
   *
   * @param indexTerms the new index terms
   */
  public void setIndexTerms(final List<String> indexTerms) {
    this.indexTerms = indexTerms;
  }

  /**
   * Returns the definitions.
   *
   * @return the definitions
   */
  @Schema(description = "Textual definitions associated with the concept")
  public List<Definition> getDefinitions() {
    if (definitions == null) {
      definitions = new ArrayList<>();
    }
    return definitions;
  }

  /**
   * Sets the definitions.
   *
   * @param definitions the definitions
   */
  public void setDefinitions(final List<Definition> definitions) {
    this.definitions = definitions;
  }

  /**
   * Returns the axioms.
   *
   * @return the axioms
   */
  @Schema(description = "OwL/RDF axioms that express the logical definition")
  public List<Axiom> getAxioms() {
    if (axioms == null) {
      axioms = new ArrayList<>();
    }
    return axioms;
  }

  /**
   * Sets the axioms.
   *
   * @param axioms the axioms
   */
  public void setAxioms(final List<Axiom> axioms) {
    this.axioms = axioms;
  }

  /**
   * Gets the attributes.
   *
   * @return the attributes
   */
  /* see superclass */
  @Override
  @Schema(description = "Attribute key/value pairs associated with the concept")
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
   * Returns the semantic types.
   *
   * @return the semantic types
   */
  @Schema(description = "High level semantic types associated with the concept")
  public Set<String> getSemanticTypes() {
    if (semanticTypes == null) {
      semanticTypes = new HashSet<>();
    }
    return semanticTypes;
  }

  /**
   * Sets the semantic types.
   *
   * @param semanticTypes the semantic types
   */
  public void setSemanticTypes(final Set<String> semanticTypes) {
    this.semanticTypes = semanticTypes;
  }

  /**
   * Gets the labels.
   *
   * @return the labels
   */
  @Schema(description = "Labels associated with the concept")
  public Set<String> getLabels() {
    if (labels == null) {
      labels = new HashSet<>();
    }
    return labels;
  }

  /**
   * Sets the labels.
   *
   * @param labels the new labels
   */
  public void setLabels(final Set<String> labels) {
    this.labels = labels;
  }

  /* see superclass */
  @Override
  @Schema(description = "Preferred name of the concept")
  public void setName(final String name) {
    super.setName(name);
    normName = StringUtility.normalize(name);
    stemName = StringUtility.normalizeWithStemming(name);
  }

  /**
   * Minimize.
   */
  @Override
  public void minimize() {
    // Actually use this to "minimize" the concept down to concept ref.
    terms = new ArrayList<>(0);
    indexTerms = new ArrayList<>(0);
    definitions = new ArrayList<>(0);
    attributes = new HashMap<>(0);
    semanticTypes = new HashSet<>(0);
    labels = new HashSet<>(0);
    eclClauses = new ArrayList<>(0);
    children = new ArrayList<>(0);
    parents = new ArrayList<>(0);
    axioms = new ArrayList<>(0);
    children = new ArrayList<>(0);
    descendants = new ArrayList<>(0);
    ancestors = new ArrayList<>(0);
    subsets = new ArrayList<>(0);

    relationships = new ArrayList<>(0);
    inverseRelationships = new ArrayList<>(0);
    treePositions = new ArrayList<>(0);
  }

  /**
   * Clean for api.
   */
  /* see superclass */
  @Override
  public void cleanForApi() {
    // Blank all @Schema "hidden"
    normName = null;
    stemName = null;
    eclClauses = new ArrayList<>(0);
    getTerms().stream().forEach(t -> t.cleanForApi());

  }

  /**
   * Gets the children.
   *
   * @return the children
   */
  @Schema(description = "Children of the concept in the hierarchy")
  public List<ConceptRef> getChildren() {
    if (children == null) {
      children = new ArrayList<>();
    }
    return children;
  }

  /**
   * Sets the children.
   *
   * @param children the new children
   */
  public void setChildren(final List<ConceptRef> children) {
    this.children = children;
  }

  /**
   * Gets the parents.
   *
   * @return the parents
   */
  @Schema(description = "Parents of the concept in the hierarchy")
  public List<ConceptRef> getParents() {
    if (parents == null) {
      parents = new ArrayList<>();
    }
    return parents;
  }

  /**
   * Sets the parents.
   *
   * @param parents the new parents
   */
  public void setParents(final List<ConceptRef> parents) {
    this.parents = parents;
  }

  /**
   * Gets the descendants.
   *
   * @return the descendants
   */
  @Schema(description = "Descendants of the concept in the hierarchy")
  public List<ConceptRef> getDescendants() {
    if (descendants == null) {
      descendants = new ArrayList<>();
    }
    return descendants;
  }

  /**
   * Sets the descendants.
   *
   * @param descendants the new descendants
   */
  public void setDescendants(final List<ConceptRef> descendants) {
    this.descendants = descendants;
  }

  /**
   * Gets the ancestors.
   *
   * @return the ancestors
   */
  @Schema(description = "Ancestors of the concept in the hierarchy")
  public List<ConceptRef> getAncestors() {
    if (ancestors == null) {
      ancestors = new ArrayList<>();
    }
    return ancestors;
  }

  /**
   * Sets the ancestors.
   *
   * @param ancestors the new ancestors
   */
  public void setAncestors(final List<ConceptRef> ancestors) {
    this.ancestors = ancestors;
  }

  /**
   * Gets the subsets.
   *
   * @return the subsets
   */
  @Schema(description = "Subsets the concept is a part of from the same terminology loader")
  public List<SubsetRef> getSubsets() {
    if (subsets == null) {
      subsets = new ArrayList<>();
    }
    return subsets;
  }

  /**
   * Sets the subset.
   *
   * @param subsets the new subset
   */
  public void setSubsets(final List<SubsetRef> subsets) {
    this.subsets = subsets;
  }

  /**
   * Gets the relationships.
   *
   * @return the relationships
   */
  @Schema(description = "Relationships from this concept to other concepts")
  public List<ConceptRelationship> getRelationships() {
    if (relationships == null) {
      relationships = new ArrayList<>();
    }
    return relationships;
  }

  /**
   * Sets the relationships.
   *
   * @param relationships the new relationships
   */
  public void setRelationships(final List<ConceptRelationship> relationships) {
    this.relationships = relationships;
  }

  /**
   * Gets the inverse relationships.
   *
   * @return the inverse relationships
   */
  @Schema(description = "Relationships from other concepts to this concept")
  public List<ConceptRelationship> getInverseRelationships() {
    if (inverseRelationships == null) {
      inverseRelationships = new ArrayList<>();
    }
    return inverseRelationships;
  }

  /**
   * Sets the inverse relationships.
   *
   * @param inverseRelationships the new inverse relationships
   */
  public void setInverseRelationships(final List<ConceptRelationship> inverseRelationships) {
    this.inverseRelationships = inverseRelationships;
  }

  /**
   * Gets the tree positions.
   *
   * @return the tree positions
   */
  @Schema(description = "Tree positions of the concept in the hierarchy")
  public List<ConceptTreePosition> getTreePositions() {
    if (treePositions == null) {
      treePositions = new ArrayList<>();
    }
    return treePositions;
  }

  /**
   * Sets the tree positions.
   *
   * @param treePositions the new tree positions
   */
  public void setTreePositions(final List<ConceptTreePosition> treePositions) {
    this.treePositions = treePositions;
  }

  /**
   * Gets the ecl clauses.
   *
   * @return the ecl clauses
   */
  @Schema(hidden = true)
  public List<String> getEclClauses() {
    if (eclClauses == null) {
      eclClauses = new ArrayList<>();
    }
    return eclClauses;
  }

  /**
   * Sets the ecl clauses.
   *
   * @param eclClauses the new ecl clauses
   */
  public void setEclClauses(final List<String> eclClauses) {
    this.eclClauses = eclClauses;
  }

  /**
   * Gets the norm name.
   *
   * @return the norm name
   */
  @Schema(hidden = true)
  public String getNormName() {
    return normName;
  }

  /**
   * Sets the norm name.
   *
   * @param normName the new norm name
   */
  public void setNormName(final String normName) {
    this.normName = normName;
  }

  /**
   * Gets the stem name.
   *
   * @return the stem name
   */
  @Schema(hidden = true)
  public String getStemName() {
    return stemName;
  }

  /**
   * Sets the stem name.
   *
   * @param stemName the new stem name
   */
  public void setStemName(final String stemName) {
    this.stemName = stemName;
  }

  /**
   * Gets the highlights.
   *
   * @return the highlights
   */
  /* see superclass */
  @Override
  @Schema(hidden = true)
  public Map<String, String> getHighlights() {
    if (highlights == null) {
      highlights = new HashMap<>();
    }
    return highlights;
  }

  /**
   * Sets the highlights.
   *
   * @param highlights the highlights
   */
  /* see superclass */
  @Override
  public void setHighlights(final Map<String, String> highlights) {
    this.highlights = highlights;
  }

}
