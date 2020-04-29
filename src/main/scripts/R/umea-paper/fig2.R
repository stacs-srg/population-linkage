# Figure 2: evolution of F1 measure for specific metric and thresholds.
# Input file: output from Umea birth sibling bundling ground truth:
# uk.ac.standrews.cs.population_linkage.groundTruth.umea.UmeaBirthSibling
# run over all records with 10 repetitions

# TODO: update with location of archived input file on manifesto

##########################################################################
# Edit these paths appropriately.

INPUT_FILE_PATH <- "~/Desktop/UmeaBirthSiblingPRFByThreshold-filtered.csv"
OUTPUT_FILE_PATH <- "~/Desktop/figure2.png"
PROJECT_DIRECTORY_PATH <- "~/Documents/Code/github/population-linkage"
##########################################################################

R_DIRECTORY_RELATIVE_PATH <- "src/main/scripts/R"

setwd(paste(PROJECT_DIRECTORY_PATH, R_DIRECTORY_RELATIVE_PATH, sep = "/"))
source("functionBank.R")
conditionLoadIntoGlobal(INPUT_FILE_PATH, "data")

METRIC <- "Sigma-Levenshtein"
THRESHOLDS <- c(0.4, 0.6, 0.8)
X_UPPER_BOUND <- 250000
X_AXIS_LABEL <- "Records processed"
Y_AXIS_LABEL <- "F1-measure"
COLOURS <- c("firebrick2", "dodgerblue2", "green3")

# The output warning "Removed ... row(s) containing missing values (geom_path)" is harmless,
# indicating that data with x > X_UPPER_BOUND is ignored.
plot <- plotFMeasureConvergence(data, METRIC, THRESHOLDS, X_UPPER_BOUND, X_AXIS_LABEL, Y_AXIS_LABEL, COLOURS)

ggsave(OUTPUT_FILE_PATH, plot, dpi = 320, width = 20, height = 20, units = "cm")
