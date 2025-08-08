/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
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

        HttpServletRequest hreq = (HttpServletRequest) req;
        HttpServletResponse hres = (HttpServletResponse) res;
        chain.doFilter(req, res);

        // Post-requets logging
        // Ignore "options" events
        if (hreq.getMethod().equals("OPTIONS")) {
            return;
        }

        // Handle things computed by RootServiceRestImpl
        for (final String key : new String[] {
                "http-version", "remote-address", "correlation-id", "req-uri", "req-method",
                "req-querystring", "user-id", "referer", "user-agent"
        }) {
            if (ThreadContext.get(key) == null) {
                ThreadContext.put(key, "");
            }
        }

        ThreadContext.put("status-code", String.valueOf(hres.getStatus()));
        // NO good way to get the content length here

        // remote-address - handled by RootServiceRestImpl
        LoggerFactory.getLogger("HttpLogger").info(hreq.getMethod() + " " + hreq.getRequestURI());
        ThreadContext.clearAll();

    }

}
