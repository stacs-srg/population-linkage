source("FunctionBank.R")
source("inputFiles.R")

filenames <- c( bf,bm,bd,ds,bs  )

process_data <- function( filename ) {
  
  conditionLoadIntoGlobal( paste(path,filename,sep=""), "mydata" )
  
  as.numeric(mydata$f_measure)
  as.numeric(mydata$recall)
  as.numeric(mydata$precision)
  as.numeric(mydata$threshold)
  max_processed <- max( mydata$records.processed )
  
  subsetted <- mydata[ which( mydata$records.processed == max_processed ), ]
  
#  return( plot( subsetted,filename ) )
  return( plot( subsetted,filename ) )
}
  
# Takes something like Sigma2-Levenshtein-16-20-14-15--13-19-11-12 and returns Levenshtein
reduce <- function( str) {
  
  return( strsplit( str,"-" )[[1]][2] )
}

plot <- function( subsetted, filename ) {
  
  results <- data.frame(metric=character(),
                        threshold=double(),
                        best_f=double(),
                        stringsAsFactors=FALSE)
  
  for( metric in unique( subsetted$metric ) ) {
    
    for( thresh in unique( subsetted$threshold ) ) {
      
      select_r_thresh  <- subsetted[ which( subsetted$metric == metric ), ]
      select_r_thresh <- select_r_thresh[ which( select_r_thresh$threshold == thresh ), ]
      
      r_max <- max( select_r_thresh$recall )
      
      results[ nrow(results)+1,"metric" ] <- metric
      results[ nrow(results),"threshold" ] <- thresh
      results[ nrow(results),"recall" ] <- r_max
      
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
    geom_line( aes( y=recall, colour=as.factor(results$metric)), show.legend=T ) +
    ggtitle( paste( "Threshold vs Recall for",filename ) ) +
    theme(legend.position="bottom") +
    ylab( "recall" ) +
    ylim(0,1) +
    xlab( "threshold") +
    scale_colour_manual(values=cbPalette) # +
  # facet_wrap(~metric) # add in facet wrap to put on sepearate graphs.
  
  
  return(gg)
}

for( f in filenames ) {
  rm( list=c("mydata"))
  ggsave( paste( "/tmp/", f, ".png", sep="" ),process_data( f ) )
}
