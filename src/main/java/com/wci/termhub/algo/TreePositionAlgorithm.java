/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.algo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.model.ConceptTreePosition;
import com.wci.termhub.model.HasId;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.TerminologyUtility;

/**
 * Algorithm to compute tree positions. Should not be autowired as it is not stateless or thread
 * safe. Use ApplicationContext with getBean to get an instance.
 */
@Scope("prototype")
@Component
public class TreePositionAlgorithm extends AbstractTerminologyAlgorithm {

  /** The logger. */
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(TreePositionAlgorithm.class);

  /** The search service. */
  @Autowired
  private EntityRepositoryService searchService;

  /** The cycle tolerant. */
  private boolean cycleTolerant;

  /** The object ct. */
  private int objectCt = 0;

  /** The Constant FETCH_SIZE. */
  private static final int FETCH_SIZE = 20000;

  /** The Constant BATCH_SIZE. */
  private static final int BATCH_SIZE = 20000;

  /**
   * Instantiates an empty {@link TreePositionAlgorithm}.
   *
   * @throws Exception if anything goes wrong
   */
  public TreePositionAlgorithm() throws Exception {
    super();
  }

  /**
   * Instantiates a new tree position algorithm.
   *
   * @param searchService the search service
   * @throws Exception the exception
   */
  public TreePositionAlgorithm(final EntityRepositoryService searchService) throws Exception {
    super();
    this.searchService = searchService;
  }

