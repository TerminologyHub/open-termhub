/*
 *
 */
package com.wci.termhub.test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import com.wci.termhub.rest.test.TerminologyServiceRestImplUnitTest;

/**
 * The Class AllTestsSuite. Tests that are dependent on LoadUnitTest.
 */
@Suite
@SelectClasses({ LoadUnitTest.class, MetadataSearchUnitTest.class, TerminologySearchUnitTest.class,
		ConceptSearchUnitTest.class, TermSearchUnitTest.class, MultithreadedReadUnitTest.class,
		TerminologyServiceRestImplUnitTest.class, CleanupUnitTest.class })
public class AllTestsSuite {

}
