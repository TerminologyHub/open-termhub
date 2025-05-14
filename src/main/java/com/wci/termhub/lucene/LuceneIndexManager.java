/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

/**
 * Lucene index manager.
 */
public final class LuceneIndexManager {

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
   * @param indexDirectory the index directory
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private LuceneIndexManager(final String indexRootDirectory, final String indexDirectory)
      throws IOException {
    // Fix Windows path handling
    final String fixedRootDir = indexRootDirectory.contains("/")
        ? indexRootDirectory.replace('/', File.separatorChar) : indexRootDirectory;

    final File indexDir = new File(fixedRootDir, indexDirectory);
    if (!indexDir.exists()) {
      indexDir.mkdirs();
    }

    @SuppressWarnings("resource")
    final FSDirectory fsDirectory = FSDirectory.open(indexDir.toPath());
    this.indexReader = DirectoryReader.open(fsDirectory);
    this.indexSearcher = new IndexSearcher(indexReader);
  }

  /**
   * Gets the single instance of LuceneIndexManager.
   *
   * @param indexRootDirectory the index root directory
   * @param indexDirectory the index directory
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
