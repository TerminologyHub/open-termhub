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

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import com.wci.termhub.rest.test.TerminologyServiceRestImplUnitTest;

/**
 * The Class AllTestsSuite. Tests that are dependent on LoadUnitTest.
 */
@Suite
@SelectClasses({
    LoadUnitTest.class, MetadataSearchUnitTest.class, TerminologySearchUnitTest.class,
    ConceptSearchUnitTest.class, TermSearchUnitTest.class, MultithreadedReadUnitTest.class,
    TerminologyServiceRestImplUnitTest.class, CleanupUnitTest.class
})
public class AllTestsSuite {

}
