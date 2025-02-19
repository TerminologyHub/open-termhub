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
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.wci.termhub.model.AbstractConfigurable;
import com.wci.termhub.model.HealthCheck;
import com.wci.termhub.rest.RestException;
import com.wci.termhub.util.JwtUtility;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.PropertyUtility;
import com.wci.termhub.util.StringUtility;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;

/**
 * Top level class for all REST clients.
 */
public class RootClientRestImpl extends AbstractConfigurable implements RootClient {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(RootClientRestImpl.class);

  /** The model package. */
  private static String modelPackage = "com.wci.termhub.model";

  /** The client. */
  private static ThreadLocal<Client> clients = new ThreadLocal<Client>() {
    @Override
    public Client initialValue() {
      return ClientBuilder.newClient();
    }
  };

  /** The service. */
  private final String service;

  /** The api url. */
  private String apiUrl;

  /** The api url internal. */
  private String apiUrlInternal;

  /** The internal. */
  private boolean internal = true;

  /**
   * Instantiates a {@link RootClientRestImpl} from the specified parameters.
   *
   * @param service the service
   * @throws Exception the exception
   */
  public RootClientRestImpl(final String service) throws Exception {
    this.service = service;

    apiUrl = PropertyUtility.getProperties().getProperty("api.url");
    apiUrlInternal =
        PropertyUtility.getProperties().getProperty("api.url.termhub-" + service + "-service");
  }

  /**
   * Validate not empty.
   *
   * @param parameter the parameter
   * @param parameterName the parameter name
   */
  protected void validateNotEmpty(final String parameter, final String parameterName) {
    if (parameter == null) {
      throw new IllegalArgumentException("Parameter " + parameterName + " must not be null.");
    }
    if (parameter.isEmpty()) {
      throw new IllegalArgumentException("Parameter " + parameterName + " must not be empty.");
    }
  }

  /**
   * Validate not empty.
   *
   * @param parameter the parameter
   * @param parameterName the parameter name
   */
  protected void validateNotEmpty(final Long parameter, final String parameterName) {
    if (parameter == null) {
      throw new IllegalArgumentException("Parameter " + parameterName + " must not be null.");
    }
  }

  /**
   * Returns the type.
   *
   * @param type the type
   * @return the type
   * @throws Exception the exception
   */
  public Class<?> getType(final String type) throws Exception {
    return Class.forName(modelPackage + "." + StringUtility.capitalize(type));
  }

  /**
   * Gets the api url.
   *
   * @return the api url
   */
  /* see superclass */
  @Override
  public String getApiUrl() {
    return apiUrl;
  }

  /**
   * Sets the api url.
   *
   * @param apiUrl the new api url
   */
  /* see superclass */
  @Override
  public void setApiUrl(final String apiUrl) {
    this.apiUrl = apiUrl;
  }

  /**
   * Gets the api url internal.
   *
   * @return the api url internal
   */
  /* see superclass */
  @Override
  public String getApiUrlInternal() {
    return apiUrlInternal;
  }

  /**
   * Sets the api url internal.
   *
   * @param apiUrlInternal the new api url internal
   */
  /* see superclass */
  @Override
  public void setApiUrlInternal(final String apiUrlInternal) {
    this.apiUrlInternal = apiUrlInternal;
  }

  /**
   * Checks if is internal.
   *
   * @return true, if is internal
   */
  /* see superclass */
  @Override
  public boolean isInternal() {
    return internal;
  }

  /**
   * Sets the internal.
   *
   * @param internal the internal
   */
  @Override
  public void setInternal(final boolean internal) {
    this.internal = internal;
  }

  /**
   * Request.
   *
   * @param target the target
   * @return the builder
   * @throws Exception the exception
   */
  public Builder request(final WebTarget target) throws Exception {
    return requestHelper(target, false);
  }

  /**
   * Request null jwt.
   *
   * @param target the target
   * @return the builder
   * @throws Exception the exception
   */
  public Builder requestNullJwt(final WebTarget target) throws Exception {
    return requestHelper(target, true);
  }

