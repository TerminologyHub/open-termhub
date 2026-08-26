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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.wci.termhub.util.PropertyUtility;

import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

/**
 * Helpers for read-only OpenAPI / Swagger presentation.
 */
public final class ReadOnlyOpenApiSupport {

  /**
   * Server modes that are inherently read-only, regardless of the {@code read.only} property. Add
   * future read-only modes here to have their APIs presented without mutating operations.
   */
  private static final Set<String> READ_ONLY_SERVER_MODES = Set.of("regenstrief");

  /**
   * Instantiates a new read only open api support.
   */
  private ReadOnlyOpenApiSupport() {
    // n/a
  }

  /**
   * Indicates whether the given server mode is inherently read-only.
   *
   * @param serverMode the server mode
   * @return true, if the server mode is read-only
   */
  public static boolean isReadOnlyServerMode() {
    return PropertyUtility.getServerMode() != null
        && READ_ONLY_SERVER_MODES.contains(PropertyUtility.getServerMode());
  }

  /**
   * Removes DELETE, PUT, PATCH, and POST operations from OpenAPI paths.
   *
   * @param paths the paths
   */
  public static void removeRestMutatingOperations(final Paths paths) {
    removeNonGetOperations(paths);
  }

  /**
   * Removes DELETE, PUT, PATCH, and POST operations from FHIR OpenAPI paths.
   *
   * @param paths the paths
   */
  public static void removeFhirMutatingOperations(final Paths paths) {
    removeNonGetOperations(paths);
  }

  /**
   * Removes non-GET operations from OpenAPI paths.
   *
   * @param paths the paths
   */
  private static void removeNonGetOperations(final Paths paths) {
    if (paths == null) {
      return;
    }
    final Iterator<Map.Entry<String, PathItem>> iterator = paths.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<String, PathItem> entry = iterator.next();
      final PathItem item = entry.getValue();
      if (item == null) {
        iterator.remove();
        continue;
      }
      item.setPost(null);
      item.setPut(null);
      item.setPatch(null);
      item.setDelete(null);
      if (isEmpty(item)) {
        iterator.remove();
      }
    }
  }

  /**
   * Indicates whether the path item has no operations.
   *
   * @param item the item
   * @return true, if empty
   */
  private static boolean isEmpty(final PathItem item) {
    return item.getGet() == null && item.getPost() == null && item.getPut() == null
        && item.getPatch() == null && item.getDelete() == null && item.getHead() == null
        && item.getOptions() == null && item.getTrace() == null;
  }
}
