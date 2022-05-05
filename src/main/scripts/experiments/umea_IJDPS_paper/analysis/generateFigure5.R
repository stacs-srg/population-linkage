# Figure 5: evolution of F1 measure error for all metrics and thresholds, for first 25,000 records.
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
OUTPUT_FILE_NAME_ROOT_DETAIL <- 5
##########################################################################

##########################################################################
# Edit if adjustments to figure required.

X_UPPER_BOUND <- 25000
X_AXIS_LABEL <- "Records processed"
Y_AXIS_LABEL <- "Absolute error in F1-measure"
COLOUR <- "black"
##########################################################################

INPUT_FILE_PATH <- inputFilePath(INPUT_DIRECTORY_PATH, INPUT_FILE_NAME_ROOT, INPUT_FILE_NAME_ROOT_DETAIL)
OUTPUT_FILE_PATH <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, OUTPUT_FILE_NAME_ROOT_DETAIL)

loadIntoGlobal(INPUT_FILE_PATH, "data")

# The output warning "Removed ... row(s) containing missing values (geom_path)" is harmless,
# indicating that data with x > X_UPPER_BOUND is ignored.
plot <- plotAllFMeasureErrorConvergence(data, X_UPPER_BOUND, X_AXIS_LABEL, Y_AXIS_LABEL, COLOUR)

ggsave(OUTPUT_FILE_PATH, plot, dpi = IMAGE_DPI, width = X_IMAGE_WIDTH, height = Y_IMAGE_WIDTH, units = IMAGE_SIZE_UNITS)
