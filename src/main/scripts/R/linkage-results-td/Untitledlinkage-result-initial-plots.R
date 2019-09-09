setwd("~/OneDrive/cs/PhD/repos/population-linkage/src/main/scripts/R")

linkageResults <- read.csv("linkage-results-td/data/linkage-results.csv", header=TRUE)
summary(linkageResults)

linkageResults <- linkageResults[which(linkageResults$code.version == "fbb4bb831cf77efac3650fc6edde44f6158bbc7f"),]

library(ggplot2)
library(roperators)

linkageResults$size <- factor(linkageResults$size, levels = c("13k", "133k", "530k"))
linkageResults$size <- int(linkageResults$size %-% '[a-z]')


ggplot(data = linkageResults) +
  geom_point(aes(x = precision, y = recall, colour = corruption.)) +
  xlim(0,1) + 
  ylim(0,1) + 
  facet_grid(linkage.approach~size) +
  ggtitle("Effect of Corruption on linkage quality")

ggplot(data = linkageResults, aes(x = precision, y = recall, fill = as.factor(corruption.), colour = as.factor(corruption.), shape = as.factor(threshold))) +
  geom_point(aes()) +
  #stat_ellipse(type = "t", geom = "polygon") +
  xlim(0.0,1) + 
  ylim(0.0,1) + 
  facet_grid(linkage.approach~size) +
  ggtitle("Effect of Corruption on linkage quality") +
  scale_shape_manual(values=1:11)

ggplot(data = linkageResults) +
  geom_point(aes(x = size, y = link.time.seconds, colour = as.factor(corruption.), shape = as.factor(pop.)), ) +
  #xlim(0.75,1) + 
  #ylim(0.75,1) + 
  facet_grid(linkage.approach~metric) +
  ggtitle("Linkage Time")

ggplot(data = linkageResults) +
  geom_point(aes(x = ROs, y = link.time.seconds, colour = as.factor(corruption.), shape = as.factor(pop.)), ) +
  #xlim(0.75,1) + 
  #ylim(0.75,1) + 
  facet_grid(size~metric, scales = "free_y") +
  ggtitle("Linkage Time")
