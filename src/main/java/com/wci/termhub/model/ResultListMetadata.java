package com.wci.termhub.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a list of metadata.
 */
@Schema(description = "Represents a list of metadata objects returned from a find call")
public class ResultListMetadata extends ResultList<Metadata> {

	/**
	 * Instantiates an empty {@link ResultListMetadata}.
	 */
	public ResultListMetadata() {
		// n/a
	}

	/**
	 * Instantiates a {@link ResultListMetadata} from the specified parameters.
	 *
	 * @param list the list
	 */
	public ResultListMetadata(final ResultList<Metadata> list) {
		this.setItems(list.getItems());
		this.setParameters(list.getParameters());
		this.setTotal(list.getTotal());
	}
}
