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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.model.Terminology;
import com.wci.termhub.test.AbstractTest;
import com.wci.termhub.test.CopyConstructorTester;
import com.wci.termhub.test.EqualsHashcodeTester;
import com.wci.termhub.test.GetterSetterTester;
import com.wci.termhub.test.SerializationTester;
import com.wci.termhub.util.ModelUtility;

/**
 * Unit test for {@link Terminology}.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TerminologyUnitTest extends AbstractTest {

	/** The logger. */
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(TerminologyUnitTest.class);

	/** The model object to test. */
	private Terminology object;

	/** The m 1. */
	private Map<String, String> m1;

	/** The m 2. */
	private Map<String, String> m2;

	/** The at 1. */
	private Map<String, Integer> sm1;

	/** The at 2. */
	private Map<String, Integer> sm2;

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
		object = new Terminology();

		// statistics maps
		sm1 = new HashMap<>();
		sm1.put("1", 1);
		sm2 = new HashMap<>();
		sm2.put("2", 2);
		sm2.put("3", 3);

		// attributes maps
		m1 = new HashMap<>();
		m1.put("1", "1");
		m2 = new HashMap<>();
		m2.put("2", "2");
		m2.put("3", "3");

		// roots
		l1 = new ArrayList<>();
		l1.add("1");
		l2 = new ArrayList<>();
		l2.add("2");
		l2.add("3");
	}

	/**
	 * Test getter and setter methods of model object.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testModelGetSet() throws Exception {
		final GetterSetterTester tester = new GetterSetterTester(object);
		tester.proxy("attributes", 1, m1);
		tester.proxy("attributes", 2, m2);
		tester.proxy("statistics", 1, sm1);
		tester.proxy("statistics", 2, sm2);
		tester.proxy(List.class, 1, l1);
		tester.proxy(List.class, 2, l2);
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
		// same as TerminologyRef
		tester.include("id");
		tester.include("abbreviation");
		tester.include("name");
		tester.include("version");
		tester.include("latest");
		tester.include("loaded");
		tester.include("publisher");

		tester.proxy("attributes", 1, m1);
		tester.proxy("attributes", 2, m2);
		tester.proxy("statistics", 1, sm1);
		tester.proxy("statistics", 2, sm2);
		tester.proxy(List.class, 1, l1);
		tester.proxy(List.class, 2, l2);

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
		tester.proxy("attributes", 1, m1);
		tester.proxy("statistics", 1, sm1);
		tester.proxy(List.class, 1, l1);
		assertTrue(tester.testCopyConstructor(Terminology.class));
	}

	/**
	 * Test model serialization.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testModelSerialization() throws Exception {
		final SerializationTester tester = new SerializationTester(object);
		tester.proxy("attributes", 1, m1);
		tester.proxy("statistics", 1, sm1);
		tester.proxy(List.class, 1, l1);
		assertTrue(tester.testJsonSerialization());
	}

//    /**
//     * Test persistence.
//     *
//     * @throws Exception the exception
//     */
//    @Test
//    public void testPersistence() throws Exception {
//        final PersistenceTester tester = new PersistenceTester(object);
//        tester.test();
//    }
//
//    /**
//     * Test persistence 2.
//     *
//     * @throws Exception the exception
//     */
//    @Test
//    public void testPersistence2() throws Exception {
//        try (final RootService service = new RootServiceImpl()) {
//            service.setModifiedBy("test");
//
//            // Add a terminology
//            final Terminology t1 = new Terminology();
//            t1.setAbbreviation("test");
//            t1.setName("test");
//            t1.setVersion("latest");
//            t1.getAttributes().put(Attributes.EXPRESSION_ENABLED.property(), "true");
//            t1.getAttributes().put(Attributes.MAX_ID.property(), "1001");
//            service.add(t1);
//
//            final Terminology t2 = new Terminology();
//            t2.setAbbreviation("test2");
//            t2.setName("test2");
//            t2.setVersion("latest2");
//            t2.getAttributes().put("max", "1001");
//            service.add(t2);
//
//            // Cleanup
//            service.remove(t1);
//            service.remove(t2);
//
//        }
//
//    }

	/**
	 * Test terminology sort.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testTerminologySort() throws Exception {
		final Terminology t1 = new Terminology();
		t1.setAbbreviation("A");
		t1.setVersion("20230101");
		final Terminology t2 = new Terminology();
		t2.setAbbreviation("A");
		t2.setVersion("20230201");
		final Terminology t3 = new Terminology();
		t3.setAbbreviation("RXNORM");
		t3.setVersion("01012023");
		final Terminology t4 = new Terminology();
		t4.setAbbreviation("RXNORM");
		t4.setVersion("02012023");
		final Terminology t5 = new Terminology();
		t5.setAbbreviation("Z");
		t5.setVersion("20230101");

		final List<Terminology> list = ModelUtility.asList(t2, t5, t4, t1, t3).stream().sorted()
				.collect(Collectors.toList());
		assertEquals(list.toString(), ModelUtility.asList(t1, t2, t3, t4, t5).toString());
	}

	/**
	 * Test terminology sort with null versions.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testTerminologySortWithNullVersions() throws Exception {
		final Terminology t1 = new Terminology();
		t1.setAbbreviation("A");
		t1.setVersion("20230101");
		final Terminology t2 = new Terminology();
		t2.setAbbreviation("A");
		// t2.setVersion("20230201");
		final Terminology t3 = new Terminology();
		t3.setAbbreviation("RXNORM");
		t3.setVersion("01012023");
		final Terminology t4 = new Terminology();
		t4.setAbbreviation("RXNORM");
		// t4.setVersion("02012023");
		final Terminology t5 = new Terminology();
		t5.setAbbreviation("Z");
		t5.setVersion("20230101");

		final List<Terminology> list = ModelUtility.asList(t2, t5, t3, t1, t4).stream().sorted()
				.collect(Collectors.toList());
		assertEquals(list.toString(), ModelUtility.asList(t1, t2, t3, t4, t5).toString());
	}

}
