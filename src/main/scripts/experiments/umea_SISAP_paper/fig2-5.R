# Figures 2-5: F1 measure vs threshold and precision vs recall for various metrics,
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
OUTPUT_FILE_NAME_ROOT <- "figure0"
##########################################################################

R_DIRECTORY_RELATIVE_PATH <- "src/main/scripts/R"
R_WORKING_DIRECTORY <- paste(PROJECT_DIRECTORY_PATH, R_DIRECTORY_RELATIVE_PATH, sep = "/")
setwd(R_WORKING_DIRECTORY)
source("umea-paper/common.R")

INPUT_FILE_NAME_ROOTS <- c(BS_FILE_NAME_ROOT, BBI_FILE_NAME_ROOT)

NUMBER_OF_INPUT_FILES <- length(INPUT_FILE_NAME_ROOTS)
FIRST_OUTPUT_FILE_NUMBER <- 2
Y_IMAGE_WIDTH <- 13

# Figure 2: birth sibling linkage (threshold vs F), overlaid
# Figure 3: birth-bride identity linkage (threshold vs F), overlaid
# Figure 4: birth sibling linkage (precision vs recall), overlaid
# Figure 5: birth-bride identity linkage (precision vs recall), overlaid

for (i in 0:(NUMBER_OF_INPUT_FILES - 1)) {

  input_file_path <- inputFilePath(INPUT_DIRECTORY_PATH, INPUT_FILE_NAME_ROOTS[i + 1], INPUT_FILE_NAME_ROOT_DETAIL)

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, FIRST_OUTPUT_FILE_NUMBER + i)

  saveFMeasureVsThreshold(input_file_path, output_file_path, "Threshold", "F1-measure",
                          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, F)

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, FIRST_OUTPUT_FILE_NUMBER + NUMBER_OF_INPUT_FILES + i)

  savePrecisionVsRecall(input_file_path, output_file_path, "Recall", "Precision",
                        PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, F)
}
