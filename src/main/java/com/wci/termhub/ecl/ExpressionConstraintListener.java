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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.wci.termhub.lucene.LuceneEclDataAccess;

/**
 * The listener interface for receiving expressionConstraint events. The class that is interested in
 * processing a expressionConstraint event implements this interface, and the object created with
 * that class is registered with a component using the component's
 * <code>addExpressionConstraintListener</code> method. When the expressionConstraint event occurs,
 * that object's appropriate method is invoked.
 *
 * @see ExpressionConstraintEvent
 */
public class ExpressionConstraintListener extends EclLogListener {

  /** The Constant ALL_CONCEPTS. */
  public static final String ALL_CONCEPTS = "*";

  /** The current sub expression. */
  private String currentSubExpression = null;

  /** The lucene ecl data access. */
  private LuceneEclDataAccess luceneEclDataAccess;

  /** The current ecl attributes. */
  private List<String> currentEclAttributes = new ArrayList<>();

  /** The is refinment. */
  private boolean isRefinment = false;

  /** The current refinement sub expression. */
  private String currentRefinementSubExpression = null;

  /**
   * Instantiates a new expression constraint listener.
   *
   * @param luceneEclDataAccess the lucene ecl data access
   */
  public ExpressionConstraintListener(final LuceneEclDataAccess luceneEclDataAccess) {
    this.luceneEclDataAccess = luceneEclDataAccess;
  }

  /** The queries. */
  private Map<String, Query> queries = new HashMap<>();

  /* see superclass */
  @Override
  public void enterSubexpressionconstraint(final ECLParser.SubexpressionconstraintContext ctx) {
    String text = ctx.getText();

    if (ctx.historysupplement() != null) {
      text = text.replace(ctx.historysupplement().getText(), "").trim();
    }

    final ECLParser.EclfocusconceptContext focusconcept = ctx.eclfocusconcept();
    if (focusconcept == null) {
      // logger.warn("Unable to handle this condition");
      return;
    }

    final String conceptId = focusconcept.eclconceptreference().conceptid().getText();
    final ECLParser.ConstraintoperatorContext constraintoperator = ctx.constraintoperator();
    if (constraintoperator == null) {
      final boolean isAdditionalType = ctx.getParent() instanceof ECLParser.EclattributenameContext;
      final TermQuery termQuery =
          new TermQuery(new Term(isAdditionalType ? "additionalType" : "code", conceptId));
      queries.put(text, termQuery);
    } else {
      if (constraintoperator.childof() != null) {
        final TermQuery parentQuery = new TermQuery(new Term("parent.code", conceptId));
        queries.put(text, parentQuery);
      } else if (constraintoperator.parentof() != null) {
        final TermQuery childrenQuery = new TermQuery(new Term("children.code", conceptId));
        queries.put(text, childrenQuery);
      } else if (constraintoperator.descendantof() != null) {
        final TermQuery ancestorsQuery = new TermQuery(new Term("ancestors.code", conceptId));
        queries.put(text, ancestorsQuery);
      } else if (constraintoperator.descendantorselfof() != null) {
        final TermQuery ancestorsTermQuery = new TermQuery(new Term("ancestors.code", conceptId));
        final TermQuery selfTermQuery = new TermQuery(new Term("code", conceptId));
        final BooleanQuery selfAncestorsQuery =
            new BooleanQuery.Builder().add(ancestorsTermQuery, BooleanClause.Occur.SHOULD)
                .add(selfTermQuery, BooleanClause.Occur.SHOULD).build();
        queries.put(text, selfAncestorsQuery);
      } else if (constraintoperator.ancestorof() != null) {
        final TermQuery descendantsTermQuery = new TermQuery(new Term("descendants.code", conceptId));
        queries.put(text, descendantsTermQuery);
      } else if (constraintoperator.ancestororselfof() != null) {
        final TermQuery descendantsTermQuery = new TermQuery(new Term("descendants.code", conceptId));
        final TermQuery selfTermQuery = new TermQuery(new Term("code", conceptId));
        final BooleanQuery selfDescendantsQuery =
            new BooleanQuery.Builder().add(descendantsTermQuery, BooleanClause.Occur.SHOULD)
                .add(selfTermQuery, BooleanClause.Occur.SHOULD).build();
        queries.put(text, selfDescendantsQuery);
      }
    }
    if (ctx.historysupplement() != null) {
      addHistorical(ctx.historysupplement());
    }
  }

