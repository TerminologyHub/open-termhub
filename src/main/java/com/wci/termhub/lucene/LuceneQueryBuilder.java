package com.wci.termhub.lucene;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LuceneQueryBuilder.
 */
public final class LuceneQueryBuilder {

	/** The logger. */
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(LuceneQueryBuilder.class);

	/**
	 * Instantiates a new lucene query builder.
	 */
	private LuceneQueryBuilder() {
		// private constructor to prevent instantiation
	}

	/**
	 * Parses the.
	 *
	 * @param clazz     the clazz
	 * @param queryText the query text
	 * @return the query
	 * @throws ParseException the parse exception
	 */
	@SuppressWarnings("rawtypes")
	public static Query parse(final Class clazz, final String queryText) throws ParseException {

		final KeywordAnalyzer analyzer = new KeywordAnalyzer();
		final QueryParser queryParser = new QueryParser(clazz.getCanonicalName(), analyzer);

		// Split the queryText into individual fieldname-value pairs
		final String[] pairs = queryText.split(" OR ");

		final StringBuilder escapedQueryTextBuilder = new StringBuilder();

		for (final String pair : pairs) {
			// Split the pair into field name and value at the first occurrence of ":"
			final String[] parts = pair.split(":", 2);
			final String fieldname = parts[0];
			String value = parts.length > 1 ? parts[1] : "";

			// Check if the value ends with a wildcard
			final boolean startsWithWildcard = value.startsWith("*");
			final boolean endsWithWildcard = value.endsWith("*");

			// If the value ends with a wildcard, remove it before escaping
			if (endsWithWildcard) {
				value = value.substring(0, value.length() - 1);
			}

			String escapedValue = (!"id".equals(fieldname)) ? QueryParser.escape(value) : value;

			// Add the wildcard back if it was present
			if (startsWithWildcard) {
				escapedValue = "*" + escapedValue;
			}
			if (endsWithWildcard && escapedValue.length() > 1) {
				escapedValue = escapedValue + "*";
			}

			// Reassemble the pair and add it to the escapedQueryTextBuilder
			escapedQueryTextBuilder.append(fieldname).append(":").append(escapedValue).append(" OR ");
		}

		// Remove the trailing " OR "
		String escapedQueryText = escapedQueryTextBuilder.toString().trim();
		if (escapedQueryText.endsWith("OR")) {
			escapedQueryText = escapedQueryText.substring(0, escapedQueryText.length() - 3).trim();
		}

		final Query query = queryParser.parse(escapedQueryText);
		logger.info("Parsed Query: {}", query.toString());

		return query;
	}

}
