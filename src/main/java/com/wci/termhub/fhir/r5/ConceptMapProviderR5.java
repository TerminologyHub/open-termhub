/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r5;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.ConceptMap;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.OperationOutcome.IssueType;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.UriType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wci.termhub.fhir.rest.r5.FhirUtilityR5;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.model.Mapping;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.StringUtility;

import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
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
 * The ConceptMap provider.
 */
@Component
public class ConceptMapProviderR5 implements IResourceProvider {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(ConceptMapProviderR5.class);

	/** The search service. */
	@Autowired
	EntityRepositoryService searchService;

	/**
	 * Gets the concept map.
	 *
	 * @param request the request
	 * @param details the details
	 * @param id      the id
	 * @return the concept map
	 * @throws Exception the exception
	 */
	@Read()
	public ConceptMap getConceptMap(final HttpServletRequest request, final ServletRequestDetails details,
			@IdParam final IdType id) throws Exception {

		FhirUtility.authorize(request);
		try {
			final List<ConceptMap> candidates = new ArrayList<>(); // findCandidates();
			for (final ConceptMap map : candidates) {
				if (id != null && id.getIdPart().equals(map.getId())) {
					return map;
				}
			}
			throw FhirUtilityR5.exception("Concept map not found = " + (id == null ? "null" : id.getIdPart()),
					IssueType.NOTFOUND, 404);

		} catch (final FHIRServerResponseException e) {
			throw e;
		} catch (final Exception e) {
			logger.error("Unexpected FHIR error", e);
			throw FhirUtilityR5.exception("Failed to load value set", OperationOutcome.IssueType.EXCEPTION, 500);
		}
	}

