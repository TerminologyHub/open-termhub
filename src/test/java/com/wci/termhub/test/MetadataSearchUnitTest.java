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
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * The Class MetadataSearchUnitTest.
 */
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class MetadataSearchUnitTest {

	/** The logger. */
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(MetadataSearchUnitTest.class);

	/** The search service. */
	@Autowired
	private EntityRepositoryService searchService;

	/** The Constant SEARCH_PARAMETERS. */
	private static final SearchParameters SEARCH_PARAMETERS = new SearchParameters(1000, 0);

	/**
	 * Test find all.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindAll() throws Exception {

		final ResultList<Metadata> all = searchService.findAll(SEARCH_PARAMETERS, Metadata.class);
		LOG.info("all size: {}", all.getItems().size());

	}

	/**
	 * Test find metadata by terminology icd 10 cm.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindMetadataByTerminologyIcd10cm() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:ICD10CM AND version:2023");
		final ResultList<Metadata> metadata = searchService.find(SEARCH_PARAMETERS, Metadata.class);
		assertEquals(15, metadata.getItems().size());
		metadata.getItems().forEach(m -> {
			assertEquals("ICD10CM", m.getTerminology());
			assertEquals("2023", m.getVersion());
		});
	}

	/**
	 * Test find metadata by terminology loinc.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindMetadataByTerminologyLoinc() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:LNC AND version:277");
		final ResultList<Metadata> metadata = searchService.find(SEARCH_PARAMETERS, Metadata.class);
		assertEquals(87, metadata.getItems().size());
		metadata.getItems().forEach(m -> {
			assertEquals("LNC", m.getTerminology());
			assertEquals("277", m.getVersion());
		});
	}

	/**
	 * Test find metadata by terminology rx norm.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindMetadataByTerminologyRxNorm() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:RXNORM AND version:02052024");
		final ResultList<Metadata> metadata = searchService.find(SEARCH_PARAMETERS, Metadata.class);
		assertEquals(80, metadata.getItems().size());
		metadata.getItems().forEach(m -> {
			assertEquals("RXNORM", m.getTerminology());
			assertEquals("02052024", m.getVersion());
		});
	}

	/**
	 * Test find metadata by terminology snomed ct us.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindMetadataByTerminologySnomedCtUs() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:SNOMEDCT_US AND version:20240301");
		final ResultList<Metadata> metadata = searchService.find(SEARCH_PARAMETERS, Metadata.class);
		assertEquals(62, metadata.getItems().size());
		metadata.getItems().forEach(m -> {
			assertEquals("SNOMEDCT_US", m.getTerminology());
			assertEquals("20240301", m.getVersion());
		});
	}

	/**
	 * Test find metadata by terminology snomed ct.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testFindMetadataByTerminologySnomedCt() throws Exception {

		SEARCH_PARAMETERS.setQuery("terminology:SNOMEDCT AND version:20240101");
		final ResultList<Metadata> metadata = searchService.find(SEARCH_PARAMETERS, Metadata.class);
		assertEquals(59, metadata.getItems().size());
		metadata.getItems().forEach(m -> {
			assertEquals("SNOMEDCT", m.getTerminology());
			assertEquals("20240101", m.getVersion());
		});
	}

}
