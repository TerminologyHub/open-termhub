/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.util;

import static java.lang.String.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.OperationOutcome.IssueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.TimerCache;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Utility for fhir data building.
 */
public final class FhirUtility {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(FhirUtility.class);

  /** The terminologies cache. */
  private static TimerCache<List<Terminology>> terminologyCache = new TimerCache<>(1000, 10000);

  /** The mapset cache. */
  private static TimerCache<List<Mapset>> mapsetCache = new TimerCache<>(1000, 10000);

  /** The code system uri cache. */
  private static TimerCache<String> codeSystemUriCache = new TimerCache<>(1000, 10000);

  /** The display map. */
  private static Map<String, Map<String, String>> displayMap = new HashMap<>();

  /**
   * Instantiates an empty {@link FhirUtility}.
   */
  private FhirUtility() {
    // n/a
  }

  /**
   * Lookup terminologies.
   *
   * @param service the search service
   * @return the list
   * @throws Exception the exception
   */
  public static List<Terminology> lookupTerminologies(final EntityRepositoryService service)
    throws Exception {

    final SearchParameters params = new SearchParameters();
    params.setQuery("*:*");
    params.setLimit(100);

    logger.info("Looking up terminologies with query: {}", params.getQuery());
    final ResultList<Terminology> terminologies = service.find(params, Terminology.class);
    logger.info("Found {} terminologies", terminologies.getItems().size());

    for (final Terminology term : terminologies.getItems()) {
      logger.info("Found terminology: {} ({}), version: {}, publisher: {}", term.getName(),
          term.getAbbreviation(), term.getVersion(), term.getPublisher());
    }

    return terminologies.getItems();
  }

  /**
   * Gets the code.
   *
   * @param code the code
   * @param coding the coding
   * @return the code
   * @throws Exception the exception
   */
  public static String getCode(final CodeType code, final Coding coding) throws Exception {
    if (code != null) {
      return code.getCode();
    }
    if (coding != null) {
      return coding.getCode();
    }

    return null;

  }

  /**
   * Gets the terminology.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param publisher the publisher
   * @param version the version
   * @return the terminology
   * @throws Exception the exception
   */
  public static Terminology getTerminology(final EntityRepositoryService searchService,
    final String terminology, final String publisher, final String version) throws Exception {

    final String key = publisher + terminology + version;
    final List<Terminology> t = terminologyCache.get(key);
    if (t != null) {
      return t.get(0);
    }
    final ResultList<Terminology> tlist =
        searchService
            .find(
                new SearchParameters("abbreviation:" + StringUtility.escapeQuery(terminology)
                    + " AND publisher: \"" + StringUtility.escapeQuery(publisher)
                    + "\" AND version:" + StringUtility.escapeQuery(version), 2, 0),
                Terminology.class);

    if (tlist.getItems().isEmpty()) {
      return null;
    }
    if (tlist.getItems().size() > 1) {
      throw new Exception(
          "Too many terminology matches = " + terminology + ", " + publisher + ", " + version);
    }
    terminologyCache.put(key, ModelUtility.asList(tlist.getItems().get(0)));
    return tlist.getItems().get(0);
  }

  /**
   * Lookup terminology.
   *
   * @param searchService the search service
   * @param codeSystemUri the code system uri
   * @return the string
   * @throws Exception the exception
   */
  public static String lookupTerminology(final EntityRepositoryService searchService,
    final String codeSystemUri) throws Exception {

    String terminology = codeSystemUriCache.get(codeSystemUri);

    if (terminology != null) {
      return terminology;
    }

    final String query = "uri:" + StringUtility.escapeQuery(codeSystemUri);
    final SearchParameters params = new SearchParameters(query, null, 1000, null, null);
    logger.debug("lookupTerminology: query: {}", query);
    final ResultList<Terminology> tlist = searchService.find(params, Terminology.class);

    if (tlist.getItems().isEmpty()) {
      return null;
    }

    if (tlist.getItems().size() > 1) {
      throw new Exception("Too many terminology matches = " + codeSystemUri);
    }

    terminology = tlist.getItems().get(0).getAbbreviation();

    codeSystemUriCache.put(codeSystemUri, terminology);

    return terminology;

  }

