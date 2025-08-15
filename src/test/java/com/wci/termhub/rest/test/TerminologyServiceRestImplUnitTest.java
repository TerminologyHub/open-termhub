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

import java.util.List;

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
import com.wci.termhub.test.AbstractTerminologyServerTest;

/**
 * Unit tests for TerminologyServiceRestImpl. All systems tests are order 1. All
 * get/find tests are order 10. All delete tests are order 20.
 */
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class TerminologyServiceRestImplUnitTest extends AbstractTerminologyServerTest {

  /** The logger. */
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
    final String id = "177f2263-fe04-4f1f-b0e6-9b351ab8baa9";
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
    final String id = "340c926f-9ad6-4f1b-b230-dc4ca14575ab";
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
    final String id = "340c926f-9ad6-4f1b-b230-dc4ca14575ab";
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
      /*
       * {"id":"...","local":false,"active":true,"terminology":"SNOMEDCT_US",
       * "version":"20240301",
       * "publisher":"SANDBOX","model":"relationship","field":"uiLabel","code":
       * "Attributes","rank":0}
       */
      // has no name
      // assertThat(metadata.getName()).isNotNull();
    }
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
      assertThat(concept.getTerminology()).isEqualTo(terminology);
      LOGGER.info(" XXX {}", concept.toString());
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
      assertThat(conceptRel.getTerminology()).contains("SNOMEDCT");
      assertThat(conceptRel.getVersion()).isNotNull();
      assertThat(conceptRel.getFrom()).isNotNull();
      assertThat(conceptRel.getFrom().getCode()).isNotNull();
      assertThat(conceptRel.getFrom().getTerminology()).contains("SNOMEDCT");
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
      assertThat(conceptRel.getTerminology()).contains("SNOMEDCT");
      assertThat(conceptRel.getVersion()).isNotNull();
      assertThat(conceptRel.getFrom()).isNotNull();
      assertThat(conceptRel.getFrom().getCode()).isNotNull();
      assertThat(conceptRel.getFrom().getTerminology()).contains("SNOMEDCT");
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
      assertThat(conceptRel.getTerminology()).contains("SNOMEDCT");
      assertThat(conceptRel.getVersion()).isNotNull();
      assertThat(conceptRel.getFrom()).isNotNull();
      assertThat(conceptRel.getFrom().getCode()).isNotNull();
      assertThat(conceptRel.getFrom().getTerminology()).contains("SNOMEDCT");
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
      assertThat(conceptRel.getTerminology()).contains("SNOMEDCT");
      assertThat(conceptRel.getVersion()).isNotNull();
      assertThat(conceptRel.getFrom()).isNotNull();
      assertThat(conceptRel.getFrom().getCode()).isNotNull();
      assertThat(conceptRel.getFrom().getTerminology()).contains("SNOMEDCT");
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

    final String id = "2a545e12-04eb-48ee-b988-c17346b4e05f";
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
      // NUNO assertNotNull(mapsetRef.getActive());
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

}
