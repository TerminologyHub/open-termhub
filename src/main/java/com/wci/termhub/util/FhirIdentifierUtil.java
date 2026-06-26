/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import java.util.List;

import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Normalizes FHIR resource identifiers on load and restores them on serve.
 *
 * <p>TermHub export files use internal identifier systems
 * ({@code https://terminologyhub.com/model/subset/code} and
 * {@code …/mapset/code}). On import those systems are rewritten to the vendor
 * terminology URI (e.g. {@code http://loinc.org}) while the value is preserved.
 * Vendor-native identifiers are stored unchanged in the {@link #ATTR_FHIR_IDENTIFIER}
 * entity attribute as a JSON array for pass-through by {@code FhirUtilityR4/R5}.
 */
public final class FhirIdentifierUtil {

  /** Attribute key for stored identifier JSON. */
  public static final String ATTR_FHIR_IDENTIFIER = "fhirIdentifier";

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(FhirIdentifierUtil.class);

  /**
   * Instantiates a new FHIR identifier util.
   */
  private FhirIdentifierUtil() {
    // Utility class
  }

  /**
   * Strip query string from a URI.
   *
   * @param uri the uri
   * @return the uri without query
   */
  public static String stripQuery(final String uri) {
    if (uri == null) {
      return null;
    }
    final int q = uri.indexOf('?');
    return q < 0 ? uri : uri.substring(0, q);
  }

  /**
   * Whether the identifier system is a TermHub internal code system.
   *
   * @param system the system
   * @return true, if TermHub code system
   */
  public static boolean isTermHubCodeSystem(final String system) {
    return system != null && system.contains("terminologyhub.com/model/")
        && system.endsWith("/code");
  }

  /**
   * Coerce a FHIR identifier node to an array.
   *
   * @param identifierNode the identifier node
   * @return the array node, or null
   */
  public static JsonNode toArrayNode(final JsonNode identifierNode) {
    if (identifierNode == null || identifierNode.isMissingNode() || identifierNode.isNull()) {
      return null;
    }
    if (identifierNode.isArray()) {
      return identifierNode;
    }
    return ThreadLocalMapper.get().createArrayNode().add(identifierNode);
  }

  /**
   * First identifier value from a FHIR identifier node.
   *
   * @param identifierNode the identifier node
   * @return the value
   */
  public static String firstValue(final JsonNode identifierNode) {
    final JsonNode arr = toArrayNode(identifierNode);
    if (arr == null) {
      return null;
    }
    for (final JsonNode item : arr) {
      final String value = item.path("value").asText(null);
      if (!StringUtility.isEmpty(value)) {
        return value;
      }
    }
    return null;
  }

  /**
   * First identifier value from R4 identifiers.
   *
   * @param identifiers the identifiers
   * @return the value
   */
  public static String firstValueR4(final List<Identifier> identifiers) {
    if (identifiers == null) {
      return null;
    }
    for (final Identifier identifier : identifiers) {
      if (!StringUtility.isEmpty(identifier.getValue())) {
        return identifier.getValue();
      }
    }
    return null;
  }

  /**
   * First identifier value from R5 identifiers.
   *
   * @param identifiers the identifiers
   * @return the value
   */
  public static String firstValueR5(final List<org.hl7.fhir.r5.model.Identifier> identifiers) {
    if (identifiers == null) {
      return null;
    }
    for (final org.hl7.fhir.r5.model.Identifier identifier : identifiers) {
      if (!StringUtility.isEmpty(identifier.getValue())) {
        return identifier.getValue();
      }
    }
    return null;
  }

  /**
   * Normalize identifiers for storage, rewriting TermHub code systems to vendor
   * basis.
   *
   * @param identifierNode the identifier node
   * @param vendorBasis the vendor basis uri
   * @return JSON array string
   */
  public static String normalizeForStorage(final JsonNode identifierNode,
    final String vendorBasis) {
    final JsonNode arr = toArrayNode(identifierNode);
    if (arr == null || arr.isEmpty()) {
      return null;
    }
    final String basis = stripQuery(vendorBasis);
    final ArrayNode out = ThreadLocalMapper.get().createArrayNode();
    for (final JsonNode item : arr) {
      final ObjectNode normalized = ThreadLocalMapper.get().createObjectNode();
      String system = item.path("system").asText(null);
      if (isTermHubCodeSystem(system) && basis != null) {
        system = basis;
      }
      if (!StringUtility.isEmpty(system)) {
        normalized.put("system", system);
      }
      final String value = item.path("value").asText(null);
      if (!StringUtility.isEmpty(value)) {
        normalized.put("value", value);
      }
      if (normalized.size() > 0) {
        out.add(normalized);
      }
    }
    return out.isEmpty() ? null : out.toString();
  }

  /**
   * Normalize R4 identifiers for storage.
   *
   * @param identifiers the identifiers
   * @param vendorBasis the vendor basis uri
   * @return JSON array string
   */
  public static String fromR4Identifiers(final List<Identifier> identifiers,
    final String vendorBasis) {
    if (identifiers == null || identifiers.isEmpty()) {
      return null;
    }
    final String basis = stripQuery(vendorBasis);
    final ArrayNode out = ThreadLocalMapper.get().createArrayNode();
    for (final Identifier identifier : identifiers) {
      final ObjectNode normalized = ThreadLocalMapper.get().createObjectNode();
      String system = identifier.getSystem();
      if (isTermHubCodeSystem(system) && basis != null) {
        system = basis;
      }
      if (!StringUtility.isEmpty(system)) {
        normalized.put("system", system);
      }
      if (!StringUtility.isEmpty(identifier.getValue())) {
        normalized.put("value", identifier.getValue());
      }
      if (normalized.size() > 0) {
        out.add(normalized);
      }
    }
    return out.isEmpty() ? null : out.toString();
  }

  /**
   * Normalize R5 identifiers for storage.
   *
   * @param identifiers the identifiers
   * @param vendorBasis the vendor basis uri
   * @return JSON array string
   */
  public static String fromR5Identifiers(final List<org.hl7.fhir.r5.model.Identifier> identifiers,
    final String vendorBasis) {
    if (identifiers == null || identifiers.isEmpty()) {
      return null;
    }
    final String basis = stripQuery(vendorBasis);
    final ArrayNode out = ThreadLocalMapper.get().createArrayNode();
    for (final org.hl7.fhir.r5.model.Identifier identifier : identifiers) {
      final ObjectNode normalized = ThreadLocalMapper.get().createObjectNode();
      String system = identifier.getSystem();
      if (isTermHubCodeSystem(system) && basis != null) {
        system = basis;
      }
      if (!StringUtility.isEmpty(system)) {
        normalized.put("system", system);
      }
      if (!StringUtility.isEmpty(identifier.getValue())) {
        normalized.put("value", identifier.getValue());
      }
      if (normalized.size() > 0) {
        out.add(normalized);
      }
    }
    return out.isEmpty() ? null : out.toString();
  }

  /**
   * Apply stored identifiers to an R4 ValueSet.
   *
   * @param valueSet the value set
   * @param fhirIdentifier the stored identifier json
   */
  public static void applyToR4ValueSet(final ValueSet valueSet, final String fhirIdentifier) {
    applyR4Identifiers(fhirIdentifier, valueSet::addIdentifier);
  }

  /**
   * Apply stored identifiers to an R4 ConceptMap (0..1).
   *
   * @param conceptMap the concept map
   * @param fhirIdentifier the stored identifier json
   */
  public static void applyToR4ConceptMap(final ConceptMap conceptMap, final String fhirIdentifier) {
    final Identifier identifier = firstR4Identifier(fhirIdentifier);
    if (identifier != null) {
      conceptMap.setIdentifier(identifier);
    }
  }

  /**
   * Apply stored identifiers to an R5 ValueSet.
   *
   * @param valueSet the value set
   * @param fhirIdentifier the stored identifier json
   */
  public static void applyToR5ValueSet(final org.hl7.fhir.r5.model.ValueSet valueSet,
    final String fhirIdentifier) {
    applyR5Identifiers(fhirIdentifier, valueSet::addIdentifier);
  }

  /**
   * Apply stored identifiers to an R5 ConceptMap.
   *
   * @param conceptMap the concept map
   * @param fhirIdentifier the stored identifier json
   */
  public static void applyToR5ConceptMap(final org.hl7.fhir.r5.model.ConceptMap conceptMap,
    final String fhirIdentifier) {
    applyR5Identifiers(fhirIdentifier, conceptMap::addIdentifier);
  }

  /**
   * Parse stored {@link #ATTR_FHIR_IDENTIFIER} JSON and return the first entry as
   * an R4 {@link Identifier}. Used for ConceptMap where cardinality is 0..1.
   *
   * @param fhirIdentifier JSON array of identifier objects from entity attributes
   * @return the first identifier, or null if missing, empty, or unparseable
   */
  private static Identifier firstR4Identifier(final String fhirIdentifier) {
    if (fhirIdentifier == null || fhirIdentifier.isEmpty()) {
      return null;
    }
    try {
      final JsonNode arr = ThreadLocalMapper.get().readTree(fhirIdentifier);
      if (!arr.isArray() || arr.isEmpty()) {
        return null;
      }
      return toR4Identifier(arr.get(0));
    } catch (final Exception e) {
      LOGGER.warn("Failed to parse fhirIdentifier", e);
      return null;
    }
  }

  /**
   * Parse stored {@link #ATTR_FHIR_IDENTIFIER} JSON and pass each entry to the
   * consumer as an R4 {@link Identifier}.
   *
   * @param fhirIdentifier JSON array of identifier objects from entity attributes
   * @param consumer receives each parsed identifier (e.g. ValueSet.addIdentifier)
   */
  private static void applyR4Identifiers(final String fhirIdentifier,
    final java.util.function.Consumer<Identifier> consumer) {
    if (fhirIdentifier == null || fhirIdentifier.isEmpty()) {
      return;
    }
    try {
      final JsonNode arr = ThreadLocalMapper.get().readTree(fhirIdentifier);
      if (!arr.isArray()) {
        return;
      }
      for (final JsonNode item : arr) {
        consumer.accept(toR4Identifier(item));
      }
    } catch (final Exception e) {
      LOGGER.warn("Failed to parse fhirIdentifier", e);
    }
  }

  /**
   * Parse stored {@link #ATTR_FHIR_IDENTIFIER} JSON and pass each entry to the
   * consumer as an R5 {@link org.hl7.fhir.r5.model.Identifier}.
   *
   * @param fhirIdentifier JSON array of identifier objects from entity attributes
   * @param consumer receives each parsed identifier (e.g. ValueSet.addIdentifier)
   */
  private static void applyR5Identifiers(final String fhirIdentifier,
    final java.util.function.Consumer<org.hl7.fhir.r5.model.Identifier> consumer) {
    if (fhirIdentifier == null || fhirIdentifier.isEmpty()) {
      return;
    }
    try {
      final JsonNode arr = ThreadLocalMapper.get().readTree(fhirIdentifier);
      if (!arr.isArray()) {
        return;
      }
      for (final JsonNode item : arr) {
        consumer.accept(toR5Identifier(item));
      }
    } catch (final Exception e) {
      LOGGER.warn("Failed to parse fhirIdentifier", e);
    }
  }

  /**
   * Map a single JSON identifier object to an R4 {@link Identifier}.
   *
   * @param item one element from the stored fhirIdentifier array
   * @return HAPI R4 identifier with system and value when present
   */
  private static Identifier toR4Identifier(final JsonNode item) {
    final Identifier identifier = new Identifier();
    if (!item.path("system").isMissingNode()) {
      identifier.setSystem(item.path("system").asText());
    }
    if (!item.path("value").isMissingNode()) {
      identifier.setValue(item.path("value").asText());
    }
    return identifier;
  }

  /**
   * Map a single JSON identifier object to an R5 {@link org.hl7.fhir.r5.model.Identifier}.
   *
   * @param item one element from the stored fhirIdentifier array
   * @return HAPI R5 identifier with system and value when present
   */
  private static org.hl7.fhir.r5.model.Identifier toR5Identifier(final JsonNode item) {
    final org.hl7.fhir.r5.model.Identifier identifier = new org.hl7.fhir.r5.model.Identifier();
    if (!item.path("system").isMissingNode()) {
      identifier.setSystem(item.path("system").asText());
    }
    if (!item.path("value").isMissingNode()) {
      identifier.setValue(item.path("value").asText());
    }
    return identifier;
  }
}
