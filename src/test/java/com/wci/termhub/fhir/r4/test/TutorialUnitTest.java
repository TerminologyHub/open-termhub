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

import com.jayway.jsonpath.JsonPath;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wci.termhub.util.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@AutoConfigureMockMvc
class TutorialUnitTest extends AbstractFhirR4ServerTest {

    private static final String VERSION_PARAMETER = "&version=http://snomed.info/sct/731000124108/version/20240301";
    private static final String VALUE_SET_VERSION_PARAMETER = VERSION_PARAMETER.replace("&version", "&valueSetVersion");
    @Autowired
    private TestRestTemplate restTemplate;

    private static Set<String> tutorialResources = new HashSet<>();

    @BeforeAll
    public static void beforeAll() {
        tutorialResources = TestUtils.getUrlsFromMarkdown("doc/TUTORIAL1.md", "Testing the FHIR R4 API");
    }


    // Test for: curl -s 'http://localhost:8080/fhir/r4/CodeSystem' | jq
    @Test
    void testFindCodeSystems() {
        String url = "/fhir/r4/CodeSystem";
        ResponseEntity<String> response =
                restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("Bundle");
        assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
        tutorialResources.remove(url);
    }

    // Test for: curl -s 'http://localhost:8080/fhir/r4/CodeSystem/$lookup?system=http://snomed.info/sct&code=73211009' | jq
    @Test
    void testCodeSystemLookup() {
        String url = "/fhir/r4/CodeSystem/$lookup?system=http://snomed.info/sct&code=73211009";
        ResponseEntity<String> response = restTemplate.getForEntity(url + VERSION_PARAMETER, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("Parameters");
        assertThat(((List<String>) JsonPath.read(body, "$.parameter[?(@.name=='name')].valueString")).get(0)).isEqualTo("SNOMEDCT_US");
        assertThat(((List<?>) JsonPath.read(body, "$.parameter[?(@.name=='version')].valueString"))).isNotEmpty();
        assertThat(((List<String>) JsonPath.read(body, "$.parameter[?(@.name=='display')].valueString")).get(0)).isEqualTo("Diabetes mellitus");
        tutorialResources.remove(url);
    }

    // Test for: curl -s 'http://localhost:8080/fhir/r4/ConceptMap' | jq
    @Test
    void testFindConceptMaps() {
        String url = "/fhir/r4/ConceptMap";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("Bundle");
        assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);
        tutorialResources.remove(url);
    }

