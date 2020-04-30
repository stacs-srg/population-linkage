# Overlaid F1 measure vs threshold for various metrics, for each linkage.
# Input files: output from ...

# TODO: update with location of archived input files on manifesto

##########################################################################
# Edit these appropriately.

INPUT_DIRECTORY_PATH <- "~/Desktop"
OUTPUT_DIRECTORY_PATH <- "~/Desktop"
PROJECT_DIRECTORY_PATH <- "~/Documents/Code/github/population-linkage"
##########################################################################

##########################################################################
# Edit if adjustments to figures required.

X_AXIS_LABEL <- "Threshold"
Y_AXIS_LABEL <- "F1-measure"
##########################################################################

R_DIRECTORY_RELATIVE_PATH <- "src/main/scripts/R"
R_WORKING_DIRECTORY <- paste(PROJECT_DIRECTORY_PATH, R_DIRECTORY_RELATIVE_PATH, sep = "/")
setwd(R_WORKING_DIRECTORY)
source("umea-paper/common.R")

filenames <- c(BBI_FILE_NAME, BDI_FILE_NAME, BFI_FILE_NAME, BGI_FILE_NAME,BMI_FILE_NAME,
               BS_FILE_NAME, BBS_FILE_NAME, BGS_FILE_NAME, DS_FILE_NAME, GGS_FILE_NAME)

for (filename in filenames) {

  input_file_path <- inputFilePath(INPUT_DIRECTORY_PATH, filename)
  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, filename)

  # Clear any data from previous plots.
  if (exists("linkage_data")) { rm(list = "linkage_data") }

  saveFMeasureVsThreshold(input_file_path, output_file_path, X_AXIS_LABEL, Y_AXIS_LABEL,
                          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, F)
}
