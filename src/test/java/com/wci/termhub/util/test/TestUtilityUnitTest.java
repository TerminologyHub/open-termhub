/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

//import com.wci.termhub.model.Organization;

/**
 * Unit testing for model object {@link Organization}.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TestUtilityUnitTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(TestUtilityUnitTest.class);

  // /**
  // * Test getter and setter methods of model object.
  // *
  // * @throws Exception the exception
  // */
  // @Test
  // public void testMockUser() throws Exception {
  // final User test = TestUtility.getMockUser("test");
  // assertEquals("eaa7f268-9b9f-45f4-8698-5e5e61627e2a", test.getId());
  // }
  //
  // /**
  // * Test mock org.
  // *
  // * @throws Exception the exception
  // */
  // @Test
  // public void testMockOrg() throws Exception {
  // final Organization wci =
  // TestUtility.getMockOrganization("bc6dac4d-18c3-480b-941d-8d07f26986e5");
  // assertEquals("West Coast Informatics", wci.getName());
  // }
  //
  // /**
  // * Test mock team.
  // *
  // * @throws Exception the exception
  // */
  // @Test
  // public void testMockTeam() throws Exception {
  // final Team devs =
  // TestUtility.getMockTeam("0fc68cfb-4c84-4392-b7f2-00b02ca4c44a");
  // assertEquals("wci-developers", devs.getName());
  // }

}
