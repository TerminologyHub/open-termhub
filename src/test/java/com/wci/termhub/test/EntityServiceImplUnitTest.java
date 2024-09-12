package com.wci.termhub.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.Application;
import com.wci.termhub.model.BaseModel;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.repository.EntityRepository;
import com.wci.termhub.service.impl.EntityServiceImpl;

@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(OrderAnnotation.class)
public class EntityServiceImplUnitTest {

	/** The logger. */
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(EntityServiceImplUnitTest.class);

	@Mock
	private EntityRepository entityRepository;

	@InjectMocks
	private EntityServiceImpl<BaseModel, String> entityServiceImpl;

	private final TestDocumentObject documentObj1 = new TestDocumentObject("1", "1000", "one", "1 description");
	private final TestDocumentObject documentObj2 = new TestDocumentObject("2", "2000", "two", "2 description");
	private final TestDocumentObject documentObj3 = new TestDocumentObject("3", "3000", "three", "3 description");
	private final TestDocumentObject documentObj4 = new TestDocumentObject("4", "4000", "four", "4 description");

//	@BeforeEach
//	public void setup() {
//		MockitoAnnotations.openMocks(this);
//	}

	@Test
	@Order(1)
	public void testCreateIndex() {

		logger.info("Testing CreateIndex");

		// Object without @Document annotation
		assertThrows(IllegalArgumentException.class, () -> entityServiceImpl.createIndex(TestNoDocumentObject.class));
		assertDoesNotThrow(() -> entityServiceImpl.createIndex(TestDocumentObject.class));
	}

	@Test
	@Order(2)
	public void testDeleteIndex() {

		logger.info("Testing DeleteIndex");

		assertThrows(IllegalArgumentException.class, () -> entityServiceImpl.deleteIndex(TestNoDocumentObject.class));
		assertDoesNotThrow(() -> entityServiceImpl.deleteIndex(TestDocumentObject.class));

	}

	@Test
	@Order(3)
	public void testAdd() {

		logger.info("Testing Add");

		final TestNoDocumentObject noDocumentObj = new TestNoDocumentObject();
		assertThrows(IllegalArgumentException.class,
				() -> entityServiceImpl.add(TestNoDocumentObject.class, noDocumentObj));

		assertDoesNotThrow(() -> entityServiceImpl.createIndex(TestDocumentObject.class));

		assertDoesNotThrow(() -> entityServiceImpl.add(documentObj1.getClass(), documentObj1));
		assertDoesNotThrow(() -> entityServiceImpl.add(documentObj2.getClass(), documentObj2));
		assertDoesNotThrow(() -> entityServiceImpl.add(documentObj3.getClass(), documentObj3));
		assertDoesNotThrow(() -> entityServiceImpl.add(documentObj4.getClass(), documentObj4));

	}

	@Test
	@Order(4)
	public void testFind() {

		logger.info("Testing Find");

		final SearchParameters searchParameters = new SearchParameters();
		searchParameters.setQuery("name:one");

		assertThrows(IllegalArgumentException.class,
				() -> entityServiceImpl.find(TestNoDocumentObject.class, searchParameters));

		try {

			final Iterable<BaseModel> result = entityServiceImpl.find(TestDocumentObject.class, searchParameters);
			assertNotNull(result);
			assertTrue(result.iterator().hasNext());
			final TestDocumentObject documentObj = (TestDocumentObject) result.iterator().next();
			assertEquals(documentObj1, documentObj);

			int count = 0;
			for (final BaseModel object : result) {
				count++;
				logger.debug("TestDocumentObject found: {}", object);
			}
			assertTrue(count == 1);

		} catch (final Exception e) {
			logger.error("Error finding document", e);
			fail("Error finding document");
		}
	}

	@Test
	@Order(5)
	public void testFindById() {

		logger.info("Testing Find By Id");

		final String documentId = "4";
		assertThrows(IllegalArgumentException.class,
				() -> entityServiceImpl.findById(TestNoDocumentObject.class, documentId));

		try {

			final Optional<BaseModel> result = entityServiceImpl.findById(TestDocumentObject.class, documentId);
			assertNotNull(result);
			logger.info("Result: {}", result);
			assertTrue(result.isPresent());
			final TestDocumentObject documentObj = (TestDocumentObject) result.get();
			assertEquals(documentObj4, documentObj);
			assertEquals(documentId, documentObj.getId());

		} catch (final Exception e) {
			logger.error("Error finding document by id", e);
			fail("Error finding document by id");
		}
	}

	@Test
	@Order(6)
	public void testFindAll() {

		logger.info("Testing Find All");

		final SearchParameters searchParameters = new SearchParameters();
		assertThrows(IllegalArgumentException.class,
				() -> entityServiceImpl.findAll(TestNoDocumentObject.class, searchParameters));

		try {

			final Iterable<BaseModel> result = entityServiceImpl.findAll(TestDocumentObject.class, searchParameters);
			assertNotNull(result);
			assertTrue(result.iterator().hasNext());
			int count = 0;
			for (final BaseModel object : result) {
				count++;
				logger.debug("TestDocumentObject found: {}", object);
			}
			assertTrue(count == 4);
		}

		catch (final Exception e) {
			logger.error("Error finding all documents", e);
			fail("Error finding all documents");
		}
	}

	@Test
	@Order(7)
	public void testCombinedQuery() {
		final SearchParameters searchParameters = new SearchParameters();
		searchParameters.setQuery("name:one OR name:two");

		try {

			final Iterable<BaseModel> result = entityServiceImpl.find(TestDocumentObject.class, searchParameters);
			assertNotNull(result);
			assertTrue(result.iterator().hasNext());
			int count = 0;
			for (final BaseModel object : result) {
				count++;
				logger.debug("TestDocumentObject found: {}", object);
			}
			assertTrue(count == 2);
		}

		catch (final Exception e) {
			logger.error("Error finding all documents", e);
			fail("Error finding all documents");
		}
	}

	@Test
	@Order(8)
	public void testRemove() {

		final String documentId = "4";
		assertDoesNotThrow(() -> entityServiceImpl.remove(TestDocumentObject.class, documentId));

		final SearchParameters searchParameters = new SearchParameters();
		try {

			final Optional<BaseModel> result = entityServiceImpl.findById(TestDocumentObject.class, documentId);
			assertNotNull(result);
			logger.info("Result: {}", result);
			assertFalse(result.isPresent());

			final Iterable<BaseModel> resultAll = entityServiceImpl.findAll(TestDocumentObject.class, searchParameters);
			assertNotNull(resultAll);
			assertTrue(resultAll.iterator().hasNext());
			int count = 0;
			for (final BaseModel object : resultAll) {
				count++;
			}
			assertTrue(count == 3);
		}

		catch (final Exception e) {
			logger.error("Error finding all documents", e);
			fail("Error finding all documents");
		}

	}

}