	/**
	 * Find concept maps.
	 *
	 * <pre>
	 * Parameters for all resources
	 *   used: _id
	 *   not used: _content, _filter, _has, _in, _language, _lastUpdated,
	 *             _list, _profile, _query, _security, _source, _tag, _text, _type
	 * https://hl7.org/fhir/R5/conceptmap.html (see Search Parameters)
	 * The following parameters in the registry are not used
	 * &#64;OptionalParam(name="context") TokenParam context,
	 * &#64;OptionalParam(name="context-quantity") QuantityParam contextQuantity,
	 * &#64;OptionalParam(name="context-type") String contextType,
	 * &#64;OptionalParam(name="context-type-quantity") QuantityParam contextTypeQuantity,
	 * &#64;OptionalParam(name="context-type-value") String contextTypeValue,
	 * &#64;OptionalParam(name="date") DateParam date,
	 * &#64;OptionalParam(name="dependsOn") String dependsOn,
	 * &#64;OptionalParam(name="identifier") StringParam identifier,
	 * &#64;OptionalParam(name="jurisdiction") StringParam jurisdiction,
	 * &#64;OptionalParam(name="other") ReferenceParam other,
	 * &#64;OptionalParam(name="product") String product,
	 * &#64;OptionalParam(name="source") ReferenceParam source,
	 * &#64;OptionalParam(name="source-code") TokenParam source,
	 * &#64;OptionalParam(name="source-uri") ReferenceParamsourceUri,
	 * &#64;OptionalParam(name="source") ReferenceParam source,
	 * &#64;OptionalParam(name="status") String status,
	 * &#64;OptionalParam(name="target") ReferenceParam source,
	 * &#64;OptionalParam(name="target-code") TokenParam source,
	 * &#64;OptionalParam(name="target-system") String sourceSystem,
	 * &#64;OptionalParam(name="target-uri") ReferenceParamsourceUri,
	 * </pre>
	 *
	 * @param request        the request
	 * @param details        the details
	 * @param id             the id
	 * @param date           the date
	 * @param description    the description
	 * @param identifier     the identifier
	 * @param name           the name
	 * @param publisher      the publisher
	 * @param sourceCode     the source code
	 * @param sourceSystem   the source system
	 * @param sourceScopeUri the source scope uri
	 * @param targetCode     the target code
	 * @param targetSystem   the target system
	 * @param targetScopeUri the target scope uri
	 * @param title          the title
	 * @param url            the url
	 * @param version        the version
	 * @param count          the count
	 * @param offset         the offset
	 * @return the list
	 * @throws Exception the exception
	 */
	@Search
	public Bundle findConceptMaps(final HttpServletRequest request, final ServletRequestDetails details,
			@OptionalParam(name = "_id") final TokenParam id, @OptionalParam(name = "date") final DateRangeParam date,
			@OptionalParam(name = "description") final StringParam description,
			@OptionalParam(name = "identifier") final StringParam identifier,
			@OptionalParam(name = "name") final StringParam name,
			@OptionalParam(name = "publisher") final StringParam publisher,
			@OptionalParam(name = "source-code") final TokenParam sourceCode,
			@OptionalParam(name = "source-group-system") final UriParam sourceSystem,
			@OptionalParam(name = "source-scope-uri") final UriParam sourceScopeUri,
			@OptionalParam(name = "target-code") final TokenParam targetCode,
			@OptionalParam(name = "target-group-system") final UriParam targetSystem,
			@OptionalParam(name = "target-scope-uri") final UriParam targetScopeUri,
			@OptionalParam(name = "title") final StringParam title, @OptionalParam(name = "url") final StringParam url,
			@OptionalParam(name = "version") final StringParam version,
			@Description(shortDefinition = "Number of entries to return") @OptionalParam(name = "_count") final NumberParam count,
			@Description(shortDefinition = "Start offset, used when reading a next page") @OptionalParam(name = "_offset") final NumberParam offset)
			throws Exception {

		FhirUtility.authorize(request);

		try {

			FhirUtilityR5.notSupportedSearchParams(request);

			// Lookup source codes if they're specified
			final Set<String> mapsetsMatchingSourceCodes = new HashSet<>();
			if (sourceCode != null) {
				if (sourceSystem != null) {
					mapsetsMatchingSourceCodes.addAll(searchService
							.find(new SearchParameters(
									"fromTerminology:" + FhirUtility.lookupTerminology(sourceSystem.getValue())
											+ " AND from.code:" + StringUtility.escapeQuery(sourceCode.getValue()),
									null, 1000, null, null), Mapping.class)
							.getItems().stream().map(m -> m.getMapset().getCode()).collect(Collectors.toSet()));
				} else {
					mapsetsMatchingSourceCodes.addAll(searchService
							.find(new SearchParameters("from.code:" + StringUtility.escapeQuery(sourceCode.getValue()),
									null, 1000, null, null), Mapping.class)
							.getItems().stream().map(m -> m.getMapset().getCode()).collect(Collectors.toSet()));
				}
			}
			// Lookup target codes if they're specified
			final Set<String> mapsetsMatchingTargetCodes = new HashSet<>();
			if (targetCode != null) {
				if (targetSystem != null) {
					mapsetsMatchingTargetCodes.addAll(searchService
							.find(new SearchParameters(
									"toTerminology:" + FhirUtility.lookupTerminology(targetSystem.getValue())
											+ " AND to.code:" + StringUtility.escapeQuery(targetCode.getValue()),
									null, 1000, null, null), Mapping.class)
							.getItems().stream().map(m -> m.getMapset().getCode()).collect(Collectors.toSet()));
				} else {
					mapsetsMatchingTargetCodes.addAll(searchService
							.find(new SearchParameters("to.code:" + StringUtility.escapeQuery(targetCode.getValue()),
									null, 1000, null, null), Mapping.class)
							.getItems().stream().map(m -> m.getMapset().getCode()).collect(Collectors.toSet()));
				}
			}

			final List<ConceptMap> list = new ArrayList<>();
			final List<ConceptMap> candidates = new ArrayList<>(); // findCandidates();
			for (final ConceptMap cm : candidates) {

				// Skip non-matching
				if ((id != null && !id.getValue().equals(cm.getId()))
						|| (url != null && !url.getValue().equals(cm.getUrl()))) {
					continue;
				}
				if (identifier != null && !cm.getIdentifier().isEmpty()
						&& !FhirUtility.compareString(identifier, cm.getIdentifier().get(0).getValue())) {
					logger.info("  SKIP identifier mismatch = " + cm.getIdentifier().get(0).getValue());
					continue;
				}
				if (date != null && !FhirUtility.compareDateRange(date, cm.getDate())) {
					logger.info("  SKIP date mismatch = " + cm.getDate());
					continue;
				}
				if (description != null && !FhirUtility.compareString(description, cm.getDescription())) {
					logger.info("  SKIP description mismatch = " + cm.getDescription());
					continue;
				}
				if (name != null && !FhirUtility.compareString(name, cm.getName())) {
					logger.info("  SKIP name mismatch = " + cm.getName());
					continue;
				}
				if (publisher != null && !FhirUtility.compareString(publisher, cm.getPublisher())) {
					logger.info("  SKIP publisher mismatch = " + cm.getPublisher());
					continue;
				}
				if (title != null && !FhirUtility.compareString(title, cm.getTitle())) {
					logger.info("  SKIP title mismatch = " + cm.getTitle());
					continue;
				}
				if (version != null && !FhirUtility.compareString(version, cm.getVersion())) {
					logger.info("  SKIP version mismatch = " + cm.getVersion());
					continue;
				}
				if (sourceSystem != null
						&& !cm.getSourceScopeUriType().getValue().startsWith(sourceSystem.getValue())) {
					logger.info("  SKIP sourceSystem mismatch = " + cm.getSourceScopeUriType().getValue());
					continue;
				}
				if (targetSystem != null
						&& !cm.getTargetScopeUriType().getValue().startsWith(targetSystem.getValue())) {
					logger.info("  SKIP targetSystem mismatch = " + cm.getTargetScopeUriType().getValue());
					continue;
				}
				if (sourceScopeUri != null
						&& !sourceScopeUri.getValue().equals(cm.getSourceScopeUriType().getValue())) {
					logger.info("  SKIP sourceSystem mismatch = " + cm.getSourceScopeUriType().getValue());
					continue;
				}
				if (targetScopeUri != null
						&& !targetScopeUri.getValue().equals(cm.getTargetScopeUriType().getValue())) {
					logger.info("  SKIP targetScopeUri mismatch = " + cm.getTargetScopeUriType().getValue());
					continue;
				}
				if (sourceCode != null && !mapsetsMatchingSourceCodes.contains(sourceCode.getValue())) {
					logger.info("  SKIP source-code not found = " + sourceCode.getValue());
					continue;
				}
				if (targetCode != null && !mapsetsMatchingTargetCodes.contains(targetCode.getValue())) {
					logger.info("  SKIP target-code not found = " + targetCode.getValue());
					continue;
				}
				list.add(cm);
			}

			// Sort the results by uri.
			final List<ConceptMap> list2 = list.stream().sorted((a, b) -> a.getUrl().compareTo(b.getUrl()))
					.collect(Collectors.toList());
			return FhirUtilityR5.makeBundle(request, list2, count, offset);

		} catch (final FHIRServerResponseException e) {
			throw e;
		} catch (final Exception e) {
			logger.error("Unexpected FHIR error", e);
			throw FhirUtilityR5.exception("Failed to load value set", OperationOutcome.IssueType.EXCEPTION, 500);
		}
	}

