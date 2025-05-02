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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.algo.TreePositionAlgorithm;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.HasId;
import com.wci.termhub.model.Mapping;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * The Class TerminologyLoaderUtility.
 */
public final class TerminologyLoaderUtility {

  /**
   * Instantiates a new terminology loader utility.
   */
  private TerminologyLoaderUtility() {
    // private constructor
  }

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TerminologyLoaderUtility.class);

  /**
   * Load terminology.
   *
   * @param service the service
   * @param terminology the terminology
   * @throws Exception the exception
   */
  public static void loadTerminology(final EntityRepositoryService service,
    final String terminology) throws Exception {
    indexTerminology(service, terminology, 1000, -1);
  }

  /**
   * Load metadata.
   *
   * @param service the service
   * @param terminology the terminology
   * @throws Exception the exception
   */
  public static void loadMetadata(final EntityRepositoryService service, final String terminology)
    throws Exception {
    indexMetadata(service, terminology, 1000, -1);
  }

  /**
   * Load concepts.
   *
   * @param service the service
   * @param terminology the terminology
   * @throws Exception the exception
   */
  public static void loadConcepts(final EntityRepositoryService service, final String terminology)
    throws Exception {
    indexConcepts(service, terminology, 1000, -1);
  }

  /**
   * Load concept relationships.
   *
   * @param service the service
   * @param terminology the terminology
   * @param computeTreePositions the compute tree positions
   * @throws Exception the exception
   */
  public static void loadConceptRelationships(final EntityRepositoryService service,
    final String terminology, final boolean computeTreePositions) throws Exception {
    indexConceptRelationships(service, terminology, 1000, -1, computeTreePositions);
  }

  public static void loadMapset(final EntityRepositoryService service, final String terminology)
    throws Exception {
    indexMapset(service, terminology, 1000, -1);
  }

  /**
   * Index terminology.
   *
   * @param service the service
   * @param fullFileName the full file name
   * @param batchSize the batch size
   * @param limit the limit
   * @throws Exception the exception
   */
  public static void indexTerminology(final EntityRepositoryService service,
    final String fullFileName, final int batchSize, final int limit) throws Exception {

    LOGGER.debug("indexTerminology: batch size: " + batchSize + " limit: " + limit);
    final long startTime = System.currentTimeMillis();
    final List<HasId> terminologyBatch = new ArrayList<>(batchSize);

    try (final BufferedReader br = new BufferedReader(new FileReader(fullFileName))) {

      service.createIndex(Terminology.class);

      String line;
      int count = 0;
      while ((line = br.readLine()) != null) {

        final Terminology terminology = ModelUtility.fromJson(line, Terminology.class);
        LOGGER.info("indexTerminology: terminology: {}", terminology);
        terminologyBatch.add(terminology);

        if (terminologyBatch.size() == batchSize) {
          service.addBulk(Terminology.class, terminologyBatch);
          terminologyBatch.clear();
          LOGGER.info("indexTerminology: count: " + count);
        }
        count++;
      }

      if (!terminologyBatch.isEmpty()) {
        service.addBulk(Terminology.class, terminologyBatch);
      }

      LOGGER.info("indexTerminology: final count: " + count);
      LOGGER
          .info("indexTerminology: duration: " + (System.currentTimeMillis() - startTime) + " ms");

    } catch (final Exception e) {
      LOGGER.error("indexTerminology: An error occurred while processing the file.");
      throw e;
    }
  }

  /**
   * Index metadata.
   *
   * @param service the service
   * @param fullFileName the full file name
   * @param batchSize the batch size
   * @param limit the limit
   * @throws Exception the exception
   */
  public static void indexMetadata(final EntityRepositoryService service, final String fullFileName,
    final int batchSize, final int limit) throws Exception {

    LOGGER.debug("indexMetadata: batch size: " + batchSize + " limit: " + limit);
    final long startTime = System.currentTimeMillis();
    final List<HasId> metadataBatch = new ArrayList<>(batchSize);

    try (final BufferedReader br = new BufferedReader(new FileReader(fullFileName))) {

      service.createIndex(Metadata.class);

      String line;
      int conceptCount = 0;
      while ((line = br.readLine()) != null && (limit == -1 || conceptCount < limit)) {

        final Metadata metadata = ModelUtility.fromJson(line, Metadata.class);
        metadataBatch.add(metadata);

        if (metadataBatch.size() == batchSize) {
          service.addBulk(Metadata.class, metadataBatch);
          metadataBatch.clear();
          LOGGER.info("indexMetadata: count: " + conceptCount);
        }
        conceptCount++;
      }

      if (!metadataBatch.isEmpty()) {
        service.addBulk(Metadata.class, metadataBatch);
      }

      LOGGER.info("indexMetadata: final metadataBatch added count: " + conceptCount);
      LOGGER.info("indexMetadata: duration: " + (System.currentTimeMillis() - startTime) + " ms");

    } catch (final Exception e) {
      LOGGER.error("indexMetadata: An error occurred while processing the file.");
      throw e;
    }

  }

  /**
   * Index concept.
   *
   * @param service the service
   * @param fullFileName the full file name
   * @param batchSize the batch size
   * @param limit the limit
   * @throws Exception the exception
   */
  public static void indexConcepts(final EntityRepositoryService service, final String fullFileName,
    final int batchSize, final int limit) throws Exception {

    LOGGER.debug("indexConcept: batch size: " + batchSize + " limit: " + limit);
    final long startTime = System.currentTimeMillis();
    final List<HasId> conceptBatch = new ArrayList<>(batchSize);
    final List<HasId> termBatch = new ArrayList<>(batchSize);

    try (final BufferedReader br = new BufferedReader(new FileReader(fullFileName))) {

      service.createIndex(Concept.class);
      service.createIndex(Term.class);

      String line;
      int conceptCount = 0;
      int termCount = 0;
      while ((line = br.readLine()) != null && (limit == -1 || conceptCount < limit)) {

        final Concept concept = ModelUtility.fromJson(line, Concept.class);
        if (concept.getTerms() != null) {
          for (final Term term : concept.getTerms()) {
            termBatch.add(term);
            if (termBatch.size() == batchSize) {
              service.addBulk(Term.class, termBatch);
              termBatch.clear();
            }
            termCount++;
          }
        }
        conceptBatch.add(concept);

        if (conceptBatch.size() == batchSize) {
          service.addBulk(Concept.class, conceptBatch);
          conceptBatch.clear();
          LOGGER.info("indexConcept: count: " + conceptCount);
        }
        conceptCount++;
      }

      if (!conceptBatch.isEmpty()) {
        service.addBulk(Concept.class, conceptBatch);
      }
      if (!termBatch.isEmpty()) {
        service.addBulk(Term.class, termBatch);
      }

      LOGGER.info("indexConcept: final concepts added count: " + conceptCount);
      LOGGER.info("indexConcept: final terms added count: " + termCount);
      LOGGER.info("indexConcept: duration: " + (System.currentTimeMillis() - startTime) + " ms");

    } catch (final Exception e) {
      LOGGER.error("indexConcept: An error occurred while processing the file.");
      throw e;
    }
  }

  /**
   * Index concept relationships.
   *
   * @param service the service
   * @param fullFileName the full file name
   * @param batchSize the batch size
   * @param limit the limit
   * @param computeTreePositions the compute tree positions
   * @throws Exception the exception
   */
  public static void indexConceptRelationships(final EntityRepositoryService service,
    final String fullFileName, final int batchSize, final int limit,
    final boolean computeTreePositions) throws Exception {

    LOGGER.debug("indexConceptRelationships: batch size: {} limit: {}", batchSize, limit);
    final long startTime = System.currentTimeMillis();
    final List<HasId> conceptRelBatch = new ArrayList<>(batchSize);
    String terminology = "";
    String publisher = "";
    String version = "";
    boolean hasMetadata = false;

    try (final BufferedReader br = new BufferedReader(new FileReader(fullFileName))) {

      service.createIndex(ConceptRelationship.class);

      String line;
      int count = 0;
      while ((line = br.readLine()) != null && (limit == -1 || count < limit)) {

        final ConceptRelationship conceptRel =
            ModelUtility.fromJson(line, ConceptRelationship.class);

        if (computeTreePositions && !hasMetadata) {
          terminology = conceptRel.getTerminology();
          publisher = conceptRel.getPublisher();
          version = conceptRel.getVersion();
          hasMetadata = true;
        }

        conceptRelBatch.add(conceptRel);

        if (conceptRelBatch.size() == batchSize) {
          service.addBulk(ConceptRelationship.class, conceptRelBatch);
          conceptRelBatch.clear();
          LOGGER.info("indexConceptRelationships: count: {}", count);
        }
        count++;
      }

      if (!conceptRelBatch.isEmpty()) {
        service.addBulk(ConceptRelationship.class, conceptRelBatch);
      }

      LOGGER.info("indexConceptRelationships: final count: {}", count);
      LOGGER.info("indexConceptRelationships: duration: {} ms",
          (System.currentTimeMillis() - startTime));

      if (computeTreePositions) {

        computeConceptTreePositions(service, terminology, publisher, version);
      }

    } catch (final Exception e) {
      LOGGER.error("An error occurred while processing the file.");
      throw e;
    }
  }

  /**
   * Index mappings.
   *
   * @param service the service
   * @param fullFileName the full file name
   * @param batchSize the batch size
   * @param limit the limit
   * @throws Exception the exception
   */
  public static void indexMapset(final EntityRepositoryService service, final String fullFileName,
    final int batchSize, final int limit) throws Exception {

    LOGGER.debug("indexMapset: batch size: {} limit: {}", batchSize, limit);
    final long startTime = System.currentTimeMillis();
    final List<HasId> mappingBatch = new ArrayList<>(batchSize);

    try (final BufferedReader br = new BufferedReader(new FileReader(fullFileName))) {

      service.createIndex(Mapset.class);
      service.createIndex(Mapping.class);

      String line;
      int mappingCount = 0;
      while ((line = br.readLine()) != null && (limit == -1 || mappingCount < limit)) {

        final Mapping mapping = ModelUtility.fromJson(line, Mapping.class);
        mappingBatch.add(mapping);

        if (mappingBatch.size() == batchSize) {
          service.addBulk(Concept.class, mappingBatch);
          mappingBatch.clear();
          LOGGER.info("indexMappings: count: {}", mappingCount);
        }
        mappingCount++;
      }

      if (!mappingBatch.isEmpty()) {
        service.addBulk(Mapset.class, mappingBatch);
      }

      LOGGER.info("indexMappings: final mappings added count: {}", mappingBatch);
      LOGGER.info("indexMappings: duration: {} ms", System.currentTimeMillis() - startTime);

    } catch (final Exception e) {
      LOGGER.error("indexMappings: An error occurred while processing the file.");
      throw e;
    }
  }

  /**
   * Compute concept tree positions.
   *
   * @param service the service
   * @param terminology the terminology
   * @param publisher the publisher
   * @param version the version
   * @throws Exception the exception
   */
  private static void computeConceptTreePositions(final EntityRepositoryService service,
    final String terminology, final String publisher, final String version) throws Exception {

    final TreePositionAlgorithm treepos = new TreePositionAlgorithm(service);
    treepos.setTerminology(terminology);
    treepos.setPublisher(publisher);
    treepos.setVersion(version);
    treepos.checkPreconditions();
    treepos.compute();

  }
}
