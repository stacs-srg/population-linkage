# Figure 4: evolution of F1 measure error for all metrics and thresholds, for all records.
# Input file: output from Umea birth sibling bundling ground truth:
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthSibling
# run over all records with 10 repetitions

# TODO: update with location of archived input file on manifesto

##########################################################################
# Edit these paths appropriately.

INPUT_FILE_PATH <- "~/Desktop/UmeaBirthSiblingPRFByThreshold-filtered.csv"
OUTPUT_FILE_PATH <- "~/Desktop/figure4.png"
PROJECT_DIRECTORY_PATH <- "~/Documents/Code/github/population-linkage"
##########################################################################

R_DIRECTORY_RELATIVE_PATH <- "src/main/scripts/R"

setwd(paste(PROJECT_DIRECTORY_PATH, R_DIRECTORY_RELATIVE_PATH, sep = "/"))
source("umea-paper/common.R")

X_UPPER_BOUND <- 227889
X_AXIS_LABEL <- "Records processed"
Y_AXIS_LABEL <- "Absolute error in F1-measure"
COLOUR <- "black"

plot <- plotAllFMeasureErrorConvergence(data, X_UPPER_BOUND, X_AXIS_LABEL, Y_AXIS_LABEL, COLOUR)

ggsave(OUTPUT_FILE_PATH, plot, dpi = IMAGE_DPI, width = X_IMAGE_WIDTH, height = Y_IMAGE_WIDTH, units = IMAGE_SIZE_UNITS)
