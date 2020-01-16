
createTermMapping <- function(LOVPrefixFile,LOVnqFile,terms) {
        
    LOVPrefixes <- fromJSON(LOVPrefixFile, flatten = T)$results$binding
    LOVPrefixesSimple <- LOVPrefixes %>% select(prefix = vocabPrefix.value, termPrefix = termPrefix.value, alternativeTermPrefix = termPrefix2.value, vocabUri = vocabURI.value)
    
    LOVnq <- read.table(LOVnqFile, header = F, sep = " ", stringsAsFactors = F, quote="\"", comment.char = "", fill=T)
    names(LOVnq) <- c("s","p","o","g","eol")
    LOVnq <- LOVnq %>% mutate(ontologyUri=gsub('^.|.$', '', g))
    LOVnq <- left_join(LOVnq,LOVPrefixesSimple,by=c("ontologyUri"="vocabUri"))
    
    # satisfactionProbabilityDCTR <- left_join(satisfactionProbabilityDCTR, LOVPrefixesSimple, by=c("prefix")) %>% mutate(term=paste0(termPrefix,localname))
    # satisfactionProbabilityDBN <- separate(satisfactionProbabilityDBN, term,c("prefix","localname"), ":", remove=F)
    # satisfactionProbabilityDBN <- left_join(satisfactionProbabilityDBN, LOVPrefixesSimple, by=c("prefix")) %>% mutate(term=paste0(termPrefix,localname))
    # satisfactionProbabilityUBM <- separate(satisfactionProbabilityUBM, term,c("prefix","localname"), ":", remove=F)
    # satisfactionProbabilityUBM <- left_join(satisfactionProbabilityUBM, LOVPrefixesSimple, by=c("prefix")) %>% mutate(term=paste0(termPrefix,localname))
    # 
    # satisfactionProbabilityDCTR <- separate(satisfactionProbabilityDCTR, term,c("prefix","localname"), ":", remove=F)
    # terms <- satisfactionProbabilityDCTR %>% select(term, prefix,localname) %>% distinct()
    
    
    outdatedTerms <- c("saref:Door", "saref:hasProduction", "saref:Window", "saref:BuildingObject", "saref:isLocatedIn", "saref:IsUsedFor",
                       "saref:contains", "saref:isInterruptionPossible", "saref:hasSingularUnit", "saref:AverageEnergy", "schema:qualifications",
                       "cwrc:hasGenre", "saref:HotWaterEnergy", "saref:hasInputParameter", "saref:hasOutputParameter", "saref:hasMeterReadingTime",
                       "saref:BuildingSpace", "saref:hasConsumption", "cwrc:hasActor", "saref:hasCategory", "saref:DeviceCategory", "saref:hasTask",
                       "saref:LightingEnergy", "cwrc:Genre")
    ambiguousTerms <- c("og:secure_url", "og:height", "og:width", "og:artist", "og:type", "lgdo:fuel", "osgeom:kmGridSquare", "lgdo:atm", "lgdo:machine",
                        "lgdo:recycling", "lgdo:trail", "lgdo:pub", "lgdo:Terminal", "void:Dataset", "moac:Who3W")
    
    
    termMapping <- setNames(data.frame(matrix(ncol = 2, nrow = 0)), c("term", "fullTerm"))
    for (i in 1:nrow(terms)) {
        row <- terms[i,]
        print("########################################")
        print(row$term)
        prefixRow <- LOVPrefixesSimple %>% filter(prefix==row$prefix)
        sMatches <- LOVnq %>% filter(prefix==row$prefix) %>% filter(grepl(paste0(row$localname,">$"),s)) %>% select(fullTerm=s)
        pMatches <- LOVnq %>% filter(prefix==row$prefix) %>% filter(grepl(paste0(row$localname,">$"),p)) %>% select(fullTerm=p)
        oMatches <- LOVnq %>% filter(prefix==row$prefix) %>% filter(grepl(paste0(row$localname,">$"),o)) %>% select(fullTerm=o)
        allMatches <- unique(rbind(sMatches, pMatches, oMatches))
        print(nrow(allMatches))
        mappedTerm <- NA
        if (nrow(allMatches) == 0) {
            if (row$term %in% outdatedTerms) {
                # mappedTerm <- paste0(prefixRow$termPrefix,row$localname)
                mappedTerm <- NA
            } else {
                print("ERROR. Please check, no match found at all!")
                print(row)
                print(prefixRow)
                stop()
            }
        } else if (nrow(allMatches) == 1) {
            print("only one match, nice.")
            allMatches <- allMatches %>% mutate(fullTerm=gsub('^.|.$', '', fullTerm))
            mappedTerm <- allMatches$fullTerm
        } else if (nrow(allMatches) > 1) {
            print("warning, more than 2 matches found!")
            exactTermPrefixMatch <- allMatches %>% filter(fullTerm==paste0("<",prefixRow$termPrefix,row$localname,">"))
            if (nrow(exactTermPrefixMatch) == 1) {
                print("exact prefix match. nice.")
                exactTermPrefixMatch <- exactTermPrefixMatch %>% mutate(fullTerm=gsub('^.|.$', '', fullTerm))
                mappedTerm <- exactTermPrefixMatch$fullTerm
            } else if (nrow(exactTermPrefixMatch) > 1) {
                fullTermsStartingWithTermPrefix <- unique(allMatches %>% filter(grepl(paste0("^<",prefixRow$termPrefix),fullTerm)) %>% mutate(fullTerm=gsub('^.|.$', '', fullTerm)))
                if (nrow(fullTermsStartingWithTermPrefix) > 0) {
                    if (nrow(fullTermsStartingWithTermPrefix) == 1) {
                        print("automatic selection because only 1 match starts with term prefix!")
                        print(fullTermsStartingWithTermPrefix$fullTerm)
                        mappedTerm <- fullTermsStartingWithTermPrefix$fullTerm
                    } else {
                        print("ERROR. Multiple prefix matches. Manually check right match for this term!!")
                        print(row)
                        print(fullTermsStartingWithTermPrefix)
                        stop()
                    } 
                }
            } else {
                exactAlternativeTermPrefixMatch <- allMatches %>% filter(fullTerm==paste0("<",prefixRow$alternativeTermPrefix,row$localname,">"))
                if (nrow(exactAlternativeTermPrefixMatch) == 1) {
                    print("exact alternative prefix match. nice.")
                    exactAlternativeTermPrefixMatch <- exactAlternativeTermPrefixMatch %>% mutate(fullTerm=gsub('^.|.$', '', fullTerm))
                    mappedTerm <- exactAlternativeTermPrefixMatch$fullTerm
                } else if (nrow(exactAlternativeTermPrefixMatch) > 1) {
                    fullTermsStartingWithAlternativeTermPrefix <- unique(allMatches %>% filter(grepl(paste0("^<",prefixRow$alternativeTermPrefix),fullTerm)) %>% mutate(fullTerm=gsub('^.|.$', '', fullTerm)))
                    if (nrow(fullTermsStartingWithAlternativeTermPrefix) > 0) {
                        if (nrow(fullTermsStartingWithAlternativeTermPrefix) == 1) {
                            print("automatic selection because only 1 match starts with ALTERNATIVE term prefix!")
                            print(fullTermsStartingWithAlternativeTermPrefix$fullTerm)
                            mappedTerm <- fullTermsStartingWithAlternativeTermPrefix$fullTerm
                        } else {
                            print("ERROR. Multiple alternative prefix matches. Manually check right match for this term!!")
                            print(row)
                            print(fullTermsStartingWithAlternativeTermPrefix)
                            stop()
                        } 
                    } 
                } else {
                    exactVocabTermPrefixMatch <- allMatches %>% filter(fullTerm==paste0("<",prefixRow$vocabUri,row$localname,">"))
                    if (nrow(exactVocabTermPrefixMatch) == 1) {
                        print("exact vocab prefix match. nice.")
                        exactVocabTermPrefixMatch <- exactVocabTermPrefixMatch %>% mutate(fullTerm=gsub('^.|.$', '', fullTerm))
                        mappedTerm <- exactVocabTermPrefixMatch$fullTerm
                    } else {
                        fullTermsStartingWithVocabUri <- unique(allMatches %>% filter(grepl(paste0("^<",prefixRow$vocabUri),fullTerm)) %>% mutate(fullTerm=gsub('^.|.$', '', fullTerm)))
                        if (nrow(fullTermsStartingWithVocabUri) > 0) {
                            if (nrow(fullTermsStartingWithVocabUri) == 1) {
                                print("automatic selection because only 1 match starts with vocab uri!")
                                print(fullTermsStartingWithVocabUri$fullTerm)
                                mappedTerm <- fullTermsStartingWithVocabUri$fullTerm
                            } else {
                                # if (row$term == "og:secure_url") {
                                #     mappedTerm <- "http://ogp.me/ns#image:secure_url"
                                # } else if (row$term == "og:height") {
                                #     mappedTerm <- "http://ogp.me/ns#image:height"
                                # } else if (row$term == "og:width") {
                                #     mappedTerm <- "http://ogp.me/ns#image:width"
                                # } else if (row$term == "og:artist") {
                                #     mappedTerm <- "http://ogp.me/ns#audio:artist"
                                # } else if (row$term == "og:type") {
                                #     mappedTerm <- "http://ogp.me/ns#audio:type"
                                # } else if (row$term == "lgdo:fuel") {
                                #     mappedTerm <- "http://linkedgeodata.org/ontology/Parking%3Bfuel"
                                # }  else if (row$term == "osgeom:kmGridSquare") {
                                #     mappedTerm <- "http://data.ordnancesurvey.co.uk/ontology/geometry/1kmGridSquare"
                                # }  else if (row$term == "lgdo:atm") {
                                #     mappedTerm <- "http://linkedgeodata.org/ontology/Bank%2Catm"
                                # }  else if (row$term == "lgdo:machine") {
                                #     mappedTerm <- "http://linkedgeodata.org/ontology/Vending+machine"
                                # }  else if (row$term == "lgdo:recycling") {
                                #     mappedTerm <- "http://linkedgeodata.org/ontology/WasteBasket%3Brecycling"
                                # }  else if (row$term == "lgdo:trail") {
                                #     mappedTerm <- "http://linkedgeodata.org/ontology/Fitness+trail"
                                # }  else if (row$term == "lgdo:pub") {
                                #     mappedTerm <- "http://linkedgeodata.org/ontology/Pub/key/amenity/value/pub"
                                # }  else if (row$term == "lgdo:Terminal") {
                                #     mappedTerm <- "http://linkedgeodata.org/ontology/AirportTerminal"
                                if (row$term %in% ambiguousTerms) {
                                    mappedTerm <- NA
                                } else {
                                    print("ERROR. Multiple vocab uri matches. Manually check right match for this term!!")
                                    print(row)
                                    print(fullTermsStartingWithVocabUri)
                                    stop()
                                }
                            } 
                        }
                    }
                }
            }
        }
        print(mappedTerm)
        mapping <- data.frame(row$term, mappedTerm)
        names(mapping) <- c("term", "fullTerm")
        termMapping <- rbind(termMapping, mapping)
    }
    termMapping
}

