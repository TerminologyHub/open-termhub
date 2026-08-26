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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wci.termhub.model.HasAttributes;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.Terminology;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * Preserves "extra" top-level FHIR properties that are not otherwise mapped onto the internal
 * {@link Subset} (ValueSet), {@link Terminology} (CodeSystem), or {@link Mapset} (ConceptMap)
 * models.
 *
 * <p>Each loader maps a handful of top-level properties onto dedicated model fields or attributes.
 * Every other top-level property (e.g. {@code purpose}, {@code jurisdiction}, {@code useContext},
 * {@code extension}, {@code text}, {@code contained}, and version-specific fields) would otherwise
 * be dropped on import. On load, the {@code storeExtra*} methods capture each such property as an
 * attribute keyed {@code <prefix><propertyName>} whose value is the JSON representation of that
 * property. On serve, the {@code applyExtra*} methods reassemble those attributes back into the
 * resource and re-parse, reconstructing the full resource.
 *
 * <p>Because storage/reconstruction round-trips through the HAPI FHIR JSON parser, complex
 * structures (repeating elements, choice types, extensions, contained resources) are handled
 * generically without per-property code. Bulk content (ValueSet compose/expansion, CodeSystem
 * concept/property, ConceptMap group) and identifier/contact (handled by dedicated code) are
 * excluded.
 */
public final class FhirExtraAttributeUtil {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(FhirExtraAttributeUtil.class);

  /** Prefix for subset attributes that carry a preserved top-level ValueSet property. */
  public static final String VS_ATTR_PREFIX = "fhirVsExtra.";

  /** Prefix for terminology attributes that carry a preserved top-level CodeSystem property. */
  public static final String CS_ATTR_PREFIX = "fhirCsExtra.";

  /** Prefix for mapset attributes that carry a preserved top-level ConceptMap property. */
  public static final String CM_ATTR_PREFIX = "fhirCmExtra.";

  /** The Constant contextR4. */
  private static final FhirContext FHIR_CONTEXT_R4 = FhirContext.forR4();

  /** The Constant contextR5. */
  private static final FhirContext FHIR_CONTEXT_R5 = FhirContext.forR5();

  /**
   * Top-level ValueSet JSON property names already captured on dedicated fields or attributes,
   * regenerated on serve, or holding bulk content. Excluded from the "extra" attributes.
   */
  private static final Set<String> VS_HANDLED = Set.of("resourceType", "id", "meta", "url",
      "identifier", "version", "name", "title", "status", "experimental", "publisher", "contact",
      "description", "copyright", "date", "compose", "expansion");

  /**
   * Top-level CodeSystem JSON property names already captured on dedicated fields or attributes,
   * regenerated on serve, or holding bulk content. Excluded from the "extra" attributes.
   */
  private static final Set<String> CS_HANDLED = Set.of("resourceType", "id", "meta", "url",
      "identifier", "version", "name", "title", "status", "publisher", "contact", "description",
      "copyright", "date", "valueSet", "caseSensitive", "versionNeeded", "compositional", "count",
      "content", "hierarchyMeaning", "concept", "property");

  /**
   * Top-level ConceptMap JSON property names already captured on dedicated fields or attributes,
   * regenerated on serve, or holding bulk content. Excluded from the "extra" attributes.
   */
  private static final Set<String> CM_HANDLED = Set.of("resourceType", "id", "meta", "url",
      "identifier", "version", "name", "title", "status", "publisher", "contact", "description",
      "copyright", "date", "active", "group", "source", "sourceUri", "sourceScope",
      "sourceScopeUri", "target", "targetUri", "targetScope", "targetScopeUri");

  /**
   * Instantiates a new FHIR extra attribute util.
   */
  private FhirExtraAttributeUtil() {
    // Utility class
  }

  /* -------------------------------------------------------------------------------------------- */
  /* ValueSet */
  /* -------------------------------------------------------------------------------------------- */

