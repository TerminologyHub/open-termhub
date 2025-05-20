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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.lucene.LuceneQueryBuilder;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptTreePosition;
import com.wci.termhub.model.Mapping;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.service.FindCallbackHandler;
import com.wci.termhub.util.FieldedStringTokenizer;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.StringUtility;

/**
 * Represents a cache of information used by terminology loaders to allow/support concept-at-a-time
 * loads.
 */
public class TerminologyCache {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(TerminologyCache.class);

  /** The name map. */
  private final Map<String, String> nameMap = new HashMap<>();

  /** The name rank map. */
  private final Map<String, String> nameRankMap = new HashMap<>();

  /** The par chd. */
  private final Map<String, Set<String>> parChd = new HashMap<>();

  /** The historical rels. */
  private final Map<String, Set<String>> historicalRels = new HashMap<>();

  /** The chd par. */
  private final Map<String, Set<String>> chdPar = new HashMap<>();

  /** The statistics. */
  private final Map<String, Integer> statistics = new HashMap<>();

  /** The defined map. */
  private final Map<String, Boolean> definedMap = new HashMap<>();

  /** The active map. */
  private final Map<String, Boolean> activeMap = new HashMap<>();

  /** The leaf map. */
  private final Map<String, Boolean> leafMap = new HashMap<>();

  /**
   * Instantiates a new terminology cache.
   */
  public TerminologyCache() {
  }

  /**
   * Adds the name.
   *
   * @param code the code
   * @param name the name
   * @param rank the rank
   */
  public void addName(final String code, final String name, final String rank) {
    // higher rank is better
    // the caller needs to compute the rank for this name first, then pass it in
    // (each loader
    // does this differently)
    if (!nameRankMap.containsKey(code)
        || (nameRankMap.containsKey(code) && rank.compareTo(nameRankMap.get(code)) > 0)) {
      nameMap.put(code, name);
      nameRankMap.put(code, rank);
    }
  }

  /**
   * Adds the name.
   *
   * @param code the code
   * @param name the name
   */
  public void addName(final String code, final String name) {
    nameMap.put(code, name);
  }

  /**
   * Gets the concept name.
   *
   * @param code the code
   * @return the concept name
   */
  public String getConceptName(final String code) {
    return nameMap.get(code);
  }

  /**
   * Adds the par chd.
   *
   * @param par the par
   * @param chd the chd
   * @throws Exception the exception
   */
  public void addParChd(final String par, final String chd) throws Exception {
    if (par.equals("189975015")) {
      throw new Exception("Descrption as parent");
    }
    if (chd.equals("189975015")) {
      throw new Exception("Descrption as child");
    }
    if (!parChd.containsKey(par)) {
      parChd.put(par, new HashSet<>());
    }
    parChd.get(par).add(chd);
    if (!chdPar.containsKey(chd)) {
      chdPar.put(chd, new HashSet<>());
    }
    chdPar.get(chd).add(par);
  }

  /**
   * Adds the historical rel.
   *
   * @param oldCode the old code
   * @param newCode the new code
   * @param type the type
   * @throws Exception the exception
   */
  public void addHistoricalRel(final String oldCode, final String newCode, final String type)
    throws Exception {
    // Save the historical rel
    if (!historicalRels.containsKey(oldCode)) {
      historicalRels.put(oldCode, new HashSet<>());
    }
    historicalRels.get(oldCode).add(newCode + "|" + type);

    // Only do this if "newCode" is active
    if (activeMap.containsKey(newCode)) {
      // Add a parChd relationship also - but NOT a chdPar (there is not an
      // ancestor here)
      if (!parChd.containsKey(newCode)) {
        parChd.put(newCode, new HashSet<>());
      }
      parChd.get(newCode).add(oldCode);

      // The old code should not have children
      if (parChd.containsKey(oldCode)) {
        throw new Exception("oldCode has children = " + oldCode + ", " + parChd.get(oldCode));
      }
    }
    // Warn if inactive
    else {
      logger
          .warn("  Unexpected inactive targetComponentId of historical relationship = " + newCode);
    }
  }

  /**
   * Gets the descendants with depth.
   *
   * @param code the code
   * @return the descendants with depth
   */
  public Map<String, ConceptRef> getDescendantsWithDepth(final String code) {
    return getDescendantsWithDepthHelper(code, Integer.valueOf(0));
  }

