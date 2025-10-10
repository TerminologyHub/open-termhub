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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.core.env.Environment;

import com.wci.termhub.lucene.LuceneDataAccess;
import com.wci.termhub.model.HasId;
import com.wci.termhub.open.configuration.ApplicationProperties;
import com.wci.termhub.syndication.SyndicationContentTracker;
import com.wci.termhub.util.AdhocUtility;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.SystemReportUtility;

/**
 * The Application entry point.
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class, QuartzAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class, ThymeleafAutoConfiguration.class,
    SecurityAutoConfiguration.class, JmxAutoConfiguration.class, MailSenderAutoConfiguration.class
})
@EnableCaching
@EnableScheduling
public class Application extends SpringBootServletInitializer implements ApplicationRunner {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(Application.class);

  /** The application properties. */
  @Autowired
  private ApplicationProperties applicationProperties;

  /** The content tracker. */
  @Autowired(required = false)
  private SyndicationContentTracker contentTracker;

  /** The environment. */
  @Autowired
  private Environment environment;

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

    if (System.getenv().get("ADHOC") != null) {
      AdhocUtility.execute(System.getenv("ADHOC"));
    }

    // Log start of application
    logger.info("OPEN TERMHUB TERMINOLOGY APPLICATION START");
    SystemReportUtility.logMemory();

  }

  /**
   * Run after application context is initialized but before ApplicationReadyEvent.
   * Ensures indexes are created before any startup listeners execute.
   */
  @Override
  public void run(final ApplicationArguments args) throws Exception {
    final String[] profiles = environment.getActiveProfiles();
    for (final String profile : profiles) {
      if ("test".equals(profile)) {
        logger.info("Skipping bootstrap in test profile");
        return;
      }
    }
    bootstrap();
  }

  /**
   * Log memory.
   *
   * @throws Exception the exception
   */
  @Override
  public void run(final ApplicationArguments args) throws Exception {
    final String[] profiles = environment.getActiveProfiles();
    for (final String profile : profiles) {
      if ("test".equals(profile)) {
        logger.debug("Skipping bootstrap in test profile");
        return;
      }
    }
    bootstrap();
  }

  /**
   * Returns the managed objects.
   *
   * @return the managed objects
   * @throws Exception the exception
   */
  public static List<Class<? extends HasId>> getManagedObjects() throws Exception {

    return ModelUtility.getIndexedObjects();

  }

  /**
   * Bootstrap.
   *
   * @throws Exception the exception
   */
  private void bootstrap() throws Exception {

    logger.info("Index directory: {}", applicationProperties.getProperty("lucene.index.directory"));

    final List<Class<? extends HasId>> indexedObjects = ModelUtility.getIndexedObjects();
    final LuceneDataAccess luceneDataAccess = new LuceneDataAccess();
    luceneDataAccess.setApplicationProperties(applicationProperties);

    for (final Class<? extends HasId> clazz : indexedObjects) {
      luceneDataAccess.createIndex(clazz);
    }

    // Initialize content tracker (only if syndication is enabled)
    if (contentTracker != null) {
      contentTracker.initialize();
      logger.info("Content tracker initialized with {} loaded items",
          contentTracker.getLoadedContentCount());
    } else {
      logger.info("Content tracker not available (syndication disabled)");
    }
  }
}
