/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import org.springframework.web.util.UriUtils;

/**
 * The Class TestUtils.
 */
public final class TestUtils {

  /**
   * Instantiates a new test utils.
   */
  private TestUtils() {
    // n/a
  }

  /**
   * Gets the urls from markdown.
   *
   * @param markdownFile the markdown file
   * @param sectionTitle the section title
   * @return the urls from markdown
   */
  public static Set<String> getUrlsFromMarkdown(final String markdownFile, final String sectionTitle) {
    final File tutorialFile = new File(markdownFile);
    final Set<String> tutorialResources = new java.util.HashSet<>();
    try {
      boolean inSection = false;
      boolean startCodeBlock = false;
      final List<String> lines = Files.readAllLines(tutorialFile.toPath());
      for (final String line : lines) {
        if (line.startsWith("### " + sectionTitle)) {
          inSection = true;
        }
        if (inSection) {
          if (line.startsWith("```")) {
            if (!startCodeBlock) {
              // start of code block
              startCodeBlock = true;
            } else {
              // end of code block
              break; // done reading curl commands
            }
          }
          if (startCodeBlock && line.startsWith("curl -s")) {
            String resource = line.substring(line.indexOf("http"));
            resource = resource.substring(0, resource.indexOf("|")).trim();
            // Remove the hostname part
            resource = resource.replace("http://localhost:8080", "").replace("\"", "")
                .replace("'", "").trim();
            resource = UriUtils.decode(resource, StandardCharsets.UTF_8);
            tutorialResources.add(resource);
          }
        }
      }
      return tutorialResources;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }

  }
}
