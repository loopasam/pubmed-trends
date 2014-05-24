data <- read.csv(file="/home/loopasam/git/pubmed-trends/data/distribution_full_index.txt",
                 sep="\t", 
                 quote = "\'");
# Plot the war data
boxplot(data$V1)

# Identify outliers and get rid of them
times <- data$V1[!data$V1 %in% boxplot.stats(data$V1)$out]
hist(times, breaks=20, col="red")