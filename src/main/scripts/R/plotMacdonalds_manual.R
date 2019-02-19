library("ggplot2")
library("stringr")
filename <- "/tmp/UmeaThresholdBirthSiblingDistances.csv"

mydata <- read.table(filename, sep = ",",  header = TRUE, stringsAsFactors=FALSE)

mydata$metric <- as.factor(mydata$metric)
mydata$link_nonlink <- as.factor(mydata$link_nonlink)

mydata <- mydata[which(mydata$records.processed==max(mydata$records.processed)),]    # extract the block of data with the highest value in records.processed

for( i in 1:(nrow(mydata)/2) ) { # process pairs of rows in the block
  
  non_links <- t(mydata[i*2-1,8:ncol(mydata)])  # transpose the row of counts on non_links into a column
  links <- t(mydata[i*2,8:ncol(mydata)])        # transpose the row of counts of links into a column
  
  non_links <- non_links / sum(non_links) # normalise non_links
  links <- links / sum(links)    # normalise links
  
  both <- data.frame(links,non_links)
  names( both ) <- c( 'links','non_links')
  
  plot <- ggplot( data=both,aes(x=as.numeric(str_replace(rownames(both),"X","")))) +
    geom_line(y=both$non_links,aes(colour="non links")) +
    geom_line(y=both$links,aes(colour="links")) +
    xlab("threshold") +
    ylab("count") +
    labs(colour="Key") + #
    ylim(0,1.1 * max(both$links,both$non_links)) +
    ggtitle(mydata[i,]$metric)
  
  ggsave( paste( paste( "/tmp",str_replace(mydata[i,]$metric,"/","-"),sep="/"  ),"png", sep="."),plot )

}

