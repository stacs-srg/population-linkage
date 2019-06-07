package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.groundTruth.LinkStatus;
import uk.ac.standrews.cs.population_linkage.model.Link;
import uk.ac.standrews.cs.population_linkage.model.Role;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SiblingBundling extends Experiment {

    SiblingBundling(Path store_path, String repo_name) {

        super(store_path, repo_name);
    }

    @Override
    protected List<Integer> getMatchFields() {

        return Utilities.SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS;
    }

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

                try {
                    if (areGroundTruthSiblings(record1, record2)) {
                        links.add(new Link(makeRole(record1), makeRole(record2), 1.0f, "ground truth"));
                    }
                } catch (PersistentObjectException e) {
                    ErrorHandling.error( "PersistentObjectException adding getGroundTruthLinks" );
                }
            }
        }

        return links;
    }

    private Role makeRole(final Birth record) throws PersistentObjectException {

        return new Role(record.getThisRef(), Birth.ROLE_BABY);
    }

    private boolean areGroundTruthSiblings(LXP record1, LXP record2) {

        return Utilities.isTrueMatchBirthSiblingUmea(record1, record2) == LinkStatus.TRUE_MATCH;
    }
}
