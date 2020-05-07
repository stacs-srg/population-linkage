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
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthGroomIdentity
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthMotherIdentity
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthSibling
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBrideBrideSibling
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBrideGroomSibling
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaDeathSibling
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaGroomGroomSibling

# Archived input data files:
# sftp://secure@manifesto.cs.st-andrews.ac.uk//data/umea-paper/UmeaBirthBrideIdentityPRFByThreshold.csv
# sftp://secure@manifesto.cs.st-andrews.ac.uk//data/umea-paper/UmeaBirthDeathIdentityPRFByThreshold.csv
# sftp://secure@manifesto.cs.st-andrews.ac.uk//data/umea-paper/UmeaBirthFatherIdentityPRFByThreshold.csv
# sftp://secure@manifesto.cs.st-andrews.ac.uk//data/umea-paper/UmeaBirthGroomIdentityPRFByThreshold.csv
# sftp://secure@manifesto.cs.st-andrews.ac.uk//data/umea-paper/UmeaBirthMotherIdentityPRFByThreshold.csv
# sftp://secure@manifesto.cs.st-andrews.ac.uk//data/umea-paper/UmeaBirthSiblingPRFByThreshold.csv
# sftp://secure@manifesto.cs.st-andrews.ac.uk//data/umea-paper/UmeaBirthSiblingPRFByThreshold-full-filtered.csv
# [to be replaced by Al with non-filtered version when connectivity allows]
# sftp://secure@manifesto.cs.st-andrews.ac.uk//data/umea-paper/UmeaBrideBrideSiblingPRFByThreshold.csv
# sftp://secure@manifesto.cs.st-andrews.ac.uk//data/umea-paper/UmeaBrideGroomSiblingPRFByThreshold.csv
# sftp://secure@manifesto.cs.st-andrews.ac.uk//data/umea-paper/UmeaDeathSiblingPRFByThreshold.csv
# sftp://secure@manifesto.cs.st-andrews.ac.uk//data/umea-paper/UmeaGroomGroomSiblingPRFByThreshold.csv

##########################################################################
# Edit these appropriately.

INPUT_DIRECTORY_PATH <- "~/Desktop/data"
OUTPUT_DIRECTORY_PATH <- "~/Desktop"
PROJECT_DIRECTORY_PATH <- "~/Documents/Code/github/population-linkage"

INPUT_FILE_NAME_ROOT_DETAIL <- "PRFByThreshold"
OUTPUT_FILE_NAME_ROOT <- "figure"
##########################################################################

R_DIRECTORY_RELATIVE_PATH <- "src/main/scripts/R"
R_WORKING_DIRECTORY <- paste(PROJECT_DIRECTORY_PATH, R_DIRECTORY_RELATIVE_PATH, sep = "/")
setwd(R_WORKING_DIRECTORY)
source("umea-paper/common.R")

INPUT_FILE_NAME_ROOTS <- c(BGI_FILE_NAME_ROOT, BS_FILE_NAME_ROOT, BBI_FILE_NAME_ROOT, BDI_FILE_NAME_ROOT, BFI_FILE_NAME_ROOT,
                           BMI_FILE_NAME_ROOT, BBS_FILE_NAME_ROOT, BGS_FILE_NAME_ROOT, DS_FILE_NAME_ROOT, GGS_FILE_NAME_ROOT)
FACETED <- c(T, T, F, F, F, F, F, F, F, F)

NUMBER_OF_INPUT_FILES <- length(INPUT_FILE_NAME_ROOTS)
FIRST_OUTPUT_FILE_NUMBER <- 6
Y_IMAGE_WIDTH <- 13

# Figure 6: birth-groom identity linkage (threshold vs F), faceted
# Figure 7: birth-groom identity linkage (precision vs recall), faceted
# Figure 8: birth sibling linkage (threshold vs F), faceted
# Figure 9: birth sibling linkage (precision vs recall), faceted
# Figure 10: birth-bride identity linkage (threshold vs F), overlaid
# Figure 11: birth-bride identity linkage (precision vs recall), overlaid
# Figure 12: birth-death identity linkage (threshold vs F), overlaid
# Figure 13: birth-death identity linkage (precision vs recall), overlaid
# Figure 14: birth-father identity linkage (threshold vs F), overlaid
# Figure 15: birth-father identity linkage (precision vs recall), overlaid
# Figure 16: birth-mother identity linkage (threshold vs F), overlaid
# Figure 17: birth-mother identity linkage (precision vs recall), overlaid
# Figure 18: bride-bride sibling linkage (threshold vs F), overlaid
# Figure 19: bride-bride sibling linkage (precision vs recall), overlaid
# Figure 20: bride-groom sibling linkage (threshold vs F), overlaid
# Figure 21: bride-groom sibling linkage (precision vs recall), overlaid
# Figure 22: death sibling linkage (threshold vs F), overlaid
# Figure 23: death sibling linkage (precision vs recall), overlaid
# Figure 24: groom-groom sibling linkage (threshold vs F), overlaid
# Figure 25: groom-groom sibling linkage (precision vs recall), overlaid

for (i in 0:(NUMBER_OF_INPUT_FILES - 1)) {

  input_file_path <- inputFilePath(INPUT_DIRECTORY_PATH, INPUT_FILE_NAME_ROOTS[i + 1], INPUT_FILE_NAME_ROOT_DETAIL)

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, i * 2 + FIRST_OUTPUT_FILE_NUMBER)

  saveFMeasureVsThreshold(input_file_path, output_file_path, "Threshold", "F1-measure",
                          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, FACETED[i + 1])

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, i * 2 + FIRST_OUTPUT_FILE_NUMBER + 1)

  savePrecisionVsRecall(input_file_path, output_file_path, "Recall", "Precision",
                        PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, FACETED[i + 1])
}
