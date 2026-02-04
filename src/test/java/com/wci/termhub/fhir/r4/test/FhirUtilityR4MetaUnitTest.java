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
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    final CodeSystem cs = FhirUtilityR4.toR4(terminology);
    assertNotNull(cs.getMeta());
    assertEquals("1", cs.getMeta().getVersionId());
    assertNotNull(cs.getMeta().getLastUpdated());
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

    final Questionnaire q = FhirUtilityR4.toR4Questionnaire(terminology, true);
    assertNotNull(q.getMeta());
    assertEquals("1", q.getMeta().getVersionId());
    assertNotNull(q.getMeta().getLastUpdated());
  }
}
