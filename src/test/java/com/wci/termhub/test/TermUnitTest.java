/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.test.context.ActiveProfiles;

import com.wci.termhub.Application;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Term;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.StringUtility;

/**
 * The Class TermUnitTest.
 */
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
public class TermUnitTest extends AbstractClassTest {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(TermUnitTest.class);

  /** The term 1. */
  private static final Term TERM1 = new Term();

  /** The term 2. */
  private static final Term TERM2 = new Term();

  /** The term 3. */
  private static final Term TERM3 = new Term();

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Setup.
   */
  @Test
  @Order(1)
  public void setup() {

    logger.info("Creating object test data");
    // string, FiedType.Text, FieldType.Keyword
    TERM1.setId("36ab1ce6-4fbb-4f86-a5bb-6974b7aa38f8");
    TERM1.setName("name-a");
    TERM1.setTerminology("terminology");
    TERM1.setVersion("version");
    TERM1.setPublisher("publisher-a");
    TERM1.setComponentId("08098098-a");
    TERM1.setConceptId("12345-a");
    TERM1.setDescriptorId("876876-a");
    TERM1.setType("type-a");
    TERM1.setCode("1234567890");
    // Map of key-value pairs, FiedType.Object
    final Map<String, String> attributes1 = new HashMap<>();
    attributes1.put("key1-a", "value1-a");
    attributes1.put("key2-a", "value2-a");
    TERM1.setAttributes(attributes1);

    TERM2.setId("910b9c92-1074-4734-ac2b-3664efb54ac1");
    TERM2.setName("name-b");
    TERM2.setTerminology("terminology");
    TERM2.setVersion("version");
    TERM2.setPublisher("publisher-b");
    TERM2.setComponentId("08098098-b");
    TERM2.setConceptId("12345-b");
    TERM2.setDescriptorId("876876-b");
    TERM2.setType("type-b");
    TERM2.setCode("9876543210");
    // Map of key-value pairs, FiedType.Object
    final Map<String, String> attributes2 = new HashMap<>();
    attributes2.put("key1-b", "value1-b");
    attributes2.put("key2-b", "value2-b");
    TERM2.setAttributes(attributes2);

    TERM3.setId("722b9816-3226-40aa-9935-3bcd0ebd47aa");
    TERM3.setName("dummyname with space");
    TERM3.setTerminology("dummyterminology");
    TERM3.setVersion("dummyversion");
    TERM3.setPublisher("dummy publisher with space");
    TERM3.setComponentId("dummycomponentId");
    TERM3.setConceptId("dummyconceptId");
    TERM3.setDescriptorId("dummydescriptorId");
    TERM3.setType("dummytype");
    TERM3.setCode("dummycode");
    // Map of key-value pairs, FiedType.Object
    final Map<String, String> attributes3 = new HashMap<>();
    attributes3.put("key1-dummy", "value1-dummy");
    attributes3.put("key2-dummy", "value2-dummy");
    TERM3.setAttributes(attributes3);
  }

  /**
   * Checks for document annotation.
   */
  @Test
  @Order(2)
  public void hasDocumentAnnotation() {

    // check if the class has the @Document annotation if not, throw an
    // exception
    final Class<?> clazz1 = Term.class;
    assertTrue(clazz1.isAnnotationPresent(Document.class));
  }

