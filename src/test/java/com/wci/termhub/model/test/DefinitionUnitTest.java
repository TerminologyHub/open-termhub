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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.model.Definition;
import com.wci.termhub.test.AbstractTest;
import com.wci.termhub.test.CopyConstructorTester;
import com.wci.termhub.test.EqualsHashcodeTester;
import com.wci.termhub.test.GetterSetterTester;
import com.wci.termhub.test.SerializationTester;

/**
 * Unit testing for model object Definition.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DefinitionUnitTest extends AbstractTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(DefinitionUnitTest.class);

  /** The model object to test. */
  private Definition object;

  /** The lm 1. */
  private Map<String, Boolean> lm1;

  /** The lm 2. */
  private Map<String, Boolean> lm2;

  /** The at 1. */
  private Map<String, String> at1;

  /** The at 2. */
  private Map<String, String> at2;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    object = new Definition();

    // locale maps
    lm1 = new HashMap<>();
    lm1.put("1", true);
    lm2 = new HashMap<>();
    lm2.put("2", true);
    lm2.put("3", true);

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
    tester.proxy("localeMap", 1, lm1);
    tester.proxy("localeMap", 2, lm2);
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
    tester.proxy("localeMap", 1, lm1);
    tester.proxy("localeMap", 2, lm2);
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
    tester.proxy("localeMap", 1, lm1);
    tester.proxy("attributes", 1, at1);
    assertTrue(tester.testCopyConstructor(Definition.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelJsonSerialization() throws Exception {
    final SerializationTester tester = new SerializationTester(object);
    tester.proxy("localeMap", 1, lm1);
    tester.proxy("attributes", 1, at1);
    assertTrue(tester.testJsonSerialization());
  }

}
