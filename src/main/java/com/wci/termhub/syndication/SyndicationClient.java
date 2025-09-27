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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.wci.termhub.rest.RestException;
import com.wci.termhub.util.StreamUtility;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * The Class SyndicationClient.
 */
@Service
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${syndication.token:}')")
public class SyndicationClient {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(SyndicationClient.class);

  /** The rest template. */
  private final RestTemplate restTemplate;

  /** The jaxb context. */
  private final JAXBContext jaxbContext;

  /** The jwt. */
  private final String token;

  /** The syndication URL. */
  private final String syndicationUrl;

  /**
   * Instantiates a new syndication client.
   *
   * @param url the url
   * @param token the token
   * @throws JAXBException the JAXB exception
   */
  public SyndicationClient(@Qualifier("syndicationUrl") final String url,
      @Qualifier("syndicationToken") final String token) throws JAXBException {

    this.syndicationUrl = url;
    this.token = token;
    restTemplate = new RestTemplateBuilder().rootUri(url)
        .messageConverters(new StringHttpMessageConverter()).build();
    jaxbContext = JAXBContext.newInstance(SyndicationFeed.class);

    logger.info("Syndication client configured with URL: {}", url);
    logger.info("Syndication client configured with Token: {}",
        Strings.isBlank(token) ? "No" : "Yes");

  }

  /**
   * Gets the syndication URL.
   *
   * @return the syndication URL
   */
  public String getSyndicationUrl() {
    return syndicationUrl;
  }

  /**
   * Gets the feed.
   *
   * @return the feed
   * @throws Exception the exception
   */
  public SyndicationFeed getFeed() throws Exception {

    logger.info("Fetching syndication feed");
    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
    headers.setBearerAuth(getSyndicationCredentials());

    final ResponseEntity<String> response = restTemplate.exchange("/terminology/syndication/feed",
        HttpMethod.GET, new HttpEntity<>(headers), String.class);

    final String body = response.getBody();
    if (body == null || body.isEmpty()) {
      logger.error("Failed to retrieve syndication feed: Body is null");
      throw new RuntimeException("Syndication feed body is null");
    }
    logger.info("Syndication feed content:\n{}", body);
    return parseFeed(body);
  }

  /**
   * Parses the feed.
   *
   * @param xml the xml
   * @return the syndication feed
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public SyndicationFeed parseFeed(final String xml) throws IOException {

    try {
      // Strip Atom namespace to simplify unmarshalling
      final String xmlBody = xml.replace("xmlns=\"http://www.w3.org/2005/Atom\"", "");
      final SyndicationFeed feed =
          (SyndicationFeed) jaxbContext.createUnmarshaller().unmarshal(new StringReader(xmlBody));

      logger.debug("Parsed syndication feed successfully. Found {} entries",
          feed.getEntries().size());
      for (int i = 0; i < feed.getEntries().size(); i++) {
        final SyndicationFeedEntry entry = feed.getEntries().get(i);
        logger.info("Entry {}: Title='{}', Category='{}', Version='{}', ID='{}'", i + 1,
            entry.getTitle(), entry.getCategory() != null ? entry.getCategory().getTerm() : "null",
            entry.getContentItemVersion(), entry.getContentItemIdentifier());
      }

      return feed;

    } catch (final JAXBException e) {
      throw new IOException("Failed to read XML feed.", e);
    }
  }

  /**
   * Find entry.
   *
   * @param loadVersionUri the load version uri
   * @param feed the feed
   * @return the syndication feed entry
   */
  public SyndicationFeedEntry findEntry(final String loadVersionUri, final SyndicationFeed feed) {

    for (final SyndicationFeedEntry entry : feed.getEntries()) {
      final SyndicationLink zipLink = entry.getZipLink();
      final SyndicationCategory category = entry.getCategory();
      if (category != null && zipLink != null
          && (entry.getContentItemVersion().equals(loadVersionUri)
              || entry.getContentItemIdentifier().equals(loadVersionUri))) {

        logger.info("Found entry to load {}", entry.getContentItemVersion());
        return entry;
      }
    }
    logger.warn("No matching syndication entry was found for URI {}", loadVersionUri);
    return null;
  }

