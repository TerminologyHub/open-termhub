/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.util.FileUtility;

/**
 * Unit tests for {@link FileUtility}.
 */
public class FileUtilityUnitTest {

  /** The logger. */
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(FileUtilityUnitTest.class);

  /** The temp directory. */
  @TempDir
  private Path tempDir;

  /** The test directory. */
  private Path testDir;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    testDir = tempDir.resolve("testDir");
    Files.createDirectory(testDir);
  }

  /**
   * Test delete directory and all files.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteDirectoryAndAllFiles() throws Exception {
    // Create test files and subdirectories
    final Path subDir = testDir.resolve("subDir");
    Files.createDirectory(subDir);
    Files.createFile(testDir.resolve("file1.txt"));
    Files.createFile(subDir.resolve("file2.txt"));

    // Verify files exist
    assertTrue(Files.exists(testDir));
    assertTrue(Files.exists(subDir));
    assertTrue(Files.exists(testDir.resolve("file1.txt")));
    assertTrue(Files.exists(subDir.resolve("file2.txt")));

    // Delete directory
    FileUtility.deleteDirectoryAndAllFiles(testDir);

    // Verify everything is deleted
    assertFalse(Files.exists(testDir));
    assertFalse(Files.exists(subDir));
    assertFalse(Files.exists(testDir.resolve("file1.txt")));
    assertFalse(Files.exists(subDir.resolve("file2.txt")));
  }

  /**
   * Test delete non existent directory.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteNonExistentDirectory() throws Exception {
    final Path nonExistentDir = tempDir.resolve("nonexistent");
    // Should not throw exception
    FileUtility.deleteDirectoryAndAllFiles(nonExistentDir);
  }

  /**
   * Test extract zip file.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExtractZipFile() throws Exception {
    // Create a test zip file
    final Path zipFile = tempDir.resolve("test.zip");
    final Path extractDir = tempDir.resolve("extract");

    createTestZipFile(zipFile.toString(), "test.txt", "Hello World!");

    // Extract the zip file
    FileUtility.extractZipFile(zipFile.toString(), extractDir.toString());

    // Verify extraction
    assertTrue(Files.exists(extractDir));
    assertTrue(Files.exists(extractDir.resolve("test.txt")));
    final String content = new String(Files.readAllBytes(extractDir.resolve("test.txt")));
    assertTrue(content.contains("Hello World!"));
  }

  /**
   * Test get zipped file with path traversal attempt.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetZippedFileWithPathTraversalAttempt() throws Exception {
    final File destDir = tempDir.toFile();
    final ZipEntry maliciousEntry = new ZipEntry("../outside.txt");

    // Should throw IOException for path traversal attempt
    assertThrows(IOException.class, () -> {
      FileUtility.getZippedFile(destDir, maliciousEntry);
    });
  }

  /**
   * Creates a test zip file.
   *
   * @param zipPath the zip path
   * @param entryName the entry name
   * @param content the content
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void createTestZipFile(final String zipPath, final String entryName, final String content)
    throws IOException {
    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
      final ZipEntry entry = new ZipEntry(entryName);
      zos.putNextEntry(entry);
      zos.write(content.getBytes());
      zos.closeEntry();
    }
  }
}
