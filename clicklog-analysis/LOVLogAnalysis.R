#' ---
#' title: Cleaning and Exploration of LOV Query and Click Logs
#' author: Anonymous
#' output:  
#'   prettydoc::html_pretty:
#'     toc: true
#'     number_sections: true
#'     theme: cayman
#'     highlight: github
#' ---


#' # Introduction
#' The purpose of this R script is to clean and explore the LOV query and click logs for vocabularies and terms as part of the WWW'20 submission titled "". The log files are analysed and discussed to understand LOV user search and click behaviour that guide and support the design of ranking features and interpretation of their performance results for ranking as presented in the paper.
#' 
#' # Set Up
#' 
#' Basic setup of variables and dataframes.
#' 
#' ## Parsing and Exploration of Log Files
#' 
#' Initialisation
library(jsonlite)
library(tidyverse)
library(plyr)
library(knitr)
library(kableExtra)

logDumpDate <- "2019-08-06"
scriptVersion <- "5"
currentTime <- as.numeric(Sys.time())

#' Setting up input and output paths. This script requires four files that are provided by LOV:
#' 
#' 1. term query log
#' 2. term click log
#' 3. vocab query log
#' 4. vocab click log
inputPath <- paste0("../resources/input/",logDumpDate,"_logs_query_click/")
outputPath <- paste0("../resources/output/",currentTime,"/",logDumpDate,"_logs_query_click/")
dir.create(outputPath, showWarnings = FALSE, recursive = TRUE)

queryTermLogFile <- paste0(inputPath,"logQueryTermEvents.json")
clickTermLogFile <- paste0(inputPath,"logClickTermEvents.json")
queryVocabLogFile <- paste0(inputPath,"logQueryVocEvents.json")
clickVocabLogFile <- paste0(inputPath,"logClickVocEvents.json")

queryTermLogFileValidJson <- paste0(outputPath,"logQueryTermEvents_valid.json")
clickTermLogFileValidJson <- paste0(outputPath,"logClickTermEvents_valid.json")
queryVocabLogFileValidJson <- paste0(outputPath,"logQueryVocEvents_valid.json")
clickVocabLogFileValidJson <- paste0(outputPath,"logClickVocEvents_valid.json")

#' Output files of cleaned logs for terms and vocabs:
termQueryClickLogYandexFile <- paste0(outputPath,"LOV_QueryClickLog_YandexFormat_Terms", "_v", scriptVersion, ".txt")
vocabQueryClickLogYandexFile <- paste0(outputPath,"LOV_QueryClickLog_YandexFormat_Vocabs", "_v", scriptVersion, ".txt")

#' 
#' Log files need to be converted to valid JSON, as original logs lack outer array brackets.
#' 
#' Converting all logs to valid json files.
#' 
createValidJsonLogFile <- function(logFilePath,validJsonLogFilePath) {
    lines <- readLines(logFilePath)
    lines <- paste0("[",paste(unlist(lines), collapse=',\n'),"]")
    fileConn <- file(validJsonLogFilePath)
    writeLines(lines, fileConn)
    close(fileConn)
} 
createValidJsonLogFile(queryTermLogFile,queryTermLogFileValidJson)
createValidJsonLogFile(clickTermLogFile,clickTermLogFileValidJson)
createValidJsonLogFile(queryVocabLogFile,queryVocabLogFileValidJson)
createValidJsonLogFile(clickVocabLogFile,clickVocabLogFileValidJson)

#' Finally we load the dataframes from the valid json files:
termQueriesRaw <- fromJSON(queryTermLogFileValidJson, flatten = TRUE)
termClicksRaw <- fromJSON(clickTermLogFileValidJson, flatten = TRUE)
vocQueriesRaw <- fromJSON(queryVocabLogFileValidJson, flatten = TRUE)
vocClicksRaw <- fromJSON(clickVocabLogFileValidJson, flatten = TRUE)

#vocPrefixes <- fromJSON("../../feature-extraction/src/main/resources/input/2019-06-17_lov_prefixes_edit.json")$results$binding

#' 
#' ## Glancing at the Log Files
#' 
#' Glance at LOV *term* *query* log entries:
#+ results='asis'
knitr::kable(head(termQueriesRaw,5)) %>% kable_styling("striped") %>% scroll_box(width = "100%")

