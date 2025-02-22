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
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.wci.termhub.util.ModelUtility;

/**
 * Represents choices about how much data to include when reading content.
 */
public class IncludeParam extends BaseModel {

  /** The concept base fields. */
  private static List<String> conceptBaseFields = getConceptBaseFields();

  /** The synonyms. */
  private boolean terms;

  /** The definitions. */
  private boolean definitions;

  /** The attributes. */
  private boolean attributes;

  /** The children. */
  private boolean children;

  /** The descendants. */
  private boolean descendants;

  /** The ancestors. */
  private boolean ancestors;

  /** The parents. */
  private boolean parents;

  /** The relationships. */
  private boolean relationships;

  /** The inverse relationships. */
  private boolean inverseRelationships;

  /** The mapsets. */
  private boolean mapsets;

  /** The subsets. */
  private boolean subsets;

  /** The axioms. */
  private boolean axioms;

  /** The highlights. */
  private boolean highlights;

  /** The paths. */
  private boolean treePositions;

  /** The semantic types. */
  private boolean semanticTypes;

  /**
   * Gets the concept base fields.
   *
   * @return the concept base fields
   */
  public static List<String> getConceptBaseFields() {
    final List<String> fields = ModelUtility.getBaseFields(Concept.class).stream()
        .map(f -> f.getName()).collect(Collectors.toList());
    fields.remove("normName");
    fields.remove("stemName");
    return fields;
  }

  /**
   * Instantiates an empty {@link IncludeParam}.
   */
  public IncludeParam() {
    // n/a
  }

