setwd("~/repos/github/population-linkage/src/main/scripts/R")
source("FunctionBank.R")

conditionLoadIntoGlobal( "~/repos/github/population-linkage/src/main/resources/UmeaThresholdBirthSiblingLinkage.csv", "mydata" )

mydata$precision <- as.numeric(mydata$precision)
mydata$recall <- as.numeric(mydata$recall)
mydata$f_measure <- as.numeric(mydata$f_measure)

# This plots each of the the metrics on a different plot
plotconvergencefacets <- function( plotdata ) {
  #plotdata <- plotdata[ which( plotdata$threshold == 0.2 ), ]
  #plotdata <- plotdata[ which( plotdata$metric == "Sigma-Levenshtein" ), ]
  
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






  

