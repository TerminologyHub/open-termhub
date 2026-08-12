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

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wci.termhub.ReadOnlyMode;

/**
 * Springdoc customization for read-only mode.
 */
@Configuration
public class ReadOnlyOpenApiConfig {

  /**
   * Hides content-mutating REST operations from Swagger when read-only.
   *
   * @param readOnlyMode the read only mode
   * @return the open api customizer
   */
  @Bean
  public OpenApiCustomizer readOnlyOpenApiCustomizer(final ReadOnlyMode readOnlyMode) {
    return openApi -> {
      if (readOnlyMode != null && readOnlyMode.isEnabled()) {
        ReadOnlyOpenApiSupport.removeRestMutatingOperations(openApi.getPaths());
      }
    };
  }
}
