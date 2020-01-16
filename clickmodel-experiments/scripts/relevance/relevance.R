
# Map satisfaction probability to relevance labels
mapProbabilitesToLabels <- function(satisfactionProbabilityTable) {
    satisfactionProbabilityTable <- satisfactionProbabilityTable %>% mutate(
        label = case_when(
            probability < 0.2 ~ -2,
            probability >= 0.2 & probability < 0.45 ~ 0,
            probability >= 0.45 & probability < 0.5 ~ 1,
            probability >= 0.5 & probability < 0.6 ~ 2,
            probability >= 0.6 & probability < 0.7 ~ 3,
            probability >= 0.7 ~ 4
        )
    )
    satisfactionProbabilityTable
}

DCG_simple <- function(y) {
    sum(y/log(1:length(y) +1 , base = 2))
}

NDCG_simple <- function(ideal_x, x) {
    DCG_simple(x)/DCG_simple(ideal_x)
}

DCG_common <- function(y) {
    sum(((2^y) -1)/log((1:length(y)+1) , base = 2))
}

NDCG_common <- function(ideal_x, x) {
    DCG_common(x)/DCG_common(ideal_x)
}

rc <- function(joinedSatisfaction,modelName) {
    resultColNames <- c("ModelName", "KendallRC", "PearsonRC")
    resultsTable <- setNames(data.frame(matrix(ncol = 3, nrow = 0)), resultColNames)
    ranking_model <- c()
    ranking_ideal <- c()
    for(queryStr in joinedSatisfaction$query) {
        scoresForQuery <- joinedSatisfaction %>% filter(query==queryStr)
        scoresForQuery$probability <- as.numeric(scoresForQuery$probability)
        scoresForQuery <- scoresForQuery %>% arrange(desc(probability)) %>% mutate(ranking_model=seq_along(probability))
        scoresForQuery <- scoresForQuery %>% arrange(desc(label)) %>% mutate(ranking_ideal=seq_along(label))
        ranking_model <- c(ranking_model,scoresForQuery$ranking_model)
        ranking_ideal <- c(ranking_ideal,scoresForQuery$ranking_ideal)
    }
    rc_kendall <- cor(ranking_model,ranking_ideal,method = "kendall", use = "pairwise")
    rc_pearson <- cor(ranking_model,ranking_ideal,method = "pearson", use = "pairwise")
    resultRow <- setNames(data.frame(modelName,rc_kendall,rc_pearson),resultColNames)
    resultsTable <- rbind(resultsTable,resultRow)
    resultsTable
}
 
compare <- function(joinedLabels,modelName) {
    resultColNames <- c("ModelName", "Query", "DCG", "DCG_ideal", "NDCG")
    resultsTable <- setNames(data.frame(matrix(ncol = 5, nrow = 0)), resultColNames)
    for (queryFilter in unique(joinedLabels$query)) {
        filteredScores <- joinedLabels %>% filter(query==queryFilter)
        filteredScores_ideal <- filteredScores %>% arrange(desc(label.x))
        filteredScores_model <- filteredScores %>% arrange(desc(label.y),label.x)
        DCGs <- DCG_common(filteredScores_model$label.x)
        DCGs_ideal <- DCG_common(filteredScores_ideal$label.x)
        NDCGs <- NDCG_common(filteredScores_ideal$label.x,filteredScores_model$label.x)
        resultRow <- setNames(data.frame(modelName,queryFilter,mean(DCGs),mean(DCGs_ideal),mean(NDCGs)),resultColNames)
        resultsTable <- rbind(resultsTable,resultRow)
    }
    resultRow <- setNames(data.frame(modelName,"MEAN",mean(resultsTable$DCG),mean(resultsTable$DCG_ideal),mean(resultsTable$NDCG)),resultColNames)
    resultsTable <- rbind(resultsTable,resultRow)
    resultsTable
}
