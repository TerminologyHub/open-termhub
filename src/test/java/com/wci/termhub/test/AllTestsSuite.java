/*
 *
 */
package com.wci.termhub.test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The Class AllTestsSuite. Tests that are dependent on LoadUnitTest.
 */
@Suite
@SelectClasses({ LoadUnitTest.class, Metadata2UnitTest.class, Terminology2UnitTest.class, Concept2UnitTest.class,
		Term2UnitTest.class, MultithreadedReadUnitTest.class })
public class AllTestsSuite {

}
