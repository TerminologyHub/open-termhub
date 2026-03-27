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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.wci.termhub.model.MetaModel;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.Terminology;

/**
 * Utility for constructing CodeSystem properties from terminology Metadata.
 */
public final class CodeSystemMetadataPropertyUtility {

  /** The default metadata property type. */
  private static final String DEFAULT_METADATA_TYPE = "string";

  /**
   * Instantiates a new {@link CodeSystemMetadataPropertyUtility}.
   */
  private CodeSystemMetadataPropertyUtility() {
    // n/a
  }

  /**
   * Builds CodeSystem properties for the specified terminology and metadata list.
   *
   * @param terminology the terminology
   * @param metadataList the metadata list
   * @return the list
   */
  public static List<CodeSystemMetadataProperty> buildProperties(final Terminology terminology,
    final List<Metadata> metadataList) {

    final List<CodeSystemMetadataProperty> result = new ArrayList<>();
    if (terminology == null || metadataList == null || metadataList.isEmpty()) {
      addStandardProperties(result);
      return result;
    }

    final String baseUrl = terminology.getUri();
    for (final Metadata metadata : metadataList) {
      if (metadata.getCode() == null || metadata.getCode().isEmpty()) {
        continue;
      }
      if (isReservedStandardPropertyCode(metadata.getCode())) {
        continue;
      }

      final MetaModel.Model model = metadata.getModel();
      final MetaModel.Field field = metadata.getField();
      final boolean isRelationshipAdditionalType =
          model == MetaModel.Model.relationship && field == MetaModel.Field.additionalType;
      final boolean isConceptAttribute =
          model == MetaModel.Model.concept && field == MetaModel.Field.attribute;

      if (!isRelationshipAdditionalType && !isConceptAttribute) {
        continue;
      }

      result.add(CodeSystemMetadataProperty.fromMetadata(metadata, baseUrl, DEFAULT_METADATA_TYPE));
    }

    addStandardProperties(result);
    return result;
  }

  /**
   * True if the code matches the standard {@code parent} or {@code status} properties (case
   * insensitive), which are always emitted by {@link #addStandardProperties(List)}.
   *
   * @param code the metadata code
   * @return true if reserved
   */
  private static boolean isReservedStandardPropertyCode(final String code) {
    if (code == null || code.isEmpty()) {
      return false;
    }
    final String normalized = code.trim().toLowerCase(Locale.ROOT);
    return "parent".equals(normalized) || "status".equals(normalized) || "child".equals(normalized);
  }

  /**
   * Adds the standard parent and status properties.
   *
   * @param list the list
   */
  private static void addStandardProperties(final List<CodeSystemMetadataProperty> list) {
    list.add(new CodeSystemMetadataProperty("parent",
        "http://hl7.org/fhir/concept-properties#parent", "Parent concept", "code"));

    list.add(new CodeSystemMetadataProperty("status",
        "http://hl7.org/fhir/concept-properties#status", "Concept status", "code"));

      list.add(new CodeSystemMetadataProperty("child",
      "http://hl7.org/fhir/concept-properties#child", "Child concept", "code"));
  }
}

