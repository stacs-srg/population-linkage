library("ggplot2")

filename = commandArgs(TRUE)[1]
resultsfilename = commandArgs(TRUE)[2]

## Summarizes data.
## Gives count, mean, standard deviation, standard error of the mean, and confidence interval (default 95%).
##   data: a data frame.
##   measurevar: the name of a column that contains the variable to be summariezed
##   groupvars: a vector containing names of columns that contain grouping variables
##   na.rm: a boolean that indicates whether to ignore NA's
##   conf.interval: the percent range of the confidence interval (default is 95%)
summarySE <- function(data=NULL, measurevar, groupvars=NULL, na.rm=FALSE,
                      conf.interval=.95, .drop=TRUE) {
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

makeplot <- function (data_path,resultsfilename) {

    print( data_path )
    print( resultsfilename )

    mydata <- read.table(filename, sep = ",",  header = TRUE, stringsAsFactors=FALSE)
    mydata$precision <- as.numeric(mydata$precision)
    mydata$recall <- as.numeric(mydata$recall)
    mydata$f_measure <- as.numeric(mydata$f_measure)

    RUNS <- 10
    mydata <- mydata[which(mydata$run.number<RUNS),]    # takes first N runs for each metric

    mydata <- summarySE(mydata, measurevar="f_measure", groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr
    mydata <- mydata[which(mydata$threshold %in% c( 0.2,0.4,0.6,0.8 )),]  # select some thresholds

    bar_width <- 10

    plot <- ggplot(data=mydata) +
        geom_line(aes(x=records.processed, y=f_measure, colour=as.factor(threshold) ), show.legend=T  ) +
        geom_errorbar(aes(x=records.processed, ymin=f_measure-ci, ymax=f_measure+ci, colour=as.factor(threshold)), width = bar_width) +
        theme(legend.position="bottom") +
        facet_wrap(~ metric, ncol=2) +
        ylab("F Measure") +
        xlab(paste0("Records processed (",paste0( RUNS," replications)"))) +
        ylim(-0.1,1.1) +
        labs(colour="Threshold")

    ggsave( resultsfilename,plot )
}


makeplot( filename,resultsfilename )

