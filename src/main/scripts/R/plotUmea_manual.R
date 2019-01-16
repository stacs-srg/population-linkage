library("ggplot2")
setwd("/Users/al/Desktop")
filename <- "/tmp/UmeaThresholdBirthSiblingLinkage.csv"

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


mydata <- read.table(filename, sep = ",",  header = TRUE, stringsAsFactors=FALSE)
mydata$precision <- as.numeric(mydata$precision)
mydata$recall <- as.numeric(mydata$recall)
mydata$f_measure <- as.numeric(mydata$f_measure)

mydata <- mydata[which(mydata$records.processed==max(mydata$records.processed)),]    # extract the block of data with the highest value in records.processed
mydata <- mydata[which(mydata$run.number<10),]    # takes first N runs for each metric

p_df <- summarySE(mydata, measurevar="precision", groupvars=c("metric", "threshold"))
r_df <- summarySE(mydata, measurevar="recall", groupvars=c("metric", "threshold"))
f_df <- summarySE(mydata, measurevar="f_measure", groupvars=c("metric", "threshold"))

#select the columns we want, then rename them
p_df <- p_df[,c('metric',"threshold","precision","ci")]
names(p_df) <- c(c('metric',"threshold","precision_mean","precision_ci"))

r_df <- r_df[,c("recall","ci")]
names(r_df) <- c(c("recall_mean","recall_ci"))

f_df <- f_df[,c("f_measure","ci")]
names(f_df) <- c(c("f_measure_mean","f_measure_ci"))

# zip the three collections together
interval_data <- cbind(p_df, r_df, f_df)

mType <- 'Sigma/Metaphone/Levenshtein'
mType <- 'Sigma/Cosine'
mType <- 'Sigma/SED'


interval_data <- interval_data[which(interval_data$metric == mType),]

bar_width <- 0.005

ggplot(data=interval_data) +
          geom_errorbar(aes(x=threshold, ymin=precision_mean-precision_ci, ymax=precision_mean+precision_ci, colour = 'precision'), width = bar_width) +
          geom_errorbar(aes(x=threshold, ymin=recall_mean-recall_ci, ymax=recall_mean+recall_ci, colour = 'recall'), width = bar_width) +
          geom_errorbar(aes(x=threshold, ymin=f_measure_mean-f_measure_ci, ymax=f_measure_mean+f_measure_ci, colour = 'f_measure'), width = bar_width) +
          geom_line(aes(x=threshold, y=precision_mean, colour='precision' ), show.legend=T  ) +
          geom_line(aes(x=threshold, y=recall_mean, colour='recall' ), show.legend=T  ) +
          geom_line(aes(x=threshold, y=f_measure_mean, colour='f_measure' ), show.legend=T  ) +
          facet_wrap(~ metric, ncol=2) +
          theme(legend.position="bottom") +
          labs(colour="") +
          ylab("Value") +
          xlab("threshold")


