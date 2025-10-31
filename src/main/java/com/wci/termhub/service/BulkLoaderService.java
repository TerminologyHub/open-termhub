/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r5.model.CodeSystem;

import com.wci.termhub.algo.ProgressListener;

/**
 * The Interface BulkLoaderService.
 */
public interface BulkLoaderService {

  /**
   * Do code system load.
   *
   * @param file the file
   * @return the code system
   * @throws Exception the exception
   */
  CodeSystem doCodeSystemLoad(File file) throws Exception;

  /**
   * Start async code system load.
   *
   * @param processId the process id
   * @param file the file
   * @param listener the listener
   * @param processResultMap the process result map
   * @param processProgressMap the process progress map
   * @param processExceptionMap the process exception map
   */
  void startAsyncCodeSystemLoad(String processId, File file, ProgressListener listener,
    Map<String, List<String>> processResultMap, Map<String, Long> processProgressMap,
    Map<String, Exception> processExceptionMap);
}
