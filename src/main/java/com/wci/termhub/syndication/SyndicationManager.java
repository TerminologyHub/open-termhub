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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import com.wci.termhub.lucene.LuceneDataAccess;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.SyndicatedContent;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.TerminologyUtility;

/**
 * Manager service that orchestrates the entire syndication process.
 */
@Service
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${syndication.token:}')")
public class SyndicationManager {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(SyndicationManager.class);

  /** The syndication client. */
  @Autowired
  private SyndicationClient syndicationClient;

  /** The content tracker. */
  @Autowired
  private SyndicationContentTracker contentTracker;

  /** The content loader. */
  @Autowired
  private SyndicationContentLoader contentLoader;

  /** The token. */
  @Autowired
  private String syndicationToken;

  /** The search service (for deletions). */
  @Autowired
  private EntityRepositoryService searchService;

  /** The syndication check in progress lock. */
  private volatile boolean syndicationCheckInProgress = false;

  /**
   * Perform a complete syndication check and load new content.
   *
   * @return the syndication results
   * @throws Exception the exception
   */
  public SyndicationResults performSyndicationCheck() throws Exception {

    // Check if syndication check is already in progress and set lock atomically
    synchronized (this) {
      if (syndicationCheckInProgress) {
        logger.info("Syndication check is already in progress, skipping this request");
        return new SyndicationResults(false, "Syndication check already in progress", 0, 0, 0, 0);
      }
      syndicationCheckInProgress = true;
    }
    logger.info("Starting syndication check and load process");
    final long startTime = System.currentTimeMillis();

    try {
      // Step 1: Get syndication feed (with simple retry)
      SyndicationFeed feed = getSyndicationFeed();

      if (feed == null || feed.getEntries() == null || feed.getEntries().isEmpty()) {
        logger.warn("No syndication feed entries found");
        return new SyndicationResults(true, "No syndication feed entries found", 0, 0, 0, 0);
      }

      logger.info("Retrieved syndication feed with {} entries", feed.getEntries().size());
      logger.info("Syndication feed details: {}", feed.toString());

      // Build feed keys for tracking (identifier|version)
      final Set<String> feedKeys = new HashSet<>();
      for (final SyndicationFeedEntry e : feed.getEntries()) {
        if (e.getContentItemIdentifier() != null && e.getContentItemVersion() != null) {
          feedKeys.add(e.getContentItemIdentifier() + "|" + e.getContentItemVersion());
        }
      }

      // Determine loaded-but-not-in-feed (candidates for removal)
      final List<SyndicatedContent> loadedNotInFeed = contentTracker.getLoadedContentNotInFeed(feedKeys);

      logger.info("Syndication tracking: feed keys = {}, loaded-but-not-in-feed = {}",
          Integer.valueOf(feedKeys.size()), Integer.valueOf(loadedNotInFeed.size()));
      if (!loadedNotInFeed.isEmpty()) {
        final int max = Math.min(5, loadedNotInFeed.size());
        for (int i = 0; i < max; i++) {
          final SyndicatedContent sc = loadedNotInFeed.get(i);
          logger.info("To-remove candidate: {} | {} ({})", sc.getContentItemIdentifier(),
              sc.getContentItemVersion(), sc.getTitle());
        }
      }

      // NOTE: Removal of loaded-but-not-in-feed will occur AFTER loading new
      // content

      // Step 2: Filter for new content based on enabled types
      logger.info("Step 2: Filtering for new content");
      final List<SyndicationFeedEntry> newEntries = getNewEntriesForEnabledTypes(feed);
      if (newEntries.isEmpty()) {
        logger.info("No new content found to load");
        return new SyndicationResults(true, "No new content found", 0, 0, 0, 0);
      }

      logger.info("Found {} new entries to process", newEntries.size());

      // Step 3: Load new content
      logger.info("Step 3: Loading new content");
      final SyndicationResults loadingResults = contentLoader.loadContent(newEntries, feed);

      // Ensure reade
      // rs are fresh after load before any removal
      LuceneDataAccess.clearReaders();

      // Post-load removal of content no longer in the feed
      final List<SyndicatedContent> postLoadNotInFeed = contentTracker.getLoadedContentNotInFeed(feedKeys);
      if (!postLoadNotInFeed.isEmpty()) {
        removeLoadedContentNotInFeed(postLoadNotInFeed);
        // Clear again so subsequent reads see deletions
        LuceneDataAccess.clearReaders();
      }

      // Step 4: Create results summary
      final long duration = System.currentTimeMillis() - startTime;
      final SyndicationResults results = new SyndicationResults(true,
          "Syndication check completed successfully", loadingResults.getTotalProcessed(),
          loadingResults.getTotalLoaded(), loadingResults.getTotalErrors(), duration);

      results.setCodeSystemResults(loadingResults.getCodeSystemLoaded(),
          loadingResults.getCodeSystemErrors());
      results.setValueSetResults(loadingResults.getValueSetLoaded(),
          loadingResults.getValueSetErrors());
      results.setConceptMapResults(loadingResults.getConceptMapLoaded(),
          loadingResults.getConceptMapErrors());
      results.setErrorMessages(loadingResults.getErrorMessages());

      logger.info(
          "Syndication check completed successfully in {} ms - Processed: {}, Loaded: {}, Errors: {}",
          duration, loadingResults.getTotalProcessed(), loadingResults.getTotalLoaded(),
          loadingResults.getTotalErrors());

      return results;

    } catch (final Exception e) {
      final long duration = System.currentTimeMillis() - startTime;
      logger.error("Syndication check failed after {} ms", duration, e);
      return new SyndicationResults(false, "Syndication check failed: " + e.getMessage(), 0, 0, 0,
          duration);
    } finally {
      // Always release the lock
      syndicationCheckInProgress = false;
      logger.debug("Syndication check lock released");
    }
  }

