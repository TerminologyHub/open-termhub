/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.algo;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Simple holder for the Spring {@link ApplicationContext} so that static
 * utility code can obtain beans.
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

  /** The application context. */
  private static ApplicationContext context;

  /**
   * Sets the application context.
   *
   * @param applicationContext the new application context
   * @throws BeansException the beans exception
   */
  /* see superclass */
  @Override
  public void setApplicationContext(final ApplicationContext applicationContext)
    throws BeansException {
    context = applicationContext;
  }

  /**
   * Returns a bean by type, or {@code null} if the context is not yet
   * initialized.
   *
   * @param <T> the bean type
   * @param clazz the clazz
   * @return the bean or {@code null}
   */
  public static <T> T getBean(final Class<T> clazz) {
    if (context == null) {
      return null;
    }
    return context.getBean(clazz);
  }

}
