package uk.ac.standrews.cs.population_linkage.groundTruth.umea;

import uk.ac.standrews.cs.data.umea.UmeaBirthsDataSet;
import uk.ac.standrews.cs.data.umea.UmeaDeathsDataSet;
import uk.ac.standrews.cs.data.umea.UmeaMarriagesDataSet;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MarriageDeathIdentityLinks {

    public static final List<List<LinkageRecipe.Pair>> TRUE_MATCH_ALTERNATIVES1 = LinkageRecipe.list(
            LinkageRecipe.list(LinkageRecipe.pair(Death.DECEASED_IDENTITY, Marriage.GROOM_IDENTITY))
    );

    public static final List<List<LinkageRecipe.Pair>> TRUE_MATCH_ALTERNATIVES2 = LinkageRecipe.list(
            LinkageRecipe.list(LinkageRecipe.pair(Death.BIRTH_RECORD_IDENTITY, Marriage.GROOM_BIRTH_RECORD_IDENTITY))
    );

    public static final List<List<LinkageRecipe.Pair>> TRUE_MATCH_ALTERNATIVES3 = LinkageRecipe.list(
            LinkageRecipe.list(LinkageRecipe.pair(Death.DECEASED_IDENTITY, Marriage.GROOM_IDENTITY)),
            LinkageRecipe.list(LinkageRecipe.pair(Death.BIRTH_RECORD_IDENTITY, Marriage.GROOM_BIRTH_RECORD_IDENTITY))
    );

    public static void main(String[] args) throws IOException {

        int SAMPLE_SIZE = 1000;

        List<Death> deaths = new ArrayList<>();
        for (Death death : Death.convertToRecords(new UmeaDeathsDataSet())) deaths.add(death);
        final List<Death> deaths_subset = Utilities.permute(deaths).subList(0, SAMPLE_SIZE);

        Iterable<Marriage> marriages = Marriage.convertToRecords(new UmeaMarriagesDataSet());

        int death_record_count = 0;

        int link_count1 = 0;
        int link_count2 = 0;
        int link_count3 = 0;

        for (Death death : deaths_subset) {

            System.out.println(death_record_count);
            death_record_count++;

            for (Marriage marriage : marriages) {

                if (LinkageRecipe.trueMatch(death, marriage, TRUE_MATCH_ALTERNATIVES1) == LinkStatus.TRUE_MATCH) {
                    link_count1++;
                }

                if (LinkageRecipe.trueMatch(death, marriage, TRUE_MATCH_ALTERNATIVES2) == LinkStatus.TRUE_MATCH) {
                    link_count2++;
                }

                if (LinkageRecipe.trueMatch(death, marriage, TRUE_MATCH_ALTERNATIVES3) == LinkStatus.TRUE_MATCH) {
                    link_count3++;
                }
            }
        }

        System.out.println("matches on " + SAMPLE_SIZE + " deaths using only deceased/groom identity: " + link_count1);
        System.out.println("matches on " + SAMPLE_SIZE + " deaths using only deceased/groom birth record identity: " + link_count2);
        System.out.println("matches on " + SAMPLE_SIZE + " deaths using either identity: " + link_count3);
    }
}
