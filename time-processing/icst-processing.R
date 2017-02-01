library(ggplot2)
library(grid)
library(reshape)
library(dplyr)
library(plyr)
timeData <- read.csv("timing-data-issta-final.csv", header=TRUE, sep = ",")
names(timeData) <- c("Webpage", "Iteration", "Model", "Failure", "Report", "Total")

# summarised <- summarise(timeData, group=c(Webpage), mean = mean(Total))
summarised <- ddply(timeData, .(Webpage), summarise, mean = mean(Total), median=median(Total))
write.csv(file="averaged.csv", x=summarised)
# modelData <- timeData[,c("Webpage","Iteration","Model")]
# plot <- ggplot(timeData, aes(x=Webpage, y=Model+Failure+Report, colour=Webpage)) + geom_boxplot() +
#   theme_bw(base_size = 12) +
#     theme(axis.text.x = element_text(angle = 45, hjust = 1)) +
#     theme(legend.position = "none",
#           legend.text = element_text(size = 12),
#           legend.box = "horizontal",
#           legend.key = element_blank(),
#           legend.key.size = unit(.5, "cm")) +
#     ylab("Execution Time (seconds)") +
#     xlab("Web Page") +
#     guides(fill = guide_legend(nrow = 1))
# print(plot)

# summarised <- summarise(data = modelData, group=Webpage, avg = mean)
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