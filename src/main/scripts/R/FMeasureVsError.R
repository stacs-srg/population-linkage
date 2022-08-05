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

conditionLoadIntoGlobal( "~/repos/github/population-linkage/src/main/resources/UmeaThresholdBirthSiblingLinkage.csv", "mydata" )

mydata$precision <- as.numeric(mydata$precision)
mydata$recall <- as.numeric(mydata$recall)
mydata$f_measure <- as.numeric(mydata$f_measure)

plot <- plotEvolutionAbsoluteError(mydata, "f_measure", 20000, "Records processed", "Absolute error in F1-measure")


