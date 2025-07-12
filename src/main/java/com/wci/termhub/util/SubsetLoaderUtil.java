/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.ValueSet;
import org.hl7.fhir.r5.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r5.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r5.model.ValueSet.ValueSetComposeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.SubsetMember;
import com.wci.termhub.model.SubsetRef;
import com.wci.termhub.service.EntityRepositoryService;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * The Class SubsetLoaderUtil.
 */
public final class SubsetLoaderUtil {

  /** The Constant logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(SubsetLoaderUtil.class);

  /**
   * Instantiates a new subset loader util.
   */
  private SubsetLoaderUtil() {
    // Utility class
  }

  /**
   * Loads a FHIR ValueSet (R4 or R5) from JSON, maps to Subset/SubsetMember,
   * and persists.
   * @param service the repository service
   * @param json the ValueSet JSON
   * @param isR5 true for R5, false for R4
   * @return the Subset id
   * @throws Exception on error
   */
  public static String loadSubset(final EntityRepositoryService service, final String json,
    final boolean isR5) throws Exception {

    LOGGER.info("Loading Subset from JSON, isR5={}", isR5);

    if (!isR5) {
      final IParser parser = FhirContext.forR4().newJsonParser();
      final org.hl7.fhir.r4.model.ValueSet vs =
          parser.parseResource(org.hl7.fhir.r4.model.ValueSet.class, json);
      return persistValueSetR4(service, vs);
    }
    final IParser parser = FhirContext.forR5().newJsonParser();
    final org.hl7.fhir.r5.model.ValueSet vs =
        parser.parseResource(org.hl7.fhir.r5.model.ValueSet.class, json);
    return persistValueSetR5(service, vs);
  }