  /**
   * Helps find the descendants with depth.
   *
   * @param code the code
   * @param currentDepth the current depth
   * @return the descendants with depth
   */
  private Map<String, ConceptRef> getDescendantsWithDepthHelper(final String code,
    final Integer currentDepth) {
    // ... transitive closure over parChd ...;
    final Map<String, ConceptRef> descendantDepthMap = new HashMap<>();
    if (parChd.get(code) == null) {
      return descendantDepthMap;
    }

    for (final String child : parChd.get(code)) {

      // check if descendant is already in map from a prior ptr
      // if so, ensure the depth value is the minimum one
      if (!descendantDepthMap.containsKey(child)
          || descendantDepthMap.get(child).getLevel() > currentDepth + 1) {
        // Make a new descendant
        final ConceptRef descRef = new ConceptRef();
        descRef.setLocal(null);
        descRef.setCode(child);
        descRef.setName(getConceptName(child));
        // descendant.setPublisher(concept.getPublisher());
        // descendant.setTerminology(concept.getTerminology());
        // descendant.setVersion(concept.getVersion());
        descRef.setDefined(getDefined(child));
        descRef.setLeaf(isLeaf(child));
        descRef.setLevel(currentDepth + 1);
        // Handle the "historical rel" thing
        // If the child is an "old code" and the parent is a "new code"
        // Set the descendant inactive and set historical field
        if (historicalRels.containsKey(child)) {
          descRef.setActive(false);
          descRef.setHistorical(
              historicalRels.get(child).stream().filter(s -> s.startsWith(code + "|"))
                  .map(s -> s.replaceFirst(".*\\|", "")).findFirst().get());
        }

        descendantDepthMap.put(child, descRef);
      }
      descendantDepthMap.putAll(getDescendantsWithDepthHelper(child, currentDepth + 1));
    }
    return descendantDepthMap;
  }

  // /**
  // * Gets the descendants.
  // *
  // * @param code the code
  // * @return the descendants
  // */
  // public Set<String> getDescendants(final String code) {
  // final Set<String> descendants = new HashSet<>();
  // if (parChd.get(code) == null) {
  // return descendants;
  // }
  // for (final String child : parChd.get(code)) {
  // descendants.add(child);
  // descendants.addAll(getDescendants(child));
  // }
  // return descendants;
  // }

  /**
   * Gets the ancestors with depth. How many recursive levels it took to (first) discover it. So a
   * parent would be 1, a grandparent would be 2, and so on. In SNOMED, the same ancestor may be in
   * multiple paths-to-root, so choose the min depth value.
   *
   * @param code the code
   * @return the ancestors with depth
   */
  public Map<String, Integer> getAncestorsWithDepth(final String code) {
    final Map<String, Integer> ancestorDepthMap = new HashMap<>();
    final Set<String> ptrs = getAncestorPaths(code);
    for (final String ptr : ptrs) {
      final String[] ancestors = FieldedStringTokenizer.split(ptr, "~");
      for (int i = 1; i <= ancestors.length; i++) {
        final String ancestor = ancestors[ancestors.length - i];
        // check if ancestor is already in map from a prior ptr
        // if so, ensure the depth value is the minimum one
        if (ancestorDepthMap.containsKey(ancestor)) {
          if (ancestorDepthMap.get(ancestor) < Integer.valueOf(i)) {
            continue;
          }
        }
        ancestorDepthMap.put(ancestor, Integer.valueOf(i));
      }
    }
    return ancestorDepthMap;
  }

  /**
   * Gets the ancestors.
   *
   * @param code the code
   * @return the ancestors
   */
  public Set<String> getAncestors(final String code) {
    // ... transitive closure over chdPar ...;
    final Set<String> ancestors = new HashSet<>();
    if (chdPar.get(code) == null) {
      return ancestors;
    }
    for (final String parent : chdPar.get(code)) {
      ancestors.add(parent);
      ancestors.addAll(getAncestors(parent));
    }

    return ancestors;
  }

  /**
   * Gets the ancestor paths.
   *
   * @param code the code
   * @return the ancestor paths
   */
  public Set<String> getAncestorPaths(final String code) {
    // ... transitive closure over chdPar to the root ...;
    final Set<String> ancestorPaths = new HashSet<>();
    String child = code;
    if (code.contains("~")) {
      child = code.substring(0, code.indexOf("~"));
    }
    if (chdPar.get(child) != null) {
      for (final String parent : chdPar.get(child)) {
        ancestorPaths.addAll(getAncestorPaths(parent + "~" + code));
      }
    }
    // we've hit the root, so print out ptr
    else {
      // take steps to remove self from ptr
      if (!code.contains("~")) {
        ancestorPaths.add("");
      } else {
        final String parentToRoot = code.substring(0, code.lastIndexOf('~'));
        ancestorPaths.add(parentToRoot);
      }
    }
    return ancestorPaths;
  }

