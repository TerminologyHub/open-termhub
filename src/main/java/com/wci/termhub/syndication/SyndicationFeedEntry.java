/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication;

import java.util.List;
import java.util.Objects;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The Class SyndicationFeedEntry.
 */
@XmlRootElement(name = "entry")
public class SyndicationFeedEntry {

  /** The title. */
  private String title;

  /** The content item identifier. */
  private String contentItemIdentifier;

  /** The content item version. */
  private String contentItemVersion;

  /** The category. */
  private SyndicationCategory category;

  /** The links. */
  private List<SyndicationLink> links;

  /** The package dependency. */
  private SyndicationDependency packageDependency;

  /**
   * Gets the zip link.
   *
   * @return the zip link
   */
  public SyndicationLink getZipLink() {
    if (links != null) {
      for (final SyndicationLink link : links) {
        if ("application/zip".equals(link.getType())) {
          return link;
        }
      }
    }
    return null;
  }

  /**
   * Gets the title.
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title.
   *
   * @param title the new title
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * Gets the content item identifier.
   *
   * @return the content item identifier
   */
  @XmlElement(namespace = "http://ns.electronichealth.net.au/ncts/syndication/asf/extensions/1.0.0",
      name = "contentItemIdentifier")
  public String getContentItemIdentifier() {
    return contentItemIdentifier;
  }

  /**
   * Sets the content item identifier.
   *
   * @param contentItemIdentifier the new content item identifier
   */
  public void setContentItemIdentifier(final String contentItemIdentifier) {
    this.contentItemIdentifier = contentItemIdentifier;
  }

  /**
   * Gets the content item version.
   *
   * @return the content item version
   */
  @XmlElement(namespace = "http://ns.electronichealth.net.au/ncts/syndication/asf/extensions/1.0.0",
      name = "contentItemVersion")
  public String getContentItemVersion() {
    return contentItemVersion;
  }

  /**
   * Sets the content item version.
   *
   * @param contentItemVersion the new content item version
   */
  public void setContentItemVersion(final String contentItemVersion) {
    this.contentItemVersion = contentItemVersion;
  }

  /**
   * Gets the category.
   *
   * @return the category
   */
  public SyndicationCategory getCategory() {
    return category;
  }

  /**
   * Sets the category.
   *
   * @param category the new category
   */
  public void setCategory(final SyndicationCategory category) {
    this.category = category;
  }

  /**
   * Gets the links.
   *
   * @return the links
   */
  @XmlElement(name = "link", type = SyndicationLink.class)
  public List<SyndicationLink> getLinks() {
    return links;
  }

  /**
   * Sets the links.
   *
   * @param links the new links
   */
  public void setLinks(final List<SyndicationLink> links) {
    this.links = links;
  }

  /**
   * Gets the package dependency.
   *
   * @return the package dependency
   */
  @XmlElement(namespace = "http://terminologyhub.com/syndication/th-extension/1.0.0",
      name = "packageDependency")
  public SyndicationDependency getPackageDependency() {
    return packageDependency;
  }

  /**
   * Sets the package dependency.
   *
   * @param packageDependency the new package dependency
   */
  public void setPackageDependency(final SyndicationDependency packageDependency) {
    this.packageDependency = packageDependency;
  }

  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SyndicationFeedEntry entry = (SyndicationFeedEntry) o;
    return Objects.equals(title, entry.title)
        && Objects.equals(contentItemIdentifier, entry.contentItemIdentifier)
        && Objects.equals(contentItemVersion, entry.contentItemVersion)
        && Objects.equals(category, entry.category);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(title, contentItemIdentifier, contentItemVersion, category);
  }
}