  /**
   * Persist value set R4.
   *
   * @param service the service
   * @param valueSet the value set
   * @return the string
   * @throws Exception the exception
   */
  private static String persistValueSetR4(final EntityRepositoryService service,
    final org.hl7.fhir.r4.model.ValueSet valueSet) throws Exception {

    LOGGER.info("Persisting ValueSet R4: {}", valueSet.getTitle());

    // throw exception if this value set was already loaded
    if (doesValueSetExist(service, valueSet.getTitle(), valueSet.getVersion(),
        valueSet.getPublisher())) {
      throw new Exception(
          "ValueSet with title '" + valueSet.getTitle() + "', version '" + valueSet.getVersion()
              + "' and publisher '" + valueSet.getPublisher() + "' already exists.");
    }

    final Subset subset = new Subset();
    String id = valueSet.getIdElement().getIdPart();
    if (id == null || id.isEmpty()) {
      id = java.util.UUID.randomUUID().toString();
    }
    subset.setId(id);
    subset.setCode(null);
    subset.setDescription(valueSet.getDescription());
    subset.setName(valueSet.getName());
    subset.setLoaded(true);
    // Set abbreviation from title if present, otherwise leave null
    if (valueSet.hasTitle() && valueSet.getTitle() != null && !valueSet.getTitle().isEmpty()) {
      subset.setAbbreviation(valueSet.getTitle());
    }
    // Set fromPublisher from publisher
    if (valueSet.hasPublisher() && valueSet.getPublisher() != null
        && !valueSet.getPublisher().isEmpty()) {
      subset.setFromPublisher(valueSet.getPublisher());
      subset.setPublisher(valueSet.getPublisher());
    }
    // Set fromVersion from version
    if (valueSet.hasVersion() && valueSet.getVersion() != null
        && !valueSet.getVersion().isEmpty()) {
      subset.setFromVersion(valueSet.getVersion());
      subset.setVersion(valueSet.getVersion());
    }
    // Set fromTerminology from first compose.include.system if present
    if (valueSet.hasCompose() && valueSet.getCompose().hasInclude()
        && !valueSet.getCompose().getInclude().isEmpty()) {
      final org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent inc =
          valueSet.getCompose().getIncludeFirstRep();
      if (inc.hasSystem() && inc.getSystem() != null && !inc.getSystem().isEmpty()) {
        subset.setFromTerminology(inc.getSystem());
      }
    }

    // Store url and date in attributes if present
    if (valueSet.hasUrl() && valueSet.getUrl() != null && !valueSet.getUrl().isEmpty()) {
      if (subset.getAttributes() == null) {
        subset.setAttributes(new java.util.HashMap<>());
      }
      subset.getAttributes().put(Subset.Attributes.fhirUrl.name(), valueSet.getUrl());
    }
    if (valueSet.hasDate() && valueSet.getDate() != null) {
      final String dateStr = valueSet.getDateElement().asStringValue();
      if (dateStr != null && !dateStr.isEmpty()) {
        if (subset.getAttributes() == null) {
          subset.setAttributes(new java.util.HashMap<>());
        }
        subset.getAttributes().put(Subset.Attributes.fhirDate.name(), dateStr);
      }
    }
    // Store experimental and identifier in attributes if present
    if (valueSet.hasExperimental()) {
      if (subset.getAttributes() == null) {
        subset.setAttributes(new java.util.HashMap<>());
      }
      subset.getAttributes().put(Subset.Attributes.fhirExperimental.name(),
          Boolean.toString(valueSet.getExperimental()));
    }
    if (valueSet.hasIdentifier() && valueSet.getIdentifier() != null
        && !valueSet.getIdentifier().isEmpty()) {
      if (subset.getAttributes() == null) {
        subset.setAttributes(new java.util.HashMap<>());
      }
      // Store only the first identifier value for simplicity
      final String identifierValue = valueSet.getIdentifierFirstRep().getValue();
      if (identifierValue != null && !identifierValue.isEmpty()) {
        subset.getAttributes().put(Subset.Attributes.fhirIdentifier.name(), identifierValue);
      }
    }

    subset.setCategory("ValueSet");
    service.add(Subset.class, subset);

    LOGGER.info("VALUE SET LOADED: {}", subset);
    LOGGER.info("LOADING SUBSET MEMBERS");

    final SubsetRef subsetRef = new SubsetRef();
    subsetRef.setCode(subset.getCode());
    subsetRef.setName(subset.getName());
    subsetRef.setFromPublisher(subset.getFromPublisher());
    subsetRef.setFromVersion(subset.getFromVersion());
    subsetRef.setFromTerminology(subset.getFromTerminology());
    subsetRef.setLoaded(true);
    subsetRef.setAbbreviation(subset.getAbbreviation());
    subsetRef.setVersion(subset.getVersion());
    subsetRef.setPublisher(subset.getPublisher());

    final List<SubsetMember> members = new ArrayList<>();
    subsetRef.setCode(subset.getCode());
    // Compose.include
    if (valueSet.hasCompose()) {
      for (final org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent inc : valueSet.getCompose()
          .getInclude()) {
        for (final org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent c : inc.getConcept()) {
          final SubsetMember m = new SubsetMember();
          m.setId(java.util.UUID.randomUUID().toString());
          m.setCode(c.getCode());
          m.setName(c.getDisplay());
          m.setSubset(subsetRef);
          members.add(m);
        }
      }
    }
    // Expansion.contains
    if (valueSet.hasExpansion()) {
      for (final org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionContainsComponent c : valueSet
          .getExpansion().getContains()) {
        final SubsetMember m = new SubsetMember();
        m.setId(java.util.UUID.randomUUID().toString());
        m.setCode(c.getCode());
        m.setName(c.getDisplay());
        m.setSubset(subsetRef);
        members.add(m);
      }
    }
    if (!members.isEmpty()) {
      service.addBulk(SubsetMember.class, new ArrayList<>(members));
    }

    return subset.getId();
  }

