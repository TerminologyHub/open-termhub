/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
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
   * Test update.
   */
  @Test
  @Order(9)
  public void testUpdate() {

    final String documentId = "20";
    final TestDocument documentToUpdate =
        new TestDocument(documentId, "20000", "twenty", "twenty description");

    try {
      // First add the document
      searchService.add(TestDocument.class, documentToUpdate);

      // Now update it
      final TestDocument updatedDocument =
          new TestDocument(documentId, "20000", "twenty updated", "twenty description updated");
      searchService.update(TestDocument.class, documentId, updatedDocument);

      // Verify the update worked
      final TestDocument result = searchService.get(documentId, TestDocument.class);
      assertNotNull(result);
      logger.info("Result: {}", result);
      assertEquals("twenty updated", result.getName());
      assertEquals("twenty description updated", result.getDescription());
      assertEquals(documentId, result.getId());

    } catch (final Exception e) {
      logger.error("Error testing update", e);
      fail("Error testing update: " + e.getMessage());
    }

  }

  /**
   * Test add field.
   */
  @Test
  @Order(10)
  public void testAddField() {

    logger.info("Testing Add Field");

    // First, let's add a new field to the TestDocument class
    final TestDocument documentWithNewField =
        new TestDocument("5", "5000", "five", "five description");

    try {
      // Add the document first
      searchService.add(TestDocument.class, documentWithNewField);

      // Now add a new field to the existing document
      final TestDocument updateEntity = new TestDocument();
      updateEntity.setCode("5000_updated");

      searchService.addField(TestDocument.class, "5", updateEntity, "code");

      // Verify the field was added by retrieving the document
      final TestDocument result = searchService.get("5", TestDocument.class);
      assertNotNull(result);
      assertEquals("5000_updated", result.getCode());
      assertEquals("five", result.getName());
      assertEquals("five description", result.getDescription());

      logger.info("Successfully added field to document: {}", result);

    } catch (final Exception e) {
      logger.error("Error testing add field", e);
      fail("Error testing add field: " + e.getMessage());
    }
  }

  /**
   * Test add field multiple times.
   */
  @Test
  @Order(11)
  public void testAddFieldMultipleTimes() {

    logger.info("Testing Add Field Multiple Times - Field Replacement");

    try {
      // Create a document with multiple fields
      final TestDocument document = new TestDocument("11", "11000", "eleven", "eleven description");
      searchService.add(TestDocument.class, document);

      // First addField call - updates the code field
      final TestDocument updateEntity1 = new TestDocument();
      updateEntity1.setCode("11000_updated");
      searchService.addField(TestDocument.class, "11", updateEntity1, "code");

      // Second addField call - updates the same field again
      final TestDocument updateEntity2 = new TestDocument();
      updateEntity2.setCode("11000_final");
      searchService.addField(TestDocument.class, "11", updateEntity2, "code");

      // Verify the final state - should have the last value
      final TestDocument retrievedDoc = searchService.get("11", TestDocument.class);
      assertNotNull(retrievedDoc, "Document should be retrievable after multiple addField calls");
      assertEquals("11000_final", retrievedDoc.getCode(),
          "Code should have the final updated value");
      assertEquals("eleven", retrievedDoc.getName(), "Name should still be preserved");
      assertEquals("eleven description", retrievedDoc.getDescription(),
          "Description should still be preserved");

      logger.info("Successfully tested add field multiple times: {}", retrievedDoc);

    } catch (final Exception e) {
      fail("Error testing add field multiple times: " + e.getMessage());
    }
  }

  /**
   * Test add field replacement limitation.
   */
  @Test
  @Order(12)
  public void testAddFieldReplacementLimitation() {

    logger.info("Testing Add Field Replacement Limitation");

    try {
      // Create a document with multiple fields
      final TestDocument document = new TestDocument("12", "12000", "twelve", "twelve description");
      searchService.add(TestDocument.class, document);

      // First addField call - updates the code field
      final TestDocument updateEntity1 = new TestDocument();
      updateEntity1.setCode("12000_updated");
      searchService.addField(TestDocument.class, "12", updateEntity1, "code");

      // Second addField call - updates the same field again
      final TestDocument updateEntity2 = new TestDocument();
      updateEntity2.setCode("12000_final");
      searchService.addField(TestDocument.class, "12", updateEntity2, "code");

      // Verify the final state - should have the last value
      final TestDocument retrievedDoc = searchService.get("12", TestDocument.class);
      assertNotNull(retrievedDoc, "Document should be retrievable after multiple addField calls");
      assertEquals("12000_final", retrievedDoc.getCode(),
          "Code should have the final updated value");
      assertEquals("twelve", retrievedDoc.getName(), "Name should still be preserved");
      assertEquals("twelve description", retrievedDoc.getDescription(),
          "Description should still be preserved");

      logger.info("Successfully tested add field replacement limitation: {}", retrievedDoc);

    } catch (final Exception e) {
      fail("Error testing add field replacement limitation: " + e.getMessage());
    }
  }

  /**
   * Test add field with parent class field.
   */
  @Test
  @Order(13)
  public void testAddFieldWithParentClassField() {

    logger.info("Testing Add Field with Parent Class Field");

    try {
      // Create a document
      final TestDocument document =
          new TestDocument("13", "13000", "thirteen", "thirteen description");
      searchService.add(TestDocument.class, document);

      // Update the code field (which is inherited from BaseModel)
      final TestDocument updateEntity = new TestDocument();
      updateEntity.setCode("13000_updated");
      searchService.addField(TestDocument.class, "13", updateEntity, "code");

      // Verify the field was updated
      final TestDocument retrievedDoc = searchService.get("13", TestDocument.class);
      assertNotNull(retrievedDoc,
          "Document should be retrievable after addField with parent class field");
      assertEquals("13000_updated", retrievedDoc.getCode(), "Code should have the updated value");

      logger.info("Successfully tested add field with parent class field: {}", retrievedDoc);

    } catch (final Exception e) {
      fail("Error testing add field with parent class field: " + e.getMessage());
    }
  }

  /**
   * Test add field with invalid field name.
   */
  @Test
  @Order(14)
  public void testAddFieldWithInvalidFieldName() {

    logger.info("Testing Add Field with Invalid Field Name");

    try {
      // Create a document
      final TestDocument document =
          new TestDocument("14", "14000", "fourteen", "fourteen description");
      searchService.add(TestDocument.class, document);

      // Try to add a field that doesn't exist
      final TestDocument updateEntity = new TestDocument();
      updateEntity.setCode("14000_updated");

      // This should throw an IllegalArgumentException
      assertThrows(IllegalArgumentException.class, () -> {
        searchService.addField(TestDocument.class, "14", updateEntity, "nonexistentField");
      });

      // Verify the original document is still intact
      final TestDocument retrievedDoc = searchService.get("14", TestDocument.class);
      assertNotNull(retrievedDoc, "Original document should still be retrievable");
      assertEquals("14000", retrievedDoc.getCode(), "Code should remain unchanged");

      logger.info(
          "Successfully tested add field with invalid field name - exception thrown as expected");

    } catch (final Exception e) {
      fail("Error testing add field with invalid field name: " + e.getMessage());
    }
  }

  /**
   * Test add field with null field value.
   */
  @Test
  @Order(15)
  public void testAddFieldWithNullFieldValue() {

    logger.info("Testing Add Field with Null Field Value");

    try {
      // Create a document
      final TestDocument document =
          new TestDocument("15", "15000", "fifteen", "fifteen description");
      searchService.add(TestDocument.class, document);

      // Try to add a field with null value
      final TestDocument updateEntity = new TestDocument();
      updateEntity.setCode(null); // Set to null
      searchService.addField(TestDocument.class, "15", updateEntity, "code");

      // Verify the document is still retrievable (should not have been
      // modified)
      final TestDocument retrievedDoc = searchService.get("15", TestDocument.class);
      assertNotNull(retrievedDoc,
          "Document should still be retrievable after addField with null value");
      assertEquals("15000", retrievedDoc.getCode(), "Code should remain unchanged");

      logger.info(
          "Successfully tested add field with null field value - field was not updated as expected");

    } catch (final Exception e) {
      fail("Error testing add field with null field value: " + e.getMessage());
    }
  }

  /**
   * Test add field with non-existent document ID.
   */
  @Test
  @Order(16)
  public void testAddFieldWithNonExistentDocumentId() {

    logger.info("Testing Add Field with Non-Existent Document ID");

    try {
      // Try to add a field to a document that doesn't exist
      final TestDocument updateEntity = new TestDocument();
      updateEntity.setCode("nonexistent_updated");

      // This should throw an IllegalStateException
      assertThrows(IllegalStateException.class, () -> {
        searchService.addField(TestDocument.class, "nonexistent_id", updateEntity, "code");
      });

      logger.info(
          "Successfully tested add field with non-existent document ID - exception thrown as expected");

    } catch (final Exception e) {
      fail("Error testing add field with non-existent document ID: " + e.getMessage());
    }
  }

  /**
   * Test add field with null parameters.
   */
  @Test
  @Order(17)
  public void testAddFieldWithNullParameters() {

    logger.info("Testing Add Field with Null Parameters");

    try {
      // Test with null entity
      assertThrows(IllegalArgumentException.class, () -> {
        searchService.addField(TestDocument.class, "test_id", null, "code");
      });

      // Test with null field name
      final TestDocument updateEntity = new TestDocument();
      updateEntity.setCode("test_updated");
      assertThrows(IllegalArgumentException.class, () -> {
        searchService.addField(TestDocument.class, "test_id", updateEntity, null);
      });

      logger.info(
          "Successfully tested add field with null parameters - exceptions thrown as expected");

    } catch (final Exception e) {
      fail("Error testing add field with null parameters: " + e.getMessage());
    }
  }

  /**
   * Test add field preserves all existing fields.
   */
  @Test
  @Order(18)
  public void testAddFieldPreservesAllExistingFields() {

    logger.info("Testing Add Field Preserves All Existing Fields");

    try {
      // Create a document with multiple fields
      final TestDocument document =
          new TestDocument("16", "16000", "sixteen", "sixteen description");
      searchService.add(TestDocument.class, document);

      // Update only the code field
      final TestDocument updateEntity = new TestDocument();
      updateEntity.setCode("16000_updated");
      searchService.addField(TestDocument.class, "16", updateEntity, "code");

      // Verify ALL fields are preserved
      final TestDocument retrievedDoc = searchService.get("16", TestDocument.class);
      assertNotNull(retrievedDoc, "Document should be retrievable after addField");
      assertEquals("16000_updated", retrievedDoc.getCode(), "Code should have the updated value");
      assertEquals("sixteen", retrievedDoc.getName(), "Name should be preserved");
      assertEquals("sixteen description", retrievedDoc.getDescription(),
          "Description should be preserved");
      assertEquals("16", retrievedDoc.getId(), "Id should be preserved");

      logger.info("Successfully tested add field preserves all existing fields: {}", retrievedDoc);

    } catch (final Exception e) {
      fail("Error testing add field preserves all existing fields: " + e.getMessage());
    }
  }

}
