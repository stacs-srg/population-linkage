library("ggplot2")
setwd("/Users/al/Desktop")
filename <- 'UmeaMeasures.csv'

mydata <- read.table(filename, sep = ",",  header = TRUE, stringsAsFactors=FALSE)
mydata$precision <- as.numeric(mydata$precision)
mydata$recall <- as.numeric(mydata$recall)
mydata$f_measure <- as.numeric(mydata$f_measure)

# data format is "Metric" + DELIMIT + "threshold" + DELIMIT + "precision" + DELIMIT + "recall" + DELIMIT + "f_measure"


ggplot(data=mydata) +
          geom_line(aes(x=threshold, y=precision, colour='precision' ), show.legend=T  ) +
          geom_line(aes(x=threshold, y=recall, colour='recall' ), show.legend=T  ) +
          geom_line(aes(x=threshold, y=f_measure, colour='f_measure' ), show.legend=T  ) +
          facet_wrap(~ Metric, ncol=2) +
          theme(legend.position="bottom") +
          labs(colour="legend") +
          ylab("Value") +
          xlab("threshold")
  

