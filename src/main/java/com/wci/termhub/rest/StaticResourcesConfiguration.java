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
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/** Static Resources Configuration. */
@Configuration
@EnableConfigurationProperties({
    ResourceWebPropertiesConfig.class
})
public class StaticResourcesConfiguration implements WebMvcConfigurer {

  /** The logger. */
  @SuppressWarnings("unused")
  private static Logger logger = LoggerFactory.getLogger(StaticResourcesConfiguration.class);

  /** Extensions served from the static locations. */
  private static final String[] STATIC_EXTENSIONS = new String[] {
      "css", "html", "js", "json", "bmp", "jpeg", "jpg", "png", "ttf", "eot", "svg", "woff",
      "woff2"
  };

  /** PathPattern allows ** only at the end, so each extension is listed per segment depth. */
  private static final int MAX_STATIC_PATH_SEGMENTS = 8;

  /** The Constant STATIC_RESOURCES. */
  static final String[] STATIC_RESOURCES = staticResourcePatterns();

  /** The resource properties. */
  @Autowired
  private WebProperties.Resources resourceProperties = new WebProperties.Resources();

  /* see superclass */
  @Override
  public void addResourceHandlers(final ResourceHandlerRegistry registry) {
    // Add all static files
    // Long cachePeriodLong = resourceProperties.getCache().getPeriod().getSeconds();
    final Long cachePeriodLong = 30L;
    final int cachePeriodInt = cachePeriodLong.intValue();
    final Integer cachePeriod = Integer.valueOf(cachePeriodInt);

    registry.addResourceHandler(STATIC_RESOURCES)
        .addResourceLocations(resourceProperties.getStaticLocations()).setCachePeriod(cachePeriod);

    // Create mapping to index.html for Angular HTML5 mode.
    final String[] indexLocations = getIndexLocations();
    registry.addResourceHandler("/**").addResourceLocations(indexLocations)
        .setCachePeriod(cachePeriod).resourceChain(true).addResolver(new PathResourceResolver() {
          @Override
          protected Resource getResource(final String resourcePath, final Resource location)
            throws IOException {
            final Resource index = location.createRelative("index.html");
            return index.exists() && index.isReadable() ? index : null;
          }
        });
  }

  /**
   * Path patterns for static files. {@code **} is only legal at the end of a pattern.
   *
   * @return the patterns
   */
  private static String[] staticResourcePatterns() {
    final String[] patterns = new String[STATIC_EXTENSIONS.length * MAX_STATIC_PATH_SEGMENTS];
    int index = 0;
    for (final String extension : STATIC_EXTENSIONS) {
      final StringBuilder prefix = new StringBuilder();
      for (int depth = 1; depth <= MAX_STATIC_PATH_SEGMENTS; depth++) {
        prefix.append("/*");
        patterns[index++] = prefix.toString() + "." + extension;
      }
    }
    return patterns;
  }

  /**
   * Returns static resource directory locations (trailing slash preserved) used to resolve
   * {@code index.html} for HTML5-mode fallback.
   *
   * @return the index locations
   */
  private String[] getIndexLocations() {
    return Arrays.stream(resourceProperties.getStaticLocations())
        .map((location) -> location.endsWith("/") ? location : location + "/")
        .toArray(String[]::new);
  }

  /* see superclass */
  @Override
  public void addInterceptors(final InterceptorRegistry registry) {
    registry.addInterceptor(new UnknownQueryParameterInterceptor());
  }

  /* see superclass */
  @Override
  public void addViewControllers(final ViewControllerRegistry registry) {
    // This view controller is the key to solving your problem.
    // It is a clean way to tell Spring to forward any path that doesn't
    // match a static resource or an API endpoint to the index.html file.
    registry.addViewController("/{path:[^\\.]*}").setViewName("forward:/index.html");

    // Multi-level paths. ** is only legal at the end under PathPattern.
    registry.addViewController("/{path:[^\\.]*}/**").setViewName("forward:/index.html");
  }

}