  /**
   * Adds the historical.
   *
   * @param ctx the ctx
   */
  private void addHistorical(final ECLParser.HistorysupplementContext ctx) {
    if (ctx.historyprofilesuffix() != null) {
      final ECLParser.HistoryprofilesuffixContext historyprofilesuffix = ctx.historyprofilesuffix();
      if (historyprofilesuffix.historymaximumsuffix() != null) {
        final TermQuery ancestorsQuery =
            new TermQuery(new Term("ancestors.code", EclUtil.HISTORY_MAX_PARENT));
        queries.put(ctx.getText(), ancestorsQuery);
      }
      if (historyprofilesuffix.historyminimumsuffix() != null) {
        final Query query =
            new TermQuery(new Term("additionalType", EclUtil.HISTORY_MIN_CONCEPTS.get(0)));
        queries.put(ctx.getText(), query);
      }
      if (historyprofilesuffix.historymoderatesuffix() != null) {
        final Query query = LuceneEclDataAccess.getOrQuery(EclUtil.HISTORY_MOD_CONCEPTS);
        queries.put(ctx.getText(), query);
      }
    }
  }

  /* see superclass */
  @Override
  public void exitEclattribute(final ECLParser.EclattributeContext ctx) {
    final EclExpression eclExpression = getExpression(ctx);
    handleExpression(eclExpression, (lhsConcepts, rhsConcepts) -> {
      try {
        Query concepts = null;
        if (rhsConcepts == null && eclExpression.rhsOperand.isLiteral) {
          concepts = new BooleanQuery.Builder()
              .add(new BooleanClause(lhsConcepts, BooleanClause.Occur.MUST))
              .add(new BooleanClause(new TermQuery(new Term("toValue", getLiteral(eclExpression))),
                  BooleanClause.Occur.MUST))
              .build();
          // } else {
          // concepts = new BooleanQuery.Builder()
          // .add(new BooleanClause(lhsConcepts, BooleanClause.Occur.MUST))
          // .add(new BooleanClause(rhsConcepts, BooleanClause.Occur.MUST)).build();
          // }
        } else {
          final Query fromQuery = ALL_CONCEPTS.equals(currentRefinementSubExpression) ? null
              : queries.get(currentRefinementSubExpression);
          final Query toQuery = ALL_CONCEPTS.equals(eclExpression.rhsOperand.expression)
              ? new MatchAllDocsQuery() : rhsConcepts;
          final Query additionalQuery = ALL_CONCEPTS.equals(eclExpression.lhsOperand.expression)
              ? new MatchAllDocsQuery() : lhsConcepts;

          final Query refinementQuery =
              luceneEclDataAccess.getRefinementQuery(fromQuery, toQuery, additionalQuery);
          concepts = refinementQuery;
        }
        queries.put(eclExpression.expressionText, concepts);
        currentEclAttributes.add(eclExpression.expressionText);
      } catch (final Exception e) {
        throw new RuntimeException("Error performing relationship search", e);
      }
    }, operands -> {
      // n/a
    });
  }

  /* see superclass */
  @Override
  public void exitConjunctionexpressionconstraint(
    final ECLParser.ConjunctionexpressionconstraintContext ctx) {
    final EclExpression expression = getExpression(ctx);
    currentSubExpression = expression.expressionText;
    handleExpression(expression, getConjunctionConsumer(expression), operands -> {
      final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
      for (final Query query : operands) {
        booleanQueryBuilder.add(query, BooleanClause.Occur.MUST);
      }
      queries.put(expression.expressionText, booleanQueryBuilder.build());
    });
  }

  /* see superclass */
  @Override
  public void exitSubattributeset(final ECLParser.SubattributesetContext ctx) {
    handleSubExpression(ctx);
  }

  /* see superclass */
  @Override
  public void exitEclattributeset(final ECLParser.EclattributesetContext ctx) {
    final EclExpression expression = getExpression(ctx);
    if (expression != null) {
      currentSubExpression = expression.expressionText;
      final ParserRuleContext rhsCtx = ctx.getChild(ParserRuleContext.class, 2);
      if (rhsCtx instanceof ECLParser.ConjunctionattributesetContext) {
        handleExpression(expression, getConjunctionConsumer(expression), getNoOpOperandsConsumer());
      }
      if (rhsCtx instanceof ECLParser.DisjunctionattributesetContext) {
        handleExpression(expression, getDisjuctionConsumer(expression), getNoOpOperandsConsumer());
      }
    }
  }

