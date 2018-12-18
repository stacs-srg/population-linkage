package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.model.Link;
import uk.ac.standrews.cs.population_linkage.model.Links;
import uk.ac.standrews.cs.population_linkage.model.Role;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SiblingBundling extends Experiment {

    protected Links getGroundTruthLinks(RecordRepository record_repository) {

        Links links = new Links();

        List<Birth> records = new ArrayList<>();

        for (Birth birth : record_repository.getBirths()) {
            records.add(birth);
        }

        int number_of_records = records.size();

        // TODO this needs to be tailored to the particular linkage.
        List<Integer> match_fields = Collections.singletonList(Birth.FAMILY);

        for (int i = 0; i < number_of_records; i++) {
            for (int j = i + 1; j < number_of_records; j++) {

                Birth record1 = records.get(i);
                Birth record2 = records.get(j);

                if (areSiblings(match_fields, record1, record2)) {

                    Role role1 = new Role(record1.getString(Birth.STANDARDISED_ID), Birth.ROLE_BABY);
                    Role role2 = new Role(record2.getString(Birth.STANDARDISED_ID), Birth.ROLE_BABY);
                    links.add(new Link(role1, role2, 1.0f, "ground truth"));
                }
            }
        }

        return links;
    }

    private static boolean areSiblings(List<Integer> match_fields, LXP record1, LXP record2) {

        if (record1 == record2) return false;

        for (int field : match_fields) {
            if (!record1.getString(field).equals(record2.getString(field))) return false;
        }

        return true;
    }
}