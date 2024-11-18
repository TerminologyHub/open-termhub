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

import java.util.List;

import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * The Class EclParser.
 */
@SuppressWarnings({ "all", "warnings", "unchecked", "unused", "cast" })
public class EclParser extends Parser {
	static {
		RuntimeMetaData.checkVersion("4.7.1", RuntimeMetaData.VERSION);
	}

	/** The Constant _decisionToDFA. */
	protected static final DFA[] _decisionToDFA;

	/** The Constant _sharedContextCache. */
	protected static final PredictionContextCache _sharedContextCache = new PredictionContextCache();

	/** The Constant U_00F4. */
	public static final int TAB = 1, LF = 2, CR = 3, SPACE = 4, EXCLAMATION = 5, QUOTE = 6, POUND = 7, DOLLAR = 8,
			PERCENT = 9, AMPERSAND = 10, APOSTROPHE = 11, LEFT_PAREN = 12, RIGHT_PAREN = 13, ASTERISK = 14, PLUS = 15,
			COMMA = 16, DASH = 17, PERIOD = 18, SLASH = 19, ZERO = 20, ONE = 21, TWO = 22, THREE = 23, FOUR = 24,
			FIVE = 25, SIX = 26, SEVEN = 27, EIGHT = 28, NINE = 29, COLON = 30, SEMICOLON = 31, LESS_THAN = 32,
			EQUALS = 33, GREATER_THAN = 34, QUESTION = 35, AT = 36, CAP_A = 37, CAP_B = 38, CAP_C = 39, CAP_D = 40,
			CAP_E = 41, CAP_F = 42, CAP_G = 43, CAP_H = 44, CAP_I = 45, CAP_J = 46, CAP_K = 47, CAP_L = 48, CAP_M = 49,
			CAP_N = 50, CAP_O = 51, CAP_P = 52, CAP_Q = 53, CAP_R = 54, CAP_S = 55, CAP_T = 56, CAP_U = 57, CAP_V = 58,
			CAP_W = 59, CAP_X = 60, CAP_Y = 61, CAP_Z = 62, LEFT_BRACE = 63, BACKSLASH = 64, RIGHT_BRACE = 65,
			CARAT = 66, UNDERSCORE = 67, ACCENT = 68, A = 69, B = 70, C = 71, D = 72, E = 73, F = 74, G = 75, H = 76,
			I = 77, J = 78, K = 79, L = 80, M = 81, N = 82, O = 83, P = 84, Q = 85, R = 86, S = 87, T = 88, U = 89,
			V = 90, W = 91, X = 92, Y = 93, Z = 94, LEFT_CURLY_BRACE = 95, PIPE = 96, RIGHT_CURLY_BRACE = 97,
			TILDE = 98, U_0080 = 99, U_0081 = 100, U_0082 = 101, U_0083 = 102, U_0084 = 103, U_0085 = 104, U_0086 = 105,
			U_0087 = 106, U_0088 = 107, U_0089 = 108, U_008A = 109, U_008B = 110, U_008C = 111, U_008D = 112,
			U_008E = 113, U_008F = 114, U_0090 = 115, U_0091 = 116, U_0092 = 117, U_0093 = 118, U_0094 = 119,
			U_0095 = 120, U_0096 = 121, U_0097 = 122, U_0098 = 123, U_0099 = 124, U_009A = 125, U_009B = 126,
			U_009C = 127, U_009D = 128, U_009E = 129, U_009F = 130, U_00A0 = 131, U_00A1 = 132, U_00A2 = 133,
			U_00A3 = 134, U_00A4 = 135, U_00A5 = 136, U_00A6 = 137, U_00A7 = 138, U_00A8 = 139, U_00A9 = 140,
			U_00AA = 141, U_00AB = 142, U_00AC = 143, U_00AD = 144, U_00AE = 145, U_00AF = 146, U_00B0 = 147,
			U_00B1 = 148, U_00B2 = 149, U_00B3 = 150, U_00B4 = 151, U_00B5 = 152, U_00B6 = 153, U_00B7 = 154,
			U_00B8 = 155, U_00B9 = 156, U_00BA = 157, U_00BB = 158, U_00BC = 159, U_00BD = 160, U_00BE = 161,
			U_00BF = 162, U_00C2 = 163, U_00C3 = 164, U_00C4 = 165, U_00C5 = 166, U_00C6 = 167, U_00C7 = 168,
			U_00C8 = 169, U_00C9 = 170, U_00CA = 171, U_00CB = 172, U_00CC = 173, U_00CD = 174, U_00CE = 175,
			U_00CF = 176, U_00D0 = 177, U_00D1 = 178, U_00D2 = 179, U_00D3 = 180, U_00D4 = 181, U_00D5 = 182,
			U_00D6 = 183, U_00D7 = 184, U_00D8 = 185, U_00D9 = 186, U_00DA = 187, U_00DB = 188, U_00DC = 189,
			U_00DD = 190, U_00DE = 191, U_00DF = 192, U_00E0 = 193, U_00E1 = 194, U_00E2 = 195, U_00E3 = 196,
			U_00E4 = 197, U_00E5 = 198, U_00E6 = 199, U_00E7 = 200, U_00E8 = 201, U_00E9 = 202, U_00EA = 203,
			U_00EB = 204, U_00EC = 205, U_00ED = 206, U_00EE = 207, U_00EF = 208, U_00F0 = 209, U_00F1 = 210,
			U_00F2 = 211, U_00F3 = 212, U_00F4 = 213;

	/** The Constant RULE_utf8_tail. */
	public static final int RULE_expressionconstraint = 0, RULE_refinedexpressionconstraint = 1,
			RULE_compoundexpressionconstraint = 2, RULE_conjunctionexpressionconstraint = 3,
			RULE_disjunctionexpressionconstraint = 4, RULE_exclusionexpressionconstraint = 5,
			RULE_subexpressionconstraint = 6, RULE_eclfocusconcept = 7, RULE_memberof = 8, RULE_eclconceptreference = 9,
			RULE_conceptid = 10, RULE_term = 11, RULE_wildcard = 12, RULE_constraintoperator = 13,
			RULE_descendantof = 14, RULE_descendantorselfof = 15, RULE_childof = 16, RULE_ancestorof = 17,
			RULE_ancestororselfof = 18, RULE_parentof = 19, RULE_conjunction = 20, RULE_disjunction = 21,
			RULE_exclusion = 22, RULE_eclrefinement = 23, RULE_conjunctionrefinementset = 24,
			RULE_disjunctionrefinementset = 25, RULE_subrefinement = 26, RULE_eclattributeset = 27,
			RULE_conjunctionattributeset = 28, RULE_disjunctionattributeset = 29, RULE_subattributeset = 30,
			RULE_eclattributegroup = 31, RULE_eclattribute = 32, RULE_cardinality = 33, RULE_minvalue = 34,
			RULE_to = 35, RULE_maxvalue = 36, RULE_many = 37, RULE_reverseflag = 38, RULE_eclattributename = 39,
			RULE_expressioncomparisonoperator = 40, RULE_numericcomparisonoperator = 41,
			RULE_stringcomparisonoperator = 42, RULE_numericvalue = 43, RULE_stringvalue = 44, RULE_integervalue = 45,
			RULE_decimalvalue = 46, RULE_nonnegativeintegervalue = 47, RULE_sctid = 48, RULE_ws = 49, RULE_mws = 50,
			RULE_comment = 51, RULE_nonstarchar = 52, RULE_starwithnonfslash = 53, RULE_nonfslash = 54, RULE_sp = 55,
			RULE_htab = 56, RULE_cr = 57, RULE_lf = 58, RULE_qm = 59, RULE_bs = 60, RULE_digit = 61, RULE_zero = 62,
			RULE_digitnonzero = 63, RULE_nonwsnonpipe = 64, RULE_anynonescapedchar = 65, RULE_codechar = 66,
			RULE_escapedchar = 67, RULE_utf8_2 = 68, RULE_utf8_3 = 69, RULE_utf8_4 = 70, RULE_utf8_tail = 71;

	/** The Constant ruleNames. */
	public static final String[] ruleNames = { "expressionconstraint", "refinedexpressionconstraint",
			"compoundexpressionconstraint", "conjunctionexpressionconstraint", "disjunctionexpressionconstraint",
			"exclusionexpressionconstraint", "subexpressionconstraint", "eclfocusconcept", "memberof",
			"eclconceptreference", "conceptid", "term", "wildcard", "constraintoperator", "descendantof",
			"descendantorselfof", "childof", "ancestorof", "ancestororselfof", "parentof", "conjunction", "disjunction",
			"exclusion", "eclrefinement", "conjunctionrefinementset", "disjunctionrefinementset", "subrefinement",
			"eclattributeset", "conjunctionattributeset", "disjunctionattributeset", "subattributeset",
			"eclattributegroup", "eclattribute", "cardinality", "minvalue", "to", "maxvalue", "many", "reverseflag",
			"eclattributename", "expressioncomparisonoperator", "numericcomparisonoperator", "stringcomparisonoperator",
			"numericvalue", "stringvalue", "integervalue", "decimalvalue", "nonnegativeintegervalue", "sctid", "ws",
			"mws", "comment", "nonstarchar", "starwithnonfslash", "nonfslash", "sp", "htab", "cr", "lf", "qm", "bs",
			"digit", "zero", "digitnonzero", "nonwsnonpipe", "anynonescapedchar", "codechar", "escapedchar", "utf8_2",
			"utf8_3", "utf8_4", "utf8_tail" };

	/** The Constant _LITERAL_NAMES. */
	private static final String[] _LITERAL_NAMES = { null, "'\\u0009'", "'\\u000A'", "'\\u000D'", "' '", "'!'", "'\"'",
			"'#'", "'$'", "'%'", "'&'", "'''", "'('", "')'", "'*'", "'+'", "','", "'-'", "'.'", "'/'", "'0'", "'1'",
			"'2'", "'3'", "'4'", "'5'", "'6'", "'7'", "'8'", "'9'", "':'", "';'", "'<'", "'='", "'>'", "'?'", "'@'",
			"'A'", "'B'", "'C'", "'D'", "'E'", "'F'", "'G'", "'H'", "'I'", "'J'", "'K'", "'L'", "'M'", "'N'", "'O'",
			"'P'", "'Q'", "'R'", "'S'", "'T'", "'U'", "'V'", "'W'", "'X'", "'Y'", "'Z'", "'['", "'\\'", "']'", "'^'",
			"'_'", "'`'", "'a'", "'b'", "'c'", "'d'", "'e'", "'f'", "'g'", "'h'", "'i'", "'j'", "'k'", "'l'", "'m'",
			"'n'", "'o'", "'p'", "'q'", "'r'", "'s'", "'t'", "'u'", "'v'", "'w'", "'x'", "'y'", "'z'", "'{'", "'|'",
			"'}'", "'~'", "'\\u0080'", "'\\u0081'", "'\\u0082'", "'\\u0083'", "'\\u0084'", "'\\u0085'", "'\\u0086'",
			"'\\u0087'", "'\\u0088'", "'\\u0089'", "'\\u008A'", "'\\u008B'", "'\\u008C'", "'\\u008D'", "'\\u008E'",
			"'\\u008F'", "'\\u0090'", "'\\u0091'", "'\\u0092'", "'\\u0093'", "'\\u0094'", "'\\u0095'", "'\\u0096'",
			"'\\u0097'", "'\\u0098'", "'\\u0099'", "'\\u009A'", "'\\u009B'", "'\\u009C'", "'\\u009D'", "'\\u009E'",
			"'\\u009F'", "'\\u00A0'", "'\\u00A1'", "'\\u00A2'", "'\\u00A3'", "'\\u00A4'", "'\\u00A5'", "'\\u00A6'",
			"'\\u00A7'", "'\\u00A8'", "'\\u00A9'", "'\\u00AA'", "'\\u00AB'", "'\\u00AC'", "'\\u00AD'", "'\\u00AE'",
			"'\\u00AF'", "'\\u00B0'", "'\\u00B1'", "'\\u00B2'", "'\\u00B3'", "'\\u00B4'", "'\\u00B5'", "'\\u00B6'",
			"'\\u00B7'", "'\\u00B8'", "'\\u00B9'", "'\\u00BA'", "'\\u00BB'", "'\\u00BC'", "'\\u00BD'", "'\\u00BE'",
			"'\\u00BF'", "'\\u00C2'", "'\\u00C3'", "'\\u00C4'", "'\\u00C5'", "'\\u00C6'", "'\\u00C7'", "'\\u00C8'",
			"'\\u00C9'", "'\\u00CA'", "'\\u00CB'", "'\\u00CC'", "'\\u00CD'", "'\\u00CE'", "'\\u00CF'", "'\\u00D0'",
			"'\\u00D1'", "'\\u00D2'", "'\\u00D3'", "'\\u00D4'", "'\\u00D5'", "'\\u00D6'", "'\\u00D7'", "'\\u00D8'",
			"'\\u00D9'", "'\\u00DA'", "'\\u00DB'", "'\\u00DC'", "'\\u00DD'", "'\\u00DE'", "'\\u00DF'", "'\\u00E0'",
			"'\\u00E1'", "'\\u00E2'", "'\\u00E3'", "'\\u00E4'", "'\\u00E5'", "'\\u00E6'", "'\\u00E7'", "'\\u00E8'",
			"'\\u00E9'", "'\\u00EA'", "'\\u00EB'", "'\\u00EC'", "'\\u00ED'", "'\\u00EE'", "'\\u00EF'", "'\\u00F0'",
			"'\\u00F1'", "'\\u00F2'", "'\\u00F3'", "'\\u00F4'" };

	/** The Constant _SYMBOLIC_NAMES. */
	private static final String[] _SYMBOLIC_NAMES = { null, "TAB", "LF", "CR", "SPACE", "EXCLAMATION", "QUOTE", "POUND",
			"DOLLAR", "PERCENT", "AMPERSAND", "APOSTROPHE", "LEFT_PAREN", "RIGHT_PAREN", "ASTERISK", "PLUS", "COMMA",
			"DASH", "PERIOD", "SLASH", "ZERO", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE",
			"COLON", "SEMICOLON", "LESS_THAN", "EQUALS", "GREATER_THAN", "QUESTION", "AT", "CAP_A", "CAP_B", "CAP_C",
			"CAP_D", "CAP_E", "CAP_F", "CAP_G", "CAP_H", "CAP_I", "CAP_J", "CAP_K", "CAP_L", "CAP_M", "CAP_N", "CAP_O",
			"CAP_P", "CAP_Q", "CAP_R", "CAP_S", "CAP_T", "CAP_U", "CAP_V", "CAP_W", "CAP_X", "CAP_Y", "CAP_Z",
			"LEFT_BRACE", "BACKSLASH", "RIGHT_BRACE", "CARAT", "UNDERSCORE", "ACCENT", "A", "B", "C", "D", "E", "F",
			"G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
			"LEFT_CURLY_BRACE", "PIPE", "RIGHT_CURLY_BRACE", "TILDE", "U_0080", "U_0081", "U_0082", "U_0083", "U_0084",
			"U_0085", "U_0086", "U_0087", "U_0088", "U_0089", "U_008A", "U_008B", "U_008C", "U_008D", "U_008E",
			"U_008F", "U_0090", "U_0091", "U_0092", "U_0093", "U_0094", "U_0095", "U_0096", "U_0097", "U_0098",
			"U_0099", "U_009A", "U_009B", "U_009C", "U_009D", "U_009E", "U_009F", "U_00A0", "U_00A1", "U_00A2",
			"U_00A3", "U_00A4", "U_00A5", "U_00A6", "U_00A7", "U_00A8", "U_00A9", "U_00AA", "U_00AB", "U_00AC",
			"U_00AD", "U_00AE", "U_00AF", "U_00B0", "U_00B1", "U_00B2", "U_00B3", "U_00B4", "U_00B5", "U_00B6",
			"U_00B7", "U_00B8", "U_00B9", "U_00BA", "U_00BB", "U_00BC", "U_00BD", "U_00BE", "U_00BF", "U_00C2",
			"U_00C3", "U_00C4", "U_00C5", "U_00C6", "U_00C7", "U_00C8", "U_00C9", "U_00CA", "U_00CB", "U_00CC",
			"U_00CD", "U_00CE", "U_00CF", "U_00D0", "U_00D1", "U_00D2", "U_00D3", "U_00D4", "U_00D5", "U_00D6",
			"U_00D7", "U_00D8", "U_00D9", "U_00DA", "U_00DB", "U_00DC", "U_00DD", "U_00DE", "U_00DF", "U_00E0",
			"U_00E1", "U_00E2", "U_00E3", "U_00E4", "U_00E5", "U_00E6", "U_00E7", "U_00E8", "U_00E9", "U_00EA",
			"U_00EB", "U_00EC", "U_00ED", "U_00EE", "U_00EF", "U_00F0", "U_00F1", "U_00F2", "U_00F3", "U_00F4" };

	/** The Constant VOCABULARY. */
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * The Constant tokenNames.
	 *
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	/* see superclass */
	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	/* see superclass */
	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	/* see superclass */
	@Override
	public String getGrammarFileName() {
		return "ECL.txt";
	}

	/* see superclass */
	@Override
	public String[] getRuleNames() {
		return ruleNames;
	}

	/* see superclass */
	@Override
	public String getSerializedATN() {
		return _serializedATN;
	}

	/* see superclass */
	@Override
	public ATN getATN() {
		return _ATN;
	}

	/**
	 * Instantiates a {@link EclParser} from the specified parameters.
	 *
	 * @param input the input
	 */
	public EclParser(final TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
	}

	/**
	 * The Class ExpressionconstraintContext.
	 */
	public static class ExpressionconstraintContext extends ParserRuleContext {

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Refinedexpressionconstraint.
		 *
		 * @return the refinedexpressionconstraint context
		 */
		public RefinedexpressionconstraintContext refinedexpressionconstraint() {
			return getRuleContext(RefinedexpressionconstraintContext.class, 0);
		}

		/**
		 * Compoundexpressionconstraint.
		 *
		 * @return the compoundexpressionconstraint context
		 */
		public CompoundexpressionconstraintContext compoundexpressionconstraint() {
			return getRuleContext(CompoundexpressionconstraintContext.class, 0);
		}

		/**
		 * Subexpressionconstraint.
		 *
		 * @return the subexpressionconstraint context
		 */
		public SubexpressionconstraintContext subexpressionconstraint() {
			return getRuleContext(SubexpressionconstraintContext.class, 0);
		}

		/**
		 * Instantiates a {@link ExpressionconstraintContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ExpressionconstraintContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_expressionconstraint;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterExpressionconstraint(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitExpressionconstraint(this);
			}
		}
	}

	/**
	 * Expressionconstraint.
	 *
	 * @return the expressionconstraint context
	 * @throws RecognitionException the recognition exception
	 */
	public final ExpressionconstraintContext expressionconstraint() throws RecognitionException {
		final ExpressionconstraintContext _localctx = new ExpressionconstraintContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_expressionconstraint);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(144);
				ws();
				setState(148);
				_errHandler.sync(this);
				switch (getInterpreter().adaptivePredict(_input, 0, _ctx)) {
				case 1: {
					setState(145);
					refinedexpressionconstraint();
				}
					break;
				case 2: {
					setState(146);
					compoundexpressionconstraint();
				}
					break;
				case 3: {
					setState(147);
					subexpressionconstraint();
				}
					break;
				}
				setState(150);
				ws();
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class RefinedexpressionconstraintContext.
	 */
	public static class RefinedexpressionconstraintContext extends ParserRuleContext {

		/**
		 * Subexpressionconstraint.
		 *
		 * @return the subexpressionconstraint context
		 */
		public SubexpressionconstraintContext subexpressionconstraint() {
			return getRuleContext(SubexpressionconstraintContext.class, 0);
		}

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Colon.
		 *
		 * @return the terminal node
		 */
		public TerminalNode COLON() {
			return getToken(EclParser.COLON, 0);
		}

		/**
		 * Eclrefinement.
		 *
		 * @return the eclrefinement context
		 */
		public EclrefinementContext eclrefinement() {
			return getRuleContext(EclrefinementContext.class, 0);
		}

		/**
		 * Instantiates a {@link RefinedexpressionconstraintContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public RefinedexpressionconstraintContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_refinedexpressionconstraint;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterRefinedexpressionconstraint(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitRefinedexpressionconstraint(this);
			}
		}
	}

	/**
	 * Refinedexpressionconstraint.
	 *
	 * @return the refinedexpressionconstraint context
	 * @throws RecognitionException the recognition exception
	 */
	public final RefinedexpressionconstraintContext refinedexpressionconstraint() throws RecognitionException {
		final RefinedexpressionconstraintContext _localctx = new RefinedexpressionconstraintContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_refinedexpressionconstraint);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(152);
				subexpressionconstraint();
				setState(153);
				ws();
				setState(154);
				match(COLON);
				setState(155);
				ws();
				setState(156);
				eclrefinement();
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class CompoundexpressionconstraintContext.
	 */
	public static class CompoundexpressionconstraintContext extends ParserRuleContext {

		/**
		 * Conjunctionexpressionconstraint.
		 *
		 * @return the conjunctionexpressionconstraint context
		 */
		public ConjunctionexpressionconstraintContext conjunctionexpressionconstraint() {
			return getRuleContext(ConjunctionexpressionconstraintContext.class, 0);
		}

		/**
		 * Disjunctionexpressionconstraint.
		 *
		 * @return the disjunctionexpressionconstraint context
		 */
		public DisjunctionexpressionconstraintContext disjunctionexpressionconstraint() {
			return getRuleContext(DisjunctionexpressionconstraintContext.class, 0);
		}

		/**
		 * Exclusionexpressionconstraint.
		 *
		 * @return the exclusionexpressionconstraint context
		 */
		public ExclusionexpressionconstraintContext exclusionexpressionconstraint() {
			return getRuleContext(ExclusionexpressionconstraintContext.class, 0);
		}

