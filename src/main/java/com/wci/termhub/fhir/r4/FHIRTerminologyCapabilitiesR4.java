/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r4;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ContactDetail;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.TerminologyCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.PropertyUtility;

import ca.uhn.fhir.model.api.annotation.ChildOrder;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Specification of the FHIR TerminologyCapabilities.
 */
@ResourceDef(name = "TerminologyCapabilities",
    profile = "http://hl7.org/fhir/StructureDefinition/TerminologyCapabilities")
@ChildOrder(names = {
    "url", "version", "name", "title", "status", "experimental", "date", "publisher", "contact",
    "description", "useContext", "jurisdiction", "purpose", "copyright", "kind", "software",
    "implementation", "lockedDate", "codeSystem", "expansion", "codeSearch", "validateCode",
    "translation", "closure"
})
@Component
public class FHIRTerminologyCapabilitiesR4 extends TerminologyCapabilities
    implements IBaseConformance {

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * With defaults.
   *
   * @param request the request
   * @param requestDetails the request details
   * @return the FHIR terminology capabilities
   */
  public FHIRTerminologyCapabilitiesR4 withDefaults(final HttpServletRequest request,
    final RequestDetails requestDetails) {
    setName("OPENTERMHUBTerminologyCapabilities");
    setStatus(Enumerations.PublicationStatus.DRAFT);
    setTitle("Open Termhub R4 Terminology Capability Statement");
    final String version = String.valueOf(PropertyUtility.getProperties().get("server.version"));
    setVersion(version);
    setDate(new java.util.Date());
    setPurpose(
        "The Open Termhub Terminology Capability Statement provides a summary of the Open Termhub API's"
            + " capabilities.");
    setKind(CapabilityStatementKind.CAPABILITY);
    setSoftware(
        new TerminologyCapabilitiesSoftwareComponent().setName("TERMHUB").setVersion(version));
    this.setExperimental(true);
    this.setPublisher("TERMHUB");
    final Meta meta = new Meta();
    meta.addProfile("http://hl7.org/fhir/StructureDefinition/TerminologyCapabilities");
    this.setMeta(meta);

    setCodeSystems(request, requestDetails);
    setContact();
    setExpansion();

    return this;
  }

  /**
   * Sets the expansion.
   */
  private void setExpansion() {

    final TerminologyCapabilitiesExpansionComponent expansion =
        new TerminologyCapabilitiesExpansionComponent();
    expansion.setHierarchical(false);
    expansion.setPaging(true);
    expansion.setTextFilter(
        "Matching is word-prefix, any-order across all designations and the code itself.\n"
            + "Codes are returned in best-match-first order.");

    // Define parameter names and optional documentation
    final Map<String, String> parameters = new HashMap<>();
    parameters.put("url", null);
    parameters.put("valueSet", null);
    parameters.put("valueSetVersion", null);
    parameters.put("filter",
        "Matching is word-prefix, any-order across all designations and the code itself.");
    parameters.put("profile", null);
    parameters.put("date", null);
    parameters.put("context", null);
    parameters.put("offset", null);
    parameters.put("count", null);
    parameters.put("activeOnly", null);
    parameters.put("includeDesignations", null);
    parameters.put("includeDefinition", null);
    parameters.put("system-version", null);
    parameters.put("force-system-version", null);
    parameters.put("property", "Pre-adopted from R4.");
    parameters.put("useSupplement", "Pre-adopted from R4.");
    parameters.put("displayLanguage", null);
    parameters.put("excludeNested",
        "Soft-defaults to true. Only returns nested results when a stored expansion is "
            + "being use and there is no `filter` parameter.");
    parameters.put("tx-resource", null);

    parameters.forEach((name, documentation) -> {
      final TerminologyCapabilitiesExpansionParameterComponent parameter =
          new TerminologyCapabilitiesExpansionParameterComponent();
      parameter.setName(name);
      if (documentation != null) {
        parameter.setDocumentation(documentation);
      }
      expansion.addParameter(parameter);
    });

    this.setExpansion(expansion);
  }

  /**
   * Sets the code systems.
   *
   * @param request the request
   * @param requestDetails the request details
   */
  private void setCodeSystems(final HttpServletRequest request,
    final RequestDetails requestDetails) {

    try {

      // Find the matching code systems in the list of terms
      for (final Terminology terminology : FhirUtility.lookupTerminologies(searchService)) {
        final CodeSystem cs = FhirUtilityR4.toR4(terminology);

        final TerminologyCapabilities.TerminologyCapabilitiesCodeSystemComponent tccsc =
            new TerminologyCapabilities.TerminologyCapabilitiesCodeSystemComponent();
        tccsc.setUri(cs.getUrl());
        final TerminologyCapabilities.TerminologyCapabilitiesCodeSystemVersionComponent vc =
            new TerminologyCapabilities.TerminologyCapabilitiesCodeSystemVersionComponent();
        vc.setCode(cs.getVersion());
        vc.setIsDefault(true);
        vc.setCompositional(false);
        tccsc.setVersion(Collections.singletonList(vc));
        this.addCodeSystem(tccsc);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sets the contact.
   */
  private void setContact() {
    final ContactPoint contactPoint = new ContactPoint();
    contactPoint.setSystem(ContactPoint.ContactPointSystem.EMAIL);
    final ContactDetail contactDetail = new ContactDetail();
    contactDetail.addTelecom(contactPoint);
    setContact(Collections.singletonList(contactDetail));
  }
}
