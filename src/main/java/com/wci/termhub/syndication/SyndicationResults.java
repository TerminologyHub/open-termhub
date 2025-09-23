/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.wci.termhub.model.BaseModel;

/**
 * Loading results summary.
 */
public final class SyndicationResults extends BaseModel {

  /** The total processed. */
  private int totalProcessed;

  /** The total loaded. */
  private int totalLoaded;

  /** The total errors. */
  private int totalErrors;

  /** The total skipped. */
  private int totalSkipped;

  /** The code system loaded. */
  // Per-content-type results
  private int codeSystemLoaded;

  /** The code system errors. */
  private int codeSystemErrors;

  /** The value set loaded. */
  private int valueSetLoaded;

  /** The value set errors. */
  private int valueSetErrors;

  /** The concept map loaded. */
  private int conceptMapLoaded;

  /** The concept map errors. */
  private int conceptMapErrors;

  /** The error messages. */
  private List<String> errorMessages = new ArrayList<>();

  /** The duration ms. */
  private long durationMs;

  /** The success. */
  private boolean success = true;

  /** The total to load. */
  private int totalToLoad;

  /** The message. */
  private String message;

  /**
   * Instantiates a new loading results.
   */
  public SyndicationResults() {
    // Default constructor
  }

  /**
   * Instantiates a new loading results.
   *
   * @param totalProcessed the total processed
   * @param totalLoaded the total loaded
   * @param totalErrors the total errors
   * @param totalSkipped the total skipped
   */
  public SyndicationResults(final int totalProcessed, final int totalLoaded, final int totalErrors,
      final int totalSkipped) {
    this.totalProcessed = totalProcessed;
    this.totalLoaded = totalLoaded;
    this.totalErrors = totalErrors;
    this.totalSkipped = totalSkipped;
  }

  /**
   * Instantiates a new syndication results.
   *
   * @param success the success
   * @param message the message
   * @param totalProcessed the total processed
   * @param totalLoaded the total loaded
   * @param totalErrors the total errors
   * @param durationMs the duration ms
   */
  public SyndicationResults(final boolean success, final String message, final int totalProcessed,
      final int totalLoaded, final int totalErrors, final long durationMs) {
    this.success = success;
    this.message = message;
    this.totalProcessed = totalProcessed;
    this.totalLoaded = totalLoaded;
    this.totalErrors = totalErrors;
    this.durationMs = durationMs;
  }

  /**
   * Sets the code system results.
   *
   * @param loaded the loaded
   * @param errors the errors
   */
  public void setCodeSystemResults(final int loaded, final int errors) {
    this.codeSystemLoaded = loaded;
    this.codeSystemErrors = errors;
  }

  /**
   * Sets the value set results.
   *
   * @param loaded the loaded
   * @param errors the errors
   */
  public void setValueSetResults(final int loaded, final int errors) {
    this.valueSetLoaded = loaded;
    this.valueSetErrors = errors;
  }

  /**
   * Sets the concept map results.
   *
   * @param loaded the loaded
   * @param errors the errors
   */
  public void setConceptMapResults(final int loaded, final int errors) {
    this.conceptMapLoaded = loaded;
    this.conceptMapErrors = errors;
  }

  /**
   * Adds the results.
   *
   * @param contentType the content type
   * @param loaded the loaded
   * @param errors the errors
   */
  public void addResults(final SyndicationContentType contentType, final int loaded,
    final int errors) {
    totalLoaded += loaded;
    totalErrors += errors;

    switch (contentType) {
      case CODESYSTEM:
        codeSystemLoaded += loaded;
        codeSystemErrors += errors;
        break;
      case VALUESET:
        valueSetLoaded += loaded;
        valueSetErrors += errors;
        break;
      case CONCEPTMAP:
        conceptMapLoaded += loaded;
        conceptMapErrors += errors;
        break;
      default:
        throw new IllegalArgumentException("Unknown content type: " + contentType);
    }
  }

  /**
   * Adds the error.
   *
   * @param contentType the content type
   * @param errorMessage the error message
   */
  public void addError(final SyndicationContentType contentType, final String errorMessage) {
    errorMessages.add(contentType + ": " + errorMessage);
  }

  /**
   * Gets the total processed.
   *
   * @return the total processed
   */
  // Getters
  public int getTotalProcessed() {
    return totalProcessed;
  }

  /**
   * Sets the total processed.
   *
   * @param totalProcessed the new total processed
   */
  public void setTotalProcessed(final int totalProcessed) {
    this.totalProcessed = totalProcessed;
  }

