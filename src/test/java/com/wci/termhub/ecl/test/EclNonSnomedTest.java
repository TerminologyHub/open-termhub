/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.ecl.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.apache.lucene.search.Query;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.ecl.EclToLuceneConverter;
import com.wci.termhub.ecl.ExpressionConstraintListener;
import com.wci.termhub.lucene.LuceneEclDataAccess;
import com.wci.termhub.model.Concept;
import com.wci.termhub.util.PropertyUtility;

/**
 * The Class EclNonSnomedTest.
 */
@Disabled("The sandbox data does not have some of the concepts used in these tests.")
public class EclNonSnomedTest {

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(EclConverterTest.class);

  /** The index root directory. */
  private final String indexRootDirectory =
      PropertyUtility.getProperties().getProperty("lucene.index.directory");

  /** The lucene ecl data access. */
  private final LuceneEclDataAccess luceneEclDataAccess;

  /**
   * Instantiates a new ecl non snomed test.
   */
  public EclNonSnomedTest() {
    final String conceptIndex = indexRootDirectory + "/com.wci.termhub.model.Concept";
    final String conceptRelationshipIndex =
        indexRootDirectory + "/com.wci.termhub.model.ConceptRelationship";
    this.luceneEclDataAccess = new LuceneEclDataAccess(conceptIndex, conceptRelationshipIndex);
  }

  /**
   * Test lnc.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLnc() throws Exception {
    String expression = "<LP14855-8";
    List<String> luceneConcepts = handleExpressionWithLucene(expression);
    assertEquals(4, luceneConcepts.size());
    assertTrue(
        luceneConcepts.containsAll(List.of("LP417866-3", "LP417605-5", "LP418774-8", "LP16680-8")));
    expression = "* : has_component=LP73603-0";
    luceneConcepts = handleExpressionWithLucene(expression);
    assertEquals(1, luceneConcepts.size());
  }

  /**
   * Test icd 10.
   *
   * @throws Exception the exception
   */
  @Test
  public void testIcd10() throws Exception {
    String expression = "<<C50";
    List<String> concepts = handleExpressionWithLucene(expression);
    assertEquals(1, concepts.size());
    expression = "<E08-E13 MINUS E10";
    concepts = handleExpressionWithLucene(expression);
    assertEquals(3, concepts.size());
  }

  /**
   * Handle expression with lucene.
   *
   * @param expression the expression
   * @return the list
   * @throws Exception the exception
   */
  private List<String> handleExpressionWithLucene(final String expression) throws Exception {
    final EclToLuceneConverter converter = new EclToLuceneConverter();
    logger.info("Running {}", expression);
    final String cleansedExpression = ExpressionConstraintListener.removeComments(expression.trim());
    final Query query = converter.parse(cleansedExpression);
    final List<Concept> concepts = luceneEclDataAccess.getConcepts(query);
    if (concepts == null) {
      logger.error("Results are null. {} did not parse correctly", cleansedExpression);
    } else {
      logger.info("Number of concepts:{}", concepts.size());
    }
    return concepts != null ? concepts.stream().map(Concept::getCode).sorted().toList()
        : Collections.emptyList();
  }

}
