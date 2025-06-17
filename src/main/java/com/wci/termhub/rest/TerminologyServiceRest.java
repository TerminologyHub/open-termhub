/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.wci.termhub.model.Concept;
import com.wci.termhub.model.Mapping;
import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.Metadata;
import com.wci.termhub.model.ResultListConcept;
import com.wci.termhub.model.ResultListConceptRelationship;
import com.wci.termhub.model.ResultListConceptTreePosition;
import com.wci.termhub.model.ResultListMapping;
import com.wci.termhub.model.ResultListMapset;
import com.wci.termhub.model.ResultListMetadata;
import com.wci.termhub.model.ResultListTerm;
import com.wci.termhub.model.ResultListTerminology;
import com.wci.termhub.model.Terminology;

/**
 * Rest services oriented around content.
 */
public interface TerminologyServiceRest extends RootServiceRest {

  /**
   * Returns the terminology. GET /terminology/{id:[a-f0-9].+} tag=terminology
   *
   * @param id the id
   * @return the terminology
   * @throws Exception the exception
   */
  public ResponseEntity<Terminology> getTerminology(String id) throws Exception;

  /**
   * Returns the terminology metadata. GET /terminology/{id:[a-f0-9].+}/metadata tag=metadata
   *
   * @param id the id
   * @return the terminology
   * @throws Exception the exception
   */
  public ResponseEntity<List<Metadata>> getTerminologyMetadata(String id) throws Exception;

  /**
   * Adds the terminology. POST /terminology tag=terminology
   *
   * @param terminologyStr the terminology str
   * @return the response entity
   * @throws Exception the exception
   */
  public ResponseEntity<Terminology> addTerminology(String terminologyStr) throws Exception;

  /**
   * Update terminology. PATCH /terminology/{id} tag=terminology
   *
   * @param id the id
   * @param terminologyStr the terminology str
   * @return the response entity
   * @throws Exception the exception
   */
  public ResponseEntity<Terminology> updateTerminology(String id, String terminologyStr)
    throws Exception;

  /**
   * Delete. DELETE /terminology/{id} tag=terminology
   *
   * @param terminologyStr the terminology str
   * @return the response entity
   * @throws Exception the exception
   */
  public ResponseEntity<Void> deleteTerminology(String terminologyStr) throws Exception;

  /**
   * Find terminologies. GET /terminology tag=terminology
   *
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param sort the sort
   * @param ascending the ascending
   * @return the response entity
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListTerminology> findTerminologies(String query, Integer offset,
    Integer limit, String sort, Boolean ascending) throws Exception;

  /**
   * Gets the concept. GET /concept/{conceptId} tag=concept by id
   *
   * @param conceptId the concept id
   * @param include the include
   * @return the concept
   * @throws Exception the exception
   */
  public ResponseEntity<Concept> getConcept(String conceptId, String include) throws Exception;

  /**
   * Gets the concept for the specified terminology and code. This call assumes that the project has
   * only a single terminology/publisher/version configured, otherwise it will return a 409. GET
   * /concept/{terminology}/{code} tag=concept by code
   *
   * @param terminology the terminology
   * @param code the code
   * @param include the include
   * @return the concept
   * @throws Exception the exception
   */
  public ResponseEntity<Concept> getConcept(String terminology, String code, String include)
    throws Exception;

  /**
   * Gets the concept codes.
   *
   * @param terminology the terminology
   * @param codes the codes
   * @param include the include
   * @return the concept codes
   * @throws Exception the exception
   */
  public ResponseEntity<List<Concept>> getConceptCodes(String terminology, String codes,
    String include) throws Exception;

  /**
   * Find concepts. GET /concept?terminology=uuid1,uuid2&query=... tag=concept
   *
   * @param terminology the terminology - if no terminologies are selected, it searches across all
   *          of them.
   * @param query the query
   * @param expression the expression
   * @param offset the offset
   * @param limit the limit
   * @param sort the sort
   * @param ascending the ascending
   * @param active the active
   * @param leaf the leaf
   * @param include the include
   * @param handler the handler
   * @return the response entity
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListConcept> findConcepts(String terminology, String query,
    String expression, Integer offset, Integer limit, String sort, Boolean ascending,
    Boolean active, Boolean leaf, String include, String handler) throws Exception;

  /**
   * Find terms.
   *
   * @param terminology the terminology
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param sort the sort
   * @param ascending the ascending
   * @param active the active
   * @param handler the handler
   * @return the response entity
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListTerm> findTerms(String terminology, String query, Integer offset,
    Integer limit, String sort, Boolean ascending, Boolean active, String handler) throws Exception;

  /**
   * Lookup.
   *
   * @param terminology the terminology
   * @param expression the expression
   * @param limit the limit
   * @param active the active
   * @param leaf the leaf
   * @param include the include
   * @param handler the handler
   * @param queries the queries
   * @return the response entity
   * @throws Exception the exception
   */
  public ResponseEntity<List<ResultListConcept>> lookup(String terminology, String expression,
    Integer limit, Boolean active, Boolean leaf, String include, String handler, String queries)
    throws Exception;

