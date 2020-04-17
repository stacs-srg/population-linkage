library("ggplot2")
library("stringr")
library("scales")

source( "utils.R" )
source( "dataManipulation.R")

# This returns a plot of Umea data with metric Sigma Levenshtein threhold 0.8 with error bars.
SL0.8AbsError <- function( mydata, measure ) {
  plotdata <- mydata
  plotdata <- plotdata[ which( plotdata$threshold==0.8 ), ]
  plotdata <- plotdata[ which( plotdata$metric=="Sigma-Levenshtein" ), ]
  plotdata <- plotdata[ order( plotdata$records.processed ), ]
  plotdata$recall <- recall( plotdata$tp, plotdata$fn )
  plotdata$precision <- precision( plotdata$tp, plotdata$fp )
  plotdata$f_measure <- fmeasure( plotdata$precision, plotdata$recall )
  
  final_measure <- plotdata[nrow(plotdata),measure]
  plotdata[, "abs_diff"] <- abs( final_measure - plotdata[,measure] )
  
  plotdata <- summarySE(plotdata, measurevar= "abs_diff", groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr
  
  plot <- ggplot( plotdata, aes( x=records.processed ) ) +
    geom_line( aes( y=abs_diff ) ) +
    geom_errorbar( aes( ymin=abs_diff-ci, ymax=abs_diff+ci ),width=1 ) +
    #  geom_line( aes( y=ci  ) ) +
    scale_x_continuous( minor_breaks=seq(0,max(plotdata$records.processed),10000 ), labels=scales::comma) +
    ggtitle( "Absolute Errors in F_measure" ) +
    ylab( "Absolute difference between measured and known value") +
    xlab( "Records processed") +
    xlim( 0,20000 )
  
  return(plot)
}

# This returns a new data frame containing the metric,threshold, closeness, records processed derived from the data parmeter for the supplied measure 
analyse_space <- function( data, measure ) {
  
  thresholds <- c( 0.4, 0.6, 0.8 )
  
  closeness.df <- data.frame( metric = character(),
                              threshold = double(),
                              closeness = double(),
                              records.processed = integer(),
                              stringsAsFactors=FALSE )
  
  for( metric in unique(data$metric))
    for( threshold in thresholds ) {
      current_subset <- select_records.processed.ci( data,measure,threshold,metric ) # a table of records.processed and Cis of absolute differences of measue from final
      
      for( closeness_tolerance in seq( 0, 0.1, 0.0001 ) ) {
        
        number <- find_stable_close_enough_to_observed( current_subset, closeness_tolerance )
        if( ! is.na(number) ) {
          closeness.df[ nrow(closeness.df)+1,"metric" ] <- metric
          closeness.df[ nrow(closeness.df),"threshold" ] <- threshold
          closeness.df[ nrow(closeness.df),"closeness" ] <- closeness_tolerance
          closeness.df[ nrow(closeness.df),"records.processed" ] <- number
        }
      }
    }
  return( closeness.df )
}

# Select the records from df with fields equal to measure, thresh and metric and calculate the confidence intervals based on final value of
select_records.processed.ci <- function(df, measure, thresh, metric) {
  # ci_s is a table in which we are interested the confidence intervals
  ci_s <- df[which(df$metric == metric),]  # select a single metric
  ci_s <- ci_s[ which( ci_s$threshold == thresh), ] # select a threshold
  
  ci_s <- ci_s[ order( ci_s$records.processed ), ] #  order by records.processed
  final_measure <- ci_s[nrow(ci_s),measure]   # find the last value for the measure supplied as a param
  ci_s[, "abs_diff"] <- abs( final_measure - ci_s[,measure] )  #add a column with the max abs differences from final_measure
  
  ci_s <- summarySE(ci_s, measurevar="abs_diff", groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr
  ci_s <- ci_s[,c( "records.processed","ci","abs_diff" ) ] 
  
  return( ci_s )
}

# find the first x value for which the modelled y value is less than required_threshold
find_first_that_close_enough_to_model <- function( data, a_hat,k_hat,limit, how_close_is_close_enough ) {
  
  for( record_count in unique(data$records.processed) ) {
    vbt_value <- eval_vbt( record_count,a_hat,k_hat,limit )
    close_enough <- vbt_value - limit 
    if( close_enough < how_close_is_close_enough ) {
      print( paste( "iterations,vbt_value,closeness: ", record_count, vbt_value, close_enough ) )
      return( record_count )
    }
  }
  print( paste( "Cannot achive required_distance of", how_close_is_close_enough ) )
  return( NA )
}

# find the first x value for which the y value is less than required_threshold
find_first_close_enough_to_observed <- function( data, required_threshold, min_y_value ) {
  
  for( record_count in unique(data$records.processed) ) {
    observed_ci <- data[which(data$records.processed == record_count), ]$ci   #eval_vbt( record_count,a_hat,k_hat,limit )
    if( is.null(observed_ci) && is.na(observed_ci) && is.nan(observed_ci) ) {
      return( NA )
    }
    difference <- observed_ci - min_y_value 
    if( difference < required_threshold ) {
      return( record_count )
    }
  }
  return( NA )
}

# find the minimum x value after which the y value is never greater than required_threshold
find_stable_close_enough_to_observed <- function( data, required_threshold ) {
  
  result = 0
  
  for( record_count in unique(data$records.processed) ) {
    observed_ci <- data[which(data$records.processed == record_count), ]$ci
    mean <- data[which(data$records.processed == record_count), ]$abs_diff  
    
    if( is.null( observed_ci ) || is.na( observed_ci )  || is.nan( observed_ci ) ||
        is.null( mean ) || is.na( mean )  || is.nan( mean ) ) {
      print( "got NA") 
      return( NA )
    }
    top_of_error_bar <- mean + observed_ci
    if( top_of_error_bar > required_threshold ) { # record the last position at which the condition doesnt hold (which is the next)
      result = record_count + 1
    }
  }
  return( result )
}

# This saves a plot the overlaps of links and non links in a big M style.
plotMacdonalds <- function( sibdata ) {
  
  sibdata <- sibdata[which(sibdata$records.processed==max(sibdata$records.processed)),]    # extract the block of data with the highest value in records.processed
  
  for( i in 1:(nrow(sibdata)/2) ) { # process pairs of rows in the block
    
    non_links <- t(sibdata[i*2-1,8:ncol(sibdata)])  # transpose the row of counts on non_links into a column
    links <- t(sibdata[i*2,8:ncol(sibdata)])        # transpose the row of counts of links into a column
    
    non_links <- non_links / sum(non_links) # normalise non_links
    links <- links / sum(links)             # normalise links
    
    both <- data.frame(links,non_links)
    names( both ) <- c( 'links','non_links')
    
    plot <- ggplot( data=both,aes(x=as.numeric(str_replace(rownames(both),"X","")))) +
      geom_line(y=both$non_links,aes(colour="non links")) +
      geom_line(y=both$links,aes(colour="links")) +
      xlab("threshold") +
      ylab("count") +
      labs(colour="Key") + #
      ylim(0,1.1 * max(both$links,both$non_links)) +
      ggtitle(mydata[i,]$metric)
    
    ggsave( paste( paste( "/tmp",str_replace(mydata[i,]$metric,"/","-"),sep="/"  ),"png", sep="."),plot )
  }
}

# the parameter plotdata is a table of type record( metric, threshold, closeness,records.processed ) from analyse_space
# it returns a plot of convergence for the supplied data.
plotconvergence <- function( plotdata, title ) {
  
  ploted <- plotdata[ order( plotdata$closeness ), ]
  
  pp <- ggplot(data=ploted,aes(x=closeness,y=records.processed)) +
    ggtitle(paste( title,"records process vs error")) +
    geom_line( aes( colour=metric, linetype=as.factor(threshold ) ) ) +
    ylim(0,20000) +
    theme(legend.position=c(1,1),legend.justification = c(1,1)) + 
    labs( linetype="threshold" ) # legend top right
  
  return(pp)
}

# This plots each of the the metrics on a different plot
plotmetrics <- function(df, measure, lim , metric, thresholds) {
  
  plotdata <- df[which(df$metric == metric),]
  
  str(plotdata)
  
  plotdata <- plotdata[which(plotdata$threshold %in% thresholds),]  # select the appropriate thresholds
  
  # plotdata <-plotdata[ which (plotdata$threshold == thresh), ] select a single threshold - not what we want here - aide de memoire.
  
  str(plotdata)
  
  bar_width <- 10
  
  plot <- ggplot(data=plotdata) +
    geom_line(aes_string(x="records.processed", y=measure, colour=as.factor(plotdata$threshold) ), show.legend=T  ) +
    geom_errorbar(aes(x=records.processed, ymin=get(measure) - ci, ymax=get(measure) + ci, colour=as.factor(plotdata$threshold)), width = bar_width) +
    theme(legend.position="bottom") +
    ylab(measure) +
    xlab(paste0("Records processed (",paste0( RUNS," replications)"))) +
    ylim(0.0,1.1) +
    xlim(0,lim) +
    labs(colour="Threshold") +
    ggtitle(metric)
  
  ggsave( paste( "/tmp/", metric, "-", measure, "-",  lim, ".png", sep="" ),plot )
}

# This plots df relative to the final measure for the given params and saves to a filename
# lim is the xlimit
plotZeroPlots <- function(df, metric, lim, measure, thresh, filename) {
  
  plotdata <- df[which(df$metric == metric),]  # select a single metric
  plotdata <- plotdata[ which( plotdata$threshold == thresh), ]
  
  final_measure <- plotdata[nrow(plotdata),measure]
  
  plot <- ggplot(data=plotdata) +
    geom_line(aes(x=records.processed, y=final_measure-get(measure), colour=1 ), show.legend=T  ) +
    geom_line(aes(x=records.processed, y=final_measure-get(measure) - ci, colour=2 ), show.legend=T  ) +
    geom_line(aes(x=records.processed, y=final_measure-get(measure) + ci, colour=3 ), show.legend=T  ) +
    scale_y_continuous(minor_breaks = seq(-1 , 1, 0.001), breaks = seq(-1 ,1, 0.01)) +
    xlim(0,lim) + 
    ggtitle(paste(metric, thresh, measure)) +
    ggsave( paste0( filename,"-",lim,"-",metric,".png", sep=""),plot,dpi=320 )
}

# This plots df relative to the final measure for the given params and saves to a filename
# lim is the xlimit
# all plots are overlayed
plotAllZeroPlots <- function(plotdata, measure, filename, xlimit) {
  
  plot <- ggplot() 
  val <- "black"  # was 1 for different colours with increment commented below.
  
  for(thresh in unique(plotdata$threshold)) {
    for( metric in unique(plotdata$metric)) {
      subset <- plotdata[ which (plotdata$metric == metric & plotdata$threshold == thresh), ]
      final <- subset[nrow(subset),measure]
      subset[,'final'] <- final
      subset[,'col_val'] <- val
      plot <- plot + geom_line(data=subset, aes(x=records.processed, y=abs(final-get(measure))  )  ) # colour=col_val
      #val <- val + 1
    }
  }
  
  plot <- plot +
    scale_y_continuous(minor_breaks = seq(-1 , 1, 0.001), breaks = seq(-1 ,1, 0.01)) +
    geom_segment(aes(x=0,xend=xlimit,y=0.01,yend=0.01), colour = 'red', linetype = 2) +
    scale_x_continuous(labels = comma, limits = c(0, xlimit)) +
    labs(x = "Records processed", y = "Absolute error in F-measure" ) +
    theme(legend.position="none", panel.background = element_rect(fill="white"),
          panel.grid.major = element_line(size = 0.25, linetype = 'solid', colour = "grey") )
  
  ggsave( paste0( filename,"-",xlimit,".png", sep="" ),plot,dpi=320 )
}

