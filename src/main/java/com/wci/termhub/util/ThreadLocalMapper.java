/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.util;

import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Supply object mapper per thread.
 */
public final class ThreadLocalMapper {

    /**
     * Instantiates a new thread local mapper.
     */
    private ThreadLocalMapper() {
        // n/a
    }

    /** The Constant mapper. */
    private static final ThreadLocal<ObjectMapper> MAPPER =
            ThreadLocal.withInitial(ThreadLocalMapper::newMapper);

    /**
     * New mapper.
     *
     * @return the object mapper
     */
    public static ObjectMapper newMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        mapper.findAndRegisterModules().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                false);
        mapper.setTimeZone(TimeZone.getDefault());
        return mapper;
    }

    /**
     * Gets the.
     *
     * @return the object mapper
     */
    public static ObjectMapper get() {
        return MAPPER.get();
    }
}
