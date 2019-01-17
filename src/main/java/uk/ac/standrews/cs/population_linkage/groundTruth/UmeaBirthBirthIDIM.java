package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;
import uk.al_richard.metricbitblaster.production.DistanceExponent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UmeaBirthBirthIDIM {

    private final RecordRepository record_repository;

    private UmeaBirthBirthIDIM(Path store_path, String repo_name) {

        record_repository = new RecordRepository(store_path, repo_name);

    }

    public void run() throws Exception {

        final Iterable<LXP> birth_records = Utilities.getDeathRecords(record_repository);

        List<LXP> dat = new ArrayList<>();
        birth_records.forEach(dat::add);

        DistanceExponent<LXP> de = new DistanceExponent<>(getCompositeMetric10()::distance, dat );
        System.out.println( "Birth-Birth IDIM over BMF,DOM,POM: " + de.IDIM() );

        de = new DistanceExponent<>(getCompositeMetric6()::distance, dat );
        System.out.println( "Birth-Birth IDIM over BMF: " + de.IDIM() );

        de = new DistanceExponent<>(getCompositeMetric4()::distance, dat );
        System.out.println( "Birth-Birth IDIM over MF: " + de.IDIM() );

    }

    protected NamedMetric<String> getBaseMetric() {

        return Utilities.SED;
    }


    public List<Integer> get10MatchFields() {
        return Arrays.asList(
                Birth.FORENAME,
                Birth.SURNAME,
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME,
                Birth.PARENTS_YEAR_OF_MARRIAGE,
                Birth.PARENTS_MONTH_OF_MARRIAGE,
                Birth.PARENTS_DAY_OF_MARRIAGE,
                Birth.PARENTS_PLACE_OF_MARRIAGE
                );
    }

    protected NamedMetric<LXP> getCompositeMetric10() {

        return new Sigma(getBaseMetric(), get10MatchFields());
    }

    public List<Integer> get6MatchFields() {
        return Arrays.asList(
                Birth.FORENAME,
                Birth.SURNAME,
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME
        );
    }

    protected NamedMetric<LXP> getCompositeMetric6() {

        return new Sigma(getBaseMetric(), get6MatchFields());
    }

    public List<Integer> get4MatchFields() {
        return Arrays.asList(
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME
        );
    }


    protected NamedMetric<LXP> getCompositeMetric4() {

        return new Sigma(getBaseMetric(), get4MatchFields());
    }


    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repository_name = "umea";

        new UmeaBirthBirthIDIM(store_path, repository_name).run();
    }
}
