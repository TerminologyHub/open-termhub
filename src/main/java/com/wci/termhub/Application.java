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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.wci.termhub.model.HasId;
import com.wci.termhub.model.Mapping;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.util.AdhocUtility;
import com.wci.termhub.util.ModelUtility;

/**
 * The Application entry point.
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class, QuartzAutoConfiguration.class,
})
@ComponentScan
@EnableCaching
@EnableScheduling
public class Application extends SpringBootServletInitializer {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(Application.class);

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public static void main(final String[] args) throws Exception {

    // https://stackoverflow.com/questions/13482020/
    // encoded-slash-2f-with-spring-requestmapping-path-param-gives-http-400
    System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
    SpringApplication.run(Application.class, args);

    // Log start of application
    logger.debug("TERMHUB TERMINOLOGY APPLICATION START");

    // Bootstrap indexes
    // TODO: is this needed?
    // bootstrap();

    if (System.getenv().get("ADHOC") != null) {
      AdhocUtility.execute(System.getenv("ADHOC"));
    }
  }

  /**
   * Returns the managed objects.
   *
   * @return the managed objects
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public static List<Class<? extends HasId>> getManagedObjects() throws Exception {
    // Create indexes on application startup
    return ModelUtility.asList(Terminology.class, Mapset.class, Mapping.class);
    // Concept, ConceptRelationship, ConceptTreePosition and Metadata all have
    // their own indexes now
  }
}
