/*
 *
 */
package com.wci.termhub.rest;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.wci.termhub.Application;
import com.wci.termhub.handler.QueryBuilder;
import com.wci.termhub.model.AuthContext;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.ConceptTreePosition;
import com.wci.termhub.model.HasId;
import com.wci.termhub.model.HealthCheck;
import com.wci.termhub.model.IncludeParam;
import com.wci.termhub.model.Mapping;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.ResultListConcept;
import com.wci.termhub.model.ResultListConceptRelationship;
import com.wci.termhub.model.ResultListConceptTreePosition;
import com.wci.termhub.model.ResultListMapping;
import com.wci.termhub.model.ResultListMapset;
import com.wci.termhub.model.ResultListMetadata;
import com.wci.termhub.model.ResultListTerm;
import com.wci.termhub.model.ResultListTerminology;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.service.RootServiceRestImpl;
import com.wci.termhub.util.AdhocUtility;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.PropertyUtility;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.TerminologyUtility;
import com.wci.termhub.util.TimerCache;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MediaType;

/**
 * Reference implementation of {@link TerminologyServiceRestOld}.
 */
@SecurityScheme(name = "bearerAuth", description = "JWT authentication with bearer token", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "Bearer [token]")
@OpenAPIDefinition(info = @Info(title = "Terminology Hub Terminology Terminology API", version = "1.0.0", description = "API documentation for the interacting with terminologies and concepts. "
		+ "<p>For a guided tour of using this API, see our github project "
		+ "<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes\">"
		+ "https://github.com/terminologyhub/termhub-in-5-minutes</a></p>", contact = @Contact(name = "API Support", url = "https://www.terminologyhub.com", email = "info@terminologyhub.com")), tags = {
				@Tag(name = "terminology", description = "Terminology service endpoints"),
				@Tag(name = "mapset", description = "Mapset service endpoints"),
				@Tag(name = "metadata", description = "Terminology and project metadata endpoints"),
				@Tag(name = "concept", description = "Concept endpoints"),
				@Tag(name = "concept by id", description = "Concept service endpoints with \"by id\" parameters"),
				@Tag(name = "concept by code", description = "Concept service endpoints with \"by code\" parameters"),
				@Tag(name = "term", description = "Term endpoints") }, servers = {
						@Server(description = "Current Instance", url = "/") })
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON)
public class TerminologyServiceRestImpl extends RootServiceRestImpl implements TerminologyServiceRest {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(TerminologyServiceRestImpl.class);

	/** The Constant DF. */
	@SuppressWarnings("unused")
	private final static DecimalFormat DF = new DecimalFormat("#.#####");

//	/** The cache. */
//	private static TimerCache<Project> projectCache = new TimerCache<>(1000, 10000);

	/** The terminologies cache. */
	private static TimerCache<Map<String, Terminology>> terminologyCache = new TimerCache<>(1000, 10000);

	/** The terminologies cache. */
	private static TimerCache<Map<String, Mapset>> mapsetCache = new TimerCache<>(1000, 10000);

	/** The request. */
	@Autowired
	HttpServletRequest request;

	/** The operations service. */
	@Autowired
	private EntityRepositoryService searchService;

//	/** The s3 storage handler. */
//	@Autowired
//	private List<FileStorageHandler> handlers;

	/** The builders. */
	@Autowired
	private List<QueryBuilder> builders;

	/**
	 * Instantiates an empty {@link TerminologyServiceRestImpl}.
	 *
	 * @throws Exception the exception
	 */
	public TerminologyServiceRestImpl() throws Exception {
		// n/a
	}

	/**
	 * Instantiates a {@link TerminologyServiceRestImpl} from the specified
	 * parameters. For testing.
	 *
	 * @param request the request
	 * @throws Exception the exception
	 */
	public TerminologyServiceRestImpl(final HttpServletRequest request) throws Exception {
		this.request = request;
	}

