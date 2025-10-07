package com.wci.termhub.rest;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CORS Options filter. Top-level filter to always support OPTIONS.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsOptionsFilter implements Filter {

  /** The Constant ALLOWED_ORIGINS. */
  // Define the CORS headers to be returned for the OPTIONS preflight check
  private static final String ALLOWED_ORIGINS = "*";

  /** The Constant ALLOWED_METHODS. */
  private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS";

  /** The Constant ALLOWED_HEADERS. */
  private static final String ALLOWED_HEADERS =
      "Authorization, Content-Type, Origin, Accept, X-Requested-With, "
          + "Access-Control-Request-Method, Access-Control-Request-Headers";

  /** The Constant MAX_AGE. Cache preflight response for 1 hour. */
  private static final String MAX_AGE = "3600";

  /* see superclass */
  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
    throws IOException, ServletException {

    final HttpServletResponse response = (HttpServletResponse) res;
    final HttpServletRequest request = (HttpServletRequest) req;

    // Apply CORS headers for all responses regardless of method
    response.setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGINS);
    response.setHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
    response.setHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS);
    response.setHeader("Access-Control-Max-Age", MAX_AGE);
    response.setHeader("Access-Control-Allow-Credentials", "true"); // If needed

    // If this is an OPTIONS preflight request, terminate it here.
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      response.setStatus(HttpServletResponse.SC_OK);
      return; // STOP EXECUTION
    }

    // For all other methods (GET, POST, etc.), continue down the chain to HAPI.
    chain.doFilter(req, res);
  }
}
