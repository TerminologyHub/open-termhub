/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class StreamUtility.
 */
public final class StreamUtility {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(StreamUtility.class);

  /**
   * Instantiates a new stream utility.
   */
  private StreamUtility() {
    // Prevent instantiation
  }

  /**
   * Copy with progress.
   *
   * @param inputStream the input stream
   * @param outputStream the output stream
   * @param totalStreamLength the total stream length
   * @param messageFormat the message format
   * @return the int
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static int copyWithProgress(final InputStream inputStream, final OutputStream outputStream,
    final int totalStreamLength, final String messageFormat) throws IOException {
    int byteCount = 0;
    int bytesRead;
    int percentageLogged = -1;
    long lastLogTimeMs = 0L;
    final long logIntervalMs = 5000L;
    for (final byte[] buffer =
        new byte[4096]; (bytesRead = inputStream.read(buffer)) != -1; byteCount += bytesRead) {
      outputStream.write(buffer, 0, bytesRead);

      final long now = System.currentTimeMillis();
      final boolean totalKnownAndValid = totalStreamLength > 0 && byteCount <= totalStreamLength;

      if (totalKnownAndValid) {
        final float percentageFloat = ((float) byteCount / (float) totalStreamLength) * 100;
        final int percentage = (int) Math.floor(percentageFloat);
        if (percentage % 10 == 0 && percentage > percentageLogged
            && now - lastLogTimeMs >= logIntervalMs) {
          logger.info("{} {}%", messageFormat, percentage);
          percentageLogged = percentage;
          lastLogTimeMs = now;
        }
      } else {
        if (now - lastLogTimeMs >= logIntervalMs) {
          logger.info("{} still downloading...", messageFormat);
          lastLogTimeMs = now;
        }
      }
    }

    outputStream.flush();
    return byteCount;
  }

}
