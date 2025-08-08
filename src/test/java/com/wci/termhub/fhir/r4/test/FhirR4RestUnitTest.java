/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemHierarchyMeaning;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.ThreadLocalMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * Class tests for FhirR4Tests. Tests the functionality of the FHIR R4
 * endpoints, CodeSystem, ValueSet, and ConceptMap. All passed ids MUST be
 * lowercase, so they match our internally set id's
 */
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class FhirR4RestUnitTest extends AbstractFhirR4ServerTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(FhirR4RestUnitTest.class);

  /** The port. */
  @LocalServerPort
  private int port;

  /** The rest template. */
  @Autowired
  private TestRestTemplate restTemplate;

  /** local host prefix. */
  private static final String LOCALHOST = "http://localhost:";

  /** The fhir metadata. */
  private static final String FHIR_METADATA = "/fhir/r4/metadata";

  /** Fhir url paths. */
  private static final String FHIR_CODESYSTEM = "/fhir/r4/CodeSystem";

  /** The Constant FHIR_CONCEPTMAP. */
  private static final String FHIR_CONCEPTMAP = "/fhir/r4/ConceptMap";

  /** The fhir VS path. */
  private static final String FHIR_VALUESET = "/fhir/r4/ValueSet";

  /** The parser. */
  private static IParser parser;

  /** The object mapper. */
  private ObjectMapper objectMapper = null;

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Sets the up once.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public void setUpOnce() throws Exception {
    parser = FhirContext.forR4().newJsonParser();

  }

  /** Sets the up. */
  @BeforeEach
  public void setUp() {
    // The object mapper
    objectMapper = ThreadLocalMapper.get();
    JacksonTester.initFields(this, objectMapper);
  }

  /**
   * Test code system search.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testMetadata() throws Exception {
    // Arrange
    final String endpoint = LOCALHOST + port + FHIR_METADATA;

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final CapabilityStatement data = parser.parseResource(CapabilityStatement.class, content);

    // Assert
    assertNotNull(data);
    LOGGER.info("  metadata = {}", parser.encodeResourceToString(data));
    assertEquals(ResourceType.CapabilityStatement, data.getResourceType());
    assertNotNull(data.getStatus());
    assertNotNull(data.getDate());
    assertNotNull(data.getFhirVersion());
    assertNotNull(data.getFormat());
    assertFalse(data.getRest().isEmpty());
  }

  /**
   * Test code system search.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testCodeSystemSearch() throws Exception {
    // Arrange
    final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final Bundle data = parser.parseResource(Bundle.class, content);
    final List<Resource> codeSystems =
        data.getEntry().stream().map(BundleEntryComponent::getResource).toList();

    // Assert bundle properties
    assertNotNull(data);
    assertEquals(ResourceType.Bundle, data.getResourceType());
    assertEquals(BundleType.SEARCHSET, data.getType());

    assertEquals(5, data.getTotal());
    assertNotNull(data.getMeta().getLastUpdated());
    assertFalse(data.getLink().isEmpty());
    assertEquals("self", data.getLink().get(0).getRelation());
    assertTrue(data.getLink().get(0).getUrl().endsWith(FHIR_CODESYSTEM));

    // Verify expected code systems
    final Set<String> expectedTitles =
        new HashSet<>(Set.of("ICD10CM", "LNC", "RXNORM", "SNOMEDCT", "SNOMEDCT_US"));
    final Set<String> expectedPublishers = new HashSet<>(Set.of("SANDBOX"));

    // Assert code systems
    assertFalse(codeSystems.isEmpty());
    assertEquals(5, codeSystems.size());

    for (final Resource cs : codeSystems) {
      LOGGER.info("  code system = {}", parser.encodeResourceToString(cs));
      final CodeSystem css = (CodeSystem) cs;

      // Verify required properties
      assertNotNull(css);
      assertEquals(ResourceType.CodeSystem, css.getResourceType());
      assertNotNull(css.getId(), "Id should not be null");
      assertNotNull(css.getVersion(), "Version should not be null");
      assertNotNull(css.getName(), "Name should not be null");
      assertNotNull(css.getTitle(), "Title should not be null");
      assertNotNull(css.getUrl(), "URL should not be null");
      assertEquals(PublicationStatus.ACTIVE, css.getStatus());
      assertFalse(css.getExperimental());
      assertNotNull(css.getDate());
      assertNotNull(css.getPublisher());
      assertEquals(CodeSystemHierarchyMeaning.ISA, css.getHierarchyMeaning());
      assertFalse(css.getCompositional());
      assertNotNull(css.getContent());
      assertTrue(css.getCount() > 0);

      // Verify expected values
      expectedTitles.remove(css.getTitle());
      expectedPublishers.remove(css.getPublisher());
    }

    // Verify all expected values were found
    assertTrue(expectedTitles.isEmpty(), "Missing code systems: " + expectedTitles);
    assertTrue(expectedPublishers.isEmpty(), "Missing publishers: " + expectedPublishers);
  }

  /**
   * Test code system search by url.
   */
  @Test
  @Order(1)
  public void testCodeSystemSearchByUrl() {

    // Arrange
    final String codeSystemUri = "http://snomed.info/sct";
    final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + "?url=" + codeSystemUri;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final Bundle data = parser.parseResource(Bundle.class, content);

    // Assert
    assertNotNull(data);
    assertEquals(ResourceType.Bundle, data.getResourceType());
    assertEquals(Bundle.BundleType.SEARCHSET, data.getType());
    assertEquals(2, data.getTotal());
    assertNotNull(data.getEntry());
    assertEquals(2, data.getEntry().size());
    // log the code systems
    for (final BundleEntryComponent entry : data.getEntry()) {
      final CodeSystem cs = (CodeSystem) entry.getResource();
      LOGGER.info("  code system = {}", parser.encodeResourceToString(cs));
      assertNotNull(cs);
      assertEquals(codeSystemUri, cs.getUrl());
      assertTrue("SNOMEDCT".equals(cs.getTitle()) || "SNOMEDCT_US".equals(cs.getTitle()));
    }

  }

  /**
   * Test retrieving a specific CodeSystem by ID.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testCodeSystemById() throws Exception {
    // Arrange
    final String csId = "340c926f-9ad6-4f1b-b230-dc4ca14575ab";
    final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + "/" + csId;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final CodeSystem codeSystem = parser.parseResource(CodeSystem.class, content);

    // Assert
    assertNotNull(codeSystem);
    LOGGER.info("  code system = {}", parser.encodeResourceToString(codeSystem));

    // Verify resource type and id
    assertEquals(ResourceType.CodeSystem, codeSystem.getResourceType());
    assertEquals("CodeSystem/" + csId, codeSystem.getId());

    // Verify specific field values
    assertEquals("http://snomed.info/sct/731000124108/version/20240301", codeSystem.getVersion());
    assertEquals("Mini version of SNOMEDCT_US For testing purposes", codeSystem.getName());
    assertEquals("SNOMEDCT_US", codeSystem.getTitle());
    assertEquals(PublicationStatus.ACTIVE, codeSystem.getStatus());
    assertFalse(codeSystem.getExperimental());
    assertEquals("2024-03-01T08:00:00Z", codeSystem.getDate().toInstant().toString());
    assertEquals("SANDBOX", codeSystem.getPublisher());
    assertEquals(CodeSystemHierarchyMeaning.ISA, codeSystem.getHierarchyMeaning());
    assertFalse(codeSystem.getCompositional());
    assertEquals("fragment", codeSystem.getContent().toString().toLowerCase());
    assertEquals(456, codeSystem.getCount());
  }

  /**
   * Test CodeSystem _search operation.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testCodeSystemSearchOperation() throws Exception {
    // Arrange
    final String searchParams = "/_search?title=SNOMEDCT&_count=50";
    final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + searchParams;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final Bundle bundle = parser.parseResource(Bundle.class, content);

    // Assert
    assertNotNull(bundle);
    assertEquals(ResourceType.Bundle, bundle.getResourceType());
    assertEquals(org.hl7.fhir.r4.model.Bundle.BundleType.SEARCHSET, bundle.getType());
    assertNotNull(bundle.getMeta().getLastUpdated());
    assertFalse(bundle.getEntry().isEmpty());

    // Verify matching results contain SNOMED
    for (final BundleEntryComponent entry : bundle.getEntry()) {
      final CodeSystem cs = (CodeSystem) entry.getResource();
      LOGGER.info("CodeSystem = {}", parser.setPrettyPrint(true).encodeResourceToString(cs));
      assertTrue(cs.getName().contains("SNOMEDCT") || cs.getTitle().contains("SNOMEDCT"),
          "Search result should contain SNOMEDCT: " + cs.getName());
    }
  }

  /**
   * Test CodeSystem $validate-code operation.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testCodeSystemValidateCode() throws Exception {
    // Arrange
    final String code = "385487005";
    final String url = "http://snomed.info/sct";
    final String version = "http://snomed.info/sct/900000000000207008/version/20240101";
    final String validateParams =
        "/$validate-code?url=" + url + "&code=" + code + "&version=" + version;
    final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + validateParams;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    LOGGER.info("Response content = {}", content);
    final Parameters result = parser.parseResource(Parameters.class, content);

    // Assert
    assertNotNull(result, "Parameters response should not be null");
    LOGGER.info("Parameters = {}", parser.encodeResourceToString(result));

    // Verify result parameter
    assertTrue(result.hasParameter("result"), "Should have result parameter");
    assertEquals("true", result.getParameter("result").getValue().toString(),
        "Result should be true");

    // Verify code parameter
    assertTrue(result.hasParameter("code"), "Should have code parameter");
    assertEquals(code, result.getParameter("code").getValue().toString(),
        "Code should match input");

    // Verify display parameter
    assertTrue(result.hasParameter("display"), "Should have display parameter");
    assertEquals("Surgical procedure on thorax",
        result.getParameter("display").getValue().toString(),
        "Display should match expected value");

    // Verify active parameter
    assertTrue(result.hasParameter("active"), "Should have active parameter");
    assertTrue(result.getParameterBool("active"), "Active should be true");

    // Verify system parameter
    assertTrue(result.hasParameter("system"), "Should have system parameter");
    assertEquals(url, result.getParameter("system").getValue().toString(),
        "System should match url");

    // Verify version parameter
    assertTrue(result.hasParameter("version"), "Should have version parameter");
    assertEquals(version, result.getParameter("version").getValue().toString(),
        "Version should match expected value");
  }

  /**
   * Test CodeSystem $validate-code operation with specific ID.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testCodeSystemValidateCodeById() throws Exception {
    // Arrange
    final String csId = "177f2263-fe04-4f1f-b0e6-9b351ab8baa9";
    final String code = "E10";
    final String validateParams = "/$validate-code?code=" + code;
    final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + "/" + csId + validateParams;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final Parameters result = parser.parseResource(Parameters.class, content);

    // Assert
    assertNotNull(result);
    LOGGER.info("Parameters = {}", parser.encodeResourceToString(result));
    assertEquals("true", result.getParameter("result").getValue().toString());
    assertEquals(code, result.getParameter("code").getValue().toString());
    assertNotNull(result.getParameter("display"));
    assertNotNull(result.getParameter("active"));
    assertNotNull(result.getParameter("version"));
  }

  /**
   * Test CodeSystem $subsumes operation.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testCodeSystemSubsumes() throws Exception {
    // Arrange
    final String codeA = "73211009";
    final String codeB = "727499001";
    final String system = "http://snomed.info/sct";
    final String version = "http://snomed.info/sct/731000124108/version/20240301";
    final String subsumesParams = "/$subsumes?codeA=" + codeA + "&codeB=" + codeB + "&system="
        + system + "&version=" + version;
    final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + subsumesParams;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final Parameters result = parser.parseResource(Parameters.class, content);

    // Assert
    assertNotNull(result, "Parameters response should not be null");
    LOGGER.info("Parameters = {}", parser.encodeResourceToString(result));

    // Verify outcome parameter
    assertNotNull(result.getParameter("outcome"));
    assertTrue(result.getParameter("outcome").getValue().toString()
        .matches("(subsumes|subsumed-by|equivalent|not-subsumed)"));
    assertNotNull(result.getParameter("version"));
  }

  /**
   * Test CodeSystem $subsumes operation with specific ID.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testCodeSystemSubsumesById() throws Exception {
    // Arrange
    final String csId = "3e8e4d7c-7d3a-4682-a1e4-c5db5bc33d4b";
    final String codeA = "73211009";
    final String codeB = "727499001";
    final String subsumesParams = "/$subsumes?codeA=" + codeA + "&codeB=" + codeB;
    final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + "/" + csId + subsumesParams;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final Parameters result = parser.parseResource(Parameters.class, content);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getParameter("outcome"));
    assertTrue(result.getParameter("outcome").getValue().toString()
        .matches("(subsumes|subsumed-by|equivalent|not-subsumed)"));
    assertNotNull(result.getParameter("version"));
  }

  /**
   * Test CodeSystem $lookup operation.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testCodeSystemLookup() throws Exception {
    // Arrange
    final String code = "73211009";
    final String csId = "http://snomed.info/sct";
    final String version = "http://snomed.info/sct/900000000000207008/version/20240101";
    final String lookupParams = "/$lookup?code=" + code + "&system=" + csId + "&version=" + version;
    final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + lookupParams;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final Parameters result = parser.parseResource(Parameters.class, content);

    // Assert
    assertNotNull(result, "Parameters response should not be null");
    LOGGER.info("Parameters = {}", parser.encodeResourceToString(result));

    // Verify code parameter
    assertTrue(result.hasParameter("code"), "Should have code parameter");
    assertEquals(code, result.getParameter("code").getValue().toString());

    // Verify system parameter
    assertTrue(result.hasParameter("system"), "Should have system parameter");
    assertEquals("http://snomed.info/sct", result.getParameter("system").getValue().toString());

    // Verify name parameter
    assertTrue(result.hasParameter("name"), "Should have name parameter");
    assertEquals("SNOMEDCT", result.getParameter("name").getValue().toString());

    // Verify version parameter
    assertTrue(result.hasParameter("version"), "Should have version parameter");
    assertEquals(version, result.getParameter("version").getValue().toString());

    // Verify display parameter
    assertTrue(result.hasParameter("display"), "Should have display parameter");
    assertEquals("Diabetes mellitus", result.getParameter("display").getValue().toString());

    // Verify active parameter
    assertTrue(result.hasParameter("active"), "Should have active parameter");
    assertTrue(result.getParameterBool("active"));

    // Verify sufficientlyDefined parameter
    assertTrue(result.hasParameter("sufficientlyDefined"),
        "Should have sufficientlyDefined parameter");
    // assertFalse(result.getParameterBool("sufficientlyDefined"));

    // Verify property parameters
    final List<Parameters.ParametersParameterComponent> properties =
        result.getParameter().stream().filter(p -> "property".equals(p.getName())).toList();
    assertFalse(properties.isEmpty(), "Should have properties");

    // Verify effectiveTime property
    // TODO: re-enable when effectiveTime is added
    // final boolean hasEffectiveTime = properties.stream()
    // .anyMatch(p -> p.getPart().stream()
    // .anyMatch(part -> "effectiveTime".equals(part.getValue().toString())
    // && p.getPart().get(1).getValue().toString().equals("20020131")));
    // assertTrue(hasEffectiveTime, "Should have effectiveTime property with
    // value 20020131");

    // Verify normalForm parameter
    // TODO: re-enable when normalForm is added
    // assertTrue(result.hasParameter("normalForm"), "Should have normalForm
    // parameter");
    // assertTrue(result.getParameter("normalForm").getValue().toString()
    // .contains("<<< 126877002|Disorder of glucose metabolism|"));

    // Verify designation parameters
    final List<Parameters.ParametersParameterComponent> designations =
        result.getParameter().stream().filter(p -> "designation".equals(p.getName())).toList();
    assertFalse(designations.isEmpty(), "Should have designations");

    // // Verify has fully specified name designation
    // boolean hasFullySpecifiedName = designations.stream().anyMatch(d ->
    // d.getPart().stream()
    // .anyMatch(p -> p.getName().equals("use") &&
    // p.getValue().toString().contains("900000000000003001")));
    // assertTrue(hasFullySpecifiedName, "Should have fully specified name
    // designation");
  }

  /**
   * Test CodeSystem $lookup operation with specific ID.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testCodeSystemLookupById() throws Exception {
    // Arrange
    final String system = "3e8e4d7c-7d3a-4682-a1e4-c5db5bc33d4b";
    final String code = "73211009";
    final String lookupParams = "/$lookup?code=" + code;
    final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + "/" + system + lookupParams;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final Parameters result = parser.parseResource(Parameters.class, content);

    // Assert
    assertNotNull(result, "Parameters response should not be null");
    LOGGER.info("Parameters = {}", parser.encodeResourceToString(result));

    // Verify code parameter
    assertTrue(result.hasParameter("code"), "Should have code parameter");
    assertEquals(code, result.getParameter("code").getValue().toString());

    // Verify system parameter
    assertTrue(result.hasParameter("system"), "Should have system parameter");
    assertEquals("http://snomed.info/sct", result.getParameter("system").getValue().toString());

    // Verify name parameter
    assertTrue(result.hasParameter("name"), "Should have name parameter");
    assertEquals("SNOMEDCT", result.getParameter("name").getValue().toString());

    // Verify version parameter
    assertTrue(result.hasParameter("version"), "Should have version parameter");
    assertEquals("http://snomed.info/sct/900000000000207008/version/20240101",
        result.getParameter("version").getValue().toString());

    // Verify display parameter
    assertTrue(result.hasParameter("display"), "Should have display parameter");
    assertEquals("Diabetes mellitus", result.getParameter("display").getValue().toString());

    // Verify active parameter
    assertTrue(result.hasParameter("active"), "Should have active parameter");
    assertTrue(result.getParameterBool("active"));

    // Verify sufficientlyDefined parameter
    assertTrue(result.hasParameter("sufficientlyDefined"),
        "Should have sufficientlyDefined parameter");
    // assertFalse(result.getParameterBool("sufficientlyDefined"));

    // Verify property parameters
    final List<Parameters.ParametersParameterComponent> properties =
        result.getParameter().stream().filter(p -> "property".equals(p.getName())).toList();
    assertFalse(properties.isEmpty(), "Should have properties");

    // Verify effectiveTime property is added
    // TODO: re-enable when effectiveTime
    // final boolean hasEffectiveTime = properties.stream()
    // .anyMatch(p -> p.getPart().stream()
    // .anyMatch(part -> "effectiveTime".equals(part.getValue().toString())
    // && p.getPart().get(1).getValue().toString().equals("20020131")));
    // assertTrue(hasEffectiveTime, "Should have effectiveTime property with
    // value 20020131");

    // Verify normalForm parameter
    // assertTrue(result.hasParameter("normalForm"), "Should have normalForm
    // parameter");
    // assertTrue(result.getParameter("normalForm").getValue().toString()
    // .contains("<<< 126877002|Disorder of glucose metabolism|"));

    // Verify designation parameters
    final List<Parameters.ParametersParameterComponent> designations =
        result.getParameter().stream().filter(p -> "designation".equals(p.getName())).toList();
    assertFalse(designations.isEmpty(), "Should have designations");

    // Verify has fully specified name designation
    // final boolean hasFullySpecifiedName = designations.stream()
    // .anyMatch(d -> d.getPart().stream().anyMatch(p ->
    // p.getName().equals("use")
    // && p.getValue().toString().contains("900000000000003001")));
    // assertTrue(hasFullySpecifiedName, "Should have fully specified name
    // designation");
  }

  /**
   * Test ValueSet search.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testValueSetSearch() throws Exception {
    // Arrange
    final String endpoint = LOCALHOST + port + FHIR_VALUESET;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final Bundle bundle = parser.parseResource(Bundle.class, content);

    // Assert bundle properties
    assertNotNull(bundle);
    assertEquals(ResourceType.Bundle, bundle.getResourceType());
    assertEquals(org.hl7.fhir.r4.model.Bundle.BundleType.SEARCHSET, bundle.getType());
    assertNotNull(bundle.getMeta().getLastUpdated());
    assertFalse(bundle.getEntry().isEmpty());

    // Verify each ValueSet
    for (final BundleEntryComponent entry : bundle.getEntry()) {
      final ValueSet valueSet = (ValueSet) entry.getResource();
      assertNotNull(valueSet);
      assertEquals(ResourceType.ValueSet, valueSet.getResourceType());
      assertNotNull(valueSet.getId());
      assertNotNull(valueSet.getVersion());
      assertNotNull(valueSet.getName());
      assertNotNull(valueSet.getTitle());
      assertEquals(PublicationStatus.ACTIVE, valueSet.getStatus());
      assertNotNull(valueSet.getPublisher());
    }
  }

  /**
   * Test retrieving a specific ValueSet by ID.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testValueSetById() throws Exception {
    // Arrange
    final String vsId = "3e8e4d7c-7d3a-4682-a1e4-c5db5bc33d4b_entire";
    final String endpoint = LOCALHOST + port + FHIR_VALUESET + "/" + vsId;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final ValueSet valueSet = parser.parseResource(ValueSet.class, content);

    // Assert
    assertNotNull(valueSet);
    assertEquals(ResourceType.ValueSet, valueSet.getResourceType());
    assertEquals("ValueSet/" + vsId, valueSet.getId());
    assertNotNull(valueSet.getUrl());
    assertNotNull(valueSet.getVersion());
    assertNotNull(valueSet.getName());
    assertNotNull(valueSet.getTitle());
    assertEquals(PublicationStatus.ACTIVE, valueSet.getStatus());
    assertNotNull(valueSet.getDate());
    assertNotNull(valueSet.getPublisher());
    assertNotNull(valueSet.getDescription());
  }

  /**
   * Test ValueSet _search operation.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testValueSetSearchOperation() throws Exception {
    // Arrange
    final String searchParams = "/_search?title=SNOMEDCT&_count=50";
    final String endpoint = LOCALHOST + port + FHIR_VALUESET + searchParams;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final Bundle bundle = parser.parseResource(Bundle.class, content);

    // Assert
    assertNotNull(bundle);
    assertEquals(ResourceType.Bundle, bundle.getResourceType());
    assertEquals(org.hl7.fhir.r4.model.Bundle.BundleType.SEARCHSET, bundle.getType());
    assertNotNull(bundle.getMeta().getLastUpdated());
    assertFalse(bundle.getEntry().isEmpty());

    // Verify matching results contain SNOMED
    for (final BundleEntryComponent entry : bundle.getEntry()) {
      final ValueSet vs = (ValueSet) entry.getResource();
      assertTrue(vs.getName().contains("SNOMED") || vs.getTitle().contains("SNOMED"),
          "Search result should contain SNOMED: " + vs.getName());
    }
  }

  /**
   * Test value set read.
   *
   * @throws Exception exception
   */
  @Test
  @Order(1)
  public void testValueSetRead() throws Exception {
    // Arrange
    final String endpoint = LOCALHOST + port + FHIR_VALUESET;
    LOGGER.info("endpoint = {}", endpoint);

    String content = this.restTemplate.getForObject(endpoint, String.class);
    final Bundle data = parser.parseResource(Bundle.class, content);
    final List<Resource> valueSets =
        data.getEntry().stream().map(Bundle.BundleEntryComponent::getResource).toList();

    // Assert bundle has expected number of entries
    final int expectedCount = CODE_SYSTEM_FILES.size() + VALUE_SET_FILES.size();
    assertEquals(expectedCount, valueSets.size(),
        "Should have " + expectedCount + " ValueSet entries, found " + valueSets.size());

    // Test each ValueSet entry
    for (final Resource resource : valueSets) {
      // Get the ValueSet ID
      final String valueSetId = resource.getIdPart();

      // Get the individual ValueSet
      content = this.restTemplate.getForObject(endpoint + "/" + valueSetId, String.class);
      final ValueSet valueSet = parser.parseResource(ValueSet.class, content);

      // Assert common properties
      assertNotNull(valueSet, "ValueSet should not be null");
      assertEquals(ResourceType.ValueSet, valueSet.getResourceType(),
          "Resource type should be ValueSet");
      assertEquals(valueSetId, valueSet.getIdPart(), "IDs should match");

      // Compare with original bundle entry
      final ValueSet originalVs = (ValueSet) resource;
      assertEquals(originalVs.getUrl(), valueSet.getUrl(), "URLs should match");
      assertEquals(originalVs.getName(), valueSet.getName(), "Names should match");
      assertEquals(originalVs.getTitle(), valueSet.getTitle(), "Titles should match");
      assertEquals(originalVs.getVersion(), valueSet.getVersion(), "Versions should match");
      assertEquals(originalVs.getPublisher(), valueSet.getPublisher(), "Publishers should match");
      assertEquals(originalVs.getDescription(), valueSet.getDescription(),
          "Descriptions should match");
      assertEquals(originalVs.getStatus(), valueSet.getStatus(), "Status should match");
    }
  }

  /**
   * Test ValueSet $validate-code operation.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testValueSetValidateCode() throws Exception {
    // Arrange
    final String code = "73211009";
    final String url = "2023?fhir_vs";
    final String validateParams = "/$validate-code?url=" + url + "&code=" + code;
    final String endpoint = LOCALHOST + port + FHIR_VALUESET + validateParams;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final Parameters result = parser.parseResource(Parameters.class, content);

    // Assert
    assertNotNull(result, "Parameters response should not be null");
    LOGGER.info("Parameters = {}", parser.encodeResourceToString(result));

    // Verify result parameter
    assertTrue(result.hasParameter("result"), "Should have result parameter");
    assertTrue(result.getParameterBool("result"), "Result should be true");

    // Verify display parameter
    assertTrue(result.hasParameter("display"), "Should have display parameter");
    assertEquals("Diabetes mellitus", result.getParameter("display").getValue().toString(),
        "Display should match expected value");
  }

  /**
   * Test ValueSet $validate-code operation with specific ID.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testValueSetValidateCodeById() throws Exception {
    // Arrange
    final String vsId = "3e8e4d7c-7d3a-4682-a1e4-c5db5bc33d4b_entire";
    final String code = "73211009";
    final String validateParams = "/$validate-code?code=" + code;
    final String endpoint = LOCALHOST + port + FHIR_VALUESET + "/" + vsId + validateParams;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final Parameters result = parser.parseResource(Parameters.class, content);

    // Assert
    assertNotNull(result, "Parameters response should not be null");
    LOGGER.info("Parameters = {}", parser.encodeResourceToString(result));

    // Verify result parameter
    assertTrue(result.hasParameter("result"), "Should have result parameter");
    assertTrue(result.getParameterBool("result"), "Result should be true");

    // Verify display parameter
    assertTrue(result.hasParameter("display"), "Should have display parameter");
    assertEquals("Diabetes mellitus", result.getParameter("display").getValue().toString(),
        "Display should match expected value");
  }

  /**
   * Test ValueSet $expand operation.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testValueSetExpand() throws Exception {

    // Arrange
    final String expandParams =
        "/$expand?url=http://www.nlm.nih.gov/research/umls/rxnorm?fhir_vs&count=50";
    final String endpoint = LOCALHOST + port + FHIR_VALUESET + expandParams;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final ValueSet valueSet = parser.parseResource(ValueSet.class, content);

    // Assert
    assertNotNull(valueSet, "ValueSet should not be null");
    assertEquals(ResourceType.ValueSet, valueSet.getResourceType(),
        "Resource type should be ValueSet");

    // Verify expansion
    assertNotNull(valueSet.getExpansion(), "Expansion should not be null");
    assertNotNull(valueSet.getExpansion().getId(), "Expansion ID should not be null");
    assertNotNull(valueSet.getExpansion().getTimestamp(), "Expansion timestamp should not be null");
    assertEquals(816, valueSet.getExpansion().getTotal());
    assertEquals(0, valueSet.getExpansion().getOffset(), "Offset should be 0");

    // Verify expansion contains
    assertNotNull(valueSet.getExpansion().getContains(), "Contains should not be null");
    assertFalse(valueSet.getExpansion().getContains().isEmpty(), "Contains should not be empty");

    // Verify first entry has required fields
    final ValueSet.ValueSetExpansionContainsComponent firstEntry =
        valueSet.getExpansion().getContains().get(0);
    assertNotNull(firstEntry.getCode(), "First entry code should not be null");
    assertNotNull(firstEntry.getDisplay(), "First entry display should not be null");
  }

  /**
   * Test ValueSet $expand operation with specific ID.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testValueSetExpandById() throws Exception {
    // Arrange
    final String vsId = "3e8e4d7c-7d3a-4682-a1e4-c5db5bc33d4b_entire";
    final int count = 50;
    final String expandParams = "/$expand?count=" + count;
    final String endpoint = LOCALHOST + port + FHIR_VALUESET + "/" + vsId + expandParams;
    LOGGER.info("endpoint = {}", endpoint);

    // Act
    final String content = this.restTemplate.getForObject(endpoint, String.class);
    final ValueSet valueSet = parser.parseResource(ValueSet.class, content);

    // Assert
    assertNotNull(valueSet, "ValueSet should not be null");
    assertEquals(ResourceType.ValueSet, valueSet.getResourceType(),
        "Resource type should be ValueSet");
    assertEquals(vsId, valueSet.getIdPart(), "ValueSet ID should match");

    // Verify ValueSet metadata
    assertEquals("SNOMEDCT-ENTIRE", valueSet.getTitle(), "Title should match");
    assertEquals("VS Mini version of SNOMEDCT For testing purposes", valueSet.getName(),
        "Name should match");
    assertEquals("http://snomed.info/sct/900000000000207008/version/20240101",
        valueSet.getVersion(), "Version should match");
    assertEquals(PublicationStatus.ACTIVE, valueSet.getStatus(), "Status should be active");
    assertEquals("SANDBOX", valueSet.getPublisher(), "Publisher should match");

    // Verify expansion
    assertNotNull(valueSet.getExpansion(), "Expansion should not be null");
    assertNotNull(valueSet.getExpansion().getId(), "Expansion ID should not be null");
    assertNotNull(valueSet.getExpansion().getTimestamp(), "Expansion timestamp should not be null");

    // The total should represent all available concepts, not the count
    // parameter
    assertEquals(434, valueSet.getExpansion().getTotal(),
        "Total should be 434 but is " + valueSet.getExpansion().getTotal());
    assertEquals(0, valueSet.getExpansion().getOffset(), "Offset should be 0");

    // Verify expansion contains - should have at most 'count' items
    assertNotNull(valueSet.getExpansion().getContains(), "Contains should not be null");
    assertFalse(valueSet.getExpansion().getContains().isEmpty(), "Contains should not be empty");
    assertTrue(valueSet.getExpansion().getContains().size() <= count,
        "Contains size should be <= count: " + valueSet.getExpansion().getContains().size());

    // Verify first entry in contains
    final ValueSet.ValueSetExpansionContainsComponent firstEntry =
        valueSet.getExpansion().getContains().get(0);
    assertNotNull(firstEntry.getCode(), "First entry code should not be null");
    assertNotNull(firstEntry.getDisplay(), "First entry display should not be null");
    // assertEquals("1007411", firstEntry.getCode(), "First entry code should
    // match");
    // assertEquals("chlorpropamide / metformin", firstEntry.getDisplay(),
    // "First entry display should match");
  }

  /**
   * Test delete code system.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(10)
  public void testDeleteNonExistentCodeSystem() throws Exception {

    LOGGER.info("Testing delete NonExistent CodeSystem endpoint");
    final String testId = "test-codesystem-delete";
    final String deleteUrl = LOCALHOST + port + FHIR_CODESYSTEM + "/" + testId;
    final ResponseEntity<String> response =
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    LOGGER.info("Delete non-existent CodeSystem returned 404 as expected");
  }

  /**
   * Test delete value set.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(11)
  public void testDeleteNonExistentValueSet() throws Exception {

    LOGGER.info("Testing delete NonExistent ValueSet endpoint");
    final String testId = "test-valueset-delete";
    final String deleteUrl = LOCALHOST + port + FHIR_VALUESET + "/" + testId;
    final ResponseEntity<String> response =
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    LOGGER.info("Delete non-existent ValueSet returned 404 as expected");
  }

  /**
   * Test delete concept map.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(12)
  public void testDeleteNonExistentConceptMap() throws Exception {

    LOGGER.info("Testing delete NonExistent ConceptMap endpoint");
    final String testId = "test-conceptmap-delete";
    final String deleteUrl = LOCALHOST + port + FHIR_CONCEPTMAP + "/" + testId;
    final ResponseEntity<String> response =
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    LOGGER.info("Delete non-existent ConceptMap returned 404 as expected");
  }

  /**
   * Test delete implicit value set (should not be allowed).
   *
   * @throws Exception the exception
   */
  @Test
  @Order(13)
  public void testDeleteImplicitValueSet() throws Exception {

    LOGGER.info("Testing delete implicit ValueSet (should not be allowed)");

    // Get Id of loaded terminology
    String id = "";
    final SearchParameters params = new SearchParameters();
    params.setQuery("*:*");
    params.setLimit(100);

    // list terminologies to verify the LNC terminology is loaded
    final ResultList<Terminology> terminologies = searchService.find(params, Terminology.class);
    for (final Terminology t : terminologies.getItems()) {
      LOGGER.info("Terminology: {} - {}", t.getName(), t.getVersion());
      id = t.getId();
    }

    // Try to delete an implicit value set -
    // Should return 405 Method Not Allowed
    final String testId = id + "_entire";
    final String deleteUrl = LOCALHOST + port + FHIR_VALUESET + "/" + testId;
    final ResponseEntity<String> response =
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);

    // Should return 405 Method Not Allowed for implicit value sets
    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    LOGGER.info("Delete implicit ValueSet returned 405 as expected");
  }

  /**
   * Test delete code system.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(14)
  public void testDeleteCodeSystem() throws Exception {
    LOGGER.info("Testing delete CodeSystem endpoint");

    // Get Id of loaded terminology
    String testId = "";
    final SearchParameters params = new SearchParameters();
    params.setQuery("*:*");
    params.setLimit(100);

    ResultList<Terminology> terminologies = searchService.find(params, Terminology.class);
    for (final Terminology t : terminologies.getItems()) {
      LOGGER.info("Terminology: {} - {}", t.getName(), t.getVersion());
      testId = t.getId();
    }

    assertNotNull(testId);

    // Try to delete the code system
    final String deleteUrl = LOCALHOST + port + FHIR_CODESYSTEM + "/" + testId;
    LOGGER.info("Delete codes system URL {}", deleteUrl);
    final ResponseEntity<String> response =
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    // Verify the code system is deleted by attempting to delete it again
    final ResponseEntity<String> response2 =
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
    terminologies = searchService.find(params, Terminology.class);

    assertNotNull(terminologies);
    // assert testId not in terminologies
    final String finalTestId = testId;
    assertTrue(terminologies.getItems().stream().noneMatch(t -> t.getId().equals(finalTestId)),
        "Deleted terminology should not be found");
  }

  /**
   * Test delete value set.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(15)
  public void testDeleteValueSet() throws Exception {
    LOGGER.info("Testing delete ValueSet endpoint");

    // Get Id of loaded terminology
    String testId = "";
    final SearchParameters params = new SearchParameters();
    params.setQuery("*:*");
    params.setLimit(100);

    ResultList<Subset> subsets = searchService.find(params, Subset.class);
    for (final Subset s : subsets.getItems()) {
      LOGGER.info("Subset: {} - {}", s.getName(), s.getVersion());
      if ("Lateralizable body structure reference set".equals(s.getName())) {
        testId = s.getId();
      }
    }

    assertNotNull(testId);

    // Try to delete the value set
    final String deleteUrl = LOCALHOST + port + FHIR_VALUESET + "/" + testId;
    LOGGER.info("Delete value set URL {}", deleteUrl);
    final ResponseEntity<String> response =
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    // Verify the value set is deleted by attempting to delete it again
    final ResponseEntity<String> response2 =
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
    subsets = searchService.find(params, Subset.class);
    assertEquals(1, subsets.getTotal());
  }

  /**
   * Test delete concept map.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(16)
  public void testDeleteConceptMap() throws Exception {
    LOGGER.info("Testing delete ConceptMap endpoint");

    // Get Id of loaded terminology
    String testId = "";
    final SearchParameters params = new SearchParameters();
    params.setQuery("*:*");
    params.setLimit(100);

    ResultList<Mapset> mapsets = searchService.find(params, Mapset.class);
    for (final Mapset m : mapsets.getItems()) {
      LOGGER.info("Terminology: {} - {}", m.getName(), m.getVersion());
      testId = m.getId();
    }

    assertNotNull(testId);

    // Try to delete the concept map
    final String deleteUrl = LOCALHOST + port + FHIR_CONCEPTMAP + "/" + testId;
    LOGGER.info("Delete concept map URL {}", deleteUrl);
    final ResponseEntity<String> response =
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    // Verify the concept map is deleted by attempting to delete it again
    final ResponseEntity<String> response2 =
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
    mapsets = searchService.find(params, Mapset.class);
    assertEquals(0, mapsets.getTotal());
  }

}
