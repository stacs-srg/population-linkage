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
package uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthGroomOwnMarriage;

import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthBrideSiblingBundleBuilder;
import uk.ac.standrews.cs.population_linkage.graph.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.linkageRunners.MakePersistent;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.util.Arrays;
import java.util.List;

/**
 *  This class attempts to find birth-groom links: links a baby on a birth to the same person as a groom on a marriage.
 *  This is NOT STRONG: uses the 3 names: the groom/baby and the names of the mother and father.
 */
public class BirthGroomOwnMarriageBuilder implements MakePersistent {

    // expected_matches = 32787
    private static final double m_prior = 4.111135079392392E-5;
    private static final double u_prior = 1d - m_prior;
    public static final double odds_prior = m_prior/u_prior;

    // u baby_groom_first match = 79
// u baby_groom_first unmatched = 25186
// u Total = 25265
    private static final double u_prior_baby_groom_first = 0.0031268552;
    // u baby_groom_second match = 123
// u baby_groom_second unmatched = 25142
// u Total = 25265
    private static final double u_prior_baby_groom_second = 0.004868395;
    // m baby_groom_first match = 20755
// m baby_groom_first unmatched = 12032
// m Total = 32787
    private static final double m_prior_baby_groom_first = 0.6330253;
    // m baby_groom_second match = 22482
// m baby_groom_second unmatched = 10305
// m Total = 32787
    private static final double m_prior_baby_groom_second = 0.6856986;
    // u baby_groom_father_first match = 943
// u baby_groom_father_first unmatched = 24675
// u Total = 25618
    private static final double u_prior_baby_groom_father_first = 0.036810055;
    // u baby_groom_father_surname match = 808
// u baby_groom_father_surname unmatched = 24810
// u Total = 25618
    private static final double u_prior_baby_groom_father_surname = 0.031540323;
    // m baby_groom_father_first match = 18695
// m baby_groom_father_first unmatched = 14092
// m Total = 32787
    private static final double m_prior_baby_groom_father_first = 0.5701955;
    // m baby_groom_father_surname match = 20437
// m baby_groom_father_surname unmatched = 12350
// m Total = 32787
    private static final double m_prior_baby_groom_father_surname = 0.6233263;
    // u baby_groom_mother_first match = 220
// u baby_groom_mother_first unmatched = 25266
// u Total = 25486
    private static final double u_prior_baby_groom_mother_first = 0.0086321905;
    // u baby_groom_mother_surname match = 366
// u baby_groom_mother_surname unmatched = 25120
// u Total = 25486
    private static final double u_prior_baby_groom_mother_surname = 0.0143608255;
    // m baby_groom_mother_first match = 14058
// m baby_groom_mother_first unmatched = 18729
// m Total = 32787
    private static final double m_prior_baby_groom_mother_first = 0.4287675;
    // m baby_groom_mother_surname match = 7927
// m baby_groom_mother_surname unmatched = 24860
// m Total = 32787
    private static final double m_prior_baby_groom_mother_surname = 0.24177265;

    public static final List<Double> m_priors = Arrays.asList( new Double[]{ m_prior_baby_groom_first,m_prior_baby_groom_second,m_prior_baby_groom_father_first,m_prior_baby_groom_father_surname,m_prior_baby_groom_mother_first,m_prior_baby_groom_mother_surname} );
    public static final List<Double> u_priors = Arrays.asList( new Double[]{ u_prior_baby_groom_first,u_prior_baby_groom_second,u_prior_baby_groom_father_first,u_prior_baby_groom_father_surname,u_prior_baby_groom_mother_first,u_prior_baby_groom_mother_surname } );

    public static BirthGroomIdentityLinkageRecipe getRecipe(String sourceRepo, String number_of_records) {
        return new BirthGroomIdentityLinkageRecipe(sourceRepo, number_of_records, m_priors, u_priors, odds_prior, BirthGroomOwnMarriageBuilder.class.getName() );
    }

    public static void main(String[] args) throws Exception {

        String sourceRepo = args[0];  // e.g. umea
        String number_of_records = args[1]; // e.g. EVERYTHING or 10000 etc.

        try(
            BirthGroomIdentityLinkageRecipe linkageRecipe = new BirthGroomIdentityLinkageRecipe(sourceRepo, number_of_records, m_priors, u_priors, odds_prior, BirthBrideSiblingBundleBuilder.class.getName() ) ) {

            BitBlasterLinkageRunner runner = new BitBlasterLinkageRunner();

            int linkage_fields = linkageRecipe.ALL_LINKAGE_FIELDS;
            int half_fields = linkage_fields - (linkage_fields / 2 );

            while( linkage_fields >= half_fields ) {
                linkageRecipe.setNumberLinkageFieldsRequired(linkage_fields);
                LinkageResult lr = runner.run(linkageRecipe, new BirthGroomOwnMarriageBuilder(), false, true);
                LinkageQuality quality = lr.getLinkageQuality();
                quality.print(System.out);
                linkage_fields--;
            }
        }
    }

    public void makePersistent(LinkageRecipe recipe, Link link) {
        try {
            String std_id1 = link.getRecord1().getReferend(Birth.class).getString(Birth.STANDARDISED_ID);
            String std_id2 = link.getRecord2().getReferend(Marriage.class).getString(Marriage.STANDARDISED_ID);

            if (!Query.BMBirthGroomReferenceExists(recipe.getBridge(), std_id1, std_id2, recipe.getLinksPersistentName())) {

                Query.createBirthGroomOwnMarriageReference(
                        recipe.getBridge(),
                        std_id1,
                        std_id2,
                        recipe.getLinksPersistentName(),
                        recipe.getNumberOfLinkageFieldsRequired(),
                        link.getDistance());
            }
        } catch (uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}