  /**
   * Store extra top-level attributes from an R4 ValueSet on the subset.
   *
   * @param subset the subset
   * @param valueSet the value set
   */
  public static void storeExtraValueSetAttributesR4(final Subset subset,
    final org.hl7.fhir.r4.model.ValueSet valueSet) {
    storeExtraFromJson(subset, VS_ATTR_PREFIX, VS_HANDLED,
        FHIR_CONTEXT_R4.newJsonParser().encodeResourceToString(valueSet));
  }

  /**
   * Store extra top-level attributes from an R5 ValueSet on the subset.
   *
   * @param subset the subset
   * @param valueSet the value set
   */
  public static void storeExtraValueSetAttributesR5(final Subset subset,
    final org.hl7.fhir.r5.model.ValueSet valueSet) {
    storeExtraFromJson(subset, VS_ATTR_PREFIX, VS_HANDLED,
        FHIR_CONTEXT_R5.newJsonParser().encodeResourceToString(valueSet));
  }

  /**
   * Reconstruct extra top-level properties onto an R4 ValueSet from stored subset attributes.
   *
   * @param valueSet the value set built from handled fields
   * @param subset the subset
   * @return the value set with extra properties reconstructed
   */
  public static org.hl7.fhir.r4.model.ValueSet applyExtraToR4ValueSet(
    final org.hl7.fhir.r4.model.ValueSet valueSet, final Subset subset) {
    return applyExtra(valueSet, subset, VS_ATTR_PREFIX, "ValueSet", FHIR_CONTEXT_R4,
        org.hl7.fhir.r4.model.ValueSet.class);
  }

  /**
   * Reconstruct extra top-level properties onto an R5 ValueSet from stored subset attributes.
   *
   * @param valueSet the value set built from handled fields
   * @param subset the subset
   * @return the value set with extra properties reconstructed
   */
  public static org.hl7.fhir.r5.model.ValueSet applyExtraToR5ValueSet(
    final org.hl7.fhir.r5.model.ValueSet valueSet, final Subset subset) {
    return applyExtra(valueSet, subset, VS_ATTR_PREFIX, "ValueSet", FHIR_CONTEXT_R5,
        org.hl7.fhir.r5.model.ValueSet.class);
  }

  /* -------------------------------------------------------------------------------------------- */
  /* CodeSystem */
  /* -------------------------------------------------------------------------------------------- */

  /**
   * Store extra top-level attributes from a CodeSystem JSON tree on the terminology. The loader
   * parses the input directly, so the original JSON tree is passed through with full fidelity.
   *
   * @param terminology the terminology
   * @param root the parsed CodeSystem json
   */
  public static void storeExtraCodeSystemAttributes(final Terminology terminology,
    final JsonNode root) {
    storeExtraFromNode(terminology, CS_ATTR_PREFIX, CS_HANDLED, root);
  }

  /**
   * Reconstruct extra top-level properties onto an R4 CodeSystem from stored terminology
   * attributes.
   *
   * @param codeSystem the code system built from handled fields
   * @param terminology the terminology
   * @return the code system with extra properties reconstructed
   */
  public static org.hl7.fhir.r4.model.CodeSystem applyExtraToR4CodeSystem(
    final org.hl7.fhir.r4.model.CodeSystem codeSystem, final Terminology terminology) {
    return applyExtra(codeSystem, terminology, CS_ATTR_PREFIX, "CodeSystem", FHIR_CONTEXT_R4,
        org.hl7.fhir.r4.model.CodeSystem.class);
  }

  /**
   * Reconstruct extra top-level properties onto an R5 CodeSystem from stored terminology
   * attributes.
   *
   * @param codeSystem the code system built from handled fields
   * @param terminology the terminology
   * @return the code system with extra properties reconstructed
   */
  public static org.hl7.fhir.r5.model.CodeSystem applyExtraToR5CodeSystem(
    final org.hl7.fhir.r5.model.CodeSystem codeSystem, final Terminology terminology) {
    return applyExtra(codeSystem, terminology, CS_ATTR_PREFIX, "CodeSystem", FHIR_CONTEXT_R5,
        org.hl7.fhir.r5.model.CodeSystem.class);
  }

