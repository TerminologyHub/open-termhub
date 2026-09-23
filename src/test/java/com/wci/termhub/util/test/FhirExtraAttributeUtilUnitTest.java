/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UsageContext;
import org.junit.jupiter.api.Test;

import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.util.FhirExtraAttributeUtil;

/**
 * Unit tests for {@link FhirExtraAttributeUtil}.
 */
public class FhirExtraAttributeUtilUnitTest {

  /**
   * Round-trip extra top-level R4 ValueSet properties through a subset.
   */
  @Test
  public void testValueSetRoundTripR4() {
    // Build a value set with a mix of handled and "extra" properties
    final org.hl7.fhir.r4.model.ValueSet source = new org.hl7.fhir.r4.model.ValueSet();
    source.setUrl("http://example.org/ValueSet/vs-1");
    source.setName("Example");
    source.setStatus(PublicationStatus.ACTIVE);
    // Extra: simple primitive
    source.setPurpose("Testing preservation of extra properties");
    source.setImmutable(true);
    // Extra: complex repeating structure
    final UsageContext usage = new UsageContext();
    usage.getCode().setSystem("http://terminology.hl7.org/CodeSystem/usage-context-type")
        .setCode("focus");
    usage.setValue(new CodeableConcept().addCoding(
        new org.hl7.fhir.r4.model.Coding("http://snomed.info/sct", "12345", "example")));
    source.addUseContext(usage);
    source.addJurisdiction(new CodeableConcept()
        .addCoding(new org.hl7.fhir.r4.model.Coding("urn:iso:std:iso:3166", "US", "United States")));
    // Extra: resource-level extension
    source.addExtension(new Extension("http://example.org/ext/foo", new StringType("bar")));

    // Store onto subset
    final Subset subset = new Subset();
    FhirExtraAttributeUtil.storeExtraValueSetAttributesR4(subset, source);

    // Handled fields should NOT be stored as extras
    assertFalse(subset.getAttributes().containsKey(FhirExtraAttributeUtil.VS_ATTR_PREFIX + "url"));
    assertFalse(subset.getAttributes().containsKey(FhirExtraAttributeUtil.VS_ATTR_PREFIX + "name"));
    assertFalse(
        subset.getAttributes().containsKey(FhirExtraAttributeUtil.VS_ATTR_PREFIX + "status"));
    // Extra fields should be stored
    assertTrue(
        subset.getAttributes().containsKey(FhirExtraAttributeUtil.VS_ATTR_PREFIX + "purpose"));
    assertTrue(
        subset.getAttributes().containsKey(FhirExtraAttributeUtil.VS_ATTR_PREFIX + "useContext"));
    assertTrue(
        subset.getAttributes().containsKey(FhirExtraAttributeUtil.VS_ATTR_PREFIX + "jurisdiction"));
    assertTrue(
        subset.getAttributes().containsKey(FhirExtraAttributeUtil.VS_ATTR_PREFIX + "extension"));

    // Reconstruct onto a fresh value set (as toR4ValueSet would)
    org.hl7.fhir.r4.model.ValueSet target = new org.hl7.fhir.r4.model.ValueSet();
    target.setUrl(source.getUrl());
    target.setName(source.getName());
    target = FhirExtraAttributeUtil.applyExtraToR4ValueSet(target, subset);

    assertEquals("Testing preservation of extra properties", target.getPurpose());
    assertTrue(target.getImmutable());
    assertEquals(1, target.getUseContext().size());
    assertEquals("focus", target.getUseContextFirstRep().getCode().getCode());
    assertEquals("12345",
        target.getUseContextFirstRep().getValueCodeableConcept().getCodingFirstRep().getCode());
    assertEquals(1, target.getJurisdiction().size());
    assertEquals("US", target.getJurisdictionFirstRep().getCodingFirstRep().getCode());
    assertEquals(1, target.getExtension().size());
    assertEquals("http://example.org/ext/foo", target.getExtension().get(0).getUrl());
    assertEquals("bar", ((StringType) target.getExtension().get(0).getValue()).getValue());
    // Handled fields untouched by reconstruction
    assertEquals("http://example.org/ValueSet/vs-1", target.getUrl());
    assertEquals("Example", target.getName());
  }

