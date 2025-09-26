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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.ConceptMap;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.OperationOutcome.IssueType;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.UriType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wci.termhub.fhir.rest.r5.FhirUtilityR5;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.model.Mapping;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.ConceptMapLoaderUtil;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.TerminologyUtility;

import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The ConceptMap provider.
 */
@Component
public class ConceptMapProviderR5 implements IResourceProvider {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(ConceptMapProviderR5.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Gets the concept map.
   *
   * @param request the request
   * @param details the details
   * @param id the id
   * @return the concept map
   * @throws Exception the exception
   */
  @Read()
  public ConceptMap getConceptMap(final HttpServletRequest request,
    final ServletRequestDetails details, @IdParam final IdType id) throws Exception {

    try {
      if (logger.isDebugEnabled()) {
        logger.debug("Getting concept map with id = {}", id);
      }
      final List<ConceptMap> candidates = findCandidates();
      if (logger.isDebugEnabled()) {
        logger.debug("Found {} candidate concept maps", candidates.size());
      }
      for (final ConceptMap map : candidates) {
        if (logger.isDebugEnabled()) {
          logger.debug("Checking candidate map: id={}, identifier={}, title={}", map.getId(),
              map.getIdentifierFirstRep().getValue(), map.getTitle());
        }
        // Strip any resource prefix from the ID for comparison
        final String requestedId = id.getIdPart();
        String candidateId = map.getId();
        if (candidateId.contains("/")) {
          candidateId = candidateId.substring(candidateId.lastIndexOf("/") + 1);
        }
        if (logger.isDebugEnabled()) {
          logger.debug("Comparing requested id {} with candidate id {}", requestedId, candidateId);
        }
        if (requestedId.equals(candidateId)) {
          if (logger.isDebugEnabled()) {
            logger.debug("Found matching concept map: id={}, title={}", map.getId(),
                map.getTitle());
          }
          return map;
        }
      }
      logger.warn("No concept map found matching id={}", id);
      throw FhirUtilityR5.exception(
          "Concept map not found = " + (id == null ? "null" : id.getIdPart()), IssueType.NOTFOUND,
          HttpServletResponse.SC_NOT_FOUND);

    } catch (final FHIRServerResponseException e) {
      logger.error("FHIR Server Response Exception", e);
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR5.exception("Failed to load concept map",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Find concept maps.
   *
   * <pre>
   * Parameters for all resources
   *   used: _id
   *   not used: _content, _filter, _has, _in, _language, _lastUpdated,
   *             _list, _profile, _query, _security, _source, _tag, _text, _type
   * https://hl7.org/fhir/R5/conceptmap.html (see Search Parameters)
   * The following parameters in the registry are not used
   * &#64;OptionalParam(name="context") TokenParam context,
   * &#64;OptionalParam(name="context-quantity") QuantityParam contextQuantity,
   * &#64;OptionalParam(name="context-type") String contextType,
   * &#64;OptionalParam(name="context-type-quantity") QuantityParam contextTypeQuantity,
   * &#64;OptionalParam(name="context-type-value") String contextTypeValue,
   * &#64;OptionalParam(name="date") DateParam date,
   * &#64;OptionalParam(name="dependsOn") String dependsOn,
   * &#64;OptionalParam(name="identifier") StringParam identifier,
   * &#64;OptionalParam(name="jurisdiction") StringParam jurisdiction,
   * &#64;OptionalParam(name="other") ReferenceParam other,
   * &#64;OptionalParam(name="product") String product,
   * &#64;OptionalParam(name="source") ReferenceParam source,
   * &#64;OptionalParam(name="source-code") TokenParam sourceCode,
   * &#64;OptionalParam(name="source-uri") ReferenceParam sourceUri,
   * &#64;OptionalParam(name="status") String status,
   * &#64;OptionalParam(name="target") ReferenceParam target,
   * &#64;OptionalParam(name="target-code") TokenParam targetCode,
   * &#64;OptionalParam(name="target-system") UriParam targetSystem,
   * &#64;OptionalParam(name="target-uri") ReferenceParam targetUri,
   * </pre>
   *
   * @param request the request
   * @param details the details
   * @param id the id
   * @param date the date
   * @param description the description
   * @param identifier the identifier
   * @param name the name
   * @param publisher the publisher
   * @param sourceSystem the source system
   * @param sourceCode the source code
   * @param sourceScopeUri the source scope uri
   * @param targetCode the target code
   * @param targetSystem the target system
   * @param targetScopeUri the target scope uri
   * @param title the title
   * @param url the url
   * @param version the version
   * @param count the count
   * @param offset the offset
   * @return the list
   * @throws Exception the exception
   */
  @Search
  public Bundle findConceptMaps(final HttpServletRequest request,
    final ServletRequestDetails details, @OptionalParam(name = "_id") final TokenParam id,
    @OptionalParam(name = "date") final StringParam date,
    @OptionalParam(name = "description") final StringParam description,
    @OptionalParam(name = "identifier") final StringParam identifier,
    @OptionalParam(name = "name") final StringParam name,
    @OptionalParam(name = "publisher") final StringParam publisher,
    @OptionalParam(name = "source-group-system") final UriParam sourceSystem,
    @OptionalParam(name = "source-code") final TokenParam sourceCode,
    @OptionalParam(name = "source-scope-uri") final UriParam sourceScopeUri,
    @OptionalParam(name = "target-code") final TokenParam targetCode,
    @OptionalParam(name = "target-group-system") final UriParam targetSystem,
    @OptionalParam(name = "target-scope-uri") final UriParam targetScopeUri,
    @OptionalParam(name = "title") final StringParam title,
    @OptionalParam(name = "url") final UriType url,
    @OptionalParam(name = "version") final StringParam version,
    @Description(shortDefinition = "Number of entries to return")
    @OptionalParam(name = "_count") final NumberParam count,
    @Description(shortDefinition = "Start offset, used when reading a next page")
    @OptionalParam(name = "_offset") final NumberParam offset) throws Exception {

    try {

      FhirUtilityR5.notSupportedSearchParams(request);

      // Get all mapsets and then restrict
      final List<ConceptMap> candidates = findCandidates();
      final List<ConceptMap> list = new ArrayList<>();
      for (final ConceptMap cm : candidates) {
        // Skip non-matching
        if ((id != null && !id.getValue().equals(cm.getId()))
            || (url != null && !url.getValue().equals(cm.getUrl()))) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP id/url mismatch");
          }
          continue;
        }

        // Check source system
        if (sourceSystem != null) {
          if (logger.isDebugEnabled()) {
            logger.debug("Checking sourceSystem match: expected={}, actual={}",
                sourceSystem.getValue(),
                cm.getSourceScope() != null ? ((UriType) cm.getSourceScope()).getValue() : "null");
          }

          if (cm.getSourceScope() == null || ((UriType) cm.getSourceScope()).getValue() == null
              || !((UriType) cm.getSourceScope()).getValue().equals(sourceSystem.getValue())) {
            if (logger.isDebugEnabled()) {
              logger.debug("  SKIP sourceSystem mismatch = {}", sourceSystem.getValue());
            }
            continue;
          }
        }

        // Check target system
        if (targetSystem != null) {
          if (logger.isDebugEnabled()) {
            logger.debug("Checking targetSystem match: expected={}, actual={}",
                targetSystem.getValue(),
                cm.getTargetScope() != null ? ((UriType) cm.getTargetScope()).getValue() : "null");
          }

          if (cm.getTargetScope() == null || ((UriType) cm.getTargetScope()).getValue() == null
              || !((UriType) cm.getTargetScope()).getValue().equals(targetSystem.getValue())) {
            if (logger.isDebugEnabled()) {
              logger.debug("  SKIP targetSystem mismatch = {}", targetSystem.getValue());
            }
            continue;
          }
        }

        // Check version
        if (version != null && !version.getValue().equals(cm.getVersion())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP version mismatch: expected={}, actual={}", version.getValue(),
                cm.getVersion());
          }
          continue;
        }

        list.add(cm);
        if (logger.isDebugEnabled()) {
          logger.debug("  ADDED to results");
        }
      }

      // Sort and return
      final List<ConceptMap> list2 = list.stream()
          .sorted((a, b) -> a.getUrl().compareTo(b.getUrl())).collect(Collectors.toList());
      if (logger.isDebugEnabled()) {
        logger.debug("Returning bundle with {} concept maps", list2.size());
      }
      return FhirUtilityR5.makeBundle(request, list2, count, offset);

    } catch (final Exception e) {
      logger.error("Unexpected error finding concept maps", e);
      throw FhirUtilityR5.exception("Failed to find concept maps", IssueType.EXCEPTION,
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Perform the lookup in the instance map.
   *
   * <pre>
   * Parameters for all resources
   *   used: _id
   *   not used: _content, _filter, _has, _in, _language, _lastUpdated,
   *             _list, _profile, _query, _security, _source, _tag, _text, _type
   * https://hl7.org/fhir/R5/conceptmap-operation-translate.html
   * The following parameters in the operation are not used
   * &#64;OptionalParam(name="dependency") TokenParam dependency,
   * &#64;OperationParam(name = "conceptMap") ConceptMap conceptMap,
   * &#64;OperationParam(name = "conceptMapVersion") String conceptMapVersion,
   * &#64;OperationParam(name = "source") UriType source,
   * &#64;OperationParam(name = "target") String target,
   * &#64;OperationParam(name = "reverse") BooleanType reverse
   * </pre>
   *
   * @param request the request
   * @param response the response
   * @param details the details
   * @param id the id
   * @param url the url
   * @param conceptMapVersion the concept map version
   * @param sourceCode the source code
   * @param sourceSystem the source system
   * @param sourceVersion the version
   * @param sourceScope the source scope
   * @param sourceCoding the source coding
   * @param targetCode the target code
   * @param targetCoding the target coding
   * @param targetScope the target scope
   * @param targetSystem the target system
   * @return the parameters
   * @throws Exception the exception
   */
  @Operation(name = "$translate", idempotent = true)
  public Parameters translateInstance(final HttpServletRequest request,
    final HttpServletResponse response, final ServletRequestDetails details,
    @IdParam final IdType id, @OperationParam(name = "url") final UriType url,
    @OperationParam(name = "conceptMapVersion") final StringType conceptMapVersion,
    @OperationParam(name = "sourceCode") final CodeType sourceCode,
    @OperationParam(name = "system") final UriType sourceSystem,
    @OperationParam(name = "version") final StringType sourceVersion,
    @OperationParam(name = "sourceScope") final UriType sourceScope,
    @OperationParam(name = "sourceCoding") final Coding sourceCoding,
    @OperationParam(name = "targetCode") final CodeType targetCode,
    @OperationParam(name = "targetCoding") final Coding targetCoding,
    @OperationParam(name = "targetScope") final UriType targetScope,
    @OperationParam(name = "targetSystem") final UriType targetSystem) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("translateInstance called with parameters:");
      logger.debug("  id: {}", id);
      logger.debug("  url: {}", url);
      logger.debug("  sourceCode: {}", sourceCode);
      logger.debug("  system: {}", sourceSystem);
      logger.debug("  sourceCoding: {}", sourceCoding);
      logger.debug("  targetCode: {}", targetCode);
      logger.debug("  targetCoding: {}", targetCoding);
    }
    // Reject post
    if (request.getMethod().equals("POST")) {
      logger.warn("POST method not supported for $translate");
      throw FhirUtilityR5.exception("POST method not supported for $translate",
          IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    FhirUtilityR5.requireExactlyOneOf("sourceCode", sourceCode, "sourceCoding", sourceCoding,
        "targetCode", targetCode, "targetCoding", targetCoding);
    FhirUtilityR5.mutuallyExclusive("targetScope", targetScope, "targetSystem", targetSystem);

    try {
      final String sourceCodeStr = FhirUtilityR5.getCode(sourceCode, sourceCoding);
      final String targetCodeStr = FhirUtilityR5.getCode(targetCode, targetCoding);
      if (logger.isDebugEnabled()) {
        logger.debug("Extracted codes - sourceCodeStr: {}, targetCodeStr: {}", sourceCodeStr,
            targetCodeStr);
      }

      final Terminology sourceTerminology = FhirUtilityR5.getTerminology(searchService, null,
          sourceCode, "sourceSystem", sourceSystem, sourceVersion, sourceCoding, true);
      final Terminology targetTerminology = FhirUtilityR5.getTerminology(searchService, null,
          targetCode, "targetSystem", targetSystem, sourceVersion, targetCoding, true);
      if (logger.isDebugEnabled()) {
        logger.debug("Retrieved terminologies - source: {}, target: {}",
            sourceTerminology != null ? sourceTerminology.getAbbreviation() : "null",
            targetTerminology != null ? targetTerminology.getAbbreviation() : "null");
      }

      // Get all mapsets and then restrict
      final List<ConceptMap> candidates = findCandidates();
      if (logger.isDebugEnabled()) {
        logger.debug("Found {} candidate concept maps", candidates.size());
      }
      for (final ConceptMap cm : candidates) {

        // Skip non-matching
        if ((id != null && !id.getIdPart().equals(cm.getId()))
            || (url != null && !url.getValue().equals(cm.getUrl()))) {
          continue;
        }

        if (conceptMapVersion != null && !conceptMapVersion.getValue().equals(cm.getVersion())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP concept map version mismatch = " + cm.getVersion());
          }
          continue;
        }
        if (sourceScope != null
            && !sourceScope.getValue().equals(((UriType) cm.getSourceScope()).getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP sourceScope mismatch = {}, {}", sourceScope.getValue(),
                ((UriType) cm.getSourceScope()).getValue());
          }
          continue;
        }
        if (targetScope != null
            && !targetScope.getValue().equals(((UriType) cm.getTargetScope()).getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP targetScope mismatch = {}, {}", targetScope.getValue(),
                ((UriType) cm.getTargetScope()).getValue());
          }
          continue;
        }
        if (targetSystem != null && cm.getTargetScope() != null
            && ((UriType) cm.getTargetScope()).getValue() != null
            && !((UriType) cm.getTargetScope()).getValue().startsWith(targetSystem.getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP targetSystem mismatch = {}",
                ((UriType) cm.getTargetScope()).getValue());
          }
          continue;
        }
        final boolean reverse = sourceCodeStr == null;
        return translateHelper(ModelUtility.asList(cm), sourceTerminology, targetTerminology,
            reverse ? targetCodeStr : sourceCodeStr, reverse);
      }

      throw FhirUtilityR5.exception("Concept map not found = " + url, IssueType.NOTFOUND,
          HttpServletResponse.SC_NOT_FOUND);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR5.exception("Failed to translate concept map",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Perform the lookup in the implicit map.
   *
   * <pre>
   * Parameters for all resources
   *   used: _id
   *   not used: _content, _filter, _has, _in, _language, _lastUpdated,
   *             _list, _profile, _query, _security, _source, _tag, _text, _type
   * https://hl7.org/fhir/R5/conceptmap-operation-translate.html
   * The following parameters in the operation are not used
   * &#64;OptionalParam(name="dependency") TokenParam dependency,
   * &#64;OperationParam(name = "conceptMap") ConceptMap conceptMap,
   * &#64;OperationParam(name = "conceptMapVersion") String conceptMapVersion,
   * &#64;OperationParam(name = "source") UriType source,
   * &#64;OperationParam(name = "target") String target,
   * &#64;OperationParam(name = "reverse") BooleanType reverse
   * </pre>
   *
   * @param request the request
   * @param response the response
   * @param details the details
   * @param url the url
   * @param conceptMapVersion the concept map version
   * @param sourceCode the code
   * @param sourceSystem the source system
   * @param sourceVersion the version
   * @param sourceScope the source scope
   * @param sourceCoding the coding
   * @param targetCode the target code
   * @param targetCoding the target coding
   * @param targetScope the target scope
   * @param targetSystem the target system
   * @return the parameters
   * @throws Exception the exception
   */
  @Operation(name = "$translate", idempotent = true)
  public Parameters translateImplicit(final HttpServletRequest request,
    final HttpServletResponse response, final ServletRequestDetails details,
    @OperationParam(name = "url") final UriType url,
    @OperationParam(name = "conceptMapVersion") final StringType conceptMapVersion,
    @OperationParam(name = "sourceCode") final CodeType sourceCode,
    @OperationParam(name = "system") final UriType sourceSystem,
    @OperationParam(name = "version") final StringType sourceVersion,
    @OperationParam(name = "sourceScope") final UriType sourceScope,
    @OperationParam(name = "sourceCoding") final Coding sourceCoding,
    @OperationParam(name = "targetCode") final CodeType targetCode,
    @OperationParam(name = "targetCoding") final Coding targetCoding,
    @OperationParam(name = "targetScope") final UriType targetScope,
    @OperationParam(name = "targetSystem") final UriType targetSystem) throws Exception {

    // Reject post
    if (request.getMethod().equals("POST")) {
      throw FhirUtilityR5.exception("POST method not supported for $translate",
          IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    FhirUtilityR5.requireExactlyOneOf("sourceCode", sourceCode, "sourceCoding", sourceCoding,
        "targetCode", targetCode, "targetCoding", targetCoding);
    FhirUtilityR5.mutuallyExclusive("targetScope", targetScope, "targetSystem", targetSystem);

    try {

      final String sourceCodeStr = FhirUtilityR5.getCode(sourceCode, sourceCoding);
      final String targetCodeStr = FhirUtilityR5.getCode(targetCode, targetCoding);
      final Terminology sourceTerminology = FhirUtilityR5.getTerminology(searchService, null,
          sourceCode, "sourceSystem", sourceSystem, sourceVersion, sourceCoding, true);
      final Terminology targetTerminology = FhirUtilityR5.getTerminology(searchService, null,
          targetCode, "targetSystem", targetSystem, null, targetCoding, true);

      // Get all mapsets and then restrict
      final List<ConceptMap> candidates = findCandidates();
      final List<ConceptMap> list = new ArrayList<>();
      for (final ConceptMap cm : candidates) {

        // Skip non-matching
        if ((url != null && !url.getValue().equals(cm.getUrl()))) {
          continue;
        }
        if (conceptMapVersion != null && !conceptMapVersion.getValue().equals(cm.getVersion())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP concept map version mismatch = {}", cm.getVersion());
          }
          continue;
        }
        if (sourceScope != null
            && !sourceScope.getValue().equals(((UriType) cm.getSourceScope()).getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP sourceScope mismatch = {}, {}", sourceScope.getValue(),
                ((UriType) cm.getSourceScope()).getValue());
          }
          continue;
        }
        if (targetScope != null
            && !targetScope.getValue().equals(((UriType) cm.getTargetScope()).getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP targetScope mismatch = {}, {}", targetScope.getValue(),
                ((UriType) cm.getTargetScope()).getValue());
          }
          continue;
        }
        if (targetSystem != null && cm.getTargetScope() != null
            && ((UriType) cm.getTargetScope()).getValue() != null
            && !((UriType) cm.getTargetScope()).getValue().startsWith(targetSystem.getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP targetSystem mismatch = {}, {}", targetSystem.getValue(),
                ((UriType) cm.getTargetScope()).getValue());
          }
          continue;
        }

        list.add(cm);
      }

      if (!list.isEmpty()) {
        final boolean reverse = sourceCodeStr == null;
        return translateHelper(list, sourceTerminology, targetTerminology,
            reverse ? targetCodeStr : sourceCodeStr, reverse);
      }

      // We can either fail with params indicating no result, or with 404
      // final Parameters parameters = new Parameters();
      // parameters.addParameter("result", false);
      // parameters.addParameter("message", "No concept maps found matching
      // specified
      // critiera");
      // return parameters;

      throw FhirUtilityR5.exception("Concept map not found = " + url, IssueType.NOTFOUND,
          HttpServletResponse.SC_NOT_FOUND);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR5.exception("Failed to translate concept map",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Deletes the concept map.
   *
   * @param request the request
   * @param details the details
   * @param id the id
   * @throws Exception the exception
   */
  @Delete
  public void deleteConceptMap(final HttpServletRequest request,
    final ServletRequestDetails details, @IdParam final IdType id) throws Exception {

    try {
      if (id == null || id.getIdPart() == null) {
        throw FhirUtilityR5.exception("Concept Map ID required for delete", IssueType.INVALID,
            HttpServletResponse.SC_BAD_REQUEST);
      }

      logger.info("Delete concept map with ID: {}", id.getIdPart());

      final Mapset mapset = searchService.get(id.getIdPart(), Mapset.class);
      if (mapset == null) {
        throw FhirUtilityR5.exception("Concept map not found = " + id.getIdPart(),
            IssueType.NOTFOUND, HttpServletResponse.SC_NOT_FOUND);
      }

      TerminologyUtility.removeMapset(searchService, mapset.getId());

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected error deleting concept map", e);
      throw FhirUtilityR5.exception("Failed to delete concept map: " + e.getMessage(),
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /* see superclass */
  @Override
  public Class<ConceptMap> getResourceType() {
    return ConceptMap.class;
  }

  /**
   * Find candidates.
   *
   * @return the list
   * @throws Exception the exception
   */
  public List<ConceptMap> findCandidates() throws Exception {
    if (logger.isDebugEnabled()) {
      logger.info("Finding candidate concept maps from Lucene");
    }

    try {
      // Lookup and filter mapsets
      final List<ConceptMap> list = new ArrayList<>();
      for (final Mapset mapset : FhirUtility.lookupMapsets(searchService)) {
        final ConceptMap cm = FhirUtilityR5.toR5(mapset);
        list.add(cm);
      }
      return list;

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR5.exception("Failed to find concept maps",
          OperationOutcome.IssueType.EXCEPTION, 500);
    }
  }

  /**
   * Translate helper.
   *
   * @param maps the maps
   * @param sourceTerminology the source terminology
   * @param targetTerminology the target terminology
   * @param code the code
   * @param reverse the reverse
   * @return the parameters
   * @throws Exception the exception
   */
  private Parameters translateHelper(final List<ConceptMap> maps,
    final Terminology sourceTerminology, final Terminology targetTerminology, final String code,
    final boolean reverse) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.info("translateHelper called with parameters:");
      logger.info("  maps size: {}", maps.size());
      logger.info("  sourceTerminology: {}",
          sourceTerminology != null ? sourceTerminology.getAbbreviation() : "null");
      logger.info("  targetTerminology: {}",
          targetTerminology != null ? targetTerminology.getAbbreviation() : "null");
      logger.info("  code: {}", code);
      logger.info("  reverse: {}", reverse);
    }
    final Parameters parameters = new Parameters();
    final List<ParametersParameterComponent> matches = new ArrayList<>();

    for (final ConceptMap map : maps) {
      // Get the identifier from the map
      final String mapsetCode = map.getIdentifier().get(0).getValue();
      logger.info("Processing concept map: id={}, code={}, url={}", map.getId(), mapsetCode,
          map.getUrl());

      final List<Mapping> mappings =
          searchService.find(new SearchParameters(StringUtility.composeQuery("AND",
              // code clause
              (reverse ? "to.code:" : "from.code:") + StringUtility.escapeQuery(code),
              // terminology clause (null if null) - no reversing
              sourceTerminology == null ? null
                  : ("from.terminology:"
                      + StringUtility.escapeQuery(sourceTerminology.getAbbreviation())),
              targetTerminology == null ? null
                  : ("to.terminology:"
                      + StringUtility.escapeQuery(targetTerminology.getAbbreviation())),
              // mapset clauses
              "mapset.abbreviation:" + StringUtility.escapeQuery(map.getTitle()),
              "mapset.version:" + StringUtility.escapeQuery(map.getVersion()),
              "mapset.code:" + mapsetCode), null, 1000, null, null), Mapping.class).getItems();

      if (logger.isDebugEnabled()) {
        logger.info("Found {} mappings for concept map {}", mappings.size(), map.getId());
      }
      if (!mappings.isEmpty()) {
        for (final Mapping mapping : mappings) {
          final ParametersParameterComponent match = new ParametersParameterComponent();
          match.setName("match");
          match.addPart().setName("equivalence").setValue(new StringType("inexact"));
          if (reverse) {
            match.addPart().setName("concept")

                .setValue(new Coding()
                    .setSystem(FhirUtility.getCodeSystemUri(searchService, mapping.getFrom()))
                    .setCode(mapping.getFrom().getCode()).setDisplay(mapping.getFrom().getName()));
          } else {
            // Skip blank mappings
            if (mapping.getTo().getCode().isEmpty()) {
              continue;
            }
            match.addPart().setName("concept")
                .setValue(new Coding()
                    .setSystem(FhirUtility.getCodeSystemUri(searchService, mapping.getTo()))
                    .setCode(mapping.getTo().getCode()).setDisplay(mapping.getTo().getName()));
          }
          match.addPart().setName("source").setValue(new StringType(map.getUrl()));
          matches.add(match);
        }
      }

    }

    // If matches, proceed
    if (!matches.isEmpty()) {
      parameters.addParameter("result", true);
      for (final ParametersParameterComponent match : matches) {
        parameters.addParameter(match);
      }
      return parameters;
    }

    // OR:::

    // {
    // "resourceType": "Parameters",
    // "parameter": [ {
    // "name": "result",
    // "valueBoolean": false
    // }, {
    // "name": "message",
    // "valueString": "No mapping found for code '90708003', system
    // 'http://snomed.info/sct'."
    // } ]
    // }
    parameters.addParameter("result", false);
    parameters.addParameter("message", "No mapping found matching specified critiera = "
        + maps.stream().map(m -> m.getId()).collect(Collectors.toSet()));
    return parameters;
  }

  /**
   * Creates the concept map.
   *
   * @param bytes the bytes
   * @return the method outcome (as required by HAPI)
   * @throws Exception the exception
   */
  @Create
  public MethodOutcome createConceptMap(@ResourceParam final byte[] bytes) throws Exception {
    try {

      logger.info("Create concept map R5");

      // Write to a file so we can re-open streams against it
      final File file = File.createTempFile("tmp", ".json");
      FileUtils.writeByteArrayToFile(file, bytes);

      final ConceptMap conceptMap =
          ConceptMapLoaderUtil.loadConceptMap(searchService, file, ConceptMap.class);

      FileUtils.delete(file);

      // Return success
      final MethodOutcome out = new MethodOutcome();
      final IdType id = new IdType("ConceptMap", conceptMap.getId());
      out.setId(id);
      out.setResource(conceptMap);
      out.setCreated(true);

      final OperationOutcome outcome = new OperationOutcome();
      outcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.INFORMATION)
          .setCode(OperationOutcome.IssueType.INFORMATIONAL)
          .setDiagnostics("ConceptMap created = " + conceptMap.getId());
      out.setOperationOutcome(outcome);

      return out;

    } catch (FHIRServerResponseException fe) {
      throw fe;
    } catch (final Exception e) {
      logger.error("Unexpected error creating concept map", e);
      throw FhirUtilityR5.exception(e.getMessage(), IssueType.EXCEPTION,
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
