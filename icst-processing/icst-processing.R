library(ggplot2)
library(grid)
library(reshape)
library(dplyr)
timeData <- read.csv("timing-data-new.csv", header=FALSE, sep = ",")
names(timeData) <- c("Webpage", "Iteration", "Model", "Failure", "Report")

modelData <- timeData[,c("Webpage","Iteration","Model")]
plot <- ggplot(modelData, aes(x=Webpage, y=Model, colour=Webpage)) + geom_boxplot()
print(plot)
# melted <- melt(timeData, id.vars = c("Webpage"))
# names(melted) <- c("Webpage", "Stage", "Time")
# 
# barplot <- ggplot(data=melted, aes(x=Webpage, y=Time, fill=factor(Stage))) + 
#   geom_bar(width=.85, stat="identity") +
#   theme_bw(base_size = 12) +
#   theme(axis.text.x = element_text(angle = 45, hjust = 1)) +
#   theme(legend.position = "top",
#         legend.text = element_text(size = 12),
#         legend.box = "horizontal",
#         legend.key = element_blank(),
#         legend.key.size = unit(.5, "cm")) +
#   ylab("Execution Time (seconds)") +
#   xlab("Web Page") +
#   guides(fill = guide_legend(nrow = 1))
# print(barplot)