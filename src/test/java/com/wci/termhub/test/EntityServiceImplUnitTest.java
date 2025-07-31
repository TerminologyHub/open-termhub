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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.wci.termhub.Application;
import com.wci.termhub.model.HasId;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * The Class EntityServiceImplUnitTest.
 */
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(OrderAnnotation.class)
public class EntityServiceImplUnitTest extends AbstractClassTest {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(EntityServiceImplUnitTest.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The document obj 1. */
  private final TestDocument documentObj1 = new TestDocument("1", "1000", "one", "1 description");

  /** The document obj 2. */
  private final TestDocument documentObj2 = new TestDocument("2", "2000", "two", "2 description");

  /** The document obj 3. */
  private final TestDocument documentObj3 = new TestDocument("3", "3000", "three", "3 description");

  /** The document obj 4. */
  private final TestDocument documentObj4 = new TestDocument("4", "4000", "four", "4 description");

  /**
   * Test create index.
   */
  @Test
  @Order(1)
  public void testCreateIndex() {

    logger.info("Testing CreateIndex");

    assertThrows(IllegalArgumentException.class,
        () -> searchService.createIndex(TestNoDocument.class));
    assertDoesNotThrow(() -> searchService.createIndex(TestDocument.class));
  }

  /**
   * Test delete index.
   */
  @Test
  @Order(2)
  public void testDeleteIndex() {

    logger.info("Testing DeleteIndex");

    assertThrows(IllegalArgumentException.class,
        () -> searchService.deleteIndex(TestNoDocument.class));
    assertDoesNotThrow(() -> searchService.deleteIndex(TestDocument.class));

  }

  /**
   * Test add.
   */
  @Test
  @Order(3)
  public void testAdd() {

    logger.info("Testing Add");

    final TestNoDocument noDocumentObj = new TestNoDocument();
    assertThrows(IllegalArgumentException.class,
        () -> searchService.add(TestNoDocument.class, noDocumentObj));

    assertDoesNotThrow(() -> searchService.createIndex(TestDocument.class));

  }

  /**
   * Test add batch.
   */
  @Test
  @Order(4)
  public void testAddBatch() {

    logger.info("Testing Add Batch");

    final List<HasId> listOfDocuments =
        List.of(documentObj1, documentObj2, documentObj3, documentObj4);

    assertDoesNotThrow(() -> searchService.addBulk(TestDocument.class, listOfDocuments));

  }

  /**
   * Test find.
   */
  @Test
  @Order(5)
  public void testFind() {

    logger.info("Testing Find");

    final SearchParameters searchParameters =
        new SearchParameters("name:" + documentObj1.getName(), 100, 0);

    assertThrows(IllegalArgumentException.class,
        () -> searchService.find(searchParameters, TestNoDocument.class));

    try {

      final ResultList<TestDocument> result =
          searchService.find(searchParameters, TestDocument.class);
      assertNotNull(result);
      assertTrue(result.getItems().iterator().hasNext());
      final TestDocument documentObj = result.getItems().iterator().next();
      assertEquals(documentObj1, documentObj);

      int count = 0;
      for (final HasId object : result.getItems()) {
        count++;
        logger.debug("TestDocumentObject found: {}", object);
      }
      assertEquals(1, count);

    } catch (final Exception e) {
      logger.error("Error finding document", e);
      fail("Error finding document");
    }
  }

  /**
   * Test find by id.
   */
  @Test
  @Order(5)
  public void testFindById() {

    logger.info("Testing Find By Id");

    final String documentId = "4";
    assertThrows(IllegalArgumentException.class,
        () -> searchService.get(documentId, TestNoDocument.class));

    try {

      final TestDocument result = searchService.get(documentId, TestDocument.class);
      assertNotNull(result); // failed
      logger.info("Result: {}", result);
      final TestDocument documentObj = result;
      assertEquals(documentObj4, documentObj);
      assertEquals(documentId, documentObj.getId());

    } catch (final Exception e) {
      logger.error("Error finding document by id", e);
      fail("Error finding document by id");
    }
  }

  /**
   * Test find all.
   */
  @Test
  @Order(6)
  public void testFindAll() {

    logger.info("Testing Find All");

    final SearchParameters searchParameters = new SearchParameters();
    assertThrows(IllegalArgumentException.class,
        () -> searchService.findAll(searchParameters, TestNoDocument.class));

    try {

      final ResultList<TestDocument> result =
          searchService.findAll(searchParameters, TestDocument.class);
      assertNotNull(result);
      assertTrue(result.getItems().iterator().hasNext());
      int count = 0;
      for (final TestDocument object : result.getItems()) {
        count++;
        logger.debug("TestDocumentObject found: {}", object);
      }
      assertEquals(4, count);

    } catch (final Exception e) {
      logger.error("Error finding all documents", e);
      fail("Error finding all documents");
    }
  }

  /**
   * Test combined query.
   */
  @Test
  @Order(7)
  public void testCombinedQuery() {

    final SearchParameters searchParameters = new SearchParameters("name:one OR name:two", 100, 0);

    try {

      final ResultList<TestDocument> result =
          searchService.find(searchParameters, TestDocument.class);
      assertNotNull(result);
      assertTrue(result.getItems().iterator().hasNext());
      int count = 0;
      for (final TestDocument object : result.getItems()) {
        count++;
        logger.debug("TestDocumentObject found: {}", object);
      }
      assertEquals(2, count);

    } catch (final Exception e) {
      logger.error("Error finding all documents", e);
      fail("Error finding all documents");
    }
  }

  /**
   * Test remove.
   */
  @Test
  @Order(8)
  public void testRemove() {

    final String documentId = "4";
    assertDoesNotThrow(() -> searchService.remove(documentId, TestDocument.class));

    try {

      final TestDocument result = searchService.get(documentId, TestDocument.class);
      assertNull(result);
      logger.info("Result: {}", result);

      final ResultList<TestDocument> resultAll =
          searchService.findAll(new SearchParameters(), TestDocument.class);
      assertNotNull(resultAll);
      assertTrue(resultAll.getItems().iterator().hasNext());
      assertEquals(3, resultAll.getItems().size());

    } catch (final Exception e) {
      logger.error("Error finding all documents", e);
      fail("Error finding all documents");
    }

  }

  /**
   * Test udpdate.
   */
  @Test
  @Order(9)
  public void testUdpdate() {

    final String documentId = "4";
    documentObj4.setName("four updated");

    try {

      searchService.update(TestDocument.class, documentId, documentObj4);

      final TestDocument result = searchService.get(documentId, TestDocument.class);
      assertNotNull(result);
      logger.info("Result: {}", result);
      final TestDocument documentObj = result;
      assertEquals(documentObj4, documentObj);
      assertEquals(documentId, documentObj.getId());

    } catch (final Exception e) {
      logger.error("Error finding all documents", e);
      fail("Error finding all documents");
    }

  }

}