#' Glance at LOV *term* *click* log entries:
#+ results='asis'
knitr::kable(head(termClicksRaw,5)) %>% kable_styling("striped") %>% scroll_box(width = "100%")

#' Glance at LOV *vocabulary* *query* log entries:
#+ results='asis'
knitr::kable(head(vocQueriesRaw,5)) %>% kable_styling("striped") %>% scroll_box(width = "100%")

#' Glance at LOV *vocabulary* *click* log entries:
#+ results='asis'
knitr::kable(head(vocClicksRaw,5)) %>% kable_styling("striped") %>% scroll_box(width = "100%")

#' ## Preliminary Data Cleaning and Preparation
#' 
#' Removing all sessions without clicks, and those with clicks without query:
termQueries <- termQueriesRaw %>% filter(!is.na(sessionId), sessionId %in% termClicksRaw$sessionId)
termClicks <- termClicksRaw %>% filter(!is.na(sessionId), sessionId %in% termQueriesRaw$sessionId)
vocQueries <- vocQueriesRaw %>% filter(!is.na(sessionId), sessionId %in% vocClicksRaw$sessionId)
vocClicks <- vocClicksRaw %>% filter(!is.na(sessionId), sessionId %in% vocQueriesRaw$sessionId)

#' LOV queries are case-insensitive, so we transform all queries to lower case. Also surrouding whitespace is removed.
termQueries <- termQueries %>% mutate(searchWords=trimws(tolower(searchWords)),filterTypes=tolower(filterTypes),filterTags=tolower(filterTags),filterVocs=tolower(filterVocs))
vocQueries <- vocQueries %>% mutate(searchWords=trimws(tolower(searchWords)), filterTags=tolower(filterTags), filterLangs=tolower(filterLangs))

#' LOV search also ignores quotes. We remove them as they correspond to the exact same query without the quotes.
termQueries <- termQueries %>% mutate(searchWords=gsub("&quot;", "", searchWords))
vocQueries <- vocQueries %>% mutate(searchWords=gsub("&quot;", "", searchWords))

#' We generate output in the same format as the well-known Yandex click log datasets.
#' Thus, we add and rename the columns correspondingly.
#' 
#' 1. For query actions: SessionID TimePassed TypeOfAction QueryID RegionID ListOfURLs
#' 2. For click actions: SessionID TimePassed TypeOfAction URLID
termQueries <- termQueries %>% 
    mutate(SessionID=sessionId,
           TypeOfAction="Q",
           QueryID=paste0("searchWords=",searchWords,"&filterTypes=",filterTypes,"&filterTags=",filterTags,"&filterVocs=",filterVocs),
           RegionID=0,
           ListOfURLs=results,
           hasFilter=(filterTypes!="" | filterTags!="" | filterVocs!=""),
           hasFullResultPage=(nbResults>=10)) %>% 
    select(SessionID,TypeOfAction,QueryID,searchWords,filterTypes,filterTags,filterVocs,RegionID,ListOfURLs,date,page,hasFilter,hasFullResultPage)
termClicks <- termClicks %>% 
    mutate(SessionID=sessionId,
           TypeOfAction="C",
           URLID=clickedTerm) %>% 
    select(SessionID,TypeOfAction,URLID,date)
vocQueries <- vocQueries %>% 
    mutate(SessionID=sessionId,
           TypeOfAction="Q",
           QueryID=paste0("searchWords=",searchWords,"&filterTags=",filterTags,"&filterLangs=",filterLangs),
           RegionID=0,
           ListOfURLs=results,
           hasFilter=(filterTags!="" | filterLangs!=""),
           hasFullResultPage=(nbResults>=15)) %>% 
    select(SessionID,TypeOfAction,QueryID,searchWords,filterTags,filterLangs,RegionID,ListOfURLs,date,page,hasFilter,hasFullResultPage)
vocClicks <- vocClicks %>% 
    mutate(SessionID=sessionId,TypeOfAction="C",URLID=clickedVoc) %>% 
    select(SessionID,TypeOfAction,URLID,date)

