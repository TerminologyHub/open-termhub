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
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r5.model.Bundle.LinkRelationTypes;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.CodeSystem.CodeSystemHierarchyMeaning;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.ResourceType;
import org.hl7.fhir.r5.model.ValueSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.model.HasId;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.CodeSystemLoaderUtil;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.PropertyUtility;
import com.wci.termhub.util.ThreadLocalMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * Class tests for FhirR5Tests. Tests the functionality of the FHIR R5 endpoints, CodeSystem,
 * ValueSet, and ConceptMap. All passed ids MUST be lowercase, so they match our internally set id's
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:application-test-r5.properties")
public class FhirR5RestUnitTest {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(FhirR5RestUnitTest.class);

  /** The port. */
  @LocalServerPort
  private int port;

  /** The rest template. */
  @Autowired
  private TestRestTemplate restTemplate;

  /** The object mapper. */
  private ObjectMapper objectMapper;

  /** local host prefix. */
  private static final String LOCALHOST = "http://localhost:";

  /** The fhir metadata. */
  private static final String FHIR_METADATA = "/fhir/r5/metadata";

  /** Fhir url paths. */
  private static final String FHIR_CODESYSTEM = "/fhir/r5/CodeSystem";

  /** The fhir VS path. */
  private static final String FHIR_VALUESET = "/fhir/r5/ValueSet";

  /** The parser. */
  private static IParser parser;

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** List of FHIR Code System files to load. */
  private static final List<String> CODE_SYSTEM_FILES =
      List.of("CodeSystem-snomedctus-sandbox-20240301-r5.json",
          "CodeSystem-snomedct-sandbox-20240101-r5.json", "CodeSystem-lnc-sandbox-277-r5.json",
          "CodeSystem-icd10cm-sandbox-2023-r5.json", "CodeSystem-rxnorm-sandbox-04012024-r5.json");

  /**
   * Sets the up once.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public void setUpOnce() throws Exception {
    // Instantiate parser
    parser = FhirContext.forR5().newJsonParser();

    // Get index directory from properties
    final String indexDirPath =
        PropertyUtility.getProperties().getProperty("lucene.index.directory");
    LOGGER.info("Using index directory: {}", indexDirPath);

    // Delete all indexes for a fresh start
    final File indexDir = new File(indexDirPath);
    if (indexDir.exists()) {
      LOGGER.info("Deleting existing indexes from directory: {}", indexDirPath);
      FileUtils.deleteDirectory(indexDir);
    }

    final List<Class<? extends HasId>> indexedObjects = ModelUtility.getIndexedObjects();
    for (final Class<? extends HasId> clazz : indexedObjects) {
      searchService.deleteIndex(clazz);
      searchService.createIndex(clazz);
    }

    // Load each code system
    for (final String codeSystemFile : CODE_SYSTEM_FILES) {
      try {
        // Read file from classpath
        final ClassPathResource resource = new ClassPathResource("data/" + codeSystemFile,
            FhirR5RestUnitTest.class.getClassLoader());

        if (!resource.exists()) {
          throw new FileNotFoundException("Could not find resource: data/" + codeSystemFile);
        }

        final String fileContent =
            FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8);

        LOGGER.info("Loading code system from file: {}", codeSystemFile);
        CodeSystemLoaderUtil.loadCodeSystem(searchService, fileContent, true);

      } catch (final Exception e) {
        LOGGER.error("Error loading code system file: {}", codeSystemFile, e);
        throw e;
      }
    }
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
    assertEquals(5, data.getTotal());
    assertNotNull(data.getMeta().getLastUpdated());
    assertFalse(data.getLink().isEmpty());
    assertEquals(LinkRelationTypes.SELF, data.getLink().get(0).getRelation());
    assertTrue(data.getLink().get(0).getUrl().endsWith("/fhir/r5/CodeSystem"));

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
  public void testCodeSystemById() throws Exception {
    // Arrange
    final String csId = "a1d1e426-26a6-4326-b18b-c54c154079b5";
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
    assertEquals(440, codeSystem.getCount());
  }

  /**
   * Test CodeSystem _search operation.
   *
   * @throws Exception the exception
   */
  @Test
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
  public void testValueSetRead() throws Exception {
    // Arrange
    final String endpoint = LOCALHOST + port + FHIR_VALUESET;
    LOGGER.info("endpoint = {}", endpoint);

    String content = this.restTemplate.getForObject(endpoint, String.class);
    final Bundle data = parser.parseResource(Bundle.class, content);
    final List<Resource> valueSets =
        data.getEntry().stream().map(Bundle.BundleEntryComponent::getResource).toList();

    // Assert bundle has expected number of entries
    assertEquals(5, valueSets.size(), "Should have 5 ValueSet entries");

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
  public void testValueSetExpand() throws Exception {
    // Arrange
    final String expandParams = "/$expand?url=2023&count=50";
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
    assertEquals(50, valueSet.getExpansion().getTotal());
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
  public void testValueSetExpandById() throws Exception {
    // Arrange
    final String vsId = "3e8e4d7c-7d3a-4682-a1e4-c5db5bc33d4b_entire";
    // ValueSet
    // ID
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
    assertEquals(count, valueSet.getExpansion().getTotal());
    assertEquals(0, valueSet.getExpansion().getOffset(), "Offset should be 0");

    // Verify expansion contains
    assertNotNull(valueSet.getExpansion().getContains(), "Contains should not be null");
    assertFalse(valueSet.getExpansion().getContains().isEmpty(), "Contains should not be empty");

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

}
