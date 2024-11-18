/*
 *
 */
package com.wci.termhub.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.Application;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * The Class Concept2UnitTest.
 */
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class Concept2UnitTest {

	/** The logger. */
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(Concept2UnitTest.class);

	/** The Constant SEARCH_PARAMETERS. */
	private static final SearchParameters SEARCH_PARAMETERS = new SearchParameters(1000, 0);

	/** The search service. */
	@Autowired
	private EntityRepositoryService searchService;

	/**
	 * Test find concept icd 10 cm.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConceptIcd10cm() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:ICD10CM AND version:2023 AND code:E10");
		final ResultList<Concept> concept = searchService.find(SEARCH_PARAMETERS, Concept.class);
		assertEquals(1, concept.getItems().size());
		assertEquals("E10", concept.getItems().get(0).getCode());
		assertEquals("ICD10CM", concept.getItems().get(0).getTerminology());
		assertEquals("2023", concept.getItems().get(0).getVersion());
	}

	/**
	 * Test find concept loinc.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConceptLoinc() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:LNC AND version:277 AND code:LA14283-8");
		final ResultList<Concept> concept = searchService.find(SEARCH_PARAMETERS, Concept.class);
		assertEquals(1, concept.getItems().size());
		assertEquals("LA14283-8", concept.getItems().get(0).getCode());
		assertEquals("LNC", concept.getItems().get(0).getTerminology());
		assertEquals("277", concept.getItems().get(0).getVersion());
	}

	/**
	 * Test find concept rx norm.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConceptRxNorm() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:RXNORM AND version:02052024 AND code:899989");
		final ResultList<Concept> concept = searchService.find(SEARCH_PARAMETERS, Concept.class);
		assertEquals(1, concept.getItems().size());
		assertEquals("899989", concept.getItems().get(0).getCode());
		assertEquals("RXNORM", concept.getItems().get(0).getTerminology());
		assertEquals("02052024", concept.getItems().get(0).getVersion());
	}

	/**
	 * Test find concept snomed ct us.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConceptSnomedCtUs() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:SNOMEDCT_US AND version:20240301 AND code:384719006");
		final ResultList<Concept> concept = searchService.find(SEARCH_PARAMETERS, Concept.class);
		assertEquals(1, concept.getItems().size());
		assertEquals("384719006", concept.getItems().get(0).getCode());
		assertEquals("SNOMEDCT_US", concept.getItems().get(0).getTerminology());
		assertEquals("20240301", concept.getItems().get(0).getVersion());
	}

	/**
	 * Test find concept snomed ct.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConceptSnomedCt() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:SNOMEDCT AND version:20240101 AND code:277302009");
		final ResultList<Concept> concept = searchService.find(SEARCH_PARAMETERS, Concept.class);
		assertEquals(1, concept.getItems().size());
		assertEquals("277302009", concept.getItems().get(0).getCode());
		assertEquals("SNOMEDCT", concept.getItems().get(0).getTerminology());
		assertEquals("20240101", concept.getItems().get(0).getVersion());
	}

}