  /* see superclass */
  @Override
  public void exitConjunctionrefinementset(final ECLParser.ConjunctionrefinementsetContext ctx) {
    super.exitConjunctionrefinementset(ctx);
  }

  /* see superclass */
  @Override
  public void exitDisjunctionexpressionconstraint(
    final ECLParser.DisjunctionexpressionconstraintContext ctx) {
    final EclExpression expression = getExpression(ctx);
    currentSubExpression = expression.expressionText;
    handleExpression(expression, getDisjuctionConsumer(expression), operands -> {
      final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
      for (final Query query : operands) {
        booleanQueryBuilder.add(query, BooleanClause.Occur.SHOULD);
      }
      queries.put(expression.expressionText, booleanQueryBuilder.build());
    });
  }

  /* see superclass */
  @Override
  public void exitExclusionexpressionconstraint(
    final ECLParser.ExclusionexpressionconstraintContext ctx) {
    final EclExpression expression = getExpression(ctx);
    currentSubExpression = expression.expressionText;
    handleExpression(expression, (lhsConcepts, rhsConcepts) -> {
      final Query query = new BooleanQuery.Builder().add(lhsConcepts, BooleanClause.Occur.MUST)
          .add(rhsConcepts, BooleanClause.Occur.MUST_NOT).build();
      queries.put(expression.expressionText, query);
    }, operands -> {
      // n/a
    });
  }

  /* see superclass */
  @Override
  public void exitConjunctionattributeset(final ECLParser.ConjunctionattributesetContext ctx) {
    super.exitConjunctionattributeset(ctx);
  }

  /* see superclass */
  @Override
  public void exitDisjunctionattributeset(final ECLParser.DisjunctionattributesetContext ctx) {
    super.exitDisjunctionattributeset(ctx);
  }

  /* see superclass */
  @Override
  public void exitDisjunctionrefinementset(final ECLParser.DisjunctionrefinementsetContext ctx) {
    super.exitDisjunctionrefinementset(ctx);
  }

  /* see superclass */
  @Override
  public void enterRefinedexpressionconstraint(final ECLParser.RefinedexpressionconstraintContext ctx) {
    super.enterRefinedexpressionconstraint(ctx);
    isRefinment = true;
  }

  /* see superclass */
  @Override
  public void exitRefinedexpressionconstraint(final ECLParser.RefinedexpressionconstraintContext ctx) {
    final EclExpression eclExpression = getExpression(ctx);
    currentSubExpression = eclExpression.expressionText;
    assertOperands(eclExpression);
    handleExpression(eclExpression, (lhsConcepts, rhsConcepts) -> {
      // Query fromQuery = lhsConcepts;
      // if (ALL_CONCEPTS.equals(eclExpression.lhsOperand.expression)) {
      // fromQuery = null;
      // }
      // Query additionalTypeQuery = ((BooleanQuery) rhsConcepts).clauses().get(0).getQuery();
      // Query toQuery = ((BooleanQuery) rhsConcepts).clauses().get(1).getQuery();
      // Query query = null;
      // try {
      // query = luceneDao.getRefinementQuery(fromQuery, toQuery, additionalTypeQuery);
      // } catch (IOException e) {
      // throw new RuntimeException(e);
      // }
      // queries.put(eclExpression.expressionText, query);
      queries.put(eclExpression.expressionText, rhsConcepts);
    }, getNoOpOperandsConsumer());
  }

