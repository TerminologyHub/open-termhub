/*
 * Copyright 2024 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.service;

import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.wci.termhub.model.AuthContext;
import com.wci.termhub.rest.ExceptionHandler;
import com.wci.termhub.rest.RestException;
import com.wci.termhub.util.Claims;
import com.wci.termhub.util.JwtUtility;
import com.wci.termhub.util.LocalException;
import com.wci.termhub.util.PropertyUtility;
import com.wci.termhub.util.StringUtility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;

/**
 * Top level class for all REST services.
 */
public class RootServiceRestImpl {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(RootServiceRestImpl.class);

	/** The in progress. */
	private static boolean adminInProgress = false;

	/**
	 * Instantiates an empty {@link RootServiceRestImpl}.
	 *
	 * @throws Exception the exception
	 */
	public RootServiceRestImpl() throws Exception {
		// n/a
	}

	/**
	 * Returns the jwt.
	 *
	 * @param request the request
	 * @return the jwt
	 * @throws Exception the exception
	 */
	public String getJwt(final HttpServletRequest request) throws Exception {
		// Extract header token - verify against the URL
		String jwt = request.getHeader("X-Termhub-Token");
		final String apiUrl = PropertyUtility.getProperties().getProperty("api.url");
		if (!StringUtility.isEmpty(jwt)) {
			return jwt;
			// Allow X-termhub-token except for external call
			// if (request.getRequestURL().toString()
			// .contains(apiUrl.substring(apiUrl.indexOf("://") + 3))
			// && !request.getRequestURL().toString().startsWith("http://localhost")) {
			// logger.warn("Unexpected X-Termhub-Token from non-internal URL = "
			// + request.getRequestURL());
			// throw new Exception("Unexpected X-Termhub-Token from
			// non-internal URL = "
			// + request.getRequestURL());
			// }
		} else {
			jwt = request.getHeader("Authorization");
			if (!StringUtility.isEmpty(jwt) && jwt.startsWith("Bearer ")) {

				if (!request.getRequestURL().toString().contains(apiUrl.substring(apiUrl.indexOf("://") + 3))
						&& !request.getRequestURL().toString().startsWith("http://localhost")) {
					logger.warn("BAD request url = " + request.getRequestURL());
					logger.warn("    api url = " + PropertyUtility.getProperties().getProperty("api.url"));
					// throw new Exception(
					// "Unexpected Authorization Bearer Token from non-external
					// URL");
				}
				jwt = jwt.substring(jwt.indexOf(" ") + 1);
			}

			else {
				// Localhost access not allowed here
//				if (request.getRequestURL().toString().startsWith("http://localhost")) {
//					throw new Exception("Missing JWT header for localhost");
//				}

				// Support local interactions in guest mode - mock a JWT.
				jwt = "guest";
				if (PropertyUtility.isTestMode() && jwt.equals("guest")) {
					jwt = JwtUtility.mockJwt(UUID.randomUUID().toString());
				} else {
					return null;
				}
			}
		}
		return jwt;
	}

	/**
	 * Authorize admin.
	 *
	 * @param request the request
	 * @return the auth context
	 * @throws Exception the exception
	 */
	public AuthContext authorizeAdmin(final HttpServletRequest request) throws Exception {
		final AuthContext context = authorizeHelper(request);
		if (!"ADMIN".equals(context.getRole())) {
			throw new RestException(false, 403, "Forbidden", context.getRole());
		}
		return context;
	}

	/**
	 * Authorize.
	 *
	 * @param request the request
	 * @return the string
	 * @throws Exception the exception
	 */
	public AuthContext authorize(final HttpServletRequest request) throws Exception {
		final AuthContext context = authorizeHelper(request);
		// Only allow ADMIN and USER (or null, which defaults to USER)
		if (!"ADMIN".equals(context.getRole()) && context.getRole() != null && !"USER".equals(context.getRole())) {
			throw new RestException(false, 403, "Forbidden", context.getRole());
		}
		return context;
	}

	/**
	 * Authorize project.
	 *
	 * @param request the request
	 * @return the auth context
	 * @throws Exception the exception
	 */
	public AuthContext authorizeProject(final HttpServletRequest request) throws Exception {
		final AuthContext context = authorizeHelper(request);

		// Only allow ADMIN, USER, PROJECT role
		if (!"ADMIN".equals(context.getRole()) && context.getRole() != null && !"USER".equals(context.getRole())
				&& !"PROJECT".equals(context.getRole())) {
			throw new RestException(false, 403, "Forbidden", context.getRole());
		}
		return context;
	}

	/**
	 * Authorize refresh. Decode the JWT (check expiration). Verify the role or
	 * throw 401.
	 *
	 * @param refreshToken the refresh token
	 * @return the string
	 * @throws Exception the exception
	 */
	public String authorizeRefresh(final String refreshToken) throws Exception {
		try {
			final DecodedJWT djwt = JWT.decode(refreshToken);
			JwtUtility.verify(djwt);
			final Map<String, Claim> claims = djwt.getClaims();
			if (!JwtUtility.getRole(claims).equals("REFRESH")) {
				throw new RestException(false, 403, "Forbidden", JwtUtility.getRole(claims));
			}
			return JwtUtility.getUserId(claims);
		} catch (final Exception e) {
			throw new RestException(false, 401, "Unauthorized", null);
		}
	}

