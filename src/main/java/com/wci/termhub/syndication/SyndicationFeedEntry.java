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

  /** The links. */
  private List<SyndicationLink> links;

  /** The category. */
  private SyndicationCategory category;

  /** The author. */
  private SyndicationAuthor author;

  /** The id. */
  private String id;

  /** The rights. */
  private String rights;

  /** The updated. */
  private String updated;

  /** The published. */
  private String published;

  /** The summary. */
  private String summary;

  /** The content item identifier. */
  private String contentItemIdentifier;

  /** The content item version. */
  private String contentItemVersion;

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
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the summary.
   *
   * @return the summary
   */
  public String getSummary() {
    return summary;
  }

  /**
   * Sets the summary.
   *
   * @param summary the new summary
   */
  public void setSummary(final String summary) {
    this.summary = summary;
  }

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(final String id) {
    this.id = id;
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
   * Gets the author.
   *
   * @return the author
   */
  @XmlElement(name = "author", type = SyndicationAuthor.class)
  public SyndicationAuthor getAuthor() {
    return author;
  }

  /**
   * Sets the author.
   *
   * @param author the new author
   */
  public void setAuthor(final SyndicationAuthor author) {
    this.author = author;
  }

  /**
   * Gets the rights.
   *
   * @return the rights
   */
  public String getRights() {
    return rights;
  }

  /**
   * Sets the rights.
   *
   * @param rights the new rights
   */
  public void setRights(final String rights) {
    this.rights = rights;
  }

  /**
   * Gets the updated.
   *
   * @return the updated
   */
  public String getUpdated() {
    return updated;
  }

  /**
   * Sets the updated.
   *
   * @param updated the new updated
   */
  public void setUpdated(final String updated) {
    this.updated = updated;
  }

  /**
   * Gets the published.
   *
   * @return the published
   */
  public String getPublished() {
    return published;
  }

  /**
   * Sets the published.
   *
   * @param published the new published
   */
  public void setPublished(final String published) {
    this.published = published;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    return Objects.hash(author, category, contentItemIdentifier, contentItemVersion, id, links,
        packageDependency, published, rights, summary, title, updated);
  }

  /* see superclass */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }
    final SyndicationFeedEntry other = (SyndicationFeedEntry) obj;
    return Objects.equals(author, other.author) && Objects.equals(category, other.category)
        && Objects.equals(contentItemIdentifier, other.contentItemIdentifier)
        && Objects.equals(contentItemVersion, other.contentItemVersion)
        && Objects.equals(id, other.id) && Objects.equals(links, other.links)
        && Objects.equals(packageDependency, other.packageDependency)
        && Objects.equals(published, other.published) && Objects.equals(rights, other.rights)
        && Objects.equals(summary, other.summary) && Objects.equals(title, other.title)
        && Objects.equals(updated, other.updated);
  }

}
