/*
 *
 */
package com.wci.termhub.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.Application;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * The Class TerminologyUnitTest.
 */
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(OrderAnnotation.class)
public class TerminologyUnitTest extends BaseUnitTest {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(TerminologyUnitTest.class);

	private static final String TERMINOLOGY_JSON = """
			{
				"id": "04efd633-bcbc-41cd-959c-f5ed8d94adaa",
				"confidence": 0.6931471228599548,
				"modified": "2024-10-16T11:01:33.921-07:00",
				"created": "2024-10-16T11:01:33.921-07:00",
				"modifiedBy": "loader",
				"local": false,
				"active": true,
				"abbreviation": "ICD10CM",
				"name": "International Classification of Diseases, Tenth Revision, Clinical Modification",
				"version": "2023",
				"publisher": "NLM",
				"releaseDate": "2023-05-01",
				"family": "ICD10CM",
				"indexName": "icd10cm-nlm-2023",
				"attributes": {
					"origin-version": "2023AA",
					"autocomplete": "true",
					"hierarchical": "true",
					"fhirVersion": "2023",
					"tree-positions": "true",
					"origin-terminology": "UMLS",
					"ecl": "true",
					"fhirId": "icd10cm_2023",
					"fhirCompositional": "false"
				},
				"roots": [
					"ICD-10-CM"
				],
				"statistics": {
					"termsInactive": 0,
					"childrenInactive": 0,
					"relationships": 18,
					"concepts": 10,
					"terms": 22,
					"parentsInactive": 0,
					"termsActive": 22,
					"parentsActive": 9,
					"definitions": 0,
					"treePositions": 10,
					"conceptsActive": 10,
					"childrenActive": 9
				}
			}
				""";

	/** The search service. */
	@Autowired
	private EntityRepositoryService searchService;

	private static Terminology terminology;

	private static final String INDEX_NAME = Terminology.class.getCanonicalName();

	/**
	 * Creates the index.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(1)
	public void createIndex() throws Exception {

		logger.info("Creating index for Terminology");
		searchService.createIndex(Terminology.class);

		// test if directory exists
		assertTrue(Files.exists(Paths.get(INDEX_DIRECTORY, INDEX_NAME)));
	}

	/**
	 * Test add terminology.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(2)
	public void testAddTerminology() throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		final JsonNode terminologyNode = objectMapper.readTree(TERMINOLOGY_JSON);

		if (terminologyNode != null) {
			terminology = objectMapper.treeToValue(terminologyNode, Terminology.class);
			logger.info("Terminology: {}", terminology.toString());
			assertDoesNotThrow(() -> searchService.add(Terminology.class, terminology));
		} else {
			logger.error("No '_source' node found in the provided JSON.");
		}
	}

//	/**
//	 * Delete index.
//	 *
//	 * @throws Exception the exception
//	 */
//	@Test
//	@Order(3)
//	public void deleteIndex() throws Exception {
//
//		logger.info("Deleting index for Terminology from {}", Paths.get(INDEX_DIRECTORY, INDEX_NAME).toString());
//		searchService.deleteIndex(Terminology.class);
//
//		// assert directory does not exist
//		assertFalse(Files.exists(Paths.get(INDEX_DIRECTORY, INDEX_NAME)));
//	}

}