  /**
   * Download packages.
   *
   * @param entry the entry
   * @param feed the feed
   * @return the sets the
   * @throws RestException the rest exception
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public Set<String> downloadPackages(final SyndicationFeedEntry entry, final SyndicationFeed feed)
    throws RestException, Exception {

    final String syndicationCredentials = getSyndicationCredentials();
    final Set<Pair<SyndicationFeedEntry, SyndicationLink>> packageUrls = new LinkedHashSet<>();
    // Only download the specific entry, not all entries with matching versions
    final SyndicationLink zipLink = entry.getZipLink();
    if (zipLink != null && entry.getCategory() != null) {
      packageUrls.add(Pair.of(entry, zipLink));
    }
    if (!packageUrls.isEmpty()) {
      final Set<String> packageFilePaths = new HashSet<>();
      logger.info("Matched the following packages:");
      for (final Pair<SyndicationFeedEntry, SyndicationLink> packageEntry : packageUrls) {
        logger.info("Syndication packages {}, {}", packageEntry.getFirst().getTitle(),
            packageEntry.getFirst().getContentItemVersion());
      }
      try {
        for (final Pair<SyndicationFeedEntry, SyndicationLink> packageEntry : packageUrls) {
          final SyndicationLink packageLink = packageEntry.getSecond();
          final String normalizedUrl = normalizeUrl(packageLink.getHref());
          logger.info("Downloading package {} file {} (normalized: {})",
              packageEntry.getFirst().getContentItemVersion(), packageLink.getHref(),
              normalizedUrl);

          // Test credentials and download link using OPTIONS request
          final HttpHeaders headers = new HttpHeaders();
          headers.setBearerAuth(syndicationCredentials);
          headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
          logger.debug("Testing credentials with OPTIONS request to: {}", normalizedUrl);
          logger.debug("Using credentials: {}", syndicationCredentials != null ? "***"
              + syndicationCredentials.substring(Math.max(0, syndicationCredentials.length() - 4))
              : "null");
          restTemplate.exchange(normalizedUrl, HttpMethod.OPTIONS, new HttpEntity<Void>(headers),
              Void.class);

          final File outputFile =
              Files.createTempFile(UUID.randomUUID().toString(), ".zip").toFile();
          restTemplate.execute(normalizedUrl, HttpMethod.GET, request -> {
            request.getHeaders().setBearerAuth(syndicationCredentials);
            request.getHeaders()
                .setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
          }, clientHttpResponse -> {
            try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {
              final String lengthString = packageLink.getLength();
              int length;
              if (lengthString == null || lengthString.isEmpty()) {
                length = 1024 * 500;
              } else {
                length = Integer.parseInt(lengthString.replace(",", ""));
              }
              try {
                StreamUtility.copyWithProgress(clientHttpResponse.getBody(), outputStream, length,
                    "Download progress = ");
              } catch (final Exception e) {
                logger.error("Failed to download file from syndication service.", e);
              }
            }
            return outputFile;
          });
          outputFile.deleteOnExit();
          packageFilePaths.add(outputFile.getAbsolutePath());
        }
      } catch (final HttpClientErrorException e) {
        logger.error("HTTP error downloading package: Status={}, Response={}",
            e.getStatusCode().value(), e.getResponseBodyAsString(), e);
        throw new RestException(false, e.getStatusCode().value(), "HTTP_ERROR",
            e.getResponseBodyAsString());
      }
      return packageFilePaths;
    }
    return Collections.emptySet();
  }

  /**
   * Normalize URL by converting format parameter to lowercase.
   *
   * @param url the original URL
   * @return the normalized URL
   */
  private String normalizeUrl(final String url) {
    if (url == null) {
      return null;
    }
    // Convert format=R5 to format=r5 and format=R4 to format=r4
    return url.replaceAll("format=R([45])", "format=r$1");
  }

