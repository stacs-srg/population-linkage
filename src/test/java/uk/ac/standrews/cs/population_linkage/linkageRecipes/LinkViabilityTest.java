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
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import org.junit.jupiter.api.Test;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_records.Normalisation;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class LinkViabilityTest {

    @Test
    public void birthBrideIdentityViability() {

        // All consistent.
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1, 7, 1920, "20"));
        assertTrue(birthBrideIdentityLinkViable(2, 2, 1900, 2, 8, 1920, "01/01/1900"));

        // Slight differences between ages derived from different records.
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1, 7, 1920, "19"));
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1, 7, 1920, "01/01/1901"));
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1, 7, 1920, "21"));
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1, 7, 1920, "01/01/1899"));

        // Significant differences between ages derived from different records, but within acceptable bounds.
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1, 7, 1920, "24"));
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1, 7, 1930, "26"));

        // Significant differences between ages derived from different records, exceeding acceptable bounds.
        assertFalse(birthBrideIdentityLinkViable(1, 1, 1910, 1, 7, 1920, "20"));
        assertFalse(birthBrideIdentityLinkViable(1, 1, 1900, 1, 7, 1930, "25"));
        assertFalse(birthBrideIdentityLinkViable(1, 1, 1920, 1, 7, 1910, "25"));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(birthBrideIdentityLinkViable(1, 1, 1900, 1, 7, 1710, "--/--/"));
        assertTrue(birthBrideIdentityLinkViableWithInvalidData());
    }

    @Test
    public void birthBrideSiblingViability() {

        // All consistent.
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1, 7, 1920, "20"));
        assertTrue(birthBrideSiblingLinkViable(2, 2, 1900, 2, 8, 1920, "30"));
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1, 7, 1910, "20"));
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1, 7, 1899, "20"));

        // Consistent, with full or partial date of birth on marriage record.
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1, 7, 1910, "--/--/1890"));
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1, 7, 1910, "29/02/1890"));

        // Twins.
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1, 7, 1910, "01/01/1900"));

        // Rapid succession.
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1, 7, 1910, "01/10/1900"));

        // Significant difference between sibling ages, but within acceptable bounds.
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1, 7, 1920, "60"));

        // Significant difference between sibling ages, exceeding acceptable bounds.
        assertFalse(birthBrideSiblingLinkViable(1, 1, 1900, 1, 7, 1920, "62"));
        assertFalse(birthBrideSiblingLinkViable(1, 1, 1800, 1, 7, 1920, "20"));
        assertFalse(birthBrideSiblingLinkViable(1, 1, 1900, 1, 7, 1910, "--/--/1790"));

        // Don't check for implausibly small gap between non-twin sibling births, since don't have enough precision where age rather than date of birth recorded on marriage record.

        // Treat as viable if necessary data missing or invalid.
        assertTrue(birthBrideSiblingLinkViable(1, 1, 1900, 1, 7, 1710, "--/--/"));
        assertTrue(birthBrideSiblingLinkViableWithInvalidData());
    }

    @Test
    public void birthDeathIdentityViability() {

        // All consistent.
        assertTrue(birthDeathIdentityLinkViable(1900, 1940, 40, "01/01/1900"));

        // Dates of birth on birth and death records slightly different.
        assertTrue(birthDeathIdentityLinkViable(1900, 1940, 38, "01/01/1902"));

        // Dates of birth on birth and death records significantly different.
        assertFalse(birthDeathIdentityLinkViable(1900, 1940, 35, "01/01/1905"));

        // Born after death.
        assertFalse(birthDeathIdentityLinkViable(1941, 1940, 40, "01/01/1900"));

        // Too old at death.
        assertFalse(birthDeathIdentityLinkViable(1841, 1970, 40, "01/01/1930"));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(birthDeathIdentityLinkViableWithInvalidData());
    }

    @Test
    public void birthDeathSiblingViability() {

        // Small sibling age difference.
        assertTrue(birthDeathSiblingLinkViable(2, 2, 1901, 1950, 48, "01/01/1902"));
        assertTrue(birthDeathSiblingLinkViable(1, 1, 1900, 1950, 52, "01/01/1898"));

        // Acceptably large sibling age difference.
        assertTrue(birthDeathSiblingLinkViable(1, 1, 1900, 1990, 50, "01/01/1940"));
        assertTrue(birthDeathSiblingLinkViable(1, 1, 1900, 1910, 50, "01/01/1860"));

        // Twins.
        assertTrue(birthDeathSiblingLinkViable(1, 1, 1900, 1950, 50, "01/01/1900"));

        // Rapid succession.
        assertTrue(birthDeathSiblingLinkViable(1, 9, 1900, 1950, 50, "30/06/1901"));

        // Sibling age difference too high.
        assertFalse(birthDeathSiblingLinkViable(1, 1, 1900, 1991, 50, "01/01/1941"));
        assertFalse(birthDeathSiblingLinkViable(1, 1, 1900, 1909, 50, "01/01/1859"));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(birthDeathSiblingLinkViableWithInvalidData());
    }

    @Test
    public void birthFatherIdentityViability() {

        // Unsurprising father age at birth.
        assertTrue(birthFatherIdentityLinkViable(1920, 1940));

        // Extremes of plausible father age at birth.
        assertTrue(birthFatherIdentityLinkViable(1920, 1935));
        assertTrue(birthFatherIdentityLinkViable(1920, 1990));

        // Father born after birth.
        assertFalse(birthFatherIdentityLinkViable(1940, 1920));

        // Father too young.
        assertFalse(birthFatherIdentityLinkViable(1920, 1934));

        // Father too old.
        assertFalse(birthFatherIdentityLinkViable(1920, 1991));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(birthFatherIdentityLinkViableWithInvalidData());
    }

    @Test
    public void birthGroomIdentityViability() {

        // All consistent.
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1, 7, 1920, "20"));
        assertTrue(birthGroomIdentityLinkViable(2, 2, 1900, 2, 8, 1920, "01/01/1900"));

        // Slight differences between ages derived from different records.
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1, 7, 1920, "19"));
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1, 7, 1920, "01/01/1901"));
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1, 7, 1920, "21"));
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1, 7, 1920, "01/01/1899"));

        // Significant differences between ages derived from different records, but within acceptable bounds.
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1, 7, 1920, "24"));
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1, 7, 1930, "26"));

        // Significant differences between ages derived from different records, exceeding acceptable bounds.
        assertFalse(birthGroomIdentityLinkViable(1, 1, 1910, 1, 7, 1920, "20"));
        assertFalse(birthGroomIdentityLinkViable(1, 1, 1900, 1, 7, 1930, "25"));
        assertFalse(birthGroomIdentityLinkViable(1, 1, 1920, 1, 7, 1910, "25"));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(birthGroomIdentityLinkViable(1, 1, 1900, 1, 7, 1710, "--/--/"));
        assertTrue(birthGroomIdentityLinkViableWithInvalidData());
    }

    @Test
    public void birthGroomSiblingViability() {

        // All consistent.
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1, 7, 1920, "20"));
        assertTrue(birthGroomSiblingLinkViable(2, 2, 1900, 2, 8, 1920, "30"));
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1, 7, 1910, "20"));
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1, 7, 1899, "20"));

        // Consistent, with full or partial date of birth on marriage record.
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1, 7, 1910, "--/--/1890"));
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1, 7, 1910, "29/02/1890"));

        // Twins.
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1, 7, 1910, "01/01/1900"));

        // Rapid succession.
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1, 7, 1910, "01/10/1900"));

        // Significant difference between sibling ages, but within acceptable bounds.
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1, 7, 1920, "60"));

        // Significant difference between sibling ages, exceeding acceptable bounds.
        assertFalse(birthGroomSiblingLinkViable(1, 1, 1900, 1, 7, 1920, "62"));
        assertFalse(birthGroomSiblingLinkViable(1, 1, 1800, 1, 7, 1920, "20"));
        assertFalse(birthGroomSiblingLinkViable(1, 1, 1900, 1, 7, 1910, "--/--/1790"));

        // Don't check for implausibly small gap between non-twin sibling births, since don't have enough precision where age rather than date of birth recorded on marriage record.

        // Treat as viable if necessary data missing or invalid.
        assertTrue(birthGroomSiblingLinkViable(1, 1, 1900, 1, 7, 1710, "--/--/"));
        assertTrue(birthGroomSiblingLinkViableWithInvalidData());
    }

    @Test
    public void birthMotherIdentityViability() {

        // Unsurprising mother age at birth.
        assertTrue(birthMotherIdentityLinkViable(1920, 1940));

        // Extremes of plausible mother age at birth.
        assertTrue(birthMotherIdentityLinkViable(1920, 1935));
        assertTrue(birthMotherIdentityLinkViable(1920, 1970));

        // Mother born after birth.
        assertFalse(birthMotherIdentityLinkViable(1940, 1920));

        // Mother too young.
        assertFalse(birthMotherIdentityLinkViable(1920, 1934));

        // Mother too old.
        assertFalse(birthMotherIdentityLinkViable(1920, 1971));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(birthMotherIdentityLinkViableWithInvalidData());
    }

    @Test
    public void birthParentsMarriageIdentityViability() {

        // Birth 5 years after marriage.
        assertTrue(birthParentsMarriageIdentityLinkViable(1, 1, 1915, 1, 7, 1910));

        // Birth shortly after marriage.
        assertTrue(birthParentsMarriageIdentityLinkViable(2, 2, 1915, 2, 8, 1914));

        // Birth 2 years before marriage.
        assertTrue(birthParentsMarriageIdentityLinkViable(1, 1, 1915, 1, 7, 1917));

        // Birth 60 years after marriage.
        assertFalse(birthParentsMarriageIdentityLinkViable(1, 1, 1975, 1, 7, 1914));

        // Birth 10 years before marriage.
        assertFalse(birthParentsMarriageIdentityLinkViable(1, 1, 1975, 1, 7, 1985));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(birthParentsMarriageIdentityLinkViableWithInvalidData());
    }

    @Test
    public void birthSiblingViability() {

        // Siblings one year apart.
        assertTrue(birthSiblingLinkViable(1920, 1921));
        assertTrue(birthSiblingLinkViable(1921, 1920));

        // Twins or non-twins with short gap.
        assertTrue(birthSiblingLinkViable(1921, 1921));

        // Significant gap between siblings, but within acceptable bounds.
        assertTrue(birthSiblingLinkViable(1920, 1960));

        // Twins.
        assertTrue(birthSiblingLinkViable("01/01/1900", "01/01/1900"));

        // Rapid succession.
        assertTrue(birthSiblingLinkViable("01/10/1900", "01/08/1901"));

        // Gap between siblings beyond acceptable bounds.
        assertFalse(birthSiblingLinkViable(1920, 1980));
        assertFalse(birthSiblingLinkViable(1961, 1920));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(birthBirthSiblingLinkViableWithInvalidData());
    }

    @Test
    public void brideBrideIdentityViability() {

        // All consistent.
        assertTrue(brideBrideIdentityLinkViable(1, 1, 1900, "25", 1, 7, 1920, "45"));
        assertTrue(brideBrideIdentityLinkViable(1, 1, 1901, "25", 1, 7, 1921, "03/07/1876"));
        assertTrue(brideBrideIdentityLinkViable(2, 2, 1900, "01/07/1875", 2, 8, 1920, "03/08/1875"));

        // Slight differences between ages derived from different records.
        assertTrue(brideBrideIdentityLinkViable(1, 1, 1900, "25", 1, 7, 1920, "44"));
        assertTrue(brideBrideIdentityLinkViable(1, 1, 1900, "25", 1, 7, 1920, "03/07/1876"));
        assertTrue(brideBrideIdentityLinkViable(2, 2, 1900, "01/07/1875", 2, 8, 1920, "01/07/1876"));

        // Significant differences between ages derived from different records, but within acceptable bounds.
        assertTrue(brideBrideIdentityLinkViable(1, 1, 1900, "29", 1, 7, 1920, "45"));
        assertTrue(brideBrideIdentityLinkViable(1, 1, 1900, "28", 1, 7, 1920, "03/07/1876"));
        assertTrue(brideBrideIdentityLinkViable(2, 2, 1900, "01/07/1875", 2, 8, 1920, "01/07/1879"));

        // Significant differences between ages derived from different records, exceeding acceptable bounds.
        assertFalse(brideBrideIdentityLinkViable(1, 1, 1900, "30", 1, 7, 1920, "45"));
        assertFalse(brideBrideIdentityLinkViable(1, 1, 1900, "29", 1, 7, 1920, "03/07/1876"));
        assertFalse(brideBrideIdentityLinkViable(2, 2, 1900, "01/07/1875", 2, 8, 2000, "01/07/1975"));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(brideBrideIdentityLinkViable(1, 1, 1900, "25", 1, 7, 1710, "--/--/"));
        assertTrue(brideBrideIdentityLinkViableWithInvalidData());
    }

    @Test
    public void brideBrideSiblingViability() {

        // Births one year apart.
        assertTrue(brideBrideSiblingLinkViable(1, 7, 1920, "25", 31, 12, 1925, "31"));

        // Births seven years apart.
        assertTrue(brideBrideSiblingLinkViable(1, 7, 1920, "25", 31, 12, 1925, "22"));
        assertTrue(brideBrideSiblingLinkViable(31, 12, 1925, "22", 1, 7, 1920, "25"));
        assertTrue(brideBrideSiblingLinkViable(1, 7, 1920, "1/1/1895", 31, 12, 1925, "30/11/1903"));

        // Births 40 years apart.
        assertTrue(brideBrideSiblingLinkViable(1, 7, 1920, "25", 1, 7, 1960, "25"));

        // Twins, with a minor discrepancy in date of birth.
        assertTrue(brideBrideSiblingLinkViable(1, 1, 1900, "01/04/1870", 7, 10, 1910, "03/04/1870"));

        // Rapid succession.
        assertTrue(brideBrideSiblingLinkViable(1, 1, 1900, "01/04/1870", 7, 10, 1910, "01/02/1871"));

        // Gap between siblings beyond acceptable bounds (75 years, 80 years, 41 years).
        assertFalse(brideBrideSiblingLinkViable(1, 1, 1925, "25", 1, 1, 2000, "25"));
        assertFalse(brideBrideSiblingLinkViable(1, 1, 1920, "1/1/1895", 1, 1, 2000, "30/11/1975"));
        assertFalse(brideBrideSiblingLinkViable(1, 7, 1920, "25", 1, 7, 1961, "25"));

        // Implausibly small gap between non-twin sibling births.
        assertTrue(brideBrideSiblingLinkViable(1, 1, 1900, "01/06/1870", 7, 10, 1910, "31/12/1870"));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(brideBrideSiblingLinkViableWithInvalidData());
    }

    @Test
    public void brideGroomSiblingViability() {

        // Births one year apart.
        assertTrue(brideGroomSiblingLinkViable(1, 7, 1920, "25", 31, 12, 1925, "31"));

        // Births seven years apart.
        assertTrue(brideGroomSiblingLinkViable(1, 7, 1920, "25", 31, 12, 1925, "22"));
        assertTrue(brideGroomSiblingLinkViable(1, 7, 1920, "25", 1, 7, 1960, "25"));
        assertTrue(brideGroomSiblingLinkViable(1, 7, 1920, "1/1/1895", 31, 12, 1925, "30/11/1903"));

        // Births 40 years apart.
        assertTrue(brideGroomSiblingLinkViable(31, 12, 1925, "22", 1, 7, 1960, "25"));

        // Twins, with a minor discrepancy in date of birth.
        assertTrue(brideGroomSiblingLinkViable(1, 1, 1900, "01/04/1870", 7, 10, 1910, "03/04/1870"));

        // Rapid succession.
        assertTrue(brideGroomSiblingLinkViable(1, 1, 1900, "01/04/1870", 7, 10, 1910, "01/02/1871"));

        // Gap between siblings beyond acceptable bounds (75 years, 80 years, 41 years).
        assertFalse(brideGroomSiblingLinkViable(1, 1, 1925, "25", 1, 1, 2000, "25"));
        assertFalse(brideGroomSiblingLinkViable(1, 1, 1920, "1/1/1895", 1, 1, 2000, "30/11/1975"));
        assertFalse(brideGroomSiblingLinkViable(1, 7, 1920, "25", 1, 7, 1961, "25"));

        // Implausibly small gap between non-twin sibling births.
        assertTrue(brideGroomSiblingLinkViable(1, 1, 1900, "01/06/1870", 7, 10, 1910, "31/12/1870"));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(brideGroomSiblingLinkViableWithInvalidData());
    }

    @Test
    public void brideMarriageParentsMarriageIdentityViability() {

        // Bride marriage 25 years after parents' marriage.
        assertTrue(brideMarriageParentsMarriageIdentityLinkViable(1, 1, 1915, 1, 7, 1940));

        // Bride marriage 15 years after parents' marriage.
        assertTrue(brideMarriageParentsMarriageIdentityLinkViable(2, 2, 1915, 2, 8, 1930));

        // Bride marriage 10 years after parents' marriage.
        assertFalse(brideMarriageParentsMarriageIdentityLinkViable(1, 1, 1915, 1, 7, 1925));

        // Bride marriage same year as parents' marriage.
        assertFalse(brideMarriageParentsMarriageIdentityLinkViable(1, 1, 1915, 1, 7, 1915));

        // Bride marriage before parents' marriage.
        assertFalse(brideMarriageParentsMarriageIdentityLinkViable(1, 1, 1915, 1, 7, 1910));

        // Bride marriage 110 years after parents' marriage.
        assertFalse(brideMarriageParentsMarriageIdentityLinkViable(1, 1, 1905, 1, 7, 2015));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(brideMarriageParentsMarriageIdentityLinkViableWithInvalidData());
    }

    @Test
    public void deathBrideIdentityViability() {

        // All reasonably consistent.
        assertTrue(deathBrideIdentityLinkViable(1970, 70, "01/07/1900", 5, 5, 1925, "25"));
        assertTrue(deathBrideIdentityLinkViable(1971, 71, "02/07/1900", 6, 6, 1925, "01/06/1900"));

        // Marriage in same year as death.
        assertTrue(deathBrideIdentityLinkViable(1970, 70, "01/07/1900", 5, 5, 1970, "01/06/1900"));

        // Marriage year before death.
        assertTrue(deathBrideIdentityLinkViable(1970, 70, "01/07/1900", 5, 5, 1969, "01/06/1900"));

        // Marriage after death.
        assertFalse(deathBrideIdentityLinkViable(1970, 70, "01/07/1900", 5, 5, 1971, "01/06/1900"));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(deathBrideIdentityLinkViableWithInvalidData());
    }

    @Test
    public void deathBrideSiblingViability() {

        // All consistent.
        assertTrue(deathBrideSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1920, "20"));
        assertTrue(deathBrideSiblingLinkViable(1970, 70, "01/07/1900", 2, 8, 1920, "30"));
        assertTrue(deathBrideSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1910, "20"));
        assertTrue(deathBrideSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1899, "20"));

        // Consistent, with full or partial date of birth on marriage record.
        assertTrue(deathBrideSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1910, "--/--/1890"));
        assertTrue(deathBrideSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1910, "29/02/1890"));

        // Twins.
        assertTrue(deathBrideSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1910, "01/07/1900"));

        // Rapid succession.
        assertTrue(deathBrideSiblingLinkViable(1960, 60, "01/01/1900", 1, 7, 1910, "01/10/1900"));

        // Significant difference between sibling ages, but within acceptable bounds.
        assertTrue(deathBrideSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1920, "60"));

        // Significant difference between sibling ages, exceeding acceptable bounds.
        assertFalse(deathBrideSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1920, "61"));
        assertFalse(deathBrideSiblingLinkViable(1970, 70, "01/07/1800", 1, 7, 1920, "20"));
        assertFalse(deathBrideSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1910, "--/--/1790"));

        // Don't check for implausibly small gap between non-twin sibling births, since don't have enough precision where age rather than date of birth recorded on marriage record.

        // Treat as viable if necessary data missing or invalid.
        assertTrue(deathBrideSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1710, "--/--/"));
        assertTrue(deathBrideSiblingLinkViableWithInvalidData());
    }

    @Test
    public void deathGroomIdentityViability() {

        // All reasonably consistent.
        assertTrue(deathGroomIdentityLinkViable(1970, 70, "01/07/1900", 5, 5, 1925, "25"));
        assertTrue(deathGroomIdentityLinkViable(1971, 70, "01/07/1901", 6, 6, 1925, "01/06/1901"));

        // Marriage in same year as death.
        assertTrue(deathGroomIdentityLinkViable(1971, 71, "01/07/1900", 5, 5, 1970, "01/06/1900"));

        // Marriage year before death.
        assertTrue(deathGroomIdentityLinkViable(1970, 70, "01/07/1900", 5, 5, 1969, "01/06/1900"));

        // Marriage after death.
        assertFalse(deathGroomIdentityLinkViable(1970, 70, "01/07/1900", 5, 5, 1971, "01/06/1900"));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(deathGroomIdentityLinkViableWithInvalidData());
    }

    @Test
    public void deathGroomSiblingViability() {

        // All consistent.
        assertTrue(deathGroomSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1920, "20"));
        assertTrue(deathGroomSiblingLinkViable(1970, 70, "01/07/1900", 2, 8, 1920, "30"));
        assertTrue(deathGroomSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1910, "20"));
        assertTrue(deathGroomSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1899, "20"));

        // Consistent, with full or partial date of birth on marriage record.
        assertTrue(deathGroomSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1910, "--/--/1890"));
        assertTrue(deathGroomSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1910, "29/02/1890"));

        // Twins.
        assertTrue(deathGroomSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1910, "01/07/1900"));

        // Rapid succession.
        assertTrue(deathGroomSiblingLinkViable(1960, 60, "01/01/1900", 1, 7, 1910, "01/10/1900"));

        // Significant difference between sibling ages, but within acceptable bounds.
        assertTrue(deathGroomSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1920, "60"));

        // Significant difference between sibling ages, exceeding acceptable bounds.
        assertFalse(deathGroomSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1920, "61"));
        assertFalse(deathGroomSiblingLinkViable(1970, 70, "01/07/1800", 1, 7, 1920, "20"));
        assertFalse(deathGroomSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1910, "--/--/1790"));

        // Don't check for implausibly small gap between non-twin sibling births, since don't have enough precision where age rather than date of birth recorded on marriage record.

        // Treat as viable if necessary data missing or invalid.
        assertTrue(deathGroomSiblingLinkViable(1970, 70, "01/07/1900", 1, 7, 1710, "--/--/"));
        assertTrue(deathGroomSiblingLinkViableWithInvalidData());
    }

    @Test
    public void deathSiblingViability() {

        // Twins or non-twins with short gap.
        assertTrue(deathSiblingLinkViable(1920, 50, 1930, 60));

        // Births one year apart.
        assertTrue(deathSiblingLinkViable(1920, 51, 1930, 60));

        // Births 10 years apart.
        assertTrue(deathSiblingLinkViable(1930, 50, 1920, 50));

        // Births 40 years apart.
        assertTrue(deathSiblingLinkViable(1930, 50, 1970, 50));

        // Twins.
        assertTrue(deathSiblingLinkViable("01/01/1900", "01/01/1900"));

        // Rapid succession.
        assertTrue(deathSiblingLinkViable("01/10/1900", "01/08/1901"));

        // Gap between siblings beyond acceptable bounds (90 years, 51 years).
        assertFalse(deathSiblingLinkViable(1920, 50, 1980, 20));
        assertFalse(deathSiblingLinkViable(1930, 50, 1971, 50));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(deathSiblingLinkViableWithInvalidData());
    }

    @Test
    public void groomGroomIdentityViability() {

        // All consistent.
        assertTrue(groomGroomIdentityLinkViable(1, 1, 1900, "25", 1, 7, 1920, "45"));
        assertTrue(groomGroomIdentityLinkViable(1, 1, 1901, "26", 1, 7, 1920, "03/07/1875"));
        assertTrue(groomGroomIdentityLinkViable(2, 2, 1900, "01/07/1875", 2, 8, 1920, "03/08/1875"));

        // Slight differences between ages derived from different records.
        assertTrue(groomGroomIdentityLinkViable(1, 1, 1900, "25", 1, 7, 1920, "44"));
        assertTrue(groomGroomIdentityLinkViable(1, 1, 1900, "25", 1, 7, 1920, "03/07/1876"));
        assertTrue(groomGroomIdentityLinkViable(2, 2, 1900, "01/07/1875", 2, 8, 1920, "01/07/1876"));

        // Significant differences between ages derived from different records, but within acceptable bounds.
        assertTrue(groomGroomIdentityLinkViable(1, 1, 1900, "29", 1, 7, 1920, "45"));
        assertTrue(groomGroomIdentityLinkViable(1, 1, 1900, "28", 1, 7, 1920, "03/07/1876"));
        assertTrue(groomGroomIdentityLinkViable(2, 2, 1900, "01/07/1875", 2, 8, 1920, "01/07/1879"));

        // Significant differences between ages derived from different records, exceeding acceptable bounds.
        assertFalse(groomGroomIdentityLinkViable(1, 1, 1900, "30", 1, 7, 1920, "45"));
        assertFalse(groomGroomIdentityLinkViable(1, 1, 1900, "29", 1, 7, 1920, "03/07/1876"));
        assertFalse(groomGroomIdentityLinkViable(2, 2, 1900, "01/07/1875", 2, 8, 2000, "01/07/1975"));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(groomGroomIdentityLinkViable(1, 1, 1900, "25", 1, 7, 1710, "--/--/"));
        assertTrue(groomGroomIdentityLinkViableWithInvalidData());
    }

    @Test
    public void groomGroomSiblingViability() {

        // Births one year apart.
        assertTrue(groomGroomSiblingLinkViable(1, 7, 1920, "25", 31, 12, 1925, "31"));

        // Births seven years apart.
        assertTrue(groomGroomSiblingLinkViable(1, 7, 1920, "25", 31, 12, 1925, "22"));
        assertTrue(groomGroomSiblingLinkViable(31, 12, 1925, "22", 1, 7, 1920, "25"));
        assertTrue(groomGroomSiblingLinkViable(1, 7, 1920, "1/1/1895", 31, 12, 1925, "30/11/1903"));

        // Births 40 years apart.
        assertTrue(groomGroomSiblingLinkViable(1, 7, 1920, "25", 1, 7, 1960, "25"));

        // Twins, with a minor discrepancy in date of birth.
        assertTrue(groomGroomSiblingLinkViable(1, 1, 1900, "01/04/1870", 7, 10, 1910, "03/04/1870"));

        // Rapid succession.
        assertTrue(groomGroomSiblingLinkViable(1, 1, 1900, "01/04/1870", 7, 10, 1910, "01/02/1871"));

        // Gap between siblings beyond acceptable bounds (75 years, 80 years, 41 years).
        assertFalse(groomGroomSiblingLinkViable(1, 1, 1925, "25", 1, 1, 2000, "25"));
        assertFalse(groomGroomSiblingLinkViable(1, 1, 1920, "1/1/1895", 1, 1, 2000, "30/11/1975"));
        assertFalse(groomGroomSiblingLinkViable(1, 7, 1920, "25", 1, 7, 1961, "25"));

        // Implausibly small gap between non-twin sibling births.
        assertTrue(groomGroomSiblingLinkViable(1, 1, 1900, "01/06/1870", 7, 10, 1910, "31/12/1870"));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(groomGroomSiblingLinkViableWithInvalidData());
    }

    @Test
    public void groomMarriageParentsMarriageIdentityViability() {

        // Bride marriage 25 years after parents' marriage.
        assertTrue(groomMarriageParentsMarriageIdentityLinkViable(1, 1, 1915, 1, 7, 1940));

        // Bride marriage 15 years after parents' marriage.
        assertTrue(groomMarriageParentsMarriageIdentityLinkViable(2, 2, 1915, 2, 8, 1930));

        // Bride marriage 10 years after parents' marriage.
        assertFalse(groomMarriageParentsMarriageIdentityLinkViable(1, 1, 1915, 1, 7, 1925));

        // Bride marriage same year as parents' marriage.
        assertFalse(groomMarriageParentsMarriageIdentityLinkViable(1, 1, 1915, 1, 7, 1915));

        // Bride marriage before parents' marriage.
        assertFalse(groomMarriageParentsMarriageIdentityLinkViable(1, 1, 1915, 1, 7, 1910));

        // Bride marriage 110 years after parents' marriage.
        assertFalse(groomMarriageParentsMarriageIdentityLinkViable(1, 1, 1905, 1, 7, 2015));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(groomMarriageParentsMarriageIdentityLinkViableWithInvalidData());
    }

    @Test
    public void deathParentsMarriageIdentityViability() {

        // Child death 5 years after marriage, born after marriage.
        assertTrue(deathParentsMarriageIdentityLinkViable(1915, 4, "01/07/1911", 1, 7, 1910));

        // Child death 5 years after marriage, born long before marriage.
        assertFalse(deathParentsMarriageIdentityLinkViable(1915, 65, "01/07/1850", 1, 7, 1910));

        // Child death shortly after marriage, born after marriage.
        assertTrue(deathParentsMarriageIdentityLinkViable(1913, 1, "01/01/1913", 1, 7, 1912));

        // Child death shortly after marriage, born long before marriage.
        assertFalse(deathParentsMarriageIdentityLinkViable(1913, 33, "01/01/1880", 1, 7, 1912));

        // Child death 2 years before marriage, born 4 years before marriage.
        assertTrue(deathParentsMarriageIdentityLinkViable(1910, 2, "01/01/1908", 1, 7, 1912));

        // Child death 2 years before marriage, born 20 years before marriage.
        assertFalse(deathParentsMarriageIdentityLinkViable(1910, 18, "01/01/1892", 1, 7, 1912));

        // Child death 60 years after marriage.
        assertTrue(deathParentsMarriageIdentityLinkViable(1970, 49, "01/07/1921", 1, 7, 1910));

        // Child death 60 years after marriage, born 50 years after marriage.
        assertFalse(deathParentsMarriageIdentityLinkViable(1970, 10, "01/07/1960", 1, 7, 1910));

        // Child death 170 years after marriage.
        assertFalse(deathParentsMarriageIdentityLinkViable(2020, 120, "01/07/1900", 1, 7, 1850));

        // Child death long before marriage.
        assertFalse(deathParentsMarriageIdentityLinkViable(1880, 4, "01/07/1876", 1, 7, 1910));

        // Treat as viable if necessary data missing or invalid.
        assertTrue(deathParentsMarriageIdentityLinkViableWithInvalidData());
    }

    @Test
    public void getBirthDateFromBirthRecord() {

        final LXP record1 = makeBirth(1890);
        final LXP record2 = makeBirth(3, 3, 1890);
        final LXP record3 = makeBirth("03/03/1890");
        final LXP record4 = makeBirth("--/--/1890");

        assertEquals(LocalDate.of(1890, 7, 1), CommonLinkViabilityLogic.getBirthDateFromBirthRecord(record1));
        assertEquals(LocalDate.of(1890, 3, 3), CommonLinkViabilityLogic.getBirthDateFromBirthRecord(record2));
        assertEquals(LocalDate.of(1890, 3, 3), CommonLinkViabilityLogic.getBirthDateFromBirthRecord(record3));
        assertEquals(LocalDate.of(1890, 7, 1), CommonLinkViabilityLogic.getBirthDateFromBirthRecord(record4));
    }

    @Test
    public void getBirthDateFromMarriageRecord() {

        final LXP record1 = makeMarriage(1, 1, 1890, "27", true, "1");
        final LXP record2 = makeMarriage(1, 1, 1904, "05/10/1863", true, "1");
        final LXP record3 = makeMarriage(1, 1, 1890, "27", false, "1");
        final LXP record4 = makeMarriage(1, 1, 1904, "05/10/1863", false, "1");
        final LXP record5 = makeMarriage(1, 1, 1904, "--/--/1863", false, "1");

        assertEquals(LocalDate.of(1863, 7, 1), CommonLinkViabilityLogic.getBirthDateFromMarriageRecord(record1, true));
        assertEquals(LocalDate.of(1863, 10, 5), CommonLinkViabilityLogic.getBirthDateFromMarriageRecord(record2, true));
        assertEquals(LocalDate.of(1863, 7, 1), CommonLinkViabilityLogic.getBirthDateFromMarriageRecord(record3, false));
        assertEquals(LocalDate.of(1863, 10, 5), CommonLinkViabilityLogic.getBirthDateFromMarriageRecord(record4, false));
        assertEquals(LocalDate.of(1863, 7, 1), CommonLinkViabilityLogic.getBirthDateFromMarriageRecord(record5, false));
    }

    @Test
    public void getMarriageDateFromMarriageRecord() {

        final LXP record1 = makeMarriage(3, 3, 1890);
        final LXP record2 = makeMarriage(1863);

        assertEquals(LocalDate.of(1890, 3, 3), CommonLinkViabilityLogic.getMarriageDateFromMarriageRecord(record1));
        assertEquals(LocalDate.of(1863, 7, 1), CommonLinkViabilityLogic.getMarriageDateFromMarriageRecord(record2));
    }

    @Test
    public void getBirthDateFromDeathRecord() {

        final LXP record1 = makeDeath("03/03/1890");
        final LXP record2 = makeDeath("--/--/1890");
        final LXP record3 = makeDeath(1923, 65);
        final LXP record4 = makeDeath("--/--/1890", 65);

        assertEquals(LocalDate.of(1890, 3, 3), CommonLinkViabilityLogic.getBirthDateFromDeathRecord(record1));
        assertEquals(LocalDate.of(1890, 7, 1), CommonLinkViabilityLogic.getBirthDateFromDeathRecord(record2));
        assertEquals(LocalDate.of(1858, 7, 1), CommonLinkViabilityLogic.getBirthDateFromDeathRecord(record3));
        assertEquals(LocalDate.of(1890, 7, 1), CommonLinkViabilityLogic.getBirthDateFromDeathRecord(record4));
    }

    @Test
    public void getDeathDateFromDeathRecord() {

        final LXP record1 = makeDeath(3, 3, 1890);
        final LXP record2 = makeDeath(1890);
        final LXP record3 = makeDeath("03/03/1890", 65);
        final LXP record4 = makeDeath("--/--/1890", 65);

        assertEquals(LocalDate.of(1890, 3, 3), CommonLinkViabilityLogic.getDeathDateFromDeathRecord(record1));
        assertEquals(LocalDate.of(1890, 7, 1), CommonLinkViabilityLogic.getDeathDateFromDeathRecord(record2));
        assertEquals(LocalDate.of(1955, 3, 3), CommonLinkViabilityLogic.getDeathDateFromDeathRecord(record3));
        assertEquals(LocalDate.of(1955, 7, 1), CommonLinkViabilityLogic.getDeathDateFromDeathRecord(record4));
    }

    @Test
    public void extractBirthYearFromMarriageRecordWithInvalidDate() {

        assertThrows(NumberFormatException.class, () -> {

            final LXP record = makeMarriage(1, 1, 1904, "05/10/----", false, "1");
            CommonLinkViabilityLogic.getBirthDateFromMarriageRecord(record, false);
        });
    }

    @Test
    public void extractBirthYearFromMarriageRecordWithInvalidDate2() {

        assertThrows(NumberFormatException.class, () -> {

            final LXP record = makeInvalidMarriage("1");
            CommonLinkViabilityLogic.getBirthDateFromMarriageRecord(record, true);
        });
    }

    private boolean birthSiblingLinkViable(final int birth_year1, final int birth_year2) {

        final LXP birth_record1 = makeBirth(birth_year1);
        final LXP birth_record2 = makeBirth(birth_year2);

        return BirthSiblingLinkageRecipe.isViable(birth_record1, birth_record2);
    }

    private boolean birthSiblingLinkViable(final String date_of_birth1, final String date_of_birth2) {

        final LXP birth_record1 = makeBirth(date_of_birth1);
        final LXP birth_record2 = makeBirth(date_of_birth2);

        return BirthSiblingLinkageRecipe.isViable(birth_record1, birth_record2);
    }

    private boolean birthBirthSiblingLinkViableWithInvalidData() {

        final LXP birth_record1 = makeInvalidBirth();
        final LXP birth_record2 = makeInvalidBirth();

        return BirthSiblingLinkageRecipe.isViable(birth_record1, birth_record2);
    }

    private boolean birthDeathIdentityLinkViable(final int birth_year, final int death_year, int age_at_death, String date_of_birth) {

        final LXP birth_record = makeBirth(birth_year);
        final LXP death_record = makeDeath(death_year, age_at_death, date_of_birth);

        return BirthDeathIdentityLinkageRecipe.isViable(birth_record, death_record);
    }

    private boolean birthDeathIdentityLinkViableWithInvalidData() {

        final LXP birth_record = makeInvalidBirth();
        final LXP death_record = makeInvalidDeath();

        return BirthDeathIdentityLinkageRecipe.isViable(birth_record, death_record);
    }

    private boolean birthFatherIdentityLinkViable(final int birth_year1, final int birth_year2) {

        final LXP birth_record1 = makeBirth(birth_year1);
        final LXP birth_record2 = makeBirth(birth_year2);

        return BirthFatherIdentityLinkageRecipe.isViable(birth_record1, birth_record2);
    }

    private boolean birthFatherIdentityLinkViableWithInvalidData() {

        final LXP birth_record1 = makeInvalidBirth();
        final LXP birth_record2 = makeInvalidBirth();

        return BirthFatherIdentityLinkageRecipe.isViable(birth_record1, birth_record2);
    }

    private boolean birthMotherIdentityLinkViable(final int birth_year1, final int birth_year2) {

        final LXP birth_record1 = makeBirth(birth_year1);
        final LXP birth_record2 = makeBirth(birth_year2);

        return BirthMotherIdentityLinkageRecipe.isViable(birth_record1, birth_record2);
    }

    private boolean birthMotherIdentityLinkViableWithInvalidData() {

        final LXP birth_record1 = makeInvalidBirth();
        final LXP birth_record2 = makeInvalidBirth();

        return BirthMotherIdentityLinkageRecipe.isViable(birth_record1, birth_record2);
    }

    private boolean birthBrideIdentityLinkViable(final int birth_day, final int birth_month, final int birth_year, final int marriage_day, final int marriage_month, final int marriage_year, final String marriage_age_or_date_of_birth) {

        final LXP birth_record = makeBirth(birth_day, birth_month, birth_year);
        final LXP marriage_record = makeMarriage(marriage_day, marriage_month, marriage_year, marriage_age_or_date_of_birth, true, "1");

        return BirthBrideIdentityLinkageRecipe.isViable(birth_record, marriage_record);
    }

    private boolean birthBrideIdentityLinkViableWithInvalidData() {

        final LXP birth_record = makeInvalidBirth();
        final LXP marriage_record = makeInvalidMarriage("1");

        return BirthBrideIdentityLinkageRecipe.isViable(birth_record, marriage_record);
    }

    private boolean birthBrideSiblingLinkViable(final int birth_day, final int birth_month, final int birth_year, final int marriage_day, final int marriage_month, final int marriage_year, final String marriage_age_or_date_of_birth) {

        final LXP birth_record = makeBirth(birth_day, birth_month, birth_year);
        final LXP marriage_record = makeMarriage(marriage_day, marriage_month, marriage_year, marriage_age_or_date_of_birth, true, "1");

        return BirthBrideSiblingLinkageRecipe.isViable(birth_record, marriage_record);
    }

    private boolean birthBrideSiblingLinkViableWithInvalidData() {

        final LXP marriage_record = makeInvalidMarriage("1");
        final LXP birth_record = makeInvalidBirth();

        return BirthBrideSiblingLinkageRecipe.isViable(birth_record, marriage_record);
    }

    private boolean birthDeathSiblingLinkViable(final int birth_day, final int birth_month, final int birth_year, final int death_year, final int age_at_death, final String date_of_birth) {

        final LXP birth_record = makeBirth(birth_day, birth_month, birth_year);
        final LXP death_record = makeDeath(death_year, age_at_death, date_of_birth);

        return BirthDeathSiblingLinkageRecipe.isViable(birth_record, death_record);
    }

    private boolean birthDeathSiblingLinkViableWithInvalidData() {

        final LXP birth_record = makeInvalidBirth();
        final LXP death_record = makeInvalidDeath();

        return BirthDeathSiblingLinkageRecipe.isViable(birth_record, death_record);
    }

    private boolean birthGroomIdentityLinkViable(final int birth_day, final int birth_month, final int birth_year, final int marriage_day, final int marriage_month, final int marriage_year, final String age_or_date_of_birth) {

        final LXP birth_record = makeBirth(birth_day, birth_month, birth_year);
        final LXP marriage_record = makeMarriage(marriage_day, marriage_month, marriage_year, age_or_date_of_birth, false, "1");

        return BirthGroomIdentityLinkageRecipe.isViable(birth_record, marriage_record);
    }

    private boolean birthGroomSiblingLinkViable(final int birth_day, final int birth_month, final int birth_year, final int marriage_day, final int marriage_month, final int marriage_year, final String marriage_age_or_date_of_birth) {

        final LXP birth_record = makeBirth(birth_day, birth_month, birth_year);
        final LXP marriage_record = makeMarriage(marriage_day, marriage_month, marriage_year, marriage_age_or_date_of_birth, false, "1");

        return BirthGroomSiblingLinkageRecipe.isViable(birth_record, marriage_record);
    }

    private boolean birthGroomSiblingLinkViableWithInvalidData() {

        final LXP birth_record = makeInvalidBirth();
        final LXP marriage_record = makeInvalidMarriage("1");

        return BirthGroomSiblingLinkageRecipe.isViable(birth_record, marriage_record);
    }

    private boolean birthParentsMarriageIdentityLinkViable(final int birth_day, final int birth_month, final int birth_year, final int marriage_day, final int marriage_month, final int marriage_year) {

        final LXP birth_record = makeBirth(birth_day, birth_month, birth_year);
        final LXP marriage_record = makeMarriage(marriage_day, marriage_month, marriage_year, "0", false, "1");

        return BirthParentsMarriageIdentityLinkageRecipe.isViable(birth_record, marriage_record);
    }

    private boolean birthParentsMarriageIdentityLinkViableWithInvalidData() {

        final LXP birth_record = makeInvalidBirth();
        final LXP marriage_record = makeInvalidMarriage("1");

        return BirthParentsMarriageIdentityLinkageRecipe.isViable(birth_record, marriage_record);
    }

    private boolean brideBrideIdentityLinkViable(final int marriage_day1, final int marriage_month1, final int marriage_year1, final String age_or_date_of_birth1, final int marriage_day2, final int marriage_month2, final int marriage_year2, final String age_or_date_of_birth2) {

        final LXP marriage_record1 = makeMarriage(marriage_day1, marriage_month1, marriage_year1, age_or_date_of_birth1, true, "1");
        final LXP marriage_record2 = makeMarriage(marriage_day2, marriage_month2, marriage_year2, age_or_date_of_birth2, true, "2");

        return BrideBrideIdentityLinkageRecipe.isViable(marriage_record1, marriage_record2);
    }

    private boolean brideBrideIdentityLinkViableWithInvalidData() {

        final LXP marriage_record1 = makeInvalidMarriage("1");
        final LXP marriage_record2 = makeInvalidMarriage("2");

        return BrideBrideIdentityLinkageRecipe.isViable(marriage_record1, marriage_record2);
    }

    private boolean brideBrideSiblingLinkViable(final int marriage_day1, final int marriage_month1, final int marriage_year1, final String age_or_date_of_birth1, final int marriage_day2, final int marriage_month2, final int marriage_year2, final String age_or_date_of_birth2) {

        final LXP marriage_record1 = makeMarriage(marriage_day1, marriage_month1, marriage_year1, age_or_date_of_birth1, true, "1");
        final LXP marriage_record2 = makeMarriage(marriage_day2, marriage_month2, marriage_year2, age_or_date_of_birth2, true, "2");

        return BrideBrideSiblingLinkageRecipe.isViable(marriage_record1, marriage_record2);
    }

    private boolean brideBrideSiblingLinkViableWithInvalidData() {

        final LXP marriage_record1 = makeInvalidMarriage("1");
        final LXP marriage_record2 = makeInvalidMarriage("2");

        return BrideBrideSiblingLinkageRecipe.isViable(marriage_record1, marriage_record2);
    }

    private boolean brideGroomSiblingLinkViable(final int marriage_day1, final int marriage_month1, final int marriage_year1, final String age_or_date_of_birth1, final int marriage_day2, final int marriage_month2, final int marriage_year2, final String age_or_date_of_birth2) {

        final LXP marriage_record1 = makeMarriage(marriage_day1, marriage_month1, marriage_year1, age_or_date_of_birth1, true, "1");
        final LXP marriage_record2 = makeMarriage(marriage_day2, marriage_month2, marriage_year2, age_or_date_of_birth2, false, "2");

        return BrideGroomSiblingLinkageRecipe.isViable(marriage_record1, marriage_record2);
    }

    private boolean brideGroomSiblingLinkViableWithInvalidData() {

        final LXP marriage_record1 = makeInvalidMarriage("1");
        final LXP marriage_record2 = makeInvalidMarriage("2");

        return BrideGroomSiblingLinkageRecipe.isViable(marriage_record1, marriage_record2);
    }

    private boolean brideMarriageParentsMarriageIdentityLinkViable(final int marriage_day1, final int marriage_month1, final int marriage_year1, final int marriage_day2, final int marriage_month2, final int marriage_year2) {

        final LXP marriage_record1 = makeMarriage(marriage_day1, marriage_month1, marriage_year1);
        final LXP marriage_record2 = makeMarriage(marriage_day2, marriage_month2, marriage_year2);

        return BrideMarriageParentsMarriageIdentityLinkageRecipe.isViable(marriage_record1, marriage_record2);
    }

    private boolean brideMarriageParentsMarriageIdentityLinkViableWithInvalidData() {

        final LXP marriage_record1 = makeInvalidMarriage("1");
        final LXP marriage_record2 = makeInvalidMarriage("2");

        return BrideMarriageParentsMarriageIdentityLinkageRecipe.isViable(marriage_record1, marriage_record2);
    }

    private boolean deathSiblingLinkViable(final int death_year1, final int age_at_death1, final int death_year2, final int age_at_death2) {

        final LXP death_record1 = makeDeath(death_year1, age_at_death1);
        final LXP death_record2 = makeDeath(death_year2, age_at_death2);

        return DeathSiblingLinkageRecipe.isViable(death_record1, death_record2);
    }

    private boolean deathSiblingLinkViable(final String date_of_birth1, final String date_of_birth2) {

        final LXP death_record1 = makeDeath(date_of_birth1);
        final LXP death_record2 = makeDeath(date_of_birth2);

        return DeathSiblingLinkageRecipe.isViable(death_record1, death_record2);
    }

    private boolean deathSiblingLinkViableWithInvalidData() {

        final LXP death_record1 = makeInvalidDeath();
        final LXP death_record2 = makeInvalidDeath();

        return DeathSiblingLinkageRecipe.isViable(death_record1, death_record2);
    }

    private boolean deathBrideIdentityLinkViable(final int death_year, final int age_at_death, final String date_of_birth, final int marriage_day, final int marriage_month, final int marriage_year, String age_or_date_of_birth) {

        final LXP death_record = makeDeath(death_year, age_at_death, date_of_birth);
        final LXP marriage_record = makeMarriage(marriage_day, marriage_month, marriage_year, age_or_date_of_birth, true, "1");

        return DeathBrideIdentityLinkageRecipe.isViable(death_record, marriage_record);
    }

    private boolean deathBrideIdentityLinkViableWithInvalidData() {

        final LXP death_record = makeInvalidDeath();
        final LXP marriage_record = makeInvalidMarriage("1");

        return DeathBrideIdentityLinkageRecipe.isViable(death_record, marriage_record);
    }

    private boolean deathBrideSiblingLinkViable(final int death_year, final int age_at_death, final String date_of_birth, final int marriage_day, final int marriage_month, final int marriage_year, String age_or_date_of_birth) {

        final LXP death_record = makeDeath(death_year, age_at_death, date_of_birth);
        final LXP marriage_record = makeMarriage(marriage_day, marriage_month, marriage_year, age_or_date_of_birth, true, "1");

        return DeathBrideSiblingLinkageRecipe.isViable(death_record, marriage_record);
    }

    private boolean deathBrideSiblingLinkViableWithInvalidData() {

        final LXP death_record = makeInvalidDeath();
        final LXP marriage_record = makeInvalidMarriage("1");

        return DeathBrideSiblingLinkageRecipe.isViable(death_record, marriage_record);
    }

    private boolean deathGroomIdentityLinkViable(final int death_year, final int age_at_death, final String date_of_birth, final int marriage_day, final int marriage_month, final int marriage_year, String age_or_date_of_birth) {

        final LXP death_record = makeDeath(death_year, age_at_death, date_of_birth);
        final LXP marriage_record = makeMarriage(marriage_day, marriage_month, marriage_year, age_or_date_of_birth, false, "1");

        return DeathGroomIdentityLinkageRecipe.isViable(death_record, marriage_record);
    }

    private boolean deathGroomIdentityLinkViableWithInvalidData() {

        final LXP death_record = makeInvalidDeath();
        final LXP marriage_record = makeInvalidMarriage("1");

        return DeathGroomIdentityLinkageRecipe.isViable(death_record, marriage_record);
    }

    private boolean deathGroomSiblingLinkViable(final int death_year, final int age_at_death, final String date_of_birth, final int marriage_day, final int marriage_month, final int marriage_year, String age_or_date_of_birth) {

        final LXP death_record = makeDeath(death_year, age_at_death, date_of_birth);
        final LXP marriage_record = makeMarriage(marriage_day, marriage_month, marriage_year, age_or_date_of_birth, true, "1");

        return DeathBrideSiblingLinkageRecipe.isViable(death_record, marriage_record);
    }

    private boolean deathGroomSiblingLinkViableWithInvalidData() {

        final LXP death_record = makeInvalidDeath();
        final LXP marriage_record = makeInvalidMarriage("1");

        return DeathBrideSiblingLinkageRecipe.isViable(death_record, marriage_record);
    }

    private boolean deathParentsMarriageIdentityLinkViable(final int death_year, final int age_at_death, final String date_of_birth, final int marriage_day, final int marriage_month, final int marriage_year) {

        final LXP death_record = makeDeath(death_year, age_at_death, date_of_birth);
        final LXP marriage_record = makeMarriage(marriage_day, marriage_month, marriage_year, "0", false, "1");

        return DeathParentsMarriageIdentityLinkageRecipe.isViable(death_record, marriage_record);
    }

    private boolean deathParentsMarriageIdentityLinkViableWithInvalidData() {

        final LXP death_record = makeInvalidDeath();
        final LXP marriage_record = makeInvalidMarriage("1");

        return DeathParentsMarriageIdentityLinkageRecipe.isViable(death_record, marriage_record);
    }

    private boolean birthGroomIdentityLinkViableWithInvalidData() {

        final LXP birth_record = makeInvalidBirth();
        final LXP marriage_record = makeInvalidMarriage("1");

        return BirthGroomIdentityLinkageRecipe.isViable(birth_record, marriage_record);
    }

    private boolean groomGroomIdentityLinkViable(final int marriage_day1, final int marriage_month1, final int marriage_year1, final String age_or_date_of_birth1, final int marriage_day2, final int marriage_month2, final int marriage_year2, final String age_or_date_of_birth2) {

        final LXP marriage_record1 = makeMarriage(marriage_day1, marriage_month1, marriage_year1, age_or_date_of_birth1, false, "1");
        final LXP marriage_record2 = makeMarriage(marriage_day2, marriage_month2, marriage_year2, age_or_date_of_birth2, false, "2");

        return GroomGroomIdentityLinkageRecipe.isViable(marriage_record1, marriage_record2);
    }

    private boolean groomGroomIdentityLinkViableWithInvalidData() {

        final LXP marriage_record1 = makeInvalidMarriage("1");
        final LXP marriage_record2 = makeInvalidMarriage("2");

        return GroomGroomIdentityLinkageRecipe.isViable(marriage_record1, marriage_record2);
    }

    private boolean groomGroomSiblingLinkViable(final int marriage_day1, final int marriage_month1, final int marriage_year1, final String age_or_date_of_birth1, final int marriage_day2, final int marriage_month2, final int marriage_year2, final String age_or_date_of_birth2) {

        final LXP marriage_record1 = makeMarriage(marriage_day1, marriage_month1, marriage_year1, age_or_date_of_birth1, false, "1");
        final LXP marriage_record2 = makeMarriage(marriage_day2, marriage_month2, marriage_year2, age_or_date_of_birth2, false, "2");

        return GroomGroomSiblingLinkageRecipe.isViable(marriage_record1, marriage_record2);
    }

    private boolean groomGroomSiblingLinkViableWithInvalidData() {

        final LXP marriage_record1 = makeInvalidMarriage("1");
        final LXP marriage_record2 = makeInvalidMarriage("2");

        return GroomGroomSiblingLinkageRecipe.isViable(marriage_record1, marriage_record2);
    }

    private boolean groomMarriageParentsMarriageIdentityLinkViable(final int marriage_day1, final int marriage_month1, final int marriage_year1, final int marriage_day2, final int marriage_month2, final int marriage_year2) {

        final LXP marriage_record1 = makeMarriage(marriage_day1, marriage_month1, marriage_year1);
        final LXP marriage_record2 = makeMarriage(marriage_day2, marriage_month2, marriage_year2);

        return GroomMarriageParentsMarriageIdentityLinkageRecipe.isViable(marriage_record1, marriage_record2);
    }

    private boolean groomMarriageParentsMarriageIdentityLinkViableWithInvalidData() {

        final LXP marriage_record1 = makeInvalidMarriage("1");
        final LXP marriage_record2 = makeInvalidMarriage("2");

        return GroomMarriageParentsMarriageIdentityLinkageRecipe.isViable(marriage_record1, marriage_record2);
    }

    private Birth makeBirth(final int birth_year) {

        return makeBirth(1, CommonLinkViabilityLogic.DEFAULT_MONTH, birth_year);
    }

    private Birth makeBirth(final String date_of_birth) {

        int birth_day = 1;
        int birth_month = CommonLinkViabilityLogic.DEFAULT_MONTH;

        try {
            birth_day = Integer.parseInt(Normalisation.extractDay(date_of_birth));
            birth_month = Integer.parseInt(Normalisation.extractMonth(date_of_birth));
        }
        catch (NumberFormatException e) {
            // Ignore, use default values.
        }

        final int birth_year = Integer.parseInt(Normalisation.extractYear(date_of_birth));

        return makeBirth(birth_day, birth_month, birth_year);
    }

    private Birth makeBirth(final int birth_day, final int birth_month, final int birth_year) {

        final Birth record = new Birth();

        for (int i = 0; i < Birth.BIRTH_YEAR; i++) {
            record.put(i, "");
        }
        Random r = new Random( 6789143561L );

        record.put(Birth.FORENAME, String.valueOf(r.nextDouble() ) ); // random forename
        record.put(Birth.SURNAME, String.valueOf(r.nextDouble() ) ); // random surname
        record.put(Birth.BIRTH_YEAR, String.valueOf(birth_year));
        record.put(Birth.BIRTH_DAY, String.valueOf(birth_day));
        record.put(Birth.BIRTH_MONTH, String.valueOf(birth_month));
        return record;
    }

    private Birth makeInvalidBirth() {

        final Birth record = new Birth();

        for (int i = 0; i < Birth.BIRTH_YEAR; i++) {
            record.put(i, "");
        }

        Random r = new Random( 6783456341143561L );

        record.put(Birth.FORENAME, String.valueOf(r.nextDouble() ) ); // random forename
        record.put(Birth.SURNAME, String.valueOf(r.nextDouble() ) ); // random surname

        record.put(Birth.BIRTH_YEAR, "Unknown");
        return record;
    }

    private Death makeDeath(final int death_year) {

        final Death record = new Death();

        for (int i = 0; i < Death.DEATH_YEAR; i++) {
            record.put(i, "");
        }

        Random r = new Random( 23432333L );

        record.put(Death.FORENAME, String.valueOf(r.nextDouble() ) ); // random forename
        record.put(Death.SURNAME, String.valueOf(r.nextDouble() ) ); // random surname
        record.put(Death.DEATH_YEAR, String.valueOf(death_year));
        return record;
    }

    private Death makeDeath(final int death_day, final int death_month, final int death_year) {

        final Death record = makeDeath(death_year);

        record.put(Death.DEATH_DAY, String.valueOf(death_day));
        record.put(Death.DEATH_MONTH, String.valueOf(death_month));
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

    private Death makeDeath(final String date_of_birth) {

        final int year_of_birth = Integer.parseInt(Normalisation.extractYear(date_of_birth));

        final Death record = makeDeath(year_of_birth + 50, 50, date_of_birth);

        record.put(Death.DATE_OF_BIRTH, date_of_birth);
        return record;
    }

    private Death makeDeath(final String date_of_birth, int age_at_death) {

        final Death record = new Death();

        for (int i = 0; i <= Death.DEATH_YEAR; i++) {
            record.put(i, "");
        }

        record.put(Death.DATE_OF_BIRTH, date_of_birth);
        record.put(Death.AGE_AT_DEATH, String.valueOf(age_at_death));
        return record;
    }

    private Death makeInvalidDeath() {

        final Death record = new Death();

        for (int i = 0; i < Death.DEATH_YEAR; i++) {
            record.put(i, "");
        }

        Random r = new Random( 2455432333L );

        record.put(Death.FORENAME, String.valueOf(r.nextDouble() ) ); // random forename
        record.put(Death.SURNAME, String.valueOf(r.nextDouble() ) ); // random surname

        record.put(Death.DEATH_YEAR, "Unknown");
        record.put(Death.AGE_AT_DEATH, "Unknown");
        return record;
    }

    private Marriage makeMarriage(final int marriage_year) {

        final Marriage record = new Marriage();

        for (int i = 0; i < Marriage.MARRIAGE_YEAR; i++) {
            record.put(i, "");
        }

        Random r = new Random( 2343243241L );

        record.put(Marriage.GROOM_FORENAME, String.valueOf(r.nextDouble() ) ); // random forename
        record.put(Marriage.GROOM_SURNAME, String.valueOf(r.nextDouble() ) ); // random surname
        record.put(Marriage.BRIDE_FORENAME, String.valueOf(r.nextDouble() ) ); // random forename
        record.put(Marriage.BRIDE_SURNAME, String.valueOf(r.nextDouble() ) ); // random surname
        record.put(Marriage.MARRIAGE_YEAR, String.valueOf(marriage_year));
        return record;
    }

    private Marriage makeMarriage(final int marriage_day, final int marriage_month, final int marriage_year) {

        final Marriage record = makeMarriage(marriage_year);


        record.put(Marriage.MARRIAGE_DAY, String.valueOf(marriage_day));
        record.put(Marriage.MARRIAGE_MONTH, String.valueOf(marriage_month));
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

        Random r = new Random( 23431L );

        record.put(Marriage.GROOM_FORENAME, String.valueOf(r.nextDouble() ) ); // random forename
        record.put(Marriage.GROOM_SURNAME, String.valueOf(r.nextDouble() ) ); // random surname
        record.put(Marriage.BRIDE_FORENAME, String.valueOf(r.nextDouble() ) ); // random forename
        record.put(Marriage.BRIDE_SURNAME, String.valueOf(r.nextDouble() ) ); // random surname

        record.put(Marriage.STANDARDISED_ID, id);
        record.put(Marriage.MARRIAGE_DAY, "Unknown");
        record.put(Marriage.MARRIAGE_MONTH, "Unknown");
        record.put(Marriage.MARRIAGE_YEAR, "Unknown");
        record.put(Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH, "Unknown");
        record.put(Marriage.GROOM_AGE_OR_DATE_OF_BIRTH, "Unknown");

        return record;
    }
}
