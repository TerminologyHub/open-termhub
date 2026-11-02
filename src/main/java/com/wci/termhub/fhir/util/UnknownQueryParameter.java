/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.uhn.fhir.rest.annotation.Metadata;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.method.BaseMethodBinding;
import ca.uhn.fhir.rest.server.method.BaseQueryParameter;
import ca.uhn.fhir.rest.server.method.IParameter;
import ca.uhn.fhir.rest.server.method.OperationParameter;

/**
 * Finds a query parameter that the matched FHIR method does not declare.
 */
public final class UnknownQueryParameter {

  /** Content negotiation, applied by HAPI before the provider method runs. */
  private static final Set<String> TRANSPORT = Set.of("_format", "_pretty");

  /**
   * Instantiates a new {@link UnknownQueryParameter}.
   */
  private UnknownQueryParameter() {
    // n/a
  }

  /**
   * Returns the first query parameter that is not declared on the matched method.
   *
   * <p>
   * {@code _format} and {@code _pretty} are always allowed. {@code mode} is allowed on
   * {@code @Metadata} methods. A search modifier such as {@code title:contains} is allowed when
   * the name before {@code :} is declared.
   *
   * @param requestDetails the request details
   * @param binding the matched method
   * @return the unknown parameter name, or null when every name is allowed
   */
  public static String firstUnknown(final RequestDetails requestDetails,
    final BaseMethodBinding binding) {
    if (requestDetails == null || binding == null) {
      return null;
    }
    final Map<String, String[]> parameters = requestDetails.getParameters();
    if (parameters == null || parameters.isEmpty()) {
      return null;
    }
    final Set<String> allowed = allowedNames(binding);
    for (final String name : parameters.keySet()) {
      if (name == null || !allowed.contains(baseName(name))) {
        return name;
      }
    }
    return null;
  }

  /**
   * Declared query names for the matched method, plus transport names.
   *
   * @param binding the matched method
   * @return the allowed names
   */
  private static Set<String> allowedNames(final BaseMethodBinding binding) {
    final Set<String> allowed = new HashSet<>(TRANSPORT);
    if (binding.getMethod().getAnnotation(Metadata.class) != null) {
      allowed.add("mode");
    }
    for (final IParameter parameter : binding.getParameters()) {
      final String name = parameterName(parameter);
      if (name != null && !name.isEmpty()) {
        allowed.add(name);
      }
    }
    return allowed;
  }

  /**
   * Query name for a search or operation parameter.
   *
   * @param parameter the method parameter
   * @return the name, or null when the parameter is not a query parameter
   */
  private static String parameterName(final IParameter parameter) {
    if (parameter instanceof BaseQueryParameter) {
      return ((BaseQueryParameter) parameter).getName();
    }
    if (parameter instanceof OperationParameter) {
      return ((OperationParameter) parameter).getName();
    }
    return null;
  }

  /**
   * Name without a FHIR search modifier.
   *
   * @param name the query parameter name
   * @return the name before the first colon
   */
  private static String baseName(final String name) {
    final int colon = name.indexOf(':');
    return colon < 0 ? name : name.substring(0, colon);
  }
}
