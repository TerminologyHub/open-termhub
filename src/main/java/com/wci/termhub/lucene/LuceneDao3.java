package com.wci.termhub.lucene;

//import java.io.IOException;
//import java.nio.file.Paths;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.core.KeywordAnalyzer;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.index.DirectoryReader;
//import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
//import org.apache.lucene.search.IndexSearcher;
//import org.apache.lucene.search.Query;
//import org.apache.lucene.store.Directory;
//import org.apache.lucene.store.FSDirectory;
//import org.springframework.data.elasticsearch.annotations.FieldType;
//
///**
// * The Class LuceneDao3.
// */
//public class LuceneDao3 {
//
//	/** The index dir. */
//	private final Directory indexDir;
//
//	/** The field analyzers. */
//	private final Map<String, Analyzer> fieldAnalyzers = new HashMap<>();
//
//	/**
//	 * Instantiates a new lucene dao 3.
//	 *
//	 * @param indexPath the index path
//	 * @throws IOException Signals that an I/O exception has occurred.
//	 */
//	public LuceneDao3(final String indexPath) throws IOException {
//		indexDir = FSDirectory.open(Paths.get(indexPath));
//		// Initialize fieldAnalyzers with default analyzers or custom ones
//		fieldAnalyzers.put("text", new StandardAnalyzer());
//		// ... add other analyzers for different field types
//	}
//
//	/**
//	 * Index.
//	 *
//	 * @param pojo the pojo
//	 * @throws IOException            Signals that an I/O exception has occurred.
//	 * @throws IllegalAccessException the illegal access exception
//	 */
//	public void index(Object pojo) throws IOException, IllegalAccessException {
//		// ... indexing logic as before, but also populate fieldAnalyzers if needed
//	}
//
//	/**
//	 * Search.
//	 *
//	 * @param queryStr the query str
//	 * @return the list
//	 * @throws IOException    Signals that an I/O exception has occurred.
//	 * @throws ParseException the parse exception
//	 */
//	public List<Object> search(final String queryStr) throws IOException, ParseException {
//		final DirectoryReader reader = DirectoryReader.open(indexDir);
//		final IndexSearcher searcher = new IndexSearcher(reader);
//
//		// Create a MultiFieldQueryParser with dynamic analyzers
//		// String[] fields, Analyzer analyzer, Map<String,Float> boosts
//		final StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
//		final MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[] { "field1", "field2" },
//				standardAnalyzer);
//
//		final Query query = parser.parse(queryStr);
//
//		// ... search and result processing as before
//	}
//
//	/**
//	 * Creates the analyzer.
//	 *
//	 * @param fieldType the field type
//	 * @return the analyzer
//	 */
//	// Helper method to create appropriate analyzer based on field type
//	private Analyzer createAnalyzer(final FieldType fieldType) {
//		// Implement logic to create different analyzers based on field type
//		switch (fieldType) {
//		case Text:
//			return new StandardAnalyzer();
//		case Keyword:
//			return new KeywordAnalyzer();
//		// ... other field types and analyzers
//		default:
//			return new StandardAnalyzer(); // Default analyzer
//		}
//	}
//}