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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.RecognitionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wci.termhub.ecl.EclConceptFieldNames;
import com.wci.termhub.ecl.EclToLuceneConverter;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.ConceptTreePosition;
import com.wci.termhub.model.IncludeParam;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Term;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;

/**
 * Utility for complex terminology operations (with caching).
 */
public final class TerminologyUtility {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(TerminologyUtility.class);

  /** The converter. */
  private static EclToLuceneConverter converter = new EclToLuceneConverter();

  /** The internal function pattern map. */
  private static final Map<EclToLuceneConverter.InternalFunction, Pattern> CONVERTER_MAP =
      getConverterMap();

  /**
   * Gets the converter map.
   *
   * @return the converter map
   */
  private static Map<EclToLuceneConverter.InternalFunction, Pattern> getConverterMap() {
    final Map<EclToLuceneConverter.InternalFunction, Pattern> map = new TreeMap<>();

    // compute the internal functions from the modified SQS lucene converter
    // NOTE: Kept out of Converter to minimize SQS code modification
    final EclToLuceneConverter.InternalFunction[] x =
        EclToLuceneConverter.InternalFunction.values();
    for (final EclToLuceneConverter.InternalFunction internalFunction : x) {
      map.put(internalFunction, Pattern.compile(".*(" + internalFunction + "\\(([^\\)]+)\\)).*"));
    }
    return map;
  }

  /**
   * Instantiates an empty {@link TerminologyUtility}.
   */
  private TerminologyUtility() {
    // n/a
  }

  /**
   * Gets the terminology.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param publisher the publisher
   * @param version the version
   * @return the terminology
   * @throws Exception the exception
   */
  public static Terminology getTerminology(final EntityRepositoryService searchService,
    final String terminology, final String publisher, final String version) throws Exception {

    final ResultList<Terminology> tlist = searchService
        .find(new SearchParameters("abbreviation:" + StringUtility.escapeQuery(terminology)
            + " AND publisher:" + StringUtility.escapeQuery(publisher) + " AND version:"
            + StringUtility.escapeQuery(version), 2, 0), Terminology.class);

    if (tlist.getItems().size() == 0) {
      return null;
    }
    if (tlist.getItems().size() > 1) {
      throw new Exception(
          "Too many terminology matches = " + terminology + ", " + publisher + ", " + version);
    }

    return tlist.getItems().get(0);
  }

  /**
   * Gets the mapset.
   *
   * @param searchService the search service
   * @param abbreviation the abbreviation
   * @param publisher the publisher
   * @param version the version
   * @return the mapset
   * @throws Exception the exception
   */
  public static Mapset getMapset(final EntityRepositoryService searchService,
    final String abbreviation, final String publisher, final String version) throws Exception {

    final ResultList<Mapset> tlist = searchService.find(new SearchParameters(
        "abbreviation:" + abbreviation + " AND publisher:" + publisher + " AND version:" + version,
        2, 0), Mapset.class);

    if (tlist.getItems().size() == 0) {
      return null;
    }
    if (tlist.getItems().size() > 1) {
      throw new Exception(
          "Too many terminology matches = " + abbreviation + ", " + publisher + ", " + version);
    }

    return tlist.getItems().get(0);
  }

  /**
   * Gets the concept.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param code the code
   * @return the concept
   * @throws Exception the exception
   */
  public static Concept getConcept(final EntityRepositoryService searchService,
    final Terminology terminology, final String code) throws Exception {
    return getConcept(searchService, terminology.getAbbreviation(), terminology.getPublisher(),
        terminology.getVersion(), code, terminology.getIndexName());
  }

  /**
   * Gets the concept.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param publisher the publisher
   * @param version the version
   * @param code the code
   * @param indexName the index name
   * @return the concept
   * @throws Exception the exception
   */
  public static Concept getConcept(final EntityRepositoryService searchService,
    final String terminology, final String publisher, final String version, final String code,
    final String indexName) throws Exception {
    final SearchParameters nameParams = new SearchParameters(2, 0);
    final String t = code.startsWith("V-") ? "SRC" : terminology;
    nameParams.setQuery("code:" + StringUtility.escapeQuery(code) + " AND terminology:" + t
        + " AND publisher:" + publisher + " AND version:" + version);

    final ResultList<Concept> list = searchService.find(nameParams, Concept.class);
    if (list.getItems().size() == 0) {
      return null;
    }
    return list.getItems().get(0);

  }

  /**
   * Gets the concept ref.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param code the code
   * @return the concept ref
   * @throws Exception the exception
   */
  public static Concept getConceptRef(final EntityRepositoryService searchService,
    final Terminology terminology, final String code) throws Exception {
    return getConceptRef(searchService, terminology.getAbbreviation(), terminology.getPublisher(),
        terminology.getVersion(), code, terminology.getIndexName());
  }

  /**
   * Returns the concept.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param publisher the publisher
   * @param version the version
   * @param code the code
   * @param indexName the index name
   * @return the concept
   * @throws Exception the exception
   */
  public static Concept getConceptRef(final EntityRepositoryService searchService,
    final String terminology, final String publisher, final String version, final String code,
    final String indexName) throws Exception {
    final SearchParameters nameParams = new SearchParameters(2, 0);
    final String t = code.startsWith("V-") ? "SRC" : terminology;
    nameParams.setQuery("code:" + StringUtility.escapeQuery(code) + " AND terminology:" + t
        + " AND publisher:" + publisher + " AND version:" + version);
    final ResultList<Concept> list = searchService.findFields(nameParams,
        ModelUtility.asList("id", "name", "code", "terminology", "version", "publisher", "leaf"),
        Concept.class);
    if (list.getItems().size() == 0) {
      return null;
    }
    return list.getItems().get(0);

  }

