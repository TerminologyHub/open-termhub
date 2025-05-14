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

/**
 * Represents an update to progress information.
 */
public class ProgressEvent {

  /** The source. */
  private Object source;

  /** The pct. */
  private int pct = 0;

  /** The progress. */
  private long progress = 0;

  /** The note. */
  private String note = null;

  /** The warning. */
  private boolean warning = false;

  /**
   * Instantiates a {@link ProgressEvent} from the specified information.
   * 
   * @param source event source
   * @param pct percent finished
   * @param progress total progress value
   * @param note progress note
   */
  public ProgressEvent(final Object source, final int pct, final long progress, final String note) {
    this.pct = pct;
    this.progress = progress;
    this.note = note;
    this.source = source;
  }

  /**
   * Instantiates a {@link ProgressEvent} from the specified information.
   *
   * @param note progress note
   * @param warning the warning
   */
  public ProgressEvent(final String note, final boolean warning) {
    this.note = note;
    this.warning = warning;
  }

  /**
   * Returns the percentage completed.
   * 
   * @return the percentage completed
   */
  public int getPercent() {
    return pct;
  }

  /**
   * Returns the scaled percentage. It maps a 0 to 100 range to the specified range. E.g. If
   * getPercent() returns 50, getScaledPercent(50,100) eturns 75.
   * 
   * @param low the low end of the scale
   * @param high the high end of the scale
   * @return the scaled percentage completed
   */
  public int getScaledPercent(final int low, final int high) {
    if (pct < 0) {
      return low;
    }
    if (pct > 100) {
      return high;
    }
    return (((high - low) * pct) / 100) + low;
  }

  /**
   * Returns the progress.
   * 
   * @return the progress
   */
  public long getProgress() {
    return progress;
  }

  /**
   * Returns the note.
   * 
   * @return the note
   */
  public String getNote() {
    return note;
  }

  /**
   * Returns the source.
   * 
   * @return the source
   */
  public Object getSource() {
    return source;
  }

  /**
   * Indicates whether or not warning is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isWarning() {
    return warning;
  }
}
