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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetFilterComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionContainsComponent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * FHIR R4 ingest of Regenstrief-style ad hoc ValueSets (missing version, filters).
 */
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
@TestPropertySource(properties = {
    "lucene.index.directory=build/index/lucene-fhir-r4-adhoc"
})
public class AdHocValueSetR4UnitTest extends AbstractFhirR4ServerTest {

  /** FHIR ValueSet path. */
  private static final String FHIR_VALUESET = "/fhir/r4/ValueSet";

  /** FHIR CodeSystem path. */
  private static final String FHIR_CODESYSTEM = "/fhir/r4/CodeSystem";

  /** FHIR transaction path (HAPI @Transaction is POST to the FHIR base). */
  private static final String FHIR_TRANSACTION = "/fhir/r4";

  /** LOINC sandbox version from CodeSystem-lnc-sandbox-277-r4.json. */
  private static final String LOINC_SANDBOX_VERSION = "277";

  /** The port. */
  @LocalServerPort
  private int port;

  /** The rest template. */
  @Autowired
  private TestRestTemplate restTemplate;

  /** The parser. */
  private static IParser parser;

  /**
   * Sets the up once.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public void setUpParser() throws Exception {
    parser = FhirContext.forR4().newJsonParser();
  }

  /**
   * Extensional POST without version injects the loaded LOINC version.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testExtensionalPostInjectsLoincVersion() throws Exception {
    final ValueSet created = postValueSet("data/ValueSet-adhoc-sars-cov-2-aoe.json");
    assertEquals(LOINC_SANDBOX_VERSION, created.getVersion());
    assertTrue(created.hasCompose());
    assertEquals(3, created.getCompose().getIncludeFirstRep().getConcept().size());

    final ValueSet read = getValueSet(created.getIdPart());
    assertEquals(LOINC_SANDBOX_VERSION, read.getVersion());
    assertEquals("active", read.getStatus().toCode());
    assertFalse(read.getUseContext().isEmpty());
    assertFalse(read.getIdentifier().isEmpty());
    assertFalse(read.getContact().isEmpty());
    assertEquals("http://loinc.org", read.getCompose().getIncludeFirstRep().getSystem());
  }

  /**
   * Transaction bundle with extensional and intensional entries succeeds.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(2)
  public void testMixedTransactionSucceeds() throws Exception {
    final String json = readClasspath("data/Bundle-adhoc-mixed-valuesets.json");
    final ResponseEntity<String> response = postFhir(FHIR_TRANSACTION, json);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    final Bundle bundle = parser.parseResource(Bundle.class, response.getBody());
    assertEquals(2, bundle.getEntry().size());
    for (final BundleEntryComponent entry : bundle.getEntry()) {
      assertNotNull(entry.getResponse());
      assertTrue(entry.getResponse().getStatus().startsWith("200")
          || entry.getResponse().getStatus().startsWith("201"),
          "Entry should succeed: " + entry.getResponse().getStatus());
      assertTrue(entry.getResource() instanceof ValueSet);
      final ValueSet vs = (ValueSet) entry.getResource();
      assertEquals(LOINC_SANDBOX_VERSION, vs.getVersion());
    }
  }

  /**
   * Intensional filter = expands matching synthetic LOINC properties.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(3)
  public void testEqualsFilterExpandAndRoundTrip() throws Exception {
    postCodeSystem("data/CodeSystem-adhoc-filter-props.json");
    final ValueSet created = postValueSet("data/ValueSet-adhoc-hl7-attachment-eq.json");
    assertEquals("2.83", created.getVersion());
    assertEquals(PublicationStatus.ACTIVE, created.getStatus());
    assertFalse(created.getUseContext().isEmpty());
    assertFalse(created.getIdentifier().isEmpty());
    final ConceptSetComponent include = created.getCompose().getIncludeFirstRep();
    assertEquals(1, include.getFilter().size());
    final ConceptSetFilterComponent filter = include.getFilterFirstRep();
    assertEquals("ValidHL7AttachmentRequest", filter.getProperty());
    assertEquals("=", filter.getOp().toCode());
    assertEquals("Y", filter.getValue());

    final ValueSet expanded = expandByUrl("http://example.org/vs/valid-hl7-attachment-requests");
    assertNotNull(expanded.getExpansion());
    final Set<String> codes = expanded.getExpansion().getContains().stream()
        .map(ValueSetExpansionContainsComponent::getCode).collect(Collectors.toSet());
    assertEquals(Set.of("A-YES"), codes);
    assertFalse(expanded.getCompose().getIncludeFirstRep().getFilter().isEmpty());
  }

  /**
   * Intensional regex filter expands matching properties and preserves draft status.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(4)
  public void testRegexFilterExpandAndDraftStatus() throws Exception {
    postCodeSystem("data/CodeSystem-adhoc-filter-props.json");
    final ValueSet created = postValueSet("data/ValueSet-adhoc-hl7-attachment-regex.json");
    assertEquals("2.83", created.getVersion());
    assertEquals(PublicationStatus.DRAFT, created.getStatus());
    final ConceptSetFilterComponent filter =
        created.getCompose().getIncludeFirstRep().getFilterFirstRep();
    assertEquals("regex", filter.getOp().toCode());

    final ValueSet expanded = expandByUrl("http://example.org/vs/valid-hl7-attachment-responses");
    final Set<String> codes = expanded.getExpansion().getContains().stream()
        .map(ValueSetExpansionContainsComponent::getCode).collect(Collectors.toSet());
    assertEquals(Set.of("A-YES", "A-NO", "A-RANK"), codes);
    assertEquals(PublicationStatus.DRAFT, getValueSet(created.getIdPart()).getStatus());
  }

  /**
   * Post value set.
   *
   * @param classpath the classpath
   * @return the value set
   * @throws Exception the exception
   */
  private ValueSet postValueSet(final String classpath) throws Exception {
    final ResponseEntity<String> response = postFhir(FHIR_VALUESET, readClasspath(classpath));
    assertTrue(response.getStatusCode() == HttpStatus.CREATED
        || response.getStatusCode() == HttpStatus.OK, response.getBody());
    return parser.parseResource(ValueSet.class, response.getBody());
  }

