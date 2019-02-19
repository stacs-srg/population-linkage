library("ggplot2")
setwd("/Users/al/Desktop")
filename <- "/tmp/UmeaThresholdBirthSiblingLinkage.csv"

# datafile schema: time,run number,records processed,pairs evaluated,pairs ignored,metric,threshold,tp,fp,fn,tn,precision,recall,f_measure
# entries look like this:
# 2019-01-15T13:54:57.300,1,100,819537,1458838,Sigma-Levenshtein,0.00,14,0,22,819501,1.00,0.39,0.56

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

find_acceptable <- function(data, measure, thresh, metric) {
  plotdata <- data[which(data$metric == metric),]  # select a single metric
  plotdata <- summarySE(plotdata, measurevar=measure, groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr
  plotdata <- plotdata[ which( plotdata$threshold == thresh), ]
  sig <- 0
  num_runs <- 100
  window_size <- 5000
  # find the 95% signficance level
  step <- 0
  while( sig < 0.3) { 
    num_runs <- num_runs + step
    
    window <- plotdata[ which(num_runs < plotdata$records.processed & plotdata$records.processed < num_runs + window_size ), ]
    
    #print( head(plotdata))
    #print( summary(plotdata))
    
    if(num_runs > max(plotdata$records.processed))
      return(NA)
    
    model <- glm(f_measure~records.processed,data=window,family=binomial())
    #print(summary(model))
    coef <- coef(summary(model) )[,4][2]  # column 4 group of p values, column 2 is p value for measure
    if( ! is.na(coef) ) {  # value not applicable
      sig <- coef
    }
  }
  return( num_runs )
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

plotAllZeroPlots <- function(plotdata, measure, filename, xlimit) {

  plot <- ggplot() 
  val <- 1
  
  for(thresh in unique(plotdata$threshold)) {
    for( metric in unique(plotdata$metric)) {
      subset <- plotdata[ which (plotdata$metric == metric & plotdata$threshold == thresh), ]
      final <- subset[nrow(subset),measure]
      subset[,'final'] <- final
      subset[,'col_val'] <- val
      plot <- plot + geom_line(data=subset, aes(x=records.processed, y=final-get(measure), colour=col_val )  )
      val <- val + 1
    }
  }
  
  plot <- plot +
    scale_y_continuous(minor_breaks = seq(-1 , 1, 0.001), breaks = seq(-1 ,1, 0.01)) +
    geom_segment(aes(x=0,xend=xlimit,y=0.01,yend=0.01), colour = 'red', linetype = 2) +
    geom_segment(aes(x=0,xend=xlimit,y=-0.01,yend=-0.01), colour = 'red', linetype = 2) +
    xlim(0,xlimit) + 
    ggtitle(paste("Max error", measure)) + 
    theme(legend.position="none")
  
  ggsave( paste0( filename,"-",xlimit,".png", sep="" ),plot,dpi=320 )
}

mydata <- read.table(filename, sep = ",",  header = TRUE, stringsAsFactors=FALSE)
mydata$precision <- as.numeric(mydata$precision)
mydata$recall <- as.numeric(mydata$recall)
mydata$f_measure <- as.numeric(mydata$f_measure)

measures <- c( "precision","recall","f_measure" )  
xlimits <- c (10000,227000)
thresholds <- c( 0.4, 0.6, 0.8 )
RUNS <- 10

mydata <- mydata[which(mydata$run.number<RUNS),]    # takes first N runs for each metric
# mydata <- mydata[which(mydata$threshold %in% thresholds),]  # select some thresholds

# Plot graphs for f_measure




fff <- summarySE(mydata, measurevar="f_measure", groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr

for( xlim in xlimits ) 
  for( metric in unique(mydata$metric))
    plotmetrics( fff, "f_measure", xlim ,metric, thresholds ) 

for( xlim in xlimits ) 
  for( metric in unique(fff$metric))
    for( threshold in thresholds )
      plotZeroPlots(fff, metric, xlim, "f_measure", threshold, "/tmp/f_measure")

for( xlim in xlimits )
  plotAllZeroPlots(fff, "f_measure", "/tmp/f_measure_max_error", xlim)

# Plot graphs for precision

ppp <- summarySE(mydata, measurevar="precision", groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr

for( xlim in xlimits ) 
  for( metric in unique(mydata$metric))
    plotmetrics( ppp, "precision", xlim ,metric, thresholds ) 

for( xlim in xlimits ) 
  for( metric in unique(ppp$metric))
    for( threshold in thresholds )
      plotZeroPlots(ppp, metric, xlim, "precision", threshold, "/tmp/precision_zero" )

for( xlim in xlimits )
  plotAllZeroPlots(ppp, "precision", "/tmp/precision_max_error", xlim)

# Plot graphs for recall

rrr <- summarySE(mydata, measurevar="recall", groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr

for( xlim in xlimits ) 
  for( metric in unique(mydata$metric))
    plotmetrics( rrr, "recall", xlim ,metric, thresholds ) 

for( xlim in xlimits ) 
  for( metric in unique(rrr$metric))
    for( threshold in thresholds )
      plotZeroPlots(rrr, metric, xlim, "recall", threshold, "/tmp/recall_zero" )

for( xlim in xlimits )
  plotAllZeroPlots(rrr, "recall", "/tmp/recall_max_error", xlim)


recall <- function( tp, fn ) {
  return( tp / ( tp + fn ) )
}

precision <- function( tp, fp ) {
  return( tp / ( tp + fp ) )
}

fmeasure <- function( precision, recall ) {
  return( 2 * ( precision * recall ) / ( precision + recall ) )
}

graham_confidences <- function( measure ) {
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

  return(plotdata)
}

plotdata <- graham_confidences("f_measure")

ggplot( plotdata, aes( x=records.processed ) ) +
  geom_line( aes( y=abs_diff ) ) +
  geom_errorbar( aes( ymin=abs_diff-ci, ymax=abs_diff+ci ),width=1 ) +
#  geom_line( aes( y=ci  ) ) +
  scale_x_continuous( minor_breaks=seq(0,max(plotdata$records.processed),10000 ) ) +
  ggtitle( "Absolute Errors in F_measure" ) +
  ylab( "Absolute difference between measured and known value") +
  xlab( "Records processed") +
  xlim( 0,20000 )

                    
  




