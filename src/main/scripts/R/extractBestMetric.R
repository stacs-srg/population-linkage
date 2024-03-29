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

setwd("~/repos/github/population-linkage/src/main/scripts/R")
source("FunctionBank.R")

path <- "~/repos/github/population-linkage/src/main/resources/"
f1 <- "UmeaBirthFatherIdentityPRFByThreshold.csv"
f2 <- "UmeaBirthSiblingPRFByThreshold.csv"

filename <- f2

conditionLoadIntoGlobal( paste(path,filename,sep=""), "mydata" )

max_processed <- max( mydata$records.processed )
subsetted <- mydata[ which( mydata$records.processed == max_processed ), ]
as.numeric(mydata$f_measure)
as.numeric(mydata$recall)
as.numeric(mydata$precision)

colMax <- function(data) sapply(data, max, na.rm = TRUE)
colMin <- function(data) sapply(data, min, na.rm = TRUE)

results <- data.frame(metric=character(),
                      min_threshold=double(),
                      max_threshold=double(),
                      best_f=double(),
                      stringsAsFactors=FALSE)

for( metric in unique( mydata$metric ) ) {

  best_f_for_metric <- subsetted[ which( mydata$metric == metric ), ]
  best_f_for_metric <- best_f_for_metric[ , c( "threshold","precision","recall","f_measure" ) ]
  
  max <- colMax( best_f_for_metric )
  f_max <- max[ "f_measure" ]
  
  results[ nrow(results)+1,"metric" ] <- metric
  results[ nrow(results),"best_f" ] <- f_max
  results[ nrow(results),"max_threshold" ] <- max( best_f_for_metric[ which(best_f_for_metric$f_measure==f_max ), ]$threshold )
  results[ nrow(results),"min_threshold" ] <- min( best_f_for_metric[ which(best_f_for_metric$f_measure==f_max ), ]$threshold )
  
  results <- results[with(results, order(-best_f)), ] # sort in descending order

}

print( filename )
print( results )
  