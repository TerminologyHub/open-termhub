/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r4.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.fhir.util.LoincConstants;
import com.wci.termhub.fhir.util.LoincValueSetHelper.LllgComposeStructure;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptPropertyValueCoding;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.util.DateUtility;
import com.wci.termhub.util.PropertyUtility;

/**
 * Unit tests for FHIR R4 Meta (versionId, lastUpdated) on CodeSystem, ValueSet, ConceptMap,
 * Questionnaire.
 */
public class FhirUtilityR4MetaUnitTest {

  /**
   * Test CodeSystem meta has versionId and lastUpdated.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCodeSystemMeta() throws Exception {
    final Terminology terminology = new Terminology();
    terminology.setId("test-cs");
    terminology.setReleaseDate("2022-04-11");
    terminology.setUri("http://example.org/cs");
    terminology.setVersion("1");
    terminology.setName("Test CodeSystem");
    terminology.setAbbreviation("TCS");
    terminology.setPublisher("Test");
    final Map<String, String> attrs = new HashMap<>();
    terminology.setAttributes(attrs);
    terminology.setConceptCt(10L);
    terminology.setCreated(Date.from(LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant()));

    final CodeSystem cs = FhirUtilityR4.toR4(terminology);
    assertNotNull(cs.getMeta());
    assertEquals("1", cs.getMeta().getVersionId());
    assertNotNull(cs.getMeta().getLastUpdated());
    assertNotNull(cs.getDate());
    assertEquals(cs.getDate(), cs.getMeta().getLastUpdated());
  }

  /**
   * When release date is absent, meta.lastUpdated falls back to terminology created (UTC parse).
   *
   * @throws Exception the exception
   */
  @Test
  public void testCodeSystemMetaLastUpdatedFallsBackWhenNoReleaseDate() throws Exception {
    final Terminology terminology = new Terminology();
    terminology.setId("test-cs-nodate");
    terminology.setReleaseDate(null);
    terminology.setUri("http://example.org/cs");
    terminology.setVersion("1");
    terminology.setName("Test CodeSystem");
    terminology.setAbbreviation("TCS");
    terminology.setPublisher("Test");
    final Map<String, String> attrs = new HashMap<>();
    terminology.setAttributes(attrs);
    terminology.setConceptCt(10L);
    final Date created =
        Date.from(LocalDate.of(2023, 6, 15).atStartOfDay(ZoneOffset.UTC).toInstant());
    terminology.setCreated(created);

    final CodeSystem cs = FhirUtilityR4.toR4(terminology);
    assertNotNull(cs.getMeta().getLastUpdated());
    assertEquals(DateUtility.parseToUtcDate(created), cs.getMeta().getLastUpdated());
  }

  /**
   * Test CodeSystem description, copyright, identifier, valueSet, contact, caseSensitive,
   * versionNeeded round-trip from terminology attributes (LOINC-style).
   *
   * @throws Exception the exception
   */
  @Test
  public void testCodeSystemDescriptionCopyrightIdentifierValueSetContactCaseSensitiveVersionNeeded()
      throws Exception {
    final String description =
        "LOINC is a freely available international standard for tests, measurements, and observations";
    final String copyright =
        "This material contains content from LOINC (http://loinc.org). LOINC is copyright Regenstrief Institute, Inc.";
    final String fhirIdentifier =
        "[{\"system\":\"urn:ietf:rfc:3986\",\"value\":\"urn:oid:2.16.840.1.113883.6.1\"}]";
    final String valueSet = "http://loinc.org/?fhir_vs";
    final String fhirContact =
        "[{\"telecom\":[{\"system\":\"url\",\"value\":\"http://loinc.org\"}]}]";

    final Terminology terminology = new Terminology();
    terminology.setId("test-loinc");
    terminology.setReleaseDate("2022-04-11");
    terminology.setUri("http://loinc.org");
    terminology.setVersion("2.78");
    terminology.setName("Logical Observation Identifiers Names and Codes");
    terminology.setAbbreviation("LOINC");
    terminology.setPublisher("Regenstrief Institute, Inc.");
    final Map<String, String> attrs = new HashMap<>();
    attrs.put("description", description);
    attrs.put("copyright", copyright);
    attrs.put("fhirIdentifier", fhirIdentifier);
    attrs.put("valueSet", valueSet);
    attrs.put("fhirContact", fhirContact);
    attrs.put("caseSensitive", "false");
    attrs.put("versionNeeded", "false");
    terminology.setAttributes(attrs);
    terminology.setConceptCt(100L);
    terminology.setCreated(Date.from(LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant()));

    final CodeSystem cs = FhirUtilityR4.toR4(terminology);

    assertEquals(description, cs.getDescription());
    assertEquals(copyright, cs.getCopyright());
    assertNotNull(cs.getIdentifier());
    assertTrue(cs.getIdentifier().size() >= 1);
    assertEquals("urn:ietf:rfc:3986", cs.getIdentifier().get(0).getSystem());
    assertEquals("urn:oid:2.16.840.1.113883.6.1", cs.getIdentifier().get(0).getValue());
    assertEquals(valueSet, cs.getValueSet());
    assertNotNull(cs.getContact());
    assertTrue(cs.getContact().size() >= 1);
    assertEquals("Regenstrief Institute, Inc.", cs.getContact().get(0).getName());
    assertNotNull(cs.getContact().get(0).getTelecom());
    assertTrue(cs.getContact().get(0).getTelecom().size() >= 1);
    assertEquals("http://loinc.org", cs.getContact().get(0).getTelecom().get(0).getValue());
    assertFalse(cs.getCaseSensitive());
    assertFalse(cs.getVersionNeeded());
  }

