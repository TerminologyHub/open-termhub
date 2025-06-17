/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.rest.r5;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r5.model.Bundle.BundleLinkComponent;
import org.hl7.fhir.r5.model.Bundle.BundleType;
import org.hl7.fhir.r5.model.Bundle.LinkRelationTypes;
import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.ConceptMap;
import org.hl7.fhir.r5.model.DateTimeType;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.Enumerations.CodeSystemContentMode;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Meta;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.OperationOutcome.IssueType;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.UriType;
import org.hl7.fhir.r5.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.Definition;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.DateUtility;
import com.wci.termhub.util.ModelUtility;

import ca.uhn.fhir.rest.param.NumberParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Utility for fhir data building.
 */
public final class FhirUtilityR5 {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(FhirUtilityR5.class);

  /**
   * Instantiates an empty {@link FhirUtilityR5}.
   */
  private FhirUtilityR5() {
    // n/a
  }

  /**
   * To bool.
   *
   * @param bool the bool
   * @return true, if successful
   */
  public static boolean toBool(final BooleanType bool) {
    return bool != null && bool.booleanValue();
  }

  /**
   * Gets the terminologies.
   *
   * @param searchService the search service
   * @param id the id
   * @param code the code
   * @param systemField the system field
   * @param system the system
   * @param version the version
   * @param coding the coding
   * @return the terminologies
   * @throws Exception the exception
   */
  public static Terminology getTerminology(final EntityRepositoryService searchService,
    final IdType id, final CodeType code, final String systemField, final UriType system,
    final StringType version, final Coding coding) throws Exception {
    return getTerminology(searchService, id, code, systemField, system, version, coding, false);
  }

  /**
   * Gets the terminology.
   *
   * @param searchService the search service
   * @param id the id
   * @param code the code
   * @param systemField the system field
   * @param system the system
   * @param version the version
   * @param coding the coding
   * @param nullAllowed the null allowed
   * @return the terminology
   * @throws Exception the exception
   */
  public static Terminology getTerminology(final EntityRepositoryService searchService,
    final IdType id, final CodeType code, final String systemField, final UriType system,
    final StringType version, final Coding coding, final boolean nullAllowed) throws Exception {

    final String codeSystem = code == null ? null : code.getSystem();
    final String systemSystem = system == null ? null : system.getValue();
    final String codingSystem = coding == null ? null : coding.getSystem();
    // null values not added
    if (ModelUtility.asSet(codeSystem, systemSystem, codingSystem).size() > 1) {
      throw exception(
          "Conflicting system values = "
              + ModelUtility.asSet(codeSystem, systemSystem, codingSystem),
          OperationOutcome.IssueType.INVARIANT, HttpServletResponse.SC_BAD_REQUEST);
    }
    if (id == null && codeSystem == null && systemSystem == null && codingSystem == null) {
      if (nullAllowed) {
        return null;
      }
      throw FhirUtilityR5.exception("Code system not found", IssueType.NOTFOUND,
          HttpServletResponse.SC_NOT_FOUND);
    }

    final List<Terminology> list = new ArrayList<>();
    for (final Terminology terminology : FhirUtility.lookupTerminologies(searchService)) {
      final CodeSystem cs = toR5(terminology);
      // Skip non-matching id
      // Skip nonmatching system
      if ((id != null && !id.getIdPart().equals(cs.getId()))
          || (system != null && !system.getValue().equals(cs.getUrl()))) {
        continue;
      }
      if (code != null && code.getSystem() != null && !code.getSystem().equals(cs.getUrl())) {
        continue;
      }
      if (coding != null && coding.getSystem() != null && !coding.getSystem().equals(cs.getUrl())) {
        continue;
      }
      // Skip nomatching version
      if (version != null && !version.getValue().equals(cs.getVersion())) {
        continue;
      }

      // If we get this far, it matches.
      list.add(terminology);
    }
    if (list.size() == 1) {
      return list.get(0);
    }
    if (list.size() > 1) {
      throw FhirUtilityR5.exception(
          "Too many code systems found matching '" + systemField + "' "
              + "parameter, please specify version as well to differentiate",
          IssueType.NOTFOUND, HttpServletResponse.SC_BAD_REQUEST);

    }
    // If we get here and "system" was specified, we have a bad system
    if (system != null) {
      throw FhirUtilityR5.exception(
          "Code system not found matching '" + systemField + "' parameter", IssueType.NOTFOUND,
          HttpServletResponse.SC_NOT_FOUND);
    }
    if (nullAllowed) {
      return null;
    }
    throw FhirUtilityR5.exception("Code system not found", IssueType.NOTFOUND,
        HttpServletResponse.SC_NOT_FOUND);
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
      return code.getValue();
    }
    if (coding != null) {
      return coding.getCode();
    }

