/*
 * Copyright 2024 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.util.StringUtility;

/**
 * Default implementation a query builder.
 */
@Component
@Order(1)
public class DefaultQueryBuilder implements QueryBuilder {

	/** The logger. */
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(DefaultQueryBuilder.class);

	/**
	 * Instantiates a handler.
	 */
	public DefaultQueryBuilder() {
		// n/a
	}

	/* see superclass */
	@Override
	public boolean accepts(final String handler) {
		return handler == null || handler.equals("default");

	}

	/* see superclass */
	@Override
	public String buildQuery(final SearchParameters params) {
		final StringBuilder sb = new StringBuilder();
		sb.append(buildQuery(params.getQuery()));
		if (params.getActive() != null) {
			sb.append(" AND active:" + params.getActive());
		}
		if (params.getLeaf() != null) {
			sb.append(" AND leaf:" + params.getLeaf());
		}
		return sb.toString();
	}

	/* see superclass */
	@Override
	public String buildQuery(final String query) {
		return (StringUtility.isEmpty(query) || query.equals("*")) ? "*" : query;
	}

	/* see superclass */
	@Override
	public String buildEscapedQuery(final SearchParameters params) {
		final StringBuilder sb = new StringBuilder();
		// Same thing but with an escaped query
		sb.append(buildEscapedQuery(params.getQuery()));
		if (params.getActive() != null) {
			sb.append(" AND active:" + params.getActive());
		}
		if (params.getLeaf() != null) {
			sb.append(" AND leaf:" + params.getLeaf());
		}
		return sb.toString();
	}

	/* see superclass */
	@Override
	public String buildEscapedQuery(final String query) {
		return (StringUtility.isEmpty(query) || query.equals("*")) ? "*"
				: ("\"" + StringUtility.escapeQuery(query) + "\"");
	}

}