	/**
	 * Perform the lookup in the instance map.
	 *
	 * <pre>
	 * Parameters for all resources
	 *   used: _id
	 *   not used: _content, _filter, _has, _in, _language, _lastUpdated,
	 *             _list, _profile, _query, _security, _source, _tag, _text, _type
	 * https://hl7.org/fhir/R5/conceptmap-operation-translate.html
	 * The following parameters in the operation are not used
	 * &#64;OptionalParam(name="dependency") ?? dependency
	 * &#64;OperationParam(name = "conceptMap") ConceptMap conceptMap,
	 * &#64;OperationParam(name = "conceptMapVersion") String conceptMapVersion,
	 * &#64;OperationParam(name = "source") UriType source,
	 * &#64;OperationParam(name = "target") String target,
	 * &#64;OperationParam(name = "reverse") BooleanType reverse
	 * </pre>
	 *
	 * @param request           the request
	 * @param response          the response
	 * @param details           the details
	 * @param id                the id
	 * @param url               the url
	 * @param conceptMapVersion the concept map version
	 * @param sourceCode        the source code
	 * @param sourceSystem      the source system
	 * @param sourceVersion     the version
	 * @param sourceScope       the source scope
	 * @param sourceCoding      the source coding
	 * @param targetCode        the target code
	 * @param targetCoding      the target coding
	 * @param targetScope       the target scope
	 * @param targetSystem      the target system
	 * @return the parameters
	 * @throws Exception the exception
	 */
	@Operation(name = "$translate", idempotent = true)
	public Parameters translateInstance(final HttpServletRequest request, final HttpServletResponse response,
			final ServletRequestDetails details, @IdParam final IdType id,
			@OperationParam(name = "url") final UriType url,
			@OperationParam(name = "conceptMapVersion") final StringType conceptMapVersion,
			@OperationParam(name = "sourceCode") final CodeType sourceCode,
			@OperationParam(name = "system") final UriType sourceSystem,
			@OperationParam(name = "version") final StringType sourceVersion,
			@OperationParam(name = "sourceScope") final UriType sourceScope,
			@OperationParam(name = "sourceCoding") final Coding sourceCoding,
			@OperationParam(name = "targetCode") final CodeType targetCode,
			@OperationParam(name = "targetCoding") final Coding targetCoding,
			@OperationParam(name = "targetScope") final UriType targetScope,
			@OperationParam(name = "targetSystem") final UriType targetSystem) throws Exception {

		FhirUtility.authorize(request);

		// Reject post
		if (request.getMethod().equals("POST")) {
			throw FhirUtilityR5.exception("POST method not supported for $translate", IssueType.NOTSUPPORTED, 405);
		}

		// Reject post
		if (request.getMethod().equals("POST")) {
			throw FhirUtilityR5.exception("POST method not supported for $translate", IssueType.NOTSUPPORTED, 405);
		}

		FhirUtilityR5.requireExactlyOneOf("sourceCode", sourceCode, "sourceCoding", sourceCoding, "targetCode",
				targetCode, "targetCoding", targetCoding);
		FhirUtilityR5.mutuallyExclusive("targetScope", targetScope, "targetSystem", targetSystem);

		try {

			final String sourceCodeStr = FhirUtilityR5.getCode(sourceCode, sourceCoding);
			final String targetCodeStr = FhirUtilityR5.getCode(targetCode, targetCoding);
			final Terminology sourceTerminology = FhirUtilityR5.getTerminology(searchService, null, sourceCode,
					"sourceSystem", sourceSystem, sourceVersion, sourceCoding, true);
			final Terminology targetTerminology = FhirUtilityR5.getTerminology(searchService, null, targetCode,
					"targetSystem", targetSystem, sourceVersion, targetCoding, true);

			// Get all mapsets and then restrict
			final List<ConceptMap> candidates = new ArrayList<>(); // findCandidates();
			for (final ConceptMap cm : candidates) {

				// Skip non-matching
				if ((id != null && !id.getIdPart().equals(cm.getId()))
						|| (url != null && !url.getValue().equals(cm.getUrl()))) {
					continue;
				}

				if (conceptMapVersion != null && !conceptMapVersion.getValue().equals(cm.getVersion())) {
					logger.info("  SKIP concept map version mismatch = " + cm.getVersion());
					continue;
				}
				if (sourceScope != null && !sourceScope.getValue().equals(cm.getSourceScopeUriType().getValue())) {
					logger.info("  SKIP sourceScope mismatch = " + sourceScope.getValue() + ", "
							+ cm.getSourceScopeUriType().getValue());
					continue;
				}
				if (targetScope != null && !targetScope.getValue().equals(cm.getTargetScopeUriType().getValue())) {
					logger.info("  SKIP targetScope mismatch = " + targetScope.getValue() + ", "
							+ cm.getTargetScopeUriType().getValue());
					continue;
				}
				if (targetSystem != null
						&& !cm.getTargetScopeUriType().getValue().startsWith(targetSystem.getValue())) {
					logger.info("  SKIP targetSystem mismatch = " + targetSystem.getValue() + ", "
							+ cm.getTargetScopeUriType().getValue());
					continue;
				}
				final boolean reverse = sourceCodeStr == null;
				return translateHelper(ModelUtility.asList(cm), sourceTerminology, targetTerminology,
						reverse ? targetCodeStr : sourceCodeStr, reverse);
			}

			throw FhirUtilityR5.exception("Concept map not found = " + url, IssueType.NOTFOUND, 404);

		} catch (final FHIRServerResponseException e) {
			throw e;
		} catch (final Exception e) {
			logger.error("Unexpected FHIR error", e);
			throw FhirUtilityR5.exception("Failed to translate concept map", OperationOutcome.IssueType.EXCEPTION, 500);
		}
	}

