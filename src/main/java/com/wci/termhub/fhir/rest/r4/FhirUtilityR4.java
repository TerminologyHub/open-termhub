/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

import com.wci.termhub.fhir.r4.AnswerOptionSequenceComparator;
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
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.DateUtility;
import com.wci.termhub.util.ModelUtility;
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
    }

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
    // Parse the full date string with timezone information
    final String releaseDate = subset.getReleaseDate();
    if (releaseDate != null && releaseDate.contains("T")) {
      // Full ISO 8601 date string with timezone
      valueSet.setDate(Date.from(Instant.parse(releaseDate)));
    } else {
      // Fallback to date-only format
      valueSet.setDate(DateUtility.DATE_YYYY_MM_DD_DASH.parse(releaseDate));
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
    }

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
    if (releaseDate != null && releaseDate.contains("T")) {
      // Full ISO 8601 date string with timezone
      cs.setDate(Date.from(Instant.parse(releaseDate)));
    } else {
      // Fallback to date-only format
      cs.setDate(DateUtility.DATE_YYYY_MM_DD_DASH.parse(releaseDate));
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
   * @throws Exception the exception
   */
  public static void populateQuestionnaire(final Questionnaire questionnaire,
    final EntityRepositoryService searchService) throws Exception {

    // Clear any existing items
    questionnaire.getItem().clear();

    // Extract LOINC code from questionnaire ID
    final String loincCode = questionnaire.getId();
    if (loincCode == null) {
      return;
    }

    try {
      // Get the concept for this questionnaire
      final Concept concept = getConceptFromQuestionnaireId(loincCode, searchService);
      if (concept == null) {
        return;
      }

      final Terminology terminology = TerminologyUtility.getTerminology(searchService,
          concept.getTerminology(), concept.getPublisher(), concept.getVersion());

      // Create items from has_member relationships
      final List<Questionnaire.QuestionnaireItemComponent> items = new ArrayList<>();
      for (final ConceptRelationship relationship : concept.getRelationships()) {
        if ("has_member".equals(relationship.getAdditionalType())) {
          // Skip if this is the panel concept itself (same code as
          // questionnaire)
          if (relationship.getTo() != null && relationship.getTo().getCode() != null
              && relationship.getTo().getCode().equals(loincCode)) {
            continue;
          }
          final Questionnaire.QuestionnaireItemComponent item =
              createQuestionnaireItemFromRelationship(relationship, searchService, terminology);
          if (item != null) {
            items.add(item);
          }
        }
      }

      // Sort items by their code.code field
      items.sort((item1, item2) -> {
        final String code1 = item1.getCode().isEmpty() ? "" : item1.getCode().get(0).getCode();
        final String code2 = item2.getCode().isEmpty() ? "" : item2.getCode().get(0).getCode();
        return code1.compareTo(code2);
      });

      // Add sorted items to questionnaire
      for (final Questionnaire.QuestionnaireItemComponent item : items) {
        questionnaire.addItem(item);
      }

    } catch (final Exception e) {
      // Log error but don't fail the entire questionnaire
      final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
      logger.error("Failed to populate questionnaire " + loincCode + " with questions", e);
    }
  }

  /**
   * Gets the concept for a questionnaire ID.
   *
   * @param loincCode the LOINC code
   * @param searchService the search service
   * @return the concept or null if not found
   * @throws Exception the exception
   */
  private static Concept getConceptFromQuestionnaireId(final String loincCode,
    final EntityRepositoryService searchService) throws Exception {

    return TerminologyUtility.getConcept(searchService, "LOINC", "Regenstrief Institute", "2.78",
        loincCode);
  }

  /**
   * Creates a questionnaire item from a concept relationship.
   *
   * @param relationship the relationship
   * @param searchService the search service
   * @param terminology the terminology
   * @return the questionnaire item component
   * @throws Exception the exception
   */
  private static Questionnaire.QuestionnaireItemComponent createQuestionnaireItemFromRelationship(
    final ConceptRelationship relationship, final EntityRepositoryService searchService,
    final Terminology terminology) throws Exception {

    final Questionnaire.QuestionnaireItemComponent item =
        new Questionnaire.QuestionnaireItemComponent();

    // TODO: What is linkId supposed to be?
    item.setLinkId(relationship.getId());

    // Set text from the relationship's "to" concept name
    if (relationship.getTo() != null) {
      item.setText(relationship.getTo().getName());

      // Add coding with the "to" concept's code and system
      final Coding coding = new Coding();
      coding.setSystem(terminology.getUri());
      coding.setCode(relationship.getTo().getCode());
      coding.setDisplay(relationship.getTo().getName());
      item.addCode(coding);

      // Find answer options for this question
      final List<Questionnaire.QuestionnaireItemAnswerOptionComponent> answerOptions =
          findAnswerOptionsForQuestion(relationship.getTo().getCode(), searchService, terminology);
      for (final Questionnaire.QuestionnaireItemAnswerOptionComponent option : answerOptions) {
        item.addAnswerOption(option);
      }
    }

    // TODO: determine logic. Not included in json import example
    if (item.getAnswerOption().isEmpty()) {
      item.setType(Questionnaire.QuestionnaireItemType.DECIMAL);
    } else {
      item.setType(Questionnaire.QuestionnaireItemType.CHOICE);
    }

    // TODO: determine logic. Not included in json import example
    item.setRepeats(false);
    return item;
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

    try {
      // Find has_answers relationships from the question to get LL codes
      final String hasAnswersQuery =
          "from.code:\"" + questionCode + "\" AND additionalType:\"has_answers\"";
      final List<ConceptRelationship> hasAnswersRels =
          searchService.findAll(hasAnswersQuery, null, ConceptRelationship.class);

      for (final ConceptRelationship hasAnswersRel : hasAnswersRels) {
        final String llCode = hasAnswersRel.getTo().getCode();
        if (llCode != null) {
          // Find parent relationships from LA codes to the LL code
          final String parentQuery = "to.code:\"" + llCode + "\" AND type:\"Is a\"";
          final List<ConceptRelationship> parentRels =
              searchService.findAll(parentQuery, null, ConceptRelationship.class);

          for (final ConceptRelationship parentRel : parentRels) {
            final String laCode = parentRel.getFrom().getCode();
            if (laCode != null && laCode.startsWith("LA")) {
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

                  // Store the concept for sorting by SequenceNumber
                  option.setUserData("concept", laConcept);
                  answerOptions.add(option);

                  // Log successful concept retrieval for debugging
                  final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
                  logger.debug("Successfully retrieved concept for LA code {}: {} (type: {})",
                      laCode, laConcept.getName(), laConcept.getClass().getSimpleName());
                } else {
                  final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
                  logger.warn("TerminologyUtility.getConcept returned null for LA code: {}",
                      laCode);
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

      // Sort answer options by their SequenceNumber attribute for proper LOINC
      // ordering
      AnswerOptionSequenceComparator.sortAnswerOptions(answerOptions);

      // Clean up stored concept data to avoid memory leaks
      for (final Questionnaire.QuestionnaireItemAnswerOptionComponent option : answerOptions) {
        option.setUserData("concept", null);
      }

    } catch (final Exception e) {
      // Log error but don't fail the entire questionnaire
      final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
      logger.warn("Failed to find answer options for question {}: {}", questionCode,
          e.getMessage());
    }

    return answerOptions;
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
      logger.warn("Failed to get terminology URI from database for {}: {}", terminology,
          e.getMessage());
    }

    return null;

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
}