  /**
   * Find metadata across project terminologies. GET /metadata tag=metadata
   *
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param sort the sort
   * @param ascending the ascending
   * @return the response entity
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListMetadata> findMetadata(String query, Integer offset,
    Integer limit, String sort, Boolean ascending) throws Exception;

  /**
   * Find concept relationships. GET /concept/{conceptId}/relationships tag=concept
   *
   * @param conceptId the concept id
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param ascending the ascending
   * @param sort the sort
   * @param handler the handler
   * @return the result list concept relationship
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListConceptRelationship> findConceptRelationships(String conceptId,
    String query, Integer offset, Integer limit, Boolean ascending, String sort, String handler)
    throws Exception;

  /**
   * Find concept relationships. This call assumes that the project has only a single
   * terminology/publisher/version configured, otherwise it will return a 409. GET
   * /concept/{terminology}/{code}/relationships tag=concept by code
   *
   * @param terminology the terminology
   * @param code the code
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param ascending the ascending
   * @param sort the sort
   * @param handler the handler
   * @return the result list concept relationship
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListConceptRelationship> findConceptRelationships(String terminology,
    String code, String query, Integer offset, Integer limit, Boolean ascending, String sort,
    String handler) throws Exception;

  /**
   * Find concept inverse relationships. GET /concept/{conceptId}/inverseRelationships tag=concept
   *
   * @param conceptId the concept id
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param ascending the ascending
   * @param sort the sort
   * @param handler the handler
   * @return the response entity
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListConceptRelationship> findConceptInverseRelationships(
    String conceptId, String query, Integer offset, Integer limit, Boolean ascending, String sort,
    String handler) throws Exception;

  /**
   * Find concept inverse relationships. This call assumes that the project has only a single
   * terminology/publisher/version configured, otherwise it will return a 409. GET
   * /concept/{terminology}/{code}/inverseRelationships tag=concept by code
   *
   * @param terminology the terminology
   * @param code the code
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param ascending the ascending
   * @param sort the sort
   * @param handler the handler
   * @return the response entity
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListConceptRelationship> findConceptInverseRelationships(
    String terminology, String code, String query, Integer offset, Integer limit, Boolean ascending,
    String sort, String handler) throws Exception;

  /**
   * Find concept tree positions. GET /concept/{conceptId}/treepos tag=concept
   *
   * @param conceptId the concept id
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param ascending the ascending
   * @param sort the sort
   * @param handler the handler
   * @return the result list concept tree position
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListConceptTreePosition> findTreePositions(String conceptId,
    String query, Integer offset, Integer limit, Boolean ascending, String sort, String handler)
    throws Exception;

  /**
   * Find concept tree positions. This call assumes that the project has only a single
   * terminology/publisher/version configured, otherwise it will return a 409. GET
   * /concept/{terminology}/{code}/treepos tag=concept by code
   *
   * @param terminology the terminology
   * @param code the code
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param ascending the ascending
   * @param sort the sort
   * @param handler the handler
   * @return the result list concept tree position
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListConceptTreePosition> findTreePositions(String terminology,
    String code, String query, Integer offset, Integer limit, Boolean ascending, String sort,
    String handler) throws Exception;

  /**
   * Find concept tree position children. GET /concept/{conceptId}/treepos/children tag=concept
   *
   * @param conceptId the concept id
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param ascending the ascending
   * @param sort the sort
   * @param handler the handler
   * @return the result list concept tree position
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListConceptTreePosition> findTreePositionChildren(String conceptId,
    String query, Integer offset, Integer limit, Boolean ascending, String sort, String handler)
    throws Exception;

  /**
   * Find concept tree positions. This call assumes that the project has only a single
   * terminology/publisher/version configured, otherwise it will return a 409. GET
   * /concept/{terminology}/{code}/treepos/children tag=concept by code
   *
   * @param terminology the terminology
   * @param code the code
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param ascending the ascending
   * @param sort the sort
   * @param handler the handler
   * @return the result list concept tree position
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListConceptTreePosition> findTreePositionChildren(String terminology,
    String code, String query, Integer offset, Integer limit, Boolean ascending, String sort,
    String handler) throws Exception;

  /**
   * Gets the mapset.
   *
   * @param id the id
   * @return the mapset
   * @throws Exception the exception
   */
  public ResponseEntity<Mapset> getMapset(String id) throws Exception;

  /**
   * Find mapsets.
   *
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param sort the sort
   * @param ascending the ascending
   * @return the response entity
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListMapset> findMapsets(String query, Integer offset, Integer limit,
    String sort, Boolean ascending) throws Exception;

  /**
   * Find mappings.
   *
   * @param mapset the mapset
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param sort the sort
   * @param ascending the ascending
   * @param active the active
   * @param leaf the leaf
   * @return the response entity
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListMapping> findMappings(String mapset, String query, Integer offset,
    Integer limit, String sort, Boolean ascending, Boolean active, Boolean leaf) throws Exception;

  /**
   * Find mapset.
   *
   * @param mapset the mapset id
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param sort the sort
   * @param ascending the ascending
   * @param active the active
   * @param leaf the leaf
   * @return the response entity
   * @throws Exception the exception
   */
  public ResponseEntity<ResultListMapping> findMapsetMappings(String mapset, String query,
    Integer offset, Integer limit, String sort, Boolean ascending, Boolean active, Boolean leaf)
    throws Exception;

  /**
   * Find concept mappings.
   *
   * @param conceptId the concept id
   * @return the response entity
   * @throws Exception the exception
   */
  public ResponseEntity<List<Mapping>> getConceptMappings(String conceptId) throws Exception;

  /**
   * Gets the concept inverse mappings.
   *
   * @param conceptId the concept id
   * @return the concept inverse mappings
   * @throws Exception the exception
   */
  public ResponseEntity<List<Mapping>> getConceptInverseMappings(String conceptId) throws Exception;

  /**
   * Gets the concept mappings.
   *
   * @param terminology the terminology
   * @param code the code
   * @return the concept mappings
   * @throws Exception the exception
   */
  public ResponseEntity<List<Mapping>> getConceptMappings(String terminology, String code)
    throws Exception;

  /**
   * Gets the concept inverse mappings.
   *
   * @param terminology the terminology
   * @param code the code
   * @return the concept inverse mappings
   * @throws Exception the exception
   */
  public ResponseEntity<List<Mapping>> getConceptInverseMappings(String terminology, String code)
    throws Exception;

}
