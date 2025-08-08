/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.ecl;// Generated from ECL.antlr by ANTLR 4.13.2

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides an empty implementation of {@link ECLListener}, which can be extended to
 * create a listener which only needs to handle a subset of the available methods.
 *
 * @see EclLogEvent
 */
public class EclLogListener implements ECLListener {

  /** The log. */
  private final Logger log = LoggerFactory.getLogger(EclLogListener.class);

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterExpressionconstraint(final ECLParser.ExpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterExpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitExpressionconstraint(final ECLParser.ExpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitExpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterRefinedexpressionconstraint(
    final ECLParser.RefinedexpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterRefinedexpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitRefinedexpressionconstraint(
    final ECLParser.RefinedexpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitRefinedexpressionconstraint");
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterCompoundexpressionconstraint(
    final ECLParser.CompoundexpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterCompoundexpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitCompoundexpressionconstraint(
    final ECLParser.CompoundexpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitCompoundexpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterConjunctionexpressionconstraint(
    final ECLParser.ConjunctionexpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterConjunctionexpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitConjunctionexpressionconstraint(
    final ECLParser.ConjunctionexpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitConjunctionexpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDisjunctionexpressionconstraint(
    final ECLParser.DisjunctionexpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDisjunctionexpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDisjunctionexpressionconstraint(
    final ECLParser.DisjunctionexpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDisjunctionexpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterExclusionexpressionconstraint(
    final ECLParser.ExclusionexpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterExclusionexpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitExclusionexpressionconstraint(
    final ECLParser.ExclusionexpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitExclusionexpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDottedexpressionconstraint(
    final ECLParser.DottedexpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDottedexpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDottedexpressionconstraint(
    final ECLParser.DottedexpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDottedexpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDottedexpressionattribute(final ECLParser.DottedexpressionattributeContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDottedexpressionattribute");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDottedexpressionattribute(final ECLParser.DottedexpressionattributeContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDottedexpressionattribute");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterSubexpressionconstraint(final ECLParser.SubexpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterSubexpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitSubexpressionconstraint(final ECLParser.SubexpressionconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitSubexpressionconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterEclfocusconcept(final ECLParser.EclfocusconceptContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterEclfocusconcept");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitEclfocusconcept(final ECLParser.EclfocusconceptContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitEclfocusconcept");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDot(final ECLParser.DotContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDot");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDot(final ECLParser.DotContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDot");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterMemberof(final ECLParser.MemberofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterMemberof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitMemberof(final ECLParser.MemberofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitMemberof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterRefsetfieldset(final ECLParser.RefsetfieldsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterRefsetfieldset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitRefsetfieldset(final ECLParser.RefsetfieldsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitRefsetfieldset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterRefsetfield(final ECLParser.RefsetfieldContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterRefsetfield");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitRefsetfield(final ECLParser.RefsetfieldContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitRefsetfield");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterRefsetfieldname(final ECLParser.RefsetfieldnameContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterRefsetfieldname");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitRefsetfieldname(final ECLParser.RefsetfieldnameContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitRefsetfieldname");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterRefsetfieldref(final ECLParser.RefsetfieldrefContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterRefsetfieldref");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitRefsetfieldref(final ECLParser.RefsetfieldrefContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitRefsetfieldref");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterEclconceptreference(final ECLParser.EclconceptreferenceContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterEclconceptreference");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitEclconceptreference(final ECLParser.EclconceptreferenceContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitEclconceptreference");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterEclconceptreferenceset(final ECLParser.EclconceptreferencesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterEclconceptreferenceset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitEclconceptreferenceset(final ECLParser.EclconceptreferencesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitEclconceptreferenceset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterConceptid(final ECLParser.ConceptidContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterConceptid");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitConceptid(final ECLParser.ConceptidContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitConceptid");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTerm(final ECLParser.TermContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTerm");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTerm(final ECLParser.TermContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTerm");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterWildcard(final ECLParser.WildcardContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterWildcard");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitWildcard(final ECLParser.WildcardContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitWildcard");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterConstraintoperator(final ECLParser.ConstraintoperatorContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterConstraintoperator");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitConstraintoperator(final ECLParser.ConstraintoperatorContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitConstraintoperator");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDescendantof(final ECLParser.DescendantofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDescendantof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDescendantof(final ECLParser.DescendantofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDescendantof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDescendantorselfof(final ECLParser.DescendantorselfofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDescendantorselfof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDescendantorselfof(final ECLParser.DescendantorselfofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDescendantorselfof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterChildof(final ECLParser.ChildofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterChildof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitChildof(final ECLParser.ChildofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitChildof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterChildorselfof(final ECLParser.ChildorselfofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterChildorselfof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitChildorselfof(final ECLParser.ChildorselfofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitChildorselfof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterAncestorof(final ECLParser.AncestorofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterAncestorof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitAncestorof(final ECLParser.AncestorofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitAncestorof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterAncestororselfof(final ECLParser.AncestororselfofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterAncestororselfof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitAncestororselfof(final ECLParser.AncestororselfofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitAncestororselfof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterParentof(final ECLParser.ParentofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterParentof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitParentof(final ECLParser.ParentofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitParentof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterParentorselfof(final ECLParser.ParentorselfofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterParentorselfof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitParentorselfof(final ECLParser.ParentorselfofContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitParentorselfof");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterConjunction(final ECLParser.ConjunctionContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterConjunction");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitConjunction(final ECLParser.ConjunctionContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitConjunction");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDisjunction(final ECLParser.DisjunctionContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDisjunction");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDisjunction(final ECLParser.DisjunctionContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDisjunction");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterExclusion(final ECLParser.ExclusionContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterExclusion");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitExclusion(final ECLParser.ExclusionContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitExclusion");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterEclrefinement(final ECLParser.EclrefinementContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterEclrefinement");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitEclrefinement(final ECLParser.EclrefinementContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitEclrefinement");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterConjunctionrefinementset(final ECLParser.ConjunctionrefinementsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterConjunctionrefinementset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitConjunctionrefinementset(final ECLParser.ConjunctionrefinementsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitConjunctionrefinementset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDisjunctionrefinementset(final ECLParser.DisjunctionrefinementsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDisjunctionrefinementset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDisjunctionrefinementset(final ECLParser.DisjunctionrefinementsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDisjunctionrefinementset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterSubrefinement(final ECLParser.SubrefinementContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterSubrefinement");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitSubrefinement(final ECLParser.SubrefinementContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitSubrefinement");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterEclattributeset(final ECLParser.EclattributesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterEclattributeset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitEclattributeset(final ECLParser.EclattributesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitEclattributeset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterConjunctionattributeset(final ECLParser.ConjunctionattributesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterConjunctionattributeset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitConjunctionattributeset(final ECLParser.ConjunctionattributesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitConjunctionattributeset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDisjunctionattributeset(final ECLParser.DisjunctionattributesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDisjunctionattributeset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDisjunctionattributeset(final ECLParser.DisjunctionattributesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDisjunctionattributeset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterSubattributeset(final ECLParser.SubattributesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterSubattributeset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitSubattributeset(final ECLParser.SubattributesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitSubattributeset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterEclattributegroup(final ECLParser.EclattributegroupContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterEclattributegroup");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitEclattributegroup(final ECLParser.EclattributegroupContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitEclattributegroup");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterEclattribute(final ECLParser.EclattributeContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterEclattribute");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitEclattribute(final ECLParser.EclattributeContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitEclattribute");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterCardinality(final ECLParser.CardinalityContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterCardinality");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitCardinality(final ECLParser.CardinalityContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitCardinality");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterMinvalue(final ECLParser.MinvalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterMinvalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitMinvalue(final ECLParser.MinvalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitMinvalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTo(final ECLParser.ToContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTo");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTo(final ECLParser.ToContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTo");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterMaxvalue(final ECLParser.MaxvalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterMaxvalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitMaxvalue(final ECLParser.MaxvalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitMaxvalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterMany(final ECLParser.ManyContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterMany");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitMany(final ECLParser.ManyContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitMany");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterReverseflag(final ECLParser.ReverseflagContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterReverseflag");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitReverseflag(final ECLParser.ReverseflagContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitReverseflag");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterEclattributename(final ECLParser.EclattributenameContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterEclattributename");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitEclattributename(final ECLParser.EclattributenameContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitEclattributename");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterExpressioncomparisonoperator(
    final ECLParser.ExpressioncomparisonoperatorContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterExpressioncomparisonoperator");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitExpressioncomparisonoperator(
    final ECLParser.ExpressioncomparisonoperatorContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitExpressioncomparisonoperator");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterNumericcomparisonoperator(final ECLParser.NumericcomparisonoperatorContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterNumericcomparisonoperator");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitNumericcomparisonoperator(final ECLParser.NumericcomparisonoperatorContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitNumericcomparisonoperator");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTimecomparisonoperator(final ECLParser.TimecomparisonoperatorContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTimecomparisonoperator");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTimecomparisonoperator(final ECLParser.TimecomparisonoperatorContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTimecomparisonoperator");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterStringcomparisonoperator(final ECLParser.StringcomparisonoperatorContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterStringcomparisonoperator");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitStringcomparisonoperator(final ECLParser.StringcomparisonoperatorContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitStringcomparisonoperator");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterBooleancomparisonoperator(final ECLParser.BooleancomparisonoperatorContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterBooleancomparisonoperator");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitBooleancomparisonoperator(final ECLParser.BooleancomparisonoperatorContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitBooleancomparisonoperator");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDescriptionfilterconstraint(
    final ECLParser.DescriptionfilterconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDescriptionfilterconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDescriptionfilterconstraint(
    final ECLParser.DescriptionfilterconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDescriptionfilterconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDescriptionfilter(final ECLParser.DescriptionfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDescriptionfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDescriptionfilter(final ECLParser.DescriptionfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDescriptionfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTermfilter(final ECLParser.TermfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTermfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTermfilter(final ECLParser.TermfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTermfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTermkeyword(final ECLParser.TermkeywordContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTermkeyword");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTermkeyword(final ECLParser.TermkeywordContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTermkeyword");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTypedsearchterm(final ECLParser.TypedsearchtermContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTypedsearchterm");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTypedsearchterm(final ECLParser.TypedsearchtermContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTypedsearchterm");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTypedsearchtermset(final ECLParser.TypedsearchtermsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTypedsearchtermset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTypedsearchtermset(final ECLParser.TypedsearchtermsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTypedsearchtermset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterWild(final ECLParser.WildContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterWild");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitWild(final ECLParser.WildContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitWild");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterMatch(final ECLParser.MatchContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterMatch");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitMatch(final ECLParser.MatchContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitMatch");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterMatchsearchterm(final ECLParser.MatchsearchtermContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterMatchsearchterm");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitMatchsearchterm(final ECLParser.MatchsearchtermContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitMatchsearchterm");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterMatchsearchtermset(final ECLParser.MatchsearchtermsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterMatchsearchtermset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitMatchsearchtermset(final ECLParser.MatchsearchtermsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitMatchsearchtermset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterWildsearchterm(final ECLParser.WildsearchtermContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterWildsearchterm");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitWildsearchterm(final ECLParser.WildsearchtermContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitWildsearchterm");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterWildsearchtermset(final ECLParser.WildsearchtermsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterWildsearchtermset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitWildsearchtermset(final ECLParser.WildsearchtermsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitWildsearchtermset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterLanguagefilter(final ECLParser.LanguagefilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterLanguagefilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitLanguagefilter(final ECLParser.LanguagefilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitLanguagefilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterLanguage(final ECLParser.LanguageContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterLanguage");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitLanguage(final ECLParser.LanguageContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitLanguage");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterLanguagecode(final ECLParser.LanguagecodeContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterLanguagecode");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitLanguagecode(final ECLParser.LanguagecodeContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitLanguagecode");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterLanguagecodeset(final ECLParser.LanguagecodesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterLanguagecodeset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitLanguagecodeset(final ECLParser.LanguagecodesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitLanguagecodeset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTypefilter(final ECLParser.TypefilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTypefilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTypefilter(final ECLParser.TypefilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTypefilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTypeidfilter(final ECLParser.TypeidfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTypeidfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTypeidfilter(final ECLParser.TypeidfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTypeidfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTypeid(final ECLParser.TypeidContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTypeid");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTypeid(final ECLParser.TypeidContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTypeid");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTypetokenfilter(final ECLParser.TypetokenfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTypetokenfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTypetokenfilter(final ECLParser.TypetokenfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTypetokenfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterType(final ECLParser.TypeContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterType");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitType(final ECLParser.TypeContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitType");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTypetoken(final ECLParser.TypetokenContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTypetoken");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTypetoken(final ECLParser.TypetokenContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTypetoken");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTypetokenset(final ECLParser.TypetokensetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTypetokenset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTypetokenset(final ECLParser.TypetokensetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTypetokenset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterSynonym(final ECLParser.SynonymContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterSynonym");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitSynonym(final ECLParser.SynonymContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitSynonym");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterFullyspecifiedname(final ECLParser.FullyspecifiednameContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterFullyspecifiedname");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitFullyspecifiedname(final ECLParser.FullyspecifiednameContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitFullyspecifiedname");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDefinition(final ECLParser.DefinitionContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDefinition");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDefinition(final ECLParser.DefinitionContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDefinition");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDialectfilter(final ECLParser.DialectfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDialectfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDialectfilter(final ECLParser.DialectfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDialectfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDialectidfilter(final ECLParser.DialectidfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDialectidfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDialectidfilter(final ECLParser.DialectidfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDialectidfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDialectid(final ECLParser.DialectidContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDialectid");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDialectid(final ECLParser.DialectidContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDialectid");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDialectaliasfilter(final ECLParser.DialectaliasfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDialectaliasfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDialectaliasfilter(final ECLParser.DialectaliasfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDialectaliasfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDialect(final ECLParser.DialectContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDialect");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDialect(final ECLParser.DialectContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDialect");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDialectalias(final ECLParser.DialectaliasContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDialectalias");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDialectalias(final ECLParser.DialectaliasContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDialectalias");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDialectaliasset(final ECLParser.DialectaliassetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDialectaliasset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDialectaliasset(final ECLParser.DialectaliassetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDialectaliasset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDialectidset(final ECLParser.DialectidsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDialectidset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDialectidset(final ECLParser.DialectidsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDialectidset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterAcceptabilityset(final ECLParser.AcceptabilitysetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterAcceptabilityset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitAcceptabilityset(final ECLParser.AcceptabilitysetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitAcceptabilityset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterAcceptabilityconceptreferenceset(
    final ECLParser.AcceptabilityconceptreferencesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterAcceptabilityconceptreferenceset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitAcceptabilityconceptreferenceset(
    final ECLParser.AcceptabilityconceptreferencesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitAcceptabilityconceptreferenceset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterAcceptabilitytokenset(final ECLParser.AcceptabilitytokensetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterAcceptabilitytokenset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitAcceptabilitytokenset(final ECLParser.AcceptabilitytokensetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitAcceptabilitytokenset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterAcceptabilitytoken(final ECLParser.AcceptabilitytokenContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterAcceptabilitytoken");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitAcceptabilitytoken(final ECLParser.AcceptabilitytokenContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitAcceptabilitytoken");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterAcceptable(final ECLParser.AcceptableContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterAcceptable");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitAcceptable(final ECLParser.AcceptableContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitAcceptable");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterPreferred(final ECLParser.PreferredContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterPreferred");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitPreferred(final ECLParser.PreferredContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitPreferred");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterConceptfilterconstraint(final ECLParser.ConceptfilterconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterConceptfilterconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitConceptfilterconstraint(final ECLParser.ConceptfilterconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitConceptfilterconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterConceptfilter(final ECLParser.ConceptfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterConceptfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitConceptfilter(final ECLParser.ConceptfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitConceptfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDefinitionstatusfilter(final ECLParser.DefinitionstatusfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDefinitionstatusfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDefinitionstatusfilter(final ECLParser.DefinitionstatusfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDefinitionstatusfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDefinitionstatusidfilter(final ECLParser.DefinitionstatusidfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDefinitionstatusidfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDefinitionstatusidfilter(final ECLParser.DefinitionstatusidfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDefinitionstatusidfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDefinitionstatusidkeyword(final ECLParser.DefinitionstatusidkeywordContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDefinitionstatusidkeyword");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDefinitionstatusidkeyword(final ECLParser.DefinitionstatusidkeywordContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDefinitionstatusidkeyword");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDefinitionstatustokenfilter(
    final ECLParser.DefinitionstatustokenfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDefinitionstatustokenfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDefinitionstatustokenfilter(
    final ECLParser.DefinitionstatustokenfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDefinitionstatustokenfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDefinitionstatuskeyword(final ECLParser.DefinitionstatuskeywordContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDefinitionstatuskeyword");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDefinitionstatuskeyword(final ECLParser.DefinitionstatuskeywordContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDefinitionstatuskeyword");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDefinitionstatustoken(final ECLParser.DefinitionstatustokenContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDefinitionstatustoken");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDefinitionstatustoken(final ECLParser.DefinitionstatustokenContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDefinitionstatustoken");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDefinitionstatustokenset(final ECLParser.DefinitionstatustokensetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDefinitionstatustokenset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDefinitionstatustokenset(final ECLParser.DefinitionstatustokensetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDefinitionstatustokenset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterPrimitivetoken(final ECLParser.PrimitivetokenContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterPrimitivetoken");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitPrimitivetoken(final ECLParser.PrimitivetokenContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitPrimitivetoken");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDefinedtoken(final ECLParser.DefinedtokenContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDefinedtoken");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDefinedtoken(final ECLParser.DefinedtokenContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDefinedtoken");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterModulefilter(final ECLParser.ModulefilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterModulefilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitModulefilter(final ECLParser.ModulefilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitModulefilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterModuleidkeyword(final ECLParser.ModuleidkeywordContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterModuleidkeyword");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitModuleidkeyword(final ECLParser.ModuleidkeywordContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitModuleidkeyword");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterEffectivetimefilter(final ECLParser.EffectivetimefilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterEffectivetimefilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitEffectivetimefilter(final ECLParser.EffectivetimefilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitEffectivetimefilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterEffectivetimekeyword(final ECLParser.EffectivetimekeywordContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterEffectivetimekeyword");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitEffectivetimekeyword(final ECLParser.EffectivetimekeywordContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitEffectivetimekeyword");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTimevalue(final ECLParser.TimevalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTimevalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTimevalue(final ECLParser.TimevalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTimevalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTimevalueset(final ECLParser.TimevaluesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTimevalueset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTimevalueset(final ECLParser.TimevaluesetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTimevalueset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterYear(final ECLParser.YearContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterYear");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitYear(final ECLParser.YearContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitYear");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterMonth(final ECLParser.MonthContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterMonth");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitMonth(final ECLParser.MonthContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitMonth");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDay(final ECLParser.DayContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDay");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDay(final ECLParser.DayContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDay");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterActivefilter(final ECLParser.ActivefilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterActivefilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitActivefilter(final ECLParser.ActivefilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitActivefilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterActivekeyword(final ECLParser.ActivekeywordContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterActivekeyword");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitActivekeyword(final ECLParser.ActivekeywordContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitActivekeyword");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterActivevalue(final ECLParser.ActivevalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterActivevalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitActivevalue(final ECLParser.ActivevalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitActivevalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterActivetruevalue(final ECLParser.ActivetruevalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterActivetruevalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitActivetruevalue(final ECLParser.ActivetruevalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitActivetruevalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterActivefalsevalue(final ECLParser.ActivefalsevalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterActivefalsevalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitActivefalsevalue(final ECLParser.ActivefalsevalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitActivefalsevalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterMemberfilterconstraint(final ECLParser.MemberfilterconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterMemberfilterconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitMemberfilterconstraint(final ECLParser.MemberfilterconstraintContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitMemberfilterconstraint");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterMemberfilter(final ECLParser.MemberfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterMemberfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitMemberfilter(final ECLParser.MemberfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitMemberfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterMemberfieldfilter(final ECLParser.MemberfieldfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterMemberfieldfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitMemberfieldfilter(final ECLParser.MemberfieldfilterContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitMemberfieldfilter");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterHistorysupplement(final ECLParser.HistorysupplementContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterHistorysupplement");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitHistorysupplement(final ECLParser.HistorysupplementContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitHistorysupplement");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterHistorykeyword(final ECLParser.HistorykeywordContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterHistorykeyword");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitHistorykeyword(final ECLParser.HistorykeywordContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitHistorykeyword");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterHistoryprofilesuffix(final ECLParser.HistoryprofilesuffixContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterHistoryprofilesuffix");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitHistoryprofilesuffix(final ECLParser.HistoryprofilesuffixContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitHistoryprofilesuffix");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterHistoryminimumsuffix(final ECLParser.HistoryminimumsuffixContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterHistoryminimumsuffix");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitHistoryminimumsuffix(final ECLParser.HistoryminimumsuffixContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitHistoryminimumsuffix");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterHistorymoderatesuffix(final ECLParser.HistorymoderatesuffixContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterHistorymoderatesuffix");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitHistorymoderatesuffix(final ECLParser.HistorymoderatesuffixContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitHistorymoderatesuffix");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterHistorymaximumsuffix(final ECLParser.HistorymaximumsuffixContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterHistorymaximumsuffix");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitHistorymaximumsuffix(final ECLParser.HistorymaximumsuffixContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitHistorymaximumsuffix");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterHistorysubset(final ECLParser.HistorysubsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterHistorysubset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitHistorysubset(final ECLParser.HistorysubsetContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitHistorysubset");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterNumericvalue(final ECLParser.NumericvalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterNumericvalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitNumericvalue(final ECLParser.NumericvalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitNumericvalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterStringvalue(final ECLParser.StringvalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterStringvalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitStringvalue(final ECLParser.StringvalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitStringvalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterIntegervalue(final ECLParser.IntegervalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterIntegervalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitIntegervalue(final ECLParser.IntegervalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitIntegervalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDecimalvalue(final ECLParser.DecimalvalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDecimalvalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDecimalvalue(final ECLParser.DecimalvalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDecimalvalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterBooleanvalue(final ECLParser.BooleanvalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterBooleanvalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitBooleanvalue(final ECLParser.BooleanvalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitBooleanvalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterTrue_1(final ECLParser.True_1Context ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterTrue_1");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitTrue_1(final ECLParser.True_1Context ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitTrue_1");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterFalse_1(final ECLParser.False_1Context ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterFalse_1");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitFalse_1(final ECLParser.False_1Context ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitFalse_1");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterNonnegativeintegervalue(final ECLParser.NonnegativeintegervalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterNonnegativeintegervalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitNonnegativeintegervalue(final ECLParser.NonnegativeintegervalueContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitNonnegativeintegervalue");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterSctid(final ECLParser.SctidContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterSctid");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitSctid(final ECLParser.SctidContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitSctid");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterWs(final ECLParser.WsContext ctx) {// Too noisy, not logging
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitWs(final ECLParser.WsContext ctx) {// Too noisy, not logging
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterMws(final ECLParser.MwsContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterMws");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitMws(final ECLParser.MwsContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitMws");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterComment(final ECLParser.CommentContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterComment");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitComment(final ECLParser.CommentContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitComment");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterNonstarchar(final ECLParser.NonstarcharContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterNonstarchar");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitNonstarchar(final ECLParser.NonstarcharContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitNonstarchar");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterStarwithnonfslash(final ECLParser.StarwithnonfslashContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterStarwithnonfslash");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitStarwithnonfslash(final ECLParser.StarwithnonfslashContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitStarwithnonfslash");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterNonfslash(final ECLParser.NonfslashContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterNonfslash");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitNonfslash(final ECLParser.NonfslashContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitNonfslash");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterSp(final ECLParser.SpContext ctx) {
    // Too noisy, not logging
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitSp(final ECLParser.SpContext ctx) {
    // Too noisy, not logging
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterHtab(final ECLParser.HtabContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterHtab");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitHtab(final ECLParser.HtabContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitHtab");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterCr(final ECLParser.CrContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterCr");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitCr(final ECLParser.CrContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitCr");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterLf(final ECLParser.LfContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterLf");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitLf(final ECLParser.LfContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitLf");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterQm(final ECLParser.QmContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterQm");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitQm(final ECLParser.QmContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitQm");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterBs(final ECLParser.BsContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterBs");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitBs(final ECLParser.BsContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitBs");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterStar(final ECLParser.StarContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterStar");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitStar(final ECLParser.StarContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitStar");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDigit(final ECLParser.DigitContext ctx) {
    // Too noisy, not logging
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDigit(final ECLParser.DigitContext ctx) {
    // Too noisy, not logging
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterZero(final ECLParser.ZeroContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterZero");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitZero(final ECLParser.ZeroContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitZero");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDigitnonzero(final ECLParser.DigitnonzeroContext ctx) {
    // Too noisy, not logging
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDigitnonzero(final ECLParser.DigitnonzeroContext ctx) {
    // Too noisy, not logging
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterNonwsnonpipe(final ECLParser.NonwsnonpipeContext ctx) {// Too noisy, not logging
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitNonwsnonpipe(final ECLParser.NonwsnonpipeContext ctx) {// Too noisy, not logging
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterAnynonescapedchar(final ECLParser.AnynonescapedcharContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterAnynonescapedchar");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitAnynonescapedchar(final ECLParser.AnynonescapedcharContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitAnynonescapedchar");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterCodechar(final ECLParser.CodecharContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterCodechar");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitCodechar(final ECLParser.CodecharContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitCodechar");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterEscapedchar(final ECLParser.EscapedcharContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterEscapedchar");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitEscapedchar(final ECLParser.EscapedcharContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitEscapedchar");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterEscapedwildchar(final ECLParser.EscapedwildcharContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterEscapedwildchar");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitEscapedwildchar(final ECLParser.EscapedwildcharContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitEscapedwildchar");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterNonwsnonescapedchar(final ECLParser.NonwsnonescapedcharContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterNonwsnonescapedchar");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitNonwsnonescapedchar(final ECLParser.NonwsnonescapedcharContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitNonwsnonescapedchar");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterAlpha(final ECLParser.AlphaContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterAlpha");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitAlpha(final ECLParser.AlphaContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitAlpha");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterDash(final ECLParser.DashContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("enterDash");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitDash(final ECLParser.DashContext ctx) {
    if (log.isTraceEnabled()) {
      log.trace("exitDash");
      log.trace(ctx.getText());
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void enterEveryRule(final ParserRuleContext ctx) {// Too noisy, not logging
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void exitEveryRule(final ParserRuleContext ctx) {// Too noisy, not logging
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void visitTerminal(final TerminalNode node) {// Too noisy, not logging
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The default implementation does nothing.
   * </p>
   */
  @Override
  public void visitErrorNode(final ErrorNode node) {
    if (log.isTraceEnabled()) {
      log.trace("visitErrorNode");
    }
  }
}
