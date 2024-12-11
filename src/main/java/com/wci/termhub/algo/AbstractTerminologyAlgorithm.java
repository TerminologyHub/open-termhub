/*
 * Copyright 2024 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.algo;

import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.model.TerminologyComponent;
import com.wci.termhub.util.ModelUtility;

/**
 * Abstract algorithm for loading a terminology.
 */
public abstract class AbstractTerminologyAlgorithm extends AbstractNoServiceAlgorithm
        implements TerminologyAlgorithm {

    /** The logger. */
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(AbstractTerminologyAlgorithm.class);

    /** The terminologies. */
    private String terminology;

    /** The version. */
    private String version;

    /** The publisher. */
    private String publisher;

    /**
     * Instantiates an empty {@link AbstractTerminologyAlgorithm}.
     *
     * @throws Exception the exception
     */
    public AbstractTerminologyAlgorithm() throws Exception {
        super();

    }

    /* see superclass */
    @Override
    public String getDescription() {
        return ModelUtility.getNameFromClass(getClass());
    }

    /* see superclass */
    @Override
    public void setProperties(final Properties p) throws Exception {
        // n/a
    }

    /**
     * Returns the terminology.
     *
     * @return the terminology
     */
    public String getTerminology() {
        return terminology;
    }

    /**
     * Sets the terminology.
     *
     * @param terminology the terminology
     */
    /* see superclass */
    @Override
    public void setTerminology(final String terminology) {
        this.terminology = terminology;
    }

    /**
     * Returns the version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version the version
     */
    /* see superclass */
    @Override
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * Returns the publisher.
     *
     * @return the publisher
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * Sets the publisher.
     *
     * @param publisher the publisher
     */
    /* see superclass */
    @Override
    public void setPublisher(final String publisher) {
        this.publisher = publisher;
    }

    /* see superclass */
    @Override
    public void reset() throws Exception {
        throw new UnsupportedOperationException();
    }

    /* see superclass */
    @Override
    public String getHandlerKey() {
        return null;
    }

    /**
     * Sets the common fields.
     *
     * @param component the common fields
     */
    public void setCommonFields(final TerminologyComponent component) {
        component.setId(UUID.randomUUID().toString());
        component.setCreated(new Date());
        component.setModified(new Date());
        component.setModifiedBy("loader");
        component.setActive(true);
        component.setPublisher(getPublisher());
        component.setTerminology(getTerminology());
        component.setVersion(getVersion());
    }

}
