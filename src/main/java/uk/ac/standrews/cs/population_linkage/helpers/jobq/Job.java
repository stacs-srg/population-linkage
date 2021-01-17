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
package uk.ac.standrews.cs.population_linkage.helpers.jobq;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class Job {
    List<String> labels;
    List<String> values;

    public Job(List<String> labels, List<String> values) {
        this.labels = labels;
        this.values = values;
    }

    public String get(String label) {
        return values.get(labels.indexOf(label)).trim();
    }

    public <T> T get(String label, Class<T> clazz) {
        try {
            return clazz.getConstructor(String.class).newInstance(values.get(labels.indexOf(label)).trim());
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getNullable(String label, String nullString, Class<T> clazz) {
        if(get(label).equals(nullString)) {
            return null;
        } else {
            return get(label, clazz);
        }
    }

    @Override
    public String toString() {
        return "Job{" +
                "labels=" + labels +
                ", values=" + values +
                '}';
    }
}
