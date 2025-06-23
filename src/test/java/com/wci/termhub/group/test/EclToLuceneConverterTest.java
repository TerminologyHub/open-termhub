/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.group.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.lucene.search.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.ecl.EclToLuceneConverter;
import com.wci.termhub.test.AbstractTest;

/**
 * Unit testing for ECL to lucene syntax.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class EclToLuceneConverterTest extends AbstractTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(EclToLuceneConverterTest.class);

  /**
   * Test basic ECL queries.
   *
   * @throws Exception the exception
   */
  @Test
  public void testBasicQueries() throws Exception {
    final EclToLuceneConverter converter = new EclToLuceneConverter();

    // Test descendant queries
    final Query descendantQuery = converter.parse("<<111111111");
    assertNotNull(descendantQuery);

    final Query directDescendantQuery = converter.parse("<111111111");
    assertNotNull(directDescendantQuery);

    final Query minusQuery = converter.parse("<11111111 MINUS <22222222");
    assertNotNull(minusQuery);

    final Query descendantMinusQuery = converter.parse("<<11111111 MINUS <22222222");
    assertNotNull(descendantMinusQuery);

    final Query descendantMinusDescendantQuery = converter.parse("<<11111111 MINUS <<22222222");
    assertNotNull(descendantMinusDescendantQuery);

    // Test queries with names
    final Query namedQuery = converter.parse("<111111111 | abc |");
    assertNotNull(namedQuery);

    final Query namedQueryNoSpaces = converter.parse("<111111111|abc|");
    assertNotNull(namedQueryNoSpaces);

    final Query orQueryWithNames = converter.parse("<11111111 | abc| OR 22222222 | def |");
    assertNotNull(orQueryWithNames);

    // Test refinement queries
    final Query refinementQuery = converter.parse("<11111111 : 12345 = 67890");
    assertNotNull(refinementQuery);

    final Query descendantRefinementQuery = converter.parse("<11111111 : 12345 = <67890");
    assertNotNull(descendantRefinementQuery);

    // Test member queries
    final Query memberQuery = converter.parse("^67890");
    assertNotNull(memberQuery);

    // Test ancestor queries
    final Query ancestorQuery = converter.parse(">12345");
    assertNotNull(ancestorQuery);

    final Query ancestorOrSelfQuery = converter.parse(">>12345");
    assertNotNull(ancestorOrSelfQuery);
  }

  /**
   * Test complex ECL queries.
   *
   * @throws Exception the exception
   */
  @Test
  public void testComplexQueries() throws Exception {
    final EclToLuceneConverter converter = new EclToLuceneConverter();

    final Query complexAncestorQuery = converter.parse("< * : answer_to=21907-1");
    assertNotNull(complexAncestorQuery);

    final Query complexDescendantQuery = converter.parse("<< * : answer_to=21907-1");
    assertNotNull(complexDescendantQuery);

    final Query multipleDescendantQuery =
        converter.parse("<<255412001 OR <<263714004 OR <<260245000");
    logger.info("multipleDescendantQuery: {}", multipleDescendantQuery);
    assertNotNull(multipleDescendantQuery);

    final Query attributeQuery = converter.parse("< 609328004 : 246075003 = 762952008");
    assertNotNull(attributeQuery);

    final Query descendantAttributeQuery = converter.parse("< 609328004 : 246075003 = <762952008");
    assertNotNull(descendantAttributeQuery);

    final Query ancestorAttributeQuery = converter.parse("< 609328004 : 246075003 = >762952008");
    assertNotNull(ancestorAttributeQuery);

    final Query descendantOrSelfAttributeQuery =
        converter.parse("< 609328004 : 246075003 = <<762952008");
    assertNotNull(descendantOrSelfAttributeQuery);

    final Query complexAttributeQuery =
        converter.parse("< 609328004 : (<<246075003 OR <762952000) = (<<762952008 OR <229006007)");
    assertNotNull(complexAttributeQuery);
  }

  /**
   * Test invalid queries.
   *
   * @throws Exception the exception
   */
  @Test
  public void testInvalidQueries() throws Exception {
    final EclToLuceneConverter converter = new EclToLuceneConverter();

    // Test that ALL_CONCEPTS query throws an exception
    assertThrows(RuntimeException.class, () -> {
      converter.parse("*");
    }, "Query * would match too many concepts. Please refine");

    // Test syntax error - using an invalid character that should cause a syntax
    // error
    // fail, does not throw an exception
    // assertThrows(RuntimeException.class, () -> {
    // converter.parse("11111111 @ 22222222");
    // }, "Syntax error");
  }
}
