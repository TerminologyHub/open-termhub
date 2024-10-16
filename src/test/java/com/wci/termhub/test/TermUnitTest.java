package com.wci.termhub.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.Application;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Term;
import com.wci.termhub.service.impl.EntityServiceImpl;

/**
 * The Class TermUnitTest.
 */
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(OrderAnnotation.class)
public class TermUnitTest extends BaseUnitTest {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(TermUnitTest.class);

	/** The term 1. */
	private final static Term term1 = new Term();

	/** The term 2. */
	private final static Term term2 = new Term();

	/** The term 3. */
	private final static Term term3 = new Term();

	/** The entity service impl. */
	@Autowired
	private EntityServiceImpl<Term, String> entityServiceImpl;

	/** The Constant INDEX_NAME. */
	private static final String INDEX_NAME = Term.class.getCanonicalName();

	/**
	 * Setup.
	 */
	@BeforeAll
	public static void setup() {

		logger.info("Creating object test data");
		// string, FiedType.Text, FieldType.Keyword
		term1.setId("36ab1ce6-4fbb-4f86-a5bb-6974b7aa38f8");
		term1.setName("name-a");
		term1.setTerminology("terminology");
		term1.setVersion("version");
		term1.setPublisher("publisher-a");
		term1.setComponentId("08098098-a");
		term1.setConceptId("12345-a");
		term1.setDescriptorId("876876-a");
		term1.setType("type-a");
		term1.setCode("1234567890");
		// Map of key-value pairs, FiedType.Object
		final Map<String, String> attributes1 = new HashMap<>();
		attributes1.put("key1-a", "value1-a");
		attributes1.put("key2-a", "value2-a");
		term1.setAttributes(attributes1);

		term2.setId("910b9c92-1074-4734-ac2b-3664efb54ac1");
		term2.setName("name-b");
		term2.setTerminology("terminology");
		term2.setVersion("version");
		term2.setPublisher("publisher-b");
		term2.setComponentId("08098098-b");
		term2.setConceptId("12345-b");
		term2.setDescriptorId("876876-b");
		term2.setType("type-b");
		term2.setCode("9876543210");
		// Map of key-value pairs, FiedType.Object
		final Map<String, String> attributes2 = new HashMap<>();
		attributes2.put("key1-b", "value1-b");
		attributes2.put("key2-b", "value2-b");
		term2.setAttributes(attributes2);

		term3.setId("722b9816-3226-40aa-9935-3bcd0ebd47aa");
		term3.setName("dummyname");
		term3.setTerminology("dummyterminology");
		term3.setVersion("dummyversion");
		term3.setPublisher("dummypublisher");
		term3.setComponentId("dummycomponentId");
		term3.setConceptId("dummyconceptId");
		term3.setDescriptorId("dummydescriptorId");
		term3.setType("dummytype");
		term3.setCode("dummycode");
		// Map of key-value pairs, FiedType.Object
		final Map<String, String> attributes3 = new HashMap<>();
		attributes3.put("key1-dummy", "value1-dummy");
		attributes3.put("key2-dummy", "value2-dummy");
		term3.setAttributes(attributes3);
	}

	/**
	 * Checks for document annotation.
	 */
	@Test
	@Order(2)
	public void hasDocumentAnnotation() {

		// check if the class has the Document annotation
		// if not, throw an exception
		// check if the clazz has annotation @Document
		final Term term = new Term();
		final Class<?> clazz1 = term.getClass();
		assertTrue(clazz1.isAnnotationPresent(Document.class));
	}

