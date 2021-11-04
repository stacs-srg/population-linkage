/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import org.junit.Test;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.neoStorr.impl.LXP;

import static org.junit.Assert.*;

public class LinkViabilityTest {

    @Test
    public void birthBrideIdentityViability() {

        // All consistent.
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1,7,1920, "20"));
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1,7,1920, "01/01/1900"));

        // Slight differences between ages derived from different records.
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1,7,1920, "19"));
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1,7,1920, "01/01/1901"));
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1,7,1920, "21"));
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1,7,1920, "01/01/1899"));

        // Significant differences between ages derived from different records, but within acceptable bounds.
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1,7,1920, "24"));
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1,7,1930, "26"));

        // Invalid age of marriage on marriage record.
        assertFalse(birthBrideIdentityLinkViable(1, 1, 1910, 1,7,1920, "10"));

        // Significant differences between ages derived from different records, exceeding acceptable bounds.
        assertFalse(birthBrideIdentityLinkViable(1, 1, 1910, 1,7,1920, "20"));
        assertFalse(birthBrideIdentityLinkViable(1, 1, 1900, 1,7,1930, "25"));
        assertFalse(birthBrideIdentityLinkViable(1, 1, 1920, 1,7,1910, "25"));

        assertTrue(birthBrideIdentityLinkViableWithInvalidData());
    }

    @Test
    public void birthBrideSiblingViability() {

        // All consistent.
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1,7,1920, "20"));
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1,7,1920, "30"));
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1,7,1910, "20"));
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1,7,1899, "20"));

        // Consistent, with full or partial date of birth on marriage record.
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1,7,1910, "--/--/1890"));
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1,7,1910, "29/02/1890"));

        // Significant difference between sibling ages, but within acceptable bounds.
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1,7,1920, "60"));

        // Significant difference between sibling ages, exceeding acceptable bounds.
        assertFalse(birthBrideSiblingLinkViable(1, 1, 1900, 1,7,1920, "61"));
        assertFalse(birthBrideSiblingLinkViable(1, 1, 1800, 1,7,1920, "20"));
        assertFalse(birthBrideSiblingLinkViable(1, 1, 1900, 1,7,1910, "--/--/1790"));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1,7,1710, "--/--/"));
        assertTrue(birthBrideSiblingLinkViableWithInvalidData());
    }

    @Test
    public void birthDeathIdentityViability() {

        // All consistent.
        assertTrue(birthDeathIdentityLinkViable(1900, 1940, 40,"01/01/1900"));

        // Dates of birth on birth and death records slightly different.
        assertTrue(birthDeathIdentityLinkViable(1900, 1940, 38,"01/01/1902"));

        // Dates of birth on birth and death records significantly different.
        assertFalse(birthDeathIdentityLinkViable(1900, 1940, 35,"01/01/1905"));

        // Born after death.
        assertFalse(birthDeathIdentityLinkViable(1941, 1940, 40,"01/01/1900"));

        // Too old at death.
        assertFalse(birthDeathIdentityLinkViable(1841, 1970, 40,"01/01/1930"));

        // Details on death record slightly internally inconsistent.
        assertTrue(birthDeathIdentityLinkViable(1900, 1940, 40,"01/01/1902"));
        assertTrue(birthDeathIdentityLinkViable(1900, 1940, 40,"01/01/1898"));

        // Details on death record significantly internally inconsistent.
        assertFalse(birthDeathIdentityLinkViable(1900, 1940, 40,"01/01/1905"));
        assertFalse(birthDeathIdentityLinkViable(1900, 1940, 40,"01/01/1895"));

        assertTrue(birthDeathIdentityLinkViableWithInvalidData());
    }

    @Test
    public void birthDeathSiblingViability() {

        // Small sibling age difference.
        assertTrue(birthDeathSiblingLinkViable(1, 1, 1900, 1950,50,"01/01/1900"));
        assertTrue(birthDeathSiblingLinkViable(1, 1, 1900, 1950,48,"01/01/1902"));
        assertTrue(birthDeathSiblingLinkViable(1, 1, 1900, 1950,52,"01/01/1898"));

        // Acceptably large sibling age difference.
        assertTrue(birthDeathSiblingLinkViable(1, 1, 1900, 1990,50,"01/01/1940"));
        assertTrue(birthDeathSiblingLinkViable(1, 1, 1900, 1910,50,"01/01/1860"));

        // Sibling age difference too high.
        assertFalse(birthDeathSiblingLinkViable(1, 1, 1900, 1991,50,"01/01/1941"));
        assertFalse(birthDeathSiblingLinkViable(1, 1, 1900, 1909,50,"01/01/1859"));

        // Details on death record slightly internally inconsistent.
        assertTrue(birthDeathSiblingLinkViable(1, 1, 1900, 1940, 40,"01/01/1902"));
        assertTrue(birthDeathSiblingLinkViable(1, 1, 1900, 1940, 40,"01/01/1898"));

        // Details on death record significantly internally inconsistent.
        assertFalse(birthDeathSiblingLinkViable(1, 1, 1900, 1940, 40,"01/01/1905"));
        assertFalse(birthDeathSiblingLinkViable(1, 1, 1900, 1940, 40,"01/01/1895"));

        assertTrue(birthDeathSiblingLinkViableWithInvalidData());
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
    public void birthGroomIdentityViability() {

        // All consistent.
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1,7,1920, "20"));
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1,7,1920, "01/01/1900"));

        // Slight differences between ages derived from different records.
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1,7,1920, "19"));
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1,7,1920, "01/01/1901"));
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1,7,1920, "21"));
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1,7,1920, "01/01/1899"));

        // Significant differences between ages derived from different records, but within acceptable bounds.
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1,7,1920, "24"));
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1,7,1930, "26"));

        // Invalid age of marriage on marriage record.
        assertFalse(birthGroomIdentityLinkViable(1, 1, 1910, 1,7,1920, "10"));

        // Significant differences between ages derived from different records, exceeding acceptable bounds.
        assertFalse(birthGroomIdentityLinkViable(1, 1, 1910, 1,7,1920, "20"));
        assertFalse(birthGroomIdentityLinkViable(1, 1, 1900, 1,7,1930, "25"));
        assertFalse(birthGroomIdentityLinkViable(1, 1, 1920, 1,7,1910, "25"));

        assertTrue(birthGroomIdentityLinkViableWithInvalidData());
    }

    @Test
    public void birthGroomSiblingViability() {

        // All consistent.
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1,7,1920, "20"));
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1,7,1920, "30"));
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1,7,1910, "20"));
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1,7,1899, "20"));

        // Consistent, with full or partial date of birth on marriage record.
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1,7,1910, "--/--/1890"));
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1,7,1910, "29/02/1890"));

        // Significant difference between sibling ages, but within acceptable bounds.
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1,7,1920, "60"));

        // Significant difference between sibling ages, exceeding acceptable bounds.
        assertFalse(birthGroomSiblingLinkViable(1, 1, 1900, 1,7,1920, "61"));
        assertFalse(birthGroomSiblingLinkViable(1, 1, 1800, 1,7,1920, "20"));
        assertFalse(birthGroomSiblingLinkViable(1, 1, 1900, 1,7,1910, "--/--/1790"));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1,7,1710, "--/--/"));
        assertTrue(birthGroomSiblingLinkViableWithInvalidData());
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
    public void birthParentsMarriageIdentityViability() {

        fail();
    }

    @Test
    public void birthSiblingViability() {

        assertTrue(birthSiblingLinkViable(1920, 1921));
        assertTrue(birthSiblingLinkViable(1921, 1920));
        assertTrue(birthSiblingLinkViable(1921, 1921));
        assertTrue(birthSiblingLinkViable(1920, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE));

        assertFalse(birthSiblingLinkViable(1920, 1980));
        assertFalse(birthSiblingLinkViable(1920, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE + 1));

        assertTrue(birthBirthSiblingLinkViableWithInvalidData());
    }

    @Test
    public void brideBrideIdentityViability() {

        fail();
    }

    @Test
    public void brideBrideSiblingViability() {

        assertTrue(brideBrideSiblingLinkViable(1, 7,1920, "25", 31,12, 1925, "22"));
        assertTrue(brideBrideSiblingLinkViable(1, 7,1920, "1/1/1895", 31,12, 1925, "30/11/1903"));
        assertTrue(brideBrideSiblingLinkViable(31,12, 1925, "22", 1, 7,1920, "25"));
        assertTrue(brideBrideSiblingLinkViable(1, 7,1920, "25", 1,7, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE, "25"));

        assertFalse(brideBrideSiblingLinkViable(1, 1,1925, "25", 1,1, 2000, "25"));
        assertFalse(brideBrideSiblingLinkViable(1, 1,1920, "1/1/1895", 1,1, 2000, "30/11/1975"));
        assertFalse(brideBrideSiblingLinkViable(1, 7,1920, "25", 1,7, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE + 1, "25"));

        assertTrue(brideBrideSiblingLinkViableWithInvalidData());
    }

    @Test
    public void brideGroomSiblingViability() {

        assertTrue(brideGroomSiblingLinkViable(1, 7,1920, "25", 31,12, 1925, "22"));
        assertTrue(brideGroomSiblingLinkViable(1, 7,1920, "1/1/1895", 31,12, 1925, "30/11/1903"));
        assertTrue(brideGroomSiblingLinkViable(31,12, 1925, "22", 1, 7,1920, "25"));
        assertTrue(brideGroomSiblingLinkViable(1, 7,1920, "25", 1,7, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE, "25"));

        assertFalse(brideGroomSiblingLinkViable(1, 1,1925, "25", 1,1, 2000, "25"));
        assertFalse(brideGroomSiblingLinkViable(1, 1,1920, "1/1/1895", 1,1, 2000, "30/11/1975"));
        assertFalse(brideGroomSiblingLinkViable(1, 7,1920, "25", 1,7, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE + 1, "25"));

        assertTrue(brideGroomSiblingLinkViableWithInvalidData());
    }

    @Test
    public void brideMarriageParentsMarriageIdentityViability() {

        fail();
    }

    @Test
    public void deathBirthIdentityViability() {

        fail();
    }

    @Test
    public void deathBrideIdentityViability() {

        assertTrue(deathBrideIdentityLinkViable(1920,1920));
        assertTrue(deathBrideIdentityLinkViable(1921,1920));

        assertFalse(deathBrideIdentityLinkViable(1920,1921));

        assertTrue(deathBrideIdentityLinkViableWithInvalidData());
    }

    @Test
    public void deathBrideSiblingViability() {

        fail();
    }

    @Test
    public void deathGroomIdentityViability() {

        assertTrue(deathGroomIdentityLinkViable(1920,1920));
        assertTrue(deathGroomIdentityLinkViable(1921,1920));

        assertFalse(deathGroomIdentityLinkViable(1920,1921));

        assertTrue(deathGroomIdentityLinkViableWithInvalidData());
    }

    @Test
    public void deathGroomSiblingViability() {

        fail();
    }

    @Test
    public void deathSiblingViability() {

        assertTrue(deathSiblingLinkViable(1920, 50, 1930, 60));
        assertTrue(deathSiblingLinkViable(1920, 50, 1930, 50));
        assertTrue(deathSiblingLinkViable(1930, 50, 1920, 50));
        assertTrue(deathSiblingLinkViable(1930, 50, 1930 + LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE, 50));

        assertFalse(deathSiblingLinkViable(1920, 50, 1980, 20));
        assertFalse(deathSiblingLinkViable(1930, 50, 1931 + LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE, 50));

        assertTrue(deathDeathSiblingLinkViableWithInvalidData());
    }

    @Test
    public void groomBrideSiblingViability() {

        fail();
    }

    @Test
    public void groomGroomIdentityViability() {

        fail();
    }

    @Test
    public void groomGroomSiblingViability() {

        assertTrue(groomGroomSiblingLinkViable(1, 7,1920, "25", 31,12, 1925, "22"));
        assertTrue(groomGroomSiblingLinkViable(1, 7,1920, "1/1/1895", 31,12, 1925, "30/11/1903"));
        assertTrue(groomGroomSiblingLinkViable( 31,12, 1925, "22", 1, 7,1920, "25"));
        assertTrue(groomGroomSiblingLinkViable(1, 7,1920, "25", 1,7, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE, "25"));

        assertFalse(groomGroomSiblingLinkViable(1, 1,1925, "25", 1,1, 2000, "25"));
        assertFalse(groomGroomSiblingLinkViable(1, 1,1920, "1/1/1895", 1,1, 2000, "30/11/1975"));
        assertFalse(groomGroomSiblingLinkViable(1, 7,1920, "25", 1,7, 1920 + LinkageConfig.MAX_SIBLING_AGE_DIFFERENCE + 1, "25"));

        assertTrue(groomGroomSiblingLinkViableWithInvalidData());
    }

    @Test
    public void groomMarriageParentsMarriageIdentityViability() {

        fail();
    }

    @Test
    public void parentsMarriageBirthIdentityViability() {

        fail();
    }

    @Test
    public void parentsMarriageDeathIdentityViability() {

        fail();
    }

    @Test
    public void extractBirthYearFromMarriageRecord() {

        LXP record1 = makeMarriage(1, 1, 1890, "27", true, "1");
        LXP record2 = makeMarriage(1, 1, 1904, "05/10/1863", true, "1");
        LXP record3 = makeMarriage(1, 1, 1890, "27", false, "1");
        LXP record4 = makeMarriage(1, 1, 1904, "05/10/1863", false, "1");

        assertEquals(1863, CommonLinkViabilityLogic.getBirthYearOfPersonBeingMarried(record1, true));
        assertEquals(1863, CommonLinkViabilityLogic.getBirthYearOfPersonBeingMarried(record2, true));
        assertEquals(1863, CommonLinkViabilityLogic.getBirthYearOfPersonBeingMarried(record3, false));
        assertEquals(1863, CommonLinkViabilityLogic.getBirthYearOfPersonBeingMarried(record4, false));
    }

    @Test (expected = NumberFormatException.class)
    public void extractBirthYearFromMarriageRecordWithInvalidDate() {

        LXP record1 = makeMarriage(1, 1, 1904, "05/10/----", false, "1");

        assertEquals(1863, CommonLinkViabilityLogic.getBirthYearOfPersonBeingMarried(record1, false));
    }

    @Test (expected = NumberFormatException.class)
    public void extractBirthYearFromMarriageRecordWithInvalidDate2() {

        LXP record1 = makeInvalidMarriage("1");

        assertEquals(1863, CommonLinkViabilityLogic.getBirthYearOfPersonBeingMarried(record1, false));
    }

    private boolean birthSiblingLinkViable(final int birth_year1, final int birth_year2) {

        final LXP birth_record1 = makeBirth(birth_year1);
        final LXP birth_record2 = makeBirth(birth_year2);

        return BirthSiblingLinkageRecipe.isViable(new RecordPair(birth_record1, birth_record2, 0.0));
    }

    private boolean birthBirthSiblingLinkViableWithInvalidData() {

        final LXP birth_record1 = makeInvalidBirth();
        final LXP birth_record2 = makeInvalidBirth();

        return BirthSiblingLinkageRecipe.isViable(new RecordPair(birth_record1, birth_record2, 0.0));
    }

    private boolean birthDeathIdentityLinkViable(final int birth_year, final int death_year, int age_at_death, String date_of_birth) {

        final LXP birth_record = makeBirth(birth_year);
        final LXP death_record = makeDeath(death_year, age_at_death, date_of_birth);

        return BirthDeathIdentityLinkageRecipe.isViable(new RecordPair(birth_record, death_record, 0.0));
    }

    private boolean birthDeathIdentityLinkViableWithInvalidData() {

        final LXP birth_record = makeInvalidBirth();
        final LXP death_record = makeInvalidDeath();

        return BirthDeathIdentityLinkageRecipe.isViable(new RecordPair(birth_record, death_record, 0.0));
    }

    private boolean birthFatherIdentityLinkViable(final int birth_year1, final int birth_year2) {

        final LXP birth_record1 = makeBirth(birth_year1);
        final LXP birth_record2 = makeBirth(birth_year2);

        return BirthFatherIdentityLinkageRecipe.isViable(new RecordPair(birth_record1, birth_record2, 0.0));
    }

    private boolean birthFatherIdentityLinkViableWithInvalidData() {

        final LXP birth_record1 = makeInvalidBirth();
        final LXP birth_record2 = makeInvalidBirth();

        return BirthFatherIdentityLinkageRecipe.isViable(new RecordPair(birth_record1, birth_record2, 0.0));
    }

    private boolean birthMotherIdentityLinkViable(final int birth_year1, final int birth_year2) {

        final LXP birth_record1 = makeBirth(birth_year1);
        final LXP birth_record2 = makeBirth(birth_year2);

        return BirthMotherIdentityLinkageRecipe.isViable(new RecordPair(birth_record1, birth_record2, 0.0));
    }

    private boolean birthMotherIdentityLinkViableWithInvalidData() {

        final LXP birth_record1 = makeInvalidBirth();
        final LXP birth_record2 = makeInvalidBirth();

        return BirthMotherIdentityLinkageRecipe.isViable(new RecordPair(birth_record1, birth_record2, 0.0));
    }

    private boolean birthBrideIdentityLinkViable(final int birth_day, final int birth_month, final int birth_year, final int marriage_day, final int marriage_month, final int marriage_year, final String marriage_age_or_date_of_birth) {

        final LXP birth_record = makeBirth(birth_day, birth_month, birth_year);
        final LXP marriage_record = makeMarriage(marriage_day, marriage_month, marriage_year, marriage_age_or_date_of_birth, true, "1");

        return BirthBrideIdentityLinkageRecipe.isViable(new RecordPair(birth_record, marriage_record, 0.0));
    }

    private boolean birthBrideIdentityLinkViableWithInvalidData() {

        final LXP birth_record = makeInvalidBirth();
        final LXP marriage_record = makeInvalidMarriage("1");

        return BirthBrideIdentityLinkageRecipe.isViable(new RecordPair(birth_record, marriage_record, 0.0));
    }

    private boolean birthBrideSiblingLinkViable(final int birth_day, final int birth_month, final int birth_year, final int marriage_day, final int marriage_month, final int marriage_year, final String marriage_age_or_date_of_birth) {

        final LXP birth_record = makeBirth(birth_day, birth_month, birth_year);
        final LXP marriage_record = makeMarriage(marriage_day, marriage_month, marriage_year, marriage_age_or_date_of_birth, true, "1");

        return BirthBrideSiblingLinkageRecipe.isViable(new RecordPair(birth_record, marriage_record, 0.0));
    }

    private boolean birthBrideSiblingLinkViableWithInvalidData() {

        final LXP marriage_record = makeInvalidMarriage("1");
        final LXP birth_record = makeInvalidBirth();

        return BirthBrideSiblingLinkageRecipe.isViable(new RecordPair(birth_record, marriage_record, 0.0));
    }

    private boolean birthDeathSiblingLinkViable(final int birth_day, final int birth_month, final int birth_year, final int death_year, final int age_at_death, final String date_of_birth) {

        final LXP birth_record = makeBirth(birth_day, birth_month, birth_year);
        final LXP death_record = makeDeath(death_year, age_at_death, date_of_birth);

        return BirthDeathSiblingLinkageRecipe.isViable(new RecordPair(birth_record, death_record, 0.0));
    }

    private boolean birthDeathSiblingLinkViableWithInvalidData() {

        final LXP birth_record = makeInvalidBirth();
        final LXP death_record = makeInvalidDeath();

        return BirthDeathSiblingLinkageRecipe.isViable(new RecordPair(birth_record, death_record, 0.0));
    }

    private boolean birthGroomIdentityLinkViable(final int birth_day, final int birth_month, final int birth_year, final int marriage_day, final int marriage_month, final int marriage_year, final String age_or_date_of_birth) {

        final LXP birth_record = makeBirth(birth_day, birth_month, birth_year);
        final LXP marriage_record = makeMarriage(marriage_day, marriage_month, marriage_year, age_or_date_of_birth, false, "1");

        return BirthGroomIdentityLinkageRecipe.isViable(new RecordPair(birth_record, marriage_record, 0.0));
    }

    private boolean birthGroomSiblingLinkViable(final int birth_day, final int birth_month, final int birth_year, final int marriage_day, final int marriage_month, final int marriage_year, final String marriage_age_or_date_of_birth) {

        final LXP birth_record = makeBirth(birth_day, birth_month, birth_year);
        final LXP marriage_record = makeMarriage(marriage_day, marriage_month, marriage_year, marriage_age_or_date_of_birth, false, "1");

        return BirthGroomSiblingLinkageRecipe.isViable(new RecordPair(birth_record, marriage_record, 0.0));
    }

    private boolean birthGroomSiblingLinkViableWithInvalidData() {

        final LXP birth_record = makeInvalidBirth();
        final LXP marriage_record = makeInvalidMarriage("1");

        return BirthGroomSiblingLinkageRecipe.isViable(new RecordPair(birth_record, marriage_record, 0.0));
    }

    private boolean brideBrideSiblingLinkViable(final int marriage_day1, final int marriage_month1, final int marriage_year1, final String age_or_date_of_birth1, final int marriage_day2, final int marriage_month2, final int marriage_year2, final String age_or_date_of_birth2) {

        final LXP marriage_record1 = makeMarriage(marriage_day1, marriage_month1, marriage_year1, age_or_date_of_birth1, true, "1");
        final LXP marriage_record2 = makeMarriage(marriage_day2, marriage_month2, marriage_year2, age_or_date_of_birth2, true, "2");

        return BrideBrideSiblingLinkageRecipe.isViable(new RecordPair(marriage_record1, marriage_record2, 0.0));
    }

    private boolean brideBrideSiblingLinkViableWithInvalidData() {

        final LXP marriage_record1 = makeInvalidMarriage("1");
        final LXP marriage_record2 = makeInvalidMarriage("2");

        return BrideBrideSiblingLinkageRecipe.isViable(new RecordPair(marriage_record1, marriage_record2, 0.0));
    }

    private boolean brideGroomSiblingLinkViable(final int marriage_day1, final int marriage_month1, final int marriage_year1, final String age_or_date_of_birth1, final int marriage_day2, final int marriage_month2, final int marriage_year2, final String age_or_date_of_birth2) {

        final LXP marriage_record1 = makeMarriage(marriage_day1, marriage_month1, marriage_year1, age_or_date_of_birth1, true, "1");
        final LXP marriage_record2 = makeMarriage(marriage_day2, marriage_month2, marriage_year2, age_or_date_of_birth2, false, "2");

        return BrideGroomSiblingLinkageRecipe.isViable(new RecordPair(marriage_record1, marriage_record2, 0.0));
    }

    private boolean brideGroomSiblingLinkViableWithInvalidData() {

        final LXP marriage_record1 = makeInvalidMarriage("1");
        final LXP marriage_record2 = makeInvalidMarriage("2");

        return BrideGroomSiblingLinkageRecipe.isViable(new RecordPair(marriage_record1, marriage_record2, 0.0));
    }

    private boolean deathSiblingLinkViable(final int death_year1, final int age_at_death1, final int death_year2, final int age_at_death2) {

        final LXP death_record1 = makeDeath(death_year1, age_at_death1);
        final LXP death_record2 = makeDeath(death_year2, age_at_death2);

        return DeathSiblingLinkageRecipe.isViable(new RecordPair(death_record1, death_record2, 0.0));
    }

    private boolean deathDeathSiblingLinkViableWithInvalidData() {

        final LXP death_record1 = makeInvalidDeath();
        final LXP death_record2 = makeInvalidDeath();

        return DeathSiblingLinkageRecipe.isViable(new RecordPair(death_record1, death_record2, 0.0));
    }

    private boolean deathBrideIdentityLinkViable(final int death_year, final int marriage_year) {

        final LXP death_record = makeDeath(death_year);
        final LXP marriage_record = makeMarriage(marriage_year);

        return DeathBrideIdentityLinkageRecipe.isViable(new RecordPair(death_record, marriage_record, 0.0));
    }

    private boolean deathBrideIdentityLinkViableWithInvalidData() {

        final LXP death_record = makeInvalidDeath();
        final LXP marriage_record = makeInvalidMarriage("1");

        return DeathBrideIdentityLinkageRecipe.isViable(new RecordPair(death_record, marriage_record, 0.0));
    }
    private boolean deathGroomIdentityLinkViable(final int death_year, final int marriage_year) {

        final LXP death_record = makeDeath(death_year);
        final LXP marriage_record = makeMarriage(marriage_year);

        return DeathGroomIdentityLinkageRecipe.isViable(new RecordPair(death_record, marriage_record, 0.0));
    }

    private boolean deathGroomIdentityLinkViableWithInvalidData() {

        final LXP death_record = makeInvalidDeath();
        final LXP marriage_record = makeInvalidMarriage("1");

        return DeathGroomIdentityLinkageRecipe.isViable(new RecordPair(death_record, marriage_record, 0.0));
    }

    private boolean birthGroomIdentityLinkViableWithInvalidData() {

        final LXP birth_record = makeInvalidBirth();
        final LXP marriage_record = makeInvalidMarriage("1");

        return BirthGroomIdentityLinkageRecipe.isViable(new RecordPair(birth_record, marriage_record, 0.0));
    }

    private boolean groomGroomSiblingLinkViable(final int marriage_day1, final int marriage_month1, final int marriage_year1, final String age_or_date_of_birth1, final int marriage_day2, final int marriage_month2, final int marriage_year2, final String age_or_date_of_birth2) {

        final LXP marriage_record1 = makeMarriage(marriage_day1, marriage_month1, marriage_year1, age_or_date_of_birth1, false, "1");
        final LXP marriage_record2 = makeMarriage(marriage_day2, marriage_month2, marriage_year2, age_or_date_of_birth2, false, "2");

        return GroomGroomSiblingLinkageRecipe.isViable(new RecordPair(marriage_record1, marriage_record2, 0.0));
    }

    private boolean groomGroomSiblingLinkViableWithInvalidData() {

        final LXP marriage_record1 = makeInvalidMarriage("1");
        final LXP marriage_record2 = makeInvalidMarriage("2");

        return GroomGroomSiblingLinkageRecipe.isViable(new RecordPair(marriage_record1, marriage_record2, 0.0));
    }

    private Birth makeBirth(final int birth_year) {

        final Birth record = new Birth();

        for (int i = 0; i < Birth.BIRTH_YEAR; i++) {
            record.put(i, "");
        }

        record.put(Birth.BIRTH_YEAR, String.valueOf(birth_year));
        return record;
    }

    private Birth makeBirth(final int birth_day, final int birth_month, final int birth_year) {

        final Birth record = makeBirth(birth_year);

        record.put(Birth.BIRTH_DAY, String.valueOf(birth_day));
        record.put(Birth.BIRTH_MONTH, String.valueOf(birth_month));
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

    private Death makeDeath(final int death_year) {

        final Death record = new Death();

        for (int i = 0; i < Death.DEATH_YEAR; i++) {
            record.put(i, "");
        }

        record.put(Death.DEATH_YEAR, String.valueOf(death_year));
        return record;
    }

    private Death makeDeath(final int death_year, final int age_at_death) {

        final Death record = makeDeath(death_year);

        record.put(Death.AGE_AT_DEATH, String.valueOf(age_at_death));
        return record;
    }

    private Death makeDeath(final int death_year, final int age_at_death, final String date_of_birth) {

        final Death record = makeDeath(death_year, age_at_death);

        record.put(Death.DATE_OF_BIRTH, date_of_birth);
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

    private Marriage makeMarriage(final int marriage_day, final int marriage_month, final int marriage_year, String age_or_date_of_birth, boolean spouse_is_bride, String id) {

        final Marriage record = new Marriage();

        for (int i = 0; i < Marriage.MARRIAGE_DAY; i++) {
            record.put(i, "");
        }

        record.put(Marriage.STANDARDISED_ID, id);
        record.put(Marriage.MARRIAGE_DAY, String.valueOf(marriage_day));
        record.put(Marriage.MARRIAGE_MONTH, String.valueOf(marriage_month));
        record.put(Marriage.MARRIAGE_YEAR, String.valueOf(marriage_year));
        record.put((spouse_is_bride ? Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH : Marriage.GROOM_AGE_OR_DATE_OF_BIRTH), age_or_date_of_birth);

        return record;
    }

    private Marriage makeInvalidMarriage(String id) {

        final Marriage record = new Marriage();

        for (int i = 0; i < Marriage.MARRIAGE_DAY; i++) {
            record.put(i, "");
        }

        record.put(Marriage.STANDARDISED_ID, id);
        record.put(Marriage.MARRIAGE_DAY, "Unknown");
        record.put(Marriage.MARRIAGE_MONTH, "Unknown");
        record.put(Marriage.MARRIAGE_YEAR, "Unknown");
        record.put(Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH, "Unknown");
        record.put(Marriage.GROOM_AGE_OR_DATE_OF_BIRTH, "Unknown");

        return record;
    }
}
