setwd("~/repos/github/population-linkage/src/main/scripts/R")
source( "FunctionBank.R" )

conditionLoadIntoGlobal( "/tmp/UmeaThresholdBirthSiblingDistances.csv", "sibdata" )

mydata$precision <- as.numeric(mydata$precision)
mydata$recall <- as.numeric(mydata$recall)
mydata$f_measure <- as.numeric(mydata$f_measure)
mydata$metric <- as.factor(mydata$metric)
mydata$link_nonlink <- as.factor(mydata$link_nonlink)

plotMacdonalds( mydata )

