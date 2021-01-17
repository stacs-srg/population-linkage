/*
 * ************************************************************************
 *
 * Copyright 2021 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 * ************************************************************************
 */
package uk.ac.standrews.cs.population_linkage.helpers;

import java.util.List;
import org.junit.Test;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.Job;

import static org.assertj.core.api.Assertions.assertThat;

public class JobTest {

    private final Job job = makeJob();

    @Test
    public void getString() {
        String value = job.get("string");
        assertThat(value).isEqualTo("hello");
    }

    @Test
    public void getInt() {
        Integer value = job.get("integer", Integer.class);
        assertThat(value).isEqualTo(2);
    }

    @Test
    public void getBoolean() {
        Boolean value = job.get("boolean", Boolean.class);
        assertThat(value).isEqualTo(true);
    }

    @Test
    public void getBooleanCaps() {
        Boolean value = job.get("BOOLEAN", Boolean.class);
        assertThat(value).isEqualTo(false);
    }

    @Test
    public void getDouble() {
        Double value = job.get("double", Double.class);
        assertThat(value).isEqualTo(2.9);
    }

    @Test
    public void getLong() {
        Long value = job.get("long", Long.class);
        assertThat(value).isEqualTo(12345L);
    }

    private Job makeJob() {
        return new Job(
                List.of("string", "double", "integer", "boolean", "BOOLEAN", "long"),
                List.of("hello", "2.9", "2", "true", "FALSE", "12345"));
    }

}
