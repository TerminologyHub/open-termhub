package com.wci.termhub.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
public class EntityServiceImplUnitTest {

	/** The logger. */
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(EntityServiceImplUnitTest.class);

	@Mock
	private EntityRepository entityRepository;

	@InjectMocks
	private EntityServiceImpl<BaseModel, String> entityServiceImpl;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testCreateIndex() {

		final Class<TestDocumentObject> documentClass = TestDocumentObject.class;
		assertDoesNotThrow(() -> entityServiceImpl.createIndex(documentClass));

		// Object without @Document annotation
		final Class<TestNoDocumentObject> noDocumentClass = TestNoDocumentObject.class;
		assertThrows(IllegalArgumentException.class, () -> entityServiceImpl.createIndex(noDocumentClass));
	}

	@Test
	public void testDeleteIndex() {

		final Class<TestDocumentObject> documentClass = TestDocumentObject.class;
		assertDoesNotThrow(() -> entityServiceImpl.deleteIndex(documentClass));
	}

	@Test
	public void testAdd() {

		final TestDocumentObject documentClass = new TestDocumentObject();
		documentClass.setName("name");
		documentClass.setDescription("description");

		assertDoesNotThrow(() -> entityServiceImpl.add(documentClass, documentClass.getClass()));
	}

	@Test
	public void testFind() {
		final SearchParameters searchParameters = new SearchParameters();
		final Class<TestDocumentObject> clazz = TestDocumentObject.class;

		try {
			final Iterable<BaseModel> result = entityServiceImpl.find(clazz, searchParameters);

			assertNotNull(result);
			assertFalse(result.iterator().hasNext());

			logger.info("Result: " + result.toString());

		} catch (final Exception e) {
			logger.error("Error finding document", e);
		}
	}

	// TODO: Implement the following test methods
//	@Test
//	public void testFindAll() {
//
//		final TestDocumentObject documentClass = new TestDocumentObject();
//		Iterable<Object> result = entityServiceImpl.findAll(documentClass.getClass());
//		assertNull(result);
//	}
//
//	@Test
//	public void testFindById() {
//		String id = "1";
//		Class<Object> clazz = Object.class;
//
//		Optional<Object> result = entityServiceImpl.findById(id, clazz);
//
//		assertNotNull(result);
//		assertFalse(result.isPresent());
//	}
//

//
//	@Test
//	public void testDelete() {
//		Class<Object> clazz = Object.class;
//		Object entity = new Object();
//
//		assertDoesNotThrow(() -> entityServiceImpl.delete(clazz, entity));
//	}

}
