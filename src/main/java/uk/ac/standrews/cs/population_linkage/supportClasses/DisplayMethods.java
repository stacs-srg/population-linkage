/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.util.List;
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
                showBirth(birth, true, true);
            } else {
                showBirth(birth, family_father_id.equals(family_father_id), family_mother_id.equals(this_mother_id));
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
        showMarriage(marriage,Detail.MINIMAL);
    }

    public static void showBirth(LXP birth, boolean father_matches, boolean mother_matches) throws BucketException {

        String firstname = birth.getString(Birth.FORENAME);
        String surname = birth.getString(Birth.SURNAME);
        String father_id = birth.getString(Birth.FATHER_IDENTITY);
        String mother_id = birth.getString(Birth.MOTHER_IDENTITY);

        String parental_match = "  " + (father_matches ? "YES" : "NO") + "/" + (mother_matches ? "YES" : "NO");
        String oid = birth.getId();
        String std_id = birth.getString(Birth.STANDARDISED_ID);
        System.out.println(oid + "/" + std_id + ": " + firstname + "," + surname + " F: " + father_id + " M: " + mother_id + "\t" + parental_match);
    }

    public static void showBirth(LXP birth, boolean groom_matches_birth) {
        String firstname = birth.getString(Birth.FORENAME);
        String surname = birth.getString(Birth.SURNAME);
        String father_id = birth.getString(Birth.FATHER_IDENTITY);
        String mother_id = birth.getString(Birth.MOTHER_IDENTITY);

        String groom_match = "  " + (groom_matches_birth ? "YES" : "NO");
        String oid = birth.getId();
        String std_id = birth.getString(Birth.STANDARDISED_ID);
        System.out.println(oid + "/" + std_id + ": " + firstname + "," + surname + " F: " + father_id + " M: " + mother_id + "\t" + " groom_match: " + groom_match);
    }

    public static void showBirth(LXP birth) {
        showBirth(birth,Detail.MINIMAL);
    }

    public static void showBirth(LXP birth, Detail detail) {
        StringBuilder sb = new StringBuilder();

        sb.append( birth.getId() + "/" );           // Standard identifiers - Storr Id/Standardised Id
        sb.append( birth.getString(Birth.STANDARDISED_ID) + ": " );

        sb.append( birth.getString(Birth.CHILD_IDENTITY) + "," );
        sb.append( birth.getString(Birth.FORENAME) + "," );
        sb.append( birth.getString(Birth.SURNAME) + "," );
        sb.append( birth.getString(Birth.BIRTH_DAY) + "/" );
        sb.append( birth.getString(Birth.BIRTH_MONTH) + "/" );
        sb.append( birth.getString(Birth.BIRTH_YEAR) + "," );
        sb.append( birth.getString(Birth.SEX) );


        if( detail == Detail.HIGH || detail == Detail.MEDIUM ) {
            sb.append( ", F:" );
            sb.append( birth.getString(Birth.FATHER_FORENAME) + "," );
            sb.append( birth.getString(Birth.FATHER_SURNAME) + "," );
            sb.append( birth.getString(Birth.FATHER_IDENTITY) + "," );
            sb.append( birth.getString(Birth.FATHER_OCCUPATION) + ", M:" );

            sb.append( birth.getString(Birth.MOTHER_FORENAME) + "," );
            sb.append( birth.getString(Birth.MOTHER_SURNAME) + "," );
            sb.append( birth.getString(Birth.MOTHER_MAIDEN_SURNAME) + "," );
            sb.append( birth.getString(Birth.MOTHER_IDENTITY) + "," );
            sb.append( birth.getString(Birth.MOTHER_OCCUPATION) + "," );

            sb.append( "pmar=" + birth.getString(Birth.PARENTS_DAY_OF_MARRIAGE) + "/" );
            sb.append( birth.getString(Birth.PARENTS_MONTH_OF_MARRIAGE) + "/" );
            sb.append( birth.getString(Birth.PARENTS_YEAR_OF_MARRIAGE) + "," );
            sb.append( birth.getString(Birth.PARENTS_PLACE_OF_MARRIAGE) );
        }

        if( detail == Detail.HIGH ) {
            sb.append( "," );
            sb.append( "d=" + birth.getString(Birth.DEATH) + "," );
            sb.append( "fid=" + birth.getString(Birth.FATHER_IDENTITY) + "," );
            sb.append( "fbrid=" +  birth.getString(Birth.FATHER_BIRTH_RECORD_IDENTITY) + "," );
            sb.append( "mid=" + birth.getString(Birth.MOTHER_IDENTITY) + "," );
            sb.append( "mbrid=" + birth.getString(Birth.MOTHER_BIRTH_RECORD_IDENTITY) + "," );
            sb.append( "drid=" + birth.getString(Birth.DEATH_RECORD_IDENTITY) + "," );
            sb.append( "pmid=" + birth.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY) + "," );
            sb.append( "mrid1=" + birth.getString(Birth.MARRIAGE_RECORD_IDENTITY1) + "," );
            sb.append( "mrid2=" + birth.getString(Birth.MARRIAGE_RECORD_IDENTITY2));
        }
        System.out.println( sb.toString() );
    }

    public static void showDeath(LXP death) {
        showDeath(death,Detail.MINIMAL);
    }

    public static void showDeath(LXP death, Detail detail) {
        StringBuilder sb = new StringBuilder();

        sb.append( death.getId() + "/" );           // Standard identifiers - Storr Id/Standardised Id
        sb.append( death.getString(Death.STANDARDISED_ID) + ": " );

        sb.append( death.getString(Death.DECEASED_IDENTITY) + "," );
        sb.append( death.getString(Death.FORENAME) + "," );
        sb.append( death.getString(Death.SURNAME) + "," );
        sb.append( death.getString(Death.DATE_OF_BIRTH)+ ",");
        sb.append( death.getString(Death.SEX) );


        if( detail == Detail.HIGH || detail == Detail.MEDIUM ) {
            sb.append( ", F:" );
            sb.append( death.getString(Death.FATHER_FORENAME) + "," );
            sb.append( death.getString(Death.FATHER_SURNAME) + "," );
            sb.append( death.getString(Death.FATHER_IDENTITY) + "," );
            sb.append( death.getString(Death.FATHER_OCCUPATION) + ", M:" );

            sb.append( death.getString(Death.MOTHER_FORENAME) + "," );
            sb.append( death.getString(Death.MOTHER_SURNAME) + "," );
            sb.append( death.getString(Death.MOTHER_MAIDEN_SURNAME) + "," );
            sb.append( death.getString(Death.MOTHER_IDENTITY) + "," );
            sb.append( death.getString(Death.MOTHER_OCCUPATION) + "," );
        }

        if( detail == Detail.HIGH ) {
            sb.append( "," );
            sb.append( "fid=" + death.getString(Death.FATHER_IDENTITY) + "," );
            sb.append( "fbrid=" +  death.getString(Death.FATHER_BIRTH_RECORD_IDENTITY) + "," );
            sb.append( "mid=" + death.getString(Death.MOTHER_IDENTITY) + "," );
            sb.append( "mbrid=" + death.getString(Death.MOTHER_BIRTH_RECORD_IDENTITY) + "," );
            sb.append( "drid=" + death.getString(Death.BIRTH_RECORD_IDENTITY) + "," );
            sb.append( "pmid=" + death.getString(Death.PARENT_MARRIAGE_RECORD_IDENTITY) + "," );
            sb.append( "mid=" + death.getString(Death.SPOUSE_MARRIAGE_RECORD_IDENTITY));
        }
        System.out.println( sb.toString() );
    }

    public static void showMarriage(LXP marriage, Detail detail) {
        StringBuilder sb = new StringBuilder();

        sb.append(marriage.getId() + "/");           // Standard identifiers - Storr Id/Standardised Id
        sb.append(marriage.getString(Marriage.STANDARDISED_ID) + ": ");

        sb.append(marriage.getString(Marriage.GROOM_FORENAME) + ",");
        sb.append(marriage.getString(Marriage.GROOM_SURNAME) + ",");
        sb.append(marriage.getString(Marriage.GROOM_IDENTITY) + ",");
        sb.append(marriage.getString(Marriage.GROOM_BIRTH_RECORD_IDENTITY) + ",");

        sb.append(marriage.getString(Marriage.BRIDE_FORENAME) + ",");
        sb.append(marriage.getString(Marriage.BRIDE_SURNAME) + ",");
        sb.append(marriage.getString(Marriage.BRIDE_IDENTITY) + ",");
        sb.append(marriage.getString(Marriage.BRIDE_BIRTH_RECORD_IDENTITY) + ",");
        
        sb.append(marriage.getString(Marriage.PLACE_OF_MARRIAGE) + ",");
        sb.append(marriage.getString(Marriage.MARRIAGE_DAY) + "/");
        sb.append(marriage.getString(Marriage.MARRIAGE_MONTH) + "/");
        sb.append(marriage.getString(Marriage.MARRIAGE_YEAR) + ",");
                
        if( detail == Detail.HIGH || detail == Detail.MEDIUM ) {
            
            sb.append( "GF:" + marriage.getString(Marriage.GROOM_FATHER_FORENAME) + ",");
            sb.append(marriage.getString(Marriage.GROOM_FATHER_SURNAME) + ",");
            sb.append( "GM:" + marriage.getString(Marriage.GROOM_MOTHER_FORENAME) + ",");
            sb.append(marriage.getString(Marriage.GROOM_MOTHER_MAIDEN_SURNAME) + ",");

            sb.append( "BF:" + marriage.getString(Marriage.BRIDE_FATHER_SURNAME) + ",");
            sb.append(marriage.getString(Marriage.BRIDE_FATHER_FORENAME) + ",");
            sb.append( "BM:" + marriage.getString(Marriage.BRIDE_MOTHER_FORENAME) + ",");
            sb.append(marriage.getString(Marriage.BRIDE_MOTHER_MAIDEN_SURNAME) );
        }

        if( detail == Detail.HIGH ) {
            sb.append("," );
            sb.append("dis:" + marriage.getString(Marriage.REGISTRATION_DISTRICT_NUMBER) + ",");
            sb.append("suf:" + marriage.getString(Marriage.REGISTRATION_DISTRICT_SUFFIX) + ",");
            sb.append("bdob:" + marriage.getString(Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH) + ",");
            sb.append("gdob:" + marriage.getString(Marriage.GROOM_AGE_OR_DATE_OF_BIRTH) + ",");

            sb.append(marriage.getString(Marriage.GROOM_BIRTH_RECORD_IDENTITY) + ",");
            sb.append("gbid:" + marriage.getString(Marriage.BRIDE_BIRTH_RECORD_IDENTITY) + ",");

            sb.append("gfid:" + marriage.getString(Marriage.GROOM_FATHER_IDENTITY) + ",");
            sb.append("gfbrid:" + marriage.getString(Marriage.GROOM_FATHER_BIRTH_RECORD_IDENTITY) + ",");
            sb.append("gmid:" + marriage.getString(Marriage.GROOM_MOTHER_IDENTITY) + ",");
            sb.append("gmbrid:" + marriage.getString(Marriage.GROOM_MOTHER_BIRTH_RECORD_IDENTITY) + ",");
            
            sb.append("bfid:" + marriage.getString(Marriage.BRIDE_FATHER_IDENTITY) + ",");
            sb.append("bfbrid:" + marriage.getString(Marriage.BRIDE_FATHER_BIRTH_RECORD_IDENTITY) + ",");
            sb.append("bmid:" + marriage.getString(Marriage.BRIDE_MOTHER_IDENTITY) + ",");
            sb.append("bmbrid:" + marriage.getString(Marriage.BRIDE_MOTHER_BIRTH_RECORD_IDENTITY));
        }

        System.out.println( sb.toString() );
    }

    public static void showMatchFields(LXP rec1, LXP rec2, List<Integer> fields) {
        StringBuilder sb = new StringBuilder();
        for( int index : fields ) {
            sb.append( rec1.getString(index) + "==" + rec2.getString(index) + "\n" );
        }
        System.out.println(sb.toString());
    }
    
    public enum Detail {
        HIGH,
        MEDIUM,
        MINIMAL
    }

}
