/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hl7.fhir.r4.model.CodeSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.fhir.util.LoincConstants;
import com.wci.termhub.model.Concept;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractServerTest;
import com.wci.termhub.util.CodeSystemLoaderUtil;
import com.wci.termhub.util.TerminologyUtility;

/**
 * CodeSystem import when {@code server.mode=default}: concept semanticType is indexed.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties", properties = {
    "lucene.index.directory=build/index/lucene-codesystem-loader-basic-test",
    "server.mode=default"
})
public class CodeSystemLoaderUtilBasicModeUnitTest extends AbstractServerTest {

  /** LOINC part code with semanticType in sandbox input. */
  private static final String LOINC_PART_CODE = "LP343406-7";

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The index directory. */
  @Value("${lucene.index.directory}")
  private String indexDirectory;

  /**
   * Fresh index for each test.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void resetIndex() throws Exception {
    clearAndCreateIndexDirectories(searchService, indexDirectory);
  }

  /**
   * Basic mode persists semanticType on concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testBasicModePersistsSemanticTypeOnConcept() throws Exception {
    final ClassPathResource sourceResource =
        new ClassPathResource("data/CodeSystem-lnc-sandbox-277-r4.json");
    CodeSystemLoaderUtil.loadCodeSystem(searchService, sourceResource.getFile(), false,
        CodeSystem.class, new DefaultProgressListener());

    final Concept concept = TerminologyUtility.getConcept(searchService, LoincConstants.LOINC_SYSTEM_ALT,
        "SANDBOX", "277", LOINC_PART_CODE);

    assertNotNull(concept);
    assertTrue(concept.getAttributes().containsKey("semanticType"));
    assertFalse(concept.getSemanticTypes().isEmpty());
  }

  /**
   * Basic mode persists lowercase status on concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testBasicModePersistsStatusOnConcept() throws Exception {
    final ClassPathResource sourceResource =
        new ClassPathResource("data/CodeSystem-lnc-sandbox-277-r4.json");
    CodeSystemLoaderUtil.loadCodeSystem(searchService, sourceResource.getFile(), false,
        CodeSystem.class, new DefaultProgressListener());

    final Concept concept = TerminologyUtility.getConcept(searchService, LoincConstants.LOINC_SYSTEM_ALT,
        "SANDBOX", "277", "LG50982-4");

    assertNotNull(concept);
    assertTrue(concept.getAttributes().containsKey("status"));
    assertEquals("active", concept.getAttributes().get("status"));
  }
}
