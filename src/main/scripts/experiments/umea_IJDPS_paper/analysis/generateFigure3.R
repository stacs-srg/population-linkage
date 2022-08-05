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

# Figure 3: evolution of F1 measure error for specific metric and threshold, for first 25,000 records.
#
# Input file: output from Umea birth sibling bundling ground truth:
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthSibling
# run over all records with 10 repetitions

source("common.R")
source("functionBank.R")

##########################################################################
# Edit these appropriately.

INPUT_FILE_NAME_ROOT <- "UmeaBirthSibling"
INPUT_FILE_NAME_ROOT_DETAIL <- "PRFByThreshold"
OUTPUT_FILE_NAME_ROOT <- "figure"
OUTPUT_FILE_NAME_ROOT_DETAIL <- 3
##########################################################################

##########################################################################
# Edit if adjustments to figure required.

DISTANCE_MEASURE_NAME <- "Normalised mean of field distances using: Cosine"
THRESHOLD <- 0.1
X_UPPER_BOUND <- 25000
X_AXIS_LABEL <- "Records processed"
Y_AXIS_LABEL <- "Absolute error in F1-measure"
COLOURS <- "black"
##########################################################################

INPUT_FILE_PATH <- inputFilePath(INPUT_DIRECTORY_PATH, INPUT_FILE_NAME_ROOT, INPUT_FILE_NAME_ROOT_DETAIL)
OUTPUT_FILE_PATH <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, OUTPUT_FILE_NAME_ROOT_DETAIL)

loadIntoGlobal(INPUT_FILE_PATH, "data")

# The output warning "Removed ... row(s) containing missing values (geom_path)" is harmless,
# indicating that data with x > X_UPPER_BOUND is ignored.
plot <- plotFMeasureErrorConvergence(data, DISTANCE_MEASURE_NAME, THRESHOLD, X_UPPER_BOUND, X_AXIS_LABEL, Y_AXIS_LABEL, COLOURS)

ggsave(OUTPUT_FILE_PATH, plot, dpi = IMAGE_DPI, width = X_IMAGE_WIDTH, height = Y_IMAGE_WIDTH, units = IMAGE_SIZE_UNITS)
