source("FunctionBank.R")
source("inputFiles.R")

filename <- bbs

conditionLoadIntoGlobal( paste(path,filename,sep=""), "mydata" )

as.numeric(mydata$f_measure)
as.numeric(mydata$recall)
as.numeric(mydata$precision)
as.numeric(mydata$threshold)
max_processed <- max( mydata$records.processed )

subsetted <- mydata[ which( mydata$records.processed == max_processed ), ]

plot <- function( metric ) {
  
  results <- data.frame(metric=character(),
                        threshold=double(),
                        best_f=double(),
                        stringsAsFactors=FALSE)
  
  for( thresh in unique( subsetted$threshold ) ) {
  
      select_f_thresh  <- subsetted[ which( subsetted$metric == metric ), ]
      select_f_thresh <- select_f_thresh[ which( select_f_thresh$threshold == thresh ), ]

      # print( length( select_f_thresh$f_measure ))
      f_max <- max( select_f_thresh$f_measure )   # this line is not necessary - always one f measure per threshold value!
  
      results[ nrow(results)+1,"metric" ] <- metric
      results[ nrow(results),"threshold" ] <- thresh
      results[ nrow(results),"best_f" ] <- f_max
      
      results <- results[with(results, order(threshold)), ] # sort in threshold order
  }

  #  These settings for fig 7/8
  
  gg <- ggplot( results, aes( x=threshold ) ) +
    geom_line( aes( y=best_f) ) +
    ggtitle( paste( "Threshold vs F-measure for ",metric ) ) +
    ylab( "F-measure" ) +
    ylim(0,1) +
    xlab( "threshold")  +
    geom_vline(aes(xintercept=0.72), color="blue", linetype="dashed", size=0.5) +
    geom_vline(aes(xintercept=0.82), color="blue", linetype="dashed", size=0.5)
  
#  was 00, 09 for other link (fig5)
  
  return(gg)
}

for( metric in unique( subsetted$metric ) ) { print( plot(metric) ) }
  