  /* see superclass */
  @Override
  public void exitSubexpressionconstraint(final ECLParser.SubexpressionconstraintContext ctx) {
    if (ctx.historysupplement() != null) {
      final EclExpression expression = getExpression(ctx);
      handleExpression(expression, (lhsConcepts, rhsConcepts) -> {
        try {
          final Query fromQuery = ALL_CONCEPTS.equals(currentRefinementSubExpression) ? null
              : queries.get(currentRefinementSubExpression);
          final Query toQuery = ALL_CONCEPTS.equals(expression.lhsOperand.expression)
              ? new MatchAllDocsQuery() : lhsConcepts;
          final Query additionalQuery = ALL_CONCEPTS.equals(expression.rhsOperand.expression)
              ? new MatchAllDocsQuery() : rhsConcepts;
          final Query query = luceneEclDataAccess.getRefinementQuery(fromQuery, toQuery, additionalQuery);
          final BooleanQuery booleanQuery =
              new BooleanQuery.Builder().add(query, BooleanClause.Occur.SHOULD)
                  .add(toQuery, BooleanClause.Occur.SHOULD).build();
          queries.put(expression.expressionText, booleanQuery);
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
      }, operands -> {
        // n/a
      });
    }
    handleSubExpression(ctx);
    if (isRefinment) {
      currentRefinementSubExpression = ctx.getText();
    }
  }

  /* see superclass */
  @Override
  public void enterEclrefinement(final ECLParser.EclrefinementContext ctx) {
    super.enterEclrefinement(ctx);
    isRefinment = false;
  }

  /**
   * Handle expression.
   *
   * @param eclExpression the ecl expression
   * @param biOperandSetOperation the bi operand set operation
   * @param multiOperandSetOperation the multi operand set operation
   */
  private void handleExpression(final EclExpression eclExpression,
    final BiConsumer<Query, Query> biOperandSetOperation,
    final Consumer<List<Query>> multiOperandSetOperation) {
    if (eclExpression.operands() == null) {
      // this is an operation that has LHS and RHS
      assertOperands(eclExpression);
      final Query lhsConcepts = getQuery(eclExpression.lhsOperand.expression);
      final Query rhsConcepts = getQuery(eclExpression.rhsOperand.expression);
      biOperandSetOperation.accept(lhsConcepts, rhsConcepts);
    } else {
      final List<Query> operandConcepts = new ArrayList<>();
      for (final String operand : eclExpression.operands) {
        final Query concepts = getQuery(operand);
        if (concepts == null) {
          throw new RuntimeException(String.format(
              "Operand concept eclExpression produced null concepts. Expression:%s", operand));
        }
        operandConcepts.add(concepts);
      }
      multiOperandSetOperation.accept(operandConcepts);
    }
  }

  /**
   * Assert operands.
   *
   * @param eclExpression the ecl expression
   */
  private void assertOperands(final EclExpression eclExpression) {
    final Query lhsQuery = getQuery(eclExpression.lhsOperand.expression);
    final Query rhsQuery = getQuery(eclExpression.rhsOperand.expression);
    if (lhsQuery == null) {
      if (!ALL_CONCEPTS.equals(eclExpression.lhsOperand.expression)) {
        throw new RuntimeException(
            String.format("lhsConcepts %s is null", eclExpression.lhsOperand.expression));
      }
    }
    if (rhsQuery == null) {
      if (!ALL_CONCEPTS.equals(eclExpression.rhsOperand.expression)
          && !eclExpression.rhsOperand.isLiteral) {
        throw new RuntimeException(
            String.format("rhsConcepts %s is null", eclExpression.rhsOperand.expression));
      }
    }
  }

  /**
   * Gets the query.
   *
   * @param operand2 the operand 2
   * @return the query
   */
  private Query getQuery(final String operand2) {
    final String operand = operand2.trim();
    Query query = queries.get(operand);
    if (query == null) {
      query = queries.get(appendBrackets(operand));
    }
    if (query == null) {
      query = queries.get(removeBrackets(operand));
    }
    return query;
  }

  /**
   * Append brackets.
   *
   * @param str the str
   * @return the string
   */
  public static String appendBrackets(final String str) {
    final String bracketType = "()";
    return bracketType.charAt(0) + str + bracketType.charAt(1);
  }

  /**
   * Gets the expression.
   *
   * @param context the context
   * @return the expression
   */
  private static EclExpression getExpression(final ParserRuleContext context) {
    final String expressionText = cleanExpression(context.getText());
    if (context instanceof ECLParser.EclattributesetContext) {
      if (context.getChildCount() > 2) {
        final String lhs = context.getChild(0).getText();
        final String rhs = context.getChild(2).getChild(3).getText();
        return new EclExpression(expressionText, new Operand(lhs, false, "lhs"),
            new Operand(rhs, false, "rhs"), null);
      } else {
        return null;
      }
    }
    // This for expressions that only have 2 operands. For example, (<<246075003 OR <762952000).
    // This is most common case
    if (context.getChildCount() == 5) {
      EclExpression eclExpression = null;
      if (context instanceof ECLParser.SubexpressionconstraintContext) {
        eclExpression = getHistoricalExpression(expressionText, context);
        if (eclExpression != null) {
          return eclExpression;
        }
      }
      final String lhs = cleanExpression(context.getChild(0).getText());
      final String rhs = cleanExpression(context.getChild(4).getText());
      final boolean rhsLiteral = isLiteral(context);
      return new EclExpression(expressionText, new Operand(lhs, false, "lhs"),
          new Operand(removeQuotes(rhs), rhsLiteral, "rhs"), null);
    } else if (context.getChildCount() % 4 == 1) {
      // This for expression that have multiple operators. For example, <<255412001 OR <<263714004
      // OR <<260245000
      final List<String> operands = new ArrayList<>();
      final int numberOfOperands = ((context.getChildCount() - 1) / 4) + 1;
      for (int index = 0; index < numberOfOperands; index++) {
        operands.add(context.getChild(index * 4).getText());
      }
      return new EclExpression(expressionText, null, null, operands);
    } else if (context.getChildCount() == 6) {
      // This is for supporting numeric literals which have # symbol
      final String lhs = cleanExpression(context.getChild(0).getText());
      final String rhs = cleanExpression(context.getChild(5).getText());
      // Add HASH to literal
      return new EclExpression(expressionText, new Operand(lhs, false, "lhs"),
          new Operand("#" + rhs, true, "rhs"), null);
    }
    throw new RuntimeException(
        "Unable to create EclExpression. expression text:" + context.getText());
  }

  /**
   * Removes the brackets.
   *
   * @param str the str
   * @return the string
   */
  public static String removeBrackets(final String str) {
    return removeFirstAndLast(str, "(", ")");
  }

  /**
   * Removes the quotes.
   *
   * @param str the str
   * @return the string
   */
  public static String removeQuotes(final String str) {
    return removeFirstAndLast(str, "\"");
  }

  /**
   * Removes the first and last.
   *
   * @param str the str
   * @param symbol the symbol
   * @return the string
   */
  public static String removeFirstAndLast(final String str, final String symbol) {
    return removeFirstAndLast(str, symbol, symbol);
  }

  /**
   * Removes the first and last.
   *
   * @param str the str
   * @param firstSymbol the first symbol
   * @param lastSymbol the last symbol
   * @return the string
   */
  public static String removeFirstAndLast(final String str, final String firstSymbol, final String lastSymbol) {
    return str.startsWith(firstSymbol) && str.endsWith(lastSymbol)
        ? str.substring(1, str.length() - 1) : str;
  }

  /**
   * Removes the comments.
   *
   * @param code the code
   * @return the string
   */
  public static String removeComments(final String code) {
    return Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL).matcher(code).replaceAll("");
  }

