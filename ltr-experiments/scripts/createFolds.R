library(dplyr)
library(plyr)
library(tidyverse)
library(gtools)

# Setup
args <- commandArgs(trailingOnly=TRUE)
if (length(args)<1) {
    stop("Usage: Rscript ./createFolds.R directoryOfSourceFile", call.=FALSE)
}
setwd(args[1])

## Paths
outputPath <- paste0("../resources/output/")
dir.create(outputPath, showWarnings = FALSE, recursive = TRUE)

# Create benchmark file in LibSVM format
featureList <- read.csv("../resources/feature_list.csv",header = T,stringsAsFactors = F)
groundTruth <- read.csv("../../resources/LOVBench_GroundTruth.csv",stringsAsFactors = F)
columns <- c("query","term","label")
colnames(groundTruth) <- columns

## assign query ids
queryid = function(x) match(x, unique(x))
benchmark <- groundTruth %>% mutate(qid = group_indices(., query) %>% queryid)

## read and join feature scores
for (featureName in featureList$Name) {
    print(paste0("Reading: ../../resources/Features/",featureName,".csv"))
    featureScores <- read.csv(paste0("../../resources/Features/",featureName,".csv"),header = T,stringsAsFactors = F) %>% select(query=Query,term=RankingElement,!!as.name(featureName):=Score)
    benchmark <- left_join(benchmark,featureScores, by=c("query"="query","term"="term"))
}

## order labels
benchmark$label <- as.numeric(benchmark$label)
benchmark <- benchmark %>% arrange(qid,desc(label))

## write benchmark in csv format to file
### first add feature number to column name
write.csv(benchmark, paste0(outputPath,"LOVBench.csv"))

### write libsvm formatted line
benchmark <- benchmark %>% mutate(LibSVMline=paste(label, paste0("qid:", qid)))
for (featureRow in 1:nrow(featureList)) {
    benchmark <- benchmark %>% mutate(LibSVMline=paste(LibSVMline, paste0(featureList$ID[featureRow], ":", get(featureList$Name[featureRow]))))
}
benchmark <- benchmark %>% mutate(LibSVMline=paste(LibSVMline, "#", paste0("query:", query), paste0("term:", term)))

## write benchmark in libsvm format to file
fileConn <- file(paste0(outputPath,"LOVBench.txt"))
writeLines(benchmark$LibSVMline, fileConn)
close(fileConn)

# Creating folds

## Random partitioning per query groups
set.seed(232323)
#benchmarkPartitions <- benchmark %>% group_by(qid) %>% sample()

qids <- benchmark %>% select(qid) %>% distinct()
qids$partition <- sample(factor(rep(1:5, length.out=nrow(qids)), labels=1:5))
benchmark <- left_join(benchmark, qids, by=c("qid"="qid"))
#partitions <- split(benchmark, benchmark$partition)

## Folds
#partitionCombinations <- combinations(5,3)
createFold <- function(benchmark,foldId,train,validation,test,featureList,outputPath) {
    foldFolder <- paste0(outputPath,"folds/fold",foldId,"/")
    dir.create(foldFolder, showWarnings = FALSE, recursive = TRUE)
    ### Create train/validation/test split
    train <- benchmark %>% filter(partition %in% train)
    validation <- benchmark %>% filter(partition %in% validation)
    test <- benchmark %>% filter(partition %in% test)
    ### Write to file
    writeToFile <- function(lines,filename) {
        fileConn <- file(filename)
        writeLines(lines, fileConn)
        close(fileConn)    
    }
    writeToFile(train$LibSVMline,paste0(foldFolder,"LOVBench_Fold",foldId,"_Train.txt"))
    writeToFile(validation$LibSVMline,paste0(foldFolder,"LOVBench_Fold",foldId,"_Validation.txt"))
    writeToFile(test$LibSVMline,paste0(foldFolder,"LOVBench_Fold",foldId,"_Test.txt"))
    
    # Create baseline snippets
    for (featureRow in 1:nrow(featureList)) {
        baseline <- test %>% select(label, qid, featureList$Name[featureRow])
        # baseline <- baseline %>% mutate(LibSVMline=paste(label, paste0("qid:", qid)))
        # baseline <- baseline %>% arrange(qid,desc(featureList$Name[featureRow]),label)
        # baseline <- baseline %>% mutate(LibSVMline=paste(get(featureList$Name[featureRow]),paste0("qid:", qid)))
        arrange <- c("qid", paste0("desc(",featureList$Name[featureRow],")"), "label")
        baseline <- baseline %>% arrange_(.dots=arrange)
        baseline <- baseline %>% mutate(LibSVMline=paste(label, paste0("qid:", qid), paste0(featureList$ID[featureRow], ":", get(featureList$Name[featureRow]))))
        fileConn <- file(paste0(foldFolder,"Baseline_Fold",foldId,"_",featureRow,".txt"))
        writeLines(baseline$LibSVMline, fileConn)
        close(fileConn)
    }
    
}
createFold(benchmark,1,c(1,2,3),c(4),c(5),featureList,outputPath)
createFold(benchmark,2,c(2,3,4),c(5),c(1),featureList,outputPath)
createFold(benchmark,3,c(3,4,5),c(1),c(2),featureList,outputPath)
createFold(benchmark,4,c(4,5,1),c(2),c(3),featureList,outputPath)
createFold(benchmark,5,c(5,1,2),c(3),c(4),featureList,outputPath)