  /**
   * Gets the syndication credentials.
   *
   * @return the syndication credentials
   * @throws Exception the exception
   */
  private String getSyndicationCredentials() throws Exception {
    if (!Strings.isBlank(token)) {
      logger.debug("Using syndication token: ***{}",
          token.substring(Math.max(0, token.length() - 4)));
      return token;
    }
    logger.error("Syndication token is not set. "
        + "Please set the PROJECT_API_KEY environment variable or syndication.token property.");
    throw new Exception("Syndication token is not set. "
        + "Please set the PROJECT_API_KEY environment variable or syndication.token property.");
  }

  /**
   * Download all available packages from the syndication feed.
   *
   * @param feed the feed
   * @return the list of downloaded file paths
   * @throws RestException the rest exception
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public List<String> downloadAllAvailablePackages(final SyndicationFeed feed)
    throws RestException, Exception {

    final List<String> downloadedFiles = new ArrayList<>();
    final String syndicationCredentials = getSyndicationCredentials();

    for (final SyndicationFeedEntry entry : feed.getEntries()) {
      final SyndicationLink zipLink = entry.getZipLink();
      if (zipLink != null) {
        final String normalizedUrl = normalizeUrl(zipLink.getHref());
        logger.info("Downloading package {} file {} (normalized: {})",
            entry.getContentItemVersion(), zipLink.getHref(), normalizedUrl);

        // Test credentials and download link using OPTIONS request
        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(syndicationCredentials);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
        restTemplate.exchange(normalizedUrl, HttpMethod.OPTIONS, new HttpEntity<Void>(headers),
            Void.class);

        final File outputFile = Files.createTempFile(UUID.randomUUID().toString(), ".zip").toFile();
        restTemplate.execute(normalizedUrl, HttpMethod.GET, request -> {
          request.getHeaders().setBearerAuth(syndicationCredentials);
          request.getHeaders()
              .setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
        }, clientHttpResponse -> {
          try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            final String lengthString = zipLink.getLength();
            final int length = (lengthString == null || lengthString.isEmpty()) ? 1024 * 500
                : Integer.parseInt(lengthString.replace(",", ""));
            try {
              StreamUtility.copyWithProgress(clientHttpResponse.getBody(), outputStream, length,
                  "Download progress = ");
            } catch (final Exception e) {
              logger.error("Failed to download file from syndication service.", e);
            }
          }
          return outputFile;
        });
        outputFile.deleteOnExit();
        downloadedFiles.add(outputFile.getAbsolutePath());
      }
    }
    return downloadedFiles;
  }

  /**
   * Get syndication feed entries filtered by content type.
   *
   * @param feed the syndication feed
   * @param contentType the content type to filter by
   * @return the filtered entries
   */
  public List<SyndicationFeedEntry> getEntriesByContentType(final SyndicationFeed feed,
    final SyndicationContentType contentType) {

    if (feed == null || feed.getEntries() == null || contentType == null) {
      return Collections.emptyList();
    }

    final List<SyndicationFeedEntry> filteredEntries = new ArrayList<>();
    for (final SyndicationFeedEntry entry : feed.getEntries()) {
      if (isEntryOfContentType(entry, contentType)) {
        filteredEntries.add(entry);
      }
    }

    logger.debug("Filtered {} entries for content type {}", filteredEntries.size(), contentType);
    return filteredEntries;
  }

  /**
   * Get syndication feed entries that are new (not already loaded).
   *
   * @param feed the syndication feed
   * @param contentTracker the content tracker for duplicate checking
   * @return the new entries
   * @throws Exception the exception
   */
  public List<SyndicationFeedEntry> getNewEntries(final SyndicationFeed feed,
    final SyndicationContentTracker contentTracker) throws Exception {

    if (feed == null || feed.getEntries() == null || contentTracker == null) {
      return Collections.emptyList();
    }

    final List<SyndicationFeedEntry> newEntries = new ArrayList<>();
    logger.info("Checking {} total entries for new content", feed.getEntries().size());

    for (final SyndicationFeedEntry entry : feed.getEntries()) {
      final boolean isNew = isNewEntry(entry, contentTracker);
      logger.info("Entry {} - isNew: {}", entry.getContentItemIdentifier(), isNew);
      if (isNew) {
        newEntries.add(entry);
      }
    }

    logger.info("Found {} new entries out of {} total entries", newEntries.size(),
        feed.getEntries().size());
    return newEntries;
  }