  /**
   * Creates the index.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(3)
  public void verifyIndex() throws Exception {

    logger.info("Verify index for Term in INDEX_DIRECTORY:{}", getIndexDirectory());
    final File indexFile = new File(getIndexDirectory(), Term.class.getCanonicalName());
    assertTrue(indexFile.exists(),
        "Index directory does not exist: " + indexFile.getAbsolutePath());
  }

  /**
   * Test create term.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(4)
  public void testAddTerm() throws Exception {

    logger.info("Creating objects");
    assertDoesNotThrow(() -> searchService.add(Term.class, TERM1));
    assertDoesNotThrow(() -> searchService.add(Term.class, TERM2));
    assertDoesNotThrow(() -> searchService.add(Term.class, TERM3));
  }

  /**
   * Test find.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(5)
  public void testFind() throws Exception {

    ResultList<Term> foundTermsObjects = null;
    final SearchParameters searchParameters = new SearchParameters();

    // find the term by code
    searchParameters.setQuery("code:" + TERM1.getCode());
    logger.info("Search for : {}", searchParameters.getQuery());
    foundTermsObjects = searchService.find(searchParameters, Term.class);
    assertEquals(1, foundTermsObjects.getItems().size());

    for (final Object foundTermObject : foundTermsObjects.getItems()) {
      final Term foundTerm = (Term) foundTermObject;
      logger.info("Term found: {}", foundTerm);
      assertEquals(TERM1.toString(), foundTerm.toString());
    }

    // now find the term by code
    searchParameters.setQuery("code:" + TERM2.getCode());
    logger.info("Search for : {}", searchParameters.getQuery());
    foundTermsObjects = searchService.find(searchParameters, Term.class);
    assertEquals(1, foundTermsObjects.getItems().size());

    for (final Object foundTermObject : foundTermsObjects.getItems()) {
      final Term foundTerm = (Term) foundTermObject;
      logger.info("Term found: {}", foundTerm);
      assertEquals(TERM2.toString(), foundTerm.toString());
    }

    searchParameters.setQuery("code:1234567*");
    logger.info("Search for : {}", searchParameters.getQuery());
    foundTermsObjects = searchService.find(searchParameters, Term.class);
    assertEquals(1, foundTermsObjects.getItems().size());

    for (final Object foundTermObject : foundTermsObjects.getItems()) {
      final Term foundTerm = (Term) foundTermObject;
      foundTerm.getAttributes().entrySet().stream().sorted(Map.Entry.comparingByKey());
      assertEquals(TERM1.toString(), foundTerm.toString());
      logger.info("Term found: {}", foundTerm);
    }

    searchParameters.setQuery("code:" + TERM1.getCode() + " OR code:" + TERM2.getCode());
    logger.info("Search for : {}", searchParameters.getQuery());
    foundTermsObjects = searchService.find(searchParameters, Term.class);
    assertEquals(2, foundTermsObjects.getItems().size());

    searchParameters.setQuery(StringUtility.escapeField("publisher", "dummy publisher with space"));
    logger.info("Search for : {}", searchParameters.getQuery());
    foundTermsObjects = searchService.find(searchParameters, Term.class);
    assertEquals(1, foundTermsObjects.getItems().size());

    // add more complex queries
    searchParameters.setQuery("name:\"" + TERM3.getName() + "\"");
    logger.info("Search for : {}", searchParameters.getQuery());
    foundTermsObjects = searchService.find(searchParameters, Term.class);
    assertEquals(1, foundTermsObjects.getItems().size());

    // add more complex queries
    searchParameters.setQuery("code:" + TERM1.getCode() + " AND name:\""
        + StringUtility.escapeQuery(TERM2.getName()) + "\"");
    logger.info("Search for : {}", searchParameters.getQuery());
    foundTermsObjects = searchService.find(searchParameters, Term.class);
    assertEquals(0, foundTermsObjects.getItems().size());

    // wild card search
    searchParameters.setQuery("*:*");
    foundTermsObjects = searchService.find(searchParameters, Term.class);
    assertEquals(3, foundTermsObjects.getItems().size());

    // search for all
    searchParameters.setQuery(null);
    foundTermsObjects = searchService.findAll(searchParameters, Term.class);
    assertEquals(3, foundTermsObjects.getItems().size());

    // search by id
    searchParameters.setQuery(null);
    final Term foundTermOjbect = searchService.get(TERM3.getId(), Term.class);
    assertNotNull(foundTermOjbect);
    assertEquals(TERM3.toString(), foundTermOjbect.toString());
  }

  /**
   * Test delete.
   *
   * @throws Exception the exception
   */
  @Test
  @Order(6)
  public void testRemove() throws Exception {

    logger.info("Deleting objects");
    assertDoesNotThrow(() -> searchService.remove(TERM1.getId(), Term.class));
    logger.info("Done deleting");

    // find the term by code
    final SearchParameters searchParameters = new SearchParameters();
    searchParameters.setQuery("id:" + TERM1.getId());
    final ResultList<Term> foundTermsObjects = searchService.find(searchParameters, Term.class);
    logger.info("Found: {}", foundTermsObjects.getItems().size());
    assertEquals(0, foundTermsObjects.getItems().size());
  }

}
