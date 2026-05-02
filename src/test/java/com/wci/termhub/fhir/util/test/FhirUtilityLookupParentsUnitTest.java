/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.wci.termhub.fhir.util.FhirUtility;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;

/**
 * Unit tests for {@link FhirUtility#distinctHierarchicalParents}.
 */
public class FhirUtilityLookupParentsUnitTest {

  /**
   * Duplicate hierarchical edges to the same parent code yield one entry (first
   * relationship wins).
   */
  @Test
  public void testDistinctHierarchicalParentsCollapsesDuplicateParentCodes() {
    final ConceptRef from = new ConceptRef("10001-6", "Child");
    final ConceptRef toA = new ConceptRef("LP407090-2", "Display A");
    final ConceptRef toB = new ConceptRef("LP407090-2", "Display B");

    final List<ConceptRelationship> rels = new ArrayList<>();
    rels.add(hierarchicalRel(from, toA));
    rels.add(hierarchicalRel(from, toB));

    final List<ConceptRef> parents = FhirUtility.distinctHierarchicalParents(rels);
    assertEquals(1, parents.size());
    assertEquals("LP407090-2", parents.get(0).getCode());
    assertEquals("Display A", parents.get(0).getName());
  }

  /**
   * Non-hierarchical relationships are ignored.
   */
  @Test
  public void testDistinctHierarchicalParentsIgnoresNonHierarchical() {
    final ConceptRef from = new ConceptRef("A", "a");
    final ConceptRef to = new ConceptRef("B", "b");
    final ConceptRelationship flat = hierarchicalRel(from, to);
    flat.setHierarchical(false);

    final List<ConceptRef> parents = FhirUtility.distinctHierarchicalParents(List.of(flat));
    assertTrue(parents.isEmpty());
  }

  /**
   * Null or empty input yields an empty list.
   */
  @Test
  public void testDistinctHierarchicalParentsNullOrEmpty() {
    assertTrue(FhirUtility.distinctHierarchicalParents(null).isEmpty());
    assertTrue(FhirUtility.distinctHierarchicalParents(List.of()).isEmpty());
  }

  /**
   * Hierarchical rel.
   *
   * @param from the from
   * @param to the to
   * @return the concept relationship
   */
  private static ConceptRelationship hierarchicalRel(final ConceptRef from, final ConceptRef to) {
    final ConceptRelationship r = new ConceptRelationship();
    r.setHierarchical(true);
    r.setFrom(from);
    r.setTo(to);
    return r;
  }
}
