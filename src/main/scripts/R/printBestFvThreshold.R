setwd("~/repos/github/population-linkage/src/main/scripts/R")
source("FunctionBank.R")

path <- "~/repos/github/population-linkage/src/main/resources/"

bm <- "UmeaBirthMotherPRFByThreshold.csv"
bf <- "UmeaBirthFatherPRFByThreshold.csv"
bfaf <- "UmeaBirthFatherAgeFilteredPRFByThreshold.csv"
bs <- "UmeaBirthSiblingPRFByThreshold.csv"

db <- "UmeaDeathBirthPRFByThreshold.csv"
ds <- "UmeaDeathSiblingPRFByThreshold.csv"

gb <- "UmeaGroomBirthPRFByThreshold.csv"
gp <- "UmeaGroomParentsPRFByThreshold.csv"

bb <- "UmeaBrideBirthPRFByThreshold.csv"
bp <- "UmeaBrideParentsPRFByThreshold.csv"

bgs <- "UmeaBrideGroomSiblingPRFByThreshold.csv"
bbs <- "UmeaBrideBrideSiblingPRFByThreshold.csv"
ggs <- "UmeaGroomGroomSiblingPRFByThreshold.csv"
gbs <- "UmeaGroomBrideSiblingPRFByThreshold.csv"

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
  

