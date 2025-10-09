/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wci.termhub.rest.RestException;
import com.wci.termhub.syndication.SyndicationClient;
import com.wci.termhub.syndication.SyndicationFeed;
import com.wci.termhub.syndication.SyndicationFeedEntry;
import com.wci.termhub.syndication.SyndicationLink;
import com.wci.termhub.syndication.SyndicationLink.RelType;

/**
 * The Class MockSyndicationClient.
 */
public class MockSyndicationClient extends SyndicationClient {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(MockSyndicationClient.class);

  /** The next feed. */
  private SyndicationFeed nextFeed;

  /**
   * Instantiates a new mock syndication client.
   *
   * @throws Exception the exception
   */
  public MockSyndicationClient() throws Exception {
    super("http://mock-syndication.example.com", "mock-token");
  }

  /**
   * Sets the next feed.
   *
   * @param feed the new next feed
   */
  public void setNextFeed(final SyndicationFeed feed) {
    this.nextFeed = feed;
  }

  /**
   * Gets the feed.
   *
   * @return the feed
   * @throws Exception the exception
   */
  @Override
  public SyndicationFeed getFeed() throws Exception {
    if (nextFeed == null) {
      throw new IllegalStateException("MockSyndicationClient.nextFeed not set");
    }
    return nextFeed;
  }

  /**
   * Download packages.
   *
   * @param entry the entry
   * @param feed  the feed
   * @return the sets the
   * @throws RestException the rest exception
   * @throws Exception     the exception
   */
  @Override
  public Set<String> downloadPackages(final SyndicationFeedEntry entry, final SyndicationFeed feed)
      throws RestException, Exception {
    final Set<String> files = new HashSet<>();
    final SyndicationLink link = entry.getZipLink();
    if (link == null || link.getHref() == null) {
      return files;
    }

    final String href = link.getHref();
    if (!href.startsWith("classpath:")) {
      throw new IOException("Unsupported href: " + href);
    }

    final String resourcePath = href.substring("classpath:".length());
    String classpathPath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
    if (!classpathPath.startsWith("data/")) {
      classpathPath = "data/" + classpathPath;
    }
    final Path tempFile = Files.createTempFile("mock-syndication-", ".json");

    try (InputStream stream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(classpathPath)) {
      if (stream == null) {
        throw new IOException("Resource not found: " + classpathPath);
      }
      Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);
      normalizeVersion(tempFile, entry);
      files.add(tempFile.toAbsolutePath().toString());
    }

    LOGGER.info("Mock syndication download for {} ({}) -> {}", entry.getTitle(),
        entry.getContentItemVersion(), tempFile);
    return files;
  }

  /**
   * Download all available packages.
   *
   * @param feed the feed
   * @return the list
   * @throws RestException the rest exception
   * @throws Exception     the exception
   */
  @Override
  public List<String> downloadAllAvailablePackages(final SyndicationFeed feed)
      throws RestException, Exception {
    return Collections.emptyList();
  }

  /**
   * Normalize version.
   *
   * @param file  the file
   * @param entry the entry
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void normalizeVersion(final Path file, final SyndicationFeedEntry entry)
      throws IOException {
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode root = mapper.readTree(file.toFile());
    if (root == null || !root.isObject()) {
      return;
    }

    final ObjectNode object = (ObjectNode) root;
    if (object.has("version")) {
      object.put("version", entry.getContentItemVersion());
    }
    if (object.has("fhirVersion")) {
      object.put("fhirVersion", entry.getContentItemVersion());
    }
    Files.writeString(file, mapper.writeValueAsString(object));
  }

  /**
   * Creates the link.
   *
   * @param classpathResource the classpath resource
   * @return the syndication link
   */
  public static SyndicationLink createLink(final String classpathResource) {
    final SyndicationLink link = new SyndicationLink();
    link.setHref("classpath:" + classpathResource);
    link.setRel(RelType.related);
    link.setType("application/json");
    link.setLength("1024");
    return link;
  }
}
