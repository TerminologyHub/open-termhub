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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.wci.termhub.EnablePostLoadComputations;
import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.algo.ProgressEvent;
import com.wci.termhub.algo.ProgressListener;
import com.wci.termhub.fhir.rest.r5.FhirUtilityR5;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.CodeSystemLoaderUtil;
import com.wci.termhub.util.ConceptMapLoaderUtil;
import com.wci.termhub.util.ThreadLocalMapper;
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

  @Autowired
    private com.wci.termhub.service.BulkLoaderService bulkLoaderService;

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
  public ResponseEntity<String> loadCodeSystem(@Parameter(hidden = true)
  @RequestParam(name = "background", required = false) final boolean background,
    @RequestPart("resource") final MultipartFile resourceFile) throws Exception {
    // MultipartFile resourceFile
    try {
      logger.info("Load code system");

      // Write to a file so we can re-open streams against it
      final File file = File.createTempFile("tmp", ".json");
      try (var in = resourceFile.getInputStream(); var out = FileUtils.openOutputStream(file)) {
        in.transferTo(out);
      }

      // If not running in the background -> load and return the resource
      if (!background) {
        // Use existing loader utility
        final CodeSystem codeSystem = bulkLoaderService.doCodeSystemLoad(file);

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
        bulkLoaderService.startAsyncCodeSystemLoad(processId, file, listener, processResultMap, processProgressMap, processExceptionMap);

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
  public ResponseEntity<String> loadValueSet(@Parameter(hidden = true)
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
          required = false, hidden = true),
      @Parameter(name = "resource",
          description = "Multi-part form data part containing the code system data",
          required = true)
  })
  @RequestBody(description = "Multi-part form data", required = true,
      content = @Content(schema = @Schema(implementation = String.class), examples = {
          @ExampleObject(value = "resource={\"resourceType\":\"ConceptMap\",...}")
      }))
  public ResponseEntity<String> loadConceptMap(@Parameter(hidden = true)
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
      // @Parameter(name = "background",
      // description = "true/false value indicating whether to run the load in the background."
      // + "If 'true', the endpoint returns a process id.",
      // required = false),
      @Parameter(name = "resource",
          description = "Multi-part form data part containing the code system data",
          required = true)
  })
  @RequestBody(description = "Multi-part form data", required = true,
      content = @Content(schema = @Schema(implementation = String.class), examples = {
          @ExampleObject(value = "resource={\"resourceType\":\"Bundle\",...}")
      }))
  public ResponseEntity<String> loadBundle(@Parameter(hidden = true)
  @RequestParam(name = "background", required = false) final boolean background,
    @RequestPart("resource") final MultipartFile resourceFile) throws Exception {
    // MultipartFile resourceFile
    try {
      logger.info("Load Bundle");

      // Write to a file so we can re-open streams against it
      final File file = File.createTempFile("tmp", ".json");
      FileUtils.writeByteArrayToFile(file, resourceFile.getBytes());
      final List<String> results = new ArrayList<>();

      if (!background) {

        handleBundle(searchService, new DefaultProgressListener(), file, results);
        FileUtils.delete(file);

        return new ResponseEntity<>(results.toString(), null, 200);
      }

      // If running in the background, load and return the process id
      else {
        final String processId = UUID.randomUUID().toString();
        BulkLoadRestImpl.processResultMap.put(processId, results);

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
              handleBundle(searchService, listener, file, results);
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
   * Handle bundle.
   *
   * @param searchService the search service
   * @param listener the listener
   * @param file the file
   * @param results the results
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  private void handleBundle(final EntityRepositoryService searchService,
    final ProgressListener listener, final File file, final List<String> results) throws Exception {

    // Get the entry count for progress monitoring
    long totalEntryCount = 0;

    // Use try-with-resources to ensure the JsonParser is safely closed
    try (final JsonParser parser = ThreadLocalMapper.get().getFactory().createParser(file)) {

      // We iterate through all tokens until the end of the file
      while (parser.nextToken() != null) {

        // 1. Check for the "entry" field name
        if (parser.currentToken() == JsonToken.FIELD_NAME && "entry".equals(parser.getText())) {

          // Advance the parser one token: it should now be at the start of the array ([)
          if (parser.nextToken() != JsonToken.START_ARRAY) {
            // Handle case where "entry" field is not followed by an array (e.g., malformed FHIR)
            continue;
          }

          // 2. We are now inside the "entry" array. Loop until the END_ARRAY (]) token.
          while (parser.nextToken() != JsonToken.END_ARRAY) {

            // 3. Check for the start of a new resource object ({). This marks a new entry.
            if (parser.currentToken() == JsonToken.START_OBJECT) {
              totalEntryCount++;

              // 4. Skip the entire resource object now that we have counted it.
              // This is the CRITICAL step for low memory usage.
              parser.skipChildren();
            }
          }

          // We found and counted the entries, so we can stop processing the file.
          break;
        }
      }
    }

    int entryCount = 0;

    // Go through the bundle and determine which resource type we're on
    // save that to a file and make the appropriate loader call
    try (JsonParser parser = ThreadLocalMapper.get().getFactory().createParser(file)) {

      // You can iterate through the tokens one by one
      while (parser.nextToken() != null) {

        // Get the current token
        final JsonToken token = parser.currentToken();

        // Example: Find the start of a nested JSON array
        if (token == JsonToken.START_ARRAY && "entry".equals(parser.currentName())) {

          // Process each object within the array without loading the whole array into memory
          while (parser.nextToken() != JsonToken.END_ARRAY) {

            // Progress update
            entryCount++;
            listener.updateProgress(new ProgressEvent((int) (entryCount * 1.0 / totalEntryCount)));

            // Use ObjectMapper to read the sub-object as a JsonNode
            final JsonNode entryNode = parser.readValueAsTree();
            if (entryNode.has("resource")) {
              final String resourceType =
                  entryNode.get("resource").get("resourceType").asText(null);
              logger.info("  resource = " + resourceType);

              final File tmpResourceFile = File.createTempFile("tmp", ".json");
              ThreadLocalMapper.get().writeValue(tmpResourceFile, entryNode.get("resource"));

              switch (resourceType) {
                case "CodeSystem":
                  final CodeSystem codeSystem = CodeSystemLoaderUtil.loadCodeSystem(searchService,
                      tmpResourceFile, enablePostLoadComputations.isEnabled(), CodeSystem.class,
                      new DefaultProgressListener());
                  results.add("CodeSystem/" + codeSystem.getId());
                  break;

                case "ValueSet":
                  // Use existing loader utility
                  final ValueSet valueSet = ValueSetLoaderUtil.loadValueSet(searchService,
                      tmpResourceFile, ValueSet.class, new DefaultProgressListener());
                  results.add("ValueSet/" + valueSet.getId());
                  break;

                case "ConceptMap":
                  // Use existing loader utility
                  final ConceptMap conceptMap = ConceptMapLoaderUtil.loadConceptMap(searchService,
                      tmpResourceFile, ConceptMap.class, new DefaultProgressListener());
                  results.add("ConceptMap/" + conceptMap.getId());
                  break;

                default:
                  logger.info("    SKIP unhandled resource");
                  continue;

              }

            }

          }
        }
      }
    }
  }

}
