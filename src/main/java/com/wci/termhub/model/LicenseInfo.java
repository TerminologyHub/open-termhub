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
 * Represents a license type for published content. This is configuration
 * information to better describe and access license resource information
 * supported by termhub.
 */
@Entity
@Table(name = "license_info")
@Schema(description = "Represents a type of license for published content.")
@Document(indexName = "license-info-v1")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LicenseInfo extends AbstractHasModified implements HasLazyInit, Copyable<LicenseInfo> {

  /** The type. e.g. UMLS, MLDS, AMA, APACHE2 */
  @Column(nullable = false, length = 256)
  @Field(type = FieldType.Keyword)
  private String type;

  /** The human readable name of the license type. */
  @Transient
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword)
  })
  private String name;

  /** The version. */
  @Column(nullable = false, length = 256)
  @Field(type = FieldType.Keyword)
  private String version;

  /** The license text if textual. */
  @Transient
  @Field(type = FieldType.Object, enabled = false)
  private String text;

  /** The text mime type. e.g. text/plain or text/html */
  @Transient
  @Field(type = FieldType.Object, enabled = false)
  private String textMimeType;

  /** The uri pointing to information about the license. */
  @Transient
  @Field(type = FieldType.Object, enabled = false)
  private String infoUri;

  /** The uri pointing to the license agreement itself. */
  @Transient
  @Field(type = FieldType.Object, enabled = false)
  private String licenseUri;

  /** An indcator of whether the license is open source. */
  @Transient
  @Field(type = FieldType.Boolean)
  private Boolean openSource;

  /** the URI of the license. */
  @Transient
  @Field(type = FieldType.Keyword)
  private String textUri;

  // NOTE: other things can be
  // 1. payment terms,
  // 2. the "period" the license is good
  // for (and if there's a calendar period or it starts when you agree to it,
  // etc).
  // 3. where the license is valid (e.g. member territories)
  // 4. cost of the license
  // 5. license -> sub-license relationships (e.g. SNOMEDCT-TERMHUB)
  // ...
  //

  /**
   * Instantiates an empty {@link LicenseInfo}.
   */
  public LicenseInfo() {
    // n/a
  }

  /**
   * Instantiates a {@link LicenseInfo} from the specified parameters.
   *
   * @param other the other
   */
  public LicenseInfo(final LicenseInfo other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  @Override
  public void populateFrom(final LicenseInfo other) {
    super.populateFrom(other);
    type = other.getType();
    name = other.getName();
    version = other.getVersion();
    text = other.getText();
    textMimeType = other.getTextMimeType();
    infoUri = other.getInfoUri();
    licenseUri = other.getLicenseUri();
    openSource = other.getOpenSource();
    textUri = other.getTextUri();
  }

  /* see superclass */
  @Override
  public void patchFrom(final LicenseInfo other) {
    super.patchFrom(other);
    if (other.getType() != null) {
      type = other.getType();
    }
    if (other.getName() != null) {
      name = other.getName();
    }
    if (other.getVersion() != null) {
      version = other.getVersion();
    }
    if (other.getText() != null) {
      text = other.getText();
    }
    if (other.getTextMimeType() != null) {
      text = other.getTextMimeType();
    }
    if (other.getInfoUri() != null) {
      infoUri = other.getInfoUri();
    }
    if (other.getLicenseUri() != null) {
      licenseUri = other.getLicenseUri();
    }
    if (other.getOpenSource() != null) {
      openSource = other.getOpenSource();
    }
    if (other.getTextUri() != null) {
      textUri = other.getTextUri();
    }
  }

  // /* see superclass */
  // @Override
  // public void unmarshall() throws Exception {
  // final LicenseInfo li = ModelUtility.fromJson(getData(), this.getClass());
  // populateFrom(li);
  // }

  /* see superclass */
  @Override
  public void lazyInit() {
    // n/a
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
   * Returns the version.
   *
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Sets the version.
   *
   * @param version the version
   */
  public void setVersion(final String version) {
    this.version = version;
  }

  /**
   * Returns the text.
   *
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * Sets the text.
   *
   * @param text the text
   */
  public void setText(final String text) {
    this.text = text;
  }

  /**
   * Returns the text mime type.
   *
   * @return the text mime type
   */
  public String getTextMimeType() {
    return textMimeType;
  }

  /**
   * Sets the text mime type.
   *
   * @param textMimeType the text mime type
   */
  public void setTextMimeType(final String textMimeType) {
    this.textMimeType = textMimeType;
  }

  /**
   * Returns the info uri.
   *
   * @return the info uri
   */
  public String getInfoUri() {
    return infoUri;
  }

  /**
   * Sets the info uri.
   *
   * @param infoUri the info uri
   */
  public void setInfoUri(final String infoUri) {
    this.infoUri = infoUri;
  }

  /**
   * Returns the text uri.
   *
   * @return the text uri
   */
  public String getTextUri() {
    return textUri;
  }

  /**
   * Sets the text uri.
   *
   * @param textUri the text uri
   */
  public void setTextUri(final String textUri) {
    this.textUri = textUri;
  }

  /**
   * Returns the open source.
   *
   * @return the open source
   */
  public Boolean getOpenSource() {
    return openSource;
  }

  /**
   * Sets the open source.
   *
   * @param openSource the open source
   */
  public void setOpenSource(final Boolean openSource) {
    this.openSource = openSource;
  }

  /**
   * Returns the license uri.
   *
   * @return the license uri
   */
  public String getLicenseUri() {
    return licenseUri;
  }

  /**
   * Sets the license uri.
   *
   * @param licenseUri the license uri
   */
  public void setLicenseUri(final String licenseUri) {
    this.licenseUri = licenseUri;
  }

}
