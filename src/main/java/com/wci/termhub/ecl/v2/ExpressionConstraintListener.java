/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.ecl.v2;

import com.wci.termhub.lucene.LuceneEclDataAccess;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ExpressionConstraintListener extends EclLogListener {
    public static final String ALL_CONCEPTS = "*";
    private String currentSubExpression = null;
    private LuceneEclDataAccess luceneEclDataAccess;
    private List<String> currentEclAttributes = new ArrayList<>();
    private boolean isRefinment = false;
    private String currentRefinementSubExpression = null;

    public ExpressionConstraintListener(LuceneEclDataAccess luceneEclDataAccess) {
        this.luceneEclDataAccess = luceneEclDataAccess;
    }

    private Map<String, Query> queries = new HashMap<>();

    /* see superclass */
    @Override
    public void enterSubexpressionconstraint(final ECLParser.SubexpressionconstraintContext ctx) {
        String text = ctx.getText();

        final ECLParser.EclfocusconceptContext focusconcept = ctx.eclfocusconcept();
        if (focusconcept == null) {
            // logger.warn("Unable to handle this condition");
            return;
        }

        final String conceptId = focusconcept.eclconceptreference().conceptid().getText();
        final ECLParser.ConstraintoperatorContext constraintoperator = ctx.constraintoperator();
        if (constraintoperator == null) {
            boolean isAdditionalType = ctx.getParent() instanceof ECLParser.EclattributenameContext;
            TermQuery termQuery = new TermQuery(new Term(isAdditionalType ? "additionalType" : "code", conceptId));
            queries.put(text, termQuery);
        } else {
            if (constraintoperator.childof() != null) {
                TermQuery parentQuery = new TermQuery(new Term("parent.code", conceptId));
                queries.put(text, parentQuery);
            } else if (constraintoperator.parentof() != null) {
                TermQuery childrenQuery = new TermQuery(new Term("children.code", conceptId));
                queries.put(text, childrenQuery);
            } else if (constraintoperator.descendantof() != null) {
                TermQuery ancestorsQuery = new TermQuery(new Term("ancestors.code", conceptId));
                queries.put(text, ancestorsQuery);
            } else if (constraintoperator.descendantorselfof() != null) {
                TermQuery ancestorsTermQuery = new TermQuery(new Term("ancestors.code", conceptId));
                TermQuery selfTermQuery = new TermQuery(new Term("code", conceptId));
                BooleanQuery selfAncestorsQuery = new BooleanQuery.Builder().add(ancestorsTermQuery, BooleanClause.Occur.SHOULD).add(selfTermQuery, BooleanClause.Occur.SHOULD).build();
                queries.put(text, selfAncestorsQuery);
            } else if (constraintoperator.ancestorof() != null) {
                TermQuery descendantsTermQuery = new TermQuery(new Term("descendants.code", conceptId));
                queries.put(text, descendantsTermQuery);
            } else if (constraintoperator.ancestororselfof() != null) {
                TermQuery descendantsTermQuery = new TermQuery(new Term("descendants.code", conceptId));
                TermQuery selfTermQuery = new TermQuery(new Term("code", conceptId));
                BooleanQuery selfDescendantsQuery = new BooleanQuery.Builder().add(descendantsTermQuery, BooleanClause.Occur.SHOULD).add(selfTermQuery, BooleanClause.Occur.SHOULD).build();
                queries.put(text, selfDescendantsQuery);
            }
        }
    }

    @Override
    public void exitEclattribute(final ECLParser.EclattributeContext ctx) {
        EclExpression eclExpression = getExpression(ctx);
        handleExpression(
                eclExpression,
                (lhsConcepts, rhsConcepts) -> {
                    try {
                        Query concepts = null;
                        if (rhsConcepts == null && eclExpression.rhsOperand.isLiteral) {
                            concepts = new BooleanQuery.Builder()
                                    .add(new BooleanClause(lhsConcepts, BooleanClause.Occur.MUST))
                                    .add(new BooleanClause(new TermQuery(new Term("toValue", getLiteral(eclExpression))), BooleanClause.Occur.MUST)).build();
//                            } else {
//                                concepts = new BooleanQuery.Builder()
//                                        .add(new BooleanClause(lhsConcepts, BooleanClause.Occur.MUST))
//                                        .add(new BooleanClause(rhsConcepts, BooleanClause.Occur.MUST)).build();
//                            }
                        } else {
                            Query fromQuery = ALL_CONCEPTS.equals(currentRefinementSubExpression) ? null : queries.get(currentRefinementSubExpression);
                            Query toQuery = ALL_CONCEPTS.equals(eclExpression.rhsOperand.expression) ? new MatchAllDocsQuery() : rhsConcepts;
                            Query additionalQuery = ALL_CONCEPTS.equals(eclExpression.lhsOperand.expression) ? new MatchAllDocsQuery() : lhsConcepts;

                            Query refinementQuery = luceneEclDataAccess.getRefinementQuery(fromQuery, toQuery, additionalQuery);
                            concepts = refinementQuery;
                        }
                        queries.put(eclExpression.expressionText, concepts);
                        currentEclAttributes.add(eclExpression.expressionText);
                    } catch (Exception e) {
                        throw new RuntimeException("Error performing relationship search", e);
                    }
                },
                operands -> {
                });
    }

    @Override
    public void exitConjunctionexpressionconstraint(
            ECLParser.ConjunctionexpressionconstraintContext ctx) {
        EclExpression expression = getExpression(ctx);
        currentSubExpression = expression.expressionText;
        handleExpression(
                expression,
                getConjunctionConsumer(expression),
                operands -> {
                    BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
                    for (Query query : operands) {
                        booleanQueryBuilder.add(query, BooleanClause.Occur.MUST);
                    }
                    queries.put(expression.expressionText, booleanQueryBuilder.build());
                });
    }

    @Override
    public void exitSubattributeset(ECLParser.SubattributesetContext ctx) {
        handleSubExpression(ctx);
    }

    @Override
    public void exitEclattributeset(ECLParser.EclattributesetContext ctx) {
        EclExpression expression = getExpression(ctx);
        if (expression != null) {
            currentSubExpression = expression.expressionText;
            ParserRuleContext rhsCtx = ctx.getChild(ParserRuleContext.class, 2);
            if (rhsCtx instanceof ECLParser.ConjunctionattributesetContext) {
                handleExpression(expression, getConjunctionConsumer(expression), getNoOpOperandsConsumer());
            }
            if (rhsCtx instanceof ECLParser.DisjunctionattributesetContext) {
                handleExpression(expression, getDisjuctionConsumer(expression), getNoOpOperandsConsumer());
            }
        }
    }

    @Override
    public void exitConjunctionrefinementset(ECLParser.ConjunctionrefinementsetContext ctx) {
        super.exitConjunctionrefinementset(ctx);
    }

    @Override
    public void exitDisjunctionexpressionconstraint(
            ECLParser.DisjunctionexpressionconstraintContext ctx) {
        EclExpression expression = getExpression(ctx);
        currentSubExpression = expression.expressionText;
        handleExpression(
                expression,
                getDisjuctionConsumer(expression),
                operands -> {
                    BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
                    for (Query query : operands) {
                        booleanQueryBuilder.add(query, BooleanClause.Occur.SHOULD);
                    }
                    queries.put(expression.expressionText, booleanQueryBuilder.build());
                });
    }

    @Override
    public void exitExclusionexpressionconstraint(
            ECLParser.ExclusionexpressionconstraintContext ctx) {
        EclExpression expression = getExpression(ctx);
        currentSubExpression = expression.expressionText;
        handleExpression(
                expression,
                (lhsConcepts, rhsConcepts) -> {
                    Query query = new BooleanQuery.Builder().add(lhsConcepts, BooleanClause.Occur.MUST).add(rhsConcepts, BooleanClause.Occur.MUST_NOT).build();
                    queries.put(expression.expressionText, query);
                },
                operands -> {
                });
    }

    @Override
    public void exitConjunctionattributeset(ECLParser.ConjunctionattributesetContext ctx) {
        super.exitConjunctionattributeset(ctx);
    }

    @Override
    public void exitDisjunctionattributeset(ECLParser.DisjunctionattributesetContext ctx) {
        super.exitDisjunctionattributeset(ctx);
    }

    @Override
    public void exitDisjunctionrefinementset(ECLParser.DisjunctionrefinementsetContext ctx) {
        super.exitDisjunctionrefinementset(ctx);
    }

    @Override
    public void enterRefinedexpressionconstraint(ECLParser.RefinedexpressionconstraintContext ctx) {
        super.enterRefinedexpressionconstraint(ctx);
        isRefinment = true;
    }

    public void exitRefinedexpressionconstraint(ECLParser.RefinedexpressionconstraintContext ctx) {
        EclExpression eclExpression = getExpression(ctx);
        currentSubExpression = eclExpression.expressionText;
        assertOperands(eclExpression);
        handleExpression(
                eclExpression,
                (lhsConcepts, rhsConcepts) -> {
//                    Query fromQuery = lhsConcepts;
//                    if (ALL_CONCEPTS.equals(eclExpression.lhsOperand.expression)) {
//                        fromQuery = null;
//                    }
//                    Query additionalTypeQuery = ((BooleanQuery) rhsConcepts).clauses().get(0).getQuery();
//                    Query toQuery = ((BooleanQuery) rhsConcepts).clauses().get(1).getQuery();
//                    Query query = null;
//                    try {
//                        query = luceneDao.getRefinementQuery(fromQuery, toQuery, additionalTypeQuery);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                    queries.put(eclExpression.expressionText, query);
                    queries.put(eclExpression.expressionText, rhsConcepts);
                },
                getNoOpOperandsConsumer());
    }

    @Override
    public void exitSubexpressionconstraint(ECLParser.SubexpressionconstraintContext ctx) {
        handleSubExpression(ctx);
        if (isRefinment) {
            currentRefinementSubExpression = ctx.getText();
        }
    }

    @Override
    public void enterEclrefinement(ECLParser.EclrefinementContext ctx) {
        super.enterEclrefinement(ctx);
        isRefinment = false;
    }

    private void handleExpression(
            EclExpression eclExpression,
            BiConsumer<Query, Query> biOperandSetOperation,
            Consumer<List<Query>> multiOperandSetOperation) {
        if (eclExpression.operands() == null) {
            // this is an operation that has LHS and RHS
            assertOperands(eclExpression);
            Query lhsConcepts = getQuery(eclExpression.lhsOperand.expression);
            Query rhsConcepts = getQuery(eclExpression.rhsOperand.expression);
            biOperandSetOperation.accept(lhsConcepts, rhsConcepts);
        } else {
            List<Query> operandConcepts = new ArrayList<>();
            for (String operand : eclExpression.operands) {
                Query concepts = getQuery(operand);
                if (concepts == null) {
                    throw new RuntimeException(
                            String.format(
                                    "Operand concept eclExpression produced null concepts. Expression:%s", operand));
                }
                operandConcepts.add(concepts);
            }
            multiOperandSetOperation.accept(operandConcepts);
        }
    }

    private void assertOperands(EclExpression eclExpression) {
        Query lhsQuery = getQuery(eclExpression.lhsOperand.expression);
        Query rhsQuery = getQuery(eclExpression.rhsOperand.expression);
        if (lhsQuery == null) {
            if (!ALL_CONCEPTS.equals(eclExpression.lhsOperand.expression)) {
                throw new RuntimeException(String.format("lhsConcepts %s is null", eclExpression.lhsOperand.expression));
            }
        }
        if (rhsQuery == null) {
            if (!ALL_CONCEPTS.equals(eclExpression.rhsOperand.expression) && !eclExpression.rhsOperand.isLiteral) {
                throw new RuntimeException(String.format("rhsConcepts %s is null", eclExpression.rhsOperand.expression));
            }
        }
    }

    private Query getQuery(String operand) {
        operand = operand.trim();
        Query query = queries.get(operand);
        if (query == null) {
            query = queries.get(appendBrackets(operand));
        }
        if (query == null) {
            query = queries.get(removeBrackets(operand));
        }
        return query;
    }

    public static String appendBrackets(String str) {
        String bracketType = "()";
        return bracketType.charAt(0) + str + bracketType.charAt(1);
    }

    private static EclExpression getExpression(ParserRuleContext context) {
        String expressionText = cleanExpression(context.getText());
        if (context instanceof ECLParser.EclattributesetContext) {
            if (context.getChildCount() > 2) {
                String lhs = context.getChild(0).getText();
                String rhs = context.getChild(2).getChild(3).getText();
                return new EclExpression(expressionText, new Operand(lhs, false, "lhs"), new Operand(rhs, false, "rhs"), null);
            } else {
                return null;
            }
        }
        // This for expressions that only have 2 operands. For example, (<<246075003 OR <762952000).
        // This is most common case
        if (context.getChildCount() == 5) {
            String lhs = cleanExpression(context.getChild(0).getText());
            String rhs = cleanExpression(context.getChild(4).getText());
            boolean rhsLiteral = isLiteral(context);
            return new EclExpression(expressionText, new Operand(lhs, false, "lhs"), new Operand(removeQuotes(rhs), rhsLiteral, "rhs"), null);
        } else if (context.getChildCount() % 4 == 1) {
            // This for expression that have multiple operators. For example, <<255412001 OR <<263714004
            // OR <<260245000
            List<String> operands = new ArrayList<>();
            int numberOfOperands = ((context.getChildCount() - 1) / 4) + 1;
            for (int index = 0; index < numberOfOperands; index++) {
                operands.add(context.getChild(index * 4).getText());
            }
            return new EclExpression(expressionText, null, null, operands);
        } else if (context.getChildCount() == 6) {
            // This is for supporting numeric literals which have # symbol
            String lhs = cleanExpression(context.getChild(0).getText());
            String rhs = cleanExpression(context.getChild(5).getText());
            // Add HASH to literal
            return new EclExpression(expressionText, new Operand(lhs, false, "lhs"), new Operand("#" + rhs, true, "rhs"), null);
        }
        throw new RuntimeException(
                "Unable to create EclExpression. expression text:" + context.getText());
    }

    public static String removeBrackets(String str) {
        return removeFirstAndLast(str, "(", ")");
    }

    public static String removeQuotes(String str) {
        return removeFirstAndLast(str, "\"");
    }

    public static String removeFirstAndLast(String str, String symbol) {
        return removeFirstAndLast(str, symbol, symbol);
    }

    public static String removeFirstAndLast(String str, String firstSymbol, String lastSymbol) {
        return str.startsWith(firstSymbol) && str.endsWith(lastSymbol) ? str.substring(1, str.length() - 1) : str;
    }

    public static String removeComments(String code) {
        return Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL).matcher(code).replaceAll("");
    }

    public static String cleanExpression(String expression) {
        String cleansedExpression = removeComments(expression);
        cleansedExpression = removeQuotes(cleansedExpression);
        return cleansedExpression;
    }

    public record ParserResult(
            String luceneQuery,
            LinkedHashSet<EclConceptQuery> msearchQueries,
            Map<String, List<String>> results) {
    }

    public record EclConceptQuery(
            String text, String conceptId, String hierarchyType, boolean includeSelf) {
    }

    public record EclExpression(
            String expressionText, Operand lhsOperand,
            Operand rhsOperand, List<String> operands) {
    }

    public record Operand(String expression, boolean isLiteral, String type) {
    }

    private BiConsumer<Query, Query> getDisjuctionConsumer(EclExpression expression) {
        return (lhsQuery, rhsQuery) -> {
            BooleanQuery query = new BooleanQuery.Builder().add(lhsQuery, BooleanClause.Occur.SHOULD).add(rhsQuery, BooleanClause.Occur.SHOULD).build();
            queries.put(expression.expressionText, query);
        };
    }

    private BiConsumer<Query, Query> getConjunctionConsumer(EclExpression expression) {
        return (lhsQuery, rhsQuery) -> {
            BooleanQuery query = new BooleanQuery.Builder().add(lhsQuery, BooleanClause.Occur.MUST).add(rhsQuery, BooleanClause.Occur.MUST).build();
            queries.put(expression.expressionText, query);
        };
    }

    private Consumer<List<Query>> getNoOpOperandsConsumer() {
        return operands -> {
        };
    }

    /**
     * This is to handle expression that have a bracket in them.
     * For example, for (<<72431002  : 246075003=<410942007), we would have created the query for <<72431002  : 246075003=<410942007 and stored it as the key in the query map.
     * That would not match the bracketed expression.
     * In this method we are replacing that key to the bracketed expression
     *
     * @param ctx
     */
    private void handleSubExpression(ParserRuleContext ctx) {
        if (currentSubExpression != null) {
            Query concepts = queries.get(currentSubExpression);
            queries.put(ctx.getText(), concepts);
            queries.remove(currentSubExpression);
        }
        currentSubExpression = null;
    }

    private static boolean isLiteral(ParserRuleContext context) {
        for (ParseTree childContext : context.children) {
            if (childContext instanceof ECLParser.NumericcomparisonoperatorContext
                    || childContext instanceof ECLParser.StringcomparisonoperatorContext
                    || childContext instanceof ECLParser.BooleancomparisonoperatorContext) {
                return true;
            }
        }
        return false;
    }

    private static String getLiteral(EclExpression eclExpression) {
        if (eclExpression == null || eclExpression.rhsOperand == null || !eclExpression.rhsOperand.isLiteral) {
            return null;
        }
        return eclExpression.rhsOperand.expression;
    }

    public Map<String, Query> getQueries() {
        return this.queries;
    }
}
