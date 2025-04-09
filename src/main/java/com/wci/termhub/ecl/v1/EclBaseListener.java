/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.ecl.v1;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * The listener interface for receiving eclBase events. The class that is
 * interested in processing a eclBase event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's addEclBaseListener method. When the eclBase event occurs, that
 * object's appropriate method is invoked.
 *
 */
public class EclBaseListener implements EclListener {

  /* see superclass */
  @Override
  public void enterExpressionconstraint(final EclParser.ExpressionconstraintContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitExpressionconstraint(final EclParser.ExpressionconstraintContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterRefinedexpressionconstraint(
    final EclParser.RefinedexpressionconstraintContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitRefinedexpressionconstraint(
    final EclParser.RefinedexpressionconstraintContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterCompoundexpressionconstraint(
    final EclParser.CompoundexpressionconstraintContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitCompoundexpressionconstraint(
    final EclParser.CompoundexpressionconstraintContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterConjunctionexpressionconstraint(
    final EclParser.ConjunctionexpressionconstraintContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitConjunctionexpressionconstraint(
    final EclParser.ConjunctionexpressionconstraintContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterDisjunctionexpressionconstraint(
    final EclParser.DisjunctionexpressionconstraintContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitDisjunctionexpressionconstraint(
    final EclParser.DisjunctionexpressionconstraintContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterExclusionexpressionconstraint(
    final EclParser.ExclusionexpressionconstraintContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitExclusionexpressionconstraint(
    final EclParser.ExclusionexpressionconstraintContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterSubexpressionconstraint(final EclParser.SubexpressionconstraintContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitSubexpressionconstraint(final EclParser.SubexpressionconstraintContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterEclfocusconcept(final EclParser.EclfocusconceptContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitEclfocusconcept(final EclParser.EclfocusconceptContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterMemberof(final EclParser.MemberofContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitMemberof(final EclParser.MemberofContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterEclconceptreference(final EclParser.EclconceptreferenceContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitEclconceptreference(final EclParser.EclconceptreferenceContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterConceptid(final EclParser.ConceptidContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitConceptid(final EclParser.ConceptidContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterTerm(final EclParser.TermContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitTerm(final EclParser.TermContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterWildcard(final EclParser.WildcardContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitWildcard(final EclParser.WildcardContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterConstraintoperator(final EclParser.ConstraintoperatorContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitConstraintoperator(final EclParser.ConstraintoperatorContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterDescendantof(final EclParser.DescendantofContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitDescendantof(final EclParser.DescendantofContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterDescendantorselfof(final EclParser.DescendantorselfofContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitDescendantorselfof(final EclParser.DescendantorselfofContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterChildof(final EclParser.ChildofContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitChildof(final EclParser.ChildofContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterAncestorof(final EclParser.AncestorofContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitAncestorof(final EclParser.AncestorofContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterAncestororselfof(final EclParser.AncestororselfofContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitAncestororselfof(final EclParser.AncestororselfofContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterParentof(final EclParser.ParentofContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitParentof(final EclParser.ParentofContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterConjunction(final EclParser.ConjunctionContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitConjunction(final EclParser.ConjunctionContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterDisjunction(final EclParser.DisjunctionContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitDisjunction(final EclParser.DisjunctionContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterExclusion(final EclParser.ExclusionContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitExclusion(final EclParser.ExclusionContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterEclrefinement(final EclParser.EclrefinementContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitEclrefinement(final EclParser.EclrefinementContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterConjunctionrefinementset(final EclParser.ConjunctionrefinementsetContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitConjunctionrefinementset(final EclParser.ConjunctionrefinementsetContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterDisjunctionrefinementset(final EclParser.DisjunctionrefinementsetContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitDisjunctionrefinementset(final EclParser.DisjunctionrefinementsetContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterSubrefinement(final EclParser.SubrefinementContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitSubrefinement(final EclParser.SubrefinementContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterEclattributeset(final EclParser.EclattributesetContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitEclattributeset(final EclParser.EclattributesetContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterConjunctionattributeset(final EclParser.ConjunctionattributesetContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitConjunctionattributeset(final EclParser.ConjunctionattributesetContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterDisjunctionattributeset(final EclParser.DisjunctionattributesetContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitDisjunctionattributeset(final EclParser.DisjunctionattributesetContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterSubattributeset(final EclParser.SubattributesetContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitSubattributeset(final EclParser.SubattributesetContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterEclattributegroup(final EclParser.EclattributegroupContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitEclattributegroup(final EclParser.EclattributegroupContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterEclattribute(final EclParser.EclattributeContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitEclattribute(final EclParser.EclattributeContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterCardinality(final EclParser.CardinalityContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitCardinality(final EclParser.CardinalityContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterMinvalue(final EclParser.MinvalueContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitMinvalue(final EclParser.MinvalueContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterTo(final EclParser.ToContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitTo(final EclParser.ToContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterMaxvalue(final EclParser.MaxvalueContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitMaxvalue(final EclParser.MaxvalueContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterMany(final EclParser.ManyContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitMany(final EclParser.ManyContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterReverseflag(final EclParser.ReverseflagContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitReverseflag(final EclParser.ReverseflagContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterEclattributename(final EclParser.EclattributenameContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitEclattributename(final EclParser.EclattributenameContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterExpressioncomparisonoperator(
    final EclParser.ExpressioncomparisonoperatorContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitExpressioncomparisonoperator(
    final EclParser.ExpressioncomparisonoperatorContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterNumericcomparisonoperator(final EclParser.NumericcomparisonoperatorContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitNumericcomparisonoperator(final EclParser.NumericcomparisonoperatorContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterStringcomparisonoperator(final EclParser.StringcomparisonoperatorContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitStringcomparisonoperator(final EclParser.StringcomparisonoperatorContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterNumericvalue(final EclParser.NumericvalueContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitNumericvalue(final EclParser.NumericvalueContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterStringvalue(final EclParser.StringvalueContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitStringvalue(final EclParser.StringvalueContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterIntegervalue(final EclParser.IntegervalueContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitIntegervalue(final EclParser.IntegervalueContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterDecimalvalue(final EclParser.DecimalvalueContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitDecimalvalue(final EclParser.DecimalvalueContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterNonnegativeintegervalue(final EclParser.NonnegativeintegervalueContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitNonnegativeintegervalue(final EclParser.NonnegativeintegervalueContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterSctid(final EclParser.SctidContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitSctid(final EclParser.SctidContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterWs(final EclParser.WsContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitWs(final EclParser.WsContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterMws(final EclParser.MwsContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitMws(final EclParser.MwsContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterComment(final EclParser.CommentContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitComment(final EclParser.CommentContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterNonstarchar(final EclParser.NonstarcharContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitNonstarchar(final EclParser.NonstarcharContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterStarwithnonfslash(final EclParser.StarwithnonfslashContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitStarwithnonfslash(final EclParser.StarwithnonfslashContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterNonfslash(final EclParser.NonfslashContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitNonfslash(final EclParser.NonfslashContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterSp(final EclParser.SpContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitSp(final EclParser.SpContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterHtab(final EclParser.HtabContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitHtab(final EclParser.HtabContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterCr(final EclParser.CrContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitCr(final EclParser.CrContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterLf(final EclParser.LfContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitLf(final EclParser.LfContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterQm(final EclParser.QmContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitQm(final EclParser.QmContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterBs(final EclParser.BsContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitBs(final EclParser.BsContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterDigit(final EclParser.DigitContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitDigit(final EclParser.DigitContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterZero(final EclParser.ZeroContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitZero(final EclParser.ZeroContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterDigitnonzero(final EclParser.DigitnonzeroContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitDigitnonzero(final EclParser.DigitnonzeroContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterNonwsnonpipe(final EclParser.NonwsnonpipeContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitNonwsnonpipe(final EclParser.NonwsnonpipeContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterAnynonescapedchar(final EclParser.AnynonescapedcharContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitAnynonescapedchar(final EclParser.AnynonescapedcharContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterCodechar(final EclParser.CodecharContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitCodechar(final EclParser.CodecharContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterEscapedchar(final EclParser.EscapedcharContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitEscapedchar(final EclParser.EscapedcharContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterUtf8_2(final EclParser.Utf8_2Context ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitUtf8_2(final EclParser.Utf8_2Context ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterUtf8_3(final EclParser.Utf8_3Context ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitUtf8_3(final EclParser.Utf8_3Context ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterUtf8_4(final EclParser.Utf8_4Context ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitUtf8_4(final EclParser.Utf8_4Context ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterUtf8_tail(final EclParser.Utf8_tailContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitUtf8_tail(final EclParser.Utf8_tailContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void enterEveryRule(final ParserRuleContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void exitEveryRule(final ParserRuleContext ctx) { // n/a
  }

  /* see superclass */
  @Override
  public void visitTerminal(final TerminalNode node) { // n/a
  }

  /* see superclass */
  @Override
  public void visitErrorNode(final ErrorNode node) { // n/a
  }
}