package com.wci.termhub.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.Application;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.impl.EntityServiceImpl;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(OrderAnnotation.class)
public class ConceptRelationshipUnitTest extends BaseUnitTest {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(ConceptRelationshipUnitTest.class);

	/** The concept relationship json. */
	private static final String CONCEPT_RELATIONSHIP_JSON = """
			{
				"_index": "snomedctus-nlm-20230901-concept-relationship-v1",
				"_type": "_doc",
				"_id": "84234204-cfc9-4258-a07b-9c13cf02b70b",
				"_score": 1,
				"_source": {
					"_class": "com.wci.termhub.model.ConceptRelationship",
					"terminology": "SNOMEDCT_US",
					"version": "20230901",
					"publisher": "NLM",
					"componentId": "88aa17e4-4971-5dfa-9643-1a97ca3539a5",
					"type": "other",
					"additionalType": "900000000000524003",
					"from": {
						"name": "EFA LIQUID",
						"code": "100476003",
						"terminology": "SNOMEDCT_US",
						"version": "20230901",
						"publisher": "NLM",
						"leaf": true,
						"defined": false,
						"local": false,
						"active": false
					},
					"to": {
						"name": "Extension Namespace 1000009",
						"code": "416516009",
						"terminology": "SNOMEDCT_US",
						"version": "20230901",
						"publisher": "NLM",
						"leaf": true,
						"defined": false,
						"local": false,
						"active": true
					},
					"historical": true,
					"asserted": true,
					"attributes": {
						"moduleId": "900000000000207008"
					},
					"modified": "1249023600000",
					"created": "1249023600000",
					"modifiedBy": "loader",
					"local": false,
					"active": true,
					"id": "84234204-cfc9-4258-a07b-9c13cf02b70b"
				}
			}
				""";

	/** The entity service impl. */
	@Autowired
	private EntityServiceImpl<ConceptRelationship, String> entityServiceImpl;

	/** The concept. */
	private static ConceptRelationship conceptRelationship;

	/** The Constant INDEX_DIRECTORY. */

	private static final String INDEX_DIRECTORY = "C:\\tmp\\index"; // "./build/index";

	/**
	 * Delete index.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(1)
	public void deleteIndex() throws Exception {

		logger.info("Deleting index for Concept Relationship");
		entityServiceImpl.deleteIndex(ConceptRelationship.class);

		// assert directory does not exist
		assertFalse(Files.exists(Paths.get(INDEX_DIRECTORY, ConceptRelationship.class.getCanonicalName())));
	}

	/**
	 * Creates the index.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(2)
	public void createIndex() throws Exception {

		logger.info("Creating index for Concept Relationship");
		entityServiceImpl.createIndex(ConceptRelationship.class);

		// test if directory exists
		assertTrue(Files.exists(Paths.get(INDEX_DIRECTORY, ConceptRelationship.class.getCanonicalName())));
	}

	/**
	 * Test add concept.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(3)
	public void testAddConceptRelationship() throws Exception {

		final ObjectMapper objectMapper = new ObjectMapper();
		final JsonNode rootNode = objectMapper.readTree(CONCEPT_RELATIONSHIP_JSON);
		final JsonNode conceptRelNode = rootNode.get("_source");

		if (conceptRelNode != null) {
			conceptRelationship = objectMapper.treeToValue(conceptRelNode, ConceptRelationship.class);
			logger.info("Concept Relationship: {}", conceptRelationship.toString());
			assertDoesNotThrow(() -> entityServiceImpl.add(ConceptRelationship.class, conceptRelationship));
		} else {
			logger.error("No '_source' node found in the provided JSON.");
		}
	}

//	/**
//	 * Find concept by code.
//	 *
//	 * @throws Exception the exception
//	 */
//	@Test
//	@Order(4)
//	public void findConceptRelationshipById() throws Exception {
//
//		final SearchParameters searchParameters = new SearchParameters();
//		searchParameters.setQuery("id:84234204-cfc9-4258-a07b-9c13cf02b70b");
//		logger.info("Search for : {}", searchParameters.getQuery());
//
//		final Iterable<ConceptRelationship> foundConceptRelObjects = LUCENE_DATA.find(ConceptRelationship.class,
//				searchParameters);
//		assertEquals(1, getSize(foundConceptRelObjects));
//
//		for (final Object foundConceptObject : foundConceptRelObjects) {
//			final ConceptRelationship foundConceptRel = (ConceptRelationship) foundConceptObject;
//			logger.info("Concept Relationship found: {}", foundConceptRel.toString());
//			assertEquals(conceptRelationship.toString(), foundConceptRel.toString());
//		}
//	}

	/**
	 * Find concept by code.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(5)
	public void findConceptRelationshipByFromCode() throws Exception {

		final SearchParameters searchParameters = new SearchParameters();
		searchParameters.setQuery("from.code:100476003");
		logger.info("Search for : {}", searchParameters.getQuery());

		final Iterable<ConceptRelationship> foundConceptRelObjects = entityServiceImpl.find(ConceptRelationship.class,
				searchParameters);
		assertEquals(1, getSize(foundConceptRelObjects));

		for (final Object foundConceptObject : foundConceptRelObjects) {
			final ConceptRelationship foundConceptRel = (ConceptRelationship) foundConceptObject;
			logger.info("Concept Relationship found: {}", foundConceptRel.toString());
			assertEquals(conceptRelationship.toString(), foundConceptRel.toString());
		}
	}

}
