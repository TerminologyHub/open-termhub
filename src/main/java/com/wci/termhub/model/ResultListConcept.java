package com.wci.termhub.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a list of concepts.
 */
@Schema(description = "Represents a list of concepts returned from a find call")
public class ResultListConcept extends ResultList<Concept> {

	/**
	 * Instantiates an empty {@link ResultListConcept}.
	 */
	public ResultListConcept() {
		// n/a
	}

	/**
	 * Instantiates a {@link ResultListConcept} from the specified parameters.
	 *
	 * @param list the list
	 */
	public ResultListConcept(final ResultList<Concept> list) {
		this.setItems(list.getItems());
		this.setParameters(list.getParameters());
		this.setTotal(list.getTotal());
	}
}
