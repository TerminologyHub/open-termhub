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

import java.util.List;

import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.util.CodeSystemMetadataProperty;
import com.wci.termhub.fhir.util.CodeSystemMetadataPropertyUtility;
import com.wci.termhub.model.MetaModel;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.Terminology;

/**
 * Unit tests for {@link CodeSystemMetadataPropertyUtility}.
 */
public class CodeSystemMetadataPropertyUtilityTest {

  /**
   * LOINC: {@code parent} uses HL7 {@code concept-properties#parent}
   * (fhir.loinc.org parity), not {@code http://loinc.org/property/parent}.
   */
  @Test
  public void testLoincParentMetadataUsesHl7ConceptPropertyUri() {
    final Terminology terminology = new Terminology();
    terminology.setUri("http://loinc.org");

    final Metadata parent =
        new Metadata(MetaModel.Model.relationship, MetaModel.Field.additionalType);
    parent.setCode("parent");
    parent.setName("A parent code in the Component Hierarchy by System");
    parent.getAttributes().put("fhirPropertyType", "code");

    final List<CodeSystemMetadataProperty> properties =
        CodeSystemMetadataPropertyUtility.buildProperties(terminology, List.of(parent));

    assertEquals(3, properties.size());
    final CodeSystemMetadataProperty p = properties.stream()
        .filter(x -> "parent".equalsIgnoreCase(x.getCode())).findFirst().orElseThrow();
    assertEquals(CodeSystemMetadataProperty.FHIR_CONCEPT_PROPERTY_URI_PARENT, p.getUri());
    assertEquals("code", p.getType());
  }

  /**
   * Non-LOINC: parent / child / status use HL7 concept-properties fragment
   * URIs.
   */
  @Test
  public void testLoincStatusMetadataUsesLoincPropertyUri() {
    final Terminology terminology = new Terminology();
    terminology.setUri("http://loinc.org");

    final Metadata status = new Metadata(MetaModel.Model.concept, MetaModel.Field.attribute);
    status.setCode("STATUS");
    status.setName("Status of the term");
    status.getAttributes().put("fhirPropertyType", "string");

    final CodeSystemMetadataProperty p =
        CodeSystemMetadataProperty.fromMetadata(status, "http://loinc.org", "string");
    assertEquals("http://loinc.org/property/STATUS", p.getUri());
    assertEquals("string", p.getType());
  }

  /**
   * Test loinc child uses hl 7 concept property uri.
   */
  @Test
  public void testLoincChildUsesHl7ConceptPropertyUri() {
    final Terminology terminology = new Terminology();
    terminology.setUri("http://loinc.org");

    final Metadata child =
        new Metadata(MetaModel.Model.relationship, MetaModel.Field.additionalType);
    child.setCode("child");
    child.setName("A child code in the Component Hierarchy by System");
    child.getAttributes().put("fhirPropertyType", "code");

    final List<CodeSystemMetadataProperty> properties =
        CodeSystemMetadataPropertyUtility.buildProperties(terminology, List.of(child));

    final CodeSystemMetadataProperty p = properties.stream()
        .filter(x -> "child".equalsIgnoreCase(x.getCode())).findFirst().orElseThrow();
    assertEquals(CodeSystemMetadataProperty.FHIR_CONCEPT_PROPERTY_URI_CHILD, p.getUri());
  }

  /**
   * Test non loinc parent metadata uses hl 7 concept property uri.
   */
  @Test
  public void testNonLoincParentMetadataUsesHl7ConceptPropertyUri() {
    final Terminology terminology = new Terminology();
    terminology.setUri("http://snomed.info/sct");

    final Metadata parent =
        new Metadata(MetaModel.Model.relationship, MetaModel.Field.additionalType);
    parent.setCode("parent");
    parent.setName("p");
    parent.getAttributes().put("fhirPropertyType", "code");

    final List<CodeSystemMetadataProperty> properties =
        CodeSystemMetadataPropertyUtility.buildProperties(terminology, List.of(parent));

    final CodeSystemMetadataProperty p = properties.stream()
        .filter(x -> "parent".equalsIgnoreCase(x.getCode())).findFirst().orElseThrow();
    assertEquals(CodeSystemMetadataProperty.FHIR_CONCEPT_PROPERTY_URI_PARENT, p.getUri());
  }

  /**
   * Test fhir concept property uri for code case insensitive.
   */
  @Test
  public void testFhirConceptPropertyUriForCodeCaseInsensitive() {
    assertEquals(CodeSystemMetadataProperty.FHIR_CONCEPT_PROPERTY_URI_PARENT,
        CodeSystemMetadataProperty.fhirConceptPropertyUriForCode("PARENT"));
    assertEquals(CodeSystemMetadataProperty.FHIR_CONCEPT_PROPERTY_URI_STATUS,
        CodeSystemMetadataProperty.fhirConceptPropertyUriForCode("STATUS"));
  }

  /**
   * Test non standard property uri still composed.
   */
  @Test
  public void testNonStandardPropertyUriStillComposed() {
    final Metadata m = new Metadata(MetaModel.Model.concept, MetaModel.Field.attribute);
    m.setCode("ORDINAL");
    m.setName("Ordinal");

    final CodeSystemMetadataProperty p =
        CodeSystemMetadataProperty.fromMetadata(m, "http://loinc.org", "string");
    assertEquals("http://loinc.org/property/ORDINAL", p.getUri());
  }

  /**
   * Test fhir uri when code system url empty.
   */
  @Test
  public void testFhirUriWhenCodeSystemUrlEmpty() {
    final Metadata parent =
        new Metadata(MetaModel.Model.relationship, MetaModel.Field.additionalType);
    parent.setCode("parent");
    parent.setName("p");

    final CodeSystemMetadataProperty p =
        CodeSystemMetadataProperty.fromMetadata(parent, "", "string");
    assertEquals(CodeSystemMetadataProperty.FHIR_CONCEPT_PROPERTY_URI_PARENT, p.getUri());
  }
}
