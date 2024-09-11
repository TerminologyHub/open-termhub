package com.wci.termhub.open.configuration;

import java.util.Iterator;
import java.util.Properties;

/**
 * Application properties configuration.
 */

@SuppressWarnings("serial")
public class ApplicationProperties extends Properties {

	/**
	 * Returns the prefixed properties.
	 *
	 * @param prefix the prefix
	 * @return the prefixed properties
	 * @throws Exception the exception
	 */
	public Properties getPrefixedProperties(final String prefix) throws Exception {
		return getPrefixedProperties(prefix, true);
	}

	/**
	 * Returns the prefixed properties.
	 *
	 * @param prefix       the prefix
	 * @param removePrefix the remove prefix
	 * @return the prefixed properties
	 * @throws Exception the exception
	 */
	public Properties getPrefixedProperties(final String prefix, final boolean removePrefix) throws Exception {

		final Properties propertiesSubset = new Properties();
		final Iterator<Object> keys = keySet().iterator();

		// get any properties that start with the prefix
		while (keys.hasNext()) {

			String key = keys.next().toString();
			final String originalKey = key;

			if (key.startsWith(prefix)) {

				if (removePrefix) {
					key = key.replace(prefix, "");
				}

				propertiesSubset.put(key, getProperty(originalKey));
			}
		}

		return propertiesSubset;
	}
}