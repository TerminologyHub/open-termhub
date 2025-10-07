/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.lucene.search.Query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.wci.termhub.util.StringUtility;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents search parameters for a "find" call.
 */
@Schema(description = "Represents parameters for a 'find' call.")
@JsonInclude(Include.NON_EMPTY)
public class SearchParameters extends BaseModel {

  /** The field mappings. */
  // Map of special field names to their Lucene field names
  private final Map<String, String> fieldMappings =
      Map.of("name", "name.keyword", "normName", "normName.keyword", "to.name", "to.name.keyword",
          "from.name", "from.name.keyword", "stemName", "stemName.keyword", "concept.name",
          "concept.name.keyword", "title", "title.keyword");

  /** The terminology. */
  private String terminology;

  /** The query. */
  private String query;

  /** The lucene query. */
  // Not serializable to json. So ignoring
  @JsonIgnore()
  private Query luceneQuery;

  /** The expression. */
  private String expression;

  /** The filters. */
  private Map<String, String> filters;

  /** The limit. */
  private Integer limit = 10;

  /** The offset. */
  private Integer offset = 0;

  /** The active. */
  private Boolean active;

  /** The sort. */
  private List<String> sort;

  /** The sort ascending. */
  private Boolean ascending;

  /** The index. */
  private String index;

  /** The field. */
  private String field;

  /** The leaf. */
  private Boolean leaf;

  /**
   * Instantiates an empty {@link SearchParameters}.
   */
  public SearchParameters() {
    // n/a
  }

  /**
   * Instantiates a {@link SearchParameters} from the specified parameters.
   *
   * @param limit the limit
   * @param offset the offset
   */
  public SearchParameters(final int limit, final int offset) {

    this.limit = limit;
    this.offset = offset;
  }

  /**
   * Instantiates a {@link SearchParameters} from the specified parameters.
   *
   * @param query the query
   * @param limit the limit
   * @param offset the offset
   */
  public SearchParameters(final String query, final int limit, final int offset) {

    this.query = query;
    this.limit = limit;
    this.offset = offset;
  }

  /**
   * Instantiates a new search parameters.
   *
   * @param query the query
   * @param limit the limit
   * @param offset the offset
   */
  public SearchParameters(final Query query, final int limit, final int offset) {

    this.luceneQuery = query;
    this.limit = limit;
    this.offset = offset;
  }

  /**
   * Instantiates a new search parameters.
   *
   * @param offset the offset
   * @param limit the limit
   * @param sort the sort
   * @param ascending the ascending
   */
  public SearchParameters(final Integer offset, final Integer limit, final String sort,
      final Boolean ascending) {

    if (offset != null) {
      setOffset(offset);
    }
    if (limit != null) {
      setLimit(limit);
    }
    if (ascending != null) {
      setAscending(ascending);
    }
    if (!StringUtility.isEmpty(sort)) {
      // Split comma-separated sort fields
      final String[] sortFields = sort.split(",");
      for (final String sortField : sortFields) {
        final String trimmedField = sortField.trim();
        final String luceneField = fieldMappings.getOrDefault(trimmedField, trimmedField);
        getSort().add(luceneField);
      }
    }
  }

  /**
   * Instantiates a {@link SearchParameters} from the specified parameters. This
   * is a helper constructor so it can be easily constructed for "find" methods.
   *
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param sort the sort
   * @param ascending the ascending
   */
  public SearchParameters(final String query, final Integer offset, final Integer limit,
      final String sort, final Boolean ascending) {
    this(offset, limit, sort, ascending);
    if (query != null) {
      setQuery(query);
    } else {
      setQuery("");
    }
  }

  /**
   * Instantiates a new search parameters.
   *
   * @param query the query
   * @param offset the offset
   * @param limit the limit
   * @param sort the sort
   * @param ascending the ascending
   */
  public SearchParameters(final Query query, final Integer offset, final Integer limit,
      final String sort, final Boolean ascending) {
    this(offset, limit, sort, ascending);
    this.luceneQuery = query;
  }

