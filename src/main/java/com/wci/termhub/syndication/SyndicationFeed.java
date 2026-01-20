/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The Class SyndicationFeed.
 */
@XmlRootElement(name = "feed")
public class SyndicationFeed {

  /** The title. */
  private String title;

  /** The entries. */
  private List<SyndicationFeedEntry> entries;

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
   * Gets the entries.
   *
   * @return the entries
   */
  @XmlElement(name = "entry", type = SyndicationFeedEntry.class)
  public List<SyndicationFeedEntry> getEntries() {
    return entries;
  }

  /**
   * Sets the entries.
   *
   * @param entries the new entries
   */
  public void setEntries(final List<SyndicationFeedEntry> entries) {
    this.entries = entries;
  }
}
