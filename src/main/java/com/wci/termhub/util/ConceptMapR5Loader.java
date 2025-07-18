/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import java.io.BufferedReader;
import java.io.FileReader;

import org.hl7.fhir.r5.model.ConceptMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.fhir.r5.ConceptMapProviderR5;
import com.wci.termhub.service.EntityRepositoryService;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * The Class ConceptMapR5Loader.
 */
public final class ConceptMapR5Loader {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConceptMapR5Loader.class);

  /** The FHIR context. */
  private static final FhirContext FHIR_CONTEXT = FhirContext.forR5();

  /**
   * Instantiates a new concept map R5 loader.
   */
  private ConceptMapR5Loader() {
    // n/a
  }

  /**
   * Load concept map from a JSON file and save it using the repository service.
   *
   * @param service the repository service to use for saving
   * @param provider the ConceptMapProviderR5 instance to use
   * @param fullFileName the path to the JSON file
   * @throws Exception if there is an error reading or processing the file
   */
  public static void loadConceptMap(final EntityRepositoryService service,
    final ConceptMapProviderR5 provider, final String fullFileName) throws Exception {
    indexConceptMap(service, provider, fullFileName, 1000, -1);
  }

  /**
   * Index concept map.
   *
   * @param service the service
   * @param provider the provider
   * @param fullFileName the full file name
   * @param batchSize the batch size
   * @param limit the limit
   * @throws Exception the exception
   */
  private static void indexConceptMap(final EntityRepositoryService service,
    final ConceptMapProviderR5 provider, final String fullFileName, final int batchSize,
    final int limit) throws Exception {

    LOGGER.debug("indexConceptMap: batch size: {} limit: {}", batchSize, limit);
    final long startTime = System.currentTimeMillis();

    try (final BufferedReader br = new BufferedReader(new FileReader(fullFileName))) {
      // Parse JSON to FHIR ConceptMap
      final String json = br.lines().reduce("", String::concat);
      final IParser parser = FHIR_CONTEXT.newJsonParser();
      final ConceptMap conceptMap = parser.parseResource(ConceptMap.class, json);

      // Create and save the ConceptMap using the provided provider instance
      provider.createConceptMap(null, null, conceptMap);

      LOGGER.info("Successfully loaded ConceptMap from file: {}", fullFileName);
    } catch (final Exception e) {
      LOGGER.error("Error loading ConceptMap from file: {}", fullFileName, e);
      throw e;
    } finally {
      LOGGER.info("indexConceptMap: duration: {}: ms", (System.currentTimeMillis() - startTime));
    }
  }

}
