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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.ConceptTreePosition;
import com.wci.termhub.model.HealthCheck;
import com.wci.termhub.model.Mapping;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.MapsetRef;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.ResultListConcept;
import com.wci.termhub.model.ResultListConceptRelationship;
import com.wci.termhub.model.ResultListConceptTreePosition;
import com.wci.termhub.model.ResultListMapping;
import com.wci.termhub.model.ResultListMapset;
import com.wci.termhub.model.ResultListMetadata;
import com.wci.termhub.model.ResultListSubset;
import com.wci.termhub.model.ResultListSubsetMember;
import com.wci.termhub.model.ResultListTerm;
import com.wci.termhub.model.ResultListTerminology;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.SubsetMember;
import com.wci.termhub.model.SubsetRef;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.util.CodeSystemLoaderUtil;
import com.wci.termhub.util.ConceptMapLoaderUtil;

/**
 * Unit tests for TerminologyServiceRestImpl. All systems tests are order 1. All get/find tests are
 * order 10. All delete tests are order 20.
 */
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class TerminologyServiceRestImplUnitTest extends AbstractTerminologyServerTest {

  /** The LOGGER. */
  private static final Logger LOGGER =
      LoggerFactory.getLogger(TerminologyServiceRestImplUnitTest.class);

  /** The mock mvc. */
  @Autowired
  private MockMvc mockMvc;

  /** The base url. */
  private String baseUrl = "";

  /** The object mapper. */
  @Autowired
  private ObjectMapper objectMapper;

  /** The Constant INITIAL. */
  private static final int INITIAL = 1;

  /** The Constant FIND. */
  private static final int FIND = 10;

  /** The Constant DELETE. */
  private static final int DELETE = 20;

  /** The Constant ERROR_FIELDS. */
  private static final String[] ERROR_FIELDS = new String[] {
      "status", "error", "message", "local", "timestamp"
  };

  /**
   * Sets the up.
   */
  @BeforeEach
  public void setUp() {
    baseUrl = "";
  }

  /**
   * Test health.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(INITIAL)
  public void testHealth() throws Exception {

    // Add a user through the API
    final String url = baseUrl + "/terminology/health";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final HealthCheck check = objectMapper.readValue(content, HealthCheck.class);
    assertThat(check).isNotNull();
    assertThat(check.getName()).isEqualTo("open-termhub-terminology-service");
  }

  /**
   * Test get terminology ICD 10 CM.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetTerminologyIcd10Cm() throws Exception {
    final String id = CodeSystemLoaderUtil.mapOriginalId("177f2263-fe04-4f1f-b0e6-9b351ab8baa9");
    final String url = baseUrl + "/terminology/" + id;
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final Terminology terminology = objectMapper.readValue(content, Terminology.class);
    assertThat(terminology).isNotNull();
    assertEquals(id, terminology.getId());
    assertEquals("ICD10CM", terminology.getAbbreviation());
    assertEquals("Mini version of ICD10CM for testing purposes", terminology.getName());
  }

  /**
   * Test get terminology ICD 10 CM.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetTerminologyNonExistant() throws Exception {
    final String id = "177f2263-fe04-4f1f-b0e6-9b351abFAKE";
    final String url = baseUrl + "/terminology/" + id;
    LOGGER.info("Testing url - {}", url);
    mockMvc.perform(get(url)).andExpect(status().isNotFound()).andReturn();
  }

  /**
   * Test get terminology snomed ct us.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetTerminologySnomedCtUs() throws Exception {
    final String id = CodeSystemLoaderUtil.mapOriginalId("340c926f-9ad6-4f1b-b230-dc4ca14575ab");
    final String url = baseUrl + "/terminology/" + id;
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final Terminology terminology = objectMapper.readValue(content, Terminology.class);
    assertThat(terminology).isNotNull();
    assertEquals(id, terminology.getId());
    assertEquals("SNOMEDCT_US", terminology.getAbbreviation());
    assertEquals("Mini version of SNOMEDCT_US For testing purposes", terminology.getName());
  }

  /**
   * Test get terminology meta data snomed ct us.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetTerminologyMetaDataSnomedCtUs() throws Exception {
    final String id = CodeSystemLoaderUtil.mapOriginalId("340c926f-9ad6-4f1b-b230-dc4ca14575ab");
    final String url = baseUrl + "/terminology/" + id + "/metadata";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    final List<Metadata> metadataList =
        objectMapper.readValue(content, new TypeReference<List<Metadata>>() {
          // n/a
        });
    assertThat(metadataList).isNotNull();
    assertFalse(metadataList.isEmpty());
    for (final Metadata metadata : metadataList) {
      assertThat(metadata).isNotNull();
      assertThat(metadata.getId()).isNotNull();
      assertEquals("SNOMEDCT_US", metadata.getTerminology());
      assertThat(metadata.getPublisher()).isNotNull();
      assertThat(metadata.getField()).isNotNull();
      assertThat(metadata.getModel()).isNotNull();
      assertThat(metadata.getCode()).isNotNull();
    }
    /*
     * {"id":"...","local":false,"active":true,"terminology":"SNOMEDCT_US", "version":"20240301",
     * "publisher":"SANDBOX","model":"relationship","field":"uiLabel","code": "Attributes","rank":0}
     */
    // has no name
    // assertThat(metadata.getName()).isNotNull();
  }

  /**
   * Test get terminology meta data snomed ct us not found.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetTerminologyMetaDataSnomedCtUsNotFound() throws Exception {
    final String id = "a1d1e426-26a6-4326-b18b-c54c1540FAKE";
    final String url = baseUrl + "/terminology/" + id + "/metadata";
    LOGGER.info("Testing url - {}", url);
    mockMvc.perform(get(url)).andExpect(status().isNotFound()).andReturn();
  }

  /**
   * Test get terminologies.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetTerminologies() throws Exception {
    final String url = baseUrl + "/terminology";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    final ResultListTerminology terminologyList =
        objectMapper.readValue(content, ResultListTerminology.class);
    assertThat(terminologyList).isNotNull();
    assertThat(terminologyList.getTotal()).isPositive();
    for (final Terminology terminology : terminologyList.getItems()) {
      assertThat(terminology).isNotNull();
      assertThat(terminology.getId()).isNotNull();
      assertThat(terminology.getAbbreviation()).isNotNull();
      assertThat(terminology.getName()).isNotNull();
      assertThat(terminology.getFamily()).isNotNull();
      assertThat(terminology.getPublisher()).isNotNull();
    }
  }

  /**
   * Test get terminology with query.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetTerminologyWithQuery() throws Exception {
    final String url = baseUrl + "/terminology?query=abbreviation:SNOMEDCT_US";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    final ResultListTerminology terminologyList =
        objectMapper.readValue(content, ResultListTerminology.class);
    assertThat(terminologyList).isNotNull();
    assertThat(terminologyList.getTotal()).isPositive();
    for (final Terminology terminology : terminologyList.getItems()) {
      assertThat(terminology).isNotNull();
      assertThat(terminology.getId()).isNotNull();
      assertEquals("SNOMEDCT_US", terminology.getAbbreviation());
    }
  }

  /**
   * Test get concept by id.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetConceptById() throws Exception {

    // Lookup by code to get the id
    final Concept testConcept = getConceptByCode("ICD10CM", "E11");

    final String id = testConcept.getId();
    final String url = baseUrl + "/concept/" + id;

    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();

    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final Concept concept = objectMapper.readValue(content, Concept.class);
    assertThat(concept).isNotNull();
    assertEquals(id, concept.getId());
    assertEquals("ICD10CM", concept.getTerminology());
    assertEquals("SANDBOX", concept.getPublisher());
    assertEquals("2023", concept.getVersion());
    assertEquals("E11", concept.getCode());
    assertEquals("Type 2 diabetes mellitus", concept.getName());
    assertThat(concept.getTerms()).isNotNull();
    assertEquals(4, concept.getTerms().size());
    assertThat(concept.getAttributes()).isNotNull();
    assertThat(concept.getSemanticTypes()).isNotNull();
    assertThat(concept.getParents()).isNotNull();
    assertThat(concept.getChildren()).isNotNull();
    assertThat(concept.getAncestors()).isNotNull();
    assertThat(concept.getDescendants()).isNotNull();
  }

  /**
   * Test get concept by id not found.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetConceptByIdNotFound() throws Exception {
    final String id = "ef721e67-ebf5-4b50-a0b9-16d7aea7FAKE";
    final String url = baseUrl + "/concept/" + id;
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull().isEmpty();

  }

  /**
   * Test get concept by terminology and code.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetConceptByTerminologyAndCode() throws Exception {
    final String code = "E11";
    final String terminology = "ICD10CM";
    final String url = baseUrl + "/concept/" + terminology + "/" + code;
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final Concept concept = objectMapper.readValue(content, Concept.class);
    assertThat(concept).isNotNull();
    assertThat(concept.getId()).isNotBlank();
    assertEquals(terminology, concept.getTerminology());
    assertEquals(code, concept.getCode());
    assertEquals("Type 2 diabetes mellitus", concept.getName());
    assertEquals("2023", concept.getVersion());
    assertEquals("SANDBOX", concept.getPublisher());
    assertThat(concept.getTerms()).isNotNull();
    assertEquals(4, concept.getTerms().size());
    assertThat(concept.getAttributes()).isNotNull();
    assertThat(concept.getSemanticTypes()).isNotNull();
    assertThat(concept.getParents()).isNotNull();
    assertThat(concept.getChildren()).isNotNull();
    assertThat(concept.getAncestors()).isNotNull();
    assertThat(concept.getDescendants()).isNotNull();
  }

  /**
   * Test get concept by terminology and code not found.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetConceptByTerminologyAndCodeNotFound() throws Exception {
    final String code = "ZZZ";
    final String terminology = "ICD10CM";
    final String url = baseUrl + "/concept/" + terminology + "/" + code;
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull().isEmpty();
  }

  /**
   * Test get concept codes.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetConceptCodes() throws Exception {
    final String terminology = "LNC";
    final List<String> codes = List.of("LP32519-8", "LP231645-5", "63904-7", "74291-6", "FAKE");
    final String url = baseUrl + "/concept/" + terminology + "?codes=" + String.join(",", codes);
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final List<Concept> concepts =
        objectMapper.readValue(content, new TypeReference<List<Concept>>() {
          // n/a
        });
    assertThat(concepts).isNotNull();
    assertEquals(4, concepts.size());
    for (final Concept concept : concepts) {
      assertThat(concept).isNotNull();
      assertThat(concept.getId()).isNotNull();
      assertThat(concept.getTerminology()).isEqualTo(terminology);
      assertThat(concept.getCode()).isIn(codes);
    }
  }

  /**
   * Test get concept with definition.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetConceptWithDefinition() throws Exception {
    final String code = "723277005";
    final String terminology = "SNOMEDCT";
    final String url = baseUrl + "/concept/" + terminology + "/" + code;
    LOGGER.info("Testing url - {}", url);

    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);

    assertThat(content).isNotNull();
    final Concept concept = objectMapper.readValue(content, Concept.class);
    assertThat(concept).isNotNull();

    // Verify basic concept properties
    assertEquals(code, concept.getCode());
    assertEquals(terminology, concept.getTerminology());
    assertEquals("Nonconformance to editorial policy component", concept.getName());

    // Verify definition was loaded
    assertNotNull(concept.getDefinitions(), "Definitions list should not be null");
    assertFalse(concept.getDefinitions().isEmpty(), "Should have loaded definition");
    assertEquals(1, concept.getDefinitions().size(), "Should have exactly one definition");

    final com.wci.termhub.model.Definition definition = concept.getDefinitions().get(0);
    assertNotNull(definition, "Definition should not be null");
    assertEquals("A component that fails to comply with the current editorial guidance.",
        definition.getDefinition());
    assertEquals("SNOMEDCT", definition.getTerminology());
    assertEquals("20240101", definition.getVersion());
    assertEquals("SANDBOX", definition.getPublisher());
    assertTrue(definition.getActive(), "Definition should be active");
    assertTrue(definition.getLocaleMap().containsKey("en"), "Should have English locale");
    assertTrue(definition.getLocaleMap().get("en"), "English should be preferred");

    LOGGER.info("Successfully verified definition in REST API: {}", definition.getDefinition());
  }

  /**
   * Test find concepts.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFindConcepts() throws Exception {
    final String terminology = "RXNORM";
    final String query = "canagliflozin";
    final int limit = 15;
    final String url = baseUrl + "/concept?terminology=" + terminology + "&query=name:" + query
        + "&limit=" + limit;
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConcept conceptList = objectMapper.readValue(content, ResultListConcept.class);
    assertThat(conceptList).isNotNull();
    assertFalse(conceptList.getItems().isEmpty());
    assertTrue(conceptList.getItems().size() <= limit);
    for (final Concept concept : conceptList.getItems()) {
      assertThat(concept).isNotNull();
      assertThat(concept.getId()).isNotNull();
      assertThat(concept.getTerminology()).isEqualTo(terminology);
      assertThat(concept.getName().toLowerCase()).contains(query.toLowerCase());
    }
  }

  /**
   * Test find concepts multiple terminologies.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFindConceptsMultipleTerminologies() throws Exception {
    final String terminology = "SNOMEDCT,LNC";
    final String query = "diabetes";
    final int limit = 15;
    final String url = baseUrl + "/concept?terminology=" + terminology + "&query=name:" + query
        + "&limit=" + limit;
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConcept conceptList = objectMapper.readValue(content, ResultListConcept.class);
    assertThat(conceptList).isNotNull();
    assertFalse(conceptList.getItems().isEmpty());
    assertTrue(conceptList.getItems().size() <= limit);
    for (final Concept concept : conceptList.getItems()) {
      assertThat(concept).isNotNull();
      assertThat(concept.getId()).isNotNull();
      assertThat(concept.getTerminology()).isIn("SNOMEDCT", "LNC");
      assertThat(concept.getName().toLowerCase()).contains(query.toLowerCase());
    }
  }

  /**
   * Test find concepts by code.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFindConcepts2() throws Exception {
    final String terminology = "LNC";
    final String query = "cancer";
    final int limit = 15;
    final String url =
        baseUrl + "/concept?terminology=" + terminology + "&query=" + query + "&limit=" + limit;
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConcept conceptList = objectMapper.readValue(content, ResultListConcept.class);
    assertThat(conceptList).isNotNull();
    assertFalse(conceptList.getItems().isEmpty());
    assertTrue(conceptList.getItems().size() <= limit);
    for (final Concept concept : conceptList.getItems()) {
      assertThat(concept).isNotNull();
      assertThat(concept.getId()).isNotNull();
      assertThat(concept.getTerminology()).isEqualTo(terminology);
      assertThat(concept.getName().toLowerCase()).contains(query.toLowerCase());
    }
  }

  /**
   * Test find concepts by ancestor code.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptsByAncestorCode() throws Exception {
    final String terminology = "LNC";
    final String query = "MTHU000003";
    final int limit = 15;
    final String url = baseUrl + "/concept?terminology=" + terminology + "&query=ancestors.code:"
        + query + "&limit=" + limit + "&include=ancestors";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConcept conceptList = objectMapper.readValue(content, ResultListConcept.class);
    assertThat(conceptList).isNotNull();
    LOGGER.info(" conceptList.getTotal() = {}", conceptList.getTotal());
    assertFalse(conceptList.getItems().isEmpty());
    assertTrue(conceptList.getItems().size() <= limit);
    for (final Concept concept : conceptList.getItems()) {
      assertThat(concept).isNotNull();
      assertThat(concept.getId()).isNotNull();
      assertThat(concept.getAncestors()).anyMatch(ancestor -> ancestor.getCode().equals(query));
    }
  }

  /**
   * Test find terms.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFindTerms() throws Exception {
    final String terminology = "RXNORM";
    final String query = "nirmatrelvir";
    final int limit = 15;
    final String url =
        baseUrl + "/term?terminology=" + terminology + "&query=name:" + query + "&limit=" + limit;
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListTerm termsList = objectMapper.readValue(content, ResultListTerm.class);
    assertThat(termsList).isNotNull();
    assertFalse(termsList.getItems().isEmpty());
    assertTrue(termsList.getItems().size() <= limit);
    for (final Term term : termsList.getItems()) {
      assertThat(term).isNotNull();
      assertThat(term.getId()).isNotNull();
      assertThat(term.getTerminology()).isEqualTo(terminology);
      assertThat(term.getName().toLowerCase()).contains(query.toLowerCase());
    }
  }

  /**
   * Test concept bulk.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testConceptBulk() throws Exception {
    final String terminology = "LNC";
    final int limit = 12;
    final String url = baseUrl + "/concept/bulk?terminology=" + terminology + "&limit=" + limit;
    final List<String> codes =
        List.of("code:LP32519-8", "code:LP231645-5", "code:63904-7", "code:74291-6", "code:FAKE");
    final String body = String.join("\n", codes);
    LOGGER.info("Testing url - {}, body - {}", url, body);
    final MvcResult result =
        mockMvc.perform(post(url).contentType(MediaType.TEXT_PLAIN).content(body))
            .andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final List<ResultListConcept> resultConceptList =
        objectMapper.readValue(content, new TypeReference<List<ResultListConcept>>() {
          // n/a
        });
    assertThat(resultConceptList).isNotNull();
    assertThat(resultConceptList.size()).isEqualTo(5);
    for (final ResultListConcept resultConcept : resultConceptList) {
      if ("code:FAKE".equals(resultConcept.getParameters().getQuery())) {
        continue;
      }
      assertThat(resultConcept).isNotNull();
      for (final Concept concept : resultConcept.getItems()) {
        assertThat(concept).isNotNull();
        assertThat(concept.getId()).isNotNull();
        assertThat(concept.getTerminology()).isEqualTo(terminology);
        assertThat("code:" + concept.getCode()).isIn(codes);
      }
    }
  }

  /**
   * Test find metadata.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFindMetadata() throws Exception {
    // GET /metadata
    final String terminology = "LNC";
    final int limit = 100;
    final String url = baseUrl + "/metadata?query=terminology:" + terminology + "&limit=" + limit;
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListMetadata metadataList =
        objectMapper.readValue(content, ResultListMetadata.class);
    assertThat(metadataList).isNotNull();
    assertFalse(metadataList.getItems().isEmpty());
    assertThat(metadataList.getItems().size()).isLessThanOrEqualTo(limit);
    for (final Metadata metadata : metadataList.getItems()) {
      assertThat(metadata).isNotNull();
      // can be null
      // assertThat(metadata.getCode()).isNotNull();
      assertThat(metadata.getTerminology()).isEqualTo(terminology);
      assertThat(metadata.getVersion()).isNotNull();
      assertThat(metadata.getModel()).isNotNull();
      assertThat(metadata.getField()).isNotNull();
      assertThat(metadata.getCode()).isNotNull();
      // LNC "model":"term","field":"language","code":"ENG","rank":0
      // has no name
      // assertThat(metadata.getName()).isNotNull();
    }
  }

  /**
   * Test find concept relationships.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFindConceptRelationships() throws Exception {

    final Concept testConcept = getConceptByCode("SNOMEDCT", "404684003");

    // GET concept/{conceptId}/relationships
    final String conceptId = testConcept.getId();
    final String url = baseUrl + "/concept/" + conceptId + "/relationships";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConceptRelationship conceptRels =
        objectMapper.readValue(content, ResultListConceptRelationship.class);
    assertThat(conceptRels).isNotNull();
    assertFalse(conceptRels.getItems().isEmpty());
    for (final ConceptRelationship conceptRel : conceptRels.getItems()) {
      assertThat(conceptRel).isNotNull();
      assertThat(conceptRel.getId()).isNotNull();
      assertEquals("SNOMEDCT", conceptRel.getTerminology(),
          "All relationships should be from SNOMEDCT terminology, not SNOMEDCT_US");
      assertThat(conceptRel.getVersion()).isNotNull();
      assertThat(conceptRel.getFrom()).isNotNull();
      assertThat(conceptRel.getFrom().getCode()).isNotNull();
      assertEquals("SNOMEDCT", conceptRel.getFrom().getTerminology(),
          "All relationships should be from SNOMEDCT terminology, not SNOMEDCT_US");
      assertThat(conceptRel.getTo()).isNotNull();
    }
  }

  /**
   * Test find concept relationships 2.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFindConceptRelationships2() throws Exception {
    // GET concept/{terminology}/{code}/relationships
    final String terminology = "SNOMEDCT_US";
    final String code = "404684003";
    final String url = baseUrl + "/concept/" + terminology + "/" + code + "/relationships";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConceptRelationship conceptRels =
        objectMapper.readValue(content, ResultListConceptRelationship.class);
    assertThat(conceptRels).isNotNull();
    assertFalse(conceptRels.getItems().isEmpty());
    for (final ConceptRelationship conceptRel : conceptRels.getItems()) {
      assertThat(conceptRel).isNotNull();
      assertThat(conceptRel.getId()).isNotNull();
      assertEquals("SNOMEDCT_US", conceptRel.getTerminology(),
          "All relationships should be from SNOMEDCT terminology, not SNOMEDCT");
      assertThat(conceptRel.getVersion()).isNotNull();
      assertThat(conceptRel.getFrom()).isNotNull();
      assertThat(conceptRel.getFrom().getCode()).isNotNull();
      assertEquals("SNOMEDCT_US", conceptRel.getFrom().getTerminology(),
          "All relationships should be from SNOMEDCT terminology, not SNOMEDCT");
      assertThat(conceptRel.getTo()).isNotNull();
    }
  }

  /**
   * Test find concept inverse relationships.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFindConceptInverseRelationships() throws Exception {

    final Concept testConcept = getConceptByCode("SNOMEDCT", "404684003");
    // GET concept/{conceptId}/inverseRelationships
    final String conceptId = testConcept.getId();
    final String url = baseUrl + "/concept/" + conceptId + "/inverseRelationships";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConceptRelationship conceptRels =
        objectMapper.readValue(content, ResultListConceptRelationship.class);
    assertThat(conceptRels).isNotNull();
    assertFalse(conceptRels.getItems().isEmpty());
    for (final ConceptRelationship conceptRel : conceptRels.getItems()) {
      assertThat(conceptRel).isNotNull();
      assertThat(conceptRel.getId()).isNotNull();
      assertEquals("SNOMEDCT", conceptRel.getTerminology(),
          "All relationships should be from SNOMEDCT terminology, not SNOMEDCT_US");
      assertThat(conceptRel.getVersion()).isNotNull();
      assertThat(conceptRel.getFrom()).isNotNull();
      assertEquals("SNOMEDCT", conceptRel.getFrom().getTerminology(),
          "All relationships should be from SNOMEDCT terminology, not SNOMEDCT_US");
      assertThat(conceptRel.getTo()).isNotNull();
    }
  }

  /**
   * Test find concept inverse relationships 2.
   *
   * @throws Exception the exception
   */
  // GET concept/{terminology}/{code}/inverseRelationships
  @Test
  @Order(FIND)
  public void testFindConceptInverseRelationships2() throws Exception {

    final String terminology = "SNOMEDCT_US";
    final String code = "404684003";
    final String url = baseUrl + "/concept/" + terminology + "/" + code + "/inverseRelationships";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConceptRelationship conceptRels =
        objectMapper.readValue(content, ResultListConceptRelationship.class);
    assertThat(conceptRels).isNotNull();
    assertFalse(conceptRels.getItems().isEmpty());
    for (final ConceptRelationship conceptRel : conceptRels.getItems()) {
      assertThat(conceptRel).isNotNull();
      assertThat(conceptRel.getId()).isNotNull();
      assertEquals("SNOMEDCT_US", conceptRel.getTerminology(),
          "All relationships should be from SNOMEDCT terminology, not SNOMEDCT");
      assertThat(conceptRel.getVersion()).isNotNull();
      assertThat(conceptRel.getFrom()).isNotNull();
      assertThat(conceptRel.getFrom().getCode()).isNotNull();
      assertEquals("SNOMEDCT_US", conceptRel.getFrom().getTerminology(),
          "All relationships should be from SNOMEDCT terminology, not SNOMEDCT");
      assertThat(conceptRel.getTo()).isNotNull();
    }
  }

  // skipping mapsets and diagram

  /**
   * Test find tree position children.
   *
   * @throws Exception the exception
   */
  // GET /concept/{conceptId}/trees/children
  @Test
  @Order(FIND)
  public void testFindTreePositionChildren() throws Exception {

    final Concept testConcept = getConceptByCode("SNOMEDCT", "138875005");

    final String conceptId = testConcept.getId();
    final String url = baseUrl + "/concept/" + conceptId + "/trees/children";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConceptTreePosition conceptTreePositonList =
        objectMapper.readValue(content, ResultListConceptTreePosition.class);
    assertThat(conceptTreePositonList).isNotNull();
    assertFalse(conceptTreePositonList.getItems().isEmpty(),
        "Expected non-empty list of concept tree positions, got "
            + conceptTreePositonList.getItems().size());
    for (final ConceptTreePosition ctp : conceptTreePositonList.getItems()) {
      assertThat(ctp).isNotNull();
      assertThat(ctp.getId()).isNotNull();
      assertThat(ctp.getTerminology()).contains("SNOMEDCT");
      assertThat(ctp.getVersion()).isNotNull();
      assertThat(ctp.getConcept()).isNotNull();
      assertThat(ctp.getAncestorPath()).isNotNull();
      assertThat(ctp.getChildCt()).isNotNull();
    }
  }

  /**
   * Test find tree position children 2.
   *
   * @throws Exception the exception
   */
  // GET /concept/{terminology}/{code}/trees/children
  @Test
  @Order(FIND)
  public void testFindTreePositionChildren2() throws Exception {

    final String terminology = "SNOMEDCT_US";
    final String code = "138875005";
    final String url = baseUrl + "/concept/" + terminology + "/" + code + "/trees/children";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConceptTreePosition conceptTreePositonList =
        objectMapper.readValue(content, ResultListConceptTreePosition.class);
    assertThat(conceptTreePositonList).isNotNull();
    assertFalse(conceptTreePositonList.getItems().isEmpty());
    for (final ConceptTreePosition ctp : conceptTreePositonList.getItems()) {
      assertThat(ctp).isNotNull();
      assertThat(ctp.getId()).isNotNull();
      assertThat(ctp.getTerminology()).contains("SNOMEDCT_US");
      assertThat(ctp.getVersion()).isNotNull();
      assertThat(ctp.getConcept()).isNotNull();
      assertThat(ctp.getAncestorPath()).isNotNull();
    }
  }

  /**
   * Test find tree positions.
   *
   * @throws Exception the exception
   */
  // GET /concept/{conceptId}/trees
  @Test
  @Order(FIND)
  public void testFindTreePositions() throws Exception {

    final String terminology = "SNOMEDCT";
    final Concept testConcept = getConceptByCode(terminology, "91723000");

    final String conceptId = testConcept.getId();
    final String url = baseUrl + "/concept/" + conceptId + "/trees";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConceptTreePosition conceptTreePositonList =
        objectMapper.readValue(content, ResultListConceptTreePosition.class);
    assertThat(conceptTreePositonList).isNotNull();

    assertEquals(1, conceptTreePositonList.getItems().size());
    for (final ConceptTreePosition ctp : conceptTreePositonList.getItems()) {
      assertThat(ctp).isNotNull();
      assertThat(ctp.getId()).isNotNull();
      assertThat(ctp.getTerminology()).contains(terminology);
      assertThat(ctp.getVersion()).isNotNull();
      assertThat(ctp.getConcept()).isNotNull();

      final ConceptRef concept = ctp.getConcept();
      assertThat(concept.getActive()).isNotNull();
      assertThat(concept.getCode()).isNotBlank();
      assertThat(concept.getName()).isNotBlank();
      assertThat(concept.getTerminology()).isNotBlank();
      assertThat(concept.getPublisher()).isNotBlank();
      assertThat(concept.getLeaf()).isNotNull();

    }
  }

  /**
   * Test find tree positions 2.
   *
   * @throws Exception the exception
   */
  // GET /concept/{terminology}/{code}/trees
  @Test
  @Order(FIND)
  public void testFindTreePositions2() throws Exception {

    final String terminology = "SNOMEDCT_US";
    final String code = "73211009";
    final String url = baseUrl + "/concept/" + terminology + "/" + code + "/trees";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConceptTreePosition conceptTreePositonList =
        objectMapper.readValue(content, ResultListConceptTreePosition.class);
    assertThat(conceptTreePositonList).isNotNull();
    assertFalse(conceptTreePositonList.getItems().isEmpty(),
        "Expected at least one tree position, got " + conceptTreePositonList.getItems().size());
    for (final ConceptTreePosition ctp : conceptTreePositonList.getItems()) {
      assertThat(ctp).isNotNull();
      assertThat(ctp.getId()).isNotNull();
      assertThat(ctp.getTerminology()).contains(terminology);
      assertThat(ctp.getVersion()).isNotNull();
      assertThat(ctp.getConcept()).isNotNull();
    }
  }

  /**
   * Test find mapsets.
   *
   * @throws Exception the exception
   */
  // GET /mapset/
  @Test
  @Order(FIND)
  public void testFindMapsets() throws Exception {
    final String url = baseUrl + "/mapset";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListMapset mapsetList = objectMapper.readValue(content, ResultListMapset.class);
    assertThat(mapsetList).isNotNull();

    assertThat(mapsetList.getTotal()).isPositive();
    for (final Mapset mapset : mapsetList.getItems()) {
      LOGGER.info("  mapset = {}", mapset);
      assertThat(mapset).isNotNull();
      assertThat(mapset.getId()).isNotNull();
      assertThat(mapset.getName()).isNotNull();
      assertThat(mapset.getAbbreviation()).isNotNull();
      // NOT SET assertThat(mapset.getTerminology()).isNotNull();
      // NOT SET assertThat(mapset.getVersion()).isNotNull();
      assertThat(mapset.getFromTerminology()).isNotNull();
      // NOT SET assertThat(mapset.getFromVersion()).isNotNull();
      assertThat(mapset.getToTerminology()).isNotNull();
      // assertThat(mapset.getToVersion()).isNotNull();
    }
  }

  /**
   * Test find mapsets with multiple sort.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFindMapsetsWithMultipleSort() throws Exception {
    final String url = "/mapset/SNOMEDCT_US-ICD10CM/mapping?query=&offset=0&limit=20&"
        + "sort=from.code,group,priority&ascending=true&leaf=null";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListMapset mapsetList = objectMapper.readValue(content, ResultListMapset.class);
    assertThat(mapsetList).isNotNull();
    assertThat(mapsetList.getTotal()).isPositive();
  }

  /**
   * Test mapset by publisher.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testMappingsByPublisher() throws Exception {
    final String url = "/mapping?query=mapset.publisher:SANDBOX";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListMapping mappingList = objectMapper.readValue(content, ResultListMapping.class);
    assertThat(mappingList).isNotNull();
    assertThat(mappingList.getTotal()).isPositive();
  }

  /**
   * Test mapset by abbreviation wildcard.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testMappingsByAbbreviationWildcard() throws Exception {
    final String url = "/mapping?query=mapset.abbreviation:SNOMED*";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListMapping mappingList = objectMapper.readValue(content, ResultListMapping.class);
    assertThat(mappingList).isNotNull();
    assertThat(mappingList.getTotal()).isPositive();
  }

  /**
   * Test mapset by abbreviation.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testMappingsByAbbreviation() throws Exception {
    final String url = "/mapping?query=mapset.abbreviation:SNOMEDCT_US-ICD10CM";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListMapping mappingList = objectMapper.readValue(content, ResultListMapping.class);
    assertThat(mappingList).isNotNull();
    assertThat(mappingList.getTotal()).isPositive();
  }

  /**
   * Test find mapsets with query.
   *
   * @throws Exception the exception
   */
  // GET /mapset/
  @Test
  @Order(FIND)
  public void testFindMapsetsWithQuery() throws Exception {
    final String query = "terminology=SNOMEDCT_US-ICD10CM&offset=0&limit=10";
    final String url = baseUrl + "/mapset?" + query;
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListMapset mapsetList = objectMapper.readValue(content, ResultListMapset.class);
    assertThat(mapsetList).isNotNull();
    assertThat(mapsetList.getTotal()).isPositive();
    for (final Mapset mapset : mapsetList.getItems()) {
      assertThat(mapset).isNotNull();
      assertThat(mapset.getId()).isNotNull();
      assertThat(mapset.getName()).isNotNull();
      assertThat(mapset.getAbbreviation()).isNotNull();
      // NOT SET assertThat(mapset.getTerminology()).isNotNull();
      // NOT SET assertThat(mapset.getVersion()).isNotNull();
      assertThat(mapset.getFromTerminology()).isNotNull();
      // NOT SET assertThat(mapset.getFromVersion()).isNotNull();
      assertThat(mapset.getToTerminology()).isNotNull();
      // NOT SET assertThat(mapset.getToVersion()).isNotNull();
    }
  }

  /**
   * Test find mapset by id.
   *
   * @throws Exception the exception
   */
  // GET /mapset/{id}
  @Test
  @Order(FIND)
  public void testFindMapsetById() throws Exception {

    final String id = ConceptMapLoaderUtil.mapOriginalId("2a545e12-04eb-48ee-b988-c17346b4e05f");
    final String url = baseUrl + "/mapset/" + id;
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final Mapset mapset = objectMapper.readValue(content, Mapset.class);

    assertThat(mapset).isNotNull();
    assertThat(mapset.getId()).isNotNull();
    assertThat(mapset.getName()).isNotNull();
    assertThat(mapset.getAbbreviation()).isNotNull();
    // NOT SET assertThat(mapset.getTerminology()).isNotNull();
    // NOT SET assertThat(mapset.getVersion()).isNotNull();
    assertThat(mapset.getFromTerminology()).isNotNull();
    // NOT SET assertThat(mapset.getFromVersion()).isNotNull();
    assertThat(mapset.getToTerminology()).isNotNull();
    // NOT SET assertThat(mapset.getToVersion()).isNotNull();
  }

  /**
   * Test find mappings.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  void testFindMappings() throws Exception {

    final String url = baseUrl + "/mapping";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListMapping mappingList = objectMapper.readValue(content, ResultListMapping.class);
    assertThat(mappingList).isNotNull();
    assertThat(mappingList.getTotal()).isPositive();
    assertThat(mappingList.getItems().size()).isLessThanOrEqualTo(FIND);
    for (final Mapping mapping : mappingList.getItems()) {
      assertThat(mapping).isNotNull();
      assertThat(mapping.getId()).isNotNull();
      assertThat(mapping.getMapset()).isNotNull();
      assertThat(mapping.getFrom()).isNotNull();
      assertThat(mapping.getTo()).isNotNull();
      // assertThat(mapping.getGroup()).isNotNull();
      // assertThat(mapping.getPriority()).isNotNull();
    }
  }

  /**
   * Test find mapset by from code 1.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFindMapsetMappingsByFromCode1() throws Exception {

    final Mapset testMapset = getMapsetByAbbreviation("SNOMEDCT_US-ICD10CM");

    final String id = testMapset.getId();
    final String url = baseUrl + "/mapset/" + id + "/mapping?query=from.code:40733004";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListMapping resultListMapping =
        objectMapper.readValue(content, ResultListMapping.class);
    assertThat(resultListMapping).isNotNull();
    assertThat(resultListMapping.getTotal()).isPositive();

    for (final Mapping mapping : resultListMapping.getItems()) {

      assertNotNull(mapping);

      final MapsetRef mapsetRef = mapping.getMapset();
      assertThat(mapsetRef).isNotNull();

      assertEquals("SNOMEDCT_US-ICD10CM", mapsetRef.getAbbreviation());
      assertNotNull(mapsetRef.getVersion());
      assertNotNull(mapsetRef.getPublisher());
      assertNotNull(mapsetRef.getCode());

      // mapping
      assertNotNull(mapping.getId());
      assertTrue(mapping.getActive());

      // from
      assertEquals(mapping.getFrom().getCode(), "40733004");
      assertNotNull(mapping.getFrom().getName());
      assertEquals("SNOMEDCT_US", mapping.getFrom().getTerminology());

      // to
      assertNotNull(mapping.getTo().getCode());
      assertNotNull(mapping.getTo().getName());
      assertEquals("ICD10CM", mapping.getTo().getTerminology());

      // attributes
      assertNotNull(mapping.getAttributes().get("advice"));
      assertNotNull(mapping.getAttributes().get("rule"));
      assertNotNull(mapping.getAttributes().get("priority"));
      assertNotNull(mapping.getAttributes().get("group"));

      assertNotNull(mapping.getType());
    }
  }

  /**
   * Test find mapset by from code 2.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFindMapsetMappingsByFromCode2() throws Exception {

    final String name = "SNOMEDCT_US-ICD10CM";
    final String url = baseUrl + "/mapset/" + name + "/mapping?query=from.code:40733004";

    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListMapping resultListMapping =
        objectMapper.readValue(content, ResultListMapping.class);
    assertThat(resultListMapping).isNotNull();
    assertThat(resultListMapping.getTotal()).isPositive();

    for (final Mapping mapping : resultListMapping.getItems()) {

      assertNotNull(mapping);

      final MapsetRef mapsetRef = mapping.getMapset();
      assertThat(mapsetRef).isNotNull();
      // assertNotNull(mapsetRef.getActive());
      assertEquals("SNOMEDCT_US-ICD10CM", mapsetRef.getAbbreviation());
      assertNotNull(mapsetRef.getVersion());
      assertNotNull(mapsetRef.getPublisher());
      assertNotNull(mapsetRef.getCode());

      // mapping
      assertNotNull(mapping.getId());
      assertTrue(mapping.getActive());

      // from
      assertEquals(mapping.getFrom().getCode(), "40733004");
      assertNotNull(mapping.getFrom().getName());
      assertEquals("SNOMEDCT_US", mapping.getFrom().getTerminology());

      // to
      assertNotNull(mapping.getTo().getCode());
      assertNotNull(mapping.getTo().getName());
      assertEquals("ICD10CM", mapping.getTo().getTerminology());

      // attributes
      assertNotNull(mapping.getAttributes().get("advice"));
      assertNotNull(mapping.getAttributes().get("rule"));
      assertNotNull(mapping.getAttributes().get("priority"));
      assertNotNull(mapping.getAttributes().get("group"));

      assertNotNull(mapping.getType());
    }
  }

  /**
   * Test find mapset by id not found.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindMapsetByIdNotFound() throws Exception {

    final String id = "2a545e12-04eb-48ee-b988-c17346b4e05FAKE";
    final String url = baseUrl + "/mapset/" + id;
    LOGGER.info("Testing url - {}", url);
    mockMvc.perform(get(url)).andExpect(status().isNotFound()).andReturn();
  }

  /**
   * Test concept search with expression.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testConceptSearchWithExpression() throws Exception {
    final String terminology = "SNOMEDCT_US";
    final String expression = "<<128927009";
    final String url = baseUrl + "/concept?terminology=" + terminology + "&expression=" + expression
        + "&include=minimal&limit=100";
    LOGGER.info(" Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConcept conceptList = objectMapper.readValue(content, ResultListConcept.class);
    assertThat(conceptList).isNotNull();
    assertFalse(conceptList.getItems().isEmpty());
    assertThat(conceptList.getTotal()).isEqualTo(48);
    boolean found = false;
    for (final Concept concept : conceptList.getItems()) {
      assertThat(concept).isNotNull();
      assertThat(concept.getTerminology()).isEqualTo(terminology);
      if ("128927009".equals(concept.getCode())) {
        found = true;
      }
    }
    assertTrue(found, "Ancestor code should have 1 128927009 Concept");

  }

  /**
   * Test concept search with browser handler and LNC concept name.
   *
   * @throws Exception the exception
   */
  // TODO: bring this test back
  @Test
  @Order(FIND)
  public void testConceptSearchWithBrowserHandlerAndLNCConceptName() throws Exception {
    final String url = "/concept?terminology=LNC&query=Diagnosis.primary:Imp:Pt:^Patient:Nom"
        + "&offset=0&limit=20&leaf=false&include=semanticTypes&handler=browser";
    LOGGER.info(" Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConcept conceptList = objectMapper.readValue(content, ResultListConcept.class);
    assertThat(conceptList).isNotNull();
    assertFalse(conceptList.getItems().isEmpty());
    for (final Concept concept : conceptList.getItems()) {
      LOGGER.info(" concept.name = {}", concept.getName());
      assertThat(concept.getName().contains("Diagnosis.primary:Imp:Pt:^Patient:Nom"));
    }
  }

  /**
   * Test get subsets.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetSubsets() throws Exception {
    final String url = baseUrl + "/subset";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListSubset subsetList =
        objectMapper.readValue(content, new TypeReference<ResultListSubset>() {
          // n/a
        });
    assertThat(subsetList).isNotNull();
    assertFalse(subsetList.getItems().isEmpty());
    for (final Subset subset : subsetList.getItems()) {
      assertThat(subset).isNotNull();
      assertThat(subset.getId()).isNotNull();
      assertThat(subset.getAbbreviation()).isNotNull();
      assertThat(subset.getName()).isNotNull();
      // assertThat(subset.getTerminology()).isNotNull();
      assertThat(subset.getPublisher()).isNotNull();
      assertThat(subset.getVersion()).isNotNull();
    }
  }

  /**
   * Test get subset by id.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetSubsetById() throws Exception {
    // Get a subset dynamically to avoid hardcoded ID issues
    final Subset testSubset = getSubsetByAbbreviation("SNOMEDCT_US-EXTENSION");

    final String url = baseUrl + "/subset/" + testSubset.getId();
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final Subset subset = objectMapper.readValue(content, Subset.class);
    assertThat(subset).isNotNull();
    assertEquals(testSubset.getId(), subset.getId());
    assertEquals(testSubset.getAbbreviation(), subset.getAbbreviation());
    assertEquals(testSubset.getName(), subset.getName());
    assertEquals(testSubset.getPublisher(), subset.getPublisher());
    assertEquals(testSubset.getVersion(), subset.getVersion());
  }

  /**
   * Test get subset by id.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetSubsetWithQuery1() throws Exception {

    final String url = baseUrl + "/subset/SNOMEDCT_US-EXTENSION/member?query=icd&offset=0&limit=10";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final Subset subset = objectMapper.readValue(content, Subset.class);
    assertThat(subset).isNotNull();

  }

  /**
   * Test get subset by id.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetSubsetWithQuery2() throws Exception {

    final String url = baseUrl + "/subset/SNOMEDCT_US-EXTENSION/member?query=ICD&offset=0&limit=10";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final Subset subset = objectMapper.readValue(content, Subset.class);
    assertThat(subset).isNotNull();

  }

  /**
   * Test get subset members.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetSubsetMembers() throws Exception {
    // Get a subset dynamically to avoid hardcoded ID issues
    final Subset testSubset = getSubsetByAbbreviation("SNOMEDCT_US-EXTENSION");

    final String subsetId = testSubset.getId();
    final String url = baseUrl + "/subset/" + subsetId + "/member";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListSubsetMember subsetMemberList =
        objectMapper.readValue(content, ResultListSubsetMember.class);
    assertThat(subsetMemberList).isNotNull();
    assertFalse(subsetMemberList.getItems().isEmpty());
    for (final SubsetMember subsetMember : subsetMemberList.getItems()) {
      assertThat(subsetMember).isNotNull();
      assertThat(subsetMember.getId()).isNotNull();
      assertThat(subsetMember.getCode()).isNotNull();
      assertThat(subsetMember.getName()).isNotNull();
      final SubsetRef subsetRef = subsetMember.getSubset();
      assertThat(subsetRef).isNotNull();
      assertThat(subsetRef.getAbbreviation()).isNotNull();
      assertThat(subsetRef.getPublisher()).isNotNull();
      assertThat(subsetRef.getVersion()).isNotNull();
    }
  }

  /**
   * Test get subset members by code.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testGetSubsetMembersByCode() throws Exception {
    // Get a subset dynamically to avoid hardcoded ID issues
    final Subset testSubset = getSubsetByAbbreviation("SNOMEDCT_US-EXTENSION");

    final String subsetId = testSubset.getId();
    final String url = baseUrl + "/subset/" + subsetId + "/member";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListSubsetMember subsetMemberList =
        objectMapper.readValue(content, ResultListSubsetMember.class);
    assertThat(subsetMemberList).isNotNull();
    assertFalse(subsetMemberList.getItems().isEmpty());
    for (final SubsetMember subsetMember : subsetMemberList.getItems()) {
      assertThat(subsetMember).isNotNull();
      assertThat(subsetMember.getId()).isNotNull();
      assertThat(subsetMember.getCode()).isNotNull();
      assertThat(subsetMember.getName()).isNotNull();
      final SubsetRef subsetRef = subsetMember.getSubset();
      assertThat(subsetRef).isNotNull();
      assertThat(subsetRef.getAbbreviation()).isNotNull();
      assertThat(subsetRef.getPublisher()).isNotNull();
      assertThat(subsetRef.getVersion()).isNotNull();
    }
  }

  /**
   * Test delete terminology.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(DELETE)
  public void testDeleteTerminology() throws Exception {

    // find a terminology that can be deleted
    String url = baseUrl + "/terminology";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    final ResultListTerminology terminologyList =
        objectMapper.readValue(content, ResultListTerminology.class);
    assertThat(terminologyList).isNotNull();
    assertThat(terminologyList.getTotal()).isPositive();
    final Terminology terminologyToDelete = terminologyList.getItems().get(0);

    // Now, delete the terminology
    url = baseUrl + "/terminology/" + terminologyToDelete.getId();
    LOGGER.info("Testing url - {}", url);
    mockMvc.perform(delete(url)).andExpect(status().isAccepted()).andReturn();

    // Verify the terminology has been deleted
    url = baseUrl + "/terminology/" + terminologyToDelete.getId();
    LOGGER.info("Testing url - {}", url);
    mockMvc.perform(get(url)).andExpect(status().isNotFound()).andReturn();
  }

  /**
   * Test delete mapset.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(DELETE)
  public void testDeleteMapset() throws Exception {

    // find a mapset that can be deleted
    String url = baseUrl + "/mapset";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    final ResultListMapset mapsetList = objectMapper.readValue(content, ResultListMapset.class);
    assertThat(mapsetList).isNotNull();
    assertThat(mapsetList.getTotal()).isPositive();
    final Mapset mapsetToDelete = mapsetList.getItems().get(0);

    // Now, delete the mapset
    url = baseUrl + "/mapset/" + mapsetToDelete.getId();
    LOGGER.info("Testing url - {}", url);
    mockMvc.perform(delete(url)).andExpect(status().isAccepted()).andReturn();

    // Verify the mapset has been deleted
    url = baseUrl + "/mapset/" + mapsetToDelete.getId();
    LOGGER.info("Testing url - {}", url);
    mockMvc.perform(get(url)).andExpect(status().isNotFound()).andReturn();

  }

  /**
   * Test delete subset.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(DELETE)
  public void testDeleteSubset() throws Exception {

    // find a subset that can be deleted
    String url = baseUrl + "/subset";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    final ResultListSubset subsetList =
        objectMapper.readValue(content, new TypeReference<ResultListSubset>() {
          // n/a
        });
    assertThat(subsetList).isNotNull();
    assertThat(subsetList.getTotal()).isPositive();
    final Subset subsetToDelete = subsetList.getItems().get(0);

    // Now, delete the subset
    url = baseUrl + "/subset/" + subsetToDelete.getId();
    LOGGER.info("Testing url - {}", url);
    mockMvc.perform(delete(url)).andExpect(status().isAccepted()).andReturn();

    // Verify the subset has been deleted
    url = baseUrl + "/subset/" + subsetToDelete.getId();
    LOGGER.info("Testing url - {}", url);
    mockMvc.perform(get(url)).andExpect(status().isNotFound()).andReturn();
  }

  /**
   * Test terminologies sorted by name.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testTerminologiesSortedByName() throws Exception {
    final String url = baseUrl + "/terminology?sort=name&ascending=true&limit=10";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListTerminology terminologyList =
        objectMapper.readValue(content, ResultListTerminology.class);
    assertThat(terminologyList).isNotNull();
    assertThat(terminologyList.getTotal()).isPositive();
    assertFalse(terminologyList.getItems().isEmpty());

    // Verify results are sorted by name
    final List<Terminology> items = terminologyList.getItems();
    for (int i = 1; i < items.size(); i++) {
      final String currentName = items.get(i).getName();
      final String previousName = items.get(i - 1).getName();
      assertTrue(currentName.compareToIgnoreCase(previousName) >= 0,
          "Terminologies should be sorted by name. Found '" + currentName + "' after '"
              + previousName + "'");
    }
  }

  /**
   * Test concepts sorted by name.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testConceptsSortedByName() throws Exception {
    final String terminology = "ICD10CM";
    final String url =
        baseUrl + "/concept?terminology=" + terminology + "&sort=name&ascending=true&limit=10";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListConcept conceptList = objectMapper.readValue(content, ResultListConcept.class);
    assertThat(conceptList).isNotNull();
    assertFalse(conceptList.getItems().isEmpty());

    // Log the first few items to debug sorting
    final List<Concept> items = conceptList.getItems();
    LOGGER.info("First 10 concepts:");
    for (int i = 0; i < Math.min(10, items.size()); i++) {
      LOGGER.info("  {}: {}", i, items.get(i).getName());
    }

    // Verify results are sorted by name
    for (int i = 1; i < items.size(); i++) {
      final String currentName = items.get(i).getName();
      final String previousName = items.get(i - 1).getName();
      if (currentName.compareToIgnoreCase(previousName) < 0) {
        LOGGER.error("Sorting violation at position {}: '{}' should come before '{}'", i,
            currentName, previousName);
        // Log more context around the violation
        for (int j = Math.max(0, i - 3); j <= Math.min(items.size() - 1, i + 2); j++) {
          LOGGER.error("  Position {}: {}", j, items.get(j).getName());
        }
      }
      assertTrue(currentName.compareToIgnoreCase(previousName) >= 0,
          "Concepts should be sorted by name. Found '" + currentName + "' after '" + previousName
              + "'");
    }
  }

  /**
   * Test terms sorted by name.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testTermsSortedByName() throws Exception {
    final String terminology = "RXNORM";
    final String url =
        baseUrl + "/term?terminology=" + terminology + "&sort=name&ascending=true&limit=10";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListTerm termList = objectMapper.readValue(content, ResultListTerm.class);
    assertThat(termList).isNotNull();
    assertFalse(termList.getItems().isEmpty());

    LOGGER.info(" termList = {}", termList);

    // Verify results are sorted by name
    final List<Term> items = termList.getItems();
    for (int i = 1; i < items.size(); i++) {
      final String currentName = items.get(i).getName();
      final String previousName = items.get(i - 1).getName();
      assertTrue(currentName.compareToIgnoreCase(previousName) >= 0,
          "Terms should be sorted by name. Found '" + currentName + "' after '" + previousName
              + "'");
    }
  }

  /**
   * Test metadata sorted by name.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testMetadataSortedByName() throws Exception {
    final String terminology = "LNC";
    final String url =
        baseUrl + "/metadata?terminology=" + terminology + "&sort=name&ascending=true&limit=10";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListMetadata metadataList =
        objectMapper.readValue(content, ResultListMetadata.class);
    assertThat(metadataList).isNotNull();
    assertFalse(metadataList.getItems().isEmpty());

    // Verify results are sorted by name (note: some metadata may not have
    // names)
    final List<Metadata> items = metadataList.getItems();
    for (int i = 1; i < items.size(); i++) {
      final String currentName = items.get(i).getName();
      final String previousName = items.get(i - 1).getName();

      // Handle null names - they should come first in ascending order
      if (currentName == null && previousName == null) {
        continue; // Both null, order doesn't matter
      } else if (currentName == null) {
        assertTrue(false, "Null names should come first in ascending sort order");
      } else if (previousName == null) {
        continue; // Previous is null, current is not, which is correct for
        // ascending
      } else {
        assertTrue(currentName.compareToIgnoreCase(previousName) >= 0,
            "Metadata should be sorted by name. Found '" + currentName + "' after '" + previousName
                + "'");
      }
    }
  }

  /**
   * Test subsets sorted by name.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testSubsetsSortedByName() throws Exception {
    final String url = baseUrl + "/subset?sort=name&ascending=true&limit=10";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();
    final ResultListSubset subsetList =
        objectMapper.readValue(content, new TypeReference<ResultListSubset>() {
          // n/a
        });
    assertThat(subsetList).isNotNull();
    assertFalse(subsetList.getItems().isEmpty());

    // Copy array and sort it by name
    List<Subset> sortedNames = new ArrayList<>(subsetList.getItems());
    Collections.sort(sortedNames, new Comparator<Subset>() {
      @Override
      public int compare(final Subset s1, final Subset s2) {
        final String name1 = s1.getName() != null ? s1.getName() : "";
        final String name2 = s2.getName() != null ? s2.getName() : "";
        return name1.compareTo(name2);
      }
    });

    // loop through the sorted names and compare to the original list
    for (int i = 0; i < sortedNames.size(); i++) {
      final Subset sortedName = sortedNames.get(i);
      final Subset originalName = subsetList.getItems().get(i);
      assertEquals(sortedName.getName(), originalName.getName(),
          "Subsets should be sorted by name");
    }

  }

  /**
   * Test value set H 17 body site subset loaded.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testValueSetH17BodySiteSubsetLoaded() throws Exception {
    final String url = baseUrl + "/subset";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    final ResultListSubset subsetList =
        objectMapper.readValue(content, new TypeReference<ResultListSubset>() {
          // n/a
        });
    assertThat(subsetList).isNotNull();
    final Subset bodySite = subsetList.getItems().stream()
        .filter(s -> "SNOMED CT Body Structures".equals(s.getAbbreviation())).findFirst()
        .orElse(null);
    assertNotNull(bodySite, "Expected 'SNOMED CT Body Structures' subset");
    assertEquals("FHIR Project team", bodySite.getPublisher());
    assertEquals("4.0.1", bodySite.getVersion());
  }

  /**
   * Test value set ips personal relationship subset members.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testValueSetIpsPersonalRelationshipSubsetMembers() throws Exception {
    // Find subset by abbreviation
    final String url = baseUrl + "/subset";
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    final ResultListSubset subsetList =
        objectMapper.readValue(content, new TypeReference<ResultListSubset>() {
          // n/a
        });
    final Subset ipsRel = subsetList.getItems().stream()
        .filter(s -> "IPS Personal Relationship".equals(s.getAbbreviation())).findFirst()
        .orElse(null);
    assertNotNull(ipsRel, "Expected 'IPS Personal Relationship' subset");
    assertEquals("HL7 International", ipsRel.getPublisher());
    assertEquals("0.1.0", ipsRel.getVersion());

    // Verify members
    final String membersUrl = baseUrl + "/subset/" + ipsRel.getId() + "/member?limit=1000";
    final MvcResult mres = mockMvc.perform(get(membersUrl)).andExpect(status().isOk()).andReturn();
    final String mcontent = mres.getResponse().getContentAsString();
    final ResultListSubsetMember members =
        objectMapper.readValue(mcontent, ResultListSubsetMember.class);
    assertThat(members).isNotNull();
    assertTrue(members.getTotal() >= 39, "Should have at least 39 members");

    // Spot-check some expected codes
    final List<String> codes = members.getItems().stream().map(SubsetMember::getCode).toList();
    assertThat(codes).contains("AUNT", "SPS", "PRN", "MTH", "SON");
  }

  /**
   * Test concept hierarchy fields for SNOMEDCT_US:73211009 - validates children, parents,
   * descendants, ancestors are populated.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testConceptHierarchySnomedctUs362965005() throws Exception {
    final String terminology = "SNOMEDCT";
    final String code = "362965005";
    final String url = baseUrl + "/concept/" + terminology + "/" + code
        + "?include=children,parents,descendants,ancestors";
    LOGGER.info("Testing url - {}", url);

    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();

    final Concept concept = objectMapper.readValue(content, Concept.class);
    assertThat(concept).isNotNull();
    assertEquals(code, concept.getCode(), "Concept code should match");
    assertEquals(terminology, concept.getTerminology(), "Terminology should match");

    // Validate hierarchy fields are populated
    assertNotNull(concept.getChildren(), "Children list should not be null");
    assertNotNull(concept.getParents(), "Parents list should not be null");
    assertNotNull(concept.getDescendants(), "Descendants list should not be null");
    assertNotNull(concept.getAncestors(), "Ancestors list should not be null");

    /*
     * "children": [ {"local":false,"active":true,"name":"Disorder of cardiovascular system"
     * ,"code":"49601007","terminology":"SNOMEDCT","version":"20240101",
     * "publisher":"SANDBOX","leaf":true,"defined":true},
     * {"local":false,"active":true,"name":"Disorder of breast","code":
     * "79604008","terminology":"SNOMEDCT","version":"20240101","publisher":
     * "SANDBOX","leaf":false,"defined":true},
     * {"local":false,"active":true,"name":"Disorder of endocrine system","code"
     * :"362969004","terminology":"SNOMEDCT","version":"20240101","publisher":
     * "SANDBOX","leaf":false,"defined":true} ], "parents": [
     * {"local":false,"active":true,"name":"Disease","code":"64572001",
     * "terminology":"SNOMEDCT","version":"20240101","publisher":"SANDBOX",
     * "leaf":false,"defined":false} ], "descendants": [
     * {"local":false,"active":true,"name":"Disorder of breast","code":
     * "79604008","leaf":false,"defined":true,"level":1},
     * {"local":false,"active":true,"name":"Disorder of cardiovascular system"
     * ,"code":"49601007","leaf":true,"defined":true,"level":1},
     * {"local":false,"active":true,"name":"Disorder of endocrine system","code"
     * :"362969004","leaf":false,"defined":true,"level":1},
     * {"local":false,"active":true,"name":"Diabetes mellitus","code":"73211009"
     * ,"leaf":true,"defined":false,"level":2},
     * {"local":false,"active":true,"name":"Lesion of breast","code":"290073004"
     * ,"leaf":false,"defined":true,"level":2},
     * {"local":false,"active":true,"name":"Neoplasm of breast","code":
     * "126926005","leaf":true,"defined":true,"level":3} ], "ancestors": [
     * {"local":false,"active":true,"name":"Disease","code":"64572001","leaf":
     * false,"defined":false,"level":1},
     * {"local":false,"active":true,"name":"Clinical finding","code":"404684003"
     * ,"leaf":false,"defined":false,"level":2},
     * {"local":false,"active":true,"name":"SNOMED CT Concept","code":
     * "138875005","leaf":false,"defined":false,"level":3}
     */

    // should be empty at the end
    final Map<String, String> childrenErrors = new HashMap<>();
    final Map<String, String> parentsErrors = new HashMap<>();
    final Map<String, String> descendantsErrors = new HashMap<>();
    final Map<String, String> ancestorsErrors = new HashMap<>();

    if (concept.getChildren().size() != 3) {
      childrenErrors.put("children",
          "Should be 3 children, found: " + concept.getChildren().size());
    }
    if (concept.getParents().size() != 1) {
      parentsErrors.put("parents", "Should be 1 parent, found: " + concept.getParents().size());
    }
    if (concept.getDescendants().size() != 6) {
      descendantsErrors.put("descendants",
          "Should be 6 descendants, found: " + concept.getDescendants().size());
    }
    if (concept.getAncestors().size() != 3) {
      ancestorsErrors.put("ancestors",
          "Should be 3 ancestors, found: " + concept.getAncestors().size());
    }

    String testCode = "49601007";
    ConceptRef testConceptRef = concept.getChildren().stream()
        .filter(c -> testCode.equals(c.getCode())).findFirst().orElse(null);
    if (testConceptRef == null) {
      childrenErrors.put(testCode, "Should have child with code " + testCode);
    } else {
      if (!testConceptRef.getName().equals("Disorder of cardiovascular system")) {
        childrenErrors.put(testCode, "Should have name 'Disorder of cardiovascular system', found: "
            + testConceptRef.getName());
      }
      if (!testConceptRef.getLeaf()) {
        childrenErrors.put(testCode, "Should be leaf");
      }
      if (!testConceptRef.getDefined()) {
        childrenErrors.put(testCode, "Should be defined");
      }
    }

    final String testCode2 = "79604008";
    testConceptRef = concept.getChildren().stream().filter(c -> testCode2.equals(c.getCode()))
        .findFirst().orElse(null);
    if (testConceptRef == null) {
      childrenErrors.put(testCode2, "Should have child with code " + testCode2);
    } else {
      if (!testConceptRef.getName().equals("Disorder of breast")) {
        childrenErrors.put(testCode2,
            "Should have name 'Disorder of breast', found: " + testConceptRef.getName());
      }
      if (testConceptRef.getLeaf()) {
        childrenErrors.put(testCode2, "Should not be leaf");
      }
      if (!testConceptRef.getDefined()) {
        childrenErrors.put(testCode2, "Should be defined");
      }
    }

    final String testCode3 = "362969004";
    testConceptRef = concept.getChildren().stream().filter(c -> testCode3.equals(c.getCode()))
        .findFirst().orElse(null);
    if (testConceptRef == null) {
      childrenErrors.put(testCode3, "Should have child with code " + testCode3);
    } else {
      if (!testConceptRef.getName().equals("Disorder of endocrine system")) {
        childrenErrors.put(testCode3,
            "Should have name 'Disorder of endocrine system', found: " + testConceptRef.getName());
      }
      if (testConceptRef.getLeaf()) {
        childrenErrors.put(testCode3, "Should not be leaf");
      }
      if (!testConceptRef.getDefined()) {
        childrenErrors.put(testCode3, "Should be defined");
      }
    }

    // parents
    final String parentCode = "64572001";
    testConceptRef = concept.getParents().stream().filter(c -> parentCode.equals(c.getCode()))
        .findFirst().orElse(null);
    if (testConceptRef == null) {
      parentsErrors.put(parentCode, "Should have parent with code " + parentCode);
    } else {
      if (!testConceptRef.getName().equals("Disease")) {
        parentsErrors.put(parentCode,
            "Should have name 'Disease', found: " + testConceptRef.getName());
      }
      if (testConceptRef.getLeaf()) {
        parentsErrors.put(parentCode, "Should not be leaf");
      }
      if (testConceptRef.getDefined()) {
        parentsErrors.put(parentCode, "Should not be defined");
      }
    }

    // descendants
    final String descCode1 = "79604008";
    testConceptRef = concept.getDescendants().stream().filter(c -> descCode1.equals(c.getCode()))
        .findFirst().orElse(null);
    if (testConceptRef == null) {
      descendantsErrors.put(descCode1, "Should have descendant with code " + descCode1);
    } else {
      if (!testConceptRef.getName().equals("Disorder of breast")) {
        descendantsErrors.put(descCode1,
            "Should have name 'Disorder of breast', found: " + testConceptRef.getName());
      }
      if (testConceptRef.getLeaf()) {
        descendantsErrors.put(descCode1, "Should not be leaf");
      }
      if (!testConceptRef.getDefined()) {
        descendantsErrors.put(descCode1, "Should be defined");
      }
      if (testConceptRef.getLevel() != 1) {
        descendantsErrors.put(descCode1,
            "Should have level 1, found: " + testConceptRef.getLevel());
      }
    }

    final String descCode2 = "49601007";
    testConceptRef = concept.getDescendants().stream().filter(c -> descCode2.equals(c.getCode()))
        .findFirst().orElse(null);
    if (testConceptRef == null) {
      descendantsErrors.put(descCode2, "Should have descendant with code " + descCode2);
    } else {
      if (!testConceptRef.getName().equals("Disorder of cardiovascular system")) {
        descendantsErrors.put(descCode2,
            "Should have name 'Disorder of cardiovascular system', found: "
                + testConceptRef.getName());
      }
      if (!testConceptRef.getLeaf()) {
        descendantsErrors.put(descCode2, "Should be leaf");
      }
      if (!testConceptRef.getDefined()) {
        descendantsErrors.put(descCode2, "Should be defined");
      }
      if (testConceptRef.getLevel() != 1) {
        descendantsErrors.put(descCode2,
            "Should have level 1, found: " + testConceptRef.getLevel());
      }
    }

    final String descCode3 = "362969004";
    testConceptRef = concept.getDescendants().stream().filter(c -> descCode3.equals(c.getCode()))
        .findFirst().orElse(null);
    if (testConceptRef == null) {
      descendantsErrors.put(descCode3, "Should have descendant with code " + descCode3);
    } else {
      if (!testConceptRef.getName().equals("Disorder of endocrine system")) {
        descendantsErrors.put(descCode3,
            "Should have name 'Disorder of endocrine system', found: " + testConceptRef.getName());
      }
      if (testConceptRef.getLeaf()) {
        descendantsErrors.put(descCode3, "Should not be leaf");
      }
      if (!testConceptRef.getDefined()) {
        descendantsErrors.put(descCode3, "Should be defined");
      }
      if (testConceptRef.getLevel() != 1) {
        descendantsErrors.put(descCode3,
            "Should have level 1, found: " + testConceptRef.getLevel());
      }
    }

    final String descCode4 = "73211009";
    testConceptRef = concept.getDescendants().stream().filter(c -> descCode4.equals(c.getCode()))
        .findFirst().orElse(null);
    if (testConceptRef == null) {
      descendantsErrors.put(descCode4, "Should have descendant with code " + descCode4);
    } else {
      if (!testConceptRef.getName().equals("Diabetes mellitus")) {
        descendantsErrors.put(descCode4,
            "Should have name 'Diabetes mellitus', found: " + testConceptRef.getName());
      }
      if (!testConceptRef.getLeaf()) {
        descendantsErrors.put(descCode4, "Should be leaf");
      }
      if (testConceptRef.getDefined()) {
        descendantsErrors.put(descCode4, "Should not be defined");
      }
      if (testConceptRef.getLevel() != 2) {
        descendantsErrors.put(descCode4,
            "Should have level 2, found: " + testConceptRef.getLevel());
      }
    }

    final String descCode5 = "290073004";
    testConceptRef = concept.getDescendants().stream().filter(c -> descCode5.equals(c.getCode()))
        .findFirst().orElse(null);
    if (testConceptRef == null) {
      descendantsErrors.put(descCode5, "Should have descendant with code " + descCode5);
    } else {
      if (!testConceptRef.getName().equals("Lesion of breast")) {
        descendantsErrors.put(descCode5,
            "Should have name 'Lesion of breast', found: " + testConceptRef.getName());
      }
      if (testConceptRef.getLeaf()) {
        descendantsErrors.put(descCode5, "Should not be leaf");
      }
      if (!testConceptRef.getDefined()) {
        descendantsErrors.put(descCode5, "Should be defined");
      }
      if (testConceptRef.getLevel() != 2) {
        descendantsErrors.put(descCode5,
            "Should have level 2, found: " + testConceptRef.getLevel());
      }
    }

    final String descCode6 = "126926005";
    testConceptRef = concept.getDescendants().stream().filter(c -> descCode6.equals(c.getCode()))
        .findFirst().orElse(null);
    if (testConceptRef == null) {
      descendantsErrors.put(descCode6, "Should have descendant with code " + descCode6);
    } else {
      if (!testConceptRef.getName().equals("Neoplasm of breast")) {
        descendantsErrors.put(descCode6,
            "Should have name 'Neoplasm of breast', found: " + testConceptRef.getName());
      }
      if (!testConceptRef.getLeaf()) {
        descendantsErrors.put(descCode6, "Should be leaf");
      }
      if (!testConceptRef.getDefined()) {
        descendantsErrors.put(descCode6, "Should be defined");
      }
      if (testConceptRef.getLevel() != 3) {
        descendantsErrors.put(descCode6,
            "Should have level 3, found: " + testConceptRef.getLevel());
      }
    }

    // ancestors
    final String ancCode1 = "64572001";
    testConceptRef = concept.getAncestors().stream().filter(c -> ancCode1.equals(c.getCode()))
        .findFirst().orElse(null);
    if (testConceptRef == null) {
      ancestorsErrors.put(ancCode1, "Should have ancestor with code " + ancCode1);
    } else {
      if (!testConceptRef.getName().equals("Disease")) {
        ancestorsErrors.put(ancCode1,
            "Should have name 'Disease', found: " + testConceptRef.getName());
      }
      if (testConceptRef.getLeaf()) {
        ancestorsErrors.put(ancCode1, "Should not be leaf");
      }
      if (testConceptRef.getDefined()) {
        ancestorsErrors.put(ancCode1, "Should not be defined");
      }
      if (testConceptRef.getLevel() != 1) {
        ancestorsErrors.put(ancCode1, "Should have level 1, found: " + testConceptRef.getLevel());
      }
    }

    final String ancCode2 = "404684003";
    testConceptRef = concept.getAncestors().stream().filter(c -> ancCode2.equals(c.getCode()))
        .findFirst().orElse(null);
    if (testConceptRef == null) {
      ancestorsErrors.put(ancCode2, "Should have ancestor with code " + ancCode2);
    } else {
      if (!testConceptRef.getName().equals("Clinical finding")) {
        ancestorsErrors.put(ancCode2,
            "Should have name 'Clinical finding', found: " + testConceptRef.getName());
      }
      if (testConceptRef.getLeaf()) {
        ancestorsErrors.put(ancCode2, "Should not be leaf");
      }
      if (testConceptRef.getDefined()) {
        ancestorsErrors.put(ancCode2, "Should not be defined");
      }
      if (testConceptRef.getLevel() != 2) {
        ancestorsErrors.put(ancCode2, "Should have level 2, found: " + testConceptRef.getLevel());
      }
    }

    final String ancCode3 = "138875005";
    testConceptRef = concept.getAncestors().stream().filter(c -> ancCode3.equals(c.getCode()))
        .findFirst().orElse(null);
    if (testConceptRef == null) {
      ancestorsErrors.put(ancCode3, "Should have ancestor with code " + ancCode3);
    } else {
      if (!testConceptRef.getName().equals("SNOMED CT Concept")) {
        ancestorsErrors.put(ancCode3,
            "Should have name 'SNOMED CT Concept', found: " + testConceptRef.getName());
      }
      if (testConceptRef.getLeaf()) {
        ancestorsErrors.put(ancCode3, "Should not be leaf");
      }
      if (testConceptRef.getDefined()) {
        ancestorsErrors.put(ancCode3, "Should not be defined");
      }
      if (testConceptRef.getLevel() != 3) {
        ancestorsErrors.put(ancCode3, "Should have level 3, found: " + testConceptRef.getLevel());
      }
    }

    LOGGER.error("Children errors: {}", childrenErrors);
    LOGGER.error("Parents errors: {}", parentsErrors);
    LOGGER.error("Descendants errors: {}", descendantsErrors);
    LOGGER.error("Ancestors errors: {}", ancestorsErrors);

    assertEquals(0, childrenErrors.size(), "Children errors = " + childrenErrors);
    assertEquals(0, parentsErrors.size(), "Children errors = " + parentsErrors);
    assertEquals(0, descendantsErrors.size(), "Children errors = " + descendantsErrors);
    assertEquals(0, ancestorsErrors.size(), "Children errors = " + ancestorsErrors);

    LOGGER.info("Successfully validated hierarchy fields for concept {}", code);
  }

  /**
   * Test autocomplete single partial word.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testAutocompleteSingle() throws Exception {
    String url = baseUrl + "/autocomplete?terminology=SNOMEDCT&query=dia&limit=10";
    MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    // expect array of strings
    List<String> suggestions = objectMapper.readValue(content, new TypeReference<List<String>>() {
      // n/a
    });
    assertThat(suggestions).isNotNull();
    assertThat(suggestions).hasSize(10);
    for (final String suggestion : suggestions) {
      assertThat(suggestion.toLowerCase()).contains("dia");
    }

    // change limit
    url = baseUrl + "/autocomplete?terminology=SNOMEDCT&query=dia&limit=15";
    result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    suggestions = objectMapper.readValue(content, new TypeReference<List<String>>() {
      // n/a
    });
    assertThat(suggestions).isNotNull();
    assertTrue(suggestions.size() >= 10,
        "Should be at least 10 suggestions, got: " + suggestions.size());
    assertTrue(suggestions.size() <= 15,
        "Should be at most 15 suggestions, got: " + suggestions.size());
    for (final String suggestion : suggestions) {
      assertThat(suggestion.toLowerCase()).contains("dia");
    }
  }

  /**
   * Test autocomplete multiple partial word.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testAutocompleteMultiple() throws Exception {
    final String url = baseUrl + "/autocomplete?terminology=SNOMEDCT&query=dia mel&limit=10";
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    // expect array of strings
    final List<String> suggestions =
        objectMapper.readValue(content, new TypeReference<List<String>>() {
          // n/a
        });
    assertThat(suggestions).isNotNull();
    assertThat(suggestions.size()).isPositive();
    for (final String suggestion : suggestions) {
      assertThat(suggestion.toLowerCase()).contains("dia");
      assertThat(suggestion.toLowerCase()).contains("mel");
    }
  }

  /**
   * Test autocomplete with multiple terminolgies.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testAutocompleteMultipleTerminologies() throws Exception {
    final String url = baseUrl + "/autocomplete?terminology=SNOMEDCT,LNC&query=dia mel&limit=100";
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    // expect array of strings
    final List<String> suggestions =
        objectMapper.readValue(content, new TypeReference<List<String>>() {
          // n/a
        });
    assertThat(suggestions).isNotNull();
    assertTrue(suggestions.size() >= 20,
        "Should be at least 20 suggestions, got: " + suggestions.size());
    for (final String suggestion : suggestions) {
      assertThat(suggestion.toLowerCase()).contains("dia");
      assertThat(suggestion.toLowerCase()).contains("mel");
    }
  }

  /**
   * Test autocomplete no expected results.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testAutocompleteNoResults() throws Exception {
    final String url = baseUrl + "/autocomplete?terminology=RXNORM&query=noresults&limit=10";
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    // expect empty array
    final String content = result.getResponse().getContentAsString();
    final List<String> suggestions =
        objectMapper.readValue(content, new TypeReference<List<String>>() {
          // n/a
        });
    assertThat(suggestions).isNotNull();
    assertThat(suggestions).isEmpty();
  }

  /**
   * Gets the concept by code.
   *
   * @param terminology the terminology
   * @param code the code
   * @return the concept by code
   * @throws Exception the exception
   */
  private Concept getConceptByCode(final String terminology, final String code) throws Exception {

    final String url = baseUrl + "/concept/" + terminology + "/" + code;
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    return objectMapper.readValue(content, Concept.class);
  }

  /**
   * Gets the mapset by abbreviation.
   *
   * @param abbreviation the abbreviation
   * @return the mapset by abbreviation
   * @throws Exception the exception
   */
  private Mapset getMapsetByAbbreviation(final String abbreviation) throws Exception {
    final String url = baseUrl + "/mapset";
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    final ResultListMapset mapsetList =
        objectMapper.readValue(content, new TypeReference<ResultListMapset>() {
          // n/a
        });

    return mapsetList.getItems().stream()
        .filter(mapset -> abbreviation.equals(mapset.getAbbreviation())).findFirst().orElseThrow(
            () -> new RuntimeException("Subset with abbreviation " + abbreviation + " not found"));
  }

  /**
   * Gets the subset by abbreviation.
   *
   * @param abbreviation the abbreviation
   * @return the subset by abbreviation
   * @throws Exception the exception
   */
  private Subset getSubsetByAbbreviation(final String abbreviation) throws Exception {
    final String url = baseUrl + "/subset";
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" subset content = {}", content);
    final ResultListSubset subsetList =
        objectMapper.readValue(content, new TypeReference<ResultListSubset>() {
          // n/a
        });

    return subsetList.getItems().stream()
        .filter(subset -> abbreviation.equals(subset.getAbbreviation())).findFirst().orElseThrow(
            () -> new RuntimeException("Subset with abbreviation " + abbreviation + " not found"));
  }

  @AfterAll
  public static void teardown() {
    // There are tests that delete content. So any subsequent tests should
    // re-setup the data
    setSetupOnce(false);
  }

  /**
   * Test that the fake ConceptMap (CPT-HCPCS) is loaded by checking the count of mapsets.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFakeConceptMapLoaded() throws Exception {
    final String url = baseUrl + "/mapset";
    LOGGER.info("Testing url - {}", url);
    final MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);
    assertThat(content).isNotNull();

    final ResultListMapset mapsetList = objectMapper.readValue(content, ResultListMapset.class);
    assertThat(mapsetList).isNotNull();
    assertThat(mapsetList.getTotal()).isPositive();

    // Check that we have at least one mapset (the fake CPT-HCPCS one should be
    // loaded)
    final int totalMapsets = mapsetList.getItems().size();
    LOGGER.info("Total mapsets loaded: {}", totalMapsets);
    assertThat(totalMapsets).isGreaterThanOrEqualTo(1);

    // Look for the fake ConceptMap by checking for CPT-HCPCS mapping
    boolean foundFakeMapset = false;
    for (final Mapset mapset : mapsetList.getItems()) {
      LOGGER.info("Checking mapset: {} - {}", mapset.getAbbreviation(), mapset.getName());
      if (mapset.getFromTerminology() != null && mapset.getToTerminology() != null) {
        final String fromTerm = mapset.getFromTerminology().toLowerCase();
        final String toTerm = mapset.getToTerminology().toLowerCase();
        if ((fromTerm.contains("cpt") && toTerm.contains("hcpcs"))
            || (fromTerm.contains("hcpcs") && toTerm.contains("cpt"))) {
          foundFakeMapset = true;
          LOGGER.info("Found fake ConceptMap: {} -> {}", mapset.getFromTerminology(),
              mapset.getToTerminology());
          break;
        }
      }
    }

    // Verify that the fake ConceptMap was loaded
    assertTrue(foundFakeMapset, "Fake ConceptMap (CPT-HCPCS) should be loaded");
  }

  /**
   * Autocomplete without required query parameter should return 400.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testAutocompleteMissingQueryReturnsBadRequest() throws Exception {
    final String url = "/autocomplete?terminology=SNOMEDCT";
    LOGGER.info("Testing url - {}", url);
    mockMvc.perform(get(url)).andExpect(status().isBadRequest()).andReturn();
  }

  /**
   * Tree positions for a non-existent concept id should return 404.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFindTreePositionsNotFound() throws Exception {
    final String fakeConceptId = "00000000-0000-0000-0000-00000000ffff";
    final String url = "/concept/" + fakeConceptId + "/trees";
    LOGGER.info("Testing url - {}", url);
    mockMvc.perform(get(url)).andExpect(status().isNotFound()).andReturn();
  }

  /**
   * Tree children for a non-existent concept id should return 404.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFindTreePositionChildrenNotFound() throws Exception {
    final String fakeConceptId = "00000000-0000-0000-0000-00000000ffff";
    final String url = "/concept/" + fakeConceptId + "/trees/children";
    LOGGER.info("Testing url - {}", url);
    mockMvc.perform(get(url)).andExpect(status().isNotFound()).andReturn();
  }

  /**
   * Mapset mappings for an unknown mapset id/abbreviation should return 417.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testFindMapsetMappingsUnknownMapsetReturnsExpectationFailed() throws Exception {
    final String url = "/mapset/DOES_NOT_EXIST/mapping";
    LOGGER.info("Testing url - {}", url);
    mockMvc.perform(get(url)).andExpect(status().isExpectationFailed()).andReturn();
  }

  /**
   * Concept bulk with empty body should return a default "No match" entry.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testConceptBulkEmptyBodyReturnsNoMatch() throws Exception {
    final String url = "/concept/bulk?terminology=LNC&limit=5";
    LOGGER.info("Testing url - {}", url);

    final MvcResult result =
        mockMvc.perform(post(url).contentType(MediaType.TEXT_PLAIN).content(""))
            .andExpect(status().isOk()).andReturn();

    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);

    final List<ResultListConcept> lists =
        objectMapper.readValue(content, new TypeReference<List<ResultListConcept>>() {
          // n/a
        });

    assertThat(lists).isNotNull();
    assertThat(lists.size()).isGreaterThanOrEqualTo(1);
    final ResultListConcept first = lists.get(0);
    assertThat(first.getItems()).isNotNull();
    assertThat(first.getItems().size()).isGreaterThanOrEqualTo(1);

    final Concept c = first.getItems().get(0);
    assertThat(c).isNotNull();
    assertThat(c.getName()).isEqualTo("No match");
  }

  /**
   * Unknown terminology/code should return 417 with standard error JSON keys.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testErrorBodyForUnknownTerminologyAndCode() throws Exception {
    final String url = "/concept/DOES_NOT_EXIST/DOES_NOT_EXIST";
    LOGGER.info("Testing url - {}", url);

    final MvcResult result =
        mockMvc.perform(get(url)).andExpect(status().isExpectationFailed()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);

    final Map<String, Object> body =
        objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {
          // n/a
        });

    assertThat(body).isNotNull();
    assertThat(body).containsKeys("status", "error", "message");
    // local and timestamp are produced by our RestException/Error model
    assertThat(body).containsKeys("local", "timestamp");
  }

  /**
   * Unknown mapset mapping should return 417 with standard error JSON keys.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testErrorBodyForUnknownMapsetMapping() throws Exception {
    final String url = "/mapset/DOES_NOT_EXIST/mapping";
    LOGGER.info("Testing url - {}", url);

    final MvcResult result =
        mockMvc.perform(get(url)).andExpect(status().isExpectationFailed()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);

    final Map<String, Object> body =
        objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {
          // n/a
        });

    assertThat(body).isNotNull();
    assertThat(body).containsKeys(ERROR_FIELDS);
  }

  /**
   * Unknown subset members should return 417 with standard error JSON keys.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testErrorBodyForUnknownSubsetMembers() throws Exception {
    final String url = "/subset/DOES_NOT_EXIST/member";
    LOGGER.info("Testing url - {}", url);

    final MvcResult result =
        mockMvc.perform(get(url)).andExpect(status().isExpectationFailed()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);

    final Map<String, Object> body =
        objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {
          // n/a
        });

    assertThat(body).isNotNull();
    assertThat(body).containsKeys(ERROR_FIELDS);
  }

  /**
   * Missing codes parameter should return 400 with standard error JSON keys.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testErrorBodyForMissingRequiredParameter() throws Exception {
    final String url = "/concept/LNC"; // no codes param
    LOGGER.info("Testing url - {}", url);

    final MvcResult result =
        mockMvc.perform(get(url)).andExpect(status().isBadRequest()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);

    final Map<String, Object> body =
        objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {
          // n/a
        });

    assertThat(body).isNotNull();
    LOGGER.info(" body = {}", body);
    assertThat(body).containsKeys("timestamp", "message", "status", "error");
  }

  /**
   * Missing codes parameter should return 417 with standard error JSON keys.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testErrorBodyForEmptyCodesParameter() throws Exception {
    final String url = "/concept/LNC?codes=";
    LOGGER.info("Testing url - {}", url);

    final MvcResult result =
        mockMvc.perform(get(url)).andExpect(status().isExpectationFailed()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);

    final Map<String, Object> body =
        objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {
          // n/a
        });

    assertThat(body).isNotNull();
    assertThat(body).containsKeys(ERROR_FIELDS);
  }

  /**
   * Expression with multiple terminologies should return 417 and standard error
   * JSON keys.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(FIND)
  public void testErrorBodyForExpressionWithMultipleTerminologies() throws Exception {
    final String url = "/concept?terminology=SNOMEDCT,LNC&expression=%3C%3C128927009";
    LOGGER.info("Testing url - {}", url);

    final MvcResult result =
        mockMvc.perform(get(url)).andExpect(status().isExpectationFailed()).andReturn();
    final String content = result.getResponse().getContentAsString();
    LOGGER.info(" content = {}", content);

    final Map<String, Object> body =
        objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {
          // n/a
        });

    assertThat(body).isNotNull();
    assertThat(body).containsKeys(ERROR_FIELDS);
  }
}
