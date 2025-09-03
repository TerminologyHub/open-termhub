/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Sort;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.util.IndexUtility;

/**
 * Unit tests for {@link IndexUtility}.
 */
public class IndexUtilityUnitTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(IndexUtilityUnitTest.class);

  /**
   * Test class with annotated fields for testing.
   */
  private static class TestModel {

    /** The text field. */
    @Field(type = FieldType.Text)
    private final String textField;

    /** The keyword field. */
    @Field(type = FieldType.Keyword)
    private final String keywordField;

    /** The date field. */
    @Field(type = FieldType.Date)
    private final Date dateField;

    /** The int field. */
    @Field(type = FieldType.Integer)
    private final Integer intField;

    /** The string list. */
    @Field(type = FieldType.Object)
    private final List<String> stringList;

    /**
     * Instantiates a new test model.
     */
    public TestModel() {
      textField = "test text";
      keywordField = "test keyword";
      dateField = new Date();
      intField = 123;
      stringList = Arrays.asList("item1", "item2");
    }
  }

  /**
   * Test getting indexable fields from a collection of strings.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetIndexableFieldsFromStringCollection() throws Exception {
    final Collection<String> collection = Arrays.asList("test1", "test2");
    final java.lang.reflect.Field field = TestModel.class.getDeclaredField("stringList");

    final List<IndexableField> fields = IndexUtility.getIndexableFields(collection, field);

    assertNotNull(fields);
    assertEquals(2, fields.size());
    assertTrue(fields.stream().allMatch(f -> f instanceof StringField));
  }

  /**
   * Test getting indexable fields from an object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetIndexableFieldsFromObject() throws Exception {
    final TestModel model = new TestModel();
    final java.lang.reflect.Field field = TestModel.class.getDeclaredField("textField");

    final List<IndexableField> fields =
        IndexUtility.getIndexableFields(model, field, "test", false);

    assertNotNull(fields);
    assertTrue(fields.size() >= 1);
    assertTrue(fields.stream().anyMatch(f -> f instanceof TextField));
  }

  /**
   * Test getting default sort order.
   */
  @Test
  public void testGetDefaultSortOrder() {
    final Sort sort = IndexUtility.getDefaultSortOrder(TestModel.class);

    assertNotNull(sort);
    assertTrue(sort.getSort().length > 0);
  }

  /**
   * Test getting sort order from search parameters.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSortOrder() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setSort(Arrays.asList("textField"));
    params.setAscending(true);

    final Sort sort = IndexUtility.getSortOrder(params, TestModel.class);

    assertNotNull(sort);
    assertEquals(1, sort.getSort().length);
    assertEquals("textField", sort.getSort()[0].getField());
  }

  /**
   * Test field mapping - .keyword suffix mapping.
   *
   * @throws Exception the exception
   */
  @Test
  public void testKeywordFieldMapping() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setSort(Arrays.asList("name.keyword"));
    params.setAscending(true);

    final Sort sort = IndexUtility.getSortOrder(params, Terminology.class);

    assertNotNull(sort);
    assertEquals(1, sort.getSort().length);
    assertEquals("name.keyword", sort.getSort()[0].getField());
  }

  /**
   * Test field mapping - .ngram suffix mapping.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNgramFieldMapping() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setSort(Arrays.asList("name.ngram"));
    params.setAscending(true);

    final Sort sort = IndexUtility.getSortOrder(params, Terminology.class);

    assertNotNull(sort);
    assertEquals(1, sort.getSort().length);
    assertEquals("name.ngram", sort.getSort()[0].getField());
  }

  /**
   * Test field mapping - nested field with .keyword suffix. Note: This test
   * will fail because Terminology class doesn't have 'to' field. This is
   * expected behavior - the test validates error handling.
   */
  @Test
  public void testNestedKeywordFieldMapping() {
    final SearchParameters params = new SearchParameters();
    params.setSort(Arrays.asList("to.name.keyword"));
    params.setAscending(true);

    // This should throw NoSuchFieldException because 'to' field doesn't exist
    // in Terminology
    assertThrows(Exception.class, () -> {
      IndexUtility.getSortOrder(params, Terminology.class);
    });
  }

  /**
   * Test field mapping - multiple field mappings.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMultipleFieldMappings() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setSort(Arrays.asList("name.keyword", "abbreviation", "version.keyword"));
    params.setAscending(true);

    final Sort sort = IndexUtility.getSortOrder(params, Terminology.class);

    assertNotNull(sort);
    assertEquals(3, sort.getSort().length);
    assertEquals("name.keyword", sort.getSort()[0].getField());
    assertEquals("abbreviation", sort.getSort()[1].getField());
    assertEquals("version.keyword", sort.getSort()[2].getField());
  }

  /**
   * Test field mapping - mixed .keyword and .ngram suffixes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMixedSuffixMappings() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setSort(Arrays.asList("name.keyword", "name.ngram", "abbreviation"));
    params.setAscending(true);

    final Sort sort = IndexUtility.getSortOrder(params, Terminology.class);

    assertNotNull(sort);
    assertEquals(3, sort.getSort().length);
    assertEquals("name.keyword", sort.getSort()[0].getField());
    assertEquals("name.ngram", sort.getSort()[1].getField());
    assertEquals("abbreviation", sort.getSort()[2].getField());
  }

  /**
   * Test field mapping - complex nested field with .keyword. Note: This test
   * will fail because Terminology class doesn't have 'concept' field. This is
   * expected behavior - the test validates error handling.
   */
  @Test
  public void testComplexNestedFieldMapping() {
    final SearchParameters params = new SearchParameters();
    params.setSort(Arrays.asList("concept.name.keyword"));
    params.setAscending(true);

    // This should throw NoSuchFieldException because 'concept' field doesn't
    // exist in Terminology
    assertThrows(Exception.class, () -> {
      IndexUtility.getSortOrder(params, Terminology.class);
    });
  }

  /**
   * Test field mapping - field that doesn't exist should throw exception.
   */
  @Test
  public void testFieldMappingNonExistentField() {
    final SearchParameters params = new SearchParameters();
    params.setSort(Arrays.asList("nonExistentField.keyword"));
    params.setAscending(true);

    try {
      IndexUtility.getSortOrder(params, Terminology.class);
      // If we get here, the test should fail
      assertTrue(false, "Expected NoSuchFieldException for non-existent field");
    } catch (final NoSuchFieldException e) {
      // Expected exception
      assertTrue(e.getMessage().contains("nonExistentField"));
      assertTrue(e.getMessage().contains("Terminology"));
    }
  }

  /**
   * Test field mapping - regular Java field (no suffix) still works.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRegularJavaFieldStillWorks() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setSort(Arrays.asList("abbreviation"));
    params.setAscending(true);

    final Sort sort = IndexUtility.getSortOrder(params, Terminology.class);

    assertNotNull(sort);
    assertEquals(1, sort.getSort().length);
    assertEquals("abbreviation", sort.getSort()[0].getField());
  }

  /**
   * Test field mapping - field from superclass works.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFieldMappingFromSuperclass() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setSort(Arrays.asList("name.keyword"));
    params.setAscending(true);

    // Terminology extends TerminologyRef, and 'name' field is in TerminologyRef
    final Sort sort = IndexUtility.getSortOrder(params, Terminology.class);

    assertNotNull(sort);
    assertEquals(1, sort.getSort().length);
    assertEquals("name.keyword", sort.getSort()[0].getField());
  }

  /**
   * Test field mapping - descending sort order.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFieldMappingDescendingSort() throws Exception {
    final SearchParameters params = new SearchParameters();
    params.setSort(Arrays.asList("name.keyword"));
    params.setAscending(false);

    final Sort sort = IndexUtility.getSortOrder(params, Terminology.class);

    assertNotNull(sort);
    assertEquals(1, sort.getSort().length);
    assertEquals("name.keyword", sort.getSort()[0].getField());
    assertTrue(sort.getSort()[0].getReverse(), "Sort should be descending");
  }
}
