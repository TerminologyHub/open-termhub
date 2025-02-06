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

import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.test.AbstractTest;

/**
 * Validates the norm utility.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ModelUtilityTest extends AbstractTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(ModelUtilityTest.class);

  // /**
  // * Test get sort field.
  // */
  // @Test
  // public void testGetSortField() {
  // assertEquals(ModelUtility.getSortField(Concept.class, "name"),
  // "name.keyword");
  // assertEquals(ModelUtility.getSortField(Concept.class, "terms.name"),
  // "terms.name.keyword");
  // assertEquals(ModelUtility.getSortField(Template.class, "code"), "code");
  // assertEquals(ModelUtility.getSortField(Template.class, "labels"),
  // "labels");
  // assertEquals(ModelUtility.getSortField(Template.class, "stringField"),
  // "stringField.keyword");
  // // call again to check cache works
  // assertEquals(ModelUtility.getSortField(Concept.class, "terms.name"),
  // "terms.name.keyword");
  // assertEquals(ModelUtility.getSortField(Concept.class, "name"),
  // "name.keyword");
  // assertEquals(ModelUtility.getSortField(Template.class, "code"), "code");
  // assertEquals(ModelUtility.getSortField(Template.class, "labels"),
  // "labels");
  // assertEquals(ModelUtility.getSortField(Template.class, "stringField"),
  // "stringField.keyword");
  // }
}
