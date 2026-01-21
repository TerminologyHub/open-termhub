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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.model.Subset;
import com.wci.termhub.test.AbstractTest;
import com.wci.termhub.test.CopyConstructorTester;
import com.wci.termhub.test.EqualsHashcodeTester;
import com.wci.termhub.test.GetterSetterTester;
import com.wci.termhub.test.SerializationTester;

/**
 * Unit testing for model object Subset.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SubsetUnitTest extends AbstractTest {

    /** The logger. */
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(SubsetUnitTest.class);

    /** The model object to test. */
    private Subset object;

    /** The lm 1. */
    private Set<String> s1;

    /** The lm 2. */
    private Set<String> s2;

    /** The at 1. */
    private Map<String, String> at1;

    /** The at 2. */
    private Map<String, String> at2;

    /** The stat 1. */
    private Map<String, Integer> stat1;

    /** The stat 2. */
    private Map<String, Integer> stat2;

    /**
     * Setup.
     *
     * @throws Exception the exception
     */
    @BeforeEach
    public void setup() throws Exception {
        object = new Subset();

        // locale maps
        s1 = new HashSet<>();
        s1.add("1");
        s2 = new HashSet<>();
        s2.add("2");
        s2.add("3");

        // attribute maps
        at1 = new HashMap<>();
        at1.put("1", "1");
        at2 = new HashMap<>();
        at2.put("2", "2");
        at2.put("3", "3");

        // Stats maps
        stat1 = new HashMap<>();
        stat1.put("1", 1);
        stat2 = new HashMap<>();
        stat2.put("2", 2);
        stat2.put("3", 3);

    }

    /**
     * Test getter and setter methods of model object.
     *
     * @throws Exception the exception
     */
    @Test
    public void testModelGetSet() throws Exception {
        final GetterSetterTester tester = new GetterSetterTester(object);
        tester.proxy("disjointSubsets", 1, s1);
        tester.proxy("disjointSubsets", 2, s2);
        tester.proxy("attributes", 1, at1);
        tester.proxy("attributes", 2, at2);
        tester.proxy("statistics", 1, stat1);
        tester.proxy("statistics", 2, stat2);
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
        tester.proxy("disjointSubsets", 1, s1);
        tester.proxy("disjointSubsets", 2, s2);
        tester.proxy("attributes", 1, at1);
        tester.proxy("attributes", 2, at2);
        tester.proxy("statistics", 1, stat1);
        tester.proxy("statistics", 2, stat2);
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
        tester.proxy("disjointSubsets", 1, s1);
        tester.proxy("attributes", 1, at1);
        tester.proxy("statistics", 1, stat1);
        assertTrue(tester.testCopyConstructor(Subset.class));
    }

    /**
     * Test XML serialization.
     *
     * @throws Exception the exception
     */
    @Test
    public void testModelJsonSerialization() throws Exception {
        final SerializationTester tester = new SerializationTester(object);
        tester.proxy("disjointSubsets", 1, s1);
        tester.proxy("attributes", 1, at1);
        tester.proxy("statistics", 1, stat1);
        assertTrue(tester.testJsonSerialization());
    }

}
