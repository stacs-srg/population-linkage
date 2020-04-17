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

printLinkageMetricPowerTable <- function( results_frame ) {
  
  for( filename in unique( results_frame$filename ) ) {
    
    for( metric in unique( results_frame$metric ) ) {
      
      row = results_frame[ which( results_frame$metric == metric & results_frame$filename == filename ), ]
      
      if( nrow(row) == 1 ) {
        print( paste( filename, metric, row$power ) )
      }
    }
  }
}

process_data <- function( filename ) {
  
 # print( filename )
  
  conditionLoadIntoGlobal( paste(path,filename,sep=""), "mydata" )
  
  as.numeric(mydata$f_measure)
  as.numeric(mydata$recall)
  as.numeric(mydata$precision)
  as.numeric(mydata$threshold)
  max_processed <- max( mydata$records.processed )
  
  threshold_interval = 0.01
  
  subsetted <- mydata[ which( mydata$records.processed == max_processed ), ]
  
  tmp_results <- data.frame(metric=character(),
                            filename=character(),
                            f_max=double(),
                            thresh_start=double(),
                            thresh_f_max=double(),
                            thresh_end=double(),
                            power=double(),
                            fudge=double(),
                            thresh_fmax_unnormalised=double(),
                            stringsAsFactors=FALSE)
  
  for( metric in unique( subsetted$metric ) ) {
    profiles <- heightvswidth( metric, subsetted )
    
    f_max = max( profiles$f )
    
    best_thresh <- round( mean( profiles$threshold[profiles$f == f_max]),2 )   #  take the mean threshold value for which we get f_max
    
    measurement_interval = 0.01  # the threshold measurement interval
    threshold_interval <- 0.05  # the interval either side of the threshold value that yields f_max for which we calculate power.
    
    first_threshold <- best_thresh - threshold_interval
    last_threshold <- best_thresh + threshold_interval
    
    if( first_threshold < 0 ) {  # keep thresholds in 0..1 range.
      first_threshold = 0
    }
    if( last_threshold > 1 ) {
      last_threshold = 1
    }
    
    interval <- profiles[ which( profiles$threshold > first_threshold & profiles$threshold < last_threshold ), ]
    power <- sum( interval$f  )
    
    tmp_results[ nrow(tmp_results )+1,"metric" ] <- metric
    tmp_results[ nrow(tmp_results ),"filename" ] <- filename
    tmp_results[ nrow(tmp_results ),"f_max" ] <- f_max
    tmp_results[ nrow(tmp_results ),"thresh_start" ] <- first_threshold
    tmp_results[ nrow(tmp_results ),"thresh_f_max" ] <- best_thresh
    tmp_results[ nrow(tmp_results ),"thresh_end" ] <- last_threshold
    tmp_results[ nrow(tmp_results ),"power" ] <- power
    tmp_results[ nrow(tmp_results ),"fudge" ] <- (5 * f_max) - power
    tmp_results[ nrow(tmp_results),"thresh_fmax_unnormalised" ] <- ( 1 / ( 1 - best_thresh ) ) - 1
  }
  
  tmp_results  <- tmp_results[with(tmp_results, order(-fudge,-power,-f_max)), ] # sort in power then f_max order
  return( tmp_results  )
}

all_results <- data.frame(metric=character(),
                            filename=character(),
                            f_max=double(),
                            thresh_start=double(),
                            thresh_f_max=double(),
                            thresh_end=double(),
                            power=double(),
                            fudge=double(),
                            stringsAsFactors=FALSE)



#filename <- bm
#process_data( filename )

for( filename in filenames ) {
  rm( list=c("mydata"))
  next_batch <- process_data( filename )
  all_results <- rbind(all_results, next_batch)
}

printLinkageMetricPowerTable( all_results )

all_results <- all_results[with(all_results, order(-fudge,-power,-f_max)), ] # sort in power then f_max order
options(max.print=1000000)
print( all_results ) 

