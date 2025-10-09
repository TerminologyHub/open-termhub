/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication.test;

import java.util.Collections;
import java.util.Date;

import com.wci.termhub.syndication.SyndicationCategory;
import com.wci.termhub.syndication.SyndicationFeedEntry;
import com.wci.termhub.syndication.SyndicationLink;

/**
 * The Class SyndicationTestDataHelper.
 */
public final class SyndicationTestDataHelper {

  /** The Constant CATEGORY_CODE_SYSTEM. */
  private static final String CATEGORY_CODE_SYSTEM = "CODESYSTEM_FHIRR5_ALL";

  /** The Constant CATEGORY_VALUE_SET. */
  private static final String CATEGORY_VALUE_SET = "VALUESET_FHIRR5_ALL";

  /** The Constant CATEGORY_CONCEPT_MAP. */
  private static final String CATEGORY_CONCEPT_MAP = "CONCEPTMAP_FHIRR5_ALL";

  /**
   * Instantiates a new syndication test data helper.
   */
  private SyndicationTestDataHelper() {
    // utility class
  }

  /**
   * Creates the code system entry.
   *
   * @param id the id
   * @param title the title
   * @param publisher the publisher
   * @param version the version
   * @param identifier the identifier
   * @param fileName the file name
   * @return the syndication feed entry
   */
  public static SyndicationFeedEntry createCodeSystemEntry(final String id, final String title,
    final String publisher, final String version, final String identifier, final String fileName) {
    return createEntry(id, title, publisher, version, identifier, fileName, CATEGORY_CODE_SYSTEM);
  }

  /**
   * Creates the value set entry.
   *
   * @param id the id
   * @param title the title
   * @param publisher the publisher
   * @param version the version
   * @param identifier the identifier
   * @param fileName the file name
   * @return the syndication feed entry
   */
  public static SyndicationFeedEntry createValueSetEntry(final String id, final String title,
    final String publisher, final String version, final String identifier, final String fileName) {
    return createEntry(id, title, publisher, version, identifier, fileName, CATEGORY_VALUE_SET);
  }

  /**
   * Creates the concept map entry.
   *
   * @param id the id
   * @param title the title
   * @param publisher the publisher
   * @param version the version
   * @param identifier the identifier
   * @param fileName the file name
   * @return the syndication feed entry
   */
  public static SyndicationFeedEntry createConceptMapEntry(final String id, final String title,
    final String publisher, final String version, final String identifier, final String fileName) {
    return createEntry(id, title, publisher, version, identifier, fileName, CATEGORY_CONCEPT_MAP);
  }

  /**
   * Creates the entry.
   *
   * @param id the id
   * @param title the title
   * @param publisher the publisher
   * @param version the version
   * @param identifier the identifier
   * @param fileName the file name
   * @param categoryTerm the category term
   * @return the syndication feed entry
   */
  private static SyndicationFeedEntry createEntry(final String id, final String title,
    final String publisher, final String version, final String identifier, final String fileName,
    final String categoryTerm) {

    final SyndicationFeedEntry entry = new SyndicationFeedEntry();
    entry.setId(id);
    entry.setTitle(title);
    entry.setContentItemIdentifier(identifier);
    entry.setContentItemVersion(version);
    entry.setUpdated(new Date().toString());
    entry.setPublished(new Date().toString());

    final SyndicationCategory category = new SyndicationCategory();
    category.setLabel(title);
    category.setScheme("http://ns.electronichealth.net.au/ncts/syndication/asf/scheme/1.0.0");
    category.setTerm(categoryTerm);
    entry.setCategory(category);

    final SyndicationLink link = new SyndicationLink();
    link.setHref("classpath:data/" + fileName);
    link.setRel(SyndicationLink.RelType.related);
    link.setType("application/zip");
    link.setLength("1024");
    entry.setLinks(Collections.singletonList(link));

    return entry;
  }
}
