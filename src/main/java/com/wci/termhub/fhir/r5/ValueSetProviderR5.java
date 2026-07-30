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
import com.wci.termhub.algo.MarkLatestRunner;
import com.wci.termhub.fhir.rest.r5.FhirUtilityR5;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.fhir.util.LoincValueSetHelper;
import com.wci.termhub.fhir.util.ValueSetExpandCache;
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

  /** The mark latest runner. */
  @Autowired
  private MarkLatestRunner markLatestRunner;

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
              "GET ValueSet/{}: LL/LG path skipped (server.mode is not regenstrief)",
              idPart);
        } else {
          final Terminology loinc = loincValueSetHelper.findLoincTerminology(searchService);
          if (loinc != null) {
            final int memberLimit = 100_000;
            final ResultList<Concept> list =
                loincValueSetHelper.findLllgMembers(searchService, loinc, idPart, 0, memberLimit);
            final List<Concept> items = new ArrayList<>(list.getItems());
            loincValueSetHelper.sortDirectLllgMembers(idPart, items);
            final LoincValueSetHelper.LllgComposeStructure composeStructure =
                loincValueSetHelper.buildLllgComposeStructure(items);
            logger.info("GET ValueSet/{}: returning compose only (no expansion), members={}",
                idPart, items.size());
            final Concept lllgConcept =
                loincValueSetHelper.findLllgConcept(searchService, loinc, idPart);
            final String valueSetId = lllgConcept != null ? lllgConcept.getId() : null;
            return FhirUtilityR5.toR5LllgValueSetWithComposeOnly(loinc, idPart, valueSetId,
                composeStructure);
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
      // 2.5 LOINC LL/LG by Concept UUID
      if (idPart != null && loincValueSetHelper.isEnabled()) {
        final Concept concept = searchService.get(idPart, Concept.class);
        if (concept != null && concept.getCode() != null
            && loincValueSetHelper.isLllgId(concept.getCode())) {
          Terminology loincTerm = TerminologyUtility.getTerminology(searchService,
              concept.getTerminology(), concept.getPublisher(), concept.getVersion());
          if (loincTerm == null) {
            loincTerm = loincValueSetHelper.findLoincTerminology(searchService);
          }
          if (loincTerm != null) {
            final int memberLimit = 10_000;
            final ResultList<Concept> list = loincValueSetHelper.findLllgMembers(searchService,
                loincTerm, concept.getCode(), 0, memberLimit);
            final List<Concept> items = new ArrayList<>(list.getItems());
            loincValueSetHelper.sortDirectLllgMembers(concept.getCode(), items);
            final LoincValueSetHelper.LllgComposeStructure composeStructure =
                loincValueSetHelper.buildLllgComposeStructure(items);
            logger.info("GET ValueSet/{}: returning LL/LG compose by concept UUID, members={}",
                idPart, items.size());
            return FhirUtilityR5.toR5LllgValueSetWithComposeOnly(loincTerm, concept.getCode(),
                concept.getId(), composeStructure);
          }
        }
      }
      // 3. Check explicit subsets
      final Subset subset = idPart != null ? searchService.get(idPart, Subset.class) : null;
      if (subset != null) {
        final List<SubsetMember> members = findSubsetMembersForSubset(subset);
        return FhirUtilityR5.toR5ValueSet(subset, members, false, searchService);
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
    @Description(shortDefinition = "Number of entries to return")
    @OptionalParam(name = "_count") final NumberParam count,
    @Description(shortDefinition = "Start offset, used when reading a next page")
    @OptionalParam(name = "_offset") final NumberParam offset) throws Exception {

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
          getExpandedValueSet(null, url, version, filter,
              offset != null ? offset.getValue() : 0, count != null ? count.getValue() : 100, false,
              displayLanguage == null ? null
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
          getExpandedValueSet(id, null, version, filter,
              offset != null ? offset.getValue() : 0, count != null ? count.getValue() : 100, false,
              displayLanguage == null ? null
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
          new DefaultProgressListener(), false, markLatestRunner);

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

      FhirUtility.clearCaches();
      ValueSetExpandCache.clear();
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
      if (valueSet != null && loincValueSetHelper.isEnabled()
          && (loincValueSetHelper.isLllgValueSetUrl(valueSet.getUrl())
              || loincValueSetHelper.isLllgId(valueSet.getId()))) {
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
      FhirUtility.clearCaches();
      ValueSetExpandCache.clear();

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
  private ValueSet getExpandedValueSet(final IdType id,
    final UriType url, final StringType version, final StringType filter, final int offset,
    final int count, final boolean activeOnly, final Set<String> languages) throws Exception {
    final String cacheKey = ValueSetExpandCache.buildKey("R5", id == null ? null : id.getIdPart(),
        url == null ? null : url.getValue(), version == null ? null : version.getValue(), offset,
        count, filter == null ? null : filter.getValue(), activeOnly, languages);
    final ValueSet cached = ValueSetExpandCache.getR5(cacheKey);
    if (cached != null) {
      return cached;
    }

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
    if (loincValueSetHelper.isEnabled()) {
      String lllgId = loincValueSetHelper.parseIdFromUrl(vs.getUrl());
      if (lllgId == null && loincValueSetHelper.isLllgId(vs.getId())) {
        lllgId = vs.getId();
      }
      if (lllgId != null && loincValueSetHelper.isLllgId(lllgId)) {
        final Terminology terminology = resolveLllgTerminology(vs);
        if (terminology == null) {
          throw FhirUtilityR5.exception("Failed to find LOINC terminology for value set",
              OperationOutcome.IssueType.NOTFOUND, HttpServletResponse.SC_NOT_FOUND);
        }
        final int ct = count < 0 ? 0 : (count > 2000 ? 2000 : count);
        final ResultList<Concept> directList = loincValueSetHelper.findLllgMembers(searchService,
            terminology, lllgId, 0, 100_000);
        final List<Concept> directItems = new ArrayList<>(directList.getItems());
        loincValueSetHelper.sortDirectLllgMembers(lllgId, directItems);
        final LoincValueSetHelper.LllgComposeStructure composeStructure =
            loincValueSetHelper.buildLllgComposeStructure(directItems);
        final String filterValue = filter != null ? filter.getValue() : null;
        final LoincValueSetHelper.ExpandedLllgResult expanded = loincValueSetHelper.expandLllgLeaves(
            searchService, terminology, lllgId, offset, ct, filterValue);
        final List<Concept> items = expanded.getItems();
        final String systemUri = terminology.getUri();
        if (systemUri != null) {
          FhirUtilityR5.setR5LllgCompose(vs, systemUri, composeStructure);
        }
        final ValueSetExpansionComponent expansion = new ValueSetExpansionComponent();
        expansion.setIdentifier(UUID.randomUUID().toString());
        expansion.setTimestamp(new Date());
        expansion.setTotal(expanded.getTotal());
        expansion.setOffset(offset);
        expansion.addParameter(new ValueSetExpansionParameterComponent().setName("offset")
            .setValue(new IntegerType(offset)));
        if (filter != null) {
          expansion.addParameter(
              new ValueSetExpansionParameterComponent().setName("filter").setValue(filter));
        }
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
            final boolean isLoinc =
                terminology.getUri() != null && terminology.getUri().contains("loinc.org");
            for (final Term term : concept.getTerms()) {
              if (!Sets.intersection(languages, term.getLocaleMap().keySet()).isEmpty()) {
                final Map<String, String> displayMap = FhirUtility.getDisplayMap(searchService,
                    concept.getTerminology(), concept.getPublisher(), concept.getVersion());
                final Coding coding = new Coding();
                coding.setCode(term.getType());
                if (isLoinc) {
                  coding.setDisplay(term.getType());
                } else {
                  final String useDisplay = term.getAttributes().get("designationUseDisplay");
                  if (useDisplay != null) {
                    coding.setDisplay(useDisplay);
                  } else if (displayMap.containsKey(term.getType())) {
                    coding.setDisplay(displayMap.get(term.getType()));
                  }
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
        if (loincValueSetHelper.isLgId(lllgId)) {
          vs.setExperimental(true);
        }
        vs.setMeta(null);
        ValueSetExpandCache.putR5(cacheKey, vs);
        return vs;
      }
    }

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
    Terminology terminology = TerminologyUtility.getTerminology(searchService,
        fromTerminology, fromPublisher, fromVersion);

    final int ct = count < 0 ? 0 : (count > 2000 ? 2000 : count);

    // Loaded ValueSet (Subset): expand from SubsetMembers; keep full compose.
    // Concept path is used only for displayLanguage / ECL.
    if (!terminologyFlag) {
      final Subset loadedSubset = searchService.get(vs.getId(), Subset.class);
      if (loadedSubset == null) {
        final ValueSet empty = createEmptyValueSetExpansion(vs, offset, ct, filter, version);
        ValueSetExpandCache.putR5(cacheKey, empty);
        return empty;
      }
      final List<SubsetMember> members = findSubsetMembersForSubset(loadedSubset);
      if (members.isEmpty()) {
        final ValueSet empty = createEmptyValueSetExpansion(vs, offset, ct, filter, version);
        ValueSetExpandCache.putR5(cacheKey, empty);
        return empty;
      }
      final SubsetMember m0 = members.get(0);
      final Terminology terminologyFromMember = TerminologyUtility.getTerminology(searchService,
          m0.getTerminology(), m0.getPublisher(), m0.getVersion());
      if (terminologyFromMember != null) {
        terminology = terminologyFromMember;
      }
      final String systemUri = resolveLoadedSubsetSystemUri(loadedSubset, terminology);

      setComposeFromLoadedSubset(vs, loadedSubset, members, systemUri);

      // Designations need Concept.terms — keep legacy Concept expand only then.
      final Query expressionQuery = getExpressionQuery(url == null ? null : url.getValue());
      if (languages == null && expressionQuery == null) {
        final ValueSet expanded = expandLoadedSubsetFromMembers(vs, members, systemUri, offset, ct,
            filter, version);
        ValueSetExpandCache.putR5(cacheKey, expanded);
        return expanded;
      }

      // Fallback: Concept path for displayLanguage / ECL (compose already set)
      final Query terminologyQuery = LuceneQueryBuilder.parse(
          TerminologyUtility.getTerminologyQuery(m0.getTerminology(), m0.getPublisher(),
              m0.getVersion()),
          Concept.class);
      final List<String> memberClauses = members.stream()
          .map(s -> "code:" + StringUtility.escapeQuery(s.getCode())).toList();
      final Query subsetQuery;
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
      final Query filterQuery = LuceneQueryBuilder.parse(
          new BrowserQueryBuilder().buildQuery(filter == null ? null : filter.getValue()),
          Concept.class);
      final Query booleanQuery =
          getAndQuery(terminologyQuery, subsetQuery, filterQuery, expressionQuery);
      final SearchParameters params = new SearchParameters(booleanQuery, offset, ct, null, null);
      if (activeOnly) {
        params.setActive(activeOnly);
      }
      final ResultList<Concept> list = searchService.find(params, Concept.class);
      final ValueSetExpansionComponent expansion =
          buildExpansionHeader(offset, ct, filter, version, (int) Math.min(list.getTotal(),
              Integer.MAX_VALUE));
      final boolean isLoinc =
          systemUri != null && systemUri.contains("loinc.org");
      for (final Concept concept : list.getItems()) {
        final ValueSetExpansionContainsComponent code = new ValueSetExpansionContainsComponent()
            .setSystem(systemUri).setCode(concept.getCode()).setDisplay(concept.getName());
        if (languages != null) {
          for (final Term term : concept.getTerms()) {
            if (!Sets.intersection(languages, term.getLocaleMap().keySet()).isEmpty()) {
              final Map<String, String> displayMap = FhirUtility.getDisplayMap(searchService,
                  concept.getTerminology(), concept.getPublisher(), concept.getVersion());
              final Coding coding = new Coding();
              coding.setCode(term.getType());
              if (isLoinc) {
                coding.setDisplay(term.getType());
              } else {
                final String useDisplay = term.getAttributes().get("designationUseDisplay");
                if (useDisplay != null) {
                  coding.setDisplay(useDisplay);
                } else if (displayMap.containsKey(term.getType())) {
                  coding.setDisplay(displayMap.get(term.getType()));
                }
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
      ValueSetExpandCache.putR5(cacheKey, vs);
      return vs;
    }

    final Query terminologyQuery = LuceneQueryBuilder.parse(
        TerminologyUtility.getTerminologyQuery(fromTerminology, fromPublisher, fromVersion),
        Concept.class);
    final Query filterQuery = LuceneQueryBuilder.parse(
        new BrowserQueryBuilder().buildQuery(filter == null ? null : filter.getValue()),
        Concept.class);
    final Query expressionQuery = getExpressionQuery(url == null ? null : url.getValue());
    final Query booleanQuery =
        getAndQuery(terminologyQuery, null, filterQuery, expressionQuery);
    final SearchParameters params = new SearchParameters(booleanQuery, offset, ct, null, null);
    if (activeOnly) {
      params.setActive(activeOnly);
    }
    final ResultList<Concept> list = searchService.find(params, Concept.class);
    final ValueSetExpansionComponent expansion = buildExpansionHeader(offset, ct, filter, version,
        (int) Math.min(list.getTotal(), Integer.MAX_VALUE));
    for (final Concept concept : list.getItems()) {
      final ValueSetExpansionContainsComponent code = new ValueSetExpansionContainsComponent()
          .setSystem(terminology.getUri()).setCode(concept.getCode()).setDisplay(concept.getName());
      if (languages != null) {
        final boolean isLoinc =
            terminology.getUri() != null && terminology.getUri().contains("loinc.org");
        for (final Term term : concept.getTerms()) {
          if (!Sets.intersection(languages, term.getLocaleMap().keySet()).isEmpty()) {
            final Map<String, String> displayMap = FhirUtility.getDisplayMap(searchService,
                concept.getTerminology(), concept.getPublisher(), concept.getVersion());
            final Coding coding = new Coding();
            coding.setCode(term.getType());
            if (isLoinc) {
              coding.setDisplay(term.getType());
            } else {
              final String useDisplay = term.getAttributes().get("designationUseDisplay");
              if (useDisplay != null) {
                coding.setDisplay(useDisplay);
              } else if (displayMap.containsKey(term.getType())) {
                coding.setDisplay(displayMap.get(term.getType()));
              }
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

    ValueSetExpandCache.putR5(cacheKey, vs);
    return vs;
  }


  /**
   * Expands a loaded ValueSet from SubsetMembers. Compose must already be set.
   *
   * @param vs the value set
   * @param members all subset members
   * @param systemUri includes system URI
   * @param offset expansion offset
   * @param ct expansion count
   * @param filter optional filter
   * @param version valueSetVersion param
   * @return value set with expansion
   */
  private ValueSet expandLoadedSubsetFromMembers(final ValueSet vs,
    final List<SubsetMember> members, final String systemUri, final int offset, final int ct,
    final StringType filter, final StringType version) {
    final String filterValue =
        filter == null || filter.getValue() == null ? null : filter.getValue().trim().toLowerCase();
    final List<SubsetMember> eligible = new ArrayList<>();
    for (final SubsetMember member : members) {
      if (member.getCode() == null) {
        continue;
      }
      // Match compose: inactive codes are omitted
      if (member.getCodeActive() != null && !member.getCodeActive()) {
        continue;
      }
      if (filterValue != null && !filterValue.isEmpty()) {
        final String code = member.getCode().toLowerCase();
        final String name = member.getName() == null ? "" : member.getName().toLowerCase();
        if (!code.contains(filterValue) && !name.contains(filterValue)) {
          continue;
        }
      }
      eligible.add(member);
    }
    eligible.sort(Comparator.comparing(SubsetMember::getCode, Comparator.nullsLast(String::compareTo)));

    final int total = eligible.size();
    final ValueSetExpansionComponent expansion =
        buildExpansionHeader(offset, ct, filter, version, total);
    if (ct > 0 && offset < total) {
      final int to = Math.min(offset + ct, total);
      for (final SubsetMember member : eligible.subList(offset, to)) {
        expansion.addContains(new ValueSetExpansionContainsComponent().setSystem(systemUri)
            .setCode(member.getCode()).setDisplay(member.getName()));
      }
    }
    vs.setExpansion(expansion);
    vs.setMeta(null);
    return vs;
  }

  /**
   * Builds expansion metadata parameters.
   *
   * @param offset the offset
   * @param ct the count
   * @param filter the filter
   * @param version the version
   * @param total the total
   * @return expansion component
   */
  private ValueSetExpansionComponent buildExpansionHeader(final int offset, final int ct,
    final StringType filter, final StringType version, final int total) {
    final ValueSetExpansionComponent expansion = new ValueSetExpansionComponent();
    expansion.setId(UUID.randomUUID().toString());
    expansion.setTimestamp(new Date());
    expansion.setTotal(total);
    expansion.setOffset(offset);
    if (filter != null) {
      expansion.addParameter(
          new ValueSetExpansionParameterComponent().setName("filter").setValue(filter));
    }
    expansion.addParameter(
        new ValueSetExpansionParameterComponent().setName("count").setValue(new IntegerType(ct)));
    if (version != null) {
      expansion.addParameter(new ValueSetExpansionParameterComponent().setName("version")
          .setValue(new StringType(version.getValue())));
    }
    return expansion;
  }

  /**
   * Resolves the compose/expansion system URI for a loaded subset.
   *
   * @param subset the subset
   * @param terminology the terminology
   * @return system URI
   */
  private String resolveLoadedSubsetSystemUri(final Subset subset, final Terminology terminology) {
    if (subset.getAttributes() != null) {
      final String includesUri = subset.getAttributes().get("fhirIncludesUri");
      if (includesUri != null && !includesUri.isEmpty()) {
        return includesUri;
      }
    }
    return terminology == null ? null : terminology.getUri();
  }

  /**
   * Creates the empty value set expansion.
   *
   * @param vs the vs
   * @param offset the offset
   * @param ct the ct
   * @param filter the filter
   * @param version the version
   * @return the value set
   */
  private ValueSet createEmptyValueSetExpansion(final ValueSet vs, final int offset, final int ct,
    final StringType filter, final StringType version) {
    final ValueSetExpansionComponent expansion = new ValueSetExpansionComponent();
    expansion.setId(UUID.randomUUID().toString());
    expansion.setTimestamp(new Date());
    expansion.setTotal(0);
    expansion.setOffset(offset);
    expansion.addParameter(
        new ValueSetExpansionParameterComponent().setName("count").setValue(new IntegerType(ct)));
    if (filter != null) {
      expansion.addParameter(
          new ValueSetExpansionParameterComponent().setName("filter").setValue(filter));
    }
    if (version != null) {
      expansion.addParameter(new ValueSetExpansionParameterComponent().setName("version")
          .setValue(new StringType(version.getValue())));
    }
    vs.setExpansion(expansion);
    vs.setMeta(null);
    return vs;
  }

  /**
   * Loads ValueSet subsets for findPossibleValueSets. Uses direct get/search when id or url
   * is provided so expand-by-id does not scan every loaded ValueSet.
   *
   * @param id the id filter
   * @param url the url filter
   * @return matching subsets (may be empty)
   * @throws Exception the exception
   */
  private List<Subset> findLoadedValueSetSubsets(final TokenParam id, final UriParam url)
    throws Exception {
    if (id != null && id.getValue() != null) {
      final Subset subset = searchService.get(id.getValue(), Subset.class);
      if (subset != null && "ValueSet".equals(subset.getCategory())) {
        return List.of(subset);
      }
      return List.of();
    }
    if (url != null && url.getValue() != null) {
      final String query = StringUtility.composeQuery("AND", "category:ValueSet",
          "uri:" + StringUtility.escapeQuery(url.getValue()));
      return searchService.find(new SearchParameters(query, 0, 100, null, null), Subset.class)
          .getItems();
    }
    final SearchParameters subsetParams = new SearchParameters();
    subsetParams.getFilters().put("category", "ValueSet");
    return searchService.findAll(subsetParams, Subset.class).getItems();
  }

  /**
   * Subset members for this subset (abbreviation + publisher + version on nested subset ref).
   *
   * @param subset the subset
   * @return members, possibly empty
   * @throws Exception the exception
   */
  private List<SubsetMember> findSubsetMembersForSubset(final Subset subset) throws Exception {
    if (subset == null) {
      return new ArrayList<>();
    }
    // Load members via paged findAll.
    final String subsetMemberQuery = StringUtility.composeQuery("AND",
        StringUtility.escapeKeywordField("subset.abbreviation", subset.getAbbreviation()),
        StringUtility.escapeKeywordField("subset.publisher", subset.getPublisher()),
        StringUtility.escapeKeywordField("subset.version", subset.getVersion()),
        StringUtility.escapeKeywordField("subset.code", subset.getCode()));
    return searchService.findAll(subsetMemberQuery, null, SubsetMember.class);
  }

  /**
   * Sets the compose from loaded subset.
   *
   * @param vs the vs
   * @param subset the subset
   * @param members the members
   * @param systemFallback the system fallback
   */
  private void setComposeFromLoadedSubset(final ValueSet vs, final Subset subset,
    final List<SubsetMember> members, final String systemFallback) {
    String includesUri =
        subset.getAttributes() != null ? subset.getAttributes().get("fhirIncludesUri") : null;
    if (includesUri == null || includesUri.isEmpty()) {
      includesUri = systemFallback;
    }
    if (includesUri == null || includesUri.isEmpty()) {
      return;
    }
    final ValueSetComposeComponent compose = new ValueSetComposeComponent();
    final ConceptSetComponent include = new ConceptSetComponent();
    include.setSystem(includesUri);
    for (final SubsetMember member : members) {
      if (member.getCode() == null || (member.getCodeActive() != null && !member.getCodeActive())) {
        continue;
      }
      final ConceptReferenceComponent concept =
          new ConceptReferenceComponent().setCode(member.getCode());
      if (member.getName() != null) {
        concept.setDisplay(member.getName());
      }
      include.addConcept(concept);
    }
    if (!include.getConcept().isEmpty()) {
      compose.addInclude(include);
      vs.setCompose(compose);
    }
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
    final List<Terminology> allTerminologies = FhirUtility.lookupTerminologies(searchService);
    for (final Terminology terminology : allTerminologies) {
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
    // Prefer direct id/url lookup; avoid findAll of every ValueSet subset.
    final List<Subset> subsets = findLoadedValueSetSubsets(id, url);
    for (final Subset subset : subsets) {
      final ValueSet set =
          FhirUtilityR5.toR5ValueSet(subset, new ArrayList<SubsetMember>(0), metaFlag, searchService);
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
    // returns the value set even if server.mode is not regenstrief).
    final boolean lllgRequested =
        (url != null && loincValueSetHelper.isLllgValueSetUrl(url.getValue()))
            || (id != null && id.getValue() != null && loincValueSetHelper.isLllgId(id.getValue()))
            || (code != null && code.getValue() != null
                && loincValueSetHelper.isLllgId(code.getValue()));
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
          final Terminology loincForLllg;
          if (version != null && !version.isEmpty()) {
            final String requestedVersion = version.getValue();
            loincForLllg = allTerminologies.stream()
                .filter(t -> t.getUri() != null && t.getUri().contains("loinc.org"))
                .filter(t -> requestedVersion.equals(t.getVersion()))
                .findFirst().orElse(null);
          } else {
            loincForLllg = loinc;
          }
          if (loincForLllg != null) {
            final Concept lllgConcept =
                loincValueSetHelper.findLllgConcept(searchService, loincForLllg, lllgId);
            final String valueSetId = lllgConcept != null ? lllgConcept.getId() : null;
            final ValueSet lllgVs =
                FhirUtilityR5.toR5LllgValueSet(loincForLllg, lllgId, valueSetId, metaFlag);
            final boolean idUrlMatch =
                (id == null || FhirUtilityR5.matchesLllgValueSetId(id.getValue(), lllgVs))
                    && (url == null || url.getValue().equals(lllgVs.getUrl()));
            final boolean dateMatch =
                date == null || FhirUtility.compareDate(date, lllgVs.getDate());
            final boolean versionMatch =
                version == null || FhirUtility.compareString(version, lllgVs.getVersion());
            final boolean nameMatch =
                name == null || FhirUtility.compareString(name, lllgVs.getName());
            final boolean publisherMatch =
                publisher == null || FhirUtility.compareString(publisher, lllgVs.getPublisher());
            final boolean titleMatch =
                title == null || FhirUtility.compareString(title, lllgVs.getTitle());
            final boolean descriptionMatch = description == null
                || FhirUtility.compareString(description, lllgVs.getDescription());
            if (idUrlMatch && dateMatch && versionMatch && nameMatch && publisherMatch && titleMatch
                && descriptionMatch) {
              list.add(lllgVs);
            }
          }
        } else if (id != null && id.getValue() != null && loincValueSetHelper.isEnabled()) {
          // Targeted by resource id: direct Concept get for LL/LG.
          final Concept concept = searchService.get(id.getValue(), Concept.class);
          if (concept != null && concept.getCode() != null
              && loincValueSetHelper.isLllgId(concept.getCode())) {
            Terminology loincTerm = TerminologyUtility.getTerminology(searchService,
                concept.getTerminology(), concept.getPublisher(), concept.getVersion());
            if (loincTerm == null) {
              loincTerm = loinc;
            }
            if (version != null && !version.isEmpty()
                && (loincTerm == null || !version.getValue().equals(loincTerm.getVersion()))) {
              final String requestedVersion = version.getValue();
              loincTerm = allTerminologies.stream()
                  .filter(t -> t.getUri() != null && t.getUri().contains("loinc.org"))
                  .filter(t -> requestedVersion.equals(t.getVersion())).findFirst().orElse(null);
            }
            if (loincTerm != null) {
              final ValueSet lgVs =
                  FhirUtilityR5.toR5LllgValueSetFromConcept(loincTerm, concept, metaFlag);
              final boolean versionMatch =
                  version == null || FhirUtility.compareString(version, lgVs.getVersion());
              if (versionMatch) {
                list.add(lgVs);
              }
            }
          }
        } else if (loincValueSetHelper.isEnabled() && id == null && url == null) {
          // General listing only: enumerate LL/LG concepts when not targeting id/url.
          final List<Terminology> loincTerminologies = allTerminologies.stream()
              .filter(t -> t.getUri() != null && t.getUri().contains("loinc.org")).toList();
          for (final Terminology loincTerm : loincTerminologies) {
            final ResultList<Concept> lllgConcepts =
                loincValueSetHelper.findAllLllgConcepts(searchService, loincTerm, 10_000, 0);
            for (final Concept concept : lllgConcepts.getItems()) {
              if (!loincValueSetHelper.isLllgId(concept.getCode())) {
                continue;
              }
              final ValueSet lgVs =
                  FhirUtilityR5.toR5LllgValueSetFromConcept(loincTerm, concept, metaFlag);
              final boolean idUrlMatch =
                  (id == null || FhirUtilityR5.matchesLllgValueSetId(id.getValue(), lgVs))
                      && (url == null || url.getValue().equals(lgVs.getUrl()));
              final boolean dateMatch =
                  date == null || FhirUtility.compareDate(date, lgVs.getDate());
              final boolean versionMatch =
                  version == null || FhirUtility.compareString(version, lgVs.getVersion());
              final boolean nameMatch =
                  name == null || FhirUtility.compareString(name, lgVs.getName());
              final boolean publisherMatch =
                  publisher == null || FhirUtility.compareString(publisher, lgVs.getPublisher());
              final boolean titleMatch =
                  title == null || FhirUtility.compareString(title, lgVs.getTitle());
              final boolean descriptionMatch = description == null
                  || FhirUtility.compareString(description, lgVs.getDescription());
              if (idUrlMatch && dateMatch && versionMatch && nameMatch && publisherMatch && titleMatch
                  && descriptionMatch) {
                list.add(lgVs);
              }
            }
          }
        }
      }
    }

    return list;

  }

  /**
   * Resolves LOINC terminology for an LL/LG ValueSet from meta tags or resource fields.
   *
   * @param vs the value set
   * @return the terminology, or null if not found
   * @throws Exception the exception
   */
  private Terminology resolveLllgTerminology(final ValueSet vs) throws Exception {
    if (vs.getMeta() != null) {
      final String fromTerminology = vs.getMeta().getTag().stream()
          .filter(t -> "fromTerminology".equals(t.getSystem())).map(t -> t.getCode()).findFirst()
          .orElse(null);
      final String fromPublisher = vs.getMeta().getTag().stream()
          .filter(t -> "fromPublisher".equals(t.getSystem())).map(t -> t.getCode()).findFirst()
          .orElse(null);
      final String fromVersion = vs.getMeta().getTag().stream()
          .filter(t -> "fromVersion".equals(t.getSystem())).map(t -> t.getCode()).findFirst()
          .orElse(null);
      if (fromTerminology != null && fromPublisher != null && fromVersion != null) {
        final Terminology fromMeta = TerminologyUtility.getTerminology(searchService,
            fromTerminology, fromPublisher, fromVersion);
        if (fromMeta != null) {
          return fromMeta;
        }
      }
    }
    if (vs.getPublisher() != null && vs.getVersion() != null) {
      final Terminology loinc = loincValueSetHelper.findLoincTerminology(searchService);
      if (loinc != null) {
        final Terminology fromFields = TerminologyUtility.getTerminology(searchService,
            loinc.getAbbreviation(), vs.getPublisher(), vs.getVersion());
        if (fromFields != null) {
          return fromFields;
        }
      }
    }
    return loincValueSetHelper.findLoincTerminology(searchService);
  }

}
