package com.wci.termhub.test;

import java.util.Objects;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.wci.termhub.model.BaseModel;

/**
 * The Class TestDocumentObject.
 */
@Document(indexName = "test")
public class TestDocumentObject extends BaseModel {

	/** The id. */
	@Field(type = FieldType.Keyword)
	private String id;

	/** The code. */
	@Field(type = FieldType.Keyword)
	private String code;

	/** The name. */
	@Field(type = FieldType.Keyword)
	private String name;

	/** The description. */
	@Field(type = FieldType.Keyword)
	private String description;

	/**
	 * Instantiates a new test document object.
	 */
	public TestDocumentObject() {
		super();
	}

	/**
	 * Instantiates a new test document object.
	 *
	 * @param id          the id
	 * @param name        the name
	 * @param description the description
	 */
	public TestDocumentObject(final String id, final String code, final String name, final String description) {
		super();
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code.
	 *
	 * @param id the new code
	 */
	public void setCode(final String code) {
		this.code = code;
	}

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
	public void setName(final String name) {
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
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(code, description, id, name);
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
		return Objects.equals(description, other.description) && Objects.equals(id, other.id)
				&& Objects.equals(code, other.code) && Objects.equals(name, other.name);
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return "TestDocumentObject [id=" + id + ", code=" + code + ", name=" + name + ", description=" + description
				+ "]";
	}

}
