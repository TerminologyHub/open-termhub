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
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class SyndicationClient {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(SyndicationClient.class);

  // /** The Constant acceptablePackageTypes. */
  // public static final Set<String> acceptablePackageTypes =
  // Set.of("SCT_RF2_SNAPSHOT", "SCT_RF2_FULL", "SCT_RF2_ALL");

  /** The rest template. */
  private final RestTemplate restTemplate;

  /** The jaxb context. */
  private final JAXBContext jaxbContext;

  /** The jwt. */
  private final String token;

  /**
   * Instantiates a new syndication client.
   *
   * @param url the url
   * @param token the token
   * @throws JAXBException the JAXB exception
   */
  public SyndicationClient(@Qualifier("syndicationUrl") final String url,
      @Qualifier("syndicationToken") final String token) throws JAXBException {

    restTemplate = new RestTemplateBuilder().rootUri(url)
        .messageConverters(new StringHttpMessageConverter()).build();
    jaxbContext = JAXBContext.newInstance(SyndicationFeed.class);
    this.token = token;

  }

  /**
   * Gets the feed.
   *
   * @return the feed
   * @throws Exception the exception
   */
  public SyndicationFeed getFeed() throws Exception {

    logger.info("Loading syndication feed");
    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
    headers.setBearerAuth(getSyndicationCredentials());

    final ResponseEntity<String> response = restTemplate.exchange("/terminology/syndication/feed",
        HttpMethod.GET, new HttpEntity<>(headers), String.class);

    return parseFeed(response.getBody());
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
      final List<SyndicationFeedEntry> sortedEntries = new ArrayList<>(feed.getEntries());
      sortedEntries.sort(Comparator.comparing(SyndicationFeedEntry::getContentItemVersion,
          Comparator.reverseOrder()));
      feed.setEntries(sortedEntries);
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
      if (category != null) {
        // final String categoryString = category.getTerm();
        if (zipLink != null // &&
                            // acceptablePackageTypes.contains(categoryString)
            && (entry.getContentItemVersion().equals(loadVersionUri)
                || entry.getContentItemIdentifier().equals(loadVersionUri))) {

          logger.info("Found entry to load {}", entry.getContentItemVersion());
          return entry;
        }
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
  public Set<String> downloadPackages(final SyndicationFeedEntry entry, final SyndicationFeed feed)
    throws RestException, Exception {

    final String syndicationCredentials = getSyndicationCredentials();
    final Set<Pair<SyndicationFeedEntry, SyndicationLink>> packageUrls = new LinkedHashSet<>();
    gatherPackageUrls(entry.getContentItemVersion(), feed.getEntries(), packageUrls);
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
          logger.info("Downloading package {} file {}",
              packageEntry.getFirst().getContentItemVersion(), packageLink.getHref());

          // Test credentials and download link using OPTIONS request
          final HttpHeaders headers = new HttpHeaders();
          headers.setBearerAuth(syndicationCredentials);
          restTemplate.exchange(packageLink.getHref(), HttpMethod.OPTIONS,
              new HttpEntity<Void>(headers), Void.class);

          final File outputFile =
              Files.createTempFile(UUID.randomUUID().toString(), ".zip").toFile();
          restTemplate.execute(packageLink.getHref(), HttpMethod.GET, request -> {
            request.getHeaders().setBearerAuth(syndicationCredentials);
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
                    "Download progress: %s%%");
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
        throw new RestException(false, e.getStatusCode().value(), "HTTP_ERROR",
            e.getResponseBodyAsString());
      }
      return packageFilePaths;
    }
    return Collections.emptySet();
  }

  /**
   * Gets the syndication credentials.
   *
   * @return the syndication credentials
   * @throws Exception the exception
   */
  private String getSyndicationCredentials() throws Exception {
    if (!Strings.isBlank(token)) {
      return token;
    }
    throw new Exception("Token is not set.");
  }

  /**
   * Gather package urls.
   *
   * @param loadVersionUri the load version uri
   * @param sortedEntries the sorted entries
   * @param downloadList the download list
   */
  private void gatherPackageUrls(final String loadVersionUri,
    final List<SyndicationFeedEntry> sortedEntries,
    final Set<Pair<SyndicationFeedEntry, SyndicationLink>> downloadList) {

    for (final SyndicationFeedEntry entry : sortedEntries) {
      final SyndicationLink zipLink = entry.getZipLink();
      if (zipLink != null && entry.getCategory() != null
      // && acceptablePackageTypes.contains(entry.getCategory().getTerm())
          && (entry.getContentItemVersion().equals(loadVersionUri)
              || entry.getContentItemIdentifier().equals(loadVersionUri))) {

        downloadList.add(Pair.of(entry, zipLink));

        final SyndicationDependency packageDependency = entry.getPackageDependency();
        if (packageDependency != null) {
          if (packageDependency.getEditionDependency() != null) {
            gatherPackageUrls(packageDependency.getEditionDependency(), sortedEntries,
                downloadList);
          }
          if (packageDependency.getDerivativeDependency() != null) {
            for (final String dependencyUri : packageDependency.getDerivativeDependency()) {
              gatherPackageUrls(dependencyUri, sortedEntries, downloadList);
            }
          }
        }

      }
    }
  }

  /**
   * Download all available packages from the syndication feed.
   *
   * @param feed the feed
   * @return the list of downloaded file paths
   * @throws RestException the rest exception
   * @throws Exception the exception
   */
  public List<String> downloadAllAvailablePackages(final SyndicationFeed feed)
    throws RestException, Exception {

    final List<String> downloadedFiles = new ArrayList<>();
    final String syndicationCredentials = getSyndicationCredentials();

    for (final SyndicationFeedEntry entry : feed.getEntries()) {
      final SyndicationLink zipLink = entry.getZipLink();
      // final SyndicationCategory category = entry.getCategory();
      // && acceptablePackageTypes.contains(category.getTerm())
      if (zipLink != null) {
        // && category != null) {
        logger.info("Downloading package {} file {}", entry.getContentItemVersion(),
            zipLink.getHref());

        // Test credentials and download link using OPTIONS request
        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(syndicationCredentials);
        restTemplate.exchange(zipLink.getHref(), HttpMethod.OPTIONS, new HttpEntity<Void>(headers),
            Void.class);

        final File outputFile =
            java.nio.file.Files.createTempFile(UUID.randomUUID().toString(), ".zip").toFile();
        restTemplate.execute(zipLink.getHref(), HttpMethod.GET, request -> {
          request.getHeaders().setBearerAuth(syndicationCredentials);
        }, clientHttpResponse -> {
          try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            final String lengthString = zipLink.getLength();
            int length;
            if (lengthString == null || lengthString.isEmpty()) {
              length = 1024 * 500;
            } else {
              length = Integer.parseInt(lengthString.replace(",", ""));
            }
            try {
              StreamUtility.copyWithProgress(clientHttpResponse.getBody(), outputStream, length,
                  "Download progress: %s%%");
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
}