  /**
   * public Set<String> getAncestorPaths(String code) { // ... transitive closure over chdPar to the
   * root ...; Set<String> ancestorPaths = new HashSet<>(); String child = code; if
   * (code.contains("~")) { child = code.substring(0, code.indexOf("~")); } if (chdPar.get(child) !=
   * null) { for (String parent : chdPar.get(child)) { ancestorPaths.addAll(getAncestorPaths(parent
   * + "~" + code)); } // we've hit the root, so print out ptr } else { ancestorPaths.add(code); }
   * return ancestorPaths; }
   *
   * @param par the par
   * @return the children
   */
  /**
   * Gets the children.
   *
   * @param par the par
   * @return the children
   */
  public Set<String> getChildren(final String par) {
    final Set<String> set = parChd.get(par);
    if (set == null) {
      return new HashSet<>(0);
    }
    // Remove historical rels from children calculation
    return set.stream().filter(c -> !historicalRels.containsKey(c)).collect(Collectors.toSet());
  }

  /**
   * Checks for children.
   *
   * @param par the par
   * @return true, if successful
   */
  public boolean hasChildren(final String par) {
    return parChd.containsKey(par);
  }

  /**
   * Checks if is leaf.
   *
   * @param code the code
   * @return true, if is leaf
   */
  public boolean isLeaf(final String code) {
    if (leafMap.containsKey(code)) {
      return leafMap.get(code);
    }
    return !parChd.containsKey(code);
  }

  /**
   * Checks if is defined.
   *
   * @param code the code
   * @return true, if is defined
   */
  public Boolean getDefined(final String code) {

    return definedMap.containsKey(code) ? definedMap.get(code) : Boolean.valueOf(false);
  }

  /**
   * Checks if is active.
   *
   * @param code the code
   * @return true, if is active
   */
  public boolean isActive(final String code) {
    return activeMap.containsKey(code) ? activeMap.get(code) : false;
  }

  /**
   * Gets the parents.
   *
   * @param chd the chd
   * @return the parents
   */
  public Set<String> getParents(final String chd) {
    return chdPar.get(chd);
  }

  /**
   * Checks for parents.
   *
   * @param chd the chd
   * @return true, if successful
   */
  public boolean hasParents(final String chd) {
    return chdPar.containsKey(chd);
  }

  /**
   * Gets the roots.
   *
   * @return the roots
   */
  public Set<String> getRoots() {
    final Set<String> rootCodes = new HashSet<>();
    // things that have children but no parents (i.e. this excludes orphans)
    for (final String par : parChd.keySet()) {
      if (!chdPar.containsKey(par)) {
        rootCodes.add(par);
      }
    }
    return rootCodes;
  }

  /**
   * Gets the statistics.
   *
   * @return the statistics
   */
  public Map<String, Integer> getStatistics() {
    return statistics;
  }

  /**
   * Initialize statistics.
   */
  public void initializeStatistics() {
    statistics.clear();
  }

  /**
   * Compute statistics.
   *
   * @param concept the concept
   */
  public void computeStatistics(final Concept concept) {
    int ct = 0;

    // increment concept stats
    incrementStatistic("concepts", 1);
    if (concept.getActive()) {
      incrementStatistic("conceptsActive", 1);
    } else {
      incrementStatistic("conceptsInactive", 1);
    }

    // increment term stats
    incrementStatistic("terms", concept.getTerms().size());
    ct = (int) concept.getTerms().stream().filter(t -> t.getActive()).count();
    incrementStatistic("termsActive", ct);
    incrementStatistic("termsInactive", concept.getTerms().size() - ct);

    // increment definitions
    incrementStatistic("definitions", concept.getDefinitions().size());

    // increment parents
    ct = (int) concept.getParents().stream().filter(t -> t.getActive()).count();
    incrementStatistic("parentsActive", ct);
    incrementStatistic("parentsInactive", concept.getParents().size() - ct);

    // increment children
    ct = (int) concept.getChildren().stream().filter(t -> t.getActive()).count();
    incrementStatistic("childrenActive", ct);
    incrementStatistic("childrenInactive", concept.getChildren().size() - ct);

    // increment relationships...
    incrementStatistic("relationships", concept.getRelationships().size());

    // increment relationshipsHistorical...
    // ct = (int) concept.getRelationships().stream().filter(t ->
    // t.getHistorical()).count();
    // incrementStatistic("relationshipsHistorical",
    // concept.getRelationships().size() - ct);
  }

  /**
   * Compute statistics.
   *
   * @param tps the tps
   */
  public void computeStatistics(final Collection<ConceptTreePosition> tps) {

    // increment tree positions...
    incrementStatistic("treePositions", tps.size());

  }

