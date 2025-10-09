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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.lucene.LuceneDataAccess;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.SyndicatedContent;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.CodeSystemLoaderUtil;
import com.wci.termhub.util.ConceptMapLoaderUtil;
import com.wci.termhub.util.FileUtility;
import com.wci.termhub.util.TerminologyUtility;
import com.wci.termhub.util.ThreadLocalMapper;
import com.wci.termhub.util.ValueSetLoaderUtil;

/**
 * Service for loading syndication content into the application.
 */
@Service
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${syndication.token:}')")
public class SyndicationContentLoader {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(SyndicationContentLoader.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The syndication client. */
  @Autowired
  private SyndicationClient syndicationClient;

  /** The content tracker. */
  @Autowired
  private SyndicationContentTracker contentTracker;

  /** The enable post load computations. */
  @Value("${enable.post.load.computations:false}")
  private boolean enablePostLoadComputations;

  /**
   * Load syndication content from downloaded files.
   *
   * @param downloadedFiles the downloaded file paths
   * @param contentType the content type
   * @return the number of successfully loaded files
   * @throws Exception the exception
   */
  public int loadContent(final List<String> downloadedFiles,
    final SyndicationContentType contentType) throws Exception {

    if (downloadedFiles == null || downloadedFiles.isEmpty()) {
      logger.info("No files to load for content type: {}", contentType);
      return 0;
    }

    logger.info("Loading {} files for content type: {}", downloadedFiles.size(), contentType);

    int successCount = 0;
    int errorCount = 0;

    for (final String filePath : downloadedFiles) {
      try {
        loadSingleFile(filePath, contentType);
        successCount++;
        logger.info("Successfully loaded file: {}", filePath);
      } catch (final Exception e) {
        errorCount++;
        logger.error("Failed to load file: {}", filePath, e);
        // Continue with other files even if one fails
      }
    }

    // Clear Lucene readers after loading
    LuceneDataAccess.clearReaders();

    logger.info("Content loading completed - Success: {}, Errors: {}", successCount, errorCount);
    return successCount;
  }

  /**
   * Load a single file based on content type.
   *
   * @param filePath the file path
   * @param contentType the content type
   * @throws Exception the exception
   */
  private void loadSingleFile(final String filePath, final SyndicationContentType contentType)
    throws Exception {

    final File file = new File(filePath);
    if (!file.exists()) {
      throw new IOException("File does not exist: " + filePath);
    }

    switch (contentType) {
      case CODESYSTEM:
        loadCodeSystem(file);
        break;
      case VALUESET:
        loadValueSet(file);
        break;
      case CONCEPTMAP:
        loadConceptMap(file);
        break;
      default:
        throw new IllegalArgumentException("Unknown content type: " + contentType);
    }
  }

  /**
   * Load CodeSystem content.
   *
   * @param file the file
   * @throws Exception the exception
   */
  private void loadCodeSystem(final File file) throws Exception {
    logger.debug("Loading CodeSystem content");

    CodeSystemLoaderUtil.loadCodeSystem(searchService, file, enablePostLoadComputations,
        org.hl7.fhir.r5.model.CodeSystem.class, new DefaultProgressListener());
  }

  /**
   * Load ValueSet content.
   *
   * @param file the file
   * @throws Exception the exception
   */
  private void loadValueSet(final File file) throws Exception {
    logger.debug("Loading ValueSet content");
    ValueSetLoaderUtil.loadValueSet(searchService, file, org.hl7.fhir.r5.model.ValueSet.class,
        new DefaultProgressListener());
  }

  /**
   * Load ConceptMap content.
   *
   * @param file the file
   * @throws Exception the exception
   */
  private void loadConceptMap(final File file) throws Exception {
    logger.debug("Loading ConceptMap content");
    ConceptMapLoaderUtil.loadConceptMap(searchService, file, org.hl7.fhir.r5.model.ConceptMap.class,
        new DefaultProgressListener());
  }

  /**
   * Normalize a version string: if it contains "/version/", return the suffix;
   * if it is a URL, return the last path segment; otherwise return as-is.
   *
   * @param version the version string
   * @return normalized version
   */
  private String normalizeVersion(final String version) {
    if (version == null) {
      return null;
    }
    final String v = version.trim();
    final int idx = v.indexOf("/version/");
    if (idx >= 0) {
      return v.substring(idx + "/version/".length());
    }
    if (v.startsWith("http")) {
      final int slash = v.lastIndexOf('/');
      if (slash >= 0 && slash < v.length() - 1) {
        return v.substring(slash + 1);
      }
    }
    return v;
  }

  /**
   * Load syndication entries by content type.
   *
   * @param entries the syndication feed entries
   * @param contentType the content type
   * @param syndicationClient the syndication client for downloading
   * @return the number of successfully loaded entries
   * @throws Exception the exception
   */
  public int loadEntriesByContentType(final List<SyndicationFeedEntry> entries,
    final SyndicationContentType contentType, final SyndicationClient syndicationClient)
    throws Exception {

    if (entries == null || entries.isEmpty()) {
      logger.info("No entries to load for content type: {}", contentType);
      return 0;
    }

    logger.info("Loading {} entries for content type: {}", entries.size(), contentType);

    int successCount = 0;
    int errorCount = 0;

    for (final SyndicationFeedEntry entry : entries) {
      try {
        loadSingleEntry(entry, contentType, syndicationClient);
        successCount++;
        logger.info("Successfully loaded entry: {} - {}", entry.getContentItemIdentifier(),
            entry.getTitle());
      } catch (final Exception e) {
        errorCount++;
        logger.error("Failed to load entry: {} - {}", entry.getContentItemIdentifier(),
            entry.getTitle(), e);
        // Continue with other entries even if one fails
      }
    }

    // Clear Lucene readers after loading
    LuceneDataAccess.clearReaders();

    logger.info("Entry loading completed - Success: {}, Errors: {}", successCount, errorCount);
    return successCount;
  }

  /**
   * Load a single syndication entry.
   *
   * @param entry the syndication feed entry
   * @param contentType the content type
   * @param syndicationClient the syndication client for downloading
   * @throws Exception the exception
   */
  private void loadSingleEntry(final SyndicationFeedEntry entry,
    final SyndicationContentType contentType, final SyndicationClient syndicationClient)
    throws Exception {

    if (entry.getZipLink() == null) {
      throw new IOException(
          "No download link available for entry: " + entry.getContentItemIdentifier());
    }

    // Download the package
    final Set<String> downloadedFiles = syndicationClient.downloadPackages(entry, null);
    if (downloadedFiles.isEmpty()) {
      throw new IOException("No files downloaded for entry: " + entry.getContentItemIdentifier());
    }

    // Load the downloaded files
    final List<String> fileList = new ArrayList<>(downloadedFiles);
    loadContent(fileList, contentType);

    // Clean up temporary files
    for (final String filePath : fileList) {
      try {
        final File file = new File(filePath);
        if (file.exists()) {
          file.delete();
        }
      } catch (final Exception e) {
        logger.warn("Failed to delete temporary file: {}", filePath, e);
      }
    }
  }

  /**
   * Load content from syndication feed entries.
   *
   * @param entries the list of syndication feed entries to load
   * @param feed the syndication feed (needed for downloading packages)
   * @return the loading results
   * @throws Exception the exception
   */
  public SyndicationResults loadContent(final List<SyndicationFeedEntry> entries,
    final SyndicationFeed feed) throws Exception {
    final long startTime = System.currentTimeMillis();
    final SyndicationResults results = new SyndicationResults();
    results.setTotalToLoad(entries.size());

    if (entries.isEmpty()) {
      logger.info("No new syndication entries to load.");
      results.setSuccess(true);
      results.setDurationMs(System.currentTimeMillis() - startTime);
      return results;
    }

    logger.info("Starting content loading for {} syndication entries.", entries.size());
    final Map<FeedKey, Set<String>> feedVersions = buildFeedVersionMap(entries);

    // Track processed entry IDs to prevent duplicates in current run
    final Set<String> processedEntryIds = new HashSet<>();

    // First pass: try to load all entries
    final List<SyndicationFeedEntry> failedEntries = new ArrayList<>();
    final List<SyndicationFeedEntry> retryEntries = new ArrayList<>();

    for (final SyndicationFeedEntry entry : entries) {
      // Skip if already processed in this run
      if (processedEntryIds.contains(entry.getId())) {
        logger.info("Skipping duplicate entry in current run: {} ({})", entry.getTitle(),
            entry.getId());
        continue;
      }

      results.incrementTotalProcessed();
      processedEntryIds.add(entry.getId());

      try {
        loadSingleEntry(entry, feed, feedVersions, results);
      } catch (final Exception e) {
        logger.warn("Failed to load entry on first attempt: {}", entry.getTitle(), e);
        failedEntries.add(entry);
        retryEntries.add(entry);
      }
    }

    // Second pass: retry failed entries
    if (!retryEntries.isEmpty()) {
      logger.info("Retrying {} failed entries...", retryEntries.size());
      final List<SyndicationFeedEntry> stillFailed = new ArrayList<>();

      for (final SyndicationFeedEntry entry : retryEntries) {
        try {
          loadSingleEntry(entry, feed, feedVersions, results);
          logger.info("Successfully loaded entry on retry: {}", entry.getTitle());
        } catch (final Exception e) {
          logger.error("Failed to load entry on retry: {}", entry.getTitle(), e);
          stillFailed.add(entry);
          results.incrementTotalErrors();
          results.addErrorMessage(
              "Failed to load " + entry.getTitle() + " after retry: " + e.getMessage());
        }
      }

      if (!stillFailed.isEmpty()) {
        logger.error("{} entries failed after retry and will be skipped", stillFailed.size());
      }
    }

    results.setSuccess(results.getTotalErrors() == 0);
    results.setDurationMs(System.currentTimeMillis() - startTime);
    logger.info("Content loading completed. Loaded: {}, Errors: {}, Duration: {} ms",
        results.getTotalLoaded(), results.getTotalErrors(), results.getDurationMs());

    // Log all SyndicatedContent records for debugging
    logAllSyndicatedContentRecords();

    return results;
  }

  /**
   * Load a single syndication entry.
   *
   * @param entry the syndication feed entry to load
   * @param feed the syndication feed (needed for downloading packages)
   * @param feedVersions the feed versions
   * @param results the loading results to update
   * @throws Exception the exception
   */
  private void loadSingleEntry(final SyndicationFeedEntry entry, final SyndicationFeed feed,
    final Map<FeedKey, Set<String>> feedVersions, final SyndicationResults results)
    throws Exception {

    final String downloadUrl = entry.getZipLink().getHref();
    final SyndicationContentType resourceType = SyndicationContentType.fromDownloadUrl(downloadUrl);

    logger.info("Downloading and loading content: {} (version: {}) from URL: {}", entry.getTitle(),
        entry.getContentItemVersion(), downloadUrl);

    // Guard by resource identity (identifier + version)
    final String identifier = entry.getContentItemIdentifier();
    final String version = entry.getContentItemVersion();
    if (contentTracker.isContentLoaded(identifier, version)) {
      logger.info("Resource already loaded: {}|{}, skipping", identifier, version);
      return;
    }

    // Check if this entry is already loaded to prevent duplicate processing
    if (contentTracker.isContentLoaded(entry.getId())) {
      logger.info("Entry already loaded, skipping: {} (ID: {})", entry.getTitle(), entry.getId());
      return;
    }

    // Download the package(s)
    final Set<String> downloadedFilePaths = syndicationClient.downloadPackages(entry, feed);
    if (downloadedFilePaths.isEmpty()) {
      throw new RuntimeException(
          "No packages downloaded for entry: " + entry.getContentItemIdentifier());
    }

    for (final String filePath : downloadedFilePaths) {
      final File downloadedFile = new File(filePath);
      if (!downloadedFile.exists()) {
        throw new RuntimeException("Downloaded file does not exist: " + filePath);
      }

      // Check if file is a ZIP file and extract if necessary
      // final JsonNode jsonFileContent;
      final File file;
      final File extractDir =
          Files.createTempDirectory("syndication-extract-" + UUID.randomUUID().toString()).toFile();
      if (downloadedFile.getName().endsWith(".zip") || isZipFile(downloadedFile)) {
        logger.info("Downloaded file is a ZIP file, extracting...");
        FileUtility.extractZipFile(downloadedFile.getAbsolutePath(), extractDir.getAbsolutePath());

        // Find the first JSON file in the extracted directory
        final File jsonFile = findFirstJsonFile(extractDir);
        if (jsonFile == null) {
          logger.warn("No JSON file found in extracted ZIP: {}. Using original downloaded file.",
              downloadedFile.getName());
          continue;
        }

        file = jsonFile;

      } else {
        // Read file content directly
        file = downloadedFile;
      }

      final SyndicationContentType actualResourceType =
          SyndicationContentType.fromResourceType(getResourceType(file));

      final SyndicationContentType effectiveResourceType =
          resourceType != null ? resourceType : actualResourceType;

      if (resourceType != null && !resourceType.equals(actualResourceType)) {
        throw new IllegalArgumentException("Content type mismatch: URL-based type is "
            + resourceType + ", but JSON file type is " + actualResourceType);
      }

      final Set<String> allowedVersions =
          feedVersions.getOrDefault(FeedKey.from(entry), Collections.emptySet());

      switch (effectiveResourceType) {
        case CODESYSTEM:
          final org.hl7.fhir.r5.model.CodeSystem loadedCodeSystem =
              CodeSystemLoaderUtil.loadCodeSystem(searchService, file, enablePostLoadComputations,
                  org.hl7.fhir.r5.model.CodeSystem.class, new DefaultProgressListener(),
                  Boolean.TRUE);
          logger.info("Successfully loaded CodeSystem from file: {}", filePath);

          // Ensure a fresh reader view for newly indexed terminology
          LuceneDataAccess.clearReaderForClass(Terminology.class);
          final String versionNormalized = normalizeVersion(loadedCodeSystem.getVersion());
          final Terminology terminologyInternal = getTerminologyWithRetry(
              loadedCodeSystem.getTitle(), loadedCodeSystem.getPublisher(), versionNormalized);
          if (terminologyInternal == null) {
            logger.warn(
                "Loaded terminology not immediately searchable; proceeding with known values: {} | {} | {}",
                loadedCodeSystem.getTitle(), loadedCodeSystem.getPublisher(), versionNormalized);
            contentTracker.markContentAsLoaded(entry, effectiveResourceType,
                loadedCodeSystem.getTitle(), loadedCodeSystem.getPublisher(), versionNormalized,
                syndicationClient.getSyndicationUrl());
            results.addResults(effectiveResourceType, 1, 0);
            break;
          }

          // Mark as loaded before cleanup to ensure idempotency on retry
          contentTracker.markContentAsLoaded(entry, effectiveResourceType,
              terminologyInternal.getAbbreviation(), terminologyInternal.getPublisher(),
              terminologyInternal.getVersion(), syndicationClient.getSyndicationUrl());

          // Cleanup other versions should not be fatal
          try {
            TerminologyUtility.removeOtherTerminologyVersions(searchService, terminologyInternal,
                allowedVersions);
          } catch (final Exception e) {
            logger.warn("Cleanup failed removing other versions for {}: {}",
                terminologyInternal.getAbbreviation(), e.getMessage());
          }
          results.addResults(effectiveResourceType, 1, 0);
          break;
        case VALUESET:
          final org.hl7.fhir.r5.model.ValueSet loadedSubset = ValueSetLoaderUtil.loadValueSet(
              searchService, file, org.hl7.fhir.r5.model.ValueSet.class,
              new DefaultProgressListener(), Boolean.TRUE);
          logger.info("Successfully loaded ValueSet from file: {}", filePath);

          // Ensure a fresh reader view for newly indexed subsets
          LuceneDataAccess.clearReaderForClass(Subset.class);
          final String versionNormalizedSubset = normalizeVersion(loadedSubset.getVersion());
          final Subset subsetInternal = getSubsetWithRetry(loadedSubset.getTitle(),
              loadedSubset.getPublisher(), versionNormalizedSubset);
          if (subsetInternal == null) {
            logger.warn(
                "Loaded subset not immediately searchable; proceeding with known values: {} | {} | {}",
                loadedSubset.getTitle(), loadedSubset.getPublisher(), versionNormalizedSubset);
            contentTracker.markContentAsLoaded(entry, effectiveResourceType,
                loadedSubset.getTitle(), loadedSubset.getPublisher(), versionNormalizedSubset,
                syndicationClient.getSyndicationUrl());
            results.addResults(effectiveResourceType, 1, 0);
            break;
          }

          // Mark as loaded before cleanup to ensure idempotency on retry
          contentTracker.markContentAsLoaded(entry, effectiveResourceType,
              subsetInternal.getAbbreviation(), subsetInternal.getPublisher(),
              subsetInternal.getVersion(), syndicationClient.getSyndicationUrl());

          // Cleanup other versions should not be fatal
          try {
            TerminologyUtility.removeOtherSubsetVersions(searchService, subsetInternal,
                allowedVersions);
          } catch (final Exception e) {
            logger.warn("Cleanup failed removing other versions for {}: {}",
                subsetInternal.getAbbreviation(), e.getMessage());
          }
          results.addResults(effectiveResourceType, 1, 0);
          break;
        case CONCEPTMAP:
          final org.hl7.fhir.r5.model.ConceptMap loadedConceptMap = ConceptMapLoaderUtil
              .loadConceptMap(searchService, file, org.hl7.fhir.r5.model.ConceptMap.class,
                  new DefaultProgressListener(), Boolean.TRUE);
          logger.info("Successfully loaded ConceptMap from file: {}", filePath);

          // Ensure a fresh reader view for newly indexed mapsets
          LuceneDataAccess.clearReaderForClass(Mapset.class);
          final String versionNormalizedMapset = normalizeVersion(loadedConceptMap.getVersion());
          final Mapset mapsetInternal = getMapsetWithRetry(loadedConceptMap.getTitle(),
              loadedConceptMap.getPublisher(), versionNormalizedMapset);
          if (mapsetInternal == null) {
            logger.warn(
                "Loaded mapset not immediately searchable; proceeding with known values: {} | {} | {}",
                loadedConceptMap.getTitle(), loadedConceptMap.getPublisher(),
                versionNormalizedMapset);
            contentTracker.markContentAsLoaded(entry, effectiveResourceType,
                loadedConceptMap.getTitle(), loadedConceptMap.getPublisher(),
                versionNormalizedMapset, syndicationClient.getSyndicationUrl());
            results.addResults(effectiveResourceType, 1, 0);
            break;
          }

          // Mark as loaded before cleanup to ensure idempotency on retry
          contentTracker.markContentAsLoaded(entry, effectiveResourceType,
              mapsetInternal.getAbbreviation(), mapsetInternal.getPublisher(),
              mapsetInternal.getVersion(), syndicationClient.getSyndicationUrl());

          // Cleanup other versions should not be fatal
          try {
            TerminologyUtility.removeOtherMapsetVersions(searchService, mapsetInternal,
                allowedVersions);
          } catch (final Exception e) {
            logger.warn("Cleanup failed removing other versions for {}: {}",
                mapsetInternal.getAbbreviation(), e.getMessage());
          }
          results.addResults(effectiveResourceType, 1, 0);
          break;
        default:
          throw new IllegalArgumentException(
              "Unsupported resource type: " + effectiveResourceType + " for " + entry.getTitle());
      }
      results.incrementTotalLoaded();

      // Clean up temporary file and extract directory
      if (!file.delete()) {
        logger.warn("Failed to delete temporary file: {}", filePath);
      }
      FileUtils.deleteDirectory(extractDir);
    }
    // Clear Lucene readers after loading to ensure new content is visible
    LuceneDataAccess.clearReaders();
  }

  /**
   * Log all SyndicatedContent records for debugging.
   */
  private void logAllSyndicatedContentRecords() {
    try {
      logger.info("ALL SYNDICATED CONTENT RECORDS ===");
      final SearchParameters searchParams = new SearchParameters("*", 1000, 0);
      final ResultList<SyndicatedContent> results =
          searchService.find(searchParams, SyndicatedContent.class);

      logger.info("Total SyndicatedContent records found: {}", results.getTotal());

      for (final SyndicatedContent content : results.getItems()) {
        logger.info("SyndicatedContent - {}", content);
      }
      logger.info("END SYNDICATED CONTENT RECORDS ===");
    } catch (final Exception e) {
      logger.error("Error logging SyndicatedContent records", e);
    }
  }

  /**
   * Check if a file is a ZIP file by examining its content.
   *
   * @param file the file to check
   * @return true if the file appears to be a ZIP file
   */
  private boolean isZipFile(final File file) {
    try (final FileInputStream fis = new FileInputStream(file)) {
      final byte[] header = new byte[2];
      final int bytesRead = fis.read(header);
      return bytesRead >= 2 && header[0] == 'P' && header[1] == 'K';
    } catch (final IOException e) {
      logger.warn("Error checking if file is ZIP: {}", file.getName(), e);
      return false;
    }
  }

  /**
   * Retry helper for terminology lookup.
   *
   * @param abbr the abbr
   * @param publisher the publisher
   * @param version the version
   * @return the terminology with retry
   * @throws Exception the exception
   */
  private Terminology getTerminologyWithRetry(final String abbr, final String publisher,
    final String version) throws Exception {
    for (int i = 0; i < 5; i++) {
      try {
        LuceneDataAccess.clearReaderForClass(Terminology.class);
        final Terminology t =
            TerminologyUtility.getTerminology(searchService, abbr, publisher, version);
        if (t != null) {
          return t;
        }
        Thread.sleep(150);
      } catch (final Exception e) {
        Thread.sleep(150);
      }
    }
    return null;
  }

  /**
   * Retry helper for subset lookup.
   *
   * @param title the title
   * @param publisher the publisher
   * @param version the version
   * @return the subset with retry
   * @throws Exception the exception
   */
  private Subset getSubsetWithRetry(final String title, final String publisher,
    final String version) throws Exception {
    for (int i = 0; i < 5; i++) {
      try {
        LuceneDataAccess.clearReaderForClass(Subset.class);
        final Subset s = TerminologyUtility.getSubset(searchService, title, publisher, version);
        if (s != null) {
          return s;
        }
        Thread.sleep(150);
      } catch (final Exception e) {
        Thread.sleep(150);
      }
    }
    return null;
  }

  /**
   * Retry helper for mapset lookup.
   *
   * @param title the title
   * @param publisher the publisher
   * @param version the version
   * @return the mapset with retry
   * @throws Exception the exception
   */
  private Mapset getMapsetWithRetry(final String title, final String publisher,
    final String version) throws Exception {
    for (int i = 0; i < 5; i++) {
      try {
        LuceneDataAccess.clearReaderForClass(Mapset.class);
        final Mapset m = TerminologyUtility.getMapset(searchService, title, publisher, version);
        if (m != null) {
          return m;
        }
        Thread.sleep(150);
      } catch (final Exception e) {
        Thread.sleep(150);
      }
    }
    return null;
  }

  /**
   * Find the first JSON file in a directory.
   *
   * @param directory the directory to search
   * @return the first JSON file found, or null if none found
   */
  private File findFirstJsonFile(final File directory) {
    if (!directory.exists() || !directory.isDirectory()) {
      return null;
    }

    final File[] files = directory.listFiles();
    if (files == null) {
      return null;
    }

    for (final File file : files) {
      if (file.isFile() && file.getName().toLowerCase().endsWith(".json")) {
        return file;
      }
      if (file.isDirectory()) {
        final File found = findFirstJsonFile(file);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  /**
   * Read file.
   *
   * @param jsonFile the json file
   * @return the json node
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private JsonNode readFile(final File jsonFile) throws Exception {
    try (final FileInputStream fis = new FileInputStream(jsonFile);
        final JsonParser parser = ThreadLocalMapper.get().getFactory().createParser(fis)) {
      return parser.readValueAsTree();
    }
  }

  /**
   * Gets the resource type.
   *
   * @param file the file
   * @return the resource type
   * @throws Exception the exception
   */
  private String getResourceType(final File file) throws Exception {

    // Use try-with-resources to ensure the parser is closed
    try (JsonParser parser = ThreadLocalMapper.get().getFactory().createParser(file)) {

      // The first token should be the start of the root object: {
      if (parser.nextToken() != JsonToken.START_OBJECT) {
        throw new IOException("Expected start of object at the root of the file.");
      }

      // Create a node to build the new JSON tree, skipping the concepts array
      @SuppressWarnings("unused")
      final JsonNode newRoot = ThreadLocalMapper.get().createObjectNode();

      while (parser.nextToken() != JsonToken.END_OBJECT) {
        final String fieldName = parser.currentName();

        // Advance the parser past the field name
        parser.nextToken();

        if ("resourceType".equals(fieldName)) {
          return parser.getValueAsString();
        }
      }

      throw new Exception("Unable to find resourceType field");
    }
  }

  /**
   * Builds the feed version map.
   *
   * @param entries the entries
   * @return the map
   */
  private Map<FeedKey, Set<String>> buildFeedVersionMap(final List<SyndicationFeedEntry> entries) {
    final Map<FeedKey, Set<String>> map = new HashMap<>();
    for (final SyndicationFeedEntry entry : entries) {
      map.computeIfAbsent(FeedKey.from(entry), key -> new LinkedHashSet<>())
          .add(entry.getContentItemVersion());
    }
    return map;
  }

  /**
   * The Class FeedKey.
   */
  private static final class FeedKey {

    /** The type. */
    private final SyndicationContentType type;

    /** The identifier. */
    private final String identifier;

    /**
     * Instantiates a new feed key.
     *
     * @param type the type
     * @param identifier the identifier
     */
    private FeedKey(final SyndicationContentType type, final String identifier) {
      this.type = type;
      this.identifier = identifier;
    }

    /**
     * From.
     *
     * @param entry the entry
     * @return the feed key
     */
    static FeedKey from(final SyndicationFeedEntry entry) {
      final SyndicationContentType type =
          SyndicationContentType.fromDownloadUrl(entry.getZipLink().getHref());
      final String identifier = entry.getContentItemIdentifier();
      return new FeedKey(type, identifier);
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof FeedKey)) {
        return false;
      }
      final FeedKey other = (FeedKey) obj;
      return type == other.type && Objects.equals(identifier, other.identifier);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
      return Objects.hash(type, identifier);
    }
  }
}
