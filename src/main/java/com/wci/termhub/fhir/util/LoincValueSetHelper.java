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
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.TerminologyUtility;

/**
 * Helper for LOINC LL/LG value set support (Regenstrief mode). When enabled
 * (FHIR_LOINC_LLLG_VALUESETS_ENABLED=true), value set providers expose value
 * sets at http://loinc.org?fhir_vs/LL* and http://loinc.org?fhir_vs/LG*, and at
 * http://loinc.org/vs/{id} (path form). When enabled, LG ids may include a
 * version suffix (e.g. LG51018-6-2.72) and expansion is scoped to that LOINC
 * version.
 */
@Component
public class LoincValueSetHelper {

  /** The Constant logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(LoincValueSetHelper.class);

  /** URL prefix for LOINC value sets (query form). */
  public static final String LOINC_VS_URL_PREFIX = "http://loinc.org?fhir_vs";

  /** Path prefix for LOINC value set URLs (e.g. http://loinc.org/vs/LG51018-6-2.72). */
  public static final String LOINC_VS_PATH_PREFIX = "http://loinc.org/vs/";

  /** Path prefix for LOINC value set URLs (HTTPS). */
  private static final String LOINC_VS_PATH_PREFIX_HTTPS = "https://loinc.org/vs/";

  /** LL pattern: LL followed by digits, hyphen, digits (e.g. LL1162-8). */
  private static final Pattern LL_PATTERN = Pattern.compile("^LL\\d+-\\d+$");

  /**
   * LG pattern: LG followed by digits, hyphen, digits, optional -version (e.g.
   * LG51018-6 or LG51018-6-2.78).
   */
  private static final Pattern LG_PATTERN = Pattern.compile("^LG\\d+-\\d+(-[\\d.]+)?$");

  /** LOINC system abbreviation. */
  public static final String LOINC_SYSTEM = "LOINC";

  /** LOINC publisher. */
  public static final String LOINC_PUBLISHER = "Regenstrief Institute";

  /** Concept attribute for answer list ID (LOINC). */
  public static final String ATTR_ANSWER_LIST_ID = "ANSWER_LIST_ID";