  /**
   * Check if syndication check is currently in progress.
   *
   * @return true if syndication check is in progress
   */
  public boolean isSyndicationCheckInProgress() {
    return syndicationCheckInProgress;
  }

  /**
   * Get new entries for enabled content types.
   *
   * @param feed the syndication feed
   * @return the new entries for enabled types
   * @throws Exception the exception
   */
  private List<SyndicationFeedEntry> getNewEntriesForEnabledTypes(final SyndicationFeed feed)
      throws Exception {
    final List<SyndicationFeedEntry> allNewEntries = syndicationClient.getNewEntries(feed, contentTracker);
    final List<SyndicationFeedEntry> enabledNewEntries = new java.util.ArrayList<>();

    for (final SyndicationFeedEntry entry : allNewEntries) {
      if (isEntryTypeEnabled(entry)) {
        enabledNewEntries.add(entry);
      }
    }

    logger.debug("Filtered {} new entries to {} enabled entries", allNewEntries.size(),
        enabledNewEntries.size());
    return enabledNewEntries;
  }

  /**
   * Check if an entry's content type is enabled for loading.
   *
   * @param entry the syndication feed entry
   * @return true if the entry type is enabled
   */
  private boolean isEntryTypeEnabled(final SyndicationFeedEntry entry) {
    if (entry == null || entry.getCategory() == null) {
      return false;
    }

    final String category = entry.getCategory().getTerm();
    if (category == null) {
      return false;
    }

    return true;
  }

  /**
   * Get syndication status information.
   *
   * @return the syndication status
   */
  public SyndicationStatus getSyndicationStatus() {
    final boolean enabled = StringUtils.isNotBlank(syndicationToken);
    return new SyndicationStatus(enabled);
  }

  /**
   * Manual trigger for syndication check.
   *
   * @return the syndication results
   * @throws Exception the exception
   */
  public SyndicationResults triggerSyndicationCheck() throws Exception {
    logger.info("Manual syndication check triggered");
    return performSyndicationCheck();
  }

  /**
   * Syndication status information.
   */
  public static class SyndicationStatus {

    /** The enabled. */
    private final boolean enabled;

    /**
     * Instantiates a new syndication status.
     *
     * @param enabled the enabled
     */
    public SyndicationStatus(final boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Checks if is enabled.
     *
     * @return true, if is enabled
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
      return String.format("SyndicationStatus{enabled=%s}", enabled);
    }
  }

