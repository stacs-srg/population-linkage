library("ggplot2")
library("FSA")
setwd("/Users/al/Desktop")
filename <- "/tmp/UmeaThresholdBirthSiblingLinkage.csv"

## Summarizes data.
## Gives count, mean, standard deviation, standard error of the mean, and confidence interval (default 95%).
##   data: a data frame.
##   measurevar: the name of a column that contains the variable to be summarized
##   groupvars: a vector containing names of columns that contain grouping variables
##   na.rm: a boolean that indicates whether to ignore NA's
##   conf.interval: the percent range of the confidence interval (default is 95%)
summarySE <- function(data=NULL, measurevar, groupvars=NULL, na.rm=FALSE, conf.interval=.95, .drop=TRUE) {
  library(plyr)
  
  # New version of length which can handle NA's: if na.rm==T, don't count them
  length2 <- function (x, na.rm=FALSE) {
    if (na.rm) sum(!is.na(x))
    else       length(x)
  }
  
  # This does the summary. For each group's data frame, return a vector with
  # N, mean, and sd
  datac <- ddply(data, groupvars, .drop=.drop,
                 .fun = function(xx, col) {
                   c(N    = length2(xx[[col]], na.rm=na.rm),
                     mean = mean   (xx[[col]], na.rm=na.rm),
                     sd   = sd     (xx[[col]], na.rm=na.rm)
                   )
                 },
                 measurevar
  )
  
  # Rename the "mean" column    
  datac <- rename(datac, c("mean" = measurevar))
  
  datac$se <- datac$sd / sqrt(datac$N)  # Calculate standard error of the mean
  
  # Confidence interval multiplier for standard error
  # Calculate t-statistic for confidence interval: 
  # e.g., if conf.interval is .95, use .975 (above/below), and use df=N-1
  ciMult <- qt(conf.interval/2 + .5, datac$N-1)
  datac$ci <- datac$se * ciMult
  
  return(datac)
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

# The Von Bertalanffy function
eval_vbt <- function ( x,a,k,c ) {
  return( a * exp( k * x ) + c )
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

clear_env_apart_from <- function( keep_strings_column ) {
  print( keep_strings_column )
  keep_strings_column <- cbind( keep_strings_column,"clear_env_apart_from" )
  print( keep_strings_column )
  rm( list=ls(envir=.GlobalEnv)[!(ls(envir=.GlobalEnv ) %in% keep_strings_column)], envir=.GlobalEnv)
}

analyse_space <- function( data, measure ) {
  
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

# This plots each of the the metrics on a different plot
# gets supplied with a table of type record( metric, threshold, closeness,records.processed ) from analyse_space
plotconvergence <- function( plotdata, title ) {
  
  ploted <- plotdata[ order( plotdata$closeness ), ]
  
  pp <- ggplot(data=ploted,aes(x=closeness,y=records.processed)) +
    ggtitle(paste( title,"records process vs error")) +
    geom_line( aes( colour=metric, linetype=as.factor(threshold ) ) ) +  # interaction(plotdata$metric,plotdata$threshold)
    # + facet_grid( metric ~ threshold )  ) 
    ylim(0,20000) +
    xlab( "error" ) +
    theme(legend.position=c(1,1),legend.justification = c(1,1)) + 
    labs( linetype="threshold" ) # legend top right
  
  return(pp)
}

print_table <- function( data, measure ) {
  required_error <- c( 0.1, 0.08, 0.06, 0.04, 0.02, 0.01 )  #0.075, 0.05, 0.025, 0.01, 0.005, 0.0025 ) 
  print( paste( measure))
  print( "closeness | records.processed" )
  for( err in required_error ) {
    req <- data[ which( data$closeness == err ), ]
    req <- req[ which( req$records.processed == max( req$records.processed ) ), ] # find highest number of records that need to be processed to make err (find worst case)
    max <- max( req$records.processed )
    print( paste( err,"|", max ) )
  }
}

# ------------- Model the system as a non linear model  -------------

mydata <- read.table(filename, sep = ",",  header = TRUE, stringsAsFactors=FALSE)

measures <- c( "precision","recall","f_measure" )  
thresholds <- c( 0.2, 0.4, 0.6, 0.8 )
RUNS <- 10

# xx <- select_records.processed.ci(mydata,"f_measure",0.4,"Sigma-Levenshtein" )

# limit.0 <- min(xx$ci) + 0.5
# linear_model <- lm( log(ci) ~ records.processed, data=xx)
# start <- list(a_hat=exp(coef(linear_model)[1]), k_hat=coef(linear_model)[2], limit = limit.0)
# model <- nls(ci ~ ( ( a_hat * exp( k_hat * records.processed ) ) + limit ), data=xx, start=start)

# a_hat = summary(model)$coefficients[1,1]
# std_err_a_hat = summary(model)$coefficients[1,2]
# k_hat = summary(model)$coefficients[2,1]
# limit = summary(model)$coefficients[3,1]

# print(a_hat)    # the scaling factor of the curve (magnitude?)
# print(std_err_a_hat)
# print(k_hat)    # the model gradient
# print(limit)    # the y value to which the model converges

# plot(closeness_data)

# ------------- Plot the real data and how small the error bars are  -------------

f_closeness <- analyse_space(mydata,"f_measure")
plot <- plotconvergence(f_closeness,"F-measure")
print( plot )

r_closeness <- analyse_space(mydata,"recall")
plot <- plotconvergence(f_closeness,"Recall")
print( plot )

p_closeness <- analyse_space(mydata,"precision")
plot <- plotconvergence(f_closeness,"Precision")
print( plot )

print_table( f_closeness, "f_measure")
print_table( r_closeness,"recall")
print_table( p_closeness,"precision")



