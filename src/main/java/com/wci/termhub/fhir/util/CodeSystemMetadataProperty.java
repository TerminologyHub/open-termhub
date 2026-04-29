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

  /** HL7 standard CodeSystem property URIs (FHIR concept properties). */
  public static final String FHIR_CONCEPT_PROPERTY_URI_PARENT =
      "http://hl7.org/fhir/concept-properties#parent";

  /** The Constant FHIR_CONCEPT_PROPERTY_URI_STATUS. */
  public static final String FHIR_CONCEPT_PROPERTY_URI_STATUS =
      "http://hl7.org/fhir/concept-properties#status";

  /** The Constant FHIR_CONCEPT_PROPERTY_URI_CHILD. */
  public static final String FHIR_CONCEPT_PROPERTY_URI_CHILD =
      "http://hl7.org/fhir/concept-properties#child";

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
   * If {@code code} is a standard FHIR concept property (parent, child, status,
   * case insensitive), returns the HL7 fragment URI; otherwise returns
   * {@code null}.
   *
   * @param code the property code
   * @return the URI or null
   */
  public static String fhirConceptPropertyUriForCode(final String code) {
    if (code == null || code.isEmpty()) {
      return null;
    }
    final String n = code.trim();
    if ("parent".equalsIgnoreCase(n)) {
      return FHIR_CONCEPT_PROPERTY_URI_PARENT;
    }
    if ("child".equalsIgnoreCase(n)) {
      return FHIR_CONCEPT_PROPERTY_URI_CHILD;
    }
    if ("status".equalsIgnoreCase(n)) {
      return FHIR_CONCEPT_PROPERTY_URI_STATUS;
    }
    return null;
  }

  /**
   * True when the code system is LOINC.
   *
   * @param codeSystemUrl the CodeSystem URL
   * @return true if LOINC
   */
  public static boolean isLoincCodeSystemUrl(final String codeSystemUrl) {
    return codeSystemUrl != null && codeSystemUrl.contains("loinc.org");
  }

  /**
   * For {@code parent} / {@code child} / {@code status} only: parent and child
   * always use HL7 {@code concept-properties#} URIs (including for LOINC,
   * matching fhir.loinc.org). For LOINC only, {@code status} uses
   * {@code {base}/property/{code}}; all other code systems use HL7 for status
   * as well.
   *
   * @param codeSystemUrl the code system base URL
   * @param code the property code (one of the three; casing preserved for the
   *          LOINC status path)
   * @return the URI, or null if code is not one of the three
   */
  public static String propertyUriForLoincOrFhirStandardCode(final String codeSystemUrl,
    final String code) {

    final String fhir = fhirConceptPropertyUriForCode(code);
    if (fhir == null) {
      return null;
    }
    if (isLoincCodeSystemUrl(codeSystemUrl) && !codeSystemUrl.isEmpty()
        && "status".equalsIgnoreCase(code.trim())) {
      return codeSystemUrl + "/property/" + code.trim();
    }
    return fhir;
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
    String uri = codeSystemUrl == null || codeSystemUrl.isEmpty() ? null
        : codeSystemUrl + "/property/" + code;
    final String fhirUri = fhirConceptPropertyUriForCode(code);
    if (fhirUri != null) {
      if (!isLoincCodeSystemUrl(codeSystemUrl)) {
        uri = fhirUri;
      } else if (code == null || !"status".equalsIgnoreCase(code.trim())) {
        uri = fhirUri;
      }
    }
    final String description = metadata.getName();
    final String storedType = metadata.getAttributes().get("fhirPropertyType");
    final String type = (storedType != null && !storedType.isEmpty()) ? storedType : defaultType;
    return new CodeSystemMetadataProperty(code, uri, description, type);
  }
}
