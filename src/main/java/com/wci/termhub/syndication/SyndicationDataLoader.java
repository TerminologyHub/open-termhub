/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.StringUtils;

/**
 * Standalone application for loading syndication data.
 * This class performs syndication and then exits, without starting a web server.
 * It is designed to be run as a one-time data loading operation.
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class, QuartzAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class, ThymeleafAutoConfiguration.class,
    SecurityAutoConfiguration.class, JmxAutoConfiguration.class, MailSenderAutoConfiguration.class
})
@ComponentScan(basePackages = "com.wci.termhub")

public class SyndicationDataLoader {

  /** The logger. */
  private static final Logger logger = LoggerFactory.getLogger(SyndicationDataLoader.class);

  /** Exit code for successful completion. */
  private static final int EXIT_SUCCESS = 0;

  /** Exit code for syndication errors. */
  private static final int EXIT_SYNDICATION_ERROR = 1;

  /** Exit code for configuration errors. */
  private static final int EXIT_CONFIGURATION_ERROR = 2;

  /**
   * Main method for standalone syndication data loading.
   *
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    logger.info("=".repeat(80));
    logger.info("Starting Standalone Syndication Data Loader");
    logger.info("=".repeat(80));

    ConfigurableApplicationContext context = null;
    int exitCode = EXIT_SUCCESS;

    try {
      // Validate that PROJECT_API_KEY is set
      final String apiKey = System.getenv("PROJECT_API_KEY");
      if (!StringUtils.hasText(apiKey)) {
        logger.error("PROJECT_API_KEY environment variable is not set or is empty");
        logger.error("Syndication requires a valid API key to authenticate with the syndication service");
        logger.error("Please set PROJECT_API_KEY and try again");
        System.exit(EXIT_CONFIGURATION_ERROR);
      }

      logger.info("PROJECT_API_KEY is set, proceeding with syndication");

      // Set system property to prevent Tomcat from starting
      System.setProperty("spring.main.web-application-type", "none");

      // Disable scheduling to prevent cron jobs from running
      System.setProperty("spring.scheduling.enabled", "false");

      // Initialize Spring context
      logger.info("Initializing Spring application context...");
      final SpringApplication app = new SpringApplication(SyndicationDataLoader.class);
      app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
      context = app.run(args);

      logger.info("Spring context initialized successfully");

      // Bootstrap Lucene indexes (same as Application.bootstrap())
      logger.info("Bootstrapping Lucene indexes...");
      final com.wci.termhub.open.configuration.ApplicationProperties applicationProperties =
          context.getBean(com.wci.termhub.open.configuration.ApplicationProperties.class);

      logger.info("Index directory: {}", applicationProperties.getProperty("lucene.index.directory"));

      final java.util.List<Class<? extends com.wci.termhub.model.HasId>> indexedObjects =
          com.wci.termhub.util.ModelUtility.getIndexedObjects();
      final com.wci.termhub.lucene.LuceneDataAccess luceneDataAccess =
          new com.wci.termhub.lucene.LuceneDataAccess();
      luceneDataAccess.setApplicationProperties(applicationProperties);

      for (final Class<? extends com.wci.termhub.model.HasId> clazz : indexedObjects) {
        luceneDataAccess.createIndex(clazz);
      }

      // Initialize content tracker
      final SyndicationContentTracker contentTracker =
          context.getBean(SyndicationContentTracker.class);
      contentTracker.initialize();
      logger.info("Content tracker initialized with {} loaded items",
          contentTracker.getLoadedContentCount());

      logger.info("Lucene indexes bootstrapped successfully");

      // Get the syndication manager bean
      final SyndicationManager syndicationManager = context.getBean(SyndicationManager.class);

      // Perform syndication check
      logger.info("Starting syndication process...");
      final SyndicationResults results = syndicationManager.performSyndicationCheck();

      // Log results
      logger.info("=".repeat(80));
      logger.info("Syndication Results:");
      logger.info("  Success: {}", results.isSuccess());
      logger.info("  Message: {}", results.getMessage());
      logger.info("  Total Processed: {}", results.getTotalProcessed());
      logger.info("  Total Loaded: {}", results.getTotalLoaded());
      logger.info("  Total Errors: {}", results.getTotalErrors());
      logger.info("  Duration: {} ms", results.getDurationMs());
      logger.info("  CodeSystems - Loaded: {}, Errors: {}", results.getCodeSystemLoaded(),
          results.getCodeSystemErrors());
      logger.info("  ValueSets - Loaded: {}, Errors: {}", results.getValueSetLoaded(),
          results.getValueSetErrors());
      logger.info("  ConceptMaps - Loaded: {}, Errors: {}", results.getConceptMapLoaded(),
          results.getConceptMapErrors());

      if (!results.getErrorMessages().isEmpty()) {
        logger.error("Errors encountered during syndication:");
        for (final String errorMsg : results.getErrorMessages()) {
          logger.error("  - {}", errorMsg);
        }
      }

      logger.info("=".repeat(80));

      // Determine exit code based on results
      if (!results.isSuccess()) {
        logger.error("Syndication failed");
        exitCode = EXIT_SYNDICATION_ERROR;
      } else if (results.getTotalErrors() > 0) {
        logger.warn("Syndication completed with errors");
        exitCode = EXIT_SYNDICATION_ERROR;
      } else {
        logger.info("Syndication completed successfully");
        exitCode = EXIT_SUCCESS;
      }

    } catch (final Exception e) {
      logger.error("Fatal error during syndication", e);
      exitCode = EXIT_SYNDICATION_ERROR;
    } finally {
      // Close the Spring context
      if (context != null) {
        try {
          logger.info("Closing Spring application context...");
          context.close();
          logger.info("Spring context closed");
        } catch (final Exception e) {
          logger.error("Error closing Spring context", e);
        }
      }

      logger.info("=".repeat(80));
      logger.info("Syndication Data Loader exiting with code: {}", exitCode);
      logger.info("=".repeat(80));

      // Exit with appropriate code
      System.exit(exitCode);
    }
  }
}
