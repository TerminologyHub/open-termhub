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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wci.termhub.AppConfig;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.SingleFlight;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.TerminologyUtility;

/**
 * Finds LOINC panel concepts for FHIR Questionnaire search. Questionnaire is always scoped to the
 * latest loaded LOINC release (e.g. 2.81); callers must pass the terminology from
 * {@link LoincValueSetHelper#findLoincTerminology(EntityRepositoryService)}.
 *
 * <p>
 * A concept is exposed as a Questionnaire when it has form children, a {@code CLASS} starting with
 * {@code PANEL}, and is not {@code PANEL.HEDIS}.
 */
@Component
public class QuestionnaireSearchHelper {

  /** The app config. */
  @SuppressWarnings("unused")
  private final AppConfig appConfig;

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuestionnaireSearchHelper.class);

  /** Lucene clause for LOINC panel concepts. */
  private static final String PANEL_TYPE_CLAUSE = "attributes.PanelType:Panel";

  /** Lucene clause for laboratory class type. */
  // private static final String CLASS_TYPE_LAB_CLAUSE = "attributes.CLASSTYPE:1";

  /** LOINC CLASS relationship additionalType. */
  private static final String CLASS_REL_TYPE = "CLASS";

  /** CLASS name prefix for concepts exposed as Questionnaires. */
  private static final String PANEL_CLASS_PREFIX = "PANEL.";

  /** Batch size for paginated concept queries. */
  private static final int BATCH_SIZE = 5000;

  /** Cache lock. */
  private static final Object CACHE_LOCK = new Object();

  /** Coalesces concurrent panel scans for the same terminology key. */
  private static final SingleFlight PANEL_FLIGHT = new SingleFlight();

  /** Cached terminology key. */
  private static String cachedTerminologyKey;

  /** Cached panel concepts for the latest LOINC version. */
  private static List<Concept> cachedPanelConcepts;

  /**
   * Instantiates a new questionnaire search helper.
   *
   * @param appConfig the app config
   */
  QuestionnaireSearchHelper(final AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  /**
   * Returns panel concepts for the given latest LOINC terminology, using an indexed query when
   * available and an in-memory cache to avoid repeated full scans. Concurrent cold misses for the
   * same terminology share a single scan.
   *
   * @param searchService the search service
   * @param latestLoincTerminology latest LOINC terminology (abbreviation, publisher, version)
   * @return panel concepts for questionnaire conversion
   * @throws Exception the exception
   */
  public List<Concept> findPanelConcepts(final EntityRepositoryService searchService,
    final Terminology latestLoincTerminology) throws Exception {

    final String cacheKey = buildCacheKey(latestLoincTerminology);
    synchronized (CACHE_LOCK) {
      if (cacheKey.equals(cachedTerminologyKey) && cachedPanelConcepts != null) {
        return cachedPanelConcepts;
      }
    }

    return PANEL_FLIGHT.execute(cacheKey, () -> {
      synchronized (CACHE_LOCK) {
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

      final Set<String> eligibleCodes =
          findEligibleQuestionnaireConceptCodes(searchService, latestLoincTerminology);
      if (!eligibleCodes.isEmpty()) {
        panels = new ArrayList<>(
            panels.stream().filter(c -> eligibleCodes.contains(c.getCode())).toList());
      } else {
        LOGGER.info(
            "No CLASS/member edges for questionnaire filter on LOINC {}; returning PanelType=Panel",
            latestLoincTerminology.getVersion());
      }

      final List<Concept> immutable = Collections.unmodifiableList(panels);
      synchronized (CACHE_LOCK) {
        if (cacheKey.equals(cachedTerminologyKey) && cachedPanelConcepts != null) {
          return cachedPanelConcepts;
        }
        cachedTerminologyKey = cacheKey;
        cachedPanelConcepts = immutable;
        return cachedPanelConcepts;
      }
    });
  }

  /**
   * Clears the cached panel list and related Questionnaire caches (e.g. after LOINC reload).
   */
  public void clearCache() {
    clearCaches();
  }

  /**
   * Static clear for {@link FhirUtility#clearCaches()} and loaders.
   */
  public static void clearCaches() {
    synchronized (CACHE_LOCK) {
      cachedTerminologyKey = null;
      cachedPanelConcepts = null;
    }
    PANEL_FLIGHT.clear();
    QuestionnaireSearchCache.clear();
    QuestionnaireGetCache.clear();
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
   * Checks if a concept has LOINC {@code CLASSTYPE=1} (laboratory).
   *
   * @param concept the concept
   * @return true when CLASSTYPE is 1
   */
  public static boolean isLabClassType(final Concept concept) {
    return concept != null && concept.getAttributes() != null
        && "1".equals(concept.getAttributes().get(LoincConstants.ATTR_CLASSTYPE));
  }

  /**
   * Whether a LOINC CLASS target is excluded from FHIR Questionnaire (e.g. PANEL.HEDIS).
   *
   * @param classCode LOINC part code on the CLASS relationship target
   * @param className LOINC CLASS name on the relationship target
   * @return true when the panel class must not be exposed as Questionnaire
   */
  public static boolean isExcludedQuestionnairePanelClass(final String classCode,
    final String className) {

    if (LoincConstants.LOINC_EXCLUDED_PANEL_CLASS_CODE.equals(classCode)) {
      return true;
    }
    if (className == null) {
      return false;
    }
    final String normalized = className.trim().toUpperCase();
    return normalized.startsWith(LoincConstants.LOINC_EXCLUDED_PANEL_CLASS_NAME);
  }

  /**
   * Whether a concept qualifies as a Questionnaire (form children + PANEL class, not HEDIS).
   *
   * @param searchService the search service
   * @param concept the concept
   * @return true when the concept should be exposed as Questionnaire
   * @throws Exception the exception
   */
  public boolean isQuestionnairePanel(final EntityRepositoryService searchService,
    final Concept concept) throws Exception {

    // if (!isPanelConcept(concept) || !isLabClassType(concept) || concept.getCode() == null) {
    if (!isPanelConcept(concept) || concept.getCode() == null) {
      return false;
    }
    if (!hasFormChildren(searchService, concept)) {
      return false;
    }
    return hasEligiblePanelClass(searchService, concept);
  }

  /**
   * Whether a panel concept must not be exposed as Questionnaire (HEDIS CLASS).
   *
   * @param searchService the search service
   * @param concept the concept
   * @return true when excluded
   * @throws Exception the exception
   */
  public boolean isExcludedQuestionnairePanel(final EntityRepositoryService searchService,
    final Concept concept) throws Exception {

    if (concept == null || concept.getCode() == null) {
      return false;
    }

    final String termQuery = TerminologyUtility.getTerminologyQuery(concept.getTerminology(),
        concept.getPublisher(), concept.getVersion());
    final String query = StringUtility.composeQuery("AND", termQuery,
        "from.code:" + StringUtility.escapeQuery(concept.getCode()),
        StringUtility.escapeKeywordField("additionalType", CLASS_REL_TYPE));

    final SearchParameters params = new SearchParameters();
    params.setQuery(query);
    params.setLimit(BATCH_SIZE);
    params.setOffset(0);

    for (final ConceptRelationship rel : searchService.findAll(params, ConceptRelationship.class)
        .getItems()) {
      if (rel.getTo() == null) {
        continue;
      }
      if (isExcludedQuestionnairePanelClass(rel.getTo().getCode(), rel.getTo().getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Builds the cache key.
   *
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
   * Query panel concepts.
   *
   * @param searchService the search service
   * @param terminology latest LOINC terminology
   * @return PanelType=Panel concepts
   * @throws Exception the exception
   */
  private List<Concept> queryPanelConcepts(final EntityRepositoryService searchService,
    final Terminology terminology) throws Exception {

    final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), terminology.getVersion());
    // final String query =
    // StringUtility.composeQuery("AND", termQuery, PANEL_TYPE_CLAUSE, CLASS_TYPE_LAB_CLAUSE);
    final String query = StringUtility.composeQuery("AND", termQuery, PANEL_TYPE_CLAUSE);
    return fetchAllConcepts(searchService, query);
  }

  /**
   * Concept codes that have CLASS {@code PANEL*} (not HEDIS) and at least one form child member.
   *
   * @param searchService the search service
   * @param terminology latest LOINC terminology
   * @return eligible concept codes
   * @throws Exception the exception
   */
  private Set<String> findEligibleQuestionnaireConceptCodes(
    final EntityRepositoryService searchService, final Terminology terminology) throws Exception {

    final Set<String> panelClassCodes = findPanelClassConceptCodes(searchService, terminology);
    if (panelClassCodes.isEmpty()) {
      return Set.of();
    }
    final Set<String> withChildren = findCodesWithFormChildren(searchService, terminology);
    if (withChildren.isEmpty()) {
      return panelClassCodes;
    }
    final Set<String> eligible = new HashSet<>(panelClassCodes);
    eligible.retainAll(withChildren);
    return eligible;
  }

  /**
   * Collects {@code from.code} of CLASS relationships whose target name starts with {@code PANEL.}
   * and is not HEDIS.
   *
   * @param searchService the search service
   * @param terminology latest LOINC terminology
   * @return concept codes with eligible CLASS
   * @throws Exception the exception
   */
  private Set<String> findPanelClassConceptCodes(final EntityRepositoryService searchService,
    final Terminology terminology) throws Exception {

    final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), terminology.getVersion());
    final String query = StringUtility.composeQuery("AND", termQuery,
        StringUtility.escapeKeywordField("additionalType", CLASS_REL_TYPE));
    final Set<String> codes = new HashSet<>();
    int offset = 0;
    long total = Long.MAX_VALUE;

    while (offset < total) {
      final SearchParameters params = new SearchParameters();
      params.setQuery(query);
      params.setLimit(BATCH_SIZE);
      params.setOffset(offset);

      final ResultList<ConceptRelationship> batch =
          searchService.findAll(params, ConceptRelationship.class);
      total = batch.getTotal();
      if (batch.getItems().isEmpty()) {
        break;
      }
      for (final ConceptRelationship rel : batch.getItems()) {
        if (rel.getFrom() == null || rel.getFrom().getCode() == null || rel.getTo() == null) {
          continue;
        }
        final String className = rel.getTo().getName();
        if (className == null) {
          continue;
        }
        final String normalized = className.trim().toUpperCase();
        if (normalized.startsWith(PANEL_CLASS_PREFIX)
            && !isExcludedQuestionnairePanelClass(rel.getTo().getCode(), className)) {
          codes.add(rel.getFrom().getCode());
        }
      }
      offset += BATCH_SIZE;
    }
    return codes;
  }

  /**
   * Collects panel codes that have at least one {@code member} edge to a different code.
   *
   * @param searchService the search service
   * @param terminology latest LOINC terminology
   * @return concept codes with form children
   * @throws Exception the exception
   */
  private Set<String> findCodesWithFormChildren(final EntityRepositoryService searchService,
    final Terminology terminology) throws Exception {

    final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), terminology.getVersion());
    final String query = StringUtility.composeQuery("AND", termQuery,
        StringUtility.escapeKeywordField("additionalType", LoincConstants.LOINC_REL_PANEL_MEMBER));
    final Set<String> codes = new HashSet<>();
    int offset = 0;
    long total = Long.MAX_VALUE;

    while (offset < total) {
      final SearchParameters params = new SearchParameters();
      params.setQuery(query);
      params.setLimit(BATCH_SIZE);
      params.setOffset(offset);

      final ResultList<ConceptRelationship> batch =
          searchService.findAll(params, ConceptRelationship.class);
      total = batch.getTotal();
      if (batch.getItems().isEmpty()) {
        break;
      }
      for (final ConceptRelationship rel : batch.getItems()) {
        if (rel.getFrom() == null || rel.getFrom().getCode() == null || rel.getTo() == null
            || rel.getTo().getCode() == null) {
          continue;
        }
        if (!rel.getFrom().getCode().equals(rel.getTo().getCode())) {
          codes.add(rel.getFrom().getCode());
        }
      }
      offset += BATCH_SIZE;
    }
    return codes;
  }

  /**
   * Checks for form children.
   *
   * @param searchService the search service
   * @param concept the concept
   * @return true when the concept has a member child other than itself
   * @throws Exception the exception
   */
  private boolean hasFormChildren(final EntityRepositoryService searchService,
    final Concept concept) throws Exception {

    final String termQuery = TerminologyUtility.getTerminologyQuery(concept.getTerminology(),
        concept.getPublisher(), concept.getVersion());
    final String query = StringUtility.composeQuery("AND", termQuery,
        "from.code:" + StringUtility.escapeQuery(concept.getCode()),
        StringUtility.escapeKeywordField("additionalType", LoincConstants.LOINC_REL_PANEL_MEMBER));

    final SearchParameters params = new SearchParameters();
    params.setQuery(query);
    params.setLimit(BATCH_SIZE);
    params.setOffset(0);

    for (final ConceptRelationship rel : searchService.findAll(params, ConceptRelationship.class)
        .getItems()) {
      if (rel.getTo() == null || rel.getTo().getCode() == null) {
        continue;
      }
      if (!concept.getCode().equals(rel.getTo().getCode())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks for eligible panel class.
   *
   * @param searchService the search service
   * @param concept the concept
   * @return true when CLASS is PANEL* and not HEDIS
   * @throws Exception the exception
   */
  private boolean hasEligiblePanelClass(final EntityRepositoryService searchService,
    final Concept concept) throws Exception {

    final String termQuery = TerminologyUtility.getTerminologyQuery(concept.getTerminology(),
        concept.getPublisher(), concept.getVersion());
    final String query = StringUtility.composeQuery("AND", termQuery,
        "from.code:" + StringUtility.escapeQuery(concept.getCode()),
        StringUtility.escapeKeywordField("additionalType", CLASS_REL_TYPE));

    final SearchParameters params = new SearchParameters();
    params.setQuery(query);
    params.setLimit(BATCH_SIZE);
    params.setOffset(0);

    boolean foundPanel = false;
    for (final ConceptRelationship rel : searchService.findAll(params, ConceptRelationship.class)
        .getItems()) {
      if (rel.getTo() == null) {
        continue;
      }
      final String className = rel.getTo().getName();
      if (className == null) {
        continue;
      }
      final String normalized = className.trim().toUpperCase();
      if (isExcludedQuestionnairePanelClass(rel.getTo().getCode(), className)) {
        return false;
      }
      if (normalized.startsWith(PANEL_CLASS_PREFIX)) {
        foundPanel = true;
      }
    }
    return foundPanel;
  }

  /**
   * Fallback when attributes are not indexed: scan the release and filter in memory.
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

      for (final Concept concept : batch.getItems()) {
        // if (isPanelConcept(concept) && isLabClassType(concept)) {
        if (isPanelConcept(concept)) {
          panels.add(concept);
        }
      }
      offset += BATCH_SIZE;
    }
    return panels;
  }

  /**
   * Fetch all concepts.
   *
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
