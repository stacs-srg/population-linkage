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

mydata <- read.table(filename, sep = ",",  header = TRUE, stringsAsFactors=FALSE)
mydata$precision <- as.numeric(mydata$precision)
mydata$recall <- as.numeric(mydata$recall)
mydata$f_measure <- as.numeric(mydata$f_measure)

measures <- c( "precision","recall","f_measure" )  
xlimits <- c (10000,227889)
thresholds <- c( 0.4, 0.6, 0.8 )
RUNS <- 10

xx <- select_records.processed.ci(mydata,"f_measure",0.4,"Sigma-Levenshtein" )

eval_vbt <- function ( x,linf,k ) {
  return( linf * ( 1 - exp( -1 * k * x ) ) )
}

( vbT <- vbFuns("typical") )      # von-Bertalanffy typical

xintervals <- c( 300,500,700,900,1100 )
for( x in xintervals ) {
  yy <- xx[ which( xx$records.processed <= x ), ]
  # start <- yy[1, ]$ci #  nrow(yy)
  yy$ci <- 0 - yy$ci  # invert graph - make it into growth rather than exponential decline

  ( svTall <- vbStarts(ci~records.processed,data=yy,type="typical" ) ) # starting values
  print(paste("x",x))
  print(paste( "linf", svTall$Linf ) )
  print(paste( "k", svTall$K ) )
  print(paste( "t0", svTall$t0) )
  print( paste( "transformed original ci", yy[ which(yy$records.processed == x ), ]$ci ) )
  print( paste( "vbt", eval_vbt( x,svTall$Linf,svTall$K ) ) )
  print( "--------- " )
}


# start <- yy[1, ]$ci #  nrow(yy)
yy <- xx
yy$ci <- 10 - yy$ci  # invert graph - make it into growth rather than exponential decline

( svTall <- vbStarts(ci~records.processed,data=yy,type="typical" ) ) # starting values
print(paste( "linf", svTall$Linf ) )
print(paste( "k", svTall$K ) )
print(paste( "t0", svTall$t0) )

xintervals <- seq( 100, 2200, 100 )
for( x in xintervals ) {
  print(paste("x",x))
  print( paste( "vbt", eval_vbt( x,svTall$Linf,svTall$K ) ) )
}



plot(yy)
plot(svTall)

