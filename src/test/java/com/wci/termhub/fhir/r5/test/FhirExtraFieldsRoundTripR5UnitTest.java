/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r5.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.ConceptMap;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.ValueSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;

import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.fhir.r5.CodeSystemProviderR5;
import com.wci.termhub.fhir.r5.ConceptMapProviderR5;
import com.wci.termhub.fhir.r5.ValueSetProviderR5;
import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.CodeSystemLoaderUtil;
import com.wci.termhub.util.ConceptMapLoaderUtil;
import com.wci.termhub.util.ValueSetLoaderUtil;

import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;

/**
 * Verifies that "extra" top-level FHIR properties survive the full load -&gt; serve flow for R5
 * CodeSystem, ValueSet, and ConceptMap resources through their providers, including R5-only fields.
 */
public class FhirExtraFieldsRoundTripR5UnitTest extends AbstractFhirR5ServerTest {

  /** Sample CodeSystem file exercising all top-level fields. */
  private static final String CODE_SYSTEM_FILE = "CodeSystem-extra-fields-test-r5.json";

  /** Sample ValueSet file exercising all top-level fields. */
  private static final String VALUE_SET_FILE = "ValueSet-extra-fields-test-r5.json";

  /** Sample ConceptMap file exercising all top-level fields. */
  private static final String CONCEPT_MAP_FILE = "ConceptMap-extra-fields-test-r5.json";

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The value set provider. */
  @Autowired
  private ValueSetProviderR5 valueSetProvider;

  /** The code system provider. */
  @Autowired
  private CodeSystemProviderR5 codeSystemProvider;

  /** The concept map provider. */
  @Autowired
  private ConceptMapProviderR5 conceptMapProvider;

  /** Loaded resource ids. */
  private static String codeSystemId;

  /** The value set id. */
  private static String valueSetId;

  /** The concept map id. */
  private static String conceptMapId;

  /** The request. */
  private MockHttpServletRequest request;

  /** The details. */
  private ServletRequestDetails details;

  /**
   * Load the sample resources exercising all top-level fields.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public void loadExtraFields() throws Exception {
    final CodeSystem cs = CodeSystemLoaderUtil.loadCodeSystem(searchService,
        new ClassPathResource("data/" + CODE_SYSTEM_FILE).getFile(), false, CodeSystem.class,
        new DefaultProgressListener());
    codeSystemId = cs.getIdElement().getIdPart();

    final ValueSet vs = ValueSetLoaderUtil.loadValueSet(searchService,
        new ClassPathResource("data/" + VALUE_SET_FILE).getFile(), ValueSet.class,
        new DefaultProgressListener());
    valueSetId = vs.getIdElement().getIdPart();

    final ConceptMap cm = ConceptMapLoaderUtil.loadConceptMap(searchService,
        new ClassPathResource("data/" + CONCEPT_MAP_FILE).getFile(), ConceptMap.class,
        new DefaultProgressListener());
    conceptMapId = cm.getIdElement().getIdPart();

    FhirUtility.clearCaches();
  }

  /**
   * Setup per-test request objects.
   */
  @BeforeEach
  public void setup() {
    request = new MockHttpServletRequest();
    details = new ServletRequestDetails();
    details.setServletRequest(request);
  }

  /**
   * Verify the ValueSet served by the provider preserves all top-level fields (incl. R5-only).
   *
   * @throws Exception the exception
   */
  @Test
  public void testValueSetExtraFields() throws Exception {
    final ValueSet vs = valueSetProvider.getValueSet(request, details, new IdType(valueSetId));
    assertNotNull(vs);

    // Handled fields
    assertEquals("http://example.org/ValueSet/extra-fields-test", vs.getUrl());
    assertEquals("A ValueSet exercising all top-level fields", vs.getDescription());
    assertEquals("Copyright example for ValueSet", vs.getCopyright());
    assertEquals("VS-EXTRA-1", vs.getIdentifierFirstRep().getValue());
    assertEquals("Example Contact", vs.getContactFirstRep().getName());
    assertTrue(vs.getExperimental());

    // Extra fields reconstructed from attributes
    assertEquals("To test round-tripping of ValueSet top-level fields", vs.getPurpose());
    assertTrue(vs.getImmutable());
    assertEquals("en", vs.getLanguage());
    assertTrue(vs.hasText());
    assertEquals(1, vs.getUseContext().size());
    assertEquals("focus", vs.getUseContextFirstRep().getCode().getCode());
    assertEquals(1, vs.getJurisdiction().size());
    assertEquals("US", vs.getJurisdictionFirstRep().getCodingFirstRep().getCode());
    assertEquals("valueset-note", findExtensionValue(vs.getExtension(), "http://example.org/ext/note"));
    assertFalse(vs.getContained().isEmpty());

    // R5-only fields
    assertEquals("Example copyright label", vs.getCopyrightLabel());
    assertEquals("2024-01-15", vs.getApprovalDateElement().getValueAsString());
    assertEquals("semver", vs.getVersionAlgorithmStringType().getValue());
    assertTrue(vs.hasScope());
    assertEquals("All diabetes-related concepts", vs.getScope().getInclusionCriteria());
    assertEquals(1, vs.getTopic().size());
    assertEquals(1, vs.getAuthor().size());
  }

