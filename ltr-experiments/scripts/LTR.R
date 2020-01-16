
train <- function(foldsPath,modelPath,rankerNames,featureConfiguration,foldId,rankerId,rankerParameters,metric2train="NDCG@10",norm="no-normalization",gmax="4",metric2test="NDCG@10") {
    trainingSet <- paste0(foldsPath,"fold",foldId,"/LOVBench_Fold",foldId,"_Train.txt")
    validationSet <- paste0(foldsPath,"fold",foldId,"/LOVBench_Fold",foldId,"_Validation.txt")
    testSet <- paste0(foldsPath,"fold",foldId,"/LOVBench_Fold",foldId,"_Test.txt")
    modelFile <- paste0(modelPath,featureConfiguration,"_Fold",foldId,"_",rankerNames[rankerId],".txt")
    featureFile <- paste0("../resources/configurations/",featureConfiguration,".txt")
    
    command <- paste("java -jar",rankLibJar,"-train",trainingSet,"-validate",validationSet,"-test",testSet,"-ranker",rankerId,"-feature",featureFile,"-metric2t",metric2train,"-gmax",gmax,"-metric2T",metric2test,rankerParameters,"-save",modelFile)
    if (norm != "no-normalization") {
        command <- paste(command,"-norm",norm)
    }
    print(command)
    system(command)
}

test <- function(featureConfiguration,foldId,rankerId,rankerNames,metric2test) {
    modelFile <- paste0(modelPath,featureConfiguration,"_Fold",foldId,"_",rankerNames[rankerId],".txt")
    testSet <- paste0(foldsPath,"fold",foldId,"/LOVBench_Fold",foldId,"_Test.txt")
    tmpfile <- tempfile()
    command <- paste("java -jar",rankLibJar,"-load",modelFile,"-test",testSet,"-metric2T",metric2test,">",tmpfile)
    print(command)
    system(command)
    
    # Read the test results
    result <- readLines(tmpfile)
    resultLast <- result[length(result)]
    score <- tail(strsplit(resultLast, split=" ")[[1]],1)
    print(score)
    as.numeric(score)
}

idvTest <- function(foldsPath,modelPath,testPath,rankerNames,featureConfiguration,foldId,rankerId,norm,gmax,metric2test) {
    modelFile <- paste0(modelPath,featureConfiguration,"_Fold",foldId,"_",rankerNames[rankerId],".txt")
    testSet <- paste0(foldsPath,"fold",foldId,"/LOVBench_Fold",foldId,"_Test.txt")
    idvFile <- gsub("@","",paste0(testPath,"fold",foldId,"/",metric2test,"/",featureConfiguration,"_Fold",foldId,"_",rankerNames[rankerId],"_",metric2test,".txt"))
    dir.create(dirname(idvFile), showWarnings = FALSE, recursive = TRUE)
    command <- paste("java -jar",rankLibJar,"-load",modelFile,"-test",testSet,"-metric2T",metric2test,"-gmax",gmax,"-idv",idvFile)
    if (norm != "no-normalization") {
        command <- paste(command,"-norm",norm)
    }
    print(command)
    system(command)
}

idvTestBaseline <- function(foldsPath,baselineFeatureId,testPath,rankerNames,foldId,rankerId,norm,gmax,metric2test) {
    testSet <- paste0(foldsPath,"fold",foldId,"/Baseline_Fold",foldId,"_",baselineFeatureId,".txt")
    idvFile <- gsub("@","",paste0(testPath,"fold",foldId,"/",metric2test,"/Baseline_",baselineFeatureId,"_Fold",foldId,"_",rankerNames[rankerId],"_",metric2test,".txt"))
    dir.create(dirname(idvFile), showWarnings = FALSE, recursive = TRUE)
    command <- paste("java -jar",rankLibJar,"-test",testSet,"-metric2T",metric2test,"-gmax",gmax,"-idv",idvFile)
    if (norm != "no-normalization") {
        command <- paste(command,"-norm",norm)
    }
    print(command)
    system(command)
}

compare <- function(testPath,resultPath,baselineFeatureId,foldId,rankerId,rankerNames,metric2test) {
    idvPath <- gsub("@","",paste0(testPath,"fold",foldId,"/",metric2test,"/"))
    baselineIdvFilename <- gsub("@","",paste0("Baseline_",baselineFeatureId,"_Fold",foldId,"_",rankerNames[rankerId],"_",metric2test,".txt"))
    resultFile <- gsub("@","",paste0(resultPath,rankerNames[rankerId],"/Comparison_Fold",foldId,"_",rankerNames[rankerId],"_",metric2test,".txt"))
    dir.create(dirname(resultFile), showWarnings = FALSE, recursive = TRUE)
    command <- paste("java -cp",rankLibJar,"ciir.umass.edu.eval.Analyzer -all", idvPath,"-base",baselineIdvFilename,">",resultFile)
    command <- gsub("@","",command)
    print(command)
    system(command)
}

summarise_comparison <- function(countFolds, metrics2test, featureConfigurations, resultPath,rankerNames,rankerId) {
    resultColNames <- c("Ranker", "Configuration", "Fold")
    for (metric2test in metrics2test) {
        resultColNames <- c(resultColNames, metric2test, paste0(metric2test,"_improvement"), paste0(metric2test,"_pvalue"))
    }
    resultsTable <- setNames(data.frame(matrix(ncol = length(resultColNames), nrow = 0)), resultColNames)
    
    ranker <- rankerNames[rankerId]
    for (featureConfiguration in c("Baseline",featureConfigurations)) {
        print(featureConfiguration)
        for (foldId in 1:countFolds) {
            metricScores <- c()
            for (metric2test in metrics2test) {
                resultFile <- gsub("@","",paste0(resultPath,rankerNames[rankerId],"/Comparison_Fold",foldId,"_",rankerNames[rankerId],"_",metric2test,".txt"))
                print(resultFile)
                comparisonTable <- read.table(resultFile, header = T, sep = "\t", skip = length(featureConfigurations)+5, nrows = length(featureConfigurations)+1, fill = T)
                comparisonRow <- comparisonTable[grep(paste0(featureConfiguration,"_"), comparisonTable$System),]
                metricScores <- c(metricScores,comparisonRow$Performance,as.character(comparisonRow$Improvement),comparisonRow$p.value)
            }
            comparisonRow <- comparisonTable[grep(paste0(featureConfiguration,"_"), comparisonTable$System),]
            configuration <- sub("_.*", "", comparisonRow$System)
            resultsTable[nrow(resultsTable) + 1,] <- c(ranker,configuration,foldId,metricScores)
        }
        meanScores <- c()
        filteredTable <- resultsTable %>% filter(Configuration==featureConfiguration)
        #print(filteredTable)
        
        for (col in 4:(length(metrics2test)*3+3)) {
            metricScores <- filteredTable[,col]
            meanScores <- c(meanScores,mean(as.numeric(metricScores)))
        }
        #print(meanScores)
        resultsTable[nrow(resultsTable) + 1,] <- c(ranker,configuration,"MEAN",meanScores)
    }
    resultsTable
}

