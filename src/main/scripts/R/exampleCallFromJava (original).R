
library("ggplot2")

data_path = commandArgs(TRUE)[1]
results_path = commandArgs(TRUE)[2]



makeplot <- function (data_path,results_path) {

    print( data_path )
    print( results_path )

    mydata <- read.table(data_path, sep = ",",  header = TRUE, stringsAsFactors=TRUE)
    ggplot(data=mydata) + geom_line(aes(x=MARRIAGE_YEAR, y=STANDARDISED_ID ), show.legend=T  )
    ggsave( results_path )

}

makeplot( data_path,results_path )

