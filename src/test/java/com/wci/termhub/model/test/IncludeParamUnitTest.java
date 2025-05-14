/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.model.IncludeParam;
import com.wci.termhub.test.AbstractTest;

/**
 * Unit testing for model object {@link IncludeParam}.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class IncludeParamUnitTest extends AbstractTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(IncludeParamUnitTest.class);

  /**
   * Test constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMinimal() throws Exception {

    IncludeParam ip = null;

    // Test empty
    ip = new IncludeParam();
    for (final String field : IncludeParam.getConceptBaseFields()) {
      assertThat(ip.getIncludedFields()).contains(field);
    }
    assertThat(ip.isAttributes()).isFalse();
    assertThat(ip.isAxioms()).isFalse();
    assertThat(ip.isChildren()).isFalse();
    assertThat(ip.isDefinitions()).isFalse();
    assertThat(ip.isDescendants()).isFalse();
    assertThat(ip.isHighlights()).isFalse();
    assertThat(ip.isInverseRelationships()).isFalse();
    assertThat(ip.isMapsets()).isFalse();
    assertThat(ip.isParents()).isFalse();
    assertThat(ip.isRelationships()).isFalse();
    assertThat(ip.isSemanticTypes()).isFalse();
    assertThat(ip.isSubsets()).isFalse();
    assertThat(ip.isTerms()).isFalse();
    assertThat(ip.isTreePositions()).isFalse();

    // Test null
    ip = new IncludeParam((String) null);
    for (final String field : IncludeParam.getConceptBaseFields()) {
      assertThat(ip.getIncludedFields()).contains(field);
    }
    assertThat(ip.isAttributes()).isFalse();
    assertThat(ip.isAxioms()).isFalse();
    assertThat(ip.isChildren()).isFalse();
    assertThat(ip.isDefinitions()).isFalse();
    assertThat(ip.isDescendants()).isFalse();
    assertThat(ip.isHighlights()).isFalse();
    assertThat(ip.isInverseRelationships()).isFalse();
    assertThat(ip.isMapsets()).isFalse();
    assertThat(ip.isParents()).isFalse();
    assertThat(ip.isRelationships()).isFalse();
    assertThat(ip.isSemanticTypes()).isFalse();
    assertThat(ip.isSubsets()).isFalse();
    assertThat(ip.isTerms()).isFalse();
    assertThat(ip.isTreePositions()).isFalse();

    // Test "minimal"
    ip = new IncludeParam("minimal");
    for (final String field : IncludeParam.getConceptBaseFields()) {
      assertThat(ip.getIncludedFields()).contains(field);
    }
    assertThat(ip.isAttributes()).isFalse();
    assertThat(ip.isAxioms()).isFalse();
    assertThat(ip.isChildren()).isFalse();
    assertThat(ip.isDefinitions()).isFalse();
    assertThat(ip.isDescendants()).isFalse();
    assertThat(ip.isHighlights()).isFalse();
    assertThat(ip.isInverseRelationships()).isFalse();
    assertThat(ip.isMapsets()).isFalse();
    assertThat(ip.isParents()).isFalse();
    assertThat(ip.isRelationships()).isFalse();
    assertThat(ip.isSemanticTypes()).isFalse();
    assertThat(ip.isSubsets()).isFalse();
    assertThat(ip.isTerms()).isFalse();
    assertThat(ip.isTreePositions()).isFalse();
  }

  /**
   * Test summary.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSummary() throws Exception {

    IncludeParam ip = null;

    // Test "summary"
    ip = new IncludeParam("summary");
    for (final String field : IncludeParam.getConceptBaseFields()) {
      assertThat(ip.getIncludedFields()).contains(field);
    }
    assertThat(ip.isAttributes()).isTrue();
    assertThat(ip.isAxioms()).isFalse();
    assertThat(ip.isChildren()).isFalse();
    assertThat(ip.isDefinitions()).isTrue();
    assertThat(ip.isDescendants()).isFalse();
    assertThat(ip.isHighlights()).isFalse();
    assertThat(ip.isInverseRelationships()).isFalse();
    assertThat(ip.isMapsets()).isFalse();
    assertThat(ip.isParents()).isFalse();
    assertThat(ip.isRelationships()).isFalse();
    assertThat(ip.isSemanticTypes()).isTrue();
    assertThat(ip.isSubsets()).isFalse();
    assertThat(ip.isTerms()).isTrue();
    assertThat(ip.isTreePositions()).isFalse();

    // Test "summary" plus
    ip = new IncludeParam("summary,descendants");
    for (final String field : IncludeParam.getConceptBaseFields()) {
      assertThat(ip.getIncludedFields()).contains(field);
    }
    assertThat(ip.isAttributes()).isTrue();
    assertThat(ip.isAxioms()).isFalse();
    assertThat(ip.isChildren()).isFalse();
    assertThat(ip.isDefinitions()).isTrue();
    assertThat(ip.isDescendants()).isTrue();
    assertThat(ip.isHighlights()).isFalse();
    assertThat(ip.isInverseRelationships()).isFalse();
    assertThat(ip.isMapsets()).isFalse();
    assertThat(ip.isParents()).isFalse();
    assertThat(ip.isRelationships()).isFalse();
    assertThat(ip.isSemanticTypes()).isTrue();
    assertThat(ip.isSubsets()).isFalse();
    assertThat(ip.isTerms()).isTrue();
    assertThat(ip.isTreePositions()).isFalse();

    // Test "summary" plus
    ip = new IncludeParam("summary,descendants,subsets");
    for (final String field : IncludeParam.getConceptBaseFields()) {
      assertThat(ip.getIncludedFields()).contains(field);
    }
    assertThat(ip.isAttributes()).isTrue();
    assertThat(ip.isAxioms()).isFalse();
    assertThat(ip.isChildren()).isFalse();
    assertThat(ip.isDefinitions()).isTrue();
    assertThat(ip.isDescendants()).isTrue();
    assertThat(ip.isHighlights()).isFalse();
    assertThat(ip.isInverseRelationships()).isFalse();
    assertThat(ip.isMapsets()).isFalse();
    assertThat(ip.isParents()).isFalse();
    assertThat(ip.isRelationships()).isFalse();
    assertThat(ip.isSemanticTypes()).isTrue();
    assertThat(ip.isSubsets()).isTrue();
    assertThat(ip.isTerms()).isTrue();
    assertThat(ip.isTreePositions()).isFalse();
  }

  /**
   * Test full.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFull() throws Exception {

    IncludeParam ip = null;

    // Test "full"
    ip = new IncludeParam("full");
    for (final String field : IncludeParam.getConceptBaseFields()) {
      assertThat(ip.getIncludedFields()).contains(field);
    }
    assertThat(ip.isAttributes()).isTrue();
    assertThat(ip.isAxioms()).isTrue();
    assertThat(ip.isChildren()).isTrue();
    assertThat(ip.isDefinitions()).isTrue();
    assertThat(ip.isDescendants()).isFalse();
    assertThat(ip.isHighlights()).isFalse();
    assertThat(ip.isInverseRelationships()).isTrue();
    assertThat(ip.isMapsets()).isTrue();
    assertThat(ip.isParents()).isTrue();
    assertThat(ip.isRelationships()).isTrue();
    assertThat(ip.isSemanticTypes()).isTrue();
    assertThat(ip.isSubsets()).isTrue();
    assertThat(ip.isTerms()).isTrue();
    assertThat(ip.isTreePositions()).isFalse();
  }

  /**
   * Test bad.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  @Test
  public void testBad() throws Exception {

    // Test "bad"
    try {
      new IncludeParam("bad");
      fail("Expected an exception");
    } catch (final Exception e) {
      // n/a
    }

    // Test "summary,bad"
    try {
      new IncludeParam("summary,bad");
      fail("Expected an exception");
    } catch (final Exception e) {
      // n/a
    }
  }

  /**
   * Test highlights.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHighlights() throws Exception {

    IncludeParam ip = null;
    String[] fields = null;

    // Test "highlights"
    ip = new IncludeParam("highlights");
    fields = ip.getIncludedFields();
    assertThat(fields[fields.length - 1].equals("highlights")).isTrue();

    ip = new IncludeParam("highlights,full");
    fields = ip.getIncludedFields();
    assertThat(fields[fields.length - 1].equals("highlights")).isTrue();

  }
}