#' TimePassed column will be computed after proper cleaning, as it depends on previous entries.
#' 
#' Yandex format keeps queries and clicks in a single log. Thus, we join the query and click logs into a single dataframe.
#' 
termQueryClickLog <- rbind.fill(termQueries,termClicks)
termQueryClickLog <- termQueryClickLog %>% arrange(SessionID,date)
vocabQueryClickLog <- rbind.fill(vocQueries,vocClicks)
vocabQueryClickLog <- vocabQueryClickLog %>% arrange(SessionID,date)

#' 
#' # Detailed Filtering of Query-Click Logs
#' 
#' We remove useless entries from the log and generate the joint log files in the well-known Yandex format.
#' 
#' ## Filtering
#' 
#' Following queries are removed:
#' 
#' - queries beyond the first SERP (result page)
#' - queries with no keywords
#' - queries that delivered no results
#' - queries that use a filter
#' - queries with result lists not filling the complete first SERP
#' - blacklisted queries containing potentially personaly identifyable information (email addresses etc.)
#' - for clicks with no correspondences in the previous query result list.
#' 
#' Cleaning needs to be done in a loop in order to identify clicks associated with removed queries.

piiQueries <- readLines(paste0(inputPath,"PIIQueries.txt"))
 
cleanLog <- function(queryClickLog) {
    rowsToBeRemoved <- c()
    multipleTabSessions <- c()
    lastResults <- ""
    for (rowNumber in 1:nrow(queryClickLog)) {
        row <- queryClickLog[rowNumber,]
        # If a query, check if query is valid and if not remove query + clicks
        if(row$TypeOfAction=="Q") {
            lastResults <- queryClickLog[rowNumber,]$ListOfURLs
            if ((!is.na(row$page) & row$page!="1") # not first page
                | (row$searchWords == "") # empty search word
                #| (row$searchWords %in% vocPrefixes$vocabPrefix$value) # searchWords equals a vocabulary prefix
                | (row$hasFilter) # ignore queries with filter
                #| ((!grepl("&filterTypes=&filterTags=&filterVocs=$",row$QueryID)) || (!grepl("&filterTags=&filterLangs=$",row$QueryID))) # ignore all queries with filters
                | (row$ListOfURLs == "") # no result list
                | (!row$hasFullResultPage) # SERP not complete
                | (row$searchWords) %in% piiQueries # sensitive query
                | (grepl("iso37120:$|iso37120:,",row$ListOfURLs)) # error in click log
            ) {
                rowsToBeRemoved <- c(rowsToBeRemoved, rowNumber)
                counter <- rowNumber+1
                # print(paste("Removing ", rowNumber, " with query ",  row$QueryID, " and page ", row$page, "and empty result", row$ListOfURLs == "" ,". Now checking subsequent event: ", termQueryClickLog[counter,2]))
                # if (counter <= nrow(queryClickLog)) {
                    # print(counter)
                    # print(queryClickLog[counter,])
                    while(counter <= nrow(queryClickLog) & queryClickLog[counter,2]=="C") {
                        rowsToBeRemoved <- c(rowsToBeRemoved, counter)
                        counter <- counter + 1
                    }
                # }
            }
        }
        # if a click, check whether clicked element is contained in previous queried result list
        if(row$TypeOfAction=="C") {
            if(!grepl(row$URLID,lastResults)) {
                multipleTabSessions <- c(multipleTabSessions, row$SessionID)
            }
        }
    }
    queryClickLog <- queryClickLog[-rowsToBeRemoved,]
    queryClickLog <- queryClickLog %>% filter(!SessionID %in% unique(multipleTabSessions))
    queryClickLog
}
termQueryClickLog <- cleanLog(termQueryClickLog)
vocabQueryClickLog <- cleanLog(vocabQueryClickLog)

#' Compute "TimePassed" column based on timestamps in sessions
minDatePerSessionTerm <- termQueryClickLog %>% group_by(SessionID) %>% arrange(date) %>% filter(row_number() == 1) %>% mutate(minDateOfSession=date) %>% select(SessionID,minDateOfSession)
termQueryClickLog <- termQueryClickLog %>% 
    left_join(minDatePerSessionTerm,by="SessionID") %>%
    mutate(TimePassed=round(date-minDateOfSession,0)) %>%
    select(SessionID,TimePassed,TypeOfAction,QueryID,searchWords,filterTypes,filterTags,filterVocs,RegionID,ListOfURLs,URLID)

