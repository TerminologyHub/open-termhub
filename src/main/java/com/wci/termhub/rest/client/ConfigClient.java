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

import java.util.List;

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

/**
 * Spec for implementation client for REST config/ service.
 */
public interface ConfigClient extends RootClient {

  /**
   * Returns the license.
   *
   * @param id the id
   * @return the user
   * @throws Exception the exception
   */
  public LicenseInfo getLicenseInfo(String id) throws Exception;

  /**
   * Adds the license.
   *
   * @param license the license
   * @return the response
   * @throws Exception the exception
   */
  public LicenseInfo addLicenseInfo(LicenseInfo license) throws Exception;

  /**
   * Update license.
   *
   * @param id the id
   * @param license the license
   * @return the response
   * @throws Exception the exception
   */
  public LicenseInfo updateLicenseInfo(String id, LicenseInfo license) throws Exception;

  /**
   * Delete license.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void deleteLicenseInfo(String id) throws Exception;

  /**
   * Get all licenses.
   *
   * 
   * @return the response
   * @throws Exception the exception
   */
  public List<LicenseInfo> getAllLicenseInfo() throws Exception;

  /**
   * Returns the family.
   *
   * @param id the id
   * @return the user
   * @throws Exception the exception
   */
  public FamilyInfo getFamilyInfo(String id) throws Exception;

  /**
   * Adds the family.
   *
   * @param family the family
   * @return the response
   * @throws Exception the exception
   */
  public FamilyInfo addFamilyInfo(FamilyInfo family) throws Exception;

  /**
   * Update family.
   *
   * @param id the id
   * @param family the family
   * @return the response
   * @throws Exception the exception
   */
  public FamilyInfo updateFamilyInfo(String id, FamilyInfo family) throws Exception;

  /**
   * Delete family.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void deleteFamilyInfo(String id) throws Exception;

  /**
   * Get all families.
   *
   * 
   * @return the response
   * @throws Exception the exception
   */
  public List<FamilyInfo> getAllFamilyInfo() throws Exception;

  /**
   * Returns the publisher.
   *
   * @param id the id
   * @return the user
   * @throws Exception the exception
   */
  public PublisherInfo getPublisherInfo(String id) throws Exception;

  /**
   * Adds the publisher.
   *
   * @param publisher the publisher
   * @return the response
   * @throws Exception the exception
   */
  public PublisherInfo addPublisherInfo(PublisherInfo publisher) throws Exception;

  /**
   * Update publisher.
   *
   * @param id the id
   * @param publisher the publisher
   * @return the response
   * @throws Exception the exception
   */
  public PublisherInfo updatePublisherInfo(String id, PublisherInfo publisher) throws Exception;

  /**
   * Delete publisher.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void deletePublisherInfo(String id) throws Exception;

  /**
   * Get all publishers.
   *
   * 
   * @return the response
   * @throws Exception the exception
   */
  public List<PublisherInfo> getAllPublisherInfo() throws Exception;

  /**
   * Gets the plan info.
   *
   * @param id the id
   * @return the plan info
   * @throws Exception the exception
   */
  public PlanInfo getPlanInfo(String id) throws Exception;

  /**
   * Adds the plan info.
   *
   * @param plan the plan
   * @return the plan info
   * @throws Exception the exception
   */
  public PlanInfo addPlanInfo(PlanInfo plan) throws Exception;

  /**
   * Update plan info.
   *
   * @param id the id
   * @param plan the plan
   * @return the plan info
   * @throws Exception the exception
   */
  public PlanInfo updatePlanInfo(String id, PlanInfo plan) throws Exception;

  /**
   * Delete plan info.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void deletePlanInfo(String id) throws Exception;

  /**
   * Gets the all plan info.
   *
   * @return the all plan info
   * @throws Exception the exception
   */
  public List<PlanInfo> getAllPlanInfo() throws Exception;

  /**
   * Gets the resource info.
   *
   * @param id the id
   * @return the resource info
   * @throws Exception the exception
   */
  public ResourceInfo getResourceInfo(String id) throws Exception;

  /**
   * Adds the resource info.
   *
   * @param resource the resource
   * @return the resource info
   * @throws Exception the exception
   */
  public ResourceInfo addResourceInfo(ResourceInfo resource) throws Exception;

  /**
   * Update resource info.
   *
   * @param id the id
   * @param resource the resource
   * @return the resource info
   * @throws Exception the exception
   */
  public ResourceInfo updateResourceInfo(String id, ResourceInfo resource) throws Exception;

