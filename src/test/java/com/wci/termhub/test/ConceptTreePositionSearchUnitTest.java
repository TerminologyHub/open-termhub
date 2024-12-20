/*
 *
 */
package com.wci.termhub.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.Application;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptTreePosition;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * The Class ConceptTreePositionSearchUnitTest.
 */
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ConceptTreePositionSearchUnitTest {

	/** The logger. */
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(ConceptTreePositionSearchUnitTest.class);

	/** The Constant SEARCH_PARAMETERS. */
	private static final SearchParameters SEARCH_PARAMETERS = new SearchParameters(100000, 0);

	/** The search service. */
	@Autowired
	private EntityRepositoryService searchService;

	// Just logs the concept tree positions
	// @Test
	public void testLogAllConceptTreePositions() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:SNOMEDCT_US AND publisher:SANDBOX AND version:20240301");
		final ResultList<ConceptTreePosition> conceptTreePositions1 = searchService.find(SEARCH_PARAMETERS,
				ConceptTreePosition.class);
		for (final ConceptTreePosition ctp : conceptTreePositions1.getItems()) {
			LOG.info("QQQ terminology:{}, code:{}, ancestorPath:{} ", ctp.getTerminology(), ctp.getConcept().getCode(),
					ctp.getAncestorPath());
		}

		SEARCH_PARAMETERS.setQuery("terminology:SNOMEDCT AND publisher:SANDBOX AND version:20240301");
		final ResultList<ConceptTreePosition> conceptTreePositions2 = searchService.find(SEARCH_PARAMETERS,
				ConceptTreePosition.class);
		for (final ConceptTreePosition ctp : conceptTreePositions2.getItems()) {
			LOG.info("QQQ terminology:{}, code:{}, ancestorPath:{} ", ctp.getTerminology(), ctp.getConcept().getCode(),
					ctp.getAncestorPath());
		}

		assertTrue(false);
	}

	/**
	 * Test find concept.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConcept() throws Exception {

		SEARCH_PARAMETERS
				.setQuery("terminology:SNOMEDCT_US AND publisher:SANDBOX AND version:20240301 AND code:52988006");
		final ResultList<Concept> concepts = searchService.find(SEARCH_PARAMETERS, Concept.class);
		assertEquals(1, concepts.getItems().size());
	}

	/**
	 * Test find concept tree position for SNOMEDCT_US concept.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConceptTreePositions() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:SNOMEDCT_US AND publisher:SANDBOX AND version:20240301");
		final ResultList<ConceptTreePosition> conceptTreePositions = searchService.find(SEARCH_PARAMETERS,
				ConceptTreePosition.class);
		assertTrue(conceptTreePositions.getItems().size() > 10);
		for (final ConceptTreePosition ctp : conceptTreePositions.getItems()) {
			assertEquals("SNOMEDCT_US", ctp.getTerminology());
			assertEquals("SANDBOX", ctp.getPublisher());
			assertEquals("20240301", ctp.getVersion());
		}

	}

	/**
	 * Test find concept tree position for SNOMEDCT_US concept.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConceptTreePositionsForCode() throws Exception {

		SEARCH_PARAMETERS.setQuery(
				"terminology:SNOMEDCT_US AND publisher:SANDBOX AND version:20240301 AND concept.code:52988006");
		final ResultList<ConceptTreePosition> conceptTreePositions = searchService.find(SEARCH_PARAMETERS,
				ConceptTreePosition.class);
		assertEquals(2, conceptTreePositions.getItems().size());
		for (final ConceptTreePosition ctp : conceptTreePositions.getItems()) {
			assertEquals("SNOMEDCT_US", ctp.getTerminology());
			assertEquals("SANDBOX", ctp.getPublisher());
			assertEquals("20240301", ctp.getVersion());
			assertEquals("52988006", ctp.getConcept().getCode());
			// 1 is empty the other has a ancestor path
			// assertNotNull(ctp.getAncestorPath());
		}

	}

	/**
	 * Test find concept tree position for SNOMEDCT_US concept.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConceptTreePositionsWithEmptyAncestor() throws Exception {

		SEARCH_PARAMETERS.setQuery(
				"terminology:SNOMEDCT_US AND publisher:SANDBOX AND version:20240301 AND concept.code:52988006 AND ancestorPath:\"\"");
		final ResultList<ConceptTreePosition> conceptTreePositions = searchService.find(SEARCH_PARAMETERS,
				ConceptTreePosition.class);
		assertEquals(1, conceptTreePositions.getItems().size());
		for (final ConceptTreePosition ctp : conceptTreePositions.getItems()) {
			assertEquals("SNOMEDCT_US", ctp.getTerminology());
			assertEquals("SANDBOX", ctp.getPublisher());
			assertEquals("20240301", ctp.getVersion());
			assertEquals(null, ctp.getAncestorPath());
		}

	}

	/**
	 * Test find concept tree position for SNOMEDCT_US concept.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConceptTreePositionsWithAncestor() throws Exception {

		final String ancestorPath = "138875005~123037004~118956008~49755003";
		SEARCH_PARAMETERS.setQuery(
				"terminology:SNOMEDCT_US AND publisher:SANDBOX AND version:20240301 AND concept.code:52988006 AND ancestorPath:\""
						+ ancestorPath + "\"");
		final ResultList<ConceptTreePosition> conceptTreePositions = searchService.find(SEARCH_PARAMETERS,
				ConceptTreePosition.class);
		assertEquals(1, conceptTreePositions.getItems().size());
		for (final ConceptTreePosition ctp : conceptTreePositions.getItems()) {
			assertEquals("SNOMEDCT_US", ctp.getTerminology());
			assertEquals("SANDBOX", ctp.getPublisher());
			assertEquals("20240301", ctp.getVersion());
			assertEquals(ancestorPath, ctp.getAncestorPath());
		}

	}

	//

}
