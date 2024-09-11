package com.wci.termhub.model;

import javax.persistence.MappedSuperclass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.util.ModelUtility;

/**
 * Base model for all classes.
 */
@MappedSuperclass
public abstract class BaseModel {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(BaseModel.class);

	/**
	 * Instantiates an empty {@link BaseModel}.
	 */
	protected BaseModel() {
		// n/a
	}

	/* see superclass */
	@Override
	public String toString() {
		try {
			return ModelUtility.toJson(this);
		} catch (final Exception e) {
			logger.error("Unexpected error serializing object", e);
			throw new RuntimeException(e);
		}
	}
}