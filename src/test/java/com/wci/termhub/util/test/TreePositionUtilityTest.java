/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.test.AbstractTest;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.TreePositionUtility;

/**
 * Validates the terminology utility.
 */
public class TreePositionUtilityTest extends AbstractTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(TreePositionUtilityTest.class);

  /**
   * Test compute transitive closure.
   *
   * @throws Exception the exception
   */
  @Test
  public void testComputeTransitiveClosure() throws Exception {
    final Map<String, Set<String>> map = new HashMap<>();
    map.put("a", new HashSet<>());
    map.get("a").add("b");
    map.get("a").add("c");
    map.put("b", new HashSet<>());
    map.get("b").add("d");
    map.put("c", new HashSet<>());
    map.get("c").add("e");

    final List<String> results = TreePositionUtility.computeTransitiveClosure("a", "", map, false);
    assertEquals("[ \"a~b~d\", \"a~c~e\" ]", ModelUtility.prettyFormatJson(results));

    assertFalse(TreePositionUtility.detectCycle("a", map, false));
  }

  /**
   * Test compute transitive closure cycle.
   *
   * @throws Exception the exception
   */
  @Test
  public void testComputeTransitiveClosureCycle() throws Exception {
    final Map<String, Set<String>> map = new HashMap<>();
    map.put("a", new HashSet<>());
    map.get("a").add("b");
    map.get("a").add("c");
    map.put("b", new HashSet<>());
    map.get("b").add("d");
    map.put("c", new HashSet<>());
    map.get("c").add("e");
    map.put("e", new HashSet<>());
    map.get("e").add("a");

    try {
      TreePositionUtility.computeTransitiveClosure("a", "", map, false);
      fail("Unexpected success - should contain cycle");
    } catch (final Exception e) {
      // n/a
    }

    assertTrue(TreePositionUtility.detectCycle("a", map, false));

  }

}