  /**
   * Gets the terminology.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param publisher the publisher
   * @param version the version
   * @return the terminology
   * @throws Exception the exception
   */
  public static String getCodeSystemUri(final EntityRepositoryService searchService,
    final String terminology, final String publisher, final String version) throws Exception {

    final Terminology t = getTerminology(searchService, terminology, publisher, version);
    return t == null ? null : t.getAttributes().get("fhirUri");
  }

  /**
   * Gets the code system uri.
   *
   * @param searchService the search service
   * @param concept the concept
   * @return the code system uri
   * @throws Exception the exception
   */
  public static String getCodeSystemUri(final EntityRepositoryService searchService,
    final ConceptRef concept) throws Exception {
    final Terminology t = getTerminology(searchService, concept.getTerminology(),
        concept.getPublisher(), concept.getVersion());
    return t == null ? null : t.getAttributes().get("fhirUri");
  }

  /**
   * Mutually exclusive.
   *
   * @param param1Name the param 1 name
   * @param param1 the param 1
   * @param param2Name the param 2 name
   * @param param2 the param 2
   */
  public static void mutuallyExclusive(final String param1Name, final Object param1,
    final String param2Name, final Object param2) {
    if (param1 != null && param2 != null) {
      throw exception(format("Use one of '%s' or '%s' parameters.", param1Name, param2Name),
          OperationOutcome.IssueType.INVARIANT, HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  /**
   * Not supported.
   *
   * @param paramName the param name
   * @param obj the obj
   */
  public static void notSupported(final String paramName, final Object obj) {
    notSupported(paramName, obj, null);
  }

  /**
   * Not supported.
   *
   * @param paramName the param name
   * @param obj the obj
   * @param additionalDetail the additional detail
   */
  public static void notSupported(final String paramName, final Object obj,
    final String additionalDetail) {
    if (obj != null) {
      final String message = format("Input parameter '%s' is not supported%s", paramName,
          (additionalDetail == null ? "." : format(" %s", additionalDetail)));
      throw exception(message, OperationOutcome.IssueType.NOTSUPPORTED,
          HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  /**
   * Require exactly one of.
   *
   * @param param1Name the param 1 name
   * @param param1 the param 1
   * @param param2Name the param 2 name
   * @param param2 the param 2
   */
  public static void requireExactlyOneOf(final String param1Name, final Object param1,
    final String param2Name, final Object param2) {
    if (param1 == null && param2 == null) {
      throw exception(
          format("One of '%s' or '%s' parameters must be supplied.", param1Name, param2Name),
          IssueType.INVARIANT, HttpServletResponse.SC_BAD_REQUEST);
    } else {
      mutuallyExclusive(param1Name, param1, param2Name, param2);
    }
  }

  /**
   * Require exactly one of.
   *
   * @param param1Name the param 1 name
   * @param param1 the param 1
   * @param param2Name the param 2 name
   * @param param2 the param 2
   * @param param3Name the param 3 name
   * @param param3 the param 3
   */
  public static void requireExactlyOneOf(final String param1Name, final Object param1,
    final String param2Name, final Object param2, final String param3Name, final Object param3) {
    if (param1 == null && param2 == null && param3 == null) {
      throw exception(
          format("One of '%s' or '%s' or '%s' parameters must be supplied.", param1Name, param2Name,
              param3Name),
          OperationOutcome.IssueType.INVARIANT, HttpServletResponse.SC_BAD_REQUEST);
    } else {
      mutuallyExclusive(param1Name, param1, param2Name, param2);
      mutuallyExclusive(param1Name, param1, param3Name, param3);
      mutuallyExclusive(param2Name, param2, param3Name, param3);
    }
  }

  /**
   * Mutually required.
   *
   * @param param1Name the param 1 name
   * @param param1 the param 1
   * @param param2Name the param 2 name
   * @param param2 the param 2
   */
  public static void mutuallyRequired(final String param1Name, final Object param1,
    final String param2Name, final Object param2) {
    if (param1 != null && param2 == null) {
      throw exception(
          format("Input parameter '%s' can only be used in conjunction with parameter '%s'.",
              param1Name, param2Name),
          IssueType.INVARIANT, HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  /**
   * Mutually required.
   *
   * @param param1Name the param 1 name
   * @param param1 the param 1
   * @param param2Name the param 2 name
   * @param param2 the param 2
   * @param param3Name the param 3 name
   * @param param3 the param 3
   */
  public static void mutuallyRequired(final String param1Name, final Object param1,
    final String param2Name, final Object param2, final String param3Name, final Object param3) {
    if (param1 != null && param2 == null && param3 == null) {
      throw exception(
          format("Use of input parameter '%s' only allowed if '%s' or '%s' is also present.",
              param1Name, param2Name, param3Name),
          IssueType.INVARIANT, HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  /**
   * Recover code.
   *
   * @param code the code
   * @param coding the coding
   * @return the string
   */
  public static String recoverCode(final CodeType code, final Coding coding) {
    if (code == null && coding == null) {
      throw exception("Use either 'code' or 'coding' parameters, not both.",
          OperationOutcome.IssueType.INVARIANT, HttpServletResponse.SC_BAD_REQUEST);
    } else if (code != null) {
      if (code.getCode().contains("|")) {
        throw exception(
            "The 'code' parameter cannot supply a codeSystem. "
                + "Use 'coding' or provide CodeSystem in 'system' parameter.",
            OperationOutcome.IssueType.NOTSUPPORTED, HttpServletResponse.SC_BAD_REQUEST);
      }
      return code.getCode();
    }
    return coding.getCode();
  }

  /**
   * Exception not supported.
   *
   * @param message the message
   * @return the FHIR server response exception
   */
  public static FHIRServerResponseException exceptionNotSupported(final String message) {
    return exception(message, OperationOutcome.IssueType.NOTSUPPORTED,
        HttpServletResponse.SC_NOT_IMPLEMENTED);
  }

  /**
   * Exception.
   *
   * @param message the message
   * @param issueType the issue type
   * @param theStatusCode the the status code
   * @return the FHIR server response exception
   */
  public static FHIRServerResponseException exception(final String message,
    final OperationOutcome.IssueType issueType, final int theStatusCode) {
    return exception(message, issueType, theStatusCode, null);
  }

  /**
   * Exception.
   *
   * @param message the message
   * @param issueType the issue type
   * @param theStatusCode the the status code
   * @param e the e
   * @return the FHIR server response exception
   */
  public static FHIRServerResponseException exception(final String message,
    final OperationOutcome.IssueType issueType, final int theStatusCode, final Throwable e) {
    final OperationOutcome outcome = new OperationOutcome();
    final OperationOutcome.OperationOutcomeIssueComponent component =
        new OperationOutcome.OperationOutcomeIssueComponent();
    component.setSeverity(OperationOutcome.IssueSeverity.ERROR);
    component.setCode(issueType);
    component.setDiagnostics(message);
    outcome.addIssue(component);
    return new FHIRServerResponseException(theStatusCode, message, outcome, e);
  }

  /**
   * Gets the type name.
   *
   * @param obj the obj
   * @return the type name
   */
  @SuppressWarnings("unused")
  private static String getTypeName(final Object obj) {
    if (obj instanceof String) {
      return "valueString";
    } else if (obj instanceof Boolean) {
      return "valueBoolean";
    }
    return null;
  }

  /**
   * Compare string.
   *
   * @param s1 the s 1
   * @param s2 the s 2
   * @return true, if successful
   */
  public static boolean compareString(final StringParam s1, final String s2) {
    // If we've not specified a search term, then we pass through a match
    if (s1 == null || StringUtils.isEmpty(s1.getValue())) {
      return true;
    }

    // If we've specified a search term but the target element is not populated,
    // that's not a
    // match
    if (s2 == null) {
      return false;
    }

    // What sort of matching are we doing? StartsWith by default
    if (s1.isExact()) {
      return s2.equalsIgnoreCase(s1.getValue());
    } else if (s1.isContains()) {
      return s2.toLowerCase().contains(s1.getValue().toLowerCase());
    } else {
      return s2.toLowerCase().startsWith(s1.getValue().toLowerCase());
    }
  }

  /**
   * Compare date range.
   *
   * @param d1 the d 1
   * @param d2 the d 2
   * @return true, if successful
   */
  public static boolean compareDateRange(final DateRangeParam d1, final Date d2) {

    // If we've not specified a search term, then we pass through a match
    if (d1 == null) {
      return true;
    }

    // If we've specified a search term but the target element is not populated,
    // that's not a
    // match
    if (d2 == null) {
      return false;
    }

    // Check that date is in range
    return compareDate(d1.getLowerBound(), d2) && compareDate(d1.getUpperBound(), d2);
  }

  /**
   * Compare date.
   *
   * @param d1 the d 1
   * @param d2 the d 2
   * @return true, if successful
   */
  public static boolean compareDate(final DateParam d1, final Date d2) {

    // If we've not specified a search term, then we pass through a match
    if (d1 == null) {
      return true;
    }

    // If we've specified a search term but the target element is not populated,
    // that's not a
    // match
    if (d2 == null) {
      return false;
    }

    // NO prefix is equals
    if (d1.getPrefix() == null) {
      return d1.getValue().equals(d2);
    }
    switch (d1.getPrefix()) {
      case APPROXIMATE: {
        return d1.getValue().equals(d2);
      }
      case ENDS_BEFORE: {
        // doesn't really make sense for a single date
        return d2.compareTo(d1.getValue()) < 0;
      }
      case EQUAL: {
        return d1.getValue().equals(d2);
      }
      case GREATERTHAN: {
        return d2.compareTo(d1.getValue()) > 0;
      }
      case GREATERTHAN_OR_EQUALS: {
        return d2.compareTo(d1.getValue()) >= 0;
      }
      case LESSTHAN: {
        return d2.compareTo(d1.getValue()) < 0;
      }
      case LESSTHAN_OR_EQUALS: {
        return d2.compareTo(d1.getValue()) <= 0;
      }
      case NOT_EQUAL: {
        return !d1.getValue().equals(d2);
      }
      case STARTS_AFTER: {
        // doesn't really make sense for a single date
        return d2.compareTo(d1.getValue()) > 0;
      }
      default:
        break;
    }
    return false;
  }

  /**
   * Compare date.
   *
   * @param d1 the d 1
   * @param d2 the d 2
   * @return true, if successful
   */
  public static boolean compareDate(final DateRangeParam d1, final Date d2) {

    // If we've not specified a search term, then we pass through a match
    if (d1 == null) {
      return true;
    }

    // If we've specified a search term but the target element is not populated,
    // that's not a
    // match
    if (d2 == null) {
      return false;
    }

    // Lower bound
    final DateParam lower = d1.getLowerBound();
    boolean lowerFlag = true;
    // NO prefix is equals
    if (lower == null) {
      lowerFlag = true;
    } else {
      switch (lower.getPrefix()) {
        case APPROXIMATE: {
          return Math.abs(lower.getValue().getTime() - d2.getTime()) < 5000;
        }
        case ENDS_BEFORE: {
          return d2.compareTo(lower.getValue()) < 0;
        }
        case EQUAL: {
          return lower.getValue().equals(d2);
        }
        case GREATERTHAN: {
          return d2.compareTo(lower.getValue()) > 0;
        }
        case GREATERTHAN_OR_EQUALS: {
          return d2.compareTo(lower.getValue()) >= 0;
        }
        case LESSTHAN: {
          return d2.compareTo(lower.getValue()) < 0;
        }
        case LESSTHAN_OR_EQUALS: {
          return d2.compareTo(lower.getValue()) <= 0;
        }
        case NOT_EQUAL: {
          return !lower.getValue().equals(d2);
        }
        case STARTS_AFTER: {
          return d2.compareTo(lower.getValue()) > 0;
        }
        default:
          break;
      }
    }

    // Upper bound
    final DateParam upper = d1.getUpperBound();
    boolean upperFlag = true;
    // NO prefix is equals
    if (upper == null) {
      upperFlag = true;
    } else {
      switch (upper.getPrefix()) {
        case APPROXIMATE: {
          return Math.abs(upper.getValue().getTime() - d2.getTime()) < 5000;
        }
        case ENDS_BEFORE: {
          return d2.compareTo(upper.getValue()) < 0;
        }
        case EQUAL: {
          return upper.getValue().equals(d2);
        }
        case GREATERTHAN: {
          return d2.compareTo(upper.getValue()) > 0;
        }
        case GREATERTHAN_OR_EQUALS: {
          return d2.compareTo(upper.getValue()) >= 0;
        }
        case LESSTHAN: {
          return d2.compareTo(upper.getValue()) < 0;
        }
        case LESSTHAN_OR_EQUALS: {
          return d2.compareTo(upper.getValue()) <= 0;
        }
        case NOT_EQUAL: {
          return !upper.getValue().equals(d2);
        }
        case STARTS_AFTER: {
          return d2.compareTo(upper.getValue()) > 0;
        }
        default:
          break;
      }
    }
    return lowerFlag && upperFlag;
  }

  /**
   * Gets the display map.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @return the display map
   * @throws Exception the exception
   */
  public static Map<String, String> getDisplayMap(final EntityRepositoryService searchService,
    final Terminology terminology) throws Exception {

    final String key =
        terminology.getAbbreviation() + terminology.getPublisher() + terminology.getVersion();

    // Lazy initialize for the terminology
    if (displayMap.containsKey(key)) {
      return displayMap.get(key);
    }

    // we need the query to avoid duplicate keys in the map
    final Map<String, String> map = new HashMap<>();
    for (final Metadata metadata : searchService.findAll(
        "active:true AND ((model:concept AND field:attribute) OR "
            + "(model:relationship AND field:additionalType) OR " + "(model:term AND field:type))",
        null, Metadata.class).stream().collect(Collectors.toList())) {
      map.put(metadata.getCode(), metadata.getName());
    }
    displayMap.put(key, map);
    return map;
  }

  /**
   * Gets the normal form.
   *
   * @param concept the concept
   * @param relationships the relationships
   * @param displayMap the display map
   * @param terse the terse
   * @return the normal form
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static String getNormalForm(final Concept concept,
    final List<ConceptRelationship> relationships, final Map<String, String> displayMap,
    final boolean terse) throws IOException {

    final boolean defined = concept.getDefined();
    final StringBuilder builder = new StringBuilder(defined ? "===" : "<<<");
    if (!terse) {
      builder.append(" ");
    }

    final Map<String, String> terms = terse ? null : new HashMap<>(displayMap);
    final Map<String, List<ConceptRelationship>> groupMap = new HashMap<>();
    for (final ConceptRelationship rel : relationships) {
      if (terms != null && rel.getTo() != null) {
        terms.put(rel.getTo().getCode(), rel.getTo().getName());
      }
      if (rel.getHierarchical() != null && rel.getHierarchical()) {
        continue;
      }
      if (!groupMap.containsKey(rel.getGroup())) {
        groupMap.put(rel.getGroup(), new ArrayList<>());
      }
      groupMap.get(rel.getGroup()).add(rel);
    }
    for (final String parentCode : relationships.stream()
        .filter(r -> r.getHierarchical() != null && r.getHierarchical())
        .map(r -> r.getTo().getCode()).collect(Collectors.toList())) {
      builder.append(getCode(parentCode, terms));
      builder.append(",");
    }
    builder.deleteCharAt(builder.length() - 1);

    if (!groupMap.isEmpty()) {
      builder.append(":");

      for (final Map.Entry<String, List<ConceptRelationship>> group : groupMap.entrySet()) {
        if (!group.getKey().equals("0") && !group.getKey().isEmpty()) {
          builder.append("{");
        }
        for (final ConceptRelationship relationship : group.getValue()) {
          builder.append(getCode(relationship.getAdditionalType().toString(), terms));
          builder.append("=");
          if (relationship.getToValue() != null) {
            builder.append(relationship.getToValue());
          } else {
            builder.append(getCode(relationship.getTo().getCode(), terms));
          }
          builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        if (!group.getKey().equals("0") && !group.getKey().isEmpty()) {
          builder.append("}");
        }
        builder.append(",");
      }
      builder.deleteCharAt(builder.length() - 1);
    }

    return builder.toString();
  }

  /**
   * Lookup mapsets.
   *
   * @param searchService the search service
   * @return the list
   * @throws Exception the exception
   */
  public static List<Mapset> lookupMapsets(final EntityRepositoryService searchService)
    throws Exception {

    // final String query = "active:true";
    final String query = "*:*";
    List<Mapset> mapsets = mapsetCache.get(query);

    if (mapsets == null) {
      // then do a find on the query
      final SearchParameters params = new SearchParameters(query, null, 1000, null, null);
      final ResultList<Mapset> results = searchService.find(params, Mapset.class);
      mapsets = results.getItems();

      // HOW MANY?
      logger.info("Found {} mapsets", mapsets.size());
      for (final Mapset mapset : mapsets) {
        logger.info("Mapset: {}", mapset);
      }

      // then sort the results (just use the natural terminology sort order)
      Collections.sort(mapsets);
      mapsetCache.put(query, mapsets);
    }
    return mapsets;
  }

  /**
   * Gets the code.
   *
   * @param code the code
   * @param terms the terms
   * @return the code
   */
  private static String getCode(final String code, final Map<String, String> terms) {
    if (terms != null) {
      return format("%s|%s|", code, terms.get(code));
    } else {
      return code;
    }
  }
}