		/**
		 * Instantiates a {@link CompoundexpressionconstraintContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public CompoundexpressionconstraintContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_compoundexpressionconstraint;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterCompoundexpressionconstraint(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitCompoundexpressionconstraint(this);
			}
		}
	}

	/**
	 * Compoundexpressionconstraint.
	 *
	 * @return the compoundexpressionconstraint context
	 * @throws RecognitionException the recognition exception
	 */
	public final CompoundexpressionconstraintContext compoundexpressionconstraint() throws RecognitionException {
		final CompoundexpressionconstraintContext _localctx = new CompoundexpressionconstraintContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_compoundexpressionconstraint);
		try {
			setState(161);
			_errHandler.sync(this);
			switch (getInterpreter().adaptivePredict(_input, 1, _ctx)) {
			case 1:
				enterOuterAlt(_localctx, 1); {
				setState(158);
				conjunctionexpressionconstraint();
			}
				break;
			case 2:
				enterOuterAlt(_localctx, 2); {
				setState(159);
				disjunctionexpressionconstraint();
			}
				break;
			case 3:
				enterOuterAlt(_localctx, 3); {
				setState(160);
				exclusionexpressionconstraint();
			}
				break;
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ConjunctionexpressionconstraintContext.
	 */
	public static class ConjunctionexpressionconstraintContext extends ParserRuleContext {

		/**
		 * Subexpressionconstraint.
		 *
		 * @return the list
		 */
		public List<SubexpressionconstraintContext> subexpressionconstraint() {
			return getRuleContexts(SubexpressionconstraintContext.class);
		}

		/**
		 * Subexpressionconstraint.
		 *
		 * @param i the i
		 * @return the subexpressionconstraint context
		 */
		public SubexpressionconstraintContext subexpressionconstraint(final int i) {
			return getRuleContext(SubexpressionconstraintContext.class, i);
		}

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Conjunction.
		 *
		 * @return the list
		 */
		public List<ConjunctionContext> conjunction() {
			return getRuleContexts(ConjunctionContext.class);
		}

		/**
		 * Conjunction.
		 *
		 * @param i the i
		 * @return the conjunction context
		 */
		public ConjunctionContext conjunction(final int i) {
			return getRuleContext(ConjunctionContext.class, i);
		}

		/**
		 * Instantiates a {@link ConjunctionexpressionconstraintContext} from the
		 * specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ConjunctionexpressionconstraintContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_conjunctionexpressionconstraint;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterConjunctionexpressionconstraint(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitConjunctionexpressionconstraint(this);
			}
		}
	}

	/**
	 * Conjunctionexpressionconstraint.
	 *
	 * @return the conjunctionexpressionconstraint context
	 * @throws RecognitionException the recognition exception
	 */
	public final ConjunctionexpressionconstraintContext conjunctionexpressionconstraint() throws RecognitionException {
		final ConjunctionexpressionconstraintContext _localctx = new ConjunctionexpressionconstraintContext(_ctx,
				getState());
		enterRule(_localctx, 6, RULE_conjunctionexpressionconstraint);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
				setState(163);
				subexpressionconstraint();
				setState(169);
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1: {
						{
							setState(164);
							ws();
							setState(165);
							conjunction();
							setState(166);
							ws();
							setState(167);
							subexpressionconstraint();
						}
					}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(171);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input, 2, _ctx);
				} while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class DisjunctionexpressionconstraintContext.
	 */
	public static class DisjunctionexpressionconstraintContext extends ParserRuleContext {

		/**
		 * Subexpressionconstraint.
		 *
		 * @return the list
		 */
		public List<SubexpressionconstraintContext> subexpressionconstraint() {
			return getRuleContexts(SubexpressionconstraintContext.class);
		}

		/**
		 * Subexpressionconstraint.
		 *
		 * @param i the i
		 * @return the subexpressionconstraint context
		 */
		public SubexpressionconstraintContext subexpressionconstraint(final int i) {
			return getRuleContext(SubexpressionconstraintContext.class, i);
		}

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Disjunction.
		 *
		 * @return the list
		 */
		public List<DisjunctionContext> disjunction() {
			return getRuleContexts(DisjunctionContext.class);
		}

		/**
		 * Disjunction.
		 *
		 * @param i the i
		 * @return the disjunction context
		 */
		public DisjunctionContext disjunction(final int i) {
			return getRuleContext(DisjunctionContext.class, i);
		}

		/**
		 * Instantiates a {@link DisjunctionexpressionconstraintContext} from the
		 * specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public DisjunctionexpressionconstraintContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_disjunctionexpressionconstraint;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterDisjunctionexpressionconstraint(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitDisjunctionexpressionconstraint(this);
			}
		}
	}

	/**
	 * Disjunctionexpressionconstraint.
	 *
	 * @return the disjunctionexpressionconstraint context
	 * @throws RecognitionException the recognition exception
	 */
	public final DisjunctionexpressionconstraintContext disjunctionexpressionconstraint() throws RecognitionException {
		final DisjunctionexpressionconstraintContext _localctx = new DisjunctionexpressionconstraintContext(_ctx,
				getState());
		enterRule(_localctx, 8, RULE_disjunctionexpressionconstraint);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
				setState(173);
				subexpressionconstraint();
				setState(179);
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1: {
						{
							setState(174);
							ws();
							setState(175);
							disjunction();
							setState(176);
							ws();
							setState(177);
							subexpressionconstraint();
						}
					}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(181);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input, 3, _ctx);
				} while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ExclusionexpressionconstraintContext.
	 */
	public static class ExclusionexpressionconstraintContext extends ParserRuleContext {

		/**
		 * Subexpressionconstraint.
		 *
		 * @return the list
		 */
		public List<SubexpressionconstraintContext> subexpressionconstraint() {
			return getRuleContexts(SubexpressionconstraintContext.class);
		}

		/**
		 * Subexpressionconstraint.
		 *
		 * @param i the i
		 * @return the subexpressionconstraint context
		 */
		public SubexpressionconstraintContext subexpressionconstraint(final int i) {
			return getRuleContext(SubexpressionconstraintContext.class, i);
		}

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Exclusion.
		 *
		 * @return the exclusion context
		 */
		public ExclusionContext exclusion() {
			return getRuleContext(ExclusionContext.class, 0);
		}

		/**
		 * Instantiates a {@link ExclusionexpressionconstraintContext} from the
		 * specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ExclusionexpressionconstraintContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_exclusionexpressionconstraint;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterExclusionexpressionconstraint(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitExclusionexpressionconstraint(this);
			}
		}
	}

	/**
	 * Exclusionexpressionconstraint.
	 *
	 * @return the exclusionexpressionconstraint context
	 * @throws RecognitionException the recognition exception
	 */
	public final ExclusionexpressionconstraintContext exclusionexpressionconstraint() throws RecognitionException {
		final ExclusionexpressionconstraintContext _localctx = new ExclusionexpressionconstraintContext(_ctx,
				getState());
		enterRule(_localctx, 10, RULE_exclusionexpressionconstraint);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(183);
				subexpressionconstraint();
				setState(184);
				ws();
				setState(185);
				exclusion();
				setState(186);
				ws();
				setState(187);
				subexpressionconstraint();
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class SubexpressionconstraintContext.
	 */
	public static class SubexpressionconstraintContext extends ParserRuleContext {

		/**
		 * Eclfocusconcept.
		 *
		 * @return the eclfocusconcept context
		 */
		public EclfocusconceptContext eclfocusconcept() {
			return getRuleContext(EclfocusconceptContext.class, 0);
		}

		/**
		 * Constraintoperator.
		 *
		 * @return the constraintoperator context
		 */
		public ConstraintoperatorContext constraintoperator() {
			return getRuleContext(ConstraintoperatorContext.class, 0);
		}

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Memberof.
		 *
		 * @return the memberof context
		 */
		public MemberofContext memberof() {
			return getRuleContext(MemberofContext.class, 0);
		}

		/**
		 * Left paren.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_PAREN() {
			return getToken(EclParser.LEFT_PAREN, 0);
		}

		/**
		 * Expressionconstraint.
		 *
		 * @return the expressionconstraint context
		 */
		public ExpressionconstraintContext expressionconstraint() {
			return getRuleContext(ExpressionconstraintContext.class, 0);
		}

		/**
		 * Right paren.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_PAREN() {
			return getToken(EclParser.RIGHT_PAREN, 0);
		}

		/**
		 * Instantiates a {@link SubexpressionconstraintContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public SubexpressionconstraintContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_subexpressionconstraint;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterSubexpressionconstraint(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitSubexpressionconstraint(this);
			}
		}
	}

	/**
	 * Subexpressionconstraint.
	 *
	 * @return the subexpressionconstraint context
	 * @throws RecognitionException the recognition exception
	 */
	public final SubexpressionconstraintContext subexpressionconstraint() throws RecognitionException {
		final SubexpressionconstraintContext _localctx = new SubexpressionconstraintContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_subexpressionconstraint);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(192);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == LESS_THAN || _la == GREATER_THAN) {
					{
						setState(189);
						constraintoperator();
						setState(190);
						ws();
					}
				}

				setState(197);
				_errHandler.sync(this);
				switch (getInterpreter().adaptivePredict(_input, 5, _ctx)) {
				case 1: {
					setState(194);
					memberof();
					setState(195);
					ws();
				}
					break;
				}
				setState(206);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ASTERISK:
				case PLUS:
				case DASH:
				case PERIOD:
				case ZERO:
				case ONE:
				case TWO:
				case THREE:
				case FOUR:
				case FIVE:
				case SIX:
				case SEVEN:
				case EIGHT:
				case NINE:
				case COLON:
				case AT:
				case CAP_A:
				case CAP_B:
				case CAP_C:
				case CAP_D:
				case CAP_E:
				case CAP_F:
				case CAP_G:
				case CAP_H:
				case CAP_I:
				case CAP_J:
				case CAP_K:
				case CAP_L:
				case CAP_M:
				case CAP_N:
				case CAP_O:
				case CAP_P:
				case CAP_Q:
				case CAP_R:
				case CAP_S:
				case CAP_T:
				case CAP_U:
				case CAP_V:
				case CAP_W:
				case CAP_X:
				case CAP_Y:
				case CAP_Z:
				case CARAT:
				case UNDERSCORE:
				case A:
				case B:
				case C:
				case D:
				case E:
				case F:
				case G:
				case H:
				case I:
				case J:
				case K:
				case L:
				case M:
				case N:
				case O:
				case P:
				case Q:
				case R:
				case S:
				case T:
				case U:
				case V:
				case W:
				case X:
				case Y:
				case Z: {
					setState(199);
					eclfocusconcept();
				}
					break;
				case LEFT_PAREN: {
					{
						setState(200);
						match(LEFT_PAREN);
						setState(201);
						ws();
						setState(202);
						expressionconstraint();
						setState(203);
						ws();
						setState(204);
						match(RIGHT_PAREN);
					}
				}
					break;
				default:
					throw new NoViableAltException(this);
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class EclfocusconceptContext.
	 */
	public static class EclfocusconceptContext extends ParserRuleContext {

		/**
		 * Eclconceptreference.
		 *
		 * @return the eclconceptreference context
		 */
		public EclconceptreferenceContext eclconceptreference() {
			return getRuleContext(EclconceptreferenceContext.class, 0);
		}

		/**
		 * Wildcard.
		 *
		 * @return the wildcard context
		 */
		public WildcardContext wildcard() {
			return getRuleContext(WildcardContext.class, 0);
		}

		/**
		 * Instantiates a {@link EclfocusconceptContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public EclfocusconceptContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_eclfocusconcept;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterEclfocusconcept(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitEclfocusconcept(this);
			}
		}
	}

	/**
	 * Eclfocusconcept.
	 *
	 * @return the eclfocusconcept context
	 * @throws RecognitionException the recognition exception
	 */
	public final EclfocusconceptContext eclfocusconcept() throws RecognitionException {
		final EclfocusconceptContext _localctx = new EclfocusconceptContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_eclfocusconcept);
		try {
			setState(210);
			_errHandler.sync(this);
			switch (getInterpreter().adaptivePredict(_input, 7, _ctx)) {
			case 1:
				enterOuterAlt(_localctx, 1); {
				setState(208);
				eclconceptreference();
			}
				break;
			case 2:
				enterOuterAlt(_localctx, 2); {
				setState(209);
				wildcard();
			}
				break;
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class MemberofContext.
	 */
	public static class MemberofContext extends ParserRuleContext {

		/**
		 * Carat.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CARAT() {
			return getToken(EclParser.CARAT, 0);
		}

		/**
		 * Instantiates a {@link MemberofContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public MemberofContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_memberof;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterMemberof(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitMemberof(this);
			}
		}
	}

	/**
	 * Memberof.
	 *
	 * @return the memberof context
	 * @throws RecognitionException the recognition exception
	 */
	public final MemberofContext memberof() throws RecognitionException {
		final MemberofContext _localctx = new MemberofContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_memberof);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(212);
				match(CARAT);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class EclconceptreferenceContext.
	 */
	public static class EclconceptreferenceContext extends ParserRuleContext {

		/**
		 * Conceptid.
		 *
		 * @return the conceptid context
		 */
		public ConceptidContext conceptid() {
			return getRuleContext(ConceptidContext.class, 0);
		}

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Pipe.
		 *
		 * @return the list
		 */
		public List<TerminalNode> PIPE() {
			return getTokens(EclParser.PIPE);
		}

		/**
		 * Pipe.
		 *
		 * @param i the i
		 * @return the terminal node
		 */
		public TerminalNode PIPE(final int i) {
			return getToken(EclParser.PIPE, i);
		}

		/**
		 * Term.
		 *
		 * @return the term context
		 */
		public TermContext term() {
			return getRuleContext(TermContext.class, 0);
		}

		/**
		 * Instantiates a {@link EclconceptreferenceContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public EclconceptreferenceContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_eclconceptreference;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterEclconceptreference(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitEclconceptreference(this);
			}
		}
	}

	/**
	 * Eclconceptreference.
	 *
	 * @return the eclconceptreference context
	 * @throws RecognitionException the recognition exception
	 */
	public final EclconceptreferenceContext eclconceptreference() throws RecognitionException {
		final EclconceptreferenceContext _localctx = new EclconceptreferenceContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_eclconceptreference);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(214);
				conceptid();
				setState(222);
				_errHandler.sync(this);
				switch (getInterpreter().adaptivePredict(_input, 8, _ctx)) {
				case 1: {
					setState(215);
					ws();
					setState(216);
					match(PIPE);
					setState(217);
					ws();
					setState(218);
					term();
					setState(219);
					ws();
					setState(220);
					match(PIPE);
				}
					break;
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ConceptidContext.
	 */
	public static class ConceptidContext extends ParserRuleContext {

		/**
		 * Sctid.
		 *
		 * @return the sctid context
		 */
		public SctidContext sctid() {
			return getRuleContext(SctidContext.class, 0);
		}

		/**
		 * Instantiates a {@link ConceptidContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ConceptidContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_conceptid;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterConceptid(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitConceptid(this);
			}
		}
	}

	/**
	 * Conceptid.
	 *
	 * @return the conceptid context
	 * @throws RecognitionException the recognition exception
	 */
	public final ConceptidContext conceptid() throws RecognitionException {
		final ConceptidContext _localctx = new ConceptidContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_conceptid);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(224);
				sctid();
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class TermContext.
	 */
	public static class TermContext extends ParserRuleContext {

		/**
		 * Nonwsnonpipe.
		 *
		 * @return the list
		 */
		public List<NonwsnonpipeContext> nonwsnonpipe() {
			return getRuleContexts(NonwsnonpipeContext.class);
		}

		/**
		 * Nonwsnonpipe.
		 *
		 * @param i the i
		 * @return the nonwsnonpipe context
		 */
		public NonwsnonpipeContext nonwsnonpipe(final int i) {
			return getRuleContext(NonwsnonpipeContext.class, i);
		}

		/**
		 * Sp.
		 *
		 * @return the list
		 */
		public List<SpContext> sp() {
			return getRuleContexts(SpContext.class);
		}

		/**
		 * Sp.
		 *
		 * @param i the i
		 * @return the sp context
		 */
		public SpContext sp(final int i) {
			return getRuleContext(SpContext.class, i);
		}

		/**
		 * Instantiates a {@link TermContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public TermContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_term;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterTerm(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitTerm(this);
			}
		}
	}

	/**
	 * Term.
	 *
	 * @return the term context
	 * @throws RecognitionException the recognition exception
	 */
	public final TermContext term() throws RecognitionException {
		final TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_term);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
				setState(227);
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1: {
						{
							setState(226);
							nonwsnonpipe();
						}
					}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(229);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input, 9, _ctx);
				} while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
				setState(243);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input, 12, _ctx);
				while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
					if (_alt == 1) {
						{
							{
								setState(232);
								_errHandler.sync(this);
								_la = _input.LA(1);
								do {
									{
										{
											setState(231);
											sp();
										}
									}
									setState(234);
									_errHandler.sync(this);
									_la = _input.LA(1);
								} while (_la == SPACE);
								setState(237);
								_errHandler.sync(this);
								_alt = 1;
								do {
									switch (_alt) {
									case 1: {
										{
											setState(236);
											nonwsnonpipe();
										}
									}
										break;
									default:
										throw new NoViableAltException(this);
									}
									setState(239);
									_errHandler.sync(this);
									_alt = getInterpreter().adaptivePredict(_input, 11, _ctx);
								} while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
							}
						}
					}
					setState(245);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input, 12, _ctx);
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class WildcardContext.
	 */
	public static class WildcardContext extends ParserRuleContext {

		/**
		 * Asterisk.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ASTERISK() {
			return getToken(EclParser.ASTERISK, 0);
		}

		/**
		 * Instantiates a {@link WildcardContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public WildcardContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_wildcard;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterWildcard(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitWildcard(this);
			}
		}
	}

	/**
	 * Wildcard.
	 *
	 * @return the wildcard context
	 * @throws RecognitionException the recognition exception
	 */
	public final WildcardContext wildcard() throws RecognitionException {
		final WildcardContext _localctx = new WildcardContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_wildcard);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(246);
				match(ASTERISK);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ConstraintoperatorContext.
	 */
	public static class ConstraintoperatorContext extends ParserRuleContext {

		/**
		 * Childof.
		 *
		 * @return the childof context
		 */
		public ChildofContext childof() {
			return getRuleContext(ChildofContext.class, 0);
		}

		/**
		 * Descendantorselfof.
		 *
		 * @return the descendantorselfof context
		 */
		public DescendantorselfofContext descendantorselfof() {
			return getRuleContext(DescendantorselfofContext.class, 0);
		}

		/**
		 * Descendantof.
		 *
		 * @return the descendantof context
		 */
		public DescendantofContext descendantof() {
			return getRuleContext(DescendantofContext.class, 0);
		}

		/**
		 * Parentof.
		 *
		 * @return the parentof context
		 */
		public ParentofContext parentof() {
			return getRuleContext(ParentofContext.class, 0);
		}

		/**
		 * Ancestororselfof.
		 *
		 * @return the ancestororselfof context
		 */
		public AncestororselfofContext ancestororselfof() {
			return getRuleContext(AncestororselfofContext.class, 0);
		}

		/**
		 * Ancestorof.
		 *
		 * @return the ancestorof context
		 */
		public AncestorofContext ancestorof() {
			return getRuleContext(AncestorofContext.class, 0);
		}

		/**
		 * Instantiates a {@link ConstraintoperatorContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ConstraintoperatorContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_constraintoperator;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterConstraintoperator(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitConstraintoperator(this);
			}
		}
	}

	/**
	 * Constraintoperator.
	 *
	 * @return the constraintoperator context
	 * @throws RecognitionException the recognition exception
	 */
	public final ConstraintoperatorContext constraintoperator() throws RecognitionException {
		final ConstraintoperatorContext _localctx = new ConstraintoperatorContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_constraintoperator);
		try {
			setState(254);
			_errHandler.sync(this);
			switch (getInterpreter().adaptivePredict(_input, 13, _ctx)) {
			case 1:
				enterOuterAlt(_localctx, 1); {
				setState(248);
				childof();
			}
				break;
			case 2:
				enterOuterAlt(_localctx, 2); {
				setState(249);
				descendantorselfof();
			}
				break;
			case 3:
				enterOuterAlt(_localctx, 3); {
				setState(250);
				descendantof();
			}
				break;
			case 4:
				enterOuterAlt(_localctx, 4); {
				setState(251);
				parentof();
			}
				break;
			case 5:
				enterOuterAlt(_localctx, 5); {
				setState(252);
				ancestororselfof();
			}
				break;
			case 6:
				enterOuterAlt(_localctx, 6); {
				setState(253);
				ancestorof();
			}
				break;
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class DescendantofContext.
	 */
	public static class DescendantofContext extends ParserRuleContext {

		/**
		 * Less than.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LESS_THAN() {
			return getToken(EclParser.LESS_THAN, 0);
		}

		/**
		 * Instantiates a {@link DescendantofContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public DescendantofContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_descendantof;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterDescendantof(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitDescendantof(this);
			}
		}
	}

	/**
	 * Descendantof.
	 *
	 * @return the descendantof context
	 * @throws RecognitionException the recognition exception
	 */
	public final DescendantofContext descendantof() throws RecognitionException {
		final DescendantofContext _localctx = new DescendantofContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_descendantof);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(256);
				match(LESS_THAN);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class DescendantorselfofContext.
	 */
	public static class DescendantorselfofContext extends ParserRuleContext {

		/**
		 * Less than.
		 *
		 * @return the list
		 */
		public List<TerminalNode> LESS_THAN() {
			return getTokens(EclParser.LESS_THAN);
		}

		/**
		 * Less than.
		 *
		 * @param i the i
		 * @return the terminal node
		 */
		public TerminalNode LESS_THAN(final int i) {
			return getToken(EclParser.LESS_THAN, i);
		}

		/**
		 * Instantiates a {@link DescendantorselfofContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public DescendantorselfofContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_descendantorselfof;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterDescendantorselfof(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitDescendantorselfof(this);
			}
		}
	}

	/**
	 * Descendantorselfof.
	 *
	 * @return the descendantorselfof context
	 * @throws RecognitionException the recognition exception
	 */
	public final DescendantorselfofContext descendantorselfof() throws RecognitionException {
		final DescendantorselfofContext _localctx = new DescendantorselfofContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_descendantorselfof);
		try {
			enterOuterAlt(_localctx, 1);
			{
				{
					setState(258);
					match(LESS_THAN);
					setState(259);
					match(LESS_THAN);
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ChildofContext.
	 */
	public static class ChildofContext extends ParserRuleContext {

		/**
		 * Less than.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LESS_THAN() {
			return getToken(EclParser.LESS_THAN, 0);
		}

		/**
		 * Exclamation.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EXCLAMATION() {
			return getToken(EclParser.EXCLAMATION, 0);
		}

		/**
		 * Instantiates a {@link ChildofContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ChildofContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_childof;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterChildof(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitChildof(this);
			}
		}
	}

	/**
	 * Childof.
	 *
	 * @return the childof context
	 * @throws RecognitionException the recognition exception
	 */
	public final ChildofContext childof() throws RecognitionException {
		final ChildofContext _localctx = new ChildofContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_childof);
		try {
			enterOuterAlt(_localctx, 1);
			{
				{
					setState(261);
					match(LESS_THAN);
					setState(262);
					match(EXCLAMATION);
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class AncestorofContext.
	 */
	public static class AncestorofContext extends ParserRuleContext {

		/**
		 * Greater than.
		 *
		 * @return the terminal node
		 */
		public TerminalNode GREATER_THAN() {
			return getToken(EclParser.GREATER_THAN, 0);
		}

		/**
		 * Instantiates a {@link AncestorofContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public AncestorofContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_ancestorof;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterAncestorof(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitAncestorof(this);
			}
		}
	}

	/**
	 * Ancestorof.
	 *
	 * @return the ancestorof context
	 * @throws RecognitionException the recognition exception
	 */
	public final AncestorofContext ancestorof() throws RecognitionException {
		final AncestorofContext _localctx = new AncestorofContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_ancestorof);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(264);
				match(GREATER_THAN);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class AncestororselfofContext.
	 */
	public static class AncestororselfofContext extends ParserRuleContext {

		/**
		 * Greater than.
		 *
		 * @return the list
		 */
		public List<TerminalNode> GREATER_THAN() {
			return getTokens(EclParser.GREATER_THAN);
		}

		/**
		 * Greater than.
		 *
		 * @param i the i
		 * @return the terminal node
		 */
		public TerminalNode GREATER_THAN(final int i) {
			return getToken(EclParser.GREATER_THAN, i);
		}

		/**
		 * Instantiates a {@link AncestororselfofContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public AncestororselfofContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_ancestororselfof;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterAncestororselfof(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitAncestororselfof(this);
			}
		}
	}

	/**
	 * Ancestororselfof.
	 *
	 * @return the ancestororselfof context
	 * @throws RecognitionException the recognition exception
	 */
	public final AncestororselfofContext ancestororselfof() throws RecognitionException {
		final AncestororselfofContext _localctx = new AncestororselfofContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_ancestororselfof);
		try {
			enterOuterAlt(_localctx, 1);
			{
				{
					setState(266);
					match(GREATER_THAN);
					setState(267);
					match(GREATER_THAN);
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ParentofContext.
	 */
	public static class ParentofContext extends ParserRuleContext {

		/**
		 * Greater than.
		 *
		 * @return the terminal node
		 */
		public TerminalNode GREATER_THAN() {
			return getToken(EclParser.GREATER_THAN, 0);
		}

		/**
		 * Exclamation.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EXCLAMATION() {
			return getToken(EclParser.EXCLAMATION, 0);
		}

		/**
		 * Instantiates a {@link ParentofContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ParentofContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_parentof;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterParentof(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitParentof(this);
			}
		}
	}

	/**
	 * Parentof.
	 *
	 * @return the parentof context
	 * @throws RecognitionException the recognition exception
	 */
	public final ParentofContext parentof() throws RecognitionException {
		final ParentofContext _localctx = new ParentofContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_parentof);
		try {
			enterOuterAlt(_localctx, 1);
			{
				{
					setState(269);
					match(GREATER_THAN);
					setState(270);
					match(EXCLAMATION);
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ConjunctionContext.
	 */
	public static class ConjunctionContext extends ParserRuleContext {

		/**
		 * Mws.
		 *
		 * @return the mws context
		 */
		public MwsContext mws() {
			return getRuleContext(MwsContext.class, 0);
		}

		/**
		 * A.
		 *
		 * @return the terminal node
		 */
		public TerminalNode A() {
			return getToken(EclParser.A, 0);
		}

		/**
		 * Cap a.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_A() {
			return getToken(EclParser.CAP_A, 0);
		}

		/**
		 * N.
		 *
		 * @return the terminal node
		 */
		public TerminalNode N() {
			return getToken(EclParser.N, 0);
		}

		/**
		 * Cap n.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_N() {
			return getToken(EclParser.CAP_N, 0);
		}

		/**
		 * D.
		 *
		 * @return the terminal node
		 */
		public TerminalNode D() {
			return getToken(EclParser.D, 0);
		}

		/**
		 * Cap d.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_D() {
			return getToken(EclParser.CAP_D, 0);
		}

		/**
		 * Comma.
		 *
		 * @return the terminal node
		 */
		public TerminalNode COMMA() {
			return getToken(EclParser.COMMA, 0);
		}

		/**
		 * Instantiates a {@link ConjunctionContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ConjunctionContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_conjunction;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterConjunction(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitConjunction(this);
			}
		}
	}

	/**
	 * Conjunction.
	 *
	 * @return the conjunction context
	 * @throws RecognitionException the recognition exception
	 */
	public final ConjunctionContext conjunction() throws RecognitionException {
		final ConjunctionContext _localctx = new ConjunctionContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_conjunction);
		int _la;
		try {
			setState(277);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CAP_A:
			case A:
				enterOuterAlt(_localctx, 1); {
				{
					setState(272);
					_la = _input.LA(1);
					if (!(_la == CAP_A || _la == A)) {
						_errHandler.recoverInline(this);
					} else {
						if (_input.LA(1) == Token.EOF) {
							matchedEOF = true;
						}
						_errHandler.reportMatch(this);
						consume();
					}
					setState(273);
					_la = _input.LA(1);
					if (!(_la == CAP_N || _la == N)) {
						_errHandler.recoverInline(this);
					} else {
						if (_input.LA(1) == Token.EOF) {
							matchedEOF = true;
						}
						_errHandler.reportMatch(this);
						consume();
					}
					setState(274);
					_la = _input.LA(1);
					if (!(_la == CAP_D || _la == D)) {
						_errHandler.recoverInline(this);
					} else {
						if (_input.LA(1) == Token.EOF) {
							matchedEOF = true;
						}
						_errHandler.reportMatch(this);
						consume();
					}
					setState(275);
					mws();
				}
			}
				break;
			case COMMA:
				enterOuterAlt(_localctx, 2); {
				setState(276);
				match(COMMA);
			}
				break;
			default:
				throw new NoViableAltException(this);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class DisjunctionContext.
	 */
	public static class DisjunctionContext extends ParserRuleContext {

		/**
		 * Mws.
		 *
		 * @return the mws context
		 */
		public MwsContext mws() {
			return getRuleContext(MwsContext.class, 0);
		}

		/**
		 * O.
		 *
		 * @return the terminal node
		 */
		public TerminalNode O() {
			return getToken(EclParser.O, 0);
		}

		/**
		 * Cap o.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_O() {
			return getToken(EclParser.CAP_O, 0);
		}

		/**
		 * R.
		 *
		 * @return the terminal node
		 */
		public TerminalNode R() {
			return getToken(EclParser.R, 0);
		}

		/**
		 * Cap r.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_R() {
			return getToken(EclParser.CAP_R, 0);
		}

		/**
		 * Instantiates a {@link DisjunctionContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public DisjunctionContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_disjunction;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterDisjunction(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitDisjunction(this);
			}
		}
	}

	/**
	 * Disjunction.
	 *
	 * @return the disjunction context
	 * @throws RecognitionException the recognition exception
	 */
	public final DisjunctionContext disjunction() throws RecognitionException {
		final DisjunctionContext _localctx = new DisjunctionContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_disjunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(279);
				_la = _input.LA(1);
				if (!(_la == CAP_O || _la == O)) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
				setState(280);
				_la = _input.LA(1);
				if (!(_la == CAP_R || _la == R)) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
				setState(281);
				mws();
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ExclusionContext.
	 */
	public static class ExclusionContext extends ParserRuleContext {

		/**
		 * Mws.
		 *
		 * @return the mws context
		 */
		public MwsContext mws() {
			return getRuleContext(MwsContext.class, 0);
		}

		/**
		 * M.
		 *
		 * @return the terminal node
		 */
		public TerminalNode M() {
			return getToken(EclParser.M, 0);
		}

		/**
		 * Cap m.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_M() {
			return getToken(EclParser.CAP_M, 0);
		}

		/**
		 * I.
		 *
		 * @return the terminal node
		 */
		public TerminalNode I() {
			return getToken(EclParser.I, 0);
		}

		/**
		 * Cap i.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_I() {
			return getToken(EclParser.CAP_I, 0);
		}

		/**
		 * N.
		 *
		 * @return the terminal node
		 */
		public TerminalNode N() {
			return getToken(EclParser.N, 0);
		}

		/**
		 * Cap n.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_N() {
			return getToken(EclParser.CAP_N, 0);
		}

		/**
		 * U.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U() {
			return getToken(EclParser.U, 0);
		}

		/**
		 * Cap u.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_U() {
			return getToken(EclParser.CAP_U, 0);
		}

		/**
		 * S.
		 *
		 * @return the terminal node
		 */
		public TerminalNode S() {
			return getToken(EclParser.S, 0);
		}

		/**
		 * Cap s.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_S() {
			return getToken(EclParser.CAP_S, 0);
		}

		/**
		 * Instantiates a {@link ExclusionContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ExclusionContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_exclusion;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterExclusion(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitExclusion(this);
			}
		}
	}

	/**
	 * Exclusion.
	 *
	 * @return the exclusion context
	 * @throws RecognitionException the recognition exception
	 */
	public final ExclusionContext exclusion() throws RecognitionException {
		final ExclusionContext _localctx = new ExclusionContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_exclusion);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(283);
				_la = _input.LA(1);
				if (!(_la == CAP_M || _la == M)) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
				setState(284);
				_la = _input.LA(1);
				if (!(_la == CAP_I || _la == I)) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
				setState(285);
				_la = _input.LA(1);
				if (!(_la == CAP_N || _la == N)) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
				setState(286);
				_la = _input.LA(1);
				if (!(_la == CAP_U || _la == U)) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
				setState(287);
				_la = _input.LA(1);
				if (!(_la == CAP_S || _la == S)) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
				setState(288);
				mws();
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class EclrefinementContext.
	 */
	public static class EclrefinementContext extends ParserRuleContext {

		/**
		 * Subrefinement.
		 *
		 * @return the subrefinement context
		 */
		public SubrefinementContext subrefinement() {
			return getRuleContext(SubrefinementContext.class, 0);
		}

		/**
		 * Ws.
		 *
		 * @return the ws context
		 */
		public WsContext ws() {
			return getRuleContext(WsContext.class, 0);
		}

		/**
		 * Conjunctionrefinementset.
		 *
		 * @return the conjunctionrefinementset context
		 */
		public ConjunctionrefinementsetContext conjunctionrefinementset() {
			return getRuleContext(ConjunctionrefinementsetContext.class, 0);
		}

		/**
		 * Disjunctionrefinementset.
		 *
		 * @return the disjunctionrefinementset context
		 */
		public DisjunctionrefinementsetContext disjunctionrefinementset() {
			return getRuleContext(DisjunctionrefinementsetContext.class, 0);
		}

		/**
		 * Instantiates a {@link EclrefinementContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public EclrefinementContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_eclrefinement;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterEclrefinement(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitEclrefinement(this);
			}
		}
	}

	/**
	 * Eclrefinement.
	 *
	 * @return the eclrefinement context
	 * @throws RecognitionException the recognition exception
	 */
	public final EclrefinementContext eclrefinement() throws RecognitionException {
		final EclrefinementContext _localctx = new EclrefinementContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_eclrefinement);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(290);
				subrefinement();
				setState(291);
				ws();
				setState(294);
				_errHandler.sync(this);
				switch (getInterpreter().adaptivePredict(_input, 15, _ctx)) {
				case 1: {
					setState(292);
					conjunctionrefinementset();
				}
					break;
				case 2: {
					setState(293);
					disjunctionrefinementset();
				}
					break;
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ConjunctionrefinementsetContext.
	 */
	public static class ConjunctionrefinementsetContext extends ParserRuleContext {

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Conjunction.
		 *
		 * @return the list
		 */
		public List<ConjunctionContext> conjunction() {
			return getRuleContexts(ConjunctionContext.class);
		}

		/**
		 * Conjunction.
		 *
		 * @param i the i
		 * @return the conjunction context
		 */
		public ConjunctionContext conjunction(final int i) {
			return getRuleContext(ConjunctionContext.class, i);
		}

		/**
		 * Subrefinement.
		 *
		 * @return the list
		 */
		public List<SubrefinementContext> subrefinement() {
			return getRuleContexts(SubrefinementContext.class);
		}

		/**
		 * Subrefinement.
		 *
		 * @param i the i
		 * @return the subrefinement context
		 */
		public SubrefinementContext subrefinement(final int i) {
			return getRuleContext(SubrefinementContext.class, i);
		}

		/**
		 * Instantiates a {@link ConjunctionrefinementsetContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ConjunctionrefinementsetContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_conjunctionrefinementset;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterConjunctionrefinementset(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitConjunctionrefinementset(this);
			}
		}
	}

	/**
	 * Conjunctionrefinementset.
	 *
	 * @return the conjunctionrefinementset context
	 * @throws RecognitionException the recognition exception
	 */
	public final ConjunctionrefinementsetContext conjunctionrefinementset() throws RecognitionException {
		final ConjunctionrefinementsetContext _localctx = new ConjunctionrefinementsetContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_conjunctionrefinementset);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
				setState(301);
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1: {
						{
							setState(296);
							ws();
							setState(297);
							conjunction();
							setState(298);
							ws();
							setState(299);
							subrefinement();
						}
					}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(303);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input, 16, _ctx);
				} while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class DisjunctionrefinementsetContext.
	 */
	public static class DisjunctionrefinementsetContext extends ParserRuleContext {

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Disjunction.
		 *
		 * @return the list
		 */
		public List<DisjunctionContext> disjunction() {
			return getRuleContexts(DisjunctionContext.class);
		}

		/**
		 * Disjunction.
		 *
		 * @param i the i
		 * @return the disjunction context
		 */
		public DisjunctionContext disjunction(final int i) {
			return getRuleContext(DisjunctionContext.class, i);
		}

		/**
		 * Subrefinement.
		 *
		 * @return the list
		 */
		public List<SubrefinementContext> subrefinement() {
			return getRuleContexts(SubrefinementContext.class);
		}

		/**
		 * Subrefinement.
		 *
		 * @param i the i
		 * @return the subrefinement context
		 */
		public SubrefinementContext subrefinement(final int i) {
			return getRuleContext(SubrefinementContext.class, i);
		}

		/**
		 * Instantiates a {@link DisjunctionrefinementsetContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public DisjunctionrefinementsetContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_disjunctionrefinementset;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterDisjunctionrefinementset(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitDisjunctionrefinementset(this);
			}
		}
	}

	/**
	 * Disjunctionrefinementset.
	 *
	 * @return the disjunctionrefinementset context
	 * @throws RecognitionException the recognition exception
	 */
	public final DisjunctionrefinementsetContext disjunctionrefinementset() throws RecognitionException {
		final DisjunctionrefinementsetContext _localctx = new DisjunctionrefinementsetContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_disjunctionrefinementset);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
				setState(310);
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1: {
						{
							setState(305);
							ws();
							setState(306);
							disjunction();
							setState(307);
							ws();
							setState(308);
							subrefinement();
						}
					}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(312);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input, 17, _ctx);
				} while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class SubrefinementContext.
	 */
	public static class SubrefinementContext extends ParserRuleContext {

		/**
		 * Eclattributeset.
		 *
		 * @return the eclattributeset context
		 */
		public EclattributesetContext eclattributeset() {
			return getRuleContext(EclattributesetContext.class, 0);
		}

		/**
		 * Eclattributegroup.
		 *
		 * @return the eclattributegroup context
		 */
		public EclattributegroupContext eclattributegroup() {
			return getRuleContext(EclattributegroupContext.class, 0);
		}

		/**
		 * Left paren.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_PAREN() {
			return getToken(EclParser.LEFT_PAREN, 0);
		}

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Eclrefinement.
		 *
		 * @return the eclrefinement context
		 */
		public EclrefinementContext eclrefinement() {
			return getRuleContext(EclrefinementContext.class, 0);
		}

		/**
		 * Right paren.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_PAREN() {
			return getToken(EclParser.RIGHT_PAREN, 0);
		}

		/**
		 * Instantiates a {@link SubrefinementContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public SubrefinementContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_subrefinement;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterSubrefinement(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitSubrefinement(this);
			}
		}
	}

	/**
	 * Subrefinement.
	 *
	 * @return the subrefinement context
	 * @throws RecognitionException the recognition exception
	 */
	public final SubrefinementContext subrefinement() throws RecognitionException {
		final SubrefinementContext _localctx = new SubrefinementContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_subrefinement);
		try {
			setState(322);
			_errHandler.sync(this);
			switch (getInterpreter().adaptivePredict(_input, 18, _ctx)) {
			case 1:
				enterOuterAlt(_localctx, 1); {
				setState(314);
				eclattributeset();
			}
				break;
			case 2:
				enterOuterAlt(_localctx, 2); {
				setState(315);
				eclattributegroup();
			}
				break;
			case 3:
				enterOuterAlt(_localctx, 3); {
				{
					setState(316);
					match(LEFT_PAREN);
					setState(317);
					ws();
					setState(318);
					eclrefinement();
					setState(319);
					ws();
					setState(320);
					match(RIGHT_PAREN);
				}
			}
				break;
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class EclattributesetContext.
	 */
	public static class EclattributesetContext extends ParserRuleContext {

		/**
		 * Subattributeset.
		 *
		 * @return the subattributeset context
		 */
		public SubattributesetContext subattributeset() {
			return getRuleContext(SubattributesetContext.class, 0);
		}

		/**
		 * Ws.
		 *
		 * @return the ws context
		 */
		public WsContext ws() {
			return getRuleContext(WsContext.class, 0);
		}

		/**
		 * Conjunctionattributeset.
		 *
		 * @return the conjunctionattributeset context
		 */
		public ConjunctionattributesetContext conjunctionattributeset() {
			return getRuleContext(ConjunctionattributesetContext.class, 0);
		}

		/**
		 * Disjunctionattributeset.
		 *
		 * @return the disjunctionattributeset context
		 */
		public DisjunctionattributesetContext disjunctionattributeset() {
			return getRuleContext(DisjunctionattributesetContext.class, 0);
		}

		/**
		 * Instantiates a {@link EclattributesetContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public EclattributesetContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_eclattributeset;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterEclattributeset(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitEclattributeset(this);
			}
		}
	}

	/**
	 * Eclattributeset.
	 *
	 * @return the eclattributeset context
	 * @throws RecognitionException the recognition exception
	 */
	public final EclattributesetContext eclattributeset() throws RecognitionException {
		final EclattributesetContext _localctx = new EclattributesetContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_eclattributeset);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(324);
				subattributeset();
				setState(325);
				ws();
				setState(328);
				_errHandler.sync(this);
				switch (getInterpreter().adaptivePredict(_input, 19, _ctx)) {
				case 1: {
					setState(326);
					conjunctionattributeset();
				}
					break;
				case 2: {
					setState(327);
					disjunctionattributeset();
				}
					break;
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ConjunctionattributesetContext.
	 */
	public static class ConjunctionattributesetContext extends ParserRuleContext {

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Conjunction.
		 *
		 * @return the list
		 */
		public List<ConjunctionContext> conjunction() {
			return getRuleContexts(ConjunctionContext.class);
		}

		/**
		 * Conjunction.
		 *
		 * @param i the i
		 * @return the conjunction context
		 */
		public ConjunctionContext conjunction(final int i) {
			return getRuleContext(ConjunctionContext.class, i);
		}

		/**
		 * Subattributeset.
		 *
		 * @return the list
		 */
		public List<SubattributesetContext> subattributeset() {
			return getRuleContexts(SubattributesetContext.class);
		}

		/**
		 * Subattributeset.
		 *
		 * @param i the i
		 * @return the subattributeset context
		 */
		public SubattributesetContext subattributeset(final int i) {
			return getRuleContext(SubattributesetContext.class, i);
		}

		/**
		 * Instantiates a {@link ConjunctionattributesetContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ConjunctionattributesetContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_conjunctionattributeset;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterConjunctionattributeset(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitConjunctionattributeset(this);
			}
		}
	}

	/**
	 * Conjunctionattributeset.
	 *
	 * @return the conjunctionattributeset context
	 * @throws RecognitionException the recognition exception
	 */
	public final ConjunctionattributesetContext conjunctionattributeset() throws RecognitionException {
		final ConjunctionattributesetContext _localctx = new ConjunctionattributesetContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_conjunctionattributeset);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
				setState(335);
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1: {
						{
							setState(330);
							ws();
							setState(331);
							conjunction();
							setState(332);
							ws();
							setState(333);
							subattributeset();
						}
					}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(337);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input, 20, _ctx);
				} while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class DisjunctionattributesetContext.
	 */
	public static class DisjunctionattributesetContext extends ParserRuleContext {

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Disjunction.
		 *
		 * @return the list
		 */
		public List<DisjunctionContext> disjunction() {
			return getRuleContexts(DisjunctionContext.class);
		}

		/**
		 * Disjunction.
		 *
		 * @param i the i
		 * @return the disjunction context
		 */
		public DisjunctionContext disjunction(final int i) {
			return getRuleContext(DisjunctionContext.class, i);
		}

		/**
		 * Subattributeset.
		 *
		 * @return the list
		 */
		public List<SubattributesetContext> subattributeset() {
			return getRuleContexts(SubattributesetContext.class);
		}

		/**
		 * Subattributeset.
		 *
		 * @param i the i
		 * @return the subattributeset context
		 */
		public SubattributesetContext subattributeset(final int i) {
			return getRuleContext(SubattributesetContext.class, i);
		}

		/**
		 * Instantiates a {@link DisjunctionattributesetContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public DisjunctionattributesetContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_disjunctionattributeset;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterDisjunctionattributeset(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitDisjunctionattributeset(this);
			}
		}
	}

	/**
	 * Disjunctionattributeset.
	 *
	 * @return the disjunctionattributeset context
	 * @throws RecognitionException the recognition exception
	 */
	public final DisjunctionattributesetContext disjunctionattributeset() throws RecognitionException {
		final DisjunctionattributesetContext _localctx = new DisjunctionattributesetContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_disjunctionattributeset);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
				setState(344);
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1: {
						{
							setState(339);
							ws();
							setState(340);
							disjunction();
							setState(341);
							ws();
							setState(342);
							subattributeset();
						}
					}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(346);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input, 21, _ctx);
				} while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class SubattributesetContext.
	 */
	public static class SubattributesetContext extends ParserRuleContext {

		/**
		 * Eclattribute.
		 *
		 * @return the eclattribute context
		 */
		public EclattributeContext eclattribute() {
			return getRuleContext(EclattributeContext.class, 0);
		}

		/**
		 * Left paren.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_PAREN() {
			return getToken(EclParser.LEFT_PAREN, 0);
		}

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Eclattributeset.
		 *
		 * @return the eclattributeset context
		 */
		public EclattributesetContext eclattributeset() {
			return getRuleContext(EclattributesetContext.class, 0);
		}

		/**
		 * Right paren.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_PAREN() {
			return getToken(EclParser.RIGHT_PAREN, 0);
		}

		/**
		 * Instantiates a {@link SubattributesetContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public SubattributesetContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_subattributeset;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterSubattributeset(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitSubattributeset(this);
			}
		}
	}

	/**
	 * Subattributeset.
	 *
	 * @return the subattributeset context
	 * @throws RecognitionException the recognition exception
	 */
	public final SubattributesetContext subattributeset() throws RecognitionException {
		final SubattributesetContext _localctx = new SubattributesetContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_subattributeset);
		try {
			setState(355);
			_errHandler.sync(this);
			switch (getInterpreter().adaptivePredict(_input, 22, _ctx)) {
			case 1:
				enterOuterAlt(_localctx, 1); {
				setState(348);
				eclattribute();
			}
				break;
			case 2:
				enterOuterAlt(_localctx, 2); {
				{
					setState(349);
					match(LEFT_PAREN);
					setState(350);
					ws();
					setState(351);
					eclattributeset();
					setState(352);
					ws();
					setState(353);
					match(RIGHT_PAREN);
				}
			}
				break;
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class EclattributegroupContext.
	 */
	public static class EclattributegroupContext extends ParserRuleContext {

		/**
		 * Left curly brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_CURLY_BRACE() {
			return getToken(EclParser.LEFT_CURLY_BRACE, 0);
		}

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Eclattributeset.
		 *
		 * @return the eclattributeset context
		 */
		public EclattributesetContext eclattributeset() {
			return getRuleContext(EclattributesetContext.class, 0);
		}

		/**
		 * Right curly brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_CURLY_BRACE() {
			return getToken(EclParser.RIGHT_CURLY_BRACE, 0);
		}

		/**
		 * Left brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_BRACE() {
			return getToken(EclParser.LEFT_BRACE, 0);
		}

		/**
		 * Cardinality.
		 *
		 * @return the cardinality context
		 */
		public CardinalityContext cardinality() {
			return getRuleContext(CardinalityContext.class, 0);
		}

		/**
		 * Right brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_BRACE() {
			return getToken(EclParser.RIGHT_BRACE, 0);
		}

		/**
		 * Instantiates a {@link EclattributegroupContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public EclattributegroupContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_eclattributegroup;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterEclattributegroup(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitEclattributegroup(this);
			}
		}
	}

	/**
	 * Eclattributegroup.
	 *
	 * @return the eclattributegroup context
	 * @throws RecognitionException the recognition exception
	 */
	public final EclattributegroupContext eclattributegroup() throws RecognitionException {
		final EclattributegroupContext _localctx = new EclattributegroupContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_eclattributegroup);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(362);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == LEFT_BRACE) {
					{
						setState(357);
						match(LEFT_BRACE);
						setState(358);
						cardinality();
						setState(359);
						match(RIGHT_BRACE);
						setState(360);
						ws();
					}
				}

				setState(364);
				match(LEFT_CURLY_BRACE);
				setState(365);
				ws();
				setState(366);
				eclattributeset();
				setState(367);
				ws();
				setState(368);
				match(RIGHT_CURLY_BRACE);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class EclattributeContext.
	 */
	public static class EclattributeContext extends ParserRuleContext {

		/**
		 * Eclattributename.
		 *
		 * @return the eclattributename context
		 */
		public EclattributenameContext eclattributename() {
			return getRuleContext(EclattributenameContext.class, 0);
		}

		/**
		 * Ws.
		 *
		 * @return the list
		 */
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}

		/**
		 * Ws.
		 *
		 * @param i the i
		 * @return the ws context
		 */
		public WsContext ws(final int i) {
			return getRuleContext(WsContext.class, i);
		}

		/**
		 * Left brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_BRACE() {
			return getToken(EclParser.LEFT_BRACE, 0);
		}

		/**
		 * Cardinality.
		 *
		 * @return the cardinality context
		 */
		public CardinalityContext cardinality() {
			return getRuleContext(CardinalityContext.class, 0);
		}

		/**
		 * Right brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_BRACE() {
			return getToken(EclParser.RIGHT_BRACE, 0);
		}

		/**
		 * Reverseflag.
		 *
		 * @return the reverseflag context
		 */
		public ReverseflagContext reverseflag() {
			return getRuleContext(ReverseflagContext.class, 0);
		}

		/**
		 * Expressioncomparisonoperator.
		 *
		 * @return the expressioncomparisonoperator context
		 */
		public ExpressioncomparisonoperatorContext expressioncomparisonoperator() {
			return getRuleContext(ExpressioncomparisonoperatorContext.class, 0);
		}

		/**
		 * Subexpressionconstraint.
		 *
		 * @return the subexpressionconstraint context
		 */
		public SubexpressionconstraintContext subexpressionconstraint() {
			return getRuleContext(SubexpressionconstraintContext.class, 0);
		}

		/**
		 * Numericcomparisonoperator.
		 *
		 * @return the numericcomparisonoperator context
		 */
		public NumericcomparisonoperatorContext numericcomparisonoperator() {
			return getRuleContext(NumericcomparisonoperatorContext.class, 0);
		}

		/**
		 * Pound.
		 *
		 * @return the terminal node
		 */
		public TerminalNode POUND() {
			return getToken(EclParser.POUND, 0);
		}

		/**
		 * Numericvalue.
		 *
		 * @return the numericvalue context
		 */
		public NumericvalueContext numericvalue() {
			return getRuleContext(NumericvalueContext.class, 0);
		}

		/**
		 * Stringcomparisonoperator.
		 *
		 * @return the stringcomparisonoperator context
		 */
		public StringcomparisonoperatorContext stringcomparisonoperator() {
			return getRuleContext(StringcomparisonoperatorContext.class, 0);
		}

		/**
		 * Qm.
		 *
		 * @return the list
		 */
		public List<QmContext> qm() {
			return getRuleContexts(QmContext.class);
		}

		/**
		 * Qm.
		 *
		 * @param i the i
		 * @return the qm context
		 */
		public QmContext qm(final int i) {
			return getRuleContext(QmContext.class, i);
		}

		/**
		 * Stringvalue.
		 *
		 * @return the stringvalue context
		 */
		public StringvalueContext stringvalue() {
			return getRuleContext(StringvalueContext.class, 0);
		}

		/**
		 * Instantiates a {@link EclattributeContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public EclattributeContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_eclattribute;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterEclattribute(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitEclattribute(this);
			}
		}
	}

	/**
	 * Eclattribute.
	 *
	 * @return the eclattribute context
	 * @throws RecognitionException the recognition exception
	 */
	public final EclattributeContext eclattribute() throws RecognitionException {
		final EclattributeContext _localctx = new EclattributeContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_eclattribute);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(375);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == LEFT_BRACE) {
					{
						setState(370);
						match(LEFT_BRACE);
						setState(371);
						cardinality();
						setState(372);
						match(RIGHT_BRACE);
						setState(373);
						ws();
					}
				}

				setState(380);
				_errHandler.sync(this);
				switch (getInterpreter().adaptivePredict(_input, 25, _ctx)) {
				case 1: {
					setState(377);
					reverseflag();
					setState(378);
					ws();
				}
					break;
				}
				setState(382);
				eclattributename();
				setState(383);
				ws();
				setState(399);
				_errHandler.sync(this);
				switch (getInterpreter().adaptivePredict(_input, 26, _ctx)) {
				case 1: {
					{
						setState(384);
						expressioncomparisonoperator();
						setState(385);
						ws();
						setState(386);
						subexpressionconstraint();
					}
				}
					break;
				case 2: {
					{
						setState(388);
						numericcomparisonoperator();
						setState(389);
						ws();
						setState(390);
						match(POUND);
						setState(391);
						numericvalue();
					}
				}
					break;
				case 3: {
					{
						setState(393);
						stringcomparisonoperator();
						setState(394);
						ws();
						setState(395);
						qm();
						setState(396);
						stringvalue();
						setState(397);
						qm();
					}
				}
					break;
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class CardinalityContext.
	 */
	public static class CardinalityContext extends ParserRuleContext {

		/**
		 * Minvalue.
		 *
		 * @return the minvalue context
		 */
		public MinvalueContext minvalue() {
			return getRuleContext(MinvalueContext.class, 0);
		}

		/**
		 * To.
		 *
		 * @return the to context
		 */
		public ToContext to() {
			return getRuleContext(ToContext.class, 0);
		}

		/**
		 * Maxvalue.
		 *
		 * @return the maxvalue context
		 */
		public MaxvalueContext maxvalue() {
			return getRuleContext(MaxvalueContext.class, 0);
		}

		/**
		 * Instantiates a {@link CardinalityContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public CardinalityContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_cardinality;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterCardinality(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitCardinality(this);
			}
		}
	}

	/**
	 * Cardinality.
	 *
	 * @return the cardinality context
	 * @throws RecognitionException the recognition exception
	 */
	public final CardinalityContext cardinality() throws RecognitionException {
		final CardinalityContext _localctx = new CardinalityContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_cardinality);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(401);
				minvalue();
				setState(402);
				to();
				setState(403);
				maxvalue();
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class MinvalueContext.
	 */
	public static class MinvalueContext extends ParserRuleContext {

		/**
		 * Nonnegativeintegervalue.
		 *
		 * @return the nonnegativeintegervalue context
		 */
		public NonnegativeintegervalueContext nonnegativeintegervalue() {
			return getRuleContext(NonnegativeintegervalueContext.class, 0);
		}

		/**
		 * Instantiates a {@link MinvalueContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public MinvalueContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_minvalue;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterMinvalue(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitMinvalue(this);
			}
		}
	}

	/**
	 * Minvalue.
	 *
	 * @return the minvalue context
	 * @throws RecognitionException the recognition exception
	 */
	public final MinvalueContext minvalue() throws RecognitionException {
		final MinvalueContext _localctx = new MinvalueContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_minvalue);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(405);
				nonnegativeintegervalue();
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ToContext.
	 */
	public static class ToContext extends ParserRuleContext {

		/**
		 * Period.
		 *
		 * @return the list
		 */
		public List<TerminalNode> PERIOD() {
			return getTokens(EclParser.PERIOD);
		}

		/**
		 * Period.
		 *
		 * @param i the i
		 * @return the terminal node
		 */
		public TerminalNode PERIOD(final int i) {
			return getToken(EclParser.PERIOD, i);
		}

		/**
		 * Instantiates a {@link ToContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ToContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_to;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterTo(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitTo(this);
			}
		}
	}

	/**
	 * To.
	 *
	 * @return the to context
	 * @throws RecognitionException the recognition exception
	 */
	public final ToContext to() throws RecognitionException {
		final ToContext _localctx = new ToContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_to);
		try {
			enterOuterAlt(_localctx, 1);
			{
				{
					setState(407);
					match(PERIOD);
					setState(408);
					match(PERIOD);
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class MaxvalueContext.
	 */
	public static class MaxvalueContext extends ParserRuleContext {

		/**
		 * Nonnegativeintegervalue.
		 *
		 * @return the nonnegativeintegervalue context
		 */
		public NonnegativeintegervalueContext nonnegativeintegervalue() {
			return getRuleContext(NonnegativeintegervalueContext.class, 0);
		}

		/**
		 * Many.
		 *
		 * @return the many context
		 */
		public ManyContext many() {
			return getRuleContext(ManyContext.class, 0);
		}

		/**
		 * Instantiates a {@link MaxvalueContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public MaxvalueContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_maxvalue;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterMaxvalue(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitMaxvalue(this);
			}
		}
	}

	/**
	 * Maxvalue.
	 *
	 * @return the maxvalue context
	 * @throws RecognitionException the recognition exception
	 */
	public final MaxvalueContext maxvalue() throws RecognitionException {
		final MaxvalueContext _localctx = new MaxvalueContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_maxvalue);
		try {
			setState(412);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ZERO:
			case ONE:
			case TWO:
			case THREE:
			case FOUR:
			case FIVE:
			case SIX:
			case SEVEN:
			case EIGHT:
			case NINE:
				enterOuterAlt(_localctx, 1); {
				setState(410);
				nonnegativeintegervalue();
			}
				break;
			case ASTERISK:
				enterOuterAlt(_localctx, 2); {
				setState(411);
				many();
			}
				break;
			default:
				throw new NoViableAltException(this);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ManyContext.
	 */
	public static class ManyContext extends ParserRuleContext {

		/**
		 * Asterisk.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ASTERISK() {
			return getToken(EclParser.ASTERISK, 0);
		}

		/**
		 * Instantiates a {@link ManyContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ManyContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_many;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterMany(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitMany(this);
			}
		}
	}

	/**
	 * Many.
	 *
	 * @return the many context
	 * @throws RecognitionException the recognition exception
	 */
	public final ManyContext many() throws RecognitionException {
		final ManyContext _localctx = new ManyContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_many);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(414);
				match(ASTERISK);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ReverseflagContext.
	 */
	public static class ReverseflagContext extends ParserRuleContext {

		/**
		 * Cap r.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_R() {
			return getToken(EclParser.CAP_R, 0);
		}

		/**
		 * Instantiates a {@link ReverseflagContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ReverseflagContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_reverseflag;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterReverseflag(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitReverseflag(this);
			}
		}
	}

	/**
	 * Reverseflag.
	 *
	 * @return the reverseflag context
	 * @throws RecognitionException the recognition exception
	 */
	public final ReverseflagContext reverseflag() throws RecognitionException {
		final ReverseflagContext _localctx = new ReverseflagContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_reverseflag);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(416);
				match(CAP_R);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class EclattributenameContext.
	 */
	public static class EclattributenameContext extends ParserRuleContext {

		/**
		 * Subexpressionconstraint.
		 *
		 * @return the subexpressionconstraint context
		 */
		public SubexpressionconstraintContext subexpressionconstraint() {
			return getRuleContext(SubexpressionconstraintContext.class, 0);
		}

		/**
		 * Instantiates a {@link EclattributenameContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public EclattributenameContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_eclattributename;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterEclattributename(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitEclattributename(this);
			}
		}
	}

	/**
	 * Eclattributename.
	 *
	 * @return the eclattributename context
	 * @throws RecognitionException the recognition exception
	 */
	public final EclattributenameContext eclattributename() throws RecognitionException {
		final EclattributenameContext _localctx = new EclattributenameContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_eclattributename);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(418);
				subexpressionconstraint();
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ExpressioncomparisonoperatorContext.
	 */
	public static class ExpressioncomparisonoperatorContext extends ParserRuleContext {

		/**
		 * Equals.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EQUALS() {
			return getToken(EclParser.EQUALS, 0);
		}

		/**
		 * Exclamation.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EXCLAMATION() {
			return getToken(EclParser.EXCLAMATION, 0);
		}

		/**
		 * Instantiates a {@link ExpressioncomparisonoperatorContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ExpressioncomparisonoperatorContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_expressioncomparisonoperator;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterExpressioncomparisonoperator(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitExpressioncomparisonoperator(this);
			}
		}
	}

	/**
	 * Expressioncomparisonoperator.
	 *
	 * @return the expressioncomparisonoperator context
	 * @throws RecognitionException the recognition exception
	 */
	public final ExpressioncomparisonoperatorContext expressioncomparisonoperator() throws RecognitionException {
		final ExpressioncomparisonoperatorContext _localctx = new ExpressioncomparisonoperatorContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_expressioncomparisonoperator);
		try {
			setState(423);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EQUALS:
				enterOuterAlt(_localctx, 1); {
				setState(420);
				match(EQUALS);
			}
				break;
			case EXCLAMATION:
				enterOuterAlt(_localctx, 2); {
				{
					setState(421);
					match(EXCLAMATION);
					setState(422);
					match(EQUALS);
				}
			}
				break;
			default:
				throw new NoViableAltException(this);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class NumericcomparisonoperatorContext.
	 */
	public static class NumericcomparisonoperatorContext extends ParserRuleContext {

		/**
		 * Equals.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EQUALS() {
			return getToken(EclParser.EQUALS, 0);
		}

		/**
		 * Exclamation.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EXCLAMATION() {
			return getToken(EclParser.EXCLAMATION, 0);
		}

		/**
		 * Less than.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LESS_THAN() {
			return getToken(EclParser.LESS_THAN, 0);
		}

		/**
		 * Greater than.
		 *
		 * @return the terminal node
		 */
		public TerminalNode GREATER_THAN() {
			return getToken(EclParser.GREATER_THAN, 0);
		}

		/**
		 * Instantiates a {@link NumericcomparisonoperatorContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public NumericcomparisonoperatorContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_numericcomparisonoperator;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterNumericcomparisonoperator(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitNumericcomparisonoperator(this);
			}
		}
	}

	/**
	 * Numericcomparisonoperator.
	 *
	 * @return the numericcomparisonoperator context
	 * @throws RecognitionException the recognition exception
	 */
	public final NumericcomparisonoperatorContext numericcomparisonoperator() throws RecognitionException {
		final NumericcomparisonoperatorContext _localctx = new NumericcomparisonoperatorContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_numericcomparisonoperator);
		try {
			setState(434);
			_errHandler.sync(this);
			switch (getInterpreter().adaptivePredict(_input, 29, _ctx)) {
			case 1:
				enterOuterAlt(_localctx, 1); {
				setState(425);
				match(EQUALS);
			}
				break;
			case 2:
				enterOuterAlt(_localctx, 2); {
				{
					setState(426);
					match(EXCLAMATION);
					setState(427);
					match(EQUALS);
				}
			}
				break;
			case 3:
				enterOuterAlt(_localctx, 3); {
				{
					setState(428);
					match(LESS_THAN);
					setState(429);
					match(EQUALS);
				}
			}
				break;
			case 4:
				enterOuterAlt(_localctx, 4); {
				setState(430);
				match(LESS_THAN);
			}
				break;
			case 5:
				enterOuterAlt(_localctx, 5); {
				{
					setState(431);
					match(GREATER_THAN);
					setState(432);
					match(EQUALS);
				}
			}
				break;
			case 6:
				enterOuterAlt(_localctx, 6); {
				setState(433);
				match(GREATER_THAN);
			}
				break;
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class StringcomparisonoperatorContext.
	 */
	public static class StringcomparisonoperatorContext extends ParserRuleContext {

		/**
		 * Equals.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EQUALS() {
			return getToken(EclParser.EQUALS, 0);
		}

		/**
		 * Exclamation.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EXCLAMATION() {
			return getToken(EclParser.EXCLAMATION, 0);
		}

		/**
		 * Instantiates a {@link StringcomparisonoperatorContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public StringcomparisonoperatorContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_stringcomparisonoperator;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterStringcomparisonoperator(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitStringcomparisonoperator(this);
			}
		}
	}

	/**
	 * Stringcomparisonoperator.
	 *
	 * @return the stringcomparisonoperator context
	 * @throws RecognitionException the recognition exception
	 */
	public final StringcomparisonoperatorContext stringcomparisonoperator() throws RecognitionException {
		final StringcomparisonoperatorContext _localctx = new StringcomparisonoperatorContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_stringcomparisonoperator);
		try {
			setState(439);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EQUALS:
				enterOuterAlt(_localctx, 1); {
				setState(436);
				match(EQUALS);
			}
				break;
			case EXCLAMATION:
				enterOuterAlt(_localctx, 2); {
				{
					setState(437);
					match(EXCLAMATION);
					setState(438);
					match(EQUALS);
				}
			}
				break;
			default:
				throw new NoViableAltException(this);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class NumericvalueContext.
	 */
	public static class NumericvalueContext extends ParserRuleContext {

		/**
		 * Decimalvalue.
		 *
		 * @return the decimalvalue context
		 */
		public DecimalvalueContext decimalvalue() {
			return getRuleContext(DecimalvalueContext.class, 0);
		}

		/**
		 * Integervalue.
		 *
		 * @return the integervalue context
		 */
		public IntegervalueContext integervalue() {
			return getRuleContext(IntegervalueContext.class, 0);
		}

		/**
		 * Dash.
		 *
		 * @return the terminal node
		 */
		public TerminalNode DASH() {
			return getToken(EclParser.DASH, 0);
		}

		/**
		 * Plus.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PLUS() {
			return getToken(EclParser.PLUS, 0);
		}

		/**
		 * Instantiates a {@link NumericvalueContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public NumericvalueContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_numericvalue;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterNumericvalue(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitNumericvalue(this);
			}
		}
	}

	/**
	 * Numericvalue.
	 *
	 * @return the numericvalue context
	 * @throws RecognitionException the recognition exception
	 */
	public final NumericvalueContext numericvalue() throws RecognitionException {
		final NumericvalueContext _localctx = new NumericvalueContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_numericvalue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(442);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == PLUS || _la == DASH) {
					{
						setState(441);
						_la = _input.LA(1);
						if (!(_la == PLUS || _la == DASH)) {
							_errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}
							_errHandler.reportMatch(this);
							consume();
						}
					}
				}

				setState(446);
				_errHandler.sync(this);
				switch (getInterpreter().adaptivePredict(_input, 32, _ctx)) {
				case 1: {
					setState(444);
					decimalvalue();
				}
					break;
				case 2: {
					setState(445);
					integervalue();
				}
					break;
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class StringvalueContext.
	 */
	public static class StringvalueContext extends ParserRuleContext {

		/**
		 * Anynonescapedchar.
		 *
		 * @return the list
		 */
		public List<AnynonescapedcharContext> anynonescapedchar() {
			return getRuleContexts(AnynonescapedcharContext.class);
		}

		/**
		 * Anynonescapedchar.
		 *
		 * @param i the i
		 * @return the anynonescapedchar context
		 */
		public AnynonescapedcharContext anynonescapedchar(final int i) {
			return getRuleContext(AnynonescapedcharContext.class, i);
		}

		/**
		 * Escapedchar.
		 *
		 * @return the list
		 */
		public List<EscapedcharContext> escapedchar() {
			return getRuleContexts(EscapedcharContext.class);
		}

		/**
		 * Escapedchar.
		 *
		 * @param i the i
		 * @return the escapedchar context
		 */
		public EscapedcharContext escapedchar(final int i) {
			return getRuleContext(EscapedcharContext.class, i);
		}

		/**
		 * Instantiates a {@link StringvalueContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public StringvalueContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_stringvalue;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterStringvalue(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitStringvalue(this);
			}
		}
	}

	/**
	 * Stringvalue.
	 *
	 * @return the stringvalue context
	 * @throws RecognitionException the recognition exception
	 */
	public final StringvalueContext stringvalue() throws RecognitionException {
		final StringvalueContext _localctx = new StringvalueContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_stringvalue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(450);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
						setState(450);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case TAB:
						case LF:
						case CR:
						case SPACE:
						case EXCLAMATION:
						case POUND:
						case DOLLAR:
						case PERCENT:
						case AMPERSAND:
						case APOSTROPHE:
						case LEFT_PAREN:
						case RIGHT_PAREN:
						case ASTERISK:
						case PLUS:
						case COMMA:
						case DASH:
						case PERIOD:
						case SLASH:
						case ZERO:
						case ONE:
						case TWO:
						case THREE:
						case FOUR:
						case FIVE:
						case SIX:
						case SEVEN:
						case EIGHT:
						case NINE:
						case COLON:
						case SEMICOLON:
						case LESS_THAN:
						case EQUALS:
						case GREATER_THAN:
						case QUESTION:
						case AT:
						case CAP_A:
						case CAP_B:
						case CAP_C:
						case CAP_D:
						case CAP_E:
						case CAP_F:
						case CAP_G:
						case CAP_H:
						case CAP_I:
						case CAP_J:
						case CAP_K:
						case CAP_L:
						case CAP_M:
						case CAP_N:
						case CAP_O:
						case CAP_P:
						case CAP_Q:
						case CAP_R:
						case CAP_S:
						case CAP_T:
						case CAP_U:
						case CAP_V:
						case CAP_W:
						case CAP_X:
						case CAP_Y:
						case CAP_Z:
						case LEFT_BRACE:
						case RIGHT_BRACE:
						case CARAT:
						case UNDERSCORE:
						case ACCENT:
						case A:
						case B:
						case C:
						case D:
						case E:
						case F:
						case G:
						case H:
						case I:
						case J:
						case K:
						case L:
						case M:
						case N:
						case O:
						case P:
						case Q:
						case R:
						case S:
						case T:
						case U:
						case V:
						case W:
						case X:
						case Y:
						case Z:
						case LEFT_CURLY_BRACE:
						case PIPE:
						case RIGHT_CURLY_BRACE:
						case TILDE:
						case U_00C2:
						case U_00C3:
						case U_00C4:
						case U_00C5:
						case U_00C6:
						case U_00C7:
						case U_00C8:
						case U_00C9:
						case U_00CA:
						case U_00CB:
						case U_00CC:
						case U_00CD:
						case U_00CE:
						case U_00CF:
						case U_00D0:
						case U_00D1:
						case U_00D2:
						case U_00D3:
						case U_00D4:
						case U_00D5:
						case U_00D6:
						case U_00D7:
						case U_00D8:
						case U_00D9:
						case U_00DA:
						case U_00DB:
						case U_00DC:
						case U_00DD:
						case U_00DE:
						case U_00DF:
						case U_00E0:
						case U_00E1:
						case U_00E2:
						case U_00E3:
						case U_00E4:
						case U_00E5:
						case U_00E6:
						case U_00E7:
						case U_00E8:
						case U_00E9:
						case U_00EA:
						case U_00EB:
						case U_00EC:
						case U_00ED:
						case U_00EE:
						case U_00EF:
						case U_00F0:
						case U_00F1:
						case U_00F2:
						case U_00F3:
						case U_00F4: {
							setState(448);
							anynonescapedchar();
						}
							break;
						case BACKSLASH: {
							setState(449);
							escapedchar();
						}
							break;
						default:
							throw new NoViableAltException(this);
						}
					}
					setState(452);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TAB) | (1L << LF) | (1L << CR) | (1L << SPACE)
						| (1L << EXCLAMATION) | (1L << POUND) | (1L << DOLLAR) | (1L << PERCENT) | (1L << AMPERSAND)
						| (1L << APOSTROPHE) | (1L << LEFT_PAREN) | (1L << RIGHT_PAREN) | (1L << ASTERISK)
						| (1L << PLUS) | (1L << COMMA) | (1L << DASH) | (1L << PERIOD) | (1L << SLASH) | (1L << ZERO)
						| (1L << ONE) | (1L << TWO) | (1L << THREE) | (1L << FOUR) | (1L << FIVE) | (1L << SIX)
						| (1L << SEVEN) | (1L << EIGHT) | (1L << NINE) | (1L << COLON) | (1L << SEMICOLON)
						| (1L << LESS_THAN) | (1L << EQUALS) | (1L << GREATER_THAN) | (1L << QUESTION) | (1L << AT)
						| (1L << CAP_A) | (1L << CAP_B) | (1L << CAP_C) | (1L << CAP_D) | (1L << CAP_E) | (1L << CAP_F)
						| (1L << CAP_G) | (1L << CAP_H) | (1L << CAP_I) | (1L << CAP_J) | (1L << CAP_K) | (1L << CAP_L)
						| (1L << CAP_M) | (1L << CAP_N) | (1L << CAP_O) | (1L << CAP_P) | (1L << CAP_Q) | (1L << CAP_R)
						| (1L << CAP_S) | (1L << CAP_T) | (1L << CAP_U) | (1L << CAP_V) | (1L << CAP_W) | (1L << CAP_X)
						| (1L << CAP_Y) | (1L << CAP_Z) | (1L << LEFT_BRACE))) != 0)
						|| ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64))
								& ((1L << (BACKSLASH - 64)) | (1L << (RIGHT_BRACE - 64)) | (1L << (CARAT - 64))
										| (1L << (UNDERSCORE - 64)) | (1L << (ACCENT - 64)) | (1L << (A - 64))
										| (1L << (B - 64)) | (1L << (C - 64)) | (1L << (D - 64)) | (1L << (E - 64))
										| (1L << (F - 64)) | (1L << (G - 64)) | (1L << (H - 64)) | (1L << (I - 64))
										| (1L << (J - 64)) | (1L << (K - 64)) | (1L << (L - 64)) | (1L << (M - 64))
										| (1L << (N - 64)) | (1L << (O - 64)) | (1L << (P - 64)) | (1L << (Q - 64))
										| (1L << (R - 64)) | (1L << (S - 64)) | (1L << (T - 64)) | (1L << (U - 64))
										| (1L << (V - 64)) | (1L << (W - 64)) | (1L << (X - 64)) | (1L << (Y - 64))
										| (1L << (Z - 64)) | (1L << (LEFT_CURLY_BRACE - 64)) | (1L << (PIPE - 64))
										| (1L << (RIGHT_CURLY_BRACE - 64)) | (1L << (TILDE - 64)))) != 0)
						|| ((((_la - 163)) & ~0x3f) == 0 && ((1L << (_la - 163)) & ((1L << (U_00C2 - 163))
								| (1L << (U_00C3 - 163)) | (1L << (U_00C4 - 163)) | (1L << (U_00C5 - 163))
								| (1L << (U_00C6 - 163)) | (1L << (U_00C7 - 163)) | (1L << (U_00C8 - 163))
								| (1L << (U_00C9 - 163)) | (1L << (U_00CA - 163)) | (1L << (U_00CB - 163))
								| (1L << (U_00CC - 163)) | (1L << (U_00CD - 163)) | (1L << (U_00CE - 163))
								| (1L << (U_00CF - 163)) | (1L << (U_00D0 - 163)) | (1L << (U_00D1 - 163))
								| (1L << (U_00D2 - 163)) | (1L << (U_00D3 - 163)) | (1L << (U_00D4 - 163))
								| (1L << (U_00D5 - 163)) | (1L << (U_00D6 - 163)) | (1L << (U_00D7 - 163))
								| (1L << (U_00D8 - 163)) | (1L << (U_00D9 - 163)) | (1L << (U_00DA - 163))
								| (1L << (U_00DB - 163)) | (1L << (U_00DC - 163)) | (1L << (U_00DD - 163))
								| (1L << (U_00DE - 163)) | (1L << (U_00DF - 163)) | (1L << (U_00E0 - 163))
								| (1L << (U_00E1 - 163)) | (1L << (U_00E2 - 163)) | (1L << (U_00E3 - 163))
								| (1L << (U_00E4 - 163)) | (1L << (U_00E5 - 163)) | (1L << (U_00E6 - 163))
								| (1L << (U_00E7 - 163)) | (1L << (U_00E8 - 163)) | (1L << (U_00E9 - 163))
								| (1L << (U_00EA - 163)) | (1L << (U_00EB - 163)) | (1L << (U_00EC - 163))
								| (1L << (U_00ED - 163)) | (1L << (U_00EE - 163)) | (1L << (U_00EF - 163))
								| (1L << (U_00F0 - 163)) | (1L << (U_00F1 - 163)) | (1L << (U_00F2 - 163))
								| (1L << (U_00F3 - 163)) | (1L << (U_00F4 - 163)))) != 0));
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class IntegervalueContext.
	 */
	public static class IntegervalueContext extends ParserRuleContext {

		/**
		 * Digitnonzero.
		 *
		 * @return the digitnonzero context
		 */
		public DigitnonzeroContext digitnonzero() {
			return getRuleContext(DigitnonzeroContext.class, 0);
		}

		/**
		 * Digit.
		 *
		 * @return the list
		 */
		public List<DigitContext> digit() {
			return getRuleContexts(DigitContext.class);
		}

		/**
		 * Digit.
		 *
		 * @param i the i
		 * @return the digit context
		 */
		public DigitContext digit(final int i) {
			return getRuleContext(DigitContext.class, i);
		}

		/**
		 * Zero.
		 *
		 * @return the zero context
		 */
		public ZeroContext zero() {
			return getRuleContext(ZeroContext.class, 0);
		}

		/**
		 * Instantiates a {@link IntegervalueContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public IntegervalueContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_integervalue;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterIntegervalue(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitIntegervalue(this);
			}
		}
	}

	/**
	 * Integervalue.
	 *
	 * @return the integervalue context
	 * @throws RecognitionException the recognition exception
	 */
	public final IntegervalueContext integervalue() throws RecognitionException {
		final IntegervalueContext _localctx = new IntegervalueContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_integervalue);
		int _la;
		try {
			setState(462);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ONE:
			case TWO:
			case THREE:
			case FOUR:
			case FIVE:
			case SIX:
			case SEVEN:
			case EIGHT:
			case NINE:
				enterOuterAlt(_localctx, 1); {
				{
					setState(454);
					digitnonzero();
					setState(458);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while ((((_la) & ~0x3f) == 0 && ((1L << _la)
							& ((1L << ZERO) | (1L << ONE) | (1L << TWO) | (1L << THREE) | (1L << FOUR) | (1L << FIVE)
									| (1L << SIX) | (1L << SEVEN) | (1L << EIGHT) | (1L << NINE))) != 0)) {
						{
							{
								setState(455);
								digit();
							}
						}
						setState(460);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
				}
			}
				break;
			case ZERO:
				enterOuterAlt(_localctx, 2); {
				setState(461);
				zero();
			}
				break;
			default:
				throw new NoViableAltException(this);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class DecimalvalueContext.
	 */
	public static class DecimalvalueContext extends ParserRuleContext {

		/**
		 * Integervalue.
		 *
		 * @return the integervalue context
		 */
		public IntegervalueContext integervalue() {
			return getRuleContext(IntegervalueContext.class, 0);
		}

		/**
		 * Period.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PERIOD() {
			return getToken(EclParser.PERIOD, 0);
		}

		/**
		 * Digit.
		 *
		 * @return the list
		 */
		public List<DigitContext> digit() {
			return getRuleContexts(DigitContext.class);
		}

		/**
		 * Digit.
		 *
		 * @param i the i
		 * @return the digit context
		 */
		public DigitContext digit(final int i) {
			return getRuleContext(DigitContext.class, i);
		}

		/**
		 * Instantiates a {@link DecimalvalueContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public DecimalvalueContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_decimalvalue;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterDecimalvalue(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitDecimalvalue(this);
			}
		}
	}

	/**
	 * Decimalvalue.
	 *
	 * @return the decimalvalue context
	 * @throws RecognitionException the recognition exception
	 */
	public final DecimalvalueContext decimalvalue() throws RecognitionException {
		final DecimalvalueContext _localctx = new DecimalvalueContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_decimalvalue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(464);
				integervalue();
				setState(465);
				match(PERIOD);
				setState(467);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
						{
							setState(466);
							digit();
						}
					}
					setState(469);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ((((_la) & ~0x3f) == 0
						&& ((1L << _la) & ((1L << ZERO) | (1L << ONE) | (1L << TWO) | (1L << THREE) | (1L << FOUR)
								| (1L << FIVE) | (1L << SIX) | (1L << SEVEN) | (1L << EIGHT) | (1L << NINE))) != 0));
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class NonnegativeintegervalueContext.
	 */
	public static class NonnegativeintegervalueContext extends ParserRuleContext {

		/**
		 * Digitnonzero.
		 *
		 * @return the digitnonzero context
		 */
		public DigitnonzeroContext digitnonzero() {
			return getRuleContext(DigitnonzeroContext.class, 0);
		}

		/**
		 * Digit.
		 *
		 * @return the list
		 */
		public List<DigitContext> digit() {
			return getRuleContexts(DigitContext.class);
		}

		/**
		 * Digit.
		 *
		 * @param i the i
		 * @return the digit context
		 */
		public DigitContext digit(final int i) {
			return getRuleContext(DigitContext.class, i);
		}

		/**
		 * Zero.
		 *
		 * @return the zero context
		 */
		public ZeroContext zero() {
			return getRuleContext(ZeroContext.class, 0);
		}

		/**
		 * Instantiates a {@link NonnegativeintegervalueContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public NonnegativeintegervalueContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_nonnegativeintegervalue;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterNonnegativeintegervalue(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitNonnegativeintegervalue(this);
			}
		}
	}

	/**
	 * Nonnegativeintegervalue.
	 *
	 * @return the nonnegativeintegervalue context
	 * @throws RecognitionException the recognition exception
	 */
	public final NonnegativeintegervalueContext nonnegativeintegervalue() throws RecognitionException {
		final NonnegativeintegervalueContext _localctx = new NonnegativeintegervalueContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_nonnegativeintegervalue);
		int _la;
		try {
			setState(479);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ONE:
			case TWO:
			case THREE:
			case FOUR:
			case FIVE:
			case SIX:
			case SEVEN:
			case EIGHT:
			case NINE:
				enterOuterAlt(_localctx, 1); {
				{
					setState(471);
					digitnonzero();
					setState(475);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while ((((_la) & ~0x3f) == 0 && ((1L << _la)
							& ((1L << ZERO) | (1L << ONE) | (1L << TWO) | (1L << THREE) | (1L << FOUR) | (1L << FIVE)
									| (1L << SIX) | (1L << SEVEN) | (1L << EIGHT) | (1L << NINE))) != 0)) {
						{
							{
								setState(472);
								digit();
							}
						}
						setState(477);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
				}
			}
				break;
			case ZERO:
				enterOuterAlt(_localctx, 2); {
				setState(478);
				zero();
			}
				break;
			default:
				throw new NoViableAltException(this);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class SctidContext.
	 */
	public static class SctidContext extends ParserRuleContext {

		/**
		 * Digitnonzero.
		 *
		 * @return the digitnonzero context
		 */
		public DigitnonzeroContext digitnonzero() {
			return getRuleContext(DigitnonzeroContext.class, 0);
		}

		/**
		 * Digit.
		 *
		 * @return the list
		 */
		public List<DigitContext> digit() {
			return getRuleContexts(DigitContext.class);
		}

		/**
		 * Digit.
		 *
		 * @param i the i
		 * @return the digit context
		 */
		public DigitContext digit(final int i) {
			return getRuleContext(DigitContext.class, i);
		}

		/**
		 * Codechar.
		 *
		 * @return the list
		 */
		public List<CodecharContext> codechar() {
			return getRuleContexts(CodecharContext.class);
		}

		/**
		 * Codechar.
		 *
		 * @param i the i
		 * @return the codechar context
		 */
		public CodecharContext codechar(final int i) {
			return getRuleContext(CodecharContext.class, i);
		}

		/**
		 * Instantiates a {@link SctidContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public SctidContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_sctid;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterSctid(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitSctid(this);
			}
		}
	}

	/**
	 * Sctid.
	 *
	 * @return the sctid context
	 * @throws RecognitionException the recognition exception
	 */
	public final SctidContext sctid() throws RecognitionException {
		final SctidContext _localctx = new SctidContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_sctid);
		int _la;
		try {
			int _alt;
			setState(585);
			_errHandler.sync(this);
			switch (getInterpreter().adaptivePredict(_input, 43, _ctx)) {
			case 1:
				enterOuterAlt(_localctx, 1); {
				setState(481);
				digitnonzero();
				{
					setState(482);
					digit();
				}
				{
					setState(483);
					digit();
				}
				{
					setState(484);
					digit();
				}
				{
					setState(485);
					digit();
				}
				{
					setState(486);
					digit();
				}
				setState(578);
				_errHandler.sync(this);
				switch (getInterpreter().adaptivePredict(_input, 41, _ctx)) {
				case 1: {
					setState(488);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if ((((_la) & ~0x3f) == 0 && ((1L << _la)
							& ((1L << ZERO) | (1L << ONE) | (1L << TWO) | (1L << THREE) | (1L << FOUR) | (1L << FIVE)
									| (1L << SIX) | (1L << SEVEN) | (1L << EIGHT) | (1L << NINE))) != 0)) {
						{
							setState(487);
							digit();
						}
					}

				}
					break;
				case 2: {
					{
						{
							setState(490);
							digit();
						}
						{
							setState(491);
							digit();
						}
					}
				}
					break;
				case 3: {
					{
						{
							setState(493);
							digit();
						}
						{
							setState(494);
							digit();
						}
						{
							setState(495);
							digit();
						}
					}
				}
					break;
				case 4: {
					{
						{
							setState(497);
							digit();
						}
						{
							setState(498);
							digit();
						}
						{
							setState(499);
							digit();
						}
						{
							setState(500);
							digit();
						}
					}
				}
					break;
				case 5: {
					{
						{
							setState(502);
							digit();
						}
						{
							setState(503);
							digit();
						}
						{
							setState(504);
							digit();
						}
						{
							setState(505);
							digit();
						}
						{
							setState(506);
							digit();
						}
					}
				}
					break;
				case 6: {
					{
						{
							setState(508);
							digit();
						}
						{
							setState(509);
							digit();
						}
						{
							setState(510);
							digit();
						}
						{
							setState(511);
							digit();
						}
						{
							setState(512);
							digit();
						}
						{
							setState(513);
							digit();
						}
					}
				}
					break;
				case 7: {
					{
						{
							setState(515);
							digit();
						}
						{
							setState(516);
							digit();
						}
						{
							setState(517);
							digit();
						}
						{
							setState(518);
							digit();
						}
						{
							setState(519);
							digit();
						}
						{
							setState(520);
							digit();
						}
						{
							setState(521);
							digit();
						}
					}
				}
					break;
				case 8: {
					{
						{
							setState(523);
							digit();
						}
						{
							setState(524);
							digit();
						}
						{
							setState(525);
							digit();
						}
						{
							setState(526);
							digit();
						}
						{
							setState(527);
							digit();
						}
						{
							setState(528);
							digit();
						}
						{
							setState(529);
							digit();
						}
						{
							setState(530);
							digit();
						}
					}
				}
					break;
				case 9: {
					{
						{
							setState(532);
							digit();
						}
						{
							setState(533);
							digit();
						}
						{
							setState(534);
							digit();
						}
						{
							setState(535);
							digit();
						}
						{
							setState(536);
							digit();
						}
						{
							setState(537);
							digit();
						}
						{
							setState(538);
							digit();
						}
						{
							setState(539);
							digit();
						}
						{
							setState(540);
							digit();
						}
					}
				}
					break;
				case 10: {
					{
						{
							setState(542);
							digit();
						}
						{
							setState(543);
							digit();
						}
						{
							setState(544);
							digit();
						}
						{
							setState(545);
							digit();
						}
						{
							setState(546);
							digit();
						}
						{
							setState(547);
							digit();
						}
						{
							setState(548);
							digit();
						}
						{
							setState(549);
							digit();
						}
						{
							setState(550);
							digit();
						}
						{
							setState(551);
							digit();
						}
					}
				}
					break;
				case 11: {
					{
						{
							setState(553);
							digit();
						}
						{
							setState(554);
							digit();
						}
						{
							setState(555);
							digit();
						}
						{
							setState(556);
							digit();
						}
						{
							setState(557);
							digit();
						}
						{
							setState(558);
							digit();
						}
						{
							setState(559);
							digit();
						}
						{
							setState(560);
							digit();
						}
						{
							setState(561);
							digit();
						}
						{
							setState(562);
							digit();
						}
						{
							setState(563);
							digit();
						}
					}
				}
					break;
				case 12: {
					{
						{
							setState(565);
							digit();
						}
						{
							setState(566);
							digit();
						}
						{
							setState(567);
							digit();
						}
						{
							setState(568);
							digit();
						}
						{
							setState(569);
							digit();
						}
						{
							setState(570);
							digit();
						}
						{
							setState(571);
							digit();
						}
						{
							setState(572);
							digit();
						}
						{
							setState(573);
							digit();
						}
						{
							setState(574);
							digit();
						}
						{
							setState(575);
							digit();
						}
						{
							setState(576);
							digit();
						}
					}
				}
					break;
				}
			}
				break;
			case 2:
				enterOuterAlt(_localctx, 2); {
				setState(581);
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1: {
						{
							setState(580);
							codechar();
						}
					}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(583);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input, 42, _ctx);
				} while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
			}
				break;
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class WsContext.
	 */
	public static class WsContext extends ParserRuleContext {

		/**
		 * Sp.
		 *
		 * @return the list
		 */
		public List<SpContext> sp() {
			return getRuleContexts(SpContext.class);
		}

		/**
		 * Sp.
		 *
		 * @param i the i
		 * @return the sp context
		 */
		public SpContext sp(final int i) {
			return getRuleContext(SpContext.class, i);
		}

		/**
		 * Htab.
		 *
		 * @return the list
		 */
		public List<HtabContext> htab() {
			return getRuleContexts(HtabContext.class);
		}

		/**
		 * Htab.
		 *
		 * @param i the i
		 * @return the htab context
		 */
		public HtabContext htab(final int i) {
			return getRuleContext(HtabContext.class, i);
		}

		/**
		 * Cr.
		 *
		 * @return the list
		 */
		public List<CrContext> cr() {
			return getRuleContexts(CrContext.class);
		}

		/**
		 * Cr.
		 *
		 * @param i the i
		 * @return the cr context
		 */
		public CrContext cr(final int i) {
			return getRuleContext(CrContext.class, i);
		}

		/**
		 * Lf.
		 *
		 * @return the list
		 */
		public List<LfContext> lf() {
			return getRuleContexts(LfContext.class);
		}

		/**
		 * Lf.
		 *
		 * @param i the i
		 * @return the lf context
		 */
		public LfContext lf(final int i) {
			return getRuleContext(LfContext.class, i);
		}

		/**
		 * Comment.
		 *
		 * @return the list
		 */
		public List<CommentContext> comment() {
			return getRuleContexts(CommentContext.class);
		}

		/**
		 * Comment.
		 *
		 * @param i the i
		 * @return the comment context
		 */
		public CommentContext comment(final int i) {
			return getRuleContext(CommentContext.class, i);
		}

		/**
		 * Instantiates a {@link WsContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public WsContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_ws;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterWs(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitWs(this);
			}
		}
	}

	/**
	 * Ws.
	 *
	 * @return the ws context
	 * @throws RecognitionException the recognition exception
	 */
	public final WsContext ws() throws RecognitionException {
		final WsContext _localctx = new WsContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_ws);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
				setState(594);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input, 45, _ctx);
				while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
					if (_alt == 1) {
						{
							setState(592);
							_errHandler.sync(this);
							switch (_input.LA(1)) {
							case SPACE: {
								setState(587);
								sp();
							}
								break;
							case TAB: {
								setState(588);
								htab();
							}
								break;
							case CR: {
								setState(589);
								cr();
							}
								break;
							case LF: {
								setState(590);
								lf();
							}
								break;
							case SLASH: {
								setState(591);
								comment();
							}
								break;
							default:
								throw new NoViableAltException(this);
							}
						}
					}
					setState(596);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input, 45, _ctx);
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class MwsContext.
	 */
	public static class MwsContext extends ParserRuleContext {

		/**
		 * Sp.
		 *
		 * @return the list
		 */
		public List<SpContext> sp() {
			return getRuleContexts(SpContext.class);
		}

		/**
		 * Sp.
		 *
		 * @param i the i
		 * @return the sp context
		 */
		public SpContext sp(final int i) {
			return getRuleContext(SpContext.class, i);
		}

		/**
		 * Htab.
		 *
		 * @return the list
		 */
		public List<HtabContext> htab() {
			return getRuleContexts(HtabContext.class);
		}

		/**
		 * Htab.
		 *
		 * @param i the i
		 * @return the htab context
		 */
		public HtabContext htab(final int i) {
			return getRuleContext(HtabContext.class, i);
		}

		/**
		 * Cr.
		 *
		 * @return the list
		 */
		public List<CrContext> cr() {
			return getRuleContexts(CrContext.class);
		}

		/**
		 * Cr.
		 *
		 * @param i the i
		 * @return the cr context
		 */
		public CrContext cr(final int i) {
			return getRuleContext(CrContext.class, i);
		}

		/**
		 * Lf.
		 *
		 * @return the list
		 */
		public List<LfContext> lf() {
			return getRuleContexts(LfContext.class);
		}

		/**
		 * Lf.
		 *
		 * @param i the i
		 * @return the lf context
		 */
		public LfContext lf(final int i) {
			return getRuleContext(LfContext.class, i);
		}

		/**
		 * Comment.
		 *
		 * @return the list
		 */
		public List<CommentContext> comment() {
			return getRuleContexts(CommentContext.class);
		}

		/**
		 * Comment.
		 *
		 * @param i the i
		 * @return the comment context
		 */
		public CommentContext comment(final int i) {
			return getRuleContext(CommentContext.class, i);
		}

		/**
		 * Instantiates a {@link MwsContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public MwsContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_mws;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterMws(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitMws(this);
			}
		}
	}

	/**
	 * Mws.
	 *
	 * @return the mws context
	 * @throws RecognitionException the recognition exception
	 */
	public final MwsContext mws() throws RecognitionException {
		final MwsContext _localctx = new MwsContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_mws);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
				setState(602);
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1: {
						setState(602);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPACE: {
							setState(597);
							sp();
						}
							break;
						case TAB: {
							setState(598);
							htab();
						}
							break;
						case CR: {
							setState(599);
							cr();
						}
							break;
						case LF: {
							setState(600);
							lf();
						}
							break;
						case SLASH: {
							setState(601);
							comment();
						}
							break;
						default:
							throw new NoViableAltException(this);
						}
					}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(604);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input, 47, _ctx);
				} while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class CommentContext.
	 */
	public static class CommentContext extends ParserRuleContext {

		/**
		 * Slash.
		 *
		 * @return the list
		 */
		public List<TerminalNode> SLASH() {
			return getTokens(EclParser.SLASH);
		}

		/**
		 * Slash.
		 *
		 * @param i the i
		 * @return the terminal node
		 */
		public TerminalNode SLASH(final int i) {
			return getToken(EclParser.SLASH, i);
		}

		/**
		 * Asterisk.
		 *
		 * @return the list
		 */
		public List<TerminalNode> ASTERISK() {
			return getTokens(EclParser.ASTERISK);
		}

		/**
		 * Asterisk.
		 *
		 * @param i the i
		 * @return the terminal node
		 */
		public TerminalNode ASTERISK(final int i) {
			return getToken(EclParser.ASTERISK, i);
		}

		/**
		 * Nonstarchar.
		 *
		 * @return the list
		 */
		public List<NonstarcharContext> nonstarchar() {
			return getRuleContexts(NonstarcharContext.class);
		}

		/**
		 * Nonstarchar.
		 *
		 * @param i the i
		 * @return the nonstarchar context
		 */
		public NonstarcharContext nonstarchar(final int i) {
			return getRuleContext(NonstarcharContext.class, i);
		}

		/**
		 * Starwithnonfslash.
		 *
		 * @return the list
		 */
		public List<StarwithnonfslashContext> starwithnonfslash() {
			return getRuleContexts(StarwithnonfslashContext.class);
		}

		/**
		 * Starwithnonfslash.
		 *
		 * @param i the i
		 * @return the starwithnonfslash context
		 */
		public StarwithnonfslashContext starwithnonfslash(final int i) {
			return getRuleContext(StarwithnonfslashContext.class, i);
		}

		/**
		 * Instantiates a {@link CommentContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public CommentContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_comment;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterComment(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitComment(this);
			}
		}
	}

	/**
	 * Comment.
	 *
	 * @return the comment context
	 * @throws RecognitionException the recognition exception
	 */
	public final CommentContext comment() throws RecognitionException {
		final CommentContext _localctx = new CommentContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_comment);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
				{
					setState(606);
					match(SLASH);
					setState(607);
					match(ASTERISK);
				}
				setState(613);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input, 49, _ctx);
				while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
					if (_alt == 1) {
						{
							setState(611);
							_errHandler.sync(this);
							switch (_input.LA(1)) {
							case TAB:
							case LF:
							case CR:
							case SPACE:
							case EXCLAMATION:
							case QUOTE:
							case POUND:
							case DOLLAR:
							case PERCENT:
							case AMPERSAND:
							case APOSTROPHE:
							case LEFT_PAREN:
							case RIGHT_PAREN:
							case PLUS:
							case COMMA:
							case DASH:
							case PERIOD:
							case SLASH:
							case ZERO:
							case ONE:
							case TWO:
							case THREE:
							case FOUR:
							case FIVE:
							case SIX:
							case SEVEN:
							case EIGHT:
							case NINE:
							case COLON:
							case SEMICOLON:
							case LESS_THAN:
							case EQUALS:
							case GREATER_THAN:
							case QUESTION:
							case AT:
							case CAP_A:
							case CAP_B:
							case CAP_C:
							case CAP_D:
							case CAP_E:
							case CAP_F:
							case CAP_G:
							case CAP_H:
							case CAP_I:
							case CAP_J:
							case CAP_K:
							case CAP_L:
							case CAP_M:
							case CAP_N:
							case CAP_O:
							case CAP_P:
							case CAP_Q:
							case CAP_R:
							case CAP_S:
							case CAP_T:
							case CAP_U:
							case CAP_V:
							case CAP_W:
							case CAP_X:
							case CAP_Y:
							case CAP_Z:
							case LEFT_BRACE:
							case BACKSLASH:
							case RIGHT_BRACE:
							case CARAT:
							case UNDERSCORE:
							case ACCENT:
							case A:
							case B:
							case C:
							case D:
							case E:
							case F:
							case G:
							case H:
							case I:
							case J:
							case K:
							case L:
							case M:
							case N:
							case O:
							case P:
							case Q:
							case R:
							case S:
							case T:
							case U:
							case V:
							case W:
							case X:
							case Y:
							case Z:
							case LEFT_CURLY_BRACE:
							case PIPE:
							case RIGHT_CURLY_BRACE:
							case TILDE:
							case U_00C2:
							case U_00C3:
							case U_00C4:
							case U_00C5:
							case U_00C6:
							case U_00C7:
							case U_00C8:
							case U_00C9:
							case U_00CA:
							case U_00CB:
							case U_00CC:
							case U_00CD:
							case U_00CE:
							case U_00CF:
							case U_00D0:
							case U_00D1:
							case U_00D2:
							case U_00D3:
							case U_00D4:
							case U_00D5:
							case U_00D6:
							case U_00D7:
							case U_00D8:
							case U_00D9:
							case U_00DA:
							case U_00DB:
							case U_00DC:
							case U_00DD:
							case U_00DE:
							case U_00DF:
							case U_00E0:
							case U_00E1:
							case U_00E2:
							case U_00E3:
							case U_00E4:
							case U_00E5:
							case U_00E6:
							case U_00E7:
							case U_00E8:
							case U_00E9:
							case U_00EA:
							case U_00EB:
							case U_00EC:
							case U_00ED:
							case U_00EE:
							case U_00EF:
							case U_00F0:
							case U_00F1:
							case U_00F2:
							case U_00F3:
							case U_00F4: {
								setState(609);
								nonstarchar();
							}
								break;
							case ASTERISK: {
								setState(610);
								starwithnonfslash();
							}
								break;
							default:
								throw new NoViableAltException(this);
							}
						}
					}
					setState(615);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input, 49, _ctx);
				}
				{
					setState(616);
					match(ASTERISK);
					setState(617);
					match(SLASH);
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class NonstarcharContext.
	 */
	public static class NonstarcharContext extends ParserRuleContext {

		/**
		 * Sp.
		 *
		 * @return the sp context
		 */
		public SpContext sp() {
			return getRuleContext(SpContext.class, 0);
		}

		/**
		 * Htab.
		 *
		 * @return the htab context
		 */
		public HtabContext htab() {
			return getRuleContext(HtabContext.class, 0);
		}

		/**
		 * Cr.
		 *
		 * @return the cr context
		 */
		public CrContext cr() {
			return getRuleContext(CrContext.class, 0);
		}

		/**
		 * Lf.
		 *
		 * @return the lf context
		 */
		public LfContext lf() {
			return getRuleContext(LfContext.class, 0);
		}

		/**
		 * Exclamation.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EXCLAMATION() {
			return getToken(EclParser.EXCLAMATION, 0);
		}

		/**
		 * Quote.
		 *
		 * @return the terminal node
		 */
		public TerminalNode QUOTE() {
			return getToken(EclParser.QUOTE, 0);
		}

		/**
		 * Pound.
		 *
		 * @return the terminal node
		 */
		public TerminalNode POUND() {
			return getToken(EclParser.POUND, 0);
		}

		/**
		 * Dollar.
		 *
		 * @return the terminal node
		 */
		public TerminalNode DOLLAR() {
			return getToken(EclParser.DOLLAR, 0);
		}

		/**
		 * Percent.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PERCENT() {
			return getToken(EclParser.PERCENT, 0);
		}

		/**
		 * Ampersand.
		 *
		 * @return the terminal node
		 */
		public TerminalNode AMPERSAND() {
			return getToken(EclParser.AMPERSAND, 0);
		}

		/**
		 * Apostrophe.
		 *
		 * @return the terminal node
		 */
		public TerminalNode APOSTROPHE() {
			return getToken(EclParser.APOSTROPHE, 0);
		}

		/**
		 * Left paren.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_PAREN() {
			return getToken(EclParser.LEFT_PAREN, 0);
		}

		/**
		 * Right paren.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_PAREN() {
			return getToken(EclParser.RIGHT_PAREN, 0);
		}

		/**
		 * Plus.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PLUS() {
			return getToken(EclParser.PLUS, 0);
		}

		/**
		 * Comma.
		 *
		 * @return the terminal node
		 */
		public TerminalNode COMMA() {
			return getToken(EclParser.COMMA, 0);
		}

		/**
		 * Dash.
		 *
		 * @return the terminal node
		 */
		public TerminalNode DASH() {
			return getToken(EclParser.DASH, 0);
		}

		/**
		 * Period.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PERIOD() {
			return getToken(EclParser.PERIOD, 0);
		}

		/**
		 * Slash.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SLASH() {
			return getToken(EclParser.SLASH, 0);
		}

		/**
		 * Zero.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ZERO() {
			return getToken(EclParser.ZERO, 0);
		}

		/**
		 * One.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ONE() {
			return getToken(EclParser.ONE, 0);
		}

		/**
		 * Two.
		 *
		 * @return the terminal node
		 */
		public TerminalNode TWO() {
			return getToken(EclParser.TWO, 0);
		}

		/**
		 * Three.
		 *
		 * @return the terminal node
		 */
		public TerminalNode THREE() {
			return getToken(EclParser.THREE, 0);
		}

		/**
		 * Four.
		 *
		 * @return the terminal node
		 */
		public TerminalNode FOUR() {
			return getToken(EclParser.FOUR, 0);
		}

		/**
		 * Five.
		 *
		 * @return the terminal node
		 */
		public TerminalNode FIVE() {
			return getToken(EclParser.FIVE, 0);
		}

		/**
		 * Six.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SIX() {
			return getToken(EclParser.SIX, 0);
		}

		/**
		 * Seven.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SEVEN() {
			return getToken(EclParser.SEVEN, 0);
		}

		/**
		 * Eight.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EIGHT() {
			return getToken(EclParser.EIGHT, 0);
		}

		/**
		 * Nine.
		 *
		 * @return the terminal node
		 */
		public TerminalNode NINE() {
			return getToken(EclParser.NINE, 0);
		}

		/**
		 * Colon.
		 *
		 * @return the terminal node
		 */
		public TerminalNode COLON() {
			return getToken(EclParser.COLON, 0);
		}

		/**
		 * Semicolon.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SEMICOLON() {
			return getToken(EclParser.SEMICOLON, 0);
		}

		/**
		 * Less than.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LESS_THAN() {
			return getToken(EclParser.LESS_THAN, 0);
		}

		/**
		 * Equals.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EQUALS() {
			return getToken(EclParser.EQUALS, 0);
		}

		/**
		 * Greater than.
		 *
		 * @return the terminal node
		 */
		public TerminalNode GREATER_THAN() {
			return getToken(EclParser.GREATER_THAN, 0);
		}

		/**
		 * Question.
		 *
		 * @return the terminal node
		 */
		public TerminalNode QUESTION() {
			return getToken(EclParser.QUESTION, 0);
		}

		/**
		 * At.
		 *
		 * @return the terminal node
		 */
		public TerminalNode AT() {
			return getToken(EclParser.AT, 0);
		}

		/**
		 * Cap a.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_A() {
			return getToken(EclParser.CAP_A, 0);
		}

		/**
		 * Cap b.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_B() {
			return getToken(EclParser.CAP_B, 0);
		}

		/**
		 * Cap c.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_C() {
			return getToken(EclParser.CAP_C, 0);
		}

		/**
		 * Cap d.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_D() {
			return getToken(EclParser.CAP_D, 0);
		}

		/**
		 * Cap e.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_E() {
			return getToken(EclParser.CAP_E, 0);
		}

		/**
		 * Cap f.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_F() {
			return getToken(EclParser.CAP_F, 0);
		}

		/**
		 * Cap g.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_G() {
			return getToken(EclParser.CAP_G, 0);
		}

		/**
		 * Cap h.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_H() {
			return getToken(EclParser.CAP_H, 0);
		}

		/**
		 * Cap i.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_I() {
			return getToken(EclParser.CAP_I, 0);
		}

		/**
		 * Cap j.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_J() {
			return getToken(EclParser.CAP_J, 0);
		}

		/**
		 * Cap k.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_K() {
			return getToken(EclParser.CAP_K, 0);
		}

		/**
		 * Cap l.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_L() {
			return getToken(EclParser.CAP_L, 0);
		}

		/**
		 * Cap m.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_M() {
			return getToken(EclParser.CAP_M, 0);
		}

		/**
		 * Cap n.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_N() {
			return getToken(EclParser.CAP_N, 0);
		}

		/**
		 * Cap o.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_O() {
			return getToken(EclParser.CAP_O, 0);
		}

		/**
		 * Cap p.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_P() {
			return getToken(EclParser.CAP_P, 0);
		}

		/**
		 * Cap q.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Q() {
			return getToken(EclParser.CAP_Q, 0);
		}

		/**
		 * Cap r.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_R() {
			return getToken(EclParser.CAP_R, 0);
		}

		/**
		 * Cap s.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_S() {
			return getToken(EclParser.CAP_S, 0);
		}

		/**
		 * Cap t.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_T() {
			return getToken(EclParser.CAP_T, 0);
		}

		/**
		 * Cap u.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_U() {
			return getToken(EclParser.CAP_U, 0);
		}

		/**
		 * Cap v.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_V() {
			return getToken(EclParser.CAP_V, 0);
		}

		/**
		 * Cap w.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_W() {
			return getToken(EclParser.CAP_W, 0);
		}

		/**
		 * Cap x.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_X() {
			return getToken(EclParser.CAP_X, 0);
		}

		/**
		 * Cap y.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Y() {
			return getToken(EclParser.CAP_Y, 0);
		}

		/**
		 * Cap z.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Z() {
			return getToken(EclParser.CAP_Z, 0);
		}

		/**
		 * Left brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_BRACE() {
			return getToken(EclParser.LEFT_BRACE, 0);
		}

		/**
		 * Backslash.
		 *
		 * @return the terminal node
		 */
		public TerminalNode BACKSLASH() {
			return getToken(EclParser.BACKSLASH, 0);
		}

		/**
		 * Right brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_BRACE() {
			return getToken(EclParser.RIGHT_BRACE, 0);
		}

		/**
		 * Carat.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CARAT() {
			return getToken(EclParser.CARAT, 0);
		}

		/**
		 * Underscore.
		 *
		 * @return the terminal node
		 */
		public TerminalNode UNDERSCORE() {
			return getToken(EclParser.UNDERSCORE, 0);
		}

		/**
		 * Accent.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ACCENT() {
			return getToken(EclParser.ACCENT, 0);
		}

		/**
		 * A.
		 *
		 * @return the terminal node
		 */
		public TerminalNode A() {
			return getToken(EclParser.A, 0);
		}

		/**
		 * B.
		 *
		 * @return the terminal node
		 */
		public TerminalNode B() {
			return getToken(EclParser.B, 0);
		}

		/**
		 * C.
		 *
		 * @return the terminal node
		 */
		public TerminalNode C() {
			return getToken(EclParser.C, 0);
		}

		/**
		 * D.
		 *
		 * @return the terminal node
		 */
		public TerminalNode D() {
			return getToken(EclParser.D, 0);
		}

		/**
		 * E.
		 *
		 * @return the terminal node
		 */
		public TerminalNode E() {
			return getToken(EclParser.E, 0);
		}

		/**
		 * F.
		 *
		 * @return the terminal node
		 */
		public TerminalNode F() {
			return getToken(EclParser.F, 0);
		}

		/**
		 * G.
		 *
		 * @return the terminal node
		 */
		public TerminalNode G() {
			return getToken(EclParser.G, 0);
		}

		/**
		 * H.
		 *
		 * @return the terminal node
		 */
		public TerminalNode H() {
			return getToken(EclParser.H, 0);
		}

		/**
		 * I.
		 *
		 * @return the terminal node
		 */
		public TerminalNode I() {
			return getToken(EclParser.I, 0);
		}

		/**
		 * J.
		 *
		 * @return the terminal node
		 */
		public TerminalNode J() {
			return getToken(EclParser.J, 0);
		}

		/**
		 * K.
		 *
		 * @return the terminal node
		 */
		public TerminalNode K() {
			return getToken(EclParser.K, 0);
		}

		/**
		 * L.
		 *
		 * @return the terminal node
		 */
		public TerminalNode L() {
			return getToken(EclParser.L, 0);
		}

		/**
		 * M.
		 *
		 * @return the terminal node
		 */
		public TerminalNode M() {
			return getToken(EclParser.M, 0);
		}

		/**
		 * N.
		 *
		 * @return the terminal node
		 */
		public TerminalNode N() {
			return getToken(EclParser.N, 0);
		}

		/**
		 * O.
		 *
		 * @return the terminal node
		 */
		public TerminalNode O() {
			return getToken(EclParser.O, 0);
		}

		/**
		 * P.
		 *
		 * @return the terminal node
		 */
		public TerminalNode P() {
			return getToken(EclParser.P, 0);
		}

		/**
		 * Q.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Q() {
			return getToken(EclParser.Q, 0);
		}

		/**
		 * R.
		 *
		 * @return the terminal node
		 */
		public TerminalNode R() {
			return getToken(EclParser.R, 0);
		}

		/**
		 * S.
		 *
		 * @return the terminal node
		 */
		public TerminalNode S() {
			return getToken(EclParser.S, 0);
		}

		/**
		 * T.
		 *
		 * @return the terminal node
		 */
		public TerminalNode T() {
			return getToken(EclParser.T, 0);
		}

		/**
		 * U.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U() {
			return getToken(EclParser.U, 0);
		}

		/**
		 * V.
		 *
		 * @return the terminal node
		 */
		public TerminalNode V() {
			return getToken(EclParser.V, 0);
		}

		/**
		 * W.
		 *
		 * @return the terminal node
		 */
		public TerminalNode W() {
			return getToken(EclParser.W, 0);
		}

		/**
		 * X.
		 *
		 * @return the terminal node
		 */
		public TerminalNode X() {
			return getToken(EclParser.X, 0);
		}

		/**
		 * Y.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Y() {
			return getToken(EclParser.Y, 0);
		}

		/**
		 * Z.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Z() {
			return getToken(EclParser.Z, 0);
		}

		/**
		 * Left curly brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_CURLY_BRACE() {
			return getToken(EclParser.LEFT_CURLY_BRACE, 0);
		}

		/**
		 * Pipe.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PIPE() {
			return getToken(EclParser.PIPE, 0);
		}

		/**
		 * Right curly brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_CURLY_BRACE() {
			return getToken(EclParser.RIGHT_CURLY_BRACE, 0);
		}

		/**
		 * Tilde.
		 *
		 * @return the terminal node
		 */
		public TerminalNode TILDE() {
			return getToken(EclParser.TILDE, 0);
		}

		/**
		 * Utf 8 2.
		 *
		 * @return the utf 8 2 context
		 */
		public Utf8_2Context utf8_2() {
			return getRuleContext(Utf8_2Context.class, 0);
		}

		/**
		 * Utf 8 3.
		 *
		 * @return the utf 8 3 context
		 */
		public Utf8_3Context utf8_3() {
			return getRuleContext(Utf8_3Context.class, 0);
		}

		/**
		 * Utf 8 4.
		 *
		 * @return the utf 8 4 context
		 */
		public Utf8_4Context utf8_4() {
			return getRuleContext(Utf8_4Context.class, 0);
		}

		/**
		 * Instantiates a {@link NonstarcharContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public NonstarcharContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_nonstarchar;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterNonstarchar(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitNonstarchar(this);
			}
		}
	}

	/**
	 * Nonstarchar.
	 *
	 * @return the nonstarchar context
	 * @throws RecognitionException the recognition exception
	 */
	public final NonstarcharContext nonstarchar() throws RecognitionException {
		final NonstarcharContext _localctx = new NonstarcharContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_nonstarchar);
		int _la;
		try {
			setState(628);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SPACE:
				enterOuterAlt(_localctx, 1); {
				setState(619);
				sp();
			}
				break;
			case TAB:
				enterOuterAlt(_localctx, 2); {
				setState(620);
				htab();
			}
				break;
			case CR:
				enterOuterAlt(_localctx, 3); {
				setState(621);
				cr();
			}
				break;
			case LF:
				enterOuterAlt(_localctx, 4); {
				setState(622);
				lf();
			}
				break;
			case EXCLAMATION:
			case QUOTE:
			case POUND:
			case DOLLAR:
			case PERCENT:
			case AMPERSAND:
			case APOSTROPHE:
			case LEFT_PAREN:
			case RIGHT_PAREN:
				enterOuterAlt(_localctx, 5); {
				setState(623);
				_la = _input.LA(1);
				if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EXCLAMATION) | (1L << QUOTE) | (1L << POUND)
						| (1L << DOLLAR) | (1L << PERCENT) | (1L << AMPERSAND) | (1L << APOSTROPHE) | (1L << LEFT_PAREN)
						| (1L << RIGHT_PAREN))) != 0))) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
			}
				break;
			case PLUS:
			case COMMA:
			case DASH:
			case PERIOD:
			case SLASH:
			case ZERO:
			case ONE:
			case TWO:
			case THREE:
			case FOUR:
			case FIVE:
			case SIX:
			case SEVEN:
			case EIGHT:
			case NINE:
			case COLON:
			case SEMICOLON:
			case LESS_THAN:
			case EQUALS:
			case GREATER_THAN:
			case QUESTION:
			case AT:
			case CAP_A:
			case CAP_B:
			case CAP_C:
			case CAP_D:
			case CAP_E:
			case CAP_F:
			case CAP_G:
			case CAP_H:
			case CAP_I:
			case CAP_J:
			case CAP_K:
			case CAP_L:
			case CAP_M:
			case CAP_N:
			case CAP_O:
			case CAP_P:
			case CAP_Q:
			case CAP_R:
			case CAP_S:
			case CAP_T:
			case CAP_U:
			case CAP_V:
			case CAP_W:
			case CAP_X:
			case CAP_Y:
			case CAP_Z:
			case LEFT_BRACE:
			case BACKSLASH:
			case RIGHT_BRACE:
			case CARAT:
			case UNDERSCORE:
			case ACCENT:
			case A:
			case B:
			case C:
			case D:
			case E:
			case F:
			case G:
			case H:
			case I:
			case J:
			case K:
			case L:
			case M:
			case N:
			case O:
			case P:
			case Q:
			case R:
			case S:
			case T:
			case U:
			case V:
			case W:
			case X:
			case Y:
			case Z:
			case LEFT_CURLY_BRACE:
			case PIPE:
			case RIGHT_CURLY_BRACE:
			case TILDE:
				enterOuterAlt(_localctx, 6); {
				setState(624);
				_la = _input.LA(1);
				if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << PLUS) | (1L << COMMA) | (1L << DASH)
						| (1L << PERIOD) | (1L << SLASH) | (1L << ZERO) | (1L << ONE) | (1L << TWO) | (1L << THREE)
						| (1L << FOUR) | (1L << FIVE) | (1L << SIX) | (1L << SEVEN) | (1L << EIGHT) | (1L << NINE)
						| (1L << COLON) | (1L << SEMICOLON) | (1L << LESS_THAN) | (1L << EQUALS) | (1L << GREATER_THAN)
						| (1L << QUESTION) | (1L << AT) | (1L << CAP_A) | (1L << CAP_B) | (1L << CAP_C) | (1L << CAP_D)
						| (1L << CAP_E) | (1L << CAP_F) | (1L << CAP_G) | (1L << CAP_H) | (1L << CAP_I) | (1L << CAP_J)
						| (1L << CAP_K) | (1L << CAP_L) | (1L << CAP_M) | (1L << CAP_N) | (1L << CAP_O) | (1L << CAP_P)
						| (1L << CAP_Q) | (1L << CAP_R) | (1L << CAP_S) | (1L << CAP_T) | (1L << CAP_U) | (1L << CAP_V)
						| (1L << CAP_W) | (1L << CAP_X) | (1L << CAP_Y) | (1L << CAP_Z) | (1L << LEFT_BRACE))) != 0)
						|| ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64))
								& ((1L << (BACKSLASH - 64)) | (1L << (RIGHT_BRACE - 64)) | (1L << (CARAT - 64))
										| (1L << (UNDERSCORE - 64)) | (1L << (ACCENT - 64)) | (1L << (A - 64))
										| (1L << (B - 64)) | (1L << (C - 64)) | (1L << (D - 64)) | (1L << (E - 64))
										| (1L << (F - 64)) | (1L << (G - 64)) | (1L << (H - 64)) | (1L << (I - 64))
										| (1L << (J - 64)) | (1L << (K - 64)) | (1L << (L - 64)) | (1L << (M - 64))
										| (1L << (N - 64)) | (1L << (O - 64)) | (1L << (P - 64)) | (1L << (Q - 64))
										| (1L << (R - 64)) | (1L << (S - 64)) | (1L << (T - 64)) | (1L << (U - 64))
										| (1L << (V - 64)) | (1L << (W - 64)) | (1L << (X - 64)) | (1L << (Y - 64))
										| (1L << (Z - 64)) | (1L << (LEFT_CURLY_BRACE - 64)) | (1L << (PIPE - 64))
										| (1L << (RIGHT_CURLY_BRACE - 64)) | (1L << (TILDE - 64)))) != 0))) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
			}
				break;
			case U_00C2:
			case U_00C3:
			case U_00C4:
			case U_00C5:
			case U_00C6:
			case U_00C7:
			case U_00C8:
			case U_00C9:
			case U_00CA:
			case U_00CB:
			case U_00CC:
			case U_00CD:
			case U_00CE:
			case U_00CF:
			case U_00D0:
			case U_00D1:
			case U_00D2:
			case U_00D3:
			case U_00D4:
			case U_00D5:
			case U_00D6:
			case U_00D7:
			case U_00D8:
			case U_00D9:
			case U_00DA:
			case U_00DB:
			case U_00DC:
			case U_00DD:
			case U_00DE:
			case U_00DF:
				enterOuterAlt(_localctx, 7); {
				setState(625);
				utf8_2();
			}
				break;
			case U_00E0:
			case U_00E1:
			case U_00E2:
			case U_00E3:
			case U_00E4:
			case U_00E5:
			case U_00E6:
			case U_00E7:
			case U_00E8:
			case U_00E9:
			case U_00EA:
			case U_00EB:
			case U_00EC:
			case U_00ED:
			case U_00EE:
			case U_00EF:
				enterOuterAlt(_localctx, 8); {
				setState(626);
				utf8_3();
			}
				break;
			case U_00F0:
			case U_00F1:
			case U_00F2:
			case U_00F3:
			case U_00F4:
				enterOuterAlt(_localctx, 9); {
				setState(627);
				utf8_4();
			}
				break;
			default:
				throw new NoViableAltException(this);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class StarwithnonfslashContext.
	 */
	public static class StarwithnonfslashContext extends ParserRuleContext {

		/**
		 * Asterisk.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ASTERISK() {
			return getToken(EclParser.ASTERISK, 0);
		}

		/**
		 * Nonfslash.
		 *
		 * @return the nonfslash context
		 */
		public NonfslashContext nonfslash() {
			return getRuleContext(NonfslashContext.class, 0);
		}

		/**
		 * Instantiates a {@link StarwithnonfslashContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public StarwithnonfslashContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_starwithnonfslash;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterStarwithnonfslash(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitStarwithnonfslash(this);
			}
		}
	}

	/**
	 * Starwithnonfslash.
	 *
	 * @return the starwithnonfslash context
	 * @throws RecognitionException the recognition exception
	 */
	public final StarwithnonfslashContext starwithnonfslash() throws RecognitionException {
		final StarwithnonfslashContext _localctx = new StarwithnonfslashContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_starwithnonfslash);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(630);
				match(ASTERISK);
				setState(631);
				nonfslash();
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class NonfslashContext.
	 */
	public static class NonfslashContext extends ParserRuleContext {

		/**
		 * Sp.
		 *
		 * @return the sp context
		 */
		public SpContext sp() {
			return getRuleContext(SpContext.class, 0);
		}

		/**
		 * Htab.
		 *
		 * @return the htab context
		 */
		public HtabContext htab() {
			return getRuleContext(HtabContext.class, 0);
		}

		/**
		 * Cr.
		 *
		 * @return the cr context
		 */
		public CrContext cr() {
			return getRuleContext(CrContext.class, 0);
		}

		/**
		 * Lf.
		 *
		 * @return the lf context
		 */
		public LfContext lf() {
			return getRuleContext(LfContext.class, 0);
		}

		/**
		 * Exclamation.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EXCLAMATION() {
			return getToken(EclParser.EXCLAMATION, 0);
		}

		/**
		 * Quote.
		 *
		 * @return the terminal node
		 */
		public TerminalNode QUOTE() {
			return getToken(EclParser.QUOTE, 0);
		}

		/**
		 * Pound.
		 *
		 * @return the terminal node
		 */
		public TerminalNode POUND() {
			return getToken(EclParser.POUND, 0);
		}

		/**
		 * Dollar.
		 *
		 * @return the terminal node
		 */
		public TerminalNode DOLLAR() {
			return getToken(EclParser.DOLLAR, 0);
		}

		/**
		 * Percent.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PERCENT() {
			return getToken(EclParser.PERCENT, 0);
		}

		/**
		 * Ampersand.
		 *
		 * @return the terminal node
		 */
		public TerminalNode AMPERSAND() {
			return getToken(EclParser.AMPERSAND, 0);
		}

		/**
		 * Apostrophe.
		 *
		 * @return the terminal node
		 */
		public TerminalNode APOSTROPHE() {
			return getToken(EclParser.APOSTROPHE, 0);
		}

		/**
		 * Left paren.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_PAREN() {
			return getToken(EclParser.LEFT_PAREN, 0);
		}

		/**
		 * Right paren.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_PAREN() {
			return getToken(EclParser.RIGHT_PAREN, 0);
		}

		/**
		 * Asterisk.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ASTERISK() {
			return getToken(EclParser.ASTERISK, 0);
		}

		/**
		 * Plus.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PLUS() {
			return getToken(EclParser.PLUS, 0);
		}

		/**
		 * Comma.
		 *
		 * @return the terminal node
		 */
		public TerminalNode COMMA() {
			return getToken(EclParser.COMMA, 0);
		}

		/**
		 * Dash.
		 *
		 * @return the terminal node
		 */
		public TerminalNode DASH() {
			return getToken(EclParser.DASH, 0);
		}

		/**
		 * Period.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PERIOD() {
			return getToken(EclParser.PERIOD, 0);
		}

		/**
		 * Zero.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ZERO() {
			return getToken(EclParser.ZERO, 0);
		}

		/**
		 * One.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ONE() {
			return getToken(EclParser.ONE, 0);
		}

		/**
		 * Two.
		 *
		 * @return the terminal node
		 */
		public TerminalNode TWO() {
			return getToken(EclParser.TWO, 0);
		}

		/**
		 * Three.
		 *
		 * @return the terminal node
		 */
		public TerminalNode THREE() {
			return getToken(EclParser.THREE, 0);
		}

		/**
		 * Four.
		 *
		 * @return the terminal node
		 */
		public TerminalNode FOUR() {
			return getToken(EclParser.FOUR, 0);
		}

		/**
		 * Five.
		 *
		 * @return the terminal node
		 */
		public TerminalNode FIVE() {
			return getToken(EclParser.FIVE, 0);
		}

		/**
		 * Six.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SIX() {
			return getToken(EclParser.SIX, 0);
		}

		/**
		 * Seven.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SEVEN() {
			return getToken(EclParser.SEVEN, 0);
		}

		/**
		 * Eight.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EIGHT() {
			return getToken(EclParser.EIGHT, 0);
		}

		/**
		 * Nine.
		 *
		 * @return the terminal node
		 */
		public TerminalNode NINE() {
			return getToken(EclParser.NINE, 0);
		}

		/**
		 * Colon.
		 *
		 * @return the terminal node
		 */
		public TerminalNode COLON() {
			return getToken(EclParser.COLON, 0);
		}

		/**
		 * Semicolon.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SEMICOLON() {
			return getToken(EclParser.SEMICOLON, 0);
		}

		/**
		 * Less than.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LESS_THAN() {
			return getToken(EclParser.LESS_THAN, 0);
		}

		/**
		 * Equals.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EQUALS() {
			return getToken(EclParser.EQUALS, 0);
		}

		/**
		 * Greater than.
		 *
		 * @return the terminal node
		 */
		public TerminalNode GREATER_THAN() {
			return getToken(EclParser.GREATER_THAN, 0);
		}

		/**
		 * Question.
		 *
		 * @return the terminal node
		 */
		public TerminalNode QUESTION() {
			return getToken(EclParser.QUESTION, 0);
		}

		/**
		 * At.
		 *
		 * @return the terminal node
		 */
		public TerminalNode AT() {
			return getToken(EclParser.AT, 0);
		}

		/**
		 * Cap a.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_A() {
			return getToken(EclParser.CAP_A, 0);
		}

		/**
		 * Cap b.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_B() {
			return getToken(EclParser.CAP_B, 0);
		}

		/**
		 * Cap c.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_C() {
			return getToken(EclParser.CAP_C, 0);
		}

		/**
		 * Cap d.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_D() {
			return getToken(EclParser.CAP_D, 0);
		}

		/**
		 * Cap e.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_E() {
			return getToken(EclParser.CAP_E, 0);
		}

		/**
		 * Cap f.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_F() {
			return getToken(EclParser.CAP_F, 0);
		}

		/**
		 * Cap g.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_G() {
			return getToken(EclParser.CAP_G, 0);
		}

		/**
		 * Cap h.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_H() {
			return getToken(EclParser.CAP_H, 0);
		}

		/**
		 * Cap i.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_I() {
			return getToken(EclParser.CAP_I, 0);
		}

		/**
		 * Cap j.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_J() {
			return getToken(EclParser.CAP_J, 0);
		}

		/**
		 * Cap k.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_K() {
			return getToken(EclParser.CAP_K, 0);
		}

		/**
		 * Cap l.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_L() {
			return getToken(EclParser.CAP_L, 0);
		}

		/**
		 * Cap m.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_M() {
			return getToken(EclParser.CAP_M, 0);
		}

		/**
		 * Cap n.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_N() {
			return getToken(EclParser.CAP_N, 0);
		}

		/**
		 * Cap o.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_O() {
			return getToken(EclParser.CAP_O, 0);
		}

		/**
		 * Cap p.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_P() {
			return getToken(EclParser.CAP_P, 0);
		}

		/**
		 * Cap q.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Q() {
			return getToken(EclParser.CAP_Q, 0);
		}

		/**
		 * Cap r.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_R() {
			return getToken(EclParser.CAP_R, 0);
		}

		/**
		 * Cap s.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_S() {
			return getToken(EclParser.CAP_S, 0);
		}

		/**
		 * Cap t.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_T() {
			return getToken(EclParser.CAP_T, 0);
		}

		/**
		 * Cap u.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_U() {
			return getToken(EclParser.CAP_U, 0);
		}

		/**
		 * Cap v.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_V() {
			return getToken(EclParser.CAP_V, 0);
		}

		/**
		 * Cap w.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_W() {
			return getToken(EclParser.CAP_W, 0);
		}

		/**
		 * Cap x.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_X() {
			return getToken(EclParser.CAP_X, 0);
		}

		/**
		 * Cap y.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Y() {
			return getToken(EclParser.CAP_Y, 0);
		}

		/**
		 * Cap z.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Z() {
			return getToken(EclParser.CAP_Z, 0);
		}

		/**
		 * Left brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_BRACE() {
			return getToken(EclParser.LEFT_BRACE, 0);
		}

		/**
		 * Backslash.
		 *
		 * @return the terminal node
		 */
		public TerminalNode BACKSLASH() {
			return getToken(EclParser.BACKSLASH, 0);
		}

		/**
		 * Right brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_BRACE() {
			return getToken(EclParser.RIGHT_BRACE, 0);
		}

		/**
		 * Carat.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CARAT() {
			return getToken(EclParser.CARAT, 0);
		}

		/**
		 * Underscore.
		 *
		 * @return the terminal node
		 */
		public TerminalNode UNDERSCORE() {
			return getToken(EclParser.UNDERSCORE, 0);
		}

		/**
		 * Accent.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ACCENT() {
			return getToken(EclParser.ACCENT, 0);
		}

		/**
		 * A.
		 *
		 * @return the terminal node
		 */
		public TerminalNode A() {
			return getToken(EclParser.A, 0);
		}

		/**
		 * B.
		 *
		 * @return the terminal node
		 */
		public TerminalNode B() {
			return getToken(EclParser.B, 0);
		}

		/**
		 * C.
		 *
		 * @return the terminal node
		 */
		public TerminalNode C() {
			return getToken(EclParser.C, 0);
		}

		/**
		 * D.
		 *
		 * @return the terminal node
		 */
		public TerminalNode D() {
			return getToken(EclParser.D, 0);
		}

		/**
		 * E.
		 *
		 * @return the terminal node
		 */
		public TerminalNode E() {
			return getToken(EclParser.E, 0);
		}

		/**
		 * F.
		 *
		 * @return the terminal node
		 */
		public TerminalNode F() {
			return getToken(EclParser.F, 0);
		}

		/**
		 * G.
		 *
		 * @return the terminal node
		 */
		public TerminalNode G() {
			return getToken(EclParser.G, 0);
		}

		/**
		 * H.
		 *
		 * @return the terminal node
		 */
		public TerminalNode H() {
			return getToken(EclParser.H, 0);
		}

		/**
		 * I.
		 *
		 * @return the terminal node
		 */
		public TerminalNode I() {
			return getToken(EclParser.I, 0);
		}

		/**
		 * J.
		 *
		 * @return the terminal node
		 */
		public TerminalNode J() {
			return getToken(EclParser.J, 0);
		}

		/**
		 * K.
		 *
		 * @return the terminal node
		 */
		public TerminalNode K() {
			return getToken(EclParser.K, 0);
		}

		/**
		 * L.
		 *
		 * @return the terminal node
		 */
		public TerminalNode L() {
			return getToken(EclParser.L, 0);
		}

		/**
		 * M.
		 *
		 * @return the terminal node
		 */
		public TerminalNode M() {
			return getToken(EclParser.M, 0);
		}

		/**
		 * N.
		 *
		 * @return the terminal node
		 */
		public TerminalNode N() {
			return getToken(EclParser.N, 0);
		}

		/**
		 * O.
		 *
		 * @return the terminal node
		 */
		public TerminalNode O() {
			return getToken(EclParser.O, 0);
		}

		/**
		 * P.
		 *
		 * @return the terminal node
		 */
		public TerminalNode P() {
			return getToken(EclParser.P, 0);
		}

		/**
		 * Q.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Q() {
			return getToken(EclParser.Q, 0);
		}

		/**
		 * R.
		 *
		 * @return the terminal node
		 */
		public TerminalNode R() {
			return getToken(EclParser.R, 0);
		}

		/**
		 * S.
		 *
		 * @return the terminal node
		 */
		public TerminalNode S() {
			return getToken(EclParser.S, 0);
		}

		/**
		 * T.
		 *
		 * @return the terminal node
		 */
		public TerminalNode T() {
			return getToken(EclParser.T, 0);
		}

		/**
		 * U.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U() {
			return getToken(EclParser.U, 0);
		}

		/**
		 * V.
		 *
		 * @return the terminal node
		 */
		public TerminalNode V() {
			return getToken(EclParser.V, 0);
		}

		/**
		 * W.
		 *
		 * @return the terminal node
		 */
		public TerminalNode W() {
			return getToken(EclParser.W, 0);
		}

		/**
		 * X.
		 *
		 * @return the terminal node
		 */
		public TerminalNode X() {
			return getToken(EclParser.X, 0);
		}

		/**
		 * Y.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Y() {
			return getToken(EclParser.Y, 0);
		}

		/**
		 * Z.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Z() {
			return getToken(EclParser.Z, 0);
		}

		/**
		 * Left curly brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_CURLY_BRACE() {
			return getToken(EclParser.LEFT_CURLY_BRACE, 0);
		}

		/**
		 * Pipe.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PIPE() {
			return getToken(EclParser.PIPE, 0);
		}

		/**
		 * Right curly brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_CURLY_BRACE() {
			return getToken(EclParser.RIGHT_CURLY_BRACE, 0);
		}

		/**
		 * Tilde.
		 *
		 * @return the terminal node
		 */
		public TerminalNode TILDE() {
			return getToken(EclParser.TILDE, 0);
		}

		/**
		 * Utf 8 2.
		 *
		 * @return the utf 8 2 context
		 */
		public Utf8_2Context utf8_2() {
			return getRuleContext(Utf8_2Context.class, 0);
		}

		/**
		 * Utf 8 3.
		 *
		 * @return the utf 8 3 context
		 */
		public Utf8_3Context utf8_3() {
			return getRuleContext(Utf8_3Context.class, 0);
		}

		/**
		 * Utf 8 4.
		 *
		 * @return the utf 8 4 context
		 */
		public Utf8_4Context utf8_4() {
			return getRuleContext(Utf8_4Context.class, 0);
		}

		/**
		 * Instantiates a {@link NonfslashContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public NonfslashContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_nonfslash;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterNonfslash(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitNonfslash(this);
			}
		}
	}

	/**
	 * Nonfslash.
	 *
	 * @return the nonfslash context
	 * @throws RecognitionException the recognition exception
	 */
	public final NonfslashContext nonfslash() throws RecognitionException {
		final NonfslashContext _localctx = new NonfslashContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_nonfslash);
		int _la;
		try {
			setState(642);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SPACE:
				enterOuterAlt(_localctx, 1); {
				setState(633);
				sp();
			}
				break;
			case TAB:
				enterOuterAlt(_localctx, 2); {
				setState(634);
				htab();
			}
				break;
			case CR:
				enterOuterAlt(_localctx, 3); {
				setState(635);
				cr();
			}
				break;
			case LF:
				enterOuterAlt(_localctx, 4); {
				setState(636);
				lf();
			}
				break;
			case EXCLAMATION:
			case QUOTE:
			case POUND:
			case DOLLAR:
			case PERCENT:
			case AMPERSAND:
			case APOSTROPHE:
			case LEFT_PAREN:
			case RIGHT_PAREN:
			case ASTERISK:
			case PLUS:
			case COMMA:
			case DASH:
			case PERIOD:
				enterOuterAlt(_localctx, 5); {
				setState(637);
				_la = _input.LA(1);
				if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EXCLAMATION) | (1L << QUOTE) | (1L << POUND)
						| (1L << DOLLAR) | (1L << PERCENT) | (1L << AMPERSAND) | (1L << APOSTROPHE) | (1L << LEFT_PAREN)
						| (1L << RIGHT_PAREN) | (1L << ASTERISK) | (1L << PLUS) | (1L << COMMA) | (1L << DASH)
						| (1L << PERIOD))) != 0))) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
			}
				break;
			case ZERO:
			case ONE:
			case TWO:
			case THREE:
			case FOUR:
			case FIVE:
			case SIX:
			case SEVEN:
			case EIGHT:
			case NINE:
			case COLON:
			case SEMICOLON:
			case LESS_THAN:
			case EQUALS:
			case GREATER_THAN:
			case QUESTION:
			case AT:
			case CAP_A:
			case CAP_B:
			case CAP_C:
			case CAP_D:
			case CAP_E:
			case CAP_F:
			case CAP_G:
			case CAP_H:
			case CAP_I:
			case CAP_J:
			case CAP_K:
			case CAP_L:
			case CAP_M:
			case CAP_N:
			case CAP_O:
			case CAP_P:
			case CAP_Q:
			case CAP_R:
			case CAP_S:
			case CAP_T:
			case CAP_U:
			case CAP_V:
			case CAP_W:
			case CAP_X:
			case CAP_Y:
			case CAP_Z:
			case LEFT_BRACE:
			case BACKSLASH:
			case RIGHT_BRACE:
			case CARAT:
			case UNDERSCORE:
			case ACCENT:
			case A:
			case B:
			case C:
			case D:
			case E:
			case F:
			case G:
			case H:
			case I:
			case J:
			case K:
			case L:
			case M:
			case N:
			case O:
			case P:
			case Q:
			case R:
			case S:
			case T:
			case U:
			case V:
			case W:
			case X:
			case Y:
			case Z:
			case LEFT_CURLY_BRACE:
			case PIPE:
			case RIGHT_CURLY_BRACE:
			case TILDE:
				enterOuterAlt(_localctx, 6); {
				setState(638);
				_la = _input.LA(1);
				if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ZERO) | (1L << ONE) | (1L << TWO) | (1L << THREE)
						| (1L << FOUR) | (1L << FIVE) | (1L << SIX) | (1L << SEVEN) | (1L << EIGHT) | (1L << NINE)
						| (1L << COLON) | (1L << SEMICOLON) | (1L << LESS_THAN) | (1L << EQUALS) | (1L << GREATER_THAN)
						| (1L << QUESTION) | (1L << AT) | (1L << CAP_A) | (1L << CAP_B) | (1L << CAP_C) | (1L << CAP_D)
						| (1L << CAP_E) | (1L << CAP_F) | (1L << CAP_G) | (1L << CAP_H) | (1L << CAP_I) | (1L << CAP_J)
						| (1L << CAP_K) | (1L << CAP_L) | (1L << CAP_M) | (1L << CAP_N) | (1L << CAP_O) | (1L << CAP_P)
						| (1L << CAP_Q) | (1L << CAP_R) | (1L << CAP_S) | (1L << CAP_T) | (1L << CAP_U) | (1L << CAP_V)
						| (1L << CAP_W) | (1L << CAP_X) | (1L << CAP_Y) | (1L << CAP_Z) | (1L << LEFT_BRACE))) != 0)
						|| ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64))
								& ((1L << (BACKSLASH - 64)) | (1L << (RIGHT_BRACE - 64)) | (1L << (CARAT - 64))
										| (1L << (UNDERSCORE - 64)) | (1L << (ACCENT - 64)) | (1L << (A - 64))
										| (1L << (B - 64)) | (1L << (C - 64)) | (1L << (D - 64)) | (1L << (E - 64))
										| (1L << (F - 64)) | (1L << (G - 64)) | (1L << (H - 64)) | (1L << (I - 64))
										| (1L << (J - 64)) | (1L << (K - 64)) | (1L << (L - 64)) | (1L << (M - 64))
										| (1L << (N - 64)) | (1L << (O - 64)) | (1L << (P - 64)) | (1L << (Q - 64))
										| (1L << (R - 64)) | (1L << (S - 64)) | (1L << (T - 64)) | (1L << (U - 64))
										| (1L << (V - 64)) | (1L << (W - 64)) | (1L << (X - 64)) | (1L << (Y - 64))
										| (1L << (Z - 64)) | (1L << (LEFT_CURLY_BRACE - 64)) | (1L << (PIPE - 64))
										| (1L << (RIGHT_CURLY_BRACE - 64)) | (1L << (TILDE - 64)))) != 0))) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
			}
				break;
			case U_00C2:
			case U_00C3:
			case U_00C4:
			case U_00C5:
			case U_00C6:
			case U_00C7:
			case U_00C8:
			case U_00C9:
			case U_00CA:
			case U_00CB:
			case U_00CC:
			case U_00CD:
			case U_00CE:
			case U_00CF:
			case U_00D0:
			case U_00D1:
			case U_00D2:
			case U_00D3:
			case U_00D4:
			case U_00D5:
			case U_00D6:
			case U_00D7:
			case U_00D8:
			case U_00D9:
			case U_00DA:
			case U_00DB:
			case U_00DC:
			case U_00DD:
			case U_00DE:
			case U_00DF:
				enterOuterAlt(_localctx, 7); {
				setState(639);
				utf8_2();
			}
				break;
			case U_00E0:
			case U_00E1:
			case U_00E2:
			case U_00E3:
			case U_00E4:
			case U_00E5:
			case U_00E6:
			case U_00E7:
			case U_00E8:
			case U_00E9:
			case U_00EA:
			case U_00EB:
			case U_00EC:
			case U_00ED:
			case U_00EE:
			case U_00EF:
				enterOuterAlt(_localctx, 8); {
				setState(640);
				utf8_3();
			}
				break;
			case U_00F0:
			case U_00F1:
			case U_00F2:
			case U_00F3:
			case U_00F4:
				enterOuterAlt(_localctx, 9); {
				setState(641);
				utf8_4();
			}
				break;
			default:
				throw new NoViableAltException(this);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class SpContext.
	 */
	public static class SpContext extends ParserRuleContext {

		/**
		 * Space.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SPACE() {
			return getToken(EclParser.SPACE, 0);
		}

		/**
		 * Instantiates a {@link SpContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public SpContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_sp;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterSp(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitSp(this);
			}
		}
	}

	/**
	 * Sp.
	 *
	 * @return the sp context
	 * @throws RecognitionException the recognition exception
	 */
	public final SpContext sp() throws RecognitionException {
		final SpContext _localctx = new SpContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_sp);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(644);
				match(SPACE);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class HtabContext.
	 */
	public static class HtabContext extends ParserRuleContext {

		/**
		 * Tab.
		 *
		 * @return the terminal node
		 */
		public TerminalNode TAB() {
			return getToken(EclParser.TAB, 0);
		}

		/**
		 * Instantiates a {@link HtabContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public HtabContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_htab;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterHtab(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitHtab(this);
			}
		}
	}

	/**
	 * Htab.
	 *
	 * @return the htab context
	 * @throws RecognitionException the recognition exception
	 */
	public final HtabContext htab() throws RecognitionException {
		final HtabContext _localctx = new HtabContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_htab);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(646);
				match(TAB);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class CrContext.
	 */
	public static class CrContext extends ParserRuleContext {

		/**
		 * Cr.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CR() {
			return getToken(EclParser.CR, 0);
		}

		/**
		 * Instantiates a {@link CrContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public CrContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_cr;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterCr(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitCr(this);
			}
		}
	}

	/**
	 * Cr.
	 *
	 * @return the cr context
	 * @throws RecognitionException the recognition exception
	 */
	public final CrContext cr() throws RecognitionException {
		final CrContext _localctx = new CrContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_cr);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(648);
				match(CR);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class LfContext.
	 */
	public static class LfContext extends ParserRuleContext {

		/**
		 * Lf.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LF() {
			return getToken(EclParser.LF, 0);
		}

		/**
		 * Instantiates a {@link LfContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public LfContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_lf;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterLf(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitLf(this);
			}
		}
	}

	/**
	 * Lf.
	 *
	 * @return the lf context
	 * @throws RecognitionException the recognition exception
	 */
	public final LfContext lf() throws RecognitionException {
		final LfContext _localctx = new LfContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_lf);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(650);
				match(LF);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class QmContext.
	 */
	public static class QmContext extends ParserRuleContext {

		/**
		 * Quote.
		 *
		 * @return the terminal node
		 */
		public TerminalNode QUOTE() {
			return getToken(EclParser.QUOTE, 0);
		}

		/**
		 * Instantiates a {@link QmContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public QmContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_qm;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterQm(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitQm(this);
			}
		}
	}

	/**
	 * Qm.
	 *
	 * @return the qm context
	 * @throws RecognitionException the recognition exception
	 */
	public final QmContext qm() throws RecognitionException {
		final QmContext _localctx = new QmContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_qm);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(652);
				match(QUOTE);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class BsContext.
	 */
	public static class BsContext extends ParserRuleContext {

		/**
		 * Backslash.
		 *
		 * @return the terminal node
		 */
		public TerminalNode BACKSLASH() {
			return getToken(EclParser.BACKSLASH, 0);
		}

		/**
		 * Instantiates a {@link BsContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public BsContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_bs;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterBs(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitBs(this);
			}
		}
	}

	/**
	 * Bs.
	 *
	 * @return the bs context
	 * @throws RecognitionException the recognition exception
	 */
	public final BsContext bs() throws RecognitionException {
		final BsContext _localctx = new BsContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_bs);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(654);
				match(BACKSLASH);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class DigitContext.
	 */
	public static class DigitContext extends ParserRuleContext {

		/**
		 * Zero.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ZERO() {
			return getToken(EclParser.ZERO, 0);
		}

		/**
		 * One.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ONE() {
			return getToken(EclParser.ONE, 0);
		}

		/**
		 * Two.
		 *
		 * @return the terminal node
		 */
		public TerminalNode TWO() {
			return getToken(EclParser.TWO, 0);
		}

		/**
		 * Three.
		 *
		 * @return the terminal node
		 */
		public TerminalNode THREE() {
			return getToken(EclParser.THREE, 0);
		}

		/**
		 * Four.
		 *
		 * @return the terminal node
		 */
		public TerminalNode FOUR() {
			return getToken(EclParser.FOUR, 0);
		}

		/**
		 * Five.
		 *
		 * @return the terminal node
		 */
		public TerminalNode FIVE() {
			return getToken(EclParser.FIVE, 0);
		}

		/**
		 * Six.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SIX() {
			return getToken(EclParser.SIX, 0);
		}

		/**
		 * Seven.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SEVEN() {
			return getToken(EclParser.SEVEN, 0);
		}

		/**
		 * Eight.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EIGHT() {
			return getToken(EclParser.EIGHT, 0);
		}

		/**
		 * Nine.
		 *
		 * @return the terminal node
		 */
		public TerminalNode NINE() {
			return getToken(EclParser.NINE, 0);
		}

		/**
		 * Instantiates a {@link DigitContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public DigitContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_digit;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterDigit(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitDigit(this);
			}
		}
	}

	/**
	 * Digit.
	 *
	 * @return the digit context
	 * @throws RecognitionException the recognition exception
	 */
	public final DigitContext digit() throws RecognitionException {
		final DigitContext _localctx = new DigitContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_digit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(656);
				_la = _input.LA(1);
				if (!((((_la) & ~0x3f) == 0
						&& ((1L << _la) & ((1L << ZERO) | (1L << ONE) | (1L << TWO) | (1L << THREE) | (1L << FOUR)
								| (1L << FIVE) | (1L << SIX) | (1L << SEVEN) | (1L << EIGHT) | (1L << NINE))) != 0))) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class ZeroContext.
	 */
	public static class ZeroContext extends ParserRuleContext {

		/**
		 * Zero.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ZERO() {
			return getToken(EclParser.ZERO, 0);
		}

		/**
		 * Instantiates a {@link ZeroContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public ZeroContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_zero;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterZero(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitZero(this);
			}
		}
	}

	/**
	 * Zero.
	 *
	 * @return the zero context
	 * @throws RecognitionException the recognition exception
	 */
	public final ZeroContext zero() throws RecognitionException {
		final ZeroContext _localctx = new ZeroContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_zero);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(658);
				match(ZERO);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class DigitnonzeroContext.
	 */
	public static class DigitnonzeroContext extends ParserRuleContext {

		/**
		 * One.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ONE() {
			return getToken(EclParser.ONE, 0);
		}

		/**
		 * Two.
		 *
		 * @return the terminal node
		 */
		public TerminalNode TWO() {
			return getToken(EclParser.TWO, 0);
		}

		/**
		 * Three.
		 *
		 * @return the terminal node
		 */
		public TerminalNode THREE() {
			return getToken(EclParser.THREE, 0);
		}

		/**
		 * Four.
		 *
		 * @return the terminal node
		 */
		public TerminalNode FOUR() {
			return getToken(EclParser.FOUR, 0);
		}

		/**
		 * Five.
		 *
		 * @return the terminal node
		 */
		public TerminalNode FIVE() {
			return getToken(EclParser.FIVE, 0);
		}

		/**
		 * Six.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SIX() {
			return getToken(EclParser.SIX, 0);
		}

		/**
		 * Seven.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SEVEN() {
			return getToken(EclParser.SEVEN, 0);
		}

		/**
		 * Eight.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EIGHT() {
			return getToken(EclParser.EIGHT, 0);
		}

		/**
		 * Nine.
		 *
		 * @return the terminal node
		 */
		public TerminalNode NINE() {
			return getToken(EclParser.NINE, 0);
		}

		/**
		 * Instantiates a {@link DigitnonzeroContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public DigitnonzeroContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_digitnonzero;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterDigitnonzero(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitDigitnonzero(this);
			}
		}
	}

	/**
	 * Digitnonzero.
	 *
	 * @return the digitnonzero context
	 * @throws RecognitionException the recognition exception
	 */
	public final DigitnonzeroContext digitnonzero() throws RecognitionException {
		final DigitnonzeroContext _localctx = new DigitnonzeroContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_digitnonzero);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(660);
				_la = _input.LA(1);
				if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ONE) | (1L << TWO) | (1L << THREE) | (1L << FOUR)
						| (1L << FIVE) | (1L << SIX) | (1L << SEVEN) | (1L << EIGHT) | (1L << NINE))) != 0))) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class NonwsnonpipeContext.
	 */
	public static class NonwsnonpipeContext extends ParserRuleContext {

		/**
		 * Exclamation.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EXCLAMATION() {
			return getToken(EclParser.EXCLAMATION, 0);
		}

		/**
		 * Quote.
		 *
		 * @return the terminal node
		 */
		public TerminalNode QUOTE() {
			return getToken(EclParser.QUOTE, 0);
		}

		/**
		 * Pound.
		 *
		 * @return the terminal node
		 */
		public TerminalNode POUND() {
			return getToken(EclParser.POUND, 0);
		}

		/**
		 * Dollar.
		 *
		 * @return the terminal node
		 */
		public TerminalNode DOLLAR() {
			return getToken(EclParser.DOLLAR, 0);
		}

		/**
		 * Percent.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PERCENT() {
			return getToken(EclParser.PERCENT, 0);
		}

		/**
		 * Ampersand.
		 *
		 * @return the terminal node
		 */
		public TerminalNode AMPERSAND() {
			return getToken(EclParser.AMPERSAND, 0);
		}

		/**
		 * Apostrophe.
		 *
		 * @return the terminal node
		 */
		public TerminalNode APOSTROPHE() {
			return getToken(EclParser.APOSTROPHE, 0);
		}

		/**
		 * Left paren.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_PAREN() {
			return getToken(EclParser.LEFT_PAREN, 0);
		}

		/**
		 * Right paren.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_PAREN() {
			return getToken(EclParser.RIGHT_PAREN, 0);
		}

		/**
		 * Asterisk.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ASTERISK() {
			return getToken(EclParser.ASTERISK, 0);
		}

		/**
		 * Plus.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PLUS() {
			return getToken(EclParser.PLUS, 0);
		}

		/**
		 * Comma.
		 *
		 * @return the terminal node
		 */
		public TerminalNode COMMA() {
			return getToken(EclParser.COMMA, 0);
		}

		/**
		 * Dash.
		 *
		 * @return the terminal node
		 */
		public TerminalNode DASH() {
			return getToken(EclParser.DASH, 0);
		}

		/**
		 * Period.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PERIOD() {
			return getToken(EclParser.PERIOD, 0);
		}

		/**
		 * Slash.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SLASH() {
			return getToken(EclParser.SLASH, 0);
		}

		/**
		 * Zero.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ZERO() {
			return getToken(EclParser.ZERO, 0);
		}

		/**
		 * One.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ONE() {
			return getToken(EclParser.ONE, 0);
		}

		/**
		 * Two.
		 *
		 * @return the terminal node
		 */
		public TerminalNode TWO() {
			return getToken(EclParser.TWO, 0);
		}

		/**
		 * Three.
		 *
		 * @return the terminal node
		 */
		public TerminalNode THREE() {
			return getToken(EclParser.THREE, 0);
		}

		/**
		 * Four.
		 *
		 * @return the terminal node
		 */
		public TerminalNode FOUR() {
			return getToken(EclParser.FOUR, 0);
		}

		/**
		 * Five.
		 *
		 * @return the terminal node
		 */
		public TerminalNode FIVE() {
			return getToken(EclParser.FIVE, 0);
		}

		/**
		 * Six.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SIX() {
			return getToken(EclParser.SIX, 0);
		}

		/**
		 * Seven.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SEVEN() {
			return getToken(EclParser.SEVEN, 0);
		}

		/**
		 * Eight.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EIGHT() {
			return getToken(EclParser.EIGHT, 0);
		}

		/**
		 * Nine.
		 *
		 * @return the terminal node
		 */
		public TerminalNode NINE() {
			return getToken(EclParser.NINE, 0);
		}

		/**
		 * Colon.
		 *
		 * @return the terminal node
		 */
		public TerminalNode COLON() {
			return getToken(EclParser.COLON, 0);
		}

		/**
		 * Semicolon.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SEMICOLON() {
			return getToken(EclParser.SEMICOLON, 0);
		}

		/**
		 * Less than.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LESS_THAN() {
			return getToken(EclParser.LESS_THAN, 0);
		}

		/**
		 * Equals.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EQUALS() {
			return getToken(EclParser.EQUALS, 0);
		}

		/**
		 * Greater than.
		 *
		 * @return the terminal node
		 */
		public TerminalNode GREATER_THAN() {
			return getToken(EclParser.GREATER_THAN, 0);
		}

		/**
		 * Question.
		 *
		 * @return the terminal node
		 */
		public TerminalNode QUESTION() {
			return getToken(EclParser.QUESTION, 0);
		}

		/**
		 * At.
		 *
		 * @return the terminal node
		 */
		public TerminalNode AT() {
			return getToken(EclParser.AT, 0);
		}

		/**
		 * Cap a.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_A() {
			return getToken(EclParser.CAP_A, 0);
		}

		/**
		 * Cap b.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_B() {
			return getToken(EclParser.CAP_B, 0);
		}

		/**
		 * Cap c.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_C() {
			return getToken(EclParser.CAP_C, 0);
		}

		/**
		 * Cap d.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_D() {
			return getToken(EclParser.CAP_D, 0);
		}

		/**
		 * Cap e.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_E() {
			return getToken(EclParser.CAP_E, 0);
		}

		/**
		 * Cap f.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_F() {
			return getToken(EclParser.CAP_F, 0);
		}

		/**
		 * Cap g.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_G() {
			return getToken(EclParser.CAP_G, 0);
		}

		/**
		 * Cap h.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_H() {
			return getToken(EclParser.CAP_H, 0);
		}

		/**
		 * Cap i.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_I() {
			return getToken(EclParser.CAP_I, 0);
		}

		/**
		 * Cap j.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_J() {
			return getToken(EclParser.CAP_J, 0);
		}

		/**
		 * Cap k.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_K() {
			return getToken(EclParser.CAP_K, 0);
		}

		/**
		 * Cap l.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_L() {
			return getToken(EclParser.CAP_L, 0);
		}

		/**
		 * Cap m.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_M() {
			return getToken(EclParser.CAP_M, 0);
		}

		/**
		 * Cap n.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_N() {
			return getToken(EclParser.CAP_N, 0);
		}

		/**
		 * Cap o.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_O() {
			return getToken(EclParser.CAP_O, 0);
		}

		/**
		 * Cap p.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_P() {
			return getToken(EclParser.CAP_P, 0);
		}

		/**
		 * Cap q.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Q() {
			return getToken(EclParser.CAP_Q, 0);
		}

		/**
		 * Cap r.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_R() {
			return getToken(EclParser.CAP_R, 0);
		}

		/**
		 * Cap s.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_S() {
			return getToken(EclParser.CAP_S, 0);
		}

		/**
		 * Cap t.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_T() {
			return getToken(EclParser.CAP_T, 0);
		}

		/**
		 * Cap u.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_U() {
			return getToken(EclParser.CAP_U, 0);
		}

		/**
		 * Cap v.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_V() {
			return getToken(EclParser.CAP_V, 0);
		}

		/**
		 * Cap w.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_W() {
			return getToken(EclParser.CAP_W, 0);
		}

		/**
		 * Cap x.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_X() {
			return getToken(EclParser.CAP_X, 0);
		}

		/**
		 * Cap y.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Y() {
			return getToken(EclParser.CAP_Y, 0);
		}

		/**
		 * Cap z.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Z() {
			return getToken(EclParser.CAP_Z, 0);
		}

		/**
		 * Left brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_BRACE() {
			return getToken(EclParser.LEFT_BRACE, 0);
		}

		/**
		 * Backslash.
		 *
		 * @return the terminal node
		 */
		public TerminalNode BACKSLASH() {
			return getToken(EclParser.BACKSLASH, 0);
		}

		/**
		 * Right brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_BRACE() {
			return getToken(EclParser.RIGHT_BRACE, 0);
		}

		/**
		 * Carat.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CARAT() {
			return getToken(EclParser.CARAT, 0);
		}

		/**
		 * Underscore.
		 *
		 * @return the terminal node
		 */
		public TerminalNode UNDERSCORE() {
			return getToken(EclParser.UNDERSCORE, 0);
		}

		/**
		 * Accent.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ACCENT() {
			return getToken(EclParser.ACCENT, 0);
		}

		/**
		 * A.
		 *
		 * @return the terminal node
		 */
		public TerminalNode A() {
			return getToken(EclParser.A, 0);
		}

		/**
		 * B.
		 *
		 * @return the terminal node
		 */
		public TerminalNode B() {
			return getToken(EclParser.B, 0);
		}

		/**
		 * C.
		 *
		 * @return the terminal node
		 */
		public TerminalNode C() {
			return getToken(EclParser.C, 0);
		}

		/**
		 * D.
		 *
		 * @return the terminal node
		 */
		public TerminalNode D() {
			return getToken(EclParser.D, 0);
		}

		/**
		 * E.
		 *
		 * @return the terminal node
		 */
		public TerminalNode E() {
			return getToken(EclParser.E, 0);
		}

		/**
		 * F.
		 *
		 * @return the terminal node
		 */
		public TerminalNode F() {
			return getToken(EclParser.F, 0);
		}

		/**
		 * G.
		 *
		 * @return the terminal node
		 */
		public TerminalNode G() {
			return getToken(EclParser.G, 0);
		}

		/**
		 * H.
		 *
		 * @return the terminal node
		 */
		public TerminalNode H() {
			return getToken(EclParser.H, 0);
		}

		/**
		 * I.
		 *
		 * @return the terminal node
		 */
		public TerminalNode I() {
			return getToken(EclParser.I, 0);
		}

		/**
		 * J.
		 *
		 * @return the terminal node
		 */
		public TerminalNode J() {
			return getToken(EclParser.J, 0);
		}

		/**
		 * K.
		 *
		 * @return the terminal node
		 */
		public TerminalNode K() {
			return getToken(EclParser.K, 0);
		}

		/**
		 * L.
		 *
		 * @return the terminal node
		 */
		public TerminalNode L() {
			return getToken(EclParser.L, 0);
		}

		/**
		 * M.
		 *
		 * @return the terminal node
		 */
		public TerminalNode M() {
			return getToken(EclParser.M, 0);
		}

		/**
		 * N.
		 *
		 * @return the terminal node
		 */
		public TerminalNode N() {
			return getToken(EclParser.N, 0);
		}

		/**
		 * O.
		 *
		 * @return the terminal node
		 */
		public TerminalNode O() {
			return getToken(EclParser.O, 0);
		}

		/**
		 * P.
		 *
		 * @return the terminal node
		 */
		public TerminalNode P() {
			return getToken(EclParser.P, 0);
		}

		/**
		 * Q.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Q() {
			return getToken(EclParser.Q, 0);
		}

		/**
		 * R.
		 *
		 * @return the terminal node
		 */
		public TerminalNode R() {
			return getToken(EclParser.R, 0);
		}

		/**
		 * S.
		 *
		 * @return the terminal node
		 */
		public TerminalNode S() {
			return getToken(EclParser.S, 0);
		}

		/**
		 * T.
		 *
		 * @return the terminal node
		 */
		public TerminalNode T() {
			return getToken(EclParser.T, 0);
		}

		/**
		 * U.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U() {
			return getToken(EclParser.U, 0);
		}

		/**
		 * V.
		 *
		 * @return the terminal node
		 */
		public TerminalNode V() {
			return getToken(EclParser.V, 0);
		}

		/**
		 * W.
		 *
		 * @return the terminal node
		 */
		public TerminalNode W() {
			return getToken(EclParser.W, 0);
		}

		/**
		 * X.
		 *
		 * @return the terminal node
		 */
		public TerminalNode X() {
			return getToken(EclParser.X, 0);
		}

		/**
		 * Y.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Y() {
			return getToken(EclParser.Y, 0);
		}

		/**
		 * Z.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Z() {
			return getToken(EclParser.Z, 0);
		}

		/**
		 * Left curly brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_CURLY_BRACE() {
			return getToken(EclParser.LEFT_CURLY_BRACE, 0);
		}

		/**
		 * Right curly brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_CURLY_BRACE() {
			return getToken(EclParser.RIGHT_CURLY_BRACE, 0);
		}

		/**
		 * Tilde.
		 *
		 * @return the terminal node
		 */
		public TerminalNode TILDE() {
			return getToken(EclParser.TILDE, 0);
		}

		/**
		 * Utf 8 2.
		 *
		 * @return the utf 8 2 context
		 */
		public Utf8_2Context utf8_2() {
			return getRuleContext(Utf8_2Context.class, 0);
		}

		/**
		 * Utf 8 3.
		 *
		 * @return the utf 8 3 context
		 */
		public Utf8_3Context utf8_3() {
			return getRuleContext(Utf8_3Context.class, 0);
		}

		/**
		 * Utf 8 4.
		 *
		 * @return the utf 8 4 context
		 */
		public Utf8_4Context utf8_4() {
			return getRuleContext(Utf8_4Context.class, 0);
		}

		/**
		 * Instantiates a {@link NonwsnonpipeContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public NonwsnonpipeContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_nonwsnonpipe;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterNonwsnonpipe(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitNonwsnonpipe(this);
			}
		}
	}

	/**
	 * Nonwsnonpipe.
	 *
	 * @return the nonwsnonpipe context
	 * @throws RecognitionException the recognition exception
	 */
	public final NonwsnonpipeContext nonwsnonpipe() throws RecognitionException {
		final NonwsnonpipeContext _localctx = new NonwsnonpipeContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_nonwsnonpipe);
		int _la;
		try {
			setState(667);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXCLAMATION:
			case QUOTE:
			case POUND:
			case DOLLAR:
			case PERCENT:
			case AMPERSAND:
			case APOSTROPHE:
			case LEFT_PAREN:
			case RIGHT_PAREN:
			case ASTERISK:
			case PLUS:
			case COMMA:
			case DASH:
			case PERIOD:
			case SLASH:
			case ZERO:
			case ONE:
			case TWO:
			case THREE:
			case FOUR:
			case FIVE:
			case SIX:
			case SEVEN:
			case EIGHT:
			case NINE:
			case COLON:
			case SEMICOLON:
			case LESS_THAN:
			case EQUALS:
			case GREATER_THAN:
			case QUESTION:
			case AT:
			case CAP_A:
			case CAP_B:
			case CAP_C:
			case CAP_D:
			case CAP_E:
			case CAP_F:
			case CAP_G:
			case CAP_H:
			case CAP_I:
			case CAP_J:
			case CAP_K:
			case CAP_L:
			case CAP_M:
			case CAP_N:
			case CAP_O:
			case CAP_P:
			case CAP_Q:
			case CAP_R:
			case CAP_S:
			case CAP_T:
			case CAP_U:
			case CAP_V:
			case CAP_W:
			case CAP_X:
			case CAP_Y:
			case CAP_Z:
			case LEFT_BRACE:
			case BACKSLASH:
			case RIGHT_BRACE:
			case CARAT:
			case UNDERSCORE:
			case ACCENT:
			case A:
			case B:
			case C:
			case D:
			case E:
			case F:
			case G:
			case H:
			case I:
			case J:
			case K:
			case L:
			case M:
			case N:
			case O:
			case P:
			case Q:
			case R:
			case S:
			case T:
			case U:
			case V:
			case W:
			case X:
			case Y:
			case Z:
			case LEFT_CURLY_BRACE:
				enterOuterAlt(_localctx, 1); {
				setState(662);
				_la = _input.LA(1);
				if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EXCLAMATION) | (1L << QUOTE) | (1L << POUND)
						| (1L << DOLLAR) | (1L << PERCENT) | (1L << AMPERSAND) | (1L << APOSTROPHE) | (1L << LEFT_PAREN)
						| (1L << RIGHT_PAREN) | (1L << ASTERISK) | (1L << PLUS) | (1L << COMMA) | (1L << DASH)
						| (1L << PERIOD) | (1L << SLASH) | (1L << ZERO) | (1L << ONE) | (1L << TWO) | (1L << THREE)
						| (1L << FOUR) | (1L << FIVE) | (1L << SIX) | (1L << SEVEN) | (1L << EIGHT) | (1L << NINE)
						| (1L << COLON) | (1L << SEMICOLON) | (1L << LESS_THAN) | (1L << EQUALS) | (1L << GREATER_THAN)
						| (1L << QUESTION) | (1L << AT) | (1L << CAP_A) | (1L << CAP_B) | (1L << CAP_C) | (1L << CAP_D)
						| (1L << CAP_E) | (1L << CAP_F) | (1L << CAP_G) | (1L << CAP_H) | (1L << CAP_I) | (1L << CAP_J)
						| (1L << CAP_K) | (1L << CAP_L) | (1L << CAP_M) | (1L << CAP_N) | (1L << CAP_O) | (1L << CAP_P)
						| (1L << CAP_Q) | (1L << CAP_R) | (1L << CAP_S) | (1L << CAP_T) | (1L << CAP_U) | (1L << CAP_V)
						| (1L << CAP_W) | (1L << CAP_X) | (1L << CAP_Y) | (1L << CAP_Z) | (1L << LEFT_BRACE))) != 0)
						|| ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64))
								& ((1L << (BACKSLASH - 64)) | (1L << (RIGHT_BRACE - 64)) | (1L << (CARAT - 64))
										| (1L << (UNDERSCORE - 64)) | (1L << (ACCENT - 64)) | (1L << (A - 64))
										| (1L << (B - 64)) | (1L << (C - 64)) | (1L << (D - 64)) | (1L << (E - 64))
										| (1L << (F - 64)) | (1L << (G - 64)) | (1L << (H - 64)) | (1L << (I - 64))
										| (1L << (J - 64)) | (1L << (K - 64)) | (1L << (L - 64)) | (1L << (M - 64))
										| (1L << (N - 64)) | (1L << (O - 64)) | (1L << (P - 64)) | (1L << (Q - 64))
										| (1L << (R - 64)) | (1L << (S - 64)) | (1L << (T - 64)) | (1L << (U - 64))
										| (1L << (V - 64)) | (1L << (W - 64)) | (1L << (X - 64)) | (1L << (Y - 64))
										| (1L << (Z - 64)) | (1L << (LEFT_CURLY_BRACE - 64)))) != 0))) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
			}
				break;
			case RIGHT_CURLY_BRACE:
			case TILDE:
				enterOuterAlt(_localctx, 2); {
				setState(663);
				_la = _input.LA(1);
				if (!(_la == RIGHT_CURLY_BRACE || _la == TILDE)) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
			}
				break;
			case U_00C2:
			case U_00C3:
			case U_00C4:
			case U_00C5:
			case U_00C6:
			case U_00C7:
			case U_00C8:
			case U_00C9:
			case U_00CA:
			case U_00CB:
			case U_00CC:
			case U_00CD:
			case U_00CE:
			case U_00CF:
			case U_00D0:
			case U_00D1:
			case U_00D2:
			case U_00D3:
			case U_00D4:
			case U_00D5:
			case U_00D6:
			case U_00D7:
			case U_00D8:
			case U_00D9:
			case U_00DA:
			case U_00DB:
			case U_00DC:
			case U_00DD:
			case U_00DE:
			case U_00DF:
				enterOuterAlt(_localctx, 3); {
				setState(664);
				utf8_2();
			}
				break;
			case U_00E0:
			case U_00E1:
			case U_00E2:
			case U_00E3:
			case U_00E4:
			case U_00E5:
			case U_00E6:
			case U_00E7:
			case U_00E8:
			case U_00E9:
			case U_00EA:
			case U_00EB:
			case U_00EC:
			case U_00ED:
			case U_00EE:
			case U_00EF:
				enterOuterAlt(_localctx, 4); {
				setState(665);
				utf8_3();
			}
				break;
			case U_00F0:
			case U_00F1:
			case U_00F2:
			case U_00F3:
			case U_00F4:
				enterOuterAlt(_localctx, 5); {
				setState(666);
				utf8_4();
			}
				break;
			default:
				throw new NoViableAltException(this);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class AnynonescapedcharContext.
	 */
	public static class AnynonescapedcharContext extends ParserRuleContext {

		/**
		 * Sp.
		 *
		 * @return the sp context
		 */
		public SpContext sp() {
			return getRuleContext(SpContext.class, 0);
		}

		/**
		 * Htab.
		 *
		 * @return the htab context
		 */
		public HtabContext htab() {
			return getRuleContext(HtabContext.class, 0);
		}

		/**
		 * Cr.
		 *
		 * @return the cr context
		 */
		public CrContext cr() {
			return getRuleContext(CrContext.class, 0);
		}

		/**
		 * Lf.
		 *
		 * @return the lf context
		 */
		public LfContext lf() {
			return getRuleContext(LfContext.class, 0);
		}

		/**
		 * Space.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SPACE() {
			return getToken(EclParser.SPACE, 0);
		}

		/**
		 * Exclamation.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EXCLAMATION() {
			return getToken(EclParser.EXCLAMATION, 0);
		}

		/**
		 * Pound.
		 *
		 * @return the terminal node
		 */
		public TerminalNode POUND() {
			return getToken(EclParser.POUND, 0);
		}

		/**
		 * Dollar.
		 *
		 * @return the terminal node
		 */
		public TerminalNode DOLLAR() {
			return getToken(EclParser.DOLLAR, 0);
		}

		/**
		 * Percent.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PERCENT() {
			return getToken(EclParser.PERCENT, 0);
		}

		/**
		 * Ampersand.
		 *
		 * @return the terminal node
		 */
		public TerminalNode AMPERSAND() {
			return getToken(EclParser.AMPERSAND, 0);
		}

		/**
		 * Apostrophe.
		 *
		 * @return the terminal node
		 */
		public TerminalNode APOSTROPHE() {
			return getToken(EclParser.APOSTROPHE, 0);
		}

		/**
		 * Left paren.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_PAREN() {
			return getToken(EclParser.LEFT_PAREN, 0);
		}

		/**
		 * Right paren.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_PAREN() {
			return getToken(EclParser.RIGHT_PAREN, 0);
		}

		/**
		 * Asterisk.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ASTERISK() {
			return getToken(EclParser.ASTERISK, 0);
		}

		/**
		 * Plus.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PLUS() {
			return getToken(EclParser.PLUS, 0);
		}

		/**
		 * Comma.
		 *
		 * @return the terminal node
		 */
		public TerminalNode COMMA() {
			return getToken(EclParser.COMMA, 0);
		}

		/**
		 * Dash.
		 *
		 * @return the terminal node
		 */
		public TerminalNode DASH() {
			return getToken(EclParser.DASH, 0);
		}

		/**
		 * Period.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PERIOD() {
			return getToken(EclParser.PERIOD, 0);
		}

		/**
		 * Slash.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SLASH() {
			return getToken(EclParser.SLASH, 0);
		}

		/**
		 * Zero.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ZERO() {
			return getToken(EclParser.ZERO, 0);
		}

		/**
		 * One.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ONE() {
			return getToken(EclParser.ONE, 0);
		}

		/**
		 * Two.
		 *
		 * @return the terminal node
		 */
		public TerminalNode TWO() {
			return getToken(EclParser.TWO, 0);
		}

		/**
		 * Three.
		 *
		 * @return the terminal node
		 */
		public TerminalNode THREE() {
			return getToken(EclParser.THREE, 0);
		}

		/**
		 * Four.
		 *
		 * @return the terminal node
		 */
		public TerminalNode FOUR() {
			return getToken(EclParser.FOUR, 0);
		}

		/**
		 * Five.
		 *
		 * @return the terminal node
		 */
		public TerminalNode FIVE() {
			return getToken(EclParser.FIVE, 0);
		}

		/**
		 * Six.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SIX() {
			return getToken(EclParser.SIX, 0);
		}

		/**
		 * Seven.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SEVEN() {
			return getToken(EclParser.SEVEN, 0);
		}

		/**
		 * Eight.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EIGHT() {
			return getToken(EclParser.EIGHT, 0);
		}

		/**
		 * Nine.
		 *
		 * @return the terminal node
		 */
		public TerminalNode NINE() {
			return getToken(EclParser.NINE, 0);
		}

		/**
		 * Colon.
		 *
		 * @return the terminal node
		 */
		public TerminalNode COLON() {
			return getToken(EclParser.COLON, 0);
		}

		/**
		 * Semicolon.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SEMICOLON() {
			return getToken(EclParser.SEMICOLON, 0);
		}

		/**
		 * Less than.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LESS_THAN() {
			return getToken(EclParser.LESS_THAN, 0);
		}

		/**
		 * Equals.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EQUALS() {
			return getToken(EclParser.EQUALS, 0);
		}

		/**
		 * Greater than.
		 *
		 * @return the terminal node
		 */
		public TerminalNode GREATER_THAN() {
			return getToken(EclParser.GREATER_THAN, 0);
		}

		/**
		 * Question.
		 *
		 * @return the terminal node
		 */
		public TerminalNode QUESTION() {
			return getToken(EclParser.QUESTION, 0);
		}

		/**
		 * At.
		 *
		 * @return the terminal node
		 */
		public TerminalNode AT() {
			return getToken(EclParser.AT, 0);
		}

		/**
		 * Cap a.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_A() {
			return getToken(EclParser.CAP_A, 0);
		}

		/**
		 * Cap b.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_B() {
			return getToken(EclParser.CAP_B, 0);
		}

		/**
		 * Cap c.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_C() {
			return getToken(EclParser.CAP_C, 0);
		}

		/**
		 * Cap d.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_D() {
			return getToken(EclParser.CAP_D, 0);
		}

		/**
		 * Cap e.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_E() {
			return getToken(EclParser.CAP_E, 0);
		}

		/**
		 * Cap f.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_F() {
			return getToken(EclParser.CAP_F, 0);
		}

		/**
		 * Cap g.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_G() {
			return getToken(EclParser.CAP_G, 0);
		}

		/**
		 * Cap h.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_H() {
			return getToken(EclParser.CAP_H, 0);
		}

		/**
		 * Cap i.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_I() {
			return getToken(EclParser.CAP_I, 0);
		}

		/**
		 * Cap j.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_J() {
			return getToken(EclParser.CAP_J, 0);
		}

		/**
		 * Cap k.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_K() {
			return getToken(EclParser.CAP_K, 0);
		}

		/**
		 * Cap l.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_L() {
			return getToken(EclParser.CAP_L, 0);
		}

		/**
		 * Cap m.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_M() {
			return getToken(EclParser.CAP_M, 0);
		}

		/**
		 * Cap n.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_N() {
			return getToken(EclParser.CAP_N, 0);
		}

		/**
		 * Cap o.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_O() {
			return getToken(EclParser.CAP_O, 0);
		}

		/**
		 * Cap p.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_P() {
			return getToken(EclParser.CAP_P, 0);
		}

		/**
		 * Cap q.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Q() {
			return getToken(EclParser.CAP_Q, 0);
		}

		/**
		 * Cap r.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_R() {
			return getToken(EclParser.CAP_R, 0);
		}

		/**
		 * Cap s.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_S() {
			return getToken(EclParser.CAP_S, 0);
		}

		/**
		 * Cap t.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_T() {
			return getToken(EclParser.CAP_T, 0);
		}

		/**
		 * Cap u.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_U() {
			return getToken(EclParser.CAP_U, 0);
		}

		/**
		 * Cap v.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_V() {
			return getToken(EclParser.CAP_V, 0);
		}

		/**
		 * Cap w.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_W() {
			return getToken(EclParser.CAP_W, 0);
		}

		/**
		 * Cap x.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_X() {
			return getToken(EclParser.CAP_X, 0);
		}

		/**
		 * Cap y.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Y() {
			return getToken(EclParser.CAP_Y, 0);
		}

		/**
		 * Cap z.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Z() {
			return getToken(EclParser.CAP_Z, 0);
		}

		/**
		 * Left brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_BRACE() {
			return getToken(EclParser.LEFT_BRACE, 0);
		}

		/**
		 * Right brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_BRACE() {
			return getToken(EclParser.RIGHT_BRACE, 0);
		}

		/**
		 * Carat.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CARAT() {
			return getToken(EclParser.CARAT, 0);
		}

		/**
		 * Underscore.
		 *
		 * @return the terminal node
		 */
		public TerminalNode UNDERSCORE() {
			return getToken(EclParser.UNDERSCORE, 0);
		}

		/**
		 * Accent.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ACCENT() {
			return getToken(EclParser.ACCENT, 0);
		}

		/**
		 * A.
		 *
		 * @return the terminal node
		 */
		public TerminalNode A() {
			return getToken(EclParser.A, 0);
		}

		/**
		 * B.
		 *
		 * @return the terminal node
		 */
		public TerminalNode B() {
			return getToken(EclParser.B, 0);
		}

		/**
		 * C.
		 *
		 * @return the terminal node
		 */
		public TerminalNode C() {
			return getToken(EclParser.C, 0);
		}

		/**
		 * D.
		 *
		 * @return the terminal node
		 */
		public TerminalNode D() {
			return getToken(EclParser.D, 0);
		}

		/**
		 * E.
		 *
		 * @return the terminal node
		 */
		public TerminalNode E() {
			return getToken(EclParser.E, 0);
		}

		/**
		 * F.
		 *
		 * @return the terminal node
		 */
		public TerminalNode F() {
			return getToken(EclParser.F, 0);
		}

		/**
		 * G.
		 *
		 * @return the terminal node
		 */
		public TerminalNode G() {
			return getToken(EclParser.G, 0);
		}

		/**
		 * H.
		 *
		 * @return the terminal node
		 */
		public TerminalNode H() {
			return getToken(EclParser.H, 0);
		}

		/**
		 * I.
		 *
		 * @return the terminal node
		 */
		public TerminalNode I() {
			return getToken(EclParser.I, 0);
		}

		/**
		 * J.
		 *
		 * @return the terminal node
		 */
		public TerminalNode J() {
			return getToken(EclParser.J, 0);
		}

		/**
		 * K.
		 *
		 * @return the terminal node
		 */
		public TerminalNode K() {
			return getToken(EclParser.K, 0);
		}

		/**
		 * L.
		 *
		 * @return the terminal node
		 */
		public TerminalNode L() {
			return getToken(EclParser.L, 0);
		}

		/**
		 * M.
		 *
		 * @return the terminal node
		 */
		public TerminalNode M() {
			return getToken(EclParser.M, 0);
		}

		/**
		 * N.
		 *
		 * @return the terminal node
		 */
		public TerminalNode N() {
			return getToken(EclParser.N, 0);
		}

		/**
		 * O.
		 *
		 * @return the terminal node
		 */
		public TerminalNode O() {
			return getToken(EclParser.O, 0);
		}

		/**
		 * P.
		 *
		 * @return the terminal node
		 */
		public TerminalNode P() {
			return getToken(EclParser.P, 0);
		}

		/**
		 * Q.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Q() {
			return getToken(EclParser.Q, 0);
		}

		/**
		 * R.
		 *
		 * @return the terminal node
		 */
		public TerminalNode R() {
			return getToken(EclParser.R, 0);
		}

		/**
		 * S.
		 *
		 * @return the terminal node
		 */
		public TerminalNode S() {
			return getToken(EclParser.S, 0);
		}

		/**
		 * T.
		 *
		 * @return the terminal node
		 */
		public TerminalNode T() {
			return getToken(EclParser.T, 0);
		}

		/**
		 * U.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U() {
			return getToken(EclParser.U, 0);
		}

		/**
		 * V.
		 *
		 * @return the terminal node
		 */
		public TerminalNode V() {
			return getToken(EclParser.V, 0);
		}

		/**
		 * W.
		 *
		 * @return the terminal node
		 */
		public TerminalNode W() {
			return getToken(EclParser.W, 0);
		}

		/**
		 * X.
		 *
		 * @return the terminal node
		 */
		public TerminalNode X() {
			return getToken(EclParser.X, 0);
		}

		/**
		 * Y.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Y() {
			return getToken(EclParser.Y, 0);
		}

		/**
		 * Z.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Z() {
			return getToken(EclParser.Z, 0);
		}

		/**
		 * Left curly brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode LEFT_CURLY_BRACE() {
			return getToken(EclParser.LEFT_CURLY_BRACE, 0);
		}

		/**
		 * Pipe.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PIPE() {
			return getToken(EclParser.PIPE, 0);
		}

		/**
		 * Right curly brace.
		 *
		 * @return the terminal node
		 */
		public TerminalNode RIGHT_CURLY_BRACE() {
			return getToken(EclParser.RIGHT_CURLY_BRACE, 0);
		}

		/**
		 * Tilde.
		 *
		 * @return the terminal node
		 */
		public TerminalNode TILDE() {
			return getToken(EclParser.TILDE, 0);
		}

		/**
		 * Utf 8 2.
		 *
		 * @return the utf 8 2 context
		 */
		public Utf8_2Context utf8_2() {
			return getRuleContext(Utf8_2Context.class, 0);
		}

		/**
		 * Utf 8 3.
		 *
		 * @return the utf 8 3 context
		 */
		public Utf8_3Context utf8_3() {
			return getRuleContext(Utf8_3Context.class, 0);
		}

		/**
		 * Utf 8 4.
		 *
		 * @return the utf 8 4 context
		 */
		public Utf8_4Context utf8_4() {
			return getRuleContext(Utf8_4Context.class, 0);
		}

		/**
		 * Instantiates a {@link AnynonescapedcharContext} from the specified
		 * parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public AnynonescapedcharContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_anynonescapedchar;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterAnynonescapedchar(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitAnynonescapedchar(this);
			}
		}
	}

	/**
	 * Anynonescapedchar.
	 *
	 * @return the anynonescapedchar context
	 * @throws RecognitionException the recognition exception
	 */
	public final AnynonescapedcharContext anynonescapedchar() throws RecognitionException {
		final AnynonescapedcharContext _localctx = new AnynonescapedcharContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_anynonescapedchar);
		int _la;
		try {
			setState(679);
			_errHandler.sync(this);
			switch (getInterpreter().adaptivePredict(_input, 53, _ctx)) {
			case 1:
				enterOuterAlt(_localctx, 1); {
				setState(669);
				sp();
			}
				break;
			case 2:
				enterOuterAlt(_localctx, 2); {
				setState(670);
				htab();
			}
				break;
			case 3:
				enterOuterAlt(_localctx, 3); {
				setState(671);
				cr();
			}
				break;
			case 4:
				enterOuterAlt(_localctx, 4); {
				setState(672);
				lf();
			}
				break;
			case 5:
				enterOuterAlt(_localctx, 5); {
				setState(673);
				_la = _input.LA(1);
				if (!(_la == SPACE || _la == EXCLAMATION)) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
			}
				break;
			case 6:
				enterOuterAlt(_localctx, 6); {
				setState(674);
				_la = _input.LA(1);
				if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << POUND) | (1L << DOLLAR) | (1L << PERCENT)
						| (1L << AMPERSAND) | (1L << APOSTROPHE) | (1L << LEFT_PAREN) | (1L << RIGHT_PAREN)
						| (1L << ASTERISK) | (1L << PLUS) | (1L << COMMA) | (1L << DASH) | (1L << PERIOD)
						| (1L << SLASH) | (1L << ZERO) | (1L << ONE) | (1L << TWO) | (1L << THREE) | (1L << FOUR)
						| (1L << FIVE) | (1L << SIX) | (1L << SEVEN) | (1L << EIGHT) | (1L << NINE) | (1L << COLON)
						| (1L << SEMICOLON) | (1L << LESS_THAN) | (1L << EQUALS) | (1L << GREATER_THAN)
						| (1L << QUESTION) | (1L << AT) | (1L << CAP_A) | (1L << CAP_B) | (1L << CAP_C) | (1L << CAP_D)
						| (1L << CAP_E) | (1L << CAP_F) | (1L << CAP_G) | (1L << CAP_H) | (1L << CAP_I) | (1L << CAP_J)
						| (1L << CAP_K) | (1L << CAP_L) | (1L << CAP_M) | (1L << CAP_N) | (1L << CAP_O) | (1L << CAP_P)
						| (1L << CAP_Q) | (1L << CAP_R) | (1L << CAP_S) | (1L << CAP_T) | (1L << CAP_U) | (1L << CAP_V)
						| (1L << CAP_W) | (1L << CAP_X) | (1L << CAP_Y) | (1L << CAP_Z) | (1L << LEFT_BRACE))) != 0))) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
			}
				break;
			case 7:
				enterOuterAlt(_localctx, 7); {
				setState(675);
				_la = _input.LA(1);
				if (!(((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & ((1L << (RIGHT_BRACE - 65))
						| (1L << (CARAT - 65)) | (1L << (UNDERSCORE - 65)) | (1L << (ACCENT - 65)) | (1L << (A - 65))
						| (1L << (B - 65)) | (1L << (C - 65)) | (1L << (D - 65)) | (1L << (E - 65)) | (1L << (F - 65))
						| (1L << (G - 65)) | (1L << (H - 65)) | (1L << (I - 65)) | (1L << (J - 65)) | (1L << (K - 65))
						| (1L << (L - 65)) | (1L << (M - 65)) | (1L << (N - 65)) | (1L << (O - 65)) | (1L << (P - 65))
						| (1L << (Q - 65)) | (1L << (R - 65)) | (1L << (S - 65)) | (1L << (T - 65)) | (1L << (U - 65))
						| (1L << (V - 65)) | (1L << (W - 65)) | (1L << (X - 65)) | (1L << (Y - 65)) | (1L << (Z - 65))
						| (1L << (LEFT_CURLY_BRACE - 65)) | (1L << (PIPE - 65)) | (1L << (RIGHT_CURLY_BRACE - 65))
						| (1L << (TILDE - 65)))) != 0))) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
			}
				break;
			case 8:
				enterOuterAlt(_localctx, 8); {
				setState(676);
				utf8_2();
			}
				break;
			case 9:
				enterOuterAlt(_localctx, 9); {
				setState(677);
				utf8_3();
			}
				break;
			case 10:
				enterOuterAlt(_localctx, 10); {
				setState(678);
				utf8_4();
			}
				break;
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class CodecharContext.
	 */
	public static class CodecharContext extends ParserRuleContext {

		/**
		 * Asterisk.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ASTERISK() {
			return getToken(EclParser.ASTERISK, 0);
		}

		/**
		 * Plus.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PLUS() {
			return getToken(EclParser.PLUS, 0);
		}

		/**
		 * Dash.
		 *
		 * @return the terminal node
		 */
		public TerminalNode DASH() {
			return getToken(EclParser.DASH, 0);
		}

		/**
		 * Period.
		 *
		 * @return the terminal node
		 */
		public TerminalNode PERIOD() {
			return getToken(EclParser.PERIOD, 0);
		}

		/**
		 * Zero.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ZERO() {
			return getToken(EclParser.ZERO, 0);
		}

		/**
		 * One.
		 *
		 * @return the terminal node
		 */
		public TerminalNode ONE() {
			return getToken(EclParser.ONE, 0);
		}

		/**
		 * Two.
		 *
		 * @return the terminal node
		 */
		public TerminalNode TWO() {
			return getToken(EclParser.TWO, 0);
		}

		/**
		 * Three.
		 *
		 * @return the terminal node
		 */
		public TerminalNode THREE() {
			return getToken(EclParser.THREE, 0);
		}

		/**
		 * Four.
		 *
		 * @return the terminal node
		 */
		public TerminalNode FOUR() {
			return getToken(EclParser.FOUR, 0);
		}

		/**
		 * Five.
		 *
		 * @return the terminal node
		 */
		public TerminalNode FIVE() {
			return getToken(EclParser.FIVE, 0);
		}

		/**
		 * Six.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SIX() {
			return getToken(EclParser.SIX, 0);
		}

		/**
		 * Seven.
		 *
		 * @return the terminal node
		 */
		public TerminalNode SEVEN() {
			return getToken(EclParser.SEVEN, 0);
		}

		/**
		 * Eight.
		 *
		 * @return the terminal node
		 */
		public TerminalNode EIGHT() {
			return getToken(EclParser.EIGHT, 0);
		}

		/**
		 * Nine.
		 *
		 * @return the terminal node
		 */
		public TerminalNode NINE() {
			return getToken(EclParser.NINE, 0);
		}

		/**
		 * Colon.
		 *
		 * @return the terminal node
		 */
		public TerminalNode COLON() {
			return getToken(EclParser.COLON, 0);
		}

		/**
		 * At.
		 *
		 * @return the terminal node
		 */
		public TerminalNode AT() {
			return getToken(EclParser.AT, 0);
		}

		/**
		 * Cap a.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_A() {
			return getToken(EclParser.CAP_A, 0);
		}

		/**
		 * Cap b.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_B() {
			return getToken(EclParser.CAP_B, 0);
		}

		/**
		 * Cap c.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_C() {
			return getToken(EclParser.CAP_C, 0);
		}

		/**
		 * Cap d.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_D() {
			return getToken(EclParser.CAP_D, 0);
		}

		/**
		 * Cap e.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_E() {
			return getToken(EclParser.CAP_E, 0);
		}

		/**
		 * Cap f.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_F() {
			return getToken(EclParser.CAP_F, 0);
		}

		/**
		 * Cap g.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_G() {
			return getToken(EclParser.CAP_G, 0);
		}

		/**
		 * Cap h.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_H() {
			return getToken(EclParser.CAP_H, 0);
		}

		/**
		 * Cap i.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_I() {
			return getToken(EclParser.CAP_I, 0);
		}

		/**
		 * Cap j.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_J() {
			return getToken(EclParser.CAP_J, 0);
		}

		/**
		 * Cap k.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_K() {
			return getToken(EclParser.CAP_K, 0);
		}

		/**
		 * Cap l.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_L() {
			return getToken(EclParser.CAP_L, 0);
		}

		/**
		 * Cap m.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_M() {
			return getToken(EclParser.CAP_M, 0);
		}

		/**
		 * Cap n.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_N() {
			return getToken(EclParser.CAP_N, 0);
		}

		/**
		 * Cap o.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_O() {
			return getToken(EclParser.CAP_O, 0);
		}

		/**
		 * Cap p.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_P() {
			return getToken(EclParser.CAP_P, 0);
		}

		/**
		 * Cap q.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Q() {
			return getToken(EclParser.CAP_Q, 0);
		}

		/**
		 * Cap r.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_R() {
			return getToken(EclParser.CAP_R, 0);
		}

		/**
		 * Cap s.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_S() {
			return getToken(EclParser.CAP_S, 0);
		}

		/**
		 * Cap t.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_T() {
			return getToken(EclParser.CAP_T, 0);
		}

		/**
		 * Cap u.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_U() {
			return getToken(EclParser.CAP_U, 0);
		}

		/**
		 * Cap v.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_V() {
			return getToken(EclParser.CAP_V, 0);
		}

		/**
		 * Cap w.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_W() {
			return getToken(EclParser.CAP_W, 0);
		}

		/**
		 * Cap x.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_X() {
			return getToken(EclParser.CAP_X, 0);
		}

		/**
		 * Cap y.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Y() {
			return getToken(EclParser.CAP_Y, 0);
		}

		/**
		 * Cap z.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CAP_Z() {
			return getToken(EclParser.CAP_Z, 0);
		}

		/**
		 * Carat.
		 *
		 * @return the terminal node
		 */
		public TerminalNode CARAT() {
			return getToken(EclParser.CARAT, 0);
		}

		/**
		 * Underscore.
		 *
		 * @return the terminal node
		 */
		public TerminalNode UNDERSCORE() {
			return getToken(EclParser.UNDERSCORE, 0);
		}

		/**
		 * A.
		 *
		 * @return the terminal node
		 */
		public TerminalNode A() {
			return getToken(EclParser.A, 0);
		}

		/**
		 * B.
		 *
		 * @return the terminal node
		 */
		public TerminalNode B() {
			return getToken(EclParser.B, 0);
		}

		/**
		 * C.
		 *
		 * @return the terminal node
		 */
		public TerminalNode C() {
			return getToken(EclParser.C, 0);
		}

		/**
		 * D.
		 *
		 * @return the terminal node
		 */
		public TerminalNode D() {
			return getToken(EclParser.D, 0);
		}

		/**
		 * E.
		 *
		 * @return the terminal node
		 */
		public TerminalNode E() {
			return getToken(EclParser.E, 0);
		}

		/**
		 * F.
		 *
		 * @return the terminal node
		 */
		public TerminalNode F() {
			return getToken(EclParser.F, 0);
		}

		/**
		 * G.
		 *
		 * @return the terminal node
		 */
		public TerminalNode G() {
			return getToken(EclParser.G, 0);
		}

		/**
		 * H.
		 *
		 * @return the terminal node
		 */
		public TerminalNode H() {
			return getToken(EclParser.H, 0);
		}

		/**
		 * I.
		 *
		 * @return the terminal node
		 */
		public TerminalNode I() {
			return getToken(EclParser.I, 0);
		}

		/**
		 * J.
		 *
		 * @return the terminal node
		 */
		public TerminalNode J() {
			return getToken(EclParser.J, 0);
		}

		/**
		 * K.
		 *
		 * @return the terminal node
		 */
		public TerminalNode K() {
			return getToken(EclParser.K, 0);
		}

		/**
		 * L.
		 *
		 * @return the terminal node
		 */
		public TerminalNode L() {
			return getToken(EclParser.L, 0);
		}

		/**
		 * M.
		 *
		 * @return the terminal node
		 */
		public TerminalNode M() {
			return getToken(EclParser.M, 0);
		}

		/**
		 * N.
		 *
		 * @return the terminal node
		 */
		public TerminalNode N() {
			return getToken(EclParser.N, 0);
		}

		/**
		 * O.
		 *
		 * @return the terminal node
		 */
		public TerminalNode O() {
			return getToken(EclParser.O, 0);
		}

		/**
		 * P.
		 *
		 * @return the terminal node
		 */
		public TerminalNode P() {
			return getToken(EclParser.P, 0);
		}

		/**
		 * Q.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Q() {
			return getToken(EclParser.Q, 0);
		}

		/**
		 * R.
		 *
		 * @return the terminal node
		 */
		public TerminalNode R() {
			return getToken(EclParser.R, 0);
		}

		/**
		 * S.
		 *
		 * @return the terminal node
		 */
		public TerminalNode S() {
			return getToken(EclParser.S, 0);
		}

		/**
		 * T.
		 *
		 * @return the terminal node
		 */
		public TerminalNode T() {
			return getToken(EclParser.T, 0);
		}

		/**
		 * U.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U() {
			return getToken(EclParser.U, 0);
		}

		/**
		 * V.
		 *
		 * @return the terminal node
		 */
		public TerminalNode V() {
			return getToken(EclParser.V, 0);
		}

		/**
		 * W.
		 *
		 * @return the terminal node
		 */
		public TerminalNode W() {
			return getToken(EclParser.W, 0);
		}

		/**
		 * X.
		 *
		 * @return the terminal node
		 */
		public TerminalNode X() {
			return getToken(EclParser.X, 0);
		}

		/**
		 * Y.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Y() {
			return getToken(EclParser.Y, 0);
		}

		/**
		 * Z.
		 *
		 * @return the terminal node
		 */
		public TerminalNode Z() {
			return getToken(EclParser.Z, 0);
		}

		/**
		 * Instantiates a {@link CodecharContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public CodecharContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_codechar;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterCodechar(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitCodechar(this);
			}
		}
	}

	/**
	 * Codechar.
	 *
	 * @return the codechar context
	 * @throws RecognitionException the recognition exception
	 */
	public final CodecharContext codechar() throws RecognitionException {
		final CodecharContext _localctx = new CodecharContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_codechar);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(681);
				_la = _input.LA(1);
				if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ASTERISK) | (1L << PLUS) | (1L << DASH)
						| (1L << PERIOD) | (1L << ZERO) | (1L << ONE) | (1L << TWO) | (1L << THREE) | (1L << FOUR)
						| (1L << FIVE) | (1L << SIX) | (1L << SEVEN) | (1L << EIGHT) | (1L << NINE) | (1L << COLON)
						| (1L << AT) | (1L << CAP_A) | (1L << CAP_B) | (1L << CAP_C) | (1L << CAP_D) | (1L << CAP_E)
						| (1L << CAP_F) | (1L << CAP_G) | (1L << CAP_H) | (1L << CAP_I) | (1L << CAP_J) | (1L << CAP_K)
						| (1L << CAP_L) | (1L << CAP_M) | (1L << CAP_N) | (1L << CAP_O) | (1L << CAP_P) | (1L << CAP_Q)
						| (1L << CAP_R) | (1L << CAP_S) | (1L << CAP_T) | (1L << CAP_U) | (1L << CAP_V) | (1L << CAP_W)
						| (1L << CAP_X) | (1L << CAP_Y) | (1L << CAP_Z))) != 0)
						|| ((((_la - 66)) & ~0x3f) == 0 && ((1L << (_la - 66)) & ((1L << (CARAT - 66))
								| (1L << (UNDERSCORE - 66)) | (1L << (A - 66)) | (1L << (B - 66)) | (1L << (C - 66))
								| (1L << (D - 66)) | (1L << (E - 66)) | (1L << (F - 66)) | (1L << (G - 66))
								| (1L << (H - 66)) | (1L << (I - 66)) | (1L << (J - 66)) | (1L << (K - 66))
								| (1L << (L - 66)) | (1L << (M - 66)) | (1L << (N - 66)) | (1L << (O - 66))
								| (1L << (P - 66)) | (1L << (Q - 66)) | (1L << (R - 66)) | (1L << (S - 66))
								| (1L << (T - 66)) | (1L << (U - 66)) | (1L << (V - 66)) | (1L << (W - 66))
								| (1L << (X - 66)) | (1L << (Y - 66)) | (1L << (Z - 66)))) != 0))) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class EscapedcharContext.
	 */
	public static class EscapedcharContext extends ParserRuleContext {

		/**
		 * Bs.
		 *
		 * @return the list
		 */
		public List<BsContext> bs() {
			return getRuleContexts(BsContext.class);
		}

		/**
		 * Bs.
		 *
		 * @param i the i
		 * @return the bs context
		 */
		public BsContext bs(final int i) {
			return getRuleContext(BsContext.class, i);
		}

		/**
		 * Qm.
		 *
		 * @return the qm context
		 */
		public QmContext qm() {
			return getRuleContext(QmContext.class, 0);
		}

		/**
		 * Instantiates a {@link EscapedcharContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public EscapedcharContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_escapedchar;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterEscapedchar(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitEscapedchar(this);
			}
		}
	}

	/**
	 * Escapedchar.
	 *
	 * @return the escapedchar context
	 * @throws RecognitionException the recognition exception
	 */
	public final EscapedcharContext escapedchar() throws RecognitionException {
		final EscapedcharContext _localctx = new EscapedcharContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_escapedchar);
		try {
			setState(689);
			_errHandler.sync(this);
			switch (getInterpreter().adaptivePredict(_input, 54, _ctx)) {
			case 1:
				enterOuterAlt(_localctx, 1); {
				{
					setState(683);
					bs();
					setState(684);
					qm();
				}
			}
				break;
			case 2:
				enterOuterAlt(_localctx, 2); {
				{
					setState(686);
					bs();
					setState(687);
					bs();
				}
			}
				break;
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class Utf8_2Context.
	 */
	public static class Utf8_2Context extends ParserRuleContext {

		/**
		 * Utf 8 tail.
		 *
		 * @return the utf 8 tail context
		 */
		public Utf8_tailContext utf8_tail() {
			return getRuleContext(Utf8_tailContext.class, 0);
		}

		/**
		 * U 00c2.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00C2() {
			return getToken(EclParser.U_00C2, 0);
		}

		/**
		 * U 00c3.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00C3() {
			return getToken(EclParser.U_00C3, 0);
		}

		/**
		 * U 00c4.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00C4() {
			return getToken(EclParser.U_00C4, 0);
		}

		/**
		 * U 00c5.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00C5() {
			return getToken(EclParser.U_00C5, 0);
		}

		/**
		 * U 00c6.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00C6() {
			return getToken(EclParser.U_00C6, 0);
		}

		/**
		 * U 00c7.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00C7() {
			return getToken(EclParser.U_00C7, 0);
		}

		/**
		 * U 00c8.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00C8() {
			return getToken(EclParser.U_00C8, 0);
		}

		/**
		 * U 00c9.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00C9() {
			return getToken(EclParser.U_00C9, 0);
		}

		/**
		 * U 00ca.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00CA() {
			return getToken(EclParser.U_00CA, 0);
		}

		/**
		 * U 00cb.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00CB() {
			return getToken(EclParser.U_00CB, 0);
		}

		/**
		 * U 00cc.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00CC() {
			return getToken(EclParser.U_00CC, 0);
		}

		/**
		 * U 00cd.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00CD() {
			return getToken(EclParser.U_00CD, 0);
		}

		/**
		 * U 00ce.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00CE() {
			return getToken(EclParser.U_00CE, 0);
		}

		/**
		 * U 00cf.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00CF() {
			return getToken(EclParser.U_00CF, 0);
		}

		/**
		 * U 00d0.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00D0() {
			return getToken(EclParser.U_00D0, 0);
		}

		/**
		 * U 00d1.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00D1() {
			return getToken(EclParser.U_00D1, 0);
		}

		/**
		 * U 00d2.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00D2() {
			return getToken(EclParser.U_00D2, 0);
		}

		/**
		 * U 00d3.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00D3() {
			return getToken(EclParser.U_00D3, 0);
		}

		/**
		 * U 00d4.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00D4() {
			return getToken(EclParser.U_00D4, 0);
		}

		/**
		 * U 00d5.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00D5() {
			return getToken(EclParser.U_00D5, 0);
		}

		/**
		 * U 00d6.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00D6() {
			return getToken(EclParser.U_00D6, 0);
		}

		/**
		 * U 00d7.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00D7() {
			return getToken(EclParser.U_00D7, 0);
		}

		/**
		 * U 00d8.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00D8() {
			return getToken(EclParser.U_00D8, 0);
		}

		/**
		 * U 00d9.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00D9() {
			return getToken(EclParser.U_00D9, 0);
		}

		/**
		 * U 00da.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00DA() {
			return getToken(EclParser.U_00DA, 0);
		}

		/**
		 * U 00db.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00DB() {
			return getToken(EclParser.U_00DB, 0);
		}

		/**
		 * U 00dc.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00DC() {
			return getToken(EclParser.U_00DC, 0);
		}

		/**
		 * U 00dd.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00DD() {
			return getToken(EclParser.U_00DD, 0);
		}

		/**
		 * U 00de.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00DE() {
			return getToken(EclParser.U_00DE, 0);
		}

		/**
		 * U 00df.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00DF() {
			return getToken(EclParser.U_00DF, 0);
		}

		/**
		 * Instantiates a {@link Utf8_2Context} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public Utf8_2Context(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_utf8_2;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterUtf8_2(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitUtf8_2(this);
			}
		}
	}

	/**
	 * Utf 8 2.
	 *
	 * @return the utf 8 2 context
	 * @throws RecognitionException the recognition exception
	 */
	public final Utf8_2Context utf8_2() throws RecognitionException {
		final Utf8_2Context _localctx = new Utf8_2Context(_ctx, getState());
		enterRule(_localctx, 136, RULE_utf8_2);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(691);
				_la = _input.LA(1);
				if (!(((((_la - 163)) & ~0x3f) == 0 && ((1L << (_la - 163))
						& ((1L << (U_00C2 - 163)) | (1L << (U_00C3 - 163)) | (1L << (U_00C4 - 163))
								| (1L << (U_00C5 - 163)) | (1L << (U_00C6 - 163)) | (1L << (U_00C7 - 163))
								| (1L << (U_00C8 - 163)) | (1L << (U_00C9 - 163)) | (1L << (U_00CA - 163))
								| (1L << (U_00CB - 163)) | (1L << (U_00CC - 163)) | (1L << (U_00CD - 163))
								| (1L << (U_00CE - 163)) | (1L << (U_00CF - 163)) | (1L << (U_00D0 - 163))
								| (1L << (U_00D1 - 163)) | (1L << (U_00D2 - 163)) | (1L << (U_00D3 - 163))
								| (1L << (U_00D4 - 163)) | (1L << (U_00D5 - 163)) | (1L << (U_00D6 - 163))
								| (1L << (U_00D7 - 163)) | (1L << (U_00D8 - 163)) | (1L << (U_00D9 - 163))
								| (1L << (U_00DA - 163)) | (1L << (U_00DB - 163)) | (1L << (U_00DC - 163))
								| (1L << (U_00DD - 163)) | (1L << (U_00DE - 163)) | (1L << (U_00DF - 163)))) != 0))) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
				setState(692);
				utf8_tail();
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class Utf8_3Context.
	 */
	public static class Utf8_3Context extends ParserRuleContext {

		/**
		 * U 00e0.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00E0() {
			return getToken(EclParser.U_00E0, 0);
		}

		/**
		 * Utf 8 tail.
		 *
		 * @return the list
		 */
		public List<Utf8_tailContext> utf8_tail() {
			return getRuleContexts(Utf8_tailContext.class);
		}

		/**
		 * Utf 8 tail.
		 *
		 * @param i the i
		 * @return the utf 8 tail context
		 */
		public Utf8_tailContext utf8_tail(final int i) {
			return getRuleContext(Utf8_tailContext.class, i);
		}

		/**
		 * U 00a0.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A0() {
			return getToken(EclParser.U_00A0, 0);
		}

		/**
		 * U 00a1.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A1() {
			return getToken(EclParser.U_00A1, 0);
		}

		/**
		 * U 00a2.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A2() {
			return getToken(EclParser.U_00A2, 0);
		}

		/**
		 * U 00a3.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A3() {
			return getToken(EclParser.U_00A3, 0);
		}

		/**
		 * U 00a4.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A4() {
			return getToken(EclParser.U_00A4, 0);
		}

		/**
		 * U 00a5.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A5() {
			return getToken(EclParser.U_00A5, 0);
		}

		/**
		 * U 00a6.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A6() {
			return getToken(EclParser.U_00A6, 0);
		}

		/**
		 * U 00a7.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A7() {
			return getToken(EclParser.U_00A7, 0);
		}

		/**
		 * U 00a8.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A8() {
			return getToken(EclParser.U_00A8, 0);
		}

		/**
		 * U 00a9.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A9() {
			return getToken(EclParser.U_00A9, 0);
		}

		/**
		 * U 00aa.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AA() {
			return getToken(EclParser.U_00AA, 0);
		}

		/**
		 * U 00ab.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AB() {
			return getToken(EclParser.U_00AB, 0);
		}

		/**
		 * U 00ac.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AC() {
			return getToken(EclParser.U_00AC, 0);
		}

		/**
		 * U 00ad.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AD() {
			return getToken(EclParser.U_00AD, 0);
		}

		/**
		 * U 00ae.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AE() {
			return getToken(EclParser.U_00AE, 0);
		}

		/**
		 * U 00af.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AF() {
			return getToken(EclParser.U_00AF, 0);
		}

		/**
		 * U 00b0.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B0() {
			return getToken(EclParser.U_00B0, 0);
		}

		/**
		 * U 00b1.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B1() {
			return getToken(EclParser.U_00B1, 0);
		}

		/**
		 * U 00b2.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B2() {
			return getToken(EclParser.U_00B2, 0);
		}

		/**
		 * U 00b3.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B3() {
			return getToken(EclParser.U_00B3, 0);
		}

		/**
		 * U 00b4.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B4() {
			return getToken(EclParser.U_00B4, 0);
		}

		/**
		 * U 00b5.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B5() {
			return getToken(EclParser.U_00B5, 0);
		}

		/**
		 * U 00b6.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B6() {
			return getToken(EclParser.U_00B6, 0);
		}

		/**
		 * U 00b7.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B7() {
			return getToken(EclParser.U_00B7, 0);
		}

		/**
		 * U 00b8.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B8() {
			return getToken(EclParser.U_00B8, 0);
		}

		/**
		 * U 00b9.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B9() {
			return getToken(EclParser.U_00B9, 0);
		}

		/**
		 * U 00ba.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BA() {
			return getToken(EclParser.U_00BA, 0);
		}

		/**
		 * U 00bb.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BB() {
			return getToken(EclParser.U_00BB, 0);
		}

		/**
		 * U 00bc.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BC() {
			return getToken(EclParser.U_00BC, 0);
		}

		/**
		 * U 00bd.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BD() {
			return getToken(EclParser.U_00BD, 0);
		}

		/**
		 * U 00be.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BE() {
			return getToken(EclParser.U_00BE, 0);
		}

		/**
		 * U 00bf.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BF() {
			return getToken(EclParser.U_00BF, 0);
		}

		/**
		 * U 00e1.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00E1() {
			return getToken(EclParser.U_00E1, 0);
		}

		/**
		 * U 00e2.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00E2() {
			return getToken(EclParser.U_00E2, 0);
		}

		/**
		 * U 00e3.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00E3() {
			return getToken(EclParser.U_00E3, 0);
		}

		/**
		 * U 00e4.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00E4() {
			return getToken(EclParser.U_00E4, 0);
		}

		/**
		 * U 00e5.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00E5() {
			return getToken(EclParser.U_00E5, 0);
		}

		/**
		 * U 00e6.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00E6() {
			return getToken(EclParser.U_00E6, 0);
		}

		/**
		 * U 00e7.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00E7() {
			return getToken(EclParser.U_00E7, 0);
		}

		/**
		 * U 00e8.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00E8() {
			return getToken(EclParser.U_00E8, 0);
		}

		/**
		 * U 00e9.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00E9() {
			return getToken(EclParser.U_00E9, 0);
		}

		/**
		 * U 00ea.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00EA() {
			return getToken(EclParser.U_00EA, 0);
		}

		/**
		 * U 00eb.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00EB() {
			return getToken(EclParser.U_00EB, 0);
		}

		/**
		 * U 00ec.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00EC() {
			return getToken(EclParser.U_00EC, 0);
		}

		/**
		 * U 00ed.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00ED() {
			return getToken(EclParser.U_00ED, 0);
		}

		/**
		 * U 0080.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0080() {
			return getToken(EclParser.U_0080, 0);
		}

		/**
		 * U 0081.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0081() {
			return getToken(EclParser.U_0081, 0);
		}

		/**
		 * U 0082.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0082() {
			return getToken(EclParser.U_0082, 0);
		}

		/**
		 * U 0083.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0083() {
			return getToken(EclParser.U_0083, 0);
		}

		/**
		 * U 0084.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0084() {
			return getToken(EclParser.U_0084, 0);
		}

		/**
		 * U 0085.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0085() {
			return getToken(EclParser.U_0085, 0);
		}

		/**
		 * U 0086.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0086() {
			return getToken(EclParser.U_0086, 0);
		}

		/**
		 * U 0087.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0087() {
			return getToken(EclParser.U_0087, 0);
		}

		/**
		 * U 0088.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0088() {
			return getToken(EclParser.U_0088, 0);
		}

		/**
		 * U 0089.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0089() {
			return getToken(EclParser.U_0089, 0);
		}

		/**
		 * U 008a.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008A() {
			return getToken(EclParser.U_008A, 0);
		}

		/**
		 * U 008b.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008B() {
			return getToken(EclParser.U_008B, 0);
		}

		/**
		 * U 008c.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008C() {
			return getToken(EclParser.U_008C, 0);
		}

		/**
		 * U 008d.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008D() {
			return getToken(EclParser.U_008D, 0);
		}

		/**
		 * U 008e.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008E() {
			return getToken(EclParser.U_008E, 0);
		}

		/**
		 * U 008f.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008F() {
			return getToken(EclParser.U_008F, 0);
		}

		/**
		 * U 0090.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0090() {
			return getToken(EclParser.U_0090, 0);
		}

		/**
		 * U 0091.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0091() {
			return getToken(EclParser.U_0091, 0);
		}

		/**
		 * U 0092.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0092() {
			return getToken(EclParser.U_0092, 0);
		}

		/**
		 * U 0093.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0093() {
			return getToken(EclParser.U_0093, 0);
		}

		/**
		 * U 0094.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0094() {
			return getToken(EclParser.U_0094, 0);
		}

		/**
		 * U 0095.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0095() {
			return getToken(EclParser.U_0095, 0);
		}

		/**
		 * U 0096.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0096() {
			return getToken(EclParser.U_0096, 0);
		}

		/**
		 * U 0097.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0097() {
			return getToken(EclParser.U_0097, 0);
		}

		/**
		 * U 0098.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0098() {
			return getToken(EclParser.U_0098, 0);
		}

		/**
		 * U 0099.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0099() {
			return getToken(EclParser.U_0099, 0);
		}

		/**
		 * U 009a.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009A() {
			return getToken(EclParser.U_009A, 0);
		}

		/**
		 * U 009b.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009B() {
			return getToken(EclParser.U_009B, 0);
		}

		/**
		 * U 009c.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009C() {
			return getToken(EclParser.U_009C, 0);
		}

		/**
		 * U 009d.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009D() {
			return getToken(EclParser.U_009D, 0);
		}

		/**
		 * U 009e.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009E() {
			return getToken(EclParser.U_009E, 0);
		}

		/**
		 * U 009f.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009F() {
			return getToken(EclParser.U_009F, 0);
		}

		/**
		 * U 00ee.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00EE() {
			return getToken(EclParser.U_00EE, 0);
		}

		/**
		 * U 00ef.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00EF() {
			return getToken(EclParser.U_00EF, 0);
		}

		/**
		 * Instantiates a {@link Utf8_3Context} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public Utf8_3Context(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_utf8_3;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterUtf8_3(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitUtf8_3(this);
			}
		}
	}

	/**
	 * Utf 8 3.
	 *
	 * @return the utf 8 3 context
	 * @throws RecognitionException the recognition exception
	 */
	public final Utf8_3Context utf8_3() throws RecognitionException {
		final Utf8_3Context _localctx = new Utf8_3Context(_ctx, getState());
		enterRule(_localctx, 138, RULE_utf8_3);
		int _la;
		try {
			setState(708);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case U_00E0:
				enterOuterAlt(_localctx, 1); {
				{
					setState(694);
					match(U_00E0);
					setState(695);
					_la = _input.LA(1);
					if (!(((((_la - 131)) & ~0x3f) == 0 && ((1L << (_la - 131))
							& ((1L << (U_00A0 - 131)) | (1L << (U_00A1 - 131)) | (1L << (U_00A2 - 131))
									| (1L << (U_00A3 - 131)) | (1L << (U_00A4 - 131)) | (1L << (U_00A5 - 131))
									| (1L << (U_00A6 - 131)) | (1L << (U_00A7 - 131)) | (1L << (U_00A8 - 131))
									| (1L << (U_00A9 - 131)) | (1L << (U_00AA - 131)) | (1L << (U_00AB - 131))
									| (1L << (U_00AC - 131)) | (1L << (U_00AD - 131)) | (1L << (U_00AE - 131))
									| (1L << (U_00AF - 131)) | (1L << (U_00B0 - 131)) | (1L << (U_00B1 - 131))
									| (1L << (U_00B2 - 131)) | (1L << (U_00B3 - 131)) | (1L << (U_00B4 - 131))
									| (1L << (U_00B5 - 131)) | (1L << (U_00B6 - 131)) | (1L << (U_00B7 - 131))
									| (1L << (U_00B8 - 131)) | (1L << (U_00B9 - 131)) | (1L << (U_00BA - 131))
									| (1L << (U_00BB - 131)) | (1L << (U_00BC - 131)) | (1L << (U_00BD - 131))
									| (1L << (U_00BE - 131)) | (1L << (U_00BF - 131)))) != 0))) {
						_errHandler.recoverInline(this);
					} else {
						if (_input.LA(1) == Token.EOF) {
							matchedEOF = true;
						}
						_errHandler.reportMatch(this);
						consume();
					}
					setState(696);
					utf8_tail();
				}
			}
				break;
			case U_00E1:
			case U_00E2:
			case U_00E3:
			case U_00E4:
			case U_00E5:
			case U_00E6:
			case U_00E7:
			case U_00E8:
			case U_00E9:
			case U_00EA:
			case U_00EB:
			case U_00EC:
				enterOuterAlt(_localctx, 2); {
				{
					setState(697);
					_la = _input.LA(1);
					if (!(((((_la - 194)) & ~0x3f) == 0 && ((1L << (_la - 194)) & ((1L << (U_00E1 - 194))
							| (1L << (U_00E2 - 194)) | (1L << (U_00E3 - 194)) | (1L << (U_00E4 - 194))
							| (1L << (U_00E5 - 194)) | (1L << (U_00E6 - 194)) | (1L << (U_00E7 - 194))
							| (1L << (U_00E8 - 194)) | (1L << (U_00E9 - 194)) | (1L << (U_00EA - 194))
							| (1L << (U_00EB - 194)) | (1L << (U_00EC - 194)))) != 0))) {
						_errHandler.recoverInline(this);
					} else {
						if (_input.LA(1) == Token.EOF) {
							matchedEOF = true;
						}
						_errHandler.reportMatch(this);
						consume();
					}
					{
						setState(698);
						utf8_tail();
					}
					{
						setState(699);
						utf8_tail();
					}
				}
			}
				break;
			case U_00ED:
				enterOuterAlt(_localctx, 3); {
				{
					setState(701);
					match(U_00ED);
					setState(702);
					_la = _input.LA(1);
					if (!(((((_la - 99)) & ~0x3f) == 0
							&& ((1L << (_la - 99)) & ((1L << (U_0080 - 99)) | (1L << (U_0081 - 99))
									| (1L << (U_0082 - 99)) | (1L << (U_0083 - 99)) | (1L << (U_0084 - 99))
									| (1L << (U_0085 - 99)) | (1L << (U_0086 - 99)) | (1L << (U_0087 - 99))
									| (1L << (U_0088 - 99)) | (1L << (U_0089 - 99)) | (1L << (U_008A - 99))
									| (1L << (U_008B - 99)) | (1L << (U_008C - 99)) | (1L << (U_008D - 99))
									| (1L << (U_008E - 99)) | (1L << (U_008F - 99)) | (1L << (U_0090 - 99))
									| (1L << (U_0091 - 99)) | (1L << (U_0092 - 99)) | (1L << (U_0093 - 99))
									| (1L << (U_0094 - 99)) | (1L << (U_0095 - 99)) | (1L << (U_0096 - 99))
									| (1L << (U_0097 - 99)) | (1L << (U_0098 - 99)) | (1L << (U_0099 - 99))
									| (1L << (U_009A - 99)) | (1L << (U_009B - 99)) | (1L << (U_009C - 99))
									| (1L << (U_009D - 99)) | (1L << (U_009E - 99)) | (1L << (U_009F - 99)))) != 0))) {
						_errHandler.recoverInline(this);
					} else {
						if (_input.LA(1) == Token.EOF) {
							matchedEOF = true;
						}
						_errHandler.reportMatch(this);
						consume();
					}
					setState(703);
					utf8_tail();
				}
			}
				break;
			case U_00EE:
			case U_00EF:
				enterOuterAlt(_localctx, 4); {
				{
					setState(704);
					_la = _input.LA(1);
					if (!(_la == U_00EE || _la == U_00EF)) {
						_errHandler.recoverInline(this);
					} else {
						if (_input.LA(1) == Token.EOF) {
							matchedEOF = true;
						}
						_errHandler.reportMatch(this);
						consume();
					}
					{
						setState(705);
						utf8_tail();
					}
					{
						setState(706);
						utf8_tail();
					}
				}
			}
				break;
			default:
				throw new NoViableAltException(this);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class Utf8_4Context.
	 */
	public static class Utf8_4Context extends ParserRuleContext {

		/**
		 * U 00f0.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00F0() {
			return getToken(EclParser.U_00F0, 0);
		}

		/**
		 * U 0090.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0090() {
			return getToken(EclParser.U_0090, 0);
		}

		/**
		 * U 0091.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0091() {
			return getToken(EclParser.U_0091, 0);
		}

		/**
		 * U 0092.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0092() {
			return getToken(EclParser.U_0092, 0);
		}

		/**
		 * U 0093.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0093() {
			return getToken(EclParser.U_0093, 0);
		}

		/**
		 * U 0094.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0094() {
			return getToken(EclParser.U_0094, 0);
		}

		/**
		 * U 0095.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0095() {
			return getToken(EclParser.U_0095, 0);
		}

		/**
		 * U 0096.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0096() {
			return getToken(EclParser.U_0096, 0);
		}

		/**
		 * U 0097.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0097() {
			return getToken(EclParser.U_0097, 0);
		}

		/**
		 * U 0098.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0098() {
			return getToken(EclParser.U_0098, 0);
		}

		/**
		 * U 0099.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0099() {
			return getToken(EclParser.U_0099, 0);
		}

		/**
		 * U 009a.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009A() {
			return getToken(EclParser.U_009A, 0);
		}

		/**
		 * U 009b.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009B() {
			return getToken(EclParser.U_009B, 0);
		}

		/**
		 * U 009c.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009C() {
			return getToken(EclParser.U_009C, 0);
		}

		/**
		 * U 009d.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009D() {
			return getToken(EclParser.U_009D, 0);
		}

		/**
		 * U 009e.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009E() {
			return getToken(EclParser.U_009E, 0);
		}

		/**
		 * U 009f.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009F() {
			return getToken(EclParser.U_009F, 0);
		}

		/**
		 * U 00a0.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A0() {
			return getToken(EclParser.U_00A0, 0);
		}

		/**
		 * U 00a1.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A1() {
			return getToken(EclParser.U_00A1, 0);
		}

		/**
		 * U 00a2.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A2() {
			return getToken(EclParser.U_00A2, 0);
		}

		/**
		 * U 00a3.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A3() {
			return getToken(EclParser.U_00A3, 0);
		}

		/**
		 * U 00a4.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A4() {
			return getToken(EclParser.U_00A4, 0);
		}

		/**
		 * U 00a5.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A5() {
			return getToken(EclParser.U_00A5, 0);
		}

		/**
		 * U 00a6.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A6() {
			return getToken(EclParser.U_00A6, 0);
		}

		/**
		 * U 00a7.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A7() {
			return getToken(EclParser.U_00A7, 0);
		}

		/**
		 * U 00a8.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A8() {
			return getToken(EclParser.U_00A8, 0);
		}

		/**
		 * U 00a9.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A9() {
			return getToken(EclParser.U_00A9, 0);
		}

		/**
		 * U 00aa.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AA() {
			return getToken(EclParser.U_00AA, 0);
		}

		/**
		 * U 00ab.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AB() {
			return getToken(EclParser.U_00AB, 0);
		}

		/**
		 * U 00ac.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AC() {
			return getToken(EclParser.U_00AC, 0);
		}

		/**
		 * U 00ad.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AD() {
			return getToken(EclParser.U_00AD, 0);
		}

		/**
		 * U 00ae.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AE() {
			return getToken(EclParser.U_00AE, 0);
		}

		/**
		 * U 00af.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AF() {
			return getToken(EclParser.U_00AF, 0);
		}

		/**
		 * U 00b0.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B0() {
			return getToken(EclParser.U_00B0, 0);
		}

		/**
		 * U 00b1.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B1() {
			return getToken(EclParser.U_00B1, 0);
		}

		/**
		 * U 00b2.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B2() {
			return getToken(EclParser.U_00B2, 0);
		}

		/**
		 * U 00b3.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B3() {
			return getToken(EclParser.U_00B3, 0);
		}

		/**
		 * U 00b4.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B4() {
			return getToken(EclParser.U_00B4, 0);
		}

		/**
		 * U 00b5.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B5() {
			return getToken(EclParser.U_00B5, 0);
		}

		/**
		 * U 00b6.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B6() {
			return getToken(EclParser.U_00B6, 0);
		}

		/**
		 * U 00b7.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B7() {
			return getToken(EclParser.U_00B7, 0);
		}

		/**
		 * U 00b8.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B8() {
			return getToken(EclParser.U_00B8, 0);
		}

		/**
		 * U 00b9.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B9() {
			return getToken(EclParser.U_00B9, 0);
		}

		/**
		 * U 00ba.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BA() {
			return getToken(EclParser.U_00BA, 0);
		}

		/**
		 * U 00bb.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BB() {
			return getToken(EclParser.U_00BB, 0);
		}

		/**
		 * U 00bc.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BC() {
			return getToken(EclParser.U_00BC, 0);
		}

		/**
		 * U 00bd.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BD() {
			return getToken(EclParser.U_00BD, 0);
		}

		/**
		 * U 00be.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BE() {
			return getToken(EclParser.U_00BE, 0);
		}

		/**
		 * U 00bf.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BF() {
			return getToken(EclParser.U_00BF, 0);
		}

		/**
		 * Utf 8 tail.
		 *
		 * @return the list
		 */
		public List<Utf8_tailContext> utf8_tail() {
			return getRuleContexts(Utf8_tailContext.class);
		}

		/**
		 * Utf 8 tail.
		 *
		 * @param i the i
		 * @return the utf 8 tail context
		 */
		public Utf8_tailContext utf8_tail(final int i) {
			return getRuleContext(Utf8_tailContext.class, i);
		}

		/**
		 * U 00f1.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00F1() {
			return getToken(EclParser.U_00F1, 0);
		}

		/**
		 * U 00f2.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00F2() {
			return getToken(EclParser.U_00F2, 0);
		}

		/**
		 * U 00f3.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00F3() {
			return getToken(EclParser.U_00F3, 0);
		}

		/**
		 * U 00f4.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00F4() {
			return getToken(EclParser.U_00F4, 0);
		}

		/**
		 * U 0080.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0080() {
			return getToken(EclParser.U_0080, 0);
		}

		/**
		 * U 0081.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0081() {
			return getToken(EclParser.U_0081, 0);
		}

		/**
		 * U 0082.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0082() {
			return getToken(EclParser.U_0082, 0);
		}

		/**
		 * U 0083.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0083() {
			return getToken(EclParser.U_0083, 0);
		}

		/**
		 * U 0084.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0084() {
			return getToken(EclParser.U_0084, 0);
		}

		/**
		 * U 0085.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0085() {
			return getToken(EclParser.U_0085, 0);
		}

		/**
		 * U 0086.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0086() {
			return getToken(EclParser.U_0086, 0);
		}

		/**
		 * U 0087.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0087() {
			return getToken(EclParser.U_0087, 0);
		}

		/**
		 * U 0088.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0088() {
			return getToken(EclParser.U_0088, 0);
		}

		/**
		 * U 0089.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0089() {
			return getToken(EclParser.U_0089, 0);
		}

		/**
		 * U 008a.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008A() {
			return getToken(EclParser.U_008A, 0);
		}

		/**
		 * U 008b.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008B() {
			return getToken(EclParser.U_008B, 0);
		}

		/**
		 * U 008c.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008C() {
			return getToken(EclParser.U_008C, 0);
		}

		/**
		 * U 008d.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008D() {
			return getToken(EclParser.U_008D, 0);
		}

		/**
		 * U 008e.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008E() {
			return getToken(EclParser.U_008E, 0);
		}

		/**
		 * U 008f.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008F() {
			return getToken(EclParser.U_008F, 0);
		}

		/**
		 * Instantiates a {@link Utf8_4Context} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public Utf8_4Context(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_utf8_4;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterUtf8_4(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitUtf8_4(this);
			}
		}
	}

	/**
	 * Utf 8 4.
	 *
	 * @return the utf 8 4 context
	 * @throws RecognitionException the recognition exception
	 */
	public final Utf8_4Context utf8_4() throws RecognitionException {
		final Utf8_4Context _localctx = new Utf8_4Context(_ctx, getState());
		enterRule(_localctx, 140, RULE_utf8_4);
		int _la;
		try {
			setState(725);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case U_00F0:
				enterOuterAlt(_localctx, 1); {
				{
					setState(710);
					match(U_00F0);
					setState(711);
					_la = _input.LA(1);
					if (!(((((_la - 115)) & ~0x3f) == 0 && ((1L << (_la - 115)) & ((1L << (U_0090 - 115))
							| (1L << (U_0091 - 115)) | (1L << (U_0092 - 115)) | (1L << (U_0093 - 115))
							| (1L << (U_0094 - 115)) | (1L << (U_0095 - 115)) | (1L << (U_0096 - 115))
							| (1L << (U_0097 - 115)) | (1L << (U_0098 - 115)) | (1L << (U_0099 - 115))
							| (1L << (U_009A - 115)) | (1L << (U_009B - 115)) | (1L << (U_009C - 115))
							| (1L << (U_009D - 115)) | (1L << (U_009E - 115)) | (1L << (U_009F - 115))
							| (1L << (U_00A0 - 115)) | (1L << (U_00A1 - 115)) | (1L << (U_00A2 - 115))
							| (1L << (U_00A3 - 115)) | (1L << (U_00A4 - 115)) | (1L << (U_00A5 - 115))
							| (1L << (U_00A6 - 115)) | (1L << (U_00A7 - 115)) | (1L << (U_00A8 - 115))
							| (1L << (U_00A9 - 115)) | (1L << (U_00AA - 115)) | (1L << (U_00AB - 115))
							| (1L << (U_00AC - 115)) | (1L << (U_00AD - 115)) | (1L << (U_00AE - 115))
							| (1L << (U_00AF - 115)) | (1L << (U_00B0 - 115)) | (1L << (U_00B1 - 115))
							| (1L << (U_00B2 - 115)) | (1L << (U_00B3 - 115)) | (1L << (U_00B4 - 115))
							| (1L << (U_00B5 - 115)) | (1L << (U_00B6 - 115)) | (1L << (U_00B7 - 115))
							| (1L << (U_00B8 - 115)) | (1L << (U_00B9 - 115)) | (1L << (U_00BA - 115))
							| (1L << (U_00BB - 115)) | (1L << (U_00BC - 115)) | (1L << (U_00BD - 115))
							| (1L << (U_00BE - 115)) | (1L << (U_00BF - 115)))) != 0))) {
						_errHandler.recoverInline(this);
					} else {
						if (_input.LA(1) == Token.EOF) {
							matchedEOF = true;
						}
						_errHandler.reportMatch(this);
						consume();
					}
					{
						setState(712);
						utf8_tail();
					}
					{
						setState(713);
						utf8_tail();
					}
				}
			}
				break;
			case U_00F1:
			case U_00F2:
			case U_00F3:
				enterOuterAlt(_localctx, 2); {
				{
					setState(715);
					_la = _input.LA(1);
					if (!(((((_la - 210)) & ~0x3f) == 0 && ((1L << (_la - 210))
							& ((1L << (U_00F1 - 210)) | (1L << (U_00F2 - 210)) | (1L << (U_00F3 - 210)))) != 0))) {
						_errHandler.recoverInline(this);
					} else {
						if (_input.LA(1) == Token.EOF) {
							matchedEOF = true;
						}
						_errHandler.reportMatch(this);
						consume();
					}
					{
						setState(716);
						utf8_tail();
					}
					{
						setState(717);
						utf8_tail();
					}
					{
						setState(718);
						utf8_tail();
					}
				}
			}
				break;
			case U_00F4:
				enterOuterAlt(_localctx, 3); {
				{
					setState(720);
					match(U_00F4);
					setState(721);
					_la = _input.LA(1);
					if (!(((((_la - 99)) & ~0x3f) == 0 && ((1L << (_la - 99)) & ((1L << (U_0080 - 99))
							| (1L << (U_0081 - 99)) | (1L << (U_0082 - 99)) | (1L << (U_0083 - 99))
							| (1L << (U_0084 - 99)) | (1L << (U_0085 - 99)) | (1L << (U_0086 - 99))
							| (1L << (U_0087 - 99)) | (1L << (U_0088 - 99)) | (1L << (U_0089 - 99))
							| (1L << (U_008A - 99)) | (1L << (U_008B - 99)) | (1L << (U_008C - 99))
							| (1L << (U_008D - 99)) | (1L << (U_008E - 99)) | (1L << (U_008F - 99)))) != 0))) {
						_errHandler.recoverInline(this);
					} else {
						if (_input.LA(1) == Token.EOF) {
							matchedEOF = true;
						}
						_errHandler.reportMatch(this);
						consume();
					}
					{
						setState(722);
						utf8_tail();
					}
					{
						setState(723);
						utf8_tail();
					}
				}
			}
				break;
			default:
				throw new NoViableAltException(this);
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/**
	 * The Class Utf8_tailContext.
	 */
	public static class Utf8_tailContext extends ParserRuleContext {

		/**
		 * U 0080.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0080() {
			return getToken(EclParser.U_0080, 0);
		}

		/**
		 * U 0081.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0081() {
			return getToken(EclParser.U_0081, 0);
		}

		/**
		 * U 0082.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0082() {
			return getToken(EclParser.U_0082, 0);
		}

		/**
		 * U 0083.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0083() {
			return getToken(EclParser.U_0083, 0);
		}

		/**
		 * U 0084.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0084() {
			return getToken(EclParser.U_0084, 0);
		}

		/**
		 * U 0085.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0085() {
			return getToken(EclParser.U_0085, 0);
		}

		/**
		 * U 0086.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0086() {
			return getToken(EclParser.U_0086, 0);
		}

		/**
		 * U 0087.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0087() {
			return getToken(EclParser.U_0087, 0);
		}

		/**
		 * U 0088.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0088() {
			return getToken(EclParser.U_0088, 0);
		}

		/**
		 * U 0089.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0089() {
			return getToken(EclParser.U_0089, 0);
		}

		/**
		 * U 008a.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008A() {
			return getToken(EclParser.U_008A, 0);
		}

		/**
		 * U 008b.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008B() {
			return getToken(EclParser.U_008B, 0);
		}

		/**
		 * U 008c.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008C() {
			return getToken(EclParser.U_008C, 0);
		}

		/**
		 * U 008d.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008D() {
			return getToken(EclParser.U_008D, 0);
		}

		/**
		 * U 008e.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008E() {
			return getToken(EclParser.U_008E, 0);
		}

		/**
		 * U 008f.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_008F() {
			return getToken(EclParser.U_008F, 0);
		}

		/**
		 * U 0090.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0090() {
			return getToken(EclParser.U_0090, 0);
		}

		/**
		 * U 0091.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0091() {
			return getToken(EclParser.U_0091, 0);
		}

		/**
		 * U 0092.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0092() {
			return getToken(EclParser.U_0092, 0);
		}

		/**
		 * U 0093.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0093() {
			return getToken(EclParser.U_0093, 0);
		}

		/**
		 * U 0094.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0094() {
			return getToken(EclParser.U_0094, 0);
		}

		/**
		 * U 0095.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0095() {
			return getToken(EclParser.U_0095, 0);
		}

		/**
		 * U 0096.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0096() {
			return getToken(EclParser.U_0096, 0);
		}

		/**
		 * U 0097.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0097() {
			return getToken(EclParser.U_0097, 0);
		}

		/**
		 * U 0098.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0098() {
			return getToken(EclParser.U_0098, 0);
		}

		/**
		 * U 0099.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_0099() {
			return getToken(EclParser.U_0099, 0);
		}

		/**
		 * U 009a.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009A() {
			return getToken(EclParser.U_009A, 0);
		}

		/**
		 * U 009b.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009B() {
			return getToken(EclParser.U_009B, 0);
		}

		/**
		 * U 009c.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009C() {
			return getToken(EclParser.U_009C, 0);
		}

		/**
		 * U 009d.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009D() {
			return getToken(EclParser.U_009D, 0);
		}

		/**
		 * U 009e.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009E() {
			return getToken(EclParser.U_009E, 0);
		}

		/**
		 * U 009f.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_009F() {
			return getToken(EclParser.U_009F, 0);
		}

		/**
		 * U 00a0.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A0() {
			return getToken(EclParser.U_00A0, 0);
		}

		/**
		 * U 00a1.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A1() {
			return getToken(EclParser.U_00A1, 0);
		}

		/**
		 * U 00a2.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A2() {
			return getToken(EclParser.U_00A2, 0);
		}

		/**
		 * U 00a3.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A3() {
			return getToken(EclParser.U_00A3, 0);
		}

		/**
		 * U 00a4.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A4() {
			return getToken(EclParser.U_00A4, 0);
		}

		/**
		 * U 00a5.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A5() {
			return getToken(EclParser.U_00A5, 0);
		}

		/**
		 * U 00a6.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A6() {
			return getToken(EclParser.U_00A6, 0);
		}

		/**
		 * U 00a7.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A7() {
			return getToken(EclParser.U_00A7, 0);
		}

		/**
		 * U 00a8.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A8() {
			return getToken(EclParser.U_00A8, 0);
		}

		/**
		 * U 00a9.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00A9() {
			return getToken(EclParser.U_00A9, 0);
		}

		/**
		 * U 00aa.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AA() {
			return getToken(EclParser.U_00AA, 0);
		}

		/**
		 * U 00ab.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AB() {
			return getToken(EclParser.U_00AB, 0);
		}

		/**
		 * U 00ac.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AC() {
			return getToken(EclParser.U_00AC, 0);
		}

		/**
		 * U 00ad.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AD() {
			return getToken(EclParser.U_00AD, 0);
		}

		/**
		 * U 00ae.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AE() {
			return getToken(EclParser.U_00AE, 0);
		}

		/**
		 * U 00af.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00AF() {
			return getToken(EclParser.U_00AF, 0);
		}

		/**
		 * U 00b0.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B0() {
			return getToken(EclParser.U_00B0, 0);
		}

		/**
		 * U 00b1.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B1() {
			return getToken(EclParser.U_00B1, 0);
		}

		/**
		 * U 00b2.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B2() {
			return getToken(EclParser.U_00B2, 0);
		}

		/**
		 * U 00b3.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B3() {
			return getToken(EclParser.U_00B3, 0);
		}

		/**
		 * U 00b4.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B4() {
			return getToken(EclParser.U_00B4, 0);
		}

		/**
		 * U 00b5.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B5() {
			return getToken(EclParser.U_00B5, 0);
		}

		/**
		 * U 00b6.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B6() {
			return getToken(EclParser.U_00B6, 0);
		}

		/**
		 * U 00b7.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B7() {
			return getToken(EclParser.U_00B7, 0);
		}

		/**
		 * U 00b8.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B8() {
			return getToken(EclParser.U_00B8, 0);
		}

		/**
		 * U 00b9.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00B9() {
			return getToken(EclParser.U_00B9, 0);
		}

		/**
		 * U 00ba.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BA() {
			return getToken(EclParser.U_00BA, 0);
		}

		/**
		 * U 00bb.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BB() {
			return getToken(EclParser.U_00BB, 0);
		}

		/**
		 * U 00bc.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BC() {
			return getToken(EclParser.U_00BC, 0);
		}

		/**
		 * U 00bd.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BD() {
			return getToken(EclParser.U_00BD, 0);
		}

		/**
		 * U 00be.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BE() {
			return getToken(EclParser.U_00BE, 0);
		}

		/**
		 * U 00bf.
		 *
		 * @return the terminal node
		 */
		public TerminalNode U_00BF() {
			return getToken(EclParser.U_00BF, 0);
		}

		/**
		 * Instantiates a {@link Utf8_tailContext} from the specified parameters.
		 *
		 * @param parent        the parent
		 * @param invokingState the invoking state
		 */
		public Utf8_tailContext(final ParserRuleContext parent, final int invokingState) {
			super(parent, invokingState);
		}

		/* see superclass */
		@Override
		public int getRuleIndex() {
			return RULE_utf8_tail;
		}

		/* see superclass */
		@Override
		public void enterRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).enterUtf8_tail(this);
			}
		}

		/* see superclass */
		@Override
		public void exitRule(final ParseTreeListener listener) {
			if (listener instanceof EclListener) {
				((EclListener) listener).exitUtf8_tail(this);
			}
		}
	}

	/**
	 * Utf 8 tail.
	 *
	 * @return the utf 8 tail context
	 * @throws RecognitionException the recognition exception
	 */
	public final Utf8_tailContext utf8_tail() throws RecognitionException {
		final Utf8_tailContext _localctx = new Utf8_tailContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_utf8_tail);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(727);
				_la = _input.LA(1);
				if (!(((((_la - 99)) & ~0x3f) == 0 && ((1L << (_la - 99)) & ((1L << (U_0080 - 99))
						| (1L << (U_0081 - 99)) | (1L << (U_0082 - 99)) | (1L << (U_0083 - 99)) | (1L << (U_0084 - 99))
						| (1L << (U_0085 - 99)) | (1L << (U_0086 - 99)) | (1L << (U_0087 - 99)) | (1L << (U_0088 - 99))
						| (1L << (U_0089 - 99)) | (1L << (U_008A - 99)) | (1L << (U_008B - 99)) | (1L << (U_008C - 99))
						| (1L << (U_008D - 99)) | (1L << (U_008E - 99)) | (1L << (U_008F - 99)) | (1L << (U_0090 - 99))
						| (1L << (U_0091 - 99)) | (1L << (U_0092 - 99)) | (1L << (U_0093 - 99)) | (1L << (U_0094 - 99))
						| (1L << (U_0095 - 99)) | (1L << (U_0096 - 99)) | (1L << (U_0097 - 99)) | (1L << (U_0098 - 99))
						| (1L << (U_0099 - 99)) | (1L << (U_009A - 99)) | (1L << (U_009B - 99)) | (1L << (U_009C - 99))
						| (1L << (U_009D - 99)) | (1L << (U_009E - 99)) | (1L << (U_009F - 99)) | (1L << (U_00A0 - 99))
						| (1L << (U_00A1 - 99)) | (1L << (U_00A2 - 99)) | (1L << (U_00A3 - 99)) | (1L << (U_00A4 - 99))
						| (1L << (U_00A5 - 99)) | (1L << (U_00A6 - 99)) | (1L << (U_00A7 - 99)) | (1L << (U_00A8 - 99))
						| (1L << (U_00A9 - 99)) | (1L << (U_00AA - 99)) | (1L << (U_00AB - 99)) | (1L << (U_00AC - 99))
						| (1L << (U_00AD - 99)) | (1L << (U_00AE - 99)) | (1L << (U_00AF - 99)) | (1L << (U_00B0 - 99))
						| (1L << (U_00B1 - 99)) | (1L << (U_00B2 - 99)) | (1L << (U_00B3 - 99)) | (1L << (U_00B4 - 99))
						| (1L << (U_00B5 - 99)) | (1L << (U_00B6 - 99)) | (1L << (U_00B7 - 99)) | (1L << (U_00B8 - 99))
						| (1L << (U_00B9 - 99)) | (1L << (U_00BA - 99)) | (1L << (U_00BB - 99)) | (1L << (U_00BC - 99))
						| (1L << (U_00BD - 99)) | (1L << (U_00BE - 99)) | (1L << (U_00BF - 99)))) != 0))) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}
					_errHandler.reportMatch(this);
					consume();
				}
			}
		} catch (final RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	/** The Constant _serializedATN. */
	public static final String _serializedATN = "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u00d7\u02dc\4\2\t"
			+ "\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"
			+ "\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"
			+ "\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"
			+ "\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"
			+ "\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"
			+ ",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"
			+ "\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="
			+ "\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"
			+ "\tI\3\2\3\2\3\2\3\2\5\2\u0097\n\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\4"
			+ "\3\4\3\4\5\4\u00a4\n\4\3\5\3\5\3\5\3\5\3\5\3\5\6\5\u00ac\n\5\r\5\16\5"
			+ "\u00ad\3\6\3\6\3\6\3\6\3\6\3\6\6\6\u00b6\n\6\r\6\16\6\u00b7\3\7\3\7\3"
			+ "\7\3\7\3\7\3\7\3\b\3\b\3\b\5\b\u00c3\n\b\3\b\3\b\3\b\5\b\u00c8\n\b\3\b"
			+ "\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u00d1\n\b\3\t\3\t\5\t\u00d5\n\t\3\n\3\n\3"
			+ "\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u00e1\n\13\3\f\3\f\3\r\6\r"
			+ "\u00e6\n\r\r\r\16\r\u00e7\3\r\6\r\u00eb\n\r\r\r\16\r\u00ec\3\r\6\r\u00f0"
			+ "\n\r\r\r\16\r\u00f1\7\r\u00f4\n\r\f\r\16\r\u00f7\13\r\3\16\3\16\3\17\3"
			+ "\17\3\17\3\17\3\17\3\17\5\17\u0101\n\17\3\20\3\20\3\21\3\21\3\21\3\22"
			+ "\3\22\3\22\3\23\3\23\3\24\3\24\3\24\3\25\3\25\3\25\3\26\3\26\3\26\3\26"
			+ "\3\26\5\26\u0118\n\26\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\30"
			+ "\3\30\3\31\3\31\3\31\3\31\5\31\u0129\n\31\3\32\3\32\3\32\3\32\3\32\6\32"
			+ "\u0130\n\32\r\32\16\32\u0131\3\33\3\33\3\33\3\33\3\33\6\33\u0139\n\33"
			+ "\r\33\16\33\u013a\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\5\34\u0145\n"
			+ "\34\3\35\3\35\3\35\3\35\5\35\u014b\n\35\3\36\3\36\3\36\3\36\3\36\6\36"
			+ "\u0152\n\36\r\36\16\36\u0153\3\37\3\37\3\37\3\37\3\37\6\37\u015b\n\37"
			+ "\r\37\16\37\u015c\3 \3 \3 \3 \3 \3 \3 \5 \u0166\n \3!\3!\3!\3!\3!\5!\u016d"
			+ "\n!\3!\3!\3!\3!\3!\3!\3\"\3\"\3\"\3\"\3\"\5\"\u017a\n\"\3\"\3\"\3\"\5"
			+ "\"\u017f\n\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3"
			+ "\"\3\"\3\"\5\"\u0192\n\"\3#\3#\3#\3#\3$\3$\3%\3%\3%\3&\3&\5&\u019f\n&"
			+ "\3\'\3\'\3(\3(\3)\3)\3*\3*\3*\5*\u01aa\n*\3+\3+\3+\3+\3+\3+\3+\3+\3+\5"
			+ "+\u01b5\n+\3,\3,\3,\5,\u01ba\n,\3-\5-\u01bd\n-\3-\3-\5-\u01c1\n-\3.\3"
			+ ".\6.\u01c5\n.\r.\16.\u01c6\3/\3/\7/\u01cb\n/\f/\16/\u01ce\13/\3/\5/\u01d1"
			+ "\n/\3\60\3\60\3\60\6\60\u01d6\n\60\r\60\16\60\u01d7\3\61\3\61\7\61\u01dc"
			+ "\n\61\f\61\16\61\u01df\13\61\3\61\5\61\u01e2\n\61\3\62\3\62\3\62\3\62"
			+ "\3\62\3\62\3\62\5\62\u01eb\n\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62"
			+ "\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62"
			+ "\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62"
			+ "\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62"
			+ "\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62"
			+ "\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62"
			+ "\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\62\5\62\u0245\n\62\3\62"
			+ "\6\62\u0248\n\62\r\62\16\62\u0249\5\62\u024c\n\62\3\63\3\63\3\63\3\63"
			+ "\3\63\7\63\u0253\n\63\f\63\16\63\u0256\13\63\3\64\3\64\3\64\3\64\3\64"
			+ "\6\64\u025d\n\64\r\64\16\64\u025e\3\65\3\65\3\65\3\65\3\65\7\65\u0266"
			+ "\n\65\f\65\16\65\u0269\13\65\3\65\3\65\3\65\3\66\3\66\3\66\3\66\3\66\3"
			+ "\66\3\66\3\66\3\66\5\66\u0277\n\66\3\67\3\67\3\67\38\38\38\38\38\38\3"
			+ "8\38\38\58\u0285\n8\39\39\3:\3:\3;\3;\3<\3<\3=\3=\3>\3>\3?\3?\3@\3@\3"
			+ "A\3A\3B\3B\3B\3B\3B\5B\u029e\nB\3C\3C\3C\3C\3C\3C\3C\3C\3C\3C\5C\u02aa"
			+ "\nC\3D\3D\3E\3E\3E\3E\3E\3E\5E\u02b4\nE\3F\3F\3F\3G\3G\3G\3G\3G\3G\3G"
			+ "\3G\3G\3G\3G\3G\3G\3G\5G\u02c7\nG\3H\3H\3H\3H\3H\3H\3H\3H\3H\3H\3H\3H"
			+ "\3H\3H\3H\5H\u02d8\nH\3I\3I\3I\2\2J\2\4\6\b\n\f\16\20\22\24\26\30\32\34"
			+ "\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082"
			+ "\u0084\u0086\u0088\u008a\u008c\u008e\u0090\2!\4\2\'\'GG\4\2\64\64TT\4"
			+ "\2**JJ\4\2\65\65UU\4\288XX\4\2\63\63SS\4\2//OO\4\2;;[[\4\299YY\4\2\21"
			+ "\21\23\23\3\2\7\17\3\2\21d\3\2\7\24\3\2\26d\3\2\26\37\3\2\27\37\3\2\7"
			+ "a\3\2cd\3\2\6\7\3\2\tA\3\2Cd\b\2\20\21\23\24\26 &@DEG`\3\2\u00a5\u00c2"
			+ "\3\2\u0085\u00a4\3\2\u00c4\u00cf\3\2e\u0084\3\2\u00d1\u00d2\3\2u\u00a4"
			+ "\3\2\u00d4\u00d6\3\2et\3\2e\u00a4\2\u0306\2\u0092\3\2\2\2\4\u009a\3\2"
			+ "\2\2\6\u00a3\3\2\2\2\b\u00a5\3\2\2\2\n\u00af\3\2\2\2\f\u00b9\3\2\2\2\16"
			+ "\u00c2\3\2\2\2\20\u00d4\3\2\2\2\22\u00d6\3\2\2\2\24\u00d8\3\2\2\2\26\u00e2"
			+ "\3\2\2\2\30\u00e5\3\2\2\2\32\u00f8\3\2\2\2\34\u0100\3\2\2\2\36\u0102\3"
			+ "\2\2\2 \u0104\3\2\2\2\"\u0107\3\2\2\2$\u010a\3\2\2\2&\u010c\3\2\2\2(\u010f"
			+ "\3\2\2\2*\u0117\3\2\2\2,\u0119\3\2\2\2.\u011d\3\2\2\2\60\u0124\3\2\2\2"
			+ "\62\u012f\3\2\2\2\64\u0138\3\2\2\2\66\u0144\3\2\2\28\u0146\3\2\2\2:\u0151"
			+ "\3\2\2\2<\u015a\3\2\2\2>\u0165\3\2\2\2@\u016c\3\2\2\2B\u0179\3\2\2\2D"
			+ "\u0193\3\2\2\2F\u0197\3\2\2\2H\u0199\3\2\2\2J\u019e\3\2\2\2L\u01a0\3\2"
			+ "\2\2N\u01a2\3\2\2\2P\u01a4\3\2\2\2R\u01a9\3\2\2\2T\u01b4\3\2\2\2V\u01b9"
			+ "\3\2\2\2X\u01bc\3\2\2\2Z\u01c4\3\2\2\2\\\u01d0\3\2\2\2^\u01d2\3\2\2\2"
			+ "`\u01e1\3\2\2\2b\u024b\3\2\2\2d\u0254\3\2\2\2f\u025c\3\2\2\2h\u0260\3"
			+ "\2\2\2j\u0276\3\2\2\2l\u0278\3\2\2\2n\u0284\3\2\2\2p\u0286\3\2\2\2r\u0288"
			+ "\3\2\2\2t\u028a\3\2\2\2v\u028c\3\2\2\2x\u028e\3\2\2\2z\u0290\3\2\2\2|"
			+ "\u0292\3\2\2\2~\u0294\3\2\2\2\u0080\u0296\3\2\2\2\u0082\u029d\3\2\2\2"
			+ "\u0084\u02a9\3\2\2\2\u0086\u02ab\3\2\2\2\u0088\u02b3\3\2\2\2\u008a\u02b5"
			+ "\3\2\2\2\u008c\u02c6\3\2\2\2\u008e\u02d7\3\2\2\2\u0090\u02d9\3\2\2\2\u0092"
			+ "\u0096\5d\63\2\u0093\u0097\5\4\3\2\u0094\u0097\5\6\4\2\u0095\u0097\5\16"
			+ "\b\2\u0096\u0093\3\2\2\2\u0096\u0094\3\2\2\2\u0096\u0095\3\2\2\2\u0097"
			+ "\u0098\3\2\2\2\u0098\u0099\5d\63\2\u0099\3\3\2\2\2\u009a\u009b\5\16\b"
			+ "\2\u009b\u009c\5d\63\2\u009c\u009d\7 \2\2\u009d\u009e\5d\63\2\u009e\u009f"
			+ "\5\60\31\2\u009f\5\3\2\2\2\u00a0\u00a4\5\b\5\2\u00a1\u00a4\5\n\6\2\u00a2"
			+ "\u00a4\5\f\7\2\u00a3\u00a0\3\2\2\2\u00a3\u00a1\3\2\2\2\u00a3\u00a2\3\2"
			+ "\2\2\u00a4\7\3\2\2\2\u00a5\u00ab\5\16\b\2\u00a6\u00a7\5d\63\2\u00a7\u00a8"
			+ "\5*\26\2\u00a8\u00a9\5d\63\2\u00a9\u00aa\5\16\b\2\u00aa\u00ac\3\2\2\2"
			+ "\u00ab\u00a6\3\2\2\2\u00ac\u00ad\3\2\2\2\u00ad\u00ab\3\2\2\2\u00ad\u00ae"
			+ "\3\2\2\2\u00ae\t\3\2\2\2\u00af\u00b5\5\16\b\2\u00b0\u00b1\5d\63\2\u00b1"
			+ "\u00b2\5,\27\2\u00b2\u00b3\5d\63\2\u00b3\u00b4\5\16\b\2\u00b4\u00b6\3"
			+ "\2\2\2\u00b5\u00b0\3\2\2\2\u00b6\u00b7\3\2\2\2\u00b7\u00b5\3\2\2\2\u00b7"
			+ "\u00b8\3\2\2\2\u00b8\13\3\2\2\2\u00b9\u00ba\5\16\b\2\u00ba\u00bb\5d\63"
			+ "\2\u00bb\u00bc\5.\30\2\u00bc\u00bd\5d\63\2\u00bd\u00be\5\16\b\2\u00be"
			+ "\r\3\2\2\2\u00bf\u00c0\5\34\17\2\u00c0\u00c1\5d\63\2\u00c1\u00c3\3\2\2"
			+ "\2\u00c2\u00bf\3\2\2\2\u00c2\u00c3\3\2\2\2\u00c3\u00c7\3\2\2\2\u00c4\u00c5"
			+ "\5\22\n\2\u00c5\u00c6\5d\63\2\u00c6\u00c8\3\2\2\2\u00c7\u00c4\3\2\2\2"
			+ "\u00c7\u00c8\3\2\2\2\u00c8\u00d0\3\2\2\2\u00c9\u00d1\5\20\t\2\u00ca\u00cb"
			+ "\7\16\2\2\u00cb\u00cc\5d\63\2\u00cc\u00cd\5\2\2\2\u00cd\u00ce\5d\63\2"
			+ "\u00ce\u00cf\7\17\2\2\u00cf\u00d1\3\2\2\2\u00d0\u00c9\3\2\2\2\u00d0\u00ca"
			+ "\3\2\2\2\u00d1\17\3\2\2\2\u00d2\u00d5\5\24\13\2\u00d3\u00d5\5\32\16\2"
			+ "\u00d4\u00d2\3\2\2\2\u00d4\u00d3\3\2\2\2\u00d5\21\3\2\2\2\u00d6\u00d7"
			+ "\7D\2\2\u00d7\23\3\2\2\2\u00d8\u00e0\5\26\f\2\u00d9\u00da\5d\63\2\u00da"
			+ "\u00db\7b\2\2\u00db\u00dc\5d\63\2\u00dc\u00dd\5\30\r\2\u00dd\u00de\5d"
			+ "\63\2\u00de\u00df\7b\2\2\u00df\u00e1\3\2\2\2\u00e0\u00d9\3\2\2\2\u00e0"
			+ "\u00e1\3\2\2\2\u00e1\25\3\2\2\2\u00e2\u00e3\5b\62\2\u00e3\27\3\2\2\2\u00e4"
			+ "\u00e6\5\u0082B\2\u00e5\u00e4\3\2\2\2\u00e6\u00e7\3\2\2\2\u00e7\u00e5"
			+ "\3\2\2\2\u00e7\u00e8\3\2\2\2\u00e8\u00f5\3\2\2\2\u00e9\u00eb\5p9\2\u00ea"
			+ "\u00e9\3\2\2\2\u00eb\u00ec\3\2\2\2\u00ec\u00ea\3\2\2\2\u00ec\u00ed\3\2"
			+ "\2\2\u00ed\u00ef\3\2\2\2\u00ee\u00f0\5\u0082B\2\u00ef\u00ee\3\2\2\2\u00f0"
			+ "\u00f1\3\2\2\2\u00f1\u00ef\3\2\2\2\u00f1\u00f2\3\2\2\2\u00f2\u00f4\3\2"
			+ "\2\2\u00f3\u00ea\3\2\2\2\u00f4\u00f7\3\2\2\2\u00f5\u00f3\3\2\2\2\u00f5"
			+ "\u00f6\3\2\2\2\u00f6\31\3\2\2\2\u00f7\u00f5\3\2\2\2\u00f8\u00f9\7\20\2"
			+ "\2\u00f9\33\3\2\2\2\u00fa\u0101\5\"\22\2\u00fb\u0101\5 \21\2\u00fc\u0101"
			+ "\5\36\20\2\u00fd\u0101\5(\25\2\u00fe\u0101\5&\24\2\u00ff\u0101\5$\23\2"
			+ "\u0100\u00fa\3\2\2\2\u0100\u00fb\3\2\2\2\u0100\u00fc\3\2\2\2\u0100\u00fd"
			+ "\3\2\2\2\u0100\u00fe\3\2\2\2\u0100\u00ff\3\2\2\2\u0101\35\3\2\2\2\u0102"
			+ "\u0103\7\"\2\2\u0103\37\3\2\2\2\u0104\u0105\7\"\2\2\u0105\u0106\7\"\2"
			+ "\2\u0106!\3\2\2\2\u0107\u0108\7\"\2\2\u0108\u0109\7\7\2\2\u0109#\3\2\2"
			+ "\2\u010a\u010b\7$\2\2\u010b%\3\2\2\2\u010c\u010d\7$\2\2\u010d\u010e\7"
			+ "$\2\2\u010e\'\3\2\2\2\u010f\u0110\7$\2\2\u0110\u0111\7\7\2\2\u0111)\3"
			+ "\2\2\2\u0112\u0113\t\2\2\2\u0113\u0114\t\3\2\2\u0114\u0115\t\4\2\2\u0115"
			+ "\u0118\5f\64\2\u0116\u0118\7\22\2\2\u0117\u0112\3\2\2\2\u0117\u0116\3"
			+ "\2\2\2\u0118+\3\2\2\2\u0119\u011a\t\5\2\2\u011a\u011b\t\6\2\2\u011b\u011c"
			+ "\5f\64\2\u011c-\3\2\2\2\u011d\u011e\t\7\2\2\u011e\u011f\t\b\2\2\u011f"
			+ "\u0120\t\3\2\2\u0120\u0121\t\t\2\2\u0121\u0122\t\n\2\2\u0122\u0123\5f"
			+ "\64\2\u0123/\3\2\2\2\u0124\u0125\5\66\34\2\u0125\u0128\5d\63\2\u0126\u0129"
			+ "\5\62\32\2\u0127\u0129\5\64\33\2\u0128\u0126\3\2\2\2\u0128\u0127\3\2\2"
			+ "\2\u0128\u0129\3\2\2\2\u0129\61\3\2\2\2\u012a\u012b\5d\63\2\u012b\u012c"
			+ "\5*\26\2\u012c\u012d\5d\63\2\u012d\u012e\5\66\34\2\u012e\u0130\3\2\2\2"
			+ "\u012f\u012a\3\2\2\2\u0130\u0131\3\2\2\2\u0131\u012f\3\2\2\2\u0131\u0132"
			+ "\3\2\2\2\u0132\63\3\2\2\2\u0133\u0134\5d\63\2\u0134\u0135\5,\27\2\u0135"
			+ "\u0136\5d\63\2\u0136\u0137\5\66\34\2\u0137\u0139\3\2\2\2\u0138\u0133\3"
			+ "\2\2\2\u0139\u013a\3\2\2\2\u013a\u0138\3\2\2\2\u013a\u013b\3\2\2\2\u013b"
			+ "\65\3\2\2\2\u013c\u0145\58\35\2\u013d\u0145\5@!\2\u013e\u013f\7\16\2\2"
			+ "\u013f\u0140\5d\63\2\u0140\u0141\5\60\31\2\u0141\u0142\5d\63\2\u0142\u0143"
			+ "\7\17\2\2\u0143\u0145\3\2\2\2\u0144\u013c\3\2\2\2\u0144\u013d\3\2\2\2"
			+ "\u0144\u013e\3\2\2\2\u0145\67\3\2\2\2\u0146\u0147\5> \2\u0147\u014a\5"
			+ "d\63\2\u0148\u014b\5:\36\2\u0149\u014b\5<\37\2\u014a\u0148\3\2\2\2\u014a"
			+ "\u0149\3\2\2\2\u014a\u014b\3\2\2\2\u014b9\3\2\2\2\u014c\u014d\5d\63\2"
			+ "\u014d\u014e\5*\26\2\u014e\u014f\5d\63\2\u014f\u0150\5> \2\u0150\u0152"
			+ "\3\2\2\2\u0151\u014c\3\2\2\2\u0152\u0153\3\2\2\2\u0153\u0151\3\2\2\2\u0153"
			+ "\u0154\3\2\2\2\u0154;\3\2\2\2\u0155\u0156\5d\63\2\u0156\u0157\5,\27\2"
			+ "\u0157\u0158\5d\63\2\u0158\u0159\5> \2\u0159\u015b\3\2\2\2\u015a\u0155"
			+ "\3\2\2\2\u015b\u015c\3\2\2\2\u015c\u015a\3\2\2\2\u015c\u015d\3\2\2\2\u015d"
			+ "=\3\2\2\2\u015e\u0166\5B\"\2\u015f\u0160\7\16\2\2\u0160\u0161\5d\63\2"
			+ "\u0161\u0162\58\35\2\u0162\u0163\5d\63\2\u0163\u0164\7\17\2\2\u0164\u0166"
			+ "\3\2\2\2\u0165\u015e\3\2\2\2\u0165\u015f\3\2\2\2\u0166?\3\2\2\2\u0167"
			+ "\u0168\7A\2\2\u0168\u0169\5D#\2\u0169\u016a\7C\2\2\u016a\u016b\5d\63\2"
			+ "\u016b\u016d\3\2\2\2\u016c\u0167\3\2\2\2\u016c\u016d\3\2\2\2\u016d\u016e"
			+ "\3\2\2\2\u016e\u016f\7a\2\2\u016f\u0170\5d\63\2\u0170\u0171\58\35\2\u0171"
			+ "\u0172\5d\63\2\u0172\u0173\7c\2\2\u0173A\3\2\2\2\u0174\u0175\7A\2\2\u0175"
			+ "\u0176\5D#\2\u0176\u0177\7C\2\2\u0177\u0178\5d\63\2\u0178\u017a\3\2\2"
			+ "\2\u0179\u0174\3\2\2\2\u0179\u017a\3\2\2\2\u017a\u017e\3\2\2\2\u017b\u017c"
			+ "\5N(\2\u017c\u017d\5d\63\2\u017d\u017f\3\2\2\2\u017e\u017b\3\2\2\2\u017e"
			+ "\u017f\3\2\2\2\u017f\u0180\3\2\2\2\u0180\u0181\5P)\2\u0181\u0191\5d\63"
			+ "\2\u0182\u0183\5R*\2\u0183\u0184\5d\63\2\u0184\u0185\5\16\b\2\u0185\u0192"
			+ "\3\2\2\2\u0186\u0187\5T+\2\u0187\u0188\5d\63\2\u0188\u0189\7\t\2\2\u0189"
			+ "\u018a\5X-\2\u018a\u0192\3\2\2\2\u018b\u018c\5V,\2\u018c\u018d\5d\63\2"
			+ "\u018d\u018e\5x=\2\u018e\u018f\5Z.\2\u018f\u0190\5x=\2\u0190\u0192\3\2"
			+ "\2\2\u0191\u0182\3\2\2\2\u0191\u0186\3\2\2\2\u0191\u018b\3\2\2\2\u0192"
			+ "C\3\2\2\2\u0193\u0194\5F$\2\u0194\u0195\5H%\2\u0195\u0196\5J&\2\u0196"
			+ "E\3\2\2\2\u0197\u0198\5`\61\2\u0198G\3\2\2\2\u0199\u019a\7\24\2\2\u019a"
			+ "\u019b\7\24\2\2\u019bI\3\2\2\2\u019c\u019f\5`\61\2\u019d\u019f\5L\'\2"
			+ "\u019e\u019c\3\2\2\2\u019e\u019d\3\2\2\2\u019fK\3\2\2\2\u01a0\u01a1\7"
			+ "\20\2\2\u01a1M\3\2\2\2\u01a2\u01a3\78\2\2\u01a3O\3\2\2\2\u01a4\u01a5\5"
			+ "\16\b\2\u01a5Q\3\2\2\2\u01a6\u01aa\7#\2\2\u01a7\u01a8\7\7\2\2\u01a8\u01aa"
			+ "\7#\2\2\u01a9\u01a6\3\2\2\2\u01a9\u01a7\3\2\2\2\u01aaS\3\2\2\2\u01ab\u01b5"
			+ "\7#\2\2\u01ac\u01ad\7\7\2\2\u01ad\u01b5\7#\2\2\u01ae\u01af\7\"\2\2\u01af"
			+ "\u01b5\7#\2\2\u01b0\u01b5\7\"\2\2\u01b1\u01b2\7$\2\2\u01b2\u01b5\7#\2"
			+ "\2\u01b3\u01b5\7$\2\2\u01b4\u01ab\3\2\2\2\u01b4\u01ac\3\2\2\2\u01b4\u01ae"
			+ "\3\2\2\2\u01b4\u01b0\3\2\2\2\u01b4\u01b1\3\2\2\2\u01b4\u01b3\3\2\2\2\u01b5"
			+ "U\3\2\2\2\u01b6\u01ba\7#\2\2\u01b7\u01b8\7\7\2\2\u01b8\u01ba\7#\2\2\u01b9"
			+ "\u01b6\3\2\2\2\u01b9\u01b7\3\2\2\2\u01baW\3\2\2\2\u01bb\u01bd\t\13\2\2"
			+ "\u01bc\u01bb\3\2\2\2\u01bc\u01bd\3\2\2\2\u01bd\u01c0\3\2\2\2\u01be\u01c1"
			+ "\5^\60\2\u01bf\u01c1\5\\/\2\u01c0\u01be\3\2\2\2\u01c0\u01bf\3\2\2\2\u01c1"
			+ "Y\3\2\2\2\u01c2\u01c5\5\u0084C\2\u01c3\u01c5\5\u0088E\2\u01c4\u01c2\3"
			+ "\2\2\2\u01c4\u01c3\3\2\2\2\u01c5\u01c6\3\2\2\2\u01c6\u01c4\3\2\2\2\u01c6"
			+ "\u01c7\3\2\2\2\u01c7[\3\2\2\2\u01c8\u01cc\5\u0080A\2\u01c9\u01cb\5|?\2"
			+ "\u01ca\u01c9\3\2\2\2\u01cb\u01ce\3\2\2\2\u01cc\u01ca\3\2\2\2\u01cc\u01cd"
			+ "\3\2\2\2\u01cd\u01d1\3\2\2\2\u01ce\u01cc\3\2\2\2\u01cf\u01d1\5~@\2\u01d0"
			+ "\u01c8\3\2\2\2\u01d0\u01cf\3\2\2\2\u01d1]\3\2\2\2\u01d2\u01d3\5\\/\2\u01d3"
			+ "\u01d5\7\24\2\2\u01d4\u01d6\5|?\2\u01d5\u01d4\3\2\2\2\u01d6\u01d7\3\2"
			+ "\2\2\u01d7\u01d5\3\2\2\2\u01d7\u01d8\3\2\2\2\u01d8_\3\2\2\2\u01d9\u01dd"
			+ "\5\u0080A\2\u01da\u01dc\5|?\2\u01db\u01da\3\2\2\2\u01dc\u01df\3\2\2\2"
			+ "\u01dd\u01db\3\2\2\2\u01dd\u01de\3\2\2\2\u01de\u01e2\3\2\2\2\u01df\u01dd"
			+ "\3\2\2\2\u01e0\u01e2\5~@\2\u01e1\u01d9\3\2\2\2\u01e1\u01e0\3\2\2\2\u01e2"
			+ "a\3\2\2\2\u01e3\u01e4\5\u0080A\2\u01e4\u01e5\5|?\2\u01e5\u01e6\5|?\2\u01e6"
			+ "\u01e7\5|?\2\u01e7\u01e8\5|?\2\u01e8\u0244\5|?\2\u01e9\u01eb\5|?\2\u01ea"
			+ "\u01e9\3\2\2\2\u01ea\u01eb\3\2\2\2\u01eb\u0245\3\2\2\2\u01ec\u01ed\5|"
			+ "?\2\u01ed\u01ee\5|?\2\u01ee\u0245\3\2\2\2\u01ef\u01f0\5|?\2\u01f0\u01f1"
			+ "\5|?\2\u01f1\u01f2\5|?\2\u01f2\u0245\3\2\2\2\u01f3\u01f4\5|?\2\u01f4\u01f5"
			+ "\5|?\2\u01f5\u01f6\5|?\2\u01f6\u01f7\5|?\2\u01f7\u0245\3\2\2\2\u01f8\u01f9"
			+ "\5|?\2\u01f9\u01fa\5|?\2\u01fa\u01fb\5|?\2\u01fb\u01fc\5|?\2\u01fc\u01fd"
			+ "\5|?\2\u01fd\u0245\3\2\2\2\u01fe\u01ff\5|?\2\u01ff\u0200\5|?\2\u0200\u0201"
			+ "\5|?\2\u0201\u0202\5|?\2\u0202\u0203\5|?\2\u0203\u0204\5|?\2\u0204\u0245"
			+ "\3\2\2\2\u0205\u0206\5|?\2\u0206\u0207\5|?\2\u0207\u0208\5|?\2\u0208\u0209"
			+ "\5|?\2\u0209\u020a\5|?\2\u020a\u020b\5|?\2\u020b\u020c\5|?\2\u020c\u0245"
			+ "\3\2\2\2\u020d\u020e\5|?\2\u020e\u020f\5|?\2\u020f\u0210\5|?\2\u0210\u0211"
			+ "\5|?\2\u0211\u0212\5|?\2\u0212\u0213\5|?\2\u0213\u0214\5|?\2\u0214\u0215"
			+ "\5|?\2\u0215\u0245\3\2\2\2\u0216\u0217\5|?\2\u0217\u0218\5|?\2\u0218\u0219"
			+ "\5|?\2\u0219\u021a\5|?\2\u021a\u021b\5|?\2\u021b\u021c\5|?\2\u021c\u021d"
			+ "\5|?\2\u021d\u021e\5|?\2\u021e\u021f\5|?\2\u021f\u0245\3\2\2\2\u0220\u0221"
			+ "\5|?\2\u0221\u0222\5|?\2\u0222\u0223\5|?\2\u0223\u0224\5|?\2\u0224\u0225"
			+ "\5|?\2\u0225\u0226\5|?\2\u0226\u0227\5|?\2\u0227\u0228\5|?\2\u0228\u0229"
			+ "\5|?\2\u0229\u022a\5|?\2\u022a\u0245\3\2\2\2\u022b\u022c\5|?\2\u022c\u022d"
			+ "\5|?\2\u022d\u022e\5|?\2\u022e\u022f\5|?\2\u022f\u0230\5|?\2\u0230\u0231"
			+ "\5|?\2\u0231\u0232\5|?\2\u0232\u0233\5|?\2\u0233\u0234\5|?\2\u0234\u0235"
			+ "\5|?\2\u0235\u0236\5|?\2\u0236\u0245\3\2\2\2\u0237\u0238\5|?\2\u0238\u0239"
			+ "\5|?\2\u0239\u023a\5|?\2\u023a\u023b\5|?\2\u023b\u023c\5|?\2\u023c\u023d"
			+ "\5|?\2\u023d\u023e\5|?\2\u023e\u023f\5|?\2\u023f\u0240\5|?\2\u0240\u0241"
			+ "\5|?\2\u0241\u0242\5|?\2\u0242\u0243\5|?\2\u0243\u0245\3\2\2\2\u0244\u01ea"
			+ "\3\2\2\2\u0244\u01ec\3\2\2\2\u0244\u01ef\3\2\2\2\u0244\u01f3\3\2\2\2\u0244"
			+ "\u01f8\3\2\2\2\u0244\u01fe\3\2\2\2\u0244\u0205\3\2\2\2\u0244\u020d\3\2"
			+ "\2\2\u0244\u0216\3\2\2\2\u0244\u0220\3\2\2\2\u0244\u022b\3\2\2\2\u0244"
			+ "\u0237\3\2\2\2\u0245\u024c\3\2\2\2\u0246\u0248\5\u0086D\2\u0247\u0246"
			+ "\3\2\2\2\u0248\u0249\3\2\2\2\u0249\u0247\3\2\2\2\u0249\u024a\3\2\2\2\u024a"
			+ "\u024c\3\2\2\2\u024b\u01e3\3\2\2\2\u024b\u0247\3\2\2\2\u024cc\3\2\2\2"
			+ "\u024d\u0253\5p9\2\u024e\u0253\5r:\2\u024f\u0253\5t;\2\u0250\u0253\5v"
			+ "<\2\u0251\u0253\5h\65\2\u0252\u024d\3\2\2\2\u0252\u024e\3\2\2\2\u0252"
			+ "\u024f\3\2\2\2\u0252\u0250\3\2\2\2\u0252\u0251\3\2\2\2\u0253\u0256\3\2"
			+ "\2\2\u0254\u0252\3\2\2\2\u0254\u0255\3\2\2\2\u0255e\3\2\2\2\u0256\u0254"
			+ "\3\2\2\2\u0257\u025d\5p9\2\u0258\u025d\5r:\2\u0259\u025d\5t;\2\u025a\u025d"
			+ "\5v<\2\u025b\u025d\5h\65\2\u025c\u0257\3\2\2\2\u025c\u0258\3\2\2\2\u025c"
			+ "\u0259\3\2\2\2\u025c\u025a\3\2\2\2\u025c\u025b\3\2\2\2\u025d\u025e\3\2"
			+ "\2\2\u025e\u025c\3\2\2\2\u025e\u025f\3\2\2\2\u025fg\3\2\2\2\u0260\u0261"
			+ "\7\25\2\2\u0261\u0262\7\20\2\2\u0262\u0267\3\2\2\2\u0263\u0266\5j\66\2"
			+ "\u0264\u0266\5l\67\2\u0265\u0263\3\2\2\2\u0265\u0264\3\2\2\2\u0266\u0269"
			+ "\3\2\2\2\u0267\u0265\3\2\2\2\u0267\u0268\3\2\2\2\u0268\u026a\3\2\2\2\u0269"
			+ "\u0267\3\2\2\2\u026a\u026b\7\20\2\2\u026b\u026c\7\25\2\2\u026ci\3\2\2"
			+ "\2\u026d\u0277\5p9\2\u026e\u0277\5r:\2\u026f\u0277\5t;\2\u0270\u0277\5"
			+ "v<\2\u0271\u0277\t\f\2\2\u0272\u0277\t\r\2\2\u0273\u0277\5\u008aF\2\u0274"
			+ "\u0277\5\u008cG\2\u0275\u0277\5\u008eH\2\u0276\u026d\3\2\2\2\u0276\u026e"
			+ "\3\2\2\2\u0276\u026f\3\2\2\2\u0276\u0270\3\2\2\2\u0276\u0271\3\2\2\2\u0276"
			+ "\u0272\3\2\2\2\u0276\u0273\3\2\2\2\u0276\u0274\3\2\2\2\u0276\u0275\3\2"
			+ "\2\2\u0277k\3\2\2\2\u0278\u0279\7\20\2\2\u0279\u027a\5n8\2\u027am\3\2"
			+ "\2\2\u027b\u0285\5p9\2\u027c\u0285\5r:\2\u027d\u0285\5t;\2\u027e\u0285"
			+ "\5v<\2\u027f\u0285\t\16\2\2\u0280\u0285\t\17\2\2\u0281\u0285\5\u008aF"
			+ "\2\u0282\u0285\5\u008cG\2\u0283\u0285\5\u008eH\2\u0284\u027b\3\2\2\2\u0284"
			+ "\u027c\3\2\2\2\u0284\u027d\3\2\2\2\u0284\u027e\3\2\2\2\u0284\u027f\3\2"
			+ "\2\2\u0284\u0280\3\2\2\2\u0284\u0281\3\2\2\2\u0284\u0282\3\2\2\2\u0284"
			+ "\u0283\3\2\2\2\u0285o\3\2\2\2\u0286\u0287\7\6\2\2\u0287q\3\2\2\2\u0288"
			+ "\u0289\7\3\2\2\u0289s\3\2\2\2\u028a\u028b\7\5\2\2\u028bu\3\2\2\2\u028c"
			+ "\u028d\7\4\2\2\u028dw\3\2\2\2\u028e\u028f\7\b\2\2\u028fy\3\2\2\2\u0290"
			+ "\u0291\7B\2\2\u0291{\3\2\2\2\u0292\u0293\t\20\2\2\u0293}\3\2\2\2\u0294"
			+ "\u0295\7\26\2\2\u0295\177\3\2\2\2\u0296\u0297\t\21\2\2\u0297\u0081\3\2"
			+ "\2\2\u0298\u029e\t\22\2\2\u0299\u029e\t\23\2\2\u029a\u029e\5\u008aF\2"
			+ "\u029b\u029e\5\u008cG\2\u029c\u029e\5\u008eH\2\u029d\u0298\3\2\2\2\u029d"
			+ "\u0299\3\2\2\2\u029d\u029a\3\2\2\2\u029d\u029b\3\2\2\2\u029d\u029c\3\2"
			+ "\2\2\u029e\u0083\3\2\2\2\u029f\u02aa\5p9\2\u02a0\u02aa\5r:\2\u02a1\u02aa"
			+ "\5t;\2\u02a2\u02aa\5v<\2\u02a3\u02aa\t\24\2\2\u02a4\u02aa\t\25\2\2\u02a5"
			+ "\u02aa\t\26\2\2\u02a6\u02aa\5\u008aF\2\u02a7\u02aa\5\u008cG\2\u02a8\u02aa"
			+ "\5\u008eH\2\u02a9\u029f\3\2\2\2\u02a9\u02a0\3\2\2\2\u02a9\u02a1\3\2\2"
			+ "\2\u02a9\u02a2\3\2\2\2\u02a9\u02a3\3\2\2\2\u02a9\u02a4\3\2\2\2\u02a9\u02a5"
			+ "\3\2\2\2\u02a9\u02a6\3\2\2\2\u02a9\u02a7\3\2\2\2\u02a9\u02a8\3\2\2\2\u02aa"
			+ "\u0085\3\2\2\2\u02ab\u02ac\t\27\2\2\u02ac\u0087\3\2\2\2\u02ad\u02ae\5"
			+ "z>\2\u02ae\u02af\5x=\2\u02af\u02b4\3\2\2\2\u02b0\u02b1\5z>\2\u02b1\u02b2"
			+ "\5z>\2\u02b2\u02b4\3\2\2\2\u02b3\u02ad\3\2\2\2\u02b3\u02b0\3\2\2\2\u02b4"
			+ "\u0089\3\2\2\2\u02b5\u02b6\t\30\2\2\u02b6\u02b7\5\u0090I\2\u02b7\u008b"
			+ "\3\2\2\2\u02b8\u02b9\7\u00c3\2\2\u02b9\u02ba\t\31\2\2\u02ba\u02c7\5\u0090"
			+ "I\2\u02bb\u02bc\t\32\2\2\u02bc\u02bd\5\u0090I\2\u02bd\u02be\5\u0090I\2"
			+ "\u02be\u02c7\3\2\2\2\u02bf\u02c0\7\u00d0\2\2\u02c0\u02c1\t\33\2\2\u02c1"
			+ "\u02c7\5\u0090I\2\u02c2\u02c3\t\34\2\2\u02c3\u02c4\5\u0090I\2\u02c4\u02c5"
			+ "\5\u0090I\2\u02c5\u02c7\3\2\2\2\u02c6\u02b8\3\2\2\2\u02c6\u02bb\3\2\2"
			+ "\2\u02c6\u02bf\3\2\2\2\u02c6\u02c2\3\2\2\2\u02c7\u008d\3\2\2\2\u02c8\u02c9"
			+ "\7\u00d3\2\2\u02c9\u02ca\t\35\2\2\u02ca\u02cb\5\u0090I\2\u02cb\u02cc\5"
			+ "\u0090I\2\u02cc\u02d8\3\2\2\2\u02cd\u02ce\t\36\2\2\u02ce\u02cf\5\u0090"
			+ "I\2\u02cf\u02d0\5\u0090I\2\u02d0\u02d1\5\u0090I\2\u02d1\u02d8\3\2\2\2"
			+ "\u02d2\u02d3\7\u00d7\2\2\u02d3\u02d4\t\37\2\2\u02d4\u02d5\5\u0090I\2\u02d5"
			+ "\u02d6\5\u0090I\2\u02d6\u02d8\3\2\2\2\u02d7\u02c8\3\2\2\2\u02d7\u02cd"
			+ "\3\2\2\2\u02d7\u02d2\3\2\2\2\u02d8\u008f\3\2\2\2\u02d9\u02da\t \2\2\u02da"
			+ "\u0091\3\2\2\2;\u0096\u00a3\u00ad\u00b7\u00c2\u00c7\u00d0\u00d4\u00e0"
			+ "\u00e7\u00ec\u00f1\u00f5\u0100\u0117\u0128\u0131\u013a\u0144\u014a\u0153"
			+ "\u015c\u0165\u016c\u0179\u017e\u0191\u019e\u01a9\u01b4\u01b9\u01bc\u01c0"
			+ "\u01c4\u01c6\u01cc\u01d0\u01d7\u01dd\u01e1\u01ea\u0244\u0249\u024b\u0252"
			+ "\u0254\u025c\u025e\u0265\u0267\u0276\u0284\u029d\u02a9\u02b3\u02c6\u02d7";

	/** The Constant _ATN. */
	public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}