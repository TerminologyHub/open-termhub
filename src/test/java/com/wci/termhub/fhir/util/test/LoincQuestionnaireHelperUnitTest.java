/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.util.LoincConstants;
import com.wci.termhub.fhir.util.LoincQuestionnaireHelper;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptPropertyValueCoding;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.Term;

/**
 * Unit tests for {@link LoincQuestionnaireHelper}.
 */
public class LoincQuestionnaireHelperUnitTest {

  /**
   * Test resolve member link id from relationship.
   */
  @Test
  public void testResolveMemberLinkIdFromRelationship() {
    final ConceptRelationship rel = new ConceptRelationship();
    rel.getAttributes().put(LoincConstants.ATTR_REL_ID, "127313");
    rel.getAttributes().put(LoincConstants.ATTR_OBSERVATION_ID_IN_FORM, "A");
    rel.getAttributes().put(LoincConstants.ATTR_DISPLAY_NAME_FOR_FORM,
        "Administrative Information");
    rel.getAttributes().put(LoincConstants.ATTR_SEQUENCE, "1");

    assertEquals("127313",
        LoincQuestionnaireHelper.resolveMemberLinkId(rel, new ConceptRef("99161-2", null)));
    assertEquals("A", LoincQuestionnaireHelper.resolveFormPrefix(rel));
    assertEquals("Administrative Information",
        LoincQuestionnaireHelper.resolveFormDisplayName(rel));
    assertEquals(1, LoincQuestionnaireHelper.relationshipSequenceNumber(rel));
  }

  /**
   * Test is date property from fhir property coding.
   */
  @Test
  public void testIsDatePropertyFromFhirPropertyCoding() {
    final Concept concept = new Concept();
    final ConceptPropertyValueCoding property = new ConceptPropertyValueCoding();
    property.setPropertyCode("PROPERTY");
    property.setValueCode("LP182451-7");
    property.setValueDisplay("Date");
    concept.getFhirPropertyCodings().add(property);

    assertTrue(LoincQuestionnaireHelper.isDateProperty(concept));
    assertEquals("Date", LoincQuestionnaireHelper.getPropertyDisplay(concept));
  }

  /**
   * Test is date property from attributes.
   */
  @Test
  public void testIsDatePropertyFromAttributes() {
    final Concept concept = new Concept();
    concept.getAttributes().putAll(Map.of("PROPERTY_display", "Date"));

    assertTrue(LoincQuestionnaireHelper.isDateProperty(concept));
  }

  /**
   * ClockTime PROPERTY is distinct from Date.
   */
  @Test
  public void testIsClockTimePropertyFromAttributes() {
    final Concept concept = new Concept();
    concept.getAttributes().put(LoincConstants.ATTR_PROPERTY + "_display", "ClockTime");

    assertTrue(LoincQuestionnaireHelper.isClockTimeProperty(concept));
    assertFalse(LoincQuestionnaireHelper.isDateProperty(concept));
  }

  /**
   * EXAMPLE_UNITS=score (also {score} / UCUM fallback) forces decimal / no answer-list expansion /
   * bare item emit (FORMULA irrelevant).
   */
  @Test
  public void testIsScoreExampleUnits() {
    final Concept scoreUnits = new Concept();
    scoreUnits.getAttributes().put(LoincConstants.ATTR_EXAMPLE_UNITS, "score");
    assertTrue(LoincQuestionnaireHelper.isScoreExampleUnits(scoreUnits));

    final Concept bracedUnits = new Concept();
    bracedUnits.getAttributes().put(LoincConstants.ATTR_EXAMPLE_UNITS, "{score}");
    assertTrue(LoincQuestionnaireHelper.isScoreExampleUnits(bracedUnits));

    final Concept ucumFallback = new Concept();
    ucumFallback.getAttributes().put(LoincConstants.ATTR_EXAMPLE_UCUM_UNITS, "{score}");
    assertTrue(LoincQuestionnaireHelper.isScoreExampleUnits(ucumFallback));

    final Concept withFormula = new Concept();
    withFormula.getAttributes().put(LoincConstants.ATTR_EXAMPLE_UNITS, "score");
    withFormula.getAttributes().put(LoincConstants.ATTR_FORMULA, "Score A + Score B");
    assertTrue(LoincQuestionnaireHelper.isScoreExampleUnits(withFormula));

    final Concept otherUnits = new Concept();
    otherUnits.getAttributes().put(LoincConstants.ATTR_EXAMPLE_UNITS, "mg/dL");
    assertFalse(LoincQuestionnaireHelper.isScoreExampleUnits(otherUnits));

    final Concept otherUcumOnly = new Concept();
    otherUcumOnly.getAttributes().put(LoincConstants.ATTR_EXAMPLE_UCUM_UNITS, "mg/dL");
    assertFalse(LoincQuestionnaireHelper.isScoreExampleUnits(otherUcumOnly));

    final Concept nonScoreUcumIgnoredWhenUnitsSet = new Concept();
    nonScoreUcumIgnoredWhenUnitsSet.getAttributes().put(LoincConstants.ATTR_EXAMPLE_UNITS,
        "mg/dL");
    nonScoreUcumIgnoredWhenUnitsSet.getAttributes().put(LoincConstants.ATTR_EXAMPLE_UCUM_UNITS,
        "{score}");
    assertFalse(LoincQuestionnaireHelper.isScoreExampleUnits(nonScoreUcumIgnoredWhenUnitsSet));
  }

  /**
   * Test get external copyright notice.
   */
  @Test
  public void testGetExternalCopyrightNotice() {
    final Concept concept = new Concept();
    concept.getAttributes().put(LoincConstants.ATTR_EXTERNAL_COPYRIGHT_NOTICE,
        "Copyright © Pfizer Inc. All rights reserved.");

    assertEquals("Copyright © Pfizer Inc. All rights reserved.",
        LoincQuestionnaireHelper.getExternalCopyrightNotice(concept));
  }

  /**
   * Test order answer list members by sequence number.
   */
  @Test
  public void testOrderAnswerListMembersBySequenceNumber() {
    final Concept laDeath = new Concept();
    laDeath.setCode("LA6179-1");
    laDeath.getAttributes().put("SequenceNumber", "7");
    final Concept laSoc = new Concept();
    laSoc.setCode("LA6390-4");
    laSoc.getAttributes().put("SequenceNumber", "1");
    final Concept laRoc = new Concept();
    laRoc.setCode("LA6366-4");
    laRoc.getAttributes().put("SequenceNumber", "2");

    final java.util.List<Concept> members =
        new java.util.ArrayList<>(java.util.List.of(laDeath, laRoc, laSoc));
    LoincQuestionnaireHelper.orderAnswerListMembers(members);

    assertEquals("LA6390-4", members.get(0).getCode());
    assertEquals("LA6366-4", members.get(1).getCode());
    assertEquals("LA6179-1", members.get(2).getCode());
  }

