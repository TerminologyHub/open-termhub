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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.wci.termhub.syndication.SyndicationContentType;

/**
 * Unit test for SyndicationContentType enum.
 */
public class SyndicationContentTypeUnitTest {

  /**
   * Test enum values.
   */
  @Test
  public void testEnumValues() {
    assertEquals("CodeSystem", SyndicationContentType.CODESYSTEM.getResourceType());
    assertEquals("ValueSet", SyndicationContentType.VALUESET.getResourceType());
    assertEquals("ConceptMap", SyndicationContentType.CONCEPTMAP.getResourceType());
  }

  /**
   * Test from download url.
   */
  @Test
  public void testFromDownloadUrl() {
    // Terminology URLs
    assertEquals(SyndicationContentType.CODESYSTEM,
        SyndicationContentType.fromDownloadUrl("https://api.example.com/terminology/123/export"));
    assertEquals(SyndicationContentType.CODESYSTEM,
        SyndicationContentType.fromDownloadUrl("/terminology/456/download"));

    // Mapset URLs
    assertEquals(SyndicationContentType.CONCEPTMAP,
        SyndicationContentType.fromDownloadUrl("https://api.example.com/mapset/789/export"));
    assertEquals(SyndicationContentType.CONCEPTMAP,
        SyndicationContentType.fromDownloadUrl("/mapset/101/download"));

    // Subset URLs
    assertEquals(SyndicationContentType.VALUESET,
        SyndicationContentType.fromDownloadUrl("https://api.example.com/subset/202/export"));
    assertEquals(SyndicationContentType.VALUESET,
        SyndicationContentType.fromDownloadUrl("/subset/303/download"));

    // Null and invalid
    assertNull(SyndicationContentType.fromDownloadUrl(null));
    assertNull(SyndicationContentType.fromDownloadUrl("https://api.example.com/other/123"));
    assertNull(SyndicationContentType.fromDownloadUrl(""));
  }

}
