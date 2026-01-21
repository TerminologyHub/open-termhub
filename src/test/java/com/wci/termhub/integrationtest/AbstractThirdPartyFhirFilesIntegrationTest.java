/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.integrationtest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.test.AbstractServerTest;

/**
 * Abstract base for tests that load third-party FHIR resource files via endpoints.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties", properties = {
    "lucene.index.directory=build/index/lucene-3rd-party-files"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractThirdPartyFhirFilesIntegrationTest extends AbstractServerTest {

  /** The logger. */
  private final Logger logger =
      LoggerFactory.getLogger(AbstractThirdPartyFhirFilesIntegrationTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The index directory. */
  @Value("${lucene.index.directory}")
  private String indexDirectory;

  /** The setup once. */
  private static boolean setupOnce = false;

  /** The rest template. */
  @Autowired
  private TestRestTemplate restTemplate;

  /**
   * Setup once.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public void setupData() throws Exception {
    if (setupOnce) {
      return;
    }
    try {
      clearAndCreateIndexDirectories(searchService, indexDirectory);
    } catch (final Exception e) {
      logger.error("Error setting up data: {}", e.getMessage(), e);
      throw e;
    }
    setupOnce = true;
  }

  /**
   * Post resource.
   *
   * @param url the url
   * @param json the json
   */
  protected void postResource(final String url, final String json) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/fhir+json"));
    final HttpEntity<String> entity = new HttpEntity<>(json, headers);
    final ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
    assertTrue(response.getStatusCode().is2xxSuccessful());
  }

  /**
   * Post bundle resource.
   *
   * @param url the url
   * @param json the json
   */
  protected void postBundle(final String url, final String json) {
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("resource", new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8)) {
      @Override
      public String getFilename() {
        return "bundle.json";
      }
    });

    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    final HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
    final ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
    assertTrue(response.getStatusCode().is2xxSuccessful());
  }

  /**
   * Get resource key from JSON.
   *
   * @param node the JSON node
   * @param resourceType the resource type
   * @return the resource key
   */
  protected String getResourceKey(final JsonNode node, final String resourceType) {
    String title = null;
    String publisher = null;
    String version = null;
    if ("ValueSet".equals(resourceType)) {
      title = node.path("title").asText();
      publisher = node.path("publisher").asText();
      version = node.path("version").asText();
    } else if ("CodeSystem".equals(resourceType)) {
      title = node.path("title").asText();
      publisher = node.path("publisher").asText();
      version = node.path("version").asText();
    } else if ("ConceptMap".equals(resourceType)) {
      title = node.path("title").asText();
      publisher = node.path("publisher").asText();
      version = node.path("version").asText();
    }
    if (title != null && !title.isEmpty() && publisher != null && !publisher.isEmpty()
        && version != null && !version.isEmpty()) {
      return title + "|" + publisher + "|" + version;
    }
    return null;
  }

  /**
   * Get all files.
   *
   * @param dir the dir
   * @return the list
   * @throws Exception the exception
   */
  protected static List<Path> getAllFiles(final String dir) throws Exception {
    final File root = new File(dir);
    final File[] files = root.listFiles((d, name) -> name.endsWith(".json"));
    assertNotNull(files);
    final List<Path> list = new ArrayList<>();
    for (final File f : files) {
      list.add(f.toPath());
    }
    return list;
  }

  /**
   * First code from code system.
   *
   * @param node the node
   * @return the string
   */
  protected static String firstCodeFromCodeSystem(final JsonNode node) {
    final JsonNode concepts = node.path("concept");
    if (concepts != null && concepts.isArray() && concepts.size() > 0) {
      final JsonNode first = concepts.get(0);
      final String code = first.path("code").asText();
      return (code == null || code.isEmpty()) ? null : code;
    }
    return null;
  }

  /**
   * First translate params.
   *
   * @param cm the cm
   * @return the translate params
   */
  protected static TranslateParams firstTranslateParams(final JsonNode cm) {
    final JsonNode groupArr = cm.path("group");
    if (groupArr != null && groupArr.isArray() && groupArr.size() > 0) {
      final JsonNode group = groupArr.get(0);
      final String system = group.path("source").asText();
      final ArrayNode elementArr = (ArrayNode) group.path("element");
      if (elementArr != null && elementArr.size() > 0) {
        final JsonNode element = elementArr.get(0);
        final String code = element.path("code").asText();
        if (system != null && !system.isEmpty() && code != null && !code.isEmpty()) {
          return new TranslateParams(system, code);
        }
      }
    }
    return null;
  }

  /**
   * Encode.
   *
   * @param s the s
   * @return the string
   */
  protected static String encode(final String s) {
    return s.replace(" ", "%20");
  }

  /**
   * The Class TranslateParams.
   */
  protected static final class TranslateParams {

    /** The system. */
    private final String system;

    /** The code. */
    private final String code;

    /**
     * Instantiates a new translate params.
     *
     * @param system the system
     * @param code the code
     */
    TranslateParams(final String system, final String code) {
      this.system = system;
      this.code = code;
    }

    /**
     * Gets the system.
     *
     * @return the system
     */
    public String getSystem() {
      return system;
    }

    /**
     * Gets the code.
     *
     * @return the code
     */
    public String getCode() {
      return code;
    }

  }

  /**
   * Gets the resource type.
   *
   * @param file the file
   * @return the resource type
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  protected static String getResourceType(final Path file) throws Exception {
    final JsonFactory jf = new JsonFactory();
    try (final JsonParser p = jf.createParser(file.toFile())) {
      if (p.nextToken() != JsonToken.START_OBJECT) {
        return null;
      }
      while (p.nextToken() != null) {
        if (p.currentToken() == JsonToken.FIELD_NAME && "resourceType".equals(p.currentName())) {
          p.nextToken();
          return p.getValueAsString();
        }
        p.skipChildren();
      }
      return null;
    }
  }
}