  /* -------------------------------------------------------------------------------------------- */
  /* ConceptMap */
  /* -------------------------------------------------------------------------------------------- */

  /**
   * Store extra top-level attributes from a ConceptMap JSON tree on the mapset. The loader parses
   * the input directly, so the original JSON tree is passed through with full fidelity.
   *
   * @param mapset the mapset
   * @param root the parsed ConceptMap json
   */
  public static void storeExtraConceptMapAttributes(final Mapset mapset, final JsonNode root) {
    storeExtraFromNode(mapset, CM_ATTR_PREFIX, CM_HANDLED, root);
  }

  /**
   * Reconstruct extra top-level properties onto an R4 ConceptMap from stored mapset attributes.
   *
   * @param conceptMap the concept map built from handled fields
   * @param mapset the mapset
   * @return the concept map with extra properties reconstructed
   */
  public static org.hl7.fhir.r4.model.ConceptMap applyExtraToR4ConceptMap(
    final org.hl7.fhir.r4.model.ConceptMap conceptMap, final Mapset mapset) {
    return applyExtra(conceptMap, mapset, CM_ATTR_PREFIX, "ConceptMap", FHIR_CONTEXT_R4,
        org.hl7.fhir.r4.model.ConceptMap.class);
  }

  /**
   * Reconstruct extra top-level properties onto an R5 ConceptMap from stored mapset attributes.
   *
   * @param conceptMap the concept map built from handled fields
   * @param mapset the mapset
   * @return the concept map with extra properties reconstructed
   */
  public static org.hl7.fhir.r5.model.ConceptMap applyExtraToR5ConceptMap(
    final org.hl7.fhir.r5.model.ConceptMap conceptMap, final Mapset mapset) {
    return applyExtra(conceptMap, mapset, CM_ATTR_PREFIX, "ConceptMap", FHIR_CONTEXT_R5,
        org.hl7.fhir.r5.model.ConceptMap.class);
  }

  /* -------------------------------------------------------------------------------------------- */
  /* Generic core */
  /* -------------------------------------------------------------------------------------------- */

  /**
   * Store extra attributes from a serialized resource JSON string.
   *
   * @param entity the entity to store attributes on
   * @param prefix the attribute key prefix
   * @param handled the handled (excluded) top-level field names
   * @param json the serialized resource json
   */
  private static void storeExtraFromJson(final HasAttributes entity, final String prefix,
    final Set<String> handled, final String json) {
    try {
      storeExtraFromNode(entity, prefix, handled, ThreadLocalMapper.get().readTree(json));
    } catch (final Exception e) {
      LOGGER.warn("Failed to store extra {} attributes", prefix, e);
    }
  }

  /**
   * Store each unhandled top-level property of the resource JSON tree as an entity attribute keyed
   * {@code <prefix><propertyName>}.
   *
   * @param entity the entity to store attributes on
   * @param prefix the attribute key prefix
   * @param handled the handled (excluded) top-level field names
   * @param root the parsed resource json
   */
  private static void storeExtraFromNode(final HasAttributes entity, final String prefix,
    final Set<String> handled, final JsonNode root) {
    if (root == null || !root.isObject()) {
      return;
    }
    final Iterator<String> names = root.fieldNames();
    while (names.hasNext()) {
      final String field = names.next();
      if (isHandled(field, handled)) {
        continue;
      }
      entity.getAttributes().put(prefix + field, root.get(field).toString());
    }
  }

