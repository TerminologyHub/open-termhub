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

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TwoPhaseIterator;
import org.apache.lucene.search.Weight;

import java.io.IOException;

public abstract class FilterScorer extends Scorer {
    protected final Scorer in;
    protected final DocIdSetIterator iterator;

    /**
     * Create a new FilterScorer
     * @param in the {@link Scorer} to wrap
     */
    public FilterScorer(Scorer in, DocIdSetIterator iterator) {
        super(in.getWeight());
        this.in = in;
        this.iterator = iterator;
    }

    /**
     * Create a new FilterScorer with a specific weight
     * @param in the {@link Scorer} to wrap
     * @param weight a {@link Weight}
     */
    public FilterScorer(Scorer in, Weight weight, DocIdSetIterator iterator) {
        super(weight);
        if (in == null) {
            throw new NullPointerException("wrapped Scorer must not be null");
        }
        this.in = in;
        this.iterator = iterator;
    }

    @Override
    public float score() throws IOException {
        return in.score();
    }

    // Leave maxScore abstract on purpose since the goal of this Filter class is
    // to change the way the score is computed.

    @Override
    public final int docID() {
        return in.docID();
    }

    @Override
    public final DocIdSetIterator iterator() {
        return iterator;
    }

    @Override
    public final TwoPhaseIterator twoPhaseIterator() {
        return in.twoPhaseIterator();
    }
}
