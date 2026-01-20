/*
 * Copyright 2026 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.wci.termhub.util.StringUtility;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a name of a concept with associated information. Also called "atoms", "descriptions",
 * "names", "synonyms", "labels" and a variety of other words depending on the input terminology.
 */
@Schema(description = "Represents a name of a concept in a terminology with associated information")
@Document(indexName = "term-v1")
@Setting(settingPath = "es-config/autocomplete-analyzer.json")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Term extends AbstractHasModified
    implements TerminologyComponent, HasName, HasAttributes, Copyable<Term>, Comparable<Term> {

  /** The name. */
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword),
      @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "autocomplete_index",
          searchAnalyzer = "autocomplete_search")
  })
  private String name;

  /** The norm name. */
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword)
  })
  private String normName;

  /** The stem name. */
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword)
  })
  private String stemName;

  /** The word ct. */
  @Field(type = FieldType.Integer)
  private Integer wordCt;

  /** The length. */
  @Field(type = FieldType.Integer)
  private Integer length;

  /** The terminology. */
  @Field(type = FieldType.Keyword)
  private String terminology;

  /** The version. */
  @Field(type = FieldType.Keyword)
  private String version;

  /** The publisher. */
  @Field(type = FieldType.Keyword)
  private String publisher;

  /** The component id for this term. */
  @Field(type = FieldType.Keyword)
  private String componentId;

  /** The code id. */
  @Field(type = FieldType.Keyword)
  private String code;

  /** The concept id. */
  @Field(type = FieldType.Keyword)
  private String conceptId;

  /** The descriptor id. */
  @Field(type = FieldType.Keyword)
  private String descriptorId;

  /** The locale. */
  @Field(type = FieldType.Object)
  private Map<String, Boolean> localeMap;

  /** The term type. */
  @Field(type = FieldType.Keyword)
  private String type;

  /** The attributes. */
  @Field(type = FieldType.Object)
  private Map<String, String> attributes;

  /**
   * Instantiates an empty {@link Term}.
   */
  public Term() {
    // n/a
  }

  /**
   * Instantiates a {@link Term} from the specified parameters.
   *
   * @param other the other
   */
  public Term(final Term other) {
    populateFrom(other);
  }

  /**
   * Populate from.
   *
   * @param other the other
   */
  @Override
  public void populateFrom(final Term other) {
    super.populateFrom(other);
    setName(other.getName());
    setNormName(other.getNormName());
    setStemName(other.getStemName());
    terminology = other.getTerminology();
    version = other.getVersion();
    publisher = other.getPublisher();
    componentId = other.getComponentId();
    code = other.getCode();
    conceptId = other.getConceptId();
    descriptorId = other.getDescriptorId();
    localeMap = new HashMap<>(other.getLocaleMap());
    type = other.getType();
    attributes = new HashMap<>(other.getAttributes());
  }

  /**
   * Patch from.
   *
   * @param other the other
   */
  /* see superclass */
  @Override
  public void patchFrom(final Term other) {
    super.patchFrom(other);
    if (other.getName() != null) {
      setName(other.getName());
    }
    if (other.getTerminology() != null) {
      terminology = other.getTerminology();
    }
    if (other.getVersion() != null) {
      version = other.getVersion();
    }
    if (other.getStemName() != null) {
      stemName = other.getStemName();
    }
    if (other.getNormName() != null) {
      normName = other.getNormName();
    }
    if (other.getPublisher() != null) {
      publisher = other.getPublisher();
    }
    if (other.getComponentId() != null) {
      componentId = other.getComponentId();
    }
    if (other.getCode() != null) {
      code = other.getCode();
    }
    if (other.getConceptId() != null) {
      conceptId = other.getConceptId();
    }
    if (other.getDescriptorId() != null) {
      descriptorId = other.getDescriptorId();
    }

    if (!other.getLocaleMap().isEmpty()) {
      localeMap.putAll(other.getLocaleMap());
    }
    if (other.getType() != null) {
      type = other.getType();
    }
    if (!other.getAttributes().isEmpty()) {
      attributes.putAll(other.getAttributes());
    }
  }

  /**
   * Compare to.
   *
   * @param other the other
   * @return the int
   */
  @Override
  public int compareTo(final Term other) {
    return getName().compareTo(other.getName());
  }

  /* see superclass */
  @Override
  @Schema(description = "Name associated with this term")
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(final String name) {
    this.name = name;

    // also compute "normName" and "wordCt"
    if (name == null) {
      wordCt = 0;
      length = 0;
      normName = null;
      stemName = null;
    } else {
      wordCt = StringUtility.wordind(name).size();
      length = name.length();
      normName = StringUtility.normalize(name);
      stemName = StringUtility.normalizeWithStemming(name);
    }
  }

  /* see superclass */
  @Override
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  /* see superclass */
  @Override
  public void setAttributes(final Map<String, String> attributes) {
    this.attributes = attributes;
  }

  /* see superclass */
  @Override
  public String getTerminology() {
    return terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(final String terminology) {
    this.terminology = terminology;
  }

  /* see superclass */
  @Override
  public String getVersion() {
    return version;
  }

  /* see superclass */
  @Override
  public void setVersion(final String version) {
    this.version = version;
  }

  /* see superclass */
  @Override
  public String getPublisher() {
    return publisher;
  }

  /* see superclass */
  @Override
  public void setPublisher(final String publisher) {
    this.publisher = publisher;
  }

  /**
   * Returns the component id.
   *
   * @return the component id
   */
  @Schema(description = "Identifier for this object in the published source terminology")
  public String getComponentId() {
    return componentId;
  }

  /**
   * Sets the component id.
   *
   * @param componentId the component id
   */
  public void setComponentId(final String componentId) {
    this.componentId = componentId;
  }

  /**
   * Returns the code.
   *
   * @return the code
   */
  @Schema(description = "Code of the concept containing this term")
  public String getCode() {
    return code;
  }

  /**
   * Sets the code.
   *
   * @param code the code
   */
  public void setCode(final String code) {
    this.code = code;
  }

  /**
   * Returns the concept id.
   *
   * @return the concept id
   */
  @Schema(description = "Concept id of the concept containing this term "
      + "(typically this is the same as the code, but may be different "
      + "for some terminologies)")
  public String getConceptId() {
    return conceptId;
  }

  /**
   * Sets the concept id.
   *
   * @param conceptId the concept id
   */
  public void setConceptId(final String conceptId) {
    this.conceptId = conceptId;
  }

  /**
   * Returns the descriptor id.
   *
   * @return the descriptor id
   */
  @Schema(description = "Descriptor id of the concept containing this term "
      + "(only relevant for termionlogies that define descriptor codes)")
  public String getDescriptorId() {
    return descriptorId;
  }

  /**
   * Sets the descriptor id.
   *
   * @param descriptorId the descriptor id
   */
  public void setDescriptorId(final String descriptorId) {
    this.descriptorId = descriptorId;
  }

  /**
   * Compute single language.
   *
   * @return the string
   */
  public String computeSingleLanguage() {
    if (localeMap.size() == 0) {
      return null;
    }
    // Return longest "preferred" language
    final String prefLang = localeMap.keySet().stream().filter(l -> localeMap.get(l))
        .sorted((a, b) -> b.length() - a.length()).findFirst().orElse(null);
    if (prefLang != null) {
      return prefLang;
    }
    // Return longest language
    return localeMap.keySet().stream().sorted((a, b) -> b.length() - a.length()).findFirst()
        .orElse(null);
  }

  /**
   * Returns the locale map.
   *
   * @return the locale map
   */
  @Schema(description = "Map of language (optionally with locale) to true/false indicating "
      + "whether this term is the preferred term in that language or not.  An entry"
      + "with true indicates that it is preferred in that language. An entry with "
      + "false indicates that it is valid for that language but not preferred.")
  public Map<String, Boolean> getLocaleMap() {
    if (localeMap == null) {
      this.localeMap = new HashMap<>();
    }
    return localeMap;
  }

  /**
   * Sets the locale map.
   *
   * @param localeMap the locale map
   */
  public void setLocaleMap(final Map<String, Boolean> localeMap) {
    this.localeMap = localeMap;
  }

  /**
   * Contains language.
   *
   * @param language the language
   * @return true, if successful
   */
  public boolean containsLanguage(final String language) {
    return localeMap.entrySet().stream().anyMatch(e -> e.getKey().startsWith(language));
  }

  /**
   * Returns the term type.
   *
   * @return the term type
   */
  @Schema(description = "Term type, e.g. \"PT\" or \"900000000000013009\"")
  public String getType() {
    return type;
  }

  /**
   * Sets the term type.
   *
   * @param type the type
   */
  public void setType(final String type) {
    this.type = type;
  }

  /**
   * Gets the norm name.
   *
   * @return the norm name
   */
  @Schema(hidden = true)
  public String getNormName() {
    return normName;
  }

  /**
   * Sets the norm name.
   *
   * @param normName the new norm name
   */
  public void setNormName(final String normName) {
    this.normName = normName;
  }

  /**
   * Gets the stem name.
   *
   * @return the stem name
   */
  @Schema(hidden = true)
  public String getStemName() {
    return stemName;
  }

  /**
   * Sets the stem name.
   *
   * @param stemName the new stem name
   */
  public void setStemName(final String stemName) {
    this.stemName = stemName;
  }

  /**
   * Clean for api.
   */
  public void cleanForApi() {
    normName = null;
    stemName = null;
  }
}
