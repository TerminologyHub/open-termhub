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
    rel.getAttributes().put(LoincConstants.ATTR_SEQ_NO, "1");

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
   * Test build questionnaire copyright base only without search service.
   *
   * @throws Exception the exception
   */
  @Test
  public void testBuildQuestionnaireCopyrightBaseOnlyWithoutSearchService() throws Exception {
    final String base = "LOINC base copyright";
    final String result = LoincQuestionnaireHelper.buildQuestionnaireCopyright(base,
        java.util.List.of("44249-1"), null, "LOINC", "Regenstrief Institute, Inc.", "2.81");

    assertEquals(base, result);
  }

  /**
   * Test {@link LoincQuestionnaireHelper#toQuestionnaireName(String)} matches fhir.loinc.org.
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
    assertEquals("Fluid_IO_Pnl_h", LoincQuestionnaireHelper.toQuestionnaireName("Fluid IO Pnl 24h"));
    assertEquals("Cardiac_D_echo_panel",
        LoincQuestionnaireHelper.toQuestionnaireName("Cardiac 2D echo panel"));
    assertEquals("Creat_H_Cl_Pnl_Ur_SerPl",
        LoincQuestionnaireHelper.toQuestionnaireName("Creat 24H Cl Pnl Ur SerPl"));
    assertEquals("Vit_D25_D1_OH_D1_Pnl_SerPl_mCnc", LoincQuestionnaireHelper
        .toQuestionnaireName("Vit D25+D1,25 OH+D1,25 Pnl SerPl-mCnc"));
    assertEquals("S_pneum_serotypes_IgG_Pnl_B_Ser_mCnc",
        LoincQuestionnaireHelper.toQuestionnaireName("S pneum 14 serotypes IgG Pnl B Ser-mCnc"));
    assertEquals("Nd_trimester_screen_Pnl_SerPl",
        LoincQuestionnaireHelper.toQuestionnaireName("2nd trimester 3 screen Pnl SerPl"));
    assertEquals("CarBAMazepine_free_tot_Pnl_SerPl_mCnc",
        LoincQuestionnaireHelper.toQuestionnaireName("carBAMazepine free+tot Pnl SerPl-mCnc"));
    assertEquals("TTG_Ab_Pnl_Ser", LoincQuestionnaireHelper.toQuestionnaireName("tTG Ab Pnl Ser"));
  }

  /**
   * Deprecated panel without SHORTNAME uses COMPONENT coding display for title/name.
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
    assertEquals("MDS_full_assessment_form_version",
        LoincQuestionnaireHelper.toQuestionnaireName(
            LoincQuestionnaireHelper.resolveLoincDisplayName(concept, null)));
  }

  /**
   * FSN component is preferred over COMPONENT display when casing differs (52747-3).
   */
  @Test
  public void testResolveLoincDisplayNamePrefersFsnOverComponentDisplay() {
    final Concept concept = new Concept();
    concept.setCode("52747-3");
    concept.setName("Continuity Assessment Record and Evaluation (CARE) tool - Expired");
    final Term fsn = new Term();
    fsn.setActive(true);
    fsn.setType(LoincConstants.TERM_TYPE_FULLY_SPECIFIED_NAME);
    fsn.setName(
        "Continuity assessment record and evaluation tool - Expired:-:Pt:^Patient:-:CARE");
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
        LoincQuestionnaireHelper.toQuestionnaireName(
            LoincQuestionnaireHelper.resolveLoincDisplayName(concept, null)));
  }
}