  /**
   * Test LOINC LL/LG ValueSet contact from terminology publisher and uri.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLllgValueSetContact() throws Exception {
    final Terminology terminology = new Terminology();
    terminology.setUri("http://loinc.org");
    terminology.setVersion("2.78");
    terminology.setPublisher("Regenstrief Institute, Inc.");
    terminology.setReleaseDate("2022-04-11");
    terminology.setCreated(Date.from(LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant()));

    final ValueSet vs =
        FhirUtilityR4.toR4LllgValueSet(terminology, "LG10030-1", "test-uuid", false);

    assertNotNull(vs.getContact());
    assertEquals("Regenstrief Institute, Inc.", vs.getContactFirstRep().getName());
    assertEquals("http://loinc.org", vs.getContactFirstRep().getTelecomFirstRep().getValue());
  }

  /**
   * Test LL ValueSet identifier from concept AnswerListOID.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLllgValueSetAnswerListOid() throws Exception {
    final Terminology terminology = new Terminology();
    terminology.setUri("http://loinc.org");
    terminology.setVersion("2.83");
    terminology.setPublisher("Regenstrief Institute, Inc.");
    terminology.setReleaseDate("2026-08-18");
    terminology.setCreated(Date.from(LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant()));

    final Concept concept = new Concept();
    concept.setId("test-uuid");
    concept.setCode("LL1162-8");
    concept.setName("Quantity (5 answers, ord)");
    concept.getAttributes().put(LoincConstants.ATTR_ANSWER_LIST_OID, "1.3.6.1.4.1.12009.10.1.333");

    final ValueSet vs = FhirUtilityR4.toR4LllgValueSetFromConcept(terminology, concept, false);

    assertEquals("Quantity (5 answers, ord)", vs.getName());
    assertEquals(1, vs.getIdentifier().size());
    assertEquals("urn:ietf:rfc:3986", vs.getIdentifierFirstRep().getSystem());
    assertEquals("urn:oid:1.3.6.1.4.1.12009.10.1.333", vs.getIdentifierFirstRep().getValue());

    final ValueSet getVs = FhirUtilityR4.toR4LllgValueSetWithComposeOnly(terminology,
        concept.getCode(), concept.getId(), new LllgComposeStructure(List.of(), List.of()),
        concept);
    assertEquals("Quantity (5 answers, ord)", getVs.getName());
    assertEquals("urn:oid:1.3.6.1.4.1.12009.10.1.333", getVs.getIdentifierFirstRep().getValue());
  }

  /**
   * Test ValueSet (entire) meta has versionId and lastUpdated.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValueSetEntireMeta() throws Exception {
    final Terminology terminology = new Terminology();
    terminology.setId("test-cs");
    terminology.setReleaseDate("2022-04-11");
    terminology.setUri("http://example.org/cs");
    terminology.setVersion("1");
    terminology.setName("Test CodeSystem");
    terminology.setAbbreviation("TCS");
    terminology.setPublisher("Test");
    final Map<String, String> attrs = new HashMap<>();
    attrs.put("originalId", "orig-1");
    terminology.setAttributes(attrs);
    terminology.setConceptCt(10L);
    terminology.setCreated(Date.from(LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant()));

    final ValueSet vs = FhirUtilityR4.toR4ValueSet(terminology, true);
    assertNotNull(vs.getMeta());
    assertEquals("1", vs.getMeta().getVersionId());
    assertNotNull(vs.getMeta().getLastUpdated());
  }

  /**
   * Test ConceptMap meta has versionId and lastUpdated.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConceptMapMeta() throws Exception {
    final Mapset mapset = new Mapset();
    mapset.setId("test-cm");
    mapset.setReleaseDate("2022-04-11");
    mapset.setUri("http://example.org/cm");
    mapset.setVersion("1");
    mapset.setName("Test ConceptMap");
    mapset.setAbbreviation("TCM");
    mapset.setPublisher("Test");
    final Map<String, String> attrs = new HashMap<>();
    mapset.setAttributes(attrs);
    mapset.setCreated(Date.from(LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant()));

    final ConceptMap cm = FhirUtilityR4.toR4(mapset);
    assertNotNull(cm.getMeta());
    assertEquals("1", cm.getMeta().getVersionId());
    assertNotNull(cm.getMeta().getLastUpdated());
    assertTrue(cm.getContact().isEmpty());
  }

  /**
   * Test ConceptMap contact round-trip from mapset {@code fhirContact} attribute.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConceptMapContact() throws Exception {
    final String fhirContact =
        "[{\"telecom\":[{\"system\":\"url\",\"value\":\"http://loinc.org/cm/chebi-to-loinc-parts\"}]}]";

    final Mapset mapset = new Mapset();
    mapset.setId("test-cm");
    mapset.setUri("http://loinc.org/cm/chebi-to-loinc-parts");
    mapset.setVersion("1");
    mapset.setName("Test ConceptMap");
    mapset.setAbbreviation("TCM");
    mapset.setPublisher("Regenstrief Institute, Inc.");
    final Map<String, String> attrs = new HashMap<>();
    attrs.put("fhirContact", fhirContact);
    mapset.setAttributes(attrs);
    mapset.setCreated(Date.from(LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant()));

    final ConceptMap cm = FhirUtilityR4.toR4(mapset);

    assertNotNull(cm.getContact());
    assertEquals(1, cm.getContact().size());
    assertEquals("Regenstrief Institute, Inc.", cm.getContact().get(0).getName());
    assertEquals("http://loinc.org/cm/chebi-to-loinc-parts",
        cm.getContact().get(0).getTelecomFirstRep().getValue());
  }

  /**
   * Test ConceptMap contact in regenstrief mode uses terminology contact, not mapset URL.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConceptMapContactRegenstriefUsesTerminologyContact() throws Exception {
    final String priorMode = PropertyUtility.getProperty("server.mode");
    PropertyUtility.setProperty("server.mode", "regenstrief");
    try {
      final String fhirContact =
          "[{\"telecom\":[{\"system\":\"url\",\"value\":\"http://loinc.org/cm/chebi-to-loinc-parts\"}]}]";

      final Terminology terminology = new Terminology();
      terminology.setUri("https://loinc.org");
      terminology.setPublisher("Regenstrief Institute, Inc.");
      final Map<String, String> termAttrs = new HashMap<>();
      termAttrs.put("fhirContact",
          "[{\"telecom\":[{\"system\":\"url\",\"value\":\"https://loinc.org\"}]}]");
      terminology.setAttributes(termAttrs);

      final Mapset mapset = new Mapset();
      mapset.setId("test-cm");
      mapset.setUri("http://loinc.org/cm/chebi-to-loinc-parts");
      mapset.setVersion("1");
      mapset.setName("Test ConceptMap");
      mapset.setAbbreviation("TCM");
      mapset.setPublisher("Regenstrief Institute, Inc.");
      final Map<String, String> attrs = new HashMap<>();
      attrs.put("fhirContact", fhirContact);
      mapset.setAttributes(attrs);
      mapset.setCreated(
          Date.from(LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant()));

      final ConceptMap cm = FhirUtilityR4.toR4(mapset, terminology);

      assertNotNull(cm.getContact());
      assertEquals(1, cm.getContact().size());
      assertEquals("Regenstrief Institute, Inc.", cm.getContact().get(0).getName());
      assertEquals("https://loinc.org", cm.getContact().get(0).getTelecomFirstRep().getValue());
    } finally {
      if (priorMode != null) {
        PropertyUtility.setProperty("server.mode", priorMode);
      }
    }
  }

  /**
   * Test ConceptMap copyright round-trip from mapset {@code copyright} attribute.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConceptMapCopyright() throws Exception {
    final String copyright =
        "This material contains content from LOINC and ChEBI. See respective publishers for terms.";

    final Mapset mapset = new Mapset();
    mapset.setId("test-cm");
    mapset.setUri("http://loinc.org/cm/chebi-to-loinc-parts");
    mapset.setVersion("1");
    mapset.setName("Test ConceptMap");
    mapset.setAbbreviation("TCM");
    mapset.setPublisher("Regenstrief Institute, Inc.");
    final Map<String, String> attrs = new HashMap<>();
    attrs.put("copyright", copyright);
    mapset.setAttributes(attrs);
    mapset.setCreated(Date.from(LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant()));

    final ConceptMap cm = FhirUtilityR4.toR4(mapset);

    assertEquals(copyright, cm.getCopyright());
  }

  /**
   * Test Questionnaire meta has versionId and lastUpdated.
   *
   * @throws Exception the exception
   */
  @Test
  public void testQuestionnaireMeta() throws Exception {
    final String copyright =
        "This material contains content from LOINC (http://loinc.org). LOINC is copyright Regenstrief Institute, Inc.";
    final Terminology terminology = new Terminology();
    terminology.setId("test-cs");
    terminology.setReleaseDate("2022-04-11");
    terminology.setUri("http://example.org/cs");
    terminology.setVersion("1");
    terminology.setName("Test CodeSystem");
    terminology.setAbbreviation("TCS");
    terminology.setPublisher("Test");
    final Map<String, String> attrs = new HashMap<>();
    attrs.put("copyright", copyright);
    terminology.setAttributes(attrs);
    terminology.setCreated(Date.from(LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant()));

    final Questionnaire q = FhirUtilityR4.toR4Questionnaire(terminology, true);
    assertNotNull(q.getMeta());
    assertEquals("1", q.getMeta().getVersionId());
    assertNotNull(q.getMeta().getLastUpdated());
    assertNotNull(q.getDate());
    assertEquals(q.getDate(), q.getMeta().getLastUpdated());
    assertEquals(copyright, q.getCopyright());
  }