minDatePerSessionVocab <- vocabQueryClickLog %>% group_by(SessionID) %>% arrange(date) %>% filter(row_number() == 1) %>% mutate(minDateOfSession=date) %>% select(SessionID,minDateOfSession)
vocabQueryClickLog <- vocabQueryClickLog %>% 
    left_join(minDatePerSessionVocab,by="SessionID") %>% 
    mutate(TimePassed=round(date-minDateOfSession,0)) %>% 
    select(SessionID,TimePassed,TypeOfAction,QueryID,searchWords,filterTags,filterLangs,RegionID,ListOfURLs,URLID)

#' 
#' ## Writing Cleaned Logs to File (Yandex Format)
#' 
#' Assemble output txt format:
termYandexLinesQuery <- termQueryClickLog %>% filter(TypeOfAction=="Q") %>% mutate(line=paste(SessionID,TimePassed,TypeOfAction,searchWords,RegionID,gsub(",","\t",ListOfURLs),sep="\t"))
termYandexLinesClick <- termQueryClickLog %>% filter(TypeOfAction=="C") %>% mutate(line=paste(SessionID,TimePassed,TypeOfAction,URLID,sep="\t"))
vocabYandexLinesQuery <- vocabQueryClickLog %>% filter(TypeOfAction=="Q") %>% mutate(line=paste(SessionID,TimePassed,TypeOfAction,searchWords,RegionID,gsub(",","\t",ListOfURLs),sep="\t"))
vocabYandexLinesClick <- vocabQueryClickLog %>% filter(TypeOfAction=="C") %>% mutate(line=paste(SessionID,TimePassed,TypeOfAction,URLID,sep="\t"))
termQueryClickLogYandexFormat <- rbind.fill(termYandexLinesQuery,termYandexLinesClick) %>% arrange(SessionID,TimePassed)
vocabQueryClickLogYandexFormat <- rbind.fill(vocabYandexLinesQuery,vocabYandexLinesClick) %>% arrange(SessionID,TimePassed)

#' Write the joint term and vocabulary query-click-logs to file:
writeLogsToFile <- function(queryClickLog, outputFilename) {
    fileConn <- file(outputFilename)
    writeLines(queryClickLog$line, fileConn)
    close(fileConn)
}
writeLogsToFile(termQueryClickLogYandexFormat, termQueryClickLogYandexFile)
writeLogsToFile(vocabQueryClickLogYandexFormat, vocabQueryClickLogYandexFile)

#' Cleaned logs have been written to: `r termQueryClickLogYandexFile` and `r vocabQueryClickLogYandexFile`.


#' 
#' # Analysis of LOV User Query and Click Behaviour
#' 
#' With the derived dataset which can be used, e.g., to learn a user click model, we analyse and discuss interesting characteristics of the user behaviour.
#' 
#' - For this analysis we use the clean query click logs: termQueryClickLog and vocabQueryClickLog
#'  
#'
#' ## Classes vs. Properties for Term Search
#' 
#' - **Question:** Are users more interested in classes/concepts or properties?
#' - **Motivation:** Some ranking models assume that users search for concepts and do not assign scores for properties. In cases in which LOV users search for properties these features are not contributing any valuable measures for the ranking model.
#' 

#' By convention, properties start with lower case characters, classes with upper case characters
termQueryClickLog <- separate(termQueryClickLog, URLID,c("vocabPrefix","localname"), ":", remove=F)
countClassClicks <- length(which(grepl("^[[:upper:]].+$", termQueryClickLog$localname)))
countPropertyClicks <- length(which(grepl("^[[:lower:]].+$", termQueryClickLog$localname)))
#' Number of clicks on classes:
print(countClassClicks)
#' Number of clicks on properties:
print(countPropertyClicks)

