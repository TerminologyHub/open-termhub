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

import com.wci.termhub.util.PropertyUtility;

/**
 * Reads {@code server.mode} for static utilities (e.g. CodeSystem import).
 */
public final class ServerModeUtility {

  /** Application property key for FHIR server mode. */
  public static final String SERVER_MODE_PROPERTY = "server.mode";

  /**
   * Instantiates a {@link ServerModeUtility}.
   */
  private ServerModeUtility() {
    // utility class
  }

  /**
   * Current FHIR mode from application configuration.
   *
   * @return {@link ServerMode#DEFAULT} when unset or unrecognized
   */
  public static ServerMode currentMode() {
    return ServerMode.fromValue(PropertyUtility.getProperty(SERVER_MODE_PROPERTY));
  }

  /**
   * True when {@code server.mode=regenstrief}.
   *
   * @return true in Regenstrief import/FHIR compatibility mode
   */
  public static boolean isRegenstriefImportMode() {
    return currentMode().isRegenstriefMode();
  }
}