  /**
   * Indicates whether or not cycle tolerant is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isCycleTolerant() {
    return cycleTolerant;
  }

  /**
   * Sets the cycle tolerant.
   *
   * @param cycleTolerant the cycle tolerant
   */
  public void setCycleTolerant(final boolean cycleTolerant) {
    this.cycleTolerant = cycleTolerant;
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  @Override
  public void compute() throws Exception {

    logInfo("START compute tree positions");
    logInfo("  terminology = " + getTerminology());
    logInfo("  publisher = " + getPublisher());
    logInfo("  version = " + getVersion());

    if (searchService == null) {
      throw new NullPointerException("searchService is null");
    }

    searchService.createIndex(ConceptTreePosition.class);

    // Look up the terminology to make sure it exists
    final Terminology term = TerminologyUtility.getTerminology(searchService, getTerminology(),
        getPublisher(), getVersion());
    if (term == null) {
      throw new Exception("Unable to find terminology = " + getTerminology() + ", publisher = "
          + getPublisher() + ", version = " + getVersion());
    }

    final Date startDate = new Date();

    // with hierarchical = true and active = true
    logInfo("  Find hierarchical relationships");

    // Load blocks of 10k, need to specify sort=id for this to work
    final String queryStr =
        TerminologyUtility.getTerminologyQuery(getTerminology(), getPublisher(), getVersion());
    final SearchParameters params = new SearchParameters(
        queryStr + " AND hierarchical:true AND active:true", 0, FETCH_SIZE, "id", null);

    final ResultList<ConceptRelationship> list = new ResultList<>();
    final Map<String, String> additionalTypeMap = new HashMap<>();
    while (true) {
      final ResultList<ConceptRelationship> innerList =
          searchService.find(params, ConceptRelationship.class);
      if (innerList.getItems().isEmpty()) {
        break;
      }

      // Verify that codes only participate in a tree of one particular type
      // ASSUMPTION: rel.getAdditionalType() != null
      for (final ConceptRelationship rel : innerList.getItems()) {
        final String additionalType =
            rel.getAdditionalType() == null ? "<blank>" : rel.getAdditionalType();
        if (additionalTypeMap.containsKey(rel.getFrom().getCode())
            && !additionalTypeMap.get(rel.getFrom().getCode()).equals(additionalType)) {
          // If this occurs, we need to handle the situation of the same code
          // being involved in multiple trees each with their own relationship
          // type
          // Since ATC mixes RELAs in the same tree, we would then need
          // terminology-level
          // metadata about what kind of "case" we are dealing with so this
          // algorithm
          // could respond appropriately
          throw new Exception(
              "Unexpected condition: same code, multiple additional relationship types = "
                  + rel.getFrom().getCode() + ", " + additionalType + ", "
                  + additionalTypeMap.get(rel.getFrom().getCode()));
        }
        additionalTypeMap.put(rel.getFrom().getCode(), additionalType);
      }

      // searchAfter = innerList.getItems().get(innerList.getItems().size() -
      // 1).getId();

      list.getItems().addAll(innerList.getItems());
      logInfo("    count = " + list.getItems().size());
      params.setOffset(params.getOffset() + FETCH_SIZE);
    }

    logInfo("    relationships = " + list.getItems().size());
    logInfo("    relationship ids = "
        + list.getItems().stream().map(r -> r.getId()).collect(Collectors.toSet()).size());

    int ct = 0;
    boolean polyHierarchy = false;

    final Map<String, Set<String>> parChd = new HashMap<>();
    final Map<String, Set<String>> chdPar = new HashMap<>();
    final HashSet<String> codes = new HashSet<>();

    // Get total concept count for progress monitoring
    codes.addAll(parChd.keySet());
    codes.addAll(chdPar.keySet());
    final int totalConceptCt = codes.size();
    codes.clear();

    logInfo("    processing hierarchies");
    for (final ConceptRelationship rel : list.getItems()) {

      final String fromCode = rel.getFrom().getCode();
      final String toCode = rel.getTo().getCode();

      ct++;
      // logger.debug(" from(chd)/to(par) = " + fromCode + ", " + toCode);
      if (!parChd.containsKey(toCode)) {
        parChd.put(toCode, new HashSet<>());
      }
      parChd.get(toCode).add(fromCode);

      if (!chdPar.containsKey(fromCode)) {
        chdPar.put(fromCode, new HashSet<>());
      } else {
        polyHierarchy = true;
      }
      chdPar.get(fromCode).add(toCode);

      // Check cancel flag
      if (ct % 5000 == 0) {
        checkCancel();
      }
    }

    if (ct == 0) {
      logInfo("    NO HIERARCHICAL RELATIONSHIPS");
      return;
    } else {
      logInfo("  concepts with descendants = " + parChd.size());
    }

    // Find roots (iterate through parents and see if they are children)
    final Set<String> rootCodes = new HashSet<>();
    for (final String par : parChd.keySet()) {
      // things with no children
      if (!chdPar.containsKey(par)) {
        rootCodes.add(par);
      }
    }
    logInfo("  root codes count = " + rootCodes.size() + ", " + rootCodes);

    // Something went wrong - fail
    if (rootCodes.size() > 100) {
      throw new Exception("Too many root codes, something is wrong");
    }
    chdPar.clear();

    term.getRoots().addAll(rootCodes);

    for (final String rootCode : rootCodes) {

      // Check cancel flag
      checkCancel();

      final ValidationResult result = new ValidationResult();

      final List<HasId> batch = new ArrayList<>();
      computeTreePositions(rootCode, "", parChd, result, startDate, rootCodes.size() > 1,
          additionalTypeMap, searchService, batch, new ArrayList<>(), totalConceptCt);
      if (!result.isValid()) {
        logError("  validation result = " + result);
        throw new Exception("Validation failed");
      }

      // Handle any leftover batch
      if (!batch.isEmpty()) {
        // Process the final batch
        logInfo("    BATCH index = " + batch.size());
        searchService.addBulk(ConceptTreePosition.class, batch);
        objectCt += batch.size();
        batch.clear();
      }

      // Check cancel flag
      checkCancel();

    }

    // Mark the terminology as having computed tree positions and count tree positions
    term.getAttributes().put(Terminology.Attributes.treePositions.property(), "true");
    term.getAttributes().put(Terminology.Attributes.hierarchical.property(), "true");
    if (polyHierarchy) {
      term.getAttributes().put(Terminology.Attributes.polyhierarchy.property(), "true");
    }
    term.setTreePositionCt(TerminologyUtility.countTreePositions(searchService,
        term.getAbbreviation(), term.getPublisher(), term.getVersion()));

    // Update index
    searchService.update(Terminology.class, term.getId(), term);

    logInfo("  treepos count = " + objectCt);
    logInfo("FINISH compute tree positions");

  }

  /**
   * Compute tree positions.
   *
   * @param code the code
   * @param ancestorPath the ancestor path
   * @param parChd the par chd
   * @param validationResult the validation result
   * @param startDate the start date
   * @param multipleRoots the multiple roots
   * @param additionalTypeMap the additional type map
   * @param searchService the search service
   * @param batch the batch
   * @param seen the seen
   * @param totalConceptCt the total concept ct
   * @return the sets the
   * @throws Exception the exception
   */
  public Set<String> computeTreePositions(final String code, final String ancestorPath,
    final Map<String, Set<String>> parChd, final ValidationResult validationResult,
    final Date startDate, final boolean multipleRoots, final Map<String, String> additionalTypeMap,
    final EntityRepositoryService searchService, final List<HasId> batch, final List<String> seen,
    final int totalConceptCt) throws Exception {

    final Set<String> descConceptCodes = new HashSet<>();

    // Check for cycles
    final Set<String> ancestors = new HashSet<>();
    for (final String ancestor : ancestorPath.split("~")) {
      ancestors.add(ancestor);
    }
    if (ancestors.contains(code)) {

      if (cycleTolerant) {
        return descConceptCodes;
      } else {
        // add error to validation result
        validationResult.getErrors()
            .add("Cycle detected for concept " + code + ", ancestor path is " + ancestorPath);
      }

      // return empty set of descendants to truncate calculation on this
      // path
      return descConceptCodes;
    }

    // Only create tree position if we haven't seen this code before
    if (!seen.contains(code)) {
      seen.add(code);

      // Instantiate the tree position
      final ConceptTreePosition tp = new ConceptTreePosition();
      setCommonFields(tp);
      final SearchParameters params = new SearchParameters(
          TerminologyUtility.getTerminologyQuery(getTerminology(), getPublisher(), getVersion())
              + " AND code:" + code,
          0, 1, null, null);
      final Concept concept = searchService.findSingle(params, Concept.class);
      final ConceptRef ref = new ConceptRef();
      ref.setActive(concept.getActive());
      ref.setLocal(concept.getLocal());
      ref.setCode(code);
      ref.setName(concept.getName());
      ref.setTerminology(getTerminology());
      ref.setPublisher(getPublisher());
      ref.setLeaf(concept.getLeaf());
      tp.setConcept(ref);
      tp.setAncestorPath(ancestorPath);

      // Lookup the additional type rel to the parent
      tp.setAdditionalType(additionalTypeMap.get(code));

      // construct the ancestor path terminating at this concept
      final String conceptPath = (ancestorPath.equals("") ? code : ancestorPath + "~" + code);

      // Gather descendants if this is not a leaf node
      if (parChd.containsKey(code)) {

        descConceptCodes.addAll(parChd.get(code));

        // iterate over the child terminology codes this iteration is entirely
        // local and depends on no managed objects
        for (final String childConceptCode : parChd.get(code)) {

          // call helper function on child concept add the results to the local
          // descendant set
          final Set<String> desc = computeTreePositions(childConceptCode, conceptPath, parChd,
              validationResult, startDate, multipleRoots, additionalTypeMap, searchService, batch,
              seen, totalConceptCt);
          descConceptCodes.addAll(desc);
        }
      }

      // set the children count
      tp.setChildCt(parChd.containsKey(code) ? parChd.get(code).size() : 0);
      batch.add(tp);
      if ((batch.size() % BATCH_SIZE) == 0) {
        logInfo("    BATCH index = " + batch.size());
        searchService.addBulk(ConceptTreePosition.class, batch);
        objectCt += batch.size();
        batch.clear();

        // Progress update
        fireProgressEvent((int) ((seen.size() * 1.0 / totalConceptCt)), "");
      }
      objectCt++;

      // check for cancel request
      checkCancel();
    }

    // Check that this concept does not reference itself as a child
    if (descConceptCodes.contains(code)) {

      // add error to validation result
      validationResult.getErrors().add("Concept " + code + " claims itself as a child");

      // remove this terminology code to prevent infinite loop
      descConceptCodes.remove(code);
    }

    // return the descendant concept set note that the local child and
    // descendant set will be garbage collected
    return descConceptCodes;

  }

  /**
   * Check preconditions.
   *
   * @return the validation result
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    // n/a
    return new ValidationResult();
  }

  /**
   * Sets the properties.
   *
   * @param p the new properties
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void setProperties(final Properties p) throws Exception {
    // n/a
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  /* see superclass */
  @Override
  public String getDescription() {
    return "Tree position computer";
  }

}
