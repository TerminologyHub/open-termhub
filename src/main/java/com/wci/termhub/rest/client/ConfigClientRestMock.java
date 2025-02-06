/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.rest.client;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.wci.termhub.model.ApplicationMetadata;
import com.wci.termhub.model.FamilyInfo;
import com.wci.termhub.model.LanguageInfo;
import com.wci.termhub.model.LicenseInfo;
import com.wci.termhub.model.PlanInfo;
import com.wci.termhub.model.PublisherInfo;
import com.wci.termhub.model.ResourceInfo;
import com.wci.termhub.model.ResultListTypeKeyValue;
import com.wci.termhub.model.TerminologyInfo;
import com.wci.termhub.model.TypeKeyValue;
import com.wci.termhub.rest.RestException;
import com.wci.termhub.util.JwtUtility;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.StringUtility;

/**
 * Client mock implementation for REST /config service.
 */
public class ConfigClientRestMock extends RootClientRestMock implements ConfigClient {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(ConfigClientRestMock.class);

  /** The family map. */
  private static Map<String, FamilyInfo> familyMap = new HashMap<>();

  /** The user map. */
  private static Map<String, TypeKeyValue> tkvMap = new HashMap<>();

  /** The license map. */
  private static Map<String, LicenseInfo> licenseMap = new HashMap<>();

  /** The language map. */
  private static Map<String, LanguageInfo> languageMap = new HashMap<>();

  /** The publisher map. */
  private static Map<String, PublisherInfo> publisherMap = new HashMap<>();

  /** The plan map. */
  private static Map<String, PlanInfo> planMap = new HashMap<>();

  /** The resource map. */
  private static Map<String, ResourceInfo> resourceMap = new HashMap<>();

  /** The terminology map. */
  private static Map<String, TerminologyInfo> terminologyMap = new HashMap<>();

  /** The metadata infos. */
  private static ApplicationMetadata appMetadata = new ApplicationMetadata();

