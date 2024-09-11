package com.wci.termhub.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.Application;
import com.wci.termhub.lucene.LuceneDataAccess;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.SearchParameters;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(OrderAnnotation.class)
public class ConceptUnitTest {

	/** The logger. */
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ConceptUnitTest.class);

	private static String conceptJson = "{\r\n" + "	\"_index\": \"snomedctus-nlm-20230901-concept-v1\",\r\n"
			+ "	\"_type\": \"_doc\",\r\n" + "	\"_id\": \"b13e76b4-4d22-4e5a-819e-6115f0ce7dd4\",\r\n"
			+ "	\"_score\": 1,\r\n" + "	\"_source\": {\r\n" + "		\"_class\": \"com.wci.termhub.model.Concept\",\r\n"
			+ "		\"normName\": \"blacklegol 7 with spur\",\r\n"
			+ "		\"stemName\": \"blacklegol 7 with spur\",\r\n" + "		\"terms\": [\r\n" + "			{\r\n"
			+ "				\"name\": \"BLACKLEGOL 7 WITH SPUR\",\r\n"
			+ "				\"normName\": \"blacklegol 7 with spur\",\r\n"
			+ "				\"stemName\": \"blacklegol 7 with spur\",\r\n" + "				\"wordCt\": 4,\r\n"
			+ "				\"length\": 22,\r\n" + "				\"terminology\": \"SNOMEDCT_US\",\r\n"
			+ "				\"version\": \"20230901\",\r\n" + "				\"publisher\": \"NLM\",\r\n"
			+ "				\"componentId\": \"163147015\",\r\n" + "				\"code\": \"100005005\",\r\n"
			+ "				\"conceptId\": \"100005005\",\r\n" + "				\"localeMap\": {\r\n"
			+ "					\"en_GB\": true,\r\n" + "					\"en\": true\r\n" + "				},\r\n"
			+ "				\"type\": \"900000000000013009\",\r\n" + "				\"attributes\": {\r\n"
			+ "					\"caseSignificanceId\": \"900000000000017005\",\r\n"
			+ "					\"900000000000490003\": \"900000000000495008\",\r\n"
			+ "					\"moduleId\": \"900000000000207008\"\r\n" + "				},\r\n"
			+ "				\"modified\": \"1012464000000\",\r\n" + "				\"created\": \"1012464000000\",\r\n"
			+ "				\"modifiedBy\": \"loader\",\r\n" + "				\"local\": false,\r\n"
			+ "				\"active\": true,\r\n"
			+ "				\"id\": \"501ac1d3-c925-43b6-a37f-dcbd6f2d6a9a\"\r\n" + "			},\r\n"
			+ "			{\r\n" + "				\"name\": \"BLACKLEGOL 7 WITH SPUR (substance)\",\r\n"
			+ "				\"normName\": \"blacklegol 7 with spur substance\",\r\n"
			+ "				\"stemName\": \"blacklegol 7 with spur substanc\",\r\n" + "				\"wordCt\": 5,\r\n"
			+ "				\"length\": 34,\r\n" + "				\"terminology\": \"SNOMEDCT_US\",\r\n"
			+ "				\"version\": \"20230901\",\r\n" + "				\"publisher\": \"NLM\",\r\n"
			+ "				\"componentId\": \"513544010\",\r\n" + "				\"code\": \"100005005\",\r\n"
			+ "				\"conceptId\": \"100005005\",\r\n" + "				\"localeMap\": {\r\n"
			+ "					\"en\": false\r\n" + "				},\r\n"
			+ "				\"type\": \"900000000000003001\",\r\n" + "				\"attributes\": {\r\n"
			+ "					\"caseSignificanceId\": \"900000000000020002\",\r\n"
			+ "					\"moduleId\": \"900000000000207008\"\r\n" + "				},\r\n"
			+ "				\"modified\": \"1044000000000\",\r\n" + "				\"created\": \"1044000000000\",\r\n"
			+ "				\"modifiedBy\": \"loader\",\r\n" + "				\"local\": false,\r\n"
			+ "				\"active\": false,\r\n"
			+ "				\"id\": \"628c67f3-ef86-4e73-8c6a-782646b3b6ef\"\r\n" + "			},\r\n"
			+ "			{\r\n" + "				\"name\": \"BLACKLEGOL 7 WITH SPUR (product)\",\r\n"
			+ "				\"normName\": \"blacklegol 7 with spur product\",\r\n"
			+ "				\"stemName\": \"blacklegol 7 with spur product\",\r\n" + "				\"wordCt\": 5,\r\n"
			+ "				\"length\": 32,\r\n" + "				\"terminology\": \"SNOMEDCT_US\",\r\n"
			+ "				\"version\": \"20230901\",\r\n" + "				\"publisher\": \"NLM\",\r\n"
			+ "				\"componentId\": \"1473016015\",\r\n" + "				\"code\": \"100005005\",\r\n"
			+ "				\"conceptId\": \"100005005\",\r\n" + "				\"localeMap\": {\r\n"
			+ "					\"en_GB\": true,\r\n" + "					\"en\": true\r\n" + "				},\r\n"
			+ "				\"type\": \"900000000000003001\",\r\n" + "				\"attributes\": {\r\n"
			+ "					\"caseSignificanceId\": \"900000000000017005\",\r\n"
			+ "					\"900000000000490003\": \"900000000000495008\",\r\n"
			+ "					\"moduleId\": \"900000000000207008\"\r\n" + "				},\r\n"
			+ "				\"modified\": \"1059634800000\",\r\n" + "				\"created\": \"1059634800000\",\r\n"
			+ "				\"modifiedBy\": \"loader\",\r\n" + "				\"local\": false,\r\n"
			+ "				\"active\": true,\r\n"
			+ "				\"id\": \"c7a40eb6-4aff-47a7-ab3d-99a47ce5b28d\"\r\n" + "			}\r\n" + "		],\r\n"
			+ "		\"indexTerms\": [],\r\n" + "		\"definitions\": [],\r\n" + "		\"axioms\": [],\r\n"
			+ "		\"ecl\": [\r\n" + "			\"900000000000524003=416516009\",\r\n"
			+ "			\"900000000000524003=*\"\r\n" + "		],\r\n" + "		\"attributes\": {\r\n"
			+ "			\"definitionStatusId\": \"900000000000074008\",\r\n"
			+ "			\"900000000000489007\": \"900000000000487009\",\r\n"
			+ "			\"moduleId\": \"900000000000207008\"\r\n" + "		},\r\n" + "		\"highlights\": {},\r\n"
			+ "		\"semanticTypes\": [],\r\n" + "		\"labels\": [],\r\n" + "		\"children\": [],\r\n"
			+ "		\"parents\": [],\r\n" + "		\"descendants\": [],\r\n" + "		\"ancestors\": [],\r\n"
			+ "		\"relationships\": [],\r\n" + "		\"inverseRelationships\": [],\r\n"
			+ "		\"treePositions\": [],\r\n" + "		\"name\": \"BLACKLEGOL 7 WITH SPUR\",\r\n"
			+ "		\"code\": \"100005005\",\r\n" + "		\"terminology\": \"SNOMEDCT_US\",\r\n"
			+ "		\"version\": \"20230901\",\r\n" + "		\"publisher\": \"NLM\",\r\n" + "		\"leaf\": true,\r\n"
			+ "		\"defined\": false,\r\n" + "		\"modified\": \"1249023600000\",\r\n"
			+ "		\"created\": \"1249023600000\",\r\n" + "		\"modifiedBy\": \"loader\",\r\n"
			+ "		\"local\": false,\r\n" + "		\"active\": false,\r\n"
			+ "		\"id\": \"b13e76b4-4d22-4e5a-819e-6115f0ce7dd4\"\r\n" + "	}\r\n" + "}}\r\n" + "}";

	private static Concept concept;

	/** The Constant INDEX_DIRECTORY. */
	private static final String INDEX_DIRECTORY = "C:\\tmp\\index"; // ./build/index";

	/**
	 * Delete index.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(1)
	public void deleteIndex() throws Exception {

		final LuceneDataAccess<Concept> luceneData = new LuceneDataAccess<>();
		logger.info("Deleting index for Concept");
		luceneData.deleteIndex(Concept.class);

		// assert directory does not exist
		assertFalse(Files.exists(Paths.get(INDEX_DIRECTORY, Concept.class.getCanonicalName())));

	}

	/**
	 * Creates the index.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(2)
	public void createIndex() throws Exception {

		final LuceneDataAccess<Concept> luceneData = new LuceneDataAccess<>();
		logger.info("Creating index for Concept");
		luceneData.createIndex(Concept.class);

		// test if directory exists
		assertTrue(Files.exists(Paths.get(INDEX_DIRECTORY, Concept.class.getCanonicalName())));

	}

	@Test
	@Order(3)
	public void testAddConcept() throws Exception {

		final LuceneDataAccess<Concept> luceneData = new LuceneDataAccess<>();
		final ObjectMapper objectMapper = new ObjectMapper();
		final JsonNode rootNode = objectMapper.readTree(conceptJson);
		final JsonNode conceptNode = rootNode.get("_source");

		if (conceptNode != null) {
			concept = objectMapper.treeToValue(conceptNode, Concept.class);
			logger.info("Concept: {}", concept.toString());
			assertDoesNotThrow(() -> luceneData.add(concept));
		} else {
			logger.error("No '_source' node found in the provided JSON.");
		}

	}

	@Test
	@Order(4)
	public void findConcept() throws Exception {

		final LuceneDataAccess<Concept> luceneData = new LuceneDataAccess<>();
		Iterable<Concept> foundConceptObjects = null;
		final SearchParameters searchParameters = new SearchParameters();

		searchParameters.setQuery("code:100005005");
		logger.info("Search for : {}", searchParameters.getQuery());
		foundConceptObjects = luceneData.find(Concept.class, searchParameters);
		assertTrue(getSize(foundConceptObjects) == 1);

		for (final Object foundConceptObject : foundConceptObjects) {
			final Concept foundConcept = (Concept) foundConceptObject;
			logger.info("Concept found: {}", foundConcept.toString());
			assertEquals(concept.toString(), foundConcept.toString());
		}

	}

	/**
	 * Gets the size.
	 *
	 * @param <T>      the generic type
	 * @param iterable the iterable
	 * @return the size
	 */
	@SuppressWarnings("unused")
	private static <T> int getSize(final Iterable<T> iterable) {
		if (iterable instanceof Collection) {
			return ((Collection<T>) iterable).size();
		} else {
			int size = 0;
			for (final T item : iterable) {
				size++;
			}
			return size;
		}
	}

}
