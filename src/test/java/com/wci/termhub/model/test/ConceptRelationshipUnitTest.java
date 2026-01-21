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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ConceptRelationship;
import com.wci.termhub.test.AbstractTest;
import com.wci.termhub.test.CopyConstructorTester;
import com.wci.termhub.test.EqualsHashcodeTester;
import com.wci.termhub.test.GetterSetterTester;
import com.wci.termhub.test.ProxyTester;
import com.wci.termhub.test.SerializationTester;

/**
 * Unit testing for model object ConceptRelationship.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ConceptRelationshipUnitTest extends AbstractTest {

  /** The model object to test. */
  private ConceptRelationship object;

  /** The c 1. */
  private ConceptRef c1;

  /** The c 2. */
  private ConceptRef c2;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    object = new ConceptRelationship();
    final ProxyTester tester = new ProxyTester(new ConceptRef());
    c1 = (ConceptRef) tester.createObject(1);
    c2 = (ConceptRef) tester.createObject(2);

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
    assertTrue(tester.testCopyConstructor(ConceptRelationship.class));
  }

  /**
   * Test serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelJsonSerialization() throws Exception {
    final SerializationTester tester = new SerializationTester(object);
    tester.proxy(ConceptRef.class, 1, c1);
    assertTrue(tester.testJsonSerialization());
  }

}