  /**
   * Persist value set R5.
   *
   * @param service the service
   * @param valueSet the value set
   * @return the string
   * @throws Exception the exception
   */
  private static String persistValueSetR5(final EntityRepositoryService service,
    final org.hl7.fhir.r5.model.ValueSet valueSet) throws Exception {

    LOGGER.info("Persisting ValueSet R4: {}", valueSet.getTitle());

    // throw exception if this value set was already loaded
    if (doesValueSetExist(service, valueSet.getTitle(), valueSet.getVersion(),
        valueSet.getPublisher())) {
      throw new Exception(
          "ValueSet with title '" + valueSet.getTitle() + "', version '" + valueSet.getVersion()
              + "' and publisher '" + valueSet.getPublisher() + "' already exists.");
    }

    final Subset subset = new Subset();
    String id = valueSet.getIdElement().getIdPart();
    if (id == null || id.isEmpty()) {
      id = java.util.UUID.randomUUID().toString();
    }
    subset.setId(id);
    subset.setCode(null);
    subset.setDescription(valueSet.getDescription());
    subset.setName(valueSet.getName());
    subset.setLoaded(true);
    // Set abbreviation from title if present, otherwise leave null
    if (valueSet.hasTitle() && valueSet.getTitle() != null && !valueSet.getTitle().isEmpty()) {
      subset.setAbbreviation(valueSet.getTitle());
    }
    // Set fromPublisher from publisher
    if (valueSet.hasPublisher() && valueSet.getPublisher() != null
        && !valueSet.getPublisher().isEmpty()) {
      subset.setFromPublisher(valueSet.getPublisher());
      subset.setPublisher(valueSet.getPublisher());
    }
    // Set fromVersion from version
    if (valueSet.hasVersion() && valueSet.getVersion() != null
        && !valueSet.getVersion().isEmpty()) {
      subset.setFromVersion(valueSet.getVersion());
      subset.setVersion(valueSet.getVersion());
    }
    // Set fromTerminology from first compose.include.system if present
    if (valueSet.hasCompose() && valueSet.getCompose().hasInclude()
        && !valueSet.getCompose().getInclude().isEmpty()) {
      final org.hl7.fhir.r5.model.ValueSet.ConceptSetComponent inc =
          valueSet.getCompose().getIncludeFirstRep();
      if (inc.hasSystem() && inc.getSystem() != null && !inc.getSystem().isEmpty()) {
        subset.setFromTerminology(inc.getSystem());
      }
    }
    // Store url and date in attributes if present
    if (valueSet.hasUrl() && valueSet.getUrl() != null && !valueSet.getUrl().isEmpty()) {
      if (subset.getAttributes() == null) {
        subset.setAttributes(new java.util.HashMap<>());
      }
      subset.getAttributes().put(Subset.Attributes.fhirUrl.name(), valueSet.getUrl());
    }
    if (valueSet.hasDate() && valueSet.getDate() != null) {
      final String dateStr = valueSet.getDateElement().asStringValue();
      if (dateStr != null && !dateStr.isEmpty()) {
        if (subset.getAttributes() == null) {
          subset.setAttributes(new java.util.HashMap<>());
        }
        subset.getAttributes().put(Subset.Attributes.fhirDate.name(), dateStr);
      }
    }
    // Store experimental and identifier in attributes if present
    if (valueSet.hasExperimental()) {
      if (subset.getAttributes() == null) {
        subset.setAttributes(new java.util.HashMap<>());
      }
      subset.getAttributes().put(Subset.Attributes.fhirExperimental.name(),
          Boolean.toString(valueSet.getExperimental()));
    }
    if (valueSet.hasIdentifier() && valueSet.getIdentifier() != null
        && !valueSet.getIdentifier().isEmpty()) {
      if (subset.getAttributes() == null) {
        subset.setAttributes(new java.util.HashMap<>());
      }
      // Store only the first identifier value for simplicity
      final String identifierValue = valueSet.getIdentifierFirstRep().getValue();
      if (identifierValue != null && !identifierValue.isEmpty()) {
        subset.getAttributes().put(Subset.Attributes.fhirIdentifier.name(), identifierValue);
      }
    }
    subset.setCategory("ValueSet");
    service.add(Subset.class, subset);

    LOGGER.info("VALUE SET LOADED: {}", subset);
    LOGGER.info("LOADING SUBSET MEMBERS");

    final SubsetRef subsetRef = new SubsetRef();
    subsetRef.setCode(subset.getCode());
    subsetRef.setName(subset.getName());
    subsetRef.setFromPublisher(subset.getFromPublisher());
    subsetRef.setFromVersion(subset.getFromVersion());
    subsetRef.setFromTerminology(subset.getFromTerminology());
    subsetRef.setLoaded(true);
    subsetRef.setAbbreviation(subset.getAbbreviation());
    subsetRef.setVersion(subset.getVersion());
    subsetRef.setPublisher(subset.getPublisher());

    final List<SubsetMember> members = new ArrayList<>();
    subsetRef.setCode(subset.getCode());
    // Compose.include
    if (valueSet.hasCompose()) {
      for (final org.hl7.fhir.r5.model.ValueSet.ConceptSetComponent inc : valueSet.getCompose()
          .getInclude()) {
        for (final org.hl7.fhir.r5.model.ValueSet.ConceptReferenceComponent c : inc.getConcept()) {
          final SubsetMember m = new SubsetMember();
          m.setId(java.util.UUID.randomUUID().toString());
          m.setCode(c.getCode());
          m.setName(c.getDisplay());
          m.setSubset(subsetRef);
          members.add(m);
        }
      }
    }
    // Expansion.contains
    if (valueSet.hasExpansion()) {
      for (final org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionContainsComponent c : valueSet
          .getExpansion().getContains()) {
        final SubsetMember m = new SubsetMember();
        m.setId(java.util.UUID.randomUUID().toString());
        m.setCode(c.getCode());
        m.setName(c.getDisplay());
        m.setSubset(subsetRef);
        members.add(m);
      }
    }
    if (!members.isEmpty()) {
      service.addBulk(SubsetMember.class, new ArrayList<>(members));
    }

    return subset.getId();
  }