  /**
   * Gets the concept ecl.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param publisher the publisher
   * @param version the version
   * @param code the code
   * @param indexName the index name
   * @return the concept ecl
   * @throws Exception the exception
   */
  public static List<String> getConceptEcl(final EntityRepositoryService searchService,
    final String terminology, final String publisher, final String version, final String code,
    final String indexName) throws Exception {
    final SearchParameters nameParams = new SearchParameters(2, 0);
    final String t = code.startsWith("V-") ? "SRC" : terminology;
    nameParams.setQuery("code:" + StringUtility.escapeQuery(code) + " AND terminology:" + t
        + " AND publisher:" + publisher + " AND version:" + version);
    final ResultList<Concept> list =
        searchService.findFields(nameParams, ModelUtility.asList("ecl"), Concept.class);
    if (list.getItems().size() == 0) {
      return null;
    }
    return list.getItems().get(0).getEclClauses();

  }

  /**
   * Gets the concept name.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param publisher the publisher
   * @param version the version
   * @param code the code
   * @param indexName the index name
   * @return the concept name
   * @throws Exception the exception
   */
  public static String getConceptName(final EntityRepositoryService searchService,
    final String terminology, final String publisher, final String version, final String code,
    final String indexName) throws Exception {
    final SearchParameters nameParams = new SearchParameters(2, 0);
    final String t = code.startsWith("V-") ? "SRC" : terminology;
    nameParams.setQuery("code:" + StringUtility.escapeQuery(code) + " AND terminology:" + t
        + " AND publisher:" + publisher + " AND version:" + version);

    final ResultList<Concept> list =
        searchService.findFields(nameParams, ModelUtility.asList("name"), Concept.class);
    if (list.getItems().size() == 0) {
      return null;
    }
    return list.getItems().get(0).getName();

  }

  /**
   * Compute tree.
   *
   * @param searchService the search service
   * @param treePosition the tree position
   * @return the concept tree position
   * @throws Exception the exception
   */
  public static ConceptTreePosition computeTree(final EntityRepositoryService searchService,
    final ConceptTreePosition treePosition) throws Exception {

    ConceptTreePosition tree = null;
    final SearchParameters params = new SearchParameters(2, 0);

    // the current tree variables (ancestor path and local tree)
    // initially top-level
    String partAncPath = "";
    // initially the empty tree
    ConceptTreePosition parentTree = tree;

    final String ancestorPath =
        StringUtils.isEmpty(treePosition.getAncestorPath()) ? "" : treePosition.getAncestorPath();

    // Prepare lucene
    final String fullAncPath = ancestorPath + (StringUtils.isEmpty(ancestorPath) ? "" : "~")
        + treePosition.getConcept().getCode();
    // Iterate over ancestor path
    for (final String code : fullAncPath.split("~")) {
      final StringBuilder finalQuery = new StringBuilder();
      finalQuery.append("concept.code:" + QueryParserBase.escape(code)).append(" AND terminology:")
          .append(treePosition.getTerminology()).append(" AND publisher:")
          .append(treePosition.getPublisher()).append(" AND version:")
          .append(treePosition.getVersion()).append(" AND ancestorPath:")
          .append((partAncPath.isEmpty() ? "\"\"" : QueryParserBase.escape(partAncPath)));

      // No requirement for additional type to match in hierarchies
      // if (!StringUtility.isEmpty(treePosition.getAdditionalType())) {
      // finalQuery.append(" AND additionalType:" +
      // treePosition.getAdditionalType());
      // }

      params.setQuery(finalQuery.toString());
      final ResultList<ConceptTreePosition> list =
          searchService.find(params, ConceptTreePosition.class);
      if (list.getItems().size() == 0) {
        throw new Exception("Unable to find matching tree position for ancestor = " + finalQuery);
      }
      if (list.getItems().size() > 1) {
        throw new Exception("Too many matching tree positions for ancestor = " + finalQuery);
      }

      final ConceptTreePosition partTree = new ConceptTreePosition(list.getItems().get(0));

      // Look up the name - in theory this is no longer needed
      // final ConceptRef conceptRef = new ConceptRef();
      // final Concept concept =
      // getConceptRef(searchService, partTree.getTerminology(),
      // partTree.getPublisher(),
      // partTree.getVersion(), partTree.getConcept().getCode(), indexName);
      // conceptRef.setId(concept.getId());
      // conceptRef.setName(concept.getName());
      // conceptRef.setCode(partTree.getConcept().getCode());
      // conceptRef.setLeaf(concept.getLeaf());
      // partTree.setConcept(conceptRef);

      // Set the final return value to the top node
      if (tree == null) {
        tree = partTree;
      }

      if (parentTree != null) {
        parentTree.getChildren().add(partTree);
      }

      // set parent tree to the just constructed
      parentTree = partTree;

      partAncPath += (partAncPath.equals("") ? "" : "~") + code;

    }

    return tree;
  }

