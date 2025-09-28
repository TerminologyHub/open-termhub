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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jayway.jsonpath.JsonPath;
import com.wci.termhub.test.AbstractTerminologyServerTest;
import com.wci.termhub.util.TestUtils;

/**
 * The Class TutorialUnitTest.
 */
@AutoConfigureMockMvc
public class TutorialUnitTest extends AbstractTerminologyServerTest {

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
  // @Test
  void testFindTerminologies() {
    String resource = "/terminology";
    ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isEqualTo(6);

    String id = JsonPath.read(body, "$.items[0].id");
    assertThat(id).isNotNull();
    tutorialResources.remove(resource);
  }

  /**
   * Test find terminology metadata.
   */
  // Test for: curl -s "http://localhost:8080/terminology/$id/metadata" | jq
  // @Test
  void testFindTerminologyMetadata() {
    // Make independent by fetching the ID first
    ResponseEntity<String> terminologyResponse =
        restTemplate.getForEntity("/terminology", String.class);
    assertThat(terminologyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    String terminologyBody = terminologyResponse.getBody();
    String terminologyId = JsonPath.read(terminologyBody, "$.items[0].id");

    String resource = "/terminology/" + terminologyId + "/metadata";
    ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
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
  // @Test
  void testGetSnomedConceptByCode() {
    String resource = "/concept/SNOMEDCT_US/107907001";
    ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
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
  // @Test
  void testGetSnomedConceptRelationships() {
    String resource = "/concept/SNOMEDCT_US/107907001/relationships";
    ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  /**
   * Test get snomed concept trees.
   */
  // Test for: curl -s "http://localhost:8080/concept/SNOMEDCT_US/107907001/trees" | jq
  // @Test
  void testGetSnomedConceptTrees() {
    String resource = "/concept/SNOMEDCT_US/107907001/trees";
    ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  // Test for: curl -s
  /**
   * Test search snomed by word query.
   */
  // "http://localhost:8080/concept?terminology=SNOMEDCT_US&query=diabetes&include=minimal" | jq
  // @Test
  void testSearchSnomedByWordQuery() {
    String url = "/concept?terminology=SNOMEDCT_US&query=diabetes&include=minimal";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  // Test for: curl -s
  /**
   * Test search snomed by code query.
   */
  // "http://localhost:8080/concept?terminology=SNOMEDCT_US&query=73211009&include=minimal" | jq
  // @Test
  void testSearchSnomedByCodeQuery() {
    String url = "/concept?terminology=SNOMEDCT_US&query=73211009&include=minimal";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
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
  // @Test
  void testSearchSnomedByEclExpression() {
    String url = "/concept?terminology=SNOMEDCT_US&expression=<<128927009&include=minimal";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  // Test for: curl -s
  // "http://localhost:8080/concept?terminology=SNOMEDCT_US&query=gastrointestinal&expression=%3C%3C128927009&include=minimal"
  /**
   * Test search snomed by query and ecl.
   */
  // | jq
  // @Test
  void testSearchSnomedByQueryAndEcl() {
    String url =
        "/concept?terminology=SNOMEDCT_US&query=gastrointestinal&expression=<<128927009&include=minimal";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  /**
   * Test find mapsets.
   */
  // Test for: curl -s "http://localhost:8080/mapset" | jq
  // @Test
  void testFindMapsets() {
    String resource = "/mapset";
    ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  /**
   * Test find mappings across all mapsets.
   */
  // Test for: curl -s "http://localhost:8080/mapping" | jq
  // @Test
  void testFindMappingsAcrossAllMapsets() {
    String resource = "/mapping";
    ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  /**
   * Test find mapset mappings.
   */
  // Test for: curl -s "http://localhost:8080/mapset/SNOMEDCT_US-ICD10CM/mapping" | jq
  // @Test
  void testFindMapsetMappings() {
    String resource = "/mapset/SNOMEDCT_US-ICD10CM/mapping";
    ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  // Test for: curl -s "http://localhost:8080/mapset/SNOMEDCT_US-ICD10CM/mapping?query=300862005"
  /**
   * Test find mapset mappings by code.
   */
  // | jq
  // @Test
  void testFindMapsetMappingsByCode() {
    String url = "/mapset/SNOMEDCT_US-ICD10CM/mapping?query=300862005";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  // Test for: curl -s "http://localhost:8080/mapset/SNOMEDCT_US-ICD10CM/mapping?query=300862005"
  /**
   * Test find mapset mappings by from code.
   */
  // | jq
  // @Test
  void testFindMapsetMappingsByFromCode() {
    String url = "/mapset/SNOMEDCT_US-ICD10CM/mapping?query=from.code:300862005";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(url);
  }

  /**
   * Test find subsets.
   */
  // Test for: curl -s "http://localhost:8080/subset" | jq
  // @Test
  void testFindSubsets() {
    String resource = "/subset";
    ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  /**
   * Test find members across all subsets.
   */
  // Test for: curl -s "http://localhost:8080/member" | jq
  // @Test
  void testFindMembersAcrossAllSubsets() {
    String resource = "/member";
    ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  /**
   * Test find subset members.
   */
  // Test for: curl -s "http://localhost:8080/subset/SNOMEDCT_US-EXTENSION/member" | jq
  // @Test
  void testFindSubsetMembers() {
    String resource = "/subset/SNOMEDCT_US-EXTENSION/member";
    ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
    tutorialResources.remove(resource);
  }

  // Test for: curl -s "http://localhost:8080/subset/SNOMEDCT_US-EXTENSION/member?query=diabetes"
  /**
   * Test find subset members by query.
   */
  // | jq
  // @Test
  void testFindSubsetMembersByQuery() {
    String url = "/subset/SNOMEDCT_US-EXTENSION/member?query=diabetes";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
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
  // @Test
  void testFindSubsetMembersByNameQuery() {
    String url = "/subset/SNOMEDCT_US-EXTENSION/member?query=name:diabetes";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
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
  // @Test
  void testFindConceptsInSubsetByEcl() {
    String url =
        "/concept?terminology=SNOMEDCT_US&query=diabetes&expression=^731000124108&include=minimal";
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = response.getBody();
    assertThat(body).isNotNull();
    assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
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
