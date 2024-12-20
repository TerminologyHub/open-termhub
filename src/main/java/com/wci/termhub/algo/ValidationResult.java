/*
 * Copyright 2024 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.algo;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.wci.termhub.util.ModelUtility;

/**
 * Represents a validation result.
 */
public class ValidationResult {

	/** The errors. */
	private Set<String> errors = new HashSet<>();

	/** The warnings. */
	private Set<String> warnings = new HashSet<>();

	/** The comments. */
	private Set<String> comments = new HashSet<>();

	/**
	 * Instantiates an empty {@link ValidationResult}.
	 */
	public ValidationResult() {
		// do nothing
	}

	/**
	 * Instantiates a {@link ValidationResult} from the specified parameters.
	 *
	 * @param result the result
	 */
	public ValidationResult(final ValidationResult result) {
		errors = new TreeSet<>(result.getErrors());
		warnings = new TreeSet<>(result.getWarnings());
		comments = new TreeSet<>(result.getComments());
	}

	/**
	 * Indicates whether or not valid is the case.
	 *
	 * @return <code>true</code> if so, <code>false</code> otherwise
	 */
	public boolean isValid() {
		return errors.size() == 0;
	}

	/**
	 * For JAXB.
	 *
	 * @param b the valid
	 */
	public void setValid(final boolean b) {
		// do nothing
	}

	/**
	 * Returns the errors.
	 *
	 * @return the errors
	 */
	public Set<String> getErrors() {
		return errors;
	}

	/**
	 * Sets the errors.
	 *
	 * @param errors the errors
	 */
	public void setErrors(final Set<String> errors) {
		this.errors = errors;
	}

	/**
	 * Returns the warnings.
	 *
	 * @return the warnings
	 */
	public Set<String> getWarnings() {
		return warnings;
	}

	/**
	 * Sets the warnings.
	 *
	 * @param warnings the warnings
	 */
	public void setWarnings(final Set<String> warnings) {
		this.warnings = warnings;
	}

	/**
	 * Returns the comments.
	 *
	 * @return the comments
	 */
	public Set<String> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	/**
	 * Sets the comments.
	 *
	 * @param comments the comments
	 */
	public void setComments(final Set<String> comments) {
		this.comments = comments;
	}

	/**
	 * Merge.
	 *
	 * @param validationResult the validation result
	 */
	public void merge(final ValidationResult validationResult) {

		this.errors.addAll(validationResult.getErrors());
		this.warnings.addAll(validationResult.getWarnings());
		this.comments.addAll(validationResult.getComments());

	}

	/**
	 * Adds the error.
	 *
	 * @param error the error
	 */
	public void addError(final String error) {
		this.errors.add(error);
	}

	/**
	 * Adds the warning.
	 *
	 * @param warning the warning
	 */
	public void addWarning(final String warning) {
		this.warnings.add(warning);
	}

	/* see superclass */
	@Override
	public String toString() {
		return ModelUtility.toJson(this);
	}

	/* see superclass */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((errors == null) ? 0 : errors.hashCode());
		result = prime * result + ((warnings == null) ? 0 : warnings.hashCode());
		result = prime * result + ((comments == null) ? 0 : comments.hashCode());
		return result;
	}

	/* see superclass */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ValidationResult other = (ValidationResult) obj;
		if (errors == null) {
			if (other.errors != null) {
				return false;
			}
		} else if (!errors.equals(other.errors)) {
			return false;
		}
		if (warnings == null) {
			if (other.warnings != null) {
				return false;
			}
		} else if (!warnings.equals(other.warnings)) {
			return false;
		}
		if (comments == null) {
			if (other.comments != null) {
				return false;
			}
		} else if (!comments.equals(other.comments)) {
			return false;
		}
		return true;
	}

}