  /**
   * Get syndication feed entries that are new and of specific content type.
   *
   * @param feed the syndication feed
   * @param contentType the content type to filter by
   * @param contentTracker the content tracker for duplicate checking
   * @return the new entries of the specified type
   * @throws Exception the exception
   */
  public List<SyndicationFeedEntry> getNewEntriesByContentType(final SyndicationFeed feed,
    final SyndicationContentType contentType, final SyndicationContentTracker contentTracker)
    throws Exception {

    if (feed == null || feed.getEntries() == null || contentType == null
        || contentTracker == null) {
      return Collections.emptyList();
    }

    final List<SyndicationFeedEntry> newEntries = new ArrayList<>();
    final int totalEntries = feed.getEntries().size();
    int matchingTypeEntries = 0;

    logger.debug("Filtering {} total entries for content type: {}", totalEntries, contentType);

    for (final SyndicationFeedEntry entry : feed.getEntries()) {
      final boolean isCorrectType = isEntryOfContentType(entry, contentType);
      if (isCorrectType) {
        matchingTypeEntries++;
        logger.debug("Entry {} is of type {} - checking if new", entry.getContentItemIdentifier(),
            contentType);
        if (isNewEntry(entry, contentTracker)) {
          newEntries.add(entry);
          logger.debug("Entry {} is NEW - adding to load list", entry.getContentItemIdentifier());
        } else {
          logger.debug("Entry {} is ALREADY LOADED - skipping", entry.getContentItemIdentifier());
        }
      }
    }

    logger.debug("Found {} new {} entries out of {} matching type entries ({} total entries)",
        newEntries.size(), contentType, matchingTypeEntries, totalEntries);
    return newEntries;
  }

  /**
   * Check if an entry is of the specified content type.
   *
   * @param entry the syndication feed entry
   * @param resourceType the resource type
   * @return true if the entry is of the specified type
   */
  private boolean isEntryOfContentType(final SyndicationFeedEntry entry,
    final SyndicationContentType resourceType) {
    if (entry == null || entry.getCategory() == null || resourceType == null) {
      return false;
    }

    final String category = entry.getCategory().getTerm();
    if (category == null) {
      return false;
    }

    return resourceType.getResourceType().equalsIgnoreCase(category);
  }

  /**
   * Check if an entry is new (not already loaded).
   *
   * @param entry the syndication feed entry
   * @param contentTracker the content tracker for duplicate checking
   * @return true if the entry is new
   * @throws Exception the exception
   */
  private boolean isNewEntry(final SyndicationFeedEntry entry,
    final SyndicationContentTracker contentTracker) throws Exception {

    if (entry == null || contentTracker == null) {
      logger.info("isNewEntry returning false - entry: {}, contentTracker: {}", entry != null,
          contentTracker != null);
      return false;
    }

    final String entryId = entry.getId();
    final String contentItemIdentifier = entry.getContentItemIdentifier();
    final String contentItemVersion = entry.getContentItemVersion();

    if (entryId == null || contentItemIdentifier == null || contentItemVersion == null) {
      logger.warn(
          "Missing required syndication feed fields - entryId: {}, identifier: {}, version: {}",
          entryId, contentItemIdentifier, contentItemVersion);
      return true; // Treat as new if we can't determine
    }

    logger.info("isNewEntry - entryId: '{}', identifier: '{}', version: '{}'", entryId,
        contentItemIdentifier, contentItemVersion);

    // Check if already loaded using entry ID (most reliable)
    final boolean isLoadedByEntryId = contentTracker.isContentLoaded(entryId);
    if (isLoadedByEntryId) {
      logger.info("Entry already loaded (by entry ID): {}", entryId);
      return false;
    }

    // Also check by resource identifier + version (backup check)
    final boolean isLoadedByResource =
        contentTracker.isContentLoaded(contentItemIdentifier, contentItemVersion);
    if (isLoadedByResource) {
      logger.info("Resource already loaded (by identifier + version): {} v{}",
          contentItemIdentifier, contentItemVersion);
      return false;
    }

    logger.info("Entry is new: {}", entryId);
    return true;
  }

}
