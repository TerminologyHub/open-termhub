/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.wci.termhub.syndication.SyndicationClient;
import com.wci.termhub.syndication.SyndicationFeed;
import com.wci.termhub.syndication.SyndicationFeedEntry;

/**
 * The Class SyndicationIntegrationTest.
 *
 * Parse a syndication feed file and find a suitable entry to download.
 *
 */
// Removed @SpringBootTest to make this a unit test instead of integration test
public class SyndicationFeedFileTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(SyndicationFeedFileTest.class);

  /**
   * Test parse syndication file.
   *
   * @throws Exception the exception
   */
  @Test
  public void testParseSyndicationFile() throws Exception {

    // Create syndication client
    final SyndicationClient client = new SyndicationClient("test", "test");
    assertNotNull(client, "SyndicationClient should be created successfully");

    final Resource resource = new ClassPathResource("syndication-file-termhub.xml",
        SyndicationFeedFileTest.class.getClassLoader());

    if (!resource.exists()) {
      throw new FileNotFoundException("Could not find resource: syndication-file-termhub.xml");
    }

    final String fileContent =
        FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8);

    assertNotNull(fileContent, "Syndication feed file should exist");

    // convert feed file to SyndicationFeed read file to string
    final String feedContent =
        new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
    final SyndicationFeed feed = client.parseFeed(feedContent);

    // Find a suitable entry to download
    final SyndicationFeedEntry entry = findSuitableEntry(feed);
    assertNotNull(entry, "Should find a suitable entry to download");
  }

  /**
   * Find suitable entry.
   *
   * @param feed the feed
   * @return the syndication feed entry
   */
  private SyndicationFeedEntry findSuitableEntry(final SyndicationFeed feed) {
    for (final SyndicationFeedEntry entry : feed.getEntries()) {
      if (entry.getZipLink() != null) {
        return entry;
      }
    }
    return null;
  }

}
