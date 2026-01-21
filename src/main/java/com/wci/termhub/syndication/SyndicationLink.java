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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * The Class SyndicationLink.
 */
@XmlType
public class SyndicationLink {

  /**
   * The Enum RelType.
   */
  public enum RelType {
    /** The alternate. */
    alternate,
    /** The related. */
    related
  }

  /** The rel. */
  private RelType rel;

  /** The type. */
  private String type;

  /** The href. */
  private String href;

  /** The length. */
  private String length;

  /**
   * Gets the rel.
   *
   * @return the rel
   */
  @XmlAttribute
  public RelType getRel() {
    return rel;
  }

  /**
   * Sets the rel.
   *
   * @param rel the new rel
   */
  public void setRel(final RelType rel) {
    this.rel = rel;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  @XmlAttribute
  public String getType() {
    return type;
  }

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(final String type) {
    this.type = type;
  }

  /**
   * Gets the href.
   *
   * @return the href
   */
  @XmlAttribute
  public String getHref() {
    return href;
  }

  /**
   * Sets the href.
   *
   * @param href the new href
   */
  public void setHref(final String href) {
    this.href = href;
  }

  /**
   * Gets the length.
   *
   * @return the length
   */
  @XmlAttribute
  public String getLength() {
    return length;
  }

  /**
   * Sets the length.
   *
   * @param length the new length
   */
  public void setLength(final String length) {
    this.length = length;
  }
}