  /**
   * Gets the syndication feed.
   *
   * @return the syndication feed
   * @throws Exception the exception
   */
  private SyndicationFeed getSyndicationFeed() throws Exception {
    logger.info("Step 1: Retrieving syndication feed");
    final int maxAttempts = 3;
    final long[] delaysMs = new long[] {
        1000L, 3000L, 5000L
    };
    SyndicationFeed feed = null;
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        feed = syndicationClient.getFeed();
        break;
      } catch (final Exception e) {
        if (attempt == maxAttempts) {
          throw e;
        }
        logger.warn("Syndication feed attempt {}/{} failed: {}", Integer.valueOf(attempt),
            Integer.valueOf(maxAttempts), e.getMessage());
        try {
          Thread.sleep(delaysMs[attempt - 1]);
        } catch (final InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw e;
        }
      }
    }
    return feed;
  }

  /**
   * Remove content that is loaded but no longer present in the feed. Deletes
   * underlying resources and then removes the tracker records.
   *
   * @param loadedNotInFeed the list of loaded syndicated content not in feed
   */
  private void removeLoadedContentNotInFeed(final List<SyndicatedContent> loadedNotInFeed) {
    for (final SyndicatedContent sc : loadedNotInFeed) {

      try {
        final String terminology = sc.getTerminology();
        final String publisher = sc.getPublisher();
        final String version = sc.getContentItemVersion();
        final String type = sc.getContentType();

        loadedNotInFeed.forEach(contentToRemove -> {
          logger.info("Identified removing loaded content no longer in feed: {} | {} ({}) [{}]",
              contentToRemove.getContentItemIdentifier(), version, sc.getTitle(), type);
        });

        final SyndicationContentType contentTypeEnum = SyndicationContentType.fromResourceType(type);
        final String terminologyQuery = TerminologyUtility.getTerminologyAbbrQuery(terminology, publisher, version);
        final String query = TerminologyUtility.getTerminologyQuery(terminology, publisher, version);
        switch (contentTypeEnum) {
          case CODESYSTEM:
            final ResultList<Terminology> existingTerminologies = searchService
                .find(new SearchParameters(terminologyQuery, 100, 0), Terminology.class);
            if (!existingTerminologies.getItems().isEmpty()) {
              for (final Terminology t : existingTerminologies.getItems()) {
                TerminologyUtility.removeTerminology(searchService, t.getId());
                logger.info("Removed Terminology {}", t.getId());
              }
            }
            break;
          case VALUESET:
            final ResultList<Subset> existingValueSets = searchService.find(new SearchParameters(query, 100, 0),
                Subset.class);
            if (!existingValueSets.getItems().isEmpty()) {
              for (final Subset s : existingValueSets.getItems()) {
                TerminologyUtility.removeSubset(searchService, s.getId());
                logger.info("Removed ValueSet/Subset {}", s.getId());
              }
            }
            break;
          case CONCEPTMAP:
            final ResultList<Mapset> existingConceptMaps = searchService.find(new SearchParameters(query, 100, 0),
                Mapset.class);
            if (!existingConceptMaps.getItems().isEmpty()) {
              for (final Mapset m : existingConceptMaps.getItems()) {
                TerminologyUtility.removeMapset(searchService, m.getId());
                logger.info("Removed Mapset {}", m.getId());
              }
            }
            break;
          default:
            logger.warn("Unsupported content type for removal: {}", sc.getContentType());
            continue; // Skip unsupported types

        }

      } catch (final Exception e) {
        logger.warn("Removal failed for {} | {} ({}) [{}]: {}", sc.getContentItemIdentifier(),
            sc.getContentItemVersion(), sc.getTitle(), sc.getContentType(), e.getMessage());
      } finally {
        try {
          if (sc.getId() != null) {
            searchService.remove(sc.getId(), SyndicatedContent.class);
            logger.info("Removed SyndicatedContent tracker id={}", sc.getId());
          }
        } catch (final Exception ex) {
          logger.warn("Failed to remove SyndicatedContent tracker id={}: {}", sc.getId(),
              ex.getMessage());
        }
      }
    }
    // Ensure readers are cleared after removals
    LuceneDataAccess.clearReaders();
  }
}
