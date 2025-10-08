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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.Questionnaire;
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
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.TerminologyUtility;

import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
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
 * QuestionnaireProviderR4. Prepared for a POC. Not fully tested or implemented.
 */
@Component
public class QuestionnaireProviderR4 implements IResourceProvider {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(QuestionnaireProviderR4.class);

  /** The Constant LOINC_SERVEY_INSTRUMENTS_CODE. */
  // TODO: Is there a better way to identify questionnaires?
  private static final String LOINC_SERVEY_INSTRUMENTS_CODE = "LP29696-9";

  /** The Constant LOINC_SYSTEM. */
  private static final String LOINC_SYSTEM = "LOINC";

  /** The Constant LOINC_PUBLISHER. */
  private static final String LOINC_PUBLISHER = "Regenstrief Institute";

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Gets the questionnaire.
   *
   * @param request the request
   * @param details the details
   * @param id the id
   * @return the questionnaire
   * @throws Exception the exception
   */
  @SuppressWarnings("null")
  @Read
  public Questionnaire getQuestionnaire(final HttpServletRequest request,
    final ServletRequestDetails details, @IdParam final IdType id) throws Exception {

    try {

      if (logger.isDebugEnabled()) {
        logger.debug("Looking for questionnare with ID: {}", id != null ? id.getIdPart() : "null");
      }

      // Validate input parameters
      if (id == null || id.getIdPart() == null || id.getIdPart().trim().isEmpty()) {
        throw FhirUtilityR4.exception("Questionnaire ID is required", IssueType.INVALID,
            HttpServletResponse.SC_BAD_REQUEST);
      }

      final String conceptCode = id.getIdPart().contains("/")
          ? id.getIdPart().substring(id.getIdPart().indexOf("/") + 1) : id.getIdPart();

      // Find LOINC concept using TerminologyUtility
      final Terminology latestTerminologyVersion = TerminologyUtility
          .getLatestTerminologyVersion(searchService, LOINC_SYSTEM, LOINC_PUBLISHER);
      final Concept concept =
          TerminologyUtility.getConcept(searchService, latestTerminologyVersion, conceptCode);
      if (concept == null) {
        throw FhirUtilityR4.exception(
            "Questionnaire not found = " + (id == null ? "null" : id.getIdPart()),
            IssueType.NOTFOUND, HttpServletResponse.SC_NOT_FOUND);
      }

      // Verify the code represents a questionnaire (has PanelType:Panel
      // attribute)
      if (!isQuestionnaireConcept(concept)) {
        throw FhirUtilityR4.exception("Concept " + conceptCode + " is not a questionnaire",
            IssueType.NOTFOUND, HttpServletResponse.SC_NOT_FOUND);
      }

      // Convert found concept to Questionnaire resource and return
      final Questionnaire questionnaire = FhirUtilityR4.toR4Questionnaire(concept, searchService);

      // Populate with questions and answers
      FhirUtilityR4.populateQuestionnaire(questionnaire, searchService, latestTerminologyVersion);

      return questionnaire;

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to get questionnaire",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Find questionnaires.
   *
   * <pre>
   * Parameters for all resources
   *   used: _id
   *   not used: _content, _filter, _has, _in, _language, _lastUpdated,
   *             _list, _profile, _query, _security, _source, _tag, _text, _type
   * https://hl7.org/fhir/R4/questionnaire.html (see Search Parameters)
   * The following parameters in the registry are not used
   * &#64;OptionalParam(name="context-quantity") QuantityParam contextQuantity,
   * &#64;OptionalParam(name="context-type") TokenParam contextType,
   * &#64;OptionalParam(name="context-type-quantity") QuantityParam contextTypeQuantity,
   * &#64;OptionalParam(name="context-type-value") QuantityParam contextTypeValue,
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
  public Bundle findQuestionnaires(final HttpServletRequest request,
    final ServletRequestDetails details, @OptionalParam(name = "_id") final TokenParam id,
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

      // Validate and process search parameters
      validateSearchParameters(count, offset);

      FhirUtilityR4.notSupportedSearchParams(request);

      // Get possible questionnaires with enhanced search capabilities
      final List<Questionnaire> allQuestionnaires = findPossibleQuestionnaires(true, id, code, date,
          description, identifier, name, publisher, title, url, version);

      // Log search activity for monitoring
      final Map<String, Object> searchParams = buildSearchParamsMap(id, code, date, description,
          identifier, name, publisher, title, url, version);
      logSearchActivity(searchParams, allQuestionnaires.size());

      // Create and return bundle with proper paging support
      // Note: makeBundle will handle the paging by only including the requested
      // page
      return FhirUtilityR4.makeBundle(request, allQuestionnaires, count, offset);

    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to find questionnaires",
          OperationOutcome.IssueType.EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Validates search parameters for proper paging support.
   *
   * @param count the count parameter
   * @param offset the offset parameter
   * @throws FHIRServerResponseException if parameters are invalid
   */
  private void validateSearchParameters(final NumberParam count, final NumberParam offset)
    throws FHIRServerResponseException {

    // Validate count parameter
    if (count != null) {
      final int countValue = count.getValue().intValue();
      if (countValue < 0) {
        throw FhirUtilityR4.exception("_count parameter must be non-negative", IssueType.INVALID,
            HttpServletResponse.SC_BAD_REQUEST);
      }
      if (countValue > 1000) {
        throw FhirUtilityR4.exception("_count parameter cannot exceed 1000", IssueType.INVALID,
            HttpServletResponse.SC_BAD_REQUEST);
      }
    }

    // Validate offset parameter
    if (offset != null) {
      final int offsetValue = offset.getValue().intValue();
      if (offsetValue < 0) {
        throw FhirUtilityR4.exception("_offset parameter must be non-negative", IssueType.INVALID,
            HttpServletResponse.SC_BAD_REQUEST);
      }
    }
  }

  /**
   * Logs questionnaire search activity for monitoring and debugging.
   *
   * @param searchParams the search parameters used
   * @param resultCount the number of results found
   */
  private void logSearchActivity(final Map<String, Object> searchParams, final int resultCount) {
    if (logger.isDebugEnabled()) {
      final StringBuilder sb = new StringBuilder();
      sb.append("Questionnaire search completed. ");
      sb.append("Parameters: ").append(searchParams).append(". ");
      sb.append("Results: ").append(resultCount);
      logger.debug(sb.toString());
    }
  }

  /**
   * Builds a map of search parameters for logging purposes.
   *
   * @param id the id parameter
   * @param code the code parameter
   * @param date the date parameter
   * @param description the description parameter
   * @param identifier the identifier parameter
   * @param name the name parameter
   * @param publisher the publisher parameter
   * @param title the title parameter
   * @param url the url parameter
   * @param version the version parameter
   * @return map of search parameters
   */
  private Map<String, Object> buildSearchParamsMap(final TokenParam id, final TokenParam code,
    final DateRangeParam date, final StringParam description, final TokenParam identifier,
    final StringParam name, final StringParam publisher, final StringParam title,
    final UriParam url, final StringParam version) {

    final Map<String, Object> params = new HashMap<>();

    if (id != null) {
      params.put("_id", id.getValue());
    }
    if (code != null) {
      params.put("code", code.getValue());
    }
    if (date != null) {
      params.put("date", date.toString());
    }
    if (description != null) {
      params.put("description", description.getValue());
    }
    if (identifier != null) {
      params.put("identifier", identifier.getValue());
    }
    if (name != null) {
      params.put("name", name.getValue());
    }
    if (publisher != null) {
      params.put("publisher", publisher.getValue());
    }
    if (title != null) {
      params.put("title", title.getValue());
    }
    if (url != null) {
      params.put("url", url.getValue());
    }
    if (version != null) {
      params.put("version", version.getValue());
    }

    return params;
  }

  /**
   * Gets the resource type.
   *
   * @return the resource type
   */
  /* see superclass */
  @Override
  public Class<Questionnaire> getResourceType() {
    return Questionnaire.class;
  }

  /**
   * Find possible questionnaires.
   *
   * @param metaFlag the meta flag
   * @param id the id
   * @param url the url
   * @param version the version
   * @return the list
   * @throws Exception the exception
   */
  public List<Questionnaire> findPossibleQuestionnaires(final boolean metaFlag, final IdType id,
    final UriType url, final StringType version) throws Exception {
    final TokenParam idParam = id == null ? null : new TokenParam(id.getIdPart());
    final UriParam urlParam = url == null ? null : new UriParam(url.getValue());
    final StringParam versionParam = version == null ? null : new StringParam(version.getValue());
    return findPossibleQuestionnaires(metaFlag, idParam, null, null, null, null, null, null, null,
        urlParam, versionParam);
  }

  /**
   * Find possible questionnaires.
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
  public List<Questionnaire> findPossibleQuestionnaires(final boolean metaFlag, final TokenParam id,
    final TokenParam code, final DateRangeParam date, final StringParam description,
    final TokenParam identifier, final StringParam name, final StringParam publisher,
    final StringParam title, final UriParam url, final StringParam version) throws Exception {

    final List<Questionnaire> list = new ArrayList<>();

    // Find LOINC concepts with PanelType:Panel attribute
    final SearchParameters questionnaireParams = new SearchParameters();
    questionnaireParams.getFilters().put("attributes.PanelType", "Panel");

    String codeValue = (url != null) ? extractCodeFromUrl(url.getValue()) : null;
    if (StringUtils.isBlank(codeValue) && id != null) {
      codeValue = id.getValue();
    }

    if (!StringUtils.isBlank(codeValue)) {
      // if (codeValue != null) {
      // Direct lookup by code - much simpler
      try {
        final Terminology latestTerminologyVersion = TerminologyUtility
            .getLatestTerminologyVersion(searchService, LOINC_SYSTEM, LOINC_PUBLISHER);
        final Concept concept =
            TerminologyUtility.getConcept(searchService, latestTerminologyVersion, codeValue);

        if (concept != null && isQuestionnaireConcept(concept)) {
          final Questionnaire questionnaire =
              FhirUtilityR4.toR4Questionnaire(concept, searchService);
          // Populate full content for URL searches to match LOINC FHIR server
          // behavior
          FhirUtilityR4.populateQuestionnaire(questionnaire, searchService,
              latestTerminologyVersion);
          list.add(questionnaire);
          return list;
        } else {
          return list;
        }
      } catch (final Exception e) {
        logger.error("Error looking up concept by code: {}", e.getMessage());
        return list;
      }
      // } else {
      // return list;
      // }
    }
    //
    // // If URL is provided, do a direct lookup by code
    // if (url != null) {
    // final String codeValue = extractCodeFromUrl(url.getValue());
    //
    // if (codeValue != null) {
    // // Direct lookup by code - much simpler
    // try {
    // final Terminology latestTerminologyVersion =
    // TerminologyUtility.getLatestTerminologyVersion(searchService,
    // LOINC_SYSTEM, LOINC_PUBLISHER);
    // final Concept concept = TerminologyUtility.getConcept(searchService,
    // latestTerminologyVersion, codeValue);
    //
    // if (concept != null && isQuestionnaireConcept(concept)) {
    // final Questionnaire questionnaire =
    // FhirUtilityR4.toR4Questionnaire(concept, searchService);
    // // Populate full content for URL searches to match LOINC FHIR server
    // // behavior
    // FhirUtilityR4.populateQuestionnaire(questionnaire, searchService,
    // latestTerminologyVersion);
    // list.add(questionnaire);
    // return list;
    // } else {
    // return list;
    // }
    // } catch (final Exception e) {
    // logger.error("Error looking up concept by code: {}", e.getMessage());
    // return list;
    // }
    // } else {
    // return list;
    // }
    // }
    //
    // // If ID is provided, do a direct lookup
    // if (id != null) {
    // final String codeValue = id.getValue();
    //
    // try {
    // final Terminology latestTerminologyVersion =
    // TerminologyUtility.getLatestTerminologyVersion(searchService,
    // LOINC_SYSTEM, LOINC_PUBLISHER);
    // final Concept concept = TerminologyUtility.getConcept(searchService,
    // latestTerminologyVersion, codeValue);
    //
    // if (concept != null && isQuestionnaireConcept(concept)) {
    // final Questionnaire questionnaire =
    // FhirUtilityR4.toR4Questionnaire(concept, searchService);
    // // Populate full content for ID searches to match LOINC FHIR server
    // // behavior
    // FhirUtilityR4.populateQuestionnaire(questionnaire, searchService,
    // latestTerminologyVersion);
    // list.add(questionnaire);
    // return list; // Return immediately with single result
    // } else {
    // return list; // Return empty list
    // }
    // } catch (final Exception e) {
    // logger.error("Error looking up concept by code: {}", e.getMessage());
    // return list; // Return empty list on error
    // }
    // }

    // Add additional search filters if provided
    if (code != null) {
      questionnaireParams.getFilters().put("code", code.getValue());
    }

    if (name != null) {
      questionnaireParams.getFilters().put("name", name.getValue());
    }

    if (description != null) {
      questionnaireParams.getFilters().put("definitions.definition", description.getValue());
    }

    if (publisher != null) {
      questionnaireParams.getFilters().put("publisher", publisher.getValue());
    }

    if (title != null) {
      questionnaireParams.getFilters().put("name", title.getValue());
    }

    if (version != null) {
      questionnaireParams.getFilters().put("version", version.getValue());
    }

    // Set reasonable limits for performance
    questionnaireParams.setLimit(1000);
    questionnaireParams.setOffset(0);

    if (logger.isDebugEnabled()) {
      logger.debug("Final search parameters: {}", questionnaireParams.getFilters());
    }

    final List<Concept> concepts =
        searchService.findAll(questionnaireParams, Concept.class).getItems();

    if (logger.isDebugEnabled()) {
      logger.debug("Found {} concepts with PanelType:Panel", concepts.size());
      if (concepts.isEmpty()) {
        logger.debug("No concepts found with search parameters: {}",
            questionnaireParams.getFilters());
      }
    }

    for (final Concept concept : concepts) {

      // Verify this is a questionnaire concept
      if (!isQuestionnaireConcept(concept)) {
        continue;
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Converting concept {} to questionnaire", concept.getCode());
      }

      final Questionnaire questionnaire = FhirUtilityR4.toR4Questionnaire(concept, searchService);

      // Apply filtering based on search criteria
      if (!matchesSearchCriteria(questionnaire, id, code, date, description, identifier, name,
          publisher, title, url, version)) {
        if (logger.isDebugEnabled()) {
          logger.debug("Questionnaire {} did not match search criteria", questionnaire.getId());
        }
        continue;
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Questionnaire {} matched search criteria, adding to results",
            questionnaire.getId());
      }

      list.add(questionnaire);
      if (logger.isDebugEnabled()) {
        logger.debug("Added questionnaire {} to results list", questionnaire.getId());
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Returning {} questionnaires after filtering", list.size());
      if (list.isEmpty()) {
        logger.debug("No questionnaires found after filtering");
      }
    }

    return list;
  }

  /**
   * Extracts the LOINC code from a questionnaire URL.
   *
   * @param url the questionnaire URL
   * @return the LOINC code or null if not found
   */
  private String extractCodeFromUrl(final String url) {
    if (url == null) {
      return null;
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Extracting code from URL: {}", url);
    }

    // Handle URLs like http://loinc.org/q/89689-4
    if (url.contains("/q/")) {
      final String[] parts = url.split("/q/");
      if (parts.length > 1) {
        String codePart = parts[1];
        // Remove any query parameters or fragments
        if (codePart.contains("?")) {
          codePart = codePart.substring(0, codePart.indexOf("?"));
        }
        if (codePart.contains("#")) {
          codePart = codePart.substring(0, codePart.indexOf("#"));
        }
        // Trim whitespace and clean up the code
        final String code = codePart.trim();
        if (logger.isDebugEnabled()) {
          logger.debug("Extracted code '{}' from URL part '{}'", code, codePart);
        }
        return code;
      }
    }

    // Handle URLs like http://loinc.org/q/89689-4?format=json
    if (url.contains("loinc.org/q/")) {
      final String[] parts = url.split("loinc.org/q/");
      if (parts.length > 1) {
        String codePart = parts[1];
        // Remove any query parameters or fragments
        if (codePart.contains("?")) {
          codePart = codePart.substring(0, codePart.indexOf("?"));
        }
        if (codePart.contains("#")) {
          codePart = codePart.substring(0, codePart.indexOf("#"));
        }
        // Trim whitespace and clean up the code
        final String code = codePart.trim();
        if (logger.isDebugEnabled()) {
          logger.debug("Extracted code '{}' from URL part '{}'", code, codePart);
        }
        return code;
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Could not extract code from URL: {}", url);
    }
    return null;
  }

  /**
   * Checks if a concept represents a questionnaire based on the requirements.
   *
   * @param concept the concept to check
   * @return true if the concept is a questionnaire, false otherwise
   */
  private boolean isQuestionnaireConcept(final Concept concept) {
    if (concept == null || concept.getAttributes() == null) {
      return false;
    }

    // Primary criteria: attributes.PanelType:Panel
    final String panelType = concept.getAttributes().get("PanelType");
    if ("Panel".equals(panelType)) {
      return true;
    }

    // Secondary criteria: LP29696-9 | Survey instruments |
    // Check if the concept has a relationship to the Survey instruments concept
    if (hasSurveyInstrumentsRelationship(concept)) {
      return true;
    }

    return false;
  }

  /**
   * Checks if a concept has a relationship to the Survey instruments concept (LP29696-9).
   *
   * @param concept the concept to check
   * @return true if the concept is related to Survey instruments, false otherwise
   */
  private boolean hasSurveyInstrumentsRelationship(final Concept concept) {
    if (concept == null) {
      return false;
    }

    try {
      // Search for relationships to the Survey instruments concept (LP29696-9)
      final SearchParameters params = new SearchParameters();
      params.getFilters().put("relationships.to.code", LOINC_SERVEY_INSTRUMENTS_CODE);
      params.getFilters().put("relationships.from.code", concept.getCode());

      // This would need to be implemented based on the actual relationship
      // model
      // For now, we'll use a placeholder approach
      // TODO: Implement actual relationship querying based on the data model

      return false;
    } catch (final Exception e) {
      logger.debug("Error checking survey instruments relationship for concept {}: {}",
          concept.getCode(), e.getMessage());
      return false;
    }
  }

  /**
   * Checks if a questionnaire matches the search criteria.
   *
   * @param questionnaire the questionnaire to check
   * @param id the id parameter
   * @param code the code parameter
   * @param date the date parameter
   * @param description the description parameter
   * @param identifier the identifier parameter
   * @param name the name parameter
   * @param publisher the publisher parameter
   * @param title the title parameter
   * @param url the url parameter
   * @param version the version parameter
   * @return true if the questionnaire matches all criteria, false otherwise
   */
  private boolean matchesSearchCriteria(final Questionnaire questionnaire, final TokenParam id,
    final TokenParam code, final DateRangeParam date, final StringParam description,
    final TokenParam identifier, final StringParam name, final StringParam publisher,
    final StringParam title, final UriParam url, final StringParam version) {

    if (logger.isDebugEnabled()) {
      logger.debug("Checking questionnaire {} against search criteria", questionnaire.getId());
    }

    try {
      if (id != null && !id.getValue().equals(questionnaire.getId())) {

        if (logger.isDebugEnabled()) {
          logger.debug("ID mismatch: request='{}' vs questionnaire='{}'", id.getValue(),
              questionnaire.getId());
        }
        return false;
      }

      if (name != null && !FhirUtility.compareString(name, questionnaire.getName())) {
        if (logger.isDebugEnabled()) {
          logger.debug("Name mismatch: request='{}' vs questionnaire='{}'", name.getValue(),
              questionnaire.getName());
        }
        return false;
      }

      if (title != null && !FhirUtility.compareString(title, questionnaire.getTitle())) {
        if (logger.isDebugEnabled()) {
          logger.debug("Title mismatch: request='{}' vs questionnaire='{}'", title.getValue(),
              questionnaire.getTitle());
        }
        return false;
      }

      if (description != null
          && !FhirUtility.compareString(description, questionnaire.getDescription())) {
        if (logger.isDebugEnabled()) {
          logger.debug("Description mismatch: request='{}' vs questionnaire='{}'",
              description.getValue(), questionnaire.getDescription());
        }
        return false;
      }

      if (publisher != null
          && !FhirUtility.compareString(publisher, questionnaire.getPublisher())) {
        if (logger.isDebugEnabled()) {
          logger.debug("Publisher mismatch: request='{}' vs questionnaire='{}'",
              publisher.getValue(), questionnaire.getPublisher());
        }
        return false;
      }

      if (date != null && !FhirUtility.compareDate(date, questionnaire.getDate())) {
        if (logger.isDebugEnabled()) {
          logger.debug("Date mismatch: request='{}' vs questionnaire='{}'", date.toString(),
              questionnaire.getDate());
        }
        return false;
      }

      if (code != null && !code.getValue().equals(questionnaire.getId())) {
        if (logger.isDebugEnabled()) {
          logger.debug("Code mismatch: request='{}' vs questionnaire='{}'", code.getValue(),
              questionnaire.getId());
        }
        return false;
      }

      if (identifier != null && !identifier.getValue().equals(questionnaire.getId())) {
        if (logger.isDebugEnabled()) {
          logger.debug("Identifier mismatch: request='{}' vs questionnaire='{}'",
              identifier.getValue(), questionnaire.getId());
        }
        return false;
      }

      if (version != null && !FhirUtility.compareString(version, questionnaire.getVersion())) {
        if (logger.isDebugEnabled()) {
          logger.debug("Version mismatch: request='{}' vs questionnaire='{}'", version.getValue(),
              questionnaire.getVersion());
        }
        return false;
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Questionnaire {} passed all search criteria", questionnaire.getId());
      }

      return true;

    } catch (final Exception e) {
      logger.debug("Error matching search criteria for questionnaire {}: {}", questionnaire.getId(),
          e.getMessage());
      return false;
    }
  }
}
