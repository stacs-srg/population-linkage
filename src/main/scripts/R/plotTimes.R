library("ggplot2")
library("stringr")
source("~/repos/github/population-linkage/src/main/scripts/R/FunctionBank.R")

setwd("~/Desktop")

data <- read.table("results.csv", sep = ',',  header = TRUE, stringsAsFactors=FALSE)

summary <- summarySE(data, measurevar="time", groupvars=c("code","par"))  # calculate mean,stdev,stderr

ggplot( summary, aes( x=par,colour=code ) ) +
  geom_line( aes( y=time ) ) +
  geom_errorbar( aes( ymin=time-se, ymax=time+se ),width=1 ) +
  scale_x_continuous( limits = c(0, 24),  breaks = seq(0, 24, 4), minor_breaks=seq( 0,24, by = 2 ), labels=scales::comma) +
  scale_y_continuous( limits = c(0, 75),  breaks = seq(0, 75, 10), minor_breaks=seq( 0,75, by = 5 ), labels=scales::comma) +
  scale_color_manual(labels = c("V1", "V2", "V3"),values = c("green", "blue", "red")  ) +
  labs(title = "Execution Time vs degree of parallelism", x = "Degree of parallelism", y = "Time per query (ms)", color = "Code Version") +
  theme(legend.position = c(0.9, 0.9) )