	/**
	 * Perform the lookup in the implicit map.
	 *
	 * <pre>
	 * Parameters for all resources
	 *   used: _id
	 *   not used: _content, _filter, _has, _in, _language, _lastUpdated,
	 *             _list, _profile, _query, _security, _source, _tag, _text, _type
	 * https://hl7.org/fhir/R5/conceptmap-operation-translate.html
	 * The following parameters in the operation are not used
	 * &#64;OptionalParam(name="dependency") ?? dependency
	 * &#64;OperationParam(name = "conceptMap") ConceptMap conceptMap,
	 * &#64;OperationParam(name = "conceptMapVersion") String conceptMapVersion,
	 * &#64;OperationParam(name = "source") String source,
	 * &#64;OperationParam(name = "target") String target,
	 * &#64;OperationParam(name = "reverse") BooleanType reverse
	 * </pre>
	 *
	 * @param request           the request
	 * @param response          the response
	 * @param details           the details
	 * @param url               the url
	 * @param conceptMapVersion the concept map version
	 * @param sourceCode        the code
	 * @param sourceSystem      the source system
	 * @param sourceVersion     the version
	 * @param sourceScope       the source scope
	 * @param sourceCoding      the coding
	 * @param targetCode        the target code
	 * @param targetCoding      the target coding
	 * @param targetScope       the target scope
	 * @param targetSystem      the target system
	 * @return the parameters
	 * @throws Exception the exception
	 */
	@Operation(name = "$translate", idempotent = true)
	public Parameters translateImplicit(final HttpServletRequest request, final HttpServletResponse response,
			final ServletRequestDetails details, @OperationParam(name = "url") final UriType url,
			@OperationParam(name = "conceptMapVersion") final StringType conceptMapVersion,
			@OperationParam(name = "sourceCode") final CodeType sourceCode,
			@OperationParam(name = "system") final UriType sourceSystem,
			@OperationParam(name = "version") final StringType sourceVersion,
			@OperationParam(name = "sourceScope") final UriType sourceScope,
			@OperationParam(name = "sourceCoding") final Coding sourceCoding,
			@OperationParam(name = "targetCode") final CodeType targetCode,
			@OperationParam(name = "targetCoding") final Coding targetCoding,
			@OperationParam(name = "targetScope") final UriType targetScope,
			@OperationParam(name = "targetSystem") final UriType targetSystem) throws Exception {

		FhirUtility.authorize(request);

		// Reject post
		if (request.getMethod().equals("POST")) {
			throw FhirUtilityR5.exception("POST method not supported for $translate", IssueType.NOTSUPPORTED, 405);
		}

		FhirUtilityR5.requireExactlyOneOf("sourceCode", sourceCode, "sourceCoding", sourceCoding, "targetCode",
				targetCode, "targetCoding", targetCoding);
		FhirUtilityR5.mutuallyExclusive("targetScope", targetScope, "targetSystem", targetSystem);

		try {

			final String sourceCodeStr = FhirUtilityR5.getCode(sourceCode, sourceCoding);
			final String targetCodeStr = FhirUtilityR5.getCode(targetCode, targetCoding);
			final Terminology sourceTerminology = FhirUtilityR5.getTerminology(searchService, null, sourceCode,
					"sourceSystem", sourceSystem, sourceVersion, sourceCoding, true);
			final Terminology targetTerminology = FhirUtilityR5.getTerminology(searchService, null, targetCode,
					"targetSystem", targetSystem, null, targetCoding, true);

			// Get all mapsets and then restrict
			final List<ConceptMap> candidates = new ArrayList<>(); // findCandidates();
			final List<ConceptMap> list = new ArrayList<>();
			for (final ConceptMap cm : candidates) {

				// Skip non-matching
				if ((url != null && !url.getValue().equals(cm.getUrl()))) {
					continue;
				}
				if (conceptMapVersion != null && !conceptMapVersion.getValue().equals(cm.getVersion())) {
					logger.info("  SKIP concept map version mismatch = " + cm.getVersion());
					continue;
				}
				if (sourceScope != null && !sourceScope.getValue().equals(cm.getSourceScopeUriType().getValue())) {
					logger.info("  SKIP sourceScope mismatch = " + sourceScope.getValue() + ", "
							+ cm.getSourceScopeUriType().getValue());
					continue;
				}
				if (targetScope != null && !targetScope.getValue().equals(cm.getTargetScopeUriType().getValue())) {
					logger.info("  SKIP targetScope mismatch = " + targetScope.getValue() + ", "
							+ cm.getTargetScopeUriType().getValue());
					continue;
				}
				if (targetSystem != null
						&& !cm.getTargetScopeUriType().getValue().startsWith(targetSystem.getValue())) {
					logger.info("  SKIP targetSystem mismatch = " + targetSystem.getValue() + ", "
							+ cm.getTargetScopeUriType().getValue());
					continue;
				}

				list.add(cm);
			}

			if (list.size() > 0) {
				final boolean reverse = sourceCodeStr == null;
				return translateHelper(list, sourceTerminology, targetTerminology,
						reverse ? targetCodeStr : sourceCodeStr, reverse);
			}

			// We can either fail with params indicating no result, or with 404
			// final Parameters parameters = new Parameters();
			// parameters.addParameter("result", false);
			// parameters.addParameter("message", "No concept maps found matching specified
			// critiera");
			// return parameters;

			throw FhirUtilityR5.exception("Concept map not found = " + url, IssueType.NOTFOUND, 404);

		} catch (final FHIRServerResponseException e) {
			throw e;
		} catch (final Exception e) {
			logger.error("Unexpected FHIR error", e);
			throw FhirUtilityR5.exception("Failed to translate concept map", OperationOutcome.IssueType.EXCEPTION, 500);
		}
	}