  /**
   * Resolve expression.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param expression the expression
   * @return the list
   * @throws Exception the exception
   */
  public static List<String> resolveExpression(final EntityRepositoryService searchService,
    final Terminology terminology, final String expression) throws Exception {
    final String query =
        getExpressionQuery(searchService, terminology.getAbbreviation(), terminology.getPublisher(),
            terminology.getVersion(), expression, terminology.getIndexName());
    return searchService.findAllWithFields(query, ModelUtility.asList("code"), Concept.class)
        .stream().map(c -> c.getCode()).collect(Collectors.toList());
  }

  /**
   * Resolve expression ids.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param expression the expression
   * @return the list
   * @throws Exception the exception
   */
  public static List<String> resolveExpressionIds(final EntityRepositoryService searchService,
    final Terminology terminology, final String expression) throws Exception {
    final String query =
        getExpressionQuery(searchService, terminology.getAbbreviation(), terminology.getPublisher(),
            terminology.getVersion(), expression, terminology.getIndexName());
    return searchService.findAllIds(query, Concept.class);
  }

  /**
   * Gets the ancestor codes.
   *
   * @param searchService the search service
   * @param concept the concept
   * @return the ancestor codes
   * @throws Exception the exception
   */
  public static Set<String> getAncestorCodes(final EntityRepositoryService searchService,
    final Concept concept) throws Exception {
    return concept.getEclClauses().stream()
        .filter(c -> c.matches(EclConceptFieldNames.ANCESTOR + "=[A-Z\\d].*"))
        .map(c -> c.substring(EclConceptFieldNames.ANCESTOR.length() + 1))
        .collect(Collectors.toSet());
  }

  /**
   * Gets the parent codes.
   *
   * @param searchService the search service
   * @param concept the concept
   * @return the parent codes
   * @throws Exception the exception
   */
  public static Set<String> getParentCodes(final EntityRepositoryService searchService,
    final Concept concept) throws Exception {
    return concept.getEclClauses().stream()
        .filter(c -> c.matches(EclConceptFieldNames.PARENT + "=[A-Z\\d].*"))
        .map(c -> c.substring(EclConceptFieldNames.PARENT.length() + 1))
        .collect(Collectors.toSet());
  }

  /**
   * Gets the parents.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param concept the concept
   * @return the parents
   * @throws Exception the exception
   */
  public static List<ConceptRef> getParents(final EntityRepositoryService searchService,
    final Terminology terminology, final Concept concept) throws Exception {

    final List<ConceptRelationship> list = searchService.findAll(StringUtility.composeQuery("AND",
        "from.code:" + StringUtility.escapeQuery(concept.getCode()),
        "hierarchical:true AND active:true"), ConceptRelationship.class);
    return list.stream().map(r -> r.getTo()).collect(Collectors.toList());
  }

  /**
   * Populate concept.
   *
   * @param concept the concept
   * @param includeParam the include param
   * @param terminology the terminology
   * @param searchService the search service
   * @throws Exception the exception
   */
  public static void populateConcept(final Concept concept, final IncludeParam includeParam,
    final Terminology terminology, final EntityRepositoryService searchService) throws Exception {

    if (concept.getChildren().isEmpty() && includeParam.isChildren()) {
      concept.setChildren(TerminologyUtility.getChildren(searchService, terminology, concept));
    }
    if (concept.getDescendants().isEmpty() && includeParam.isDescendants()) {
      concept
          .setDescendants(TerminologyUtility.getDescendants(searchService, terminology, concept));
    }
    if (concept.getParents().isEmpty() && includeParam.isParents()) {
      concept.setParents(TerminologyUtility.getParents(searchService, terminology, concept));
    }
    if (concept.getRelationships().isEmpty() && includeParam.isRelationships()) {
      concept.setRelationships(
          TerminologyUtility.getRelationships(searchService, terminology, concept));
    }
    if (concept.getInverseRelationships().isEmpty() && includeParam.isInverseRelationships()) {
      concept.setInverseRelationships(
          TerminologyUtility.getInverseRelationships(searchService, terminology, concept));
    }
    if (concept.getTreePositions().isEmpty() && includeParam.isTreePositions()) {
      concept.setTreePositions(
          TerminologyUtility.getTreePositions(searchService, terminology, concept));
    }
  }

  /**
   * Gets the children.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param concept the concept
   * @return the children
   * @throws Exception the exception
   */
  public static List<ConceptRef> getChildren(final EntityRepositoryService searchService,
    final Terminology terminology, final Concept concept) throws Exception {

    final List<ConceptRelationship> list = searchService.findAll(
        StringUtility.composeQuery("AND", "to.code:" + StringUtility.escapeQuery(concept.getCode()),
            "hierarchical:true", "active:true"),
        ConceptRelationship.class);
    return list.stream().map(r -> r.getFrom()).collect(Collectors.toList());
  }

  /**
   * Gets the relationships.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param concept the concept
   * @return the relationships
   * @throws Exception the exception
   */
  public static List<ConceptRelationship> getRelationships(
    final EntityRepositoryService searchService, final Terminology terminology,
    final Concept concept) throws Exception {

    final SearchParameters params = new SearchParameters(StringUtility.composeQuery("AND",
        "from.code:" + StringUtility.escapeQuery(concept.getCode())), 0, 1000, "", true);

    final ResultList<ConceptRelationship> list =
        searchService.find(params, ConceptRelationship.class);

    return list.getItems();
  }

