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

source("FunctionBank.R")
source("inputFiles.R")

filename <- bs

conditionLoadIntoGlobal( paste(path,filename,sep=""), "mydata" )

as.numeric(mydata$f_measure)
as.numeric(mydata$recall)
as.numeric(mydata$precision)
as.numeric(mydata$threshold)
max_processed <- max( mydata$records.processed )

subsetted <- mydata[ which( mydata$records.processed == max_processed ), ]

printBestF <- function( metric ) {
  
  results <- data.frame(metric=character(),
                        threshold=double(),
                        best_f=double(),
                        stringsAsFactors=FALSE)
  
  for( thresh in unique( subsetted$threshold ) ) {
  
      select_f_thresh  <- subsetted[ which( subsetted$metric == metric ), ]
      select_f_thresh <- select_f_thresh[ which( select_f_thresh$threshold == thresh ), ]

      f_max <- max( select_f_thresh$f_measure )
  
      results[ nrow(results)+1,"metric" ] <- metric
      results[ nrow(results),"threshold" ] <- thresh
      results[ nrow(results),"best_f" ] <- f_max
      
      results <- results[with(results, order(-best_f)), ] # sort in best_f order
  }
  
  print( paste( results[ 1,"metric" ], results[ 1,"threshold" ], results[ 1,"best_f" ] ) )
}

for( metric in unique( subsetted$metric ) ) { printBestF(metric) }
