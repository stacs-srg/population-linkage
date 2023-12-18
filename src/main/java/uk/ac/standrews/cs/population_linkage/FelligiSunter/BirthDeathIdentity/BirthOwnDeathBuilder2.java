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
package uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthDeathIdentity;

import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthGroomOwnMarriage.BirthGroomOwnMarriageBuilder;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.graph.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.linkageRunners.MakePersistent;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.Arrays;
import java.util.List;

/**
 *  This class attempts to find birth-death links: links a baby on a birth to the same person as the deceased on a death record.
 *  This is NOT STRONG: uses the 3 names: the groom/baby and the names of the mother and father.
 */
public class BirthOwnDeathBuilder2 implements MakePersistent {

    // expected_matches = 54384
    private static final double m_prior = 1.5985243959692483E-6;
    private static final double u_prior = 1d - m_prior;
    public static final double odds_prior = m_prior/u_prior;

    // u baby_deceased_first match = 120
// u baby_deceased_first unmatched = 62785
// u Total = 62905
    private static final double u_prior_baby_deceased_first = 0.0019076385;
    // u baby_deceased_second match = 399
// u baby_deceased_second unmatched = 62506
// u Total = 62905
    private static final double u_prior_baby_deceased_second = 0.006342898;
    // m baby_deceased_first match = 61415
// m baby_deceased_first unmatched = 31767
// m Total = 93182
    private static final double m_prior_baby_deceased_first = 0.6590865;
    // m baby_deceased_second match = 56704
// m baby_deceased_second unmatched = 36478
// m Total = 93182
    private static final double m_prior_baby_deceased_second = 0.60852957;
    // u baby_deceased_father_first match = 2790
// u baby_deceased_father_first unmatched = 59950
// u Total = 62740
    private static final double u_prior_baby_deceased_father_first = 0.044469237;
    // u baby_deceased_father_surname match = 2601
// u baby_deceased_father_surname unmatched = 60139
// u Total = 62740
    private static final double u_prior_baby_deceased_father_surname = 0.041456807;
    // m baby_deceased_father_first match = 36624
// m baby_deceased_father_first unmatched = 56558
// m Total = 93182
    private static final double m_prior_baby_deceased_father_first = 0.3930373;
    // m baby_deceased_father_surname match = 41768
// m baby_deceased_father_surname unmatched = 51414
// m Total = 93182
    private static final double m_prior_baby_deceased_father_surname = 0.44824108;
    // u baby_groom_mother_first match = 697
// u baby_groom_mother_first unmatched = 62247
// u Total = 62944
    private static final double u_prior_baby_groom_mother_first = 0.011073335;
    // u baby_deceased_mother_surname match = 1493
// u baby_deceased_mother_surname unmatched = 61451
// u Total = 62944
    private static final double u_prior_baby_deceased_mother_surname = 0.023719497;
    // m baby_groom_mother_first match = 21789
// m baby_groom_mother_first unmatched = 71393
// m Total = 93182
    private static final double m_prior_baby_groom_mother_first = 0.23383272;
    // m baby_deceased_mother_surname match = 23128
// m baby_deceased_mother_surname unmatched = 70054
// m Total = 93182
    private static final double m_prior_baby_deceased_mother_surname = 0.24820244;

    public static final List<Double> m_priors = Arrays.asList( new Double[]{ m_prior_baby_deceased_first,m_prior_baby_deceased_second,m_prior_baby_groom_mother_first,m_prior_baby_deceased_mother_surname,m_prior_baby_deceased_father_first,m_prior_baby_deceased_father_surname} );
    public static final List<Double> u_priors = Arrays.asList( new Double[]{ u_prior_baby_deceased_first,u_prior_baby_deceased_second,u_prior_baby_groom_mother_first,u_prior_baby_deceased_mother_surname,u_prior_baby_deceased_father_first,u_prior_baby_deceased_father_surname } );

    public static BirthDeathIdentityLinkageRecipe getRecipe(String sourceRepo, String number_of_records) {
        return new BirthDeathIdentityLinkageRecipe(sourceRepo, number_of_records, m_priors, u_priors, odds_prior, BirthGroomOwnMarriageBuilder.class.getName() );
    }

    public static void main(String[] args) throws Exception {

        String sourceRepo = args[0];  // e.g. umea
        String number_of_records = args[1]; // e.g. EVERYTHING or 10000 etc.
        StringMeasure base_measure = Constants.get(args[2]);
        double threshold = Double.parseDouble(args[3]);

        try (
             BirthDeathIdentityLinkageRecipe linkageRecipe = new BirthDeathIdentityLinkageRecipe(sourceRepo, number_of_records, m_priors, u_priors, odds_prior, BirthOwnDeathBuilder2.class.getName() ) ) {

//            BitBlasterLinkageRunnerAnalyseLinks runner = new BitBlasterLinkageRunnerAnalyseLinks();
            LXPMeasure record_distance_measure = new LXPMeasure(linkageRecipe.getLinkageFields(), linkageRecipe.getQueryMappingFields(), base_measure);
            BitBlasterLinkageRunnerAnalyseLinks runner = new BitBlasterLinkageRunnerAnalyseLinks(record_distance_measure, threshold);

            int linkage_fields = linkageRecipe.ALL_LINKAGE_FIELDS;

            linkageRecipe.setNumberLinkageFieldsRequired(linkage_fields);
            LinkageResult lr = runner.listsRun(linkageRecipe, new BirthOwnDeathBuilder2(), false, false, true);
            LinkageQuality quality = lr.getLinkageQuality();
            quality.print(System.out);
            linkage_fields--;
        }
    }

    @Override
    public void makePersistent(LinkageRecipe recipe, Link link) {
        try {
            final String std_id1 = link.getRecord1().getReferend(Birth.class).getString(Birth.STANDARDISED_ID);
            final String std_id2 = link.getRecord2().getReferend(Death.class).getString(Death.STANDARDISED_ID);

            if( ! Query.BDDeathReferenceExists(recipe.getBridge(), std_id1, std_id2, recipe.getLinksPersistentName())) {

                Query.createBDReference(
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
