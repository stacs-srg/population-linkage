setwd("~/repos/github/population-linkage/src/main/scripts/R")
source("FunctionBank.R")

path <- "~/repos/github/population-linkage/src/main/resources/"

f1 <- "firstnames.csv"
f2 <- "surnames.csv"
f3 <- "combinednames.csv"

f4<-"small.csv"

conditionLoadIntoGlobal( paste(path,f1,sep=""), "firstnames", sep="\t" )
conditionLoadIntoGlobal( paste(path,f2,sep=""), "surnames", sep="\t" )
conditionLoadIntoGlobal( paste(path,f3,sep=""), "combinednames", sep="\t" )

show <- function( name, data ) {
  data <- data[(!(data$name=="") & !(data$name==" ")),]
  
  print( nrow( data ) )
  print( name )
  print( mean( data$count ) )
  max = max( data$count )
  print( max )
  print( data[ which( data$count == max ), ] )
  print( sd( data$count ) )
}

show( "firstnames",firstnames )
show( "surnames",surnames )
show( "combinednames",combinednames )