mapToFullTerms <- function(table, termMaping, outputFile, targetColumn) {
    table <- left_join(table,termMapping,by=c("term"="term"))
    write.csv(table %>% select(query,fullTerm,targetColumn) %>% na.omit() %>% filter(fullTerm != ""), file = outputFile, quote = T, row.names = F)
    fullTermTable <- read.csv(outputFile,header = T,stringsAsFactors = FALSE)
    #columns <- c("query","fullTerm","label")
    #colnames(fullTermTable) <- columns
    fullTermTable
}

sampleRandomNonRelevantTerms <- function(labels,LOVAPIPath,terms) {
    resultColNames <- c("query","fullTerm","label")
    nonRelevantTermsTable <- setNames(data.frame(matrix(ncol = 3, nrow = 0)), resultColNames)
    for (query in unique(labels$query)) {
        # Load all terms in query match
        file <- paste0(LOVAPIPath,gsub("/","_",query),".json")
        if(!file.exists(file)) {
            stop(file)
        }
        LOVQueryResults <- fromJSON(file, flatten = T)$results
        matchedTerms <- unlist(LOVQueryResults$uri)
        
        # select 18 random terms (or as many as available) and add relevance 0
        set.seed(232323)
        alreadyJudged <- labels %>% filter(query==query)
        queryMatchWithoutAlreadyJudgedTerms <- terms %>% filter(term %in% matchedTerms) %>% filter(!term %in% alreadyJudged$fullTerm)
        sampleSize <- 18
        if (nrow(queryMatchWithoutAlreadyJudgedTerms) <= 18) {
            sampleSize <- nrow(queryMatchWithoutAlreadyJudgedTerms)
        }
        sampledNonRelevantTerms <- terms %>% filter(term %in% matchedTerms) %>% filter(!term %in% queryMatchWithoutAlreadyJudgedTerms) %>% sample_n(sampleSize)
        
        # Add to dataframe
        for(nonRelevantTerm in sampledNonRelevantTerms$term) {
            row <- setNames(data.frame(query,nonRelevantTerm,"0"),c("query","fullTerm","label"))
            nonRelevantTermsTable <- rbind(nonRelevantTermsTable,row)
        }
    }
    nonRelevantTermsTable
}

