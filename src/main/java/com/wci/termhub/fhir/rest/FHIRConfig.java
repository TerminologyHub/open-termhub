/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.rest;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wci.termhub.fhir.rest.r4.HapiR4RestfulServlet;
import com.wci.termhub.fhir.rest.r5.HapiR5RestfulServlet;

import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;

/**
 * Servlet registration bean.
 */
@Configuration
public class FHIRConfig {

	/**
	 * Hapi R4.
	 *
	 * @return the servlet registration bean
	 */
	@Bean
	public ServletRegistrationBean<HapiR4RestfulServlet> hapiR4() {
		final HapiR4RestfulServlet hapiServlet = new HapiR4RestfulServlet();

		final ServletRegistrationBean<HapiR4RestfulServlet> servletRegistrationBean = new ServletRegistrationBean<>(
				hapiServlet, "/fhir/r4/*");
		hapiServlet.setServerName("Open Termhub R4 FHIR Terminology Server");
		hapiServlet.setServerVersion(getClass().getPackage().getImplementationVersion());
		hapiServlet.setDefaultResponseEncoding(EncodingEnum.JSON);

		final ResponseHighlighterInterceptor interceptor = new ResponseHighlighterInterceptor();
		hapiServlet.registerInterceptor(interceptor);

		return servletRegistrationBean;
	}

	/**
	 * Hapi R5.
	 *
	 * @return the servlet registration bean
	 */
	@Bean
	public ServletRegistrationBean<HapiR5RestfulServlet> hapiR5() {
		final HapiR5RestfulServlet hapiServlet = new HapiR5RestfulServlet();

		final ServletRegistrationBean<HapiR5RestfulServlet> servletRegistrationBean = new ServletRegistrationBean<>(
				hapiServlet, "/fhir/r5/*");
		hapiServlet.setServerName("Opeb Termhub R5 FHIR Terminology Server");
		hapiServlet.setServerVersion(getClass().getPackage().getImplementationVersion());
		hapiServlet.setDefaultResponseEncoding(EncodingEnum.JSON);

		final ResponseHighlighterInterceptor interceptor = new ResponseHighlighterInterceptor();
		hapiServlet.registerInterceptor(interceptor);

		return servletRegistrationBean;
	}

}
