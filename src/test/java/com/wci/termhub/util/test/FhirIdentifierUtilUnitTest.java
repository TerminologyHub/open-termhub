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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Subset;
import com.wci.termhub.util.FhirIdentifierUtil;
import com.wci.termhub.util.ThreadLocalMapper;

/**
 * Unit tests for {@link FhirIdentifierUtil}.
 */
public class FhirIdentifierUtilUnitTest {

  /**
   * TermHub subset identifier is rewritten to vendor basis on storage.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalizeTermHubSubsetIdentifier() throws Exception {
    final JsonNode input = ThreadLocalMapper.get().readTree(
        "[{\"system\":\"https://terminologyhub.com/model/subset/code\",\"value\":\"loinc-document-ontology\"}]");
    final String stored = FhirIdentifierUtil.normalizeForStorage(input, "http://loinc.org?fhir_vs");
    assertNotNull(stored);
    final JsonNode out = ThreadLocalMapper.get().readTree(stored);
    assertEquals("http://loinc.org", out.get(0).path("system").asText());
    assertEquals("loinc-document-ontology", out.get(0).path("value").asText());
  }

  /**
   * Vendor identifiers are preserved unchanged.
   *
   * @throws Exception the exception
   */
  @Test
  public void testPreserveVendorIdentifier() throws Exception {
    final JsonNode input = ThreadLocalMapper.get().readTree(
        "[{\"system\":\"urn:ietf:rfc:3986\",\"value\":\"urn:oid:2.16.840.1.113883.6.1\"}]");
    final String stored = FhirIdentifierUtil.normalizeForStorage(input, "http://loinc.org");
    assertEquals(input.toString(), stored);
  }

  /**
   * ValueSet serve uses stored fhirIdentifier pass-through.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValueSetServePassThrough() throws Exception {
    final Subset subset = new Subset();
    subset.setId("vs-1");
    subset.setCode("loinc-document-ontology");
    subset.setName("test");
    subset.setAbbreviation("test");
    subset.setPublisher("p");
    subset.setVersion("1");
    subset.setUri("http://example.org/vs");
    final Map<String, String> attrs = new HashMap<>();
    attrs.put("fhirIncludesUri", "http://loinc.org");
    attrs.put(Subset.Attributes.fhirIdentifier.name(),
        "[{\"system\":\"http://loinc.org\",\"value\":\"loinc-document-ontology\"}]");
    subset.setAttributes(attrs);

    final ValueSet valueSet = FhirUtilityR4.toR4ValueSet(subset, List.of(), false, null);

    assertEquals(1, valueSet.getIdentifier().size());
    assertEquals("http://loinc.org", valueSet.getIdentifierFirstRep().getSystem());
    assertEquals("loinc-document-ontology", valueSet.getIdentifierFirstRep().getValue());
  }

  /**
   * ConceptMap serve uses stored fhirIdentifier pass-through.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConceptMapServePassThrough() throws Exception {
    final Mapset mapset = new Mapset();
    mapset.setId("cm-1");
    mapset.setCode("6011000124106");
    mapset.setName("test");
    mapset.setAbbreviation("test");
    mapset.setPublisher("p");
    mapset.setVersion("1");
    mapset.setUri("http://example.org/cm");
    final Map<String, String> attrs = new HashMap<>();
    attrs.put("fhirSourceUri", "http://snomed.info/sct");
    attrs.put(FhirIdentifierUtil.ATTR_FHIR_IDENTIFIER,
        "[{\"system\":\"http://snomed.info/sct\",\"value\":\"6011000124106\"}]");
    mapset.setAttributes(attrs);

    final ConceptMap conceptMap = FhirUtilityR4.toR4(mapset);

    assertTrue(conceptMap.hasIdentifier());
    assertEquals("http://snomed.info/sct", conceptMap.getIdentifier().getSystem());
    assertEquals("6011000124106", conceptMap.getIdentifier().getValue());
  }

  /**
   * R4 identifier list normalization.
   */
  @Test
  public void testFromR4Identifiers() {
    final Identifier identifier = new Identifier()
        .setSystem("https://terminologyhub.com/model/subset/code").setValue("900000000000207008");
    final String stored =
        FhirIdentifierUtil.fromR4Identifiers(List.of(identifier), "http://snomed.info/sct");
    assertNotNull(stored);
    assertTrue(stored.contains("\"system\":\"http://snomed.info/sct\""));
    assertTrue(stored.contains("\"value\":\"900000000000207008\""));
    assertFalse(stored.contains("terminologyhub.com"));
  }
}