  /**
   * Clean expression.
   *
   * @param expression the expression
   * @return the string
   */
  public static String cleanExpression(final String expression) {
    String cleansedExpression = removeComments(expression);
    cleansedExpression = removeQuotes(cleansedExpression);
    return cleansedExpression;
  }

  /**
   * The Record ParserResult.
   *
   * @param luceneQuery the lucene query
   * @param msearchQueries the msearch queries
   * @param results the results
   */
  public record ParserResult(String luceneQuery, LinkedHashSet<EclConceptQuery> msearchQueries,
      Map<String, List<String>> results) {
  }

  /**
   * The Record EclConceptQuery.
   *
   * @param text the text
   * @param conceptId the concept id
   * @param hierarchyType the hierarchy type
   * @param includeSelf the include self
   */
  public record EclConceptQuery(String text, String conceptId, String hierarchyType,
      boolean includeSelf) {
  }

  /**
   * The Record EclExpression.
   *
   * @param expressionText the expression text
   * @param lhsOperand the lhs operand
   * @param rhsOperand the rhs operand
   * @param operands the operands
   */
  public record EclExpression(String expressionText, Operand lhsOperand, Operand rhsOperand,
      List<String> operands) {
  }

  /**
   * The Record Operand.
   *
   * @param expression the expression
   * @param isLiteral the is literal
   * @param type the type
   */
  public record Operand(String expression, boolean isLiteral, String type) {
  }

