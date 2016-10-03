library(ggplot2)
library(grid)
library(reshape)
library(dplyr)
timeData <- read.csv("timing-data.csv", header=FALSE, sep = ",")
names(timeData) <- c("Webpage", "Iteration", "Model", "Failure", "Report")
honeyData <- filter(timeData, Webpage=="AirBnb")
modelData <- timeData[,c("Webpage","Iteration","Model")]



melted <- melt(timeData, id.vars = c("Webpage", "Iteration"))
plot <- ggplot(melted, aes(x=Webpage, y=value, colour=Webpage)) + geom_boxplot() + facet_grid(. ~ variable)
print(plot)