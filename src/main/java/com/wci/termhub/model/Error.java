/*
 * Copyright 2024 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model;

import java.util.Date;

/**
 * Represents an event.
 */
public class Error extends BaseModel {

	/** The timestamp. */
	private Date timestamp;

	/** The local. */
	private boolean local;

	/** The status. */
	private int status;

	/** The error. */
	private String error;

	/** The message. */
	private String message;

	/**
	 * Instantiates an empty {@link Error}.
	 */
	public Error() {
		// n/a
	}

	/**
	 * Instantiates a {@link Error} from the specified parameters.
	 *
	 * @param local   the local
	 * @param status  the status
	 * @param error   the description
	 * @param message the message
	 */
	public Error(final boolean local, final int status, final String error, final String message) {
		this.timestamp = new Date();
		this.local = local;
		this.status = status;
		this.error = error;
		this.message = message;
	}

	/**
	 * Instantiates a {@link Error} from the specified parameters.
	 *
	 * @param other the other
	 */
	public Error(final Error other) {
		populateFrom(other);
	}

	/**
	 * Populate from.
	 *
	 * @param other the other
	 */
	public void populateFrom(final Error other) {
		timestamp = other.getTimestamp();
		local = other.isLocal();
		status = other.getStatus();
		error = other.getError();
		message = other.getMessage();
	}

	/**
	 * Returns the timestamp.
	 *
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp.
	 *
	 * @param timestamp the timestamp
	 */
	public void setTimestamp(final Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Indicates whether or not local is the case.
	 *
	 * @return <code>true</code> if so, <code>false</code> otherwise
	 */
	public boolean isLocal() {
		return local;
	}

	/**
	 * Sets the local.
	 *
	 * @param local the local
	 */
	public void setLocal(final boolean local) {
		this.local = local;
	}

	/**
	 * Returns the status.
	 *
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the status
	 */
	public void setStatus(final int status) {
		this.status = status;
	}

	/**
	 * Returns the error.
	 *
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	/**
	 * Sets the error.
	 *
	 * @param error the error
	 */
	public void setError(final String error) {
		this.error = error;
	}

	/**
	 * Returns the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 *
	 * @param message the message
	 */
	public void setMessage(final String message) {
		this.message = message;
	}

	/* see superclass */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((error == null) ? 0 : error.hashCode());
		result = prime * result + (local ? 1231 : 1237);
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + status;
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}

	/* see superclass */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		final Error other = (Error) obj;
		if (error == null) {
			if (other.error != null) {
				return false;
			}
		} else if (!error.equals(other.error)) {
			return false;
		}
		if (local != other.local) {
			return false;
		}
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		if (timestamp == null) {
			if (other.timestamp != null) {
				return false;
			}
		} else if (!timestamp.equals(other.timestamp)) {
			return false;
		}
		return true;
	}

}