  /**
   * Whether a top-level JSON field is already handled and should not be preserved as an extra
   * attribute. Primitive-extension siblings ({@code _field}) of handled fields are also excluded so
   * their partial elements do not overwrite handled values on reconstruction.
   *
   * @param field the json field name
   * @param handled the handled field names
   * @return true, if handled
   */
  private static boolean isHandled(final String field, final Set<String> handled) {
    if (handled.contains(field)) {
      return true;
    }
    return field.startsWith("_") && handled.contains(field.substring(1));
  }

  /**
   * Reassemble the stored extra attributes into a JSON resource object node.
   *
   * @param entity the entity holding attributes
   * @param prefix the attribute key prefix
   * @param resourceType the FHIR resource type name
   * @return the object node, or null if there are no extra attributes
   */
  private static ObjectNode collectExtras(final HasAttributes entity, final String prefix,
    final String resourceType) {
    final Map<String, String> attributes = entity.getAttributes();
    if (attributes == null || attributes.isEmpty()) {
      return null;
    }
    final ObjectNode node = ThreadLocalMapper.get().createObjectNode();
    node.put("resourceType", resourceType);
    boolean any = false;
    for (final Map.Entry<String, String> entry : attributes.entrySet()) {
      if (!entry.getKey().startsWith(prefix)) {
        continue;
      }
      final String field = entry.getKey().substring(prefix.length());
      try {
        node.set(field, ThreadLocalMapper.get().readTree(entry.getValue()));
        any = true;
      } catch (final Exception e) {
        LOGGER.warn("Failed to parse extra attribute {}", entry.getKey(), e);
      }
    }
    return any ? node : null;
  }

  /**
   * Reconstruct extra top-level properties onto a resource. The provided resource (already
   * populated with the handled fields) is serialized, merged with the preserved extra properties,
   * and re-parsed so complex structures are reconstructed via the HAPI parser.
   *
   * @param <T> the resource type
   * @param resource the resource built from handled fields
   * @param entity the entity holding attributes
   * @param prefix the attribute key prefix
   * @param resourceType the FHIR resource type name
   * @param context the FHIR context (R4 or R5)
   * @param type the resource class
   * @return the resource with extra properties reconstructed (the original instance if there are no
   *         extras or reconstruction fails)
   */
  private static <T extends org.hl7.fhir.instance.model.api.IBaseResource> T applyExtra(
    final T resource, final HasAttributes entity, final String prefix, final String resourceType,
    final FhirContext context, final Class<T> type) {
    final ObjectNode extras = collectExtras(entity, prefix, resourceType);
    if (extras == null) {
      return resource;
    }
    try {
      final IParser parser = context.newJsonParser();
      final ObjectNode full = mergeExtras(parser.encodeResourceToString(resource), extras);
      return parser.parseResource(type, full.toString());
    } catch (final Exception e) {
      LOGGER.warn("Failed to reconstruct extra {} attributes", resourceType, e);
      return resource;
    }
  }

  /**
   * Merge the preserved extra top-level fields into the serialized resource JSON. Handled fields
   * are never present in {@code extras}, so no handled field is overwritten.
   *
   * @param resourceJson the serialized resource (built from handled fields)
   * @param extras the extra top-level fields
   * @return the merged JSON object node
   */
  private static ObjectNode mergeExtras(final String resourceJson, final ObjectNode extras) {
    final ObjectNode full = (ObjectNode) tryReadTree(resourceJson);
    final Iterator<String> names = extras.fieldNames();
    while (names.hasNext()) {
      final String field = names.next();
      if ("resourceType".equals(field)) {
        continue;
      }
      full.set(field, extras.get(field));
    }
    return full;
  }

  /**
   * Read a JSON tree, wrapping checked exceptions.
   *
   * @param json the json
   * @return the json node
   */
  private static JsonNode tryReadTree(final String json) {
    try {
      return ThreadLocalMapper.get().readTree(json);
    } catch (final Exception e) {
      throw new IllegalStateException("Unable to parse resource JSON", e);
    }
  }
}
