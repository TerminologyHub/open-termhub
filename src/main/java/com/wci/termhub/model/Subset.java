/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a subset, refset, or value set.
 */
@Schema(description = "Represents a subset, refset, or value set in a terminology")
@Document(indexName = "subset-v1")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subset extends SubsetRef implements TerminologyComponent, HasAttributes {

  /**
   * Attribute keys that can be associated with a terminology (and their
   * descriptions) rendered as terminology attributes when building a data set.
   */
  public enum Attributes {

    /** The origin terminology. */
    fhirDate("date"),

    /** The origin version. */
    fhirUrl("url"),

    /** The experimental - e.g. "true" or "false". */
    fhirExperimental("experimental"),

    /** The identifier - e.g. "900000000000012004". */
    fhirIdentifier("identifier");

    /** The name. */
    @SuppressWarnings("unused")
    private final String property;

    /**
     * Instantiates a {@link Attributes} from the specified parameters.
     *
     * @param property the property
     */
    private Attributes(final String property) {
      this.property = property;
    }
  }

  /** The terminology. */
  @Field(type = FieldType.Keyword)
  private String terminology;

  /** The license. */
  @Field(type = FieldType.Keyword)
  private String license;

  /** The category. */
  @Field(type = FieldType.Keyword)
  private String category;

  /** The entity type. */
  @Field(type = FieldType.Keyword)
  private String entityType;

  /** The description. */
  @Field(type = FieldType.Object, enabled = false)
  private String description;

  /** The scope. */
  @Field(type = FieldType.Object, enabled = false)
  private String expression;

  /** The scope. */
  @Field(type = FieldType.Object, enabled = false)
  private String query;

  /** The attributes. */
  @Field(type = FieldType.Object)
  private Map<String, String> attributes;

  /** The statistics, e.g. "mappings", "uniqueFromCodes", "uniqueToCodes". */
  @Field(type = FieldType.Object, enabled = false)
  private Map<String, Integer> statistics;

  /** The disjoint subset codes. */
  @Field(type = FieldType.Keyword)
  private Set<String> disjointSubsets;

  /**
   * Instantiates an empty {@link Subset}.
   */
  public Subset() {
    // n/a
  }

  /**
   * Instantiates a {@link Subset} from the specified parameters.
   *
   * @param other the other
   */
  public Subset(final Subset other) {
    populateFrom(other);
  }

  /* see superclass */
  @Override
  public void populateFrom(final TerminologyRef other) {
    super.populateFrom(other);

    if (other instanceof Subset) {
      final Subset subset = (Subset) other;
      terminology = subset.getTerminology();
      category = subset.getCategory();
      license = subset.getLicense();
      entityType = subset.getEntityType();
      description = subset.getDescription();
      disjointSubsets = new HashSet<>(subset.getDisjointSubsets());
      expression = subset.getExpression();
      query = subset.getQuery();
      attributes = new HashMap<>(subset.getAttributes());
      statistics = new HashMap<>(subset.getStatistics());
    }
  }

  /* see superclass */
  @Override
  public void patchFrom(final TerminologyRef other) {
    super.patchFrom(other);
    if (other instanceof Subset) {
      final Subset subset = (Subset) other;
      if (!subset.getAttributes().isEmpty()) {
        getAttributes().putAll(subset.getAttributes());
      }
      if (!subset.getStatistics().isEmpty()) {
        getStatistics().putAll(subset.getStatistics());
      }
      if (subset.getLicense() != null) {
        license = subset.getLicense();
      }
      if (subset.getTerminology() != null) {
        terminology = subset.getTerminology();
      }
      if (subset.getCategory() != null) {
        category = subset.getCategory();
      }
      if (subset.getEntityType() != null) {
        entityType = subset.getEntityType();
      }
      if (subset.getDescription() != null) {
        description = subset.getDescription();
      }
      if (!subset.getDisjointSubsets().isEmpty()) {
        getDisjointSubsets().addAll(subset.getDisjointSubsets());
      }
      if (subset.getExpression() != null) {
        expression = subset.getExpression();
      }
      if (subset.getQuery() != null) {
        query = subset.getQuery();
      }
    }
  }

  /* see superclass */
  @Override
  public void minimize() {
    super.minimize();
    cleanForApi();
  }

  /* see superclass */
  @Override
  public void cleanForApi() {
    // indexName = null;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Objects.hash(getId());
    return result;
  }

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

  /* see superclass */
  @Override
  public int compareTo(final TerminologyRef other) {
    return getAbbreviation().compareTo(other.getAbbreviation());
  }

  /* see superclass */
  @Override
  public String getTerminology() {
    return terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(final String terminology) {
    this.terminology = terminology;
  }

  /**
   * Gets the license.
   *
   * @return the license
   */
  @Schema(description = "License for usage of this subset (e.g. \"TERMHUB\")")
  public String getLicense() {
    return license;
  }

  /**
   * Sets the license.
   *
   * @param license the new license
   */
  public void setLicense(final String license) {
    this.license = license;
  }

  /**
   * Gets the category.
   *
   * @return the category
   */
  @Schema(description = "Category for usage of this subset (e.g. \"cancer\")")
  public String getCategory() {
    return category;
  }

  /**
   * Sets the category.
   *
   * @param category the new category
   */
  public void setCategory(final String category) {
    this.category = category;
  }

  /**
   * Gets the entity type.
   *
   * @return the entity type
   */
  @Schema(description = "Clinical class entity type for the kind of concepts in the subset,"
      + " e.g. \"medication\" or \"diagnosis\"")
  public String getEntityType() {
    return entityType;
  }

  /**
   * Sets the entity type.
   *
   * @param entityType the new entity type
   */
  public void setEntityType(final String entityType) {
    this.entityType = entityType;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  @Schema(description = "Description of the subset")
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
   * Gets the scope.
   *
   * @return the scope
   */
  @Schema(description = "Concept query used as part of the subsets computable definition")
  public String getExpression() {
    return expression;
  }

  /**
   * Sets the scope.
   *
   * @param expression the new scope
   */
  @Schema(description = "Concept expression used as part of the subsets computable definition")
  public void setExpression(final String expression) {
    this.expression = expression;
  }

  /**
   * Gets the query.
   *
   * @return the query
   */
  @Schema(description = "Concept query used as part of the subsets computable definition")
  public String getQuery() {
    return query;
  }

  /**
   * Sets the query.
   *
   * @param query the new query
   */
  public void setQuery(final String query) {
    this.query = query;
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
   * Gets the disjoint subsets.
   *
   * @return the disjoint subsets
   */
  @Schema(
      description = "Codes for matching terminology/publiser/version subsets this one is disjoint with")
  public Set<String> getDisjointSubsets() {
    if (disjointSubsets == null) {
      disjointSubsets = new HashSet<>();
    }
    return disjointSubsets;
  }

  /**
   * Sets the disjoint subsets.
   *
   * @param disjointSubsets the new disjoint subsets
   */
  public void setDisjointSubsets(final Set<String> disjointSubsets) {
    this.disjointSubsets = disjointSubsets;
  }

}