  /**
   * Test Questionnaire name/title from LOINC SHORTNAME (short common name).
   *
   * @throws Exception the exception
   */
  @Test
  public void testQuestionnaireNameTitleFromShortName() throws Exception {
    final Concept concept = new Concept();
    concept.setCode("100105-6");
    concept.setName("Filaria IgG and IgM panel - Serum");
    concept.setTerminology("LOINC");
    concept.setPublisher("Regenstrief Institute, Inc.");
    concept.setVersion("2.81");
    final Map<String, String> attrs = new HashMap<>();
    attrs.put("SHORTNAME", "Filaria Ab.IgG + IgM Pnl Ser");
    concept.setAttributes(attrs);

    final Questionnaire q = FhirUtilityR4.toR4Questionnaire(concept, null, null);
    assertEquals("Filaria_Ab_IgG_IgM_Pnl_Ser", q.getName());
    assertEquals("Filaria Ab.IgG + IgM Pnl Ser", q.getTitle());
    assertEquals("Filaria Ab.IgG + IgM Pnl Ser", q.getCodeFirstRep().getDisplay());
  }

  /**
   * Questionnaire resource id is the Concept UUID; LOINC code stays on code and url.
   *
   * @throws Exception the exception
   */
  @Test
  public void testQuestionnaireIdIsConceptUuid() throws Exception {
    final Concept concept = new Concept();
    concept.setId("concept-uuid-100105-6");
    concept.setCode("100105-6");
    concept.setName("Filaria IgG and IgM panel - Serum");
    concept.setTerminology("LOINC");
    concept.setPublisher("Regenstrief Institute, Inc.");
    concept.setVersion("2.81");
    final Map<String, String> attrs = new HashMap<>();
    attrs.put("SHORTNAME", "Filaria Ab.IgG + IgM Pnl Ser");
    concept.setAttributes(attrs);

    final Questionnaire q = FhirUtilityR4.toR4Questionnaire(concept, null, null);
    assertEquals("concept-uuid-100105-6", q.getId());
    assertEquals("100105-6", q.getCodeFirstRep().getCode());
    assertTrue(q.getUrl().endsWith("/q/100105-6"));
  }

