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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.model.Metadata;

/**
 * Utility for interacting with JWT tokens.
 */
public final class TreePositionUtility {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(TreePositionUtility.class);

  /**
   * Instantiates an empty {@link TreePositionUtility}.
   *
   * @throws Exception the exception
   */
  private TreePositionUtility() throws Exception {
    // n/a
  }

  /**
   * Compute relationship clauses.
   *
   * @param relaMap the rela map
   * @param terminology the terminology
   * @param type the type
   * @param toCode the to code
   * @param toValue the to value
   * @param clauses the clauses
   * @return true, if successful
   * @throws Exception the exception
   */
  public static boolean computeRelationshipClauses(final Map<String, Metadata> relaMap,
    final String terminology, final String type, final String toCode, final String toValue,
    final Set<String> clauses) throws Exception {
    boolean flag = false;

    // Index all SNOMED relationships
    if (terminology.startsWith("SNOMEDCT")) {
      if (type != null && !type.isEmpty()) {
        flag = true;
        clauses.add(type + "=" + toCode);
        clauses.add(type + "=*");
        // SNOMEDCT loaded through RRF
        if (relaMap.containsKey(type) && relaMap.get(type).getAttributes().containsKey("SCTID")) {
          final String sctid = relaMap.get(type).getAttributes().get("SCTID");
          if (toCode != null) {
            clauses.add(sctid + "=" + toCode);
          } else {
            clauses.add(sctid + "=" + toValue);
          }
          clauses.add(sctid + "=*");
        }
      }
    }
    // LNC, etc.
    // Otherwise, check exclude list
    else if (!excludeRelationshipFromEcl(type)) {
      flag = true;
      if (toCode != null) {
        clauses.add(type + "=" + toCode);
      } else {
        clauses.add(type + "=" + toValue);
      }
      clauses.add(type + "=*");
    }
    return flag;
  }

  /**
   * Exclude these standard non-defining types for performance (and they are
   * HIGH volume). NOTE: keep LOINC "answer_to" and some others
   * 
   * @param type the type
   * @return true, if successful
   */
  public static boolean excludeRelationshipFromEcl(final String type) {
    // TODO: this should be done by the "terminology semantics handler"
    if (type.equals("answer_to") || type.equals("component_of") || type.equals("divisor_of")) {
      return false;
    }
    // Overrides for RXNORM
    if (type.equals("has_tradename")) {
      return true;
    }
    if (type.equals("tradename_of")) {
      return false;
    }

    return StringUtility.isEmpty(type) || type.endsWith("_to") || type.endsWith("_of")
        || type.endsWith("Of") || type.endsWith("_by") || type.endsWith("_for")
        || type.endsWith("_in") || type.startsWith("inverse_");
  }

  /**
   * Exclude relationship from G.
   *
   * @param type the type
   * @return true, if successful
   */
  public static boolean excludeRelationshipFromGraph(final String type) {
    // TODO: this should be done by the "terminology semantics handler"
    if (type.equals("component_of") || type.equals("divisor_of")) {
      return true;
    }
    return excludeRelationshipFromEcl(type);
  }

  /**
   * Detect cycle.
   *
   * @param code the code
   * @param reverseParChd the reverse par chd
   * @param tolerateCycles the tolerate cycles
   * @return true, if successful
   * @throws Exception the exception
   */
  public static boolean detectCycle(final String code, final Map<String, Set<String>> reverseParChd,
    final boolean tolerateCycles) throws Exception {
    try {
      computeTransitiveClosure(code, "", reverseParChd, tolerateCycles);
      return false;
    } catch (LocalException e) {
      return true;
    }
  }

  /**
   * Compute transitive closure.
   *
   * @param code the code
   * @param ancestorPath the ancestor path
   * @param parChd the par chd
   * @param cycleTolerant the tolerate cycles
   * @return the list
   * @throws Exception the exception
   */
  public static List<String> computeTransitiveClosure(final String code, final String ancestorPath,
    final Map<String, Set<String>> parChd, final boolean cycleTolerant) throws Exception {

    final List<String> paths = new ArrayList<>();
    // Check for cycles
    final Set<String> ancestors = new HashSet<>();
    for (final String ancestor : ancestorPath.split("~")) {
      ancestors.add(ancestor);
    }
    if (ancestors.contains(code)) {
      if (cycleTolerant) {
        logger.info("Cycle detected = " + ancestorPath + ", " + code);
        return null;
      } else {
        throw new LocalException("Cycle detected = " + ancestorPath + ", " + code);
      }
    }

    final String conceptPath = (ancestorPath.equals("") ? code : ancestorPath + "~" + code);

    // If leaf node, add the path and return
    if (!parChd.containsKey(code)) {
      paths.add(conceptPath);
    }
    // If not leaf node, compute children and add results that are not null
    else {
      for (final String chd : parChd.get(code)) {
        final List<String> chdResults =
            computeTransitiveClosure(chd, conceptPath, parChd, cycleTolerant);
        if (chdResults != null) {
          paths.addAll(chdResults);
        }
      }
    }
    return paths;
  }
}
