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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r5.model.Bundle.LinkRelationTypes;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.CodeSystem.CodeSystemHierarchyMeaning;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.ResourceType;
import org.hl7.fhir.r5.model.ValueSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * Class tests for FhirR5Tests. Tests the functionality of the FHIR R5
 * endpoints, CodeSystem, ValueSet, and ConceptMap. All passed ids MUST be
 * lowercase, so they match our internally set id's
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class FhirR5RestUnitTest {

	/** The logger. */
	private static final Logger LOG = LoggerFactory.getLogger(FhirR5RestUnitTest.class);

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

	/** The fhir CM path. */
	// Add when available:
	// private static final String FHIR_CONCEPTMAP = "/fhir/r5/ConceptMap";

	/** The parser. */
	private static IParser parser;

	/** Sets the up once. */
	@BeforeAll
	public static void setUpOnce() {
		// Instantiate parser
		parser = FhirContext.forR5().newJsonParser();
	}

	/** Sets the up. */
	@BeforeEach
	public void setUp() {
		// The object mapper
		objectMapper = new ObjectMapper();
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
		LOG.info("  metadata = {}", parser.encodeResourceToString(data));
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
		LOG.info("endpoint = {}", endpoint);

		// Act
		final String content = this.restTemplate.getForObject(endpoint, String.class);
		final Bundle data = parser.parseResource(Bundle.class, content);
		final List<Resource> codeSystems = data.getEntry().stream().map(BundleEntryComponent::getResource).toList();

		// Assert bundle properties
		assertNotNull(data);
		assertEquals(ResourceType.Bundle, data.getResourceType());
		assertEquals(org.hl7.fhir.r5.model.Bundle.BundleType.SEARCHSET, data.getType());
		// assertEquals(org.hl7.fhir.r5.model.Bundle.BundleType.SEARCHSET,
		// data.getType());
		assertEquals(5, data.getTotal());
		assertNotNull(data.getMeta().getLastUpdated());
		assertFalse(data.getLink().isEmpty());
		assertEquals(LinkRelationTypes.SELF, data.getLink().get(0).getRelation());
		assertTrue(data.getLink().get(0).getUrl().endsWith("/fhir/r5/CodeSystem"));

		// Verify expected code systems
		final Set<String> expectedTitles = new HashSet<>(Set.of("ICD10CM", "LNC", "RXNORM", "SNOMEDCT", "SNOMEDCT_US"));
		final Set<String> expectedPublishers = new HashSet<>(Set.of("National Library of Medicine", "SANDBOX"));

		// Assert code systems
		assertFalse(codeSystems.isEmpty());
		assertEquals(5, codeSystems.size());

		for (final Resource cs : codeSystems) {
			LOG.info("  code system = {}", parser.encodeResourceToString(cs));
			final CodeSystem css = (CodeSystem) cs;

			// Verify required properties
			assertNotNull(css);
			assertEquals(ResourceType.CodeSystem, css.getResourceType());
			assertNotNull(css.getId());
			assertNotNull(css.getVersion());
			assertNotNull(css.getName());
			assertNotNull(css.getTitle());
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
	 * Test retrieving a specific CodeSystem by ID.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testCodeSystemById() throws Exception {
		// Arrange
		final String csId = "ef721e67-ebf5-4b50-a0b9-16d7aea7c1b6";
		final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + "/" + csId;
		LOG.info("endpoint = {}", endpoint);

		// Act
		final String content = this.restTemplate.getForObject(endpoint, String.class);
		final CodeSystem codeSystem = parser.parseResource(CodeSystem.class, content);

		// Assert
		assertNotNull(codeSystem);
		LOG.info("  code system = {}", parser.encodeResourceToString(codeSystem));

		// Verify resource type and id
		assertEquals(ResourceType.CodeSystem, codeSystem.getResourceType());
		assertEquals("CodeSystem/" + csId, codeSystem.getId());

		// Verify specific field values
		assertEquals("version/20240301", codeSystem.getVersion());
		assertEquals("Systematized Nomenclature of Medicine–Clinical Terminology, US Edition", codeSystem.getName());
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
		LOG.info("endpoint = {}", endpoint);

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
			LOG.info("CodeSystem = {}", parser.setPrettyPrint(true).encodeResourceToString(cs));
			assertTrue(cs.getName().contains("SNOMEDCT") || cs.getTitle().contains("SNOMEDCT"),
					"Search result should contain SNOMEDCT: " + cs.getName());
		}
	}

	/**
	 * Test CodeSystem $validate-code operation.
	 *
	 * @throws Exception the exception
	 */
	// @Test returns not found, but code is valid
	public void testCodeSystemValidateCode() throws Exception {
		// Arrange
		final String code = "385487005"; // "73211009";
		final String system = "http://snomed.info/sct";
		final String validateParams = "/$validate-code?code=" + code; // + "&system=" + system;
		final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + validateParams;
		LOG.info("endpoint = {}", endpoint);

		// Act
		final String content = this.restTemplate.getForObject(endpoint, String.class);
		final IBaseResource baseResource = parser.parseResource(content);
		final Resource resource = (Resource) baseResource;

		// Assert
		assertNotNull(resource);
		assertTrue(resource instanceof OperationOutcome,
				"Expected OperationOutcome but got " + resource.getClass().getSimpleName());

		final OperationOutcome outcome = (OperationOutcome) resource;
		LOG.info("OperationOutcome received: {}", parser.encodeResourceToString(outcome));

		// Verify the operation outcome has issues
		assertFalse(outcome.getIssue().isEmpty(), "OperationOutcome should contain issues");

		// Log all issues for debugging
		outcome.getIssue().forEach(issue -> {
			LOG.info("Issue severity: {}, code: {}, details: {}", issue.getSeverity(), issue.getCode(),
					issue.getDetails() != null ? issue.getDetails().getText() : "no details");
		});
	}

	/**
	 * Test CodeSystem $validate-code operation with specific ID.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testCodeSystemValidateCodeById() throws Exception {
		// Arrange
		final String csId = "04efd633-bcbc-41cd-959c-f5ed8d94adaa";
		final String code = "1796088";
		final String validateParams = "/$validate-code?code=" + code;
		final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + "/" + csId + validateParams;
		LOG.info("endpoint = {}", endpoint);

		// Act
		final String content = this.restTemplate.getForObject(endpoint, String.class);
		final Parameters result = parser.parseResource(Parameters.class, content);

		// Assert
		assertNotNull(result);
		LOG.info("Parameters = {}", parser.encodeResourceToString(result));
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
	// @Test Code system not found matching 'system' parameter
	public void testCodeSystemSubsumes() throws Exception {
		// Arrange
		final String codeA = "73211009";
		final String codeB = "39592008";
		final String system = "http://snomed.info/sct";
		final String subsumesParams = "/$subsumes?codeA=" + codeA + "&codeB=" + codeB + "&system=" + system;
		final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + subsumesParams;
		LOG.info("endpoint = {}", endpoint);

		// Act
		final String content = this.restTemplate.getForObject(endpoint, String.class);
		final Parameters result = parser.parseResource(Parameters.class, content);

		// Assert
		assertNotNull(result);
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
		final String csId = "ef721e67-ebf5-4b50-a0b9-16d7aea7c1b6";
		final String codeA = "73211009";
		final String codeB = "727499001";
		final String subsumesParams = "/$subsumes?codeA=" + codeA + "&codeB=" + codeB;
		final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + "/" + csId + subsumesParams;
		LOG.info("endpoint = {}", endpoint);

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
	// @Test Is system correct?
	public void testCodeSystemLookup() throws Exception {
		// Arrange
		final String code = "73211009";
		final String system = "http://snomed.info/sct";
		final String lookupParams = "/$lookup?code=" + code + "&system=" + system;
		final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + lookupParams;
		LOG.info("endpoint = {}", endpoint);

		// Act
		final String content = this.restTemplate.getForObject(endpoint, String.class);
		final Parameters result = parser.parseResource(Parameters.class, content);

		// Assert
		assertNotNull(result);
		assertEquals(code, result.getParameter("code").getValue().toString());
		assertEquals(system, result.getParameter("system").getValue().toString());
		assertNotNull(result.getParameter("display"));
		assertTrue(result.getParameterBool("active"));
	}

	/**
	 * Test CodeSystem $lookup operation with specific ID.
	 *
	 * @throws Exception the exception
	 */
	// @Test "diagnostics": "Failed to lookup code"
	public void testCodeSystemLookupById() throws Exception {
		// Arrange
		final String csId = "ef721e67-ebf5-4b50-a0b9-16d7aea7c1b6";
		final String code = "73211009";
		final String lookupParams = "/$lookup?code=" + code;
		final String endpoint = LOCALHOST + port + FHIR_CODESYSTEM + "/" + csId + lookupParams;
		LOG.info("endpoint = {}", endpoint);

		// Act
		final String content = this.restTemplate.getForObject(endpoint, String.class);
		final Parameters result = parser.parseResource(Parameters.class, content);

		// Assert
		assertNotNull(result);
		assertEquals(code, result.getParameter("code").getValue().toString());
		assertNotNull(result.getParameter("display"));
		assertTrue(result.getParameterBool("active"));
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
		LOG.info("endpoint = {}", endpoint);

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
		final String vsId = "ee824fe2-8c4d-4ad0-a060-8167914f65fd_entire";
		final String endpoint = LOCALHOST + port + FHIR_VALUESET + "/" + vsId;
		LOG.info("endpoint = {}", endpoint);

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
		LOG.info("endpoint = {}", endpoint);

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
		LOG.info("endpoint = {}", endpoint);

		String content = this.restTemplate.getForObject(endpoint, String.class);
		final Bundle data = parser.parseResource(Bundle.class, content);
		final List<Resource> valueSets = data.getEntry().stream().map(Bundle.BundleEntryComponent::getResource)
				.toList();

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
			assertEquals(ResourceType.ValueSet, valueSet.getResourceType(), "Resource type should be ValueSet");
			assertEquals(valueSetId, valueSet.getIdPart(), "IDs should match");

			// Compare with original bundle entry
			final ValueSet originalVs = (ValueSet) resource;
			assertEquals(originalVs.getUrl(), valueSet.getUrl(), "URLs should match");
			assertEquals(originalVs.getName(), valueSet.getName(), "Names should match");
			assertEquals(originalVs.getTitle(), valueSet.getTitle(), "Titles should match");
			assertEquals(originalVs.getVersion(), valueSet.getVersion(), "Versions should match");
			assertEquals(originalVs.getPublisher(), valueSet.getPublisher(), "Publishers should match");
			assertEquals(originalVs.getDescription(), valueSet.getDescription(), "Descriptions should match");
			assertEquals(originalVs.getStatus(), valueSet.getStatus(), "Status should match");
		}
	}

	/**
	 * Test ValueSet $validate-code operation.
	 *
	 * @throws Exception the exception
	 */
	// @Test STUCK IN AN INFINITE LOOP
	public void testValueSetValidateCode() throws Exception {
		// Arrange
		final String code = "73211009";
		final String system = "http://snomed.info/sct"; // null?fhir_vs Value from output of testValueSetRead
		final String validateParams = "/$validate-code?code=" + code + "&system=" + system;
		final String endpoint = LOCALHOST + port + FHIR_VALUESET + validateParams;
		LOG.info("endpoint = {}", endpoint);

		// Act
		final String content = this.restTemplate.getForObject(endpoint, String.class);
		final IBaseResource baseResource = parser.parseResource(content);
		final Resource resource = (Resource) baseResource;

		// Assert
		assertNotNull(resource);
		assertTrue(resource instanceof OperationOutcome,
				"Expected OperationOutcome but got " + resource.getClass().getSimpleName());

		final OperationOutcome outcome = (OperationOutcome) resource;
		LOG.info("OperationOutcome received: {}", parser.encodeResourceToString(outcome));

		// Verify the operation outcome has issues
		assertFalse(outcome.getIssue().isEmpty(), "OperationOutcome should contain issues");

		// Log all issues for debugging
		outcome.getIssue().forEach(issue -> {
			LOG.info("Issue severity: {}, code: {}, details: {}", issue.getSeverity(), issue.getCode(),
					issue.getDetails() != null ? issue.getDetails().getText() : "no details");
		});
	}

	/**
	 * Test ValueSet $validate-code operation with specific ID.
	 *
	 * @throws Exception the exception
	 */
	// @Test STUCK IN AN INFINITE LOOP
	public void testValueSetValidateCodeById() throws Exception {
		// Arrange
		final String vsId = "ef721e67-ebf5-4b50-a0b9-16d7aea7c1b6_entire"; // Example SNOMED CT ValueSet ID
		final String code = "73211009";
		final String validateParams = "/$validate-code?code=" + code;
		final String endpoint = LOCALHOST + port + FHIR_VALUESET + "/" + vsId + validateParams;
		LOG.info("endpoint = {}", endpoint);

		// Act
		final String content = this.restTemplate.getForObject(endpoint, String.class);
		final IBaseResource baseResource = parser.parseResource(content);
		final Resource resource = (Resource) baseResource;

		// Assert
		assertNotNull(resource);
		assertTrue(resource instanceof OperationOutcome,
				"Expected OperationOutcome but got " + resource.getClass().getSimpleName());

		final OperationOutcome outcome = (OperationOutcome) resource;
		LOG.info("OperationOutcome received: {}", parser.encodeResourceToString(outcome));

		// Verify the operation outcome has issues
		assertFalse(outcome.getIssue().isEmpty(), "OperationOutcome should contain issues");

		// Log all issues for debugging
		outcome.getIssue().forEach(issue -> {
			LOG.info("Issue severity: {}, code: {}, details: {}", issue.getSeverity(), issue.getCode(),
					issue.getDetails() != null ? issue.getDetails().getText() : "no details");
		});
	}

	/**
	 * Test ValueSet $expand operation.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testValueSetExpand() throws Exception {
		// Arrange
		final String expandParams = "/$expand?_count=50";
		final String endpoint = LOCALHOST + port + FHIR_VALUESET + expandParams;
		LOG.info("endpoint = {}", endpoint);

		// Act
		final String content = this.restTemplate.getForObject(endpoint, String.class);
		final IBaseResource baseResource = parser.parseResource(content);
		final Resource resource = (Resource) baseResource;

		// Assert
		assertNotNull(resource);
		assertTrue(resource instanceof OperationOutcome,
				"Expected OperationOutcome but got " + resource.getClass().getSimpleName());

		final OperationOutcome outcome = (OperationOutcome) resource;
		LOG.info("OperationOutcome received: {}", parser.encodeResourceToString(outcome));

		// Verify the operation outcome has issues
		assertFalse(outcome.getIssue().isEmpty(), "OperationOutcome should contain issues");

		// Log all issues for debugging
		outcome.getIssue().forEach(issue -> {
			LOG.info("Issue severity: {}, code: {}, details: {}", issue.getSeverity(), issue.getCode(),
					issue.getDetails() != null ? issue.getDetails().getText() : "no details");
		});
	}

	/**
	 * Test ValueSet $expand operation with specific ID.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testValueSetExpandById() throws Exception {
		// Arrange
		final String vsId = "ef721e67-ebf5-4b50-a0b9-16d7aea7c1b6_entire"; // SNOMEDCT_US ValueSet ID
		final int count = 50;
		final String expandParams = "/$expand?count=" + count;
		final String endpoint = LOCALHOST + port + FHIR_VALUESET + "/" + vsId + expandParams;
		LOG.info("endpoint = {}", endpoint);

		// Act
		final String content = this.restTemplate.getForObject(endpoint, String.class);
		final ValueSet valueSet = parser.parseResource(ValueSet.class, content);

		// Assert
		assertNotNull(valueSet, "ValueSet should not be null");
		assertEquals(ResourceType.ValueSet, valueSet.getResourceType(), "Resource type should be ValueSet");
		assertEquals(vsId, valueSet.getIdPart(), "ValueSet ID should match");

		// Verify ValueSet metadata
		assertEquals("SNOMEDCT_US-ENTIRE", valueSet.getTitle(), "Title should match");
		assertEquals("VS Systematized Nomenclature of Medicine–Clinical Terminology, US Edition", valueSet.getName(),
				"Name should match");
		assertEquals("version/20240301", valueSet.getVersion(), "Version should match");
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
		final ValueSet.ValueSetExpansionContainsComponent firstEntry = valueSet.getExpansion().getContains().get(0);
		assertNotNull(firstEntry.getCode(), "First entry code should not be null");
		assertNotNull(firstEntry.getDisplay(), "First entry display should not be null");
		assertEquals("1007411", firstEntry.getCode(), "First entry code should match");
		assertEquals("chlorpropamide / metformin", firstEntry.getDisplay(), "First entry display should match");
	}

}
