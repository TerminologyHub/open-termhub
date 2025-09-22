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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Manager service that orchestrates the entire syndication process.
 */
@Service
@ConditionalOnProperty(prefix = "syndication.check", name = "enabled", havingValue = "true",
    matchIfMissing = false)
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

  /** The syndication check enabled. */
  @Value("${syndication.check.enabled:true}")
  private boolean syndicationCheckEnabled;

  /** The syndication check in progress lock. */
  private volatile boolean syndicationCheckInProgress = false;

  /**
   * Perform a complete syndication check and load new content.
   *
   * @return the syndication results
   * @throws Exception the exception
   */
  public SyndicationResults performSyndicationCheck() throws Exception {
    if (!syndicationCheckEnabled) {
      logger.info("Syndication check is disabled, skipping");
      return new SyndicationResults(false, "Syndication check is disabled", 0, 0, 0, 0);
    }

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
      // Step 1: Get syndication feed
      logger.info("Step 1: Retrieving syndication feed");
      final SyndicationFeed feed = syndicationClient.getFeed();
      if (feed == null || feed.getEntries() == null || feed.getEntries().isEmpty()) {
        logger.warn("No syndication feed entries found");
        return new SyndicationResults(true, "No syndication feed entries found", 0, 0, 0, 0);
      }

      logger.info("Retrieved syndication feed with {} entries", feed.getEntries().size());
      logger.info("Syndication feed details: {}", feed.toString());

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
    final List<SyndicationFeedEntry> allNewEntries =
        syndicationClient.getNewEntries(feed, contentTracker);
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
    return new SyndicationStatus(syndicationCheckEnabled);
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
}
