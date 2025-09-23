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
import java.util.HashSet;
import java.util.List;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.wci.termhub.lucene.LuceneDataAccess;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.SyndicatedContent;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.CodeSystemLoaderUtil;
import com.wci.termhub.util.ConceptMapLoaderUtil;
import com.wci.termhub.util.FileUtility;
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

    final JsonNode jsonContent = readFile(file);

    switch (contentType) {
      case CODESYSTEM:
        loadCodeSystem(jsonContent);
        break;
      case VALUESET:
        loadValueSet(jsonContent.toString());
        break;
      case CONCEPTMAP:
        loadConceptMap(jsonContent.toString());
        break;
      default:
        throw new IllegalArgumentException("Unknown content type: " + contentType);
    }
  }

  /**
   * Load CodeSystem content.
   *
   * @param content the JSON content
   * @throws Exception the exception
   */
  private void loadCodeSystem(final JsonNode content) throws Exception {
    logger.debug("Loading CodeSystem content");
    CodeSystemLoaderUtil.loadCodeSystem(searchService, content, enablePostLoadComputations);
  }

  /**
   * Load ValueSet content.
   *
   * @param content the JSON content
   * @throws Exception the exception
   */
  private void loadValueSet(final String content) throws Exception {
    logger.debug("Loading ValueSet content");
    ValueSetLoaderUtil.loadSubset(searchService, content, true);
  }

  /**
   * Load ConceptMap content.
   *
   * @param content the JSON content
   * @throws Exception the exception
   */
  private void loadConceptMap(final String content) throws Exception {
    logger.debug("Loading ConceptMap content");
    ConceptMapLoaderUtil.loadConceptMap(searchService, content);
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

    // Track processed entry IDs to prevent duplicates in current run
    Set<String> processedEntryIds = new HashSet<>();

    // First pass: try to load all entries
    List<SyndicationFeedEntry> failedEntries = new ArrayList<>();
    List<SyndicationFeedEntry> retryEntries = new ArrayList<>();

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
        loadSingleEntry(entry, feed, results);
      } catch (final Exception e) {
        logger.warn("Failed to load entry on first attempt: {} - {}", entry.getTitle(),
            e.getMessage());
        failedEntries.add(entry);
        retryEntries.add(entry);
      }
    }

    // Second pass: retry failed entries
    if (!retryEntries.isEmpty()) {
      logger.info("Retrying {} failed entries...", retryEntries.size());
      List<SyndicationFeedEntry> stillFailed = new ArrayList<>();

      for (final SyndicationFeedEntry entry : retryEntries) {
        try {
          loadSingleEntry(entry, feed, results);
          logger.info("Successfully loaded entry on retry: {}", entry.getTitle());
        } catch (final Exception e) {
          logger.error("Failed to load entry on retry: {} - {}", entry.getTitle(), e.getMessage());
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
   * @param results the loading results to update
   * @throws Exception the exception
   */
  private void loadSingleEntry(final SyndicationFeedEntry entry, final SyndicationFeed feed,
    final SyndicationResults results) throws Exception {
    // Determine content type from download URL
    final String downloadUrl = entry.getZipLink().getHref();
    final SyndicationContentType contentType = SyndicationContentType.fromDownloadUrl(downloadUrl);
    if (contentType == null) {
      throw new Exception("Unable to determine content type = " + downloadUrl);
      // logger.warn("Using fallback content type detection for entry: {} (URL: {})",
      // entry.getTitle(),
      // downloadUrl);
    }

    logger.info("Downloading and loading {} content: {} (version: {}) from URL: {}", contentType,
        entry.getTitle(), entry.getContentItemVersion(), downloadUrl);

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
      final JsonNode jsonFileContent;
      if (downloadedFile.getName().endsWith(".zip") || isZipFile(downloadedFile)) {
        logger.info("Downloaded file is a ZIP file, extracting...");
        final File extractDir = Files
            .createTempDirectory("syndication-extract-" + UUID.randomUUID().toString()).toFile();
        FileUtility.extractZipFile(downloadedFile.getAbsolutePath(), extractDir.getAbsolutePath());

        // Find the first JSON file in the extracted directory
        final File jsonFile = findFirstJsonFile(extractDir);
        if (jsonFile == null) {
          throw new RuntimeException(
              "No JSON file found in extracted ZIP: " + downloadedFile.getName());
        }

        logger.info("Found JSON file in ZIP: {}", jsonFile.getName());
        jsonFileContent = readFile(jsonFile);

        // Clean up extracted files
        FileUtils.deleteDirectory(extractDir);
      } else {
        // Read file content directly
        jsonFileContent = readFile(downloadedFile);
      }

      // Use URL-based content type for loading (more reliable than JSON content
      // type)
      final String resourceType = contentType.getResourceType();

      // Verify JSON content type matches expected type
      final String actualResourceType = jsonFileContent.path("resourceType").asText();

      logger.info("URL-based content type: {}, JSON content type: {}", resourceType,
          actualResourceType);

      // Load content based on URL-determined resource type
      switch (resourceType) {
        case "CodeSystem":
          CodeSystemLoaderUtil.loadCodeSystem(searchService, jsonFileContent,
              enablePostLoadComputations);
          results.incrementCodeSystemsLoaded();
          logger.info("Successfully loaded CodeSystem from file: {}", filePath);
          // Mark as loaded in tracker
          contentTracker.markContentAsLoaded(entry.getId(), entry.getContentItemIdentifier(),
              entry.getContentItemVersion(), resourceType, entry.getTitle(),
              syndicationClient.getSyndicationUrl());
          break;
        case "ValueSet":
          ValueSetLoaderUtil.loadSubset(searchService, jsonFileContent.toString(), true);
          results.incrementValueSetsLoaded();
          logger.info("Successfully loaded ValueSet from file: {}", filePath);
          // Mark as loaded in tracker
          contentTracker.markContentAsLoaded(entry.getId(), entry.getContentItemIdentifier(),
              entry.getContentItemVersion(), resourceType, entry.getTitle(),
              syndicationClient.getSyndicationUrl());
          break;
        case "ConceptMap":
          ConceptMapLoaderUtil.loadConceptMap(searchService, jsonFileContent);
          results.incrementConceptMapsLoaded();
          logger.info("Successfully loaded ConceptMap from file: {}", filePath);
          // Mark as loaded in tracker
          contentTracker.markContentAsLoaded(entry.getId(), entry.getContentItemIdentifier(),
              entry.getContentItemVersion(), resourceType, entry.getTitle(),
              syndicationClient.getSyndicationUrl());
          break;
        default:
          throw new IllegalArgumentException(
              "Unsupported resource type: " + resourceType + " for " + entry.getTitle());
      }
      results.incrementTotalLoaded();

      // Clean up temporary file
      if (!downloadedFile.delete()) {
        logger.warn("Failed to delete temporary file: {}", filePath);
      }
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
  private JsonNode readFile(final File jsonFile) throws Exception {
    try (final FileInputStream fis = new FileInputStream(jsonFile);
        final JsonParser parser = ThreadLocalMapper.get().getFactory().createParser(fis)) {
      return parser.readValueAsTree();
    }
  }
}
