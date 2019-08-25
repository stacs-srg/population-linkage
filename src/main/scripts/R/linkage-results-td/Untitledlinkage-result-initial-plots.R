setwd("OneDrive/cs/PhD/repos/population-linkage/src/main/scripts/R")

linkageResults <- read.csv("linkage-results-td/data/linkage-results.csv", header=TRUE)
summary(linkageResults)

library(ggplot2)
library(roperators)

linkageResults$size <- factor(linkageResults$size, levels = c("13k", "133k", "530k"))
linkageResults$size <- int(linkageResults$size %-% '[a-z]')


ggplot(data = linkageResults) +
  geom_point(aes(x = precision, y = recall, colour = corruption.)) +
  xlim(0,1) + 
  ylim(0,1) + 
  facet_grid(metric~size) +
  ggtitle("Effect of Corruption on linkage quality")

ggplot(data = linkageResults) +
  geom_point(aes(x = precision, y = recall, colour = corruption., shape = as.factor(pop.)), ) +
  xlim(0.75,1) + 
  ylim(0.75,1) + 
  facet_grid(metric~size) +
  ggtitle("Effect of Corruption on linkage quality")

ggplot(data = linkageResults) +
  geom_point(aes(x = size, y = link.time.seconds, colour = as.factor(corruption.), shape = as.factor(pop.)), ) +
  #xlim(0.75,1) + 
  #ylim(0.75,1) + 
  facet_grid(~metric) +
  ggtitle("Linkage Time")

ggplot(data = linkageResults) +
  geom_point(aes(x = X.ROs, y = link.time.seconds, colour = as.factor(corruption.), shape = as.factor(pop.)), ) +
  #xlim(0.75,1) + 
  #ylim(0.75,1) + 
  facet_grid(size~metric, scales = "free_y") +
  ggtitle("Linkage Time")
