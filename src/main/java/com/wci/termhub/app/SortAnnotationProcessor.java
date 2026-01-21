/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.app;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * The Class SortAnnotationProcessor.
 */
public class SortAnnotationProcessor {

  /**
   * Process.
   *
   * @param obj the obj
   * @throws Exception the exception
   */
  public void process(final Object obj) throws Exception {
    final Set<Integer> orders = new HashSet<>();
    for (final Field field : obj.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(Sort.class)) {
        final int order = field.getAnnotation(Sort.class).order();
        if (!orders.add(order)) {
          throw new Exception("Duplicate order value: " + order);
        }
      }
    }
  }
}
