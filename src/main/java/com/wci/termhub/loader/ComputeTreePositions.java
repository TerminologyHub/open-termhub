/*
 *
 */
package com.wci.termhub.loader;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.stereotype.Component;
//
//import com.wci.termhub.algo.TreePositionAlgorithm;
//
///**
// * The Class ComputeTreePositions.
// */
//@Component
//public class ComputeTreePositions {
//
//	/** The logger. */
//	private static final Logger LOG = LoggerFactory.getLogger(ComputeTreePositions.class);
//
//	/** The application context. */
//	@Autowired
//	private ApplicationContext applicationContext;
//
//	/** The treepos. */
//	private static TreePositionAlgorithm treepos;
//
//	/**
//	 * The main method.
//	 *
//	 * @param args the arguments
//	 * @throws Exception the exception
//	 */
//	public static void main(final String[] args) throws Exception {
//
//		try {
//
//			if (args.length != 3) {
//				throw new Exception("Usage: ... markLatest <terminology> <publisher> <version>");
//			}
//
//			final String terminology = args[0];
//			final String publisher = args[1];
//			final String version = args[2];
//
//			computeTreePositions(terminology, publisher, version);
//
//		} catch (final Exception e) {
//			LOG.error("Error: " + e.getMessage(), e);
//			System.exit(1);
//		}
//
//		System.exit(0);
//	}
//
//	/**
//	 * Compute tree positions.
//	 *
//	 * @param terminology the terminology
//	 * @param publisher   the publisher
//	 * @param version     the version
//	 * @throws Exception the exception
//	 */
//	public static void computeTreePositions(final String terminology, final String publisher, final String version)
//			throws Exception {
//
//		treepos = new TreePositionAlgorithm();
//		// latest is injected via Spring
//		treepos.setTerminology(terminology);
//		treepos.setPublisher(publisher);
//		treepos.setVersion(version);
//		treepos.checkPreconditions();
//		treepos.compute();
//	}
//}
