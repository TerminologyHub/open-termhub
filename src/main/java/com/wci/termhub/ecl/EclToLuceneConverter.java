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

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ExpressionConstraintToLuceneConverter.
 * 
 * NOTE: Adapted from Kai Kewley's SNOMED Query Service at
 * https://github.com/IHTSDO/snomed-query-service
 */
@SuppressWarnings("all")
public class EclToLuceneConverter {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(EclToLuceneConverter.class);

	/**
	 * The Enum InternalFunction. NOTE: Changed to public from SQS and comments
	 * added
	 */
	public enum InternalFunction {

		/** The attribute child of. */
		ATTRIBUTE_CHILD_OF(EclConceptFieldNames.PARENT, false),

		/** The attribute parent of. */
		ATTRIBUTE_PARENT_OF(EclConceptFieldNames.PARENT, false),

		/** The attribute descendant of. */
		ATTRIBUTE_DESCENDANT_OF(EclConceptFieldNames.ANCESTOR, false),

		/** The attribute descendant or self of. */
		ATTRIBUTE_DESCENDANT_OR_SELF_OF(EclConceptFieldNames.ANCESTOR, true),

		/** The attribute ancestor of. */
		ATTRIBUTE_ANCESTOR_OF(EclConceptFieldNames.ANCESTOR, false),

		/** The attribute ancestor or self of. */
		ATTRIBUTE_ANCESTOR_OR_SELF_OF(EclConceptFieldNames.ANCESTOR, true),

		/** The ancestor or self of. */
		ANCESTOR_OR_SELF_OF(EclConceptFieldNames.ANCESTOR, true),

		/** The ancestor of. */
		ANCESTOR_OF(EclConceptFieldNames.ANCESTOR, false),

		/** The ancestor of. */
		PARENT_OF(EclConceptFieldNames.PARENT, false);

		/** The type. */
		private String searchField;

		/** The include self. */
		private boolean includeSelf;

		/**
		 * Instantiates a new internal function.
		 *
		 * @param type         the type
		 * @param ancestorType the ancestor type
		 * @param includeSelf  the include self
		 */
		InternalFunction(final String searchField, final boolean includeSelf) {
			this.searchField = searchField;
			this.includeSelf = includeSelf;
		}

		/**
		 * Checks if is ancestor type.
		 *
		 * @return true, if is ancestor type
		 */
		public String getSearchField() {
			return searchField;
		}

		/**
		 * Checks if is include self.
		 *
		 * @return true, if is include self
		 */
		public boolean isIncludeSelf() {
			return includeSelf;
		}

	}

	/**
	 * Parse.
	 *
	 * @param ecQuery the ec query
	 * @return the string
	 * @throws Exception the exception
	 */
	public String parse(final String ecQuery) throws Exception {
		final EclLexer lexer = new EclLexer(CharStreams.fromString(ecQuery));
		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		final EclParser parser = new EclParser(tokens);
		final List<RecognitionException> exceptions = new ArrayList<>();
		parser.setErrorHandler(new ANTLRErrorStrategy() {
			@Override
			public void reportError(final Parser parser, final RecognitionException e) {
				exceptions.add(e);
			}

			@Override
			public void reset(final Parser parser) {
				// n/a
			}

			@Override
			public Token recoverInline(final Parser parser) throws RecognitionException {
				return null;
			}

			@Override
			public void recover(final Parser parser, final RecognitionException e) throws RecognitionException {
			}

			@Override
			public void sync(final Parser parser) throws RecognitionException {
			}

			@Override
			public boolean inErrorRecoveryMode(final Parser parser) {
				return false;
			}

			@Override
			public void reportMatch(final Parser parser) {
			}
		});
		final ParserRuleContext tree = parser.expressionconstraint();

		final ParseTreeWalker walker = new ParseTreeWalker();
		final ExpressionConstraintListener listener = new ExpressionConstraintListener();
		walker.walk(listener, tree);
		if (exceptions.isEmpty()) {
			return listener.getLuceneQuery();
		} else {
			final RecognitionException recognitionException = exceptions.get(0);
			throw recognitionException;
		}
	}

	/**
	 * The listener interface for receiving expressionConstraint events. The class
	 * that is interested in processing a expressionConstraint event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's addExpressionConstraintListener method. When
	 * the expressionConstraint event occurs, that object's appropriate method is
	 * invoked.
	 *
	 * @see ExpressionConstraintEvent
	 */
	protected static final class ExpressionConstraintListener extends EclBaseListener {

		/** The lucene query. */
		private StringBuilder luceneQuery = new StringBuilder();

		/** The in attribute. */
		private boolean inAttribute;

		/* see superclass */
		@Override
		public void visitErrorNode(final ErrorNode node) {
			super.visitErrorNode(node);
		}

