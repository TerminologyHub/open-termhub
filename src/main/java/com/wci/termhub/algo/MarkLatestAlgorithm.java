/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.algo;

import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.wci.termhub.model.Mapset;
import com.wci.termhub.model.ResultList;
import com.wci.termhub.model.SearchParameters;
import com.wci.termhub.model.Subset;
import com.wci.termhub.model.Terminology;
import com.wci.termhub.model.TerminologyRef;
import com.wci.termhub.service.EntityRepositoryService;
import com.wci.termhub.util.ModelUtility;
import com.wci.termhub.util.StringUtility;

/**
 * Algorithm to mark the latest version for a terminology/publisher (and
 * corresponding mapsets and subsets) based on {@code releaseDate}. The newest
 * release is marked {@code latest=true}, and all other versions for the same
 * abbreviation/publisher are set to {@code latest=false}.
 *
 * This is stateless aside from the configured terminology and publisher and is
 * thread-safe when used as a prototype-scoped bean.
 */
@Scope("prototype")
@Component
public class MarkLatestAlgorithm extends AbstractTerminologyAlgorithm {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(MarkLatestAlgorithm.class);

  /** The repository service. */
  @Autowired
  private EntityRepositoryService searchService;

  /**
   * Instantiates an empty {@link MarkLatestAlgorithm}.
   *
   * @throws Exception if anything goes wrong
   */
  public MarkLatestAlgorithm() throws Exception {
    super();
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {

    LOGGER.info("START mark latest");
    LOGGER.info("  abbreviation = {}", getTerminology());
    LOGGER.info("  publisher = {}", getPublisher());

    // Terminology
    markLatestForType(Terminology.class);

    // Mapset
    markLatestForType(Mapset.class);

    // Subset
    markLatestForType(Subset.class);

    LOGGER.info("FINISH mark latest");
  }

  /**
   * Mark latest flag for all versions of the configured terminology/publisher
   * for a specific type.
   *
   * @param <T> the entity type
   * @param clazz the clazz
   * @throws Exception the exception
   */
  private <T extends TerminologyRef> void markLatestForType(final Class<T> clazz) throws Exception {

    // Build a query on abbreviation+publisher, ignoring version so we see all
    // versions
    final String query = StringUtility.composeQuery("AND",
        StringUtility.escapeKeywordField("abbreviation", getTerminology()),
        StringUtility.escapeKeywordField("publisher", getPublisher()));

    final SearchParameters params = new SearchParameters(query, null, 100000, null, null);
    final ResultList<T> list = searchService.find(params, clazz);

    if (list.getItems().isEmpty()) {
      LOGGER.info("  NO {} entities to mark for {}/{}", clazz.getSimpleName(), getTerminology(),
          getPublisher());
      return;
    }

    boolean first = true;
    for (final T item : list.getItems().stream()
        .sorted((a, b) -> ModelUtility.nvl(b.getReleaseDate(), "1000-01-01")
            .compareTo(ModelUtility.nvl(a.getReleaseDate(), "1000-01-01")))
        .collect(Collectors.toList())) {

      final boolean latest = first;
      first = false;

      final Boolean current = item.getLatest();
      if (current == null || current.booleanValue() != latest) {
        item.setLatest(latest);
        searchService.update(clazz, item.getId(), item);
      }

      LOGGER.info("  {} {}/{}/{} = {}latest", clazz.getSimpleName(), getTerminology(),
          getPublisher(), item.getVersion(), latest ? "" : "not ");
    }
  }

  /**
   * Check preconditions.
   *
   * @return the validation result
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResult();
  }

  /**
   * Reset.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  /* see superclass */
  @Override
  public String getDescription() {
    return "Marks the version of a terminology/publisher that is the latest "
        + "(and unmarks any other versions that are currently marked latest).";
  }

  /**
   * Gets the handler key.
   *
   * @return the handler key
   */
  /* see superclass */
  @Override
  public String getHandlerKey() {
    return null;
  }

  /**
   * Sets the properties.
   *
   * @param p the new properties
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void setProperties(final Properties p) throws Exception {
    // n/a
  }

}
