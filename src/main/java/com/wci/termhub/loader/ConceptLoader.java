package com.wci.termhub.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wci.termhub.lucene.LuceneDataAccess;
import com.wci.termhub.model.Concept;

/**
 * The Class ConceptLoader.
 */
public class ConceptLoader {

	/** The logger. */
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ConceptLoader.class);

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(final String[] args) {

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

		int limit = -1;
		if (args.length == 2 && StringUtils.isNotBlank(args[1])) {
			limit = Integer.parseInt(args[1]);
		}
		System.out.println("limit: " + limit);

		// read the file
		// for each line in the file, convert to Concept object.
		try (final BufferedReader br = new BufferedReader(new FileReader(fullFileName))) {

			final ObjectMapper objectMapper = new ObjectMapper();
			final LuceneDataAccess<Concept> luceneData = new LuceneDataAccess<>();
			luceneData.createIndex(Concept.class);

			String line;
			int count = 0;
			while ((line = br.readLine()) != null && (limit == -1 || count < limit)) {

				// convert to Concept object
				final JsonNode rootNode = objectMapper.readTree(line);
				final JsonNode conceptNode = rootNode.get("_source");
				final Concept concept = objectMapper.treeToValue(conceptNode, Concept.class);

				// add to LuceneDao object
				luceneData.add(concept);
				System.out.println("count: " + count);
				count++;
			}
		} catch (Exception e) {
			logger.error("An error occurred while processing the file.");
			e.printStackTrace();
			System.exit(1);
		}

		System.exit(0);

	}

}
