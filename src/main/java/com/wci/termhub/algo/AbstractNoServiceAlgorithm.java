/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.algo;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.util.LocalException;
import com.wci.termhub.util.ModelUtility;

/**
 * The Class AbstractAlgorithm.
 */
public abstract class AbstractNoServiceAlgorithm implements NoServiceAlgorithm {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(AbstractNoServiceAlgorithm.class);

  /** The listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The result. */
  private ValidationResult validationResult = new ValidationResult();

  /** The cancel flag. */
  private boolean cancelFlag = false;

  /**
   * Instantiates an empty {@link AbstractNoServiceAlgorithm}.
   *
   * @throws Exception the exception
   */
  public AbstractNoServiceAlgorithm() throws Exception {
    // n/a
  }

  /**
   * Log info.
   *
   * @param message the message
   * @throws Exception the exception
   */
  public void logInfo(final String message) throws Exception {
    logger.info(message);
  }

  /**
   * Log warn.
   *
   * @param message the message
   * @throws Exception the exception
   */
  public void logWarn(final String message) throws Exception {
    fireWarningEvent(message);
    logger.warn(message);
  }

  /**
   * Log error.
   *
   * @param message the message
   * @throws Exception the exception
   */
  public void logError(final String message) throws Exception {
    logger.error(message);
  }

  /* see superclass */
  @Override
  public void cancel() throws Exception {
    cancelFlag = true;
  }

  /**
   * Indicates whether or not cancelled is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isCancelled() {
    return cancelFlag;
  }

  /**
   * Check cancel.
   *
   * @return true, if successful
   * @throws Exception the exception
   * @throws CancelException the cancel exception
   */
  public boolean checkCancel() throws Exception, CancelException {
    if (isCancelled()) {
      throw new CancelException("Operation cancelled");
    }
    return false;
  }

  /**
   * Returns the total elapsed time str.
   *
   * @param time the time
   * @return the total elapsed time str
   */
  public static String getTotalElapsedTimeStr(final long time) {
    long resultnum = (System.nanoTime() - time) / 1000000000;
    String result = Long.toString(resultnum) + "s";
    resultnum = resultnum / 60;
    result = result + " / " + Long.toString(resultnum) + "m";
    resultnum = resultnum / 60;
    result = result + " / " + Long.toString(resultnum) + "h";
    return result;
  }

  /**
   * Fire progress event.
   *
   * @param pct the pct
   * @param note the note
   * @throws Exception the exception
   */
  public void fireProgressEvent(final int pct, final String note) throws Exception {
    final ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    // don't write this to a log entry
    logger.info("    " + pct + "% " + note);
  }

  /**
   * Fire adjusted progress event.
   *
   * @param pct the pct
   * @param step the step
   * @param steps the steps
   * @param note the note
   * @throws Exception the exception
   */
  public void fireAdjustedProgressEvent(final int pct, final int step, final int steps,
    final String note) throws Exception {
    final ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    logInfo("    " + ((int) (((pct * 1.0) / steps) + ((step - 1) * 100.0 / steps))) + "% " + note);
  }

  /**
   * Fire warning event.
   *
   * @param note the note
   * @throws Exception the exception
   */
  public void fireWarningEvent(final String note) throws Exception {
    final ProgressEvent pe = new ProgressEvent(note, true);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
  }

  /* see superclass */
  @Override
  public void addProgressListener(final ProgressListener l) {
    listeners.add(l);
  }

  /* see superclass */
  @Override
  public void removeProgressListener(final ProgressListener l) {
    listeners.remove(l);
  }

  /* see superclass */
  @Override
  public String getName() {
    return ModelUtility.getNameFromClass(getClass());
  }

  /**
   * Check required properties.
   *
   * @param required the required
   * @param p the p
   * @throws Exception the exception
   */
  public void checkRequiredProperties(final String[] required, final Properties p)
    throws Exception {
    if (p == null) {
      throw new LocalException("Algorithm properties must not be null");
    }
    for (final String prop : required) {
      if (!prop.equals("") && !p.containsKey(prop)) {
        throw new LocalException("Required property " + prop + " missing");
      }
    }
  }

  /**
   * Returns the result.
   *
   * @return the result
   */
  public ValidationResult getValidationResult() {
    return validationResult;
  }

  /**
   * Sets the result.
   *
   * @param validationResult the result
   */
  public void setValidationResult(final ValidationResult validationResult) {
    this.validationResult = validationResult;
  }

}
