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

import com.wci.termhub.lucene.eventing.Write;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.ConceptMap;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.OperationOutcome.IssueType;
import org.hl7.fhir.r5.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wci.termhub.EnablePostLoadComputations;
import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.fhir.rest.r5.FhirUtilityR5;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.CodeSystemLoaderUtil;
import com.wci.termhub.util.ConceptMapLoaderUtil;
import com.wci.termhub.util.ThreadLocalMapper;
import com.wci.termhub.util.ValueSetLoaderUtil;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Transaction;
import ca.uhn.fhir.rest.annotation.TransactionParam;
import jakarta.servlet.http.HttpServletResponse;

/**
 * System-level transaction provider for R5.
 */
@Component
public class SystemTransactionProviderR5 {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(SystemTransactionProviderR5.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The enable post load computations. */
  @Autowired
  private EnablePostLoadComputations enablePostLoadComputations;

  /** The context. */
  private static final FhirContext FHIR_CONTEXT_R5 = FhirContext.forR5();

  /**
   * Handle system-level transaction/batch bundle.
   *
   * @param bundle the request bundle
   * @return the response bundle
   * @throws Exception the exception
   */
  @Transaction
  @Write
  public Bundle transaction(@TransactionParam final Bundle bundle) throws Exception {

    final Bundle response = new Bundle();
    response.setType(Bundle.BundleType.TRANSACTIONRESPONSE);

    if (bundle == null || (bundle.getType() != Bundle.BundleType.TRANSACTION
        && bundle.getType() != Bundle.BundleType.BATCH)) {
      final OperationOutcome oo = new OperationOutcome();
      oo.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR).setCode(IssueType.INVALID)
          .setDiagnostics("Bundle.type must be 'transaction' or 'batch'");
      final Bundle.BundleEntryComponent entry = new Bundle.BundleEntryComponent();
      entry.setResource(oo);
      entry.getResponse().setStatus(String.valueOf(HttpServletResponse.SC_BAD_REQUEST));
      response.addEntry(entry);
      return response;
    }

    for (final Bundle.BundleEntryComponent in : bundle.getEntry()) {
      final Bundle.HTTPVerb verb = in.getRequest().getMethod();
      final String url = in.getRequest().getUrl();
      final Bundle.BundleEntryComponent out = new Bundle.BundleEntryComponent();
      try {
        if (in.getResource() == null) {
          throw FhirUtilityR5.exception("Missing entry.resource", IssueType.INVALID,
              HttpServletResponse.SC_BAD_REQUEST);
        }

        final String resourceType = in.getResource().fhirType();

        if (!("CodeSystem".equals(resourceType) || "ValueSet".equals(resourceType)
            || "ConceptMap".equals(resourceType))) {
          throw FhirUtilityR5.exception("Unsupported resource type in bundle: " + resourceType,
              IssueType.INVALID, HttpServletResponse.SC_BAD_REQUEST);
        }

        if (verb != Bundle.HTTPVerb.POST && verb != Bundle.HTTPVerb.PUT) {
          throw FhirUtilityR5.exception("Unsupported method: " + verb, IssueType.NOTSUPPORTED,
              HttpServletResponse.SC_BAD_REQUEST);
        }

        final String json =
            FHIR_CONTEXT_R5.newJsonParser().encodeResourceToString(in.getResource());
        final File tmp = File.createTempFile("txn", ".json");
        ThreadLocalMapper.get().writeValue(tmp, ThreadLocalMapper.get().readTree(json));

        if ("CodeSystem".equals(resourceType)) {
          final CodeSystem cs = CodeSystemLoaderUtil.loadCodeSystem(searchService, tmp,
              enablePostLoadComputations.isEnabled(), CodeSystem.class,
              new DefaultProgressListener());
          out.setResource(cs);
          out.getResponse().setStatus("200");
        } else if ("ValueSet".equals(resourceType)) {
          final ValueSet vs = ValueSetLoaderUtil.loadValueSet(searchService, tmp, ValueSet.class,
              new DefaultProgressListener());
          out.setResource(vs);
          out.getResponse().setStatus("200");
        } else if ("ConceptMap".equals(resourceType)) {
          final ConceptMap cm = ConceptMapLoaderUtil.loadConceptMap(searchService, tmp,
              ConceptMap.class, new DefaultProgressListener());
          out.setResource(cm);
          out.getResponse().setStatus("200");
        } else {
          logger.info("    SKIP unhandled resource type = " + resourceType);
        }

      } catch (final Exception e) {
        logger.error("Transaction entry failed: {} {}", verb, url, e);
        final OperationOutcome oo = new OperationOutcome();
        oo.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR).setCode(IssueType.EXCEPTION)
            .setDiagnostics(e.getMessage());
        out.setResource(oo);
        out.getResponse().setStatus(String.valueOf(HttpServletResponse.SC_BAD_REQUEST));
      }
      response.addEntry(out);
    }

    return response;
  }
}