  /**
   * Instantiates a {@link SearchParameters} from the specified parameters.
   *
   * @param other the other
   */
  public SearchParameters(final SearchParameters other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  public void populateFrom(final SearchParameters other) {
    terminology = other.getTerminology();
    query = other.getQuery();
    expression = other.getExpression();
    limit = other.getLimit();
    offset = other.getOffset();
    active = other.getActive();
    sort = new ArrayList<>(other.getSort());
    ascending = other.getAscending();
    filters = new HashMap<>(other.getFilters());
    index = other.getIndex();
    field = other.getField();
    leaf = other.getLeaf();
  }

  /**
   *
   *
   * /** Returns the terminology.
   *
   * @return the terminology
   */
  @Schema(description = "Terminology filter, e.g. 'SNOMEDCT'")
  public String getTerminology() {
    return terminology;
  }

  /**
   * Sets the terminology.
   *
   * @param terminology the terminology
   */
  public void setTerminology(final String terminology) {
    this.terminology = terminology;
  }

  /**
   * Returns the query.
   *
   * @return the query
   */
  @Schema(description = "Search query, e.g. 'aspirin'")
  public String getQuery() {
    return query;
  }

  /**
   * Sets the query.
   *
   * @param query the query
   */
  public void setQuery(final String query) {
    this.query = query;
  }

  /**
   * Gets the expression.
   *
   * @return the expression
   */
  @Schema(description = "Search expression, e.g. '<< 404684003'")
  public String getExpression() {
    return expression;
  }

  /**
   * Sets the expression.
   *
   * @param expression the new expression
   */
  public void setExpression(final String expression) {
    this.expression = expression;
  }

  /**
   * Returns the limit.
   *
   * @return the limit
   */
  @Schema(description = "Limit on number of results, e.g. 10")
  public Integer getLimit() {
    return limit;
  }

  /**
   * Sets the limit.
   *
   * @param limit the limit
   */
  public void setLimit(final Integer limit) {
    this.limit = limit;
  }

  /**
   * Returns the offset.
   *
   * @return the offset
   */
  @Schema(description = "Starting index for results, e.g. 0")
  public Integer getOffset() {
    return offset;
  }

  /**
   * Sets the offset.
   *
   * @param offset the offset
   */
  public void setOffset(final Integer offset) {
    this.offset = offset;
  }

  /**
   * Returns the active .
   *
   * @return the active
   */
  @Schema(description = "Specifically search for 'active only' or 'inactive only'")
  public Boolean getActive() {
    return active;
  }

  /**
   * Sets the leaf .
   *
   * @param leaf the leaf
   */
  public void setLeaf(final Boolean leaf) {
    this.leaf = leaf;
  }

  /**
   * Gets the leaf.
   *
   * @return the leaf
   */
  @Schema(description = "Specifically search for 'leaf only' or 'leaf only'")
  public Boolean getLeaf() {
    return leaf;
  }

  /**
   * Sets the active .
   *
   * @param active the active
   */
  public void setActive(final Boolean active) {
    this.active = active;
  }

  /**
   * Returns the sort.
   *
   * @return the sort
   */
  @Schema(description = "Field of the data model being searched to sort by")
  public List<String> getSort() {
    if (sort == null) {
      sort = new ArrayList<>(4);
    }
    return sort;
  }

  /**
   * Sets the sort.
   *
   * @param sort the sort
   */
  public void setSort(final List<String> sort) {
    this.sort = sort;
  }

  /**
   * Returns the sort ascending.
   *
   * @return the sort ascending
   */
  @Schema(description = "Used with 'sort' to indicate ascending or descending")
  public Boolean getAscending() {
    return ascending;
  }

  /**
   * Sets the sort ascending.
   *
   * @param ascending the sort ascending
   */
  public void setAscending(final Boolean ascending) {
    this.ascending = ascending;
  }

  /**
   * Returns the filters.
   *
   * @return the filters
   */
  @Schema(description = "Additional field level filters "
      + "(to specify that certain fields must have certain values)")
  public Map<String, String> getFilters() {
    if (filters == null) {
      filters = new HashMap<>(4);
    }
    return filters;
  }

  /**
   * Sets the filters.
   *
   * @param filters the filters
   */
  public void setFilters(final Map<String, String> filters) {
    this.filters = filters;
  }

  /**
   * Returns the index.
   *
   * @return the index
   */
  @Schema(hidden = true, description = "Index name to search (typically not used directly)")
  public String getIndex() {
    return index;
  }

  /**
   * Sets the index.
   *
   * @param index the index
   */
  public void setIndex(final String index) {
    this.index = index;
  }

  /**
   * Returns the field.
   *
   * @return the field
   */
  @Schema(hidden = true, description = "Field to search (typically not used directly)")
  public String getField() {
    return field;
  }

  /**
   * Sets the field.
   *
   * @param field the field
   */
  public void setField(final String field) {
    this.field = field;
  }

  /**
   * Returns the lucene query.
   *
   * @return the lucene query
   */
  @Schema(hidden = true, description = "Lucene query (typically not used directly)")
  public Query getLuceneQuery() {
    return luceneQuery;
  }

  /**
   * Sets the lucene query.
   *
   * @param luceneQuery the lucene query
   */
  public void setLuceneQuery(final Query luceneQuery) {
    this.luceneQuery = luceneQuery;
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  /* see superclass */
  @Override
  public int hashCode() {
    return Objects.hash(active, ascending, expression, field, filters, index, leaf, limit, offset,
        query, sort, terminology, luceneQuery);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  /* see superclass */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }
    final SearchParameters other = (SearchParameters) obj;
    return Objects.equals(active, other.active) && Objects.equals(ascending, other.ascending)
        && Objects.equals(expression, other.expression) && Objects.equals(field, other.field)
        && Objects.equals(filters, other.filters) && Objects.equals(index, other.index)
        && Objects.equals(leaf, other.leaf) && Objects.equals(limit, other.limit)
        && Objects.equals(offset, other.offset) && Objects.equals(query, other.query)
        && Objects.equals(sort, other.sort) && Objects.equals(terminology, other.terminology)
        && Objects.equals(luceneQuery, other.luceneQuery);
  }

}
