setwd("~/repos/github/population-linkage/src/main/scripts/R")
source("FunctionBank.R")

#conditionLoadIntoGlobal( "~/repos/github/population-linkage/src/main/resources/UmeaThresholdBirthSiblingLinkage.csv", "mydata" )
conditionLoadIntoGlobal( "~/repos/github/population-linkage/src/main/resources/UmeaBirthSiblingPRFByThreshold.csv", "mydata" )

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

print_table <- function( data, measure ) {
  required_error <- c( 0.1, 0.09, 0.08, 0.07, 0.06, 0.05, 0.04, 0.03, 0.02, 0.01 )  #0.075, 0.05, 0.025, 0.01, 0.005, 0.0025 ) 
  print( paste( measure))
  print( "closeness | records.processed" )
  for( err in required_error ) {
    req <- data[ which( data$closeness == err ), ]
    req <- req[ which( req$records.processed == max( req$records.processed ) ), ] # find highest number of records that need to be processed to make err (find worst case)
    max <- max( req$records.processed )
    print( paste( err,"|", max ) )
  }
}

print_table( f_closeness, "f_measure")
print_table( r_closeness,"recall")
print_table( p_closeness,"precision")