		/* see superclass */
		@Override
		public void enterSubexpressionconstraint(final EclParser.SubexpressionconstraintContext cxt) {
			addLeftParenthesisIfNotNull(cxt.LEFT_PAREN());

			final EclParser.EclfocusconceptContext focusconcept = cxt.eclfocusconcept();
			if (focusconcept == null) {
				// logger.warn("Unable to handle this condition");
				return;
			}
			if (focusconcept.wildcard() != null) {
				if (!inAttribute) {
					luceneQuery.append(EclConceptFieldNames.ID + ":[* TO *]");
				} else {
					luceneQuery.append("*");
				}
			} else if (cxt.memberof() != null) {
				luceneQuery.append("ecl:" + EclConceptFieldNames.MEMBER_OF + "="
						+ focusconcept.eclconceptreference().conceptid().getText());
			} else {
				final String conceptId = focusconcept.eclconceptreference().conceptid().getText();
				final EclParser.ConstraintoperatorContext constraintoperator = cxt.constraintoperator();
				if (constraintoperator == null) {
					if (!inAttribute) {
						luceneQuery.append(
								EclConceptFieldNames.ID + ":" + (conceptId.equals("*") ? "[* TO *]" : conceptId));
					} else {
						if (!luceneQuery.toString().endsWith("=")) {
							luceneQuery.append("ecl:");
						}
						luceneQuery.append(focusconcept.eclconceptreference().conceptid().getText());
					}
				} else {

					if (constraintoperator.childof() != null) {
						if (!inAttribute) {
							luceneQuery.append("ecl:" + EclConceptFieldNames.PARENT + "=" + conceptId);
						} else {
							luceneQuery.append(InternalFunction.ATTRIBUTE_CHILD_OF + "(" + conceptId + ")");
						}
					} else if (constraintoperator.parentof() != null) {
						if (!inAttribute) {
							luceneQuery.append(InternalFunction.PARENT_OF + "(" + conceptId + ")");
						} else {
							luceneQuery.append(InternalFunction.ATTRIBUTE_PARENT_OF + "(" + conceptId + ")");
						}
					} else if (constraintoperator.descendantof() != null) {
						if (!inAttribute) {
							luceneQuery.append("ecl:" + EclConceptFieldNames.ANCESTOR + "=" + conceptId);
						} else {
							luceneQuery.append(InternalFunction.ATTRIBUTE_DESCENDANT_OF + "(" + conceptId + ")");
						}
					} else if (constraintoperator.descendantorselfof() != null) {
						if (!inAttribute) {
							luceneQuery.append("(" + EclConceptFieldNames.ID + ":"
									+ (conceptId.equals("*") ? "[* TO *]" : conceptId) + " OR " + "ecl:"
									+ EclConceptFieldNames.ANCESTOR + "=" + conceptId + ")");
						} else {
							luceneQuery
									.append(InternalFunction.ATTRIBUTE_DESCENDANT_OR_SELF_OF + "(" + conceptId + ")");
						}
					} else if (constraintoperator.ancestororselfof() != null) {
						if (!inAttribute) {
							luceneQuery.append(InternalFunction.ANCESTOR_OR_SELF_OF + "(" + conceptId + ")");
						} else {
							luceneQuery.append(InternalFunction.ATTRIBUTE_ANCESTOR_OR_SELF_OF + "(" + conceptId + ")");
						}
					} else if (constraintoperator.ancestorof() != null) {
						if (!inAttribute) {
							luceneQuery.append(InternalFunction.ANCESTOR_OF + "(" + conceptId + ")");
						} else {
							luceneQuery.append(InternalFunction.ATTRIBUTE_ANCESTOR_OF + "(" + conceptId + ")");
						}
					}
				}
			}
		}

		/* see superclass */
		@Override
		public void exitSubexpressionconstraint(final EclParser.SubexpressionconstraintContext cxt) {
			addRightParenthesisIfNotNull(cxt.RIGHT_PAREN());
		}

		/* see superclass */
		@Override
		public void enterEclrefinement(final EclParser.EclrefinementContext cxt) {
			luceneQuery.append(" AND ");
		}

		/* see superclass */
		@Override
		public void enterEclattributeset(final EclParser.EclattributesetContext cxt) {
			inAttribute = true;
		}

		/* see superclass */
		@Override
		public void exitEclattributeset(final EclParser.EclattributesetContext cxt) {
			inAttribute = false;
		}

