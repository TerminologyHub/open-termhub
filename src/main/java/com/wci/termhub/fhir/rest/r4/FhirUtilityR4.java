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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.ContactDetail;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.MetadataResource;
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
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionContainsComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionParameterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.wci.termhub.fhir.util.CodeSystemMetadataProperty;
import com.wci.termhub.fhir.util.CodeSystemMetadataPropertyUtility;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.fhir.util.FhirDateTimeUtil;
import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.fhir.util.LoincConceptPropertyHelper;
import com.wci.termhub.fhir.util.LoincConstants;
import com.wci.termhub.fhir.util.LoincQuestionnaireHelper;
import com.wci.termhub.fhir.util.LoincValueSetHelper.LllgComposeStructure;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptPropertyValueCoding;
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
import com.wci.termhub.util.FhirIdentifierUtil;
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
      // If no explicit version was requested, pick the latest terminology version
      // instead of failing.
      if (version == null || version.isEmpty()) {
        final Terminology latestTerminology = TerminologyUtility.getLatestTerminology(list);
        if (latestTerminology != null) {
          return latestTerminology;
        }
        return list.get(0);
      }
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

  /**
   * To R4.
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
  public static Parameters toR4(final CodeSystem codeSystem, final Concept concept,
    final Set<String> properties, final Map<String, String> displayMap,
    final List<ConceptRelationship> relationships, final List<ConceptRef> children,
    final Map<String, String> conceptNameMap) throws Exception {
    return toR4(codeSystem, concept, properties, displayMap, relationships, children,
        conceptNameMap, null);
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
   * @param conceptNameMap the concept name map
   * @param searchService the search service
   * @return the parameters
   * @throws Exception the exception
   */
  public static Parameters toR4(final CodeSystem codeSystem, final Concept concept,
    final Set<String> properties, final Map<String, String> displayMap,
    final List<ConceptRelationship> relationships, final List<ConceptRef> children,
    final Map<String, String> conceptNameMap, final EntityRepositoryService searchService)
    throws Exception {
    return toR4(codeSystem, concept, properties, displayMap, relationships, children,
        conceptNameMap, searchService, false);
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
   * @param conceptNameMap the concept name map
   * @param searchService the search service
   * @param regenstriefMode true when LOINC LL/LG value set mode is enabled
   * @return the parameters
   * @throws Exception the exception
   */
  @SuppressWarnings("null")
  public static Parameters toR4(final CodeSystem codeSystem, final Concept concept,
    final Set<String> properties, final Map<String, String> displayMap,
    final List<ConceptRelationship> relationships, final List<ConceptRef> children,
    final Map<String, String> conceptNameMap, final EntityRepositoryService searchService,
    final boolean regenstriefMode)
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

    final boolean isLoinc =
        codeSystem.getUrl() != null && codeSystem.getUrl().contains(LoincConstants.LOINC_URI);

    final List<ConceptRef> distinctHierarchicalParents =
        FhirUtility.distinctHierarchicalParents(relationships);

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

    if (isLoinc && !concept.getFhirPropertyCodings().isEmpty()) {
      for (final ConceptPropertyValueCoding entry : concept.getFhirPropertyCodings()) {
        final String propertyCode = entry.getPropertyCode();
        if (properties != null && !properties.contains(propertyCode)) {
          continue;
        }
        final String codingCode = entry.getValueCode();
        if (codingCode == null) {
          continue;
        }
        if (LoincConceptPropertyHelper.suppressRelationshipPropertyOnLookupOutput(propertyCode)) {
          continue;
        }
        String display =
            resolveLoincPropertyDisplay(propertyCode, codingCode, codingCode, concept, displayMap);
        if (entry.getValueDisplay() != null && !entry.getValueDisplay().isEmpty()) {
          display = entry.getValueDisplay();
        }
        final Coding coding = new Coding();
        coding.setCode(codingCode);
        coding.setSystem(codeSystem.getUrl());
        coding.setDisplay(display);
        parameters.addParameter(createProperty(propertyCode, coding, false));
      }
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
      if (LoincConceptPropertyHelper.suppressStatusOnLookupOutput(key, concept, regenstriefMode,
          isLoinc)) {
        continue;
      }

      // Handle boolean values
      if ("true".equals(value) || "false".equals(value)) {
        parameters.addParameter(
            createProperty(loincLookupPropertyName(key), Boolean.valueOf(value), false));
        continue;
      }

      // For LOINC: properties with valueCoding use valueCoding, others use
      // valueString
      if (isLoinc) {
        final String loincPropName = loincLookupPropertyName(key);
        if (LoincConceptPropertyHelper.suppressRelationshipPropertyOnLookupOutput(loincPropName)) {
          continue;
        }
        String codingCode = null;
        if (value != null && isLoincPartCode(value)
            && concept.getAttributes().containsKey(key + "_display")) {
          codingCode = value;
        }
        if (codingCode != null) {
          final Coding coding = new Coding();
          coding.setCode(codingCode);
          coding.setSystem(codeSystem.getUrl());
          coding
              .setDisplay(resolveLoincPropertyDisplay(key, value, codingCode, concept, displayMap));
          parameters.addParameter(createProperty(loincLookupPropertyName(key), coding, false));
        } else {
          final String propName = loincLookupPropertyName(key);
          if (LoincConceptPropertyHelper.isStatusValueCodeProperty(propName)) {
            parameters.addParameter(createProperty(propName, value, true));
          } else {
            parameters.addParameter(createProperty(propName, value, false));
          }
        }
        continue;
      }

      // For non-LOINC: try to convert to valueCoding if concept found
      final String conceptCode =
          findConceptCode(key, value, concept, conceptNameMap, searchService);
      if (conceptCode != null) {
        final Coding coding = new Coding();
        coding.setCode(conceptCode);
        coding.setSystem(codeSystem.getUrl());
        coding.setDisplay(value);
        parameters.addParameter(createProperty(key, coding, false));
      } else if (displayMap.containsKey(value)) {
        final Coding coding = new Coding();
        coding.setCode(value);
        coding.setSystem(codeSystem.getUrl());
        coding.setDisplay(displayMap.get(value));
        parameters.addParameter(createProperty(key, coding, false));
      } else if (LoincConceptPropertyHelper.isStatusValueCodeProperty(key)) {
        parameters.addParameter(createProperty(key, value, true));
      } else {
        parameters.addParameter(createProperty(key, value, false));
      }
    }

    // Legacy index: semanticType was stored only on semanticTypes, not attributes
    if (isLoinc && (properties == null || properties.contains("semanticType"))
        && !concept.getAttributes().containsKey("semanticType")) {
      for (final String semanticType : concept.getSemanticTypes()) {
        if (semanticType != null && !semanticType.isEmpty()) {
          parameters.addParameter(createProperty("semanticType", semanticType, false));
        }
      }
    }

    // Parents/Children
    if (properties == null || properties.contains("parent")) {
      for (final ConceptRef parent : distinctHierarchicalParents) {
        final Coding coding = new Coding();
        coding.setCode(parent.getCode());
        coding.setSystem(codeSystem.getUrl());
        coding.setDisplay(parent.getName());
        parameters.addParameter(createProperty("parent", coding, false));
      }
    }
    if (properties == null || properties.contains("child")) {
      final List<ConceptRef> childRefs = children == null ? List.of() : children;
      for (final ConceptRef child : childRefs) {
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
   * LOINC part / answer class codes (LP…).
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
    if (key == null || value == null
        || !LoincConstants.LOINC_UPPERCASE_PROPERTY_KEYS.contains(key)) {
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
   * Legacy {@code Map} keys used {@code _N} suffixes for duplicate FHIR
   * property codes. Strip that for the $lookup parameter name (indexed
   * documents only; reload uses
   * {@link com.wci.termhub.model.Concept#getFhirPropertyCodings()}).
   *
   * @param attributeKey the attribute key
   * @return FHIR property name
   */
  private static String loincLookupPropertyName(final String attributeKey) {
    if (attributeKey != null && attributeKey.matches(".+_\\d+")) {
      return attributeKey.replaceFirst("_\\d+$", "");
    }
    return attributeKey;
  }

  /**
   * Finds a concept code for a property value by searching conceptNameMap or
   * searchService.
   *
   * @param propertyCode the property code
   * @param propertyValue the property value
   * @param concept the concept
   * @param conceptNameMap the concept name map
   * @param searchService the search service
   * @return the concept code, or null if not found
   */
  private static String findConceptCode(final String propertyCode, final String propertyValue,
    final Concept concept, final Map<String, String> conceptNameMap,
    final EntityRepositoryService searchService) {
    if (conceptNameMap != null && conceptNameMap.containsKey(propertyValue)) {
      return conceptNameMap.get(propertyValue);
    }
    if (searchService == null || propertyValue == null || propertyValue.isEmpty()) {
      return null;
    }
    try {
      final SearchParameters nameParams = new SearchParameters(2, 0);
      nameParams.setQuery(StringUtility.composeQuery("AND", "active:true",
          "name:" + StringUtility.escapeQuery(propertyValue),
          "terminology:" + StringUtility.escapeQuery(concept.getTerminology()),
          concept.getVersion() != null
              ? "version:" + StringUtility.escapeQuery(concept.getVersion()) : null,
          concept.getPublisher() != null
              ? "publisher:" + StringUtility.escapeQuery(concept.getPublisher()) : null));
      final ResultList<Concept> nameResults =
          searchService.findFields(nameParams, ModelUtility.asList("code"), Concept.class);
      if (!nameResults.getItems().isEmpty()) {
        return nameResults.getItems().get(0).getCode();
      }
    } catch (final Exception e) {
      // Ignore search errors
    }
    return null;
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
    FhirDateTimeUtil.setR4DateTimeUtc(set.getDateElement(), terminology.getReleaseDate());
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
    FhirDateTimeUtil.setR4InstantUtc(set.getMeta().getLastUpdatedElement(),
        DateUtility.toFhirUtcInstantString(terminology.getCreated()));

    return set;
  }

  /**
   * Builds a minimal R4 ValueSet for a LOINC LL/LG value set (metadata only; expansion is done in
   * provider).
   *
   * @param terminology LOINC terminology
   * @param lllgId the LL or LG id (e.g. LL1162-8, LG51018-6-2.78)
   * @param valueSetId the FHIR resource id (Concept UUID)
   * @param metaFlag when true, add fromTerminology/fromPublisher/fromVersion tags
   * @return the value set
   * @throws Exception the exception
   */
  public static ValueSet toR4LllgValueSet(final Terminology terminology, final String lllgId,
    final String valueSetId, final boolean metaFlag) throws Exception {
    final ValueSet set = new ValueSet();
    if (valueSetId != null) {
      set.setId(valueSetId);
    }
    set.setUrl(terminology.getUri() + "?fhir_vs=" + lllgId);
    set.setVersion(terminology.getVersion());
    set.setPublisher(terminology.getPublisher());
    set.setStatus(PublicationStatus.ACTIVE);
    if (terminology.getAttributes() != null
        && terminology.getAttributes().get("copyright") != null) {
      set.setCopyright(terminology.getAttributes().get("copyright"));
    }
    final Meta meta = new Meta();
    meta.setVersionId("1");
    final String releaseDate = resolveTerminologyReleaseDateString(terminology);
    if (releaseDate != null) {
      FhirDateTimeUtil.setR4InstantUtc(meta.getLastUpdatedElement(), releaseDate);
    } else {
      FhirDateTimeUtil.setR4InstantUtc(meta.getLastUpdatedElement(),
          DateUtility.toFhirUtcInstantString(terminology.getCreated()));
    }
    if (terminology.getAttributes() != null
        && terminology.getAttributes().containsKey("originalId")) {
      meta.addTag("originalId", terminology.getAttributes().get("originalId"), null);
    }
    if (metaFlag) {
      meta.addTag("fromTerminology", terminology.getAbbreviation(), null)
          .addTag("fromPublisher", terminology.getPublisher(), null)
          .addTag("fromVersion", terminology.getVersion(), null)
          .addTag("includesUri", terminology.getUri(), null);
    }
    set.setMeta(meta);
    applyTerminologyContact(set, terminology);
    return set;
  }

  /**
   * Builds a minimal R4 ValueSet for a LOINC LL/LG concept found during enumeration. Sets name and
   * title from the concept's name in addition to the fields set by
   * {@link #toR4LllgValueSet(Terminology, String, String, boolean)}.
   *
   * @param terminology LOINC terminology
   * @param concept the LL or LG concept (code used as lllgId, name used as title/name)
   * @param metaFlag when true, add fromTerminology/fromPublisher/fromVersion tags
   * @return the value set
   * @throws Exception the exception
   */
  public static ValueSet toR4LllgValueSetFromConcept(final Terminology terminology,
    final Concept concept, final boolean metaFlag) throws Exception {
    final ValueSet set =
        toR4LllgValueSet(terminology, concept.getCode(), concept.getId(), metaFlag);
    if (concept.getName() != null) {
      set.setName(concept.getName());
    }
    return set;
  }

  /**
   * Returns true when the id search parameter matches a ValueSet id or its LL/LG code from url.
   *
   * @param idValue the _id search value
   * @param vs the value set
   * @return true if matches
   */
  public static boolean matchesLllgValueSetId(final String idValue, final ValueSet vs) {
    if (idValue == null || vs == null) {
      return idValue == null;
    }
    if (idValue.equals(vs.getId())) {
      return true;
    }
    final String lllgIdFromUrl = parseLllgIdFromValueSetUrl(vs.getUrl());
    return idValue.equals(lllgIdFromUrl);
  }

  /**
   * Parses the LOINC LL/LG id from a ValueSet url (e.g. http://loinc.org?fhir_vs=LG100-4).
   *
   * @param url the value set url
   * @return the LL/LG id, or null
   */
  public static String parseLllgIdFromValueSetUrl(final String url) {
    if (url == null) {
      return null;
    }
    final String marker = "fhir_vs=";
    final int idx = url.indexOf(marker);
    if (idx < 0) {
      return null;
    }
    String id = url.substring(idx + marker.length()).trim();
    final int amp = id.indexOf('&');
    if (amp >= 0) {
      id = id.substring(0, amp).trim();
    }
    return id.isEmpty() ? null : id;
  }

  /**
   * Applies LOINC LL/LG compose (nested value set refs and/or leaf concepts) to a ValueSet.
   *
   * @param set the value set
   * @param systemUri LOINC code system URI for leaf concepts
   * @param composeStructure partitioned compose structure
   */
  public static void setR4LllgCompose(final ValueSet set, final String systemUri,
    final LllgComposeStructure composeStructure) {
    if (set == null || systemUri == null || composeStructure == null) {
      return;
    }
    final ValueSetComposeComponent compose = new ValueSetComposeComponent();
    if (!composeStructure.getNestedValueSetUrls().isEmpty()) {
      final ConceptSetComponent nestedInclude = new ConceptSetComponent();
      for (final String url : composeStructure.getNestedValueSetUrls()) {
        nestedInclude.addValueSet(url);
      }
      compose.addInclude(nestedInclude);
    }
    if (!composeStructure.getLeafConcepts().isEmpty()) {
      final ConceptSetComponent leafInclude = new ConceptSetComponent();
      leafInclude.setSystem(systemUri);
      for (final Concept c : composeStructure.getLeafConcepts()) {
        leafInclude.addConcept(
            new ConceptReferenceComponent().setCode(c.getCode()).setDisplay(c.getName()));
      }
      compose.addInclude(leafInclude);
    }
    if (!compose.getInclude().isEmpty()) {
      set.setCompose(compose);
    }
  }

  /**
   * Builds an R4 ValueSet for a LOINC LL/LG value set with compose but no expansion (for GET
   * ValueSet/{id} when expansion should not be included).
   *
   * @param terminology LOINC terminology
   * @param lllgId the LL or LG id (e.g. LL1162-8, LG51018-6-2.78)
   * @param valueSetId the FHIR resource id (Concept UUID)
   * @param composeStructure partitioned compose structure from direct members
   * @return the value set with compose.include set, no expansion
   * @throws Exception the exception
   */
  public static ValueSet toR4LllgValueSetWithComposeOnly(final Terminology terminology,
    final String lllgId, final String valueSetId, final LllgComposeStructure composeStructure)
    throws Exception {
    final ValueSet set = toR4LllgValueSet(terminology, lllgId, valueSetId, false);
    setR4LllgCompose(set, terminology.getUri(), composeStructure);
    return set;
  }

  /**
   * Builds an R4 ValueSet for a LOINC LL/LG value set with compose and expansion populated from
   * leaf members (recursive expansion).
   *
   * @param terminology LOINC terminology
   * @param lllgId the LL or LG id (e.g. LL1162-8, LG51018-6-2.78)
   * @param valueSetId the FHIR resource id (Concept UUID)
   * @param composeStructure partitioned compose from direct members
   * @param leafMembers paginated leaf concepts for expansion.contains
   * @param expansionTotal total leaf count before pagination
   * @param expansionOffset expansion offset
   * @param expansionCount expansion count parameter
   * @return the value set with compose.include and expansion.contains set
   * @throws Exception the exception
   */
  public static ValueSet toR4LllgValueSetWithMembers(final Terminology terminology,
    final String lllgId, final String valueSetId, final LllgComposeStructure composeStructure,
    final List<Concept> leafMembers, final int expansionTotal, final int expansionOffset,
    final int expansionCount) throws Exception {
    final ValueSet set = toR4LllgValueSet(terminology, lllgId, valueSetId, false);
    final String systemUri = terminology.getUri();
    setR4LllgCompose(set, systemUri, composeStructure);
    if (systemUri == null || leafMembers == null) {
      return set;
    }
    final ValueSetExpansionComponent expansion = new ValueSetExpansionComponent();
    expansion.setIdentifier(UUID.randomUUID().toString());
    expansion.setTimestamp(new Date());
    expansion.setTotal(expansionTotal);
    expansion.setOffset(expansionOffset);
    expansion.addParameter(new ValueSetExpansionParameterComponent().setName("offset")
        .setValue(new IntegerType(expansionOffset)));
    expansion.addParameter(new ValueSetExpansionParameterComponent().setName("count")
        .setValue(new IntegerType(expansionCount)));
    for (final Concept c : leafMembers) {
      expansion.addContains(new ValueSetExpansionContainsComponent().setSystem(systemUri)
          .setCode(c.getCode()).setDisplay(c.getName()));
    }
    set.setExpansion(expansion);
    return set;
  }

  /**
   * Converts a Subset and its SubsetMembers to a FHIR R4 ValueSet.
   *
   * @param subset the Subset
   * @param members the SubsetMembers
   * @param metaFlag the meta flag
   * @param searchService the search service (used to resolve copyright from source terminology)
   * @return the FHIR R4 ValueSet
   * @throws Exception the exception
   */
  public static ValueSet toR4ValueSet(final Subset subset, final List<SubsetMember> members,
    final boolean metaFlag, final EntityRepositoryService searchService) throws Exception {

    final ValueSet valueSet = new ValueSet();
    valueSet.setId(subset.getId());
    valueSet.setUrl(subset.getUri());
    valueSet.setPublisher(subset.getPublisher());
    valueSet.setVersion(subset.getVersion());
    FhirDateTimeUtil.setR4DateTimeUtc(valueSet.getDateElement(), subset.getReleaseDate());

    valueSet.setName(subset.getName());
    valueSet.setDescription(subset.getDescription());
    valueSet.setStatus(Enumerations.PublicationStatus.ACTIVE);

    applyCopyrightFromTerminology(valueSet, subset, searchService);
    applySubsetContact(valueSet, subset);

    // Set experimental from attributes if present, else fallback
    final String experimentalStr = subset.getAttributes() != null
        ? subset.getAttributes().get(Subset.Attributes.fhirExperimental.name()) : null;
    if (experimentalStr != null) {
      valueSet.setExperimental(Boolean.parseBoolean(experimentalStr));
    }

    FhirIdentifierUtil.applyToR4ValueSet(valueSet,
        subset.getAttributes().get(Subset.Attributes.fhirIdentifier.name()));

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
    FhirDateTimeUtil.setR4InstantUtc(valueSet.getMeta().getLastUpdatedElement(),
        DateUtility.toFhirUtcInstantString(subset.getCreated()));

    return valueSet;
  }

  /**
   * Sets ValueSet copyright from source terminology when available, otherwise from subset
   * attributes (preserved at import).
   *
   * @param valueSet the value set
   * @param subset the subset
   * @param searchService the search service
   * @throws Exception the exception
   */
  /**
   * Adds contact from subset {@code fhirContact} JSON or publisher+uri fallback.
   *
   * @param valueSet the value set
   * @param subset the subset
   */
  private static void applySubsetContact(final ValueSet valueSet, final Subset subset) {
    if (valueSet == null || subset == null) {
      return;
    }
    final Map<String, String> attrs = subset.getAttributes();
    final String includesUri = attrs != null ? attrs.get("fhirIncludesUri") : null;
    final String fallbackUri = includesUri != null ? includesUri : subset.getUri();
    for (final ContactDetail contact : resolveContactsFromAttributes(subset.getPublisher(),
        subset.getUri(), attrs, null, fallbackUri)) {
      valueSet.addContact(contact);
    }
  }

  private static void applyCopyrightFromTerminology(final ValueSet valueSet, final Subset subset,
    final EntityRepositoryService searchService) throws Exception {

    String copyright = null;
    if (searchService != null && subset.getFromTerminology() != null) {
      final Terminology terminology = TerminologyUtility.getTerminology(searchService,
          subset.getFromTerminology(), subset.getFromPublisher(), subset.getFromVersion(), false);
      if (terminology != null && terminology.getAttributes() != null) {
        copyright = terminology.getAttributes().get("copyright");
      }
    }
    if (copyright == null && subset.getAttributes() != null) {
      copyright = subset.getAttributes().get("copyright");
    }
    if (copyright != null) {
      valueSet.setCopyright(copyright);
    }
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

    final String releaseDate = terminology.getReleaseDate();
    FhirDateTimeUtil.setR4DateTimeUtc(cs.getDateElement(), releaseDate);

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
        LoggerFactory.getLogger(FhirUtilityR4.class).warn("Failed to parse fhirIdentifier", e);
      }
    }

    final String valueSet = terminology.getAttributes().get("valueSet");
    if (valueSet != null && !valueSet.isEmpty()) {
      cs.setValueSet(valueSet);
    }

    applyTerminologyContact(cs, terminology);

    final String caseSensitive = terminology.getAttributes().get("caseSensitive");
    if (caseSensitive != null) {
      cs.setCaseSensitive(Boolean.parseBoolean(caseSensitive));
    }

    final String versionNeeded = terminology.getAttributes().get("versionNeeded");
    if (versionNeeded != null) {
      cs.setVersionNeeded(Boolean.parseBoolean(versionNeeded));
    }

    // Meta: versionId for _history; lastUpdated from release date when present, else created
    final Meta csMeta = new Meta();
    csMeta.setVersionId("1");
    if (releaseDate != null && !releaseDate.isEmpty()) {
      FhirDateTimeUtil.setR4InstantUtc(csMeta.getLastUpdatedElement(), releaseDate);
    } else {
      FhirDateTimeUtil.setR4InstantUtc(csMeta.getLastUpdatedElement(),
          DateUtility.toFhirUtcInstantString(terminology.getCreated()));
    }
    if (terminology.getAttributes().containsKey("originalId")) {
      csMeta.addTag("originalId", terminology.getAttributes().get("originalId"), null);
    }
    cs.setMeta(csMeta);

    return cs;
  }

  /**
   * To R4 with metadata-based properties.
   *
   * @param terminology the terminology
   * @param metadataList the metadata list
   * @return the code system
   * @throws Exception the exception
   */
  public static CodeSystem toR4(final Terminology terminology, final List<Metadata> metadataList)
    throws Exception {

    final CodeSystem cs = toR4(terminology);

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
      } else if ("Coding".equals(property.getType())) {
        pc.setType(CodeSystem.PropertyType.CODING);
      } else if ("boolean".equals(property.getType())) {
        pc.setType(CodeSystem.PropertyType.BOOLEAN);
      } else if ("integer".equals(property.getType())) {
        pc.setType(CodeSystem.PropertyType.INTEGER);
      } else if ("dateTime".equals(property.getType())) {
        pc.setType(CodeSystem.PropertyType.DATETIME);
      } else if ("decimal".equals(property.getType())) {
        pc.setType(CodeSystem.PropertyType.DECIMAL);
      }
    }

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
    FhirDateTimeUtil.setR4DateTimeUtc(cm.getDateElement(), mapset.getReleaseDate());
    cm.setVersion(mapset.getAttributes().containsKey("fhirVersion")
        ? mapset.getAttributes().get("fhirVersion") : mapset.getVersion());
    cm.setId(mapset.getId());
    cm.setName(mapset.getName());
    cm.setTitle(mapset.getAbbreviation());
    cm.setPublisher(mapset.getPublisher());
    cm.setStatus(Enumerations.PublicationStatus.ACTIVE);
    cm.setCopyright(mapset.getAttributes().get("copyright"));
    applyMapsetContact(cm, mapset);
    FhirIdentifierUtil.applyToR4ConceptMap(cm,
        mapset.getAttributes().get(FhirIdentifierUtil.ATTR_FHIR_IDENTIFIER));

    // Set source and target scopes from fromTerminology and toTerminology
    if (mapset.getAttributes().containsKey("fhirSourceUri")) {
      cm.setSource(new UriType(mapset.getAttributes().get("fhirSourceUri")));
    }

    if (mapset.getAttributes().containsKey("fhirTargetUri")) {
      cm.setTarget(new UriType(mapset.getAttributes().get("fhirTargetUri")));
    }

    // Meta: versionId for _history, lastUpdated from created (UTC)
    final Meta cmMeta = new Meta();
    cmMeta.setVersionId("1");
    FhirDateTimeUtil.setR4InstantUtc(cmMeta.getLastUpdatedElement(),
        DateUtility.toFhirUtcInstantString(mapset.getCreated()));
    if (mapset.getAttributes().containsKey("originalId")) {
      cmMeta.addTag("originalId", mapset.getAttributes().get("originalId"), null);
    }
    cm.setMeta(cmMeta);

    return cm;
  }

  /**
   * To R4 with groups and elements from mappings.
   *
   * @param mapset the mapset
   * @param mappings the mappings (may be null or empty for metadata-only)
   * @return the concept map
   * @throws Exception the exception
   */
  public static ConceptMap toR4(final Mapset mapset, final List<Mapping> mappings)
    throws Exception {

    final ConceptMap cm = toR4(mapset);
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
      final ConceptMap.SourceElementComponent element = group.addElement();
      element.setCode(first.getFrom().getCode());
      final String fromDisplay = first.getFrom().getName();
      if (!StringUtility.isEmpty(fromDisplay)) {
        element.setDisplay(fromDisplay);
      }

      for (final Mapping m : elementMappings) {
        final ConceptMap.TargetElementComponent target = element.addTarget();
        if (m.getTo() != null && m.getTo().getCode() != null) {
          target.setCode(m.getTo().getCode());
        }
        if (m.getTo() != null) {
          final String toDisplay = m.getTo().getName();
          if (!StringUtility.isEmpty(toDisplay)) {
            target.setDisplay(toDisplay);
          }
        }
        final String equiv =
            m.getType() != null ? m.getType().toLowerCase().replace("-", "") : "relatedto";
        try {
          target.setEquivalence(
              org.hl7.fhir.r4.model.Enumerations.ConceptMapEquivalence.fromCode(equiv));
        } catch (final Exception e) {
          target.setEquivalence(org.hl7.fhir.r4.model.Enumerations.ConceptMapEquivalence.RELATEDTO);
        }
      }
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
      final String baseUrl = request.getRequestURL().toString().replaceAll("/$", "");
      component.setFullUrl(baseUrl + "/" + list.get(i).getIdElement().getIdPart());
      bundle.addEntry(component);
    }

    return bundle;
  }

  /**
   * Adds contact from terminology {@code fhirContact} JSON or publisher+uri fallback.
   *
   * @param resource the FHIR metadata resource
   * @param terminology the terminology
   */
  private static void applyTerminologyContact(final MetadataResource resource,
    final Terminology terminology) {
    applyTerminologyContact(resource, terminology, null, null);
  }

  /**
   * Adds contact from terminology {@code fhirContact} JSON or publisher+uri fallback.
   *
   * @param questionnaire the questionnaire
   * @param terminology the terminology (optional)
   * @param fallbackName contact name when terminology or fhirContact name is absent
   * @param fallbackUri contact url when terminology uri is absent
   */
  private static void applyTerminologyContact(final Questionnaire questionnaire,
    final Terminology terminology, final String fallbackName, final String fallbackUri) {
    if (questionnaire == null) {
      return;
    }
    for (final ContactDetail contact : resolveTerminologyContacts(terminology, fallbackName,
        fallbackUri)) {
      questionnaire.addContact(contact);
    }
  }

  /**
   * Adds contact from terminology {@code fhirContact} JSON or publisher+uri fallback.
   *
   * @param resource the FHIR metadata resource
   * @param terminology the terminology (optional)
   * @param fallbackName contact name when terminology or fhirContact name is absent
   * @param fallbackUri contact url when terminology uri is absent
   */
  private static void applyTerminologyContact(final MetadataResource resource,
    final Terminology terminology, final String fallbackName, final String fallbackUri) {
    if (resource == null) {
      return;
    }
    for (final ContactDetail contact : resolveTerminologyContacts(terminology, fallbackName,
        fallbackUri)) {
      resource.addContact(contact);
    }
  }

  /**
   * Adds contact from mapset {@code fhirContact} when stored at import.
   *
   * @param resource the FHIR metadata resource
   * @param mapset the mapset
   */
  private static void applyMapsetContact(final MetadataResource resource, final Mapset mapset) {
    if (resource == null || mapset == null) {
      return;
    }
    for (final ContactDetail contact : resolveContactsFromAttributes(mapset.getPublisher(),
        mapset.getUri(), mapset.getAttributes(), null, null, false)) {
      resource.addContact(contact);
    }
  }

  /**
   * Builds contact details from terminology {@code fhirContact} JSON or publisher+uri fallback.
   *
   * @param terminology the terminology (optional)
   * @param fallbackName contact name when terminology or fhirContact name is absent
   * @param fallbackUri contact url when terminology uri is absent
   * @return contact details to add to a FHIR resource
   */
  private static List<ContactDetail> resolveTerminologyContacts(final Terminology terminology,
    final String fallbackName, final String fallbackUri) {
    final String publisher = terminology != null ? terminology.getPublisher() : null;
    final String uri = terminology != null ? terminology.getUri() : null;
    final Map<String, String> attrs =
        terminology != null ? terminology.getAttributes() : null;
    return resolveContactsFromAttributes(publisher, uri, attrs, fallbackName, fallbackUri, true);
  }

  /**
   * Builds contact details from {@code fhirContact} JSON or publisher+uri fallback.
   *
   * @param publisher the publisher
   * @param uri the resource uri
   * @param attrs attribute map that may contain {@code fhirContact}
   * @param fallbackName contact name when publisher or fhirContact name is absent
   * @param fallbackUri contact url when uri is absent
   * @return contact details to add to a FHIR resource
   */
  private static List<ContactDetail> resolveContactsFromAttributes(final String publisher,
    final String uri, final Map<String, String> attrs, final String fallbackName,
    final String fallbackUri) {
    return resolveContactsFromAttributes(publisher, uri, attrs, fallbackName, fallbackUri, true);
  }

  /**
   * Builds contact details from {@code fhirContact} JSON, optionally with publisher+uri fallback.
   *
   * @param publisher the publisher
   * @param uri the resource uri
   * @param attrs attribute map that may contain {@code fhirContact}
   * @param fallbackName contact name when publisher or fhirContact name is absent
   * @param fallbackUri contact url when uri is absent
   * @param allowPublisherFallback when false, returns only stored {@code fhirContact}
   * @return contact details to add to a FHIR resource
   */
  private static List<ContactDetail> resolveContactsFromAttributes(final String publisher,
    final String uri, final Map<String, String> attrs, final String fallbackName,
    final String fallbackUri, final boolean allowPublisherFallback) {
    final List<ContactDetail> contacts = new ArrayList<>();
    final String fhirContact = attrs != null ? attrs.get("fhirContact") : null;
    if (fhirContact != null && !fhirContact.isEmpty()) {
      try {
        final JsonNode arr = ThreadLocalMapper.get().readTree(fhirContact);
        if (arr.isArray()) {
          for (final JsonNode item : arr) {
            final ContactDetail contact = new ContactDetail();
            if (!item.path("name").isMissingNode() && !item.path("name").asText().isEmpty()) {
              contact.setName(item.path("name").asText());
            } else if (publisher != null) {
              contact.setName(publisher);
            }
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
            contacts.add(contact);
          }
          return contacts;
        }
      } catch (final Exception e) {
        LoggerFactory.getLogger(FhirUtilityR4.class).warn("Failed to parse fhirContact", e);
      }
    }
    if (!allowPublisherFallback) {
      return contacts;
    }
    final String contactName = publisher != null ? publisher : fallbackName;
    final String contactUri = uri != null ? uri : fallbackUri;
    if (contactName == null) {
      return contacts;
    }
    final ContactDetail contact = new ContactDetail();
    contact.setName(contactName);
    if (contactUri != null) {
      final ContactPoint telecom = new ContactPoint();
      telecom.setSystem(ContactPoint.ContactPointSystem.URL);
      telecom.setValue(contactUri);
      contact.addTelecom(telecom);
    }
    contacts.add(contact);
    return contacts;
  }

  /**
   * Parses terminology release date for resource meta (same rules as CodeSystem).
   *
   * @param terminology the terminology
   * @return release date or null
   * @throws Exception the exception
   */
  private static String resolveTerminologyReleaseDateString(final Terminology terminology) {
    if (terminology == null) {
      return null;
    }
    return DateUtility.toFhirUtcDateTimeString(terminology.getReleaseDate());
  }

  /**
   * Builds Questionnaire meta consistent with CodeSystem: versionId, lastUpdated, optional
   * originalId tag.
   *
   * @param terminology the terminology (optional)
   * @param concept the concept (optional)
   * @return the meta
   * @throws Exception the exception
   */
  private static Meta buildQuestionnaireMeta(final Terminology terminology, final Concept concept)
    throws Exception {
    final Meta meta = new Meta();
    meta.setVersionId("1");
    String lastUpdated = resolveTerminologyReleaseDateString(terminology);
    if (lastUpdated == null && terminology != null && terminology.getCreated() != null) {
      lastUpdated = DateUtility.toFhirUtcInstantString(terminology.getCreated());
    }
    if (lastUpdated == null && concept != null) {
      if (concept.getModified() != null) {
        lastUpdated = DateUtility.toFhirUtcInstantString(concept.getModified());
      } else if (concept.getCreated() != null) {
        lastUpdated = DateUtility.toFhirUtcInstantString(concept.getCreated());
      }
    }
    if (lastUpdated != null) {
      FhirDateTimeUtil.setR4InstantUtc(meta.getLastUpdatedElement(), lastUpdated);
    }
    if (concept != null && concept.getAttributes() != null
        && concept.getAttributes().containsKey("originalId")) {
      meta.addTag("originalId", concept.getAttributes().get("originalId"), null);
    } else if (terminology != null && terminology.getAttributes() != null
        && terminology.getAttributes().containsKey("originalId")) {
      meta.addTag("originalId", terminology.getAttributes().get("originalId"), null);
    }
    return meta;
  }

  /**
   * Resolves terminology copyright (same source as CodeSystem).
   *
   * @param terminology the terminology
   * @param searchService the search service (optional, reloads full record by id)
   * @return copyright text or null
   * @throws Exception the exception
   */
  private static String resolveTerminologyCopyright(final Terminology terminology,
    final EntityRepositoryService searchService) throws Exception {
    if (terminology == null) {
      return null;
    }
    Terminology source = terminology;
    if (searchService != null && terminology.getId() != null) {
      final Terminology full = searchService.get(terminology.getId(), Terminology.class);
      if (full != null) {
        source = full;
      }
    }
    final String copyright = source.getAttributes().get("copyright");
    return StringUtility.isEmpty(copyright) ? null : copyright;
  }

  /**
   * Resolves questionnaire copyright from CodeSystem copyright plus external
   * notices on member codes.
   *
   * @param questionnaire the questionnaire
   * @param terminology the terminology
   * @param searchService the search service
   * @return copyright text or null
   * @throws Exception the exception
   */
  private static String resolveQuestionnaireCopyright(final Questionnaire questionnaire,
    final Terminology terminology, final EntityRepositoryService searchService) throws Exception {
    final String baseCopyright = resolveTerminologyCopyright(terminology, searchService);
    if (questionnaire == null || terminology == null || searchService == null) {
      return baseCopyright;
    }
    return LoincQuestionnaireHelper.buildQuestionnaireCopyright(baseCopyright,
        collectLoincCodesFromQuestionnaire(questionnaire), searchService,
        terminology.getAbbreviation(), terminology.getPublisher(), terminology.getVersion());
  }

  /**
   * Collects LOINC codes referenced by a questionnaire (root code, items,
   * answer options).
   *
   * @param questionnaire the questionnaire
   * @return codes in depth-first order
   */
  private static Set<String> collectLoincCodesFromQuestionnaire(final Questionnaire questionnaire) {
    final Set<String> codes = new LinkedHashSet<>();
    if (questionnaire == null) {
      return codes;
    }
    for (final Coding coding : questionnaire.getCode()) {
      if (coding.hasCode()) {
        codes.add(coding.getCode());
      }
    }
    for (final Questionnaire.QuestionnaireItemComponent item : questionnaire.getItem()) {
      collectLoincCodesFromItem(item, codes);
    }
    return codes;
  }

  /**
   * Adds LOINC codes from a questionnaire item subtree.
   *
   * @param item the item
   * @param codes the collector
   */
  private static void collectLoincCodesFromItem(final Questionnaire.QuestionnaireItemComponent item,
    final Set<String> codes) {
    if (item == null) {
      return;
    }
    for (final Coding coding : item.getCode()) {
      if (coding.hasCode()) {
        codes.add(coding.getCode());
      }
    }
    for (final Questionnaire.QuestionnaireItemAnswerOptionComponent option : item
        .getAnswerOption()) {
      if (option.getValue() instanceof Coding) {
        final Coding valueCoding = (Coding) option.getValue();
        if (valueCoding.hasCode()) {
          codes.add(valueCoding.getCode());
        }
      }
    }
    for (final Questionnaire.QuestionnaireItemComponent child : item.getItem()) {
      collectLoincCodesFromItem(child, codes);
    }
  }

  /**
   * Sets Questionnaire copyright from terminology attributes when available.
   *
   * @param questionnaire the questionnaire
   * @param terminology the terminology
   * @param searchService the search service (optional)
   * @throws Exception the exception
   */
  private static void applyQuestionnaireCopyright(final Questionnaire questionnaire,
    final Terminology terminology, final EntityRepositoryService searchService) throws Exception {
    final String copyright =
        resolveQuestionnaireCopyright(questionnaire, terminology, searchService);
    if (copyright != null) {
      questionnaire.setCopyright(copyright);
    }
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
    FhirDateTimeUtil.setR4DateTimeUtc(questionnaire.getDateElement(),
        resolveTerminologyReleaseDateString(terminology));
    questionnaire.setPublisher(terminology.getPublisher());
    applyQuestionnaireCopyright(questionnaire, terminology, null);

    final Meta meta = buildQuestionnaireMeta(terminology, null);
    if (metaFlag) {
      meta.addTag("fromTerminology", terminology.getAbbreviation(), null)
          .addTag("fromPublisher", terminology.getPublisher(), null)
          .addTag("fromVersion", terminology.getVersion(), null)
          .addTag("includesUri", terminology.getUri(), null);
    }
    questionnaire.setMeta(meta);

    return questionnaire;
  }

  /**
   * Converts a LOINC Concept to a FHIR R4 Questionnaire. This is the primary method for creating
   * questionnaires from LOINC concepts.
   *
   * @param concept the LOINC Concept
   * @param searchService the search service
   * @return the FHIR R4 Questionnaire
   * @throws Exception the exception
   */
  public static Questionnaire toR4Questionnaire(final Concept concept,
    final EntityRepositoryService searchService) throws Exception {
    return toR4Questionnaire(concept, searchService, null);
  }

  /**
   * Converts a LOINC Concept to a FHIR R4 Questionnaire. This is the primary method for creating
   * questionnaires from LOINC concepts.
   *
   * @param concept the LOINC Concept
   * @param searchService the search service
   * @param terminology the LOINC terminology (for meta.lastUpdated)
   * @return the FHIR R4 Questionnaire
   * @throws Exception the exception
   */
  public static Questionnaire toR4Questionnaire(final Concept concept,
    final EntityRepositoryService searchService, final Terminology terminology) throws Exception {

    final Questionnaire questionnaire = new Questionnaire();

    // Set basic metadata
    questionnaire.setId(concept.getCode());

    // Build URL from terminology information
    final String systemUri = getSystemUriFromTerminology(searchService, concept.getTerminology(),
        concept.getPublisher(), concept.getVersion());
    questionnaire.setUrl(systemUri + "/q/" + concept.getCode());

    final String shortCommonName = resolveLoincShortCommonName(concept);
    final String title =
        !StringUtility.isEmpty(shortCommonName) ? shortCommonName : concept.getName();
    final String name = !StringUtility.isEmpty(shortCommonName) ? toQuestionnaireName(shortCommonName)
        : concept.getName();
    questionnaire.setName(name);
    questionnaire.setTitle(title);
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
    coding.setDisplay(title);
    questionnaire.addCode(coding);

    applyTerminologyContact(questionnaire, terminology, concept.getPublisher(), systemUri);

    Terminology copyrightTerminology = terminology;
    if (copyrightTerminology == null && searchService != null) {
      copyrightTerminology = TerminologyUtility.getTerminology(searchService,
          concept.getTerminology(), concept.getPublisher(), concept.getVersion());
    }
    applyQuestionnaireCopyright(questionnaire, copyrightTerminology, searchService);

    questionnaire.setMeta(buildQuestionnaireMeta(terminology, concept));

    return questionnaire;
  }

  /**
   * Populates a Questionnaire with questions and answers based on LOINC relationships. This method
   * uses the concept's existing relationships to create questionnaire items.
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

      // Track processed form linkIds to prevent duplicates at any level
      final Set<String> processedLinkIds = new HashSet<>();

      // Get the main concept to find its relationships
      final Concept mainConcept =
          TerminologyUtility.getConcept(searchService, terminology.getAbbreviation(),
              terminology.getPublisher(), terminology.getVersion(), loincCode);
      if (mainConcept == null) {
        return;
      }

      // Find panel members via has_member or hierarchical parent relationships
      final List<Questionnaire.QuestionnaireItemComponent> groupItems = findGroupConcepts(
          mainConcept, searchService, terminology, processedLinkIds, terminology.getVersion());

      // Add group items to questionnaire
      for (final Questionnaire.QuestionnaireItemComponent groupItem : groupItems) {
        questionnaire.addItem(groupItem);
      }

      applyQuestionnaireCopyright(questionnaire, terminology, searchService);

    } catch (final Exception e) {
      // Log error but don't fail the entire questionnaire
      final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
      logger.warn("Failed to populate questionnaire {}: {}", loincCode, e.getMessage());
    }
  }

  /** LOINC scale type attribute keys on indexed concepts. */
  private static final String ATTR_SCALE_TYP = "SCALE_TYP";

  /** Alternate LOINC scale type attribute key. */
  private static final String ATTR_LOINC_SCALE_TYP = "LOINC_SCALE_TYP";

  /**
   * Finds panel member relationships for a questionnaire/panel code. Prefers outbound
   * {@code member} edges (full LOINC), then {@code has_member} (sandbox); falls back to
   * inbound hierarchical {@code parent} edges (child {@code from} → panel {@code to}).
   *
   * @param panelCode the panel or questionnaire code
   * @param searchService the search service
   * @param terminology the LOINC terminology
   * @return sorted member relationships (may be empty)
   * @throws Exception the exception
   */
  private static List<ConceptRelationship> findPanelMemberRelationships(final String panelCode,
    final EntityRepositoryService searchService, final Terminology terminology) throws Exception {

    final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
        terminology.getPublisher(), terminology.getVersion());

    final String memberQuery = StringUtility.composeQuery("AND", termQuery,
        StringUtility.escapeKeywordField("from.code", panelCode),
        StringUtility.escapeKeywordField("additionalType", LoincConstants.LOINC_REL_PANEL_MEMBER));
    final List<ConceptRelationship> memberRels =
        searchService.findAll(memberQuery, null, ConceptRelationship.class);
    if (!memberRels.isEmpty()) {
      return sortMemberRelationships(filterPanelMemberRelationships(panelCode, memberRels));
    }

    final String hasMemberQuery = StringUtility.composeQuery("AND", termQuery,
        StringUtility.escapeKeywordField("from.code", panelCode),
        StringUtility.escapeKeywordField("additionalType", LoincConstants.LOINC_REL_HAS_MEMBER));
    final List<ConceptRelationship> hasMemberRels =
        searchService.findAll(hasMemberQuery, null, ConceptRelationship.class);
    if (!hasMemberRels.isEmpty()) {
      return sortMemberRelationships(filterPanelMemberRelationships(panelCode, hasMemberRels));
    }

    final String childQuery = StringUtility.composeQuery("AND", termQuery,
        StringUtility.escapeKeywordField("to.code", panelCode), "hierarchical:true", "active:true");
    final List<ConceptRelationship> childRels =
        searchService.findAll(childQuery, null, ConceptRelationship.class);

    final List<ConceptRelationship> filtered = new ArrayList<>();
    for (final ConceptRelationship rel : childRels) {
      if (rel.getHierarchical() == null || !rel.getHierarchical()) {
        continue;
      }
      final ConceptRef memberRef = rel.getFrom();
      if (memberRef == null || memberRef.getCode() == null
          || panelCode.equals(memberRef.getCode())) {
        continue;
      }
      filtered.add(rel);
    }
    if (!filtered.isEmpty()) {
      return sortMemberRelationships(filterPanelMemberRelationships(panelCode, filtered));
    }

    final String parentClause = "parents.code:" + StringUtility.escapeQuery(panelCode);
    final String excludePanel = "-code:" + StringUtility.escapeQuery(panelCode);
    final String conceptQuery =
        StringUtility.composeQuery("AND", termQuery, parentClause, excludePanel);
    final SearchParameters params = new SearchParameters(conceptQuery, 500, 0);
    final ResultList<Concept> childConcepts = searchService.find(params, Concept.class);

    final List<ConceptRelationship> fromParents = new ArrayList<>();
    for (final Concept child : childConcepts.getItems()) {
      if (child.getCode() == null || panelCode.equals(child.getCode())) {
        continue;
      }
      final ConceptRelationship rel = new ConceptRelationship();
      rel.setHierarchical(true);
      rel.setAdditionalType("parent");
      rel.setFrom(new ConceptRef(child.getCode(), child.getName()));
      rel.setTo(new ConceptRef(panelCode, null));
      fromParents.add(rel);
    }
    return sortMemberRelationships(filterPanelMemberRelationships(panelCode, fromParents));
  }

  /**
   * Drops self-references and LOINC part (LP*) targets from panel member
   * relationships.
   *
   * @param panelCode the panel code
   * @param relationships the candidate relationships
   * @return filtered relationships
   */
  private static List<ConceptRelationship> filterPanelMemberRelationships(final String panelCode,
    final List<ConceptRelationship> relationships) {
    if (relationships == null || relationships.isEmpty()) {
      return List.of();
    }
    final List<ConceptRelationship> filtered = new ArrayList<>();
    for (final ConceptRelationship rel : relationships) {
      final ConceptRef memberRef = getMemberConceptRef(rel);
      if (memberRef == null || memberRef.getCode() == null) {
        continue;
      }
      final String memberCode = memberRef.getCode();
      if (panelCode.equals(memberCode) || memberCode.startsWith("LP")) {
        continue;
      }
      filtered.add(rel);
    }
    return filtered;
  }

  /**
   * Returns the member concept reference for a panel membership relationship.
   *
   * @param rel the relationship
   * @return the member concept ref, or null
   */
  private static ConceptRef getMemberConceptRef(final ConceptRelationship rel) {
    if (rel == null) {
      return null;
    }
    final String additionalType = rel.getAdditionalType();
    if (LoincConstants.LOINC_REL_HAS_MEMBER.equals(additionalType)
        || LoincConstants.LOINC_REL_PANEL_MEMBER.equals(additionalType)) {
      return rel.getTo();
    }
    if (Boolean.TRUE.equals(rel.getHierarchical())) {
      return rel.getFrom();
    }
    return rel.getTo();
  }

  /**
   * Sorts panel member relationships by sequence metadata when present.
   *
   * @param relationships the relationships
   * @return sorted list
   */
  private static List<ConceptRelationship> sortMemberRelationships(
    final List<ConceptRelationship> relationships) {
    if (relationships == null || relationships.size() <= 1) {
      return relationships == null ? List.of() : relationships;
    }
    final List<ConceptRelationship> sorted = new ArrayList<>(relationships);
    sorted.sort(
        Comparator.comparingInt(FhirUtilityR4::relationshipSequenceNumber).thenComparing(rel -> {
          final ConceptRef memberRef = getMemberConceptRef(rel);
          return memberRef == null || memberRef.getCode() == null ? "" : memberRef.getCode();
        }));
    return sorted;
  }

  /**
   * Sequence number for a panel member relationship.
   *
   * @param rel the relationship
   * @return sequence number or max value if unknown
   */
  private static int relationshipSequenceNumber(final ConceptRelationship rel) {
    final int seq = LoincQuestionnaireHelper.relationshipSequenceNumber(rel);
    if (seq < Integer.MAX_VALUE) {
      return seq;
    }
    final ConceptRef memberRef = getMemberConceptRef(rel);
    if (memberRef != null && memberRef.getCode() != null) {
      return Integer.MAX_VALUE - 1;
    }
    return Integer.MAX_VALUE;
  }

  /**
   * Resolves questionnaire item text from member-edge metadata, then concept
   * display.
   *
   * @param memberRel the member relationship
   * @param memberConcept the member concept
   * @param memberRef the member concept ref
   * @return display text
   */
  private static String resolveItemDisplayName(final ConceptRelationship memberRel,
    final Concept memberConcept, final ConceptRef memberRef) {
    final String formDisplay = LoincQuestionnaireHelper.resolveFormDisplayName(memberRel);
    if (!StringUtility.isEmpty(formDisplay)) {
      return formDisplay;
    }
    return resolveLoincDisplayName(memberConcept, memberRef);
  }

  /**
   * Resolves the LOINC short common name for a concept (panel or member).
   *
   * @param concept the concept
   * @return short common name or null
   */
  private static String resolveLoincShortCommonName(final Concept concept) {
    return resolveLoincDisplayName(concept, null);
  }

  /**
   * Converts a LOINC short common name to a FHIR Questionnaire.name
   * (non-alphanumeric → underscore).
   *
   * @param shortCommonName the short common name
   * @return machine name
   */
  private static String toQuestionnaireName(final String shortCommonName) {
    if (StringUtility.isEmpty(shortCommonName)) {
      return shortCommonName;
    }
    return shortCommonName.replaceAll("[^a-zA-Z0-9]+", "_").replaceAll("^_+|_+$", "");
  }

  /**
   * Resolves the LOINC short common name for questionnaire item display.
   *
   * @param concept the loaded member concept (optional)
   * @param fallback the member concept ref from a relationship (optional)
   * @return display text
   */
  private static String resolveLoincDisplayName(final Concept concept, final ConceptRef fallback) {
    if (concept != null && concept.getAttributes() != null) {
      final Map<String, String> attrs = concept.getAttributes();
      String shortName = attrs.get(LoincConstants.ATTR_SHORT_COMMON_NAME);
      if (StringUtility.isEmpty(shortName)) {
        shortName = attrs.get(LoincConstants.ATTR_SHORTNAME);
      }
      if (!StringUtility.isEmpty(shortName)) {
        return shortName;
      }
    }
    if (concept != null) {
      for (final Term term : concept.getTerms()) {
        if (!term.getActive() || StringUtility.isEmpty(term.getName())) {
          continue;
        }
        final String termType = term.getType();
        if (LoincConstants.TERM_TYPE_SHORT_COMMON_NAME.equals(termType)
            || LoincConstants.TERM_TYPE_SHORTNAME.equals(termType)) {
          return term.getName();
        }
      }
    }
    if (concept != null && !StringUtility.isEmpty(concept.getName())) {
      return concept.getName();
    }
    if (fallback != null && !StringUtility.isEmpty(fallback.getName())) {
      return fallback.getName();
    }
    return null;
  }

  /**
   * Reads LOINC scale type from a concept's indexed attributes.
   *
   * @param concept the concept
   * @return scale type or null
   */
  private static String getScaleType(final Concept concept) {
    if (concept == null || concept.getAttributes() == null) {
      return null;
    }
    final Map<String, String> attrs = concept.getAttributes();
    String scaleTyp = attrs.get(ATTR_SCALE_TYP);
    if (scaleTyp == null) {
      scaleTyp = attrs.get(ATTR_LOINC_SCALE_TYP);
    }
    if (scaleTyp == null) {
      scaleTyp = attrs.get("scale_typ");
    }
    return scaleTyp;
  }

  /**
   * Resolves FHIR Questionnaire item type from LOINC scale type and answer
   * options.
   *
   * @param memberConcept the member concept
   * @param answerOptions the answer options
   * @return the item type
   */
  private static Questionnaire.QuestionnaireItemType resolveQuestionnaireItemType(
    final Concept memberConcept,
    final List<Questionnaire.QuestionnaireItemAnswerOptionComponent> answerOptions) {
    if (answerOptions != null && !answerOptions.isEmpty()) {
      return Questionnaire.QuestionnaireItemType.CHOICE;
    }
    if (LoincQuestionnaireHelper.isDateProperty(memberConcept)) {
      return Questionnaire.QuestionnaireItemType.DATE;
    }
    final String scaleTyp = getScaleType(memberConcept);
    if (scaleTyp != null) {
      final String normalized = scaleTyp.toUpperCase(Locale.ENGLISH);
      if (normalized.contains("QN") || normalized.contains("SEMIQN")) {
        return Questionnaire.QuestionnaireItemType.DECIMAL;
      }
      if (normalized.contains("QL") || normalized.equals("ORD") || normalized.equals("NAR")
          || normalized.equals("NOM") || normalized.equals("DOC") || normalized.equals("SET")
          || normalized.equals("MULTI")) {
        return Questionnaire.QuestionnaireItemType.STRING;
      }
    }
    return Questionnaire.QuestionnaireItemType.DECIMAL;
  }

  /**
   * Finds questionnaire items for panel members (groups or direct questions).
   *
   * @param mainConcept the main questionnaire concept
   * @param searchService the search service
   * @param terminology the terminology
   * @param processedLinkIds set of already processed form linkIds
   * @param latestVersion the latest version
   * @return list of group questionnaire item components
   * @throws Exception the exception
   */
  private static List<Questionnaire.QuestionnaireItemComponent> findGroupConcepts(
    final Concept mainConcept, final EntityRepositoryService searchService,
    final Terminology terminology, final Set<String> processedLinkIds, final String latestVersion)
    throws Exception {

    final List<Questionnaire.QuestionnaireItemComponent> allItems = new ArrayList<>();

    try {
      List<ConceptRelationship> memberRels =
          findPanelMemberRelationships(mainConcept.getCode(), searchService, terminology);

      if (memberRels.isEmpty() && !mainConcept.getChildren().isEmpty()) {
        memberRels = new ArrayList<>();
        for (final ConceptRef child : mainConcept.getChildren()) {
          if (child.getCode() == null || mainConcept.getCode().equals(child.getCode())) {
            continue;
          }
          final ConceptRelationship rel = new ConceptRelationship();
          rel.setHierarchical(true);
          rel.setAdditionalType("parent");
          rel.setFrom(child);
          rel.setTo(new ConceptRef(mainConcept.getCode(), mainConcept.getName()));
          memberRels.add(rel);
        }
        memberRels = sortMemberRelationships(memberRels);
      }

      for (final ConceptRelationship memberRel : memberRels) {
        final ConceptRef memberRef = getMemberConceptRef(memberRel);
        if (memberRef == null) {
          continue;
        }
        final String linkId = LoincQuestionnaireHelper.resolveMemberLinkId(memberRel, memberRef);
        if (StringUtility.isEmpty(linkId) || processedLinkIds.contains(linkId)) {
          continue;
        }
        final String memberCode = memberRef.getCode();
        if (memberCode == null) {
          continue;
        }

        final Concept memberConcept = TerminologyUtility.getConcept(searchService,
            terminology.getAbbreviation(), terminology.getPublisher(), latestVersion, memberCode);

        if (memberConcept != null) {
          final boolean isOrganizer = isOrganizerConcept(memberConcept);

          if (isOrganizer) {
            final Questionnaire.QuestionnaireItemComponent groupItem = createGroupItem(memberRel,
                searchService, terminology, processedLinkIds, latestVersion);
            if (groupItem != null) {
              allItems.add(groupItem);
              processedLinkIds.add(linkId);
            }
          } else {
            final Questionnaire.QuestionnaireItemComponent questionItem = createDirectQuestionItem(
                memberRel, searchService, terminology, processedLinkIds, latestVersion);
            if (questionItem != null) {
              allItems.add(questionItem);
              processedLinkIds.add(linkId);
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
   * @param processedLinkIds set of already processed form linkIds
   * @param latestVersion the latest version
   * @return the questionnaire item component
   * @throws Exception the exception
   */
  private static Questionnaire.QuestionnaireItemComponent createGroupItem(
    final ConceptRelationship hasMemberRel, final EntityRepositoryService searchService,
    final Terminology terminology, final Set<String> processedLinkIds, final String latestVersion)
    throws Exception {

    final Questionnaire.QuestionnaireItemComponent groupItem =
        new Questionnaire.QuestionnaireItemComponent();

    final ConceptRef memberRef = getMemberConceptRef(hasMemberRel);
    if (memberRef != null) {
      final ConceptRef toConcept = memberRef;
      final Concept memberConcept =
          TerminologyUtility.getConcept(searchService, terminology.getAbbreviation(),
              terminology.getPublisher(), latestVersion, toConcept.getCode());
      final String displayName = resolveItemDisplayName(hasMemberRel, memberConcept, toConcept);

      groupItem.setLinkId(LoincQuestionnaireHelper.resolveMemberLinkId(hasMemberRel, toConcept));
      final String prefix = LoincQuestionnaireHelper.resolveFormPrefix(hasMemberRel);
      if (!StringUtility.isEmpty(prefix)) {
        groupItem.setPrefix(prefix);
      }
      groupItem.setText(displayName);
      groupItem.setType(Questionnaire.QuestionnaireItemType.GROUP);
      if (isOrganizerConcept(memberConcept)) {
        groupItem.setRequired(true);
      }

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
          answerCoding.setSystem(LoincConstants.LOINC_URI);
          answerCoding.setCode("LA33-6");
          enableWhen.setAnswer(answerCoding);

          groupItem.addEnableWhen(enableWhen);
        }
      }

      // Find questions for this group
      final List<Questionnaire.QuestionnaireItemComponent> questions =
          findQuestionsForGroup(toConcept.getCode(), searchService, terminology, processedLinkIds,
              latestVersion, LoincQuestionnaireHelper.resolveMemberLinkId(hasMemberRel, toConcept));

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
   * @param processedLinkIds set of already processed form linkIds
   * @param latestVersion the terminology version
   * @param parentLinkId parent group linkId for form-scoped member-edge
   *          selection
   * @return list of question components
   * @throws Exception the exception
   */
  private static List<Questionnaire.QuestionnaireItemComponent> findQuestionsForGroup(
    final String groupCode, final EntityRepositoryService searchService,
    final Terminology terminology, final Set<String> processedLinkIds, final String latestVersion,
    final String parentLinkId) throws Exception {

    final List<Questionnaire.QuestionnaireItemComponent> questions = new ArrayList<>();
    final Set<String> groupProcessedLinkIds = new HashSet<>();

    try {
      List<ConceptRelationship> memberRels =
          findPanelMemberRelationships(groupCode, searchService, terminology);
      memberRels = LoincQuestionnaireHelper.dedupePanelMemberRelationshipsForFormContext(memberRels,
          parentLinkId);
      memberRels = sortMemberRelationships(memberRels);

      final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
      logger.debug("Found {} panel member relationships for group {}: {}", memberRels.size(),
          groupCode, memberRels.stream().map(rel -> {
            final ConceptRef ref = getMemberConceptRef(rel);
            return ref != null ? ref.getCode() : "null";
          }).collect(Collectors.joining(", ")));

      for (final ConceptRelationship hasMemberRel : memberRels) {
        final ConceptRef memberRef = getMemberConceptRef(hasMemberRel);
        if (memberRef == null || memberRef.getCode() == null) {
          continue;
        }
        final String linkId = LoincQuestionnaireHelper.resolveMemberLinkId(hasMemberRel, memberRef);
        if (StringUtility.isEmpty(linkId) || groupProcessedLinkIds.contains(linkId)) {
          continue;
        }
        final String questionCode = memberRef.getCode();
        if (groupCode.equals("93303-6")) {
          final String questionText = memberRef.getName();
          if (questionText != null && questionText.contains("description")) {
            logger.debug("Filtering out description item: {} - not in master file structure",
                questionCode);
            continue;
          }
        }

        final Concept memberConcept = TerminologyUtility.getConcept(searchService,
            terminology.getAbbreviation(), terminology.getPublisher(), latestVersion, questionCode);
        if (memberConcept == null) {
          continue;
        }

        final Questionnaire.QuestionnaireItemComponent item;
        if (isOrganizerConcept(memberConcept)) {
          item = createGroupItem(hasMemberRel, searchService, terminology, processedLinkIds,
              latestVersion);
        } else {
          item = createQuestionItem(hasMemberRel, searchService, terminology, processedLinkIds);
        }
        if (item != null && !StringUtility.isEmpty(item.getLinkId())) {
          questions.add(item);
          groupProcessedLinkIds.add(linkId);
          logger.debug("Successfully created item linkId={} code={}", linkId, questionCode);
        } else {
          logger.warn("Failed to create item for linkId={} code={}", linkId, questionCode);
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
   * @param processedLinkIds set of already processed form linkIds
   * @return the questionnaire item component
   * @throws Exception the exception
   */
  private static Questionnaire.QuestionnaireItemComponent createQuestionItem(
    final ConceptRelationship hasMemberRel, final EntityRepositoryService searchService,
    final Terminology terminology, final Set<String> processedLinkIds) throws Exception {

    final Questionnaire.QuestionnaireItemComponent questionItem =
        new Questionnaire.QuestionnaireItemComponent();

    final ConceptRef memberRef = getMemberConceptRef(hasMemberRel);
    if (memberRef != null) {
      final ConceptRef toConcept = memberRef;
      final Concept memberConcept =
          TerminologyUtility.getConcept(searchService, terminology.getAbbreviation(),
              terminology.getPublisher(), terminology.getVersion(), toConcept.getCode());
      final String displayName = resolveItemDisplayName(hasMemberRel, memberConcept, toConcept);

      questionItem.setLinkId(LoincQuestionnaireHelper.resolveMemberLinkId(hasMemberRel, toConcept));
      final String prefix = LoincQuestionnaireHelper.resolveFormPrefix(hasMemberRel);
      if (!StringUtility.isEmpty(prefix)) {
        questionItem.setPrefix(prefix);
      }
      questionItem.setText(displayName);
      questionItem.setRepeats(false);

      final Coding coding = new Coding();
      coding.setSystem(terminology.getUri());
      coding.setCode(toConcept.getCode());
      coding.setDisplay(displayName);
      questionItem.addCode(coding);

      // Find answer options for this question
      final List<Questionnaire.QuestionnaireItemAnswerOptionComponent> answerOptions =
          findAnswerOptionsForQuestion(memberConcept, searchService, terminology);

      questionItem.setType(resolveQuestionnaireItemType(memberConcept, answerOptions));
      for (final Questionnaire.QuestionnaireItemAnswerOptionComponent option : answerOptions) {
        questionItem.addAnswerOption(option);
      }
    }

    return questionItem;
  }

  /**
   * Builds a FHIR answer option from an LA answer-list member concept.
   *
   * @param laConcept the LA concept
   * @param terminology the terminology
   * @return the answer option or null
   */
  private static Questionnaire.QuestionnaireItemAnswerOptionComponent toAnswerOption(
    final Concept laConcept, final Terminology terminology) {
    if (laConcept == null || laConcept.getCode() == null) {
      return null;
    }
    final Questionnaire.QuestionnaireItemAnswerOptionComponent option =
        new Questionnaire.QuestionnaireItemAnswerOptionComponent();
    final Coding valueCoding = new Coding();
    valueCoding.setSystem(terminology.getUri());
    valueCoding.setCode(laConcept.getCode());
    valueCoding.setDisplay(laConcept.getName());
    option.setValue(valueCoding);
    return option;
  }

  /**
   * Finds answer options for a question from its {@code answer-list} property,
   * with {@code has_answers} relationship fallback.
   *
   * @param memberConcept the question concept
   * @param searchService the search service
   * @param terminology the terminology
   * @return list of answer option components
   * @throws Exception the exception
   */
  private static List<Questionnaire.QuestionnaireItemAnswerOptionComponent> findAnswerOptionsForQuestion(
    final Concept memberConcept, final EntityRepositoryService searchService,
    final Terminology terminology) throws Exception {

    final List<Questionnaire.QuestionnaireItemAnswerOptionComponent> answerOptions =
        new ArrayList<>();
    if (memberConcept == null || memberConcept.getCode() == null) {
      return answerOptions;
    }

    try {
      final String llCode = LoincQuestionnaireHelper.resolveAnswerListCode(memberConcept);
      if (!StringUtility.isEmpty(llCode)) {
        for (final Concept laConcept : LoincQuestionnaireHelper.findAnswerListMembers(searchService,
            terminology, llCode)) {
          final Questionnaire.QuestionnaireItemAnswerOptionComponent option =
              toAnswerOption(laConcept, terminology);
          if (option != null) {
            answerOptions.add(option);
          }
        }
      }
      if (!answerOptions.isEmpty()) {
        return answerOptions;
      }

      final String questionCode = memberConcept.getCode();
      final String hasAnswersQuery = "from.code:" + StringUtility.escapeQuery(questionCode)
          + " AND additionalType:has_answers";
      final List<ConceptRelationship> hasAnswersRels =
          searchService.findAll(hasAnswersQuery, null, ConceptRelationship.class);
      final Set<String> uniqueAnswerCodes = new HashSet<>();

      for (final ConceptRelationship hasAnswersRel : hasAnswersRels) {
        if (hasAnswersRel.getTo() == null || hasAnswersRel.getTo().getCode() == null) {
          continue;
        }
        final String fallbackLlCode = hasAnswersRel.getTo().getCode();
        for (final Concept laConcept : LoincQuestionnaireHelper.findAnswerListMembers(searchService,
            terminology, fallbackLlCode)) {
          if (laConcept == null || laConcept.getCode() == null
              || !uniqueAnswerCodes.add(laConcept.getCode())) {
            continue;
          }
          final Questionnaire.QuestionnaireItemAnswerOptionComponent option =
              toAnswerOption(laConcept, terminology);
          if (option != null) {
            answerOptions.add(option);
          }
        }
      }

    } catch (final Exception e) {
      final Logger logger = LoggerFactory.getLogger(FhirUtilityR4.class);
      logger.warn("Failed to find answer options for question {}: {}", memberConcept.getCode(),
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
   * @param processedLinkIds set of already processed form linkIds
   * @param latestVersion the latest version
   * @return the questionnaire item component
   * @throws Exception the exception
   */
  private static Questionnaire.QuestionnaireItemComponent createDirectQuestionItem(
    final ConceptRelationship hasMemberRel, final EntityRepositoryService searchService,
    final Terminology terminology, final Set<String> processedLinkIds, final String latestVersion)
    throws Exception {

    final Questionnaire.QuestionnaireItemComponent questionItem =
        new Questionnaire.QuestionnaireItemComponent();

    final ConceptRef memberRef = getMemberConceptRef(hasMemberRel);
    if (memberRef != null) {
      final ConceptRef toConcept = memberRef;
      final Concept memberConcept =
          TerminologyUtility.getConcept(searchService, terminology.getAbbreviation(),
              terminology.getPublisher(), latestVersion, toConcept.getCode());
      final String displayName = resolveItemDisplayName(hasMemberRel, memberConcept, toConcept);

      questionItem.setLinkId(LoincQuestionnaireHelper.resolveMemberLinkId(hasMemberRel, toConcept));
      final String prefix = LoincQuestionnaireHelper.resolveFormPrefix(hasMemberRel);
      if (!StringUtility.isEmpty(prefix)) {
        questionItem.setPrefix(prefix);
      }
      questionItem.setText(displayName);
      questionItem.setRepeats(false);

      final Coding coding = new Coding();
      coding.setSystem(terminology.getUri());
      coding.setCode(toConcept.getCode());
      coding.setDisplay(displayName);
      questionItem.addCode(coding);

      // Find answer options for this question
      final List<Questionnaire.QuestionnaireItemAnswerOptionComponent> answerOptions =
          findAnswerOptionsForQuestion(memberConcept, searchService, terminology);

      questionItem.setType(resolveQuestionnaireItemType(memberConcept, answerOptions));
      for (final Questionnaire.QuestionnaireItemAnswerOptionComponent option : answerOptions) {
        questionItem.addAnswerOption(option);
      }
    }

    return questionItem;
  }

  /**
   * Gets the system URI for a terminology based on its abbreviation, publisher, and version. This
   * method uses TerminologyUtility to get the actual URI from the database.
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
   * Determines if a concept should be included as a main question based on its properties. Filters
   * out variant concepts that are overly specific or descriptive.
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
