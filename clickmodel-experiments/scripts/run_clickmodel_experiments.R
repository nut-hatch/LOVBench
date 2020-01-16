library(dplyr)
library(jsonlite)
library(plyr)
library(tidyverse)

# Reading command line args and setting working directory
args <- commandArgs(trailingOnly=TRUE)
if (length(args)<1) {
    stop("Usage: Rscript run_clickmodel_experiments.R directoryOfSourceFile", call.=FALSE)
}
setwd(args[1])

# Paths
currentTime <- as.numeric(Sys.time())
outputPath <- paste0("../resources/",currentTime,"/")
projectResourcePath <- "../../resources/"
dir.create(outputPath, showWarnings = FALSE, recursive = TRUE)

# Load functions
source("./relevance/relevance.R")
source("./relevance/uri.R")

# Learning and testing user click models
command <- paste("python","./model/LOVCLickModel.py",projectResourcePath,outputPath,paste0(projectResourcePath,"LOV_SearchLogs_Clean.txt"),"DCTR","DBN","UBM")
print(command)
system(command)


# Reading output from user click model
satisfactionProbabilityDCTR <- read.csv(paste0(outputPath,"DCTR_SatisfactionProbability_Raw.csv"), header = FALSE, stringsAsFactors = FALSE)
satisfactionProbabilityDBN <- read.csv(paste0(outputPath,"DBN_SatisfactionProbability_Raw.csv"), header = FALSE, stringsAsFactors = FALSE)
satisfactionProbabilityUBM <- read.csv(paste0(outputPath,"UBM_SatisfactionProbability_Raw.csv"), header = FALSE, stringsAsFactors = FALSE)
columns <- c("query","term","probability")
colnames(satisfactionProbabilityDCTR) <- columns
colnames(satisfactionProbabilityDBN) <- columns
colnames(satisfactionProbabilityUBM) <- columns

# Infer relevance labels
RelevanceLabelsDCTR <- mapProbabilitesToLabels(satisfactionProbabilityDCTR) %>% select(query, term, label) %>% group_by(query) %>% add_count(query) %>% filter(n == 10) %>% select(-n)
RelevanceLabelsDBN <- mapProbabilitesToLabels(satisfactionProbabilityDBN) %>% select(query, term, label) %>% group_by(query) %>% add_count(query) %>% filter(n == 10) %>% select(-n)
RelevanceLabelsUBM <- mapProbabilitesToLabels(satisfactionProbabilityUBM) %>% select(query, term, label) %>% group_by(query) %>% add_count(query) %>% filter(n == 10) %>% select(-n)

# Write relevance labels to file
write.csv(RelevanceLabelsDCTR, file = paste0(outputPath,"DCTR_RelevanceLabels_Raw.csv"), quote = T, row.names = F)
write.csv(RelevanceLabelsDBN, file = paste0(outputPath,"DBN_RelevanceLabels_Raw.csv"), quote = T, row.names = F)
write.csv(RelevanceLabelsUBM, file = paste0(outputPath,"UBM_RelevanceLabels_Raw.csv"), quote = T, row.names = F)

# Map short terms to full URI
termMappingFile <- paste0(projectResourcePath,"LOV_Corpus/TermMapping.csv")
LOVPrefixFile <- paste0(projectResourcePath,"LOV_Corpus/2019-08-06_lov_prefixes_edit.json")
LOVnqFile <- paste0(projectResourcePath,"LOV_Corpus/2019-08-06_lov-fix.nq")

if (!file.exists(termMappingFile)) {
    terms <- separate(satisfactionProbabilityDCTR, term,c("prefix","localname"), ":", remove=F) %>% select(term, prefix,localname) %>% distinct()
    termMapping <- createTermMapping(LOVPrefixFile, LOVnqFile, terms)
    write.csv(termMapping, file <- paste0(outputPath,"TermMapping.csv"), quote = T, row.names = F)
} else {
    termMapping <- read.csv(termMappingFile)
}

