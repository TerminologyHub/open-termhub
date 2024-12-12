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

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents information about a file.
 */
@Schema(description = "Represents information about a file")
public class FileInfo extends BaseModel implements Copyable<FileInfo> {

	/** The uri. */
	private String uri;

	/** The mime type. */
	private String mimeType;

	/** The hash. */
	private String hash;

	/** The length. */
	private Long length;

	/** The base 64 encoded. */
	private Boolean base64Encoded;

	/**
	 * Instantiates an empty {@link FileInfo}.
	 */
	public FileInfo() {
		// n/a
	}

	/**
	 * Instantiates a {@link FileInfo} from the specified parameters.
	 *
	 * @param other the other
	 */
	public FileInfo(final FileInfo other) {
		populateFrom(other);
	}

	/* see superclass */
	@Override
	public void populateFrom(final FileInfo other) {
		uri = other.getUri();
		mimeType = other.getMimeType();
		hash = other.getHash();
		length = other.getLength();
		base64Encoded = other.getBase64Encoded();
	}

	/* see superclass */
	@Override
	public void patchFrom(final FileInfo other) {
		if (other.getUri() != null) {
			uri = other.getUri();
		}
		if (other.getMimeType() != null) {
			mimeType = other.getMimeType();
		}
		if (other.getHash() != null) {
			hash = other.getHash();
		}
		if (other.getLength() != null) {
			length = other.getLength();
		}
		if (other.getBase64Encoded() != null) {
			base64Encoded = other.getBase64Encoded();
		}
	}

	/**
	 * Returns the uri.
	 *
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the uri.
	 *
	 * @param uri the uri
	 */
	public void setUri(final String uri) {
		this.uri = uri;
	}

	/**
	 * Returns the mime type.
	 *
	 * @return the mime type
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Sets the mime type.
	 *
	 * @param mimeType the mime type
	 */
	public void setMimeType(final String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Returns the hash.
	 *
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * Sets the hash.
	 *
	 * @param hash the hash
	 */
	public void setHash(final String hash) {
		this.hash = hash;
	}

	/**
	 * Returns the length.
	 *
	 * @return the length
	 */
	public Long getLength() {
		return length;
	}

	/**
	 * Sets the length.
	 *
	 * @param length the length
	 */
	public void setLength(final Long length) {
		this.length = length;
	}

	/**
	 * Indicates whether or not base 64 encoded is the case.
	 *
	 * @return <code>true</code> if so, <code>false</code> otherwise
	 */
	public Boolean getBase64Encoded() {
		return base64Encoded;
	}

	/**
	 * Sets the base 64 encoded.
	 *
	 * @param base64Encoded the base 64 encoded
	 */
	public void setBase64Encoded(final Boolean base64Encoded) {
		this.base64Encoded = base64Encoded;
	}

	/* see superclass */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base64Encoded == null) ? 0 : base64Encoded.hashCode());
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result + ((length == null) ? 0 : length.hashCode());
		result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		final FileInfo other = (FileInfo) obj;
		if (base64Encoded == null) {
			if (other.base64Encoded != null) {
				return false;
			}
		} else if (!base64Encoded.equals(other.base64Encoded)) {
			return false;
		}
		if (hash == null) {
			if (other.hash != null) {
				return false;
			}
		} else if (!hash.equals(other.hash)) {
			return false;
		}
		if (length == null) {
			if (other.length != null) {
				return false;
			}
		} else if (!length.equals(other.length)) {
			return false;
		}
		if (mimeType == null) {
			if (other.mimeType != null) {
				return false;
			}
		} else if (!mimeType.equals(other.mimeType)) {
			return false;
		}
		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
			return false;
		}
		return true;
	}

}
