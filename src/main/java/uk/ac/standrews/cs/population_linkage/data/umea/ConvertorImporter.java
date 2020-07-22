/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.umea;

import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConvertorImporter {

    private List<Birth> births;

    public ConvertorImporter() {
        this.births = new ArrayList<>();
    }


    public Iterable<Birth> getBirths() {
        return this.births;
    }

    public void addBirth(Birth birth) {
        this.births.add(birth);
    }

    public void importBirthRecords(DataSet birth_records)  {
        Iterator var2 = Birth.convertToRecords(birth_records).iterator();

        while(var2.hasNext()) {
            Birth birth = (Birth)var2.next();
            this.addBirth(birth);
        }

    }
}
