/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.ConceptMap;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wci.termhub.EnablePostLoadComputations;
import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.algo.ProgressEvent;
import com.wci.termhub.algo.ProgressListener;
import com.wci.termhub.fhir.rest.r5.FhirUtilityR5;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.CodeSystemLoaderUtil;
import com.wci.termhub.util.ConceptMapLoaderUtil;
import com.wci.termhub.util.ValueSetLoaderUtil;

import ca.uhn.fhir.context.FhirContext;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The bulk loader.
 */
@RestController
@RequestMapping("/fhir")
public class BulkLoadController {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(BulkLoadController.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The enable post load computations. */
  @Autowired
  private EnablePostLoadComputations enablePostLoadComputations;

  /** The process resource map. */
  private static Map<String, List<String>> processResourceMap = new HashMap<>();

  /** The process progress map. */
  private static Map<String, Long> processProgressMap = new HashMap<>();

  /** The process exception map. */
  private static Map<String, Exception> processExceptionMap = new HashMap<>();

  /**
   * Load code system.
   *
   * @param background the background
   * @param resourceFile the resource file
   * @return the response entity
   * @throws Exception the exception
   */
  @PostMapping(path = "/CodeSystem/$load", consumes = "multipart/form-data")
  public ResponseEntity<String> loadCodeSystem(
    @RequestParam(name = "background", required = false) final boolean background,
    @RequestPart("resource") final MultipartFile resourceFile) throws Exception {
    // MultipartFile resourceFile
    try {
      logger.info("Load code system");

      // Write to a file so we can re-open streams against it
      final File file = File.createTempFile("tmp", ".json");
      // try (FileOutputStream outputStream = new FileOutputStream(file)) {
      // IOUtils.copy(request.getInputStream(), outputStream);
      // }
      FileUtils.writeByteArrayToFile(file, resourceFile.getBytes());

      // If not running in the background -> load and return the resource
      if (!background) {
        // Use existing loader utility
        final CodeSystem codeSystem = CodeSystemLoaderUtil.loadCodeSystem(searchService, file,
            enablePostLoadComputations.isEnabled(), CodeSystem.class,
            new DefaultProgressListener());

        FileUtils.delete(file);

        return new ResponseEntity<>(
            FhirContext.forR5().newJsonParser().encodeResourceToString(codeSystem), null, 200);

      }

      // If running in the background, load and return the process id
      else {
        final String processId = UUID.randomUUID().toString();
        final ProgressListener listener = new ProgressListener() {

          /**
           * Update progress.
           *
           * @param event the event
           */
          @Override
          public void updateProgress(final ProgressEvent event) {
            if (!processProgressMap.containsKey(processId)
                || event.getProgress() > processProgressMap.get(processId)) {
              processProgressMap.put(processId, event.getProgress());
            }
          }
        };

        final Thread t = new Thread(new Runnable() {

          /* see superclass */
          @Override
          public void run() {
            try {
              // Use existing loader utility
              final CodeSystem codeSystem = CodeSystemLoaderUtil.loadCodeSystem(searchService, file,
                  enablePostLoadComputations.isEnabled(), CodeSystem.class, listener);

              FileUtils.delete(file);

              BulkLoadController.processResourceMap.put(processId, new ArrayList<>());
              BulkLoadController.processResourceMap.get(processId)
                  .add("CodeSystem/" + codeSystem.getId());

            } catch (Exception e) {
              processExceptionMap.put(processId, e);

            }
          }
        });
        t.start();

        // Return the process id
        return new ResponseEntity<>(processId, null, 200);

      }

    } catch (

    final Exception e) {
      logger.error("Unexpected error creating code system", e);
      throw FhirUtilityR5.exception("Failed to create code system = " + e.getMessage(),
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Load value set.
   *
   * @param background the background
   * @param resourceFile the resource file
   * @return the response entity
   * @throws Exception the exception
   */
  @PostMapping(path = "/ValueSet/$load", consumes = "multipart/form-data")
  public ResponseEntity<String> loadValueSet(
    @RequestParam(name = "background", required = false) final boolean background,
    @RequestPart("resource") final MultipartFile resourceFile) throws Exception {
    // MultipartFile resourceFile
    try {
      logger.info("Load ValueSet");

      // Write to a file so we can re-open streams against it
      final File file = File.createTempFile("tmp", ".json");
      // try (FileOutputStream outputStream = new FileOutputStream(file)) {
      // IOUtils.copy(request.getInputStream(), outputStream);
      // }
      FileUtils.writeByteArrayToFile(file, resourceFile.getBytes());

      // Use existing loader utility
      final ValueSet valueSet = ValueSetLoaderUtil.loadValueSet(searchService, file, ValueSet.class);

      FileUtils.delete(file);

      return new ResponseEntity<>(
          FhirContext.forR5().newJsonParser().encodeResourceToString(valueSet), null, 200);

    } catch (final Exception e) {
      logger.error("Unexpected error creating value set", e);
      throw FhirUtilityR5.exception("Failed to create value set = " + e.getMessage(),
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Load concept map.
   *
   * @param background the background
   * @param resourceFile the resource file
   * @return the response entity
   * @throws Exception the exception
   */
  @PostMapping(path = "/ConceptMap/$load", consumes = "multipart/form-data")
  public ResponseEntity<String> loadConceptMap(
    @RequestParam(name = "background", required = false) final boolean background,
    @RequestPart("resource") final MultipartFile resourceFile) throws Exception {
    // MultipartFile resourceFile
    try {
      logger.info("Load ConceptMap");

      // Write to a file so we can re-open streams against it
      final File file = File.createTempFile("tmp", ".json");
      // try (FileOutputStream outputStream = new FileOutputStream(file)) {
      // IOUtils.copy(request.getInputStream(), outputStream);
      // }
      FileUtils.writeByteArrayToFile(file, resourceFile.getBytes());

      // Use existing loader utility
      final ConceptMap conceptMap =
          ConceptMapLoaderUtil.loadConceptMap(searchService, file, ConceptMap.class);

      FileUtils.delete(file);

      return new ResponseEntity<>(
          FhirContext.forR5().newJsonParser().encodeResourceToString(conceptMap), null, 200);

    } catch (final Exception e) {
      logger.error("Unexpected error creating concept map", e);
      throw FhirUtilityR5.exception("Failed to create concept map = " + e.getMessage(),
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Load bundle.
   *
   * @param background the background
   * @param resourceFile the resource file
   * @return the response entity
   * @throws Exception the exception
   */
  @PostMapping(path = "/Bundle/$load", consumes = "multipart/form-data")
  public ResponseEntity<String> loadBundle(
    @RequestParam(name = "background", required = false) final boolean background,
    @RequestPart("resource") final MultipartFile resourceFile) throws Exception {
    // MultipartFile resourceFile
    try {
      logger.info("Load Bundle");

      // Write to a file so we can re-open streams against it
      final File file = File.createTempFile("tmp", ".json");
      // try (FileOutputStream outputStream = new FileOutputStream(file)) {
      // IOUtils.copy(request.getInputStream(), outputStream);
      // }
      FileUtils.writeByteArrayToFile(file, resourceFile.getBytes());

      // Go through the bundle and determine which resource type we're on
      // save that to a file and make the appropriate loader call
      // Use existing loader utility
      final ConceptMap conceptMap =
          ConceptMapLoaderUtil.loadConceptMap(searchService, file, ConceptMap.class);

      FileUtils.delete(file);

      return new ResponseEntity<>(
          FhirContext.forR5().newJsonParser().encodeResourceToString(conceptMap), null, 200);

    } catch (final Exception e) {
      logger.error("Unexpected error creating concept map", e);
      throw FhirUtilityR5.exception("Failed to create concept map = " + e.getMessage(),
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  // TDOO: update tutorial1
}
