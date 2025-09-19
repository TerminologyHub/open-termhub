/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication;

/**
 * Enumeration of syndication content types and their corresponding Lucene index
 * mappings.
 */
public enum SyndicationContentType {

  /** CodeSystem content type. */
  CODESYSTEM("CodeSystem", "CodeSystem", "terminology", "terminology"),

  /** ValueSet content type. */
  VALUESET("ValueSet", "ValueSet", "subset", "subset"),

  /** ConceptMap content type. */
  CONCEPTMAP("ConceptMap", "ConceptMap", "mapset", "mapset");

  /** The syndication category term. */
  private final String syndicationCategory;

  /** The resource type. */
  private final String resourceType;

  /** The Lucene index name. */
  private final String luceneIndexName;

  /** The Lucene field prefix. */
  private final String luceneFieldPrefix;

  /**
   * Instantiates a {@link SyndicationContentType} from the specified
   * parameters.
   *
   * @param resourceType the resource type
   * @param syndicationCategory the syndication category term
   * @param luceneIndexName the Lucene index name
   * @param luceneFieldPrefix the Lucene field prefix
   */
  SyndicationContentType(final String resourceType, final String syndicationCategory,
      final String luceneIndexName, final String luceneFieldPrefix) {
    this.resourceType = resourceType;
    this.syndicationCategory = syndicationCategory;
    this.luceneIndexName = luceneIndexName;
    this.luceneFieldPrefix = luceneFieldPrefix;
  }

  /**
   * Returns the resource type.
   *
   * @return the resource type
   */
  public String getResourceType() {
    return resourceType;
  }

  /**
   * Returns the syndication category term.
   *
   * @return the syndication category
   */
  public String getSyndicationCategory() {
    return syndicationCategory;
  }

  /**
   * Returns the Lucene index name.
   *
   * @return the Lucene index name
   */
  public String getLuceneIndexName() {
    return luceneIndexName;
  }

  /**
   * Returns the Lucene field prefix.
   *
   * @return the Lucene field prefix
   */
  public String getLuceneFieldPrefix() {
    return luceneFieldPrefix;
  }

  /**
   * Get syndication content type from syndication category.
   *
   * @param syndicationCategory the syndication category
   * @return the syndication content type, or null if not found
   */
  public static SyndicationContentType fromSyndicationCategory(final String syndicationCategory) {
    if (syndicationCategory == null) {
      return null;
    }

    // First try exact match
    for (final SyndicationContentType type : values()) {
      if (type.syndicationCategory.equalsIgnoreCase(syndicationCategory)) {
        return type;
      }
    }

    // Handle FHIR R5 categories by pattern matching
    if (syndicationCategory.contains("_FHIRR5_ALL")) {
      if (syndicationCategory.contains("ICD10CM") && syndicationCategory.contains("SNOMEDCT")) {
        return CONCEPTMAP; // SNOMEDCT-US-ICD10CM_FHIRR5_ALL
      } else if (syndicationCategory.contains("EXTENSION") || syndicationCategory.contains("CORE")
          || syndicationCategory.contains("MODEL") || syndicationCategory.contains("723264001")) {
        return VALUESET; // ValueSet categories
      } else {
        return CODESYSTEM; // All other FHIR R5 categories are CodeSystems
      }
    }

    return null;
  }

  /**
   * Get syndication content type from download URL path.
   *
   * @param downloadUrl the download URL
   * @return the syndication content type, or null if not found
   */
  public static SyndicationContentType fromDownloadUrl(final String downloadUrl) {
    if (downloadUrl == null) {
      return null;
    }

    if (downloadUrl.contains("/terminology/")) {
      return CODESYSTEM;
    } else if (downloadUrl.contains("/mapset/")) {
      return CONCEPTMAP;
    } else if (downloadUrl.contains("/subset/")) {
      return VALUESET;
    }

    return null;
  }

  /**
   * Get the Lucene field name for abbreviation.
   *
   * @return the abbreviation field name
   */
  public String getAbbreviationFieldName() {
    return luceneFieldPrefix + ".abbreviation";
  }

  /**
   * Get the Lucene field name for publisher.
   *
   * @return the publisher field name
   */
  public String getPublisherFieldName() {
    return luceneFieldPrefix + ".publisher";
  }

  /**
   * Get the Lucene field name for version.
   *
   * @return the version field name
   */
  public String getVersionFieldName() {
    return luceneFieldPrefix + ".version";
  }
}
