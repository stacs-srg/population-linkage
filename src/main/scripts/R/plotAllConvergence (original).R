setwd("~/repos/github/population-linkage/src/main/scripts/R")
source("FunctionBank.R")

conditionLoadIntoGlobal( "~/repos/github/population-linkage/src/main/resources/UmeaThresholdBirthSiblingLinkage.csv", "mydata" )

mydata$precision <- as.numeric(mydata$precision)
mydata$recall <- as.numeric(mydata$recall)
mydata$f_measure <- as.numeric(mydata$f_measure)

measures <- c( "precision","recall","f_measure" )  
thresholds <- c( 0.2, 0.4, 0.6, 0.8 )
RUNS <- 10

f_closeness <- analyse_space(mydata,"f_measure")
plot <- plotconvergence(f_closeness,"F-measure")
print( plot )

r_closeness <- analyse_space(mydata,"recall")
plot <- plotconvergence(f_closeness,"Recall")
print( plot )

p_closeness <- analyse_space(mydata,"precision")
plot <- plotconvergence(f_closeness,"Precision")
print( plot )

print_table( f_closeness, "f_measure")
print_table( r_closeness,"recall")
print_table( p_closeness,"precision")