#'
#' - **Answer:** Classes and properties are equally important and properties need to be considered equally.
#'
#' 
#' ## Query length: Number of Words in Keyword Queries
#' 
#' - **Question:** How many words are typically provided by users for keyword searches?
#' - **Motivation:** Some ranking features assume that the input query contains multiple words. If that is not the case, the feature will not compute a meaningful score.
#' 
#' Adding a column with number of words in query:
termQueryClickLog <- termQueryClickLog %>% mutate(countSearchWords=stringr::str_count(searchWords, ' ') + 1)
vocabQueryClickLog <- vocabQueryClickLog %>% mutate(countSearchWords=stringr::str_count(searchWords, ' ') + 1)

#' Computing query length frequencies
queryLengthFrequenciesTerms <- count(termQueryClickLog$countSearchWords %>% na.omit()) %>% select(QueryLength = x, FrequencyInTermLog=freq)
queryLengthFrequenciesVocabs <- count(vocabQueryClickLog$countSearchWords %>% na.omit()) %>% select(QueryLength = x, FrequencyInVocabLog=freq)
queryLengthFrequencies <- queryLengthFrequenciesTerms %>% full_join(queryLengthFrequenciesVocabs,by="QueryLength") %>% arrange(QueryLength)

#' Tabular view of query length frequency for the cleaned and the raw log:
knitr::kable(queryLengthFrequencies) %>% kable_styling("striped", full_width = F)

#' 
#' - **Conclusions:** The majority of searches in LOV is performed with single keywords. For these queries, features that score number of word matches in the query, or location of matched terms within the ontology graph have no valuable contribution to the ranking model. This holds for term and vocab queries.
#'
#' 
#' ## Unique Queries
#' 
#' - **Question:** How often have same queries been issues by different users?
#' - **Motivation:** Clicks are supposed to give a relevance feedback by users. Proper feedback requires many different queries, but also same queries by different users.
#' 
#' Counting the number of times queries occured in different sessions for terms and vocabs:
countSessionsWithSameQueryTerms <- termQueryClickLog %>% group_by(searchWords) %>% dplyr::summarise(sessionCount = n_distinct(SessionID)) %>% na.omit() %>% arrange(desc(sessionCount))
countSessionsWithSameQueryVocabs <- vocabQueryClickLog %>% group_by(searchWords) %>% dplyr::summarise(sessionCount = n_distinct(SessionID)) %>% na.omit() %>% arrange(desc(sessionCount))
topQueriesBySessionCount <- cbind(head(countSessionsWithSameQueryTerms %>% select(TopTermQueries=searchWords,Sessions=sessionCount),10),head(countSessionsWithSameQueryVocabs %>% select(TopVocabQueries=searchWords,Sessions=sessionCount),10))
#' Showing the most popular queries based on the count in how many different sessions they appeared:
knitr::kable(topQueriesBySessionCount) %>% kable_styling("striped", full_width = F)
#' From a total of `r nrow(termQueryClickLog %>% filter(TypeOfAction=="Q"))` term queries, `r nrow(countSessionsWithSameQueryTerms)` distinct queries were made. Of these, `r nrow(countSessionsWithSameQueryTerms %>% filter(sessionCount==1))` were only made once.
#' From a total of `r nrow(vocabQueryClickLog %>% filter(TypeOfAction=="Q"))` vocabulary queries, `r nrow(countSessionsWithSameQueryVocabs)` distinct queries were made. Of these, `r nrow(countSessionsWithSameQueryVocabs %>% filter(sessionCount==1))` were only made once.
#'
#' **Conclusion:** The statistics show that there is sufficient overlap of queries made by different users.

