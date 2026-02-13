/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.group.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.ConceptRef;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.ResultListConcept;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.rest.TerminologyServiceRestImpl;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.TerminologyUtility;

/**
 * Test class for remove descendants functionality.
 */
public class RemoveDescendantsUnitTest extends AbstractTerminologyTest {

  @Autowired
  private TerminologyServiceRestImpl restService;

  @Autowired
  private EntityRepositoryService searchService;

  @Test
  public void testRemoveDescendants() throws Exception {
    // 1. Find a concept with children in SNOMEDCT_US
    Terminology term = TerminologyUtility.getTerminology(searchService, "SNOMEDCT_US", "SANDBOX", "20240301");
    if (term == null) {
      throw new Exception("Could not find terminology SNOMEDCT_US");
    }
    
    // Find a non-leaf concept to ensure it has children/descendants
    SearchParameters params = new SearchParameters("leaf:false AND terminology:SNOMEDCT_US", 100, 0);
    ResultList<Concept> results = searchService.find(params, Concept.class);
    
    Concept parent = null;
    Concept child = null;
    
    for (Concept c : results.getItems()) {
        List<ConceptRef> children = TerminologyUtility.getChildren(searchService, term, c);
        if (!children.isEmpty()) {
            parent = c;
            // Load child to get its code and ensure it exists
            child = TerminologyUtility.getConcept(searchService, term, children.get(0).getCode());
            break;
        }
    }
    
    if (parent == null || child == null) {
        // Fallback or fail if data not sufficient
        // sandbox data should have hierarchy
        throw new Exception("Could not find parent/child pair in sandbox data");
    }
    
    // 2. Direct Utility Test
    List<String> codes = Arrays.asList(parent.getCode(), child.getCode(), "12345FAKE");
    List<String> filtered = TerminologyUtility.removeDescendants(searchService, term, codes);
    
    // Child should be removed. Parent should stay. Fake should stay.
    assertTrue(filtered.contains(parent.getCode()), "Parent should remain");
    assertTrue(filtered.contains("12345FAKE"), "Unrelated code should remain");
    assertTrue(!filtered.contains(child.getCode()), "Child should be removed");
    assertEquals(2, filtered.size());

    // 3. Endpoint Test (computeRemoveDescendants)
    String codesParam = parent.getCode() + "," + child.getCode() + ",12345FAKE";
    ResponseEntity<List<String>> response = restService.computeRemoveDescendants("SNOMEDCT_US", codesParam);
    List<String> respList = response.getBody();
    
    assertTrue(respList.contains(parent.getCode()));
    assertTrue(respList.contains("12345FAKE"));
    assertTrue(!respList.contains(child.getCode()));
    
    // 4. Endpoint Test (findTerminologyConcepts with aggregate)
    // Query for both codes: code:(c1 OR c2)
    String query = "code:(" + parent.getCode() + " OR " + child.getCode() + ")";
    
    // Without aggregate
    ResponseEntity<ResultListConcept> findResp = restService.findTerminologyConcepts("SNOMEDCT_US", query, null, null, null, null, null, null, null, null, null, null);
    assertEquals(2, findResp.getBody().getItems().size());
    
    // With aggregate
    findResp = restService.findTerminologyConcepts("SNOMEDCT_US", query, null, null, null, null, null, null, null, null, "removeDescendants", null);
    List<Concept> items = findResp.getBody().getItems();
    List<String> itemCodes = items.stream().map(Concept::getCode).collect(Collectors.toList());
    
    assertEquals(1, items.size());
    assertTrue(itemCodes.contains(parent.getCode()));
    assertTrue(!itemCodes.contains(child.getCode()));
    
    // Test case where no descendants (siblings)
    // Find another child
    // ...
  }
}
