/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
/*
 * Copyright 2020 Persona Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Persona Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * Persona Informatics and may be covered by U.S. and Foreign Patents, patents in process,
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

import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.wci.termhub.util.ModelUtility;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a concept tree position.
 */
@Schema(description = "Represents a tree position in the terminology hierarchy for a concept")
@Document(indexName = "concept-tree-position-v1")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConceptTreePosition extends AbstractTerminologyComponent
    implements Copyable<ConceptTreePosition>
{

  /** The concept. */
  @Field(type = FieldType.Object)
  private ConceptRef concept;

  /** The additional type. */
  @Field(type = FieldType.Keyword)
  private String additionalType;

  /** The ancestor path. */
  @Field(type = FieldType.Keyword)
  private String ancestorPath;

  /** The child ct. */
  @Field(type = FieldType.Integer)
  private Integer childCt;

  /** The level. */
  @Field(type = FieldType.Integer)
  private Integer level;

  /** The children. */
  @Transient
  private List<ConceptTreePosition> children;

  /**
   * Instantiates an empty {@link ConceptTreePosition}.
   */
  public ConceptTreePosition() {
    // do nothing
  }

  /**
   * Instantiates a {@link ConceptTreePosition} from the specified parameters.
   *
   * @param other the other
   */
  public ConceptTreePosition(final Concept other) {
    final ConceptRef conceptRef = new ConceptRef();
    conceptRef.setName(other.getName());
    conceptRef.setCode(other.getCode());
    setConcept(conceptRef);
    setTerminology(other.getTerminology());
    setPublisher(other.getPublisher());
    setVersion(other.getVersion());
  }

  /**
   * Instantiates a {@link ConceptTreePosition} from the specified parameters.
   *
   * @param other the other
   */
  public ConceptTreePosition(final ConceptTreePosition other) {
    populateFrom(other);
  }

  /* see superclass */
  @Override
  public void populateFrom(final ConceptTreePosition other) {
    super.populateFrom(other);
    additionalType = other.getAdditionalType();
    ancestorPath = other.getAncestorPath();
    childCt = other.getChildCt();
    concept = other.getConcept();
    level = other.getLevel();
    children = new ArrayList<>(other.getChildren());
  }

  /* see superclass */
  @Override
  public void patchFrom(final ConceptTreePosition other) {
    super.patchFrom(other);
    if (other.getAdditionalType() != null) {
      additionalType = other.getAdditionalType();
    }
    if (other.getAncestorPath() != null) {
      ancestorPath = other.getAncestorPath();
    }
    if (other.getChildCt() != 0) {
      childCt = other.getChildCt();
    }
    if (other.getConcept() != null) {
      concept = other.getConcept();
    }
    if (other.getLevel() != null) {
      level = other.getLevel();
    }
    if (!other.getChildren().isEmpty()) {
      children.addAll(other.getChildren());
    }
  }

  /**
   * Gets the concept.
   *
   * @return the concept
   */
  @Schema(description = "reference to the concept with this tree position")
  public ConceptRef getConcept() {
    return concept;
  }

  /**
   * Sets the concept.
   *
   * @param concept the new concept
   */
  public void setConcept(final ConceptRef concept) {
    this.concept = concept;
  }

  /**
   * Returns the additional type.
   *
   * @return the additional type
   */
  @Schema(description = "Type of relationship in a more specific sense, "
      + "e.g. \"is_a\", \"116680003\".  Typically this field"
      + "will be used for the hierarchical relationship type asserted in the"
      + "published source data")
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
   * Returns the ancestor path.
   *
   * @return the ancestor path
   */
  @Schema(description = "Dot-separated chain of codes from the "
      + "root of the tree to the parent code of this node")
  public String getAncestorPath() {
    return ancestorPath;
  }

  /**
   * Sets the ancestor path.
   *
   * @param ancestorPath the ancestor path
   */
  public void setAncestorPath(final String ancestorPath) {
    this.ancestorPath = ancestorPath;
  }

  /**
   * Returns the child ct.
   *
   * @return the child ct
   */
  @Schema(description = "Count of child nodes")
  public Integer getChildCt() {
    return childCt;
  }

  /**
   * Sets the child ct.
   *
   * @param childCt the child ct
   */
  public void setChildCt(final Integer childCt) {
    this.childCt = childCt;
  }

  /**
   * Returns the level.
   *
   * @return the level
   */
  @Schema(description = "Level of depth in the hierarchy")
  public Integer getLevel() {
    return level;
  }

  /**
   * Sets the level.
   *
   * @param level the level
   */
  public void setLevel(final Integer level) {
    this.level = level;
  }

  /**
   * Returns the children.
   *
   * @return the children
   */
  @Schema(description = "Child tree position nodes")
  public List<ConceptTreePosition> getChildren() {
    if (children == null) {
      children = new ArrayList<>();
    }
    return children;
  }

  /**
   * Sets the children.
   *
   * @param children the children
   */
  public void setChildren(final List<ConceptTreePosition> children) {
    this.children = children;
  }

  /**
   * Merge.
   *
   * @param from the from
   * @param sortField the sort field
   * @throws Exception the exception
   */
  public void merge(final ConceptTreePosition from, final String sortField) throws Exception {
    // allow for merging trees with null ids
    if (!(from.getId() == null && getId() == null)) {
      // but don't allow merging trees with different ids
      if (!getId().equals(from.getId())) {
        throw new IllegalArgumentException("Unable to merge tree with different root");
      }
    }
    // assemble a map of this tree's children
    final Map<String, ConceptTreePosition> childMap = new HashMap<>();
    for (final ConceptTreePosition t : getChildren()) {
      childMap.put(t.getId(), t);
    }

    for (final ConceptTreePosition child : from.getChildren()) {
      if (!childMap.containsKey(child.getId())) {
        getChildren().add(child);
      } else {
        childMap.get(child.getId()).merge(child, sortField);
      }
    }
    // Sort the children at this level
    if (sortField != null) {
      ModelUtility.reflectionSort(getChildren(), ConceptTreePosition.class, sortField);
    }

  }

  /**
   * Compute leaf nodes.
   *
   * @return the list
   * @throws Exception the exception
   */
  public List<ConceptTreePosition> computeLeafNodes() throws Exception {
    final Set<ConceptTreePosition> results = new HashSet<>();
    getLeafNodesHelper(this, results);
    // package as list
    return new ArrayList<ConceptTreePosition>(results);
  }

  /**
   * Returns the leaf nodes helper.
   *
   * @param tree the tree
   * @param leafNodes the leaf nodes
   */
  private void getLeafNodesHelper(final ConceptTreePosition tree,
    final Set<ConceptTreePosition> leafNodes) {
    if (tree.getChildren().size() == 0) {
      leafNodes.add(tree);
    } else {
      for (final ConceptTreePosition chd : tree.getChildren()) {
        getLeafNodesHelper(chd, leafNodes);
      }
    }
  }
}
