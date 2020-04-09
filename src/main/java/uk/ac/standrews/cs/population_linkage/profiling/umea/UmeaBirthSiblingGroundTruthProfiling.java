package uk.ac.standrews.cs.population_linkage.profiling.umea;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthBirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UmeaBirthSiblingGroundTruthProfiling {

    public static void main(String[] args) {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        final Iterable<LXP> records = Utilities.getBirthRecords(new RecordRepository(store_path, repo_name));

        List<LXP> record_list = new ArrayList<>(230000);
        for (LXP record : records) {
            record_list.add(record);
        }

        long match_count = 0;
        long non_match_count = 0;
        long unknown_count = 0;
        long total = 0;

        final int size = record_list.size();
        System.out.println("size: " + size);

        for (int i = 0; i < size; i++) {
            if (i%100 == 0) System.out.println(i);

            for (int j = i + 1; j < size; j++) {

                total++;

                final LinkStatus linkStatus = BirthBirthSiblingLinkageRecipe.trueMatch(record_list.get(i), record_list.get(j));

                switch (linkStatus) {
                    case TRUE_MATCH: match_count++; break;
                    case NOT_TRUE_MATCH: non_match_count++; break;
                    default: unknown_count++;
                }
            }
        }

        System.out.println("matches: " + match_count);
        System.out.println("non-matches: " + non_match_count);
        System.out.println("unknown: " + unknown_count);
        System.out.println("total: " + total);
    }
}
