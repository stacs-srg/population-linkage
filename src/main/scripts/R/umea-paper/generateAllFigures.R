# Generate all figures for Umea paper.

PROJECT_DIRECTORY_PATH <- "~/Documents/Code/github/population-linkage"
R_DIRECTORY_RELATIVE_PATH <- "src/main/scripts/R"

setwd(paste(PROJECT_DIRECTORY_PATH, R_DIRECTORY_RELATIVE_PATH, sep = "/"))
source("functionBank.R")

rm(list = ls())
source("umea-paper/fig2.R")
rm(list = ls())
source("umea-paper/fig3.R")
rm(list = ls())
source("umea-paper/fig4.R")
rm(list = ls())
source("umea-paper/fig5.R")
rm(list = ls())