	/**
	 * Authorize account.
	 *
	 * @param accountToken the account token
	 * @return the string
	 * @throws Exception the exception
	 */
	public String authorizeAccount(final String accountToken) throws Exception {
		try {
			final DecodedJWT djwt = JWT.decode(accountToken);
			JwtUtility.verify(djwt);
			final Map<String, Claim> claims = djwt.getClaims();
			if (!JwtUtility.getRole(claims).equals("ACCOUNT")) {
				throw new RestException(false, 403, "Forbidden", JwtUtility.getRole(claims));
			}
			return JwtUtility.getUserId(claims);
		} catch (final Exception e) {
			throw new RestException(false, 401, "Unauthorized", null);
		}

	}

	/**
	 * Noauth.
	 *
	 * @param request the request
	 * @return the auth context
	 * @throws Exception the exception
	 */
	public AuthContext noauth(final HttpServletRequest request) throws Exception {
		final AuthContext context = new AuthContext();
		// final String jwt = JwtUtility.mockAdminJwt();
		// context.setJwt(jwt);

		// Setup thread context
		// ThreadContext.put("jwt", jwt);
		ThreadContext.put("http-version", request.getProtocol());
		// Nginx configured to send X-Real-IP as the real IP
		// ThreadContext.put("remote-address", request.getRemoteAddr());
		ThreadContext.put("remote-address", request.getHeader("X-Real-IP"));
		ThreadContext.put("correlation-id", request.getHeader("X-Correlation-ID"));
		ThreadContext.put("req-uri", request.getRequestURI());
		ThreadContext.put("req-method", request.getMethod());
		ThreadContext.put("req-querystring", getQueryString(request.getParameterMap()));
		context.setCorrelationId(request.getHeader("X-Correlation-ID"));
		context.setSkipHeader("true".equals(request.getHeader("X-Termhub-Skip")));

		// final DecodedJWT djwt = JWT.decode(jwt);
		// NO need to verify, we created this token
		// final Map<String, Claim> claims = djwt.getClaims();
		// context.setClaims(JwtUtility.getPayload(djwt));
		// context.setUserId(JwtUtility.getUserId(claims));
		// context.setOrganizationId(JwtUtility.getOrganizationId(claims));
		// context.setRole(JwtUtility.getRole(claims));
		return context;
	}

	/**
	 * Authorize helper.
	 *
	 * @param request the request
	 * @return the auth context
	 * @throws Exception the exception
	 */
	public AuthContext authorizeHelper(final HttpServletRequest request) throws Exception {
		adminInProgressCheck();

		final AuthContext context = new AuthContext();
		// Set logging ThreadContext stuff
		ThreadContext.put("http-version", request.getProtocol());
		ThreadContext.put("remote-address", request.getRemoteAddr());
		ThreadContext.put("correlation-id", request.getHeader("X-Correlation-ID"));
		ThreadContext.put("req-uri", request.getRequestURI());
		ThreadContext.put("req-method", request.getMethod());
		ThreadContext.put("req-querystring", getQueryString(request.getParameterMap()));
		ThreadContext.put("referer", request.getHeader("Referer"));
		ThreadContext.put("user-agent", request.getHeader("User-Agent"));
		context.setCorrelationId(request.getHeader("X-Correlation-ID"));

		// Skip header
		context.setSkipHeader("true".equals(request.getHeader("X-Termhub-Skip")));

		try {
			final String jwt = getJwt(request);
			ThreadContext.put("jwt", jwt);

			context.setJwt(jwt);
			if (jwt == null) {
				logger.error("Unexpected null jwt");
				// Throw a 401
				throw new RestException(false, 401, "Unauthorized", null);
			} else {
				final DecodedJWT djwt = JWT.decode(jwt);
				JwtUtility.verify(djwt);
				// final Map<String, Claim> claims = djwt.getClaims();
				context.setClaims(JwtUtility.getPayload(djwt));
				context.setUserId(context.getClaims().get(Claims.ID.getValue()));
				context.setPlan(context.getClaims().get(Claims.PLAN.getValue()));
				context.setRole(context.getClaims().get(Claims.ROLE.getValue()));
				// These will be null except for PROJECT tokens
				context.setProjectId(context.getClaims().get(Claims.PROJECT_ID.getValue()));
				context.setOrganizationId(context.getClaims().get(Claims.ORG_ID.getValue()));

				if (context.getUserId() != null) {
					ThreadContext.put("user-id", context.getUserId());
				}
				return context;
			}
		} catch (final RestException e) {
			throw e;
		} catch (final Exception e) {

			if (e instanceof com.auth0.jwt.exceptions.JWTDecodeException
					&& e.getMessage().contains("The token was expected to have 3 parts")) {
				logger.error(" jwt = " + ThreadContext.get("jwt"));
			}

			logger.error("Unexpected auth exception", e);
			// Throw a 401
			throw new RestException(false, 401, "Unauthorized", null);
		}
	}