  /**
   * Verify the CodeSystem served by the provider preserves all top-level fields (incl. R5-only).
   *
   * @throws Exception the exception
   */
  @Test
  public void testCodeSystemExtraFields() throws Exception {
    final CodeSystem cs = codeSystemProvider.getCodeSystem(request, details, new IdType(codeSystemId));
    assertNotNull(cs);

    // Handled fields
    assertEquals("http://example.org/CodeSystem/extra-fields-test", cs.getUrl());
    assertEquals("A CodeSystem exercising all top-level fields", cs.getDescription());
    assertEquals("Copyright example for CodeSystem", cs.getCopyright());
    assertEquals("CS-EXTRA-1", cs.getIdentifierFirstRep().getValue());
    assertEquals("Example Contact", cs.getContactFirstRep().getName());
    assertTrue(cs.getCaseSensitive());
    assertEquals("http://example.org/ValueSet/extra-fields-test-all", cs.getValueSet());

    // Extra fields reconstructed from attributes
    assertEquals("To test round-tripping of CodeSystem top-level fields", cs.getPurpose());
    assertTrue(cs.getExperimental());
    assertEquals("en", cs.getLanguage());
    assertTrue(cs.hasText());
    assertEquals(1, cs.getUseContext().size());
    assertEquals("focus", cs.getUseContextFirstRep().getCode().getCode());
    assertEquals(1, cs.getJurisdiction().size());
    assertEquals("US", cs.getJurisdictionFirstRep().getCodingFirstRep().getCode());
    assertEquals("codesystem-note", findExtensionValue(cs.getExtension(), "http://example.org/ext/note"));
    assertFalse(cs.getFilter().isEmpty());

    // R5-only fields
    assertEquals("Example copyright label", cs.getCopyrightLabel());
    assertEquals("2024-01-15", cs.getApprovalDateElement().getValueAsString());
    assertEquals("semver", cs.getVersionAlgorithmStringType().getValue());
    assertEquals(1, cs.getTopic().size());
    assertEquals(1, cs.getAuthor().size());
  }

  /**
   * Verify the ConceptMap served by the provider preserves all top-level fields (incl. R5-only).
   *
   * @throws Exception the exception
   */
  @Test
  public void testConceptMapExtraFields() throws Exception {
    final ConceptMap cm = conceptMapProvider.getConceptMap(request, details, new IdType(conceptMapId));
    assertNotNull(cm);

    // Handled fields
    assertEquals("http://example.org/ConceptMap/extra-fields-test", cm.getUrl());
    assertEquals("A ConceptMap exercising all top-level fields", cm.getDescription());
    assertEquals("Copyright example for ConceptMap", cm.getCopyright());
    assertEquals("CM-EXTRA-1", cm.getIdentifierFirstRep().getValue());
    assertEquals("Example Contact", cm.getContactFirstRep().getName());

    // Extra fields reconstructed from attributes
    assertEquals("To test round-tripping of ConceptMap top-level fields", cm.getPurpose());
    assertTrue(cm.getExperimental());
    assertEquals("en", cm.getLanguage());
    assertTrue(cm.hasText());
    assertEquals(1, cm.getUseContext().size());
    assertEquals("focus", cm.getUseContextFirstRep().getCode().getCode());
    assertEquals(1, cm.getJurisdiction().size());
    assertEquals("US", cm.getJurisdictionFirstRep().getCodingFirstRep().getCode());
    assertEquals("conceptmap-note", findExtensionValue(cm.getExtension(), "http://example.org/ext/note"));

    // R5-only fields
    assertEquals("Example copyright label", cm.getCopyrightLabel());
    assertEquals("2024-01-15", cm.getApprovalDateElement().getValueAsString());
    assertEquals("semver", cm.getVersionAlgorithmStringType().getValue());
    assertEquals(1, cm.getTopic().size());
    assertEquals(1, cm.getAuthor().size());
  }

  /**
   * Find a string extension value by url.
   *
   * @param extensions the extensions
   * @param url the url
   * @return the value, or null
   */
  private static String findExtensionValue(final java.util.List<Extension> extensions,
    final String url) {
    for (final Extension extension : extensions) {
      if (url.equals(extension.getUrl()) && extension.getValue() instanceof StringType) {
        return ((StringType) extension.getValue()).getValue();
      }
    }
    return null;
  }
}
