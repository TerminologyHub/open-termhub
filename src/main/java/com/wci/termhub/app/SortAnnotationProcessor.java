package com.wci.termhub.app;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * The Class SortAnnotationProcessor.
 */
public class SortAnnotationProcessor {

	/**
	 * Process.
	 *
	 * @param obj the obj
	 * @throws Exception the exception
	 */
	public void process(Object obj) throws Exception {
		final Set<Integer> orders = new HashSet<>();
		for (final Field field : obj.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Sort.class)) {
				int order = field.getAnnotation(Sort.class).order();
				if (!orders.add(order)) {
					throw new Exception("Duplicate order value: " + order);
				}
			}
		}
	}
}