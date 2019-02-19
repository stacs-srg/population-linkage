library("ggplot2")

data_path = commandArgs(TRUE)[1]
results_path = commandArgs(TRUE)[2]


makeplot <- function (data_path,results_path) {

    print( data_path )
    print( results_path )

    mydata <- read.table(data_path, sep = ",",  header = TRUE, stringsAsFactors=FALSE)

    mydata$precision <- as.numeric(mydata$precision)
    mydata$recall <- as.numeric(mydata$recall)
    mydata$f_measure <- as.numeric(mydata$f_measure)

    ggplot(data=mydata) +
        geom_line(aes(x=threshold, y=precision, colour='precision' ), show.legend=T  ) +
        geom_line(aes(x=threshold, y=recall, colour='recall' ), show.legend=T  ) +
        geom_line(aes(x=threshold, y=f_measure, colour='f_measure' ), show.legend=T  ) +
        facet_wrap(~ metric, ncol=2) +
        theme(legend.position="bottom") +
        labs(colour="legend") +
        ylab("Value") +
        xlab("threshold")
    ggsave( results_path )

}

makeplot( data_path,results_path )

