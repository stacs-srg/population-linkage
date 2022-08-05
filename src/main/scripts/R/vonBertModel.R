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
library("FSA")

conditionLoadIntoGlobal( "~/repos/github/population-linkage/src/main/resources/UmeaThresholdBirthSiblingLinkage.csv", "mydata" )

mydata$precision <- as.numeric(mydata$precision)
mydata$recall <- as.numeric(mydata$recall)
mydata$f_measure <- as.numeric(mydata$f_measure)

measures <- c( "precision","recall","f_measure" )  
thresholds <- c( 0.2, 0.4, 0.6, 0.8 )
RUNS <- 10

xx <- select_records.processed.ci(mydata,"f_measure",0.4,"Sigma-Levenshtein" )

# first create a linear model to get estimators for a_hat, k_hat and limit
limit.0 <- min(xx$ci) + 0.5
linear_model <- lm( log(ci) ~ records.processed, data=xx)
start <- list(a_hat=exp(coef(linear_model)[1]), k_hat=coef(linear_model)[2], limit = limit.0)

# Use the output of the linear model in a non linear model (Von-Bertalanffy)

model <- nls(ci ~ ( ( a_hat * exp( k_hat * records.processed ) ) + limit ), data=xx, start=start)

a_hat = summary(model)$coefficients[1,1]
std_err_a_hat = summary(model)$coefficients[1,2]
k_hat = summary(model)$coefficients[2,1]
limit = summary(model)$coefficients[3,1]

print(a_hat)    # the scaling factor of the curve (magnitude?)
print(std_err_a_hat)
print(k_hat)    # the model gradient
print(limit)    # the y value to which the model converges