  /**
   * Converts a Subset and its SubsetMembers to a FHIR R5 ValueSet.
   * @param subset the Subset
   * @param members the SubsetMembers
   * @return the FHIR R5 ValueSet
   */
  public static ValueSet toR5ValueSet(final Subset subset, final List<SubsetMember> members) {

    final ValueSet valueSet = new ValueSet();
    final String id = subset.getId() != null ? subset.getId() : subset.getCode();
    valueSet.setId(id);
    valueSet.setName(subset.getName());
    // Set title from abbreviation if present, else fallback to name
    if (subset.getAbbreviation() != null && !subset.getAbbreviation().isEmpty()) {
      valueSet.setTitle(subset.getAbbreviation());
    } else {
      valueSet.setTitle(subset.getName());
    }
    valueSet.setDescription(subset.getDescription());
    valueSet.setVersion(subset.getFromVersion());
    valueSet.setStatus(Enumerations.PublicationStatus.ACTIVE);
    valueSet.setPublisher(subset.getFromPublisher());
    // Set url from attributes if present, else fallback
    final String url = subset.getAttributes() != null
        ? subset.getAttributes().get(Subset.Attributes.fhirUrl.name()) : null;
    if (url != null && !url.isEmpty()) {
      valueSet.setUrl(url);
    }
    // Set date from attributes if present, else fallback
    final String dateStr = subset.getAttributes() != null
        ? subset.getAttributes().get(Subset.Attributes.fhirDate.name()) : null;
    if (dateStr != null && !dateStr.isEmpty()) {
      try {
        valueSet.setDate(new org.hl7.fhir.r5.model.DateTimeType(dateStr).getValue());
      } catch (final Exception e) {
        valueSet.setDate(new java.util.Date());
      }
    } else {
      valueSet.setDate(new java.util.Date());
    }
    // Set experimental from attributes if present, else fallback
    final String experimentalStr = subset.getAttributes() != null
        ? subset.getAttributes().get(Subset.Attributes.fhirExperimental.name()) : null;
    if (experimentalStr != null) {
      valueSet.setExperimental(Boolean.parseBoolean(experimentalStr));
    }
    // Set identifier from attributes if present, else fallback
    final String identifierValue = subset.getAttributes() != null
        ? subset.getAttributes().get(Subset.Attributes.fhirIdentifier.name()) : null;
    if (identifierValue != null && !identifierValue.isEmpty()) {
      valueSet.addIdentifier().setValue(identifierValue);
    }
    // Compose/include
    final ValueSetComposeComponent compose = new ValueSetComposeComponent();
    final ConceptSetComponent include = new ConceptSetComponent();
    // Use terminology as system if available
    if (subset.getTerminology() != null) {
      include.setSystem(subset.getTerminology());
    }
    for (final SubsetMember member : members) {
      if (member.getCode() == null || (member.getCodeActive() != null && !member.getCodeActive())) {
        continue;
      }
      final ConceptReferenceComponent concept = new ConceptReferenceComponent();
      concept.setCode(member.getCode());
      if (member.getName() != null) {
        concept.setDisplay(member.getName());
      }
      include.addConcept(concept);
    }
    if (!include.getConcept().isEmpty()) {
      compose.addInclude(include);
      valueSet.setCompose(compose);
    }
    return valueSet;
  }

