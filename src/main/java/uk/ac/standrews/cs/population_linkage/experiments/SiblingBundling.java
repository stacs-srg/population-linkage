package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.model.Link;
import uk.ac.standrews.cs.population_linkage.model.Role;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SiblingBundling extends Experiment {

    protected Set<Link> getGroundTruthLinks(final RecordRepository record_repository) {

        final Set<Link> links = new HashSet<>();

        final List<Birth> records = new ArrayList<>();

        for (Birth birth : record_repository.getBirths()) {
            records.add(birth);
        }

        final int number_of_records = records.size();

        for (int i = 0; i < number_of_records; i++) {
            for (int j = i + 1; j < number_of_records; j++) {

                Birth record1 = records.get(i);
                Birth record2 = records.get(j);

                if (areGroundTruthSiblings(record1, record2)) {

                    links.add(new Link(makeRole(record1), makeRole(record2), 1.0f, "ground truth"));
                }
            }
        }

        return links;
    }

    private Role makeRole(final Birth record) {

        return new Role(record.getString(Birth.STANDARDISED_ID), Birth.ROLE_BABY);
    }

    private boolean areGroundTruthSiblings(LXP record1, LXP record2) {

        if (record1 == record2) return false;

        for (int field : getSiblingGroundTruthFields()) {
            if (!record1.getString(field).equals(record2.getString(field))) return false;
        }

        return true;
    }

    protected abstract List<Integer> getSiblingGroundTruthFields();
}
