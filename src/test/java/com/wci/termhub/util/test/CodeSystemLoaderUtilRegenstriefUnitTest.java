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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Parameters;
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
import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.fhir.util.LoincConstants;
import com.wci.termhub.model.Concept;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractServerTest;
import com.wci.termhub.util.CodeSystemLoaderUtil;
import com.wci.termhub.util.TerminologyUtility;

/**
 * CodeSystem import when {@code server.mode=regenstrief}: concept-level properties
 * {@code semanticType} and lowercase {@code status} are not indexed.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties", properties = {
    "lucene.index.directory=build/index/lucene-codesystem-loader-regenstrief-test",
    "server.mode=regenstrief"
})
public class CodeSystemLoaderUtilRegenstriefUnitTest extends AbstractServerTest {

  /** LOINC code in mini source with semanticType Observation in input JSON. */
  private static final String LOINC_TEST_CODE = "10-9";

  /** LOINC version in mini source file. */
  private static final String LOINC_MINI_VERSION = "2.78";

  /** Sandbox concept with both status and STATUS properties. */
  private static final String LOINC_STATUS_TEST_CODE = "LG50982-4";

  /** LOINC sandbox version. */
  private static final String LOINC_SANDBOX_VERSION = "277";

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
   * Regenstrief import must not persist semanticType on concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadSuppressesSemanticTypeOnConcept() throws Exception {
    loadMiniLoincSource();

    final Concept concept = getMiniLoincConcept(LOINC_TEST_CODE);
    assertNotNull(concept);
    assertFalse(concept.getAttributes().containsKey("semanticType"));
    assertTrue(concept.getSemanticTypes().isEmpty());
  }

  /**
   * Loaded concepts must not emit semanticType on FHIR $lookup.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadLookupOmitsSemanticType() throws Exception {
    loadMiniLoincSource();

    final Concept concept = getMiniLoincConcept(LOINC_TEST_CODE);
    assertNotNull(concept);

    final Parameters parameters = lookupParameters(concept, LOINC_MINI_VERSION);
    assertFalse(collectPropertyCodes(parameters).contains("semanticType"));
  }

  /**
   * Regenstrief import must not persist lowercase status on concepts but still set active.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadSuppressesStatusAttributeOnConcept() throws Exception {
    loadLoincSandbox();

    final Concept concept = TerminologyUtility.getConcept(searchService, LoincConstants.LOINC_SYSTEM_ALT,
        "SANDBOX", LOINC_SANDBOX_VERSION, LOINC_STATUS_TEST_CODE);

    assertNotNull(concept);
    assertTrue(concept.getActive());
    assertFalse(concept.getAttributes().containsKey("status"));
    assertTrue(concept.getAttributes().containsKey("STATUS"));
  }

  /**
   * Loaded concepts must not emit property status|active on $lookup when STATUS is present.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadLookupOmitsStatusProperty() throws Exception {
    loadLoincSandbox();

    final Concept concept = TerminologyUtility.getConcept(searchService, LoincConstants.LOINC_SYSTEM_ALT,
        "SANDBOX", LOINC_SANDBOX_VERSION, LOINC_STATUS_TEST_CODE);
    assertNotNull(concept);

    final Parameters parameters = lookupParameters(concept, LOINC_SANDBOX_VERSION);
    final Set<String> propertyKeys = collectPropertyKeys(parameters);

    assertFalse(propertyKeys.contains("status|active"));
    assertTrue(propertyKeys.contains("STATUS|Active"));
  }

  /**
   * @throws Exception the exception
   */
  private void loadMiniLoincSource() throws Exception {
    final ClassPathResource sourceResource =
        new ClassPathResource("data/loinc/CodeSystem-loinc-2.78-mini-source.json");
    CodeSystemLoaderUtil.loadCodeSystem(searchService, sourceResource.getFile(), false,
        CodeSystem.class, new DefaultProgressListener());
  }

  /**
   * @throws Exception the exception
   */
  private void loadLoincSandbox() throws Exception {
    final ClassPathResource sourceResource =
        new ClassPathResource("data/CodeSystem-lnc-sandbox-277-r4.json");
    CodeSystemLoaderUtil.loadCodeSystem(searchService, sourceResource.getFile(), false,
        CodeSystem.class, new DefaultProgressListener());
  }

  /**
   * @param code the code
   * @return the concept
   * @throws Exception the exception
   */
  private Concept getMiniLoincConcept(final String code) throws Exception {
    return TerminologyUtility.getConcept(searchService, LoincConstants.LOINC_SYSTEM,
        LoincConstants.LOINC_PUBLISHER, LOINC_MINI_VERSION, code);
  }

  /**
   * @param concept the concept
   * @param version the version
   * @return lookup parameters
   * @throws Exception the exception
   */
  private Parameters lookupParameters(final Concept concept, final String version) throws Exception {
    final CodeSystem codeSystem = new CodeSystem();
    codeSystem.setUrl(LoincConstants.LOINC_URI);
    codeSystem.setTitle(LoincConstants.LOINC_SYSTEM);
    codeSystem.setVersion(version);
    return FhirUtilityR4.toR4(codeSystem, concept, null, Map.of(), List.of(), List.of(), null,
        null, true);
  }

  /**
   * @param parameters the parameters
   * @return property codes only
   */
  private static Set<String> collectPropertyCodes(final Parameters parameters) {
    return parameters.getParameter().stream().filter(p -> "property".equals(p.getName()))
        .map(CodeSystemLoaderUtilRegenstriefUnitTest::propertyCodeFromParameter)
        .collect(Collectors.toSet());
  }

  /**
   * @param parameters the parameters
   * @return property code|value keys
   */
  private static Set<String> collectPropertyKeys(final Parameters parameters) {
    return parameters.getParameter().stream().filter(p -> "property".equals(p.getName()))
        .map(p -> {
          String code = null;
          String value = null;
          for (final Parameters.ParametersParameterComponent part : p.getPart()) {
            if ("code".equals(part.getName()) && part.getValue() instanceof CodeType) {
              code = ((CodeType) part.getValue()).getValue();
            } else if ("value".equals(part.getName())) {
              if (part.getValue() instanceof CodeType) {
                value = ((CodeType) part.getValue()).getValue();
              } else if (part.getValue() != null) {
                value = part.getValue().primitiveValue();
              }
            }
          }
          return code + "|" + value;
        }).collect(Collectors.toSet());
  }

  /**
   * @param propertyParam the property param
   * @return the code
   */
  private static String propertyCodeFromParameter(
    final Parameters.ParametersParameterComponent propertyParam) {
    return propertyParam.getPart().stream()
        .filter(part -> "code".equals(part.getName()) && part.getValue() instanceof CodeType)
        .map(part -> ((CodeType) part.getValue()).getValue()).findFirst().orElse(null);
  }
}
