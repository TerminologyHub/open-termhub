/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.integrationtest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.Application;
import com.wci.termhub.syndication.SyndicationClient;
import com.wci.termhub.syndication.SyndicationFeed;
import com.wci.termhub.syndication.SyndicationFeedEntry;
import com.wci.termhub.util.FileUtility;

/**
 * The Class SyndicationIntegrationTest.
 *
 * This test requires PROJECT_API_KEY to have a valid JWT token for the syndication service. It will
 * download a sample syndication feed, verify there are contents. Loading terminologies is covered
 * in other tests.
 *
 */
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-syndication-test.properties")
public class SyndicationIntegrationTest extends AbstractSyndicationIntegrationTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(SyndicationIntegrationTest.class);

  /** The syndication url. */
  @Value("${syndication.url}")
  private String syndicationUrl;

  /** The jwt. */
  @Value("${syndication.token}")
  private String token;

  /**
   * Test syndication workflow.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSyndication() throws Exception {

    // Create syndication client
    final SyndicationClient client = new SyndicationClient(syndicationUrl, token);
    assertNotNull(client, "SyndicationClient should be created successfully");
    final SyndicationFeed feed = client.getFeed();

    // Find a suitable entry to download
    final SyndicationFeedEntry entry = findSuitableEntry(feed);
    assertNotNull(entry, "Should find a suitable entry to download");

    // Download packages
    final List<String> downloadedFiles = client.downloadAllAvailablePackages(feed);
    assertNotNull(downloadedFiles, "Downloaded files should not be null");
    assertTrue(!downloadedFiles.isEmpty(), "Should have downloaded files");

    // Verify downloaded files are valid zip files
    for (final String filePath : downloadedFiles) {
      LOGGER.info("Downloaded file: {}", filePath);
      assertTrue(isValidZipFile(filePath), "Downloaded file should be a valid zip file");

      // Extract zip file to a temporary directory
      final String extractDir = Files.createTempDirectory("termhub-test-").toString();
      FileUtility.extractZipFile(filePath, extractDir);
      LOGGER.info("Extracted files to: {}", extractDir);

      // Find the CodeSystem JSON file in the extracted directory
      final File[] files = new File(extractDir).listFiles((dir, name) -> name.endsWith("-r5.json"));
      assertNotNull(files, "Should find JSON files in extracted directory");
      assertTrue(files.length > 0, "Should find at least one JSON file");

      // skip load since it is performed in other tests

    }
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

  /**
   * Checks if is valid zip file.
   *
   * @param filePath the file path
   * @return true, if is valid zip file
   */
  private boolean isValidZipFile(final String filePath) {
    try (final ZipInputStream zis = new ZipInputStream(new FileInputStream(filePath))) {
      while ((zis.getNextEntry()) != null) {
        // Just verify we can read entries
        zis.closeEntry();
      }
      return true;
    } catch (final IOException e) {
      return false;
    }
  }
}
