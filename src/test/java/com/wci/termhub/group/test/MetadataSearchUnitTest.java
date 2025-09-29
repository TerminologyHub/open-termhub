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

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * The Class MetadataSearchUnitTest.
 */
public class MetadataSearchUnitTest extends AbstractTerminologyTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataSearchUnitTest.class);

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

    final ResultList<Metadata> all = searchService.findAll(SEARCH_PARAMETERS, Metadata.class);
    LOGGER.info("all size: {}", all.getItems().size());

  }

  /**
   * Test find metadata by terminology icd 10 cm.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindMetadataByTerminologyIcd10cm() throws Exception {

    final String terminology = "ICD10CM";
    final String version = "2023";
    final TermQueryComposer termQuery = new TermQueryComposer(terminology, version, null, null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery());
    final ResultList<Metadata> metadata = searchService.find(SEARCH_PARAMETERS, Metadata.class);
    assertEquals(14, metadata.getItems().size());
    metadata.getItems().forEach(m -> {
      assertEquals(terminology, m.getTerminology());
      assertEquals(version, m.getVersion());
    });
  }

  /**
   * Test find metadata by terminology loinc.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindMetadataByTerminologyLoinc() throws Exception {

    final String terminology = "LNC";
    final String version = "277";
    final TermQueryComposer termQuery = new TermQueryComposer(terminology, version, null, null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery());
    final ResultList<Metadata> metadata = searchService.find(SEARCH_PARAMETERS, Metadata.class);
    assertEquals(93, metadata.getItems().size());
    metadata.getItems().forEach(m -> {
      assertEquals(terminology, m.getTerminology());
      assertEquals(version, m.getVersion());
    });
  }

  /**
   * Test find metadata by terminology rx norm.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindMetadataByTerminologyRxNorm() throws Exception {

    final String terminology = "RXNORM";
    final String version = "04012024";
    final TermQueryComposer termQuery = new TermQueryComposer(terminology, version, null, null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery());
    final ResultList<Metadata> metadata = searchService.find(SEARCH_PARAMETERS, Metadata.class);
    assertEquals(78, metadata.getItems().size());
    metadata.getItems().forEach(m -> {
      assertEquals(terminology, m.getTerminology());
      assertEquals(version, m.getVersion());
    });
  }

  /**
   * Test find metadata by terminology snomed ct us.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindMetadataByTerminologySnomedCtUs() throws Exception {

    final String terminology = "SNOMEDCT_US";
    final String version = "20240301";
    final TermQueryComposer termQuery = new TermQueryComposer(terminology, version, null, null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery());
    final ResultList<Metadata> metadata = searchService.find(SEARCH_PARAMETERS, Metadata.class);
    assertEquals(54, metadata.getItems().size());
    metadata.getItems().forEach(m -> {
      assertEquals(terminology, m.getTerminology());
      assertEquals(version, m.getVersion());
    });
  }

  /**
   * Test find metadata by terminology snomed ct.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindMetadataByTerminologySnomedCt() throws Exception {

    final String terminology = "SNOMEDCT";
    final String version = "20240101";
    final TermQueryComposer termQuery = new TermQueryComposer(terminology, version, null, null);
    SEARCH_PARAMETERS.setQuery(termQuery.getQuery());
    final ResultList<Metadata> metadata = searchService.find(SEARCH_PARAMETERS, Metadata.class);
    assertEquals(50, metadata.getItems().size());
    metadata.getItems().forEach(m -> {
      assertEquals(terminology, m.getTerminology());
      assertEquals(version, m.getVersion());
    });
  }

}