  /**
   * Compute statistics.
   *
   * @param mappings the mappings
   */
  public void computeStatistics(final List<Mapping> mappings) {

    incrementStatistic("mappings", mappings.size());
    incrementStatistic("mappingsActive",
        (int) (mappings.stream().filter(m -> m.getActive() != null && m.getActive()).count()));
    incrementStatistic("uniqueFromCodes",
        mappings.stream().map(m -> m.getFrom().getCode()).collect(Collectors.toSet()).size());
    incrementStatistic("uniqueToCodes", mappings.stream().filter(m -> m.getTo() != null)
        .map(m -> m.getTo().getCode()).collect(Collectors.toSet()).size());
    incrementStatistic("mappingsEmptyTarget",
        (int) (mappings.stream().filter(
            m -> m.getTo() == null || m.getTo().getCode() == null || m.getTo().getCode().isEmpty())
            .count()));

  }

  /**
   * Increment statistic.
   *
   * @param key the key
   * @param ct the ct
   */
  public void incrementStatistic(final String key, final int ct) {
    if (!statistics.containsKey(key)) {
      statistics.put(key, ct);
    } else {
      statistics.put(key, statistics.get(key) + ct);
    }
  }

  /**
   * Adds the defined.
   *
   * @param code the code
   * @param defined the defined
   */
  public void addDefined(final String code, final Boolean defined) {
    definedMap.put(code, defined);
  }

  /**
   * Adds the active.
   *
   * @param code the code
   * @param active the active
   */
  public void addActive(final String code, final Boolean active) {
    activeMap.put(code, active);
  }

  /**
   * Adds the leaf. ONLY use this to explicitly override the normal hierarchy-based leaf
   * calculation.
   *
   * @param code the code
   * @param leaf the leaf
   */
  public void addLeaf(final String code, final Boolean leaf) {
    leafMap.put(code, leaf);
  }

  /**
   * Adds the concept.
   *
   * @param concept the concept
   * @throws Exception the exception
   */
  public void addConcept(final Concept concept) throws Exception {
    addActive(concept.getCode(), concept.getActive());
    addDefined(concept.getCode(), concept.getDefined());
    addName(concept.getCode(), concept.getName());
    for (final ConceptRef parent : concept.getParents()) {
      addParChd(parent.getCode(), concept.getCode());
    }
  }

  /**
   * New concept ref.
   *
   * @param code the code
   * @param publisher the publisher
   * @param terminology the terminology
   * @param version the version
   * @return the concept ref
   */
  public ConceptRef newConceptRef(final String code, final String publisher,
    final String terminology, final String version) {
    final ConceptRef cpt = new ConceptRef();
    cpt.setCode(code);
    cpt.setName(getConceptName(code));
    cpt.setPublisher(publisher);
    cpt.setTerminology(terminology);
    cpt.setVersion(version);
    cpt.setDefined(getDefined(code));
    cpt.setLeaf(isLeaf(code));
    cpt.setActive(isActive(code));
    return cpt;
  }

  /**
   * This method loads a terminology cache from elasticsearch for the terminology specified. This
   * cache just has enough populated to generate concept refs.
   *
   * @param searchService the search service
   * @param terminology the terminology
   * @return the terminology cache
   * @throws Exception the exception
   */
  public static TerminologyCache loadTerminologyCache(final EntityRepositoryService searchService,
    final Terminology terminology) throws Exception {

    final TerminologyCache cache = new TerminologyCache();

    final FindCallbackHandler<Concept> handler = new FindCallbackHandler<Concept>() {

      /* see superclass */
      @Override
      public void callback(final List<Concept> list) throws Exception {
        for (final Concept concept : list) {
          cache.addConcept(concept);
        }
      }
    };
    final List<String> fields = ModelUtility.asList("code", "terminology", "publisher", "version",
        "name", "leaf", "defined", "parents", "parents.code");
    final String strQuery = StringUtility.composeQuery("AND",
        "publisher:" + StringUtility.escapeQuery(terminology.getPublisher()),
        "terminology:" + StringUtility.escapeQuery(terminology.getAbbreviation()),
        "version:" + StringUtility.escapeQuery(terminology.getVersion()));
    final Query query = LuceneQueryBuilder.parse(strQuery, Concept.class);
    searchService.findAllWithFields(query, fields, Concept.class, handler);

    logger.info("    cache size = {}", cache.size());
    return cache;
  }

  /**
   * Size.
   *
   * @return the long
   */
  public long size() {
    return nameMap.size();
  }
}
