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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wci.termhub.util.ThreadLocalMapper;

/**
 * Version-neutral parse/compare helpers for FHIR contact JSON stored in terminology attributes.
 */
public final class FhirContactUtility {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(FhirContactUtility.class);

  /**
   * Instantiates an empty {@link FhirContactUtility}.
   */
  private FhirContactUtility() {
    // n/a
  }

  /**
   * Parses the fhirContact attribute JSON array.
   *
   * @param fhirContact the fhirContact attribute value
   * @return contact JSON nodes, or empty when absent or invalid
   */
  public static List<JsonNode> parseArray(final String fhirContact) {
    if (fhirContact == null || fhirContact.isEmpty()) {
      return Collections.emptyList();
    }
    try {
      final JsonNode arr = ThreadLocalMapper.get().readTree(fhirContact);
      if (!arr.isArray()) {
        return Collections.emptyList();
      }
      final List<JsonNode> contacts = new ArrayList<>();
      for (final JsonNode item : arr) {
        contacts.add(item);
      }
      return contacts;
    } catch (final Exception e) {
      LOGGER.warn("Failed to parse fhirContact", e);
      return Collections.emptyList();
    }
  }

  /**
   * Returns true when two contact JSON nodes represent the same contact detail.
   *
   * @param a the first contact node
   * @param b the second contact node
   * @return true when equivalent
   */
  public static boolean isEquivalent(final JsonNode a, final JsonNode b) {
    return signature(a).equals(signature(b));
  }

  /**
   * Returns true when an equivalent contact is already in the list.
   *
   * @param existing the existing contact nodes
   * @param candidate the candidate contact node
   * @return true when a match exists
   */
  public static boolean containsEquivalent(final List<JsonNode> existing, final JsonNode candidate) {
    for (final JsonNode contact : existing) {
      if (isEquivalent(contact, candidate)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true when an equivalent contact signature is already present.
   *
   * @param existingSignatures the existing contact signatures
   * @param candidate the candidate contact node
   * @return true when a match exists
   */
  public static boolean containsEquivalentSignature(final Set<String> existingSignatures,
    final JsonNode candidate) {
    return existingSignatures.contains(signature(candidate));
  }

  /**
   * Builds a normalized signature for contact comparison.
   *
   * @param contactNode the contact node
   * @return the signature
   */
  public static String signature(final JsonNode contactNode) {
    final StringBuilder sb = new StringBuilder();
    if (!contactNode.path("name").isMissingNode() && !contactNode.path("name").isNull()) {
      sb.append('n').append(contactNode.path("name").asText()).append('\n');
    }
    if (contactNode.has("telecom") && contactNode.get("telecom").isArray()) {
      for (final JsonNode tp : contactNode.get("telecom")) {
        sb.append('t').append(tp.path("system").asText()).append('\t')
            .append(tp.path("value").asText()).append('\n');
      }
    }
    return sb.toString();
  }

  /**
   * Collects signatures for the supplied contact nodes.
   *
   * @param contacts the contact nodes
   * @return signature set
   */
  public static Set<String> signatures(final List<JsonNode> contacts) {
    final Set<String> result = new HashSet<>();
    for (final JsonNode contact : contacts) {
      result.add(signature(contact));
    }
    return result;
  }

  /**
   * Serializes contact nodes to the fhirContact attribute JSON format.
   *
   * @param contacts the contact nodes
   * @return JSON array string
   */
  public static String serializeArray(final List<JsonNode> contacts) {
    final ArrayNode arr = ThreadLocalMapper.get().createArrayNode();
    for (final JsonNode contact : contacts) {
      arr.add(contact);
    }
    return arr.toString();
  }

  /**
   * Builds a contact JSON node.
   *
   * @param name optional contact name
   * @param telecomSystem telecom system code
   * @param telecomValue telecom value
   * @return the contact node
   */
  public static JsonNode toContactNode(final String name, final String telecomSystem,
    final String telecomValue) {
    final ObjectNode contact = ThreadLocalMapper.get().createObjectNode();
    if (name != null && !name.isEmpty()) {
      contact.put("name", name);
    }
    final ArrayNode telecom = ThreadLocalMapper.get().createArrayNode();
    final ObjectNode tp = ThreadLocalMapper.get().createObjectNode();
    if (telecomSystem != null && !telecomSystem.isEmpty()) {
      tp.put("system", telecomSystem);
    }
    if (telecomValue != null && !telecomValue.isEmpty()) {
      tp.put("value", telecomValue);
    }
    telecom.add(tp);
    contact.set("telecom", telecom);
    return contact;
  }

  /**
   * Serializes R4 contact details to the fhirContact attribute JSON format.
   *
   * @param contacts the contact details
   * @return JSON array string, or null when empty
   */
  public static String fromR4Contacts(
    final List<org.hl7.fhir.r4.model.ContactDetail> contacts) {
    if (contacts == null || contacts.isEmpty()) {
      return null;
    }
    final List<JsonNode> nodes = new ArrayList<>();
    for (final org.hl7.fhir.r4.model.ContactDetail contact : contacts) {
      nodes.add(toJsonNode(contact));
    }
    return serializeArray(nodes);
  }

  /**
   * Serializes R5 contact details to the fhirContact attribute JSON format.
   *
   * @param contacts the contact details
   * @return JSON array string, or null when empty
   */
  public static String fromR5Contacts(
    final List<org.hl7.fhir.r5.model.ContactDetail> contacts) {
    if (contacts == null || contacts.isEmpty()) {
      return null;
    }
    final List<JsonNode> nodes = new ArrayList<>();
    for (final org.hl7.fhir.r5.model.ContactDetail contact : contacts) {
      nodes.add(toJsonNode(contact));
    }
    return serializeArray(nodes);
  }

  private static JsonNode toJsonNode(final org.hl7.fhir.r4.model.ContactDetail contact) {
    final ObjectNode obj = ThreadLocalMapper.get().createObjectNode();
    if (contact.hasName()) {
      obj.put("name", contact.getName());
    }
    final ArrayNode telecom = ThreadLocalMapper.get().createArrayNode();
    for (final org.hl7.fhir.r4.model.ContactPoint cp : contact.getTelecom()) {
      telecom.add(toJsonNode(cp));
    }
    obj.set("telecom", telecom);
    return obj;
  }

  private static JsonNode toJsonNode(final org.hl7.fhir.r5.model.ContactDetail contact) {
    final ObjectNode obj = ThreadLocalMapper.get().createObjectNode();
    if (contact.hasName()) {
      obj.put("name", contact.getName());
    }
    final ArrayNode telecom = ThreadLocalMapper.get().createArrayNode();
    for (final org.hl7.fhir.r5.model.ContactPoint cp : contact.getTelecom()) {
      telecom.add(toJsonNode(cp));
    }
    obj.set("telecom", telecom);
    return obj;
  }

  private static JsonNode toJsonNode(final org.hl7.fhir.r4.model.ContactPoint cp) {
    final ObjectNode tp = ThreadLocalMapper.get().createObjectNode();
    if (cp.hasSystem()) {
      tp.put("system", cp.getSystem().toCode());
    }
    if (cp.hasValue()) {
      tp.put("value", cp.getValue());
    }
    return tp;
  }

  private static JsonNode toJsonNode(final org.hl7.fhir.r5.model.ContactPoint cp) {
    final ObjectNode tp = ThreadLocalMapper.get().createObjectNode();
    if (cp.hasSystem()) {
      tp.put("system", cp.getSystem().toCode());
    }
    if (cp.hasValue()) {
      tp.put("value", cp.getValue());
    }
    return tp;
  }
}
