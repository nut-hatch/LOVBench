# LOVBench Module: LTR Datasets

A Java project that extracts ranking features for query-term pairs of a given ground truth from an ontology collection.

## Ranking Features

The following features have been implemented:

| ID | Name                        | Scores     | Type       | Implementation                                                                                                                  |
|----|-----------------------------|------------|------------|---------------------------------------------------------------------------------------------------------------------------------|
| 1  | Boolean match               | Terms      | Relevance  | [BooleanMatch.java](src/main/java/experiment/feature/extraction/term/relevance/BooleanMatch.java)                               |
| 2  | Match property boost        | Terms      | Relevance  | [LOVTermMatch.java](src/main/java/experiment/feature/extraction/term/relevance/LOVTermMatch.java)                               |
| 3  | Match description           | Ontologies | Relevance  | [LOVOntologyMatch.java](src/main/java/experiment/feature/extraction/ontology/relevance/LOVOntologyMatch.java)                   |
| 4  | Text relevancy              | Terms      | Relevance  | [TextRelevancy.java](src/main/java/experiment/feature/extraction/term/relevance/TextRelevancy.java)                             |
| 5  | Class match                 | Ontologies | Relevance  | [ClassMatchMeasure.java](src/main/java/experiment/feature/extraction/ontology/relevance/ClassMatchMeasure.java)                 |
| 6  | Property match              | Ontologies | Relevance  | [PropertyMatchMeasure.java](src/main/java/experiment/feature/extraction/ontology/relevance/PropertyMatchMeasure.java)           |
| 7  | Query length                | Query      | -          | [QueryLength.java](src/main/java/experiment/feature/extraction/term/relevance/QueryLength.java)                                 |
| 8  | PageRank (owl:imports)      | Ontologies | Importance | [PageRankImports.java](src/main/java/experiment/feature/extraction/ontology/importance/PageRankImports.java)                    |
| 9  | PageRank (implicit imports) | Ontologies | Importance | [PageRankImplicit.java](src/main/java/experiment/feature/extraction/ontology/importance/PageRankImplicit.java)                  |
| 10 | PageRank (voaf relations)   | Ontologies | Importance | [PageRankVoaf.java](src/main/java/experiment/feature/extraction/ontology/importance/PageRankVoaf.java)                          |
| 11 | Hub                         | Terms      | Importance | [HubDWRank.java](src/main/java/experiment/feature/extraction/term/importance/HubDWRank.java)                                    |
| 12 | Max hub                     | Ontologies | Importance | [MaxHubDWRank.java](src/main/java/experiment/feature/extraction/ontology/importance/MaxHubDWRank.java)                          |
| 13 | Min hub                     | Ontologies | Importance | [MinHubDWRank.java](src/main/java/experiment/feature/extraction/ontology/importance/MinHubDWRank.java)                          |
| 14 | Betweenness                 | Terms      | Importance | [BetweennessMeasureTerms.java](src/main/java/experiment/feature/extraction/term/importance/BetweennessMeasureTerms.java)        |
| 15 | Betweenness                 | Ontologies | Relevance  | [BetweennessMeasure.java](src/main/java/experiment/feature/extraction/ontology/relevance/BetweennessMeasure.java)               |
| 16 | Semantic similarity         | Ontologies | Relevance  | [SemanticSimilarityMeasure.java](src/main/java/experiment/feature/extraction/ontology/relevance/SemanticSimilarityMeasure.java) |
| 17 | TF                          | Terms      | Importance | [TFTerm.java](src/main/java/experiment/feature/extraction/term/importance/TFTerm.java)                                          |
| 18 | IDF                         | Terms      | Importance | [IDFTerm.java](src/main/java/experiment/feature/extraction/term/importance/IDFTerm.java)                                        |
| 19 | TF-IDF                      | Terms      | Importance | [TFIDFTerm.java](src/main/java/experiment/feature/extraction/term/importance/TFIDFTerm.java)                                    |
| 20 | TF                          | Ontologies | Relevance  | [TFOntology.java](src/main/java/experiment/feature/extraction/ontology/relevance/TFOntology.java)                               |
| 21 | IDF                         | Ontologies | Relevance  | [IDFOntology.java](src/main/java/experiment/feature/extraction/ontology/relevance/IDFOntology.java)                             |
| 22 | TF-IDF                      | Ontologies | Relevance  | [TFIDFOntology.java](src/main/java/experiment/feature/extraction/ontology/relevance/TFIDFOntology.java)                         |
| 23 | BM25                        | Terms      | Importance | [BM25Term.java](src/main/java/experiment/feature/extraction/term/importance/BM25Term.java)                                      |
| 24 | BM25                        | Ontologies | Relevance  | [BM25Ontology.java](src/main/java/experiment/feature/extraction/ontology/relevance/BM25Ontology.java)                           |
| 25 | Vector Space Model          | Ontologies | Relevance  | [VSMOntology.java](src/main/java/experiment/feature/extraction/ontology/relevance/VSMOntology.java)                             |
| 26 | Subclasses                  | Terms      | Importance | [Subclasses.java](src/main/java/experiment/feature/extraction/term/importance/Subclasses.java)                                  |
| 27 | Superclasses                | Terms      | Importance | [Superclasses.java](src/main/java/experiment/feature/extraction/term/importance/Superclasses.java)                              |
| 28 | Relations                   | Terms      | Importance | [Relations.java](src/main/java/experiment/feature/extraction/term/importance/Relations.java)                                    |
| 29 | Siblings                    | Terms      | Importance | [Siblings.java](src/main/java/experiment/feature/extraction/term/importance/Siblings.java)                                      |
| 30 | Density                     | Terms      | Importance | [DensityMeasureTerm.java](src/main/java/experiment/feature/extraction/term/importance/DensityMeasureTerm.java)                  |
| 31 | Density                     | Ontologies | Relevance  | [DensityMeasure.java](src/main/java/experiment/feature/extraction/ontology/relevance/DensityMeasure.java)                       |
| 32 | Subproperties               | Terms      | Importance | [Subproperties.java](src/main/java/experiment/feature/extraction/term/importance/Subproperties.java)                            |
| 33 | Superproperties             | Terms      | Importance | [Superproperties.java](src/main/java/experiment/feature/extraction/term/importance/Superproperties.java)                        |
| 34 | Lucene search on rdfs:label | Terms      | Relevance  | [LabelSearch.java](src/main/java/experiment/feature/extraction/term/relevance/LabelSearch.java)                                 |


