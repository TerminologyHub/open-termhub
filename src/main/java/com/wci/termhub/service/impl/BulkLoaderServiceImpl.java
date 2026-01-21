/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r5.model.CodeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.wci.termhub.EnablePostLoadComputations;
import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.algo.ProgressListener;
import com.wci.termhub.lucene.eventing.Write;
import com.wci.termhub.service.BulkLoaderService;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.CodeSystemLoaderUtil;

/**
 * The Class BulkLoaderServiceImpl.
 */
@Service
public class BulkLoaderServiceImpl implements BulkLoaderService {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(BulkLoaderServiceImpl.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The enable post load computations. */
  @Autowired
  private EnablePostLoadComputations enablePostLoadComputations;

  /* see superclass */
  @Override
  @Write
  public CodeSystem doCodeSystemLoad(final File file) throws Exception {
    logger.info("Synchronous Load code system");
    // Use existing loader utility
    final CodeSystem codeSystem = CodeSystemLoaderUtil.loadCodeSystem(searchService, file,
        enablePostLoadComputations.isEnabled(), CodeSystem.class, new DefaultProgressListener());

    FileUtils.delete(file);
    logger.info("Synchronous Load complete");
    return codeSystem;
  }

  /* see superclass */
  @Override
  @Async
  @Write
  public void startAsyncCodeSystemLoad(final String processId, final File file, final ProgressListener listener,
    final Map<String, List<String>> processResultMap, final Map<String, Long> processProgressMap,
    final Map<String, Exception> processExceptionMap) {
    try {
      logger.info("Async Load code system");
      // Use existing loader utility
      final CodeSystem codeSystem = CodeSystemLoaderUtil.loadCodeSystem(searchService, file,
          enablePostLoadComputations.isEnabled(), CodeSystem.class, listener);

      FileUtils.delete(file);

      processResultMap.put(processId, new ArrayList<>());
      processResultMap.get(processId).add("CodeSystem/" + codeSystem.getId());
      logger.info("Async Load complete");
    } catch (final Exception e) {
      processProgressMap.put(processId, -1L);
      processExceptionMap.put(processId, e);
    }
  }
}
