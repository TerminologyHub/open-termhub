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

import java.io.IOException;

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TwoPhaseIterator;
import org.apache.lucene.search.Weight;

/**
 * The Class FilterScorer.
 */
public abstract class FilterScorer extends Scorer {

  /** The in. */
  private final Scorer in;

  /** The iterator. */
  private final DocIdSetIterator iterator;

  /**
   * Create a new FilterScorer.
   *
   * @param in the {@link Scorer} to wrap
   * @param iterator the iterator
   */
  public FilterScorer(final Scorer in, final DocIdSetIterator iterator) {
    super(in.getWeight());
    this.in = in;
    this.iterator = iterator;
  }

  /**
   * Create a new FilterScorer with a specific weight.
   *
   * @param in the {@link Scorer} to wrap
   * @param weight a {@link Weight}
   * @param iterator the iterator
   */
  public FilterScorer(final Scorer in, final Weight weight, final DocIdSetIterator iterator) {
    super(weight);
    if (in == null) {
      throw new NullPointerException("wrapped Scorer must not be null");
    }
    this.in = in;
    this.iterator = iterator;
  }

  /**
   * Score.
   *
   * @return the float
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public float score() throws IOException {
    return in.score();
  }

  // Leave maxScore abstract on purpose since the goal of this Filter class is
  // to change the way the score is computed.

  /**
   * Doc ID.
   *
   * @return the int
   */
  @Override
  public final int docID() {
    return in.docID();
  }

  /**
   * Iterator.
   *
   * @return the doc id set iterator
   */
  @Override
  public final DocIdSetIterator iterator() {
    return iterator;
  }

  /**
   * Two phase iterator.
   *
   * @return the two phase iterator
   */
  @Override
  public final TwoPhaseIterator twoPhaseIterator() {
    return in.twoPhaseIterator();
  }
}