#'
#' ## Unique Clicks/Views
#' 
#' - **Question:** How many terms of the total collection have been clicked and viewed?
#' - **Motivation:** Since we try to generalise user behaviour, this percentage shows whether it is sufficient.
#'
#' The total term and vocabulary count from LOV wer extracted from the LOV.nq vocabulary collection 
countVocsLOV <- 680
countTermsLOV <- 78281
#' Computing views and clicks:
countVocsViewed <- length(unique(unlist(strsplit(vocabQueryClickLog$ListOfURLs,split=',', fixed=TRUE))))
countVocsClicked <- length(unique(vocabQueryClickLog$URLID))
countTermsViewed <- length(unique(unlist(strsplit(termQueryClickLog$ListOfURLs,split=',', fixed=TRUE))))
countTermsClicked <- length(unique(termQueryClickLog$URLID))
countTermsVocClicks <- length(unique(termQueryClickLog$vocabPrefix))
#' From a total of `r format(countVocsLOV,digits=5)` vocabularies, `r countVocsViewed` (`r format(countVocsViewed/countVocsLOV * 100, digits=2, nsmall=2)`%) have been viewed, and `r countVocsClicked` (`r format(countVocsClicked/countVocsLOV * 100, digits=2, nsmall=2)`%) vocabs have been clicked.
#' 
#' From a total of `r countTermsLOV` terms, `r countTermsViewed` (`r format(countTermsViewed/countTermsLOV * 100, digits=2, nsmall=2)`%) have been viewed, and `r countTermsClicked` (`r format(countTermsClicked/countTermsLOV * 100, digits=2, nsmall=2)`%) terms from `r countTermsVocClicks` differenct vocabularies have been clicked.

#' 
#' **Conclusion:** It shows a sufficient coverage of the corpus in terms of clicks and views.
#' 

#'
#' ## Frequency of Clicks at Position X
#' 
#' - **Question:** How often was the term at position X in the result list clicks?
#' - **Motivation:** It is interesting to see whether user are biased towards higher ranked elements in the result list. This insight can be helpful when learning the user click model.
#' 
#' We first add the column with the position of the clicked element in the result list:
termQueryClickLog$ID <- seq.int(nrow(termQueryClickLog))
vocabQueryClickLog$ID <- seq.int(nrow(vocabQueryClickLog))
getClickPositions <- function(queryClickLog) {
    clickPositions <- c()
    for (rowNumber in 1:nrow(queryClickLog)) {
        row <- queryClickLog[rowNumber,]
        if(row$TypeOfAction=="C") {
            click <- row$URLID
            previousLog <- queryClickLog[1:rowNumber,]
            queryResults <- previousLog %>% filter(TypeOfAction=="Q") %>% filter(ID == max(ID)) %>% select(ListOfURLs)
            queryResultList <- strsplit(queryResults$ListOfURLs,split=',', fixed=TRUE)
            clickPosition <- match(click, queryResultList[[1]])
            clickPositions <- c(clickPositions, clickPosition)
        } else {
            clickPositions <- c(clickPositions, NA)
        }
    }
    clickPositions
}
termQueryClickLog$clickPosition <- getClickPositions (termQueryClickLog)
vocabQueryClickLog$clickPosition <- getClickPositions (vocabQueryClickLog)
#' Visualising the distribution of click position
#+ fig.width=5, fig.height=5
hist(termQueryClickLog$clickPosition, breaks=seq(min(termQueryClickLog$clickPosition, na.rm = T)-0.5, max(termQueryClickLog$clickPosition, na.rm = T)+0.5, by=1))
hist(vocabQueryClickLog$clickPosition, breaks=seq(min(vocabQueryClickLog$clickPosition, na.rm = T)-0.5, max(vocabQueryClickLog$clickPosition, na.rm = T)+0.5, by=1))

#' Tabular view of click position frequencies:
clickPositionFrequenciesTerms <- count(termQueryClickLog$clickPosition %>% na.omit()) %>% select(ClickPosition = x, FrequencyInTermLog=freq)
clickPositionFrequenciesVocabs <- count(vocabQueryClickLog$clickPosition %>% na.omit()) %>% select(ClickPosition = x, FrequencyInVocabLog=freq)
clickPositionFrequencies <- clickPositionFrequenciesTerms %>% full_join(clickPositionFrequenciesVocabs,by="ClickPosition") %>% arrange(ClickPosition)
knitr::kable(clickPositionFrequencies) %>% kable_styling("striped", full_width = F)

#' 
#' - **Answer:** As expected, users are biased towards higher ranks.
#'
#' # Conclusion
#'
#' We conclude that the vocabulary click log is not a sufficiently reliable source for inferring relevance labels. However, the term click logs seem like a promising source, being larger in size and showing user behaviour that corresponds to keyword-based searches with a sufficient coverage of the LOV corpus.
#'
#' Command to generate this report:
#' rmarkdown::render('./LOVLogAnalysis.R',output_file = "../resources/output/LOVLogAnalysis.html")

