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

select_records.processed.ci <- function(df, measure, thresh, metric) {
  # ci_s is a table in which we are interested the confidence intervals
  ci_s <- df[which(df$metric == metric),]  # select a single metric
  ci_s <- ci_s[ which( ci_s$threshold == thresh), ] # select a threshold
  ci_s <- summarySE(ci_s, measurevar=measure, groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr
  
  ci_s <- ci_s[,c( 3,8 ) ]  # select records.processed and ci columns - a column of confidence intervals.
  
  return( ci_s )
}

eval_vbt <- function ( x,a,k,c ) {
  return( a * exp( k * x ) + c )
}

find_first_that_close_enough <- function( data, a_hat,k_hat,limit, how_close_is_close_enough ) {
  
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

find_first_close_enough_to_observed <- function( data, required_threshold, limit ) {
  
  for( record_count in unique(data$records.processed) ) {
    observed_value <- data[which(data$records.processed == record_count), ]$ci   #eval_vbt( record_count,a_hat,k_hat,limit )
    if( is.null(observed_value) && is.na(observed_value) && is.nan(observed_value) ) {
      return( NA )
    }
    difference <- observed_value - limit 
    if( difference < required_threshold ) {
      return( record_count )
    }
  }
  return( NA )
}

clear_env_apart_from <- function( keep_strings_column ) {
  print( keep_strings_column )
  keep_strings_column <- cbind( keep_strings_column,"clear_env_apart_from" )
  print( keep_strings_column )
  rm( list=ls(envir=.GlobalEnv)[!(ls(envir=.GlobalEnv ) %in% keep_strings_column)], envir=.GlobalEnv)
}

mydata <- read.table(filename, sep = ",",  header = TRUE, stringsAsFactors=FALSE)
mydata$precision <- as.numeric(mydata$precision)
mydata$recall <- as.numeric(mydata$recall)
mydata$f_measure <- as.numeric(mydata$f_measure)

measures <- c( "precision","recall","f_measure" )  
thresholds <- c( 0.2, 0.4, 0.6, 0.8 )
RUNS <- 10

xx <- select_records.processed.ci(mydata,"f_measure",0.4,"Sigma-Levenshtein" )

limit.0 <- min(xx$ci) + 0.5
linear_model <- lm( log(ci) ~ records.processed, data=xx)
start <- list(a_hat=exp(coef(linear_model)[1]), k_hat=coef(linear_model)[2], limit = limit.0)
model <- nls(ci ~ ( ( a_hat * exp( k_hat * records.processed ) ) + limit ), data=xx, start=start)

a_hat = summary(model)$coefficients[1,1]
std_err_a_hat = summary(model)$coefficients[1,2]
k_hat = summary(model)$coefficients[2,1]
limit = summary(model)$coefficients[3,1]

print(a_hat)    # the scaling factor of the curve (magnitude?)
print(std_err_a_hat)
print(k_hat)    # the model gradient
print(limit)    # the y value to which the model converges

closeness_data <- data.frame( closeness = double(), records.processed = integer() )

for( closeness_tolerance in seq( 0, 0.2, 0.00001 ) ) {
  number <- find_first_that_close_enough_to_observed( xx, closeness_tolerance )
  closeness_data[ nrow(closeness_data)+1, ] <- c( closeness_tolerance,number )
  if( number == 100 ) {
    break
  }
}

plot(closeness_data)

analyse_space <- function( data, measure ) {
  
  closeness.df <- data.frame( metric = character(),
                              threshold = double(),
                              closeness = double(),
                              records.processed = integer(),
                              stringsAsFactors=FALSE )
  
  for( metric in unique(data$metric))
      for( threshold in thresholds ) {
        current_subset <- select_records.processed.ci( data,measure,threshold,metric )
      
        for( closeness_tolerance in seq( 0, 0.1, 0.0001 ) ) {
          limit <- 0
          number <- find_first_close_enough_to_observed( current_subset, closeness_tolerance, limit )
          if( ! is.na(number) ) {
            closeness.df[ nrow(closeness.df)+1, ] <- c( metric,threshold,closeness_tolerance,number )   #<<<<<<<<<<<<<<----------- WRONG makes everything into a string
            print( "DONT USE THIS _ ERROR ") 
            #print( paste( metric,measure,threshold,closeness_tolerance,number ) )
          }
        }
      }
  return( closeness.df )
}

closeness <- analyse_space(mydata,"f_measure")

# This plots each of the the metrics on a different plot
plotconvergence <- function( plotdata ) {
  #plotdata <- plotdata[ which( plotdata$threshold == 0.2 ), ]
  #plotdata <- plotdata[ which( plotdata$metric == "Sigma-Levenshtein" ), ]
  
  pp <- ggplot(data=plotdata,aes(x=records.processed)) +
    #theme(legend.position="bottom") +
    #ggtitle("closeness") +
    scale_x_discrete( limits=0:40000, breaks = seq( 0,40000,10000)) +
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

plot <- plotconvergence(closeness)
print( plot )






  

