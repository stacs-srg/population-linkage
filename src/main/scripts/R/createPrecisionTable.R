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

filename <- bd

# Takes something like Sigma2-Levenshtein-16-20-14-15--13-19-11-12 and returns Levenshtein
reduceMetricName <- function( str) {
  
  return( strsplit( str,"-" )[[1]][2] )
}

# Takes a name like "UmeaBrideBrideSiblingPRFByThreshold.csv" and returns "BrideBrideSibling"
reduceSourceName <- function( filename ) {
  indexOfPRF <- regexpr( "PRF", filename )
  str <- substring( filename,5,indexOfPRF -1 )
}

getMaxProcessed <- function( filename ) {
  
  conditionLoadIntoGlobal( paste(path,filename,sep=""), "mydata" )
  
  as.numeric(mydata$f_measure)
  as.numeric(mydata$recall)
  as.numeric(mydata$precision)
  as.numeric(mydata$threshold)
  max_processed <- max( mydata$records.processed )
  
  extracted <- mydata[ which( mydata$records.processed == max_processed ), ]
  
  return( extracted )
}

createPrecisionTable <- function( filename ) {

  subsetted <- getMaxProcessed( filename )

  results <- data.frame(metric=character(),
                        threshold=double(),
                        precision=double(),
                        stringsAsFactors=FALSE)

  for( metric in unique( subsetted$metric ) ) {
  
    for( thresh in unique( subsetted$threshold ) ) {
    
      metric_and_thresh  <- subsetted[ which( subsetted$metric == metric ), ]
      metric_and_thresh <- metric_and_thresh[ which( metric_and_thresh$threshold == thresh ), ]
    
      results[ nrow(results)+1,"metric" ] <- metric # reduceMetricName( metric )
      results[ nrow(results),"threshold" ] <- thresh
      results[ nrow(results),"precision" ] <- metric_and_thresh$precision
    }
  }
  
  return( results )
}
  
results <- createPrecisionTable( filename )
write.csv(results,paste0( "/tmp/",reduceSourceName( filename ),"-precision",".csv"), row.names = TRUE)