  /**
   * Gets the inverse relationships.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param concept the concept
   * @return the inverse relationships
   * @throws Exception the exception
   */
  public static List<ConceptRelationship> getInverseRelationships(
    final EntityRepositoryService searchService, final Terminology terminology,
    final Concept concept) throws Exception {

    final SearchParameters params = new SearchParameters(StringUtility.composeQuery("AND",
        "to.code:" + StringUtility.escapeQuery(concept.getCode())), 0, 1000, "", true);

    final ResultList<ConceptRelationship> list =
        searchService.find(params, ConceptRelationship.class);

    return list.getItems();
  }

  /**
   * Gets the tree positions.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param concept the concept
   * @return the tree positions
   * @throws Exception the exception
   */
  public static List<ConceptTreePosition> getTreePositions(
    final EntityRepositoryService searchService, final Terminology terminology,
    final Concept concept) throws Exception {

    final SearchParameters params = new SearchParameters(StringUtility.composeQuery("AND",
        "terminology:" + StringUtility.escapeQuery(terminology.getAbbreviation()),
        "concept.code:" + StringUtility.escapeQuery(concept.getCode())), 0, 1000, "", true);

    final ResultList<ConceptTreePosition> list =
        searchService.find(params, ConceptTreePosition.class);

    return list.getItems();
  }

  /**
   * Gets the descendants.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param concept the concept
   * @return the descendants
   * @throws Exception the exception
   */
  public static List<ConceptRef> getDescendants(final EntityRepositoryService searchService,
    final Terminology terminology, final Concept concept) throws Exception {

    final List<ConceptRef> list = new ArrayList<>();
    final String expression =
        getExpressionQuery(searchService, terminology.getAbbreviation(), terminology.getPublisher(),
            terminology.getVersion(), "<" + concept.getCode(), terminology.getIndexName());

    final SearchParameters params = new SearchParameters(expression, 0, 1000, "code", null);
    String searchAfter = null;

    final int ct = 0;
    while (true) {
      final List<Concept> concepts = searchService
          .findFields(params, IncludeParam.getConceptBaseFields(), Concept.class).getItems();
      if (concepts.isEmpty()) {
        break;
      }
      list.addAll(concepts.stream().map(c -> new ConceptRef(c)).collect(Collectors.toList()));

      searchAfter = concepts.get(concepts.size() - 1).getId();
      logger.info("    count = " + ct);

    }
    return list;
  }

  /**
   * Gets the expression query.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param expression the expression
   * @return the expression query
   * @throws Exception the exception
   */
  public static String getExpressionQuery(final EntityRepositoryService searchService,
    final Terminology terminology, final String expression) throws Exception {
    return getExpressionQuery(searchService, terminology.getAbbreviation(),
        terminology.getPublisher(), terminology.getVersion(), expression,
        terminology.getIndexName());
  }

  /**
   * Gets the ecl query.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param publisher the publisher
   * @param version the version
   * @param expression the expression
   * @param indexName the index name
   * @return the ecl query
   * @throws Exception the exception
   */
  public static String getExpressionQuery(final EntityRepositoryService searchService,
    final String terminology, final String publisher, final String version, final String expression,
    final String indexName) throws Exception {

    if ((expression == null) || expression.isEmpty() || (terminology == null)) {
      return null;
    }

    // Handle an STY expression
    if (isStyExpression(expression)) {
      final String clause = getStyClause(expression);
      return clause;
    }
    final String clause =
        getExpressionClause(searchService, terminology, publisher, version, expression, indexName);
    return clause;
  }

  /**
   * Checks if is sty expression.
   *
   * @param expr the expr
   * @return true, if is sty expression
   */
  public static boolean isStyExpression(final String expr) {
    for (final String part : expr.split("( OR |;)")) {
      // logger.info("PART = " + part);
      if (part.trim().startsWith("\"") && part.trim().endsWith("\"")) {
        continue;
      }
      // NO semantic types have consecutive letters
      // NOTE: should check against actual STY values (cache them in a map, etc)
      if (part.matches(".*[A-Z][A-Z].*") || !part.matches("^\\s*[a-zA-Z][a-z][a-zA-Z\\/ ,\\d;]+")) {
        return false;
      }
      // NOTE: later we can support hierarchical search of STYs
      // expr.matches("^(<|<<|>)\\s*[a-zA-Z][a-z][a-zA-Z\\/ ,\\d;]+");
    }
    return true;
  }

  /**
   * Gets the sty clause.
   *
   * @param expression the expression
   * @return the sty clause
   */
  public static String getStyClause(final String expression) {

    final List<String> tags = new ArrayList<>();
    // split on "OR" or on ";"
    for (final String tag : expression.trim()
        .split(expression.contains(";") ? ";" : "\\s*OR\\s*")) {
      tags.add(tag.trim());
    }

    final List<String> clauses = tags.stream()
        .map(s -> ((s.startsWith("\"") && s.endsWith("\"") || s.contains(" "))
            ? ("semanticTypes:\"" + s.replaceAll("\"", "") + "\"") : "semanticTypes:" + s))
        .collect(Collectors.toList());

    return clauses.size() == 1 ? clauses.get(0) : StringUtility.composeQuery("OR", clauses);
  }