  /**
   * Gets the disjuction consumer.
   *
   * @param expression the expression
   * @return the disjuction consumer
   */
  private BiConsumer<Query, Query> getDisjuctionConsumer(final EclExpression expression) {
    return (lhsQuery, rhsQuery) -> {
      final BooleanQuery query = new BooleanQuery.Builder().add(lhsQuery, BooleanClause.Occur.SHOULD)
          .add(rhsQuery, BooleanClause.Occur.SHOULD).build();
      queries.put(expression.expressionText, query);
    };
  }

  /**
   * Gets the conjunction consumer.
   *
   * @param expression the expression
   * @return the conjunction consumer
   */
  private BiConsumer<Query, Query> getConjunctionConsumer(final EclExpression expression) {
    return (lhsQuery, rhsQuery) -> {
      final BooleanQuery query = new BooleanQuery.Builder().add(lhsQuery, BooleanClause.Occur.MUST)
          .add(rhsQuery, BooleanClause.Occur.MUST).build();
      queries.put(expression.expressionText, query);
    };
  }

  /**
   * Gets the no op operands consumer.
   *
   * @return the no op operands consumer
   */
  private Consumer<List<Query>> getNoOpOperandsConsumer() {
    return operands -> {
      // n/a
    };
  }

  /**
   * This is to handle expression that have a bracket in them. For example, for (<<72431002 :
   * 246075003=<410942007), we would have created the query for <<72431002 : 246075003=<410942007
   * and stored it as the key in the query map. That would not match the bracketed expression. In
   * this method we are replacing that key to the bracketed expression
   *
   * @param ctx the ctx
   */
  private void handleSubExpression(final ParserRuleContext ctx) {
    if (currentSubExpression != null) {
      final Query concepts = queries.get(currentSubExpression);
      queries.put(ctx.getText(), concepts);
      queries.remove(currentSubExpression);
    }
    currentSubExpression = null;
  }

  /**
   * Checks if is literal.
   *
   * @param context the context
   * @return true, if is literal
   */
  private static boolean isLiteral(final ParserRuleContext context) {
    for (final ParseTree childContext : context.children) {
      if (childContext instanceof ECLParser.NumericcomparisonoperatorContext
          || childContext instanceof ECLParser.StringcomparisonoperatorContext
          || childContext instanceof ECLParser.BooleancomparisonoperatorContext) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the literal.
   *
   * @param eclExpression the ecl expression
   * @return the literal
   */
  private static String getLiteral(final EclExpression eclExpression) {
    if (eclExpression == null || eclExpression.rhsOperand == null
        || !eclExpression.rhsOperand.isLiteral) {
      return null;
    }
    return eclExpression.rhsOperand.expression;
  }

  /**
   * Gets the queries.
   *
   * @return the queries
   */
  public Map<String, Query> getQueries() {
    return this.queries;
  }

  /**
   * Gets the historical expression.
   *
   * @param expressionText the expression text
   * @param ctx the ctx
   * @return the historical expression
   */
  private static EclExpression getHistoricalExpression(final String expressionText,
    final ParserRuleContext ctx) {
    final ECLParser.SubexpressionconstraintContext subCtx =
        (ECLParser.SubexpressionconstraintContext) ctx;
    if (subCtx.historysupplement() != null) {
      String lhs = cleanExpression(subCtx.getText());
      final String rhs = subCtx.historysupplement().historyprofilesuffix() != null
          ? cleanExpression(subCtx.historysupplement().getText())
          : getHistorySubsetExpression(subCtx.historysupplement().historysubset());
      // remove the historical supplement from expression
      lhs = lhs.replace(subCtx.historysupplement().getText(), "").trim();
      return new EclExpression(expressionText, new Operand(lhs, false, "lhs"),
          new Operand(rhs, false, "rhs"), null);
    }
    return null;
  }

  /**
   * Gets the history subset expression.
   *
   * @param subCtx the sub ctx
   * @return the history subset expression
   */
  private static String getHistorySubsetExpression(final ECLParser.HistorysubsetContext subCtx) {
    if (subCtx.getChildCount() == 5) {
      return subCtx.getChild(2).getText();
    }
    throw new RuntimeException("Unable to create history expression from: " + subCtx.getText());
  }
}
