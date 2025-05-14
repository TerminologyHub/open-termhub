/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.lucene.LuceneDataAccess;
import com.wci.termhub.model.Term;
import com.wci.termhub.util.ModelUtility;

/**
 * Term loader.
 */
public final class TermLoader {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(TermLoader.class);

  /**
   * Instantiates a new term loader.
   */
  private TermLoader() {
    // private constructor
  }

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(final String[] args) {

    try {

      if (args == null || args.length == 0 || StringUtils.isBlank(args[0])) {
        logger.error("File name is required.");
        System.exit(1);
      }

      // get file name from command line
      @SuppressWarnings("null")
      final String fullFileName = args[0];
      if (!Files.exists(Paths.get(fullFileName))) {
        logger.error("File does not exist at " + fullFileName);
        System.exit(1);
      }

      int batchSize = 1000;
      if (args.length > 1 && StringUtils.isNotBlank(args[1])) {
        batchSize = Integer.parseInt(args[1]);
      }

      int limit = -1;
      if (args.length > 2 && StringUtils.isNotBlank(args[2])) {
        limit = Integer.parseInt(args[2]);
      }

      index(fullFileName, batchSize, limit);

    } catch (final Exception e) {
      logger.error("An error occurred while loading the file.");
      e.printStackTrace();
      System.exit(1);
    }

    System.exit(0);

  }

  /**
   * Index.
   *
   * @param fullFileName the full file name
   * @param batchSize the batch size
   * @param limit the limit
   * @throws Exception the exception
   */
  public static void index(final String fullFileName, final int batchSize, final int limit)
    throws Exception {

    System.out.println("batch size: " + batchSize + " limit: " + limit);
    final long startTime = System.currentTimeMillis();

    final List<Term> termBatch = new ArrayList<>(batchSize);

    // read the file
    // for each line in the file, convert to Concept object.
    try (final BufferedReader br = new BufferedReader(new FileReader(fullFileName))) {

      final ObjectMapper objectMapper = new ObjectMapper();
      final LuceneDataAccess luceneDataAccess = new LuceneDataAccess();
      luceneDataAccess.createIndex(Term.class);

      String line;
      int count = 1;
      while ((line = br.readLine()) != null && (limit == -1 || count < limit)) {

        final JsonNode rootNode = objectMapper.readTree(line);
        final JsonNode termNode = (rootNode.has("_source")) ? rootNode.get("_source") : rootNode;
        final Term term = ModelUtility.fromJson(termNode.toString(), Term.class);
        termBatch.add(term);

        if (termBatch.size() == batchSize) {
          luceneDataAccess.add(termBatch);
          termBatch.clear();
          System.out.println("count: " + count);
        }

        count++;
      }

      if (!termBatch.isEmpty()) {
        luceneDataAccess.add(termBatch);
      }

      System.out.println("final count: " + count);
      System.out.println("duration: " + (System.currentTimeMillis() - startTime) + " ms");

    } catch (final Exception e) {
      logger.error("An error occurred while processing the file.");
      e.printStackTrace();
      System.exit(1);
    }
  }
}
