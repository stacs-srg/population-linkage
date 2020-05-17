# Generates all plots of F1 measure vs threshold, precision vs recall and ROC for various metrics,
# for selected linkages, both facet wrapped and overlaid.
#
# This generates all the versions, including those not currently selected for use as figures.
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
##########################################################################

R_DIRECTORY_RELATIVE_PATH <- "src/main/scripts/R"
R_WORKING_DIRECTORY <- paste(PROJECT_DIRECTORY_PATH, R_DIRECTORY_RELATIVE_PATH, sep = "/")
setwd(R_WORKING_DIRECTORY)
source("umea-paper/common.R")

# Compress Y direction to reduce space taken up in papers.
Y_IMAGE_WIDTH <- 13

INPUT_FILE_NAME_ROOTS <- c(BBI_FILE_NAME_ROOT, BDI_FILE_NAME_ROOT, BFI_FILE_NAME_ROOT, BGI_FILE_NAME_ROOT, BMI_FILE_NAME_ROOT,
                           BS_FILE_NAME_ROOT, BBS_FILE_NAME_ROOT, BGS_FILE_NAME_ROOT, DS_FILE_NAME_ROOT, GGS_FILE_NAME_ROOT)

adjustOutputFileNameDetail <- function(output_file_name_detail, faceted) {

  if (faceted) {
    return(paste0(output_file_name_detail, "Faceted"))
  }
  else {
    return(output_file_name_detail)
  }
}

saveFMeasureVsThresholdPlot <- function(input_file_path, output_file_name_root, faceted) {

  output_file_name_detail <- adjustOutputFileNameDetail("FVsThreshold", faceted)
  x_axis_label <- "Threshold"
  y_axis_label <- "F1-measure"

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, output_file_name_root, output_file_name_detail)

  saveFMeasureVsThreshold(input_file_path, output_file_path, x_axis_label, y_axis_label,
                          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, faceted)
}

savePrecisionVsRecallPlot <- function(input_file_path, output_file_name_root, faceted) {

  output_file_name_detail <- adjustOutputFileNameDetail("PrecisionVsRecall", faceted)
  x_axis_label <- "Recall"
  y_axis_label <- "Precision"

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, output_file_name_root, output_file_name_detail)

  savePrecisionVsRecall(input_file_path, output_file_path, x_axis_label, y_axis_label,
                        PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, faceted)
}

saveROCPlot <- function(input_file_path, output_file_name_root, faceted) {

  output_file_name_detail <- adjustOutputFileNameDetail("ROC", faceted)
  x_axis_label <- "False positive rate"
  y_axis_label <- "True positive rate"

  output_file_path <- outputFilePath(OUTPUT_DIRECTORY_PATH, output_file_name_root, output_file_name_detail)

  saveROC(input_file_path, output_file_path, x_axis_label, y_axis_label,
          PALETTE, IMAGE_DPI, X_IMAGE_WIDTH, Y_IMAGE_WIDTH, IMAGE_SIZE_UNITS, faceted)
}

saveFMeasureVsThresholdPlots <- function(input_file_path, output_file_name_root) {

  saveFMeasureVsThresholdPlot(input_file_path, output_file_name_root, F)
  saveFMeasureVsThresholdPlot(input_file_path, output_file_name_root, T)
}

savePrecisionVsRecallPlots <- function(input_file_path, output_file_name_root) {

  savePrecisionVsRecallPlot(input_file_path, output_file_name_root, F)
  savePrecisionVsRecallPlot(input_file_path, output_file_name_root, T)
}

saveROCPlots <- function(input_file_path, output_file_name_root) {

  saveROCPlot(input_file_path, output_file_name_root, F)
  saveROCPlot(input_file_path, output_file_name_root, T)
}

for (input_filename_root in INPUT_FILE_NAME_ROOTS) {

  input_file_path <- inputFilePath(INPUT_DIRECTORY_PATH, input_filename_root, INPUT_FILE_NAME_ROOT_DETAIL)

  saveFMeasureVsThresholdPlots(input_file_path, input_filename_root)
  savePrecisionVsRecallPlots(input_file_path, input_filename_root)
  saveROCPlots(input_file_path, input_filename_root)
}
