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
      addStandardProperties(result, terminology != null ? terminology.getUri() : null);
      return result;
    }

    final String baseUrl = terminology.getUri();
    for (final Metadata metadata : metadataList) {
      if (metadata.getCode() == null || metadata.getCode().isEmpty()) {
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

    addStandardProperties(result, baseUrl);
    return result;
  }

  /**
   * Adds the standard parent, status, and child properties when missing.
   *
   * @param list the list
   * @param codeSystemUrl the code system base URL; see
   *          {@link CodeSystemMetadataProperty#propertyUriForLoincOrFhirStandardCode}
   */
  private static void addStandardProperties(final List<CodeSystemMetadataProperty> list,
    final String codeSystemUrl) {

    if (list.stream().noneMatch(p -> "parent".equalsIgnoreCase(p.getCode()))) {
      list.add(new CodeSystemMetadataProperty("parent",
          CodeSystemMetadataProperty.propertyUriForLoincOrFhirStandardCode(codeSystemUrl, "parent"),
          "A parent code in the Component Hierarchy by System", "code"));
    }

    if (list.stream().noneMatch(p -> "status".equalsIgnoreCase(p.getCode()))) {
      list.add(new CodeSystemMetadataProperty("status",
          CodeSystemMetadataProperty.propertyUriForLoincOrFhirStandardCode(codeSystemUrl, "status"),
          "Concept status", "code"));
    }

    if (list.stream().noneMatch(p -> "child".equalsIgnoreCase(p.getCode()))) {
      list.add(new CodeSystemMetadataProperty("child",
          CodeSystemMetadataProperty.propertyUriForLoincOrFhirStandardCode(codeSystemUrl, "child"),
          "A child code in the Component Hierarchy by System", "code"));
    }
  }
}

