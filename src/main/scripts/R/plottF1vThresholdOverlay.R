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

# This code used to create the figures 8-14 the overlayed non faced images in the paper.


setwd("~/repos/github/population-linkage/src/main/scripts/R")
source("FunctionBank.R")

path <- "~/repos/github/population-linkage/src/main/resources/"

bm <- "UmeaBirthMotherPRFByThreshold.csv"
bf <- "UmeaBirthFatherPRFByThreshold.csv"
bs <- "UmeaBirthSiblingPRFByThreshold.csv"

db <- "UmeaBirthDeathPRFByThreshold.csv"
ds <- "UmeaDeathSiblingPRFByThreshold.csv"

gb <- "UmeaGroomBirthPRFByThreshold.csv"
bb <- "UmeaBrideBirthPRFByThreshold.csv"

bbs <- "UmeaBrideBrideSiblingPRFByThreshold.csv"
ggs <- "UmeaGroomGroomSiblingPRFByThreshold.csv"
gbs <- "UmeaGroomBrideSiblingPRFByThreshold.csv"

bdv <- "UmeaBirthDeathViabilityPRFByThreshold.csv"
bfv <- "UmeaBirthFatherViabilityPRFByThreshold.csv"
bmv <- "UmeaBirthMotherViabilityPRFByThreshold.csv"
bsv <- "UmeaBirthSiblingViabilityPRFByThreshold.csv"
bbsv <- "UmeaBrideBrideSiblingViabilityPRFByThreshold.csv"
bgsv <- "UmeaBrideGroomSiblingViabilityPRFByThreshold.csv"
dsv <- "UmeaDeathSiblingViabilityPRFByThreshold.csv"
gbv <- "UmeaGroomBirthViabilityPRFByThreshold.csv"
gbsv <- "UmeaGroomBrideSiblingViabilityPRFByThreshold.csv"
ggsv <- "UmeaGroomGroomSiblingViabilityPRFByThreshold.csv"

bbv <- "UmeaBrideBirthViabilityPRFByThreshold.csv"

filenames <- c( bdv,bfv,bmv,bsv,bbsv,bgsv,dsv,gbv,gbsv,ggsv, bbv )

#filenames <- c( bm,bf,bs,db,gb,bb,bbs,ggs,gbs,ds )

process_data <- function( filename ) {
  
  conditionLoadIntoGlobal( paste(path,filename,sep=""), "mydata" )
  
  as.numeric(mydata$f_measure)
  as.numeric(mydata$recall)
  as.numeric(mydata$precision)
  as.numeric(mydata$threshold)
  max_processed <- max( mydata$records.processed )
  
  subsetted <- mydata[ which( mydata$records.processed == max_processed ), ]
  
  return( plot( subsetted,filename ) )
}
  
  
# Takes something like Sigma2-Levenshtein-16-20-14-15--13-19-11-12 and returns Levenshtein
reduce <- function( str) {
  
  return( strsplit( str,"-" )[[1]][2] )
}

#extract experiment name from filename
expt_name <- function( filename ) {
  name <-substr( filename, 5, nchar(filename))
  name <- strsplit( name,"P" )[[1]]  # eliminate text after PRF
  return( strsplit( name,"Viability" )[[1]] )  # eliminate Viability at end
}

plot <- function( subsetted, filename ) {
  
  results <- data.frame(metric=character(),
                        threshold=double(),
                        best_f=double(),
                        stringsAsFactors=FALSE)
  
  for( metric in unique( subsetted$metric ) ) {
    
    for( thresh in unique( subsetted$threshold ) ) {
      
      select_f_thresh  <- subsetted[ which( subsetted$metric == metric ), ]
      select_f_thresh <- select_f_thresh[ which( select_f_thresh$threshold == thresh ), ]
      
      f_max <- max( select_f_thresh$f_measure )
      
      results[ nrow(results)+1,"metric" ] <- metric
      results[ nrow(results),"threshold" ] <- thresh
      results[ nrow(results),"best_f" ] <- f_max
      
      results <- results[with(results, order(threshold)), ] # sort in threshold order
    }
  }
  
  labels <- unique( subsetted$metric )
  labels <- lapply( labels, reduce)
  
    cbPalette <- c( '#ffcc00','#CC66FF','#99cc00','#cc9900','#ff6633',
                    '#333300','#ff3366','#336633','#ffff33','#cc3399',
                    '#3399ff','#33cccc','#ff00CC','#00ffcc','#000000' ) #  from http://www.cookbook-r.com/Graphs/Colors_(ggplot2)/ 
    
  palette( cbPalette )
  gg <- ggplot( results, aes( x=threshold ) ) +
    geom_line( aes( y=best_f, colour=as.factor(results$metric)), show.legend=T ) +
    ggtitle( paste( "Threshold vs F1 measure for",expt_name( filename ) ) ) +
    theme(legend.position="bottom") +
    ylab( "F1 measure" ) +
    ylim(0,1) +
    xlab( "threshold") +
    guides(colour=guide_legend(ncol=4)) +  # 4 columns in legend
    labs( color="") + # no label on legend!
    scale_colour_manual(values=cbPalette) # +
#    facet_wrap(~metric)
  
  return(gg)
}

for( f in filenames ) {
  rm( list=c("mydata"))
  ggsave( paste( "/tmp/", f, ".png", sep="" ),process_data( f ) )
}

  

