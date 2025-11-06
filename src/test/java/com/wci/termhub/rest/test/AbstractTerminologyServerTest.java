/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.rest.test;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractServerTest;

/**
 * Abstract superclass for source code tests.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties", properties = {
    "lucene.index.directory=build/index/lucene-rest"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractTerminologyServerTest extends AbstractServerTest {

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(AbstractTerminologyServerTest.class);

  /** The search service. */
  @Autowired
  protected EntityRepositoryService searchService;

  /** The index directory. */
  @Value("${lucene.index.directory}")
  private String indexDirectory;

  /** List of FHIR Code System files to load. */
  private static final List<String> CODE_SYSTEM_FILES =
      List.of("CodeSystem-snomedct_us-sandbox-20240301-r5.json",
          "CodeSystem-snomedct-sandbox-20240101-r5.json", "CodeSystem-lnc-sandbox-277-r5.json",
          "CodeSystem-icd10cm-sandbox-2023-r5.json", "CodeSystem-rxnorm-sandbox-04012024-r5.json");

  /** List of FHIR ConceptMap files to load. */
  private static final List<String> CONCEPT_MAP_FILES =
      List.of("ConceptMap-snomedct_us-icd10cm-sandbox-20240301-r5.json",
          "ConceptMap-cpt-hcpcs-fake-r5.json");

  /** The Constant VALUE_SET_FILES. */
  private static final List<String> VALUE_SET_FILES =
      List.of("ValueSet-snomedct_us-extension-sandbox-20240301-r5.json",
          "ValueSet-hl7-body-site-r5.json", "ValueSet-personal-hl7-relationship-uv-ips-r5.json");

  /** The setup once. */
  private static boolean setupOnce = false;

  public static void setSetupOnce(final boolean flag) {
    setupOnce = flag;
  }

  /**
   * Setup once.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public void setupData() throws Exception {
    if (setupOnce) {
      return;
    }
    try {
      clearAndCreateIndexDirectories(searchService, indexDirectory);
      loadCodeSystems(searchService, CODE_SYSTEM_FILES, true);
      loadConceptMaps(searchService, CONCEPT_MAP_FILES);
      loadValueSets(searchService, VALUE_SET_FILES);

      // add another terminology to test searches that should not match
      // create a terminology with publisher = "SNOMEDCT International"
      final String publisher = "SNOMEDCT International";
      final Terminology terminology = new Terminology();
      terminology.setId(UUID.randomUUID().toString());
      terminology.setAbbreviation("FAKE");
      terminology.setName("Fake Terminology for Testing");
      terminology.setVersion("http://fake.info/");
      terminology.setPublisher(publisher);
      terminology.setFamily("Fake Family");
      searchService.add(Terminology.class, terminology);

    } catch (final Exception e) {
      logger.error("Error setting up data: {}", e.getMessage(), e);
      throw e;
    }
    setupOnce = true;
  }

}
