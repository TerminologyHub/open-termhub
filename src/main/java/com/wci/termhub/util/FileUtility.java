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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 * File utility.
 */
public final class FileUtility {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(FileUtility.class);

  /**
   * Instantiates a new file utility.
   */
  private FileUtility() {
    // private constructor
  }

  /**
   * Delete directory and all files.
   *
   * @param path the path
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void deleteDirectoryAndAllFiles(final Path path) throws IOException {

    if (!Files.exists(path)) {
      return;
    }

    FileUtility.deleteDirectoryRecursively(path);
    Files.deleteIfExists(path);
  }

  /**
   * Delete directory recursively.
   *
   * @param path the path
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void deleteDirectoryRecursively(final Path path) throws IOException {
    if (!Files.exists(path)) {
      return;
    }

    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
        throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
        throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * Extract zip file.
   *
   * @param zipFilePath the zip file path
   * @param destDir the dest dir
   */
  public static void extractZipFile(final String zipFilePath, final String destDir) {

    final File destinationDir = new File(destDir);
    // create output directory if it doesn't exist
    if (!destinationDir.exists()) {
      destinationDir.mkdirs();
    }
    logger.info("  starting extracting files to {}", destinationDir.getAbsolutePath());

    try (final InputStream fis = Files.newInputStream(Paths.get(zipFilePath));
        final ZipInputStream zis = new ZipInputStream(fis)) {

      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {

        final File newFile = getZippedFile(destinationDir, zipEntry);

        logger.info("    file = {}", newFile.getAbsolutePath());

        try (final FileOutputStream fos = new FileOutputStream(newFile)) {
          final byte[] buffer = new byte[1024];
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
        }
        zipEntry = zis.getNextEntry();
      }
      zis.closeEntry();

    } catch (final IOException e) {
      logger.error("Error occurred while extracting zip file", e);
    }
  }

  /**
   * Get zipped file.
   *
   * @param destinationDir the destination dir
   * @param zipEntry the zip entry
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static File getZippedFile(final File destinationDir, final ZipEntry zipEntry)
    throws IOException {

    final File destFile = new File(destinationDir, zipEntry.getName());
    final String destDirPath = destinationDir.getCanonicalPath();
    final String destFilePath = destFile.getCanonicalPath();
    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }
    return destFile;
  }

  /**
   * Extract files.
   *
   * @param sourceFile the source file
   * @param tempDirName the temp dir name
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static File extractFiles(final MultipartFile sourceFile, final String tempDirName)
    throws IOException {

    final File tempDir = Files.createTempDirectory(tempDirName).toFile();
    try (final ZipInputStream zis = new ZipInputStream(sourceFile.getInputStream())) {
      ZipEntry zipEntry;
      while ((zipEntry = zis.getNextEntry()) != null) {
        final File newFile = new File(tempDir, zipEntry.getName());
        if (zipEntry.isDirectory()) {
          newFile.mkdirs();
        } else {
          new File(newFile.getParent()).mkdirs();
          try (final FileOutputStream fos = new FileOutputStream(newFile)) {
            final byte[] buffer = new byte[1024];
            int len;
            while ((len = zis.read(buffer)) > 0) {
              fos.write(buffer, 0, len);
            }
          }
        }
        zis.closeEntry();
      }
    }
    return tempDir;
  }

}