	/**
	 * Returns the query string.
	 *
	 * @param map the map
	 * @return the query string
	 */
	private String getQueryString(final Map<String, String[]> map) {
		if (map == null) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		for (final String key : map.keySet()) {
			if (map.get(key) != null) {
				for (final String value : map.get(key)) {
					if (sb.length() > 0) {
						sb.append("&");
					}
					sb.append(key).append("=").append(value);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Handle exception.
	 *
	 * @param e               the e
	 * @param whatIsHappening the what is happening
	 * @throws Exception the exception
	 */
	public static void handleException(final Exception e, final String whatIsHappening) throws Exception {
		ExceptionHandler.handleException(e, whatIsHappening, "");
	}

//	/**
//	 * Reindex.
//	 *
//	 * @param operationsService the operations service
//	 * @param objects           the objects
//	 * @param background        the background
//	 * @throws Exception the exception
//	 */
//	public void reindex(final ElasticOperationsService operationsService, final String objects,
//			final boolean background) throws Exception {
//		adminInProgressCheck();
//
//		final Thread t = new Thread(new Runnable() {
//
//			/* see superclass */
//			@Override
//			public void run() {
//
//				// Create a local service so we can run in the background
//				try (final RootService service = new RootServiceImpl(operationsService) {
//					// n/a
//				}) {
//
//					adminInProgress = true;
//
//					for (final Class<? extends HasId> clazz : ModelUtility.getManagedObjects(service, objects)) {
//						logger.info("  indexing " + clazz.getSimpleName());
//						service.reindex(clazz, true);
//					}
//
//				} catch (final Exception e) {
//					try {
//						handleException(e, "trying to reindex");
//					} catch (final Exception e1) {
//						// n/a
//					}
//				} finally {
//					adminInProgress = false;
//				}
//
//			}
//		});
//		t.start();
//		if (!background) {
//			t.join();
//		}
//
//	}

//	/**
//	 * Delete objects.
//	 *
//	 * @param searchService     the search service
//	 * @param operationsService the operations service
//	 * @param objects           the objects
//	 * @param background        the background
//	 * @throws Exception the exception
//	 */
//	public void delete(final ElasticSearchService searchService, final ElasticOperationsService operationsService,
//			final String objects, final boolean background) throws Exception {
//		adminInProgressCheck();
//
//		final Thread t = new Thread(new Runnable() {
//
//			/* see superclass */
//			@Override
//			public void run() {
//				// Create a local service so we can run in the background
//				try (final RootService service = new RootServiceImpl(operationsService) {
//					// n/a
//				}) {
//					service.setModifiedBy("admin");
//					service.setTransactionPerOperation(true);
//
//					adminInProgress = true;
//
//					for (final Class<? extends HasId> clazz : ModelUtility.getManagedObjects(service, objects)) {
//						logger.info("  deleting data for " + clazz.getSimpleName());
//						try {
//
//							final List<String> ids = searchService.findAllIds("*:*", clazz);
//
//							for (final String id : ids) {
//								// Avoid removing the admin user
//								if (clazz.getSimpleName().equals("User")) {
//									final Object o = service.getEntityManager().find(clazz, id);
//									if (o != null && o.toString().contains("\"username\":\"admin\"")) {
//										logger.info("    SKIP deleting admin user");
//										continue;
//									}
//
//								}
//								// Removes the object AND the indexes if configured to do so
//								service.removeObject(service.getEntityManager().find(clazz, id));
//							}
//						} catch (final IllegalArgumentException e) {
//							logger.warn("  NOT AN ENTITY in this project");
//						}
//
//					}
//				} catch (final Exception e) {
//					try {
//						handleException(e, "trying to delete");
//					} catch (final Exception e1) {
//						// n/a
//					}
//				} finally {
//					adminInProgress = false;
//				}
//
//			}
//
//		});
//		t.start();
//		if (!background) {
//			t.join();
//		}
//
//	}

	/**
	 * Check reindex.
	 *
	 * @throws Exception               the exception
	 * @throws WebApplicationException the web application exception
	 */
	private void adminInProgressCheck() throws Exception {
		if (adminInProgress) {
			throw new RestException(true, 503, "Unable to process request, reindexing in progress.", null);
		}
	}

	/**
	 * Validate not empty.
	 *
	 * @param value     the value
	 * @param parameter the parameter
	 * @throws Exception the exception
	 */
	public void validateNotEmpty(final String value, final String parameter) throws Exception {
		if (StringUtility.isEmpty(value)) {
			throw new LocalException("Required parameter '" + parameter + "' is missing");
		}
	}
}
