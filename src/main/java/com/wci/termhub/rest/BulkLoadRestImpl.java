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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wci.termhub.EnablePostLoadComputations;
import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.algo.ProgressEvent;
import com.wci.termhub.algo.ProgressListener;
import com.wci.termhub.fhir.rest.r5.FhirUtilityR5;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.CodeSystemLoaderUtil;
import com.wci.termhub.util.ConceptMapLoaderUtil;
import com.wci.termhub.util.ValueSetLoaderUtil;

import ca.uhn.fhir.context.FhirContext;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The bulk loader.
 */
@RestController
@RequestMapping("/fhir")
@CrossOrigin(origins = "*")
public class BulkLoadRestImpl {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(BulkLoadRestImpl.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The enable post load computations. */
  @Autowired
  private EnablePostLoadComputations enablePostLoadComputations;

  /** The process resource map. */
  private static Map<String, List<String>> processResultMap = new HashMap<>();

  /** The process progress map. */
  private static Map<String, Long> processProgressMap = new HashMap<>();

  /** The process exception map. */
  private static Map<String, Exception> processExceptionMap = new HashMap<>();

  /**
   * Gets the progress.
   *
   * @param type the type
   * @param processId the process id
   * @return the progress
   * @throws Exception the exception
   */
  @Hidden
  @RequestMapping(value = "/{type}/$load/{processId}/progress", method = RequestMethod.GET,
      produces = "text/plain")
  @Operation(summary = "Get process progress",
      description = "Gets progress for the specified process id", tags = {
          "load"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Sucess finding progress"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "type",
          description = "resource type, e.g. CodeSystem, ConceptMap, ValueSet, Bundle",
          required = true),
      @Parameter(name = "processId", description = "process id, e.g. <uuid>")
  })
  public ResponseEntity<String> getProcessProgress(@PathVariable("type") final String type,
    @PathVariable("processId") final String processId) throws Exception {

    if (!processProgressMap.containsKey(processId)) {
      throw FhirUtilityR5.exception("No progress found for process id = " + processId,
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_NOT_FOUND);
    }

    logger.info("  progress " + processId + " = " + processProgressMap.get(processId));
    return new ResponseEntity<String>("" + processProgressMap.get(processId), null, 200);

  }