  /**
   * Round-trip extra top-level R5 ValueSet properties, including an R5-only field.
   */
  @Test
  public void testValueSetRoundTripR5() {
    final org.hl7.fhir.r5.model.ValueSet source = new org.hl7.fhir.r5.model.ValueSet();
    source.setUrl("http://example.org/ValueSet/vs-2");
    source.setStatus(org.hl7.fhir.r5.model.Enumerations.PublicationStatus.ACTIVE);
    source.setPurpose("R5 purpose");
    // R5-only: copyrightLabel and approvalDate
    source.setCopyrightLabel("Some copyright label");
    source.setApprovalDateElement(new org.hl7.fhir.r5.model.DateType("2024-01-15"));
    source.addContact(new org.hl7.fhir.r5.model.ContactDetail().setName("Ignored - handled"));

    final Subset subset = new Subset();
    FhirExtraAttributeUtil.storeExtraValueSetAttributesR5(subset, source);

    // contact is handled elsewhere; must not be captured as an extra
    assertFalse(
        subset.getAttributes().containsKey(FhirExtraAttributeUtil.VS_ATTR_PREFIX + "contact"));
    assertTrue(subset.getAttributes()
        .containsKey(FhirExtraAttributeUtil.VS_ATTR_PREFIX + "copyrightLabel"));
    assertTrue(
        subset.getAttributes().containsKey(FhirExtraAttributeUtil.VS_ATTR_PREFIX + "approvalDate"));

    org.hl7.fhir.r5.model.ValueSet target = new org.hl7.fhir.r5.model.ValueSet();
    target = FhirExtraAttributeUtil.applyExtraToR5ValueSet(target, subset);

    assertEquals("R5 purpose", target.getPurpose());
    assertEquals("Some copyright label", target.getCopyrightLabel());
    assertEquals("2024-01-15", target.getApprovalDateElement().getValueAsString());
    // contact was not preserved as an extra, so it stays empty here
    assertTrue(target.getContact().isEmpty());
  }

