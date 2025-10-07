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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.SyndicatedContent;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.syndication.SyndicationClient;
import com.wci.termhub.syndication.SyndicationFeed;
import com.wci.termhub.syndication.SyndicationManager;

/**
 * The Class SyndicationLoadReloadIntegrationTest.
 */
@Import(SyndicationLoadReloadUnitTest.MockConfig.class)
public class SyndicationLoadReloadUnitTest extends AbrstractSyndicationTest {

  /**
   * The Class MockConfig.
   */
  @TestConfiguration
  static class MockConfig {

    /**
     * Syndication client.
     *
     * @return the syndication client
     * @throws Exception the exception
     */
    @Bean
    @Primary
    SyndicationClient syndicationClient() throws Exception {
      return new com.wci.termhub.syndication.test.MockSyndicationClient();
    }
  }

  /** The syndication manager. */
  @Autowired
  private SyndicationManager syndicationManager;

  // /** The syndication client. */
  // @Autowired
  // private SyndicationClient syndicationClient;

  /** The mock client. */
  @Autowired
  private com.wci.termhub.syndication.test.MockSyndicationClient mockClient;

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Test syndication load reload with version management.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSyndicationLoadReloadWithVersionManagement() throws Exception {
    SyndicationFeed feed = buildInitialFeed();
    processFeed(feed);

    verifyTerminology("ICD10CM", "SANDBOX", "2023", true);
    verifyTerminology("RXNORM", "SANDBOX", "04012024", true);
    verifyTerminology("SNOMEDCT", "SANDBOX", "20240101", true);

    feed = buildReloadFeed();
    processFeed(feed);

    verifyTerminology("ICD10CM", "SANDBOX", "2023", false);
    verifyTerminology("ICD10CM", "SANDBOX", "2024", true);
    verifyTerminology("RXNORM", "SANDBOX", "04012024", false);
    verifyTerminology("RXNORM", "SANDBOX", "07012024", true);
    verifyTerminology("SNOMEDCT", "SANDBOX", "20240101", false);
    verifyTerminology("SNOMEDCT_US", "SANDBOX", "20240301", true);
  }

  /**
   * Builds the initial feed.
   *
   * @return the syndication feed
   */
  private SyndicationFeed buildInitialFeed() {
    final SyndicationFeed feed = new SyndicationFeed();
    feed.setEntries(new ArrayList<>());
    feed.getEntries().add(SyndicationTestDataHelper.createCodeSystemEntry("urn:uuid:icd10cm-2023",
        "ICD10CM 2023", "SANDBOX", "2023", "ICD10CM", "CodeSystem-icd10cm-sandbox-2023-r5.json"));
    feed.getEntries()
        .add(SyndicationTestDataHelper.createCodeSystemEntry("urn:uuid:rxnorm-04012024",
            "RXNORM 04012024", "SANDBOX", "04012024", "RXNORM",
            "CodeSystem-rxnorm-sandbox-04012024-r5.json"));
    feed.getEntries()
        .add(SyndicationTestDataHelper.createCodeSystemEntry("urn:uuid:snomedct-20240101",
            "SNOMEDCT 20240101", "SANDBOX", "20240101", "SNOMEDCT",
            "CodeSystem-snomedct-sandbox-20240101-r5.json"));
    return feed;
  }

  /**
   * Builds the reload feed.
   *
   * @return the syndication feed
   */
  private SyndicationFeed buildReloadFeed() {
    final SyndicationFeed feed = new SyndicationFeed();
    feed.setEntries(new ArrayList<>());
    feed.getEntries()
        .add(SyndicationTestDataHelper.createCodeSystemEntry("urn:uuid:icd10cm-2024",
            "ICD10CM 2024", "SANDBOX", "2024", "ICD10CM",
            // Use existing classpath file; version will be normalized to 2024
            // by mock
            "CodeSystem-icd10cm-sandbox-2023-r5.json"));
    feed.getEntries()
        .add(SyndicationTestDataHelper.createCodeSystemEntry("urn:uuid:rxnorm-07012024",
            "RXNORM 07012024", "SANDBOX", "07012024", "RXNORM",
            // Use existing classpath file; version will be normalized to
            // 07012024 by mock
            "CodeSystem-rxnorm-sandbox-04012024-r5.json"));
    feed.getEntries()
        .add(SyndicationTestDataHelper.createCodeSystemEntry("urn:uuid:snomedctus-20240301",
            "SNOMEDCT_US 20240301", "SANDBOX", "20240301", "SNOMEDCT_US",
            "CodeSystem-snomedct_us-sandbox-20240301-r5.json"));
    return feed;
  }

  /**
   * Process feed.
   *
   * @param feed the feed
   * @throws Exception the exception
   */
  private void processFeed(final SyndicationFeed feed) throws Exception {
    mockClient.setNextFeed(feed);
    syndicationManager.performSyndicationCheck();
  }

  /**
   * Verify terminology.
   *
   * @param abbreviation the abbreviation
   * @param publisher the publisher
   * @param version the version
   * @param shouldExist the should exist
   * @throws Exception the exception
   */
  private void verifyTerminology(final String abbreviation, final String publisher, final String version,
    final boolean shouldExist) throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setQuery(
        String.format("abbreviation:\"%s\" AND publisher:\"%s\"", abbreviation, publisher));
    final ResultList<Terminology> results = searchService.find(params, Terminology.class);

    Terminology match = null;
    for (Terminology terminology : results.getItems()) {
      if (version.equals(terminology.getVersion())) {
        match = terminology;
        break;
      }
    }

    if (shouldExist) {
      assertNotNull(match);
      assertEquals("true", match.getAttributes().get("syndicated"));

      final SearchParameters contentParams = new SearchParameters();
      contentParams.setQuery(String.format(
          "contentItemIdentifier:\"%s\" AND contentItemVersion:\"%s\"", abbreviation, version));
      final ResultList<SyndicatedContent> contentResults =
          searchService.find(contentParams, SyndicatedContent.class);
      assertTrue(contentResults.getTotal() > 0);
    } else {
      assertNull(match);
      final SearchParameters contentParams = new SearchParameters();
      contentParams.setQuery(String.format(
          "contentItemIdentifier:\"%s\" AND contentItemVersion:\"%s\"", abbreviation, version));
      final ResultList<SyndicatedContent> contentResults =
          searchService.find(contentParams, SyndicatedContent.class);
      assertEquals(0, contentResults.getTotal());
    }
  }
}
