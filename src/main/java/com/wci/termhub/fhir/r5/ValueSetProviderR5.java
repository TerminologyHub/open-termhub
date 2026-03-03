/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r5;

import static com.wci.termhub.util.IndexUtility.getAndQuery;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.IntegerType;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.OperationOutcome.IssueType;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.UriType;
import org.hl7.fhir.r5.model.ValueSet;
import org.hl7.fhir.r5.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r5.model.ValueSet.ConceptReferenceDesignationComponent;
import org.hl7.fhir.r5.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r5.model.ValueSet.ValueSetComposeComponent;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionComponent;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionContainsComponent;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionParameterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.fhir.rest.r5.FhirUtilityR5;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.fhir.util.LoincValueSetHelper;
import com.wci.termhub.handler.BrowserQueryBuilder;
import com.wci.termhub.lucene.LuceneQueryBuilder;
import com.wci.termhub.lucene.eventing.Write;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.SubsetMember;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.TerminologyUtility;
import com.wci.termhub.util.ValueSetLoaderUtil;

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
public class ValueSetProviderR5 implements IResourceProvider {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(ValueSetProviderR5.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The LOINC LL/LG value set helper (Regenstrief mode). */
  @Autowired
  private LoincValueSetHelper loincValueSetHelper;

  /**
   * Gets the value set.
   *
   * @param request the request
   * @param details the details
   * @param id the id
   * @return the value set
   * @throws Exception the exception
   */
  @SuppressWarnings("null")
  @Read(version = true)
  public ValueSet getValueSet(final HttpServletRequest request, final ServletRequestDetails details,
    @IdParam final IdType id) throws Exception {

    try {
      if (id != null && id.hasVersionIdPart() && !"1".equals(id.getVersionIdPart())) {
        throw FhirUtilityR5.exception("Value set " + id.getIdPart()
            + " exists but does not have history version " + id.getVersionIdPart(),
            IssueType.NOTFOUND, HttpServletResponse.SC_NOT_FOUND);
      }
      // 1. LOINC LL/LG first (before any other lookup) so GET ValueSet/LL1162-8
      // always uses this path
      final String idPart = id != null ? id.getIdPart() : null;
      if (idPart != null && loincValueSetHelper.isLllgId(idPart)) {
        if (!loincValueSetHelper.isEnabled()) {
          logger.debug(
              "GET ValueSet/{}: LL/LG path skipped (fhir.loinc.lllg.valuesets.enabled=false)",
              idPart);
        } else {
          final Terminology loinc = loincValueSetHelper.findLoincTerminology(searchService);
          if (loinc != null) {
            final int memberLimit = 10_000;
            final ResultList<Concept> list =
                loincValueSetHelper.findLllgMembers(searchService, loinc, idPart, 0, memberLimit);
            final List<Concept> items = list.getItems();
            if (loincValueSetHelper.isLlId(idPart)) {
              LoincValueSetHelper.sortLlMembersBySequenceNumber(items);
            }
            logger.info("GET ValueSet/{}: returning compose only (no expansion), members={}", idPart,
                items.size());
            return FhirUtilityR5.toR5LllgValueSetWithComposeOnly(loinc, idPart, items);
          }
          logger.debug("GET ValueSet/{}: LL/LG path skipped (LOINC terminology not found)", idPart);
        }
      }
      // 2. Check implicit ValueSets
      final ValueSet vs = findPossibleValueSets(false, id, null, null).stream()
          .filter(s -> s.getId().equals(idPart)).findFirst().orElse(null);
      if (vs != null && vs.getId().endsWith("_entire")) {
        return vs;
      }
      // 3. Check explicit subsets
      final Subset subset = idPart != null ? searchService.get(idPart, Subset.class) : null;
      if (subset != null) {
        // // Fetch members
        // final SearchParameters memberParams = new SearchParameters();
        // memberParams.setLimit(100);
        // memberParams.getFilters().put("subset.code", subset.getCode());
        // final List<SubsetMember> members =
        // searchService.findAll(memberParams, SubsetMember.class).getItems();
        final ValueSet valueSet = FhirUtilityR5.toR5ValueSet(subset, new ArrayList<>(0), false);
        return valueSet;
      }
      throw FhirUtilityR5.exception("Value set not found = " + (idPart == null ? "null" : idPart),
          IssueType.NOTFOUND, HttpServletResponse.SC_NOT_FOUND);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR5.exception("Failed to get value set", OperationOutcome.IssueType.EXCEPTION,
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
   * https://hl7.org/fhir/R5/valueset.html (see Search Parameters)
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

      FhirUtilityR5.notSupportedSearchParams(request);

      // Get possible value sets
      final List<ValueSet> list = findPossibleValueSets(false, id, code, date, description,
          identifier, name, publisher, title, url, version);

      return FhirUtilityR5.makeBundle(request, list, count, offset);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR5.exception("Failed to find value sets",
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
      throw FhirUtilityR5.exception("POST method not supported for $expand", IssueType.NOTSUPPORTED,
          HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    try {
      if (url == null || url.isEmpty()) {
        throw FhirUtilityR5.exception("Use the 'url' parameter.",
            OperationOutcome.IssueType.INVARIANT, HttpServletResponse.SC_BAD_REQUEST);
      }
      FhirUtilityR5.notSupported("valueSet", valueSet);

      final ValueSet vs =
          getExpandedValueSet(null, url, version, filter, offset != null ? offset.getValue() : 0,
              count != null ? count.getValue() : 100, false, displayLanguage == null ? null
                  : displayLanguage.stream().map(c -> c.getValue()).collect(Collectors.toSet()));

      if (vs == null) {
        throw FhirUtilityR5.exception("Value set not found = " + url, IssueType.NOTFOUND,
            HttpServletResponse.SC_NOT_FOUND);
      }
      return vs;

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR5.exception("Failed to expand value set",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Expand instance. See ca.uhn.fhir.jpa.provider.ValueSetOperationProvider
   *
   * <pre>
   * https://hl7.org/fhir/R5/valueset-operation-expand.html
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
    final ServletRequestDetails details, @IdParam final IdType id,
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
      throw FhirUtilityR5.exception("POST method not supported for $expand", IssueType.NOTSUPPORTED,
          HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    try {

      final ValueSet vs =
          getExpandedValueSet(id, null, version, filter, offset != null ? offset.getValue() : 0,
              count != null ? count.getValue() : 100, false, displayLanguage == null ? null
                  : displayLanguage.stream().map(c -> c.getValue()).collect(Collectors.toSet()));

      if (vs == null) {
        throw FhirUtilityR5.exception("Value set not found = " + id.getIdPart(), IssueType.NOTFOUND,
            HttpServletResponse.SC_NOT_FOUND);
      }
      return vs;

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR5.exception("Failed to expand value set",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Validate code implicit.
   *
   * <pre>
   * https://hl7.org/fhir/R5/valueset-operation-validate-code.html
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
      throw FhirUtilityR5.exception("POST method not supported for $validate-code",
          IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    try {

      if (url == null || url.isEmpty()) {
        throw FhirUtilityR5.exception("Use the 'url' parameter.",
            OperationOutcome.IssueType.INVARIANT, HttpServletResponse.SC_BAD_REQUEST);
      }
      FhirUtilityR5.requireExactlyOneOf("code", code, "coding", coding);
      FhirUtilityR5.notSupported("codeableConcept", codeableConcept);
      final String lcode = FhirUtilityR5.getCode(code, coding);
      return validateCodeHelper(null, url, version, lcode, display);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR5.exception("Failed to validate value set code",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }

  /**
   * Validate code instance.
   *
   * <pre>
   * https://hl7.org/fhir/R5/valueset-operation-validate-code.html
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
      throw FhirUtilityR5.exception("POST method not supported for $validate-code",
          IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    try {

      FhirUtilityR5.requireExactlyOneOf("code", code, "coding", coding);
      final String lcode = FhirUtilityR5.getCode(code, coding);
      return validateCodeHelper(id, null, version, lcode, display);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR5.exception("Failed to validate value set code",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  // /**
  // * Loads a ValueSet from a FHIR R5 ValueSet resource and persists it as a Subset and
  // * SubsetMembers. Example usage: POST /ValueSet/$load with a FHIR R5 ValueSet resource in the
  // * body.
  // *
  // * @param valueSet the FHIR R5 ValueSet resource
  // * @return Parameters resource with the new Subset code
  // * @throws Exception if loading fails
  // */
  // @Operation(name = "$load", idempotent = true)
  // public Parameters loadValueSet(
  // @OperationParam(name = "valueSet", min = 1, max = 1) final ValueSet valueSet) throws Exception
  // {
  // if (valueSet == null) {
  // throw FhirUtilityR5.exception("Missing valueSet parameter", IssueType.INVALID, 400);
  // }
  // final String subsetId = ValueSetLoaderUtil.loadSubset(searchService,
  // context.newJsonParser().encodeResourceToString(valueSet), false);
  // final Parameters out = new Parameters();
  // out.addParameter().setName("subsetId").setValue(new StringType(subsetId));
  // return out;
  // }

  /**
   * Creates a ValueSet.
   *
   * @param bytes the bytes
   * @return the method outcome (as required by HAPI)
   * @throws Exception if creating fails
   */
  @Create
  @Write
  public MethodOutcome createValueSet(@ResourceParam final byte[] bytes) throws Exception {

    try {
      logger.info("Create value set R5");

      // Write to a file so we can re-open streams against it
      final File file = File.createTempFile("tmp", ".json");
      FileUtils.writeByteArrayToFile(file, bytes);

      final ValueSet valueSet = ValueSetLoaderUtil.loadValueSet(searchService, file, ValueSet.class,
          new DefaultProgressListener());

      FileUtils.delete(file);

      valueSet.getCompose().getInclude().clear();
      valueSet.getCompose().getExclude().clear();

      final MethodOutcome out = new MethodOutcome();
      final IdType id = new IdType("ValueSet", valueSet.getId());
      out.setId(id);
      out.setResource(valueSet);
      out.setCreated(true);

      final OperationOutcome outcome = new OperationOutcome();
      outcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.INFORMATION)
          .setCode(OperationOutcome.IssueType.INFORMATIONAL)
          .setDiagnostics("ValueSet created = " + valueSet.getId());
      out.setOperationOutcome(outcome);

      return out;

    } catch (FHIRServerResponseException fe) {
      throw fe;
    } catch (final Exception e) {
      logger.error("Unexpected error creating value set", e);
      throw FhirUtilityR5.exception(e.getMessage(), OperationOutcome.IssueType.EXCEPTION,
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Deletes the value set.
   *
   * @param request the request
   * @param details the details
   * @param id the id
   * @throws Exception the exception
   */
  @Delete
  @Write
  public void deleteValueSet(final HttpServletRequest request, final ServletRequestDetails details,
    @IdParam final IdType id) throws Exception {

    try {
      if (id == null || id.getIdPart() == null) {
        throw FhirUtilityR5.exception("Value Set ID required for delete", IssueType.INVALID,
            HttpServletResponse.SC_BAD_REQUEST);
      }
      logger.info("Delete value set with ID: {}", id.getIdPart());

      // Check if it's an implicit code system ValueSet (these cannot be
      // deleted)
      final ValueSet valueSet =
          findPossibleValueSets(false, id, null, null).stream().findFirst().orElse(null);
      if (valueSet != null && valueSet.getId().endsWith("_entire")) {
        throw FhirUtilityR5.exception(
            "Cannot delete implicit value set for code system = " + id.getIdPart(),
            IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      }
      if (valueSet != null && valueSet.getMeta() != null && valueSet.getMeta().getTag().stream()
          .anyMatch(t -> FhirUtilityR5.META_LOINC_LLLG_ID.equals(t.getSystem()))) {
        throw FhirUtilityR5.exception("Cannot delete LOINC LL/LG value set = " + id.getIdPart(),
            IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      }

      // Check if it's a loaded ValueSet (Subset)
      final Subset subset = searchService.get(id.getIdPart(), Subset.class);
      if (subset == null) {
        throw FhirUtilityR5.exception("Value set not found for id = " + id.getIdPart(),
            IssueType.NOTFOUND, HttpServletResponse.SC_NOT_FOUND);
      }

      // Allow deletion of any subset that is not an implicit value set
      // (implicit value sets end with "_entire")
      if (subset.getId().endsWith("_entire")) {
        throw FhirUtilityR5.exception(
            "Cannot delete implicit value set for code system = " + id.getIdPart(),
            IssueType.NOTSUPPORTED, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      }

      TerminologyUtility.removeSubset(searchService, subset.getId());

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected error deleting value set", e);
      throw FhirUtilityR5.exception("Failed to delete value set: " + e.getMessage(),
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
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
    final List<ValueSet> valueSets = findPossibleValueSets(true, id, url, version);

    // Expect a single value set
    if (valueSets.isEmpty()) {
      throw FhirUtilityR5.exception("Failed to find matching value set",
          OperationOutcome.IssueType.NOTFOUND, HttpServletResponse.SC_NOT_FOUND);
    }
    final ValueSet vs;
    if (valueSets.size() > 1) {
      // If no explicit ValueSet version is requested, choose the most recent
      // terminology
      // version instead of failing.
      if (version == null || version.isEmpty()) {
        final ValueSet latestVs = valueSets.stream().filter(v -> v.getDate() != null)
            .max(Comparator.comparing(ValueSet::getDate)).orElse(valueSets.get(0));
        vs = latestVs;
      } else {
        throw FhirUtilityR5.exception("Too many matching value sets found",
            OperationOutcome.IssueType.MULTIPLEMATCHES, HttpServletResponse.SC_EXPECTATION_FAILED);
      }
    } else {
      vs = valueSets.get(0);
    }

    // LOINC LL/LG expansion (Regenstrief mode)
    if (loincValueSetHelper.isEnabled() && vs.getMeta() != null && vs.getMeta().getTag().stream()
        .anyMatch(t -> FhirUtilityR5.META_LOINC_LLLG_ID.equals(t.getSystem()))) {
      final String lllgId = vs.getMeta().getTag().stream()
          .filter(t -> FhirUtilityR5.META_LOINC_LLLG_ID.equals(t.getSystem())).findFirst().get()
          .getCode();
      final String fromTerminology = vs.getMeta().getTag().stream()
          .filter(t -> "fromTerminology".equals(t.getSystem())).findFirst().get().getCode();
      final String fromPublisher = vs.getMeta().getTag().stream()
          .filter(t -> "fromPublisher".equals(t.getSystem())).findFirst().get().getCode();
      final String fromVersion = vs.getMeta().getTag().stream()
          .filter(t -> "fromVersion".equals(t.getSystem())).findFirst().get().getCode();
      final Terminology terminology = TerminologyUtility.getTerminology(searchService,
          fromTerminology, fromPublisher, fromVersion);
      final int ct = count < 0 ? 0 : (count > 2000 ? 2000 : count);
      final ResultList<Concept> list =
          loincValueSetHelper.findLllgMembers(searchService, terminology, lllgId, offset, ct);
      final List<Concept> items = list.getItems();
      if (loincValueSetHelper.isLlId(lllgId)) {
        LoincValueSetHelper.sortLlMembersBySequenceNumber(items);
      }
      final String systemUri = terminology.getUri();
      if (systemUri != null) {
        final ValueSetComposeComponent compose = new ValueSetComposeComponent();
        final ConceptSetComponent include = new ConceptSetComponent();
        include.setSystem(systemUri);
        for (final Concept c : items) {
          include.addConcept(
              new ConceptReferenceComponent().setCode(c.getCode()).setDisplay(c.getName()));
        }
        compose.addInclude(include);
        vs.setCompose(compose);
      }
      final ValueSetExpansionComponent expansion = new ValueSetExpansionComponent();
      expansion.setId(UUID.randomUUID().toString());
      expansion.setTimestamp(new Date());
      expansion.setTotal((int) list.getTotal());
      expansion.setOffset(offset);
      expansion.addParameter(new ValueSetExpansionParameterComponent().setName("offset")
          .setValue(new IntegerType(offset)));
      expansion.addParameter(
          new ValueSetExpansionParameterComponent().setName("count").setValue(new IntegerType(ct)));
      if (version != null) {
        expansion.addParameter(new ValueSetExpansionParameterComponent().setName("version")
            .setValue(new StringType(version.getValue())));
      }
      for (final Concept concept : items) {
        final ValueSetExpansionContainsComponent code =
            new ValueSetExpansionContainsComponent().setSystem(terminology.getUri())
                .setCode(concept.getCode()).setDisplay(concept.getName());
        if (languages != null) {
          for (final Term term : concept.getTerms()) {
            if (!Sets.intersection(languages, term.getLocaleMap().keySet()).isEmpty()) {
              final Map<String, String> displayMap = FhirUtility.getDisplayMap(searchService,
                  concept.getTerminology(), concept.getPublisher(), concept.getVersion());
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
      vs.setMeta(null);
      return vs;
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

    // If terminology-based, set a terminology query (only things we create have
    // ids ending in
    // "_entire"
    final boolean terminologyFlag = vs.getId().endsWith("_entire");
    final String fromTerminology = vs.getMeta().getTag().stream()
        .filter(c -> c.getSystem().equals("fromTerminology")).findFirst().get().getCode();
    final String fromPublisher = vs.getMeta().getTag().stream()
        .filter(c -> c.getSystem().equals("fromPublisher")).findFirst().get().getCode();
    final String fromVersion = vs.getMeta().getTag().stream()
        .filter(c -> c.getSystem().equals("fromVersion")).findFirst().get().getCode();
    final Terminology terminology = TerminologyUtility.getTerminology(searchService,
        fromTerminology, fromPublisher, fromVersion);

    // Use meta flags to get the terminology of the concepts
    final Query terminologyQuery = LuceneQueryBuilder.parse(
        TerminologyUtility.getTerminologyQuery(fromTerminology, fromPublisher, fromVersion),
        Concept.class);

    final Query subsetQuery;
    if (!terminologyFlag) {
      final List<String> memberClauses =
          searchService
              .find(new SearchParameters(TerminologyUtility.getTerminologyQuery(fromTerminology,
                  fromPublisher, fromVersion), 0, 1000000, null, null), SubsetMember.class)
              .getItems().stream().map(s -> "code:" + StringUtility.escapeQuery(s.getCode()))
              .toList();
      // if (memberClauses.isEmpty()) {
      // final ValueSetExpansionComponent expansion = new
      // ValueSetExpansionComponent();
      // expansion.setId(UUID.randomUUID().toString());
      // expansion.setTimestamp(new Date());
      // expansion.setTotal(0);
      // expansion.setOffset(offset);
      // expansion.addParameter(new ValueSetExpansionParameterComponent()
      // .setName("count")
      // .setValue(new IntegerType(count < 0 ? 0 : (count > 1000 ? 1000 :
      // count))));
      // if (version != null) {
      // expansion.addParameter(new ValueSetExpansionParameterComponent()
      // .setName("version")
      // .setValue(new StringType(version.getValue())));
      // }
      // vs.setExpansion(expansion);
      // vs.setMeta(null);
      // return vs;
      // }
      if (memberClauses.size() > LuceneQueryBuilder.MAX_CLAUSE_COUNT) {
        final BooleanQuery.Builder subsetQueryBuilder = new BooleanQuery.Builder();
        for (int i = 0; i < memberClauses.size(); i += LuceneQueryBuilder.MAX_CLAUSE_COUNT) {
          final int end = Math.min(i + LuceneQueryBuilder.MAX_CLAUSE_COUNT, memberClauses.size());
          final List<String> chunk = memberClauses.subList(i, end);
          final String memberQuery = StringUtility.composeQuery("OR", chunk);
          final Query chunkQuery = LuceneQueryBuilder.parse(memberQuery, Concept.class);
          subsetQueryBuilder.add(new ConstantScoreQuery(chunkQuery), BooleanClause.Occur.SHOULD);
        }
        subsetQuery = subsetQueryBuilder.build();
      } else {
        final String memberQuery = StringUtility.composeQuery("OR", memberClauses);
        subsetQuery = LuceneQueryBuilder.parse(memberQuery, Concept.class);
      }
    } else {
      subsetQuery = null;
    }
    final Query filterQuery = LuceneQueryBuilder.parse(
        new BrowserQueryBuilder().buildQuery(filter == null ? null : filter.getValue()),
        Concept.class);
    final Query expressionQuery = getExpressionQuery(url == null ? null : url.getValue());
    final Query booleanQuery =
        getAndQuery(terminologyQuery, subsetQuery, filterQuery, expressionQuery);
    final int ct = count < 0 ? 0 : (count > 2000 ? 2000 : count);
    final SearchParameters params = new SearchParameters(booleanQuery, offset, ct, null, null);
    if (activeOnly) {
      params.setActive(activeOnly);
    }
    final ResultList<Concept> list = searchService.find(params, Concept.class);
    final ValueSetExpansionComponent expansion = new ValueSetExpansionComponent();
    expansion.setId(UUID.randomUUID().toString());
    expansion.setTimestamp(new Date());
    expansion.setTotal((int) list.getTotal());
    expansion.setOffset(offset);
    // set count
    expansion.addParameter(
        new ValueSetExpansionParameterComponent().setName("count").setValue(new IntegerType(ct)));
    if (version != null) {
      expansion.addParameter(new ValueSetExpansionParameterComponent().setName("version")
          .setValue(new StringType(version.getValue())));
    }
    for (final Concept concept : list.getItems()) {
      final ValueSetExpansionContainsComponent code = new ValueSetExpansionContainsComponent()
          .setSystem(terminology.getUri()).setCode(concept.getCode()).setDisplay(concept.getName());
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

            final Map<String, String> displayMap = FhirUtility.getDisplayMap(searchService,
                concept.getTerminology(), concept.getPublisher(), concept.getVersion());
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

    // Clear meta
    vs.setMeta(null);

    return vs;
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
      final ValueSet vs = FhirUtilityR5.toR5ValueSet(terminology, false);

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
        if (logger.isDebugEnabled()) {
          logger.debug("  SKIP version mismatch = {}", vs.getVersion());
        }
        continue;
      }

      // Perform the lookup - result, message, display
      // "valueString": "The code '16224591000119104' from CodeSystem
      // 'http://snomed.info/sct'
      // was not found in this ValueSet."
      // "valueString": "The code '16224591000119103' was found in the ValueSet,
      // however the
      // display 'abc' did not match any designations."
      final Query codeQuery =
          LuceneQueryBuilder.parse("code:" + StringUtility.escapeQuery(code), Concept.class);
      final Query expression = getExpressionQuery(vs.getUrl());
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

    // LOINC LL/LG validate-code (Regenstrief mode)
    if (loincValueSetHelper.isEnabled()) {
      String lllgId = null;
      if (url != null && loincValueSetHelper.isLllgValueSetUrl(url.getValue())) {
        lllgId = loincValueSetHelper.parseIdFromUrl(url.getValue());
      }
      if (lllgId == null && id != null && loincValueSetHelper.isLllgId(id.getIdPart())) {
        lllgId = id.getIdPart();
      }
      if (lllgId != null) {
        final Terminology loinc = loincValueSetHelper.findLoincTerminology(searchService);
        if (loinc != null) {
          final Concept member =
              loincValueSetHelper.findMemberByCode(searchService, loinc, lllgId, code);
          final Parameters parameters = new Parameters();
          if (member == null) {
            parameters.addParameter(new ParametersParameterComponent().setName("result")
                .setValue(new BooleanType(false)));
            parameters.addParameter(new ParametersParameterComponent().setName("message").setValue(
                new StringType("The code '" + code + "' was not found in this value set")));
            return parameters;
          }
          parameters.addParameter(
              new ParametersParameterComponent().setName("result").setValue(new BooleanType(true)));
          parameters.addParameter(new ParametersParameterComponent().setName("display")
              .setValue(new StringType(member.getName())));
          if (display != null && member.getTerms().stream()
              .filter(t -> display.getValue().equals(t.getName())).count() == 0) {
            parameters.addParameter(new ParametersParameterComponent().setName("message")
                .setValue(new StringType(
                    "The code '" + code + "' was found in this value set, however the display '"
                        + display + "' did not match any designations")));
          }
          return parameters;
        }
      }
    }
    return null;
  }

  /**
   * Gets the expression.
   *
   * @param url the url
   * @return the expression
   * @throws Exception the exception
   */
  private Query getExpressionQuery(final String url) throws Exception {
    if (url == null) {
      return null;
    }
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
      throw FhirUtilityR5.exception("Unable to parse expression = " + expression,
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }

  /**
   * Gets the base url.
   *
   * @param url the url
   * @return the base url
   */
  private String getBaseUrl(final String url) {
    if (url == null) {
      return null;
    }
    return url.replaceFirst("=(ecl|isa|refset)/.+", "");
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

  /**
   * Find possible value sets.
   *
   * @param metaFlag the meta flag
   * @param id the id
   * @param url the url
   * @param version the version
   * @return the list
   * @throws Exception the exception
   */
  public List<ValueSet> findPossibleValueSets(final boolean metaFlag, final IdType id,
    final UriType url, final StringType version) throws Exception {
    final TokenParam idParam = id == null ? null : new TokenParam(id.getIdPart());
    final UriParam urlParam = url == null ? null : new UriParam(url.getValue());
    final StringParam versionParam = version == null ? null : new StringParam(version.getValue());
    return findPossibleValueSets(metaFlag, idParam, null, null, null, null, null, null, null,
        urlParam, versionParam);
  }

  /**
   * Find possible value sets.
   *
   * @param metaFlag the meta flag
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
   * @return the list
   * @throws Exception the exception
   */
  public List<ValueSet> findPossibleValueSets(final boolean metaFlag, final TokenParam id,
    final TokenParam code, final DateRangeParam date, final StringParam description,
    final TokenParam identifier, final StringParam name, final StringParam publisher,
    final StringParam title, final UriParam url, final StringParam version) throws Exception {

    final List<ValueSet> list = new ArrayList<>();
    // For now (until we have real value sets)
    // Look up implicit value sets for code systems
    for (final Terminology terminology : FhirUtility.lookupTerminologies(searchService)) {
      final ValueSet vs = FhirUtilityR5.toR5ValueSet(terminology, metaFlag);

      // Skip non-matching
      if ((id != null && !id.getValue().equals(vs.getId()))
          || (url != null && !getBaseUrl(url.getValue()).equals(vs.getUrl()))) {
        continue;
      }

      if (date != null && !FhirUtility.compareDate(date, vs.getDate())) {
        if (logger.isDebugEnabled()) {
          logger.debug("  SKIP date mismatch = {}", vs.getDate());
        }
        continue;
      }
      if (description != null && !FhirUtility.compareString(description, vs.getDescription())) {
        if (logger.isDebugEnabled()) {
          logger.debug("  SKIP description mismatch = {}", vs.getDescription());
        }
        continue;
      }
      if (name != null && !FhirUtility.compareString(name, vs.getName())) {
        if (logger.isDebugEnabled()) {
          logger.debug("  SKIP name mismatch = {}", vs.getName());
        }
        continue;
      }
      if (publisher != null && !FhirUtility.compareString(publisher, vs.getPublisher())) {
        if (logger.isDebugEnabled()) {
          logger.debug("  SKIP publisher mismatch = {}", vs.getPublisher());
        }
        continue;
      }
      if (title != null && !FhirUtility.compareString(title, vs.getTitle())) {
        if (logger.isDebugEnabled()) {
          logger.debug("  SKIP title mismatch = {}", vs.getTitle());
        }
        continue;
      }
      if (version != null && !FhirUtility.compareString(version, vs.getVersion())) {
        if (logger.isDebugEnabled()) {
          logger.debug("  SKIP version mismatch = {}", vs.getVersion());
        }
        continue;
      }

      if (code != null
          && TerminologyUtility.getConcept(searchService, terminology, code.getValue()) == null) {
        if (logger.isDebugEnabled()) {
          logger.debug("  SKIP code mismatch = {}",
              terminology.getAbbreviation() + " " + code.getValue());
        }
        continue;
      }

      list.add(vs);
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
      memberParams.setLimit(1000000);
      memberParams.setOffset(0);
      // final List<SubsetMember> members =
      // searchService.findAll(memberParams, SubsetMember.class).getItems();
      final ValueSet set =
          FhirUtilityR5.toR5ValueSet(subset, new ArrayList<SubsetMember>(0), metaFlag);
      // Apply the same filtering as above
      if ((id != null && !id.getValue().equals(set.getId()))
          || (url != null && !url.getValue().equals(set.getUrl()))) {
        continue;
      }
      if (date != null && !FhirUtility.compareDate(date, set.getDate())) {
        if (logger.isDebugEnabled()) {
          logger.debug("  SKIP date mismatch = {}", set.getDate());
        }
        continue;
      }
      if (description != null && !FhirUtility.compareString(description, set.getDescription())) {
        if (logger.isDebugEnabled()) {
          logger.debug("  SKIP description mismatch = {}", set.getDescription());
        }
        continue;
      }
      if (name != null && !FhirUtility.compareString(name, set.getName())) {
        if (logger.isDebugEnabled()) {
          logger.debug("  SKIP name mismatch = {}", set.getName());
        }
        continue;
      }
      if (publisher != null && !FhirUtility.compareString(publisher, set.getPublisher())) {
        if (logger.isDebugEnabled()) {
          logger.debug("  SKIP publisher mismatch = {}", set.getPublisher());
        }
        continue;
      }
      if (title != null && !FhirUtility.compareString(title, set.getTitle())) {
        if (logger.isDebugEnabled()) {
          logger.debug("  SKIP title mismatch = {}", set.getTitle());
        }
        continue;
      }
      if (version != null && !FhirUtility.compareString(version, set.getVersion())) {
        if (logger.isDebugEnabled()) {
          logger.debug("  SKIP version mismatch = {}", set.getVersion());
        }
        continue;
      }
      // No code filter for loaded sets
      list.add(set);
    }

    // --- LOINC LL/LG value sets (Regenstrief mode) ---
    // Run when enabled or when search params indicate an LL/LG request (so ?code=LG51018-6-2.81
    // returns the value set even if FHIR_LOINC_LLLG_VALUESETS_ENABLED is not set).
    final boolean lllgRequested = (url != null && loincValueSetHelper.isLllgValueSetUrl(url.getValue()))
        || (id != null && id.getValue() != null && loincValueSetHelper.isLllgId(id.getValue()))
        || (code != null && code.getValue() != null && loincValueSetHelper.isLllgId(code.getValue()));
    if (loincValueSetHelper.isEnabled() || lllgRequested) {
      final Terminology loinc = loincValueSetHelper.findLoincTerminology(searchService);
      if (loinc != null) {
        String lllgId = null;
        if (url != null && loincValueSetHelper.isLllgValueSetUrl(url.getValue())) {
          lllgId = loincValueSetHelper.parseIdFromUrl(url.getValue());
        }
        if (lllgId == null && id != null && loincValueSetHelper.isLllgId(id.getValue())) {
          lllgId = id.getValue();
        }
        if (lllgId == null && code != null && code.getValue() != null
            && loincValueSetHelper.isLllgId(code.getValue())) {
          lllgId = code.getValue();
        }
        if (lllgId != null) {
          final ValueSet lllgVs = FhirUtilityR5.toR5LllgValueSet(loinc, lllgId, metaFlag);
          final boolean idUrlMatch = (id == null || id.getValue().equals(lllgVs.getId()))
              && (url == null || url.getValue().equals(lllgVs.getUrl()));
          final boolean dateMatch = date == null || FhirUtility.compareDate(date, lllgVs.getDate());
          final boolean versionMatch =
              version == null || FhirUtility.compareString(version, lllgVs.getVersion());
          final boolean nameMatch =
              name == null || FhirUtility.compareString(name, lllgVs.getName());
          final boolean publisherMatch =
              publisher == null || FhirUtility.compareString(publisher, lllgVs.getPublisher());
          final boolean titleMatch =
              title == null || FhirUtility.compareString(title, lllgVs.getTitle());
          final boolean descriptionMatch =
              description == null
                  || FhirUtility.compareString(description, lllgVs.getDescription());
          if (idUrlMatch && dateMatch && versionMatch && nameMatch && publisherMatch
              && titleMatch && descriptionMatch) {
            list.add(lllgVs);
          }
        }
      }
    }

    return list;

  }

}
