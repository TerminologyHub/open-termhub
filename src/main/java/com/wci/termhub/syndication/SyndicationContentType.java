/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
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
  CODESYSTEM("CodeSystem"),

  /** ValueSet content type. */
  VALUESET("ValueSet"),

  /** ConceptMap content type. */
  CONCEPTMAP("ConceptMap");

  /** The resource type. */
  private final String resourceType;

  /**
   * Instantiates a {@link SyndicationContentType} from the specified
   * parameters.
   *
   * @param resourceType the resource type
   */
  SyndicationContentType(final String resourceType) {
    this.resourceType = resourceType;
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
   * Resolve a content type from a stored resource type string.
   *
   * @param resourceType the resource type string
   * @return the matching content type, or null if none matches
   */
  public static SyndicationContentType fromResourceType(final String resourceType) {
    if (resourceType == null) {
      return null;
    }
    for (final SyndicationContentType t : values()) {
      if (t.getResourceType().equals(resourceType)) {
        return t;
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

}
