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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DoubleValues;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.store.FSDirectory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;

/**
 * The Class LuceneEclDataAccess.
 */
public class LuceneEclDataAccess {

  /** The index directory. */
  private final String indexDirectory;

  /** The relationship index directory. */
  private String relationshipIndexDirectory;

  /**
   * Instantiates a new lucene ecl data access.
   *
   * @param indexDirectory the index directory
   */
  public LuceneEclDataAccess(final String indexDirectory) {
    this.indexDirectory = indexDirectory;
  }

  /**
   * Instantiates a new lucene ecl data access.
   *
   * @param indexDirectory the index directory
   * @param relationshipIndexDirectory the relationship index directory
   */
  public LuceneEclDataAccess(final String indexDirectory, final String relationshipIndexDirectory) {
    this.indexDirectory = indexDirectory;
    this.relationshipIndexDirectory = relationshipIndexDirectory;
  }

  /**
   * Gets the refinement query.
   *
   * @param fromQuery the from query
   * @param toQuery the to query
   * @param additionalTypeQuery the additional type query
   * @return the refinement query
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Query getRefinementQuery(Query fromQuery, Query toQuery, Query additionalTypeQuery) throws IOException {
    try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDirectory))); DirectoryReader relationshipReader = DirectoryReader.open(FSDirectory.open(Paths.get(relationshipIndexDirectory)))
    ) {
      IndexSearcher searcher = new IndexSearcher(new MultiReader(reader, relationshipReader));
      Query conceptJoinFromQuery = null;
      if (fromQuery != null) {
        conceptJoinFromQuery = JoinUtil.createJoinQuery(
                "code",
                false,
                "from.code",
                fromQuery,
                searcher,
                ScoreMode.None);
      } else {
        conceptJoinFromQuery = new TermQuery(new Term("from.code", "*"));
      }
      Query conceptJoinToQuery = null;
      if (toQuery != null) {
        if (toQuery.toString().contains("toValue")) {
          conceptJoinToQuery = toQuery;
        } else {
          conceptJoinToQuery = JoinUtil.createJoinQuery(
                  "code",
                  false,
                  "to.code",
                  toQuery,
                  searcher,
                  ScoreMode.None);
        }
      } else {
        conceptJoinToQuery = new TermQuery(new Term("code", "*"));
      }
      // additionalType queries are different.
      // The reason being that not all terminologies have the additionalType/relationType as a code in the Concept index.
      // So there is nothing to join on.
      // However, we still need to support this for Snomed where the additionalType can be a complex expression.
      // Any additionalType that is not a TermQuery needs to join with the Concept index.
      if (additionalTypeQuery == null) {
        additionalTypeQuery = new TermQuery(new Term("additionalType", "*"));
      } else if (!(additionalTypeQuery instanceof TermQuery) || !((TermQuery) additionalTypeQuery).getTerm().field().equals("additionalType")) {
        // Anything more than a TermQuery needs to join with the Concept index
        additionalTypeQuery = JoinUtil.createJoinQuery(
                "code",
                false,
                "additionalType",
                additionalTypeQuery,
                searcher,
                ScoreMode.None);
      }
      FilteredQuery filteredQuery = new FilteredQuery(searcher.rewrite(additionalTypeQuery), searcher.rewrite(conceptJoinToQuery));
      FilteredQuery filteredQuery2 = fromQuery != null ? new FilteredQuery(searcher.rewrite(conceptJoinFromQuery), filteredQuery) : null;
      return JoinUtil.createJoinQuery("from.code", false, "code", filteredQuery2 != null ? filteredQuery2 : filteredQuery, searcher, ScoreMode.None);
    }
  }

  /**
   * Gets the concepts.
   *
   * @param query the query
   * @return the concepts
   */
  public List<Concept> getConcepts(final Query query) {
    final List<Concept> concepts = new ArrayList<>();
    try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDirectory)));
        DirectoryReader relationshipReader =
            DirectoryReader.open(FSDirectory.open(Paths.get(relationshipIndexDirectory)))) {
      final MultiReader multiReader = new MultiReader(reader, relationshipReader);
      final IndexSearcher fromSearcher = new IndexSearcher(multiReader);
      final TopDocs docs = fromSearcher.search(query, 300000);
      for (final ScoreDoc scoreDoc : docs.scoreDocs) {
        final Document doc = fromSearcher.doc(scoreDoc.doc);
        String code = getCodeFromEntity(doc.get("entity"));
        Concept concept = null;
        if (code != null) {
          final List<ConceptRef> children = getConceptRefs(doc.getValues("children"));
          final List<ConceptRef> parents = getConceptRefs(doc.getValues("parents"));
          final List<ConceptRef> ancestors = getConceptRefs(doc.getValues("ancestors"));
          final List<ConceptRef> descendants = getConceptRefs(doc.getValues("descendants"));
          concept = createConceptWithRefs(code, parents, ancestors, children, descendants, null);
        } else {
          code = doc.get("fromCode");
          concept = new Concept();
          concept.setCode(code);
        }
        if (!concepts.contains(concept)) {
          concepts.add(concept);
        }
      }
      return concepts;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets the code from entity.
   *
   * @param entity the entity
   * @return the code from entity
   * @throws JsonProcessingException the json processing exception
   */
  private String getCodeFromEntity(final String entity) throws JsonProcessingException {
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode node = mapper.readTree(entity);
    return node.get("code").textValue();
  }

  public static Query getOrQuery(List<String> conceptCodes) {
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for (String conceptCode : conceptCodes) {
      TermQuery query = new TermQuery(new Term("code", conceptCode));
      builder.add(query, BooleanClause.Occur.SHOULD);
    }
    return builder.build();
  }

  /**
   * The Class MultiQueryDoubleValuesSource.
   */
  public class MultiQueryDoubleValuesSource extends DoubleValuesSource {

    /** The query. */
    private final Query query;

    /**
     * Instantiates a new multi query double values source.
     *
     * @param query the query
     */
    public MultiQueryDoubleValuesSource(final Query query) {
      this.query = query;
    }

    /**
     * Gets the values.
     *
     * @param ctx the ctx
     * @param scores the scores
     * @return the values
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public DoubleValues getValues(final LeafReaderContext ctx, final DoubleValues scores)
      throws IOException {
      final DoubleValuesSource queryDoubleValuesSource = DoubleValuesSource.fromQuery(this.query);
      return null;
    }

    /**
     * Needs scores.
     *
     * @return true, if successful
     */
    @Override
    public boolean needsScores() {
      return false;
    }

    /**
     * Rewrite.
     *
     * @param reader the reader
     * @return the double values source
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public DoubleValuesSource rewrite(final IndexSearcher reader) throws IOException {
      final DoubleValuesSource queryDoubleValuesSource = DoubleValuesSource.fromQuery(this.query);
      return queryDoubleValuesSource.rewrite(reader);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
      return query != null ? query.hashCode() : 0;
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
      final MultiQueryDoubleValuesSource that = (MultiQueryDoubleValuesSource) obj;
      return query != null ? query.equals(that.query) : that.query == null;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
      return "";
    }

    /**
     * Checks if is cacheable.
     *
     * @param ctx the ctx
     * @return true, if is cacheable
     */
    @Override
    public boolean isCacheable(final LeafReaderContext ctx) {
      return false;
    }
  }

  /**
   * Gets the concept ref.
   *
   * @param code the code
   * @return the concept ref
   */
  private ConceptRef getConceptRef(final String code) {
    return new ConceptRef(code, null);
  }

  /**
   * Gets the concept refs.
   *
   * @param codes the codes
   * @return the concept refs
   */
  private List<ConceptRef> getConceptRefs(final String[] codes) {
    return Arrays.stream(codes).map(this::getConceptRef).toList();
  }

  /**
   * Creates the concept with refs.
   *
   * @param code the code
   * @param parents the parents
   * @param ancestors the ancestors
   * @param children the children
   * @param descendants the descendants
   * @param relationships the relationships
   * @return the concept
   */
  private static Concept createConceptWithRefs(final String code, final List<ConceptRef> parents,
    final List<ConceptRef> ancestors, final List<ConceptRef> children,
    final List<ConceptRef> descendants, final List<ConceptRelationship> relationships) {
    final Concept concept = new Concept();
    concept.setCode(code);
    concept.setId(code);
    concept.setChildren(children);
    concept.setParents(parents);
    concept.setAncestors(ancestors);
    concept.setDescendants(descendants);
    concept.setRelationships(relationships);
    return concept;
  }
}
