/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.rest.r4;

import static java.lang.String.format;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleLinkComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.ContactDetail;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetComposeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.Definition;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.SubsetMember;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.DateUtility;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.TerminologyUtility;

import ca.uhn.fhir.rest.param.NumberParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Utility for fhir data building.
 */
public final class FhirUtilityR4 {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);

  /**
   * Instantiates an empty {@link FhirUtilityR4}.
   */
  private FhirUtilityR4() {
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
      throw FhirUtilityR4.exception("Code system not found", IssueType.NOTFOUND,
          HttpServletResponse.SC_NOT_FOUND);
    }

    final List<Terminology> list = new ArrayList<>();
    for (final Terminology terminology : FhirUtility.lookupTerminologies(searchService)) {
      final CodeSystem cs = toR4(terminology);
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
      throw FhirUtilityR4.exception(
          "Too many code systems found matching '" + systemField + "' "
              + "parameter, please specify version as well to differentiate",
          IssueType.NOTFOUND, HttpServletResponse.SC_BAD_REQUEST);

    }
    // If we get here and "system" was specified, we have a bad system
    if (system != null) {
      throw FhirUtilityR4.exception(
          "Code system not found matching '" + systemField + "' parameter", IssueType.NOTFOUND,
          HttpServletResponse.SC_NOT_FOUND);
    }
    if (nullAllowed) {
      return null;
    }
    throw FhirUtilityR4.exception("Code system not found", IssueType.NOTFOUND,
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
      final String message = format("Input parameter '%s' is not supported", paramName);
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
   * To R4.
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
  public static Parameters toR4(final CodeSystem codeSystem, final Concept concept,
    final Set<String> properties, final Map<String, String> displayMap,
    final List<ConceptRelationship> relationships, final List<ConceptRef> children)
    throws Exception {
    return toR4(codeSystem, concept, properties, displayMap, relationships, children, null);
  }

  public static Parameters toR4(final CodeSystem codeSystem, final Concept concept,
    final Set<String> properties, final Map<String, String> displayMap,
    final List<ConceptRelationship> relationships, final List<ConceptRef> children,
    final Map<String, String> conceptNameMap) throws Exception {
    return toR4(codeSystem, concept, properties, displayMap, relationships, children,
        conceptNameMap, null);
  }

  public static Parameters toR4(final CodeSystem codeSystem, final Concept concept,
    final Set<String> properties, final Map<String, String> displayMap,
    final List<ConceptRelationship> relationships, final List<ConceptRef> children,
    final Map<String, String> conceptNameMap, final EntityRepositoryService searchService)
    throws Exception {
    final Parameters parameters = new Parameters();

    // Properties to include by default from
    // https://hl7.org/fhir/R4/codesystem-operation-lookup.html
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
      final Coding coding = new Coding();
      coding.setCode(term.getType());

      // Use stored system and display from term attributes if available
      final String useSystem = term.getAttributes().get("designationUseSystem");
      if (useSystem != null) {
        coding.setSystem(useSystem);
      }

      if (displayMap.containsKey(term.getType())) {
        coding.setDisplay(displayMap.get(term.getType()));
      } else {
        // Fallback to stored display from term attributes if available
        final String useDisplay = term.getAttributes().get("designationUseDisplay");
        if (useDisplay != null) {
          coding.setDisplay(useDisplay);
        }
      }

      designation.addPart().setName("use").setValue(coding);
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
      // Check if property value can be looked up in conceptNameMap to create
      // valueCoding
      // This transforms property values that match concept names into
      // valueCoding format
      String conceptCode = null;
      if (conceptNameMap != null && conceptNameMap.containsKey(value)) {
        conceptCode = conceptNameMap.get(value);
      }
      // Fallback: search for concept by name if not found in map and
      // searchService is available
      else if (searchService != null && value != null && !value.isEmpty()) {
        try {
          final SearchParameters nameParams = new SearchParameters(2, 0);
          nameParams.setQuery(StringUtility.composeQuery("AND", "active:true",
              "name:" + StringUtility.escapeQuery(value),
              "terminology:" + StringUtility.escapeQuery(concept.getTerminology()),
              concept.getVersion() != null
                  ? "version:" + StringUtility.escapeQuery(concept.getVersion()) : null,
              concept.getPublisher() != null
                  ? "publisher:" + StringUtility.escapeQuery(concept.getPublisher()) : null));
          final ResultList<Concept> nameResults =
              searchService.findFields(nameParams, ModelUtility.asList("code"), Concept.class);
          if (!nameResults.getItems().isEmpty()) {
            conceptCode = nameResults.getItems().get(0).getCode();
          }
        } catch (final Exception e) {
          // Ignore search errors, fall through to string value
        }
      }

      if (conceptCode != null) {
        final Coding coding = new Coding();
        coding.setCode(conceptCode);
        // Use the codeSystem URL as the system (for LOINC this will be
        // "http://loinc.org")
        coding.setSystem(codeSystem.getUrl());
        coding.setDisplay(value);
        parameters.addParameter(createProperty(key, coding, false));
      }
      // Check for coding in displayMap (existing logic)
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
   * To R4.
   *
   * @param codeSystem the code system
   * @param concept the concept
   * @param display the display
   * @return the parameters
   * @throws Exception the exception
   */
  public static Parameters toR4ValidateCode(final CodeSystem codeSystem, final Concept concept,
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
   * To R4 value set.
   *
   * @param terminology the terminology
   * @param metaFlag the meta flag
   * @return the value set
   * @throws Exception the exception
   */
  public static ValueSet toR4ValueSet(final Terminology terminology, final boolean metaFlag)
    throws Exception {

    final CodeSystem cs = FhirUtilityR4.toR4(terminology);
    final ValueSet set = new ValueSet();
    set.setId(cs.getId() + "_entire");
    set.setUrl(cs.getUrl() + "?fhir_vs");
    set.setVersion(cs.getVersion());
    set.setName("VS " + cs.getName());
    set.setTitle(cs.getTitle() + "-ENTIRE");
    set.setStatus(PublicationStatus.ACTIVE);
    set.setDescription("Value set representing the entire contents of this code system");
    set.setDate(cs.getDate());
    set.setPublisher(cs.getPublisher());
    set.setCopyright(cs.getCopyright());

    // Add "from" info for members
    if (metaFlag) {
      set.setMeta(new Meta().addTag("fromTerminology", terminology.getAbbreviation(), null)
          .addTag("fromPublisher", terminology.getPublisher(), null)
          .addTag("fromVersion", terminology.getVersion(), null)
          .addTag("includesUri", terminology.getUri(), null));
    } else {
      set.setMeta(new Meta());
    }
    set.getMeta().addTag("originalId", terminology.getAttributes().get("originalId"), null);

    return set;
  }

  /**
   * Converts a Subset and its SubsetMembers to a FHIR R4 ValueSet.
   *
   * @param subset the Subset
   * @param members the SubsetMembers
   * @param metaFlag the meta flag
   * @return the FHIR R4 ValueSet
   * @throws Exception the exception
   */
  public static ValueSet toR4ValueSet(final Subset subset, final List<SubsetMember> members,
    final boolean metaFlag) throws Exception {

    final ValueSet valueSet = new ValueSet();
    valueSet.setId(subset.getId());
    valueSet.setUrl(subset.getUri());
    valueSet.setPublisher(subset.getPublisher());
    valueSet.setVersion(subset.getVersion());
    // Parse the full date string with timezone information (if present)
    final String releaseDate = subset.getReleaseDate();
    if (releaseDate != null) {
      if (releaseDate.contains("T")) {
        // Full ISO 8601 date string with timezone
        valueSet.setDate(Date.from(Instant.parse(releaseDate)));
      } else {
        // Fallback to date-only format
        valueSet.setDate(DateUtility.DATE_YYYY_MM_DD_DASH.parse(releaseDate));
      }
    }

    valueSet.setName(subset.getName());
    // Set title from abbreviation if present, else fallback to name
    if (subset.getAbbreviation() != null && !subset.getAbbreviation().isEmpty()) {
      valueSet.setTitle(subset.getAbbreviation());
    } else {
      valueSet.setTitle(subset.getName());
    }
    valueSet.setDescription(subset.getDescription());
    valueSet.setStatus(Enumerations.PublicationStatus.ACTIVE);

    // Set experimental from attributes if present, else fallback
    final String experimentalStr = subset.getAttributes() != null
        ? subset.getAttributes().get(Subset.Attributes.fhirExperimental.name()) : null;
    if (experimentalStr != null) {
      valueSet.setExperimental(Boolean.parseBoolean(experimentalStr));
    }

    // Set identifier from attributes if present, else fallback
    valueSet.addIdentifier().setValue(subset.getCode())
        .setSystem("https://terminologyhub.com/model/subset/code");

    // Compose/include
    final ValueSetComposeComponent compose = new ValueSetComposeComponent();
    final ConceptSetComponent include = new ConceptSetComponent();

    include.setSystem(subset.getAttributes().get("fhirIncludesUri"));
    if (members != null) {
      for (final SubsetMember member : members) {
        if (member.getCode() == null
            || (member.getCodeActive() != null && !member.getCodeActive())) {
          continue;
        }
        final ConceptReferenceComponent concept = new ConceptReferenceComponent();
        concept.setCode(member.getCode());
        if (member.getName() != null) {
          concept.setDisplay(member.getName());
        }
        include.addConcept(concept);
      }
    }
    if (!include.getConcept().isEmpty()) {
      compose.addInclude(include);
      valueSet.setCompose(compose);
    }

    // Add "from" info for members
    if (metaFlag) {
      valueSet.setMeta(new Meta().addTag("fromTerminology", subset.getFromTerminology(), null)
          .addTag("fromPublisher", subset.getFromPublisher(), null)
          .addTag("fromVersion", subset.getFromVersion(), null)
          .addTag("includesUri", subset.getAttributes().get("fhirIncludesUri"), null));
    } else {
      valueSet.setMeta(new Meta());
    }
    valueSet.getMeta().addTag("originalId", subset.getAttributes().get("originalId"), null);

    return valueSet;
  }

  /**
   * To R4 subsumes.
   *
   * @param outcome the outcome
   * @param terminology the terminology
   * @return the parameters
   * @throws Exception the exception
   */
  public static Parameters toR4Subsumes(final String outcome, final Terminology terminology)
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
    parameters.addParameter("system", terminology.getUri());
    parameters.addParameter("version", terminology.getAttributes().get("fhirVersion"));
    return parameters;
  }

  /**
   * To R4.
   *
   * @param terminology the terminology
   * @return the coding
   * @throws Exception the exception
   */
  public static CodeSystem toR4(final Terminology terminology) throws Exception {
    final CodeSystem cs = new CodeSystem();

    cs.setUrl(terminology.getUri());

    // Parse the full date string with timezone information
    final String releaseDate = terminology.getReleaseDate();
    if (releaseDate != null && !releaseDate.isEmpty()) {
      if (releaseDate.contains("T")) {
        // Full ISO 8601 date string with timezone
        cs.setDate(Date.from(Instant.parse(releaseDate)));
      } else {
        // Fallback to date-only format
        cs.setDate(DateUtility.DATE_YYYY_MM_DD_DASH.parse(releaseDate));
      }
    }

    // Set version - prefer fhirVersion attribute if available, otherwise use
    // terminology version
    String version = terminology.getAttributes().get("fhirVersion");
    if (version == null) {
      version = terminology.getVersion();
    }
    cs.setVersion(version);

    // Set ID - ensure it's properly formatted
    String id = terminology.getId();
    if (id != null && id.contains("/")) {
      id = id.substring(id.lastIndexOf("/") + 1);
    }
    cs.setId(id);

    cs.setName(terminology.getName());
    cs.setTitle(terminology.getAbbreviation());
    cs.setPublisher(terminology.getPublisher());
    cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
    cs.setHierarchyMeaning(CodeSystem.CodeSystemHierarchyMeaning.ISA);
    cs.setCompositional("true".equals(terminology.getAttributes().get("fhirCompositional")));

    // Set content mode based on publisher
    if ("SANDBOX".equals(terminology.getPublisher())) {
      cs.setContent(CodeSystem.CodeSystemContentMode.FRAGMENT);
    } else {
      cs.setContent(CodeSystem.CodeSystemContentMode.COMPLETE);
    }

    cs.setExperimental(false);

    // Set count from statistics or conceptCt
    if (terminology.getStatistics() != null
        && terminology.getStatistics().containsKey("concepts")) {
      cs.setCount(terminology.getStatistics().get("concepts"));
    } else if (terminology.getConceptCt() != null) {
      cs.setCount(terminology.getConceptCt().intValue());
    }

    // Set copyright if available
    final String copyright = terminology.getAttributes().get("copyright");
    if (copyright != null) {
      cs.setCopyright(copyright);
    }

    // Add originalId as meta info
    if (terminology.getAttributes().containsKey("originalId")) {
      cs.setMeta(
          new Meta().addTag("originalId", terminology.getAttributes().get("originalId"), null));
    }

    // logger.info("Converted terminology to CodeSystem: id={}, name={},
    // version={}", cs.getId(),
    // cs.getName(), cs.getVersion());

    return cs;
  }

  /**
   * To R4.
   *
   * @param mapset the mapset
   * @return the concept map
   * @throws Exception the exception
   */
  public static ConceptMap toR4(final Mapset mapset) throws Exception {
    if (mapset == null) {
      throw new FHIRServerResponseException(HttpServletResponse.SC_BAD_REQUEST,
          "Mapset cannot be null", null);
    }
    final ConceptMap cm = new ConceptMap();

    // Debug logging for Mapset data
    // logger.info("Converting Mapset: id={}, fromTerminology={},
    // toTerminology={}", mapset.getId(),
    // mapset.getFromTerminology(), mapset.getToTerminology());

    cm.setUrl(mapset.getUri());
    if (mapset.getReleaseDate() != null) {
      final String releaseDate = mapset.getReleaseDate();
      if (releaseDate.contains("T")) {
        // Full ISO 8601 date string with timezone
        cm.setDate(Date.from(Instant.parse(releaseDate)));
      } else {
        // Fallback to date-only format
        cm.setDate(DateUtility.DATE_YYYY_MM_DD_DASH.parse(releaseDate));
      }
    }
    cm.setVersion(mapset.getAttributes().containsKey("fhirVersion")
        ? mapset.getAttributes().get("fhirVersion") : mapset.getVersion());
    cm.setId(mapset.getId());
    cm.setName(mapset.getName());
    cm.setTitle(mapset.getAbbreviation());
    cm.setPublisher(mapset.getPublisher());
    cm.setStatus(Enumerations.PublicationStatus.ACTIVE);
    cm.setCopyright(mapset.getAttributes().get("copyright"));
    if (mapset.getCode() != null) {
      cm.setIdentifier(new Identifier().setSystem("https://terminologyhub.com/model/mapset/code")
          .setValue(mapset.getCode()));
    }

    // Set source and target scopes from fromTerminology and toTerminology
    if (mapset.getAttributes().containsKey("fhirSourceUri")) {
      cm.setSource(new UriType(mapset.getAttributes().get("fhirSourceUri")));
    }

    if (mapset.getAttributes().containsKey("fhirTargetUri")) {
      cm.setTarget(new UriType(mapset.getAttributes().get("fhirTargetUri")));
    }

    // Add originalId as meta info
    if (mapset.getAttributes().containsKey("originalId")) {
      cm.setMeta(new Meta().addTag("originalId", mapset.getAttributes().get("originalId"), null));
    }

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
    return createPagingLink(uri, offset, offsetInt, count, countInt, nextOffset, "next");
  }

  /**
   * Gets the previous link.
   *
   * @param uri the uri
   * @param offset the offset
   * @param offsetInt the offset int
   * @param count the count
   * @param countInt the count int
   * @return the previous link
   */
  public static BundleLinkComponent getPreviousLink(final String uri, final NumberParam offset,
    final int offsetInt, final NumberParam count, final int countInt) {
    final int prevOffset = Math.max(0, offsetInt - countInt);
    return createPagingLink(uri, offset, offsetInt, count, countInt, prevOffset, "previous");
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

    final int countInt = count == null ? 25 : count.getValue().intValue();
    final int offsetInt = offset == null ? 0 : offset.getValue().intValue();
    final String thisUrl = request.getQueryString() == null ? request.getRequestURL().toString()
        : request.getRequestURL().append('?').append(request.getQueryString()).toString();

    final Bundle bundle = new Bundle();
    bundle.setId(UUID.randomUUID().toString());
    bundle.setType(BundleType.SEARCHSET);
    bundle.setTotal(list.size());

    // Add self link
    bundle.addLink(new BundleLinkComponent().setUrl(thisUrl).setRelation("self"));

    // Add previous link if not on first page
    if (offsetInt > 0) {
      bundle.addLink(FhirUtilityR4.getPreviousLink(thisUrl, offset, offsetInt, count, countInt));
    }

    // Add next link if there are more results
    final boolean shouldAddNext = offsetInt + countInt < list.size();
    if (shouldAddNext) {
      bundle.addLink(FhirUtilityR4.getNextLink(thisUrl, offset, offsetInt, count, countInt));
    }

    // Add entries for current page
    for (int i = offsetInt; i < offsetInt + countInt && i < list.size(); i++) {
      final BundleEntryComponent component = new BundleEntryComponent();
      component.setResource(list.get(i));
      component.setFullUrl(request.getRequestURL() + "/" + list.get(i).getId());
      bundle.addEntry(component);
    }

    return bundle;
  }

  /**
   * Converts a Terminology to a FHIR R4 Questionnaire.
   *
   * @param terminology the Terminology
   * @param metaFlag the meta flag
   * @return the FHIR R4 Questionnaire
   * @throws Exception the exception
   */
  public static Questionnaire toR4Questionnaire(final Terminology terminology,
    final boolean metaFlag) throws Exception {

    final Questionnaire questionnaire = new Questionnaire();
    questionnaire.setId(terminology.getId() + "_entire");
    questionnaire.setUrl(terminology.getUri() + "?fhir_questionnaire");
    questionnaire.setVersion(terminology.getVersion());
    questionnaire.setName("Q " + terminology.getName());
    questionnaire.setTitle(terminology.getAbbreviation() + "-ENTIRE");
    questionnaire.setStatus(PublicationStatus.ACTIVE);
    questionnaire
        .setDescription("Questionnaire representing the entire contents of this code system");
    questionnaire.setDate(terminology.getReleaseDate() != null
        ? Date.from(Instant.parse(terminology.getReleaseDate())) : null);
    questionnaire.setPublisher(terminology.getPublisher());

    // Add "from" info for members
    if (metaFlag) {
      questionnaire
          .setMeta(new Meta().addTag("fromTerminology", terminology.getAbbreviation(), null)
              .addTag("fromPublisher", terminology.getPublisher(), null)
              .addTag("fromVersion", terminology.getVersion(), null)
              .addTag("includesUri", terminology.getUri(), null));
    }

    return questionnaire;
  }

  /**
   * Converts a LOINC Concept to a FHIR R4 Questionnaire. This is the primary
   * method for creating questionnaires from LOINC concepts.
   *
   * @param concept the LOINC Concept
   * @param searchService the search service
   * @return the FHIR R4 Questionnaire
   * @throws Exception the exception
   */
  public static Questionnaire toR4Questionnaire(final Concept concept,
    final EntityRepositoryService searchService) throws Exception {

    final Questionnaire questionnaire = new Questionnaire();

    // Set basic metadata
    questionnaire.setId(concept.getCode());

    // Build URL from terminology information
    final String systemUri = getSystemUriFromTerminology(searchService, concept.getTerminology(),
        concept.getPublisher(), concept.getVersion());
    questionnaire.setUrl(systemUri + "/q/" + concept.getCode());

    questionnaire.setName(concept.getName());
    questionnaire.setTitle(concept.getName());
    questionnaire.setStatus(PublicationStatus.DRAFT);

    // Set description from definitions if available, or use name as fallback
    String description = concept.getName();
    if (concept.getDefinitions() != null && !concept.getDefinitions().isEmpty()) {
      description = concept.getDefinitions().get(0).getDefinition();
    }
    questionnaire.setDescription(description);

    // Set publisher from concept data
    questionnaire.setPublisher(concept.getPublisher());

    // Set subject type to Patient for most questionnaires
    final List<CodeType> subjectTypes = new ArrayList<>();
    subjectTypes.add(new CodeType("Patient"));
    questionnaire.setSubjectType(subjectTypes);

    // Add coding to questionnaire.code field using database information
    final Coding coding = new Coding();
    coding.setSystem(systemUri);
    coding.setCode(concept.getCode());
    coding.setDisplay(concept.getName());
    questionnaire.addCode(coding);

    // Add contact information using concept data
    final ContactDetail contact = new ContactDetail();
    contact.setName(concept.getPublisher());
    final ContactPoint telecom = new ContactPoint();
    telecom.setSystem(ContactPoint.ContactPointSystem.URL);
    telecom.setValue(systemUri);
    contact.addTelecom(telecom);
    questionnaire.addContact(contact);

    // Get copyright from concept attributes if available, otherwise use generic
    String copyright = null;
    if (concept.getAttributes() != null
        && concept.getAttributes().containsKey("EXTERNAL_COPYRIGHT_NOTICE")) {
      copyright = concept.getAttributes().get("EXTERNAL_COPYRIGHT_NOTICE");
    }
    questionnaire.setCopyright(copyright);

    return questionnaire;
  }

  /**
   * Populates a Questionnaire with questions and answers based on LOINC
   * relationships. This method uses the concept's existing relationships to
   * create questionnaire items.
   *
   * @param questionnaire the Questionnaire to populate
   * @param searchService the search service for data access
   * @param terminology the terminology
   * @throws Exception the exception
   */
  public static void populateQuestionnaire(final Questionnaire questionnaire,
    final EntityRepositoryService searchService, final Terminology terminology) throws Exception {

    // Clear any existing items
    questionnaire.getItem().clear();

    // Extract LOINC code from questionnaire ID
    final String loincCode = questionnaire.getId();
    if (loincCode == null) {
      return;
    }

    try {

      // Track all processed codes to prevent duplicates at any level
      final Set<String> processedCodes = new HashSet<>();
      processedCodes.add(loincCode); // Add the main questionnaire code

      // Get the main concept to find its relationships
      final Concept mainConcept =
          TerminologyUtility.getConcept(searchService, terminology.getAbbreviation(),
              terminology.getPublisher(), terminology.getVersion(), loincCode);
      if (mainConcept == null) {
        return;
      }

      // Find group concepts via has_member relationships
      final List<Questionnaire.QuestionnaireItemComponent> groupItems = findGroupConcepts(
          mainConcept, searchService, terminology, processedCodes, terminology.getVersion());

      // Add group items to questionnaire
      for (final Questionnaire.QuestionnaireItemComponent groupItem : groupItems) {
        questionnaire.addItem(groupItem);
      }

    } catch (final Exception e) {
      // Log error but don't fail the entire questionnaire
      final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
      logger.warn("Failed to populate questionnaire {}: {}", loincCode, e.getMessage());
    }
  }

  /**
   * Finds group concepts via has_member relationships from the main concept.
   *
   * @param mainConcept the main questionnaire concept
   * @param searchService the search service
   * @param terminology the terminology
   * @param processedCodes set of already processed codes
   * @param latestVersion the latest version
   * @return list of group questionnaire item components
   * @throws Exception the exception
   */
  private static List<Questionnaire.QuestionnaireItemComponent> findGroupConcepts(
    final Concept mainConcept, final EntityRepositoryService searchService,
    final Terminology terminology, final Set<String> processedCodes, final String latestVersion)
    throws Exception {

    final List<Questionnaire.QuestionnaireItemComponent> allItems = new ArrayList<>();

    try {
      // Find has_member relationships from the main concept
      final String hasMemberQuery = "from.code:" + StringUtility.escapeQuery(mainConcept.getCode())
          + " AND additionalType:has_member";
      final List<ConceptRelationship> hasMemberRels =
          searchService.findAll(hasMemberQuery, null, ConceptRelationship.class);

      for (final ConceptRelationship hasMemberRel : hasMemberRels) {
        if (hasMemberRel.getTo() != null) {
          final String memberCode = hasMemberRel.getTo().getCode();
          if (memberCode != null && !processedCodes.contains(memberCode)) {

            // Check if this member should be a group or direct question
            final Concept memberConcept = TerminologyUtility.getConcept(searchService, "LOINC",
                "Regenstrief Institute", latestVersion, memberCode);

            if (memberConcept != null) {
              final boolean isOrganizer = isOrganizerConcept(memberConcept);

              if (isOrganizer) {
                // Create a group with nested questions
                final Questionnaire.QuestionnaireItemComponent groupItem = createGroupItem(
                    hasMemberRel, searchService, terminology, processedCodes, latestVersion);
                if (groupItem != null) {
                  allItems.add(groupItem);
                  processedCodes.add(memberCode);
                }
              } else {
                // Create a direct question with answer options
                final Questionnaire.QuestionnaireItemComponent questionItem =
                    createDirectQuestionItem(hasMemberRel, searchService, terminology,
                        processedCodes, latestVersion);
                if (questionItem != null) {
                  allItems.add(questionItem);
                  processedCodes.add(memberCode);
                }
              }
            }
          }
        }
      }

    } catch (final Exception e) {
      final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
      logger.warn("Failed to find group concepts for main concept {}: {}", mainConcept.getCode(),
          e.getMessage());
    }

    return allItems;
  }

  /**
   * Creates a group questionnaire item from a has_member relationship.
   *
   * @param hasMemberRel the has_member relationship
   * @param searchService the search service
   * @param terminology the terminology
   * @param processedCodes set of already processed codes
   * @param latestVersion the latest version
   * @return the questionnaire item component
   * @throws Exception the exception
   */
  private static Questionnaire.QuestionnaireItemComponent createGroupItem(
    final ConceptRelationship hasMemberRel, final EntityRepositoryService searchService,
    final Terminology terminology, final Set<String> processedCodes, final String latestVersion)
    throws Exception {

    final Questionnaire.QuestionnaireItemComponent groupItem =
        new Questionnaire.QuestionnaireItemComponent();

    if (hasMemberRel.getTo() != null) {
      final ConceptRef toConcept = hasMemberRel.getTo();

      // Set group properties
      groupItem.setLinkId(toConcept.getCode());
      groupItem.setText(toConcept.getName());
      groupItem.setType(Questionnaire.QuestionnaireItemType.GROUP);
      groupItem.setRequired(true);

      // Add special properties for "Intensity of ideation" group (93303-6)
      if (toConcept.getCode().equals("93303-6")) {
        // Add enableWhen conditions based on master file
        groupItem.setEnableBehavior(Questionnaire.EnableWhenBehavior.ANY);

        // Add enableWhen conditions for questions 113944, 113952, 113947,
        // 113951
        final String[] enableWhenQuestions = {
            "113944", "113952", "113947", "113951"
        };
        for (final String questionId : enableWhenQuestions) {
          final Questionnaire.QuestionnaireItemEnableWhenComponent enableWhen =
              new Questionnaire.QuestionnaireItemEnableWhenComponent();
          enableWhen.setQuestion(questionId);
          enableWhen.setOperator(Questionnaire.QuestionnaireItemOperator.EQUAL);

          final Coding answerCoding = new Coding();
          answerCoding.setSystem("http://loinc.org");
          answerCoding.setCode("LA33-6");
          enableWhen.setAnswer(answerCoding);

          groupItem.addEnableWhen(enableWhen);
        }
      }

      // Add coding
      final Coding coding = new Coding();
      coding.setSystem(terminology.getUri());
      coding.setCode(toConcept.getCode());
      coding.setDisplay(toConcept.getName());
      groupItem.addCode(coding);

      // Find questions for this group
      final List<Questionnaire.QuestionnaireItemComponent> questions =
          findQuestionsForGroup(toConcept.getCode(), searchService, terminology, processedCodes);

      for (final Questionnaire.QuestionnaireItemComponent question : questions) {
        groupItem.addItem(question);
      }
    }

    return groupItem;
  }

  /**
   * Finds questions for a group via has_member relationships.
   *
   * @param groupCode the group LOINC code
   * @param searchService the search service
   * @param terminology the terminology
   * @param processedCodes set of already processed codes
   * @return list of question components
   * @throws Exception the exception
   */
  private static List<Questionnaire.QuestionnaireItemComponent> findQuestionsForGroup(
    final String groupCode, final EntityRepositoryService searchService,
    final Terminology terminology, final Set<String> processedCodes) throws Exception {

    final List<Questionnaire.QuestionnaireItemComponent> questions = new ArrayList<>();

    // Create a separate processedCodes set for this group to avoid conflicts
    // with main level
    final Set<String> groupProcessedCodes = new HashSet<>();

    try {
      // Find has_member relationships from the group concept
      final String hasMemberQuery =
          "from.code:" + StringUtility.escapeQuery(groupCode) + " AND additionalType:has_member";
      final List<ConceptRelationship> hasMemberRels =
          searchService.findAll(hasMemberQuery, null, ConceptRelationship.class);

      // Debug logging to see what we're finding
      final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
      logger.debug("Found {} has_member relationships for group {}: {}", hasMemberRels.size(),
          groupCode,
          hasMemberRels.stream().map(rel -> rel.getTo() != null ? rel.getTo().getCode() : "null")
              .collect(Collectors.joining(", ")));

      for (final ConceptRelationship hasMemberRel : hasMemberRels) {
        if (hasMemberRel.getTo() != null) {
          final String questionCode = hasMemberRel.getTo().getCode();
          if (questionCode != null && !groupProcessedCodes.contains(questionCode)) {
            // Filter for "Intensity of ideation" group to match master file
            // structure
            if (groupCode.equals("93303-6")) {
              // Filter out description items to match master file (12 items
              // instead of 14)
              final String questionText = hasMemberRel.getTo().getName();
              if (questionText != null && questionText.contains("description")) {
                logger.debug("Filtering out description item: {} - not in master file structure",
                    questionCode);
                continue;
              }
            }

            // Create question item
            final Questionnaire.QuestionnaireItemComponent questionItem =
                createQuestionItem(hasMemberRel, searchService, terminology, groupProcessedCodes);
            if (questionItem != null) {
              questions.add(questionItem);
              groupProcessedCodes.add(questionCode); // Mark as processed to
                                                     // avoid duplicates within
                                                     // this group
              logger.debug("Successfully created question item for code: {}", questionCode);
            } else {
              logger.warn(
                  "Failed to create question item for code: {} - createQuestionItem returned null",
                  questionCode);
            }
          } else {
            if (questionCode == null) {
              logger.warn("Question code is null for hasMemberRel: {}", hasMemberRel);
            } else if (groupProcessedCodes.contains(questionCode)) {
              logger.debug("Skipping already processed code: {}", questionCode);
            }
          }
        }
      }

    } catch (final Exception e) {
      // Log error but don't fail the entire questionnaire
      final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
      logger.warn("Failed to find questions for group {}: {}", groupCode, e.getMessage());
    }

    return questions;
  }

  /**
   * Creates a question item from a has_member relationship.
   *
   * @param hasMemberRel the has_member relationship
   * @param searchService the search service
   * @param terminology the terminology
   * @param processedCodes set of already processed codes
   * @return the questionnaire item component
   * @throws Exception the exception
   */
  private static Questionnaire.QuestionnaireItemComponent createQuestionItem(
    final ConceptRelationship hasMemberRel, final EntityRepositoryService searchService,
    final Terminology terminology, final Set<String> processedCodes) throws Exception {

    final Questionnaire.QuestionnaireItemComponent questionItem =
        new Questionnaire.QuestionnaireItemComponent();

    if (hasMemberRel.getTo() != null) {
      final ConceptRef toConcept = hasMemberRel.getTo();

      // Set question properties
      questionItem.setLinkId(toConcept.getCode());
      questionItem.setText(toConcept.getName());
      questionItem.setRepeats(false);

      // Add coding
      final Coding coding = new Coding();
      coding.setSystem(terminology.getUri());
      coding.setCode(toConcept.getCode());
      coding.setDisplay(toConcept.getName());
      questionItem.addCode(coding);

      // Find answer options for this question
      final List<Questionnaire.QuestionnaireItemAnswerOptionComponent> answerOptions =
          findAnswerOptionsForQuestion(toConcept.getCode(), searchService, terminology);

      // Set type based on whether it has answer options
      if (answerOptions.isEmpty()) {
        questionItem.setType(Questionnaire.QuestionnaireItemType.DECIMAL);
      } else {
        questionItem.setType(Questionnaire.QuestionnaireItemType.CHOICE);
        // Add answer options
        for (final Questionnaire.QuestionnaireItemAnswerOptionComponent option : answerOptions) {
          questionItem.addAnswerOption(option);
        }
      }
    }

    return questionItem;
  }

  /**
   * Finds answer options for a question via has_answers relationships. Follows
   * LOINC structure: Question --has_answers--> LL Code <--parent-- LA Codes
   *
   * @param questionCode the question LOINC code
   * @param searchService the search service
   * @param terminology the terminology
   * @return list of answer option components
   * @throws Exception the exception
   */
  private static List<Questionnaire.QuestionnaireItemAnswerOptionComponent> findAnswerOptionsForQuestion(
    final String questionCode, final EntityRepositoryService searchService,
    final Terminology terminology) throws Exception {

    final List<Questionnaire.QuestionnaireItemAnswerOptionComponent> answerOptions =
        new ArrayList<>();
    final Set<String> uniqueAnswerCodes = new HashSet<>();

    try {
      // Find has_answers relationships from the question to get LL codes
      final String hasAnswersQuery = "from.code:" + StringUtility.escapeQuery(questionCode)
          + " AND additionalType:has_answers";
      final List<ConceptRelationship> hasAnswersRels =
          searchService.findAll(hasAnswersQuery, null, ConceptRelationship.class);

      for (final ConceptRelationship hasAnswersRel : hasAnswersRels) {
        final String llCode = hasAnswersRel.getTo().getCode();
        if (llCode != null) {
          // Find parent relationships from LA codes to the LL code
          final String parentQuery =
              "to.code:" + StringUtility.escapeQuery(llCode) + " AND type:\"Is a\"";
          final List<ConceptRelationship> parentRels =
              searchService.findAll(parentQuery, null, ConceptRelationship.class);

          for (final ConceptRelationship parentRel : parentRels) {
            final String laCode = parentRel.getFrom().getCode();
            if (laCode != null && laCode.startsWith("LA") && uniqueAnswerCodes.add(laCode)) {
              try {
                // Get the full Concept object to access attributes
                final Concept laConcept =
                    TerminologyUtility.getConcept(searchService, terminology.getAbbreviation(),
                        terminology.getPublisher(), terminology.getVersion(), laCode);

                if (laConcept != null) {
                  // Create answer option component
                  final Questionnaire.QuestionnaireItemAnswerOptionComponent option =
                      new Questionnaire.QuestionnaireItemAnswerOptionComponent();

                  // Set the value coding for the LA code
                  final Coding valueCoding = new Coding();
                  valueCoding.setSystem(terminology.getUri());
                  valueCoding.setCode(laCode);
                  valueCoding.setDisplay(laConcept.getName());
                  option.setValue(valueCoding);

                  answerOptions.add(option);
                }
              } catch (final Exception e) {
                // Log error but continue with other options
                final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
                logger.warn("Failed to get concept for LA code {}: {}", laCode, e.getMessage());
              }
            }
          }
        }
      }

    } catch (final Exception e) {
      final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
      logger.warn("Failed to find answer options for question {}: {}", questionCode,
          e.getMessage());
    }

    return answerOptions;
  }

  /**
   * Determines if a concept is an organizer concept based on its attributes.
   *
   * @param concept the concept to check
   * @return true if it's an organizer, false otherwise
   */
  private static boolean isOrganizerConcept(final Concept concept) {
    // Check for PanelType attribute = "Organizer"
    if (concept.getAttributes() != null) {
      final String panelType = concept.getAttributes().get("PanelType");
      if ("Organizer".equals(panelType)) {
        return true;
      }
    }

    // Fallback: check if name contains "organizer" (case insensitive)
    final String name = concept.getName();
    if (name != null && name.toLowerCase().contains("organizer")) {
      return true;
    }

    return false;
  }

  /**
   * Creates a direct question item from a has_member relationship.
   *
   * @param hasMemberRel the has_member relationship
   * @param searchService the search service
   * @param terminology the terminology
   * @param processedCodes set of already processed codes
   * @param latestVersion the latest version
   * @return the questionnaire item component
   * @throws Exception the exception
   */
  private static Questionnaire.QuestionnaireItemComponent createDirectQuestionItem(
    final ConceptRelationship hasMemberRel, final EntityRepositoryService searchService,
    final Terminology terminology, final Set<String> processedCodes, final String latestVersion)
    throws Exception {

    final Questionnaire.QuestionnaireItemComponent questionItem =
        new Questionnaire.QuestionnaireItemComponent();

    if (hasMemberRel.getTo() != null) {
      final ConceptRef toConcept = hasMemberRel.getTo();

      // Set question properties
      questionItem.setLinkId(toConcept.getCode());
      questionItem.setText(toConcept.getName());
      questionItem.setRepeats(false);

      // Add coding
      final Coding coding = new Coding();
      coding.setSystem(terminology.getUri());
      coding.setCode(toConcept.getCode());
      coding.setDisplay(toConcept.getName());
      questionItem.addCode(coding);

      // Find answer options for this question
      final List<Questionnaire.QuestionnaireItemAnswerOptionComponent> answerOptions =
          findAnswerOptionsForQuestion(toConcept.getCode(), searchService, terminology);

      // Set type based on whether it has answer options
      if (answerOptions.isEmpty()) {
        questionItem.setType(Questionnaire.QuestionnaireItemType.DECIMAL);
      } else {
        questionItem.setType(Questionnaire.QuestionnaireItemType.CHOICE);
        // Add answer options
        for (final Questionnaire.QuestionnaireItemAnswerOptionComponent option : answerOptions) {
          questionItem.addAnswerOption(option);
        }
      }
    }

    return questionItem;
  }

  /**
   * Gets the system URI for a terminology based on its abbreviation, publisher,
   * and version. This method uses TerminologyUtility to get the actual URI from
   * the database.
   *
   * @param searchService the search service
   * @param terminology the terminology abbreviation
   * @param publisher the publisher
   * @param version the version
   * @return the system URI
   */
  private static String getSystemUriFromTerminology(final EntityRepositoryService searchService,
    final String terminology, final String publisher, final String version) {

    try {
      final Terminology term =
          TerminologyUtility.getTerminology(searchService, terminology, publisher, version);
      if (term != null && term.getUri() != null) {
        return term.getUri();
      }
    } catch (final Exception e) {
      final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
      logger.warn("Failed to get terminology URI from database for {}: {}", terminology,
          e.getMessage());
    }

    return null;
  }

  /**
   * Determines if a concept should be included as a main question based on its
   * properties. Filters out variant concepts that are overly specific or
   * descriptive.
   *
   * @param conceptCode the LOINC concept code to check
   * @param searchService the search service to query concept properties
   * @return true if the concept should be included as a main question
   */
  @SuppressWarnings("unused")
  private static boolean isMainQuestionConcept(final String conceptCode,
    final EntityRepositoryService searchService) {
    try {
      // Get the concept to check its properties
      final Concept concept = searchService.get(conceptCode, Concept.class);
      if (concept == null || concept.getAttributes() == null) {
        return true; // Default to including if we can't determine
      }

      // Log what we're checking
      final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
      logger.info("Checking concept: {} - {}", conceptCode, concept.getName());

      final Map<String, String> attributes = concept.getAttributes();

      // Check for time-based variants (filter out)
      if (attributes.containsKey("TimeAspect")) {
        final String timeAspect = attributes.get("TimeAspect");
        if (timeAspect != null && (timeAspect.contains("1 month") || timeAspect.contains("3 months")
            || timeAspect.contains("Lifetime"))) {
          return false;
        }
      }

      // Check concept name for specific variant patterns only
      final String conceptName = concept.getName();
      if (conceptName != null) {
        final String lowerName = conceptName.toLowerCase();

        // Only filter out concepts that are clearly variants of the main
        // question
        // Look for patterns like "Most severe suicidal ideation 1 month" vs
        // "Most severe suicidal ideation"
        if (lowerName.contains("1 month") || lowerName.contains("3 months")
            || lowerName.contains("lifetime")) {
          // Check if this is a time variant of a main question
          // If the concept name without the time modifier exists, this is a
          // variant
          final String baseName = lowerName.replaceAll("\\s*1 month\\s*", "")
              .replaceAll("\\s*3 months\\s*", "").replaceAll("\\s*lifetime\\s*", "").trim();

          // If we can find a concept with the base name, this is a variant
          try {
            final String searchQuery = "name:" + StringUtility.escapeQuery(baseName);
            final List<Concept> baseConcepts =
                searchService.findAll(searchQuery, null, Concept.class);
            if (!baseConcepts.isEmpty()) {
              // Log what we're filtering out
              logger.info("Filtering out variant concept: {} (base: {})", conceptName, baseName);
              return false; // This is a variant, filter it out
            }
          } catch (final Exception searchEx) {
            // If search fails, default to including
            logger.debug("Failed to search for base concept: {}", searchEx.getMessage());
          }
        }
      }

      return true; // Include if no filtering criteria match

    } catch (final Exception e) {
      // Log error but default to including the concept
      final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
      logger.warn("Failed to check concept properties for {}: {}", conceptCode, e.getMessage());
      return true;
    }
  }

  /**
   * Creates a paging link with the specified offset and relation.
   *
   * @param uri the base uri
   * @param offset the offset parameter
   * @param offsetInt the current offset value
   * @param count the count parameter
   * @param countInt the count value
   * @param newOffset the new offset value to use
   * @param relation the link relation ("next" or "previous")
   * @return the bundle link component
   */
  private static BundleLinkComponent createPagingLink(final String uri, final NumberParam offset,
    final int offsetInt, final NumberParam count, final int countInt, final int newOffset,
    final String relation) {

    String newUri = uri;
    if (!uri.contains("?")) {
      newUri = newUri + "?";
    }

    if (offset != null) {
      newUri = newUri.replaceFirst("_offset=\\d+", "_offset=" + newOffset);
    } else {
      newUri += (newUri.endsWith("?") ? "" : "&") + "_offset=" + newOffset;
    }

    if (count != null) {
      newUri = newUri.replaceFirst("_count=\\d+", "_count=" + countInt);
    } else {
      newUri += (newUri.endsWith("?") ? "" : "&") + "_count=" + countInt;
    }

    return new BundleLinkComponent().setUrl(newUri).setRelation(relation);
  }

  /**
   * Creates a Coding object for a given code and terminology.
   *
   * @param code the code
   * @param terminology the terminology
   * @return the coding object
   */
  @SuppressWarnings("unused")
  private static Coding createCoding(final String code, final Terminology terminology) {
    final Coding coding = new Coding();
    coding.setSystem(terminology.getUri());
    coding.setCode(code);

    // Try to get the display name from the terminology
    try {
      // This is a simplified approach - in a real implementation you might want
      // to
      // look up the actual concept to get the proper display name
      coding.setDisplay(code);
    } catch (final Exception e) {
      // Fallback to using the code as display
      coding.setDisplay(code);
    }

    return coding;
  }
}