  /**
   * Increment total processed.
   */
  public void incrementTotalProcessed() {
    this.totalProcessed++;
  }

  /**
   * Increment total loaded.
   */
  public void incrementTotalLoaded() {
    this.totalLoaded++;
  }

  /**
   * Increment total errors.
   */
  public void incrementTotalErrors() {
    this.totalErrors++;
  }

  /**
   * Increment code systems loaded.
   */
  public void incrementCodeSystemsLoaded() {
    this.codeSystemLoaded++;
  }

  /**
   * Increment value sets loaded.
   */
  public void incrementValueSetsLoaded() {
    this.valueSetLoaded++;
  }

  /**
   * Increment concept maps loaded.
   */
  public void incrementConceptMapsLoaded() {
    this.conceptMapLoaded++;
  }

  /**
   * Adds the error message.
   *
   * @param errorMessage the error message
   */
  public void addErrorMessage(final String errorMessage) {
    this.errorMessages.add(errorMessage);
  }

  /**
   * Sets the success.
   *
   * @param success the new success
   */
  public void setSuccess(final boolean success) {
    this.success = success;
  }

  /**
   * Checks if is success.
   *
   * @return true, if is success
   */
  public boolean isSuccess() {
    return this.success;
  }

  /**
   * Sets the duration ms.
   *
   * @param durationMs the new duration ms
   */
  public void setDurationMs(final long durationMs) {
    this.durationMs = durationMs;
  }

  /**
   * Gets the duration ms.
   *
   * @return the duration ms
   */
  public long getDurationMs() {
    return durationMs;
  }

  /**
   * Sets the total to load.
   *
   * @param totalToLoad the new total to load
   */
  public void setTotalToLoad(final int totalToLoad) {
    this.totalToLoad = totalToLoad;
  }

  /**
   * Gets the total loaded.
   *
   * @return the total loaded
   */
  public int getTotalLoaded() {
    return totalLoaded;
  }

  /**
   * Gets the total errors.
   *
   * @return the total errors
   */
  public int getTotalErrors() {
    return totalErrors;
  }

  /**
   * Gets the total skipped.
   *
   * @return the total skipped
   */
  public int getTotalSkipped() {
    return totalSkipped;
  }

  /**
   * Gets the code system loaded.
   *
   * @return the code system loaded
   */
  public int getCodeSystemLoaded() {
    return codeSystemLoaded;
  }

  /**
   * Gets the code system errors.
   *
   * @return the code system errors
   */
  public int getCodeSystemErrors() {
    return codeSystemErrors;
  }

  /**
   * Gets the value set loaded.
   *
   * @return the value set loaded
   */
  public int getValueSetLoaded() {
    return valueSetLoaded;
  }

  /**
   * Gets the value set errors.
   *
   * @return the value set errors
   */
  public int getValueSetErrors() {
    return valueSetErrors;
  }

  /**
   * Gets the concept map loaded.
   *
   * @return the concept map loaded
   */
  public int getConceptMapLoaded() {
    return conceptMapLoaded;
  }

  /**
   * Gets the concept map errors.
   *
   * @return the concept map errors
   */
  public int getConceptMapErrors() {
    return conceptMapErrors;
  }

  /**
   * Gets the error messages.
   *
   * @return the error messages
   */
  public List<String> getErrorMessages() {
    return errorMessages;
  }

  /**
   * Sets the error messages.
   *
   * @param errorMessages the new error messages
   */
  public void setErrorMessages(final List<String> errorMessages) {
    this.errorMessages = errorMessages;
  }

  /**
   * Gets the message.
   *
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(codeSystemErrors, codeSystemLoaded, conceptMapErrors, conceptMapLoaded,
        durationMs, errorMessages, message, success, totalErrors, totalLoaded, totalProcessed,
        totalSkipped, totalToLoad, valueSetErrors, valueSetLoaded);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SyndicationResults other = (SyndicationResults) obj;
    return codeSystemErrors == other.codeSystemErrors && codeSystemLoaded == other.codeSystemLoaded
        && conceptMapErrors == other.conceptMapErrors && conceptMapLoaded == other.conceptMapLoaded
        && durationMs == other.durationMs && Objects.equals(errorMessages, other.errorMessages)
        && Objects.equals(message, other.message) && success == other.success
        && totalErrors == other.totalErrors && totalLoaded == other.totalLoaded
        && totalProcessed == other.totalProcessed && totalSkipped == other.totalSkipped
        && totalToLoad == other.totalToLoad && valueSetErrors == other.valueSetErrors
        && valueSetLoaded == other.valueSetLoaded;
  }

}
