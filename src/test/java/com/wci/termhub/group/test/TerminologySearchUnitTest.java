/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.group.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractTerminologyServerTest;
import com.wci.termhub.util.StringUtility;

/**
 * The Class TerminologySearchUnitTest.
 */
public class TerminologySearchUnitTest extends AbstractTerminologyServerTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TerminologySearchUnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The Constant SEARCH_PARAMETERS. */
  private static final SearchParameters SEARCH_PARAMETERS = new SearchParameters(1000, 0);

  /**
   * Test find all.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindAll() throws Exception {

    final ResultList<Terminology> all = searchService.findAll(SEARCH_PARAMETERS, Terminology.class);
    LOGGER.info("Find all: {}", all.getItems().size());
  }

  /**
   * Test find terminology icd 10 cm.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindTerminologyIcd10cm() throws Exception {

    SEARCH_PARAMETERS.setQuery("abbreviation: ICD10CM");
    final ResultList<Terminology> terminologies =
        searchService.find(SEARCH_PARAMETERS, Terminology.class);
    assertEquals(1, terminologies.getItems().size());
    final Terminology terminology = terminologies.getItems().get(0);
    assertEquals("ICD10CM", terminology.getAbbreviation());
    assertEquals("Mini version of ICD10CM for testing purposes", terminology.getName());
    assertEquals("2023", terminology.getVersion());
    assertEquals("SANDBOX", terminology.getPublisher());
  }

  /**
   * Test find terminology lnc.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindTerminologyLnc() throws Exception {

    SEARCH_PARAMETERS.setQuery("abbreviation: LNC");
    final ResultList<Terminology> terminologies =
        searchService.find(SEARCH_PARAMETERS, Terminology.class);
    assertEquals(1, terminologies.getItems().size());
    final Terminology terminology = terminologies.getItems().get(0);
    assertEquals("LNC", terminology.getAbbreviation());
    assertEquals("Mini version of LOINC for testing purposes", terminology.getName());
    assertEquals("277", terminology.getVersion());
    assertEquals("SANDBOX", terminology.getPublisher());
  }

  /**
   * Test find terminology rxnorm.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindTerminologyRxnorm() throws Exception {

    SEARCH_PARAMETERS.setQuery("abbreviation: RXNORM");
    final ResultList<Terminology> terminologies =
        searchService.find(SEARCH_PARAMETERS, Terminology.class);
    assertEquals(1, terminologies.getItems().size());
    final Terminology terminology = terminologies.getItems().get(0);
    assertEquals("RXNORM", terminology.getAbbreviation());
    assertEquals("Mini version of RXNORM for testing purposes", terminology.getName());
    assertEquals("04012024", terminology.getVersion());
    assertEquals("SANDBOX", terminology.getPublisher());
  }

  /**
   * Test find terminology snomed us.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindTerminologySnomedUs() throws Exception {

    SEARCH_PARAMETERS.setQuery("abbreviation: SNOMEDCT_US");
    final ResultList<Terminology> terminologies =
        searchService.find(SEARCH_PARAMETERS, Terminology.class);
    assertEquals(1, terminologies.getItems().size());
    final Terminology terminology = terminologies.getItems().get(0);
    assertEquals("SNOMEDCT_US", terminology.getAbbreviation());
    assertEquals("Mini version of SNOMEDCT_US For testing purposes", terminology.getName());
    assertEquals("20240301", terminology.getVersion());
    assertEquals("SANDBOX", terminology.getPublisher());
  }

  /**
   * Test find terminology snomed sandbox.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindTerminologySnomedSandbox() throws Exception {

    SEARCH_PARAMETERS.setQuery("abbreviation: SNOMEDCT");
    final ResultList<Terminology> terminologies =
        searchService.find(SEARCH_PARAMETERS, Terminology.class);
    assertEquals(1, terminologies.getItems().size());
    final Terminology terminology = terminologies.getItems().get(0);
    assertEquals("SNOMEDCT", terminology.getAbbreviation());
    assertEquals("Mini version of SNOMEDCT For testing purposes", terminology.getName());
    assertEquals("20240101", terminology.getVersion());
    assertEquals("SANDBOX", terminology.getPublisher());
  }

  /**
   * Test find terminology fake sandbox.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindTerminologyFake() throws Exception {

    SEARCH_PARAMETERS.setQuery("abbreviation: FAKE");
    final ResultList<Terminology> terminologies =
        searchService.find(SEARCH_PARAMETERS, Terminology.class);
    assertEquals(0, terminologies.getItems().size());
  }

  /**
   * Test publisher search.
   *
   * @throws Exception the exception
   */
  @Test
  public void testPublisherSearch() throws Exception {

    final String publisher = "SNOMEDCT International";
    // create a terminology with publisher = "SNOMEDCT International"
    final Terminology terminology = new Terminology();
    terminology.setId(UUID.randomUUID().toString());
    terminology.setAbbreviation("FAKE");
    terminology.setName("Fake Terminology for Testing");
    terminology.setVersion("http://fake.info/");
    terminology.setPublisher(publisher);
    terminology.setFamily("Fake Family");
    searchService.add(Terminology.class, terminology);

    final String query = "publisher: \"" + StringUtility.escapeQuery(publisher) + "\"";

    LOGGER.info("testPublisherSearch Query: {}", query);

    SEARCH_PARAMETERS.setQuery(query);
    final ResultList<Terminology> terminologies =
        searchService.find(SEARCH_PARAMETERS, Terminology.class);
    assertEquals(1, terminologies.getItems().size());
    assertEquals(publisher, terminologies.getItems().get(0).getPublisher());
  }

}
