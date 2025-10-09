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

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jayway.jsonpath.JsonPath;
import com.wci.termhub.util.test.TestUtils;

/**
 * The Class TutorialUnitTest.
 */
@AutoConfigureMockMvc
public class TutorialUnitTest extends AbstractTerminologyServerTest {

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(TutorialUnitTest.class);

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
        TestUtils.getUrlsFromMarkdown("doc/TUTORIAL1.md", "Testing the Terminology API");
  }

  /**
   * Test find terminologies.
   */
  // Test for: curl -s "http://localhost:8080/terminology" | jq
  @Test
  void testFindTerminologies() {
    final String resource = "/terminology";
    final ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isEqualTo(6);

    final String id = JsonPath.read(body, "$.items[0].id");
    assertThat(id).isNotNull();
    tutorialResources.remove(resource);
  }

  /**
   * Test find terminology metadata.
   */
  // Test for: curl -s "http://localhost:8080/terminology/$id/metadata" | jq
  @Test
  void testFindTerminologyMetadata() {
    // Make independent by fetching the ID first
    final ResponseEntity<String> terminologyResponse =
        restTemplate.getForEntity("/terminology", String.class);
    assertThat(terminologyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String terminologyBody = terminologyResponse.getBody();
    final String terminologyId = JsonPath.read(terminologyBody, "$.items[0].id");

    final String resource = "/terminology/" + terminologyId + "/metadata";
    final ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((String) JsonPath.read(body, "$[0].terminology")).isEqualTo("SNOMEDCT_US");
    assertThat((String) JsonPath.read(body, "$[0].publisher")).isEqualTo("SANDBOX");

    // Remove the placeholder version of the resource from the set
    tutorialResources.remove("/terminology/$id/metadata");
  }

  /**
   * Test get snomed concept by code.
   */
  // Test for: curl -s "http://localhost:8080/concept/SNOMEDCT_US/107907001" | jq
  @Test
  void testGetSnomedConceptByCode() {
    final String resource = "/concept/SNOMEDCT_US/107907001";
    final ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((String) JsonPath.read(body, "$.terminology")).isEqualTo("SNOMEDCT_US");
    assertThat((String) JsonPath.read(body, "$.version")).isEqualTo("20240301");
    assertThat((String) JsonPath.read(body, "$.code")).isEqualTo("107907001");
    tutorialResources.remove(resource);
  }

  /**
   * Test get snomed concept relationships.
   */
  // Test for: curl -s "http://localhost:8080/concept/SNOMEDCT_US/107907001/relationships" | jq
  @Test
  void testGetSnomedConceptRelationships() {
    final String resource = "/concept/SNOMEDCT_US/107907001/relationships";
    final ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  /**
   * Test get snomed concept trees.
   */
  // Test for: curl -s "http://localhost:8080/concept/SNOMEDCT_US/107907001/trees" | jq
  @Test
  void testGetSnomedConceptTrees() {
    final String resource = "/concept/SNOMEDCT_US/107907001/trees";
    final ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  // Test for: curl -s
  /**
   * Test search snomed by word query.
   */
  // "http://localhost:8080/concept?terminology=SNOMEDCT_US&query=diabetes&include=minimal" | jq
  @Test
  void testSearchSnomedByWordQuery() {
    final String url = "/concept?terminology=SNOMEDCT_US&query=diabetes&include=minimal";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  // Test for: curl -s
  /**
   * Test search snomed by code query.
   */
  // "http://localhost:8080/concept?terminology=SNOMEDCT_US&query=73211009&include=minimal" | jq
  @Test
  void testSearchSnomedByCodeQuery() {
    final String url = "/concept?terminology=SNOMEDCT_US&query=73211009&include=minimal";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isEqualTo(1);
    tutorialResources.remove(url);
  }

  // Test for: curl -s
  // "http://localhost:8080/concept?terminology=SNOMEDCT_US&expression=%3C%3C128927009&include=minimal"
  /**
   * Test search snomed by ecl expression.
   */
  // | jq
  @Test
  void testSearchSnomedByEclExpression() {
    final String url = "/concept?terminology=SNOMEDCT_US&expression=<<128927009&include=minimal";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  // Test for: curl -s
  // "http://localhost:8080/concept?terminology=SNOMEDCT_US&query=gastrointestinal
  // &expression=%3C%3C128927009&include=minimal"
  /**
   * Test search snomed by query and ecl.
   */
  // | jq
  @Test
  void testSearchSnomedByQueryAndEcl() {
    final String url =
        "/concept?terminology=SNOMEDCT_US&query=gastrointestinal&expression=<<128927009&include=minimal";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  /**
   * Test find mapsets.
   */
  // Test for: curl -s "http://localhost:8080/mapset" | jq
  @Test
  void testFindMapsets() {
    final String resource = "/mapset";
    final ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  /**
   * Test find mappings across all mapsets.
   */
  // Test for: curl -s "http://localhost:8080/mapping" | jq
  @Test
  void testFindMappingsAcrossAllMapsets() {
    final String resource = "/mapping";
    final ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  /**
   * Test find mapset mappings.
   */
  // Test for: curl -s "http://localhost:8080/mapset/SNOMEDCT_US-ICD10CM/mapping" | jq
  @Test
  void testFindMapsetMappings() {
    final String resource = "/mapset/SNOMEDCT_US-ICD10CM/mapping";
    final ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  // Test for: curl -s
  // "http://localhost:8080/mapset/SNOMEDCT_US-ICD10CM/mapping?query=300862005"
  /**
   * Test find mapset mappings by code.
   */
  // | jq
  @Test
  void testFindMapsetMappingsByCode() {
    final String url = "/mapset/SNOMEDCT_US-ICD10CM/mapping?query=300862005";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  // Test for: curl -s
  // "http://localhost:8080/mapset/SNOMEDCT_US-ICD10CM/mapping?query=300862005"
  /**
   * Test find mapset mappings by from code.
   */
  // | jq
  @Test
  void testFindMapsetMappingsByFromCode() {
    final String url = "/mapset/SNOMEDCT_US-ICD10CM/mapping?query=from.code:300862005";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  /**
   * Test find subsets.
   */
  // Test for: curl -s "http://localhost:8080/subset" | jq
  @Test
  void testFindSubsets() {
    final String resource = "/subset";
    final ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  /**
   * Test find members across all subsets.
   */
  // Test for: curl -s "http://localhost:8080/member" | jq
  @Test
  void testFindMembersAcrossAllSubsets() {
    final String resource = "/member";
    final ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  /**
   * Test find subset members.
   */
  // Test for: curl -s "http://localhost:8080/subset/SNOMEDCT_US-EXTENSION/member" | jq
  @Test
  void testFindSubsetMembers() {
    final String resource = "/subset/SNOMEDCT_US-EXTENSION/member";
    final ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  // Test for: curl -s
  // "http://localhost:8080/subset/SNOMEDCT_US-EXTENSION/member?query=diabetes"
  /**
   * Test find subset members by query.
   */
  // | jq
  @Test
  void testFindSubsetMembersByQuery() {
    final String url = "/subset/SNOMEDCT_US-EXTENSION/member?query=diabetes";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  // Test for: curl -s
  // "http://localhost:8080/subset/SNOMEDCT_US-EXTENSION/member?query=name:diabetes"
  /**
   * Test find subset members by name query.
   */
  // | jq
  @Test
  void testFindSubsetMembersByNameQuery() {
    final String url = "/subset/SNOMEDCT_US-EXTENSION/member?query=name:diabetes";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  // Test for: curl -s
  // "http://localhost:8080/concept?terminology=SNOMEDCT_US&query=diabetes&expression=%5E731000124108&include=minimal"
  /**
   * Test find concepts in subset by ecl.
   */
  // | jq
  @Test
  void testFindConceptsInSubsetByEcl() {
    final String url =
        "/concept?terminology=SNOMEDCT_US&query=diabetes&expression=^731000124108&include=minimal";
    final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  // test for: curl -s -X POST \
  //  "http://localhost:8080/concept/bulk?terminology=SNOMEDCT_US&limit=1&active=true'
  // -H 'accept: application/json'  -H 'Content-Type: text/plain'  --data-binary $'heart\nprocedure"
  @Test
  void testFindConceptsBulkSearch() {
    final String url = "/concept/bulk?terminology=SNOMEDCT_US&limit=1&active=true";
    final org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
    headers.add("Accept", "application/json");
    headers.add("Content-Type", "text/plain");
    final org.springframework.http.HttpEntity<String> entity =
        new org.springframework.http.HttpEntity<>("heart\nprocedure", headers);

    final ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    final String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.length()")).isEqualTo(2);

    // Remove any tutorial entry that starts with the URL (handles appended headers/body)
    tutorialResources.removeIf(s -> s.startsWith(url));
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