  /**
   * Gets the process error.
   *
   * @param type the type
   * @param processId the process id
   * @return the process error
   * @throws Exception the exception
   */
  @Hidden
  @RequestMapping(value = "/{type}/$load/{processId}/error", method = RequestMethod.GET,
      produces = "text/plain")
  @Operation(summary = "Get process error", description = "Gets error for the specified process id",
      tags = {
          "load"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Sucess finding error"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "type",
          description = "resource type, e.g. CodeSystem, ConceptMap, ValueSet, Bundle",
          required = true),
      @Parameter(name = "processId", description = "process id, e.g. <uuid>")
  })
  public ResponseEntity<String> getProcessError(@PathVariable("type") final String type,
    @PathVariable("processId") final String processId) throws Exception {

    if (!processExceptionMap.containsKey(processId)) {
      throw FhirUtilityR5.exception("No exception found for process id = " + processId,
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_NOT_FOUND);
    }

    return new ResponseEntity<String>(processExceptionMap.get(processId).getMessage(), null, 200);

  }

  /**
   * Gets the process result.
   *
   * @param type the type
   * @param processId the process id
   * @return the process result
   * @throws Exception the exception
   */
  @Hidden
  @RequestMapping(value = "/{type}/$load/{processId}/result", method = RequestMethod.GET,
      produces = "text/plain")
  @Operation(summary = "Get process result",
      description = "Gets result for the specified process id", tags = {
          "load"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Sucess finding result"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "type",
          description = "resource type, e.g. CodeSystem, ConceptMap, ValueSet, Bundle",
          required = true),
      @Parameter(name = "processId", description = "process id, e.g. <uuid>")
  })
  public ResponseEntity<String> getProcessResult(@PathVariable("type") final String type,
    @PathVariable("processId") final String processId) throws Exception {

    if (!processResultMap.containsKey(processId)) {
      throw FhirUtilityR5.exception("No result found for process id = " + processId,
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_NOT_FOUND);
    }

    return new ResponseEntity<String>(processResultMap.get(processId).toString(), null, 200);

  }

  /**
   * Load code system.
   *
   * @param background the background
   * @param resourceFile the resource file
   * @return the response entity
   * @throws Exception the exception
   */
  @RequestMapping(value = "/CodeSystem/$load", method = RequestMethod.POST,
      consumes = "multipart/form-data")
  @Operation(summary = "Load CodeSystem data",
      description = "Loads CodeSystem data from the specified file.", tags = {
          "load"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Successful load of data (or processId if background=true used)"),
      @ApiResponse(responseCode = "409", description = "Conflict", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "background",
          description = "true/false value indicating whether to run the load in the background."
              + "If 'true', the endpoint returns a process id.",
          required = false, hidden = true),
      @Parameter(name = "resource",
          description = "Multi-part form data part containing the code system data",
          required = true)
  })
  @RequestBody(description = "Multi-part form data", required = true,
      content = @Content(schema = @Schema(implementation = String.class), examples = {
          @ExampleObject(value = "resource={\"resourceType\":\"CodeSystem\",...}")
      }))
  public ResponseEntity<String> loadCodeSystem(
    @RequestParam(name = "background", required = false) final boolean background,
    @RequestPart("resource") final MultipartFile resourceFile) throws Exception {
    // MultipartFile resourceFile
    try {
      logger.info("Load code system");

      // Write to a file so we can re-open streams against it
      final File file = File.createTempFile("tmp", ".json");
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

              BulkLoadRestImpl.processResultMap.put(processId, new ArrayList<>());
              BulkLoadRestImpl.processResultMap.get(processId)
                  .add("CodeSystem/" + codeSystem.getId());

            } catch (final Exception e) {
              processProgressMap.put(processId, -1L);
              processExceptionMap.put(processId, e);

            }
          }
        });
        t.start();

        // Return the process id
        return new ResponseEntity<>(processId, null, 200);

      }

    } catch (final FHIRServerResponseException fe) {
      throw fe;
    } catch (final Exception e) {
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

  @RequestMapping(value = "/ValueSet/$load", method = RequestMethod.POST,
      consumes = "multipart/form-data")
  @Operation(summary = "Load ValueSet data",
      description = "Loads ValueSet data from the specified file.", tags = {
          "load"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Successful load of data (or processId if background=true used)"),
      @ApiResponse(responseCode = "409", description = "Conflict", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "background",
          description = "true/false value indicating whether to run the load in the background."
              + "If 'true', the endpoint returns a process id.",
          required = false, hidden = true),
      @Parameter(name = "resource",
          description = "Multi-part form data part containing the code system data",
          required = true)
  })
  @RequestBody(description = "Multi-part form data", required = true,
      content = @Content(schema = @Schema(implementation = String.class), examples = {
          @ExampleObject(value = "resource={\"resourceType\":\"ValueSet\",...}")
      }))
  public ResponseEntity<String> loadValueSet(
    @RequestParam(name = "background", required = false) final boolean background,
    @RequestPart("resource") final MultipartFile resourceFile) throws Exception {
    // MultipartFile resourceFile
    try {
      logger.info("Load ValueSet");

      // Write to a file so we can re-open streams against it
      final File file = File.createTempFile("tmp", ".json");
      FileUtils.writeByteArrayToFile(file, resourceFile.getBytes());

      // If not running in the background -> load and return the resource
      if (!background) {
        // Use existing loader utility
        final ValueSet valueSet = ValueSetLoaderUtil.loadValueSet(searchService, file,
            ValueSet.class, new DefaultProgressListener());

        FileUtils.delete(file);

        return new ResponseEntity<>(
            FhirContext.forR5().newJsonParser().encodeResourceToString(valueSet), null, 200);

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
              final ValueSet valueSet =
                  ValueSetLoaderUtil.loadValueSet(searchService, file, ValueSet.class, listener);

              FileUtils.delete(file);

              BulkLoadRestImpl.processResultMap.put(processId, new ArrayList<>());
              BulkLoadRestImpl.processResultMap.get(processId).add("ValueSet/" + valueSet.getId());

            } catch (final Exception e) {
              processExceptionMap.put(processId, e);

            }
          }
        });
        t.start();

        // Return the process id
        return new ResponseEntity<>(processId, null, 200);

      }

    } catch (final FHIRServerResponseException fe) {
      throw fe;

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
  @RequestMapping(value = "/ConceptMap/$load", method = RequestMethod.POST,
      consumes = "multipart/form-data")
  @Operation(summary = "Load ConceptMap data",
      description = "Loads ConceptMap data from the specified file.", tags = {
          "load"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Successful load of data (or processId if background=true used)"),
      @ApiResponse(responseCode = "409", description = "Conflict", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "background",
          description = "true/false value indicating whether to run the load in the background."
              + "If 'true', the endpoint returns a process id.",
          required = false),
      @Parameter(name = "resource",
          description = "Multi-part form data part containing the code system data",
          required = true)
  })
  @RequestBody(description = "Multi-part form data", required = true,
      content = @Content(schema = @Schema(implementation = String.class), examples = {
          @ExampleObject(value = "resource={\"resourceType\":\"ConceptMap\",...}")
      }))
  public ResponseEntity<String> loadConceptMap(
    @RequestParam(name = "background", required = false) final boolean background,
    @RequestPart("resource") final MultipartFile resourceFile) throws Exception {
    // MultipartFile resourceFile
    try {
      logger.info("Load ConceptMap");

      // Write to a file so we can re-open streams against it
      final File file = File.createTempFile("tmp", ".json");
      FileUtils.writeByteArrayToFile(file, resourceFile.getBytes());

      // If not running in the background -> load and return the resource
      if (!background) {
        // Use existing loader utility
        final ConceptMap conceptMap = ConceptMapLoaderUtil.loadConceptMap(searchService, file,
            ConceptMap.class, new DefaultProgressListener());

        FileUtils.delete(file);

        return new ResponseEntity<>(
            FhirContext.forR5().newJsonParser().encodeResourceToString(conceptMap), null, 200);

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
              final ConceptMap conceptMap = ConceptMapLoaderUtil.loadConceptMap(searchService, file,
                  ConceptMap.class, listener);

              FileUtils.delete(file);

              BulkLoadRestImpl.processResultMap.put(processId, new ArrayList<>());
              BulkLoadRestImpl.processResultMap.get(processId)
                  .add("ConceptMap/" + conceptMap.getId());

            } catch (final Exception e) {
              processExceptionMap.put(processId, e);

            }
          }
        });
        t.start();

        // Return the process id
        return new ResponseEntity<>(processId, null, 200);

      }

    } catch (final FHIRServerResponseException fe) {
      throw fe;

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
  @Hidden
  @RequestMapping(value = "/Bundle/$load", method = RequestMethod.POST,
      consumes = "multipart/form-data")
  @Operation(summary = "Load Bundle data",
      description = "Loads Bundle data from the specified file.", tags = {
          "load"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Successful load of data (or processId if background=true used)"),
      @ApiResponse(responseCode = "409", description = "Conflict", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "background",
          description = "true/false value indicating whether to run the load in the background."
              + "If 'true', the endpoint returns a process id.",
          required = false),
      @Parameter(name = "resource",
          description = "Multi-part form data part containing the code system data",
          required = true)
  })
  @RequestBody(description = "Multi-part form data", required = true,
      content = @Content(schema = @Schema(implementation = String.class), examples = {
          @ExampleObject(value = "resource={\"resourceType\":\"Bundle\",...}")
      }))
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

      // // Use existing loader utility
      // final ConceptMap conceptMap =
      // ConceptMapLoaderUtil.loadConceptMap(searchService, file, ConceptMap.class);
      //
      // FileUtils.delete(file);

      return new ResponseEntity<>("Coming soon", null, 200);

    } catch (final Exception e) {
      logger.error("Unexpected error creating concept map", e);
      throw FhirUtilityR5.exception("Failed to create concept map = " + e.getMessage(),
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  // TDOO: update tutorial1
}
