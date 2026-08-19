/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r5.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jayway.jsonpath.JsonPath;
import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.lucene.LuceneDataAccess;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * $translate default latest map version and match Coding (no LOINC map version on Coding).
 */
@SuppressWarnings("unchecked")
public class ConceptMapTranslateLatestR5UnitTest extends AbstractFhirR5ServerTest {

  /** Map URL for the dual-version LOINC-IEEE fixtures. */
  private static final String IEEE_MAP_URL = "http://loinc.org/cm/loinc-to-ieee-11073-10101";

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The rest template. */
  @Autowired
  private TestRestTemplate restTemplate;

  /** The port. */
  @LocalServerPort
  private int port;

  /** Translate without version pins. */
  private static final String TRANSLATE =
      "/fhir/r5/ConceptMap/$translate?system=http://loinc.org&sourceCode=11556-8";

  /**
   * Load two versions of the LOINC-IEEE ConceptMap on top of the shared R5 fixture index.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public void setupTranslateFixtures() throws Exception {
    // Shared index already has LOINC CodeSystem (http://loinc.org); only add dual-version maps.
    LuceneDataAccess.clearReaders();
    loadConceptMaps(searchService,
        List.of("ConceptMap-loinc-ieee-2.69.json", "ConceptMap-loinc-ieee-2.83.json"));
    // Prove $translate uses version strings, not latest=true.
    markIeeeMapsetLatest("2.69");
    FhirUtility.clearCaches();
  }

  /**
   * Marks latest only on LOINC-IEEE mapsets so other shared fixtures stay untouched.
   *
   * @param latestVersion the version to mark latest
   * @throws Exception the exception
   */
  private void markIeeeMapsetLatest(final String latestVersion) throws Exception {
    final List<Mapset> mapsets =
        searchService.find(new SearchParameters("*:*", 0, 50, null, null), Mapset.class).getItems();
    for (final Mapset mapset : mapsets) {
      if (!IEEE_MAP_URL.equals(mapset.getUri())) {
        continue;
      }
      mapset.setLatest(latestVersion.equals(mapset.getVersion()));
      searchService.update(Mapset.class, mapset.getId(), mapset);
    }
    LuceneDataAccess.clearReaders();
  }

  /**
   * No version params uses the latest map and fills Coding fields.
   */
  @Test
  public void testTranslateDefaultsToLatestMapVersion() {
    final ResponseEntity<String> response =
        restTemplate.getForEntity("http://localhost:" + port + TRANSLATE, String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    final String body = response.getBody();
    assertNotNull(body);
    final List<Map<String, ?>> matches =
        JsonPath.read(body, "$.parameter[?(@.name=='match')]");
    assertEquals(1, matches.size());
    final Map<String, ?> coding = ((List<Map<String, ?>>) JsonPath.read(body,
        "$.parameter[?(@.name=='match')].part[?(@.name=='concept')].valueCoding")).get(0);
    assertEquals(false, coding.containsKey("version"));
    assertEquals("160116", coding.get("code"));
    assertEquals("urn:iso:std:iso:11073:10101", coding.get("system"));
    assertEquals("MDC_CONC_PO2_GEN", coding.get("display"));
    assertEquals("equivalent",
        ((List<String>) JsonPath.read(body,
            "$.parameter[?(@.name=='match')].part[?(@.name=='relationship')].valueCode"))
            .get(0));
    assertEquals(IEEE_MAP_URL,
        ((List<String>) JsonPath.read(body,
            "$.parameter[?(@.name=='match')].part[?(@.name=='source')].valueUri"))
            .get(0));
  }

  /**
   * conceptMapVersion selects the older map.
   */
  @Test
  public void testTranslateConceptMapVersionSelectsOlder() {
    final ResponseEntity<String> response = restTemplate.getForEntity(
        "http://localhost:" + port + TRANSLATE + "&conceptMapVersion=2.69", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    final String body = response.getBody();
    assertNotNull(body);
    final List<Map<String, ?>> matches =
        JsonPath.read(body, "$.parameter[?(@.name=='match')]");
    assertEquals(1, matches.size());
    assertEquals("MDC_CONC_PO2_GEN_269",
        ((List<String>) JsonPath.read(body,
            "$.parameter[?(@.name=='match')].part[?(@.name=='concept')].valueCoding.display"))
            .get(0));
  }
}
