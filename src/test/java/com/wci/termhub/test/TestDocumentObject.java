package com.wci.termhub.test;

import java.util.Objects;

import org.springframework.data.elasticsearch.annotations.Document;

import com.wci.termhub.model.BaseModel;

@Document(indexName = "test")
public class TestDocumentObject extends BaseModel {

	/** The name. */
	private String name;

	/** The description. */
	private String description;

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(description, name);
	}

	/**
	 * Equals.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestDocumentObject other = (TestDocumentObject) obj;
		return Objects.equals(description, other.description) && Objects.equals(name, other.name);
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return "TestDocumentObject [name=" + name + ", description=" + description + "]";
	}

}