## How to use

### Requirements

The implementation relies on a stardog triplestore, which requires a valid license: [https://www.stardog.com/](https://www.stardog.com/).

However, other db connectors can be implemented, only the implementation of feature 34 uses specific stardog features (lucene search in sparql query).

The Stardog DB can be initialized by running the script:

```
./src/main/resources/db_setup.sh
```

### Implementing New Features

To implement a new ranking features it is recommended to create a new class in the respective package (depending on whether it is term/ontology and a relevance/importance feature) and further implement the respective abstract class of that feature type. The functions to be implemented already pass all required parameters from the feature extraction process to the scoring function, where the new feature scoring needs to be implemented.


### Run Feature Extraction

Unfortunately, as of now, no command line script / parser is implemented, so the Java source files need to be configured and adjusted for the intended feature extraction and the Java project needs to be compiled:

```
mvn clean install
```

And the resulting jar file needs to be executed.

- The configuration, such as the database connection, can be configured in the class [ExperimentConfiguration.java](src/main/java/experiment/repository/file/ExperimentConfiguration.java).
- The interested features for the extraction run can be specified in [FeatureExperimentApplication.java](src/main/java/experiment/FeatureExperimentApplication.java).

Extraction of all ranking features for the LOVBench ground truth may take up to several days, depending on the computing resources available - extracting features separate or in groups (where cached computations can be used for several features) are recommended.