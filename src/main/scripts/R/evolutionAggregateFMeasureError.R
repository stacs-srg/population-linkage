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

# This program used to generate the convergence graphs in the paper.
# Currently these are numbered 4 & 5 and have titles "Evolution of aggregate errors in F1-measure..."

INPUT_FILE_PATH <- "/Users/al/Documents/Current/Results/2019-03-08-Umea-full-nXn/UmeaBirthSiblingLPRFByThreshold-full.csv"
OUTPUT_DIRECTORY_PATH <- "/tmp/"

source("FunctionBank.R")

conditionLoadIntoGlobal(INPUT_FILE_PATH, "mydata" )

xlimits <- c(25000,227889)

combinedstats <- summarySE(mydata, summarised_column_name="f_measure", grouping_variable_column_names=c("metric", "threshold", "records.processed")) # calculate mean,stdev,stderr

for (xlim in xlimits)
  plotAllZeroPlots(combinedstats, "f_measure", OUTPUT_DIRECTORY_PATH, xlim)
