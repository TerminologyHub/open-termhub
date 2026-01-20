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

import java.io.IOException;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Json logging filter.
 */

@Component
public class JsonLoggingFilter implements Filter {

    /** The logger. */
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(JsonLoggingFilter.class);

    /**
     * Do filter.
     *
     * @param req the req
     * @param res the res
     * @param chain the chain
     * @throws ServletException the servlet exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res,
        final FilterChain chain) throws ServletException, IOException {

        final HttpServletRequest hreq = (HttpServletRequest) req;
        final HttpServletResponse hres = (HttpServletResponse) res;
        final long startNanos = System.nanoTime();
        chain.doFilter(req, res);

        // Post-requets logging
        // Ignore "options" events
        if (hreq.getMethod().equals("OPTIONS")) {
            return;
        }

        final String reqUri = hreq.getRequestURI();
        if ("/favicon.ico".equals(reqUri)) {
            return;
        }

        final String httpVersion = hreq.getProtocol();
        final String reqMethod = hreq.getMethod();
        final String reqQuerystring = hreq.getQueryString();
        final String referer = hreq.getHeader("referer");
        final String userAgent = hreq.getHeader("user-agent");
        final long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;

        final String xff = hreq.getHeader("X-Forwarded-For");
        final String xri = hreq.getHeader("X-Real-IP");
        final String clientIp = xff != null && !xff.isEmpty() ? xff.split(",")[0].trim()
            : (xri != null && !xri.isEmpty() ? xri : hreq.getRemoteAddr());

        ThreadContext.put("http-version", httpVersion != null ? httpVersion : "");
        ThreadContext.put("remote-address", clientIp != null ? clientIp : "");
        ThreadContext.put("req-uri", reqUri != null ? reqUri : "");
        ThreadContext.put("req-method", reqMethod != null ? reqMethod : "");
        ThreadContext.put("req-querystring", reqQuerystring != null ? reqQuerystring : "");
        ThreadContext.put("referer", referer != null ? referer : "");
        ThreadContext.put("user-agent", userAgent != null ? userAgent : "");
        ThreadContext.put("status-code", String.valueOf(hres.getStatus()));
        ThreadContext.put("duration-ms", String.valueOf(durationMs));
        LoggerFactory.getLogger("HttpLogger").info(hreq.getMethod() + " " + hreq.getRequestURI());
        ThreadContext.clearAll();

    }

}
