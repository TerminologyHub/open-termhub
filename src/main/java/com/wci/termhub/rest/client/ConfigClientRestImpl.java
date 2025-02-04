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

import java.net.URLEncoder;
import java.util.List;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wci.termhub.model.ApplicationMetadata;
import com.wci.termhub.model.FamilyInfo;
import com.wci.termhub.model.LanguageInfo;
import com.wci.termhub.model.LicenseInfo;
import com.wci.termhub.model.PlanInfo;
import com.wci.termhub.model.PublisherInfo;
import com.wci.termhub.model.ResourceInfo;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.ResultListTypeKeyValue;
import com.wci.termhub.model.TerminologyInfo;
import com.wci.termhub.model.TypeKeyValue;
import com.wci.termhub.rest.RestException;
import com.wci.termhub.util.JwtUtility;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.StringUtility;
import com.wci.termhub.util.TimerCache;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;

/**
 * Client implementation for REST /config service.
 */
public class ConfigClientRestImpl extends RootClientRestImpl implements ConfigClient {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(ConfigClientRestImpl.class);

	/** The cache (holds 1000 things for up to 1 minute). */
	private static TimerCache<String> cache = new TimerCache<>(1000, 60000);

	/**
	 * Instantiates an empty {@link AccountClientRestImpl}.
	 *
	 * @throws Exception the exception
	 */
	public ConfigClientRestImpl() throws Exception {
		super("config");
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/license/" + id);
		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				// Handle "gone" 410
				if ((response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode())
						|| (response.getStatusInfo().getStatusCode() == 410)) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config license service GET = /config/license/" + id);
			}
			return ModelUtility.fromJson(result, LicenseInfo.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/license");
		try (final Response response = request(target).post(Entity.json(license.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config license service POST");
			}
			final String location = response.getHeaderString("Location");
			if (location == null) {
				ThreadContext.push("detail", result);
				throw new Exception("POST to config license service unexpectedly did not return a location");
			}
			return ModelUtility.fromJson(result, LicenseInfo.class);
		}
	}

	/**
	 * Update license info.
	 *
	 * @param id      the id
	 * @param license the license
	 * @return the license info
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	public LicenseInfo updateLicenseInfo(final String id, final LicenseInfo license) throws Exception {
		final Client client = getClients().get();
		// To support PATCH in Jersey
		final WebTarget target = client.target(getUrl() + "/config/license/" + id);
		try (final Response response = request(target).method("PATCH", Entity.json(license.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config license service PATCH = /config/license/" + license.getId());
			}
			return ModelUtility.fromJson(result, LicenseInfo.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/license/" + id);
		try (final Response response = request(target).delete();) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config license service DELETE = /config/license/" + id);
			}
		}

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
		final Client client = getClients().get();
		// Build query params
		final WebTarget target = client.target(getUrl() + "/config/license?limit=100000");

		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				if (response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config service FIND = /config/license/");
			}
			// converting to object
			return ModelUtility.fromJson(result, new TypeReference<ResultList<LicenseInfo>>() {
				// n/a
			}).getItems();

		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/family/" + id);
		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				// Handle "gone" 410
				if ((response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode())
						|| (response.getStatusInfo().getStatusCode() == 410)) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config family service GET = /config/family/" + id);
			}
			return ModelUtility.fromJson(result, FamilyInfo.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/family");
		try (final Response response = request(target).post(Entity.json(family.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config family service POST");
			}
			final String location = response.getHeaderString("Location");
			if (location == null) {
				ThreadContext.push("detail", result);
				throw new Exception("POST to config family service unexpectedly did not return a location");
			}
			return ModelUtility.fromJson(result, FamilyInfo.class);
		}
	}

	/**
	 * Update family info.
	 *
	 * @param id     the id
	 * @param family the family
	 * @return the family info
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	public FamilyInfo updateFamilyInfo(final String id, final FamilyInfo family) throws Exception {
		final Client client = getClients().get();
		// To support PATCH in Jersey
		final WebTarget target = client.target(getUrl() + "/config/family/" + id);
		try (final Response response = request(target).method("PATCH", Entity.json(family.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config family service PATCH = /config/family/" + family.getId());
			}
			return ModelUtility.fromJson(result, FamilyInfo.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/family/" + id);
		try (final Response response = request(target).delete();) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config family service DELETE = /config/family/" + id);
			}
		}

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
		final Client client = getClients().get();
		// Build query params
		final WebTarget target = client.target(getUrl() + "/config/family?limit=100000");

		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				if (response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config service FIND = /config/family/");
			}
			// converting to object
			return ModelUtility.fromJson(result, new TypeReference<ResultList<FamilyInfo>>() {
				// n/a
			}).getItems();

		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/publisher/" + id);
		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				// Handle "gone" 410
				if ((response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode())
						|| (response.getStatusInfo().getStatusCode() == 410)) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config publisher service GET = /config/publisher/" + id);
			}
			return ModelUtility.fromJson(result, PublisherInfo.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/publisher");
		try (final Response response = request(target).post(Entity.json(publisher.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config publisher service POST");
			}
			final String location = response.getHeaderString("Location");
			if (location == null) {
				ThreadContext.push("detail", result);
				throw new Exception("POST to config publisher service unexpectedly did not return a location");
			}
			return ModelUtility.fromJson(result, PublisherInfo.class);
		}
	}

	/**
	 * Update publisher info.
	 *
	 * @param id        the id
	 * @param publisher the publisher
	 * @return the publisher info
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	public PublisherInfo updatePublisherInfo(final String id, final PublisherInfo publisher) throws Exception {
		final Client client = getClients().get();
		// To support PATCH in Jersey
		final WebTarget target = client.target(getUrl() + "/config/publisher/" + id);
		try (final Response response = request(target).method("PATCH", Entity.json(publisher.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config publisher service PATCH = /config/publisher/"
								+ publisher.getId());
			}
			return ModelUtility.fromJson(result, PublisherInfo.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/publisher/" + id);
		try (final Response response = request(target).delete();) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config publisher service DELETE = /config/publisher/" + id);
			}
		}

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
		final Client client = getClients().get();
		// Build query params
		final WebTarget target = client.target(getUrl() + "/config/publisher?limit=100000");

		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				if (response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config service FIND = /config/publisher/");
			}
			// converting to object
			return ModelUtility.fromJson(result, new TypeReference<ResultList<PublisherInfo>>() {
				// n/a
			}).getItems();

		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/plan/" + id);
		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				// Handle "gone" 410
				if ((response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode())
						|| (response.getStatusInfo().getStatusCode() == 410)) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config plan service GET = /config/plan/" + id);
			}
			return ModelUtility.fromJson(result, PlanInfo.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/plan");
		try (final Response response = request(target).post(Entity.json(plan.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config plan service POST");
			}
			final String location = response.getHeaderString("Location");
			if (location == null) {
				ThreadContext.push("detail", result);
				throw new Exception("POST to config plan service unexpectedly did not return a location");
			}
			return ModelUtility.fromJson(result, PlanInfo.class);
		}
	}

	/**
	 * Update plan info.
	 *
	 * @param id   the id
	 * @param plan the plan
	 * @return the plan info
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	public PlanInfo updatePlanInfo(final String id, final PlanInfo plan) throws Exception {
		final Client client = getClients().get();
		// To support PATCH in Jersey
		final WebTarget target = client.target(getUrl() + "/config/plan/" + id);
		try (final Response response = request(target).method("PATCH", Entity.json(plan.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config plan service PATCH = /config/plan/" + plan.getId());
			}
			return ModelUtility.fromJson(result, PlanInfo.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/plan/" + id);
		try (final Response response = request(target).delete();) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config plan service DELETE = /config/plan/" + id);
			}
		}

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
		final Client client = getClients().get();
		// Build query params
		final WebTarget target = client.target(getUrl() + "/config/plan?limit=100000");

		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				if (response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config service FIND = /config/plan/");
			}
			// converting to object
			return ModelUtility.fromJson(result, new TypeReference<ResultList<PlanInfo>>() {
				// n/a
			}).getItems();

		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/resource/" + id);
		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				// Handle "gone" 410
				if ((response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode())
						|| (response.getStatusInfo().getStatusCode() == 410)) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config resource service GET = /config/resource/" + id);
			}
			return ModelUtility.fromJson(result, ResourceInfo.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/resource");
		try (final Response response = request(target).post(Entity.json(resource.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config resource service POST");
			}
			final String location = response.getHeaderString("Location");
			if (location == null) {
				ThreadContext.push("detail", result);
				throw new Exception("POST to config resource service unexpectedly did not return a location");
			}
			return ModelUtility.fromJson(result, ResourceInfo.class);
		}
	}

	/**
	 * Update resource info.
	 *
	 * @param id       the id
	 * @param resource the resource
	 * @return the resource info
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	public ResourceInfo updateResourceInfo(final String id, final ResourceInfo resource) throws Exception {
		final Client client = getClients().get();
		// To support PATCH in Jersey
		final WebTarget target = client.target(getUrl() + "/config/resource/" + id);
		try (final Response response = request(target).method("PATCH", Entity.json(resource.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config resource service PATCH = /config/resource/"
								+ resource.getId());
			}
			return ModelUtility.fromJson(result, ResourceInfo.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/resource/" + id);
		try (final Response response = request(target).delete();) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config resource service DELETE = /config/resource/" + id);
			}
		}

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
		final Client client = getClients().get();
		// Build query params
		final WebTarget target = client.target(getUrl() + "/config/resource?limit=100000");

		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				if (response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config service FIND = /config/resource/");
			}
			// converting to object
			return ModelUtility.fromJson(result, new TypeReference<ResultList<ResourceInfo>>() {
				// n/a
			}).getItems();

		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/terminology/" + id);
		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				// Handle "gone" 410
				if ((response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode())
						|| (response.getStatusInfo().getStatusCode() == 410)) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config terminology service GET = /config/terminology/" + id);
			}
			return ModelUtility.fromJson(result, TerminologyInfo.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/terminology");
		try (final Response response = request(target).post(Entity.json(terminology.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config terminology service POST");
			}
			final String location = response.getHeaderString("Location");
			if (location == null) {
				ThreadContext.push("detail", result);
				throw new Exception("POST to config terminology service unexpectedly did not return a location");
			}
			return ModelUtility.fromJson(result, TerminologyInfo.class);
		}
	}

	/**
	 * Update terminology info.
	 *
	 * @param id          the id
	 * @param terminology the terminology
	 * @return the terminology info
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	public TerminologyInfo updateTerminologyInfo(final String id, final TerminologyInfo terminology) throws Exception {
		final Client client = getClients().get();
		// To support PATCH in Jersey
		final WebTarget target = client.target(getUrl() + "/config/terminology/" + id);
		try (final Response response = request(target).method("PATCH", Entity.json(terminology.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config terminology service PATCH = /config/terminology/"
								+ terminology.getId());
			}
			return ModelUtility.fromJson(result, TerminologyInfo.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/terminology/" + id);
		try (final Response response = request(target).delete();) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config terminology service DELETE = /config/terminology/" + id);
			}
		}

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
		final Client client = getClients().get();
		// Build query params
		final WebTarget target = client.target(getUrl() + "/config/terminology?limit=100000");

		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				if (response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config service FIND = /config/terminology/");
			}
			// converting to object
			return ModelUtility.fromJson(result, new TypeReference<ResultList<TerminologyInfo>>() {
				// n/a
			}).getItems();

		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/typeKeyValue/" + id);
		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				// Handle "gone" 410
				if ((response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode())
						|| (response.getStatusInfo().getStatusCode() == 410)) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config typeKeyValue service GET = /config/typeKeyValue/" + id);
			}
			return ModelUtility.fromJson(result, TypeKeyValue.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/typeKeyValue");
		try (final Response response = request(target).post(Entity.json(typeKeyValue.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config typeKeyValue service POST");
			}
			final String location = response.getHeaderString("Location");
			if (location == null) {
				ThreadContext.push("detail", result);
				throw new Exception("POST to config typeKeyValue service unexpectedly did not return a location");
			}
			return ModelUtility.fromJson(result, TypeKeyValue.class);
		}
	}

	/**
	 * Update type key value.
	 *
	 * @param id           the id
	 * @param typeKeyValue the type key value
	 * @return the type key value
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	public TypeKeyValue updateTypeKeyValue(final String id, final TypeKeyValue typeKeyValue) throws Exception {
		final Client client = getClients().get();
		// To support PATCH in Jersey
		final WebTarget target = client.target(getUrl() + "/config/typeKeyValue/" + id);
		try (final Response response = request(target).method("PATCH", Entity.json(typeKeyValue.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config typeKeyValue service PATCH = /config/typeKeyValue/"
								+ typeKeyValue.getId());
			}
			return ModelUtility.fromJson(result, TypeKeyValue.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/typeKeyValue/" + id);
		try (final Response response = request(target).delete();) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config typeKeyValue service DELETE = /config/typeKeyValue/" + id);
			}
		}
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

		final String jwt = ThreadContext.get("jwt");
		ThreadContext.put("jwt", JwtUtility.mockAdminJwt());
		try {

			final Client client = getClients().get();
			final WebTarget target = client.target(getUrl() + "/config/enableApiKey");
			try (final Response response = request(target).post(Entity.text(apiKey))) {
				if (response == null) {
					throw new Exception("Unexpected null response");
				}
				final String result = response.readEntity(String.class);
				if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
					logger.error("  ERROR JSON = " + result);
					throw new RestException(false, response.getStatus(), "Unexpected config client error",
							"Unexpected error calling config typeKeyValue service POST = /config/enableApiKey");
				}

			}
		} finally {
			ThreadContext.put("jwt", jwt);
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
		final String jwt = ThreadContext.get("jwt");
		ThreadContext.put("jwt", JwtUtility.mockAdminJwt());
		try {
			final Client client = getClients().get();
			final WebTarget target = client.target(getUrl() + "/config/disableApiKey");
			try (final Response response = request(target).post(Entity.text(apiKey))) {
				if (response == null) {
					throw new Exception("Unexpected null response");
				}
				final String result = response.readEntity(String.class);
				if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
					logger.error("  ERROR JSON = " + result);
					throw new RestException(false, response.getStatus(), "Unexpected config client error",
							"Unexpected error calling config typeKeyValue service POST = /config/enableApiKey");
				}
			}
		} finally {
			ThreadContext.put("jwt", jwt);
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

		final String jwt = ThreadContext.get("jwt");
		ThreadContext.put("jwt", JwtUtility.mockAdminJwt());
		try {
			// Find a matching api key
			final ResultListTypeKeyValue list = findTypeKeyValue(
					"type:apiKey AND key:" + StringUtility.escapeQuery(apiKey), 0, 1, null, null);

			if (list.getItems().isEmpty()) {
				return;
			}

			// Otherwise, delete this by id
			deleteTypeKeyValue(list.getItems().get(0).getId());
		} finally {
			ThreadContext.put("jwt", jwt);
		}
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

		final String value = cache.get(apiKey);

		if (value == null) {
			final String jwt = ThreadContext.get("jwt");
			ThreadContext.put("jwt", JwtUtility.mockAdminJwt());
			// Find a matching api key
			ResultListTypeKeyValue list = null;
			try {
				list = findTypeKeyValue("type:apiKey AND key:" + StringUtility.escapeQuery(apiKey), 0, 1, null, null);
			} finally {
				ThreadContext.put("jwt", jwt);
			}

			if (list.getItems().isEmpty()) {
				return false;
			}
			cache.put(apiKey, list.getItems().get(0).getValue());
			return "true".equals(list.getItems().get(0).getValue());
		}
		return "true".equals(value);

	}

	/**
	 * Find type key value.
	 *
	 * @param query     the query
	 * @param offset    the offset
	 * @param limit     the limit
	 * @param sort      the sort
	 * @param ascending the ascending
	 * @return the result list type key value
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	public ResultListTypeKeyValue findTypeKeyValue(final String query, final Integer offset, final Integer limit,
			final String sort, final Boolean ascending) throws Exception {
		final Client client = getClients().get();
		// Build query params
		final WebTarget target = client.target(getUrl() + "/config/typeKeyValue?query="
				+ ((StringUtility.isEmpty(query)) ? "" : URLEncoder.encode(query, "UTF-8").replaceAll("\\+", "%20"))
				+ (offset == null ? "" : "&offset=" + offset) + (limit == null ? "" : "&limit=" + limit)
				+ (sort == null ? "" : "&sort=" + URLEncoder.encode(sort, "UTF-8").replaceAll("\\+", "%20"))
				+ (ascending == null ? "" : "&ascending=" + ascending));

		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				if (response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config service FIND = /config/typeKeyValue/");
			}
			// converting to object
			return ModelUtility.fromJson(result, ResultListTypeKeyValue.class);

		}
	}

	/**
	 * Find type key value.
	 *
	 * @param type the type
	 * @param key  the key
	 * @return the result list type key value
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	public ResultListTypeKeyValue findTypeKeyValue(final String type, final String key) throws Exception {
		return this.findTypeKeyValue("type:" + type + " AND key:" + key, 0, 100, null, null);
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
		final Client client = getClients().get();
		// Build query params
		final WebTarget target = client.target(getUrl() + "/config/typeKeyValue?limit=100000");

		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				if (response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config service FIND = /config/typeKeyValue/");
			}
			// converting to object
			return ModelUtility.fromJson(result, new TypeReference<ResultList<TypeKeyValue>>() {
				// n/a
			}).getItems();

		}
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
		final Client client = getClients().get();
		// Build query params
		final WebTarget target = client.target(getUrl() + "/config");

		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				if (response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config service GET = /config");
			}
			// converting to object
			return ModelUtility.fromJson(result, ApplicationMetadata.class);

		}
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
		final Client client = getClients().get();
		// Build query params
		final WebTarget target = client.target(getUrl() + "/config/language?limit=100000");

		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				if (response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config service GET = /config/language");
			}
			// converting to object
			return ModelUtility.fromJson(result, new TypeReference<ResultList<LanguageInfo>>() {
				// n/a
			}).getItems();

		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/language/" + language);
		try (final Response response = request(target).get()) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				// Handle "gone" 410
				if ((response.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode())
						|| (response.getStatusInfo().getStatusCode() == 410)) {
					return null;
				}
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config language info service GET = /config/language/" + language);
			}
			return ModelUtility.fromJson(result, LanguageInfo.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/language/" + id);
		try (final Response response = request(target).delete();) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config language service DELETE = /config/language/" + id);
			}
		}

	}

	/**
	 * Update language info.
	 *
	 * @param id       the id
	 * @param language the language
	 * @return the language info
	 * @throws Exception the exception
	 */
	/* see superclass */
	@Override
	public LanguageInfo updateLanguageInfo(final String id, final LanguageInfo language) throws Exception {
		final Client client = getClients().get();
		// To support PATCH in Jersey
		final WebTarget target = client.target(getUrl() + "/config/language/" + id);
		try (final Response response = request(target).method("PATCH", Entity.json(language.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config language service PATCH = /config/language/"
								+ language.getId());
			}
			return ModelUtility.fromJson(result, LanguageInfo.class);
		}
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
		final Client client = getClients().get();
		final WebTarget target = client.target(getUrl() + "/config/language");
		try (final Response response = request(target).post(Entity.json(language.toString()))) {
			if (response == null) {
				throw new Exception("Unexpected null response");
			}
			final String result = response.readEntity(String.class);
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				logger.error("  ERROR JSON = " + result);
				throw new RestException(false, response.getStatus(), "Unexpected config client error",
						"Unexpected error calling config language service POST");
			}
			final String location = response.getHeaderString("Location");
			if (location == null) {
				ThreadContext.push("detail", result);
				throw new Exception("POST to config language service unexpectedly did not return a location");
			}
			return ModelUtility.fromJson(result, LanguageInfo.class);
		}
	}
}
