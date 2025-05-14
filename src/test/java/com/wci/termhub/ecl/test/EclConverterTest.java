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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Query;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wci.termhub.ecl.EclToLuceneConverter;
import com.wci.termhub.ecl.ExpressionConstraintListener;
import com.wci.termhub.lucene.LuceneEclDataAccess;
import com.wci.termhub.model.Concept;
import com.wci.termhub.util.PropertyUtility;

/**
 * Unit testing to for ECL to lucene syntax;.
 */
@Disabled("The sandbox data does not have some of the concepts used in these tests.")
public class EclConverterTest {

  /**
   * The logger.
   */
  private final Logger logger = LoggerFactory.getLogger(EclConverterTest.class);

  /** The expected results. */
  private static Map<String, SnomedEclResults> expectedResults;

  /** The index root directory. */
  private final String indexRootDirectory =
      PropertyUtility.getProperties().getProperty("lucene.index.directory");

  /** The lucene ecl data access. */
  private final LuceneEclDataAccess luceneEclDataAccess;

  /**
   * Instantiates a new ecl converter test.
   */
  public EclConverterTest() {
    final String conceptIndex = indexRootDirectory + "/com.wci.termhub.model.Concept";
    final String conceptRelationshipIndex =
        indexRootDirectory + "/com.wci.termhub.model.ConceptRelationship";
    this.luceneEclDataAccess = new LuceneEclDataAccess(conceptIndex, conceptRelationshipIndex);
  }

  /**
   * Before all.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  public static void beforeAll() throws Exception {
    try (InputStream inputStream =
        EclConverterTest.class.getResourceAsStream("/ecl/expected_results.json")) {
      expectedResults = new Gson().fromJson(new InputStreamReader(inputStream),
          new TypeToken<Map<String, SnomedEclResults>>() {
            // n/a
          }.getType());
    }
  }

  /**
   * Test ecl expressions from file.
   *
   * @param file the file
   * @throws Exception the exception
   */
  @ParameterizedTest
  @ValueSource(strings = {
      "/ecl/icd_11_scope_rules.txt", "/ecl/snomed_ecl_examples.txt",
      "/ecl/asterisk_multiple_clauses.txt"
  })
  public void testEclExpressionsFromFile(final String file) throws Exception {
    try (InputStream inputStream = this.getClass().getResourceAsStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      String expression;
      int lineNumber = 0;
      while ((expression = reader.readLine()) != null) {
        lineNumber++;
        if (expression.startsWith("#")) {
          continue;
        }
        logger.info("Working on line number:{}", lineNumber);
        logger.info("Testing Ecl expression:{}", expression);
        final List<String> luceneConcepts = handleExpressionWithElasticSearchWithLucene(expression);
        final SnomedEclResults expectedResult = expectedResults.get(expression);
        if (expectedResult != null) {
          assertEquals(expectedResult.getCount(), luceneConcepts.size());
          if (expectedResult.getConcepts() != null) {
            Collections.sort(expectedResult.getConcepts());
            assertEquals(expectedResult.getConcepts(), luceneConcepts);
          } else {
            logger.info("Expected concepts not provided for:{}", expression);
          }
        } else {
          logger.info("No expected results found for:{}", expression);
        }
      }
    }
  }

  /**
   * Handle expression with elastic search with lucene.
   *
   * @param expression the expression
   * @return the list
   * @throws Exception the exception
   */
  private List<String> handleExpressionWithElasticSearchWithLucene(final String expression)
    throws Exception {
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
