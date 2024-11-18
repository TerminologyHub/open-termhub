/*
 * Copyright 2024 West Coast Informatics - All Rights Reserved.
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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.Term;
import com.wci.termhub.test.AbstractTest;
import com.wci.termhub.test.CopyConstructorTester;
import com.wci.termhub.test.EqualsHashcodeTester;
import com.wci.termhub.test.GetterSetterTester;
import com.wci.termhub.test.ProxyTester;
import com.wci.termhub.test.SerializationTester;

/**
 * Unit testing for model object Concept.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ConceptUnitTest extends AbstractTest {

	/** The logger. */
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ConceptUnitTest.class);

	/** The model object to test. */
	private Concept object;

	/** The t 1. */
	private List<Term> t1;

	/** The t 2. */
	private List<Term> t2;

	/**
	 * Setup.
	 *
	 * @throws Exception the exception
	 */
	@BeforeEach
	public void setup() throws Exception {
		object = new Concept();
		final ProxyTester tester = new ProxyTester(new Term());
		t1 = new ArrayList<>();
		t1.add((Term) tester.createObject(1));
		t2 = new ArrayList<>();
		t2.add((Term) tester.createObject(2));
		t2.add((Term) tester.createObject(3));

	}

	/**
	 * Test getter and setter methods of model object.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testModelGetSet() throws Exception {
		final GetterSetterTester tester = new GetterSetterTester(object);
		tester.proxy("terms", 1, t1);
		tester.proxy("terms", 2, t2);
		tester.exclude("inverseRelationships");
		tester.exclude("relationships");
		tester.exclude("treePositions");
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
		tester.proxy("terms", 1, t1);
		tester.proxy("terms", 2, t2);
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
		tester.exclude("normName");
		tester.exclude("stemName");
		tester.proxy("terms", 1, t1);
		assertTrue(tester.testCopyConstructor(Concept.class));
	}

	/**
	 * Test XML serialization.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testModelJsonSerialization() throws Exception {
		final SerializationTester tester = new SerializationTester(object);
		tester.proxy("terms", 1, t1);
		assertTrue(tester.testJsonSerialization());
	}

}
