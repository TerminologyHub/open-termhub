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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.wci.termhub.lucene.eventing.Write;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
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
 * The ConceptMap provider.
 */
@Component
public class ConceptMapProviderR4 implements IResourceProvider {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(ConceptMapProviderR4.class);

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

      final List<ConceptMap> candidates = findCandidates();
      for (final ConceptMap map : candidates) {
        if (id != null && id.getIdPart().equals(map.getId())) {
          return map;
        }
      }
      throw FhirUtilityR4.exception(
          "Concept map not found = " + (id == null ? "null" : id.getIdPart()), IssueType.NOTFOUND,
          HttpServletResponse.SC_NOT_FOUND);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to load concept map",
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
   * https://hl7.org/fhir/R4/conceptmap.html (see Search Parameters)
   * The following parameters in the registry are not used
   * &#64;OptionalParam(name="context") TokenParam context,
   * &#64;OptionalParam(name="context-quantity") QuantityParam contextQuantity,
   * &#64;OptionalParam(name="context-type") String contextType,
   * &#64;OptionalParam(name="context-type-quantity") QuantityParam contextTypeQuantity,
   * &#64;OptionalParam(name="context-type-value") String contextTypeValue,
   * &#64;OptionalParam(name="dependsOn") String dependsOn,
   * &#64;OptionalParam(name="identifier") StringParam identifier,
   * &#64;OptionalParam(name="jurisdiction") StringParam jurisdiction,
   * &#64;OptionalParam(name="other") ReferenceParam other,
   * &#64;OptionalParam(name="product") String product,
   * &#64;OptionalParam(name="source") ReferenceParam source,
   * &#64;OptionalParam(name="source-code") TokenParam source,
   * &#64;OptionalParam(name="source-uri") ReferenceParamsourceUri,
   * &#64;OptionalParam(name="source") ReferenceParam source,
   * &#64;OptionalParam(name="status") String status,
   * &#64;OptionalParam(name="target") ReferenceParam source,
   * &#64;OptionalParam(name="target-code") TokenParam source,
   * &#64;OptionalParam(name="target-system") String sourceSystem,
   * &#64;OptionalParam(name="target-uri") ReferenceParamsourceUri,
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
   * @param sourceCode the source code
   * @param sourceSystem the source system
   * @param targetCode the target code
   * @param targetSystem the target system
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
    @OptionalParam(name = "date") final DateRangeParam date,
    @OptionalParam(name = "description") final StringParam description,
    @OptionalParam(name = "identifier") final StringParam identifier,
    @OptionalParam(name = "name") final StringParam name,
    @OptionalParam(name = "publisher") final StringParam publisher,
    @OptionalParam(name = "source-code") final TokenParam sourceCode,
    @OptionalParam(name = "source-system") final UriParam sourceSystem,
    @OptionalParam(name = "target-code") final TokenParam targetCode,
    @OptionalParam(name = "target-system") final UriParam targetSystem,
    @OptionalParam(name = "title") final StringParam title,
    @OptionalParam(name = "url") final StringParam url,
    @OptionalParam(name = "version") final StringParam version,
    @Description(shortDefinition = "Number of entries to return")
    @OptionalParam(name = "_count") final NumberParam count,
    @Description(shortDefinition = "Start offset, used when reading a next page")
    @OptionalParam(name = "_offset") final NumberParam offset) throws Exception {

    try {

      FhirUtilityR4.notSupportedSearchParams(request);

      // Lookup source codes if they're specified
      final Set<String> mapsetsMatchingSourceCodes = new HashSet<>();
      if (sourceCode != null) {
        if (sourceSystem != null) {
          mapsetsMatchingSourceCodes
              .addAll(
                  searchService
                      .find(new SearchParameters(
                          "fromTerminology:"
                              + FhirUtility.lookupTerminology(searchService,
                                  sourceSystem.getValue())
                              + " AND from.code:"
                              + StringUtility.escapeQuery(sourceCode.getValue()),
                          null, 2000, null, null), Mapping.class)
                      .getItems().stream().map(m -> m.getMapset().getCode())
                      .collect(Collectors.toSet()));
        } else {
          mapsetsMatchingSourceCodes.addAll(searchService
              .find(new SearchParameters(
                  "from.code:" + StringUtility.escapeQuery(sourceCode.getValue()), null, 2000, null,
                  null), Mapping.class)
              .getItems().stream().map(m -> m.getMapset().getCode()).collect(Collectors.toSet()));
        }
      }
      // Lookup target codes if they're specified
      final Set<String> mapsetsMatchingTargetCodes = new HashSet<>();
      if (targetCode != null) {
        if (targetSystem != null) {
          mapsetsMatchingTargetCodes
              .addAll(
                  searchService
                      .find(new SearchParameters(
                          "toTerminology:"
                              + FhirUtility.lookupTerminology(searchService,
                                  targetSystem.getValue())
                              + " AND to.code:" + StringUtility.escapeQuery(targetCode.getValue()),
                          null, 2000, null, null), Mapping.class)
                      .getItems().stream().map(m -> m.getMapset().getCode())
                      .collect(Collectors.toSet()));
        } else {
          mapsetsMatchingTargetCodes.addAll(searchService
              .find(new SearchParameters(
                  "to.code:" + StringUtility.escapeQuery(targetCode.getValue()), null, 2000, null,
                  null), Mapping.class)
              .getItems().stream().map(m -> m.getMapset().getCode()).collect(Collectors.toSet()));
        }
      }

      final List<ConceptMap> list = new ArrayList<>();
      final List<ConceptMap> candidates = findCandidates();
      for (final ConceptMap cm : candidates) {

        // Skip non-matching
        if ((id != null && !id.getValue().equals(cm.getId()))
            || (url != null && !url.getValue().equals(cm.getUrl()))) {
          continue;
        }
        if (identifier != null
            && !FhirUtility.compareString(identifier, cm.getIdentifier().getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP identifier mismatch = {}", cm.getIdentifier().getValue());
          }
          continue;
        }
        if (date != null && !FhirUtility.compareDateRange(date, cm.getDate())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP date mismatch = {}", cm.getDate());
          }
          continue;
        }
        if (description != null && !FhirUtility.compareString(description, cm.getDescription())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP description mismatch = {}", cm.getDescription());
          }
          continue;
        }
        if (name != null && !FhirUtility.compareString(name, cm.getName())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP name mismatch = {}", cm.getName());
          }
          continue;
        }
        if (publisher != null && !FhirUtility.compareString(publisher, cm.getPublisher())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP publisher mismatch = {}", cm.getPublisher());
          }
          continue;
        }
        if (title != null && !FhirUtility.compareString(title, cm.getTitle())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP title mismatch = {}", cm.getTitle());
          }
          continue;
        }
        if (version != null && !FhirUtility.compareString(version, cm.getVersion())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP version mismatch = {}", cm.getVersion());
          }
          continue;
        }

        if (logger.isDebugEnabled()) {
          logger.debug(" sourceSystem = {}, targetSystem = {}", cm.getSource(), cm.getTarget());
        }

        if (sourceSystem != null && (cm.getSourceUriType() == null
            || cm.getSourceUriType().getValue() == null || !cm.getSourceUriType().getValue()
                .replace("?fhir_vs", "").equals(sourceSystem.getValue()))) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP sourceSystem mismatch = {}, {}", sourceSystem.getValue(),
                cm.getSourceUriType() != null ? cm.getSourceUriType().getValue() : "null");
          }
          continue;
        }
        if (targetSystem != null && (cm.getTargetUriType() == null
            || cm.getTargetUriType().getValue() == null || !cm.getTargetUriType().getValue()
                .replace("?fhir_vs", "").equals(targetSystem.getValue()))) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP targetSystem mismatch = {}, {}", targetSystem.getValue(),
                cm.getTargetUriType() != null ? cm.getTargetUriType().getValue() : "null");
          }
          continue;
        }

        if (sourceCode != null && !mapsetsMatchingSourceCodes.contains(sourceCode.getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP source-code not found = {}", sourceCode.getValue());
          }
          continue;
        }
        if (targetCode != null && !mapsetsMatchingTargetCodes.contains(targetCode.getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP target-code not found = {}", targetCode.getValue());
          }
          continue;
        }
        list.add(cm);
      }

      // Sort the results by uri.
      final List<ConceptMap> list2 =
          list.stream().sorted((a, b) -> a.getUrl().compareTo(b.getUrl())).toList();
      return FhirUtilityR4.makeBundle(request, list2, count, offset);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to find concept maps",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
   * https://hl7.org/fhir/R4/conceptmap-operation-translate.html
   * The following parameters in the operation are not used
   * &#64;OptionalParam(name="dependency") ?? dependency
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
   * @param code the code
   * @param system the system
   * @param version the version
   * @param coding the coding
   * @param codeableConcept the codeable concept
   * @param source the source
   * @param target the target
   * @param targetSystem the target system
   * @param reverse the reverse
   * @return the parameters
   * @throws Exception the exception
   */
  @Operation(name = "$translate", idempotent = true)
  public Parameters translateInstance(final HttpServletRequest request,
    final HttpServletResponse response, final ServletRequestDetails details,
    @IdParam final IdType id, @OperationParam(name = "url") final UriType url,
    @OperationParam(name = "conceptMapVersion") final StringType conceptMapVersion,
    @OperationParam(name = "code") final CodeType code,
    @OperationParam(name = "system") final UriType system,
    @OperationParam(name = "version") final StringType version,
    @OperationParam(name = "coding") final Coding coding,
    @OperationParam(name = "codeableConcept") final CodeableConcept codeableConcept,
    @OperationParam(name = "source") final UriType source,
    @OperationParam(name = "target") final UriType target,
    @OperationParam(name = "targetsystem") final UriType targetSystem,
    @OperationParam(name = "reverse") final BooleanType reverse) throws Exception {

    // Reject post
    if (request.getMethod().equals("POST")) {
      throw FhirUtilityR4.exception("POST method not supported for $translate",
          IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    FhirUtilityR4.requireExactlyOneOf("code", code, "coding", coding, "codeableConcept",
        codeableConcept);
    // FhirUtilityR4.mutuallyRequired("code", code, "system", system);
    FhirUtilityR4.mutuallyExclusive("code", code, "coding", coding);

    try {

      final String codeStr = FhirUtilityR4.getCode(code, coding);
      final Terminology terminology = FhirUtilityR4.getTerminology(searchService, null, code,
          "system", system, version, coding, true);

      // Get all mapsets and then restrict
      final List<ConceptMap> candidates = findCandidates();
      for (final ConceptMap cm : candidates) {

        // Skip non-matching
        if ((id != null && !id.getIdPart().equals(cm.getId()))
            || (url != null && !url.getValue().equals(cm.getUrl()))) {
          continue;
        }

        if (conceptMapVersion != null && !conceptMapVersion.getValue().equals(cm.getVersion())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP concept map version mismatch = {}", cm.getVersion());
          }
          continue;
        }
        if (source != null && !source.getValue().equals(cm.getSourceUriType().getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP source mismatch = {}, {}", source.getValue(),
                cm.getSourceUriType().getValue());
          }
          continue;
        }
        if (target != null && !target.getValue().equals(cm.getTargetUriType().getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP target mismatch = {}, {}", target.getValue(),
                cm.getTargetUriType().getValue());
          }
          continue;
        }
        if (targetSystem != null
            && !cm.getTargetUriType().getValue().startsWith(targetSystem.getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP targetSystem mismatch = {}, {}", targetSystem.getValue(),
                cm.getTargetUriType().getValue());
          }
          continue;
        }
        return translateHelper(ModelUtility.asList(cm), terminology, codeStr,
            reverse != null && reverse.getValue());
      }

      throw FhirUtilityR4.exception("Concept map not found = " + url, IssueType.NOTFOUND,
          HttpServletResponse.SC_NOT_FOUND);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to translate concept map",
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
   * https://hl7.org/fhir/R4/conceptmap-operation-translate.html
   * The following parameters in the operation are not used
   * &#64;OptionalParam(name="dependency") ?? dependency
   * &#64;OperationParam(name = "conceptMap") ConceptMap conceptMap,
   * &#64;OperationParam(name = "conceptMapVersion") String conceptMapVersion,
   * &#64;OperationParam(name = "source") String source,
   * &#64;OperationParam(name = "target") String target,
   * &#64;OperationParam(name = "reverse") BooleanType reverse
   * </pre>
   *
   * @param request the request
   * @param response the response
   * @param details the details
   * @param url the url
   * @param conceptMapVersion the concept map version
   * @param code the code
   * @param system the system
   * @param version the version
   * @param coding the coding
   * @param codeableConcept the codeable concept
   * @param source the source
   * @param target the target
   * @param targetSystem the target system
   * @param reverse the reverse
   * @return the parameters
   * @throws Exception the exception
   */
  @Operation(name = "$translate", idempotent = true)
  public Parameters translateImplicit(final HttpServletRequest request,
    final HttpServletResponse response, final ServletRequestDetails details,
    @OperationParam(name = "url") final UriType url,
    @OperationParam(name = "conceptMapVersion") final StringType conceptMapVersion,
    @OperationParam(name = "code") final CodeType code,
    @OperationParam(name = "system") final UriType system,
    @OperationParam(name = "version") final StringType version,
    @OperationParam(name = "coding") final Coding coding,
    @OperationParam(name = "codeableConcept") final CodeableConcept codeableConcept,
    @OperationParam(name = "source") final UriType source,
    @OperationParam(name = "target") final UriType target,
    @OperationParam(name = "targetSystem") final UriType targetSystem,
    @OperationParam(name = "reverse") final BooleanType reverse) throws Exception {

    // Reject post
    if (request.getMethod().equals("POST")) {
      throw FhirUtilityR4.exception("POST method not supported for $translate",
          IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    FhirUtilityR4.requireExactlyOneOf("code", code, "coding", coding, "codeableConcept",
        codeableConcept);
    // FhirUtilityR4.mutuallyRequired("code", code, "system", system);
    FhirUtilityR4.mutuallyExclusive("target", target, "targetSystem", targetSystem);

    try {

      final String codeStr = FhirUtilityR4.getCode(code, coding);
      final Terminology terminology = FhirUtilityR4.getTerminology(searchService, null, code,
          "system", system, version, coding, true);

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
        if (source != null && !source.getValue().equals(cm.getSourceUriType().getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP source mismatch = " + source.getValue() + ", "
                + cm.getSourceUriType().getValue());
          }
          continue;
        }
        if (target != null && !target.getValue().equals(cm.getTargetUriType().getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP target mismatch = " + target.getValue() + ", "
                + cm.getTargetUriType().getValue());
          }
          continue;
        }
        if (targetSystem != null
            && !cm.getTargetUriType().getValue().startsWith(targetSystem.getValue())) {
          if (logger.isDebugEnabled()) {
            logger.debug("  SKIP targetSystem mismatch = " + targetSystem.getValue() + ", "
                + cm.getTargetUriType().getValue());
          }
          continue;
        }

        list.add(cm);
      }

      if (!list.isEmpty()) {
        return translateHelper(list, terminology, codeStr, reverse != null && reverse.getValue());
      }

      // We can either fail with params indicating no result, or with 404
      // final Parameters parameters = new Parameters();
      // parameters.addParameter("result", false);
      // parameters.addParameter("message", "No concept maps found matching
      // specified
      // critiera");
      // return parameters;

      throw FhirUtilityR4.exception("Concept map not found = " + url, IssueType.NOTFOUND,
          HttpServletResponse.SC_NOT_FOUND);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to translate concept map",
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
  @Write
  public void deleteConceptMap(final HttpServletRequest request,
    final ServletRequestDetails details, @IdParam final IdType id) throws Exception {

    try {
      if (id == null || id.getIdPart() == null) {
        throw FhirUtilityR4.exception("Concept Map ID required for delete", IssueType.INVALID,
            HttpServletResponse.SC_BAD_REQUEST);
      }

      logger.info("Delete concept map with ID: {}", id.getIdPart());

      final Mapset mapset = searchService.get(id.getIdPart(), Mapset.class);
      if (mapset == null) {
        throw FhirUtilityR4.exception("Concept map not found = " + id.getIdPart(),
            IssueType.NOTFOUND, HttpServletResponse.SC_NOT_FOUND);
      }

      TerminologyUtility.removeMapset(searchService, mapset.getId());

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected error deleting concept map", e);
      throw FhirUtilityR4.exception("Failed to delete concept map: " + e.getMessage(),
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

    try {
      // Lookup and filter mapsets
      final List<ConceptMap> list = new ArrayList<>();
      for (final Mapset mapset : FhirUtility.lookupMapsets(searchService)) {
        final ConceptMap cm = FhirUtilityR4.toR4(mapset);
        list.add(cm);
      }
      return list;

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to find concept maps",
          OperationOutcome.IssueType.EXCEPTION, 500);
    }
  }

  /**
   * Translate helper.
   *
   * @param maps the maps
   * @param terminology the terminology
   * @param code the code
   * @param reverse the reverse
   * @return the parameters
   * @throws Exception the exception
   */
  private Parameters translateHelper(final List<ConceptMap> maps, final Terminology terminology,
    final String code, final boolean reverse) throws Exception {

    final Parameters parameters = new Parameters();
    final List<ParametersParameterComponent> matches = new ArrayList<>();

    // {
    // "resourceType": "Parameters",
    // "parameter": [ {
    // "name": "result",
    // "valueBoolean": true
    // }, {
    // "name": "message",
    // "valueString": "Please observe the following map advice. Group:1,
    // Priority:1,
    // Rule:TRUE,
    // Advice:'ALWAYS N28.9', Map Category:'Map source concept is properly
    // classified'."
    // }, {
    // "name": "match",
    // "part": [ {
    // "name": "equivalence",
    // "valueCode": "unmatched"
    // }, {
    // "name": "concept",
    // "valueCoding": {
    // "system": "http://hl7.org/fhir/sid/icd-10",
    // "code": "N28.9"
    // }
    // }, {
    // "name": "source",
    // "valueString":
    // "http://snomed.info/sct/900000000000207008/version/20240101?fhir_cm=447562003"
    // } ]
    // } ]
    // }
    for (final ConceptMap map : maps) {
      // Get the identifier from the map
      final String mapsetCode = map.getIdentifier().getValue();

      final SearchParameters params = new SearchParameters(StringUtility.composeQuery("AND",
          // code clause
          (reverse ? "to.code:" : "from.code:") + StringUtility.escapeQuery(code),
          // terminology clause (null if null) - no reversing
          terminology == null ? null
              : ("from.terminology:" + StringUtility.escapeQuery(terminology.getAbbreviation())),
          // mapset clauses
          "mapset.abbreviation:" + StringUtility.escapeQuery(map.getTitle()),
          "mapset.version:" + StringUtility.escapeQuery(map.getVersion()),
          "mapset.code:" + StringUtility.escapeQuery(mapsetCode)), null, 2000, null, null);

      final List<Mapping> mappings = searchService.find(params, Mapping.class).getItems();

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
  @Write
  public MethodOutcome createConceptMap(@ResourceParam final byte[] bytes) throws Exception {
    try {

      logger.info("Create concept map R4");

      // Write to a file so we can re-open streams against it
      final File file = File.createTempFile("tmp", ".json");
      FileUtils.writeByteArrayToFile(file, bytes);

      final ConceptMap conceptMap = ConceptMapLoaderUtil.loadConceptMap(searchService, file,
          ConceptMap.class, new DefaultProgressListener());

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
      throw FhirUtilityR4.exception(e.getMessage(), IssueType.EXCEPTION,
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

}
