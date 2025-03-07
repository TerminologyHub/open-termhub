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

import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Loader {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(Loader.class);

  /**
   * Instantiates a new loader.
   */
  private Loader() {
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
      final String terminology = args[0];

      int batchSize = 1000;
      if (args.length > 1 && StringUtils.isNotBlank(args[1])) {
        batchSize = Integer.parseInt(args[1]);
      }

      // {x}.json
      String fullFileName = terminology + ".json";
      checkIfFileExists(fullFileName);
      TerminologyLoader.indexAll(fullFileName, batchSize);

      // concepts.json, includes concepts and terms {x}-concepts.json
      fullFileName = terminology + "-concepts.json";
      checkIfFileExists(fullFileName);
      ConceptLoader.indexAll(fullFileName, batchSize);

      // metadata.json {x}-metadata.json
      fullFileName = terminology + "-metadata.json";
      checkIfFileExists(fullFileName);
      MetadataLoader.indexAll(fullFileName, batchSize);

      // relationships.json {x}-relationships.json
      fullFileName = terminology + "-relationships.json";
      checkIfFileExists(fullFileName);
      ConceptRelationshipLoader.indexAll(fullFileName, batchSize);

    } catch (final Exception e) {
      logger.error("An error occurred while loading the file.");
      e.printStackTrace();
      System.exit(1);
    }

    System.exit(0);

  }

  /**
   * Check if file exists.
   *
   * @param fullFileName the full file name
   */
  private static void checkIfFileExists(final String fullFileName) {
    if (!Files.exists(Paths.get(fullFileName))) {
      logger.error("File does not exist at " + fullFileName);
      System.exit(1);
    }
  }

}
