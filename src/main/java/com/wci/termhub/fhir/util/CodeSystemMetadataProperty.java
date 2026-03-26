/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.util;

import com.wci.termhub.model.Metadata;

/**
 * Simple value object representing a CodeSystem property derived from Metadata.
 */
public final class CodeSystemMetadataProperty {

  /** The code. */
  private final String code;

  /** The uri. */
  private final String uri;

  /** The description. */
  private final String description;

  /** The type. */
  private final String type;

  /**
   * Instantiates a new {@link CodeSystemMetadataProperty}.
   *
   * @param code the code
   * @param uri the uri
   * @param description the description
   * @param type the type (e.g. "string", "code")
   */
  public CodeSystemMetadataProperty(final String code, final String uri, final String description,
      final String type) {
    this.code = code;
    this.uri = uri;
    this.description = description;
    this.type = type;
  }

  /**
   * Returns the code.
   *
   * @return the code
   */
  public String getCode() {
    return code;
  }

  /**
   * Returns the uri.
   *
   * @return the uri
   */
  public String getUri() {
    return uri;
  }

  /**
   * Returns the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Creates a {@link CodeSystemMetadataProperty} from the specified metadata.
   *
   * @param metadata the metadata
   * @param codeSystemUrl the code system url
   * @param defaultType the default type
   * @return the code system metadata property
   */
  public static CodeSystemMetadataProperty fromMetadata(final Metadata metadata,
    final String codeSystemUrl, final String defaultType) {

    final String code = metadata.getCode();
    final String uri = codeSystemUrl == null || codeSystemUrl.isEmpty() ? null
        : codeSystemUrl + "/property/" + code;
    final String description = metadata.getName();
    return new CodeSystemMetadataProperty(code, uri, description, defaultType);
  }
}
