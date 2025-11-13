/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.TerminologyUtility;
import com.wci.termhub.util.ThreadLocalMapper;

/**
 * Loads all R5 files via FHIR endpoints and verifies basic queries. Collects
 * failures and logs them at the end of each test run.
 */
@TestMethodOrder(OrderAnnotation.class)
public class ThirdPartyFhirR5FilesIntegrationTest
    extends AbstractThirdPartyFhirFilesIntegrationTest {

  /** The logger. */
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ThirdPartyFhirR5FilesIntegrationTest.class);

  /** The port. */
  @LocalServerPort
  private int port;

  /** The rest template. */
  @Autowired
  private TestRestTemplate restTemplate;

  /** Repository service for deeper assertions. */
  @Autowired
  private EntityRepositoryService searchService;

  /** Localhost prefix. */
  private static final String LOCALHOST = "http://localhost:";

  /** Track loaded resources by key (title/publisher/version). */
  private final Set<String> loadedResources = new HashSet<>();

  private static final String BASE_FILE_PATH = "src/test/resources/data/integration/r5/";

  /**
   * Test fhir R 5 valueset.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(1)
  public void testFhirR5ValueSet() throws Exception {
    final String baseUrl = LOCALHOST + port + "/fhir/r5";
    final List<Path> vsFiles = getAllFiles(BASE_FILE_PATH + "/value-sets");
    final List<String> failures = new ArrayList<>();
    for (final Path file : vsFiles) {
      try {
        LOGGER.info("R5 Upload ValueSet: {}", file.getFileName());
        final String rt = getResourceType(file);
        if (!"ValueSet".equals(rt)) {
          LOGGER.info("Skipping non-ValueSet resourceType={} for file={}", rt, file.getFileName());
          continue;
        }
        LOGGER.info("Loading file: {}", file.toString());
        final String json = Files.readString(file, StandardCharsets.UTF_8);
        final JsonNode node = ThreadLocalMapper.get().readTree(json);
        final String resourceKey = getResourceKey(node, "ValueSet");
        if (resourceKey != null && loadedResources.contains(resourceKey)) {
          LOGGER.info("Skipping already loaded ValueSet: {} (key: {})", file.getFileName(),
              resourceKey);
          continue;
        }
        postResource(baseUrl + "/ValueSet", json);
        if (resourceKey != null) {
          loadedResources.add(resourceKey);
        }
        final String url = node.path("url").asText();
        assertTrue(url != null && !url.isEmpty());
        final ResponseEntity<String> searchResp = restTemplate
            .exchange(baseUrl + "/ValueSet?url=" + encode(url), HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.OK, searchResp.getStatusCode());
        final ResponseEntity<String> expandResp =
            restTemplate.exchange(baseUrl + "/ValueSet/$expand?url=" + encode(url) + "&count=1",
                HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.OK, expandResp.getStatusCode());
      } catch (final Exception e) {
        failures.add(file.toString() + ": " + e.getMessage());
      }
    }
    if (failures.size() > 1) {
      LOGGER.error("Test failures ({}):", failures.size());
      for (final String failure : failures) {
        LOGGER.error("  {}", failure);
      }
    }
    assertTrue(failures.isEmpty(), "Test failures: " + String.join("; ", failures));
  }

  /**
   * Test fhir R 5 codesystem.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(2)
  public void testFhirR5CodeSystem() throws Exception {
    final String baseUrl = LOCALHOST + port + "/fhir/r5";
    final List<Path> csFiles = getAllFiles(BASE_FILE_PATH + "/code-systems");
    final List<String> failures = new ArrayList<>();
    for (final Path file : csFiles) {
      try {
        LOGGER.info("R5 Upload CodeSystem: {}", file.getFileName());
        final String rt = getResourceType(file);
        if (!"CodeSystem".equals(rt)) {
          LOGGER.info("Skipping non-CodeSystem resourceType={} for file={}", rt,
              file.getFileName());
          continue;
        }
        LOGGER.info("Loading file: {}", file.toString());
        final String json = Files.readString(file, StandardCharsets.UTF_8);
        final JsonNode node = ThreadLocalMapper.get().readTree(json);
        final String resourceKey = getResourceKey(node, "CodeSystem");
        if (resourceKey != null && loadedResources.contains(resourceKey)) {
          LOGGER.info("Skipping already loaded CodeSystem: {} (key: {})", file.getFileName(),
              resourceKey);
          continue;
        }
        postResource(baseUrl + "/CodeSystem", json);
        if (resourceKey != null) {
          loadedResources.add(resourceKey);
        }
        final String url = node.path("url").asText();
        final String title = node.path("title").asText();
        final String publisher = node.path("publisher").asText();
        final String versionStr = node.path("version").asText();
        assertTrue(url != null && !url.isEmpty());

        final ResponseEntity<String> searchResp = restTemplate.exchange(
            baseUrl + "/CodeSystem?url=" + encode(url), HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.OK, searchResp.getStatusCode());

        final String termQuery =
            TerminologyUtility.getTerminologyAbbrQuery(title, publisher, versionStr);
        final ResultList<Terminology> tlist = searchService
            .find(new SearchParameters(termQuery, 0, 1, null, null), Terminology.class);
        assertTrue(tlist.getTotal() >= 1);
        final Terminology t = tlist.getItems().get(0);
        assertEquals(title, t.getAbbreviation());
        assertEquals(publisher, t.getPublisher());
        assertEquals(versionStr, t.getVersion());

        final String code = firstCodeFromCodeSystem(node);
        if (code != null) {
          final String conceptQuery =
              StringUtility.composeQuery("AND", "code:" + StringUtility.escapeQuery(code),
                  TerminologyUtility.getTerminologyQuery(title, publisher, versionStr));
          final ResultList<Concept> clist = searchService
              .find(new SearchParameters(conceptQuery, 0, 1, null, null), Concept.class);
          assertTrue(clist.getTotal() >= 1);
          assertEquals(code, clist.getItems().get(0).getCode());
        }
      } catch (final Exception e) {
        failures.add(file.toString() + ": " + e.getMessage());
      }
    }
    if (failures.size() > 1) {
      LOGGER.error("Test failures ({}):", failures.size());
      for (final String failure : failures) {
        LOGGER.error("  {}", failure);
      }
    }
    assertTrue(failures.isEmpty(), "Test failures: " + String.join("; ", failures));
  }

  /**
   * Test fhir R 5 conceptmap.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(3)
  public void testFhirR5ConceptMap() throws Exception {
    final String baseUrl = LOCALHOST + port + "/fhir/r5";
    final List<Path> cmFiles = getAllFiles(BASE_FILE_PATH + "/concept-maps");
    final List<String> failures = new ArrayList<>();
    for (final Path file : cmFiles) {
      try {
        LOGGER.info("R5 Upload ConceptMap: {}", file.getFileName());
        final String rt = getResourceType(file);
        LOGGER.info("Loading file: {}", file.toString());
        final String json = Files.readString(file, StandardCharsets.UTF_8);

        if ("Bundle".equals(rt)) {
          // Handle Bundle files via bulk load endpoint
          LOGGER.info("Loading Bundle file: {}", file.getFileName());
          postBundle(LOCALHOST + port + "/fhir/Bundle/$load", json);
          // Bundle load processes all resources inside, so we don't need
          // individual verification
          continue;
        }

        if (!"ConceptMap".equals(rt)) {
          LOGGER.info("Skipping non-ConceptMap resourceType={} for file={}", rt,
              file.getFileName());
          continue;
        }
        final JsonNode node = ThreadLocalMapper.get().readTree(json);
        final String resourceKey = getResourceKey(node, "ConceptMap");
        if (resourceKey != null && loadedResources.contains(resourceKey)) {
          LOGGER.info("Skipping already loaded ConceptMap: {} (key: {})", file.getFileName(),
              resourceKey);
          continue;
        }
        postResource(baseUrl + "/ConceptMap", json);
        if (resourceKey != null) {
          loadedResources.add(resourceKey);
        }
        final String url = node.path("url").asText();
        assertTrue(url != null && !url.isEmpty());
        final ResponseEntity<String> searchResp = restTemplate.exchange(
            baseUrl + "/ConceptMap?url=" + encode(url), HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.OK, searchResp.getStatusCode());
        final TranslateParams params = firstTranslateParams(node);
        if (params != null) {
          final String translateUrl = baseUrl + "/ConceptMap/$translate?url=" + encode(url)
              + "&system=" + encode(params.getSystem()) + "&code=" + encode(params.getCode());
          final ResponseEntity<String> translateResp =
              restTemplate.exchange(translateUrl, HttpMethod.GET, null, String.class);
          assertEquals(HttpStatus.OK, translateResp.getStatusCode());
        }
      } catch (final Exception e) {
        failures.add(file.toString() + ": " + e.getMessage());
      }
    }
    if (failures.size() > 1) {
      LOGGER.error("Test failures ({}):", failures.size());
      for (final String failure : failures) {
        LOGGER.error("  {}", failure);
      }
    }
    assertTrue(failures.isEmpty(), "Test failures: " + String.join("; ", failures));
  }

}
