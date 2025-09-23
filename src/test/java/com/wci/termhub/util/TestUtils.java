/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

public class TestUtils {
    public static Set<String> getUrlsFromMarkdown(String markdownFile, String sectionTitle) {
        File tutorialFile = new File(markdownFile);
        Set<String> tutorialResources = new java.util.HashSet<>();
        try {
            boolean inSection = false;
            boolean startCodeBlock = false;
            List<String> lines = Files.readAllLines(tutorialFile.toPath());
            for (String line : lines) {
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
                        resource = resource.replace("http://localhost:8080", "").replace("\"", "").replace("'","").trim();
                        resource = UriUtils.decode(resource, StandardCharsets.UTF_8);
                        tutorialResources.add(resource);
                    }
                }
            }
            return tutorialResources;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