  /**
   * Post code system. Ignore conflict if already loaded.
   *
   * @param classpath the classpath
   * @throws Exception the exception
   */
  private void postCodeSystem(final String classpath) throws Exception {
    final ResponseEntity<String> response = postFhir(FHIR_CODESYSTEM, readClasspath(classpath));
    assertTrue(response.getStatusCode() == HttpStatus.CREATED
        || response.getStatusCode() == HttpStatus.OK
        || response.getStatusCode() == HttpStatus.CONFLICT, response.getBody());
  }

  /**
   * Gets the value set.
   *
   * @param id the id
   * @return the value set
   */
  private ValueSet getValueSet(final String id) {
    final ResponseEntity<String> response =
        restTemplate.getForEntity("http://localhost:" + port + FHIR_VALUESET + "/" + id,
            String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    return parser.parseResource(ValueSet.class, response.getBody());
  }

  /**
   * Expand by url.
   *
   * @param url the url
   * @return the value set
   */
  private ValueSet expandByUrl(final String url) {
    final ResponseEntity<String> response = restTemplate.getForEntity(
        "http://localhost:" + port + FHIR_VALUESET + "/$expand?url={url}&includeDefinition=true",
        String.class, url);
    assertEquals(HttpStatus.OK, response.getStatusCode(), response.getBody());
    return parser.parseResource(ValueSet.class, response.getBody());
  }

  /**
   * Post fhir.
   *
   * @param path the path
   * @param json the json
   * @return the response entity
   */
  private ResponseEntity<String> postFhir(final String path, final String json) {
    final HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/fhir+json");
    return restTemplate.postForEntity("http://localhost:" + port + path,
        new HttpEntity<>(json, headers), String.class);
  }

  /**
   * Read classpath.
   *
   * @param path the path
   * @return the string
   * @throws Exception the exception
   */
  private static String readClasspath(final String path) throws Exception {
    return Files.readString(new ClassPathResource(path).getFile().toPath(), StandardCharsets.UTF_8);
  }

  /**
   * Teardown. POSTed CodeSystems/ValueSets must not leak into later FHIR R4 tests.
   */
  @AfterAll
  public static void teardown() {
    setSetupOnce(false);
  }
}
