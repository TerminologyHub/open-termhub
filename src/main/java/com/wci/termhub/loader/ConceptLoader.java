/*
 *
 */
package com.wci.termhub.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.lucene.LuceneDataAccess;
import com.wci.termhub.model.Concept;
import com.wci.termhub.model.Term;
import com.wci.termhub.util.ModelUtility;

/**
 * The Class ConceptLoader.
 */
public final class ConceptLoader {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(ConceptLoader.class);

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(final String[] args) {

		try {

			if (args == null || args.length == 0 || StringUtils.isBlank(args[0])) {
				logger.error("File name is required.");
				System.exit(1);
			}

			// get file name from command line
			final String fullFileName = args[0];
			if (!Files.exists(Paths.get(fullFileName))) {
				logger.error("File does not exist at " + fullFileName);
				System.exit(1);
			}

			int batchSize = 1000;
			if (args.length > 1 && StringUtils.isNotBlank(args[1])) {
				batchSize = Integer.parseInt(args[1]);
			}

			int limit = -1;
			if (args.length > 2 && StringUtils.isNotBlank(args[2])) {
				limit = Integer.parseInt(args[2]);
			}

			index(fullFileName, batchSize, limit);

		} catch (final Exception e) {
			logger.error("An error occurred while loading the file.");
			e.printStackTrace();
			System.exit(1);
		}

		System.exit(0);

	}

	/**
	 * Index all.
	 *
	 * @param fullFileName the full file name
	 * @param batchSize    the batch size
	 * @throws Exception the exception
	 */
	public static void indexAll(final String fullFileName, final int batchSize) throws Exception {
		index(fullFileName, batchSize, -1);
	}

	/**
	 * Index.
	 *
	 * @param fullFileName the full file name
	 * @param batchSize    the batch size
	 * @param limit        the limit
	 * @throws Exception the exception
	 */
	public static void index(final String fullFileName, final int batchSize, final int limit) throws Exception {

		System.out.println("batch size: " + batchSize + " limit: " + limit);
		final long startTime = System.currentTimeMillis();

		final List<Concept> conceptBatch = new ArrayList<>(batchSize);
		final List<Term> termBatch = new ArrayList<>(batchSize);

		// read the file
		// for each line in the file, convert to Concept object.
		try (final BufferedReader br = new BufferedReader(new FileReader(fullFileName))) {

			final ObjectMapper objectMapper = new ObjectMapper();
			final LuceneDataAccess luceneDataAccess = new LuceneDataAccess();
			luceneDataAccess.createIndex(Concept.class);
			luceneDataAccess.createIndex(Term.class);

			String line;
			int conceptCount = 1;
			int termCount = 0;
			while ((line = br.readLine()) != null && (limit == -1 || conceptCount < limit)) {

				final JsonNode rootNode = objectMapper.readTree(line);
				final JsonNode conceptNode = (rootNode.has("_source")) ? rootNode.get("_source") : rootNode;
				final Concept concept = ModelUtility.fromJson(conceptNode.toString(), Concept.class);

				if (concept.getTerms() != null) {
					for (final Term term : concept.getTerms()) {
						termBatch.add(term);
						if (termBatch.size() == batchSize) {
							luceneDataAccess.add(termBatch);
							termBatch.clear();
						}
						termCount++;
					}
				}

				conceptBatch.add(concept);

				if (conceptBatch.size() == batchSize) {
					luceneDataAccess.add(conceptBatch);
					conceptBatch.clear();
					System.out.println("count: " + conceptCount);
				}

				conceptCount++;
			}

			if (!conceptBatch.isEmpty()) {
				luceneDataAccess.add(conceptBatch);
			}
			if (!termBatch.isEmpty()) {
				luceneDataAccess.add(termBatch);
			}

			System.out.println("final concepts added count: " + conceptCount);
			System.out.println("final terms added count: " + termCount);
			System.out.println("duration: " + (System.currentTimeMillis() - startTime) + " ms");

		} catch (final Exception e) {
			logger.error("An error occurred while processing the file.");
			e.printStackTrace();
			System.exit(1);
		}

	}

}
