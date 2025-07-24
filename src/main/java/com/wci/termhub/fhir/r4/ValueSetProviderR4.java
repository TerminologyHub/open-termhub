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

import static com.wci.termhub.util.IndexUtility.getAndQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceDesignationComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionContainsComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionParameterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.handler.BrowserQueryBuilder;
import com.wci.termhub.lucene.LuceneQueryBuilder;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.SubsetMember;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.SubsetLoaderUtil;
import com.wci.termhub.util.TerminologyUtility;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.model.util.JpaConstants;
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
 * The ValueSet provider.
 */
@Component
public class ValueSetProviderR4 implements IResourceProvider {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(ValueSetProviderR4.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Gets the value set.
   *
   * @param request the request
   * @param details the details
   * @param id the id
   * @return the value set
   * @throws Exception the exception
   */
  @Read
  public ValueSet getValueSet(final HttpServletRequest request, final ServletRequestDetails details,
    @IdParam final IdType id) throws Exception {

    try {
      // 1. Check implicit code system ValueSets
      final ValueSet set = getImplicitCodeSystemValueSets().stream()
          .filter(s -> s.getId().equals(id.getIdPart())).findFirst().orElse(null);
      if (set != null) {
        return set;
      }
      // 2. Check loaded ValueSets (Subset)
      final Subset subset = searchService.get(id.getIdPart(), Subset.class);
      if (subset != null && "ValueSet".equals(subset.getCategory())) {
        // Fetch members
        final SearchParameters memberParams = new SearchParameters();
        memberParams.getFilters().put("subset.code", subset.getCode());
        final List<SubsetMember> members =
            searchService.findAll(memberParams, SubsetMember.class).getItems();
        return SubsetLoaderUtil.toR4ValueSet(subset, members);
      }
      throw FhirUtilityR4.exception(
          "Value set not found = " + (id == null ? "null" : id.getIdPart()), IssueType.NOTFOUND,
          HttpServletResponse.SC_NOT_FOUND);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to get value set", OperationOutcome.IssueType.EXCEPTION,
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Find value sets.
   *
   * <pre>
   * Parameters for all resources
   *   used: _id
   *   not used: _content, _filter, _has, _in, _language, _lastUpdated,
   *             _list, _profile, _query, _security, _source, _tag, _text, _type
   * https://hl7.org/fhir/R4/valueset.html (see Search Parameters)
   * The following parameters in the registry are not used
   * &#64;OptionalParam(name="context-quantity") QuantityParam contextQuantity,
   * &#64;OptionalParam(name="context-type") TokenParam contextType,
   * &#64;OptionalParam(name="context-type-quantity") QuantityParam contextTypeQuantity,
   * &#64;OptionalParam(name="context-type-value") CompositeParam contextTypeValue,
   * &#64;OptionalParam(name="date") DateParam date,
   * &#64;OptionalParam(name="expansion") String expansion,
   * &#64;OptionalParam(name="identifier") TokenParam identifier,
   * &#64;OptionalParam(name="jurisdiction") TokenParam jurisdiction,
   * &#64;OptionalParam(name="reference") String reference,
   * </pre>
   *
   * @param request the request
   * @param details the details
   * @param id the id
   * @param code the code
   * @param date the date
   * @param description the description
   * @param identifier the identifier
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
  public Bundle findValueSets(final HttpServletRequest request, final ServletRequestDetails details,
    @OptionalParam(name = "_id") final TokenParam id,
    @OptionalParam(name = "code") final TokenParam code,
    @OptionalParam(name = "date") final DateRangeParam date,
    @OptionalParam(name = "description") final StringParam description,
    @OptionalParam(name = "identifier") final TokenParam identifier,
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

      final List<ValueSet> list = new ArrayList<>();
      // For now (until we have real value sets)
      // Look up implicit value sets for code systems
      for (final Terminology terminology : FhirUtility.lookupTerminologies(searchService)) {
        final ValueSet set = getImplicitCodeSystemValueSet(terminology);

        // Skip non-matching
        if ((id != null && !id.getValue().equals(set.getId()))
            || (url != null && !url.getValue().equals(set.getUrl()))) {
          continue;
        }

        if (date != null && !FhirUtility.compareDate(date, set.getDate())) {
          logger.info("  SKIP date mismatch = {}", set.getDate());
          continue;
        }
        if (description != null && !FhirUtility.compareString(description, set.getDescription())) {
          logger.info("  SKIP description mismatch = {}", set.getDescription());
          continue;
        }
        // TODO: identifier (e.g. for refests that have URIs but also concept
        // ids)
        if (name != null && !FhirUtility.compareString(name, set.getName())) {
          logger.info("  SKIP name mismatch = {}", set.getName());
          continue;
        }
        if (publisher != null && !FhirUtility.compareString(publisher, set.getPublisher())) {
          logger.info("  SKIP publisher mismatch = {}", set.getPublisher());
          continue;
        }
        if (title != null && !FhirUtility.compareString(title, set.getTitle())) {
          logger.info("  SKIP title mismatch = {}", set.getTitle());
          continue;
        }
        if (version != null && !FhirUtility.compareString(version, set.getVersion())) {
          logger.info("  SKIP version mismatch = {}", set.getVersion());
          continue;
        }

        if (code != null
            && TerminologyUtility.getConcept(searchService, terminology, code.getValue()) == null) {
          logger.info("  SKIP code mismatch = {}",
              terminology.getAbbreviation() + " " + code.getValue());
          continue;
        }

        list.add(set);
      }

      // --- Add loaded ValueSets (Subset/SubsetMember) ---
      // Find all Subset where category = "ValueSet"
      final SearchParameters subsetParams = new SearchParameters();
      subsetParams.getFilters().put("category", "ValueSet");
      final List<Subset> subsets = searchService.findAll(subsetParams, Subset.class).getItems();
      for (final Subset subset : subsets) {
        // Fetch SubsetMembers for this subset
        final SearchParameters memberParams = new SearchParameters();
        memberParams.getFilters().put("subset.code", subset.getCode());
        final int pageSize = (count != null) ? count.getValue().intValue() : 10;
        final int pageOffset = (offset != null) ? offset.getValue().intValue() : 0;
        memberParams.setLimit(pageSize);
        memberParams.setOffset(pageOffset);
        final List<SubsetMember> members =
            searchService.findAll(memberParams, SubsetMember.class).getItems();
        final ValueSet set = SubsetLoaderUtil.toR4ValueSet(subset, members);
        // Apply the same filtering as above
        if ((id != null && !id.getValue().equals(set.getId()))
            || (url != null && !url.getValue().equals(set.getUrl()))) {
          continue;
        }
        if (date != null && !FhirUtility.compareDate(date, set.getDate())) {
          logger.info("  SKIP date mismatch = {}", set.getDate());
          continue;
        }
        if (description != null && !FhirUtility.compareString(description, set.getDescription())) {
          logger.info("  SKIP description mismatch = {}", set.getDescription());
          continue;
        }
        if (name != null && !FhirUtility.compareString(name, set.getName())) {
          logger.info("  SKIP name mismatch = {}", set.getName());
          continue;
        }
        if (publisher != null && !FhirUtility.compareString(publisher, set.getPublisher())) {
          logger.info("  SKIP publisher mismatch = {}", set.getPublisher());
          continue;
        }
        if (title != null && !FhirUtility.compareString(title, set.getTitle())) {
          logger.info("  SKIP title mismatch = {}", set.getTitle());
          continue;
        }
        if (version != null && !FhirUtility.compareString(version, set.getVersion())) {
          logger.info("  SKIP version mismatch = {}", set.getVersion());
          continue;
        }
        // No code filter for loaded sets
        list.add(set);
      }

      return FhirUtilityR4.makeBundle(request, list, count, offset);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to find value sets",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Expand implicit. See ca.uhn.fhir.jpa.provider.ValueSetOperationProvider
   *
   * @param request the request
   * @param details the details
   * @param valueSet the value set
   * @param url the url
   * @param version the version
   * @param filter the filter
   * @param offset the offset
   * @param count the count
   * @param displayLanguage the display language
   * @return the value set
   * @throws Exception the exception
   */
  @Operation(name = JpaConstants.OPERATION_EXPAND, idempotent = true)
  public ValueSet expandImplicit(final HttpServletRequest request,
    final ServletRequestDetails details,
    // @ResourceParam String rawBody,
    @OperationParam(name = "valueSet", min = 0, max = 1) final ValueSet valueSet,
    @OperationParam(name = "url", min = 0, max = 1, typeName = "uri") final UriType url,
    @OperationParam(name = "valueSetVersion", min = 0, max = 1,
        typeName = "string") final StringType version,
    // @OperationParam(name = "context") String context,
    // @OperationParam(name = "contextDirection") String contextDirection,
    @OperationParam(name = "filter", min = 0, max = 1, typeName = "string") final StringType filter,
    @OperationParam(name = "offset", min = 0, max = 1,
        typeName = "integer") final IntegerType offset,
    @OperationParam(name = "count", min = 0, max = 1, typeName = "integer") final IntegerType count,
    @OperationParam(name = "displayLanguage", min = 0, max = 1,
        typeName = "code") final List<CodeType> displayLanguage)
    throws Exception {

    // Reject post
    if (request.getMethod().equals("POST")) {
      throw FhirUtilityR4.exception("POST method not supported for $expand", IssueType.NOTSUPPORTED,
          HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    try {
      if (url == null || url.isEmpty()) {
        throw FhirUtilityR4.exception("Use the 'url' parameter.",
            OperationOutcome.IssueType.INVARIANT, HttpServletResponse.SC_BAD_REQUEST);
      }
      FhirUtilityR4.notSupported("valueSet", valueSet);

      final ValueSet vs =
          getExpandedValueSet(null, url, version, filter, offset != null ? offset.getValue() : 0,
              count != null ? count.getValue() : 100, false, displayLanguage == null ? null
                  : displayLanguage.stream().map(c -> c.getValue()).collect(Collectors.toSet()));

      if (vs == null) {
        throw FhirUtilityR4.exception("Value set not found = " + url, IssueType.NOTFOUND,
            HttpServletResponse.SC_NOT_FOUND);
      }
      return vs;

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to expand value set",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Expand instance. See ca.uhn.fhir.jpa.provider.ValueSetOperationProvider
   *
   * <pre>
   * https://hl7.org/fhir/R4/valueset-operation-expand.html
   * </pre>
   *
   * @param request the request
   * @param details the details
   * @param id the id
   * @param valueSet the value set
   * @param url the url
   * @param version the version
   * @param filter the filter
   * @param offset the offset
   * @param count the count
   * @param displayLanguage the display language
   * @return the value set
   * @throws Exception the exception
   */
  @Operation(name = JpaConstants.OPERATION_EXPAND, idempotent = true)
  public ValueSet expandInstance(final HttpServletRequest request,
    final ServletRequestDetails details, @IdParam final IdType id, // @ResourceParam
    // String
    // rawBody,
    @OperationParam(name = "valueSet", min = 0, max = 1) final ValueSet valueSet,
    @OperationParam(name = "url", min = 0, max = 1, typeName = "uri") final UriType url,
    @OperationParam(name = "valueSetVersion", min = 0, max = 1,
        typeName = "string") final StringType version,
    // @OperationParam(name = "context") String context,
    // @OperationParam(name = "contextDirection") String contextDirection,
    @OperationParam(name = "filter", min = 0, max = 1, typeName = "string") final StringType filter,
    @OperationParam(name = "offset", min = 0, max = 1,
        typeName = "integer") final IntegerType offset,
    @OperationParam(name = "count", min = 0, max = 1, typeName = "integer") final IntegerType count,
    @OperationParam(name = "displayLanguage", min = 0, max = 1,
        typeName = "code") final Set<CodeType> displayLanguage)
    throws Exception {

    // Reject post
    if (request.getMethod().equals("POST")) {
      throw FhirUtilityR4.exception("POST method not supported for $expand", IssueType.NOTSUPPORTED,
          HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    try {

      final ValueSet vs =
          getExpandedValueSet(id, null, version, filter, offset != null ? offset.getValue() : 0,
              count != null ? count.getValue() : 100, false, displayLanguage == null ? null
                  : displayLanguage.stream().map(c -> c.getValue()).collect(Collectors.toSet()));

      if (vs == null) {
        throw FhirUtilityR4.exception("Value set not found = " + id.getIdPart(), IssueType.NOTFOUND,
            HttpServletResponse.SC_NOT_FOUND);
      }
      return vs;

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to expand value set",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Validate code implicit.
   *
   * <pre>
   * https://hl7.org/fhir/R4/valueset-operation-validate-code.html
   * </pre>
   *
   * @param request the request
   * @param details the details
   * @param url the url
   * @param version the version
   * @param code the code
   * @param system the system
   * @param systemVersion the system version
   * @param display the display
   * @param coding the coding
   * @param codeableConcept the codeable concept
   * @return the parameters
   * @throws Exception the exception
   */
  @Operation(name = JpaConstants.OPERATION_VALIDATE_CODE, idempotent = true, typeName = "ValueSet")
  public Parameters validateCodeImplicit(final HttpServletRequest request,
    final ServletRequestDetails details,
    @OperationParam(name = "url", min = 0, max = 1, typeName = "uri") final UriType url,
    @OperationParam(name = "valueSetVersion", min = 0, max = 1,
        typeName = "string") final StringType version,
    @OperationParam(name = "code", min = 0, max = 1, typeName = "code") final CodeType code,
    @OperationParam(name = "system", min = 0, max = 1, typeName = "uri") final UriType system,
    @OperationParam(name = "systemVersion", min = 0, max = 1,
        typeName = "string") final StringType systemVersion,
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

      if (url == null || url.isEmpty()) {
        throw FhirUtilityR4.exception("Use the 'url' parameter.",
            OperationOutcome.IssueType.INVARIANT, HttpServletResponse.SC_BAD_REQUEST);
      }
      FhirUtilityR4.requireExactlyOneOf("code", code, "coding", coding);
      FhirUtilityR4.notSupported("codeableConcept", codeableConcept);
      final String lcode = FhirUtilityR4.getCode(code, coding);
      return validateCodeHelper(null, url, version, lcode, display);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to validate value set code",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }

  /**
   * Validate code instance.
   *
   * <pre>
   * https://hl7.org/fhir/R4/valueset-operation-validate-code.html
   * </pre>
   *
   * @param request the request
   * @param details the details
   * @param id the id
   * @param uri the uri
   * @param version the version
   * @param code the code
   * @param system the system
   * @param systemVersion the system version
   * @param display the display
   * @param coding the coding
   * @param codeableConcept the codeable concept
   * @return the parameters
   * @throws Exception the exception
   */
  @Operation(name = JpaConstants.OPERATION_VALIDATE_CODE, idempotent = true)
  public Parameters validateCodeInstance(final HttpServletRequest request,
    final ServletRequestDetails details, @IdParam final IdType id,
    @OperationParam(name = "url", min = 0, max = 1, typeName = "uri") final UriType uri,
    @OperationParam(name = "valueSetVersion", min = 0, max = 1,
        typeName = "string") final StringType version,
    @OperationParam(name = "code", min = 0, max = 1, typeName = "code") final CodeType code,
    @OperationParam(name = "system", min = 0, max = 1, typeName = "uri") final UriType system,
    @OperationParam(name = "systemVersion", min = 0, max = 1,
        typeName = "string") final StringType systemVersion,
    @OperationParam(name = "display", min = 0, max = 1,
        typeName = "string") final StringType display,
    @OperationParam(name = "coding", min = 0, max = 1, typeName = "Coding") final Coding coding,
    @OperationParam(name = "codeableConcept", min = 0, max = 1,
        typeName = "CodeableConcept") final CodeableConcept codeableConcept)
    // @OperationParam(name = "displayLanguage") String displayLanguage)
    throws Exception {

    // Reject post
    if (request.getMethod().equals("POST")) {
      throw FhirUtilityR4.exception("POST method not supported for $validate-code",
          IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    try {

      FhirUtilityR4.requireExactlyOneOf("code", code, "coding", coding);
      final String lcode = FhirUtilityR4.getCode(code, coding);
      return validateCodeHelper(id, null, version, lcode, display);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to validate value set code",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Loads a ValueSet from a FHIR R4 ValueSet resource and persists it as a
   * Subset and SubsetMembers. Example usage: POST /ValueSet/$load with a FHIR
   * R4 ValueSet resource in the body.
   *
   * @param valueSet the FHIR R4 ValueSet resource
   * @return Parameters resource with the new Subset code
   * @throws Exception if loading fails
   */
  @Operation(name = "$load", idempotent = true)
  public Parameters loadValueSet(
    @OperationParam(name = "valueSet", min = 1, max = 1) final ValueSet valueSet) throws Exception {
    if (valueSet == null) {
      throw FhirUtilityR4.exception("Missing valueSet parameter", IssueType.INVALID, 400);
    }
    final String subsetId = SubsetLoaderUtil.loadSubset(searchService,
        FhirContext.forR4().newJsonParser().encodeResourceToString(valueSet), false);
    final Parameters out = new Parameters();
    out.addParameter().setName("subsetId").setValue(new StringType(subsetId));
    return out;
  }

  /**
   * Creates a ValueSet.
   *
   * @param valueSet the FHIR R4 ValueSet resource
   * @return MethodOutcome with the new Subset code
   * @throws Exception if creating fails
   */
  @Create
  public MethodOutcome createValueSet(@ResourceParam final ValueSet valueSet) throws Exception {

    try {

      final String subsetId = SubsetLoaderUtil.loadSubset(searchService,
          FhirContext.forR4().newJsonParser().encodeResourceToString(valueSet), true);

      valueSet.getCompose().getInclude().clear();
      valueSet.getCompose().getExclude().clear();

      final MethodOutcome out = new MethodOutcome();
      final IdType id = new IdType("ValueSet", subsetId);
      out.setId(id);
      out.setResource(valueSet);
      out.setCreated(true);

      final OperationOutcome outcome = new OperationOutcome();
      outcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.INFORMATION)
          .setCode(OperationOutcome.IssueType.INFORMATIONAL)
          .setDiagnostics("ValueSet created. Subset ID: " + subsetId);

      out.setOperationOutcome(outcome);

      return out;

    } catch (final Exception e) {
      logger.error("Unexpected error creating value set", e);
      throw FhirUtilityR4.exception("Failed to create value set: " + e.getMessage(),
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Deletes the value set.
   *
   * @param request the request
   * @param details the details
   * @param id the id
   * @return the method outcome
   * @throws Exception the exception
   */
  @Delete
  public MethodOutcome deleteValueSet(final HttpServletRequest request,
    final ServletRequestDetails details, @IdParam final IdType id) throws Exception {

    try {
      if (id == null || id.getIdPart() == null) {
        throw FhirUtilityR4.exception("Value Set ID required for delete", IssueType.INVALID,
            HttpServletResponse.SC_BAD_REQUEST);
      }

      // Check if it's an implicit code system ValueSet (these cannot be
      // deleted)
      final ValueSet implicitSet = getImplicitCodeSystemValueSets().stream()
          .filter(s -> s.getId().equals(id.getIdPart())).findFirst().orElse(null);
      if (implicitSet != null) {
        throw FhirUtilityR4.exception(
            "Cannot delete implicit value set for code system = " + id.getIdPart(),
            IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      }

      // Check if it's a loaded ValueSet (Subset)
      final Subset subset = searchService.get(id.getIdPart(), Subset.class);
      if (subset == null || !"ValueSet".equals(subset.getCategory())) {
        throw FhirUtilityR4.exception("Value set not found = " + id.getIdPart(), IssueType.NOTFOUND,
            HttpServletResponse.SC_NOT_FOUND);
      }

      TerminologyUtility.removeSubset(searchService, subset.getId());
      return new MethodOutcome();

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected error deleting value set", e);
      throw FhirUtilityR4.exception("Failed to delete value set: " + e.getMessage(),
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Gets the implicit code system value sets.
   *
   * @return the implicit code system value sets
   * @throws Exception the exception
   */
  private List<ValueSet> getImplicitCodeSystemValueSets() throws Exception {
    final List<ValueSet> list = new ArrayList<>();

    for (final Terminology terminology : FhirUtility.lookupTerminologies(searchService)) {
      final CodeSystem cs = FhirUtilityR4.toR4(terminology);
      final ValueSet set = FhirUtilityR4.toR4ValueSet(cs);
      list.add(set);
    }

    return list;
  }

  /**
   * Gets the implicit code system value set.
   *
   * @param terminology the terminology
   * @return the implicit code system value set
   * @throws Exception the exception
   */
  private ValueSet getImplicitCodeSystemValueSet(final Terminology terminology) throws Exception {

    return FhirUtilityR4.toR4ValueSet(FhirUtilityR4.toR4(terminology));

  }

  /**
   * Gets the implicit code system value set.
   *
   * @param id the id
   * @param url the url
   * @param version the version
   * @param filter the filter
   * @param offset the offset
   * @param count the count
   * @param activeOnly the active only
   * @param languages the languages
   * @return the implicit code system value set
   * @throws Exception the exception
   */
  private ValueSet getExpandedValueSet(final IdType id, final UriType url, final StringType version,
    final StringType filter, final int offset, final int count, final boolean activeOnly,
    final Set<String> languages) throws Exception {
    // Look up implicit value sets for code systems
    for (final Terminology terminology : FhirUtility.lookupTerminologies(searchService)) {
      final ValueSet vs = getImplicitCodeSystemValueSet(terminology);

      // Skip non-matching
      if ((id != null && !id.getIdPart().equals(vs.getId()))
          || (url != null && !url.getValue().equals(vs.getUrl())
              && !url.getValue().startsWith(vs.getUrl() + "="))) {

        // for SNOMED, check whether the url matches the terminology FHIR
        // version
        if ((url == null) || (!url.getValue().equals(terminology.getAttributes().get("fhirVersion"))
            && !url.getValue().startsWith(terminology.getAttributes().get("fhirVersion") + "="))) {
          continue;
        }
      }

      if (version != null && !version.getValue().equals(vs.getVersion())) {
        logger.info("  SKIP version mismatch = {}", vs.getVersion());
        continue;
      }

      // Perform the expansion
      // "expansion": {
      // "id": "e604febb-3736-4256-8a11-7bc25261f616",
      // "timestamp": "2023-12-13T01:30:44+00:00",
      // "total": 1412,
      // "offset": 0,
      // "parameter": [ {
      // "name": "version",
      // "valueUri":
      // "http://snomed.info/sct|http://snomed.info/sct/900000000000207008/version/20231201"
      // }, {
      // "name": "displayLanguage",
      // "valueString": "en-US,en;q=0.9"
      // } ],
      // "contains": [ {
      // "system": "http://snomed.info/sct",
      // "code": "16224591000119103",
      // "display": "Allergy to honey bee venom",
      // "designations": [...]
      // } ]
      // }
      final Query browserQuery = LuceneQueryBuilder.parse(
          new BrowserQueryBuilder().buildQuery(filter == null ? "*:*" : filter.getValue()),
          Concept.class);
      final Query expression = getExpressionQuery(terminology, vs.getUrl());

      final Query valueSetQuery = expression != null
          ? new BooleanQuery.Builder().add(browserQuery, BooleanClause.Occur.MUST)
              .add(expression, BooleanClause.Occur.MUST).build()
          : browserQuery;

      final int ct = count < 0 ? 0 : (count > 1000 ? 1000 : count);
      final SearchParameters params = new SearchParameters(valueSetQuery, offset, ct, null, null);
      if (activeOnly) {
        params.setActive(activeOnly);
      }
      final ResultList<Concept> list = searchService.find(params, Concept.class, null);
      final ValueSetExpansionComponent expansion = new ValueSetExpansionComponent();
      expansion.setId(UUID.randomUUID().toString());
      expansion.setTimestamp(new Date());
      expansion.setTotal((int) list.getTotal());
      expansion.setOffset(offset);
      if (version != null) {
        expansion.addParameter(new ValueSetExpansionParameterComponent().setName("version")
            .setValue(new StringType(version.getValue())));
      }
      for (final Concept concept : list.getItems()) {
        final ValueSetExpansionContainsComponent code = new ValueSetExpansionContainsComponent()
            .setSystem(terminology.getAttributes().get("fhirUri")).setCode(concept.getCode())
            .setDisplay(concept.getName());
        if (languages != null) {
          // "language": "en",
          // "use": {
          // "system":
          // "http://terminology.hl7.org/CodeSystem/designation-usage",
          // "code": "display"
          // },
          // "value": "Chronic nontraumatic intracranial subdural hematoma"
          for (final Term term : concept.getTerms()) {

            if (!Sets.intersection(languages, term.getLocaleMap().keySet()).isEmpty()) {

              final Map<String, String> displayMap =
                  FhirUtility.getDisplayMap(searchService, terminology);
              final Coding coding = new Coding();
              if (displayMap.containsKey(term.getType())) {
                coding.setCode(term.getType());
                coding.setDisplay(displayMap.get(term.getType()));
              } else {
                coding.setCode(term.getType());
              }
              code.addDesignation(new ConceptReferenceDesignationComponent()
                  .setLanguage(
                      Sets.intersection(languages, term.getLocaleMap().keySet()).iterator().next())
                  .setUse(coding).setValue(term.getName()));
            }
          }
        }
        expansion.addContains(code);
      }
      vs.setExpansion(expansion);
      return vs;
    }
    return null;
  }

  /**
   * Validate code helper.
   *
   * @param id the id
   * @param url the url
   * @param version the version
   * @param code the code
   * @param display the display
   * @return the parameters
   * @throws Exception the exception
   */
  private Parameters validateCodeHelper(final IdType id, final UriType url,
    final StringType version, final String code, final StringType display) throws Exception {
    // Look up implicit value sets for code systems
    for (final Terminology terminology : FhirUtility.lookupTerminologies(searchService)) {
      final ValueSet vs = getImplicitCodeSystemValueSet(terminology);

      // Skip non-matching
      if ((id != null && !id.getIdPart().equals(vs.getId()))
          || (url != null && !url.getValue().equals(vs.getUrl())
              && !url.getValue().startsWith(vs.getUrl() + "="))) {

        // for SNOMED, check whether the url matches the terminology FHIR
        // version
        if (url == null) {
          continue;
        }
        final String versionUrl = terminology.getAttributes().get("fhirVersion") + "?fhir_vs";
        if (!url.getValue().equals(versionUrl) && !url.getValue().startsWith(versionUrl + "=")) {
          continue;
        }
      }

      if (version != null && !version.getValue().equals(vs.getVersion())) {
        logger.info("  SKIP version mismatch = {}", vs.getVersion());
        continue;
      }

      // Perform the lookup - result, message, display
      // "valueString": "The code '16224591000119104' from CodeSystem
      // 'http://snomed.info/sct'
      // was not found in this ValueSet."
      // "valueString": "The code '16224591000119103' was found in the ValueSet,
      // however the
      // display 'abc' did not match any designations."
      final Query codeQuery = LuceneQueryBuilder
          .parse("code:\"" + StringUtility.escapeQuery(code) + "\"", Concept.class);
      final Query expression = getExpressionQuery(terminology, vs.getUrl());
      final Query booleanQuery = getAndQuery(codeQuery, expression);
      final List<Concept> list = searchService.findAll(null, booleanQuery, Concept.class);
      final Parameters parameters = new Parameters();
      // If no match
      if (list.isEmpty()) {
        parameters.addParameter(
            new ParametersParameterComponent().setName("result").setValue(new BooleanType(false)));
        parameters.addParameter(new ParametersParameterComponent().setName("message")
            .setValue(new StringType("The code '" + code + "' was not found in this value set")));
        return parameters;
      }
      parameters.addParameter(
          new ParametersParameterComponent().setName("result").setValue(new BooleanType(true)));
      parameters.addParameter(new ParametersParameterComponent().setName("display")
          .setValue(new StringType(list.get(0).getName())));

      // display doesn't match
      if (display != null && list.get(0).getTerms().stream()
          .filter(t -> display.getValue().equals(t.getName())).count() == 0) {
        parameters.addParameter(new ParametersParameterComponent().setName("message")
            .setValue(new StringType(
                "The code '" + code + "' was found in this value set, however the display '"
                    + display + "' did not match any designations")));
        return parameters;
      }

      return parameters;
    }
    return null;
  }

  /**
   * Gets the expression.
   *
   * @param terminology the terminology
   * @param url the url
   * @return the expression
   * @throws Exception the exception
   */
  private Query getExpressionQuery(final Terminology terminology, final String url)
    throws Exception {
    final String part = url.replaceFirst(".*fhir_vs", "");
    String expression = null;
    if (part.startsWith("=")) {
      if (part.startsWith("=ecl/")) {
        expression = part.replaceFirst("=ecl/", "");
      }
      if (part.startsWith("=isa/")) {
        expression = part.replaceFirst("=isa/", "<");
      }
      if (part.startsWith("=refset/")) {
        expression = part.replaceFirst("=refset/", "^");
      }
    } else {
      return null;
    }
    try {
      return TerminologyUtility.getExpressionQuery(expression);
    } catch (final Exception e) {
      logger.error("Unexpected error", e);
      throw FhirUtilityR4.exception("Unable to parse expression = " + expression,
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
  public Class<ValueSet> getResourceType() {
    return ValueSet.class;
  }
}
