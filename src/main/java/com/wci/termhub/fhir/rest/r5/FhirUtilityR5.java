/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.ConceptMap;
import org.hl7.fhir.r5.model.ContactDetail;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.DateTimeType;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.Enumerations.CodeSystemContentMode;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.IntegerType;
import org.hl7.fhir.r5.model.Meta;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.OperationOutcome.IssueType;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.UriType;
import org.hl7.fhir.r5.model.ValueSet;
import org.hl7.fhir.r5.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r5.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r5.model.ValueSet.ValueSetComposeComponent;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionComponent;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionContainsComponent;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionParameterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.wci.termhub.fhir.util.CodeSystemMetadataProperty;
import com.wci.termhub.fhir.util.CodeSystemMetadataPropertyUtility;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.Definition;
import com.wci.termhub.model.Mapping;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.SubsetMember;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.DateUtility;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.TerminologyUtility;
import com.wci.termhub.util.ThreadLocalMapper;

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

  /** Meta tag system for LOINC LL/LG value set id (used by ValueSetProvider for expand/validate). */
  public static final String META_LOINC_LLLG_ID = "loincLllgId";

  /**
   * Uppercase LOINC property codes that duplicate lowercase {@code valueCoding} axes in the same
   * CodeSystem (legacy string row vs part code row).
   */
  private static final Set<String> LOINC_UPPERCASE_PROPERTY_KEYS =
      Set.of("CLASS", "COMPONENT", "METHOD_TYP", "PROPERTY", "SCALE_TYP", "SYSTEM", "TIME_ASPCT");

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
      // If no explicit version was requested, pick the latest terminology version
      // instead of failing.
      if (version == null || version.isEmpty()) {
        final Terminology latestTerminology = TerminologyUtility.getLatestTerminology(list);
        if (latestTerminology != null) {
          return latestTerminology;
        }
        return list.get(0);
      }
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
   * @param coding the codiHttpServletResponse.SC_BAD_REQUEST) * @return the
   *          string
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
    component.setDetails(new CodeableConcept().setText(message));
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
   * To R5.
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
    return toR5(codeSystem, concept, properties, displayMap, relationships, children, null);
  }

  /**
   * To R5.
   *
   * @param codeSystem the code system
   * @param concept the concept
   * @param properties the properties
   * @param displayMap the display map
   * @param relationships the relationships
   * @param children the children
   * @param conceptNameMap the concept name map
   * @return the parameters
   * @throws Exception the exception
   */
  public static Parameters toR5(final CodeSystem codeSystem, final Concept concept,
    final Set<String> properties, final Map<String, String> displayMap,
    final List<ConceptRelationship> relationships, final List<ConceptRef> children,
    final Map<String, String> conceptNameMap) throws Exception {
    return toR5(codeSystem, concept, properties, displayMap, relationships, children,
        conceptNameMap, null);
  }

  /**
   * To R5.
   *
   * @param codeSystem the code system
   * @param concept the concept
   * @param properties the properties
   * @param displayMap the display map
   * @param relationships the relationships
   * @param children the children
   * @param conceptNameMap the concept name map
   * @param searchService the search service
   * @return the parameters
   * @throws Exception the exception
   */
  public static Parameters toR5(final CodeSystem codeSystem, final Concept concept,
    final Set<String> properties, final Map<String, String> displayMap,
    final List<ConceptRelationship> relationships, final List<ConceptRef> children,
    final Map<String, String> conceptNameMap, final EntityRepositoryService searchService)
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

    final boolean isLoinc =
        codeSystem.getUrl() != null && codeSystem.getUrl().contains("loinc.org");

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
      if (value == null) {
        continue;
      }

      if (isLoinc && isLoincLookupInternalDisplayKey(key)) {
        continue;
      }
      if (isLoinc && isLoincLegacyStringSupersededByValueCoding(key, value, concept)) {
        continue;
      }

      // Check for boolean value
      if ("true".equals(value) || "false".equals(value)) {
        parameters.addParameter(
            createProperty(loincLookupPropertyName(key), Boolean.valueOf(value), false));
        continue;
      }

      if (isLoinc) {
        String codingCode = findLoincCodingCode(key, concept);
        if (codingCode == null && isLoincPartCode(value)
            && concept.getAttributes().containsKey(key + "_display")) {
          codingCode = value;
        }
        if (codingCode != null) {
          final Coding coding = new Coding();
          coding.setCode(codingCode);
          coding.setSystem(codeSystem.getUrl());
          coding.setDisplay(
              resolveLoincPropertyDisplay(key, value, codingCode, concept, displayMap));
          parameters.addParameter(createProperty(loincLookupPropertyName(key), coding, false));
        } else {
          parameters.addParameter(createProperty(loincLookupPropertyName(key), value, false));
        }
        continue;
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
   * Checks if is loinc part code.
   *
   * @param value the value
   * @return true, if is loinc part code
   */
  private static boolean isLoincPartCode(final String value) {
    return value != null && value.matches("^LP\\d+-\\d+$");
  }

  /**
   * True for attribute keys that only store display text for a {@code valueCoding} pair and must
   * not be emitted as their own {@code property} in $lookup.
   *
   * @param key the attribute key
   * @return true if internal display-only key
   */
  private static boolean isLoincLookupInternalDisplayKey(final String key) {
    return key != null && key.endsWith("_display");
  }

  /**
   * True when this attribute is the legacy uppercase string duplicate of a lowercase
   * {@code valueCoding} property (same axis: display text vs LP code).
   *
   * @param key the attribute key
   * @param value the attribute value
   * @param concept the concept
   * @return true if superseded by the canonical lowercase valueCoding attribute
   */
  private static boolean isLoincLegacyStringSupersededByValueCoding(final String key,
    final String value, final Concept concept) {
    if (key == null || value == null || !LOINC_UPPERCASE_PROPERTY_KEYS.contains(key)) {
      return false;
    }
    final String canonical = key.toLowerCase(Locale.ROOT);
    final String canonicalVal = concept.getAttributes().get(canonical);
    if (canonicalVal == null || !isLoincPartCode(canonicalVal)) {
      return false;
    }
    return !isLoincPartCode(value);
  }

  /**
   * Resolve loinc property display.
   *
   * @param key the key
   * @param value the value
   * @param codingCode the coding code
   * @param concept the concept
   * @param displayMap the display map
   * @return the string
   */
  private static String resolveLoincPropertyDisplay(final String key, final String value,
    final String codingCode, final Concept concept, final Map<String, String> displayMap) {
    final String fromAttr = concept.getAttributes().get(key + "_display");
    if (fromAttr != null) {
      return fromAttr;
    }
    if (value != null && !isLoincPartCode(value)) {
      return value;
    }
    if (codingCode != null && displayMap != null && displayMap.containsKey(codingCode)) {
      return displayMap.get(codingCode);
    }
    return codingCode != null ? codingCode : value;
  }

  /**
   * Loinc lookup property name.
   *
   * @param attributeKey the attribute key
   * @return the string
   */
  private static String loincLookupPropertyName(final String attributeKey) {
    if (attributeKey.length() > "category_".length() && attributeKey.startsWith("category_")) {
      final String suffix = attributeKey.substring("category_".length());
      if (suffix.matches("\\d+")) {
        return "category";
      }
    }
    if (attributeKey.length() > "search_".length() && attributeKey.startsWith("search_")) {
      final String suffix = attributeKey.substring("search_".length());
      if (suffix.matches("\\d+")) {
        return "search";
      }
    }
    return attributeKey;
  }

  /**
   * Find loinc coding code.
   *
   * @param propertyCode the property code
   * @param concept the concept
   * @return the string
   */
  private static String findLoincCodingCode(final String propertyCode, final Concept concept) {
    if (propertyCode.matches("category_\\d+") || propertyCode.matches("search_\\d+")) {
      return concept.getAttributes().get(propertyCode);
    }
    return null;
  }

  /**
   * To R5.
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
   * To R5 value set.
   *
   * @param terminology the terminology
   * @param metaFlag the meta flag
   * @return the value set
   * @throws Exception the exception
   */
  public static ValueSet toR5ValueSet(final Terminology terminology, final boolean metaFlag)
    throws Exception {

    final CodeSystem cs = FhirUtilityR5.toR5(terminology);
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
    set.getMeta().setVersionId("1");
    set.getMeta().setLastUpdated(DateUtility.parseToUtcDate(terminology.getCreated()));

    return set;
  }

  /**
   * Builds a minimal R5 ValueSet for a LOINC LL/LG value set (metadata only;
   * expansion is done in provider).
   *
   * @param terminology LOINC terminology
   * @param lllgId the LL or LG id (e.g. LL1162-8, LG51018-6-2.78)
   * @param metaFlag when true, add fromTerminology/fromPublisher/fromVersion
   *          and loincLllgId tags
   * @return the value set
   */
  public static ValueSet toR5LllgValueSet(final Terminology terminology, final String lllgId,
    final boolean metaFlag) {
    final ValueSet set = new ValueSet();
    set.setId(lllgId);
    set.setUrl(terminology.getUri() + "?fhir_vs=" + lllgId);
    set.setStatus(PublicationStatus.ACTIVE);
    if (terminology.getAttributes() != null
        && terminology.getAttributes().get("copyright") != null) {
      set.setCopyright(terminology.getAttributes().get("copyright"));
    }
    if (metaFlag) {
      set.setMeta(new Meta().addTag("fromTerminology", terminology.getAbbreviation(), null)
          .addTag("fromPublisher", terminology.getPublisher(), null)
          .addTag("fromVersion", terminology.getVersion(), null)
          .addTag("includesUri", terminology.getUri(), null)
          .addTag(META_LOINC_LLLG_ID, lllgId, null));
    } else {
      set.setMeta(new Meta().addTag(META_LOINC_LLLG_ID, lllgId, null));
    }
    if (set.getMeta() != null && terminology.getCreated() != null) {
      set.getMeta().setVersionId("1");
      set.getMeta().setLastUpdated(DateUtility.parseToUtcDate(terminology.getCreated()));
    }
    return set;
  }

  /**
   * Builds an R5 ValueSet for a LOINC LL/LG value set with compose but no
   * expansion (for GET ValueSet/{id} when expansion should not be included).
   *
   * @param terminology LOINC terminology
   * @param lllgId the LL or LG id (e.g. LL1162-8, LG51018-6-2.78)
   * @param members the concepts that are members of the value set
   * @return the value set with compose.include set, no expansion
   */
  public static ValueSet toR5LllgValueSetWithComposeOnly(final Terminology terminology,
    final String lllgId, final List<Concept> members) {
    final ValueSet set = toR5LllgValueSet(terminology, lllgId, false);
    final String systemUri = terminology.getUri();
    if (systemUri == null || members == null) {
      return set;
    }
    final ValueSetComposeComponent compose = new ValueSetComposeComponent();
    final ConceptSetComponent include = new ConceptSetComponent();
    include.setSystem(systemUri);
    for (final Concept c : members) {
      include
          .addConcept(new ConceptReferenceComponent().setCode(c.getCode()).setDisplay(c.getName()));
    }
    compose.addInclude(include);
    set.setCompose(compose);
    return set;
  }

  /**
   * Builds an R5 ValueSet for a LOINC LL/LG value set with compose and
   * expansion populated from the given members (for GET ValueSet/{id} to match
   * fhir.loinc.org behavior).
   *
   * @param terminology LOINC terminology
   * @param lllgId the LL or LG id (e.g. LL1162-8, LG51018-6-2.78)
   * @param members the concepts that are members of the value set
   * @return the value set with compose.include and expansion.contains set
   */
  public static ValueSet toR5LllgValueSetWithMembers(final Terminology terminology,
    final String lllgId, final List<Concept> members) {
    final ValueSet set = toR5LllgValueSet(terminology, lllgId, false);
    final String systemUri = terminology.getUri();
    if (systemUri == null || members == null) {
      return set;
    }
    final ValueSetComposeComponent compose = new ValueSetComposeComponent();
    final ConceptSetComponent include = new ConceptSetComponent();
    include.setSystem(systemUri);
    for (final Concept c : members) {
      include
          .addConcept(new ConceptReferenceComponent().setCode(c.getCode()).setDisplay(c.getName()));
    }
    compose.addInclude(include);
    set.setCompose(compose);
    final ValueSetExpansionComponent expansion = new ValueSetExpansionComponent();
    expansion.setId(UUID.randomUUID().toString());
    expansion.setTimestamp(new Date());
    expansion.setTotal(members.size());
    expansion.setOffset(0);
    expansion.addParameter(
        new ValueSetExpansionParameterComponent().setName("offset").setValue(new IntegerType(0)));
    expansion.addParameter(new ValueSetExpansionParameterComponent().setName("count")
        .setValue(new IntegerType(members.size())));
    for (final Concept c : members) {
      expansion.addContains(new ValueSetExpansionContainsComponent().setSystem(systemUri)
          .setCode(c.getCode()).setDisplay(c.getName()));
    }
    set.setExpansion(expansion);
    return set;
  }

  /**
   * To value set.
   *
   * @param subset the subset
   * @param members the members
   * @param metaFlag the meta flag
   * @return the value set
   * @throws Exception the exception
   */
  public static ValueSet toR5ValueSet(final Subset subset, final List<SubsetMember> members,
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
      valueSet.setDate(Date.from(java.time.Instant.parse(releaseDate)));
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
    valueSet.setStatus(PublicationStatus.ACTIVE);

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
    valueSet.getMeta().setVersionId("1");
    valueSet.getMeta().setLastUpdated(DateUtility.parseToUtcDate(subset.getCreated()));

    return valueSet;
  }

  /**
   * To R5 subsumes.
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
    parameters.addParameter("system", terminology.getUri());
    parameters.addParameter("version", terminology.getAttributes().get("fhirVersion"));
    return parameters;
  }

  /**
   * To R5.
   *
   * @param terminology the terminology
   * @return the coding
   * @throws Exception the exception
   */
  public static CodeSystem toR5(final Terminology terminology) throws Exception {
    final CodeSystem cs = new CodeSystem();

    cs.setUrl(terminology.getUri());

    // Parse the full date string with timezone information
    final String releaseDate = terminology.getReleaseDate();
    if (releaseDate != null && releaseDate.contains("T")) {
      // Full ISO 8601 date string with timezone
      cs.setDate(Date.from(java.time.Instant.parse(releaseDate)));
    } else {
      // Fallback to date-only format
      cs.setDate(DateUtility.DATE_YYYY_MM_DD_DASH.parse(releaseDate));
    }
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

    final String copyright = terminology.getAttributes().get("copyright");
    if (copyright != null) {
      cs.setCopyright(copyright);
    }

    final String description = terminology.getAttributes().get("description");
    if (description != null) {
      cs.setDescription(description);
    }

    final String fhirIdentifier = terminology.getAttributes().get("fhirIdentifier");
    if (fhirIdentifier != null && !fhirIdentifier.isEmpty()) {
      try {
        final JsonNode arr = ThreadLocalMapper.get().readTree(fhirIdentifier);
        if (arr.isArray()) {
          for (final JsonNode item : arr) {
            final Identifier identifier = new Identifier();
            if (!item.path("system").isMissingNode()) {
              identifier.setSystem(item.path("system").asText());
            }
            if (!item.path("value").isMissingNode()) {
              identifier.setValue(item.path("value").asText());
            }
            cs.addIdentifier(identifier);
          }
        }
      } catch (final Exception e) {
        LoggerFactory.getLogger(FhirUtilityR5.class).warn("Failed to parse fhirIdentifier", e);
      }
    }

    final String valueSet = terminology.getAttributes().get("valueSet");
    if (valueSet != null && !valueSet.isEmpty()) {
      cs.setValueSet(valueSet);
    }

    final String fhirContact = terminology.getAttributes().get("fhirContact");
    if (fhirContact != null && !fhirContact.isEmpty()) {
      try {
        final JsonNode arr = ThreadLocalMapper.get().readTree(fhirContact);
        if (arr.isArray()) {
          for (final JsonNode item : arr) {
            final ContactDetail contact = new ContactDetail();
            final JsonNode telecomArr = item.path("telecom");
            if (telecomArr.isArray()) {
              for (final JsonNode tp : telecomArr) {
                final ContactPoint cp = new ContactPoint();
                if (!tp.path("system").isMissingNode()) {
                  cp.setSystem(
                      ContactPoint.ContactPointSystem.fromCode(tp.path("system").asText()));
                }
                if (!tp.path("value").isMissingNode()) {
                  cp.setValue(tp.path("value").asText());
                }
                contact.addTelecom(cp);
              }
            }
            cs.addContact(contact);
          }
        }
      } catch (final Exception e) {
        LoggerFactory.getLogger(FhirUtilityR5.class).warn("Failed to parse fhirContact", e);
      }
    }

    final String caseSensitive = terminology.getAttributes().get("caseSensitive");
    if (caseSensitive != null) {
      cs.setCaseSensitive(Boolean.parseBoolean(caseSensitive));
    }

    final String versionNeeded = terminology.getAttributes().get("versionNeeded");
    if (versionNeeded != null) {
      cs.setVersionNeeded(Boolean.parseBoolean(versionNeeded));
    }

    // Meta: versionId for _history, lastUpdated from release date (UTC)
    final Meta csMeta = new Meta();
    csMeta.setVersionId("1");
    csMeta.setLastUpdated(DateUtility.parseToUtcDate(terminology.getCreated()));
    if (terminology.getAttributes().containsKey("originalId")) {
      csMeta.addTag("originalId", terminology.getAttributes().get("originalId"), null);
    }
    cs.setMeta(csMeta);

    return cs;
  }

  /**
   * To R5 with metadata-based properties.
   *
   * @param terminology the terminology
   * @param metadataList the metadata list
   * @return the code system
   * @throws Exception the exception
   */
  public static CodeSystem toR5(final Terminology terminology, final List<Metadata> metadataList)
    throws Exception {

    final CodeSystem cs = toR5(terminology);

    final List<CodeSystemMetadataProperty> properties =
        CodeSystemMetadataPropertyUtility.buildProperties(terminology, metadataList);
    for (final CodeSystemMetadataProperty property : properties) {
      final CodeSystem.PropertyComponent pc = cs.addProperty();
      pc.setCode(property.getCode());
      if (property.getDescription() != null) {
        pc.setDescription(property.getDescription());
      }
      if (property.getUri() != null) {
        pc.setUri(property.getUri());
      }
      if ("string".equals(property.getType())) {
        pc.setType(CodeSystem.PropertyType.STRING);
      } else if ("code".equals(property.getType())) {
        pc.setType(CodeSystem.PropertyType.CODE);
      }
    }

    return cs;
  }

  /**
   * To R5.
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

    // Debug logging for Mapset data
    // logger.info("Converting Mapset: id={}, fromTerminology={},
    // toTerminology={}", mapset.getId(),
    // mapset.getFromTerminology(), mapset.getToTerminology());

    // Set other fields
    cm.setUrl(mapset.getUri());
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
      cm.addIdentifier(new Identifier().setSystem("https://terminologyhub.com/model/mapset/code")
          .setValue(mapset.getCode()));
    }

    // Set source and target scopes from fromTerminology and toTerminology
    if (mapset.getAttributes().containsKey("fhirSourceUri")) {
      cm.setSourceScope(new UriType(mapset.getAttributes().get("fhirSourceUri") + "?fhir_vs"));
    }

    if (mapset.getAttributes().containsKey("fhirTargetUri")) {
      cm.setTargetScope(new UriType(mapset.getAttributes().get("fhirTargetUri") + "?fhir_vs"));
    }

    // Meta: versionId for _history, lastUpdated from release date (UTC)
    final Meta cmMeta = new Meta();
    cmMeta.setVersionId("1");
    cmMeta.setLastUpdated(DateUtility.parseToUtcDate(mapset.getCreated()));
    if (mapset.getAttributes().containsKey("originalId")) {
      cmMeta.addTag("originalId", mapset.getAttributes().get("originalId"), null);
    }
    cm.setMeta(cmMeta);

    return cm;
  }

  /**
   * To R5 with groups and elements from mappings.
   *
   * @param mapset the mapset
   * @param mappings the mappings (may be null or empty for metadata-only)
   * @return the concept map
   * @throws Exception the exception
   */
  public static ConceptMap toR5(final Mapset mapset, final List<Mapping> mappings)
    throws Exception {

    final ConceptMap cm = toR5(mapset);
    if (mappings == null || mappings.isEmpty()) {
      return cm;
    }

    final String sourceUri = mapset.getAttributes().containsKey("fhirSourceUri")
        ? mapset.getAttributes().get("fhirSourceUri") : null;
    final String targetUri = mapset.getAttributes().containsKey("fhirTargetUri")
        ? mapset.getAttributes().get("fhirTargetUri") : null;
    if (sourceUri == null || targetUri == null) {
      return cm;
    }

    final ConceptMap.ConceptMapGroupComponent group = cm.addGroup();
    group.setSource(sourceUri);
    group.setTarget(targetUri);

    final Map<String, List<Mapping>> bySourceCode = new HashMap<>();
    for (final Mapping m : mappings) {
      if (m.getFrom() != null && m.getFrom().getCode() != null) {
        bySourceCode.computeIfAbsent(m.getFrom().getCode(), k -> new ArrayList<>()).add(m);
      }
    }

    for (final Map.Entry<String, List<Mapping>> entry : bySourceCode.entrySet()) {
      final List<Mapping> elementMappings = entry.getValue();
      final Mapping first = elementMappings.get(0);
      final var element = group.addElement();
      element.setCode(first.getFrom().getCode());
      element.setDisplay(first.getFrom().getName() != null ? first.getFrom().getName()
          : first.getFrom().getCode());

      for (final Mapping m : elementMappings) {
        final var target = element.addTarget();
        if (m.getTo() != null && m.getTo().getCode() != null) {
          target.setCode(m.getTo().getCode());
        }
        target.setDisplay(m.getTo() != null && m.getTo().getName() != null ? m.getTo().getName()
            : "Unable to determine name");
        final String rel = mapRelationshipTo(m.getType());
        try {
          target.setRelationship(
              org.hl7.fhir.r5.model.Enumerations.ConceptMapRelationship.fromCode(rel));
        } catch (final Exception e) {
          target
              .setRelationship(org.hl7.fhir.r5.model.Enumerations.ConceptMapRelationship.RELATEDTO);
        }
      }
    }

    return cm;
  }

  /**
   * Map relationship to.
   *
   * @param type the type
   * @return the string
   */
  private static String mapRelationshipTo(final String type) {
    if (type == null) {
      return "related-to";
    }
    final String t = type.toLowerCase().replace("_", "-");
    switch (t) {
      case "equivalent":
      case "equal":
        return "equivalent";
      case "source-is-narrower-than-target":
      case "narrower":
      case "specializes":
        return "source-is-narrower-than-target";
      case "source-is-broader-than-target":
      case "broader":
      case "subsumes":
      case "wider":
        return "source-is-broader-than-target";
      case "not-related-to":
      case "disjoint":
      case "unmatched":
        return "not-related-to";
      default:
        return "related-to";
    }
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
