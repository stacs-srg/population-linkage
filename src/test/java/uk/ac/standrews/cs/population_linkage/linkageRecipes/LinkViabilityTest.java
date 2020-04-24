/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import org.junit.Test;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.unused.DeathBrideOwnMarriageIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.unused.DeathGroomOwnMarriageIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinkViabilityTest {

    @Test
    public void birthSiblingViability() {

        assertTrue(birthSiblingLinkViable(1920, 1921));
        assertTrue(birthSiblingLinkViable(1921, 1920));
        assertTrue(birthSiblingLinkViable(1921, 1921));
        assertTrue(birthSiblingLinkViable(1920, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFF));

        assertFalse(birthSiblingLinkViable(1920, 1980));
        assertFalse(birthSiblingLinkViable(1920, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFF + 1));

        assertTrue(birthBirthSiblingLinkViableWithInvalidData());
    }

    @Test
    public void birthDeathIdentityViability() {

        assertTrue(birthDeathIdentityLinkViable(1920, 1920));
        assertTrue(birthDeathIdentityLinkViable(1920, 1921));
        assertTrue(birthDeathIdentityLinkViable(1920, 1920 + LinkageConfig.MAX_AGE_AT_DEATH));

        assertFalse(birthDeathIdentityLinkViable(1920, 1919));
        assertFalse(birthDeathIdentityLinkViable(1860, 1990));
        assertFalse(birthDeathIdentityLinkViable(1920, 1920 + LinkageConfig.MAX_AGE_AT_DEATH + 1));

        assertTrue(birthDeathIdentityLinkViableWithInvalidData());
    }

    @Test
    public void birthFatherIdentityViability() {

        assertTrue(birthFatherIdentityLinkViable(1920, 1940));
        assertTrue(birthFatherIdentityLinkViable(1920, 1920 + LinkageConfig.MIN_PARENT_AGE_AT_BIRTH));
        assertTrue(birthFatherIdentityLinkViable(1920, 1920 + LinkageConfig.MAX_PARENT_AGE_AT_BIRTH));

        assertFalse(birthFatherIdentityLinkViable(1940, 1920));
        assertFalse(birthFatherIdentityLinkViable(1920, 1920 + LinkageConfig.MIN_PARENT_AGE_AT_BIRTH - 1));
        assertFalse(birthFatherIdentityLinkViable(1920, 1920 + LinkageConfig.MAX_PARENT_AGE_AT_BIRTH + 1));

        assertTrue(birthFatherIdentityLinkViableWithInvalidData());
    }

    @Test
    public void birthMotherIdentityViability() {

        assertTrue(birthMotherIdentityLinkViable(1920, 1940));
        assertTrue(birthMotherIdentityLinkViable(1920, 1920 + LinkageConfig.MIN_PARENT_AGE_AT_BIRTH));
        assertTrue(birthMotherIdentityLinkViable(1920, 1920 + LinkageConfig.MAX_PARENT_AGE_AT_BIRTH));

        assertFalse(birthMotherIdentityLinkViable(1940, 1920));
        assertFalse(birthMotherIdentityLinkViable(1920, 1920 + LinkageConfig.MIN_PARENT_AGE_AT_BIRTH - 1));
        assertFalse(birthMotherIdentityLinkViable(1920, 1920 + LinkageConfig.MAX_PARENT_AGE_AT_BIRTH + 1));

        assertTrue(birthMotherIdentityLinkViableWithInvalidData());
    }

    @Test
    public void birthBrideIdentityViability() {

        assertTrue(birthBrideIdentityLinkViable(1,7,1920, 1,1,1900, "20"));
        assertTrue(birthBrideIdentityLinkViable(1,7,1920, 1,1,1900, "01/01/1900"));
        assertTrue(birthBrideIdentityLinkViable(1,7,1920, 1,1,1900, "19"));
        assertTrue(birthBrideIdentityLinkViable(1,7,1920, 1,1,1900, "01/01/1901"));
        assertTrue(birthBrideIdentityLinkViable(1,7,1920, 1,1,1900, String.valueOf(20 + LinkageConfig.MAX_ALLOWABLE_MARRIAGE_AGE_DISCREPANCY)));
        assertTrue(birthBrideIdentityLinkViable(1,7,1930, 1,1,1900, String.valueOf(30 - LinkageConfig.MAX_ALLOWABLE_MARRIAGE_AGE_DISCREPANCY)));

        assertFalse(birthBrideIdentityLinkViable(1,7,1920, 1,1,1910, "10"));
        assertFalse(birthBrideIdentityLinkViable(1,7,1920, 1,1,1910, "20"));
        assertFalse(birthBrideIdentityLinkViable(1,7,1930, 1,1,1900, String.valueOf(30 - LinkageConfig.MAX_ALLOWABLE_MARRIAGE_AGE_DISCREPANCY - 1)));

        assertTrue(birthBrideIdentityLinkViableWithInvalidData());
    }

    @Test
    public void brideBrideSiblingViability() {

        assertTrue(brideBrideSiblingLinkViable(1, 7,1920, "25", 31,12, 1925, "22"));
        assertTrue(brideBrideSiblingLinkViable(1, 7,1920, "1/1/1895", 31,12, 1925, "30/11/1903"));
        assertTrue(brideBrideSiblingLinkViable( 31,12, 1925, "22", 1, 7,1920, "25"));
        assertTrue(brideBrideSiblingLinkViable(1, 7,1920, "25", 1,7, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFF, "25"));

        assertFalse(brideBrideSiblingLinkViable(1, 1,1925, "25", 1,1, 2000, "25"));
        assertFalse(brideBrideSiblingLinkViable(1, 1,1920, "1/1/1895", 1,1, 2000, "30/11/1975"));
        assertFalse(brideBrideSiblingLinkViable(1, 7,1920, "25", 1,7, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFF + 1, "25"));

        assertTrue(brideBrideSiblingLinkViableWithInvalidData());
    }

    @Test
    public void brideGroomSiblingViability() {

        assertTrue(brideGroomSiblingLinkViable(1, 7,1920, "25", 31,12, 1925, "22"));
        assertTrue(brideGroomSiblingLinkViable(1, 7,1920, "1/1/1895", 31,12, 1925, "30/11/1903"));
        assertTrue(brideGroomSiblingLinkViable( 31,12, 1925, "22", 1, 7,1920, "25"));
        assertTrue(brideGroomSiblingLinkViable(1, 7,1920, "25", 1,7, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFF, "25"));

        assertFalse(brideGroomSiblingLinkViable(1, 1,1925, "25", 1,1, 2000, "25"));
        assertFalse(brideGroomSiblingLinkViable(1, 1,1920, "1/1/1895", 1,1, 2000, "30/11/1975"));
        assertFalse(brideGroomSiblingLinkViable(1, 7,1920, "25", 1,7, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFF + 1, "25"));

        assertTrue(brideGroomSiblingLinkViableWithInvalidData());
    }

    @Test
    public void deathBrideMarriageIdentityViability() {

        assertTrue(deathBrideMarriageIdentityLinkViable(1920,1920));
        assertTrue(deathBrideMarriageIdentityLinkViable(1921,1920));

        assertFalse(deathBrideMarriageIdentityLinkViable(1920,1921));

        assertTrue(deathBrideMarriageIdentityLinkViableWithInvalidData());
    }

    @Test
    public void deathSiblingViability() {

        assertTrue(deathSiblingLinkViable(1920, 50, 1930, 60));
        assertTrue(deathSiblingLinkViable(1920, 50, 1930, 50));
        assertTrue(deathSiblingLinkViable(1930, 50, 1920, 50));
        assertTrue(deathSiblingLinkViable(1930, 50, 1930 + LinkageConfig.MAX_SIBLING_AGE_DIFF, 50));

        assertFalse(deathSiblingLinkViable(1920, 50, 1980, 20));
        assertFalse(deathSiblingLinkViable(1930, 50, 1931 + LinkageConfig.MAX_SIBLING_AGE_DIFF, 50));

        assertTrue(deathDeathSiblingLinkViableWithInvalidData());
    }

    @Test
    public void deathGroomMarriageIdentityViability() {

        assertTrue(deathGroomMarriageIdentityLinkViable(1920,1920));
        assertTrue(deathGroomMarriageIdentityLinkViable(1921,1920));

        assertFalse(deathGroomMarriageIdentityLinkViable(1920,1921));

        assertTrue(deathGroomMarriageIdentityLinkViableWithInvalidData());
    }

    @Test
    public void birthGroomIdentityViability() {

        assertTrue(groomBirthIdentityLinkViable(1,7,1920, 1,1,1900, "20"));
        assertTrue(groomBirthIdentityLinkViable(1,7,1920, 1,1,1900, "01/01/1900"));
        assertTrue(groomBirthIdentityLinkViable(1,7,1920, 1,1,1900, "19"));
        assertTrue(groomBirthIdentityLinkViable(1,7,1920, 1,1,1900, "01/01/1901"));
        assertTrue(groomBirthIdentityLinkViable(1,7,1920, 1,1,1900, String.valueOf(20 + LinkageConfig.MAX_ALLOWABLE_MARRIAGE_AGE_DISCREPANCY)));
        assertTrue(groomBirthIdentityLinkViable(1,7,1930, 1,1,1900, String.valueOf(30 - LinkageConfig.MAX_ALLOWABLE_MARRIAGE_AGE_DISCREPANCY)));

        assertFalse(groomBirthIdentityLinkViable(1,7,1920, 1,1,1910, "10"));
        assertFalse(groomBirthIdentityLinkViable(1,7,1920, 1,1,1910, "20"));
        assertFalse(groomBirthIdentityLinkViable(1,7,1930, 1,1,1900, String.valueOf(30 - LinkageConfig.MAX_ALLOWABLE_MARRIAGE_AGE_DISCREPANCY - 1)));

        assertTrue(groomBirthIdentityLinkViableWithInvalidData());
    }

    @Test
    public void groomGroomSiblingViability() {

        assertTrue(groomGroomSiblingLinkViable(1, 7,1920, "25", 31,12, 1925, "22"));
        assertTrue(groomGroomSiblingLinkViable(1, 7,1920, "1/1/1895", 31,12, 1925, "30/11/1903"));
        assertTrue(groomGroomSiblingLinkViable( 31,12, 1925, "22", 1, 7,1920, "25"));
        assertTrue(groomGroomSiblingLinkViable(1, 7,1920, "25", 1,7, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFF, "25"));

        assertFalse(groomGroomSiblingLinkViable(1, 1,1925, "25", 1,1, 2000, "25"));
        assertFalse(groomGroomSiblingLinkViable(1, 1,1920, "1/1/1895", 1,1, 2000, "30/11/1975"));
        assertFalse(groomGroomSiblingLinkViable(1, 7,1920, "25", 1,7, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFF + 1, "25"));

        assertTrue(groomGroomSiblingLinkViableWithInvalidData());
    }

    private boolean birthSiblingLinkViable(final int birth_year1, final int birth_year2) {

        final LXP record1 = makeBirth(birth_year1);
        final LXP record2 = makeBirth(birth_year2);

        return BirthSiblingLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean birthBirthSiblingLinkViableWithInvalidData() {

        final LXP record1 = makeInvalidBirth();
        final LXP record2 = makeInvalidBirth();

        return BirthSiblingLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean birthDeathIdentityLinkViable(final int birth_year, final int death_year) {

        final LXP record1 = makeBirth(birth_year);
        final LXP record2 = makeDeath(death_year, 0); // Don't care about age of death recorded on death record.

        return BirthDeathIdentityLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean birthDeathIdentityLinkViableWithInvalidData() {

        final LXP record1 = makeInvalidBirth();
        final LXP record2 = makeInvalidDeath();

        return BirthDeathIdentityLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean birthFatherIdentityLinkViable(final int birth_year1, final int birth_year2) {

        final LXP record1 = makeBirth(birth_year1);
        final LXP record2 = makeBirth(birth_year2);

        return BirthFatherIdentityLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean birthFatherIdentityLinkViableWithInvalidData() {

        final LXP record1 = makeInvalidBirth();
        final LXP record2 = makeInvalidBirth();

        return BirthFatherIdentityLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean birthMotherIdentityLinkViable(final int birth_year1, final int birth_year2) {

        final LXP record1 = makeBirth(birth_year1);
        final LXP record2 = makeBirth(birth_year2);

        return BirthMotherIdentityLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean birthMotherIdentityLinkViableWithInvalidData() {

        final LXP record1 = makeInvalidBirth();
        final LXP record2 = makeInvalidBirth();

        return BirthMotherIdentityLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean birthBrideIdentityLinkViable(final int marriage_day, final int marriage_month, final int marriage_year, final int birth_day, final int birth_month, final int birth_year, final String age_or_date_of_birth) {

        final LXP record1 = makeMarriage(marriage_day, marriage_month, marriage_year, age_or_date_of_birth, true);
        final LXP record2 = makeBirth(birth_day, birth_month, birth_year);

        return BirthBrideIdentityLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean birthBrideIdentityLinkViableWithInvalidData() {

        final LXP record1 = makeInvalidMarriage();
        final LXP record2 = makeInvalidBirth();

        return BirthBrideIdentityLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean brideBrideSiblingLinkViable(final int marriage_day1, final int marriage_month1, final int marriage_year1, final String age_or_date_of_birth1, final int marriage_day2, final int marriage_month2, final int marriage_year2, final String age_or_date_of_birth2) {

        final LXP record1 = makeMarriage(marriage_day1, marriage_month1, marriage_year1, age_or_date_of_birth1, true);
        final LXP record2 = makeMarriage(marriage_day2, marriage_month2, marriage_year2, age_or_date_of_birth2, true);

        return BrideBrideSiblingLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean brideBrideSiblingLinkViableWithInvalidData() {

        final LXP record1 = makeInvalidMarriage();
        final LXP record2 = makeInvalidMarriage();

        return BrideBrideSiblingLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean brideGroomSiblingLinkViable(final int marriage_day1, final int marriage_month1, final int marriage_year1, final String age_or_date_of_birth1, final int marriage_day2, final int marriage_month2, final int marriage_year2, final String age_or_date_of_birth2) {

        final LXP record1 = makeMarriage(marriage_day1, marriage_month1, marriage_year1, age_or_date_of_birth1, true);
        final LXP record2 = makeMarriage(marriage_day2, marriage_month2, marriage_year2, age_or_date_of_birth2, false);

        return BrideGroomSiblingLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean brideGroomSiblingLinkViableWithInvalidData() {

        final LXP record1 = makeInvalidMarriage();
        final LXP record2 = makeInvalidMarriage();

        return BrideGroomSiblingLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean deathSiblingLinkViable(final int death_year1, final int age_at_death1, final int death_year2, final int age_at_death2) {

        final LXP record1 = makeDeath(death_year1, age_at_death1);
        final LXP record2 = makeDeath(death_year2, age_at_death2);

        return DeathSiblingLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean deathDeathSiblingLinkViableWithInvalidData() {

        final LXP record1 = makeInvalidDeath();
        final LXP record2 = makeInvalidDeath();

        return DeathSiblingLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean deathBrideMarriageIdentityLinkViable(final int death_year, final int marriage_year) {

        final LXP record1 = makeDeath(death_year);
        final LXP record2 = makeMarriage(marriage_year);

        return DeathBrideOwnMarriageIdentityLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean deathBrideMarriageIdentityLinkViableWithInvalidData() {

        final LXP record1 = makeInvalidMarriage();
        final LXP record2 = makeInvalidBirth();

        return DeathBrideOwnMarriageIdentityLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }
    private boolean deathGroomMarriageIdentityLinkViable(final int death_year, final int marriage_year) {

        final LXP record1 = makeDeath(death_year);
        final LXP record2 = makeMarriage(marriage_year);

        return DeathGroomOwnMarriageIdentityLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean deathGroomMarriageIdentityLinkViableWithInvalidData() {

        final LXP record1 = makeInvalidMarriage();
        final LXP record2 = makeInvalidBirth();

        return DeathGroomOwnMarriageIdentityLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }



    private boolean groomBirthIdentityLinkViable(final int marriage_day, final int marriage_month, final int marriage_year, final int birth_day, final int birth_month, final int birth_year, final String age_or_date_of_birth) {

        final LXP record1 = makeMarriage(marriage_day, marriage_month, marriage_year, age_or_date_of_birth, false);
        final LXP record2 = makeBirth(birth_day, birth_month, birth_year);

        return BirthGroomIdentityLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean groomBirthIdentityLinkViableWithInvalidData() {

        final LXP record1 = makeInvalidMarriage();
        final LXP record2 = makeInvalidBirth();

        return BirthGroomIdentityLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean groomGroomSiblingLinkViable(final int marriage_day1, final int marriage_month1, final int marriage_year1, final String age_or_date_of_birth1, final int marriage_day2, final int marriage_month2, final int marriage_year2, final String age_or_date_of_birth2) {

        final LXP record1 = makeMarriage(marriage_day1, marriage_month1, marriage_year1, age_or_date_of_birth1, false);
        final LXP record2 = makeMarriage(marriage_day2, marriage_month2, marriage_year2, age_or_date_of_birth2, false);

        return GroomGroomSiblingLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private boolean groomGroomSiblingLinkViableWithInvalidData() {

        final LXP record1 = makeInvalidMarriage();
        final LXP record2 = makeInvalidMarriage();

        return GroomGroomSiblingLinkageRecipe.isViable(new RecordPair(record1, record2, 0.0));
    }

    private Birth makeBirth(int birth_year) {

        final Birth record = new Birth();

        for (int i = 0; i < Birth.BIRTH_YEAR; i++) {
            record.put(i, "");
        }

        record.put(Birth.BIRTH_YEAR, String.valueOf(birth_year));
        return record;
    }

    private Birth makeBirth(int birth_day, int birth_month, int birth_year) {

        final Birth record = new Birth();

        for (int i = 0; i < Birth.BIRTH_DAY; i++) {
            record.put(i, "");
        }

        record.put(Birth.BIRTH_DAY, String.valueOf(birth_day));
        record.put(Birth.BIRTH_MONTH, String.valueOf(birth_month));
        record.put(Birth.BIRTH_YEAR, String.valueOf(birth_year));
        return record;
    }

    private Birth makeInvalidBirth() {

        final Birth record = new Birth();

        for (int i = 0; i < Birth.BIRTH_YEAR; i++) {
            record.put(i, "");
        }

        record.put(Birth.BIRTH_YEAR, "Unknown");
        return record;
    }

    private Death makeDeath(int death_year) {

        final Death record = new Death();

        for (int i = 0; i < Death.DEATH_YEAR; i++) {
            record.put(i, "");
        }

        record.put(Death.DEATH_YEAR, String.valueOf(death_year));
        return record;
    }

    private Death makeDeath(int death_year, int age_at_death) {

        final Death record = new Death();

        for (int i = 0; i < Death.DEATH_YEAR; i++) {
            record.put(i, "");
        }

        record.put(Death.DEATH_YEAR, String.valueOf(death_year));
        record.put(Death.AGE_AT_DEATH, String.valueOf(age_at_death));
        return record;
    }

    private Death makeInvalidDeath() {

        final Death record = new Death();

        for (int i = 0; i < Death.DEATH_YEAR; i++) {
            record.put(i, "");
        }

        record.put(Death.DEATH_YEAR, "Unknown");
        record.put(Death.AGE_AT_DEATH, "Unknown");
        return record;
    }

    private Marriage makeMarriage(final int marriage_year) {

        final Marriage record = new Marriage();

        for (int i = 0; i < Marriage.MARRIAGE_YEAR; i++) {
            record.put(i, "");
        }

        record.put(Marriage.MARRIAGE_YEAR, String.valueOf(marriage_year));

        return record;
    }

    private Marriage makeMarriage(final int marriage_day, final int marriage_month, final int marriage_year, String age_or_date_of_birth, boolean spouse_is_bride) {

        final Marriage record = new Marriage();

        for (int i = 0; i < Marriage.MARRIAGE_DAY; i++) {
            record.put(i, "");
        }

        record.put(Marriage.MARRIAGE_DAY, String.valueOf(marriage_day));
        record.put(Marriage.MARRIAGE_MONTH, String.valueOf(marriage_month));
        record.put(Marriage.MARRIAGE_YEAR, String.valueOf(marriage_year));
        record.put(spouse_is_bride ? Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH : Marriage.GROOM_AGE_OR_DATE_OF_BIRTH, age_or_date_of_birth);

        return record;
    }

    private Marriage makeInvalidMarriage() {

        final Marriage record = new Marriage();

        for (int i = 0; i < Marriage.MARRIAGE_DAY; i++) {
            record.put(i, "");
        }

        record.put(Marriage.MARRIAGE_DAY, "Unknown");
        record.put(Marriage.MARRIAGE_MONTH, "Unknown");
        record.put(Marriage.MARRIAGE_YEAR, "Unknown");
        record.put(Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH, "Unknown");
        record.put(Marriage.GROOM_AGE_OR_DATE_OF_BIRTH, "Unknown");

        return record;
    }
}
