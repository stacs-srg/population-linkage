# This program used to generate the convergence graphs in the paper.
# Currently these are numbered 4 & 5 and have titles "Evolution of aggregate errors in F1-measure..."

INPUT_FILE_PATH <- "/Users/al/Documents/Current/Results/2019-03-08-Umea-full-nXn/UmeaBirthSiblingLPRFByThreshold-full.csv"
OUTPUT_DIRECTORY_PATH <- "/tmp/"

source("FunctionBank.R")

conditionLoadIntoGlobal(INPUT_FILE_PATH, "mydata" )

xlimits <- c(25000,227889)

combinedstats <- summarySE(mydata, measurevar="f_measure", groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr

for (xlim in xlimits)
  plotAllZeroPlots(combinedstats, "f_measure", OUTPUT_DIRECTORY_PATH, xlim)
