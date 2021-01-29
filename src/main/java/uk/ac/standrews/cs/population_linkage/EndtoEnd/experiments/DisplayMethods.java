package uk.ac.standrews.cs.population_linkage.EndtoEnd.experiments;

import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

import java.util.Set;

public class DisplayMethods {

    public static int father_errors = 0;
    public static int mother_errors = 0;

    public static void showFamily(Set<LXP> births) throws BucketException {
        father_errors = 0; // horrid hack
        mother_errors = 0; // horrid hack
        System.out.println("MetaMarriage:");
        String family_father_id = "";
        String family_mother_id = "";

        for (LXP birth : births) {
            String this_father_id = birth.getString(Birth.FATHER_IDENTITY);
            String this_mother_id = birth.getString(Birth.MOTHER_IDENTITY);
            if (family_father_id.equals("")) {
                family_father_id = this_father_id;  // saves the first mother and father
                family_mother_id = this_mother_id;
                showLXPBirth(birth, true, true);
            } else {
                showLXPBirth(birth, family_father_id.equals(family_father_id), family_mother_id.equals(this_mother_id));
                if (!family_father_id.equals(family_father_id)) {
                    father_errors++;
                }
                if (!family_father_id.equals(family_father_id)) {
                    mother_errors++;
                }
            }
        }
        System.out.println("===");
    }

    public static void showMarriage(LXP marriage) throws BucketException {

        String gfirstname = marriage.getString(Marriage.GROOM_FORENAME);
        String gsurname = marriage.getString(Marriage.GROOM_SURNAME);
        String gid = marriage.getString(Marriage.GROOM_IDENTITY);
        String gbid = marriage.getString(Marriage.GROOM_BIRTH_RECORD_IDENTITY);

        String bfirstname = marriage.getString(Marriage.BRIDE_FORENAME);
        String bsurname = marriage.getString(Marriage.BRIDE_SURNAME);
        String bid = marriage.getString(Marriage.BRIDE_IDENTITY);

        long oid = marriage.getId();
        String std_id = marriage.getString( Marriage.STANDARDISED_ID );

        System.out.println(oid + "/" + std_id + ": G: " + gfirstname + "," + gsurname + " GID: " + gid + " B: " + bfirstname + "," + bsurname + " BID: " + bid );
    }

    public static void showLXPBirth(LXP birth, boolean father_matches, boolean mother_matches) throws BucketException {

        String firstname = birth.getString(Birth.FORENAME);
        String surname = birth.getString(Birth.SURNAME);
        String father_id = birth.getString(Birth.FATHER_IDENTITY);
        String mother_id = birth.getString(Birth.MOTHER_IDENTITY);

        String parental_match = "  " + (father_matches ? "YES" : "NO") + "/" + (mother_matches ? "YES" : "NO");
        long oid = birth.getId();
        String std_id = birth.getString(Birth.STANDARDISED_ID);
        System.out.println(oid + "/" + std_id + ": " + firstname + "," + surname + " F: " + father_id + " M: " + mother_id + "\t" + parental_match);
    }

    public static void showLXPBirth(LXP birth, boolean groom_matches_birth) {
        String firstname = birth.getString(Birth.FORENAME);
        String surname = birth.getString(Birth.SURNAME);
        String father_id = birth.getString(Birth.FATHER_IDENTITY);
        String mother_id = birth.getString(Birth.MOTHER_IDENTITY);

        String groom_match = "  " + (groom_matches_birth ? "YES" : "NO");
        long oid = birth.getId();
        String std_id = birth.getString(Birth.STANDARDISED_ID);
        System.out.println(oid + "/" + std_id + ": " + firstname + "," + surname + " F: " + father_id + " M: " + mother_id + "\t" + " groom_match: " + groom_match);
    }

    public static void showLXPBirth(LXP birth) {
        String firstname = birth.getString(Birth.FORENAME);
        String surname = birth.getString(Birth.SURNAME);
        String father_id = birth.getString(Birth.FATHER_IDENTITY);
        String mother_id = birth.getString(Birth.MOTHER_IDENTITY);

        long oid = birth.getId();
        String std_id = birth.getString(Birth.STANDARDISED_ID);
        System.out.println(oid + "/" + std_id + ": " + firstname + "," + surname + " F: " + father_id + " M: " + mother_id);

    }


}
