package com.wci.termhub.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileUtility {

	/** The logger. */
	@SuppressWarnings("unused")
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
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

}
