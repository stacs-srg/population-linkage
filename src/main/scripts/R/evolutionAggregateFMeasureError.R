INPUT_FILE_PATH <- "/Users/graham/Desktop/dummy.csv"
OUTPUT_DIRECTORY_PATH <- "/Users/graham/Desktop/f_measure_max_error"

source("FunctionBank.R")

conditionLoadIntoGlobal(INPUT_FILE_PATH, "mydata" )

xlimits <- c(25000,227889)

combinedstats <- summarySE(mydata, measurevar="f_measure", groupvars=c("metric", "threshold","records.processed")) # calculate mean,stdev,stderr

for (xlim in xlimits)
  plotAllZeroPlots(combinedstats, "f_measure", OUTPUT_DIRECTORY_PATH, xlim)
