/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.test;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

import com.wci.termhub.ecl.test.EclNonSnomedTest;
import com.wci.termhub.ecl.test.EclToLuceneConverterTest;
import com.wci.termhub.rest.test.TerminologyServiceRestImplUnitTest;

/**
 * The Class AllTestsSuite. Tests that are dependent on CodeSystemLoadUnitTest.
 */
@Suite
@SuiteDisplayName("All Tests")
@IncludeEngines("junit-jupiter")
@SelectClasses(value = {

    CleanupUnitTest.class,

    CodeSystemLoadUnitTest.class,

    // Run tests that verify the data
    TerminologyCodeSystemUnitTest.class, ConceptCodeSystemUnitTest.class,
    ConceptRelationshipCodeSystemUnitTest.class,

    MetadataSearchUnitTest.class, TerminologySearchUnitTest.class, ConceptSearchUnitTest.class,
    ConceptTreePositionSearchUnitTest.class, TermSearchUnitTest.class,
    MultithreadedReadUnitTest.class,

    // Then run ECL tests that depend on concept data
    EclToLuceneConverterTest.class, EclNonSnomedTest.class,

    // Then run REST tests
    TerminologyServiceRestImplUnitTest.class,

    // Finally cleanup
    CleanupUnitTest.class
})
public class AllTestsSuite {
  // n/a
}
