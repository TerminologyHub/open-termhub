/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.lucene.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.jupiter.api.Test;

import com.wci.termhub.lucene.LuceneQueryBuilder;
import com.wci.termhub.model.Concept;

/**
 * Tests for field-to-analyzer mappings built by {@link LuceneQueryBuilder}.
 */
public class LuceneQueryBuilderAnalyzerTest {

  /**
   * Ensure top-level keyword subfield uses KeywordAnalyzer.
   */
  @Test
  public void testTopLevelKeywordAnalyzerMapping() {
    final Map<String, Analyzer> analyzers = LuceneQueryBuilder.getFieldAnalyzers(Concept.class);
    final Analyzer analyzer = analyzers.get("stemName.keyword");
    assertNotNull(analyzer);
    assertTrue(analyzer instanceof KeywordAnalyzer);
  }

  /**
   * Ensure nested keyword subfield uses KeywordAnalyzer.
   */
  @Test
  public void testNestedKeywordAnalyzerMapping() {
    final Map<String, Analyzer> analyzers = LuceneQueryBuilder.getFieldAnalyzers(Concept.class);
    final Analyzer analyzer = analyzers.get("terms.stemName.keyword");
    assertNotNull(analyzer);
    assertTrue(analyzer instanceof KeywordAnalyzer);
  }

  /**
   * Ensure nested text field and ngram subfield have analyzers registered.
   */
  @Test
  public void testNestedTextAndNgramAnalyzerMappings() {
    final Map<String, Analyzer> analyzers = LuceneQueryBuilder.getFieldAnalyzers(Concept.class);
    final Analyzer textAnalyzer = analyzers.get("terms.name");
    final Analyzer ngramAnalyzer = analyzers.get("terms.name.ngram");
    assertNotNull(textAnalyzer);
    assertTrue(textAnalyzer instanceof StandardAnalyzer);
    assertNotNull(ngramAnalyzer);
  }
}
