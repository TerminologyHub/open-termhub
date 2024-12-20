/*
 *
 */
package com.wci.termhub.lucene;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

/**
 * The Class LuceneIndexManager.
 */
public class LuceneIndexManager {

	/** The instance. */
	private static LuceneIndexManager instance;

	/** The index reader. */
	private IndexReader indexReader;

	/** The index searcher. */
	private IndexSearcher indexSearcher;

	/**
	 * Instantiates a new lucene index manager.
	 *
	 * @param indexRootDirectory the index root directory
	 * @param indexDirectory     the index directory
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private LuceneIndexManager(final String indexRootDirectory, final String indexDirectory) throws IOException {
		final FSDirectory fsDirectory = FSDirectory.open(Paths.get(indexRootDirectory, indexDirectory));
		this.indexReader = DirectoryReader.open(fsDirectory);
		this.indexSearcher = new IndexSearcher(indexReader);
	}

	/**
	 * Gets the single instance of LuceneIndexManager.
	 *
	 * @param indexRootDirectory the index root directory
	 * @param indexDirectory     the index directory
	 * @return single instance of LuceneIndexManager
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static synchronized LuceneIndexManager getInstance(final String indexRootDirectory,
			final String indexDirectory) throws IOException {
		if (instance == null) {
			instance = new LuceneIndexManager(indexRootDirectory, indexDirectory);
		}
		return instance;
	}

	/**
	 * Gets the index searcher.
	 *
	 * @return the index searcher
	 */
	public IndexSearcher getIndexSearcher() {
		return indexSearcher;
	}

	/**
	 * Refresh.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void refresh() throws IOException {
		final DirectoryReader newReader = DirectoryReader.openIfChanged((DirectoryReader) indexReader);
		if (newReader != null) {
			indexReader.close();
			indexReader = newReader;
			indexSearcher = new IndexSearcher(indexReader);
		}
	}
}