	/**
	 * Health.
	 *
	 * @param dependencies the dependencies
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/terminology/health", method = RequestMethod.GET)
	@Hidden
	public ResponseEntity<HealthCheck> health(
			@RequestParam(name = "dependencies", required = false) final Boolean dependencies) throws Exception {

		// Authorize
		authorize(request);

		try {

			final HealthCheck healthCheck = new HealthCheck();
			healthCheck.setStatus(true);
			healthCheck.setName("open-termhub-terminology-service");
			healthCheck.setTimestamp(new Date());

			// NO dependent checks
			return new ResponseEntity<>(healthCheck, new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "on health check =  " + e);
		}

		final HealthCheck healthCheck = new HealthCheck();
		healthCheck.setStatus(false);
		healthCheck.setName("open-termhub-terminology-service");
		healthCheck.setTimestamp(new Date());
		return new ResponseEntity<>(healthCheck, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);

	}

	/**
	 * Admin.
	 *
	 * @param task       the task
	 * @param adminKey   the admin key
	 * @param background the background
	 * @param payload    the payload
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/terminology/admin", method = RequestMethod.POST)
	@Hidden
	public ResponseEntity<String> admin(@RequestParam("task") final String task,
			@RequestParam("adminKey") final String adminKey,
			@RequestParam(name = "background", required = false) final Boolean background,
			@org.springframework.web.bind.annotation.RequestBody(required = false) final String payload)
			throws Exception {

		authorizeAdmin(request);
		try {

			if (adminKey != null && adminKey.equals(PropertyUtility.getProperties().getProperty("admin.key"))) {

				// Delete data
				if ("delete".equals(task)) {
					logger.debug("Delete all data - drop and recreate indexes");

					// Drop and recreate all indexes
					for (final Class<? extends HasId> clazz : Application.getManagedObjects()) {
						searchService.deleteIndex(clazz);
						searchService.createIndex(clazz);
					}
				}

				// Perform adhoc task (not via env var).
				else if (task != null && task.startsWith("adhoc")) {

					AdhocUtility.execute(task.replace("^[^,]*,", ""));

				}

			} else {
				throw new RestException(false, 403, "Forbidden", null);
			}
			return new ResponseEntity<>("Successful", new HttpHeaders(), HttpStatus.OK);
		} catch (final Exception e) {
			handleException(e, "trying to perform admin = " + task);
			return null;
		}
	}

	/**
	 * Gets the terminology.
	 *
	 * @param id the id
	 * @return the terminology
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/terminology/{id:[a-f0-9].+}", method = RequestMethod.GET)
	@Operation(summary = "Get terminology by id", description = "Gets terminology for the specified id", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"terminology" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Terminology"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({ @Parameter(name = "id", description = "Terminology id, e.g. \"uuid\"", required = true) })
	public ResponseEntity<Terminology> getTerminology(@PathVariable("id") final String id) throws Exception {

		@SuppressWarnings("unused")
		final AuthContext context = authorize(request);
		try {
			final Terminology terminology = searchService.get(id, Terminology.class);
			// not found - 404
			if (terminology == null) {
				throw new RestException(false, 404, "Not Found", "Unable to find terminology for " + id);
			}
//			Metric.addMetric(operationsService, context.getUserId(), null, terminology.getPublisher(),
//					terminology.getAbbreviation(), terminology.getVersion(), "TERMINOLOGY", null, null);

			terminology.cleanForApi();

			// Return the object
			return new ResponseEntity<>(terminology, new HttpHeaders(), HttpStatus.OK);
		} catch (final Exception e) {
			handleException(e, "trying to get terminology = " + id);
			return null;
		}
	}

	/**
	 * Gets the mapset.
	 *
	 * @param id the id
	 * @return the mapset
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/mapset/{id:[a-f0-9].+}", method = RequestMethod.GET)
	@Operation(summary = "Get mapset by id", description = "Gets mapset for the specified id", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"mapset" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Mapset"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({ @Parameter(name = "id", description = "Mapset id, e.g. \"uuid\"", required = true) })
	public ResponseEntity<Mapset> getMapset(@PathVariable("id") final String id) throws Exception {

		@SuppressWarnings("unused")
		final AuthContext context = authorize(request);
		try {
			final Mapset mapset = searchService.get(id, Mapset.class);
			// not found - 404
			if (mapset == null) {
				throw new RestException(false, 404, "Not Found", "Unable to find mapset for " + id);
			}
//			Metric.addMetric(operationsService, context.getUserId(), null, mapset.getPublisher(),
//					mapset.getAbbreviation(), mapset.getVersion(), "MAPSET", null, null);

			mapset.cleanForApi();

			// Return the object
			return new ResponseEntity<>(mapset, new HttpHeaders(), HttpStatus.OK);
		} catch (final Exception e) {
			handleException(e, "trying to get mapset = " + id);
			return null;
		}
	}

	/**
	 * Gets the terminology metadata.
	 *
	 * @param id the id
	 * @return the terminology metadata
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/terminology/{id:[a-f0-9].+}/metadata", method = RequestMethod.GET)
	@Operation(summary = "Get terminology metadata", description = "Gets terminology metadata for the specified terminology id", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"metadata" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Terminology"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({ @Parameter(name = "id", description = "Terminology id, e.g. \"uuid\"", required = true) })
	public ResponseEntity<List<Metadata>> getTerminologyMetadata(@PathVariable("id") final String id) throws Exception {

		@SuppressWarnings("unused")
		final AuthContext context = authorize(request);
		try {

			final Terminology terminology = searchService.get(id, Terminology.class);
			// not found - 404
			if (terminology == null) {
				throw new RestException(false, 404, "Not Found", "Unable to find terminology = " + id);
			}
//			Metric.addMetric(operationsService, context.getUserId(), null, terminology.getPublisher(),
//					terminology.getAbbreviation(), terminology.getVersion(), "METADATA", null, null);

			final SearchParameters params = new SearchParameters();
			params.setQuery(StringUtility.composeQuery("AND", "terminology:" + terminology.getAbbreviation(),
					"publisher:" + terminology.getPublisher(), "version:" + terminology.getVersion()));
			params.setLimit(100000);

			// Find and return the list
			return new ResponseEntity<>(searchService.find(params, Metadata.class, null).getItems(), new HttpHeaders(),
					HttpStatus.OK);
		} catch (final Exception e) {
			handleException(e, "trying to get metadata for terminology = " + id);
			return null;
		}
	}

	/**
	 * Gets the terminology history new concepts.
	 *
	 * @param id           the id
	 * @param priorVersion the prior version
	 * @return the terminology history new concepts
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/terminology/{id:[a-f0-9].+}/history/{priorVersion}/concepts/new", method = RequestMethod.GET)
	@Operation(summary = "Get terminology concepts added since older version", description = "Gets concept code/name for new active concepts in the terminology "
			+ "since the specified older version", security = @SecurityRequirement(name = "bearerAuth"), tags = {
					"history" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "List of concept code/name that are new"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({ @Parameter(name = "id", description = "Terminology id, e.g. \"uuid\"", required = true),
			@Parameter(name = "priorVersion", description = "Prior terminology version, e.g. \"20240101\"", required = true), })
	public ResponseEntity<List<ConceptRef>> getTerminologyHistoryNewConcepts(@PathVariable("id") final String id,
			@PathVariable("priorVersion") final String priorVersion) throws Exception {

		@SuppressWarnings("unused")
		final AuthContext context = authorize(request);
		try {

			final Terminology terminology = searchService.get(id, Terminology.class);
			// not found - 404
			if (terminology == null) {
				throw new RestException(false, 404, "Not Found", "Unable to find terminology = " + id);
			}

			final Terminology priorTerminology = TerminologyUtility.getTerminology(searchService,
					terminology.getAbbreviation(), terminology.getPublisher(), priorVersion);
			if (priorTerminology == null) {
				throw new RestException(false, 404, "Not Found", "Unable to find terminology = "
						+ terminology.getAbbreviation() + ", " + terminology.getPublisher() + ", " + priorVersion);
			}
			if (priorTerminology.getReleaseDate().compareTo(terminology.getReleaseDate()) > 0) {
				throw new RestException(false, 404, "Not Found",
						"The priorVersion must be earlier than for the specified terminology = "
								+ terminology.getVersion() + " < " + priorVersion);
			}

//			Metric.addMetric(operationsService, context.getUserId(), null, terminology.getPublisher(),
//					terminology.getAbbreviation(), terminology.getVersion(), "HISTORY", "new since", priorVersion);

			final Exception[] errors = new Exception[2];
			final List<Concept> concepts = new ArrayList<>();
			final Thread t1 = new Thread(new Runnable() {
				/* see superclass */
				@Override
				public void run() {
					try {
						concepts.addAll(searchService.findAllWithFields(
								StringUtility.composeQuery("AND", "terminology:" + terminology.getAbbreviation(),
										"publisher:" + terminology.getPublisher(),
										"version:" + terminology.getVersion(), "active:true"),
								ModelUtility.asList("code", "name"), Concept.class));
					} catch (final Exception e) {
						errors[0] = e;
					}
				}
			});

			final List<Concept> priorConcepts = new ArrayList<>();
			final Thread t2 = new Thread(new Runnable() {
				/* see superclass */
				@Override
				public void run() {
					try {
						priorConcepts.addAll(searchService.findAllWithFields(
								StringUtility.composeQuery("AND", "terminology:" + terminology.getAbbreviation(),
										"publisher:" + terminology.getPublisher(),
										"version:" + priorTerminology.getVersion(), "active:true"),
								ModelUtility.asList("code", "name"), Concept.class));
					} catch (final Exception e) {
						errors[0] = e;
					}
				}

			});
			// Start threads
			t1.start();
			t2.start();
			// Wait for threads
			t1.join();
			t2.join();
			// Check errors
			if (errors[0] != null) {
				throw errors[0];
			}
			if (errors[1] != null) {
				throw errors[1];
			}
			final Set<String> codes = concepts.stream().map(c -> c.getCode()).collect(Collectors.toSet());
			final Set<String> priorCodes = priorConcepts.stream().map(c -> c.getCode()).collect(Collectors.toSet());
			// things active now that were not active before
			codes.removeAll(priorCodes);

			// Find and return the list
			return new ResponseEntity<>(concepts.stream().filter(c -> codes.contains(c.getCode())).peek(c -> {
				c.setId(null);
				c.setConfidence(null);
				c.setLocal(null);
				c.setActive(null);
				c.setLeaf(null);
			}).sorted((a, b) -> a.getCode().compareTo(b.getCode())).collect(Collectors.toList()), new HttpHeaders(),
					HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to get new concept history for terminology = " + id + ", " + priorVersion);
			return null;
		}

	}

	/**
	 * Gets the terminology history retired concepts.
	 *
	 * @param id           the id
	 * @param priorVersion the prior version
	 * @return the terminology history retired concepts
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/terminology/{id:[a-f0-9].+}/history/{priorVersion}/concepts/retired", method = RequestMethod.GET)
	@Operation(summary = "Get terminology concepts retired since older version", description = "Gets concept code/name for concepts retired in the terminology "
			+ "since the specified older version", security = @SecurityRequirement(name = "bearerAuth"), tags = {
					"history" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "List of concept code/name that are now retired"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({ @Parameter(name = "id", description = "Terminology id, e.g. \"uuid\"", required = true),
			@Parameter(name = "priorVersion", description = "Prior terminology version, e.g. \"20240101\"", required = true), })
	public ResponseEntity<List<ConceptRef>> getTerminologyHistoryRetiredConcepts(@PathVariable("id") final String id,
			@PathVariable("priorVersion") final String priorVersion) throws Exception {

		@SuppressWarnings("unused")
		final AuthContext context = authorize(request);
		try {

			final Terminology terminology = searchService.get(id, Terminology.class);
			// not found - 404
			if (terminology == null) {
				throw new RestException(false, 404, "Not Found", "Unable to find terminology = " + id);
			}

			final Terminology priorTerminology = TerminologyUtility.getTerminology(searchService,
					terminology.getAbbreviation(), terminology.getPublisher(), priorVersion);
			if (priorTerminology == null) {
				throw new RestException(false, 404, "Not Found", "Unable to find terminology = "
						+ terminology.getAbbreviation() + ", " + terminology.getPublisher() + ", " + priorVersion);
			}
			if (priorTerminology.getReleaseDate().compareTo(terminology.getReleaseDate()) > 0) {
				throw new RestException(false, 404, "Not Found",
						"The priorVersion must be earlier than for the specified terminology = "
								+ terminology.getVersion() + " < " + priorVersion);
			}

//			Metric.addMetric(operationsService, context.getUserId(), null, terminology.getPublisher(),
//					terminology.getAbbreviation(), terminology.getVersion(), "HISTORY", "retired since", priorVersion);

			final Exception[] errors = new Exception[2];
			final List<Concept> concepts = new ArrayList<>();
			final Thread t1 = new Thread(new Runnable() {
				/* see superclass */
				@Override
				public void run() {
					try {
						concepts.addAll(searchService.findAllWithFields(
								StringUtility.composeQuery("AND", "terminology:" + terminology.getAbbreviation(),
										"publisher:" + terminology.getPublisher(),
										"version:" + terminology.getVersion(), "active:true"),
								ModelUtility.asList("code", "name"), Concept.class));
					} catch (final Exception e) {
						errors[0] = e;
					}
				}
			});

			final List<Concept> priorConcepts = new ArrayList<>();
			final Thread t2 = new Thread(new Runnable() {
				/* see superclass */
				@Override
				public void run() {
					try {
						priorConcepts.addAll(searchService.findAllWithFields(
								StringUtility.composeQuery("AND", "terminology:" + terminology.getAbbreviation(),
										"publisher:" + terminology.getPublisher(),
										"version:" + priorTerminology.getVersion(), "active:true"),
								ModelUtility.asList("code", "name"), Concept.class));
					} catch (final Exception e) {
						errors[0] = e;
					}
				}

			});
			// Start threads
			t1.start();
			t2.start();
			// Wait for threads
			t1.join();
			t2.join();
			// Check errors
			if (errors[0] != null) {
				throw errors[0];
			}
			if (errors[1] != null) {
				throw errors[1];
			}

			final Set<String> codes = concepts.stream().map(c -> c.getCode()).collect(Collectors.toSet());
			final Set<String> priorCodes = priorConcepts.stream().map(c -> c.getCode()).collect(Collectors.toSet());
			// Things active before that are not active now
			priorCodes.removeAll(codes);

			// Find and return the list
			return new ResponseEntity<>(priorConcepts.stream().filter(c -> priorCodes.contains(c.getCode())).peek(c -> {
				c.setId(null);
				c.setConfidence(null);
				c.setLocal(null);
				c.setActive(null);
				c.setLeaf(null);
			}).sorted((a, b) -> a.getCode().compareTo(b.getCode())).collect(Collectors.toList()), new HttpHeaders(),
					HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to get retired concept history for terminology = " + id + ", " + priorVersion);
			return null;
		}

	}

	/**
	 * Adds the terminology.
	 *
	 * @param terminologyStr the terminology str
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/terminology", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON)
	@Hidden
	public ResponseEntity<Terminology> addTerminology(
			@org.springframework.web.bind.annotation.RequestBody final String terminologyStr) throws Exception {

		authorizeAdmin(request);
		try {

			Terminology terminology = null;
			try {
				terminology = ModelUtility.fromJson(terminologyStr, Terminology.class);
			} catch (final Exception e) {
				throw new RestException(false, 417, "Expectation Failed ",
						"Unable to parse terminology = " + terminologyStr);
			}

			searchService.createIndex(Terminology.class);
			searchService.add(Terminology.class, terminology);

			// Compute the URI location
			final String location = PropertyUtility.getProperties().getProperty("api.url") + "/organization/"
					+ terminology.getId();

			// Return the response
			final HttpHeaders headers = new HttpHeaders();
			headers.add("Location", location);
			return new ResponseEntity<>(terminology, headers, HttpStatus.CREATED);

		} catch (final Exception e) {
			handleException(e, "trying to add terminology");
			return null;
		}
	}

	/**
	 * Update terminology.
	 *
	 * @param id             the id
	 * @param terminologyStr the terminology str
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/terminology/{id:[a-f0-9].+}", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON)
	@Hidden
	public ResponseEntity<Terminology> updateTerminology(@PathVariable("id") final String id,
			@org.springframework.web.bind.annotation.RequestBody final String terminologyStr) throws Exception {

		authorizeAdmin(request);
		try {
			Terminology terminology = null;
			try {
				terminology = ModelUtility.fromJson(terminologyStr, Terminology.class);
			} catch (final Exception e) {
				throw new RestException(false, 417, "Expectation Failed",
						"Unable to parse terminology = " + terminologyStr);
			}

			// Find the terminology
			final Terminology orig = searchService.get(id, Terminology.class);
			// not found - 404
			if (orig == null) {
				throw new RestException(false, 404, "Not Found", "Unable to find terminology = " + id);
			}

			// Apply changes
			orig.patchFrom(terminology);

			// Update
			searchService.update(Terminology.class, id, orig);

			return new ResponseEntity<>(orig, new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to update terminology = " + id);
			return null;
		}
	}

	/**
	 * Delete terminology.
	 *
	 * @param id the id
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/terminology/{id:[a-f0-9].+}", method = RequestMethod.DELETE)
	@Hidden
	public ResponseEntity<Void> deleteTerminology(@PathVariable("id") final String id) throws Exception {

		authorizeAdmin(request);
		try {
			// Find the object
			final Terminology terminology = searchService.get(id, Terminology.class);
			// not found - 404
			if (terminology == null) {
				throw new RestException(false, 404, "Not Found", "Unable to find terminology = " + id);
			}

			searchService.remove(id, Terminology.class);

			// Return the object
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (final Exception e) {
			handleException(e, "trying to delete terminology = " + id);
			return null;
		}
	}

	/**
	 * Find terminologies.
	 *
	 * @param query     the query
	 * @param offset    the offset
	 * @param limit     the limit
	 * @param sort      the sort
	 * @param ascending the ascending
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/terminology", method = RequestMethod.GET)
	@Operation(summary = "Find terminologies", description = "Finds terminologies matching specified criteria.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"terminology" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Result list of matching terminologies"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "query", description = "Search text"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/SEARCH.md\">"
					+ "See here for more info</a>)", required = false, schema = @Schema(implementation = String.class)),
			@Parameter(name = "offset", description = "Start index for search results", required = false, schema = @Schema(implementation = Integer.class), example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, schema = @Schema(implementation = Integer.class), example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false, schema = @Schema(implementation = String.class)),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false, schema = @Schema(implementation = Boolean.class)) })
	public ResponseEntity<ResultListTerminology> findTerminologies(
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "ascending", required = false) final Boolean ascending) throws Exception {

		@SuppressWarnings("unused")
		final AuthContext context = authorize(request);
		try {

//			Metric.addMetric(operationsService, context.getUserId(), null, null, null, null, "TERMINOLOGY", null, null);

			// limit return objects to 1000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

			// Limit to loaded terminologies
			final SearchParameters params = new SearchParameters(
					"loaded:true" + ((query == null || query.isEmpty()) ? "" : " AND " + query), offset, maxLimit, sort,
					ascending);
			final ResultList<Terminology> list = searchService.find(params, Terminology.class);
			list.setParameters(params);
			list.getItems().forEach(t -> t.cleanForApi());

			return new ResponseEntity<>(new ResultListTerminology(list), new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to find terminologies");
			return null;
		}
	}

	/**
	 * Find mapsets.
	 *
	 * @param query     the query
	 * @param offset    the offset
	 * @param limit     the limit
	 * @param sort      the sort
	 * @param ascending the ascending
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/mapset", method = RequestMethod.GET)
	@Operation(summary = "Find mapsets", description = "Finds mapsets matching specified criteria.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"mapset" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Result list of matching mapsets"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "query", description = "Search text", required = false, schema = @Schema(implementation = String.class)),
			@Parameter(name = "offset", description = "Start index for search results", required = false, schema = @Schema(implementation = Integer.class), example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, schema = @Schema(implementation = Integer.class), example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false, schema = @Schema(implementation = String.class)),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false, schema = @Schema(implementation = Boolean.class)) })
	public ResponseEntity<ResultListMapset> findMapsets(
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "ascending", required = false) final Boolean ascending) throws Exception {

		@SuppressWarnings("unused")
		final AuthContext context = authorize(request);
		try {

//			Metric.addMetric(operationsService, context.getUserId(), null, null, null, null, "MAPSET", null, null);

			// limit return objects to 1000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

			// Limit to loaded mapsets
			final SearchParameters params = new SearchParameters(
					"loaded:true" + ((query == null || query.isEmpty()) ? "" : " AND " + query), offset, maxLimit, sort,
					ascending);
			final ResultList<Mapset> list = searchService.find(params, Mapset.class);
			list.setParameters(params);
			list.getItems().forEach(t -> t.cleanForApi());

			return new ResponseEntity<>(new ResultListMapset(list), new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to find mapsets");
			return null;
		}
	}

	/**
	 * Gets the concept.
	 *
	 * @param conceptId the concept id
	 * @param include   the include
	 * @return the concept
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{conceptId:[a-f0-9].*}", method = RequestMethod.GET)
	@Operation(summary = "Get concept by id", description = "Gets concept for the specified id", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"concept by id" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Concept matching specified id in specified project"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true),
			@Parameter(name = "include", description = "Indicator of how much data to return. Comma-separated list of any of the following values: "
					+ "minimal, summary, full, axioms, attributes, children, definitions, descendants, "
					+ "highlights, inverseRelationships, mapsets, parents, relationships, semanticTypes, "
					+ "subsets, terms, treePositions"
					+ "<a href='https://github.com/TerminologyHub/termhub-in-5-minutes/blob/main/doc/INCLUDE.md' target='_blank'>See here "
					+ "for detailed information</a>.", required = false), })
	public ResponseEntity<Concept> getConcept(@PathVariable("conceptId") final String conceptId,
			@RequestParam(value = "include", required = false) final String include) throws Exception {

		@SuppressWarnings("unused")
		final AuthContext context = authorize(request);
		try {
			final IncludeParam ip = new IncludeParam(include == null ? "summary" : include);

			final Map<String, Terminology> map = lookupTerminologyMap();

			// Get the concept
			final String query = "id:" + StringUtility.escapeQuery(conceptId);

			// then do a find on the query
			// don't use 'get' because it doesn't work with include param fields
			final SearchParameters params = new SearchParameters(query, null, 2, null, null);
			final ResultList<Concept> results = searchService.findFields(params,
					new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

			if (results.getTotal() == 0) {
				return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
			}
			if (results.getTotal() > 1) {
				throw new RestException(false, 417, "Expecation failed",
						"Too many matching concepts for id = " + conceptId);
			}

			final Concept concept = results.getItems().get(0);

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, concept.getPublisher(),
//					concept.getTerminology(), concept.getVersion(), "CONCEPT-BY-ID", concept.getCode(), null);

			concept.cleanForApi();

			// Get requested addendums to the concept
			final Terminology terminology = getTerminology(map, concept);

			TerminologyUtility.populateConcept(concept, ip, terminology, searchService);

			// Return the object
			return new ResponseEntity<>(concept, new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to get concept = " + conceptId);
			return null;
		}
	}

	/**
	 * Gets the concept.
	 *
	 * @param terminology the terminology
	 * @param code        the code
	 * @param include     the include
	 * @return the concept
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{terminology}/{code}", method = RequestMethod.GET)
	@Operation(summary = "Get concept by terminology and code", description = "Gets concept for the specified terminology and code."
			+ " This call will not work for projects hosting multiple versions of the same terminology.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
					"concept by code" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Concept matching specified terminology and code in specified project"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "terminology", description = "Terminology id or abbreviation."
					+ " e.g. \"uuid1\" or \"ICD10CM\".", required = true),
			@Parameter(name = "code", description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"", required = true),
			@Parameter(name = "include", description = "Indicator of how much data to return. Comma-separated list of any of the following values: "
					+ "minimal, summary, full, axioms, attributes, children, definitions, descendants, "
					+ "highlights, inverseRelationships, mapsets, parents, relationships, semanticTypes, "
					+ "subsets, terms, treePositions"
					+ "<a href='https://github.com/TerminologyHub/termhub-in-5-minutes/blob/main/doc/INCLUDE.md' target='_blank'>See here "
					+ "for detailed information</a>.", required = false), })
	public ResponseEntity<Concept> getConcept(@PathVariable("terminology") final String terminology,
			@PathVariable("code") final String code,
			@RequestParam(value = "include", required = false) final String include) throws Exception {

		final AuthContext context = authorize(request);
		try {

			final IncludeParam ip = new IncludeParam(include == null ? "summary" : include);

			final Terminology term = lookupTerminology(context, terminology);

			// find with code, term, pub, version
			final String query = StringUtility.composeQuery("AND", "code:" + StringUtility.escapeQuery(code),
					"terminology:" + StringUtility.escapeQuery(term.getAbbreviation()),
					"publisher:" + StringUtility.escapeQuery(term.getPublisher()),
					"version:" + StringUtility.escapeQuery(term.getVersion()));

			// then do a find on the query
			final SearchParameters params = new SearchParameters(query, null, 2, null, null);
			final ResultList<Concept> results = searchService.findFields(params,
					new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

			if (results.getTotal() == 0) {
				return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
			}
			if (results.getTotal() > 1) {
				throw new RestException(false, 417, "Expecation failed",
						"Too many matching concepts for terminology/code = " + terminology + ", " + code);
			}

			final Concept concept = results.getItems().get(0);
//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, concept.getPublisher(),
//					concept.getTerminology(), concept.getVersion(), "CONCEPT-BY-CODE", concept.getCode(), null);

			concept.cleanForApi();

			TerminologyUtility.populateConcept(concept, ip, term, searchService);

			// Return the object
			return new ResponseEntity<>(concept, new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to get concept = " + code);
			return null;
		}
	}

	/**
	 * Gets the concept mappings.
	 *
	 * @param conceptId the concept id
	 * @return the concept mappings
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{conceptId:[a-f0-9].*}/mappings", method = RequestMethod.GET)
	@Operation(summary = "Get mappings from concept by id", description = "Gets mappings from the specified concept", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"concept by id" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Mappings from the concept matching specified id in specified project", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Mapping.class)))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true) })
	public ResponseEntity<List<Mapping>> getConceptMappings(@PathVariable("conceptId") final String conceptId)
			throws Exception {

		final AuthContext context = authorize(request);
		try {
			final IncludeParam ip = new IncludeParam("minimal");

			final Map<String, Terminology> map = lookupTerminologyMap();

			// Get the concept
			final String query = "id:" + StringUtility.escapeQuery(conceptId);

			// then do a find on the query
			// don't use 'get' because it doesn't work with include param fields
			final SearchParameters params = new SearchParameters(query, null, 2, null, null);
			final ResultList<Concept> results = searchService.findFields(params,
					new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

			if (results.getTotal() == 0) {
				return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
			}
			if (results.getTotal() > 1) {
				throw new RestException(false, 417, "Expecation failed",
						"Too many matching concepts for id = " + conceptId);
			}

			final Concept concept = results.getItems().get(0);

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, concept.getPublisher(),
//					concept.getTerminology(), concept.getVersion(), "CONCEPT-MAPPINGS-BY-ID", concept.getCode(), null);

			// Find mappings
			final List<Mapset> mapsets = lookupMapsets(context, null, true);
			final List<Mapping> mappings = findMappingsHelper(mapsets,
					StringUtility.composeQuery("AND", "from.code:" + concept.getCode(),
							"from.terminology:" + concept.getTerminology(), "from.publisher:" + concept.getPublisher()),
					0, 10000, null, null, true, null).getItems();

			// Return the object
			return new ResponseEntity<>(mappings, new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to get concept mappings = " + conceptId);
			return null;
		}
	}

	/**
	 * Gets the concept inverse mappings.
	 *
	 * @param conceptId the concept id
	 * @return the concept inverse mappings
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{conceptId:[a-f0-9].*}/inverseMappings", method = RequestMethod.GET)
	@Operation(summary = "Get mappings to concept by id", description = "Gets mappings to the specified concept", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"concept by id" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Mappings to the concept matching specified id in specified project", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Mapping.class)))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true) })
	public ResponseEntity<List<Mapping>> getConceptInverseMappings(@PathVariable("conceptId") final String conceptId)
			throws Exception {

		final AuthContext context = authorize(request);
		try {
			final IncludeParam ip = new IncludeParam("minimal");

			final Map<String, Terminology> map = lookupTerminologyMap();

			// Get the concept
			final String query = "id:" + StringUtility.escapeQuery(conceptId);

			// then do a find on the query
			// don't use 'get' because it doesn't work with include param fields
			final SearchParameters params = new SearchParameters(query, null, 2, null, null);
			final ResultList<Concept> results = searchService.findFields(params,
					new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

			if (results.getTotal() == 0) {
				return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
			}
			if (results.getTotal() > 1) {
				throw new RestException(false, 417, "Expecation failed",
						"Too many matching concepts for id = " + conceptId);
			}

			final Concept concept = results.getItems().get(0);

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, concept.getPublisher(),
//					concept.getTerminology(), concept.getVersion(), "CONCEPT-MAPPINGS-BY-ID", concept.getCode(), null);

			// Find mappings
			final List<Mapset> mapsets = lookupMapsets(context, null, true);
			final List<Mapping> mappings = findMappingsHelper(mapsets,
					StringUtility.composeQuery("AND", "to.code:" + concept.getCode(),
							"to.terminology:" + concept.getTerminology(), "to.publisher:" + concept.getPublisher()),
					0, 10000, null, null, true, null).getItems();

			// Return the object
			return new ResponseEntity<>(mappings, new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to get concept mappings = " + conceptId);
			return null;
		}
	}

	/**
	 * Gets the concept mappings.
	 *
	 * @param terminology the terminology
	 * @param code        the code
	 * @return the concept mappings
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{terminology}/{code}/mappings", method = RequestMethod.GET)
	@Operation(summary = "Get mappings from concept by terminology and code", description = "Gets mappings from the concept with the specified terminology and code."
			+ " This call will not work for projects hosting multiple versions of the same terminology.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
					"concept by code" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Mappings from the concept matching specified terminology and code", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Mapping.class)))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "terminology", description = "Terminology id or abbreviation."
					+ " e.g. \"uuid1\" or \"ICD10CM\".", required = true),
			@Parameter(name = "code", description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"", required = true) })
	public ResponseEntity<List<Mapping>> getConceptMappings(@PathVariable("terminology") final String terminology,
			@PathVariable("code") final String code) throws Exception {

		final AuthContext context = authorize(request);
		try {

			final IncludeParam ip = new IncludeParam("minimal");
			final Terminology term = lookupTerminology(context, terminology);

			// find with code, term, pub, version
			final String query = StringUtility.composeQuery("AND", "code:" + StringUtility.escapeQuery(code),
					"terminology:" + StringUtility.escapeQuery(term.getAbbreviation()),
					"publisher:" + StringUtility.escapeQuery(term.getPublisher()),
					"version:" + StringUtility.escapeQuery(term.getVersion()));

			// then do a find on the query
			final SearchParameters params = new SearchParameters(query, null, 2, null, null);
			final ResultList<Concept> results = searchService.findFields(params,
					new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

			if (results.getTotal() == 0) {
				return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
			}
			if (results.getTotal() > 1) {
				throw new RestException(false, 417, "Expecation failed",
						"Too many matching concepts for terminology/code = " + terminology + ", " + code);
			}

			final Concept concept = results.getItems().get(0);
//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, concept.getPublisher(),
//					concept.getTerminology(), concept.getVersion(), "CONCEPT-MAPPINGS-BY-CODE", concept.getCode(),
//					null);

			// Find mappings
			final List<Mapset> mapsets = lookupMapsets(context, null, true);
			final List<Mapping> mappings = findMappingsHelper(mapsets,
					StringUtility.composeQuery("AND", "from.code:" + concept.getCode(),
							"from.terminology:" + concept.getTerminology(), "from.publisher:" + concept.getPublisher()),
					0, 10000, null, null, true, null).getItems();

			// Return the object
			return new ResponseEntity<>(mappings, new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to get concept mappings for = " + code);
			return null;
		}
	}

	/**
	 * Gets the concept inverse mappings.
	 *
	 * @param terminology the terminology
	 * @param code        the code
	 * @return the concept inverse mappings
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{terminology}/{code}/inverseMappings", method = RequestMethod.GET)
	@Operation(summary = "Get mappings to concept by terminology and code", description = "Gets mappings tothe concept with the specified terminology and code."
			+ " This call will not work for projects hosting multiple versions of the same terminology.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
					"concept by code" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Mappings to the concept matching specified terminology and code", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Mapping.class)))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "terminology", description = "Terminology id or abbreviation."
					+ " e.g. \"uuid1\" or \"ICD10CM\".", required = true),
			@Parameter(name = "code", description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"", required = true) })
	public ResponseEntity<List<Mapping>> getConceptInverseMappings(

			@PathVariable("terminology") final String terminology, @PathVariable("code") final String code)
			throws Exception {

		final AuthContext context = authorize(request);
		try {

			final IncludeParam ip = new IncludeParam("minimal");
			final Terminology term = lookupTerminology(context, terminology);

			// find with code, term, pub, version
			final String query = StringUtility.composeQuery("AND", "code:" + StringUtility.escapeQuery(code),
					"terminology:" + StringUtility.escapeQuery(term.getAbbreviation()),
					"publisher:" + StringUtility.escapeQuery(term.getPublisher()),
					"version:" + StringUtility.escapeQuery(term.getVersion()));

			// then do a find on the query
			final SearchParameters params = new SearchParameters(query, null, 2, null, null);
			final ResultList<Concept> results = searchService.findFields(params,
					new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

			if (results.getTotal() == 0) {
				return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
			}
			if (results.getTotal() > 1) {
				throw new RestException(false, 417, "Expecation failed",
						"Too many matching concepts for terminology/code = " + terminology + ", " + code);
			}

			final Concept concept = results.getItems().get(0);
//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, concept.getPublisher(),
//					concept.getTerminology(), concept.getVersion(), "CONCEPT-MAPPINGS-BY-CODE", concept.getCode(),
//					null);

			// Find mappings
			final List<Mapset> mapsets = lookupMapsets(context, null, true);
			final List<Mapping> mappings = findMappingsHelper(mapsets,
					StringUtility.composeQuery("AND", "to.code:" + concept.getCode(),
							"to.terminology:" + concept.getTerminology(), "to.publisher:" + concept.getPublisher()),
					0, 10000, null, null, true, null).getItems();

			// Return the object
			return new ResponseEntity<>(mappings, new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to get concept mappings for = " + code);
			return null;
		}
	}

	/**
	 * Gets the concept codes.
	 *
	 * @param terminology the terminology
	 * @param codes       the codes
	 * @param include     the include
	 * @return the concept codes
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{terminology:[A-Z].*}", method = RequestMethod.GET)
	@Operation(summary = "Get concepts by terminology and list of codes", description = "Gets concepts for the specified terminology and list of codes."
			+ " This call will not work for projects hosting multiple versions of the same terminology.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
					"concept by code" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Concepts mathcing code list in the specified project"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "terminology", description = "Terminology id or abbreviation."
					+ " e.g. \"uuid1\" or \"ICD10CM\".", required = true),
			@Parameter(name = "codes", description = "Comma-separated list of terminology codes, e.g. \"1119,1149\" or \"64572001,22298006 \"", required = true),
			@Parameter(name = "include", description = "Indicator of how much data to return. Comma-separated list of any of the following values: "
					+ "minimal, summary, full, axioms, attributes, children, definitions, descendants, "
					+ "highlights, inverseRelationships, mapsets, parents, relationships, semanticTypes, "
					+ "subsets, terms, treePositions"
					+ "<a href='https://github.com/TerminologyHub/termhub-in-5-minutes/blob/main/doc/INCLUDE.md' target='_blank'>See here "
					+ "for detailed information</a>.", required = false), })
	public ResponseEntity<List<Concept>> getConceptCodes(@PathVariable("terminology") final String terminology,
			@RequestParam(value = "codes", required = true) final String codes,
			@RequestParam(value = "include", required = false) final String include) throws Exception {

		final AuthContext context = authorize(request);
		try {

			final IncludeParam ip = new IncludeParam(include == null ? "summary" : include);

			final Terminology term = lookupTerminology(context, terminology);

			if (StringUtility.isEmpty(codes)) {
				throw new RestException(false, 417, "Expecation failed", "Codes parameter must be specified");
			}

			final String[] codeArray = codes.split(",");
			if (codeArray.length > 500) {
				throw new RestException(false, 417, "Expecation failed",
						"Too many codes specified in list (max is 500) = " + codeArray.length);
			}

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, term.getPublisher(),
//					term.getAbbreviation(), term.getVersion(), "CONCEPT-BY-CODE", codes, null);

			// find with code, term, pub, version
			final String query = StringUtility.composeQuery("AND",
					"code:(" + String.join(" OR ",
							Arrays.asList(codeArray).stream().map(c -> StringUtility.escapeQuery(c))
									.collect(Collectors.toList()))
							+ ")",
					"terminology:" + StringUtility.escapeQuery(term.getAbbreviation()),
					"publisher:" + StringUtility.escapeQuery(term.getPublisher()),
					"version:" + StringUtility.escapeQuery(term.getVersion()));

			// then do a find on the query
			final SearchParameters params = new SearchParameters(query, 0, 500, null, null);
			final ResultList<Concept> results = searchService.findFields(params,
					new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

			results.getItems().forEach(c -> {
				c.cleanForApi();
				try {
					TerminologyUtility.populateConcept(c, ip, term, searchService);
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			});

			// Return the object
			return new ResponseEntity<>(results.getItems(), new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to get concepts for codes = " + codes);
			return null;
		}
	}

	/**
	 * Find concepts.
	 *
	 * @param terminology the terminology
	 * @param query       the query
	 * @param expression  the expression
	 * @param offset      the offset
	 * @param limit       the limit
	 * @param sort        the sort
	 * @param ascending   the ascending
	 * @param active      the active
	 * @param leaf        the leaf
	 * @param include     the include
	 * @param handler     the handler
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept", method = RequestMethod.GET)
	@Operation(summary = "Find concepts across project terminologies", description = "Finds concepts matching specified search criteria.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"concept" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Result list of matching concepts"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({ @Parameter(name = "idOrUriLabel", description = "Project id or uriLabel", required = true),
			@Parameter(name = "terminology", description = "Comma-separated list of terminology ids or abbreviations (or null for all terminologies)."
					+ " e.g. \"uuid1,uuid2\", \"SNOMEDCT,RXNORM\", or \"ICD10CM\".", required = false),
			@Parameter(name = "query", description = "Search text"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/SEARCH.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "expression", description = "ECL-style expression"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/EXPRESSION.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "offset", description = "Start index for search results", required = false, schema = @Schema(implementation = Integer.class), example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, schema = @Schema(implementation = Integer.class), example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false, schema = @Schema(implementation = String.class)),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false, schema = @Schema(implementation = Boolean.class)),
			@Parameter(name = "active", description = "<code>true</code> for active concepts only, <code>false</code> for inactive concepts only,"
					+ " <code>null</code> for both", required = false, schema = @Schema(implementation = Boolean.class)),
			@Parameter(name = "leaf", description = "<code>true</code> for leaf nodes only, <code>false</code> for non-leaf nodes,"
					+ " <code>null</code> for either", required = false, schema = @Schema(implementation = Boolean.class)),
			@Parameter(name = "include", description = "Indicator of how much data to return. Comma-separated list of any of the following values: "
					+ "minimal, summary, full, axioms, attributes, children, definitions, descendants, "
					+ "highlights, inverseRelationships, mapsets, parents, relationships, semanticTypes, "
					+ "subsets, terms, treePositions"
					+ "<a href='https://github.com/TerminologyHub/termhub-in-5-minutes/blob/main/doc/INCLUDE.md' target='_blank'>See here "
					+ "for detailed information</a>.", required = false), })
	public ResponseEntity<ResultListConcept> findConcepts(
			@RequestParam(value = "terminology", required = false) final String terminology,
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "expression", required = false) final String expression,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "ascending", required = false) final Boolean ascending,
			@RequestParam(name = "active", required = false) final Boolean active,
			@RequestParam(name = "leaf", required = false) final Boolean leaf,
			@RequestParam(value = "include", required = false) final String include,
			@RequestParam(name = "handler", required = false) @Parameter(hidden = true) final String handler)
			throws Exception {

		final AuthContext context = authorize(request);
		try {

			final IncludeParam ip = new IncludeParam(include == null ? "highlights" : include);
			final List<Terminology> tlist = lookupTerminologies(context, terminology, true);

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, tlist.get(0).getPublisher(),
//					tlist.get(0).getAbbreviation(), tlist.get(0).getVersion(), "CONCEPT", null, null);

			// Build a query from the handler and use it in findHelper
			final String query2 = QueryBuilder.findBuilder(builders, handler).buildQuery(query);

			// limit return objects to 1000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

			// Handler applied, send null handler below
			final ResultList<Concept> list = findHelper(tlist, query2, expression, offset, maxLimit, sort, ascending,
					active, leaf, null, ip);

			return new ResponseEntity<>(new ResultListConcept(list), new HttpHeaders(), HttpStatus.OK);

		} catch (

		final Exception e) {
			handleException(e, "trying to find concepts");
			return null;
		}
	}

	/**
	 * Find mappings.
	 *
	 * @param mapset    the mapset
	 * @param query     the query
	 * @param offset    the offset
	 * @param limit     the limit
	 * @param sort      the sort
	 * @param ascending the ascending
	 * @param active    the active
	 * @param leaf      the leaf
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/mapping", method = RequestMethod.GET)
	@Operation(summary = "Find mappings across project mapsets", description = "Finds mapping matching specified search criteria.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"mapset" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Result list of matching mappings"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({ @Parameter(name = "idOrUriLabel", description = "Project id or uriLabel", required = true),
			@Parameter(name = "mapset", description = "Comma-separated list of mapset ids or abbreviations (or null for all mapsets)."
					+ " e.g. \"uuid1,uuid2\", \"SNOMEDCT_US-ICD10CM,CVX-NDC\".", required = false),
			@Parameter(name = "query", description = "Search text", required = false),
			@Parameter(name = "offset", description = "Start index for search results", required = false, schema = @Schema(implementation = Integer.class), example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, schema = @Schema(implementation = Integer.class), example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false, schema = @Schema(implementation = String.class)),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false, schema = @Schema(implementation = Boolean.class)),
			@Parameter(name = "active", description = "<code>true</code> for active mappings only, <code>false</code> for inactive mappings only,"
					+ " <code>null</code> for both", required = false, schema = @Schema(implementation = Boolean.class)),
			@Parameter(name = "leaf", description = "<code>true</code> for leaf nodes only, <code>false</code> for non-leaf nodes,"
					+ " <code>null</code> for either", required = false, schema = @Schema(implementation = Boolean.class)), })
	public ResponseEntity<ResultListMapping> findMappings(
			@RequestParam(name = "mapset", required = false) final String mapset,
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "ascending", required = false) final Boolean ascending,
			@RequestParam(name = "active", required = false) final Boolean active,
			@RequestParam(name = "leaf", required = false) final Boolean leaf) throws Exception {

		final AuthContext context = authorize(request);
		try {

			// Allow mapset to be blank here
			final List<Mapset> mapsets = lookupMapsets(context, mapset, true);

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, mapsets.get(0).getPublisher(),
//					mapsets.get(0).getAbbreviation(), mapsets.get(0).getVersion(), "MAPPING", null, null);

			// Build a query from the handler and use it in findHelper
			// limit return objects to 1000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

			// Handler applied, send null handler below
			final ResultList<Mapping> list = findMappingsHelper(mapsets, query, offset, maxLimit, sort, ascending,
					active, leaf);

			return new ResponseEntity<>(new ResultListMapping(list), new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to find mappings");
			return null;
		}
	}

	/**
	 * Find mapset mappings.
	 *
	 * @param mapsetId  the mapset id
	 * @param query     the query
	 * @param offset    the offset
	 * @param limit     the limit
	 * @param sort      the sort
	 * @param ascending the ascending
	 * @param active    the active
	 * @param leaf      the leaf
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/mapset/{mapset}/mappings", method = RequestMethod.GET)
	@Operation(summary = "Find mappings for the specified mapset", description = "Finds mapping for the specified mapset and the specified search criteria.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"mapset" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Result list of matching mappings"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({ @Parameter(name = "idOrUriLabel", description = "Project id or uriLabel", required = true),
			@Parameter(name = "mapset", description = "Mapset id or abbreviation" + " e.g. \"uuid1\" or \"CVX-NDC\"."),
			@Parameter(name = "query", description = "Search text", required = false),
			@Parameter(name = "offset", description = "Start index for search results", required = false, schema = @Schema(implementation = Integer.class), example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, schema = @Schema(implementation = Integer.class), example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false, schema = @Schema(implementation = String.class)),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false, schema = @Schema(implementation = Boolean.class)),
			@Parameter(name = "active", description = "<code>true</code> for active mappings only, <code>false</code> for inactive mappings only,"
					+ " <code>null</code> for both", required = false, schema = @Schema(implementation = Boolean.class)),
			@Parameter(name = "leaf", description = "<code>true</code> for leaf nodes only, <code>false</code> for non-leaf nodes,"
					+ " <code>null</code> for either", required = false, schema = @Schema(implementation = Boolean.class)), })
	public ResponseEntity<ResultListMapping> findMapsetMappings(@PathVariable("mapset") final String mapsetId,
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "ascending", required = false) final Boolean ascending,
			@RequestParam(name = "active", required = false) final Boolean active,
			@RequestParam(name = "leaf", required = false) final Boolean leaf) throws Exception {

		final AuthContext context = authorize(request);
		try {

			// Allow mapset to be blank here
			final List<Mapset> mapsets = lookupMapsets(context, mapsetId, false);

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, mapsets.get(0).getPublisher(),
//					mapsets.get(0).getAbbreviation(), mapsets.get(0).getVersion(), "MAPPING", null, null);

			// Build a query from the handler and use it in findHelper
			// limit return objects to 1000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

			// Handler applied, send null handler below
			final ResultList<Mapping> list = findMappingsHelper(mapsets, query, offset, maxLimit, sort, ascending,
					active, leaf);

			return new ResponseEntity<>(new ResultListMapping(list), new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to find mapset mappings");
			return null;
		}
	}

	/**
	 * Find terms.
	 *
	 * @param terminology the terminology
	 * @param query       the query
	 * @param offset      the offset
	 * @param limit       the limit
	 * @param sort        the sort
	 * @param ascending   the ascending
	 * @param active      the active
	 * @param handler     the handler
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/term", method = RequestMethod.GET)
	@Operation(summary = "Find terms across project terminologies", description = "Finds terms matching specified search criteria.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"term" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Result list of matching terms"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({ @Parameter(name = "idOrUriLabel", description = "Project id or uriLabel", required = true),
			@Parameter(name = "terminology", description = "Comma-separated list of terminology ids or abbreviations (or null for all terminologies)."
					+ " e.g. \"uuid1,uuid2\", \"SNOMEDCT,RXNORM\", or \"ICD10CM\".", required = false),
			@Parameter(name = "query", description = "Search text"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/SEARCH.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "offset", description = "Start index for search results", required = false, schema = @Schema(implementation = Integer.class), example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, schema = @Schema(implementation = Integer.class), example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false, schema = @Schema(implementation = String.class)),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false, schema = @Schema(implementation = Boolean.class)),
			@Parameter(name = "active", description = "<code>true</code> for active terms only, <code>false</code> for inactive terms only,"
					+ " <code>null</code> for both", required = false, schema = @Schema(implementation = Boolean.class)), })
	public ResponseEntity<ResultListTerm> findTerms(
			@RequestParam(value = "terminology", required = false) final String terminology,
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "ascending", required = false) final Boolean ascending,
			@RequestParam(name = "active", required = false) final Boolean active,
			@RequestParam(name = "handler", required = false) @Parameter(hidden = true) final String handler)
			throws Exception {

		final AuthContext context = authorize(request);
		try {

			final List<Terminology> tlist = lookupTerminologies(context, terminology);

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, tlist.get(0).getPublisher(),
//					tlist.get(0).getAbbreviation(), tlist.get(0).getVersion(), "TERM", null, null);

			// Build a query from the handler and use it in findHelper
			final String query2 = QueryBuilder.findBuilder(builders, handler).buildQuery(query);

			// limit return objects to 1000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

			final SearchParameters params = new SearchParameters(query2, offset, maxLimit, sort, ascending);
			if (active != null && active) {
				params.setActive(true);
			}
//			final ResultList<Term> list = searchService.find(params, Term.class, null,
//					tlist.stream().map(t -> t.getIndexName()).collect(Collectors.toList()));
			final ResultList<Term> list = searchService.find(params, Term.class);

			list.getItems().forEach(t -> t.cleanForApi());

			// Restore the original query for the response
			params.setQuery(query);
			list.setParameters(params);

			return new ResponseEntity<>(new ResultListTerm(list), new HttpHeaders(), HttpStatus.OK);

		} catch (

		final Exception e) {
			handleException(e, "trying to find terms");
			return null;
		}
	}

	/**
	 * Autocomplete.
	 *
	 * @param terminology the terminology
	 * @param query       the query
	 * @param limit       the limit
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/autocomplete", method = RequestMethod.GET)
	@Operation(summary = "Suggest autocompletions for text while searching", description = "Finds top ten strings matching input query.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"term" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "List of top ten matching strings"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({ @Parameter(name = "idOrUriLabel", description = "Project id or uriLabel", required = true),
			@Parameter(name = "terminology", description = "Comma-separated list of terminology ids or abbreviations (or null for all terminologies)."
					+ " e.g. \"uuid1,uuid2\", \"SNOMEDCT,RXNORM\", or \"ICD10CM\".", required = false),
			@Parameter(name = "query", description = "Search text", required = true),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, schema = @Schema(implementation = Integer.class), example = "10"), })
	public ResponseEntity<List<String>> autocomplete(
			@RequestParam(value = "terminology", required = false) final String terminology,
			@RequestParam(name = "query", required = true) final String query,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit) throws Exception {

		final AuthContext context = authorize(request);
		try {

			// Get project terminologies (that support autocomplete)
			final List<Terminology> tlist = lookupTerminologies(context, terminology, true).stream()
					.filter(t -> t.getAttributes().containsKey(Terminology.Attributes.autocomplete.property()))
					.collect(Collectors.toList());

			// Perform ngram search for chars typed
			final Integer maxLimit = (limit == null) ? 10 : Math.min(limit, 1000);
			final int doubleQueryLength = (query.length() < 5 ? 5 : query.length()) * 2;
			final SearchParameters params = new SearchParameters(
					"name.ngram:\"" + StringUtility.escapeQuery(query) + "\"" + " AND ((length:[* TO "
							+ doubleQueryLength + "])^100" + " OR length:[" + (doubleQueryLength + 1) + " TO *])",
					0, maxLimit * 2, null, null);
			final ResultList<Term> list = searchService.findFields(params, ModelUtility.asList("name"), Term.class);
			final Set<String> seen = new HashSet<>();
			return new ResponseEntity<>(list.getItems().stream()
					.filter(t -> !seen.contains(t.getName().toLowerCase()) && seen.size() < maxLimit)
					.peek(t -> seen.add(t.getName().toLowerCase())).map(t -> t.getName()).collect(Collectors.toList()),
					new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to find autocomplete terms");
			return null;
		}
	}

	/**
	 * Lookup.
	 *
	 * @param terminology the terminology
	 * @param expression  the expression
	 * @param limit       the limit
	 * @param active      the active
	 * @param leaf        the leaf
	 * @param include     the include
	 * @param handler     the handler
	 * @param queries     the queries
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/bulk", method = RequestMethod.POST)
	@Operation(summary = "Bulk find of concepts across specified project terminologies", description = "Bulk find of concepts matching specified search criteria.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"concept" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "List of result lists of matching concepts for each query"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({ @Parameter(name = "idOrUriLabel", description = "Project id or uriLabel", required = true),
			@Parameter(name = "terminology", description = "Comma-separated list of terminology ids or abbreviations (or null for all terminologies)."
					+ " e.g. \"uuid1,uuid2\", \"SNOMEDCT,RXNORM\", or \"ICD10CM\".", required = false),
			@Parameter(name = "expression", description = "ECL-style expression"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/EXPRESSION.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "limit", description = "Limit of results to return, max is 10", required = false, schema = @Schema(implementation = Integer.class), example = "1"),
			@Parameter(name = "active", description = "<code>true</code> for active concepts only, <code>false</code> for inactive concepts only,"
					+ " <code>null</code> for both", required = false, schema = @Schema(implementation = Boolean.class)),
			@Parameter(name = "leaf", description = "<code>true</code> for leaf nodes only, <code>false</code> for non-leaf nodes,"
					+ " <code>null</code> for either", required = false, schema = @Schema(implementation = Boolean.class)),
			@Parameter(name = "include", description = "Indicator of how much data to return. Comma-separated list of any of the following values: "
					+ "minimal, summary, full, axioms, attributes, children, definitions, descendants, "
					+ "highlights, inverseRelationships, mapsets, parents, relationships, semanticTypes, "
					+ "subsets, terms, treePositions"
					+ "<a href='https://github.com/TerminologyHub/termhub-in-5-minutes/blob/main/doc/INCLUDE.md' target='_blank'>See here "
					+ "for detailed information</a>.", required = false) })
	@RequestBody(description = "Newline-separated lines of text, one line for each query", required = true, content = @Content(schema = @Schema(implementation = String.class), mediaType = MediaType.TEXT_PLAIN, examples = {
			@ExampleObject(value = "heart\nkidney\n12738006") }))
	public ResponseEntity<List<ResultListConcept>> lookup(
			@RequestParam(value = "terminology", required = false) final String terminology,
			@RequestParam(name = "expression", required = false) final String expression,
			@RequestParam(name = "limit", required = false, defaultValue = "1") final Integer limit,
			@RequestParam(name = "active", required = false) final Boolean active,
			@RequestParam(name = "leaf", required = false) final Boolean leaf,
			@RequestParam(value = "include", required = false) final String include,
			@RequestParam(name = "handler", required = false) @Parameter(hidden = true) final String handler,
			@org.springframework.web.bind.annotation.RequestBody(required = false) @Parameter(hidden = true) final String queries)
			throws Exception {

		final AuthContext context = authorize(request);
		try {

			final IncludeParam ip = new IncludeParam(include == null ? "semanticTypes" : include);
			final List<Terminology> tlist = lookupTerminologies(context, terminology);
			final String[] array = ModelUtility.nvl(queries, "").split("\n");

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, tlist.get(0).getPublisher(),
//					tlist.get(0).getAbbreviation(), tlist.get(0).getVersion(), "CONCEPT-BATCH", "size",
//					array.length + "");

			final List<ResultListConcept> list = new ArrayList<>();
			for (final String query : array) {

				final String query2 = QueryBuilder.findBuilder(builders, handler).buildQuery(query);
				final int useLimit = limit == null ? 1 : (limit > 10 ? 10 : limit);
				final ResultList<Concept> result = findHelper(tlist, query2, expression, 0, useLimit, null, null,
						active, leaf, null, ip);
				result.getParameters().setQuery(query2);
				result.getParameters().setExpression(expression);

				// Round confidences to 4 places
				result.getItems().stream().forEach(c -> c.setConfidence(Precision.round(c.getConfidence(), 3)));

				if (query.isEmpty() || result.getItems().isEmpty()) {
					final Concept concept = new Concept();
					concept.setConfidence(0.0);
					concept.setCode("");
					concept.setTerminology("");
					concept.setPublisher("");
					concept.getSemanticTypes().add("");
					concept.setName("No match");
					result.getItems().clear();
					result.getItems().add(concept);
				}
				list.add(new ResultListConcept(result));
			}

			return new ResponseEntity<>(list, new HttpHeaders(), HttpStatus.OK);

		} catch (

		final Exception e) {
			handleException(e, "trying to bulk find concepts");
			return null;
		}
	}

	/**
	 * Export lookup.
	 *
	 * @param terminology the terminology
	 * @param expression  the expression
	 * @param limit       the limit
	 * @param active      the active
	 * @param leaf        the leaf
	 * @param handler     the handler
	 * @param queries     the queries
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/bulk/export", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN)
	@Operation(summary = "Export bulk find of concepts across specified project terminologies", description = "Export bulk find of concepts matching specified search criteria.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"concept" })

	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Tab separated values file content for matching concepts", content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({ @Parameter(name = "idOrUriLabel", description = "Project id or uriLabel", required = true),
			@Parameter(name = "terminology", description = "Comma-separated list of terminology ids or abbreviations (or null for all terminologies)."
					+ " e.g. \"uuid1,uuid2\", \"SNOMEDCT,RXNORM\", or \"ICD10CM\".", required = false),
			@Parameter(name = "expression", description = "ECL-style expression"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/EXPRESSION.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "limit", description = "Limit of results to return, max is 10", required = false, schema = @Schema(implementation = Integer.class), example = "1"),
			@Parameter(name = "active", description = "<code>true</code> for active concepts only, <code>false</code> for inactive concepts only,"
					+ " <code>null</code> for both", required = false, schema = @Schema(implementation = Boolean.class)),
			@Parameter(name = "leaf", description = "<code>true</code> for leaf nodes only, <code>false</code> for non-leaf nodes,"
					+ " <code>null</code> for either", required = false, schema = @Schema(implementation = Boolean.class)) })
	@RequestBody(description = "Newline-separated lines of text, one line for each query", required = true, content = @Content(schema = @Schema(implementation = String.class), mediaType = MediaType.TEXT_PLAIN, examples = {
			@ExampleObject(value = "heart\nkidney\n12738006") }))
	public ResponseEntity<Resource> exportLookup(
			@RequestParam(value = "terminology", required = false) final String terminology,
			@RequestParam(name = "expression", required = false) final String expression,
			@RequestParam(name = "limit", required = false, defaultValue = "1") final Integer limit,
			@RequestParam(name = "active", required = false) final Boolean active,
			@RequestParam(name = "leaf", required = false) final Boolean leaf,
			@RequestParam(name = "handler", required = false) @Parameter(hidden = true) final String handler,
			@org.springframework.web.bind.annotation.RequestBody(required = false) @Parameter(hidden = true) final String queries)
			throws Exception {

		final AuthContext context = authorize(request);
		try {

			final IncludeParam ip = new IncludeParam("semanticTypes");
			final List<Terminology> tlist = lookupTerminologies(context, terminology);
			final String[] array = ModelUtility.nvl(queries, "").split("\n");

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, tlist.get(0).getPublisher(),
//					tlist.get(0).getAbbreviation(), tlist.get(0).getVersion(), "EXPORT", "lookup", array.length + "");

			// Build export file
			final StringBuilder sb = new StringBuilder("term\tterminology\tcode\tname\ttype\tconfidence\n");

			final QueryBuilder builder = QueryBuilder.findBuilder(builders, handler);
			for (final String query : array) {
				final String query2 = builder.buildQuery(query);
				final int useLimit = limit == null ? 1 : (limit > 10 ? 10 : limit);
				final ResultList<Concept> result = findHelper(tlist, query2, expression, 0, useLimit, null, null,
						active, leaf, null, ip);

				// Round confidences to 4 places
				result.getItems().stream().forEach(c -> c.setConfidence(Precision.round(c.getConfidence(), 3)));

				// Show empty results for an empty query
				if (query.isEmpty() || result.getItems().isEmpty()) {
					final Concept concept = new Concept();
					concept.setConfidence(0.0);
					concept.setCode("");
					concept.setTerminology("");
					concept.setPublisher("");
					concept.getSemanticTypes().add("");
					concept.setName("No match");
					result.getItems().clear();
					result.getItems().add(concept);
				}

				// TODO: add terminology and semantic type
				for (final Concept concept : result.getItems()) {
					sb.append(query).append("\t").append(concept.getTerminology()).append("\t")
							.append(concept.getCode()).append("\t").append(concept.getName()).append("\t")
							.append(String.join(";", concept.getSemanticTypes())).append("\t")
							.append(concept.getConfidence());
					// Always just append with "\n"
					sb.append("\n");
				}

			}

			// Package data
			final ByteArrayInputStream bais = new ByteArrayInputStream(String.valueOf(sb).getBytes());
			final InputStreamResource inputStreamResource = new InputStreamResource(bais);
			final HttpHeaders headers = new HttpHeaders();

			// Compute filename
			final LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
			final String dateString = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

			// snomed.terminology.tools uses terminology instead of projectId
			final String filename = "bulk".concat("-search-").concat(dateString).concat(".xls");

			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
			headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
					HttpHeaders.CONTENT_DISPOSITION + "," + HttpHeaders.ACCEPT + "," + HttpHeaders.AUTHORIZATION);

			return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);

		} catch (

		final Exception e) {
			handleException(e, "trying to bulk find concepts");
			return null;
		}
	}

	/**
	 * Export concepts.
	 *
	 * @param terminology the terminology
	 * @param query       the query
	 * @param expression  the expression
	 * @param sort        the sort
	 * @param ascending   the ascending
	 * @param active      the active
	 * @param leaf        the leaf
	 * @param handler     the handler
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/export", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN)
	@Operation(summary = "Export concept search across all project terminologies", description = "Export concept search matching specified search criteria.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"concept" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Tab separated values file content for matching concepts", content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({ @Parameter(name = "idOrUriLabel", description = "Project id or uriLabel", required = true),
			@Parameter(name = "terminology", description = "Comma-separated list of terminology ids or abbreviations (or null for all terminologies)."
					+ " e.g. \"uuid1,uuid2\", \"SNOMEDCT,RXNORM\", or \"ICD10CM\".", required = false),
			@Parameter(name = "query", description = "Search text"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/SEARCH.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "expression", description = "ECL-style expression"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/EXPRESSION.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false, schema = @Schema(implementation = String.class)),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false, schema = @Schema(implementation = Boolean.class)),
			@Parameter(name = "active", description = "<code>true</code> for active concepts only, <code>false</code> for inactive concepts only,"
					+ " <code>null</code> for both", required = false, schema = @Schema(implementation = Boolean.class)),
			@Parameter(name = "leaf", description = "<code>true</code> for leaf nodes only, <code>false</code> for non-leaf nodes,"
					+ " <code>null</code> for either", required = false, schema = @Schema(implementation = Boolean.class)) })
	public ResponseEntity<Resource> exportConcepts(@RequestParam(value = "terminology") final String terminology,
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "expression", required = false) final String expression,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "ascending", required = false) final Boolean ascending,
			@RequestParam(name = "active", required = false) final Boolean active,
			@RequestParam(name = "leaf", required = false) final Boolean leaf,
			@RequestParam(name = "handler", required = false) @Parameter(hidden = true) final String handler)
			throws Exception {

		final AuthContext context = authorize(request);
		try {

			final List<Terminology> tlist = lookupTerminologies(context, terminology);

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, tlist.get(0).getPublisher(),
//					tlist.get(0).getAbbreviation(), tlist.get(0).getVersion(), "EXPORT", "search", null);

			// Check for a single terminology
			final Terminology single = tlist.size() == 0 ? null : tlist.get(0);
			// If expression is specified, there can be only one terminology
			if (!StringUtility.isEmpty(expression) && tlist.size() > 1) {
				throw new RestException(false, 417, "Expecation failed",
						"Expression parameter can only be used in " + "conjunction with a single terminology");
			}

			// Build a query from the handler
			final String query2 = QueryBuilder.findBuilder(builders, handler).buildQuery(query);

			// use limit=2 here for the quick search
			final SearchParameters params = new SearchParameters(
					StringUtility.composeQuery("AND", query2, single == null ? null
							: TerminologyUtility.getExpressionQuery(searchService, single.getAbbreviation(),
									single.getPublisher(), single.getVersion(), expression, single.getIndexName())),
					0, 2, sort, ascending);
			if (active != null && active) {
				params.setActive(true);
			}
			if (leaf != null && leaf) {
				params.setLeaf(true);
			}
			if (sort == null) {
				// Do a quick search (limit=2) - if there are > 5000 things, use "id" sort.
				// Otherwise we won't have to worry about it
				if (searchService.find(params, Concept.class).getTotal() > 5000) {
					params.setSort(ModelUtility.asList("id"));
				}
			}

			// Build export file
			// Begin Concepts exporting
			final StringBuilder stringBuilder = new StringBuilder("terminology\tcode\tname\tsemantic type\n");

			// If number of entities in ElasticSearch is large perform a findAll and let UI
			// filter
			// BAC: I set this to 5K instead of 10 because concepts can be large in some
			// contexts
			params.setLimit(5000);
			final IncludeParam ip = new IncludeParam("semanticTypes");

			// begin duplicate logic from searchService.findAll(finalQuery, clazz)
			while (true) {
				// Use findFields here with the include param
				final List<Concept> innerList = searchService
						.findFields(params, new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class)
						.getItems();

				for (final Concept concept : innerList) {
					stringBuilder.append(concept.getTerminology()).append("\t").append(concept.getCode()).append("\t")
							.append(concept.getName()).append("\t")
							.append(String.join(":", concept.getSemanticTypes()));
					// Always just append with "\n"
					stringBuilder.append("\n");
				}

				// If no results, or less than this page, we're done
				if (innerList.isEmpty() || innerList.size() < 5000) {
					break;
				}

			}

			final ByteArrayInputStream bais = new ByteArrayInputStream(String.valueOf(stringBuilder).getBytes());

			final InputStreamResource inputStreamResource = new InputStreamResource(bais);
			final HttpHeaders headers = new HttpHeaders();

			// Compute filename
			final LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
			final String dateString = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

			// snomed.terminology.tools uses terminology instead of projectId
			final String filename = "export".concat("-concepts-").concat(dateString).concat(".xls");

			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
			headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
					HttpHeaders.CONTENT_DISPOSITION + "," + HttpHeaders.ACCEPT + "," + HttpHeaders.AUTHORIZATION);

			return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);

		} catch (

		final Exception e) {
			handleException(e, "trying to export concepts");
			return null;
		}
	}

	/**
	 * Find helper.
	 *
	 * @param terminologies the terminologies
	 * @param query         the query
	 * @param expression    the expression
	 * @param offset        the offset
	 * @param limit         the limit
	 * @param sort          the sort
	 * @param ascending     the ascending
	 * @param active        the active
	 * @param leaf          the leaf
	 * @param handler       the handler
	 * @param ip            the ip
	 * @return the result list
	 * @throws Exception the exception
	 */
	private ResultList<Concept> findHelper(final List<Terminology> terminologies, final String query,
			final String expression, final Integer offset, final Integer limit, final String sort,
			final Boolean ascending, final Boolean active, final Boolean leaf, final String handler,
			final IncludeParam ip) throws Exception {

		// Check for a single terminology
		final Terminology single = terminologies.size() == 0 ? null : terminologies.get(0);
		// If expression is specified, there can be only one terminology
		if (!StringUtility.isEmpty(expression) && terminologies.size() > 1) {
			throw new RestException(false, 417, "Expecation failed",
					"Expression parameter can only be used in " + "conjunction with a single terminology");
		}

		final SearchParameters params = new SearchParameters(
				StringUtility.composeQuery("AND", query,
						single == null ? null
								: TerminologyUtility.getExpressionQuery(searchService, single.getAbbreviation(),
										single.getPublisher(), single.getVersion(), expression, single.getIndexName())),
				offset, limit, sort, ascending);
		if (active != null && active) {
			params.setActive(true);
		}
		if (leaf != null && leaf) {
			params.setLeaf(true);
		}
		final ResultList<Concept> list = searchService.findFields(params,
				new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

		for (final Concept concept : list.getItems()) {
			concept.cleanForApi();
			TerminologyUtility.populateConcept(concept, ip, single, searchService);
		}

		// Restore the original query for the response
		params.setQuery(query);
		params.setExpression(expression);
		list.setParameters(params);
		return list;

	}

	/**
	 * Find mapset helper.
	 *
	 * @param mapsets   the mapsets
	 * @param query     the query
	 * @param offset    the offset
	 * @param limit     the limit
	 * @param sort      the sort
	 * @param ascending the ascending
	 * @param active    the active
	 * @param leaf      the leaf
	 * @return the result list
	 * @throws Exception the exception
	 */
	private ResultList<Mapping> findMappingsHelper(final List<Mapset> mapsets, final String query, final Integer offset,
			final Integer limit, final String sort, final Boolean ascending, final Boolean active, final Boolean leaf)
			throws Exception {

		// We are not using multiple indexes, so we instead have to add constraints
		final String mapsetClause = "(" + String.join(" OR ", mapsets.stream()
				.map(m -> "(" + StringUtility.composeQuery("AND", "mapset.abbreviation:" + m.getAbbreviation(),
						"mapset.publisher:" + m.getPublisher(), "mapset.version:" + m.getVersion()) + ")")
				.collect(Collectors.toList())) + ")";
		final SearchParameters params = new SearchParameters(StringUtility.composeQuery("AND", query, mapsetClause),
				offset, limit, sort, ascending);
		if (active != null && active) {
			params.setActive(true);
		}
		if (leaf != null && leaf) {
			params.setLeaf(true);
		}

		// Bail if no mapsets in project
		if (ModelUtility.isEmpty(mapsets)) {
			return new ResultList<Mapping>();
		}

		final ResultList<Mapping> list = searchService.find(params, Mapping.class);

		// for (final Mapping mapping : list.getItems()) {
		// mapping.cleanForApi();
		// TerminologyUtility.populateConcept(mapping, ip, single, searchService);
		// }

		// Sort the mappings by mapsetCode, group, priority
		Collections.sort(list.getItems(), new Comparator<Mapping>() {

			/* see superclass */
			@Override
			public int compare(final Mapping m1, final Mapping m2) {
				final String k1 = m1.getMapset().getCode() + StringUtils.leftPad(m1.getGroup(), 2, "0")
						+ StringUtils.leftPad(m1.getPriority(), 2, "0");
				final String k2 = m2.getMapset().getCode() + StringUtils.leftPad(m2.getGroup(), 2, "0")
						+ StringUtils.leftPad(m2.getPriority(), 2, "0");
				return k1.compareTo(k2);
			}
		});

		// Restore the original query for the response
		params.setQuery(query);
		list.setParameters(params);
		return list;

	}

	/**
	 * Find metadata.
	 *
	 * @param query     the query
	 * @param offset    the offset
	 * @param limit     the limit
	 * @param sort      the sort
	 * @param ascending the ascending
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/metadata", method = RequestMethod.GET)
	@Operation(summary = "Find project terminology metadata", description = "Find metadata for the terminologies of the specified project id or uri", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"metadata" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Matadata for the specified project and query parameters"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "query", description = "Search text"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/SEARCH.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "offset", description = "Start index for search results", required = false, example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false), })
	public ResponseEntity<ResultListMetadata> findMetadata(
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "ascending", required = false) final Boolean ascending) throws Exception {

		@SuppressWarnings("unused")
		final AuthContext context = authorize(request);
		try {

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, null, null, null, "METADATA", "find",
//					null);

			final Map<String, Terminology> map = lookupTerminologyMap();
			if (map.size() == 0) {
				throw new RestException(false, 417, "Not Found", "Unexpected project without terminologies.");
			}

			// limit return objects to 20000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 20000);

			final SearchParameters params = new SearchParameters(query, offset, maxLimit, sort, ascending);
			final ResultList<Metadata> list = searchService.find(params, Metadata.class);

			// Minimize
			list.setParameters(params);
			return new ResponseEntity<>(new ResultListMetadata(list), new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to find metadata");
			return null;
		}
	}

	/**
	 * Find concept relationships.
	 *
	 * @param conceptId the concept id
	 * @param query     the query
	 * @param offset    the offset
	 * @param limit     the limit
	 * @param ascending the ascending
	 * @param sort      the sort
	 * @param handler   the handler
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{conceptId}/relationships", method = RequestMethod.GET)
	@Operation(summary = "Find concept relationships", description = "Finds concept relationships for the specified concept id.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"concept by id" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Result list of matching concept relationships"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true),
			@Parameter(name = "query", description = "Search text"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/SEARCH.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "offset", description = "Start index for search results", required = false, example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false) })

	public ResponseEntity<ResultListConceptRelationship> findConceptRelationships(
			@PathVariable("conceptId") final String conceptId,
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "ascending", required = false) final Boolean ascending,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "handler", required = false) @Parameter(hidden = true) final String handler)
			throws Exception {

		@SuppressWarnings("unused")
		final AuthContext context = authorize(request);
		try {

			final Map<String, Terminology> indexMap = lookupTerminologyMap();

			// look up concept first and get code
			final Concept concept = searchService.get(conceptId, Concept.class);
			if (concept == null) {
				throw new RestException(false, 404, "Not Found", "Unable to find concept = " + conceptId);
			}

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, concept.getPublisher(),
//					concept.getTerminology(), concept.getVersion(), "CONCEPT-BY-ID", "relationships", null);

			// Choose indexName for the concept
			final Terminology terminology = getTerminology(indexMap, concept);

			// limit return objects to 1000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

			final SearchParameters params = new SearchParameters(
					StringUtility.composeQuery("AND", "from.code:" + StringUtility.escapeQuery(concept.getCode()),
							QueryBuilder.findBuilder(builders, handler).buildQuery(query)),
					offset, maxLimit, sort, ascending);

			final ResultList<ConceptRelationship> list = searchService.find(params, ConceptRelationship.class);

			list.setParameters(params);
			return new ResponseEntity<>(new ResultListConceptRelationship(list), new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to find concept relationships");
			return null;
		}
	}

	/**
	 * Find concept relationships.
	 *
	 * @param terminology the terminology
	 * @param code        the code
	 * @param query       the query
	 * @param offset      the offset
	 * @param limit       the limit
	 * @param ascending   the ascending
	 * @param sort        the sort
	 * @param handler     the handler
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{terminology}/{code}/relationships", method = RequestMethod.GET)
	@Operation(summary = "Find concept relationships by terminology and code", description = "Finds concept relationships for the specified terminology and code."
			+ " This call will not work for projects hosting multiple versions of the same terminology.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
					"concept by code" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Result list of matching concept relationships"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "terminology", description = "Terminology id or abbreviation."
					+ " e.g. \"uuid1\" or \"ICD10CM\"."),
			@Parameter(name = "code", description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"", required = true),
			@Parameter(name = "query", description = "Search text"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/SEARCH.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "offset", description = "Start index for search results", required = false, example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false) })
	public ResponseEntity<ResultListConceptRelationship> findConceptRelationships(

			@PathVariable("terminology") final String terminology, @PathVariable("code") final String code,
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "ascending", required = false) final Boolean ascending,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "handler", required = false) @Parameter(hidden = true) final String handler)
			throws Exception {

		final AuthContext context = authorize(request);
		try {

			final Terminology term = lookupTerminology(context, terminology);

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, term.getPublisher(),
//					term.getAbbreviation(), term.getVersion(), "CONCEPT-BY-CODE", "relationships", null);

			// limit return objects to 1000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

			final SearchParameters params = new SearchParameters(
					StringUtility.composeQuery("AND", "terminology:" + StringUtility.escapeQuery(terminology),
							"from.code:" + StringUtility.escapeQuery(code),
							QueryBuilder.findBuilder(builders, handler).buildQuery(query)),
					offset, maxLimit, sort, ascending);

			final ResultList<ConceptRelationship> list = searchService.find(params, ConceptRelationship.class);
			/**
			 * for (final ConceptRelationship rel : list.getItems()) {
			 * checkTerminology(project, rel); }
			 */

			list.setParameters(params);
			return new ResponseEntity<>(new ResultListConceptRelationship(list), new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to find concept relationships");
			return null;
		}
	}

	/**
	 * Find concept inverse relationships.
	 *
	 * @param conceptId the concept id
	 * @param query     the query
	 * @param offset    the offset
	 * @param limit     the limit
	 * @param ascending the ascending
	 * @param sort      the sort
	 * @param handler   the handler
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{conceptId}/inverseRelationships", method = RequestMethod.GET)
	@Operation(summary = "Find concept inverse relationships", description = "Finds concept inverse relationships for the specified concept id.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"concept by id" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Result list of matching concept inverse relationships"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true),
			@Parameter(name = "query", description = "Search text"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/SEARCH.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "offset", description = "Start index for search results", required = false, example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false) })
	public ResponseEntity<ResultListConceptRelationship> findConceptInverseRelationships(
			@PathVariable("conceptId") final String conceptId,
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "ascending", required = false) final Boolean ascending,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "handler", required = false) @Parameter(hidden = true) final String handler)
			throws Exception {

		@SuppressWarnings("unused")
		final AuthContext context = authorize(request);
		try {

			final Map<String, Terminology> indexMap = lookupTerminologyMap();

			// look up concept first and get code
			// then do a find on the query
			final Concept concept = searchService.get(conceptId, Concept.class);
			if (concept == null) {
				throw new RestException(false, 404, "Not Found", "Unable to find concept = " + conceptId);
			}

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, concept.getPublisher(),
//					concept.getTerminology(), concept.getVersion(), "CONCEPT-BY-ID", "inverseRelationships", null);

			// Choose indexName for the concept
			final Terminology terminology = getTerminology(indexMap, concept);

			// limit return objects to 1000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

			final SearchParameters params = new SearchParameters(
					StringUtility.composeQuery("AND", "to.code:" + StringUtility.escapeQuery(concept.getCode()),
							QueryBuilder.findBuilder(builders, handler).buildQuery(query)),
					offset, maxLimit, sort, ascending);

			final ResultList<ConceptRelationship> list = searchService.find(params, ConceptRelationship.class);

			list.setParameters(params);
			return new ResponseEntity<>(new ResultListConceptRelationship(list), new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to find concept inverse relationships");
			return null;
		}
	}

	/**
	 * Find concept inverse relationships.
	 *
	 * @param terminology the terminology
	 * @param code        the code
	 * @param query       the query
	 * @param offset      the offset
	 * @param limit       the limit
	 * @param ascending   the ascending
	 * @param sort        the sort
	 * @param handler     the handler
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{terminology}/{code}/inverseRelationships", method = RequestMethod.GET)
	@Operation(summary = "Find concept inverse relationships by terminology and code", description = "Finds concept inverse relationships for the specified terminology and code."
			+ " This call will not work for projects hosting multiple versions of the same terminology.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
					"concept by code" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Result list of matching concept inverse relationships"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "terminology", description = "Terminology id or abbreviation."
					+ " e.g. \"uuid1\" or \"ICD10CM\"."),
			@Parameter(name = "code", description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"", required = true),
			@Parameter(name = "query", description = "Search text"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/SEARCH.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "offset", description = "Start index for search results", required = false, example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false) })
	public ResponseEntity<ResultListConceptRelationship> findConceptInverseRelationships(

			@PathVariable("terminology") final String terminology, @PathVariable("code") final String code,
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "ascending", required = false) final Boolean ascending,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "handler", required = false) @Parameter(hidden = true) final String handler)
			throws Exception {

		final AuthContext context = authorize(request);
		try {

			final Terminology term = lookupTerminology(context, terminology);

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, term.getPublisher(),
//					term.getAbbreviation(), term.getVersion(), "CONCEPT-BY-CODE", "inverseRelationships", null);

			// limit return objects to 1000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

			final SearchParameters params = new SearchParameters(
					StringUtility.composeQuery("AND", "terminology:" + StringUtility.escapeQuery(terminology),
							"to.code:" + StringUtility.escapeQuery(code),
							QueryBuilder.findBuilder(builders, handler).buildQuery(query)),
					offset, maxLimit, sort, ascending);

			final ResultList<ConceptRelationship> list = searchService.find(params, ConceptRelationship.class);
			/**
			 * for (final ConceptRelationship rel : list.getItems()) {
			 * checkTerminology(project, rel); }
			 */

			list.setParameters(params);
			return new ResponseEntity<>(new ResultListConceptRelationship(list), new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to find concept inverse relationships");
			return null;
		}
	}

	/**
	 * Find tree positions.
	 *
	 * @param conceptId the concept id
	 * @param query     the query
	 * @param offset    the offset
	 * @param limit     the limit
	 * @param ascending the ascending
	 * @param sort      the sort
	 * @param handler   the handler
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{conceptId}/trees", method = RequestMethod.GET)
	@Operation(summary = "Find concept tree positions", description = "Finds concept tree positions for the specified concept id.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"concept by id" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Result list of matching concept tree positions"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true),
			@Parameter(name = "query", description = "Search text"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/SEARCH.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "offset", description = "Start index for search results", required = false, example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false) })
	public ResponseEntity<ResultListConceptTreePosition> findTreePositions(
			@PathVariable("conceptId") final String conceptId,
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "ascending", required = false) final Boolean ascending,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "handler", required = false) @Parameter(hidden = true) final String handler)
			throws Exception {

		@SuppressWarnings("unused")
		final AuthContext context = authorize(request);
		try {

			final Map<String, Terminology> map = lookupTerminologyMap();

			// look up concept first and get code,
			// then do a find on the query
			final Concept concept = searchService.get(conceptId, Concept.class);
			if (concept == null) {
				throw new RestException(false, 404, "Not Found", "Unable to find concept = " + conceptId);
			}

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, concept.getPublisher(),
//					concept.getTerminology(), concept.getVersion(), "CONCEPT-BY-ID", "treePositions", null);

			// Choose indexName for the concept
			final Terminology terminology = getTerminology(map, concept);

			// limit return objects to 1000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

			final SearchParameters params = new SearchParameters(
					StringUtility.composeQuery("AND", "concept.code:" + StringUtility.escapeQuery(concept.getCode()),
							QueryBuilder.findBuilder(builders, handler).buildQuery(query)),
					offset, maxLimit, sort, ascending);

			final ResultList<ConceptTreePosition> list = searchService.find(params, ConceptTreePosition.class);

			final ResultList<ConceptTreePosition> treeList = new ResultList<>();
			for (final ConceptTreePosition treepos : list.getItems()) {

				final ConceptTreePosition tree = TerminologyUtility.computeTree(searchService, treepos,
						terminology.getIndexName());
				treeList.getItems().add(tree);
			}
			treeList.setParameters(params);
			treeList.setTotal(list.getTotal());

			return new ResponseEntity<>(new ResultListConceptTreePosition(treeList), new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to find concept tree positions");
			return null;
		}
	}

	/**
	 * Find tree positions.
	 *
	 * @param terminology the terminology
	 * @param code        the code
	 * @param query       the query
	 * @param offset      the offset
	 * @param limit       the limit
	 * @param ascending   the ascending
	 * @param sort        the sort
	 * @param handler     the handler
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{terminology}/{code}/trees", method = RequestMethod.GET)
	@Operation(summary = "Find concept tree positions by terminology and code", description = "Finds concept tree positions for the specified terminology and code."
			+ " This call will not work for projects hosting multiple versions of the same terminology.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
					"concept by code" })
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Result list of matching concept tree positions"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "terminology", description = "Terminology id or abbreviation."
					+ " e.g. \"uuid1\" or \"ICD10CM\"."),
			@Parameter(name = "code", description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"", required = true),
			@Parameter(name = "query", description = "Search text"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/SEARCH.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "offset", description = "Start index for search results", required = false, example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false) })
	public ResponseEntity<ResultListConceptTreePosition> findTreePositions(

			@PathVariable("terminology") final String terminology, @PathVariable("code") final String code,
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "ascending", required = false) final Boolean ascending,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "handler", required = false) @Parameter(hidden = true) final String handler)
			throws Exception {

		final AuthContext context = authorize(request);
		try {

			final Terminology term = lookupTerminology(context, terminology);

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, term.getPublisher(),
//					term.getAbbreviation(), term.getVersion(), "CONCEPT-BY-CODE", "treePositions", null);

			// limit return objects to 1000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

			final SearchParameters params = new SearchParameters(
					StringUtility.composeQuery("AND", "terminology:" + StringUtility.escapeQuery(terminology),
							"concept.code:" + StringUtility.escapeQuery(code),
							QueryBuilder.findBuilder(builders, handler).buildQuery(query)),
					offset, maxLimit, sort, ascending);

			final ResultList<ConceptTreePosition> list = searchService.find(params, ConceptTreePosition.class);

			final ResultList<ConceptTreePosition> treeList = new ResultList<>();
			for (final ConceptTreePosition treepos : list.getItems()) {
				// checkTerminology(project, treepos);

				final ConceptTreePosition tree = TerminologyUtility.computeTree(searchService, treepos,
						term.getIndexName());
				treeList.getItems().add(tree);
			}
			treeList.setParameters(params);
			treeList.setTotal(list.getTotal());

			return new ResponseEntity<>(new ResultListConceptTreePosition(treeList), new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to find concept tree positions");
			return null;
		}
	}

	/**
	 * Find tree position children.
	 *
	 * @param conceptId the concept id
	 * @param query     the query
	 * @param offset    the offset
	 * @param limit     the limit
	 * @param ascending the ascending
	 * @param sort      the sort
	 * @param handler   the handler
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{conceptId}/trees/children", method = RequestMethod.GET)

	@Operation(summary = "Find concept tree position children", description = "Finds concept tree position children for the specified concept id.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"concept by id" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Result list of matching concept tree position children"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true),
			@Parameter(name = "query", description = "Search text"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/SEARCH.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "offset", description = "Start index for search results", required = false, example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false)

	})
	public ResponseEntity<ResultListConceptTreePosition> findTreePositionChildren(
			@PathVariable("conceptId") final String conceptId,
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "ascending", required = false) final Boolean ascending,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "handler", required = false) @Parameter(hidden = true) final String handler)
			throws Exception {

		@SuppressWarnings("unused")
		final AuthContext context = authorize(request);
		try {

			final Map<String, Terminology> map = lookupTerminologyMap();

			// look up concept first and get code
			// then do a find on the query
			final Concept concept = searchService.get(conceptId, Concept.class);
			if (concept == null) {
				throw new RestException(false, 404, "Not Found", "Unable to find concept = " + conceptId);
			}

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, concept.getPublisher(),
//					concept.getTerminology(), concept.getVersion(), "CONCEPT-BY-ID", "treePosition.children", null);

			// Choose indexName for the concept
			final Terminology terminology = getTerminology(map, concept);

			// Find this thing
			final SearchParameters params = new SearchParameters(1, 0);
			params.setQuery(
					StringUtility.composeQuery("AND", QueryBuilder.findBuilder(builders, handler).buildQuery(query),
							"terminology:" + StringUtility.escapeQuery(concept.getTerminology()),
							"concept.code:" + StringUtility.escapeQuery(concept.getCode())));

			// limit return objects to 1000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

			final ResultList<ConceptTreePosition> list = searchService.find(params, ConceptTreePosition.class);
			if (list.getItems().size() == 0) {
				list.setParameters(new SearchParameters(null, offset, maxLimit, sort, ascending));
				return new ResponseEntity<>(new ResultListConceptTreePosition(list), new HttpHeaders(), HttpStatus.OK);
			}
			// The first one is OK because all nodes will have the same children.
			final ConceptTreePosition tp = list.getItems().get(0);

			// Find its children
			final SearchParameters paramsChd = new SearchParameters(null, offset, maxLimit, sort, ascending);
			final String ancPath = (tp.getAncestorPath().isEmpty() ? "" : tp.getAncestorPath() + "~")
					+ concept.getCode();
			paramsChd.setQuery(
					StringUtility.composeQuery("AND", "terminology:" + StringUtility.escapeQuery(tp.getTerminology()),
							"ancestorPath:" + StringUtility.escapeQuery(ancPath),
							QueryBuilder.findBuilder(builders, handler).buildQuery(query)));
			final ResultList<ConceptTreePosition> listChd = searchService.find(paramsChd, ConceptTreePosition.class);
			listChd.setParameters(paramsChd);

			// NO LONGER NECESSARY
			// // Tree position computation doesn't set names, so we have to look
			// // them up here. When it does, stop doing this.
			// for (final ConceptTreePosition chd : listChd.getItems()) {
			// final ConceptRef ref = new ConceptRef();
			// final Concept concept2 = TerminologyUtility.getConcept(searchService,
			// chd.getTerminology(), chd.getPublisher(), chd.getVersion(),
			// chd.getConcept().getCode(), terminology.getIndexName());
			// ref.setId(concept2.getId());
			// ref.setName(concept2.getName());
			// ref.setCode(concept2.getCode());
			// ref.setLeaf(concept2.getLeaf());
			// chd.setConcept(ref);
			// }

			return new ResponseEntity<>(new ResultListConceptTreePosition(listChd), new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to find tree position children.");
			return null;
		}
	}

	/**
	 * Find tree position children.
	 *
	 * @param terminology the terminology
	 * @param code        the code
	 * @param query       the query
	 * @param offset      the offset
	 * @param limit       the limit
	 * @param ascending   the ascending
	 * @param sort        the sort
	 * @param handler     the handler
	 * @return the response entity
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	@RequestMapping(value = "/concept/{terminology}/{code}/trees/children", method = RequestMethod.GET)
	@Operation(summary = "Find concept tree position children by terminology and code", description = "Finds concept tree position children for the specified terminology and code."
			+ " This call will not work for projects hosting multiple versions of the same terminology.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
					"concept by code" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Result list of matching concept tree position children"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
			@ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content()) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "terminology", description = "Terminology id or abbreviation."
					+ " e.g. \"uuid1\" or \"ICD10CM\"."),
			@Parameter(name = "code", description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"", required = true),
			@Parameter(name = "query", description = "Search text"
					+ " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/SEARCH.md\">"
					+ "See here for more info</a>)", required = false),
			@Parameter(name = "offset", description = "Start index for search results", required = false, example = "0"),
			@Parameter(name = "limit", description = "Limit of results to return (hard limit of 1000 regardless of value)", required = false, example = "10"),
			@Parameter(name = "sort", description = "Comma-separated list of fields to sort on", required = false),
			@Parameter(name = "ascending", description = "<code>true</code> for ascending, <code>false</code> for descending,"
					+ " <code>null</code> for unspecified", required = false) })
	public ResponseEntity<ResultListConceptTreePosition> findTreePositionChildren(

			@PathVariable("terminology") final String terminology, @PathVariable("code") final String code,
			@RequestParam(name = "query", required = false) final String query,
			@RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
			@RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
			@RequestParam(name = "ascending", required = false) final Boolean ascending,
			@RequestParam(name = "sort", required = false) final String sort,
			@RequestParam(name = "handler", required = false) @Parameter(hidden = true) final String handler)
			throws Exception {

		final AuthContext context = authorize(request);
		try {

			final Terminology term = lookupTerminology(context, terminology);

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, term.getPublisher(),
//					term.getAbbreviation(), term.getVersion(), "CONCEPT-BY-CODE", "treePosition.children", null);

			// Find this thing
			final SearchParameters params = new SearchParameters(1, 0);
			params.setQuery(
					StringUtility.composeQuery("AND", QueryBuilder.findBuilder(builders, handler).buildQuery(query),
							"terminology:" + StringUtility.escapeQuery(terminology),
							"concept.code:" + StringUtility.escapeQuery(code)));

			// limit return objects to 1000 regardless of user request
			final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

			final ResultList<ConceptTreePosition> list = searchService.find(params, ConceptTreePosition.class);
			if (list.getItems().size() == 0) {
				list.setParameters(new SearchParameters(null, offset, maxLimit, sort, ascending));
				return new ResponseEntity<>(new ResultListConceptTreePosition(list), new HttpHeaders(), HttpStatus.OK);
			}
			// The first one is OK because all nodes will have the same children.
			final ConceptTreePosition tp = list.getItems().get(0);

			// Find its children
			final SearchParameters paramsChd = new SearchParameters(null, offset, maxLimit, sort, ascending);
			final String ancPath = (tp.getAncestorPath().isEmpty() ? "" : tp.getAncestorPath() + "~") + code;
			paramsChd.setQuery(
					StringUtility.composeQuery("AND", "terminology:" + StringUtility.escapeQuery(tp.getTerminology()),
							"ancestorPath:" + StringUtility.escapeQuery(ancPath),
							QueryBuilder.findBuilder(builders, handler).buildQuery(query)));
			final ResultList<ConceptTreePosition> listChd = searchService.find(paramsChd, ConceptTreePosition.class);
			listChd.setParameters(paramsChd);

			// NO LONGER NECESSARY
			// // Tree position computation doesn't set names, so we have to look
			// // them up here. When it does, stop doing this.
			// for (final ConceptTreePosition chd : listChd.getItems()) {
			// final ConceptRef ref = new ConceptRef();
			// final Concept concept = TerminologyUtility.getConcept(searchService,
			// chd.getTerminology(), chd.getPublisher(), chd.getVersion(),
			// chd.getConcept().getCode(), term.getIndexName());
			// ref.setId(concept.getId());
			// ref.setName(concept.getName());
			// ref.setCode(concept.getCode());
			// ref.setLeaf(concept.getLeaf());
			// chd.setConcept(ref);
			// }

			return new ResponseEntity<>(new ResultListConceptTreePosition(listChd), new HttpHeaders(), HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to find tree position children.");
			return null;
		}
	}

	/**
	 * Gets the concept diagram.
	 *
	 * @param conceptId the concept id
	 * @return the concept diagram
	 * @throws Exception the exception
	 */
	/* see superclass */
	@SuppressWarnings("resource")
	@Override
	@RequestMapping(value = "/concept/{conceptId}/diagram", method = RequestMethod.GET, produces = { "image/png",
			MediaType.APPLICATION_JSON })
	@Operation(summary = "Get concept diagram", description = "Gets concept diagram for the specified concept.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"concept by id" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Concept diagram", content = @Content(mediaType = "image/png")),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
			@ApiResponse(responseCode = "417", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON)) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true) })
	public @ResponseBody ResponseEntity<Resource> getConceptDiagram(@PathVariable("conceptId") final String conceptId)
			throws Exception {

		@SuppressWarnings("unused")
		final AuthContext context = authorize(request);
		try {
			final Map<String, Terminology> indexMap = lookupTerminologyMap();

			// look up concept first and get code
			// Get the concept
			final Concept concept = searchService.findSingle(
					new SearchParameters("id:" + StringUtility.escapeQuery(conceptId), null, null, null, null),
					Concept.class);
			if (concept == null) {
				throw new RestException(false, 417, "Expectation failed", "Specified concept is null " + conceptId);

			}
			final Terminology terminology = indexMap
					.get(concept.getTerminology() + concept.getPublisher() + concept.getVersion());
			if (terminology == null) {
				logger.error("    indexMap = " + indexMap);
				logger.error("    key = " + concept.getTerminology() + concept.getPublisher() + concept.getVersion());
				throw new RestException(false, 417, "Expectation failed",
						"Specified concept is not valid for this project =  " + conceptId);
			}

			// look up concept's relationships
			SearchParameters params = new SearchParameters("from.code:" + StringUtility.escapeQuery(concept.getCode()),
					null, 100000, null, null);
			final ResultList<ConceptRelationship> rels = searchService.find(params, ConceptRelationship.class);
			concept.setRelationships(rels.getItems());

			// lookup metadata
			params = new SearchParameters("", null, 100000, null, null);
			final ResultList<Metadata> metadata = searchService.find(params, Metadata.class);

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, concept.getPublisher(),
//					concept.getTerminology(), concept.getVersion(), "CONCEPT-BY-ID", "diagram", null);

			// call diagram application to generate image
			final HttpClient httpClient = HttpClientBuilder.create().build();
			final String diagramUrl = PropertyUtility.getProperties().getProperty("api.url.termhub-diagram-service");
			final HttpPost request = new HttpPost(diagramUrl + "/generate/diagram");
			final String diagramModel = TerminologyUtility.toDiagramModel(concept, metadata.getItems());
			final StringEntity payload = new StringEntity(diagramModel);
			request.addHeader("content-type", MediaType.APPLICATION_JSON);
			request.addHeader("accept", "image/png");
			request.setEntity(payload);
			logger.info("    get diagram = " + diagramModel);
			final HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() >= 400) {
				throw new RestException(false, 417, "Diagram service error", response.getStatusLine().toString());
			}
			// Return the diagram
			final HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=" + concept.getTerminology() + "-" + concept.getCode() + "-"
							+ StringUtility
									.substr(concept.getName().replaceAll(" ", "-").replaceAll("[^A-Za-z0-9]", "-"), 80)
							+ ".png");
			headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
					HttpHeaders.CONTENT_DISPOSITION + "," + HttpHeaders.ACCEPT + "," + HttpHeaders.AUTHORIZATION);

			return new ResponseEntity<>(new InputStreamResource(response.getEntity().getContent()), headers,
					HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to get concept diagram = " + conceptId);
			return null;
		}

	}

	/**
	 * Gets the concept diagram png. This method is for
	 * <img src="https://..../diagram.png">.
	 *
	 * @param conceptId the concept id
	 * @return the concept diagram png
	 * @throws Exception the exception
	 */
	/* see superclass */
	@SuppressWarnings("resource")
	@RequestMapping(value = "/concept/{conceptId}/diagram.png", method = RequestMethod.GET, produces = { "image/png" })
	@Hidden
	public @ResponseBody ResponseEntity<Resource> getConceptDiagramPng(
			@PathVariable("conceptId") final String conceptId) throws Exception {

		try {
			// Fake context for this call since we want to embed it in an img tag
			final AuthContext context = new AuthContext();
			context.setUserId("admin");
			context.setRole("ADMIN");
			final Map<String, Terminology> indexMap = lookupTerminologyMap();

			// look up concept first and get code
			// Get the concept
			final Concept concept = searchService.findSingle(
					new SearchParameters("id:" + StringUtility.escapeQuery(conceptId), null, null, null, null),
					Concept.class);
			if (concept == null) {
				throw new RestException(false, 417, "Expectation failed", "Specified concept is null " + conceptId);

			}
			final Terminology terminology = indexMap
					.get(concept.getTerminology() + concept.getPublisher() + concept.getVersion());
			if (terminology == null) {
				logger.error("    indexMap = " + indexMap);
				logger.error("    key = " + concept.getTerminology() + concept.getPublisher() + concept.getVersion());
				throw new RestException(false, 417, "Expectation failed",
						"Specified concept is not valid for this project =  " + conceptId);
			}

			// look up concept's relationships
			SearchParameters params = new SearchParameters("from.code:" + StringUtility.escapeQuery(concept.getCode()),
					null, 100000, null, null);
			final ResultList<ConceptRelationship> rels = searchService.find(params, ConceptRelationship.class);
			concept.setRelationships(rels.getItems());

			// lookup metadata
			params = new SearchParameters("", null, 100000, null, null);
			final ResultList<Metadata> metadata = searchService.find(params, Metadata.class);

			// call diagram application to generate image
			final HttpClient httpClient = HttpClientBuilder.create().build();
			final String diagramUrl = PropertyUtility.getProperties().getProperty("api.url.termhub-diagram-service");
			final HttpPost request = new HttpPost(diagramUrl + "/generate/diagram");
			final String diagramModel = TerminologyUtility.toDiagramModel(concept, metadata.getItems());
			final StringEntity payload = new StringEntity(diagramModel);
			request.addHeader("content-type", MediaType.APPLICATION_JSON);
			request.addHeader("accept", "image/png");
			request.setEntity(payload);
			logger.info("    get diagram = " + diagramModel);
			final HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() >= 400) {
				throw new RestException(false, 417, "Diagram service error", response.getStatusLine().toString());
			}
			// Return the diagram
			final HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=" + concept.getTerminology() + "-" + concept.getCode() + "-"
							+ StringUtility
									.substr(concept.getName().replaceAll(" ", "-").replaceAll("[^A-Za-z0-9]", "-"), 80)
							+ ".png");
			headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
					HttpHeaders.CONTENT_DISPOSITION + "," + HttpHeaders.ACCEPT + "," + HttpHeaders.AUTHORIZATION);

			return new ResponseEntity<>(new InputStreamResource(response.getEntity().getContent()), headers,
					HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to get concept diagram = " + conceptId);
			return null;
		}

	}

	/**
	 * Gets the concept diagram.
	 *
	 * @param terminology the terminology
	 * @param code        the code
	 * @return the concept diagram
	 * @throws Exception the exception
	 */
	/* see superclass */
	@SuppressWarnings("resource")
	@Override
	@RequestMapping(value = "/concept/{terminology}/{code}/diagram", method = RequestMethod.GET, produces = {
			"image/png", MediaType.APPLICATION_JSON })
	@Operation(summary = "Get concept diagram by terminology and code", description = "Gets concept diagram for the specified terminology and code.", security = @SecurityRequirement(name = "bearerAuth"), tags = {
			"concept by code" })
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Concept diagram", content = @Content(mediaType = "image/png")),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
			@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
			@ApiResponse(responseCode = "417", description = "Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON)) })
	@Parameters({
			@Parameter(name = "idOrUriLabel", description = "Project id or uriLabel, e.g. \"sandbox\"", required = true),
			@Parameter(name = "terminology", description = "Terminology id or abbreviation."
					+ " e.g. \"uuid1\" or \"ICD10CM\".", required = true),
			@Parameter(name = "code", description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"", required = true) })
	public @ResponseBody ResponseEntity<Resource> getConceptDiagram(

			@PathVariable("terminology") final String terminology, @PathVariable("code") final String code)
			throws Exception {

		final AuthContext context = authorize(request);
		try {

			final Terminology term = lookupTerminology(context, terminology);

			// find with code, term, pub, version
			final String query = StringUtility.composeQuery("AND", "code:" + StringUtility.escapeQuery(code),
					"terminology:" + StringUtility.escapeQuery(term.getAbbreviation()),
					"publisher:" + StringUtility.escapeQuery(term.getPublisher()),
					"version:" + StringUtility.escapeQuery(term.getVersion()));

			// then do a find on the query
			final IncludeParam ip = new IncludeParam("summary");
			SearchParameters params = new SearchParameters(query, null, 2, null, null);
			final ResultList<Concept> results = searchService.findFields(params,
					new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

			if (results.getTotal() == 0) {
				return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
			}
			if (results.getTotal() > 1) {
				throw new RestException(false, 417, "Expecation failed",
						"Too many matching concepts for terminology/code = " + terminology + ", " + code);
			}
			final Concept concept = results.getItems().get(0);

			// look up concept's relationships
			params = new SearchParameters("from.code:" + StringUtility.escapeQuery(concept.getCode()), null, 100000,
					null, null);
			final ResultList<ConceptRelationship> rels = searchService.find(params, ConceptRelationship.class);
			concept.setRelationships(rels.getItems());

			// lookup metadata
			params = new SearchParameters("", null, 100000, null, null);
			final ResultList<Metadata> metadata = searchService.find(params, Metadata.class);

//			Metric.addMetric(operationsService, context.getUserId(), idOrUriLabel, concept.getPublisher(),
//					concept.getTerminology(), concept.getVersion(), "CONCEPT-BY-CODE", "diagram", null);

			// call diagram application to generate image
			final HttpClient httpClient = HttpClientBuilder.create().build();
			final String diagramUrl = PropertyUtility.getProperties().getProperty("api.url.termhub-diagram-service");
			final HttpPost request = new HttpPost(diagramUrl + "/generate/diagram");
			final String diagramModel = TerminologyUtility.toDiagramModel(concept, metadata.getItems());
			final StringEntity payload = new StringEntity(diagramModel);
			request.addHeader("content-type", MediaType.APPLICATION_JSON);
			request.addHeader("accept", "image/png");
			request.setEntity(payload);
			logger.info("    get diagram = " + diagramModel);
			final HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() >= 400) {
				throw new RestException(false, 417, "Diagram service error", response.getStatusLine().toString());
			}
			// Return the diagram
			return new ResponseEntity<>(new InputStreamResource(response.getEntity().getContent()), new HttpHeaders(),
					HttpStatus.OK);

		} catch (final Exception e) {
			handleException(e, "trying to get concept diagram = " + terminology + ", " + code);
			return null;
		}

	}

	/**
	 * Lookup project terminologies.
	 *
	 * @return the list
	 * @throws Exception the exception
	 */
	public Map<String, Terminology> lookupTerminologyMap() throws Exception {

		final String query = "*:*"; // "latest:true";
		Map<String, Terminology> indexMap = terminologyCache.get(query);

		if (indexMap == null) {
			// then do a find on the query
			final SearchParameters params = new SearchParameters(query, null, 100000, null, null);
			final ResultList<Terminology> results = searchService.find(params, Terminology.class);
			final List<Terminology> terminologies = results.getItems();

			// then sort the results (just use the natural terminology sort order)
			indexMap = new HashMap<>();
			for (final Terminology t : terminologies) {
				indexMap.put(t.getAbbreviation() + t.getPublisher() + t.getVersion(), t);
			}
			terminologyCache.put(query, indexMap);
		}
		return indexMap;
	}

	/**
	 * Lookup project mapset map.
	 *
	 * @param context the context
	 * @return the map
	 * @throws Exception the exception
	 */
	public Map<String, Mapset> lookupMapsetMap(final AuthContext context) throws Exception {

		final String query = "latest:true";
		Map<String, Mapset> indexMap = mapsetCache.get(query);

		if (indexMap == null) {
			// then do a find on the query
			final SearchParameters params = new SearchParameters(query, null, 100000, null, null);
			final ResultList<Mapset> results = searchService.find(params, Mapset.class);
			final List<Mapset> mapsets = results.getItems();

			// then sort the results (just use the natural mapset sort order)
			indexMap = new HashMap<>();
			for (final Mapset mapset : mapsets) {
				indexMap.put(mapset.getAbbreviation() + mapset.getPublisher() + mapset.getVersion(), mapset);
			}
			mapsetCache.put(query, indexMap);
		}
		return indexMap;
	}

	/**
	 * Lookup project terminologies.
	 *
	 * @param context     the context
	 * @param terminology the terminology
	 * @return the map
	 * @throws Exception the exception
	 */
	public Terminology lookupTerminology(final AuthContext context, final String terminology) throws Exception {

		final List<Terminology> terminologies = lookupTerminologyMap().values().stream()
				.filter(t -> t.getId().equals(terminology) || t.getAbbreviation().equals(terminology))
				.collect(Collectors.toList());
		if (terminologies.size() == 0) {
			throw new RestException(false, 417, "Expectation failed",
					"Unable to find terminology in project = " + terminology);
		}

		if (terminologies.size() > 1) {
			throw new RestException(false, 417, "Expectation failed",
					"Too many terminologies found in project = " + terminology);
		}
		return terminologies.get(0);

	}

	/**
	 * Lookup project terminologies.
	 *
	 * @param context     the context
	 * @param terminology the terminology
	 * @return the list
	 * @throws Exception the exception
	 */
	public List<Terminology> lookupTerminologies(final AuthContext context, final String terminology) throws Exception {
		return lookupTerminologies(context, terminology, false);
	}

	/**
	 * Lookup terminologies.
	 *
	 * @param context     the context
	 * @param terminology the terminology
	 * @param allowBlank  the allow blank
	 * @return the list
	 * @throws Exception the exception
	 */
	public List<Terminology> lookupTerminologies(final AuthContext context, final String terminology,
			final boolean allowBlank) throws Exception {

		if (!allowBlank && StringUtility.isEmpty(terminology)) {
			throw new RestException(false, 417, "Expectation failed", "Terminology parameter should not be blank");
		}

		final Map<String, Terminology> map = lookupTerminologyMap();
		// Find terminologies matching the project
		final Set<String> terminologies = new HashSet<>();
		if (terminology != null) {
			for (final String value : terminology.split(",")) {
				terminologies.add(value);
			}
		}
		final List<Terminology> list = map
				.values().stream().filter(t -> (allowBlank && terminologies.isEmpty())
						|| terminologies.contains(t.getId()) || terminologies.contains(t.getAbbreviation()))
				.collect(Collectors.toList());
		if (list.size() == 0) {
			throw new RestException(false, 417, "Expectation failed",
					"Unable to find any matching terminology in project = " + terminology);
		}
		return list;
	}

	/**
	 * Lookup project mapsets.
	 *
	 * @param context the context
	 * @param mapset  the mapset
	 * @return the list
	 * @throws Exception the exception
	 */
	public List<Mapset> lookupMapsets(final AuthContext context, final String mapset) throws Exception {
		return lookupMapsets(context, mapset, false);
	}

	/**
	 * Lookup project mapsets.
	 *
	 * @param context    the context
	 * @param mapset     the mapset
	 * @param allowBlank the allow blank
	 * @return the list
	 * @throws Exception the exception
	 */
	public List<Mapset> lookupMapsets(final AuthContext context, final String mapset, final boolean allowBlank)
			throws Exception {

		if (!allowBlank && StringUtility.isEmpty(mapset)) {
			throw new RestException(false, 417, "Expectation failed", "Mapset parameter should not be blank");
		}

		final Map<String, Mapset> map = lookupMapsetMap(context);
		// Find mapsets matching the project
		final Set<String> mapsets = new HashSet<>();
		if (mapset != null) {
			for (final String value : mapset.split(",")) {
				mapsets.add(value);
			}
		}
		final List<Mapset> list = map.values().stream().filter(m -> (allowBlank && mapsets.isEmpty())
				|| mapsets.contains(m.getId()) || mapsets.contains(m.getAbbreviation())).collect(Collectors.toList());
		if (!allowBlank && list.size() == 0) {
			throw new RestException(false, 417, "Expectation failed",
					"Unable to find any matching mapset in project = " + mapset);
		}
		return list;
	}

	/**
	 * Gets the terminology.
	 *
	 * @param map     the map
	 * @param concept the concept
	 * @return the terminology
	 * @throws Exception the exception
	 */
	public Terminology getTerminology(final Map<String, Terminology> map, final Concept concept) throws Exception {
		// Choose indexName for the concept
		final Terminology terminology = map
				.get(concept.getTerminology() + concept.getPublisher() + concept.getVersion());
		if (terminology == null) {
			logger.error("    indexMap = " + map);
			logger.error("    key = " + concept.getTerminology() + concept.getPublisher() + concept.getVersion());
			throw new RestException(false, 417, "Expectation failed",
					"Specified concept is not valid for this project = " + concept.getId());
		}
		return terminology;
	}

//	/**
//	 * Lookup project.
//	 *
//	 * @param context      the context
//	 * @param idOrUriLabel the project id or uri label
//	 * @return the project
//	 * @throws Exception the exception
//	 */
//	private Project lookupProject(final AuthContext context, final String idOrUriLabel) throws Exception {
//
//		final boolean projectToken = "PROJECT".equals(context.getRole());
//
//		// If the "context" is a PROJECT token, verify the project id matches
//		if (projectToken && !ClientFactory.get(ConfigClient.class).checkApiKey(context.getJwt())) {
//			throw new RestException(false, 403, "Forbidden", "Project api key is no longer valid");
//		}
//
//		// Lookup project in cache or get
//		Project project = projectCache.get(idOrUriLabel);
//		if (project == null) {
//			project = ClientFactory.get(ProjectClient.class).getOrFindProject(idOrUriLabel);
//			projectCache.put(idOrUriLabel, project);
//		}
//
//		// Check to see if project exists
//		if (project == null) {
//			throw new RestException(false, 404, "Not found", "Unable to find project for " + idOrUriLabel);
//		}
//
//		// Verify project token is for this project
//		if (projectToken && !project.getId().equals(context.getProjectId())) {
//			// NOTE: this means that the api key can't be used for public projects either
//			throw new RestException(false, 403, "Forbidden",
//					"Project API key can only access project = " + context.getProjectId());
//
//		}
//
//		// Handle expired - expired is only allowed to access public projects
//		if (context.isPlanExpired() && (project.getPublic() == null || !project.getPublic())) {
//			throw new RestException(false, 403, "Forbidden",
//					"User plan is expired, only public projects are allowed to be accesed");
//		}
//		return project;
//	}

}