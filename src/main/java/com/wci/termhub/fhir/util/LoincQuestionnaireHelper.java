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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptPropertyValueCoding;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.TerminologyUtility;

/**
 * LOINC questionnaire item fields derived from indexed panel member
 * relationships.
 */
public final class LoincQuestionnaireHelper {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(LoincQuestionnaireHelper.class);

  /**
   * Lower bound below parent linkId when scoping shared organizer member edges.
   */
  private static final int FORM_LINK_ID_LOWER_MARGIN = 50;

  /**
   * Upper bound above parent linkId when scoping shared organizer member edges.
   */
  private static final int FORM_LINK_ID_UPPER_MARGIN = 1500;

  /**
   * Instantiates a new loinc questionnaire helper.
   */
  private LoincQuestionnaireHelper() {
    // utility class
  }

  /**
   * Reads a persisted member-edge attribute from a relationship.
   *
   * @param rel the relationship
   * @param name the attribute name
   * @return the value or null
   */
  public static String getRelationshipAttribute(final ConceptRelationship rel, final String name) {
    if (rel == null || name == null || rel.getAttributes() == null) {
      return null;
    }
    return rel.getAttributes().get(name);
  }

  /**
   * Resolves FHIR Questionnaire.item.linkId from the parent member edge.
   *
   * @param rel the member relationship
   * @param memberRef the member concept ref
   * @return link id or null
   */
  public static String resolveMemberLinkId(final ConceptRelationship rel,
    final ConceptRef memberRef) {
    final String id = getRelationshipAttribute(rel, LoincConstants.ATTR_REL_ID);
    if (StringUtility.isEmpty(id)) {
      LOGGER.warn("missing {} on member edge, rel={}, memberRef={}", LoincConstants.ATTR_REL_ID,
          rel, memberRef);
    }
    return id;
  }

  /**
   * Member concept on a panel membership relationship.
   *
   * @param rel the relationship
   * @return member concept ref or null
   */
  public static ConceptRef getMemberConceptRef(final ConceptRelationship rel) {
    if (rel == null) {
      return null;
    }
    final String additionalType = rel.getAdditionalType();
    if (LoincConstants.LOINC_REL_HAS_MEMBER.equals(additionalType)
        || LoincConstants.LOINC_REL_PANEL_MEMBER.equals(additionalType)) {
      return rel.getTo();
    }
    if (Boolean.TRUE.equals(rel.getHierarchical())) {
      return rel.getFrom();
    }
    return rel.getTo();
  }

  /**
   * When the same organizer LOINC panel appears in multiple questionnaires,
   * member edges duplicate per child code with different form {@code ID}s. Keep
   * one edge per member code whose linkId sits in the parent item's numeric
   * band (e.g. 99160-4 vs 106529-1).
   *
   * @param relationships candidate member relationships
   * @param parentLinkId parent group item linkId
   * @return deduplicated relationships (unchanged when parent linkId is absent)
   */
  public static List<ConceptRelationship> dedupePanelMemberRelationshipsForFormContext(
    final List<ConceptRelationship> relationships, final String parentLinkId) {

    if (relationships == null || relationships.isEmpty()) {
      return List.of();
    }
    if (StringUtility.isEmpty(parentLinkId)) {
      return relationships;
    }
    final int parentId;
    try {
      parentId = Integer.parseInt(parentLinkId.trim());
    } catch (final NumberFormatException e) {
      return relationships;
    }
    final int minId = parentId - FORM_LINK_ID_LOWER_MARGIN;
    final int maxId = parentId + FORM_LINK_ID_UPPER_MARGIN;

    final Map<String, List<ConceptRelationship>> byMember = new LinkedHashMap<>();
    for (final ConceptRelationship rel : relationships) {
      final ConceptRef memberRef = getMemberConceptRef(rel);
      if (memberRef == null || memberRef.getCode() == null) {
        continue;
      }
      byMember.computeIfAbsent(memberRef.getCode(), code -> new ArrayList<>()).add(rel);
    }

    final List<ConceptRelationship> result = new ArrayList<>();
    for (final List<ConceptRelationship> group : byMember.values()) {
      if (group.size() == 1) {
        result.add(group.get(0));
        continue;
      }
      ConceptRelationship best = null;
      int bestDistance = Integer.MAX_VALUE;
      for (final ConceptRelationship rel : group) {
        final ConceptRef memberRef = getMemberConceptRef(rel);
        final String linkId = resolveMemberLinkId(rel, memberRef);
        final Integer candidateId = parseLinkId(linkId);
        if (candidateId == null || candidateId < minId || candidateId > maxId) {
          continue;
        }
        final int distance = Math.abs(candidateId - parentId);
        if (distance < bestDistance) {
          bestDistance = distance;
          best = rel;
        }
      }
      if (best == null) {
        LOGGER.warn("no member edge in form band [{}, {}] for parent linkId={}, member codes={}",
            minId, maxId, parentLinkId, group.stream().map(rel -> {
              final ConceptRef ref = getMemberConceptRef(rel);
              return ref != null ? ref.getCode() : "?";
            }).distinct().toList());
        for (final ConceptRelationship rel : group) {
          final ConceptRef memberRef = getMemberConceptRef(rel);
          final Integer candidateId = parseLinkId(resolveMemberLinkId(rel, memberRef));
          if (candidateId == null) {
            continue;
          }
          final int distance = Math.abs(candidateId - parentId);
          if (distance < bestDistance) {
            bestDistance = distance;
            best = rel;
          }
        }
      }
      if (best != null) {
        result.add(best);
      }
    }
    return result;
  }