		/* see superclass */
		@Override
		public void enterEclattributename(final EclParser.EclattributenameContext cxt) {
			if (cxt.subexpressionconstraint().eclfocusconcept() == null) {
				return;
			}
			final EclParser.EclconceptreferenceContext conceptreference = cxt.subexpressionconstraint()
					.eclfocusconcept().eclconceptreference();
			if (conceptreference != null) {
				// Handled
				// luceneQuery.append(conceptreference.conceptid().getText());
			} else {
				throwUnsupported("wildcard attributeName");
			}
		}

		/* see superclass */
		@Override
		public void enterExpressioncomparisonoperator(final EclParser.ExpressioncomparisonoperatorContext cxt) {
			if (cxt.getText().equals("=")) {
				luceneQuery.append("=");
			} else {
				throwUnsupported("not-equal expressionComparisonOperator");
			}
		}

		/* see superclass */
		@Override
		public void enterConjunction(final EclParser.ConjunctionContext cxt) {
			luceneQuery.append(" AND ");
		}

		/* see superclass */
		@Override
		public void enterDisjunction(final EclParser.DisjunctionContext cxt) {
			luceneQuery.append(" OR ");
		}

		/* see superclass */
		@Override
		public void enterExclusion(final EclParser.ExclusionContext cxt) {
			luceneQuery.append(" AND NOT ");
		}

		/* see superclass */
		@Override
		public void enterSubrefinement(final EclParser.SubrefinementContext cxt) {
			addLeftParenthesisIfNotNull(cxt.LEFT_PAREN());
		}

		/* see superclass */
		@Override
		public void exitSubrefinement(final EclParser.SubrefinementContext cxt) {
			addRightParenthesisIfNotNull(cxt.RIGHT_PAREN());
		}

		// /* see superclass */
		// @Override
		// public void enterSubattributeset(final EclParser.SubattributesetContext
		// cxt) {
		// addLeftParenthesisIfNotNull(cxt.LEFT_PAREN());
		// }
		//
		// /* see superclass */
		// @Override
		// public void enterExpressionconstraint(final
		// EclParser.ExpressionconstraintContext cxt) {
		// if (cxt.refinedexpressionconstraint() != null ||
		// (cxt.compoundexpressionconstraint() != null
		// && cxt.compoundexpressionconstraint().getText().contains(":"))) {
		// throw new UnsupportedOperationException(
		// "Within an expressionConstraintValue refinedExpressionConstraint is not
		// currently supported.");
		// }
		// // addLeftParenthesisIfNotNull(cxt.LEFT_PAREN());
		// }
		//
		// /* see superclass */
		// @Override
		// public void exitExpressionconstraint(final
		// EclParser.ExpressionconstraintContext cxt) {
		// // addRightParenthesisIfNotNull(cxt.RIGHT_PAREN());
		// }

		/* see superclass */
		@Override
		public void exitSubattributeset(final EclParser.SubattributesetContext cxt) {
			addRightParenthesisIfNotNull(cxt.RIGHT_PAREN());
		}

		/**
		 * Add left parenthesis if not null.
		 *
		 * @param terminalNode the terminal node
		 */
		private void addLeftParenthesisIfNotNull(final TerminalNode terminalNode) {
			if (terminalNode != null) {
				luceneQuery.append(" ( ");
			}
		}

		/**
		 * Add right parenthesis if not null.
		 *
		 * @param terminalNode the terminal node
		 */
		private void addRightParenthesisIfNotNull(final TerminalNode terminalNode) {
			if (terminalNode != null) {
				luceneQuery.append(" ) ");
			}
		}

		// Unsupported enter methods below this line

		/* see superclass */
		/* see superclass */
		/* see superclass */
		@Override
		public void enterCardinality(final EclParser.CardinalityContext cxt) {
			throwUnsupported("cardinality");
		}

		/* see superclass */
		@Override
		public void enterEclattributegroup(final EclParser.EclattributegroupContext cxt) {
			throwUnsupported("attributeGroup");
		}

		/* see superclass */
		@Override
		public void enterStringcomparisonoperator(final EclParser.StringcomparisonoperatorContext cxt) {
			throwUnsupported("stringComparisonOperator");
		}

		/* see superclass */
		@Override
		public void enterNumericcomparisonoperator(final EclParser.NumericcomparisonoperatorContext cxt) {
			throwUnsupported("numericComparisonOperator");
		}

		/* see superclass */
		@Override
		public void enterReverseflag(final EclParser.ReverseflagContext cxt) {
			throwUnsupported("reverseFlag");
		}

		/**
		 * Throw unsupported.
		 *
		 * @param feature the feature
		 */
		private void throwUnsupported(final String feature) {
			throw new UnsupportedOperationException(feature + " is not currently supported.");
		}

		/**
		 * Gets the lucene query.
		 *
		 * @return the lucene query
		 */
		public String getLuceneQuery() {
			return luceneQuery.toString();
		}
	}

}
