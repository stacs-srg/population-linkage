package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus.TRUE_MATCH;

public class DeathDeathSiblingLinkageRecipe extends LinkageRecipe {

    private final Iterable<LXP> death_records;

    public DeathDeathSiblingLinkageRecipe(String results_repository_name, String links_persistent_name, String ground_truth_persistent_name, String source_repository_name, RecordRepository record_repository) {

        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
        death_records = Utilities.getDeathRecords(record_repository);
    }

    @Override
    public Iterable<LXP> getSourceRecords1() {
        return death_records;
    }

    @Override
    public Iterable<LXP> getSourceRecords2() {
        return death_records;
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return DeathDeathSiblingLinkageRecipe.trueMatch(record1, record2);
    }

    @Override
    public String getDatasetName() {
        return "Rubbish this is";
    } // TODO delete or clean this up

    @Override
    public String getLinkageType() {
        return "sibling bundling between deceased on death records";
    }

    @Override
    public String getSourceType1() {
        return "deaths";
    }

    @Override
    public String getSourceType2() {
        return "deaths";
    }

    @Override
    public String getRole1() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public String getRole2() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public List<Integer> getLinkageFields1() {
        return Constants.SIBLING_BUNDLING_DEATH_LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getLinkageFields2() {
        return Constants.SIBLING_BUNDLING_DEATH_LINKAGE_FIELDS;
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {

        final Map<String, Link> links = new HashMap<>();

        final List<LXP> records = new ArrayList<>();

        for (LXP lxp : record_repository.getDeaths()) {
            records.add(lxp);
        }

        final int number_of_records = records.size();

        for (int i = 0; i < number_of_records; i++) {

            for (int j = i + 1; j < number_of_records; j++) {

                LXP record1 = records.get(i);
                LXP record2 = records.get(j);


                try {
                    if (isTrueMatch(record1, record2).equals(TRUE_MATCH)) {

                        Link l = new Link(record1, Death.ROLE_DECEASED, record2, Death.ROLE_DECEASED, 1.0f, "ground truth");
                        String linkKey = toKey(record1, record2);
                        links.put(linkKey.toString(), l);

                    }
                } catch (PersistentObjectException e) {
                    ErrorHandling.error("PersistentObjectException adding getGroundTruthLinks");
                }
            }
        }

        return links;
    }

    /*
     * This is an efficient way of counting the number of true links without having an n^2 comparison space.
     * The number of true links when performing sibling bundling is equal to the (sum of (each person's family size - 1)) divided by 2
     * If we consider each person in turn and find how many family members they have this is the number of true links going out from them.
     * If we do this for each person then we get a total. However, at this point we will have counted each link in both directions - therefore we need to divide by two.
     *
     * The first loop gets the size of family for each ID.
     *
     * The second loop goes through each person, looks up the family size and adds it to the total -
     * we take away one as we don't want to count every link as the currently considered person is in the family count and so it's not really a link.
     *
     * Finally, we divide by two on the return.
     *
     * We could actually make this more efficient by replacing the second loop with a loop that iterates over the map values
     * and calculating (where s is the family size): the sum of (s * (s-1)) - I think this would work, but test the outcome if you implement it.
     *
     * This optimisation can only be used for sibling bundling and not for identity linkage.
     *
     * To calculate it for others - e.g. BrideBrideSibling, you'd take a similar approach using a map with the key
     * being the unique identifier for the family group. Marriage and death records don't have a FAMILY id in the
     * same way as births - we could add them into the synthetic data but that would lead to divergence from the Umea data.
     *
     * A way that works with Umea in mind would be to make a key from the information that uniquely identifies the family grouping
     * I'd go with concatenated parent IDs with a dash in the middle (as concatenated parents names can't be guaranteed to be
     * unique to the family). So repeating the process with none birth records it is just a matter of choosing a suitable key for the map.
     *
     * Also - the compute if absent line returns the value if the key is already present and the newly created value if not.
     * The use of atomic integer is to enable incrementation of a value in the map in a single line.
     */
    public int numberOfGroundTruthTrueLinks() { // See comment above

        int count = 0;

        Map<String, AtomicInteger> deathRecords = new HashMap<>();
        for(LXP birth : record_repository.getBirths()) {

            String fID = toKey( birth );

            if(!fID.equals("")) {
                deathRecords.computeIfAbsent(fID, k -> new AtomicInteger()).incrementAndGet();
            }
        }

        for(LXP birth : record_repository.getBirths()) {

            String fID = toKey( birth );

            if(!fID.equals(""))
                count += deathRecords.get(fID).get() - 1; // minus one as not a link to link to self - we're linking a dataset to itself!

        }

        return count / 2; // divide by 2 - symmetric linkage - making the same link in both directions only counts as one link!

    }

    private String toKey(LXP birth) {
        String s1 = birth.getString(Birth.FATHER_IDENTITY);
        String s2 = birth.getString(Birth.MOTHER_IDENTITY);

        if( s1.equals("") || s2.equals("") ) {
            return "";
        }  else {
            return s1 + "-" + s2;
        }
    }

    private Collection<LXP> filteredDeathRecords = null;

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {

        if(filteredDeathRecords == null) {

            filteredDeathRecords = new HashSet<>();

            for(LXP record : death_records) {

                String fathersForename = record.getString(Death.FATHER_FORENAME).trim();
                String fathersSurname = record.getString(Death.FATHER_SURNAME).trim();
                String mothersForename = record.getString(Death.MOTHER_FORENAME).trim();
                String mothersSurname = record.getString(Death.MOTHER_MAIDEN_SURNAME).trim();

                int populatedFields = 0;

                if(!(fathersForename.equals("") || fathersForename.equals("missing") ) ) {
                    populatedFields++;
                }
                if(!(fathersSurname.equals("") || fathersSurname.equals("missing") ) ) {
                    populatedFields++;
                }
                if(!( mothersForename.equals("") || mothersForename.equals("missing") ) ) {
                    populatedFields++;
                }
                if(!( mothersSurname.equals("") || mothersSurname.equals("missing") ) ) {
                    populatedFields++;
                }
                if( populatedFields >= requiredNumberOfPreFilterFields() ) {
                        filteredDeathRecords.add(record);
                } // else reject record for linkage - not enough info
            }
        }
        return filteredDeathRecords;
    }

    private int requiredNumberOfPreFilterFields() {
        return 3;
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {
        return getPreFilteredSourceRecords1();
    }

    private String toKey(LXP record1, LXP record2) {
        String s1 = record1.getString(Death.ORIGINAL_ID);
        String s2 = record2.getString(Death.ORIGINAL_ID);

        if(s1.compareTo(s2) < 0)
            return s1 + "-" + s2;
        else
            return s2 + "-" + s1;

    }


    public static void showLXP(LXP lxp) {
        System.out.println(lxp.getString(Death.FORENAME) + " " + lxp.getString(Death.SURNAME) + " // "
                + lxp.getString(Death.FATHER_FORENAME) + " " + lxp.getString(Death.FATHER_SURNAME) );
    }

    public static LinkStatus trueMatch(LXP record1, LXP record2) {

        final String b1_mother_id = record1.getString(Death.MOTHER_IDENTITY);
        final String b2_mother_id = record2.getString(Death.MOTHER_IDENTITY);

        final String b1_father_id = record1.getString(Death.FATHER_IDENTITY);
        final String b2_father_id = record2.getString(Death.FATHER_IDENTITY);

        if (!b1_mother_id.isEmpty() && b1_mother_id.equals(b2_mother_id) && !b1_father_id.isEmpty() && b1_father_id.equals(b2_father_id)) return LinkStatus.TRUE_MATCH;

        if ( b1_mother_id.isEmpty() && b2_mother_id.isEmpty() &&
                b1_father_id.isEmpty() && b2_father_id.isEmpty() ) return LinkStatus.UNKNOWN;

        return LinkStatus.NOT_TRUE_MATCH;
    }
}
