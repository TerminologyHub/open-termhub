/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r4.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jayway.jsonpath.JsonPath;
import com.wci.termhub.util.test.TestUtils;

/**
 * Unit tests for tutorial 1.
 */
@AutoConfigureMockMvc
class TutorialUnitTest extends AbstractFhirR4ServerTest {

  /** The Constant VERSION_PARAMETER. */
  private static final String VERSION_PARAMETER =
      "&version=http://snomed.info/sct/731000124108/version/20240301";

  /** The Constant VALUE_SET_VERSION_PARAMETER. */
  private static final String VALUE_SET_VERSION_PARAMETER =
      VERSION_PARAMETER.replace("&version", "&valueSetVersion");

  /** The rest template. */
  @Autowired
  private TestRestTemplate restTemplate;

  /** The tutorial resources. */
  private static Set<String> tutorialResources = new HashSet<>();

  /**
   * Before all.
   */
  @BeforeAll
  public static void beforeAll() {
    tutorialResources =
        TestUtils.getUrlsFromMarkdown("doc/TUTORIAL1.md", "Testing the FHIR R4 API");
  }

  /**
   * Test find code systems.
   */
  // Test for: curl -s 'http://localhost:8080/fhir/r4/CodeSystem' | jq
  @Test
  void testFindCodeSystems() {
    final String url = "/fhir/r4/CodeSystem";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("Bundle");
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  /**
   * Test code system lookup.
   */
  @SuppressWarnings("unchecked")
  // Test for: curl -s
  // 'http://localhost:8080/fhir/r4/CodeSystem/$lookup?system=http://snomed.info/sct&code=73211009'
  // | jq
  @Test
  void testCodeSystemLookup() {
    final String url = "/fhir/r4/CodeSystem/$lookup?system=http://snomed.info/sct&code=73211009";
    final ResponseEntity<String> response =
        restTemplate.getForEntity(url + VERSION_PARAMETER, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("Parameters");
    assertThat(
        ((List<String>) JsonPath.read(body, "$.parameter[?(@.name=='name')].valueString")).get(0))
            .isEqualTo("SNOMEDCT_US");
    assertThat(((List<?>) JsonPath.read(body, "$.parameter[?(@.name=='version')].valueString")))
        .isNotEmpty();
    assertThat(((List<String>) JsonPath.read(body, "$.parameter[?(@.name=='display')].valueString"))
        .get(0)).isEqualTo("Diabetes mellitus");
    tutorialResources.remove(url);
  }

  /**
   * Test find concept maps.
   */
  // Test for: curl -s 'http://localhost:8080/fhir/r4/ConceptMap' | jq
  @Test
  void testFindConceptMaps() {
    final String url = "/fhir/r4/ConceptMap";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("Bundle");
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  /**
   * Test concept map translate.
   */
  @SuppressWarnings("unchecked")
  // Test for: curl -s
  // 'http://localhost:8080/fhir/r4/ConceptMap/$translate?url=http://snomed.info/sct?fhir_cm=6011000124106
  // &system=http://snomed.info/sct&code=300862005'
  // | jq
  @Test
  void testConceptMapTranslate() {
    final String url =
        "/fhir/r4/ConceptMap/$translate?url=http://snomed.info/sct?fhir_cm=6011000124106&"
            + "system=http://snomed.info/sct&code=300862005";
    final ResponseEntity<String> response =
        restTemplate.getForEntity(url + VERSION_PARAMETER, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("Parameters");
    assertThat(
        ((List<Boolean>) JsonPath.read(body, "$.parameter[?(@.name=='result')].valueBoolean"))
            .get(0)).isTrue();
    assertThat(((List<Map<String, ?>>) JsonPath.read(body, "$.parameter[?(@.name=='match')].part")))
        .hasSizeGreaterThan(0);
    tutorialResources.remove(url);
  }

  /**
   * Test find all value sets and extract id.
   */
  // Test for: curl -s 'http://localhost:8080/fhir/r4/ValueSet' | jq
  @Test
  void testFindAllValueSetsAndExtractId() {
    final String url = "/fhir/r4/ValueSet";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("Bundle");
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);

    // Extract the ID for a subsequent test
    final String valueSetId = JsonPath.read(body, "$.entry[0].resource.id");
    assertThat(valueSetId).isNotNull();
    tutorialResources.remove(url);
  }

  /**
   * Test find implicit value set for code system.
   */
  // Test for: curl -s 'http://localhost:8080/fhir/r4/ValueSet?url=http://snomed.info/sct?fhir_vs' |
  // jq
  @Test
  void testFindImplicitValueSetForCodeSystem() {
    final String url = "/fhir/r4/ValueSet?url=http://snomed.info/sct?fhir_vs";
    final ResponseEntity<String> response =
        restTemplate.getForEntity(url + VERSION_PARAMETER, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("Bundle");
    assertThat((Integer) JsonPath.read(body, "$.total")).isEqualTo(1);
    assertThat((String) JsonPath.read(body, "$.entry[0].resource.url"))
        .isEqualTo("http://snomed.info/sct?fhir_vs");
    tutorialResources.remove(url);
  }

  /**
   * Test find explicit value set.
   */
  // Test for: curl -s
  // 'http://localhost:8080/fhir/r4/ValueSet?url=http://snomed.info/sct?fhir_vs=731000124108' | jq
  @Test
  void testFindExplicitValueSet() {
    final String url = "/fhir/r4/ValueSet?url=http://snomed.info/sct?fhir_vs=731000124108";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("Bundle");
    assertThat((Integer) JsonPath.read(body, "$.total")).isEqualTo(1);
    assertThat((String) JsonPath.read(body, "$.entry[0].resource.url"))
        .isEqualTo("http://snomed.info/sct?fhir_vs=731000124108");
    tutorialResources.remove(url);
  }

  /**
   * Test get value set by id.
   */
  // Test for: curl -s "http://localhost:8080/fhir/r4/ValueSet/$id" | jq
  @Test
  void testGetValueSetById() {
    String url = "/fhir/r4/ValueSet";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    String body = response.getBody();
    // Get a value set by id (pick the first one)
    final String valueSetId = JsonPath.read(body, "$.entry[0].resource.id");

    url = "/fhir/r4/ValueSet/$id";
    response = restTemplate.getForEntity(url.replace("$id", valueSetId), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("ValueSet");
    assertThat((String) JsonPath.read(body, "$.id")).isEqualTo(valueSetId);
    assertThat((String) JsonPath.read(body, "$.url")).isNotNull();
    assertThat((String) JsonPath.read(body, "$.publisher")).isNotNull();
    tutorialResources.remove(url);
  }

  /**
   * Test expand implicit value set.
   */
  // Test for: curl -s
  // 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs' | jq
  @Test
  void testExpandImplicitValueSet() {
    final String url = "/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs";
    final ResponseEntity<String> response =
        restTemplate.getForEntity(url + VALUE_SET_VERSION_PARAMETER, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("ValueSet");
    assertThat((String) JsonPath.read(body, "$.url")).isEqualTo("http://snomed.info/sct?fhir_vs");
    assertThat((Integer) JsonPath.read(body, "$.expansion.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  /**
   * Test expand explicit value set.
   */
  // Test for: curl -s
  // 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=731000124108'
  // | jq
  @Test
  void testExpandExplicitValueSet() {
    final String url = "/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=731000124108";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("ValueSet");
    assertThat((String) JsonPath.read(body, "$.url"))
        .isEqualTo("http://snomed.info/sct?fhir_vs=731000124108");
    assertThat((Integer) JsonPath.read(body, "$.expansion.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  /**
   * Test expand implicit value set with filter.
   */
  // Test for: curl -s
  // 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs&filter=diabetes'
  // | jq
  @Test
  void testExpandImplicitValueSetWithFilter() {
    final String url =
        "/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs&filter=diabetes";
    final ResponseEntity<String> response =
        restTemplate.getForEntity(url + VALUE_SET_VERSION_PARAMETER, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.expansion.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  /**
   * Test expand explicit value set with filter.
   */
  // Test for: curl -s
  // 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=731000124108&filter=diabetes'
  // | jq
  @Test
  void testExpandExplicitValueSetWithFilter() {
    final String url =
        "/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=731000124108&filter=diabetes";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.expansion.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  /**
   * Test expand value set with filter and ecl.
   */
  // Test for: curl -s
  // 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=ecl/%3C%3C128927009
  // &filter=gastrointestinal'
  // | jq
  @Test
  void testExpandValueSetWithFilterAndEcl() {
    final String url =
        "/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=ecl/<<128927009&filter=gastrointestinal";
    final ResponseEntity<String> response =
        restTemplate.getForEntity(url + VALUE_SET_VERSION_PARAMETER, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("ValueSet");
    assertThat((Integer) JsonPath.read(body, "$.expansion.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  /**
   * Check all tutorial resources tested.
   */
  @AfterAll
  public static void checkAllTutorialResourcesTested() {
    assertThat(tutorialResources)
        .as("The list of tutorial resources was expected to be empty, but contained: %s",
            tutorialResources)
        .isEmpty();
  }
}
