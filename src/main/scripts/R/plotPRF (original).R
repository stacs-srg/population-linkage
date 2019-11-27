setwd("~/repos/github/population-linkage/src/main/scripts/R")

source("FunctionBank.R")

conditionLoadIntoGlobal( "~/repos/github/population-linkage/src/main/resources/UmeaThresholdBirthSiblingLinkage.csv", "mydata" )

mydata$precision <- as.numeric(mydata$precision)
mydata$recall <- as.numeric(mydata$recall)
mydata$f_measure <- as.numeric(mydata$f_measure)

RUNS <- 10
thresholds <- c( 0.4, 0.6, 0.8 )
xlimits <- c (25000,227889)

subsetted <- mydata[which(mydata$run.number<RUNS),]    # takes first N runs for each metric, 10 is all of them.

# Plot graphs for f_measure
fff <- summarySE(subsetted, measurevar="f_measure", groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr

for( xlim in xlimits ) 
  for( metric in unique(subsetted$metric))
    plotmetrics( fff, "f_measure", xlim ,metric, thresholds ) 

for( xlim in xlimits ) 
  for( metric in unique(fff$metric))
    for( threshold in thresholds )
      plotZeroPlots(fff, metric, xlim, "f_measure", threshold, "/tmp/f_measure")

for( xlim in xlimits )
  plotAllZeroPlots(fff, "f_measure", "/tmp/f_measure_max_error", xlim)

# Plot graphs for precision
ppp <- summarySE(subsetted, measurevar="precision", groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr

for( xlim in xlimits ) 
  for( metric in unique(subsetted$metric))
    plotmetrics( ppp, "precision", xlim ,metric, thresholds ) 

for( xlim in xlimits ) 
  for( metric in unique(ppp$metric))
    for( threshold in thresholds )
      plotZeroPlots(ppp, metric, xlim, "precision", threshold, "/tmp/precision_zero" )

for( xlim in xlimits )
  plotAllZeroPlots(ppp, "precision", "/tmp/precision_max_error", xlim)

# Plot graphs for recall
rrr <- summarySE(subsetted, measurevar="recall", groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr

for( xlim in xlimits ) 
  for( metric in unique(subsetted$metric))
    plotmetrics( rrr, "recall", xlim ,metric, thresholds ) 

for( xlim in xlimits ) 
  for( metric in unique(rrr$metric))
    for( threshold in thresholds )
      plotZeroPlots(rrr, metric, xlim, "recall", threshold, "/tmp/recall_zero" )

for( xlim in xlimits )
  plotAllZeroPlots(rrr, "recall", "/tmp/recall_max_error", xlim)
