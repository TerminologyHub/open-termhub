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
                        // An example line is curl -s
                        // "http://localhost:8080/concept?terminology=SNOMEDCT_US&query=diabetes&include=minimal"
                        // | jq. From this line I only want the resource. Strip out the curl, hostname and jq
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
