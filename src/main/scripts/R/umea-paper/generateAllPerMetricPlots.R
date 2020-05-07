# F1 measure vs threshold for various metrics, for each linkage
# Input files: output from ...

# TODO: update with location of archived input files on manifesto

##########################################################################
# Edit these appropriately.

INPUT_DIRECTORY_PATH <- "~/Desktop/data"
OUTPUT_DIRECTORY_PATH <- "~/Desktop"
PROJECT_DIRECTORY_PATH <- "~/Documents/Code/github/population-linkage"
##########################################################################

R_DIRECTORY_RELATIVE_PATH <- "src/main/scripts/R"
R_WORKING_DIRECTORY <- paste(PROJECT_DIRECTORY_PATH, R_DIRECTORY_RELATIVE_PATH, sep = "/")
setwd(R_WORKING_DIRECTORY)
source("umea-paper/common.R")

filename_roots <- c(BBI_FILE_NAME_ROOT, BDI_FILE_NAME_ROOT, BFI_FILE_NAME_ROOT, BGI_FILE_NAME_ROOT, BMI_FILE_NAME_ROOT,
                    BS_FILE_NAME_ROOT, BBS_FILE_NAME_ROOT, BGS_FILE_NAME_ROOT, DS_FILE_NAME_ROOT, GGS_FILE_NAME_ROOT)

for (filename_root in filename_roots) {

  input_file_path <- inputFilePath(INPUT_DIRECTORY_PATH, filename_root)
  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, filename_root, "FVsThreshold")

  # Clear any data from previous plots.
  if (exists("linkage_data")) { rm(list = "linkage_data") }

  saveFMeasureVsThreshold(input_file_path, output_file_path, "Threshold", "F1-measure",
                          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, F)

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, filename_root, "FVsThresholdFaceted")
  rm(list = "linkage_data")

  saveFMeasureVsThreshold(input_file_path, output_file_path, "Threshold", "F1-measure",
                          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, T)

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, filename_root, "PrecisionVsRecall")
  rm(list = "linkage_data")

  savePrecisionVsRecall(input_file_path, output_file_path, "Recall", "Precision",
                        PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, F)

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, filename_root, "PrecisionVsRecallFaceted")
  rm(list = "linkage_data")

  savePrecisionVsRecall(input_file_path, output_file_path, "Recall", "Precision",
                        PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, T)

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, filename_root, "ROC")
  rm(list = "linkage_data")

  saveROC(input_file_path, output_file_path, "False positive rate", "True positive rate",
          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, F)

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, filename_root, "ROCFaceted")
  rm(list = "linkage_data")

  saveROC(input_file_path, output_file_path, "False positive rate", "True positive rate",
          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, T)
}
