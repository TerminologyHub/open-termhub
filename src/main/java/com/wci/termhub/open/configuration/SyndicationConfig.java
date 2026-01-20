/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.open.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for syndication-related beans.
 */
@Configuration
public class SyndicationConfig {

  /**
   * Syndication URL bean.
   *
   * @param url the url
   * @return the string
   */
  @Bean
  public String syndicationUrl(@Value("${syndication.url}") final String url) {
    return url;
  }

  /**
   * Syndication token bean.
   *
   * @param token the token
   * @return the string
   */
  @Bean
  public String syndicationToken(@Value("${syndication.token}") final String token) {
    return token;
  }
}
