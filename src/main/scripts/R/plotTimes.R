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

library("ggplot2")
library("stringr")
source("~/repos/github/population-linkage/src/main/scripts/R/FunctionBank.R")

setwd("~/Desktop")

data <- read.table("results.csv", sep = ',',  header = TRUE, stringsAsFactors=FALSE)

summary <- summarySE(data, summarised_column_name="time", grouping_variable_column_names=c("code", "par"))  # calculate mean,stdev,stderr

ggplot( summary, aes( x=par,colour=code ) ) +
  geom_line( aes( y=time ) ) +
  geom_errorbar( aes( ymin=time-se, ymax=time+se ),width=1 ) +
  scale_x_continuous( limits = c(0, 24),  breaks = seq(0, 24, 4), minor_breaks=seq( 0,24, by = 2 ), labels=scales::comma) +
  scale_y_continuous( limits = c(0, 75),  breaks = seq(0, 75, 10), minor_breaks=seq( 0,75, by = 5 ), labels=scales::comma) +
  scale_color_manual(labels = c("V1", "V2", "V3"),values = c("green", "blue", "red")  ) +
  labs(title = "Execution Time vs degree of parallelism", x = "Degree of parallelism", y = "Time per query (ms)", color = "Code Version") +
  theme(legend.position = c(0.9, 0.9) )


