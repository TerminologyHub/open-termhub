/*
 *
 */
package com.wci.termhub.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.Application;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Term;
import com.wci.termhub.service.EntityRepositoryService;

@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class TermSearchUnitTest {

	/** The logger. */
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(TermSearchUnitTest.class);

	/** The Constant SEARCH_PARAMETERS. */
	private static final SearchParameters SEARCH_PARAMETERS = new SearchParameters(1000, 0);

	/** The search service. */
	@Autowired
	private EntityRepositoryService searchService;

	@Test
	public void testFindConceptIcd10cm() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:ICD10CM AND version:2023 AND code:C50");
		final ResultList<Term> terms = searchService.find(SEARCH_PARAMETERS, Term.class);
		assertEquals(4, terms.getItems().size());
		terms.getItems().forEach(term -> {
			assertEquals("C50", term.getCode());
			assertEquals("ICD10CM", term.getTerminology());
			assertEquals("2023", term.getVersion());
		});
	}

	/**
	 * Test find concept loinc.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConceptLoinc() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:LNC AND version:277 AND code:LA14283-8");
		final ResultList<Term> terms = searchService.find(SEARCH_PARAMETERS, Term.class);
		assertEquals(1, terms.getItems().size());
		terms.getItems().forEach(term -> {
			assertEquals("LA14283-8", term.getCode());
			assertEquals("LNC", term.getTerminology());
			assertEquals("277", term.getVersion());
		});
	}

	/**
	 * Test find concept rx norm.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConceptRxNorm() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:RXNORM AND version:02052024 AND code:899989");
		final ResultList<Term> terms = searchService.find(SEARCH_PARAMETERS, Term.class);
		assertEquals(5, terms.getItems().size());
		terms.getItems().forEach(term -> {
			assertEquals("899989", term.getCode());
			assertEquals("RXNORM", term.getTerminology());
			assertEquals("02052024", term.getVersion());
		});

	}

	/**
	 * Test find concept snomed ct us.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConceptSnomedCtUs() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:SNOMEDCT_US AND version:20240301 AND code:384719006");
		final ResultList<Term> terms = searchService.find(SEARCH_PARAMETERS, Term.class);
		assertEquals(2, terms.getItems().size());
		terms.getItems().forEach(term -> {
			assertEquals("384719006", term.getCode());
			assertEquals("SNOMEDCT_US", term.getTerminology());
			assertEquals("20240301", term.getVersion());
		});

	}

	/**
	 * Test find concept snomed ct.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConceptSnomedCt() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:SNOMEDCT AND version:20240101 AND code:277302009");
		final ResultList<Term> terms = searchService.find(SEARCH_PARAMETERS, Term.class);
		assertEquals(2, terms.getItems().size());
		terms.getItems().forEach(term -> {
			assertEquals("277302009", term.getCode());
			assertEquals("SNOMEDCT", term.getTerminology());
			assertEquals("20240101", term.getVersion());
		});

	}

	/**
	 * Test find concept by term.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindConceptByTermName() throws Exception {

		final String term = "Procedure on gastrointestinal tract";
		final SearchParameters sp = new SearchParameters(
				"terminology:SNOMEDCT_US AND version:20240301 AND name:" + term, 100, 0);

		final ResultList<Term> results = searchService.find(sp, Term.class);

		for (final Term t : results.getItems()) {
			LOG.info(t.toString());
		}

		// check that each concept has at least a term has the word 'Procedure on
		// gastrointestinal tract' in it
		results.getItems().forEach(t -> {
			assertTrue(t.getName().toLowerCase().contains(term));
		});

	}

	/**
	 * Test find test paging.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindTestPaging() throws Exception {

		final String term = "heart";
		final SearchParameters sp = new SearchParameters(
				"terminology:SNOMEDCT_US AND version:20240301 AND name:" + term, 10, 0);
		final List<String> terms = new ArrayList<>();

		ResultList<Term> results = searchService.find(sp, Term.class);
		assertEquals(10, results.getItems().size());
		results.getItems().forEach(t -> {
			assertEquals("SNOMEDCT_US", t.getTerminology());
			assertEquals("20240301", t.getVersion());
			assertFalse(terms.contains(t.getCode() + "_" + t.getName()));
			terms.add(t.getCode() + "_" + t.getName());
		});

		sp.setOffset(10);
		results = searchService.find(sp, Term.class);
		LOG.info("result size:{}", results.getItems().size());
		assertEquals(10, results.getItems().size());
		results.getItems().forEach(t -> {
			LOG.info("term:{}", t);
			assertEquals("SNOMEDCT_US", t.getTerminology());
			assertEquals("20240301", t.getVersion());
			assertFalse(terms.contains(t.getCode() + "_" + t.getName()));
			terms.add(t.getCode() + "_" + t.getName());
		});

		results.getItems().forEach(t -> {
			terms.add(t.getName());
		});

		sp.setOffset(20);
		results = searchService.find(sp, Term.class);
		assertEquals(6, results.getItems().size());
		results.getItems().forEach(t -> {
			assertEquals("SNOMEDCT_US", t.getTerminology());
			assertEquals("20240301", t.getVersion());
			assertFalse(terms.contains(t.getCode() + "_" + t.getName()));
			terms.add(t.getCode() + "_" + t.getName());
		});
	}

}
