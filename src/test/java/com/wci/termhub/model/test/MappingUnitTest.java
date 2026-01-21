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

import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.Mapping;
import com.wci.termhub.model.MapsetRef;
import com.wci.termhub.test.AbstractTest;
import com.wci.termhub.test.CopyConstructorTester;
import com.wci.termhub.test.EqualsHashcodeTester;
import com.wci.termhub.test.GetterSetterTester;
import com.wci.termhub.test.ProxyTester;
import com.wci.termhub.test.SerializationTester;

/**
 * Unit testing for model object Mapping.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MappingUnitTest extends AbstractTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(MappingUnitTest.class);

  /** The model object to test. */
  private Mapping object;

  /** The at 1. */
  private Map<String, String> at1;

  /** The at 2. */
  private Map<String, String> at2;

  /** The c 1. */
  private ConceptRef c1;

  /** The c 2. */
  private ConceptRef c2;

  /** The m 1. */
  private MapsetRef m1;

  /** The m 2. */
  private MapsetRef m2;

  /** The l 1. */
  private List<String> l1;

  /** The l 2. */
  private List<String> l2;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    object = new Mapping();

    final ProxyTester tester = new ProxyTester(new ConceptRef());
    c1 = (ConceptRef) tester.createObject(1);
    c2 = (ConceptRef) tester.createObject(2);

    final ProxyTester tester2 = new ProxyTester(new MapsetRef());
    m1 = (MapsetRef) tester2.createObject(1);
    m2 = (MapsetRef) tester2.createObject(2);

    l1 = new ArrayList<>();
    l1.add("1");
    l2 = new ArrayList<>();
    l2.add("2");
    l2.add("3");

    // attribute maps
    at1 = new HashMap<>();
    at1.put("1", "1");
    at2 = new HashMap<>();
    at2.put("2", "2");
    at2.put("3", "3");

  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet() throws Exception {
    final GetterSetterTester tester = new GetterSetterTester(object);
    tester.proxy(ConceptRef.class, 1, c1);
    tester.proxy(ConceptRef.class, 2, c2);
    tester.proxy(MapsetRef.class, 1, m1);
    tester.proxy(MapsetRef.class, 2, m2);
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);
    tester.proxy("attributes", 1, at1);
    tester.proxy("attributes", 2, at2);
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
    tester.proxy(ConceptRef.class, 1, c1);
    tester.proxy(ConceptRef.class, 2, c2);
    tester.proxy(MapsetRef.class, 1, m1);
    tester.proxy(MapsetRef.class, 2, m2);
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);
    tester.proxy("attributes", 1, at1);
    tester.proxy("attributes", 2, at2);
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
    tester.proxy(ConceptRef.class, 1, c1);
    tester.proxy(MapsetRef.class, 1, m1);
    tester.proxy(List.class, 1, l1);
    tester.proxy("attributes", 1, at1);
    assertTrue(tester.testCopyConstructor(Mapping.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelJsonSerialization() throws Exception {
    final SerializationTester tester = new SerializationTester(object);
    tester.proxy(ConceptRef.class, 1, c1);
    tester.proxy(MapsetRef.class, 1, m1);
    tester.proxy(List.class, 1, l1);
    tester.proxy("attributes", 1, at1);
    assertTrue(tester.testJsonSerialization());
  }

}
