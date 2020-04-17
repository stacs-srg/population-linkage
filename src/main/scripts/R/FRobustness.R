source("FunctionBank.R")
source("inputFiles.R")

heightvswidth <- function( metric, subsetted ) {
  
  results <- data.frame(threshold=double(),
                        f=double(),
                        stringsAsFactors=FALSE)
  
  for( thresh in unique( subsetted$threshold ) ) {
    
    f_for_thresh  <- subsetted[ which( subsetted$metric == metric ), ]
    f_for_thresh <- f_for_thresh[ which( f_for_thresh$threshold == thresh ), ]
    
    results[ nrow(results)+1,"threshold" ] <- thresh
    results[ nrow(results),"f" ] <- max( f_for_thresh$f_measure )
    
    results <- results[with(results, order(threshold)), ] # sort in threshold order
  }
  
  return( results )
}

process_data <- function( filename ) {
  conditionLoadIntoGlobal( paste(path,filename,sep=""), "mydata" )

  as.numeric(mydata$f_measure)
  as.numeric(mydata$recall)
  as.numeric(mydata$precision)
  as.numeric(mydata$threshold)
  max_processed <- max( mydata$records.processed )

  threshold_interval = 0.01

  subsetted <- mydata[ which( mydata$records.processed == max_processed ), ]

  results_frame <- data.frame(metric=character(),
                            f_max=double(),
                            thresh_start=double(),
                            thresh_f_max=double(),
                            thresh_end=double(),
                            count=integer(),
                            power=double(),
                            thresh_fmax_unnormalised=double(),
                            stringsAsFactors=FALSE)

  for( metric in unique( subsetted$metric ) ) {
    profiles <- heightvswidth( metric, subsetted )
  
    f_max = max( profiles$f )
    best_row <- profiles[ which( profiles$f == f_max ), ]
    best_thresh = best_row$threshold[1]
  
    lower_limit <- f_max[1] * 0.95

    count = 0;
    first = FALSE;
  
    first_threshold_gt_limit <- 0;
    last_threshold_gt_limit <- 0;
  
    for( thresh in unique( profiles$threshold ) ) {
    
      row = profiles[ which( profiles$threshold == thresh ), ]
    
      if( row$f > lower_limit ) {
        count = count + 1;
        if( first_threshold_gt_limit == 0 ) {
          first_threshold_gt_limit <- row$threshold
        }
        last_threshold_gt_limit <- row$threshold 
      }
    
    }
      results_frame[ nrow(results_frame)+1,"metric" ] <- metric
      results_frame[ nrow(results_frame),"f_max" ] <- f_max
      results_frame[ nrow(results_frame),"thresh_start" ] <- first_threshold_gt_limit
      results_frame[ nrow(results_frame),"thresh_f_max" ] <- best_thresh
      results_frame[ nrow(results_frame),"thresh_end" ] <- last_threshold_gt_limit
      results_frame[ nrow(results_frame),"count" ] <- count
      results_frame[ nrow(results_frame),"power" ] <- count * f_max
      results_frame[ nrow(results_frame),"thresh_fmax_unnormalised" ] <- ( 1 / ( 1 - best_thresh ) ) - 1
  }

  results_frame <- results_frame[with(results_frame, order(-power,-f_max)), ] # sort in power then f_max order
  print( results_frame )
}

#filename <- bm
#process_data( filename )

for( filename in filenames ) {
  rm( list=c("mydata"))
  print( filename )
  process_data( filename )
}
  

