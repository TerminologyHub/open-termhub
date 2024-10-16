package com.wci.termhub.app;

public enum Direction {
	ASC("asc"), DESC("desc");

	private final String text;

	/**
	 * Instantiates a new direction.
	 *
	 * @param text the text
	 */
	Direction(final String text) {
		this.text = text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return text;
	}
}
