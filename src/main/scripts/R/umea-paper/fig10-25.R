# Figures 8-15: F1 measure vs threshold for various metrics, for selected linkages, overlaid.
# Input files: output from ...

# TODO: update with location of archived input files on manifesto

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

INPUT_FILE_NAME_ROOTS <- c(BBI_FILE_NAME_ROOT, BDI_FILE_NAME_ROOT, BFI_FILE_NAME_ROOT, BMI_FILE_NAME_ROOT,
                           BBS_FILE_NAME_ROOT, BGS_FILE_NAME_ROOT, DS_FILE_NAME_ROOT, GGS_FILE_NAME_ROOT)

number_of_input_files <- length(INPUT_FILE_NAME_ROOTS)
first_output_file_number <- 10

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

for (i in 0:(number_of_input_files - 1)) {

  input_file_path <- inputFilePath(INPUT_DIRECTORY_PATH, INPUT_FILE_NAME_ROOTS[i + 1], INPUT_FILE_NAME_ROOT_DETAIL)

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, i * 2 + first_output_file_number)

  # Clear any data from previous plots.
  if (exists("linkage_data")) { rm(list = "linkage_data") }

  saveFMeasureVsThreshold(input_file_path, output_file_path, "Threshold", "F1-measure",
                          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, F)

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, i * 2 + first_output_file_number + 1)

  rm(list = "linkage_data")

  savePrecisionVsRecall(input_file_path, output_file_path, "Recall", "Precision",
                        PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, F)
}
