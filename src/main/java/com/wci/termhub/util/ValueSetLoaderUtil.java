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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.SubsetMember;
import com.wci.termhub.model.SubsetRef;
import com.wci.termhub.model.TerminologyRef;
import com.wci.termhub.service.EntityRepositoryService;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * The Class SubsetLoaderUtil.
 */
public final class ValueSetLoaderUtil {

  /** The Constant logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ValueSetLoaderUtil.class);

  /** The Constant contextR4. */
  private static final FhirContext contextR4 = FhirContext.forR4();

  /** The Constant contextR5. */
  private static final FhirContext contextR5 = FhirContext.forR5();

  /**
   * Instantiates a new subset loader util.
   */
  private ValueSetLoaderUtil() {
    // Utility class
  }

  /**
   * Loads a FHIR ValueSet (R4 or R5) from JSON, maps to Subset/SubsetMember, and persists.
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
      final IParser parser = contextR4.newJsonParser();
      final org.hl7.fhir.r4.model.ValueSet vs =
          parser.parseResource(org.hl7.fhir.r4.model.ValueSet.class, json);
      return indexValueSetR4(service, vs);
    }
    final IParser parser = contextR5.newJsonParser();
    final org.hl7.fhir.r5.model.ValueSet vs =
        parser.parseResource(org.hl7.fhir.r5.model.ValueSet.class, json);
    return indexValueSetR5(service, vs);
  }

  /**
   * Persist value set R4.
   *
   * @param service the service
   * @param valueSet the value set
   * @return the string
   * @throws Exception the exception
   */
  private static String indexValueSetR4(final EntityRepositoryService service,
    final org.hl7.fhir.r4.model.ValueSet valueSet) throws Exception {

    LOGGER.info("Indexing ValueSet R4: {}", valueSet.getTitle());
    final long startTime = System.currentTimeMillis();

    try {

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
      subset.setUri(valueSet.getUrl());

      // Use the identifier as the code, otherwise use the id
      // NOTE: termhub generated files will have a single id
      // with a system matching this value.
      for (final org.hl7.fhir.r4.model.Identifier identifier : valueSet.getIdentifier()) {
        if ("https://terminologyhub.com/model/subset/code".equals(identifier.getSystem())) {
          subset.setCode(identifier.getValue());
        }
      }
      if (StringUtility.isEmpty(subset.getCode())) {
        subset.setCode(subset.getId());
      }

      if (valueSet.hasDate()) {
        subset.setReleaseDate(DateUtility.DATE_YYYY_MM_DD_DASH.format(valueSet.getDate()));
      }

      subset.setDescription(valueSet.getDescription());
      subset.setName(valueSet.getName());
      subset.setLoaded(true);

      subset.setAbbreviation(valueSet.getTitle());
      subset.setPublisher(valueSet.getPublisher());
      subset.setVersion(valueSet.getVersion());

      // Set fromTerminology from first compose.include.system if present
      if (valueSet.hasCompose() && valueSet.getCompose().hasInclude()
          && !valueSet.getCompose().getInclude().isEmpty()) {
        final org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent include =
            valueSet.getCompose().getIncludeFirstRep();
        if (include.hasSystem() && include.getSystem() != null && !include.getSystem().isEmpty()) {

          final TerminologyRef fromRef =
              TerminologyUtility.getTerminology(service, include.getSystem());
          subset.setFromTerminology(fromRef.getAbbreviation());
          subset.setFromPublisher(fromRef.getPublisher());
          subset.setFromVersion(fromRef.getVersion());
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

      // subset.setCategory("ValueSet");
      service.add(Subset.class, subset);

      final SubsetRef subsetRef = new SubsetRef(subset.getCode(), subset.getAbbreviation(),
          subset.getPublisher(), subset.getVersion());
      final List<SubsetMember> members = new ArrayList<>();
      subsetRef.setCode(subset.getCode());
      // Compose.include
      if (valueSet.hasCompose()) {
        for (final org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent includes : valueSet
            .getCompose().getInclude()) {

          if (includes.getSystem() == null) {
            throw new Exception("Unable to determine system of value set");
          }

          final TerminologyRef ref =
              TerminologyUtility.getTerminology(service, includes.getSystem());

          for (final org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent c : includes
              .getConcept()) {

            if (c.getCode() == null) {
              LOGGER.warn("    Value set includes component reference without a code = " + c);
              continue;
            }

            final Concept existingConcept = TerminologyUtility.getConcept(service,
                ref.getAbbreviation(), ref.getPublisher(), ref.getVersion(), c.getCode());

            final SubsetMember m = new SubsetMember();
            m.setId(java.util.UUID.randomUUID().toString());
            m.setTerminology(ref.getAbbreviation());
            m.setPublisher(ref.getPublisher());
            m.setVersion(ref.getVersion());
            m.setCode(c.getCode());
            m.setName(
                existingConcept == null ? "Unable to determine name" : existingConcept.getName());
            m.setSubset(subsetRef);
            members.add(m);
          }
        }
      }
      // Expansion.contains
      if (valueSet.hasExpansion()) {

        final Map<String, TerminologyRef> map = new HashMap<>();

        for (final org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionContainsComponent c : valueSet
            .getExpansion().getContains()) {

          if (c.getSystem() == null) {
            LOGGER.warn("    Value set includes expansion entry without a system = " + c);
            continue;
          }
          if (c.getCode() == null) {
            LOGGER.warn("    Value set includes expansion entry without a code = " + c);
            continue;
          }

          if (!map.containsKey(c.getSystem())) {
            map.put(c.getSystem(), TerminologyUtility.getTerminology(service, c.getSystem()));
          }
          final TerminologyRef ref = map.get(c.getSystem());

          final Concept existingConcept = TerminologyUtility.getConcept(service,
              ref.getAbbreviation(), ref.getPublisher(), ref.getVersion(), c.getCode());

          final SubsetMember m = new SubsetMember();
          m.setId(java.util.UUID.randomUUID().toString());
          m.setTerminology(ref.getAbbreviation());
          m.setPublisher(ref.getPublisher());
          m.setVersion(ref.getVersion());
          m.setCode(c.getCode());
          m.setName(
              existingConcept == null ? "Unable to determine name" : existingConcept.getName());
          m.setSubset(subsetRef);
          members.add(m);

        }
      }
      if (!members.isEmpty()) {
        service.addBulk(SubsetMember.class, new ArrayList<>(members));
      }

      LOGGER.info("  member count: {}", members.size());
      LOGGER.info("  duration: {} ms", (System.currentTimeMillis() - startTime));

      return subset.getId();
    } catch (final Exception e) {
      LOGGER.error("Error indexing value set", e);
      throw e;
    }
  }

  /**
   * Persist value set R5.
   *
   * @param service the service
   * @param valueSet the value set
   * @return the string
   * @throws Exception the exception
   */
  private static String indexValueSetR5(final EntityRepositoryService service,
    final org.hl7.fhir.r5.model.ValueSet valueSet) throws Exception {

    LOGGER.info("Indexing ValueSet R5: {}", valueSet.getTitle());
    final long startTime = System.currentTimeMillis();

    try {

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
      subset.setUri(valueSet.getUrl());

      // Use the identifier as the code, otherwise use the id
      // NOTE: termhub generated files will have a single id
      // with a system matching this value.
      for (final org.hl7.fhir.r5.model.Identifier identifier : valueSet.getIdentifier()) {
        if ("https://terminologyhub.com/model/subset/code".equals(identifier.getSystem())) {
          subset.setCode(identifier.getValue());
        }
      }
      if (StringUtility.isEmpty(subset.getCode())) {
        subset.setCode(subset.getId());
      }

      if (valueSet.hasDate()) {
        subset.setReleaseDate(DateUtility.DATE_YYYY_MM_DD_DASH.format(valueSet.getDate()));
      }

      subset.setDescription(valueSet.getDescription());
      subset.setName(valueSet.getName());
      subset.setLoaded(true);

      subset.setAbbreviation(valueSet.getTitle());
      subset.setPublisher(valueSet.getPublisher());
      subset.setVersion(valueSet.getVersion());

      // Set fromTerminology from first compose.include.system if present
      if (valueSet.hasCompose() && valueSet.getCompose().hasInclude()
          && !valueSet.getCompose().getInclude().isEmpty()) {
        final org.hl7.fhir.r5.model.ValueSet.ConceptSetComponent include =
            valueSet.getCompose().getIncludeFirstRep();
        if (include.hasSystem() && include.getSystem() != null && !include.getSystem().isEmpty()) {

          final TerminologyRef fromRef =
              TerminologyUtility.getTerminology(service, include.getSystem());
          subset.setFromTerminology(fromRef.getAbbreviation());
          subset.setFromPublisher(fromRef.getPublisher());
          subset.setFromVersion(fromRef.getVersion());
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

      // subset.setCategory("ValueSet");
      service.add(Subset.class, subset);

      final SubsetRef subsetRef = new SubsetRef(subset.getCode(), subset.getAbbreviation(),
          subset.getPublisher(), subset.getVersion());
      final List<SubsetMember> members = new ArrayList<>();
      subsetRef.setCode(subset.getCode());
      // Compose.include
      if (valueSet.hasCompose()) {
        for (final org.hl7.fhir.r5.model.ValueSet.ConceptSetComponent includes : valueSet
            .getCompose().getInclude()) {

          if (includes.getSystem() == null) {
            throw new Exception("Unable to determine system of value set");
          }

          final TerminologyRef ref =
              TerminologyUtility.getTerminology(service, includes.getSystem());

          for (final org.hl7.fhir.r5.model.ValueSet.ConceptReferenceComponent c : includes
              .getConcept()) {

            if (c.getCode() == null) {
              LOGGER.warn("    Value set includes component reference without a code = " + c);
              continue;
            }

            final Concept existingConcept = TerminologyUtility.getConcept(service,
                ref.getAbbreviation(), ref.getPublisher(), ref.getVersion(), c.getCode());

            final SubsetMember m = new SubsetMember();
            m.setId(java.util.UUID.randomUUID().toString());
            m.setTerminology(ref.getAbbreviation());
            m.setPublisher(ref.getPublisher());
            m.setVersion(ref.getVersion());
            m.setCode(c.getCode());
            m.setName(
                existingConcept == null ? "Unable to determine name" : existingConcept.getName());
            m.setSubset(subsetRef);
            members.add(m);
          }
        }
      }
      // Expansion.contains
      if (valueSet.hasExpansion()) {

        final Map<String, TerminologyRef> map = new HashMap<>();

        for (final org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionContainsComponent c : valueSet
            .getExpansion().getContains()) {

          if (c.getSystem() == null) {
            LOGGER.warn("    Value set includes expansion entry without a system = " + c);
            continue;
          }
          if (c.getCode() == null) {
            LOGGER.warn("    Value set includes expansion entry without a code = " + c);
            continue;
          }

          if (!map.containsKey(c.getSystem())) {
            map.put(c.getSystem(), TerminologyUtility.getTerminology(service, c.getSystem()));
          }
          final TerminologyRef ref = map.get(c.getSystem());

          final Concept existingConcept = TerminologyUtility.getConcept(service,
              ref.getAbbreviation(), ref.getPublisher(), ref.getVersion(), c.getCode());

          final SubsetMember m = new SubsetMember();
          m.setId(java.util.UUID.randomUUID().toString());
          m.setTerminology(ref.getAbbreviation());
          m.setPublisher(ref.getPublisher());
          m.setVersion(ref.getVersion());
          m.setCode(c.getCode());
          m.setName(
              existingConcept == null ? "Unable to determine name" : existingConcept.getName());
          m.setSubset(subsetRef);
          members.add(m);

        }
      }
      if (!members.isEmpty()) {
        service.addBulk(SubsetMember.class, new ArrayList<>(members));
      }

      LOGGER.info("  member count: {}", members.size());
      LOGGER.info("  duration: {} ms", (System.currentTimeMillis() - startTime));

      return subset.getId();
    } catch (final Exception e) {
      LOGGER.error("Error indexing value set", e);
      throw e;
    }

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
