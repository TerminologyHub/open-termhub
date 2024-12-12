/*
 * Copyright 2024 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.ecl;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link EclParser}.
 */
public interface EclListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link EclParser#expressionconstraint}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterExpressionconstraint(EclParser.ExpressionconstraintContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#expressionconstraint}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitExpressionconstraint(EclParser.ExpressionconstraintContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#refinedexpressionconstraint}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterRefinedexpressionconstraint(EclParser.RefinedexpressionconstraintContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#refinedexpressionconstraint}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitRefinedexpressionconstraint(EclParser.RefinedexpressionconstraintContext ctx);

	/**
	 * Enter a parse tree produced by
	 * {@link EclParser#compoundexpressionconstraint}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterCompoundexpressionconstraint(EclParser.CompoundexpressionconstraintContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#compoundexpressionconstraint}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitCompoundexpressionconstraint(EclParser.CompoundexpressionconstraintContext ctx);

	/**
	 * Enter a parse tree produced by
	 * {@link EclParser#conjunctionexpressionconstraint}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterConjunctionexpressionconstraint(EclParser.ConjunctionexpressionconstraintContext ctx);

	/**
	 * Exit a parse tree produced by
	 * {@link EclParser#conjunctionexpressionconstraint}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitConjunctionexpressionconstraint(EclParser.ConjunctionexpressionconstraintContext ctx);

	/**
	 * Enter a parse tree produced by
	 * {@link EclParser#disjunctionexpressionconstraint}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterDisjunctionexpressionconstraint(EclParser.DisjunctionexpressionconstraintContext ctx);

	/**
	 * Exit a parse tree produced by
	 * {@link EclParser#disjunctionexpressionconstraint}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitDisjunctionexpressionconstraint(EclParser.DisjunctionexpressionconstraintContext ctx);

	/**
	 * Enter a parse tree produced by
	 * {@link EclParser#exclusionexpressionconstraint}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterExclusionexpressionconstraint(EclParser.ExclusionexpressionconstraintContext ctx);

	/**
	 * Exit a parse tree produced by
	 * {@link EclParser#exclusionexpressionconstraint}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitExclusionexpressionconstraint(EclParser.ExclusionexpressionconstraintContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#subexpressionconstraint}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterSubexpressionconstraint(EclParser.SubexpressionconstraintContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#subexpressionconstraint}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitSubexpressionconstraint(EclParser.SubexpressionconstraintContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#eclfocusconcept}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterEclfocusconcept(EclParser.EclfocusconceptContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#eclfocusconcept}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitEclfocusconcept(EclParser.EclfocusconceptContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#memberof}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterMemberof(EclParser.MemberofContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#memberof}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitMemberof(EclParser.MemberofContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#eclconceptreference}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterEclconceptreference(EclParser.EclconceptreferenceContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#eclconceptreference}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitEclconceptreference(EclParser.EclconceptreferenceContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#conceptid}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterConceptid(EclParser.ConceptidContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#conceptid}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitConceptid(EclParser.ConceptidContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#term}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterTerm(EclParser.TermContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#term}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitTerm(EclParser.TermContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#wildcard}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterWildcard(EclParser.WildcardContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#wildcard}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitWildcard(EclParser.WildcardContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#constraintoperator}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterConstraintoperator(EclParser.ConstraintoperatorContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#constraintoperator}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitConstraintoperator(EclParser.ConstraintoperatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#descendantof}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterDescendantof(EclParser.DescendantofContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#descendantof}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitDescendantof(EclParser.DescendantofContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#descendantorselfof}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterDescendantorselfof(EclParser.DescendantorselfofContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#descendantorselfof}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitDescendantorselfof(EclParser.DescendantorselfofContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#childof}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterChildof(EclParser.ChildofContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#childof}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitChildof(EclParser.ChildofContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#ancestorof}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterAncestorof(EclParser.AncestorofContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#ancestorof}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitAncestorof(EclParser.AncestorofContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#ancestororselfof}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterAncestororselfof(EclParser.AncestororselfofContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#ancestororselfof}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitAncestororselfof(EclParser.AncestororselfofContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#parentof}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterParentof(EclParser.ParentofContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#parentof}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitParentof(EclParser.ParentofContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#conjunction}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterConjunction(EclParser.ConjunctionContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#conjunction}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitConjunction(EclParser.ConjunctionContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#disjunction}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterDisjunction(EclParser.DisjunctionContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#disjunction}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitDisjunction(EclParser.DisjunctionContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#exclusion}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterExclusion(EclParser.ExclusionContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#exclusion}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitExclusion(EclParser.ExclusionContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#eclrefinement}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterEclrefinement(EclParser.EclrefinementContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#eclrefinement}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitEclrefinement(EclParser.EclrefinementContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#conjunctionrefinementset}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterConjunctionrefinementset(EclParser.ConjunctionrefinementsetContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#conjunctionrefinementset}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitConjunctionrefinementset(EclParser.ConjunctionrefinementsetContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#disjunctionrefinementset}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterDisjunctionrefinementset(EclParser.DisjunctionrefinementsetContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#disjunctionrefinementset}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitDisjunctionrefinementset(EclParser.DisjunctionrefinementsetContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#subrefinement}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterSubrefinement(EclParser.SubrefinementContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#subrefinement}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitSubrefinement(EclParser.SubrefinementContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#eclattributeset}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterEclattributeset(EclParser.EclattributesetContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#eclattributeset}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitEclattributeset(EclParser.EclattributesetContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#conjunctionattributeset}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterConjunctionattributeset(EclParser.ConjunctionattributesetContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#conjunctionattributeset}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitConjunctionattributeset(EclParser.ConjunctionattributesetContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#disjunctionattributeset}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterDisjunctionattributeset(EclParser.DisjunctionattributesetContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#disjunctionattributeset}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitDisjunctionattributeset(EclParser.DisjunctionattributesetContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#subattributeset}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterSubattributeset(EclParser.SubattributesetContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#subattributeset}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitSubattributeset(EclParser.SubattributesetContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#eclattributegroup}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterEclattributegroup(EclParser.EclattributegroupContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#eclattributegroup}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitEclattributegroup(EclParser.EclattributegroupContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#eclattribute}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterEclattribute(EclParser.EclattributeContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#eclattribute}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitEclattribute(EclParser.EclattributeContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#cardinality}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterCardinality(EclParser.CardinalityContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#cardinality}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitCardinality(EclParser.CardinalityContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#minvalue}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterMinvalue(EclParser.MinvalueContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#minvalue}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitMinvalue(EclParser.MinvalueContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#to}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterTo(EclParser.ToContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#to}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitTo(EclParser.ToContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#maxvalue}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterMaxvalue(EclParser.MaxvalueContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#maxvalue}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitMaxvalue(EclParser.MaxvalueContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#many}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterMany(EclParser.ManyContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#many}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitMany(EclParser.ManyContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#reverseflag}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterReverseflag(EclParser.ReverseflagContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#reverseflag}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitReverseflag(EclParser.ReverseflagContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#eclattributename}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterEclattributename(EclParser.EclattributenameContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#eclattributename}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitEclattributename(EclParser.EclattributenameContext ctx);

	/**
	 * Enter a parse tree produced by
	 * {@link EclParser#expressioncomparisonoperator}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterExpressioncomparisonoperator(EclParser.ExpressioncomparisonoperatorContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#expressioncomparisonoperator}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitExpressioncomparisonoperator(EclParser.ExpressioncomparisonoperatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#numericcomparisonoperator}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterNumericcomparisonoperator(EclParser.NumericcomparisonoperatorContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#numericcomparisonoperator}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitNumericcomparisonoperator(EclParser.NumericcomparisonoperatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#stringcomparisonoperator}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterStringcomparisonoperator(EclParser.StringcomparisonoperatorContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#stringcomparisonoperator}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitStringcomparisonoperator(EclParser.StringcomparisonoperatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#numericvalue}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterNumericvalue(EclParser.NumericvalueContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#numericvalue}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitNumericvalue(EclParser.NumericvalueContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#stringvalue}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterStringvalue(EclParser.StringvalueContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#stringvalue}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitStringvalue(EclParser.StringvalueContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#integervalue}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterIntegervalue(EclParser.IntegervalueContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#integervalue}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitIntegervalue(EclParser.IntegervalueContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#decimalvalue}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterDecimalvalue(EclParser.DecimalvalueContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#decimalvalue}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitDecimalvalue(EclParser.DecimalvalueContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#nonnegativeintegervalue}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterNonnegativeintegervalue(EclParser.NonnegativeintegervalueContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#nonnegativeintegervalue}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitNonnegativeintegervalue(EclParser.NonnegativeintegervalueContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#sctid}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterSctid(EclParser.SctidContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#sctid}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitSctid(EclParser.SctidContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#ws}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterWs(EclParser.WsContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#ws}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitWs(EclParser.WsContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#mws}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterMws(EclParser.MwsContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#mws}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitMws(EclParser.MwsContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#comment}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterComment(EclParser.CommentContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#comment}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitComment(EclParser.CommentContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#nonstarchar}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterNonstarchar(EclParser.NonstarcharContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#nonstarchar}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitNonstarchar(EclParser.NonstarcharContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#starwithnonfslash}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterStarwithnonfslash(EclParser.StarwithnonfslashContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#starwithnonfslash}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitStarwithnonfslash(EclParser.StarwithnonfslashContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#nonfslash}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterNonfslash(EclParser.NonfslashContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#nonfslash}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitNonfslash(EclParser.NonfslashContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#sp}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterSp(EclParser.SpContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#sp}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitSp(EclParser.SpContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#htab}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterHtab(EclParser.HtabContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#htab}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitHtab(EclParser.HtabContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#cr}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterCr(EclParser.CrContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#cr}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitCr(EclParser.CrContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#lf}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterLf(EclParser.LfContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#lf}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitLf(EclParser.LfContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#qm}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterQm(EclParser.QmContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#qm}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitQm(EclParser.QmContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#bs}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterBs(EclParser.BsContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#bs}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitBs(EclParser.BsContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#digit}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterDigit(EclParser.DigitContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#digit}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitDigit(EclParser.DigitContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#zero}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterZero(EclParser.ZeroContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#zero}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitZero(EclParser.ZeroContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#digitnonzero}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterDigitnonzero(EclParser.DigitnonzeroContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#digitnonzero}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitDigitnonzero(EclParser.DigitnonzeroContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#nonwsnonpipe}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterNonwsnonpipe(EclParser.NonwsnonpipeContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#nonwsnonpipe}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitNonwsnonpipe(EclParser.NonwsnonpipeContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#anynonescapedchar}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterAnynonescapedchar(EclParser.AnynonescapedcharContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#anynonescapedchar}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitAnynonescapedchar(EclParser.AnynonescapedcharContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#codechar}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterCodechar(EclParser.CodecharContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#codechar}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitCodechar(EclParser.CodecharContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#escapedchar}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterEscapedchar(EclParser.EscapedcharContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#escapedchar}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitEscapedchar(EclParser.EscapedcharContext ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#utf8_2}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterUtf8_2(EclParser.Utf8_2Context ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#utf8_2}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitUtf8_2(EclParser.Utf8_2Context ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#utf8_3}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterUtf8_3(EclParser.Utf8_3Context ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#utf8_3}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitUtf8_3(EclParser.Utf8_3Context ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#utf8_4}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterUtf8_4(EclParser.Utf8_4Context ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#utf8_4}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitUtf8_4(EclParser.Utf8_4Context ctx);

	/**
	 * Enter a parse tree produced by {@link EclParser#utf8_tail}.
	 * 
	 * @param ctx the parse tree
	 */
	void enterUtf8_tail(EclParser.Utf8_tailContext ctx);

	/**
	 * Exit a parse tree produced by {@link EclParser#utf8_tail}.
	 * 
	 * @param ctx the parse tree
	 */
	void exitUtf8_tail(EclParser.Utf8_tailContext ctx);
}