/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Represents a information about a terminology. Intended for use in ApplicationConfiguration.
 */
@Entity
@Table(name = "terminology_info")
@Schema(description = "Represents metadata and other info about a terminology. "
    + "This is for platform level tracking of terminologies and not tied to version.")
@Document(indexName = "terminology-info-v1")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TerminologyInfo extends AbstractHasModified
    implements HasLazyInit, Copyable<TerminologyInfo> {

  /** The type. This is the terminology abbreviation */
  @Column(nullable = false, length = 256)
  @Field(type = FieldType.Keyword)
  private String type;

  /** The name. */
  @Transient
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword)
  })
  private String name;

  /** The (html-enabled) description. */
  @Transient
  @Field(type = FieldType.Text)
  private String description;

  /** The publisher type. e.g. "TERMHUB", "NLM", "SNOMED" */
  @Column(nullable = false, length = 256)
  @Field(type = FieldType.Keyword)
  private String publisher;

  /** The license type. Different licenses may apply in different regions */
  @Transient
  @Field(type = FieldType.Keyword)
  private String license;

  /** The copyright. */
  @Transient
  @Field(type = FieldType.Keyword)
  private String copyright;

  // Identifiers
  /** The FHIR uri, or uri for use in RDF. */
  @Transient
  @Field(type = FieldType.Keyword)
  private String uri;

  /** The regions. */
  @Transient
  @Field(type = FieldType.Keyword)
  private List<String> regions;

  /** The labels. */
  @Transient
  @Field(type = FieldType.Keyword)
  private List<String> labels;

  /**
   * Other identifiers this terminology is known by for lookup, including FHIR OIDs, UMLS SAB, etc.
   */
  @Transient
  @Field(type = FieldType.Keyword)
  private List<String> identifiers;

  /**
   * For terminologies like UMLS, this gives us a chance to identify the sub-terminologies that are
   * "in scope".
   */
  @Transient
  @Field(type = FieldType.Keyword)
  private List<String> subTerminologies;

  // Links

  /** The contact uri. */
  @Transient
  @Field(type = FieldType.Keyword)
  private String contactUri;

  /** The download uri. */
  @Transient
  @Field(type = FieldType.Keyword)
  private String downloadUri;

  /** The documentation uri. */
  @Transient
  @Field(type = FieldType.Keyword)
  private String documentationUri;

  // FUTURE: standards that it is part of
  // content areas covered
  // languages

  /**
   * The ui labels. This is so we can say "descriptions" instead of "terms" when looking at SNOMED.
   * The set of labels can be managed by an enum if we want but is like this:
   *
   * <pre>
   * terms-label => "Descriptions"
   * hierarchies-label => "Trees"
   * relationships-label -> "Attributes"
   * axioms-label => "Axioms"
   * </pre>
   *
   * These are "overrides" so if not specified, default labels are still used.
   */
  @Transient
  @Field(type = FieldType.Object)
  private Map<MetaModel.Model, String> uiLabels;

  /**
   * Instantiates an empty {@link TerminologyInfo}.
   */
  public TerminologyInfo() {
    // n/a
  }

  /**
   * Instantiates a {@link TerminologyInfo} from the specified parameters.
   *
   * @param other the other
   */
  public TerminologyInfo(final TerminologyInfo other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  @Override
  public void populateFrom(final TerminologyInfo other) {
    super.populateFrom(other);
    type = other.getType();
    name = other.getName();
    description = other.getDescription();
    publisher = other.getPublisher();
    license = other.getLicense();
    copyright = other.getCopyright();
    uri = other.getUri();
    identifiers = new ArrayList<>(other.getIdentifiers());
    regions = new ArrayList<>(other.getRegions());
    labels = new ArrayList<>(other.getLabels());
    subTerminologies = new ArrayList<>(other.getSubTerminologies());
    contactUri = other.getContactUri();
    downloadUri = other.getDownloadUri();
    documentationUri = other.getDocumentationUri();
    uiLabels = new HashMap<>(other.getUiLabels());
  }

  /**
   * Patch from.
   *
   * @param other the other
   */
  @Override
  public void patchFrom(final TerminologyInfo other) {
    super.patchFrom(other);
    if (other.getType() != null) {
      type = other.getType();
    }
    if (other.getName() != null) {
      name = other.getName();
    }
    if (other.getDescription() != null) {
      description = other.getDescription();
    }
    if (other.getPublisher() != null) {
      publisher = other.getPublisher();
    }
    if (other.getLicense() != null) {
      license = other.getLicense();
    }
    if (other.getCopyright() != null) {
      copyright = other.getCopyright();
    }
    if (other.getUri() != null) {
      uri = other.getUri();
    }
    if (!other.getIdentifiers().isEmpty()) {
      // Supports add only via patch
      getIdentifiers().addAll(other.getIdentifiers());
    }
    if (!other.getRegions().isEmpty()) {
      // Supports add only via patch
      getRegions().addAll(other.getRegions());
    }
    if (!other.getLabels().isEmpty()) {
      // Supports add only via patch
      getLabels().addAll(other.getLabels());
    }
    if (!other.getSubTerminologies().isEmpty()) {
      // Supports add only via patch
      getSubTerminologies().addAll(other.getSubTerminologies());
    }
    if (other.getContactUri() != null) {
      contactUri = other.getContactUri();
    }
    if (other.getDownloadUri() != null) {
      downloadUri = other.getDownloadUri();
    }
    if (other.getDocumentationUri() != null) {
      documentationUri = other.getDocumentationUri();
    }
    if (!other.getUiLabels().isEmpty()) {
      // Supports add only via patch
      getUiLabels().putAll(other.getUiLabels());
    }

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
   * Sets the type.
   *
   * @param type the type
   */
  public void setType(final String type) {
    this.type = type;
  }

  /**
   * Returns the publisher.
   *
   * @return the publisher
   */
  public String getPublisher() {
    return publisher;
  }

  /**
   * Sets the publisher.
   *
   * @param publisher the publisher
   */
  public void setPublisher(final String publisher) {
    this.publisher = publisher;
  }

  /**
   * Returns the license.
   *
   * @return the license
   */
  public String getLicense() {
    return license;
  }

  /**
   * Sets the license.
   *
   * @param license the license
   */
  public void setLicense(final String license) {
    this.license = license;
  }

  /**
   * Returns the copyright.
   *
   * @return the copyright
   */
  public String getCopyright() {
    return copyright;
  }

  /**
   * Sets the copyright.
   *
   * @param copyright the copyright
   */
  public void setCopyright(final String copyright) {
    this.copyright = copyright;
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
   * Sets the uri.
   *
   * @param uri the uri
   */
  public void setUri(final String uri) {
    this.uri = uri;
  }

  /**
   * Returns the identifiers.
   *
   * @return the identifiers
   */
  public List<String> getIdentifiers() {
    if (identifiers == null) {
      identifiers = new ArrayList<>();
    }
    return identifiers;
  }

  /**
   * Sets the identifiers.
   *
   * @param identifiers the new identifiers
   */
  public void setIdentifiers(final List<String> identifiers) {
    this.identifiers = identifiers;
  }

  /**
   * Gets the regions.
   *
   * @return the regions
   */
  public List<String> getRegions() {
    if (regions == null) {
      regions = new ArrayList<>();
    }
    return regions;
  }

  /**
   * Sets the regions.
   *
   * @param regions the new regions
   */
  public void setRegions(final List<String> regions) {
    this.regions = regions;
  }

  /**
   * Gets the labels.
   *
   * @return the labels
   */
  public List<String> getLabels() {
    if (labels == null) {
      labels = new ArrayList<>();
    }
    return labels;
  }

  /**
   * Sets the labels.
   *
   * @param labels the new labels
   */
  public void setLabels(final List<String> labels) {
    this.labels = labels;
  }

  /**
   * Gets the sub terminologies.
   *
   * @return the sub terminologies
   */
  public List<String> getSubTerminologies() {
    if (subTerminologies == null) {
      subTerminologies = new ArrayList<>();
    }
    return subTerminologies;
  }

  /**
   * Sets the sub terminologies.
   *
   * @param subTerminologies the new sub terminologies
   */
  public void setSubTerminologies(final List<String> subTerminologies) {
    this.subTerminologies = subTerminologies;
  }

  /**
   * Returns the contact uri.
   *
   * @return the contact uri
   */
  public String getContactUri() {
    return contactUri;
  }

  /**
   * Sets the contact uri.
   *
   * @param contactUri the contact uri
   */
  public void setContactUri(final String contactUri) {
    this.contactUri = contactUri;
  }

  /**
   * Returns the download uri.
   *
   * @return the download uri
   */
  public String getDownloadUri() {
    return downloadUri;
  }

  /**
   * Sets the download uri.
   *
   * @param downloadUri the download uri
   */
  public void setDownloadUri(final String downloadUri) {
    this.downloadUri = downloadUri;
  }

  /**
   * Returns the documentation uri.
   *
   * @return the documentation uri
   */
  public String getDocumentationUri() {
    return documentationUri;
  }

  /**
   * Sets the documentation uri.
   *
   * @param documentationUri the documentation uri
   */
  public void setDocumentationUri(final String documentationUri) {
    this.documentationUri = documentationUri;
  }

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the name
   */
  public void setName(final String name) {
    this.name = name;
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
   * Sets the description.
   *
   * @param description the description
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * Returns the ui labels.
   *
   * @return the ui labels
   */
  public Map<MetaModel.Model, String> getUiLabels() {
    if (uiLabels == null) {
      uiLabels = new HashMap<>();
    }
    return uiLabels;
  }

  /**
   * Sets the ui labels.
   *
   * @param uiLabels the ui labels
   */
  public void setUiLabels(final Map<MetaModel.Model, String> uiLabels) {
    this.uiLabels = uiLabels;
  }

  // /* see superclass */
  // @Override
  // public void unmarshall() throws Exception {
  // final TerminologyInfo ti = ModelUtility.fromJson(getData(),
  // this.getClass());
  // populateFrom(ti);
  // }

  /* see superclass */
  @Override
  public void lazyInit() {
    // n/a
  }

}
