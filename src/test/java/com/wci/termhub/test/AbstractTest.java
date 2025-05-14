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

import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.model.User;
import com.wci.termhub.util.JwtUtility;
import com.wci.termhub.util.PropertyUtility;
import com.wci.termhub.util.TestUtility;

/**
 * Abstract superclass for source code tests.
 */
public class AbstractTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(AbstractTest.class);

  /** The setup. */
  private static boolean setup = false;

  /** The test id. */
  private static String testId = null;

  /** The test jwt. */
  private static String testJwt = null;

  /** The admin id. */
  private static String adminId = null;

  /** The admin jwt. */
  private static String adminJwt = null;

  /**
   * Setup once. NOTE: Using @BeforeAll means the PropertyUtility is not yet configured.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setupOnce() throws Exception {
    if (!setup) {
      setup = true;
      PropertyUtility.getProperties();
      // Prepare JWTs for testing
      final User testUser = TestUtility.getMockUser("test");
      testId = testUser.getId();
      testJwt = JwtUtility.mockJwt(testId, "UNLIMITED", "USER");

      final User adminUser = TestUtility.getMockUser("admin");
      adminId = adminUser.getId();
      adminJwt = JwtUtility.mockJwt(adminId, "UNLIMITED", "ADMIN");

      // Start with testuser
      setTestUser();
    }
  }

  /**
   * Sets the test user.
   */
  public static void setTestUser() {
    ThreadContext.put("jwt", testJwt);
    ThreadContext.put("user-id", testId);
  }

  /**
   * Sets the admin user.
   */
  public static void setAdminUser() {
    ThreadContext.put("jwt", adminJwt);
    ThreadContext.put("user-id", adminId);
  }

  /**
   * Returns the test jwt.
   *
   * @return the test jwt
   */
  public static String getTestJwt() {
    return testJwt;
  }

  /**
   * Returns the admin jwt.
   *
   * @return the admin jwt
   */
  public static String getAdminJwt() {
    return adminJwt;
  }

}
