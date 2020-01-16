library(utils)
library(dplyr)

currentTime <- as.numeric(Sys.time())
outputPath <- paste0("../resources/output/",currentTime,"/")

modelPath <- paste0(outputPath,"models/")
testPath <- paste0(outputPath,"tests/")
resultPath <- paste0(outputPath,"results/")
foldsPath <- "../resources/output/folds/"
rankLibJar <- "../resources/bin/RankLib.jar"
countFolds <- 5


makeDirs <- function() {
    dir.create(outputPath, showWarnings = FALSE, recursive = TRUE)
    dir.create(modelPath, showWarnings = FALSE, recursive = TRUE)
    dir.create(testPath, showWarnings = FALSE, recursive = TRUE)
    dir.create(resultPath, showWarnings = FALSE, recursive = TRUE)
}

rankerNames <- c("RankNet", "RankBoost", "AdaRank", "Coordinate Ascent", "", "LambdaMART", "ListNet", "Random_Forests")

args <- commandArgs(trailingOnly=TRUE)
if (length(args)<2) {
    stop("Help to train and test a model: <<train.R train>>. Help to train, test and compare several models: <<train.R compare>>", call.=FALSE)
}

setwd(args[2])

source("./LTR.R")

## Download RankLib if it does not exist
ranklibPath <- "../resources/bin/Ranklib.jar"
if (!file.exists(ranklibPath)) {
    dir.create("../resources/bin/", showWarnings = FALSE, recursive = TRUE)
    download.file("https://sourceforge.net/projects/lemur/files/lemur/RankLib-2.1/RankLib-2.1-patched.jar/download", ranklibPath, method="auto")
}

task <- args[1]
if (task == "train") {
    if (length(args)<3) {
        stop("Usage: train.R train directoryOfSourceFile featureConfigurationFile rankerId ", call.=FALSE)
    }
    featureConfigurationFile <- args[3]
    rankerId <- as.numeric(args[4])
    makeDirs()
    
    resultColNames <- c("FeatureConfiguration", "RankerName", "Fold", "NDCG_3", "NDCG_5", "NDCG_10", "MAP")
    resultsTable <- setNames(data.frame(matrix(ncol = 7, nrow = 0)), resultColNames)
    
    for (foldId in 1:countFolds) {
        train(foldsPath,modelPath,featureConfigurationFile,foldId,rankerId,rankerNames)
        ndcg3 <- test(featureConfigurationFile,foldId,rankerId,rankerNames,"NDCG@3")
        ndcg5 <- test(featureConfigurationFile,foldId,rankerId,rankerNames,"NDCG@5")
        ndcg10 <-test(featureConfigurationFile,foldId,rankerId,rankerNames,"NDCG@10")
        map <- test(featureConfigurationFile,foldId,rankerId,rankerNames,"MAP")
        
        resultRow <- setNames(data.frame(modelName,rankerNames[rankerId],foldId,ndcg3,ndcg5,ndcg10,map),resultColNames)
        resultsTable <- rbind(resultsTable,resultRow)
    }
    
    resultRow <- setNames(data.frame(modelName,rankerNames[rankerId],"MEAN",mean(resultsTable$NDCG_3),mean(resultsTable$NDCG_5),mean(resultsTable$NDCG_10),mean(resultsTable$MAP)),resultColNames)
    resultsTable <- rbind(resultsTable,resultRow)
    print(resultsTable)
    
    write.csv(resultsTable,paste0(modelPath,modelName,"_Tests_",rankerNames[rankerId],".csv"), row.names = FALSE)
    
} else if (task == "compare") {
    if (length(args)<10) {
        stop("Usage: Rscript ./train.R compare directoryOfSourceFile rankerId metric2train norm gmax metric2test baselineFeatureId rankerParameters featureConfigurationFiles...", call.=FALSE)
    }
    rankerId <- as.numeric(args[3])
    metric2train <- args[4]
    norm <- args[5]
    gmax <- args[6]
    metric2test <- args[7]
    baselineFeatureId <- args[8]
    rankerParameters <- args[9]
    featureConfigurationFiles <- args[10:length(args)]
    makeDirs()
    fileConn <- file(paste0(outputPath,"args.txt"))
    writeLines(args, fileConn)
    close(fileConn)
    
    # First: train each model on all folds
    for (featureConfiguration in featureConfigurationFiles) {
        for (foldId in 1:countFolds) {
            train(foldsPath,modelPath,rankerNames,featureConfiguration,foldId,rankerId,rankerParameters,metric2train,norm,gmax,metric2test)
        }
    }
    
    #metrics2test <- c("MAP","NDCG@3","NDCG@5","NDCG@10","ERR@3","ERR@5","ERR@10")
    metrics2test <- c("MAP","NDCG@3","NDCG@5","NDCG@10")
    # Second: for all folds, make tests
    for (foldId in 1:countFolds) {
        for (metric2test in metrics2test) {
            print(metric2test)
            idvTestBaseline(foldsPath,baselineFeatureId,testPath,rankerNames,foldId,rankerId,norm,gmax,metric2test)
            for (featureConfiguration in featureConfigurationFiles) {
                idvTest(foldsPath,modelPath,testPath,rankerNames,featureConfiguration,foldId,rankerId,norm,gmax,metric2test)
            }
        }
    }
    
    # Third: for all tests, make comparison
    for (foldId in 1:countFolds) {
        for (metric2test in metrics2test) {
            compare(testPath,resultPath,baselineFeatureId,foldId,rankerId,rankerNames,metric2test)
        }
    }
    
    # Fourth: compose single evaluation file
    resultsTable <- summarise_comparison(countFolds, metrics2test, featureConfigurationFiles, resultPath, rankerNames, rankerId)
    print(resultsTable)
    write.csv(resultsTable, file = paste0(outputPath,"Results.csv"), quote = T, row.names = F)
}

