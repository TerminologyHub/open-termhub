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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.SyndicatedContent;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.StringUtility;

/**
 * Service for tracking loaded syndication content using Lucene index.
 *
 * This service maintains a registry of loaded content using: - Primary Key: syndication feed entry
 * ID (UUID) - Resource Key: contentItemIdentifier + contentItemVersion
 */
@Service
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${syndication.token:}')")
public class SyndicationContentTracker {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(SyndicationContentTracker.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Initialize the tracker by checking existing tracking data in Lucene.
   */
  public void initialize() {
    try {
      final int count = getLoadedContentCount();
      logger.info("Content tracker initialized with {} loaded items in Lucene index", count);
    } catch (final Exception e) {
      logger.warn("Failed to initialize content tracker", e);
    }
  }

  /**
   * Check if content is already loaded using syndication feed entry ID.
   *
   * @param entryId the syndication feed entry ID (UUID)
   * @return true if the content is already loaded
   */
  public boolean isContentLoaded(final String entryId) {
    if (entryId == null || entryId.trim().isEmpty()) {
      logger.warn("Cannot check content - entry ID is null or empty");
      return false;
    }

    try {
      final String query = "entryId:\"" + StringUtility.escapeQuery(entryId) + "\"";
      final SearchParameters searchParams = new SearchParameters(query, 1, 0);
      final ResultList<SyndicatedContent> results =
          searchService.find(searchParams, SyndicatedContent.class);
      final boolean isLoaded = !results.getItems().isEmpty();

      logger.info("Content loaded check for entry ID '{}': {} (found {} results)", entryId,
          isLoaded, results.getItems().size());
      if (!results.getItems().isEmpty()) {
        for (final SyndicatedContent content : results.getItems()) {
          logger.info("Found existing content: entryId='{}', type='{}', title='{}'",
              content.getEntryId(), content.getContentType(), content.getTitle());
        }
      }
      return isLoaded;
    } catch (final Exception e) {
      logger.error("Error checking if content is loaded by entry ID: {}", entryId, e);
      return false;
    }
  }

  /**
   * Check if content is already loaded using resource identifier and version.
   *
   * @param contentItemIdentifier the content item identifier (e.g., URL)
   * @param contentItemVersion the content item version
   * @return true if the content is already loaded
   */
  public boolean isContentLoaded(final String contentItemIdentifier,
    final String contentItemVersion) {
    if (contentItemIdentifier == null || contentItemVersion == null) {
      logger.warn("Cannot check content - missing required parameters: identifier={}, version={}",
          contentItemIdentifier, contentItemVersion);
      return false;
    }

    try {
      final String query =
          String.format("contentItemIdentifier:\"%s\" AND contentItemVersion:\"%s\"",
              StringUtility.escapeQuery(contentItemIdentifier),
              StringUtility.escapeQuery(contentItemVersion));
      final SearchParameters searchParams = new SearchParameters(query, 1, 0);
      final ResultList<SyndicatedContent> results =
          searchService.find(searchParams, SyndicatedContent.class);
      final boolean isLoaded = !results.getItems().isEmpty();

      logger.debug("Content loaded check for resource '{}' version '{}': {}", contentItemIdentifier,
          contentItemVersion, isLoaded);
      return isLoaded;
    } catch (final Exception e) {
      logger.error("Error checking if content is loaded by resource: {} v{}", contentItemIdentifier,
          contentItemVersion, e);
      return false;
    }
  }

  /**
   * Mark content as loaded.
   *
   * @param entry the entry
   * @param contentType the content type
   * @param terminology the terminology
   * @param publisher the publisher
   * @param version the version
   * @param syndicationFeedUrl the syndication feed URL
   */
  public void markContentAsLoaded(final SyndicationFeedEntry entry,
    final SyndicationContentType contentType, final String terminology, final String publisher,
    final String version, final String syndicationFeedUrl) {

    if (entry == null) {
      logger.warn("Cannot mark content as loaded - entry is null");
      return;
    }

    if (contentType == null) {
      logger.warn("Cannot mark content as loaded - content type is null");
      return;
    }

    if (entry.getId() == null || entry.getId().isBlank()) {
      logger.warn("Cannot mark content as loaded - entry ID is null or empty");
      return;
    }

    if (entry.getContentItemIdentifier() == null || entry.getContentItemVersion() == null) {
      logger.warn(
          "Cannot mark content as loaded - missing required parameters: identifier={}, version={}",
          entry.getContentItemIdentifier(), entry.getContentItemVersion());
      return;
    }

    try {
      final SyndicatedContent content =
          new SyndicatedContent(entry.getId(), entry.getContentItemIdentifier(),
              entry.getContentItemVersion(), contentType.getResourceType(), entry.getTitle());
      content.setId(UUID.randomUUID().toString());
      content.setSyndicationFeedUrl(syndicationFeedUrl);
      content.setTerminology(terminology);
      content.setPublisher(publisher);

      searchService.add(SyndicatedContent.class, content);
      logger.info("Marked content as loaded: {} ({} v{})", entry.getTitle(),
          entry.getContentItemIdentifier(), entry.getContentItemVersion());
    } catch (final Exception e) {
      logger.error("Failed to mark content as loaded: {} ({} v{})", entry.getTitle(),
          entry.getContentItemIdentifier(), entry.getContentItemVersion(), e);
    }
  }

  /**
   * Get content item information by entry ID.
   *
   * @param entryId the syndication feed entry ID
   * @return the content item information, or null if not found
   */
  public SyndicatedContent getContentItemInfo(final String entryId) {
    try {
      final String query = "entryId:\"" + StringUtility.escapeQuery(entryId) + "\"";
      final SearchParameters searchParams = new SearchParameters(query, 1, 0);
      final ResultList<SyndicatedContent> results =
          searchService.find(searchParams, SyndicatedContent.class);
      return results.getItems().isEmpty() ? null : results.getItems().get(0);
    } catch (final Exception e) {
      logger.error("Error getting content item info for entry ID: {}", entryId, e);
      return null;
    }
  }

  /**
   * Get all loaded content items.
   *
   * @return list of all loaded content items
   */
  public List<SyndicatedContent> getAllLoadedContent() {
    try {
      final SearchParameters searchParams = new SearchParameters("*:*", 2000, 0);
      final ResultList<SyndicatedContent> results =
          searchService.find(searchParams, SyndicatedContent.class);
      return results.getItems();
    } catch (final Exception e) {
      logger.error("Error getting all loaded content", e);
      return List.of();
    }
  }

  /**
   * Get the count of loaded content items.
   *
   * @return the count of loaded content items
   */
  public int getLoadedContentCount() {
    try {
      final SearchParameters searchParams = new SearchParameters("*:*", 1, 0);
      final ResultList<SyndicatedContent> results =
          searchService.find(searchParams, SyndicatedContent.class);
      return (int) results.getTotal();
    } catch (final Exception e) {
      logger.debug("No syndicated content found in index", e);
      return 0;
    }
  }

  /**
   * Returns set of resource keys (identifier|version) for all loaded syndicated
   * content.
   *
   * @return the set of resource keys
   */
  public Set<String> getLoadedResourceKeys() {
    try {
      final SearchParameters searchParams = new SearchParameters("*:*", 2000, 0);
      final ResultList<SyndicatedContent> results =
          searchService.find(searchParams, SyndicatedContent.class);
      final Set<String> keys = new HashSet<>();
      for (final SyndicatedContent sc : results.getItems()) {
        keys.add(sc.getResourceKey());
      }
      return keys;
    } catch (final Exception e) {
      logger.error("Error getting loaded resource keys", e);
      return Collections.emptySet();
    }
  }

  /**
   * Returns list of loaded syndicated content not present in the provided feed
   * keys.
   *
   * @param feedKeys set of keys from current feed (identifier|version)
   * @return list of SyndicatedContent that are loaded but not in feed
   */
  public List<SyndicatedContent> getLoadedContentNotInFeed(final Set<String> feedKeys) {
    try {
      final SearchParameters searchParams = new SearchParameters("*:*", 2000, 0);
      final ResultList<SyndicatedContent> results =
          searchService.find(searchParams, SyndicatedContent.class);
      final List<SyndicatedContent> notInFeed = new ArrayList<>();
      for (final SyndicatedContent sc : results.getItems()) {
        if (!feedKeys.contains(sc.getResourceKey())) {
          notInFeed.add(sc);
        }
      }
      return notInFeed;
    } catch (final Exception e) {
      logger.error("Error computing loaded content not in feed", e);
      return Collections.emptyList();
    }
  }

}
