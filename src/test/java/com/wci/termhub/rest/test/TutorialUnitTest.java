package com.wci.termhub.rest.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import com.wci.termhub.test.AbstractTerminologyServerTest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.wci.termhub.util.TestUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriUtils;

@AutoConfigureMockMvc
public class TutorialUnitTest extends AbstractTerminologyServerTest {

    @Autowired private TestRestTemplate restTemplate;

    private static Set<String> tutorialResources = new HashSet<>();

    @BeforeAll
    public static void beforeAll() {
        tutorialResources = TestUtils.getUrlsFromMarkdown("doc/TUTORIAL1.md", "Testing the Terminology API");
    }

    // Test for: curl -s "http://localhost:8080/terminology" | jq
    @Test
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

    // Test for: curl -s "http://localhost:8080/terminology/$id/metadata" | jq
    @Test
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

    // Test for: curl -s "http://localhost:8080/concept/SNOMEDCT_US/107907001" | jq
    @Test
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

    // Test for: curl -s "http://localhost:8080/concept/SNOMEDCT_US/107907001/relationships" | jq
    @Test
    void testGetSnomedConceptRelationships() {
        String resource = "/concept/SNOMEDCT_US/107907001/relationships";
        ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
        tutorialResources.remove(resource);
    }

    // Test for: curl -s "http://localhost:8080/concept/SNOMEDCT_US/107907001/trees" | jq
    @Test
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
    // "http://localhost:8080/concept?terminology=SNOMEDCT_US&query=diabetes&include=minimal" | jq
    @Test
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
    // "http://localhost:8080/concept?terminology=SNOMEDCT_US&query=73211009&include=minimal" | jq
    @Test
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
    // | jq
    @Test
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
    // | jq
    @Test
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

    // Test for: curl -s "http://localhost:8080/mapset" | jq
    @Test
    void testFindMapsets() {
        String resource = "/mapset";
        ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
        tutorialResources.remove(resource);
    }

    // Test for: curl -s "http://localhost:8080/mapping" | jq
    @Test
    void testFindMappingsAcrossAllMapsets() {
        String resource = "/mapping";
        ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
        tutorialResources.remove(resource);
    }

    // Test for: curl -s "http://localhost:8080/mapset/SNOMEDCT_US-ICD10CM/mapping" | jq
    @Test
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
    // | jq
    @Test
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
    // | jq
    @Test
    void testFindMapsetMappingsByFromCode() {
        String url = "/mapset/SNOMEDCT_US-ICD10CM/mapping?query=from.code:300862005";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
        tutorialResources.remove(url);
    }

    // Test for: curl -s "http://localhost:8080/subset" | jq
    @Test
    void testFindSubsets() {
        String resource = "/subset";
        ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
        tutorialResources.remove(resource);
    }

    // Test for: curl -s "http://localhost:8080/member" | jq
    @Test
    void testFindMembersAcrossAllSubsets() {
        String resource = "/member";
        ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
        tutorialResources.remove(resource);
    }

    // Test for: curl -s "http://localhost:8080/subset/SNOMEDCT_US-EXTENSION/member" | jq
    @Test
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
    // | jq
    @Test
    void testFindSubsetMembersByQuery() {
        String url = "/subset/SNOMEDCT_US-EXTENSION/member?query=diabetes";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
        tutorialResources.remove(url);
    }

    // Test for: curl -s "http://localhost:8080/subset/SNOMEDCT_US-EXTENSION/member?query=name:diabetes"
    // | jq
    @Test
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
    // | jq
    @Test
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

    @AfterAll
    public static void checkAllTutorialResourcesTested() {
        assertThat(tutorialResources)
                .as(
                        "The list of tutorial resources was expected to be empty, but contained: %s",
                        tutorialResources)
                .isEmpty();
    }
}

