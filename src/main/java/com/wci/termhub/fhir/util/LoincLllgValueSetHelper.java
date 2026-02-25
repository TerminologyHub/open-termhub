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

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.TerminologyUtility;

/**
 * Helper for LOINC LL/LG value set support (Regenstrief mode). When enabled, value set
 * providers expose value sets at http://loinc.org/vs/LL* and http://loinc.org/vs/LG*.
 */
@Component
public class LoincLllgValueSetHelper {

  private static final Logger logger = LoggerFactory.getLogger(LoincLllgValueSetHelper.class);

  /** URL prefix for LOINC value sets. */
  public static final String LOINC_VS_URL_PREFIX = "http://loinc.org/vs/";

  /** LL pattern: LL followed by digits, hyphen, digits (e.g. LL1162-8). */
  private static final Pattern LL_PATTERN = Pattern.compile("^LL\\d+-\\d+$");

  /** LG pattern: LG followed by digits, hyphen, digits, optional -version (e.g. LG51018-6 or LG51018-6-2.78). */
  private static final Pattern LG_PATTERN = Pattern.compile("^LG\\d+-\\d+(-[\\d.]+)?$");

  /** LOINC system abbreviation. */
  public static final String LOINC_SYSTEM = "LOINC";

  /** LOINC publisher. */
  public static final String LOINC_PUBLISHER = "Regenstrief Institute";

  /** Concept attribute for answer list ID (LOINC). */
  public static final String ATTR_ANSWER_LIST_ID = "ANSWER_LIST_ID";

  @Value("${fhir.loinc.lllg.valuesets.enabled:false}")
  private boolean enabled;

  /**
   * Returns whether LL/LG value set support is enabled.
   *
   * @return true if enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Checks if the URL is a LOINC LL/LG value set URL (http://loinc.org/vs/LL... or .../LG...).
   *
   * @param url the url (e.g. http://loinc.org/vs/LL1162-8)
   * @return true if matches
   */
  public boolean isLllgValueSetUrl(final String url) {
    if (url == null || !url.startsWith(LOINC_VS_URL_PREFIX)) {
      return false;
    }
    final String id = url.substring(LOINC_VS_URL_PREFIX.length()).trim();
    return isLllgId(id);
  }

  /**
   * Checks if the id is an LL or LG value set id (e.g. LL1162-8, LG51018-6-2.78).
   *
   * @param id the id
   * @return true if matches
   */
  public boolean isLllgId(final String id) {
    if (id == null) {
      return false;
    }
    return LL_PATTERN.matcher(id).matches() || LG_PATTERN.matcher(id).matches();
  }

  /**
   * Extracts the value set id from a LOINC value set URL.
   *
   * @param url the url (e.g. http://loinc.org/vs/LL1162-8)
   * @return the id or null
   */
  public String parseIdFromUrl(final String url) {
    if (!isLllgValueSetUrl(url)) {
      return null;
    }
    return url.substring(LOINC_VS_URL_PREFIX.length()).trim();
  }

  /**
   * For LG ids with version suffix (e.g. LG51018-6-2.78), returns the version part (2.78).
   * For LL or LG without version, returns null.
   *
   * @param lllgId the full id
   * @return version or null
   */
  public String getVersionFromLllgId(final String lllgId) {
    if (lllgId == null || !lllgId.startsWith("LG")) {
      return null;
    }
    final int lastDash = lllgId.lastIndexOf('-');
    if (lastDash <= 0) {
      return null;
    }
    final String afterLast = lllgId.substring(lastDash + 1);
    if (afterLast.matches("[\\d.]+")) {
      return afterLast;
    }
    return null;
  }

  /**
   * Builds the canonical URL for an LL/LG value set id.
   *
   * @param id the id (e.g. LL1162-8)
   * @return http://loinc.org/vs/{id}
   */
  public String toLllgUrl(final String id) {
    return id == null ? null : LOINC_VS_URL_PREFIX + id;
  }

