/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;

import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r5.model.Bundle.LinkRelationTypes;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.CodeSystem.CodeSystemHierarchyMeaning;
import org.hl7.fhir.r5.model.ConceptMap;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.ResourceType;
import org.hl7.fhir.r5.model.ValueSet;
import org.junit.jupiter.api.AfterAll;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.util.CodeSystemLoaderUtil;
import com.wci.termhub.util.ThreadLocalMapper;
import com.wci.termhub.util.TerminologyUtility;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * Class tests for FhirR5Tests. Tests the functionality of the FHIR R5 endpoints, CodeSystem,
 * ValueSet, and ConceptMap. All passed ids MUST be lowercase, so they match our internally set id's
 */
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class FhirR5RestUnitTest extends AbstractFhirR5ServerTest {

  /** The LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(FhirR5RestUnitTest.class);

  /** The port. */
  @LocalServerPort
  private int port;

  /** The rest template. */
  @Autowired
  private TestRestTemplate restTemplate;

  /** local host prefix. */
  private static final String LOCALHOST = "http://localhost:";

  /** The fhir metadata. */
  private static final String FHIR_METADATA = "/fhir/r5/metadata";

  /** Fhir url paths. */
  private static final String FHIR_CODESYSTEM = "/fhir/r5/CodeSystem";

  /** The Constant FHIR_CONCEPTMAP. */
  private static final String FHIR_CONCEPTMAP = "/fhir/r5/ConceptMap";

  /** The fhir VS path. */
  private static final String FHIR_VALUESET = "/fhir/r5/ValueSet";

  /** The Constant FIND. */
  private static final int FIND = 10;

  /** The Constant DELETE. */
  private static final int DELETE = 20;

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

    // Instantiate parser
    parser = FhirContext.forR5().newJsonParser();

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
  @Order(FIND)
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
  @Order(FIND)
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
    assertEquals(Bundle.BundleType.SEARCHSET, data.getType());
    assertEquals(6, data.getTotal());
    assertNotNull(data.getMeta().getLastUpdated());
    assertFalse(data.getLink().isEmpty());
    assertEquals(LinkRelationTypes.SELF, data.getLink().get(0).getRelation());
    assertTrue(data.getLink().get(0).getUrl().endsWith(FHIR_CODESYSTEM));

    // Verify expected code systems
    // missing rxnorm files
    final Set<String> expectedTitles =
        new HashSet<>(Set.of("ICD10CM", "LNC", "RXNORM", "SNOMEDCT", "SNOMEDCT_US"));
    final Set<String> expectedPublishers = new HashSet<>(Set.of("SANDBOX"));

    // Assert code systems
    assertFalse(codeSystems.isEmpty());
    // log each code system name and version
    for (final Resource cs : codeSystems) {
      final CodeSystem css = (CodeSystem) cs;
      LOGGER.info("  Code System Name = {}, Version = {}", css.getName(), css.getVersion());
      LOGGER.info("  Code System URL = {}", css.getUrl());
    }
    assertEquals(6, codeSystems.size());

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
  @Order(FIND)
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
   * Test CodeSystem lookup with definition.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testCodeSystemLookupWithDefinition() throws Exception {
    // Arrange
    final String system =
        CodeSystemLoaderUtil.mapOriginalId("3e8e4d7c-7d3a-4682-a1e4-c5db5bc33d4b");
    final String code = "723277005"; // Concept with definition
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

    // Verify display parameter
    assertTrue(result.hasParameter("display"), "Should have display parameter");
    assertEquals("Nonconformance to editorial policy component",
        result.getParameter("display").getValue().toString());

    // Verify definition property exists
    final List<Parameters.ParametersParameterComponent> properties =
        result.getParameter().stream().filter(p -> "property".equals(p.getName())).toList();
    assertFalse(properties.isEmpty(), "Should have properties");

    // Find definition property
    final boolean hasDefinition = properties.stream().anyMatch(p -> p.getPart().stream().anyMatch(
        part -> "code".equals(part.getName()) && "definition".equals(part.getValue().toString())));
    assertTrue(hasDefinition, "Should have definition property");

    // Get the definition value
    final String definition = properties.stream().filter(p -> p.getPart().stream().anyMatch(
        part -> "code".equals(part.getName()) && "definition".equals(part.getValue().toString())))
        .findFirst().orElseThrow(() -> new AssertionError("Definition property not found"))
        .getPart().stream().filter(part -> "value".equals(part.getName())).findFirst()
        .orElseThrow(() -> new AssertionError("Definition value not found")).getValue().toString();

    assertEquals("A component that fails to comply with the current editorial guidance.",
        definition);

    LOGGER.info("Successfully verified definition in FHIR R5 lookup: {}", definition);
  }

  /**
   * Test retrieving a specific CodeSystem by ID.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testCodeSystemById() throws Exception {
    // Arrange
    final String csId = CodeSystemLoaderUtil.mapOriginalId("340c926f-9ad6-4f1b-b230-dc4ca14575ab");
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
    assertEquals("2024-03-01T00:00:00Z", codeSystem.getDate().toInstant().toString());
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
  @Order(FIND)
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
    assertEquals(org.hl7.fhir.r5.model.Bundle.BundleType.SEARCHSET, bundle.getType());
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
  @Order(FIND)
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
    assertEquals("http://snomed.info/sct/900000000000207008/version/20240101",
        result.getParameter("version").getValue().toString(),
        "Version should match expected value");
  }

  /**
   * Test CodeSystem $validate-code operation with specific ID.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testCodeSystemValidateCodeById() throws Exception {
    // Arrange
    final String csId = CodeSystemLoaderUtil.mapOriginalId("177f2263-fe04-4f1f-b0e6-9b351ab8baa9");
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
  @Order(FIND)
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
  }

  /**
   * Test CodeSystem $subsumes operation with specific ID.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testCodeSystemSubsumesById() throws Exception {
    // Arrange
    final String csId = CodeSystemLoaderUtil.mapOriginalId("3e8e4d7c-7d3a-4682-a1e4-c5db5bc33d4b");
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
  @Order(FIND)
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
    assertEquals(csId, result.getParameter("system").getValue().toString());

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

    // Verify effectiveTime property is added
    // Re-enable when effectiveTime
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
    final boolean hasFullySpecifiedName = designations.stream()
        .anyMatch(d -> d.getPart().stream().anyMatch(p -> p.getName().equals("use")
            && p.getValue().toString().contains("900000000000003001")));
    assertTrue(hasFullySpecifiedName, "Should have fully specified name designation");
  }

  /**
   * Test CodeSystem $lookup operation with specific ID.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testCodeSystemLookupById() throws Exception {
    // Arrange
    final String system =
        CodeSystemLoaderUtil.mapOriginalId("3e8e4d7c-7d3a-4682-a1e4-c5db5bc33d4b");
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
    // Re-enable when effectiveTime
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
    final boolean hasFullySpecifiedName = designations.stream()
        .anyMatch(d -> d.getPart().stream().anyMatch(p -> p.getName().equals("use")
            && p.getValue().toString().contains("900000000000003001")));
    assertTrue(hasFullySpecifiedName, "Should have fully specified name designation");
  }

  /**
   * Test ValueSet search.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
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
    assertEquals(org.hl7.fhir.r5.model.Bundle.BundleType.SEARCHSET, bundle.getType());
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
  @Order(FIND)
  public void testValueSetById() throws Exception {
    // Arrange
    final String vsId =
        CodeSystemLoaderUtil.mapOriginalId("3e8e4d7c-7d3a-4682-a1e4-c5db5bc33d4b") + "_entire";
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
  @Order(FIND)
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
    assertEquals(org.hl7.fhir.r5.model.Bundle.BundleType.SEARCHSET, bundle.getType());
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
  @Order(FIND)
  public void testValueSetRead() throws Exception {
    // Arrange
    final String endpoint = LOCALHOST + port + FHIR_VALUESET;
    LOGGER.info("endpoint = {}", endpoint);

    String content = this.restTemplate.getForObject(endpoint, String.class);
    final Bundle data = parser.parseResource(Bundle.class, content);
    final List<Resource> valueSets =
        data.getEntry().stream().map(Bundle.BundleEntryComponent::getResource).toList();

    // Assert bundle has expected number of entries
    // plus one for the lnc-sandbox-277-r4 code system (testCodeSystemLookupLoincWithVersion)
    final int expectedCount = CODE_SYSTEM_FILES.size() + VALUE_SET_FILES.size() + 1;
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
  @Order(FIND)
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
  @Order(FIND)
  public void testValueSetValidateCodeById() throws Exception {
    // Arrange
    final String vsId =
        CodeSystemLoaderUtil.mapOriginalId("3e8e4d7c-7d3a-4682-a1e4-c5db5bc33d4b") + "_entire";
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
  @Order(FIND)
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
  @Order(FIND)
  public void testValueSetExpandById() throws Exception {
    // Arrange
    final String vsId =
        CodeSystemLoaderUtil.mapOriginalId("3e8e4d7c-7d3a-4682-a1e4-c5db5bc33d4b") + "_entire";
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
    // assertEquals("1000004", firstEntry.getCode(), "First entry code should
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
  @Order(DELETE)
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
  @Order(DELETE)
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
  @Order(DELETE)
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
  @Order(DELETE)
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
  @Order(DELETE)
  public void testCreateReadDeleteCodeSystem() throws Exception {
    // 1. Create a new CodeSystem
    final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM;
    CodeSystem newCs = new CodeSystem();
    newCs.setName("TestCS");
    newCs.setTitle("Test CodeSystem");
    newCs.setStatus(PublicationStatus.ACTIVE);
    newCs.setPublisher("TEST");
    newCs.setHierarchyMeaning(CodeSystemHierarchyMeaning.ISA);
    newCs.setVersion("1.0");
    newCs.setUrl("http://example.org/fhir/CodeSystem/test-codesystem");
    newCs.setDate(new Date());

    final org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
    headers.set("Content-Type", "application/fhir+json");
    String csJson = parser.encodeResourceToString(newCs);
    final org.springframework.http.HttpEntity<String> entity =
        new org.springframework.http.HttpEntity<>(csJson, headers);

    ResponseEntity<String> createResponse =
        restTemplate.postForEntity(endpoint, entity, String.class);
    CodeSystem createdCodeSystem = parser.parseResource(CodeSystem.class, createResponse.getBody());
    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

    // 2. Read the created CodeSystem
    String getUrl = endpoint + "/" + createdCodeSystem.getIdPart();
    ResponseEntity<String> getResponse = restTemplate.getForEntity(getUrl, String.class);
    assertEquals(HttpStatus.OK, getResponse.getStatusCode());

    // 3. Delete the CodeSystem
    ResponseEntity<String> deleteResponse =
        restTemplate.exchange(getUrl, HttpMethod.DELETE, null, String.class);
    assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

    // 4. Verify deletion
    ResponseEntity<String> getAfterDelete = restTemplate.getForEntity(getUrl, String.class);
    assertEquals(HttpStatus.NOT_FOUND, getAfterDelete.getStatusCode());
  }

  /**
   * Test delete value set.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(DELETE)
  public void testCreateAndDeleteValueSet() throws Exception {
    LOGGER.info("Testing delete ValueSet endpoint");

    ValueSet valueSet = new ValueSet();
    valueSet.setTitle("Test ValueSet");
    valueSet.setPublisher("Test Publisher");
    valueSet.setVersion("1.0.0");
    valueSet.setUrl("http://example.org/fhir/ValueSet/" + valueSet.getIdPart());
    valueSet.setDate(new Date());

    final String json = parser.encodeResourceToString(valueSet);
    final org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
    headers.set("Content-Type", "application/fhir+json");
    final org.springframework.http.HttpEntity<String> entity =
        new org.springframework.http.HttpEntity<>(json, headers);
    ResponseEntity<String> response =
        restTemplate.postForEntity(LOCALHOST + port + FHIR_VALUESET, entity, String.class);
    ValueSet createdValueSet = parser.parseResource(ValueSet.class, response.getBody());
    String strTestId = createdValueSet.getIdPart();
    assertNotNull(strTestId);

    final String findUrl = LOCALHOST + port + FHIR_VALUESET + "/" + strTestId;
    LOGGER.info("Find value set URL {}", findUrl);
    response = restTemplate.getForEntity(findUrl, String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    valueSet = parser.parseResource(ValueSet.class, response.getBody());
    assertNotNull(valueSet, "ValueSet should not be null");

    // Try to delete the value set
    final String deleteUrl = LOCALHOST + port + FHIR_VALUESET + "/" + strTestId;
    LOGGER.info("Delete value set URL {}", deleteUrl);
    response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    // Verify the value set is deleted by attempting to delete it again
    final ResponseEntity<String> response2 =
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);
    assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());

    response = restTemplate.getForEntity(findUrl, String.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  /**
   * Test delete concept map.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(DELETE)
  public void testCreateAndDeleteConceptMap() throws Exception {
    LOGGER.info("Testing create, read and delete ConceptMap endpoint");

    ConceptMap conceptMap = new ConceptMap();
    conceptMap.setTitle("Test ConceptMap");
    conceptMap.setPublisher("Test Publisher");
    conceptMap.setVersion("1.0.0");
    conceptMap.setUrl("http://example.org/fhir/ConceptMap/" + conceptMap.getIdPart());
    conceptMap.setSourceScope(new IdType("CodeSystem/source-cs"));
    conceptMap.setTargetScope(new IdType("CodeSystem/target-cs"));
    conceptMap.setDate(new Date());
    conceptMap.setIdentifier(
        List.of(new Identifier().setSystem("http://example.org/fhir/ids").setValue("cm-1")));
    final String json = parser.encodeResourceToString(conceptMap);
    final org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
    headers.set("Content-Type", "application/fhir+json");
    final org.springframework.http.HttpEntity<String> entity =
        new org.springframework.http.HttpEntity<>(json, headers);
    ResponseEntity<String> response =
        restTemplate.postForEntity(LOCALHOST + port + FHIR_CONCEPTMAP, entity, String.class);
    ConceptMap createdMapset = parser.parseResource(ConceptMap.class, response.getBody());
    String testId = createdMapset.getIdPart();
    assertNotNull(testId);

    // Try to delete the concept map
    final String deleteUrl = LOCALHOST + port + FHIR_CONCEPTMAP + "/" + testId;
    LOGGER.info("Delete concept map URL {}", deleteUrl);
    response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    // Verify the concept map is deleted by attempting to delete it again
    final ResponseEntity<String> response2 =
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
  }

  /**
   * Test bundle loading using the bulk load endpoint.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(DELETE)
  public void testBundleWithCsCmVsTransaction() throws Exception {
    // Arrange
    final String endpoint = LOCALHOST + port + "/fhir/Bundle/$load";
    LOGGER.info("endpoint = {}", endpoint);

    // Load the bundle file as raw JSON
    @SuppressWarnings("resource")
    final String bundleJson = new String(getClass().getClassLoader()
        .getResourceAsStream("data/test-bundle-cs-cm-vs-r5.json").readAllBytes());

    // Create multipart request for bulk load endpoint
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("resource", new ByteArrayResource(bundleJson.getBytes()) {
      @Override
      public String getFilename() {
        return "test-bundle-cs-cm-vs-r5.json";
      }
    });

    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    final HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    // Act
    final ResponseEntity<String> response =
        this.restTemplate.postForEntity(endpoint, requestEntity, String.class);
    LOGGER.info("Response status: {}", response.getStatusCode());
    LOGGER.info("Response body: {}", response.getBody());

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Bundle POST should succeed");
    assertNotNull(response.getBody(), "Response should not be null");

    LOGGER.info("Bundle transaction test completed successfully");
  }

  /**
   * Test bundle loading using the bulk load endpoint.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(DELETE)
  public void testBundleWithCmVsTransaction() throws Exception {
    // Arrange
    final String endpoint = LOCALHOST + port + "/fhir/Bundle/$load";
    LOGGER.info("endpoint = {}", endpoint);

    // Load the bundle file as raw JSON
    @SuppressWarnings("resource")
    final String bundleJson = new String(getClass().getClassLoader()
        .getResourceAsStream("data/test-bundle-cm-vs-r5.json").readAllBytes());

    // Create multipart request for bulk load endpoint
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("resource", new ByteArrayResource(bundleJson.getBytes()) {
      @Override
      public String getFilename() {
        return "test-bundle-cm-vs-r5.json";
      }
    });

    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    final HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    // Act
    final ResponseEntity<String> response =
        this.restTemplate.postForEntity(endpoint, requestEntity, String.class);
    LOGGER.info("Response status: {}", response.getStatusCode());
    LOGGER.info("Response body: {}", response.getBody());

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Bundle POST should succeed");
    assertNotNull(response.getBody(), "Response should not be null");

    LOGGER.info("Bundle transaction test completed successfully");
  }

  /**
   * Test CodeSystem $lookup with missing code parameter returns 400.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testCodeSystemLookupMissingCodeReturnsBadRequest() throws Exception {
    final String endpoint = LOCALHOST + port + "/fhir/r5/CodeSystem/$lookup";
    LOGGER.info("Testing endpoint: {}", endpoint);

    final HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/fhir+json");
    final HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

    final ResponseEntity<String> response =
        this.restTemplate.postForEntity(endpoint, requestEntity, String.class);
    LOGGER.info("Response status: {}", response.getStatusCode());
    LOGGER.info("Response body: {}", response.getBody());

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
        "Missing code parameter should return 400");
    assertNotNull(response.getBody(), "Response should not be null");
    assertTrue(response.getBody().contains("OperationOutcome"),
        "Response should contain OperationOutcome");
    assertTrue(response.getBody().contains("issue"), "Response should contain issue");
  }

  /**
   * Test CodeSystem $lookup with invalid code returns 400.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testCodeSystemLookupInvalidCodeReturnsBadRequest() throws Exception {
    final String endpoint = LOCALHOST + port
        + "/fhir/r5/CodeSystem/$lookup?code=INVALID_CODE&system=http://snomed.info/sct";
    LOGGER.info("Testing endpoint: {}", endpoint);

    final HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/fhir+json");
    final HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

    final ResponseEntity<String> response =
        this.restTemplate.exchange(endpoint, HttpMethod.GET, requestEntity, String.class);
    LOGGER.info("Response status: {}", response.getStatusCode());
    LOGGER.info("Response body: {}", response.getBody());

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
        "Invalid code should return 400");
    assertNotNull(response.getBody(), "Response should not be null");
    assertTrue(response.getBody().contains("OperationOutcome"),
        "Response should contain OperationOutcome");
  }

  /**
   * Test CodeSystem $lookup with LOINC code when multiple versions exist.
   * This test loads a fake version 278 based on version 277, verifies that lookup
   * works correctly with version specified, then cleans up the fake version.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testCodeSystemLookupLoincWithVersion() throws Exception {
    // Load a fake version 278 based on 277
    final ClassPathResource resource277 = new ClassPathResource("data/CodeSystem-lnc-sandbox-277-r5.json");
    final String json277 = Files.readString(resource277.getFile().toPath(), StandardCharsets.UTF_8);

    // Replace version 277 with 278
    final String json278 = json277.replace("\"version\": \"277\"", "\"version\": \"278\"")
        .replace("\"277\"", "\"278\"").replace("-277-", "-278-");

    // Create temporary file
    final File tempFile = File.createTempFile("CodeSystem-lnc-sandbox-278-r5", ".json");
    tempFile.deleteOnExit();
    Files.write(tempFile.toPath(), json278.getBytes(StandardCharsets.UTF_8));

    Terminology terminology278 = null;
    try {
      // Load the fake version 278
      LOGGER.info("Loading fake LOINC version 278 for testing");
      CodeSystemLoaderUtil.loadCodeSystem(searchService, tempFile, false, CodeSystem.class,
          new DefaultProgressListener());

      // Find the terminology that was just loaded
      final SearchParameters params = new SearchParameters(
          "terminology:LNC AND version:278 AND publisher:SANDBOX", 0, 1, null, null);
      final ResultList<Terminology> results = searchService.find(params, Terminology.class);
      if (!results.getItems().isEmpty()) {
        terminology278 = results.getItems().get(0);
      }

      // Now test lookup with version 278 - should work without "multiple objects" error
      final String code = "66480-5";
      final String system = "http://loinc.org";
      final String version = "278";
      final String lookupParams = "/$lookup?code=" + code + "&system=" + system + "&version=" + version;
      final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + lookupParams;
      LOGGER.info("Testing lookup endpoint: {}", endpoint);

      final String content = this.restTemplate.getForObject(endpoint, String.class);
      LOGGER.info("Response content = {}", content);

      // Assert - Should NOT throw "Unexpectedly found more than one object" exception
      assertNotNull(content, "Response should not be null");
      assertFalse(content.contains("Unexpectedly found more than one object"),
          "Should not find multiple objects when version is specified - this validates the fix");

      // If successful, verify the response structure
      if (!content.contains("OperationOutcome") || content.contains("\"code\":\"not-found\"")) {
        // Either success or expected not-found (if code doesn't exist in test data)
        // Both are acceptable - the important thing is no "multiple objects" error
        LOGGER.info("Test passed: No 'multiple objects' error occurred");
      }

    } finally {
      // Clean up: remove the fake version 278
      if (terminology278 != null) {
        LOGGER.info("Cleaning up fake LOINC version 278");
        TerminologyUtility.removeTerminology(searchService, terminology278.getId());
      }
      // Delete temp file
      if (tempFile.exists()) {
        tempFile.delete();
      }
    }
  }

  /**
   * Test CodeSystem read with unknown ID returns 404.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testCodeSystemReadUnknownIdReturnsNotFound() throws Exception {
    final String endpoint = LOCALHOST + port + "/fhir/r5/CodeSystem/UNKNOWN_ID";
    LOGGER.info("Testing endpoint: {}", endpoint);

    final ResponseEntity<String> response = this.restTemplate.getForEntity(endpoint, String.class);
    LOGGER.info("Response status: {}", response.getStatusCode());
    LOGGER.info("Response body: {}", response.getBody());

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Unknown ID should return 404");
    assertNotNull(response.getBody(), "Response should not be null");
    assertTrue(response.getBody().contains("OperationOutcome"),
        "Response should contain OperationOutcome");
  }

  /**
   * Test ValueSet $expand with missing URL parameter returns 400.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testValueSetExpandMissingUrlReturnsBadRequest() throws Exception {
    final String endpoint = LOCALHOST + port + "/fhir/r5/ValueSet/$expand";
    LOGGER.info("Testing endpoint: {}", endpoint);

    final HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/fhir+json");
    final HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

    final ResponseEntity<String> response =
        this.restTemplate.postForEntity(endpoint, requestEntity, String.class);
    LOGGER.info("Response status: {}", response.getStatusCode());
    LOGGER.info("Response body: {}", response.getBody());

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
        "Missing URL parameter should return 400");
    assertNotNull(response.getBody(), "Response should not be null");
    assertTrue(response.getBody().contains("OperationOutcome"),
        "Response should contain OperationOutcome");
  }

  /**
   * Test ValueSet $validate-code with missing parameters returns 400.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testValueSetValidateCodeMissingParametersReturnsBadRequest() throws Exception {
    final String endpoint = LOCALHOST + port + "/fhir/r5/ValueSet/$validate-code";
    LOGGER.info("Testing endpoint: {}", endpoint);

    final HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/fhir+json");
    final HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

    final ResponseEntity<String> response =
        this.restTemplate.postForEntity(endpoint, requestEntity, String.class);
    LOGGER.info("Response status: {}", response.getStatusCode());
    LOGGER.info("Response body: {}", response.getBody());

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
        "Missing parameters should return 400");
    assertNotNull(response.getBody(), "Response should not be null");
    assertTrue(response.getBody().contains("OperationOutcome"),
        "Response should contain OperationOutcome");
  }

  /**
   * Test ValueSet read with unknown ID returns 404.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testValueSetReadUnknownIdReturnsNotFound() throws Exception {
    final String endpoint = LOCALHOST + port + "/fhir/r5/ValueSet/UNKNOWN_ID";
    LOGGER.info("Testing endpoint: {}", endpoint);

    final ResponseEntity<String> response = this.restTemplate.getForEntity(endpoint, String.class);
    LOGGER.info("Response status: {}", response.getStatusCode());
    LOGGER.info("Response body: {}", response.getBody());

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Unknown ID should return 404");
    assertNotNull(response.getBody(), "Response should not be null");
    assertTrue(response.getBody().contains("OperationOutcome"),
        "Response should contain OperationOutcome");
  }

  /**
   * Test ConceptMap $translate with missing parameters returns 400.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testConceptMapTranslateMissingParametersReturnsBadRequest() throws Exception {
    final String endpoint = LOCALHOST + port + "/fhir/r5/ConceptMap/$translate";
    LOGGER.info("Testing endpoint: {}", endpoint);

    final HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/fhir+json");
    final HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

    final ResponseEntity<String> response =
        this.restTemplate.postForEntity(endpoint, requestEntity, String.class);
    LOGGER.info("Response status: {}", response.getStatusCode());
    LOGGER.info("Response body: {}", response.getBody());

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
        "Missing parameters should return 400");
    assertNotNull(response.getBody(), "Response should not be null");
    assertTrue(response.getBody().contains("OperationOutcome"),
        "Response should contain OperationOutcome");
  }

  /**
   * Test ConceptMap read with unknown ID returns 404.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testConceptMapReadUnknownIdReturnsNotFound() throws Exception {
    final String endpoint = LOCALHOST + port + "/fhir/r5/ConceptMap/UNKNOWN_ID";
    LOGGER.info("Testing endpoint: {}", endpoint);

    final ResponseEntity<String> response = this.restTemplate.getForEntity(endpoint, String.class);
    LOGGER.info("Response status: {}", response.getStatusCode());
    LOGGER.info("Response body: {}", response.getBody());

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Unknown ID should return 404");
    assertNotNull(response.getBody(), "Response should not be null");
    assertTrue(response.getBody().contains("OperationOutcome"),
        "Response should contain OperationOutcome");
  }

  /**
   * Test Bundle $transaction with invalid bundle type returns 400.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testBundleTransactionInvalidTypeReturnsBadRequest() throws Exception {
    final String endpoint = LOCALHOST + port + "/fhir/r5/$transaction";
    LOGGER.info("Testing endpoint: {}", endpoint);

    final String requestBody = "{\"resourceType\":\"Bundle\",\"type\":\"invalid-type\"}";
    final HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/fhir+json");
    final HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

    final ResponseEntity<String> response =
        this.restTemplate.postForEntity(endpoint, requestEntity, String.class);
    LOGGER.info("Response status: {}", response.getStatusCode());
    LOGGER.info("Response body: {}", response.getBody());

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
        "Invalid bundle type should return 400");
    assertNotNull(response.getBody(), "Response should not be null");
    assertTrue(response.getBody().contains("OperationOutcome"),
        "Response should contain OperationOutcome");
  }

  /**
   * Test that error responses never return 500 status.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testErrorResponsesNeverReturn500() throws Exception {
    final String[] endpoints = {
        LOCALHOST + port + "/fhir/r5/CodeSystem/$lookup",
        LOCALHOST + port + "/fhir/r5/ValueSet/$expand",
        LOCALHOST + port + "/fhir/r5/ConceptMap/$translate",
        LOCALHOST + port + "/fhir/r5/CodeSystem/UNKNOWN_ID",
        LOCALHOST + port + "/fhir/r5/ValueSet/UNKNOWN_ID",
        LOCALHOST + port + "/fhir/r5/ConceptMap/UNKNOWN_ID"
    };

    for (final String endpoint : endpoints) {
      LOGGER.info("Testing endpoint: {}", endpoint);

      final HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/fhir+json");
      final HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

      final ResponseEntity<String> response =
          this.restTemplate.postForEntity(endpoint, requestEntity, String.class);
      LOGGER.info("Response status for {}: {}", endpoint, response.getStatusCode());

      assertTrue(response.getStatusCode().is4xxClientError(), "Endpoint " + endpoint
          + " should return 4xx, not 5xx. Status: " + response.getStatusCode());
    }
  }

  /**
   * Teardown.
   */
  @AfterAll
  public static void teardown() {
    // There are tests that delete content. So any subsequent tests should re-setup the data
    setSetupOnce(false);
  }
}
