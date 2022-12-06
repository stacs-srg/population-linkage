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

# Generates all plots of F1 measure vs threshold, precision vs recall and ROC for various metrics,
# for selected linkages, both facet wrapped and overlaid.
#
# This generates all the versions, including those not currently selected for use as figures.
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
##########################################################################

# Compress Y direction to reduce space taken up in papers.
Y_IMAGE_WIDTH <- 13

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
