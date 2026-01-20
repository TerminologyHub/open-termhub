/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.lucene;

import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Analyzer type enum providing factory for fresh instances.
 */
public enum AnalyzerType {

  /** The standard. */
  STANDARD {
    @Override
    public Analyzer newInstance() {
      return new StandardAnalyzer();
    }
  },

  /** The keyword. */
  KEYWORD {
    @Override
    public Analyzer newInstance() {
      return new KeywordAnalyzer();
    }
  },

  /** The ngram. */
  NGRAM {
    @Override
    public Analyzer newInstance() {
      return new Analyzer() {
        @SuppressWarnings("resource")
        @Override
        protected TokenStreamComponents createComponents(final String fieldName) {
          final Tokenizer tokenizer = new StandardTokenizer();
          final TokenStream lower = new LowerCaseFilter(tokenizer);
          final TokenStream cleaned =
              new PatternReplaceFilter(lower, Pattern.compile("[^a-z0-9]+"), "", true);
          final TokenStream ngrams = new NGramTokenFilter(cleaned, 3, 20, false);
          return new TokenStreamComponents(tokenizer, ngrams);
        }
      };
    }
  };

  /**
   * New instance.
   *
   * @return the analyzer
   */
  public abstract Analyzer newInstance();
}
