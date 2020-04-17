setwd("~/Documents/Code/github/population-linkage/src/main/scripts/R")

source("functionBank.R")

conditionLoadIntoGlobal( "../../resources/UmeaBirthFatherIdentityPRFByThreshold.csv", "mydata" )

mydata$precision <- as.numeric(mydata$precision)
mydata$recall <- as.numeric(mydata$recall)
mydata$f_measure <- as.numeric(mydata$f_measure)

RUNS <- 10
thresholds <- c( 0.4, 0.6, 0.8 )
xlim <- 25000

metric <- "Sigma2-Damerau-Levenshtein-2-4--7-8"
subsetted <- mydata[which(mydata$run.number<RUNS),]    # takes first N runs for each metric, 10 is all of them.

# Plot graphs for f_measure
fff <- summarySE(subsetted, measurevar="f_measure", groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr

plotmetrics( fff, "f_measure", xlim ,metric, thresholds ) 

rrr <- summarySE(subsetted, measurevar="recall", groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr

plotmetrics( rrr, "recall", xlim ,metric, thresholds ) 

ppp <- summarySE(subsetted, measurevar="precision", groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr

plotmetrics( ppp, "precision", xlim ,metric, thresholds ) 


