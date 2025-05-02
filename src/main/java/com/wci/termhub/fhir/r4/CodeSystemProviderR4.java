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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.CodeSystemLoaderUtil;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.TerminologyUtility;

import ca.uhn.fhir.jpa.model.util.JpaConstants;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The CodeSystem provider.
 */
@Component
public class CodeSystemProviderR4 implements IResourceProvider {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(CodeSystemProviderR4.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Gets the code system.
   *
   * @param request the request
   * @param details the details
   * @param id the id
   * @return the code system
   * @throws Exception the exception
   */
  @Read()
  public CodeSystem getCodeSystem(final HttpServletRequest request,
    final ServletRequestDetails details, @IdParam final IdType id) throws Exception {

    try {
      logger.info("Looking for code system with ID: {}", id != null ? id.getIdPart() : "null");

      for (final Terminology terminology : FhirUtility.lookupTerminologies(searchService)) {
        final CodeSystem cs = FhirUtilityR4.toR4(terminology);
        logger.info("Checking code system {} with ID: {}", cs.getTitle(), cs.getId());

        // Skip non-matching
        if (id != null && id.getIdPart().equals(cs.getId())) {
          logger.info("Found matching code system: {}", cs.getTitle());
          return cs;
        }
      }
      throw FhirUtilityR4.exception(
          "Code system not found = " + (id == null ? "null" : id.getIdPart()), IssueType.NOTFOUND,
          HttpServletResponse.SC_NOT_FOUND);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to load code system",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Find code systems.
   *
   * <pre>
   * Parameters for all resources
   *   used: _id
   *   not used: _content, _filter, _has, _in, _language, _lastUpdated,
   *             _list, _profile, _query, _security, _source, _tag, _text, _type
   * https://hl7.org/fhir/R4/codesystem.html (see Search Parameters)
   * The following parameters in the registry are not used
   * &#64;OptionalParam(name="code") String code,
   * &#64;OptionalParam(name="context") TokenParam
   * &#64;OptionalParam(name="context-quantity") QuantityParam contextQuantity,
   * &#64;OptionalParam(name="context-type") String contextType,
   * &#64;OptionalParam(name="context-type-quantity") QuantityParam contextTypeQuantity,
   * &#64;OptionalParam(name="context-type-value") String contextTypeValue,
   * &#64;OptionalParam(name="identifier") StringParam identifier,
   * &#64;OptionalParam(name="jurisdiction") StringParam jurisdiction,
   * &#64;OptionalParam(name="status") String status,
   * </pre>
   *
   * @param request the request
   * @param details the details
   * @param id the id
   * @param code the code
   * @param date the date
   * @param description the description
   * @param name the name
   * @param publisher the publisher
   * @param title the title
   * @param url the url
   * @param version the version
   * @param count the count
   * @param offset the offset
   * @return the list
   * @throws Exception the exception
   */
  @Search
  public Bundle findCodeSystems(final HttpServletRequest request,
    final ServletRequestDetails details, @OptionalParam(name = "_id") final TokenParam id,
    @OptionalParam(name = "code") final TokenParam code,
    @OptionalParam(name = "date") final DateRangeParam date,
    @OptionalParam(name = "description") final StringParam description,
    @OptionalParam(name = "name") final StringParam name,
    @OptionalParam(name = "publisher") final StringParam publisher,
    @OptionalParam(name = "title") final StringParam title,
    @OptionalParam(name = "url") final UriParam url,
    @OptionalParam(name = "version") final StringParam version,
    @Description(shortDefinition = "Number of entries to return") @OptionalParam(
        name = "_count") final NumberParam count,
    @Description(shortDefinition = "Start offset, used when reading a next page") @OptionalParam(
        name = "_offset") final NumberParam offset)
    throws Exception {

    try {

      FhirUtilityR4.notSupportedSearchParams(request);

      final List<CodeSystem> list = new ArrayList<>();
      for (final Terminology terminology : FhirUtility.lookupTerminologies(searchService)) {
        final CodeSystem cs = FhirUtilityR4.toR4(terminology);

        final Set<String> mapsetsMatchingCodes = new HashSet<>();
        if (code != null) {
          mapsetsMatchingCodes.addAll(searchService
              .find(new SearchParameters("to.code:" + StringUtility.escapeQuery(code.getValue()),
                  null, 1000, null, null), Concept.class, null)
              .getItems().stream().map(c -> c.getCode()).collect(Collectors.toSet()));
        }

        // Skip non-matching
        if ((id != null && !id.getValue().equals(cs.getId()))
            || (url != null && !url.getValue().equals(cs.getUrl()))) {
          continue;
        }
        if (date != null && !FhirUtility.compareDate(date, cs.getDate())) {
          logger.info("  SKIP date mismatch = " + cs.getDate());
          continue;
        }
        if (description != null && !FhirUtility.compareString(description, cs.getDescription())) {
          logger.info("  SKIP description mismatch = " + cs.getDescription());
          continue;
        }
        if (name != null && !FhirUtility.compareString(name, cs.getName())) {
          logger.info("  SKIP name mismatch = " + cs.getName());
          continue;
        }
        if (publisher != null && !FhirUtility.compareString(publisher, cs.getPublisher())) {
          logger.info("  SKIP publisher mismatch = " + cs.getPublisher());
          continue;
        }
        if (title != null && !FhirUtility.compareString(title, cs.getTitle())) {
          logger.info("  SKIP title mismatch = " + cs.getTitle());
          continue;
        }
        if (version != null && !FhirUtility.compareString(version, cs.getVersion())) {
          logger.info("  SKIP version mismatch = " + cs.getVersion());
          continue;
        }
        if (code != null && !mapsetsMatchingCodes.contains(code.getValue())) {
          logger.info("  SKIP code not found = " + code.getValue());
          continue;
        }
        list.add(cs);
      }

      return FhirUtilityR4.makeBundle(request, list, count, offset);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to find code systems",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Lookup implicit.
   *
   * <pre>
   * https://build.fhir.org/codesystem-operation-lookup.html
   * All properties implemented.
   * </pre>
   *
   * @param request the request
   * @param response the response
   * @param details the details
   * @param code the code
   * @param system the system
   * @param version the version
   * @param coding the coding
   * @param displayLanguage the display language
   * @param properties the properties
   * @return the parameters
   * @throws Exception the exception
   */
  @Operation(name = JpaConstants.OPERATION_LOOKUP, idempotent = true)
  public Parameters lookupImplicit(final HttpServletRequest request,
    final HttpServletResponse response, final ServletRequestDetails details,
    @OperationParam(name = "code", min = 0, max = 1, typeName = "code") final CodeType code,
    @OperationParam(name = "system", min = 0, max = 1, typeName = "uri") final UriType system,
    @OperationParam(name = "version", min = 0, max = 1,
        typeName = "string") final StringType version,
    @OperationParam(name = "coding", min = 0, max = 1, typeName = "Coding") final Coding coding,
    @OperationParam(name = "displayLanguage", min = 0, max = 1,
        typeName = "code") final CodeType displayLanguage,
    @OperationParam(name = "property", min = 0, max = OperationParam.MAX_UNLIMITED,
        typeName = "code") final Set<CodeType> properties)
    throws Exception {

    // Reject post
    if (request.getMethod().equals("POST")) {
      throw FhirUtilityR4.exception("POST method not supported for $lookup", IssueType.NOTSUPPORTED,
          HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    try {
      FhirUtilityR4.mutuallyExclusive("code", code, "coding", coding);

      final Terminology terminology = FhirUtilityR4.getTerminology(searchService, null, code,
          "system", system, version, coding);

      final String codeStr = FhirUtilityR4.getCode(code, coding);
      return lookupHelper(terminology, codeStr, properties);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to lookup code", OperationOutcome.IssueType.EXCEPTION,
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Lookup instance. https://build.fhir.org/codesystem-operation-lookup.html
   *
   * <pre>
   * https://build.fhir.org/codesystem-operation-lookup.html
   * All properties implemented.
   * </pre>
   *
   * @param request the request
   * @param response the response
   * @param details the details
   * @param id the id
   * @param code the code
   * @param system the system
   * @param version the version
   * @param coding the coding
   * @param displayLanguage the display language
   * @param properties the properties
   * @return the parameters
   * @throws Exception the exception
   */
  @Operation(name = JpaConstants.OPERATION_LOOKUP, idempotent = true)
  public Parameters lookupInstance(final HttpServletRequest request,
    final HttpServletResponse response, final ServletRequestDetails details,
    @IdParam final IdType id,
    @OperationParam(name = "code", min = 0, max = 1, typeName = "code") final CodeType code,
    @OperationParam(name = "system", min = 0, max = 1, typeName = "uri") final UriType system,
    @OperationParam(name = "version", min = 0, max = 1,
        typeName = "string") final StringType version,
    @OperationParam(name = "coding", min = 0, max = 1, typeName = "Coding") final Coding coding,
    @OperationParam(name = "displayLanguage", min = 0, max = 1,
        typeName = "code") final CodeType displayLanguage,
    @OperationParam(name = "property", min = 0, max = OperationParam.MAX_UNLIMITED,
        typeName = "code") final Set<CodeType> properties)
    throws Exception {

    // Reject post
    if (request.getMethod().equals("POST")) {
      throw FhirUtilityR4.exception("POST method not supported for $lookup", IssueType.NOTSUPPORTED,
          HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    try {

      FhirUtilityR4.mutuallyExclusive("code", code, "coding", coding);
      // FhirUtility.notSupported("date", date);

      final Terminology terminology =
          FhirUtilityR4.getTerminology(searchService, id, null, null, null, null, null);

      final String codeStr = FhirUtilityR4.getCode(code, coding);
      return lookupHelper(terminology, codeStr, properties);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to lookup code", OperationOutcome.IssueType.EXCEPTION,
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Validate code implicit.
   *
   * <pre>
   * https://hl7.org/fhir/R4/codesystem-operation-validate-code.html
   * The following parameters are not used:
   * &#64;OperationParam(name = "codeSystem") CodeSystem codeSystem
   * &#64;OperationParam(name = "date") DateTimeType date,
   * &#64;OperationParam(name = "codeableConcept") CodeableConcept codeableConcept
   * &#64;OperationParam(name = "abstract") BooleanType abstract
   * &#64;OperationParam(name = "displayLanguage") String displayLanguage
   * </pre>
   *
   * @param request the request
   * @param response the response
   * @param details the details
   * @param url the url
   * @param code the code
   * @param version the version
   * @param display the display
   * @param coding the coding
   * @return the parameters
   * @throws Exception the exception
   */
  @Operation(name = JpaConstants.OPERATION_VALIDATE_CODE, idempotent = true)
  public Parameters validateCodeImplicit(final HttpServletRequest request,
    final HttpServletResponse response, final ServletRequestDetails details,
    @OperationParam(name = "url", min = 0, max = 1, typeName = "uri") final UriType url,
    @OperationParam(name = "code", min = 0, max = 1, typeName = "code") final CodeType code,
    @OperationParam(name = "version", min = 0, max = 1,
        typeName = "string") final StringType version,
    @OperationParam(name = "display", min = 0, max = 1,
        typeName = "string") final StringType display,
    @OperationParam(name = "coding", min = 0, max = 1, typeName = "Coding") final Coding coding)
    throws Exception {

    // Reject post
    if (request.getMethod().equals("POST")) {
      throw FhirUtilityR4.exception("POST method not supported for $validate-code",
          IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    try {

      FhirUtilityR4.mutuallyExclusive("code", code, "coding", coding);
      FhirUtilityR4.mutuallyRequired("display", display, "code", code, "coding", coding);

      final Terminology terminology =
          FhirUtilityR4.getTerminology(searchService, null, code, "url", url, version, coding);

      final String codeStr = FhirUtilityR4.getCode(code, coding);
      return validateHelper(terminology, codeStr, display);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to validate code", OperationOutcome.IssueType.EXCEPTION,
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Validate code implicit.
   *
   * <pre>
   * https://hl7.org/fhir/R4/codesystem-operation-validate-code.html
   * The following parameters are not used:
   * &#64;OperationParam(name = "codeSystem") CodeSystem codeSystem
   * &#64;OperationParam(name = "date") DateTimeType date,
   * &#64;OperationParam(name = "codeableConcept") CodeableConcept codeableConcept
   * &#64;OperationParam(name = "abstract") BooleanType abstract
   * &#64;OperationParam(name = "displayLanguage") String displayLanguage
   * </pre>
   *
   * @param request the request
   * @param response the response
   * @param details the details
   * @param id the id
   * @param url the url
   * @param version the version
   * @param code the code
   * @param display the display
   * @param coding the coding
   * @param codeableConcept the codeable concept
   * @return the parameters
   * @throws Exception the exception
   */
  @Operation(name = JpaConstants.OPERATION_VALIDATE_CODE, idempotent = true)
  public Parameters validateCodeInstance(final HttpServletRequest request,
    final HttpServletResponse response, final ServletRequestDetails details,
    @IdParam final IdType id,
    @OperationParam(name = "url", min = 0, max = 1, typeName = "uri") final UriType url,
    @OperationParam(name = "version", min = 0, max = 1,
        typeName = "string") final StringType version,
    @OperationParam(name = "code", min = 0, max = 1, typeName = "code") final CodeType code,
    @OperationParam(name = "display", min = 0, max = 1,
        typeName = "string") final StringType display,
    @OperationParam(name = "coding", min = 0, max = 1, typeName = "Coding") final Coding coding,
    @OperationParam(name = "codeableConcept", min = 0, max = 1,
        typeName = "CodeableConcept") final CodeableConcept codeableConcept)
    throws Exception {

    // Reject post
    if (request.getMethod().equals("POST")) {
      throw FhirUtilityR4.exception("POST method not supported for $validate-code",
          IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    try {

      FhirUtilityR4.mutuallyExclusive("code", code, "coding", coding);
      FhirUtilityR4.mutuallyRequired("display", display, "code", code, "coding", coding);

      final Terminology terminology =
          FhirUtilityR4.getTerminology(searchService, id, code, "url", url, version, coding);

      final String codeStr = FhirUtilityR4.getCode(code, coding);
      return validateHelper(terminology, codeStr, display);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to validate code", OperationOutcome.IssueType.EXCEPTION,
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }

  /**
   * Subsumes implicit.
   *
   * <pre>
   * https://hl7.org/fhir/R4/codesystem-operation-subsumes.html All parameters
   * supported.
   * </pre>
   *
   * @param request the request
   * @param response the response
   * @param details the details
   * @param codeA the code A
   * @param codeB the code B
   * @param system the system
   * @param codingA the coding A
   * @param codingB the coding B
   * @param version the version
   * @return the parameters
   * @throws Exception the exception
   */
  @Operation(name = JpaConstants.OPERATION_SUBSUMES, idempotent = true)
  public Parameters subsumesImplicit(final HttpServletRequest request,
    final HttpServletResponse response, final ServletRequestDetails details,
    @OperationParam(name = "codeA", min = 0, max = 1, typeName = "code") final CodeType codeA,
    @OperationParam(name = "codeB", min = 0, max = 1, typeName = "code") final CodeType codeB,
    @OperationParam(name = "system", min = 0, max = 1, typeName = "uri") final UriType system,
    @OperationParam(name = "codingA", min = 0, max = 1, typeName = "Coding") final Coding codingA,
    @OperationParam(name = "codingB", min = 0, max = 1, typeName = "Coding") final Coding codingB,
    @OperationParam(name = "version", min = 0, max = 1,
        typeName = "string") final StringType version)
    throws Exception {

    // Reject post
    if (request.getMethod().equals("POST")) {
      throw FhirUtilityR4.exception("POST method not supported for $subsumes",
          IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    try {
      return subsumesHelper(null, system, version, codeA, codeB, codingA, codingB);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to check if A subsumes B",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Subsumes instance.
   *
   * <pre>
   * https://hl7.org/fhir/R4/codesystem-operation-subsumes.html All parameters
   * supported.
   * </pre>
   *
   * @param request the request
   * @param response the response
   * @param details the details
   * @param id the id
   * @param codeA the code A
   * @param codeB the code B
   * @param system the system
   * @param codingA the coding A
   * @param codingB the coding B
   * @param version the version
   * @return the parameters
   * @throws Exception the exception
   */
  @Operation(name = JpaConstants.OPERATION_SUBSUMES, idempotent = true)
  public Parameters subsumesInstance(final HttpServletRequest request,
    final HttpServletResponse response, final ServletRequestDetails details,
    @IdParam final IdType id,
    @OperationParam(name = "codeA", min = 0, max = 1, typeName = "code") final CodeType codeA,
    @OperationParam(name = "codeB", min = 0, max = 1, typeName = "code") final CodeType codeB,
    @OperationParam(name = "system", min = 0, max = 1, typeName = "uri") final UriType system,
    @OperationParam(name = "codingA", min = 0, max = 1, typeName = "Coding") final Coding codingA,
    @OperationParam(name = "codingB", min = 0, max = 1, typeName = "Coding") final Coding codingB,
    @OperationParam(name = "version", min = 0, max = 1,
        typeName = "string") final StringType version)
    throws Exception {

    // Reject post
    if (request.getMethod().equals("POST")) {
      throw FhirUtilityR4.exception("POST method not supported for $subsumes",
          IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    try {
      return subsumesHelper(id, system, version, codeA, codeB, codingA, codingB);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to check if A subsumes B",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }

  /**
   * Lookup helper.
   *
   * @param terminology the terminology
   * @param code the code
   * @param properties the properties
   * @return the parameters
   * @throws Exception the exception
   */
  private Parameters lookupHelper(final Terminology terminology, final String code,
    final Set<CodeType> properties) throws Exception {

    // Lookup concept (only code is needed because we have terminology index)
    final SearchParameters params =
        new SearchParameters("code:" + StringUtility.escapeQuery(code) + " AND terminology:"
            + StringUtility.escapeQuery(terminology.getAbbreviation()), 0, 2, null, null);
    final Concept concept = searchService.findSingle(params, Concept.class);
    if (concept == null) {
      throw FhirUtilityR4.exception("Unable to find code for system/version = " + code,
          OperationOutcome.IssueType.NOTFOUND, HttpServletResponse.SC_NOT_FOUND);
    }

    // Lookup parents/children
    final List<ConceptRelationship> relationships =
        searchService.findAll(StringUtility.composeQuery("AND", "active:true",
            "from.code:" + StringUtility.escapeQuery(code)), null, ConceptRelationship.class);
    final List<ConceptRelationship> children =
        searchService.findAll(StringUtility.composeQuery("AND", "active:true", "hierarchical:true",
            "to.code:" + StringUtility.escapeQuery(code)), null, ConceptRelationship.class);

    // Look up metadata
    final Map<String, String> displayMap = FhirUtility.getDisplayMap(searchService, terminology);

    return FhirUtilityR4.toR4(FhirUtilityR4.toR4(terminology), concept,
        properties == null ? null
            : properties.stream().map(c -> c.getValue()).collect(Collectors.toSet()),
        displayMap, relationships, children == null ? null
            : children.stream().map(r -> r.getFrom()).collect(Collectors.toList()));
  }

  /**
   * Validate helper.
   *
   * @param terminology the terminology
   * @param code the code
   * @param display the display
   * @return the parameters
   * @throws Exception the exception
   */
  private Parameters validateHelper(final Terminology terminology, final String code,
    final StringType display) throws Exception {

    // Lookup concept
    final SearchParameters params =
        new SearchParameters("code:" + StringUtility.escapeQuery(code) + " AND terminology:"
            + StringUtility.escapeQuery(terminology.getAbbreviation()), 0, 2, null, null);

    final Concept concept = searchService.findSingle(params, Concept.class);

    return FhirUtilityR4.toR4ValidateCode(FhirUtilityR4.toR4(terminology), concept,
        display == null ? null : display.getValue());
  }

  /**
   * Subsumes helper.
   *
   * @param id the id
   * @param system the system
   * @param version the version
   * @param codeAParam the code A param
   * @param codeBParam the code B param
   * @param codingA the coding A
   * @param codingB the coding B
   * @return the parameters
   * @throws Exception the exception
   */
  private Parameters subsumesHelper(final IdType id, final UriType system, final StringType version,
    final CodeType codeAParam, final CodeType codeBParam, final Coding codingA,
    final Coding codingB) throws Exception {
    // "The system parameter is required unless the operation is invoked on an
    // instance of a
    // code system resource."
    // (https://www.hl7.org/fhir/codesystem-operation-subsumes.html)
    if (id == null && system == null) {
      throw FhirUtilityR4.exception(
          "One of id or system parameters must be supplied for the $subsumes operation.",
          IssueType.INVALID, HttpServletResponse.SC_BAD_REQUEST);
    }

    FhirUtilityR4.requireExactlyOneOf("codeA", codeAParam, "codingA", codingA);
    FhirUtilityR4.requireExactlyOneOf("codeB", codeBParam, "codingB", codingB);

    final Terminology terminologyA = FhirUtilityR4.getTerminology(searchService, id, codeAParam,
        "system", system, version, codingA);
    final Terminology terminologyB = FhirUtilityR4.getTerminology(searchService, id, codeBParam,
        "system", system, version, codingB);

    if (terminologyA == null && terminologyB == null) {
      throw FhirUtilityR4.exception("Unable to determine code system for $subsumes operation.",
          IssueType.INVALID, HttpServletResponse.SC_BAD_REQUEST);
    }

    Terminology terminology = terminologyB;
    if (terminologyA == null) {
      terminology = terminologyB;
    } else if (terminologyB == null) {
      terminology = terminologyA;
    } else if (!terminologyA.getId().equals(terminologyB.getId())) {
      throw FhirUtilityR4.exception("Incompatable code system A/B for $subsumes operation.",
          IssueType.INVALID, HttpServletResponse.SC_BAD_REQUEST);
    }

    final String codeA = FhirUtilityR4.getCode(codeAParam, codingA);
    final String codeB = FhirUtilityR4.getCode(codeBParam, codingB);

    final Concept conceptA = TerminologyUtility.getConcept(searchService, terminology, codeA);
    if (conceptA == null) {
      throw FhirUtilityR4.exception(
          String.format("Code does not exist for code system =" + codeA + ","
              + terminology.getAttributes().get("fhirUri")),
          OperationOutcome.IssueType.INVALID, HttpServletResponse.SC_BAD_REQUEST);
    }
    final Concept conceptB = TerminologyUtility.getConcept(searchService, terminology, codeB);
    if (conceptB == null) {
      throw FhirUtilityR4.exception(
          String.format("Code does not exist for code system =" + codeB + ","
              + terminology.getAttributes().get("fhirUri")),
          OperationOutcome.IssueType.INVALID, HttpServletResponse.SC_BAD_REQUEST);
    }

    // equivalent http://hl7.org/fhir/concept-subsumption-outcome Equivalent
    // subsumes http://hl7.org/fhir/concept-subsumption-outcome Subsumes
    // subsumed-by http://hl7.org/fhir/concept-subsumption-outcome Subsumed-By
    // not-subsumed http://hl7.org/fhir/concept-subsumption-outcome Not-Subsumed
    if (codeA.equals(codeB)) {
      return FhirUtilityR4.toR4Subsumes("equivalent", terminology);
    }

    // If A is an ancestor of b
    if (TerminologyUtility.getAncestorCodes(searchService, conceptB).contains(conceptA.getCode())) {
      return FhirUtilityR4.toR4Subsumes("subsumes", terminology);
    } else if (TerminologyUtility.getAncestorCodes(searchService, conceptA)
        .contains(conceptB.getCode())) {
      return FhirUtilityR4.toR4Subsumes("subsumed-by", terminology);
    }
    return FhirUtilityR4.toR4Subsumes("not-subsumed", terminology);
  }

  /**
   * Create a new CodeSystem resource.
   *
   * @param request the request
   * @param details the details
   * @param codeSystem the code system resource
   * @return the created code system
   * @throws Exception the exception
   */
  @Create
  public MethodOutcome createCodeSystem(final HttpServletRequest request,
    final ServletRequestDetails details, @ResourceParam final CodeSystem codeSystem)
    throws Exception {

    try {
      logger.info("Creating code system with {} concepts",
          codeSystem.getConcept() != null ? codeSystem.getConcept().size() : 0);

      // Convert CodeSystem to JSON
      final String content = ca.uhn.fhir.context.FhirContext.forR4().newJsonParser()
          .encodeResourceToString(codeSystem);

      // Write content to temporary file
      final java.nio.file.Path tempFile =
          java.nio.file.Files.createTempFile("codesystem-", ".json");
      java.nio.file.Files.write(tempFile, content.getBytes());

      // Use existing loader utility
      CodeSystemLoaderUtil.loadCodeSystem(searchService, tempFile.toString());

      // Clean up temp file
      java.nio.file.Files.delete(tempFile);

      // Return success
      final MethodOutcome outcome = new MethodOutcome();
      outcome.setResource(codeSystem);
      outcome.setCreated(true);
      return outcome;

    } catch (final Exception e) {
      logger.error("Unexpected error creating code system", e);
      throw FhirUtilityR4.exception("Failed to create code system: " + e.getMessage(),
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Gets the resource type.
   *
   * @return the resource type
   */
  /* see superclass */
  @Override
  public Class<CodeSystem> getResourceType() {
    return CodeSystem.class;
  }
}
