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
import java.util.Map;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.util.DateUtility;

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
    assertNotNull(cs.getContact().get(0).getTelecom());
    assertTrue(cs.getContact().get(0).getTelecom().size() >= 1);
    assertEquals("http://loinc.org", cs.getContact().get(0).getTelecom().get(0).getValue());
    assertFalse(cs.getCaseSensitive());
    assertFalse(cs.getVersionNeeded());
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
  }

  /**
   * Test Questionnaire meta has versionId and lastUpdated.
   *
   * @throws Exception the exception
   */
  @Test
  public void testQuestionnaireMeta() throws Exception {
    final Terminology terminology = new Terminology();
    terminology.setId("test-cs");
    terminology.setReleaseDate("2022-04-11");
    terminology.setUri("http://example.org/cs");
    terminology.setVersion("1");
    terminology.setName("Test CodeSystem");
    terminology.setAbbreviation("TCS");
    terminology.setPublisher("Test");
    terminology.setAttributes(new HashMap<>());
    terminology.setCreated(Date.from(LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant()));

    final Questionnaire q = FhirUtilityR4.toR4Questionnaire(terminology, true);
    assertNotNull(q.getMeta());
    assertEquals("1", q.getMeta().getVersionId());
    assertNotNull(q.getMeta().getLastUpdated());
  }
}