    // Test for: curl -s 'http://localhost:8080/fhir/r4/ConceptMap/$translate?url=http://snomed.info/sct?fhir_cm=6011000124106&system=http://snomed.info/sct&code=300862005' | jq
    @Test
    void testConceptMapTranslate() {
        String url =
                "/fhir/r4/ConceptMap/$translate?url=http://snomed.info/sct?fhir_cm=6011000124106&system=http://snomed.info/sct&code=300862005";
        ResponseEntity<String> response = restTemplate.getForEntity(url + VERSION_PARAMETER, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("Parameters");
        assertThat(((List<Boolean>) JsonPath.read(body, "$.parameter[?(@.name=='result')].valueBoolean")).get(0)).isTrue();
        assertThat(((List<Map<String, ?>>) JsonPath.read(body, "$.parameter[?(@.name=='match')].part"))).hasSizeGreaterThan(0);
        tutorialResources.remove(url);
    }

    // Test for: curl -s 'http://localhost:8080/fhir/r4/ValueSet' | jq
    @Test
    void testFindAllValueSetsAndExtractId() {
        String url = "/fhir/r4/ValueSet";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("Bundle");
        assertThat((Integer) JsonPath.read(body, "$.total")).isGreaterThan(0);

        // Extract the ID for a subsequent test
        String valueSetId = JsonPath.read(body, "$.entry[0].resource.id");
        assertThat(valueSetId).isNotNull();
        tutorialResources.remove(url);
    }

    // Test for: curl -s 'http://localhost:8080/fhir/r4/ValueSet?url=http://snomed.info/sct?fhir_vs' | jq
    @Test
    void testFindImplicitValueSetForCodeSystem() {
        String url = "/fhir/r4/ValueSet?url=http://snomed.info/sct?fhir_vs";
        ResponseEntity<String> response =
                restTemplate.getForEntity(url + VERSION_PARAMETER, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("Bundle");
        assertThat((Integer) JsonPath.read(body, "$.total")).isEqualTo(1);
        assertThat((String) JsonPath.read(body, "$.entry[0].resource.url")).isEqualTo("http://snomed.info/sct?fhir_vs");
        tutorialResources.remove(url);
    }

    // Test for: curl -s 'http://localhost:8080/fhir/r4/ValueSet?url=http://snomed.info/sct?fhir_vs=731000124108' | jq
    @Test
    void testFindExplicitValueSet() {
        String url = "/fhir/r4/ValueSet?url=http://snomed.info/sct?fhir_vs=731000124108";
        ResponseEntity<String> response =
                restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("Bundle");
        assertThat((Integer) JsonPath.read(body, "$.total")).isEqualTo(1);
        assertThat((String) JsonPath.read(body, "$.entry[0].resource.url")).isEqualTo("http://snomed.info/sct?fhir_vs=731000124108");
        tutorialResources.remove(url);
    }

    // Test for: curl -s "http://localhost:8080/fhir/r4/ValueSet/$id" | jq
    @Test
    void testGetValueSetById() {
        String url = "/fhir/r4/ValueSet";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String body = response.getBody();
        //Get a value set by id (pick the first one)
        String valueSetId = JsonPath.read(body, "$.entry[0].resource.id");

        url = "/fhir/r4/ValueSet/$id";
        response =
                restTemplate.getForEntity(url.replace("$id", valueSetId), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("ValueSet");
        assertThat((String) JsonPath.read(body, "$.id")).isEqualTo(valueSetId);
        assertThat((String) JsonPath.read(body, "$.url")).isNotNull();
        assertThat((String) JsonPath.read(body, "$.publisher")).isNotNull();
        tutorialResources.remove(url);
    }

    // Test for: curl -s 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs' | jq
    @Test
    void testExpandImplicitValueSet() {
        String url = "/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs";
        ResponseEntity<String> response =
                restTemplate.getForEntity(url+VALUE_SET_VERSION_PARAMETER, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("ValueSet");
        assertThat((String) JsonPath.read(body, "$.url")).isEqualTo("http://snomed.info/sct?fhir_vs");
        assertThat((Integer) JsonPath.read(body, "$.expansion.total")).isGreaterThan(0);
        tutorialResources.remove(url);
    }

    // Test for: curl -s 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=731000124108' | jq
    @Test
    void testExpandExplicitValueSet() {
        String url = "/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=731000124108";
        ResponseEntity<String> response =
                restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("ValueSet");
        assertThat((String) JsonPath.read(body, "$.url")).isEqualTo("http://snomed.info/sct?fhir_vs=731000124108");
        assertThat((Integer) JsonPath.read(body, "$.expansion.total")).isGreaterThan(0);
        tutorialResources.remove(url);
    }

    // Test for: curl -s 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs&filter=diabetes' | jq
    @Test
    void testExpandImplicitValueSetWithFilter() {
        String url = "/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs&filter=diabetes";
        ResponseEntity<String> response =
                restTemplate.getForEntity(url + VALUE_SET_VERSION_PARAMETER, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((Integer) JsonPath.read(body, "$.expansion.total")).isGreaterThan(0);
        tutorialResources.remove(url);
    }

    // Test for: curl -s 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=731000124108&filter=diabetes' | jq
    @Test
    void testExpandExplicitValueSetWithFilter() {
        String url = "/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=731000124108&filter=diabetes";
        ResponseEntity<String> response =
                restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((Integer) JsonPath.read(body, "$.expansion.total")).isGreaterThan(0);
        tutorialResources.remove(url);
    }

    // Test for: curl -s 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=ecl/%3C%3C128927009&filter=gastrointestinal' | jq
    @Test
    void testExpandValueSetWithFilterAndEcl() {
        String url =
                "/fhir/r4/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=ecl/<<128927009&filter=gastrointestinal";
        ResponseEntity<String> response = restTemplate.getForEntity(url + VALUE_SET_VERSION_PARAMETER, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((String) JsonPath.read(body, "$.resourceType")).isEqualTo("ValueSet");
        assertThat((Integer) JsonPath.read(body, "$.expansion.total")).isGreaterThan(0);
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
