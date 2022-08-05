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

conditionLoadIntoGlobal( "~/repos/github/population-linkage/src/main/resources/UmeaThresholdBirthSiblingLinkage.csv", "mydata" )

mydata$precision <- as.numeric(mydata$precision)
mydata$recall <- as.numeric(mydata$recall)
mydata$f_measure <- as.numeric(mydata$f_measure)

# This plots each of the the metrics on a different plot
plotconvergencefacets <- function( plotdata ) {
  #plotdata <- plotdata[ which( plotdata$threshold == 0.2 ), ]
  #plotdata <- plotdata[ which( plotdata$metric == "SigmaMissingOne-Levenshtein" ), ]
  
  pp <- ggplot(data=plotdata,aes(x=records.processed)) +
    #theme(legend.position="bottom") +
    #ggtitle("closeness") +
    scale_x_discrete( limits=0:228000, breaks = seq( 0,40000,10000)) +
    scale_y_discrete( breaks = seq( 0,0.1,0.01))
  
    for( met in unique( plotdata$metric ) ) {
      for( thresh in unique( plotdata$threshold ) ) {
        pp <- pp + geom_line( aes( y=closeness,
                                   colour=interaction(plotdata$metric,plotdata$threshold),
                                   group=met )) #interaction(metric,threshold) ) )
      }
    }
    return( pp +
            facet_grid( metric ~ threshold ) +
            # coord_flip() ) +
            theme(legend.position = "none") ) 
}

closeness <- analyse_space(mydata,"f_measure")
plot <- plotconvergencefacets(closeness)
print( plot )






  