  /**
   * Instantiates an empty {@link ConfigClientRestMock}.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public ConfigClientRestMock() throws Exception {
    super("config");

    // Read statements from resources
    List<String> lines = IOUtils.readLines(
        getClass().getClassLoader().getResourceAsStream("mock/config/typeKeyValue.txt"), "UTF-8");
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final TypeKeyValue tkv = ModelUtility.fromJson(line, TypeKeyValue.class);
      tkvMap.put(tkv.getId(), tkv);
    }

    // Read statements from resources
    lines = IOUtils.readLines(
        getClass().getClassLoader().getResourceAsStream("mock/config/licenseInfo.txt"), "UTF-8");
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final LicenseInfo li = ModelUtility.fromJson(line, LicenseInfo.class);
      licenseMap.put(li.getId(), li);
    }

    // Read statements from resources
    lines = IOUtils.readLines(
        getClass().getClassLoader().getResourceAsStream("mock/config/languageInfo.txt"), "UTF-8");
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final LanguageInfo li = ModelUtility.fromJson(line, LanguageInfo.class);
      languageMap.put(li.getId(), li);
    }

    // Read statements from resources
    lines = IOUtils.readLines(
        getClass().getClassLoader().getResourceAsStream("mock/config/publisherInfo.txt"), "UTF-8");
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final PublisherInfo pi = ModelUtility.fromJson(line, PublisherInfo.class);
      publisherMap.put(pi.getId(), pi);
    }

    // Read statements from resources
    lines = IOUtils.readLines(
        getClass().getClassLoader().getResourceAsStream("mock/config/planInfo.txt"), "UTF-8");
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final PlanInfo pi = ModelUtility.fromJson(line, PlanInfo.class);
      planMap.put(pi.getId(), pi);
    }

    // Read statements from resources
    lines = IOUtils.readLines(
        getClass().getClassLoader().getResourceAsStream("mock/config/resourceInfo.txt"), "UTF-8");
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final ResourceInfo ri = ModelUtility.fromJson(line, ResourceInfo.class);
      resourceMap.put(ri.getId(), ri);
    }

    // Read statements from resources
    lines = IOUtils.readLines(
        getClass().getClassLoader().getResourceAsStream("mock/config/terminologyInfo.txt"),
        "UTF-8");
    for (final String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      final TerminologyInfo ti = ModelUtility.fromJson(line, TerminologyInfo.class);
      terminologyMap.put(ti.getId(), ti);
    }

    appMetadata = new ApplicationMetadata();

  }

  /**
   * Admin.
   *
   * @param task the task
   * @param adminKey the admin key
   * @param payload the payload
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void admin(final String task, final String adminKey, final String payload)
    throws Exception {
    throw new UnsupportedOperationException("Mock does not support admin");
  }

  /**
   * Gets the license info.
   *
   * @param id the id
   * @return the license info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public LicenseInfo getLicenseInfo(final String id) throws Exception {
    return licenseMap.get(id);
  }

  /**
   * Adds the license info.
   *
   * @param license the license
   * @return the license info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public LicenseInfo addLicenseInfo(final LicenseInfo license) throws Exception {
    if (license == null) {
      throw new Exception("Unable to add null license");
    }
    if (license.getId() != null) {
      throw new Exception("Unable to add license that already has an id");
    }

    // Copy the object
    final String jwt = JwtUtility.getLocalJwt();

    final Constructor<?> jpaConstructor = license.getClass().getConstructor(LicenseInfo.class);
    final LicenseInfo copy = (LicenseInfo) jpaConstructor.newInstance(license);

    // POST sets the id if not already set.
    if (license.getId() == null) {
      copy.setId(UUID.randomUUID().toString());
    }

    // Meta fields
    copy.setCreated(new Date());
    copy.setModified(license.getCreated());
    copy.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));
    licenseMap.put(copy.getId(), copy);

    return copy;
  }

  /**
   * Update license info.
   *
   * @param id the id
   * @param license the license
   * @return the license info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public LicenseInfo updateLicenseInfo(final String id, final LicenseInfo license)
    throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    // Verify there is an id
    if (license.getId() == null) {
      throw new Exception("Unexpected null id for license");
    }

    final Constructor<?> jpaConstructor = license.getClass().getConstructor(LicenseInfo.class);
    final LicenseInfo copy = (LicenseInfo) jpaConstructor.newInstance(license);

    final LicenseInfo orig = licenseMap.get(license.getId());
    orig.patchFrom(copy);

    // Add a "meta" object
    if (copy.getCreated() != null) {
      orig.setCreated(copy.getCreated());
    } else {
      orig.setCreated(new Date());
    }
    orig.setModified(orig.getCreated());
    orig.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));

    // already in the map, just leave it there.
    return orig;
  }

  /**
   * Delete license info.
   *
   * @param id the id
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void deleteLicenseInfo(final String id) throws Exception {
    licenseMap.remove(id);

  }

  /**
   * Gets the all license info.
   *
   * @return the all license info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<LicenseInfo> getAllLicenseInfo() throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    if (jwt == null) {
      throw new Exception("Unexpected missing jwt, specify via ThreadLocal.put(\"jwt\",...);");
    }

    final List<LicenseInfo> al = new ArrayList<LicenseInfo>(licenseMap.values());
    return al;
  }

  /**
   * Gets the family info.
   *
   * @param id the id
   * @return the family info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public FamilyInfo getFamilyInfo(final String id) throws Exception {
    return familyMap.get(id);
  }

  /**
   * Adds the family info.
   *
   * @param family the family
   * @return the family info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public FamilyInfo addFamilyInfo(final FamilyInfo family) throws Exception {
    if (family == null) {
      throw new Exception("Unable to add null family");
    }
    if (family.getId() != null) {
      throw new Exception("Unable to add family that already has an id");
    }

    // Copy the object
    final String jwt = JwtUtility.getLocalJwt();

    final Constructor<?> jpaConstructor = family.getClass().getConstructor(FamilyInfo.class);
    final FamilyInfo copy = (FamilyInfo) jpaConstructor.newInstance(family);

    // POST sets the id if not already set.
    if (family.getId() == null) {
      copy.setId(UUID.randomUUID().toString());
    }

    // Meta fields
    copy.setCreated(new Date());
    copy.setModified(family.getCreated());
    copy.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));
    familyMap.put(copy.getId(), copy);

    return copy;
  }

  /**
   * Update family info.
   *
   * @param id the id
   * @param family the family
   * @return the family info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public FamilyInfo updateFamilyInfo(final String id, final FamilyInfo family) throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    // Verify there is an id
    if (family.getId() == null) {
      throw new Exception("Unexpected null id for family");
    }

    final Constructor<?> jpaConstructor = family.getClass().getConstructor(LicenseInfo.class);
    final FamilyInfo copy = (FamilyInfo) jpaConstructor.newInstance(family);

    final FamilyInfo orig = familyMap.get(family.getId());
    orig.patchFrom(copy);

    // Add a "meta" object
    if (copy.getCreated() != null) {
      orig.setCreated(copy.getCreated());
    } else {
      orig.setCreated(new Date());
    }
    orig.setModified(orig.getCreated());
    orig.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));

    // already in the map, just leave it there.
    return orig;
  }

  /**
   * Delete family info.
   *
   * @param id the id
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void deleteFamilyInfo(final String id) throws Exception {
    familyMap.remove(id);

  }

  /**
   * Gets the all family info.
   *
   * @return the all family info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<FamilyInfo> getAllFamilyInfo() throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    if (jwt == null) {
      throw new Exception("Unexpected missing jwt, specify via ThreadLocal.put(\"jwt\",...);");
    }
    final List<FamilyInfo> al = new ArrayList<FamilyInfo>(familyMap.values());
    return al;
  }

  /**
   * Gets the publisher info.
   *
   * @param id the id
   * @return the publisher info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public PublisherInfo getPublisherInfo(final String id) throws Exception {
    return publisherMap.get(id);
  }

  /**
   * Adds the publisher info.
   *
   * @param publisher the publisher
   * @return the publisher info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public PublisherInfo addPublisherInfo(final PublisherInfo publisher) throws Exception {
    if (publisher == null) {
      throw new Exception("Unable to add null publisher");
    }
    if (publisher.getId() != null) {
      throw new Exception("Unable to add publisher that already has an id");
    }

    // Copy the object
    final String jwt = JwtUtility.getLocalJwt();

    final Constructor<?> jpaConstructor = publisher.getClass().getConstructor(PublisherInfo.class);
    final PublisherInfo copy = (PublisherInfo) jpaConstructor.newInstance(publisher);

    // POST sets the id if not already set.
    if (publisher.getId() == null) {
      copy.setId(UUID.randomUUID().toString());
    }

    // Meta fields
    copy.setCreated(new Date());
    copy.setModified(publisher.getCreated());
    copy.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));
    publisherMap.put(copy.getId(), copy);

    return copy;
  }

  /**
   * Update publisher info.
   *
   * @param id the id
   * @param publisher the publisher
   * @return the publisher info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public PublisherInfo updatePublisherInfo(final String id, final PublisherInfo publisher)
    throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    // Verify there is an id
    if (publisher.getId() == null) {
      throw new Exception("Unexpected null id for publisher");
    }

    final Constructor<?> jpaConstructor = publisher.getClass().getConstructor(LicenseInfo.class);
    final PublisherInfo copy = (PublisherInfo) jpaConstructor.newInstance(publisher);

    final PublisherInfo orig = publisherMap.get(publisher.getId());
    orig.patchFrom(copy);

    // Add a "meta" object
    if (copy.getCreated() != null) {
      orig.setCreated(copy.getCreated());
    } else {
      orig.setCreated(new Date());
    }
    orig.setModified(orig.getCreated());
    orig.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));

    // already in the map, just leave it there.
    return orig;
  }

  /**
   * Delete publisher info.
   *
   * @param id the id
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void deletePublisherInfo(final String id) throws Exception {
    publisherMap.remove(id);

  }

  /**
   * Gets the all publisher info.
   *
   * @return the all publisher info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<PublisherInfo> getAllPublisherInfo() throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    if (jwt == null) {
      throw new Exception("Unexpected missing jwt, specify via ThreadLocal.put(\"jwt\",...);");
    }

    final List<PublisherInfo> al = new ArrayList<PublisherInfo>(publisherMap.values());
    return al;
  }

  /**
   * Gets the plan info.
   *
   * @param id the id
   * @return the plan info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public PlanInfo getPlanInfo(final String id) throws Exception {
    return planMap.get(id);
  }

  /**
   * Adds the plan info.
   *
   * @param plan the plan
   * @return the plan info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public PlanInfo addPlanInfo(final PlanInfo plan) throws Exception {
    if (plan == null) {
      throw new Exception("Unable to add null plan");
    }
    if (plan.getId() != null) {
      throw new Exception("Unable to add plan that already has an id");
    }

    // Copy the object
    final String jwt = JwtUtility.getLocalJwt();

    final Constructor<?> jpaConstructor = plan.getClass().getConstructor(PlanInfo.class);
    final PlanInfo copy = (PlanInfo) jpaConstructor.newInstance(plan);

    // POST sets the id if not already set.
    if (plan.getId() == null) {
      copy.setId(UUID.randomUUID().toString());
    }

    // Meta fields
    copy.setCreated(new Date());
    copy.setModified(plan.getCreated());
    copy.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));
    planMap.put(copy.getId(), copy);

    return copy;
  }

  /**
   * Update plan info.
   *
   * @param id the id
   * @param plan the plan
   * @return the plan info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public PlanInfo updatePlanInfo(final String id, final PlanInfo plan) throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    // Verify there is an id
    if (plan.getId() == null) {
      throw new Exception("Unexpected null id for plan");
    }

    final Constructor<?> jpaConstructor = plan.getClass().getConstructor(LicenseInfo.class);
    final PlanInfo copy = (PlanInfo) jpaConstructor.newInstance(plan);

    final PlanInfo orig = planMap.get(plan.getId());
    orig.patchFrom(copy);

    // Add a "meta" object
    if (copy.getCreated() != null) {
      orig.setCreated(copy.getCreated());
    } else {
      orig.setCreated(new Date());
    }
    orig.setModified(orig.getCreated());
    orig.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));

    // already in the map, just leave it there.
    return orig;
  }

  /**
   * Delete plan info.
   *
   * @param id the id
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void deletePlanInfo(final String id) throws Exception {
    planMap.remove(id);

  }

  /**
   * Gets the all plan info.
   *
   * @return the all plan info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<PlanInfo> getAllPlanInfo() throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    if (jwt == null) {
      throw new Exception("Unexpected missing jwt, specify via ThreadLocal.put(\"jwt\",...);");
    }

    final List<PlanInfo> al = new ArrayList<PlanInfo>(planMap.values());
    return al;
  }

  /**
   * Gets the resource info.
   *
   * @param id the id
   * @return the resource info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ResourceInfo getResourceInfo(final String id) throws Exception {
    return resourceMap.get(id);
  }

  /**
   * Adds the resource info.
   *
   * @param resource the resource
   * @return the resource info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ResourceInfo addResourceInfo(final ResourceInfo resource) throws Exception {
    if (resource == null) {
      throw new Exception("Unable to add null resource");
    }
    if (resource.getId() != null) {
      throw new Exception("Unable to add resource that already has an id");
    }

    // Copy the object
    final String jwt = JwtUtility.getLocalJwt();

    final Constructor<?> jpaConstructor = resource.getClass().getConstructor(ResourceInfo.class);
    final ResourceInfo copy = (ResourceInfo) jpaConstructor.newInstance(resource);

    // POST sets the id if not already set.
    if (resource.getId() == null) {
      copy.setId(UUID.randomUUID().toString());
    }

    // Meta fields
    copy.setCreated(new Date());
    copy.setModified(resource.getCreated());
    copy.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));
    resourceMap.put(copy.getId(), copy);

    return copy;
  }

  /**
   * Update resource info.
   *
   * @param id the id
   * @param resource the resource
   * @return the resource info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ResourceInfo updateResourceInfo(final String id, final ResourceInfo resource)
    throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    // Verify there is an id
    if (resource.getId() == null) {
      throw new Exception("Unexpected null id for resource");
    }

    final Constructor<?> jpaConstructor = resource.getClass().getConstructor(LicenseInfo.class);
    final ResourceInfo copy = (ResourceInfo) jpaConstructor.newInstance(resource);

    final ResourceInfo orig = resourceMap.get(resource.getId());
    orig.patchFrom(copy);

    // Add a "meta" object
    if (copy.getCreated() != null) {
      orig.setCreated(copy.getCreated());
    } else {
      orig.setCreated(new Date());
    }
    orig.setModified(orig.getCreated());
    orig.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));

    // already in the map, just leave it there.
    return orig;
  }

  /**
   * Delete resource info.
   *
   * @param id the id
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void deleteResourceInfo(final String id) throws Exception {
    resourceMap.remove(id);

  }

  /**
   * Gets the all resource info.
   *
   * @return the all resource info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<ResourceInfo> getAllResourceInfo() throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    if (jwt == null) {
      throw new Exception("Unexpected missing jwt, specify via ThreadLocal.put(\"jwt\",...);");
    }

    final List<ResourceInfo> al = new ArrayList<ResourceInfo>(resourceMap.values());
    return al;
  }

  /**
   * Gets the terminology info.
   *
   * @param id the id
   * @return the terminology info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public TerminologyInfo getTerminologyInfo(final String id) throws Exception {
    return terminologyMap.get(id);
  }

  /**
   * Adds the terminology info.
   *
   * @param terminology the terminology
   * @return the terminology info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public TerminologyInfo addTerminologyInfo(final TerminologyInfo terminology) throws Exception {
    if (terminology == null) {
      throw new Exception("Unable to add null terminology");
    }
    if (terminology.getId() != null) {
      throw new Exception("Unable to add terminology that already has an id");
    }

    // Copy the object
    final String jwt = JwtUtility.getLocalJwt();

    final Constructor<?> jpaConstructor =
        terminology.getClass().getConstructor(TerminologyInfo.class);
    final TerminologyInfo copy = (TerminologyInfo) jpaConstructor.newInstance(terminology);

    // POST sets the id if not already set.
    if (terminology.getId() == null) {
      copy.setId(UUID.randomUUID().toString());
    }

    // Meta fields
    copy.setCreated(new Date());
    copy.setModified(terminology.getCreated());
    copy.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));
    terminologyMap.put(copy.getId(), copy);

    return copy;
  }

  /**
   * Update terminology info.
   *
   * @param id the id
   * @param terminology the terminology
   * @return the terminology info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public TerminologyInfo updateTerminologyInfo(final String id, final TerminologyInfo terminology)
    throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    // Verify there is an id
    if (terminology.getId() == null) {
      throw new Exception("Unexpected null id for terminology");
    }

    final Constructor<?> jpaConstructor = terminology.getClass().getConstructor(LicenseInfo.class);
    final TerminologyInfo copy = (TerminologyInfo) jpaConstructor.newInstance(terminology);

    final TerminologyInfo orig = terminologyMap.get(terminology.getId());
    orig.patchFrom(copy);

    // Add a "meta" object
    if (copy.getCreated() != null) {
      orig.setCreated(copy.getCreated());
    } else {
      orig.setCreated(new Date());
    }
    orig.setModified(orig.getCreated());
    orig.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));

    // already in the map, just leave it there.
    return orig;
  }

  /**
   * Delete terminology info.
   *
   * @param id the id
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void deleteTerminologyInfo(final String id) throws Exception {
    terminologyMap.remove(id);

  }

  /**
   * Gets the all terminology info.
   *
   * @return the all terminology info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<TerminologyInfo> getAllTerminologyInfo() throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    if (jwt == null) {
      throw new Exception("Unexpected missing jwt, specify via ThreadLocal.put(\"jwt\",...);");
    }

    final List<TerminologyInfo> al = new ArrayList<TerminologyInfo>(terminologyMap.values());
    return al;
  }

  /**
   * Gets the type key value.
   *
   * @param id the id
   * @return the type key value
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public TypeKeyValue getTypeKeyValue(final String id) throws Exception {
    return tkvMap.get(id);
  }

  /**
   * Adds the type key value.
   *
   * @param typeKeyValue the type key value
   * @return the type key value
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public TypeKeyValue addTypeKeyValue(final TypeKeyValue typeKeyValue) throws Exception {
    if (typeKeyValue == null) {
      throw new Exception("Unable to add null typeKeyValue");
    }
    if (typeKeyValue.getId() != null) {
      throw new Exception("Unable to add typeKeyValue that already has an id");
    }

    // Copy the object
    final String jwt = JwtUtility.getLocalJwt();

    final Constructor<?> jpaConstructor =
        typeKeyValue.getClass().getConstructor(TypeKeyValue.class);
    final TypeKeyValue copy = (TypeKeyValue) jpaConstructor.newInstance(typeKeyValue);

    // POST sets the id if not already set.
    if (typeKeyValue.getId() == null) {
      copy.setId(UUID.randomUUID().toString());
    }

    // Meta fields
    copy.setCreated(new Date());
    copy.setModified(typeKeyValue.getCreated());
    copy.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));
    tkvMap.put(copy.getId(), copy);

    return copy;
  }

  /**
   * Update type key value.
   *
   * @param id the id
   * @param typeKeyValue the type key value
   * @return the type key value
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public TypeKeyValue updateTypeKeyValue(final String id, final TypeKeyValue typeKeyValue)
    throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    // Verify there is an id
    if (typeKeyValue.getId() == null) {
      throw new Exception("Unexpected null id for typeKeyValue");
    }

    final Constructor<?> jpaConstructor = typeKeyValue.getClass().getConstructor(LicenseInfo.class);
    final TypeKeyValue copy = (TypeKeyValue) jpaConstructor.newInstance(typeKeyValue);

    final TypeKeyValue orig = tkvMap.get(typeKeyValue.getId());
    orig.patchFrom(copy);

    // Add a "meta" object
    if (copy.getCreated() != null) {
      orig.setCreated(copy.getCreated());
    } else {
      orig.setCreated(new Date());
    }
    orig.setModified(orig.getCreated());
    orig.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));

    // already in the map, just leave it there.
    return orig;
  }

  /**
   * Delete type key value.
   *
   * @param id the id
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void deleteTypeKeyValue(final String id) throws Exception {
    tkvMap.remove(id);

  }

  /**
   * Enable api key.
   *
   * @param apiKey the api key
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void enableApiKey(final String apiKey) throws Exception {
    TypeKeyValue tkv = tkvMap.values().stream()
        .filter(t -> "apiKey".equals(t.getType()) && t.getKey().equals(apiKey)).findFirst()
        .orElse(null);
    if (tkv == null) {
      tkv = new TypeKeyValue("apiKey", apiKey, "true");
      tkv.setId(UUID.randomUUID().toString());
      tkvMap.put(tkv.getId(), tkv);
    } else {
      tkv.setValue("true");
    }
  }

  /**
   * Disable api key.
   *
   * @param apiKey the api key
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void disableApiKey(final String apiKey) throws Exception {
    final TypeKeyValue tkv = tkvMap.values().stream()
        .filter(t -> "apiKey".equals(t.getType()) && t.getKey().equals(apiKey)).findFirst()
        .orElse(null);
    if (tkv != null) {
      tkv.setValue("false");
    } else {
      throw new RestException(false, 404, "Not found", "Unable to find typeKeyValue for " + apiKey);
    }
  }

  /**
   * Delete api key.
   *
   * @param apiKey the api key
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void deleteApiKey(final String apiKey) throws Exception {

    // Find a matching api key
    final ResultListTypeKeyValue list = findTypeKeyValue(
        "type:apiKey AND key:" + StringUtility.escapeQuery(apiKey), 0, 1, null, null);

    if (list.getItems().isEmpty()) {
      return;
    }

    // Otherwise, delete this by id
    deleteTypeKeyValue(list.getItems().get(0).getId());

  }

  /**
   * Check api key.
   *
   * @param apiKey the api key
   * @return true, if successful
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public boolean checkApiKey(final String apiKey) throws Exception {
    // Find the tkv
    final TypeKeyValue tkv = tkvMap.values().stream()
        .filter(t -> "apiKey".equals(t.getType()) && t.getKey().equals(apiKey)).findFirst()
        .orElse(null);
    // if true, return true
    if (tkv != null) {
      return "true".equals(tkv.getValue());
    }
    // return false otherwise
    return false;
  }

  /**
   * Gets the all type key value.
   *
   * @return the all type key value
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<TypeKeyValue> getAllTypeKeyValue() throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    if (jwt == null) {
      throw new Exception("Unexpected missing jwt, specify via ThreadLocal.put(\"jwt\",...);");
    }

    final List<TypeKeyValue> al = new ArrayList<TypeKeyValue>(tkvMap.values());
    return al;
  }

  /**
   * Gets the all config metadata.
   *
   * @return the all config metadata
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ApplicationMetadata getAllConfigMetadata() throws Exception {
    return appMetadata;
  }

  /**
   * Gets the all language info.
   *
   * @return the all language info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<LanguageInfo> getAllLanguageInfo() throws Exception {
    final List<LanguageInfo> al = new ArrayList<LanguageInfo>(languageMap.values());
    return al;
  }

  /**
   * Gets the language info.
   *
   * @param language the language
   * @return the language info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public LanguageInfo getLanguageInfo(final String language) throws Exception {
    for (final LanguageInfo la : appMetadata.getLanguage()) {
      if (la.getName().toLowerCase().contains(language.toLowerCase())) {
        return la;
      }
    }
    return null;
  }

  /**
   * Adds the language info.
   *
   * @param language the language
   * @return the language info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public LanguageInfo addLanguageInfo(final LanguageInfo language) throws Exception {
    if (language == null) {
      throw new Exception("Unable to add null language");
    }
    if (language.getId() != null) {
      throw new Exception("Unable to add language that already has an id");
    }

    // Copy the object
    final String jwt = JwtUtility.getLocalJwt();

    final Constructor<?> jpaConstructor = language.getClass().getConstructor(LanguageInfo.class);
    final LanguageInfo copy = (LanguageInfo) jpaConstructor.newInstance(language);

    // POST sets the id if not already set.
    if (language.getId() == null) {
      copy.setId(UUID.randomUUID().toString());
    }

    // Meta fields
    copy.setCreated(new Date());
    copy.setModified(language.getCreated());
    copy.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));
    languageMap.put(copy.getId(), copy);

    return copy;
  }

  /**
   * Delete language info.
   *
   * @param id the id
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void deleteLanguageInfo(final String id) throws Exception {
    languageMap.remove(id);

  }

  /**
   * Update language info.
   *
   * @param id the id
   * @param language the language
   * @return the language info
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public LanguageInfo updateLanguageInfo(final String id, final LanguageInfo language)
    throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    // Verify there is an id
    if (language.getId() == null) {
      throw new Exception("Unexpected null id for language");
    }

    final Constructor<?> jpaConstructor = language.getClass().getConstructor(LanguageInfo.class);
    final LanguageInfo copy = (LanguageInfo) jpaConstructor.newInstance(language);

    final LanguageInfo orig = languageMap.get(language.getId());
    orig.patchFrom(copy);

    // Add a "meta" object
    if (copy.getCreated() != null) {
      orig.setCreated(copy.getCreated());
    } else {
      orig.setCreated(new Date());
    }
    orig.setModified(orig.getCreated());
    orig.setModifiedBy(JwtUtility.getUserId(JWT.decode(jwt).getClaims()));

    // already in the map, just leave it there.
    return orig;
  }

  /**
   * Find type key value.
   *
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param sort the sort
   * @param ascending the ascending
   * @return the result list type key value
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ResultListTypeKeyValue findTypeKeyValue(final String query, final Integer offset,
    final Integer limit, final String sort, final Boolean ascending) throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    if (jwt == null) {
      throw new Exception("Unexpected missing jwt, specify via ThreadLocal.put(\"jwt\",...);");
    }

    final ResultListTypeKeyValue list = new ResultListTypeKeyValue();
    final String type = query.substring(query.indexOf(":") + 1, query.indexOf(" AND"));
    final String key = query.substring(query.lastIndexOf(":") + 1);
    for (final TypeKeyValue tkv : tkvMap.values()) {
      if (tkv.getKey().equals(key) && tkv.getType().equals(type)) {
        final List<TypeKeyValue> resultList = new ArrayList<>();
        resultList.add(tkv);
        list.setItems(resultList);
      }
    }
    list.setTotal(list.getItems().size());
    return list;
  }

  /**
   * Find type key value.
   *
   * @param type the type
   * @param key the key
   * @return the result list type key value
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ResultListTypeKeyValue findTypeKeyValue(final String type, final String key)
    throws Exception {
    final String jwt = JwtUtility.getLocalJwt();
    if (jwt == null) {
      throw new Exception("Unexpected missing jwt, specify via ThreadLocal.put(\"jwt\",...);");
    }

    final ResultListTypeKeyValue list = new ResultListTypeKeyValue();
    for (final TypeKeyValue tkv : tkvMap.values()) {
      if (tkv.getKey().equals(key) && tkv.getType().equals(type)) {
        final List<TypeKeyValue> resultList = new ArrayList<>();
        resultList.add(tkv);
        list.setItems(resultList);
      }
    }
    list.setTotal(list.getItems().size());
    return list;
  }
}
