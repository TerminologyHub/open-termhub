/*
 *
 */
package com.wci.termhub.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.Application;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.LoaderUtil;

/**
 * The Class LoadUnitTest.
 */
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class LoadUnitTest {

	/** The logger. */
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(LoadUnitTest.class);

	/** The search service. */
	@Autowired
	private EntityRepositoryService searchService;

	/**
	 * Test create index.
	 *
	 * @throws Exception the exception
	 */
	@BeforeAll
	public void setup() throws Exception {

		final List<String> terminologies = List.of("icd10cm-nlm-2023", "lnc-nlm-277", "rxnorm-nlm-02052024",
				"snomedct_us-sandbox-20240301", "snomedct-sandbox-20240101");

		final String rootDir = "src/main/resources/data/";

		// delete all indexes for a fresh start
		searchService.deleteIndex(Terminology.class);
		searchService.deleteIndex(Metadata.class);
		searchService.deleteIndex(Concept.class);
		searchService.deleteIndex(Term.class);
		searchService.deleteIndex(ConceptRelationship.class);

		for (final String terminology : terminologies) {
			LoaderUtil.loadTerminology(searchService, rootDir + terminology + ".json");
			LoaderUtil.loadMetadata(searchService, rootDir + terminology + "-metadata.json");
			LoaderUtil.loadConcepts(searchService, rootDir + terminology + "-concepts.json");
			LoaderUtil.loadConceptRelationships(searchService, rootDir + terminology + "-relationships.json");
		}
	}

	@Test
	public void test() {
		// Do not remove this test. It forces the setup before other classes.
		assertTrue(true);
	}

}
