/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.rest;

import static com.wci.termhub.util.IndexUtility.getAndQuery;

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

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wci.termhub.Application;
import com.wci.termhub.algo.DefaultProgressListener;
import com.wci.termhub.algo.TreePositionAlgorithm;
import com.wci.termhub.handler.QueryBuilder;
import com.wci.termhub.lucene.LuceneQueryBuilder;
import com.wci.termhub.model.Concept;
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
import com.wci.termhub.model.ResultListSubset;
import com.wci.termhub.model.ResultListSubsetMember;
import com.wci.termhub.model.ResultListTerm;
import com.wci.termhub.model.ResultListTerminology;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.SubsetMember;
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
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Terminology service rest implementation.
 */
@OpenAPIDefinition(info = @Info(title = "Open TermHub Terminology API", version = "1.0.0",
    description = "API documentation for the interacting with terminologies and concepts. "
        + "<p>For more information, see our github project "
        + "<a href=\"https://github.com/terminologyhub/open-termhub\">"
        + "https://github.com/terminologyhub/termhub-in-5-minutes</a></p>"
        + "<p>See <a href=\"/fhir/r4/swagger-ui/index.html\" target=\"_blank\">FHIR R4 API</a></p>"
        + "<p>See <a href=\"/fhir/r5/swagger-ui/index.html\" target=\"_blank\">FHIR R5 API</a></p>",
    contact = @Contact(name = "API Support", url = "https://www.terminologyhub.com",
        email = "info@terminologyhub.com")),
    tags = {
        @Tag(name = "terminology", description = "Terminology service endpoints"),
        @Tag(name = "mapset", description = "Mapset endpoints"),
        @Tag(name = "subset", description = "Subset endpoints"),
        @Tag(name = "metadata", description = "Terminology and metadata endpoints"),
        @Tag(name = "concept", description = "Concept endpoints"),
        @Tag(name = "concept by id",
            description = "Concept service endpoints with \"by id\" parameters"),
        @Tag(name = "concept by code",
            description = "Concept service endpoints with \"by code\" parameters"),
        @Tag(name = "term", description = "Term endpoints"),
        @Tag(name = "load", description = "Bulk loader endpoints"),
    }, servers = {
        @Server(description = "Current Instance", url = "/")
    })
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class TerminologyServiceRestImpl extends RootServiceRestImpl
    implements TerminologyServiceRest {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(TerminologyServiceRestImpl.class);

  /** The terminologies cache. */
  private static TimerCache<Map<String, Terminology>> terminologyCache =
      new TimerCache<>(1000, 10000);

  /** The terminologies cache. */
  private static TimerCache<Map<String, Mapset>> mapsetCache = new TimerCache<>(1000, 10000);

  /** The subset cache. */
  private static TimerCache<Map<String, Subset>> subsetCache = new TimerCache<>(1000, 10000);

  /** The request. */
  @SuppressWarnings("unused")
  @Autowired
  private HttpServletRequest request;

  /** The operations service. */
  @Autowired
  private EntityRepositoryService searchService;

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
   * Instantiates a {@link TerminologyServiceRestImpl} from the specified parameters. For testing.
   *
   * @param request the request
   * @throws Exception the exception
   */
  public TerminologyServiceRestImpl(final HttpServletRequest request) throws Exception {
    this.request = request;
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/terminology/health", method = RequestMethod.GET)
  @Hidden
  public ResponseEntity<HealthCheck> health(
    @RequestParam(name = "dependencies", required = false) final Boolean dependencies)
    throws Exception {

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

  /* see superclass */
  @Override
  @RequestMapping(value = "/terminology/admin", method = RequestMethod.POST)
  @Hidden
  public ResponseEntity<String> admin(@RequestParam("task") final String task,
    @RequestParam("adminKey") final String adminKey,
    @RequestParam(name = "background", required = false) final Boolean background,
    @org.springframework.web.bind.annotation.RequestBody(required = false) final String payload)
    throws Exception {

    try {

      if (adminKey != null
          && adminKey.equals(PropertyUtility.getProperties().getProperty("admin.key"))) {

        // Delete data
        if ("delete".equals(task)) {
          if (logger.isDebugEnabled()) {
            logger.debug("Delete all data - drop and recreate indexes");
          }

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

      }

      return new ResponseEntity<>("Successful", new HttpHeaders(), HttpStatus.OK);
    } catch (final Exception e) {
      handleException(e, "trying to perform admin = " + task);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/terminology/{id:[a-f0-9].+}", method = RequestMethod.GET)
  @Operation(summary = "Get terminology by id",
      description = "Gets terminology for the specified id", tags = {
          "terminology"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Terminology"),
      @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "id", description = "Terminology id, e.g. \"uuid\"", required = true)
  })
  public ResponseEntity<Terminology> getTerminology(@PathVariable("id") final String id)
    throws Exception {

    try {
      final Terminology terminology = searchService.get(id, Terminology.class);
      if (terminology == null) {
        return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.NOT_FOUND);
      }

      terminology.cleanForApi();
      return new ResponseEntity<>(terminology, new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to get terminology = " + id);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/terminology/{id:[a-f0-9].+}/metadata", method = RequestMethod.GET)
  @Operation(summary = "Get terminology metadata",
      description = "Gets terminology metadata for the specified terminology id", tags = {
          "metadata"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Terminology"),
      @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "id", description = "Terminology id, e.g. \"uuid\"", required = true)
  })
  public ResponseEntity<List<Metadata>> getTerminologyMetadata(@PathVariable("id") final String id)
    throws Exception {

    try {

      final Terminology terminology = searchService.get(id, Terminology.class);
      // not found - 404
      if (terminology == null) {
        return new ResponseEntity<>(Collections.emptyList(), new HttpHeaders(),
            HttpStatus.NOT_FOUND);
      }

      final SearchParameters searchParams = new SearchParameters();
      searchParams.setQuery(TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
          terminology.getPublisher(), terminology.getVersion()));
      searchParams.setLimit(100000);

      // Find and return the list
      return new ResponseEntity<>(searchService.find(searchParams, Metadata.class, null).getItems(),
          new HttpHeaders(), HttpStatus.OK);
    } catch (final Exception e) {
      handleException(e, "trying to get metadata for terminology = " + id);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/terminology", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @Hidden
  public ResponseEntity<Terminology> addTerminology(
    @org.springframework.web.bind.annotation.RequestBody final String terminologyStr)
    throws Exception {

    try {

      Terminology terminology = null;
      try {
        terminology = ModelUtility.fromJson(terminologyStr, Terminology.class);
      } catch (final Exception e) {
        throw new RestException(false, 417, "Expectation Failed ",
            "Unable to parse terminology = " + terminologyStr);
      }

      searchService.add(Terminology.class, terminology);

      // Compute the URI location
      final String location = PropertyUtility.getProperties().getProperty("api.url")
          + "/organization/" + terminology.getId();

      // Return the response
      final HttpHeaders headers = new HttpHeaders();
      headers.add("Location", location);
      return new ResponseEntity<>(terminology, headers, HttpStatus.CREATED);

    } catch (final Exception e) {
      handleException(e, "trying to add terminology");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/terminology/{id:[a-f0-9].+}", method = RequestMethod.PATCH,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @Hidden
  public ResponseEntity<Terminology> updateTerminology(@PathVariable("id") final String id,
    @org.springframework.web.bind.annotation.RequestBody final String terminologyStr)
    throws Exception {

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

  /* see superclass */
  @Override
  @RequestMapping(value = "/terminology/{id:[a-f0-9].+}", method = RequestMethod.DELETE)
  @Operation(summary = "Delete terminology",
      description = "Delete a terminology and concepts, relationships and "
          + "tree positions for the specified terminology id",
      tags = {
          "terminology"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "202", description = "Deleted Terminology"),
      @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "id", description = "Terminology id, e.g. \"uuid\"", required = true)
  })
  public ResponseEntity<Void> deleteTerminology(@PathVariable("id") final String id)
    throws Exception {

    try {
      // Verify the object exists
      final Terminology terminology = searchService.get(id, Terminology.class);
      if (terminology == null) {
        throw new RestException(false, 404, "Not Found", "Unable to find terminology = " + id);
      }

      TerminologyUtility.removeTerminology(searchService, id);
      return new ResponseEntity<>(HttpStatus.ACCEPTED);

    } catch (final Exception e) {
      handleException(e, "trying to delete terminology = " + id);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/mapset/{id:[a-f0-9].+}", method = RequestMethod.DELETE)
  @Operation(summary = "Delete mapset",
      description = "Delete a mapset and releated mappings for the specified mapset id", tags = {
          "mapset"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "202", description = "Deleted Mapset"),
      @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "id", description = "Mapset id, e.g. \"uuid\"", required = true)
  })
  public ResponseEntity<Void> deleteMapset(@PathVariable("id") final String id) throws Exception {

    try {
      // Verify the object exists
      final Mapset mapset = searchService.get(id, Mapset.class);
      if (mapset == null) {
        throw new RestException(false, 404, "Not Found", "Unable to find mapset = " + id);
      }

      TerminologyUtility.removeMapset(searchService, id);
      return new ResponseEntity<>(HttpStatus.ACCEPTED);

    } catch (final Exception e) {
      handleException(e, "trying to delete mapset = " + id);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/subset/{id:[a-f0-9].+}", method = RequestMethod.DELETE)
  @Operation(summary = "Delete subset/value set",
      description = "Delete a subset/value set and related subset members for the specified subset id",
      tags = {
          "subset"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "202", description = "Deleted Subset"),
      @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "id", description = "Subset id, e.g. \"uuid\"", required = true)
  })
  public ResponseEntity<Void> deleteSubset(@PathVariable("id") final String id) throws Exception {

    try {
      // Verify the object exists
      final Subset subset = searchService.get(id, Subset.class);
      if (subset == null) {
        throw new RestException(false, 404, "Not Found", "Unable to find subset = " + id);
      }

      TerminologyUtility.removeSubset(searchService, id);
      return new ResponseEntity<>(HttpStatus.ACCEPTED);

    } catch (final Exception e) {
      handleException(e, "trying to delete subset = " + id);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/terminology", method = RequestMethod.GET)
  @Operation(summary = "Find terminologies",
      description = "Finds terminologies matching specified criteria.", tags = {
          "terminology"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Result list of matching terminologies"),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "query", description = "Search text"
          + " (<a href=\"https://github.com/terminologyhub/termhub-in-5-minutes/blob/master/doc/SEARCH.md\">"
          + "See here for more info</a>)", required = false,
          schema = @Schema(implementation = String.class)),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          schema = @Schema(implementation = Integer.class), example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, schema = @Schema(implementation = Integer.class), example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false, schema = @Schema(implementation = String.class)),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false, schema = @Schema(implementation = Boolean.class))
  })
  public ResponseEntity<ResultListTerminology> findTerminologies(
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "ascending", required = false) final Boolean ascending) throws Exception {

    try {

      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);
      final SearchParameters searchParams =
          new SearchParameters(query, offset, maxLimit, sort, ascending);
      final ResultList<Terminology> list = searchService.find(searchParams, Terminology.class);
      list.setParameters(searchParams);
      list.getItems().forEach(t -> t.cleanForApi());

      return new ResponseEntity<>(new ResultListTerminology(list), new HttpHeaders(),
          HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find terminologies");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{conceptId:[a-f0-9].*}", method = RequestMethod.GET)
  @Operation(summary = "Get concept by id", description = "Gets concept for the specified id",
      tags = {
          "concept by id"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Concept matching specified id"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true),
      @Parameter(name = "include",
          description = "Indicator of how much data to return. "
              + "Comma-separated list of any of the following values: "
              + "minimal, summary, full, axioms, attributes, children, definitions, descendants, "
              + "highlights, inverseRelationships, mapsets, parents, relationships, semanticTypes, "
              + "subsets, terms, treePositions " + "<a href='https://github.com/TerminologyHub/"
              + "termhub-in-5-minutes/blob/main/doc/INCLUDE.md' "
              + "target='_blank'>See here for detailed information</a>.",
          required = false),
  })
  public ResponseEntity<Concept> getConcept(@PathVariable("conceptId") final String conceptId,
    @RequestParam(value = "include", required = false) final String include) throws Exception {

    try {
      final IncludeParam ip = new IncludeParam(include == null ? "summary" : include);

      final Map<String, Terminology> map = lookupTerminologyMap();

      // Get the concept
      final String query = "id:" + conceptId;

      // then do a find on the query
      // don't use 'get' because it doesn't work with include param fields
      final SearchParameters searchParams = new SearchParameters(query, null, 2, null, null);
      final ResultList<Concept> results = searchService.findFields(searchParams,
          new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

      if (results.getTotal() == 0) {
        return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
      }
      if (results.getTotal() > 1) {
        throw new RestException(false, 417, "Expecation failed",
            "Too many matching concepts for id = " + conceptId);
      }

      final Concept concept = results.getItems().get(0);
      concept.cleanForApi();

      // Get requested addendums to the concept
      final Terminology terminology = getTerminology(map, concept);

      // Handle include parameter
      IncludeParam.applyInclude(concept, ip);
      TerminologyUtility.populateConcept(concept, ip, terminology, searchService);

      // Return the object
      return new ResponseEntity<>(concept, new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to get concept = " + conceptId);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{terminology}/{code}", method = RequestMethod.GET)
  @Operation(summary = "Get concept by terminology and code",
      description = "Gets concept for the specified terminology and code.", tags = {
          "concept by code"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Concept matching specified terminology and code"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "terminology",
          description = "Terminology id or abbreviation." + " e.g. \"uuid1\" or \"ICD10CM\".",
          required = true),
      @Parameter(name = "code",
          description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"",
          required = true),
      @Parameter(name = "include",
          description = "Indicator of how much data to return. "
              + "Comma-separated list of any of the following values: "
              + "minimal, summary, full, axioms, attributes, children, definitions, descendants, "
              + "highlights, inverseRelationships, mapsets, parents, relationships, semanticTypes, "
              + "subsets, terms, treePositions " + "<a href='https://github.com/TerminologyHub/"
              + "termhub-in-5-minutes/blob/main/doc/INCLUDE.md' "
              + "target='_blank'>See here for detailed information</a>.",
          required = false),
  })
  public ResponseEntity<Concept> getConcept(@PathVariable("terminology") final String terminology,
    @PathVariable("code") final String code,
    @RequestParam(value = "include", required = false) final String include) throws Exception {

    try {

      final IncludeParam ip = new IncludeParam(include == null ? "summary" : include);
      final Terminology term = lookupTerminology(terminology);

      // find with code, term, pub, version
      final String query =
          StringUtility.composeQuery(
              "AND", TerminologyUtility.getTerminologyQuery(term.getAbbreviation(),
                  term.getPublisher(), term.getVersion()),
              "code:" + StringUtility.escapeQuery(code));

      // then do a find on the query
      final SearchParameters searchParams = new SearchParameters(query, null, 2, null, null);
      final ResultList<Concept> results = searchService.findFields(searchParams,
          new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

      if (results.getTotal() == 0) {
        return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
      }
      if (results.getTotal() > 1) {
        throw new RestException(false, 417, "Expecation failed",
            "Too many matching concepts for terminology/code = " + terminology + ", " + code);
      }

      final Concept concept = results.getItems().get(0);
      concept.cleanForApi();

      // Handle include parameter
      IncludeParam.applyInclude(concept, ip);
      TerminologyUtility.populateConcept(concept, ip, term, searchService);

      // Return the object
      return new ResponseEntity<>(concept, new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to get concept = " + code);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{terminology:[A-Z].*}", method = RequestMethod.GET)
  @Operation(summary = "Get concepts by terminology and list of codes",
      description = "Gets concepts for the specified terminology and list of codes.", tags = {
          "concept by code"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Concepts mathcing code"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "terminology",
          description = "Terminology id or abbreviation." + " e.g. \"uuid1\" or \"ICD10CM\".",
          required = true),
      @Parameter(name = "codes",
          description = "Comma-separated list of terminology codes, "
              + "e.g. \"1119,1149\" or \"64572001,22298006 \"",
          required = true),
      @Parameter(name = "include",
          description = "Indicator of how much data to return. "
              + "Comma-separated list of any of the following values: "
              + "minimal, summary, full, axioms, attributes, children, definitions, descendants, "
              + "highlights, inverseRelationships, mapsets, parents, relationships, semanticTypes, "
              + "subsets, terms, treePositions " + "<a href='https://github.com/TerminologyHub/"
              + "termhub-in-5-minutes/blob/main/doc/INCLUDE.md' "
              + "target='_blank'>See here for detailed information</a>.",
          required = false),
  })
  public ResponseEntity<List<Concept>> getConceptCodes(
    @PathVariable("terminology") final String terminology,
    @RequestParam(value = "codes", required = true) final String codes,
    @RequestParam(value = "include", required = false) final String include) throws Exception {

    try {

      final IncludeParam ip = new IncludeParam(include == null ? "summary" : include);
      final Terminology term = lookupTerminology(terminology);
      if (StringUtility.isEmpty(codes)) {
        throw new RestException(false, 417, "Expecation failed",
            "Codes parameter must be specified");
      }

      final String[] codeArray = codes.split(",");
      if (codeArray.length > 500) {
        throw new RestException(false, 417, "Expecation failed",
            "Too many codes specified in list (max is 500) = " + codeArray.length);
      }

      // find with code, term, pub, version
      final String query = StringUtility.composeQuery("AND",
          TerminologyUtility.getTerminologyQuery(term.getAbbreviation(), term.getPublisher(),
              term.getVersion()),
          "code:("
              + String.join(" OR ",
                  Arrays.asList(codeArray).stream().map(c -> StringUtility.escapeQuery(c)).toList())
              + ")");

      // then do a find on the query
      final SearchParameters searchParams = new SearchParameters(query, 0, 500, null, null);
      final ResultList<Concept> results = searchService.findFields(searchParams,
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

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept", method = RequestMethod.GET)
  @Operation(summary = "Find concepts across terminologies",
      description = "Finds concepts matching specified search criteria.", tags = {
          "concept"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Result list of matching concepts"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "terminology",
          description = "Comma-separated list of terminology ids or "
              + "abbreviations (or null for all terminologies)."
              + " e.g. \"uuid1,uuid2\", \"SNOMEDCT,RXNORM\", or \"ICD10CM\".",
          required = false),
      @Parameter(name = "query",
          description = "Search text" + " (<a href=\"https://github.com/terminologyhub/"
              + "termhub-in-5-minutes/blob/master/doc/SEARCH.md\">" + "See here for more info</a>)",
          required = false),
      @Parameter(name = "expression",
          description = "ECL-style expression" + " (<a href=\"https://github.com/terminologyhub/"
              + "termhub-in-5-minutes/blob/master/doc/EXPRESSION.md\">"
              + "See here for more info</a>)",
          required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          schema = @Schema(implementation = Integer.class), example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, schema = @Schema(implementation = Integer.class), example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false, schema = @Schema(implementation = String.class)),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false, schema = @Schema(implementation = Boolean.class)),
      @Parameter(name = "active",
          description = "<code>true</code> for active concepts only, "
              + "<code>false</code> for inactive concepts only," + " <code>null</code> for both",
          required = false, schema = @Schema(implementation = Boolean.class)),
      @Parameter(name = "leaf",
          description = "<code>true</code> for leaf nodes only, <code>false</code> for non-leaf nodes,"
              + " <code>null</code> for either",
          required = false, schema = @Schema(implementation = Boolean.class)),
      @Parameter(name = "include",
          description = "Indicator of how much data to return. "
              + "Comma-separated list of any of the following values: "
              + "minimal, summary, full, axioms, attributes, children, definitions, descendants, "
              + "highlights, inverseRelationships, mapsets, parents, relationships, semanticTypes, "
              + "subsets, terms, treePositions " + "<a href='https://github.com/TerminologyHub/"
              + "termhub-in-5-minutes/blob/main/doc/INCLUDE.md' "
              + "target='_blank'>See here for detailed information</a>.",
          required = false),
  })
  public ResponseEntity<ResultListConcept> findTerminologyConcepts(
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
    @RequestParam(name = "handler", required = false)
    @Parameter(hidden = true) final String handler) throws Exception {

    try {

      final IncludeParam ip = new IncludeParam(include == null ? "highlights" : include);
      final List<Terminology> tlist = lookupTerminologies(terminology, true);

      // Build a query from the handler and use it in findHelper
      final String query2 = QueryBuilder.findBuilder(builders, handler).buildQuery(query);

      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      // Handler applied, send null handler below
      final ResultList<Concept> list = findConceptsHelper(tlist, query2, expression, offset,
          maxLimit, sort, ascending, active, leaf, ip);

      return new ResponseEntity<>(new ResultListConcept(list), new HttpHeaders(), HttpStatus.OK);

    } catch (

    final Exception e) {
      handleException(e, "trying to find concepts");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/term", method = RequestMethod.GET)
  @Operation(summary = "Find terms across terminologies",
      description = "Finds terms matching specified search criteria.", tags = {
          "term"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Result list of matching terms"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "terminology",
          description = "Comma-separated list of terminology ids or "
              + "abbreviations (or null for all terminologies)."
              + " e.g. \"uuid1,uuid2\", \"SNOMEDCT,RXNORM\", or \"ICD10CM\".",
          required = false),
      @Parameter(name = "query",
          description = "Search text" + " (<a href=\"https://github.com/terminologyhub/"
              + "termhub-in-5-minutes/blob/master/doc/SEARCH.md\">" + "See here for more info</a>)",
          required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          schema = @Schema(implementation = Integer.class), example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, schema = @Schema(implementation = Integer.class), example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false, schema = @Schema(implementation = String.class)),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false, schema = @Schema(implementation = Boolean.class)),
      @Parameter(name = "active",
          description = "<code>true</code> for active terms only, <code>false</code> for inactive terms only,"
              + " <code>null</code> for both",
          required = false, schema = @Schema(implementation = Boolean.class)),
  })
  public ResponseEntity<ResultListTerm> findTerms(
    @RequestParam(value = "terminology", required = false) final String terminology,
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "ascending", required = false) final Boolean ascending,
    @RequestParam(name = "active", required = false) final Boolean active,
    @RequestParam(name = "handler", required = false)
    @Parameter(hidden = true) final String handler) throws Exception {

    try {

      // Build a query from the handler and use it in findHelper
      final String query2 = QueryBuilder.findBuilder(builders, handler).buildQuery(query);

      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      logger.info("query for term: {}", query2);

      final SearchParameters searchParams =
          new SearchParameters(query2, offset, maxLimit, sort, ascending);
      if (active != null && active) {
        searchParams.setActive(true);
      }
      final ResultList<Term> list = searchService.find(searchParams, Term.class);
      list.getItems().forEach(Term::cleanForApi);

      // Restore the original query for the response
      searchParams.setQuery(query);
      list.setParameters(searchParams);

      return new ResponseEntity<>(new ResultListTerm(list), new HttpHeaders(), HttpStatus.OK);

    } catch (

    final Exception e) {
      handleException(e, "trying to find terms");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/bulk", method = RequestMethod.POST)
  @Operation(summary = "Bulk find of concepts across specified terminologies",
      description = "Bulk find of concepts matching specified search criteria.", tags = {
          "concept"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "List of result lists of matching concepts for each query"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "terminology",
          description = "Comma-separated list of terminology ids or "
              + "abbreviations (or null for all terminologies)."
              + " e.g. \"uuid1,uuid2\", \"SNOMEDCT,RXNORM\", or \"ICD10CM\".",
          required = false),
      @Parameter(name = "expression",
          description = "ECL-style expression" + " (<a href=\"https://github.com/terminologyhub/"
              + "termhub-in-5-minutes/blob/master/doc/EXPRESSION.md\">"
              + "See here for more info</a>)",
          required = false),
      @Parameter(name = "limit", description = "Limit of results to return, max is 10",
          required = false, schema = @Schema(implementation = Integer.class), example = "1"),
      @Parameter(name = "active",
          description = "<code>true</code> for active concepts only, "
              + "<code>false</code> for inactive concepts only," + " <code>null</code> for both",
          required = false, schema = @Schema(implementation = Boolean.class)),
      @Parameter(name = "leaf",
          description = "<code>true</code> for leaf nodes only, "
              + "<code>false</code> for non-leaf nodes," + " <code>null</code> for either",
          required = false, schema = @Schema(implementation = Boolean.class)),
      @Parameter(name = "include",
          description = "Indicator of how much data to return. "
              + "Comma-separated list of any of the following values: "
              + "minimal, summary, full, axioms, attributes, children, definitions, descendants, "
              + "highlights, inverseRelationships, mapsets, parents, relationships, semanticTypes, "
              + "subsets, terms, treePositions " + "<a href='https://github.com/TerminologyHub/"
              + "termhub-in-5-minutes/blob/main/doc/INCLUDE.md' "
              + "target='_blank'>See here for detailed information</a>.",
          required = false)
  })
  @RequestBody(description = "Newline-separated lines of text, one line for each query",
      required = true, content = @Content(schema = @Schema(implementation = String.class),
          mediaType = MediaType.TEXT_PLAIN_VALUE, examples = {
              @ExampleObject(value = "heart\nkidney\n12738006")
          }))
  public ResponseEntity<List<ResultListConcept>> lookup(
    @RequestParam(value = "terminology", required = false) final String terminology,
    @RequestParam(name = "expression", required = false) final String expression,
    @RequestParam(name = "limit", required = false, defaultValue = "1") final Integer limit,
    @RequestParam(name = "active", required = false) final Boolean active,
    @RequestParam(name = "leaf", required = false) final Boolean leaf,
    @RequestParam(value = "include", required = false) final String include,
    @RequestParam(name = "handler", required = false)
    @Parameter(hidden = true) final String handler,
    @org.springframework.web.bind.annotation.RequestBody(required = false)
    @Parameter(hidden = true) final String queries) throws Exception {

    try {

      final IncludeParam ip = new IncludeParam(include == null ? "semanticTypes" : include);
      final List<Terminology> tlist = lookupTerminologies(terminology, false);
      final String[] array = ModelUtility.nvl(queries, "").split("\n");

      final List<ResultListConcept> list = new ArrayList<>();
      for (final String query : array) {

        final String query2 = QueryBuilder.findBuilder(builders, handler).buildQuery(query);
        final int useLimit = limit == null ? 1 : (limit > 10 ? 10 : limit);
        final ResultList<Concept> result = findConceptsHelper(tlist, query2, expression, 0,
            useLimit, null, null, active, leaf, ip);
        result.getParameters().setQuery(query2);
        result.getParameters().setExpression(expression);

        if (query.isEmpty() || result.getItems().isEmpty()) {
          final Concept concept = new Concept();
          concept.setConfidence(0.0);
          concept.setCode("");
          concept.setTerminology("");
          concept.setPublisher("");
          concept.getSemanticTypes().add("");
          concept.setName("No match");
          result.setItems(new ArrayList<>(Arrays.asList(concept)));
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
   * Find helper.
   *
   * @param terminologies the terminologies
   * @param query the query
   * @param expression the expression
   * @param offset the offset
   * @param limit the limit
   * @param sort the sort
   * @param ascending the ascending
   * @param active the active
   * @param leaf the leaf
   * @param ip the ip
   * @return the result list
   * @throws Exception the exception
   */
  private ResultList<Concept> findConceptsHelper(final List<Terminology> terminologies,
    final String query, final String expression, final Integer offset, final Integer limit,
    final String sort, final Boolean ascending, final Boolean active, final Boolean leaf,
    final IncludeParam ip) throws Exception {

    // Check for a single terminology
    final Terminology single = terminologies.isEmpty() ? null : terminologies.get(0);
    // If expression is specified, there can be only one terminology
    if (!StringUtility.isEmpty(expression) && terminologies.size() > 1) {
      throw new RestException(false, 417, "Expecation failed",
          "Expression parameter can only be used in " + "conjunction with a single terminology");
    }

    // Ensure we lookup all terminologies
    final List<String> terminologyClauses = new ArrayList<>();
    for (final Terminology terminology : terminologies) {
      final String termQuery = TerminologyUtility.getTerminologyQuery(terminology.getAbbreviation(),
          terminology.getPublisher(), terminology.getVersion());
      terminologyClauses.add("(" + termQuery + ")");
    }
    final String terminologyQueryStr = StringUtility.composeQuery("OR", terminologyClauses);
    final Query terminologyQuery = LuceneQueryBuilder.parse(terminologyQueryStr, Concept.class);
    final Query keywordQuery = LuceneQueryBuilder.parse(query, Concept.class);
    final Query expressionQuery = TerminologyUtility.getExpressionQuery(expression);
    final Query leafQuery =
        BooleanUtils.isTrue(leaf) ? LuceneQueryBuilder.parse("leaf:true", Concept.class) : null;
    final Query activeQuery =
        active == null ? LuceneQueryBuilder.parse("(active:true^25 OR active:false)", Concept.class)
            : LuceneQueryBuilder.parse("active:" + active, Concept.class);
    final Query booleanQuery =
        getAndQuery(terminologyQuery, keywordQuery, expressionQuery, leafQuery, activeQuery);

    final SearchParameters searchParams =
        new SearchParameters(booleanQuery, offset, limit, sort, ascending);
    if (active != null && active) {
      searchParams.setActive(true);
    }
    if (leaf != null && leaf) {
      searchParams.setLeaf(true);
    }

    if (logger.isDebugEnabled()) {
      logger.debug("  query = " + query);
      logger.debug("    expression = " + expression);
      logger.debug("    params = " + searchParams);
    }

    final ResultList<Concept> list = searchService.findFields(searchParams,
        new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

    for (final Concept concept : list.getItems()) {
      concept.cleanForApi();
      // Handle include parameter
      IncludeParam.applyInclude(concept, ip);
      TerminologyUtility.populateConcept(concept, ip, single, searchService);
    }

    // Restore the original query for the response
    searchParams.setQuery(query);
    searchParams.setExpression(expression);
    list.setParameters(searchParams);
    return list;

  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/metadata", method = RequestMethod.GET)
  @Operation(summary = "Find terminology metadata",
      description = "Find metadata for the terminologies", tags = {
          "metadata"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Matadata for the specified query parameters"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "query",
          description = "Search text" + " (<a href=\"https://github.com/terminologyhub/"
              + "termhub-in-5-minutes/blob/master/doc/SEARCH.md\">" + "See here for more info</a>)",
          required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false),
  })
  public ResponseEntity<ResultListMetadata> findMetadata(
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "ascending", required = false) final Boolean ascending) throws Exception {

    try {

      final Map<String, Terminology> map = lookupTerminologyMap();
      if (map.isEmpty()) {
        throw new RestException(false, 417, "Not Found", "Unexpected terminologies.");
      }

      // limit return objects to 20000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 20000);

      final SearchParameters searchParams =
          new SearchParameters(query, offset, maxLimit, sort, ascending);
      final ResultList<Metadata> list = searchService.find(searchParams, Metadata.class);

      // Minimize
      list.setParameters(searchParams);
      return new ResponseEntity<>(new ResultListMetadata(list), new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find metadata");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{conceptId}/relationships", method = RequestMethod.GET)
  @Operation(summary = "Find concept relationships",
      description = "Finds concept relationships for the specified concept id.", tags = {
          "concept by id"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Result list of matching concept relationships"),
      @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true),
      @Parameter(name = "query",
          description = "Search text" + " (<a href=\"https://github.com/terminologyhub/"
              + "termhub-in-5-minutes/blob/master/doc/SEARCH.md\">" + "See here for more info</a>)",
          required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false)
  })
  public ResponseEntity<ResultListConceptRelationship> findConceptRelationships(
    @PathVariable("conceptId") final String conceptId,
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "ascending", required = false) final Boolean ascending,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "handler", required = false)
    @Parameter(hidden = true) final String handler) throws Exception {

    try {

      // look up concept first and get code
      final Concept concept = searchService.get(conceptId, Concept.class);
      if (concept == null) {
        throw new RestException(false, 404, "Not Found", "Unable to find concept = " + conceptId);
      }

      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      final SearchParameters searchParams = new SearchParameters(
          StringUtility.composeQuery("AND",
              "from.code:" + StringUtility.escapeQuery(concept.getCode()),
              "terminology:" + StringUtility.escapeQuery(concept.getTerminology()),
              QueryBuilder.findBuilder(builders, handler).buildQuery(query)),
          offset, maxLimit, sort, ascending);

      final ResultList<ConceptRelationship> list =
          searchService.find(searchParams, ConceptRelationship.class);

      list.setParameters(searchParams);
      return new ResponseEntity<>(new ResultListConceptRelationship(list), new HttpHeaders(),
          HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find concept relationships");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{terminology}/{code}/relationships", method = RequestMethod.GET)
  @Operation(summary = "Find concept relationships by terminology and code",
      description = "Finds concept relationships for the specified terminology and code.", tags = {
          "concept by code"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Result list of matching concept relationships"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "terminology",
          description = "Terminology id or abbreviation." + " e.g. \"uuid1\" or \"ICD10CM\"."),
      @Parameter(name = "code",
          description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"",
          required = true),
      @Parameter(name = "query",
          description = "Search text" + " (<a href=\"https://github.com/terminologyhub/"
              + "termhub-in-5-minutes/blob/master/doc/SEARCH.md\">" + "See here for more info</a>)",
          required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false)
  })
  public ResponseEntity<ResultListConceptRelationship> findConceptRelationships(

    @PathVariable("terminology") final String terminology, @PathVariable("code") final String code,
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "ascending", required = false) final Boolean ascending,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "handler", required = false)
    @Parameter(hidden = true) final String handler) throws Exception {

    try {

      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      final SearchParameters searchParams = new SearchParameters(
          StringUtility.composeQuery("AND", "terminology:" + StringUtility.escapeQuery(terminology),
              "from.code:" + StringUtility.escapeQuery(code),
              QueryBuilder.findBuilder(builders, handler).buildQuery(query)),
          offset, maxLimit, sort, ascending);

      final ResultList<ConceptRelationship> list =
          searchService.find(searchParams, ConceptRelationship.class);

      list.setParameters(searchParams);
      return new ResponseEntity<>(new ResultListConceptRelationship(list), new HttpHeaders(),
          HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find concept relationships");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{conceptId}/inverseRelationships", method = RequestMethod.GET)
  @Operation(summary = "Find concept inverse relationships",
      description = "Finds concept inverse relationships for the specified concept id.", tags = {
          "concept by id"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Result list of matching concept inverse relationships"),
      @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true),
      @Parameter(name = "query",
          description = "Search text" + " (<a href=\"https://github.com/terminologyhub/"
              + "termhub-in-5-minutes/blob/master/doc/SEARCH.md\">" + "See here for more info</a>)",
          required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false)
  })
  public ResponseEntity<ResultListConceptRelationship> findConceptInverseRelationships(
    @PathVariable("conceptId") final String conceptId,
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "ascending", required = false) final Boolean ascending,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "handler", required = false)
    @Parameter(hidden = true) final String handler) throws Exception {

    try {

      // look up concept first and get code
      // then do a find on the query
      final Concept concept = searchService.get(conceptId, Concept.class);
      if (concept == null) {
        throw new RestException(false, 404, "Not Found", "Unable to find concept = " + conceptId);
      }

      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      final SearchParameters searchParams = new SearchParameters(
          StringUtility.composeQuery("AND",
              "to.code:" + StringUtility.escapeQuery(concept.getCode()),
              "terminology:" + StringUtility.escapeQuery(concept.getTerminology()),
              QueryBuilder.findBuilder(builders, handler).buildQuery(query)),
          offset, maxLimit, sort, ascending);

      final ResultList<ConceptRelationship> list =
          searchService.find(searchParams, ConceptRelationship.class);

      list.setParameters(searchParams);
      return new ResponseEntity<>(new ResultListConceptRelationship(list), new HttpHeaders(),
          HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find concept inverse relationships");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{terminology}/{code}/inverseRelationships",
      method = RequestMethod.GET)
  @Operation(summary = "Find concept inverse relationships by terminology and code",
      description = "Finds concept inverse relationships for the specified terminology and code.",
      tags = {
          "concept by code"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Result list of matching concept inverse relationships"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "terminology",
          description = "Terminology id or abbreviation." + " e.g. \"uuid1\" or \"ICD10CM\"."),
      @Parameter(name = "code",
          description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"",
          required = true),
      @Parameter(name = "query",
          description = "Search text" + " (<a href=\"https://github.com/terminologyhub/"
              + "termhub-in-5-minutes/blob/master/doc/SEARCH.md\">" + "See here for more info</a>)",
          required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false)
  })
  public ResponseEntity<ResultListConceptRelationship> findConceptInverseRelationships(

    @PathVariable("terminology") final String terminology, @PathVariable("code") final String code,
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "ascending", required = false) final Boolean ascending,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "handler", required = false)
    @Parameter(hidden = true) final String handler) throws Exception {

    try {

      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      final SearchParameters searchParams = new SearchParameters(
          StringUtility.composeQuery("AND", "terminology:" + StringUtility.escapeQuery(terminology),
              "to.code:" + StringUtility.escapeQuery(code),
              QueryBuilder.findBuilder(builders, handler).buildQuery(query)),
          offset, maxLimit, sort, ascending);

      final ResultList<ConceptRelationship> list =
          searchService.find(searchParams, ConceptRelationship.class);

      list.setParameters(searchParams);
      return new ResponseEntity<>(new ResultListConceptRelationship(list), new HttpHeaders(),
          HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find concept inverse relationships");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{conceptId}/trees", method = RequestMethod.GET)
  @Operation(summary = "Find concept tree positions",
      description = "Finds concept tree positions for the specified concept id.", tags = {
          "concept by id"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Result list of matching concept tree positions"),
      @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true),
      @Parameter(name = "query",
          description = "Search text" + " (<a href=\"https://github.com/terminologyhub/"
              + "termhub-in-5-minutes/blob/master/doc/SEARCH.md\">" + "See here for more info</a>)",
          required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false)
  })
  public ResponseEntity<ResultListConceptTreePosition> findTreePositions(
    @PathVariable("conceptId") final String conceptId,
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "ascending", required = false) final Boolean ascending,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "handler", required = false)
    @Parameter(hidden = true) final String handler) throws Exception {

    try {

      final Map<String, Terminology> map = lookupTerminologyMap();
      // look up concept first and get code,
      // then do a find on the query
      final Concept concept = searchService.get(conceptId, Concept.class);
      if (concept == null) {
        throw new RestException(false, 404, "Not Found", "Unable to find concept = " + conceptId);
      }
      // Choose indexName for the concept
      final Terminology terminology = getTerminology(map, concept);
      terminologyHasTreePositions(terminology);

      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      final SearchParameters searchParams = new SearchParameters(
          StringUtility.composeQuery("AND",
              "concept.code:" + StringUtility.escapeQuery(concept.getCode()),
              "terminology:" + StringUtility.escapeQuery(terminology.getAbbreviation()),
              QueryBuilder.findBuilder(builders, handler).buildQuery(query)),
          offset, maxLimit, sort, ascending);

      final ResultList<ConceptTreePosition> list =
          searchService.find(searchParams, ConceptTreePosition.class);

      final ResultList<ConceptTreePosition> treeList = new ResultList<>();
      for (final ConceptTreePosition treepos : list.getItems()) {

        final ConceptTreePosition tree = TerminologyUtility.computeTree(searchService, treepos);
        treeList.getItems().add(tree);
      }
      treeList.setParameters(searchParams);
      treeList.setTotal(list.getTotal());

      return new ResponseEntity<>(new ResultListConceptTreePosition(treeList), new HttpHeaders(),
          HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find concept tree positions");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{terminology}/{code}/trees", method = RequestMethod.GET)
  @Operation(summary = "Find concept tree positions by terminology and code",
      description = "Finds concept tree positions for the specified terminology and code.", tags = {
          "concept by code"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Result list of matching concept tree positions"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "terminology",
          description = "Terminology id or abbreviation." + " e.g. \"uuid1\" or \"ICD10CM\"."),
      @Parameter(name = "code",
          description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"",
          required = true),
      @Parameter(name = "query",
          description = "Search text" + " (<a href=\"https://github.com/terminologyhub/"
              + "termhub-in-5-minutes/blob/master/doc/SEARCH.md\">" + "See here for more info</a>)",
          required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false)
  })
  public ResponseEntity<ResultListConceptTreePosition> findTreePositions(
    @PathVariable("terminology") final String terminology, @PathVariable("code") final String code,
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "ascending", required = false) final Boolean ascending,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "handler", required = false)
    @Parameter(hidden = true) final String handler) throws Exception {

    try {
      // validate terminology - throw exception if not found
      final Terminology term = lookupTerminology(terminology);
      terminologyHasTreePositions(term);
      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      final SearchParameters searchParams = new SearchParameters(
          StringUtility.composeQuery("AND", "terminology:" + StringUtility.escapeQuery(terminology),
              "concept.code:" + StringUtility.escapeQuery(code),
              QueryBuilder.findBuilder(builders, handler).buildQuery(query)),
          offset, maxLimit, sort, ascending);

      final ResultList<ConceptTreePosition> list =
          searchService.find(searchParams, ConceptTreePosition.class);

      final ResultList<ConceptTreePosition> treeList = new ResultList<>();
      for (final ConceptTreePosition treepos : list.getItems()) {
        final ConceptTreePosition tree = TerminologyUtility.computeTree(searchService, treepos);
        treeList.getItems().add(tree);
      }
      treeList.setParameters(searchParams);
      treeList.setTotal(list.getTotal());

      return new ResponseEntity<>(new ResultListConceptTreePosition(treeList), new HttpHeaders(),
          HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find concept tree positions");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{conceptId}/trees/children", method = RequestMethod.GET)

  @Operation(summary = "Find concept tree position children",
      description = "Finds concept tree position children for the specified concept id.", tags = {
          "concept by id"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Result list of matching concept tree position children"),
      @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true),
      @Parameter(name = "query",
          description = "Search text" + " (<a href=\"https://github.com/terminologyhub/"
              + "termhub-in-5-minutes/blob/master/doc/SEARCH.md\">" + "See here for more info</a>)",
          required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false)

  })
  public ResponseEntity<ResultListConceptTreePosition> findTreePositionChildren(
    @PathVariable("conceptId") final String conceptId,
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "ascending", required = false) final Boolean ascending,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "handler", required = false)
    @Parameter(hidden = true) final String handler) throws Exception {

    try {

      // look up concept first and get code
      // then do a find on the query
      final Concept concept = searchService.get(conceptId, Concept.class);
      if (concept == null) {
        throw new RestException(false, 404, "Not Found", "Unable to find concept = " + conceptId);
      }
      // check if terminology has tree positions
      final Terminology terminology = lookupTerminology(concept.getTerminology());
      terminologyHasTreePositions(terminology);

      // Find this thing
      final SearchParameters searchParams = new SearchParameters(1, 0);
      searchParams.setQuery(StringUtility.composeQuery("AND",
          "terminology:" + StringUtility.escapeQuery(concept.getTerminology()),
          "concept.code:" + StringUtility.escapeQuery(concept.getCode()),
          QueryBuilder.findBuilder(builders, handler).buildQuery(query)));

      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      final ResultList<ConceptTreePosition> list =
          searchService.find(searchParams, ConceptTreePosition.class);
      if (list.getItems().isEmpty()) {
        list.setParameters(new SearchParameters((String) null, offset, maxLimit, sort, ascending));
        return new ResponseEntity<>(new ResultListConceptTreePosition(list), new HttpHeaders(),
            HttpStatus.OK);
      }
      // The first one is OK because all nodes will have the same children.
      final ConceptTreePosition tp = list.getItems().get(0);

      // Find its children
      final SearchParameters paramsChd =
          new SearchParameters((String) null, offset, maxLimit, sort, ascending);
      final String ancPath =
          (StringUtils.isEmpty(tp.getAncestorPath()) ? "" : tp.getAncestorPath() + "~")
              + concept.getCode();
      paramsChd.setQuery(StringUtility.composeQuery("AND",
          "terminology:" + StringUtility.escapeQuery(tp.getTerminology()),
          "ancestorPath:" + StringUtility.escapeQuery(ancPath),
          QueryBuilder.findBuilder(builders, handler).buildQuery(query)));

      final ResultList<ConceptTreePosition> listChd =
          searchService.find(paramsChd, ConceptTreePosition.class);

      listChd.setParameters(paramsChd);

      return new ResponseEntity<>(new ResultListConceptTreePosition(listChd), new HttpHeaders(),
          HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find tree position children.");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{terminology}/{code}/trees/children",
      method = RequestMethod.GET)
  @Operation(summary = "Find concept tree position children by terminology and code",
      description = "Finds concept tree position children for the specified terminology and code.",
      tags = {
          "concept by code"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Result list of matching concept tree position children"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "terminology",
          description = "Terminology id or abbreviation." + " e.g. \"uuid1\" or \"ICD10CM\"."),
      @Parameter(name = "code",
          description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"",
          required = true),
      @Parameter(name = "query",
          description = "Search text" + " (<a href=\"https://github.com/terminologyhub/"
              + "termhub-in-5-minutes/blob/master/doc/SEARCH.md\">" + "See here for more info</a>)",
          required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false)
  })
  public ResponseEntity<ResultListConceptTreePosition> findTreePositionChildren(

    @PathVariable("terminology") final String terminology, @PathVariable("code") final String code,
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "ascending", required = false) final Boolean ascending,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "handler", required = false)
    @Parameter(hidden = true) final String handler) throws Exception {

    try {

      // check if terminology has tree positions
      final Terminology term = lookupTerminology(terminology);
      terminologyHasTreePositions(term);

      // Find this thing
      final SearchParameters searchParams = new SearchParameters(1, 0);
      searchParams.setQuery(StringUtility.composeQuery("AND",
          QueryBuilder.findBuilder(builders, handler).buildQuery(query),
          "terminology:" + StringUtility.escapeQuery(terminology),
          "concept.code:" + StringUtility.escapeQuery(code)));

      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      final ResultList<ConceptTreePosition> list =
          searchService.find(searchParams, ConceptTreePosition.class);
      if (list.getItems().isEmpty()) {
        list.setParameters(new SearchParameters((String) null, offset, maxLimit, sort, ascending));
        return new ResponseEntity<>(new ResultListConceptTreePosition(list), new HttpHeaders(),
            HttpStatus.OK);
      }
      // The first one is OK because all nodes will have the same children.
      final ConceptTreePosition tp = list.getItems().get(0);

      // Find its children
      final SearchParameters paramsChd =
          new SearchParameters((String) null, offset, maxLimit, sort, ascending);
      final String ancPath =
          (StringUtils.isEmpty(tp.getAncestorPath()) ? "" : tp.getAncestorPath() + "~") + code;
      paramsChd.setQuery(StringUtility.composeQuery("AND",
          "terminology:" + StringUtility.escapeQuery(tp.getTerminology()),
          "ancestorPath:" + StringUtility.escapeQuery(ancPath),
          QueryBuilder.findBuilder(builders, handler).buildQuery(query)));
      final ResultList<ConceptTreePosition> listChd =
          searchService.find(paramsChd, ConceptTreePosition.class);
      listChd.setParameters(paramsChd);

      return new ResponseEntity<>(new ResultListConceptTreePosition(listChd), new HttpHeaders(),
          HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find tree position children.");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{terminology}/trees", method = RequestMethod.POST)
  @Hidden
  // @Operation(summary = "Compute concept tree positions by terminology, publisher and version",
  // description = "Computes concept tree positions for the specified terminology, publisher and
  // version. "
  // + "This is useful when tree positions were not computed during initial load "
  // + "(ENABLE_POST_LOAD_COMPUTATIONS=false).",
  // tags = {
  // "concept"
  // })
  // @ApiResponses({
  // @ApiResponse(responseCode = "200", description = "Tree positions computed successfully"),
  // @ApiResponse(responseCode = "404", description = "Terminology not found",
  // content = @Content()),
  // @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
  // @ApiResponse(responseCode = "500", description = "Internal server error",
  // content = @Content())
  // })
  // @Parameters({
  // @Parameter(name = "terminology",
  // description = "Terminology abbreviation. e.g. \"SNOMEDCT_US\".", required = true),
  // @Parameter(name = "publisher", description = "Terminology publisher. e.g. \"SANDBOX\".",
  // required = true),
  // @Parameter(name = "version", description = "Terminology version. e.g. \"20240301\".",
  // required = true)
  // })
  public ResponseEntity<String> computeTreePositions(
    @PathVariable("terminology") final String terminology,
    @RequestParam("publisher") final String publisher,
    @RequestParam("version") final String version) throws Exception {

    try {

      logger.info("Computing tree positions for terminology: {}, publisher: {}, version: {}",
          terminology, publisher, version);

      // Look up the terminology to make sure it exists
      final Terminology term =
          TerminologyUtility.getTerminology(searchService, terminology, publisher, version);
      if (term == null) {
        throw new Exception("Unable to find terminology = " + terminology + ", publisher = "
            + publisher + ", version = " + version);
      }

      // Create and configure the tree position algorithm
      final TreePositionAlgorithm treepos = new TreePositionAlgorithm(searchService);
      treepos.addProgressListener(new DefaultProgressListener());
      treepos.setTerminology(terminology);
      treepos.setPublisher(publisher);
      treepos.setVersion(version);
      treepos.checkPreconditions();
      treepos.compute();

      // Update the terminology attributes to indicate tree positions exist
      if (term.getAttributes() == null) {
        term.setAttributes(new HashMap<>());
      }
      term.getAttributes().put(Terminology.Attributes.treePositions.property(), "true");
      searchService.update(Terminology.class, term.getId(), term);

      // Force terminology cache to be reloaded
      terminologyCache.put(term.getId(), null);

      return ResponseEntity.ok("Tree positions computed successfully");

    } catch (final Exception e) {
      logger.error("Unexpected error computing tree positions", e);
      throw new Exception("Failed to compute tree positions: " + e.getMessage());
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/mapping", method = RequestMethod.GET)
  @Operation(summary = "Find mappings across mapsets",
      description = "Finds mapping matching specified search criteria.", tags = {
          "mapset"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Result list of matching mappings"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "mapset",
          description = "Comma-separated list of mapset ids or "
              + "abbreviations (or null for all mapsets)."
              + " e.g. \"uuid1,uuid2\", \"SNOMEDCT_US-ICD10CM,CVX-NDC\".",
          required = false),
      @Parameter(name = "query", description = "Search text", required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          schema = @Schema(implementation = Integer.class), example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, schema = @Schema(implementation = Integer.class), example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false, schema = @Schema(implementation = String.class)),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false, schema = @Schema(implementation = Boolean.class)),
      @Parameter(name = "active",
          description = "<code>true</code> for active mappings only, "
              + "<code>false</code> for inactive mappings only," + " <code>null</code> for both",
          required = false, schema = @Schema(implementation = Boolean.class)),
  })
  public ResponseEntity<ResultListMapping> findMappings(
    @RequestParam(name = "mapset", required = false) final String mapset,
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "ascending", required = false) final Boolean ascending,
    @RequestParam(name = "active", required = false) final Boolean active) throws Exception {

    try {

      // Allow mapset to be blank here
      final List<Mapset> mapsets = lookupMapsets(mapset, true);

      // Build a query from the handler and use it in findHelper
      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      // Handler applied, send null handler below
      final ResultList<Mapping> list =
          findMappingsHelper(mapsets, query, offset, maxLimit, sort, ascending, active);

      return new ResponseEntity<>(new ResultListMapping(list), new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find mappings");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/member", method = RequestMethod.GET)
  @Operation(summary = "Find members across all subsets",
      description = "Finds member matching specified search criteria.", tags = {
          "subset"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Result list of matching subpings"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "subset",
          description = "Comma-separated list of subset ids or "
              + "abbreviations (or null for all subsets)."
              + " e.g. \"uuid1,uuid2\", \"SNOMEDCT_US-EXTENSION\".",
          required = false),
      @Parameter(name = "query", description = "Search text", required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          schema = @Schema(implementation = Integer.class), example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, schema = @Schema(implementation = Integer.class), example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false, schema = @Schema(implementation = String.class)),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false, schema = @Schema(implementation = Boolean.class)),
      @Parameter(name = "active",
          description = "<code>true</code> for active members only, "
              + "<code>false</code> for inactive members only," + " <code>null</code> for both",
          required = false, schema = @Schema(implementation = Boolean.class)),
  })
  public ResponseEntity<ResultListSubsetMember> findMembers(
    @RequestParam(name = "mapset", required = false) final String mapset,
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "ascending", required = false) final Boolean ascending,
    @RequestParam(name = "active", required = false) final Boolean active) throws Exception {

    try {

      // Allow subset to be blank here
      final List<Subset> subsets = lookupSubsets(mapset, true);

      // Build a query from the handler and use it in findHelper
      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      // Handler applied, send null handler below
      final ResultList<SubsetMember> list =
          findMembersHelper(subsets, query, offset, maxLimit, sort, ascending, active);

      return new ResponseEntity<>(new ResultListSubsetMember(list), new HttpHeaders(),
          HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find subpings");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/mapset/{mapset}/mapping", method = RequestMethod.GET)
  @Operation(summary = "Find mappings for the specified mapset",
      description = "Finds mapping for the specified mapset and the specified search criteria.",
      tags = {
          "mapset"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Result list of matching mappings"),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "mapset",
          description = "Mapset id or abbreviation" + " e.g. \"uuid1\" or \"CVX-NDC\"."),
      @Parameter(name = "query", description = "Search text", required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          schema = @Schema(implementation = Integer.class), example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, schema = @Schema(implementation = Integer.class), example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false, schema = @Schema(implementation = String.class)),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false, schema = @Schema(implementation = Boolean.class)),
      @Parameter(name = "active",
          description = "<code>true</code> for active mappings only, "
              + "<code>false</code> for inactive mappings only," + " <code>null</code> for both",
          required = false, schema = @Schema(implementation = Boolean.class)),
      @Parameter(name = "leaf",
          description = "<code>true</code> for leaf nodes only, "
              + "<code>false</code> for non-leaf nodes," + " <code>null</code> for either",
          required = false, schema = @Schema(implementation = Boolean.class)),
  })
  public ResponseEntity<ResultListMapping> findMapsetMappings(
    @PathVariable("mapset") final String mapsetId,
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "ascending", required = false) final Boolean ascending,
    @RequestParam(name = "active", required = false) final Boolean active) throws Exception {

    try {

      // Allow mapset to be blank here
      final List<Mapset> mapsets = lookupMapsets(mapsetId, false);

      // Build a query from the handler and use it in findHelper
      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      // Handler applied, send null handler below
      final ResultList<Mapping> list =
          findMappingsHelper(mapsets, query, offset, maxLimit, sort, ascending, active);

      return new ResponseEntity<>(new ResultListMapping(list), new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find mapset mappings");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{conceptId:[a-f0-9].*}/mappings", method = RequestMethod.GET)
  @Operation(summary = "Get mappings from concept by id",
      description = "Gets mappings from the specified concept", tags = {
          "concept by id"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Mappings from the concept matching specified id in specified project",
          content = @Content(
              array = @ArraySchema(schema = @Schema(implementation = Mapping.class)))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true)
  })
  public ResponseEntity<List<Mapping>> getConceptMappings(
    @PathVariable("conceptId") final String conceptId) throws Exception {

    try {
      final IncludeParam ip = new IncludeParam("minimal");
      // final Map<String, Terminology> map = lookupTerminologyMap();

      // Get the concept
      final String query = "id:" + StringUtility.escapeQuery(conceptId);

      // then do a find on the query
      // don't use 'get' because it doesn't work with include param fields
      final SearchParameters searchParams = new SearchParameters(query, null, 2, null, null);
      final ResultList<Concept> results = searchService.findFields(searchParams,
          new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

      if (results.getTotal() == 0) {
        return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
      }
      if (results.getTotal() > 1) {
        throw new RestException(false, 417, "Expecation failed",
            "Too many matching concepts for id = " + conceptId);
      }

      final Concept concept = results.getItems().get(0);

      // Find mappings
      final List<Mapset> mapsets = lookupMapsets(null, true);
      final List<Mapping> mappings = findMappingsHelper(mapsets,
          StringUtility.composeQuery("AND",
              "from.code:" + StringUtility.escapeQuery(concept.getCode()),
              "from.terminology:" + StringUtility.escapeQuery(concept.getTerminology()),
              "from.publisher:" + StringUtility.escapeQuery(concept.getPublisher())),
          0, 10000, null, null, true).getItems();

      // Return the object
      return new ResponseEntity<>(mappings, new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to get concept mappings = " + conceptId);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{conceptId:[a-f0-9].*}/inverseMappings",
      method = RequestMethod.GET)
  @Operation(summary = "Get mappings to concept by id",
      description = "Gets mappings to the specified concept", tags = {
          "concept by id"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Mappings to the concept",
          content = @Content(
              array = @ArraySchema(schema = @Schema(implementation = Mapping.class)))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true)
  })
  public ResponseEntity<List<Mapping>> getConceptInverseMappings(
    @PathVariable("conceptId") final String conceptId) throws Exception {

    try {
      final IncludeParam ip = new IncludeParam("minimal");
      // Get the concept
      final String query = "id:" + StringUtility.escapeQuery(conceptId);

      // then do a find on the query
      // don't use 'get' because it doesn't work with include param fields
      final SearchParameters searchParams = new SearchParameters(query, null, 2, null, null);
      final ResultList<Concept> results = searchService.findFields(searchParams,
          new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

      if (results.getTotal() == 0) {
        return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
      }
      if (results.getTotal() > 1) {
        throw new RestException(false, 417, "Expecation failed",
            "Too many matching concepts for id = " + conceptId);
      }

      final Concept concept = results.getItems().get(0);

      // Find mappings
      final List<Mapset> mapsets = lookupMapsets(null, true);
      final List<Mapping> mappings = findMappingsHelper(mapsets,
          StringUtility.composeQuery("AND",
              "to.code:" + StringUtility.escapeQuery(concept.getCode()),
              "to.terminology:" + StringUtility.escapeQuery(concept.getTerminology()),
              "to.publisher:" + StringUtility.escapeQuery(concept.getPublisher())),
          0, 10000, null, null, true).getItems();

      // Return the object
      return new ResponseEntity<>(mappings, new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to get concept mappings = " + conceptId);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{conceptId:[a-f0-9].*}/members", method = RequestMethod.GET)
  @Operation(summary = "Get subset members from concept by id",
      description = "Gets subset members from the specified concept", tags = {
          "concept by id"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Subset members from the concept matching specified id in specified project",
          content = @Content(
              array = @ArraySchema(schema = @Schema(implementation = SubsetMember.class)))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "conceptId", description = "concept id, e.g. \"uuid\"", required = true)
  })
  public ResponseEntity<List<SubsetMember>> getConceptMembers(
    @PathVariable("conceptId") final String conceptId) throws Exception {

    try {
      final IncludeParam ip = new IncludeParam("minimal");

      // Get the concept
      final String query = "id:" + StringUtility.escapeQuery(conceptId);

      // then do a find on the query
      // don't use 'get' because it doesn't work with include param fields
      final SearchParameters searchParams = new SearchParameters(query, null, 2, null, null);
      final ResultList<Concept> results = searchService.findFields(searchParams,
          new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

      if (results.getTotal() == 0) {
        return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
      }
      if (results.getTotal() > 1) {
        throw new RestException(false, 417, "Expecation failed",
            "Too many matching concepts for id = " + conceptId);
      }

      final Concept concept = results.getItems().get(0);

      // Find members
      final List<Subset> subsets = lookupSubsets(null, true);
      final List<SubsetMember> members = findMembersHelper(subsets,
          StringUtility.composeQuery("AND", "code:" + StringUtility.escapeQuery(concept.getCode()),
              "terminology:" + StringUtility.escapeQuery(concept.getTerminology()),
              "publisher:" + StringUtility.escapeQuery(concept.getPublisher()),
              "version:" + StringUtility.escapeQuery(concept.getVersion())),
          0, 10000, null, null, true).getItems();

      // Return the object
      return new ResponseEntity<>(members, new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to get concept members = " + conceptId);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{terminology}/{code}/mapping", method = RequestMethod.GET)
  @Operation(summary = "Get mappings from concept by terminology and code",
      description = "Gets mappings from the concept with the specified terminology and code.",
      tags = {
          "concept by code"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Mappings from the concept matching specified terminology and code",
          content = @Content(
              array = @ArraySchema(schema = @Schema(implementation = Mapping.class)))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "terminology",
          description = "Terminology id or abbreviation." + " e.g. \"uuid1\" or \"ICD10CM\".",
          required = true),
      @Parameter(name = "code",
          description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"",
          required = true)
  })
  public ResponseEntity<List<Mapping>> getConceptMappings(
    @PathVariable("terminology") final String terminology, @PathVariable("code") final String code)
    throws Exception {

    try {

      final IncludeParam ip = new IncludeParam("minimal");
      final Terminology term = lookupTerminology(terminology);

      // find with code, term, pub, version
      final String query =
          StringUtility.composeQuery(
              "AND", TerminologyUtility.getTerminologyQuery(term.getAbbreviation(),
                  term.getPublisher(), term.getVersion()),
              "code:" + StringUtility.escapeQuery(code));

      // then do a find on the query
      final SearchParameters searchParams = new SearchParameters(query, null, 2, null, null);
      final ResultList<Concept> results = searchService.findFields(searchParams,
          new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

      if (results.getTotal() == 0) {
        return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
      }
      if (results.getTotal() > 1) {
        throw new RestException(false, 417, "Expecation failed",
            "Too many matching concepts for terminology/code = " + terminology + ", " + code);
      }

      final Concept concept = results.getItems().get(0);

      // Find mappings
      final List<Mapset> mapsets = lookupMapsets(null, true);
      final List<Mapping> mappings = findMappingsHelper(mapsets,
          StringUtility.composeQuery("AND",
              "from.code:" + StringUtility.escapeQuery(concept.getCode()),
              "from.terminology:" + concept.getTerminology(),
              "from.publisher:" + concept.getPublisher()),
          0, 10000, null, null, true).getItems();

      // Return the object
      return new ResponseEntity<>(mappings, new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to get concept mappings for = " + code);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "concept/{terminology}/{code}/inverseMappings",
      method = RequestMethod.GET)
  @Operation(summary = "Get mappings to concept by terminology and code",
      description = "Gets mappings tothe concept with the specified terminology and code.", tags = {
          "concept by code"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Mappings to the concept matching specified terminology and code",
          content = @Content(
              array = @ArraySchema(schema = @Schema(implementation = Mapping.class)))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "terminology",
          description = "Terminology id or abbreviation." + " e.g. \"uuid1\" or \"ICD10CM\".",
          required = true),
      @Parameter(name = "code",
          description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"",
          required = true)
  })
  public ResponseEntity<List<Mapping>> getConceptInverseMappings(
    @PathVariable("terminology") final String terminology, @PathVariable("code") final String code)
    throws Exception {

    try {

      final IncludeParam ip = new IncludeParam("minimal");
      final Terminology term = lookupTerminology(terminology);

      // find with code, term, pub, version
      final String query =
          StringUtility.composeQuery(
              "AND", TerminologyUtility.getTerminologyQuery(term.getAbbreviation(),
                  term.getPublisher(), term.getVersion()),
              "code:" + StringUtility.escapeQuery(code));

      // then do a find on the query
      final SearchParameters searchParams = new SearchParameters(query, null, 2, null, null);
      final ResultList<Concept> results = searchService.findFields(searchParams,
          new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

      if (results.getTotal() == 0) {
        return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
      }
      if (results.getTotal() > 1) {
        throw new RestException(false, 417, "Expecation failed",
            "Too many matching concepts for terminology/code = " + terminology + ", " + code);
      }

      final Concept concept = results.getItems().get(0);

      // Find mappings
      final List<Mapset> mapsets = lookupMapsets(null, true);
      final List<Mapping> mappings = findMappingsHelper(mapsets,
          StringUtility.composeQuery("AND",
              "to.code:" + StringUtility.escapeQuery(concept.getCode()),
              "to.terminology:" + StringUtility.escapeQuery(concept.getTerminology()),
              "to.publisher:" + StringUtility.escapeQuery(concept.getPublisher())),
          0, 10000, null, null, true).getItems();

      // Return the object
      return new ResponseEntity<>(mappings, new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to get concept mappings for = " + code);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/concept/{terminology}/{code}/members", method = RequestMethod.GET)
  @Operation(summary = "Get members from concept by terminology and code",
      description = "Gets members from the concept with the specified terminology and code.",
      tags = {
          "concept by code"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          description = "Members from the concept matching specified terminology and code",
          content = @Content(
              array = @ArraySchema(schema = @Schema(implementation = SubsetMember.class)))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "terminology",
          description = "Terminology id or abbreviation." + " e.g. \"uuid1\" or \"ICD10CM\".",
          required = true),
      @Parameter(name = "code",
          description = "Terminology code, e.g. \"1119\", \"8867-4\", or \"64572001\"",
          required = true)
  })
  public ResponseEntity<List<SubsetMember>> getConceptMembers(
    @PathVariable("terminology") final String terminology, @PathVariable("code") final String code)
    throws Exception {

    try {

      final IncludeParam ip = new IncludeParam("minimal");
      final Terminology term = lookupTerminology(terminology);

      // find with code, term, pub, version
      final String query =
          StringUtility.composeQuery(
              "AND", TerminologyUtility.getTerminologyQuery(term.getAbbreviation(),
                  term.getPublisher(), term.getVersion()),
              "code:" + StringUtility.escapeQuery(code));

      // then do a find on the query
      final SearchParameters searchParams = new SearchParameters(query, null, 2, null, null);
      final ResultList<Concept> results = searchService.findFields(searchParams,
          new ArrayList<String>(Arrays.asList(ip.getIncludedFields())), Concept.class);

      if (results.getTotal() == 0) {
        return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
      }
      if (results.getTotal() > 1) {
        throw new RestException(false, 417, "Expecation failed",
            "Too many matching concepts for terminology/code = " + terminology + ", " + code);
      }

      final Concept concept = results.getItems().get(0);

      // Find subpings
      final List<Subset> subsets = lookupSubsets(null, true);
      final List<SubsetMember> members = findMembersHelper(subsets,
          StringUtility.composeQuery("AND",
              "from.code:" + StringUtility.escapeQuery(concept.getCode()),
              "from.terminology:" + StringUtility.escapeQuery(concept.getTerminology()),
              "from.publisher:" + StringUtility.escapeQuery(concept.getPublisher())),
          0, 10000, null, null, true).getItems();

      // Return the object
      return new ResponseEntity<>(members, new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to get concept members for = " + code);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/mapset/{id:[a-f0-9].+}", method = RequestMethod.GET)
  @Operation(summary = "Get mapset by id", description = "Gets mapset for the specified id",
      tags = {
          "mapset"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Mapset"),
      @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "id", description = "Mapset id, e.g. \"uuid\"", required = true)
  })
  public ResponseEntity<Mapset> getMapset(@PathVariable("id") final String id) throws Exception {

    try {
      final Mapset mapset = searchService.get(id, Mapset.class);
      if (mapset == null) {
        return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.NOT_FOUND);
      }
      mapset.cleanForApi();
      return new ResponseEntity<>(mapset, new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to get mapset = " + id);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/mapset", method = RequestMethod.GET)
  @Operation(summary = "Find mapsets", description = "Finds mapsets matching specified criteria.",
      tags = {
          "mapset"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Result list of matching mapsets"),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "query", description = "Search text", required = false,
          schema = @Schema(implementation = String.class)),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          schema = @Schema(implementation = Integer.class), example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, schema = @Schema(implementation = Integer.class), example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false, schema = @Schema(implementation = String.class)),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false, schema = @Schema(implementation = Boolean.class))
  })
  public ResponseEntity<ResultListMapset> findMapsets(
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "ascending", required = false) final Boolean ascending) throws Exception {

    try {

      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      // Limit to loaded mapsets
      final SearchParameters searchParams = new SearchParameters(
          StringUtility.isEmpty(query) ? "*:*" : query, offset, maxLimit, sort, ascending);
      final ResultList<Mapset> list = searchService.find(searchParams, Mapset.class);
      list.setParameters(searchParams);
      list.getItems().forEach(t -> t.cleanForApi());

      return new ResponseEntity<>(new ResultListMapset(list), new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find mapsets");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/subset", method = RequestMethod.GET)
  @Operation(summary = "Find subsets", description = "Finds subsets matching specified criteria.",
      tags = {
          "subset"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Result list of matching subsets"),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "query", description = "Search text", required = false,
          schema = @Schema(implementation = String.class)),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          schema = @Schema(implementation = Integer.class), example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, schema = @Schema(implementation = Integer.class), example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false, schema = @Schema(implementation = String.class)),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false, schema = @Schema(implementation = Boolean.class))
  })
  public ResponseEntity<ResultListSubset> findSubsets(
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "ascending", required = false) final Boolean ascending) throws Exception {

    try {

      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      // Limit to loaded subsets
      final SearchParameters searchParams = new SearchParameters(
          StringUtility.isEmpty(query) ? "*:*" : query, offset, maxLimit, sort, ascending);
      final ResultList<Subset> list = searchService.find(searchParams, Subset.class);
      list.setParameters(searchParams);
      list.getItems().forEach(t -> t.cleanForApi());

      return new ResponseEntity<>(new ResultListSubset(list), new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find subsets");
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/subset/{id:[a-f0-9].+}", method = RequestMethod.GET)
  @Operation(summary = "Get subset by id", description = "Gets subset for the specified id",
      tags = {
          "subset"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Subset"),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
      @ApiResponse(responseCode = "404", description = "Not Found", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "id", description = "Subset id, e.g. \"uuid\"", required = true)
  })
  public ResponseEntity<Subset> getSubset(@PathVariable("id") final String id) throws Exception {

    try {
      final Subset subset = searchService.get(id, Subset.class);
      if (subset == null) {
        return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.NOT_FOUND);
      }
      subset.cleanForApi();
      return new ResponseEntity<>(subset, new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to get subset = " + id);
      return null;
    }
  }

  /* see superclass */
  @Override
  @RequestMapping(value = "/subset/{subset}/member", method = RequestMethod.GET)
  @Operation(summary = "Find members for the specified subset",
      description = "Finds members for the specified subset and the specified search criteria.",
      tags = {
          "subset"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Result list of matching subset members"),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "subset",
          description = "Subset id or abbreviation"
              + " e.g. \"uuid1\" or \"SNOMEDCT_US-EXTENSION\".",
          required = true),
      @Parameter(name = "query", description = "Search text", required = false),
      @Parameter(name = "offset", description = "Start index for search results", required = false,
          schema = @Schema(implementation = Integer.class), example = "0"),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, schema = @Schema(implementation = Integer.class), example = "10"),
      @Parameter(name = "sort", description = "Comma-separated list of fields to sort on",
          required = false, schema = @Schema(implementation = String.class)),
      @Parameter(name = "ascending",
          description = "<code>true</code> for ascending, <code>false</code> for descending,"
              + " <code>null</code> for unspecified",
          required = false, schema = @Schema(implementation = Boolean.class)),
      @Parameter(name = "active",
          description = "<code>true</code> for active members only, <code>false</code> for inactive members only,"
              + " <code>null</code> for both",
          required = false, schema = @Schema(implementation = Boolean.class)),
  })
  public ResponseEntity<ResultListSubsetMember> findSubsetMembers(
    @PathVariable("subset") final String subsetId,
    @RequestParam(name = "query", required = false) final String query,
    @RequestParam(name = "offset", required = false, defaultValue = "0") final Integer offset,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit,
    @RequestParam(name = "sort", required = false) final String sort,
    @RequestParam(name = "ascending", required = false) final Boolean ascending,
    @RequestParam(name = "active", required = false) final Boolean active) throws Exception {

    try {

      // Allow subset to be blank here
      final List<Subset> subsets = lookupSubsets(subsetId, false);

      // Build a query from the handler and use it in findHelper
      // limit return objects to 1000 regardless of user request
      final Integer maxLimit = (limit == null) ? null : Math.min(limit, 1000);

      // Handler applied, send null handler below
      final ResultList<SubsetMember> list =
          findMembersHelper(subsets, query, offset, maxLimit, sort, ascending, active);

      return new ResponseEntity<>(new ResultListSubsetMember(list), new HttpHeaders(),
          HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find project subset members");
      return null;
    }
  }

  /**
   * Autocomplete for terms.
   *
   * @param terminology the terminology
   * @param query the query
   * @param limit the limit
   * @return the response entity
   * @throws Exception the exception
   */
  @RequestMapping(value = "/autocomplete", method = RequestMethod.GET)
  @Operation(summary = "Suggest autocompletions for text while searching",
      description = "Finds top ten strings matching input query.", tags = {
          "term"
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "List of top ten matching strings"),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
      @ApiResponse(responseCode = "417", description = "Expectation failed", content = @Content()),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content())
  })
  @Parameters({
      @Parameter(name = "terminology",
          description = "Comma-separated list of terminology ids or abbreviations (or null for all terminologies)."
              + " e.g. \"uuid1,uuid2\", \"SNOMEDCT,RXNORM\", or \"ICD10CM\".",
          required = false),
      @Parameter(name = "query", description = "Search text", required = true),
      @Parameter(name = "limit",
          description = "Limit of results to return (hard limit of 1000 regardless of value)",
          required = false, schema = @Schema(implementation = Integer.class), example = "10"),
  })
  public ResponseEntity<List<String>> autocomplete(
    @RequestParam(value = "terminology", required = false) final String terminology,
    @RequestParam(name = "query", required = true) final String query,
    @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit)
    throws Exception {

    try {
      // Get project terminologies (that support autocomplete)
      final List<Terminology> tlist = lookupTerminologies(terminology, false).stream()
          .filter(
              t -> t.getAttributes().containsKey(Terminology.Attributes.autocomplete.property()))
          .collect(Collectors.toList());

      if (tlist.isEmpty()) {
        return new ResponseEntity<>(new ArrayList<>(), new HttpHeaders(), HttpStatus.OK);
      }
      final BooleanQuery.Builder terminologyQueryBuilder = new BooleanQuery.Builder();
      for (final Terminology term : tlist) {
        terminologyQueryBuilder.add(
            new TermQuery(new org.apache.lucene.index.Term("terminology", term.getAbbreviation())),
            BooleanClause.Occur.SHOULD);
      }
      final BooleanQuery terminologyQuery = terminologyQueryBuilder.build();
      final String[] words = query.split("\\s+");
      final BooleanQuery.Builder ngramQueryBuilder = new BooleanQuery.Builder();
      for (final String word : words) {
        ngramQueryBuilder.add(
            new PrefixQuery(new org.apache.lucene.index.Term("name.ngram", word.toLowerCase())),
            BooleanClause.Occur.MUST);
      }
      final BooleanQuery ngramQuery = ngramQueryBuilder.build();

      // Prepare length boost
      final int doubleQueryLength = Math.max(query.length(), 5) * 2;
      final Query shortLength = IntPoint.newRangeQuery("length", 0, doubleQueryLength);
      final Query longLength =
          IntPoint.newRangeQuery("length", doubleQueryLength + 1, Integer.MAX_VALUE);
      final BoostQuery boostedShortLength = new BoostQuery(shortLength, 100f);

      final BooleanQuery lengthQuery =
          new BooleanQuery.Builder().add(boostedShortLength, BooleanClause.Occur.SHOULD)
              .add(longLength, BooleanClause.Occur.SHOULD).build();
      final BooleanQuery finalQuery = new BooleanQuery.Builder()
          .add(ngramQuery, BooleanClause.Occur.MUST).add(terminologyQuery, BooleanClause.Occur.MUST)
          .add(lengthQuery, BooleanClause.Occur.MUST).build();

      logger.info("Autocomplete search query: {}", finalQuery);
      final Integer maxLimit = (limit == null) ? 10 : Math.min(limit, 1000);
      final SearchParameters params = new SearchParameters(finalQuery, 0, maxLimit * 2, null, null);
      final ResultList<Term> list =
          searchService.findFields(params, ModelUtility.asList("name"), Term.class);

      // De-duplicate and limit results
      final Set<String> seen = new HashSet<>();
      final List<String> resultNames = list.getItems().stream()
          .filter(t -> !seen.contains(t.getName().toLowerCase()) && seen.size() < maxLimit)
          .peek(t -> seen.add(t.getName().toLowerCase())).map(Term::getName)
          .collect(Collectors.toList());

      return new ResponseEntity<>(resultNames, new HttpHeaders(), HttpStatus.OK);

    } catch (final Exception e) {
      handleException(e, "trying to find autocomplete terms");
      return null;
    }
  }

  /**
   * Lookup terminologies.
   *
   * @return the list
   * @throws Exception the exception
   */
  private Map<String, Terminology> lookupTerminologyMap() throws Exception {

    final String query = "*:*";
    Map<String, Terminology> indexMap = terminologyCache.get(query);

    if (indexMap == null) {
      // then do a find on the query
      final SearchParameters searchParams = new SearchParameters(query, null, 100000, null, null);
      final ResultList<Terminology> results = searchService.find(searchParams, Terminology.class);
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
   * Lookup terminologies.
   *
   * @param terminology the terminology
   * @return the map
   * @throws Exception the exception
   */
  private Terminology lookupTerminology(final String terminology) throws Exception {

    final List<Terminology> terminologies = lookupTerminologies(terminology, false).stream()
        .filter(t -> t.getId().equals(terminology) || t.getAbbreviation().equals(terminology))
        .toList();
    if (terminologies.isEmpty()) {
      throw new RestException(false, 417, "Expectation failed",
          "Unable to find terminology = " + terminology);
    }

    if (terminologies.size() > 1) {
      throw new RestException(false, 417, "Expectation failed",
          "Too many terminologies found = " + terminology);
    }
    return terminologies.get(0);

  }

  /**
   * Lookup terminologies.
   *
   * @param terminology the terminology
   * @param allowBlank the allow blank
   * @return the list
   * @throws Exception the exception
   */
  private List<Terminology> lookupTerminologies(final String terminology, final boolean allowBlank)
    throws Exception {

    if (!allowBlank && StringUtility.isEmpty(terminology)) {
      throw new RestException(false, 417, "Expectation failed",
          "Terminology parameter should not be blank");
    }

    final Map<String, Terminology> map = lookupTerminologyMap();
    final Set<String> terminologies = new HashSet<>();
    if (terminology != null) {
      for (final String value : terminology.split(",")) {
        terminologies.add(value);
      }
    }
    final List<Terminology> list = map
        .values().stream().filter(t -> (allowBlank && terminologies.isEmpty())
            || terminologies.contains(t.getId()) || terminologies.contains(t.getAbbreviation()))
        .toList();
    if (list.isEmpty()) {
      throw new RestException(false, 417, "Expectation failed",
          "Unable to find any matching terminology = " + terminology);
    }
    return list;
  }

  /**
   * Gets the terminology.
   *
   * @param map the map
   * @param concept the concept
   * @return the terminology
   * @throws Exception the exception
   */
  private Terminology getTerminology(final Map<String, Terminology> map, final Concept concept)
    throws Exception {
    // Choose indexName for the concept
    final Terminology terminology =
        map.get(concept.getTerminology() + concept.getPublisher() + concept.getVersion());
    if (terminology == null) {
      logger.error("    indexMap = {}", map);
      logger.error("    key = {}{}{}", concept.getTerminology(), concept.getPublisher(),
          concept.getVersion());
      throw new RestException(false, 417, "Expectation failed",
          "Specified concept is not valid for this terminology = " + concept.getId());
    }
    return terminology;
  }

  /**
   * Lookup mapsets.
   *
   * @param mapset the mapset name or id
   * @param allowBlank the allow blank
   * @return the list
   * @throws Exception the exception
   */
  private List<Mapset> lookupMapsets(final String mapset, final boolean allowBlank)
    throws Exception {

    if (!allowBlank && StringUtility.isEmpty(mapset)) {
      throw new RestException(false, 417, "Expectation failed",
          "Mapset parameter should not be blank");
    }

    final Map<String, Mapset> map = lookupMapsetMap();
    // Find mapsets
    final Set<String> mapsets = new HashSet<>();
    if (mapset != null) {
      for (final String value : mapset.split(",")) {
        mapsets.add(value);
      }
    }
    final List<Mapset> list = map.values().stream().filter(m -> (allowBlank && mapsets.isEmpty())
        || mapsets.contains(m.getId()) || mapsets.contains(m.getAbbreviation())).toList();
    if (!allowBlank && list.isEmpty()) {
      throw new RestException(false, 417, "Expectation failed",
          "Unable to find any matching mapset = " + mapset);
    }
    return list;
  }

  /**
   * Lookup mapset map.
   *
   * @return the map
   * @throws Exception the exception
   */
  private Map<String, Mapset> lookupMapsetMap() throws Exception {

    final String query = "*:*";
    Map<String, Mapset> indexMap = mapsetCache.get(query);

    if (indexMap == null) {
      // then do a find on the query
      final SearchParameters searchParams = new SearchParameters(query, null, 100000, null, null);
      final ResultList<Mapset> results = searchService.find(searchParams, Mapset.class);
      final List<Mapset> mapsets = results.getItems();

      // then sort the results (just use the natural mapset sort order)
      indexMap = new HashMap<>();
      for (final Mapset mapset : mapsets) {
        indexMap.put(mapset.getAbbreviation() + mapset.getPublisher() + mapset.getVersion(),
            mapset);
      }
      mapsetCache.put(query, indexMap);
    }
    return indexMap;
  }

  /**
   * Find mapset helper.
   *
   * @param mapsets the mapsets
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param sort the sort
   * @param ascending the ascending
   * @param active the active
   * @return the result list
   * @throws Exception the exception
   */
  private ResultList<Mapping> findMappingsHelper(final List<Mapset> mapsets, final String query,
    final Integer offset, final Integer limit, final String sort, final Boolean ascending,
    final Boolean active) throws Exception {

    // We are not using multiple indexes, so we instead have to add constraints
    final BooleanQuery.Builder mapsetQueryBuilder = new BooleanQuery.Builder();
    if (!ModelUtility.isEmpty(mapsets)) {
      for (final Mapset m : mapsets) {
        final BooleanQuery.Builder mapsetClauseBuilder = new BooleanQuery.Builder();
        mapsetClauseBuilder.add(
            new TermQuery(
                new org.apache.lucene.index.Term("mapset.abbreviation", m.getAbbreviation())),
            BooleanClause.Occur.MUST);
        mapsetClauseBuilder.add(
            new TermQuery(new org.apache.lucene.index.Term("mapset.publisher", m.getPublisher())),
            BooleanClause.Occur.MUST);
        mapsetClauseBuilder.add(
            new TermQuery(new org.apache.lucene.index.Term("mapset.version", m.getVersion())),
            BooleanClause.Occur.MUST);
        mapsetQueryBuilder.add(mapsetClauseBuilder.build(), BooleanClause.Occur.SHOULD);
      }
    }

    final Query mapsetQuery = mapsetQueryBuilder.build();
    final String query2 = QueryBuilder.findBuilder(builders, null).buildQuery(query);
    final Query keywordQuery =
        StringUtility.isEmpty(query) ? null : LuceneQueryBuilder.parse(query2, Mapping.class);
    final Query booleanQuery =
        ModelUtility.isEmpty(mapsets) ? keywordQuery : getAndQuery(mapsetQuery, keywordQuery);
    final SearchParameters searchParams =
        new SearchParameters(booleanQuery, offset, limit, sort, ascending);

    if (active != null && active) {
      searchParams.setActive(true);
    }

    final ResultList<Mapping> list = searchService.find(searchParams, Mapping.class);
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
    searchParams.setQuery(query2);
    list.setParameters(searchParams);
    return list;

  }

  /**
   * Find members helper.
   *
   * @param subsets the subsets
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param sort the sort
   * @param ascending the ascending
   * @param active the active
   * @return the result list
   * @throws Exception the exception
   */
  private ResultList<SubsetMember> findMembersHelper(final List<Subset> subsets, final String query,
    final Integer offset, final Integer limit, final String sort, final Boolean ascending,
    final Boolean active) throws Exception {

    // We are not using multiple indexes, so we instead have to add constraints
    final BooleanQuery.Builder subsetQueryBuilder = new BooleanQuery.Builder();
    if (!ModelUtility.isEmpty(subsets)) {
      for (final Subset s : subsets) {
        final BooleanQuery.Builder subsetClauseBuilder = new BooleanQuery.Builder();
        subsetClauseBuilder.add(
            new TermQuery(
                new org.apache.lucene.index.Term("subset.abbreviation", s.getAbbreviation())),
            BooleanClause.Occur.MUST);
        subsetClauseBuilder.add(
            new TermQuery(new org.apache.lucene.index.Term("subset.publisher", s.getPublisher())),
            BooleanClause.Occur.MUST);
        subsetClauseBuilder.add(
            new TermQuery(new org.apache.lucene.index.Term("subset.version", s.getVersion())),
            BooleanClause.Occur.MUST);
        subsetQueryBuilder.add(subsetClauseBuilder.build(), BooleanClause.Occur.SHOULD);
      }
    }
    final Query subsetQuery = subsetQueryBuilder.build();
    final String query2 = QueryBuilder.findBuilder(builders, null).buildQuery(query);
    final Query keywordQuery = LuceneQueryBuilder.parse(query2, SubsetMember.class);
    final Query booleanQuery = getAndQuery(subsetQuery, keywordQuery);
    final SearchParameters searchParams =
        new SearchParameters(booleanQuery, offset, limit, sort, ascending);

    if (active != null && active) {
      searchParams.setActive(true);
    }

    final ResultList<SubsetMember> list = searchService.find(searchParams, SubsetMember.class);

    // Restore the original query for the response
    searchParams.setQuery(query2);
    list.setParameters(searchParams);
    return list;

  }

  /**
   * Terminology has tree positions. Throw exception if not.
   *
   * @param terminology the terminology
   * @throws RestException the rest exception
   */
  private void terminologyHasTreePositions(final Terminology terminology) throws RestException {

    if (!terminology.getAttributes().containsKey(Terminology.Attributes.treePositions.property())
        || !"true".equalsIgnoreCase(
            terminology.getAttributes().get(Terminology.Attributes.treePositions.property()))) {
      throw new RestException(false, 417, "Expectation failed",
          "Tree positions were not computed for " + terminology.getAbbreviation());
    }
  }

  /**
   * Lookup subsets.
   *
   * @param subset the subset
   * @param allowBlank the allow blank
   * @return the subset
   * @throws Exception the exception
   */
  private List<Subset> lookupSubsets(final String subset, final boolean allowBlank)
    throws Exception {

    if (!allowBlank && StringUtility.isEmpty(subset)) {
      throw new RestException(false, 417, "Expectation failed",
          "Subset parameter should not be blank");
    }

    final Map<String, Subset> map = lookupSubsetMap();
    // Find subsets
    final Set<String> subsets = new HashSet<>();
    if (subset != null) {
      for (final String value : subset.split(",")) {
        subsets.add(value);
      }
    }
    final List<Subset> list = map.values().stream().filter(m -> (allowBlank && subsets.isEmpty())
        || subsets.contains(m.getId()) || subsets.contains(m.getAbbreviation())).toList();
    if (!allowBlank && list.isEmpty()) {
      throw new RestException(false, 417, "Expectation failed",
          "Unable to find any matching subset = " + subset);
    }
    return list;
  }

  /**
   * Lookup project subset map.
   *
   * @return the map
   * @throws Exception the exception
   */
  public Map<String, Subset> lookupSubsetMap() throws Exception {

    final String query = "*:*";
    Map<String, Subset> indexMap = subsetCache.get(query);

    if (indexMap == null) {
      // then do a find on the query
      final SearchParameters searchParams = new SearchParameters(query, null, 100000, null, null);
      final ResultList<Subset> results = searchService.find(searchParams, Subset.class);
      final List<Subset> subsets = results.getItems();

      // then sort the results (just use the natural subset sort order)
      indexMap = new HashMap<>();
      for (final Subset subset : subsets) {
        indexMap.put(subset.getAbbreviation() + subset.getPublisher() + subset.getVersion(),
            subset);
      }
      subsetCache.put(query, indexMap);
    }
    return indexMap;
  }

}
