/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.TerminologyUtility;

/**
 * Finds LOINC panel concepts for FHIR Questionnaire search. Questionnaire is always scoped to the
 * latest loaded LOINC release (e.g. 2.81); callers must pass the terminology from
 * {@link LoincValueSetHelper#findLoincTerminology(EntityRepositoryService)}.
 */
@Component
public class QuestionnaireSearchHelper {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuestionnaireSearchHelper.class);

  /** Lucene clause for LOINC panel concepts. */
  private static final String PANEL_TYPE_CLAUSE = "attributes.PanelType:Panel";

  /** Batch size for paginated concept queries. */
  private static final int BATCH_SIZE = 5000;

  /** Cache lock. */
  private final Object cacheLock = new Object();

  /** Cached terminology key. */
  private String cachedTerminologyKey;

  /** Cached panel concepts for the latest LOINC version. */
  private List<Concept> cachedPanelConcepts;

  /**
   * Returns panel concepts for the given latest LOINC terminology, using an indexed query when
   * available and an in-memory cache to avoid repeated full scans.
   *
   * @param searchService the search service
   * @param latestLoincTerminology latest LOINC terminology (abbreviation, publisher, version)
   * @return panel concepts for questionnaire conversion
   * @throws Exception the exception
   */
  public List<Concept> findPanelConcepts(final EntityRepositoryService searchService,
    final Terminology latestLoincTerminology) throws Exception {

    final String cacheKey = buildCacheKey(latestLoincTerminology);
    synchronized (cacheLock) {
      if (cacheKey.equals(cachedTerminologyKey) && cachedPanelConcepts != null) {
        return cachedPanelConcepts;
      }
    }

    List<Concept> panels = queryPanelConcepts(searchService, latestLoincTerminology);
    if (panels.isEmpty()) {
      LOGGER.info(
          "No indexed panel concepts for LOINC {} {} (attributes.PanelType:Panel); scanning release",
          latestLoincTerminology.getAbbreviation(), latestLoincTerminology.getVersion());
      panels = scanPanelConcepts(searchService, latestLoincTerminology);
    } else if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Found {} panel concepts via attributes.PanelType index for LOINC {}",
          panels.size(), latestLoincTerminology.getVersion());
    }

    synchronized (cacheLock) {
      cachedTerminologyKey = cacheKey;
      cachedPanelConcepts = panels;
    }
    return panels;
  }

  /**
   * Clears the cached panel list (e.g. after LOINC reload).
   */
  public void clearCache() {
    synchronized (cacheLock) {
      cachedTerminologyKey = null;
      cachedPanelConcepts = null;
    }
  }

  /**
   * Checks if a concept has {@code PanelType=Panel}.
   *
   * @param concept the concept
   * @return true when the concept is a LOINC panel
   */
  public static boolean isPanelConcept(final Concept concept) {
    return concept != null && concept.getAttributes() != null
        && "Panel".equals(concept.getAttributes().get("PanelType"));
  }

  /**
   * @param terminology the terminology
   * @return cache key
   */
  private String buildCacheKey(final Terminology terminology) {
    if (terminology.getId() != null) {
      return terminology.getId();
    }
    return TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), terminology.getVersion());
  }

  /**
   * @param searchService the search service
   * @param terminology latest LOINC terminology
   * @return panel concepts from indexed query
   * @throws Exception the exception
   */
  private List<Concept> queryPanelConcepts(final EntityRepositoryService searchService,
    final Terminology terminology) throws Exception {

    final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), terminology.getVersion());
    final String query = StringUtility.composeQuery("AND", termQuery, PANEL_TYPE_CLAUSE);
    return fetchAllConcepts(searchService, query);
  }

  /**
   * Fallback when {@code attributes.PanelType} is not yet indexed: scan only the latest LOINC
   * release and filter in memory.
   *
   * @param searchService the search service
   * @param terminology latest LOINC terminology
   * @return panel concepts
   * @throws Exception the exception
   */
  private List<Concept> scanPanelConcepts(final EntityRepositoryService searchService,
    final Terminology terminology) throws Exception {

    final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), terminology.getVersion());
    final List<Concept> panels = new ArrayList<>();
    int offset = 0;
    long total = Long.MAX_VALUE;
    long scanned = 0;

    while (offset < total) {
      final SearchParameters params = new SearchParameters();
      params.setQuery(termQuery);
      params.setLimit(BATCH_SIZE);
      params.setOffset(offset);
      params.getSort().add("code");
      params.setAscending(true);

      final ResultList<Concept> batch = searchService.findAll(params, Concept.class);
      total = batch.getTotal();
      if (batch.getItems().isEmpty()) {
        break;
      }

      scanned += batch.getItems().size();
      for (final Concept concept : batch.getItems()) {
        if (isPanelConcept(concept)) {
          panels.add(concept);
        }
      }
      offset += BATCH_SIZE;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Scanned {} concepts in LOINC {}, found {} panels", scanned,
          terminology.getVersion(), panels.size());
    }
    return panels;
  }

  /**
   * @param searchService the search service
   * @param query the lucene query
   * @return all matching concepts
   * @throws Exception the exception
   */
  private List<Concept> fetchAllConcepts(final EntityRepositoryService searchService,
    final String query) throws Exception {

    final List<Concept> concepts = new ArrayList<>();
    int offset = 0;
    long total = Long.MAX_VALUE;

    while (offset < total) {
      final SearchParameters params = new SearchParameters();
      params.setQuery(query);
      params.setLimit(BATCH_SIZE);
      params.setOffset(offset);
      params.getSort().add("code");
      params.setAscending(true);

      final ResultList<Concept> batch = searchService.findAll(params, Concept.class);
      total = batch.getTotal();
      if (batch.getItems().isEmpty()) {
        break;
      }
      concepts.addAll(batch.getItems());
      offset += BATCH_SIZE;
    }
    return concepts;
  }
}