  /**
   * Request helper.
   *
   * @param target the target
   * @param nullJwt the null jwt
   * @return the builder
   * @throws Exception the exception
   */
  private Builder requestHelper(final WebTarget target, final boolean nullJwt) throws Exception {
    Builder builder = target.request(MediaType.APPLICATION_JSON);
    String jwt = ThreadContext.get("jwt");

    // If crossing the transom from "localhost" to "api.dev.termhub.net",
    // rewrite the jwt using JWT_KEY environment variable
    // logger.debug("XX test mode = " + ConfigUtility.isTestMode());
    // logger.debug("XX jwt = " + jwt);
    // logger.debug("XX remote addr = " +
    // ThreadContext.get("remote-address"));
    // logger.debug("XX url = " + getUrl());
    // logger.debug("XX JWT_KEY = " +
    // System.getenv().containsKey("JWT_KEY"));
    if (PropertyUtility.isTestMode() && jwt != null && System.getenv().containsKey("JWT_KEY")
        && (ThreadContext.get("remote-address") == null
            || ThreadContext.get("remote-address").contains("0:0:0:0:0:0:0:1")
            || ThreadContext.get("remote-address").contains("127.0.0.1")
            || ThreadContext.get("remote-address").contains("localhost"))
        && (target.getUri().toString().contains("qa.")
            || target.getUri().toString().contains("dev."))) {
      try {
        final String userId = JwtUtility.getUserId(JWT.decode(jwt).getClaims());
        logger.debug("  rewrite jwt = {}", userId);
        jwt = JwtUtility.rewriteJwt(jwt, System.getenv().get("JWT_KEY"), false);

        // Internal vs external token style
        if (target.getUri().toString().contains("/queues/")) {
          builder = builder.header("X-Termhub-Token", jwt);
        } else {
          // Rewriting for dev, technically this is coming from
          // outside.
          builder = builder.header("Authorization", "Bearer " + jwt);
        }
        if (ThreadContext.containsKey("correlation-id")) {
          builder = builder.header("X-Correlation-ID", ThreadContext.get("correlation-id"));
        }
        return builder;
      } catch (final SignatureException se) {
        // IF we get here, it's because the JWT involved wasn't signed
        // with the
        // local jwt.secret. This is likely because of use by a local
        // integration test that has authenticated with the dev
        // environment and
        // is passing an appropriate request.
        if (ThreadContext.get("remote-address") != null) {
          throw se;
        }
      }
    }

    if (internal && jwt != null) {
      builder = builder.header("X-Termhub-Token", jwt);
    } else if (!internal && jwt != null) {
      builder = builder.header("Authorization", "Bearer " + jwt);
    } else if (!nullJwt && jwt == null) {
      throw new Exception("JWT is unexpectedly null");
    }
    if (ThreadContext.containsKey("correlation-id")) {
      builder = builder.header("X-Correlation-ID", ThreadContext.get("correlation-id"));
    }
    return builder;
  }

  /**
   * Returns the url.
   *
   * @return the url
   */
  public String getUrl() {
    if (internal) {
      return apiUrlInternal;
    } else {
      return apiUrl;
    }
  }

  /**
   * Returns the clients.
   *
   * @return the clients
   */
  public static ThreadLocal<Client> getClients() {
    return clients;
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
    this.validateNotEmpty(adminKey, "adminKey");
    this.validateNotEmpty(task, "task");
    final Client client = getClients().get();
    final WebTarget target = client.target(getUrl() + "/" + service + "/admin?adminKey="
        + URLEncoder.encode(adminKey, StandardCharsets.UTF_8).replace("\\+", "%20") + "&task="
        + URLEncoder.encode(task, StandardCharsets.UTF_8).replace("\\+", "%20"));
    try (final Response response = request(target).post(Entity.json(payload))) {
      if (response == null) {
        throw new Exception("Unexpected null response");
      }
      final String result = response.readEntity(String.class);
      if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
        logger.error("  ERROR JSON = {}", result);
        throw new RestException(false, response.getStatus(), "Unexpected client error",
            "Unexpected error calling " + service + " admin");
      }
    }
  }

  /**
   * Health.
   *
   * @param dependencies the dependencies
   * @return the health check
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public HealthCheck health(final Boolean dependencies) throws Exception {
    final Client client = getClients().get();
    final WebTarget target = client.target(getUrl() + "/" + service + "/health"
        + (dependencies != null ? "?dependencies=" + dependencies : ""));
    try (final Response response = request(target).get()) {
      if (response == null) {
        throw new Exception("Unexpected null response");
      }
      final String result = response.readEntity(String.class);
      if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
        logger.error("  ERROR JSON = {}", result);
        throw new RestException(false, response.getStatus(), "Unexpected client error",
            "Unexpected error calling " + service + " health check");
      }
      return ModelUtility.fromJson(result, HealthCheck.class);
    }
  }

  /**
   * Gets the handler key.
   *
   * @return the handler key
   */
  /* see superclass */
  @Override
  public String getHandlerKey() {
    return null;
  }

  /**
   * Gets the encoded parameter.
   *
   * @param value the param
   * @return the encoded parameter
   * @throws Exception the exception
   */
  public String getEncodedParameter(final String value) throws Exception {
    return StringUtility.isEmpty(value) ? ""
        : URLEncoder.encode(value, StandardCharsets.UTF_8).replace("\\+", "%20");
  }

  /**
   * Gets the encoded parameter.
   *
   * @param value the value
   * @return the encoded parameter
   * @throws Exception the exception
   */
  public String getEncodedParameter(final Integer value) throws Exception {
    return (value == null) ? ""
        : URLEncoder.encode("" + value, StandardCharsets.UTF_8).replace("\\+", "%20");
  }

  /**
   * Gets the encoded parameter.
   *
   * @param value the value
   * @return the encoded parameter
   * @throws Exception the exception
   */
  public String getEncodedParameter(final Boolean value) throws Exception {
    return (value == null) ? ""
        : URLEncoder.encode("" + value, StandardCharsets.UTF_8).replace("\\+", "%20");
  }
}
