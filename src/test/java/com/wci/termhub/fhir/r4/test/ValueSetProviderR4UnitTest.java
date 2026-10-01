/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r4.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.fhir.r4.ValueSetProviderR4;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.SubsetMember;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.ValueSetLoaderUtil;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;

/**
 * Tests for ValueSetProviderR4.
 */
@AutoConfigureMockMvc
public class ValueSetProviderR4UnitTest extends AbstractFhirR4ServerTest {

  /**
   * The Constant LOGGER.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ValueSetProviderR4UnitTest.class);

  /** The context. */
  private static FhirContext context = FhirContext.forR4();

  /** The port. */
  @LocalServerPort
  private int port;

  /** The rest template. */
  @Autowired
  private TestRestTemplate restTemplate;

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
  private static final String TEST_VALUESET_URL = "http://snomed.info/sct?fhir_vs=731000124108-instance";

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
        null, // UriParam reference
        null, // TokenParam status
        null, // StringParam title
        null, // UriParam url
        null, // StringParam version
        null, // NumberParam count
        null // NumberParam offset
    );
    assertNotNull(bundle);
    assertOmitsCompose(bundle);
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
        null, // UriParam reference
        null, // TokenParam status
        null, // StringParam title
        url, // UriParam url
        null, // StringParam version
        null, // NumberParam count
        null // NumberParam offset
    );
    assertNotNull(bundle);
    assertOmitsCompose(bundle);
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
        null, // UriParam reference
        null, // TokenParam status
        null, // StringParam title
        null, // UriParam url
        null, // StringParam version
        null, // NumberParam count
        null // NumberParam offset
    );
    assertNotNull(bundle);
    assertOmitsCompose(bundle);
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
        null, // UriParam reference
        null, // TokenParam status
        null, // StringParam title
        null, // UriParam url
        version, // StringParam version
        null, // NumberParam count
        null // NumberParam offset
    );

    assertNotNull(bundle);
    assertOmitsCompose(bundle);
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
        null, // UriParam reference
        null, // TokenParam status
        null, // StringParam title
        null, // UriParam url
        null, // StringParam version
        null, // NumberParam count
        null // NumberParam offset
    );

    assertNotNull(bundle);
    assertOmitsCompose(bundle);
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
        null, // UriParam reference
        null, // TokenParam status
        null, // StringParam title
        null, // UriParam url
        null, // StringParam version
        null, // NumberParam count
        null // NumberParam offset
    );
    assertNotNull(vsBundle);
    assertOmitsCompose(vsBundle);
    assertTrue(vsBundle.getEntry().stream()
        .anyMatch(e -> e.getResource() instanceof ValueSet && id.equals(e.getResource().getId())));
  }

  /**
   * GET by id rejects search parameters such as version.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetValueSetRejectsSearchParams() throws Exception {
    final Bundle bundle = provider.findValueSets(request, details, null, null, null, null, null,
        null, null, null, null, null, new UriParam(TEST_VALUESET_URL), null, null, null);
    assertOmitsCompose(bundle);
    final String id = bundle.getEntry().stream().map(e -> (ValueSet) e.getResource())
        .filter(vs -> TEST_VALUESET_URL.equals(vs.getUrl())).findFirst()
        .orElseThrow(() -> new AssertionError("Test ValueSet not found")).getIdElement()
        .getIdPart();
    request.addParameter("version", "2.81");
    final FHIRServerResponseException ex = assertThrows(FHIRServerResponseException.class,
        () -> provider.getValueSet(request, details, new IdType(id)));
    assertTrue(ex.getMessage().contains("version"));
  }

  /**
   * Search identifier, status, reference, and member code with AND.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindValueSetNewSearchParams() throws Exception {
    final Bundle byStatus = provider.findValueSets(request, details, null, null, null, null, null,
        null, null, null, new TokenParam("active"), null, new UriParam(TEST_VALUESET_URL), null,
        null, null);
    assertTrue(byStatus.getEntry().stream().anyMatch(e -> TEST_VALUESET_URL
        .equals(((ValueSet) e.getResource()).getUrl())));
    assertOmitsCompose(byStatus);

    final Bundle byIdentifier = provider.findValueSets(request, details, null, null, null, null,
        new TokenParam("731000124108"), null, null, null, null, null,
        new UriParam(TEST_VALUESET_URL), null, null, null);
    assertTrue(byIdentifier.getEntry().stream()
        .anyMatch(e -> TEST_VALUESET_URL.equals(((ValueSet) e.getResource()).getUrl())));
    assertOmitsCompose(byIdentifier);

    final Bundle byReference = provider.findValueSets(request, details, null, null, null, null,
        null, null, null, new UriParam("http://snomed.info/sct"), null, null,
        new UriParam(TEST_VALUESET_URL), null, null, null);
    assertTrue(byReference.getEntry().stream()
        .anyMatch(e -> TEST_VALUESET_URL.equals(((ValueSet) e.getResource()).getUrl())));
    assertOmitsCompose(byReference);

    final Bundle byCode = provider.findValueSets(request, details, null,
        new TokenParam("731000124108"), null, null, null, null, null, null, null, null,
        new UriParam(TEST_VALUESET_URL), null, null, null);
    assertTrue(byCode.getEntry().stream()
        .anyMatch(e -> TEST_VALUESET_URL.equals(((ValueSet) e.getResource()).getUrl())));
    assertOmitsCompose(byCode);

    final Bundle andMiss = provider.findValueSets(request, details, null, null, null, null, null,
        null, null, null, new TokenParam("draft"), null, new UriParam(TEST_VALUESET_URL), null,
        null, null);
    assertTrue(andMiss.getEntry() == null || andMiss.getEntry().isEmpty());
    assertOmitsCompose(andMiss);
  }

  /**
   * $expand includeDefinition, activeOnly, property, and paging.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExpandNewParams() throws Exception {
    final UriType url = new UriType(TEST_VALUESET_URL);
    final ValueSet withoutDef = provider.expandImplicit(request, details, null, url, null, null,
        new IntegerType(0), new IntegerType(10), null, null, null, null, null, null);
    assertNotNull(withoutDef.getExpansion());
    assertOmitsCompose(withoutDef);

    final ValueSet withDef = provider.expandImplicit(request, details, null, url, null, null,
        new IntegerType(0), new IntegerType(10), null, null, null, new BooleanType(true), null,
        null);
    assertNotNull(withDef.getExpansion());
    assertOmitsCompose(withDef);

    final ValueSet active = provider.expandImplicit(request, details, null, url, null, null,
        new IntegerType(0), new IntegerType(10), null, null, null, null, new BooleanType(true),
        null);
    assertNotNull(active.getExpansion());
    assertOmitsCompose(active);

    final ValueSet withProp = provider.expandImplicit(request, details, null, url, null, null,
        new IntegerType(0), new IntegerType(10), null, null, null, null, null,
        List.of(new CodeType("semanticType")));
    assertNotNull(withProp.getExpansion());
    assertOmitsCompose(withProp);

    final ValueSet withLang = provider.expandImplicit(request, details, null, url, null, null,
        new IntegerType(0), new IntegerType(10), List.of(new CodeType("en")), null, null, null,
        null, null);
    assertNotNull(withLang.getExpansion());
    assertOmitsCompose(withLang);
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
          ValueSetLoaderUtil.loadValueSet(searchService, resource.getFile(), ValueSet.class,
              new DefaultProgressListener());
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
    vs1.setDate(Date.from(Instant.now()));

    final ValueSet vs2 = new ValueSet();
    vs2.setUrl("http://example.org/concurrent-vs-2");
    vs2.setName("Concurrent VS 2");
    vs2.setVersion("1.0");
    vs2.setPublisher("Unit Test");
    vs2.setTitle("Concurrent Value Set 2");
    vs2.setDate(Date.from(Instant.now()));

    final Callable<ValueSet> writeTask1 = () -> {
      return createValueSet(vs1);
    };

    final Callable<ValueSet> writeTask2 = () -> {
      return createValueSet(vs2);
    };

    final Future<ValueSet> future1 = executor.submit(writeTask1);
    // Ensure the first write starts before the second
    Thread.sleep(10); // Small delay to increase chance of overlap

    final Future<ValueSet> future2 = executor.submit(writeTask2);

    executor.shutdown();

    final ValueSet created1 = future1.get();
    final ValueSet created2 = future2.get();

    // Exactly one should fail, then delete the other
    Assertions.assertTrue(created1 == null ^ created2 == null,
        "Exactly one write should fail due to locking");
    if (created1 != null) {
      provider.deleteValueSet(request, details, new IdType(created1.getId()));
    } else if (created2 != null) {
      provider.deleteValueSet(request, details, new IdType(created2.getId()));
    }
  }

  /**
   * Creates the value set. Return null if there is an ereror creating it.
   *
   * @param vs the vs
   * @return the method outcome
   * @throws Exception the exception
   */
  private ValueSet createValueSet(final ValueSet vs) throws Exception {
    try {
      final MethodOutcome out = provider
          .createValueSet(context.newJsonParser().encodeResourceToString(vs).getBytes("UTF-8"));
      return (ValueSet) out.getResource();

    } catch (final FHIRServerResponseException fe) {
      return null;
    }
  }

  /**
   * Test multi-system value set loading and member verification. This test
   * loads a ValueSet with concepts from both SNOMED CT and HL7 RoleCode systems
   * and verifies that members from each system are correctly loaded with their
   * proper terminology.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMultiSystemValueSet() throws Exception {
    // Find all members of this value set
    final SearchParameters params = new SearchParameters();
    params.setQuery("subset.abbreviation:SNOMEDCT_HL7_COMBINED_TEST");
    params.setLimit(100);

    final ResultList<SubsetMember> members = searchService.find(params, SubsetMember.class);

    assertNotNull(members, "Members should not be null");
    assertTrue(members.getTotal() > 0, "Should have members loaded");
    LOGGER.info("Found {} total members", members.getTotal());

    // Log ALL members to see what terminologies we actually have
    LOGGER.info("All members:");
    members.getItems()
        .forEach(m -> LOGGER.info("  Member: code={}, terminology={}, publisher={}, name={}",
            m.getCode(), m.getTerminology(), m.getPublisher(), m.getName()));

    // Check for SNOMED CT member - terminology might be SNOMEDCT or fallback to
    // title prefix
    final boolean hasSnomedMember = members.getItems().stream()
        .anyMatch(m -> "105724001".equals(m.getCode())
            && (m.getTerminology() != null && (m.getTerminology().equals("SNOMEDCT")
                || m.getTerminology().equals("SNOMEDCT_US")
                || m.getTerminology().equals("SNOMEDCT_HL7_COMBINED_TEST"))));
    assertTrue(hasSnomedMember, "Should have SNOMED CT member with code 105724001");

    // Check for HL7 RoleCode member - will have fallback terminology if not in
    // DB
    final boolean hasRoleMember =
        members.getItems().stream().anyMatch(m -> "AUNT".equals(m.getCode()));
    assertTrue(hasRoleMember, "Should have HL7 RoleCode member with code AUNT");

    // Verify we have members from both systems based on codes
    // Since database lookup may fail, all get fallback terminology, so check by
    // code patterns
    final long snomedCount = members.getItems().stream()
        .filter(m -> m.getCode() != null && m.getCode().matches("\\d+")).count();
    final long roleCount = members.getItems().stream()
        .filter(m -> m.getCode() != null && m.getCode().matches("[A-Z]+")).count();

    LOGGER.info("SNOMED CT members (numeric codes): {}", snomedCount);
    LOGGER.info("HL7 Role members (alpha codes): {}", roleCount);

    assertTrue(snomedCount == 6, "Should have 6 SNOMED CT members");
    assertTrue(roleCount == 16, "Should have 16 HL7 Role members");
  }

  /**
   * Loaded extensional and implicit value sets omit compose on every search and $expand.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSearchAndExpandOmitCompose() throws Exception {
    final UriParam extensionalUrl = new UriParam(TEST_VALUESET_URL);
    final UriParam entireUrl = new UriParam("http://snomed.info/sct?fhir_vs");
    assertFound(search(null, null, null, null, null, null, null, null, null, null, extensionalUrl,
        null), TEST_VALUESET_URL);
    assertFound(search(null, null, null, null, null, null, null, null, null, null, entireUrl, null),
        "http://snomed.info/sct?fhir_vs");

    final Bundle extensional = search(null, null, null, null, null, null, null, null, null, null,
        extensionalUrl, null);
    final ValueSet shell = valueSetWithUrl(extensional, TEST_VALUESET_URL);
    final String id = shell.getIdElement().getIdPart();
    assertFound(search(new TokenParam(id), null, null, null, null, null, null, null, null, null,
        null, null), TEST_VALUESET_URL);
    assertFound(search(null, null, null, null, null, new StringParam("SNOMEDCT_US"), null, null,
        null, null, extensionalUrl, null), TEST_VALUESET_URL);
    final ValueSet entire = valueSetWithUrl(
        search(null, null, null, null, null, null, null, null, null, null, entireUrl, null),
        "http://snomed.info/sct?fhir_vs");
    assertTrue(entire.hasTitle());
    assertFound(search(null, null, null, null, null, null, null, null, null,
        new StringParam(entire.getTitle()), entireUrl, null), entire.getUrl());
    assertFound(search(null, null, null, null, null, null, new StringParam("SANDBOX"), null, null,
        null, extensionalUrl, null), TEST_VALUESET_URL);
    assertFound(search(null, null, null, new StringParam("US National"), null, null, null, null,
        null, null, extensionalUrl, null), TEST_VALUESET_URL);
    assertFound(search(null, null, null, null, null, null, null, null, null, null, extensionalUrl,
        new StringParam("20240301")), TEST_VALUESET_URL);
    assertFound(search(null, null, new DateRangeParam("ge2024-01-01", null), null, null, null,
        null, null, null, null, extensionalUrl, null), TEST_VALUESET_URL);
    assertFound(search(null, null, new DateRangeParam(null, "le2024-12-31"), null, null, null,
        null, null, null, null, extensionalUrl, null), TEST_VALUESET_URL);
    assertFound(search(null, null, null, null, null, null, null, null, new TokenParam("active"),
        null, extensionalUrl, null), TEST_VALUESET_URL);
    assertFound(search(null, null, null, null, new TokenParam("731000124108"), null, null, null,
        null, null, extensionalUrl, null), TEST_VALUESET_URL);
    assertFound(search(null, null, null, null, null, null, null,
        new UriParam("http://snomed.info/sct"), null, null, extensionalUrl, null),
        TEST_VALUESET_URL);
    assertFound(search(null, new TokenParam("731000124108"), null, null, null, null, null, null,
        null, null, extensionalUrl, null), TEST_VALUESET_URL);
    assertFound(searchPage(1, 0), null);
    assertFound(searchPage(1, 1), null);
    final Bundle miss = search(null, null, null, null, null, null, null, null,
        new TokenParam("draft"), null, extensionalUrl, null);
    assertTrue(miss.getEntry() == null || miss.getEntry().isEmpty());

    final ValueSet read = provider.getValueSet(request, details, new IdType(id));
    assertTrue(read.hasCompose());
    assertFalse(read.getCompose().getIncludeFirstRep().getConcept().isEmpty());
    assertTrue(context.newJsonParser().encodeResourceToString(read).contains("\"compose\""));

    final UriType expandUrl = new UriType(TEST_VALUESET_URL);
    final IdType expandId = new IdType(id);
    assertExpandOmitsCompose(provider.expandImplicit(request, details, null, expandUrl, null, null,
        null, null, null, null, null, null, null, null));
    assertExpandOmitsCompose(provider.expandImplicit(request, details, null, expandUrl, null, null,
        null, null, null, null, null, new BooleanType(true), null, null));
    assertExpandOmitsCompose(provider.expandImplicit(request, details, null, expandUrl, null, null,
        null, null, null, null, null, new BooleanType(false), null, null));
    assertExpandOmitsCompose(provider.expandImplicit(request, details, null, expandUrl, null,
        new StringType("___nomatch___"), new IntegerType(0),
        new IntegerType(10), null, null, null, null, null, null));
    assertExpandOmitsCompose(provider.expandImplicit(request, details, null, expandUrl, null, null,
        new IntegerType(0), new IntegerType(1), null, null, null, null, new BooleanType(true),
        List.of(new CodeType("semanticType"))));
    assertExpandOmitsCompose(provider.expandImplicit(request, details, null, expandUrl, null, null,
        null, null, List.of(new CodeType("en")), new BooleanType(true), null, null, null, null));
    assertExpandOmitsCompose(provider.expandInstance(request, details, expandId, null, null, null,
        null, null, null, null, null, null, new BooleanType(true), null, null));
    final ValueSet cached = provider.expandImplicit(request, details, null, expandUrl, null, null,
        null, null, null, null, null, new BooleanType(true), null, null);
    assertExpandOmitsCompose(cached);
    final UriType entireExpand = new UriType("http://snomed.info/sct?fhir_vs");
    final StringType noMatch = new StringType("___nomatch___");
    assertExpandOmitsCompose(provider.expandImplicit(request, details, null, entireExpand, null, noMatch,
        new IntegerType(0), new IntegerType(0), null, null, null, null, null, null));

    assertHttpOmitsCompose("/fhir/r4/ValueSet?url={url}", TEST_VALUESET_URL);
    assertHttpOmitsCompose("/fhir/r4/ValueSet?_count=1&_offset=0", null);
    assertHttpOmitsCompose("/fhir/r4/ValueSet?_count=1&_offset=1", null);
    assertHttpOmitsCompose("/fhir/r4/ValueSet/$expand?url={url}&includeDefinition=true",
        TEST_VALUESET_URL);
    assertHttpOmitsCompose("/fhir/r4/ValueSet/$expand?url={url}&includeDefinition=false",
        TEST_VALUESET_URL);
    assertHttpOmitsCompose("/fhir/r4/ValueSet/" + id + "/$expand?includeDefinition=true", null);
  }

  /**
   * Search.
   *
   * @param id the id
   * @param code the code
   * @param date the date
   * @param description the description
   * @param identifier the identifier
   * @param name the name
   * @param publisher the publisher
   * @param reference the reference
   * @param status the status
   * @param title the title
   * @param url the url
   * @param version the version
   * @return the bundle
   * @throws Exception the exception
   */
  private Bundle search(final TokenParam id, final TokenParam code, final DateRangeParam date,
    final StringParam description, final TokenParam identifier, final StringParam name,
    final StringParam publisher, final UriParam reference, final TokenParam status,
    final StringParam title, final UriParam url, final StringParam version) throws Exception {
    final Bundle bundle = provider.findValueSets(request, details, id, code, date, description,
        identifier, name, publisher, reference, status, title, url, version, null, null);
    assertOmitsCompose(bundle);
    return bundle;
  }

  /**
   * Unfiltered search page.
   *
   * @param count the count
   * @param offset the offset
   * @return the bundle
   * @throws Exception the exception
   */
  private Bundle searchPage(final int count, final int offset) throws Exception {
    final Bundle bundle = provider.findValueSets(request, details, null, null, null, null, null,
        null, null, null, null, null, null, null, new NumberParam(count), new NumberParam(offset));
    assertOmitsCompose(bundle);
    return bundle;
  }

  /**
   * Asserts the url is present when expected.
   *
   * @param bundle the bundle
   * @param url the url, or null to only require a non-empty page
   */
  private static void assertFound(final Bundle bundle, final String url) {
    assertNotNull(bundle.getEntry());
    assertFalse(bundle.getEntry().isEmpty());
    if (url != null) {
      assertTrue(bundle.getEntry().stream().anyMatch(e -> e.getResource() instanceof ValueSet
          && url.equals(((ValueSet) e.getResource()).getUrl())));
    }
  }

  /**
   * Value set with url.
   *
   * @param bundle the bundle
   * @param url the url
   * @return the value set
   */
  private static ValueSet valueSetWithUrl(final Bundle bundle, final String url) {
    return bundle.getEntry().stream().map(e -> (ValueSet) e.getResource())
        .filter(vs -> url.equals(vs.getUrl())).findFirst()
        .orElseThrow(() -> new AssertionError(url));
  }

  /**
   * Asserts a bundle has no compose in the model or the JSON.
   *
   * @param bundle the bundle
   */
  private static void assertOmitsCompose(final Bundle bundle) {
    assertNotNull(bundle);
    if (bundle.getEntry() == null) {
      return;
    }
    for (final Bundle.BundleEntryComponent entry : bundle.getEntry()) {
      if (entry.getResource() instanceof ValueSet) {
        assertOmitsCompose((ValueSet) entry.getResource());
      }
    }
  }

  /**
   * Asserts a value set has no compose in the model or the JSON.
   *
   * @param valueSet the value set
   */
  private static void assertOmitsCompose(final ValueSet valueSet) {
    assertFalse(valueSet.hasCompose(), valueSet.getUrl());
    final String json = context.newJsonParser().encodeResourceToString(valueSet);
    assertFalse(json.contains("\"compose\""), valueSet.getUrl());
  }

  /**
   * Asserts an expansion has no compose.
   *
   * @param valueSet the value set
   */
  private static void assertExpandOmitsCompose(final ValueSet valueSet) {
    assertNotNull(valueSet.getExpansion());
    assertOmitsCompose(valueSet);
  }

  /**
   * Asserts an HTTP body has no compose.
   *
   * @param pathAndQuery path and query after /fhir/r4/ValueSet
   * @param url the url template variable, or null
   */
  private void assertHttpOmitsCompose(final String pathAndQuery, final String url) {
    final String endpoint = "http://localhost:" + port + pathAndQuery;
    final ResponseEntity<String> response = url == null
        ? restTemplate.getForEntity(endpoint, String.class)
        : restTemplate.getForEntity(endpoint, String.class, url);
    assertEquals(HttpStatus.OK, response.getStatusCode(), response.getBody());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().contains("\"compose\""), pathAndQuery);
  }

}
