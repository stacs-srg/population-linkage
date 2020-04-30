# Figures 8-15: F1 measure vs threshold for various metrics, for selected linkages, overlaid.
# Input files: output from ...

# TODO: update with location of archived input files on manifesto

##########################################################################
# Edit these appropriately.

INPUT_DIRECTORY_PATH <- "~/Desktop"
OUTPUT_DIRECTORY_PATH <- "~/Desktop"
PROJECT_DIRECTORY_PATH <- "~/Documents/Code/github/population-linkage"

OUTPUT_FILE_NAMES <- c("figure8", "figure9", "figure10", "figure11", "figure12", "figure13", "figure14", "figure15")
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

# Figure 8:  birth-bride identity linkage
# Figure 9:  birth-death identity linkage
# Figure 10: birth-father identity linkage
# Figure 11: birth-mother identity linkage
# Figure 12: bride-bride sibling linkage
# Figure 13: bride-groom sibling linkage
# Figure 14: death sibling linkage
# Figure 15: groom-groom sibling linkage
INPUT_FILE_NAMES <- c(BBI_FILE_NAME, BDI_FILE_NAME, BFI_FILE_NAME, BMI_FILE_NAME, BBS_FILE_NAME, BGS_FILE_NAME, DS_FILE_NAME, GGS_FILE_NAME)

for (i in seq_along(OUTPUT_FILE_NAMES)) {
  input_file_path <- inputFilePath(INPUT_DIRECTORY_PATH, INPUT_FILE_NAMES[i])
  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAMES[i])

  # Clear any data from previous plots.
  if (exists("linkage_data")) { rm(list = "linkage_data") }

  saveFMeasureVsThreshold(input_file_path, output_file_path, X_AXIS_LABEL, Y_AXIS_LABEL,
                          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, F)
}
