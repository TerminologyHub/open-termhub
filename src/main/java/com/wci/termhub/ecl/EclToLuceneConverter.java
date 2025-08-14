/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.ecl;

import com.wci.termhub.lucene.LuceneEclDataAccess;
import com.wci.termhub.model.Concept;
import com.wci.termhub.util.PropertyUtility;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class EclToLuceneConverter {
  /**
   * The logger.
   */
  private static Logger logger = LoggerFactory.getLogger(EclToLuceneConverter.class);

  private Query eclQuery = null;

  private LuceneEclDataAccess luceneEclDataAccess = null;

  private String indexRootDirectory =
      PropertyUtility.getProperties().getProperty("lucene.index.directory");

  public EclToLuceneConverter() {
    if(indexRootDirectory == null || indexRootDirectory.isEmpty()) {
      indexRootDirectory =
          System.getProperty("lucene.index.directory");
    }
    final String conceptIndex = indexRootDirectory + "/com.wci.termhub.model.Concept";
    final String conceptRelationshipIndex =
        indexRootDirectory + "/com.wci.termhub.model.ConceptRelationship";
    this.luceneEclDataAccess = new LuceneEclDataAccess(conceptIndex, conceptRelationshipIndex);
  }

  /**
   * Parse.
   *
   * @param ecQuery the ec query
   * @return the string
   * @throws Exception the exception
   */
  public Query parse(final String ecQuery) throws Exception {
    if (ExpressionConstraintListener.ALL_CONCEPTS.equals(ecQuery)) {
      throw new RuntimeException(
          String.format("Query %s would match too many concepts. Please refine", ecQuery));
    }
    final ECLLexer lexer = new ECLLexer(CharStreams.fromString(ecQuery));
    final CommonTokenStream tokens = new CommonTokenStream(lexer);
    final ECLParser parser = new ECLParser(tokens);
    final List<RecognitionException> exceptions = new ArrayList<>();
    parser.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol,
        final int line, final int charPositionInLine, final String msg,
        final RecognitionException e) {
        if (ECLParser.RULE_dottedexpressionconstraint != e.getCtx().getRuleIndex()) {
          throw new RuntimeException(String.format("Syntax error at line %s, character %s: %s",
              line, charPositionInLine, msg.replace("extraneous input", "unexpected character")));
        }
      }
    });
    final ECLParser.ExpressionconstraintContext tree = parser.expressionconstraint();

    final ParseTreeWalker walker = new ParseTreeWalker();
    final ExpressionConstraintListener listener =
        new ExpressionConstraintListener(luceneEclDataAccess);
    walker.walk(listener, tree);
    if (exceptions.isEmpty()) {
      final Map<String, Query> queries = listener.getQueries();
      final Query query = queries.get(ecQuery);
      return query;
    } else {
      final RecognitionException recognitionException = exceptions.get(0);
      throw recognitionException;
    }
  }

  public List<Concept> getConcepts(final Query query) throws Exception {
    if (query != null) {
      final List<Concept> concepts = luceneEclDataAccess.getConcepts(query);
      return concepts;
    }
    return null;
  }

  public record ParserResult(
      LinkedHashSet<ExpressionConstraintListener.EclConceptQuery> msearchQueries,
      Map<String, List<String>> results) {
  }
}
