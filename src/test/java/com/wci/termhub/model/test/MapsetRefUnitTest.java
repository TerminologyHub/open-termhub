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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.model.MapsetRef;
import com.wci.termhub.test.AbstractTest;
import com.wci.termhub.test.CopyConstructorTester;
import com.wci.termhub.test.EqualsHashcodeTester;
import com.wci.termhub.test.GetterSetterTester;
import com.wci.termhub.test.ProxyTester;
import com.wci.termhub.test.SerializationTester;

/**
 * Unit test for {@link MapsetRef}.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MapsetRefUnitTest extends AbstractTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(MapsetRefUnitTest.class);

  /** The model object to test. */
  private MapsetRef object;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    object = new MapsetRef();
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet() throws Exception {
    final GetterSetterTester tester = new GetterSetterTester(object);
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
    tester.include("abbreviation");
    tester.include("name");
    tester.include("version");
    tester.include("latest");
    tester.include("loaded");
    tester.include("publisher");

    assertTrue(tester.testIdentityFieldEquals());
    assertTrue(tester.testNonIdentityFieldEquals());
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentityFieldHashcode());
    assertTrue(tester.testNonIdentityFieldHashcode());
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test model copy.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy() throws Exception {
    final CopyConstructorTester tester = new CopyConstructorTester(object);
    assertTrue(tester.testCopyConstructor(MapsetRef.class));
  }

  /**
   * Test model serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelSerialization() throws Exception {
    final SerializationTester tester = new SerializationTester(object);

    assertTrue(tester.testJsonSerialization());
  }

  /**
   * Test minimize.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMinimize() throws Exception {
    final ProxyTester tester = new ProxyTester(object);
    final MapsetRef m = (MapsetRef) tester.createObject(1);
    assertNotNull(m.getId());
    assertNotNull(m.getConfidence());
    assertNotNull(m.getModified());
    assertNotNull(m.getModifiedBy());
    assertNotNull(m.getCreated());
    assertNotNull(m.getLocal());
    assertNotNull(m.getActive());
    assertNotNull(m.getName());
    assertNotNull(m.getLoaded());
    assertNotNull(m.getFromPublisher());
    assertNotNull(m.getFromTerminology());
    assertNotNull(m.getFromVersion());
    assertNotNull(m.getToPublisher());
    assertNotNull(m.getToTerminology());
    assertNotNull(m.getToVersion());

    m.minimize();
    assertNull(m.getId());
    assertNull(m.getConfidence());
    assertNull(m.getModified());
    assertNull(m.getModifiedBy());
    assertNull(m.getCreated());
    assertNull(m.getLocal());
    assertNull(m.getActive());
    assertNull(m.getName());
    assertNull(m.getLoaded());

    assertNotNull(m.getAbbreviation());
    assertNotNull(m.getVersion());
    assertNotNull(m.getPublisher());
    assertNotNull(m.getLatest());

    assertNotNull(m.getFromPublisher());
    assertNotNull(m.getFromTerminology());
    assertNotNull(m.getFromVersion());
    assertNotNull(m.getToPublisher());
    assertNotNull(m.getToTerminology());
    assertNotNull(m.getToVersion());
  }

  /**
   * Test clean for api.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCleanForApi() throws Exception {
    final ProxyTester tester = new ProxyTester(object);
    final MapsetRef t = (MapsetRef) tester.createObject(1);
    final String pre = t.toString();
    t.cleanForApi();
    assertEquals(pre, t.toString());

  }
}
