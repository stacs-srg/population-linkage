INPUT_FILE_PATH <- "~/Desktop/UmeaBirthSiblingPRFByThreshold-filtered.csv"
OUTPUT_FILE_PATH <- "~/Desktop/figure3.png"

PROJECT_DIRECTORY_PATH <- "~/Documents/Code/github/population-linkage"
R_DIRECTORY_RELATIVE_PATH <- "src/main/scripts/R"

source(paste(PROJECT_DIRECTORY_PATH, R_DIRECTORY_RELATIVE_PATH, "functionBank.R", sep = "/"))
conditionLoadIntoGlobal(INPUT_FILE_PATH, "plotdata")

METRIC <- "Sigma-Levenshtein"
THRESHOLD <- 0.8
X_UPPER_BOUND <- 25000
X_LABEL <- "Records processed"
Y_LABEL <- "Absolute error in F1-measure"

# The output warning "Removed ... row(s) containing missing values (geom_path)" is harmless,
# indicating that data with x > X_UPPER_BOUND is ignored.
plot <- plotFMeasureConvergence(METRIC, THRESHOLD, X_UPPER_BOUND, X_LABEL, Y_LABEL)

ggsave(OUTPUT_FILE_PATH, plot, dpi = 320, width = 20, height = 20, units = "cm")