  /**
   * Parses the link id.
   *
   * @param linkId the link id
   * @return the integer
   */
  private static Integer parseLinkId(final String linkId) {
    if (StringUtility.isEmpty(linkId)) {
      return null;
    }
    try {
      return Integer.parseInt(linkId.trim());
    } catch (final NumberFormatException e) {
      return null;
    }
  }

  /**
   * Resolves FHIR Questionnaire.item.prefix from the parent member edge.
   *
   * @param rel the member relationship
   * @return prefix or null
   */
  public static String resolveFormPrefix(final ConceptRelationship rel) {
    return getRelationshipAttribute(rel, LoincConstants.ATTR_OBSERVATION_ID_IN_FORM);
  }

  /**
   * Resolves FHIR Questionnaire.item.text / code.display from the parent member
   * edge.
   *
   * @param rel the member relationship
   * @return form display or null
   */
  public static String resolveFormDisplayName(final ConceptRelationship rel) {
    return getRelationshipAttribute(rel, LoincConstants.ATTR_DISPLAY_NAME_FOR_FORM);
  }

  /**
   * Sequence number for a panel member relationship.
   *
   * @param rel the relationship
   * @return sequence number or max value if unknown
   */
  public static int relationshipSequenceNumber(final ConceptRelationship rel) {
    if (rel == null) {
      return Integer.MAX_VALUE;
    }
    if (rel.getAttributes() != null) {
      final String seq = rel.getAttributes().get(LoincConstants.ATTR_SEQ_NO);
      if (seq != null) {
        try {
          return Integer.parseInt(seq);
        } catch (final NumberFormatException e) {
          LOGGER.warn("invalid {}='{}' on {}", LoincConstants.ATTR_SEQ_NO, seq, rel);
        }
      }
    }
    if (rel.getGroup() != null) {
      try {
        return Integer.parseInt(rel.getGroup());
      } catch (final NumberFormatException e) {
        LOGGER.warn("invalid group='{}' on {}", rel.getGroup(), rel);
      }
    }
    return Integer.MAX_VALUE;
  }

  /**
   * LOINC answer-list id (LL code) from concept properties.
   *
   * @param concept the question concept
   * @return LL code or null
   */
  public static String resolveAnswerListCode(final Concept concept) {
    if (concept == null) {
      return null;
    }
    if (concept.getFhirPropertyCodings() != null) {
      for (final ConceptPropertyValueCoding coding : concept.getFhirPropertyCodings()) {
        if (coding != null && "answer-list".equalsIgnoreCase(coding.getPropertyCode())
            && !StringUtility.isEmpty(coding.getValueCode())) {
          return coding.getValueCode();
        }
      }
    }
    if (concept.getAttributes() != null) {
      final String id = concept.getAttributes().get(LoincConstants.ATTR_ANSWER_LIST_ID);
      if (!StringUtility.isEmpty(id)) {
        return id;
      }
    }
    return null;
  }

