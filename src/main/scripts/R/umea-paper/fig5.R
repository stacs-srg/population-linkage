# Figure 5: evolution of F1 measure error for all metrics and thresholds, for first 25,000 records.
# Input file: output from Umea birth sibling bundling ground truth:
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthSibling
# run over all records with 10 repetitions

# TODO: update with location of archived input file on manifesto

##########################################################################
# Edit these paths appropriately.

INPUT_FILE_PATH <- "~/Desktop/UmeaBirthSiblingPRFByThreshold-filtered.csv"
OUTPUT_FILE_PATH <- "~/Desktop/figure5.png"
PROJECT_DIRECTORY_PATH <- "~/Documents/Code/github/population-linkage"
##########################################################################

R_DIRECTORY_RELATIVE_PATH <- "src/main/scripts/R"

setwd(paste(PROJECT_DIRECTORY_PATH, R_DIRECTORY_RELATIVE_PATH, sep = "/"))
source("functionBank.R")
conditionLoadIntoGlobal(INPUT_FILE_PATH, "data")

X_UPPER_BOUND <- 25000
X_AXIS_LABEL <- "Records processed"
Y_AXIS_LABEL <- "Absolute error in F1-measure"
COLOUR <- "black"

plot <- plotAllFMeasureErrorConvergence(data, X_UPPER_BOUND, X_AXIS_LABEL, Y_AXIS_LABEL, COLOUR)

ggsave(OUTPUT_FILE_PATH, plot, dpi = 320, width = 20, height = 20, units = "cm")
