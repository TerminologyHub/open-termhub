/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wci.termhub.Application;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.FamilyInfo;
import com.wci.termhub.model.LanguageInfo;
import com.wci.termhub.model.LicenseInfo;
import com.wci.termhub.model.PlanInfo;
import com.wci.termhub.model.PublisherInfo;
import com.wci.termhub.model.ResourceInfo;
import com.wci.termhub.model.TerminologyInfo;
import com.wci.termhub.model.TypeKeyValue;
import com.wci.termhub.model.User;

/**
 * Utility class for interacting with the configuration, serializing to JSON and
 * other purposes.
 */
public final class TestUtility {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(TestUtility.class);

  /**
   * Instantiates an empty {@link TestUtility}.
   *
   * @throws Exception the exception
   */
  private TestUtility() throws Exception {
    // n/a
  }

  /**
   * Returns the test user.
   *
   * @param value the id, uriLabel, or username
   * @return the test user
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public static User getMockUser(final String value) throws Exception {
    final List<String> lines = IOUtils.readLines(
        TestUtility.class.getClassLoader().getResourceAsStream("mock/user/users.txt"),
        StandardCharsets.UTF_8);
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final User candidate = ModelUtility.fromJson(line, User.class);
      if (candidate.getId().equals(value) || candidate.getUsername().equals(value)
          || candidate.getUriLabel().equals(value)) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * Prep mock user.
   *
   * @param value the value
   * @return the user
   * @throws Exception the exception
   */
  public static User prepMockUser(final String value) throws Exception {
    final User user = getMockUser(value);
    user.setUsername(UUID.randomUUID().toString() + "@example.com");
    user.setUriLabel(UUID.randomUUID().toString());
    user.setId(null);
    return user;
  }

  /**
   * Gets the mock concept.
   *
   * @param conceptName the concept name
   * @return the mock concept
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public static Concept getMockConcept(final String conceptName) throws Exception {
    final InputStream is =
        Application.class.getClassLoader().getResourceAsStream("mock/terminology/concepts.txt");
    final List<Concept> concepts = ModelUtility
        .fromJson(IOUtils.toString(is, StandardCharsets.UTF_8), new TypeReference<List<Concept>>() {
          // n/a
        });
    for (final Concept cpt : concepts) {
      if (cpt.getName().equals(conceptName)) {
        return cpt;
      }
      if (cpt.getId().equals(conceptName)) {
        return cpt;
      }
    }
    return null;
  }

  /**
   * Gets the mock license.
   *
   * @param license the license name
   * @return the mock license
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public static LicenseInfo getMockLicense(final String license) throws Exception {
    final List<String> lines = IOUtils.readLines(
        TestUtility.class.getClassLoader().getResourceAsStream("mock/config/licenseInfo.txt"),
        StandardCharsets.UTF_8);
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final LicenseInfo candidate = ModelUtility.fromJson(line, LicenseInfo.class);
      if (candidate.getName().equals(license)) {
        return candidate;
      }
      if (candidate.getType().equals(license)) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * Gets the mock family.
   *
   * @param family the family name
   * @return the mock family
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public static FamilyInfo getMockFamily(final String family) throws Exception {
    final List<String> lines = IOUtils.readLines(
        TestUtility.class.getClassLoader().getResourceAsStream("mock/config/familyInfo.txt"),
        StandardCharsets.UTF_8);
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final FamilyInfo candidate = ModelUtility.fromJson(line, FamilyInfo.class);
      if (candidate.getName().equals(family)) {
        return candidate;
      }
      if (candidate.getType().equals(family)) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * Gets the mock publisher.
   *
   * @param publisher the publisher name
   * @return the mock publisher
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public static PublisherInfo getMockPublisher(final String publisher) throws Exception {
    final List<String> lines = IOUtils.readLines(
        TestUtility.class.getClassLoader().getResourceAsStream("mock/config/publisherInfo.txt"),
        StandardCharsets.UTF_8);
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final PublisherInfo candidate = ModelUtility.fromJson(line, PublisherInfo.class);
      if (candidate.getName().equals(publisher)) {
        return candidate;
      }
      if (candidate.getType().equals(publisher)) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * Gets the mock plan.
   *
   * @param plan the plan
   * @return the mock plan
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public static PlanInfo getMockPlan(final String plan) throws Exception {
    final List<String> lines = IOUtils.readLines(
        TestUtility.class.getClassLoader().getResourceAsStream("mock/config/planInfo.txt"),
        StandardCharsets.UTF_8);
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final PlanInfo candidate = ModelUtility.fromJson(line, PlanInfo.class);
      if (candidate.getType().equals(plan)) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * Gets the mock resource.
   *
   * @param resource the resource
   * @return the mock resource
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public static ResourceInfo getMockResource(final String resource) throws Exception {
    final List<String> lines = IOUtils.readLines(
        TestUtility.class.getClassLoader().getResourceAsStream("mock/config/resourceInfo.txt"),
        StandardCharsets.UTF_8);
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final ResourceInfo candidate = ModelUtility.fromJson(line, ResourceInfo.class);
      if (candidate.getType().equals(resource)) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * Gets the mock terminology.
   *
   * @param terminology the terminology name
   * @return the mock terminology
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public static TerminologyInfo getMockTerminology(final String terminology) throws Exception {
    final List<String> lines = IOUtils.readLines(
        TestUtility.class.getClassLoader().getResourceAsStream("mock/config/terminologyInfo.txt"),
        StandardCharsets.UTF_8);
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final TerminologyInfo candidate = ModelUtility.fromJson(line, TerminologyInfo.class);
      if (candidate.getName().equals(terminology)) {
        return candidate;
      }
      if (candidate.getType().equals(terminology)) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * Gets the mock typeKeyValue.
   *
   * @param typeKeyValue the typeKeyValue name
   * @return the mock typeKeyValue
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public static TypeKeyValue getMockTypeKeyValue(final String typeKeyValue) throws Exception {
    final List<String> lines = IOUtils.readLines(
        TestUtility.class.getClassLoader().getResourceAsStream("mock/config/typeKeyValue.txt"),
        StandardCharsets.UTF_8);
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final TypeKeyValue candidate = ModelUtility.fromJson(line, TypeKeyValue.class);
      if (candidate.getValue().equals(typeKeyValue)) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * Gets the mock language.
   *
   * @param language the language name
   * @return the mock language
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public static LanguageInfo getMockLanguage(final String language) throws Exception {
    final List<String> lines = IOUtils.readLines(
        TestUtility.class.getClassLoader().getResourceAsStream("mock/config/languageInfo.txt"),
        StandardCharsets.UTF_8);
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final LanguageInfo candidate = ModelUtility.fromJson(line, LanguageInfo.class);
      if (candidate.getName().equals(language)) {
        return candidate;
      }
      if (candidate.getType().equals(language)) {
        return candidate;
      }
    }
    return null;
  }
}
