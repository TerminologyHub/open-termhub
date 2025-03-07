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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.util.TerminologyUtility;

/**
 * Unit testing for TerminologyUtility.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TerminologyUtilityTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(TerminologyUtilityTest.class);

  /**
   * Test is sty expression.
   */
  @Test
  public void testIsStyExpression() {
    assertTrue(TerminologyUtility.isStyExpression("procedure"));
    assertTrue(TerminologyUtility.isStyExpression("procedure OR disorder"));
    assertTrue(TerminologyUtility.isStyExpression("body part OR disorder"));
    assertTrue(TerminologyUtility.isStyExpression("procedure  OR  disorder"));
    assertTrue(TerminologyUtility.isStyExpression("\"procedure\" OR disorder"));
    assertTrue(TerminologyUtility.isStyExpression("\"procedure\" OR \"disorder\""));
    assertTrue(TerminologyUtility.isStyExpression("\"body part\" OR \"disorder\""));
    assertTrue(TerminologyUtility.isStyExpression("procedure OR \"disorder\""));
    assertFalse(TerminologyUtility.isStyExpression("procedure OR disorder\""));
    assertFalse(TerminologyUtility.isStyExpression("\"procedure OR disorder\""));

    assertTrue(TerminologyUtility.isStyExpression("procedure;disorder"));
    assertTrue(TerminologyUtility.isStyExpression("body part;disorder"));
    assertTrue(TerminologyUtility.isStyExpression("procedure ; disorder"));
    assertTrue(TerminologyUtility.isStyExpression("\"procedure\";disorder"));
    assertTrue(TerminologyUtility.isStyExpression("\"procedure\";\"disorder\""));
    assertTrue(TerminologyUtility.isStyExpression("\"body part\";\"disorder\""));
    assertTrue(TerminologyUtility.isStyExpression("procedure;\"disorder\""));
    assertFalse(TerminologyUtility.isStyExpression("procedure;disorder\""));
    assertFalse(TerminologyUtility.isStyExpression("\"procedure;disorder\""));

    assertTrue(TerminologyUtility.isStyExpression("Random Value With Capitalized First Letters"));
    assertFalse(TerminologyUtility.isStyExpression("<<182340101 OR <MTHU12345"));
    assertFalse(TerminologyUtility.isStyExpression("<<1823-1"));
    assertFalse(
        TerminologyUtility.isStyExpression("random value with TWo capital letters back to back"));
  }

  /**
   * Test get sty expression.
   */
  @Test
  public void testGetStysExpression() {
    assertEquals("semanticTypes:procedure", TerminologyUtility.getStyClause("procedure"));
    assertEquals("semanticTypes:\"body structure\"",
        TerminologyUtility.getStyClause("body structure"));
    assertEquals("semanticTypes:\"body structure\"",
        TerminologyUtility.getStyClause("\"body structure\""));
    assertEquals("(semanticTypes:procedure OR semanticTypes:disorder)",
        TerminologyUtility.getStyClause("procedure OR disorder"));

    assertEquals("(semanticTypes:\"body part\" OR semanticTypes:disorder)",
        TerminologyUtility.getStyClause("body part OR disorder"));
    assertEquals("(semanticTypes:procedure OR semanticTypes:disorder)",
        TerminologyUtility.getStyClause("procedure  OR  disorder"));
    assertEquals("(semanticTypes:\"procedure\" OR semanticTypes:disorder)",
        TerminologyUtility.getStyClause("\"procedure\" OR disorder"));
    assertEquals("(semanticTypes:\"procedure\" OR semanticTypes:\"disorder\")",
        TerminologyUtility.getStyClause("\"procedure\" OR \"disorder\""));
    assertEquals("(semanticTypes:\"body part\" OR semanticTypes:\"disorder\")",
        TerminologyUtility.getStyClause("\"body part\" OR \"disorder\""));
    assertEquals("(semanticTypes:procedure OR semanticTypes:\"disorder\")",
        TerminologyUtility.getStyClause("procedure OR \"disorder\""));
    assertEquals("(semanticTypes:procedure OR semanticTypes:disorder)",
        TerminologyUtility.getStyClause("procedure;disorder"));
    assertEquals("(semanticTypes:\"body part\" OR semanticTypes:disorder)",
        TerminologyUtility.getStyClause("body part;disorder"));
    assertEquals("(semanticTypes:procedure OR semanticTypes:disorder)",
        TerminologyUtility.getStyClause("procedure ; disorder"));
    assertEquals("(semanticTypes:\"procedure\" OR semanticTypes:disorder)",
        TerminologyUtility.getStyClause("\"procedure\";disorder"));
    assertEquals("(semanticTypes:\"procedure\" OR semanticTypes:\"disorder\")",
        TerminologyUtility.getStyClause("\"procedure\";\"disorder\""));
    assertEquals("(semanticTypes:\"body part\" OR semanticTypes:\"disorder\")",
        TerminologyUtility.getStyClause("\"body part\";\"disorder\""));
    assertEquals("(semanticTypes:procedure OR semanticTypes:\"disorder\")",
        TerminologyUtility.getStyClause("procedure;\"disorder\""));
    assertEquals("semanticTypes:\"Random Value With Capitalized First Letters\"",
        TerminologyUtility.getStyClause("Random Value With Capitalized First Letters"));

  }

}
