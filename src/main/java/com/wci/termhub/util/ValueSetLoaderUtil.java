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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.algo.ProgressEvent;
import com.wci.termhub.algo.ProgressListener;
import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.fhir.rest.r5.FhirUtilityR5;
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
import jakarta.servlet.http.HttpServletResponse;

/**
 * Value set loader utility.
 */
public final class ValueSetLoaderUtil {

  /** The Constant logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ValueSetLoaderUtil.class);

  /** The id map. Used by QA to map from test files to internal ids. */
  private static Map<String, String> idMap =
      new LinkedHashMap<String, String>(101 * 4 / 3, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<String, String> eldest) {
          return size() > 100;
        }
      };

  /** The Constant contextR4. */
  private static FhirContext contextR4 = FhirContext.forR4();

  /** The Constant contextR5. */
  private static FhirContext contextR5 = FhirContext.forR5();

  /** The Constant JSON_SUBSET_CODE. */
  private static final String JSON_SUBSET_CODE = "https://terminologyhub.com/model/subset/code";

  /**
   * Instantiates a new subset loader util.
   */
  private ValueSetLoaderUtil() {
    // Utility class
  }

  /**
   * Map original id.
   *
   * @param originalId the original id
   * @return the string
   */
  public static String mapOriginalId(final String originalId) {
    return idMap.get(originalId);
  }

  /**
   * Loads a FHIR ValueSet (R4 or R5) from JSON, maps to Subset/SubsetMember, and persists.
   *
   * @param <T> the generic type
   * @param service the repository service
   * @param file the file
   * @param type the type
   * @param listener the listener
   * @return the Subset id
   * @throws Exception on error
   */
  @SuppressWarnings("unchecked")
  public static <T> T loadValueSet(final EntityRepositoryService service, final File file,
    final Class<T> type, final ProgressListener listener) throws Exception {

    LOGGER.info("Loading Subset from JSON, isR5={}", type == org.hl7.fhir.r5.model.ValueSet.class);

    if (type == org.hl7.fhir.r4.model.ValueSet.class) {
      final IParser parser = contextR4.newJsonParser();
      final org.hl7.fhir.r4.model.ValueSet vs = parser.parseResource(
          org.hl7.fhir.r4.model.ValueSet.class, FileUtils.readFileToString(file, "UTF-8"));
      return (T) indexValueSetR4(service, vs, listener);
    }
    final IParser parser = contextR5.newJsonParser();
    final org.hl7.fhir.r5.model.ValueSet vs = parser.parseResource(
        org.hl7.fhir.r5.model.ValueSet.class, FileUtils.readFileToString(file, "UTF-8"));
    return (T) indexValueSetR5(service, vs, listener);
  }

  /**
   * Persist value set R4.
   *
   * @param service the service
   * @param valueSet the value set
   * @param listener the listener
   * @return the string
   * @throws Exception the exception
   */
  private static org.hl7.fhir.r4.model.ValueSet indexValueSetR4(
    final EntityRepositoryService service, final org.hl7.fhir.r4.model.ValueSet valueSet,
    final ProgressListener listener) throws Exception {

    final long startTime = System.currentTimeMillis();

    try {
      // Set listener to 0%
      listener.updateProgress(new ProgressEvent(0));

      LOGGER.info("Indexing ValueSet R4 {} = {}", valueSet.getTitle(), valueSet.getUrl());
      // Basic checks
      // Validate required fields
      if (valueSet.getUrl() == null) {
        throw FhirUtilityR4.exception("ValueSet.url is required", IssueType.INVALID,
            HttpServletResponse.SC_BAD_REQUEST);
      }

      // check for existing
      final String abbreviation = valueSet.getTitle();
      final String publisher = valueSet.getPublisher();
      final String version = valueSet.getVersion();
      Subset subset = findSubset(service, abbreviation, publisher, version);
      if (subset != null) {
        throw FhirUtilityR4.exception(
            "Can not create multiple ValueSet resources the same title, publisher,"
                + " and version. duplicate = " + subset.getId(),
            IssueType.INVALID, HttpServletResponse.SC_CONFLICT);
      }

      subset = new Subset();
      subset.getAttributes().put("loaded", "false");
      // The HAPI Plan server @Create method blanks the identifier on sending a
      // code system in. Always create a new identifier.
      String id = UUID.randomUUID().toString();
      String originalId = valueSet.getIdElement().getIdPart();
      if (!StringUtility.isEmpty(originalId)) {
        subset.getAttributes().put("originalId", originalId);
        idMap.put(originalId, id);
      }
      subset.setId(id);
      subset.setActive(true);
      subset.setUri(valueSet.getUrl());

      // Use the identifier as the code, otherwise use the id
      // NOTE: termhub generated files will have a single id
      // with a system matching this value.
      for (final org.hl7.fhir.r4.model.Identifier identifier : valueSet.getIdentifier()) {
        if (JSON_SUBSET_CODE.equals(identifier.getSystem())) {
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

          final TerminologyRef fromRef = new TerminologyRef();
          fromRef.setAbbreviation(valueSet.getTitle().split("-")[0]);
          fromRef.setUri(include.getSystem());
          fromRef.setPublisher(valueSet.getPublisher());
          fromRef.setVersion(valueSet.getVersion());

          subset.setFromTerminology(fromRef.getAbbreviation());
          subset.setFromPublisher(fromRef.getPublisher());
          subset.setFromVersion(fromRef.getVersion());
          // Save the URI that goes with the from stuff.
          subset.getAttributes().put("fhirIncludesUri", fromRef.getUri());
        }
      }

      // Store experimental and identifier in attributes if present
      if (valueSet.hasExperimental()) {
        if (subset.getAttributes() == null) {
          subset.setAttributes(new HashMap<>());
        }
        subset.getAttributes().put(Subset.Attributes.fhirExperimental.name(),
            Boolean.toString(valueSet.getExperimental()));
      }

      subset.setCategory("ValueSet");
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
            throw FhirUtilityR4.exception("Unable to determine system of value set",
                IssueType.INVALID, HttpServletResponse.SC_EXPECTATION_FAILED);
          }

          final TerminologyRef ref = new TerminologyRef();
          ref.setAbbreviation(valueSet.getTitle().split("-")[0]);
          ref.setUri(includes.getSystem());
          ref.setPublisher(subset.getPublisher());
          ref.setVersion(subset.getVersion());

          for (final org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent c : includes
              .getConcept()) {

            if (c.getCode() == null) {
              LOGGER.warn("    Value set includes component reference without a code = " + c);
              continue;
            }

            final Concept existingConcept = TerminologyUtility.getConcept(service,
                ref.getAbbreviation(), ref.getPublisher(), ref.getVersion(), c.getCode());

            final SubsetMember m = new SubsetMember();
            m.setId(UUID.randomUUID().toString());
            m.setActive(true);
            m.setTerminology(ref.getAbbreviation());
            m.setPublisher(ref.getPublisher());
            m.setVersion(ref.getVersion());
            m.setCode(c.getCode());
            m.setName(
                existingConcept == null ? "Unable to determine name" : existingConcept.getName());
            m.setSubset(subsetRef);
            members.add(m);

            // Add subsetRef to the concept corresponding to this
            if (existingConcept != null) {
              existingConcept.getSubsets().add(subsetRef);
              service.update(Concept.class, existingConcept.getId(), existingConcept);
            }
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
            final TerminologyRef ref = new TerminologyRef();
            ref.setAbbreviation(valueSet.getTitle().split("-")[0]);
            ref.setUri(c.getSystem());
            ref.setPublisher(subset.getPublisher());
            ref.setVersion(subset.getVersion());
            map.put(c.getSystem(), ref);
          }
          final TerminologyRef ref = map.get(c.getSystem());

          final Concept existingConcept = TerminologyUtility.getConcept(service,
              ref.getAbbreviation(), ref.getPublisher(), ref.getVersion(), c.getCode());

          final SubsetMember m = new SubsetMember();
          m.setId(UUID.randomUUID().toString());
          m.setActive(true);
          m.setTerminology(ref.getAbbreviation());
          m.setPublisher(ref.getPublisher());
          m.setVersion(ref.getVersion());
          m.setCode(c.getCode());
          m.setName(
              existingConcept == null ? "Unable to determine name" : existingConcept.getName());
          m.setSubset(subsetRef);
          members.add(m);

          // Add subsetRef to the concept corresponding to this
          if (existingConcept != null) {
            existingConcept.getSubsets().add(subsetRef);
            service.update(Concept.class, existingConcept.getId(), existingConcept);
          }

        }
      }
      if (!members.isEmpty()) {
        service.addBulk(SubsetMember.class, new ArrayList<>(members));
      }

      LOGGER.info("  member count: {}", members.size());
      LOGGER.info("  duration: {} ms", (System.currentTimeMillis() - startTime));

      // Set listener to 100%
      listener.updateProgress(new ProgressEvent(100));
      subset.getAttributes().put("loaded", "true");
      service.update(Subset.class, subset.getId(), subset);
      return FhirUtilityR4.toR4ValueSet(subset, null, false);

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
   * @param listener the listener
   * @return the string
   * @throws Exception the exception
   */
  private static org.hl7.fhir.r5.model.ValueSet indexValueSetR5(
    final EntityRepositoryService service, final org.hl7.fhir.r5.model.ValueSet valueSet,
    final ProgressListener listener) throws Exception {

    final long startTime = System.currentTimeMillis();

    try {

      LOGGER.info("Indexing ValueSet R4 {} = {}", valueSet.getTitle(), valueSet.getUrl());
      // Basic checks
      // Validate required fields
      if (valueSet.getUrl() == null) {
        throw FhirUtilityR4.exception("ValueSet.url is required", IssueType.INVALID,
            HttpServletResponse.SC_BAD_REQUEST);
      }

      // check for existing
      final String abbreviation = valueSet.getTitle();
      final String publisher = valueSet.getPublisher();
      final String version = valueSet.getVersion();
      Subset subset = findSubset(service, abbreviation, publisher, version);
      if (subset != null) {
        throw FhirUtilityR4.exception(
            "Can not create multiple ValueSet resources the same title, publisher,"
                + " and version. duplicate = " + subset.getId(),
            IssueType.INVALID, HttpServletResponse.SC_CONFLICT);
      }

      subset = new Subset();
      subset.getAttributes().put("loaded", "false");
      // The HAPI Plan server @Create method blanks the identifier on sending a
      // code system in. Always create a new identifier.
      String id = UUID.randomUUID().toString();
      String originalId = valueSet.getIdElement().getIdPart();
      if (!StringUtility.isEmpty(originalId)) {
        idMap.put(originalId, id);
        subset.getAttributes().put("originalId", originalId);
      }
      subset.setId(id);
      subset.setActive(true);
      subset.setUri(valueSet.getUrl());

      // Use the identifier as the code, otherwise use the id
      // NOTE: termhub generated files will have a single id
      // with a system matching this value.
      for (final org.hl7.fhir.r5.model.Identifier identifier : valueSet.getIdentifier()) {
        if (JSON_SUBSET_CODE.equals(identifier.getSystem())) {
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

          final TerminologyRef fromRef = new TerminologyRef();
          fromRef.setAbbreviation(valueSet.getTitle().split("-")[0]);
          fromRef.setUri(include.getSystem());
          fromRef.setPublisher(valueSet.getPublisher());
          fromRef.setVersion(valueSet.getVersion());

          subset.setFromTerminology(fromRef.getAbbreviation());
          subset.setFromPublisher(fromRef.getPublisher());
          subset.setFromVersion(fromRef.getVersion());
        }
      }

      // Store experimental and identifier in attributes if present
      if (valueSet.hasExperimental()) {
        if (subset.getAttributes() == null) {
          subset.setAttributes(new HashMap<>());
        }
        subset.getAttributes().put(Subset.Attributes.fhirExperimental.name(),
            Boolean.toString(valueSet.getExperimental()));
      }

      subset.setCategory("ValueSet");
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
            throw FhirUtilityR4.exception("Unable to determine system of value set",
                IssueType.INVALID, HttpServletResponse.SC_EXPECTATION_FAILED);
          }

          final TerminologyRef ref = new TerminologyRef();
          ref.setAbbreviation(valueSet.getTitle().split("-")[0]);
          ref.setUri(includes.getSystem());
          ref.setPublisher(subset.getPublisher());
          ref.setVersion(subset.getVersion());

          for (final org.hl7.fhir.r5.model.ValueSet.ConceptReferenceComponent c : includes
              .getConcept()) {

            if (c.getCode() == null) {
              LOGGER.warn("    Value set includes component reference without a code = " + c);
              continue;
            }

            final Concept existingConcept = TerminologyUtility.getConcept(service,
                ref.getAbbreviation(), ref.getPublisher(), ref.getVersion(), c.getCode());

            final SubsetMember m = new SubsetMember();
            m.setId(UUID.randomUUID().toString());
            m.setActive(true);
            m.setTerminology(ref.getAbbreviation());
            m.setPublisher(ref.getPublisher());
            m.setVersion(ref.getVersion());
            m.setCode(c.getCode());
            m.setName(
                existingConcept == null ? "Unable to determine name" : existingConcept.getName());
            m.setSubset(subsetRef);
            members.add(m);

            // Add subsetRef to the concept corresponding to this
            if (existingConcept != null) {
              existingConcept.getSubsets().add(subsetRef);
              service.update(Concept.class, existingConcept.getId(), existingConcept);
            }

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

          if (!map.containsKey(c.getSystem())) {
            final TerminologyRef ref = new TerminologyRef();
            ref.setAbbreviation(valueSet.getTitle().split("-")[0]);
            ref.setUri(c.getSystem());
            ref.setPublisher(subset.getPublisher());
            ref.setVersion(subset.getVersion());
            map.put(c.getSystem(), ref);
          }
          final TerminologyRef ref = map.get(c.getSystem());

          final Concept existingConcept = TerminologyUtility.getConcept(service,
              ref.getAbbreviation(), ref.getPublisher(), ref.getVersion(), c.getCode());

          final SubsetMember subsetMember = new SubsetMember();
          subsetMember.setId(UUID.randomUUID().toString());
          subsetMember.setActive(true);
          subsetMember.setTerminology(ref.getAbbreviation());
          subsetMember.setPublisher(ref.getPublisher());
          subsetMember.setVersion(ref.getVersion());
          subsetMember.setCode(c.getCode());
          subsetMember.setName(
              existingConcept == null ? "Unable to determine name" : existingConcept.getName());
          subsetMember.setSubset(subsetRef);
          members.add(subsetMember);

          // Add subsetRef to the concept corresponding to this
          if (existingConcept != null) {
            service.addField(Concept.class, existingConcept.getId(), subsetRef, "subsets");
          }

        }
      }
      if (!members.isEmpty()) {
        service.addBulk(SubsetMember.class, new ArrayList<>(members));
      }

      LOGGER.info("  member count: {}", members.size());
      LOGGER.info("  duration: {} ms", (System.currentTimeMillis() - startTime));

      // Set listener to 100%
      listener.updateProgress(new ProgressEvent(100));
      subset.getAttributes().put("loaded", "true");
      service.update(Subset.class, subset.getId(), subset);
      return FhirUtilityR5.toR5ValueSet(subset, null, false);

    } catch (final Exception e) {
      LOGGER.error("Error indexing value set", e);
      throw e;
    }

  }

  /**
   * Does value set exist.
   *
   * @param service the service
   * @param abbreviation the abbreviation
   * @param publisher the publisher
   * @param version the version
   * @return the subset
   * @throws Exception the exception
   */
  private static Subset findSubset(final EntityRepositoryService service, final String abbreviation,
    final String publisher, final String version) throws Exception {

    final SearchParameters searchParams = new SearchParameters();
    searchParams
        .setQuery(TerminologyUtility.getTerminologyAbbrQuery(abbreviation, publisher, version));
    final ResultList<Subset> subset = service.find(searchParams, Subset.class);

    return (subset.getItems().isEmpty()) ? null : subset.getItems().get(0);
  }
}
