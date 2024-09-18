package com.wci.termhub.test;

import java.util.Collection;

public class BaseUnitTest {

	/**
	 * Gets the size.
	 *
	 * @param <T>      the generic type
	 * @param iterable the iterable
	 * @return the size
	 */
	@SuppressWarnings("unused")
	protected static <T> int getSize(final Iterable<T> iterable) {
		if (iterable instanceof Collection) {
			return ((Collection<T>) iterable).size();
		} else {
			int size = 0;
			for (final T item : iterable) {
				size++;
			}
			return size;
		}
	}
}
