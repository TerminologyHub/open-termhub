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
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.util.IndexUtility;

/**
 * Unit tests for {@link IndexUtility}.
 */
public class IndexUtilityUnitTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(IndexUtilityUnitTest.class);

  /**
   * Test class with annotated fields for testing.
   */
  private static class TestModel {

    /** The text field. */
    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Text)
    private final String textField;

    /** The keyword field. */
    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Keyword)
    private final String keywordField;

    /** The date field. */
    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Date)
    private final Date dateField;

    /** The int field. */
    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Integer)
    private final Integer intField;

    /** The string list. */
    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Object)
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

    final List<IndexableField> fields = IndexUtility.getIndexableFields(model, field, "test", false);

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
}
