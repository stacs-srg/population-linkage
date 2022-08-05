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

setwd("~/Documents/Code/github/population-linkage/src/main/scripts/R")

source("functionBank.R")

conditionLoadIntoGlobal( "../../resources/UmeaBirthFatherIdentityPRFByThreshold.csv", "mydata" )

mydata$precision <- as.numeric(mydata$precision)
mydata$recall <- as.numeric(mydata$recall)
mydata$f_measure <- as.numeric(mydata$f_measure)

RUNS <- 10
thresholds <- c( 0.4, 0.6, 0.8 )
xlim <- 25000

metric <- "Sigma2-Damerau-Levenshtein-2-4--7-8"
subsetted <- mydata[which(mydata$run.number<RUNS),]    # takes first N runs for each metric, 10 is all of them.

# Plot graphs for f_measure
fff <- summarySE(subsetted, summarised_column_name="f_measure", grouping_variable_column_names=c("metric", "threshold", "records.processed")) # calculate mean,stdev,stderr

plotmetrics( fff, "f_measure", xlim ,metric, thresholds ) 

rrr <- summarySE(subsetted, summarised_column_name="recall", grouping_variable_column_names=c("metric", "threshold", "records.processed")) # calculate mean,stdev,stderr

plotmetrics( rrr, "recall", xlim ,metric, thresholds ) 

ppp <- summarySE(subsetted, summarised_column_name="precision", grouping_variable_column_names=c("metric", "threshold", "records.processed")) # calculate mean,stdev,stderr

plotmetrics( ppp, "precision", xlim ,metric, thresholds ) 