	/**
	 * Gets the resource type.
	 *
	 * @return the resource type
	 */
	/* see superclass */
	@Override
	public Class<ConceptMap> getResourceType() {
		return ConceptMap.class;
	}

	/**
	 * Find candidates.
	 *
	 * @return the list
	 * @throws Exception the exception
	 */
	public List<ConceptMap> findCandidates() throws Exception {

		// try {
		//
		// // Lookup and filter mapsets
		// final List<ConceptMap> list = new ArrayList<>();
		// for (final Mapset mapset : FhirUtility.lookupMapsets(searchService)) {
		// final ConceptMap cm = FhirUtilityR5.toR5(mapset);
		// list.add(cm);
		// }
		// return list;
		//
		// } catch (final FHIRServerResponseException e) {
		// throw e;
		// } catch (final Exception e) {
		// logger.error("Unexpected FHIR error", e);
		// throw FhirUtilityR5.exception("Failed to find concept maps",
		// OperationOutcome.IssueType.EXCEPTION, 500);
		// }
		return new ArrayList<>();
	}

	/**
	 * Translate helper.
	 *
	 * @param maps              the maps
	 * @param sourceTerminology the source terminology
	 * @param targetTerminology the target terminology
	 * @param code              the code
	 * @param reverse           the reverse
	 * @return the parameters
	 * @throws Exception the exception
	 */
	private Parameters translateHelper(final List<ConceptMap> maps, final Terminology sourceTerminology,
			final Terminology targetTerminology, final String code, final boolean reverse) throws Exception {

		final Parameters parameters = new Parameters();
		final List<ParametersParameterComponent> matches = new ArrayList<>();

		// {
		// "resourceType": "Parameters",
		// "parameter": [ {
		// "name": "result",
		// "valueBoolean": true
		// }, {
		// "name": "message",
		// "valueString": "Please observe the following map advice. Group:1, Priority:1,
		// Rule:TRUE,
		// Advice:'ALWAYS N28.9', Map Category:'Map source concept is properly
		// classified'."
		// }, {
		// "name": "match",
		// "part": [ {
		// "name": "equivalence",
		// "valueCode": "unmatched"
		// }, {
		// "name": "concept",
		// "valueCoding": {
		// "system": "http://hl7.org/fhir/sid/icd-10",
		// "code": "N28.9"
		// }
		// }, {
		// "name": "source",
		// "valueString":
		// "http://snomed.info/sct/900000000000207008/version/20240101?fhir_cm=447562003"
		// } ]
		// } ]
		// }

		for (final ConceptMap map : maps) {
			// Get the identifier from the map
			final String mapsetCode = map.getIdentifier().get(0).getValue();

			final List<Mapping> mappings = searchService.find(new SearchParameters(StringUtility.composeQuery("AND",
					// code clause
					(reverse ? "to.code:" : "from.code:") + StringUtility.escapeQuery(code),
					// terminology clause (null if null) - no reversing
					sourceTerminology == null ? null : ("from.terminology:" + sourceTerminology.getAbbreviation()),
					targetTerminology == null ? null : ("to.terminology:" + targetTerminology.getAbbreviation()),
					// mapset clauses
					// TODO: how do we include publisher?, needs to be in concept map
					"mapset.abbreviation:" + map.getTitle(), "mapset.version:" + map.getVersion(),
					"mapset.code:" + mapsetCode), null, 1000, null, null), Mapping.class).getItems();
			if (mappings.size() > 0) {
				for (final Mapping mapping : mappings) {
					final ParametersParameterComponent match = new ParametersParameterComponent();
					match.setName("match");
					match.addPart().setName("equivalence").setValue(new StringType("inexact"));
					if (reverse) {
						match.addPart().setName("concept")

								.setValue(new Coding()
										.setSystem(FhirUtility.getCodeSystemUri(searchService, mapping.getFrom()))
										.setCode(mapping.getFrom().getCode()).setDisplay(mapping.getFrom().getName()));
					} else {
						// Skip blank mappings
						if (mapping.getTo().getCode().isEmpty()) {
							continue;
						}
						match.addPart().setName("concept")
								.setValue(new Coding()
										.setSystem(FhirUtility.getCodeSystemUri(searchService, mapping.getTo()))
										.setCode(mapping.getTo().getCode()).setDisplay(mapping.getTo().getName()));
					}
					match.addPart().setName("source").setValue(new StringType(map.getUrl()));
					matches.add(match);
				}
			}

		}

		// If matches, proceed
		if (matches.size() > 0) {
			parameters.addParameter("result", true);
			for (final ParametersParameterComponent match : matches) {
				parameters.addParameter(match);
			}
			return parameters;
		}

		// OR:::

		// {
		// "resourceType": "Parameters",
		// "parameter": [ {
		// "name": "result",
		// "valueBoolean": false
		// }, {
		// "name": "message",
		// "valueString": "No mapping found for code '90708003', system
		// 'http://snomed.info/sct'."
		// } ]
		// }
		parameters.addParameter("result", false);
		parameters.addParameter("message", "No mapping found matching specified critiera = "
				+ maps.stream().map(m -> m.getId()).collect(Collectors.toSet()));
		return parameters;
	}
}