  /**
   * Gets the expression query.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param publisher the publisher
   * @param version the version
   * @param expr the expr
   * @param indexName the index name
   * @return the expression query
   * @throws Exception the exception
   */
  private static String getExpressionClause(final EntityRepositoryService searchService,
    final String terminology, final String publisher, final String version, final String expr,
    final String indexName) throws Exception {
    if (expr == null || expr.isEmpty()) {
      return "";
    }

    String luceneQuery;
    try {
      luceneQuery = converter.parse(expr);

    } catch (final RecognitionException e) {
      logger.error("  expr = " + expr);
      throw new LocalException("Expression cannot be parsed, must reference an id", e);
    } catch (final NullPointerException e) {
      throw new LocalException("Expression cannot be parsed", e);
    } catch (final Exception e) {
      throw new LocalException(e.getMessage(), e);
    }
    try {
      final Set<EclToLuceneConverter.InternalFunction> x = CONVERTER_MAP.keySet();
      for (final EclToLuceneConverter.InternalFunction internalFunction : x) {
        while (luceneQuery.contains(internalFunction.name())) {
          luceneQuery = processInternalFunction(searchService, terminology, publisher, version,
              luceneQuery, internalFunction, indexName);
        }
      }

      luceneQuery = fixExpressionQuery(luceneQuery);

    } catch (final IOException e) {
      throw new InternalError("Error preparing internal search query.", e);
    }

    // Handle an obvious empty sub-query case
    if (luceneQuery.endsWith("AND ")) {
      return "";
    }
    return luceneQuery;

  }

  /**
   * Process internal function.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param publisher the publisher
   * @param version the version
   * @param luceneQuery the lucene query
   * @param internalFunction the internal function
   * @param indexName the index name
   * @return the string
   * @throws Exception the exception
   */
  private static String processInternalFunction(final EntityRepositoryService searchService,
    final String terminology, final String publisher, final String version,
    final String luceneQuery, final EclToLuceneConverter.InternalFunction internalFunction,
    final String indexName) throws Exception {

    // apply the pattern matcher
    final Matcher matcher = CONVERTER_MAP.get(internalFunction).matcher(luceneQuery);

    // if no match, log and throw error
    if (!matcher.matches() || matcher.groupCount() != 2) {
      final String message = "Failed to extract the id from the function " + internalFunction
          + " in internal query '" + luceneQuery + "'";
      logger.error(message);
      throw new IllegalStateException(message);
    }

    // extract the terminology id
    final String code = matcher.group(2);
    List<String> relatedConcepts = new ArrayList<>(0);

    // if ancestor function
    final boolean inlineFlag =
        internalFunction.name().contains("PARENT") || internalFunction.name().contains("ANCESTOR");
    if (inlineFlag) {
      // Get all ancestors of THIS concept
      final List<String> eclClauses =
          getConceptEcl(searchService, terminology, publisher, version, code, indexName);
      if (eclClauses == null) {
        throw new LocalException("Expression contains missing code = " + terminology + " " + code);
      } else {
        relatedConcepts =
            eclClauses.stream().filter(e -> e.startsWith(internalFunction.getSearchField() + "="))
                .map(e -> e.substring(e.indexOf("=") + 1)).collect(Collectors.toList());
      }
    }

    // if not ancestor function
    else {

      // get ecl clauses matching the internal function field
      relatedConcepts =
          searchService.findAllWithFields(
              StringUtility.composeQuery("AND", "terminology:" + terminology,
                  "publisher:" + publisher, "version:" + version,
                  "ecl:" + internalFunction.getSearchField() + "="
                      + StringUtility.escapeQuery(code)),
              ModelUtility.asList("code"), Concept.class).stream().map(c -> c.getCode())
              .collect(Collectors.toList());
    }

    if (internalFunction.isIncludeSelf()) {
      relatedConcepts.add(code);
    }

    final String newLuceneQuery = luceneQuery.replace(matcher.group(1),
        buildOptionsList(relatedConcepts, !internalFunction.name().contains("ATTRIBUTE")));
    return newLuceneQuery;
  }

  /**
   * Composes a clause of an OR separated list of codes for an inline query.
   *
   * @param relatedCodes the related concepts
   * @param includeIdFieldName the include id field name
   * @return the string
   */
  private static String buildOptionsList(final List<String> relatedCodes,
    final boolean includeIdFieldName) {
    final StringBuilder relativesIdBuilder = new StringBuilder();
    if (!relatedCodes.isEmpty()) {
      relativesIdBuilder.append("(");
      boolean first = true;
      for (final String code : relatedCodes) {
        if (first) {
          first = false;
        } else {
          relativesIdBuilder.append(" OR ");
        }
        if (includeIdFieldName) {
          relativesIdBuilder.append(EclConceptFieldNames.ID).append(":");
        }
        relativesIdBuilder.append(code);
      }
      relativesIdBuilder.append(")");
    } else {
      relativesIdBuilder.append("null");
    }
    return relativesIdBuilder.toString();
  }

  // /**
  // * Count concepts.
  // *
  // * @param searchService the search service
  // * @param terminology the terminology
  // * @param publisher the publisher
  // * @param version the version
  // * @param indexName the index name
  // * @return the int
  // * @throws Exception the exception
  // */
  // public static long countConcepts(final EntityRepositoryService<Concept,
  // String> searchService, final String terminology,
  // final String publisher, final String version, final String indexName)
  // throws Exception {
  //
  // final SearchParameters params = new SearchParameters(2, 0);
  // params.setQuery("terminology:" + StringUtility.escapeQuery(terminology) + "
  // AND publisher:" + publisher
  // + " AND version:" + version);
  //
  // return searchService.find(params, Concept.class, null, null,
  // ModelUtility.asList(indexName)).getTotal();
  //
  // }