    return null;

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
   * Not supported.
   *
   * @param request the request
   * @param paramName the param name
   */
  public static void notSupported(final HttpServletRequest request, final String paramName) {
    if (request.getParameterMap().containsKey(paramName)) {
      final String message =
          format("HttpServletResponse.SC_BAD_REQUEST)t parameter '%s' is not supported", paramName);
      throw exception(message, OperationOutcome.IssueType.NOTSUPPORTED,
          HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  /**
   * Not supported search params.
   *
   * @param request the request
   */
  public static void notSupportedSearchParams(final HttpServletRequest request) {
    for (final String param : new String[] {
        "_lastUpdated", "_tag", "_profile", "_security", "_text", "_list", "_type", "_include",
        "_revinclude", "_summary", "_total", "_elements", "_contained", "_containedType"

    }) {
      notSupported(request, param);
    }
    if (Collections.list(request.getParameterNames()).stream().filter(k -> k.startsWith("_has"))
        .count() > 0) {
      notSupported(request, "_has");
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
   * Require exactly one of.
   *
   * @param param1Name the param 1 name
   * @param param1 the param 1
   * @param param2Name the param 2 name
   * @param param2 the param 2
   * @param param3Name the param 3 name
   * @param param3 the param 3
   * @param param4Name the param 4 name
   * @param param4 the param 4
   */
  public static void requireExactlyOneOf(final String param1Name, final Object param1,
    final String param2Name, final Object param2, final String param3Name, final Object param3,
    final String param4Name, final Object param4) {
    if (param1 == null && param2 == null && param3 == null && param4 == null) {
      throw exception(
          format("One of '%s' or '%s' or '%s' or '%s' parameters must be supplied.", param1Name,
              param2Name, param3Name, param4Name),
          OperationOutcome.IssueType.INVARIANT, HttpServletResponse.SC_BAD_REQUEST);
    } else {
      mutuallyExclusive(param1Name, param1, param2Name, param2);
      mutuallyExclusive(param1Name, param1, param3Name, param3);
      mutuallyExclusive(param1Name, param1, param4Name, param4);
      mutuallyExclusive(param2Name, param2, param3Name, param3);
      mutuallyExclusive(param2Name, param2, param4Name, param4);
      mutuallyExclusive(param3Name, param3, param4Name, param4);
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
   * @param coding the codiHttpServletResponse.SC_BAD_REQUEST) * @return the string
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
   * Creates the property.
   *
   * @param propertyName the property name
   * @param propertyValue the property value
   * @param isCode the is code
   * @return the parameters. parameters parameter component
   */
  public static Parameters.ParametersParameterComponent createProperty(final String propertyName,
    final Object propertyValue, final boolean isCode) {
    // Make a property with code as "valueCode"
    final Parameters.ParametersParameterComponent property =
        new Parameters.ParametersParameterComponent().setName("property");
    property.addPart().setName("code").setValue(new CodeType(propertyName));

    // Determine the value
    final String propertyValueString = propertyValue == null ? "" : propertyValue.toString();
    // Set code type
    if (isCode) {
      property.addPart().setName("value").setValue(new CodeType(propertyValueString));
    }

    // Set coding ntype
    else if (propertyValue instanceof Coding) {
      property.addPart().setName("value").setValue((Coding) propertyValue);
    }
    // Set boolean type
    else if (propertyValue instanceof Boolean) {
      property.addPart().setName("value").setValue(new BooleanType((Boolean) propertyValue));
    }
    // Set date type
    else if (propertyValue instanceof Date) {
      property.addPart().setName("value").setValue(new DateTimeType((Date) propertyValue));
    }
    // Otherwise use a string
    else {
      final StringType value = new StringType(propertyValueString);
      property.addPart().setName("value").setValue(value);
    }
    return property;
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
   * To R 4.
   *
   * @param codeSystem the code system
   * @param concept the concept
   * @param properties the properties
   * @param displayMap the display map
   * @param relationships the relationships
   * @param children the children
   * @return the code system
   * @throws Exception the exception
   */
  public static Parameters toR5(final CodeSystem codeSystem, final Concept concept,
    final Set<String> properties, final Map<String, String> displayMap,
    final List<ConceptRelationship> relationships, final List<ConceptRef> children)
    throws Exception {
    final Parameters parameters = new Parameters();

    // Properties to include by default from
    // https://hl7.org/fhir/R5/codesystem-operation-lookup.html
    // url, name, version, display, definition,designation, parent and child,
    // lang.X

    // Always include these fields
    parameters.addParameter(new Parameters.ParametersParameterComponent().setName("code")
        .setValue(new CodeType(concept.getCode())));
    parameters.addParameter("system", codeSystem.getUrl());
    parameters.addParameter("name", codeSystem.getTitle());
    parameters.addParameter("version", codeSystem.getVersion());
    parameters.addParameter("display", concept.getName());
    parameters.addParameter(new Parameters.ParametersParameterComponent().setName("active")
        .setValue(new BooleanType(concept.getActive())));
    if (properties == null || properties.contains("sufficientlyDefined")) {
      parameters.addParameter(new Parameters.ParametersParameterComponent()
          .setName("sufficientlyDefined").setValue(new BooleanType(concept.getDefined())));
    }
    // TODO: review where the value comes from
    // if (codeSystem.getTitle().startsWith("SNOMED")) {
    // if (properties == null || properties.contains("effectiveTime")) {
    // parameters.addParameter(createProperty("effectiveTime",
    // DateUtility.DATE_YYYYMMDD.format(concept.getModified()), false));
    // }
    // } else {
    // if (properties == null || properties.contains("modified")) {
    // parameters.addParameter(createProperty("modified", concept.getModified(),
    // false));
    // }
    // }

    // TODO: review missing "defined"
    // if (properties == null || properties.contains("normalForm")) {
    // if (codeSystem.getTitle().startsWith("SNOMED")) {
    // parameters.addParameter(new
    // Parameters.ParametersParameterComponent().setName("normalForm")
    // .setValue(new StringType(
    // FhirUtility.getNormalForm(concept, relationships, displayMap, false))));
    // }
    // }

    // Definitions
    if (properties == null || properties.contains("definition")) {
      for (final Definition def : concept.getDefinitions()) {
        parameters.addParameter(createProperty("definition", def.getDefinition(), false));
      }
    }

    // Designations
    for (final Term term : concept.getTerms()) {

      final String lang = term.computeSingleLanguage();
      if (properties != null && !properties.contains("lang." + lang)) {
        continue;
      }
      final Parameters.ParametersParameterComponent designation =
          new Parameters.ParametersParameterComponent().setName("designation");
      // Language
      designation.addPart().setName("language")
          .setValue(new CodeType(term.computeSingleLanguage()));
      // Term Type
      if (displayMap.containsKey(term.getType())) {
        final Coding coding = new Coding();
        coding.setCode(term.getType());
        coding.setDisplay(displayMap.get(term.getType()));
        designation.addPart().setName("use").setValue(coding);
      } else {
        final Coding coding = new Coding();
        coding.setCode(term.getType());
        designation.addPart().setName("use").setValue(coding);
      }
      designation.addPart().setName("value").setValue(new StringType(term.getName()));
      parameters.addParameter(designation);
      // for (String preferredLangRefset :
      // description.getPreferredLangRefsets()) {
      // Parameters.ParametersParameterComponent preferredDesignation =
      // new Parameters.ParametersParameterComponent().setName("designation");
      // preferredDesignation.addPart().setName("language").setValue(new
      // CodeType(
      // getLangAndRefsetCode(description.getLang(), preferredLangRefset)));
      // preferredDesignation.addPart().setName("use")
      // .setValue(PREFERED_FOR_LANGUAGE_CODING);
      // preferredDesignation.addPart().setName("value")
      // .setValue(new StringType(description.getTerm()));
      // parameters.addParameter(preferredDesignation);
      // }

    }

    // Concept attributes
    for (final String key : concept.getAttributes().keySet()) {
      if (properties != null && !properties.contains(key)) {
        continue;
      }
      final String value = concept.getAttributes().get(key);
      // Check for boolean value
      if ("true".equals(value) || "false".equals(value)) {
        parameters.addParameter(createProperty(key, Boolean.valueOf(value), false));
      }
      // Check for coding
      else if (displayMap.containsKey(value)) {
        final Coding coding = new Coding();
        coding.setCode(value);
        coding.setSystem(codeSystem.getUrl());
        coding.setDisplay(displayMap.get(value));
        parameters.addParameter(createProperty(key, coding, false));
      }

      // otherwise just a string
      else {
        parameters.addParameter(createProperty(key, value, false));
      }
    }

    // Parents/Children
    if (properties == null || properties.contains("parent")) {
      for (final ConceptRef parent : relationships.stream()
          .filter(r -> r.getHierarchical() != null && r.getHierarchical()).map(r -> r.getTo())
          .toList()) {
        final Coding coding = new Coding();
        coding.setCode(parent.getCode());
        coding.setSystem(codeSystem.getUrl());
        coding.setDisplay(parent.getName());
        parameters.addParameter(createProperty("parent", coding, false));
      }
    }
    if (properties == null || properties.contains("child")) {
      for (final ConceptRef child : children) {
        final Coding coding = new Coding();
        coding.setCode(child.getCode());
        coding.setSystem(codeSystem.getUrl());
        coding.setDisplay(child.getName());
        parameters.addParameter(createProperty("child", coding, false));
      }
    }

    return parameters;
  }

  /**
   * To R 4.
   *
   * @param codeSystem the code system
   * @param concept the concept
   * @param display the display
   * @return the parameters
   * @throws Exception the exception
   */
  public static Parameters toR5ValidateCode(final CodeSystem codeSystem, final Concept concept,
    final String display) throws Exception {

    // result 1..1 boolean
    // message 0..1 string
    // display 0..1 string
    // code 0..1 code
    // system 0..1 uri
    // version 0..1 string
    final Parameters parameters = new Parameters();
    final boolean result =
        concept != null && (display == null || display.equals(concept.getName()));
    parameters.addParameter("result", "" + result);
    if (!result) {
      if (concept == null) {
        parameters.addParameter("message",
            "The code does not exist for the supplied code system and/or version");
      } else {
        parameters.addParameter("message", "The code exists but the display is not valid");
        parameters.addParameter("display", concept.getName());
        parameters.addParameter("active", concept.getActive());
      }
    }
    // This if condition is guaranteed to be truew
    else if (concept != null) {
      parameters.addParameter("code", concept.getCode());
      parameters.addParameter("display", concept.getName());
      parameters.addParameter("active", concept.getActive());
    }
    parameters.addParameter("system", codeSystem.getUrl());
    parameters.addParameter("version", codeSystem.getVersion());
    return parameters;
  }

  /**
   * To R 4 value set.
   *
   * @param cs the code system
   * @return the value set
   * @throws Exception the exception
   */
  public static ValueSet toR5ValueSet(final CodeSystem cs) throws Exception {

    final ValueSet set = new ValueSet();
    set.setId(cs.getId() + "_entire");
    set.setUrl(cs.getUrl() + "?fhir_vs");
    set.setVersion(cs.getVersion());
    set.setName("VS " + cs.getName());
    set.setTitle(cs.getTitle() + "-ENTIRE");
    set.setStatus(PublicationStatus.ACTIVE);
    set.setDescription("Value set representing the entire contents of this code system");
    set.setCopyright("TBD: needs to be tracked in terminology info");
    set.setDate(cs.getDate());
    set.setPublisher(cs.getPublisher());
    set.setMeta(new Meta());

    return set;
  }

  /**
   * To R 4 subsumes.
   *
   * @param outcome the outcome
   * @param terminology the terminology
   * @return the parameters
   * @throws Exception the exception
   */
  public static Parameters toR5Subsumes(final String outcome, final Terminology terminology)
    throws Exception {

    // {
    // "resourceType": "Parameters",
    // "parameter": [ {
    // "name": "outcome",
    // "valueString": "not-subsumed"
    // }, {
    // "name": "system",
    // "valueString": "http://snomed.info/sct"
    // }, {
    // "name": "version",
    // "valueString":
    // "http://snomed.info/sct/900000000000207008/version/20231001"
    // } ]
    // }
    final Parameters parameters = new Parameters();
    parameters.addParameter("outcome", outcome);
    parameters.addParameter("system", terminology.getAttributes().get("fhirUri"));
    parameters.addParameter("version", terminology.getAttributes().get("fhirVersion"));
    return parameters;
  }

  /**
   * To R 4.
   *
   * @param terminology the terminology
   * @return the coding
   * @throws Exception the exception
   */
  public static CodeSystem toR5(final Terminology terminology) throws Exception {
    final CodeSystem cs = new CodeSystem();

    // cs.setUrl(terminology.getAttributes().get("fhirUri"));
    // fhirUri is not in the json data files. setting to id for now
    cs.setUrl(terminology.getUri());

    cs.setDate(DateUtility.DATE_YYYY_MM_DD_DASH.parse(terminology.getReleaseDate()));
    String version = terminology.getAttributes().get("fhirVersion");
    if (version == null) {
      version = terminology.getVersion();
    }
    cs.setVersion(version);
    // cs.setId(terminology.getAttributes().get("fhirId"));
    cs.setId(terminology.getId());
    cs.setName(terminology.getName());
    cs.setTitle(terminology.getAbbreviation());

    cs.setPublisher(terminology.getPublisher());

    cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
    cs.setHierarchyMeaning(CodeSystem.CodeSystemHierarchyMeaning.ISA);
    cs.setCompositional("true".equals(terminology.getAttributes().get("fhirCompositional")));

    if ("SANDBOX".equals(terminology.getPublisher())) {
      cs.setContent(CodeSystemContentMode.FRAGMENT);
    } else {
      cs.setContent(CodeSystemContentMode.COMPLETE);
    }

    cs.setExperimental(false);
    if (terminology.getStatistics().containsKey("concepts")) {
      cs.setCount(terminology.getStatistics().get("concepts"));
    } else if (terminology.getConceptCt() != null) {
      cs.setCount(terminology.getConceptCt().intValue());
    }

    logger.info("Converted terminology to CodeSystem: id={}, name={}, version={}", cs.getId(),
        cs.getName(), cs.getVersion());

    return cs;
  }

  /**
   * To R 5.
   *
   * @param mapset the mapset
   * @return the concept map
   * @throws Exception the exception
   */
  public static ConceptMap toR5(final Mapset mapset) throws Exception {
    if (mapset == null) {
      throw new FHIRServerResponseException(HttpServletResponse.SC_BAD_REQUEST,
          "Mapset cannot be null", null);
    }

    final ConceptMap cm = new ConceptMap();
    final Logger logger = LoggerFactory.getLogger(FhirUtilityR5.class);

    // Debug logging for Mapset data
    logger.info("Converting Mapset: id={}, fromTerminology={}, toTerminology={}", mapset.getId(),
        mapset.getFromTerminology(), mapset.getToTerminology());

    // Set other fields
    cm.setUrl(mapset.getAttributes().get("fhirUri"));
    // Use the stored FHIR version if available, otherwise use the regular
    // version
    cm.setVersion(mapset.getAttributes().containsKey("fhirVersion")
        ? mapset.getAttributes().get("fhirVersion") : mapset.getVersion());
    cm.setId(mapset.getId());
    cm.setName(mapset.getName());
    cm.setTitle(mapset.getAbbreviation());
    cm.setPublisher(mapset.getPublisher());
    cm.setStatus(Enumerations.PublicationStatus.ACTIVE);
    if (mapset.getCode() != null) {
      cm.addIdentifier(new Identifier().setValue(mapset.getCode()));
    }

    // Set source and target scopes from fromTerminology and toTerminology
    if (mapset.getFromTerminology() != null) {
      cm.setSourceScope(new UriType(mapset.getFromTerminology()));
      logger.info("Set sourceScope from fromTerminology: {}", mapset.getFromTerminology());
    } else if (mapset.getAttributes().containsKey("sourceScopeUri")) {
      cm.setSourceScope(new UriType(mapset.getAttributes().get("sourceScopeUri")));
      logger.info("Set sourceScope from attributes: {}",
          mapset.getAttributes().get("sourceScopeUri"));
    }

    if (mapset.getToTerminology() != null) {
      cm.setTargetScope(new UriType(mapset.getToTerminology()));
      logger.info("Set targetScope from toTerminology: {}", mapset.getToTerminology());
    } else if (mapset.getAttributes().containsKey("targetScopeUri")) {
      cm.setTargetScope(new UriType(mapset.getAttributes().get("targetScopeUri")));
      logger.info("Set targetScope from attributes: {}",
          mapset.getAttributes().get("targetScopeUri"));
    }

    // Debug final state
    logger.info("Converted ConceptMap: id={}, sourceScope={}, targetScope={}", cm.getId(),
        cm.getSourceScope() != null ? ((UriType) cm.getSourceScope()).getValue() : "null",
        cm.getTargetScope() != null ? ((UriType) cm.getTargetScope()).getValue() : "null");

    return cm;
  }

  /**
   * Returns the next link.
   *
   * @param uri the uri
   * @param offset the offset
   * @param offsetInt the offset int
   * @param count the count
   * @param countInt the count int
   * @return the next link
   */
  public static BundleLinkComponent getNextLink(final String uri, final NumberParam offset,
    final int offsetInt, final NumberParam count, final int countInt) {
    final int nextOffset = offsetInt + countInt;
    String nextUri = uri;
    if (!uri.contains("?")) {
      nextUri = nextUri + "?";
    }
    if (offset != null) {
      nextUri = nextUri.replaceFirst("_offset=\\d+", "_offset=" + nextOffset);
    } else {
      nextUri += (nextUri.endsWith("?") ? "" : "&") + "_offset=" + nextOffset;
    }
    if (count != null) {
      nextUri = nextUri.replaceFirst("_count=\\d+", "_count=" + countInt);
    } else {
      nextUri += (nextUri.endsWith("?") ? "" : "&") + "_count=" + countInt;
    }

    return new BundleLinkComponent().setUrl(nextUri).setRelation(LinkRelationTypes.NEXT);
  }

  /**
   * Make bundle.
   *
   * @param request the request
   * @param list the list
   * @param count the count
   * @param offset the offset
   * @return the bundle
   */
  public static Bundle makeBundle(final HttpServletRequest request,
    final List<? extends Resource> list, final NumberParam count, final NumberParam offset) {

    final int countInt = count == null ? 100 : count.getValue().intValue();
    final int offsetInt = offset == null ? 0 : offset.getValue().intValue();
    final String thisUrl = request.getQueryString() == null ? request.getRequestURL().toString()
        : request.getRequestURL().append('?').append(request.getQueryString()).toString();
    final Bundle bundle = new Bundle();
    bundle.setId(UUID.randomUUID().toString());
    bundle.setType(BundleType.SEARCHSET);
    bundle.setTotal(list.size());
    // This seems to be automatically added
    // bundle.addLink(
    // new
    // BundleLinkComponent().setUrl(thisUrl).setRelation(LinkRelationTypes.SELF));
    if (offsetInt + countInt < list.size()) {
      bundle.addLink(FhirUtilityR5.getNextLink(thisUrl, offset, offsetInt, count, countInt));
    }
    for (int i = offsetInt; i < offsetInt + countInt; i++) {
      if (i > list.size() - 1) {
        break;
      }
      final BundleEntryComponent component = new BundleEntryComponent();
      component.setResource(list.get(i));
      component.setFullUrl(request.getRequestURL() + "/" + list.get(i).getId());
      bundle.addEntry(component);
    }
    return bundle;
  }
}
