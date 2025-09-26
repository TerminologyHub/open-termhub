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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;

import com.wci.termhub.fhir.r4.ValueSetProviderR4;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.ValueSetLoaderUtil;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;

/**
 * The Class ValueSetProviderR4UnitTest.
 */
@AutoConfigureMockMvc
public class ValueSetProviderR4UnitTest extends AbstractFhirR4ServerTest {

  /**
   * The Constant LOGGER.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ValueSetProviderR4UnitTest.class);

  /** The context. */
  private static FhirContext context = FhirContext.forR4();

  /**
   * The search service.
   */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * List of FHIR Code System files to load.
   */
  private static final List<String> VALUE_SET_FILES =
      List.of("ValueSet-snomedct_us-extension-sandbox-20240301-r4.json");

  /**
   * The provider.
   */
  @Autowired
  private ValueSetProviderR4 provider;

  /**
   * The request.
   */
  private MockHttpServletRequest request;

  /**
   * The details.
   */
  private ServletRequestDetails details;

  /**
   * The Constant TEST_VALUESET_URL.
   */
  private static final String TEST_VALUESET_URL = "http://snomed.info/sct?fhir_vs=731000124108";

  /**
   * Setup.
   */
  @BeforeEach
  public void setup() {
    request = new MockHttpServletRequest();
    details = new ServletRequestDetails();
    details.setServletRequest(request);
  }

  /**
   * Test find value sets.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindValueSets() throws Exception {
    final Bundle bundle = provider.findValueSets(request, details,

        null, // TokenParam id
        null, // TokenParam code
        null, // DateRangeParam date
        null, // StringParam description
        null, // TokenParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // StringParam title
        null, // UriParam url
        null, // StringParam version
        null, // NumberParam count
        null // NumberParam offset
    );
    assertNotNull(bundle);
    bundle.getEntry().forEach(entry -> {
      if (entry.getResource() instanceof ValueSet) {
        final ValueSet vs = (ValueSet) entry.getResource();
        LOGGER.info("FOUND ValueSet: id={}, url={}, name={}, version={}",
            vs.getIdElement().getIdPart(), vs.getUrl(), vs.getName(), vs.getVersion());
      }
    });

    assertTrue(bundle.getEntry().stream().anyMatch(e -> e.getResource() instanceof ValueSet));
  }

  /**
   * Test find value set by url.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindValueSetByUrl() throws Exception {
    final UriParam url = new UriParam(TEST_VALUESET_URL);
    final Bundle bundle = provider.findValueSets(request, details,

        null, // TokenParam id
        null, // TokenParam code
        null, // DateRangeParam date
        null, // StringParam description
        null, // TokenParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // StringParam title
        url, // UriParam url
        null, // StringParam version
        null, // NumberParam count
        null // NumberParam offset
    );
    assertNotNull(bundle);
    assertTrue(bundle.getEntry().stream().anyMatch(e -> e.getResource() instanceof ValueSet
        && TEST_VALUESET_URL.equals(((ValueSet) e.getResource()).getUrl())));
  }

  /**
   * Test find value set by name.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindValueSetByName() throws Exception {
    final StringParam name = new StringParam("SNOMEDCT_US extension concepts");
    final Bundle bundle = provider.findValueSets(request, details,

        null, // TokenParam id
        null, // TokenParam code
        null, // DateRangeParam date
        null, // StringParam description
        null, // TokenParam identifier
        name, // StringParam name
        null, // StringParam publisher
        null, // StringParam title
        null, // UriParam url
        null, // StringParam version
        null, // NumberParam count
        null // NumberParam offset
    );
    assertNotNull(bundle);
    assertTrue(bundle.getEntry().stream().anyMatch(e -> e.getResource() instanceof ValueSet
        && "SNOMEDCT_US extension concepts".equals(((ValueSet) e.getResource()).getName())));
  }

  /**
   * Test find value set by version.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindValueSetByVersion() throws Exception {
    final StringParam version = new StringParam("20240301");
    final Bundle bundle = provider.findValueSets(request, details,

        null, // TokenParam id
        null, // TokenParam code
        null, // DateRangeParam date
        null, // StringParam description
        null, // TokenParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // StringParam title
        null, // UriParam url
        version, // StringParam version
        null, // NumberParam count
        null // NumberParam offset
    );

    assertNotNull(bundle);
    assertTrue(bundle.getEntry().stream().anyMatch(e -> e.getResource() instanceof ValueSet
        && "20240301".equals(((ValueSet) e.getResource()).getVersion())));
  }

  /**
   * Test get value set by id.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetValueSetById() throws Exception {
    // Get all ValueSets and find the one with the test URL
    final Bundle bundle = provider.findValueSets(request, details,

        null, // TokenParam id
        null, // TokenParam code
        null, // DateRangeParam date
        null, // StringParam description
        null, // TokenParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // StringParam title
        null, // UriParam url
        null, // StringParam version
        null, // NumberParam count
        null // NumberParam offset
    );

    assertNotNull(bundle);
    final ValueSet found = bundle.getEntry().stream()
        .filter(e -> e.getResource() instanceof ValueSet).map(e -> (ValueSet) e.getResource())
        .filter(vs -> TEST_VALUESET_URL.equals(vs.getUrl())).findFirst()
        .orElseThrow(() -> new AssertionError("Test ValueSet not found by URL"));
    final String id = found.getIdElement().getIdPart();
    final Bundle vsBundle = provider.findValueSets(request, details,

        new TokenParam(id), // TokenParam id
        null, // TokenParam code
        null, // DateRangeParam date
        null, // StringParam description
        null, // TokenParam identifier
        null, // StringParam name
        null, // StringParam publisher
        null, // StringParam title
        null, // UriParam url
        null, // StringParam version
        null, // NumberParam count
        null // NumberParam offset
    );
    assertNotNull(vsBundle);
    assertTrue(vsBundle.getEntry().stream()
        .anyMatch(e -> e.getResource() instanceof ValueSet && id.equals(e.getResource().getId())));
  }

  /**
   * Test reload value set.
   *
   * @throws Exception the exception
   */
  @Test
  public void testReloadValueSet() throws Exception {
    // Should throw an exception if the code system is already loaded
    for (final String valueSetFile : VALUE_SET_FILES) {
      try {
        final Resource resource = new ClassPathResource("data/" + valueSetFile,
            ValueSetProviderR4UnitTest.class.getClassLoader());

        assertThrows(Exception.class, () -> {
          LOGGER.info("Attempt reload of value set from classpath resource: data/{}", valueSetFile);
          ValueSetLoaderUtil.loadSubset(searchService, resource.getFile(), ValueSet.class);
        });

      } catch (final Exception e) {
        LOGGER.error("Error reloading value set file: {}", valueSetFile, e);
        throw e;
      }
    }
  }

