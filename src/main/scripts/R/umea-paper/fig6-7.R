# Figures 6-7: F1 measure vs threshold for various metrics, for selected linkages, facet wrapped.
# Input files: output from ...

# TODO: update with location of archived input files on manifesto

##########################################################################
# Edit these appropriately.

INPUT_DIRECTORY_PATH <- "~/Desktop"
OUTPUT_DIRECTORY_PATH <- "~/Desktop"
PROJECT_DIRECTORY_PATH <- "~/Documents/Code/github/population-linkage"

OUTPUT_FILE_NAMES <- c("figure6", "figure7")
##########################################################################

##########################################################################
# Edit if adjustments to figure required.

X_AXIS_LABEL <- "Threshold"
Y_AXIS_LABEL <- "F1-measure"
##########################################################################

R_DIRECTORY_RELATIVE_PATH <- "src/main/scripts/R"
R_WORKING_DIRECTORY <- paste(PROJECT_DIRECTORY_PATH, R_DIRECTORY_RELATIVE_PATH, sep = "/")
setwd(R_WORKING_DIRECTORY)
source("umea-paper/common.R")

# Figure 6: birth-groom identity linkage
# Figure 7: birth sibling linkage
INPUT_FILE_NAMES <- c(BGI_FILE_NAME, BS_FILE_NAME)

for (i in 1:2) {
  input_file_path <- inputFilePath(INPUT_DIRECTORY_PATH, INPUT_FILE_NAMES[i])
  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAMES[i])

  # Clear any data from previous plots.
  if (exists("linkage_data")) { rm(list = "linkage_data") }

  saveFMeasureVsThreshold(input_file_path, output_file_path, X_AXIS_LABEL, Y_AXIS_LABEL,
                          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, T)
}
