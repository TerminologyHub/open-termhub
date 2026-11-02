/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.rest;

import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Rejects query parameters that the matched Spring handler does not declare.
 */
public class UnknownQueryParameterInterceptor implements HandlerInterceptor {

  /**
   * Rejects a query parameter that is not a {@code @RequestParam} on the handler.
   *
   * @param request the request
   * @param response the response
   * @param handler the handler
   * @return true when every query parameter is declared
   */
  @Override
  public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
    final Object handler) {
    if (!(handler instanceof HandlerMethod)) {
      return true;
    }
    final Set<String> allowed = declaredNames((HandlerMethod) handler);
    for (final String name : request.getParameterMap().keySet()) {
      if (!allowed.contains(name)) {
        throw new RestException(false, HttpServletResponse.SC_BAD_REQUEST, "Bad Request",
            "Input parameter '" + name + "' is not supported");
      }
    }
    return true;
  }

  /**
   * {@code @RequestParam} names on the handler method.
   *
   * @param handlerMethod the handler method
   * @return the declared names
   */
  private static Set<String> declaredNames(final HandlerMethod handlerMethod) {
    final Set<String> allowed = new HashSet<>();
    for (final MethodParameter parameter : handlerMethod.getMethodParameters()) {
      final RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
      if (requestParam == null) {
        continue;
      }
      final String named = requestParam.name().isEmpty() ? requestParam.value() : requestParam.name();
      if (!named.isEmpty()) {
        allowed.add(named);
      }
    }
    return allowed;
  }
}
