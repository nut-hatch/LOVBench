# LOVBench: Ontology Ranking Benchmark

## Introduction

This repository contains the sources and resulting datasets and experiments to benchmark ontology ranking models. In summary it contains everything necessary to do the following:

- We analyse user queries and clicks collected from the term search user interface of the [Linked Open Vocabularies (LOV)](https://lov.linkeddata.es/dataset/lov/) platform, derive insight of real-world user behaviour for ontology search and make the cleaned search logs (i.e., with all PII in queries removed) available in the [Yandex](https://github.com/markovi/PyClick/blob/master/examples/data/YandexRelPredChallenge) search log format,
- evaluate the *implicit* user feedback based on expert judgments from [CBRBench](https://zenodo.org/record/11121#.Wmim90tG2Q4), learn and evaluate several user click models with [PyClick](https://github.com/markovi/PyClick) to infer *actual* relevance (forming the ground truth for evaluations), 
- extract 34 ranking features for ontology term search (supported by a [Stardog](https://www.stardog.com/) triple store), follow the same sample selection strategy as used by [LETOR](https://www.microsoft.com/en-us/research/project/letor-learning-rank-information-retrieval/) and build a LTR dataset for ontoloy search for 5-fold cross validation,
- perform LTR experiments (using [RankLib](https://sourceforge.net/p/lemur/wiki/RankLib/)) with two LTR algorithms (RankNet and AdaRank) for three feature sets as proposed in the literature (CBRBench, AKTiveRank, DWRank) as well as newly proposed configurations with feature modifications that improve the ranking performance.

The details of the results and resources are presented in the following paper:

```
Niklas Kolbe, Pierre-Yves Vandenbussche, Sylvain Kubler, Yves Le Traon. 
LOVBench: Ontology Ranking Benchmark. 
The Web Conference, ACM, 2020.
(to appear, full paper)
```

## Highlights (TL;DR)

- Do you want to work with the (cleaned) LOV search logs? [Get the log file!](results/clicklog-analysis/LOV_SearchLogs_Clean.txt)

- Do you want to evaluate the effectiveness of your own ontology ranking model with LOVBench? [Use our ground truth!](results/lovbench-dataset/LOVBench_GroundTruth.csv)

- Do you want to implement new ranking features and/or use LOVBench to extract ranking features for your ontology collection? [Go to the Java library!](lovbench-dataset/)

- Do you want to experiment with different LOVBench feature configurations or different LTR algorithms/parameters? [Use or extend the LTR scripts!](ltr-experiments/)




## Repository Overview 

This repository contains the modules as shown below. Each module folder contains another README for further details.

| Module                                                                                               | Description                                                       | Results/Datasets                                                                                                                                                                                                                                                                                                |
|------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **clicklog-analysis** ([source](clicklog-analysis/), [results](results/clicklog-analysis))           | R script for LOV log exploration                                  | - The LOV search log file ([txt, Yandex format](results/clicklog-analysis/LOV_SearchLogs_Clean.txt))<br>- Generated log analysis report ([html](results/clicklog-analysis/LOVLogAnalysis.html))                                                                                                                  |
| **clickmodel-experiments** ([source](clickmodel-experiments/), [results](results/clicklog-analysis)) | R and python scripts to learn and evaluate user click models      | - Click model's relevance predictions ([csv files](results/clickmodel-experiments/models/))<br>- Click model's performances ([csv](results/clickmodel-experiments/PerformanceResults.csv))<br>- Click model's correlation to CBRBench ([csv](results/clickmodel-experiments/Correlation_CBRBench_LOVBench.csv)) |
| **lovbench-dataset** ([source](lovbench-dataset/), [results](results/lovbench-dataset))              | Java project to extract ranking features and generate LTR dataset | - Ground truth file ([csv](results/lovbench-dataset/LOVBench_GroundTruth.csv))<br>- Ground truth file with extracted features ([csv](results/lovbench-dataset/LOVBench_GroundTruthWithFeatures.csv))<br>- 5-fold LTR dataset ([zip, txt files in RankLib format](results/lovbench-dataset/LOVBench_LTR_5Folds.zip))                     |
| **ltr-experiments** ([source](ltr-experiments/), [results](results/ltr-experiments))                 | R scripts to run LTR experiments                                  | - Ranking model's performances using AdaRank ([csv](results/ltr-experiments/AdaRank_Results.csv))<br>- Ranking model's performances using RankNet ([csv](results/ltr-experiments/RankNet_Results.csv))                                                                                                          |
| **resources** ([folder](resources/))                                                                 | Shared resources across modules                                   | <br>- CBRBench ground truth (easy parsing) ([csv](resources/CBRBench/CBRBenchGroundTruth.csv))<br>- LOV ontology collection used for the experiments ([nq.zip](resources/LOV_Corpus/2019-08-06_lov-fix.nq.zip))                                                                                                         |                                                                                                        |


## License
See the [LICENSE](LICENSE) file for license rights and limitations (Apache License 2.0).


<!--### Results

The results derived resources and experimental results as described in the paper

1. LOV search logs and analysis report ([link to folder](results/clicklog-analysis))
2. Performance of learnt user click models (click and satisfaction probabilities, mapped relevance labels) ([link to folder](results/clicklog-analysis))
3. The LOVBench dataset (ground truth, extracted features, LTR format) ([link to folder](results/lovbench-dataset))
4. Results of the learning to rank (LTR) experiments ([link to folder](results/ltr-experiments))

### Source code / modules

- Source code modules: the source files used to generate the aforementioned resources
	1. [clicklog-analysis](clicklog-analysis/): R script to analyse the LOV search logs 
	2. [clickmodel-experiments]() R and python scripts to learn and evaluate user click models
	3. Java project to extract ranking features
	4. R scripts to run LTR experiments

The resources in the "results" folder can be reviewed as they are. In the following, we present details and prerequisites to run the codes.

## 1-ClickLogs
The script LOVLogAnalysis.R was used to generate the clean log file and the HTML report. It allows insights in our pre-processing, such as removing personal identifiable information from the raw log that in rare cases was captured through the query input. 

For this reason, we do not include the raw logs. However, the HTML report in ./results/1-ClickLogs/LOVLogAnalysis.html provides a detailed documentation of our preprocessing.

## 2-ClickModelExperiments
Python and R scripts are used to learn and evaluate user click models based on the clean LOV search logs.

If wished to run the code, it is necessary to install the PyClick library first: https://github.com/markovi/PyClick

The experiments can then be executed with the file "./code/2-ClickModelExperiments/scripts/run.sh".

## 3-LOVBenchDatasets
A Java project that extracts the ranking features as defined in the paper.

The implementation of ranking features can be reviewed at ./code/3-LOVBenchDataset/src/main/java/experiment/feature/extraction

The features are grouped in ontology and term, as well as relevance and importance features respectively.

Advanced: if wished to the run the feature extraction, one requires a stardog implementation with valid license: https://www.stardog.com/

The implementation can be configured through ./code/3-LOVBenchDataset/src/main/java/experiment/repository/file/ExperimentConfiguration.java

The desired features for extraction can be configured in ./code/3-LOVBenchDataset/src/main/java/experiment/FeatureExperimentApplication.java

The project can then be built with "mvn clean install" and the respective jar file executed.

Extraction of all ranking features for the LOVBench ground truth may take up to several days, depending on the computing resources available.

## 4-LTRExperiments
The LTR experiments can simply be replicated by running ./code/4-LTRExperiments/scripts/run.sh

If not existent, the RankLib library will automatically be downloaded (https://sourceforge.net/p/lemur/wiki/RankLib%20How%20to%20use/)

0-->