  /**
   * Deprecated panel without SHORTNAME uses COMPONENT display for questionnaire metadata.
   *
   * @throws Exception the exception
   */
  @Test
  public void testQuestionnaireNameTitleFromComponentDisplay() throws Exception {
    final Concept concept = new Concept();
    concept.setCode("45981-8");
    concept.setName("Deprecated MDS full assessment form - version 2.0");
    concept.setTerminology("LOINC");
    concept.setPublisher("Regenstrief Institute, Inc.");
    concept.setVersion("2.81");
    final ConceptPropertyValueCoding component = new ConceptPropertyValueCoding();
    component.setPropertyCode(LoincConstants.ATTR_COMPONENT);
    component.setValueCode("LP75085-8");
    component.setValueDisplay("MDS full assessment form - version 2.0");
    concept.getFhirPropertyCodings().add(component);

    final Questionnaire q = FhirUtilityR4.toR4Questionnaire(concept, null, null);
    assertEquals("MDS_full_assessment_form_version", q.getName());
    assertEquals("MDS full assessment form - version 2.0", q.getTitle());
    assertEquals("MDS full assessment form - version 2.0", q.getCodeFirstRep().getDisplay());
  }

  /**
   * CodeSystem and implicit ValueSet dates preserve imported UTC offset on serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCodeSystemAndValueSetDatePreserveUtcOffset() throws Exception {
    final Terminology terminology = new Terminology();
    terminology.setId("loinc-cs");
    terminology.setReleaseDate("2024-08-06T00:00:00+00:00");
    terminology.setUri("http://loinc.org");
    terminology.setVersion("2.78");
    terminology.setName("Logical Observation Identifiers Names and Codes");
    terminology.setAbbreviation("LOINC");
    terminology.setPublisher("Regenstrief Institute, Inc.");
    terminology.setAttributes(new HashMap<>());
    terminology.setConceptCt(1L);
    terminology.setCreated(
        Date.from(LocalDate.of(2024, 8, 6).atStartOfDay(ZoneOffset.UTC).toInstant()));

    final CodeSystem cs = FhirUtilityR4.toR4(terminology);
    assertEquals("2024-08-06T00:00:00+00:00", cs.getDateElement().getValueAsString());

    final ValueSet vs = FhirUtilityR4.toR4ValueSet(terminology, false);
    assertEquals("2024-08-06T00:00:00+00:00", vs.getDateElement().getValueAsString());
  }

  /**
   * Mapping.type converts to R4 ConceptMapEquivalence; blank/unknown is relatedto.
   */
  @Test
  public void testToEquivalence() {
    assertEquals("relatedto", FhirUtilityR4.toEquivalence(null).toCode());
    assertEquals("relatedto", FhirUtilityR4.toEquivalence("").toCode());
    assertEquals("relatedto", FhirUtilityR4.toEquivalence("relatedto").toCode());
    assertEquals("relatedto", FhirUtilityR4.toEquivalence("related-to").toCode());
    assertEquals("equal", FhirUtilityR4.toEquivalence("equal").toCode());
    assertEquals("equivalent", FhirUtilityR4.toEquivalence("equivalent").toCode());
    assertEquals("narrower", FhirUtilityR4.toEquivalence("narrower").toCode());
    assertEquals("relatedto", FhirUtilityR4.toEquivalence("not-a-code").toCode());
  }
}