  // /**
  // * Count concept relationships.
  // *
  // * @param searchService the search service
  // * @param terminology the terminology
  // * @param publisher the publisher
  // * @param version the version
  // * @param indexName the index name
  // * @return the long
  // * @throws Exception the exception
  // */
  // public static long countRelationships(final
  // EntityRepositoryService<ConceptRelationship, String> searchService,
  // final String terminology, final String publisher, final String version,
  // final String indexName)
  // throws Exception {
  //
  // final SearchParameters params = new SearchParameters(2, 0);
  // params.setQuery("terminology:" + StringUtility.escapeQuery(terminology) + "
  // AND publisher:" + publisher
  // + " AND version:" + version);
  //
  // return searchService.find(params, ConceptRelationship.class, null, null,
  // ModelUtility.asList(indexName))
  // .getTotal();
  //
  // }

  /**
   * Count tree positions.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @param publisher the publisher
   * @param version the version
   * @param indexName the index name
   * @return the long
   * @throws Exception the exception
   */
  public static long countTreePositions(final EntityRepositoryService searchService,
    final String terminology, final String publisher, final String version, final String indexName)
    throws Exception {

    final SearchParameters params = new SearchParameters(2, 0);
    params.setQuery("terminology:" + StringUtility.escapeQuery(terminology) + " AND publisher:"
        + publisher + " AND version:" + version);

    final ResultList<ConceptTreePosition> result =
        searchService.find(params, ConceptTreePosition.class);
    return result.getTotal();
  }

  /**
   * To diagram model.
   *
   * @param concept the concept
   * @param metadata the metadata
   * @return the string
   */
  public static String toDiagramModel(final Concept concept, final List<Metadata> metadata) {

    // concept
    final ObjectMapper objectMapper = new ObjectMapper();
    final JsonNode rootNode = objectMapper.createObjectNode();
    final ObjectNode conceptNode = objectMapper.createObjectNode();
    conceptNode.put("conceptId", concept.getCode());
    final ObjectNode fsnNode = objectMapper.createObjectNode();
    fsnNode.put("term", concept.getName());
    conceptNode.set("fsn", fsnNode);
    conceptNode.put("definitionStatus",
        (concept.getDefined() != null && concept.getDefined()) ? "FULLY_DEFINIED" : "PRIMITIVE");

    // descriptions
    conceptNode.set("descriptions", createDescriptionsNode(concept));

    // relationships
    conceptNode.set("relationships", createRelationshipsNode(concept, metadata));

    ((ObjectNode) rootNode).set("concept", conceptNode);
    return rootNode.toString();

  }

  /**
   * Creates the description node.
   *
   * @param concept the concept
   * @return the array node
   */
  private static ArrayNode createDescriptionsNode(final Concept concept) {

    final ObjectMapper objectMapper = new ObjectMapper();
    final ArrayNode descArray = objectMapper.createArrayNode();
    for (final Term term : concept.getTerms()) {
      if (term.getActive()) {
        final ObjectNode descNode = objectMapper.createObjectNode();
        descNode.put("term", term.getName());
        descNode.put("typeId", term.getType());
        descArray.add(descNode);
      }
    }
    return descArray;
  }

  /**
   * Creates the relationship node.
   *
   * @param concept the concept
   * @param metadata the metadata
   * @return the array node
   */
  private static ArrayNode createRelationshipsNode(final Concept concept,
    final List<Metadata> metadata) {

    final ObjectMapper objectMapper = new ObjectMapper();
    final ArrayNode relArray = objectMapper.createArrayNode();
    for (final ConceptRelationship rel : concept.getRelationships().stream().sorted((a, b) -> {
      final String ha = "" + a.getHierarchical() + (a.getAdditionalType().equals("isa"));
      final String hb = "" + b.getHierarchical() + (b.getAdditionalType().equals("isa"));
      if (ha.equals(hb)) {
        final String ca = (a.getToValue() == null ? a.getTo().getCode() : a.getToValue());
        final String cb = (b.getToValue() == null ? b.getTo().getCode() : b.getToValue());
        return ca.compareTo(cb);
      } else {
        return hb.compareTo(ha);
      }
    }).collect(Collectors.toList())) {

      // Use same algorithm as ECL to determine for non-SNOMED relationships
      // whether
      // to use
      final boolean skipRel = !concept.getTerminology().startsWith("SNOMED")
          && !(rel.getHierarchical() != null && rel.getHierarchical())
          && TreePositionUtility.excludeRelationshipFromGraph(rel.getAdditionalType());

      if (!skipRel && rel.getActive()) {
        final String type = ((rel.getHierarchical() != null && rel.getHierarchical())
            || rel.getAdditionalType().equals("isa")) ? "116680003" : rel.getAdditionalType();
        final ObjectNode relNode = objectMapper.createObjectNode();
        relNode.put("active", true);
        relNode.put("typeId", type);
        // Don't group if not snomed
        if (rel.getGroup() != null && !rel.getGroup().isEmpty()) {
          relNode.put("groupId", Integer.parseInt(rel.getGroup()));
        } else {
          relNode.put("groupId", 0);
        }

        // Type Node
        String typeFsn = type;
        final ObjectNode fsnTypeNode = objectMapper.createObjectNode();
        for (final Metadata m : metadata) {
          if (m.getCode().equals(type)) {
            typeFsn = m.getName();
            continue;
          }
        }
        fsnTypeNode.put("term", typeFsn);
        final ObjectNode typeNode = objectMapper.createObjectNode();
        // 116680003 is a "magic value" for parents
        typeNode.put("conceptId", type);
        typeNode.set("fsn", fsnTypeNode);
        relNode.set("type", typeNode);

        // Target Node
        final ObjectNode fsnNode = objectMapper.createObjectNode();
        final ObjectNode targetNode = objectMapper.createObjectNode();
        if (rel.getTo() != null) {
          targetNode.put("conceptId", rel.getTo().getCode());
          if (rel.getTo().getDefined() != null) {
            targetNode.put("definitionStatus",
                rel.getTo().getDefined() ? "FULLY_DEFINED" : "PRIMITIVE");
          } else {
            targetNode.put("definitionStatus", "PRIMITIVE");
          }
          // targetNode.put("definitionStatus",
          // rel.getTo().getDefined() ? "FULLY_DEFINED" : "PRIMITIVE");
          fsnNode.put("term", rel.getTo().getName());
          targetNode.set("fsn", fsnNode);
        } else {
          targetNode.put("conceptId", rel.getToValue());
          fsnNode.put("term", "");
          targetNode.set("fsn", fsnNode);
        }

        relNode.set("target", targetNode);

        relArray.add(relNode);
      }
    }
    return relArray;
  }

