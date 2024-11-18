package com.wci.termhub.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a list of terminologies.
 */
@Schema(description = "Represents a list of terminologies returned from a find call")
public class ResultListTerminology extends ResultList<Terminology> {

	/**
	 * Instantiates an empty {@link ResultListTerminology}.
	 */
	public ResultListTerminology() {
		// n/a
	}

	/**
	 * Instantiates a {@link ResultListTerminology} from the specified parameters.
	 *
	 * @param list the list
	 */
	public ResultListTerminology(final ResultList<Terminology> list) {
		this.setItems(list.getItems());
		this.setParameters(list.getParameters());
		this.setTotal(list.getTotal());
	}
}