	/**
	 * Delete index.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(3)
	public void deleteIndex() throws Exception {

		logger.info("Deleting index for Term");
		entityServiceImpl.deleteIndex(Term.class);

		logger.info("Deleted index for Term: {}", INDEX_DIRECTORY + "/" + INDEX_NAME);

		// assert directory does not exist
		assertFalse(Files.exists(Paths.get(INDEX_DIRECTORY, INDEX_NAME)));
	}

	/**
	 * Creates the index.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(4)
	public void createIndex() throws Exception {

		logger.info("Creating index for Term");
		entityServiceImpl.createIndex(Term.class);

		// assert directory exists
		assertTrue(Files.exists(Paths.get(INDEX_DIRECTORY, INDEX_NAME)));
	}

	/**
	 * Test create term.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(5)
	public void testAddTerm() throws Exception {

		logger.info("Creating objects");
		assertDoesNotThrow(() -> entityServiceImpl.add(Term.class, term1));
		assertDoesNotThrow(() -> entityServiceImpl.add(Term.class, term2));
		assertDoesNotThrow(() -> entityServiceImpl.add(Term.class, term3));
	}

	/**
	 * Test find.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(6)
	public void testFind() throws Exception {

		Iterable<Term> foundTermsObjects = null;
		final SearchParameters searchParameters = new SearchParameters();

		// find the term by code
		searchParameters.setQuery("code:" + term1.getCode());
		logger.info("Search for : {}", searchParameters.getQuery());
		foundTermsObjects = entityServiceImpl.find(Term.class, searchParameters);
		assertEquals(1, getSize(foundTermsObjects));

		for (final Object foundTermObject : foundTermsObjects) {
			final Term foundTerm = (Term) foundTermObject;
			logger.info("Term found: {}", foundTerm.toString());
			assertEquals(term1.toString(), foundTerm.toString());
		}

		// now find the term by code
		searchParameters.setQuery("code:" + term2.getCode());
		logger.info("Search for : {}", searchParameters.getQuery());
		foundTermsObjects = entityServiceImpl.find(Term.class, searchParameters);
		assertEquals(1, getSize(foundTermsObjects));

		for (final Object foundTermObject : foundTermsObjects) {
			final Term foundTerm = (Term) foundTermObject;
			logger.info("Term found: {}", foundTerm.toString());
			assertEquals(term2.toString(), foundTerm.toString());
		}

		searchParameters.setQuery("code:1234567*");
		logger.info("Search for : {}", searchParameters.getQuery());
		foundTermsObjects = entityServiceImpl.find(Term.class, searchParameters);
		assertEquals(1, getSize(foundTermsObjects));

		for (final Object foundTermObject : foundTermsObjects) {
			final Term foundTerm = (Term) foundTermObject;
			foundTerm.getAttributes().entrySet().stream().sorted(Map.Entry.comparingByKey());
			assertEquals(term1.toString(), foundTerm.toString());
			logger.info("Term found: {}", foundTerm.toString());
		}

		searchParameters.setQuery("code:" + term1.getCode() + " OR code:" + term2.getCode());
		logger.info("Search for : {}", searchParameters.getQuery());
		foundTermsObjects = entityServiceImpl.find(Term.class, searchParameters);
		assertEquals(2, getSize(foundTermsObjects));

		// add more complex queries
		searchParameters.setQuery("name:" + term3.getName());
		logger.info("Search for : {}", searchParameters.getQuery());
		foundTermsObjects = entityServiceImpl.find(Term.class, searchParameters);
		assertEquals(1, getSize(foundTermsObjects));

		// add more complex queries
		searchParameters.setQuery("code:" + term1.getCode() + " AND name:" + term2.getName());
		logger.info("Search for : {}", searchParameters.getQuery());
		foundTermsObjects = entityServiceImpl.find(Term.class, searchParameters);
		assertEquals(0, getSize(foundTermsObjects));

		// wild card search
		searchParameters.setQuery("*:*");
		foundTermsObjects = entityServiceImpl.find(Term.class, searchParameters);
		assertEquals(3, getSize(foundTermsObjects));

		// search for all
		searchParameters.setQuery(null);
		foundTermsObjects = entityServiceImpl.findAll(Term.class, searchParameters);
		assertEquals(3, getSize(foundTermsObjects));

		// search by id
		searchParameters.setQuery(null);
		final Optional<Term> foundTermOjbect = entityServiceImpl.findById(Term.class, term3.getId());
		assertTrue(foundTermOjbect.isPresent());
		assertEquals(term3.toString(), foundTermOjbect.get().toString());
	}

	/**
	 * Test delete.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(7)
	public void testRemove() throws Exception {

		logger.info("Deleting objects");
		assertDoesNotThrow(() -> entityServiceImpl.remove(Term.class, term1.getId()));
		logger.info("Done deleting");

		// find the term by code
		final SearchParameters searchParameters = new SearchParameters();
		searchParameters.setQuery("id:" + term1.getId());
		final Iterable<Term> foundTermsObjects = entityServiceImpl.find(Term.class, searchParameters);
		logger.info("Found: {}", getSize(foundTermsObjects));
		assertEquals(0, getSize(foundTermsObjects));
	}

}