  /**
   * Fix expression query.
   *
   * @param query the query
   * @return the string
   * @throws Exception the exception
   */
  public static String fixExpressionQuery(final String query) throws Exception {
    String luceneQuery = query;
    // logger.info("QUERY = " + query);

    // Handle embedded paired clauses with AND, OR or AND NOT
    // e.g. ( (260769002 OR 312412007) AND NOT (373545003 OR 373244000) )
    while (true) {
      boolean found = false;
      final Pattern p =
          Pattern.compile("\\(\\s+\\(([^\\)]+)\\)\\s+(AND|OR|AND NOT)\\s*\\(([^\\)]+)\\)\\s*\\)");
      final Matcher m = p.matcher(luceneQuery);
      while (m.find()) {
        // logger.info("MATCH1 = " + m.group(0));
        final String first = m.group(1);
        if (first.contains(" AND ") || first.contains(" NOT ") || first.contains("ecl:")) {
          continue;
        }
        final String second = m.group(3);
        if (second.contains(" AND ") || second.contains(" NOT ") || second.contains("ecl:")) {
          continue;
        }
        found = true;
        final String conj = m.group(2);

        final StringBuilder repl = new StringBuilder();
        repl.append("( ");
        final Set<String> firstSet = new HashSet<>(Set.of(first.split(" OR ")));
        final Set<String> secondSet = Set.of(second.split(" OR "));
        if (conj.equals("AND")) {
          // Intersection
          firstSet.retainAll(secondSet);
        } else if (conj.equals("OR")) {
          // Union
          firstSet.addAll(secondSet);
        } else if (conj.equals("AND NOT")) {
          // Difference
          firstSet.removeAll(secondSet);
        }
        final Set<String> merge = firstSet;
        if (merge.isEmpty()) {
          repl.append("null");
        } else {
          repl.append(String.join(" OR ", merge.stream().sorted().collect(Collectors.toList())));
        }
        repl.append(" )");
        luceneQuery = luceneQuery.substring(0, m.start()) + repl + luceneQuery.substring(m.end());
        // logger.info(" FIX = " + luceneQuery);

        break;
      }
      if (!found) {
        break;
      }
    }

    // Handle "null=*"
    // Handle "null= ( (407481006 OR 407478001) AND (446397006 OR 447573005) )"
    while (true) {
      boolean found = false;
      final Pattern p = Pattern.compile("null=\\s*\\*");
      final Matcher m = p.matcher(luceneQuery);
      while (m.find()) {
        luceneQuery =
            luceneQuery.substring(0, m.start()) + "ecl:null=null" + luceneQuery.substring(m.end());
        // luceneQuery = luceneQuery.replaceFirst(Pattern.quote(m.group(0)),
        // "ecl:null=null");
        found = true;
        break;
      }
      if (!found) {
        break;
      }
    }

    // Identify "left hand side" and "right hand side" of an equals
    // Handle "ecl:246075003=(418165002 OR 373545003 )" =>
    // "(ecl:246075003=418165002
    // OR
    // ecl:246075003=373545003)"
    // Handle "(288556008 OR 726633004)=725894000" => "(ecl:288556008=725894000
    // OR
    // ecl:726633004=725894000)"
    // Handle "(288556008 OR 726633004)= ( (407481006 OR 407478001) OR
    // ecl:70296001
    // )"
    // Handle "(288556008 OR 726633004)= ( (407481006 OR 407478001) OR null )"
    // Handle "(288556008 OR 726633004)= ( (407481006 OR 407478001 ) OR
    // (417793006
    // OR 417771001)
    // )"
    // Handle "(288556008 OR 726633004)= ( (407481006 OR 407478001) AND NOT
    // ecl:407477006)"
    // Handle "null= ( 407481006 OR 407478001 )"
    final String leftHandSide = "(ecl:[A-Z0-9][^=\\(]+|\\([^\\)]+\\)|null)";
    // Patterns like (...) or ( ... ( ... ) ... ( ...) ... ) or "726633004" (no
    // spaces or
    // equals)
    final String rightHandSide =
        "(\\([^(]+\\)|\\(([^(]*\\([^\\(]+\\)[^(]*)+[^(]*\\)|[^\\s=\\(\\)]+)";

    // require a space before the pattern because this is for attribute
    // restriction
    // it will
    // always
    // have one
    while (true) {
      boolean found = false;
      final Pattern p = Pattern.compile(" " + leftHandSide + "\\s*=\\s*" + rightHandSide);
      // logger.info("QUERY = " + luceneQuery);
      final Matcher m = p.matcher(luceneQuery);
      while (m.find()) {
        // Either LHS or RHS must have a parens or there is nothing to
        // distribute
        if (!m.group(0).contains("(")) {
          continue;
        }
        // logger.info("MATCH2 = " + m.group(0) + ", " + m.groupCount());
        // Get left hand side match, strip parents and split on "OR"
        final String left = m.group(1);
        // logger.info(" LEFT = " + left);
        // skip if unbalanced parns in left
        if (StringUtils.countMatches(left, "(") != StringUtils.countMatches(left, ")")) {
          // logger.info(" SKIP = unbalanced parens");
          continue;
        }
        final List<String> lhs = Arrays.asList(left.split("OR")).stream()
            .map(s -> s.replaceAll("[\\(\\)]", "").replaceFirst("ecl:", "").trim())
            .collect(Collectors.toList());
        // left has AND or NOT, fail
        if (left.contains(" AND ") || left.contains(" NOT ")) {
          throw new Exception(
              "Unexpected AND or NOT in left hand side of expression " + left + ", " + query);
        }

        // Get right-hand side and strip down to simple codes
        // logger.info(" RIGHT = " + m.group(2) + ", " + (m.group(2) == null));
        final String right = m.group(2).replaceAll("ecl:", "");
        final StringBuilder rewrite = new StringBuilder();
        int i = 0;

        // Go through the expression on the right hand side
        // and replace each code with lhs=$code
        // Identify codes including word boundaries
        final String code = "\\b((ecl\\:)?[A-Z0-9][^ =\\(\\)]+|null)\\b";
        final Pattern p2 = Pattern.compile(code);
        final Matcher m2 = p2.matcher(right);
        while (m2.find()) {
          // skip boolean operators that "look like codes"
          if (m2.group(0).matches("(OR|AND|NOT)")) {
            continue;
          }
          // skip if next char is =
          if (right.length() < m2.end() && right.charAt(m2.end()) == '=') {
            continue;
          }
          // logger.info(" CODE = " + m2.group(0));
          final StringBuilder repl = new StringBuilder();
          if (lhs.size() > 1 && !right.startsWith("(")) {
            repl.append("(");
          }
          for (final String l : lhs) {
            // LHS only supports or logic, so we can assume OR here
            if (repl.length() > 1) {
              repl.append(" OR ");
            }
            repl.append("ecl:").append(l).append("=").append(m2.group(0));
          }
          if (lhs.size() > 1 && !right.startsWith("(")) {
            repl.append(")");
          }
          // logger.info(" REPL '" + m2.group(0) + "' = " + repl);

          // Append from last index to start of this group, then replacement,
          // reset index
          rewrite.append(right.substring(i, m2.start()));
          rewrite.append(repl);
          i = m2.end();
          // right = right.replaceFirst("\\b" + Pattern.quote(m2.group(0)) +
          // "\\b",
          // repl.toString());
          found = true;
        }

        rewrite.append(right.substring(i, right.length()));

        if (found) {
          // logger.info(" GROUP = " + m.start() + ", " + m.end() + ", " +
          // m.group(0));
          // logger.info(" PRE = " + luceneQuery.substring(0, m.start()));
          // logger.info(" REWRITE = " + rewrite);
          // logger.info(" POST = " + luceneQuery.substring(m.end()));
          luceneQuery =
              luceneQuery.substring(0, m.start()) + " " + rewrite + luceneQuery.substring(m.end());
          // luceneQuery =
          // luceneQuery.substring(0, m.start()) + " " + rewrite +
          // luceneQuery.substring(m.end());
          // luceneQuery = luceneQuery.replaceFirst(Pattern.quote(m.group(0)), "
          // " +
          // right);
          // logger.info(" FIX2 = " + luceneQuery);
          break;
        }

      }
      if (!found) {
        break;
      }
    }
    return luceneQuery;
  }

  /**
   * Find closest by release date.
   *
   * @param searchService the search service
   * @param abbreviation the abbreviation
   * @param publisher the publisher
   * @param date the date
   * @return the terminology
   * @throws Exception the exception
   */
  public static Terminology findClosestByReleaseDate(final EntityRepositoryService searchService,
    final String abbreviation, final String publisher, final Date date) throws Exception {
    final ResultList<Terminology> list = searchService.find(
        new SearchParameters("abbreviation:" + StringUtility.escapeQuery(abbreviation)
            + " AND publisher:" + StringUtility.escapeQuery(publisher), null, null, null, null),
        Terminology.class);
    Date maxDate = null;
    Terminology closest = null;
    for (final Terminology terminology : list.getItems()) {
      final Date date2 = DateUtility.DATE_YYYY_MM_DD_DASH.parse(terminology.getReleaseDate());
      // If this is before AND no max or greater than max
      if (date2.compareTo(date) < 0 && (maxDate == null || date2.compareTo(maxDate) > 0)) {
        maxDate = date2;
        closest = terminology;
      }
    }
    return closest;
  }
}
