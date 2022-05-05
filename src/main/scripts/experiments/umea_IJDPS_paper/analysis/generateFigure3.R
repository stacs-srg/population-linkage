# Figure 3: evolution of F1 measure error for specific metric and threshold.
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
