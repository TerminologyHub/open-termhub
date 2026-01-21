/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.wci.termhub.rest.RestException;
import com.wci.termhub.syndication.SyndicationClient;
import com.wci.termhub.syndication.SyndicationFeed;
import com.wci.termhub.syndication.SyndicationFeedEntry;
import com.wci.termhub.syndication.SyndicationLink;

/**
 * Unit test for SyndicationClient.
 */
public class SyndicationClientUnitTest {

  /** The client. */
  private SyndicationClient client;

  /** The mock rest template. */
  private RestTemplate mockRestTemplate;

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setUp() throws Exception {
    mockRestTemplate = mock(RestTemplate.class);
    // Create client with proper constructor parameters
    client = new SyndicationClient("https://api.example.com", "testToken");
    // Use reflection to set the private restTemplate field
    try {
      java.lang.reflect.Field field = SyndicationClient.class.getDeclaredField("restTemplate");
      field.setAccessible(true);
      field.set(client, mockRestTemplate);
    } catch (Exception e) {
      throw new RuntimeException("Failed to inject mock RestTemplate", e);
    }
  }

  /**
   * Test constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConstructor() throws Exception {
    SyndicationClient client = new SyndicationClient("https://api.example.com", "testToken");
    assertNotNull(client);
  }

  /**
   * Test get syndication credentials.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSyndicationCredentials() throws Exception {
    // Test the private method via reflection
    java.lang.reflect.Method method =
        SyndicationClient.class.getDeclaredMethod("getSyndicationCredentials");
    method.setAccessible(true);
    String credentials = (String) method.invoke(client);
    assertEquals("testToken", credentials);
  }

  /**
   * Test get syndication url.
   */
  @Test
  public void testGetSyndicationUrl() {
    String url = client.getSyndicationUrl();
    assertNotNull(url);
  }

  /**
   * Test parse feed.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testParseFeed() throws IOException {
    String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<feed>\n" + "  <entry>\n"
        + "<title>Test CodeSystem</title>\n"
        + "<link href=\"https://api.example.com/terminology/123/export\" rel=\"related\" type=\"application/zip\"/>\n"
        + "<category term=\"CodeSystem\" label=\"CodeSystem\"/>\n"
        + "<id>urn:uuid:12345678-1234-1234-1234-123456789012</id>\n"
        + "<updated>2024-01-01T00:00:00Z</updated>\n" + "  </entry>\n" + "</feed>";

    SyndicationFeed feed = client.parseFeed(xmlContent);
    assertNotNull(feed);
    assertEquals(1, feed.getEntries().size());

    SyndicationFeedEntry entry = feed.getEntries().get(0);
    assertEquals("Test CodeSystem", entry.getTitle());
    assertEquals("urn:uuid:12345678-1234-1234-1234-123456789012", entry.getId());
    assertNotNull(entry.getZipLink());
    assertEquals("https://api.example.com/terminology/123/export", entry.getZipLink().getHref());
  }

  /**
   * Test parse feed with invalid xml.
   */
  @Test
  public void testParseFeedWithInvalidXml() {
    String invalidXml = "invalid xml content";

    assertThrows(IOException.class, () -> {
      client.parseFeed(invalidXml);
    });
  }

  /**
   * Test download packages.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDownloadPackages() throws Exception {
    // Mock the feed and entry
    SyndicationFeed mockFeed = mock(SyndicationFeed.class);
    SyndicationFeedEntry mockEntry = mock(SyndicationFeedEntry.class);
    SyndicationLink mockLink = mock(SyndicationLink.class);

    when(mockEntry.getZipLink()).thenReturn(mockLink);
    when(mockLink.getHref()).thenReturn("https://api.example.com/terminology/123/export");
    when(mockEntry.getCategory())
        .thenReturn(mock(com.wci.termhub.syndication.SyndicationCategory.class));

    // Mock the HTTP response
    byte[] zipContent = "fake zip content".getBytes(StandardCharsets.UTF_8);
    ResponseEntity<byte[]> response = new ResponseEntity<>(zipContent, HttpStatus.OK);

    when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
        eq(byte[].class))).thenReturn(response);

    Set<String> downloadedFiles = client.downloadPackages(mockEntry, mockFeed);

    assertNotNull(downloadedFiles);
    assertEquals(1, downloadedFiles.size());
  }

  /**
   * Test download packages with http error.
   */
  @Test
  public void testDownloadPackagesWithHttpError() {
    SyndicationFeed mockFeed = mock(SyndicationFeed.class);
    SyndicationFeedEntry mockEntry = mock(SyndicationFeedEntry.class);
    SyndicationLink mockLink = mock(SyndicationLink.class);

    when(mockEntry.getZipLink()).thenReturn(mockLink);
    when(mockLink.getHref()).thenReturn("https://api.example.com/terminology/123/export");
    when(mockEntry.getCategory())
        .thenReturn(mock(com.wci.termhub.syndication.SyndicationCategory.class));

    // Mock HTTP error response for OPTIONS request (which happens first)
    when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.OPTIONS), any(HttpEntity.class),
        eq(Void.class))).thenThrow(
            new org.springframework.web.client.HttpClientErrorException(HttpStatus.NOT_FOUND));

    assertThrows(RestException.class, () -> {
      client.downloadPackages(mockEntry, mockFeed);
    });
  }

  /**
   * Test download packages with null entry.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDownloadPackagesWithNullEntry() throws Exception {
    SyndicationFeed mockFeed = mock(SyndicationFeed.class);

    assertThrows(NullPointerException.class, () -> {
      client.downloadPackages(null, mockFeed);
    });
  }

  /**
   * Test download packages with null zip link.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDownloadPackagesWithNullZipLink() throws Exception {
    SyndicationFeed mockFeed = mock(SyndicationFeed.class);
    SyndicationFeedEntry mockEntry = mock(SyndicationFeedEntry.class);

    when(mockEntry.getZipLink()).thenReturn(null);

    Set<String> result = client.downloadPackages(mockEntry, mockFeed);
    assertNotNull(result);
    assertEquals(0, result.size());
  }
}
