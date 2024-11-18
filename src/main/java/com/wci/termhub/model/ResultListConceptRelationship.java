package com.wci.termhub.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a list of concept relationships.
 */
@Schema(description = "Represents a list of concept relationships returned from a find call")
public class ResultListConceptRelationship extends ResultList<ConceptRelationship> {

	/**
	 * Instantiates an empty {@link ResultListConceptRelationship}.
	 */
	public ResultListConceptRelationship() {
		// n/a
	}

	/**
	 * Instantiates a {@link ResultListConceptRelationship} from the specified
	 * parameters.
	 *
	 * @param list the list
	 */
	public ResultListConceptRelationship(final ResultList<ConceptRelationship> list) {
		this.setItems(list.getItems());
		this.setParameters(list.getParameters());
		this.setTotal(list.getTotal());
	}
}
