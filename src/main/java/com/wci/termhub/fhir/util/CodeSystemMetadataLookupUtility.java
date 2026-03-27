/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.StringUtility;

/**
 * Utility for looking up and caching Metadata for a Terminology.
 */
public final class CodeSystemMetadataLookupUtility {

  /** The cache. */
  private static final Map<String, List<Metadata>> CACHE = new ConcurrentHashMap<>();

  /**
   * Instantiates a new {@link CodeSystemMetadataLookupUtility}.
   */
  private CodeSystemMetadataLookupUtility() {
    // n/a
  }

  /**
   * Returns metadata for the specified terminology, using a cache.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @return the metadata for terminology
   * @throws Exception the exception
   */
  public static List<Metadata> getMetadataForTerminology(
    final EntityRepositoryService searchService, final Terminology terminology) throws Exception {

    if (terminology == null) {
      return List.of();
    }
    final String id = terminology.getId();
    if (id == null) {
      return List.of();
    }

    return CACHE.computeIfAbsent(id, key -> {
      try {
        return loadMetadata(searchService, terminology);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  /**
   * Clear cache for terminology.
   *
   * @param terminologyId the terminology id
   */
  public static void clearCacheForTerminology(final String terminologyId) {
    if (terminologyId != null) {
      CACHE.remove(terminologyId);
    }
  }

  /**
   * Load metadata for a terminology.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @return the list
   * @throws Exception the exception
   */
  private static List<Metadata> loadMetadata(final EntityRepositoryService searchService,
    final Terminology terminology) throws Exception {

    final String terminologyAbbr = terminology.getAbbreviation();
    final String publisher = terminology.getPublisher();
    final String version = terminology.getVersion();

    final String terminologyClause =
        "terminology:" + StringUtility.escapeQuery(terminologyAbbr);
    final String publisherClause =
        "publisher:" + StringUtility.escapeQuery(publisher);
    final String versionClause =
        "version:" + StringUtility.escapeQuery(version);

    final String relationshipAdditionalType =
        "(model:relationship AND field:additionalType)";
    final String conceptAttribute =
        "(model:concept AND field:attribute)";
    final String modelFieldClause =
        "(" + relationshipAdditionalType + " OR " + conceptAttribute + ")";

    final String query = StringUtility.composeQuery("AND", terminologyClause, publisherClause,
        versionClause, modelFieldClause);

    final SearchParameters params = new SearchParameters(query, 0, 1000, null, null);
    final ResultList<Metadata> result = searchService.find(params, Metadata.class);
    return result.getItems();
  }
}