  /**
   * Test dedupe panel member relationships for form context.
   */
  @Test
  public void testDedupePanelMemberRelationshipsForFormContext() {
    final ConceptRelationship roc = memberRel("127887", "46589-8");
    final ConceptRelationship discharge = memberRel("145176", "46589-8");
    final ConceptRelationship otherForm = memberRel("127136", "58104-1");
    final ConceptRelationship rocM1710 = memberRel("127888", "58104-1");

    final List<ConceptRelationship> deduped =
        LoincQuestionnaireHelper.dedupePanelMemberRelationshipsForFormContext(
            List.of(discharge, roc, otherForm, rocM1710), "127886");

    assertEquals(2, deduped.size());
    assertEquals("127887", linkIdForMember(deduped, "46589-8"));
    assertEquals("127888", linkIdForMember(deduped, "58104-1"));
  }

  /**
   * ParentFormLinkId selects the correct member edge when linkIds span forms.
   */
  @Test
  public void testDedupePanelMemberRelationshipsByParentFormLinkId() {
    final ConceptRelationship wrongForm = memberRel("31341", "52455-2");
    wrongForm.getAttributes().put(LoincConstants.ATTR_DISPLAY_NAME_FOR_FORM, "Laboratory");
    wrongForm.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "32168");

    final ConceptRelationship correctForm = memberRel("56580", "52455-2");
    correctForm.getAttributes().put(LoincConstants.ATTR_DISPLAY_NAME_FOR_FORM, "Laboratory");
    correctForm.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "32270");

    final List<ConceptRelationship> deduped = LoincQuestionnaireHelper
        .dedupePanelMemberRelationshipsForFormContext(List.of(wrongForm, correctForm), "32270");

    assertEquals(1, deduped.size());
    assertEquals("56580",
        LoincQuestionnaireHelper.resolveMemberLinkId(deduped.get(0), deduped.get(0).getTo()));
  }

  /**
   * FACT emotional well-being ({@code 70500-4}): same child LOINC under many
   * ParentIds on one panel. Exact ParentId wins; nearest/lowest sibling IDs
   * must not cascade (70505-3 hotspot).
   */
  @Test
  public void testDedupeFactSharedPanelUsesExactParentId() {
    final ConceptRelationship siblingEarly = memberRel("58764", "70392-6");
    siblingEarly.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "58680");

    final ConceptRelationship correct = memberRel("58858", "70392-6");
    correct.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "58857");

    final ConceptRelationship siblingLate = memberRel("58881", "70392-6");
    siblingLate.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "58880");

    final List<ConceptRelationship> deduped =
        LoincQuestionnaireHelper.dedupePanelMemberRelationshipsForFormContext(
            List.of(siblingEarly, correct, siblingLate), "58857");

    assertEquals(1, deduped.size());
    assertEquals("58858",
        LoincQuestionnaireHelper.resolveMemberLinkId(deduped.get(0), deduped.get(0).getTo()));
  }

  /**
   * Contaminated pool with sibling Additional-concerns ParentIds: keep only the
   * path parent.
   */
  @Test
  public void testDedupeRejectsSiblingAdditionalConcernsParentId() {
    final ConceptRelationship factBr = memberRel("59421", "70302-5");
    factBr.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "61106");

    final ConceptRelationship factCns = memberRel("59434", "70302-5");
    factCns.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "61108");

    final List<ConceptRelationship> deduped = LoincQuestionnaireHelper
        .dedupePanelMemberRelationshipsForFormContext(List.of(factBr, factCns), "61108");

    assertEquals(1, deduped.size());
    assertEquals("59434",
        LoincQuestionnaireHelper.resolveMemberLinkId(deduped.get(0), deduped.get(0).getTo()));
  }

  /**
   * AnswerCardinality multi-range enables repeats only with an answer-list;
   * otherwise QuestionCardinality decides (e.g. {@code 1..n}).
   */
  @Test
  public void testResolveMemberRepeatsFromAnswerCardinalityRange() {
    final ConceptRelationship rel = new ConceptRelationship();
    rel.getAttributes().put(LoincConstants.ATTR_ANSWER_CARDINALITY, "1..7");
    assertFalse(LoincQuestionnaireHelper.resolveMemberRepeats(rel));

    final Concept withAnswerList = new Concept();
    withAnswerList.getAttributes().put(LoincConstants.ATTR_ANSWER_LIST_ID, "LL1-1");
    assertTrue(LoincQuestionnaireHelper.resolveMemberRepeats(rel, withAnswerList));

    rel.getAttributes().put(LoincConstants.ATTR_ANSWER_CARDINALITY, "0..1");
    assertFalse(LoincQuestionnaireHelper.resolveMemberRepeats(rel));
  }

  /**
   * Scale type is read from FHIR property codings when attributes are absent.
   */
  @Test
  public void testResolveScaleTypeFromFhirPropertyCoding() {
    final Concept concept = new Concept();
    final ConceptPropertyValueCoding scaleTyp = new ConceptPropertyValueCoding();
    scaleTyp.setPropertyCode("SCALE_TYP");
    scaleTyp.setValueCode("LP7750-5");
    scaleTyp.setValueDisplay("Nom");
    concept.getFhirPropertyCodings().add(scaleTyp);

    assertEquals("Nom", LoincQuestionnaireHelper.resolveScaleType(concept));
  }

  /**
   * ObservationRequiredInPanel=R maps to required items.
   */
  @Test
  public void testResolveMemberRequired() {
    final ConceptRelationship rel = new ConceptRelationship();
    rel.getAttributes().put(LoincConstants.ATTR_OBSERVATION_REQUIRED_IN_PANEL, "R");
    assertTrue(LoincQuestionnaireHelper.resolveMemberRequired(rel));

    final ConceptRelationship ra = new ConceptRelationship();
    ra.getAttributes().put(LoincConstants.ATTR_OBSERVATION_REQUIRED_IN_PANEL, "R-a");
    assertTrue(LoincQuestionnaireHelper.resolveMemberRequired(ra));

    // PAF lowercase r (unique in 2.82) is not required — do not uppercase to R.
    final ConceptRelationship lowercaseR = new ConceptRelationship();
    lowercaseR.getAttributes().put(LoincConstants.ATTR_OBSERVATION_REQUIRED_IN_PANEL, "r");
    assertFalse(LoincQuestionnaireHelper.resolveMemberRequired(lowercaseR));
  }

  /**
   * AnswerCardinality 1..1 maps to required when ObservationRequiredInPanel is
   * not R.
   */
  @Test
  public void testResolveMemberRequiredFromAnswerCardinality() {
    final ConceptRelationship rel = new ConceptRelationship();
    rel.getAttributes().put(LoincConstants.ATTR_OBSERVATION_REQUIRED_IN_PANEL, "O");
    rel.getAttributes().put(LoincConstants.ATTR_ANSWER_CARDINALITY, "1..1");
    assertTrue(LoincQuestionnaireHelper.resolveMemberRequired(rel));

    final ConceptRelationship optional = new ConceptRelationship();
    optional.getAttributes().put(LoincConstants.ATTR_OBSERVATION_REQUIRED_IN_PANEL, "O");
    optional.getAttributes().put(LoincConstants.ATTR_ANSWER_CARDINALITY, "0..1");
    assertFalse(LoincQuestionnaireHelper.resolveMemberRequired(optional));
  }

  /**
   * QuestionCardinality 0..* enables repeats.
   */
  @Test
  public void testResolveMemberRepeatsFromQuestionCardinality() {
    final ConceptRelationship rel = new ConceptRelationship();
    rel.getAttributes().put(LoincConstants.ATTR_QUESTION_CARDINALITY, "0..*");
    assertTrue(LoincQuestionnaireHelper.resolveMemberRepeats(rel));
  }

  /**
   * QuestionCardinality 1..n still repeats when AnswerCardinality is 1..1.
   */
  @Test
  public void testResolveMemberRepeatsQuestionCardinalityNotBlockedByAnswerCardinality() {
    final ConceptRelationship rel = new ConceptRelationship();
    rel.getAttributes().put(LoincConstants.ATTR_ANSWER_CARDINALITY, "1..1");
    rel.getAttributes().put(LoincConstants.ATTR_QUESTION_CARDINALITY, "1..n");
    assertTrue(LoincQuestionnaireHelper.resolveMemberRepeats(rel));

    // Multi AnswerCardinality without answer-list does not force repeats.
    rel.getAttributes().put(LoincConstants.ATTR_ANSWER_CARDINALITY, "0..4");
    rel.getAttributes().remove(LoincConstants.ATTR_QUESTION_CARDINALITY);
    assertFalse(LoincQuestionnaireHelper.resolveMemberRepeats(rel));
  }

  /**
   * Root form linkId is inferred from the mode ParentID on top-level members.
   */
  @Test
  public void testResolveQuestionnaireRootFormLinkId() {
    final ConceptRelationship a = memberRel("32169", "52452-0");
    a.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "32168");
    final ConceptRelationship b = memberRel("32270", "52453-8");
    b.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "32168");
    final ConceptRelationship c = memberRel("99999", "52454-6");
    c.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "88888");

    assertEquals("32168",
        LoincQuestionnaireHelper.resolveQuestionnaireRootFormLinkId(List.of(a, b, c), "52451-8"));
  }

  /**
   * Child edges for parent 38702 are not pulled into an unrelated form row
   * (55774).
   */
  @Test
  public void testDedupePanelMemberRelationshipsDoesNotMixFormSubtrees() {
    final ConceptRelationship morphologySection = memberRel("38707", "58407-8");
    morphologySection.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "38702");

    final ConceptRelationship rbcSection = memberRel("38716", "58406-0");
    rbcSection.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "38702");

    final List<ConceptRelationship> dedupedUnderWrongParent =
        LoincQuestionnaireHelper.dedupePanelMemberRelationshipsForFormContext(
            List.of(morphologySection, rbcSection), "55774");

    assertTrue(dedupedUnderWrongParent.isEmpty());

    final ConceptRelationship correctFormSmear = memberRel("38702", "34994-4");
    correctFormSmear.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "10182");

    final ConceptRelationship wrongFormSmear = memberRel("55774", "34994-4");
    wrongFormSmear.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "55699");

    final List<ConceptRelationship> dedupedAtRoot =
        LoincQuestionnaireHelper.dedupePanelMemberRelationshipsForFormContext(
            List.of(correctFormSmear, wrongFormSmear), "10182");

    assertEquals(1, dedupedAtRoot.size());
    assertEquals("38702", LoincQuestionnaireHelper.resolveMemberLinkId(dedupedAtRoot.get(0),
        dedupedAtRoot.get(0).getTo()));
  }

  /**
   * Root form linkId comes from the panel self-reference row (ParentId equals
   * ID).
   */
  @Test
  public void testResolveQuestionnaireRootFormLinkIdFromSelfReference() {
    final ConceptRelationship hemogramGroup = memberRel("10171", "24358-4");
    hemogramGroup.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "10170");

    final ConceptRelationship platelets = memberRel("10180", "26515-7");
    platelets.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "10170");

    final ConceptRelationship wrongFormGroup = memberRel("85850", "24358-4");
    wrongFormGroup.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "85849");

    final ConceptRelationship selfRef = memberRel("10170", "24317-0");
    selfRef.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "10170");

    assertEquals("10170", LoincQuestionnaireHelper.resolveQuestionnaireRootFormLinkId(
        List.of(hemogramGroup, platelets, wrongFormGroup, selfRef), "24317-0"));
  }

  /**
   * Leaf-embedded panels prefer the nest form ParentId over the self-ref row
   * when that form is present on the member edges.
   */
  @Test
  public void testResolveQuestionnaireRootFormLinkIdPrefersLeafEmbeddedNestForm() {
    final ConceptRelationship selfRef = memberRel("11908", "29580-8");
    selfRef.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "11908");

    final ConceptRelationship selfChild = memberRel("11909", "26466-3");
    selfChild.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "11908");

    final ConceptRelationship nestChild = memberRel("12492", "26466-3");
    nestChild.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "12491");

    assertEquals("12491", LoincQuestionnaireHelper
        .resolveQuestionnaireRootFormLinkId(List.of(selfRef, selfChild, nestChild), "29580-8"));
  }

  /**
   * TORCH panel 24314-7: CodeSystem has self-root ParentId 10155 and nest
   * ParentId 26111; gold Questionnaire linkIds are the nest copy IDs.
   */
  @Test
  public void testResolveQuestionnaireRootFormLinkIdTorchNestForm() {
    final ConceptRelationship selfRef = memberRel("10155", "24314-7");
    selfRef.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "10155");

    final ConceptRelationship selfChild = memberRel("10156", "9422-7");
    selfChild.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "10155");

    final ConceptRelationship nestChild = memberRel("68478", "9422-7");
    nestChild.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "26111");

    assertEquals("26111", LoincQuestionnaireHelper
        .resolveQuestionnaireRootFormLinkId(List.of(selfRef, selfChild, nestChild), "24314-7"));
  }

  /**
   * Leaf-embed nest ParentId occurrence is a coded leaf (do not expand); other
   * form IDs still expand as groups.
   */
  @Test
  public void testLeafEmbeddedFormOccurrenceIsNotExpanded() {
    assertTrue(LoincQuestionnaireHelper.isLeafEmbeddedFormOccurrence("29580-8", "12491"));
    assertFalse(LoincQuestionnaireHelper.isLeafEmbeddedFormOccurrence("29580-8", "11908"));
    assertFalse(LoincQuestionnaireHelper.isLeafEmbeddedFormOccurrence("29580-8", null));

    final Concept panel = new Concept();
    panel.setCode("29580-8");
    panel.getAttributes().put("PanelType", "Panel");
    assertFalse(LoincQuestionnaireHelper.shouldExpandAsQuestionnaireGroup(panel, "12491"));
    assertTrue(LoincQuestionnaireHelper.shouldExpandAsQuestionnaireGroup(panel, "11908"));

    final Concept emotional = new Concept();
    emotional.setCode("70500-4");
    emotional.getAttributes().put("PanelType", "Panel");
    // Nested under FACT forms uses non-map IDs → still expand
    assertTrue(LoincQuestionnaireHelper.shouldExpandAsQuestionnaireGroup(emotional, "58880"));
    // Standalone nest root occurrence → leaf
    assertFalse(LoincQuestionnaireHelper.shouldExpandAsQuestionnaireGroup(emotional, "60836"));

    // HIV NRTI genotype/phenotype: leaf under one form copy, expand under the other
    final Concept nrtiGeno = new Concept();
    nrtiGeno.setCode("49658-8");
    nrtiGeno.getAttributes().put("PanelType", "Panel");
    assertFalse(LoincQuestionnaireHelper.shouldExpandAsQuestionnaireGroup(nrtiGeno, "11844"));
    assertTrue(LoincQuestionnaireHelper.shouldExpandAsQuestionnaireGroup(nrtiGeno, "19232"));

    final Concept nrtiPheno = new Concept();
    nrtiPheno.setCode("49663-8");
    nrtiPheno.getAttributes().put("PanelType", "Panel");
    assertFalse(LoincQuestionnaireHelper.shouldExpandAsQuestionnaireGroup(nrtiPheno, "11875"));
    assertTrue(LoincQuestionnaireHelper.shouldExpandAsQuestionnaireGroup(nrtiPheno, "19263"));

    // Genetic analysis / NB panels: gold leaf-embeds under parent form linkIds
    final Concept geneticSummary = new Concept();
    geneticSummary.setCode("55232-3");
    geneticSummary.getAttributes().put("PanelType", "Panel");
    assertTrue(LoincQuestionnaireHelper.isLeafEmbeddedFormOccurrence("55232-3", "30074"));
    assertFalse(LoincQuestionnaireHelper.isLeafEmbeddedFormOccurrence("55232-3", "99999"));
    assertFalse(LoincQuestionnaireHelper.shouldExpandAsQuestionnaireGroup(geneticSummary, "30074"));
    assertTrue(LoincQuestionnaireHelper.shouldExpandAsQuestionnaireGroup(geneticSummary, "99999"));

    final Concept mps2Nb = new Concept();
    mps2Nb.setCode("104188-8");
    mps2Nb.getAttributes().put("PanelType", "Panel");
    assertTrue(LoincQuestionnaireHelper.isLeafEmbeddedFormOccurrence("104188-8", "144301"));
    assertFalse(LoincQuestionnaireHelper.shouldExpandAsQuestionnaireGroup(mps2Nb, "144301"));
    assertTrue(LoincQuestionnaireHelper.shouldExpandAsQuestionnaireGroup(mps2Nb, "99999"));

    // All cycle string-vs-group leaf-embed pairs (panelCode → member linkId)
    final String[][] leafEmbedPairs = {
        {"104188-8", "144301"}, {"111825-6", "152212"}, {"54078-1", "27712"},
        {"54076-5", "27729"}, {"54081-5", "27750"}, {"54082-3", "27761"},
        {"55232-3", "30074"}, {"55207-5", "30117"}, {"51956-1", "70576"},
        {"62355-3", "47376"}, {"62367-8", "47385"}, {"62343-9", "47402"},
        {"62386-8", "47490"}, {"70495-7", "58645"}, {"70499-9", "58676"},
        {"70498-1", "58677"}, {"70503-8", "58663"}, {"70497-3", "58664"},
        {"70496-5", "58665"}, {"70571-5", "60059"}, {"70585-5", "60073"},
        {"70606-9", "60094"}, {"70624-2", "60113"}, {"70635-8", "60212"},
        {"70636-6", "60258"}, {"70637-4", "60263"}, {"70649-9", "60319"},
        {"70655-6", "60388"}, {"74078-7", "66162"}, {"76463-9", "72153"},
        {"76456-3", "72154"}, {"76455-5", "72155"}, {"76454-8", "72156"},
        {"76453-0", "72157"}, {"76452-2", "72158"}, {"77638-5", "73318"},
        {"77637-7", "73319"}, {"92252-6", "110869"}, {"94128-6", "118684"},
        {"94391-0", "120323"}, {"95800-9", "122386"},
    };
    for (final String[] pair : leafEmbedPairs) {
      assertTrue(LoincQuestionnaireHelper.isLeafEmbeddedFormOccurrence(pair[0], pair[1]),
          "expected leaf-embed " + pair[0] + "@" + pair[1]);
      assertFalse(LoincQuestionnaireHelper.isLeafEmbeddedFormOccurrence(pair[0], "0"),
          "expected non-map linkId not leaf-embed for " + pair[0]);
    }
  }

  /**
   * Leaf-embedded override is ignored when that ParentId is absent from the
   * member edges.
   */
  @Test
  public void testResolveQuestionnaireRootFormLinkIdIgnoresAbsentLeafEmbeddedForm() {
    final ConceptRelationship selfRef = memberRel("11908", "29580-8");
    selfRef.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "11908");

    final ConceptRelationship selfChild = memberRel("11909", "26466-3");
    selfChild.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "11908");

    assertEquals("11908", LoincQuestionnaireHelper
        .resolveQuestionnaireRootFormLinkId(List.of(selfRef, selfChild), "29580-8"));
  }

  /**
   * Link id for member.
   *
   * @param rels the rels
   * @param memberCode the member code
   * @return the string
   */
  private static String linkIdForMember(final java.util.List<ConceptRelationship> rels,
    final String memberCode) {
    for (final ConceptRelationship rel : rels) {
      if (rel.getTo() != null && memberCode.equals(rel.getTo().getCode())) {
        return LoincQuestionnaireHelper.resolveMemberLinkId(rel, rel.getTo());
      }
    }
    return null;
  }

  /**
   * Member rel.
   *
   * @param linkId the link id
   * @param memberCode the member code
   * @return the concept relationship
   */
  private static ConceptRelationship memberRel(final String linkId, final String memberCode) {
    final ConceptRelationship rel = new ConceptRelationship();
    rel.setAdditionalType(LoincConstants.LOINC_REL_PANEL_MEMBER);
    rel.setTo(new ConceptRef(memberCode, null));
    rel.getAttributes().put(LoincConstants.ATTR_REL_ID, linkId);
    return rel;
  }

  /**
   * Test build questionnaire copyright base only without external notices.
   */
  @Test
  public void testBuildQuestionnaireCopyrightBaseOnlyWithoutSearchService() {
    final String base = "LOINC base copyright";
    final String result = LoincQuestionnaireHelper.buildQuestionnaireCopyright(base, null);

    assertEquals(base, result);
  }

  /**
   * Test build questionnaire copyright appends collected external notices.
   */
  @Test
  public void testBuildQuestionnaireCopyrightWithExternalNotices() {
    assertEquals("LOINC base\r\nCopyright © Pfizer Inc. All rights reserved.",
        LoincQuestionnaireHelper.buildQuestionnaireCopyright("LOINC base",
            java.util.List.of("Copyright © Pfizer Inc. All rights reserved.")));
  }

  /**
   * Test AdditionalCopyright from a form-scoped panel edge is collected.
   */
  @Test
  public void testAddAdditionalCopyrightNoticeFromRelationship() {
    final ConceptRelationship rel = memberRel("113152", "93025-5");
    rel.getAttributes().put(LoincConstants.ATTR_ADDITIONAL_COPYRIGHT,
        "© 2019. National Association of Community Health Centers, Inc.");
    final java.util.Set<String> notices = new java.util.LinkedHashSet<>();
    LoincQuestionnaireHelper.addAdditionalCopyrightNotice(rel, notices);
    assertEquals(
        java.util.Set.of("© 2019. National Association of Community Health Centers, Inc."),
        notices);
  }

  /**
   * Test {@link LoincQuestionnaireHelper#toQuestionnaireName(String)} matches
   * fhir.loinc.org.
   */
  @Test
  public void testToQuestionnaireName() {
    assertEquals("Filaria_Ab_IgG_IgM_Pnl_Ser",
        LoincQuestionnaireHelper.toQuestionnaireName("Filaria Ab.IgG + IgM Pnl Ser"));
    assertEquals("Lytes_Pnl_Fld", LoincQuestionnaireHelper.toQuestionnaireName("Lytes 3 Pnl Fld"));
    assertEquals("Hepatitis_Pnl_Ser",
        LoincQuestionnaireHelper.toQuestionnaireName("Hepatitis 1996 Pnl Ser"));
    assertEquals("GTT_gest_h_Pnl_Ur_SerPl",
        LoincQuestionnaireHelper.toQuestionnaireName("GTT gest 2h Pnl Ur SerPl"));
    assertEquals("Lead_EKG_Pnl", LoincQuestionnaireHelper.toQuestionnaireName("12 lead EKG Pnl"));
    assertEquals("Fluid_IO_Pnl_h",
        LoincQuestionnaireHelper.toQuestionnaireName("Fluid IO Pnl 24h"));
    assertEquals("Cardiac_D_echo_panel",
        LoincQuestionnaireHelper.toQuestionnaireName("Cardiac 2D echo panel"));
    assertEquals("Creat_H_Cl_Pnl_Ur_SerPl",
        LoincQuestionnaireHelper.toQuestionnaireName("Creat 24H Cl Pnl Ur SerPl"));
    assertEquals("Vit_D25_D1_OH_D1_Pnl_SerPl_mCnc",
        LoincQuestionnaireHelper.toQuestionnaireName("Vit D25+D1,25 OH+D1,25 Pnl SerPl-mCnc"));
    assertEquals("S_pneum_serotypes_IgG_Pnl_B_Ser_mCnc",
        LoincQuestionnaireHelper.toQuestionnaireName("S pneum 14 serotypes IgG Pnl B Ser-mCnc"));
    assertEquals("Nd_trimester_screen_Pnl_SerPl",
        LoincQuestionnaireHelper.toQuestionnaireName("2nd trimester 3 screen Pnl SerPl"));
    assertEquals("CarBAMazepine_free_tot_Pnl_SerPl_mCnc",
        LoincQuestionnaireHelper.toQuestionnaireName("carBAMazepine free+tot Pnl SerPl-mCnc"));
    assertEquals("TTG_Ab_Pnl_Ser", LoincQuestionnaireHelper.toQuestionnaireName("tTG Ab Pnl Ser"));
  }

  /**
   * Deprecated panel without SHORTNAME uses COMPONENT coding display for
   * title/name.
   */
  @Test
  public void testResolveLoincDisplayNameFromComponentWhenShortNameAbsent() {
    final Concept concept = new Concept();
    concept.setCode("45981-8");
    concept.setName("Deprecated MDS full assessment form - version 2.0");
    final ConceptPropertyValueCoding component = new ConceptPropertyValueCoding();
    component.setPropertyCode(LoincConstants.ATTR_COMPONENT);
    component.setValueCode("LP75085-8");
    component.setValueDisplay("MDS full assessment form - version 2.0");
    concept.getFhirPropertyCodings().add(component);

    assertEquals("MDS full assessment form - version 2.0",
        LoincQuestionnaireHelper.resolveLoincDisplayName(concept, null));
    assertEquals("MDS_full_assessment_form_version", LoincQuestionnaireHelper
        .toQuestionnaireName(LoincQuestionnaireHelper.resolveLoincDisplayName(concept, null)));
  }

  /**
   * Nested panel sections (PanelType=Panel) are questionnaire groups, not leaf
   * questions.
   */
  @Test
  public void testIsQuestionnaireGroupConceptPanelTypePanel() {
    final Concept concept = new Concept();
    concept.setCode("52452-0");
    concept.getAttributes().put("PanelType", "Panel");

    assertTrue(LoincQuestionnaireHelper.isQuestionnaireGroupConcept(concept));
    assertEquals("Panel", LoincQuestionnaireHelper.resolvePanelType(concept));
  }

  /**
   * Organizer and Convenience group panel types are questionnaire groups.
   */
  @Test
  public void testIsQuestionnaireGroupConceptOrganizerAndConvenienceGroup() {
    final Concept organizer = new Concept();
    organizer.getAttributes().put("PanelType", "Organizer");
    assertTrue(LoincQuestionnaireHelper.isQuestionnaireGroupConcept(organizer));

    final Concept convenience = new Concept();
    convenience.getAttributes().put("PanelType", "Convenience group");
    assertTrue(LoincQuestionnaireHelper.isQuestionnaireGroupConcept(convenience));
  }

  /**
   * Leaf observations without a panel type are not questionnaire groups.
   */
  @Test
  public void testIsQuestionnaireGroupConceptFalseForLeafObservation() {
    final Concept concept = new Concept();
    concept.setCode("70160-7");
    assertFalse(LoincQuestionnaireHelper.isQuestionnaireGroupConcept(concept));
  }

  /**
   * PanelType can be read from FHIR property codings when attributes are
   * absent.
   */
  @Test
  public void testResolvePanelTypeFromFhirPropertyCoding() {
    final Concept concept = new Concept();
    final ConceptPropertyValueCoding panelType = new ConceptPropertyValueCoding();
    panelType.setPropertyCode("PanelType");
    panelType.setValueDisplay("Panel");
    concept.getFhirPropertyCodings().add(panelType);

    assertEquals("Panel", LoincQuestionnaireHelper.resolvePanelType(concept));
    assertTrue(LoincQuestionnaireHelper.isQuestionnaireGroupConcept(concept));
  }

  /**
   * FSN component is preferred over COMPONENT display when casing differs
   * (52747-3).
   */
  @Test
  public void testResolveLoincDisplayNamePrefersFsnOverComponentDisplay() {
    final Concept concept = new Concept();
    concept.setCode("52747-3");
    concept.setName("Continuity Assessment Record and Evaluation (CARE) tool - Expired");
    final Term fsn = new Term();
    fsn.setActive(true);
    fsn.setType(LoincConstants.TERM_TYPE_FULLY_SPECIFIED_NAME);
    fsn.setName("Continuity assessment record and evaluation tool - Expired:-:Pt:^Patient:-:CARE");
    fsn.getLocaleMap().put("en-US", true);
    concept.getTerms().add(fsn);
    final ConceptPropertyValueCoding component = new ConceptPropertyValueCoding();
    component.setPropertyCode(LoincConstants.ATTR_COMPONENT);
    component.setValueCode("LP74591-6");
    component.setValueDisplay("Continuity assessment record and evaluation tool - expired");
    concept.getFhirPropertyCodings().add(component);

    assertEquals("Continuity assessment record and evaluation tool - Expired",
        LoincQuestionnaireHelper.resolveLoincDisplayName(concept, null));
    assertEquals("Continuity_assessment_record_and_evaluation_tool_Expired",
        LoincQuestionnaireHelper
            .toQuestionnaireName(LoincQuestionnaireHelper.resolveLoincDisplayName(concept, null)));
  }

  /**
   * Under 30914, the acute CARE patient panel (ParentId 32168) is excluded.
   */
  @Test
  public void testDedupePanelMemberRelationshipsExcludesOtherFormPanels() {
    final ConceptRelationship acutePanel = memberRel("56590", "69326-7");
    acutePanel.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "32168");

    final ConceptRelationship patientPanel = memberRel("30920", "52460-3");
    patientPanel.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "30914");

    final List<ConceptRelationship> deduped = LoincQuestionnaireHelper
        .dedupePanelMemberRelationshipsForFormContext(List.of(acutePanel, patientPanel), "30914");

    assertEquals(1, deduped.size());
    assertEquals("52460-3", deduped.get(0).getTo().getCode());
    assertEquals("30920",
        LoincQuestionnaireHelper.resolveMemberLinkId(deduped.get(0), deduped.get(0).getTo()));
  }

  /**
   * Duplicate panel rows for the same LOINC code keep the edge for the parent
   * form linkId.
   */
  @Test
  public void testDedupePanelMemberRelationshipsForPatientMedicareUnder30920() {
    final ConceptRelationship careAcute = memberRel("32179", "45397-7");
    careAcute.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "56590");

    final ConceptRelationship patientInfo = memberRel("30925", "45397-7");
    patientInfo.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "30920");
    patientInfo.getAttributes().put(LoincConstants.ATTR_DISPLAY_NAME_FOR_FORM,
        "Patient's Medicare Health Insurance Number");

    final ConceptRelationship otherForm = memberRel("31599", "45397-7");
    otherForm.getAttributes().put(LoincConstants.ATTR_PARENT_FORM_LINK_ID, "31594");
    otherForm.getAttributes().put(LoincConstants.ATTR_DISPLAY_NAME_FOR_FORM,
        "Patient's Medicare Health Insurance Number");

    final List<ConceptRelationship> deduped =
        LoincQuestionnaireHelper.dedupePanelMemberRelationshipsForFormContext(
            List.of(careAcute, patientInfo, otherForm), "30920");

    assertEquals(1, deduped.size());
    assertEquals("30925",
        LoincQuestionnaireHelper.resolveMemberLinkId(deduped.get(0), deduped.get(0).getTo()));
    assertEquals("Patient's Medicare Health Insurance Number",
        LoincQuestionnaireHelper.resolveQuestionnaireItemDisplayName(deduped.get(0), new Concept(),
            deduped.get(0).getTo()));
  }

  /**
   * DisplayNameForForm on the member edge overrides concept FSN for item text.
   */
  @Test
  public void testResolveQuestionnaireItemDisplayNamePrefersFormDisplay() {
    final ConceptRelationship rel = new ConceptRelationship();
    rel.getAttributes().put(LoincConstants.ATTR_DISPLAY_NAME_FOR_FORM,
        "Date of Onset of Impairment");
    final Concept concept = new Concept();
    concept.setCode("85585-8");
    concept.setName("Date of condition onset:Date:Pt:^Patient:Qn");

    assertEquals("Date of Onset of Impairment",
        LoincQuestionnaireHelper.resolveQuestionnaireItemDisplayName(rel, concept, null));
  }

  /**
   * Leaf-embedded survey panel (empty SHORTNAME) uses full FullySpecifiedName;
   * DisplayNameForForm does not override.
   */
  @Test
  public void testResolveQuestionnaireItemDisplayNameLeafEmbedUsesFullFsn() {
    final String fsnValue =
        "Confusion Assessment Method (CAM):-:Pt:^Patient:-:Observed.CAM.CARE";
    final Concept concept = camConcept(fsnValue, "Confusion Assessment Method (CAM)");

    final ConceptRelationship leafRel = new ConceptRelationship();
    leafRel.getAttributes().put(LoincConstants.ATTR_REL_ID, "32622");
    leafRel.getAttributes().put(LoincConstants.ATTR_DISPLAY_NAME_FOR_FORM,
        "Should not win over FSN when leaf-embedded");

    assertEquals(fsnValue, LoincQuestionnaireHelper.resolveQuestionnaireItemDisplayName(leafRel,
        concept, null, "32622"));
    assertEquals(fsnValue,
        LoincQuestionnaireHelper.resolveQuestionnaireItemDisplayName(leafRel, concept, null));
  }

  /**
   * Leaf-embedded lab panel with SHORTNAME uses SHORTNAME (not full FSN).
   */
  @Test
  public void testResolveQuestionnaireItemDisplayNameLeafEmbedPrefersShortName() {
    final Concept concept = new Concept();
    concept.setCode("24358-4");
    concept.getAttributes().put(LoincConstants.ATTR_SHORTNAME, "CBC WO Platelets Bld");
    final Term fsn = new Term();
    fsn.setActive(true);
    fsn.setType(LoincConstants.TERM_TYPE_FULLY_SPECIFIED_NAME);
    fsn.setName("Hemogram WO platelets panel:-:Pt:Bld:Qn");
    fsn.getLocaleMap().put("en-US", true);
    concept.getTerms().add(fsn);

    final ConceptRelationship leafRel = new ConceptRelationship();
    leafRel.getAttributes().put(LoincConstants.ATTR_REL_ID, "10793");
    leafRel.getAttributes().put(LoincConstants.ATTR_DISPLAY_NAME_FOR_FORM, "Should not win");

    assertEquals("CBC WO Platelets Bld", LoincQuestionnaireHelper
        .resolveQuestionnaireItemDisplayName(leafRel, concept, null, "10793"));
  }

  /**
   * Same panel under a non-leaf-embed linkId keeps DisplayNameForForm / LCN stack.
   */
  @Test
  public void testResolveQuestionnaireItemDisplayNameNonLeafKeepsFormDisplay() {
    final String fsnValue =
        "Confusion Assessment Method (CAM):-:Pt:^Patient:-:Observed.CAM.CARE";
    final Concept concept = camConcept(fsnValue, "Confusion Assessment Method (CAM)");

    final ConceptRelationship otherRel = new ConceptRelationship();
    otherRel.getAttributes().put(LoincConstants.ATTR_REL_ID, "99999");
    otherRel.getAttributes().put(LoincConstants.ATTR_DISPLAY_NAME_FOR_FORM, "CAM form label");

    assertEquals("CAM form label", LoincQuestionnaireHelper
        .resolveQuestionnaireItemDisplayName(otherRel, concept, null, "99999"));
  }

  /**
   * Non-leaf-embed without DNF uses LCN, not full FSN.
   */
  @Test
  public void testResolveQuestionnaireItemDisplayNameNonLeafUsesLcnNotFullFsn() {
    final String fsnValue =
        "Confusion Assessment Method (CAM):-:Pt:^Patient:-:Observed.CAM.CARE";
    final String lcn = "Confusion Assessment Method (CAM)";
    final Concept concept = camConcept(fsnValue, lcn);

    final ConceptRelationship otherRel = new ConceptRelationship();
    otherRel.getAttributes().put(LoincConstants.ATTR_REL_ID, "99999");

    assertEquals(lcn, LoincQuestionnaireHelper.resolveQuestionnaireItemDisplayName(otherRel,
        concept, null, "99999"));
  }

  private static Concept camConcept(final String fsnValue, final String lcn) {
    final Concept concept = new Concept();
    concept.setCode("52495-9");
    concept.getAttributes().put(LoincConstants.ATTR_LONG_COMMON_NAME, lcn);
    final Term fsn = new Term();
    fsn.setActive(true);
    fsn.setType(LoincConstants.TERM_TYPE_FULLY_SPECIFIED_NAME);
    fsn.setName(fsnValue);
    fsn.getLocaleMap().put("en-US", true);
    concept.getTerms().add(fsn);
    final Term lcnTerm = new Term();
    lcnTerm.setActive(true);
    lcnTerm.setType(LoincConstants.TERM_TYPE_LONG_COMMON_NAME);
    lcnTerm.setName(lcn);
    lcnTerm.getLocaleMap().put("en-US", true);
    concept.getTerms().add(lcnTerm);
    return concept;
  }

  /**
   * SURVEY_QUEST_TEXT is preferred over SHORTNAME for PhenX questionnaire
   * items.
   */
  @Test
  public void testResolveLoincDisplayNamePrefersSurveyQuestTextOverShortName() {
    final Concept concept = new Concept();
    concept.setCode("61437-0");
    concept.getAttributes().put(LoincConstants.ATTR_SHORTNAME, "Bread in 30D PhenX");
    concept.getAttributes().put(LoincConstants.ATTR_SURVEY_QUEST_TEXT,
        "How often do you eat bread, toast or dinner rolls, including bread "
            + "as part of a sandwich (DO NOT count buns with hamburgers or hot dogs)?");

    assertEquals(
        "How often do you eat bread, toast or dinner rolls, including bread "
            + "as part of a sandwich (DO NOT count buns with hamburgers or hot dogs)?",
        LoincQuestionnaireHelper.resolveLoincDisplayName(concept, null));
  }

  /**
   * SURVEY_QUEST_TEXT is used when DisplayNameForForm is absent on the member
   * edge.
   */
  @Test
  public void testResolveLoincDisplayNameUsesSurveyQuestTextWhenFormDisplayAbsent() {
    final Concept concept = new Concept();
    concept.setCode("85585-8");
    concept.setName("Date of condition onset:Date:Pt:^Patient:Qn");
    concept.getAttributes().put(LoincConstants.ATTR_SURVEY_QUEST_TEXT,
        "Date of Onset of Impairment");

    assertEquals("Date of Onset of Impairment",
        LoincQuestionnaireHelper.resolveLoincDisplayName(concept, null));
  }

  /**
   * Legacy FSN parsing yields the component axis for organizer panels.
   */
  @Test
  public void testResolveLoincDisplayNameFromLegacyFsnComponent() {
    final Concept concept = new Concept();
    concept.setCode("18719-5");
    final Term fsn = new Term();
    fsn.setActive(true);
    fsn.setType(LoincConstants.TERM_TYPE_FULLY_SPECIFIED_NAME);
    fsn.setName("Chemistry studies:Cmplx:-:^Patient:Set");
    fsn.getLocaleMap().put("en-US", true);
    concept.getTerms().add(fsn);
    final ConceptPropertyValueCoding property = new ConceptPropertyValueCoding();
    property.setPropertyCode("PROPERTY");
    property.setValueDisplay("Cmplx");
    concept.getFhirPropertyCodings().add(property);

    assertEquals("Chemistry studies",
        LoincQuestionnaireHelper.resolveLoincDisplayName(concept, null));
  }

  /**
   * FSN component parsing respects colons inside the component when PROPERTY is
   * known.
   */
  @Test
  public void testResolveLoincDisplayNameFromFsnWithTimeInComponent() {
    final Concept concept = new Concept();
    concept.setCode("54723-2");
    final Term fsn = new Term();
    fsn.setActive(true);
    fsn.setType(LoincConstants.TERM_TYPE_FULLY_SPECIFIED_NAME);
    fsn.setName("Resident prefers staying up past 8:00 p.m.:Find:Pt:^Patient:Ord:MDSv3");
    fsn.getLocaleMap().put("en-US", true);
    concept.getTerms().add(fsn);
    final ConceptPropertyValueCoding property = new ConceptPropertyValueCoding();
    property.setPropertyCode("PROPERTY");
    property.setValueDisplay("Find");
    concept.getFhirPropertyCodings().add(property);

    assertEquals("Resident prefers staying up past 8:00 p.m.",
        LoincQuestionnaireHelper.resolveLoincDisplayName(concept, null));
  }

  /**
   * Colon FSN yields the component axis when survey text is absent.
   */
  @Test
  public void testResolveLoincDisplayNameFromColonFsnComponent() {
    final Concept concept = new Concept();
    concept.setCode("18609-8");
    final Term fsn = new Term();
    fsn.setActive(true);
    fsn.setType(LoincConstants.TERM_TYPE_FULLY_SPECIFIED_NAME);
    fsn.setName("Route:Prid:Pt:Medication.current:Nom");
    fsn.getLocaleMap().put("en-US", true);
    concept.getTerms().add(fsn);

    assertEquals("Route", LoincQuestionnaireHelper.resolveLoincDisplayName(concept, null));
  }

  /**
   * Empty ApplicableContext binding wins over foreign-context and link-type rows.
   */
  @Test
  public void testSelectAnswerListFromLinksPrefersEmptyApplicableContext() {
    final ConceptRelationship foreign = answerListLink("LL653-7", "NORMATIVE", "54682-0");
    final ConceptRelationship empty = answerListLink("LL4960-2", "EXAMPLE", null);
    final ConceptRelationship contextMatch = answerListLink("LL999-9", "NORMATIVE", "54644-0");

    assertEquals("LL4960-2", LoincQuestionnaireHelper
        .selectAnswerListFromLinks(List.of(foreign, empty, contextMatch)));
  }

  /**
   * Without an empty ApplicableContext row, no LL is selected from links.
   */
  @Test
  public void testSelectAnswerListFromLinksReturnsNullWithoutEmptyContext() {
    final ConceptRelationship foreign = answerListLink("LL653-7", "NORMATIVE", "54682-0");
    final ConceptRelationship other = answerListLink("LL999-9", "PREFERRED", "54644-0");

    assertNull(LoincQuestionnaireHelper.selectAnswerListFromLinks(List.of(foreign, other)));
    assertNull(LoincQuestionnaireHelper.selectAnswerListFromLinks(List.of()));
    assertNull(LoincQuestionnaireHelper.selectAnswerListFromLinks(null));
  }

  /**
   * Override on the member edge wins over concept answer-list properties.
   */
  @Test
  public void testResolveAnswerListCodePrefersOverride() {
    final ConceptRelationship member = memberRel("1", "54644-0");
    member.getAttributes().put(LoincConstants.ATTR_ANSWER_LIST_ID_OVERRIDE, "LL111-1");

    final Concept concept = new Concept();
    concept.setCode("54644-0");
    concept.getAttributes().put(LoincConstants.ATTR_ANSWER_LIST_ID, "LL222-2");

    assertEquals("LL111-1", LoincQuestionnaireHelper.resolveAnswerListCode(member, concept,
        "54682-0", null, null));
  }

  /**
   * Builds an answer-list relationship with link type and optional context.
   *
   * @param llCode answer list code
   * @param linkType AnswerListLinkType
   * @param applicableContext ApplicableContext or null
   * @return relationship
   */
  private static ConceptRelationship answerListLink(final String llCode, final String linkType,
    final String applicableContext) {
    final ConceptRelationship rel = new ConceptRelationship();
    rel.setAdditionalType(LoincConstants.LOINC_REL_ANSWER_LIST_LINK);
    rel.setTo(new ConceptRef(llCode, null));
    if (linkType != null) {
      rel.getAttributes().put(LoincConstants.ATTR_ANSWER_LIST_LINK_TYPE, linkType);
    }
    if (applicableContext != null) {
      rel.getAttributes().put(LoincConstants.ATTR_APPLICABLE_CONTEXT, applicableContext);
    }
    return rel;
  }

}
