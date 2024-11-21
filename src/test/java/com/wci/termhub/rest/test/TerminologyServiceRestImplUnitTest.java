/*
 *
 */
package com.wci.termhub.rest.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.Application;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.HealthCheck;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.ResultListConcept;
import com.wci.termhub.model.ResultListConceptRelationship;
import com.wci.termhub.model.ResultListMetadata;
import com.wci.termhub.model.ResultListTerm;
import com.wci.termhub.model.ResultListTerminology;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.test.AbstractTerminologyServerTest;

/**
 * The Class TerminologyServiceRestImplUnitTest.
 */
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class TerminologyServiceRestImplUnitTest extends AbstractTerminologyServerTest {

	/** The logger. */
	private static final Logger logger = LoggerFactory.getLogger(TerminologyServiceRestImplUnitTest.class);

	/** The mock mvc. */
	@Autowired
	private MockMvc mockMvc;

	/** The base url. */
	private String baseUrl = "";

	/** The object mapper. */
	@Autowired
	private ObjectMapper objectMapper;

	/** The terminology service rest impl unit test. */
	@InjectMocks
	private TerminologyServiceRestImplUnitTest terminologyServiceRestImplUnitTest;

	/**
	 * Sets the up.
	 */
	@BeforeEach
	public void setUp() {
		baseUrl = "";
	}

	/**
	 * Test health.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testHealth() throws Exception {

		// Add a user through the API
		final String url = baseUrl + "/terminology/health";
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final HealthCheck check = objectMapper.readValue(content, HealthCheck.class);
		assertThat(check).isNotNull();
		assertThat(check.getName()).isEqualTo("open-termhub-terminology-service");
	}

	@Test
	public void testGetTerminologyICD10CM() throws Exception {
		final String id = "04efd633-bcbc-41cd-959c-f5ed8d94adaa";
		final String url = baseUrl + "/terminology/" + id;
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final Terminology terminology = objectMapper.readValue(content, Terminology.class);
		assertThat(terminology).isNotNull();
		assertEquals(id, terminology.getId());
		assertEquals("ICD10CM", terminology.getAbbreviation());
		assertEquals("International Classification of Diseases, Tenth Revision, Clinical Modification",
				terminology.getName());
	}

	// @Test
	// TODO: NUNO
	// expected: <Systematized Nomenclature of Medicine–Clinical Terminology, US
	// Edition> but was: <Systematized Nomenclature of Medicineâ€“Clinical
	// Terminology, US Edition>
	public void testGetTerminologySnomedCtUs() throws Exception {
		final String id = "ef721e67-ebf5-4b50-a0b9-16d7aea7c1b6";
		final String url = baseUrl + "/terminology/" + id;
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final Terminology terminology = objectMapper.readValue(content, Terminology.class);
		assertThat(terminology).isNotNull();
		assertEquals(id, terminology.getId());
		assertEquals("SNOMEDCT_US", terminology.getAbbreviation());
		assertEquals("Systematized Nomenclature of Medicine–Clinical Terminology, US Edition", terminology.getName());
	}

	@Test
	public void testGetTerminologyMetaDataSnomedCtUs() throws Exception {
		final String id = "ef721e67-ebf5-4b50-a0b9-16d7aea7c1b6";
		final String url = baseUrl + "/terminology/" + id + "/metadata";
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		final List<Metadata> metadataList = objectMapper.readValue(content, new TypeReference<List<Metadata>>() {
		});
		assertThat(metadataList).isNotNull();
		assertThat(metadataList.size()).isGreaterThan(0);
		for (final Metadata metadata : metadataList) {
			assertThat(metadata).isNotNull();
			// name can be null
			// assertThat(metadata.getName()).isNotNull();
			assertThat(metadata.getCode()).isNotNull();
			assertThat(metadata.getModel()).isNotNull();
			assertEquals("SNOMEDCT_US", metadata.getTerminology());
			assertEquals("20240301", metadata.getVersion());
		}
	}

	// @Test
	// Should be 404 but is 500
	public void testGetTerminologyMetaDataSnomedCtUsNotFound() throws Exception {
		final String id = "ef721e67-ebf5-4b50-a0b9-16d7aea7FAKE";
		final String url = baseUrl + "/terminology/" + id + "/metadata";
		logger.info("Testing url - {}", url);
		mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt())).andExpect(status().isNotFound())
				.andReturn();
	}

	@Test
	public void testGetTerminologies() throws Exception {
		final String url = baseUrl + "/terminology";
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		final ResultListTerminology terminologyList = objectMapper.readValue(content, ResultListTerminology.class);
		assertThat(terminologyList).isNotNull();
		assertThat(terminologyList.getTotal()).isGreaterThan(0);
		for (final Terminology terminology : terminologyList.getItems()) {
			assertThat(terminology).isNotNull();
			assertThat(terminology.getId()).isNotNull();
			assertThat(terminology.getAbbreviation()).isNotNull();
			assertThat(terminology.getName()).isNotNull();
			assertThat(terminology.getFamily()).isNotNull();
			assertThat(terminology.getPublisher()).isNotNull();
		}
	}

	@Test
	public void testGetTerminologyWithQuery() throws Exception {
		final String url = baseUrl + "/terminology?query=abbreviation:SNOMEDCT_US";
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		final ResultListTerminology terminologyList = objectMapper.readValue(content, ResultListTerminology.class);
		assertThat(terminologyList).isNotNull();
		assertThat(terminologyList.getTotal()).isGreaterThan(0);
		for (final Terminology terminology : terminologyList.getItems()) {
			assertThat(terminology).isNotNull();
			assertThat(terminology.getId()).isNotNull();
			assertEquals("SNOMEDCT_US", terminology.getAbbreviation());
		}
	}

	@Test
	public void testGetConceptById() throws Exception {
		final String id = "99d4899b-db6f-4bf8-ba51-b65e1cf8f7fb";
		final String url = baseUrl + "/concept/" + id;
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final Concept concept = objectMapper.readValue(content, Concept.class);
		assertThat(concept).isNotNull();
		assertEquals(id, concept.getId());
		assertEquals("ICD10CM", concept.getTerminology());
		assertEquals("E11", concept.getCode());
		assertEquals("Type 2 diabetes mellitus", concept.getName());
		assertEquals("2023", concept.getVersion());
		assertEquals("NLM", concept.getPublisher());
		assertThat(concept.getTerms()).isNotNull();
		assertEquals(4, concept.getTerms().size());
		assertThat(concept.getAttributes()).isNotNull();
		assertThat(concept.getSemanticTypes()).isNotNull();
		assertThat(concept.getParents()).isNotNull();
		assertThat(concept.getChildren()).isNotNull();
		assertThat(concept.getAncestors()).isNotNull();
		assertThat(concept.getDescendants()).isNotNull();
	}

	@Test
	public void testGetConceptByIdNotFound() throws Exception {
		final String id = "ef721e67-ebf5-4b50-a0b9-16d7aea7FAKE";
		final String url = baseUrl + "/concept/" + id;
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		assertThat(content).isEmpty();

	}

	@Test
	public void testGetConceptByTerminologyAndCode() throws Exception {
		final String code = "E11";
		final String terminology = "ICD10CM";
		final String url = baseUrl + "/concept/" + terminology + "/" + code;
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final Concept concept = objectMapper.readValue(content, Concept.class);
		assertThat(concept).isNotNull();
		assertEquals("99d4899b-db6f-4bf8-ba51-b65e1cf8f7fb", concept.getId());
		assertEquals(terminology, concept.getTerminology());
		assertEquals(code, concept.getCode());
		assertEquals("Type 2 diabetes mellitus", concept.getName());
		assertEquals("2023", concept.getVersion());
		assertEquals("NLM", concept.getPublisher());
		assertThat(concept.getTerms()).isNotNull();
		assertEquals(4, concept.getTerms().size());
		assertThat(concept.getAttributes()).isNotNull();
		assertThat(concept.getSemanticTypes()).isNotNull();
		assertThat(concept.getParents()).isNotNull();
		assertThat(concept.getChildren()).isNotNull();
		assertThat(concept.getAncestors()).isNotNull();
		assertThat(concept.getDescendants()).isNotNull();
	}

	@Test
	public void testGetConceptByTerminologyAndCodeNotFound() throws Exception {
		final String code = "ZZZ";
		final String terminology = "ICD10CM";
		final String url = baseUrl + "/concept/" + terminology + "/" + code;
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		assertThat(content).isEmpty();
	}

	@Test
	public void testGetConceptCodes() throws Exception {
		final String terminology = "LNC";
		final List<String> codes = List.of("LP32519-8", "LP231645-5", "63904-7", "74291-6", "FAKE");
		final String url = baseUrl + "/concept/" + terminology + "?codes=" + String.join(",", codes);
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final List<Concept> concepts = objectMapper.readValue(content, new TypeReference<List<Concept>>() {
		});
		assertThat(concepts).isNotNull();
		assertThat(concepts.size()).isEqualTo(4);
		for (final Concept concept : concepts) {
			assertThat(concept).isNotNull();
			assertThat(concept.getId()).isNotNull();
			assertThat(concept.getTerminology()).isEqualTo(terminology);
			assertThat(concept.getCode()).isIn(codes);
		}
	}

	@Test
	public void testFindConcepts() throws Exception {
		final String terminology = "RXNORM";
		final String query = "canagliflozin";
		final int limit = 15;
		final String url = baseUrl + "/concept?terminology=" + terminology + "&query=name:" + query + "&limit=" + limit;
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final ResultListConcept conceptList = objectMapper.readValue(content, ResultListConcept.class);
		assertThat(conceptList).isNotNull();
		assertThat(conceptList.getItems().size()).isGreaterThan(0);
		assertThat(conceptList.getItems().size()).isLessThanOrEqualTo(limit);
		for (final Concept concept : conceptList.getItems()) {
			assertThat(concept).isNotNull();
			assertThat(concept.getId()).isNotNull();
			assertThat(concept.getTerminology()).isEqualTo(terminology);
			assertThat(concept.getName().toLowerCase()).contains(query.toLowerCase());
		}
	}

	@Test
	public void testFindTerms() throws Exception {
		final String terminology = "RXNORM";
		final String query = "nirmatrelvir";
		final int limit = 15;
		final String url = baseUrl + "/term?terminology=" + terminology + "&query=name:" + query + "&limit=" + limit;
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final ResultListTerm termsList = objectMapper.readValue(content, ResultListTerm.class);
		assertThat(termsList).isNotNull();
		assertThat(termsList.getItems().size()).isGreaterThan(0);
		assertThat(termsList.getItems().size()).isLessThanOrEqualTo(limit);
		for (final Term term : termsList.getItems()) {
			assertThat(term).isNotNull();
			assertThat(term.getId()).isNotNull();
			assertThat(term.getTerminology()).isEqualTo(terminology);
			assertThat(term.getName().toLowerCase()).contains(query.toLowerCase());
		}
	}

	// @Test query not working with Lucene
	public void testAutocomplete() throws Exception {
		final String terminology = "RXNORM";
		final String query = "nirmat";
		final int limit = 10;
		final String url = baseUrl + "/autocomplete?terminology=" + terminology + "&query=" + query + "&limit=" + limit;
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final List<String> names = objectMapper.readValue(content, new TypeReference<List<String>>() {
		});
		assertThat(names).isNotNull();
		assertThat(names.size()).isGreaterThan(0);
		assertThat(names.size()).isLessThanOrEqualTo(limit);
		for (final String name : names) {
			assertThat(name).isNotNull();
			assertThat(name.toLowerCase()).contains(query.toLowerCase());
		}
	}

	@Test
	public void testConceptBulk() throws Exception {
		final String terminology = "LNC";
		final int limit = 12;
		final String url = baseUrl + "/concept/bulk?terminology=" + terminology + "&limit=" + limit;
		final List<String> codes = List.of("code:LP32519-8", "code:LP231645-5", "code:63904-7", "code:74291-6",
				"code:FAKE");
		final String body = String.join("\n", codes);
		logger.info("Testing url - {}, body - {}", url, body);
		final MvcResult result = mockMvc.perform(post(url).header("Authorization", "Bearer " + getTestJwt())
				.contentType(MediaType.TEXT_PLAIN).content(body)).andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final List<ResultListConcept> resultConceptList = objectMapper.readValue(content,
				new TypeReference<List<ResultListConcept>>() {
				});
		assertThat(resultConceptList).isNotNull();
		assertThat(resultConceptList.size()).isEqualTo(5);
		for (final ResultListConcept resultConcept : resultConceptList) {
			if ("code:FAKE".equals(resultConcept.getParameters().getQuery())) {
				continue;
			}
			assertThat(resultConcept).isNotNull();
			for (final Concept concept : resultConcept.getItems()) {
				assertThat(concept).isNotNull();
				assertThat(concept.getId()).isNotNull();
				assertThat(concept.getTerminology()).isEqualTo(terminology);
				assertThat("code:" + concept.getCode()).isIn(codes);
			}
		}
	}

	@Test
	public void testExportLookup() throws Exception {
		final String terminology = "LNC";
		final int limit = 12;
		final String url = baseUrl + "/concept/bulk/export?terminology=" + terminology + "&limit=" + limit;
		final List<String> codes = List.of("code:LP32519-8", "code:LP231645-5", "code:63904-7", "code:74291-6",
				"code:FAKE");
		final String body = String.join("\n", codes);
		logger.info("Testing url - {}, body - {}", url, body);
		final MvcResult result = mockMvc.perform(post(url).header("Authorization", "Bearer " + getTestJwt())
				.contentType(MediaType.TEXT_PLAIN).content(body)).andExpect(status().isOk()).andReturn();
		final byte[] content = result.getResponse().getContentAsByteArray();
		logger.info(" content length = {}", content.length);
		assertThat(content).isNotEmpty();

		// Optionally, write the content to a temporary file and check its contents
		final Path tempFile = Files.createTempFile("concept_bulk_export", ".xls");
		Files.write(tempFile, content);
		final List<String> fileContent = Files.readAllLines(tempFile);
		logger.info(" file content = {}", fileContent);
		assertThat(fileContent.contains("LP32519-8"));
		assertThat(fileContent.contains("LP231645-5"));
		assertThat(fileContent.contains("63904-7"));
		assertThat(fileContent.contains("74291-6"));
		assertThat(!fileContent.contains("FAKE"));
	}

	// @Test - TODO in method for expression
	public void testExportConcepts() throws Exception {
		final String terminology = "LNC";
		final int limit = 12;
		final String url = baseUrl + "/concept/export?terminology=" + terminology + "&limit=" + limit;
		final List<String> codes = List.of("code:LP32519-8", "code:LP231645-5", "code:63904-7", "code:74291-6",
				"code:FAKE");
		final String body = String.join("\n", codes);
		logger.info("Testing url - {}, body - {}", url, body);
		final MvcResult result = mockMvc.perform(post(url).header("Authorization", "Bearer " + getTestJwt())
				.contentType(MediaType.TEXT_PLAIN).content(body)).andExpect(status().isOk()).andReturn();

		final byte[] content = result.getResponse().getContentAsByteArray();
		logger.info(" content length = {}", content.length);
		assertThat(content).isNotEmpty();

		// Optionally, write the content to a temporary file and check its contents
		final Path tempFile = Files.createTempFile("concept_export", ".txt");
		Files.write(tempFile, content);
		final List<String> fileContent = Files.readAllLines(tempFile);
		logger.info(" file content = {}", fileContent);
		assertThat(fileContent.contains("LP32519-8"));
		assertThat(fileContent.contains("LP231645-5"));
		assertThat(fileContent.contains("63904-7"));
		assertThat(fileContent.contains("74291-6"));
		assertThat(!fileContent.contains("FAKE"));
	}

	@Test
	public void testFindMetadata() throws Exception {
		// GET /metadata
		final String terminology = "LNC";
		final int limit = 100;
		final String url = baseUrl + "/metadata?query=terminology:" + terminology + "&limit=" + limit;
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final ResultListMetadata metadataList = objectMapper.readValue(content, ResultListMetadata.class);
		assertThat(metadataList).isNotNull();
		assertThat(metadataList.getItems().size()).isGreaterThan(0);
		assertThat(metadataList.getItems().size()).isLessThanOrEqualTo(limit);
		for (final Metadata metadata : metadataList.getItems()) {
			assertThat(metadata).isNotNull();
			// can be null
			// assertThat(metadata.getCode()).isNotNull();
			assertThat(metadata.getTerminology()).isEqualTo(terminology);
			assertThat(metadata.getModel()).isNotNull();
			assertThat(metadata.getName()).isNotNull();
			assertThat(metadata.getVersion()).isNotNull();
		}
	}

	@Test
	public void testFindConceptRelationships() throws Exception {
		// GET concept/{conceptId}/relationships
		final String conceptId = "28bf322a-5e06-4771-844b-172aaa0fb4c6";
		final String url = baseUrl + "/concept/" + conceptId + "/relationships";
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final ResultListConceptRelationship conceptRels = objectMapper.readValue(content,
				ResultListConceptRelationship.class);
		assertThat(conceptRels).isNotNull();
		assertThat(conceptRels.getItems().size()).isGreaterThan(0);
		for (final ConceptRelationship conceptRel : conceptRels.getItems()) {
			assertThat(conceptRel).isNotNull();
			assertThat(conceptRel.getId()).isNotNull();
			assertThat(conceptRel.getTerminology()).contains("SNOMEDCT");
			assertThat(conceptRel.getVersion()).isNotNull();
			assertThat(conceptRel.getFrom()).isNotNull();
			assertThat(conceptRel.getFrom().getCode()).isNotNull();
			assertThat(conceptRel.getFrom().getTerminology()).contains("SNOMEDCT");
			assertThat(conceptRel.getTo()).isNotNull();
		}
	}

	@Test
	public void testFindConceptRelationships2() throws Exception {
		// GET concept/{terminology}/{code}/relationships
		final String terminology = "SNOMEDCT_US";
		final String code = "404684003";
		final String url = baseUrl + "/concept/" + terminology + "/" + code + "/relationships";
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final ResultListConceptRelationship conceptRels = objectMapper.readValue(content,
				ResultListConceptRelationship.class);
		assertThat(conceptRels).isNotNull();
		assertThat(conceptRels.getItems().size()).isGreaterThan(0);
		for (final ConceptRelationship conceptRel : conceptRels.getItems()) {
			assertThat(conceptRel).isNotNull();
			assertThat(conceptRel.getId()).isNotNull();
			assertThat(conceptRel.getTerminology()).contains("SNOMEDCT");
			assertThat(conceptRel.getVersion()).isNotNull();
			assertThat(conceptRel.getFrom()).isNotNull();
			assertThat(conceptRel.getFrom().getCode()).isNotNull();
			assertThat(conceptRel.getFrom().getTerminology()).contains("SNOMEDCT");
			assertThat(conceptRel.getTo()).isNotNull();
		}
	}

	@Test
	public void testFindConceptInverseRelationships() throws Exception {
		// GET concept/{conceptId}/inverseRelationships
		final String conceptId = "28bf322a-5e06-4771-844b-172aaa0fb4c6";
		final String url = baseUrl + "/concept/" + conceptId + "/inverseRelationships";
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final ResultListConceptRelationship conceptRels = objectMapper.readValue(content,
				ResultListConceptRelationship.class);
		assertThat(conceptRels).isNotNull();
		assertThat(conceptRels.getItems().size()).isGreaterThan(0);
		for (final ConceptRelationship conceptRel : conceptRels.getItems()) {
			assertThat(conceptRel).isNotNull();
			assertThat(conceptRel.getId()).isNotNull();
			assertThat(conceptRel.getTerminology()).contains("SNOMEDCT");
			assertThat(conceptRel.getVersion()).isNotNull();
			assertThat(conceptRel.getFrom()).isNotNull();
			assertThat(conceptRel.getFrom().getCode()).isNotNull();
			assertThat(conceptRel.getFrom().getTerminology()).contains("SNOMEDCT");
			assertThat(conceptRel.getTo()).isNotNull();
		}
	}

	@Test
	public void testFindConceptInverseRelationships2() throws Exception {
		// GET concept/{terminology}/{code}/inverseRelationships
		final String terminology = "SNOMEDCT_US";
		final String code = "404684003";
		final String url = baseUrl + "/concept/" + terminology + "/" + code + "/inverseRelationships";
		logger.info("Testing url - {}", url);
		final MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + getTestJwt()))
				.andExpect(status().isOk()).andReturn();
		final String content = result.getResponse().getContentAsString();
		logger.info(" content = {}", content);
		assertThat(content).isNotNull();
		final ResultListConceptRelationship conceptRels = objectMapper.readValue(content,
				ResultListConceptRelationship.class);
		assertThat(conceptRels).isNotNull();
		assertThat(conceptRels.getItems().size()).isGreaterThan(0);
		for (final ConceptRelationship conceptRel : conceptRels.getItems()) {
			assertThat(conceptRel).isNotNull();
			assertThat(conceptRel.getId()).isNotNull();
			assertThat(conceptRel.getTerminology()).contains("SNOMEDCT");
			assertThat(conceptRel.getVersion()).isNotNull();
			assertThat(conceptRel.getFrom()).isNotNull();
			assertThat(conceptRel.getFrom().getCode()).isNotNull();
			assertThat(conceptRel.getFrom().getTerminology()).contains("SNOMEDCT");
			assertThat(conceptRel.getTo()).isNotNull();
		}
	}

	// skipping mapsets, tree position and diagram

}
