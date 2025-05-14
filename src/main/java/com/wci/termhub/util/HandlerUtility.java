/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import java.util.Properties;

import com.wci.termhub.model.Configurable;

/**
 * Utility methods for handlers.
 */
public final class HandlerUtility {

  /**
   * Instantiates an empty {@link HandlerUtility}.
   */
  private HandlerUtility() {
    // n/a
  }

  /**
   * Instantiates a handler using standard setup and configures it with properties.
   *
   * @param <T> the
   * @param property the property
   * @param handlerName the handler name
   * @param type the type
   * @return the t
   * @throws Exception the exception
   */
  public static <T extends Configurable> T newStandardHandlerInstanceWithConfiguration(
    final String property, final String handlerName, final Class<T> type) throws Exception {

    // Instantiate the handler
    // property = "metadata.service.handler" (e.g)
    // handlerName = "SNOMED" (e.g.)
    final String classKey = property + "." + handlerName + ".class";

    if (PropertyUtility.getProperty(classKey) == null) {
      throw new Exception("Unexpected null classkey " + classKey);
    }
    final String handlerClass = PropertyUtility.getProperty(classKey);
    // logger.debug("Instantiate " + handlerClass);
    final T handler = newHandlerInstance(handlerName, handlerClass, type);

    // Look up and build properties
    final Properties handlerProperties = new Properties();
    // handlerProperties.setProperty("security.handler", handlerName);

    for (final Object key : PropertyUtility.getProperties().keySet()) {

      // properties like "metadata.service.handler.SNOMED.class"
      String propertyNamePrefix = property + "." + handlerName + ".";

      // also add any general properties so the handlers can access them
      if (key.toString().startsWith("general.")) {
        propertyNamePrefix = "general.";
      }

      // Find relevant properties
      if (key.toString().startsWith(propertyNamePrefix)) {

        String shortKey = key.toString();

        // do not shorten general properties so there will be no name
        // conflicts
        if (propertyNamePrefix != "general.") {
          shortKey = key.toString().substring((propertyNamePrefix).length());
        }

        // if (!property.contains("password")) {
        // logger.debug(" property " + shortKey + " = " +
        // config.getProperty(key.toString()));
        // }
        handlerProperties.put(shortKey, PropertyUtility.getProperty(key.toString()));
      }
    }

    handler.setProperties(handlerProperties);
    return handler;
  }

  /**
   * New handler instance.
   *
   * @param <T> the
   * @param handler the handler
   * @param handlerClass the handler class
   * @param type the type
   * @return the object
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "unchecked", "deprecation"
  })
  public static <T> T newHandlerInstance(final String handler, final String handlerClass,
    final Class<T> type) throws Exception {
    if (handlerClass == null) {
      throw new Exception("Handler class " + handlerClass + " is not defined");
    }
    final Class<?> toInstantiate = Class.forName(handlerClass);
    if (toInstantiate == null) {
      throw new Exception("Unable to find class " + handlerClass);
    }
    Object o = null;
    try {
      o = toInstantiate.newInstance();
    } catch (final Exception e) {
      e.printStackTrace();
      // do nothing
    }
    if (o == null) {
      throw new Exception(
          "Unable to instantiate class " + handlerClass + ", check for default constructor.");
    }
    if (type.isAssignableFrom(o.getClass())) {
      return (T) o;
    }
    throw new Exception("Handler is not assignable from " + type.getName());
  }

}
