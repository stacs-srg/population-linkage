package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.*;
import uk.ac.standrews.cs.utilities.PercentageProgressIndicator;
import uk.ac.standrews.cs.utilities.ProgressIndicator;

import java.util.List;

public class BruteForceExactMatchSiblingBundler implements Linker {

    Matcher matcher;
    ProgressIndicator progress_indicator;

    public BruteForceExactMatchSiblingBundler() {

        matcher = new ExactMatchMatcher(
                BirthLinkageSubRecord.FATHERS_FORENAME, BirthLinkageSubRecord.FATHERS_SURNAME,
                BirthLinkageSubRecord.MOTHERS_FORENAME, BirthLinkageSubRecord.MOTHERS_MAIDEN_SURNAME,
                BirthLinkageSubRecord.PARENTS_PLACE_OF_MARRIAGE,
                BirthLinkageSubRecord.PARENTS_DAY_OF_MARRIAGE, BirthLinkageSubRecord.PARENTS_MONTH_OF_MARRIAGE, BirthLinkageSubRecord.PARENTS_YEAR_OF_MARRIAGE);

        progress_indicator = new PercentageProgressIndicator(10);
    }

    @Override
    public Links link(List<BirthLinkageSubRecord> birth_records) {

        Links links = new Links();

        int number_of_records = birth_records.size();

        int total_comparisons = number_of_records * (number_of_records - 1) / 2;
        progress_indicator.setTotalSteps(total_comparisons);

        for (int i = 0; i < number_of_records; i++) {
            for (int j = i + 1; j < number_of_records; j++) {

                BirthLinkageSubRecord record1 = birth_records.get(i);
                BirthLinkageSubRecord record2 = birth_records.get(j);

                if (matcher.match(record1, record2)) {
                    links.add(new Link(getIdentifier(record1), getIdentifier(record2)));
                }

                progress_indicator.progressStep();
            }
        }

        return links;
    }

    private String getIdentifier(BirthLinkageSubRecord record) {

        return record.getString(BirthLinkageSubRecord.STANDARDISED_ID);
    }
}