  /**
   * Test concurrent add.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConcurrentAdd() throws Exception {
    final ExecutorService executor = Executors.newFixedThreadPool(2);

    final ValueSet vs1 = new ValueSet();
    vs1.setUrl("http://example.org/concurrent-vs-1");
    vs1.setName("Concurrent VS 1");
    vs1.setVersion("1.0");
    vs1.setPublisher("Unit Test");
    vs1.setTitle("Concurrent Value Set 1");
    vs1.setDate(java.util.Date.from(java.time.Instant.now()));

    final ValueSet vs2 = new ValueSet();
    vs2.setUrl("http://example.org/concurrent-vs-2");
    vs2.setName("Concurrent VS 2");
    vs2.setVersion("1.0");
    vs2.setPublisher("Unit Test");
    vs2.setTitle("Concurrent Value Set 2");
    vs2.setDate(java.util.Date.from(java.time.Instant.now()));

    final Callable<MethodOutcome> writeTask1 = () -> {
      return createValueSet(vs1);
    };

    final Callable<MethodOutcome> writeTask2 = () -> {
      return createValueSet(vs2);
    };

    final Future<MethodOutcome> future1 = executor.submit(writeTask1);
    // Ensure the first write starts before the second
    Thread.sleep(10); // Small delay to increase chance of overlap

    final Future<MethodOutcome> future2 = executor.submit(writeTask2);

    executor.shutdown();

    final MethodOutcome created1 = future1.get();
    final MethodOutcome created2 = future2.get();

    // Exactly one should fail
    Assertions.assertTrue(created1.getCreated() ^ created2.getCreated(),
        "Exactly one write should fail due to locking");
    if (created1.getCreated()) {
      provider.deleteValueSet(request, details, ((IdType) created1.getId()));
    } else {
      provider.deleteValueSet(request, details, ((IdType) created2.getId()));
    }
  }

  /**
   * Creates the value set.
   *
   * @param vs the vs
   * @return the method outcome
   * @throws Exception the exception
   */
  private MethodOutcome createValueSet(final ValueSet vs) throws Exception {
    try {
      // Turn value set to bytes and send back through
      return provider
          .createValueSet(context.newJsonParser().encodeResourceToString(vs).getBytes("UTF-8"));
    } catch (final FHIRServerResponseException e) {
      final MethodOutcome methodOutcome = new MethodOutcome();
      methodOutcome.setCreated(false);
      return methodOutcome;
    }
  }

}
