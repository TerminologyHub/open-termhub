package com.wci.termhub.rest;

import org.springframework.http.ResponseEntity;

import com.wci.termhub.model.HealthCheck;

/**
 * Cross-cutting services that should belong to all REST APIs.
 */
public interface RootServiceRest {

	/**
	 * Health.
	 *
	 * @param dependencies the dependencies
	 * @return the health check
	 * @throws Exception the exception
	 */
	public ResponseEntity<HealthCheck> health(final Boolean dependencies) throws Exception;

	/**
	 * Hook for admin tasks. STandard tasks include: reindexing, clearing caches,
	 * etc.
	 *
	 * @param task       the task
	 * @param adminKey   the admin key
	 * @param background the background
	 * @param payload    the payload
	 * @return the response
	 * @throws Exception the exception
	 */
	public ResponseEntity<?> admin(String task, String adminKey, Boolean background, String payload) throws Exception;

}