  /**
   * Delete resource info.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void deleteResourceInfo(String id) throws Exception;

  /**
   * Gets the all resource info.
   *
   * @return the all resource info
   * @throws Exception the exception
   */
  public List<ResourceInfo> getAllResourceInfo() throws Exception;

  /**
   * Returns the terminology.
   *
   * @param id the id
   * @return the user
   * @throws Exception the exception
   */
  public TerminologyInfo getTerminologyInfo(String id) throws Exception;

  /**
   * Adds the terminology.
   *
   * @param terminology the terminology
   * @return the response
   * @throws Exception the exception
   */
  public TerminologyInfo addTerminologyInfo(TerminologyInfo terminology) throws Exception;

  /**
   * Update terminology.
   *
   * @param id the id
   * @param terminology the terminology
   * @return the response
   * @throws Exception the exception
   */
  public TerminologyInfo updateTerminologyInfo(String id, TerminologyInfo terminology)
    throws Exception;

  /**
   * Delete terminology.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void deleteTerminologyInfo(String id) throws Exception;

  /**
   * Get all terminolgies.
   *
   * 
   * @return the response
   * @throws Exception the exception
   */
  public List<TerminologyInfo> getAllTerminologyInfo() throws Exception;

  /**
   * Returns the typeKeyValue.
   *
   * @param id the id
   * @return the user
   * @throws Exception the exception
   */
  public TypeKeyValue getTypeKeyValue(String id) throws Exception;

  /**
   * Adds the typeKeyValue.
   *
   * @param typeKeyValue the type key value
   * @return the response
   * @throws Exception the exception
   */
  public TypeKeyValue addTypeKeyValue(TypeKeyValue typeKeyValue) throws Exception;

  /**
   * Update typeKeyValue.
   *
   * @param id the id
   * @param typeKeyValue the type key value
   * @return the response
   * @throws Exception the exception
   */
  public TypeKeyValue updateTypeKeyValue(String id, TypeKeyValue typeKeyValue) throws Exception;

  /**
   * Delete typeKeyValue.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void deleteTypeKeyValue(String id) throws Exception;

  /**
   * Enable api key.
   *
   * @param apiKey the api key
   * @throws Exception the exception
   */
  public void enableApiKey(String apiKey) throws Exception;

  /**
   * Disable api key.
   *
   * @param apiKey the api key
   * @throws Exception the exception
   */
  public void disableApiKey(String apiKey) throws Exception;

  /**
   * Delete api key.
   *
   * @param apiKey the api key
   * @throws Exception the exception
   */
  public void deleteApiKey(String apiKey) throws Exception;

  /**
   * Check api key (whether it is valid or not), use a cache with a timer - 5
   * min.
   *
   * @param apiKey the api key
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean checkApiKey(String apiKey) throws Exception;

  /**
   * Get All typeKeyValues.
   *
   * 
   * @return the response
   * @throws Exception the exception
   */
  public List<TypeKeyValue> getAllTypeKeyValue() throws Exception;

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
  public ResultListTypeKeyValue findTypeKeyValue(String query, Integer offset, Integer limit,
    String sort, Boolean ascending) throws Exception;

  /**
   * Find type key value.
   *
   * @param type the type
   * @param key the key
   * @return the result list type key value
   * @throws Exception the exception
   */
  public ResultListTypeKeyValue findTypeKeyValue(String type, String key) throws Exception;

  /**
   * Get All Config Metadata.
   *
   * 
   * @return the response
   * @throws Exception the exception
   */
  public ApplicationMetadata getAllConfigMetadata() throws Exception;

  /**
   * Get given language config.
   *
   * @param language the language
   * 
   * @return the response
   * @throws Exception the exception
   */
  public LanguageInfo getLanguageInfo(String language) throws Exception;

  /**
   * Adds the language info.
   *
   * @param language the language
   * @return the language info
   * @throws Exception the exception
   */
  public LanguageInfo addLanguageInfo(LanguageInfo language) throws Exception;

  /**
   * Gets the all language info.
   *
   * @return the all language info
   * @throws Exception the exception
   */
  public List<LanguageInfo> getAllLanguageInfo() throws Exception;

  /**
   * Update language info.
   *
   * @param id the id
   * @param language the language
   * @return the language info
   * @throws Exception the exception
   */
  public LanguageInfo updateLanguageInfo(String id, LanguageInfo language) throws Exception;

  /**
   * Delete language info.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void deleteLanguageInfo(String id) throws Exception;
}
