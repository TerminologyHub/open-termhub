/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.model.MetaModel;
import com.wci.termhub.model.TerminologyInfo;
import com.wci.termhub.test.AbstractTest;
import com.wci.termhub.test.CopyConstructorTester;
import com.wci.termhub.test.EqualsHashcodeTester;
import com.wci.termhub.test.GetterSetterTester;
import com.wci.termhub.test.SerializationTester;

/**
 * Unit testing for model object {@link TerminologyInfo}.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TerminologyInfoUnitTest extends AbstractTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(TerminologyInfoUnitTest.class);

  /** The model object to test. */
  private TerminologyInfo object;

  /** The l 1. */
  private List<String> l1;

  /** The l 2. */
  private List<String> l2;

  /** The m 1. */
  private Map<String, String> m1;

  /** The m 2. */
  private Map<String, String> m2;

  /** The m 1. */
  private Map<MetaModel.Model, String> uim1;

  /** The m 2. */
  private Map<MetaModel.Model, String> uim2;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    object = new TerminologyInfo();
    l1 = new ArrayList<>();
    l1.add("1");
    l2 = new ArrayList<>();
    l2.add("2");
    l2.add("3");
    m1 = new HashMap<>();
    m1.put("1", "1");
    m2 = new HashMap<>();
    m2.put("2", "2");
    m2.put("", "3");

    uim1 = new HashMap<>();
    uim1.put(MetaModel.Model.concept, "1");
    uim2 = new HashMap<>();
    uim2.put(MetaModel.Model.concept, "2");
    uim2.put(MetaModel.Model.term, "3");

  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet() throws Exception {
    final GetterSetterTester tester = new GetterSetterTester(object);
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);
    tester.proxy(Map.class, 1, m1);
    tester.proxy(Map.class, 2, m2);
    tester.proxy("uiLabels", 1, uim1);
    tester.proxy("uiLabels", 2, uim2);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode() throws Exception {
    final EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("id");
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);
    tester.proxy(Map.class, 1, m1);
    tester.proxy(Map.class, 2, m2);
    tester.proxy("uiLabels", 1, uim1);
    tester.proxy("uiLabels", 2, uim2);

    assertTrue(tester.testIdentityFieldEquals());
    assertTrue(tester.testNonIdentityFieldEquals());
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentityFieldHashcode());
    assertTrue(tester.testNonIdentityFieldHashcode());
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy() throws Exception {
    final CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);
    tester.proxy(Map.class, 1, m1);
    tester.proxy(Map.class, 2, m2);
    tester.proxy("uiLabels", 1, uim1);
    tester.proxy("uiLabels", 2, uim2);
    assertTrue(tester.testCopyConstructor(TerminologyInfo.class));
  }

  /**
   * Test json serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelSerialization() throws Exception {
    final SerializationTester tester = new SerializationTester(object);
    tester.proxy(List.class, 1, l1);
    tester.proxy(Map.class, 1, m1);
    tester.proxy("uiLabels", 1, uim1);
    assertTrue(tester.testJsonSerialization());
  }

  // /**
  // * Test persistence.
  // *
  // * @throws Exception the exception
  // */
  // @Test
  // public void testPersistence() throws Exception {
  // final PersistenceTester tester = new PersistenceTester(object);
  // tester.include("type");
  // tester.include("publisher");
  // tester.proxy(List.class, 1, l1);
  // tester.proxy(Map.class, 1, m1);
  // tester.proxy("uiLabels", 2, uim2);
  // tester.test();
  // }

}