  /**
   * Finds LOINC terminology (any version) for LL/LG resolution. Prefers LOINC + Regenstrief
   * Institute; if not found (e.g. sandbox with different publisher), falls back to any
   * terminology whose URI contains "loinc.org".
   *
   * @param searchService the search service
   * @return LOINC terminology or null
   */
  public Terminology findLoincTerminology(final EntityRepositoryService searchService) {
    try {
      Terminology term = TerminologyUtility.getLatestTerminologyVersion(searchService, LOINC_SYSTEM,
          LOINC_PUBLISHER);
      if (term != null) {
        return term;
      }
      final SearchParameters params = new SearchParameters("*:*", 50, 0);
      final ResultList<Terminology> list = searchService.find(params, Terminology.class);
      for (final Terminology t : list.getItems()) {
        if (t.getUri() != null && t.getUri().contains("loinc.org")) {
          return t;
        }
      }
      return null;
    } catch (final Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("LOINC terminology not found: {}", e.getMessage());
      }
      return null;
    }
  }

  /**
   * Resolves membership for an LL value set: concepts in LOINC whose ANSWER_LIST_ID equals the
   * given LL code.
   *
   * @param searchService the search service
   * @param terminology LOINC terminology (abbreviation, publisher, version)
   * @param llCode the LL code (e.g. LL1162-8)
   * @param offset the offset
   * @param count the count
   * @return list of concepts (members of the answer list)
   */
  public ResultList<Concept> findLlMembers(final EntityRepositoryService searchService,
      final Terminology terminology, final String llCode, final int offset, final int count)
      throws Exception {
    final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), terminology.getVersion());
    final String attrClause =
        "attributes." + ATTR_ANSWER_LIST_ID + ":" + StringUtility.escapeQuery(llCode);
    final String query = StringUtility.composeQuery("AND", termQuery, attrClause);
    final int limit = count < 0 ? 1000 : count;
    final SearchParameters params = new SearchParameters(query, limit, offset);
    return searchService.find(params, Concept.class);
  }

  /**
   * Resolves membership for an LG value set. LG can have an optional version suffix
   * (e.g. LG51018-6-2.78). For now we resolve: (1) the concept with code = LG id (base part if
   * versioned), and (2) concepts that reference this LG (e.g. panel members). If the model
   * stores group membership in an attribute, we use it; otherwise we return concepts whose
   * code equals the LG code (single-member value set).
   *
   * @param searchService the search service
   * @param terminology LOINC terminology
   * @param lgId the LG id (e.g. LG51018-6 or LG51018-6-2.78)
   * @param offset the offset
   * @param count the count
   * @return list of concepts
   */
  public ResultList<Concept> findLgMembers(final EntityRepositoryService searchService,
      final Terminology terminology, final String lgId, final int offset, final int count)
      throws Exception {
    String baseLgCode = lgId;
    String versionFilter = terminology.getVersion();
    final String versionFromId = getVersionFromLllgId(lgId);
    if (versionFromId != null) {
      baseLgCode = lgId.substring(0, lgId.lastIndexOf('-'));
      versionFilter = versionFromId;
    }
    final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), versionFilter == null ? "*" : versionFilter);
    final String codeClause = "code:" + StringUtility.escapeQuery(baseLgCode);
    final String query = StringUtility.composeQuery("AND", termQuery, codeClause);
    final int limit = count < 0 ? 1000 : count;
    final SearchParameters params = new SearchParameters(query, limit, offset);
    return searchService.find(params, Concept.class);
  }

  /**
   * Resolves membership for an LL or LG value set.
   *
   * @param searchService the search service
   * @param terminology LOINC terminology
   * @param lllgId the LL or LG id
   * @param offset the offset
   * @param count the count
   * @return list of concepts
   */
  public ResultList<Concept> findLllgMembers(final EntityRepositoryService searchService,
      final Terminology terminology, final String lllgId, final int offset, final int count)
      throws Exception {
    if (lllgId != null && lllgId.startsWith("LL")) {
      return findLlMembers(searchService, terminology, lllgId, offset, count);
    }
    if (lllgId != null && lllgId.startsWith("LG")) {
      return findLgMembers(searchService, terminology, lllgId, offset, count);
    }
    final ResultList<Concept> empty = new ResultList<>();
    empty.setTotal(0);
    empty.setItems(List.of());
    return empty;
  }

  /**
   * Checks if a code is a member of the given LL/LG value set.
   *
   * @param searchService the search service
   * @param terminology LOINC terminology
   * @param lllgId the LL or LG id
   * @param code the LOINC code to check
   * @return the concept if member, or null
   */
  public Concept findMemberByCode(final EntityRepositoryService searchService,
      final Terminology terminology, final String lllgId, final String code) throws Exception {
    final ResultList<Concept> members = findLllgMembers(searchService, terminology, lllgId, 0,
        100_000);
    for (final Concept c : members.getItems()) {
      if (code != null && code.equals(c.getCode())) {
        return c;
      }
    }
    return null;
  }
}
