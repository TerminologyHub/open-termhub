package com.wci.termhub.lucene;

//import java.io.IOException;
//import java.nio.file.Paths;
//
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.index.IndexWriter;
//import org.apache.lucene.index.IndexWriterConfig;
//import org.apache.lucene.store.Directory;
//import org.apache.lucene.store.FSDirectory;
//import org.springframework.data.elasticsearch.annotations.Field;
//import org.springframework.data.elasticsearch.annotations.FieldType;
//import org.springframework.data.elasticsearch.annotations.InnerField;
//import org.springframework.data.elasticsearch.annotations.MultiField;
//
///**
// * The Class LuceneDao2.
// */
//public class LuceneDao2 {
//	
//	/** The index dir. */
//	private final Directory indexDir;
//
//	/**
//	 * Instantiates a new lucene dao 2.
//	 *
//	 * @param indexPath the index path
//	 * @throws IOException Signals that an I/O exception has occurred.
//	 */
//	public LuceneDao2(String indexPath) throws IOException {
//		indexDir = FSDirectory.open(Paths.get(indexPath));
//	}
//
//	/**
//	 * Index.
//	 *
//	 * @param pojo the pojo
//	 * @throws IOException Signals that an I/O exception has occurred.
//	 * @throws IllegalAccessException the illegal access exception
//	 */
//	public void index(Object pojo) throws IOException, IllegalAccessException {
//
//		final IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
//
//		try (final IndexWriter writer = new IndexWriter(indexDir, config);) {
//
//			final Document document = new Document();
//
//			for (java.lang.reflect.Field field : pojo.getClass().getDeclaredFields()) {
//				field.setAccessible(true);
//				final Object fieldValue = field.get(pojo);
//
//				final MultiField multiFieldAnnotation = field.getAnnotation(MultiField.class);
//				if (multiFieldAnnotation != null) {
//					for (final InnerField innerFieldAnnotation : multiFieldAnnotation.value()) {
//						final String fieldName = innerFieldAnnotation.name();
//						final FieldType fieldType = innerFieldAnnotation.type();
//						// Create Lucene field based on fieldType and add to document
//					}
//				} else {
//					final Field annotation = field.getAnnotation(Field.class);
//					if (annotation != null) {
//						final FieldType fieldType = annotation.type();
//						// Create Lucene field based on fieldType and add to document
//					}
//				}
//			}
//
//			writer.addDocument(document);
//		}
//	}
//
//	// ... search method
//}
