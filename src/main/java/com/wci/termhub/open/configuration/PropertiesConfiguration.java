/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.open.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Properties configuration.
 */
@Configuration
@EnableAutoConfiguration
public class PropertiesConfiguration {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(PropertiesConfiguration.class);

  /**
   * Instantiates a new properties configuration.
   */
  public PropertiesConfiguration() {
    logger.info("CREATE PropertiesConfiguration");
  }

  /**
   * Application properties.
   *
   * @return the application properties
   */
  @ConfigurationProperties
  @Bean
  public ApplicationProperties applicationProperties() {
    logger.info("CREATE ApplicationProperties");
    final ApplicationProperties prop = new ApplicationProperties();
    logger.info("Loaded properties: {}", prop);
    return prop;
  }

}