  /**
   * Instantiates a {@link IncludeParam} from the specified parameters.
   *
   * @param include the include
   */
  public IncludeParam(final String include) {
    if (include == null) {
      populateMinimal();
    } else {
      for (final String partPreTrim : include.split(",")) {
        final String part = partPreTrim.trim();
        if (part.equals("minimal")) {
          populateMinimal();
        } else if (part.equals("summary")) {
          populateSummary();
        } else if (part.equals("full")) {
          populateFull();
        } else if (part.equals("terms")) {
          terms = true;
        } else if (part.equals("definitions")) {
          definitions = true;
        } else if (part.equals("attributes")) {
          attributes = true;
        } else if (part.equals("children")) {
          children = true;
        } else if (part.equals("descendants")) {
          descendants = true;
        } else if (part.equals("ancestors")) {
          ancestors = true;
        } else if (part.equals("parents")) {
          parents = true;
        } else if (part.equals("relationships")) {
          relationships = true;
        } else if (part.equals("inverseRelationships")) {
          inverseRelationships = true;
        } else if (part.equals("mapsets")) {
          mapsets = true;
        } else if (part.equals("subsets")) {
          subsets = true;
        } else if (part.equals("highlights")) {
          highlights = true;
        } else if (part.equals("axioms")) {
          axioms = true;
        } else if (part.equals("treePositions")) {
          treePositions = true;
        } else if (part.equals("semanticTypes")) {
          semanticTypes = true;
        } else {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              "Invalid includes value = " + part + (part.equals(include) ? "" : "; " + include));
        }
      }
    }
  }

  /**
   * Instantiates a {@link IncludeParam} from the specified parameters.
   *
   * @param other the other
   */
  public IncludeParam(final IncludeParam other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  public void populateFrom(final IncludeParam other) {
    terms = other.isTerms();
    definitions = other.isDefinitions();
    attributes = other.isAttributes();
    children = other.isChildren();
    descendants = other.isDescendants();
    ancestors = other.isAncestors();
    parents = other.isParents();
    relationships = other.isRelationships();
    inverseRelationships = other.isInverseRelationships();
    mapsets = other.isMapsets();
    subsets = other.isSubsets();
    highlights = other.isHighlights();
    axioms = other.isAxioms();
    treePositions = other.isTreePositions();
    semanticTypes = other.isSemanticTypes();
  }

  /**
   * Populate minimal.
   */
  public void populateMinimal() {
    // n/a - this involves setting actually nothing right now because code/name
    // are required.
  }

  /**
   * Populate summary.
   */
  public void populateSummary() {
    terms = true;
    definitions = true;
    attributes = true;
    semanticTypes = true;
  }

  /**
   * Populate full.
   */
  public void populateFull() {
    terms = true;
    definitions = true;
    attributes = true;
    children = true;
    parents = true;
    relationships = true;
    inverseRelationships = true;
    mapsets = true;
    subsets = true;
    axioms = true;
    semanticTypes = true;

    // Full doesn't include descendants, ancestors and tree positions
    // treePositions = true;
    // descendants = true;
    // ancestors = true;
  }

  /**
   * Checks for anything set.
   *
   * @return true, if successful
   */
  public boolean hasAnyTrue() {
    return terms || definitions || attributes || children || ancestors || descendants || parents
        || relationships || inverseRelationships || mapsets || subsets || axioms || treePositions
        || semanticTypes;
  }

  /**
   * Returns included fields.
   *
   * @return the included fields
   */
  public String[] getIncludedFields() {
    final List<String> fields = new ArrayList<>(conceptBaseFields);

    if (terms) {
      fields.add("terms");
    }
    if (definitions) {
      fields.add("definitions");
    }
    if (attributes) {
      fields.add("attributes");
    }
    if (children) {
      fields.add("children");
    }
    if (parents) {
      fields.add("parents");
    }
    if (mapsets) {
      fields.add("mapsets");
    }
    if (subsets) {
      fields.add("subsets");
    }
    if (axioms) {
      fields.add("axioms");
    }
    if (descendants) {
      fields.add("descendants");
    }
    if (ancestors) {
      fields.add("ancestors");
    }
    if (semanticTypes) {
      fields.add("semanticTypes");
    }
    // treePositions, relationships, and inverseRelationships
    // are NOT present in the concept index
    // They have their own indexes and need to be looked up if included.

    // Always put highlights last so it is easy to check for
    if (highlights) {
      fields.add("highlights");
    }

    return fields.toArray(new String[fields.size()]);
  }

  /**
   * Returns excluded fields.
   *
   * @return the excluded fields
   */
  public String[] getExcludedFields() {
    final List<String> fields = new ArrayList<>();
    fields.add("stemName"); // stemName always excluded
    fields.add("normName"); // normName always excluded
    fields.add("eclClauses"); // eclClauses always excluded
    fields.add("labels"); // always excluded

    if (!terms) {
      fields.add("terms");
    }
    if (!definitions) {
      fields.add("definitions");
    }
    if (!attributes) {
      fields.add("attributes");
    }
    if (!children) {
      fields.add("children");
    }
    if (!parents) {
      fields.add("parents");
    }
    if (!mapsets) {
      fields.add("mapsets");
    }
    if (!subsets) {
      fields.add("subsets");
    }
    if (!highlights) {
      fields.add("highlights");
    }
    if (!axioms) {
      fields.add("axioms");
    }
    if (!descendants) {
      fields.add("descendants");
    }
    if (!ancestors) {
      fields.add("ancestors");
    }
    if (!semanticTypes) {
      fields.add("semanticTypes");
    }
    // treePositions, relationships, and inverseRelationships
    // are NOT present in the concept index
    // They have their own indexes and need to be looked up if included.
    return fields.toArray(new String[fields.size()]);
  }

  /**
   * Checks if is terms.
   *
   * @return true, if is terms
   */
  public boolean isTerms() {
    return terms;
  }

  /**
   * Sets the terms.
   *
   * @param terms the new terms
   */
  public void setTerms(final boolean terms) {
    this.terms = terms;
  }

  /**
   * Indicates whether or not definitions is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isDefinitions() {
    return definitions;
  }

  /**
   * Sets the definitions.
   *
   * @param definitions the definitions
   */
  public void setDefinitions(final boolean definitions) {
    this.definitions = definitions;
  }

  /**
   * Checks if is attributes.
   *
   * @return true, if is attributes
   */
  public boolean isAttributes() {
    return attributes;
  }

  /**
   * Sets the attributes.
   *
   * @param properties the new attributes
   */
  public void setAttributes(final boolean properties) {
    this.attributes = properties;
  }

  /**
   * Indicates whether or not children is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isChildren() {
    return children;
  }

  /**
   * Sets the children.
   *
   * @param children the children
   */
  public void setChildren(final boolean children) {
    this.children = children;
  }

  /**
   * Indicates whether or not descendant is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isDescendants() {
    return descendants;
  }

  /**
   * Sets the descendants.
   *
   * @param descendants the descendants
   */
  public void setDescendant(final boolean descendants) {
    this.descendants = descendants;
  }

  /**
   * Checks if is ancestors.
   *
   * @return true, if is ancestors
   */
  public boolean isAncestors() {
    return ancestors;
  }

  /**
   * Sets the ancestors.
   *
   * @param ancestors the new ancestors
   */
  public void setAncestors(final boolean ancestors) {
    this.ancestors = ancestors;
  }

  /**
   * Indicates whether or not parents is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isParents() {
    return parents;
  }

  /**
   * Sets the parents.
   *
   * @param parents the parents
   */
  public void setParents(final boolean parents) {
    this.parents = parents;
  }

  /**
   * Checks if is relationships.
   *
   * @return true, if is relationships
   */
  public boolean isRelationships() {
    return relationships;
  }

  /**
   * Sets the relationships.
   *
   * @param relationships the new relationships
   */
  public void setRelationships(final boolean relationships) {
    this.relationships = relationships;
  }

  /**
   * Indicates whether or not inverse relationships is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isInverseRelationships() {
    return inverseRelationships;
  }

  /**
   * Sets the inverse relationships.
   *
   * @param inverseRelationships the inverse relationships
   */
  public void setInverseRelationships(final boolean inverseRelationships) {
    this.inverseRelationships = inverseRelationships;
  }

  /**
   * Indicates whether or not mapsets is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isMapsets() {
    return mapsets;
  }

  /**
   * Sets the mapsets.
   *
   * @param mapsets the mapsets
   */
  public void setMapsets(final boolean mapsets) {
    this.mapsets = mapsets;
  }

  /**
   * Sets the flag for including subset memberships.
   *
   * @return true, if is subsets
   */
  public boolean isSubsets() {
    return subsets;
  }

  /**
   * Sets the subsets.
   *
   * @param subsets the new subsets
   */
  public void setSubsets(final boolean subsets) {
    this.subsets = subsets;
  }

  /**
   * Indicates whether or not highlights is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isHighlights() {
    return highlights;
  }

  /**
   * Sets the highlights.
   *
   * @param highlights the highlights
   */
  public void setHighlights(final boolean highlights) {
    this.highlights = highlights;
  }

  /**
   * Checks if is axioms.
   *
   * @return true, if is axioms
   */
  public boolean isAxioms() {
    return axioms;
  }

  /**
   * Sets the axioms.
   *
   * @param axioms the new axioms
   */
  public void setAxioms(final boolean axioms) {
    this.axioms = axioms;
  }

  /**
   * Checks if is tree positions.
   *
   * @return true, if is tree positions
   */
  public boolean isTreePositions() {
    return treePositions;
  }

  /**
   * Sets the tree positions.
   *
   * @param treePositions the new tree positions
   */
  public void setTreePositions(final boolean treePositions) {
    this.treePositions = treePositions;
  }

  /**
   * Checks if is semantic types.
   *
   * @return true, if is semantic types
   */
  public boolean isSemanticTypes() {
    return semanticTypes;
  }

  /**
   * Sets the semantic types.
   *
   * @param semanticTypes the new semantic types
   */
  public void setSemanticTypes(final boolean semanticTypes) {
    this.semanticTypes = semanticTypes;
  }

}