  /**
   * LA answer concepts for an LL answer list (via indexed
   * {@code parents.code}).
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param llCode the answer list code
   * @return LA concepts ordered by concept {@code SequenceNumber}
   * @throws Exception the exception
   */
  public static List<Concept> findAnswerListMembers(final EntityRepositoryService searchService,
    final Terminology terminology, final String llCode) throws Exception {

    if (StringUtility.isEmpty(llCode) || searchService == null || terminology == null) {
      return Collections.emptyList();
    }
    final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), terminology.getVersion());
    final List<Concept> fromMemberEdges =
        findAnswerListMembersFromPanelEdges(searchService, terminology, termQuery, llCode);
    if (!fromMemberEdges.isEmpty()) {
      orderAnswerListMembers(fromMemberEdges);
      return fromMemberEdges;
    }
    final String relQuery = StringUtility.composeQuery("AND", termQuery,
        "to.code:" + StringUtility.escapeQuery(llCode), "hierarchical:true");
    final List<ConceptRelationship> parentRels =
        searchService.findAll(relQuery, null, ConceptRelationship.class);
    if (parentRels != null && !parentRels.isEmpty()) {
      parentRels
          .sort(Comparator.comparingInt(LoincQuestionnaireHelper::relationshipSequenceNumber));
      final List<Concept> ordered = new ArrayList<>();
      final Set<String> seen = new LinkedHashSet<>();
      for (final ConceptRelationship rel : parentRels) {
        if (rel.getFrom() == null || rel.getFrom().getCode() == null) {
          continue;
        }
        final String laCode = rel.getFrom().getCode();
        if (!laCode.startsWith("LA") || !seen.add(laCode)) {
          continue;
        }
        final Concept la =
            TerminologyUtility.getConcept(searchService, terminology.getAbbreviation(),
                terminology.getPublisher(), terminology.getVersion(), laCode);
        if (la != null) {
          ordered.add(la);
        }
      }
      if (!ordered.isEmpty()) {
        orderAnswerListMembers(ordered);
        return ordered;
      }
    }
    final String conceptQuery = StringUtility.composeQuery("AND", termQuery,
        "parents.code:" + StringUtility.escapeQuery(llCode));
    final SearchParameters params = new SearchParameters(conceptQuery, 1000, 0);
    final ResultList<Concept> result = searchService.find(params, Concept.class);
    if (result == null || result.getItems() == null) {
      return Collections.emptyList();
    }
    final List<Concept> laConcepts = new ArrayList<>();
    for (final Concept concept : result.getItems()) {
      if (concept != null && concept.getCode() != null && concept.getCode().startsWith("LA")) {
        laConcepts.add(concept);
      }
    }
    orderAnswerListMembers(laConcepts);
    return laConcepts;
  }

  /**
   * Orders LA answer-list members by concept {@code SequenceNumber} (matches
   * fhir.loinc.org LL expansion).
   *
   * @param members LA concepts to order in place
   */
  public static void orderAnswerListMembers(final List<Concept> members) {
    LoincValueSetHelper.sortLlMembersBySequenceNumber(members);
  }

  /**
   * LA answer concepts for an LL list via indexed {@code member} edges on the
   * LL concept.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param termQuery the terminology query clause
   * @param llCode the answer list code
   * @return ordered LA concepts or empty
   * @throws Exception the exception
   */
  private static List<Concept> findAnswerListMembersFromPanelEdges(
    final EntityRepositoryService searchService, final Terminology terminology,
    final String termQuery, final String llCode) throws Exception {

    String memberQuery = StringUtility.composeQuery("AND", termQuery,
        StringUtility.escapeKeywordField("from.code", llCode),
        StringUtility.escapeKeywordField("additionalType", LoincConstants.LOINC_REL_PANEL_MEMBER));
    List<ConceptRelationship> memberRels =
        searchService.findAll(memberQuery, null, ConceptRelationship.class);
    if (memberRels == null || memberRels.isEmpty()) {
      memberQuery = StringUtility.composeQuery("AND", termQuery,
          StringUtility.escapeKeywordField("from.code", llCode),
          StringUtility.escapeKeywordField("additionalType", LoincConstants.LOINC_REL_HAS_MEMBER));
      memberRels = searchService.findAll(memberQuery, null, ConceptRelationship.class);
    }
    if (memberRels == null || memberRels.isEmpty()) {
      return Collections.emptyList();
    }
    memberRels.sort(Comparator.comparingInt(LoincQuestionnaireHelper::relationshipSequenceNumber)
        .thenComparing(rel -> {
          if (rel.getTo() == null || rel.getTo().getCode() == null) {
            return "";
          }
          return rel.getTo().getCode();
        }));
    final List<Concept> ordered = new ArrayList<>();
    final Set<String> seen = new LinkedHashSet<>();
    for (final ConceptRelationship rel : memberRels) {
      if (rel.getTo() == null || rel.getTo().getCode() == null) {
        continue;
      }
      final String laCode = rel.getTo().getCode();
      if (!laCode.startsWith("LA") || !seen.add(laCode)) {
        continue;
      }
      final Concept la = TerminologyUtility.getConcept(searchService, terminology.getAbbreviation(),
          terminology.getPublisher(), terminology.getVersion(), laCode);
      if (la != null) {
        ordered.add(la);
      }
    }
    return ordered;
  }

  /**
   * Whether the concept's LOINC PROPERTY is Date (not scale type alone).
   *
   * @param concept the concept
   * @return true when PROPERTY display is Date
   */
  public static boolean isDateProperty(final Concept concept) {
    final String display = getPropertyDisplay(concept);
    return display != null && "Date".equalsIgnoreCase(display.trim());
  }

  /**
   * LOINC PROPERTY display for a concept.
   *
   * @param concept the concept
   * @return property display or null
   */
  public static String getPropertyDisplay(final Concept concept) {
    if (concept == null) {
      return null;
    }
    if (concept.getAttributes() != null) {
      String display = concept.getAttributes().get(LoincConstants.ATTR_PROPERTY + "_display");
      if (!StringUtility.isEmpty(display)) {
        return display;
      }
      display = concept.getAttributes().get("property_display");
      if (!StringUtility.isEmpty(display)) {
        return display;
      }
    }
    if (concept.getFhirPropertyCodings() != null) {
      for (final ConceptPropertyValueCoding coding : concept.getFhirPropertyCodings()) {
        if (coding == null || coding.getPropertyCode() == null) {
          continue;
        }
        if (LoincConstants.ATTR_PROPERTY.equalsIgnoreCase(coding.getPropertyCode())
            && !StringUtility.isEmpty(coding.getValueDisplay())) {
          return coding.getValueDisplay();
        }
      }
    }
    return null;
  }

  /**
   * Reads {@link LoincConstants#ATTR_EXTERNAL_COPYRIGHT_NOTICE} from a concept.
   *
   * @param concept the concept
   * @return notice text or null
   */
  public static String getExternalCopyrightNotice(final Concept concept) {
    if (concept == null || concept.getAttributes() == null) {
      return null;
    }
    final String notice =
        concept.getAttributes().get(LoincConstants.ATTR_EXTERNAL_COPYRIGHT_NOTICE);
    return StringUtility.isEmpty(notice) ? null : notice;
  }

  /**
   * Builds questionnaire copyright: CodeSystem copyright plus unique external
   * notices for LOINC codes appearing in the questionnaire (depth-first order).
   *
   * @param terminologyCopyright the CodeSystem root copyright
   * @param loincCodes LOINC codes referenced in the questionnaire
   * @param searchService the search service
   * @param abbreviation the terminology abbreviation
   * @param publisher the publisher
   * @param version the terminology version
   * @return combined copyright or null
   * @throws Exception the exception
   */
  public static String buildQuestionnaireCopyright(final String terminologyCopyright,
    final Iterable<String> loincCodes, final EntityRepositoryService searchService,
    final String abbreviation, final String publisher, final String version) throws Exception {

    final StringBuilder copyright = new StringBuilder();
    if (!StringUtility.isEmpty(terminologyCopyright)) {
      copyright.append(terminologyCopyright);
    }
    if (searchService == null || loincCodes == null) {
      return copyright.length() == 0 ? null : copyright.toString();
    }

    final Set<String> seenNotices = new LinkedHashSet<>();
    for (final String code : loincCodes) {
      if (StringUtility.isEmpty(code)) {
        continue;
      }
      final Concept concept =
          TerminologyUtility.getConcept(searchService, abbreviation, publisher, version, code);
      final String notice = getExternalCopyrightNotice(concept);
      if (!StringUtility.isEmpty(notice)) {
        seenNotices.add(notice);
      }
    }

    for (final String notice : seenNotices) {
      if (copyright.length() > 0) {
        copyright.append("\r\n");
      }
      copyright.append(notice);
    }
    return copyright.length() == 0 ? null : copyright.toString();
  }
}
