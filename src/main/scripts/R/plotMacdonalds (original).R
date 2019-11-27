setwd("~/repos/github/population-linkage/src/main/scripts/R")
source( "FunctionBank.R" )

conditionLoadIntoGlobal( "/tmp/UmeaThresholdBirthSiblingDistances.csv", "sibdata" )

sibdata$precision <- as.numeric(sibdata$precision)
sibdata$recall <- as.numeric(sibdata$recall)
sibdata$f_measure <- as.numeric(sibdata$f_measure)
sibdata$metric <- as.factor(sibdata$metric)
sibdata$link_nonlink <- as.factor(sibdata$link_nonlink)

plotMacdonalds( sibdata )

