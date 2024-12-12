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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wci.termhub.model.ResourceInfo;
import com.wci.termhub.test.AbstractTest;
import com.wci.termhub.test.CopyConstructorTester;
import com.wci.termhub.test.EqualsHashcodeTester;
import com.wci.termhub.test.GetterSetterTester;
import com.wci.termhub.test.SerializationTester;

/**
 * Unit test for {@link ResourceInfo}.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ResourceInfoUnitTest extends AbstractTest {

	/** The logger. */
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ResourceInfoUnitTest.class);

	/** The model object to test. */
	private ResourceInfo object;

	/**
	 * Setup.
	 *
	 * @throws Exception the exception
	 */
	@BeforeEach
	public void setup() throws Exception {
		object = new ResourceInfo();
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
		assertTrue(tester.testCopyConstructor(ResourceInfo.class));
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

//    /**
//     * Test persistence.
//     *
//     * @throws Exception the exception
//     */
//    @Test
//    public void testPersistence() throws Exception {
//        final PersistenceTester tester = new PersistenceTester(object);
//        tester.include("type");
//        tester.test();
//    }

}
