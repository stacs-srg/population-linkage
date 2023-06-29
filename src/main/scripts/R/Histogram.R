


non_links <- read.csv( "/Users/al/Desktop/u.csv" )
links <- read.csv( "/Users/al/Desktop/m.csv" )

  


hist(non_links$u, xlab = "distance", col = "red", border = "black")
hist(links$m, col = "green", add=TRUE )