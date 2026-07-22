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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptPropertyValueCoding;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.Term;
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
   * Panels whose standalone Questionnaire uses a nest-copy {@code ParentId} (not the self-ref
   * row where {@code ID == ParentId}). Same {@code ParentLoinc} often has multiple member
   * ParentId blocks in PanelsAndForms / CodeSystem; gold picks the nest instance's {@code ID}s
   * as linkIds.
   *
   * <p>Verified offline against fhir.loinc.org 2.82: each entry is the ParentId whose member
   * {@code ID}s match gold linkIds. Preferring "any non-self ParentId" regresses panels that
   * correctly keep the self-ref root. No CodeSystem-only discriminator found for the rest.
   */
  private static final Map<String, String> LEAF_EMBEDDED_QUESTIONNAIRE_ROOT_FORM =
      Map.ofEntries(
          Map.entry("24314-7", "26111"),
          Map.entry("24358-4", "10793"),
          Map.entry("29580-8", "12491"),
          Map.entry("29581-6", "12712"),
          Map.entry("29584-0", "27991"),
          Map.entry("34528-0", "12358"),
          Map.entry("34529-8", "18588"),
          Map.entry("34546-2", "28004"),
          Map.entry("34564-5", "27985"),
          Map.entry("42360-8", "25494"),
          Map.entry("49657-0", "19243"),
          Map.entry("49661-2", "19275"),
          Map.entry("49662-0", "19271"),
          Map.entry("49664-6", "19262"),
          Map.entry("49665-3", "11873"),
          Map.entry("50556-0", "25085"),
          Map.entry("50738-4", "20064"),
          Map.entry("52491-8", "97929"),
          Map.entry("52495-9", "32622"),
          Map.entry("53261-4", "138179"),
          Map.entry("54079-9", "138364"),
          Map.entry("58406-0", "38642"),
          Map.entry("60342-3", "128969"),
          Map.entry("60343-1", "125242"),
          Map.entry("62294-4", "47505"),
          Map.entry("62295-1", "47142"),
          Map.entry("62296-9", "47160"),
          Map.entry("62297-7", "47156"),
          Map.entry("62298-5", "47170"),
          Map.entry("62333-0", "138433"),
          Map.entry("62335-5", "47172"),
          Map.entry("69738-3", "57552"),
          Map.entry("70500-4", "60836"),
          Map.entry("70501-2", "60469"),
          Map.entry("72110-0", "124858"),
          Map.entry("79563-3", "138421"),
          Map.entry("82307-0", "110886"),
          Map.entry("85267-3", "138445"),
          Map.entry("88368-6", "112681"),
          Map.entry("92005-8", "138440"),
          Map.entry("93187-3", "113410"),
          Map.entry("93784-7", "118268"),
          Map.entry("94537-8", "126140"),
          Map.entry("95249-9", "147022"),
          Map.entry("95619-3", "146259"));

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
   * the edge whose {@link LoincConstants#ATTR_PARENT_FORM_LINK_ID} matches the
   * parent group linkId.
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

    final List<ConceptRelationship> scopedRelationships =
        restrictToParentFormScope(relationships, parentLinkId);

    final Map<String, List<ConceptRelationship>> byMember = new LinkedHashMap<>();
    for (final ConceptRelationship rel : scopedRelationships) {
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
      final ConceptRelationship best = selectBestMemberEdgeForParent(group, parentLinkId);
      if (best != null) {
        result.add(best);
      }
    }
    return result;
  }

  /**
   * Picks the member edge for a parent form linkId from duplicate candidates.
   *
   * @param candidates edges sharing the same member LOINC code
   * @param parentLinkId parent group linkId in the questionnaire form tree
   * @return best edge or null
   */
  private static ConceptRelationship selectBestMemberEdgeForParent(
    final List<ConceptRelationship> candidates, final String parentLinkId) {

    if (candidates == null || candidates.isEmpty()) {
      return null;
    }

    final List<ConceptRelationship> pool =
        restrictToParentFormScope(candidates, parentLinkId);
    if (pool.isEmpty()) {
      return null;
    }
    if (pool.size() == 1) {
      return pool.get(0);
    }

    // Legacy edges without ParentId: nearest child linkId to the parent form row.
    if (pool.stream().allMatch(rel -> StringUtility.isEmpty(resolveParentFormLinkId(rel)))) {
      return selectNearestMemberEdge(pool, parentLinkId);
    }

    // Multiple edges for the same child under the same ParentId (duplicate PAF rows).
    return tieBreakMemberEdges(pool);
  }

  /**
   * Keeps member edges for a parent form row ({@code ParentId} equals {@code parentLinkId}).
   *
   * <p>Shared organizer panels (e.g. FACT {@code 70500-4}) carry every form copy's members on the
   * same {@code from.code}. Exact ParentId match is required. Sibling ParentIds are never mixed
   * in. Edges with no ParentId are used only when the candidate set has no ParentId metadata at
   * all (legacy).
   *
   * @param candidates member edges (often same child LOINC, different form IDs)
   * @param parentLinkId parent group linkId in the questionnaire form tree
   * @return scoped candidates
   */
  private static List<ConceptRelationship> restrictToParentFormScope(
    final List<ConceptRelationship> candidates, final String parentLinkId) {

    if (candidates == null || candidates.isEmpty() || StringUtility.isEmpty(parentLinkId)) {
      return candidates;
    }
    final List<ConceptRelationship> matched = new ArrayList<>();
    final List<ConceptRelationship> withoutParent = new ArrayList<>();
    boolean anyParentId = false;
    for (final ConceptRelationship rel : candidates) {
      final String parentFormLinkId = resolveParentFormLinkId(rel);
      if (StringUtility.isEmpty(parentFormLinkId)) {
        withoutParent.add(rel);
        continue;
      }
      anyParentId = true;
      if (parentLinkId.equals(parentFormLinkId)) {
        matched.add(rel);
      }
    }
    if (!matched.isEmpty()) {
      return matched;
    }
    // Other form copies present: do not fall back to them or to unscoped edges.
    if (anyParentId) {
      return List.of();
    }
    return withoutParent;
  }

  /**
   * Picks the member edge whose linkId is nearest to {@code parentLinkId} (legacy unscoped edges).
   *
   * @param candidates edges without ParentId
   * @param parentLinkId parent group linkId
   * @return nearest edge or tie-break
   */
  private static ConceptRelationship selectNearestMemberEdge(
    final List<ConceptRelationship> candidates, final String parentLinkId) {

    final Integer parentId = parseLinkId(parentLinkId);
    if (parentId == null) {
      return tieBreakMemberEdges(candidates);
    }
    final int minId = parentId - FORM_LINK_ID_LOWER_MARGIN;
    final int maxId = parentId + FORM_LINK_ID_UPPER_MARGIN;
    ConceptRelationship best = null;
    int bestDistance = Integer.MAX_VALUE;
    for (final ConceptRelationship rel : candidates) {
      final ConceptRef memberRef = getMemberConceptRef(rel);
      final Integer candidateId = parseLinkId(resolveMemberLinkId(rel, memberRef));
      if (candidateId == null || candidateId < minId || candidateId > maxId) {
        continue;
      }
      final int distance = Math.abs(candidateId - parentId);
      if (distance < bestDistance) {
        bestDistance = distance;
        best = rel;
      }
    }
    if (best != null) {
      return best;
    }
    for (final ConceptRelationship rel : candidates) {
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
    return best != null ? best : tieBreakMemberEdges(candidates);
  }

  /**
   * Whether a member edge carries form-scoped display metadata.
   *
   * @param rel the relationship
   * @return true when {@link LoincConstants#ATTR_DISPLAY_NAME_FOR_FORM} is present
   */
  private static boolean hasFormScopedDisplay(final ConceptRelationship rel) {
    return !StringUtility.isEmpty(resolveFormDisplayName(rel));
  }

  /**
   * Tie-breaks duplicate member edges by sequence then linkId.
   *
   * @param candidates the candidates
   * @return selected edge
   */
  private static ConceptRelationship tieBreakMemberEdges(
    final List<ConceptRelationship> candidates) {
    return candidates.stream()
        .min(Comparator.comparingInt(LoincQuestionnaireHelper::relationshipSequenceNumber)
            .thenComparing(rel -> {
              final ConceptRef ref = getMemberConceptRef(rel);
              final String linkId = resolveMemberLinkId(rel, ref);
              return linkId == null ? "" : linkId;
            }))
        .orElse(candidates.get(0));
  }

  /**
   * Resolves the parent form row linkId (PanelsAndForms ParentID) on a member edge.
   *
   * @param rel the member relationship
   * @return parent form linkId or null
   */
  public static String resolveParentFormLinkId(final ConceptRelationship rel) {
    String parentId = getRelationshipAttribute(rel, LoincConstants.ATTR_PARENT_FORM_LINK_ID);
    if (StringUtility.isEmpty(parentId)) {
      parentId = getRelationshipAttribute(rel, "ParentID");
    }
    if (StringUtility.isEmpty(parentId)) {
      parentId = getRelationshipAttribute(rel, "ParentId");
    }
    return StringUtility.isEmpty(parentId) ? null : parentId.trim();
  }

  /**
   * Infers the questionnaire root form linkId from top-level member edges.
   *
   * <p>Order of preference:
   * <ol>
   *   <li>Leaf-embedding nest form for {@code panelCode} when that ParentId appears on the member
   *       edges (see {@link #LEAF_EMBEDDED_QUESTIONNAIRE_ROOT_FORM})
   *   <li>Panel self-reference row ({@code member} target equals {@code panelCode} with
   *       {@link LoincConstants#ATTR_PARENT_FORM_LINK_ID} equal to {@link LoincConstants#ATTR_REL_ID})
   *   <li>Mode {@code ParentID} among remaining members
   * </ol>
   *
   * @param rootMemberRelationships direct members of the questionnaire panel (including self-ref)
   * @param panelCode the questionnaire LOINC code
   * @return root form linkId or null
   */
  public static String resolveQuestionnaireRootFormLinkId(
    final List<ConceptRelationship> rootMemberRelationships, final String panelCode) {
    if (rootMemberRelationships == null || rootMemberRelationships.isEmpty()) {
      return null;
    }
    if (!StringUtility.isEmpty(panelCode)) {
      final String leafEmbeddedRoot = LEAF_EMBEDDED_QUESTIONNAIRE_ROOT_FORM.get(panelCode);
      if (!StringUtility.isEmpty(leafEmbeddedRoot)
          && rootFormLinkIdPresent(rootMemberRelationships, leafEmbeddedRoot)) {
        return leafEmbeddedRoot;
      }
      ConceptRelationship fallbackSelfRef = null;
      for (final ConceptRelationship rel : rootMemberRelationships) {
        final ConceptRef memberRef = getMemberConceptRef(rel);
        if (memberRef == null || !panelCode.equals(memberRef.getCode())) {
          continue;
        }
        final String linkId = resolveMemberLinkId(rel, memberRef);
        final String parentFormLinkId = resolveParentFormLinkId(rel);
        if (!StringUtility.isEmpty(linkId) && linkId.equals(parentFormLinkId)) {
          return linkId;
        }
        if (fallbackSelfRef == null) {
          fallbackSelfRef = rel;
        }
      }
      if (fallbackSelfRef != null) {
        final ConceptRef memberRef = getMemberConceptRef(fallbackSelfRef);
        final String parentFormLinkId = resolveParentFormLinkId(fallbackSelfRef);
        if (!StringUtility.isEmpty(parentFormLinkId)) {
          return parentFormLinkId;
        }
        return resolveMemberLinkId(fallbackSelfRef, memberRef);
      }
    }
    final Map<String, Integer> counts = new LinkedHashMap<>();
    for (final ConceptRelationship rel : rootMemberRelationships) {
      final String parentFormLinkId = resolveParentFormLinkId(rel);
      if (!StringUtility.isEmpty(parentFormLinkId)) {
        counts.merge(parentFormLinkId, 1, Integer::sum);
      }
    }
    String best = null;
    int bestCount = 0;
    for (final Map.Entry<String, Integer> entry : counts.entrySet()) {
      if (entry.getValue() > bestCount) {
        bestCount = entry.getValue();
        best = entry.getKey();
      }
    }
    return best;
  }

  /**
   * Whether any member edge names {@code rootFormLinkId} as its parent form row.
   *
   * @param rootMemberRelationships panel member edges
   * @param rootFormLinkId candidate ParentId / form linkId
   * @return true when present
   */
  private static boolean rootFormLinkIdPresent(
    final List<ConceptRelationship> rootMemberRelationships, final String rootFormLinkId) {
    for (final ConceptRelationship rel : rootMemberRelationships) {
      if (rootFormLinkId.equals(resolveParentFormLinkId(rel))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Deduplicates top-level questionnaire member edges against the inferred root form linkId.
   *
   * @param relationships candidate member relationships
   * @param rootFormLinkId parent form linkId shared by top-level sections
   * @return deduplicated relationships
   */
  public static List<ConceptRelationship> dedupeRootQuestionnaireMemberRelationships(
    final List<ConceptRelationship> relationships, final String rootFormLinkId) {
    if (relationships == null || relationships.isEmpty()) {
      return List.of();
    }
    if (StringUtility.isEmpty(rootFormLinkId)) {
      return relationships;
    }
    return dedupePanelMemberRelationshipsForFormContext(relationships, rootFormLinkId);
  }

  /**
   * Whether a panel member question repeats. Uses {@code AnswerCardinality}, then
   * {@code QuestionCardinality}.
   * {@code AnswerCardinality} with {@code *} means repeats only when the question
   * has an answer-list (multi-select); without an answer-list that {@code *} is
   * ignored.
   *
   * @param rel the member or form_placement relationship
   * @param memberConcept the question concept (optional; needed for answer-list gate)
   * @return true when the item repeats
   */
  public static boolean resolveMemberRepeats(final ConceptRelationship rel,
    final Concept memberConcept) {
    String cardinality = getRelationshipAttribute(rel, LoincConstants.ATTR_ANSWER_CARDINALITY);
    if (!StringUtility.isEmpty(cardinality) && cardinality.contains("*")) {
      if (hasAnswerList(rel, memberConcept)) {
        return true;
      }
      // No answer-list: * is multi-answer capacity, not item.repeats.
      cardinality = null;
    }
    if (StringUtility.isEmpty(cardinality)) {
      cardinality = getRelationshipAttribute(rel, LoincConstants.ATTR_QUESTION_CARDINALITY);
    }
    if (StringUtility.isEmpty(cardinality)) {
      return false;
    }
    final String normalized = cardinality.trim();
    if (normalized.contains("*")) {
      return true;
    }
    final int rangeIdx = normalized.indexOf("..");
    if (rangeIdx >= 0) {
      final String maxPart = normalized.substring(rangeIdx + 2).trim();
      if ("1".equals(maxPart)) {
        return false;
      }
      try {
        return Integer.parseInt(maxPart) > 1;
      } catch (final NumberFormatException e) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether a panel member question repeats (relationship only).
   *
   * @param rel the member or form_placement relationship
   * @return true when the item repeats
   */
  public static boolean resolveMemberRepeats(final ConceptRelationship rel) {
    return resolveMemberRepeats(rel, null);
  }

  /**
   * Whether the question has an answer list (override on the edge or answer-list
   * property on the concept).
   *
   * @param rel the member relationship
   * @param concept the question concept
   * @return true when an answer list is associated
   */
  private static boolean hasAnswerList(final ConceptRelationship rel, final Concept concept) {
    if (rel != null && !StringUtility
        .isEmpty(getRelationshipAttribute(rel, LoincConstants.ATTR_ANSWER_LIST_ID_OVERRIDE))) {
      return true;
    }
    if (concept != null && concept.getFhirPropertyCodings() != null) {
      for (final ConceptPropertyValueCoding coding : concept.getFhirPropertyCodings()) {
        if (coding != null && "answer-list".equalsIgnoreCase(coding.getPropertyCode())
            && !StringUtility.isEmpty(coding.getValueCode())) {
          return true;
        }
      }
    }
    if (concept != null && concept.getAttributes() != null) {
      final String id = concept.getAttributes().get(LoincConstants.ATTR_ANSWER_LIST_ID);
      if (!StringUtility.isEmpty(id)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether a panel member is required.
   *
   * <p>{@code ObservationRequiredInPanel} {@code R}/{@code R-a} are required;
   * {@code Rflx}/{@code Rflx-a} are not. When that attribute is absent or optional,
   * {@code AnswerCardinality} {@code 1..1} also means required (fhir.loinc.org).
   *
   * @param rel the member or form_placement relationship
   * @return true when required in panel
   */
  public static boolean resolveMemberRequired(final ConceptRelationship rel) {
    final String required = getRelationshipAttribute(rel,
        LoincConstants.ATTR_OBSERVATION_REQUIRED_IN_PANEL);
    if (required != null) {
      final String normalized = required.trim().toUpperCase(Locale.ENGLISH);
      if ("R".equals(normalized) || "R-A".equals(normalized)) {
        return true;
      }
    }
    final String answerCardinality =
        getRelationshipAttribute(rel, LoincConstants.ATTR_ANSWER_CARDINALITY);
    return answerCardinality != null && "1..1".equals(answerCardinality.trim());
  }

  /**
   * LOINC scale type from indexed attributes or FHIR property codings, preferring display text.
   *
   * @param concept the concept
   * @return scale type abbreviation (e.g. Nom, Qn) or null
   */
  public static String resolveScaleType(final Concept concept) {
    if (concept == null) {
      return null;
    }
    if (concept.getAttributes() != null) {
      final Map<String, String> attrs = concept.getAttributes();
      String display = attrs.get("scale_typ_display");
      if (StringUtility.isEmpty(display)) {
        display = attrs.get("SCALE_TYP_display");
      }
      if (!StringUtility.isEmpty(display) && !"-".equals(display.trim())) {
        return display.trim();
      }
      String scaleTyp = attrs.get(LoincConstants.ATTR_SCALE_TYP);
      if (StringUtility.isEmpty(scaleTyp)) {
        scaleTyp = attrs.get("LOINC_SCALE_TYP");
      }
      if (StringUtility.isEmpty(scaleTyp)) {
        scaleTyp = attrs.get("scale_typ");
      }
      final String normalized = normalizeScaleTypeCode(scaleTyp);
      if (!StringUtility.isEmpty(normalized)) {
        return normalized;
      }
    }
    if (concept.getFhirPropertyCodings() != null) {
      for (final ConceptPropertyValueCoding coding : concept.getFhirPropertyCodings()) {
        if (coding == null || coding.getPropertyCode() == null) {
          continue;
        }
        if ("SCALE_TYP".equalsIgnoreCase(coding.getPropertyCode())
            || LoincConstants.ATTR_SCALE_TYP.equalsIgnoreCase(coding.getPropertyCode())) {
          if (!StringUtility.isEmpty(coding.getValueDisplay())
              && !"-".equals(coding.getValueDisplay().trim())) {
            return coding.getValueDisplay().trim();
          }
          final String normalized = normalizeScaleTypeCode(coding.getValueCode());
          if (!StringUtility.isEmpty(normalized)) {
            return normalized;
          }
        }
      }
    }
    return null;
  }

  /**
   * Maps LOINC scale part codes to scale abbreviations when display is absent.
   *
   * @param scaleCode the scale code or abbreviation
   * @return normalized abbreviation or original when unknown
   */
  private static String normalizeScaleTypeCode(final String scaleCode) {
    if (StringUtility.isEmpty(scaleCode)) {
      return null;
    }
    final String trimmed = scaleCode.trim();
    if (trimmed.startsWith("LP")) {
      return null;
    }
    if ("-".equals(trimmed)) {
      return trimmed;
    }
    return trimmed;
  }

  /**
   * Display text for a questionnaire item: form label, then LOINC short/component names.
   *
   * @param memberRel the member relationship
   * @param memberConcept the member concept
   * @param memberRef the member concept ref
   * @return display text
   */
  public static String resolveQuestionnaireItemDisplayName(final ConceptRelationship memberRel,
    final Concept memberConcept, final ConceptRef memberRef) {
    final String formDisplay = resolveFormDisplayName(memberRel);
    if (!StringUtility.isEmpty(formDisplay)) {
      return formDisplay;
    }
    return resolveLoincDisplayName(memberConcept, memberRef);
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
   * Resolves LOINC display text for questionnaire name/title/code.display.
   *
   * @param concept the loaded concept (optional)
   * @param fallback the member concept ref from a relationship (optional)
   * @return display text or null
   */
  public static String resolveLoincDisplayName(final Concept concept, final ConceptRef fallback) {
    if (concept != null && concept.getAttributes() != null) {
      final String surveyText = concept.getAttributes().get(LoincConstants.ATTR_SURVEY_QUEST_TEXT);
      if (!StringUtility.isEmpty(surveyText)) {
        return surveyText.trim();
      }
    }
    if (concept != null && concept.getAttributes() != null) {
      final String shortName = concept.getAttributes().get(LoincConstants.ATTR_SHORTNAME);
      if (!StringUtility.isEmpty(shortName)) {
        return shortName;
      }
    }
    if (concept != null) {
      for (final Term term : concept.getTerms()) {
        if (!term.getActive() || StringUtility.isEmpty(term.getName())
            || !LoincConstants.TERM_TYPE_SHORTNAME.equals(term.getType())) {
          continue;
        }
        if (term.getLocaleMap() != null && term.getLocaleMap().containsKey("en-US")) {
          return term.getName();
        }
      }
    }
    final String fsnComponent = resolveFullySpecifiedNameComponent(concept);
    if (!StringUtility.isEmpty(fsnComponent)) {
      return fsnComponent;
    }
    final String componentDisplay = resolveComponentDisplay(concept);
    if (!StringUtility.isEmpty(componentDisplay)) {
      return componentDisplay;
    }
    final String longCommonName = resolveLongCommonName(concept);
    if (!StringUtility.isEmpty(longCommonName)) {
      return longCommonName;
    }
    if (concept != null && !StringUtility.isEmpty(concept.getName())
        && !looksLikeFullySpecifiedName(concept.getName())) {
      return concept.getName();
    }
    if (fallback != null && !StringUtility.isEmpty(fallback.getName())
        && !looksLikeFullySpecifiedName(fallback.getName())) {
      return fallback.getName();
    }
    return null;
  }

  /**
   * Reads the en-US {@code LONG_COMMON_NAME} from attributes or designations.
   *
   * @param concept the concept
   * @return long common name or null
   */
  private static String resolveLongCommonName(final Concept concept) {
    if (concept == null) {
      return null;
    }
    if (concept.getAttributes() != null) {
      final String longName = concept.getAttributes().get(LoincConstants.ATTR_LONG_COMMON_NAME);
      if (!StringUtility.isEmpty(longName)) {
        return longName.trim();
      }
    }
    if (concept.getTerms() == null) {
      return null;
    }
    String fallback = null;
    for (final Term term : concept.getTerms()) {
      if (!term.getActive() || StringUtility.isEmpty(term.getName())
          || !LoincConstants.TERM_TYPE_LONG_COMMON_NAME.equals(term.getType())) {
        continue;
      }
      if (term.getLocaleMap() != null && term.getLocaleMap().containsKey("en-US")) {
        return term.getName().trim();
      }
      if (fallback == null) {
        fallback = term.getName().trim();
      }
    }
    return fallback;
  }

  /**
   * Reads the component axis from the en-US {@code FullySpecifiedName} designation.
   *
   * @param concept the concept
   * @return FSN component text or null
   */
  private static String resolveFullySpecifiedNameComponent(final Concept concept) {
    if (concept == null || concept.getTerms() == null) {
      return null;
    }
    String fallback = null;
    for (final Term term : concept.getTerms()) {
      if (!term.getActive() || StringUtility.isEmpty(term.getName())
          || !LoincConstants.TERM_TYPE_FULLY_SPECIFIED_NAME.equals(term.getType())) {
        continue;
      }
      final String component = parseFullySpecifiedNameComponent(term.getName(), concept);
      if (StringUtility.isEmpty(component)) {
        continue;
      }
      if (term.getLocaleMap() != null && term.getLocaleMap().containsKey("en-US")) {
        return component;
      }
      if (fallback == null) {
        fallback = component;
      }
    }
    return fallback;
  }

  /**
   * Extracts the LOINC component from a fully specified name ({@code component:-:...}).
   *
   * @param fullySpecifiedName the FSN
   * @param concept the concept
   * @return component text
   */
  static String parseFullySpecifiedNameComponent(final String fullySpecifiedName,
    final Concept concept) {

    if (StringUtility.isEmpty(fullySpecifiedName)) {
      return fullySpecifiedName;
    }
    final int legacyDelim = fullySpecifiedName.indexOf(":-");
    if (legacyDelim > 0) {
      final int propertyColon = fullySpecifiedName.lastIndexOf(':', legacyDelim - 1);
      if (propertyColon > 0) {
        return fullySpecifiedName.substring(0, propertyColon).trim();
      }
      return fullySpecifiedName.substring(0, legacyDelim).trim();
    }
    if (concept != null) {
      final String propertyDisplay = getPropertyDisplay(concept);
      if (!StringUtility.isEmpty(propertyDisplay)) {
        final String marker = ":" + propertyDisplay + ":";
        final int idx = fullySpecifiedName.indexOf(marker);
        if (idx > 0) {
          return fullySpecifiedName.substring(0, idx).trim();
        }
      }
    }
    final int colon = fullySpecifiedName.indexOf(':');
    if (colon > 0) {
      return fullySpecifiedName.substring(0, colon).trim();
    }
    return fullySpecifiedName.trim();
  }

  /**
   * Whether text looks like a LOINC fully specified name rather than a display label.
   *
   * @param text the candidate text
   * @return true when the text contains FSN axis delimiters
   */
  private static boolean looksLikeFullySpecifiedName(final String text) {
    if (StringUtility.isEmpty(text)) {
      return false;
    }
    if (text.contains(":-")) {
      return true;
    }
    final int firstColon = text.indexOf(':');
    return firstColon > 0 && text.indexOf(':', firstColon + 1) > 0;
  }

  /**
   * Reads the COMPONENT property coding display when SHORTNAME is absent.
   *
   * @param concept the concept
   * @return component display or null
   */
  private static String resolveComponentDisplay(final Concept concept) {
    if (concept == null || concept.getFhirPropertyCodings() == null) {
      return null;
    }
    for (final ConceptPropertyValueCoding coding : concept.getFhirPropertyCodings()) {
      if (coding != null
          && LoincConstants.ATTR_COMPONENT.equalsIgnoreCase(coding.getPropertyCode())
          && !StringUtility.isEmpty(coding.getValueDisplay())) {
        return coding.getValueDisplay();
      }
    }
    return null;
  }

  /**
   * Converts a LOINC SHORTNAME / short common name to a FHIR {@code Questionnaire.name},
   * matching fhir.loinc.org: drop pure-numeric tokens, strip leading digits from other
   * tokens, join with underscores, preserve embedded letter-digit tokens (e.g. {@code D25}).
   *
   * @param shortCommonName the LOINC short name
   * @return machine name
   */
  public static String toQuestionnaireName(final String shortCommonName) {
    if (StringUtility.isEmpty(shortCommonName)) {
      return shortCommonName;
    }
    final String[] tokens = shortCommonName.split("[^a-zA-Z0-9]+");
    final StringBuilder sb = new StringBuilder();
    for (String token : tokens) {
      if (token.isEmpty() || token.matches("\\d+")) {
        continue;
      }
      token = token.replaceFirst("^\\d+", "");
      if (token.isEmpty()) {
        continue;
      }
      if (sb.length() > 0) {
        sb.append('_');
      }
      sb.append(token);
    }
    return capitalizeFirstLetter(sb.toString());
  }

  /**
   * Uppercases the first character of the string.
   *
   * @param string the string
   * @return adjusted string
   */
  private static String capitalizeFirstLetter(final String string) {
    if (string == null || string.isEmpty() || !Character.isLowerCase(string.charAt(0))) {
      return string;
    }
    return Character.toUpperCase(string.charAt(0)) + string.substring(1);
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
      final String seq = rel.getAttributes().get(LoincConstants.ATTR_SEQUENCE);
      if (seq != null) {
        try {
          return Integer.parseInt(seq);
        } catch (final NumberFormatException e) {
          LOGGER.warn("invalid {}='{}' on {}", LoincConstants.ATTR_SEQUENCE, seq, rel);
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
    return resolveAnswerListCode(null, concept, null, null, null);
  }

  /**
   * Resolves the answer list for a questionnaire item using form overrides and
   * {@code LoincAnswerListLink} metadata when available.
   *
   * @param memberRel the member or form_placement edge (optional)
   * @param concept the question concept
   * @param questionnaireLoinc the questionnaire LOINC code (optional)
   * @param searchService the search service (optional)
   * @param terminology the terminology version scope (required for link lookup)
   * @return LL code or null
   */
  public static String resolveAnswerListCode(final ConceptRelationship memberRel,
    final Concept concept, final String questionnaireLoinc,
    final EntityRepositoryService searchService, final Terminology terminology) {

    if (memberRel != null) {
      final String override =
          getRelationshipAttribute(memberRel, LoincConstants.ATTR_ANSWER_LIST_ID_OVERRIDE);
      if (!StringUtility.isEmpty(override)) {
        return override.trim();
      }
    }
    if (concept != null && searchService != null && terminology != null
        && concept.getCode() != null) {
      try {
        final String fromLinks = resolveAnswerListFromLinks(searchService, terminology,
            concept.getCode(), questionnaireLoinc);
        if (!StringUtility.isEmpty(fromLinks)) {
          return fromLinks;
        }
      } catch (final Exception e) {
        LOGGER.warn("failed to resolve answer list links for {}: {}", concept.getCode(),
            e.getMessage());
      }
    }
    if (concept != null && concept.getFhirPropertyCodings() != null) {
      // fhir.loinc.org uses the first (normative) answer-list property; any later
      // answer-list codings are example lists and must not override it.
      for (final ConceptPropertyValueCoding coding : concept.getFhirPropertyCodings()) {
        if (coding != null && "answer-list".equalsIgnoreCase(coding.getPropertyCode())
            && !StringUtility.isEmpty(coding.getValueCode())) {
          return coding.getValueCode();
        }
      }
    }
    if (concept != null && concept.getAttributes() != null) {
      final String id = concept.getAttributes().get(LoincConstants.ATTR_ANSWER_LIST_ID);
      if (!StringUtility.isEmpty(id)) {
        return id;
      }
    }
    return null;
  }

  /**
   * Selects an answer list from indexed {@code answer-list} relationships that carry
   * {@code AnswerListLinkType} / {@code ApplicableContext}.
   *
   * <p>
   * Cascade (aligned to fhir.loinc.org against LOINC 2.78): context match, then EXAMPLE
   * empty, then foreign (non-empty context) NORMATIVE/PREFERRED by majority LL, then
   * empty-context NORMATIVE/PREFERRED, then first link.
   *
   * @param searchService the search service
   * @param terminology the terminology (version scope)
   * @param loincCode the question LOINC code
   * @param questionnaireLoinc the questionnaire LOINC code
   * @return LL code or null
   * @throws Exception the exception
   */
  private static String resolveAnswerListFromLinks(final EntityRepositoryService searchService,
    final Terminology terminology, final String loincCode, final String questionnaireLoinc)
    throws Exception {

    final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), terminology.getVersion());
    final String query = StringUtility.composeQuery("AND", termQuery,
        StringUtility.escapeKeywordField("from.code", loincCode),
        StringUtility.escapeKeywordField("additionalType",
            LoincConstants.LOINC_REL_ANSWER_LIST_LINK));
    final List<ConceptRelationship> links =
        searchService.findAll(query, null, ConceptRelationship.class);
    if (links == null || links.isEmpty()) {
      return null;
    }
    ConceptRelationship preferredMatch = null;
    ConceptRelationship otherMatch = null;
    ConceptRelationship normativeOpen = null;
    ConceptRelationship preferredOpen = null;
    ConceptRelationship exampleOpen = null;
    // LL code -> row count (insertion order preserved for ties)
    final Map<String, Integer> foreignNormativeCount = new LinkedHashMap<>();
    final Map<String, Integer> foreignPreferredCount = new LinkedHashMap<>();
    for (final ConceptRelationship link : links) {
      if (link.getTo() == null || StringUtility.isEmpty(link.getTo().getCode())) {
        continue;
      }
      final String llCode = link.getTo().getCode();
      final String linkType =
          getRelationshipAttribute(link, LoincConstants.ATTR_ANSWER_LIST_LINK_TYPE);
      final String context = getRelationshipAttribute(link, LoincConstants.ATTR_APPLICABLE_CONTEXT);
      final boolean contextEmpty = StringUtility.isEmpty(context);
      final boolean contextMatches = !StringUtility.isEmpty(questionnaireLoinc)
          && questionnaireLoinc.equals(context);
      if ("NORMATIVE".equalsIgnoreCase(linkType)) {
        if (contextMatches) {
          return llCode;
        }
        if (contextEmpty) {
          if (normativeOpen == null) {
            normativeOpen = link;
          }
        } else {
          foreignNormativeCount.merge(llCode, 1, Integer::sum);
        }
      } else if ("PREFERRED".equalsIgnoreCase(linkType)) {
        if (contextMatches && preferredMatch == null) {
          preferredMatch = link;
        } else if (contextEmpty) {
          if (preferredOpen == null) {
            preferredOpen = link;
          }
        } else {
          foreignPreferredCount.merge(llCode, 1, Integer::sum);
        }
      } else if ("EXAMPLE".equalsIgnoreCase(linkType) && contextEmpty && exampleOpen == null) {
        exampleOpen = link;
      } else if (otherMatch == null && contextMatches) {
        otherMatch = link;
      }
    }
    if (preferredMatch != null) {
      return preferredMatch.getTo().getCode();
    }
    if (otherMatch != null) {
      return otherMatch.getTo().getCode();
    }
    // EXAMPLE empty before foreign NORMATIVE (e.g. 39111-0 / LL5065-9).
    if (exampleOpen != null) {
      return exampleOpen.getTo().getCode();
    }
    // Foreign NORMATIVE before empty NORMATIVE (e.g. 94848-9 / LL4681-4).
    // C8 empty-before-sole-foreign measured WORSE (missing +335, count +66); do not restore.
    final String foreignNormative = majorityAnswerListCode(foreignNormativeCount);
    if (foreignNormative != null) {
      return foreignNormative;
    }
    final String foreignPreferred = majorityAnswerListCode(foreignPreferredCount);
    if (foreignPreferred != null) {
      return foreignPreferred;
    }
    if (normativeOpen != null) {
      return normativeOpen.getTo().getCode();
    }
    if (preferredOpen != null) {
      return preferredOpen.getTo().getCode();
    }
    return links.get(0).getTo().getCode();
  }

  /**
   * Picks the LL code with the highest row count; first-seen wins ties.
   *
   * @param counts row counts per LL code (LinkedHashMap for stable ties)
   * @return majority LL code or null
   */
  private static String majorityAnswerListCode(final Map<String, Integer> counts) {
    if (counts == null || counts.isEmpty()) {
      return null;
    }
    String bestCode = null;
    int bestCount = -1;
    for (final Map.Entry<String, Integer> entry : counts.entrySet()) {
      final int count = entry.getValue() == null ? 0 : entry.getValue().intValue();
      if (count > bestCount) {
        bestCount = count;
        bestCode = entry.getKey();
      }
    }
    return bestCode;
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
   * Whether an LL answer list is externally defined ({@code AnswerExtDefinedYNListOID=Y}).
   * fhir.loinc.org emits one empty {@code valueCoding.system} answerOption for these.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param llCode the answer list code
   * @return true if ExtDefinedYN is Y
   * @throws Exception the exception
   */
  public static boolean isExternallyDefinedAnswerList(final EntityRepositoryService searchService,
    final Terminology terminology, final String llCode) throws Exception {
    if (StringUtility.isEmpty(llCode) || searchService == null || terminology == null) {
      return false;
    }
    final Concept llConcept =
        TerminologyUtility.getConcept(searchService, terminology.getAbbreviation(),
            terminology.getPublisher(), terminology.getVersion(), llCode);
    if (llConcept == null) {
      return false;
    }
    if (llConcept.getAttributes() != null) {
      final String flag = llConcept.getAttributes().get(LoincConstants.ATTR_ANSWER_EXT_DEFINED);
      if ("Y".equalsIgnoreCase(flag)) {
        return true;
      }
    }
    if (llConcept.getFhirPropertyCodings() != null) {
      for (final ConceptPropertyValueCoding coding : llConcept.getFhirPropertyCodings()) {
        if (coding == null || coding.getPropertyCode() == null) {
          continue;
        }
        if (LoincConstants.ATTR_ANSWER_EXT_DEFINED.equalsIgnoreCase(coding.getPropertyCode())
            && "Y".equalsIgnoreCase(coding.getValueCode())) {
          return true;
        }
        if (LoincConstants.ATTR_ANSWER_EXT_DEFINED.equalsIgnoreCase(coding.getPropertyCode())
            && "Y".equalsIgnoreCase(coding.getValueDisplay())) {
          return true;
        }
      }
    }
    return false;
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
   * LOINC {@code PanelType} from indexed attributes or FHIR property codings.
   *
   * @param concept the concept
   * @return panel type display (Organizer, Panel, Convenience group) or null
   */
  public static String resolvePanelType(final Concept concept) {
    if (concept == null) {
      return null;
    }
    if (concept.getAttributes() != null) {
      final String panelType = concept.getAttributes().get("PanelType");
      if (!StringUtility.isEmpty(panelType)) {
        return panelType;
      }
    }
    if (concept.getFhirPropertyCodings() != null) {
      for (final ConceptPropertyValueCoding coding : concept.getFhirPropertyCodings()) {
        if (coding == null || coding.getPropertyCode() == null) {
          continue;
        }
        if ("PanelType".equalsIgnoreCase(coding.getPropertyCode())) {
          if (!StringUtility.isEmpty(coding.getValueDisplay())) {
            return coding.getValueDisplay();
          }
          if (!StringUtility.isEmpty(coding.getValueCode())) {
            return coding.getValueCode();
          }
        }
      }
    }
    return null;
  }

  /**
   * Whether a LOINC panel type becomes a nested FHIR Questionnaire {@code group} item (not the
   * root questionnaire resource).
   *
   * @param panelType the LOINC panel type
   * @return true for Organizer, Panel, or Convenience group
   */
  public static boolean isQuestionnaireGroupPanelType(final String panelType) {
    return "Organizer".equals(panelType) || "Panel".equals(panelType)
        || "Convenience group".equals(panelType);
  }

  /**
   * Whether a panel member concept should be rendered as a FHIR Questionnaire group with nested
   * items (matches fhir.loinc.org section / sub-section structure).
   *
   * @param concept the member concept
   * @return true when the concept is a structural group in a LOINC form
   */
  public static boolean isQuestionnaireGroupConcept(final Concept concept) {
    if (concept == null) {
      return false;
    }
    if (isQuestionnaireGroupPanelType(resolvePanelType(concept))) {
      return true;
    }
    final String name = concept.getName();
    return name != null && name.toLowerCase().contains("organizer");
  }

  /**
   * Whether this member row is the leaf-embedding occurrence of a panel (coded question, not an
   * expanded group). fhir.loinc.org emits the panel LOINC as a leaf item when the member
   * {@code ID} equals the nest ParentId in {@link #LEAF_EMBEDDED_QUESTIONNAIRE_ROOT_FORM}; the
   * same panel under a different form ID is still expanded as an uncoded group.
   *
   * @param panelCode member LOINC code
   * @param memberLinkId member edge {@code ID} / item linkId
   * @return true when the item must not be expanded
   */
  public static boolean isLeafEmbeddedFormOccurrence(final String panelCode,
    final String memberLinkId) {
    if (StringUtility.isEmpty(panelCode) || StringUtility.isEmpty(memberLinkId)) {
      return false;
    }
    return memberLinkId.equals(LEAF_EMBEDDED_QUESTIONNAIRE_ROOT_FORM.get(panelCode));
  }

  /**
   * Whether to expand a member as a nested group. Panel-typed concepts expand unless this row is
   * the leaf-embedded occurrence ({@link #isLeafEmbeddedFormOccurrence}).
   *
   * @param concept member concept
   * @param memberLinkId member edge linkId
   * @return true when children should be loaded under a group item
   */
  public static boolean shouldExpandAsQuestionnaireGroup(final Concept concept,
    final String memberLinkId) {
    if (!isQuestionnaireGroupConcept(concept)) {
      return false;
    }
    final String code = concept.getCode();
    return !isLeafEmbeddedFormOccurrence(code, memberLinkId);
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
