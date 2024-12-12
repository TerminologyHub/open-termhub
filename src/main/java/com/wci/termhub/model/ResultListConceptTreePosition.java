package com.wci.termhub.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a list of concept tree positions.
 */
@Schema(description = "Represents a list of concept tree positions returned from a find call")
public class ResultListConceptTreePosition extends ResultList<ConceptTreePosition> {

	/**
	 * Instantiates an empty {@link ResultListConceptTreePosition}.
	 */
	public ResultListConceptTreePosition() {
		// n/a
	}

	/**
	 * Instantiates a {@link ResultListConceptTreePosition} from the specified
	 * parameters.
	 *
	 * @param list the list
	 */
	public ResultListConceptTreePosition(final ResultList<ConceptTreePosition> list) {
		this.setItems(list.getItems());
		this.setParameters(list.getParameters());
		this.setTotal(list.getTotal());
	}
}