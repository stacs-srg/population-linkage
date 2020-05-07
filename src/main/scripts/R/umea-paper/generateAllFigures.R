# Generates all data plot figures for Umea paper.

PROJECT_DIRECTORY_PATH <- "~/Documents/Code/github/population-linkage"
R_DIRECTORY_RELATIVE_PATH <- "src/main/scripts/R"

setwd(paste(PROJECT_DIRECTORY_PATH, R_DIRECTORY_RELATIVE_PATH, sep = "/"))

source("umea-paper/fig2.R")
source("umea-paper/fig3.R")
source("umea-paper/fig4.R")
source("umea-paper/fig5.R")
source("umea-paper/fig6-25.R")
