/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * The app configuration.
 */
@Configuration
@ComponentScan(basePackages = {
    "com.wci.termhub.algo", "com.wci.termhub.fhir", "com.wci.termhub.handler",
    "com.wci.termhub.loader", "com.wci.termhub.lucene", "com.wci.termhub.util"
})
public class AppConfig {

  /**
   * Rest template.
   *
   * @return the rest template
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