  /** The enabled. */
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
   * Checks if the URL is a LOINC LL/LG value set URL. Accepts either the legacy
   * http://loinc.org/?fhir_vs/{id} form or the implicit ValueSet style
   * http://loinc.org/?fhir_vs={id}.
   *
   * @param url the url (e.g. http://loinc.org/?fhir_vs=LL1162-8)
   * @return true if matches
   */
  public boolean isLllgValueSetUrl(final String url) {
    return parseIdFromUrl(url) != null;
  }

  /**
   * Checks if the id is an LL or LG value set id (e.g. LL1162-8,
   * LG51018-6-2.78).
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
   * Returns true if the id is an LL (answer list) value set id (e.g. LL1162-8).
   * LL value set members (LA concepts) have SequenceNumber and should be sorted
   * by it.
   *
   * @param id the id
   * @return true if LL
   */
  public boolean isLlId(final String id) {
    return id != null && LL_PATTERN.matcher(id).matches();
  }

  /**
   * Returns true when a member code is itself an LL/LG value set (nested
   * reference).
   *
   * @param code the concept code
   * @return true if nested LL/LG value set
   */
  public boolean isNestedLllgValueSetCode(final String code) {
    return isLllgId(code);
  }

  /**
   * Canonical child value set URL (Regenstrief path form).
   *
   * @param code the LL or LG id
   * @return http://loinc.org/vs/{code}
   */
  public String toChildValueSetUrl(final String code) {
    return code == null ? null : LOINC_VS_PATH_PREFIX + code;
  }

  /**
   * Partitioned compose structure for LL/LG value sets.
   */
  public static final class LllgComposeStructure {

    /** The nested value set urls. */
    private final List<String> nestedValueSetUrls;

    /** The leaf concepts. */
    private final List<Concept> leafConcepts;

    /**
     * Instantiates a {@link LllgComposeStructure}.
     *
     * @param nestedValueSetUrls nested value set canonical URLs
     * @param leafConcepts direct leaf LOINC concepts
     */
    public LllgComposeStructure(final List<String> nestedValueSetUrls,
        final List<Concept> leafConcepts) {
      this.nestedValueSetUrls = nestedValueSetUrls == null ? List.of() : nestedValueSetUrls;
      this.leafConcepts = leafConcepts == null ? List.of() : leafConcepts;
    }

    /**
     * Gets the nested value set urls.
     *
     * @return the nested value set urls
     */
    public List<String> getNestedValueSetUrls() {
      return nestedValueSetUrls;
    }

    /**
     * Gets the leaf concepts.
     *
     * @return the leaf concepts
     */
    public List<Concept> getLeafConcepts() {
      return leafConcepts;
    }
  }

  /**
   * Result of recursive LL/LG expansion to leaf concepts.
   */
  public static final class ExpandedLllgResult {

    /** The items. */
    private final List<Concept> items;

    /** The total. */
    private final int total;

    /**
     * Instantiates an {@link ExpandedLllgResult}.
     *
     * @param items page of leaf concepts
     * @param total total leaf count before pagination
     */
    public ExpandedLllgResult(final List<Concept> items, final int total) {
      this.items = items == null ? List.of() : items;
      this.total = total;
    }

    /**
     * Gets the items.
     *
     * @return the items
     */
    public List<Concept> getItems() {
      return items;
    }

    /**
     * Gets the total.
     *
     * @return the total
     */
    public int getTotal() {
      return total;
    }
  }

  /**
   * Builds compose structure: nested LL/LG members as value set references,
   * others as concepts.
   *
   * @param members direct members (sorted by caller)
   * @return compose structure
   */
  public LllgComposeStructure buildLllgComposeStructure(final List<Concept> members) {
    final List<String> nestedUrls = new ArrayList<>();
    final List<Concept> leafConcepts = new ArrayList<>();
    if (members == null) {
      return new LllgComposeStructure(nestedUrls, leafConcepts);
    }
    for (final Concept c : members) {
      if (c.getCode() != null && isNestedLllgValueSetCode(c.getCode())) {
        nestedUrls.add(toChildValueSetUrl(c.getCode()));
      } else {
        leafConcepts.add(c);
      }
    }
    return new LllgComposeStructure(nestedUrls, leafConcepts);
  }

  /**
   * Sorts direct LL/LG members for compose (sequence order for LL, code order
   * for LG).
   *
   * @param lllgId the value set id
   * @param members members to sort in place
   */
  public void sortDirectLllgMembers(final String lllgId, final List<Concept> members) {
    if (members == null || members.isEmpty()) {
      return;
    }
    if (isLlId(lllgId)) {
      sortLlMembersBySequenceNumber(members);
    } else if (lllgId != null && lllgId.startsWith("LG")) {
      members.sort(
          Comparator.comparing(Concept::getCode, Comparator.nullsFirst(Comparator.naturalOrder())));
    }
  }

  /** LOINC concept attribute for answer sequence (ordinal order in list). */
  private static final String ATTR_SEQUENCE_NUMBER = "SequenceNumber";

  /** Maximum recursion depth when expanding nested LG/LL value sets. */
  private static final int MAX_LLLG_EXPANSION_DEPTH = 10;

  /** Member fetch limit for full LL/LG tree expansion. */
  private static final int LLLG_MEMBER_FETCH_LIMIT = 100_000;

  /**
   * Sorts LL value set members (LA concepts) by SequenceNumber so expansion
   * order matches fhir.loinc.org (e.g. None, Rare, Few, Moderate, Many). Falls
   * back to code order if SequenceNumber is missing. Sorts the list in place.
   *
   * @param members the list to sort (modified in place)
   */
  public static void sortLlMembersBySequenceNumber(final List<Concept> members) {
    if (members == null || members.size() <= 1) {
      return;
    }
    members.sort(Comparator.comparingInt(LoincValueSetHelper::sequenceNumberOrMax)
        .thenComparing(Concept::getCode, Comparator.nullsFirst(Comparator.naturalOrder())));
  }

  /**
   * Sequence number or max.
   *
   * @param c the c
   * @return the int
   */
  private static int sequenceNumberOrMax(final Concept c) {
    if (c.getAttributes() == null) {
      return Integer.MAX_VALUE;
    }
    final String seq = c.getAttributes().get(ATTR_SEQUENCE_NUMBER);
    if (seq == null) {
      return Integer.MAX_VALUE;
    }
    try {
      return Integer.parseInt(seq);
    } catch (final NumberFormatException e) {
      return Integer.MAX_VALUE;
    }
  }

  /**
   * Extracts the value set id from a LOINC value set URL.
   *
   * @param url the url (e.g. http://loinc.org/?fhir_vs=LL1162-8)
   * @return the id or null
   */
  public String parseIdFromUrl(final String url) {
    if (url == null) {
      return null;
    }
    // Implicit ValueSet form: ...?fhir_vs={id} or ...&fhir_vs={id} (check first
    // so URL
    // http://loinc.org?fhir_vs=LL1162-8 is not treated as legacy prefix +
    // "=LL1162-8")
    final String marker = "fhir_vs=";
    final int idx = url.indexOf(marker);
    if (idx >= 0) {
      String id = url.substring(idx + marker.length()).trim();
      final int amp = id.indexOf('&');
      if (amp >= 0) {
        id = id.substring(0, amp).trim();
      }
      final int hash = id.indexOf('#');
      if (hash >= 0) {
        id = id.substring(0, hash).trim();
      }
      return isLllgId(id) ? id : null;
    }
    // Legacy form: http://loinc.org?fhir_vs/{id} or prefix with = or / before
    // id
    if (url.startsWith(LOINC_VS_URL_PREFIX)) {
      String id = url.substring(LOINC_VS_URL_PREFIX.length()).trim();
      if (id.startsWith("=")) {
        id = id.substring(1).trim();
      } else if (id.startsWith("/")) {
        id = id.substring(1).trim();
      }
      return isLllgId(id) ? id : null;
    }
    // Path form: http://loinc.org/vs/{id} or https://loinc.org/vs/{id} (e.g. LG51018-6-2.72)
    if (url.startsWith(LOINC_VS_PATH_PREFIX) || url.startsWith(LOINC_VS_PATH_PREFIX_HTTPS)) {
      final String prefix = url.startsWith(LOINC_VS_PATH_PREFIX_HTTPS)
          ? LOINC_VS_PATH_PREFIX_HTTPS : LOINC_VS_PATH_PREFIX;
      String id = url.substring(prefix.length()).trim();
      final int q = id.indexOf('?');
      if (q >= 0) {
        id = id.substring(0, q).trim();
      }
      final int h = id.indexOf('#');
      if (h >= 0) {
        id = id.substring(0, h).trim();
      }
      return isLllgId(id) ? id : null;
    }
    return null;
  }

  /**
   * For LG ids with version suffix (e.g. LG51018-6-2.78), returns the version
   * part (2.78). For LG ids with only one hyphen (e.g. LG33055-1) the trailing
   * segment is part of the id, not a version, so returns null.
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
    if (lllgId.indexOf('-') >= lastDash) {
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
   * @return http://loinc.org/?fhir_vs={id}
   */
  public String toLllgUrl(final String id) {
    return id == null ? null : "http://loinc.org/?fhir_vs=" + id;
  }

  /**
   * Finds LOINC terminology (any version) for LL/LG resolution. Prefers LOINC +
   * Regenstrief Institute; if not found (e.g. sandbox with different
   * publisher), falls back to any terminology whose URI contains "loinc.org".
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
      term = TerminologyUtility.getLatestTerminologyVersion(searchService, LOINC_SYSTEM,
          "Regenstrief Institute, Inc.");
      if (term != null) {
        return term;
      }
      term = TerminologyUtility.getLatestTerminologyVersion(searchService, "LNC", null);
      if (term != null && term.getUri() != null && term.getUri().contains("loinc.org")) {
        return term;
      }
      final SearchParameters params = new SearchParameters(
          StringUtility.escapeKeywordField("abbreviation", LOINC_SYSTEM), 50, 0);
      ResultList<Terminology> list = searchService.find(params, Terminology.class);
      List<Terminology> loincTerms = list.getItems().stream()
          .filter(t -> t.getUri() != null && t.getUri().contains("loinc.org"))
          .toList();
      if (loincTerms.isEmpty()) {
        final SearchParameters lncParams = new SearchParameters(
            StringUtility.escapeKeywordField("abbreviation", "LNC"), 50, 0);
        list = searchService.find(lncParams, Terminology.class);
        loincTerms = list.getItems().stream()
            .filter(t -> t.getUri() != null && t.getUri().contains("loinc.org"))
            .toList();
      }
      if (loincTerms.isEmpty()) {
        return null;
      }
      return TerminologyUtility.getLatestTerminology(loincTerms);
    } catch (final Exception e) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("LOINC terminology not found: {}", e.getMessage());
      }
      return null;
    }
  }

  /**
   * Resolves membership for an LL value set. In full LOINC, answer (LA)
   * concepts link to the answer list (LL) via parent: each LA has one or more
   * parent valueCodings with code = LL. So members of LL1162-8 are concepts
   * that have parents.code = LL1162-8 (the LA codes in that list). The sandbox
   * model (ANSWER_LIST_ID on question concepts) is not used here; use full
   * LOINC with LL/LA concepts and parent relationships for expand to return LA
   * members.
   *
   * @param searchService the search service
   * @param terminology LOINC terminology (abbreviation, publisher, version)
   * @param llCode the LL code (e.g. LL1162-8)
   * @param offset the offset
   * @param count the count
   * @return list of concepts (LA members of the answer list)
   * @throws Exception the exception
   */
  public ResultList<Concept> findLlMembers(final EntityRepositoryService searchService,
    final Terminology terminology, final String llCode, final int offset, final int count)
    throws Exception {
    final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), terminology.getVersion());
    final String parentClause = "parents.code:" + StringUtility.escapeQuery(llCode);
    final String query = StringUtility.composeQuery("AND", termQuery, parentClause);
    final int limit = count < 0 ? 1000 : count;
    final SearchParameters params = new SearchParameters(query, limit, offset);
    return searchService.find(params, Concept.class);
  }

  /**
   * Resolves membership for an LG value set. LG can have an optional version
   * suffix (e.g. LG51018-6-2.78). For now we resolve: (1) the concept with code
   * = LG id (base part if versioned), and (2) concepts that reference this LG
   * (e.g. panel members). If the model stores group membership in an attribute,
   * we use it; otherwise we return concepts whose code equals the LG code
   * (single-member value set).
   *
   * @param searchService the search service
   * @param terminology LOINC terminology
   * @param lgId the LG id (e.g. LG51018-6 or LG51018-6-2.78)
   * @param offset the offset
   * @param count the count
   * @return list of concepts
   * @throws Exception the exception
   */
  public ResultList<Concept> findLgMembers(final EntityRepositoryService searchService,
    final Terminology terminology, final String lgId, final int offset, final int count)
    throws Exception {
    String baseLgCode = lgId;
    String versionFilter = terminology.getVersion();
    final String versionFromId = getVersionFromLllgId(lgId);
    if (versionFromId != null && enabled) {
      baseLgCode = lgId.substring(0, lgId.lastIndexOf('-'));
      versionFilter = versionFromId;
    } else if (versionFromId != null) {
      baseLgCode = lgId.substring(0, lgId.lastIndexOf('-'));
    }
    final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), versionFilter == null ? "*" : versionFilter);
    final String parentClause = "parents.code:" + StringUtility.escapeQuery(baseLgCode);
    final String excludeLgClause = "-code:" + StringUtility.escapeQuery(baseLgCode);
    final String query =
        StringUtility.composeQuery("AND", termQuery, parentClause, excludeLgClause);
    final int limit = count < 0 ? 1000 : count;
    final SearchParameters params = new SearchParameters(query, limit, offset);
    ResultList<Concept> result = searchService.find(params, Concept.class);
    result = filterOutLgConcept(result, baseLgCode);
    if (!result.getItems().isEmpty()) {
      return result;
    }
    final String relTermQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
            terminology.getPublisher(), versionFilter == null ? "*" : versionFilter);
    final String toCodeClause = "to.code:" + StringUtility.escapeQuery(baseLgCode);
    final String relQuery =
        StringUtility.composeQuery("AND", relTermQuery, toCodeClause, "hierarchical:true");
    final SearchParameters relParams = new SearchParameters(relQuery, limit, offset);
    final ResultList<ConceptRelationship> relResult =
        searchService.find(relParams, ConceptRelationship.class);
    final List<Concept> concepts = new ArrayList<>();
    for (final ConceptRelationship rel : relResult.getItems()) {
      if (rel.getFrom() != null && rel.getFrom().getCode() != null
          && !baseLgCode.equals(rel.getFrom().getCode())) {
        final Concept c = TerminologyUtility.getConcept(searchService, terminology,
            rel.getFrom().getCode());
        if (c != null) {
          concepts.add(c);
        }
      }
    }
    final ResultList<Concept> fallbackResult = new ResultList<>();
    fallbackResult.setTotal(concepts.size());
    fallbackResult.setItems(concepts);
    return fallbackResult;
  }

  /**
   * Filter out lg concept.
   *
   * @param result the result
   * @param lgCode the lg code
   * @return the result list
   */
  private ResultList<Concept> filterOutLgConcept(final ResultList<Concept> result,
    final String lgCode) {
    if (result == null || lgCode == null) {
      return result;
    }
    final List<Concept> filtered =
        result.getItems().stream().filter(c -> !lgCode.equals(c.getCode())).toList();
    if (filtered.size() == result.getItems().size()) {
      return result;
    }
    final ResultList<Concept> filteredResult = new ResultList<>();
    filteredResult.setTotal(filtered.size());
    filteredResult.setItems(filtered);
    return filteredResult;
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
   * @throws Exception the exception
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
   * Recursively expands an LL/LG value set to leaf LOINC concepts, then applies
   * filter and pagination.
   *
   * @param searchService the search service
   * @param terminology LOINC terminology
   * @param lllgId the LL or LG id
   * @param offset expansion offset
   * @param count expansion page size (0 = none, negative = all)
   * @param filter optional text filter on code or display
   * @return paginated leaf concepts and total count before pagination
   * @throws Exception the exception
   */
  public ExpandedLllgResult expandLllgLeaves(final EntityRepositoryService searchService,
    final Terminology terminology, final String lllgId, final int offset, final int count,
    final String filter) throws Exception {
    final List<Concept> leaves = new ArrayList<>();
    collectLllgLeaves(searchService, terminology, lllgId, new HashSet<>(), 0, leaves);
    List<Concept> filtered = leaves;
    if (filter != null && !filter.isBlank()) {
      final String f = filter.toLowerCase();
      filtered = leaves.stream().filter(c -> matchesLllgFilter(c, f)).toList();
    }
    final int total = filtered.size();
    final int from = Math.min(Math.max(offset, 0), total);
    final int to = count < 0 ? total : (count == 0 ? from : Math.min(from + count, total));
    return new ExpandedLllgResult(filtered.subList(from, to), total);
  }

  /**
   * Matches lllg filter.
   *
   * @param c the c
   * @param filterLower the filter lower
   * @return true, if successful
   */
  private static boolean matchesLllgFilter(final Concept c, final String filterLower) {
    if (c.getCode() != null && c.getCode().toLowerCase().contains(filterLower)) {
      return true;
    }
    return c.getName() != null && c.getName().toLowerCase().contains(filterLower);
  }

  /**
   * Collect lllg leaves.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param lllgId the lllg id
   * @param visited the visited
   * @param depth the depth
   * @param out the out
   * @throws Exception the exception
   */
  private void collectLllgLeaves(final EntityRepositoryService searchService,
    final Terminology terminology, final String lllgId, final Set<String> visited, final int depth,
    final List<Concept> out) throws Exception {
    if (lllgId == null || visited.contains(lllgId)) {
      return;
    }
    if (depth > MAX_LLLG_EXPANSION_DEPTH) {
      LOGGER.warn("LL/LG expansion depth exceeded at {} (max {})", lllgId,
          MAX_LLLG_EXPANSION_DEPTH);
      return;
    }
    visited.add(lllgId);
    final ResultList<Concept> list =
        findLllgMembers(searchService, terminology, lllgId, 0, LLLG_MEMBER_FETCH_LIMIT);
    final List<Concept> items = new ArrayList<>(list.getItems());
    sortDirectLllgMembers(lllgId, items);
    final Map<String, Concept> seen = new LinkedHashMap<>();
    for (final Concept existing : out) {
      if (existing.getCode() != null) {
        seen.put(existing.getCode(), existing);
      }
    }
    for (final Concept member : items) {
      if (member.getCode() != null && isNestedLllgValueSetCode(member.getCode())) {
        collectLllgLeaves(searchService, terminology, member.getCode(), visited, depth + 1, out);
        continue;
      }
      if (member.getCode() != null && !seen.containsKey(member.getCode())) {
        seen.put(member.getCode(), member);
        out.add(member);
      }
    }
  }

  /**
   * Finds all LG concepts in the given LOINC terminology using Lucene wildcard
   * queries. Used when {@code fhir.loinc.lllg.valuesets.enabled=true} to
   * enumerate value sets for a general {@code GET /ValueSet} listing.
   *
   * @param searchService the search service
   * @param terminology LOINC terminology
   * @param limit maximum number of concepts to return
   * @param offset start offset
   * @return result list of LG concepts
   * @throws Exception the exception
   */
  public ResultList<Concept> findAllLgConcepts(final EntityRepositoryService searchService,
    final Terminology terminology, final int limit, final int offset) throws Exception {
    final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), terminology.getVersion());
    final String query = "(" + termQuery + ") AND (code:LG*)";
    final SearchParameters params = new SearchParameters(query, limit, offset);
    return searchService.find(params, Concept.class);
  }

  /**
   * Checks if a code is a member of the given LL/LG value set.
   *
   * @param searchService the search service
   * @param terminology LOINC terminology
   * @param lllgId the LL or LG id
   * @param code the LOINC code to check
   * @return the concept if member, or null
   * @throws Exception the exception
   */
  public Concept findMemberByCode(final EntityRepositoryService searchService,
    final Terminology terminology, final String lllgId, final String code) throws Exception {
    final ResultList<Concept> members =
        findLllgMembers(searchService, terminology, lllgId, 0, 100_000);
    LOGGER.info("findMemberByCode lllgId={} code={}: findLllgMembers returned {} items", lllgId,
        code, members.getItems().size());
    for (final Concept c : members.getItems()) {
      if (code != null && code.equals(c.getCode())) {
        LOGGER.info("findMemberByCode: found code in findLllgMembers, returning concept");
        return c;
      }
    }
    if (lllgId != null && lllgId.startsWith("LG") && code != null) {
      LOGGER.info("findMemberByCode: code not in LG members, trying findLgPanelMemberByCode");
      return findLgPanelMemberByCode(searchService, terminology, lllgId, code);
    }
    LOGGER.info("findMemberByCode: returning null (not LG or no code)");
    return null;
  }

  /**
   * Finds a concept that is a panel member of the given LG by querying
   * ConceptRelationship for to.code=LG and from.code=code (parent relationship
   * is stored in the relationship index when the loader processes the parent
   * property).
   *
   * @param searchService the search service
   * @param terminology LOINC terminology
   * @param lgId the LG id (e.g. LG33055-1 or LG51018-6-2.78)
   * @param code the LOINC code to check
   * @return the concept if panel member, or null
   * @throws Exception the exception
   */
  private Concept findLgPanelMemberByCode(final EntityRepositoryService searchService,
    final Terminology terminology, final String lgId, final String code) throws Exception {
    String baseLgCode = lgId;
    String versionFilter = terminology.getVersion();
    final String versionFromId = getVersionFromLllgId(lgId);
    if (versionFromId != null && enabled) {
      baseLgCode = lgId.substring(0, lgId.lastIndexOf('-'));
      versionFilter = versionFromId;
    } else if (versionFromId != null) {
      baseLgCode = lgId.substring(0, lgId.lastIndexOf('-'));
    }
    final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), versionFilter == null ? "*" : versionFilter);
    final String toCodeClause = "to.code:" + StringUtility.escapeQuery(baseLgCode);
    final String fromCodeClause = "from.code:" + StringUtility.escapeQuery(code);
    final String query = StringUtility.composeQuery("AND", termQuery, toCodeClause, fromCodeClause);
    LOGGER.info(
        "findLgPanelMemberByCode: abbrev={} publisher={} versionFilter={} baseLgCode={} code={}",
        terminology.getAbbreviation(), terminology.getPublisher(), versionFilter, baseLgCode, code);
    LOGGER.info("findLgPanelMemberByCode: query=[{}]", query);
    final SearchParameters params = new SearchParameters(query, 1, 0);
    final ResultList<ConceptRelationship> relResult =
        searchService.find(params, ConceptRelationship.class);
    LOGGER.info("findLgPanelMemberByCode: ConceptRelationship find returned {} items",
        relResult.getItems().size());
    if (relResult.getItems().isEmpty()) {
      return null;
    }
    final Concept concept = TerminologyUtility.getConcept(searchService, terminology, code);
    LOGGER.info("findLgPanelMemberByCode: getConcept for code={} => {}", code,
        concept != null ? "found" : "null");
    return concept;
  }
}