  /**
   * Converts a Subset and its SubsetMembers to a FHIR R4 ValueSet.
   * @param subset the Subset
   * @param members the SubsetMembers
   * @return the FHIR R4 ValueSet
   */
  public static org.hl7.fhir.r4.model.ValueSet toR4ValueSet(final Subset subset,
    final List<SubsetMember> members) {

    final org.hl7.fhir.r4.model.ValueSet valueSet = new org.hl7.fhir.r4.model.ValueSet();
    final String id = subset.getId() != null ? subset.getId() : subset.getCode();
    valueSet.setId(id);
    valueSet.setName(subset.getName());
    // Set title from abbreviation if present, else fallback to name
    if (subset.getAbbreviation() != null && !subset.getAbbreviation().isEmpty()) {
      valueSet.setTitle(subset.getAbbreviation());
    } else {
      valueSet.setTitle(subset.getName());
    }
    valueSet.setDescription(subset.getDescription());
    valueSet.setVersion(subset.getFromVersion());
    valueSet.setStatus(org.hl7.fhir.r4.model.Enumerations.PublicationStatus.ACTIVE);

    valueSet.setPublisher(subset.getFromPublisher());
    // Set url from attributes if present, else fallback
    final String url = subset.getAttributes() != null
        ? subset.getAttributes().get(Subset.Attributes.fhirUrl.name()) : null;
    if (url != null && !url.isEmpty()) {
      valueSet.setUrl(url);
    }
    // Set date from attributes if present, else fallback
    final String dateStr = subset.getAttributes() != null
        ? subset.getAttributes().get(Subset.Attributes.fhirDate.name()) : null;
    if (dateStr != null && !dateStr.isEmpty()) {
      try {
        valueSet.setDate(new org.hl7.fhir.r4.model.DateTimeType(dateStr).getValue());
      } catch (final Exception e) {
        valueSet.setDate(new java.util.Date());
      }
    } else {
      valueSet.setDate(new java.util.Date());
    }

    // Set experimental from attributes if present, else fallback
    final String experimentalStr = subset.getAttributes() != null
        ? subset.getAttributes().get(Subset.Attributes.fhirExperimental.name()) : null;
    if (experimentalStr != null) {
      valueSet.setExperimental(Boolean.parseBoolean(experimentalStr));
    }
    // Set identifier from attributes if present, else fallback
    final String identifierValue = subset.getAttributes() != null
        ? subset.getAttributes().get(Subset.Attributes.fhirIdentifier.name()) : null;
    if (identifierValue != null && !identifierValue.isEmpty()) {
      valueSet.addIdentifier().setValue(identifierValue);
    }
    // Compose/include
    final org.hl7.fhir.r4.model.ValueSet.ValueSetComposeComponent compose =
        new org.hl7.fhir.r4.model.ValueSet.ValueSetComposeComponent();
    final org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent include =
        new org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent();

    // Use terminology as system if available
    if (subset.getTerminology() != null) {
      include.setSystem(subset.getTerminology());
    }
    for (final SubsetMember member : members) {
      if (member.getCode() == null || (member.getCodeActive() != null && !member.getCodeActive())) {
        continue;
      }
      final org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent concept =
          new org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent();
      concept.setCode(member.getCode());
      if (member.getName() != null) {
        concept.setDisplay(member.getName());
      }
      include.addConcept(concept);
    }
    if (!include.getConcept().isEmpty()) {
      compose.addInclude(include);
      valueSet.setCompose(compose);
    }
    return valueSet;
  }

  /**
   * Does value set exist.
   *
   * @param service the service
   * @param title the title
   * @param version the version
   * @param publisher the publisher
   * @return true, if successful
   * @throws Exception the exception
   */
  private static boolean doesValueSetExist(final EntityRepositoryService service,
    final String title, final String version, final String publisher) throws Exception {

    LOGGER.info(
        "Checking if ValueSet exists with abbreviation={}, fromVersion={}, fromPublisher={}", title,
        version, publisher);

    final SearchParameters params = new SearchParameters();
    params.setQuery("abbreviation:" + StringUtility.escapeQuery(title) + " AND version: \""
        + StringUtility.escapeQuery(version) + "\"" + " AND publisher: \""
        + StringUtility.escapeQuery(publisher) + "\"");

    final ResultList<Subset> existingSubsets = service.find(params, Subset.class);
    return !existingSubsets.getItems().isEmpty();
  }
}
