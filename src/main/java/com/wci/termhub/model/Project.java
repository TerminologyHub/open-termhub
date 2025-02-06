/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Represents a project used by a user or organization to define a terminology
 * set.
 */
@Entity
@Table(name = "project")
@Schema(description = "Represents a project used by a user or organization"
    + " to define a terminology set")
@Document(indexName = "project-v1")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project extends AbstractHasModified
    implements HasName, HasLazyInit, Copyable<Project> {

  /** The name. */

  /** The name. */
  @Transient
  @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
      @InnerField(suffix = "keyword", type = FieldType.Keyword)
  })
  private String name;

  /** The description. */
  @Transient
  @Field(type = FieldType.Text)
  private String description;

  /** The api key. */
  @Transient
  @Field(type = FieldType.Object, enabled = false)
  private String apiKey;

  /** The organization id. */
  @Column(length = 256, nullable = false)
  @Field(type = FieldType.Keyword)
  private String organizationId;

  /** The type. BROWSER, EDIT, SUBSET, MAPSET, etc. */
  @Transient
  @Field(type = FieldType.Keyword)
  private String type;

  /** The uri label. */
  @Column(length = 256, nullable = false)
  @Field(type = FieldType.Keyword)
  private String uriLabel;

  /** By default this is null, but "true" when it should be purged. */
  @Column(nullable = true)
  @Field(type = FieldType.Boolean)
  private Boolean softDeleted;

  /** The public flag. True means anyone has access to it. */
  @Transient
  @Field(type = FieldType.Boolean)
  private Boolean publicFlag;

  /** The terminologies. */
  @Transient
  @Field(type = FieldType.Object)
  private List<TerminologyRef> terminologies;

  /** The mapsets. */
  @Transient
  @Field(type = FieldType.Object)

  /** The mapsets. */
  private List<MapsetRef> mapsets;

  // /** The subsets. */
  // @Transient
  // @Field(type = FieldType.Object)
  // private List<TerminologyRef> subsets;

  /** The admins. */
  @Transient
  private Set<String> admins;

  /** The writers. */
  @Transient
  private Set<String> writers;

  /** The readers. */
  @Transient
  private Set<String> readers;

  // Feature set

  /** The browser enabled. */
  @Transient
  @Field(type = FieldType.Boolean)
  private Boolean browserEnabled;

  /** The api enabled. */
  @Transient
  @Field(type = FieldType.Boolean)
  private Boolean apiEnabled;

  /** The download enabled. */
  @Transient
  @Field(type = FieldType.Boolean)
  private Boolean downloadEnabled;

  /** The docker enabled. */
  @Transient
  @Field(type = FieldType.Boolean)
  private Boolean dockerEnabled;

  /**
   * Instantiates an empty {@link Project}.
   */
  public Project() {
    // n/a
  }

  /**
   * Instantiates a {@link Project} from the specified parameters.
   *
   * @param other the other
   */
  public Project(final Project other) {
    populateFrom(other);
  }

  /* see superclass */
  @Override
  public void populateFrom(final Project other) {
    super.populateFrom(other);
    name = other.getName();
    description = other.getDescription();
    apiKey = other.getApiKey();
    organizationId = other.getOrganizationId();
    type = other.getType();
    uriLabel = other.getUriLabel();
    publicFlag = other.getPublic();
    terminologies = new ArrayList<>(other.getTerminologies());
    mapsets = new ArrayList<>(other.getMapsets());
    admins = new HashSet<>(other.getAdmins());
    writers = new HashSet<>(other.getWriters());
    readers = new HashSet<>(other.getReaders());
    browserEnabled = other.getBrowserEnabled();
    apiEnabled = other.getApiEnabled();
    downloadEnabled = other.getDownloadEnabled();
    dockerEnabled = other.getDockerEnabled();
    // softDeleted = other.getSoftDeleted();
  }

  // /* see superclass */
  // @Override
  // public Boolean getSoftDeleted() {
  // return softDeleted;
  // }

  // /* see superclass */
  // @Override
  // public void setSoftDeleted(final Boolean softDeleted) {
  // this.softDeleted = softDeleted;
  // }

  /* see superclass */
  @Override
  public void patchFrom(final Project other) {
    super.patchFrom(other);
    if (other.getName() != null) {
      name = other.getName();
    }
    if (other.getDescription() != null) {
      description = other.getDescription();
    }
    if (other.getApiKey() != null) {
      apiKey = other.getApiKey();
    }
    if (other.getOrganizationId() != null) {
      organizationId = other.getOrganizationId();
    }
    if (other.getType() != null) {
      type = other.getType();
    }
    if (other.getUriLabel() != null) {
      uriLabel = other.getUriLabel();
    }
    // if (other.getSoftDeleted() != null) {
    // softDeleted = other.getSoftDeleted();
    // }
    if (other.getPublic() != null) {
      publicFlag = other.getPublic();
    }
    if (other.getBrowserEnabled() != null) {
      browserEnabled = other.getBrowserEnabled();
    }
    if (other.getApiEnabled() != null) {
      apiEnabled = other.getApiEnabled();
    }
    if (other.getDownloadEnabled() != null) {
      downloadEnabled = other.getDownloadEnabled();
    }
    if (other.getDockerEnabled() != null) {
      dockerEnabled = other.getDockerEnabled();
    }

    // Patch terminologies by adding them
    if (!other.getTerminologies().isEmpty()) {
      for (final TerminologyRef term : other.getTerminologies()) {
        if (!getTerminologies().contains(term)) {
          getTerminologies().add(term);
        }
      }
    }
    // Patch mapsets by adding them
    if (!other.getMapsets().isEmpty()) {
      for (final MapsetRef mapset : other.getMapsets()) {
        if (!getMapsets().contains(mapset)) {
          getMapsets().add(mapset);
        }
      }
    }
  }

  // /* see superclass */
  // @Override
  // @PostLoad
  // public void unmarshall() throws Exception {
  // final Project resource = ModelUtility.fromJson(getData(), this.getClass());
  // if (resource != null) {
  // populateFrom(resource);
  // }
  // }

  /* see superclass */
  @Override
  public void lazyInit() {
    // n/a
  }

  // /* see superclass */
  // @Override
  // public void validateAdd(final AuthContext context) throws Exception {
  //
  // if (getId() != null) {
  // throw new Exception("Unexpected non-null id");
  // }
  // if (getName() == null) {
  // throw new Exception("Project name is required");
  // }
  // if (getOrganizationId() == null) {
  // throw new Exception("organizationId is required");
  // }
  // if (getUriLabel() == null) {
  // throw new Exception("uriLabel is required");
  // }
  // if (!getUriLabel().matches("^[A-Za-z0-9_\\-]+$")) {
  // throw new Exception("uriLabel contains characters other than letters,
  // numbers, _, and -");
  // }
  // if (getTerminologies().size() == 0 && getMapsets().size() == 0) {
  // throw new Exception("At least one terminology or mapset must be selected");
  // }
  //
  // // adding an already deleted thing is not allowed
  // if (BooleanUtils.isTrue(getSoftDeleted())) {
  // throw new Exception("Adding an already soft deleted project is not
  // allowed");
  // }
  //
  // // Trim certain fields
  // if (getName() != null) {
  // setName(getName().trim());
  // }
  //
  // // Verify terminologies are set up properly
  // validateTerminologies();
  // validateMapsets();
  //
  // }

  // /* see superclass */
  // @Override
  // public void validateUpdate(final AuthContext context, final Project other)
  // throws Exception {
  // if (other.getId() != null && !other.getId().equals(getId())) {
  // throw new Exception("Mismatch between id parameter and id specified in
  // object");
  // }
  // if (getUriLabel() != null && !getUriLabel().matches("^[A-Za-z0-9_\\-]+$"))
  // {
  // throw new Exception("uriLabel contains characters other than letters,
  // numbers, _, and -");
  // }
  //
  // // Should not be able to delete something with an update
  // if (BooleanUtils.isTrue(getSoftDeleted())) {
  // throw new Exception("Setting soft deleted for project via update is not
  // allowed");
  // }
  //
  // // Trim certain fields
  // if (getName() != null) {
  // setName(getName().trim());
  // }
  // if (getUriLabel() != null) {
  // setUriLabel(getUriLabel().trim());
  // }
  //
  // // Verify terminologies are set up properly
  // validateTerminologies();
  // validateMapsets();
  //
  // }

  // /* see superclass */
  // @Override
  // public void validateDelete(final AuthContext context) throws Exception {
  //
  // // n/a
  // }

  /**
   * Validate terminologies.
   *
   * @throws Exception the exception
   */
  private void validateTerminologies() throws Exception {
    final Set<String> seenVersion = new HashSet<>();
    final Set<String> seenLatest = new HashSet<>();
    for (final TerminologyRef t : getTerminologies()) {
      final String key = t.getAbbreviation() + ", " + t.getPublisher();
      if (t.getLatest() != null && t.getLatest() && t.getVersion() != null) {
        throw new Exception(
            "Project unexpectedly has a terminology with both 'version' and 'latest' set = " + t);
      }

      if (t.getLatest() != null && t.getLatest() && seenVersion.contains(key)) {
        throw new Exception("Project must not have the same terminology selected as both 'latest' "
            + "and for a specific version = " + key);
      }
      if (t.getVersion() != null && seenLatest.contains(key)) {
        throw new Exception("Project must not have the same terminology selected as both 'latest' "
            + "and for a specific version = " + key);
      }

      // This works because we've verified that it can't be both latest and seen
      if (t.getVersion() != null) {
        seenVersion.add(key);
      } else {
        seenLatest.add(key);
      }

    }
  }

  /**
   * Validate mapsets.
   *
   * @throws Exception the exception
   */
  private void validateMapsets() throws Exception {
    final Set<String> seenVersion = new HashSet<>();
    final Set<String> seenLatest = new HashSet<>();
    for (final TerminologyRef mapset : getMapsets()) {
      final String key = mapset.getAbbreviation() + ", " + mapset.getPublisher();
      if (mapset.getLatest() != null && mapset.getLatest() && mapset.getVersion() != null) {
        throw new Exception(
            "Project unexpectedly has a terminology with both 'version' and 'latest' set = "
                + mapset);
      }

      if (mapset.getLatest() != null && mapset.getLatest() && seenVersion.contains(key)) {
        throw new Exception("Project must not have the same terminology selected as both 'latest' "
            + "and for a specific version = " + key);
      }
      if (mapset.getVersion() != null && seenLatest.contains(key)) {
        throw new Exception("Project must not have the same terminology selected as both 'latest' "
            + "and for a specific version = " + key);
      }

      // This works because we've verified that it can't be both latest and seen
      if (mapset.getVersion() != null) {
        seenVersion.add(key);
      } else {
        seenLatest.add(key);
      }

    }
  }

  /**
   * Returns the type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the type.
   *
   * @param type the type
   */
  public void setType(final String type) {
    this.type = type;
  }

  /**
   * Returns the uri label.
   *
   * @return the uri label
   */
  public String getUriLabel() {
    return uriLabel;
  }

  /**
   * Sets the uri label.
   *
   * @param uriLabel the uri label
   */
  public void setUriLabel(final String uriLabel) {
    this.uriLabel = uriLabel;
  }

  /**
   * Returns the organization id.
   *
   * @return the organization id
   */
  public String getOrganizationId() {
    return organizationId;
  }

  /**
   * Sets the organization id.
   *
   * @param organizationId the organization id
   */
  public void setOrganizationId(final String organizationId) {
    this.organizationId = organizationId;
  }

  /**
   * Returns the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description the description
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * Gets the api key.
   *
   * @return the api key
   */
  public String getApiKey() {
    return apiKey;
  }

  /**
   * Sets the api key.
   *
   * @param apiKey the new api key
   */
  public void setApiKey(final String apiKey) {
    this.apiKey = apiKey;
  }

  /* see superclass */
  @Override
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Returns the public flag.
   *
   * @return the public flag
   */
  public Boolean getPublic() {
    return publicFlag;
  }

  /**
   * Sets the public flag.
   *
   * @param publicFlag the public flag
   */
  public void setPublic(final Boolean publicFlag) {
    this.publicFlag = publicFlag;
  }

  /**
   * Returns the terminologies.
   *
   * @return the terminologies
   */
  public List<TerminologyRef> getTerminologies() {
    if (terminologies == null) {
      terminologies = new ArrayList<>();
    }
    return terminologies;
  }

  /**
   * Check terminology of the component is compatible with the project.
   *
   * @param comp the comp
   * @return true, if successful
   */
  public boolean checkTerminology(final TerminologyComponent comp) {
    return comp != null && getTerminologies().stream()
        .filter(t -> t.getAbbreviation().equals(comp.getTerminology())
            && t.getPublisher().equals(comp.getPublisher())
            && (t.getVersion() == null || t.getVersion().equals(comp.getVersion())))
        .count() > 0;
    // TODO: need to deal with version/latest
  }

  /**
   * Check terminology.
   *
   * @param terminology the terminology
   * @return true, if successful
   */
  public boolean checkTerminology(final Terminology terminology) {
    return terminology != null && getTerminologies().stream()
        .filter(t -> terminology.getAbbreviation().equals(t.getAbbreviation())
            && terminology.getPublisher().equals(t.getPublisher())
            && (terminology.getVersion().equals(t.getVersion()) || (t.getLatest() != null
                && t.getLatest() && terminology.getLatest() != null && terminology.getLatest())))
        .count() > 0;
  }

  /**
   * Sets the terminologies.
   *
   * @param terminologies the terminologies
   */
  public void setTerminologies(final List<TerminologyRef> terminologies) {
    this.terminologies = terminologies;
  }

  /**
   * Gets the mapsets.
   *
   * @return the mapsets
   */
  public List<MapsetRef> getMapsets() {
    if (mapsets == null) {
      mapsets = new ArrayList<>();
    }
    return mapsets;
  }

  /**
   * Sets the mapsets.
   *
   * @param mapsets the new mapsets
   */
  public void setMapsets(final List<MapsetRef> mapsets) {
    this.mapsets = mapsets;
  }

  /**
   * Returns the admins.
   *
   * @return the admins
   */
  public Set<String> getAdmins() {
    if (admins == null) {
      admins = new HashSet<>();
    }
    return admins;
  }

  /**
   * Sets the admins.
   *
   * @param admins the admins to set
   */
  public void setAdmins(final Set<String> admins) {
    this.admins = admins;
  }

  /**
   * Returns the writers.
   *
   * @return the writers
   */
  public Set<String> getWriters() {
    if (writers == null) {
      writers = new HashSet<>();
    }
    return writers;
  }

  /**
   * Sets the writers.
   *
   * @param writers the writers to set
   */
  public void setWriters(final Set<String> writers) {
    this.writers = writers;
  }

  /**
   * Returns the readers.
   *
   * @return the readers
   */
  public Set<String> getReaders() {
    if (readers == null) {
      readers = new HashSet<>();
    }
    return readers;
  }

  /**
   * Sets the readers.
   *
   * @param readers the readers to set
   */
  public void setReaders(final Set<String> readers) {
    this.readers = readers;
  }

  /**
   * Returns the browser enabled.
   *
   * @return the browser enabled
   */
  public Boolean getBrowserEnabled() {
    return browserEnabled;
  }

  /**
   * Sets the browser enabled.
   *
   * @param browserEnabled the browser enabled
   */
  public void setBrowserEnabled(final Boolean browserEnabled) {
    this.browserEnabled = browserEnabled;
  }

  /**
   * Returns the api enabled.
   *
   * @return the api enabled
   */
  public Boolean getApiEnabled() {
    return apiEnabled;
  }

  /**
   * Sets the api enabled.
   *
   * @param apiEnabled the api enabled
   */
  public void setApiEnabled(final Boolean apiEnabled) {
    this.apiEnabled = apiEnabled;
  }

  /**
   * Returns the download enabled.
   *
   * @return the download enabled
   */
  public Boolean getDownloadEnabled() {
    return downloadEnabled;
  }

  /**
   * Sets the download enabled.
   *
   * @param downloadEnabled the download enabled
   */
  public void setDownloadEnabled(final Boolean downloadEnabled) {
    this.downloadEnabled = downloadEnabled;
  }

  /**
   * Returns the docker enabled.
   *
   * @return the docker enabled
   */
  public Boolean getDockerEnabled() {
    return dockerEnabled;
  }

  /**
   * Sets the docker enabled.
   *
   * @param dockerEnabled the docker enabled
   */
  public void setDockerEnabled(final Boolean dockerEnabled) {
    this.dockerEnabled = dockerEnabled;
  }

}