satisfactionProbabilityDCTR <- mapToFullTerms(satisfactionProbabilityDCTR, termMapping, paste0(outputPath,"DCTR_SatisfactionProbability.csv"), "probability")
satisfactionProbabilityDBN <- mapToFullTerms(satisfactionProbabilityDBN, termMapping, paste0(outputPath,"DBN_SatisfactionProbability.csv"), "probability")
satisfactionProbabilityUBM <- mapToFullTerms(satisfactionProbabilityUBM, termMapping, paste0(outputPath,"UBM_SatisfactionProbability.csv"), "probability")
RelevanceLabelsDCTR <- mapToFullTerms(RelevanceLabelsDCTR, termMapping, paste0(outputPath,"DCTR_RelevanceLabels.csv"), "label")
RelevanceLabelsDBN <- mapToFullTerms(RelevanceLabelsDBN, termMapping, paste0(outputPath,"DBN_RelevanceLabels.csv"), "label")
RelevanceLabelsUBM <- mapToFullTerms(RelevanceLabelsUBM, termMapping, paste0(outputPath,"UBM_RelevanceLabels.csv"), "label")

# Evaluation of relevance labels with CBRBench
cbrBench <- read.csv(paste0(projectResourcePath,"CBRBench/CBRBenchGroundTruth.csv"), header = FALSE, stringsAsFactors = FALSE)
colnames(cbrBench) <- c("query","term","rating","label")
cbrBench <- cbrBench %>% select(query, term, label)

queries <- c("person", "title", "time", "location", "address", "organization", "name", "event", "author", "music")
joinedLabelsDCTR <- cbrBench %>% filter(query %in% queries) %>% inner_join(RelevanceLabelsDCTR %>% filter(query %in% queries), by=c("query"="query", "term"="fullTerm"))
joinedLabelsDBN <- cbrBench %>% filter(query %in% queries) %>% inner_join(RelevanceLabelsDBN %>% filter(query %in% queries), by=c("query"="query", "term"="fullTerm"))
joinedLabelsUBM <- cbrBench %>% filter(query %in% queries) %>% inner_join(RelevanceLabelsUBM %>% filter(query %in% queries), by=c("query"="query", "term"="fullTerm"))
joinedSatisfactionDCTR <- cbrBench %>% filter(query %in% queries) %>% inner_join(satisfactionProbabilityDCTR %>% filter(query %in% queries), by=c("query"="query", "term"="fullTerm"))
joinedSatisfactionDBN <- cbrBench %>% filter(query %in% queries) %>% inner_join(satisfactionProbabilityDBN %>% filter(query %in% queries), by=c("query"="query", "term"="fullTerm"))
joinedSatisfactionUBM <- cbrBench %>% filter(query %in% queries) %>% inner_join(satisfactionProbabilityUBM %>% filter(query %in% queries), by=c("query"="query", "term"="fullTerm"))


rcDCTR <- rc(joinedSatisfactionDCTR,"DCTR")
rcDBN <- rc(joinedSatisfactionDBN,"DBN")
rcUBM <- rc(joinedSatisfactionUBM,"UBM")
rcTable <- rbind(rcDCTR,rcDBN,rcUBM)
write.csv(rcTable, file = paste0(outputPath,"Correlation_CBRBench_LOVBench.csv"), quote = T, row.names = F)

# From now, UBM only!
labels <- RelevanceLabelsUBM %>% select(query,fullTerm,label)
# Add non-relevant labels

# Load pool of all terms in LOV
termFile <- paste0(projectResourcePath,"LOV_Corpus/Terms.csv")
terms <- read.csv(termFile,header = T) %>% filter(grepl("http*",term))

# Sample non relevant terms
nonReleventTermsFile <- paste0(projectResourcePath,"NonRelevantTermsSampled.csv")
nonRelevantTermsTable <- read.csv(nonReleventTermsFile,header = T)

# write to file
groundTruthFile <- paste0(outputPath,"LOVBench_GroundTruth.csv")
labels <- rbind(labels, nonRelevantTermsTable)
write.csv(labels %>% arrange(query,desc(label)), file = groundTruthFile, quote = T, row.names = F)

# Distribution of relevance labels
labels %>% group_by(label) %>% add_count(label) %>% select(label, n) %>% unique() %>% arrange(as.numeric(label)) %>% mutate(percentage=n/sum(n)*100)

# Average sample size
mean(group_size(labels %>% group_by(query)))

# test <- labels %>% group_by(query) %>% add_count(query) %>% filter(n == 10)
# test %>% filter(n == 10) %>%  select(-n) %>% group_by(label) %>% add_count(label) %>% select(label, n) %>% unique() %>% arrange(as.numeric(label)) %>% mutate(percentage=n/sum(n)*100)
