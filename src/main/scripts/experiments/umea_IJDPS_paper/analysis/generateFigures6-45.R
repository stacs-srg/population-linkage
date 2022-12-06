#
# Copyright 2022 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#
# This file is part of the module population-linkage.
#
# population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
# License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
# version.
#
# population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with population-linkage. If not, see
# <http://www.gnu.org/licenses/>.
#

# Figures 6-9: F1 measure vs threshold and precision vs recall for various metrics,
# for selected linkages, facet wrapped.
#
# Figures 10-25: F1 measure vs threshold and precision vs recall for various metrics,
# for selected linkages, overlaid.
#
# Input files: output from all the various linkages, run over 25,000 records without repetition:
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthBrideIdentity
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthDeathIdentity
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthFatherIdentity
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthParentsMarriageIdentity
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.FelligiSunterBirthParentsMarriageAnalysis
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthSibling
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBrideBrideSibling
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBrideGroomSibling
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaDeathSibling
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaGroomGroomSibling

source("common.R")
source("functionBank.R")

##########################################################################
# Edit these appropriately.

INPUT_FILE_NAME_ROOT_DETAIL <- "PRFByThreshold"
OUTPUT_FILE_NAME_ROOT <- "figure"
##########################################################################

FACETED <- c(T, T, F, F, F, F, F, F, F, F)

NUMBER_OF_INPUT_FILES <- length(INPUT_FILE_NAME_ROOTS)
FIRST_OUTPUT_FILE_NUMBER <- 6
Y_IMAGE_WIDTH <- 13

# Figure 6: birth-groom identity linkage (threshold vs F), faceted
# Figure 7: birth-groom identity linkage (threshold vs specificity), faceted
# Figure 8: birth-groom identity linkage (precision vs recall), faceted
# Figure 9: birth-groom identity linkage (specificity vs recall), faceted
# Figure 10: birth sibling linkage (threshold vs F), faceted
# Figure 11: birth sibling linkage (threshold vs specificity), faceted
# Figure 12: birth sibling linkage (precision vs recall), faceted
# Figure 13: birth sibling linkage (specificity vs recall), faceted
# Figure 14: birth-bride identity linkage (threshold vs F), overlaid
# Figure 15: birth-bride identity linkage (threshold vs specificity), overlaid
# Figure 16: birth-bride identity linkage (precision vs recall), overlaid
# Figure 17: birth-bride identity linkage (specificity vs recall), overlaid
# Figure 18: birth-death identity linkage (threshold vs F), overlaid
# Figure 19: birth-death identity linkage (threshold vs specificity), overlaid
# Figure 20: birth-death identity linkage (precision vs recall), overlaid
# Figure 21: birth-death identity linkage (specificity vs recall), overlaid
# Figure 22: birth-father identity linkage (threshold vs F), overlaid
# Figure 23: birth-father identity linkage (threshold vs specificity), overlaid
# Figure 24: birth-father identity linkage (precision vs recall), overlaid
# Figure 25: birth-father identity linkage (specificity vs recall), overlaid
# Figure 26: birth-mother identity linkage (threshold vs F), overlaid
# Figure 27: birth-mother identity linkage (threshold vs specificity), overlaid
# Figure 28: birth-mother identity linkage (precision vs recall), overlaid
# Figure 29: birth-mother identity linkage (specificity vs recall), overlaid
# Figure 30: bride-bride sibling linkage (threshold vs F), overlaid
# Figure 31: bride-bride sibling linkage (threshold vs specificity), overlaid
# Figure 32: bride-bride sibling linkage (precision vs recall), overlaid
# Figure 33: bride-bride sibling linkage (specificity vs recall), overlaid
# Figure 34: bride-groom sibling linkage (threshold vs F), overlaid
# Figure 35: bride-groom sibling linkage (threshold vs specificity), overlaid
# Figure 36: bride-groom sibling linkage (precision vs recall), overlaid
# Figure 37: bride-groom sibling linkage (specificity vs recall), overlaid
# Figure 38: death sibling linkage (threshold vs F), overlaid
# Figure 39: death sibling linkage (threshold vs specificity), overlaid
# Figure 40: death sibling linkage (precision vs recall), overlaid
# Figure 41: death sibling linkage (specificity vs recall), overlaid
# Figure 42: groom-groom sibling linkage (threshold vs F), overlaid
# Figure 43: groom-groom sibling linkage (threshold vs specificity), overlaid
# Figure 44: groom-groom sibling linkage (precision vs recall), overlaid
# Figure 45: groom-groom sibling linkage (specificity vs recall), overlaid

for (i in 0:(NUMBER_OF_INPUT_FILES - 1)) {

  input_file_path <- inputFilePath(INPUT_DIRECTORY_PATH, INPUT_FILE_NAME_ROOTS[i + 1], INPUT_FILE_NAME_ROOT_DETAIL)

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, i * 4 + FIRST_OUTPUT_FILE_NUMBER)

  saveFMeasureVsThreshold(input_file_path, output_file_path, "Threshold", "F1-measure",
                          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, FACETED[i + 1])

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, i * 4 + FIRST_OUTPUT_FILE_NUMBER + 1)

  saveSpecificityVsThreshold(input_file_path, output_file_path, "Threshold", "Specificity",
                          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, FACETED[i + 1])

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, i * 4 + FIRST_OUTPUT_FILE_NUMBER + 2)

  savePrecisionVsRecall(input_file_path, output_file_path, "Recall", "Precision",
                        PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, FACETED[i + 1])

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, i * 4 + FIRST_OUTPUT_FILE_NUMBER + 3)

  saveSpecificityVsRecall(input_file_path, output_file_path, "Recall", "Specificity",
                        PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, FACETED[i + 1])
}
