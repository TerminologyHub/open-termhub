package com.wci.termhub.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import com.wci.termhub.model.SearchParameters;

/**
 * Performs utility functions relating to Lucene indexes.
 */
public final class IndexUtility {

	/** The logger. */
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(IndexUtility.class);

	/**
	 * Instantiates a new index utility.
	 */
	private IndexUtility() {
		// private constructor

	}

	/**
	 * Gets the indexable fields.
	 *
	 * @param obj             the obj
	 * @param field           the field
	 * @param indexNamePrefix the index name prefix
	 * @return the indexable fields
	 * @throws IllegalAccessException the illegal access exception
	 */
	public static List<IndexableField> getIndexableFields(final Object obj, final java.lang.reflect.Field field,
			final String indexNamePrefix) throws IllegalAccessException {

		logger.debug("indexableFields: field: {}, indexNamePrefix: {}", field.getName(), indexNamePrefix);

		final List<IndexableField> indexableFields = new ArrayList<>();
		field.setAccessible(true);
		final Object fieldValue = field.get(obj);
		if (fieldValue != null) {

			org.springframework.data.elasticsearch.annotations.Field annotation = field
					.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);

			if (annotation != null) {
				final String indexName = ((StringUtils.isNotEmpty(indexNamePrefix)
						? indexNamePrefix + "." + field.getName()
						: field.getName()));
				logger.info("IndexName: {} for field {}", indexName, field.getName());

				FieldType fieldType = annotation.type();
				switch (fieldType) {
				case Text:
					indexableFields.add(new TextField(indexName, fieldValue.toString(),
							org.apache.lucene.document.Field.Store.YES));
					indexableFields.add(new SortedDocValuesField(indexName, new BytesRef(fieldValue.toString())));
					break;
				case Keyword:
					indexableFields.add(new StringField(indexName, fieldValue.toString(),
							org.apache.lucene.document.Field.Store.YES));
					indexableFields.add(new SortedDocValuesField(indexName, new BytesRef(fieldValue.toString())));
					break;
				case Date:
					final long dateValue = ((java.util.Date) fieldValue).getTime();
					indexableFields.add(new NumericDocValuesField(indexName, dateValue));
					indexableFields.add(new StoredField(indexName, dateValue));
					break;
				case Long:
					indexableFields.add(new NumericDocValuesField(indexName, Long.parseLong(fieldValue.toString())));
					indexableFields.add(new StoredField(indexName, Long.parseLong(fieldValue.toString())));
					break;
				case Integer:
					indexableFields.add(new NumericDocValuesField(indexName, Integer.parseInt(fieldValue.toString())));
					indexableFields.add(new StoredField(indexName, Integer.parseInt(fieldValue.toString())));
					break;
				case Float:
					indexableFields.add(new NumericDocValuesField(indexName,
							Float.floatToRawIntBits(Float.parseFloat(fieldValue.toString()))));
					indexableFields.add(new StoredField(indexName, Float.parseFloat(fieldValue.toString())));
					break;
				case Double:
					indexableFields.add(new NumericDocValuesField(indexName,
							Double.doubleToRawLongBits(Double.parseDouble(fieldValue.toString()))));
					indexableFields.add(new StoredField(indexName, Double.parseDouble(fieldValue.toString())));
					break;
				case Boolean:
					indexableFields.add(new StringField(indexName, fieldValue.toString(),
							org.apache.lucene.document.Field.Store.YES));
					break;
				default:
					if (fieldType == FieldType.Object && fieldValue instanceof Collection) {
						final Collection<?> collection = (Collection<?>) fieldValue;
						for (final Object item : collection) {
							if (item instanceof String) {
								indexableFields.add(new StringField(indexName, (String) item,
										org.apache.lucene.document.Field.Store.YES));
							} else if (item instanceof Integer) {
								indexableFields.add(new NumericDocValuesField(indexName, (Integer) item));
								indexableFields.add(new StoredField(indexName, (Integer) item));
							}
						}
					}
				}
			} else {

				final MultiField multiFieldAnnotation = field.getAnnotation(MultiField.class);

				if (multiFieldAnnotation != null && fieldValue != null) {

					logger.debug("MF Field: {}, value: {}, annotation: {}", multiFieldAnnotation.annotationType(),
							fieldValue, multiFieldAnnotation);

					for (final InnerField innerFieldAnnotation : multiFieldAnnotation.otherFields()) {
						logger.debug("MF innerFieldAnnotation {}", innerFieldAnnotation.toString());
						final FieldType fieldTypeMF = innerFieldAnnotation.type();

						logger.debug("MF Adding multi-field: fieldName: {}, type: {}", field.getName(), fieldTypeMF);

						switch (fieldTypeMF) {
						case Text:
							logger.debug("MF Adding text field: {}, value:{}", field.getName(), fieldValue.toString());
							indexableFields.add(new TextField(field.getName(), fieldValue.toString(),
									org.apache.lucene.document.Field.Store.YES));
							break;
						case Keyword:
							logger.debug("MF Adding keyword field: {}, value:{}", field.getName(),
									fieldValue.toString());
							indexableFields.add(new TextField(field.getName(), fieldValue.toString(),
									org.apache.lucene.document.Field.Store.YES));
							indexableFields.add(
									new SortedDocValuesField(field.getName(), new BytesRef(fieldValue.toString())));
							break;
						default:
							logger.info("MultiField field not found Adding default field: {}", fieldTypeMF);

						}

					}
				}
			}
		}
		return indexableFields;
	}

	/**
	 * Gets the sort order.
	 *
	 * @param searchParameters the search parameters
	 * @param clazz            the clazz
	 * @return the sort order
	 * @throws NoSuchFieldException the no such field exception
	 */
	public static Sort getSortOrder(final SearchParameters searchParameters, final Class clazz)
			throws NoSuchFieldException {

		final List<String> sortFields = searchParameters.getSort();
		final SortField[] sortFieldArray = new SortField[sortFields.size()];

		for (int i = 0; i < sortFields.size(); i++) {

			final String sortField = sortFields.get(i);

			java.lang.reflect.Field field = null;
			Class<?> currentClass = clazz;
			while (currentClass != null && field == null) {
				try {
					field = currentClass.getDeclaredField(sortField);
				} catch (NoSuchFieldException e) {
					currentClass = currentClass.getSuperclass();
				}
			}

			if (field == null) {
				throw new NoSuchFieldException(
						"Field " + sortField + " not found in class " + clazz.getName() + " or its superclasses");
			}

			SortField.Type sortType;
			final String fieldType = field.getType().getSimpleName();
			switch (fieldType) {
			case "String":
				sortType = SortField.Type.STRING;
				break;
			case "Integer":
				sortType = SortField.Type.INT;
				break;
			case "Long":
			case "Date":
			case "Instant":
				sortType = SortField.Type.LONG;
				break;
			case "Float":
				sortType = SortField.Type.FLOAT;
				break;
			case "Double":
				sortType = SortField.Type.DOUBLE;
				break;
			default:
				throw new IllegalArgumentException("Unsupported field type for sorting: " + field.getType());
			}

			sortFieldArray[i] = new SortField(sortField, sortType, !searchParameters.getAscending());
		}

		return new Sort(sortFieldArray);
	}
}
