/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.rest.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.wci.termhub.rest.ReadOnlyOpenApiSupport;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

/**
 * Unit tests for {@link ReadOnlyOpenApiSupport}.
 */
public class ReadOnlyOpenApiSupportUnitTest {

  /**
   * Removes REST DELETE/PUT/PATCH/POST including concept bulk.
   */
  @Test
  public void testRemoveRestMutatingOperations() {
    final Paths paths = new Paths();
    paths.addPathItem("/terminology/{id}", new PathItem().get(new Operation()).delete(new Operation()));
    paths.addPathItem("/syndicate", new PathItem().post(new Operation()));
    paths.addPathItem("/concept/bulk", new PathItem().post(new Operation()));
    paths.addPathItem("/mapset/{id}", new PathItem().delete(new Operation()));

    ReadOnlyOpenApiSupport.removeRestMutatingOperations(paths);

    assertNotNull(paths.get("/terminology/{id}").getGet());
    assertNull(paths.get("/terminology/{id}").getDelete());
    assertNull(paths.get("/syndicate"));
    assertNull(paths.get("/concept/bulk"));
    assertNull(paths.get("/mapset/{id}"));
  }

  /**
   * Removes FHIR create/delete/transaction POSTs and keeps GET search.
   */
  @Test
  public void testRemoveFhirMutatingOperations() {
    final Paths paths = new Paths();
    paths.addPathItem("/", new PathItem().post(new Operation()));
    paths.addPathItem("/CodeSystem",
        new PathItem().get(new Operation()).post(new Operation()));
    paths.addPathItem("/CodeSystem/{id}",
        new PathItem().get(new Operation()).delete(new Operation()).put(new Operation()));
    paths.addPathItem("/CodeSystem/$lookup",
        new PathItem().get(new Operation()).post(new Operation()));

    ReadOnlyOpenApiSupport.removeFhirMutatingOperations(paths);

    assertNull(paths.get("/"));
    assertNotNull(paths.get("/CodeSystem").getGet());
    assertNull(paths.get("/CodeSystem").getPost());
    assertNotNull(paths.get("/CodeSystem/{id}").getGet());
    assertNull(paths.get("/CodeSystem/{id}").getDelete());
    assertNull(paths.get("/CodeSystem/{id}").getPut());
    assertNotNull(paths.get("/CodeSystem/$lookup").getGet());
    assertNull(paths.get("/CodeSystem/$lookup").getPost());
  }
}
