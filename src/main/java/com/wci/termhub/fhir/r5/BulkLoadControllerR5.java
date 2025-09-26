/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r5;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wci.termhub.EnablePostLoadComputations;
import com.wci.termhub.fhir.rest.r5.FhirUtilityR5;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.CodeSystemLoaderUtil;

import ca.uhn.fhir.context.FhirContext;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The CodeSystem provider.
 */
@RestController
@RequestMapping("/fhir/r5")
public class BulkLoadControllerR5 {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(BulkLoadControllerR5.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The enable post load computations. */
  @Autowired
  private EnablePostLoadComputations enablePostLoadComputations;

  /**
   * Load code system.
   *
   * @param resourceFile the resource file
   * @return the response entity
   * @throws Exception the exception
   */
  @PostMapping(path = "/CodeSystem/$load", consumes = "multipart/form-data")
  public ResponseEntity<String> loadCodeSystem(
    @RequestPart("resource") final MultipartFile resourceFile) throws Exception {
    // MultipartFile resourceFile
    try {
      logger.info("Load code system R5");

      // Write to a file so we can re-open streams against it
      final File file = File.createTempFile("tmp", ".json");
      // try (FileOutputStream outputStream = new FileOutputStream(file)) {
      // IOUtils.copy(request.getInputStream(), outputStream);
      // }
      FileUtils.writeByteArrayToFile(file, resourceFile.getBytes());

      // Use existing loader utility
      final CodeSystem codeSystem = CodeSystemLoaderUtil.loadCodeSystem(searchService, file,
          enablePostLoadComputations.isEnabled(), CodeSystem.class);

      FileUtils.delete(file);

      return new ResponseEntity<>(
          FhirContext.forR5().newJsonParser().encodeResourceToString(codeSystem), null, 200);

    } catch (final Exception e) {
      logger.error("Unexpected error creating code system", e);
      throw FhirUtilityR5.exception("Failed to create code system: " + e.getMessage(),
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

}
