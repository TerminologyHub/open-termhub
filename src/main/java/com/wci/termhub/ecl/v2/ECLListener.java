/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.ecl.v2;// Generated from ECL.antlr by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ECLParser}.
 */
public interface ECLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ECLParser#expressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterExpressionconstraint(ECLParser.ExpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#expressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitExpressionconstraint(ECLParser.ExpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#refinedexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterRefinedexpressionconstraint(ECLParser.RefinedexpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#refinedexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitRefinedexpressionconstraint(ECLParser.RefinedexpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#compoundexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterCompoundexpressionconstraint(ECLParser.CompoundexpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#compoundexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitCompoundexpressionconstraint(ECLParser.CompoundexpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#conjunctionexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterConjunctionexpressionconstraint(ECLParser.ConjunctionexpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#conjunctionexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitConjunctionexpressionconstraint(ECLParser.ConjunctionexpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#disjunctionexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterDisjunctionexpressionconstraint(ECLParser.DisjunctionexpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#disjunctionexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitDisjunctionexpressionconstraint(ECLParser.DisjunctionexpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#exclusionexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterExclusionexpressionconstraint(ECLParser.ExclusionexpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#exclusionexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitExclusionexpressionconstraint(ECLParser.ExclusionexpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#dottedexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterDottedexpressionconstraint(ECLParser.DottedexpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#dottedexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitDottedexpressionconstraint(ECLParser.DottedexpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#dottedexpressionattribute}.
	 * @param ctx the parse tree
	 */
	void enterDottedexpressionattribute(ECLParser.DottedexpressionattributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#dottedexpressionattribute}.
	 * @param ctx the parse tree
	 */
	void exitDottedexpressionattribute(ECLParser.DottedexpressionattributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#subexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterSubexpressionconstraint(ECLParser.SubexpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#subexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitSubexpressionconstraint(ECLParser.SubexpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#eclfocusconcept}.
	 * @param ctx the parse tree
	 */
	void enterEclfocusconcept(ECLParser.EclfocusconceptContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#eclfocusconcept}.
	 * @param ctx the parse tree
	 */
	void exitEclfocusconcept(ECLParser.EclfocusconceptContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#dot}.
	 * @param ctx the parse tree
	 */
	void enterDot(ECLParser.DotContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#dot}.
	 * @param ctx the parse tree
	 */
	void exitDot(ECLParser.DotContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#memberof}.
	 * @param ctx the parse tree
	 */
	void enterMemberof(ECLParser.MemberofContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#memberof}.
	 * @param ctx the parse tree
	 */
	void exitMemberof(ECLParser.MemberofContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#refsetfieldset}.
	 * @param ctx the parse tree
	 */
	void enterRefsetfieldset(ECLParser.RefsetfieldsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#refsetfieldset}.
	 * @param ctx the parse tree
	 */
	void exitRefsetfieldset(ECLParser.RefsetfieldsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#refsetfield}.
	 * @param ctx the parse tree
	 */
	void enterRefsetfield(ECLParser.RefsetfieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#refsetfield}.
	 * @param ctx the parse tree
	 */
	void exitRefsetfield(ECLParser.RefsetfieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#refsetfieldname}.
	 * @param ctx the parse tree
	 */
	void enterRefsetfieldname(ECLParser.RefsetfieldnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#refsetfieldname}.
	 * @param ctx the parse tree
	 */
	void exitRefsetfieldname(ECLParser.RefsetfieldnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#refsetfieldref}.
	 * @param ctx the parse tree
	 */
	void enterRefsetfieldref(ECLParser.RefsetfieldrefContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#refsetfieldref}.
	 * @param ctx the parse tree
	 */
	void exitRefsetfieldref(ECLParser.RefsetfieldrefContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#eclconceptreference}.
	 * @param ctx the parse tree
	 */
	void enterEclconceptreference(ECLParser.EclconceptreferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#eclconceptreference}.
	 * @param ctx the parse tree
	 */
	void exitEclconceptreference(ECLParser.EclconceptreferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#eclconceptreferenceset}.
	 * @param ctx the parse tree
	 */
	void enterEclconceptreferenceset(ECLParser.EclconceptreferencesetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#eclconceptreferenceset}.
	 * @param ctx the parse tree
	 */
	void exitEclconceptreferenceset(ECLParser.EclconceptreferencesetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#conceptid}.
	 * @param ctx the parse tree
	 */
	void enterConceptid(ECLParser.ConceptidContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#conceptid}.
	 * @param ctx the parse tree
	 */
	void exitConceptid(ECLParser.ConceptidContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(ECLParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(ECLParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#wildcard}.
	 * @param ctx the parse tree
	 */
	void enterWildcard(ECLParser.WildcardContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#wildcard}.
	 * @param ctx the parse tree
	 */
	void exitWildcard(ECLParser.WildcardContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#constraintoperator}.
	 * @param ctx the parse tree
	 */
	void enterConstraintoperator(ECLParser.ConstraintoperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#constraintoperator}.
	 * @param ctx the parse tree
	 */
	void exitConstraintoperator(ECLParser.ConstraintoperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#descendantof}.
	 * @param ctx the parse tree
	 */
	void enterDescendantof(ECLParser.DescendantofContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#descendantof}.
	 * @param ctx the parse tree
	 */
	void exitDescendantof(ECLParser.DescendantofContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#descendantorselfof}.
	 * @param ctx the parse tree
	 */
	void enterDescendantorselfof(ECLParser.DescendantorselfofContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#descendantorselfof}.
	 * @param ctx the parse tree
	 */
	void exitDescendantorselfof(ECLParser.DescendantorselfofContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#childof}.
	 * @param ctx the parse tree
	 */
	void enterChildof(ECLParser.ChildofContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#childof}.
	 * @param ctx the parse tree
	 */
	void exitChildof(ECLParser.ChildofContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#childorselfof}.
	 * @param ctx the parse tree
	 */
	void enterChildorselfof(ECLParser.ChildorselfofContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#childorselfof}.
	 * @param ctx the parse tree
	 */
	void exitChildorselfof(ECLParser.ChildorselfofContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#ancestorof}.
	 * @param ctx the parse tree
	 */
	void enterAncestorof(ECLParser.AncestorofContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#ancestorof}.
	 * @param ctx the parse tree
	 */
	void exitAncestorof(ECLParser.AncestorofContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#ancestororselfof}.
	 * @param ctx the parse tree
	 */
	void enterAncestororselfof(ECLParser.AncestororselfofContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#ancestororselfof}.
	 * @param ctx the parse tree
	 */
	void exitAncestororselfof(ECLParser.AncestororselfofContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#parentof}.
	 * @param ctx the parse tree
	 */
	void enterParentof(ECLParser.ParentofContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#parentof}.
	 * @param ctx the parse tree
	 */
	void exitParentof(ECLParser.ParentofContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#parentorselfof}.
	 * @param ctx the parse tree
	 */
	void enterParentorselfof(ECLParser.ParentorselfofContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#parentorselfof}.
	 * @param ctx the parse tree
	 */
	void exitParentorselfof(ECLParser.ParentorselfofContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#conjunction}.
	 * @param ctx the parse tree
	 */
	void enterConjunction(ECLParser.ConjunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#conjunction}.
	 * @param ctx the parse tree
	 */
	void exitConjunction(ECLParser.ConjunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#disjunction}.
	 * @param ctx the parse tree
	 */
	void enterDisjunction(ECLParser.DisjunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#disjunction}.
	 * @param ctx the parse tree
	 */
	void exitDisjunction(ECLParser.DisjunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#exclusion}.
	 * @param ctx the parse tree
	 */
	void enterExclusion(ECLParser.ExclusionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#exclusion}.
	 * @param ctx the parse tree
	 */
	void exitExclusion(ECLParser.ExclusionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#eclrefinement}.
	 * @param ctx the parse tree
	 */
	void enterEclrefinement(ECLParser.EclrefinementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#eclrefinement}.
	 * @param ctx the parse tree
	 */
	void exitEclrefinement(ECLParser.EclrefinementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#conjunctionrefinementset}.
	 * @param ctx the parse tree
	 */
	void enterConjunctionrefinementset(ECLParser.ConjunctionrefinementsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#conjunctionrefinementset}.
	 * @param ctx the parse tree
	 */
	void exitConjunctionrefinementset(ECLParser.ConjunctionrefinementsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#disjunctionrefinementset}.
	 * @param ctx the parse tree
	 */
	void enterDisjunctionrefinementset(ECLParser.DisjunctionrefinementsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#disjunctionrefinementset}.
	 * @param ctx the parse tree
	 */
	void exitDisjunctionrefinementset(ECLParser.DisjunctionrefinementsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#subrefinement}.
	 * @param ctx the parse tree
	 */
	void enterSubrefinement(ECLParser.SubrefinementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#subrefinement}.
	 * @param ctx the parse tree
	 */
	void exitSubrefinement(ECLParser.SubrefinementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#eclattributeset}.
	 * @param ctx the parse tree
	 */
	void enterEclattributeset(ECLParser.EclattributesetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#eclattributeset}.
	 * @param ctx the parse tree
	 */
	void exitEclattributeset(ECLParser.EclattributesetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#conjunctionattributeset}.
	 * @param ctx the parse tree
	 */
	void enterConjunctionattributeset(ECLParser.ConjunctionattributesetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#conjunctionattributeset}.
	 * @param ctx the parse tree
	 */
	void exitConjunctionattributeset(ECLParser.ConjunctionattributesetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#disjunctionattributeset}.
	 * @param ctx the parse tree
	 */
	void enterDisjunctionattributeset(ECLParser.DisjunctionattributesetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#disjunctionattributeset}.
	 * @param ctx the parse tree
	 */
	void exitDisjunctionattributeset(ECLParser.DisjunctionattributesetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#subattributeset}.
	 * @param ctx the parse tree
	 */
	void enterSubattributeset(ECLParser.SubattributesetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#subattributeset}.
	 * @param ctx the parse tree
	 */
	void exitSubattributeset(ECLParser.SubattributesetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#eclattributegroup}.
	 * @param ctx the parse tree
	 */
	void enterEclattributegroup(ECLParser.EclattributegroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#eclattributegroup}.
	 * @param ctx the parse tree
	 */
	void exitEclattributegroup(ECLParser.EclattributegroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#eclattribute}.
	 * @param ctx the parse tree
	 */
	void enterEclattribute(ECLParser.EclattributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#eclattribute}.
	 * @param ctx the parse tree
	 */
	void exitEclattribute(ECLParser.EclattributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#cardinality}.
	 * @param ctx the parse tree
	 */
	void enterCardinality(ECLParser.CardinalityContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#cardinality}.
	 * @param ctx the parse tree
	 */
	void exitCardinality(ECLParser.CardinalityContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#minvalue}.
	 * @param ctx the parse tree
	 */
	void enterMinvalue(ECLParser.MinvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#minvalue}.
	 * @param ctx the parse tree
	 */
	void exitMinvalue(ECLParser.MinvalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#to}.
	 * @param ctx the parse tree
	 */
	void enterTo(ECLParser.ToContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#to}.
	 * @param ctx the parse tree
	 */
	void exitTo(ECLParser.ToContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#maxvalue}.
	 * @param ctx the parse tree
	 */
	void enterMaxvalue(ECLParser.MaxvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#maxvalue}.
	 * @param ctx the parse tree
	 */
	void exitMaxvalue(ECLParser.MaxvalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#many}.
	 * @param ctx the parse tree
	 */
	void enterMany(ECLParser.ManyContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#many}.
	 * @param ctx the parse tree
	 */
	void exitMany(ECLParser.ManyContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#reverseflag}.
	 * @param ctx the parse tree
	 */
	void enterReverseflag(ECLParser.ReverseflagContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#reverseflag}.
	 * @param ctx the parse tree
	 */
	void exitReverseflag(ECLParser.ReverseflagContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#eclattributename}.
	 * @param ctx the parse tree
	 */
	void enterEclattributename(ECLParser.EclattributenameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#eclattributename}.
	 * @param ctx the parse tree
	 */
	void exitEclattributename(ECLParser.EclattributenameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#expressioncomparisonoperator}.
	 * @param ctx the parse tree
	 */
	void enterExpressioncomparisonoperator(ECLParser.ExpressioncomparisonoperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#expressioncomparisonoperator}.
	 * @param ctx the parse tree
	 */
	void exitExpressioncomparisonoperator(ECLParser.ExpressioncomparisonoperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#numericcomparisonoperator}.
	 * @param ctx the parse tree
	 */
	void enterNumericcomparisonoperator(ECLParser.NumericcomparisonoperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#numericcomparisonoperator}.
	 * @param ctx the parse tree
	 */
	void exitNumericcomparisonoperator(ECLParser.NumericcomparisonoperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#timecomparisonoperator}.
	 * @param ctx the parse tree
	 */
	void enterTimecomparisonoperator(ECLParser.TimecomparisonoperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#timecomparisonoperator}.
	 * @param ctx the parse tree
	 */
	void exitTimecomparisonoperator(ECLParser.TimecomparisonoperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#stringcomparisonoperator}.
	 * @param ctx the parse tree
	 */
	void enterStringcomparisonoperator(ECLParser.StringcomparisonoperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#stringcomparisonoperator}.
	 * @param ctx the parse tree
	 */
	void exitStringcomparisonoperator(ECLParser.StringcomparisonoperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#booleancomparisonoperator}.
	 * @param ctx the parse tree
	 */
	void enterBooleancomparisonoperator(ECLParser.BooleancomparisonoperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#booleancomparisonoperator}.
	 * @param ctx the parse tree
	 */
	void exitBooleancomparisonoperator(ECLParser.BooleancomparisonoperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#descriptionfilterconstraint}.
	 * @param ctx the parse tree
	 */
	void enterDescriptionfilterconstraint(ECLParser.DescriptionfilterconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#descriptionfilterconstraint}.
	 * @param ctx the parse tree
	 */
	void exitDescriptionfilterconstraint(ECLParser.DescriptionfilterconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#descriptionfilter}.
	 * @param ctx the parse tree
	 */
	void enterDescriptionfilter(ECLParser.DescriptionfilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#descriptionfilter}.
	 * @param ctx the parse tree
	 */
	void exitDescriptionfilter(ECLParser.DescriptionfilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#termfilter}.
	 * @param ctx the parse tree
	 */
	void enterTermfilter(ECLParser.TermfilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#termfilter}.
	 * @param ctx the parse tree
	 */
	void exitTermfilter(ECLParser.TermfilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#termkeyword}.
	 * @param ctx the parse tree
	 */
	void enterTermkeyword(ECLParser.TermkeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#termkeyword}.
	 * @param ctx the parse tree
	 */
	void exitTermkeyword(ECLParser.TermkeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#typedsearchterm}.
	 * @param ctx the parse tree
	 */
	void enterTypedsearchterm(ECLParser.TypedsearchtermContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#typedsearchterm}.
	 * @param ctx the parse tree
	 */
	void exitTypedsearchterm(ECLParser.TypedsearchtermContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#typedsearchtermset}.
	 * @param ctx the parse tree
	 */
	void enterTypedsearchtermset(ECLParser.TypedsearchtermsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#typedsearchtermset}.
	 * @param ctx the parse tree
	 */
	void exitTypedsearchtermset(ECLParser.TypedsearchtermsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#wild}.
	 * @param ctx the parse tree
	 */
	void enterWild(ECLParser.WildContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#wild}.
	 * @param ctx the parse tree
	 */
	void exitWild(ECLParser.WildContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#match}.
	 * @param ctx the parse tree
	 */
	void enterMatch(ECLParser.MatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#match}.
	 * @param ctx the parse tree
	 */
	void exitMatch(ECLParser.MatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#matchsearchterm}.
	 * @param ctx the parse tree
	 */
	void enterMatchsearchterm(ECLParser.MatchsearchtermContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#matchsearchterm}.
	 * @param ctx the parse tree
	 */
	void exitMatchsearchterm(ECLParser.MatchsearchtermContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#matchsearchtermset}.
	 * @param ctx the parse tree
	 */
	void enterMatchsearchtermset(ECLParser.MatchsearchtermsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#matchsearchtermset}.
	 * @param ctx the parse tree
	 */
	void exitMatchsearchtermset(ECLParser.MatchsearchtermsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#wildsearchterm}.
	 * @param ctx the parse tree
	 */
	void enterWildsearchterm(ECLParser.WildsearchtermContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#wildsearchterm}.
	 * @param ctx the parse tree
	 */
	void exitWildsearchterm(ECLParser.WildsearchtermContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#wildsearchtermset}.
	 * @param ctx the parse tree
	 */
	void enterWildsearchtermset(ECLParser.WildsearchtermsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#wildsearchtermset}.
	 * @param ctx the parse tree
	 */
	void exitWildsearchtermset(ECLParser.WildsearchtermsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#languagefilter}.
	 * @param ctx the parse tree
	 */
	void enterLanguagefilter(ECLParser.LanguagefilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#languagefilter}.
	 * @param ctx the parse tree
	 */
	void exitLanguagefilter(ECLParser.LanguagefilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#language}.
	 * @param ctx the parse tree
	 */
	void enterLanguage(ECLParser.LanguageContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#language}.
	 * @param ctx the parse tree
	 */
	void exitLanguage(ECLParser.LanguageContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#languagecode}.
	 * @param ctx the parse tree
	 */
	void enterLanguagecode(ECLParser.LanguagecodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#languagecode}.
	 * @param ctx the parse tree
	 */
	void exitLanguagecode(ECLParser.LanguagecodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#languagecodeset}.
	 * @param ctx the parse tree
	 */
	void enterLanguagecodeset(ECLParser.LanguagecodesetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#languagecodeset}.
	 * @param ctx the parse tree
	 */
	void exitLanguagecodeset(ECLParser.LanguagecodesetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#typefilter}.
	 * @param ctx the parse tree
	 */
	void enterTypefilter(ECLParser.TypefilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#typefilter}.
	 * @param ctx the parse tree
	 */
	void exitTypefilter(ECLParser.TypefilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#typeidfilter}.
	 * @param ctx the parse tree
	 */
	void enterTypeidfilter(ECLParser.TypeidfilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#typeidfilter}.
	 * @param ctx the parse tree
	 */
	void exitTypeidfilter(ECLParser.TypeidfilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#typeid}.
	 * @param ctx the parse tree
	 */
	void enterTypeid(ECLParser.TypeidContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#typeid}.
	 * @param ctx the parse tree
	 */
	void exitTypeid(ECLParser.TypeidContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#typetokenfilter}.
	 * @param ctx the parse tree
	 */
	void enterTypetokenfilter(ECLParser.TypetokenfilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#typetokenfilter}.
	 * @param ctx the parse tree
	 */
	void exitTypetokenfilter(ECLParser.TypetokenfilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(ECLParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(ECLParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#typetoken}.
	 * @param ctx the parse tree
	 */
	void enterTypetoken(ECLParser.TypetokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#typetoken}.
	 * @param ctx the parse tree
	 */
	void exitTypetoken(ECLParser.TypetokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#typetokenset}.
	 * @param ctx the parse tree
	 */
	void enterTypetokenset(ECLParser.TypetokensetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#typetokenset}.
	 * @param ctx the parse tree
	 */
	void exitTypetokenset(ECLParser.TypetokensetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#synonym}.
	 * @param ctx the parse tree
	 */
	void enterSynonym(ECLParser.SynonymContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#synonym}.
	 * @param ctx the parse tree
	 */
	void exitSynonym(ECLParser.SynonymContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#fullyspecifiedname}.
	 * @param ctx the parse tree
	 */
	void enterFullyspecifiedname(ECLParser.FullyspecifiednameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#fullyspecifiedname}.
	 * @param ctx the parse tree
	 */
	void exitFullyspecifiedname(ECLParser.FullyspecifiednameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#definition}.
	 * @param ctx the parse tree
	 */
	void enterDefinition(ECLParser.DefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#definition}.
	 * @param ctx the parse tree
	 */
	void exitDefinition(ECLParser.DefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#dialectfilter}.
	 * @param ctx the parse tree
	 */
	void enterDialectfilter(ECLParser.DialectfilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#dialectfilter}.
	 * @param ctx the parse tree
	 */
	void exitDialectfilter(ECLParser.DialectfilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#dialectidfilter}.
	 * @param ctx the parse tree
	 */
	void enterDialectidfilter(ECLParser.DialectidfilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#dialectidfilter}.
	 * @param ctx the parse tree
	 */
	void exitDialectidfilter(ECLParser.DialectidfilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#dialectid}.
	 * @param ctx the parse tree
	 */
	void enterDialectid(ECLParser.DialectidContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#dialectid}.
	 * @param ctx the parse tree
	 */
	void exitDialectid(ECLParser.DialectidContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#dialectaliasfilter}.
	 * @param ctx the parse tree
	 */
	void enterDialectaliasfilter(ECLParser.DialectaliasfilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#dialectaliasfilter}.
	 * @param ctx the parse tree
	 */
	void exitDialectaliasfilter(ECLParser.DialectaliasfilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#dialect}.
	 * @param ctx the parse tree
	 */
	void enterDialect(ECLParser.DialectContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#dialect}.
	 * @param ctx the parse tree
	 */
	void exitDialect(ECLParser.DialectContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#dialectalias}.
	 * @param ctx the parse tree
	 */
	void enterDialectalias(ECLParser.DialectaliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#dialectalias}.
	 * @param ctx the parse tree
	 */
	void exitDialectalias(ECLParser.DialectaliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#dialectaliasset}.
	 * @param ctx the parse tree
	 */
	void enterDialectaliasset(ECLParser.DialectaliassetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#dialectaliasset}.
	 * @param ctx the parse tree
	 */
	void exitDialectaliasset(ECLParser.DialectaliassetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#dialectidset}.
	 * @param ctx the parse tree
	 */
	void enterDialectidset(ECLParser.DialectidsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#dialectidset}.
	 * @param ctx the parse tree
	 */
	void exitDialectidset(ECLParser.DialectidsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#acceptabilityset}.
	 * @param ctx the parse tree
	 */
	void enterAcceptabilityset(ECLParser.AcceptabilitysetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#acceptabilityset}.
	 * @param ctx the parse tree
	 */
	void exitAcceptabilityset(ECLParser.AcceptabilitysetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#acceptabilityconceptreferenceset}.
	 * @param ctx the parse tree
	 */
	void enterAcceptabilityconceptreferenceset(ECLParser.AcceptabilityconceptreferencesetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#acceptabilityconceptreferenceset}.
	 * @param ctx the parse tree
	 */
	void exitAcceptabilityconceptreferenceset(ECLParser.AcceptabilityconceptreferencesetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#acceptabilitytokenset}.
	 * @param ctx the parse tree
	 */
	void enterAcceptabilitytokenset(ECLParser.AcceptabilitytokensetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#acceptabilitytokenset}.
	 * @param ctx the parse tree
	 */
	void exitAcceptabilitytokenset(ECLParser.AcceptabilitytokensetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#acceptabilitytoken}.
	 * @param ctx the parse tree
	 */
	void enterAcceptabilitytoken(ECLParser.AcceptabilitytokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#acceptabilitytoken}.
	 * @param ctx the parse tree
	 */
	void exitAcceptabilitytoken(ECLParser.AcceptabilitytokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#acceptable}.
	 * @param ctx the parse tree
	 */
	void enterAcceptable(ECLParser.AcceptableContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#acceptable}.
	 * @param ctx the parse tree
	 */
	void exitAcceptable(ECLParser.AcceptableContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#preferred}.
	 * @param ctx the parse tree
	 */
	void enterPreferred(ECLParser.PreferredContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#preferred}.
	 * @param ctx the parse tree
	 */
	void exitPreferred(ECLParser.PreferredContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#conceptfilterconstraint}.
	 * @param ctx the parse tree
	 */
	void enterConceptfilterconstraint(ECLParser.ConceptfilterconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#conceptfilterconstraint}.
	 * @param ctx the parse tree
	 */
	void exitConceptfilterconstraint(ECLParser.ConceptfilterconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#conceptfilter}.
	 * @param ctx the parse tree
	 */
	void enterConceptfilter(ECLParser.ConceptfilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#conceptfilter}.
	 * @param ctx the parse tree
	 */
	void exitConceptfilter(ECLParser.ConceptfilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#definitionstatusfilter}.
	 * @param ctx the parse tree
	 */
	void enterDefinitionstatusfilter(ECLParser.DefinitionstatusfilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#definitionstatusfilter}.
	 * @param ctx the parse tree
	 */
	void exitDefinitionstatusfilter(ECLParser.DefinitionstatusfilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#definitionstatusidfilter}.
	 * @param ctx the parse tree
	 */
	void enterDefinitionstatusidfilter(ECLParser.DefinitionstatusidfilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#definitionstatusidfilter}.
	 * @param ctx the parse tree
	 */
	void exitDefinitionstatusidfilter(ECLParser.DefinitionstatusidfilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#definitionstatusidkeyword}.
	 * @param ctx the parse tree
	 */
	void enterDefinitionstatusidkeyword(ECLParser.DefinitionstatusidkeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#definitionstatusidkeyword}.
	 * @param ctx the parse tree
	 */
	void exitDefinitionstatusidkeyword(ECLParser.DefinitionstatusidkeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#definitionstatustokenfilter}.
	 * @param ctx the parse tree
	 */
	void enterDefinitionstatustokenfilter(ECLParser.DefinitionstatustokenfilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#definitionstatustokenfilter}.
	 * @param ctx the parse tree
	 */
	void exitDefinitionstatustokenfilter(ECLParser.DefinitionstatustokenfilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#definitionstatuskeyword}.
	 * @param ctx the parse tree
	 */
	void enterDefinitionstatuskeyword(ECLParser.DefinitionstatuskeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#definitionstatuskeyword}.
	 * @param ctx the parse tree
	 */
	void exitDefinitionstatuskeyword(ECLParser.DefinitionstatuskeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#definitionstatustoken}.
	 * @param ctx the parse tree
	 */
	void enterDefinitionstatustoken(ECLParser.DefinitionstatustokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#definitionstatustoken}.
	 * @param ctx the parse tree
	 */
	void exitDefinitionstatustoken(ECLParser.DefinitionstatustokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#definitionstatustokenset}.
	 * @param ctx the parse tree
	 */
	void enterDefinitionstatustokenset(ECLParser.DefinitionstatustokensetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#definitionstatustokenset}.
	 * @param ctx the parse tree
	 */
	void exitDefinitionstatustokenset(ECLParser.DefinitionstatustokensetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#primitivetoken}.
	 * @param ctx the parse tree
	 */
	void enterPrimitivetoken(ECLParser.PrimitivetokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#primitivetoken}.
	 * @param ctx the parse tree
	 */
	void exitPrimitivetoken(ECLParser.PrimitivetokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#definedtoken}.
	 * @param ctx the parse tree
	 */
	void enterDefinedtoken(ECLParser.DefinedtokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#definedtoken}.
	 * @param ctx the parse tree
	 */
	void exitDefinedtoken(ECLParser.DefinedtokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#modulefilter}.
	 * @param ctx the parse tree
	 */
	void enterModulefilter(ECLParser.ModulefilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#modulefilter}.
	 * @param ctx the parse tree
	 */
	void exitModulefilter(ECLParser.ModulefilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#moduleidkeyword}.
	 * @param ctx the parse tree
	 */
	void enterModuleidkeyword(ECLParser.ModuleidkeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#moduleidkeyword}.
	 * @param ctx the parse tree
	 */
	void exitModuleidkeyword(ECLParser.ModuleidkeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#effectivetimefilter}.
	 * @param ctx the parse tree
	 */
	void enterEffectivetimefilter(ECLParser.EffectivetimefilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#effectivetimefilter}.
	 * @param ctx the parse tree
	 */
	void exitEffectivetimefilter(ECLParser.EffectivetimefilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#effectivetimekeyword}.
	 * @param ctx the parse tree
	 */
	void enterEffectivetimekeyword(ECLParser.EffectivetimekeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#effectivetimekeyword}.
	 * @param ctx the parse tree
	 */
	void exitEffectivetimekeyword(ECLParser.EffectivetimekeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#timevalue}.
	 * @param ctx the parse tree
	 */
	void enterTimevalue(ECLParser.TimevalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#timevalue}.
	 * @param ctx the parse tree
	 */
	void exitTimevalue(ECLParser.TimevalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#timevalueset}.
	 * @param ctx the parse tree
	 */
	void enterTimevalueset(ECLParser.TimevaluesetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#timevalueset}.
	 * @param ctx the parse tree
	 */
	void exitTimevalueset(ECLParser.TimevaluesetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#year}.
	 * @param ctx the parse tree
	 */
	void enterYear(ECLParser.YearContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#year}.
	 * @param ctx the parse tree
	 */
	void exitYear(ECLParser.YearContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#month}.
	 * @param ctx the parse tree
	 */
	void enterMonth(ECLParser.MonthContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#month}.
	 * @param ctx the parse tree
	 */
	void exitMonth(ECLParser.MonthContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#day}.
	 * @param ctx the parse tree
	 */
	void enterDay(ECLParser.DayContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#day}.
	 * @param ctx the parse tree
	 */
	void exitDay(ECLParser.DayContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#activefilter}.
	 * @param ctx the parse tree
	 */
	void enterActivefilter(ECLParser.ActivefilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#activefilter}.
	 * @param ctx the parse tree
	 */
	void exitActivefilter(ECLParser.ActivefilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#activekeyword}.
	 * @param ctx the parse tree
	 */
	void enterActivekeyword(ECLParser.ActivekeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#activekeyword}.
	 * @param ctx the parse tree
	 */
	void exitActivekeyword(ECLParser.ActivekeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#activevalue}.
	 * @param ctx the parse tree
	 */
	void enterActivevalue(ECLParser.ActivevalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#activevalue}.
	 * @param ctx the parse tree
	 */
	void exitActivevalue(ECLParser.ActivevalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#activetruevalue}.
	 * @param ctx the parse tree
	 */
	void enterActivetruevalue(ECLParser.ActivetruevalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#activetruevalue}.
	 * @param ctx the parse tree
	 */
	void exitActivetruevalue(ECLParser.ActivetruevalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#activefalsevalue}.
	 * @param ctx the parse tree
	 */
	void enterActivefalsevalue(ECLParser.ActivefalsevalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#activefalsevalue}.
	 * @param ctx the parse tree
	 */
	void exitActivefalsevalue(ECLParser.ActivefalsevalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#memberfilterconstraint}.
	 * @param ctx the parse tree
	 */
	void enterMemberfilterconstraint(ECLParser.MemberfilterconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#memberfilterconstraint}.
	 * @param ctx the parse tree
	 */
	void exitMemberfilterconstraint(ECLParser.MemberfilterconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#memberfilter}.
	 * @param ctx the parse tree
	 */
	void enterMemberfilter(ECLParser.MemberfilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#memberfilter}.
	 * @param ctx the parse tree
	 */
	void exitMemberfilter(ECLParser.MemberfilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#memberfieldfilter}.
	 * @param ctx the parse tree
	 */
	void enterMemberfieldfilter(ECLParser.MemberfieldfilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#memberfieldfilter}.
	 * @param ctx the parse tree
	 */
	void exitMemberfieldfilter(ECLParser.MemberfieldfilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#historysupplement}.
	 * @param ctx the parse tree
	 */
	void enterHistorysupplement(ECLParser.HistorysupplementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#historysupplement}.
	 * @param ctx the parse tree
	 */
	void exitHistorysupplement(ECLParser.HistorysupplementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#historykeyword}.
	 * @param ctx the parse tree
	 */
	void enterHistorykeyword(ECLParser.HistorykeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#historykeyword}.
	 * @param ctx the parse tree
	 */
	void exitHistorykeyword(ECLParser.HistorykeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#historyprofilesuffix}.
	 * @param ctx the parse tree
	 */
	void enterHistoryprofilesuffix(ECLParser.HistoryprofilesuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#historyprofilesuffix}.
	 * @param ctx the parse tree
	 */
	void exitHistoryprofilesuffix(ECLParser.HistoryprofilesuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#historyminimumsuffix}.
	 * @param ctx the parse tree
	 */
	void enterHistoryminimumsuffix(ECLParser.HistoryminimumsuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#historyminimumsuffix}.
	 * @param ctx the parse tree
	 */
	void exitHistoryminimumsuffix(ECLParser.HistoryminimumsuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#historymoderatesuffix}.
	 * @param ctx the parse tree
	 */
	void enterHistorymoderatesuffix(ECLParser.HistorymoderatesuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#historymoderatesuffix}.
	 * @param ctx the parse tree
	 */
	void exitHistorymoderatesuffix(ECLParser.HistorymoderatesuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#historymaximumsuffix}.
	 * @param ctx the parse tree
	 */
	void enterHistorymaximumsuffix(ECLParser.HistorymaximumsuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#historymaximumsuffix}.
	 * @param ctx the parse tree
	 */
	void exitHistorymaximumsuffix(ECLParser.HistorymaximumsuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#historysubset}.
	 * @param ctx the parse tree
	 */
	void enterHistorysubset(ECLParser.HistorysubsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#historysubset}.
	 * @param ctx the parse tree
	 */
	void exitHistorysubset(ECLParser.HistorysubsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#numericvalue}.
	 * @param ctx the parse tree
	 */
	void enterNumericvalue(ECLParser.NumericvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#numericvalue}.
	 * @param ctx the parse tree
	 */
	void exitNumericvalue(ECLParser.NumericvalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#stringvalue}.
	 * @param ctx the parse tree
	 */
	void enterStringvalue(ECLParser.StringvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#stringvalue}.
	 * @param ctx the parse tree
	 */
	void exitStringvalue(ECLParser.StringvalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#integervalue}.
	 * @param ctx the parse tree
	 */
	void enterIntegervalue(ECLParser.IntegervalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#integervalue}.
	 * @param ctx the parse tree
	 */
	void exitIntegervalue(ECLParser.IntegervalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#decimalvalue}.
	 * @param ctx the parse tree
	 */
	void enterDecimalvalue(ECLParser.DecimalvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#decimalvalue}.
	 * @param ctx the parse tree
	 */
	void exitDecimalvalue(ECLParser.DecimalvalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#booleanvalue}.
	 * @param ctx the parse tree
	 */
	void enterBooleanvalue(ECLParser.BooleanvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#booleanvalue}.
	 * @param ctx the parse tree
	 */
	void exitBooleanvalue(ECLParser.BooleanvalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#true_1}.
	 * @param ctx the parse tree
	 */
	void enterTrue_1(ECLParser.True_1Context ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#true_1}.
	 * @param ctx the parse tree
	 */
	void exitTrue_1(ECLParser.True_1Context ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#false_1}.
	 * @param ctx the parse tree
	 */
	void enterFalse_1(ECLParser.False_1Context ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#false_1}.
	 * @param ctx the parse tree
	 */
	void exitFalse_1(ECLParser.False_1Context ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#nonnegativeintegervalue}.
	 * @param ctx the parse tree
	 */
	void enterNonnegativeintegervalue(ECLParser.NonnegativeintegervalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#nonnegativeintegervalue}.
	 * @param ctx the parse tree
	 */
	void exitNonnegativeintegervalue(ECLParser.NonnegativeintegervalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#sctid}.
	 * @param ctx the parse tree
	 */
	void enterSctid(ECLParser.SctidContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#sctid}.
	 * @param ctx the parse tree
	 */
	void exitSctid(ECLParser.SctidContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#ws}.
	 * @param ctx the parse tree
	 */
	void enterWs(ECLParser.WsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#ws}.
	 * @param ctx the parse tree
	 */
	void exitWs(ECLParser.WsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#mws}.
	 * @param ctx the parse tree
	 */
	void enterMws(ECLParser.MwsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#mws}.
	 * @param ctx the parse tree
	 */
	void exitMws(ECLParser.MwsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(ECLParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(ECLParser.CommentContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#nonstarchar}.
	 * @param ctx the parse tree
	 */
	void enterNonstarchar(ECLParser.NonstarcharContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#nonstarchar}.
	 * @param ctx the parse tree
	 */
	void exitNonstarchar(ECLParser.NonstarcharContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#starwithnonfslash}.
	 * @param ctx the parse tree
	 */
	void enterStarwithnonfslash(ECLParser.StarwithnonfslashContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#starwithnonfslash}.
	 * @param ctx the parse tree
	 */
	void exitStarwithnonfslash(ECLParser.StarwithnonfslashContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#nonfslash}.
	 * @param ctx the parse tree
	 */
	void enterNonfslash(ECLParser.NonfslashContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#nonfslash}.
	 * @param ctx the parse tree
	 */
	void exitNonfslash(ECLParser.NonfslashContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#sp}.
	 * @param ctx the parse tree
	 */
	void enterSp(ECLParser.SpContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#sp}.
	 * @param ctx the parse tree
	 */
	void exitSp(ECLParser.SpContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#htab}.
	 * @param ctx the parse tree
	 */
	void enterHtab(ECLParser.HtabContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#htab}.
	 * @param ctx the parse tree
	 */
	void exitHtab(ECLParser.HtabContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#cr}.
	 * @param ctx the parse tree
	 */
	void enterCr(ECLParser.CrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#cr}.
	 * @param ctx the parse tree
	 */
	void exitCr(ECLParser.CrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#lf}.
	 * @param ctx the parse tree
	 */
	void enterLf(ECLParser.LfContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#lf}.
	 * @param ctx the parse tree
	 */
	void exitLf(ECLParser.LfContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#qm}.
	 * @param ctx the parse tree
	 */
	void enterQm(ECLParser.QmContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#qm}.
	 * @param ctx the parse tree
	 */
	void exitQm(ECLParser.QmContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#bs}.
	 * @param ctx the parse tree
	 */
	void enterBs(ECLParser.BsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#bs}.
	 * @param ctx the parse tree
	 */
	void exitBs(ECLParser.BsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#star}.
	 * @param ctx the parse tree
	 */
	void enterStar(ECLParser.StarContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#star}.
	 * @param ctx the parse tree
	 */
	void exitStar(ECLParser.StarContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#digit}.
	 * @param ctx the parse tree
	 */
	void enterDigit(ECLParser.DigitContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#digit}.
	 * @param ctx the parse tree
	 */
	void exitDigit(ECLParser.DigitContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#zero}.
	 * @param ctx the parse tree
	 */
	void enterZero(ECLParser.ZeroContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#zero}.
	 * @param ctx the parse tree
	 */
	void exitZero(ECLParser.ZeroContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#digitnonzero}.
	 * @param ctx the parse tree
	 */
	void enterDigitnonzero(ECLParser.DigitnonzeroContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#digitnonzero}.
	 * @param ctx the parse tree
	 */
	void exitDigitnonzero(ECLParser.DigitnonzeroContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#nonwsnonpipe}.
	 * @param ctx the parse tree
	 */
	void enterNonwsnonpipe(ECLParser.NonwsnonpipeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#nonwsnonpipe}.
	 * @param ctx the parse tree
	 */
	void exitNonwsnonpipe(ECLParser.NonwsnonpipeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#anynonescapedchar}.
	 * @param ctx the parse tree
	 */
	void enterAnynonescapedchar(ECLParser.AnynonescapedcharContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#anynonescapedchar}.
	 * @param ctx the parse tree
	 */
	void exitAnynonescapedchar(ECLParser.AnynonescapedcharContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#codechar}.
	 * @param ctx the parse tree
	 */
	void enterCodechar(ECLParser.CodecharContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#codechar}.
	 * @param ctx the parse tree
	 */
	void exitCodechar(ECLParser.CodecharContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#escapedchar}.
	 * @param ctx the parse tree
	 */
	void enterEscapedchar(ECLParser.EscapedcharContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#escapedchar}.
	 * @param ctx the parse tree
	 */
	void exitEscapedchar(ECLParser.EscapedcharContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#escapedwildchar}.
	 * @param ctx the parse tree
	 */
	void enterEscapedwildchar(ECLParser.EscapedwildcharContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#escapedwildchar}.
	 * @param ctx the parse tree
	 */
	void exitEscapedwildchar(ECLParser.EscapedwildcharContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#nonwsnonescapedchar}.
	 * @param ctx the parse tree
	 */
	void enterNonwsnonescapedchar(ECLParser.NonwsnonescapedcharContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#nonwsnonescapedchar}.
	 * @param ctx the parse tree
	 */
	void exitNonwsnonescapedchar(ECLParser.NonwsnonescapedcharContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#alpha}.
	 * @param ctx the parse tree
	 */
	void enterAlpha(ECLParser.AlphaContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#alpha}.
	 * @param ctx the parse tree
	 */
	void exitAlpha(ECLParser.AlphaContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#dash}.
	 * @param ctx the parse tree
	 */
	void enterDash(ECLParser.DashContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#dash}.
	 * @param ctx the parse tree
	 */
	void exitDash(ECLParser.DashContext ctx);
}