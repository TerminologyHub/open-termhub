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

import java.util.Date;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.wci.termhub.util.ModelUtility;

/**
 * Model for tracking syndicated content that has been loaded.
 */
@Document(indexName = "syndicated-content-v1")
public class SyndicatedContent implements HasId {

  /** The id. */
  @Field(type = FieldType.Keyword)
  private String id;

  /** The entry id from syndication feed. */
  @Field(type = FieldType.Keyword)
  private String entryId;

  /** The content item identifier. */
  @Field(type = FieldType.Keyword)
  private String contentItemIdentifier;

  /** The content item version. */
  @Field(type = FieldType.Keyword)
  private String contentItemVersion;

  /** The content type. */
  @Field(type = FieldType.Keyword)
  private String contentType;

  /** The title. */
  @Field(type = FieldType.Text)
  private String title;

  /** The loaded timestamp. */
  @Field(type = FieldType.Date)
  private Date loadedTimestamp;

  /** The syndication feed url. */
  @Field(type = FieldType.Keyword)
  private String syndicationFeedUrl;


  /** The resource key (identifier + version). */
  @Field(type = FieldType.Keyword)
  private String resourceKey;

  /**
   * Instantiates an empty {@link SyndicatedContent}.
   */
  public SyndicatedContent() {
    // Default constructor
  }

  /**
   * Instantiates a {@link SyndicatedContent} from the specified parameters.
   *
   * @param entryId the entry id
   * @param contentItemIdentifier the content item identifier
   * @param contentItemVersion the content item version
   * @param contentType the content type
   * @param title the title
   */
  public SyndicatedContent(final String entryId, final String contentItemIdentifier,
      final String contentItemVersion, final String contentType, final String title) {
    this.entryId = entryId;
    this.contentItemIdentifier = contentItemIdentifier;
    this.contentItemVersion = contentItemVersion;
    this.contentType = contentType;
    this.title = title;
    this.loadedTimestamp = new Date();
    this.resourceKey = contentItemIdentifier + "|" + contentItemVersion;
  }

  /**
   * Returns the id.
   *
   * @return the id
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the id
   */
  @Override
  public void setId(final String id) {
    this.id = id;
  }

  /**
   * Returns the entry id.
   *
   * @return the entry id
   */
  public String getEntryId() {
    return entryId;
  }

  /**
   * Sets the entry id.
   *
   * @param entryId the entry id
   */
  public void setEntryId(final String entryId) {
    this.entryId = entryId;
  }

  /**
   * Returns the content item identifier.
   *
   * @return the content item identifier
   */
  public String getContentItemIdentifier() {
    return contentItemIdentifier;
  }

  /**
   * Sets the content item identifier.
   *
   * @param contentItemIdentifier the content item identifier
   */
  public void setContentItemIdentifier(final String contentItemIdentifier) {
    this.contentItemIdentifier = contentItemIdentifier;
  }

  /**
   * Returns the content item version.
   *
   * @return the content item version
   */
  public String getContentItemVersion() {
    return contentItemVersion;
  }

  /**
   * Sets the content item version.
   *
   * @param contentItemVersion the content item version
   */
  public void setContentItemVersion(final String contentItemVersion) {
    this.contentItemVersion = contentItemVersion;
  }

  /**
   * Returns the content type.
   *
   * @return the content type
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Sets the content type.
   *
   * @param contentType the content type
   */
  public void setContentType(final String contentType) {
    this.contentType = contentType;
  }

  /**
   * Returns the title.
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title.
   *
   * @param title the title
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * Returns the loaded timestamp.
   *
   * @return the loaded timestamp
   */
  public Date getLoadedTimestamp() {
    return loadedTimestamp;
  }

  /**
   * Sets the loaded timestamp.
   *
   * @param loadedTimestamp the loaded timestamp
   */
  public void setLoadedTimestamp(final Date loadedTimestamp) {
    this.loadedTimestamp = loadedTimestamp;
  }

  /**
   * Returns the syndication feed url.
   *
   * @return the syndication feed url
   */
  public String getSyndicationFeedUrl() {
    return syndicationFeedUrl;
  }

  /**
   * Sets the syndication feed url.
   *
   * @param syndicationFeedUrl the syndication feed url
   */
  public void setSyndicationFeedUrl(final String syndicationFeedUrl) {
    this.syndicationFeedUrl = syndicationFeedUrl;
  }

  /**
   * Returns the confidence (not used in syndication).
   *
   * @return null (confidence not applicable for syndicated content)
   */
  @Override
  public Double getConfidence() {
    return null;
  }

  /**
   * Sets the confidence (not used in syndication).
   *
   * @param confidence the confidence (ignored)
   */
  @Override
  public void setConfidence(final Double confidence) {
    // Confidence is not used in syndicated content
  }


  /**
   * Sets the resource key.
   *
   * @param resourceKey the resource key
   */
  public void setResourceKey(final String resourceKey) {
    this.resourceKey = resourceKey;
  }

  /**
   * Returns the resource key (identifier + version).
   *
   * @return the resource key
   */
  public String getResourceKey() {
    if (resourceKey != null) {
      return resourceKey;
    }
    return contentItemIdentifier + "|" + contentItemVersion;
  }

  @Override
  public String toString() {
    try {
      return ModelUtility.toJson(this);
    } catch (final Exception e) {
      // logger.error("Unexpected error serializing object", e);
      throw new RuntimeException(e);
    }
  }
}
