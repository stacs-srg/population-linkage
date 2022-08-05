#
# Copyright 2022 Systems Research Group, University of St Andrews:
# <https://github.com/stacs-srg>
#
# This file is part of the module population-linkage.
#
# population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
# License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
# version.
#
# population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with population-linkage. If not, see
# <http://www.gnu.org/licenses/>.
#

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