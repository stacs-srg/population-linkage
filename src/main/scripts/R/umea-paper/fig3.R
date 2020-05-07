# Figure 3: evolution of F1 measure error for specific metric and threshold.
#
# Input file: output from Umea birth sibling bundling ground truth:
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthSibling
# run over all records with 10 repetitions
#
# Archived input data file:
# sftp://secure@manifesto.cs.st-andrews.ac.uk//data/umea-paper/UmeaBirthSiblingPRFByThreshold-full-filtered.csv
# [to be replaced by Al with non-filtered version when connectivity allows]

##########################################################################
# Edit these appropriately.

INPUT_DIRECTORY_PATH <- "~/Desktop/data"
OUTPUT_DIRECTORY_PATH <- "~/Desktop"
PROJECT_DIRECTORY_PATH <- "~/Documents/Code/github/population-linkage"

INPUT_FILE_NAME_ROOT <- "UmeaBirthSibling"
INPUT_FILE_NAME_ROOT_DETAIL <- "PRFByThreshold-full-filtered"
OUTPUT_FILE_NAME_ROOT <- "figure"
OUTPUT_FILE_NAME_ROOT_DETAIL <- 3
##########################################################################

##########################################################################
# Edit if adjustments to figure required.

METRIC <- "Sigma-Levenshtein"
THRESHOLD <- 0.8
X_UPPER_BOUND <- 25000
X_AXIS_LABEL <- "Records processed"
Y_AXIS_LABEL <- "Absolute error in F1-measure"
COLOURS <- "black"
##########################################################################

R_DIRECTORY_RELATIVE_PATH <- "src/main/scripts/R"
R_WORKING_DIRECTORY <- paste(PROJECT_DIRECTORY_PATH, R_DIRECTORY_RELATIVE_PATH, sep = "/")
setwd(R_WORKING_DIRECTORY)
source("umea-paper/common.R")

INPUT_FILE_PATH <- inputFilePath(INPUT_DIRECTORY_PATH, INPUT_FILE_NAME_ROOT, INPUT_FILE_NAME_ROOT_DETAIL)
OUTPUT_FILE_PATH <- outputFilePath(OUTPUT_DIRECTORY_PATH, OUTPUT_FILE_NAME_ROOT, OUTPUT_FILE_NAME_ROOT_DETAIL)

loadIntoGlobal(INPUT_FILE_PATH, "data")

# The output warning "Removed ... row(s) containing missing values (geom_path)" is harmless,
# indicating that data with x > X_UPPER_BOUND is ignored.
plot <- plotFMeasureErrorConvergence(data, METRIC, THRESHOLD, X_UPPER_BOUND, X_AXIS_LABEL, Y_AXIS_LABEL, COLOURS)

ggsave(OUTPUT_FILE_PATH, plot, dpi = IMAGE_DPI, width = X_IMAGE_WIDTH, height = Y_IMAGE_WIDTH, units = IMAGE_SIZE_UNITS)
