/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.syndication;

import java.util.Date;

/**
 * Status for an asynchronous syndication job.
 */
public class SyndicationJobStatus {

  /** The job status. */
  public enum Status {
    /** The job is queued. */
    QUEUED,
    /** The job is running. */
    RUNNING,
    /** The job completed successfully. */
    COMPLETED,
    /** The job failed. */
    FAILED
  }

  /** The process id. */
  private String processId;

  /** The status. */
  private Status status;

  /** The message. */
  private String message;

  /** The created timestamp. */
  private Date created;

  /** The started timestamp. */
  private Date started;

  /** The finished timestamp. */
  private Date finished;

  /** The result. */
  private SyndicationResults result;

  /**
   * Instantiates an empty {@link SyndicationJobStatus}.
   */
  public SyndicationJobStatus() {
    // n/a
  }

  /**
   * Instantiates a {@link SyndicationJobStatus} from the specified parameters.
   *
   * @param processId the process id
   */
  public SyndicationJobStatus(final String processId) {
    this.processId = processId;
    status = Status.QUEUED;
    message = "Syndication job queued";
    created = new Date();
  }

  /**
   * Mark running.
   */
  public void markRunning() {
    status = Status.RUNNING;
    message = "Syndication job running";
    started = new Date();
  }

  /**
   * Mark completed.
   *
   * @param result the result
   */
  public void markCompleted(final SyndicationResults result) {
    status = Status.COMPLETED;
    this.result = result;
    message = result == null ? "Syndication job completed" : result.getMessage();
    finished = new Date();
  }

  /**
   * Mark failed.
   *
   * @param message the message
   * @param result the result
   */
  public void markFailed(final String message, final SyndicationResults result) {
    status = Status.FAILED;
    this.message = message;
    this.result = result;
    finished = new Date();
  }

  /**
   * Returns the process id.
   *
   * @return the process id
   */
  public String getProcessId() {
    return processId;
  }

  /**
   * Sets the process id.
   *
   * @param processId the process id
   */
  public void setProcessId(final String processId) {
    this.processId = processId;
  }

  /**
   * Returns the status.
   *
   * @return the status
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Sets the status.
   *
   * @param status the status
   */
  public void setStatus(final Status status) {
    this.status = status;
  }

  /**
   * Returns the message.
   *
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the message.
   *
   * @param message the message
   */
  public void setMessage(final String message) {
    this.message = message;
  }

  /**
   * Returns the created timestamp.
   *
   * @return the created timestamp
   */
  public Date getCreated() {
    return created;
  }

  /**
   * Sets the created timestamp.
   *
   * @param created the created timestamp
   */
  public void setCreated(final Date created) {
    this.created = created;
  }

  /**
   * Returns the started timestamp.
   *
   * @return the started timestamp
   */
  public Date getStarted() {
    return started;
  }

  /**
   * Sets the started timestamp.
   *
   * @param started the started timestamp
   */
  public void setStarted(final Date started) {
    this.started = started;
  }

  /**
   * Returns the finished timestamp.
   *
   * @return the finished timestamp
   */
  public Date getFinished() {
    return finished;
  }

  /**
   * Sets the finished timestamp.
   *
   * @param finished the finished timestamp
   */
  public void setFinished(final Date finished) {
    this.finished = finished;
  }

  /**
   * Returns the result.
   *
   * @return the result
   */
  public SyndicationResults getResult() {
    return result;
  }

  /**
   * Sets the result.
   *
   * @param result the result
   */
  public void setResult(final SyndicationResults result) {
    this.result = result;
  }
}