  /**
   * Round-trip extra top-level R4 CodeSystem properties through a terminology, storing from a JSON
   * tree as the loader does.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCodeSystemRoundTripR4() throws Exception {
    // Build a code system with handled fields, bulk content, and "extra" properties
    final org.hl7.fhir.r4.model.CodeSystem source = new org.hl7.fhir.r4.model.CodeSystem();
    source.setUrl("http://example.org/CodeSystem/cs-1");
    source.setName("ExampleCs");
    source.setStatus(PublicationStatus.ACTIVE);
    source.setContent(org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode.COMPLETE);
    // Bulk content that must NOT be preserved as extras
    source.addConcept().setCode("A").setDisplay("Alpha");
    // Extra properties
    source.setPurpose("CS purpose");
    source.setExperimental(true);
    source.addUseContext(new UsageContext(
        new org.hl7.fhir.r4.model.Coding("http://terminology.hl7.org/CodeSystem/usage-context-type",
            "focus", null),
        new CodeableConcept().addCoding(
            new org.hl7.fhir.r4.model.Coding("http://snomed.info/sct", "111", "x"))));

    // Store from the JSON tree (mirrors CodeSystemLoaderUtil which parses JSON directly)
    final com.fasterxml.jackson.databind.JsonNode root = com.wci.termhub.util.ThreadLocalMapper.get()
        .readTree(ca.uhn.fhir.context.FhirContext.forR4().newJsonParser()
            .encodeResourceToString(source));
    final Terminology terminology = new Terminology();
    FhirExtraAttributeUtil.storeExtraCodeSystemAttributes(terminology, root);

    // Bulk / handled fields not stored as extras
    assertFalse(
        terminology.getAttributes().containsKey(FhirExtraAttributeUtil.CS_ATTR_PREFIX + "concept"));
    assertFalse(
        terminology.getAttributes().containsKey(FhirExtraAttributeUtil.CS_ATTR_PREFIX + "url"));
    assertFalse(
        terminology.getAttributes().containsKey(FhirExtraAttributeUtil.CS_ATTR_PREFIX + "content"));
    // Extras stored
    assertTrue(
        terminology.getAttributes().containsKey(FhirExtraAttributeUtil.CS_ATTR_PREFIX + "purpose"));
    assertTrue(terminology.getAttributes()
        .containsKey(FhirExtraAttributeUtil.CS_ATTR_PREFIX + "experimental"));
    assertTrue(terminology.getAttributes()
        .containsKey(FhirExtraAttributeUtil.CS_ATTR_PREFIX + "useContext"));

    // Reconstruct
    org.hl7.fhir.r4.model.CodeSystem target = new org.hl7.fhir.r4.model.CodeSystem();
    target.setUrl(source.getUrl());
    target.setExperimental(false);
    target = FhirExtraAttributeUtil.applyExtraToR4CodeSystem(target, terminology);

    assertEquals("CS purpose", target.getPurpose());
    assertTrue(target.getExperimental());
    assertEquals(1, target.getUseContext().size());
    assertEquals("focus", target.getUseContextFirstRep().getCode().getCode());
    // Bulk content not reconstructed via extras
    assertTrue(target.getConcept().isEmpty());
    assertEquals("http://example.org/CodeSystem/cs-1", target.getUrl());
  }

  /**
   * Round-trip extra top-level R5 CodeSystem properties.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCodeSystemRoundTripR5() throws Exception {
    final org.hl7.fhir.r5.model.CodeSystem source = new org.hl7.fhir.r5.model.CodeSystem();
    source.setUrl("http://example.org/CodeSystem/cs-2");
    source.setStatus(org.hl7.fhir.r5.model.Enumerations.PublicationStatus.ACTIVE);
    source.setPurpose("CS5 purpose");
    source.setApprovalDateElement(new org.hl7.fhir.r5.model.DateType("2023-05-01"));

    final com.fasterxml.jackson.databind.JsonNode root = com.wci.termhub.util.ThreadLocalMapper.get()
        .readTree(ca.uhn.fhir.context.FhirContext.forR5().newJsonParser()
            .encodeResourceToString(source));
    final Terminology terminology = new Terminology();
    FhirExtraAttributeUtil.storeExtraCodeSystemAttributes(terminology, root);

    assertTrue(
        terminology.getAttributes().containsKey(FhirExtraAttributeUtil.CS_ATTR_PREFIX + "purpose"));
    assertTrue(terminology.getAttributes()
        .containsKey(FhirExtraAttributeUtil.CS_ATTR_PREFIX + "approvalDate"));

    org.hl7.fhir.r5.model.CodeSystem target = new org.hl7.fhir.r5.model.CodeSystem();
    target = FhirExtraAttributeUtil.applyExtraToR5CodeSystem(target, terminology);

    assertEquals("CS5 purpose", target.getPurpose());
    assertEquals("2023-05-01", target.getApprovalDateElement().getValueAsString());
  }

  /**
   * Round-trip extra top-level R4 ConceptMap properties through a mapset, ensuring bulk group and
   * source/target are excluded.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConceptMapRoundTripR4() throws Exception {
    final org.hl7.fhir.r4.model.ConceptMap source = new org.hl7.fhir.r4.model.ConceptMap();
    source.setUrl("http://example.org/ConceptMap/cm-1");
    source.setStatus(PublicationStatus.ACTIVE);
    source.setSource(new org.hl7.fhir.r4.model.UriType("http://example.org/from"));
    source.setTarget(new org.hl7.fhir.r4.model.UriType("http://example.org/to"));
    // Bulk content that must NOT be preserved as extras
    source.addGroup().setSource("http://example.org/from").setTarget("http://example.org/to")
        .addElement().setCode("A");
    // Extra properties
    source.setPurpose("CM purpose");
    source.setExperimental(true);
    source.addExtension(new Extension("http://example.org/ext/cm", new StringType("v")));

    final com.fasterxml.jackson.databind.JsonNode root = com.wci.termhub.util.ThreadLocalMapper.get()
        .readTree(ca.uhn.fhir.context.FhirContext.forR4().newJsonParser()
            .encodeResourceToString(source));
    final Mapset mapset = new Mapset();
    FhirExtraAttributeUtil.storeExtraConceptMapAttributes(mapset, root);

    // Bulk / handled fields not stored as extras
    assertFalse(
        mapset.getAttributes().containsKey(FhirExtraAttributeUtil.CM_ATTR_PREFIX + "group"));
    assertFalse(
        mapset.getAttributes().containsKey(FhirExtraAttributeUtil.CM_ATTR_PREFIX + "source"));
    assertFalse(
        mapset.getAttributes().containsKey(FhirExtraAttributeUtil.CM_ATTR_PREFIX + "target"));
    // Extras stored
    assertTrue(
        mapset.getAttributes().containsKey(FhirExtraAttributeUtil.CM_ATTR_PREFIX + "purpose"));
    assertTrue(
        mapset.getAttributes().containsKey(FhirExtraAttributeUtil.CM_ATTR_PREFIX + "experimental"));
    assertTrue(
        mapset.getAttributes().containsKey(FhirExtraAttributeUtil.CM_ATTR_PREFIX + "extension"));

    org.hl7.fhir.r4.model.ConceptMap target = new org.hl7.fhir.r4.model.ConceptMap();
    target.setUrl(source.getUrl());
    target = FhirExtraAttributeUtil.applyExtraToR4ConceptMap(target, mapset);

    assertEquals("CM purpose", target.getPurpose());
    assertTrue(target.getExperimental());
    assertEquals(1, target.getExtension().size());
    // Bulk content not reconstructed via extras
    assertTrue(target.getGroup().isEmpty());
    assertEquals("http://example.org/ConceptMap/cm-1", target.getUrl());
  }

  /**
   * Round-trip extra top-level R5 ConceptMap properties.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConceptMapRoundTripR5() throws Exception {
    final org.hl7.fhir.r5.model.ConceptMap source = new org.hl7.fhir.r5.model.ConceptMap();
    source.setUrl("http://example.org/ConceptMap/cm-2");
    source.setStatus(org.hl7.fhir.r5.model.Enumerations.PublicationStatus.ACTIVE);
    source.setPurpose("CM5 purpose");
    source.addExtension(new org.hl7.fhir.r5.model.Extension("http://example.org/ext/cm5",
        new org.hl7.fhir.r5.model.StringType("w")));

    final com.fasterxml.jackson.databind.JsonNode root = com.wci.termhub.util.ThreadLocalMapper.get()
        .readTree(ca.uhn.fhir.context.FhirContext.forR5().newJsonParser()
            .encodeResourceToString(source));
    final Mapset mapset = new Mapset();
    FhirExtraAttributeUtil.storeExtraConceptMapAttributes(mapset, root);

    assertTrue(
        mapset.getAttributes().containsKey(FhirExtraAttributeUtil.CM_ATTR_PREFIX + "purpose"));
    assertTrue(
        mapset.getAttributes().containsKey(FhirExtraAttributeUtil.CM_ATTR_PREFIX + "extension"));

    org.hl7.fhir.r5.model.ConceptMap target = new org.hl7.fhir.r5.model.ConceptMap();
    target = FhirExtraAttributeUtil.applyExtraToR5ConceptMap(target, mapset);

    assertEquals("CM5 purpose", target.getPurpose());
    assertEquals(1, target.getExtension().size());
  }

  /**
   * No extra attributes present is a no-op.
   */
  @Test
  public void testNoExtras() {
    final Subset subset = new Subset();
    subset.getAttributes().put("loaded", "true");
    final org.hl7.fhir.r4.model.ValueSet target = new org.hl7.fhir.r4.model.ValueSet();
    final org.hl7.fhir.r4.model.ValueSet result =
        FhirExtraAttributeUtil.applyExtraToR4ValueSet(target, subset);
    assertFalse(result.hasPurpose());
  }
}
