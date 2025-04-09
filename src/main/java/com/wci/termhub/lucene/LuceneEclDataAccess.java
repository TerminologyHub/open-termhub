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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LuceneEclDataAccess {

    private String indexDirectory;
    private String relationshipIndexDirectory;

    public LuceneEclDataAccess(String indexDirectory) {
        this.indexDirectory = indexDirectory;
    }

    public LuceneEclDataAccess(String indexDirectory, String relationshipIndexDirectory) {
        this.indexDirectory = indexDirectory;
        this.relationshipIndexDirectory = relationshipIndexDirectory;
    }

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
            if (additionalTypeQuery == null) {
                additionalTypeQuery = new TermQuery(new Term("additionalType", "*"));
            } else if (!(additionalTypeQuery instanceof TermQuery)){
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

    public List<Concept> getConcepts(Query query) {
        List<Concept> concepts = new ArrayList<>();
        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDirectory)));
             DirectoryReader relationshipReader = DirectoryReader.open(FSDirectory.open(Paths.get(relationshipIndexDirectory)))) {
            MultiReader multiReader = new MultiReader(reader, relationshipReader);
            IndexSearcher fromSearcher = new IndexSearcher(multiReader);
            TopDocs docs = fromSearcher.search(query, 300000);
            for (ScoreDoc scoreDoc : docs.scoreDocs) {
                Document doc = fromSearcher.doc(scoreDoc.doc);
                String code = getCodeFromEntity(doc.get("entity"));
                Concept concept = null;
                if (code != null) {
                    List<ConceptRef> children = getConceptRefs(doc.getValues("children"));
                    List<ConceptRef> parents = getConceptRefs(doc.getValues("parents"));
                    List<ConceptRef> ancestors = getConceptRefs(doc.getValues("ancestors"));
                    List<ConceptRef> descendants = getConceptRefs(doc.getValues("descendants"));
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCodeFromEntity(String entity) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(entity);
        return node.get("code").textValue();
    }

    public void search() throws IOException {
        TermQuery termQuery = new TermQuery(new Term("ancestors", "72431002"));
        TermQuery termQuery2 = new TermQuery(new Term("additionalType", "has_component"));
        BooleanQuery booleanQuery = new BooleanQuery.Builder().add(termQuery, BooleanClause.Occur.SHOULD).add(termQuery2, BooleanClause.Occur.SHOULD).build();

        TermQuery termQuery3 = new TermQuery(new Term("code", "LP73603-0"));
        TermQuery termQuery4 = new TermQuery(new Term("ancestors", "410942007"));
        TermQuery termQuery5 = new TermQuery(new Term("code", "410942007"));
        BooleanQuery booleanQuery3 = new BooleanQuery.Builder().add(termQuery4, BooleanClause.Occur.SHOULD).add(termQuery5, BooleanClause.Occur.SHOULD).build();

        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDirectory)));
             DirectoryReader relationshipReader = DirectoryReader.open(FSDirectory.open(Paths.get(relationshipIndexDirectory)))) {
            MultiReader multiReader = new MultiReader(reader, relationshipReader);
            IndexSearcher relationshipSearcher = new IndexSearcher(multiReader);
            Query joinQuery = JoinUtil.createJoinQuery("codeJoin", false, "fromCode", booleanQuery, relationshipSearcher, ScoreMode.None);
            Query joinQuery2 = JoinUtil.createJoinQuery("code", false, "to.code", termQuery3, relationshipSearcher, ScoreMode.None);
            Query joinQuery3 = JoinUtil.createJoinQuery("code", false, "additionalType", termQuery2, relationshipSearcher, ScoreMode.None);
            FilteredQuery filteredQuery = new FilteredQuery(relationshipSearcher.rewrite(joinQuery2), relationshipSearcher.rewrite(joinQuery3));
            FilteredQuery filteredQuery2 = new FilteredQuery(relationshipSearcher.rewrite(joinQuery), filteredQuery);
            Query joinQuery4 = JoinUtil.createJoinQuery("fromCodeJoin", false, "code", filteredQuery2, relationshipSearcher, ScoreMode.None);
//        Query joinQuery6 = JoinUtil.createJoinQuery("toCodeJoin", false, "code", joinQuery3, relationshipSearcher, ScoreMode.None);
//        Query booleanQuery4 = new BooleanQuery.Builder().add(joinQuery6, BooleanClause.Occur.MUST).add(booleanQuery3, BooleanClause.Occur.MUST).build();
//        FunctionScoreQuery functionScoreQuery = new FunctionScoreQuery(joinQuery3, new MultiQueryDoubleValuesSource(joinQuery2));
//        FunctionScoreQuery functionScoreQuery2 = new FunctionScoreQuery(functionScoreQuery, new MultiQueryDoubleValuesSource(joinQuery));

            //Query joinQuery7 = JoinUtil.createJoinQuery("codeJoin", false, "toCode", joinQuery6, relationshipSearcher, ScoreMode.None);
//        Query joinQuery4 = JoinUtil.createJoinQuery("toCodeJoin", false, "code", joinQuery7, relationshipSearcher, ScoreMode.None);
//        Query booleanQuery4 = new BooleanQuery.Builder().add(joinQuery4, BooleanClause.Occur.MUST).add(booleanQuery3, BooleanClause.Occur.MUST).build();
            //Query joinQuery5 = JoinUtil.createJoinQuery("fromCodeJoin", false, "code", joinQuery2, relationshipSearcher, ScoreMode.None);

//        BooleanQuery booleanQuery2 = new BooleanQuery.Builder().add(joinQuery, BooleanClause.Occur.MUST).add(functionScoreQuery, BooleanClause.Occur.MUST).build();
            if (relationshipIndexDirectory == null) {
//            TopDocs docs = fromSearcher.search(booleanQuery2, 1000000);
//            System.out.println(docs.totalHits.value);
            } else {
                List<Concept> concepts = getConcepts(termQuery2);
                concepts.stream().map(Concept::getCode).forEach(System.out::println);
                System.out.println("Results:" + concepts.size());
            }
            //List<Concept> concepts = getConcepts(booleanQuery2);
            //concepts.stream().map(Concept::getCode).forEach(System.out::println);

        }
    }

    public class MultiQueryDoubleValuesSource extends DoubleValuesSource {

        private Query query;

        public MultiQueryDoubleValuesSource(Query query) {
            this.query = query;
        }

        @Override
        public DoubleValues getValues(LeafReaderContext ctx, DoubleValues scores) throws IOException {
            DoubleValuesSource queryDoubleValuesSource = DoubleValuesSource.fromQuery(this.query);
            return null;
        }

        @Override
        public boolean needsScores() {
            return false;
        }

        @Override
        public DoubleValuesSource rewrite(IndexSearcher reader) throws IOException {
            DoubleValuesSource queryDoubleValuesSource = DoubleValuesSource.fromQuery(this.query);
            return queryDoubleValuesSource.rewrite(reader);
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public boolean isCacheable(LeafReaderContext ctx) {
            return false;
        }
    }

    private ConceptRef getConceptRef(String code) {
        return new ConceptRef(code, null);
    }

    private List<ConceptRef> getConceptRefs(String[] codes) {
        return Arrays.stream(codes).map(this::getConceptRef).toList();
    }

    private static Concept createConceptWithRefs(String code, List<ConceptRef> parents, List<ConceptRef> ancestors, List<ConceptRef> children, List<ConceptRef> descendants, List<ConceptRelationship> relationships) {
        Concept concept = new Concept();
        concept.setCode(code);
        concept.setId(code);
        concept.setChildren(children);
        concept.setParents(parents);
        concept.setAncestors(ancestors);
        concept.setDescendants(descendants);
        concept.setRelationships(relationships);
        return concept;
    }


    public static void main(String[] args) throws IOException {
//        LuceneDao luceneDao = new LuceneDao("/Users/squareroot/wci/snomed/index/snomed");
        LuceneEclDataAccess luceneEclDataAccess = new LuceneEclDataAccess("/Users/squareroot/wci/snomed/open-termhub/index/com.wci.termhub.model.Concept", "/Users/squareroot/wci/snomed/open-termhub/index/com.wci.termhub.model.ConceptRelationship");
        luceneEclDataAccess.search();
    }
}
