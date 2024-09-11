package com.wci.termhub.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
		// private constructor
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
		final StandardAnalyzer analyzer = new StandardAnalyzer();
		final QueryParser queryParser = new QueryParser(clazz.getCanonicalName(), analyzer);

		// Split the queryText into individual fieldname-value pairs
		final String[] pairs = queryText.split(" OR ");

		final StringBuilder escapedQueryTextBuilder = new StringBuilder();

		for (final String pair : pairs) {
			// Split the pair into fieldname and value at the first occurrence of ":"
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

			// Escape the value
			String escapedValue = QueryParser.escape(value);

			// Add the wildcard back if it was present
			if (startsWithWildcard) {
				escapedValue = "*" + escapedValue;
			}
			if (endsWithWildcard) {
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

//	/**
//	 * Parses the.
//	 *
//	 * @param searcher  the searcher
//	 * @param queryText the query text
//	 * @return the query
//	 * @throws IOException Signals that an I/O exception has occurred.
//	 */
//	@SuppressWarnings("rawtypes")
//	public static Query parse(final Class clazz, final String queryText) throws ParseException {
//		
//		final StandardAnalyzer analyzer = new StandardAnalyzer();
//        final QueryParser queryParser = new QueryParser(clazz.getCanonicalName(), analyzer);
//        
//        // Split the queryText into fieldname and value
//        final String[] parts = queryText.split(":", 2);
//        final String fieldname = parts[0];
//        final String value = parts.length > 1 ? parts[1] : "";
//
//        // Check if the value ends with a wildcard
//        boolean startsWithWildcard = value.startsWith("*");
//        boolean endsWithWildcard = value.endsWith("*");
//        logger.info("Starts with wildcard: {}", startsWithWildcard);
//        logger.info("Ends   with wildcard: {}", endsWithWildcard);
//
//        String escapedValue = value;
//		if (!startsWithWildcard && !endsWithWildcard) {
//			// Escape the value
//			escapedValue = QueryParser.escape(value);
//		} else {
//			if (startsWithWildcard) {
//				// Remove the leading wildcard
//				escapedValue =  QueryParser.escape(value.substring(1));
//				escapedValue = "*" + escapedValue;
//		    }
//			if (endsWithWildcard) {
//				// Remove the trailing wildcard
//				escapedValue =  QueryParser.escape(value.substring(0, value.length() - 1));
//				escapedValue = escapedValue + "*";
//			}
//		}
//        
//        logger.info("Escape Value: {}", escapedValue);
//        
//        // Reassemble the queryText
//        final String escapedQueryText = fieldname + ":" + escapedValue;
//
//        final Query query = queryParser.parse(escapedQueryText);
//
//        logger.info("Parsed Query: {}", query.toString());
//
//        return query;
//	}

}
