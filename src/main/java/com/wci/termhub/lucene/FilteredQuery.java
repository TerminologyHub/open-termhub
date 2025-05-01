/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.lucene;

import java.io.IOException;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryVisitor;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.BitSetIterator;
import org.apache.lucene.util.FixedBitSet;

/**
 * The Class FilteredQuery.
 */
public class FilteredQuery extends Query {

  /** The inner query. */
  private final Query innerQuery;

  /** The filter query. */
  private final Query filterQuery;

  /**
   * Instantiates a new filtered query.
   *
   * @param innerQuery the inner query
   * @param filterQuery the filter query
   */
  public FilteredQuery(final Query innerQuery, final Query filterQuery) {
    this.innerQuery = innerQuery;
    this.filterQuery = filterQuery;
  }

  /**
   * Creates the weight.
   *
   * @param searcher the searcher
   * @param scoreMode the score mode
   * @param boost the boost
   * @return the weight
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public Weight createWeight(final IndexSearcher searcher, final ScoreMode scoreMode,
    final float boost) throws IOException {
    final Weight innerWeight = innerQuery.createWeight(searcher, scoreMode, boost);
    final Weight filterWeight = filterQuery.createWeight(searcher, scoreMode, boost);
    return new FilteredWeight(innerWeight, filterWeight);
  }

  /**
   * Visit.
   *
   * @param queryVisitor the query visitor
   */
  @Override
  public void visit(final QueryVisitor queryVisitor) {

  }

  /**
   * To string.
   *
   * @param field the field
   * @return the string
   */
  @Override
  public String toString(final String field) {
    return String.format("Inner Query:%s FilterQuery: %s", innerQuery.toString(),
        filterQuery.toString());
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final FilteredQuery that = (FilteredQuery) obj;
    return innerQuery.equals(that.innerQuery) && filterQuery.equals(that.filterQuery);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    int result = innerQuery.hashCode();
    result = 31 * result + filterQuery.hashCode();
    return result;
  }

  /**
   * The Class FilteredWeight.
   */
  private static class FilteredWeight extends Weight {

    /** The inner weight. */
    private final Weight innerWeight;

    /** The filter weight. */
    private final Weight filterWeight;

    /**
     * Instantiates a new filtered weight.
     *
     * @param innerWeight the inner weight
     * @param filterWeight the filter weight
     */
    public FilteredWeight(final Weight innerWeight, final Weight filterWeight) {
      super(innerWeight.getQuery());
      this.innerWeight = innerWeight;
      this.filterWeight = filterWeight;
    }

    /**
     * Explain.
     *
     * @param context the context
     * @param docId the doc id
     * @return the explanation
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public Explanation explain(final LeafReaderContext context, final int docId)
      throws IOException {
      // ... (Implement explanation logic)
      return null;
    }

    /**
     * Scorer.
     *
     * @param context the context
     * @return the scorer
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public Scorer scorer(final LeafReaderContext context) throws IOException {
      final Scorer innerScorer = innerWeight.scorer(context);
      final Scorer filterScorer = filterWeight.scorer(context);
      if (innerScorer == null || filterScorer == null) {
        return null;
      }
      final FixedBitSet matchingDocIds = new FixedBitSet(context.reader().maxDoc());
      final DocIdSetIterator innerIterator = innerScorer.iterator();
      final DocIdSetIterator filterIterator = filterScorer.iterator();
      int docId1 = innerIterator.docID();
      int docId2 = filterIterator.nextDoc();

      while (docId1 != DocIdSetIterator.NO_MORE_DOCS && docId2 != DocIdSetIterator.NO_MORE_DOCS) {
        if (docId1 == docId2) {
          // Process the document with docId1 (or docId2)
          matchingDocIds.set(docId1);
          // Advance both iterators
          docId1 = innerIterator.nextDoc();
          docId2 = filterIterator.nextDoc();
        } else if (docId1 < docId2) {
          // Advance the first iterator
          docId1 = innerIterator.nextDoc();
        } else {
          // Advance the second iterator
          docId2 = filterIterator.nextDoc();
        }
      }
      final DocIdSetIterator iterator = new BitSetIterator(matchingDocIds, 1);

      return new FilterScorer(innerScorer, iterator) {
        @Override
        public float getMaxScore(final int upTo) throws IOException {
          return 0;
        }

        @Override
        public float score() throws IOException {
          return super.score();
        }
      };
    }

    /**
     * Checks if is cacheable.
     *
     * @param ctx the ctx
     * @return true, if is cacheable
     */
    @Override
    public boolean isCacheable(final LeafReaderContext ctx) {
      return innerWeight.isCacheable(ctx) && filterWeight.isCacheable(ctx);
    }
  }
}
