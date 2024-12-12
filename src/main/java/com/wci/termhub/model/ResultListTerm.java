package com.wci.termhub.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a list of terms.
 */
@Schema(description = "Represents a list of terms returned from a find call")
public class ResultListTerm extends ResultList<Term> {

	/**
	 * Instantiates an empty {@link ResultListTerm}.
	 */
	public ResultListTerm() {
		// n/a
	}

	/**
	 * Instantiates a {@link ResultListTerm} from the specified parameters.
	 *
	 * @param list the list
	 */
	public ResultListTerm(final ResultList<Term> list) {
		this.setItems(list.getItems());
		this.setParameters(list.getParameters());
		this.setTotal(list.getTotal());
	}
}
