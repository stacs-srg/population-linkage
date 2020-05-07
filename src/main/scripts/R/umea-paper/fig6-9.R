# Figures 6-9: F1 measure vs threshold and precision vs recall for various metrics,
# for selected linkages, facet wrapped.
#
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

INPUT_FILE_NAME_ROOTS <- c(BGI_FILE_NAME_ROOT, BS_FILE_NAME_ROOT)

number_of_input_files <- length(INPUT_FILE_NAME_ROOTS)
first_output_file_number <- 6

# Figure 6: birth-groom identity linkage (threshold vs F)
# Figure 7: birth-groom identity linkage (precision vs recall)
# Figure 8: birth sibling linkage (threshold vs F)
# Figure 9: birth sibling linkage (precision vs recall)

for (i in 0:(number_of_input_files - 1)) {

  input_file_path <- inputFilePath(INPUT_DIRECTORY_PATH, INPUT_FILE_NAME_ROOTS[i + 1], INPUT_FILE_NAME_ROOT_DETAIL)

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, i * 2 + first_output_file_number)

  # Clear any data from previous plots.
  if (exists("linkage_data")) { rm(list = "linkage_data") }

  saveFMeasureVsThreshold(input_file_path, output_file_path, "Threshold", "F1-measure",
                          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, T)

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, i * 2 + first_output_file_number + 1)

  rm(list = "linkage_data")

  savePrecisionVsRecall(input_file_path, output_file_path, "Recall", "Precision",
                        PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, T)
}
